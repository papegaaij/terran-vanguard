package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.BreachArena;
import vanguard.sim.BreachDraw;
import vanguard.sim.EchoSpec;
import vanguard.sim.PartSpec;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPieceSpec;

/**
 * M11 part G, step G2 (design/enemies/bosses, Boss rush; m11g-gaps.md §1 #2–#7): the three echoes read from their
 * originals' {@code boss.echo} overlays ({@link EchoOverlay}): no weak-point multiplier, each bounty half its original's
 * (225 / 240 / 217 in Act 1 terms), its forming and dissolve; the originals read as before. Step G5 (round 93 L.3, the
 * user's boss recipe): each in two stages, the subtargets (each a gun of its own) and then the heart, firing while it
 * forms; its HP step G5's measured ({@link EchoFightTest}: the Carrier 3,960, the Colossus 1,600, the Sovereign 4,400).
 */
class EchoKeysTest {
    private static final Content CONTENT = LevelMetrics.CONTENT;

    static SetPieceSpec echo(String slug, double x, Difficulty difficulty) {
        return BossRules.boss(
                CONTENT,
                new LevelData.BossPlacement(slug, 100, x, 2, Optional.empty(), Optional.of(true)),
                difficulty,
                new UnitRules.InLevel(7, 49));
    }

    private static double sum(List<PartSpec> parts, java.util.function.Predicate<PartSpec> which) {
        return parts.stream().filter(which).mapToDouble(PartSpec::hp).sum();
    }

    private static int bounty(SetPieceSpec spec) {
        return spec.parts().stream().mapToInt(PartSpec::bounty).sum();
    }

    /** The boss attack named {@code name}'s index. */
    private static int attack(BossSpec boss, String name) {
        for (int a = 0; a < boss.attacks().size(); a++) {
            if (boss.attacks().get(a).name().equals(name)) {
                return a;
            }
        }
        throw new AssertionError("no attack " + name);
    }

    /** Step G5: every subtarget of {@code parts} fires a gun of its own in the first stage, listed by the stage. */
    private static void ownGuns(SetPieceSpec spec, List<String> parts) {
        BossSpec boss = spec.boss().orElseThrow();
        BossSpec.Phase stage = boss.phases().getFirst();
        for (String name : parts) {
            int p = spec.parts().stream().map(PartSpec::name).toList().indexOf(name);
            assertTrue(p >= 0, name);
            assertTrue(
                    stage.attacks().stream()
                            .anyMatch(a -> boss.attacks().get(a).parts().contains(p)),
                    spec.slug() + ": " + name + " fires a gun of its own in stage 1");
        }
    }

    @Test
    void theBroodCarrierEchoIsTheSacsThenTheCore() {
        SetPieceSpec carrier = echo("brood-carrier", 170, Difficulty.MEDIUM);
        BossSpec boss = carrier.boss().orElseThrow();
        assertEquals("brood-carrier", carrier.slug(), "the original's sprites and looks");
        assertEquals(3_960, sum(carrier.parts(), part -> !part.name().equals("mandibles")), 1e-6);
        assertTrue(carrier.parts().stream().allMatch(part -> part.multiplier() == 1), "no weak-point multiplier");
        assertEquals(225, bounty(carrier));
        assertEquals(2, boss.phases().size(), "the sacs, then the core");
        List<String> sacs = carrier.parts().stream()
                .map(PartSpec::name)
                .filter(name -> name.startsWith("bay"))
                .toList();
        assertEquals(8, boss.phases().getFirst().untilParts().size(), "until every sac is dead");
        assertEquals(0, boss.phases().getFirst().left());
        assertTrue(boss.phases().getFirst().windows().isEmpty(), "every sac open (step G5)");
        ownGuns(carrier, sacs);
        assertEquals(
                List.of(attack(boss, "core-spiral"), attack(boss, "core-ring"), attack(boss, "core-burst")),
                boss.phases().get(1).attacks(),
                "the core's spiral the whole stage, its rings and its gun");
        assertEquals(2, boss.playbooks().size());
        assertEquals(3, boss.playbooks().getFirst().moves().size(), "fans, sweep, brood");
        assertEquals(2, boss.playbooks().get(1).moves().size(), "fans, sweep");
        assertTrue(boss.playbooks().getFirst().moves().get(1).sweepRadians() > 0, "the mandible sweep");
        BossSpec.Rise rise = boss.rise().orElseThrow();
        assertEquals(3, rise.seconds());
        assertEquals(boss.x(), rise.fromX(), 1e-9);
        assertEquals(boss.hoverY(), rise.fromY(), 1e-9);
        assertEquals(attack(boss, "form-volley"), rise.fireAttack(), "it fires while it forms");
        assertEquals(2, boss.deathSeconds());
        assertEquals(1.0, boss.echo().orElseThrow().dissolveSeconds());
        assertTrue(boss.poses().isEmpty(), "already broadside");
    }

