package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.content.ColossusBlock.BreachChange;
import vanguard.content.ColossusBlock.EruptionChange;
import vanguard.content.ColossusBlock.SuctionChange;
import vanguard.content.EnemyMovementData.Ring;
import vanguard.content.MawBlock.CoilArenaChange;

/**
 * An enemy's difficulty overrides in its stat block ({@link EnemyData}, {@code difficulty:}): the hooks per
 * difficulty and every {@code *Change} record they hold.
 *
 * <p>It only holds these types: {@link EnemyData} implements it, so callers keep naming them {@code EnemyData.X}
 * (Java inherits member types from superinterfaces, JLS 8.5) while the record's file stays small. New code may name
 * the interface directly; an import must (the canonical name: {@code import vanguard.content.EnemyHookData.X}).
 */
public sealed interface EnemyHookData permits EnemyData {
    /** M7 part E: a difficulty's change to a spin cycle: its open window instead (the Dust Devil's easy 1.0 s). */
    record VortexChange(Optional<Double> open) {
        public VortexChange {
            open.ifPresent(t -> Check.positive("open", t));
        }
    }

    /** M7 part E: a difficulty's change to a pull: its speed on the ship instead (the Dust Devil's easy 25, hard 45). */
    record PullChange(Optional<Double> shipSpeed) {
        public PullChange {
            shipSpeed.ifPresent(s -> Check.positive("ship_speed", s));
        }
    }

    /**
     * M7 part G: a difficulty's change to the pursuit formation: its time between lunges or its tell instead, s (the
     * Mote Swarm's easy 8 s and 1.2 s, hard 4.5 s).
     */
    record PursuitChange(Optional<Double> lungeEvery, Optional<Double> tell) {
        public PursuitChange {
            lungeEvery.ifPresent(s -> Check.positive("lunge_every", s));
            tell.ifPresent(s -> Check.positive("tell", s));
        }
    }

    /** M7 part F: a difficulty's change to a turret: its turn rate instead, °/s (the Warden Tank's hard 160). */
    record TurretChange(Optional<Double> turnRate) {
        public TurretChange {
            turnRate.ifPresent(r -> Check.positive("turn_rate", r));
        }
    }

    /** M7 part F: a difficulty's change to homing missiles: their turn rate instead, °/s (the walker's hard 120). */
    record HomingChange(Optional<Double> turnRate) {
        public HomingChange {
            turnRate.ifPresent(r -> Check.positive("turn_rate", r));
        }
    }

    /**
     * M7 part F: a difficulty's change to a walking boss's flame cone: its length (px), swivel (°) or tell (s)
     * instead (the walker's hard 170 px and 35°, easy 0.8 s).
     */
    record ConeChange(Optional<Double> length, Optional<Double> swivel, Optional<Double> tell) {
        public ConeChange {
            length.ifPresent(l -> Check.positive("length", l));
            swivel.ifPresent(w -> Check.notNegative("swivel", w));
            tell.ifPresent(t -> Check.notNegative("tell", t));
        }
    }

    /** M7 part D: a difficulty's change to a curl-roll: its roll time instead (the Shellback's hard 3.0 s). */
    record RollChange(Optional<Double> seconds) {
        public RollChange {
            seconds.ifPresent(t -> Check.positive("seconds", t));
        }
    }

    /** M7 part D: a difficulty's change to an aura: its radius or refill instead (the Choir Herald's easy and hard). */
    record AuraChange(Optional<Double> radius, Optional<Double> refill) {
        public AuraChange {
            radius.ifPresent(r -> Check.positive("radius", r));
            refill.ifPresent(r -> Check.that(r > 0 && r <= 1, "refill: above 0 and at most 1"));
        }
    }

    /** M8 part F: a difficulty's change to a lunge: its trigger distance instead (the Glow Angler's easy 100, hard 160). */
    record LungeChange(Optional<Double> trigger) {
        public LungeChange {
            trigger.ifPresent(t -> Check.positive("trigger", t));
        }
    }

    /** M8 part E: a difficulty's change to a coiling orbit: its open window instead (the Nautilus's easy 1.8, hard 1.4 s). */
    record CoilChange(Optional<Double> open) {
        public CoilChange {
            open.ifPresent(o -> Check.positive("open", o));
        }
    }

    /** M8 part E: a difficulty's change to a slowing ring: its factor, time or immunity instead (the Siren's). */
    record SlowChange(Optional<Double> factor, Optional<Double> seconds, Optional<Double> immunity) {
        public SlowChange {
            factor.ifPresent(f -> Check.that(f > 0 && f <= 1, "factor: above 0 and at most 1, was " + f));
            seconds.ifPresent(t -> Check.positive("seconds", t));
            immunity.ifPresent(t -> Check.notNegative("immunity", t));
        }
    }

