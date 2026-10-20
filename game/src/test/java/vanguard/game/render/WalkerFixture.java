package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * M7 part F: the Revenant Walker's arena for the game's tests (design/enemies/bosses/revenant-walker, as content's
 * {@code FoundryRules} maps it at medium): 3 s of ground at 40 px/s, the arena where it arrives (the clock halted
 * while it lives), then the run-out; flown with the starter loadout and the ship invulnerable, under Level 19's rules.
 */
public final class WalkerFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** Level 19's key: its rules. */
    static final String LEVEL = "act-3-red-dust/level-19-foundry-of-tharsis";

    public static final double ARENA = 3;
    public static final int HULL = 0;
    public static final int LEFT_POD = 1;
    public static final int RIGHT_POD = 2;

    private WalkerFixture() {}

    public static LevelScript level() {
        return LevelScript.builder(19, 3)
                .sections(List.of(new Section(ARENA, 40), new Section(ARENA + 1, 0, true), new Section(ARENA + 16, 40)))
                .objectives(ObjectiveSpec.NONE.withSecondary(Secondary.afloat(100, "NONE")))
                .setPieces(List.of(SimSpecs.boss(
                        CONTENT,
                        new LevelData.BossPlacement("revenant-walker", ARENA, PlayField.WIDTH / 2.0, 2),
                        Difficulty.MEDIUM)))
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

    /** The walker of a sortie of {@link #level()}. */
    public static SetPiece walker(Sortie sortie) {
        return WalkerLooks.walker(sortie);
    }
}
