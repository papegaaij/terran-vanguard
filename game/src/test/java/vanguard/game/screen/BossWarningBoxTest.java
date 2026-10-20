package vanguard.game.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelScript;
import vanguard.sim.SetPiece;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * The act bosses' warning box (round 41, the 2026-10-19 playtest: the Siege Spire fired from under it): in
 * each level with a boss warning track (the Brood Carrier's Level 07, the Siege Spire's Level 14; the other
 * bosses, the Kraken and the mid-bosses, have the sting and no box), flown on medium with the ship
 * invulnerable and holding still, the box opens at the boss's lead warning ({@code BOSS_INCOMING}) or else
 * its arrival, and closes early as the screen does ({@link LevelScreen#fightsUnderBanner}): at the arrival
 * when it was announced ahead, or once the boss fights under it. Every box shows long enough to read; the
 * boxes are printed for the user's review.
 */
class BossWarningBoxTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /** The box shows at least this long: its 0.2 s grow-in and the boss's name read once. */
    private static final double READ_SECONDS = 1.5;
    /** The box's full time, as {@code BossBanner.SECONDS}. */
    private static final double BOX_SECONDS = 5;

    /**
     * Levels whose box does not show long enough yet: none since step FX-C gave the Siege Spire its 7.5 s lead
     * ({@code boss.incoming}, {@code BossSpec.incomingSeconds}); a new level that cannot meet the rule yet is
     * listed here.
     */
    private static final java.util.Set<String> PENDING = java.util.Set.of();

    /** A boss's box in a flown level: when it opened and how long it showed. */
    record Box(String level, String boss, double opened, double shown, boolean ahead, String closedBy) {}

    @Test
    void everyActBossBoxShowsAndNeverStaysOverTheFight() {
        List<Box> boxes = new ArrayList<>();
        for (String key : CONTENT.levels().keySet().stream().sorted().toList()) {
            LevelData level = CONTENT.level(key);
            if (LevelScreen.bossMusic(level.music()).isEmpty()) {
                continue;
            }
            boxes.add(fly(key));
        }
        StringBuilder report = new StringBuilder();
        for (Box box : boxes) {
            report.append(String.format(
                    "%n%s %s: opened at %.1f s%s, shown %.1f s (%s)",
                    box.level(),
                    box.boss(),
                    box.opened(),
                    box.ahead() ? " ahead of its arrival" : " at its arrival",
                    box.shown(),
                    box.closedBy()));
        }
        System.out.println("boss warning boxes:" + report);
        assertTrue(boxes.size() >= 2, "the act boss levels are flown:" + report);
        for (Box box : boxes) {
            assertTrue(box.opened() >= 0, box.level() + ": the boss's box opens" + report);
            assertTrue(
                    box.shown() >= READ_SECONDS || PENDING.contains(box.level()),
                    box.level() + ": the box shows long enough to read" + report);
        }
    }

    /** Flies {@code key} to its first act boss's warning and follows the box over its 5 s. */
    private static Box fly(String key) {
        LevelScript script = SimSpecs.level(CONTENT, key, Difficulty.MEDIUM);
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                script,
                SimSpecs.rules(CONTENT, key, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        int last = SimStep.ticks(script.seconds() + 120);
        int piece = -1;
        boolean ahead = false;
        long opened = -1;
        for (int step = 0; step < last && !sortie.complete(); step++) {
            sortie.step(0);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (opened < 0
                        && (type == SimEvents.Type.BOSS_INCOMING || type == SimEvents.Type.BOSS_ARRIVED)
                        && sortie.setPiece(events.value(i)).boss().isPresent()) {
                    piece = events.value(i);
                    ahead = type == SimEvents.Type.BOSS_INCOMING;
                    opened = sortie.tick();
                } else if (opened >= 0 && ahead && type == SimEvents.Type.BOSS_ARRIVED && events.value(i) == piece) {
                    return box(key, sortie.setPiece(piece), opened, sortie.tick(), true, "the boss arrived");
                }
            }
            if (opened >= 0) {
                SetPiece boss = sortie.setPiece(piece);
                if (LevelScreen.fightsUnderBanner(boss)) {
                    return box(key, boss, opened, sortie.tick(), ahead, "the boss fights under it");
                }
                if ((sortie.tick() - opened) * SimStep.SECONDS >= BOX_SECONDS) {
                    return box(key, boss, opened, sortie.tick(), ahead, "its full time");
                }
            }
        }
        return new Box(key, "(no boss reached)", -1, 0, false, "never opened");
    }

    private static Box box(String key, SetPiece boss, long opened, long closed, boolean ahead, String why) {
        return new Box(
                key,
                boss.boss().orElseThrow().barName(),
                opened * SimStep.SECONDS,
                (closed - opened) * SimStep.SECONDS,
                ahead,
                why);
    }
}
