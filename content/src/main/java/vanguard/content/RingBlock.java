package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.EnemyAttackData.PartData;
import vanguard.content.EnemyBossData.BossData;

/**
 * M9 part H (design/enemies/bosses/iron-sovereign): a ringed boss's keys in its stat block ({@link EnemyData}): the boss
 * script's {@code rings} (each ring's radius, turn or step and its batteries), the {@code fire_arc}, the {@code spin_up},
 * {@code grind}, {@code speed_ups} and {@code cookoff} times, the {@code core} (its glow, opening, hex shield, spiral
 * cycle, rail and rim bays), an attack's {@code from} (a battery's attack taken from a ground unit's stat block) and the
 * difficulties' {@code ring_arena} changes. Content's {@link RingRules} turns them into the simulation's {@code
 * RingSpec}; {@link RingChecks} checks them.
 *
 * <p>Angles: a ring's {@code start}, a step's {@code angle} and the bays' {@code start} are degrees clockwise (on the
 * screen) from straight down, the simulation's convention; the {@code fire_arc}'s {@code clamp} is the boss doc's
 * bearings (90° straight down, clockwise from the right), the rings' convention + 90°.
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.RingData} and
 * so on, without growing that record's file.
 */
public sealed interface RingBlock permits EnemyData {
    /** The most rings a boss turns (the simulation's {@code RingSpec.MAX_RINGS}). */
    int MOST_RINGS = vanguard.sim.RingSpec.MAX_RINGS;

    /** The fire arc's narrowest and widest, ° (any difficulty's). */
    double ARC_MIN = 60;

    double ARC_MAX = 180;

    /** The defaults where the boss doc gives no number: the spin-up from the halt, s (the doc's ≈ 8 s intro). */
    double SPIN_UP = 4;

    /** The grind of a locked ring to a halt, s (the doc's 2 s). */
    double GRIND = 2;

    /** The survivors' speed factors by the count of locked rings (the doc's ×1.25, ×1.5). */
    List<Double> SPEED_UPS = List.of(1.0, 1.25, 1.5);

    /** The cook-offs' spacing at the death, s (the doc's 0.15 s). */
    double COOKOFF = 0.15;

    /**
     * A ring ({@code boss.rings[]}, outer first): {@code radius} px from the centre, {@code segments} (the game's
     * hull pieces only), turning {@code speed} °/s {@code direction} {@code cw} or {@code ccw} (on the screen) or, with
     * a {@code step} instead, stepping; its first battery {@code start} ° (clockwise from straight down, 0 if not given)
     * at rest; its {@code batteries}.
     */
    record RingData(
            String name,
            double radius,
            Optional<Integer> segments,
            Optional<Double> speed,
            String direction,
            Optional<Double> start,
            Optional<StepData> step,
            Batteries batteries) {
        public RingData {
            Check.positive("radius", radius);
            segments.ifPresent(n -> Check.positive("segments", n));
            Check.that(
                    direction.equals("cw") || direction.equals("ccw"), "direction: cw or ccw, was '" + direction + "'");
            Check.that(speed.isPresent() != step.isPresent(), name + ": a ring turns at a speed or steps, one of them");
            speed.ifPresent(s -> Check.positive("speed", s));
        }

        /** The simulation's direction: 1 clockwise on the screen, -1 counter-clockwise. */
        public int sense() {
            return direction.equals("cw") ? 1 : -1;
        }

        /** The degrees of battery {@code k}'s mount at rest, clockwise from straight down. */
        public double mountDegrees(int k) {
            return start.orElse(0.0) + 360.0 * k / batteries.count();
        }
    }

    /**
     * A stepping ring's step: it turns {@code angle} ° in {@code seconds}, then holds {@code hold} s, the last
     * {@code tell} s of the hold its tell.
     */
    record StepData(double angle, double seconds, double hold, double tell) {
        public StepData {
            Check.positive("angle", angle);
            Check.positive("seconds", seconds);
            Check.positive("hold", hold);
            Check.notNegative("tell", tell);
            Check.that(tell <= hold, "tell: inside the hold, was " + tell);
        }
    }

    /**
     * A ring's batteries: {@code count} parts {@code part} on it, evenly spaced, firing the boss's attack {@code
     * attack}; named {@code <name>-1} … ({@code name} the attack's if not given). They are appended to the boss's
     * {@code part_list} in ring order (outer first).
     */
    record Batteries(int count, Optional<String> name, BatteryPart part, String attack) {
        public Batteries {
            Check.positive("count", count);
        }

        /** Battery {@code k}'s part name (0-based k). */
        public String partName(int k) {
            return name.orElse(attack) + "-" + (k + 1);
        }
    }

    /** A battery part: its {@code hp}, hit {@code box} and {@code bounty} (destroyable). */
    record BatteryPart(double hp, Size box, int bounty) {
        public BatteryPart {
            Check.positive("hp", hp);
            Check.notNegative("bounty", bounty);
        }
    }

    /**
     * The fire arc: a battery fires only while its mount lies within {@code degrees} ° of its ring centred on the
     * bearing from the centre to the ship, that bearing clamped to {@code clamp} (the doc's bearings: 90° straight
     * down, clockwise from the right).
     */
    record FireArc(double degrees, Span clamp) {
        public FireArc {
            Check.positive("degrees", degrees);
        }
    }

