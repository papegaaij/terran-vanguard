package vanguard.sim;

import java.util.List;

/**
 * The pickups of a {@link Sortie}: dropped by kills and destroyed objects ({@link #drop}), placed by the
 * script on the level clock, drifting down or pulled by a Pickup magnet, sliding off rock, and collected:
 * credits (with the Salvage scanner's factor), overdrive, shield cells, armour patches, special charges
 * and data cores.
 */
final class PickupPasses {
    private final Sortie s;
    private final LevelScript script;
    private final Rules rules;
    private final Ship ship;
    private final PlayerFire fire;
    private final SpecialSlot special;
    private final SimEvents events;
    private final Radio radio;
    private final Tally tally;
    private final Pool<Pickup> pickups;
    /** M7 part C: the rock with the scroll now; null without terrain. */
    private final Canyon canyon;
    /** A pickup's life, steps. */
    private final int pickupTicks;
    /** The fitted Pickup magnet's reach, px; 0 without one. */
    private final double magnetRadius;
    /** How far a pickup in the magnet's reach flies per step, px; 0 without a magnet. */
    private final double magnetStep;
    /** The Salvage scanner's factor on salvage pickups and hidden crates: 1 + its bonus, 1 without one. */
    private final double salvageFactor;
    /** Per secret, its data core when it is one (Level 06's settlement log); null for a crate. */
    private final LevelResult.DataCore[] cores;
    /** Per secret, whether its data core was collected in this attempt. */
    private final boolean[] coresCollected;
    /** Per secret, its name, for the core's radio line. */
    private final String[] secretNames;
    /** M5 part F: the steps the placed pickups enter at, in time order. */
    private final int[] placedTicks;

    private int nextPlaced;

    PickupPasses(
            Sortie s,
            LevelScript script,
            Loadout loadout,
            Rules rules,
            Ship ship,
            PlayerFire fire,
            SpecialSlot special,
            SimEvents events,
            Radio radio,
            Tally tally,
            Pool<Pickup> pickups,
            Canyon canyon) {
        this.s = s;
        this.script = script;
        this.rules = rules;
        this.ship = ship;
        this.fire = fire;
        this.special = special;
        this.events = events;
        this.radio = radio;
        this.tally = tally;
        this.pickups = pickups;
        this.canyon = canyon;
        cores = new LevelResult.DataCore[script.secrets()];
        coresCollected = new boolean[script.secrets()];
        secretNames = new String[script.secrets()];
        for (GroundObjectSpec object : script.groundObjects()) {
            if (object.secretIndex() >= 0) {
                secretNames[object.secretIndex()] = object.secret();
                cores[object.secretIndex()] = object.core().orElse(null);
            }
        }
        pickupTicks = SimStep.ticks(rules.pickups().seconds());
        magnetRadius = loadout.magnet().map(Magnet::radius).orElse(0.0);
        magnetStep = loadout.magnet().map(Magnet::pullSpeed).orElse(0.0) * SimStep.SECONDS;
        salvageFactor = 1 + loadout.salvageBonus();
        placedTicks = new int[script.drops().placedPickups().size()];
        for (int i = 0; i < placedTicks.length; i++) {
            placedTicks[i] = SimStep.ticks(script.drops().placedPickups().get(i).t());
        }
    }

    /** A new attempt: no data core collected, no placed pickup let in. */
    void reset() {
        java.util.Arrays.fill(coresCollected, false);
        nextPlaced = 0;
    }

    /** Per data core of the level, whether it was collected. */
    void addCoresTo(StateHash hash) {
        for (int k = 0; k < cores.length; k++) {
            if (cores[k] != null) {
                hash.add(coresCollected[k] ? 1 : 0);
            }
        }
    }

    /** M5 part F: only in a level with placed pickups, so the other levels hash as before. */
    void addPlacedTo(StateHash hash) {
        if (placedTicks.length > 0) {
            hash.add(nextPlaced);
        }
    }

    /** The data cores collected in this attempt, in the level's secret order. */
    List<LevelResult.DataCore> dataCores() {
        List<LevelResult.DataCore> collected = new java.util.ArrayList<>();
        for (int k = 0; k < cores.length; k++) {
            if (cores[k] != null && coresCollected[k]) {
                collected.add(cores[k]);
            }
        }
        return collected;
    }

