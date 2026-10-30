package vanguard.game.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Enemy;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * Step MARK70 (design/campaign Level 29, Friendly marker; round 70 B.1 d): the IFF brackets with their {@code FRIENDLY}
 * tag on each neutral craft while it is on the screen, gone as it leaves or is destroyed, and the "FRIENDLIES INBOUND —
 * HOLD FIRE" banner ≈ 2 s before each wavering pair's first pixel, in its own slot under the hostile band (stacked
 * under the t=77 and t=97 pincers' amber banners, never waiting for them). Level 29 at medium, flown invulnerable,
 * steering clear of the wavering craft and through the rescue halt's clamp guns.
 */
class FriendlyMarkerTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(29).orElseThrow();
    private static final String HAWK = "wavering-stormhawk";
    private static final double HALF = CONTENT.enemy(HAWK).size().height() / 2;
    private static final double[] PAIRS = {77, 98, 140};

    /** The level seconds of each pair's first pixel, the friendly banner's openings, both banners showing together. */
    private static final List<Double> firstPixels = new ArrayList<>();

    private static final List<Double> openings = new ArrayList<>();
    private static final List<Double> stacked = new ArrayList<>();
    /** Steps where the marks differ from the neutral craft on the screen, or one is off its craft. */
    private static final List<String> misses = new ArrayList<>();
    /** The most marks at once, and the steps with marks before the first pair and between the pairs. */
    private static int most;

    private static int strayMarks;

    @BeforeAll
    static void fly() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FriendlyMarks marks = new FriendlyMarks(null, null);
        FriendlyBanner friendly = new FriendlyBanner(null, null, false);
        WaveBanners hostile = new WaveBanners(null, null, false);
        int warned = 0;
        int onScreenBefore = 0;
        long limit = SimStep.ticks(400);
        while (!sortie.complete() && sortie.levelSeconds() < 150 && sortie.tick() < limit) {
            sortie.step(IceShelfFixture.toward(sortie, aim(sortie), 90));
            boolean reversed = sortie.reversed();
            // The edges whose warning started in this step, as EdgeWarnings.step returns them.
            int started = sortie.edgeWarnings() & ~warned;
            warned = sortie.edgeWarnings();
            hostile.step(sortie.edgeWarnings(), started, sortie.tick(), reversed);
            boolean was = friendly.showing();
            friendly.step(sortie.friendliesInbound(), sortie.tick());
            double t = sortie.levelSeconds();
            if (friendly.showing() && !was) {
                openings.add(t);
            }
            if (friendly.showing() && !hostile.shown().isEmpty()) {
                stacked.add(t);
            }
            marks.plan(sortie, 1);
            int onScreen = 0;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                if (enemy.spec().neutral() && onScreen(enemy)) {
                    boolean found = false;
                    for (int m = 0; m < marks.count(); m++) {
                        found |= marks.x(m) == (int) Math.round(enemy.renderX(1))
                                && marks.y(m) == (int) Math.round(enemy.renderY(1));
                    }
                    if (!found) {
                        misses.add(String.format("t=%.2f: a craft on the screen unmarked", t));
                    }
                    onScreen++;
                }
            }
            if (marks.count() != onScreen) {
                misses.add(String.format("t=%.2f: %d marks, %d craft on the screen", t, marks.count(), onScreen));
            }
            if (onScreen > 0 && onScreenBefore == 0) {
                firstPixels.add(t);
            }
            if (onScreen == 0 && marks.count() > 0) {
                strayMarks++;
            }
            most = Math.max(most, marks.count());
            onScreenBefore = onScreen;
        }
    }

    /** Whether any pixel of the neutral craft's sprite is on the screen. */
    private static boolean onScreen(Enemy enemy) {
        double x = enemy.renderX(1);
        double y = enemy.renderY(1);
        return y - HALF < PlayField.HEIGHT && y + HALF > 0 && x + HALF > 0 && x - HALF < PlayField.WIDTH;
    }

    /**
     * Where the ship steers: clear of the wavering craft on the screen (the lane among 40, 240 and 440 farthest from
     * them), else under the lowest other unit ahead (the halt's clamp guns among them).
     */
    private static double aim(Sortie sortie) {
        double best = Double.NaN;
        double bestGap = -1;
        boolean neutral = false;
        for (double lane : new double[] {40, 240, 440}) {
            double gap = Double.MAX_VALUE;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                if (enemy.spec().neutral() && enemy.renderY(1) - HALF < PlayField.HEIGHT + 60) {
                    neutral = true;
                    gap = Math.min(gap, Math.abs(enemy.renderX(1) - lane));
                }
            }
            if (gap > bestGap) {
                bestGap = gap;
                best = lane;
            }
        }
        if (neutral) {
            return best;
        }
        double lowest = Double.MAX_VALUE;
        double x = 240;
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            double y = enemy.renderY(1);
            if (y > 150 && y < PlayField.HEIGHT && y < lowest) {
                lowest = y;
                x = enemy.renderX(1);
            }
        }
        return x;
    }

    @Test
    void theFlightReachesTheThirdPair() {
        assertEquals(3, firstPixels.size(), "three wavering pairs on the screen: " + firstPixels);
        for (int i = 0; i < PAIRS.length; i++) {
            assertEquals(PAIRS[i], firstPixels.get(i), 0.5, "pair " + (i + 1) + "'s first pixel just after its wave");
        }
    }

    /** The brackets: one on each neutral craft while any of it is on the screen, at its spot; none otherwise. */
    @Test
    void theBracketsMarkEachWaveringCraftWhileItIsOnTheScreen() {
        assertTrue(misses.isEmpty(), misses.size() + " steps off: " + misses.subList(0, Math.min(5, misses.size())));
        assertEquals(2, most, "both craft of a pair marked");
        assertEquals(0, strayMarks, "no mark without a craft on the screen");
    }

    /** A craft shot down loses its brackets in the same step (flown under the first pair, firing). */
    @Test
    void aDestroyedCraftLosesItsBrackets() {
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FriendlyMarks marks = new FriendlyMarks(null, null);
        int before = 0;
        boolean down = false;
        while (!down && sortie.levelSeconds() < 140) {
            double x = 240;
            for (int e = 0; e < sortie.enemyCount(); e++) {
                if (sortie.enemy(e).spec().neutral()) {
                    x = sortie.enemy(e).renderX(1);
                    break;
                }
            }
            sortie.step(IceShelfFixture.toward(sortie, x, 100));
            marks.plan(sortie, 1);
            for (int i = 0; i < sortie.events().size(); i++) {
                down |= sortie.events().type(i) == SimEvents.Type.NEUTRAL_DOWN;
            }
            if (down) {
                assertEquals(before - 1, marks.count(), "the downed craft's brackets gone at once");
            }
            before = marks.count();
        }
        assertTrue(down, "a wavering craft shot down by t=140 (60 HP at medium since step FIX7)");
    }

    /**
     * The banner: it opens ≈ 2 s before each pair's first pixel (t≈75, 96, 138) and shows for the warning banner's
     * 2.5 s; at t=77 and t=97 the pincers' amber banner shows at the same moment, so it has its own slot under the
     * hostile band (stacked), never in its place.
     */
    @Test
    void theBannerOpensTwoSecondsBeforeEachPairInItsOwnSlot() {
        assertEquals(3, openings.size(), "one banner a pair: " + openings);
        for (int i = 0; i < openings.size(); i++) {
            double lead = firstPixels.get(i) - openings.get(i);
            assertTrue(lead >= 1.8 && lead <= 2.5, String.format("pair %d: the banner %.2f s ahead", i + 1, lead));
        }
        System.out.printf(
                "first pixels %s, banners open at %s, stacked with an amber banner %.2f s from t=%.2f%n",
                firstPixels,
                openings,
                stacked.size() * SimStep.SECONDS,
                stacked.isEmpty() ? Double.NaN : stacked.get(0));
        assertFalse(stacked.isEmpty(), "the pincers' banner and this one show together (stacked)");
        int top = FriendlyBanner.centreY(false) + WaveBanners.HEIGHT / 2;
        int hostileBottom = WaveBanners.centreY(false) - WaveBanners.HEIGHT / 2;
        assertTrue(top < hostileBottom, "its band is under the hostile band, not over it");
        assertTrue(
                FriendlyBanner.centreY(true) + WaveBanners.HEIGHT / 2
                        < WaveBanners.centreY(true) - WaveBanners.HEIGHT / 2,
                "and with an air escort");
    }

    /** It shows for 2.5 s from the announcement's start, once an announcement, and a restart clears it. */
    @Test
    void theBannerShowsTwoAndAHalfSecondsAnAnnouncement() {
        FriendlyBanner banner = new FriendlyBanner(null, null, false);
        int ticks = SimStep.ticks(WaveBanners.SECONDS);
        banner.step(false, 0);
        assertFalse(banner.showing());
        banner.step(true, 1);
        assertTrue(banner.showing());
        banner.step(true, 1 + SimStep.ticks(1.9));
        assertTrue(banner.showing(), "through the announcement");
        banner.step(false, 1 + ticks - 1);
        assertTrue(banner.showing());
        banner.step(false, 1 + ticks);
        assertFalse(banner.showing(), "gone after 2.5 s");
        banner.step(true, 2 + ticks);
        assertTrue(banner.showing(), "the next announcement opens it again");
        banner.clear();
        assertFalse(banner.showing());
        assertEquals("FRIENDLIES INBOUND — HOLD FIRE", FriendlyBanner.TEXT);
    }
}
