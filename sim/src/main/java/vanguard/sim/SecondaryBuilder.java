package vanguard.sim;

import java.util.List;

/**
 * M10 part C, step C1 (RF9b's pattern, {@link WaveSpecBuilder}): a secondary objective under construction ({@link
 * Secondary#builder}), every component at its default (no objective: {@link Secondary#NONE}'s, a kill ratio of 0 for
 * no credits, the bosses counted), built through the canonical constructor so its checks see them all at once. Also
 * the mutable copy behind {@link Secondary}'s {@code withX} copies ({@link Secondary#toBuilder}). Used while the level
 * script is built, never in a step.
 */
public final class SecondaryBuilder {
    private double killRatio = 0;
    private int credits = 0;
    private List<String> groups = List.of();
    private String escapes = "";
    private List<String> killAll = List.of();
    private String label = "";
    private String partsOf = "";
    private List<Integer> parts = List.of();
    private int beforePhase = -1;
    private String tag = "";
    private boolean afloat = false;
    private boolean sondes = false;
    private double integrity = 0;
    private boolean links = false;
    private boolean untouched = false;
    private boolean standing = false;
    private int escapesAllowed = 0;
    private boolean countsBosses = true;
    private String spared = "";
    private boolean objects = false;
    private int saved = 0;
    private String enemy = "";
    private int files = 0;
    private boolean untilWarning = false;

    SecondaryBuilder() {}

    SecondaryBuilder(Secondary secondary) {
        killRatio = secondary.killRatio();
        credits = secondary.credits();
        groups = secondary.groups();
        escapes = secondary.escapes();
        killAll = secondary.killAll();
        label = secondary.label();
        partsOf = secondary.partsOf();
        parts = secondary.parts();
        beforePhase = secondary.beforePhase();
        tag = secondary.tag();
        afloat = secondary.afloat();
        sondes = secondary.sondes();
        integrity = secondary.integrity();
        links = secondary.links();
        untouched = secondary.untouched();
        standing = secondary.standing();
        escapesAllowed = secondary.escapesAllowed();
        countsBosses = secondary.countsBosses();
        spared = secondary.spared();
        objects = secondary.objects();
        saved = secondary.saved();
        enemy = secondary.enemy();
        files = secondary.files();
        untilWarning = secondary.untilWarning();
    }

    /** Its {@link Secondary#killRatio()}. */
    public SecondaryBuilder killRatio(double killRatio) {
        this.killRatio = killRatio;
        return this;
    }

    /** Its {@link Secondary#credits()}. */
    public SecondaryBuilder credits(int credits) {
        this.credits = credits;
        return this;
    }

    /** Its {@link Secondary#groups()}. */
    public SecondaryBuilder groups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    /** Its {@link Secondary#escapes()}. */
    public SecondaryBuilder escapes(String escapes) {
        this.escapes = escapes;
        return this;
    }

    /** Its {@link Secondary#killAll()}. */
    public SecondaryBuilder killAll(List<String> killAll) {
        this.killAll = killAll;
        return this;
    }

    /** Its {@link Secondary#label()}. */
    public SecondaryBuilder label(String label) {
        this.label = label;
        return this;
    }

    /** Its {@link Secondary#partsOf()}. */
    public SecondaryBuilder partsOf(String partsOf) {
        this.partsOf = partsOf;
        return this;
    }

    /** Its {@link Secondary#parts()}. */
    public SecondaryBuilder parts(List<Integer> parts) {
        this.parts = parts;
        return this;
    }

    /** Its {@link Secondary#beforePhase()}. */
    public SecondaryBuilder beforePhase(int beforePhase) {
        this.beforePhase = beforePhase;
        return this;
    }

    /** Its {@link Secondary#tag()}. */
    public SecondaryBuilder tag(String tag) {
        this.tag = tag;
        return this;
    }

    /** Its {@link Secondary#afloat()}. */
    public SecondaryBuilder afloat(boolean afloat) {
        this.afloat = afloat;
        return this;
    }

    /** Its {@link Secondary#sondes()}. */
    public SecondaryBuilder sondes(boolean sondes) {
        this.sondes = sondes;
        return this;
    }

    /** Its {@link Secondary#integrity()}. */
    public SecondaryBuilder integrity(double integrity) {
        this.integrity = integrity;
        return this;
    }

    /** Its {@link Secondary#links()}. */
    public SecondaryBuilder links(boolean links) {
        this.links = links;
        return this;
    }

    /** Its {@link Secondary#untouched()}. */
    public SecondaryBuilder untouched(boolean untouched) {
        this.untouched = untouched;
        return this;
    }

    /** Its {@link Secondary#standing()}. */
    public SecondaryBuilder standing(boolean standing) {
        this.standing = standing;
        return this;
    }

    /** Its {@link Secondary#escapesAllowed()}. */
    public SecondaryBuilder escapesAllowed(int escapesAllowed) {
        this.escapesAllowed = escapesAllowed;
        return this;
    }

    /** Its {@link Secondary#countsBosses()}. */
    public SecondaryBuilder countsBosses(boolean countsBosses) {
        this.countsBosses = countsBosses;
        return this;
    }

    /** Its {@link Secondary#spared()}. */
    public SecondaryBuilder spared(String spared) {
        this.spared = spared;
        return this;
    }

    /** Its {@link Secondary#objects()}. */
    public SecondaryBuilder objects(boolean objects) {
        this.objects = objects;
        return this;
    }

    /** Its {@link Secondary#saved()}. */
    public SecondaryBuilder saved(int saved) {
        this.saved = saved;
        return this;
    }

    /** M10 part D: its {@link Secondary#enemy()} (a kill ratio scoped to one enemy's slug). */
    public SecondaryBuilder enemy(String enemy) {
        this.enemy = enemy;
        return this;
    }

    /** M10 part G, step G1a: its {@link Secondary#files()} (the archive cores' uploads, Level 41's {@code FILES}). */
    public SecondaryBuilder files(int files) {
        this.files = files;
        return this;
    }

    /** M11 part G, step G1: its {@link Secondary#untilWarning()} (Level 49's kill ratio by the first warning). */
    public SecondaryBuilder untilWarning(boolean untilWarning) {
        this.untilWarning = untilWarning;
        return this;
    }

    /** The secondary objective, checked as a whole. */
    public Secondary build() {
        return new Secondary(
                killRatio,
                credits,
                groups,
                escapes,
                killAll,
                label,
                partsOf,
                parts,
                beforePhase,
                tag,
                afloat,
                sondes,
                integrity,
                links,
                untouched,
                standing,
                escapesAllowed,
                countsBosses,
                spared,
                objects,
                saved,
                enemy,
                files,
                untilWarning);
    }
}