    /**
     * Pickups drift down; with a Pickup magnet fitted, those within its reach of the flying ship fly
     * at it instead (design/player/systems). Every pickup is pulled, the hidden crates and data cores
     * too; the beacons, containers and other ground objects that release them are not pickups.
     */
    void driftPickups() {
        // M7 part G: with the scroll's sign (up the screen while it runs reversed).
        double distance = s.travel() * rules.pickups().driftSpeed() * SimStep.SECONDS;
        boolean pulling = magnetStep > 0 && s.flying();
        boolean vortices = s.force.pulls();
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            if (vortices && pickup.heldBy() >= 0) {
                // M7 part E: a held pickup circles its holder (released at its death, carried off at its escape).
                Enemy holder = s.force.bySerial(pickup.heldBy());
                if (holder != null) {
                    pickup.circle(holder.x(), holder.y());
                    continue;
                }
                pickup.release(pickupTicks);
            }
            boolean magnet = pulling && pickup.within(ship.x(), ship.y(), magnetRadius);
            Enemy vortex = vortices ? strongestPull(pickup) : null;
            boolean stays;
            if (vortex != null
                    && (!magnet || vortex.spec().pull().get().pickupSpeed() * SimStep.SECONDS > magnetStep)) {
                // M7 part E: the stronger pull wins (the magnet a tie); a pickup it draws in close enough is held.
                stays = pickup.pull(
                        vortex.x(), vortex.y(), vortex.spec().pull().get().pickupSpeed() * SimStep.SECONDS);
                if (pickup.within(vortex.x(), vortex.y(), Pickup.HOLD_RADIUS)) {
                    pickup.hold(vortex.serial(), vortex.x(), vortex.y());
                    stays = true;
                }
            } else {
                stays = magnet ? pickup.pull(ship.x(), ship.y(), magnetStep) : pickup.drift(distance);
            }
            if (!stays) {
                if (pickup.type() == PickupType.STORY && s.story != null) {
                    // M9 part G: the core drifted off the screen or ran out of time: the tug's line.
                    s.story.missed(pickup.x(), pickup.y());
                }
                pickups.free(i);
            } else if (canyon != null) {
                slideOffRock(pickup);
            }
        }
    }

    /**
     * M7 part E: the unit whose pull reaches {@code pickup} with the most speed (the first of equals); null for
     * none. A unit pulls while on the play field and not stunned by the EMP Burst.
     */
    private Enemy strongestPull(Pickup pickup) {
        Enemy strongest = null;
        double most = 0;
        Pool<Enemy> enemies = s.force.enemies();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy.spec().pull().isEmpty()
                    || enemy.empStunned()
                    || !PlayField.overlaps(enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            EnemySpec.Pull pull = enemy.spec().pull().get();
            if (pull.pickupSpeed() > most && pickup.within(enemy.x(), enemy.y(), pull.pickupRadius())) {
                strongest = enemy;
                most = pull.pickupSpeed();
            }
        }
        return strongest;
    }

    /**
     * M7 part E (design/enemies/ground/dust-devil, Death): unit {@code holder} died: the pickups it held are loose
     * again where they circled, each with a whole pickup life ({@link SimEvents.Type#PICKUPS_RELEASED} at it, with
     * how many, when there was one).
     */
    void release(Enemy holder) {
        int released = 0;
        for (int i = 0; i < pickups.size(); i++) {
            Pickup pickup = pickups.get(i);
            if (pickup.heldBy() == holder.serial()) {
                pickup.release(pickupTicks);
                released++;
            }
        }
        if (released > 0) {
            events.add(SimEvents.Type.PICKUPS_RELEASED, holder.x(), holder.y(), released);
        }
    }

    /** M7 part E: unit {@code holder} left the screen alive: the pickups it held go with it. */
    void carryOff(Enemy holder) {
        for (int i = pickups.size() - 1; i >= 0; i--) {
            if (pickups.get(i).heldBy() == holder.serial()) {
                if (pickups.get(i).type() == PickupType.STORY && s.story != null) {
                    s.story.missed(pickups.get(i).x(), pickups.get(i).y());
                }
                pickups.free(i);
            }
        }
    }

    /**
     * M7 part C: a pickup slides off rock this fast, px/s: out within 0.3 s from as deep as rock reaches in
     * that time (design/campaign Level 16, What walls stop; default pending the user's review).
     */
    static final double PICKUP_SLIDE_SPEED = 400;

    /** M7 part C: a pickup on rock slides across onto the flyable side, then drifts as usual. */
    private void slideOffRock(Pickup pickup) {
        double to = canyon.flyableX(pickup.y(), pickup.x(), Sortie.PICKUP_ROCK_MARGIN);
        if (to != pickup.x()) {
            double most = PICKUP_SLIDE_SPEED * SimStep.SECONDS;
            pickup.slide(Math.clamp(to - pickup.x(), -most, most));
        }
    }

    /**
     * Drops a pickup of {@code type} at ({@code x}, {@code y}), as a kill or a destroyed object does;
     * package-private for the tests. Returns whether it dropped (a special charge without a special
     * fitted does not, nor one the pool has no room for).
     */
    boolean drop(PickupType type, double x, double y) {
        if (type == PickupType.SPECIAL_CHARGE && !special.fitted()) {
            return false;
        }
        Pickup pickup = pickups.obtain();
        if (pickup == null) {
            return false;
        }
        int credits =
                switch (type) {
                    case SMALL_SALVAGE -> rules.pickups().smallSalvageCredits();
                    case MEDIUM_SALVAGE -> rules.pickups().mediumSalvageCredits();
                    case LARGE_SALVAGE -> rules.pickups().largeSalvageCredits();
                    default -> 0;
                };
        pickup.drop(type, credits, x, y, pickupTicks);
        return true;
    }

    /**
     * M5 part F: the placed pickups due now enter at the top edge (Level 12's armour patch); M5 part G:
     * or appear at their point (Level 13's supply drones' patches), with their event.
     */
    void placePickups() {
        while (nextPlaced < placedTicks.length && placedTicks[nextPlaced] <= s.levelTick) {
            DropSpec.PlacedPickup placed = script.drops().placedPickups().get(nextPlaced++);
            if (drop(placed.pickup(), placed.x(), placed.y())) {
                events.add(
                        SimEvents.Type.PICKUP_PLACED,
                        placed.x(),
                        placed.y(),
                        placed.pickup().ordinal());
            }
        }
    }

    void collectPickups() {
        double radius = rules.pickups().collectionRadius();
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            if (pickup.heldBy() >= 0) {
                // M7 part E: a held pickup is out of reach until its holder dies.
                continue;
            }
            double dx = pickup.x() - ship.x();
            double dy = pickup.y() - ship.y();
            boolean touched = pickup.type() == PickupType.STORY
                    // M9 part G: the core's crate is collected by flying through its box.
                    ? StoryPickups.touches(pickup.x(), pickup.y(), ship.x(), ship.y(), radius)
                    : dx * dx + dy * dy <= radius * radius;
            if (touched) {
                apply(pickup);
                pickups.free(i);
            }
        }
    }

    private void apply(Pickup pickup) {
        Defences defences = ship.defences();
        switch (pickup.type()) {
            case SMALL_SALVAGE, MEDIUM_SALVAGE, LARGE_SALVAGE -> payPickup(CreditSource.SALVAGE, pickup);
            case OVERDRIVE -> fire.overdrive(SimStep.ticks(rules.pickups().overdriveSeconds()));
            case HIDDEN_CRATE -> payPickup(CreditSource.SECRETS, pickup);
            case SHIELD_CELL -> defences.restoreShield(rules.pickups().shieldCellShare() * defences.maxShield());
            case ARMOUR_PATCH -> defences.repair(rules.pickups().armourPatch());
            case SPECIAL_CHARGE -> special.collect();
            case DATA_CORE -> {
                coresCollected[pickup.credits()] = true;
                radio.cue(CueTrigger.SECRET, secretNames[pickup.credits()]);
            }
            case STORY -> {
                // M9 part G: no credits, no secret; its own event and cue (after the pickup's event below).
            }
        }
        events.add(
                SimEvents.Type.PICKUP_COLLECTED,
                pickup.x(),
                pickup.y(),
                pickup.type().ordinal());
        if (pickup.type() == PickupType.STORY && s.story != null) {
            s.story.collected(pickup.x(), pickup.y());
        }
    }

    /**
     * A salvage pickup or a hidden crate: its credits times the Salvage scanner's factor (one
     * rounding with the credit factor), scored at its plain value.
     */
    private void payPickup(CreditSource source, Pickup pickup) {
        int paid = tally.earn(source, pickup.credits(), salvageFactor);
        tally.scoreValue(pickup.credits());
        events.add(SimEvents.Type.CREDITS_PICKED_UP, pickup.x(), pickup.y(), paid);
    }
}
