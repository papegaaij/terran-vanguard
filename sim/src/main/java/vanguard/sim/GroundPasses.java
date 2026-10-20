package vanguard.sim;

import java.util.List;

/**
 * The ground objects and debris chunks of a {@link Sortie}: letting them in on the level clock, scrolling
 * and moving them, the darkness's lit triggers and the sled's clamp, a Shellback's roll crushing them,
 * their destruction (pay, drops, a group's last drop, a secret revealed by one or several triggers) and the
 * rocks a ground unit throws in low gravity (Level 05).
 */
final class GroundPasses {
    /** Thrown rocks' serials start here, above the level's placed chunks. */
    private static final int DEBRIS_THROWN = 1 << 20;

    private final Sortie s;
    private final LevelScript script;
    private final SplitMix64 rng;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Tally tally;
    private final Objectives objectives;
    private final Pool<GroundObject> ground;
    private final Pool<Pickup> pickups;
    private final Pool<Debris> debris;
    /** The mass-driver sleds; null without them. */
    private final Sled sled;
    /** The level's darkness; null in a lit level. */
    private final HazardSpec.Darkness darkness;
    /** The chunk a thrown rock is; null in a level without rocks. */
    final HazardSpec.DebrisSpec rock;
    /** A secret's crate or data core: the level's own life for it. */
    private final int crateTicks;
    /** Per secret, the triggers spent that reveal it together (Level 03's lifeboat lights). */
    final int[] secretTriggersSpent;
    /**
     * M5 part E: per destructible group, its destructibles destroyed (Level 11's floating containers;
     * empty in a level without one).
     */
    final int[] groupDemolished;
    /** Rocks thrown in this attempt, for their serials. */
    int rocksThrown;
    /** The next ground object and debris chunk of the script to let in. */
    int nextGroundObject;

    int nextDebris;

    GroundPasses(
            Sortie s,
            LevelScript script,
            SplitMix64 rng,
            Ship ship,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Tally tally,
            Objectives objectives,
            Pool<GroundObject> ground,
            Pool<Pickup> pickups,
            Pool<Debris> debris,
            Sled sled,
            int crateTicks) {
        this.s = s;
        this.script = script;
        this.rng = rng;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.tally = tally;
        this.objectives = objectives;
        this.ground = ground;
        this.pickups = pickups;
        this.debris = debris;
        this.sled = sled;
        this.crateTicks = crateTicks;
        darkness = script.hazards().darkness().orElse(null);
        rock = script.hazards().rocks().map(HazardSpec.RockSpec::chunk).orElse(null);
        secretTriggersSpent = new int[script.secrets()];
        int destructibleGroups = 0;
        for (GroundObjectSpec object : script.groundObjects()) {
            destructibleGroups = Math.max(destructibleGroups, object.group() + 1);
        }
        groupDemolished = new int[destructibleGroups];
    }

    /** A new attempt: nothing let in, spent, demolished or thrown. */
    void reset() {
        java.util.Arrays.fill(secretTriggersSpent, 0);
        java.util.Arrays.fill(groupDemolished, 0);
        rocksThrown = 0;
        nextGroundObject = 0;
        nextDebris = 0;
    }

    /**
     * The darkness (Level 06): a flare fired now is announced, and the triggers marked dark can be
     * hit only while the headlight or a flare lights them (shots pass them in the dark).
     */
    void light() {
        double seconds = s.levelSeconds();
        List<HazardSpec.Darkness.Flare> flares = darkness.flares();
        for (int i = 0; i < flares.size(); i++) {
            if (s.clockTicked && SimStep.ticks(flares.get(i).t()) == s.levelTick && !s.complete) {
                events.add(SimEvents.Type.FLARE_FIRED, flares.get(i).x(), darkness.flareY(i, seconds), i);
            }
        }
        for (int i = 0; i < ground.size(); i++) {
            GroundObject object = ground.get(i);
            if (object.spec().dark()) {
                object.shut(!darkness.lit(object.x(), object.y(), object.spec().size(), ship.x(), ship.y(), seconds));
            }
        }
    }

