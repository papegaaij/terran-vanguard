package vanguard.game.render;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.AllySpec;
import vanguard.sim.Hitbox;
import vanguard.sim.HookMode;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.Sortie;
import vanguard.sim.WaveSpec;

/**
 * M7 part D: a fixture of Level 17's domes for the game's tests while the level's mapping is being written (step
 * D4): one section of {@value #LENGTH} s at {@value #SPEED} px/s with three domes (design/allies, Ares Landing dome:
 * a 120 px footprint, its hit box 32 px less, {@value #HITS} hits, a bomb run one hit), Bradbury at x 130 entering
 * at t=5, Lowell at x 350 at t=14 and the larger Eden (144 px) at x 240 at t=23, the untouched secondary {@code
 * DOMES}, and optionally a hooked wave of Spore Bombers (the content's, at medium; each drops one load) at t=0 over
 * Bradbury. As
 * sim's {@code SiegeSpecs} builds them; flown with the starter loadout and the ship invulnerable.
 */
public final class DomesFixture {
    public static final Content CONTENT = ContentLoader.fromClasspath();
    /** The rules of an Act 2 level (Act 3's own are the mapping's). */
    static final String RULES_LEVEL = "act-2-homefront/level-10-evacuation-corridor";

    public static final double SPEED = 80;
    public static final double LENGTH = 40;
    public static final double HITS = 3;
    public static final List<String> NAMES = List.of("Bradbury", "Lowell", "Eden");
    /** The domes' sprite sets, in the sortie's order (Eden's the level's {@code sprite}). */
    public static final List<String> SETS = List.of("ares-dome", "ares-dome", "ares-dome-eden");

    private DomesFixture() {}

    /** A dome: footprint {@code size}, hit box 32 px less, {@code hits} to break; bomb runs, lobs and rolls one hit each. */
    static AllySpec dome(double size, double hits) {
        return new AllySpec(
                "ares-landing-dome",
                new Hitbox(size, size),
                new Hitbox(size - 32, size - 32),
                hits,
                false,
                0,
                0.34,
                0,
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                true,
                1,
                1);
    }

    /** The level: the three domes, the untouched secondary, and {@code bombers} hooked bombers over Bradbury (0: none). */
    public static LevelScript level(int bombers) {
        AllySpec dome = dome(120, HITS);
        ObjectiveSpec.GroundAllies domes = new ObjectiveSpec.GroundAllies(
                List.of(
                        new ObjectiveSpec.GroundAlly(dome, NAMES.get(0), 130, 5),
                        new ObjectiveSpec.GroundAlly(dome, NAMES.get(1), 350, 14),
                        new ObjectiveSpec.GroundAlly(dome(144, HITS), NAMES.get(2), 240, 23)),
                Map.of("spore-bomber", HookMode.ALWAYS),
                160,
                40);
        List<WaveSpec> waves = bombers > 0
                ? List.of(new WaveSpec(
                        0,
                        WaveSpec.Formation.LINE_ABREAST,
                        SimSpecs.enemy(CONTENT, "spore-bomber", Difficulty.MEDIUM, Optional.empty()),
                        bombers,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE,
                        Optional.empty(),
                        Optional.empty(),
                        1,
                        Optional.empty(),
                        Optional.empty(),
                        List.of()))
                : List.of();
        return LevelScript.builder(17, 3)
                .sections(List.of(new Section(LENGTH, SPEED)))
                .waves(waves)
                .objectives(ObjectiveSpec.NONE
                        .withSecondary(Secondary.untouched(100, "DOMES"))
                        .withGroundAllies(Optional.of(domes)))
                .build();
    }

    public static Sortie sortie(int bombers) {
        return new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                level(bombers),
                SimSpecs.rules(CONTENT, RULES_LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Steps the sortie without input until level time {@code seconds}. */
    public static void runTo(Sortie sortie, double seconds) {
        while (sortie.levelSeconds() < seconds && !sortie.complete()) {
            sortie.step(vanguard.sim.Command.NONE);
        }
    }
}
