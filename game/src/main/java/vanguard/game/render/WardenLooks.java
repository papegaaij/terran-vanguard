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
import vanguard.sim.LineBeam;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;
import vanguard.sim.WindowShell;

/**
 * M11 part B, step B4 (design/enemies/space/gate-warden, Look; round 84 A.1 a "idol"): the Gate Warden on ART-B's
 * production frames (tools/art/gate_units.py, whose docstring gives the sprites), in place of A3a's code-drawn shell
 * ({@link WindowShellLooks}, which stays for a shelled unit without frames of its own). The body by its shell's state:
 *
 * <ul>
 *   <li>the six-petal iris ({@code gate-warden_0..5}: closed to open) by the shell's iris share, and over the closed
 *       egg its slow specular sweep ({@code -sweep}, additive, one pass in {@value #SWEEP_SECONDS} s, a phase per unit);
 *   <li>broken by a breaker hit or the EMP the bare body ({@code -bare}; the shatter is the screen's {@code
 *       shell-break}), then the petals growing back over it in the regrowth ({@code -regrow}, over the bare body);
 *   <li>in its leaving tell the closed Warden tilting its top toward the top edge ({@code -tilt_0..3}, eased in over
 *       {@link VrellLooks#TILT_SECONDS} s);
 *   <li>the glyph band and the eye's light ({@code -glyphs}, or the tilt's {@code -tilt-glyphs}; additive) at {@link
 *       WindowShellLooks#glyphLevel}'s strength, dim behind the closed shell, burning open, dimmed and flickering in the
 *       tell ({@link WindowShellLooks#flicker}); the eye's telegraph charge ({@code -eye_0..7}, additive, frame 7 held
 *       through the beam), its pupil {@value #PUPIL} px toward the locked line;
 *   <li>its death: the three slabs falling apart ({@code -slabs}, the unit's death pieces: {@link EnemyLooks}), the
 *       shell's shatter at the death (the screen's), no tumbling wreck ({@link VrellLooks#breaksUp}).
 * </ul>
 *
 * Nothing is allocated per frame.
 */
final class WardenLooks {
    static final String SLUG = "gate-warden";
    /** The closed shell's sweep runs over it once in this many seconds (12 frames). */
    static final double SWEEP_SECONDS = 2.5;
    /** The eye's pupil turns this far toward the locked line, px. */
    static final double PUPIL = 2.5;
    /** The units whose regrowth is followed at once. */
    private static final int UNITS = 16;

    private final boolean[] wardens;
    private final Array<AtlasRegion> body;
    private final AtlasRegion bare;
    private final Array<AtlasRegion> regrow;
    private final Array<AtlasRegion> sweep;
    private final AtlasRegion glyphs;
    private final Array<AtlasRegion> eye;
    private final Array<AtlasRegion> tilt;
    private final Array<AtlasRegion> tiltGlyphs;
    private final SerialClock regrowths = new SerialClock(UNITS);

    private WardenLooks(Sprites sprites, List<EnemySpec> kinds) {
        wardens = new boolean[kinds.size()];
        for (int k = 0; k < wardens.length; k++) {
            wardens[k] = kinds.get(k).slug().equals(SLUG)
                    && kinds.get(k).windowShield().isPresent();
        }
        body = sprites.frames(SLUG);
        bare = sprites.region(SLUG + "-bare");
        regrow = sprites.frames(SLUG + "-regrow");
        sweep = sprites.frames(SLUG + "-sweep");
        glyphs = sprites.region(SLUG + "-glyphs");
        eye = sprites.frames(SLUG + "-eye");
        tilt = sprites.frames(SLUG + "-tilt");
        tiltGlyphs = sprites.frames(SLUG + "-tilt-glyphs");
    }

    /** The Warden's looks for a level whose kinds hold it and whose atlas has its frames; null otherwise. */
    static WardenLooks of(Sprites sprites, List<EnemySpec> kinds) {
        boolean any = kinds.stream().anyMatch(kind -> kind.slug().equals(SLUG));
        if (!any || sprites == null || !sprites.has(SLUG) || !sprites.has(SLUG + "-tilt-glyphs")) {
            return null;
        }
        return new WardenLooks(sprites, kinds);
    }

    /** Whether it draws this unit (a Warden): its shell then is not the code-drawn one. */
    boolean draws(Enemy enemy) {
        return enemy.kind() < wardens.length && wardens[enemy.kind()];
    }

    /** Whether it draws units of {@code kind}. */
    boolean drawsKind(int kind) {
        return kind >= 0 && kind < wardens.length && wardens[kind];
    }