    void placeGroundObjects() {
        List<GroundObjectSpec> objects = script.groundObjects();
        while (nextGroundObject < objects.size()
                && SimStep.ticks(objects.get(nextGroundObject).t()) <= s.levelTick) {
            GroundObject object = ground.obtain();
            if (object != null) {
                object.place(objects.get(nextGroundObject), nextGroundObject);
            }
            nextGroundObject++;
        }
    }

    /**
     * Lets in the debris chunks due now, each at its x unless that is closer to the ship than its
     * clearance: then it enters as far to the side, away from the ship.
     */
    void placeDebris() {
        List<HazardSpec.DebrisSpec> specs = script.hazards().debris();
        while (nextDebris < specs.size() && SimStep.ticks(specs.get(nextDebris).t()) <= s.levelTick) {
            Debris chunk = debris.obtain();
            if (chunk != null) {
                HazardSpec.DebrisSpec spec = specs.get(nextDebris);
                chunk.place(spec, nextDebris, clearOfShip(spec));
            }
            nextDebris++;
        }
    }

    /** Where a chunk enters: its x, or moved sideways away from the ship to its clearance. */
    private double clearOfShip(HazardSpec.DebrisSpec spec) {
        double dy = PlayField.HEIGHT + spec.size().height() / 2 - ship.y();
        double dx = spec.x() - ship.x();
        double clearance = spec.clearance();
        if (dx * dx + dy * dy >= clearance * clearance) {
            return spec.x();
        }
        double shift = Math.sqrt(clearance * clearance - dy * dy);
        double half = spec.size().width() / 2;
        double away = Math.clamp(ship.x() + (dx >= 0 ? shift : -shift), half, PlayField.WIDTH - half);
        if (Math.abs(away - ship.x()) >= shift) {
            return away;
        }
        return Math.clamp(ship.x() + (dx >= 0 ? -shift : shift), half, PlayField.WIDTH - half);
    }

    void moveDebris() {
        for (int i = debris.size() - 1; i >= 0; i--) {
            if (!debris.get(i).move()) {
                debris.free(i);
            }
        }
    }

    void scrollGround(double distance) {
        for (int i = ground.size() - 1; i >= 0; i--) {
            if (!ground.get(i).scroll(distance)) {
                ground.free(i);
            }
        }
    }

