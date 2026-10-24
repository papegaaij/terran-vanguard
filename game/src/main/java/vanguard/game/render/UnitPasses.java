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
import java.util.List;
import vanguard.game.render.LevelRenderer.Depth;
import vanguard.game.render.LevelRenderer.Launched;
import vanguard.sim.Chain;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.Flock;
import vanguard.sim.Pursuit;
import vanguard.sim.SetPiece;
import vanguard.sim.Ship;
import vanguard.sim.Sortie;

/**
 * The enemy units for {@link LevelRenderer}'s passes: the units at each depth (a cloaked unit's
 * shimmer, a decloak, a naval unit's two layers, a leap's glow), the segment chains, the walkers' and
 * spawners' glows, a spawner's creep, their drop shadows, the Lampreys latched on the ship, and what a
 * lightning flash reveals under the banks.
 */
final class UnitPasses {
    /** A diver's pause flare pulses at 10 fps; a crane's lights blink at 3 Hz. */
    private static final int FLARE_FRAME_TICKS = 6;

    /** M5 part D: a cloaked unit without its shimmer set is drawn as its body at this opacity, additive. */
    private static final float PLACEHOLDER_CLOAK = 0.35f;

    /** The units the level's boss launches from its windows (the Brood Carrier's sacs); empty without. */
    private final List<EnemySpec> launches;

    /** M5 part E: each Driftjelly's quickening pulse. */
    private final PulseClock pulses = new PulseClock();

    /** M5 part E: the last enemy volleys (x, y, step), for a gun's recoil frame. */
    private final double[] firedX = new double[FIRED];

    private final double[] firedY = new double[FIRED];

    private final long[] firedTick = new long[FIRED];

    private int fired;

    private static final int FIRED = 16;

    private static final float[] NO_OFFSET = new float[2];

    /** A volley belongs to the raft within this distance of where it left, px. */
    private static final double FIRED_REACH = 24;

    /** M7 part B: the Dust Kites' entry fade. */
    private final KiteFade kiteFade = new KiteFade();

    /** M7 part B: a Burrower's frame drawn as a unit's, flashing white after a hit (allocated once). */
    final BurrowerLooks.Flasher unitFlash = this::drawUnit;

    private final EnemyLooks[] looks;
    private final FlashShader flash;
    private final PlayFrame play;
    private final KindLooks kinds;
    private final FarsideLooks farside;
    private final LunaLooks luna;
    private final WeatherLooks weather;
    private final ShipLooks shipLooks;
    /** M5 part E: the surface's water effects, for a surfaced bell's ripple train. */
    private final Effects surfaceWater;
    /** Step GFX2: the soft disc and ring for the pursuit swarm's rim light and tell ({@link PursuitLooks}). */
    private final SoftGlow softGlow;

    UnitPasses(
            EnemyLooks[] looks,
            FlashShader flash,
            PlayFrame play,
            KindLooks kinds,
            FarsideLooks farside,
            LunaLooks luna,
            WeatherLooks weather,
            ShipLooks shipLooks,
            Effects surfaceWater,
            List<EnemySpec> launches,
            SoftGlow softGlow) {
        this.softGlow = softGlow;
        this.looks = looks;
        this.flash = flash;
        this.play = play;
        this.kinds = kinds;
        this.farside = farside;
        this.luna = luna;
        this.weather = weather;
        this.shipLooks = shipLooks;
        this.surfaceWater = surfaceWater;
        this.launches = launches;
        java.util.Arrays.fill(firedTick, Long.MIN_VALUE);
    }

