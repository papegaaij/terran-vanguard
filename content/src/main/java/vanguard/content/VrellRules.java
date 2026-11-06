package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.HazardSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Range;
import vanguard.sim.Section;
import vanguard.sim.StoryPickups;
import vanguard.sim.VrellLevel;
import vanguard.sim.WaveSpec;
import vanguard.sim.WaveSpecBuilder;

/**
 * M11 part A, step A2a: Act 7's unit and level rules for the simulation ({@link UnitRules}, {@link ScriptRules};
 * design/enemies, Window shields and Distorted space; the movement vocabulary's {@code teleport} and {@code mirror};
 * design/enemies/space/gate-warden): a unit's window shield, its line's aim, its hover's rise out of the top edge, its
 * teleport and its mirror at a difficulty; a wave's line aim and stagger; a level's current and its reversals' sections
 * on easy. A unit, wave or level without them as it is.
 */
final class VrellRules {
    private VrellRules() {}

    /**
     * {@code spec} with the unit's Act 7 keys at {@code difficulty}: its window shield ({@code window_shield}), its laser
     * line's aim (the hook's {@code line_aim}, else the line's {@code aim}; aimed when not given), its hover's {@code
     * exit: up} with its {@code exit_speed} and {@code leaving_tell}, its teleport (the hook's ring, jumps and
     * fire-after instead) and its mirror.
     */
    static EnemySpec unit(Content content, EnemyData enemy, Difficulty difficulty, EnemySpec spec) {
        EnemyData.Movement movement = enemy.movement();
        Optional<EnemyData.Hook> hook = UnitRules.hook(enemy, difficulty);
        EnemySpec out = spec;
        if (enemy.windowShield().isPresent()) {
            EnemyData.WindowShield shell = enemy.windowShield().get();
            out = out.withWindowShield(Optional.of(
                    new EnemySpec.WindowShield(shell.hold(), shell.regrow(), shell.iris(), shell.tickGap())));
        }
        if (out.laserLine().isPresent() && straight(enemy, hook, Optional.empty())) {
            out = out.withLaserLine(Optional.of(out.laserLine().get().withStraight(true)));
        }
        Optional<EnemyData.Hover> hover = movement.hover();
        if (out.hover().isPresent() && hover.flatMap(EnemyData.Hover::exitSpeed).isPresent()) {
            out = out.withHover(Optional.of(out.hover()
                    .get()
                    .withExitUp(
                            hover.get().exitSpeed().get(),
                            hover.get().leavingTell().orElse(0.0))));
        }
        if (movement.teleport().isPresent()) {
            out = out.withTeleport(Optional.of(teleport(movement.teleport().get(), hook)));
        }
        if (movement.mirror().isPresent()) {
            EnemyData.MirrorMove mirror = movement.mirror().get();
            out = out.withShadow(Optional.of(new EnemySpec.Shadow(mirror.lag(), mirror.speed(), mirror.minDy())));
        }
        if (movement.lineDrift().isPresent()) {
            // M11 part D, step D2: the Husk's and the seed casket's drift along their waves' lines (CD1's Drift).
            EnemyData.LineDrift drift = movement.lineDrift().get();
            out = out.withLineDrift(
                    Optional.of(new EnemySpec.Drift(UnitRules.range(drift.speed()), UnitRules.range(drift.spin()))));
        }
        if (enemy.crack().isPresent()) {
            // M11 part D, step D2: the Husk's crack (CD1's Crack), its fan the named attack.
            out = out.withCrack(Optional.of(crack(content, enemy, difficulty, hook)));
        }
        return out;
    }

