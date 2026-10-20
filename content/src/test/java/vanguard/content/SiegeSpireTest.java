package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import vanguard.sim.Allocations;
import vanguard.sim.Armament;
import vanguard.sim.BossSpec;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.PartSpec;
import vanguard.sim.PickupType;
import vanguard.sim.PlayField;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WingmanSpec;

/**
 * M5 part H, step H4: the Siege Spire from its data file (design/enemies/bosses/siege-spire; decisions H1,
 * H3–H5 and H10 = a of 2026-10-11, pending the user's review) on a fixture arena: 3 s at 140 px/s, then the
 * arena at speed 0 where it arrives, then the daylight; the balance plan's Level 14 fit (the front at L4, two
 * Micro-missile Pods at L2, the Tail Gun at L2, Armour Mk III, Rook's Autocannon, the Airstrike's charges)
 * flown by the {@link Autopilot}, which shoots the lowest exposed part, sidesteps the lobs' markers, keeps
 * out of the sweep's reach and lines up over the maw's Wraiths for the rear gun. The fight's length at
 * medium is the one the HP are tuned to (H10 = a: a mean of about 135 s, every seed in the act-boss window
 * of 90–180 s) and the par.
 *
 * <p>Round 40 (proposals 4a–4c of the Act 2 playtest, approved 2026-10-18): the pods fire straight down their
 * columns and take turns to aim at the ship after a glow, the maw's fan is thinner and one Wraith comes a launch
 * (at most 5 hits a minute in every phase for the autopilot), the roots 460/530 HP so the front gun alone ends the
 * fight inside the act-boss window.
 */
class SiegeSpireTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    static final String SLUG = "siege-spire";
    /** The rules of an Act 2 level. */
    private static final String RULES_LEVEL = Level13Test.LEVEL;

    static final double ARENA = 3;
    static final int POD_1 = 0;
    static final int MORTAR_LEFT = 4;
    static final int MORTAR_RIGHT = 5;
    static final int MAW = 6;
    static final int CORE = 7;
    static final int TRUNK = 8;
    static final List<Integer> ROOTS = List.of(0, 1, 2, 3, 4, 5);
    /** The seeds the autopilot's fight is measured on. */
    static final long[] SEEDS = {2185, 11, 77, 1301, 4242};

    static SetPieceSpec spire(Difficulty difficulty) {
        return SimSpecs.boss(CONTENT, new LevelData.BossPlacement(SLUG, ARENA, PlayField.WIDTH / 2.0, 2), difficulty);
    }

    static LevelScript level(Difficulty difficulty) {
        return LevelScript.builder(14, 2)
                .sections(List.of(
                        new Section(ARENA, 140), new Section(ARENA + 200, 0, true), new Section(ARENA + 212, 140)))
                .setPieces(List.of(spire(difficulty)))
                .build();
    }

    /**
     * The balance plan's fit at Level 14 (design/player/balance-plan.yaml: Level 13's with the Tail Gun at
     * L2; Rook's Autocannon; the Airstrike's charges at their most, 4).
     */
    static Loadout planLoadout(Content content, Difficulty difficulty) {
        return SimSpecs.loadout(
                        content,
                        content.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "tail-gun", 2)),
                        content.shields().models().get(1).name(),
                        content.armour().plating().get(2).name(),
                        0,
                        difficulty)
                .withWingman(SimSpecs.wingman(
                        content,
                        "autocannon",
                        1,
                        WingmanSpec.Side.LEFT,
                        content.wingmen().rook().armour()))
                .withSpecial(SimSpecs.special(content, "Airstrike", 4));
    }

    /** Round 40: the user's playtest fit at Level 14 without the wings (the Pulse Cannon at L4, Rook's Autocannon L2). */
    static Loadout pulseOnlyLoadout(Difficulty difficulty) {
        return SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 4)),
                        CONTENT.shields().models().get(1).name(),
                        CONTENT.armour().plating().get(1).name(),
                        0,
                        difficulty)
                .withWingman(SimSpecs.wingman(
                        CONTENT,
                        "autocannon",
                        2,
                        WingmanSpec.Side.LEFT,
                        CONTENT.wingmen().rook().armour()));
    }

    /**
     * Round 40: the user's playtest fit (pt2-b: the Pulse Cannon L4, a Micro-missile Pod L2, the Bomb Rack L3, the
     * Side Splitter L1, Mk II, Composite I, Rook's Autocannon L2).
     */
    static Loadout userLoadout(Difficulty difficulty) {
        return SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 4),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 2),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "bomb-rack", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.REAR, "side-splitter", 1)),
                        CONTENT.shields().models().get(1).name(),
                        CONTENT.armour().plating().get(1).name(),
                        0,
                        difficulty)
                .withWingman(SimSpecs.wingman(
                        CONTENT,
                        "autocannon",
                        2,
                        WingmanSpec.Side.LEFT,
                        CONTENT.wingmen().rook().armour()));
    }

    static Sortie sortie(long seed, Difficulty difficulty) {
        return sortie(seed, difficulty, planLoadout(CONTENT, difficulty));
    }

    static Sortie sortie(long seed, Difficulty difficulty, Loadout loadout) {
        return new Sortie(
                seed,
                loadout,
                level(difficulty),
                SimSpecs.rules(CONTENT, RULES_LEVEL, difficulty),
                loadout.plating().maxArmour());
    }

    private static List<Integer> values(Sortie sortie, SimEvents.Type type) {
        List<Integer> values = new ArrayList<>();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == type) {
                values.add(events.value(i));
            }
        }
        return values;
    }

    /** What one autopilot run of the fight saw. */
    record Fight(
            double seconds,
            boolean killed,
            double armourLost,
            double rootsSeconds,
            double mawSeconds,
            boolean lifted,
            int launched,
            int lobs,
            int sweeps,
            double sweepDamage,
            boolean flying,
            double[] lostByPhase,
            double lobDamage,
            int[] hitsByPhase) {
        /** Round 40: the steps the ship lost shield or armour in, a minute of phase {@code phase} (0–2). */
        double hitsPerMinute(int phase) {
            double[] ends = {rootsSeconds, mawSeconds, seconds};
            double length = phase == 0 ? rootsSeconds : ends[phase] - ends[phase - 1];
            return Double.isNaN(length) || length <= 0 ? 0 : hitsByPhase[phase] * 60 / length;
        }

        @Override
        public String toString() {
            return String.format(
                    "%.1f s%s, armour lost %.0f, roots %.1f s, maw %.1f s, lifted %b, launched %d, lobs %d, sweeps %d"
                            + " (hit %.0f), lost by phase %.0f/%.0f/%.0f (lob impacts %.0f), hits/min %.1f/%.1f/%.1f",
                    seconds,
                    killed ? "" : " NOT KILLED",
                    armourLost,
                    rootsSeconds,
                    mawSeconds - rootsSeconds,
                    lifted,
                    launched,
                    lobs,
                    sweeps,
                    sweepDamage,
                    lostByPhase[0],
                    lostByPhase[1],
                    lostByPhase[2],
                    lobDamage,
                    hitsPerMinute(0),
                    hitsPerMinute(1),
                    hitsPerMinute(2));
        }
    }

    /** The autopilot flies the fixture until the Spire dies (at most 4 minutes). */
    static Fight fight(long seed, Difficulty difficulty) {
        return fight(seed, difficulty, planLoadout(CONTENT, difficulty), 240);
    }

    /** The same with {@code loadout}, at most {@code seconds}. */
    static Fight fight(long seed, Difficulty difficulty, Loadout loadout, double seconds) {
        Sortie sortie = sortie(seed, difficulty, loadout);
        SetPiece spire = sortie.setPiece(0);
        double armour = sortie.ship().defences().armour();
        double roots = Double.NaN;
        double maw = Double.NaN;
        boolean lifted = false;
        int launched = 0;
        int lobs = 0;
        int sweeps = 0;
        double sweepDamage = 0;
        int present = 0;
        double[] lostByPhase = new double[3];
        int[] hitsByPhase = new int[3];
        double lobDamage = 0;
        for (int i = 0; i < SimStep.ticks(seconds) && !spire.destroyed(); i++) {
            present += spire.present() ? 1 : 0;
            double before =
                    sortie.ship().defences().shield() + sortie.ship().defences().armour();
            sortie.step(Autopilot.commands(sortie));
            double lost = Math.max(
                    0,
                    before
                            - sortie.ship().defences().shield()
                            - sortie.ship().defences().armour());
            if (!values(sortie, SimEvents.Type.SWEEP_HIT).isEmpty()) {
                sweepDamage += lost;
            }
            if (!values(sortie, SimEvents.Type.MORTAR_IMPACT).isEmpty()) {
                lobDamage += lost;
            }
            lostByPhase[Math.min(2, spire.phase())] += lost;
            hitsByPhase[Math.min(2, spire.phase())] += lost > 0 && spire.engaged() ? 1 : 0;
            if (spire.phase() >= 1 && Double.isNaN(roots)) {
                roots = present * SimStep.SECONDS;
            }
            if (spire.phase() >= 2 && Double.isNaN(maw)) {
                maw = present * SimStep.SECONDS;
            }
            lifted |= spire.lifted();
            launched += values(sortie, SimEvents.Type.BOSS_LAUNCHED).size();
            lobs += values(sortie, SimEvents.Type.MORTAR_LOBBED).size();
            sweeps += values(sortie, SimEvents.Type.SPIRE_SWEEP).size();
            if (!sortie.flying()) {
                break;
            }
        }
        return new Fight(
                spire.destroyed() ? spire.killSeconds() : present * SimStep.SECONDS,
                spire.destroyed(),
                armour - sortie.ship().defences().armour(),
                roots,
                maw,
                lifted,
                launched,
                lobs,
                sweeps,
                sweepDamage,
                sortie.flying(),
                lostByPhase,
                lobDamage,
                hitsByPhase);
    }

    @Test
    void theStatBlockMapsToAnAnchoredArenaBossWithoutLanes() {
        SetPieceSpec spec = spire(Difficulty.MEDIUM);
        BossSpec boss = spec.boss().orElseThrow();
        assertTrue(boss.anchored());
        assertFalse(boss.midBoss(), "the act boss's long bar");
        assertEquals("SIEGE SPIRE", boss.barName());
        assertEquals(PlayField.HEIGHT - 196, boss.hoverY(), "the anchor 196 px below the top edge (the concept's)");
        assertEquals(Layer.GROUND, boss.layer());
        assertEquals(
                List.of("pod 1", "pod 2", "pod 3", "pod 4", "mortar left", "mortar right", "maw", "core", "trunk"),
                spec.parts().stream().map(PartSpec::name).toList());
        assertEquals(List.of(TRUNK), boss.armoured(), "the trunk: shots glance off it, it stays as the stump");
        assertTrue(spec.parts().get(CORE).vital());
        assertEquals(2, spec.parts().get(MAW).multiplier(), "the lime maw ×2");
        assertEquals(2, spec.parts().get(CORE).multiplier(), "the lime core ×2");
        assertEquals(460, spec.parts().get(POD_1).hp(), "round 40 (4b): 690 -> 460");
        assertEquals(530, spec.parts().get(MORTAR_LEFT).hp(), "round 40 (4b): 800 -> 530");
        assertEquals(2880, spec.parts().get(MAW).hp());
        assertEquals(3450, spec.parts().get(CORE).hp(), "round 41: 4950 -> 3450 with the leading sweep");
        assertEquals(PAR, boss.parSeconds(), "the par: the measured mean");

        BossSpec.Arena arena = boss.arena().orElseThrow();
        assertEquals(BossSpec.Lanes.NONE, arena.lanes(), "no slam lanes (H1 = a)");
        assertTrue(arena.partLayers().stream().allMatch(layer -> layer == Layer.GROUND), "rooted: all on the ground");

        BossSpec.Attack orbs = boss.attacks().get(0);
        assertEquals(BossSpec.Pattern.AIMED, orbs.pattern());
        assertTrue(orbs.stagger(), "each pod on its own clock");
        assertEquals(List.of(0, 1, 2, 3), orbs.parts());
        assertEquals(1.6, orbs.gun().intervalSeconds(), 1e-9);
        assertEquals(List.of(0, 1, 2, 3), orbs.down(), "round 40 (4a): every pod fires straight down its column");
        assertEquals(
                Optional.of(new BossSpec.AimTurns(2.0, 0.6)), orbs.aimTurns(), "one pod at a time aims, after a glow");
        BossSpec.Attack acid = boss.attacks().get(1);
        assertEquals(BossSpec.Pattern.LOB, acid.pattern(), "the mortar roots' lob (H5 = a)");
        assertTrue(acid.stagger());
        assertEquals(List.of(MORTAR_LEFT, MORTAR_RIGHT), acid.parts());
        assertEquals(8, acid.gun().intervalSeconds(), 1e-9, "round 34, P2: was 4");
        assertEquals(1.0, acid.gun().mortar().orElseThrow().flightSeconds());
        assertEquals(10, acid.gun().mortar().orElseThrow().ring());
        assertEquals(10, acid.gun().damage(), "the direct hit `heavy`");
        BossSpec.Attack spit = boss.attacks().get(2);
        assertEquals(BossSpec.Pattern.FAN, spit.pattern());
        assertEquals(7, spit.gun().fan(), "round 40 (4a): 9 -> 7");
        assertEquals(3.0, spit.gun().intervalSeconds(), 1e-9, "round 40 (4a): 2.5 -> 3.0");
        BossSpec.Attack sweep = boss.attacks().get(3);
        assertEquals(BossSpec.Pattern.SWEEP, sweep.pattern(), "the free spire's laser sweep (H5 = a)");
        assertEquals(Math.toRadians(120), sweep.sweep().orElseThrow().arcRadians(), 1e-9);
        assertEquals(0.8, sweep.sweep().orElseThrow().telegraphSeconds(), "round 41: 0.6 -> 0.8");
        assertEquals(600, sweep.sweep().orElseThrow().length(), "round 41: 300 -> 600, every corner of the field");
        assertEquals(
                Math.toRadians(12),
                sweep.sweep().orElseThrow().leadRadians(),
                1e-9,
                "round 41: the arc starts 12° beside the ship's bearing, so a still ship is swept");
        assertEquals(8, sweep.sweep().orElseThrow().damage(), "`laser`");
        assertEquals(List.of(), sweep.parts(), "fired by the part its phase ends on, the core");
        assertEquals(14, boss.attacks().get(4).count(), "the ring of 14");

        BossSpec.Phase roots = boss.phases().get(0);
        assertEquals(ROOTS, roots.untilParts());
        assertEquals(1.5, roots.delaySeconds(), "round 41: no fire under the warning banner, the first orb ≈4 s in");
        BossSpec.Phase maw = boss.phases().get(1);
        assertEquals(List.of(MAW), maw.exposes());
        BossSpec.Windows windows = maw.windows().orElseThrow();
        assertEquals(8, windows.everySeconds());
        BossSpec.Spawn wraiths = windows.spawns().getFirst();
        assertEquals("wraith", wraiths.enemy().slug());
        assertEquals(1, wraiths.count(), "round 40 (4a): 2 -> 1 a launch");
        assertEquals(2, wraiths.ambushMost(), "ambush launches (H3 = a), at most 2 alive (round 40: 4)");
        BossSpec.Phase free = boss.phases().get(2);
        BossSpec.Move lift = free.move().orElseThrow();
        assertEquals(Layer.AIR, lift.layer(), "the core onto air (H4 = a)");
        assertEquals(3, lift.descendSeconds(), "the 3 s lift");
        assertEquals(PlayField.WIDTH / 2.0, lift.x());
        assertEquals(PlayField.HEIGHT - 120, lift.y(), "its station in the upper half");
        assertEquals(1, lift.pose());
        assertEquals("free", boss.poses().get(1).name());
        assertTrue(free.alternate(), "the sweep and the ring in turn");
        assertEquals(Optional.of(PickupType.ARMOUR_PATCH), free.arena().drop(), "the shockwave's patch");
        assertEquals(100, boss.sineAmplitude(), "the free spire's sway ±100 px");
        assertEquals(50, 2 * Math.PI * boss.sineAmplitude() / boss.sinePeriod(), 0.01, "at 50 px/s at most");

        BossSpec hard = spire(Difficulty.HARD).boss().orElseThrow();
        assertEquals(12, hard.attacks().get(1).gun().mortar().orElseThrow().ring(), "hard: the lob's ring of 12");
        assertEquals(
                Math.toRadians(180), hard.attacks().get(3).sweep().orElseThrow().arcRadians(), 1e-9);
        assertEquals(
                2,
                hard.phases().get(1).windows().orElseThrow().spawns().getFirst().count(),
                "hard: 2 Wraiths (round 40: 3)");
        assertEquals(460 * 1.3, spire(Difficulty.HARD).parts().get(POD_1).hp(), 1);
    }

    /** The par (round 40: 130 -> the measured mean with the lighter roots, 4b). */
    static final double PAR = 103;

    /**
     * H10 = a: the autopilot's fight with the plan's fit lasts its par at medium (the bar appearing to the kill;
     * round 40, 4b: about 103 s, was 135), inside the act-boss window 90–180 s on every seed; every phase happens
     * (the roots, the maw with its launches, the lift and the free spire's sweeps); round 40 (4a): at most 5 hits
     * a minute in every phase.
     */
    @Test
    void theAutopilotsFightLastsItsParAtMedium() {
        double sum = 0;
        for (long seed : SEEDS) {
            Fight fight = fight(seed, Difficulty.MEDIUM);
            System.out.printf("Spire medium seed %d: %s%n", seed, fight);
            assertTrue(fight.killed(), "killed: " + fight);
            assertTrue(LevelMetrics.inBossWindow(false, fight.seconds()), "the act-boss window: " + fight);
            assertTrue(fight.lifted() && fight.launched() >= 2 && fight.sweeps() >= 1, "every phase: " + fight);
            for (int phase = 0; phase < 3; phase++) {
                assertTrue(
                        fight.hitsPerMinute(phase) <= 5,
                        "at most 5 hits a minute in phase " + (phase + 1) + ": " + fight);
            }
            sum += fight.seconds();
        }
        double mean = sum / SEEDS.length;
        double par = spire(Difficulty.MEDIUM).boss().orElseThrow().parSeconds();
        System.out.printf("Spire medium: mean %.1f s, par %.0f%n", mean, par);
        assertEquals(mean, par, 2, "the par is the measured mean");
    }

    /**
     * Round 40 (4b, the user: "My L4 pulse cannon takes ages to break a single root"): the Pulse Cannon at L4 with
     * Rook's Autocannon and no wing weapons ends the fight inside the act-boss window (at most 180 s) at medium
     * whenever the autopilot survives it, which it does on most seeds (without a rear gun the maw's Wraiths wear it
     * down on one); the user's own fit (with the Micro-missile Pod) is printed and always wins.
     */
    @Test
    void thePulseCannonAloneEndsTheFightInTheWindow() {
        int killed = 0;
        for (long seed : SEEDS) {
            Fight fight = fight(seed, Difficulty.MEDIUM, pulseOnlyLoadout(Difficulty.MEDIUM), 300);
            Fight user = fight(seed, Difficulty.MEDIUM, userLoadout(Difficulty.MEDIUM), 300);
            System.out.printf("Spire pulse-only medium seed %d: %s%n", seed, fight);
            System.out.printf("Spire user-fit medium seed %d: %s%n", seed, user);
            assertTrue(!fight.killed() || fight.seconds() <= 180, "the Pulse Cannon alone: " + fight);
            assertTrue(user.killed() && user.seconds() <= 180, "the user's fit: " + user);
            killed += fight.killed() ? 1 : 0;
        }
        assertTrue(killed >= SEEDS.length - 1, killed + " won with the Pulse Cannon alone");
    }

    /**
     * Easy and hard are measured too (printed; the window is medium's): easy is won on every seed; on hard
     * (×1.3 HP, the ring of 12, the 180° sweep, 2 Wraiths a launch) the autopilot, a simple pilot, is worn
     * down on some seeds.
     */
    @ParameterizedTest
    @EnumSource(
            value = Difficulty.class,
            names = {"EASY", "HARD"})
    void theFightOnEasyAndHard(Difficulty difficulty) {
        double sum = 0;
        int killed = 0;
        for (long seed : SEEDS) {
            Fight fight = fight(seed, difficulty);
            System.out.printf("Spire %s seed %d: %s%n", difficulty, seed, fight);
            if (fight.killed()) {
                killed++;
                sum += fight.seconds();
            }
        }
        System.out.printf("Spire %s: %d of %d won, mean %.1f s%n", difficulty, killed, SEEDS.length, sum / killed);
        assertTrue(killed >= (difficulty == Difficulty.EASY ? SEEDS.length : 3), killed + " won");
    }

    /**
     * Round 40 (4a, the Spire's lanes): every pod fires its orbs straight down its column (safe lanes between),
     * and one pod at a time takes its aim turn: every 2.0 s the next living pod in turn (1, 2, 3, 4, 1, …) glows
     * for 0.6 s (the {@code AIM_TELL} at its start; never two pods at once), then fires one orb at the ship. The
     * ship is parked off the pods' columns.
     */
    @Test
    void thePodsFireDownTheirColumnsAndTakeTurnsToAim() {
        Sortie sortie = sortie(2185, Difficulty.MEDIUM);
        SetPiece spire = sortie.setPiece(0);
        int guard = 0;
        while (!spire.engaged() && guard++ < SimStep.ticks(120)) {
            sortie.step(0);
        }
        assertTrue(spire.engaged());
        int down = 0;
        List<Integer> aimed = new ArrayList<>();
        List<Double> aimedAt = new ArrayList<>();
        List<Integer> tells = new ArrayList<>();
        List<Double> tellAt = new ArrayList<>();
        double firstOrb = -1;
        // Round 41: the Roots phase holds its fire 1.5 s after the halt (no fire under the warning banner).
        for (int i = 0; i < SimStep.ticks(12) && sortie.flying(); i++) {
            sortie.step(0);
            double t = (i + 1) * SimStep.SECONDS;
            for (int pod : values(sortie, SimEvents.Type.AIM_TELL)) {
                tells.add(pod);
                tellAt.add(t);
            }
            int glowing = 0;
            for (int pod = 0; pod < 4; pod++) {
                glowing += spire.partAimGlowing(pod) ? 1 : 0;
            }
            assertTrue(glowing <= 1, "one pod at a time glows");
            for (int b = 0; b < sortie.bulletCount(); b++) {
                vanguard.sim.EnemyBullet bullet = sortie.bullet(b);
                for (int pod = 0; pod < 4; pod++) {
                    double dx = bullet.renderX(1) - spire.partX(pod);
                    double dy = bullet.renderY(1) - spire.partY(pod);
                    // A new orb: one step (150 px/s) from its pod.
                    if (Math.hypot(dx, dy) > 3) {
                        continue;
                    }
                    if (firstOrb < 0) {
                        firstOrb = t;
                    }
                    if (Math.abs(Math.abs(bullet.heading()) - Math.PI) < 1e-9) {
                        down++;
                    } else {
                        double toShip = StrictMath.atan2(
                                sortie.ship().x() - spire.partX(pod),
                                sortie.ship().y() - spire.partY(pod));
                        assertEquals(toShip, bullet.heading(), 0.05, "at the ship from pod " + (pod + 1));
                        aimed.add(pod);
                        aimedAt.add(t);
                    }
                }
            }
        }
        assertTrue(down >= 8, down + " straight down");
        assertTrue(firstOrb >= 1.5, "round 41: no orb before the Roots phase's 1.5 s delay, the first at " + firstOrb);
        assertEquals(List.of(0, 1, 2, 3, 0), aimed.subList(0, 5), "the pods aim in turn");
        for (int k = 1; k < aimed.size(); k++) {
            assertEquals(2.0, aimedAt.get(k) - aimedAt.get(k - 1), 0.05, "an aimed orb every 2.0 s");
        }
        for (int k = 0; k < aimed.size(); k++) {
            assertEquals(aimed.get(k), tells.get(k), "the glow names the pod that fires");
            assertEquals(0.6, aimedAt.get(k) - tellAt.get(k), 0.05, "after a 0.6 s glow");
        }
    }

    @Test
    void theArenaIsAnchoredWhereTheScrollHaltsWithAllSixRootsOnTheField() {
        Sortie sortie = sortie(1, Difficulty.MEDIUM);
        SetPiece spire = sortie.setPiece(0);
        boolean approached = false;
        while (!spire.engaged()) {
            sortie.step(0);
            approached |= spire.approaching();
        }
        assertTrue(approached);
        assertEquals(ARENA * 140 + Sortie.easeDistance(140), sortie.groundScroll(), 1e-6);
        assertEquals(PlayField.HEIGHT - 196, spire.renderY(1), 1e-6);
        for (int p : ROOTS) {
            assertTrue(
                    spire.partY(p) > 0
                            && spire.partY(p) < PlayField.HEIGHT
                            && spire.partX(p) > 0
                            && spire.partX(p) < PlayField.WIDTH,
                    "root part " + p + " on the field from the start (H1 = a)");
            assertFalse(spire.partShielded(p));
        }
        assertTrue(spire.partShielded(MAW), "the maw from phase 2");
        assertTrue(spire.partShielded(CORE), "the core from phase 3");
    }

    @Test
    void theFightAllocatesNothingPerStep() {
        long allocated = Allocations.least(
                () -> {
                    Sortie sortie = sortie(2185, Difficulty.HARD);
                    for (int i = 0; i < SimStep.ticks(ARENA + 2); i++) {
                        sortie.step(Autopilot.commands(sortie));
                    }
                    return sortie;
                },
                sortie -> {
                    for (int i = 0; i < SimStep.ticks(30); i++) {
                        sortie.step(Autopilot.commands(sortie));
                    }
                });
        assertEquals(0, allocated);
    }

    @Test
    void theStatBlocksBountyIsTheBudgetsActBossShare() {
        EnemyData spire = CONTENT.enemy(SLUG);
        List<EnemyData.PartData> parts = spire.partList().orElseThrow();
        assertEquals(
                spire.bounty(),
                parts.stream().mapToInt(EnemyData.PartData::bounty).sum());
        assertEquals(
                spire.hp(), parts.stream().mapToDouble(EnemyData.PartData::hp).sum());
        double paid = spire.bounty()
                * CONTENT.economy().actFactor()
                * CONTENT.level(Level14Test.LEVEL).bounties();
        System.out.printf(
                "Siege Spire paid %.0f = %.1f %% of budget(14)%n",
                paid, 100 * paid / CONTENT.economy().budget().of(14));
        assertEquals(0.30, paid / CONTENT.economy().budget().of(14), 0.30 * BalanceTest.SHARE_TOLERANCE);
    }
}
