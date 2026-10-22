package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * M8 part C, the Glassfin (design/enemies/naval/glassfin): a school of fry on the Mote Swarm's flock, swaying as a
 * ribbon across its route (14 px at 1.4 Hz); 3 s after a fry first came onto the screen it draws, by its own seed,
 * whether it breaks off (easy 15 %, medium 25 %, hard 40 %): a 0.3 s glint (its tell), 0.5 s later a straight dive at
 * where the ship was at 230 px/s without homing, gone at the bottom (an escape). A path wave's fry ram the same way.
 * Deterministic and allocation-free.
 */
class GlassfinTest {
    static final EnemySpec.Sine RIBBON = new EnemySpec.Sine(14, 1 / 1.4);

    static EnemySpec.Ram ram(double share) {
        return new EnemySpec.Ram(3, share, 0.3, 0.5, 230);
    }

    /** The Glassfin's flock: separation 14, cruise 170, at most 24, with {@code sway} and {@code ram}. */
    static EnemySpec.FlockSpec flock(Optional<EnemySpec.Sine> sway, Optional<EnemySpec.Ram> ram) {
        return new EnemySpec.FlockSpec(14, 48, 0.5, 0.3, 0.6, 170, 230, Math.toRadians(360), 24).withRibbon(sway, ram);
    }

    /** One fry: 1 HP, 10×10, `tiny` contact 6 and destroyed by it, 2 credits, 170 px/s. */
    static EnemySpec glassfin(EnemySpec.FlockSpec flock) {
        return new EnemySpec(
                        "glassfin",
                        1,
                        new Hitbox(10, 10),
                        Layer.AIR,
                        6,
                        true,
                        2,
                        170,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        false,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(flock))
                .withSeekersIgnore(true);
    }

    /** A long S down the screen (x, px below the top edge), clear of the ship's spot (240, 444) by 60 px or more. */
    static final List<WaveSpec.At> ROUTE = List.of(
            new WaveSpec.At(60, -40),
            new WaveSpec.At(420, 100),
            new WaveSpec.At(60, 240),
            new WaveSpec.At(420, 360),
            new WaveSpec.At(80, 470),
            new WaveSpec.At(60, 620));

