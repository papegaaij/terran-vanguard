package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.List;
import java.util.Optional;
import vanguard.content.LevelData;
import vanguard.content.SectionsBlock.Light;
import vanguard.content.SectionsBlock.UnderwaterLook;
import vanguard.sim.Bulwark;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.FurnaceVents;
import vanguard.sim.Hitbox;
import vanguard.sim.PlayField;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;
import vanguard.sim.Terrain;
import vanguard.sim.Wingman;

/**
 * M8 part F (design/campaign Level 26, Darkness; europa's Darkness (level 26); m8f-gaps.md §1.4, §5 F3, F4, F7, F13): the
 * dark of a level whose under-water look carries part F's keys (its static {@code lights} or its {@code escort_lamp}),
 * drawn into {@link UnderwaterGrade}'s light map beside step LX's headlights, ambient and rim (a look only: nothing in
 * the simulation knows it). Into the light map, each frame, without allocation:
 *
 * <ul>
 *   <li>the static {@code lights} (the lamp posts, the beacons, the farm barge's lamp, the survey buoy), each a pool of
 *       its colour and radius on its terrain row, scrolling with the ground;
 *   <li>every unit's <b>rim</b>: a soft flat pool over its body and a faint halo at the look's {@code rim} (0.15), so a
 *       unit outside the light reads faintly beside its glow;
 *   <li>a kind's <b>glow pool</b> ({@link #GLOWS}): the light its glowing parts throw on its body (the Driftjelly's
 *       veins, the Siren's sac, the Glow Angler's lure and eyes, the Spiral Nautilus's crown, the Eel Swarm's stripe);
 *   <li>the <b>Sonar Pulse</b>: every unit whole (the rim pool at full) for its window, fading over its last {@value
 *       #SONAR_FADE} s; the backdrop stays dark;
 *   <li>the escort's <b>lamp</b> ({@code escort_lamp}, px): a warm pool round Rook (not ejected) or the Bulwark, so the
 *       player always sees him (it stands in for LX's pool round Rook).
 * </ul>
 *
 * Ungraded, after the grade ({@link #drawHalos}): each pickup's warm gold halo (a pickup is never teal). The marine snow
 * is lit only inside the headlight cones ({@link UnderwaterGrade#coneLight}). The settings' <b>Dark levels assist</b>
 * lifts the ambient to {@value #ASSIST_AMBIENT} ({@link UnderwaterGrade#ambientFloor}).
 */
final class DarkLight implements Disposable {
    /** The Dark levels assist's ambient (design/campaign Level 26, Darkness: 20 %). */
    static final float ASSIST_AMBIENT = 0.20f;
    /** A unit's rim and sonar pool: its flat middle covers this share of its radius; the radius per body half-size. */
    static final double PLATEAU = 0.6;

    static final double RIM_REACH = 1.45;
    /** A static light's strength (of the light missing to full there). */
    static final float LIGHT = 0.9f;
    /** A kind's glow pool's strength. */
    static final float GLOW_LIGHT = 0.5f;
    /** The escort's lamp's strength and colour (a warm white). */
    static final float LAMP_LIGHT = 0.85f;

    private static final Color LAMP = new Color(1f, 0.92f, 0.78f, 1);
    /** The Sonar Pulse's whole-unit light fades over the window's last this many s. */
    static final double SONAR_FADE = 0.5;
    /** A pickup's gold halo: its diameter px, its opacity and its slow pulse's swing and rate. */
    static final double HALO_DIAMETER = 46;

    static final float HALO_ALPHA = 0.5f;
    static final float HALO_SWING = 0.12f;
    static final double HALO_HZ = 1.1;

    private static final Color GOLD = new Color(1f, 0.78f, 0.32f, 1);

    /**
     * A kind's glow pool: the light its glowing part throws on its body. {@code at}: {@code CENTRE} the unit's centre,
     * {@code SAC} the Siren's sac, {@code LURE} the Angler's bead (with a weaker crimson pool at its eyes).
     */
    record Glow(String slug, boolean prefix, float radius, float r, float g, float b, int at) {}

