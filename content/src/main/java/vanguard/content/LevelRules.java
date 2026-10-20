package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import vanguard.sim.CueTrigger;
import vanguard.sim.DropSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PickupType;
import vanguard.sim.RadioCue;
import vanguard.sim.Secondary;

/**
 * Part G's level rules for the simulation (design/campaign, Level 07 Brood Carrier), used by
 * {@link SimSpecs#level}: the tows, the pickups a boss's parts drop, the parts objective, and the
 * radio cues' requirements, boss subject and timeout trigger.
 */
final class LevelRules {
    private LevelRules() {}

    /** The level's tows (Level 07's lifeboat), each with its secret's crate. */
    static List<ObjectiveSpec.TowSpec> tows(LevelData level) {
        List<ObjectiveSpec.TowSpec> tows = new ArrayList<>();
        for (LevelData.Tow tow : level.tows().orElse(List.of())) {
            LevelData.Secret secret = level.secrets().stream()
                    .filter(s -> s.name().equals(tow.reveals()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("tows: no secret '" + tow.reveals() + "'"));
            tows.add(new ObjectiveSpec.TowSpec(
                    tow.t(),
                    tow.x(),
                    tow.drift().x(),
                    tow.drift().y(),
                    hitbox(tow.boat()),
                    hitbox(tow.pod()),
                    tow.tether().x(),
                    tow.tether().y(),
                    hitbox(tow.cable()),
                    tow.hits(),
                    secret.crate(),
                    secret.name()));
        }
        return tows;
    }

    /**
     * The pickups the level boss's parts drop (Level 07: the first bay sac shot off), from the
     * {@code carried} pickups of the difficulty whose carrier names parts.
     */
    static List<DropSpec.PartDrop> partDrops(
            Content content,
            LevelData level,
            List<LevelData.PlacedPickup> carried,
            Function<Pickup, PickupType> pickupType) {
        List<DropSpec.PartDrop> drops = new ArrayList<>();
        for (LevelData.PlacedPickup placed : carried) {
            Optional<List<String>> parts = placed.carrier().parts();
            if (parts.isEmpty()) {
                continue;
            }
            String boss = boss(level, "dropped_by.parts");
            drops.add(new DropSpec.PartDrop(
                    boss,
                    partIndexes(content.enemy(boss), parts.get()),
                    switch (placed.carrier().unit()) {
                        case FIRST -> 1;
                        case SECOND -> 2;
                        case LAST -> DropSpec.PartDrop.LAST;
                    },
                    pickupType.apply(placed.pickup())));
        }
        return drops;
    }

    /** The secondary objective for the simulation; a parts objective's parts and phase as indexes into the boss's. */
    static Secondary secondary(Content content, LevelData level, LevelData.Secondary secondary) {
        // M7 part D (Q.25c): every ground ally untouched, or (round 41) standing (Level 17's "No dome lost",
        // SiegeRules).
        Optional<Secondary> allies = SiegeRules.secondary(secondary);
        if (allies.isPresent()) {
            return allies.get();
        }
        if (secondary.afloat().orElse(false)) {
            // M5 part E (user decision E8 = a): the naval convoy afloat at the boss's death.
            return Secondary.afloat(secondary.credits(), secondary.label().orElseThrow());
        }
        if (secondary.integrity().isPresent()) {
            // M5 part G: the defended ally's integrity kept at or above the share (Level 13's relay).
            return Secondary.integrity(
                    secondary.integrity().get(),
                    secondary.credits(),
                    secondary.label().orElseThrow());
        }
        if (secondary.breaksLinks()) {
            // M7 part C (Q.16a): every tendril of the gates broken before it leaves the screen (Level 16's "Barriers").
            return Secondary.links(secondary.credits(), secondary.label().orElseThrow());
        }
        if (secondary.collect().isPresent()) {
            // M5 part F: every weather sonde flown through (Level 12's "Sondes").
            return Secondary.sondes(secondary.credits(), secondary.label().orElseThrow());
        }
        String partsOf = "";
        List<Integer> parts = List.of();
        int beforePhase = -1;
        if (secondary.parts().isPresent()) {
            partsOf = boss(level, "objectives.secondary.parts");
            EnemyData enemy = content.enemy(partsOf);
            parts = partIndexes(enemy, secondary.parts().get());
            String before = secondary.before().orElseThrow();
            List<EnemyData.PhaseData> phases = enemy.boss().orElseThrow().phases();
            for (int i = 0; i < phases.size() && beforePhase < 0; i++) {
                beforePhase = phases.get(i).name().equals(before) ? i : -1;
            }
            if (beforePhase < 0) {
                throw new IllegalArgumentException(partsOf + ": no boss phase '" + before + "'");
            }
        }
        return new Secondary(
                        secondary.killRatio().orElse(0.0),
                        secondary.credits(),
                        secondary.groups().orElse(List.of()),
                        secondary.escapes().orElse(""),
                        secondary.killAll().orElse(List.of()),
                        secondary.label().orElse(""),
                        partsOf,
                        parts,
                        beforePhase,
                        secondary.tag().orElse(""))
                // M7 part F (Level 19's "12 of 14"): how many of an escapes objective's units may get away.
                .withEscapesAllowed(secondary.escapesAllowed());
    }

    /** What a radio cue's {@code requires} or {@code requires_not} names, as the simulation's fitted bits (0 for none). */
    static int requirement(Optional<String> name) {
        return name.map(n -> switch (n) {
                    case Requirement.SPECIAL -> RadioCue.FITTED_SPECIAL;
                    case Requirement.HOMING -> RadioCue.FITTED_HOMING;
                    case Requirement.ESCORT -> RadioCue.FITTED_ESCORT;
                    default -> throw new IllegalArgumentException("requires: unknown '" + n + "'");
                })
                .orElse(0);
    }

    /**
     * A radio cue's subject: the enemy, group or phase it names; for a {@code boss-destroyed} cue the
     * level boss's slug, which the simulation cues it with.
     */
    static String subject(LevelData level, LevelData.RadioCue cue) {
        if (cue.event().orElse(null) == LevelData.CueEvent.BOSS_DESTROYED) {
            return boss(level, "a boss-destroyed cue");
        }
        return cue.enemy().or(cue::group).or(cue::phase).orElse("");
    }

    /** A cue's trigger: a boss-phase cue that waits for a timeout has its own. */
    static CueTrigger trigger(LevelData.RadioCue cue, CueTrigger trigger) {
        return trigger == CueTrigger.BOSS_PHASE && cue.timeout().orElse(false) ? CueTrigger.BOSS_TIMEOUT : trigger;
    }

    private static String boss(LevelData level, String what) {
        return level.boss()
                .map(LevelData.BossPlacement::enemy)
                .orElseThrow(() -> new IllegalArgumentException(what + " needs the level's boss"));
    }

    private static List<Integer> partIndexes(EnemyData enemy, List<String> names) {
        List<Integer> indexes = new ArrayList<>();
        for (String name : names) {
            int index = enemy.partIndex(name);
            if (index < 0) {
                throw new IllegalArgumentException(enemy.name() + ": no part '" + name + "'");
            }
            indexes.add(index);
        }
        return indexes;
    }

    private static Hitbox hitbox(Size size) {
        return new Hitbox(size.width(), size.height());
    }
}
