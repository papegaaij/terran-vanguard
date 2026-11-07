package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.BossSpec;
import vanguard.sim.PartSpec;
import vanguard.sim.RingSpec;
import vanguard.sim.SetPieceSpec;

/**
 * M11 part G, step G2 (design/enemies/bosses, Boss rush; m11g-gaps.md §1 #2–#7): the three echoes read from their
 * originals' {@code boss.echo} overlays ({@link EchoOverlay}): each bar step G2's measured one ({@link EchoFightTest}: the
 * Brood Carrier 5,500, the Dust Colossus 2,300 with its vents outside, the Iron Sovereign 7,400; the bosses' paper
 * 10,320 / 6,300 / 8,500), no weak-point multiplier, each bounty
 * half its original's (225 / 240 / 217 in Act 1 terms), its forming and dissolve; the originals read as before.
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

    @Test
    void theBroodCarrierEchoIsOneBarOfSacsAndCore() {
        SetPieceSpec carrier = echo("brood-carrier", 170, Difficulty.MEDIUM);
        BossSpec boss = carrier.boss().orElseThrow();
        assertEquals("brood-carrier", carrier.slug(), "the original's sprites and looks");
        assertEquals(5_500, sum(carrier.parts(), part -> !part.name().equals("mandibles")), 1e-6);
        assertTrue(carrier.parts().stream().allMatch(part -> part.multiplier() == 1), "no weak-point multiplier");
        assertEquals(225, bounty(carrier));
        assertEquals(2, boss.phases().size(), "the sacs, then the core: one bar");
        assertEquals(4, boss.phases().getFirst().left(), "until four sacs are dead");
        assertTrue(boss.phases().getFirst().windows().orElseThrow().onMove(), "a window at each move's end");
        assertEquals(2, boss.playbooks().size());
        assertEquals(5, boss.playbooks().get(1).moves().size(), "fans, sweep, brood, spiral, rings");
        assertTrue(boss.playbooks().getFirst().moves().get(1).sweepRadians() > 0, "the mandible sweep");
        assertEquals(
                1,
                boss.playbooks()
                        .getFirst()
                        .moves()
                        .get(2)
                        .bays()
                        .orElseThrow()
                        .turns()
                        .size(),
                "Needlers next");
        BossSpec.Rise rise = boss.rise().orElseThrow();
        assertEquals(3, rise.seconds());
        assertEquals(boss.x(), rise.fromX(), 1e-9);
        assertEquals(boss.hoverY(), rise.fromY(), 1e-9);
        assertEquals(2, boss.deathSeconds());
        assertEquals(1.0, boss.echo().orElseThrow().dissolveSeconds());
        assertTrue(boss.poses().isEmpty(), "already broadside");
    }

    @Test
    void theDustColossusEchoHuntsByTheDraw() {
        SetPieceSpec colossus = echo("dust-colossus", 240, Difficulty.MEDIUM);
        BossSpec boss = colossus.boss().orElseThrow();
        assertEquals(2_300, colossus.parts().getFirst().hp(), 1e-6);
        assertEquals(1, colossus.parts().getFirst().multiplier(), "no lips ×0.5");
        assertTrue(boss.arena().orElseThrow().spots().isEmpty(), "no gullet ×2");
        assertEquals(3 * 550, sum(colossus.parts(), part -> part.name().startsWith("vent")), 1e-6);
        assertEquals(240, bounty(colossus));
        assertEquals(1, boss.phases().size(), "hunting only");
        assertTrue(boss.breach().orElseThrow().draw().isPresent(), "the seeded draw");
        assertEquals(2, boss.breach().orElseThrow().introSeconds(), "its first tell 2 s after the bar");
        assertTrue(boss.vents().orElseThrow().stayUp());
        assertEquals(2, boss.deathSeconds());
        assertTrue(boss.echo().isPresent());
    }

    @Test
    void theIronSovereignEchoIsTheOpenCoreUnderTheOuterRing() {
        SetPieceSpec sovereign = echo("iron-sovereign", 240, Difficulty.MEDIUM);
        BossSpec boss = sovereign.boss().orElseThrow();
        RingSpec rings = boss.rings().orElseThrow();
        assertEquals(1, rings.rings().size(), "the outer ring alone");
        assertEquals(7_400, sum(sovereign.parts(), part -> true), 1e-6);
        assertEquals(217, bounty(sovereign));
        RingSpec.Core core = rings.core().orElseThrow();
        assertEquals(0, core.openingSeconds(), "already open");
        assertEquals(110, core.shield().orElseThrow().hp(), 1e-6);
        RingSpec.Pool pool = core.pool().orElseThrow();
        assertEquals(1.0, pool.restSeconds());
        assertEquals(0.7, pool.lowRestSeconds());
        assertEquals(2, pool.wrecks().orElseThrow().minDead());
        assertTrue(pool.cover() >= 0, "an aimed pair as each play ends");
        assertEquals(1, boss.phases().size());
        assertEquals(3, boss.echo().orElseThrow().formSeconds());
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
