package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * What counts towards the level's objectives in an attempt: the kills of every enemy kind, the
 * secrets found and the secondary objective: destroy a share of all enemies (design/systems/scoring),
 * clear groups of ground units (Level 02's docks), let none of an enemy get away, destroy every unit
 * of some enemies, or shoot off a boss's parts in time (Level 07). A group's outcome is decided when the last
 * of its units is gone: cleared if every one was destroyed, lost if any left the screen alive. The
 * primary objective, reaching the end of the scroll, is the {@link Sortie}'s.
 */
final class Objectives {
    /** A group's state: still open, cleared, lost. */
    static final int OPEN = 0;

    static final int CLEARED = 1;
    static final int LOST = 2;

    private final boolean byGroups;
    private final Secondary secondary;
    private final int escapesTotal;
    private int escapesDestroyed;
    /** M7 part F: the units of an escapes objective that got away (counted only with escapes allowed). */
    private int escapesLost;

    private boolean escapesFailed;
    private final int[] killsByKind;
    private int requiredKills;
    /** The level's units as planned, for the kill ratio (M7 part G: less the pursuits' refills left out). */
    private final int plannedUnits;

    private final int[] groupUnits;
    private final int[] groupDestroyed;
    private final int[] groupEscaped;
    private final int[] groupState;
    private int secretsFound;
    private int groupsCleared;
    private int groupsLost;
    private boolean secondaryMet;
    /** M5 part E: whether a naval convoy unit sank, which fails an afloat objective. */
    private boolean afloatFailed;
    /** M5 part G: whether the defended ally dropped below an integrity objective's share. */
    private boolean integrityFailed;
    /**
     * M7 part D: whether a ground ally was hit, which fails an untouched objective; round 41: or broken, which
     * fails a standing one.
     */
    private boolean untouchedFailed;

    /**
     * @param groups the groups the ground units belong to: the secondary's or the destroy-targets
     *     primary's ({@link LevelScript#groups()})
     * @param kinds the number of distinct enemies in the level
     * @param units every enemy the level sends
     * @param escapesTotal the units of the enemies of an escapes or kill-all objective the level
     *     sends, or the boss parts of a parts objective
     */
    Objectives(Secondary secondary, int groups, int kinds, int units, List<GroundUnit> groundUnits, int escapesTotal) {
        this.secondary = secondary;
        byGroups = secondary.byGroups();
        this.escapesTotal = escapesTotal;
        killsByKind = new int[kinds];
        plannedUnits = units;
        requiredKills = required(units);
        groupUnits = new int[groups];
        groupDestroyed = new int[groups];
        groupEscaped = new int[groups];
        groupState = new int[groups];
        for (GroundUnit unit : groundUnits) {
            if (unit.group() >= 0) {
                groupUnits[unit.group()]++;
            }
        }
    }

    /** Takes over {@code other}'s tallies (a boss checkpoint); both are of the same level. */
    void copyFrom(Objectives other) {
        System.arraycopy(other.killsByKind, 0, killsByKind, 0, killsByKind.length);
        System.arraycopy(other.groupDestroyed, 0, groupDestroyed, 0, groupDestroyed.length);
        System.arraycopy(other.groupEscaped, 0, groupEscaped, 0, groupEscaped.length);
        System.arraycopy(other.groupState, 0, groupState, 0, groupState.length);
        secretsFound = other.secretsFound;
        groupsCleared = other.groupsCleared;
        groupsLost = other.groupsLost;
        escapesDestroyed = other.escapesDestroyed;
        escapesLost = other.escapesLost;
        escapesFailed = other.escapesFailed;
        secondaryMet = other.secondaryMet;
        afloatFailed = other.afloatFailed;
        integrityFailed = other.integrityFailed;
        untouchedFailed = other.untouchedFailed;
    }

    /** Back to the level start. */
    void reset() {
        Arrays.fill(killsByKind, 0);
        Arrays.fill(groupDestroyed, 0);
        Arrays.fill(groupEscaped, 0);
        Arrays.fill(groupState, OPEN);
        secretsFound = 0;
        groupsCleared = 0;
        groupsLost = 0;
        escapesDestroyed = 0;
        escapesLost = 0;
        escapesFailed = false;
        secondaryMet = false;
        afloatFailed = false;
        integrityFailed = false;
        untouchedFailed = false;
    }

    /**
     * M7 part D (design/campaign Level 17, Q.25c): a ground ally took a hit; returns whether that failed an
     * untouched objective (the first hit, before the last of them left the screen).
     */
    boolean allyHit() {
        if (!secondary.untouched() || untouchedFailed || secondaryMet) {
            return false;
        }
        untouchedFailed = true;
        return true;
    }

    /**
     * Round 41 (design/campaign Level 17, "No dome lost"): a ground ally broke; returns whether that failed a
     * standing objective (the first one broken, before the last of them left the screen).
     */
    boolean allyLost() {
        if (!secondary.standing() || untouchedFailed || secondaryMet) {
            return false;
        }
        untouchedFailed = true;
        return true;
    }

    /**
     * M7 part D: the last ground ally left the bottom edge; returns whether that met an untouched objective (none
     * hit) or (round 41) a standing one (none broken).
     */
    boolean alliesPassed() {
        if (!secondary.byGroundAllies() || untouchedFailed || secondaryMet) {
            return false;
        }
        secondaryMet = true;
        return true;
    }

    /** M7 part D: whether an untouched objective failed (a ground ally was hit); round 41: or a standing one (one broke). */
    boolean untouchedFailed() {
        return untouchedFailed;
    }

    /**
     * M5 part G (design/campaign Level 13, user decision G2 = a): the defended ally took a hit and has
     * {@code share} of its integrity left; returns whether that failed an integrity objective (the first
     * drop below its share: the ally does not regenerate, so it cannot recover).
     */
    boolean integrityLeft(double share) {
        if (!secondary.byIntegrity() || integrityFailed || secondaryMet || share >= secondary.integrity()) {
            return false;
        }
        integrityFailed = true;
        return true;
    }

    /** M5 part G: the defend's synchronisation completed; returns whether that met an integrity objective. */
    boolean defended() {
        if (!secondary.byIntegrity() || integrityFailed || secondaryMet) {
            return false;
        }
        secondaryMet = true;
        return true;
    }

    /** M5 part G: whether an integrity objective failed (the ally dropped below its share). */
    boolean integrityFailed() {
        return integrityFailed;
    }

    /**
     * M5 part E (user decision E8 = a): a naval convoy unit sank; returns whether that failed an
     * afloat objective (the first sinking, before the boss's death).
     */
    boolean sunk() {
        if (!secondary.afloat() || afloatFailed || secondaryMet) {
            return false;
        }
        afloatFailed = true;
        return true;
    }

    /** M5 part E: the level boss died; returns whether that met an afloat objective (no unit sank). */
    boolean bossDownAfloat() {
        if (!secondary.afloat() || afloatFailed || secondaryMet) {
            return false;
        }
        secondaryMet = true;
        return true;
    }

    /** M5 part E: whether an afloat objective failed (a unit sank). */
    boolean afloatFailed() {
        return afloatFailed;
    }

    /**
     * A unit of {@code slug} of a wave tagged {@code tag} ("" for none) was destroyed; returns
     * whether that met an escapes objective.
     */
    boolean escapeDestroyed(String slug, String tag) {
        if (!secondary.counts(slug, tag) || escapesFailed) {
            return false;
        }
        escapesDestroyed++;
        if (escapesDestroyed >= escapesNeeded()) {
            return meet();
        }
        return false;
    }

    /**
     * The units of an escapes objective it needs destroyed: all of them, or with escapes allowed all but those (round
     * 42: met as soon as it can no longer fail, so Level 19's GREY TANKS turns DONE at the twelfth, not when the last
     * two are decided).
     */
    private int escapesNeeded() {
        return escapesTotal - secondary.escapesAllowed();
    }

    private boolean meet() {
        if (secondaryMet) {
            return false;
        }
        secondaryMet = true;
        return true;
    }

    /** A part of a parts objective was shot off; returns whether that met the objective. */
    boolean partShotOff() {
        if (!secondary.byParts() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesDestroyed++;
        if (escapesDestroyed == escapesTotal) {
            secondaryMet = true;
            return true;
        }
        return false;
    }

    /** M5 part F: a weather sonde was collected; returns whether that met a collect: sondes objective. */
    boolean sondeCollected() {
        if (!secondary.sondes() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesDestroyed++;
        if (escapesDestroyed == escapesTotal) {
            secondaryMet = true;
            return true;
        }
        return false;
    }

    /** M5 part F: a weather sonde left the bottom edge uncollected; returns whether that failed the objective. */
    boolean sondeLost() {
        if (!secondary.sondes() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesFailed = true;
        return true;
    }

    /**
     * M7 part C (design/campaign Level 16, Q.16a): a gate's tendril was broken; returns whether that met a
     * links objective (the last of them).
     */
    boolean linkBroken() {
        if (!secondary.links() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesDestroyed++;
        if (escapesDestroyed == escapesTotal) {
            secondaryMet = true;
            return true;
        }
        return false;
    }

    /** M7 part C: a tendril left the screen unbroken; returns whether that failed a links objective. */
    boolean linkLost() {
        if (!secondary.links() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesFailed = true;
        return true;
    }

    /** A parts objective's phase ended (or its boss died) with a part alive; returns whether that failed the objective. */
    boolean partsSurvived() {
        if (!secondary.byParts() || escapesFailed || secondaryMet) {
            return false;
        }
        escapesFailed = true;
        return true;
    }

    /** A unit of {@code slug} of a wave tagged {@code tag} ("" for none) got away; returns whether that failed an escapes objective. */
    boolean escapeLost(String slug, String tag) {
        if (!secondary.counts(slug, tag) || escapesFailed || secondaryMet) {
            return false;
        }
        if (secondary.escapesAllowed() > 0) {
            // M7 part F ("12 of 14"): the allowed ones get away without failing it; the one past them fails it.
            escapesLost++;
            if (escapesLost <= secondary.escapesAllowed()) {
                return false;
            }
        }
        escapesFailed = true;
        return true;
    }

    /**
     * M7 part F: the level is complete; returns whether an escapes objective with escapes allowed is met then (no more
     * than the allowed units got away, the rest undecided or destroyed). Asked once, at the end.
     */
    boolean escapesAtEnd() {
        if (secondary.escapesAllowed() == 0 || escapesFailed || secondaryMet) {
            return false;
        }
        return meet();
    }

    /** M7 part F: the units of an escapes objective that got away in this attempt (with escapes allowed; else 0). */
    int escapesLost() {
        return escapesLost;
    }

    int escapesDestroyed() {
        return escapesDestroyed;
    }

    int escapesTotal() {
        return escapesTotal;
    }

    boolean escapesFailed() {
        return escapesFailed;
    }

    /** A unit of group {@code g} was destroyed; returns the group's state after it. */
    int groupUnitDestroyed(int g) {
        groupDestroyed[g]++;
        return decide(g);
    }

    /** A unit of group {@code g} left the screen alive; returns the group's state after it. */
    int groupUnitEscaped(int g) {
        groupEscaped[g]++;
        return decide(g);
    }

    private int decide(int g) {
        if (groupState[g] == OPEN && groupDestroyed[g] + groupEscaped[g] == groupUnits[g]) {
            if (groupEscaped[g] == 0) {
                groupState[g] = CLEARED;
                groupsCleared++;
                secondaryMet |= byGroups && groupsCleared == groupState.length;
            } else {
                groupState[g] = LOST;
                groupsLost++;
            }
        }
        return groupState[g];
    }

    /** The groups cleared in this attempt. */
    int groupsCleared() {
        return groupsCleared;
    }

    /** The groups lost in this attempt. */
    int groupsLost() {
        return groupsLost;
    }

    int groupCount() {
        return groupState.length;
    }

    /** M5 part H: the ground units in group {@code g} (the tracker's {@code ROOTS n / 8}). */
    int groupUnits(int g) {
        return groupUnits[g];
    }

    /** M5 part H: the units of group {@code g} destroyed in this attempt. */
    int groupDestroyed(int g) {
        return groupDestroyed[g];
    }

    int groupState(int g) {
        return groupState[g];
    }

    /** Counts a kill of an enemy of {@code kind}; returns the kills of that kind in this attempt. */
    int kill(int kind) {
        return ++killsByKind[kind];
    }

    /**
     * Whether {@code kills} in all meet a kill-ratio secondary, the first time they do. It is asked
     * once, when the level is complete: the share is judged at the end, never mid-level, so its
     * credits and line come with the level-end lines (design/systems/scoring). A level that sends
     * no enemies has no share to meet.
     */
    boolean meetsKillRatio(int kills) {
        if (byGroups || secondary.byEscapes() || secondaryMet || requiredKills == 0 || kills < requiredKills) {
            return false;
        }
        secondaryMet = true;
        return true;
    }

    void secretFound() {
        secretsFound++;
    }

    int secretsFound() {
        return secretsFound;
    }

    boolean secondaryMet() {
        return secondaryMet;
    }

    int requiredKills() {
        return requiredKills;
    }

    private int required(int units) {
        return byGroups || secondary.byEscapes() ? 0 : (int) Math.ceil(secondary.killRatio() * units - 1e-9);
    }

    /**
     * M7 part G (design/campaign Level 20, The pursuit swarm; the gap check's default, pending round 43): {@code
     * skipped} planned units never entered (the pursuits' refill motes with no room in their flock): the kill ratio
     * counts the level's units without them.
     */
    void unitsSkipped(int skipped) {
        requiredKills = required(plannedUnits - skipped);
    }

    void addKillsTo(StateHash hash) {
        for (int kills : killsByKind) {
            hash.add(kills);
        }
        for (int g = 0; g < groupState.length; g++) {
            hash.add(groupDestroyed[g]).add(groupEscaped[g]).add(groupState[g]);
        }
        hash.add(escapesDestroyed).add(escapesFailed ? 1 : 0);
        if (secondary.afloat()) {
            // M5 part E: only with an afloat objective, so the other levels hash as before.
            hash.add(afloatFailed ? 1 : 0);
        }
        if (secondary.byIntegrity()) {
            // M5 part G: only with an integrity objective, so the other levels hash as before.
            hash.add(integrityFailed ? 1 : 0);
        }
        if (secondary.byGroundAllies()) {
            // M7 part D: only with an untouched (round 41: or standing) objective, so the other levels hash as before.
            hash.add(untouchedFailed ? 1 : 0);
        }
        if (secondary.escapesAllowed() > 0) {
            // M7 part F: only with escapes allowed, so the other levels hash as before.
            hash.add(escapesLost);
        }
    }
}
