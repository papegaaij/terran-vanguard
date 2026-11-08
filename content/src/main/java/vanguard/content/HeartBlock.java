package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * M11 part H, step H2 (design/enemies/bosses/choir-heart; m11h-gaps.md §1 #1–#5, #7, #9): the Choir Heart's keys in its
 * stat block ({@link EnemyData}, {@code boss:}), flown by steps H1a/H1b's simulation: the arena {@code walls} of a phase
 * (two tissue walls grown from the field's sides, terrain under the canyon-wall rule, a node riding each half), the
 * phase's {@code drift} (distorted space in a halted arena, one reversal), the membranes' {@code part_windows} (open
 * only while they sing), the phase's {@code dark} (a look only: the game's ambient), the {@code heartbeat} (the attack
 * whose shots raise the pulse's event) and the {@code checkpoints} at each phase's start; and the difficulties' {@code
 * heart} changes. Content's {@link HeartRules} turns them into the simulation's specs; {@link HeartChecks} checks them.
 *
 * <p>Places are the design's screen px (x from the left edge, y below the top edge).
 *
 * <p>It only holds the block's types: {@link EnemyData} implements it, so they are named {@code EnemyData.WallsData}
 * and so on, without growing that record's file.
 */
public sealed interface HeartBlock permits EnemyData {
    /** {@code checkpoints: phases}: the boss checkpoint taken again at each later phase's start. */
    String PHASE_CHECKPOINTS = "phases";

    /**
     * The arena walls: in phase {@code phase} (its name) two walls grow from the field's sides over {@code grow} s after a
     * {@code tell} s (the veins' glow) to leave a {@code corridor} px wide, centred; they breathe ±{@code breathe.amp} px
     * at {@code breathe.hz}, never faster than {@code most_speed} px/s but in the grow, never inward while a hull is
     * within {@code clear} px of the face; a touch deals {@code contact} and pushes the hull {@code push} px out over
     * {@code push_seconds} s, once per {@code cooldown} s; the {@code nodes} ride the halves' inner faces.
     */
    record WallsData(
            String phase,
            double corridor,
            double grow,
            double tell,
            BreatheData breathe,
            double mostSpeed,
            double clear,
            double contact,
            Optional<Double> push,
            Optional<Double> pushSeconds,
            Optional<Double> cooldown,
            List<WallNodeData> nodes) {
        /** The canyon walls' push, px (step H1a's). */
        public static final double PUSH = 28;

        /** The push's time, s. */
        public static final double PUSH_SECONDS = 0.12;

        /** The touch's cooldown, s (Act 3's canyon walls'). */
        public static final double COOLDOWN = 0.5;

        public WallsData {
            nodes = List.copyOf(Check.notEmpty("walls.nodes", nodes));
            Check.that(corridor > 0 && corridor < 480, "walls.corridor: px inside the 480 px field, was " + corridor);
            Check.positive("walls.grow", grow);
            Check.notNegative("walls.tell", tell);
            Check.positive("walls.most_speed", mostSpeed);
            Check.notNegative("walls.clear", clear);
            Check.notNegative("walls.contact", contact);
            push.ifPresent(p -> Check.notNegative("walls.push", p));
            pushSeconds.ifPresent(p -> Check.positive("walls.push_seconds", p));
            cooldown.ifPresent(c -> Check.notNegative("walls.cooldown", c));
        }
    }

    /** The walls' breath: ±{@code amp} px about the corridor's edge at {@code hz}. */
    record BreatheData(double amp, double hz) {
        public BreatheData {
            Check.notNegative("walls.breathe.amp", amp);
            Check.positive("walls.breathe.hz", hz);
        }
    }

    /** A wall node: part {@code part} on the {@code side} wall's inner face ({@code left} or {@code right}) at height {@code y}. */
    record WallNodeData(String part, String side, double y) {
        public WallNodeData {
            Check.that(side.equals("left") || side.equals("right"), "walls.nodes[].side: left or right, was " + side);
            Check.that(y >= 0 && y <= 540, "walls.nodes[].y: px below the top edge on the field, was " + y);
        }

        /** Whether it rides the left wall. */
        public boolean left() {
            return side.equals("left");
        }
    }

    /**
     * The drift in a halted arena: from phase {@code phase}'s start (its name) the angle follows {@code profile} ({@code
     * [t, degrees]} pairs on the phase's clock, positive to the right), the sideways drift {@code speed} × sin θ (QH-2,
     * the nominal arena speed); its one {@code reversal}; eased out over {@code ease_out} s after the phase.
     */
    record DriftData(
            String phase, double speed, List<List<Double>> profile, Optional<ReversalData> reversal, double easeOut) {
        public DriftData {
            profile = List.copyOf(Check.notEmpty("drift.profile", profile));
            profile.forEach(point -> Check.that(point.size() == 2, "drift.profile[]: [t, degrees]"));
            Check.positive("drift.speed", speed);
            Check.positive("drift.ease_out", easeOut);
        }
    }

    /**
     * The drift's reversal: told {@code tell} s ahead at the {@code at_kills}-th death of the phase's parts or at {@code
     * at_seconds} into the phase at the latest, running reversed {@code seconds}.
     */
    record ReversalData(int atKills, double atSeconds, double seconds, double tell) {
        public ReversalData {
            Check.notNegative("drift.reversal.at_kills", atKills);
            Check.positive("drift.reversal.seconds", seconds);
            Check.notNegative("drift.reversal.tell", tell);
            Check.that(atSeconds >= tell, "drift.reversal: at_seconds at least its tell");
        }
    }

    /**
     * The window parts: each of {@code parts} is closed (invulnerable, no target) except while it sings, while a
     * shield-breaker's {@code breaker} s hold lasts and while the EMP's {@code emp} s hold lasts.
     */
    record PartWindowsData(List<String> parts, double breaker, double emp) {
        public PartWindowsData {
            parts = List.copyOf(Check.notEmpty("part_windows.parts", parts));
            Check.notNegative("part_windows.breaker", breaker);
            Check.notNegative("part_windows.emp", emp);
        }
    }

    /**
     * The dark of phase {@code phase} (a look only, the game's): the ambient light {@code ambient} outside the headlight
     * and the light pools, a further {@code per_node} back with each of the phase's parts dead.
     */
    record DarkData(String phase, double ambient, double perNode) {
        public DarkData {
            Check.share("dark.ambient", ambient);
            Check.share("dark.per_node", perNode);
        }
    }

    /**
     * A difficulty's changes to the Heart ({@code heart} in its hook): the walls' {@code corridor} and {@code contact},
     * the drift's reversal ({@code reversal: false}: none), the dark's {@code ambient}, and {@code singly}: no
     * playbook draws two moves at once.
     */
    record HeartChange(
            Optional<Double> corridor,
            Optional<Double> contact,
            Optional<Boolean> reversal,
            Optional<Double> ambient,
            Optional<Boolean> singly) {
        public HeartChange {
            corridor.ifPresent(c -> Check.that(c > 0 && c < 480, "heart.corridor: px inside the field"));
            contact.ifPresent(c -> Check.notNegative("heart.contact", c));
            ambient.ifPresent(a -> Check.share("heart.ambient", a));
        }
    }
}
