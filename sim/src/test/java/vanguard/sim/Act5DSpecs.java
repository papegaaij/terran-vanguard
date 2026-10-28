package vanguard.sim;

import static vanguard.sim.TestSpecs.INFINITE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * M9 part D, step D1: the Level 31 fixtures the sim tests fly (the approved numbers of round 60 at medium, the tug at
 * 200×300 with the section boxes of m9d-gaps.md §6): the mining tug's hull escort, the Hornet's aimed homing salvo, the
 * Shard Drone's linked line and ring, the Buzzsaw Drone's dash. Content builds the same from the data (D2). The sim is
 * y up: the station's (240, 380) from the top-left is (240, 160).
 */
final class Act5DSpecs {
    private Act5DSpecs() {}

    /** The station: (240, 380) y down, its sway ±10 px across and ±2.5 px up and down over 14 s (§6). */
    static final ObjectiveSpec.Station STATION = new ObjectiveSpec.Station(240, 160, 10, 2.5, 14, 0);

    /** The sections (y up from the hull's centre), their hit boxes ≈ 90 % of the opaque boxes, HP at medium, 40 each. */
    static List<ObjectiveSpec.HullSection> sections(double bow, double cargo, double engines) {
        return List.of(
                new ObjectiveSpec.HullSection("bow", 0, 108, new Hitbox(130, 76), bow, 40),
                new ObjectiveSpec.HullSection("cargo", 0, 4, new Hitbox(180, 110), cargo, 40),
                new ObjectiveSpec.HullSection("engines", 0, -103, new Hitbox(134, 84), engines, 40));
    }

    /** Who hurts the tug: the Hornets, the Darts and the Shard Drones, always. */
    static final Map<String, HookMode> HOOKED =
            Map.of("hornet", HookMode.ALWAYS, "dart-drone", HookMode.ALWAYS, "shard-drone", HookMode.ALWAYS);

    /**
     * The tug's hull: 200×300 at its station, rising in over 6 s, climbing out from {@code climbAt} over 6 s; a missile
     * 5, a dive 3, a link 4 a touch with a 0.5 s cooldown per link and section.
     */
    static ObjectiveSpec.Hull hull(
            List<ObjectiveSpec.HullSection> sections, double climbAt, Map<String, HookMode> hook) {
        return new ObjectiveSpec.Hull(
                new Hitbox(200, 300), STATION, sections, 6, new ObjectiveSpec.Climb(climbAt, 6), 5, 3, 4, 0.5, hook);
    }

    /** The whole tug's ally spec (its slug, its footprint; the sections carry their own HP). */
    static final AllySpec TUG = new AllySpec(
            "mining-tug",
            new Hitbox(200, 300),
            new Hitbox(200, 300),
            560,
            false,
            0,
            0.5,
            0,
            false,
            false,
            0,
            0,
            0,
            0,
            0,
            3,
            false,
            0,
            0);

    static ObjectiveSpec.Escort escort(ObjectiveSpec.Hull hull) {
        return ObjectiveSpec.Escort.hull(TUG, hull);
    }

    /** Medium's escort: bow 240, cargo 160, engines 160, home at 178 s. */
    static final ObjectiveSpec.Escort ESCORT = escort(hull(sections(240, 160, 160), 178, HOOKED));

