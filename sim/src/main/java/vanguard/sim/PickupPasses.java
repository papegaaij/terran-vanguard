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
        for (LevelScript.GroundObjectSpec object : script.groundObjects()) {
            if (object.secretIndex() >= 0) {
                secretNames[object.secretIndex()] = object.secret();
                cores[object.secretIndex()] = object.core().orElse(null);
            }
        }
        pickupTicks = SimStep.ticks(rules.pickups().seconds());
        magnetRadius = loadout.magnet().map(Magnet::radius).orElse(0.0);
        magnetStep = loadout.magnet().map(Magnet::pullSpeed).orElse(0.0) * SimStep.SECONDS;
        salvageFactor = 1 + loadout.salvageBonus();
        placedTicks = new int[script.placedPickups().size()];
        for (int i = 0; i < placedTicks.length; i++) {
            placedTicks[i] = SimStep.ticks(script.placedPickups().get(i).t());
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
        double distance = rules.pickups().driftSpeed() * SimStep.SECONDS;
        boolean pulling = magnetStep > 0 && s.flying();
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Pickup pickup = pickups.get(i);
            boolean stays = pulling && pickup.within(ship.x(), ship.y(), magnetRadius)
                    ? pickup.pull(ship.x(), ship.y(), magnetStep)
                    : pickup.drift(distance);
            if (!stays) {
                pickups.free(i);
            } else if (canyon != null) {
                slideOffRock(pickup);
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
            LevelScript.PlacedPickup placed = script.placedPickups().get(nextPlaced++);
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
            double dx = pickup.x() - ship.x();
            double dy = pickup.y() - ship.y();
            if (dx * dx + dy * dy <= radius * radius) {
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
                radio.cue(LevelScript.CueTrigger.SECRET, secretNames[pickup.credits()]);
            }
        }
        events.add(
                SimEvents.Type.PICKUP_COLLECTED,
                pickup.x(),
                pickup.y(),
                pickup.type().ordinal());
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