    /**
     * Whether a boss is off the play plane (on high-air, descending or rising): its launched units
     * are then drawn over its hull, which is drawn over the ship.
     */
    boolean bossOffPlane(Sortie sortie) {
        if (launches.isEmpty()) {
            return false;
        }
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (piece.boss().isPresent() && piece.present() && !piece.onPlane()) {
                return true;
            }
        }
        return false;
    }

    /** Whether an enemy is of a kind the boss launches (the same spec its windows hatch it from). */
    private boolean launched(Enemy enemy) {
        for (EnemySpec kind : launches) {
            if (enemy.spec() == kind) {
                return true;
            }
        }
        return false;
    }

    /** The enemies at one depth ({@code which} of them); a diver flares in its pause. */
    void drawEnemies(SpriteBatch batch, Sortie sortie, float alpha, Depth depth, Launched which) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (Depth.of(enemy) != depth
                    || enemy.chain() != null
                    || enemy.latched()
                    || (which != Launched.ANY && launched(enemy) != (which == Launched.ONLY))) {
                // M5 part F: a latched Lamprey is drawn over the ship (drawLatched).
                continue;
            }
            if (enemy.burrowing()) {
                // M7 part B: buried, bulging, up or diving; without its art only its body while it is up.
                if (kinds.burrowers(sortie).has(enemy.kind())) {
                    kinds.burrowers(sortie)
                            .drawUnit(batch, enemy, sortie.tick(), alpha, sortie.groundSpeed(), unitFlash);
                    continue;
                }
                if (!enemy.layer().touchable()) {
                    continue;
                }
            }
            if (kinds.rays(sortie).draws(enemy)) {
                // M8 part D: an Abyss Ray on the play plane (the rise's second half, the glide, the sink's first);
                // on sub it is drawn under the flyers (drawRisers).
                if (!AbyssRayLooks.deep(enemy)) {
                    kinds.rays(sortie).draw(batch, sortie, enemy, alpha, unitFlash);
                }
                continue;
            }
            if (kinds.sirens(sortie).draws(enemy)) {
                // M8 part E: a Siren on its wall's foot.
                kinds.sirens(sortie).draw(batch, sortie, enemy, alpha, unitFlash);
                continue;
            }
            if (kinds.anglers(sortie).draws(enemy)) {
                // M8 part F: a Glow Angler at its heading and jaw (its glows after the grade, AnglerLooks).
                kinds.anglers(sortie).draw(batch, sortie, enemy, alpha, unitFlash);
                continue;
            }
            if (kinds.nautili(sortie).draws(enemy)) {
                // M8 part E: a Spiral Nautilus's shell and crown; leaving through rock it is drawn under the walls.
                if (!kinds.nautili(sortie).underWall(sortie, enemy, alpha)) {
                    kinds.nautili(sortie).draw(batch, sortie, enemy, alpha, unitFlash);
                }
                continue;
            }
            EnemyLooks look = looks[enemy.kind()];
            AtlasRegion frame = enemyFrame(sortie, enemy, i, alpha);
            // M7 part B: a Dust Kite fades in from the dust as it enters (a look only).
            float fade = kiteFade.opacity(enemy, sortie.tick(), alpha);
            if (fade < 1) {
                batch.setColor(1, 1, 1, fade);
                drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
                batch.setColor(Color.WHITE);
                continue;
            }
            if (depth == Depth.HIGH_AIR) {
                drawCloaked(batch, sortie, enemy, look, i, alpha, 1);
                continue;
            }
            if (LunaLooks.target(sortie, enemy)) {
                luna.drawOutline(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
            }
            double decloak = enemy.decloak(alpha);
            if (decloak < 1) {
                drawDecloak(batch, sortie, enemy, look, frame, i, alpha, decloak);
                continue;
            }
            if (enemy.growing()) {
                // M5 part H: a root burst's growth grows in, scaled and tinted (it takes no hits yet).
                RootBurstLooks.drawGrowing(batch, enemy, frame, enemy.renderX(alpha), enemy.renderY(alpha), alpha);
                continue;
            }
            if (sortie.underwater() && kinds.deepBodies(sortie).deep(enemy.kind()) != null) {
                // M8 part C: a Driftjelly on the play plane under water: its whole body at its pulse frame.
                Array<AtlasRegion> body = kinds.deepBodies(sortie).deep(enemy.kind());
                drawUnit(
                        batch,
                        enemy,
                        body.get(jellyPulse(sortie, enemy, alpha) % body.size),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha));
                continue;
            }
            if (depth == Depth.GROUND && look.naval().twoLayers()) {
                // M5 part E: its surface layer over the body the sub pass drew.
                drawNaval(batch, sortie, enemy, look, alpha);
                continue;
            }
            if (enemy.serial() == play.heatTarget) {
                // M7 part A: the unit the beam burns, in a steady heat tint (not a blink per step).
                flash.draw(
                        batch,
                        frame,
                        Math.round(X0 + enemy.renderX(alpha)),
                        Math.round(enemy.renderY(alpha)),
                        ArsenalLooks.HEAT,
                        ArsenalLooks.HEAT_AMOUNT);
            } else if (kinds.shellbacks(sortie).glancing(enemy, sortie.tick())) {
                // M7 part D: a hit glancing off a curled Shellback flashes its shell grey.
                flash.draw(
                        batch,
                        frame,
                        Math.round(X0 + enemy.renderX(alpha)),
                        Math.round(enemy.renderY(alpha)),
                        ShellbackLooks.GLANCE_GREY,
                        play.whiteFlash);
            } else {
                drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
            }
            if (depth == Depth.AIR && enemy.leaping()) {
                // In its air window the leap's glow goes with the body, over the low-air layer.
                drawLeapGlow(batch, enemy, look, alpha);
            }
            // M7 part E: an open Dust Devil's lime core over its body.
            kinds.vortices(sortie).drawCore(batch, enemy, sortie.tick(), alpha);
            if (enemy.paused() && !look.flare().isEmpty()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                AtlasRegion flare = look.flare().get((int) (sortie.tick() / FLARE_FRAME_TICKS % look.flare().size));
                drawCentred(batch, flare, enemy.renderX(alpha), enemy.renderY(alpha));
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /**
     * M5 part D: a cloaked unit's shimmer (the Wraith on high-air; tools/art/wraith.py), additive at
     * {@code opacity}, already at the high-air scale, a hit flashing it once more; without a shimmer
     * set its body frame stands in, faint and at the high-air scale.
     */
    private void drawCloaked(
            SpriteBatch batch, Sortie sortie, Enemy enemy, EnemyLooks look, int i, float alpha, float opacity) {
        if (opacity <= 0) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        long step = look.step(sortie.tick(), i);
        if (look.cloaks()) {
            AtlasRegion shimmer = look.cloakFrame(enemy.facing(), step);
            batch.setColor(1, 1, 1, opacity);
            drawCentred(batch, shimmer, x, y);
            if (enemy.ticksSinceHit() < HIT_FLASH_TICKS) {
                batch.setColor(1, 1, 1, opacity * play.whiteFlash);
                drawCentred(batch, shimmer, x, y);
            }
        } else {
            batch.setColor(1, 1, 1, PLACEHOLDER_CLOAK * opacity);
            drawScaled(batch, look.frame(enemy.facing(), step), x, y, LevelRenderer.HIGH_AIR_SCALE);
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * M5 part D: a unit decloaking on air (its 0.4 s flash, tools/art/wraith.py): the body fading in
     * over the flash, the shimmer fading out over its first half, the violet flash over both.
     */
    private void drawDecloak(
            SpriteBatch batch,
            Sortie sortie,
            Enemy enemy,
            EnemyLooks look,
            AtlasRegion frame,
            int i,
            float alpha,
            double progress) {
        batch.setColor(1, 1, 1, EnemyLooks.bodyOpacity(progress));
        drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
        batch.setColor(Color.WHITE);
        drawCloaked(batch, sortie, enemy, look, i, alpha, EnemyLooks.shimmerOpacity(progress));
        int flashFrame = look.decloakFrame(progress);
        if (flashFrame >= 0) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            drawCentred(batch, look.decloak().get(flashFrame), enemy.renderX(alpha), enemy.renderY(alpha));
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    /**
     * Enemy {@code i}'s frame: its heading and animation step, its walk cycle or the Mantis's pose; a
     * periodic spawner's iris state and pulse, a pouncer's leap frame in the air part of its leap.
     */
    private AtlasRegion enemyFrame(Sortie sortie, Enemy enemy, int i, float alpha) {
        EnemyLooks look = looks[enemy.kind()];
        AtlasRegion mantis = farside.mantisFrame(look, enemy, sortie.tick());
        if (mantis != null) {
            return mantis;
        }
        if (look.iris()) {
            return look.frames().get(EnemyLooks.irisFrame(enemy.iris(), sortie.tick(), enemy.serial()));
        }
        int step = leapStep(enemy, look, alpha);
        if (step >= 0) {
            return look.leap().get(look.leapFrame(enemy.facing(), step));
        }
        LampreyLooks lamprey = kinds.lampreys() == null ? null : kinds.lampreys()[enemy.kind()];
        if (lamprey != null && enemy.latchPhase() == Enemy.LatchPhase.STUNNED) {
            // M5 part F: thrown off, tumbling a full turn over the stun.
            return lamprey.stunned().get(LampreyLooks.stunnedIndex(enemy.stunProgress(), lamprey.stunned().size));
        }
        // M7 part D: a Shellback curling, rolling or uncurling.
        AtlasRegion curled = kinds.shellbacks(sortie).frame(enemy);
        if (curled != null) {
            return curled;
        }
        // M7 part E: a Dust Devil spinning closed, rising, open or sinking; a Ghost Drone's dark lamps under the EMP.
        AtlasRegion vortex = kinds.vortices(sortie).frame(enemy);
        if (vortex != null) {
            return vortex;
        }
        AtlasRegion stunned = kinds.ghostDrones(sortie).frame(enemy);
        if (stunned != null) {
            return stunned;
        }
        // M8 part C: a Glassfin swims or glints before its ram.
        AtlasRegion fin = kinds.glassfins(sortie).frame(enemy);
        if (fin != null) {
            return fin;
        }
        // M8 part D: a Barbfin swims or tells its needle; an Abyss Ray glides with its wing beat.
        AtlasRegion barb = kinds.barbfins(sortie).frame(enemy, sortie.tick());
        if (barb != null) {
            return barb;
        }
        AtlasRegion glide = kinds.rays(sortie).frame(enemy, sortie.tick());
        if (glide != null) {
            return glide;
        }
        // M8 part E: a Siren's step, a Spiral Nautilus's shell (rolling closed, or open toward the ship).
        AtlasRegion nest = kinds.sirens(sortie).frame(sortie, enemy, alpha);
        if (nest != null) {
            return nest;
        }
        AtlasRegion shell = kinds.nautili(sortie).frame(sortie, enemy, alpha);
        if (shell != null) {
            return shell;
        }
        // M8 part F: a Glow Angler at its heading and jaw.
        AtlasRegion angler = kinds.anglers(sortie).frame(sortie, enemy);
        if (angler != null) {
            return angler;
        }
        // M7 part C: a Tendril Anchor faces its partner and pulses.
        AtlasRegion anchor = kinds.tendrils(sortie).anchorFrame(sortie, enemy, look);
        if (anchor != null) {
            return anchor;
        }
        // M5 part G: a lane walker (the Skimmer) shows its heading's tail beat, not a walk cycle.
        return enemy.walking() && !enemy.laneWalking()
                ? look.frames().get(look.walkFrame(enemy.facing(), enemy.walked()))
                : look.frame(enemy.facing(), look.step(sortie.tick(), i, enemy.burstSeconds()));
    }

    /**
     * M5 part E: a naval unit's surface layer (tools/art/driftjelly.py, reef_spitter.py): a jelly's
     * surfacing step while it swaps, its dome with its collar while surfaced (a hit flashing it), its
     * pulse's bloom through the water while submerged (additive); a raft's bob frame with its gun at
     * the frame's gun point (the recoil frame just after a volley).
     */
    private void drawNaval(SpriteBatch batch, Sortie sortie, Enemy enemy, EnemyLooks look, float alpha) {
        NavalLooks naval = look.naval();
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        long tick = sortie.tick();
        if (naval.skims()) {
            // M5 part G: a Skimmer's wake on the surface under its body above the waterline.
            int heading = EnemyLooks.heading(enemy.facing(), look.headings());
            drawCentred(batch, naval.wakeFrame(heading, tick, enemy.serial()), x, y);
            drawUnit(batch, enemy, look.frame(enemy.facing(), look.step(tick, enemy.serial())), x, y);
            return;
        }
        AtlasRegion floor = kinds.deepBodies(sortie).floor(enemy.kind());
        if (naval.rafts() && floor != null && sortie.underwater()) {
            // M8 part C: a Reef Spitter on its floor base under water (no raft, no bob), its gun on the base's centre.
            drawCentred(batch, floor, x, y);
            boolean recoil = !naval.recoil().isEmpty() && firedNear(x, y, tick, NavalLooks.RECOIL_TICKS);
            Array<AtlasRegion> set = recoil ? naval.recoil() : look.frames();
            AtlasRegion frame =
                    set.get(EnemyLooks.heading(enemy.facing(), look.headings()) * (set.size / look.headings()));
            drawUnit(batch, enemy, frame, x, y + DeepBodyLooks.FLOOR_GUN_DY);
            return;
        }
        if (naval.rafts()) {
            int bob = NavalLooks.raftFrame(tick, enemy.serial(), naval.raft().size);
            drawCentred(batch, naval.raft().get(bob), x, y);
            float[] gun = naval.raftGun().length > bob ? naval.raftGun()[bob] : NO_OFFSET;
            boolean recoil = !naval.recoil().isEmpty() && firedNear(x, y, tick, NavalLooks.RECOIL_TICKS);
            Array<AtlasRegion> set = recoil ? naval.recoil() : look.frames();
            AtlasRegion frame =
                    set.get(EnemyLooks.heading(enemy.facing(), look.headings()) * (set.size / look.headings()));
            drawUnit(batch, enemy, frame, x + gun[0], y + gun[1]);
            return;
        }
        double share = enemy.swap(alpha);
        if (share >= 0 && !naval.surface().isEmpty()) {
            drawCentred(
                    batch,
                    naval.surface().get(NavalLooks.swapFrame(share, enemy.diving(), naval.surface().size)),
                    x,
                    y);
            return;
        }
        int pulse = pulses.frame(
                enemy.serial(),
                tick,
                NavalLooks.jellyFps(Math.hypot(
                        sortie.ship().renderX(alpha) - x, sortie.ship().renderY(alpha) - y)),
                NavalLooks.PULSE_FRAMES);
        if (enemy.submerged()) {
            if (!naval.pulse().isEmpty()) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                drawCentred(batch, naval.pulse().get(pulse % naval.pulse().size), x, y);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
            return;
        }
        drawUnit(batch, enemy, look.frames().get(pulse % look.frames().size), x, y);
    }

    /** M8 part C: a jelly's pulse frame now, quickening as the ship closes in (as {@link #drawNaval}'s). */
    private int jellyPulse(Sortie sortie, Enemy enemy, float alpha) {
        double x = enemy.renderX(alpha);
        double y = enemy.renderY(alpha);
        return pulses.frame(
                enemy.serial(),
                sortie.tick(),
                NavalLooks.jellyFps(Math.hypot(
                        sortie.ship().renderX(alpha) - x, sortie.ship().renderY(alpha) - y)),
                NavalLooks.PULSE_FRAMES);
    }

    /**
     * M8 part C: after the under-water grade, the deep units' lights: a jelly's pulse bloom over its body (additive),
     * the eels' stripe and jaws, the Glassfins' glint, the siphon pumps' sump light.
     */
    void drawDeepGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        if (sortie.underwater()) {
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                Array<AtlasRegion> pulse = looks[enemy.kind()].naval().pulse();
                if (pulse.isEmpty() || kinds.deepBodies(sortie).deep(enemy.kind()) == null) {
                    continue;
                }
                Array<AtlasRegion> veins = kinds.deepBodies(sortie).veins(enemy.kind());
                if (kinds.dark() && veins != null) {
                    // M8 part F: in the dark a jelly reads as its veins, the pulse bloom over them stays the tell.
                    drawCentred(
                            batch,
                            veins.get(jellyPulse(sortie, enemy, alpha) % veins.size),
                            enemy.renderX(alpha),
                            enemy.renderY(alpha));
                }
                drawCentred(
                        batch,
                        pulse.get(jellyPulse(sortie, enemy, alpha) % pulse.size),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha));
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        kinds.eels(sortie).drawGlows(batch, sortie, alpha);
        kinds.glassfins(sortie).drawGlows(batch, sortie, alpha);
        kinds.pumps(sortie).drawGlows(batch, sortie, looks, alpha);
        // M8 part D: the Rays' gill slits and mouth flash, the Barbfins' snout sparkle.
        kinds.rays(sortie).drawGlows(batch, sortie, alpha);
        kinds.barbfins(sortie).drawGlows(batch, sortie, alpha);
    }

    /** M8 part D: the Abyss Rays on {@code sub} (their shadow, the rise's first half, the sink's second). */
    void drawRisers(SpriteBatch batch, Sortie sortie, float alpha) {
        kinds.rays(sortie).drawDeep(batch, sortie, alpha, unitFlash);
    }

    /** M8 part E: under the walls, the Spiral Nautili leaving through their rock (behind the walls' face). */
    void drawUnderWalls(SpriteBatch batch, Sortie sortie, float alpha) {
        kinds.nautili(sortie).drawUnderWalls(batch, sortie, alpha, unitFlash);
    }

    /** A unit's frame at (x, y), flashing white just after a hit. */
    void drawUnit(SpriteBatch batch, Enemy enemy, AtlasRegion frame, double x, double y) {
        if (enemy.ticksSinceHit() < HIT_FLASH_TICKS) {
            flash.draw(batch, frame, Math.round(X0 + x), Math.round(y), HIT_WHITE, play.whiteFlash);
        } else {
            drawCentred(batch, frame, x, y);
        }
    }

    /**
     * A pouncer's leap step at the render time ({@link EnemyLooks#leapStep}): -1 when it is not
     * leaping, has no leap frames, or is low enough to be drawn galloping.
     */
    private static int leapStep(Enemy enemy, EnemyLooks look, float alpha) {
        if (!enemy.leaping() || !look.leaps()) {
            return -1;
        }
        return EnemyLooks.leapStep(EnemyLooks.leapLift(enemy.leapProgress(alpha)));
    }

    /** A leaping pouncer's glow (its maw and eyes), additive over its leap frame. */
    private void drawLeapGlow(SpriteBatch batch, Enemy enemy, EnemyLooks look, float alpha) {
        int step = leapStep(enemy, look, alpha);
        if (step < 0 || look.leapGlow().isEmpty()) {
            return;
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        drawCentred(
                batch,
                look.leapGlow().get(look.leapFrame(enemy.facing(), step)),
                enemy.renderX(alpha),
                enemy.renderY(alpha));
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * A pouncer's shadow in its leap (tools/art/ravager.py, the art direction's Shadows rule): its leap
     * frame's silhouette at its ground size, sliding down-right with the lift up to the air layer's
     * offset at the apex; none while it is drawn galloping.
     */
    private static void drawLeapShadow(SpriteBatch batch, Enemy enemy, EnemyLooks look, float alpha) {
        double lift = EnemyLooks.leapLift(enemy.leapProgress(alpha));
        int step = EnemyLooks.leapStep(lift);
        if (step < 0) {
            return;
        }
        Shadows.draw(
                batch,
                look.leap().get(look.leapFrame(enemy.facing(), step)),
                (float) (X0 + enemy.renderX(alpha)),
                (float) enemy.renderY(alpha),
                (int) Math.round(EnemyLooks.LEAP_SHADOW_DX * lift),
                (int) Math.round(EnemyLooks.LEAP_SHADOW_DY * lift),
                (float) (1 / (Shadows.SCALE * EnemyLooks.LEAP_SCALES[step])));
    }

    /**
     * Under each live periodic spawner (the Hive Node) its biomass creep, on the ground over the
     * ground objects (its wither after the death is an effect on the ground, started by the screen).
     */
    void drawCreep(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            EnemyLooks look = looks[enemy.kind()];
            if (!look.creep().isEmpty()) {
                drawCentred(batch, look.creep().first(), enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
    }

    /**
     * The segment chains (design/enemies/air/coilwyrm), each from its tail to its head so the head
     * lies on top, the segments drawn at their tapering sizes; a cut part's head grows at the cut.
     */
    void drawChains(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (chain.ground()) {
                // M7 part C: a Threadcrawler is drawn with the ground units (CrawlerLooks).
                continue;
            }
            if (kinds.eels(sortie).draws(chain)) {
                // M8 part C: an Eel Swarm's head, body eels and tail with the stripe tell (EelLooks).
                kinds.eels(sortie).drawChain(batch, sortie, chain, alpha, unitFlash);
                continue;
            }
            for (int k = chain.size() - 1; k >= 0; k--) {
                Enemy member = chain.member(k);
                if (member != null) {
                    drawChainMember(batch, sortie, chain, member, k, alpha, false);
                }
            }
            if (chain.regrowing() && chain.alive()) {
                EnemyLooks look = looks[chain.regrownKind()];
                AtlasRegion frame = look.frame(chain.headFacing(), look.step(sortie.tick(), 0));
                drawScaled(batch, frame, chain.headX(), chain.headY(), (float) Math.max(0.2, chain.regrowth()));
            }
        }
    }

    /** A chain's member {@code k} at its tapering size, or its shadow. */
    private void drawChainMember(
            SpriteBatch batch, Sortie sortie, Chain chain, Enemy member, int k, float alpha, boolean shadow) {
        double first = chain.spec().segmentBoxes().getFirst().width();
        EnemyLooks look = looks[member.kind()];
        boolean segment = k > 0
                && k < chain.spec().members() - 1
                && member.link() == k
                && member.spec() == chain.spec().segment();
        // The production segments are one set per size of the taper (the frames' phases):
        // the nearest size, unscaled; the placeholder is scaled to the member's size.
        int sizes = look.frames().size / look.headings();
        boolean sized = segment && sizes > 1;
        long step = sized ? sizeIndex(chain, member, sizes) : look.step(sortie.tick(), k);
        AtlasRegion frame = look.frame(member.facing(), step);
        float scale = segment && !sized ? (float) (member.hitbox().width() / first) : 1;
        if (shadow) {
            Shadows.draw(
                    batch,
                    frame,
                    (float) (X0 + member.renderX(alpha)),
                    (float) member.renderY(alpha),
                    Shadows.AIR_DX,
                    Shadows.AIR_DY,
                    scale);
        } else {
            drawScaled(batch, frame, member.renderX(alpha), member.renderY(alpha), scale);
        }
    }

    /**
     * A segment's size in a production set of {@code sizes} sizes tapering from the chain's first
     * segment to its last (tools/art/coilwyrm.py: size 0 the largest): the nearest to its hit box,
     * so a hard chain's 14 segments take the 12 sizes' nearest.
     */
    private static int sizeIndex(Chain chain, Enemy member, int sizes) {
        double first = chain.spec().segmentBoxes().getFirst().width();
        double last = chain.spec().segmentBoxes().getLast().width();
        if (first <= last) {
            return 0;
        }
        long index = Math.round((first - member.hitbox().width()) / (first - last) * (sizes - 1));
        return Math.clamp(index, 0, sizes - 1);
    }

    /**
     * The walkers' emissive backs, additive above the low-air layer, so the Scuttler's lime back
     * glows through the dust that hides its body (design/campaign, Level 04 hazards); a leaping
     * pouncer's maw and eyes off the ground (in its air window they are drawn with its body); a
     * periodic spawner's throat and polyps at its iris state (the Hive Node: the iris glows while it
     * opens, the spawn's telegraph).
     */
    void drawUnitGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            EnemyLooks look = looks[enemy.kind()];
            if (look.glow().isEmpty()) {
                continue;
            }
            // M7 part A: an EMP-stunned unit's glow dims (a machine's running lights go out).
            float glow = ArsenalLooks.glowShare(enemy);
            if (glow == 0) {
                continue;
            }
            batch.setColor(1, 1, 1, glow);
            if (look.iris()) {
                int frame = EnemyLooks.irisFrame(enemy.iris(), sortie.tick(), enemy.serial());
                drawCentred(batch, look.glow().get(frame), enemy.renderX(alpha), enemy.renderY(alpha));
            } else if (enemy.walking()) {
                if (leapStep(enemy, look, alpha) >= 0) {
                    if (Depth.of(enemy) == Depth.LEAP) {
                        drawLeapGlow(batch, enemy, look, alpha);
                        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                    }
                    continue;
                }
                drawCentred(
                        batch,
                        look.glow().get(look.walkFrame(enemy.facing(), enemy.walked())),
                        enemy.renderX(alpha),
                        enemy.renderY(alpha));
            }
        }
        batch.setColor(Color.WHITE);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * M5 part F: the Lampreys latched onto the ship, over the hull at their spots (the ship's position
     * plus the spot's offset, so they ride with it between the steps): latch look b (round 34), the
     * tail lashing and the siphon's glow at each mouth; look a (art without the siphon's frames), with
     * each gulp the ship's shield ring pulsing.
     */
    void drawLatched(SpriteBatch batch, Sortie sortie, float alpha) {
        LampreyLooks[] lampreys = kinds.lampreys();
        if (lampreys == null) {
            return;
        }
        Ship craft = sortie.ship();
        double shipX = craft.renderX(alpha);
        double shipY = craft.renderY(alpha);
        float gulp = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            LampreyLooks look = lampreys[enemy.kind()];
            if (!enemy.latched() || look == null) {
                continue;
            }
            int spot = enemy.spot();
            EnemySpec.Spot offset = enemy.spec().latch().orElseThrow().spots().get(spot);
            int phase = enemy.serial();
            AtlasRegion frame = look.latched().get(LampreyLooks.latchedIndex(spot, sortie.tick(), phase));
            drawUnit(batch, enemy, frame, shipX + offset.dx(), shipY + offset.dy());
            look.drawDrain(batch, spot, sortie.tick(), phase, (float) (X0 + shipX), (float) shipY);
            gulp = Math.max(gulp, look.gulpStrength(LampreyLooks.gulpFrame(sortie.tick(), phase)));
        }
        shipLooks.drawGulp(batch, Math.round(X0 + shipX), Math.round(shipY), gulp);
    }

    /** M5 part E: an enemy fired a volley from (x, y) at step {@code tick} (a gun's recoil frame). */
    void enemyFired(double x, double y, long tick) {
        firedX[fired] = x;
        firedY[fired] = y;
        firedTick[fired] = tick;
        fired = (fired + 1) % FIRED;
    }

    /** Whether a volley left within reach of (x, y) in the last {@code ticks} steps before {@code tick}. */
    private boolean firedNear(double x, double y, long tick, int ticks) {
        for (int i = 0; i < FIRED; i++) {
            if (firedTick[i] != Long.MIN_VALUE
                    && tick - firedTick[i] < ticks
                    && Math.abs(firedX[i] - x) < FIRED_REACH
                    && Math.abs(firedY[i] - y) < FIRED_REACH) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part E: the naval units' plain bodies in the {@code sub} pass, under the water (a surfaced
     * bell's contraction leaving its ripple train on the sea).
     */
    void drawNavalSub(SpriteBatch batch, Sortie sortie, float alpha) {
        long tick = sortie.tick();
        double shipX = sortie.ship().renderX(alpha);
        double shipY = sortie.ship().renderY(alpha);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            EnemyLooks look = looks[enemy.kind()];
            NavalLooks naval = look.naval();
            if (!naval.twoLayers() || Depth.of(enemy) != Depth.GROUND) {
                continue;
            }
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            if (naval.rafts()) {
                drawCentred(batch, naval.raftSub().first(), x, y);
                continue;
            }
            if (naval.skims()) {
                // M5 part G: a Skimmer's plain body at its heading, under the surface.
                drawCentred(batch, naval.sub().get(EnemyLooks.heading(enemy.facing(), naval.sub().size)), x, y);
                continue;
            }
            int pulse = pulses.frame(
                    enemy.serial(),
                    tick,
                    NavalLooks.jellyFps(Math.hypot(shipX - x, shipY - y)),
                    NavalLooks.PULSE_FRAMES);
            boolean swapping = enemy.swap(alpha) >= 0;
            if (pulses.contracted()
                    && !swapping
                    && !enemy.submerged()
                    && !naval.ripple().isEmpty()) {
                // A surfaced bell's contraction leaves its ripple train on the sea.
                surfaceWater.startOnGround(
                        naval.ripple(), NavalLooks.RIPPLE_FRAME_TICKS, x, y, 0, sortie.groundScroll());
            }
            drawCentred(batch, naval.sub().get(swapping ? 0 : pulse % naval.sub().size), x, y);
        }
    }

    /**
     * The flyers' drop shadows in the shadow pass: the low flyers' at the low-air offset, the play
     * plane's flyers' and the chains' segments at the air offset, a leaping pouncer's sliding out with
     * its lift.
     */
    void drawShadows(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Depth depth = Depth.of(enemy);
            // M5 part D: a cloaked unit casts no shadow; a decloaking one's fades in with its body.
            // M5 part F: a latched Lamprey lies on the hull, the ship's shadow is its own.
            if (depth == Depth.GROUND || depth == Depth.HIGH_AIR || enemy.chain() != null || enemy.latched()) {
                continue;
            }
            if (kinds.rays(sortie).draws(enemy) && enemy.risePhase() != vanguard.sim.RisePhase.GLIDE) {
                // M8 part D: an Abyss Ray casts its shadow only in its glide (its own shadow is the tell, the rise
                // and the sink are translucent).
                continue;
            }
            if (kinds.sirens(sortie).draws(enemy) || kinds.nautili(sortie).underWall(sortie, enemy, alpha)) {
                // M8 part E: a Siren is part of its wall; a Nautilus in the rock is behind it.
                continue;
            }
            float shade = EnemyLooks.bodyOpacity(enemy.decloak(alpha)) * kiteFade.opacity(enemy, sortie.tick(), alpha);
            if (PursuitLooks.pursuing(enemy.flock())) {
                // Step GFX2: the pursuit swarm's shadows only faint (at 50 % they read as a second flock).
                shade *= PursuitLooks.SHADOW_SHARE;
            }
            if (shade < 1) {
                batch.setColor(0, 0, 0, Shadows.OPACITY * shade);
            }
            if (enemy.leaping()) {
                EnemyLooks look = looks[enemy.kind()];
                if (look.leaps()) {
                    drawLeapShadow(batch, enemy, look, alpha);
                }
                continue;
            }
            boolean low = depth == Depth.LOW_AIR;
            Shadows.draw(
                    batch,
                    enemyFrame(sortie, enemy, i, alpha),
                    (float) (X0 + enemy.renderX(alpha)),
                    (float) enemy.renderY(alpha),
                    low ? Shadows.LOW_AIR_DX : Shadows.AIR_DX,
                    low ? Shadows.LOW_AIR_DY : Shadows.AIR_DY,
                    1);
            if (shade < 1) {
                batch.setColor(0, 0, 0, Shadows.OPACITY);
            }
        }
        for (int c = 0; c < sortie.chainCount(); c++) {
            Chain chain = sortie.chain(c);
            if (chain.ground()) {
                continue;
            }
            for (int k = chain.size() - 1; k >= 0; k--) {
                Enemy member = chain.member(k);
                if (member != null) {
                    drawChainMember(batch, sortie, chain, member, k, alpha, true);
                }
            }
        }
    }

    /** M5 part F, a lightning flash's reveal: the {@code low-air} units under the banks, at the batch's colour. */
    void drawRevealed(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (Depth.of(enemy) == Depth.LOW_AIR && enemy.chain() == null && !enemy.latched()) {
                drawCentred(batch, enemyFrame(sortie, enemy, i, alpha), enemy.renderX(alpha), enemy.renderY(alpha));
            }
        }
    }

    /** M5 part F, over the banks that hide them (additive): each {@code low-air} unit's faint glow. */
    void drawHiddenGlows(SpriteBatch batch, Sortie sortie, float alpha, double real) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (Depth.of(enemy) == Depth.LOW_AIR) {
                weather.drawGlow(batch, enemy.renderX(alpha), enemy.renderY(alpha), real);
            }
        }
    }

    /**
     * M7 part E, the dust storm's silhouette pass ({@link StormLooks#drawSilhouettes}): each {@code air} unit's frame
     * at its place again, at the batch's shader and its entry fade (each frame's texture handed to the storm first,
     * its edge's softening); a unit decloaking, growing, in a chain or latched is left as it is.
     */
    void drawAirSilhouettes(SpriteBatch batch, Sortie sortie, float alpha, StormLooks storm) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (Depth.of(enemy) != Depth.AIR
                    || enemy.chain() != null
                    || enemy.latched()
                    || enemy.growing()
                    || enemy.decloak(alpha) < 1) {
                continue;
            }
            AtlasRegion frame = enemyFrame(sortie, enemy, i, alpha);
            storm.texelOf(batch, frame.getTexture());
            batch.setColor(1, 1, 1, kiteFade.opacity(enemy, sortie.tick(), alpha));
            drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * M7 part E: the {@code air} units' {@code -glow} frames (the Ghost Drone's red eye and white running lights;
     * tools/art/storm_units.py), additive over their bodies at the heading and frame drawn, dark while an EMP stuns a
     * machine, then a pursuit's lunge tell (M7 part G, {@link PursuitLooks}) and the Ghost Drones' eye glints ({@link
     * GhostDroneLooks}); in every level after the air units, in
     * the dust storm after its silhouette pass, so they light the silhouettes outside the radius.
     */
    void drawAirGlows(SpriteBatch batch, Sortie sortie, float alpha) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            EnemyLooks look = looks[enemy.kind()];
            if (Depth.of(enemy) != Depth.AIR
                    || look.glow().isEmpty()
                    || look.glow().size % look.headings() != 0
                    || look.iris()
                    || enemy.walking()
                    || enemy.chain() != null
                    || enemy.latched()
                    || enemy.decloak(alpha) < 1
                    || kinds.rays(sortie).draws(enemy)
                    || kinds.sirens(sortie).draws(enemy)
                    || kinds.nautili(sortie).draws(enemy)
                    || kinds.anglers(sortie).angler(enemy.kind())
                    || kinds.deepBodies(sortie).veins(enemy.kind()) != null) {
                // M8 part D: an Abyss Ray's glow follows its rise (drawDeepGlows); M8 part E: a Siren's and a
                // Nautilus's their steps (after the grade); M8 part F: an Angler's its heading and jaw (AnglerLooks), a
                // Driftjelly's veins its pulse, in the dark only (drawDeepGlows).
                continue;
            }
            float glow = ArsenalLooks.glowShare(enemy) * kiteFade.opacity(enemy, sortie.tick(), alpha);
            if (glow <= 0) {
                continue;
            }
            batch.setColor(1, 1, 1, glow);
            AtlasRegion frame = look.glowFrame(enemy.facing(), look.step(sortie.tick(), i, enemy.burstSeconds()));
            drawCentred(batch, frame, enemy.renderX(alpha), enemy.renderY(alpha));
        }
        batch.setColor(Color.WHITE);
        drawPursuitTell(batch, sortie, alpha);
        // The Ghost Drones' eye glints before their bursts, through the dust like their lights.
        kinds.ghostDrones(sortie).drawGlints(batch, sortie, alpha);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * M7 part G: a pursuit's lunge tell ({@link PursuitLooks}): each member of a pursuing flock drawn again over its
     * body, additive (the batch is), in a warm white rising through the tell and fading along the dive. Step GFX2: and
     * at play size: each member's pale teal rim light (a halo and its body lit) giving way to a reddish-white disc that
     * grows with the tell, and one short ring widening round the ball through the tell's second part.
     */
    private void drawPursuitTell(SpriteBatch batch, Sortie sortie, float alpha) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!PursuitLooks.pursuing(enemy.flock()) || Depth.of(enemy) != Depth.AIR) {
                continue;
            }
            float glow = PursuitLooks.glow(enemy.flock());
            double x = enemy.renderX(alpha);
            double y = enemy.renderY(alpha);
            float rim = PursuitLooks.rim(glow);
            if (rim > 0) {
                SoftGlow.tint(batch, PursuitLooks.RIM, PursuitLooks.RIM_ALPHA * rim);
                softGlow.disc(batch, x, y, PursuitLooks.RIM_DIAMETER);
                SoftGlow.tint(batch, PursuitLooks.RIM, PursuitLooks.RIM_BODY * rim);
                drawCentred(batch, enemyFrame(sortie, enemy, i, alpha), x, y);
            }
            if (glow > 0) {
                SoftGlow.tint(batch, PursuitLooks.TELL_DISC, PursuitLooks.tellDiscAlpha(glow));
                softGlow.disc(batch, x, y, PursuitLooks.tellDisc(glow));
                Color tint = PursuitLooks.TINT;
                batch.setColor(tint.r, tint.g, tint.b, glow);
                drawCentred(batch, enemyFrame(sortie, enemy, i, alpha), x, y);
            }
        }
        for (int f = 0; f < sortie.flockCount(); f++) {
            Flock flock = sortie.flock(f);
            Pursuit pursuit = flock.pursuing() ? flock.pursuit().orElse(null) : null;
            float ring = pursuit == null ? 0 : PursuitLooks.ringAlpha(pursuit.phase(), pursuit.progress());
            if (ring <= 0) {
                continue;
            }
            double sumX = 0;
            double sumY = 0;
            int members = 0;
            for (int k = 0; k < flock.size(); k++) {
                Enemy member = flock.member(k);
                if (member != null) {
                    sumX += member.renderX(alpha);
                    sumY += member.renderY(alpha);
                    members++;
                }
            }
            if (members > 0) {
                SoftGlow.tint(batch, PursuitLooks.TELL_DISC, ring);
                softGlow.ring(batch, sumX / members, sumY / members, PursuitLooks.ringRadius(pursuit.progress()));
            }
        }
        batch.setColor(Color.WHITE);
    }

    /** M7 part B: after each simulation step, the Dust Kites' entry fades follow it. */
    void stepped(Sortie sortie) {
        kiteFade.stepped(sortie);
    }

    /** The level restarts: the jellies' pulses, the volleys' recoil and the kites' fades start over. */
    void restart() {
        pulses.reset();
        java.util.Arrays.fill(firedTick, Long.MIN_VALUE);
        kiteFade.restart();
    }
}
