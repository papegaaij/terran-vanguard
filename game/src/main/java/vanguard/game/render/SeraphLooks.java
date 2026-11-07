package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part E, step E4 (design/enemies/air/choir-seraph, Look; round 84 A.4 a "blades"; tools/art/act7_sphere.py {@code
 * seraph}): the Choir Seraph on its production frames, by its cycle's phase ({@link Teleporting}):
 *
 * <ul>
 *   <li>gliding level ({@code choir-seraph_0..3}, {@value #GLIDE_FPS} fps, each unit at its own phase) on its path and
 *       while it mirrors the ship slowly; mirroring faster, banked with its sideways speed ({@code -bank_0..6}: 3 level,
 *       0..2 moving toward the screen's left 30/20/10°, 4..6 right 10/20/30°, {@link #bankFrame});
 *   <li>its fold before a jump ({@code -fold_0..5} over the 0.3 s fold by how far it has run), the folded frame held
 *       through the built 0.2 s fade (the unit's opacity, {@link VrellLooks#opacity});
 *   <li>its leaving tell ({@code -leave_0..11} over the 0.8 s tell, the wings flaring white; the 40 px rise is the
 *       simulation's), the last frame held through its last fade; no tilt (the Warden's tilt is its station's exit);
 * </ul>
 *
 * <p>Over the air units, additive ({@link #drawLights}): the heart's and the leading edges' soft light over any body
 * frame while it shows ({@code -glow}, faded with the body, dimmed under the EMP: {@link ArsenalLooks#glowShare}); the
 * spiral's tell after each landing, a violet flare at the wing roots ({@code -flare_0..5}, {@value #FLARE_FRAME_TICKS}
 * steps a frame: 0.4 s); the leaving flare's white-gold light ({@code -leave-glow_0..11}, with the tell's frames, then
 * fading with the body). The arrival ring is {@link TeleportLooks}' large ring; the death its {@code -tatters} and
 * {@code -death} ({@link EnemyLooks}, {@link VrellLooks#breaksUp}); its spiral's orb {@link BulletLooks}' own look
 * ({@code -shot}). Nothing is allocated per frame.
 */
final class SeraphLooks {
    static final String SLUG = "choir-seraph";

    static final String BANK = "-bank";
    static final String FOLD = "-fold";
    static final String LEAVE = "-leave";
    static final String LEAVE_GLOW = "-leave-glow";
    static final String GLOW = "-glow";
    static final String FLARE = "-flare";
    static final int GLIDE_FPS = 10;
    /** The bank frames' level one (3) and their steps each way (30, 20, 10°). */
    static final int LEVEL = 3;
    /** The sideways speed of the full 30° bank, px/s (the mirror's cap). */
    static final double FULL_BANK_SPEED = 260;
    /** The spiral's tell: steps a frame after a landing (6 frames: 0.4 s). */
    static final int FLARE_FRAME_TICKS = 4;
    /** The units whose landing is followed at once. */
    private static final int UNITS = 16;

    private final boolean[] seraphs;
    private final Array<AtlasRegion> glide;
    private final Array<AtlasRegion> bank;
    private final Array<AtlasRegion> fold;
    private final Array<AtlasRegion> leave;
    private final Array<AtlasRegion> leaveGlow;
    private final AtlasRegion glow;
    private final Array<AtlasRegion> flare;
    private final SerialClock landings = new SerialClock(UNITS);

    private SeraphLooks(Sprites sprites, List<EnemySpec> kinds) {
        seraphs = new boolean[kinds.size()];
        for (int k = 0; k < seraphs.length; k++) {
            seraphs[k] = seraph(kinds.get(k));
        }
        glide = sprites.frames(SLUG);
        bank = sprites.frames(SLUG + BANK);
        fold = sprites.frames(SLUG + FOLD);
        leave = sprites.frames(SLUG + LEAVE);
        leaveGlow = sprites.frames(SLUG + LEAVE_GLOW);
        glow = sprites.region(SLUG + GLOW);
        flare = sprites.frames(SLUG + FLARE);
    }

    private static boolean seraph(EnemySpec kind) {
        return kind.slug().equals(SLUG) && kind.teleport().isPresent();
    }

    /** The Seraph's looks for a level whose kinds hold it and whose atlas has its frames; null otherwise. */
    static SeraphLooks of(Sprites sprites, List<EnemySpec> kinds) {
        if (sprites == null || kinds.stream().noneMatch(SeraphLooks::seraph)) {
            return null;
        }
        for (String set : List.of("", BANK, FOLD, LEAVE, LEAVE_GLOW, GLOW, FLARE)) {
            if (!sprites.has(SLUG + set)) {
                return null;
            }
        }
        return new SeraphLooks(sprites, kinds);
    }

    /** Whether {@code enemy} is a Seraph drawn here. */
    boolean draws(Enemy enemy) {
        return enemy.kind() >= 0 && enemy.kind() < seraphs.length && seraphs[enemy.kind()];
    }

    /**
     * Whether a Seraph has landed and mirrors after a landing (the flare's clock runs from then): it enters by its ring,
     * so it glides only after a landing.
     */
    static boolean landed(Enemy enemy) {
        return enemy.teleport().phase() == Teleporting.Phase.GLIDE;
    }

    /** After each step: the landings' starts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        landings.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (draws(enemy) && landed(enemy)) {
                landings.touch(enemy.serial(), tick);
            }
        }
        landings.end();
    }

    /** The level restarts. */
    void restart() {
        landings.clear();
    }

    /** The frame of {@code frames} a tell shows at {@code share} (0 to 1) of its time. */
    static int shareFrame(double share, int frames) {
        return Math.clamp((int) Math.floor(share * frames), 0, frames - 1);
    }

    /**
     * The bank frame (0..6) at a sideways speed of {@code vx} px/s: {@value #LEVEL} level, toward 0 moving left (30° at
     * the full speed), toward 6 moving right; the steps at a sixth, a half and five sixths of {@link #FULL_BANK_SPEED}.
     */
    static int bankFrame(double vx) {
        int steps = (int) Math.min(LEVEL, Math.round(Math.abs(vx) / FULL_BANK_SPEED * LEVEL));
        return vx < 0 ? LEVEL - steps : LEVEL + steps;
    }

    /** A unit's sideways speed at its last step, px/s. */
    static double sideways(Enemy enemy) {
        return (enemy.renderX(1) - enemy.renderX(0)) * SimStep.PER_SECOND;
    }

    /** The glide frame at step {@code tick} for unit {@code serial} ({@value #GLIDE_FPS} fps, its own phase). */
    static int glideFrame(long tick, int serial, int frames) {
        return (int) Math.floorMod(tick * GLIDE_FPS / SimStep.PER_SECOND + serial, (long) frames);
    }

    /** A Seraph's body frame now (the unit pass fades it with {@link VrellLooks#opacity}); null for another unit. */
    AtlasRegion frame(Enemy enemy, long tick) {
        if (!draws(enemy)) {
            return null;
        }
        Teleporting jumps = enemy.teleport();
        EnemySpec.Teleport spec = enemy.spec().teleport().get();
        return switch (jumps.phase()) {
            case FOLD -> fold.get(shareFrame(jumps.share(spec), fold.size));
            case LEAVE_FOLD -> leave.get(shareFrame(jumps.share(spec), leave.size));
            case FADE -> jumps.leavingNow() ? leave.peek() : fold.peek();
            case GLIDE -> {
                int frame = enemy.shadow().mirroring() ? bankFrame(sideways(enemy)) : LEVEL;
                yield frame == LEVEL
                        ? glide.get(glideFrame(tick, enemy.serial(), glide.size))
                        : bank.get(Math.min(frame, bank.size - 1));
            }
            default -> glide.get(glideFrame(tick, enemy.serial(), glide.size));
        };
    }

    /** The spiral tell's flare frame {@code ticks} after a landing; -1 outside it. */
    static int flareFrame(long ticks, int frames) {
        if (ticks < 0 || ticks >= (long) frames * FLARE_FRAME_TICKS) {
            return -1;
        }
        return (int) (ticks / FLARE_FRAME_TICKS);
    }

    /** Over the air units (additive): each Seraph's light, its spiral's tell after a landing, its leaving flare. */
    void drawLights(SpriteBatch batch, Sortie sortie, float alpha) {
        long now = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            float body = VrellLooks.opacity(enemy);
            float light = body * ArsenalLooks.glowShare(enemy);
            if (light <= 0) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            batch.setColor(1, 1, 1, light);
            drawCentred(batch, glow, x, y);
            Teleporting jumps = enemy.teleport();
            if (jumps.leavingNow()) {
                int frame = jumps.phase() == Teleporting.Phase.LEAVE_FOLD
                        ? shareFrame(jumps.share(enemy.spec().teleport().get()), leaveGlow.size)
                        : leaveGlow.size - 1;
                drawCentred(batch, leaveGlow.get(frame), x, y);
            } else if (landed(enemy)) {
                int frame = flareFrame(now - landings.since(enemy.serial(), now), flare.size);
                if (frame >= 0) {
                    drawCentred(batch, flare.get(frame), x, y);
                }
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