    static final int CENTRE = 0;
    static final int SAC = 1;
    static final int LURE = 2;
    /** The glow pools (radius 24-40 px; their colours the art's glow frames'). */
    static final List<Glow> GLOWS = List.of(
            new Glow("driftjelly", false, 28, 0.6f, 1f, 0.2f, CENTRE),
            new Glow(SirenLooks.SLUG, false, 32, 0.1f, 1f, 0.65f, SAC),
            new Glow(AnglerLooks.SLUG, false, 24, 0.3f, 1f, 0.8f, LURE),
            new Glow(NautilusLooks.SLUG, false, 36, 0.65f, 0.4f, 1f, CENTRE),
            new Glow(EelLooks.SLUG, true, 24, 0.6f, 0.35f, 1f, CENTRE));
    /** The Angler's eyes' pool: radius px, strength, colour. */
    static final float EYES_RADIUS = 16;

    static final float EYES_LIGHT = 0.35f;
    private static final Color EYES = new Color(1f, 0.25f, 0.3f, 1);

    private final KindLooks kinds;
    private final SoftGlow softGlow;
    /** The static lights: terrain row, x, radius, colour. */
    private final double[] lightRow;

    private final double[] lightX;
    private final float[] lightRadius;
    private final float[][] lightColour;
    /** The escort's lamp's radius, px; 0 without one. */
    private final float escortLamp;

    private final Texture plateauTexture;
    private final TextureRegion plateau;
    /** M8 part G: whether the level's vents are heat plumes, whose mouths throw an amber pool by their phase. */
    private final boolean plumes;
    /** Rook's loss's looks: his lamp's flicker and place as he spins down; null before {@link #rookLoss}. */
    private RookLossLooks rookLoss;
    /** Per enemy kind its glow pool's index in {@link #GLOWS} (-1 none), built at the first draw. */
    private int[] glowOf;

