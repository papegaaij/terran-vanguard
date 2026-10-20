package vanguard.content;

import vanguard.sim.Enemy;
import vanguard.sim.GroundObject;
import vanguard.sim.Layer;
import vanguard.sim.PlayField;
import vanguard.sim.Sondes;
import vanguard.sim.Sortie;
import vanguard.sim.Tendril;

/**
 * The autopilot's targets (see {@link Autopilot}): what to line up under besides the lowest enemy, level by level
 * (Level 05's batteries, Level 06's chain heads, Level 09's Hive Nodes and the Airstrike, Level 04's convoy, Level 10's
 * Wraith ambushes and ferry hatch, Level 12's sondes, Level 13's guard, Level 14's growths, Level 15's Burrowers,
 * Level 16's gate anchors, Level 18's Dust Devils) and the ground and pickups when the air is clear.
 */
final class TargetPlay {
    private TargetPlay() {}

    /**
     * Level 09: a hardened target this close to the bottom edge, px, with more HP than {@link
     * #STRIKE_HP} gets the Airstrike, if one is fitted and charged.
     */
    private static final double STRIKE_LOW = 170;

    private static final double STRIKE_HP = 15;

    /** The Airstrike's bombers fly this far either side of the ship's x (design/player/specials). */
    private static final double STRIKE_OFFSET = 64;

    /** Two targets this far apart sideways, px, both lie under one strike's two bombers. */
    private static final double STRIKE_PAIR_MIN = 80;

    private static final double STRIKE_PAIR_MAX = 190;

    /** Level 10: the Airstrike goes to an ambush of at least this many Wraiths (the larger ones, later in the level). */
    private static final int STRIKE_AMBUSH = 3;

    /** M5 part G: a walker counts as aiming at the defended ally while it lies this far either side of its facing, rad. */
    private static final double GUARD_ARC = Math.toRadians(60);

    /**
     * M7 part E (design/campaign Level 18; Varga: "Shoot the core when the spin opens"): a Dust Devil with its vanes
     * closed is no target (every shot glances off, and lining up under it holds the ship in its pull); a rising one
     * is, and one in the second half of its closed time, so the ship stands under it when the core opens ({@link
     * #openVortex}: the lowest of them above the ship comes first).
     */
    /** M7 part E: from this share of a Dust Devil's closed time on it lines up under it for the window. */
    private static final double VORTEX_LINE_UP = 0.5;

