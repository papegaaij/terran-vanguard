package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.Hangar;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveGame;
import vanguard.sim.Armament;
import vanguard.sim.EnemySpec;

/**
 * The balancing checks over the data (design/systems/economy, design/enemies Balancing basis;
 * architecture: balance checks as JUnit tests), ported from tools/balance.py, which prints the same
 * plan as the balancing sheet:
 *
 * <ul>
 *   <li>the balance plan's purchases (M7 part A: to Level 21; M7 part I: in the shop the game runs, every Act 3
 *       icon is in) are in the shop and affordable at every hangar visit, with
 *       the levels' typical hauls as income (every level has its data since M7 part H) plus the typical
 *       player's grade bonus (a B, round 34 P3 = A), within the generator's output; on medium with
 *       the repairs of the plan's estimated damage, the ship's and from Level 08 Rook's, on hard
 *       (income × 0.9, repairs 10 cr a point) with the repairs the credits left allow, the
 *       shortfall printed as accepted ({@link #HARD_REPAIRS}); Rook's gun (bought and fitted from
 *       Level 08 on) priced in and his single-target DPS printed per visit (M5 part I);
 *   <li>the plan's fit reaches the reference DPS the enemy stat blocks assume (0.75–1.33×), with
 *       Levels 01–03's gentle onboarding (about 1.5×) as the accepted exception;
 *   <li>every enemy's time to kill at its first level and its bounty fit its size class, with the
 *       Coilwyrm's bounty and the Ravager's and the Wraith's times to kill as the accepted exceptions
 *       ({@link #ACCEPTED}); a boss's bounty as paid there (its Act 1 terms × the act factor × the
 *       level's bounty scale);
 *   <li>a returning {@code medium} or larger unit (and the Needler and the Stinger, capped at ×3, 2026-10-18) keeps
 *       its time to kill at every Act 2 level after its first, with the act HP factor (user decision D5 = c of M5
 *       part A);
 *   <li>the plan's anti-ground sources clear every hold zone's hardened cluster in at most {@link
 *       #HOLD_SHARE} of the hold's window at medium (user decision D8 = c of M5 part C; hard's
 *       tighter ratio is accepted, user decision 2026-10-07);
 *   <li>the plan's rear DPS kills every rear-ambush unit (a Wraith) in at most {@link #REAR_SHARE} of
 *       its decloak flash and hold at medium (user decision D7 = a of M5 part D), Level 12's Wraith with
 *       the act HP factor as the exception the user accepted in round 34 (P4; {@link #REAR_PENDING});
 *   <li>M5 part F: the plan's rear DPS kills a latched Lamprey (its HP at medium, ×2 damage while
 *       latched) well inside the shake-off's time, and the autopilot (Level 12's plan fit with Rook)
 *       shakes off or kills every Lamprey that latches, the drain of a latch staying a small share of
 *       the plan's shield, on every difficulty.
 * </ul>
 */
class BalanceTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    /**
     * M7 part I: the shop the game runs (Act 3's hangar icons are all in, so it is the complete catalogue); M8 part A:
     * the complete catalogue, the Act 4 items awaiting their icons ({@link Catalogue#AWAITING_ICONS}) included.
     */
    private static final Catalogue CATALOGUE = Catalogue.complete(CONTENT);

    private static final CampaignRules RULES = CampaignRules.of(CONTENT);

    /** The plan's fit against the reference DPS: inside this band. */
    static final double DPS_LOW = 0.75;

    static final double DPS_HIGH = 1.33;

    /**
     * Levels 01–03: a typical loadout is about 1.5× the reference DPS, accepted as gentle onboarding
     * (user decision 2026-10-01, design/enemies Balancing basis); up to {@link #ONBOARDING_HIGH}.
     */
    static final Set<Integer> ONBOARDING = Set.of(1, 2, 3);

    static final double ONBOARDING_HIGH = 1.75;

    /** Bosses and set pieces assume an effective DPS of 0.6 × reference (design/enemies Balancing basis). */
    static final double EFFECTIVE = 0.6;

    /**
     * M5 part C (D8 = c): a hold zone's cluster dies within this share of its window at medium, the
     * plan's anti-ground DPS against the cluster's HP (hard is not checked: about 0.73 there,
     * accepted by the user on 2026-10-07).
     */
    static final double HOLD_SHARE = 0.6;

    /**
     * M5 part D (D7 = a): a rear-ambush unit dies within this share of its decloak flash and hold at
     * medium, its HP against the plan's rear DPS (the Wraith: 16 HP ÷ the Tail Gun's 10 = 1.6 s of
     * 0.4 + 2.5 s).
     */
    static final double REAR_SHARE = 0.6;

    /**
     * M5 part F: the rear checks over {@link #REAR_SHARE} reported for the user's decision in the combined
     * concept round (key "Lnn slug"), accepted by the user (round 34 P4); the test fails when one is fixed
     * (remove it here) or a new one appears.
     */
    static final Map<String, String> REAR_PENDING = Map.of(
            "L12 wraith",
            "18 HP (the act HP factor ×1.11) / the Tail Gun L1's 10 DPS = 1.80 s of the 2.9 s flash and hold"
                    + " (0.62, at most 0.6): the plan upgrades the rear only at L14 (Tail Gun L2, 14 DPS);"
                    + " accepted by the user (round 34 P4)");

    /** M5 part F: a latched Lamprey dies within this share of its shake-off time to the plan's rear DPS. */
    static final double LATCHED_SHARE = 0.25;

    /** M5 part F: the shield a latch drains on the autopilot stays below this share of the plan's shield, on average. */
    static final double DRAIN_SHARE = 0.5;

    /** A boss's or set piece's bounty may lie this share of its target either side (the basis says "about"). */
    static final double SHARE_TOLERANCE = 1 / 3.0;

    /**
     * Deviations from the balancing basis the user accepted (design/enemies Balancing basis), with the
     * reason; the test fails when one disappears (remove it here).
     */
    static final Map<String, String> ACCEPTED = Map.ofEntries(
            Map.entry(
                    "clamp-gun ttk",
                    "250 HP at L29's reference DPS 120 is 2.1 s, above the medium class's 0.4-1.5 s: the four hold the"
                            + " rescue halt, 1,000 HP together for the README's typical halt of about 14 s at the boss rule's"
                            + " 0.6 x the reference; at 80 they fell in about 3 s (M9 part B, step B3b; accepted in round 62 C.2)"),
            Map.entry(
                    "clamp-gun bounty",
                    "the bounty 10 is below the medium class's 18-30: Level 29's approved credit budget gives it"
                            + " (round 60): the four hold the rescue halt; at 18 the typical"
                            + " haul would be 1.7 % higher, inside its 5 % (M9 part B, step B3)"),
            Map.entry(
                    "coilwyrm bounty",
                    "the parts' sum 86 (head 40, 12 segments × 3, tail 10) is above the large class's 40-60:"
                            + " a multi-part enemy, cutting it up is extra work; a head-first kill pays 40"
                            + " (user decision 2026-10-05)"),
            Map.entry(
                    "rail-serpent bounty",
                    "the parts' sum 73 (engine 40, 6 cars × 3, 3 turret cars × 5) is above the large class's 40-60: a"
                            + " multi-part train, the Coilwyrm's rule; an engine-first kill pays 40 (round 60's approved stat"
                            + " block, M9 part C, step C2)"),
            Map.entry(
                    "ravager ttk",
                    "16 HP at L09's reference DPS 63 is 0.25 s, below the medium class's 0.4-1.5 s: a fast,"
                            + " fragile pack hunter (user decision 2026-10-07, M5 part C)"),
            Map.entry(
                    "sentinel-tower bounty",
                    "the approved stat block's 40 (round 60) is above the medium class's 18-30: a tower behind a hardened"
                            + " collar, which the plan (no anti-ground) reaches only with the beam through the emitter or a"
                            + " charge; kept as approved (round 65, M9 part E)"),
            Map.entry(
                    "strider ttk",
                    "the whole 410 HP at L32's reference DPS 132 is 3.1 s, above the large class's 1-3 s: the approved"
                            + " stat block (round 60) sums the hull 220, two legs of 80 and the pod 30, and a leg (0.6 s)"
                            + " topples it; kept as approved (round 65, M9 part E)"),
            Map.entry(
                    "wraith ttk",
                    "16 HP at L10's reference DPS 66 is 0.24 s, below the medium class's 0.4–1.5 s: set for the plan's"
                            + " rear DPS (Tail Gun L1, 10) with its rear check instead (user decision D7 = a, 2026-10-08,"
                            + " M5 part D)"),
            Map.entry(
                    "iron-sovereign ttk",
                    "the paper fight (the 13 batteries' 10,620 HP and the core's 6,600 at 0.6 × L35's reference 144) is"
                            + " 199 s, above the act boss's 90-180 s: the HP set on the autopilot's measured fight (the plan's"
                            + " fit and Rook, 144 s at medium over five seeds, par 145: the beam's columns, the pods and Rook"
                            + " beat the paper's 0.6); round 70 A.1 (rail 1,200, laser 1,500, core 6,600), step BOSS70"),
            Map.entry(
                    "relay-beacon bounty",
                    "Level 27's approved bounty 15 (round 48) is below the medium class's 18-30 at 60 HP (0.5 s at L27's"
                            + " reference DPS 116, the medium class's time): the secondary is its reward; accepted in round 57"
                            + " (C.3, M8 part G, step G2)"),
            Map.entry(
                    "minelayer ttk",
                    "450 HP at L30's reference DPS 124 is 3.6 s, above the medium class's 0.4-1.5 s: the user's"
                            + " playthrough (2026-10-30) killed them before they laid a mine, and their re-test found FIX6's"
                            + " 600 px/s rise too fast; it rises at 200 px/s and strafes at 150, and at 450 a fit flying"
                            + " under it from its entry lets it lay its first cluster (Level30Test); step FIX7, pending the"
                            + " user's review"),
            Map.entry(
                    "minelayer bounty",
                    "48, above the medium class's 18-30: in proportion to its 450 HP (30 at 280; the large class's"
                            + " 40-60); step FIX7, pending the user's review"));

    /**
     * Hard: the static estimate cannot always pay both the plan's purchases and the repairs of its
     * {@code repair_points_per_level} at 10 cr a point; accepted (user decision 2026-10-05,
     * design/systems/economy): hard is meant to be tighter, a typical player there takes less damage
     * or skips some repairs.
     */
    static final String HARD_REPAIRS =
            "hard is tighter: a typical player takes less damage or skips some repairs (user decision 2026-10-05)";

    /**
     * Deviations from the balancing basis reported for a decision; the test fails when one is fixed
     * (remove it here) or a new one appears.
     */
    static final Map<String, String> PENDING = Map.of();

    /** One hangar visit of the balancing sheet. */
    record Row(
            int level,
            double budget,
            int income,
            int before,
            BalancePlan.Visit visit,
            int repairs,
            int unrepaired,
            int left,
            double load,
            double output,
            double front,
            double rear,
            double volley,
            Optional<Double> reference,
            double antiGround,
            double escort) {
        Optional<Double> ratio() {
            return reference.map(ref -> front / ref);
        }
    }

    /**
     * The balancing sheet: the plan bought visit by visit through the shop on medium, the static
     * repair estimate after every level, and each level's typical haul as its income.
     */
    static List<Row> sheet() {
        return sheet(Difficulty.MEDIUM);
    }

    /**
     * The balancing sheet on a difficulty: the income is the typical haul of the level as the
     * difficulty builds it (its formation sizes) × the difficulty's
     * credit income, the repairs cost the difficulty's price; on another difficulty than medium it
     * repairs what the credits left allow and records the rest as unrepaired.
     */
    static List<Row> sheet(Difficulty difficulty) {
        BalancePlan plan = BalancePlan.load(CATALOGUE);
        double incomeFactor = CONTENT.difficulty().creditIncome().of(difficulty);
        List<Row> rows = new ArrayList<>();
        SaveGame save = Campaign.start(RULES, difficulty).save(Instant.EPOCH);
        for (int level : plan.levels()) {
            Campaign campaign = Campaign.load(RULES, save);
            campaign.giveFreeCharges(CONTENT.specials());
            Hangar hangar = new Hangar(CATALOGUE, campaign);
            int before = campaign.credits();
            BalancePlan.Visit visit = plan.buy(hangar, level);
            List<String> problems = new ArrayList<>(visit.problems());
            int repairs = 0;
            int unrepaired = 0;
            if (hangar.missingArmour() > 0) {
                int credits = campaign.credits();
                int missing = hangar.missingArmour();
                int points = difficulty == Difficulty.MEDIUM ? missing : hangar.affordableRepair();
                if (points > 0 && !hangar.repair(points)) {
                    problems.add("repairs: " + missing + " points cost " + missing * hangar.repairCost() + ", "
                            + credits + " credits left");
                } else {
                    unrepaired = missing - points;
                }
                repairs = credits - campaign.credits();
            }
            // Round 34, P3 = A: Rook's repair estimate after a level flown with him, after the ship's.
            if (hangar.escortMissingArmour() > 0) {
                int credits = campaign.credits();
                int missing = hangar.escortMissingArmour();
                int points = difficulty == Difficulty.MEDIUM ? missing : hangar.affordableEscortRepair();
                if (points > 0 && !hangar.repairEscort(points)) {
                    problems.add("Rook's repairs: " + missing + " points cost " + missing * hangar.repairCost() + ", "
                            + credits + " credits left");
                } else {
                    unrepaired += missing - points;
                }
                repairs += credits - campaign.credits();
            }
            if (hangar.load() > hangar.output() + 1e-9) {
                problems.add("load " + hangar.load() + " MW over the output " + hangar.output());
            }
            boolean wet = plan.underwater().contains(level);
            double[] dps = dps(campaign.gear().loadout(), wet);
            // M7 part I: every planned level has its data (Level 21 since part H), so the income is always the
            // level's typical haul; M8 part I: Act 4's too, M9 part I: Act 5's (each part A's temporary budget(n)
            // for a level without its data is gone).
            double budget = CONTENT.economy().budget().of(level);
            Optional<String> key = CONTENT.levelKey(level);
            if (key.isEmpty()) {
                throw new AssertionError("Level " + level + " has no level data");
            }
            int haul = (int) Math.rint(incomeFactor
                    * TypicalHaul.of(CONTENT, key.get(), difficulty).typical());
            // Round 34, P3 = A: the typical player's grade bonus (a B) on top of the haul.
            int income = haul + (int) Math.rint(plan.gradeBonus() * haul);
            rows.add(new Row(
                    level,
                    budget,
                    income,
                    before,
                    new BalancePlan.Visit(visit.spent(), visit.log(), problems),
                    repairs,
                    unrepaired,
                    campaign.credits(),
                    hangar.load(),
                    hangar.output(),
                    dps[0],
                    dps[1],
                    dps[0] + dps[1],
                    Optional.ofNullable(CONTENT.enemyBasis().referenceDps().get(level)),
                    antiGround(campaign.gear().loadout()),
                    escort(campaign, wet)));
            SaveGame after = campaign.save(Instant.EPOCH);
            save = next(
                    after,
                    after.credits() + income,
                    campaign.maxArmour() - plan.repairPoints(),
                    campaign.escortMaxArmour() - plan.escortRepairPoints());
        }
        return rows;
    }

    /**
     * The save after the level: its income banked and the estimated damage taken, Rook's too once he is
     * hired ({@code escortArmour}); Rook's guns kept.
     */
    private static SaveGame next(SaveGame save, int credits, double armour, double escortArmour) {
        return new SaveGame(
                save.version(),
                save.created(),
                save.playtime(),
                save.difficulty(),
                save.nextLevel() + 1,
                credits,
                save.score(),
                save.loadout(),
                save.inventory(),
                save.unlocks(),
                save.specials(),
                armour,
                save.escort().hired()
                        ? new SaveGame.EscortSlot(
                                true,
                                save.escort().side(),
                                save.escort().fitted(),
                                save.escort().guns(),
                                escortArmour,
                                save.escort().flies(),
                                save.escort().bulwark())
                        : save.escort(),
                save.retriesLeft(),
                save.grades(),
                save.dataCores(),
                save.storyFlags(),
                save.stats());
    }

    /** {forward single-target DPS, rear single-target DPS} of a loadout's weapons. */
    static double[] dps(Map<LoadoutSlot, Fitted> loadout) {
        return dps(loadout, false);
    }

    /**
     * M8 part A: the weapon's (slug's) damage factor under water ({@code wet}): 1 with the {@code anti-sub} trait, else the
     * water's damage factor; M8 AW (the user, 2026-10-23): an energy weapon's (the beam, the lasers) {@code
     * energy_damage} in its place. 1 when not {@code wet}.
     */
    static double water(String slug, boolean wet) {
        EuropaData.Underwater underwater = CONTENT.europa().underwater();
        if (!wet || CONTENT.weapon(slug).antiSub()) {
            return 1;
        }
        return underwater.energy(slug) ? underwater.energyDamage() : underwater.damage();
    }

    /**
     * {forward single-target DPS, rear single-target DPS} of a loadout's weapons, M8 part A: under water ({@code
     * wet}) each at its {@link #water(String, boolean) factor}.
     */
    static double[] dps(Map<LoadoutSlot, Fitted> loadout, boolean wet) {
        double front = 0;
        double rear = 0;
        for (LoadoutSlot slot :
                List.of(LoadoutSlot.FRONT, LoadoutSlot.LEFT_WING, LoadoutSlot.RIGHT_WING, LoadoutSlot.REAR)) {
            Fitted fitted = loadout.get(slot);
            if (fitted == null) {
                continue;
            }
            WeaponData weapon = CONTENT.weapon(fitted.item());
            double single = singleTarget(weapon, weapon.levels().get(fitted.level() - 1)) * water(fitted.item(), wet);
            if (slot == LoadoutSlot.REAR) {
                rear += single;
            } else {
                front += single;
            }
        }
        return new double[] {front, rear};
    }

    /**
     * Rook's single-target DPS (M5 part I): his fitted gun's scale × its base weapon's single-target
     * DPS at the gun's level; 0 without a gun in the escort slot. Printed, not part of the forward DPS
     * the reference compares (he flies his own line and picks his own targets).
     */
    static double escort(Map<LoadoutSlot, Fitted> loadout) {
        return escort(loadout, false);
    }

    /** Rook's single-target DPS ({@link #escort(Map)}), M8 part A: under water ({@code wet}) at his gun's factor. */
    static double escort(Map<LoadoutSlot, Fitted> loadout, boolean wet) {
        Fitted fitted = loadout.get(LoadoutSlot.ESCORT);
        if (fitted == null) {
            return 0;
        }
        WingmenData.Gun gun = escortGun(fitted.item());
        WeaponData base = CONTENT.weapon(gun.base());
        return gun.scale() * singleTarget(base, base.levels().get(fitted.level() - 1)) * water(gun.base(), wet);
    }

    /**
     * M8 part A: the escort's single-target DPS in the next flight: Rook's ({@link #escort(Map, boolean)}), or the
     * Bulwark's cannon (anti-sub: full damage) when it flies (while Rook is missing, the loaner).
     */
    static double escort(Campaign campaign, boolean wet) {
        return campaign.escortFlight()
                .map(flight -> flight.rook()
                        ? escort(campaign.gear().loadout(), wet)
                        : CONTENT.wingmen().bulwark().gun().dps())
                .orElse(escort(campaign.gear().loadout(), wet));
    }

    private static WingmenData.Gun escortGun(String id) {
        return CONTENT.wingmen().guns().list().stream()
                .filter(g -> g.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    /**
     * The single-target DPS of a loadout's anti-ground sources (the only damage a hardened unit
     * takes, the specials aside): its {@code anti-ground} weapons and Rook's gun if its base weapon
     * is one (his gun's scale × the base weapon's single-target DPS at its level).
     */
    static double antiGround(Map<LoadoutSlot, Fitted> loadout) {
        double dps = 0;
        for (Map.Entry<LoadoutSlot, Fitted> entry : loadout.entrySet()) {
            Fitted fitted = entry.getValue();
            if (entry.getKey() == LoadoutSlot.ESCORT) {
                WingmenData.Gun gun = escortGun(fitted.item());
                WeaponData base = CONTENT.weapon(gun.base());
                if (base.traits().contains("anti-ground")) {
                    dps += gun.scale() * singleTarget(base, base.levels().get(fitted.level() - 1));
                }
            } else if (List.of(LoadoutSlot.FRONT, LoadoutSlot.LEFT_WING, LoadoutSlot.RIGHT_WING, LoadoutSlot.REAR)
                    .contains(entry.getKey())) {
                WeaponData weapon = CONTENT.weapon(fitted.item());
                if (weapon.traits().contains("anti-ground")) {
                    dps += singleTarget(weapon, weapon.levels().get(fitted.level() - 1));
                }
            }
        }
        return dps;
    }

    /**
     * A level's single-target DPS: its volley DPS × the share of a volley that hits a target {@code
     * single_target.width} wide {@code single_target.distance} from the muzzle (a seeking weapon:
     * all). M7 part A, by the hand formulas of their documents: a beam's damage per second (it burns
     * its first target), a drone bay's rate × damage (its shots aim at the convergence point), a
     * deflector's 0. The same model as tools/design_data.py's {@code stats} and the weapon tables.
     */
    static double singleTarget(WeaponData weapon, WeaponData.Level level) {
        double volley = weapon.volleyDps(level);
        if (level.kind() != WeaponData.Level.Kind.SHOTS
                || weapon.seek().orElse(false)
                || weapon.drone().isPresent()) {
            return volley;
        }
        WeaponRulesData.SingleTarget target = CONTENT.weaponRules().singleTarget();
        double half = target.width() / 2 + weapon.size().orElseThrow().width() / 2;
        double base = baseAngle(weapon, level);
        long hits = level.pattern().stream()
                .filter(shot ->
                        Math.abs(shot.x() + target.distance() * Math.tan(Math.toRadians(shot.angle() - base))) <= half)
                .count();
        return volley * hits / level.pattern().size();
    }

    private static double baseAngle(WeaponData weapon, WeaponData.Level level) {
        if (weapon.mirrored().orElse(false)) {
            return level.pattern().stream()
                    .mapToDouble(WeaponData.Shot::angle)
                    .average()
                    .orElseThrow();
        }
        return weapon.slot() == WeaponData.Slot.REAR ? 180 : 0;
    }

    @Test
    void thePlanIsInTheShopAndAffordableAtEveryVisit() {
        List<String> problems = print(sheet());
        assertTrue(problems.isEmpty(), "the balance plan: " + problems);
    }

    /**
     * Hard: the plan's purchases are in the shop and fit the generator at every visit; where the
     * credits fall short, of the repairs or of a purchase after the repairs of the visits before,
     * the shortfall is printed as accepted ({@link #HARD_REPAIRS}), and so is a later refit of an item
     * whose purchase it refused (M5 part E: Level 11's Mortar for Rook, refused at Level 09 on hard,
     * so Rook keeps his Autocannon there) and the sale of the item a refused purchase would have
     * replaced (M7 part A: an Ion Beam refused on hard leaves the Pulse Cannon L4 fitted; M7 part B, the
     * user's decision of 2026-10-14: on budget(n) income hard is tighter than medium by design).
     */
    @Test
    void onHardThePlanIsInTheShopWithTheShortfallAccepted() {
        List<Row> rows = sheet(Difficulty.HARD);
        int cost = CONTENT.difficulty().repairCost().of(Difficulty.HARD);
        System.out.printf(
                "Hard (income × %.2f of the hard levels' typical hauls, repairs %d cr a point):%n",
                CONTENT.difficulty().creditIncome().of(Difficulty.HARD), cost);
        List<String> problems = new ArrayList<>();
        java.util.Set<String> refused = new java.util.HashSet<>();
        java.util.regex.Pattern buy =
                java.util.regex.Pattern.compile("buy (.+ → \\S+): " + Hangar.Refusal.CREDITS.name() + " \\(");
        java.util.regex.Pattern fit = java.util.regex.Pattern.compile("fit (.+ → \\S+): not owned");
        // M7 part A: the replaced front gun is sold from the inventory only once its successor is bought.
        java.util.regex.Pattern sell = java.util.regex.Pattern.compile("sell .+ → (\\S+): not owned");
        for (String problem : print(rows)) {
            java.util.regex.Matcher bought = buy.matcher(problem);
            java.util.regex.Matcher fitted = fit.matcher(problem);
            java.util.regex.Matcher sold = sell.matcher(problem);
            if (problem.contains(": " + Hangar.Refusal.CREDITS.name() + " (")) {
                System.out.println(problem + ": accepted, " + HARD_REPAIRS);
                if (bought.find()) {
                    refused.add(bought.group(1));
                }
            } else if (fitted.find() && refused.contains(fitted.group(1))) {
                System.out.println(problem + ": accepted, its purchase was refused for credits before");
            } else if (sold.find() && refused.stream().anyMatch(item -> item.endsWith("→ " + sold.group(1)))) {
                System.out.println(problem + ": accepted, the purchase that replaces it was refused for credits");
            } else {
                problems.add(problem);
            }
        }
        for (Row row : rows) {
            if (row.unrepaired() > 0) {
                System.out.printf(
                        "L%02d visit: %d of %d armour points unrepaired (%d cr short): accepted, %s%n",
                        row.level(),
                        row.unrepaired(),
                        row.unrepaired() + row.repairs() / cost,
                        row.unrepaired() * cost,
                        HARD_REPAIRS);
            }
        }
        assertTrue(problems.isEmpty(), "the balance plan on hard: " + problems);
    }

    /** Prints the sheet and returns its problems. */
    private static List<String> print(List<Row> rows) {
        System.out.println(
                "Lvl | Budget | Income | Before | Spent | Repairs | Left | Load/Out | Fwd DPS | Ref | Fwd/Ref | Rear | Rook | Purchases");
        List<String> problems = new ArrayList<>();
        for (Row row : rows) {
            System.out.printf(
                    "%02d  | %6.0f | %6d | %6d | %5d | %7d | %4d | %4.1f/%-3.0f | %7.1f | %3.0f | %7s | %4.1f | %4.1f | %s%n",
                    row.level(),
                    row.budget(),
                    row.income(),
                    row.before(),
                    row.visit().spent(),
                    row.repairs(),
                    row.left(),
                    row.load(),
                    row.output(),
                    row.front(),
                    row.reference().orElse(0.0),
                    row.ratio().map(r -> String.format("%.2f", r)).orElse("-"),
                    row.rear(),
                    row.escort(),
                    String.join("; ", row.visit().log()));
            row.visit().problems().forEach(problem -> problems.add("L" + row.level() + ": " + problem));
        }
        System.out.println(
                "Income: the level's typical haul (design/systems/economy) plus the typical B grade bonus, banked"
                        + " at the next visit; repairs: the ship's and Rook's estimate;"
                        + " Rook: his gun's single-target DPS (from Level 08), not in the forward DPS");
        return problems;
    }

    /**
     * M5 part C (user decision D8 = c): at every level with hold zones, the plan's fit kills each
     * hold's hardened cluster (its units' HP at medium) with its anti-ground sources in at most
     * {@link #HOLD_SHARE} of the hold's window: from the trigger (its first unit {@code depth} px
     * below the top edge) until that unit's far edge leaves the bottom edge, the scroll easing over
     * the ramp (a smoothstep, so the section's and the hold's speeds averaged) and then at the hold
     * speed. Medium only (the user's call; hard's 40 px/s window and 83 HP nodes give about 0.73).
     */
    @Test
    void thePlansAntiGroundClearsEveryHoldInTime() {
        List<String> slow = new ArrayList<>();
        int holds = 0;
        for (Row row : sheet()) {
            Optional<String> key = CONTENT.levelKey(row.level());
            if (key.isEmpty()) {
                continue;
            }
            vanguard.sim.LevelScript level = SimSpecs.level(CONTENT, key.get(), Difficulty.MEDIUM);
            double scroll = CONTENT.level(key.get()).scrollSpeed();
            for (vanguard.sim.ObjectiveSpec.Hold hold : level.objectives().holds()) {
                holds++;
                double hp = 0;
                double half = 0;
                for (vanguard.sim.GroundUnit unit : level.groundUnits()) {
                    if (hold.groups().contains(unit.group())) {
                        hp += unit.enemy().hp();
                        half = Math.max(half, unit.enemy().hitbox().height() / 2);
                    }
                }
                double ease = hold.rampSeconds() * (scroll + hold.speed()) / 2;
                double window = hold.rampSeconds()
                        + (vanguard.sim.PlayField.HEIGHT + half - hold.depth() - ease) / hold.speed();
                double kill = hp / row.antiGround();
                System.out.printf(
                        "L%02d hold %s: %.0f HP / anti-ground %.1f DPS = %.1f s of a %.1f s window (%.2f, at most %.1f)%n",
                        row.level(), hold.groups(), hp, row.antiGround(), kill, window, kill / window, HOLD_SHARE);
                if (!(kill <= HOLD_SHARE * window)) {
                    slow.add(
                            String.format("L%02d hold %s: %.1f s of %.1f s", row.level(), hold.groups(), kill, window));
                }
            }
        }
        assertTrue(holds > 0, "Level 09's holds are checked");
        assertTrue(slow.isEmpty(), "the plan's anti-ground is too slow for: " + slow);
    }

    /**
     * M5 part D (user decision D7 = a): at every level with a rear ambush, the plan's rear DPS kills
     * each ambush unit (its HP at medium, with the act HP factor) in at most {@link #REAR_SHARE} of
     * its window behind the ship: its decloak flash plus its shortest hold. Only rear-slot weapons
     * count: homing picks targets ahead and Rook fires up the screen.
     */
    @Test
    void thePlansRearGunKillsEveryAmbushInTime() {
        List<String> slow = new ArrayList<>();
        Set<String> pending = new TreeSet<>();
        int checked = 0;
        for (Row row : sheet()) {
            Optional<String> key = CONTENT.levelKey(row.level());
            if (key.isEmpty()) {
                continue;
            }
            vanguard.sim.LevelScript level = SimSpecs.level(CONTENT, key.get(), Difficulty.MEDIUM);
            Set<String> seen = new TreeSet<>();
            for (vanguard.sim.WaveSpec wave : level.waves()) {
                EnemySpec enemy = wave.enemy();
                if (wave.formation() != vanguard.sim.WaveSpec.Formation.REAR_AMBUSH || !seen.add(enemy.slug())) {
                    continue;
                }
                checked++;
                double window = enemy.cloak().map(EnemySpec.Cloak::flashSeconds).orElse(0.0)
                        + enemy.hover().orElseThrow().seconds().min();
                double kill = enemy.hp() / row.rear();
                System.out.printf(
                        "L%02d %s: %.0f HP / rear %.1f DPS = %.2f s of a %.1f s flash and hold (%.2f, at most %.1f)%n",
                        row.level(), enemy.slug(), enemy.hp(), row.rear(), kill, window, kill / window, REAR_SHARE);
                if (!(kill <= REAR_SHARE * window)) {
                    String name = String.format("L%02d %s", row.level(), enemy.slug());
                    if (REAR_PENDING.containsKey(name)) {
                        pending.add(name);
                        System.out.println(name + ": accepted by the user: " + REAR_PENDING.get(name));
                    } else {
                        slow.add(String.format("%s: %.2f s of %.1f s", name, kill, window));
                    }
                }
            }
        }
        assertTrue(checked > 0, "Level 10's Wraiths are checked");
        assertTrue(slow.isEmpty(), "the plan's rear gun is too slow for: " + slow);
        assertEquals(REAR_PENDING.keySet(), pending, "the rear checks accepted by the user (round 34 P4)");
    }

    /**
     * M5 part F (design/enemies/air/lamprey, user decision F3 = a): at every level with a unit that
     * latches, the plan's rear DPS (rear-mount shots hit a latched one) kills it, at ×its damage factor,
     * within {@link #LATCHED_SHARE} of its shake-off time: shooting it off is a real choice beside the
     * shake. Its time to kill at its first level is {@link #everyEnemyFitsTheBalancingBasis}'s.
     */
    @Test
    void thePlansRearGunKillsALatchedLampreyQuickly() {
        List<String> slow = new ArrayList<>();
        int checked = 0;
        for (Row row : sheet()) {
            Optional<String> key = CONTENT.levelKey(row.level());
            if (key.isEmpty()) {
                continue;
            }
            vanguard.sim.LevelScript level = SimSpecs.level(CONTENT, key.get(), Difficulty.MEDIUM);
            Set<String> seen = new TreeSet<>();
            for (vanguard.sim.WaveSpec wave : level.waves()) {
                EnemySpec enemy = wave.enemy();
                if (enemy.latch().isEmpty() || !seen.add(enemy.slug())) {
                    continue;
                }
                checked++;
                EnemySpec.Latch latch = enemy.latch().get();
                double kill = enemy.hp() / (row.rear() * latch.damageFactor());
                System.out.printf(
                        "L%02d %s latched: %.0f HP / (rear %.1f DPS × %.0f) = %.2f s of its %.1f s shake-off (at most %.2f)%n",
                        row.level(),
                        enemy.slug(),
                        enemy.hp(),
                        row.rear(),
                        latch.damageFactor(),
                        kill,
                        latch.withinSeconds(),
                        LATCHED_SHARE);
                if (!(kill <= LATCHED_SHARE * latch.withinSeconds())) {
                    slow.add(String.format("L%02d %s: %.2f s", row.level(), enemy.slug(), kill));
                }
            }
        }
        assertTrue(checked > 0, "Level 12's Lampreys are checked");
        assertTrue(slow.isEmpty(), "the plan's rear gun is too slow for a latched: " + slow);
    }

    /**
     * M5 part G (design/enemies/naval/skimmer, Level 13): the Skimmer, a {@code small} unit, dies within its
     * size class's time to kill (at most 0.3 s) at Level 13's reference DPS, and at the plan's DPS there;
     * {@link #everyEnemyFitsTheBalancingBasis} checks it with the others, this pins its number.
     */
    @Test
    void theSkimmerFallsWithinItsTimeToKill() {
        EnemyData skimmer = CONTENT.enemy("skimmer");
        double reference = CONTENT.enemyBasis().referenceDps().get(13);
        double ttk = skimmer.hp() / reference;
        Row row = sheet().stream().filter(r -> r.level() == 13).findFirst().orElseThrow();
        System.out.printf(
                "L13 skimmer: %.0f HP / reference %.0f DPS = %.2f s (at most 0.3); at the plan's front %.1f DPS %.2f s%n",
                skimmer.hp(), reference, ttk, row.front(), skimmer.hp() / row.front());
        assertEquals(Optional.empty(), timeToKill(skimmer));
        assertTrue(ttk <= 0.3, "ttk " + ttk);
        assertTrue(skimmer.hp() / row.front() <= 0.3, "at the plan's front DPS");
    }

    /** M5 part G: the seeds of the relay-outcome check (G2 = a: measured over several seeds). */
    static final long[] RELAY_SEEDS = {2185, 1, 2, 3, 4, 5, 6, 7};

    /**
     * M5 part G's relay check (design/campaign Level 13, Nansen Relay; user decision G2 = a, pending the
     * user's review): the autopilot guards the relay with the plan's fit and Rook over {@link #RELAY_SEEDS}
     * on every difficulty. The relay is never lost on any difficulty; on medium it keeps at least half its
     * integrity at the synchronisation's end in about half the seeds (the median seed ends at 45–60 %, some
     * seeds above the threshold and some below), on easy in nearly all (at least 7 of 8); hard may lose the
     * secondary. Printed per difficulty: the integrity left per seed,
     * the seeds that kept the secondary, the attempts lost (to the ship's armour).
     */
    @Test
    void theAutopilotKeepsTheRelay() {
        Content content = ContentLoader.fromClasspath();
        for (Difficulty difficulty : Difficulty.values()) {
            int met = 0;
            int lostShips = 0;
            List<String> shares = new ArrayList<>();
            List<String> damage = new ArrayList<>();
            List<String> relayLost = new ArrayList<>();
            for (long seed : RELAY_SEEDS) {
                vanguard.sim.Sortie sortie = Level13Test.sortie(content, seed, difficulty);
                Level13Test.Run run = Level13Test.watch(sortie);
                assertTrue(sortie.complete(), difficulty + " seed " + seed + ": the level completes");
                shares.add(String.format("%.0f", 100 * run.integrity));
                double integrity = sortie.script()
                        .objectives()
                        .defend()
                        .orElseThrow()
                        .ally()
                        .hp();
                damage.add(String.format("%.0f", (1 - run.integrity) * integrity));
                met += sortie.result().secondaryMet() ? 1 : 0;
                lostShips += sortie.attempt() - 1;
                if (run.deaths.contains("relay")) {
                    relayLost.add(seed + ": " + run.deaths.strip());
                }
            }
            System.out.printf(
                    "L13 relay %s: integrity at the sync's end %s %% (damage taken %s), secondary met in %d of %d,"
                            + " attempts lost %d, relay lost %s%n",
                    difficulty, shares, damage, met, RELAY_SEEDS.length, lostShips, relayLost);
            assertEquals(List.of(), relayLost, difficulty + ": the relay is never lost");
            switch (difficulty) {
                case EASY -> assertTrue(met >= RELAY_SEEDS.length - 1, "easy keeps it nearly always: " + met);
                case MEDIUM -> {
                    // The relay's damage comes in bullets of 4 and hardly varies by seed: "about half" is the
                    // median seed ending at the threshold, with seeds on either side of it.
                    double median = medianShare(shares);
                    assertTrue(
                            met >= 1 && met < RELAY_SEEDS.length && median >= 0.45 && median <= 0.6,
                            "medium keeps half its integrity in about half the seeds: " + met + ", median " + median);
                }
                case HARD -> {}
            }
        }
    }

    /** The median of whole per cent shares, as a share. */
    private static double medianShare(List<String> shares) {
        List<Double> sorted = shares.stream().map(Double::parseDouble).sorted().toList();
        int n = sorted.size();
        return (sorted.get((n - 1) / 2) + sorted.get(n / 2)) / 200;
    }

    /**
     * M5 part F's drain check (design/campaign Level 12, Credit budget): the autopilot flies Level 12
     * with the plan's fit and Rook on every difficulty; every Lamprey that latches is shaken off or shot
     * off, and a latch drains on average at most {@link #DRAIN_SHARE} of the plan's shield. Printed per
     * difficulty: the latches, the shake-offs, the shield drained per latch, the armour lost.
     */
    @Test
    void theAutopilotShakesOffTheLampreysWithLittleDrain() {
        Content content = ContentLoader.fromClasspath();
        for (Difficulty difficulty : Difficulty.values()) {
            vanguard.sim.Sortie sortie = Level12Test.sortie(content, 2185, difficulty);
            Level12Test.Run run = Level12Test.watch(sortie);
            double shield = sortie.ship().defences().maxShield();
            double perLatch = run.latches == 0 ? 0 : run.drained / run.latches;
            System.out.printf(
                    "L12 drain %s: %d latches, %d shake-offs, shield drained %.1f (%.1f a latch of %.0f), armour lost %.0f%n",
                    difficulty, run.latches, run.shakes, run.drained, perLatch, shield, run.armourLost);
            assertTrue(sortie.complete(), difficulty + ": the level completes");
            assertTrue(run.latches > 0, difficulty + ": the Lampreys latch");
            assertTrue(perLatch <= DRAIN_SHARE * shield, difficulty + ": " + perLatch + " a latch");
        }
    }

    /**
     * M7 part A (step A4): the hand formulas {@link #singleTarget} takes for Act 3's new kinds of weapon
     * match their DPS as flown: the Ion Beam's damage per second, the Light Drone Bay's rate × damage
     * (its drone's shots converge on the target) and the Deflector Pod's 0, every level.
     */
    @Test
    void theHandFormulasOfTheBeamTheDroneAndTheDeflectorMatchTheirFlownDps() {
        for (String slug : List.of("ion-beam", "light-drone-bay", "deflector-pod")) {
            WeaponData weapon = CONTENT.weapon(slug);
            Armament.Slot slot = weapon.slot() == WeaponData.Slot.FRONT ? Armament.Slot.FRONT : Armament.Slot.LEFT_WING;
            double distance = weapon.drone()
                    .flatMap(WeaponData.Drone::converge)
                    .orElse(CONTENT.weaponRules().singleTarget().distance());
            for (int level = 1; level <= weapon.levels().size(); level++) {
                WeaponData.Level numbers = weapon.levels().get(level - 1);
                // A drone's bolts arrive by the volley: one either way at the window's edges.
                double tolerance = weapon.drone().isPresent() ? numbers.damage() / 4 + 1e-9 : 0.01;
                assertEquals(
                        singleTarget(weapon, numbers),
                        ActThreeArsenalFlightTest.flownDps(slot, slug, level, distance),
                        tolerance,
                        slug + " L" + level);
            }
        }
    }

    @Test
    void thePlansFitMeetsTheReferenceDps() {
        List<String> off = new ArrayList<>();
        for (Row row : sheet()) {
            if (row.ratio().isEmpty()) {
                continue;
            }
            double ratio = row.ratio().get();
            boolean inside = ratio >= DPS_LOW && ratio <= DPS_HIGH;
            boolean onboarding = ONBOARDING.contains(row.level()) && ratio > DPS_HIGH && ratio <= ONBOARDING_HIGH;
            if (onboarding) {
                System.out.printf(
                        "L%02d: DPS %.0f vs reference %.0f (%.2f×): accepted, gentle onboarding%n",
                        row.level(), row.front(), row.reference().get(), ratio);
            } else if (!inside) {
                off.add(String.format(
                        "L%02d: DPS %.0f vs reference %.0f (%.2f×)",
                        row.level(), row.front(), row.reference().get(), ratio));
            }
        }
        assertTrue(off.isEmpty(), "outside " + DPS_LOW + "-" + DPS_HIGH + "× the reference: " + off);
    }

    @Test
    void everyEnemyFitsTheBalancingBasis() {
        Map<String, String> found = new TreeMap<>();
        CONTENT.enemies().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String slug = entry.getKey();
            EnemyData enemy = entry.getValue();
            timeToKill(enemy).ifPresent(problem -> found.put(slug + " ttk", problem));
            bounty(enemy).ifPresent(problem -> found.put(slug + " bounty", problem));
        });
        found.forEach((key, problem) -> System.out.println(key + ": " + problem
                + (ACCEPTED.containsKey(key) ? " (accepted: " + ACCEPTED.get(key) + ")" : "")
                + (PENDING.containsKey(key) ? " (pending a decision)" : "")));
        Set<String> expected = new TreeSet<>(ACCEPTED.keySet());
        expected.addAll(PENDING.keySet());
        assertEquals(expected, found.keySet(), "deviations from the balancing basis: " + found);
    }

    /**
     * The act HP factor (design/enemies Balancing basis, D5 = c of M5 part A): a returning unit of
     * tier {@code medium} or larger (and the basis's {@code small_act_hp} units, the factor capped at ×3: the user's Act
     * 2 playtest, 2026-10-18), built by {@link SimSpecs#enemy} for each Act 2 and (M7 part A) Act 3
     * level after its first (Levels 08–21, M8 part I: and Act 4's to Level 28, M9 part I: Act 5's to Level 35, as far as the reference DPS curve goes), keeps its time to kill inside its
     * size class's target at that level's reference DPS (M7 part E: times the beam era's factor for its tier from
     * Level 18 on, {@code beam_era_hp}) (its parts' and segments' HP summed; 1 HP of
     * rounding allowed). Units already off target at their first level are left to {@link
     * #everyEnemyFitsTheBalancingBasis}.
     */
    @Test
    void everyReturningUnitKeepsItsTimeToKillFromActTwo() {
        Map<String, String> found = new TreeMap<>();
        int checked = 0;
        for (var entry : new TreeMap<>(CONTENT.enemies()).entrySet()) {
            EnemyData enemy = entry.getValue();
            if (enemy.boss().isPresent()
                    || (enemy.tier().compareTo(Tier.MEDIUM) < 0
                            && !CONTENT.enemyBasis().smallActHp().units().contains(entry.getKey()))
                    || enemy.tier() == Tier.HUGE
                    || timeToKill(enemy).isPresent()) {
                continue;
            }
            double[] target =
                    switch (enemy.tier()) {
                        case SMALL -> new double[] {0, 0.3};
                        case MEDIUM -> new double[] {0.4, 1.5};
                        default -> new double[] {1, 3};
                    };
            for (int level = Math.max(ACT_2_FIRST, enemy.firstLevel() + 1); level <= ACT_5_LAST; level++) {
                Double reference = CONTENT.enemyBasis().referenceDps().get(level);
                if (reference == null) {
                    continue;
                }
                if (enemy.tier() == Tier.SMALL
                        && !CONTENT.enemyBasis().smallActHp().applies(entry.getKey(), level)) {
                    continue; // only in small_act_hp's levels (Levels 08-09)
                }
                int act = level >= ACT_5_FIRST ? 5 : level >= ACT_4_FIRST ? 4 : level >= ACT_3_FIRST ? 3 : 2;
                EnemySpec spec =
                        SimSpecs.enemy(CONTENT, entry.getKey(), Difficulty.MEDIUM, Optional.empty(), act, level);
                double hp = spec.hp()
                        + spec.chain()
                                .map(chain -> chain.segment().hp()
                                                * chain.segmentBoxes().size()
                                        + chain.tail().hp())
                                .orElse(0.0);
                checked++;
                // M7 part E (step E5): in the beam era the plan's fit kills as the reference DPS times the era's
                // factor (the factor that doubles the small units' HP there), so the time to kill is judged at that.
                double dps = reference * CONTENT.enemyBasis().beamEraHp().of(enemy.tier(), level);
                if (hp < target[0] * dps - 1 || hp > target[1] * dps + 1) {
                    found.put(
                            entry.getKey() + " L" + level,
                            String.format(
                                    "HP %.0f / %.1f DPS = %.2f s, target %s-%s s",
                                    hp, dps, hp / dps, target[0], target[1]));
                }
            }
        }
        assertTrue(checked > 0, "returning units checked");
        assertEquals(Map.of(), found, "returning units off their time to kill");
    }

    /** Act 2's levels (design/campaign/act-2-homefront). */
    static final int ACT_2_FIRST = 8;

    static final int ACT_2_LAST = 14;

    /** M7 part A: Act 3's levels (design/campaign/act-3-red-dust), as far as the reference DPS goes. */
    static final int ACT_3_FIRST = 15;

    static final int ACT_3_LAST = 21;

    /** M8 part I: Act 4's levels (design/campaign/act-4-deep-water), as far as the reference DPS goes. */
    static final int ACT_4_FIRST = 22;

    static final int ACT_4_LAST = 28;

    /** M9 part I: Act 5's levels (design/campaign/act-5-the-belt), as far as the reference DPS goes. */
    static final int ACT_5_FIRST = 29;

    static final int ACT_5_LAST = 35;

    /**
     * Time to kill at the unit's first level (design/enemies Balancing basis): {@code tiny} one hit
     * of the starting gun, {@code small} ≤ 0.3 s, {@code medium} 0.4–1.5 s, {@code large} 1–3 s,
     * {@code huge} set pieces 20–40 s, mid-bosses 45–75 s, act bosses 90–180 s; bosses and set
     * pieces at the effective 0.6 × reference DPS. A multi-part unit counts its parts' sum. The HP
     * are whole damage units, so the bounds allow 1 HP of rounding.
     */
    static Optional<String> timeToKill(EnemyData enemy) {
        Double reference = CONTENT.enemyBasis().referenceDps().get(enemy.firstLevel());
        if (reference == null) {
            return Optional.empty();
        }
        if (enemy.tier() == Tier.TINY && enemy.boss().isEmpty()) {
            double shot =
                    CONTENT.weapon(SimSpecs.PULSE_CANNON).levels().getFirst().damage();
            return enemy.hp() <= shot
                    ? Optional.empty()
                    : Optional.of("HP " + enemy.hp() + ": not one hit of the starting gun (" + shot + ")");
        }
        double[] target = enemy.boss()
                .map(boss -> LevelMetrics.bossWindow(boss.kind().equals("mid-boss")))
                .orElseGet(() -> switch (enemy.tier()) {
                    case SMALL -> new double[] {0, 0.3};
                    case MEDIUM -> new double[] {0.4, 1.5};
                    case LARGE -> new double[] {1, 3};
                    default -> new double[] {20, 40};
                });
        double dps = reference * (enemy.boss().isPresent() || enemy.tier() == Tier.HUGE ? EFFECTIVE : 1);
        if (enemy.movement().anchored().isPresent() || enemy.walks()) {
            // M5 part E (E6 = a): an arena boss's parts are exposed in windows; its duration counts them. M7 part F: a
            // walking boss's passes (walkerSeconds).
            double seconds = enemy.walks() ? walkerSeconds(enemy, dps) : arenaSeconds(enemy, dps);
            return seconds < target[0] || seconds > target[1]
                    ? Optional.of(String.format(
                            "window-aware %.1f s at %.1f DPS, target %s-%s s", seconds, dps, target[0], target[1]))
                    : Optional.empty();
        }
        if (enemy.hp() < target[0] * dps - 1 || enemy.hp() > target[1] * dps + 1) {
            return Optional.of(String.format(
                    "HP %.0f / %.1f DPS = %.2f s, target %s-%s s",
                    enemy.hp(), dps, enemy.hp() / dps, target[0], target[1]));
        }
        return Optional.empty();
    }

    /**
     * M5 part E (design/enemies/bosses/harbour-kraken, user decision E6 = a): an arena boss's fight at
     * the effective {@code dps}, its exposure windows counted (tools/balance.py prints the same): the
     * bar appears as the scroll eases into the halt ({@code Sortie.ARENA_RAMP_SECONDS}); a phase that
     * ends on slams lasts its delay and its slams (each cycle the telegraph, rise, awash and sink; the
     * last one to its impact), at most its seconds, its parts out of the way (the arms need not die); a
     * phase without a surfacing (round 40, the rooted Kraken's Arms) cuts its parts down to its left, the
     * cheapest first, at most its seconds;
     * a phase that ends on a share of its parts' HP takes that HP in cycles of its surfacing (the rise,
     * the open window, the dive, and a slam's cycle after the dive): while the window is open the damage
     * takes the mean of the part's own multiplier and its weak spots' (the eyes and the mantle hit about
     * equally; round 40, rooted arms: its weak spots'), while it rises or dives on the surface (half of each) its own, under the water nothing
     * (the plan has no Torpedo Pod); the last phase takes the rest with its part kept up.
     */
    /** M7 part F: a walking boss's paper fight at {@code dps}: its intro, then its HP at {@link #WALKER_EFFICIENCY}. */
    static double walkerSeconds(EnemyData enemy, double dps) {
        return WALKER_INTRO_SECONDS + enemy.hp() / (dps * WALKER_EFFICIENCY);
    }

    /**
     * M7 part F (design/enemies/bosses/revenant-walker, Duration and HP): the share of the effective DPS that lands on a
     * walking boss, and its intro pass's seconds before it can be hit. The paper's ≈ 0.68 (on the screen ≈ 80 % of
     * the time, about half of it facing its plate) is replaced by step F4's measurement: the autopilot's fight with the
     * plan's fit lasts 52.7 s at 3,740 HP (the back takes the whole fit, the vents ×2, Rook's Mortar's and the pods'
     * {@code anti-ground} ×2), so 1.39 (tools/balance.py's {@code WALKER_EFFICIENCY}; pending the user's review). Round 43
     * (the rework, K.a of round 42: far more fire, the bigger cone): the autopilot dodges more and stands off further,
     * 57.1 s at 3,170 HP, so 1.08. Round 44 (the pods armoured while shut, the rail lance, the stomp: far more
     * dodging, the pods hit only in their windows): 50.8 s at 1,350 HP, so 0.52.
     */
    static final double WALKER_EFFICIENCY = 0.52;

    /** M7 part F: a walking boss's intro pass before it can be hit, s (the walker's spec; tools/balance.py). */
    static final double WALKER_INTRO_SECONDS = 4;

    static double arenaSeconds(EnemyData enemy, double dps) {
        EnemyData.BossData boss = enemy.boss().orElseThrow();
        if (boss.coil().isPresent()) {
            return coilSeconds(enemy, dps);
        }
        if (boss.slam().isEmpty()) {
            return lanelessSeconds(enemy, dps);
        }
        EnemyData.SlamData slam = boss.slam().orElseThrow();
        double cycle = slam.telegraph() + slam.rise() + slam.awash() + slam.sink();
        List<EnemyData.PartData> parts = enemy.partList().orElseThrow();
        double[] left = parts.stream().mapToDouble(EnemyData.PartData::hp).toArray();
        double seconds = vanguard.sim.Sortie.ARENA_RAMP_SECONDS;
        for (EnemyData.PhaseData phase : boss.phases()) {
            EnemyData.Until until = phase.until();
            if (until.slams().isPresent()) {
                double slams =
                        phase.delay().orElse(0.0) + (until.slams().get() - 1) * cycle + slam.telegraph() + slam.rise();
                seconds += Math.min(slams, until.seconds().orElse(Double.POSITIVE_INFINITY));
                continue;
            }
            if (phase.surface().isEmpty()) {
                // Round 40 (the rooted Kraken's Arms): its always-hittable parts cut down to its left, the cheapest
                // first at their own multiplier, its delay first, at most its seconds.
                List<String> names = new java.util.ArrayList<>(until.parts().orElseThrow());
                names.sort(java.util.Comparator.comparingDouble(name -> left[enemy.partIndex(name)]
                        / parts.get(enemy.partIndex(name)).multiplier().orElse(1.0)));
                List<String> cut = names.subList(0, names.size() - until.left().orElse(0));
                double spent = phase.delay().orElse(0.0);
                for (String name : cut) {
                    int p = enemy.partIndex(name);
                    spent += left[p] / (dps * parts.get(p).multiplier().orElse(1.0));
                    left[p] = 0;
                }
                seconds += Math.min(spent, until.seconds().orElse(Double.POSITIVE_INFINITY));
                continue;
            }
            EnemyData.SurfaceData surface = phase.surface().orElseThrow();
            EnemyData.PartData part = parts.get(enemy.partIndex(surface.part()));
            double own = part.multiplier().orElse(1.0);
            double spot = part.spots().orElse(List.of()).stream()
                    .mapToDouble(EnemyData.SpotData::multiplier)
                    .max()
                    .orElse(own);
            // Round 40: the rooted Kraken's head sits in open view above the lanes and its fans are evadable, so
            // the open window goes to the eyes; before, the eyes and the mantle took about equal shares.
            boolean rooted = boss.lanes().map(EnemyData.Lanes::isRooted).orElse(false);
            double open = rooted ? spot : (own + spot) / 2;
            List<String> names = until.parts().orElseThrow();
            double need = 0;
            double full = 0;
            for (String name : names) {
                need += left[enemy.partIndex(name)];
                full += parts.get(enemy.partIndex(name)).hp();
            }
            if (until.below().isPresent()) {
                need -= until.below().get() * full;
                double dive = surface.dive().orElseThrow();
                double round = surface.rise()
                        + surface.open().orElseThrow()
                        + dive
                        + (phase.slamming()
                                        .filter(s -> s.mode().equals("after_dive"))
                                        .isPresent()
                                ? cycle
                                : 0);
                double damage = dps * (surface.open().orElseThrow() * open + (surface.rise() + dive) / 2 * own);
                seconds += need / damage * round;
                for (String name : names) {
                    int p = enemy.partIndex(name);
                    left[p] = until.below().get() * parts.get(p).hp();
                }
            } else {
                seconds += need / (dps * open);
                for (String name : names) {
                    left[enemy.partIndex(name)] = 0;
                }
            }
        }
        return seconds;
    }

    /**
     * M8 part H (design/enemies/bosses/abyssal-maw, Duration and HP; tools/balance.py prints the same): a coiled boss's
     * paper fight at the effective {@code dps}: the ease into the halt and its intro (the coil rising), then each phase
     * the parts it ends on besides its head (the gill sacs) at their own multipliers, and its share of the head's HP at
     * the phase's {@link #COIL_FACTORS} (the jaw's windows, the skull, the implants), and in the dive's phase the dive
     * (the slide, the quiet, the shadow, the lunge, the body and the way back) with next to no damage. The spec's paper
     * estimate ≈ 131 s; step H4 measured 126.5 s (the par).
     */
    static double coilSeconds(EnemyData enemy, double dps) {
        EnemyData.BossData boss = enemy.boss().orElseThrow();
        var coil = boss.coil().orElseThrow();
        List<EnemyData.PartData> parts = enemy.partList().orElseThrow();
        int head = 0;
        while (!parts.get(head).kind().equals("vital")) {
            head++;
        }
        String headName = parts.get(head).name();
        double headHp = parts.get(head).hp();
        double seconds = vanguard.sim.Sortie.ARENA_RAMP_SECONDS + coil.intro();
        double share = 1;
        List<EnemyData.PhaseData> phases = boss.phases();
        for (int i = 0; i < phases.size(); i++) {
            EnemyData.PhaseData phase = phases.get(i);
            EnemyData.Until until = phase.until();
            for (String name : until.parts().orElse(List.of())) {
                if (!name.equals(headName)) {
                    EnemyData.PartData part = parts.get(enemy.partIndex(name));
                    seconds += part.hp() / (dps * part.multiplier().orElse(1.0));
                }
            }
            double below =
                    until.share().map(EnemyData.Share::below).or(until::below).orElse(0.0);
            seconds += (share - below) * headHp / (dps * COIL_FACTORS[Math.min(i, COIL_FACTORS.length - 1)]);
            share = below;
            if (coil.dive().filter(d -> d.phase().equals(phase.name())).isPresent()) {
                var dive = coil.dive().orElseThrow();
                seconds += dive.slide() + dive.quiet() + dive.shadow() + dive.lunge() + dive.body() + dive.back();
            }
        }
        return seconds;
    }

    /**
     * M8 part H: the share of the effective DPS a coiled boss's head takes in each phase (the Maw's spec, Duration and
     * HP): phase 1 ×0.95 (the jaw open 3 s of 5 at the throat-and-skull mean ×1.25, shut 2 s at the skull's ×0.5),
     * phase 2 ×0.85 (the jaw open for each wall, the skull between), phase 3 ×0.9 (the implants ×2 while open, one at a
     * time, the skull for the rest).
     */
    static final double[] COIL_FACTORS = {0.95, 0.85, 0.9};

    /**
     * M5 part H (design/enemies/bosses/siege-spire, decision H10 = a): an arena boss without lanes at the
     * effective {@code dps} (tools/balance.py prints the same): the ease into the halt, then each phase its
     * delay, its move (the lift) and its parts' HP at their own multipliers (the weak spots' ×2; the plan's
     * {@code anti-ground} ×2 on the roots is not counted).
     */
    static double lanelessSeconds(EnemyData enemy, double dps) {
        List<EnemyData.PartData> parts = enemy.partList().orElseThrow();
        double seconds = vanguard.sim.Sortie.ARENA_RAMP_SECONDS;
        // M9 part H (step H3): a ringed boss's phases end on its rings' locks: every battery's HP is taken before the
        // core's last phase (the Iron Sovereign).
        for (var ring : enemy.boss().orElseThrow().rings().orElse(List.of())) {
            seconds += ring.batteries().count() * ring.batteries().part().hp() / dps;
        }
        for (EnemyData.PhaseData phase : enemy.boss().orElseThrow().phases()) {
            seconds += phase.delay().orElse(0.0)
                    + phase.move().flatMap(EnemyData.MoveData::descend).orElse(0.0);
            for (String name : phase.until().parts().orElse(List.of())) {
                EnemyData.PartData part = parts.get(enemy.partIndex(name));
                seconds += part.hp() / (dps * part.multiplier().orElse(1.0));
            }
        }
        return seconds;
    }

    /**
     * The bounty's class (design/enemies Balancing basis, Act 1 terms): {@code tiny} 2–5, {@code
     * small} 10–15, {@code medium} 18–30, hardened and {@code large} 40–60; a {@code huge} set piece
     * about 15 % of its level's budget, a mid-boss 15 % and an act boss 30 %, as paid there (× the
     * level's bounty scale).
     */
    static Optional<String> bounty(EnemyData enemy) {
        // M9 part B: a neutral craft pays nothing by its rule (Level 29's wavering Stormhawks).
        if (enemy.isNeutral()) {
            return Optional.empty();
        }
        boolean hardened = enemy.armour().text().filter("hardened"::equals).isPresent();
        if (enemy.boss().isPresent() || enemy.tier() == Tier.HUGE) {
            double share = enemy.boss()
                    .map(boss -> boss.kind().equals("mid-boss") ? 0.15 : 0.30)
                    .orElse(0.15);
            Optional<String> key = CONTENT.levelKey(enemy.firstLevel());
            if (key.isEmpty()) {
                return Optional.empty();
            }
            // Boss bounties hold Act 1 terms; the act factor applies to them like every payout (no exemption).
            int act = Integer.parseInt(Content.actDirectory(key.get()).split("-")[1]);
            double paid = enemy.bounty()
                    * Math.pow(CONTENT.economy().actFactor(), act - 1)
                    * CONTENT.level(key.get()).bounties();
            double actual = paid / CONTENT.economy().budget().of(enemy.firstLevel());
            if (Math.abs(actual - share) > share * SHARE_TOLERANCE) {
                return Optional.of(String.format(
                        "%.0f paid = %.1f %% of L%02d's budget, target about %.0f %%",
                        paid, 100 * actual, enemy.firstLevel(), 100 * share));
            }
            return Optional.empty();
        }
        int[] range =
                switch (enemy.tier()) {
                    case TINY -> new int[] {2, 5};
                    case SMALL -> new int[] {10, 15};
                    case MEDIUM -> hardened ? new int[] {40, 60} : new int[] {18, 30};
                    default -> new int[] {40, 60};
                };
        if (enemy.bounty() < range[0] || enemy.bounty() > range[1]) {
            return Optional.of(enemy.bounty() + " outside the " + enemy.tier() + " class " + range[0] + "-" + range[1]);
        }
        return Optional.empty();
    }
}