    /**
     * The ringed boss's core ({@code boss.core}): its {@code part}; the {@code glow} s before each shut-core fan; its
     * {@code opening} s (untouchable, silent) as the phase exposing it begins; its hex {@code shield} (the enemy shield
     * rule; the HP lever on it); its {@code spiral} cycle, its {@code rail} below a share of its HP and its rim {@code
     * bays}.
     */
    record CoreData(
            String part,
            double glow,
            double opening,
            Optional<Double> shield,
            Optional<CoreSpiral> spiral,
            Optional<CoreRail> rail,
            Optional<BaysData> bays) {
        public CoreData {
            Check.notNegative("glow", glow);
            Check.notNegative("opening", opening);
            shield.ifPresent(s -> Check.positive("shield", s));
        }
    }

    /** The open core's spiral {@code attack}: after a {@code tell} s it runs {@code run} s, then rests {@code rest} s. */
    record CoreSpiral(String attack, double run, double rest, double tell) {
        public CoreSpiral {
            Check.positive("run", run);
            Check.notNegative("rest", rest);
            Check.notNegative("tell", tell);
        }
    }

    /** The open core's rail line {@code attack}, fired only while the core holds less than {@code below} of its HP. */
    record CoreRail(String attack, double below) {
        public CoreRail {
            Check.that(below > 0 && below <= 1, "below: a share above 0, at most 1, was " + below);
        }
    }

    /**
     * The core's rim bays: {@code count} bays {@code radius} px from the centre, bay 0 at {@code start} ° (clockwise
     * from straight down, 0 if not given), the others evenly round; one bay a turn (the nearest the ship farther than
     * {@code skip_within} px) opens with a {@code tell} s before its stream of {@code stream} units of {@code from}
     * ({@code gap} s apart, every {@code every} s, the first {@code first} s after the core's opening), launched at
     * {@code speed} px/s (the unit's if not given) over {@code spread} °, flying straight {@code straight} s before
     * their own chase; none while {@code most_alive} of them live.
     */
    record BaysData(
            int count,
            double radius,
            Optional<Double> start,
            String from,
            int stream,
            double every,
            double gap,
            double first,
            Optional<Double> speed,
            double spread,
            double straight,
            int mostAlive,
            double tell,
            double skipWithin) {
        public BaysData {
            Check.positive("count", count);
            Check.notNegative("radius", radius);
            Check.positive("stream", stream);
            Check.positive("every", every);
            Check.positive("gap", gap);
            Check.notNegative("first", first);
            speed.ifPresent(s -> Check.positive("speed", s));
            Check.that(spread >= 0 && spread <= 180, "spread: 0 to 180°, was " + spread);
            Check.notNegative("straight", straight);
            Check.positive("most_alive", mostAlive);
            Check.notNegative("tell", tell);
            Check.notNegative("skip_within", skipWithin);
        }
    }

    /**
     * An attack's {@code from}: the attack is ground unit {@code unit}'s (its {@code attack} by name, its only one if not
     * given): its bullet, speed, burst, homing missiles, line or sweep, its own difficulty hooks on them; the boss sets
     * the attack's name, interval, staggering and parts. A sweep's beam is {@code length} px (the unit's if not given).
     * Written as the slug alone or as {@code {unit, attack, length}}.
     */
    record From(String unit, Optional<String> attack, Optional<Double> length) {
        public From {
            length.ifPresent(l -> Check.positive("from.length", l));
        }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        static From of(
                @JsonProperty("unit") String unit,
                @JsonProperty("attack") Optional<String> attack,
                @JsonProperty("length") Optional<Double> length) {
            Check.that(unit != null, "from has a unit");
            return new From(
                    unit, attack == null ? Optional.empty() : attack, length == null ? Optional.empty() : length);
        }

        /** The unit's slug written alone. */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        static From of(String unit) {
            return new From(unit, Optional.empty(), Optional.empty());
        }
    }

    /**
     * A difficulty's changes to a ringed boss ({@code ring_arena}): the rings' {@code speed} factor (a stepping ring's
     * step time divided by it; hard 1.2), the fire arc's {@code arc} ° instead (easy 90, hard 150) and the batteries'
     * {@code interval} factor (instead of the fire-rate lever; easy 1.3, hard 0.8; an attack's authored interval in the
     * hook's {@code attacks} stays final).
     */
    record RingArenaChange(Optional<Double> speed, Optional<Double> arc, Optional<Double> interval) {
        public RingArenaChange {
            speed.ifPresent(s -> Check.positive("ring_arena.speed", s));
            arc.ifPresent(a -> Check.positive("ring_arena.arc", a));
            interval.ifPresent(i -> Check.positive("ring_arena.interval", i));
        }
    }

    /**
     * The part list with a ringed boss's batteries appended (ring order, outer first; each ring's batteries in mount
     * order), each at its mount's rest place ({@code [dx, dy]}, dy up: the ring turns it from there); as it is without
     * rings, or when the batteries are in it already (a copy of the record).
     */
    static Optional<List<PartData>> withBatteries(Optional<List<PartData>> partList, Optional<BossData> boss) {
        Optional<List<RingData>> rings = boss.flatMap(BossData::rings);
        if (rings.isEmpty() || partList.isEmpty()) {
            return partList;
        }
        String first = rings.get().getFirst().batteries().partName(0);
        if (partList.get().stream().anyMatch(p -> p.name().equals(first))) {
            return partList;
        }
        List<PartData> parts = new ArrayList<>(partList.get());
        for (RingData ring : rings.get()) {
            Batteries batteries = ring.batteries();
            for (int k = 0; k < batteries.count(); k++) {
                double a = Math.toRadians(ring.mountDegrees(k));
                parts.add(new PartData(
                        batteries.partName(k),
                        new Point(-ring.radius() * Math.sin(a), -ring.radius() * Math.cos(a)),
                        batteries.part().box(),
                        batteries.part().hp(),
                        "destroyable",
                        batteries.part().bounty(),
                        Optional.of(batteries.attack()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
            }
        }
        return Optional.of(List.copyOf(parts));
    }
}