    /**
     * Round 44: a difficulty's change to a walking boss's rail lance: its charge, interval or last phase's interval
     * instead, s.
     */
    record LanceChange(Optional<Double> charge, Optional<Double> interval, Optional<Double> lastInterval) {
        public LanceChange {
            charge.ifPresent(c -> Check.positive("charge", c));
            interval.ifPresent(i -> Check.positive("interval", i));
            lastInterval.ifPresent(i -> Check.positive("last_interval", i));
        }
    }

    /** Round 44: a difficulty's change to a walking boss's stomp: its tell or cooldown instead, s. */
    record StompChange(Optional<Double> tell, Optional<Double> cooldown) {
        public StompChange {
            tell.ifPresent(t -> Check.notNegative("tell", t));
            cooldown.ifPresent(c -> Check.positive("cooldown", c));
        }
    }

    /** M8 part C: a difficulty's change to a flock's ram: the share of a school that breaks off. */
    record RamChange(Optional<Double> share) {
        public RamChange {
            share.ifPresent(v -> Check.that(v >= 0 && v <= 1, "share: 0 to 1, was " + v));
        }
    }

    /** M7 part B: a difficulty's change to the {@code burrow} movement (the Burrower's easy and hard windows and rings). */
    record BurrowChange(Optional<Double> surfaced, Optional<List<Ring>> rings) {
        public BurrowChange {
            surfaced.ifPresent(s -> Check.positive("surfaced", s));
            rings.ifPresent(list -> Check.notEmpty("rings", list));
        }
    }

    /** M7 part C: a difficulty's change to a pore wave (the Threadcrawler's easy every other pore, hard 1.5 s rest). */
    record PoresChange(Optional<Double> rest, Optional<Boolean> everyOther) {
        public PoresChange {
            rest.ifPresent(r -> Check.positive("rest", r));
        }
    }

    /**
     * A difficulty's change to one named attack. M8 part B (design/enemies/ground/drill-rig, hard): {@code aimed}, the
     * bullets fired straight at the ship besides a {@code ring}'s with each volley (the hard rig's aimed shard).
     */
    record AttackChange(
            Optional<Integer> burst,
            Optional<Integer> count,
            Optional<Double> interval,
            Optional<Integer> arms,
            Optional<Double> within,
            Optional<Integer> aimed) {
        /** Without M8 part B's aimed shards. */
        public AttackChange(
                Optional<Integer> burst,
                Optional<Integer> count,
                Optional<Double> interval,
                Optional<Integer> arms,
                Optional<Double> within) {
            this(burst, count, interval, arms, within, Optional.empty());
        }

        public AttackChange {
            arms.ifPresent(a -> Check.positive("arms", a));
            within.ifPresent(w -> Check.positive("within", w));
            aimed.ifPresent(a -> Check.positive("aimed", a));
        }
    }

    /** Overrides of the global difficulty levers. */
    record Hooks(Optional<Hook> easy, Optional<Hook> hard) {}

