package vanguard.game.render;

import static vanguard.game.render.PlayFrame.HIT_FLASH_TICKS;
import static vanguard.game.render.PlayFrame.HIT_WHITE;
import static vanguard.game.render.PlayFrame.X0;
import static vanguard.game.render.PlayFrame.drawCentred;
import static vanguard.game.render.PlayFrame.drawScaled;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;
import vanguard.sim.Crane;
import vanguard.sim.Debris;
import vanguard.sim.EnemyBullet;
import vanguard.sim.LevelScript;
import vanguard.sim.Mine;
import vanguard.sim.Pickup;
import vanguard.sim.PickupType;
import vanguard.sim.SimStep;
import vanguard.sim.Sondes;
import vanguard.sim.Sortie;

/**
 * The play plane's props for {@link LevelRenderer}'s passes: the debris chunks and thrown rocks, the
 * cranes, the pickups, the weather sondes, the spore mines and the enemy bullets.
 */
final class PropPasses {
    /** Pickups spin at about 10 fps and blink in their last 1.5 s (chosen pickups concept, round 09). */
    private static final int PICKUP_FRAME_TICKS = 6;

    private static final int BLINK_FRAME_TICKS = 10;

    /** A thrown rock's tumble frames per shape, and the steps each shows. */
    private static final int ROCK_TUMBLE = 4;

    private static final int ROCK_TUMBLE_TICKS = 6;

    private static final int BLINK_TICKS = SimStep.ticks(1.5);

    /** A spore mine pulses at 10 fps; while it rises it grows from 70 % and brightens from half. */
    private static final int MINE_FRAME_TICKS = 6;

    private static final float MINE_RISING_SCALE = 0.7f;

    private static final float MINE_RISING_ALPHA = 0.5f;

    /** The sonde's blink: 4 frames at 6 fps. */
    private static final double SONDE_FPS = 6;

    static final String SONDE = "weather-sonde";

    /** The debris chunks' sprites by name, looked up once. */
    private final Map<String, AtlasRegion> debrisSprites = new HashMap<>();

    private final Array<AtlasRegion> mine;

    /** The thrown rocks' looks: three shapes of {@value #ROCK_TUMBLE} tumble frames each; null without them. */
    private final Array<AtlasRegion> rocks;

    /** M5 part F: the weather sonde's blink loop (tools/art/l12_props.py); empty without sondes. */
    private final Array<AtlasRegion> sonde;

    private final CraneLooks craneLooks;

    /** The enemy bullets' sprites by their class. */
    private final BulletLooks bullets;

    private final Sprites sprites;
    private final FlashShader flash;
    private final PlayFrame play;
    private final BackdropClock clock;

    PropPasses(
            Sprites sprites,
            FlashShader flash,
            PlayFrame play,
            BackdropClock clock,
            CraneLooks craneLooks,
            LevelScript script) {
        this.sprites = sprites;
        this.flash = flash;
        this.play = play;
        this.clock = clock;
        this.craneLooks = craneLooks;
        mine = sprites.has("spore-mine") ? sprites.frames("spore-mine") : null;
        rocks = sprites.has("rock") ? sprites.frames("rock") : null;
        bullets = new BulletLooks(sprites);
        sonde = script.sondes().isPresent() && sprites.has(SONDE) ? sprites.frames(SONDE) : new Array<>(0);
    }

