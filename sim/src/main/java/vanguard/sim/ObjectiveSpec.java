package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A level's objectives and the allies beside them (refactoring step LS-3, {@link LevelScript#objectives()}): the
 * secondary objective, the primary's groups, escort, naval convoy or defend, the ground allies and tows, and the hold
 * zones and collapse that wait for the objective groups. {@link LevelScript} checks them against the rest of the level.
 *
 * @param secondary the secondary objective
 * @param targets the {@code destroy-targets} primary objective's groups (Level 05's batteries): the ground units'
 *     {@link GroundUnit#group()} indexes them; empty for another primary
 * @param escort the {@code escort} primary objective's convoy, instead of only reaching the end
 * @param defend M5 part G: the {@code defend} primary objective (Level 13's Nansen Relay): one ally on the ground that
 *     must survive its synchronisation; never beside an escort or a naval convoy
 * @param convoy M5 part E: a naval convoy outside the objectives (Level 11's cargo ships and frigate), which never fails
 *     the mission; never beside an {@code escort}
 * @param groundAllies M7 part D: allies standing on the ground outside the objectives (Level 17's Ares Landing domes,
 *     {@link GroundAllies}), which never fail the mission; never beside an escort, a naval convoy or a defend primary,
 *     and not with holds or an arena (their places follow the scroll's time)
 * @param tows part G: friendly craft towing a secret's crate on a cable (Level 07's lifeboat tow)
 * @param holds M5 part C: the hold zones (Level 09's node clusters), where the scroll eases down until their groups
 *     are destroyed and the level clock slows with it
 * @param collapse M5 part C: the collapse (Level 09's arcology) once its groups are cleared
 */
public record ObjectiveSpec(
        Secondary secondary,
        List<String> targets,
        Optional<Escort> escort,
        Optional<Defend> defend,
        Optional<Naval> convoy,
        Optional<GroundAllies> groundAllies,
        List<TowSpec> tows,
        List<Hold> holds,
        Optional<Collapse> collapse) {
    /** No objective but reaching the end: {@link Secondary#NONE}, no groups, convoy, allies, tows or holds. */
    public static final ObjectiveSpec NONE = new ObjectiveSpec(
            Secondary.NONE,
            List.of(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            List.of(),
            List.of(),
            Optional.empty());

    public ObjectiveSpec {
        holds = List.copyOf(holds);
        int groupCount = groups(targets, secondary).size();
        for (Hold hold : holds) {
            for (int group : hold.groups()) {
                if (group < 0 || group >= groupCount) {
                    throw new IllegalArgumentException("a hold waits for groups of the level, not " + group);
                }
            }
        }
        for (int group : collapse.map(Collapse::groups).orElse(List.of())) {
            if (group < 0 || group >= groupCount) {
                throw new IllegalArgumentException("a collapse waits for groups of the level, not " + group);
            }
        }
        targets = List.copyOf(targets);
        tows = List.copyOf(tows);
        if (!targets.isEmpty() && secondary.byGroups()) {
            throw new IllegalArgumentException("groups belong to the primary or to the secondary objective");
        }
        if (convoy.isPresent() && escort.isPresent()) {
            throw new IllegalArgumentException("a naval convoy is not an escort: a level has one or the other");
        }
    }

    /** The names of the groups the ground units' {@link GroundUnit#group()} indexes: the primary's or the secondary's. */
    public List<String> groups() {
        return groups(targets, secondary);
    }

    private static List<String> groups(List<String> targets, Secondary secondary) {
        return targets.isEmpty() ? secondary.groups() : targets;
    }

    /** These objectives with another {@code secondary}. */
    public ObjectiveSpec withSecondary(Secondary secondary) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code targets}. */
    public ObjectiveSpec withTargets(List<String> targets) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code escort}. */
    public ObjectiveSpec withEscort(Optional<Escort> escort) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code defend}. */
    public ObjectiveSpec withDefend(Optional<Defend> defend) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code convoy}. */
    public ObjectiveSpec withConvoy(Optional<Naval> convoy) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code groundAllies}. */
    public ObjectiveSpec withGroundAllies(Optional<GroundAllies> groundAllies) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code tows}. */
    public ObjectiveSpec withTows(List<TowSpec> tows) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code holds}. */
    public ObjectiveSpec withHolds(List<Hold> holds) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /** These objectives with another {@code collapse}. */
    public ObjectiveSpec withCollapse(Optional<Collapse> collapse) {
        return new ObjectiveSpec(secondary, targets, escort, defend, convoy, groundAllies, tows, holds, collapse);
    }

    /**
     * M7 part D, a ground ally (design/allies, Ares Landing dome; design/campaign Level 17, The Ares Landing
     * domes): {@code ally} standing on the ground at {@code x} px from the left edge, its footprint's lower
     * edge entering the top edge at {@code enterSeconds} on the level clock; it scrolls through with the
     * ground from there.
     *
     * @param name its name in the radio ({@code {ally}}: "Bradbury")
     */
    public record GroundAlly(AllySpec ally, String name, double x, double enterSeconds) {
        public GroundAlly {
            java.util.Objects.requireNonNull(ally);
            java.util.Objects.requireNonNull(name);
            if (!(x >= 0 && x <= PlayField.WIDTH) || !(enterSeconds >= 0)) {
                throw new IllegalArgumentException(
                        ally.slug() + ": a ground ally stands on the play field and enters in the level");
            }
        }
    }

    /**
     * M7 part D, the level's ground allies outside the objectives (design/campaign Level 17, The Ares Landing
     * domes; Q.25b, M16 = a): its units stand on the ground and scroll through with it; each takes the
     * hooked attacks its {@link AllySpec} names (a bomb run's {@link AllySpec#bombs()}, a lob's direct hit,
     * a roll's {@link AllySpec#rolls()}) as hits, without regeneration; a broken unit lies there as a wreck.
     * They never fail the mission. Defaults pending the user's review (round 40): the hook is per enemy
     * kind, as Level 13's.
     *
     * @param units in the order of the level's tracker pips (Level 17: Bradbury, Lowell, Schiaparelli,
     *     Burroughs, Eden)
     * @param targetedBy per enemy slug its hook mode (the level's {@code targeted_by} at the difficulty);
     *     enemies not listed are not hooked. A hooked ({@link HookMode#ALWAYS}) mine layer flies a bomb run
     * @param bombRow a bomb run's row, px below the top edge (160): a unit entering at the front descends
     *     to it; one entering from a side keeps its entry row
     * @param bombSlot px between the bomb run targets of a wave's units, across, centred on the dome (40)
     * @param bombRunIn round 41 (the user's decision of 2026-10-19): s a bomb run's unit flies on the play field
     *     before it may drop its load (its run-in, so a fast player can shoot it first); over a dome before then it
     *     holds its load and keeps steering. 0 (the default) drops at once
     */
    public record GroundAllies(
            List<GroundAlly> units,
            java.util.Map<String, HookMode> targetedBy,
            double bombRow,
            double bombSlot,
            double bombRunIn) {
        public GroundAllies {
            units = List.copyOf(units);
            targetedBy = java.util.Map.copyOf(targetedBy);
            if (units.isEmpty() || units.size() > 31) {
                throw new IllegalArgumentException("ground allies are 1 to 31 units: " + units.size());
            }
            if (!(bombRow >= 0 && bombRow <= PlayField.HEIGHT) || !(bombSlot >= 0)) {
                throw new IllegalArgumentException(
                        "a bomb run's row lies on the play field, its slots 0 or more apart");
            }
            if (!(bombRunIn >= 0)) {
                throw new IllegalArgumentException("a bomb run's run-in is 0 s or more: " + bombRunIn);
            }
        }

        /** Without round 41's run-in: a bomb run drops at once. */
        public GroundAllies(
                List<GroundAlly> units, java.util.Map<String, HookMode> targetedBy, double bombRow, double bombSlot) {
            this(units, targetedBy, bombRow, bombSlot, 0);
        }

        /** Round 41: these ground allies with a bomb run's run-in of {@code seconds}. */
        public GroundAllies withBombRunIn(double seconds) {
            return new GroundAllies(units, targetedBy, bombRow, bombSlot, seconds);
        }

        /** The hook mode of the enemy {@code slug}: {@link HookMode#NONE} when not listed. */
        public HookMode mode(String slug) {
            return targetedBy.getOrDefault(slug, HookMode.NONE);
        }
    }

    /**
     * M5 part C (design/campaign Level 09, user decisions D2 and D3 = a): a hold zone. When the first
     * unit of its {@code groups} reaches {@code depth} px below the top edge, the scroll eases over
     * {@code rampSeconds} to {@code speed} px/s and stays there until every unit of those groups is
     * gone (a hold over the {@link Collapse collapse}'s groups whose clearing started it: until its
     * fall ends); then it eases back to the section's speed. There is no timeout: a unit that leaves
     * the screen alive fails a destroy-targets primary at once.
     *
     * @param groups indexes into {@link LevelScript#groups()}
     */
    public record Hold(List<Integer> groups, double depth, double speed, double rampSeconds) {
        public Hold {
            groups = List.copyOf(groups);
            if (groups.isEmpty()) {
                throw new IllegalArgumentException("a hold waits for at least one group");
            }
            if (!(speed > 0) || !(rampSeconds > 0)) {
                throw new IllegalArgumentException("a hold eases to a speed over a ramp");
            }
        }

        /** Whether it waits for group {@code group}. */
        public boolean waitsFor(int group) {
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i) == group) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * M5 part C (design/campaign Level 09, user decision D5 = a; round 31's look c): the collapse.
     * When every unit of its {@code groups} is destroyed (none got away), its tower leans for {@code
     * warningSeconds} (the warning), then drops for {@code dropSeconds}; at the impact its blast rolls
     * out from the tower's foot: a ring round the footprint's centre ({@code x}, half way between
     * {@code bottom} and {@code top}) whose radius grows from {@code blastFrom} to {@code blastTo} px
     * over {@code blastSeconds}, eased 1 − (1 − t)² (the dust blast's front). Every ground unit in the
     * band (from {@code bottom} to {@code top}, the whole width) whose centre the ring reaches is
     * destroyed, paying and scoring as an Airstrike kill; air units and the player are untouched. Its
     * dust settles {@code settleSeconds} after the impact (the heavy dust's ramp out, at least the
     * blast's time): a hold over its groups lasts until then (user decision, 2026-10-07). The
     * band lies on the ground (ground positions: px from the screen's bottom edge at the level start,
     * as the backdrop's pieces), so it scrolls with the ground and covers the same ground (the tower
     * that falls) whenever its groups are cleared. The real clock times it, not the level clock.
     *
     * @param groups indexes into {@link LevelScript#groups()}
     * @param x the footprint's centre, px from the play field's left edge
     */
    public record Collapse(
            List<Integer> groups,
            double warningSeconds,
            double dropSeconds,
            double blastSeconds,
            double blastFrom,
            double blastTo,
            double settleSeconds,
            double x,
            double bottom,
            double top) {
        public Collapse {
            groups = List.copyOf(groups);
            if (groups.isEmpty()) {
                throw new IllegalArgumentException("a collapse waits for at least one group");
            }
            if (!(warningSeconds >= 0) || !(dropSeconds > 0) || !(blastSeconds > 0)) {
                throw new IllegalArgumentException("a collapse drops after its warning, then its blast rolls out");
            }
            if (!(blastFrom >= 0) || !(blastTo > blastFrom)) {
                throw new IllegalArgumentException("a collapse's blast grows outward");
            }
            if (!(settleSeconds >= blastSeconds)) {
                throw new IllegalArgumentException("a collapse's dust settles after its blast");
            }
            if (!(top > bottom)) {
                throw new IllegalArgumentException("a collapse's band has its top ahead of its bottom");
            }
        }

        /** The blast's kill radius {@code seconds} after the impact, px: eased out from {@code blastFrom} to {@code blastTo}. */
        public double blastRadius(double seconds) {
            double t = Math.clamp(seconds / blastSeconds, 0, 1);
            return blastFrom + (blastTo - blastFrom) * (1 - (1 - t) * (1 - t));
        }
    }

    /**
     * Part G: a friendly craft drifting on the air layer that tows a secret's crate on a cable
     * (Level 07's lifeboat tow). The {@code boat} enters at the top edge at {@code t} with its centre
     * at {@code x} and drifts at ({@code vx}, {@code vy}) px/s, y up; the {@code pod} hangs at
     * ({@code podDx}, {@code podDy}) from the boat's centre. Shots, bullets and the ship pass the boat
     * and the pod; only the {@code cable}, a hit box midway between them, takes the player's shots,
     * and {@code hits} of them cut it: the pod falls free as the hidden crate of {@code secret}.
     *
     * @param boat the boat's box (its sprite), px
     * @param pod the pod's box, px
     * @param cable the cable's hit box, px, centred midway between the boat's and the pod's centres
     * @param crateCredits the crate's credits
     * @param secret the secret's name, for its radio cue
     * @param beamTouch M9 (round 64 C.1 a, the Act 5 tows): a beam touching the cable box (the housing) counts one
     *     hit per {@link Tow#BEAM_HIT_TICKS} steps of contact, the first at once; off for Level 07's lifeboat
     */
    public record TowSpec(
            double t,
            double x,
            double vx,
            double vy,
            Hitbox boat,
            Hitbox pod,
            double podDx,
            double podDy,
            Hitbox cable,
            int hits,
            int crateCredits,
            String secret,
            boolean beamTouch) {
        public TowSpec {
            if (!(vy < 0) || hits < 1) {
                throw new IllegalArgumentException(secret + ": a tow drifts down the screen and its cable takes hits");
            }
        }

        /** A tow whose cable counts the player's shots only (Level 07's lifeboat). */
        public TowSpec(
                double t,
                double x,
                double vx,
                double vy,
                Hitbox boat,
                Hitbox pod,
                double podDx,
                double podDy,
                Hitbox cable,
                int hits,
                int crateCredits,
                String secret) {
            this(t, x, vx, vy, boat, pod, podDx, podDy, cable, hits, crateCredits, secret, false);
        }
    }

    /**
     * The {@code escort} primary objective (design/allies; design/campaign, Level 04): a convoy of
     * {@code ally} units, one per station, rolling in from the bottom edge one every
     * {@code enterInterval} seconds from {@code enterSeconds} at {@code enterSpeed} px/s up the
     * screen to their stations, then following the road. Each unit alive at the level end pays
     * {@code credits} (before the credit factor); the objective fails when every unit is lost. M5
     * part D: with {@code air}, an air escort instead (Level 10's shuttles): the units hold stations
     * in a band with no road, see {@link Air}; then {@code stations} are the stations' heights and
     * the {@code enter} numbers are not used ({@link #air(AllySpec, Air, int)}).
     *
     * @param stations the units' centre heights, px from the bottom edge (y up), the leading unit first
     * @param targetedBy the enemies whose aimed attacks use the target-the-objective hook in mode
     *     {@code nearest}: each aimed shot goes at the ship or the nearest unit, whichever is closer
     * @param air M5 part D: the air escort's stations, liftoff, climb-out and scripted loss
     */
    public record Escort(
            AllySpec ally,
            List<Double> stations,
            double enterSeconds,
            double enterInterval,
            double enterSpeed,
            int credits,
            List<String> targetedBy,
            Optional<Air> air,
            Optional<Hull> hull) {
        public Escort {
            stations = List.copyOf(stations);
            targetedBy = List.copyOf(targetedBy);
            java.util.Objects.requireNonNull(hull);
            if (hull.isPresent()
                    && (air.isPresent()
                            || !targetedBy.isEmpty()
                            || stations.size() != hull.get().sections().size())) {
                throw new IllegalArgumentException(
                        "a hull escort has one station per section and its own hook (M9 part D, the mining tug)");
            }
            if (stations.isEmpty() || !(enterSpeed > 0) || enterInterval < 0 || credits < 0) {
                throw new IllegalArgumentException("a convoy has units that roll in at a speed");
            }
            if (air.isPresent() && air.get().stations().size() != stations.size()) {
                throw new IllegalArgumentException("an air escort has one station per unit");
            }
            if (air.isPresent() && !targetedBy.isEmpty()) {
                throw new IllegalArgumentException("an air escort has no target-the-objective hook");
            }
        }

        /** A ground convoy (Level 04). */
        public Escort(
                AllySpec ally,
                List<Double> stations,
                double enterSeconds,
                double enterInterval,
                double enterSpeed,
                int credits,
                List<String> targetedBy) {
            this(ally, stations, enterSeconds, enterInterval, enterSpeed, credits, targetedBy, Optional.empty());
        }

        /** Without M9 part D's hull: a ground convoy or (M5 part D) an air escort. */
        public Escort(
                AllySpec ally,
                List<Double> stations,
                double enterSeconds,
                double enterInterval,
                double enterSpeed,
                int credits,
                List<String> targetedBy,
                Optional<Air> air) {
            this(ally, stations, enterSeconds, enterInterval, enterSpeed, credits, targetedBy, air, Optional.empty());
        }

        /**
         * M9 part D (design/allies/mining-tug; Level 31): a hull escort, {@code ally} (the whole hull's spec: its slug,
         * its footprint) flying {@code hull}, each section home paying its own credits.
         */
        public static Escort hull(AllySpec ally, Hull hull) {
            return new Escort(
                    ally,
                    hull.sections().stream()
                            .map(section -> hull.station().y() + section.dy())
                            .toList(),
                    0,
                    0,
                    1,
                    0,
                    List.of(),
                    Optional.empty(),
                    Optional.of(hull));
        }

        /** M5 part D: an air escort of {@code ally} units flying {@code air}, each saveable unit home paying {@code credits}. */
        public static Escort air(AllySpec ally, Air air, int credits) {
            return new Escort(
                    ally,
                    air.stations().stream().map(Station::y).toList(),
                    0,
                    0,
                    1,
                    credits,
                    List.of(),
                    Optional.of(air));
        }

        /** The number of units. */
        public int units() {
            return stations.size();
        }

        /** The unit lost in the scripted loss, from 0; -1 for none. */
        public int scriptedUnit() {
            return air.flatMap(Air::scriptedLoss).map(ScriptedLoss::unit).orElse(-1);
        }

        /**
         * The units the player can save: every unit but the scripted loss's (M5 part D). They pay at
         * the level end, they count for the level-end cues, and the objective fails when all are lost.
         */
        public int saveable() {
            return units() - (scriptedUnit() >= 0 ? 1 : 0);
        }
    }

    /**
     * M9 part D, a hull escort (design/allies/mining-tug; design/campaign Level 31, The tug in this level; {@link
     * HullEscort}): one huge ally on the ground layer made of {@code sections}, holding one screen {@code station} with
     * its sway on the level clock while the level scrolls beneath it. It rises from {@code size}'s half below the bottom
     * edge into its station over {@code enterSeconds} from the level's start and climbs off the top edge from {@code
     * climb}'s time, untouchable both times; a section alive at the climb-out's start is home. Only the hooked attacks
     * of the kinds {@code targetedBy} names hurt it (Q-D2 = a; mode {@code always} only), each on one section: a hooked
     * homing missile bursting in a section's box {@code missile}, a hooked diver or chaser crossing a section {@code
     * dive} once, an air link of a hooked kind lit across a section {@code link} per touch with a {@code
     * linkCooldownSeconds} cooldown per link and section. A lost section keeps its place (presentation only); all lost
     * fails the escort. The units of the {@code unhookedWaves} (indexes in the level's waves; Level 31's easy Hornet
     * waves, its Shard groups not sent across the hull) are not hooked whatever their kind; launched units always are
     * when their kind is.
     *
     * @param size the hull's footprint, px
     * @param station its centre's station, y up, with the sway
     * @param sections in hull order (the bow first), their offsets from the centre
     */
    public record Hull(
            Hitbox size,
            Station station,
            List<HullSection> sections,
            double enterSeconds,
            Climb climb,
            double missile,
            double dive,
            double link,
            double linkCooldownSeconds,
            java.util.Map<String, HookMode> targetedBy,
            List<Integer> unhookedWaves) {
        /** The most sections a hull has (a link's cooldowns are kept per section). */
        public static final int MOST_SECTIONS = 4;

        /** Every wave of a hooked kind hooked. */
        public Hull(
                Hitbox size,
                Station station,
                List<HullSection> sections,
                double enterSeconds,
                Climb climb,
                double missile,
                double dive,
                double link,
                double linkCooldownSeconds,
                java.util.Map<String, HookMode> targetedBy) {
            this(
                    size,
                    station,
                    sections,
                    enterSeconds,
                    climb,
                    missile,
                    dive,
                    link,
                    linkCooldownSeconds,
                    targetedBy,
                    List.of());
        }

        public Hull {
            sections = List.copyOf(sections);
            targetedBy = java.util.Map.copyOf(targetedBy);
            unhookedWaves = List.copyOf(unhookedWaves);
            for (int wave : unhookedWaves) {
                if (wave < 0) {
                    throw new IllegalArgumentException("an unhooked wave is a wave of the level: " + wave);
                }
            }
            java.util.Objects.requireNonNull(station);
            java.util.Objects.requireNonNull(climb);
            if (sections.isEmpty() || sections.size() > MOST_SECTIONS) {
                throw new IllegalArgumentException(
                        "a hull has 1 to " + MOST_SECTIONS + " sections: " + sections.size());
            }
            if (!(enterSeconds > 0)
                    || !(missile >= 0)
                    || !(dive >= 0)
                    || !(link >= 0)
                    || !(linkCooldownSeconds > 0)
                    || !(climb.t() > enterSeconds)) {
                throw new IllegalArgumentException(
                        "a hull enters, climbs out after it entered, and its hooked hits deal 0 or more");
            }
            for (HookMode mode : targetedBy.values()) {
                if (mode != HookMode.ALWAYS && mode != HookMode.NONE) {
                    throw new IllegalArgumentException("a hull's hook is always or none: " + mode);
                }
            }
            for (HullSection section : sections) {
                if (Math.abs(section.dx()) + section.box().width() / 2 > size.width() / 2 + 1e-9
                        || Math.abs(section.dy()) + section.box().height() / 2 > size.height() / 2 + 1e-9) {
                    throw new IllegalArgumentException("a hull's section lies inside its footprint: " + section.name());
                }
            }
        }

        /** The hook mode of the enemy {@code slug}: {@link HookMode#NONE} when not listed. */
        public HookMode mode(String slug) {
            return targetedBy.getOrDefault(slug, HookMode.NONE);
        }

        /** This hull with the waves {@code waves} (indexes in the level's list) unhooked whatever their kind. */
        public Hull withUnhookedWaves(List<Integer> waves) {
            return new Hull(
                    size,
                    station,
                    sections,
                    enterSeconds,
                    climb,
                    missile,
                    dive,
                    link,
                    linkCooldownSeconds,
                    targetedBy,
                    waves);
        }
    }

    /**
     * M9 part D: a hull's section (design/allies/mining-tug: bow, cargo, engines): its {@code name} (what {@code
     * {section}} becomes), its hit box's centre offset ({@code dx}, {@code dy}: px from the hull's centre, y up), its
     * hit {@code box}, its {@code hp} at the level's difficulty and the {@code credits} it pays when home (before the
     * credit factor).
     */
    public record HullSection(String name, double dx, double dy, Hitbox box, double hp, int credits) {
        public HullSection {
            java.util.Objects.requireNonNull(name);
            if (!(hp > 0) || credits < 0) {
                throw new IllegalArgumentException("a hull's section has HP and pays 0 or more: " + name);
            }
        }
    }

    /**
     * M5 part E (design/allies, convoy cargo ship and escort frigate; design/campaign Level 11; user
     * decision E8 = a and the stated defaults of 2026-10-08): a naval convoy outside the objectives.
     * Its units hold screen-space stations at the scroll speed; when the scroll halts in the boss's
     * arena each glides over {@code glideSeconds} (on the real steps) to its lane, centred at
     * {@code laneY}, or, a unit that {@code leaves}, off the bottom edge; when the halt ends (the boss
     * died) they hold there, or at a unit's hold point, until the sea has scrolled {@code holdClear}
     * px (Level 11: Platform Tiamat's deck has passed the bottom edge), then glide back to their
     * stations. Only what its {@link AllySpec} lists hurts a unit (the boss's slams); it never fails
     * the mission.
     *
     * @param laneY px from the bottom edge (y up): a unit's centre in its lane
     * @param holdClear px of scroll after the halt before the units glide back; 0 for at once
     * @param lanes the arena's lanes (the boss's); 0 without lanes
     * @param laneWidth px; lane 1 starts at the left edge
     */
    public record Naval(
            List<NavalUnit> units, double glideSeconds, double laneY, int lanes, double laneWidth, double holdClear) {
        public Naval {
            units = List.copyOf(units);
            if (units.isEmpty() || !(glideSeconds > 0) || lanes < 0 || (lanes > 0 && !(laneWidth > 0))) {
                throw new IllegalArgumentException("a naval convoy has units, a glide and lanes of a width");
            }
            if (!(holdClear >= 0)) {
                throw new IllegalArgumentException("a naval convoy holds clear for 0 px or more");
            }
            if (lanes * laneWidth > PlayField.WIDTH + 1e-9) {
                throw new IllegalArgumentException("the lanes lie on the play field");
            }
            for (NavalUnit unit : units) {
                if (unit.lane() > lanes) {
                    throw new IllegalArgumentException(unit.name() + ": no lane " + unit.lane());
                }
            }
        }

        /** Without a hold after the halt: the units glide back to their stations when it ends. */
        public Naval(List<NavalUnit> units, double glideSeconds, double laneY, int lanes, double laneWidth) {
            this(units, glideSeconds, laneY, lanes, laneWidth, 0);
        }

        /** The centre x of lane {@code lane} (1-based), px from the left edge. */
        public double laneX(int lane) {
            return (lane - 0.5) * laneWidth;
        }

        /** The units that can be damaged (the frigate cannot): the ones an afloat objective counts. */
        public int damageable() {
            int count = 0;
            for (NavalUnit unit : units) {
                count += unit.ally().damageable() ? 1 : 0;
            }
            return count;
        }
    }

    /**
     * M5 part E: a naval convoy's unit: an {@code ally} named {@code name} (what {@code {ally}} becomes
     * in a radio line: "Halvorsen") at its station ({@code x} px from the left edge, {@code y} px from
     * the bottom edge, y up), gliding to its arena {@code lane} (1-based) at the halt, or, with {@code
     * lane} 0, off the bottom edge ({@code leaves: true}) until the boss is down. When the halt ends it
     * holds where it is, or glides to its hold point ({@code holdX}, {@code holdY}, y up; NaN for none),
     * until the sea has scrolled its convoy's {@link Naval#holdClear()}.
     */
    public record NavalUnit(AllySpec ally, String name, double x, double y, int lane, double holdX, double holdY) {
        public NavalUnit {
            if (name.isBlank() || lane < 0) {
                throw new IllegalArgumentException("a naval convoy unit has a name and a lane from 1, or 0 to leave");
            }
            if (Double.isNaN(holdX) != Double.isNaN(holdY)) {
                throw new IllegalArgumentException(name + ": a hold point has an x and a y");
            }
        }

        /** Without a hold point: it holds where it is after the halt. */
        public NavalUnit(AllySpec ally, String name, double x, double y, int lane) {
            this(ally, name, x, y, lane, Double.NaN, Double.NaN);
        }

        /** Whether it glides to a hold point when the halt ends. */
        public boolean holds() {
            return !Double.isNaN(holdX);
        }

        /** Whether it drops back off the bottom edge at the halt instead of taking a lane. */
        public boolean leaves() {
            return lane == 0;
        }
    }

    /**
     * M5 part D (design/allies, evacuation shuttle; design/campaign Level 10; user decisions D1, D4
     * and D5 = a): an air escort. Each unit holds its {@link Station} in the band, drifting on its
     * lane sway on the level clock and never reacting to threats; there is no road and no hook. With a
     * {@code liftoff} the units stand on their pads (scrolling with the ground) until it and then
     * climb to their stations; with a {@code climb} they climb out off the top edge at its end. A
     * unit is untouchable (enemy fire and contact pass through it) during the liftoff and the
     * climb-out and, the scripted loss's unit, until its loss.
     */
    public record Air(
            List<Station> stations,
            Optional<Liftoff> liftoff,
            Optional<Climb> climb,
            Optional<ScriptedLoss> scriptedLoss) {
        public Air {
            stations = List.copyOf(stations);
            if (stations.isEmpty()) {
                throw new IllegalArgumentException("an air escort has at least one station");
            }
            if (liftoff.isPresent() && liftoff.get().pads().size() != stations.size()) {
                throw new IllegalArgumentException("a liftoff has one pad per unit");
            }
            if (scriptedLoss.isPresent()
                    && (scriptedLoss.get().unit() < 0 || scriptedLoss.get().unit() >= stations.size())) {
                throw new IllegalArgumentException("a scripted loss takes one of the units");
            }
        }
    }

    /**
     * An air escort unit's station: it flies at ({@code x} + {@code swayX} sin θ, {@code y} −
     * {@code swayY} sin 2θ), θ = 2π (t ÷ {@code period} + {@code phase}), t the level clock: a lazy
     * figure-eight round the station (the data's sway is down the screen, so it is subtracted here),
     * the sines from {@link Trig} (deterministic and allocation-free).
     *
     * @param x px from the play field's left edge
     * @param y px from the bottom edge (y up)
     * @param swayX px either side
     * @param swayY px up and down (positive: down the screen at θ = 45°, as in the data)
     * @param period s of one figure-eight
     * @param phase 0–1, its start in the figure-eight
     */
    public record Station(double x, double y, double swayX, double swayY, double period, double phase) {
        public Station {
            if (!(period > 0)) {
                throw new IllegalArgumentException("a station's sway has a period");
            }
        }

        /** The angle θ at {@code seconds} on the level clock. */
        private double theta(double seconds) {
            return 2 * StrictMath.PI * (seconds / period + phase);
        }

        /** Its x at {@code seconds} on the level clock. */
        public double xAt(double seconds) {
            return x + swayX * Trig.sin(theta(seconds));
        }

        /** Its y (up) at {@code seconds} on the level clock. */
        public double yAt(double seconds) {
            return y - swayY * Trig.sin(2 * theta(seconds));
        }
    }

    /**
     * The air escort's liftoff: until {@code t} each unit stands on its pad, scrolling with the
     * ground, so that it is at its pad's screen point at {@code t}; from there its screen position
     * eases (smoothstep) to its station over {@code seconds}, rising from the ground layer's scale
     * to the air scale.
     *
     * @param pads one screen point per unit at {@code t}
     */
    public record Liftoff(double t, double seconds, List<Pad> pads) {
        public Liftoff {
            pads = List.copyOf(pads);
            if (t < 0 || !(seconds > 0)) {
                throw new IllegalArgumentException("a liftoff starts in the level and takes a while");
            }
        }
    }

    /** A pad's screen point: px from the left edge, px from the bottom edge (y up). */
    public record Pad(double x, double y) {}

    /** The climb-out: from {@code t} the units alive climb off the top edge over {@code seconds}; each is home. */
    public record Climb(double t, double seconds) {
        public Climb {
            if (t < 0 || !(seconds > 0)) {
                throw new IllegalArgumentException("a climb-out starts in the level and takes a while");
            }
        }
    }

    /**
     * The scripted loss (Level 10's Lifeline Three, user decision D4 = a): the unit {@code unit}
     * (from 0) is untouchable until {@code t}; at {@code t} − {@code glow} the glow starts over it
     * ({@link SimEvents.Type#LOSS_GLOW}), at {@code t} the lance takes it ({@link
     * SimEvents.Type#SCRIPTED_LOSS}): it cannot be prevented, costs no pay, does not count toward the
     * fail and raises no loss cue.
     */
    public record ScriptedLoss(int unit, double t, double glow) {
        public ScriptedLoss {
            if (glow < 0 || glow > t) {
                throw new IllegalArgumentException("a scripted loss's glow starts in the level");
            }
        }
    }

    /**
     * M5 part G, the {@code defend} primary (design/campaign Level 13, Objective; design/allies, Nansen
     * Relay; user decisions G1, G2 and G6 = a): one {@code ally} on the ground at ({@code x}, {@code y}),
     * y px from the bottom edge (y up), where it stands when the level clock reaches {@code start}: it
     * scrolls with the ground before and after (Level 13: it enters at the top edge in the approach and
     * stops there because the ground halts). It must survive until {@code start} + {@code duration} (the
     * synchronisation): its loss fails the mission (not with the debug option {@code --invulnerable});
     * then it is defended, nothing aims at it any more and nothing hurts it. It pays no credits and has
     * no regeneration.
     *
     * @param targetedBy per enemy slug its hook mode (the level's {@code targeted_by} at the difficulty);
     *     enemies not listed are not hooked
     */
    public record Defend(
            AllySpec ally,
            double x,
            double y,
            double start,
            double duration,
            java.util.Map<String, HookMode> targetedBy) {
        public Defend {
            targetedBy = java.util.Map.copyOf(targetedBy);
            if (!(start >= 0) || !(duration > 0)) {
                throw new IllegalArgumentException("a defend primary starts in the level and lasts a while");
            }
            if (!(x >= 0 && x <= PlayField.WIDTH) || !(y >= 0 && y <= PlayField.HEIGHT)) {
                throw new IllegalArgumentException("a defended ally stands on the play field: (" + x + ", " + y + ")");
            }
        }

        /** When the synchronisation completes, s on the level clock. */
        public double end() {
            return start + duration;
        }

        /** The hook mode of the enemy {@code slug}: {@link HookMode#NONE} when not listed. */
        public HookMode mode(String slug) {
            return targetedBy.getOrDefault(slug, HookMode.NONE);
        }
    }
}
