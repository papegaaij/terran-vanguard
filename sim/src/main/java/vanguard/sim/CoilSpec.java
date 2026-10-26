package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M8 part H, step H1 (design/enemies/bosses/abyssal-maw): a coiled arena boss's body and script at one difficulty, run
 * by its {@link CoilArena} ({@link BossSpec#coil()}). Its body is a chain of armoured segments lying along a fixed
 * path round the arena's edges (shots glance off it, the beam stops on it, bullets pass over it; a wall for the ship
 * and the escort); its head (the first vital part) rests at the boss's ({@link BossSpec#x()}, {@link
 * BossSpec#hoverY()}). The boss needs an arena without lanes ({@link BossSpec.Arena}: its parts' starting layers, its
 * throat a {@code while: open} weak spot on the head, open while the jaw is), no stride, breach or phase moves.
 * Coordinates are play-field px, y up. Lists "per phase" are indexed by the boss phase; a missing entry (a shorter
 * list) is none.
 *
 * @param body the segments, their path and the wall they make
 * @param introSeconds from the arrival (the bar) until the coil and the head are up: invulnerable, nothing fires
 * @param sacs the gill sacs (out of the bar; their {@code RING} attack breathes from them in turn)
 * @param swellSeconds a sac swells this long before its ring (the tell, {@link SimEvents.Type#SAC_SWELL})
 * @param jaw the throat's fan cycle (a phase's {@code FAN} attack other than the wall's)
 * @param wall the fan walls; empty for none
 * @param sweeps the coil's sweeps
 * @param dive the dive and the lunge (once); empty for none
 * @param implants the implants (weak spots on the coil counting on the head's HP); empty for none
 * @param shed the Eel Swarms shed from the coil; empty for none
 */
public record CoilSpec(
        Body body,
        double introSeconds,
        List<Integer> sacs,
        double swellSeconds,
        Jaw jaw,
        Optional<Wall> wall,
        Sweeps sweeps,
        Optional<Dive> dive,
        Optional<Implants> implants,
        Optional<Shed> shed) {
    public CoilSpec {
        sacs = List.copyOf(sacs);
        if (!(introSeconds >= 0) || !(swellSeconds >= 0)) {
            throw new IllegalArgumentException("a coil has an intro and a swell, 0 or more");
        }
    }

    /** A side of the arena the coil lines (a sweep's, a shed point's). */
    public enum Side {
        LEFT,
        BOTTOM,
        RIGHT
    }

    /**
     * The coil's body: {@code segments} boxes of {@code box}, {@code spacing} px apart along {@code path} (its centre
     * line, the head's end first; beyond its last point it runs on straight), each turned along it. The wall: the ship
     * and the escort keep {@code clearance} px inside the coil's inner flanks ({@code left}, {@code right}, {@code
     * bottom}, and {@code top} ({@link PlayField#HEIGHT} plus the clearance for none)).
     */
    public record Body(
            int segments,
            double spacing,
            Hitbox box,
            List<BossSpec.Point> path,
            double left,
            double right,
            double bottom,
            double top,
            double clearance) {
        public Body {
            path = List.copyOf(path);
            if (segments < 1
                    || !(spacing > 0)
                    || path.size() < 2
                    || !(left + clearance < right - clearance)
                    || !(bottom + clearance < top - clearance)
                    || !(clearance >= 0)) {
                throw new IllegalArgumentException("a coil has segments along a path round an open arena");
            }
        }
    }

    /**
     * The throat's fan (phase 1): every its attack's interval the throat glows {@code glowSeconds} (the jaw opens),
     * then the fan fires; the jaw stays open {@code openSeconds} from the glow (the throat spot's window).
     */
    public record Jaw(double glowSeconds, double openSeconds) {
        public Jaw {
            if (!(glowSeconds >= 0) || !(openSeconds >= glowSeconds)) {
                throw new IllegalArgumentException("the jaw opens with its glow and stays open at least as long");
            }
        }
    }

    /**
     * The fan walls (phase 2): attack {@code attack} (a {@code FAN}: its gun's count of orbs, spread, speed, damage
     * and interval) fires straight down from the throat after a {@code glowSeconds} glow (the jaw open through the
     * glow), with {@code gaps} orbs left out: the first {@code near}–{@code far} px beside the ship's x at its height
     * and within {@code chain} px of the last wall's, a second (easy) the same on the ship's other side.
     */
    public record Wall(int attack, double glowSeconds, int gaps, double near, double far, double chain) {
        public Wall {
            if (attack < 0
                    || !(glowSeconds >= 0)
                    || gaps < 1
                    || gaps > 2
                    || !(near >= 0)
                    || !(far >= near)
                    || !(chain > 0)) {
                throw new IllegalArgumentException("a fan wall is an attack with one or two gaps beside the ship");
            }
        }
    }

    /**
     * The coil's sweeps: per phase every {@code everySeconds} (0: none) a bight of {@code segments} segments on the
     * next side of the phase's {@code orders}, centred on the segment nearest the ship's y ({@code along}, the left
     * and right ones) or x ({@code across}, the bottom one) as its tell starts: the tell, the swing in by {@code
     * reach} px ({@code bottomReach} the bottom one), the hold, the swing back. What the bight reaches during its
     * swing takes {@code damage} once a sweep (and is pushed out with the wall).
     */
    public record Sweeps(
            List<Double> everySeconds,
            List<List<Side>> orders,
            int segments,
            double tellSeconds,
            double swingSeconds,
            double holdSeconds,
            double backSeconds,
            double reach,
            double bottomReach,
            double damage,
            BossSpec.Clamp along,
            BossSpec.Clamp across) {
        /** No sweeps. */
        public static final Sweeps NONE = new Sweeps(
                List.of(), List.of(), 1, 1, 1, 0, 1, 0, 0, 0, new BossSpec.Clamp(0, 0), new BossSpec.Clamp(0, 0));

        public Sweeps {
            everySeconds = List.copyOf(everySeconds);
            orders = orders.stream().map(List::copyOf).toList();
            if (segments < 1
                    || !(tellSeconds > 0)
                    || !(swingSeconds > 0)
                    || !(holdSeconds >= 0)
                    || !(backSeconds > 0)
                    || !(reach >= 0)
                    || !(bottomReach >= 0)
                    || !(damage >= 0)
                    || everySeconds.stream().anyMatch(every -> !(every >= 0))) {
                throw new IllegalArgumentException("a sweep has a bight, a tell, a swing, a hold and a way back");
            }
            for (int p = 0; p < everySeconds.size(); p++) {
                if (everySeconds.get(p) > 0
                        && (p >= orders.size() || orders.get(p).isEmpty())) {
                    throw new IllegalArgumentException("a phase that sweeps has an order of sides");
                }
            }
        }

        /** Phase {@code phase}'s interval, s; 0 for none. */
        public double every(int phase) {
            return phase < everySeconds.size() ? everySeconds.get(phase) : 0;
        }
    }

    /**
     * The dive and the lunge, once, in phase {@code phase} once the head holds less than {@code below} of its HP: the
     * slide into the dark ({@code sub} from its middle, the walls gone), the quiet, the shadow up a {@code width} px
     * column at the ship's x ({@code column}), the head's lunge up it ({@code air}; {@code damage} once to what it
     * passes in the column), its body a wall in the column for {@code bodySeconds}, the slide back (the walls return at
     * its end). Its phase does not end before the dive is over.
     */
    public record Dive(
            int phase,
            double below,
            double slideSeconds,
            double quietSeconds,
            double shadowSeconds,
            double lungeSeconds,
            double bodySeconds,
            double returnSeconds,
            double width,
            BossSpec.Clamp column,
            double damage) {
        public Dive {
            if (phase < 0
                    || !(below > 0 && below < 1)
                    || !(slideSeconds > 0)
                    || !(quietSeconds >= 0)
                    || !(shadowSeconds > 0)
                    || !(lungeSeconds > 0)
                    || !(bodySeconds >= 0)
                    || !(returnSeconds > 0)
                    || !(width > 0)
                    || !(damage >= 0)) {
                throw new IllegalArgumentException("a dive slides, waits, casts its shadow, lunges and returns");
            }
        }
    }

    /**
     * The implants: {@code parts} (on the coil: each rides the segment nearest its rest place) spark open in turn from
     * phase {@code phase} on: a {@code tellSeconds} tell, then open {@code openSeconds} (a hit counts on the head's HP
     * at the implant's own multiplier; shut it is armoured); while open, attack {@code attack} (an {@code AIMED}: its
     * gun's interval, burst and bullets) fires from it, each volley after a {@code arcSeconds} arc.
     */
    public record Implants(
            List<Integer> parts, int phase, int attack, double tellSeconds, double openSeconds, double arcSeconds) {
        public Implants {
            parts = List.copyOf(parts);
            if (parts.isEmpty()
                    || phase < 0
                    || attack < 0
                    || !(tellSeconds >= 0)
                    || !(openSeconds > 0)
                    || !(arcSeconds >= 0)) {
                throw new IllegalArgumentException("implants open in turn after a tell and fire after an arc");
            }
        }
    }

    /**
     * The Eel Swarms the coil sheds: per phase every {@code everySeconds} (0: none) while fewer than {@code most} of
     * them live, a coil plate at the next of {@code points} (in turn, the one nearest the ship skipped) flares {@code
     * tellSeconds}, then one {@code enemy} (its chain's segment count is the boss's) slips out there and swims straight
     * across to the far side. No edge warning.
     */
    public record Shed(
            EnemySpec enemy,
            List<ShedPoint> points,
            List<Double> everySeconds,
            List<Integer> most,
            double tellSeconds) {
        public Shed {
            points = List.copyOf(points);
            everySeconds = List.copyOf(everySeconds);
            most = List.copyOf(most);
            if (enemy.chain().isEmpty()
                    || points.isEmpty()
                    || !(tellSeconds >= 0)
                    || everySeconds.stream().anyMatch(every -> !(every >= 0))
                    || most.stream().anyMatch(n -> n < 0 || n > MOST)) {
                throw new IllegalArgumentException("a coil sheds a chain from its points, at most " + MOST + " alive");
            }
        }

        /** The most shed units alive at once any phase may ask for. */
        public static final int MOST = 4;

        /** Phase {@code phase}'s interval, s; 0 for none. */
        public double every(int phase) {
            return phase < everySeconds.size() ? everySeconds.get(phase) : 0;
        }

        /** Phase {@code phase}'s most alive at once; 0 for none. */
        public int most(int phase) {
            return phase < most.size() ? most.get(phase) : 0;
        }
    }

    /** A shed point: on the coil's {@code side} flank at {@code at} (its y on a side, its x on the bottom), px. */
    public record ShedPoint(Side side, double at) {}

    /** Checks it against its boss's {@code phases} and {@code attacks}; throws when it does not fit. */
    void check(String name, List<BossSpec.Phase> phases, List<BossSpec.Attack> attacks) {
        if (wall.isPresent()
                && (wall.get().attack() >= attacks.size()
                        || attacks.get(wall.get().attack()).pattern() != BossSpec.Pattern.FAN)) {
            throw new IllegalArgumentException(name + ": the fan wall is one of its fan attacks");
        }
        if (implants.isPresent()
                && (implants.get().attack() >= attacks.size()
                        || attacks.get(implants.get().attack()).pattern() != BossSpec.Pattern.AIMED
                        || implants.get().phase() >= phases.size())) {
            throw new IllegalArgumentException(name + ": the implants fire one of its aimed attacks from a phase");
        }
        if (dive.isPresent() && dive.get().phase() >= phases.size()) {
            throw new IllegalArgumentException(name + ": the dive comes in one of its phases");
        }
    }
}
