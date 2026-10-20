package vanguard.sim;

import java.util.List;

/**
 * The secondary objective: destroy at least {@code killRatio} of all enemies for
 * {@code credits} (before the credit factor), or, with {@code groups}, clear every ground
 * unit of a group (Level 02's docks) before the last of them leaves the screen, for
 * {@code credits} per group; the objective is met when every group is cleared.
 *
 * @param groups the groups' names (the ground units' {@link GroundUnit#group()} indexes them); empty for a kill ratio
 * @param escapes the slug of the enemy none of which may leave the screen alive ("nothing gets
 *     through", Level 03): met when all are destroyed, failed when one gets away; empty for none
 * @param killAll the enemies every unit of which must be destroyed (Level 05's "Scorched
 *     crater"), met and failed as {@code escapes}; empty for none
 * @param label the tracker's label of a kill-all or parts objective ("NEST")
 * @param partsOf part G: the set piece ("brood-carrier") whose {@code parts} must all be shot off
 *     before its boss phase {@code beforePhase} ends (Level 07's "Gut the bays"): met when the
 *     last of them is shot off, failed when the boss enters a later phase, or dies, with one of
 *     them alive; empty for none
 * @param parts indexes into that set piece's parts
 * @param beforePhase the index of the boss phase they must die in or before
 * @param tag M5 part C (user decision D6 = a): scopes {@code escapes} to the units of the waves
 *     with this tag (Level 09's {@code bridge} packs); empty for every unit of the enemy
 * @param afloat M5 part E (user decision E8 = a, Level 11's "Convoy afloat"): met at the level
 *     boss's death with every naval convoy unit that can be damaged afloat, failed at the first
 *     sinking; {@code label} is the tracker's
 * @param integrity M5 part G (Level 13's "Integrity ≥ 50 %"): with more than 0, the defend
 *     primary's ally must keep this share of its integrity: failed at once below it, met when its
 *     synchronisation completes ({@link #integrity(double, int, String)}); 0 for none
 * @param links M7 part C (design/campaign Level 16, Q.16a): every tendril of the level's gates broken
 *     before it leaves the screen ({@link #links(int, String)}); counted as an escapes objective
 * @param untouched M7 part D (design/campaign Level 17, Q.25c): none of the level's ground allies hit
 *     ({@link #untouched(int, String)}): failed at the first hit on any of them, met when the last of
 *     them leaves the bottom edge
 * @param standing round 41 (design/campaign Level 17, the user's decision of 2026-10-19, "No dome lost"):
 *     every one of the level's ground allies still standing when the last of them leaves the bottom edge,
 *     hits allowed ({@link #standing(int, String)}): failed at the first one broken
 * @param escapesAllowed M7 part F (design/campaign Level 19, "12 of 14"): how many units of an escapes objective
 *     may get away ({@link #withEscapesAllowed(int)}); 0 for none (every one destroyed)
 */
