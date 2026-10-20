package vanguard.content;

import java.util.List;
import java.util.Optional;

/**
 * The level's {@code objectives}: the primary objective's kind and the secondary objective.
 *
 * <p>It only holds the block's types (and their logic): {@link LevelData} implements it, so the rest of the game keeps
 * naming them as members of {@code LevelData} ({@code LevelData.Objectives}).
 */
public sealed interface ObjectivesBlock permits LevelData {
    /**
     * The primary objective's kind ({@code reach-end}, {@code escort} with its {@code escort} block,
     * or {@code destroy-targets} with the ground-target groups it names as {@code targets}) and the
     * optional secondary objective.
     *
     * @param targets the groups (ground targets' {@code group}) a destroy-targets primary needs
     *     destroyed; it fails as soon as a unit of one leaves the screen alive (Level 05)
     * @param defend M5 part G: a {@code defend} primary's block (Level 13's Nansen Relay)
     */
    public record Objectives(
            String primary,
            Optional<LevelData.Escort> escort,
            Optional<List<String>> targets,
            Optional<Secondary> secondary,
            Optional<LevelData.Defend> defend) {
        /** M5 part G: the primary that defends an ally for a while (Level 13). */
        public static final String DEFEND = "defend";

        /** Without M5 part G's defend. */
        public Objectives(
                String primary,
                Optional<LevelData.Escort> escort,
                Optional<List<String>> targets,
                Optional<Secondary> secondary) {
            this(primary, escort, targets, secondary, Optional.empty());
        }

        public Objectives {
            Check.that(
                    primary.equals("reach-end")
                            || primary.equals("escort")
                            || primary.equals("destroy-targets")
                            || primary.equals(DEFEND),
                    "primary: reach-end, escort, destroy-targets or defend, was '" + primary + "'");
            Check.that(
                    defend.isPresent() == primary.equals(DEFEND),
                    "a defend primary has its defend block, other primaries do not");
            Check.that(
                    secondary.flatMap(Secondary::integrity).isEmpty() || defend.isPresent(),
                    "an integrity secondary needs a defend primary");
            Check.that(
                    escort.isPresent() == primary.equals("escort"),
                    "an escort primary has its escort block, other primaries do not");
            Check.that(
                    targets.isPresent() == primary.equals("destroy-targets"),
                    "a destroy-targets primary names its targets, other primaries do not");
            targets.ifPresent(t -> Check.notEmpty("targets", t));
            Check.that(
                    targets.isEmpty() || secondary.flatMap(Secondary::groups).isEmpty(),
                    "the groups belong to the primary (targets) or to the secondary, not both");
        }

        /** The ground-target groups the level names: the primary's targets or the secondary's groups. */
        public List<String> groups() {
            return targets.or(() -> secondary.flatMap(Secondary::groups)).orElse(List.of());
        }
    }

