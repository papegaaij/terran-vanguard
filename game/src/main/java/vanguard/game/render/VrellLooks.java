package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.Teleporting;

/**
 * M11 part A, step A3a: the looks of Act 7's unit mechanics on the play field, each family its own class, built with the
 * first sortie from the level's kinds (a family the level's units do not use stays null and costs nothing): the window
 * shells ({@link WindowShellLooks}), the teleports' arrival rings ({@link TeleportLooks}) and the mirroring units' hook
 * (a faint violet ring under a unit while it mirrors the ship, {@link #mirrorGlow}: the look part E's Choir Seraph
 * builds on). The unit pass asks it for the bodies it draws itself ({@link #drawsBody}): a teleporting unit fading
 * ({@link #opacity}) or gone (not drawn), and a unit in its leaving tell, tilting its top toward the edge it leaves by
 * (the top: its height foreshortened to {@value #TILT} and lifted {@value #TILT_LIFT} px, eased in over {@value
 * #TILT_SECONDS} s, {@link #tilt}). The screen's effects take the shell's and the jump's frames from it ({@code
 * ShellEffects}, {@code TeleportEffects}).
 */
public final class VrellLooks implements Disposable {
    /** The leaving tell's foreshortened height (a share of the body's) and lift, px, once eased in. */
    static final double TILT = 0.86;

    static final double TILT_LIFT = 3;
    static final double TILT_SECONDS = 0.5;
    /** The mirroring hook's ring: the event plane's pale violet, faint. */
    static final Color MIRROR = Color.valueOf("C8B4FF");

    static final float MIRROR_ALPHA = 0.25f;
    /** The units whose leaving tell is followed at once. */
    private static final int UNITS = 16;

    static final String SHELL_SPARK = "shell-spark";
    static final String SHELL_BREAK = "shell-break";
    static final String TELEPORT_FADE = "teleport-fade";
    static final String TELEPORT_RING = "teleport-ring";
    static final String TELEPORT_RING_LARGE = "teleport-ring-large";

    private final Sprites sprites;
    private final SoftGlow glow;
    private final SerialClock tells = new SerialClock(UNITS);
    private boolean built;
    private WindowShellLooks shells;
    /** M11 part B, step B4: the Gate Warden's and the Rift Skater's production looks (null without them). */
    private WardenLooks wardens;

    private SkaterLooks skaters;
    private TeleportLooks teleports;
    private boolean mirrors;
    private Array<AtlasRegion> spark;
    private Array<AtlasRegion> shatter;
    private Array<AtlasRegion> fade;

    VrellLooks(Sprites sprites, SoftGlow glow) {
        this.sprites = sprites;
        this.glow = glow;
    }

