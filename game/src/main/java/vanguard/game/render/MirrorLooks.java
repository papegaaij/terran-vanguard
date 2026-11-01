package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.EnemySpec;
import vanguard.sim.Mirroring;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M10 part A (design/enemies, Reflect; design/enemies/air/mirror-interceptor, Look): a mirrored unit's plate as the player
 * sees it, on the stand-in unit sprite until the unit's own art (part D) carries the plate. The plate is a crescent over
 * the mirror's arc, {@link #reach} px out along the mirror's own facing ({@link Mirroring#facing()}; the unit's sprite
 * follows it too, {@link #heading}): polished silver-gold with a slow specular sweep from tip to tip. A beam's heat
 * glows over it dull red, orange, then white ({@link #heatColour}), cracks show from {@value #CRACKS_FROM} of its heat
 * ({@link #crackOpacity}); broken or shattered it is a bare grey frame, and as it regrows a silver shimmer sweeps across
 * from one tip ({@link #regrowShare}). A break throws its shards ({@link #shatter}: silver for a shatter, glowing for a
 * heat break). The crescent, its cracks and the shards are textures generated once per level in smooth float alpha (no
 * ordered dither: the 2026-10-30 dither audit), drawn once at 1x their play size; the glows are {@link SoftGlow}'s.
 *
 * <p>M10 part C, step C4b (design/enemies/air/mirror-interceptor, Look; tools/art/aurelia_units.py): a kind with its
 * production frames ({@code <slug>-plate}, 16 headings over the hull's, which shows the bare grey nose frame on its own)
 * is drawn from them instead of the generated crescent: the plate frame of the mirror's facing (a regrowing one at its
 * regrown share's opacity under the shimmer), its cracks ({@code -cracks}) tinted dark from {@value #CRACKS_FROM} of
 * the heat and in the heat's colour through a white-hot plate, the heat through the plate's glow mask ({@code
 * -plate-glow}, additive, tinted by {@link #heatColour}) and the pods' red glow ({@code -pods}, additive) over the
 * {@value #POD_TELL_SECONDS} s before each predicted burst ({@link #podGlow}). The sweep, the shimmer and the shards
 * stay drawn in code, on the art's plate radius ({@value #ART_REACH} px).
 */
final class MirrorLooks implements Disposable {
    /** The generated plate texture's side, px, and the crescent's radius in it. */
    static final int PLATE_SIZE = 64;

    static final double PLATE_RADIUS = 26;
    /** The crescent's half width at its middle, px of the texture (it tapers to nothing at the tips). */
    static final double PLATE_HALF_WIDTH = 3.2;
    /** The plate lies this share of the unit's larger hit-box side out from its centre. */
    static final double REACH = 0.55;
    /** Cracks show from this share of the heat. */
    static final double CRACKS_FROM = 0.7;
    /** The specular sweep runs tip to tip once in this many seconds. */
    static final double SWEEP_SECONDS = 2.5;
    /** A broken plate's bare frame: grey, at this opacity. */
    static final Color BARE = new Color(0.34f, 0.35f, 0.38f, 0.6f);
    /** The polished plate: silver with a gold cast. */
    static final Color PLATE = new Color(0.88f, 0.82f, 0.62f, 0.95f);
    /** The shards of a shatter fly this long, seconds, this many a break. */
    static final double SHARD_SECONDS = 0.5;

    static final int SHARDS = 9;
    /** The breaks whose shards fly at once. */
    private static final int BURSTS = 8;
    /** M10 part C: the production plate's radius from the unit's centre in its frames, px (aurelia_units.py). */
    static final double ART_REACH = 32;
    /** M10 part C: the pods' red glow runs this long before a burst (the unit's 0.4 s tell). */
    static final double POD_TELL_SECONDS = 0.4;
    /** M10 part C: a shot leaves a unit's pods within this of its centre, px (the muzzles under the rim). */
    static final double POD_REACH = 48;
    /** M10 part C: the mirrors whose guns are followed at once. */
    private static final int GUNS = 16;

    private static final Color DULL_RED = new Color(0.62f, 0.07f, 0.02f, 1);
    private static final Color ORANGE = new Color(1f, 0.52f, 0.1f, 1);
    private static final Color WHITE_HOT = new Color(1f, 0.96f, 0.86f, 1);

    private final SoftGlow glow;
    /** Per enemy kind: its mirror's plate texture (one per distinct arc), null for a kind without a mirror. */
    private final TextureRegion[] plates;

    private final TextureRegion[] cracks;
    private final java.util.Map<Double, TextureRegion[]> byArc = new java.util.HashMap<>();
    private final java.util.List<Texture> textures = new java.util.ArrayList<>();
    private final TextureRegion shard;
    private final Color colour = new Color();
    /** The shard bursts: the step each started (-1 free), where, toward which heading, and how (1 shatter, 0 heat). */
    private final long[] burstTick = new long[BURSTS];

    private final double[] burstX = new double[BURSTS];
    private final double[] burstY = new double[BURSTS];
    private final double[] burstFacing = new double[BURSTS];
    private final int[] burstHow = new int[BURSTS];
    private int nextBurst;
    /** M10 part C: per kind its production frames (null for a kind drawn with the generated crescent). */
    private final Art[] art;
    /** M10 part C: the guns followed (serials, -1 free), when each came into view and its last shot. */
    private final int[] gunSerial = new int[GUNS];

    private final long[] gunSeen = new long[GUNS];
    private final long[] gunShot = new long[GUNS];
    private int nextGun;

    /** M10 part C: a kind's production frames by heading: the plate, its heat mask, its cracks, the pods' glow. */
    record Art(Array<AtlasRegion> plate, Array<AtlasRegion> heat, Array<AtlasRegion> cracks, Array<AtlasRegion> pods) {}

    MirrorLooks(List<EnemySpec> kinds, SoftGlow glow) {
        this(kinds, glow, null);
    }

    /** M10 part C: with {@code sprites} (null: none), whose production frames a kind with them is drawn from. */
    MirrorLooks(List<EnemySpec> kinds, SoftGlow glow, Sprites sprites) {
        this.glow = glow;
        plates = new TextureRegion[kinds.size()];
        cracks = new TextureRegion[kinds.size()];
        art = new Art[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            if (kinds.get(k).mirror().isEmpty()) {
                continue;
            }
            String slug = kinds.get(k).slug();
            if (sprites != null && sprites.has(slug + "-plate")) {
                Array<AtlasRegion> none = new Array<>(0);
                art[k] = new Art(
                        sprites.frames(slug + "-plate"),
                        sprites.has(slug + "-plate-glow") ? sprites.frames(slug + "-plate-glow") : none,
                        sprites.has(slug + "-cracks") ? sprites.frames(slug + "-cracks") : none,
                        sprites.has(slug + "-pods") ? sprites.frames(slug + "-pods") : none);
            }
            double half = kinds.get(k).mirror().get().halfArcRadians();
            TextureRegion[] pair = byArc.computeIfAbsent(
                    half, h -> new TextureRegion[] {region(plateTexture(h, false)), region(plateTexture(h, true))});
            plates[k] = pair[0];
            cracks[k] = pair[1];
        }
        shard = region(shardTexture());
        restart();
    }

    /** Whether any of the level's kinds carries a mirror. */
    static boolean any(List<EnemySpec> kinds) {
        return kinds.stream().anyMatch(kind -> kind.mirror().isPresent());
    }

    private TextureRegion region(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        textures.add(texture);
        return new TextureRegion(texture);
    }

    /** The level restarts: no shard flies, no gun is followed. */
    void restart() {
        java.util.Arrays.fill(burstTick, -1);
        nextBurst = 0;
        java.util.Arrays.fill(gunSerial, -1);
        nextGun = 0;
    }

    /**
     * M10 part C: after each step, the guns' clocks of the mirrored units with production frames: a unit coming into
     * view starts its clock, a shot near its pods ({@code ENEMY_FIRED} within {@value #POD_REACH} px) is its last.
     */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (artOf(enemy) != null && gunSlot(enemy.serial()) < 0) {
                int slot = nextGun;
                nextGun = (nextGun + 1) % GUNS;
                gunSerial[slot] = enemy.serial();
                gunSeen[slot] = tick;
                gunShot[slot] = Long.MIN_VALUE / 2;
            }
        }
        SimEvents events = sortie.events();
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) != SimEvents.Type.ENEMY_FIRED) {
                continue;
            }
            Enemy firer = null;
            double best = POD_REACH;
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (artOf(enemy) == null) {
                    continue;
                }
                double d = Math.hypot(enemy.renderX(1) - events.x(e), enemy.renderY(1) - events.y(e));
                if (d <= best) {
                    best = d;
                    firer = enemy;
                }
            }
            int slot = firer == null ? -1 : gunSlot(firer.serial());
            if (slot >= 0) {
                gunShot[slot] = tick;
            }
        }
    }

    private int gunSlot(int serial) {
        for (int s = 0; s < GUNS; s++) {
            if (gunSerial[s] == serial) {
                return s;
            }
        }
        return -1;
    }

    private Art artOf(Enemy enemy) {
        int kind = enemy.kind();
        return kind < art.length ? art[kind] : null;
    }

    /**
     * M10 part C: the pods' glow (0 to 1) {@code until} steps before a burst predicted over a tell of {@code tellTicks}:
     * rising smoothly to full as the burst comes; none outside the tell or once overdue (the gun holds fire).
     */
    static float podGlow(long until, int tellTicks) {
        if (until <= 0 || until > tellTicks || tellTicks <= 0) {
            return 0;
        }
        double s = 1 - (until - 1) / (double) tellTicks;
        return (float) (s * s * (3 - 2 * s));
    }

    /**
     * M10 part C: steps until the unit's next burst as its gun's clock predicts (a burst's interval counts from its last
     * shot; its first, the first-shot delay after it came into view); {@link Long#MAX_VALUE} without a clock.
     */
    private long untilBurst(Enemy enemy, long tick) {
        int slot = gunSlot(enemy.serial());
        EnemyGun gun = enemy.spec().gun().orElse(null);
        if (slot < 0 || gun == null) {
            return Long.MAX_VALUE;
        }
        long due = gunShot[slot] > Long.MIN_VALUE / 2
                ? gunShot[slot] + SimStep.ticks(gun.intervalSeconds())
                : gunSeen[slot] + SimStep.ticks(gun.firstShotDelay());
        return due - tick;
    }

    /**
     * The heading a unit's sprite is drawn at (radians clockwise from straight down): a mirrored unit's mirror facing
     * (the plate leads, the hull follows it), every other unit's own facing.
     */
    static double heading(Enemy enemy) {
        return enemy.spec().mirror().isPresent() ? enemy.mirror().facing() : enemy.facing();
    }

    /** How far out the plate lies from the centre of a unit whose hit box is {@code width} x {@code height}, px. */
    static double reach(double width, double height) {
        return REACH * Math.max(width, height);
    }

    /** The heat's glow colour at {@code share} of the mirror's heat (0..1): dull red, orange, then white. */
    static Color heatColour(double share, Color out) {
        double s = Math.clamp(share, 0, 1);
        if (s < 0.5) {
            return out.set(DULL_RED).lerp(ORANGE, (float) (s / 0.5));
        }
        return out.set(ORANGE).lerp(WHITE_HOT, (float) ((s - 0.5) / 0.5));
    }

    /** The heat glow's opacity at {@code share} of the heat: none cold, rising smoothly to full at the break. */
    static float heatOpacity(double share) {
        double s = Math.clamp(share, 0, 1);
        return (float) (s * s * (3 - 2 * s));
    }

    /** The cracks' opacity at {@code share} of the heat: none below {@value #CRACKS_FROM}, then 0.4 rising to full. */
    static float crackOpacity(double share) {
        if (share < CRACKS_FROM) {
            return 0;
        }
        return (float) (0.4 + 0.6 * Math.min(1, (share - CRACKS_FROM) / (1 - CRACKS_FROM)));
    }

    /** How far a regrowing mirror has regrown (0 as it starts, 1 standing) with {@code left} of its {@code total} steps. */
    static double regrowShare(int left, int total) {
        return total <= 0 ? 1 : Math.clamp(1 - (double) left / total, 0, 1);
    }

    /**
     * Where along the plate a sweep at {@code share} (0..1) is: the angle from the plate's middle, radians, from one tip
     * ({@code -half}) to the other ({@code half}).
     */
    static double sweepAngle(double share, double half) {
        return -half + 2 * half * Math.clamp(share, 0, 1);
    }

    /** The specular sweep's place at {@code seconds}: 0..1 tip to tip, then over again. */
    static double specular(double seconds) {
        double cycle = seconds / SWEEP_SECONDS;
        return cycle - Math.floor(cycle);
    }

    /**
     * A shard's offset from its break {@code age} steps on (x across, y up): shard {@code k} of {@value #SHARDS} flies
     * out round the plate's normal at {@code facing}, spread over its half circle, slowing as it goes; null-free, into
     * {@code out}.
     */
    static void shardOffset(int k, long age, double facing, double[] out) {
        double t = age * SimStep.SECONDS;
        double spread = Math.PI * ((k + 0.5) / SHARDS - 0.5);
        double angle = facing + spread * 1.1;
        double speed = 70 + 60 * ((k * 7) % SHARDS) / (double) SHARDS;
        double distance = speed * t * (1 - 0.45 * t / SHARD_SECONDS);
        // The plate's normal (-sin, -cos) of the heading, y up.
        out[0] = -Math.sin(angle) * distance;
        out[1] = -Math.cos(angle) * distance;
    }

    /** A shard's opacity {@code age} steps after its break; 0 once it has faded. */
    static float shardOpacity(long age) {
        double s = age * SimStep.SECONDS / SHARD_SECONDS;
        if (s < 0 || s >= 1) {
            return 0;
        }
        return (float) ((1 - s) * (1 - s));
    }

    /**
     * A mirror broke ({@link vanguard.sim.SimEvents.Type#MIRROR_BROKEN} at the unit (x, y), {@code how} 1 for a
     * breaker's or the EMP's shatter, 0 for a beam's heat): its shards fly from its plate, silver or glowing.
     */
    void shatter(Sortie sortie, double x, double y, int how) {
        Enemy unit = mirroredAt(sortie, x, y);
        double facing = unit == null ? 0 : unit.mirror().facing();
        double reach = unit == null
                ? 0
                : artOf(unit) != null
                        ? ART_REACH
                        : reach(unit.hitbox().width(), unit.hitbox().height());
        burstTick[nextBurst] = sortie.tick();
        burstX[nextBurst] = x - Math.sin(facing) * reach;
        burstY[nextBurst] = y - Math.cos(facing) * reach;
        burstFacing[nextBurst] = facing;
        burstHow[nextBurst] = how;
        nextBurst = (nextBurst + 1) % BURSTS;
    }

    private static Enemy mirroredAt(Sortie sortie, double x, double y) {
        Enemy found = null;
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().mirror().isEmpty()) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (d < best && d <= 4) {
                best = d;
                found = enemy;
            }
        }
        return found;
    }

    /**
     * The plates of the mirrored units on one side of the layers ({@code ground}: the ground units; else the flyers),
     * then (with the flyers) the flying shards.
     */
    void draw(SpriteBatch batch, Sortie sortie, float alpha, double seconds, boolean ground) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int kind = enemy.kind();
            if (kind >= plates.length
                    || plates[kind] == null
                    || enemy.cloaked()
                    || (DrawPasses.Depth.of(enemy) == DrawPasses.Depth.GROUND) != ground) {
                continue;
            }
            if (art[kind] != null) {
                drawArt(batch, sortie, enemy, art[kind], alpha, seconds);
            } else {
                drawPlate(batch, enemy, kind, alpha, seconds);
            }
        }
        if (!ground) {
            drawShards(batch, sortie, alpha);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPlate(SpriteBatch batch, Enemy enemy, int kind, float alpha, double seconds) {
        EnemySpec.Mirror spec = enemy.spec().mirror().orElseThrow();
        Mirroring mirror = enemy.mirror();
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        double facing = mirror.facing();
        double reach = reach(enemy.hitbox().width(), enemy.hitbox().height());
        float scale = (float) (reach / PLATE_RADIUS);
        float rotation = (float) -Math.toDegrees(facing);
        if (mirror.down()) {
            batch.setColor(BARE);
            drawPlateTexture(batch, plates[kind], x, y, scale * 0.94f, rotation);
            return;
        }
        double half = spec.halfArcRadians();
        if (mirror.regrowTicks() > 0) {
            double share = regrowShare(mirror.regrowTicks(), SimStep.ticks(spec.regrowSeconds()));
            batch.setColor(BARE);
            drawPlateTexture(batch, plates[kind], x, y, scale * 0.94f, rotation);
            batch.setColor(PLATE.r, PLATE.g, PLATE.b, PLATE.a * (float) share);
            drawPlateTexture(batch, plates[kind], x, y, scale, rotation);
            // The silver shimmer sweeping across from one tip.
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            sweepGlow(batch, x, y, facing, reach, sweepAngle(share, half), 16, 0.9f);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            return;
        }
        batch.setColor(PLATE);
        drawPlateTexture(batch, plates[kind], x, y, scale, rotation);
        double share = mirror.heat() / spec.heat();
        float cracked = crackOpacity(share);
        if (cracked > 0) {
            batch.setColor(0.12f, 0.08f, 0.06f, cracked);
            drawPlateTexture(batch, cracks[kind], x, y, scale, rotation);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        sweepGlow(batch, x, y, facing, reach, sweepAngle(specular(seconds), half), 10, 0.35f);
        float hot = heatOpacity(share);
        if (hot > 0) {
            heatColour(share, colour);
            batch.setColor(colour.r, colour.g, colour.b, hot);
            drawPlateTexture(batch, plates[kind], x, y, scale, rotation);
            batch.setColor(colour.r, colour.g, colour.b, 0.5f * hot);
            glow.disc(batch, x - Math.sin(facing) * reach, y - Math.cos(facing) * reach, reach * 1.6);
            if (cracked > 0) {
                // The cracks glow through a white-hot plate.
                batch.setColor(colour.r, colour.g, colour.b, cracked * hot);
                drawPlateTexture(batch, cracks[kind], x, y, scale, rotation);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * M10 part C: a mirror drawn from its production frames at the heading of its facing: nothing over the hull's bare
     * nose while it is down; regrowing, the plate at the regrown share under the shimmer; standing, the plate, its cracks,
     * the specular sweep, the heat through its glow mask; and the pods' tell before a burst.
     */
    private void drawArt(SpriteBatch batch, Sortie sortie, Enemy enemy, Art frames, float alpha, double seconds) {
        EnemySpec.Mirror spec = enemy.spec().mirror().orElseThrow();
        Mirroring mirror = enemy.mirror();
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        double facing = mirror.facing();
        int heading = EnemyLooks.heading(facing, frames.plate().size);
        double half = spec.halfArcRadians();
        if (!mirror.down()) {
            if (mirror.regrowTicks() > 0) {
                double share = regrowShare(mirror.regrowTicks(), SimStep.ticks(spec.regrowSeconds()));
                batch.setColor(1, 1, 1, (float) share);
                PlayFrame.drawCentred(batch, frames.plate().get(heading), x, y);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                sweepGlow(batch, x, y, facing, ART_REACH, sweepAngle(share, half), 16, 0.9f);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                batch.setColor(Color.WHITE);
                PlayFrame.drawCentred(batch, frames.plate().get(heading), x, y);
                double share = mirror.heat() / spec.heat();
                float cracked = crackOpacity(share);
                boolean cracks = cracked > 0 && frames.cracks().size > heading;
                if (cracks) {
                    batch.setColor(0.12f, 0.08f, 0.06f, cracked);
                    PlayFrame.drawCentred(batch, frames.cracks().get(heading), x, y);
                }
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                sweepGlow(batch, x, y, facing, ART_REACH, sweepAngle(specular(seconds), half), 10, 0.35f);
                float hot = heatOpacity(share);
                if (hot > 0) {
                    heatColour(share, colour);
                    if (frames.heat().size > heading) {
                        batch.setColor(colour.r, colour.g, colour.b, hot);
                        PlayFrame.drawCentred(batch, frames.heat().get(heading), x, y);
                    }
                    batch.setColor(colour.r, colour.g, colour.b, 0.5f * hot);
                    glow.disc(
                            batch, x - Math.sin(facing) * ART_REACH, y - Math.cos(facing) * ART_REACH, ART_REACH * 1.6);
                    if (cracks) {
                        // The cracks glow through a white-hot plate.
                        batch.setColor(colour.r, colour.g, colour.b, cracked * hot);
                        PlayFrame.drawCentred(batch, frames.cracks().get(heading), x, y);
                    }
                }
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
        float pods = frames.pods().size > heading && !enemy.empStunned()
                ? podGlow(untilBurst(enemy, sortie.tick()), SimStep.ticks(POD_TELL_SECONDS))
                : 0;
        if (pods > 0) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(1, 1, 1, pods);
            PlayFrame.drawCentred(batch, frames.pods().get(heading), x, y);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(Color.WHITE);
    }

    /** A soft light {@code size} px across on the plate at {@code angle} from its middle, at {@code opacity}. */
    private void sweepGlow(
            SpriteBatch batch,
            double x,
            double y,
            double facing,
            double reach,
            double angle,
            double size,
            float opacity) {
        double at = facing + angle;
        batch.setColor(1, 1, 1, opacity);
        glow.disc(batch, x - Math.sin(at) * reach, y - Math.cos(at) * reach, size);
    }

    private static void drawPlateTexture(
            SpriteBatch batch, TextureRegion region, double x, double y, float scale, float rotation) {
        float size = PLATE_SIZE;
        batch.draw(
                region,
                (float) (PixelScreen.PLAY_FIELD_X + x - size / 2),
                (float) (y - size / 2),
                size / 2,
                size / 2,
                size,
                size,
                scale,
                scale,
                rotation);
    }

    private final double[] offset = new double[2];

    private void drawShards(SpriteBatch batch, Sortie sortie, float alpha) {
        long now = sortie.tick();
        for (int b = 0; b < BURSTS; b++) {
            if (burstTick[b] < 0) {
                continue;
            }
            long age = now - burstTick[b];
            if (shardOpacity(age) <= 0 && age > 0) {
                burstTick[b] = -1;
                continue;
            }
            boolean hot = burstHow[b] == 0;
            for (int k = 0; k < SHARDS; k++) {
                shardOffset(k, age, burstFacing[b], offset);
                float opacity = shardOpacity(age);
                if (hot) {
                    heatColour(1 - age * SimStep.SECONDS / SHARD_SECONDS * 0.6, colour);
                    batch.setColor(colour.r, colour.g, colour.b, opacity);
                } else {
                    batch.setColor(PLATE.r, PLATE.g, PLATE.b, opacity);
                }
                float w = shard.getRegionWidth();
                float h = shard.getRegionHeight();
                batch.draw(
                        shard,
                        (float) (PixelScreen.PLAY_FIELD_X + burstX[b] + offset[0] - w / 2),
                        (float) (burstY[b] + offset[1] - h / 2),
                        w / 2,
                        h / 2,
                        w,
                        h,
                        1,
                        1,
                        (float) (k * 47 + age * (k % 2 == 0 ? 9 : -11)));
            }
        }
    }

    /**
     * The crescent's alpha at a point {@code d} px from the arc's centre and {@code angle} radians from its middle, for a
     * plate over {@code half} radians each side: a smooth band round {@link #PLATE_RADIUS}, its width tapering to
     * nothing at the tips.
     */
    static float plateAlpha(double d, double angle, double half) {
        double a = Math.abs(angle) / half;
        if (a >= 1) {
            return 0;
        }
        double width = PLATE_HALF_WIDTH * Math.sqrt(1 - a * a);
        double t = Math.abs(d - PLATE_RADIUS) / Math.max(0.5, width);
        if (t >= 1) {
            return 0;
        }
        double band = 1 - t * t;
        return (float) (band * band);
    }

    /** The crack lines' alpha at (dx, dy) px from the texture's centre (y down): a few jagged lines across the band. */
    static float crackAlpha(double dx, double dy, double half) {
        double best = 0;
        for (int c = 0; c < 4; c++) {
            double base = -half * 0.75 + c * half * 0.5;
            // A line from the inner edge to the outer edge of the band, kinked once.
            double inner = PLATE_RADIUS - PLATE_HALF_WIDTH - 1;
            double outer = PLATE_RADIUS + PLATE_HALF_WIDTH + 1;
            double kink = base + (c % 2 == 0 ? 0.06 : -0.07);
            best = Math.max(best, segment(dx, dy, base, inner, kink, PLATE_RADIUS));
            best = Math.max(best, segment(dx, dy, kink, PLATE_RADIUS, base + 0.03, outer));
        }
        double d = Math.hypot(dx, dy);
        double angle = Math.atan2(dx, dy);
        return plateAlpha(d, angle, half) > 0 ? (float) best : 0;
    }

    /** A soft 1 px line's alpha at (dx, dy) from the segment between two polar points (angle from down, radius). */
    private static double segment(double dx, double dy, double a0, double r0, double a1, double r1) {
        double x0 = Math.sin(a0) * r0;
        double y0 = Math.cos(a0) * r0;
        double x1 = Math.sin(a1) * r1;
        double y1 = Math.cos(a1) * r1;
        double vx = x1 - x0;
        double vy = y1 - y0;
        double t = Math.clamp(((dx - x0) * vx + (dy - y0) * vy) / (vx * vx + vy * vy), 0, 1);
        double distance = Math.hypot(dx - x0 - t * vx, dy - y0 - t * vy);
        return Math.max(0, 1 - distance / 0.9);
    }

    /** The plate (or its cracks) for a mirror over {@code half} radians each side, white in float alpha. */
    private static Pixmap plateTexture(double half, boolean cracked) {
        Pixmap pixmap = new Pixmap(PLATE_SIZE, PLATE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        double c = PLATE_SIZE / 2.0;
        for (int py = 0; py < PLATE_SIZE; py++) {
            for (int px = 0; px < PLATE_SIZE; px++) {
                double dx = px + 0.5 - c;
                double dy = py + 0.5 - c;
                float a = cracked ? crackAlpha(dx, dy, half) : plateAlpha(Math.hypot(dx, dy), Math.atan2(dx, dy), half);
                pixmap.drawPixel(px, py, Color.rgba8888(1, 1, 1, a));
            }
        }
        return pixmap;
    }

    /** A shard: a thin white sliver 6 x 10 px with soft edges. */
    private static Pixmap shardTexture() {
        int w = 6;
        int h = 10;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int py = 0; py < h; py++) {
            double width = 2.6 * (1 - (py + 0.5) / h);
            for (int px = 0; px < w; px++) {
                double d = Math.abs(px + 0.5 - w / 2.0);
                float a = (float) Math.clamp(width - d + 0.5, 0, 1);
                pixmap.drawPixel(px, py, Color.rgba8888(1, 1, 1, a));
            }
        }
        return pixmap;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            texture.dispose();
        }
    }
}