    /**
     * M11 part D, step D2 (design/enemies/space/husk, the crack): the unit's crack at {@code difficulty}: its share and
     * its tell (the difficulty's {@code telegraph} hook instead), its fan the named attack's, sized by the {@code
     * fan_count} hook with its bullets as far apart as the stat block's (the Husk's 15°), at the attack's speed by the
     * bullet-speed lever, fired once (no interval).
     */
    static EnemySpec.Crack crack(
            Content content, EnemyData enemy, Difficulty difficulty, Optional<EnemyData.Hook> hook) {
        EnemyData.Crack crack = enemy.crack().orElseThrow();
        EnemyData.Attack fan = enemy.attack(crack.attack())
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": no attack '" + crack.attack() + "'"));
        int authored = fan.count().orElseThrow();
        int count = hook.flatMap(EnemyData.Hook::fanCount).orElse(authored);
        double apart = authored > 1 ? fan.spread().orElse(0.0) / (authored - 1) : 0;
        return new EnemySpec.Crack(
                crack.at(),
                hook.flatMap(EnemyData.Hook::telegraph).orElse(crack.tell()),
                new vanguard.sim.EnemyGun(
                        Double.POSITIVE_INFINITY,
                        0,
                        1,
                        fan.speed().orElseThrow()
                                * content.difficulty().enemyBulletSpeed().of(difficulty),
                        UnitRules.bulletDamage(content, fan.bullet().orElseThrow()),
                        false,
                        count,
                        Math.toRadians(apart * (count - 1)),
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY));
    }

    /** M11 part D, step D2: whether {@code attack} is the unit's crack's fan (fired by the crack, not a gun). */
    static boolean crackAttack(EnemyData enemy, EnemyData.Attack attack) {
        return enemy.crack()
                .filter(crack -> attack.name().filter(crack.attack()::equals).isPresent())
                .isPresent();
    }

    /**
     * M11 part D, step D2 (design/enemies/space/husk; Level 46): {@code builder} with the wave's drift {@code lines}
     * ({@code [x0, depth0, x1, depth1]}: in at the first point, out past the second) and, with {@code warn: false},
     * without its edge warning; a wave without them as it is.
     */
    static WaveSpecBuilder driftLines(LevelData.Wave wave, WaveSpecBuilder builder) {
        WaveSpecBuilder out = builder;
        if (wave.lines().isPresent()) {
            out = out.lines(wave.lines().get().stream()
                    .map(line -> new WaveSpec.Line(
                            new WaveSpec.At(line.get(0), line.get(1)), new WaveSpec.At(line.get(2), line.get(3))))
                    .toList());
        }
        return wave.warn().filter(warn -> !warn).isPresent() ? out.unwarned() : out;
    }

    /** The teleport's numbers with the difficulty's {@code teleport} hook (the Rift Skater's easy and hard). */
    static EnemySpec.Teleport teleport(EnemyData.Teleport teleport, Optional<EnemyData.Hook> hook) {
        Optional<EnemyData.TeleportChange> change = hook.flatMap(EnemyData.Hook::teleport);
        return new EnemySpec.Teleport(
                teleport.fade(),
                change.flatMap(EnemyData.TeleportChange::ring).orElse(teleport.ring()),
                teleport.minDistance(),
                new Range(teleport.jump().min(), teleport.jump().max()),
                new Range(teleport.angle().min(), teleport.angle().max()),
                teleport.firstGlide(),
                teleport.glide(),
                change.flatMap(EnemyData.TeleportChange::jumps).orElse(teleport.jumps()),
                change.flatMap(EnemyData.TeleportChange::fireAfter).orElse(teleport.fireAfter()),
                teleport.mostRings());
    }

    /**
     * Whether the unit's laser line is fired straight down its frame: the difficulty's {@code line_aim} hook first (the
     * Warden's easy: only straight lines), then the wave's {@code line}, then the line's own {@code aim} (aimed when not
     * given).
     */
    static boolean straight(EnemyData enemy, Optional<EnemyData.Hook> hook, Optional<String> waveAim) {
        return hook.flatMap(EnemyData.Hook::lineAim)
                .or(() -> waveAim)
                .map(EnemyData.Line.STRAIGHT::equals)
                .orElseGet(() -> enemy.attacks().stream()
                        .filter(BeltRules::laserLine)
                        .findFirst()
                        .flatMap(EnemyData.Attack::line)
                        .map(EnemyData.Line::straight)
                        .orElse(false));
    }

    /**
     * {@code spec} with the wave's Act 7 keys at {@code difficulty}: its {@code line} (its units' line aim, unless the
     * difficulty's hook sets every line's) and its {@code line_stagger} (the difficulty's change's instead) in its {@code
     * line_order}; a line abreast's {@code spacing} (step B3b); a wave without them as it is.
     */
    static WaveSpec wave(
            Content content,
            LevelData level,
            LevelData.Wave wave,
            String slug,
            Optional<LevelData.Change> change,
            Difficulty difficulty,
            WaveSpec spec) {
        Optional<Double> stagger = change.flatMap(LevelData.Change::lineStagger).or(wave::lineStagger);
        // Step B3b (Level 43's Warden lines of three): a line abreast's spacing, px between its hovering units.
        Optional<Double> spacing =
                spec.formation() == WaveSpec.Formation.LINE_ABREAST ? wave.spacing() : Optional.empty();
        if (wave.pit().isPresent()) {
            // M11 part C, step C2: a pit's wave rises out of the named pit (CD1's Pits).
            spec = spec.toBuilder().pit(level.pitIndex(wave.pit().get())).build();
        }
        if (wave.line().isEmpty() && stagger.isEmpty() && spacing.isEmpty()) {
            return spec;
        }
        WaveSpec out =
                spacing.isEmpty() ? spec : spec.toBuilder().lineSpacing(spacing).build();
        if (wave.line().isPresent() && spec.enemy().laserLine().isPresent()) {
            EnemyData enemy = content.enemy(slug);
            boolean straight = straight(enemy, UnitRules.hook(enemy, difficulty), wave.line());
            out = out.toBuilder()
                    .enemy(spec.enemy()
                            .withLaserLine(
                                    Optional.of(spec.enemy().laserLine().get().withStraight(straight))))
                    .build();
        }
        if (stagger.isPresent()) {
            out = out.toBuilder()
                    .lineStagger(Optional.of(new WaveSpec.LineStagger(
                            stagger.get(),
                            wave.lineOrder()
                                    .filter(LevelData.Wave.MIDDLE_OUT::equals)
                                    .isPresent(),
                            // M11 part B, step B2: right to left (Level 43's t=188 line).
                            wave.lineOrder()
                                    .filter(LevelData.Wave.RIGHT_LEFT::equals)
                                    .isPresent())))
                    .build();
        }
        return out;
    }

    /** The level's current at {@code difficulty} for the simulation (easy's scaled angles); none without one. */
    static Optional<HazardSpec.Current> current(LevelData level, Difficulty difficulty) {
        return level.current().map(current -> {
            double scale = current.scaleOn(difficulty);
            return new HazardSpec.Current(
                    current.profile().stream()
                            .map(bend -> new HazardSpec.Bend(bend.t(), bend.degrees() * scale))
                            .toList(),
                    current.reversalList().stream()
                            .map(reversal -> new HazardSpec.Reversal(reversal.t(), current.runOn(reversal, difficulty)))
                            .toList(),
                    LevelData.Current.TOLD_SECONDS);
        });
    }

    /**
     * The level's sections for the simulation at {@code difficulty}: on easy each reversal's reversed section ends after
     * easy's {@code run} instead (design/world/vrell-space: easy's reversals run 3 s); the others as they are.
     */
    static List<Section> sections(LevelData level, Difficulty difficulty, List<Section> sections) {
        if (level.current().isEmpty() || difficulty != Difficulty.EASY) {
            return sections;
        }
        LevelData.Current current = level.current().get();
        List<Section> out = new ArrayList<>(sections);
        double start = 0;
        for (int i = 0; i < out.size(); i++) {
            Section section = out.get(i);
            for (LevelData.Reversal reversal : current.reversalList()) {
                if (section.speed() < 0 && Math.abs(start - reversal.t()) < VrellChecks.EPSILON) {
                    out.set(
                            i,
                            new Section(
                                    reversal.runEnd(current.runOn(reversal, difficulty)),
                                    section.speed(),
                                    section.arena(),
                                    section.rampSeconds(),
                                    section.easeSeconds(),
                                    section.holdAfterBossSeconds(),
                                    section.crawl()));
                }
            }
            start = section.end();
        }
        return out;
    }

    /**
     * M11 part A, step A2b: the level's Act 7 parts for the simulation ({@link VrellLevel}): its spore clouds with the
     * world's numbers ({@link VrellSpaceData}; the cloud's drift the backdrop's {@code high-air} scroll factor when it
     * gives one), its quiet, its rush and (M11 part B) its gate transit; {@link VrellLevel#NONE} without any of them.
     */
    static VrellLevel level(Content content, LevelData level, Difficulty difficulty) {
        VrellLevel out = level(content, level);
        if (level.crackCue().isPresent()) {
            // M11 part D, step D2: the quiet's end (Level 46), the cracks in turn (CD1's CrackCues).
            LevelData.CrackCue cue = level.crackCue().get();
            out = out.withCrackCue(Optional.of(new VrellLevel.CrackCue(cue.t(), cue.gapOn(difficulty))));
        }
        return out;
    }

    static VrellLevel level(Content content, LevelData level) {
        VrellLevel out = VrellLevel.NONE;
        if (!level.sporeClouds().isEmpty()) {
            VrellSpaceData.Spores numbers = content.vrellSpace().spores();
            out = out.withSpores(Optional.of(new VrellLevel.Spores(
                    numbers.slow(),
                    numbers.ease(),
                    numbers.burn(),
                    numbers.most(),
                    numbers.veil(),
                    level.backdrop().scrollFactors().getOrDefault(BackdropLayer.HIGH_AIR, numbers.scrollFactor()),
                    level.sporeClouds().stream()
                            .map(cloud -> new VrellLevel.Cloud(cloud.t(), cloud.x(), cloud.radius()))
                            .toList())));
        }
        if (level.quiet().isPresent()) {
            LevelData.Quiet quiet = level.quiet().get();
            out = out.withQuiet(Optional.of(new VrellLevel.Quiet(quiet.from(), quiet.to())));
        }
        if (level.transit().isPresent()) {
            // M11 part B, step B2: the gate transit at the level's end (B1's GateTransit).
            LevelData.Transit transit = level.transit().get();
            out = out.withTransit(
                    Optional.of(new VrellLevel.Transit(transit.form(), transit.warp(), transit.length())));
        }
        if (level.rush().isPresent()) {
            LevelData.Rush rush = level.rush().get();
            out = out.withRush(Optional.of(new VrellLevel.Rush(rush.refill(), rush.everyDifficulty(), rush.par())));
        }
        if (!level.pitList().isEmpty()) {
            // M11 part C, step C2: the reef's spawning pits (CD1's Pits), the world's numbers.
            VrellSpaceData.LivingTerrain terrain = content.vrellSpace().livingTerrain();
            out = out.withPits(Optional.of(new VrellLevel.PitSpec(
                    terrain.tell(),
                    terrain.rise(),
                    terrain.close(),
                    terrain.holdWithin(),
                    level.pitList().stream()
                            .map(pit -> new VrellLevel.Pit(pit.x(), pit.at()))
                            .toList())));
        }
        if (level.leaving().filter(LevelData.Leaving::bottomThird).isPresent()) {
            // M11 part C, step C2: the organs' amber blink in the field's bottom third.
            out = out.withBottomThird(true);
        }
        return out;
    }

    /**
     * M11 part C, step C2 (Level 45's call organ): the level's story pickup's story, its rise and box ({@link
     * StoryPickups.Story}); the Helix core's (Level 34) when the level gives no rise or box.
     */
    static StoryPickups.Story story(LevelData level) {
        return level.pickups().stream()
                .filter(p -> p.story().isPresent()
                        && (p.rise().isPresent() || p.box().isPresent()))
                .findFirst()
                .map(p -> new StoryPickups.Story(
                        p.story().get(),
                        p.rise().orElse(StoryPickups.Story.HELIX_CORE.riseSeconds()),
                        p.box()
                                .map(box -> new Hitbox(box.get(0), box.get(1)))
                                .orElse(StoryPickups.Story.HELIX_CORE.box())))
                .orElse(StoryPickups.Story.HELIX_CORE);
    }
}