    private DarkLight(LevelData level, KindLooks kinds, SoftGlow softGlow) {
        this.kinds = kinds;
        this.softGlow = softGlow;
        UnderwaterLook look = darkLook(level).orElseThrow();
        List<Light> lights = look.lightList();
        lightRow = new double[lights.size()];
        lightX = new double[lights.size()];
        lightRadius = new float[lights.size()];
        lightColour = new float[lights.size()][3];
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            lightRow[i] = light.scroll();
            lightX[i] = light.x();
            lightRadius[i] = (float) light.radius();
            int rgb = Integer.parseInt(light.colour(), 16);
            lightColour[i][0] = ((rgb >> 16) & 0xff) / 255f;
            lightColour[i][1] = ((rgb >> 8) & 0xff) / 255f;
            lightColour[i][2] = (rgb & 0xff) / 255f;
        }
        escortLamp = look.escortLamp().map(Double::floatValue).orElse(0f);
        plumes = PlumeLooks.plumes(level);
        plateauTexture = plateauTexture();
        plateau = new TextureRegion(plateauTexture);
    }

    /** The level's dark, or null for a level whose look has none of part F's keys. */
    static DarkLight of(LevelData level, KindLooks kinds, SoftGlow softGlow) {
        return dark(level) ? new DarkLight(level, kinds, softGlow) : null;
    }

    /** Whether the level is a dark level: a section's under-water look with static lights or an escort's lamp. */
    static boolean dark(LevelData level) {
        return darkLook(level).isPresent();
    }

    /**
     * M8 part G: a section's ambient (or its ramp's end) below this is black water (Level 26's 6-20 %); Level 27's vent
     * field (45-62 %) has static lights but is not dark (m8g-gaps.md §5 G10): its units keep their plain looks and sounds.
     */
    static final double BLACK_AMBIENT = 0.3;

    /**
     * M8 part G: whether the level's lights fall in black water: a dark level ({@link #dark}) with a section's ambient
     * (or its ramp's end) below {@value #BLACK_AMBIENT}; the units' glow-only frames and the dark's sounds (the crown
     * hum, the headlights' clicks) only there.
     */
    static boolean blackWater(LevelData level) {
        if (!dark(level)) {
            return false;
        }
        for (int i = 0; i < level.sections().size(); i++) {
            Optional<UnderwaterLook> look = level.underwaterLookOf(i);
            if (look.isPresent()
                    && (look.get().ambient() < BLACK_AMBIENT
                            || look.get()
                                    .ramp()
                                    .map(ramp -> ramp.to() < BLACK_AMBIENT)
                                    .orElse(false))) {
                return true;
            }
        }
        return false;
    }

    private static Optional<UnderwaterLook> darkLook(LevelData level) {
        for (int i = 0; i < level.sections().size(); i++) {
            Optional<UnderwaterLook> look = level.underwaterLookOf(i);
            if (look.isPresent()
                    && (!look.get().lightList().isEmpty()
                            || look.get().escortLamp().isPresent())) {
                return look;
            }
        }
        return Optional.empty();
    }

    /** Rook's loss's looks, for his lamp as he spins down (Level 26). */
    void rookLoss(RookLossLooks looks) {
        rookLoss = looks;
    }

    /** The escort's lamp's radius, px (0 without one): it stands in for the grade's pool round Rook. */
    float escortLamp() {
        return escortLamp;
    }

    /** The play-field height of a static light on terrain row {@code row} at ground scroll {@code scroll}. */
    static double lightY(double row, double scroll) {
        return Terrain.y(scroll, row);
    }

    /** A unit's rim and sonar pool's radius for its hit box, px. */
    static float rimRadius(Hitbox box) {
        return (float) (RIM_REACH * Math.max(box.width(), box.height()) / 2 / PLATEAU);
    }

    /** The Sonar Pulse's whole-unit light with {@code seconds} of its window left: full, fading over its end. */
    static float sonarLight(boolean active, double seconds) {
        return active ? (float) Math.clamp(seconds / SONAR_FADE, 0, 1) : 0;
    }

    /** A pickup's halo opacity at real time {@code real} (its own {@code phase}). */
    static float haloAlpha(double real, int phase) {
        return (float) (HALO_ALPHA + HALO_SWING * Math.sin(2 * Math.PI * HALO_HZ * real + phase * 1.7));
    }

    /** Per enemy kind its glow pool's index in {@link #GLOWS}, -1 for none. */
    static int[] glowsOf(List<EnemySpec> kinds) {
        int[] out = new int[kinds.size()];
        for (int k = 0; k < out.length; k++) {
            out[k] = -1;
            String slug = kinds.get(k).slug();
            for (int g = 0; g < GLOWS.size(); g++) {
                Glow glow = GLOWS.get(g);
                if (slug.equals(glow.slug()) || (glow.prefix() && slug.startsWith(glow.slug() + "-"))) {
                    out[k] = g;
                    break;
                }
            }
        }
        return out;
    }

    /**
     * Into the light map (additive, its projection: the play field from 0): the static lights, the units' rim, glow
     * and sonar pools, the escort's lamp.
     */
    void drawLight(SpriteBatch batch, UnderwaterGrade grade, Sortie sortie, float alpha, double scroll) {
        if (glowOf == null) {
            glowOf = glowsOf(sortie.enemyKinds());
        }
        for (int i = 0; i < lightRow.length; i++) {
            double y = lightY(lightRow[i], scroll);
            float radius = lightRadius[i];
            if (y < -radius || y > PlayField.HEIGHT + radius) {
                continue;
            }
            tint(batch, grade, y, lightColour[i][0], lightColour[i][1], lightColour[i][2], LIGHT);
            grade.pool(batch, lightX[i], y, radius);
        }
        SpecialSlot special = sortie.special();
        float sonar = sonarLight(special.sonarActive(), special.sonarSeconds());
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            float radius = rimRadius(enemy.hitbox());
            if (y < -radius - 40 || y > PlayField.HEIGHT + radius + 40) {
                continue;
            }
            float[] look = grade.lookAt(y);
            float rim = Math.max(look[UnderwaterGrade.RIM], sonar);
            if (rim > 0) {
                tint(batch, grade, y, 1, 1, 1, rim);
                flat(batch, x, y, radius);
            }
            int g = enemy.kind() < glowOf.length ? glowOf[enemy.kind()] : -1;
            if (g >= 0) {
                glowPool(batch, grade, sortie, enemy, GLOWS.get(g), x, y);
            }
        }
        drawLamp(batch, grade, sortie, alpha);
        if (plumes) {
            drawPlumes(batch, grade, sortie, scroll);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * M8 part G: each heat plume's amber pool on its mouth ({@link PlumeLooks#poolLight}: rising through the tell, full
     * while it burns, gone when cold), {@value PlumeLooks#POOL_REACH} times its disc's radius.
     */
    private void drawPlumes(SpriteBatch batch, UnderwaterGrade grade, Sortie sortie, double scroll) {
        FurnaceVents vents = sortie.vents();
        double lag = scroll - sortie.groundScroll();
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.present(g)) {
                continue;
            }
            float light = PlumeLooks.poolLight(vents.phase(g), vents.progress(g));
            double y = vents.y(g) - lag;
            float radius = (float) (PlumeLooks.POOL_REACH * vents.radius(g));
            if (light <= 0 || y < -radius || y > PlayField.HEIGHT + radius) {
                continue;
            }
            tint(batch, grade, y, PlumeLooks.AMBER.r, PlumeLooks.AMBER.g, PlumeLooks.AMBER.b, light);
            grade.pool(batch, vents.x(g), y, radius);
        }
    }

    private void glowPool(
            SpriteBatch batch, UnderwaterGrade grade, Sortie sortie, Enemy enemy, Glow glow, double x, double y) {
        double gx = x;
        double gy = y;
        if (glow.at() == SAC) {
            gx = kinds.sirens(sortie).sacX(x);
            gy = y + kinds.sirens(sortie).sacDy(x);
        } else if (glow.at() == LURE) {
            AnglerLooks anglers = kinds.anglers(sortie);
            int heading = anglers.heading(enemy);
            tint(batch, grade, y, EYES.r, EYES.g, EYES.b, EYES_LIGHT);
            grade.pool(batch, x + anglers.eyesDx(heading), y + anglers.eyesDy(heading), EYES_RADIUS);
            gx = x + anglers.lureDx(heading);
            gy = y + anglers.lureDy(heading);
        }
        tint(batch, grade, gy, glow.r(), glow.g(), glow.b(), GLOW_LIGHT);
        grade.pool(batch, gx, gy, glow.radius());
    }

    /**
     * The escort's lamp round Rook (flying, not ejected; from his loss flickering and dying with his spin, at the spin
     * frame's lamp) or the Bulwark (not destroyed).
     */
    private void drawLamp(SpriteBatch batch, UnderwaterGrade grade, Sortie sortie, float alpha) {
        if (escortLamp <= 0) {
            return;
        }
        Wingman rook = sortie.wingman().orElse(null);
        float lamp = rook == null ? 0 : RookLossLooks.lamp(rook);
        if (rook != null && !rook.ejected() && lamp > 0) {
            double x = rookLoss == null ? rook.renderX(alpha) : rookLoss.lampX(rook, alpha);
            double y = rookLoss == null ? rook.renderY(alpha) : rookLoss.lampY(rook, alpha);
            tint(batch, grade, y, LAMP.r, LAMP.g, LAMP.b, LAMP_LIGHT * lamp);
            grade.pool(batch, x, y, escortLamp);
        }
        Bulwark drone = sortie.bulwark().orElse(null);
        if (drone != null && !drone.destroyed()) {
            double y = drone.renderY(alpha);
            tint(batch, grade, y, LAMP.r, LAMP.g, LAMP.b, LAMP_LIGHT);
            grade.pool(batch, drone.renderX(alpha), y, escortLamp);
        }
    }

    /** The batch's colour: {@code (r, g, b)} times the light missing to full at height {@code y}, at {@code a}. */
    private static void tint(SpriteBatch batch, UnderwaterGrade grade, double y, float r, float g, float b, float a) {
        float[] look = grade.lookAt(y);
        float strength = grade.strengthAt(y);
        batch.setColor(
                r * (1 - look[UnderwaterGrade.AMBIENT] * look[1]) * strength,
                g * (1 - look[UnderwaterGrade.AMBIENT] * look[2]) * strength,
                b * (1 - look[UnderwaterGrade.AMBIENT] * look[3]) * strength,
                a);
    }

    /** The flat pool (full over its middle {@value #PLATEAU}, a soft edge) of {@code radius} at (x, y). */
    private void flat(SpriteBatch batch, double x, double y, float radius) {
        batch.draw(plateau, (float) (x - radius), (float) (y - radius), 2 * radius, 2 * radius);
    }

    /** Ungraded, additive: each pickup's warm gold halo, pulsing slowly on the real clock. */
    void drawHalos(SpriteBatch batch, Sortie sortie, float alpha, double real) {
        if (sortie.pickupCount() == 0) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        double t = real % 3600;
        for (int i = 0; i < sortie.pickupCount(); i++) {
            SoftGlow.tint(batch, GOLD, haloAlpha(t, i));
            softGlow.disc(
                    batch, sortie.pickup(i).renderX(alpha), sortie.pickup(i).renderY(alpha), HALO_DIAMETER);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** The flat pool's light {@code d} of its radius from its middle: full to {@value #PLATEAU}, then a smooth edge. */
    static float plateauAlpha(double d) {
        double u = Math.clamp((1 - d) / (1 - PLATEAU), 0, 1);
        return (float) (u * u * (3 - 2 * u));
    }

    private static Texture plateauTexture() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        double half = size / 2.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double d = Math.hypot(x + 0.5 - half, y + 0.5 - half) / half;
                pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, plateauAlpha(d)));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        plateauTexture.dispose();
    }
}