    /**
     * M7 part D (design/enemies/ground/shellback, Curl and roll): a rolling ball destroys the destructible
     * ground objects its hit box overlaps (a crate's drop still falls; no bounty: the player did not destroy
     * them), each with a {@link SimEvents.Type#ROLL_CRUSHED} (value -1). Never a trigger, a sunken or low-air
     * object, nor (default pending round 40) a hardened one; never enemies or the ship.
     */
    void crushGround() {
        Pool<Enemy> enemies = force.enemies();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (!enemy.rolling() || enemy.empStunned()) {
                continue;
            }
            Hitbox ball = enemy.hitbox();
            for (int j = ground.size() - 1; j >= 0; j--) {
                GroundObject object = ground.get(j);
                GroundObjectSpec spec = object.spec();
                if (!object.hittable()
                        || spec.trigger()
                        || spec.submerged()
                        || spec.lowAir()
                        || spec.hardened()
                        || !ball.overlaps(enemy.x(), enemy.y(), spec.size(), object.x(), object.y())) {
                    continue;
                }
                events.add(SimEvents.Type.ROLL_CRUSHED, object.x(), object.y(), -1);
                demolish(j, false);
            }
        }
    }

    /** How far to the right of a destroyed object its bonus drop lands. */
    private static final double BONUS_DROP_OFFSET = 16;

    void demolish(int index) {
        demolish(index, true);
    }

    /**
     * Destroys ground object {@code index}: its event, its drops and secret; {@code paid} its bounty (M7 part D:
     * not for one a Shellback's roll crushed).
     */
    void demolish(int index, boolean paid) {
        GroundObject object = ground.get(index);
        GroundObjectSpec spec = object.spec();
        events.add(
                SimEvents.Type.GROUND_DESTROYED,
                object.x(),
                object.y(),
                script.groundObjects().indexOf(spec));
        if (paid) {
            tally.bounty(CreditSource.GROUND_TARGETS, spec.bounty());
            tally.scoreValue(spec.bounty());
        }
        if (spec.drop().isPresent()) {
            s.pickupPasses.drop(spec.drop().get(), object.x(), object.y());
        }
        if (spec.bonusDrop().isPresent()) {
            // Beside the first, so both show.
            s.pickupPasses.drop(spec.bonusDrop().get(), object.x() + BONUS_DROP_OFFSET, object.y());
        }
        if (spec.secretIndex() >= 0) {
            // A destructible that hides a secret (Level 04's dugout) reveals it when destroyed.
            released(object);
        }
        if (spec.group() >= 0
                && ++groupDemolished[spec.group()] == spec.groupSize()
                && spec.groupDrop().isPresent()) {
            // M5 part E: the group's last destructible drops its pickup (Level 11's armour patch).
            s.pickupPasses.drop(spec.groupDrop().get(), object.x(), object.y());
        }
        ground.free(index);
    }

    /**
     * A trigger took its last hit: it reveals its secret, or, when several triggers reveal it
     * together, counts towards it and the last one reveals it.
     */
    void released(GroundObject trigger) {
        GroundObjectSpec spec = trigger.spec();
        if (spec.secretTriggers() > 1 && ++secretTriggersSpent[spec.secretIndex()] < spec.secretTriggers()) {
            return;
        }
        revealSecret(trigger);
    }

    private void revealSecret(GroundObject trigger) {
        objectives.secretFound();
        GroundObjectSpec spec = trigger.spec();
        events.add(SimEvents.Type.SECRET_FOUND, trigger.x(), trigger.y());
        Pickup crate = pickups.obtain();
        if (spec.core().isPresent()) {
            // A data core: its line plays when it is collected; the pickup carries its secret's index.
            if (crate != null) {
                crate.drop(PickupType.DATA_CORE, spec.secretIndex(), trigger.x(), trigger.y(), crateTicks);
            }
            return;
        }
        if (crate != null) {
            crate.drop(PickupType.HIDDEN_CRATE, spec.crateCredits(), trigger.x(), trigger.y(), crateTicks);
        }
        radio.cue(CueTrigger.SECRET, spec.secret());
    }

    /**
     * A destroyed ground unit throws its rocks in low gravity (Level 05): a number between the
     * spec's least and most, each drifting in a random direction; none when it lies close to the ship.
     */
    void throwRocks(double x, double y) {
        HazardSpec.RockSpec spec = script.hazards().rocks().orElseThrow();
        int count = spec.min() + rng.nextInt(spec.max() - spec.min() + 1);
        double dx = ship.x() - x;
        double dy = ship.y() - y;
        if (dx * dx + dy * dy < spec.clearance() * spec.clearance()) {
            return;
        }
        for (int k = 0; k < count; k++) {
            double angle = rng.range(0, 2 * StrictMath.PI);
            double speed = rng.range(spec.minSpeed(), spec.maxSpeed());
            Debris chunk = debris.obtain();
            if (chunk != null) {
                chunk.toss(
                        rock,
                        DEBRIS_THROWN + rocksThrown,
                        x,
                        y,
                        Trig.cos(angle) * speed,
                        Trig.sin(angle) * speed,
                        SimStep.ticks(spec.lifeSeconds()));
                events.add(SimEvents.Type.ROCK_THROWN, x, y);
            }
            rocksThrown++;
        }
    }

    /** The rail's sleds: their lights, their launches, and the stuck sled's clamp that only takes hits in the dark. */
    void updateSled() {
        SimEvents.Type event = sled.update(s.levelTick);
        if (event != null) {
            events.add(event, sled.spec().x(), 0);
        }
        String clamp = sled.spec().clampSecret();
        for (int i = 0; i < ground.size(); i++) {
            GroundObject object = ground.get(i);
            if (object.spec().trigger() && object.spec().secret().equals(clamp)) {
                object.shut(sled.lit());
            }
        }
    }
}
