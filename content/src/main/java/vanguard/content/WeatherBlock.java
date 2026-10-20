package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Optional;

/**
 * The level's {@code weather}, {@code sondes}, {@code darkness}, {@code flak} and {@code ridge}: wind, lightning and
 * rain, the sondes to fly through, the headlight, flares and lights of a dark level, the flak bursts over a field and
 * the travelling ridge on the ground.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Weather}).
 */
public sealed interface WeatherBlock permits LevelData {
    /**
     * M5 part F, a level's {@code weather} (design/campaign Level 12, Weather): the wind's {@code
     * gusts} and the {@code drift} through a section (added to the positions of the ship, the escort,
     * the units with a latch and the Whirl Seeds; never to shots, mines, spores, pickups, sondes or
     * drifting triggers), the {@code lightning} flashes (the simulation's {@code LIGHTNING} event, the
     * banks lit for {@code reveal} s), {@code occlusion} (the banks on {@code low-air} hide that layer's
     * bodies: draw order only) and {@code rain} ({@code [t, density]} keys, 0–1, linear between them:
     * presentation only).
     */
    public record Weather(
            Optional<Gusts> gusts,
            Optional<WallDrift> drift,
            Optional<Lightning> lightning,
            Optional<Boolean> occlusion,
            Optional<List<RainKey>> rain) {
        public Weather {
            rain = rain.map(List::copyOf);
            rain.ifPresent(keys -> {
                Check.notEmpty("rain", keys);
                for (int i = 1; i < keys.size(); i++) {
                    Check.that(keys.get(i).t() > keys.get(i - 1).t(), "rain: keys in time order");
                }
            });
        }

        /** Whether the banks on {@code low-air} hide that layer's bodies ({@code occlusion: true}). */
        public boolean occludes() {
            return occlusion.orElse(false);
        }

        /** The rain's density at {@code t}, 0–1: linear between the keys, the first and last held; 0 without rain. */
        public double rainAt(double t) {
            List<RainKey> keys = rain.orElse(List.of());
            if (keys.isEmpty()) {
                return 0;
            }
            if (t <= keys.getFirst().t()) {
                return keys.getFirst().density();
            }
            for (int i = 1; i < keys.size(); i++) {
                RainKey a = keys.get(i - 1);
                RainKey b = keys.get(i);
                if (t <= b.t()) {
                    return a.density() + (b.density() - a.density()) * (t - a.t()) / (b.t() - a.t());
                }
            }
            return keys.getLast().density();
        }
    }

    /**
     * M5 part F: the gusts {@code at} their times (each pushing from its side from its {@code t},
     * telegraphed {@code telegraph} s before), each lasting {@code seconds} including the {@code ramp}
     * s up and down, pushing {@code push} px/s; {@code easy} / {@code hard} change the push and the
     * telegraph, and hard's {@code extra} gusts come on top.
     */
    public record Gusts(
            List<GustAt> at,
            double seconds,
            double ramp,
            double push,
            double telegraph,
            Optional<GustChange> easy,
            Optional<GustChange> hard) {
        public Gusts {
            at = List.copyOf(at);
            Check.notEmpty("at", at);
            Check.notNegative("ramp", ramp);
            Check.that(seconds > 2 * ramp, "seconds: longer than the ramps up and down");
            Check.notNegative("push", push);
            Check.notNegative("telegraph", telegraph);
            for (int i = 1; i < at.size(); i++) {
                Check.that(at.get(i).t() >= at.get(i - 1).t(), "at: gusts in time order");
            }
        }
    }

    /** M5 part F: a gust starting to push at {@code t}, blowing {@code from} the left or the right edge. */
    public record GustAt(double t, WindSide from) {
        public GustAt {
            Check.notNegative("t", t);
        }
    }

    /** M5 part F: a difficulty's gusts: another push or telegraph, and (hard) {@code extra} gusts on top. */
    public record GustChange(Optional<Double> push, Optional<Double> telegraph, Optional<List<GustAt>> extra) {
        public GustChange {
            push.ifPresent(p -> Check.notNegative("push", p));
            telegraph.ifPresent(t -> Check.notNegative("telegraph", t));
        }
    }

    /**
     * M5 part F: a steady drift of {@code push} px/s {@code from} a side through section {@code section}
     * (1-based; Level 12's storm wall), with the gusts' ramps; {@code easy} / {@code hard} another push, 0
     * for none.
     */
    public record WallDrift(
            int section, double push, WindSide from, Optional<DriftChange> easy, Optional<DriftChange> hard) {
        public WallDrift {
            Check.positive("section", section);
            Check.notNegative("push", push);
        }
    }

    /** M5 part F: a difficulty's drift push, px/s; 0 for none. */
    public record DriftChange(double push) {
        public DriftChange {
            Check.notNegative("push", push);
        }
    }

    /** M5 part F: the lightning flashes {@code at} their times (in order), each lighting the banks for {@code reveal} s. */
    public record Lightning(List<Double> at, double reveal) {
        public Lightning {
            at = List.copyOf(at);
            Check.notEmpty("at", at);
            Check.positive("reveal", reveal);
            for (int i = 1; i < at.size(); i++) {
                Check.that(at.get(i) > at.get(i - 1), "at: flashes in time order");
            }
        }
    }