    /** After each step (the first builds the families the level's kinds need): the leaving tells' starts. */
    void stepped(Sortie sortie) {
        if (!built) {
            build(sortie.enemyKinds());
        }
        tells.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.leavingTell()) {
                tells.touch(enemy.serial(), sortie.tick());
            }
        }
        tells.end();
        if (shells != null) {
            shells.stepped(sortie);
        }
        if (wardens != null) {
            wardens.stepped(sortie);
        }
        if (skaters != null) {
            skaters.stepped(sortie);
        }
    }

    private void build(List<EnemySpec> kinds) {
        built = true;
        shells = WindowShellLooks.any(kinds) ? new WindowShellLooks(kinds, glow) : null;
        wardens = WardenLooks.of(sprites, kinds);
        skaters = SkaterLooks.of(sprites, kinds);
        teleports = TeleportLooks.any(kinds)
                ? new TeleportLooks(frames(TELEPORT_RING), frames(TELEPORT_RING_LARGE), glow)
                : null;
        mirrors = kinds.stream().anyMatch(kind -> kind.shadow().isPresent());
        spark = shells != null ? frames(SHELL_SPARK) : null;
        shatter = shells != null ? frames(SHELL_BREAK) : null;
        fade = teleports != null ? frames(TELEPORT_FADE) : null;
    }

    private Array<AtlasRegion> frames(String name) {
        return sprites != null && sprites.has(name) ? sprites.frames(name) : null;
    }

    /** The level restarts. */
    void restart() {
        tells.clear();
        if (shells != null) {
            shells.restart();
        }
        if (wardens != null) {
            wardens.restart();
        }
        if (skaters != null) {
            skaters.restart();
        }
    }

    /** Over the air units: the shells, the mirroring hook's rings and the arrival rings. */
    void drawAir(SpriteBatch batch, Sortie sortie, float alpha, double seconds) {
        if (shells != null) {
            shells.draw(batch, sortie, alpha, seconds, wardens);
        }
        if (wardens != null) {
            wardens.drawLights(batch, sortie, alpha, this);
        }
        if (skaters != null) {
            skaters.drawLights(batch, sortie, alpha);
        }
        if (mirrors) {
            drawMirrors(batch, sortie, alpha);
        }
        if (teleports != null) {
            teleports.draw(batch, sortie);
        }
    }

    private void drawMirrors(SpriteBatch batch, Sortie sortie, float alpha) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            float share = mirrorGlow(enemy);
            if (share > 0) {
                SoftGlow.tint(batch, MIRROR, share);
                glow.ring(
                        batch,
                        enemy.renderX(alpha),
                        enemy.renderY(alpha),
                        enemy.hitbox().width() * 0.6);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The mirroring hook's ring opacity for a unit: {@link #MIRROR_ALPHA} while it mirrors the ship, else 0. */
    static float mirrorGlow(Enemy enemy) {
        return enemy.spec().shadow().isPresent() && enemy.shadow().mirroring() ? MIRROR_ALPHA : 0;
    }

    /**
     * A unit's body opacity: a teleporting unit's fades out over its fade ({@code 1 - share}) and is 0 while it is gone;
     * 1 for every other unit (its shadow and glows follow it).
     */
    public static float opacity(Enemy enemy) {
        if (enemy.spec().teleport().isEmpty()) {
            return 1;
        }
        Teleporting jumps = enemy.teleport();
        return switch (jumps.phase()) {
            case FADE ->
                (float) Math.clamp(1 - jumps.share(enemy.spec().teleport().get()), 0, 1);
            case GONE -> 0;
            default -> 1;
        };
    }

    /** Whether the unit pass leaves this unit's body to {@link #drawBody}: fading, gone or in its leaving tell. */
    static boolean drawsBody(Enemy enemy) {
        return opacity(enemy) < 1 || enemy.leavingTell();
    }

    /** How far a leaving tell's tilt has eased in {@code ticks} after the tell began, 0..1. */
    static double tilt(long ticks) {
        double t = Math.clamp(ticks / (double) SimStep.ticks(TILT_SECONDS), 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** The tilt of a unit now: its tell's eased share (0 outside its tell). */
    double tilt(Enemy enemy, long tick) {
        return enemy.leavingTell() ? tilt(tick - tells.since(enemy.serial(), tick)) : 0;
    }

    /** A body the unit pass leaves to it ({@link #drawsBody}): faded, or tilted toward its exit, at the render time. */
    void drawBody(SpriteBatch batch, Sortie sortie, Enemy enemy, TextureRegion frame, float alpha) {
        float opacity = opacity(enemy);
        if (opacity <= 0) {
            return;
        }
        double tilt = tilt(enemy, sortie.tick());
        float width = frame.getRegionWidth();
        float height = frame.getRegionHeight();
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha) + TILT_LIFT * tilt;
        batch.setColor(1, 1, 1, opacity);
        batch.draw(
                frame,
                Math.round(PlayFrame.X0 + x - width / 2),
                Math.round(y - height / 2),
                width / 2,
                height / 2,
                width,
                height,
                1,
                (float) (1 - (1 - TILT) * tilt),
                0);
        batch.setColor(Color.WHITE);
    }

    /**
     * M11 part B, step B4: draws a Gate Warden's body on its production frames (the unit pass's {@code flasher} for
     * the hit flash); false for any other unit (or before the first step built the looks).
     */
    boolean drawWarden(SpriteBatch batch, Sortie sortie, Enemy enemy, float alpha, BurrowerLooks.Flasher flasher) {
        if (wardens == null || !wardens.draws(enemy)) {
            return false;
        }
        wardens.drawBody(batch, sortie, enemy, alpha, flasher, this);
        return true;
    }

    /**
     * M11 part B, step B4: whether a unit of {@code kind} breaks up in its own death pieces (the Warden's slabs, the
     * Skater's tatters) instead of leaving a tumbling wreck in vacuum.
     */
    public static boolean breaksUp(EnemySpec kind) {
        return kind.slug().equals(WardenLooks.SLUG) || kind.slug().equals(SkaterLooks.SLUG);
    }

    /** The window shell's hit spark frames ({@code shell-spark}); null without them. */
    public Array<AtlasRegion> shellSpark() {
        return spark;
    }

    /** The window shell's shatter frames ({@code shell-break}); null without them. */
    public Array<AtlasRegion> shellBreak() {
        return shatter;
    }

    /** The jump's fade frames ({@code teleport-fade}); null without them. */
    public Array<AtlasRegion> teleportFade() {
        return fade;
    }

    @Override
    public void dispose() {
        if (shells != null) {
            shells.dispose();
        }
    }
}
