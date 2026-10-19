package vanguard.sim;

import java.util.List;

/**
 * The target-the-objective hook of an {@link EnemyForce} (design/enemies): per enemy kind whether and how
 * its aimed attacks go at the convoy's units (an escort's convoy, a defend primary's objective, M7 part D's
 * ground allies) instead of the ship, whom an attack goes at now, and the hits the hooked attacks deal the
 * ground allies in a step. Without a convoy nothing is hooked.
 */
final class ObjectiveHook {
    private final List<EnemySpec> kinds;
    private final SimEvents events;
    /** The convoy the target-the-objective hook aims at; null without one. */
    private Convoy convoy;
    /**
     * Per enemy kind, its hook mode ({@code nearest} for an escort's hooked kinds; M5 part G: a defend
     * primary's modes); null without a convoy.
     */
    private LevelScript.HookMode[] modes;
    /**
     * M7 part D: per convoy unit, the hits the hooked attacks dealt it since the last {@link EnemyForce#move} began
     * (bomb runs, the direct hits of the lobs aimed at it, D3's rolls), which the sortie applies; empty
     * without ground allies.
     */
    private double[] allyHits = new double[0];

    ObjectiveHook(List<EnemySpec> kinds, SimEvents events) {
        this.kinds = kinds;
        this.events = events;
    }

    /** The convoy the hook aims at; null without one. */
    Convoy convoy() {
        return convoy;
    }

    /** The ground allies' hits of the step start over (nothing without ground allies). */
    void clearAllyHits() {
        if (allyHits.length > 0) {
            java.util.Arrays.fill(allyHits, 0);
        }
    }

    /**
     * The target-the-objective hook in mode {@code nearest} (design/enemies): the aimed attacks of
     * the enemies {@code slugs} go at the ship or the nearest unit of {@code target}, whichever is
     * closer, chosen every step and as each shot is fired.
     */
    void hook(Convoy target, List<String> slugs) {
        convoy = target;
        modes = new LevelScript.HookMode[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            modes[k] = slugs.contains(kinds.get(k).slug()) ? LevelScript.HookMode.NEAREST : LevelScript.HookMode.NONE;
        }
    }

    /**
     * M5 part G (design/campaign Level 13, What hurts the relay; user decision G1 = a): the hook of a
     * defend primary, {@code target} its ally, each enemy kind in its {@link LevelScript.Defend#mode
     * mode}.
     */
    void hook(Convoy target, LevelScript.Defend defend) {
        hook(target, defend.targetedBy());
    }

    /**
     * The hook of {@code target} with each enemy kind in its mode in {@code targetedBy} (not listed: not
     * hooked): a defend primary's (M5 part G) or (M7 part D) the ground allies'.
     */
    void hook(Convoy target, java.util.Map<String, LevelScript.HookMode> targetedBy) {
        convoy = target;
        modes = new LevelScript.HookMode[kinds.size()];
        for (int k = 0; k < kinds.size(); k++) {
            modes[k] = targetedBy.getOrDefault(kinds.get(k).slug(), LevelScript.HookMode.NONE);
        }
        allyHits = target.groundAllies() ? new double[target.size()] : new double[0];
    }

    /**
     * M7 part D: convoy unit {@code k} takes {@code hits} from a hooked attack this step (the sortie applies
     * them after the moves, {@link #allyHits(int)}); D3's Shellback roll crossing a dome adds its {@link
     * AllySpec#rolls()} here. Nothing without ground allies.
     */
    void hitAlly(int k, double hits) {
        if (k >= 0 && k < allyHits.length && hits > 0) {
            allyHits[k] += hits;
        }
    }

    /** M7 part D: the hits the hooked attacks dealt ground ally {@code k} since the last {@link EnemyForce#move} began. */
    double allyHits(int k) {
        return k < allyHits.length ? allyHits[k] : 0;
    }

    /** M7 part D: whether the hook aims at ground allies (Level 17's domes). */
    boolean groundHook() {
        return convoy != null && convoy.groundAllies();
    }

    /**
     * M7 part D (design/campaign Level 17, What goes for the domes): whether {@code enemy}, just let in, flies
     * a bomb run: a mine layer of a kind the ground allies' hook names {@code always}, entering on its own
     * path (not a carrier's escort).
     */
    boolean bombs(Enemy enemy, Spawn spawn) {
        return groundHook()
                && mode(enemy.kind()) == LevelScript.HookMode.ALWAYS
                && enemy.spec().gun().isPresent()
                && enemy.spec().gun().get().mine().isPresent()
                && spawn.escort().isEmpty()
                && spawn.walk().isEmpty()
                && spawn.release().isEmpty()
                && !spawn.lane()
                && spawn.field().isEmpty()
                && enemy.spec().burrow().isEmpty();
    }

