package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;
import java.util.List;
import java.util.Map;
import vanguard.sim.Enemy;
import vanguard.sim.EnemyBullet;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

/**
 * Which sprite an enemy bullet is drawn with (design/enemies, bullet readability rules: shape
 * encodes threat): the standard orb for {@code small} bullets (tools/art/enemy_bullets.py) and the
 * large pulsing orb for {@code medium} ones (tools/art/bullet_medium.py; the Leviathan's and the
 * Scuttler's aimed shots), told apart by the damage the bullet class gives them. The Spore Bomber's
 * {@code medium} is its spore, drawn as the mine. The yellow needle is the fast speed class's look;
 * no Act 1 attack is in that class, so it has no user yet. M8 part D: a unit kind may fire its own look
 * ({@link #OWN}: the Barbfin's violet needle, tools/art/enemy_bullets.py {@code violet}, at its 16 headings
 * along its flight). A bullet does not tell who fired it, so after each step ({@link #stepped}) each bullet
 * is followed from the last step by its place, and a new one takes its look from the unit that fired
 * beside it in that step. M10 part A: a bullet a mirror bounced back (design/enemies, Reflect) is the magenta
 * {@code orb-bounced} at its heading.
 */
public final class BulletLooks {
    /**
     * The {@code medium} bullet class's damage (design/enemies/data.yaml, player damage; checked
     * against the data by a test): a bullet that deals this much or more is a large orb, and its
     * shot sounds heavy ({@code FlightSounds}).
     */
    public static final double MEDIUM_DAMAGE = 6;
    /** The small orb pulses its core at 15 fps, the large orb at 10 fps; each bullet at its own phase. */
    private static final int SMALL_FRAME_TICKS = 4;

    private static final int MEDIUM_FRAME_TICKS = 6;

    /** M8 part D: the unit kinds (by slug) whose bullets have a look of their own, and that look's sprite. */
    static final Map<String, String> OWN = Map.of(BarbfinLooks.SLUG, "needle-violet");
    /** A new bullet belongs to a unit that fired within this distance of where it left, px. */
    private static final double FIRED_REACH = 24;
    /** An enemy volley's event lies within this distance of the unit that fired it, px. */
    private static final double EVENT_REACH = 8;

    private final Array<AtlasRegion> small;
    private final Array<AtlasRegion> medium;
    /**
     * M10 part A (design/enemies, Reflect): a bullet a mirror bounced out of a player shot, the magenta orb with its
     * wake at its {@value #BOUNCED_HEADINGS} headings ({@code orb-bounced}, tools/art/act6_gear.py); the small orb until
     * its art is on the pages.
     */
    private final Array<AtlasRegion> bounced;

    static final String BOUNCED = "orb-bounced";
    static final int BOUNCED_HEADINGS = 16;
    private final Sprites sprites;
    /** The own looks in use (index 1 on; 0 is none) and per enemy kind its look's index; built at the first step. */
    private final Array<Array<AtlasRegion>> own = new Array<>();

    private int[] kindLook;
    private boolean anyOwn;
    /** Per bullet (by its index after the last step) its own look's index, 0 for none. */
    private byte[] tags = new byte[256];

    private int tagged;
    /** The last step's bullets: where they ended and their looks; the open-addressed table of their places. */
    private double[] lastX = new double[256];

    private double[] lastY = new double[256];
    private byte[] lastTag = new byte[256];
    private int[] table = new int[512];

    BulletLooks(Sprites sprites) {
        this.sprites = sprites;
        small = sprites.orb;
        medium = sprites.frames("orb-medium");
        bounced = sprites.has(BOUNCED) ? sprites.frames(BOUNCED) : null;
    }

    /** A new attempt: no bullet followed. */
    void restart() {
        tagged = 0;
    }

    /** M8 part D: after each step, each bullet's look: followed from the last step, or taken from its shooter. */
    void stepped(Sortie sortie) {
        if (kindLook == null) {
            noteKinds(sortie.enemyKinds());
        }
        if (!anyOwn) {
            return;
        }
        int count = sortie.bulletCount();
        int mask = buildTable();
        if (tags.length < count) {
            tags = new byte[Math.max(count, tags.length * 2)];
        }
        for (int i = 0; i < count; i++) {
            EnemyBullet bullet = sortie.bullet(i);
            double x = bullet.renderX(0);
            double y = bullet.renderY(0);
            int found = find(x, y, mask);
            tags[i] = found >= 0 ? lastTag[found] : shooter(sortie, x, y);
        }
        if (lastX.length < count) {
            int size = Math.max(count, lastX.length * 2);
            lastX = new double[size];
            lastY = new double[size];
            lastTag = new byte[size];
        }
        for (int i = 0; i < count; i++) {
            EnemyBullet bullet = sortie.bullet(i);
            lastX[i] = bullet.renderX(1);
            lastY[i] = bullet.renderY(1);
            lastTag[i] = tags[i];
        }
        tagged = count;
    }

