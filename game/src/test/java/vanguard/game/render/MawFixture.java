package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.CoilArena;
import vanguard.sim.Command;
import vanguard.sim.LevelScript;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M8 part H (step H5): Level 28's halted arena in small for the game's tests (design/enemies/bosses/abyssal-maw at
 * medium, as content's {@code BossRules} maps it): 3 s of approach at 120 px/s, the arena (speed 0, the clock held while
 * the Maw lives) where it arrives, 20 s at 120 px/s after it; flown with the starter loadout and the ship invulnerable,
 * under Level 28's rules (the play plane in the air: no water factors).
 */
public final class MawFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 28's key: its rules. */
    public static final String LEVEL = CONTENT.levelKey(28).orElseThrow();
    /** The arena's start (the Maw's arrival), s of level time. */
    public static final double ARENA = 3;

    private MawFixture() {}

    public static LevelScript level() {
        return LevelScript.builder(28, 4)
                .sections(
                        List.of(new Section(ARENA, 120), new Section(ARENA + 1, 0, true), new Section(ARENA + 21, 120)))
                .setPieces(List.of(SimSpecs.boss(
                        CONTENT, new LevelData.BossPlacement("abyssal-maw", ARENA, 240, 2), Difficulty.MEDIUM)))
                .build();
    }

    /** The arena flown with the starter loadout (its straight guns pick the sacs) and the ship invulnerable. */
    public static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                level(),
                SimSpecs.rules(CONTENT, LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /**
     * Level 28's finale on its own clock with the approach and the Maw left out (its arena a plain 1 s section at 100
     * px/s; no waves, ground units, set pieces, radio or armour floor), flown with the starter loadout, the ship
     * invulnerable: the shaft, the breakout, the cruisers' rise and run (step I2's breach and rise tests; the finale's
     * sounds).
     */
    public static Sortie finale() {
        LevelScript real = SimSpecs.level(CONTENT, LEVEL, Difficulty.MEDIUM);
        List<Section> sections = real.sections().stream()
                .map(section -> section.arena()
                        ? new Section(section.end(), 100)
                        : new Section(
                                section.end(), section.speed(), false, section.rampSeconds(), section.easeSeconds()))
                .toList();
        LevelScript finale = real.toBuilder()
                .sections(sections)
                .waves(List.of())
                .groundUnits(List.of())
                .setPieces(List.of())
                .radio(List.of())
                .armourFloor(0)
                .build();
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                finale,
                SimSpecs.rules(CONTENT, LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** The Maw of a sortie of {@link #level()}. */
    public static SetPiece maw(Sortie sortie) {
        return sortie.setPiece(0);
    }

    public static CoilArena coil(Sortie sortie) {
        return maw(sortie).coil().orElseThrow();
    }

    /** Firing, under the first living sac (else the head), low on the field. */
    public static int attack(Sortie sortie) {
        SetPiece maw = maw(sortie);
        double x = maw.renderX(1);
        for (int p = 1; p <= 4 && p < maw.partCount(); p++) {
            if (!maw.partWrecked(p)) {
                x = maw.renderX(1) + maw.partOffsetX(p);
                break;
            }
        }
        return IceShelfFixture.toward(sortie, x, 120) | Command.FIRE.bit();
    }
}
