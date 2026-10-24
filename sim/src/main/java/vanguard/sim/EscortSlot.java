package vanguard.sim;

import java.util.Optional;

/**
 * A {@link Sortie}'s escort slot (split out of {@link Sortie}): M5 part A's wingman (Rook) or M8 part A's Bulwark heavy
 * drone, set up for the level (the water, the rock, the gates, the vents), flown each step, hit, and started with
 * their level-start armour at each attempt.
 */
final class EscortSlot {
    private final Sortie s;
    /** M5 part A: the wingman in the escort slot (Rook); null without one. */
    final Wingman wingman;
    /** {@link #wingman} for the presentation, made once (stepping allocates nothing). */
    final Optional<Wingman> wingmanView;
    /** The mount index the wingman's (and the Bulwark's) shots carry: one past the armament's. */
    final int wingmanMount;
    /** The wingman's armour at the start of the next attempt. */
    double wingmanStart;
    /** M8 part A: the Bulwark heavy drone in the escort slot; null without one (then nothing of it is hashed). */
    final Bulwark bulwark;
    /** {@link #bulwark} for the presentation, made once. */
    final Optional<Bulwark> bulwarkView;
    /** M8 part A: the Bulwark's armour at the start of the next attempt. */
    double bulwarkStart;

    EscortSlot(
            Sortie sortie,
            Loadout loadout,
            long seed,
            Medium medium,
            Canyon canyon,
            Links links,
            FurnaceVents vents,
            EnemyForce force) {
        this.s = sortie;
        wingman = loadout.wingman().map(flying -> new Wingman(flying, seed)).orElse(null);
        if (wingman != null) {
            if (medium.water()) {
                // M8 part A: his drag and his gun's layers under water.
                wingman.medium(medium);
            }
            wingman.canyon(canyon);
            wingman.links(links);
            if (vents.grates() > 0) {
                wingman.vents(vents);
            }
        }
        if (sortie.script.rookLoss().isPresent()) {
            if (sortie.checkpointBoss >= 0) {
                throw new IllegalArgumentException("Rook's loss in a level with a boss checkpoint is not supported");
            }
            if (wingman != null) {
                // M8 part F: his scripted loss; without him (no escort, the Bulwark) it is the radio's only.
                wingman.loss().plan(sortie.script.rookLoss().get());
            }
        }
        wingmanView = Optional.ofNullable(wingman);
        wingmanMount = loadout.armament().size();
        wingmanStart = loadout.wingman().map(WingmanSpec::armour).orElse(0.0);
        bulwark = loadout.bulwark().map(flying -> new Bulwark(flying, seed)).orElse(null);
        if (bulwark != null) {
            if (medium.water()) {
                // M8 part A: its drag under water.
                bulwark.medium(medium);
            }
            force.lure(bulwark);
        }
        bulwarkView = Optional.ofNullable(bulwark);
        bulwarkStart = loadout.bulwark().map(BulwarkSpec::armour).orElse(0.0);
    }

    /** See {@link Sortie#retry(double, double)}: the escort starts the next attempt with {@code armour}. */
    void retry(double armour) {
        if (wingman != null) {
            if (!(armour > 0)) {
                throw new IllegalArgumentException("a retry needs the wingman's armour");
            }
            wingmanStart = Math.min(armour, wingman.maxArmour());
        }
        if (bulwark != null) {
            // M8 part A: the Bulwark starts the next attempt with its level-start armour as Rook does.
            if (!(armour > 0)) {
                throw new IllegalArgumentException("a retry needs the Bulwark's armour");
            }
            bulwarkStart = Math.min(armour, bulwark.maxArmour());
        }
    }

    /** At an attempt's start: the escort in its slot beside the ship at (shipX, shipY), at its start armour. */
    void reset(double shipX, double shipY) {
        if (wingman != null) {
            wingman.reset(wingmanStart, shipX, shipY);
        }
        if (bulwark != null) {
            bulwark.reset(bulwarkStart, shipX, shipY);
        }
    }