    static WaveSpec school(double t, EnemySpec fry, int count) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.SWARM,
                fry,
                count,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of(ROUTE));
    }

    /** A column of {@code count} fry straight down at x 144 (left), slowed to 60 px/s to stay on the screen a while. */
    static WaveSpec column(double t, EnemySpec fry, int count) {
        return new WaveSpec(
                t,
                WaveSpec.Formation.COLUMN,
                fry,
                count,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.LEFT,
                Optional.empty(),
                Optional.empty(),
                1,
                Optional.of(60.0),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                List.of());
    }

    private static Sortie sortie(List<WaveSpec> waves) {
        return PartDSpecs.sortie(PartDSpecs.level(40, waves));
    }

    /** Every glassfin on the field by serial. */
    private static Map<Integer, Enemy> fry(Sortie sortie) {
        Map<Integer, Enemy> found = new HashMap<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().slug().equals("glassfin")) {
                found.put(enemy.serial(), enemy);
            }
        }
        return found;
    }

    @Test
    void theSchoolSwaysAsARibbonAcrossItsRoute() {
        EnemySpec swaying = glassfin(flock(Optional.of(RIBBON), Optional.empty()));
        EnemySpec plain = glassfin(flock(Optional.empty(), Optional.empty()));
        Sortie ribbon = sortie(List.of(school(1, swaying, 16)));
        Sortie still = sortie(List.of(school(1, plain, 16)));
        double most = 0;
        boolean outOfStep = false;
        for (int step = 0; step < SimStep.ticks(6); step++) {
            ribbon.step(Command.NONE);
            still.step(Command.NONE);
            if (ribbon.flockCount() == 0) {
                continue;
            }
            Flock a = ribbon.flock(0);
            Flock b = still.flock(0);
            assertEquals(a.leaderX(), b.leaderX(), 1e-9, "the same leader point");
            double previous = Double.NaN;
            for (int i = 0; i < a.size(); i++) {
                Enemy swung = a.member(i);
                Enemy base = b.member(i);
                // The steering is the same: the sway only shows the members off their unswayed places.
                double off = Math.hypot(swung.x() - base.x(), swung.y() - base.y());
                assertTrue(off <= 14 + 1e-6, "within 14 px of its place: " + off);
                most = Math.max(most, off);
                outOfStep |= Math.abs(off - previous) > 1;
                previous = off;
            }
        }
        assertEquals(14, most, 0.2, "a 14 px swing");
        assertTrue(outOfStep, "the members swing out of step: a ribbon, not a block");
    }

    @Test
    void aBreakingFryGlints3sAfterItCameOnThenDivesStraightAtTheShipAndLeavesAtTheBottom() {
        Sortie sortie = sortie(List.of(school(1, glassfin(flock(Optional.of(RIBBON), Optional.of(ram(0.4)))), 22)));
        Map<Integer, Integer> on = new HashMap<>();
        Map<Integer, Integer> glint = new HashMap<>();
        Map<Integer, Integer> dive = new HashMap<>();
        Map<Integer, double[]> before = new HashMap<>();
        Map<Integer, double[]> start = new HashMap<>();
        Map<Integer, double[]> ship = new HashMap<>();
        int glints = 0;
        int straight = 0;
        for (int step = 1; step <= SimStep.ticks(14); step++) {
            Map<Integer, Enemy> previous = fry(sortie);
            for (Map.Entry<Integer, Enemy> entry : previous.entrySet()) {
                before.put(
                        entry.getKey(),
                        new double[] {entry.getValue().x(), entry.getValue().y()});
            }
            sortie.step(Command.NONE);
            glints += sortie.events().count(SimEvents.Type.EYE_GLINT);
            for (Map.Entry<Integer, Enemy> entry : fry(sortie).entrySet()) {
                int serial = entry.getKey();
                Enemy fry = entry.getValue();
                if (!on.containsKey(serial) && PlayField.overlaps(fry.x(), fry.y(), fry.hitbox())) {
                    on.put(serial, step);
                }
                if (fry.ramGlinting() && !glint.containsKey(serial)) {
                    glint.put(serial, step);
                }
                if (fry.ramDiving()) {
                    double[] was = before.get(serial);
                    if (!dive.containsKey(serial)) {
                        dive.put(serial, step);
                        start.put(serial, was);
                        ship.put(
                                serial,
                                new double[] {sortie.ship().x(), sortie.ship().y()});
                    }
                    double[] from = start.get(serial);
                    // At where the ship was, or straight down when it was not below the fry.
                    double[] at =
                            ship.get(serial)[1] < from[1] ? ship.get(serial) : new double[] {from[0], from[1] - 100};
                    assertEquals(230 * SimStep.SECONDS, Math.hypot(fry.x() - was[0], fry.y() - was[1]), 1e-6, "230");
                    double cross = (fry.x() - from[0]) * (at[1] - from[1]) - (fry.y() - from[1]) * (at[0] - from[0]);
                    double reach = Math.hypot(at[0] - from[0], at[1] - from[1]);
                    assertEquals(0, cross / reach, 1e-6, "straight at where the ship was: no homing");
                    straight++;
                }
            }
        }
        assertFalse(glint.isEmpty(), "some fry broke off");
        assertEquals(glint.size(), glints, "one glint each");
        assertEquals(glint.keySet(), dive.keySet(), "each that glinted dove");
        for (int serial : glint.keySet()) {
            assertEquals(SimStep.ticks(3), glint.get(serial) - on.get(serial), "3 s after it came on");
            assertEquals(SimStep.ticks(0.3) + SimStep.ticks(0.5), dive.get(serial) - glint.get(serial), "0.8 s later");
        }
        assertTrue(straight > dive.size() * 20, "watched the dives a while: " + straight);
        assertEquals(0, sortie.kills(), "none was shot or rammed the untouchable ship");
        for (Enemy fry : fry(sortie).values()) {
            assertFalse(fry.ramDiving(), "every diver left at the bottom: " + fry.y());
        }
    }

    /** The share of {@code count} fry in schools of 24 that break off at {@code share}, and the serials that did. */
    private static Set<Integer> rammers(double share, int schools) {
        EnemySpec fry = glassfin(flock(Optional.of(RIBBON), Optional.of(ram(share))));
        List<WaveSpec> waves = new ArrayList<>();
        for (int k = 0; k < schools; k++) {
            waves.add(school(1 + 2.5 * k, fry, 24));
        }
        Sortie sortie = sortie(waves);
        Set<Integer> rammed = new HashSet<>();
        Set<Integer> seen = new HashSet<>();
        for (int step = 0; step < SimStep.ticks(4 + 2.5 * schools + 6); step++) {
            sortie.step(Command.NONE);
            for (Enemy enemy : fry(sortie).values()) {
                seen.add(enemy.serial());
                if (enemy.ramGlinting() || enemy.ramDiving()) {
                    rammed.add(enemy.serial());
                }
            }
        }
        assertEquals(24 * schools, seen.size(), "every fry flew");
        return rammed;
    }

    @Test
    void theSharesThatRamFollowTheDifficulty() {
        int schools = 6;
        Set<Integer> easy = rammers(0.15, schools);
        Set<Integer> medium = rammers(0.25, schools);
        Set<Integer> hard = rammers(0.40, schools);
        double total = 24.0 * schools;
        assertEquals(0.15, easy.size() / total, 0.08, "easy: " + easy.size());
        assertEquals(0.25, medium.size() / total, 0.08, "medium: " + medium.size());
        assertEquals(0.40, hard.size() / total, 0.08, "hard: " + hard.size());
        assertTrue(
                hard.containsAll(medium) && medium.containsAll(easy), "each fry's own draw: a larger share adds fry");
        assertEquals(0, rammers(0, 1).size(), "a share of 0: none");
    }

    @Test
    void aColumnsFryRamTheSameWayAndARamThatMeetsTheShipHitsAndDies() {
        Sortie sortie = sortie(List.of(column(1, glassfin(flock(Optional.empty(), Optional.of(ram(1)))), 6)));
        Set<Integer> dove = new HashSet<>();
        for (int step = 0; step < SimStep.ticks(16); step++) {
            sortie.step(Command.NONE);
            for (Enemy fry : fry(sortie).values()) {
                if (fry.ramDiving()) {
                    dove.add(fry.serial());
                }
            }
        }
        assertEquals(6, dove.size(), "a share of 1: every fry of the column dove");
        assertEquals(0, sortie.enemyCount(), "and left");

        Sortie hit = new Sortie(
                1,
                TestSpecs.LOADOUT,
                PartDSpecs.level(40, List.of(column(1, glassfin(flock(Optional.empty(), Optional.of(ram(1)))), 1))),
                TestSpecs.RULES,
                TestSpecs.FULL_ARMOUR);
        Defences defences = hit.ship().defences();
        double full = defences.shield() + defences.armour();
        int destroyed = 0;
        for (int step = 0; step < SimStep.ticks(10); step++) {
            hit.step(Command.NONE);
            destroyed += hit.events().count(SimEvents.Type.ENEMY_DESTROYED);
        }
        assertEquals(1, destroyed, "the fry died on the ship");
        assertTrue(full - (defences.shield() + defences.armour()) > 0, "its `tiny` contact hurt");
    }

    @Test
    void theSameSeedFliesTheSameSchool() {
        EnemySpec fry = glassfin(flock(Optional.of(RIBBON), Optional.of(ram(0.4))));
        Sortie a = sortie(List.of(school(1, fry, 22), column(2, fry, 8)));
        Sortie b = sortie(List.of(school(1, fry, 22), column(2, fry, 8)));
        for (int step = 0; step < SimStep.ticks(12); step++) {
            int command = Command.FIRE.bit() | (step % 120 < 60 ? Command.LEFT.bit() : Command.RIGHT.bit());
            a.step(command);
            b.step(command);
            assertEquals(a.stateHash(), b.stateHash(), "at step " + step);
        }
    }

    @Test
    void schoolsAndTheirRamsDoNotAllocate() {
        EnemySpec fry = glassfin(flock(Optional.of(RIBBON), Optional.of(ram(0.4))));
        LevelScript level = PartDSpecs.level(
                40, List.of(school(1, fry, 24), school(2, fry, 22), column(3, fry, 10), school(6, fry, 16)));
        java.util.function.Consumer<Sortie> run = sortie -> {
            for (int step = 0; step < SimStep.ticks(14); step++) {
                sortie.step(
                        step % 120 < 60
                                ? Command.FIRE.bit() | Command.LEFT.bit()
                                : Command.FIRE.bit() | Command.RIGHT.bit());
            }
        };
        // The run covers what it measures: tells, dives (the dive helper) and kills.
        Sortie seen = new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
        int glints = 0;
        int dives = 0;
        for (int step = 0; step < SimStep.ticks(14); step++) {
            seen.step(
                    step % 120 < 60
                            ? Command.FIRE.bit() | Command.LEFT.bit()
                            : Command.FIRE.bit() | Command.RIGHT.bit());
            glints += seen.events().count(SimEvents.Type.EYE_GLINT);
            for (Enemy one : fry(seen).values()) {
                dives += one.ramDiving() ? 1 : 0;
            }
        }
        assertTrue(glints > 0 && dives > 0 && seen.kills() > 0, glints + " tells, " + dives + " dive steps");

        long allocated = Allocations.least(
                () -> new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR), run);
        assertEquals(0, allocated);
    }
}