public record Secondary(
        double killRatio,
        int credits,
        List<String> groups,
        String escapes,
        List<String> killAll,
        String label,
        String partsOf,
        List<Integer> parts,
        int beforePhase,
        String tag,
        boolean afloat,
        boolean sondes,
        double integrity,
        boolean links,
        boolean untouched,
        boolean standing,
        int escapesAllowed) {
    public Secondary {
        if (escapesAllowed < 0
                || (escapesAllowed > 0
                        && (escapes.isEmpty() || !killAll.isEmpty() || !parts.isEmpty() || sondes || links))) {
            throw new IllegalArgumentException(
                    "only an escapes objective lets some of its units get away: " + escapesAllowed);
        }
        if ((untouched || standing)
                && ((untouched && standing)
                        || links
                        || sondes
                        || afloat
                        || integrity > 0
                        || killRatio > 0
                        || !groups.isEmpty()
                        || !escapes.isEmpty()
                        || !killAll.isEmpty()
                        || !parts.isEmpty())) {
            throw new IllegalArgumentException(
                    "an untouched or standing objective judges the ground allies, nothing else");
        }
        if (links
                && (sondes
                        || afloat
                        || integrity > 0
                        || killRatio > 0
                        || !groups.isEmpty()
                        || !escapes.isEmpty()
                        || !killAll.isEmpty()
                        || !parts.isEmpty())) {
            throw new IllegalArgumentException("a links objective counts the tendrils, nothing else");
        }
        if (!(integrity >= 0 && integrity < 1)) {
            throw new IllegalArgumentException("an integrity objective's share is from 0 to below 1: " + integrity);
        }
        if (integrity > 0
                && (killRatio > 0
                        || afloat
                        || sondes
                        || !groups.isEmpty()
                        || !escapes.isEmpty()
                        || !killAll.isEmpty()
                        || !parts.isEmpty())) {
            throw new IllegalArgumentException("an integrity objective judges the defended ally, nothing else");
        }
        if (sondes && (afloat || !groups.isEmpty() || !escapes.isEmpty() || !killAll.isEmpty() || !parts.isEmpty())) {
            throw new IllegalArgumentException("a collect: sondes objective counts the sondes, nothing else");
        }
        groups = List.copyOf(groups);
        killAll = List.copyOf(killAll);
        parts = List.copyOf(parts);
        if (parts.isEmpty() != partsOf.isEmpty()) {
            throw new IllegalArgumentException("a parts objective names its set piece and its parts");
        }
        if (!tag.isEmpty() && escapes.isEmpty()) {
            throw new IllegalArgumentException("only an escapes objective is scoped to a wave tag");
        }
        if (afloat && (!groups.isEmpty() || !escapes.isEmpty() || !killAll.isEmpty() || !parts.isEmpty())) {
            throw new IllegalArgumentException("an afloat objective counts the convoy, nothing else");
        }
    }

    /** Without M7 part F's escapes allowed. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat,
            boolean sondes,
            double integrity,
            boolean links,
            boolean untouched,
            boolean standing) {
        this(
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
                0);
    }

    /**
     * M7 part F (design/campaign Level 19, "Stop the grey tanks 12/14"): this escapes objective letting {@code
     * allowed} of its units get away: met once every unit is decided (or at the level's end) with no more than
     * {@code allowed} lost, failed at once by the one past it.
     */
    public Secondary withEscapesAllowed(int allowed) {
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
                allowed);
    }

    /** Without round 41's standing. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat,
            boolean sondes,
            double integrity,
            boolean links,
            boolean untouched) {
        this(
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
                false);
    }

    /**
     * Round 41 (design/campaign Level 17, the user's decision of 2026-10-19, "No dome lost"): {@code
     * standing: true}, every one of the level's ground allies still standing, hits allowed, for {@code
     * credits}, with the tracker's {@code label} ({@code DOMES}): failed at the first one broken, met when
     * the last of them leaves the bottom edge.
     */
    public static Secondary standing(int credits, String label) {
        return new Secondary(
                0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", false, false, 0, false, false,
                true);
    }

    /** Whether the objective judges the level's ground allies ({@link #untouched()} or {@link #standing()}). */
    public boolean byGroundAllies() {
        return untouched || standing;
    }

    /** Without M7 part D's untouched. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat,
            boolean sondes,
            double integrity,
            boolean links) {
        this(
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
                false);
    }

    /**
     * M7 part D (design/campaign Level 17, Q.25c): {@code untouched: true}, none of the level's ground
     * allies hit, for {@code credits}, with the tracker's {@code label} ({@code DOMES}): failed at the
     * first hit, met when the last of them leaves the bottom edge.
     */
    public static Secondary untouched(int credits, String label) {
        return new Secondary(
                0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", false, false, 0, false, true);
    }

    /** Without M7 part C's links. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat,
            boolean sondes,
            double integrity) {
        this(
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
                false);
    }

    /**
     * M7 part C (design/campaign Level 16, Q.16a): {@code links: true}, every tendril of the level's
     * gates broken before it leaves the screen, for {@code credits}, with the tracker's {@code label}
     * ({@code BARRIERS}): met at the last, failed at the first one lost.
     */
    public static Secondary links(int credits, String label) {
        return new Secondary(0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", false, false, 0, true);
    }

    /** Without M5 part G's integrity. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat,
            boolean sondes) {
        this(killRatio, credits, groups, escapes, killAll, label, partsOf, parts, beforePhase, tag, afloat, sondes, 0);
    }

    /**
     * M5 part G (design/campaign Level 13, user decisions G1 and G2 = a): {@code integrity: share}, the
     * defend primary's ally at or above {@code share} of its integrity when its synchronisation
     * completes, for {@code credits}, with the tracker's {@code label}: failed at once when it drops
     * below (it does not regenerate), met at the completion.
     */
    public static Secondary integrity(double share, int credits, String label) {
        if (!(share > 0)) {
            throw new IllegalArgumentException("an integrity objective has a share above 0: " + share);
        }
        return new Secondary(0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", false, false, share);
    }

    /** M5 part G: whether it is an integrity objective. */
    public boolean byIntegrity() {
        return integrity > 0;
    }

    /** Without M5 part F's sondes. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag,
            boolean afloat) {
        this(killRatio, credits, groups, escapes, killAll, label, partsOf, parts, beforePhase, tag, afloat, false);
    }

    /**
     * M5 part F (design/campaign Level 12): {@code collect: sondes}, every weather sonde collected, for
     * {@code credits}, with the tracker's {@code label}: met at the last, failed when one leaves the
     * bottom edge uncollected.
     */
    public static Secondary sondes(int credits, String label) {
        return new Secondary(0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", false, true);
    }

    /** Without M5 part E's afloat. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase,
            String tag) {
        this(killRatio, credits, groups, escapes, killAll, label, partsOf, parts, beforePhase, tag, false);
    }

    /**
     * M5 part E (user decision E8 = a): every naval convoy unit that can be damaged afloat at the
     * level boss's death, for {@code credits}, with the tracker's {@code label}.
     */
    public static Secondary afloat(int credits, String label) {
        return new Secondary(0, credits, List.of(), "", List.of(), label, "", List.of(), -1, "", true);
    }

    /** Without M5 part C's wave tag. */
    public Secondary(
            double killRatio,
            int credits,
            List<String> groups,
            String escapes,
            List<String> killAll,
            String label,
            String partsOf,
            List<Integer> parts,
            int beforePhase) {
        this(killRatio, credits, groups, escapes, killAll, label, partsOf, parts, beforePhase, "");
    }

    public Secondary(
            double killRatio, int credits, List<String> groups, String escapes, List<String> killAll, String label) {
        this(killRatio, credits, groups, escapes, killAll, label, "", List.of(), -1);
    }

    public Secondary(double killRatio, int credits, List<String> groups, String escapes) {
        this(killRatio, credits, groups, escapes, List.of(), "");
    }

    public Secondary(double killRatio, int credits, List<String> groups) {
        this(killRatio, credits, groups, "");
    }

    public Secondary(double killRatio, int credits) {
        this(killRatio, credits, List.of(), "");
    }

    /**
     * M5 part D (Level 10, user decision D5 = a): no secondary objective. A kill ratio of 0 for no
     * credits, so it is never met (nor paid, nor cued); the tracker and the briefing show none.
     */
    public static final Secondary NONE = new Secondary(0, 0);

    /** M5 part D: whether the level has no secondary objective ({@link #NONE}). */
    public boolean none() {
        return killRatio == 0
                && credits == 0
                && !byGroups()
                && !byEscapes()
                && !afloat
                && !byIntegrity()
                && !untouched
                && !standing;
    }

    /** Whether the objective is about groups rather than the kill ratio. */
    public boolean byGroups() {
        return !groups.isEmpty();
    }

    /**
     * Whether the objective is that none of an enemy gets away, that every unit of the
     * {@code killAll} enemies is destroyed (Level 05's "Scorched crater"), or that every one of
     * a boss's {@code parts} is shot off in time (Level 07): a count of the units (parts)
     * destroyed of all, met when the last is destroyed, failed when one gets away (survives).
     */
    public boolean byEscapes() {
        return !escapes.isEmpty() || !killAll.isEmpty() || byParts() || sondes || links;
    }

    /** Whether the objective is to shoot off a boss's parts before a phase ends (Level 07's "Gut the bays"). */
    public boolean byParts() {
        return !parts.isEmpty();
    }

    /**
     * Whether a unit of {@code slug} counts towards an escapes or kill-all objective (an escapes
     * objective scoped to a tag counts only the units of the waves with that tag).
     */
    public boolean counts(String slug) {
        return (escapes.equals(slug) && tag.isEmpty()) || killAll.contains(slug);
    }

    /** Whether a unit of {@code slug} of a wave tagged {@code waveTag} ("" for none) counts towards it. */
    public boolean counts(String slug, String waveTag) {
        return (escapes.equals(slug) && (tag.isEmpty() || tag.equals(waveTag))) || killAll.contains(slug);
    }
}
