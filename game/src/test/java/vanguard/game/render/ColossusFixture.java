package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M7 part H (step HB): Level 21's crawling arena in small for the game's tests (design/enemies/bosses/dust-colossus, as
 * content's {@code ColossusRules} maps it at medium, as content's {@code DustColossusTest} flies it): 3 s of approach at
 * 140 px/s, the arena crawling at 20 px/s where the Colossus arrives (the clock held at its end while it lives), then
 * the settling dust; flown with the starter loadout and the ship invulnerable, under Level 21's rules.
 */
public final class ColossusFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 21's key: its rules. */
    public static final String LEVEL = "act-3-red-dust/level-21-dust-colossus";
    /** The arena's start (the boss's arrival), s of level time, and its end. */
    public static final double ARENA = 3;

    public static final double ARENA_END = ARENA + 140;
    /** The mouth, the first (vital) part. */
    public static final int MOUTH = 0;

    private ColossusFixture() {}

    public static LevelScript level() {
        return LevelScript.builder(21, 3)
                .sections(List.of(
                        new Section(ARENA, 140),
                        new Section(ARENA_END, 20, true, 0, 2, 0, true),
                        new Section(ARENA_END + 20, 25, false, 1)))
                .setPieces(List.of(SimSpecs.boss(
                        CONTENT, new LevelData.BossPlacement("dust-colossus", ARENA, 240, 2), Difficulty.MEDIUM)))
                .build();
    }

    /** The arena flown with the starter loadout and the ship invulnerable (it holds still without input). */
    public static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                level(),
                SimSpecs.rules(CONTENT, LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** The Colossus of a sortie of {@link #level()}. */
    public static SetPiece colossus(Sortie sortie) {
        return sortie.setPiece(0);
    }
}
