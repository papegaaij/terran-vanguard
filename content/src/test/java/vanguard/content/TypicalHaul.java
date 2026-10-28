package vanguard.content;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import vanguard.sim.BossSpec;
import vanguard.sim.EnemySpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.PickupType;
import vanguard.sim.Secondary;
import vanguard.sim.WaveSpec;

/**
 * A level's credits at medium from its data (design/systems/economy, per-level budget): a perfect
 * run, and the typical haul, each source weighted by the economy's typical player. Bounties are ×
 * the act factor × the level's bounty scale, rounded half to even per payout; the other payouts
 * only × the act factor. The same model as tools/sync_tables.py's credit-budget table.
 *
 * @param perfect every enemy, ground target, part, pickup, secret and objective
 * @param typical what the typical medium player collects
 * @param budget budget(n) of the economy, the typical haul's target
 */
record TypicalHaul(int perfect, double typical, double budget) {
    /**
     * M9 part C (design/campaign Level 30, Density and pacing): the streams a launcher (the Gilded Gunship) sends before
     * a typical kill.
     */
    static final int TYPICAL_STREAMS = 2;

    /** The typical haul's distance from the budget, as a share of it (+0.1 = 10 % above). */
    double offBudget() {
        return (typical - budget) / budget;
    }

    static TypicalHaul of(Content content, String key) {
        return of(content, key, Difficulty.MEDIUM);
    }

    /**
     * The same from the level as {@code difficulty} builds it (its formation sizes), before that
     * difficulty's credit income; BalanceTest's hard sheet.
     */
    static TypicalHaul of(Content content, String key, Difficulty difficulty) {
        return of(content, key, difficulty, content.level(key).bounties());
    }

    /** The same with the bounties × {@code bountyScale} instead of the level's {@code bounty_scale} (the report's fit). */
    static TypicalHaul of(Content content, String key, Difficulty difficulty, double bountyScale) {
        LevelData data = content.level(key);
        LevelScript level = SimSpecs.level(content, key, difficulty);
        EconomyData economy = content.economy();
        EconomyData.TypicalPlayer rate = economy.typicalPlayer();
        double factor = Math.pow(economy.actFactor(), level.act() - 1);
        Sum sum = new Sum(factor, factor * bountyScale);
        PlayerData.Credits salvage = content.player().pickups().salvage().credits();

        // M9 part B (Level 29, the rescue halt): a typical halt's waves, one cycle of the halt cycle.
        List<WaveSpec> waves = new ArrayList<>(level.waves());
        level.haltCycle().ifPresent(cycle -> waves.addAll(cycle.waves()));
        for (var wave : waves) {
            // M7 part G: a pursuit's refills too (LevelMetrics.planned, as tools/sync_tables.py).
            sum.bounty(LevelMetrics.planned(wave), wave.enemy().bounty(), rate(rate, wave.enemy()));
            // M9 part B: a V-wing's leader pays its airframe's bounty; the typical haul counts it at the wing's (the
            // README's "leader 25, counted at 12"), the perfect run takes the extra.
            wave.leader()
                    .ifPresent(leader ->
                            sum.bounty(1, leader.bounty() - wave.enemy().bounty(), 0));
            // M7 part D: a unit part pays its own bounty (the Shellback's vent: 40 + 12 by parts, as the README's
            // table; M8 part B: each of its parts)
            for (EnemySpec.UnitPart part : wave.enemy().parts()) {
                sum.bounty(wave.count(), part.bounty(), rate(rate, wave.enemy()));
            }
            // a segment chain's unit is its head; its segments and tail pay their own bounty
            wave.enemy().chain().ifPresent(chain -> {
                if (chain.headFirstBonus() > 0) {
                    // M8 part C (step C3, as tools/sync_tables.py): a chain whose head pays a first bonus (the
                    // Threadcrawler's, the Eel Swarm's whole 48) is killed head first by the typical player, the
                    // segments dying with it, and taken apart in a perfect run, the tail first with its bonus.
                    sum.typical(wave.count(), chain.headFirstBonus(), rate(rate, wave.enemy()));
                    sum.bounty(
                            wave.count() * chain.segmentBoxes().size(),
                            chain.segment().bounty(),
                            0);
                    sum.bounty(wave.count(), chain.tail().bounty(), 0);
                    sum.bounty(wave.count(), chain.tailFirstBonus(), 0);
                    return;
                }
                // M9 part C: a train's turret cars pay their own bounty (ChainSpec.memberSpec).
                for (int i = 1; i <= chain.segmentBoxes().size(); i++) {
                    sum.bounty(wave.count(), chain.memberSpec(i).bounty(), rate(rate, wave.enemy()));
                }
                sum.bounty(wave.count(), chain.tail().bounty(), rate(rate, wave.enemy()));
            });
            // M9 part C (Level 30's Gilded Gunships): a launcher's units, two streams before a typical kill (as
            // tools/sync_tables.py).
            wave.enemy()
                    .launcher()
                    .ifPresent(launcher -> sum.bounty(
                            wave.count() * TYPICAL_STREAMS * launcher.stream(),
                            launcher.unit().bounty(),
                            rate(rate, launcher.unit())));
            wave.enemy()
                    .brood()
                    .ifPresent(brood -> sum.bounty(
                            wave.count() * brood.count(), brood.enemy().bounty(), rate(rate, brood.enemy())));
        }
        for (var unit : level.groundUnits()) {
            sum.bounty(1, unit.enemy().bounty(), rate.groundTargets());
            // M8 part B: a ground unit's parts pay their own bounty (the Drill Rig's pumps: 40 + 4 × 4)
            for (EnemySpec.UnitPart part : unit.enemy().parts()) {
                sum.bounty(1, part.bounty(), rate.groundTargets());
            }
            // M5 part C: a periodic spawner (Level 09's Hive Nodes) counts one release, as DensityTest
            unit.enemy()
                    .spawner()
                    .ifPresent(spawner ->
                            sum.bounty(spawner.count(), spawner.enemy().bounty(), rate(rate, spawner.enemy())));
        }
        for (var object : level.groundObjects()) {
            sum.bounty(1, object.bounty(), rate.groundTargets());
            sum.pay(1, credits(salvage, object.drop()), rate.groundTargets() * rate.pickups());
        }
        JsonNode bossNotes = bossNotes(key);
        int streams = bossNotes.path("streams").asInt(0);
        for (var piece : level.setPieces()) {
            // a boss dies in every won run; another set piece's parts are ground-target-like
            double share = piece.boss().isPresent() ? 1 : rate.groundTargets();
            for (var part : piece.parts()) {
                sum.bounty(1, part.bounty(), share);
            }
            sum.pay(1, credits(salvage, piece.drop()), share * rate.pickups());
            for (BossSpec.Phase phase : piece.boss().map(BossSpec::phases).orElse(List.of())) {
                phase.stream()
                        .ifPresent(stream -> sum.bounty(
                                streams * stream.count(), stream.enemy().bounty(), rate(rate, stream.enemy())));
            }
            // part G: the units a boss's windows launch in a typical fight (Level 07's Skitters and Needlers)
            for (EnemySpec kind : piece.boss().map(BossSpec::spawnKinds).orElse(List.of())) {
                int launched = bossNotes.path("spawns").path(kind.slug()).asInt(0);
                sum.bounty(launched, kind.bounty(), rate(rate, kind));
            }
        }
        // Each saveable unit home pays (M5 part D: not the scripted loss's, Level 10's Lifeline Three).
        // M9 part D (Level 31's mining tug): a hull escort pays each section home its own credits.
        level.objectives().escort().ifPresent(escort -> escort.hull()
                .ifPresentOrElse(
                        hull -> hull.sections().forEach(section -> sum.pay(1, section.credits(), rate.primary())),
                        () -> sum.pay(escort.saveable(), escort.credits(), rate.primary())));
        for (var secret : data.secrets()) {
            sum.pay(1, secret.crate(), rate.secrets());
        }
        Secondary secondary = level.objectives().secondary();
        int payouts = secondary.byGroups() ? secondary.groups().size() : 1;
        sum.pay(payouts, secondary.credits(), rate.secondary());

        return new TypicalHaul(sum.perfect, sum.typical, economy.budget().of(level.number()));
    }