    private void noteKinds(List<EnemySpec> kinds) {
        kindLook = new int[kinds.size()];
        own.add(new Array<>(0));
        for (int k = 0; k < kinds.size(); k++) {
            String sprite = OWN.get(kinds.get(k).slug());
            if (sprite != null && sprites.has(sprite)) {
                own.add(sprites.frames(sprite));
                kindLook[k] = own.size - 1;
                anyOwn = true;
            }
        }
    }

    private static int hash(double x, double y) {
        long bits = Double.doubleToLongBits(x) * 0x9E3779B97F4A7C15L ^ Double.doubleToLongBits(y);
        bits ^= bits >>> 29;
        bits *= 0xBF58476D1CE4E5B9L;
        return (int) (bits ^ (bits >>> 32));
    }

    /** The last step's bullets by their places; returns the table's mask. */
    private int buildTable() {
        int size = table.length;
        while (size < tagged * 2) {
            size *= 2;
        }
        if (size != table.length) {
            table = new int[size];
        } else {
            java.util.Arrays.fill(table, 0);
        }
        int mask = size - 1;
        for (int j = 0; j < tagged; j++) {
            int at = hash(lastX[j], lastY[j]) & mask;
            while (table[at] != 0) {
                at = (at + 1) & mask;
            }
            table[at] = j + 1;
        }
        return mask;
    }

    /** The last step's bullet that ended at (x, y), -1 for none (a new bullet). */
    private int find(double x, double y, int mask) {
        if (tagged == 0) {
            return -1;
        }
        int at = hash(x, y) & mask;
        while (table[at] != 0) {
            int j = table[at] - 1;
            if (lastX[j] == x && lastY[j] == y) {
                return j;
            }
            at = (at + 1) & mask;
        }
        return -1;
    }

    /** A new bullet's look: that of the unit with an own look that fired beside its start in this step; 0 for none. */
    private byte shooter(Sortie sortie, double x, double y) {
        SimEvents events = sortie.events();
        int best = 0;
        double nearest = FIRED_REACH;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            int look = enemy.kind() < kindLook.length ? kindLook[enemy.kind()] : 0;
            if (look == 0) {
                continue;
            }
            double d = Math.hypot(enemy.renderX(1) - x, enemy.renderY(1) - y);
            if (d > nearest || !fired(events, enemy)) {
                continue;
            }
            nearest = d;
            best = look;
        }
        return (byte) best;
    }

    private static boolean fired(SimEvents events, Enemy enemy) {
        for (int e = 0; e < events.size(); e++) {
            if (events.type(e) == SimEvents.Type.ENEMY_FIRED
                    && Math.abs(events.x(e) - enemy.renderX(1)) <= EVENT_REACH
                    && Math.abs(events.y(e) - enemy.renderY(1)) <= EVENT_REACH) {
                return true;
            }
        }
        return false;
    }

    /** The heading index in a set of {@code headings} for a bullet's flight (0 straight down, clockwise). */
    static int heading(EnemyBullet bullet, int headings) {
        return EnemyLooks.heading(bullet.heading() - Math.PI, headings);
    }

    /** The frame of bullet number {@code index} at step {@code tick}. */
    AtlasRegion frame(EnemyBullet bullet, int index, long tick) {
        if (bounced != null && bullet.bounced()) {
            // M10 part A: a bounced shot flies magenta along its own heading, whoever fired beside it.
            return bounced.get(heading(bullet, bounced.size));
        }
        if (index < tagged && tags[index] > 0) {
            Array<AtlasRegion> look = own.get(tags[index]);
            return look.get(heading(bullet, look.size));
        }
        if (bullet.damage() >= MEDIUM_DAMAGE) {
            return medium.get((int) ((tick / MEDIUM_FRAME_TICKS + index) % medium.size));
        }
        return small.get((int) ((tick / SMALL_FRAME_TICKS + index) % small.size));
    }
}
