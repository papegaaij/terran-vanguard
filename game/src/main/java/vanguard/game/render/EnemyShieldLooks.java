package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.Arrays;
import vanguard.sim.Enemy;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * M9 part A: the Ascendancy's hex shields as the player sees them (design/enemies, Enemy shields; tools/art/act5_gear.py
 * {@code shield}, whose docstring gives the sprites; round 61's "gold film" b written until the pick), all additive:
 * on each shielded unit while its shield holds a round bubble of its size tier ({@code enemy-shield-<tier>_0..3}, a
 * shimmer loop of {@value #HOLD_FRAME_TICKS} steps a frame; its opacity follows the pool's fill, {@link #holdOpacity}),
 * a hit that held lights the hexes on the bubble's rim facing the ship ({@code enemy-shield-hit}, {@value
 * #HIT_FRAME_TICKS} steps a frame; the simulation's event is at the unit, so the rim point is the one toward the
 * player's fire), emptied it shatters in a ring of gold fragments ({@code -<tier>-break}, 0.2 s, following the unit),
 * and as it starts to refill it shimmers in from the edge ({@code -<tier>-refill}, 0.4 s) before the holding look. The
 * tier is the unit's size: its sprite's larger side up to {@value #SMALL_MAX} px small, up to {@value #MEDIUM_MAX}
 * medium, else large.
 */
final class EnemyShieldLooks {
    static final String ROOT = "enemy-shield";
    static final String[] TIERS = {"small", "medium", "large"};
    /** The tiers' unit sizes (the larger side of its sprite, px): the art's small and medium bubbles' units. */
    static final int SMALL_MAX = 48;

    static final int MEDIUM_MAX = 84;
    static final int HOLD_FRAME_TICKS = 6;
    static final int HIT_FRAME_TICKS = 2;
    static final int BREAK_FRAME_TICKS = 2;
    static final int REFILL_FRAME_TICKS = 3;
    /** The holding bubble's opacity: this at an empty pool, rising to 1 at a full one. */
    static final float HOLD_LOW = 0.55f;
    /** The animations playing at once (a unit's hit events come at most every few steps). */
    private static final int SLOTS = 24;

    private static final long NEVER = Long.MIN_VALUE / 2;

    /** Per tier: the holding loop, the break, the refill. */
    private final Array<Array<AtlasRegion>> hold = new Array<>();

    private final Array<Array<AtlasRegion>> breaks = new Array<>();
    private final Array<Array<AtlasRegion>> refills = new Array<>();
    private final Array<AtlasRegion> hit;
    /** Per enemy kind: its tier (an index of {@link #TIERS}). */
    private final int[] tiers;
    /** The hit flashes: the unit's serial (-1 free), the step, the angle of its rim point (radians, y up). */
    private final int[] hitSerial = new int[SLOTS];

    private final long[] hitTick = new long[SLOTS];
    private final double[] hitAngle = new double[SLOTS];
    private int nextHit;
    /** The breaks: the unit's serial (-1 free), the step, where it was last seen and its tier. */
    private final int[] breakSerial = new int[SLOTS];

    private final long[] breakTick = new long[SLOTS];
    private final double[] breakX = new double[SLOTS];
    private final double[] breakY = new double[SLOTS];
    private final int[] breakTier = new int[SLOTS];
    private int nextBreak;
    /** The units whose shield is down, waiting for its refill to start (serial, -1 free). */
    private final int[] downSerial = new int[SLOTS];
    /** The refills playing: the unit's serial (-1 free) and the step it started. */
    private final int[] refillSerial = new int[SLOTS];

    private final long[] refillTick = new long[SLOTS];
    private int nextRefill;

    private EnemyShieldLooks(Sprites sprites, int[] tiers) {
        for (String tier : TIERS) {
            hold.add(sprites.frames(ROOT + "-" + tier));
            breaks.add(sprites.frames(ROOT + "-" + tier + "-break"));
            refills.add(sprites.frames(ROOT + "-" + tier + "-refill"));
        }
        hit = sprites.frames(ROOT + "-hit");
        this.tiers = tiers;
        restart();
    }

    /** The looks for the level's enemy kinds ({@code looks} by kind); null while the shield's art is not loaded. */
    static EnemyShieldLooks of(Sprites sprites, EnemyLooks[] looks) {
        for (String tier : TIERS) {
            if (!sprites.has(ROOT + "-" + tier)
                    || !sprites.has(ROOT + "-" + tier + "-break")
                    || !sprites.has(ROOT + "-" + tier + "-refill")) {
                return null;
            }
        }
        if (!sprites.has(ROOT + "-hit")) {
            return null;
        }
        int[] tiers = new int[looks.length];
        for (int k = 0; k < looks.length; k++) {
            Array<AtlasRegion> frames = looks[k].frames();
            int size = frames.isEmpty()
                    ? 0
                    : Math.max(frames.first().getRegionWidth(), frames.first().getRegionHeight());
            tiers[k] = tier(size);
        }
        return new EnemyShieldLooks(sprites, tiers);
    }

    /** The tier (an index of {@link #TIERS}) of a unit whose sprite's larger side is {@code size} px. */
    static int tier(int size) {
        return size <= SMALL_MAX ? 0 : size <= MEDIUM_MAX ? 1 : 2;
    }

    /** The holding bubble's opacity at the pool's fill {@code share} (0..1). */
    static float holdOpacity(double share) {
        return (float) (HOLD_LOW + (1 - HOLD_LOW) * Math.clamp(share, 0, 1));
    }

    /** The holding loop's frame at step {@code tick} for a unit (its serial staggers the loops). */
    static int holdFrame(long tick, int serial, int frames) {
        return (int) Math.floorMod(tick / HOLD_FRAME_TICKS + serial, (long) frames);
    }

    /** An animation's frame {@code age} steps after its start at {@code ticksPerFrame}; -1 once it has played out. */
    static int frameAt(long age, int ticksPerFrame, int frames) {
        if (age < 0) {
            return -1;
        }
        long frame = age / ticksPerFrame;
        return frame >= frames ? -1 : (int) frame;
    }

    /** The level restarts: nothing plays. */
    void restart() {
        Arrays.fill(hitSerial, -1);
        Arrays.fill(breakSerial, -1);
        Arrays.fill(downSerial, -1);
        Arrays.fill(refillSerial, -1);
        nextHit = nextBreak = nextRefill = 0;
    }

    /**
     * After each simulation step: a hit that held flashes ({@link SimEvents.Type#ENEMY_SHIELD_HIT}), an emptied
     * shield breaks ({@link SimEvents.Type#ENEMY_SHIELD_BROKEN}); a shield that was down and holds points again starts
     * its refill's shimmer.
     */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            SimEvents.Type type = events.type(i);
            if (type != SimEvents.Type.ENEMY_SHIELD_HIT && type != SimEvents.Type.ENEMY_SHIELD_BROKEN) {
                continue;
            }
            Enemy unit = shieldedAt(sortie, events.x(i), events.y(i));
            if (unit == null) {
                continue;
            }
            if (type == SimEvents.Type.ENEMY_SHIELD_HIT) {
                hitSerial[nextHit] = unit.serial();
                hitTick[nextHit] = tick;
                hitAngle[nextHit] = Math.atan2(
                        sortie.ship().renderY(1) - unit.renderY(1),
                        sortie.ship().renderX(1) - unit.renderX(1));
                nextHit = (nextHit + 1) % SLOTS;
            } else {
                breakSerial[nextBreak] = unit.serial();
                breakTick[nextBreak] = tick;
                breakX[nextBreak] = unit.renderX(1);
                breakY[nextBreak] = unit.renderY(1);
                breakTier[nextBreak] = tierOf(unit);
                nextBreak = (nextBreak + 1) % SLOTS;
                down(unit.serial());
            }
        }
        for (int d = 0; d < SLOTS; d++) {
            if (downSerial[d] < 0) {
                continue;
            }
            Enemy unit = bySerial(sortie, downSerial[d]);
            if (unit == null) {
                downSerial[d] = -1;
            } else if (unit.shield() > 0) {
                refillSerial[nextRefill] = unit.serial();
                refillTick[nextRefill] = tick;
                nextRefill = (nextRefill + 1) % SLOTS;
                downSerial[d] = -1;
            }
        }
    }

    /** Notes a unit whose shield went down (its refill's start is watched for). */
    private void down(int serial) {
        int free = -1;
        for (int d = 0; d < SLOTS; d++) {
            if (downSerial[d] == serial) {
                return;
            }
            if (free < 0 && downSerial[d] < 0) {
                free = d;
            }
        }
        if (free >= 0) {
            downSerial[free] = serial;
        }
    }

    private int tierOf(Enemy unit) {
        return unit.kind() >= 0 && unit.kind() < tiers.length ? tiers[unit.kind()] : 0;
    }

    /** The shielded unit nearest (x, y), where its event happened; null when none is that close. */
    private static Enemy shieldedAt(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.shieldMax() <= 0) {
                continue;
            }
            double distance = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (distance < best
                    && distance <= enemy.hitbox().width() + enemy.hitbox().height()) {
                best = distance;
                found = enemy;
            }
        }
        return found;
    }

    private static Enemy bySerial(Sortie sortie, int serial) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.serial() == serial) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * The shields of the units on one side of the layers ({@code ground}: the ground units, drawn with them; else the
     * flyers), additive: the holding bubble or its refill, the hits' flashes on its rim, then the breaks.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, boolean ground) {
        long tick = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.shieldMax() <= 0
                    || enemy.cloaked()
                    || (DrawPasses.Depth.of(enemy) == DrawPasses.Depth.GROUND) != ground) {
                continue;
            }
            int tier = tierOf(enemy);
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            int refill = refillFrame(enemy.serial(), tick, refills.get(tier).size);
            if (refill >= 0) {
                batch.setColor(Color.WHITE);
                drawCentred(batch, refills.get(tier).get(refill), x, y);
            } else if (enemy.shield() > 0) {
                Array<AtlasRegion> loop = hold.get(tier);
                batch.setColor(1, 1, 1, holdOpacity(enemy.shield() / enemy.shieldMax()));
                drawCentred(batch, loop.get(holdFrame(tick, enemy.serial(), loop.size)), x, y);
            }
            batch.setColor(Color.WHITE);
            double radius = hold.get(tier).first().getRegionWidth() / 2.0;
            for (int h = 0; h < SLOTS; h++) {
                if (hitSerial[h] != enemy.serial()) {
                    continue;
                }
                int frame = frameAt(tick - hitTick[h], HIT_FRAME_TICKS, hit.size);
                if (frame >= 0) {
                    drawCentred(
                            batch,
                            hit.get(frame),
                            x + Math.cos(hitAngle[h]) * radius,
                            y + Math.sin(hitAngle[h]) * radius);
                }
            }
        }
        for (int b = 0; b < SLOTS; b++) {
            if (breakSerial[b] < 0) {
                continue;
            }
            Array<AtlasRegion> frames = breaks.get(breakTier[b]);
            int frame = frameAt(tick - breakTick[b], BREAK_FRAME_TICKS, frames.size);
            if (frame < 0) {
                breakSerial[b] = -1;
                continue;
            }
            Enemy unit = bySerial(sortie, breakSerial[b]);
            if (unit != null) {
                if ((DrawPasses.Depth.of(unit) == DrawPasses.Depth.GROUND) != ground) {
                    continue;
                }
                breakX[b] = unit.renderX(alpha);
                breakY[b] = unit.renderY(alpha);
            } else if (ground) {
                continue;
            }
            drawCentred(batch, frames.get(frame), breakX[b], breakY[b]);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The refill's frame for unit {@code serial} at step {@code tick}; -1 when none plays. */
    private int refillFrame(int serial, long tick, int frames) {
        int frame = -1;
        for (int r = 0; r < SLOTS; r++) {
            if (refillSerial[r] == serial) {
                int at = frameAt(tick - refillTick[r], REFILL_FRAME_TICKS, frames);
                if (at < 0) {
                    refillSerial[r] = -1;
                } else {
                    frame = at;
                }
            }
        }
        return frame;
    }

    private static void drawCentred(SpriteBatch batch, AtlasRegion region, double x, double y) {
        batch.draw(region, (float) Math.round(PixelScreen.PLAY_FIELD_X + x - region.getRegionWidth() / 2f), (float)
                Math.round(y - region.getRegionHeight() / 2f));
    }
}
