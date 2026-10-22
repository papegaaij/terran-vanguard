package vanguard.game.render;

import static vanguard.game.render.PlayFrame.drawCentred;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import vanguard.content.BackdropData;
import vanguard.content.HazardsBlock;
import vanguard.content.LevelData;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyGun;
import vanguard.sim.FurnaceVents;
import vanguard.sim.GroundObjectSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M7 part F: Level 19's foundry looks (design/campaign Level 19; tools/art/l19_props.py's sprites, ART-F4): on the
 * ground, scrolling with it ({@link #drawGround}): the furnace vents' grates (shut; red, orange and white-hot over the
 * tell; white-hot under the flame), the shift office round the payroll safe, the ingot stacks beside the last belt run
 * and the pallets riding it at the belt's speed, the steel truss gantry across the field under each group of up-facing
 * nests (a nest stands about {@value #NEST_ABOVE_GANTRY} px above its centre line) and every up-facing nest's cradle
 * under its Spine Turret (the deck and chevrons pointing up the screen), and the crews' rovers leaving the smelter
 * tower with their headlights. Over the ground units ({@link #drawFire}): a grate's heat glow and shimmer rings over
 * its tell, its flame column over its burn, then its smoke puff. The belts' moving plates are the backdrop's animated
 * piece; the conveyed wrecks are their units' looks' ({@link TankLooks}, {@link WalkerLooks}).
 *
 * <p>Places on the ground are kept as {@code Y}: the screen's y (up) plus the ground's scroll, so a point is drawn at
 * {@code Y - scroll}. A unit that enters at the top edge at level time t lies at {@code scrollAt(t) + 540 + h/2}; a
 * backdrop piece at {@code pieceCentre}.
 *
 * <p>M8 part B: the vents' {@code look} names their sprites ({@link #ventSprites}): Level 22's ice geysers
 * ({@code ice-geyser*}, tools/art/l22_props.py) draw a crack in the ice for the grate, its lime-white glow over the
 * tell, a steam column for the flame and the residue mist for the smoke; the steam is drawn with normal blending over
 * the ground units (stepped translucency, not additive light).
 */
final class FoundryLooks {
    /** The art's grate states: 0 shut, 1-3 the tell's steps (red, orange, white-hot). */
    static final int GRATE_FRAMES = 4;
    /** The tell's glow frames (its steps), the burn's flame frames (8 fps over 1.0 s), the smoke's (8 fps). */
    static final int GLOW_FRAMES = 3;

    static final int FLAME_FRAMES = 8;
    static final int SMOKE_FRAMES = 6;
    /** The smoke's frames show this many steps each (8 fps). */
    static final int SMOKE_FRAME_TICKS = SimStep.ticks(1 / 8.0);
    /** An up-facing nest stands this far above its gantry's centre line, px (tools/art/l19_props.py's review). */
    static final double NEST_ABOVE_GANTRY = 26;
    /** A nest is the cradle's when it stands within this of it, px (its live place refines the computed one). */
    static final double CRADLE_REACH = 12;
    /**
     * A ground target whose look stands inside another sprite of its own: the payroll safe inside the shift office,
     * its centre this far right of and above the safe's (tools/art/l19_props.py; vanguard.pipeline.SpriteUse claims
     * the surround with the look).
     */
    static final Map<String, Surround> TRIGGER_SURROUNDS = Map.of("payroll-safe", new Surround("shift-office", 16, 22));
    /** The ingot stacks beside the last belt run: where along it (its share of the run), which side, which stack. */
    static final double[] INGOT_ALONG = {0.23, 0.35, 0.58, 0.69};

    static final int[] INGOT_SIDE = {1, -1, 1, -1};
    static final String[] INGOT_STACK = {"ingot-stack-a", "ingot-stack-b", "ingot-stack-c", "ingot-stack-a"};
    /** A stack stands this far beside its belt's centre line, px (the belt is 48 px wide, a stack 48 px). */
    static final double INGOT_BESIDE = 56;
    /** The pallets on the last run, this far apart along it, px; none within this of a run's end (its drum). */
    static final double PALLET_SPACING = 150;

    static final double PALLET_END_CLEAR = 30;
    /** The crews' rovers: how many, their speed over the ground, px/s, and how long after the tower passes mid-screen. */
    static final int ROVERS = 3;

    static final double ROVER_SPEED = 60;
    static final double ROVER_START_SECONDS = 3;
    /** Each rover leaves this long after the one before it, s. */
    static final double ROVER_GAP_SECONDS = 0.8;
    /** The rovers' road from the tower's centre (x, Y offsets): out of its door, across to the belt's side, up the field. */
    static final double[][] ROVER_ROAD = {{100, 0}, {270, 40}, {270, 900}};
    /** The smelter tower's backdrop piece, which the crews leave. */
    static final String TOWER = "smelter-tower";

    /** What a look stands in, and where its centre is against the look's, px (x right, y up). */
    record Surround(String sprite, double dx, double dy) {}

    /** A cradle under an up-facing nest at (x, Y). */
    private static final class Cradle {
        final double x;
        double groundY;

        Cradle(double x, double groundY) {
            this.x = x;
            this.groundY = groundY;
        }
    }

    /** A single sprite standing on the ground at (x, Y). */
    record Prop(AtlasRegion region, double x, double groundY) {}

    /** A sprite by its name standing on the ground at (x, Y). */
    record Place(String sprite, double x, double groundY) {}

    private final Array<AtlasRegion> grate;
    private final Array<AtlasRegion> glow;
    private final Array<AtlasRegion> flame;
    private final Array<AtlasRegion> smoke;
    /** M8 part B: the burn's column is steam, drawn with normal blending (the ice geyser's), not additive flame. */
    private final boolean steam;

    private final AtlasRegion gantry;
    private final AtlasRegion cradle;
    private final AtlasRegion pallet;
    private final Array<AtlasRegion> rover;
    private final Array<AtlasRegion> roverLights;
    /** The gantries' centre lines, Y. */
    private final double[] gantries;
    /** The up-facing nests' cradles. */
    private final List<Cradle> cradles = new ArrayList<>();
    /** The office round the safe and the ingot stacks. */
    private final List<Prop> props = new ArrayList<>();
    /** The last belt run's x, its ends (Y) and its speed (px/s up the screen); NaN x without belts. */
    private final double palletX;

    private final double palletFrom;
    private final double palletTo;
    private final double palletSpeed;
    /** The tower the rovers leave: its centre (x, Y) and the level time they start; NaN without one. */
    private final double towerX;

    private final double towerY;
    private final double roverStart;
    /** Per grate the step its smoke started (its burn ended), and the phase it was in at the last step. */
    private long[] smokeTick = new long[0];

    private FurnaceVents.Phase[] lastPhase = new FurnaceVents.Phase[0];

    private static final long NEVER = Long.MIN_VALUE / 2;

    private FoundryLooks(Sprites sprites, LevelData level, LevelScript script) {
        String[] names = ventSprites(ventLook(level));
        grate = optional(sprites, names[0]);
        glow = optional(sprites, names[1]);
        flame = optional(sprites, names[2]);
        smoke = optional(sprites, names[3]);
        steam = isGeyser(ventLook(level));
        gantry = sprites.has("gantry") ? sprites.region("gantry") : null;
        cradle = sprites.has("gantry-cradle") ? sprites.region("gantry-cradle") : null;
        pallet = sprites.has("ingot-pallet") ? sprites.region("ingot-pallet") : null;
        rover = optional(sprites, "crew-rover");
        roverLights = optional(sprites, "crew-rover-lights");
        // The up-facing nests' cradles, where the simulation sets them down (refined by their live place).
        for (double[] place : cradlePlaces(level, script)) {
            cradles.add(new Cradle(place[0], place[1]));
        }
        gantries = cradle == null || cradles.isEmpty() ? new double[0] : gantryLines(level, nestHeight(script));
        // The office round the safe, the ingot stacks beside the last belt run.
        for (Place place : places(level, script)) {
            if (sprites.has(place.sprite())) {
                props.add(new Prop(sprites.region(place.sprite()), place.x(), place.groundY()));
            }
        }
        // The pallets riding the last belt run.
        GroundSpec.Belt last = lastBelt(script);
        if (last != null) {
            palletX = last.x();
            palletFrom = last.from() + PlayField.HEIGHT / 2.0;
            palletTo = last.to() + PlayField.HEIGHT / 2.0;
            palletSpeed = last.speed();
        } else {
            palletX = Double.NaN;
            palletFrom = 0;
            palletTo = 0;
            palletSpeed = 0;
        }
        // The rovers leave the level's last smelter tower.
        BackdropData.PlacedPiece tower = null;
        for (BackdropData.PlacedPiece placed : level.backdrop().placements()) {
            if (placed.piece().equals(TOWER) && (tower == null || placed.t() > tower.t())) {
                tower = placed;
            }
        }
        if (tower != null && !rover.isEmpty() && !script.ground().belts().isEmpty()) {
            towerX = tower.x();
            towerY = level.pieceCentre(tower);
            roverStart = tower.t() + ROVER_START_SECONDS;
        } else {
            towerX = Double.NaN;
            towerY = 0;
            roverStart = 0;
        }
        restart();
    }

    /** The level's foundry looks, or null for a level with no vents, belts, up-facing nests or surrounds. */
    static FoundryLooks of(Sprites sprites, LevelData level, LevelScript script) {
        boolean any = !script.hazards().vents().isEmpty()
                || !script.ground().belts().isEmpty()
                || script.groundUnits().stream()
                        .anyMatch(unit -> upFacing(unit.enemy().gun().orElse(null)))
                || script.groundObjects().stream().anyMatch(object -> TRIGGER_SURROUNDS.containsKey(object.look()));
        return any ? new FoundryLooks(sprites, level, script) : null;
    }

    /** M8 part B: the level's vents' look ({@code furnace-vent} without vents or a look of their own). */
    static String ventLook(LevelData level) {
        return level.vents().map(HazardsBlock.Vents::lookOrDefault).orElse(HazardsBlock.Vents.FURNACE_VENT);
    }

    /** Whether a vents look is the ice geyser's: a steam column drawn with normal blending, not a flame. */
    static boolean isGeyser(String look) {
        return look.equals(HazardsBlock.Vents.ICE_GEYSER);
    }

    /**
     * M8 part B: a vents look's sprites: the grate (crack), its tell's glow, the burn's column (flame or steam) and what
     * stays after the burn (smoke or residue); the furnace vent's {@code furnace-vent}, {@code -glow}, {@code -flame},
     * {@code -smoke}, the ice geyser's {@code ice-geyser}, {@code -glow}, {@code -steam}, {@code -residue}.
     */
    static String[] ventSprites(String look) {
        boolean geyser = isGeyser(look);
        return new String[] {
            look, look + "-glow", look + (geyser ? "-steam" : "-flame"), look + (geyser ? "-residue" : "-smoke")
        };
    }

    private static Array<AtlasRegion> optional(Sprites sprites, String name) {
        return sprites.has(name) ? sprites.frames(name) : new Array<>();
    }

    /** Whether a gun is turned to face up the screen ({@code arc: up}, its arc's facing π). */
    static boolean upFacing(EnemyGun gun) {
        return gun != null && Math.abs(gun.arcFacingRadians() - Math.PI) < 1e-6;
    }

    /** The up-facing nests' cradles (x, Y) where the simulation sets the nests down at the top edge. */
    static List<double[]> cradlePlaces(LevelData level, LevelScript script) {
        List<double[]> places = new ArrayList<>();
        for (GroundUnit unit : script.groundUnits()) {
            if (upFacing(unit.enemy().gun().orElse(null))
                    && unit.side().isEmpty()
                    && unit.burst().isEmpty()) {
                places.add(new double[] {
                    unit.x(), entryY(level, unit.t(), unit.enemy().hitbox().height())
                });
            }
        }
        return places;
    }

    /** The up-facing nests' height, px (their hit box's); 0 without them. */
    static double nestHeight(LevelScript script) {
        return script.groundUnits().stream()
                .filter(unit -> upFacing(unit.enemy().gun().orElse(null)))
                .mapToDouble(unit -> unit.enemy().hitbox().height())
                .findFirst()
                .orElse(0);
    }

    /** The level's last belt run (the latest to come), or null without belts. */
    static GroundSpec.Belt lastBelt(LevelScript script) {
        return script.ground().belts().stream()
                .max((a, b) -> Double.compare(a.from(), b.from()))
                .orElse(null);
    }

    /** The single props on the ground: the surrounds of their looks (the office round the safe), the ingot stacks. */
    static List<Place> places(LevelData level, LevelScript script) {
        List<Place> places = new ArrayList<>();
        for (GroundObjectSpec object : script.groundObjects()) {
            Surround surround = TRIGGER_SURROUNDS.get(object.look());
            if (surround != null) {
                double y = entryY(level, object.t(), object.size().height());
                places.add(new Place(surround.sprite(), object.x() + surround.dx(), y + surround.dy()));
            }
        }
        GroundSpec.Belt last = lastBelt(script);
        if (last != null) {
            double from = last.from() + PlayField.HEIGHT / 2.0;
            double to = last.to() + PlayField.HEIGHT / 2.0;
            for (int i = 0; i < INGOT_ALONG.length; i++) {
                places.add(new Place(
                        INGOT_STACK[i], last.x() + INGOT_SIDE[i] * INGOT_BESIDE, from + INGOT_ALONG[i] * (to - from)));
            }
        }
        return places;
    }

    /**
     * Where a unit {@code height} px tall entering at the top edge at level time {@code t} lies on the ground (Y): set
     * down at the top edge in the step its time comes, then carried down that step's scroll with the ground.
     */
    static double entryY(LevelData level, double t, double height) {
        return level.scrollAt((SimStep.ticks(t) - 1) * SimStep.SECONDS) + PlayField.HEIGHT + height / 2;
    }

    /**
     * The gantries' centre lines (Y): one under each group of up-facing nests in the level's data (its medium
     * placements), {@value #NEST_ABOVE_GANTRY} px below the group's nests on average; the nests on the smelter stacks
     * (a group named for its stack) stand on the backdrop's stacks, not on a gantry.
     */
    static double[] gantryLines(LevelData level, double nestHeight) {
        List<Double> lines = new ArrayList<>();
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.arc().isEmpty()
                    || target.at().isEmpty()
                    || target.target().contains("stack")) {
                continue;
            }
            double sum = 0;
            for (LevelData.Placement placement : target.at()) {
                sum += entryY(level, placement.t(), nestHeight);
            }
            lines.add(sum / target.at().size() - NEST_ABOVE_GANTRY);
        }
        return lines.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /** The level restarts: no smoke rises. */
    void restart() {
        Arrays.fill(smokeTick, NEVER);
        Arrays.fill(lastPhase, FurnaceVents.Phase.IDLE);
    }

    // --- the frames --------------------------------------------------------------------------------

    /** A grate's frame: shut while idle, the tell's three steps by its share, white-hot under the flame. */
    static int grateFrame(FurnaceVents.Phase phase, double progress) {
        return switch (phase) {
            case IDLE -> 0;
            case TELL -> 1 + tellStep(progress);
            case BURN -> GRATE_FRAMES - 1;
        };
    }

    /** The tell's step (0-2) {@code progress} into it: red, orange, white-hot (0.4 s each at medium). */
    static int tellStep(double progress) {
        return Math.clamp((int) Math.floor(progress * GLOW_FRAMES), 0, GLOW_FRAMES - 1);
    }

    /** The flame column's frame {@code progress} into its burn (8 fps over 1.0 s; a longer burn spreads them). */
    static int flameFrame(double progress) {
        return Math.clamp((int) Math.floor(progress * FLAME_FRAMES), 0, FLAME_FRAMES - 1);
    }

    /** The smoke's frame {@code age} steps after its burn ended; -1 once it has gone. */
    static int smokeFrame(long age) {
        if (age < 0 || age >= (long) SMOKE_FRAMES * SMOKE_FRAME_TICKS) {
            return -1;
        }
        return (int) (age / SMOKE_FRAME_TICKS);
    }

    /**
     * Where the pallet {@code k} rides on a run from {@code from} to {@code to} (Y) after {@code seconds} on the belt's
     * clock at {@code speed} px/s: spaced {@value #PALLET_SPACING} px, coming round from the run's start; NaN while it
     * is within {@value #PALLET_END_CLEAR} px of an end (under its drum's housing).
     */
    static double palletY(int k, double from, double to, double speed, double seconds) {
        double length = to - from;
        double along = Math.floorMod((long) Math.floor(k * PALLET_SPACING + speed * seconds), (long) length);
        if (along < PALLET_END_CLEAR || along > length - PALLET_END_CLEAR) {
            return Double.NaN;
        }
        return from + along;
    }

    /** The pallets on a run of {@code length} px. */
    static int pallets(double length) {
        return (int) Math.floor(length / PALLET_SPACING);
    }

    /**
     * A rover {@code driven} px along its road from the tower's centre: its place (x, Y offsets) and its heading
     * (radians clockwise from straight down) in {@code out}.
     */
    static void roverAt(double driven, double[] out) {
        double left = Math.max(0, driven);
        double x = ROVER_ROAD[0][0];
        double y = ROVER_ROAD[0][1];
        double heading = 0;
        for (int i = 1; i < ROVER_ROAD.length; i++) {
            double dx = ROVER_ROAD[i][0] - ROVER_ROAD[i - 1][0];
            double dy = ROVER_ROAD[i][1] - ROVER_ROAD[i - 1][1];
            double length = Math.hypot(dx, dy);
            heading = Math.atan2(-dx, -dy);
            double step = Math.min(left, length);
            x += dx * step / length;
            y += dy * step / length;
            left -= step;
            if (left <= 0) {
                break;
            }
        }
        out[0] = x;
        out[1] = y;
        out[2] = heading;
    }

    // --- following the simulation ------------------------------------------------------------------

    /** After each simulation step: a grate whose burn ended starts its smoke; a live nest refines its cradle's place. */
    void stepped(Sortie sortie) {
        FurnaceVents vents = sortie.vents();
        if (smokeTick.length != vents.grates()) {
            smokeTick = new long[vents.grates()];
            lastPhase = new FurnaceVents.Phase[vents.grates()];
            restart();
        }
        for (int g = 0; g < vents.grates(); g++) {
            FurnaceVents.Phase phase = vents.phase(g);
            if (lastPhase[g] == FurnaceVents.Phase.BURN && phase != FurnaceVents.Phase.BURN) {
                smokeTick[g] = sortie.tick();
            }
            lastPhase[g] = phase;
        }
        double scroll = sortie.groundScroll();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (!enemy.grounded() || !upFacing(enemy.spec().gun().orElse(null))) {
                continue;
            }
            double groundY = enemy.renderY(1) + scroll;
            for (Cradle near : cradles) {
                if (Math.abs(near.x - enemy.renderX(1)) < 0.5 && Math.abs(near.groundY - groundY) < CRADLE_REACH) {
                    near.groundY = groundY;
                }
            }
        }
    }

    // --- drawing -----------------------------------------------------------------------------------

    /**
     * On the ground, under the ground units: the grates, the office and the ingot stacks, the pallets riding the last
     * belt, the gantries and the nests' cradles, the rovers with their headlights.
     *
     * @param scroll the ground's scroll at the render time
     * @param seconds the level clock at the render time (the rovers)
     * @param real the real clock at the render time (the belt's pallets, as its plates)
     */
    void drawGround(SpriteBatch batch, Sortie sortie, double scroll, double seconds, double real) {
        FurnaceVents vents = sortie.vents();
        double ventLag = scroll - sortie.groundScroll();
        if (!grate.isEmpty()) {
            for (int g = 0; g < vents.grates(); g++) {
                if (vents.present(g)) {
                    int frame = grateFrame(vents.phase(g), vents.progress(g));
                    drawCentred(batch, grate.get(frame % grate.size), vents.x(g), vents.y(g) - ventLag);
                }
            }
        }
        for (Prop prop : props) {
            drawOnScreen(batch, prop.region(), prop.x(), prop.groundY() - scroll);
        }
        if (pallet != null && !Double.isNaN(palletX)) {
            int count = pallets(palletTo - palletFrom);
            for (int k = 0; k < count; k++) {
                double y = palletY(k, palletFrom, palletTo, palletSpeed, real);
                if (!Double.isNaN(y)) {
                    drawOnScreen(batch, pallet, palletX, y - scroll);
                }
            }
        }
        if (gantry != null) {
            for (double line : gantries) {
                drawOnScreen(batch, gantry, PlayField.WIDTH / 2.0, line - scroll);
            }
        }
        if (cradle != null) {
            for (Cradle under : cradles) {
                drawOnScreen(batch, cradle, under.x, under.groundY - scroll);
            }
        }
        drawRovers(batch, scroll, seconds);
    }

    private void drawRovers(SpriteBatch batch, double scroll, double seconds) {
        if (Double.isNaN(towerX)) {
            return;
        }
        double[] at = new double[3];
        for (int pass = 0; pass < 2; pass++) {
            Array<AtlasRegion> set = pass == 0 ? rover : roverLights;
            if (set.isEmpty()) {
                continue;
            }
            if (pass == 1) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            }
            for (int r = 0; r < ROVERS; r++) {
                double driving = seconds - roverStart - r * ROVER_GAP_SECONDS;
                if (driving < 0) {
                    continue;
                }
                roverAt(driving * ROVER_SPEED, at);
                int heading = EnemyLooks.heading(at[2], set.size);
                drawOnScreen(batch, set.get(heading), towerX + at[0], towerY + at[1] - scroll);
            }
            if (pass == 1) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    /** A sprite centred at (x, y) when some of it is on the screen. */
    private static void drawOnScreen(SpriteBatch batch, AtlasRegion region, double x, double y) {
        double half = Math.max(region.getRegionWidth(), region.getRegionHeight()) / 2.0;
        if (y + half < 0 || y - half > PlayField.HEIGHT) {
            return;
        }
        drawCentred(batch, region, x, y);
    }

    /**
     * Over the ground units: each glowing grate's heat glow and shimmer rings (additive), each burning grate's flame
     * column (additive), then the smoke puffs after the burns.
     */
    void drawFire(SpriteBatch batch, Sortie sortie, double scroll) {
        FurnaceVents vents = sortie.vents();
        if (vents.grates() == 0) {
            return;
        }
        double lag = scroll - sortie.groundScroll();
        long tick = sortie.tick();
        if (!smoke.isEmpty()) {
            for (int g = 0; g < vents.grates() && g < smokeTick.length; g++) {
                int frame = smokeFrame(tick - smokeTick[g]);
                if (frame >= 0 && vents.present(g)) {
                    drawCentred(batch, smoke.get(frame % smoke.size), vents.x(g), vents.y(g) - lag);
                }
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (int g = 0; g < vents.grates(); g++) {
            if (!vents.present(g)) {
                continue;
            }
            double x = vents.x(g);
            double y = vents.y(g) - lag;
            switch (vents.phase(g)) {
                case TELL -> {
                    if (!glow.isEmpty()) {
                        drawCentred(batch, glow.get(tellStep(vents.progress(g)) % glow.size), x, y);
                    }
                }
                case BURN -> {
                    if (!flame.isEmpty() && !steam) {
                        drawCentred(batch, flame.get(flameFrame(vents.progress(g)) % flame.size), x, y);
                    }
                }
                case IDLE -> {}
            }
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (steam && !flame.isEmpty()) {
            // M8 part B: the ice geyser's steam column, normal blending over the ground units.
            for (int g = 0; g < vents.grates(); g++) {
                if (vents.present(g) && vents.phase(g) == FurnaceVents.Phase.BURN) {
                    drawCentred(
                            batch, flame.get(flameFrame(vents.progress(g)) % flame.size), vents.x(g), vents.y(g) - lag);
                }
            }
        }
    }
}
