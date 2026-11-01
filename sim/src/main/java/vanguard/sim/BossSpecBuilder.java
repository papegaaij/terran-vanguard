package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A boss script under construction ({@link BossSpec#builder}): the base script plus the components set on it, built
 * through the canonical constructor, so its checks see them all at once (an arena boss's phases need its arena, a
 * phase's move its poses). Also the mutable copy behind {@link BossSpec}'s {@code withX} builders. Used while the specs
 * are built, never in a step.
 */
public final class BossSpecBuilder {
    private double arriveSeconds;
    private double x;
    private double hoverY;
    private double descentSpeed;
    private double sineAmplitude;
    private double sinePeriod;
    private Layer layer;
    private boolean midBoss;
    private String barName;
    private double parSeconds;
    private List<BossSpec.Chain> chains;
    private List<BossSpec.Attack> attacks;
    private List<BossSpec.Phase> phases;
    private boolean engagesOnArrival;
    private List<Integer> armoured;
    private List<BossSpec.Pose> poses;
    private double deathSeconds;
    private Optional<BossSpec.Arena> arena;
    private double incomingSeconds;
    private Optional<BossSpec.Stride> stride;
    private Optional<BossSpec.Plate> plate;
    private Optional<BossSpec.Cone> cone;
    private Optional<BossSpec.Breach> breach;
    private Optional<BossSpec.Suction> suction;
    private Optional<BossSpec.Vents> vents;
    private Optional<BossSpec.Eruption> eruption;
    private Optional<BossSpec.DebrisFlow> debris;
    private Optional<CoilSpec> coil;
    private Optional<RingSpec> rings;
    private Optional<SquadronSpec> squadron;
    private boolean held;
    private boolean spoofable;
    private Optional<WedgeBody> wedge;
    private List<FedShield> fedShields;
    private List<PartMirror> mirrors;
    private Optional<BatteryFire> batteryFire;
    private List<PhasePlaybook> playbooks;
    private List<PhaseCue> cues;
    private List<PhaseOpening> openings;
    private List<PartRegen> regeneration;
    private Optional<EscapeSpec> escape;

    BossSpecBuilder(
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
            List<BossSpec.Chain> chains,
            List<BossSpec.Attack> attacks,
            List<BossSpec.Phase> phases) {
        this.arriveSeconds = arriveSeconds;
        this.x = x;
        this.hoverY = hoverY;
        this.descentSpeed = descentSpeed;
        this.sineAmplitude = sineAmplitude;
        this.sinePeriod = sinePeriod;
        this.layer = layer;
        this.midBoss = midBoss;
        this.barName = barName;
        this.parSeconds = parSeconds;
        this.chains = chains;
        this.attacks = attacks;
        this.phases = phases;
        engagesOnArrival = false;
        armoured = List.of();
        poses = List.of();
        deathSeconds = 0;
        arena = Optional.empty();
        incomingSeconds = 0;
        stride = Optional.empty();
        plate = Optional.empty();
        cone = Optional.empty();
        breach = Optional.empty();
        suction = Optional.empty();
        vents = Optional.empty();
        eruption = Optional.empty();
        debris = Optional.empty();
        coil = Optional.empty();
        rings = Optional.empty();
        squadron = Optional.empty();
        held = false;
        spoofable = false;
        wedge = Optional.empty();
        fedShields = List.of();
        mirrors = List.of();
        batteryFire = Optional.empty();
        playbooks = List.of();
        cues = List.of();
        openings = List.of();
        regeneration = List.of();
        escape = Optional.empty();
    }

    BossSpecBuilder(BossSpec spec) {
        arriveSeconds = spec.arriveSeconds();
        x = spec.x();
        hoverY = spec.hoverY();
        descentSpeed = spec.descentSpeed();
        sineAmplitude = spec.sineAmplitude();
        sinePeriod = spec.sinePeriod();
        layer = spec.layer();
        midBoss = spec.midBoss();
        barName = spec.barName();
        parSeconds = spec.parSeconds();
        chains = spec.chains();
        attacks = spec.attacks();
        phases = spec.phases();
        engagesOnArrival = spec.engagesOnArrival();
        armoured = spec.armoured();
        poses = spec.poses();
        deathSeconds = spec.deathSeconds();
        arena = spec.arena();
        incomingSeconds = spec.incomingSeconds();
        stride = spec.stride();
        plate = spec.plate();
        cone = spec.cone();
        breach = spec.breach();
        suction = spec.suction();
        vents = spec.vents();
        eruption = spec.eruption();
        debris = spec.debris();
        coil = spec.coil();
        rings = spec.rings();
        squadron = spec.squadron();
        held = spec.held();
        spoofable = spec.spoofable();
        wedge = spec.wedge();
        fedShields = spec.fedShields();
        mirrors = spec.mirrors();
        batteryFire = spec.batteryFire();
        playbooks = spec.playbooks();
        cues = spec.cues();
        openings = spec.openings();
        regeneration = spec.regeneration();
        escape = spec.escape();
    }

    /** Its {@link BossSpec#engagesOnArrival()}. */
    public BossSpecBuilder engagesOnArrival(boolean engagesOnArrival) {
        this.engagesOnArrival = engagesOnArrival;
        return this;
    }

    /** Its {@link BossSpec#armoured()}. */
    public BossSpecBuilder armoured(List<Integer> armoured) {
        this.armoured = armoured;
        return this;
    }

    /** Its {@link BossSpec#poses()}. */
    public BossSpecBuilder poses(List<BossSpec.Pose> poses) {
        this.poses = poses;
        return this;
    }

    /** Its {@link BossSpec#deathSeconds()}. */
    public BossSpecBuilder deathSeconds(double deathSeconds) {
        this.deathSeconds = deathSeconds;
        return this;
    }

    /** Its {@link BossSpec#arena()}. */
    public BossSpecBuilder arena(Optional<BossSpec.Arena> arena) {
        this.arena = arena;
        return this;
    }

    /** Its {@link BossSpec#incomingSeconds()}. */
    public BossSpecBuilder incomingSeconds(double incomingSeconds) {
        this.incomingSeconds = incomingSeconds;
        return this;
    }

    /** Its {@link BossSpec#stride()}. */
    public BossSpecBuilder stride(Optional<BossSpec.Stride> stride) {
        this.stride = stride;
        return this;
    }

    /** Its {@link BossSpec#plate()}. */
    public BossSpecBuilder plate(Optional<BossSpec.Plate> plate) {
        this.plate = plate;
        return this;
    }

    /** Its {@link BossSpec#cone()}. */
    public BossSpecBuilder cone(Optional<BossSpec.Cone> cone) {
        this.cone = cone;
        return this;
    }

    /** Its {@link BossSpec#breach()}. */
    public BossSpecBuilder breach(Optional<BossSpec.Breach> breach) {
        this.breach = breach;
        return this;
    }

    /** Its {@link BossSpec#suction()}. */
    public BossSpecBuilder suction(Optional<BossSpec.Suction> suction) {
        this.suction = suction;
        return this;
    }

    /** Its {@link BossSpec#vents()}. */
    public BossSpecBuilder vents(Optional<BossSpec.Vents> vents) {
        this.vents = vents;
        return this;
    }

    /** Its {@link BossSpec#eruption()}. */
    public BossSpecBuilder eruption(Optional<BossSpec.Eruption> eruption) {
        this.eruption = eruption;
        return this;
    }

    /** Its {@link BossSpec#debris()}. */
    public BossSpecBuilder debris(Optional<BossSpec.DebrisFlow> debris) {
        this.debris = debris;
        return this;
    }

    /** Its {@link BossSpec#coil()}. */
    public BossSpecBuilder coil(Optional<CoilSpec> coil) {
        this.coil = coil;
        return this;
    }

    /** Its {@link BossSpec#rings()}. */
    public BossSpecBuilder rings(Optional<RingSpec> rings) {
        this.rings = rings;
        return this;
    }

    /** Its {@link BossSpec#squadron()}. */
    public BossSpecBuilder squadron(Optional<SquadronSpec> squadron) {
        this.squadron = squadron;
        return this;
    }

    /** M10 part G, step G1b: its {@link BossSpec#held()} (a held set piece: no bar, warning or boss music). */
    public BossSpecBuilder held(boolean held) {
        this.held = held;
        return this;
    }

    /** M10 part G, step G1b: its {@link BossSpec#spoofable()} (a held set piece's guns the IFF spoofer delays). */
    public BossSpecBuilder spoofable(boolean spoofable) {
        this.spoofable = spoofable;
        return this;
    }

    /** The boss script, checked. */
    public BossSpecBuilder wedge(Optional<WedgeBody> wedge) {
        this.wedge = wedge;
        return this;
    }

    public BossSpecBuilder fedShields(List<FedShield> fedShields) {
        this.fedShields = fedShields;
        return this;
    }

    public BossSpecBuilder mirrors(List<PartMirror> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public BossSpecBuilder batteryFire(Optional<BatteryFire> batteryFire) {
        this.batteryFire = batteryFire;
        return this;
    }

    public BossSpecBuilder playbooks(List<PhasePlaybook> playbooks) {
        this.playbooks = playbooks;
        return this;
    }

    public BossSpecBuilder cues(List<PhaseCue> cues) {
        this.cues = cues;
        return this;
    }

    /** M10 part H, step H1b: how its later phases open (the eruption). */
    public BossSpecBuilder openings(List<PhaseOpening> openings) {
        this.openings = openings;
        return this;
    }

    /** M10 part H, step H1b: its regenerating parts. */
    public BossSpecBuilder regeneration(List<PartRegen> regeneration) {
        this.regeneration = regeneration;
        return this;
    }

    /** M10 part H, step H1b: its end without a death (the hull breaks, the lifeboat escapes). */
    public BossSpecBuilder escape(Optional<EscapeSpec> escape) {
        this.escape = escape;
        return this;
    }

    public BossSpec build() {
        return new BossSpec(
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
                incomingSeconds,
                stride,
                plate,
                cone,
                breach,
                suction,
                vents,
                eruption,
                debris,
                coil,
                rings,
                squadron,
                held,
                spoofable,
                wedge,
                fedShields,
                mirrors,
                batteryFire,
                playbooks,
                cues,
                openings,
                regeneration,
                escape);
    }
}
