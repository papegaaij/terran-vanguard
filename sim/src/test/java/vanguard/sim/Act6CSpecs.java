package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * M10 part C, step C1: the sim fixtures of Level 37's mechanics, after the approved docs (design/campaign Level 37, The
 * shuttles; design/enemies/air/mirror-interceptor, Movement) at medium, Level 37's times scaled down: three shuttle
 * pairs lifting at t=4, 14 and 24 (5 s each, 8 s on station, a 3 s climb-out), the level 45 s long. The content's own
 * fixtures are C2's.
 */
final class Act6CSpecs {
    private Act6CSpecs() {}

    /** The evacuation shuttle (Level 10's AllySpec, ShuttleEscortTest's) at Level 37's medium armour of 120. */
    static final AllySpec SHUTTLE = ShuttleEscortTest.SHUTTLE;

    /** The three pairs' liftoffs (s) and how long each lifts, holds its stations and climbs out. */
    static final double[] LIFTS = {4, 14, 24};

    static final double LIFT_SECONDS = 5;
    static final double HOLD_SECONDS = 8;
    static final double CLIMB_SECONDS = 3;
    static final double SPEED = 100;
    static final double END = 45;

    /** A pair's stations: one behind the other in the middle, 170 and 240 px below the top edge, ±16 px sway. */
    static List<ObjectiveSpec.Station> stations() {
        List<ObjectiveSpec.Station> stations = new ArrayList<>();
        for (int pair = 0; pair < LIFTS.length; pair++) {
            stations.add(new ObjectiveSpec.Station(240, PlayField.HEIGHT - 170, 16, 4, 9, 0));
            stations.add(new ObjectiveSpec.Station(240, PlayField.HEIGHT - 240, 16, 4, 11, 0.5));
        }
        return stations;
    }

    /** The three lift groups: pads at x 190 and 290 in the lower half. */
    static List<ObjectiveSpec.Lift> groups() {
        List<ObjectiveSpec.Lift> groups = new ArrayList<>();
        for (double t : LIFTS) {
            groups.add(new ObjectiveSpec.Lift(
                    new ObjectiveSpec.Liftoff(
                            t, LIFT_SECONDS, List.of(new ObjectiveSpec.Pad(190, 160), new ObjectiveSpec.Pad(290, 160))),
                    new ObjectiveSpec.Climb(t + LIFT_SECONDS + HOLD_SECONDS, CLIMB_SECONDS)));
        }
        return groups;
    }

    /** The six shuttles as air allies outside the objectives, the waves {@code hooked} aiming at them. */
    static ObjectiveSpec.Escort shuttles(List<Integer> hooked) {
        return ObjectiveSpec.Escort.air(SHUTTLE, ObjectiveSpec.Air.lifting(stations(), groups(), hooked), 0);
    }

    /** Level 37's secondary at medium: 5 of 6 home for 100. */
    static final Secondary SAVED = Secondary.saved(5, 100, "SHUTTLES");

    /** The radio cues the tests listen for, by index. */
    static final List<CueTrigger> CUES = List.of(
            CueTrigger.SECONDARY_OBJECTIVE,
            CueTrigger.SECONDARY_FAILED,
            CueTrigger.FIRST_ALLY_LOST,
            CueTrigger.MISSION_FAILED,
            CueTrigger.FIRST_BOUNCE,
            CueTrigger.FIRST_PREY_KILL);

    static List<RadioCue> radio() {
        List<RadioCue> cues = new ArrayList<>();
        for (CueTrigger trigger : CUES) {
            cues.add(new RadioCue(trigger, 0, "", "Rook", trigger.name(), false, "neutral"));
        }
        return cues;
    }

    /** A level with the shuttles, {@code secondary} and {@code waves} (the waves {@code hooked} aim at the shuttles). */
    static LevelScript level(Secondary secondary, List<WaveSpec> waves, List<Integer> hooked) {
        return LevelScript.builder(37, 6)
                .sections(List.of(new Section(END, SPEED)))
                .waves(waves)
                .radio(radio())
                .objectives(ObjectiveSpec.NONE.withSecondary(secondary).withAirAllies(Optional.of(shuttles(hooked))))
                .build();
    }

    /** A sortie of {@code level}, the ship invulnerable (nothing hits it). */
    static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** A sortie whose ship can be hit (the debug option that keeps a level going is off). */
    static Sortie vulnerable(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES, TestSpecs.FULL_ARMOUR);
    }

    /** The radio cue {@code trigger} started in the step just run. */
    static boolean cued(Sortie sortie, CueTrigger trigger) {
        SimEvents events = sortie.events();
        for (int i = 0; i < events.size(); i++) {
            if (events.type(i) == SimEvents.Type.RADIO && events.value(i) == CUES.indexOf(trigger)) {
                return true;
            }
        }
        return false;
    }

    /** A bullet from 40 px below shuttle {@code k}, flying up at 600 px/s. */
    static void fromBelow(Sortie sortie, int k, double damage) {
        sortie.fireEnemyBullet(sortie.ally(k).x(), sortie.ally(k).y() - 40, 0, 600, damage);
    }

    /** The Mirror Interceptor's station: 90–130 px in, 90–120 px above the bottom, a 60 px strafe at 40 px/s, gap 80. */
    static final EnemySpec.Station STATION = new EnemySpec.Station(new Range(90, 130), new Range(90, 120), 60, 40, 80);

    /**
     * A Mirror Interceptor flying to a station: 120 HP, hit box 52×48, 180 px/s, a 7 s hold 160–300 px below the top,
     * its side hover's edge, the mirror (Act6ASpecs's), no gun.
     */
    static final EnemySpec MIRROR = EnemySpec.of(
                    "mirror-interceptor", 120, new Hitbox(52, 48), Layer.AIR, 15, false, 30, 180)
            .withMachine(true)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(7, 7), new Range(160, 300))))
            .withSideHover(Optional.of(new EnemySpec.SideHover(110, true)))
            .withMirror(Optional.of(Act6ASpecs.MIRROR))
            .withStation(Optional.of(STATION));

    /** A level of {@code seconds} at 100 px/s with {@code waves} and no secondary to speak of. */
    static LevelScript plain(double seconds, List<WaveSpec> waves) {
        return LevelScript.builder(37, 6)
                .sections(List.of(new Section(seconds, SPEED)))
                .waves(waves)
                .radio(radio())
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }
}