    /**
     * Destroy at least {@code killRatio} of all enemies for {@code credits}, clear the ground
     * enemies of every one of the {@code groups} (named by its ground targets' {@code group}) for
     * {@code credits} each, let none of the enemy {@code escapes} leave the screen alive ("nothing
     * gets through", Level 03) for {@code credits}, destroy every unit of the enemies
     * {@code killAll} (Level 05's "Scorched crater", the tracker's {@code label}) for {@code credits},
     * or (part G) shoot off every one of the level boss's {@code parts} before its phase
     * {@code before} ends (Level 07's "Gut the bays": all eight sacs before the broadside phase
     * times out; the tracker's {@code label}) for {@code credits}. M5 part C (user decision D6 = a):
     * an {@code escapes} objective's {@code tag} scopes it to the units of the waves with that tag
     * (Level 09's two {@code bridge} packs). An optional {@code name} names the objective where the
     * generated wording would mislead (Level 09's "Hold the bridge": only the bridge packs count):
     * the briefing's bonus line and the README's credit table use it. M5 part E (user decision E8 =
     * a): {@code afloat: true}, every naval convoy unit that can be damaged afloat at the level boss's
     * death (Level 11's "Convoy afloat"; the tracker's {@code label}), failed at the first sinking.
     * M5 part F: {@code collect: sondes}, every weather sonde of the level flown through (Level 12's
     * "Sondes"; the tracker's {@code label}), met at the last, failed when one leaves the bottom edge
     * uncollected. M7 part C (design/campaign Level 16, Objective; Q.16a): {@code links: true}, every tendril of the
     * level's linked nests ({@code link: pairs}) broken before it leaves the screen, by killing an anchor or burning
     * it (Level 16's "Open road"; the tracker's {@code label}), failed at the first that leaves the bottom edge
     * standing, decided at the last. M7 part D (design/campaign Level 17, Objective; Q.25c): {@code untouched: true},
     * none of the level's ground allies ({@code allies}) hit (Level 17's "No dome hit"; the tracker's {@code label}),
     * failed at the first hit on any of them, met when the last leaves the bottom edge. Round 41 (the user's playtest
     * 2026-10-19, "both"): {@code standing: true}, none of the level's ground allies broken (Level 17's "No dome lost";
     * hits allowed), failed when the first breaks, met when the last leaves the bottom edge. M7 part F (design/campaign
     * Level 19, "12 of 14"): an {@code escapes} objective's {@code allowed}, how many of them may get away.
     */
    public record Secondary(
            Optional<Double> killRatio,
            Optional<List<String>> groups,
            Optional<String> escapes,
            Optional<List<String>> killAll,
            Optional<List<String>> parts,
            Optional<String> before,
            Optional<String> label,
            int credits,
            Optional<String> tag,
            Optional<String> name,
            Optional<Boolean> afloat,
            Optional<String> collect,
            Optional<Double> integrity,
            Optional<Boolean> links,
            Optional<Boolean> untouched,
            Optional<Boolean> standing,
            Optional<Integer> allowed) {
        /** M5 part F: what a {@code collect} objective counts, the level's weather sondes. */
        public static final String SONDES = "sondes";

        /** Without M7 part F's allowed. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat,
                Optional<String> collect,
                Optional<Double> integrity,
                Optional<Boolean> links,
                Optional<Boolean> untouched,
                Optional<Boolean> standing) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    collect,
                    integrity,
                    links,
                    untouched,
                    standing,
                    Optional.empty());
        }

        /**
         * M7 part F (design/campaign Level 19, "12 of 14"): how many units of an {@code escapes} objective may get away
         * ({@code allowed}); 0 if not given (every one destroyed).
         */
        public int escapesAllowed() {
            return allowed.orElse(0);
        }

        /** Without round 41's standing. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat,
                Optional<String> collect,
                Optional<Double> integrity,
                Optional<Boolean> links,
                Optional<Boolean> untouched) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    collect,
                    integrity,
                    links,
                    untouched,
                    Optional.empty());
        }

        /** Without M7 part D's untouched. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat,
                Optional<String> collect,
                Optional<Double> integrity,
                Optional<Boolean> links) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    collect,
                    integrity,
                    links,
                    Optional.empty());
        }

        /**
         * M7 part D: whether it is an {@code untouched} objective (design/campaign Level 17, "No dome hit"): failed at
         * the first hit on any of the level's ground allies, met when the last one leaves the bottom edge untouched.
         */
        public boolean keepsAlliesUntouched() {
            return untouched.orElse(false);
        }

        /**
         * Round 41: whether it is a {@code standing} objective (design/campaign Level 17, "No dome lost"): failed when
         * the first of the level's ground allies breaks, met when the last leaves the bottom edge standing.
         */
        public boolean keepsAlliesStanding() {
            return standing.orElse(false);
        }

        /** Whether it judges the level's ground allies ({@code untouched} or {@code standing}). */
        public boolean judgesGroundAllies() {
            return keepsAlliesUntouched() || keepsAlliesStanding();
        }

        /** Without M7 part C's links. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat,
                Optional<String> collect,
                Optional<Double> integrity) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    collect,
                    integrity,
                    Optional.empty(),
                    Optional.empty());
        }

        /** M7 part C: whether it is a {@code links} objective (every tendril broken). */
        public boolean breaksLinks() {
            return links.orElse(false);
        }

        /** Without M5 part G's integrity. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat,
                Optional<String> collect) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    collect,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());
        }

        /** Without M5 part F's collect. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name,
                Optional<Boolean> afloat) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    tag,
                    name,
                    afloat,
                    Optional.empty());
        }

        /** Without M5 part E's afloat. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits,
                Optional<String> tag,
                Optional<String> name) {
            this(killRatio, groups, escapes, killAll, parts, before, label, credits, tag, name, Optional.empty());
        }

        /** Without M5 part C's wave tag and name. */
        public Secondary(
                Optional<Double> killRatio,
                Optional<List<String>> groups,
                Optional<String> escapes,
                Optional<List<String>> killAll,
                Optional<List<String>> parts,
                Optional<String> before,
                Optional<String> label,
                int credits) {
            this(
                    killRatio,
                    groups,
                    escapes,
                    killAll,
                    parts,
                    before,
                    label,
                    credits,
                    Optional.empty(),
                    Optional.empty());
        }

        public Secondary {
            name.ifPresent(n -> Check.that(!n.isBlank(), "name: give the objective's name"));
            Check.that(tag.isEmpty() || escapes.isPresent(), "only an escapes objective is scoped to a wave tag");
            Check.that(
                    (killRatio.isPresent() ? 1 : 0)
                                    + (groups.isPresent() ? 1 : 0)
                                    + (escapes.isPresent() ? 1 : 0)
                                    + (killAll.isPresent() ? 1 : 0)
                                    + (parts.isPresent() ? 1 : 0)
                                    + (afloat.isPresent() ? 1 : 0)
                                    + (collect.isPresent() ? 1 : 0)
                                    + (integrity.isPresent() ? 1 : 0)
                                    + (links.isPresent() ? 1 : 0)
                                    + (untouched.isPresent() ? 1 : 0)
                                    + (standing.isPresent() ? 1 : 0)
                            == 1,
                    "give kill_ratio, groups, escapes, kill_all, parts, afloat, collect, integrity, links, untouched"
                            + " or standing");
            Check.that(links.orElse(true), "links: true, or leave it out");
            Check.that(untouched.orElse(true), "untouched: true, or leave it out");
            Check.that(standing.orElse(true), "standing: true, or leave it out");
            allowed.ifPresent(n -> Check.positive("allowed", n));
            Check.that(allowed.isEmpty() || escapes.isPresent(), "only an escapes objective allows some to get away");
            integrity.ifPresent(share -> Check.that(share > 0 && share < 1, "integrity: a share between 0 and 1"));
            collect.ifPresent(what -> Check.that(SONDES.equals(what), "collect: sondes, was '" + what + "'"));
            Check.that(afloat.orElse(true), "afloat: true, or leave it out");
            killRatio.ifPresent(r -> Check.share("kill_ratio", r));
            groups.ifPresent(g -> Check.notEmpty("groups", g));
            killAll.ifPresent(k -> Check.notEmpty("kill_all", k));
            parts.ifPresent(p -> Check.notEmpty("parts", p));
            Check.that(parts.isPresent() == before.isPresent(), "a parts objective names the phase they die before");
            // M5 part H: a groups objective may name its tracker (Level 14's root-burst growths, ROOTS n/8).
            Check.that(
                    killAll.isPresent()
                                    || parts.isPresent()
                                    || afloat.isPresent()
                                    || collect.isPresent()
                                    || integrity.isPresent()
                                    || links.isPresent()
                                    || untouched.isPresent()
                                    || standing.isPresent()
                            ? label.isPresent()
                            // M7 part F: an escapes objective that allows some away may name its counter (Level 19's
                            // GREY TANKS 12/14).
                            : label.isEmpty() || groups.isPresent() || allowed.isPresent(),
                    "a kill_all, parts, afloat, collect, integrity, links, untouched or standing objective has its"
                            + " tracker label,"
                            + " a groups one or an escapes one with allowed may have one, the others none");
            Check.notNegative("credits", credits);
        }
    }
}