    static Enemy openVortex(Sortie sortie, double shipY) {
        Enemy best = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.spec().vortex().isPresent()
                    && enemy.hp() > 0
                    && !closedVortex(enemy)
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 40
                    && (best == null || y < best.renderY(1))) {
                best = enemy;
            }
        }
        return best;
    }

    static boolean closedVortex(Enemy enemy) {
        return enemy.spec().vortex().isPresent()
                && enemy.armoured()
                && enemy.vortexPhase() != Enemy.VortexPhase.RISING
                && !(enemy.vortexPhase() == Enemy.VortexPhase.CLOSED && enemy.vortexProgress() > VORTEX_LINE_UP);
    }

    /**
     * M5 part F: the weather sonde to fly through: the lowest one drifting on the screen; -1 for none
     * (a level without sondes has none).
     */
    static int sondeToTake(Sortie sortie, double shipY) {
        Sondes sondes = sortie.sondes();
        int best = -1;
        for (int i = 0; i < sondes.count(); i++) {
            if (sondes.state(i) == Sondes.DRIFTING
                    && sondes.y(i) < PlayField.HEIGHT - 20
                    && (best < 0 || sondes.y(i) < sondes.y(best))) {
                best = i;
            }
        }
        return best;
    }

    /**
     * Level 10: the rear-ambush unit (a Wraith) to line up over: the nearest sideways of those in
     * their flash or hold below the ship, else of those still cloaked below the bottom edge or
     * rising to their hold (their lane is known); null without one.
     */
    static Enemy rearAmbusher(Sortie sortie, double shipY) {
        double shipX = sortie.ship().x();
        Enemy nearest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Enemy.AmbushPhase phase = enemy.ambushPhase();
            boolean coming = phase == Enemy.AmbushPhase.GAP || phase == Enemy.AmbushPhase.RISE;
            if ((holding(enemy) || coming)
                    && enemy.renderY(1) < shipY
                    && (nearest == null
                            || (holding(enemy) && !holding(nearest))
                            || (holding(enemy) == holding(nearest)
                                    && Math.abs(enemy.renderX(1) - shipX) < Math.abs(nearest.renderX(1) - shipX)))) {
                nearest = enemy;
            }
        }
        return nearest;
    }

    /**
     * Level 10: the pair of units of a Wraith ambush of at least {@link #STRIKE_AMBUSH} to call the
     * Airstrike on, if one is fitted, charged and ready: two of its units {@link
     * #STRIKE_PAIR_MIN}–{@link #STRIKE_PAIR_MAX} px apart still cloaked below the bottom edge or
     * rising, the pair whose midpoint lies nearest the ship (each bomber then flies up one's lane;
     * the bombs burst low on the screen just as they decloak there), as {@code first × 1024 +
     * second} (their indices); -1 for none. It is called once both rise.
     */
    static int ambushPair(Sortie sortie) {
        var special = sortie.special();
        if (!special.fitted() || !special.name().equals("Airstrike") || !special.ready() || special.charges() <= 0) {
            return -1;
        }
        int coming = 0;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (coming(sortie.enemy(i))) {
                coming++;
            }
        }
        if (coming < STRIKE_AMBUSH) {
            return -1;
        }
        double shipX = sortie.ship().x();
        int best = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy a = sortie.enemy(i);
            if (!coming(a)) {
                continue;
            }
            for (int j = 0; j < sortie.enemyCount(); j++) {
                Enemy b = sortie.enemy(j);
                double dx = b.renderX(1) - a.renderX(1);
                double distance = Math.abs((a.renderX(1) + b.renderX(1)) / 2 - shipX);
                if (coming(b) && dx >= STRIKE_PAIR_MIN && dx <= STRIKE_PAIR_MAX && distance < bestDistance) {
                    bestDistance = distance;
                    best = i * 1024 + j;
                }
            }
        }
        return best;
    }

    /** Level 10: a rear-ambush unit still cloaked below the bottom edge or rising to its hold point. */
    private static boolean coming(Enemy enemy) {
        return enemy.ambushPhase() == Enemy.AmbushPhase.GAP || enemy.ambushPhase() == Enemy.AmbushPhase.RISE;
    }

    /**
     * Level 05: the lowest unit of a destroy-targets group (a battery) on the screen above the ship,
     * which must die before it leaves; null without one.
     */
    static Enemy battery(Sortie sortie, double shipY) {
        if (sortie.script().objectives().targets().isEmpty()) {
            return null;
        }
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.groupIndex() >= 0
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 20
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    /**
     * Level 09: the lowest hardened unit of a destroy-targets group on the screen (a Hive Node: only
     * {@code anti-ground} deliveries crack it, so it flies over it); null without one.
     */
    static Enemy hardenedTarget(Sortie sortie) {
        if (sortie.script().objectives().targets().isEmpty()) {
            return null;
        }
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.groupIndex() >= 0
                    && enemy.spec().hardened()
                    && y < PlayField.HEIGHT - 20
                    && y > 20
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    /**
     * Level 09: whether to call the Airstrike on a hardened target about to get away (low on the
     * screen with HP left): only with an Airstrike fitted, charged and not already flying.
     */
    static boolean strikeDue(Sortie sortie, Enemy node) {
        var special = sortie.special();
        return special.fitted()
                && special.name().equals("Airstrike")
                && special.ready()
                && special.charges() > 0
                && node.renderY(1) < STRIKE_LOW
                && node.hp() > STRIKE_HP;
    }

    /**
     * Where to call the strike: with another hardened target of a group on the screen
     * {@link #STRIKE_PAIR_MIN}–{@link #STRIKE_PAIR_MAX} px to its side, midway between the two (each
     * bomber then flies over one); otherwise {@link #STRIKE_OFFSET} to its side, toward the centre.
     */
    static double strikeX(Sortie sortie, Enemy node) {
        double x = node.renderX(1);
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy other = sortie.enemy(i);
            double dx = Math.abs(other.renderX(1) - x);
            if (other != node
                    && other.groupIndex() >= 0
                    && other.spec().hardened()
                    && other.renderY(1) < PlayField.HEIGHT - 20
                    && dx >= STRIKE_PAIR_MIN
                    && dx <= STRIKE_PAIR_MAX) {
                return (x + other.renderX(1)) / 2;
            }
        }
        return x < PlayField.WIDTH / 2.0 ? x + STRIKE_OFFSET : x - STRIKE_OFFSET;
    }

    /** Level 06: the lowest head of a segment chain on the screen above the ship; null without one. */
    static Enemy chainHead(Sortie sortie, double shipY) {
        Enemy lowest = null;
        for (int c = 0; c < sortie.chainCount(); c++) {
            Enemy head = sortie.chain(c).member(0);
            if (head == null) {
                continue;
            }
            double x = head.renderX(1);
            double y = head.renderY(1);
            if (x > 0
                    && x < PlayField.WIDTH
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 60
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = head;
            }
        }
        return lowest;
    }

    /**
     * M7 part C: the anchor to shoot of the lowest standing gate coming down above the ship (Level 16): the live
     * anchor nearer the ship; null for none.
     */
    static Enemy gateAnchor(Sortie sortie, double shipX, double shipY) {
        if (sortie.links() == null) {
            return null;
        }
        Tendril lowest = null;
        for (int i = 0; i < sortie.links().size(); i++) {
            Tendril tendril = sortie.links().get(i);
            double y = Math.min(tendril.ay(), tendril.by());
            if (tendril.standing()
                    && y > shipY
                    && y < PlayField.HEIGHT + 20
                    && (lowest == null || y < Math.min(lowest.ay(), lowest.by()))) {
                lowest = tendril;
            }
        }
        if (lowest == null) {
            return null;
        }
        Enemy best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            if (enemy.spec().link().isEmpty()) {
                continue;
            }
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            boolean a = Math.hypot(x - lowest.ax(), y - lowest.ay()) < 4;
            boolean b = Math.hypot(x - lowest.bx(), y - lowest.by()) < 4;
            double distance = Math.abs(x - shipX);
            if ((a || b) && distance < bestDistance) {
                best = enemy;
                bestDistance = distance;
            }
        }
        return best;
    }

    /**
     * With a convoy, the ground enemy on the screen above the ship that is closest to a convoy unit
     * (a turret or a walker about to fire at it or claw it); null without one.
     */
    static Enemy convoyThreat(Sortie sortie, double shipY) {
        if (sortie.allyCount() == 0) {
            return null;
        }
        Enemy best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (enemy.layer() != Layer.GROUND
                    || y > PlayField.HEIGHT - 10
                    || y < shipY - 20
                    || x < 0
                    || x > PlayField.WIDTH) {
                continue;
            }
            for (int k = 0; k < sortie.allyCount(); k++) {
                if (sortie.ally(k).alive()) {
                    double distance = Math.hypot(
                            sortie.ally(k).renderX(1) - x, sortie.ally(k).renderY(1) - y);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = enemy;
                    }
                }
            }
        }
        return best;
    }

    /** M5 part G: whether it guards a defended ally now (the synchronisation running, the ally standing). */
    static boolean guarding(Sortie sortie) {
        return sortie.defending()
                && sortie.defendStarted()
                && sortie.defendSecondsLeft() > 0
                && sortie.allyCount() > 0
                && sortie.ally(0).alive();
    }

    /**
     * M5 part G (design/campaign Level 13): while it guards the defended ally, the hooked unit on the screen to
     * shoot first: a mortar lobbing at the ally, then a diver sent at it before its dive is past, then a walker
     * with the ally within {@link #GUARD_ARC} of its facing, then any other hooked unit; nearest the ally
     * within a rank. Null when nothing hooked is on the screen.
     */
    static Enemy guardTarget(Sortie sortie) {
        if (!guarding(sortie)) {
            return null;
        }
        vanguard.sim.ObjectiveSpec.Defend defend =
                sortie.script().objectives().defend().orElseThrow();
        double allyX = sortie.ally(0).renderX(1);
        double allyY = sortie.ally(0).renderY(1);
        Enemy best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double x = enemy.renderX(1);
            double y = enemy.renderY(1);
            if (x < 8 || x > PlayField.WIDTH - 8 || y < 10 || y > PlayField.HEIGHT - 10 || enemy.cloaked()) {
                continue;
            }
            if (defend.mode(enemy.spec().slug()) == vanguard.sim.HookMode.NONE) {
                continue;
            }
            double distance = Math.hypot(x - allyX, y - allyY);
            int rank;
            if (enemy.spec().gun().flatMap(vanguard.sim.EnemyGun::mortar).isPresent()) {
                rank = 0;
            } else if (enemy.spec().dive().isPresent()) {
                // a diver sent at the ally: until its dive has crossed it
                if (enemy.diving() && y < allyY - 40) {
                    continue;
                }
                rank = 1;
            } else if (enemy.walking()) {
                double toAlly = Math.atan2(-(allyX - x), -(allyY - y));
                double off = Math.abs(Math.IEEEremainder(toAlly - enemy.facing(), 2 * Math.PI));
                rank = off <= GUARD_ARC ? 2 : 3;
            } else {
                rank = 3;
            }
            double score = rank * 10_000 + distance;
            if (score < bestScore) {
                bestScore = score;
                best = enemy;
            }
        }
        return best;
    }

    /**
     * M5 part H: the lowest root-burst growth on the screen above the ship (growing or grown), which must
     * die before it scrolls off for the roots objective; null without one.
     */
    static Enemy growth(Sortie sortie, double shipY) {
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            if (enemy.rootBurst() >= 0
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 15
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        return lowest;
    }

    /**
     * M7 part B: the Burrower to line up under (Level 15): of those bulging or up above the ship, the one whose
     * window closes first (a bulging one's after an up one's); null for none.
     */
    static Enemy burrower(Sortie sortie, double shipY) {
        Enemy best = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            Enemy.BurrowPhase phase = enemy.burrowPhase();
            if ((phase == Enemy.BurrowPhase.BULGE || phase == Enemy.BurrowPhase.SURFACED)
                    && enemy.burrowSpotY() > shipY + 40
                    && (best == null
                            || (phase == Enemy.BurrowPhase.SURFACED
                                    && best.burrowPhase() == Enemy.BurrowPhase.BULGE))) {
                best = enemy;
            }
        }
        return best;
    }

    /** Level 10: a rear-ambush unit at its hold point (flashing or holding): it stays put, so it is lined up over, not dodged. */
    static boolean holding(Enemy enemy) {
        return enemy.ambushPhase() == Enemy.AmbushPhase.DECLOAK || enemy.ambushPhase() == Enemy.AmbushPhase.HOLD;
    }

    /**
     * Level 10: a destructible on the ground that hides a secret (the ferry hatch), above the ship and
     * not yet past the screen's middle, where its crate can still drift down to the ship; null for none.
     */
    static GroundObject secretTarget(Sortie sortie, double shipY) {
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            double y = object.renderY(1);
            if (!object.spent()
                    && object.spec().secretIndex() >= 0
                    && y > Math.max(shipY + 30, PlayField.HEIGHT / 2.0)
                    && y < PlayField.HEIGHT + object.spec().size().height()) {
                return object;
            }
        }
        return null;
    }

    static GroundObject lowestGround(Sortie sortie, double shipY) {
        GroundObject lowest = null;
        for (int i = 0; i < sortie.groundObjectCount(); i++) {
            GroundObject object = sortie.groundObject(i);
            double y = object.renderY(1);
            if (!object.spent()
                    && y > shipY + 30
                    && y < PlayField.HEIGHT
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = object;
            }
        }
        return lowest;
    }
}
