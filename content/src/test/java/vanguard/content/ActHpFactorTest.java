package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.sim.EnemySpec;

/**
 * The act HP factor (design/enemies, Balancing basis; user decision D5 = c of M5 part A): from Act 2
 * on, a returning unit of tier {@code medium} or larger flies with its HP times the reference DPS at
 * the level over that at its first level, on every part and segment, rounded once with the
 * difficulty's HP lever; the Needler and the Stinger ({@code small}) the same capped at ×3 in Levels 08–09 (the
 * user's Act 2 playtest, 2026-10-18), 11–14, 15 (step PE15), 16 (step PE16) and 18 (M7 part E); the other {@code tiny} and {@code small} units, Act 1's levels and the
 * bounties stay as they are. M7 part E (step E5): from Level 18 on, the beam era, every {@code small} unit's HP is doubled
 * on top ({@code beam_era_hp}).
 */
class ActHpFactorTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    private static EnemySpec at(String slug, Difficulty difficulty, int act, int level) {
        return SimSpecs.enemy(CONTENT, slug, difficulty, Optional.empty(), act, level);
    }

    @Test
    void aReturningMediumUnitGetsTheReferenceDpsRatioFromLevel08() {
        // The Spore Bomber (medium, first at L03, reference 32): ×60/32 at L08, ×80/32 at L14.
        assertEquals(60.0 / 32, SimSpecs.actHpFactor(CONTENT, "spore-bomber", 2, 8), 1e-12);
        assertEquals(45, at("spore-bomber", Difficulty.MEDIUM, 2, 8).hp());
        assertEquals(60, at("spore-bomber", Difficulty.MEDIUM, 2, 14).hp());
        // The Mantis (medium, L06, reference 52) at L14: 26 × 80 / 52 = 40.
        assertEquals(40, at("mantis", Difficulty.MEDIUM, 2, 14).hp());
        // The Scuttler (L04, reference 38) at L13: 28 × 76 / 38 = 56.
        assertEquals(56, at("scuttler", Difficulty.MEDIUM, 2, 13).hp());
    }

    @Test
    void theFactorIsRoundedOnceWithTheDifficultysHpLever() {
        // 24 × 60/32 = 45; easy 45 × 0.75 = 33.75 → 34, hard 45 × 1.3 = 58.5 → 58 (half to even).
        assertEquals(34, at("spore-bomber", Difficulty.EASY, 2, 8).hp());
        assertEquals(58, at("spore-bomber", Difficulty.HARD, 2, 8).hp());
    }

    @Test
    void tinyAndUnlistedSmallUnitsKeepTheirHp() {
        for (String slug : new String[] {"skitter", "mote-swarm", "spine-turret"}) {
            assertEquals(1, SimSpecs.actHpFactor(CONTENT, slug, 2, 14), slug);
            assertEquals(
                    SimSpecs.enemy(CONTENT, slug, Difficulty.MEDIUM, Optional.empty())
                            .hp(),
                    at(slug, Difficulty.MEDIUM, 2, 14).hp(),
                    slug);
        }
    }

    /**
     * The user's Act 2 playtest (2026-10-18, PT2-B 2c): the returning {@code small} units the basis lists in {@code
     * small_act_hp}, the Needler and the Stinger, get the factor capped at ×3 in its levels (08–09, 11–14), so a Needler
     * takes two bolts of the Pulse Cannon there; from Level 10 on (the user's reference) they keep their HP.
     */
    @Test
    void theListedSmallUnitsGetTheFactorCappedAtThreeInTheListedLevels() {
        assertEquals(
                List.of("needler", "stinger"), CONTENT.enemyBasis().smallActHp().units());
        assertEquals(
                List.of(8, 9, 11, 12, 13, 14, 15, 16, 18),
                CONTENT.enemyBasis().smallActHp().levels());
        // The Needler (4 HP, L01, reference 20): ×60/20 = 3 at L08, ×63/20 capped at 3 at L09.
        assertEquals(3, SimSpecs.actHpFactor(CONTENT, "needler", 2, 8), 1e-12);
        assertEquals(3, SimSpecs.actHpFactor(CONTENT, "needler", 2, 9), 1e-12);
        assertEquals(12, at("needler", Difficulty.MEDIUM, 2, 8).hp());
        // The Stinger (6 HP, L02, reference 26): 6 × 60/26 = 13.8 → 14 at L08, 6 × 63/26 = 14.5 → 15 at L09.
        assertEquals(14, at("stinger", Difficulty.MEDIUM, 2, 8).hp());
        assertEquals(15, at("stinger", Difficulty.MEDIUM, 2, 9).hp());
        // Levels 11 to 14 with their L10-style waves too (steps WA and WB): capped at ×3 again.
        assertEquals(12, at("needler", Difficulty.MEDIUM, 2, 11).hp());
        assertEquals(12, at("needler", Difficulty.MEDIUM, 2, 14).hp());
        // M7 part E: Level 18 with its waves to the Act 2+ floor (reference 88): the Needler ×88/20 capped at 3 = 12,
        // the Stinger 6 × 88/26 = 20.3 capped at ×3 = 18, both doubled by the beam era (step E5) to 24 and 36; Levels
        // 17 keeps theirs until its own rework.
        assertEquals(24, at("needler", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(36, at("stinger", Difficulty.MEDIUM, 3, 18).hp());
        // Step PE15: Level 15 with its waves to the Act 2+ floor (reference 76, pulse era): the Needler ×76/20 capped
        // at 3 = 12, the Stinger 6 × 76/26 = 17.5 → 18.
        assertEquals(12, at("needler", Difficulty.MEDIUM, 3, 15).hp());
        assertEquals(18, at("stinger", Difficulty.MEDIUM, 3, 15).hp());
        // Step PE16: Level 16 the same (pulse era).
        assertEquals(12, at("needler", Difficulty.MEDIUM, 3, 16).hp());
        assertEquals(18, at("stinger", Difficulty.MEDIUM, 3, 16).hp());
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "needler", 3, 17));
        // Not in Level 10 (the user's reference), not in Act 1, and not for the small units the basis does not list.
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "needler", 2, 10));
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "needler", 1, 7));
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "spine-turret", 2, 8));
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "lamprey", 2, 14));
    }

    @Test
    void actOneLevelsAndAUnitsOwnLevelAreUnchanged() {
        EnemySpec base = SimSpecs.enemy(CONTENT, "mantis", Difficulty.MEDIUM, Optional.empty());
        assertEquals(26, base.hp());
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "mantis", 1, 7), "Act 1");
        assertEquals(base, at("mantis", Difficulty.MEDIUM, 1, 7));
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "mantis", 2, 6), "its own first level");
    }

    @Test
    void aChainsHeadSegmentsTailAndRegrownHeadAllScaleButNotTheirBounties() {
        // The Coilwyrm (large, L06): ×80/52 at L14.
        EnemySpec base = SimSpecs.enemy(CONTENT, "coilwyrm", Difficulty.MEDIUM, Optional.empty());
        EnemySpec act2 = at("coilwyrm", Difficulty.MEDIUM, 2, 14);
        EnemySpec.ChainSpec chain = act2.chain().orElseThrow();
        assertEquals(Math.rint(40 * 80.0 / 52), act2.hp(), "head");
        assertEquals(Math.rint(4 * 80.0 / 52), chain.segment().hp(), "segment");
        assertEquals(Math.rint(10 * 80.0 / 52), chain.tail().hp(), "tail");
        assertEquals(Math.rint(20 * 80.0 / 52), chain.regrown().hp(), "regrown head");
        assertEquals(base.bounty(), act2.bounty());
        assertEquals(
                base.chain().orElseThrow().segment().bounty(), chain.segment().bounty());
    }

    @Test
    void aLevelBeyondTheReferenceCurveIsAnError() {
        // M7 part A: the curve runs to Level 21 (Act 3's reference DPS); Level 22 is beyond it.
        assertThrows(IllegalArgumentException.class, () -> SimSpecs.actHpFactor(CONTENT, "mantis", 4, 22));
    }

    /**
     * M7 part A: in Act 3 (Levels 15–21, reference 76 to 100) the factor follows the extended curve, the
     * Mantis (L06) at L16 ×80/52, and the specs built there carry it (step A4).
     */
    @Test
    void anActThreeLevelUsesTheExtendedCurve() {
        assertEquals(80.0 / 52, SimSpecs.actHpFactor(CONTENT, "mantis", 3, 16), 1e-9);
        assertEquals(100.0 / 52, SimSpecs.actHpFactor(CONTENT, "mantis", 3, 21), 1e-9);
        // The Mantis (26 HP): 26 × 76/52 = 38 at L15, 26 × 100/52 = 50 at L21.
        assertEquals(38, at("mantis", Difficulty.MEDIUM, 3, 15).hp());
        assertEquals(50, at("mantis", Difficulty.MEDIUM, 3, 21).hp());
        // The Spore Bomber (24 HP, reference 32 at L03): 24 × 88/32 = 66 at L18.
        assertEquals(66, at("spore-bomber", Difficulty.MEDIUM, 3, 18).hp());
        // The Coilwyrm's head (40 HP, L06) at L21: 40 × 100/52.
        assertEquals(
                Math.rint(40 * 100.0 / 52),
                at("coilwyrm", Difficulty.MEDIUM, 3, 21).hp());
        // Tiny units keep theirs.
        assertEquals(1, SimSpecs.actHpFactor(CONTENT, "skitter", 3, 21));
        assertEquals(
                SimSpecs.enemy(CONTENT, "skitter", Difficulty.MEDIUM, Optional.empty())
                        .hp(),
                at("skitter", Difficulty.MEDIUM, 3, 21).hp());
    }

    /**
     * M7 part E, step E5 (the user's "tougher, not more" of 2026-10-19): from Level 18 on, where the balance plan fits
     * the Ion Beam, a {@code small} unit takes the basis's {@code beam_era_hp} factor (×2) on top of its act HP factor,
     * its own first level included; {@code tiny} and {@code medium} units (the Dust Devil, whose vanes already gate its
     * time to kill, and the Wraith, set for the rear gun) and Levels 15–17 keep theirs; the bounty is unchanged.
     */
    @Test
    void fromLevelEighteenTheBeamEraDoublesTheSmallUnits() {
        EnemyBasisData.BeamEraHp era = CONTENT.enemyBasis().beamEraHp();
        assertEquals(18, era.fromLevel());
        assertEquals(2, era.factor());
        assertEquals(List.of(Tier.SMALL), era.tiers());
        // The Ghost Drone (small, new at L18, 14 HP): ×2 at its own first level.
        assertEquals(2, SimSpecs.actHpFactor(CONTENT, "ghost-drone", 3, 18), 1e-12);
        assertEquals(28, at("ghost-drone", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(
                Math.rint(28 * 1.3), at("ghost-drone", Difficulty.HARD, 3, 18).hp());
        // A small unit outside small_act_hp (the Spine Turret, 8 HP): 16 at L18 and after, 8 at L17.
        assertEquals(16, at("spine-turret", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(16, at("spine-turret", Difficulty.MEDIUM, 3, 21).hp());
        assertEquals(8, at("spine-turret", Difficulty.MEDIUM, 3, 17).hp());
        // Tiny and medium units keep theirs: the Dust Kite 1, the Dust Devil 52, the Wraith 16 × 88/66.
        assertEquals(1, at("dust-kite", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(52, at("dust-devil", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(
                Math.rint(16 * 88.0 / 66),
                at("wraith", Difficulty.MEDIUM, 3, 18).hp());
        assertEquals(
                SimSpecs.enemy(CONTENT, "ghost-drone", Difficulty.MEDIUM, Optional.empty())
                        .bounty(),
                at("ghost-drone", Difficulty.MEDIUM, 3, 18).bounty());
    }
}