    private static double rate(EconomyData.TypicalPlayer rate, EnemySpec enemy) {
        return enemy.layer() == Layer.GROUND ? rate.groundTargets() : rate.airKills();
    }

    private static int credits(PlayerData.Credits salvage, Optional<PickupType> drop) {
        return switch (drop.orElse(PickupType.OVERDRIVE)) {
            case SMALL_SALVAGE -> salvage.small();
            case MEDIUM_SALVAGE -> salvage.medium();
            case LARGE_SALVAGE -> salvage.large();
            default -> 0;
        };
    }

    /**
     * The level data's {@code boss.notes}: {@code streams}, the boss streams a fight at par sends, and
     * {@code spawns}, the units its windows launch in a typical fight by enemy slug.
     */
    private static JsonNode bossNotes(String key) {
        Path file = DesignTree.ROOT.resolve("campaign").resolve(key).resolve("data.yaml");
        try {
            return YAMLMapper.builder()
                    .build()
                    .readTree(Files.readString(file))
                    .path("boss")
                    .path("notes");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class Sum {
        private final double payFactor;
        private final double bountyFactor;
        int perfect;
        double typical;

        Sum(double payFactor, double bountyFactor) {
            this.payFactor = payFactor;
            this.bountyFactor = bountyFactor;
        }

        void bounty(int count, int credits, double share) {
            add(count, (int) Math.rint(credits * bountyFactor), share);
        }

        /** A bounty only the typical player collects (a perfect run takes the unit apart instead). */
        void typical(int count, int credits, double share) {
            typical += count * (int) Math.rint(credits * bountyFactor) * share;
        }

        void pay(int count, int credits, double share) {
            add(count, (int) Math.rint(credits * payFactor), share);
        }

        private void add(int count, int paid, double share) {
            perfect += count * paid;
            typical += count * paid * share;
        }
    }
}
