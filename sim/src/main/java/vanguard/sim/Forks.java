package vanguard.sim;

import java.util.List;

/**
 * M9 part E, a level's route choice (design/campaign Level 32, The forks; Q5 a): at each fork ({@link
 * GroundSpec.TerrainSpec#forks()}) the branch is the channel the ship's centre is in when the fork's island's tip
 * reaches the ship's row: left of the island's middle the left channel, else the right. The choice is made by the
 * ship's position, so a replay of the same input makes it again. Chosen, the side's waves fly ({@link
 * EnemyEntries}), its dormant ground units and objects wake (those entering later enter awake), and the other side's
 * waves never come and its ground units and objects stay dormant: they count nowhere ({@link #unchosen()}). The event
 * {@link SimEvents.Type#BRANCH_CHOSEN} (value {@link #code}) and the radio's {@link CueTrigger#BRANCH} cue ({@link
 * #subject}) mark the choice. Allocates nothing after it is made; only a level with forks has one.
 */
final class Forks {
    /** The most forks a level has. */
    static final int MOST_FORKS = Branch.MOST_FORKS;

    private static final String[] SUBJECTS = subjects();

    private final Terrain terrain;
    /** Per fork, its island's index. */
    private final int[] islands;
    /** Per branch code, the units counted as enemies its waves and ground units send. */
    private final int[] units;
    /** Per fork, the side chosen ({@link Branch#LEFT}, {@link Branch#RIGHT}); -1 before the choice. */
    private final int[] sides;
    /** The units of the branches not chosen so far. */
    private int unchosen;

    Forks(Terrain terrain, List<Integer> forkIslands, int[] branchUnits) {
        this.terrain = terrain;
        islands = forkIslands.stream().mapToInt(Integer::intValue).toArray();
        units = branchUnits.clone();
        sides = new int[islands.length];
        reset();
    }

    /** Fork {@code fork}'s branch on {@code side} as one number: fork × 2 + side. */
    static int code(int fork, int side) {
        return fork * 2 + side;
    }

    /**
     * The subject of the radio's {@link CueTrigger#BRANCH} cue of fork {@code fork} (from 0) and {@code side}: the fork
     * counted from 1, a colon and {@code left} or {@code right} (fork 0's right channel: {@code "1:right"}).
     */
    static String subject(int fork, int side) {
        return SUBJECTS[code(fork, side)];
    }

    private static String[] subjects() {
        String[] out = new String[2 * MOST_FORKS];
        for (int f = 0; f < MOST_FORKS; f++) {
            out[code(f, Branch.LEFT)] = (f + 1) + ":left";
            out[code(f, Branch.RIGHT)] = (f + 1) + ":right";
        }
        return out;
    }

    /** Back to the level start: no fork chosen. */
    void reset() {
        java.util.Arrays.fill(sides, -1);
        unchosen = 0;
    }

    void addTo(StateHash hash) {
        for (int side : sides) {
            hash.add(side);
        }
    }

    /** The number of forks. */
    int count() {
        return sides.length;
    }

    /** Fork {@code fork}'s side chosen; -1 before its choice. */
    int side(int fork) {
        return sides[fork];
    }

    /** Whether the branch {@code code} was chosen (false before its fork's choice). */
    boolean chosen(int code) {
        return sides[code / 2] == code % 2;
    }

    /** The units (counted as enemies) of the branches not chosen so far: they never fly, wake or pay. */
    int unchosen() {
        return unchosen;
    }

    /**
     * One step: a fork whose island's tip reached the ship's row at (shipX, shipY) is chosen now; the chosen side's
     * dormant units of {@code enemies} and objects of {@code ground} wake at {@code levelTick}.
     */
    void step(
            int levelTick,
            double groundScroll,
            double shipX,
            double shipY,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            SimEvents events,
            Radio radio) {
        for (int f = 0; f < sides.length; f++) {
            if (sides[f] >= 0 || Terrain.row(groundScroll, shipY) < terrain.islandTip(islands[f])) {
                continue;
            }
            int side = shipX < terrain.islandMiddle(islands[f]) ? Branch.LEFT : Branch.RIGHT;
            sides[f] = side;
            int code = code(f, side);
            unchosen += units[code(f, 1 - side)];
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (enemy.branch == code && enemy.dormantTick >= 0) {
                    // Woken by the wake pass this step (its tell, WOKE).
                    enemy.dormantTick = levelTick;
                }
            }
            for (int i = 0; i < ground.size(); i++) {
                GroundObject object = ground.get(i);
                if (object.spec().branch().isPresent()
                        && object.spec().branch().get().code() == code) {
                    object.dormant(false);
                }
            }
            events.add(SimEvents.Type.BRANCH_CHOSEN, shipX, shipY, code);
            radio.cue(CueTrigger.BRANCH, subject(f, side));
        }
    }

    /**
     * Whether a unit or object of branch {@code code} entering now enters dormant: before its fork's choice (it may yet
     * wake) or on the side not chosen (it never will).
     */
    boolean dormantOnEntry(int code) {
        return !chosen(code);
    }
}
