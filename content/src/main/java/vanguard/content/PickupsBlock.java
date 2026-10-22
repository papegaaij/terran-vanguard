package vanguard.content;

import java.util.List;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * The level's {@code secrets}, {@code pickups} and point {@code drops}: the hidden crates and the pickups the script
 * places, carries or releases.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Drop}).
 */
public sealed interface PickupsBlock permits LevelData {
    /**
     * M5 part G (design/allies, CDF supply drone; user decision G6 = a): a pickup released at the point
     * ({@code x}, {@code y}) at {@code t} (x px from the left edge, y px from the bottom edge, y up), then an
     * ordinary pickup; the drone that carries it there is presentation only: its {@code sprite} set (Level
     * 13's {@code supply-drone}), its arc from the defended ally's helipad ending at the point at {@code t}.
     */
    public record Drop(double t, double x, double y, Pickup pickup, Optional<String> sprite) {
        public Drop {
            Check.notNegative("t", t);
            Check.that(x >= 0 && x <= PlayField.WIDTH, "x: on the play field, was " + x);
            Check.that(y >= 0 && y < PlayField.HEIGHT, "y: inside the play field (below the top edge), was " + y);
        }
    }

    /** A hidden crate worth {@code crate} credits, with Rook's (or anyone's) line when found. */
    /**
     * A secret: its hidden crate's credits, or (part F) a {@code data_core} that pays none and
     * unlocks a shop item early; its radio line plays when it is found (a data core's when it is
     * collected).
     */
    public record Secret(String name, int crate, LevelData.RadioLine radio, Optional<DataCore> dataCore) {
        public Secret {
            if (dataCore.isPresent()) {
                Check.notNegative("crate", crate);
            } else {
                Check.positive("crate", crate);
            }
        }
    }

    /** A data core: the shop item it {@code unlocks} (design/systems/economy, Data cores). */
    public record DataCore(String unlocks) {}

    /**
     * A pickup carried by a unit of the wave starting at {@code droppedBy.wave}, dropped when it is
     * destroyed, by the last unit of the ground-target group {@code droppedBy.group}, or by one of
     * the boss's {@code droppedBy.parts}.
     *
     * @param skip part G: the difficulties it is left out on (Level 07's hard: no armour patch
     *     before the boss)
     * @param at M5 part F: instead of a carrier, where it is placed: it enters at the top edge at
     *     {@code [t, x]} and drifts like any pickup (Level 12's armour patch in the eye)
     */
    public record PlacedPickup(
            Pickup pickup, Optional<Carrier> droppedBy, Optional<List<String>> skip, Optional<LevelData.Placement> at) {
        public PlacedPickup {
            skip.ifPresent(names -> names.forEach(Difficulty::of));
            Check.that(droppedBy.isPresent() != at.isPresent(), "a pickup is dropped_by a carrier or placed at [t, x]");
        }

        /** A pickup a carrier drops. */
        public PlacedPickup(Pickup pickup, Carrier droppedBy, Optional<List<String>> skip) {
            this(pickup, Optional.of(droppedBy), skip, Optional.empty());
        }

        /** Its carrier; only for a pickup that is not placed ({@link #carried()}). */
        public Carrier carrier() {
            return droppedBy.orElseThrow(() -> new IllegalStateException("a placed pickup has no carrier"));
        }

        /** Whether a carrier drops it (not placed at a time and x). */
        public boolean carried() {
            return droppedBy.isPresent();
        }

        /** Whether it drops on {@code difficulty}. */
        public boolean dropsOn(Difficulty difficulty) {
            return skip.map(names -> names.stream().map(Difficulty::of).noneMatch(difficulty::equals))
                    .orElse(true);
        }
    }

    /**
     * The {@code unit} ({@code first}, {@code second} or {@code last}) of the wave starting at
     * {@code wave} seconds, the {@code last} unit of a ground-target {@code group} to die (when the
     * group is cleared), or (part G) the {@code unit}-th of the level boss's {@code parts} to be
     * shot off (Level 07's first destroyed bay sac); parts lost in the boss's death drop nothing. M7 part D: a wave's
     * carrier may name its {@code enemy}, the {@code unit} then counted among that enemy's units of the wave only (a
     * mixed wave's: Level 17's t=124 Choir Herald among its bombers drops the shield cell). M8 part C (design/campaign
     * Level 23, the shield cell): {@code head_first: true}, a segment chain's carrier drops it only when its head is
     * destroyed before any other part (the first Eel Swarm's head-first kill); always if not given.
     */
    public record Carrier(
            Optional<Double> wave,
            Optional<String> group,
            Optional<List<String>> parts,
            CarrierUnit unit,
            Optional<String> enemy,
            Optional<Boolean> headFirst) {
        /** Without M7 part D's enemy. */
        public Carrier(Optional<Double> wave, Optional<String> group, Optional<List<String>> parts, CarrierUnit unit) {
            this(wave, group, parts, unit, Optional.empty(), Optional.empty());
        }

        /** Without M8 part C's head-first drop. */
        public Carrier(
                Optional<Double> wave,
                Optional<String> group,
                Optional<List<String>> parts,
                CarrierUnit unit,
                Optional<String> enemy) {
            this(wave, group, parts, unit, enemy, Optional.empty());
        }

        public Carrier {
            Check.that(
                    headFirst.isEmpty() || (headFirst.get() && wave.isPresent()),
                    "dropped_by.head_first: only true, on a wave's carrier");
            Check.that(enemy.isEmpty() || wave.isPresent(), "dropped_by.enemy: only a wave's carrier names its enemy");
            Check.that(
                    (wave.isPresent() ? 1 : 0) + (group.isPresent() ? 1 : 0) + (parts.isPresent() ? 1 : 0) == 1,
                    "dropped_by: give a wave, a group or the boss's parts");
            Check.that(
                    group.isEmpty() || unit == CarrierUnit.LAST,
                    "dropped_by: a group's pickup comes from its last unit");
            parts.ifPresent(names -> {
                Check.notEmpty("parts", names);
                Check.that(
                        unit != CarrierUnit.SECOND || names.size() >= 2,
                        "dropped_by: the second of the parts needs two of them");
            });
        }

        /** M8 part C: whether only a head-first kill of the carrier drops it ({@code head_first: true}). */
        public boolean dropsHeadFirst() {
            return headFirst.orElse(false);
        }

        /** The carrier wave's time; NaN for a group's pickup. */
        public double waveT() {
            return wave.orElse(Double.NaN);
        }
    }

    public enum CarrierUnit {
        FIRST,
        SECOND,
        LAST
    }
}
