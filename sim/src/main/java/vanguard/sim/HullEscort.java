package vanguard.sim;

/**
 * M9 part D, a hull escort (design/allies/mining-tug; design/campaign Level 31, The tug in this level; {@link
 * ObjectiveSpec.Hull}): one huge ally of sections on the ground layer. The {@link Convoy} of its escort keeps one {@link
 * Ally} per section (so the tracker, the radio's ally cues, the fail and the pay work as for any escort); this class
 * places them and holds what is the hull's own: the entry from below the bottom edge, the station with its sway on the
 * level clock, the climb-out, the nearest living section a hooked attack goes for, a section's hit box, the hooked hits
 * of the step and the air links' cooldowns per link and section. A lost section keeps its place on the hull (dark,
 * presentation only) and fire passes through it. Everything is allocated up front.
 */
final class HullEscort {
    private final ObjectiveSpec.Hull spec;
    private final Ally[] sections;
    private final AllySpec[] sectionSpecs;
    private final int enterTicks;
    private final int climbTick;
    private final int climbTicks;
    private final double startY;
    private final double homeY;
    /** The hooked hits each section took this step, applied after the moves ({@link AllyPasses}). */
    private final double[] pending;
    /** Per air link slot ({@link AirLinks#SLOTS}) and section, the steps left of its touch's cooldown. */
    private final int[] linkCooldowns;
    /** By wave index: the waves whose units the hook leaves alone. */
    private final boolean[] unhooked;

    private double x;
    private double y;
    private double prevX;
    private double prevY;

    /**
     * @param hull the hull's spec
     * @param allies the convoy's units, one per section
     * @param tug the whole hull's ally spec (its slug for the sections' specs)
     */
    HullEscort(ObjectiveSpec.Hull hull, Ally[] allies, AllySpec tug) {
        spec = hull;
        sections = allies;
        int n = hull.sections().size();
        sectionSpecs = new AllySpec[n];
        for (int k = 0; k < n; k++) {
            ObjectiveSpec.HullSection section = hull.sections().get(k);
            sectionSpecs[k] = new AllySpec(
                    tug.slug(),
                    section.box(),
                    section.box(),
                    section.hp(),
                    false,
                    0,
                    tug.smokeBelow(),
                    0,
                    false,
                    false,
                    0,
                    0,
                    0,
                    0,
                    0,
                    hull.dive(),
                    false,
                    0,
                    0);
        }
        enterTicks = Math.max(1, SimStep.ticks(hull.enterSeconds()));
        climbTick = SimStep.ticks(hull.climb().t());
        climbTicks = Math.max(1, SimStep.ticks(hull.climb().seconds()));
        startY = -hull.size().height() / 2;
        homeY = PlayField.HEIGHT + hull.size().height() / 2;
        pending = new double[n];
        linkCooldowns = new int[AirLinks.SLOTS * n];
        int most =
                hull.unhookedWaves().stream().mapToInt(Integer::intValue).max().orElse(-1);
        unhooked = new boolean[most + 1];
        for (int wave : hull.unhookedWaves()) {
            unhooked[wave] = true;
        }
    }

    /** Whether the units of wave {@code wave} (-1: launched, no wave) are hooked when their kind is. */
    boolean hooked(int wave) {
        return wave < 0 || wave >= unhooked.length || !unhooked[wave];
    }

    ObjectiveSpec.Hull spec() {
        return spec;
    }

    /** Section {@code k}'s spec: its box (size and hit box), its HP, the hull's dive damage. */
    AllySpec sectionSpec(int k) {
        return sectionSpecs[k];
    }

    /** Back to the level start: below the bottom edge, every section rising and untouchable at full HP. */
    void reset() {
        for (int k = 0; k < sections.length; k++) {
            sections[k].resetAir(
                    Ally.State.LIFTING, 0, 0, spec.sections().get(k).hp(), 1);
        }
        java.util.Arrays.fill(pending, 0);
        java.util.Arrays.fill(linkCooldowns, 0);
        place(0);
        prevX = x;
        prevY = y;
        for (Ally section : sections) {
            section.settle();
        }
    }

    /**
     * One step at {@code levelTick}: the hull rises into its station (smoothstep, untouchable), flies it with its sway,
     * climbs out from the climb-out's time (s², untouchable; a section alive then is home) and is home off the top
     * edge; each section sits at its offset, a lost one too. The air links' cooldowns run.
     */
    void update(int levelTick) {
        prevX = x;
        prevY = y;
        for (Ally section : sections) {
            section.remember();
        }
        place(levelTick);
        for (int i = 0; i < linkCooldowns.length; i++) {
            if (linkCooldowns[i] > 0) {
                linkCooldowns[i]--;
            }
        }
    }

