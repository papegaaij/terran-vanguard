package vanguard.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * M10 part C (design/campaign Level 37, The Vrell turn and The falling city; design/world/jovian, Hazards &amp; set
 * pieces: the falling city, A.13 b): a backdrop's {@code growth} burst and its {@code fall}, presentation only (the
 * simulation never reads them; the game's growth and falling-city passes draw them, step C4a). The pieces they name are
 * the backdrop's {@code pieces}; ART-C2's generator (tools/art/backdrop_l37.py) writes both blocks from the level's
 * clock.
 */
public final class FallingCityData {
    private FallingCityData() {}

    /**
     * The turn's growth burst: over the ground on the screen at {@code t}, {@code piece} drawn where {@code reveal}'s
     * value is at most {@code (now − t) ÷ seconds × 255} (one texture and a threshold), {@code glow} additive under the
     * same reveal; it lies on the ground rows from {@code bottom} (a ground piece's rows, as a placed ground piece with
     * that bottom row).
     */
    public record Growth(double t, double seconds, String piece, String reveal, String glow, int bottom) {
        public Growth {
            Check.notNegative("growth.t", t);
            Check.positive("growth.seconds", seconds);
            Check.notNegative("growth.bottom", bottom);
        }

        /** The pieces it draws. */
        public List<String> pieces() {
            return List.of(piece, reveal, glow);
        }
    }

    /**
     * The fall: from {@code t} the city's ground pieces are not drawn and the {@code halves} are the ground, each on its
     * curve over {@code seconds}; the {@code shards} tumble in the widening gap, the {@code balloons} tear and the {@code
     * spires} topple (low-air pieces placed as usual), and the {@code veils} (cloud tops, round 75 F.6 redo) close over
     * the halves as they fade.
     */
    public record Fall(
            double t,
            double seconds,
            List<Half> halves,
            Optional<Shards> shards,
            Optional<List<Placed>> balloons,
            Optional<List<Spire>> spires,
            Optional<List<Veil>> veils) {
        public Fall {
            Check.notNegative("fall.t", t);
            Check.positive("fall.seconds", seconds);
            Check.notEmpty("fall.halves", halves);
            halves = List.copyOf(halves);
            balloons = balloons.map(List::copyOf);
            spires = spires.map(List::copyOf);
            veils = veils.map(List::copyOf);
            for (Half half : halves) {
                Check.that(
                        half.keys().getLast().get(0) <= seconds, "fall.halves: the last key within the fall's seconds");
            }
        }

        /** Every piece it draws. */
        public List<String> pieces() {
            List<String> pieces = new java.util.ArrayList<>();
            halves.forEach(half -> {
                pieces.add(half.piece());
                half.charred().ifPresent(pieces::add);
                half.glow().ifPresent(pieces::add);
            });
            shards.ifPresent(s -> pieces.add(s.piece()));
            balloons.ifPresent(list -> list.forEach(b -> pieces.add(b.piece())));
            spires.ifPresent(list -> list.forEach(s -> pieces.add(s.piece())));
            veils.ifPresent(list -> list.forEach(v -> pieces.add(v.piece())));
            return pieces;
        }
    }

    /**
     * A falling half: {@code piece} (the city's own pixels on its rows: its left column {@code x0}, its bottom ground
     * row {@code bottom}), its {@code char} rim (normal) and {@code glow} (additive) drawn with it, and its {@code keys}
     * {@code [u, x, y, scale, turn, shade, alpha]}: u s after the fall's t; the screen centre of its image (y down from
     * the top edge); its scale toward the field's centre; its turn, ° clockwise; the share it has darkened; its opacity
     * (round 75 F.6 redo; its rim, glow and the shards with it; 1 when left out). Interpolated linearly; the first key
     * is the ground's place at t.
     */
    public record Half(
            String piece,
            double x0,
            int bottom,
            @JsonProperty("char") Optional<String> charred,
            Optional<String> glow,
            List<List<Double>> keys) {
        public Half {
            Check.notEmpty("fall.halves.keys", keys);
            keys = keys.stream().map(List::copyOf).toList();
            for (int i = 0; i < keys.size(); i++) {
                List<Double> key = keys.get(i);
                Check.that(
                        key.size() == 6 || key.size() == 7,
                        "fall.halves.keys[" + i
                                + "]: [u, x, y, scale, turn, shade] or [u, x, y, scale, turn, shade, alpha]");
                Check.that(i > 0 || key.get(0) == 0, "fall.halves.keys[0]: u = 0 (the ground's place at t)");
                Check.that(i == 0 || key.get(0) > keys.get(i - 1).get(0), "fall.halves.keys[" + i + "]: in u order");
                Check.positive("fall.halves.keys[" + i + "].scale", key.get(3));
                Check.share("fall.halves.keys[" + i + "].shade", key.get(5));
                if (key.size() == 7) {
                    Check.share("fall.halves.keys[" + i + "].alpha", key.get(6));
                }
            }
        }
    }

    /** The shards tumbling in the gap between the halves' cut edges, {@code from}–{@code to} s after the fall's t. */
    public record Shards(String piece, double from, double to) {
        public Shards {
            Check.notNegative("fall.shards.from", from);
            Check.that(to > from, "fall.shards.to: after from");
        }
    }

    /** A low-air piece placed as usual ({@code t}, {@code x}) that plays its frames once from the fall's t. */
    public record Placed(String piece, double t, double x) {}

    /** A spire placed as usual ({@code t}, {@code x}): upright until {@code topple_at} s after the fall's t, then it topples. */
    public record Spire(String piece, double t, double x, double toppleAt) {
        public Spire {
            Check.notNegative("fall.spires.topple_at", toppleAt);
        }
    }

    /**
     * A veil (round 75 F.6 redo): a patch of the cloud tops drawn after the halves (normal blend, unshaded, unscaled),
     * centred on its {@code keys} {@code [u, x, y, alpha]}: u s after the fall's t, the screen centre (y down from the top
     * edge), its opacity; linear between the keys, nothing drawn outside their span.
     */
    public record Veil(String piece, List<List<Double>> keys) {
        public Veil {
            Check.notEmpty("fall.veils.keys", keys);
            keys = keys.stream().map(List::copyOf).toList();
            for (int i = 0; i < keys.size(); i++) {
                List<Double> key = keys.get(i);
                Check.that(key.size() == 4, "fall.veils.keys[" + i + "]: [u, x, y, alpha]");
                Check.notNegative("fall.veils.keys[" + i + "].u", key.get(0));
                Check.that(i == 0 || key.get(0) > keys.get(i - 1).get(0), "fall.veils.keys[" + i + "]: in u order");
                Check.share("fall.veils.keys[" + i + "].alpha", key.get(3));
            }
        }
    }
}