    /** The hook mode of enemy kind {@code kind}; {@link LevelScript.HookMode#NONE} without a convoy. */
    LevelScript.HookMode mode(int kind) {
        return modes == null ? LevelScript.HookMode.NONE : modes[kind];
    }

    /**
     * M7 part D (design/enemies/ground/shellback, Curl and roll; design/allies): a rolling ball touching a standing
     * ground ally's footprint crushes it: one hit per roll per ally (its {@link AllySpec#rolls()}), whatever the
     * hook says (easy's unhooked Shellbacks crush too), applied with the step's other hooked hits. Round 40 R.c:
     * the footprint (a dome's concrete ring), not the hit box (its glass), so a walk routed beside a dome, its body
     * clear of the glass, still lets a roll along it hit the dome once.
     */
    void crush(Enemy enemy) {
        Hitbox ball = enemy.hitbox();
        for (int k = 0; k < convoy.size(); k++) {
            Ally ally = convoy.get(k);
            if (enemy.crushedAlly(k)
                    || !ally.alive()
                    || !ball.overlaps(enemy.x(), enemy.y(), convoy.spec(k).size(), ally.x(), ally.y())) {
                continue;
            }
            enemy.markCrushed(k);
            events.add(SimEvents.Type.ROLL_CRUSHED, enemy.x(), enemy.y(), k);
            hitAlly(k, convoy.spec(k).rolls());
        }
    }

    /**
     * Whom a unit's aimed attack goes at now: the convoy unit the hook picks, or -1 for the ship
     * (units without the hook, fans and mines always aim at the ship).
     */
    int target(Enemy enemy, Ship ship) {
        LevelScript.HookMode mode = mode(enemy.kind());
        if (mode == LevelScript.HookMode.ALWAYS) {
            // M5 part G: every attack of the unit (a fan, a lob, a turret's barrel) at the objective.
            return enemy.spec().gun().isPresent()
                            && enemy.spec().gun().get().mine().isEmpty()
                    ? convoy.objective(enemy.x(), enemy.y())
                    : -1;
        }
        if (mode != LevelScript.HookMode.NEAREST || enemy.spec().gun().isEmpty()) {
            return -1;
        }
        EnemyGun gun = enemy.spec().gun().get();
        if (gun.fan() != 1 || gun.mine().isPresent()) {
            return -1;
        }
        return convoy.nearest(enemy.x(), enemy.y(), ship.x(), ship.y());
    }

    /**
     * M5 part G, the {@code alternate} hook (user decision G1 = a): whom the shot {@link Enemy#trigger()}
     * let go goes at. A burst's first shot starts an attack: every second attack goes at the objective
     * while it is closer than the ship (centre to centre), the others at the ship; the burst's other
     * shots go where its first went.
     */
    int alternate(Enemy enemy, Ship ship) {
        if (enemy.burstShot() == 0) {
            int target = -1;
            boolean second = enemy.startAttack() % 2 == 0;
            if (second && groundHook()) {
                // M7 part D: the nearest ground ally on the screen while it is closer than the ship.
                target = convoy.nearestCloser(enemy.x(), enemy.y(), ship.x(), ship.y());
            } else if (second) {
                int objective = convoy.objective();
                if (objective >= 0) {
                    Ally ally = convoy.get(objective);
                    double ox = ally.x() - enemy.x();
                    double oy = ally.y() - enemy.y();
                    double sx = ship.x() - enemy.x();
                    double sy = ship.y() - enemy.y();
                    target = ox * ox + oy * oy < sx * sx + sy * sy ? objective : -1;
                }
            }
            enemy.attackTarget(target);
        }
        return enemy.attackTarget();
    }

    /**
     * M5 part G, the {@code in-arc} hook: the objective a walker's fan goes at, when it can be aimed at
     * and lies within the walker's armour's arc of its facing (design/enemies/ground/scuttler: ±45°);
     * -1 otherwise.
     */
    int inArc(Enemy enemy) {
        int objective = convoy.objective(enemy.x(), enemy.y());
        if (objective < 0) {
            return -1;
        }
        Ally ally = convoy.get(objective);
        double bearing = StrictMath.atan2(-(ally.x() - enemy.x()), -(ally.y() - enemy.y()));
        double off = Math.abs(Math.IEEEremainder(bearing - enemy.facing(), 2 * StrictMath.PI));
        return off <= enemy.spec().walker().orElseThrow().frontArc() ? objective : -1;
    }
}