    /**
     * The debris chunks on the play plane, unrotated; a hit on a small, breakable chunk flashes it
     * white. A large chunk only shows the glancing spark: it takes no damage, and with the guns on
     * it a white flash per hit would strobe.
     */
    void drawDebris(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.debrisCount(); i++) {
            Debris chunk = sortie.debris(i);
            if (chunk.thrown()) {
                drawRock(batch, chunk, alpha);
                continue;
            }
            AtlasRegion sprite = debrisSprites.get(chunk.sprite());
            if (sprite == null) {
                sprite = sprites.region(chunk.sprite());
                debrisSprites.put(chunk.sprite(), sprite);
            }
            if (!chunk.large() && chunk.ticksSinceHit() < HIT_FLASH_TICKS) {
                flash.draw(
                        batch,
                        sprite,
                        Math.round(X0 + chunk.renderX(alpha)),
                        Math.round(chunk.renderY(alpha)),
                        HIT_WHITE,
                        play.whiteFlash);
            } else {
                drawCentred(batch, sprite, chunk.renderX(alpha), chunk.renderY(alpha));
            }
        }
    }

    /**
     * A thrown rock (Level 05): one of the three shapes (by its serial), tumbling, scaled to the
     * rock's size, growing as it rises in low gravity and fading out in its last half second.
     */
    private void drawRock(SpriteBatch batch, Debris chunk, float alpha) {
        if (rocks == null) {
            return;
        }
        int shape = Math.floorMod(chunk.serial(), rocks.size / ROCK_TUMBLE);
        int tumble = Math.floorMod(chunk.life() / ROCK_TUMBLE_TICKS + chunk.serial(), ROCK_TUMBLE);
        AtlasRegion rock = rocks.get(shape * ROCK_TUMBLE + tumble);
        double size = chunk.spec().size().width();
        double rise = 1 - Math.max(0, chunk.life() - alpha) / (double) SimStep.ticks(2.5);
        float scale = (float) (size / rock.getRegionWidth() * (1 + 0.3 * Math.sin(Math.PI * Math.clamp(rise, 0, 1))));
        float fade = (float) Math.min(1, chunk.life() / (double) SimStep.ticks(0.5));
        batch.setColor(1, 1, 1, fade);
        drawScaled(batch, rock, chunk.renderX(alpha), chunk.renderY(alpha), scale);
        batch.setColor(Color.WHITE);
    }

    /**
     * The spore mines, additive and above the haze like bullets, each pulsing at its own phase:
     * while one rises to the play plane it grows from 70 % and brightens from half, armed it is full.
     */
    void drawMines(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sortie.mineCount() == 0) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.mineCount(); i++) {
            Mine spore = sortie.mine(i);
            AtlasRegion frame = mine.get((int) ((sortie.tick() / MINE_FRAME_TICKS + i) % mine.size));
            double x = spore.renderX(alpha);
            double y = spore.renderY(alpha);
            if (spore.armed()) {
                drawCentred(batch, frame, x, y);
            } else {
                float rising = (float) spore.rising();
                batch.setColor(1, 1, 1, MINE_RISING_ALPHA + (1 - MINE_RISING_ALPHA) * rising);
                drawScaled(batch, frame, x, y, MINE_RISING_SCALE + (1 - MINE_RISING_SCALE) * rising);
                batch.setColor(Color.WHITE);
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * The cranes' arms at the nearest drawn angle, hanging from their pivots, with the canister on
     * the hook while it holds; the jib's lights blink in the telegraph and the clamp lights while the
     * arm swings.
     */
    void drawCranes(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int c = 0; c < sortie.craneCount(); c++) {
            Crane crane = sortie.crane(c);
            if (!crane.present()) {
                continue;
            }
            LevelScript.CraneSpec spec = crane.spec();
            double angle = crane.renderAngle(alpha);
            int k = craneLooks.index(angle);
            AtlasRegion arm = craneLooks.arm(k);
            int[] pivot = craneLooks.pivot(k);
            batch.draw(
                    arm,
                    Math.round(X0 + spec.pivotX() - pivot[0]),
                    Math.round(spec.pivotY() + pivot[1] - arm.getRegionHeight()));
            double drawn = craneLooks.angle(k);
            double dx = Math.sin(drawn);
            double dy = -Math.cos(drawn);
            double tipX = spec.pivotX() + dx * spec.length();
            double tipY = spec.pivotY() + dy * spec.length();
            if (crane.holding()) {
                drawCentred(batch, craneLooks.canister, tipX, tipY - craneLooks.canister.getRegionHeight() / 2.0);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            if (crane.telegraph() && sortie.tick() / BLINK_FRAME_TICKS % 2 == 0) {
                for (int light : craneLooks.lights()) {
                    drawCentred(batch, craneLooks.light, spec.pivotX() + dx * light, spec.pivotY() + dy * light);
                }
            }
            if (crane.swinging() && crane.holding()) {
                drawCentred(batch, craneLooks.clampLight, tipX, tipY - 4);
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    void drawPickups(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.pickupCount(); i++) {
            Pickup pickup = sortie.pickup(i);
            if (pickup.ticksLeft() < BLINK_TICKS && pickup.ticksLeft() / 6 % 2 == 0) {
                continue;
            }
            Array<AtlasRegion> frames = pickupFrames(pickup.type());
            int frame = (int) ((sortie.tick() / PICKUP_FRAME_TICKS + i) % frames.size);
            drawCentred(batch, frames.get(frame), pickup.renderX(alpha), pickup.renderY(alpha));
        }
    }

    private Array<AtlasRegion> pickupFrames(PickupType type) {
        return switch (type) {
            case SMALL_SALVAGE -> sprites.salvageSmall;
            case MEDIUM_SALVAGE -> sprites.frames("pickup-salvage-medium");
            case OVERDRIVE -> sprites.frames("pickup-overdrive");
            case LARGE_SALVAGE -> sprites.frames("pickup-salvage-large");
            case HIDDEN_CRATE -> sprites.crate;
            case SHIELD_CELL -> sprites.shieldCell;
            case ARMOUR_PATCH -> sprites.armourPatch;
            // The crate stands in until the special charge's own pickup sprite is made.
            case SPECIAL_CHARGE -> sprites.crate;
            // The amber orb of round 23 b (tools/art/l06_props.py).
            case DATA_CORE -> sprites.frames("pickup-data-core");
        };
    }

    /** M5 part F: the weather sondes drifting down the play plane, blinking (collected ones pop, LevelScreen). */
    void drawSondes(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sonde.isEmpty()) {
            return;
        }
        Sondes sondes = sortie.sondes();
        int frame = (int) Math.floorMod((long) Math.floor(clock.real() * SONDE_FPS), (long) sonde.size);
        for (int i = 0; i < sondes.count(); i++) {
            if (sondes.state(i) == Sondes.DRIFTING) {
                drawCentred(batch, sonde.get((frame + i) % sonde.size), sondes.x(i), sondes.renderY(i, alpha));
            }
        }
    }

    void drawBullets(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.bulletCount(); i++) {
            EnemyBullet bullet = sortie.bullet(i);
            drawCentred(batch, bullets.frame(bullet, i, sortie.tick()), bullet.renderX(alpha), bullet.renderY(alpha));
        }
    }
}
