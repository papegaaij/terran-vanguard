package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import vanguard.content.BackdropData;
import vanguard.content.BackdropLayer;
import vanguard.content.FallingCityData;
import vanguard.content.LevelData;
import vanguard.sim.PlayField;

/**
 * M10 part C, step C4a (design/campaign Level 37, The falling city; design/world/jovian, Hazards &amp; set pieces: the
 * falling city, A.13 b): the backdrop's {@code fall}. From its {@code t} the ground's placed pieces are not drawn
 * ({@link BackdropPieces#groundUntil}) and its two authored halves are the ground: each is the city's own pixels on its
 * ground rows, so at {@code t} it lies exactly where the city did (the hand-over is pixel-exact), and then follows its
 * keys (place, scale toward the field's centre, turn, darkening toward {@link #SHADE}) relative to that place; its
 * charred rim and its hot glow fade in over the first {@value #RIM_FADE} s as the gap opens. Shards tumble in the gap,
 * the balloons tear and the spires topple on {@code low-air}, and the sinking city crossfades in on {@code deep} as the
 * halves fade out. The halves, rims, glows and shards are drawn bilinear (their atlas pages filtered linearly: every
 * other image there is drawn at its size on whole pixels, which linear filtering leaves exact). The times go by the level
 * clock, the glows' and shards' frames by the real clock; nothing is allocated per frame ({@link Schedule}).
 */
final class FallingCity {
    private static final float X0 = PixelScreen.PLAY_FIELD_X;
    private static final int HEIGHT = PlayField.HEIGHT;

