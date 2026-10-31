package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * A wave group under construction ({@link WaveSpec#builder}): its base (time, formation, unit, count, entry and edge)
 * plus the components set on it, built through the canonical constructor, so its checks see them all at once (a leader
 * needs a wing that scatters, rocks need riders, mine lines a mine layer from the rear). Also the mutable copy behind
 * {@link WaveSpec}'s {@code withX} builders ({@link WaveSpec#toBuilder}). Used while the level script is built, never
 * in a step.
 */
public final class WaveSpecBuilder {
    private double t;
    private WaveSpec.Formation formation;
    private EnemySpec enemy;
    private int count;
    private WaveSpec.Entry entry;
    private WaveSpec.Edge edge;
    private Optional<Double> holdSeconds;
    private Optional<Double> warningSeconds;
    private int breakGroup;
    private Optional<Double> speed;
    private Optional<Double> intervalSeconds;
    private List<WaveSpec.Carried> carried;
    private Optional<WaveSpec.At> at;
    private List<List<WaveSpec.At>> paths;
    private Optional<WaveSpec.LoopBack> loopBack;
    private String tag;
    private Optional<WaveSpec.Field> field;
    private boolean clockwise;
    private List<List<WaveSpec.At>> lanes;
    private Optional<Double> pairSeconds;
    private Optional<WaveSpec.Pursuit> pursuit;
    private Optional<WaveSpec.Mount> mount;
    private Optional<EnemySpec> leader;
    private Optional<WaveSpec.OnRocks> onRocks;
    private Optional<WaveSpec.MineLines> mineLines;
    private Optional<Branch> branch;
    private String hunts;

    WaveSpecBuilder(
            double t,
            WaveSpec.Formation formation,
            EnemySpec enemy,
            int count,
            WaveSpec.Entry entry,
            WaveSpec.Edge edge) {
        this.t = t;
        this.formation = formation;
        this.enemy = enemy;
        this.count = count;
        this.entry = entry;
        this.edge = edge;
        holdSeconds = Optional.empty();
        warningSeconds = Optional.empty();
        breakGroup = 1;
        speed = Optional.empty();
        intervalSeconds = Optional.empty();
        carried = List.of();
        at = Optional.empty();
        paths = List.of();
        loopBack = Optional.empty();
        tag = "";
        field = Optional.empty();
        clockwise = false;
        lanes = List.of();
        pairSeconds = Optional.empty();
        pursuit = Optional.empty();
        mount = Optional.empty();
        leader = Optional.empty();
        onRocks = Optional.empty();
        mineLines = Optional.empty();
        branch = Optional.empty();
        hunts = "";
    }

    WaveSpecBuilder(WaveSpec spec) {
        t = spec.t();
        formation = spec.formation();
        enemy = spec.enemy();
        count = spec.count();
        entry = spec.entry();
        edge = spec.edge();
        holdSeconds = spec.holdSeconds();
        warningSeconds = spec.warningSeconds();
        breakGroup = spec.breakGroup();
        speed = spec.speed();
        intervalSeconds = spec.intervalSeconds();
        carried = spec.carried();
        at = spec.at();
        paths = spec.paths();
        loopBack = spec.loopBack();
        tag = spec.tag();
        field = spec.field();
        clockwise = spec.clockwise();
        lanes = spec.lanes();
        pairSeconds = spec.pairSeconds();
        pursuit = spec.pursuit();
        mount = spec.mount();
        leader = spec.leader();
        onRocks = spec.onRocks();
        mineLines = spec.mineLines();
        branch = spec.branch();
        hunts = spec.hunts();
    }

    /** Its {@link WaveSpec#t()}. */
    public WaveSpecBuilder t(double t) {
        this.t = t;
        return this;
    }

    /** Its {@link WaveSpec#formation()}. */
    public WaveSpecBuilder formation(WaveSpec.Formation formation) {
        this.formation = formation;
        return this;
    }

    /** Its {@link WaveSpec#enemy()}. */
    public WaveSpecBuilder enemy(EnemySpec enemy) {
        this.enemy = enemy;
        return this;
    }

    /** Its {@link WaveSpec#count()}. */
    public WaveSpecBuilder count(int count) {
        this.count = count;
        return this;
    }

    /** Its {@link WaveSpec#entry()}. */
    public WaveSpecBuilder entry(WaveSpec.Entry entry) {
        this.entry = entry;
        return this;
    }

    /** Its {@link WaveSpec#edge()}. */
    public WaveSpecBuilder edge(WaveSpec.Edge edge) {
        this.edge = edge;
        return this;
    }

    /** Its {@link WaveSpec#holdSeconds()}. */
    public WaveSpecBuilder holdSeconds(Optional<Double> holdSeconds) {
        this.holdSeconds = holdSeconds;
        return this;
    }

    /** Its {@link WaveSpec#warningSeconds()}. */
    public WaveSpecBuilder warningSeconds(Optional<Double> warningSeconds) {
        this.warningSeconds = warningSeconds;
        return this;
    }

    /** Its {@link WaveSpec#breakGroup()}. */
    public WaveSpecBuilder breakGroup(int breakGroup) {
        this.breakGroup = breakGroup;
        return this;
    }

    /** Its {@link WaveSpec#speed()}. */
    public WaveSpecBuilder speed(Optional<Double> speed) {
        this.speed = speed;
        return this;
    }

    /** Its {@link WaveSpec#intervalSeconds()}. */
    public WaveSpecBuilder intervalSeconds(Optional<Double> intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
        return this;
    }

    /** Its {@link WaveSpec#carried()}. */
    public WaveSpecBuilder carried(List<WaveSpec.Carried> carried) {
        this.carried = carried;
        return this;
    }

    /** Its {@link WaveSpec#at()}. */
    public WaveSpecBuilder at(Optional<WaveSpec.At> at) {
        this.at = at;
        return this;
    }

    /** Its {@link WaveSpec#paths()}. */
    public WaveSpecBuilder paths(List<List<WaveSpec.At>> paths) {
        this.paths = paths;
        return this;
    }

    /** Its {@link WaveSpec#loopBack()}. */
    public WaveSpecBuilder loopBack(Optional<WaveSpec.LoopBack> loopBack) {
        this.loopBack = loopBack;
        return this;
    }

    /** Its {@link WaveSpec#tag()}. */
    public WaveSpecBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    /** Its {@link WaveSpec#field()}. */
    public WaveSpecBuilder field(Optional<WaveSpec.Field> field) {
        this.field = field;
        return this;
    }

    /** Its {@link WaveSpec#clockwise()}. */
    public WaveSpecBuilder clockwise(boolean clockwise) {
        this.clockwise = clockwise;
        return this;
    }

    /** Its {@link WaveSpec#lanes()}. */
    public WaveSpecBuilder lanes(List<List<WaveSpec.At>> lanes) {
        this.lanes = lanes;
        return this;
    }

    /** Its {@link WaveSpec#pairSeconds()}. */
    public WaveSpecBuilder pairSeconds(Optional<Double> pairSeconds) {
        this.pairSeconds = pairSeconds;
        return this;
    }

    /** Its {@link WaveSpec#pursuit()}. */
    public WaveSpecBuilder pursuit(Optional<WaveSpec.Pursuit> pursuit) {
        this.pursuit = pursuit;
        return this;
    }

    /** Its {@link WaveSpec#mount()}. */
    public WaveSpecBuilder mount(Optional<WaveSpec.Mount> mount) {
        this.mount = mount;
        return this;
    }

    /** Its {@link WaveSpec#leader()}. */
    public WaveSpecBuilder leader(Optional<EnemySpec> leader) {
        this.leader = leader;
        return this;
    }

    /** Its {@link WaveSpec#onRocks()}. */
    public WaveSpecBuilder onRocks(Optional<WaveSpec.OnRocks> onRocks) {
        this.onRocks = onRocks;
        return this;
    }

    /** Its {@link WaveSpec#mineLines()}. */
    public WaveSpecBuilder mineLines(Optional<WaveSpec.MineLines> mineLines) {
        this.mineLines = mineLines;
        return this;
    }

    /** Its {@link WaveSpec#branch()}. */
    public WaveSpecBuilder branch(Optional<Branch> branch) {
        this.branch = branch;
        return this;
    }

    /** Its {@link WaveSpec#hunts()}: the tag of the prey wave it hunts (M10 part A); "" for none. */
    public WaveSpecBuilder hunts(String preyTag) {
        this.hunts = preyTag;
        return this;
    }

    /** The wave group, checked. */
    public WaveSpec build() {
        return new WaveSpec(
                t,
                formation,
                enemy,
                count,
                entry,
                edge,
                holdSeconds,
                warningSeconds,
                breakGroup,
                speed,
                intervalSeconds,
                carried,
                at,
                paths,
                loopBack,
                tag,
                field,
                clockwise,
                lanes,
                pairSeconds,
                pursuit,
                mount,
                leader,
                onRocks,
                mineLines,
                branch,
                hunts);
    }
}