    private void place(int levelTick) {
        ObjectiveSpec.Station station = spec.station();
        double seconds = levelTick * SimStep.SECONDS;
        double sx = station.xAt(seconds);
        double sy = station.yAt(seconds);
        Ally.State state;
        if (levelTick < enterTicks) {
            double s = (double) levelTick / enterTicks;
            double e = s * s * (3 - 2 * s);
            x = sx;
            y = startY + (sy - startY) * e;
            state = Ally.State.LIFTING;
        } else if (levelTick < climbTick) {
            x = sx;
            y = sy;
            state = Ally.State.FLYING;
        } else if (levelTick < climbTick + climbTicks) {
            double s = (double) (levelTick - climbTick) / climbTicks;
            x = sx;
            y = sy + (homeY - sy) * s * s;
            state = Ally.State.CLIMBING;
        } else {
            x = sx;
            y = homeY;
            state = Ally.State.HOME;
        }
        for (int k = 0; k < sections.length; k++) {
            Ally section = sections[k];
            ObjectiveSpec.HullSection at = spec.sections().get(k);
            boolean lost = section.lost();
            if (!lost && section.state() != state) {
                section.state(state);
            }
            section.fly(x + at.dx(), y + at.dy(), 1, lost || state != Ally.State.FLYING);
        }
    }

    /**
     * The section a hooked attack from (fromX, fromY) goes for now: the nearest living section that can be hit (centre
     * to centre; the first in hull order on a tie); -1 while none can (the entry, the climb-out, every section lost).
     */
    int nearest(double fromX, double fromY) {
        double best = Double.POSITIVE_INFINITY;
        int found = -1;
        for (int k = 0; k < sections.length; k++) {
            Ally section = sections[k];
            if (!section.touchable()) {
                continue;
            }
            double dx = section.x() - fromX;
            double dy = section.y() - fromY;
            double distance = dx * dx + dy * dy;
            if (distance < best) {
                best = distance;
                found = k;
            }
        }
        return found;
    }

    /** Whether section {@code k} can be hit now (alive, the hull at its station). */
    boolean touchable(int k) {
        return sections[k].touchable();
    }

    /** Section {@code k}'s centre, px from the left edge. */
    double sectionX(int k) {
        return sections[k].x();
    }

    /** Section {@code k}'s centre, px from the bottom edge (y up). */
    double sectionY(int k) {
        return sections[k].y();
    }

    /** Whether section {@code k} can be hit now and its hit box overlaps {@code box} around (bx, by). */
    boolean touches(int k, double bx, double by, Hitbox box) {
        Ally section = sections[k];
        return section.touchable() && spec.sections().get(k).box().overlaps(section.x(), section.y(), box, bx, by);
    }

    /** The first section (hull order) that can be hit now whose hit box overlaps {@code box} around (bx, by); -1 for none. */
    int touching(double bx, double by, Hitbox box) {
        for (int k = 0; k < sections.length; k++) {
            if (touches(k, bx, by, box)) {
                return k;
            }
        }
        return -1;
    }

    /**
     * Whether section {@code k} can be hit now and the segment from (ax, ay) to (bx, by), {@code halfWidth} px either
     * side, crosses its hit box.
     */
    boolean crosses(int k, double ax, double ay, double bx, double by, double halfWidth) {
        Ally section = sections[k];
        if (!section.touchable()) {
            return false;
        }
        Hitbox box = spec.sections().get(k).box();
        return Hull.segmentInBox(
                ax - section.x(),
                bx - ax,
                box.width() / 2 + halfWidth,
                ay - section.y(),
                by - ay,
                box.height() / 2 + halfWidth);
    }

    /** Section {@code k} takes {@code hits} from a hooked attack this step (applied after the moves). */
    void hit(int k, double hits) {
        if (k >= 0 && hits > 0) {
            pending[k] += hits;
        }
    }

    /** The hooked hits section {@code k} took this step; they start over. */
    double takePending(int k) {
        double hits = pending[k];
        pending[k] = 0;
        return hits;
    }

    /**
     * Whether air link {@code slot}'s touch may hurt section {@code k} now (outside its cooldown, which starts).
     */
    boolean linkTouch(int slot, int k) {
        int i = slot * sections.length + k;
        if (linkCooldowns[i] > 0) {
            return false;
        }
        linkCooldowns[i] = Math.max(1, SimStep.ticks(spec.linkCooldownSeconds()));
        return true;
    }

    /** A link slot is free again (its group gone): its cooldowns start over. */
    void linkFreed(int slot) {
        for (int k = 0; k < sections.length; k++) {
            linkCooldowns[slot * sections.length + k] = 0;
        }
    }

    /** The hull's centre, px from the left edge (derived from the level clock). */
    double x() {
        return x;
    }

    /** The hull's centre, px from the bottom edge (y up). */
    double y() {
        return y;
    }

    /** The hull's centre between the previous and the current step; {@code alpha} in [0, 1]. */
    double renderX(double alpha) {
        return prevX + (x - prevX) * alpha;
    }

    /** As {@link #renderX(double)}, y up. */
    double renderY(double alpha) {
        return prevY + (y - prevY) * alpha;
    }

    /** The cooldowns (the sections are hashed with the convoy's units; the position derives from the clock). */
    void addTo(StateHash hash) {
        for (int cooldown : linkCooldowns) {
            hash.add(cooldown);
        }
    }
}