    /** After each step: the regrowing shells' starts. */
    void stepped(Sortie sortie) {
        long tick = sortie.tick();
        regrowths.begin();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (draws(enemy) && enemy.window().regrowing()) {
                regrowths.touch(enemy.serial(), tick);
            }
        }
        regrowths.end();
    }

    /** The level restarts. */
    void restart() {
        regrowths.clear();
    }

    /** The iris frame at the shell's iris share (0 closed .. 1 open): one of {@code frames}. */
    static int irisFrame(double iris, int frames) {
        return (int) Math.round(Math.clamp(iris, 0, 1) * (frames - 1));
    }

    /** The tilt frame at the tell's eased share (0 upright: -1, then 0..frames-1 as it leans further). */
    static int tiltFrame(double share, int frames) {
        if (share <= 0) {
            return -1;
        }
        return Math.min(frames - 1, (int) Math.ceil(share * frames) - 1);
    }

    /** The regrowth's frame {@code ticks} into its {@code total} steps. */
    static int regrowFrame(long ticks, int total, int frames) {
        return Math.min(frames - 1, (int) (WindowShellLooks.regrowShare(ticks, total) * frames));
    }

    /** The eye's frame at the telegraph's charge (0..1; 1 held through the beam). */
    static int eyeFrame(double charge, int frames) {
        return Math.min(frames - 1, (int) Math.floor(Math.clamp(charge, 0, 1) * (frames - 1) + 1e-9));
    }

    /** The sweep's frame on step {@code tick} for unit {@code serial} (its own phase). */
    static int sweepFrame(long tick, int serial, int frames) {
        double stepsPerFrame = SWEEP_SECONDS * SimStep.PER_SECOND / frames;
        return (int) Math.floorMod((long) Math.floor(tick / stepsPerFrame) + serial * 5L, (long) frames);
    }

    /** The body (normal blend), drawn by the unit pass with its hit flash. */
    void drawBody(
            SpriteBatch batch,
            Sortie sortie,
            Enemy enemy,
            float alpha,
            BurrowerLooks.Flasher flasher,
            VrellLooks vrell) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        WindowShell shell = enemy.window();
        long now = sortie.tick();
        if (shell.held()) {
            flasher.draw(batch, enemy, bare, x, y);
            return;
        }
        if (shell.regrowing()) {
            flasher.draw(batch, enemy, bare, x, y);
            EnemySpec.WindowShield spec = enemy.spec().windowShield().orElseThrow();
            long into = now - regrowths.since(enemy.serial(), now);
            drawCentred(batch, regrow.get(regrowFrame(into, SimStep.ticks(spec.regrowSeconds()), regrow.size)), x, y);
            return;
        }
        int lean = tiltFrame(vrell.tilt(enemy, now), tilt.size);
        if (lean >= 0 && shell.iris() < 0.5) {
            flasher.draw(batch, enemy, tilt.get(lean), x, y);
            return;
        }
        flasher.draw(batch, enemy, body.get(irisFrame(shell.iris(), body.size)), x, y);
    }

    /** Over the air units (additive, the batch's blend set here): each Warden's sweep, glyph light and eye. */
    void drawLights(SpriteBatch batch, Sortie sortie, float alpha, VrellLooks vrell) {
        long now = sortie.tick();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!draws(enemy)) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            WindowShell shell = enemy.window();
            boolean bareBody = shell.held() || shell.regrowing();
            double iris = bareBody ? 1 : shell.iris();
            boolean leaving = enemy.leavingTell();
            float light = (float) (WindowShellLooks.glyphLevel(iris, leaving)
                    * WindowShellLooks.flicker(leaving, now, enemy.serial())
                    * ArsenalLooks.glowShare(enemy));
            int lean = bareBody ? -1 : tiltFrame(vrell.tilt(enemy, now), tilt.size);
            boolean tilted = lean >= 0 && shell.iris() < 0.5;
            if (light > 0) {
                batch.setColor(1, 1, 1, Math.min(1, light));
                drawCentred(batch, tilted ? tiltGlyphs.get(lean) : glyphs, x, y);
            }
            if (!bareBody && !tilted && shell.iris() <= 0) {
                batch.setColor(Color.WHITE);
                drawCentred(batch, sweep.get(sweepFrame(now, enemy.serial(), sweep.size)), x, y);
            }
            LineBeam line = enemy.lineBeam();
            if (line != null && line.running() && enemy.spec().laserLine().isPresent()) {
                double charge = line.charge(enemy.spec().laserLine().get());
                batch.setColor(Color.WHITE);
                drawCentred(
                        batch, eye.get(eyeFrame(charge, eye.size)), x + PUPIL * line.dirX(), y + PUPIL * line.dirY());
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
