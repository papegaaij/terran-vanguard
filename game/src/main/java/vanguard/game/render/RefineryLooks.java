package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part E, step E4: Level 39's refinery units beyond {@link EnemyLooks}' body (tools/art/refinery_units.py, whose
 * docstring gives the sprites; design/campaign Level 39, <em>The cracking tower and the pump station</em>), a look only:
 *
 * <ul>
 *   <li>a standing cracking tower's flare cap burning ({@code cracking-tower-flare}, additive, {@value #FLARE_FPS} fps,
 *       each tower from its own frame);
 *   <li>its fall in Io's near-vacuum at its death, shot or crushed by a toppled Strider: the short white flash, the top
 *       with the flare cap tumbling off, a ring of burning chunks along the ground that does not rise, the fires at the
 *       break dying ({@code cracking-tower-fall} with {@code -fall-glow} additive over it, {@value #FALL_FPS} fps, on
 *       the ground), then the dark wreck ({@code cracking-tower-wreck}) that stays until it leaves the screen; no
 *       fireball and no smoke ({@link #ownsDeath}: the screen skips the kind's explosion);
 *   <li>a pump station's wreck ({@code pump-station-wreck}) where it died, likewise until it leaves the screen (its
 *       burst is the vacuum burst of its rung, the screen's);
 *   <li>the pipes going dark: a tower's group cleared ({@code GROUP_CLEARED}) darkens the backdrop's pipe run whose
 *       {@code dark_with} names the group; a pump station's death the rack segment of its number ({@code P1} … in the
 *       order the pump stations entered, as the level's data places them).
 * </ul>
 *
 * Nothing is allocated per frame.
 */
final class RefineryLooks {
    static final String TOWER = "cracking-tower";
    static final String PUMP = "pump-station";

    /** The flare cap's flame, frames a second (tools/art/refinery_units.py: 8 frames, 10 fps). */
    static final double FLARE_FPS = 10;
    /** The fall, frames a second (12 frames, 1 s). */
    static final double FALL_FPS = 12;
    /** A wreck is forgotten once this far below the bottom edge, px (the fall's 160 px frame). */
    static final double GONE_BELOW = 100;
    /** The pump stations' names on the backdrop's rack segments ({@code dark_with}): P1 … */
    static final String PUMP_PREFIX = "P";

    private static final int SLOTS = 16;
    private static final int WRECKS = 16;

    private final boolean[] tower;
    private final boolean[] pump;
    private final Array<AtlasRegion> flare;
    private final Array<AtlasRegion> fall;
    private final Array<AtlasRegion> fallGlow;
    private final Array<AtlasRegion> towerWreck;
    private final Array<AtlasRegion> pumpWreck;
    /** The level's group names, by the index {@code GROUP_CLEARED} carries. */
    private final List<String> groups;

    /** The pump stations of the last step: serial (-1 free), number (1 …), place. */
    private final int[] pumpSerial = new int[SLOTS];

    private final int[] pumpNumber = new int[SLOTS];
    private final double[] pumpX = new double[SLOTS];
    private final double[] pumpY = new double[SLOTS];
    private final boolean[] pumpSeen = new boolean[SLOTS];
    /** The pump stations that entered so far (the next one's number less one). */
    private int pumpsEntered;

    /** The wrecks: whether a tower's (else a pump's), the step of the death, x, the place on the ground (y + scroll). */
    private final boolean[] wreckTower = new boolean[WRECKS];

    private final long[] wreckTick = new long[WRECKS];
    private final double[] wreckX = new double[WRECKS];
    private final double[] wreckGround = new double[WRECKS];
    private final boolean[] wreckUsed = new boolean[WRECKS];
    private int nextWreck;
    /** The names whose backdrop lights go dark at the next step's hand-over ({@link #darkened}); reused. */
    private final String[] dark = new String[SLOTS];

    private int darkCount;

    RefineryLooks(Sprites sprites, List<EnemySpec> kinds, List<String> groups) {
        this.groups = groups;
        tower = new boolean[kinds.size()];
        pump = new boolean[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            tower[k] = kinds.get(k).slug().equals(TOWER);
            pump[k] = kinds.get(k).slug().equals(PUMP);
        }
        flare = optional(sprites, TOWER + "-flare");
        fall = optional(sprites, TOWER + "-fall");
        fallGlow = optional(sprites, TOWER + "-fall-glow");
        towerWreck = optional(sprites, TOWER + "-wreck");
        pumpWreck = optional(sprites, PUMP + "-wreck");
        restart();
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : new Array<>(0);
    }

    /** The level restarts: no pump counted, no wreck, every light lit. */
    void restart() {
        Arrays.fill(pumpSerial, -1);
        Arrays.fill(wreckUsed, false);
        pumpsEntered = 0;
        nextWreck = 0;
        darkCount = 0;
    }

    boolean isTower(int kind) {
        return kind >= 0 && kind < tower.length && tower[kind];
    }

    boolean isPump(int kind) {
        return kind >= 0 && kind < pump.length && pump[kind];
    }

    /**
     * Whether a death of {@code kind} is drawn here instead of its explosion: the tower's fall (its own flash) with the
     * art in the atlas; the pump station's burst stays the screen's (its vacuum rung), its wreck is drawn here.
     */
    boolean ownsDeath(int kind) {
        return isTower(kind) && !fall.isEmpty();
    }

    /** M10 part E: the pump station's death is a vacuum burst without its fireball (Io's near-vacuum). */
    boolean vacuumBurst(int kind) {
        return isPump(kind) || (isTower(kind) && fall.isEmpty());
    }

    /**
     * After each simulation step: the deaths (a tower's fall and wreck, a pump's wreck and its rack going dark), the
     * groups cleared (their pipes going dark), then this step's pump stations (numbered as they enter).
     */
    void stepped(Sortie sortie) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            switch (events.type(i)) {
                case ENEMY_DESTROYED -> {
                    int kind = events.value(i);
                    if (isTower(kind)) {
                        died(true, events.x(i), events.y(i), sortie);
                    } else if (isPump(kind)) {
                        died(false, events.x(i), events.y(i), sortie);
                        int number = pumpAt(events.x(i), events.y(i));
                        if (number > 0) {
                            darken(PUMP_PREFIX + number);
                        }
                    }
                }
                case GROUP_CLEARED -> {
                    int group = events.value(i);
                    if (group >= 0 && group < groups.size()) {
                        darken(groups.get(group));
                    }
                }
                default -> {}
            }
        }
        trackPumps(sortie);
    }

    private void darken(String name) {
        if (darkCount < dark.length) {
            dark[darkCount++] = name;
        }
    }

    /** The backdrop's lights to go dark since the last hand-over, handed to {@code backdrop}. */
    void darkened(Backdrop backdrop) {
        for (int d = 0; d < darkCount; d++) {
            backdrop.darken(dark[d]);
        }
        clearDark();
    }

    /** The names waiting to go dark (for the tests). */
    int pendingDark() {
        return darkCount;
    }

    /** Waiting name {@code d} (for the tests). */
    String pendingDark(int d) {
        return dark[d];
    }

    /** Nothing waits to go dark. */
    void clearDark() {
        Arrays.fill(dark, null);
        darkCount = 0;
    }

    private void died(boolean isTower, double x, double y, Sortie sortie) {
        if (isTower ? towerWreck.isEmpty() && fall.isEmpty() : pumpWreck.isEmpty()) {
            return;
        }
        int w = nextWreck;
        nextWreck = (nextWreck + 1) % WRECKS;
        wreckUsed[w] = true;
        wreckTower[w] = isTower;
        wreckTick[w] = sortie.tick();
        wreckX[w] = x;
        wreckGround[w] = y + sortie.groundScroll();
    }

    /** The number of the last step's pump station nearest (x, y), -1 for none. */
    private int pumpAt(double x, double y) {
        int nearest = -1;
        double best = Double.POSITIVE_INFINITY;
        for (int s = 0; s < SLOTS; s++) {
            if (pumpSerial[s] < 0) {
                continue;
            }
            double d = Math.hypot(pumpX[s] - x, pumpY[s] - y);
            if (d < best) {
                best = d;
                nearest = s;
            }
        }
        if (nearest < 0) {
            return -1;
        }
        int number = pumpNumber[nearest];
        pumpSerial[nearest] = -1;
        return number;
    }

    private void trackPumps(Sortie sortie) {
        Arrays.fill(pumpSeen, false);
        for (int e = 0; e < sortie.enemyCount(); e++) {
            Enemy enemy = sortie.enemy(e);
            if (!isPump(enemy.kind())) {
                continue;
            }
            int slot = slotOf(enemy.serial());
            if (slot < 0) {
                slot = freeSlot();
                if (slot < 0) {
                    continue;
                }
                pumpSerial[slot] = enemy.serial();
                // Numbered in the order they enter, as the data places them (never two in one step).
                pumpNumber[slot] = ++pumpsEntered;
            }
            pumpSeen[slot] = true;
            pumpX[slot] = enemy.renderX(1);
            pumpY[slot] = enemy.renderY(1);
        }
        for (int s = 0; s < SLOTS; s++) {
            if (!pumpSeen[s]) {
                pumpSerial[s] = -1;
            }
        }
    }

    private int slotOf(int serial) {
        for (int s = 0; s < SLOTS; s++) {
            if (pumpSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    private int freeSlot() {
        for (int s = 0; s < SLOTS; s++) {
            if (pumpSerial[s] < 0) {
                return s;
            }
        }
        return -1;
    }

    /** The fall's frame {@code seconds} after the death, -1 once it is over (the wreck then). */
    static int fallFrame(double seconds, int count) {
        if (seconds < 0) {
            return 0;
        }
        int frame = (int) Math.floor(seconds * FALL_FPS);
        return frame < count ? frame : -1;
    }

    /** The flare's frame at step {@code tick} for a tower of serial {@code serial}. */
    static int flareFrame(long tick, int serial, int count) {
        return (int) Math.floorMod((long) Math.floor(tick * SimStep.SECONDS * FLARE_FPS) + 3L * serial, (long) count);
    }

    /**
     * On the ground, over the ground units' pass: the wrecks and the falls' bodies where the units died, scrolling with
     * the ground ({@code groundScroll} now); a wreck gone below the bottom edge is forgotten.
     */
    void drawGround(SpriteBatch batch, Sortie sortie, double groundScroll) {
        long tick = sortie.tick();
        for (int w = 0; w < WRECKS; w++) {
            if (!wreckUsed[w]) {
                continue;
            }
            double y = wreckGround[w] - groundScroll;
            if (y < -GONE_BELOW) {
                wreckUsed[w] = false;
                continue;
            }
            if (y > PlayField.HEIGHT + GONE_BELOW) {
                continue;
            }
            if (wreckTower[w]) {
                int frame = fallFrame((tick - wreckTick[w]) * SimStep.SECONDS, fall.size);
                if (frame >= 0) {
                    drawCentred(batch, fall.get(frame), wreckX[w], y);
                } else if (!towerWreck.isEmpty()) {
                    drawCentred(batch, towerWreck.first(), wreckX[w], y);
                }
            } else {
                drawCentred(batch, pumpWreck.first(), wreckX[w], y);
            }
        }
    }

    /**
     * The light, additive, after the ground units: each standing tower's flare cap burning, then the falls' flash and
     * fires.
     */
    void drawGlows(SpriteBatch batch, Sortie sortie, float alpha, double groundScroll) {
        boolean additive = false;
        long tick = sortie.tick();
        if (!flare.isEmpty()) {
            for (int e = 0; e < sortie.enemyCount(); e++) {
                Enemy enemy = sortie.enemy(e);
                if (!isTower(enemy.kind())) {
                    continue;
                }
                if (!additive) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    additive = true;
                }
                drawCentred(
                        batch,
                        flare.get(flareFrame(tick, enemy.serial(), flare.size)),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha));
            }
        }
        if (!fallGlow.isEmpty()) {
            for (int w = 0; w < WRECKS; w++) {
                if (!wreckUsed[w] || !wreckTower[w]) {
                    continue;
                }
                int frame = fallFrame((tick - wreckTick[w]) * SimStep.SECONDS, fallGlow.size);
                if (frame < 0) {
                    continue;
                }
                if (!additive) {
                    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    additive = true;
                }
                drawCentred(batch, fallGlow.get(frame), wreckX[w], wreckGround[w] - groundScroll);
            }
        }
        if (additive) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /** The wrecks lying now (for the tests). */
    int wrecks() {
        int n = 0;
        for (boolean used : wreckUsed) {
            n += used ? 1 : 0;
        }
        return n;
    }
}
