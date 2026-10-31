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
 * @param countsBosses step SIM-K (design/campaign Level 21, round 44 K.h: "count as the data says"): whether a kill
 *     ratio counts the level's bosses among its enemies (their kills and the total; {@link #withBossesCounted}); true
 *     unless the data says otherwise
 * @param spared M9 part B (design/campaign Level 29, "Hold fire"; an inverted escapes objective): the slug of the
 *     neutral craft none of which may be destroyed: met when the last of them has left the screen with none destroyed,
 *     failed at once by the first destroyed (a hit does not fail it); empty for none ({@link #spared(String, int,
 *     String)})
 * @param objects M9 part G (design/campaign Level 34, "Strip the graveyard"): {@code escapes} names a ground object's
 *     look, not an enemy: the level's destructibles of that look (or a variant of it, {@code <look>-<variant>}) count,
 *     a destroyed one as opened, one leaving the screen as away ({@link #objectEscapes(String, int, int, String)})
 * @param saved M10 part C (design/campaign Level 37, the shuttles): with more than 0, how many of the level's air
 *     allies ({@link ObjectiveSpec#airAllies()}) must climb out off the top edge: met at that many home, failed at once
 *     by the loss that leaves fewer than that many to come home ({@link #saved(int, int, String)}); 0 for none
 *
 * <p>Built with {@link #builder()} (M10 part C, step C1, RF9b's pattern): the factories and {@code withX} copies are
 * one-liners over it, and a new kind is a new builder setter, never another positional constructor.
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
        int escapesAllowed,
        boolean countsBosses,
        String spared,
        boolean objects,
        int saved) {
    public Secondary {
        if (saved < 0
                || (saved > 0
                        && (killRatio > 0
                                || !groups.isEmpty()
                                || !escapes.isEmpty()
                                || !killAll.isEmpty()
                                || !parts.isEmpty()
                                || afloat
                                || sondes
                                || integrity > 0
                                || links
                                || untouched
                                || standing
                                || !spared.isEmpty()
                                || objects))) {
            throw new IllegalArgumentException("a saved objective counts the air allies home, nothing else: " + saved);
        }
        if (objects
                && (escapes.isEmpty()
                        || !tag.isEmpty()
                        || killRatio > 0
                        || !groups.isEmpty()
                        || !killAll.isEmpty()
                        || !parts.isEmpty()
                        || afloat
                        || sondes
                        || integrity > 0
                        || links
                        || untouched
                        || standing
                        || !spared.isEmpty())) {
            throw new IllegalArgumentException(
                    "an object escapes objective counts its look's ground objects, nothing else");
        }
        if (!spared.isEmpty()
                && (killRatio > 0
                        || !groups.isEmpty()
                        || !escapes.isEmpty()
                        || !killAll.isEmpty()
                        || !parts.isEmpty()
                        || afloat
                        || sondes
                        || integrity > 0
                        || links
                        || untouched
                        || standing)) {
            throw new IllegalArgumentException("a spared objective counts its neutral craft, nothing else");
        }
        if (!countsBosses && !(killRatio > 0)) {
            throw new IllegalArgumentException("only a kill ratio leaves the bosses out of its count");
        }
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

    /**
     * The objective of Level 01–36's data loader (kept for it: {@code LevelRules}): a kill ratio, groups, an escapes or
     * kill-all objective, a boss's parts or a tagged escapes objective; every later component at its default. New code
     * uses {@link #builder()}.
     */
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
                false,
                false,
                0,
                false,
                false,
                false,
                0,
                true,
                "",
                false,
                0);
    }

    /** A kill ratio of {@code killRatio} for {@code credits} (0 and 0: {@link #NONE}). */
    public Secondary(double killRatio, int credits) {
        this(killRatio, credits, List.of(), "", List.of(), "", "", List.of(), -1, "");
    }

    /** M10 part C, step C1: a builder with every component at its default (no objective). */
    public static SecondaryBuilder builder() {
        return new SecondaryBuilder();
    }

    /** M10 part C, step C1: a builder holding this objective's every component, for a copy with some changed. */
    public SecondaryBuilder toBuilder() {
        return new SecondaryBuilder(this);
    }

    /**
     * M10 part C (design/campaign Level 37, the shuttles): {@code saved: target}, at least {@code target} of the level's
     * air allies climbed out off the top edge, for {@code credits}, with the tracker's {@code label} ({@code SHUTTLES}):
     * met at the {@code target}-th home, failed at once by the loss after which fewer than {@code target} can come home
     * (Level 37 at medium: 5 of 6, failed at the second loss).
     */
    public static Secondary saved(int target, int credits, String label) {
        if (target < 1) {
            throw new IllegalArgumentException("a saved objective names how many must come home: " + target);
        }
        return builder().saved(target).credits(credits).label(label).build();
    }

    /** M10 part C: whether it is a saved objective (the air allies home). */
    public boolean bySaved() {
        return saved > 0;
    }

    /**
     * M9 part G (design/campaign Level 34, "Strip the graveyard"): {@code escapes: look} naming a ground object's look,
     * for {@code credits}, with the tracker's {@code label} ({@code CONTAINERS}): every destructible of that look (or a
     * variant, {@code <look>-<variant>}) counts, a destroyed one as opened, one leaving the screen as away; met as soon
     * as all but {@code allowed} are opened (or at the level's end with no more than {@code allowed} away), failed by
     * the away one past {@code allowed}.
     */
    public static Secondary objectEscapes(String look, int allowed, int credits, String label) {
        if (look.isEmpty()) {
            throw new IllegalArgumentException("an object escapes objective names its look");
        }
        return builder()
                .credits(credits)
                .escapes(look)
                .label(label)
                .escapesAllowed(allowed)
                .objects(true)
                .build();
    }

    /**
     * M9 part G: whether a ground object of {@code look} counts towards an object escapes objective: the look itself or
     * a variant of it ({@code <look>-<variant>}); without allocating.
     */
    public boolean countsObject(String look) {
        return objects
                && look.startsWith(escapes)
                && (look.length() == escapes.length() || look.charAt(escapes.length()) == '-');
    }

    /**
     * M9 part B (design/campaign Level 29, "Hold fire"): {@code spared: slug}, none of the neutral craft {@code slug}
     * destroyed, for {@code credits}, with the tracker's {@code label} ({@code HOLD FIRE}): met when the last of them
     * has left the screen, failed at the first destroyed.
     */
    public static Secondary spared(String slug, int credits, String label) {
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("a spared objective names its craft");
        }
        return builder().credits(credits).label(label).spared(slug).build();
    }

    /** M9 part B: whether it is a spared objective (the neutral craft none of which may be destroyed). */
    public boolean bySpared() {
        return !spared.isEmpty();
    }

    /**
     * Step SIM-K (design/campaign Level 21, "the boss and its debris not counted"): this kill ratio counting the level's
     * bosses among its enemies ({@code counted}) or not.
     */
    public Secondary withBossesCounted(boolean counted) {
        return toBuilder().countsBosses(counted).build();
    }

    /**
     * M7 part F (design/campaign Level 19, "Stop the grey tanks 12/14"): this escapes objective letting {@code
     * allowed} of its units get away: met once every unit is decided (or at the level's end) with no more than
     * {@code allowed} lost, failed at once by the one past it.
     */
    public Secondary withEscapesAllowed(int allowed) {
        return toBuilder().escapesAllowed(allowed).build();
    }

    /**
     * Round 41 (design/campaign Level 17, the user's decision of 2026-10-19, "No dome lost"): {@code
     * standing: true}, every one of the level's ground allies still standing, hits allowed, for {@code
     * credits}, with the tracker's {@code label} ({@code DOMES}): failed at the first one broken, met when
     * the last of them leaves the bottom edge.
     */
    public static Secondary standing(int credits, String label) {
        return builder().credits(credits).label(label).standing(true).build();
    }

    /** Whether the objective judges the level's ground allies ({@link #untouched()} or {@link #standing()}). */
    public boolean byGroundAllies() {
        return untouched || standing;
    }

    /**
     * M7 part D (design/campaign Level 17, Q.25c): {@code untouched: true}, none of the level's ground
     * allies hit, for {@code credits}, with the tracker's {@code label} ({@code DOMES}): failed at the
     * first hit, met when the last of them leaves the bottom edge.
     */
    public static Secondary untouched(int credits, String label) {
        return builder().credits(credits).label(label).untouched(true).build();
    }

    /**
     * M7 part C (design/campaign Level 16, Q.16a): {@code links: true}, every tendril of the level's
     * gates broken before it leaves the screen, for {@code credits}, with the tracker's {@code label}
     * ({@code BARRIERS}): met at the last, failed at the first one lost.
     */
    public static Secondary links(int credits, String label) {
        return builder().credits(credits).label(label).links(true).build();
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
        return builder().credits(credits).label(label).integrity(share).build();
    }

    /** M5 part G: whether it is an integrity objective. */
    public boolean byIntegrity() {
        return integrity > 0;
    }

    /**
     * M5 part F (design/campaign Level 12): {@code collect: sondes}, every weather sonde collected, for
     * {@code credits}, with the tracker's {@code label}: met at the last, failed when one leaves the
     * bottom edge uncollected.
     */
    public static Secondary sondes(int credits, String label) {
        return builder().credits(credits).label(label).sondes(true).build();
    }

    /**
     * M5 part E (user decision E8 = a): every naval convoy unit that can be damaged afloat at the
     * level boss's death, for {@code credits}, with the tracker's {@code label}.
     */
    public static Secondary afloat(int credits, String label) {
        return builder().credits(credits).label(label).afloat(true).build();
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
                && !standing
                && spared.isEmpty()
                && saved == 0;
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
        return !objects && ((escapes.equals(slug) && tag.isEmpty()) || killAll.contains(slug));
    }

    /** Whether a unit of {@code slug} of a wave tagged {@code waveTag} ("" for none) counts towards it. */
    public boolean counts(String slug, String waveTag) {
        return !objects && ((escapes.equals(slug) && (tag.isEmpty() || tag.equals(waveTag))) || killAll.contains(slug));
    }
}