    /** M5 part F: the rain's {@code density} (0–1) at {@code t}. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record RainKey(double t, double density) {
        public RainKey {
            Check.notNegative("t", t);
            Check.share("density", density);
        }
    }

    /** M5 part F: the side a wind blows from. */
    public enum WindSide {
        LEFT,
        RIGHT
    }

    /**
     * M5 part F, the weather sondes (design/campaign Level 12, user decision F7 = b): each enters at the
     * top edge at its {@code [t, x]} and drifts straight down at {@code speed} px/s on the play plane; the
     * ship collects one when its centre comes within {@code radius} px of the sonde's. Not shot, not
     * pushed by the wind, not pulled by the magnet, not collected by the escort.
     */
    public record SondesData(double speed, double radius, List<LevelData.Placement> at) {
        public SondesData {
            at = List.copyOf(at);
            Check.positive("speed", speed);
            Check.positive("radius", radius);
            Check.notEmpty("at", at);
            for (int i = 1; i < at.size(); i++) {
                Check.that(at.get(i).t() >= at.get(i - 1).t(), "at: sondes in time order");
            }
        }
    }

    /**
     * Part F: a dark level (Level 06, Darkness rules). {@code headlight}: the ship's cone, on from
     * {@code from} s, {@code length} px (an {@code easy} length), {@code angle} ° wide.
     * {@code flares}: fired at {@code t} at {@code x}, starting {@code y} px below the top edge
     * (each may be left out on a difficulty with {@code skip}), falling {@code flare.seconds}
     * ({@code easy_seconds}) with a pool of {@code flare.radius} px drifting {@code flare.drift}
     * px/s down the screen. {@code lights}: static pools on the ground (dome and rail lamps),
     * entering at the top edge at {@code t}. {@code ambient}: the ground's brightness outside light.
     */
    public record Darkness(
            Headlight headlight, FlareFall flare, List<Flare> flares, Optional<List<Light>> lights, double ambient) {
        public Darkness {
            Check.that(ambient >= 0 && ambient <= 1, "ambient is between 0 and 1");
        }
    }

    public record Headlight(double from, double length, Optional<Double> easy, double angle) {
        public Headlight {
            Check.notNegative("from", from);
            Check.positive("length", length);
            Check.positive("angle", angle);
        }
    }

    public record FlareFall(double seconds, Optional<Double> easySeconds, double radius, double drift) {
        public FlareFall {
            Check.positive("seconds", seconds);
            Check.positive("radius", radius);
            Check.notNegative("drift", drift);
        }
    }

    public record Flare(double t, double x, double y, Optional<List<String>> skip) {
        public Flare {
            Check.notNegative("t", t);
            skip.ifPresent(names -> names.forEach(Difficulty::of));
        }

        /** Whether it is fired on {@code difficulty}. */
        public boolean firedOn(Difficulty difficulty) {
            return skip.map(names -> names.stream().map(Difficulty::of).noneMatch(difficulty::equals))
                    .orElse(true);
        }
    }

    public record Light(double t, double x, double radius) {
        public Light {
            Check.notNegative("t", t);
            Check.positive("radius", radius);
        }
    }

    /**
     * M7 part D, presentation (design/campaign Level 17, Layout: the militia's flak guns at the spaceport): from
     * {@code from} s on a flak burst on {@code low-air} every {@code every} s, over the lower field, hitting nothing
     * (the escort frigate's flak, Level 11, without a ship). M7 part G (Level 20, Over the Rim): with {@code band: top}
     * the bursts fill the top band instead (the rim guns firing up past the ship at the pursuers).
     */
    public record Flak(double from, double every, Optional<FlakBand> band) {
        public Flak {
            Check.notNegative("from", from);
            Check.positive("every", every);
        }

        /** Over the lower field (Level 17's). */
        public Flak(double from, double every) {
            this(from, every, Optional.empty());
        }

        /** Where its bursts fall: its {@code band}, the lower field if not given. */
        public FlakBand bandOrDefault() {
            return band.orElse(FlakBand.LOWER);
        }
    }

    /** M7 part G: where the flak bursts fall: over the lower field (Level 17's) or in the top band (Level 20's rim guns). */
    public enum FlakBand {
        LOWER,
        TOP
    }

    /**
     * M7 part G, presentation (design/campaign Level 20, Layout, section 1; Level 21's ridge pass; round 43 Q.4): the
     * Dust Colossus's travelling ridge, scenery on the ground that nothing hits. Its head surfaces off the {@code from}
     * edge at level time {@code t}, {@code depth} px below the top edge, and crosses the field to off the {@code to}
     * edge in {@code seconds} s on a slow S up the strip; the body follows on the head's track and the ridge shows until
     * its tail is off that edge too. The simulation never reads it.
     */
    public record Ridge(double t, double seconds, RidgeEdge from, RidgeEdge to, double depth) {
        public Ridge {
            Check.notNegative("ridge.t", t);
            Check.positive("ridge.seconds", seconds);
            Check.that(from != to, "ridge: from and to are opposite edges, both were " + from);
            Check.that(
                    depth >= 0 && depth < vanguard.sim.PlayField.HEIGHT,
                    "ridge.depth: px below the top edge, inside the play field, was " + depth);
        }

        /** Whether the head runs from the right edge to the left (Level 21's), the other way round to Level 20's. */
        public boolean leftward() {
            return from == RidgeEdge.RIGHT;
        }
    }

    /** M7 part G: the side edges a ridge crosses between. */
    public enum RidgeEdge {
        LEFT,
        RIGHT
    }
}
