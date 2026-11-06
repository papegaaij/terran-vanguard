package vanguard.sim;

import java.util.List;
import java.util.Optional;
import vanguard.sim.GroundSpec.Gate;
import vanguard.sim.ObjectiveSpec.Defend;
import vanguard.sim.ObjectiveSpec.Escort;
import vanguard.sim.ObjectiveSpec.GroundAllies;
import vanguard.sim.ObjectiveSpec.Hold;
import vanguard.sim.ObjectiveSpec.Naval;

/**
 * A level's script at one difficulty (design/campaign/&lt;act&gt;/&lt;level&gt;/data.yaml), built
 * by {@code vanguard.content.SimSpecs}: the scroll, the waves, the ground objects, the radio
 * cues and the objectives. The level ends when the scroll reaches the end of the last section
 * (the {@code reach-end} primary objective). Refactoring step LS-3 split its concerns into four
 * sub-records ({@link ObjectiveSpec}, {@link GroundSpec}, {@link HazardSpec}, {@link DropSpec}),
 * each validating its own parts; the level keeps the checks across them.
 *
 * @param number the global level number (01–50)
 * @param act the act the level belongs to
 * @param launchSeconds the non-playable launch at the start
 * @param launchDrop M7 part B (default pending the user's review): the launch drops the ship (and the
 *     wingman in his slot) from above the top edge to the start position instead of raising it from
 *     below (Level 15's drop from the transport's bay)
 * @param sections back to back from t = 0
 * @param waves in time order
 * @param groundObjects every placed ground object, in time order
 * @param groundUnits every enemy fixed to the ground, in time order
 * @param setPieces the huge set-piece units flying their passes (Level 03's Leviathan)
 * @param secrets the number of secrets in the level
 * @param radio the radio chatter cues
 * @param objectives the secondary objective, the primary's groups, escort, naval convoy or defend, the
 *     ground allies, tows, holds and collapse
 * @param ground the road, water, terrain, gates, no-surface zones and conveyor belts
 * @param hazards the cranes, debris, sleds, rocks, vents, darkness, weather and sondes
 * @param drops the group and part drops, the placed pickups and the drop factor
 * @param medium M8 part A (design/world/europa, Under water rules): the level's medium and its switches at the
 *     sections' starts; {@link MediumSpec#AIR} (the default) for every level before Act 4
 * @param specialLimits M8 part A (design/player/specials, Setting restrictions): the level's and its sections' lists
 *     of forbidden specials; {@link SpecialLimits#NONE} (the default): the specials' own settings decide
 * @param rookLoss M8 part F (design/player/wingmen, Rook's loss; Level 26): the scripted loss of Rook; empty (the
 *     default) for none
 * @param armourFloor M8 part H, step H2 (design/campaign Level 28, The run cannot be lost; J2): from the level's boss's
 *     kill to the level's end the ship's armour stops at this many points (the run cannot be lost; {@link
 *     Defences#floor}); 0 (the default) for none
 * @param haltCycle M9 part B (design/campaign Level 29): the waves of the held arena's halt on the halt's own clock
 *     ({@link HaltCycle}); empty (the default) for none
 * @param escortSwap M9 part B (design/player/wingmen, Rook's rescue and return): the mid-level escort swap at the held
 *     arena's release ({@link EscortSwapSpec}); empty (the default) for none
 * @param turn M10 part A, step A1b (design/enemies, The three-way war; Level 37): the turn's time on the level clock,
 *     s: the hunting waves' links ({@link WaveSpec#hunts()}) are live from it (before it the Vrell and the Ascendancy
 *     fly side by side); empty (the default): live from the hunters' entry
 * @param gateCycles M10 part G, step G1a (design/campaign Level 41, the halts): each held gate's own halt cycle, in the
 *     gates' order (one entry per arena section; an empty one for a gate without); empty (the default) for none, or
 *     for the level's one {@code haltCycle} (Level 29's), which it excludes
 * @param uplinks M10 part G, step G1a (Level 41, the secondary {@code FILES}): the archive cores under their hatches
 *     ({@link Uplinks}); empty (the default) for none
 * @param vrell M11 part A, step A1b: the level's Act 7 parts (the spore clouds, the quiet, the boss rush); {@link
 *     VrellLevel#NONE} (the default) for none
 */
