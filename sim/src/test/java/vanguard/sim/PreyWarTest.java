package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * M10 part A, step A1a (design/enemies, The three-way war): a wave marked as hunting a tagged prey wave sends its units
 * at that wave's units by their own means: its aimed fire (ordinary enemy bullets that also hit the prey wave's units
 * at their damage and pass every other unit), its chase into a ram (its contact damage to the prey, dying on the
 * impact), its latch (Level 33's rule). A prey unit the hunters kill and a hunter dead on its prey pay nothing and are
 * no kill; one the player kills pays as usual. Once the prey wave is gone the hunters go for the ship. Deterministic,
 * and it allocates nothing per step.
 */
class PreyWarTest {
    /** A hovering prey: 4 HP, its hold 200 px below the top for 8 s, no gun. */
    static final EnemySpec PREY = EnemySpec.of("prey-gunship", 4, new Hitbox(26, 24), Layer.AIR, 10, false, 20, 120)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(8, 8), new Range(260, 260))));

    /** A hunter that holds 100 px below the top and fires an aimed shot (4) every 0.5 s. */
    static final EnemySpec SHOOTER = EnemySpec.of("vrell-shooter", 6, new Hitbox(26, 24), Layer.AIR, 10, true, 12, 120)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(8, 8), new Range(100, 100))))
            .withGun(Optional.of(EnemyGun.aimed(0.5, 0.2, 1, 200, 4, false)));

    /** A hunting rammer: it chases (220 px/s, 180°/s, 4 s) and dies on what it rams. */
    static final EnemySpec RAMMER = EnemySpec.of("vrell-rammer", 3, new Hitbox(20, 20), Layer.AIR, 6, true, 12, 220)
            .withChase(new EnemySpec.Chase(220, Math.toRadians(180), 4), Optional.empty());

    private static WaveSpec prey(double t) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, PREY, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .tag("prey")
                .build();
    }

    private static WaveSpec hunters(double t, EnemySpec unit) {
        return WaveSpec.builder(t, WaveSpec.Formation.SINGLE, unit, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .hunts("prey")
                .build();
    }

    static Sortie sortie(WaveSpec... waves) {
        return Act6ASpecs.sortie(Act6ASpecs.level(30, List.of(), List.of(waves)));
    }

    private static Enemy bySlug(Sortie sortie, String slug) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                return sortie.enemy(i);
            }
        }
        return null;
    }

    @Test
    void aHuntersAimedFireGoesAtItsPreyAndKillsItForNothing() {
        Sortie sortie = sortie(prey(0), hunters(2, SHOOTER));
        int hits = 0;
        int lost = 0;
        for (int i = 0; i < SimStep.ticks(8) && lost == 0; i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.PREY_HIT);
            lost += sortie.events().count(SimEvents.Type.PREY_KILLED);
        }
        assertEquals(1, hits, "one 4-damage bullet");
        assertEquals(1, lost);
        assertNull(bySlug(sortie, "prey-gunship"));
        assertEquals(0, sortie.credits(), "a prey the Vrell kill pays nothing");
        assertEquals(0, sortie.kills(), "and is no kill");
        Enemy hunter = bySlug(sortie, "vrell-shooter");
        // Its prey gone, its next volley goes at the ship, an ordinary bullet.
        int bullets = sortie.bulletCount();
        for (int i = 0; i < SimStep.ticks(1) && sortie.bulletCount() <= bullets; i++) {
            sortie.step(Command.NONE);
        }
        EnemyBullet last = sortie.bullet(sortie.bulletCount() - 1);
        assertNull(last.preyTag());
        double toShip = Math.atan2(Ship.START_Y - hunter.y(), Ship.START_X - hunter.x());
        assertEquals(toShip, Math.atan2(last.vy(), last.vx()), 0.05, "at the ship");
    }

    @Test
    void thePlayersKillOfAPreyPaysAsUsual() {
        Sortie sortie = sortie(prey(0), hunters(2, SHOOTER));
        Act6ASpecs.run(sortie, SimStep.ticks(1.5), Command.NONE);
        Enemy prey = bySlug(sortie, "prey-gunship");
        sortie.destroyEnemy(Act6ASpecs.indexOf(sortie, prey.serial()));
        assertEquals(20, sortie.credits());
        assertEquals(1, sortie.kills());
    }

    @Test
    void aHuntingRammerRamsItsPreyAndBothAreLostToTheWar() {
        Sortie sortie = sortie(prey(0), hunters(2, RAMMER));
        int hits = 0;
        int lost = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.PREY_HIT);
            lost += sortie.events().count(SimEvents.Type.PREY_KILLED);
        }
        assertEquals(1, hits, "its contact damage on the prey");
        assertEquals(2, lost, "the prey (6 on its 4 HP) and the rammer that died on it");
        assertEquals(0, sortie.credits());
        assertEquals(0, sortie.kills());
        assertEquals(0, sortie.enemyCount());
    }

    @Test
    void aHuntingLeechLatchesOnItsPreyAndDragsItDown() {
        Sortie sortie = sortie(prey(0), hunters(2, LampreyTest.LAMPREY));
        boolean latched = false;
        for (int i = 0; i < SimStep.ticks(8) && !latched; i++) {
            sortie.step(Command.NONE);
            latched = sortie.events().count(SimEvents.Type.PREY_LATCHED) > 0;
        }
        assertTrue(latched);
        Enemy prey = bySlug(sortie, "prey-gunship");
        assertTrue(prey.preyed());
        double y = prey.y();
        sortie.step(Command.NONE);
        assertEquals(y - PreyLatches.WAR_DRIFT * SimStep.SECONDS, prey.y(), 1e-9, "down at 60 px/s");
        assertEquals(0, sortie.latched(), "never on the ship while it rides its prey");
    }

    /** M10 part C: a snake of {@code count} Skitters hunting the prey wave (TestSpecs's: 1 HP, contact 6, 190 px/s). */
    private static WaveSpec snake(double t, int count) {
        return WaveSpec.builder(
                        t, WaveSpec.Formation.SNAKE, TestSpecs.SKITTER, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.LEFT)
                .hunts("prey")
                .build();
    }

    @Test
    void aHuntingSnakeLeavesItsPathForItsPreyRamsItAndBothAreLostToTheWar() {
        // M10 part C (design/campaign Level 37, the Skitter snakes ram the Gunship): the prey holds 260 px below the
        // top.
        Sortie sortie = sortie(prey(0), snake(1.5, 3));
        boolean steered = false;
        int hits = 0;
        int lost = 0;
        for (int i = 0; i < SimStep.ticks(8); i++) {
            sortie.step(Command.NONE);
            hits += sortie.events().count(SimEvents.Type.PREY_HIT);
            lost += sortie.events().count(SimEvents.Type.PREY_KILLED);
            Enemy head = bySlug(sortie, "skitter");
            steered |= head != null && head.phase == Enemy.Phase.LEAVE && bySlug(sortie, "prey-gunship") != null;
        }
        assertTrue(steered, "off its path while its prey is on the field");
        assertTrue(hits >= 1, "its contact damage on the prey");
        assertNull(bySlug(sortie, "prey-gunship"), "rammed down (6 on its 4 HP)");
        assertTrue(lost >= 2, "the prey and the Skitter that died on it: " + lost);
        assertEquals(0, sortie.credits(), "the war pays nothing");
        assertEquals(0, sortie.kills());
    }

    @Test
    void aHuntingSnakeTurnsAtItsHuntRate() {
        Sortie sortie = sortie(prey(0), snake(1.5, 1));
        Enemy skitter = null;
        for (int i = 0; i < SimStep.ticks(4) && (skitter == null || skitter.phase != Enemy.Phase.LEAVE); i++) {
            sortie.step(Command.NONE);
            skitter = bySlug(sortie, "skitter");
        }
        assertTrue(skitter != null && skitter.phase == Enemy.Phase.LEAVE);
        double most = Math.toRadians(PreyWar.HUNT_TURN_DEGREES) * SimStep.SECONDS;
        for (int i = 0; i < 20 && bySlug(sortie, "skitter") == skitter; i++) {
            double before = Math.atan2(skitter.vx, skitter.vy);
            sortie.step(Command.NONE);
            double turned = Math.abs(Math.IEEEremainder(Math.atan2(skitter.vx, skitter.vy) - before, 2 * Math.PI));
            assertTrue(turned <= most + 1e-9, "at most 180°/s: " + Math.toDegrees(turned / SimStep.SECONDS));
            assertEquals(TestSpecs.SKITTER.speed(), Math.hypot(skitter.vx, skitter.vy), 1e-4, "at its speed");
        }
    }

    @Test
    void theDartsAPreyGunshipLaunchesAreNotPrey() {
        // M10 part C (design/campaign Level 37: "the Darts a prey Gunship launched are not prey"): launched units carry
        // no wave tag, so the hunters' fire never goes at them nor hits them.
        WaveSpec gunship = WaveSpec.builder(
                        0, WaveSpec.Formation.SINGLE, Act5CSpecs.GUNSHIP, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .tag("prey")
                .build();
        Sortie sortie = sortie(gunship, hunters(1, SHOOTER));
        int darts = 0;
        int gunshipHits = 0;
        int dartKind = -1;
        for (int i = 0; i < SimStep.ticks(10); i++) {
            sortie.step(Command.NONE);
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy unit = sortie.enemy(j);
                if (unit.spec().slug().equals("dart-drone")) {
                    assertEquals("", unit.tag(), "a launched Dart carries no wave tag");
                    dartKind = unit.kind();
                    darts++;
                }
            }
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.PREY_HIT) {
                    assertTrue(dartKind < 0 || events.value(e) != dartKind, "never a Dart");
                    gunshipHits++;
                }
            }
        }
        assertTrue(darts > 0, "the Gunship launched its Darts");
        assertTrue(gunshipHits > 0, "the hunters' fire went at the Gunship");
    }

    @Test
    void aWaveNeverHuntsItsOwnTag() {
        assertThrows(IllegalArgumentException.class, () -> WaveSpec.builder(
                        0, WaveSpec.Formation.SINGLE, PREY, 1, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .tag("prey")
                .hunts("prey")
                .build());
    }

    @Test
    void itReplaysAndAllocatesNothing() {
        Supplier<Sortie> make =
                () -> sortie(prey(0), prey(0.5), hunters(1.5, SHOOTER), hunters(2, RAMMER), prey(4), snake(5, 4));
        Sortie a = make.get();
        Sortie b = make.get();
        for (int i = 0; i < SimStep.ticks(10); i++) {
            a.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            b.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            assertEquals(a.stateHash(), b.stateHash());
        }
        assertEquals(0, Allocations.least(make, sortie -> {
            for (int i = 0; i < SimStep.ticks(10); i++) {
                sortie.step(i % 7 < 3 ? Command.FIRE.bit() : 0);
            }
        }));
    }
}