    /** The escort's step, the wind's {@code push} on the wingman first, the ground scrolling {@code scrollStep} px. */
    void fly(int commands, double push, double scrollStep) {
        if (wingman != null) {
            wingman.wind(push);
            flyWingman(commands, scrollStep);
        }
        if (bulwark != null) {
            flyBulwark(commands);
        }
    }

    /** What hits the escort in this step (the ship flying, the level neither complete nor failed). */
    void hit() {
        if (wingman != null) {
            s.hitPasses.hitWingman();
            if (s.links != null && !wingman.out()) {
                double damage = s.links.touch(wingman.hull(), wingman.x(), wingman.y(), true);
                if (damage > 0) {
                    wingman.hit(damage, s.ship.x(), s.ship.y(), s.events);
                }
            }
        }
        if (bulwark != null) {
            s.hitPasses.hitBulwark(bulwark);
        }
    }

    /**
     * M8 part A: the Bulwark's step: in its slot during the launch; then it flies to its slot and, while the player
     * fires, its cannon fires (its shots carry the escort's mount, {@link Sortie#wingmanMount()}).
     */
    private void flyBulwark(int commands) {
        Ship ship = s.ship;
        bulwark.rememberPosition();
        if (s.launching()) {
            bulwark.formUp(ship.x(), ship.y());
            return;
        }
        bulwark.fly(ship.x(), ship.y());
        if (bulwark.fires(s.flying() && !s.complete && Command.FIRE.in(commands))) {
            s.fire.wingmanVolley(
                    wingmanMount, bulwark.gun(), bulwark.x(), bulwark.y(), -1, s.force.enemies(), s.ground);
        }
    }

    /**
     * The wingman's step: in his slot during the launch; then his AI and, while the player fires,
     * his gun; M8 part F: once lost, his spin instead ({@link RookLoss}).
     */
    private void flyWingman(int commands, double scrollStep) {
        Ship ship = s.ship;
        EnemyForce force = s.force;
        wingman.rememberPosition();
        if (s.launching()) {
            wingman.formUp(ship.x(), ship.y());
            return;
        }
        if (wingman.loss().step(s.levelTick, wingman, ship.x(), scrollStep, s.events)) {
            // M8 part F: lost, he spins down (no AI, no gun).
            return;
        }
        // M7 part G: he keeps out of a pursuit's lunge lane during its tell and its dive.
        Optional<Pursuit> chase = force.flocks().size() > 0 ? force.pursuit() : Optional.empty();
        if (chase.isPresent() && chase.get().lunging()) {
            wingman.lunge(chase.get().laneLeft(), chase.get().laneRight());
        } else {
            wingman.lunge(Double.NaN, Double.NaN);
        }
        if (s.breachBoss != null) {
            // M7 part H: the breaching boss's suction pulls him as it pulls the ship.
            BreachArena breach = s.breachBoss.breach().orElseThrow();
            wingman.pull(breach.pullXAt(wingman.x(), wingman.y()), breach.pullYAt(wingman.x(), wingman.y()));
        }
        wingman.fly(
                ship.x(),
                ship.y(),
                // M7 part F: the walker walking up the screen is a rear wave for him. M7 part G (default pending the
                // user's review): in a reversed scroll no bottom wave puts him in Trail (the bottom is ahead).
                edges(),
                force.enemies(),
                force.bullets(),
                s.setPieces,
                s.fire.recentHit(wingman.recentHitTicks()));
        if (wingman.fires(s.flying() && Command.FIRE.in(commands))) {
            s.fire.wingmanVolley(
                    wingmanMount, wingman.gun(), wingman.x(), wingman.y(), wingman.target(), force.enemies(), s.ground);
        }
    }

    /**
     * The active edges the wingman flies by ({@link EnemyForce#activeEdges()}); M7 part F: a walking boss's rear; M7
     * part G: in a reversed scroll the bottom edge left out (the bottom is ahead: no Trail into the ahead waves), the
     * top one (behind) leaving him in Wing.
     */
    private int edges() {
        EnemyForce force = s.force;
        int edges =
                s.walks || s.breachBoss != null ? force.activeEdges() | s.bossPasses.strideRear() : force.activeEdges();
        return s.clock.travel < 0 ? edges & ~WarningEdge.BOTTOM.bit() : edges;
    }
}