    /** The Hornet's salvo: 4 missiles 0.2 s apart every 5 s (the first 1.5 s in), 110 px/s, 6 on the ship, the hatch 0.5 s before. */
    static final EnemyGun SALVO = new EnemyGun(
                    5, 1.5, 4, 110, 6, false, 1, 0, INFINITE, INFINITE, Optional.empty(), Optional.empty(), 0.2)
            .withTell(0.5)
            .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.toRadians(80), 4, 2, 8, 0.5, true)
                    .withPerShooter(true)
                    .withAimed(true)));

    /** The Hornet: 96 HP behind its 8-point shield, holding 9 s 90–150 px below the top edge with a 20 px/s drift. */
    static final EnemySpec HORNET = EnemySpec.of("hornet", 96, new Hitbox(52, 44), Layer.AIR, 15, false, 26, 140)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(9, 9), new Range(90, 150), Optional.empty(), 20)))
            .withGun(Optional.of(SALVO))
            .withShield(Optional.of(Act5Specs.shield(8)))
            .withMachine(true);

    /** The Shard Drone's links: an 6 px band, 6 a touch every 0.5 s, lit 0.8 s after the group is in, 120 px apart. */
    static final EnemySpec.AirLink LINE_LINK = new EnemySpec.AirLink(6, 6, 0.5, 0.8, 120, 0, 0);

    /** A ring's links: its centre descends 40 px/s and drifts 10 px/s toward the ship. */
    static final EnemySpec.AirLink RING_LINK = new EnemySpec.AirLink(6, 6, 0.5, 0.8, 120, 40, 10);

    /** A Shard Drone of a line: 28 HP (beam era), 45 px/s down with a 30 px sway over 5 s, a bolt every 3 s. */
    static final EnemySpec SHARD = EnemySpec.of("shard-drone", 28, new Hitbox(26, 26), Layer.AIR, 10, true, 14, 45)
            .withSine(Optional.of(new EnemySpec.Sine(30, 5)))
            .withGun(Optional.of(EnemyGun.aimed(3, 1, 1, 150, 4, false)))
            .withMachine(true)
            .withAirLink(Optional.of(LINE_LINK));

    /** A Shard Drone of a ring: 64 px round a centre, 60°/s, 40 px/s down the field. */
    static final EnemySpec RING_SHARD = EnemySpec.of("shard-drone", 28, new Hitbox(26, 26), Layer.AIR, 10, true, 14, 40)
            .withOrbit(Optional.of(new EnemySpec.Orbit(64, 60)))
            .withMachine(true)
            .withAirLink(Optional.of(RING_LINK));

    /**
     * The Buzzsaw's cycle at medium: chase 120°/s for 2.5 s, the tell 0.6 s at 40 px/s, a 360 px/s dash of 3 bounces,
     * slowing over 0.5 s; out after its second dash.
     */
    static final EnemySpec.Dash DASH = new EnemySpec.Dash(Math.toRadians(120), 2.5, 0.6, 40, 360, 3, 0.5, 2);

    /** The Buzzsaw Drone: 20 HP (beam era), 26 px round, contact 10 (destroyed by it), 120 px/s. */
    static final EnemySpec BUZZSAW = EnemySpec.of("buzzsaw-drone", 20, new Hitbox(26, 26), Layer.AIR, 10, true, 12, 120)
            .withMachine(true)
            .withDash(Optional.of(DASH));

    /** A circle's Buzzsaws: 4 orbiting 160 px below the top edge before they break off one by one. */
    static final EnemySpec CIRCLE_SAW = BUZZSAW.withOrbit(Optional.of(new EnemySpec.Orbit(60, 90)));

    /** A wave with a hold, an interval and a circle's centre (the fields the other fixtures leave out). */
    static WaveSpec wave(
            double t,
            WaveSpec.Formation formation,
            EnemySpec enemy,
            int count,
            WaveSpec.Entry entry,
            WaveSpec.Edge edge,
            Optional<Double> hold,
            Optional<Double> interval) {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                hold,
                Optional.empty(),
                1,
                Optional.empty(),
                interval,
                List.of());
    }

    /** A linked line of {@code count} Shard Drones down the field's middle from {@code t}. */
    static WaveSpec shardLine(double t, int count) {
        return wave(
                t,
                WaveSpec.Formation.LINE_ABREAST,
                SHARD,
                count,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty());
    }

    /** A linked ring of 4 Shard Drones round (240, 160 px below the top edge) from {@code t}. */
    static WaveSpec shardRing(double t) {
        return wave(
                        t,
                        WaveSpec.Formation.CIRCLE,
                        RING_SHARD,
                        4,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE,
                        Optional.of(4.0),
                        Optional.empty())
                .withAt(new WaveSpec.At(240, 160));
    }

    /** One Hornet (or a line of them) holding at the top from {@code t}. */
    static WaveSpec hornets(double t, int count) {
        return wave(
                t,
                WaveSpec.Formation.LINE_ABREAST,
                HORNET,
                count,
                WaveSpec.Entry.FRONT,
                WaveSpec.Edge.NONE,
                Optional.empty(),
                Optional.empty());
    }

    /** A stream of {@code count} Darts from the rear (x per the stream's path) from {@code t}. */
    static WaveSpec darts(double t, int count, WaveSpec.Entry entry, WaveSpec.Edge edge) {
        return wave(
                t, WaveSpec.Formation.STREAM, Act5CSpecs.DART, count, entry, edge, Optional.empty(), Optional.of(0.4));
    }

    /** A stream of {@code count} Buzzsaws from the side {@code edge} from {@code t}. */
    static WaveSpec saws(double t, int count, WaveSpec.Edge edge) {
        return wave(
                t,
                WaveSpec.Formation.STREAM,
                BUZZSAW,
                count,
                WaveSpec.Entry.SIDES,
                edge,
                Optional.empty(),
                Optional.of(0.6));
    }

    /** A level of {@code seconds} at 120 px/s with {@code waves} and the tug's {@code escort}, no ground units. */
    static LevelScript level(double seconds, List<WaveSpec> waves, ObjectiveSpec.Escort escort) {
        return LevelScript.builder(31, 5)
                .sections(List.of(new Section(seconds, 120)))
                .waves(waves)
                .objectives(ObjectiveSpec.NONE.withEscort(Optional.of(escort)))
                .build();
    }

    /** The samples that set the part-D components (for the copy test). */
    static List<EnemySpec> samples() {
        return List.of(BUZZSAW, SHARD);
    }
}