    /** The colour the halves darken toward (ART-C2's proposal: {@code 1e1616}). */
    static final Color SHADE = Color.valueOf("1e1616");
    /** The rims' and glows' fade-in as the gap opens, s (the proposal: the first second). */
    static final double RIM_FADE = 1;
    /** A rim image is this much larger a side than its half (the proposal's {@code CHAR_PAD}). */
    static final int RIM_PAD = 14;
    /** The shards fade in and out at the ends of their span, s. */
    static final double SHARD_FADE = 0.3;

    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = a_color;
                v_color.a = v_color.a * (255.0 / 254.0);
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;
    /**
     * As the default shader, darkened toward {@code u_shade.rgb} by {@code u_shade.a}; a texel below the shadows' ground
     * threshold is discarded, so the halves mark the shadow stencil as the city's pieces do ({@link Shadows}).
     */
    private static final String FRAGMENT = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec4 u_shade;
            void main() {
                vec4 texel = texture2D(u_texture, v_texCoords);
                if (texel.a < 1.0 / 512.0) {
                    discard;
                }
                vec4 c = v_color * texel;
                gl_FragColor = vec4(mix(c.rgb, u_shade.rgb, u_shade.a), c.a);
            }
            """;

    private final Schedule schedule;
    private final AtlasRegion[] halves;
    private final AtlasRegion[] rims;
    private final List<Array<AtlasRegion>> glows = new java.util.ArrayList<>();
    private final double[] glowFps;
    private final Array<AtlasRegion> shards;
    private final double shardFps;
    private final List<Array<AtlasRegion>> balloons = new java.util.ArrayList<>();
    private final List<Array<AtlasRegion>> spires = new java.util.ArrayList<>();
    private final AtlasRegion sinking;
    private final Array<AtlasRegion> sinkingGlow;
    private final double sinkingGlowFps;
    private final ShaderProgram shader;
    private final double[] place = new double[Schedule.SLOTS];
    private final double[] other = new double[Schedule.SLOTS];

    /**
     * @param sprites the backdrop atlas, which holds the level's images as {@code level-NN/<id>}
     * @param folder the level's backdrop folder ({@link Backdrop#folder(String, LevelData)})
     */
    FallingCity(Sprites sprites, LevelData level, String folder) {
        schedule = new Schedule(level);
        BackdropData data = level.backdrop();
        FallingCityData.Fall fall = schedule.fall;
        int count = fall.halves().size();
        halves = new AtlasRegion[count];
        rims = new AtlasRegion[count];
        glowFps = new double[count];
        for (int h = 0; h < count; h++) {
            FallingCityData.Half half = fall.halves().get(h);
            halves[h] = smooth(sprites.backdrop(folder + half.piece(), 1).first());
            rims[h] = half.charred()
                    .map(id -> smooth(sprites.backdrop(folder + id, 1).first()))
                    .orElse(null);
            if (half.glow().isPresent()) {
                BackdropData.Piece spec = data.pieces().get(half.glow().get());
                Array<AtlasRegion> frames =
                        sprites.backdrop(folder + half.glow().get(), spec.imageCount());
                frames.forEach(FallingCity::smooth);
                glows.add(frames);
                glowFps[h] = spec.fps().orElse(0.0);
            } else {
                glows.add(null);
            }
        }
        shards =
                fall.shards().map(s -> frames(sprites, folder, data, s.piece())).orElse(null);
        if (shards != null) {
            shards.forEach(FallingCity::smooth);
        }
        shardFps =
                fall.shards().flatMap(s -> data.pieces().get(s.piece()).fps()).orElse(0.0);
        List<FallingCityData.Placed> tearing = fall.balloons().orElse(List.of());
        for (FallingCityData.Placed balloon : tearing) {
            balloons.add(frames(sprites, folder, data, balloon.piece()));
        }
        List<FallingCityData.Spire> toppling = fall.spires().orElse(List.of());
        for (FallingCityData.Spire spire : toppling) {
            spires.add(frames(sprites, folder, data, spire.piece()));
        }
        sinking = fall.sinking()
                .map(s -> sprites.backdrop(folder + s.piece(), 1).first())
                .orElse(null);
        sinkingGlow = fall.sinking()
                .flatMap(FallingCityData.Sinking::glow)
                .map(id -> frames(sprites, folder, data, id))
                .orElse(null);
        sinkingGlowFps = fall.sinking()
                .flatMap(FallingCityData.Sinking::glow)
                .flatMap(id -> data.pieces().get(id).fps())
                .orElse(0.0);
        shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) {
            throw new IllegalStateException("falling city shader: " + shader.getLog());
        }
    }

    private static Array<AtlasRegion> frames(Sprites sprites, String folder, BackdropData data, String id) {
        return sprites.backdrop(folder + id, data.pieces().get(id).imageCount());
    }

    /** Bilinear filtering on the image's page (scaled and turned; the page's other images stay exact on whole pixels). */
    private static AtlasRegion smooth(AtlasRegion region) {
        region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return region;
    }

    /** The level time from which the city's ground pieces give way to the halves. */
    double start() {
        return schedule.fall.t();
    }

    void dispose() {
        shader.dispose();
    }

    /**
     * The halves on the ground layer from the fall's {@code t} (in the ground pass, where they mark the shadow stencil
     * as the city did): the shards in the gap under them, then each half (the west first) with its rim and its glow.
     */
    void drawGround(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        double script = clock.script();
        if (!schedule.falling(script)) {
            return;
        }
        ShaderProgram before = batch.getShader();
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setShader(shader);
        if (shards != null && schedule.shards(script, groundScroll, place, other)) {
            TextureRegion frame = shards.get(frameAt(clock.real(), shardFps, shards.size));
            shade(batch, (float) place[Schedule.SHADE]);
            batch.setColor(1, 1, 1, (float) place[Schedule.ALPHA]);
            drawCentred(batch, frame, place[Schedule.LEFT], place[Schedule.BOTTOM], place[Schedule.SCALE], 0);
        }
        for (int h = 0; h < halves.length; h++) {
            schedule.half(h, script, groundScroll, place);
            float alpha = (float) place[Schedule.ALPHA];
            if (alpha <= 0) {
                continue;
            }
            AtlasRegion half = halves[h];
            shade(batch, (float) place[Schedule.SHADE]);
            batch.setColor(1, 1, 1, alpha);
            float scale = (float) place[Schedule.SCALE];
            float rotation = (float) place[Schedule.ROTATION];
            float left = (float) place[Schedule.LEFT];
            float bottom = (float) place[Schedule.BOTTOM];
            float w = half.getRegionWidth();
            float hh = half.getRegionHeight();
            batch.draw(half, X0 + left, bottom, w / 2, hh / 2, w, hh, scale, scale, rotation);
            float rim = alpha * (float) place[Schedule.FADE_IN];
            if (rim <= 0) {
                continue;
            }
            if (rims[h] != null) {
                batch.setColor(1, 1, 1, rim);
                drawPadded(batch, rims[h], left, bottom, w, hh, scale, rotation);
            }
            Array<AtlasRegion> glow = glows.get(h);
            if (glow != null) {
                shade(batch, 0);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.setColor(1, 1, 1, rim);
                drawPadded(
                        batch,
                        glow.get(frameAt(clock.real(), glowFps[h], glow.size)),
                        left,
                        bottom,
                        w,
                        hh,
                        scale,
                        rotation);
                batch.setBlendFunction(src, dst);
            }
        }
        batch.setColor(Color.WHITE);
        batch.setShader(before);
    }

    /** The torn balloons and the toppling spires on {@code low-air}, scrolled with it (placed as usual). */
    void drawLowAir(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        double script = clock.script();
        long scroll = Math.round(groundScroll * schedule.lowAir);
        for (int b = 0; b < balloons.size(); b++) {
            Array<AtlasRegion> frames = balloons.get(b);
            int frame = Schedule.oneShot(script - schedule.fall.t(), schedule.balloonFps[b], frames.size);
            drawPlaced(batch, frames.get(frame), schedule.balloonX[b], schedule.balloonBottom[b] - scroll);
        }
        for (int s = 0; s < spires.size(); s++) {
            Array<AtlasRegion> frames = spires.get(s);
            int frame = Schedule.oneShot(
                    script - schedule.fall.t() - schedule.toppleAt[s], schedule.spireFps[s], frames.size);
            drawPlaced(batch, frames.get(frame), schedule.spireX[s], schedule.spireBottom[s] - scroll);
        }
    }

    /** The sinking city on {@code deep}, crossfading in as the halves fade out; its glow additive. */
    void drawDeep(SpriteBatch batch, double groundScroll, BackdropClock clock) {
        if (sinking == null) {
            return;
        }
        float alpha = (float) schedule.sinkingAlpha(clock.script());
        if (alpha <= 0) {
            return;
        }
        long y = schedule.sinkingBottom - Math.round(groundScroll * schedule.deep);
        if (y >= HEIGHT || y + sinking.getRegionHeight() <= 0) {
            return;
        }
        float x = X0 + Math.round(schedule.sinkingX - sinking.getRegionWidth() / 2.0);
        batch.setColor(1, 1, 1, alpha);
        batch.draw(sinking, x, y);
        if (sinkingGlow != null) {
            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.draw(sinkingGlow.get(frameAt(clock.real(), sinkingGlowFps, sinkingGlow.size)), x, y);
            batch.setBlendFunction(src, dst);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPlaced(SpriteBatch batch, TextureRegion image, double centreX, long y) {
        int w = image.getRegionWidth();
        if (y >= HEIGHT || y + image.getRegionHeight() <= 0) {
            return;
        }
        batch.draw(image, X0 + Math.round(centreX - w / 2.0), y);
    }

    /** A rim or glow image, {@link #RIM_PAD} px larger a side than its half, on the half's centre, scale and turn. */
    private static void drawPadded(
            SpriteBatch batch,
            TextureRegion image,
            float left,
            float bottom,
            float w,
            float h,
            float scale,
            float rot) {
        float pw = image.getRegionWidth();
        float ph = image.getRegionHeight();
        float padX = (pw - w) / 2;
        float padY = (ph - h) / 2;
        batch.draw(image, X0 + left - padX, bottom - padY, pw / 2, ph / 2, pw, ph, scale, scale, rot);
    }

    private static void drawCentred(
            SpriteBatch batch, TextureRegion image, double centreX, double centreY, double scale, float rot) {
        float w = image.getRegionWidth();
        float h = image.getRegionHeight();
        batch.draw(
                image,
                (float) (X0 + centreX - w / 2),
                (float) (centreY - h / 2),
                w / 2,
                h / 2,
                w,
                h,
                (float) scale,
                (float) scale,
                rot);
    }

    private void shade(SpriteBatch batch, float share) {
        batch.flush();
        shader.setUniformf("u_shade", SHADE.r, SHADE.g, SHADE.b, share);
    }

    /** A looping animation's frame at real {@code seconds}. */
    static int frameAt(double seconds, double fps, int count) {
        if (fps <= 0 || count <= 1) {
            return 0;
        }
        return (int) Math.floorMod((long) Math.floor(seconds * fps), (long) count);
    }

    /**
     * The fall's schedule from the level's data, pure and allocation-free (tested without a GL context): where each
     * half lies at a level time, the shards' place, the one-shot frames, the crossfade.
     */
    static final class Schedule {
        /** {@link #half}'s output: the left column (field px) and bottom (screen px, up) of the unscaled image. */
        static final int LEFT = 0;

        static final int BOTTOM = 1;
        /** The scale about the image's centre. */
        static final int SCALE = 2;
        /** The turn, ° counter-clockwise (the batch's sense; the data's {@code turn} is clockwise). */
        static final int ROTATION = 3;
        /** The share darkened toward {@link FallingCity#SHADE}. */
        static final int SHADE = 4;
        /** The half's opacity (1 until the sinking city takes over). */
        static final int ALPHA = 5;
        /** The rims' fade-in, 0 to 1. */
        static final int FADE_IN = 6;

        static final int SLOTS = 7;

        private static final int U = 0;
        private static final int X = 1;
        private static final int Y = 2;
        private static final int KEY_SCALE = 3;
        private static final int TURN = 4;
        private static final int KEY_SHADE = 5;

        final FallingCityData.Fall fall;
        private final LevelData level;
        /** Per half its keys, {@code [u, x, y, scale, turn, shade]}. */
        private final double[][][] keys;

        private final double[] x0;
        private final long[] bottom;
        private final int[] width;
        private final int[] height;
        /** The planned ground scroll at the fall's t. */
        private final double groundAtFall;
        /**
         * The planned ground's travel since the fall's t every {@link #STEP} s ({@link LevelData#scrollAt} is not free of
         * allocation), the last step's speed beyond.
         */
        private final double[] travel;

        static final double STEP = 1 / 60.0;

        final double lowAir;
        final double deep;
        final double[] balloonX;
        final long[] balloonBottom;
        final double[] balloonFps;
        final double[] spireX;
        final long[] spireBottom;
        final double[] spireFps;
        final double[] toppleAt;
        final double sinkingX;
        final long sinkingBottom;
        private final double sinkFrom;
        private final double sinkFade;
        private final double shardsFrom;
        private final double shardsTo;

        Schedule(LevelData level) {
            this.level = level;
            BackdropData data = level.backdrop();
            fall = data.fall().orElseThrow();
            int count = fall.halves().size();
            keys = new double[count][][];
            x0 = new double[count];
            bottom = new long[count];
            width = new int[count];
            height = new int[count];
            for (int h = 0; h < count; h++) {
                FallingCityData.Half half = fall.halves().get(h);
                keys[h] = half.keys().stream()
                        .map(key ->
                                key.stream().mapToDouble(Double::doubleValue).toArray())
                        .toArray(double[][]::new);
                x0[h] = half.x0();
                bottom[h] = half.bottom();
                var size = data.pieces().get(half.piece()).size();
                width[h] = (int) Math.round(size.width());
                height[h] = (int) Math.round(size.height());
            }
            groundAtFall = level.scrollAt(fall.t());
            double span = fall.seconds()
                    + fall.sinking().map(s -> s.from() + s.fade()).orElse(0.0)
                    + 2;
            travel = new double[(int) Math.ceil(span / STEP) + 2];
            for (int i = 0; i < travel.length; i++) {
                travel[i] = level.scrollAt(fall.t() + i * STEP) - groundAtFall;
            }
            lowAir = data.factor(BackdropLayer.LOW_AIR);
            deep = data.factor(BackdropLayer.DEEP);
            List<FallingCityData.Placed> balloons = fall.balloons().orElse(List.of());
            balloonX = new double[balloons.size()];
            balloonBottom = new long[balloons.size()];
            balloonFps = new double[balloons.size()];
            for (int b = 0; b < balloons.size(); b++) {
                FallingCityData.Placed placed = balloons.get(b);
                balloonX[b] = placed.x();
                balloonBottom[b] = bottomOf(data, placed.piece(), placed.t());
                balloonFps[b] = data.pieces().get(placed.piece()).fps().orElse(0.0);
            }
            List<FallingCityData.Spire> spires = fall.spires().orElse(List.of());
            spireX = new double[spires.size()];
            spireBottom = new long[spires.size()];
            spireFps = new double[spires.size()];
            toppleAt = new double[spires.size()];
            for (int s = 0; s < spires.size(); s++) {
                FallingCityData.Spire spire = spires.get(s);
                spireX[s] = spire.x();
                spireBottom[s] = bottomOf(data, spire.piece(), spire.t());
                spireFps[s] = data.pieces().get(spire.piece()).fps().orElse(0.0);
                toppleAt[s] = spire.toppleAt();
            }
            FallingCityData.Sinking sink = fall.sinking().orElse(null);
            sinkingX = sink == null ? 0 : sink.x();
            sinkingBottom = sink == null ? 0 : bottomOf(data, sink.piece(), sink.t());
            sinkFrom = sink == null ? Double.POSITIVE_INFINITY : sink.from();
            sinkFade = sink == null ? 0 : sink.fade();
            shardsFrom = fall.shards().map(FallingCityData.Shards::from).orElse(Double.NaN);
            shardsTo = fall.shards().map(FallingCityData.Shards::to).orElse(Double.NaN);
        }

        /** A piece placed by {@code t} on its layer: its bottom row, as {@link BackdropPieces} places it. */
        private long bottomOf(BackdropData data, String piece, double t) {
            BackdropData.Piece spec = data.pieces().get(piece);
            double centre = level.scrollAt(t) * data.factor(spec.layer()) + HEIGHT / 2.0;
            return Math.round(centre - spec.size().height() / 2);
        }

        /** Whether the halves are the ground at level time {@code script}. */
        boolean falling(double script) {
            return script >= fall.t();
        }

        /**
         * Half {@code h} at level time {@code script} with the ground at {@code groundScroll} into {@code out} ({@link
         * #SLOTS}): its place follows its keys relative to its ground place at the fall's t (the ground's own rows at
         * the hand-over, on whole pixels while it is neither scaled nor turned), so at {@code t} it lies exactly where
         * the city's pieces did.
         */
        void half(int h, double script, double groundScroll, double[] out) {
            double u = Math.max(0, script - fall.t());
            double[][] k = keys[h];
            double kx = at(k, u, X);
            double ky = at(k, u, Y);
            double scale = at(k, u, KEY_SCALE);
            double turn = at(k, u, TURN);
            // How far the half has fallen behind (or ahead of) the ground since t, screen px down.
            double lag = (ky - k[0][Y]) - travelled(u);
            double left = x0[h] + (kx - k[0][X]);
            long ground = bottom[h] - Math.round(groundScroll);
            boolean exact = scale == 1 && turn == 0;
            out[LEFT] = exact ? Math.round(left) : left;
            out[BOTTOM] = ground - (exact ? Math.round(lag) : lag);
            out[SCALE] = scale;
            out[ROTATION] = -turn;
            out[SHADE] = at(k, u, KEY_SHADE);
            out[ALPHA] = 1 - Math.clamp((u - sinkFrom) / sinkFade, 0, 1);
            out[FADE_IN] = Math.clamp(u / RIM_FADE, 0, 1);
        }

        /** The planned ground's travel {@code u} s after the fall's t, px. */
        double travelled(double u) {
            double at = u / STEP;
            int i = (int) Math.floor(at);
            int last = travel.length - 1;
            if (i >= last) {
                return travel[last] + (travel[last] - travel[last - 1]) * (at - last);
            }
            return travel[i] + (travel[i + 1] - travel[i]) * (at - i);
        }

        /** The halves' place at the fall's t: {@link #half} at {@code t} with the ground as the data plans it. */
        void handOver(int h, double[] out) {
            half(h, fall.t(), groundAtFall, out);
        }

        /** Half {@code h}'s image size, px. */
        int width(int h) {
            return width[h];
        }

        int height(int h) {
            return height[h];
        }

        /** Half {@code h}'s ground rows' bottom. */
        long bottom(int h) {
            return bottom[h];
        }

        double x0(int h) {
            return x0[h];
        }

        int halves() {
            return keys.length;
        }

        /**
         * The shards at level time {@code script}: false outside their span; else {@code out}'s {@link #LEFT} and
         * {@link #BOTTOM} are their centre (field x, screen y up) between the two halves' cut edges (the west half's
         * right edge, the east half's left), {@link #SCALE} and {@link #SHADE} the halves' mean, {@link #ALPHA} the
         * fade at the span's ends; {@code scratch} holds a half's place.
         */
        boolean shards(double script, double groundScroll, double[] out, double[] scratch) {
            double u = script - fall.t();
            if (Double.isNaN(shardsFrom) || u < shardsFrom || u > shardsTo || keys.length < 2) {
                return false;
            }
            half(0, script, groundScroll, scratch);
            double westScale = scratch[SCALE];
            double westX = scratch[LEFT] + width[0] / 2.0 * (1 + westScale);
            double westY = scratch[BOTTOM] + height[0] / 2.0;
            double westShade = scratch[SHADE];
            half(1, script, groundScroll, scratch);
            double eastScale = scratch[SCALE];
            double eastX = scratch[LEFT] + width[1] / 2.0 * (1 - eastScale);
            double eastY = scratch[BOTTOM] + height[1] / 2.0;
            out[LEFT] = (westX + eastX) / 2;
            out[BOTTOM] = (westY + eastY) / 2;
            out[SCALE] = (westScale + eastScale) / 2;
            out[SHADE] = (westShade + scratch[SHADE]) / 2;
            double fade = Math.min(u - shardsFrom, shardsTo - u) / SHARD_FADE;
            out[ALPHA] = Math.clamp(fade, 0, 1) * scratch[ALPHA];
            return out[ALPHA] > 0;
        }

        /** The sinking city's opacity at level time {@code script}: 0, then up to 1 over its fade as the halves go. */
        double sinkingAlpha(double script) {
            if (Double.isInfinite(sinkFrom)) {
                return 0;
            }
            return Math.clamp((script - fall.t() - sinkFrom) / sinkFade, 0, 1);
        }

        /** A one-shot's frame {@code seconds} after it starts (frame 0 before), its last frame held. */
        static int oneShot(double seconds, double fps, int count) {
            if (seconds <= 0 || fps <= 0) {
                return 0;
            }
            return (int) Math.min(count - 1, Math.floor(seconds * fps));
        }

        /** Column {@code c} of keys {@code k} at {@code u}: linear between keys, held before the first and after the last. */
        static double at(double[][] k, double u, int c) {
            if (u <= k[0][U]) {
                return k[0][c];
            }
            int last = k.length - 1;
            if (u >= k[last][U]) {
                return k[last][c];
            }
            int lo = 0;
            int hi = last;
            while (hi - lo > 1) {
                int mid = (lo + hi) >>> 1;
                if (k[mid][U] <= u) {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            double share = (u - k[lo][U]) / (k[hi][U] - k[lo][U]);
            return k[lo][c] + (k[hi][c] - k[lo][c]) * share;
        }
    }
}