    /**
     * A difficulty's changes to the stat block.
     *
     * @param leadsTargetIn selected units in these formations lead the target
     * @param fanCount a fan's bullets instead
     * @param divePause a dive's pause instead, s
     * @param burst shots per volley instead of one
     * @param ring a mine's ring bullets instead
     * @param mineBursts false: mines never burst on their own
     * @param deathBurst the puff of bullets it pops into when destroyed
     * @param attacks changes per attack name (a boss's {@code burst} or {@code count}, an {@code interval}, a spiral's {@code arms})
     * @param sweepArc a laser sweep's arc instead, °
     * @param segments a segment chain's segments instead
     * @param regrownFanCount a regrown head's fan bullets instead
     * @param spawns part G: a boss's window spawns' counts instead, by spawn name
     * @param hoverSeconds M5 part D: the hover's time instead, s (the Wraith's hard hold)
     * @param telegraph M5 part E: an arena boss's slam telegraph instead, s (hard's 0.8)
     * @param lanes M5 part E: the lanes an arena boss's volley slams instead (hard's 3)
     * @param drain M5 part F: a drain's rate instead, shield points a second (the Lamprey's hard 12)
     * @param latchSlots M5 part F: a latch's slots instead (the Lamprey's hard 2)
     * @param burrow M7 part B: the burrow's surfaced time and rings instead (the Burrower's easy and hard)
     * @param swoopSpeed M7 part B: a triggered swoop's top speed instead, px/s (the Dust Kite's hard 230)
     * @param pores M7 part C: a pore wave's rest or every other pore instead (the Threadcrawler's easy and hard)
     * @param aura M7 part D: an aura's radius or refill instead (the Choir Herald's easy 75 px and 15 %/s, hard 105 px)
     * @param roll M7 part D: a curl-roll's roll time instead (the Shellback's hard 3.0 s)
     * @param vortex M7 part E: a spin cycle's open window instead (the Dust Devil's easy 1.0 s)
     * @param pull M7 part E: a pull's speed on the ship instead (the Dust Devil's easy 25, hard 45 px/s)
     * @param secondPass M7 part E: a lane strafe's line-abreast survivors turn at the edge and strafe back once (the
     *     Ghost Drone's hard)
     * @param turret M7 part F: a turret's turn rate instead (the Warden Tank's hard 160°/s)
     * @param homing M7 part F: homing missiles' turn rate instead (the Revenant Walker's hard 120°/s)
     * @param cone M7 part F: a flame cone's length, swivel or tell instead (the Revenant Walker's hard and easy)
     * @param pursuit M7 part G: a pursuit's time between lunges or tell instead (the Mote Swarm's easy and hard)
     * @param breach M7 part H: a breach cycle's bite tell instead (the Dust Colossus's easy 1.0 s, hard 0.6 s)
     * @param suction M7 part H: a suction's speed instead (the Dust Colossus's easy 30, hard 55 px/s)
     * @param eruption M7 part H: an eruption's marker time instead (the Dust Colossus's easy 1.8 s, hard 1.2 s)
     * @param ram M8 part C: a flock's ram share instead (the Glassfin's easy 0.15, hard 0.40)
     * @param coil M8 part E: a coiling orbit's open window instead (the Spiral Nautilus's easy 1.8, hard 1.4 s)
     * @param slow M8 part E: a slowing ring's factor, time or immunity instead (the Siren's easy 1.5 s, hard 2 s)
     * @param lunge M8 part F: a lunge's trigger distance instead (the Glow Angler's easy 100, hard 160 px)
     * @param coilArena M8 part H: a coiled boss's changes instead (the Abyssal Maw's sweep tell, shadow, implants' open
     *     time, shed segments and wall gaps; {@link MawBlock.CoilArenaChange})
     */
    record Hook(
            Optional<List<String>> leadsTargetIn,
            Optional<Integer> fanCount,
            Optional<Double> divePause,
            Optional<Integer> burst,
            Optional<Integer> ring,
            Optional<Boolean> mineBursts,
            Optional<DeathBurst> deathBurst,
            Optional<Integer> spawnCount,
            Optional<Double> spawnAfter,
            Optional<java.util.Map<String, AttackChange>> attacks,
            Optional<Double> sweepArc,
            Optional<Integer> segments,
            Optional<Integer> regrownFanCount,
            Optional<java.util.Map<String, Integer>> spawns,
            Optional<Double> hoverSeconds,
            Optional<Double> telegraph,
            Optional<Integer> lanes,
            Optional<Double> drain,
            Optional<Integer> latchSlots,
            Optional<BurrowChange> burrow,
            Optional<Double> swoopSpeed,
            Optional<PoresChange> pores,
            Optional<AuraChange> aura,
            Optional<RollChange> roll,
            Optional<VortexChange> vortex,
            Optional<PullChange> pull,
            Optional<Boolean> secondPass,
            Optional<TurretChange> turret,
            Optional<HomingChange> homing,
            Optional<ConeChange> cone,
            Optional<PursuitChange> pursuit,
            Optional<LanceChange> lance,
            Optional<StompChange> stomp,
            Optional<BreachChange> breach,
            Optional<SuctionChange> suction,
            Optional<EruptionChange> eruption,
            Optional<RamChange> ram,
            Optional<CoilChange> coil,
            Optional<SlowChange> slow,
            Optional<LungeChange> lunge,
            Optional<CoilArenaChange> coilArena) {
        public Hook {
            swoopSpeed.ifPresent(s -> Check.positive("swoop_speed", s));
            drain.ifPresent(d -> Check.positive("drain", d));
            latchSlots.ifPresent(n -> Check.positive("latch_slots", n));
            hoverSeconds.ifPresent(h -> Check.positive("hover_seconds", h));
            telegraph.ifPresent(t -> Check.positive("telegraph", t));
            lanes.ifPresent(n -> Check.positive("lanes", n));
        }
    }

    /** {@code count} bullets of class {@code bullet} in a ring at {@code speed} px/s. */
    record DeathBurst(int count, double speed, String bullet) {
        public DeathBurst {
            Check.positive("count", count);
            Check.positive("speed", speed);
        }
    }
}
