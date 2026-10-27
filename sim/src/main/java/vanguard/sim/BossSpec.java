package vanguard.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A boss's script at one difficulty (design/enemies/bosses): it arrives at {@code arriveSeconds} on
 * the level clock on {@code layer}, descends from above the top edge at {@code descentSpeed} px/s
 * until its centre is at {@code hoverY}, then holds there with a sideways sine until it is killed.
 * Its parts (heads, core, sacs) are those of its {@link SetPieceSpec}; the
 * {@link Chain}s hang parts on articulated necks that bend toward the player. Its {@link Phase}s
 * run in order, each ending on how many of a set of parts are still alive or on its timer; a phase
 * may start with a {@link Move} (a descent to another layer and a turn into another {@link Pose}),
 * open its parts in {@link Windows} that release {@link Spawn}s, and fire its attacks.
 *
 * <p>Part G (the Brood Carrier): a boss that {@code engagesOnArrival} starts its first phase when
 * it arrives (its entrance is part of the fight) instead of when it settles; its
 * {@code armoured} parts fire but take no damage, are not in the bar and pay nothing; its
 * {@code poses} give each part its offset per pose (the first is the arrival pose).
 *
 * <p>M5 part E (design/enemies/bosses/harbour-kraken, user decisions E5–E7): an {@link Arena arena
 * boss} has no descent: it is anchored to the ground, scrolls in with it before its arrival and
 * engages when the scroll halts in its arena; its parts change layer ({@code sub} under the water,
 * {@code ground} on the surface), its slam arms slam telegraphed lanes ({@link SlamArena}).
 *
 * @param x its centre's x at rest, px from the left
 * @param hoverY its centre's height at rest, px from the bottom edge (y up)
 * @param sineAmplitude px each side of {@code x} it sways once settled
 * @param sinePeriod s per sway
 * @param layer the layer it arrives on ({@code high-air}: out of reach of all but homing shots,
 *     drawn at the high-air scale, no contact)
 * @param midBoss a mid-boss has the short bar
 * @param barName the name on its bar
 * @param parSeconds a kill within this time from the bar appearing pays the Boss rush bonus
 * @param engagesOnArrival its first phase starts when it arrives, not when it settles
 * @param armoured the fire-only parts: never damaged, out of the bar and the bounty
 * @param poses the part layouts, the arrival pose first; empty: the parts' own offsets only
 * @param deathSeconds how long its chained death runs (the parts bursting tail to head), for the
 *     game; 0 for the mid-boss's quick chain
 * @param arena M5 part E: the anchored arena boss's lanes, slams, part layers and weak spots; empty
 *     for a boss that descends
 * @param incomingSeconds round 41 (the 2026-10-19 playtest, Level 14): with more than 0, a {@link
 *     SimEvents.Type#BOSS_INCOMING} this long before its arrival on the level clock (the warning box shown
 *     before the boss is reached); 0 for none (every boss before it)
 * @param stride M7 part F (design/enemies/bosses/revenant-walker): a walking arena boss's passes ({@link
 *     StrideArena}); empty for every other boss. It needs an {@code arena} (no lanes: its part layers and its
 *     weak spots, the vents)
 * @param plate M7 part F: a walking boss's front plate, which turns every shot and the beam on its heading side;
 *     empty for none
 * @param cone M7 part F: a walking boss's rear flame cone; empty for none
 * @param breach M7 part H (design/enemies/bosses/dust-colossus): a breaching arena boss's breach cycle ({@link
 *     BreachArena}); empty for every other boss. It needs an {@code arena} without lanes (its part layers and its
 *     weak spots: the gullet)
 * @param suction M7 part H: a breaching boss's suction toward its open mouth; empty for none
 * @param vents M7 part H, step H3: a breaching boss's spine vents, raised with its breaches; empty for none
 * @param eruption M7 part H, step H3: a breaching boss's eruption at its under points (the marker); empty: an under
 *     point runs the normal cycle
 * @param debris M7 part H, step H3: the rocks a breaching boss tears loose toward its open mouth; empty for none
 * @param coil M8 part H (design/enemies/bosses/abyssal-maw): a coiled arena boss's body and script ({@link CoilArena});
 *     empty for every other boss. It needs an {@code arena} without lanes (its part layers, the throat's weak spot)
 */
public record BossSpec(
        double arriveSeconds,
        double x,
        double hoverY,
        double descentSpeed,
        double sineAmplitude,
        double sinePeriod,
        Layer layer,
        boolean midBoss,
        String barName,
        double parSeconds,
        List<Chain> chains,
        List<Attack> attacks,
        List<Phase> phases,
        boolean engagesOnArrival,
        List<Integer> armoured,
        List<Pose> poses,
        double deathSeconds,
        Optional<Arena> arena,
        double incomingSeconds,
        Optional<Stride> stride,
        Optional<Plate> plate,
        Optional<Cone> cone,
        Optional<Breach> breach,
        Optional<Suction> suction,
        Optional<Vents> vents,
        Optional<Eruption> eruption,
        Optional<DebrisFlow> debris,
        Optional<CoilSpec> coil) {
    /** The beat a later phase holds its fire by default in the data (the frigate's crown opening), s. */
    public static final double PHASE_DELAY_SECONDS = 1;
    /** How much larger a unit is drawn on {@code high-air} than on the play plane; a boss's part offsets grow with it. */
    public static final double HIGH_AIR_SCALE = 1.25;

    public BossSpec {
        chains = List.copyOf(chains);
        attacks = List.copyOf(attacks);
        phases = List.copyOf(phases);
        armoured = List.copyOf(armoured);
        poses = List.copyOf(poses);
        if (phases.isEmpty()
                || !(descentSpeed > 0 || arena.isPresent())
                || !(sinePeriod > 0)
                || !(parSeconds > 0)
                || deathSeconds < 0
                || !(incomingSeconds >= 0)) {
            throw new IllegalArgumentException(barName + ": a boss has phases, a descent, a sway period and a par");
        }
        for (Phase phase : phases) {
            if (phase.arena() != PhaseArena.NONE && arena.isEmpty()) {
                throw new IllegalArgumentException(
                        barName + ": phase " + phase.name() + " slams or surfaces without an arena");
            }
        }
        for (Phase phase : phases) {
            if (phase.move().isPresent() && phase.move().get().pose() >= Math.max(1, poses.size())) {
                throw new IllegalArgumentException(barName + ": phase " + phase.name() + " turns into no pose");
            }
        }
        if (stride.isPresent() && (arena.isEmpty() || arena.get().lanes().count() > 0)) {
            throw new IllegalArgumentException(barName + ": a walking boss has an arena without lanes");
        }
        if (stride.isPresent() && phases.stream().anyMatch(phase -> phase.move().isPresent())) {
            throw new IllegalArgumentException(barName + ": a walking boss has no phase moves");
        }
        if (stride.isEmpty() && (plate.isPresent() || cone.isPresent())) {
            throw new IllegalArgumentException(barName + ": only a walking boss has a front plate or a flame cone");
        }
        if (breach.isPresent()
                && (arena.isEmpty()
                        || arena.get().lanes().count() > 0
                        || stride.isPresent()
                        || phases.stream().anyMatch(phase -> phase.move().isPresent()))) {
            throw new IllegalArgumentException(
                    barName + ": a breaching boss has an arena without lanes, no stride and no phase moves");
        }
        if (suction.isPresent() && breach.isEmpty()) {
            throw new IllegalArgumentException(barName + ": only a breaching boss sucks toward its mouth");
        }
        if ((vents.isPresent() || eruption.isPresent() || debris.isPresent()) && breach.isEmpty()) {
            throw new IllegalArgumentException(
                    barName + ": only a breaching boss has spine vents, an eruption or debris");
        }
        if (vents.isPresent()) {
            Vents v = vents.get();
            if (v.burst() >= attacks.size() || attacks.get(v.burst()).pattern() == Pattern.SPIRAL) {
                throw new IllegalArgumentException(
                        barName + ": the spine vents' burst is one of its attacks, no spiral");
            }
            List<Layer> layers = arena.orElseThrow().partLayers();
            for (int p : v.parts()) {
                if (p < layers.size() && layers.get(p) != Layer.BURIED) {
                    throw new IllegalArgumentException(barName + ": a spine vent starts buried");
                }
            }
        }
        if (coil.isPresent()) {
            if (arena.isEmpty()
                    || arena.get().lanes().count() > 0
                    || stride.isPresent()
                    || breach.isPresent()
                    || phases.stream().anyMatch(phase -> phase.move().isPresent())) {
                throw new IllegalArgumentException(barName
                        + ": a coiled boss has an arena without lanes, no stride, no breach and no phase moves");
            }
            coil.get().check(barName, phases, attacks);
        }
    }

    /** M8 part H: this boss coiled round its arena ({@link CoilArena}, an arena boss without lanes); empty: none. */
    public BossSpec withCoil(Optional<CoilSpec> withCoil) {
        return new BossSpecBuilder(this).coil(withCoil).build();
    }

    /** M8 part H: whether it is coiled round its arena ({@link CoilArena}): the Abyssal Maw. */
    public boolean coils() {
        return coil.isPresent();
    }

    /**
     * Without round 41's incoming warning and the mechanics after it. Kept for {@code game/}'s callers until step RF6
     * has moved them to {@link #builder}; remove it then.
     */
    public BossSpec(
            double arriveSeconds,
            double x,
            double hoverY,
            double descentSpeed,
            double sineAmplitude,
            double sinePeriod,
            Layer layer,
            boolean midBoss,
            String barName,
            double parSeconds,
            List<Chain> chains,
            List<Attack> attacks,
            List<Phase> phases,
            boolean engagesOnArrival,
            List<Integer> armoured,
            List<Pose> poses,
            double deathSeconds,
            Optional<Arena> arena) {
        this(
                arriveSeconds,
                x,
                hoverY,
                descentSpeed,
                sineAmplitude,
                sinePeriod,
                layer,
                midBoss,
                barName,
                parSeconds,
                chains,
                attacks,
                phases,
                engagesOnArrival,
                armoured,
                poses,
                deathSeconds,
                arena,
                0,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    /**
     * A boss's base script: it engages when it settles, all parts damageable, one pose, no arena and none of the later
     * mechanics. {@link #builder} sets the rest before the checks run; the {@code withX} builders change a built one.
     */
    public static BossSpec of(
            double arriveSeconds,
            double x,
            double hoverY,
            double descentSpeed,
            double sineAmplitude,
            double sinePeriod,
            Layer layer,
            boolean midBoss,
            String barName,
            double parSeconds,
            List<Chain> chains,
            List<Attack> attacks,
            List<Phase> phases) {
        return builder(
                        arriveSeconds,
                        x,
                        hoverY,
                        descentSpeed,
                        sineAmplitude,
                        sinePeriod,
                        layer,
                        midBoss,
                        barName,
                        parSeconds,
                        chains,
                        attacks,
                        phases)
                .build();
    }

    /** A builder for a boss with this base script and every other component at {@link #of}'s defaults. */
    public static BossSpecBuilder builder(
            double arriveSeconds,
            double x,
            double hoverY,
            double descentSpeed,
            double sineAmplitude,
            double sinePeriod,
            Layer layer,
            boolean midBoss,
            String barName,
            double parSeconds,
            List<Chain> chains,
            List<Attack> attacks,
            List<Phase> phases) {
        return new BossSpecBuilder(
                arriveSeconds,
                x,
                hoverY,
                descentSpeed,
                sineAmplitude,
                sinePeriod,
                layer,
                midBoss,
                barName,
                parSeconds,
                chains,
                attacks,
                phases);
    }

    /** Round 41: this boss with its {@link SimEvents.Type#BOSS_INCOMING} {@code seconds} before its arrival (0: none). */
    public BossSpec withIncomingSeconds(double seconds) {
        return new BossSpecBuilder(this).incomingSeconds(seconds).build();
    }

    /** M7 part F: this boss walking {@code withStride}'s passes (an arena boss without lanes); empty: none. */
    public BossSpec withStride(Optional<Stride> withStride) {
        return new BossSpecBuilder(this).stride(withStride).build();
    }

    /** M7 part F: this walking boss with {@code withPlate} as its front plate; empty: none. */
    public BossSpec withPlate(Optional<Plate> withPlate) {
        return new BossSpecBuilder(this).plate(withPlate).build();
    }

    /** M7 part F: this walking boss with {@code withCone} as its rear flame cone; empty: none. */
    public BossSpec withCone(Optional<Cone> withCone) {
        return new BossSpecBuilder(this).cone(withCone).build();
    }

    /** M7 part H: this boss breaching by {@code withBreach}'s cycle (an arena boss without lanes); empty: none. */
    public BossSpec withBreach(Optional<Breach> withBreach) {
        return new BossSpecBuilder(this).breach(withBreach).build();
    }

    /** M7 part H: this breaching boss with {@code withSuction} toward its open mouth; empty: none. */
    public BossSpec withSuction(Optional<Suction> withSuction) {
        return new BossSpecBuilder(this).suction(withSuction).build();
    }

    /** M7 part H, step H3: this breaching boss raising {@code withVents} with its breaches; empty: none. */
    public BossSpec withVents(Optional<Vents> withVents) {
        return new BossSpecBuilder(this).vents(withVents).build();
    }

    /** M7 part H, step H3: this breaching boss erupting under the ship at its under points; empty: none. */
    public BossSpec withEruption(Optional<Eruption> withEruption) {
        return new BossSpecBuilder(this).eruption(withEruption).build();
    }

    /** M7 part H, step H3: this breaching boss tearing debris loose toward its open mouth; empty: none. */
    public BossSpec withDebris(Optional<DebrisFlow> withDebris) {
        return new BossSpecBuilder(this).debris(withDebris).build();
    }

    /** M7 part H: whether it breaches ({@link BreachArena}): the Dust Colossus. */
    public boolean breaches() {
        return breach.isPresent();
    }

    /** M7 part F: whether it walks passes ({@link StrideArena}): the Revenant Walker. */
    public boolean walks() {
        return stride.isPresent();
    }

    /** The distinct units its windows and (M5 part E) its releases let in, for the level's enemy kinds. */
    public List<EnemySpec> spawnKinds() {
        List<EnemySpec> kinds = new ArrayList<>();
        for (Phase phase : phases) {
            for (Spawn spawn : phase.windows().map(Windows::spawns).orElse(List.of())) {
                if (!kinds.contains(spawn.enemy())) {
                    kinds.add(spawn.enemy());
                }
            }
            phase.arena()
                    .release()
                    .map(Release::enemy)
                    .filter(enemy -> !kinds.contains(enemy))
                    .ifPresent(kinds::add);
        }
        // M8 part H: a coiled boss's shed Eel Swarms.
        coil.flatMap(CoilSpec::shed)
                .map(CoilSpec.Shed::enemy)
                .filter(enemy -> !kinds.contains(enemy))
                .ifPresent(kinds::add);
        return List.copyOf(kinds);
    }

    /** Whether it is an arena boss (M5 part E): anchored, without a descent. */
    public boolean anchored() {
        return arena.isPresent();
    }

    /**
     * A neck: {@code segments} armoured hit boxes of {@code box} from the anchor ({@code fromDx},
     * {@code fromDy}) on the body (its own frame, as a part's offset) to {@code part}, which sits at
     * the chain's end; at rest the chain runs straight to that part's offset. The anchor turns
     * toward the player by up to {@code bendRadians} and each piece after it follows the one before
     * {@code lagSeconds} late (a first-order lag), so the head swings through last.
     *
     * <p>M5 part E ({@code motion: slam}, the Harbour Kraken's arms): a {@code slam} chain does not
     * bend; its segments follow the slam cycle ({@link SlamArena}): at rest a straight line from the
     * anchor to its part, from the rise until the arm has sunk laid along the lane it slams. Its
     * segments are not armoured: they are the part's own hit boxes (a hit on one damages the part).
     */
    public record Chain(
            String name,
            double fromDx,
            double fromDy,
            int part,
            int segments,
            Hitbox box,
            double lagSeconds,
            double bendRadians,
            boolean slam) {
        public Chain {
            if (segments < 1 || !(lagSeconds > 0) || bendRadians < 0) {
                throw new IllegalArgumentException(name + ": a chain has segments, a lag and a bend");
            }
        }

        /** A neck that bends toward the player (Levels 05–07). */
        public Chain(
                String name,
                double fromDx,
                double fromDy,
                int part,
                int segments,
                Hitbox box,
                double lagSeconds,
                double bendRadians) {
            this(name, fromDx, fromDy, part, segments, box, lagSeconds, bendRadians, false);
        }
    }

    /** The attack patterns a boss fires (design/enemies, attack vocabulary). */
    public enum Pattern {
        /** Bursts of aimed shots: {@link Attack#gun()}'s burst, {@link Attack#burstGapSeconds()} apart. */
        AIMED,
        /** A ring of {@link Attack#count()} bullets, once at the start of its turn. */
        RING,
        /** {@link Attack#arms()} arms turning at {@link Attack#turnRadiansPerSecond()}, a bullet per arm every interval. */
        SPIRAL,
        /** An n-way fan centred on the ship: {@link Attack#gun()}'s fan and spread, once per interval. */
        FAN,
        /**
         * M5 part H (design/enemies, Boss attacks and root bursts; decision H5 = a): the Polyp Mortar's
         * lob from a part: {@link Attack#gun()}'s {@link EnemyGun.MortarSpec} (its marker's time, its impact
         * circle, its ring) lobbed at the ship's position at the throw, once per interval.
         */
        LOB,
        /**
         * M5 part H: the Mantis's laser sweep from a part (the airborne Siege Spire's core): {@link
         * Attack#sweep()}'s arc, centred on the ship's bearing at the telegraph's start, shown for its
         * telegraph, then the beam sweeps it, hurting the ship and Rook once each per sweep.
         */
        SWEEP
    }

    /**
     * A boss attack, with the difficulty's levers and changes applied.
     *
     * @param gun its interval, burst, bullet speed and damage (a fan's bullets and spread; M5 part H: a
     *     lob's mortar)
     * @param rotate the parts that fire it take turns: one volley every interval among the living ones
     * @param durationSeconds how long a spiral runs in an alternation (a ring's turn is its interval)
     * @param parts the parts that fire it; empty: the living parts the phase ends on
     * @param stagger M5 part H: each of its parts fires once per interval on its own clock, part k of n
     *     {@code k × interval ÷ n} after the first; a destroyed part's turn passes silently (the Siege
     *     Spire's pods "every 1.6 s each, staggered"); not with {@code rotate}
     * @param sweep M5 part H: a {@link Pattern#SWEEP}'s arc, sweep and telegraph times, beam length and
     *     width, damage and its origin below the part ({@link EnemySpec.Sweep#originDown()}); its interval
     *     and first delay are not read (the gun's interval is); empty for other patterns
     * @param down round 34 (P2, the kinder Siege Spire): the parts of {@code parts} that fire this
     *     {@link Pattern#AIMED} attack straight down the screen instead of at the ship (a part's {@code
     *     aim: down}); round 43: a walking boss's {@link Pattern#FAN} too, along its heading; empty for others
     * @param aimTurns round 40 (the Siege Spire's lanes, proposal 4a of the Act 2 playtest): its parts
     *     take turns to aim, one at a time ({@link AimTurns}); empty for none
     */
    public record Attack(
            String name,
            Pattern pattern,
            EnemyGun gun,
            double burstGapSeconds,
            boolean rotate,
            int count,
            int arms,
            double turnRadiansPerSecond,
            double durationSeconds,
            List<Integer> parts,
            boolean stagger,
            Optional<EnemySpec.Sweep> sweep,
            List<Integer> down,
            Optional<AimTurns> aimTurns) {
        public Attack {
            parts = List.copyOf(parts);
            down = List.copyOf(down);
            if (aimTurns.isPresent() && (pattern != Pattern.AIMED || rotate)) {
                throw new IllegalArgumentException(
                        name + ": only an aimed attack that does not rotate takes aim turns");
            }
            if (!down.isEmpty()
                    && (!(pattern == Pattern.AIMED || pattern == Pattern.FAN) || !parts.containsAll(down))) {
                throw new IllegalArgumentException(
                        name + ": only an aimed or fan attack's own parts fire straight down");
            }
            if (pattern == Pattern.LOB && gun.mortar().isEmpty()) {
                throw new IllegalArgumentException(name + ": a lob fires a mortar");
            }
            if ((pattern == Pattern.SWEEP) != sweep.isPresent()) {
                throw new IllegalArgumentException(name + ": a sweep, and only a sweep, has a sweep block");
            }
            if (stagger && rotate) {
                throw new IllegalArgumentException(name + ": an attack rotates or staggers, not both");
            }
        }

        /** An attack without round 40's aim turns. */
        public Attack(
                String name,
                Pattern pattern,
                EnemyGun gun,
                double burstGapSeconds,
                boolean rotate,
                int count,
                int arms,
                double turnRadiansPerSecond,
                double durationSeconds,
                List<Integer> parts,
                boolean stagger,
                Optional<EnemySpec.Sweep> sweep,
                List<Integer> down) {
            this(
                    name,
                    pattern,
                    gun,
                    burstGapSeconds,
                    rotate,
                    count,
                    arms,
                    turnRadiansPerSecond,
                    durationSeconds,
                    parts,
                    stagger,
                    sweep,
                    down,
                    Optional.empty());
        }

        /** An attack whose parts all fire as its pattern says (none straight down). */
        public Attack(
                String name,
                Pattern pattern,
                EnemyGun gun,
                double burstGapSeconds,
                boolean rotate,
                int count,
                int arms,
                double turnRadiansPerSecond,
                double durationSeconds,
                List<Integer> parts,
                boolean stagger,
                Optional<EnemySpec.Sweep> sweep) {
            this(
                    name,
                    pattern,
                    gun,
                    burstGapSeconds,
                    rotate,
                    count,
                    arms,
                    turnRadiansPerSecond,
                    durationSeconds,
                    parts,
                    stagger,
                    sweep,
                    List.of());
        }

        /** An attack without M5 part H's stagger and sweep. */
        public Attack(
                String name,
                Pattern pattern,
                EnemyGun gun,
                double burstGapSeconds,
                boolean rotate,
                int count,
                int arms,
                double turnRadiansPerSecond,
                double durationSeconds,
                List<Integer> parts) {
            this(
                    name,
                    pattern,
                    gun,
                    burstGapSeconds,
                    rotate,
                    count,
                    arms,
                    turnRadiansPerSecond,
                    durationSeconds,
                    parts,
                    false,
                    Optional.empty(),
                    List.of());
        }

        /** Whether part {@code p} fires this attack straight down (round 34, P2). */
        public boolean firesDown(int p) {
            return down.contains(p);
        }

        /** How long its turn lasts in an alternation. */
        public double turnSeconds() {
            return pattern == Pattern.SPIRAL ? durationSeconds : gun.intervalSeconds();
        }
    }

    /**
     * Round 40 (design/enemies/bosses/siege-spire; proposal 4a of the Act 2 playtest, pending the user's
     * review): an aimed attack's parts take turns to aim, one at a time. Every {@code everySeconds} (the
     * first that long after the phase's delay) the next living part of the attack, in its parts' order,
     * fires one shot of the gun at the ship; for the {@code glowSeconds} before it that part glows (the
     * tell) and holds its own regular fire. Between its turns a part fires as the attack says (with
     * {@code aim: down}: straight down its column). Not in an alternation.
     */
    public record AimTurns(double everySeconds, double glowSeconds) {
        public AimTurns {
            if (!(everySeconds > 0) || !(glowSeconds >= 0) || !(glowSeconds < everySeconds)) {
                throw new IllegalArgumentException("aim turns come every so often, each after a shorter glow");
            }
        }
    }

    /**
     * A phase: it ends once at most {@code left} of {@code untilParts} are alive, or {@code seconds}
     * after it engaged (a timeout; infinite for none). It starts with its {@code move}, if any (the
     * boss takes no damage and holds its fire until the move is done); then, {@code delaySeconds}
     * later, it fires {@code attacks} together (each a volley one interval after that), or in turn
     * when {@code alternate}, and opens its {@code windows}; sends its {@code stream}; the parts in
     * {@code exposes} take damage only from this phase on; the chains bend {@code bendRadians} in it
     * (NaN: their own bend). M5 part E: its {@code arena} keys (an arena boss's slams, surfacing,
     * release and drop; {@link PhaseArena#NONE} for others).
     */
    public record Phase(
            String name,
            List<Integer> untilParts,
            int left,
            List<Integer> attacks,
            boolean alternate,
            Optional<Stream> stream,
            List<Integer> exposes,
            double bendRadians,
            double seconds,
            double delaySeconds,
            Optional<Move> move,
            Optional<Windows> windows,
            PhaseArena arena) {
        public Phase {
            untilParts = List.copyOf(untilParts);
            attacks = List.copyOf(attacks);
            exposes = List.copyOf(exposes);
            if (!(seconds > 0) || !(delaySeconds >= 0)) {
                throw new IllegalArgumentException(name + ": a phase has a positive timeout and a delay");
            }
        }

        /** A phase of a boss that descends (Levels 05–07): no arena keys. */
        public Phase(
                String name,
                List<Integer> untilParts,
                int left,
                List<Integer> attacks,
                boolean alternate,
                Optional<Stream> stream,
                List<Integer> exposes,
                double bendRadians,
                double seconds,
                double delaySeconds,
                Optional<Move> move,
                Optional<Windows> windows) {
            this(
                    name,
                    untilParts,
                    left,
                    attacks,
                    alternate,
                    stream,
                    exposes,
                    bendRadians,
                    seconds,
                    delaySeconds,
                    move,
                    windows,
                    PhaseArena.NONE);
        }

        /** A phase of Level 05's kind: no timeout, delay, move or windows. */
        public Phase(
                String name,
                List<Integer> untilParts,
                int left,
                List<Integer> attacks,
                boolean alternate,
                Optional<Stream> stream,
                List<Integer> exposes,
                double bendRadians) {
            this(
                    name,
                    untilParts,
                    left,
                    attacks,
                    alternate,
                    stream,
                    exposes,
                    bendRadians,
                    Double.POSITIVE_INFINITY,
                    0,
                    Optional.empty(),
                    Optional.empty());
        }

        /** Whether it ends on its timer as well as (or instead of) on its parts. */
        public boolean timed() {
            return Double.isFinite(seconds);
        }
    }

    /**
     * A stream of {@code count} units of {@code enemy} from the side edges, {@code intervalSeconds}
     * apart (alternating sides, or all from one): the first when the boss settles, then every
     * {@code everySeconds} while its phase lasts.
     */
    public record Stream(EnemySpec enemy, int count, double everySeconds, double intervalSeconds, WaveSpec.Edge edge) {
        public Stream {
            if (edge == WaveSpec.Edge.TOP || edge == WaveSpec.Edge.BOTTOM) {
                throw new IllegalArgumentException("a stream comes from the side edges: " + edge);
            }
        }
    }

    /**
     * A part layout: each part's offset from the centre in this pose (px, x right, y up, on the play
     * plane; on {@code high-air} the offsets grow with the {@link #HIGH_AIR_SCALE}), and the armoured
     * body's hit box. A pose is drawn from its own pre-rendered sprites: nothing is rotated.
     */
    public record Pose(String name, Hitbox body, List<Offset> offsets) {
        public Pose {
            offsets = List.copyOf(offsets);
        }
    }

    /** A part's offset in a {@link Pose}, px right and up from the centre. */
    public record Offset(double dx, double dy) {}

    /**
     * A phase's opening move, invulnerable: over {@code descendSeconds} the centre glides to
     * ({@code x}, {@code y}) (px from the left, px up from the bottom edge) and the boss sinks or
     * rises to {@code layer}; then over {@code turnSeconds} it turns in place into pose
     * {@code pose} (an index into {@link BossSpec#poses()}), the renderer stepping through the
     * turn's pre-rendered frames. It then holds there (swaying around the new x).
     *
     * <p>M5 part H: on an anchored arena boss it is the lift (design/enemies/bosses/siege-spire,
     * decision H4 = a): the glide eases out of the anchor (smoothstep) and the layer changes at its end,
     * for the boss and its lifting parts (not before: it takes no contact while it lifts); the parts
     * destroyed or armoured when it starts stay at the anchor (the stump).
     */
    public record Move(double x, double y, Layer layer, double descendSeconds, int pose, double turnSeconds) {
        public Move {
            if (descendSeconds < 0 || turnSeconds < 0 || pose < 0) {
                throw new IllegalArgumentException("a move has no negative times and a pose");
            }
        }
    }

    /**
     * A phase's windows (the Brood Carrier's sacs): every {@code everySeconds} the next group in
     * order with a living part on the play field opens for {@code openSeconds} (every such group at
     * once when {@code all}), the first {@code offsetSeconds} after the phase's delay. A part in a
     * group takes damage only while open. Each opening releases its {@link Spawn} (the list in turn,
     * one entry per opening) from every group it opened: the entry's count from a group whose parts
     * all live, a share of it rounded up from one with parts destroyed, none from a group without
     * living parts.
     *
     * @param groups part indexes, in the order the windows cycle through them
     */
    public record Windows(
            List<List<Integer>> groups,
            double everySeconds,
            double openSeconds,
            double offsetSeconds,
            boolean all,
            List<Spawn> spawns) {
        public Windows {
            groups = groups.stream().map(List::copyOf).toList();
            spawns = List.copyOf(spawns);
            if (groups.isEmpty()
                    || groups.stream().anyMatch(List::isEmpty)
                    || !(everySeconds > 0)
                    || !(openSeconds > 0)
                    || offsetSeconds < 0) {
                throw new IllegalArgumentException("windows have groups of parts, an interval and an open time");
            }
        }

        /** Whether part {@code p} is in one of its groups. */
        public boolean holds(int p) {
            for (List<Integer> group : groups) {
                if (group.contains(p)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * What an opened window group releases: {@code count} units of {@code enemy} (the difficulty's
     * count) from its open parts in turn, spread evenly over {@code arcRadians} centred on the
     * direction to the ship, flying straight out at {@code speed} px/s. A unit that glides
     * ({@code glideSeconds} above 0) then holds for its hover time, its gun firing, and leaves
     * down the screen; otherwise it flies on until it leaves the play field.
     *
     * <p>M5 part H (design/enemies/bosses/siege-spire, the maw's Wraiths; decision H3 = a): with
     * {@code ambushMost} above 0 each unit is an <em>ambush launch</em> instead: it flies its enemy's
     * {@link EnemySpec.Ambush} rear path from the part (cloaked down its lane at {@code speed}, past the
     * ship and off the bottom edge, the gap, back up to its hold point, the decloak, the hold, out up the
     * nearer side lane), the bottom edge warned {@link WaveSchedule#EDGE_WARNING_SECONDS} ahead of its
     * re-entry, a rear unit from its re-entry on (Rook's Trail); a launch that would put more than
     * {@code ambushMost} of the boss's launched units on the field leaves the rest out. The arc and the
     * glide are not read.
     */
    public record Spawn(
            String name,
            EnemySpec enemy,
            int count,
            double speed,
            double arcRadians,
            double glideSeconds,
            int ambushMost) {
        public Spawn {
            if (count < 0 || !(speed > 0) || arcRadians < 0 || glideSeconds < 0 || ambushMost < 0) {
                throw new IllegalArgumentException(name + ": a spawn has a count, a speed, an arc and a glide");
            }
            if (ambushMost > 0 && (enemy.ambush().isEmpty() || enemy.hover().isEmpty())) {
                throw new IllegalArgumentException(name + ": an ambush launch flies its enemy's ambush path and hover");
            }
        }

        /** A spawn flying straight out (without M5 part H's ambush launch). */
        public Spawn(String name, EnemySpec enemy, int count, double speed, double arcRadians, double glideSeconds) {
            this(name, enemy, count, speed, arcRadians, glideSeconds, 0);
        }

        /** M5 part H: whether its units are ambush launches. */
        public boolean ambush() {
            return ambushMost > 0;
        }
    }

    /**
     * M5 part E, an arena boss (design/enemies/bosses/harbour-kraken; the anchored arrival, user
     * decisions E5 = a and E7 = a): anchored to the ground, its centre {@code hoverY} px above the
     * bottom edge once the scroll has halted in its arena, it scrolls in with the ground before it
     * arrives and engages at the halt.
     *
     * <p>M5 part H (design/enemies/bosses/siege-spire; decisions H1 and H4 = a): an arena boss may have
     * no lanes ({@link Lanes#NONE}); a phase's {@link Move} on it is a <em>lift</em>: the boss leaves its
     * anchor (the parts destroyed or armoured by then stay there as the stump), glides to the move's
     * station, its lifting parts take the move's layer at the glide's end, and then it sways there.
     *
     * @param lanes its slam lanes; {@link Lanes#NONE} without
     * @param slam its slam cycle; null without lanes
     * @param partLayers per part, the layer it starts on ({@code sub} or {@code ground}; M5 part H: or
     *     {@code air}); its script moves it
     * @param spots the weak spots on its parts
     */
    public record Arena(Lanes lanes, Slam slam, List<Layer> partLayers, List<Spot> spots) {
        public Arena {
            partLayers = List.copyOf(partLayers);
            spots = List.copyOf(spots);
            if (lanes.count() > 0 && slam == null) {
                throw new IllegalArgumentException("lanes are slammed: give the slam cycle");
            }
        }
    }

    /**
     * M5 part E: an arena boss's slam lanes: {@code count} lanes {@code width} px wide from the play
     * field's left edge (lane 1 first), from {@code top} px above the bottom edge (the platform's
     * lower edge at the halt) down to the bottom edge; {@code arms}, each slam arm's part with the
     * lanes it owns (user decision E5 = a: the left arm lanes 1–2, the right arm 3–4).
     *
     * <p>Round 40 (the reworked Harbour Kraken, proposal 3a of the Act 2 playtest): {@code rooted} arms,
     * one lane each. A rooted arm's part is its <em>root</em>: it stays at its offset (it is not the
     * chain's moving tip), keeps its starting layer (always hittable, also while the head is under the
     * water) and takes {@code raisedMultiplier} times the damage while its arm is raised (from its
     * telegraph until it has sunk: the tell); the arm's segments are the root's hit boxes only while the
     * arm lies along its lane out of the water (its rise and awash), laid from the root's lower edge
     * down. Cutting a root ends its lane's slams for good. Their slams pick only lanes whose root lives
     * ({@link SlamArena}).
     *
     * @param rooted round 40: every arm is a rooted one-lane arm
     * @param raisedMultiplier round 40: a rooted arm's damage multiplier while it is raised (on top of
     *     the part's own); 1 otherwise
     */
    public record Lanes(int count, double width, double top, List<Arm> arms, boolean rooted, double raisedMultiplier) {
        /** No lanes. */
        public static final Lanes NONE = new Lanes(0, 0, 0, List.of());

        public Lanes {
            arms = List.copyOf(arms);
            if (count < 0
                    || count > Integer.SIZE - 2
                    || (count > 0 && !(width > 0))
                    || count * width > PlayField.WIDTH + 1e-9) {
                throw new IllegalArgumentException("lanes of a width on the play field");
            }
            int owned = 0;
            for (Arm arm : arms) {
                if (arm.lanes() == 0 || (arm.lanes() >> (count + 1)) != 0 || (arm.lanes() & 1) != 0) {
                    throw new IllegalArgumentException("an arm owns lanes 1-" + count);
                }
                if (rooted && (Integer.bitCount(arm.lanes()) != 1 || (owned & arm.lanes()) != 0)) {
                    throw new IllegalArgumentException("a rooted arm owns one lane of its own");
                }
                owned |= arm.lanes();
            }
            if (!(raisedMultiplier > 0) || (!rooted && raisedMultiplier != 1)) {
                throw new IllegalArgumentException("only rooted arms take more damage while raised");
            }
        }

        /** Lanes whose arms are not rooted (M5 part E). */
        public Lanes(int count, double width, double top, List<Arm> arms) {
            this(count, width, top, arms, false, 1);
        }

        /** The centre of lane {@code lane} (1-based), px from the left edge. */
        public double centre(int lane) {
            return (lane - 0.5) * width;
        }
    }

    /**
     * A slam arm: the part {@code part} (its chain's end) and the lanes it owns, bit {@code n} for
     * lane {@code n}.
     */
    public record Arm(int part, int lanes) {}

    /**
     * M5 part E, the slam cycle (design/enemies/bosses/harbour-kraken, Lanes and slams): a lane is
     * telegraphed for {@code telegraphSeconds}, the arm rises base-to-tip over {@code riseSeconds} and
     * strikes (the impact: {@code damage} to the ship and the escort whose hit box overlaps the lane,
     * once per slam, a hit to a convoy ship in it, {@code splashCount} bullets of {@code
     * splashDamage} at {@code splashSpeed} px/s from along the arm), lies awash for {@code
     * awashSeconds} and sinks over {@code sinkSeconds}. A volley slams {@code volleyLanes} lanes (2, hard
     * 3: an arm whose half holds two of them slams its second {@code secondSeconds} after its first).
     */
    public record Slam(
            double telegraphSeconds,
            double riseSeconds,
            double awashSeconds,
            double sinkSeconds,
            double damage,
            int splashCount,
            double splashSpeed,
            double splashDamage,
            int volleyLanes,
            double secondSeconds) {
        /** The second lane of one arm in a volley: this long after its first, s (E5 = a). */
        public static final double SECOND_SECONDS = 0.5;

        public Slam {
            if (!(telegraphSeconds > 0)
                    || !(riseSeconds > 0)
                    || !(awashSeconds > 0)
                    || !(sinkSeconds > 0)
                    || damage < 0
                    || splashCount < 0
                    || (splashCount > 0 && !(splashSpeed > 0))
                    || volleyLanes < 1
                    || secondSeconds < 0) {
                throw new IllegalArgumentException("a slam has a telegraph, a rise, an awash and a sink time");
            }
        }
    }

    /**
     * M5 part E, a weak spot (the Kraken's eyes): a box of {@code box} at ({@code dx}, {@code dy})
     * from part {@code part}'s centre (px right and up) that routes the damage to the part at its own
     * {@code multiplier}, only while the part's surfacing window is open when {@code whileOpen}; the
     * part's multiplier elsewhere.
     */
    public record Spot(String name, int part, double dx, double dy, Hitbox box, double multiplier, boolean whileOpen) {
        public Spot {
            if (!(multiplier > 0)) {
                throw new IllegalArgumentException(name + ": a spot has a multiplier");
            }
        }
    }

    /** How a phase slams (M5 part E). */
    public enum Slamming {
        /** It does not slam. */
        NONE,
        /** One slam at a time: the next telegraph when the arm has sunk. */
        CHAIN,
        /** One slam after each dive of its surfacing part. */
        AFTER_DIVE,
        /** Every {@link PhaseArena#volleySeconds()}: one slam per living arm at once. */
        VOLLEY
    }

    /**
     * M5 part E, a phase's arena keys: it ends after {@code slams} impacts (0: not on slams) or once
     * {@code belowParts} hold less than {@code below} of their HP (NaN: not on a share); it slams as
     * {@code slamming} says ({@code volleySeconds} between volleys); its {@code surface}, its
     * {@code release} and the pickup it {@code drop}s as it begins.
     */
    public record PhaseArena(
            int slams,
            List<Integer> belowParts,
            double below,
            Slamming slamming,
            double volleySeconds,
            Optional<Surface> surface,
            Optional<Release> release,
            Optional<PickupType> drop) {
        /** None of them (a boss that descends). */
        public static final PhaseArena NONE = new PhaseArena(
                0, List.of(), Double.NaN, Slamming.NONE, 0, Optional.empty(), Optional.empty(), Optional.empty());

        public PhaseArena {
            belowParts = List.copyOf(belowParts);
            if (slams < 0
                    || (!Double.isNaN(below) && (belowParts.isEmpty() || !(below > 0) || !(below < 1)))
                    || (slamming == Slamming.VOLLEY && !(volleySeconds > 0))
                    || (slamming == Slamming.AFTER_DIVE && surface.isEmpty())) {
                throw new IllegalArgumentException(
                        "a phase ends on slams or a share below 1 of its parts' HP, volleys have an interval"
                                + " and slams after a dive need a surfacing");
            }
        }

        /** Whether it ends on its parts' HP share. */
        public boolean onShare() {
            return !Double.isNaN(below);
        }
    }

    /**
     * M5 part E, a part's surfacing (the Kraken's head): it rises crown first over {@code riseSeconds}
     * (its layer flipping from {@code sub} to {@code ground} at the middle), its window stays open for
     * {@code openSeconds} (its weak spots open; the phase's attacks fire, each one {@code
     * glowSeconds} after its tell starts, the first {@code glowSeconds} after the window opens, then
     * one every attack interval), then it dives over {@code diveSeconds} (the layer flipping back at
     * the middle); {@code stay}: it rises and stays up, its window open.
     */
    public record Surface(
            int part, double riseSeconds, double openSeconds, double diveSeconds, double glowSeconds, boolean stay) {
        public Surface {
            if (!(riseSeconds > 0) || (!stay && (!(openSeconds > 0) || !(diveSeconds > 0))) || glowSeconds < 0) {
                throw new IllegalArgumentException("a surfacing rises, stays open and dives");
            }
        }
    }

    /**
     * M5 part E, units an arena boss releases once (the Kraken's Driftjelly field): {@code count} units
     * of {@code enemy} placed as a field over {@code lanes} (shared out in turn), at its surfacing
     * part's first surfacing; they count among the level's enemies as they are released.
     */
    public record Release(EnemySpec enemy, int count, List<Integer> lanes) {
        public Release {
            lanes = List.copyOf(lanes);
            if (count < 1 || lanes.isEmpty()) {
                throw new IllegalArgumentException("a release has units and lanes");
            }
        }
    }

    /**
     * M7 part F, a walking boss's passes (design/enemies/bosses/revenant-walker, Passes; {@link StrideArena}): it
     * crosses the halted arena in passes, each from one edge to the opposite one and off the screen, entering from
     * the edges of {@code edgeCycle} in turn (the first, {@link WaveSpec.Edge#TOP}, is its intro); a vertical pass
     * walks one of {@code laneXs}, a horizontal one one of {@code laneYs} (px from the left, px up from the bottom
     * edge). Between two passes it is off the screen for {@code offSeconds}, under the next entry edge's warning (a
     * top entry has none). Its walking speed is {@code phaseSpeeds} of its phase (the last for later phases).
     * {@code besideLaneFirstPhase}: in its first phase a pass takes the lane beside the ship's (the lane nearest the
     * ship moved one toward the field's centre; from the centre lane left and right in turn), later the ship's
     * own. {@code turnInPlaceLastPhase}: its last phase overheats: its vents stay open, it no longer leaves the
     * arena but turns round on the spot at a pass's end over {@code turnSeconds} (its flame cone, after its tell,
     * burning through the turn) and walks back in the ship's lane.
     *
     * <p>Round 44 (the user in round 43, W.a/W.e): {@code hatchArmour}: a part that fires a homing attack (a pod)
     * takes damage only while its hatch is open (its salvo's tell and the salvo), shut it is armoured like the front
     * plate, until its overheating last phase (the hatches jam open as the vents do); {@code lance}: its back-mounted rail lance ({@link Lance}); {@code stomp}: its stomp shockwave ({@link
     * Stomp}).
     *
     * <p>The arrays are the record's own (not copied on access): do not change them.
     *
     * @param edgeCycle {@link WaveSpec.Edge#TOP}, {@link WaveSpec.Edge#BOTTOM}, {@link WaveSpec.Edge#LEFT} or
     *     {@link WaveSpec.Edge#RIGHT}, the first {@code TOP}
     */
    public record Stride(
            double[] laneXs,
            double[] laneYs,
            List<WaveSpec.Edge> edgeCycle,
            double offSeconds,
            double turnSeconds,
            double[] phaseSpeeds,
            boolean besideLaneFirstPhase,
            boolean turnInPlaceLastPhase,
            boolean hatchArmour,
            Optional<Lance> lance,
            Optional<Stomp> stomp) {
        public Stride {
            laneXs = laneXs.clone();
            laneYs = laneYs.clone();
            phaseSpeeds = phaseSpeeds.clone();
            edgeCycle = List.copyOf(edgeCycle);
            if (laneXs.length == 0
                    || laneYs.length == 0
                    || phaseSpeeds.length == 0
                    || edgeCycle.isEmpty()
                    || edgeCycle.getFirst() != WaveSpec.Edge.TOP
                    || !(offSeconds >= 0)
                    || !(turnSeconds > 0)) {
                throw new IllegalArgumentException(
                        "a stride has lanes, speeds, an edge cycle from the top, an off time and a turn time");
            }
            for (WaveSpec.Edge edge : edgeCycle) {
                if (!edge.screenEdge()) {
                    throw new IllegalArgumentException("a stride enters from a screen edge: " + edge);
                }
            }
            for (double speed : phaseSpeeds) {
                if (!(speed > 0)) {
                    throw new IllegalArgumentException("a stride walks at a speed: " + speed);
                }
            }
            for (double lane : laneXs) {
                if (!(lane > 0 && lane < PlayField.WIDTH)) {
                    throw new IllegalArgumentException("a vertical lane on the play field: " + lane);
                }
            }
            for (double lane : laneYs) {
                if (!(lane > 0 && lane < PlayField.HEIGHT)) {
                    throw new IllegalArgumentException("a horizontal lane on the play field: " + lane);
                }
            }
        }

        /** Without round 44's hatch armour, rail lance and stomp (step F1's walker). */
        public Stride(
                double[] laneXs,
                double[] laneYs,
                List<WaveSpec.Edge> edgeCycle,
                double offSeconds,
                double turnSeconds,
                double[] phaseSpeeds,
                boolean besideLaneFirstPhase,
                boolean turnInPlaceLastPhase) {
            this(
                    laneXs,
                    laneYs,
                    edgeCycle,
                    offSeconds,
                    turnSeconds,
                    phaseSpeeds,
                    besideLaneFirstPhase,
                    turnInPlaceLastPhase,
                    false,
                    Optional.empty(),
                    Optional.empty());
        }

        /** This stride with round 44's hatch armour, rail lance and stomp. */
        public Stride withStrikes(boolean armour, Optional<Lance> withLance, Optional<Stomp> withStomp) {
            return new Stride(
                    laneXs,
                    laneYs,
                    edgeCycle,
                    offSeconds,
                    turnSeconds,
                    phaseSpeeds,
                    besideLaneFirstPhase,
                    turnInPlaceLastPhase,
                    armour,
                    withLance,
                    withStomp);
        }

        /** Its speed in phase {@code phase}, px/s (the last one for later phases). */
        public double speed(int phase) {
            return phaseSpeeds[Math.min(phase, phaseSpeeds.length - 1)];
        }
    }

    /**
     * Round 44 (the user in round 43, W.a: "When both missile hatches are destroyed, the walker is mostly toothless"), a
     * walking boss's back-mounted rail lance ({@link StrideArena}): once every part of {@code afterParts} is destroyed,
     * or from phase {@code fromPhase} on (0-based; the phase count for never), while it walks on the screen it charges
     * for {@code chargeSeconds}, a glowing line from its lance ({@code originBehind} px behind its centre) through
     * where the ship was as the charge began, then fires a line shot along that line for {@code fireSeconds}: {@code
     * damage} to the ship and to Rook once a shot each where the line, {@code width} px wide, touches them. A charge
     * starts every {@code intervalSeconds} (charge start to charge start; {@code lastIntervalSeconds} in its last
     * phase), the first a second after the lance wakes.
     */
    public record Lance(
            List<Integer> afterParts,
            int fromPhase,
            double chargeSeconds,
            double fireSeconds,
            double intervalSeconds,
            double lastIntervalSeconds,
            double width,
            double damage,
            double originBehind) {
        public Lance {
            afterParts = List.copyOf(afterParts);
            if (fromPhase < 0
                    || !(chargeSeconds > 0)
                    || !(fireSeconds > 0)
                    || !(intervalSeconds >= chargeSeconds + fireSeconds)
                    || !(lastIntervalSeconds >= chargeSeconds + fireSeconds)
                    || !(width > 0)
                    || !(damage >= 0)
                    || !Double.isFinite(originBehind)) {
                throw new IllegalArgumentException(
                        "a rail lance has a charge, a shot, an interval holding both, a width and a damage");
            }
        }
    }

    /**
     * Round 44 (the user in round 43, W.e: "stomp shockwave"), a walking boss's stomp ({@link StrideArena}): while it
     * walks on the screen and the ship or Rook is within {@code reach} px of its centre, at most once per {@code
     * cooldownSeconds} (tell start to tell start), its legs brace for {@code tellSeconds} (a ring marks the ground),
     * then it stomps: a ring expands from its centre to {@code radius} px over {@code expandSeconds}, dealing {@code
     * damage} once a stomp to the ship and to Rook when it reaches them.
     */
    public record Stomp(
            double reach,
            double radius,
            double tellSeconds,
            double expandSeconds,
            double cooldownSeconds,
            double damage) {
        public Stomp {
            if (!(reach > 0)
                    || !(radius >= reach)
                    || !(tellSeconds >= 0)
                    || !(expandSeconds > 0)
                    || !(cooldownSeconds >= tellSeconds + expandSeconds)
                    || !(damage >= 0)) {
                throw new IllegalArgumentException(
                        "a stomp has a reach, a ring at least as wide, a tell, an expansion and a cooldown holding both");
            }
        }
    }

    /**
     * M7 part F, a walking boss's front plate (design/enemies/bosses/revenant-walker, Parts): a box of {@code box}
     * (its width across the heading, its height along it, as when it faces straight down) whose centre lies {@code
     * ahead} px from part {@code part}'s centre along its heading. Every shot that meets it glances off ({@link
     * SimEvents.Type#SHOT_GLANCED}) and it stops the Ion Beam (its {@code blocked} contact), on every layer, while
     * the part lives; no HP, not in the bar, no blasts stopped.
     */
    public record Plate(int part, Hitbox box, double ahead) {
        public Plate {
            if (part < 0 || !(box.width() > 0) || !(box.height() > 0)) {
                throw new IllegalArgumentException("a plate covers a part with a box");
            }
        }
    }

    /**
     * M7 part F, a walking boss's rear flame cone (design/enemies/bosses/revenant-walker, Attacks; built on the
     * sweep's beam test with a cone instead of a line): from its flamer {@code originBehind} px behind its centre,
     * {@code length} px long, {@code halfAngleRadians} each side of its axis, which points against its heading. It
     * starts when the ship or Rook is behind it within {@code length + reachBehind} px of the flamer, at most once
     * per {@code cooldownSeconds} (tell start to tell start): a tell of {@code tellSeconds} (in its later phases the
     * axis swivels up to {@code swivelRadians} toward the ship meanwhile), then a burn of {@code burnSeconds} that
     * deals {@code damage} to the ship and to Rook once per burn each; after the burn its vents open for {@code
     * ventSeconds} (its {@link Spot}s with {@code whileOpen}). Round 43 (the walker's rework, K.a of round 42): in its
     * later phases a burn keeps turning toward the ship at up to {@code trackRadiansPerSecond}, within its swivel either
     * side of its back (0: the burn holds the tell's axis).
     */
    public record Cone(
            double length,
            double halfAngleRadians,
            double reachBehind,
            double cooldownSeconds,
            double tellSeconds,
            double burnSeconds,
            double ventSeconds,
            double swivelRadians,
            double damage,
            double originBehind,
            double trackRadiansPerSecond) {
        public Cone {
            if (!(length > 0)
                    || !(halfAngleRadians > 0 && halfAngleRadians < StrictMath.PI / 2)
                    || !(reachBehind >= 0)
                    || !(cooldownSeconds >= 0)
                    || !(tellSeconds >= 0)
                    || !(burnSeconds > 0)
                    || !(ventSeconds >= 0)
                    || !(swivelRadians >= 0)
                    || !(damage >= 0)
                    || !Double.isFinite(originBehind)
                    || !(trackRadiansPerSecond >= 0)) {
                throw new IllegalArgumentException("a flame cone has a length, an angle, a tell, a burn and a damage");
            }
        }

        /** A cone whose burn holds the tell's axis (no tracking). */
        public Cone(
                double length,
                double halfAngleRadians,
                double reachBehind,
                double cooldownSeconds,
                double tellSeconds,
                double burnSeconds,
                double ventSeconds,
                double swivelRadians,
                double damage,
                double originBehind) {
            this(
                    length,
                    halfAngleRadians,
                    reachBehind,
                    cooldownSeconds,
                    tellSeconds,
                    burnSeconds,
                    ventSeconds,
                    swivelRadians,
                    damage,
                    originBehind,
                    0);
        }
    }

    /**
     * M7 part H (design/enemies/bosses/dust-colossus, Breaches; the user's round 43 answers H.7), a breaching arena
     * boss's cycle ({@link BreachArena}): its body stays {@code buried}; a mouth (its vital part) breaches at a {@link
     * BreachArena.Pose point} in turn. A cycle: the <b>tell</b> ({@code tellSeconds}: the bulge at the point), the
     * <b>rise</b> ({@code riseSeconds}; the mouth's layer {@code buried} → {@code ground} at its middle), <b>open</b>
     * ({@code openSeconds}: the gullet's weak spots open, the suction runs, the phase's first attack fires {@code
     * glowSeconds} after the open and then every its interval, the phase's other attacks half an interval after each),
     * the <b>bite</b> ({@code biteTellSeconds}, then the snap: the ship or Rook within {@code biteRadius} px of the
     * mouth's centre takes {@code biteDamage}, once), the <b>dive</b> ({@code diveSeconds}; back to {@code buried} at
     * its middle), then {@code buriedSeconds} from the dive's end to the next rise (the next tell its last {@code
     * tellSeconds}; at least the tell). A side or rear point is warned at its edge from {@code warnSeconds} before
     * its rise; the next point is fixed then (a front or under point at its tell). The first tell starts {@code
     * introSeconds} after the boss arrives.
     *
     * <p>{@code points}: the mouth's centre at {@link BreachArena.Pose#FRONT FRONT}, {@code LEFT}, {@code RIGHT} and
     * {@code REAR}, in that order, in the simulation's frame (px from the left, px up from the bottom edge); {@code
     * clamps}: per point, in the same order, the range a following phase's point takes the ship's coordinate in (x for
     * the front and rear, y for the sides); {@code orders}: per boss phase (the last for later ones) the points in
     * turn and whether they follow the ship. An {@link BreachArena.Pose#UNDER UNDER} point lies on the ship, {@link
     * BreachArena#UNDER_INSET} px inside the edges at most (step H3's eruption replaces its cycle).
     */
    public record Breach(
            double tellSeconds,
            double riseSeconds,
            double openSeconds,
            double biteTellSeconds,
            double diveSeconds,
            double buriedSeconds,
            double warnSeconds,
            List<Point> points,
            List<Clamp> clamps,
            List<Order> orders,
            double biteRadius,
            double biteDamage,
            double introSeconds,
            double glowSeconds) {
        /** The design's lime glow before each ring, s. */
        public static final double GLOW_SECONDS = 0.5;

        public Breach {
            points = List.copyOf(points);
            clamps = List.copyOf(clamps);
            orders = List.copyOf(orders);
            if (!(tellSeconds > 0)
                    || !(riseSeconds > 0)
                    || !(openSeconds > 0)
                    || !(biteTellSeconds > 0)
                    || !(diveSeconds > 0)
                    || !(buriedSeconds >= 0)
                    || !(warnSeconds >= 0)
                    || !(biteRadius >= 0)
                    || !(biteDamage >= 0)
                    || !(introSeconds >= 0)
                    || !(glowSeconds >= 0)) {
                throw new IllegalArgumentException("a breach has a tell, a rise, an open, a bite and a dive");
            }
            if (points.size() != 4 || clamps.size() != 4 || orders.isEmpty()) {
                throw new IllegalArgumentException("a breach has the four edge points, their clamps and an order");
            }
        }

        /** With the design's {@link #GLOW_SECONDS} before each ring. */
        public Breach(
                double tellSeconds,
                double riseSeconds,
                double openSeconds,
                double biteTellSeconds,
                double diveSeconds,
                double buriedSeconds,
                double warnSeconds,
                List<Point> points,
                List<Clamp> clamps,
                List<Order> orders,
                double biteRadius,
                double biteDamage,
                double introSeconds) {
            this(
                    tellSeconds,
                    riseSeconds,
                    openSeconds,
                    biteTellSeconds,
                    diveSeconds,
                    buriedSeconds,
                    warnSeconds,
                    points,
                    clamps,
                    orders,
                    biteRadius,
                    biteDamage,
                    introSeconds,
                    GLOW_SECONDS);
        }

        /** The order of boss phase {@code phase} (the last one for later phases). */
        public Order order(int phase) {
            return orders.get(Math.min(phase, orders.size() - 1));
        }
    }

    /** M7 part H: a breach point's mouth centre, px from the left and px up from the bottom edge. */
    public record Point(double x, double y) {
        public Point {
            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                throw new IllegalArgumentException("a breach point lies somewhere: " + x + ", " + y);
            }
        }
    }

    /** M7 part H: the range a following breach point takes the ship's coordinate in, px. */
    public record Clamp(double min, double max) {
        public Clamp {
            if (!(min <= max)) {
                throw new IllegalArgumentException("a clamp runs from its least to its most: " + min + ", " + max);
            }
        }

        /** {@code value} within the range. */
        public double apply(double value) {
            return Math.clamp(value, min, max);
        }
    }

    /** M7 part H: a boss phase's breach points in turn (repeating) and whether they follow the ship. */
    public record Order(List<BreachArena.Pose> poses, boolean follow) {
        public Order {
            poses = List.copyOf(poses);
            if (poses.isEmpty()) {
                throw new IllegalArgumentException("a breach order has a point");
            }
        }
    }

    /**
     * M7 part H (design/enemies/bosses/dust-colossus, Suction and debris): while a mouth is open (and through its
     * bite's tell) the ship and Rook drift toward its centre at {@code speed} px/s within {@code innerRadius} px,
     * falling off linearly to 0 at {@code outerRadius} px (the Dust Devil's {@link EnemySpec.Pull} fades from its
     * centre; this one has a plateau); never past the centre.
     */
    public record Suction(double speed, double innerRadius, double outerRadius) {
        public Suction {
            if (!(speed >= 0) || !(innerRadius >= 0) || !(outerRadius > innerRadius)) {
                throw new IllegalArgumentException("a suction has a speed and an inner radius inside its outer one");
            }
        }

        /** Its speed at {@code distance} px from the mouth's centre, px/s. */
        public double speedAt(double distance) {
            if (distance <= innerRadius) {
                return speed;
            }
            if (distance >= outerRadius) {
                return 0;
            }
            return speed * (outerRadius - distance) / (outerRadius - innerRadius);
        }
    }

    /**
     * M7 part H, step H3 (design/enemies/bosses/dust-colossus, Phases: phase 2's spine vents): with each breach in a
     * boss phase whose attacks include {@code burst} (an index into its attacks; the vents' aimed burst, its parts the
     * vents) the living vents among {@code parts} rise with the mouth, at {@code distances} px from the mouth's centre
     * back along the body (the ridge's curve that ends at the mouth: step SIM-K, on its spine), {@code buried} → {@code ground} at the rise's middle and
     * back at the dive's middle; a destroyed vent stays dead. While the mouth is open the burst fires from each
     * risen living vent, its first {@link BreachArena#VENT_FIRST_SECONDS} after the open, then every its interval; the
     * phase's ring and fan timing leaves it out. The vents are out of the boss bar.
     */
    public record Vents(List<Integer> parts, List<Double> distances, int burst) {
        public Vents {
            parts = List.copyOf(parts);
            distances = List.copyOf(distances);
            if (parts.isEmpty() || parts.size() != distances.size() || burst < 0) {
                throw new IllegalArgumentException("spine vents have parts, a distance each and a burst");
            }
            for (int i = 0; i < parts.size(); i++) {
                if (parts.get(i) < 0 || !(distances.get(i) >= 0)) {
                    throw new IllegalArgumentException("a spine vent is a part at a distance from the mouth");
                }
            }
        }
    }

    /**
     * M7 part H, step H3 (design/enemies/bosses/dust-colossus, Phases: phase 3): an {@link BreachArena.Pose#UNDER
     * UNDER} breach erupts instead of rising: its point is fixed on the ship ({@code inset} px inside the edges at
     * most) as its marker appears ({@code radius} px; it does not follow), which erupts {@code markerSeconds} later:
     * the mouth is up at once ({@code ground}), the ship or Rook inside the marker takes {@code damage}, once; it
     * stands open {@code openSeconds}, then bites and dives as any breach, then lies {@code buriedSeconds} before the
     * next marker. Step SIM-K (round 44, Q.4: "re-measure, then a short ring if still safe", as the walker's stomp):
     * {@code ring}, a shockwave that expands from the mouth as it erupts (the marker was its tell); empty for none.
     */
    public record Eruption(
            double markerSeconds,
            double radius,
            double damage,
            double inset,
            double openSeconds,
            double buriedSeconds,
            Optional<Shockwave> ring) {
        public Eruption {
            if (!(markerSeconds > 0)
                    || !(radius >= 0)
                    || !(damage >= 0)
                    || !(inset >= 0)
                    || !(openSeconds > 0)
                    || !(buriedSeconds >= 0)) {
                throw new IllegalArgumentException("an eruption has a marker, a radius, a damage and an open time");
            }
        }

        /** Without step SIM-K's ring at the eruption. */
        public Eruption(
                double markerSeconds,
                double radius,
                double damage,
                double inset,
                double openSeconds,
                double buriedSeconds) {
            this(markerSeconds, radius, damage, inset, openSeconds, buriedSeconds, Optional.empty());
        }

        /** Step SIM-K: this eruption with {@code shockwave} as it erupts (empty: none). */
        public Eruption withRing(Optional<Shockwave> shockwave) {
            return new Eruption(markerSeconds, radius, damage, inset, openSeconds, buriedSeconds, shockwave);
        }
    }

    /**
     * Step SIM-K (round 44, Q.4), an eruption's short ring: it expands from the mouth's centre to {@code radius} px over
     * {@code expandSeconds} as the mouth erupts, dealing {@code damage} once to the ship and to Rook when it reaches
     * their centres (not while the ship dashes), as the walker's {@link Stomp}; no bullets, so the no-fire distance
     * does not spare a ship parked on the mouth.
     */
    public record Shockwave(double radius, double expandSeconds, double damage) {
        public Shockwave {
            if (!(radius > 0) || !(expandSeconds > 0) || !(damage >= 0)) {
                throw new IllegalArgumentException("a shockwave has a radius, an expansion time and a damage");
            }
        }
    }

    /**
     * M7 part H, step H3 (design/enemies/bosses/dust-colossus, Suction and debris): while its mouth is open (through
     * its bite's tell) a rock is torn loose every {@code everyByPhase} seconds of the boss phase (the last for later
     * phases; 0: none) at a random point of the play field's edges in its far half from the mouth, {@code minSize} to
     * {@code maxSize} px across ({@link #SIZES} sizes), {@code hp} HP, on the {@code air} layer; it flies straight for
     * the mouth's centre at {@code startSpeed} px/s, gaining {@code acceleration} px/s each second, and is swallowed
     * there ({@link PulledDebris#SWALLOW} px from it), even once the mouth has dived. It stops the player's shots (not
     * the enemy's bullets: the user's round 43 answer H.7c), breaks on the hull dealing {@code contact}, pays nothing
     * and is not an enemy. The suction pulls no pickups (H.7b).
     */
    public record DebrisFlow(
            List<Double> everyByPhase,
            double minSize,
            double maxSize,
            double hp,
            double startSpeed,
            double acceleration,
            double contact) {
        /** How many rock sizes there are, evenly from the least to the most. */
        public static final int SIZES = 5;

        public DebrisFlow {
            everyByPhase = List.copyOf(everyByPhase);
            if (everyByPhase.isEmpty()
                    || everyByPhase.stream().anyMatch(every -> !(every >= 0))
                    || !(minSize > 0)
                    || !(maxSize >= minSize)
                    || !(hp > 0)
                    || !(startSpeed > 0)
                    || !(acceleration >= 0)
                    || !(contact >= 0)) {
                throw new IllegalArgumentException("debris has its times, sizes, HP, speed and contact damage");
            }
        }

        /** The seconds between two rocks in boss phase {@code phase} (the last one for later phases); 0: none. */
        public double every(int phase) {
            return everyByPhase.get(Math.min(phase, everyByPhase.size() - 1));
        }
    }
}