public record LevelScript(
        int number,
        int act,
        double launchSeconds,
        boolean launchDrop,
        List<Section> sections,
        List<WaveSpec> waves,
        List<GroundObjectSpec> groundObjects,
        List<GroundUnit> groundUnits,
        List<SetPieceSpec> setPieces,
        int secrets,
        List<RadioCue> radio,
        ObjectiveSpec objectives,
        GroundSpec ground,
        HazardSpec hazards,
        DropSpec drops,
        MediumSpec medium,
        SpecialLimits specialLimits,
        Optional<RookLossSpec> rookLoss,
        double armourFloor,
        Optional<HaltCycle> haltCycle,
        Optional<EscortSwapSpec> escortSwap,
        Optional<Double> turn,
        List<Optional<HaltCycle>> gateCycles,
        List<UplinkSpec> uplinks,
        VrellLevel vrell) {
    public LevelScript {
        java.util.Objects.requireNonNull(vrell);
        if (!turn.map(at -> at >= 0).orElse(true)) {
            throw new IllegalArgumentException("the turn is a time on the level clock: " + turn.get());
        }
        gateCycles = List.copyOf(gateCycles);
        uplinks = List.copyOf(uplinks);
        checkHeldArena(
                sections,
                groundUnits,
                setPieces,
                haltCycle,
                gateCycles,
                escortSwap,
                rookLoss,
                vrell.rush().isPresent());
        checkHeldPieces(
                sections,
                groundUnits,
                setPieces,
                objectives.holds(),
                vrell.rush().isPresent());
        checkUplinks(uplinks, groundObjects, objectives.secondary());
        checkForks(ground, waves, groundUnits, groundObjects);
        checkGroundAllies(
                sections,
                objectives.holds(),
                objectives.escort(),
                objectives.convoy(),
                objectives.defend(),
                objectives.groundAllies());
        if (!ground.noSurface().isEmpty()
                && (!objectives.holds().isEmpty() || sections.stream().anyMatch(Section::arena))) {
            throw new IllegalArgumentException(
                    "no-surface zones are placed by the scroll's time, which holds and arenas make depend on play");
        }
        checkDefend(
                sections,
                objectives.holds(),
                radio,
                objectives.secondary(),
                objectives.escort(),
                objectives.convoy(),
                objectives.defend());
        checkReversed(sections, waves, setPieces, objectives, ground, hazards);
        if (vrell.pits().isPresent() && sections.stream().anyMatch(Section::reversed)) {
            // M11 part C, step CD1: a pit's wave rises on the ground's forward scroll only.
            throw new IllegalArgumentException("a level with spawning pits never reverses its scroll");
        }
        if (objectives.secondary().sondes() && hazards.sondes().isEmpty()) {
            throw new IllegalArgumentException("a collect: sondes objective needs the level's sondes");
        }
        sections = List.copyOf(sections);
        waves = List.copyOf(waves);
        groundObjects = List.copyOf(groundObjects);
        groundUnits = List.copyOf(groundUnits);
        radio = List.copyOf(radio);
        setPieces = List.copyOf(setPieces);
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("a level needs at least one section");
        }
        if (!medium.switches().isEmpty()
                && !(medium.switches().getLast().atSeconds()
                        < sections.getLast().end())) {
            throw new IllegalArgumentException("the medium switches before the level's end: " + medium.switches());
        }
        if (!specialLimits.switches().isEmpty()
                && !(specialLimits.switches().getLast().atSeconds()
                        < sections.getLast().end())) {
            throw new IllegalArgumentException(
                    "the special limits switch before the level's end: " + specialLimits.switches());
        }
        if (objectives.escort().isPresent()
                && objectives.escort().get().air().isEmpty()
                && objectives.escort().get().hull().isEmpty()
                && ground.road().isEmpty()) {
            throw new IllegalArgumentException("a convoy follows the level's road");
        }
        java.util.Objects.requireNonNull(rookLoss);
        if (rookLoss.isPresent()
                && !(rookLoss.get().trails() >= launchSeconds
                        && rookLoss.get().t() < sections.getLast().end())) {
            throw new IllegalArgumentException(
                    "Rook's loss falls after the launch and before the level's end: " + rookLoss.get());
        }
        if (!(armourFloor >= 0) || (armourFloor > 0 && setPieces.stream().noneMatch(SetPieceSpec::isBoss))) {
            throw new IllegalArgumentException(
                    "an armour floor is 0 or more points and holds from a boss's kill: " + armourFloor);
        }
        if (hazards.cruisers().isPresent()
                && !(hazards.cruisers().get().endAt() < sections.getLast().end())) {
            throw new IllegalArgumentException("the cruisers' run ends before the level's end: " + hazards.cruisers());
        }
    }

    /** A level without M9 part B's halt cycle and escort swap. */
    public LevelScript(
            int number,
            int act,
            double launchSeconds,
            boolean launchDrop,
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundObjectSpec> groundObjects,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            int secrets,
            List<RadioCue> radio,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards,
            DropSpec drops,
            MediumSpec medium,
            SpecialLimits specialLimits,
            Optional<RookLossSpec> rookLoss,
            double armourFloor) {
        this(
                number,
                act,
                launchSeconds,
                launchDrop,
                sections,
                waves,
                groundObjects,
                groundUnits,
                setPieces,
                secrets,
                radio,
                objectives,
                ground,
                hazards,
                drops,
                medium,
                specialLimits,
                rookLoss,
                armourFloor,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                VrellLevel.NONE);
    }

    /**
     * M9 part B (design/campaign Level 29, the rescue halt): a level whose ground units include a {@link
     * GroundUnit#holder() holder} has a held arena: exactly one arena section, of speed 0, not crawling and without a
     * hold after a boss, and no anchored, breaching or coiled boss (its holders hold the clock, not a boss). A halt
     * cycle and an escort swap need one; the swap is not combined with Rook's scripted loss.
     *
     * <p>M10 part G, step G1a (design/campaign Level 41, the Halo Platform's halt): a gate without a ground holder is
     * held by exactly one boss set piece arriving in it ({@link #gatePiece}): the clock stands while it lives. Every
     * boss of a held-gates level holds a gate that way (none anchored, breaching or coiled), one per gate. The gates'
     * own halt cycles ({@code gateCycles}) number the arena sections and exclude the level's one cycle. M10 part G, step
     * G1b: a {@link BossSpec#held() held} set piece may lie anchored with its arena (no lanes) in its gate.
     */
    private static void checkHeldArena(
            List<Section> sections,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            Optional<HaltCycle> haltCycle,
            List<Optional<HaltCycle>> gateCycles,
            Optional<EscortSwapSpec> escortSwap,
            Optional<RookLossSpec> rookLoss,
            boolean rush) {
        java.util.Objects.requireNonNull(haltCycle);
        java.util.Objects.requireNonNull(escortSwap);
        // M11 part A, step A1b (design/enemies/bosses, Boss rush): a rush's bosses hold its gates.
        boolean held = rush || groundUnits.stream().anyMatch(GroundUnit::holder);
        if (!held) {
            if (haltCycle.isPresent() || escortSwap.isPresent() || !gateCycles.isEmpty()) {
                throw new IllegalArgumentException("a halt cycle or an escort swap needs a held arena's holders");
            }
            return;
        }
        List<Section> arenas = sections.stream().filter(Section::arena).toList();
        // M10 part A, step A1b (design/enemies/ground/shield-pylon, The pylon gates): several held arenas, the gates.
        boolean gatesApart = true;
        for (int i = 1; i < sections.size(); i++) {
            gatesApart &= !(sections.get(i).arena() && sections.get(i - 1).arena());
        }
        if (arenas.isEmpty()
                || arenas.stream().anyMatch(arena -> arena.speed() != 0 || arena.holdAfterBossSeconds() > 0)
                || sections.getFirst().arena()
                || !gatesApart) {
            throw new IllegalArgumentException(
                    "a held arena is an arena section of speed 0 after a moving one, without a" + " hold after a boss");
        }
        double arenaEnd = arenas.getLast().end();
        if (groundUnits.stream().anyMatch(unit -> unit.holder() && !(unit.t() < arenaEnd))) {
            throw new IllegalArgumentException("a held arena's holders enter before its end (" + arenaEnd + " s)");
        }
        for (SetPieceSpec piece : setPieces) {
            if (!piece.isBoss()) {
                continue;
            }
            BossSpec boss = piece.boss().get();
            int gate = gateOf(sections, boss.arriveSeconds());
            if (gate < 0
                    || (boss.arena().isPresent()
                            && (!boss.held() || boss.arena().get().lanes().count() > 0))
                    || boss.breach().isPresent()
                    || boss.coil().isPresent()
                    || groundUnits.stream().anyMatch(unit -> unit.holder() && gateOf(sections, unit.t()) == gate)
                    || setPieces.stream()
                                    .filter(other -> other.isBoss()
                                            && gateOf(
                                                            sections,
                                                            other.boss().get().arriveSeconds())
                                                    == gate)
                                    .count()
                            > 1) {
                throw new IllegalArgumentException("a held arena's holders hold its clock: a boss in the level holds a"
                        + " gate of its own, arriving in it (not anchored, breaching or coiled): " + piece.slug());
            }
        }
        for (int g = 0; g < arenas.size(); g++) {
            int gate = g;
            if (groundUnits.stream().noneMatch(unit -> unit.holder() && gateOf(sections, unit.t()) == gate)
                    && gatePiece(sections, setPieces, gate) < 0) {
                throw new IllegalArgumentException("every held gate has a holder: gate " + g + " ending at "
                        + arenas.get(g).end() + " s has none");
            }
        }
        if (arenas.size() > 1 && escortSwap.isPresent()) {
            throw new IllegalArgumentException(
                    "an escort swap releases at the one held arena, not at a gate of several");
        }
        if (!gateCycles.isEmpty() && (gateCycles.size() != arenas.size() || haltCycle.isPresent())) {
            throw new IllegalArgumentException("the gates' halt cycles are one per held gate (" + arenas.size()
                    + "), instead of the level's one: " + gateCycles.size());
        }
        if (escortSwap.isPresent() && rookLoss.isPresent()) {
            throw new IllegalArgumentException("an escort swap and Rook's scripted loss are not in one level");
        }
    }

    /**
     * M10 part G, step G1b (design/enemies/ground/halo-platform; Level 41): a {@link BossSpec#held() held} set piece
     * holds a gate of a held-gates level ({@link #gatePiece}): no boss fight elsewhere (no bar, no warning). One anchored
     * on the ground is anchored at its gate's halt from the scroll the level will run, so its level has no hold zones
     * (their eases depend on play).
     */
    private static void checkHeldPieces(
            List<Section> sections,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            List<Hold> holds,
            boolean rush) {
        boolean gates = rush || groundUnits.stream().anyMatch(GroundUnit::holder);
        boolean anchored = false;
        for (int k = 0; k < setPieces.size(); k++) {
            SetPieceSpec piece = setPieces.get(k);
            if (piece.boss().isEmpty() || !piece.boss().get().held()) {
                continue;
            }
            int gate = gateOf(sections, piece.boss().get().arriveSeconds());
            if (!gates || gate < 0 || gatePiece(sections, setPieces, gate) != k) {
                throw new IllegalArgumentException(
                        "a held set piece holds a gate of a held-gates level, arriving in it: " + piece.slug());
            }
            anchored |= piece.boss().get().arena().isPresent();
        }
        if (anchored && !holds.isEmpty()) {
            throw new IllegalArgumentException(
                    "a held set piece anchored in its gate needs a level without hold zones");
        }
    }

    /**
     * M10 part A, step A1b (The pylon gates): the held gate a holder entering at {@code t} s holds: the index (0 first)
     * of the first arena section among {@code sections} that ends after {@code t}; -1 for none.
     */
    static int gateOf(List<Section> sections, double t) {
        int gate = 0;
        for (Section section : sections) {
            if (!section.arena()) {
                continue;
            }
            if (t < section.end()) {
                return gate;
            }
            gate++;
        }
        return -1;
    }

    /**
     * M10 part G, step G1a (design/campaign Level 41, the Halo Platform's halt): the index among the level's set pieces
     * of the boss that holds held gate {@code gate} (arriving in it; the gate has no ground holder); -1 for a gate its
     * ground holders hold (or none).
     */
    public int gatePiece(int gate) {
        return gatePiece(sections, setPieces, gate);
    }

    private static int gatePiece(List<Section> sections, List<SetPieceSpec> setPieces, int gate) {
        for (int k = 0; k < setPieces.size(); k++) {
            SetPieceSpec piece = setPieces.get(k);
            if (piece.isBoss() && gateOf(sections, piece.boss().get().arriveSeconds()) == gate) {
                return k;
            }
        }
        return -1;
    }

    /**
     * M10 part G, step G1a (Level 41, the secondary {@code FILES}): each uplink's hatch is a plain destructible among the
     * ground objects (not hardened, no trigger, group, charge, branch, sub or low-air object), one hatch per core; a
     * {@code files} secondary counts no more files than the level has cores.
     */
    private static void checkUplinks(List<UplinkSpec> uplinks, List<GroundObjectSpec> objects, Secondary secondary) {
        java.util.Set<Integer> hatches = new java.util.HashSet<>();
        for (UplinkSpec uplink : uplinks) {
            if (uplink.hatch() >= objects.size() || !hatches.add(uplink.hatch())) {
                throw new IllegalArgumentException("an uplink's hatch is a ground object of its own: " + uplink);
            }
            GroundObjectSpec hatch = objects.get(uplink.hatch());
            if (!(hatch.hp() > 0)
                    || hatch.trigger()
                    || hatch.hardened()
                    || hatch.group() >= 0
                    || hatch.blast().isPresent()
                    || hatch.branch().isPresent()
                    || hatch.submerged()
                    || hatch.lowAir()) {
                throw new IllegalArgumentException(
                        "an uplink's hatch is a plain destructible on the ground: " + uplink);
            }
        }
        if (secondary.files() > uplinks.size()) {
            throw new IllegalArgumentException(
                    "a files secondary counts the level's cores: " + secondary.files() + " of " + uplinks.size());
        }
    }

    /**
     * M9 part B: whether the level has a held arena (a holder among its ground units). M11 part A, step A1b: a boss
     * rush's level too (its bosses hold its gates).
     */
    public boolean heldArena() {
        return vrell.rush().isPresent() || groundUnits.stream().anyMatch(GroundUnit::holder);
    }

    /** A level without M8 part H's armour floor. */
    public LevelScript(
            int number,
            int act,
            double launchSeconds,
            boolean launchDrop,
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundObjectSpec> groundObjects,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            int secrets,
            List<RadioCue> radio,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards,
            DropSpec drops,
            MediumSpec medium,
            SpecialLimits specialLimits,
            Optional<RookLossSpec> rookLoss) {
        this(
                number,
                act,
                launchSeconds,
                launchDrop,
                sections,
                waves,
                groundObjects,
                groundUnits,
                setPieces,
                secrets,
                radio,
                objectives,
                ground,
                hazards,
                drops,
                medium,
                specialLimits,
                rookLoss,
                0);
    }

    /** A level without M8 part F's scripted loss of Rook. */
    public LevelScript(
            int number,
            int act,
            double launchSeconds,
            boolean launchDrop,
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundObjectSpec> groundObjects,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            int secrets,
            List<RadioCue> radio,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards,
            DropSpec drops,
            MediumSpec medium,
            SpecialLimits specialLimits) {
        this(
                number,
                act,
                launchSeconds,
                launchDrop,
                sections,
                waves,
                groundObjects,
                groundUnits,
                setPieces,
                secrets,
                radio,
                objectives,
                ground,
                hazards,
                drops,
                medium,
                specialLimits,
                Optional.empty());
    }

    /** A level in the air (every level before Act 4): {@link MediumSpec#AIR}. */
    public LevelScript(
            int number,
            int act,
            double launchSeconds,
            boolean launchDrop,
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundObjectSpec> groundObjects,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            int secrets,
            List<RadioCue> radio,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards,
            DropSpec drops) {
        this(
                number,
                act,
                launchSeconds,
                launchDrop,
                sections,
                waves,
                groundObjects,
                groundUnits,
                setPieces,
                secrets,
                radio,
                objectives,
                ground,
                hazards,
                drops,
                MediumSpec.AIR,
                SpecialLimits.NONE);
    }

    /** M8 part A: a level whose specials follow their own settings ({@link SpecialLimits#NONE}). */
    public LevelScript(
            int number,
            int act,
            double launchSeconds,
            boolean launchDrop,
            List<Section> sections,
            List<WaveSpec> waves,
            List<GroundObjectSpec> groundObjects,
            List<GroundUnit> groundUnits,
            List<SetPieceSpec> setPieces,
            int secrets,
            List<RadioCue> radio,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards,
            DropSpec drops,
            MediumSpec medium) {
        this(
                number,
                act,
                launchSeconds,
                launchDrop,
                sections,
                waves,
                groundObjects,
                groundUnits,
                setPieces,
                secrets,
                radio,
                objectives,
                ground,
                hazards,
                drops,
                medium,
                SpecialLimits.NONE);
    }

    /**
     * A builder for a level {@code number} of act {@code act}: every other component starts empty (no
     * sections, waves, …, {@link Secondary#NONE}, no launch, no secrets, a drop factor of 1, no water, no
     * drop launch); {@link Builder#build()} validates the whole level once, as the canonical constructor.
     */
    public static Builder builder(int number, int act) {
        return new Builder(number, act);
    }

    /** A builder holding this level's components, to change some and {@link Builder#build()} a new level. */
    public Builder toBuilder() {
        return new Builder(number, act)
                .launchSeconds(launchSeconds)
                .launchDrop(launchDrop)
                .sections(sections)
                .waves(waves)
                .groundObjects(groundObjects)
                .groundUnits(groundUnits)
                .setPieces(setPieces)
                .secrets(secrets)
                .radio(radio)
                .objectives(objectives)
                .ground(ground)
                .hazards(hazards)
                .drops(drops)
                .medium(medium)
                .specialLimits(specialLimits)
                .rookLoss(rookLoss)
                .armourFloor(armourFloor)
                .haltCycle(haltCycle)
                .escortSwap(escortSwap)
                .turn(turn)
                .gateCycles(gateCycles)
                .uplinks(uplinks)
                .vrell(vrell);
    }

    /**
     * Builds a {@link LevelScript} one component at a time, with one setter per component named after it (the
     * sub-records {@link ObjectiveSpec}, {@link GroundSpec}, {@link HazardSpec} and {@link DropSpec} whole);
     * {@link #build()} runs the canonical constructor, so its copies and checks run once, on the whole level.
     */
    public static final class Builder {
        private int number;
        private int act;
        private double launchSeconds = 0;
        private boolean launchDrop = false;
        private List<Section> sections = List.of();
        private List<WaveSpec> waves = List.of();
        private List<GroundObjectSpec> groundObjects = List.of();
        private List<GroundUnit> groundUnits = List.of();
        private List<SetPieceSpec> setPieces = List.of();
        private int secrets = 0;
        private List<RadioCue> radio = List.of();
        private ObjectiveSpec objectives = ObjectiveSpec.NONE;
        private GroundSpec ground = GroundSpec.NONE;
        private HazardSpec hazards = HazardSpec.NONE;
        private DropSpec drops = DropSpec.NONE;
        private MediumSpec medium = MediumSpec.AIR;
        private SpecialLimits specialLimits = SpecialLimits.NONE;
        private Optional<RookLossSpec> rookLoss = Optional.empty();
        private double armourFloor = 0;
        private Optional<HaltCycle> haltCycle = Optional.empty();
        private Optional<EscortSwapSpec> escortSwap = Optional.empty();
        private Optional<Double> turn = Optional.empty();
        private List<Optional<HaltCycle>> gateCycles = List.of();
        private List<UplinkSpec> uplinks = List.of();
        private VrellLevel vrell = VrellLevel.NONE;

        private Builder(int number, int act) {
            this.number = number;
            this.act = act;
        }

        public Builder number(int number) {
            this.number = number;
            return this;
        }

        public Builder act(int act) {
            this.act = act;
            return this;
        }

        public Builder launchSeconds(double launchSeconds) {
            this.launchSeconds = launchSeconds;
            return this;
        }

        public Builder launchDrop(boolean launchDrop) {
            this.launchDrop = launchDrop;
            return this;
        }

        public Builder sections(List<Section> sections) {
            this.sections = sections;
            return this;
        }

        public Builder waves(List<WaveSpec> waves) {
            this.waves = waves;
            return this;
        }

        public Builder groundObjects(List<GroundObjectSpec> groundObjects) {
            this.groundObjects = groundObjects;
            return this;
        }

        public Builder groundUnits(List<GroundUnit> groundUnits) {
            this.groundUnits = groundUnits;
            return this;
        }

        public Builder setPieces(List<SetPieceSpec> setPieces) {
            this.setPieces = setPieces;
            return this;
        }

        public Builder secrets(int secrets) {
            this.secrets = secrets;
            return this;
        }

        public Builder radio(List<RadioCue> radio) {
            this.radio = radio;
            return this;
        }

        public Builder objectives(ObjectiveSpec objectives) {
            this.objectives = objectives;
            return this;
        }

        public Builder ground(GroundSpec ground) {
            this.ground = ground;
            return this;
        }

        public Builder hazards(HazardSpec hazards) {
            this.hazards = hazards;
            return this;
        }

        public Builder drops(DropSpec drops) {
            this.drops = drops;
            return this;
        }

        /** M8 part A: the level's medium (design/world/europa, Under water rules); {@link MediumSpec#AIR} by default. */
        public Builder medium(MediumSpec medium) {
            this.medium = medium;
            return this;
        }

        /**
         * M8 part A: the level's and its sections' lists of forbidden specials (design/player/specials, Setting
         * restrictions); {@link SpecialLimits#NONE} by default.
         */
        public Builder specialLimits(SpecialLimits specialLimits) {
            this.specialLimits = specialLimits;
            return this;
        }

        /** M8 part F: the scripted loss of Rook (design/player/wingmen, Rook's loss); empty by default. */
        public Builder rookLoss(Optional<RookLossSpec> rookLoss) {
            this.rookLoss = rookLoss;
            return this;
        }

        /**
         * M8 part H: the ship's armour floor from the boss's kill to the level's end (design/campaign Level 28); 0 (none)
         * by default.
         */
        public Builder armourFloor(double armourFloor) {
            this.armourFloor = armourFloor;
            return this;
        }

        /** M9 part B: the held arena's halt cycle (design/campaign Level 29); empty by default. */
        public Builder haltCycle(Optional<HaltCycle> haltCycle) {
            this.haltCycle = haltCycle;
            return this;
        }

        /** M9 part B: the mid-level escort swap (design/player/wingmen, Rook's rescue); empty by default. */
        public Builder escortSwap(Optional<EscortSwapSpec> escortSwap) {
            this.escortSwap = escortSwap;
            return this;
        }

        public Builder turn(Optional<Double> turn) {
            this.turn = turn;
            return this;
        }

        /** M10 part G, step G1a: each held gate's own halt cycle (design/campaign Level 41); empty by default. */
        public Builder gateCycles(List<Optional<HaltCycle>> gateCycles) {
            this.gateCycles = gateCycles;
            return this;
        }

        /** M10 part G, step G1a: the archive cores under their hatches (Level 41's {@code FILES}); empty by default. */
        public Builder uplinks(List<UplinkSpec> uplinks) {
            this.uplinks = uplinks;
            return this;
        }

        /** M11 part A, step A1b: the level's Act 7 parts (spore clouds, the quiet, the rush); none by default. */
        public Builder vrell(VrellLevel vrell) {
            this.vrell = vrell;
            return this;
        }

        /** The level: the sub-records validated their own parts, the level checks them against each other. */
        public LevelScript build() {
            return new LevelScript(
                    number,
                    act,
                    launchSeconds,
                    launchDrop,
                    sections,
                    waves,
                    groundObjects,
                    groundUnits,
                    setPieces,
                    secrets,
                    radio,
                    objectives,
                    ground,
                    hazards,
                    drops,
                    medium,
                    specialLimits,
                    rookLoss,
                    armourFloor,
                    haltCycle,
                    escortSwap,
                    turn,
                    gateCycles,
                    uplinks,
                    vrell);
        }
    }

    /**
     * M7 part C: this level with the tendril gates {@code pairs} (Level 16; design/enemies/ground/tendril-anchor):
     * each gate's two anchors join the ground units (in time order, after the units already there at the same
     * time; group -1), so they enter, scroll, fire, count and pay as any ground unit, and the sortie spans the
     * gate's tendril between them. The anchors are not to be among the ground units already. Only once.
     */
    public LevelScript withGates(List<Gate> pairs) {
        if (!ground.gates().isEmpty()) {
            throw new IllegalArgumentException("a level's gates are set once");
        }
        List<GroundUnit> units = new java.util.ArrayList<>(groundUnits);
        for (Gate gate : pairs) {
            units.add(new GroundUnit(gate.t(), gate.leftX(), gate.anchor(), -1));
            units.add(new GroundUnit(gate.t(), gate.rightX(), gate.anchor(), -1));
        }
        // A stable sort: the units already there keep their order, each gate's left anchor before its right.
        units.sort(java.util.Comparator.comparingDouble(GroundUnit::t));
        GroundSpec gated = new GroundSpec(
                ground.road(), ground.water(), ground.terrain(), pairs, ground.noSurface(), ground.belts());
        return toBuilder().groundUnits(units).ground(gated).build();
    }

    /**
     * M9 part E (design/campaign Level 32, The forks): every branch's wave, ground unit and ground object names one of
     * the terrain's forks.
     */
    private static void checkForks(
            GroundSpec ground, List<WaveSpec> waves, List<GroundUnit> groundUnits, List<GroundObjectSpec> objects) {
        int forks = ground.terrain().map(terrain -> terrain.forks().size()).orElse(0);
        java.util.stream.Stream<Optional<Branch>> branches = java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(
                        waves.stream().map(WaveSpec::branch),
                        groundUnits.stream().map(GroundUnit::branch)),
                objects.stream().map(GroundObjectSpec::branch));
        if (branches.anyMatch(branch -> branch.isPresent() && branch.get().fork() >= forks)) {
            throw new IllegalArgumentException("a branch belongs to one of the terrain's " + forks + " forks");
        }
    }

    /**
     * M7 part D: ground allies stand on the ground by the scroll's time, so they come without holds or an
     * arena, and beside no escort, naval convoy or defend primary (the sortie has one convoy).
     */
    private static void checkGroundAllies(
            List<Section> sections,
            List<Hold> holds,
            Optional<Escort> escort,
            Optional<Naval> convoy,
            Optional<Defend> defend,
            Optional<GroundAllies> groundAllies) {
        // An untouched secondary without ground allies is the sortie's to refuse: a level is built with its
        // secondary first and its ground allies after (ObjectiveSpec.withGroundAllies).
        if (groundAllies.isEmpty()) {
            return;
        }
        if (escort.isPresent() || convoy.isPresent() || defend.isPresent()) {
            throw new IllegalArgumentException(
                    "ground allies have no escort, naval convoy or defend primary beside them");
        }
        if (!holds.isEmpty() || sections.stream().anyMatch(Section::arena)) {
            throw new IllegalArgumentException(
                    "ground allies stand by the scroll's time, which holds and arenas make depend on play");
        }
    }

    /**
     * M5 part G (design/campaign Level 13, The halt): the rules of a timed halt and the {@code defend}
     * primary the simulation needs. A timed halt (a section of speed 0 without an arena) and a defend
     * primary never come with hold zones or a boss arena; a defend primary has no escort or naval convoy
     * beside it; an integrity secondary (M9 part D: or a hull escort) and the {@code ally-below} radio cues need a
     * defend primary. The
     * content's rules (a timed halt only with a defend primary; nothing entering at the top edge with the
     * scroll inside one, {@link #timedHaltAt(double)}) are the level validator's.
     */
    private static void checkDefend(
            List<Section> sections,
            List<Hold> holds,
            List<RadioCue> radio,
            Secondary secondary,
            Optional<Escort> escort,
            Optional<Naval> convoy,
            Optional<Defend> defend) {
        boolean arena = sections.stream().anyMatch(Section::arena);
        boolean halt = sections.stream().anyMatch(Section::timedHalt);
        if (halt && (!holds.isEmpty() || arena)) {
            throw new IllegalArgumentException("a timed halt comes without hold zones or a boss arena");
        }
        if (defend.isPresent() && (!holds.isEmpty() || arena)) {
            throw new IllegalArgumentException("a defend primary comes without hold zones or a boss arena");
        }
        if (defend.isPresent() && (escort.isPresent() || convoy.isPresent())) {
            throw new IllegalArgumentException("a defend primary has no escort or naval convoy beside it");
        }
        if (secondary.integrity() > 0
                && defend.isEmpty()
                && (escort.isEmpty() || escort.get().hull().isEmpty())) {
            // M9 part D: or a hull escort (Level 31's CLEAN HULL: every section home with at least the share).
            throw new IllegalArgumentException("an integrity secondary needs a defend primary or a hull escort");
        }
        for (RadioCue cue : radio) {
            if (cue.trigger() == CueTrigger.ALLY_BELOW) {
                if (defend.isEmpty()) {
                    throw new IllegalArgumentException("an ally-below radio cue needs a defend primary");
                }
                double share = CueTrigger.share(cue.subject());
                if (!(share > 0 && share < 1)) {
                    throw new IllegalArgumentException(
                            "an ally-below cue's subject is a share between 0 and 1: " + cue.subject());
                }
            }
        }
    }

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll; scope: what Level 20 needs, pending the user's review): a
     * level with a reversed section has nothing placed by the scroll's time or entering at the top edge with the ground
     * but its waves, ground units, ground objects and placed pickups: no terrain, road, water, gates, no-surface zones or
     * conveyor belts; no cranes, debris, sled, rocks, furnace vents, darkness or sondes; no holds, collapse, tows,
     * escort, naval convoy, defend primary or ground allies; no set pieces, arena or timed halt; and no segment chain
     * among its waves (a chain is never flipped).
     */
    private static void checkReversed(
            List<Section> sections,
            List<WaveSpec> waves,
            List<SetPieceSpec> setPieces,
            ObjectiveSpec objectives,
            GroundSpec ground,
            HazardSpec hazards) {
        if (sections.stream().noneMatch(Section::reversed)) {
            return;
        }
        // M11 part A (design/enemies, Distorted space): a level with a current may have chains (none entering in a
        // reversed span: the content's checks keep them out of the tells and runs) and debris (riding the vector).
        boolean current = hazards.current().isPresent();
        String what = null;
        if (ground.terrain().isPresent()
                || ground.road().isPresent()
                || ground.water()
                || !ground.gates().isEmpty()
                || !ground.noSurface().isEmpty()
                || !ground.belts().isEmpty()) {
            what = "terrain, road, water, gates, no-surface zones or belts";
        } else if (!hazards.cranes().isEmpty()
                || (!hazards.debris().isEmpty() && !current)
                || hazards.sled().isPresent()
                || hazards.rocks().isPresent()
                || !hazards.vents().isEmpty()
                || hazards.darkness().isPresent()
                || hazards.sondes().isPresent()) {
            what = "cranes, debris, a sled, rocks, vents, darkness or sondes";
        } else if (!objectives.holds().isEmpty()
                || objectives.collapse().isPresent()
                || !objectives.tows().isEmpty()
                || objectives.escort().isPresent()
                || objectives.convoy().isPresent()
                || objectives.defend().isPresent()
                || objectives.groundAllies().isPresent()) {
            what = "holds, a collapse, tows, an escort, a naval convoy, a defend primary or ground allies";
        } else if (!setPieces.isEmpty() || sections.stream().anyMatch(s -> s.arena() || s.timedHalt())) {
            what = "set pieces, an arena or a timed halt";
        } else if (waves.stream()
                .anyMatch(wave -> wave.enemy().chain().isPresent() && (!current || inReversed(sections, wave.t())))) {
            what = current ? "segment chains entering while it runs reversed" : "segment chains";
        }
        if (what != null) {
            throw new IllegalArgumentException("a level with a reversed scroll has no " + what);
        }
    }

    /** M11 part A: whether level time {@code t} s falls in a section that runs reversed. */
    private static boolean inReversed(List<Section> sections, double t) {
        double start = 0;
        for (Section section : sections) {
            if (t >= start && t < section.end() && section.reversed()) {
                return true;
            }
            start = section.end();
        }
        return false;
    }

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): whether a section runs reversed (its speed below 0): its
     * waves and ground units are flipped ({@link Sortie#travelAt}).
     */
    public boolean reverses() {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).reversed()) {
                return true;
            }
        }
        return false;
    }

    /** M5 part G: whether a section is a timed halt (speed 0 without an arena, Level 13's defend phase). */
    public boolean timedHalts() {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).timedHalt()) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part G: whether level time {@code t} s lies in a timed halt section (from the section before's
     * end, inclusive, to its own end): what enters at the top edge with the scroll then would wait there
     * for the whole halt.
     */
    public boolean timedHaltAt(double t) {
        double start = 0;
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (section.timedHalt() && t >= start && t < section.end()) {
                return true;
            }
            start = section.end();
        }
        return false;
    }

    /** The level's length in seconds. */
    public double seconds() {
        return sections.getLast().end();
    }

    /** The names of the groups the ground units' {@link GroundUnit#group()} indexes: the primary's or the secondary's. */
    public List<String> groups() {
        return objectives.groups();
    }
}