    @Test
    void theDustColossusEchoIsTheVentsThenTheMouthHunting() {
        SetPieceSpec colossus = echo("dust-colossus", 240, Difficulty.MEDIUM);
        BossSpec boss = colossus.boss().orElseThrow();
        assertEquals(150, colossus.parts().getFirst().hp(), 1e-6);
        assertEquals(1, colossus.parts().getFirst().multiplier(), "no lips ×0.5");
        assertTrue(boss.arena().orElseThrow().spots().isEmpty(), "no gullet ×2");
        assertEquals(3 * 80, sum(colossus.parts(), part -> part.name().startsWith("vent")), 1e-6);
        assertEquals(240, bounty(colossus));
        assertEquals(2, boss.phases().size(), "the spine, then hunting");
        assertEquals(3, boss.phases().getFirst().untilParts().size(), "until the three vents are dead");
        ownGuns(colossus, List.of("vent 1", "vent 2", "vent 3"));
        assertTrue(boss.phases().get(1).attacks().contains(attack(boss, "spiral")), "the spiral in stage 2");
        BossSpec.Breach breach = boss.breach().orElseThrow();
        BreachDraw.Spec draw = breach.draw().orElseThrow();
        assertEquals(1, draw.eruptionFrom(), "the eruption in stage 2 only");
        assertTrue(draw.frontFirst(), "its first breach at the front");
        // Step G5b: a steadier fight: the front every second breach, each stage its own points (hunting from ahead and
        // behind), the mouth sealing once hurt in a breach, the vents in turn.
        assertEquals(2, draw.frontEvery(), "the front at least every second breach");
        assertEquals(
                List.of(BreachArena.Pose.FRONT, BreachArena.Pose.REAR),
                breach.order(1).poses(),
                "stage 2 hunts from ahead and behind");
        assertEquals(100, breach.flinchHp(), 1e-9, "the mouth seals after 100 HP in a breach");
        assertEquals(List.of(1, 1, 3), boss.vents().orElseThrow().firstBreaches(), "the vents in turn");
        BossSpec.Burrow burrow = breach.burrow().orElseThrow();
        assertEquals(attack(boss, "burrow-volley"), burrow.attack(), "it fires from its first tell");
        assertEquals(1, breach.introSeconds(), "its first tell 1 s after the bar");
        assertTrue(boss.vents().orElseThrow().stayUp());
        assertEquals(2, boss.deathSeconds());
        assertTrue(boss.echo().isPresent());
    }

    @Test
    void theIronSovereignEchoIsTheRingsTurretsThenTheOpenCore() {
        SetPieceSpec sovereign = echo("iron-sovereign", 240, Difficulty.MEDIUM);
        BossSpec boss = sovereign.boss().orElseThrow();
        RingSpec rings = boss.rings().orElseThrow();
        assertEquals(1, rings.rings().size(), "the outer ring alone");
        assertEquals(4_400, sum(sovereign.parts(), part -> true), 1e-6);
        assertEquals(217, bounty(sovereign));
        ownGuns(sovereign, List.of("sam-1", "gun-1", "rail-1", "sam-2", "gun-2", "rail-2"));
        assertEquals(2, boss.phases().size(), "the ring, then the core");
        assertEquals(1, rings.locks(0), "the ring's turrets until it locks");
        RingSpec.Core core = rings.core().orElseThrow();
        assertEquals(1, core.openingSeconds(), "the petals open in 1 s");
        assertEquals(110, core.shield().orElseThrow().hp(), 1e-6);
        RingSpec.Pool pool = core.pool().orElseThrow();
        assertEquals(1.0, pool.restSeconds());
        assertEquals(0.7, pool.lowRestSeconds());
        assertEquals(2, pool.wrecks().orElseThrow().minDead());
        assertTrue(pool.cover() >= 0, "an aimed pair as each play ends");
        assertTrue(boss.phases().get(1).attacks().contains(attack(boss, "laser")), "the inner ring's sweep, new");
        EchoSpec echo = boss.echo().orElseThrow();
        assertEquals(3, echo.formSeconds());
        assertEquals(attack(boss, "form-volley"), echo.fireAttack(), "it fires while it forms");
    }

    @Test
    void theOriginalsReadAsBefore() {
        for (String slug : List.of("brood-carrier", "dust-colossus", "iron-sovereign")) {
            SetPieceSpec original =
                    SimSpecs.boss(CONTENT, new LevelData.BossPlacement(slug, 100, 240, 2), Difficulty.MEDIUM);
            assertFalse(original.boss().orElseThrow().echo().isPresent(), slug);
            assertTrue(CONTENT.enemy(slug).boss().orElseThrow().echo().isPresent(), slug + " has its echo");
            assertTrue(CONTENT.enemy(slug).boss().orElseThrow().dissolve().isEmpty(), slug);
        }
        assertEquals(3_840, CONTENT.enemy("brood-carrier").hp());
        assertEquals(
                3,
                CONTENT.enemy("iron-sovereign")
                        .boss()
                        .orElseThrow()
                        .rings()
                        .orElseThrow()
                        .size());
    }
}
