package vanguard.content.campaign;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import vanguard.content.Autopilot;
import vanguard.content.BalancePlan;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.LevelResult;
import vanguard.sim.Rules;
import vanguard.sim.Sortie;

/**
 * The campaign the test autopilot flies on the balance plan, level by level (M7 part I: shared by {@link
 * ActPlaythroughTest} and {@link PlaythroughSave}): every hangar visit gives the free charges and autosaves, buys the
 * plan's purchases through the shop and repairs what the credits left allow, the ship first and then Rook (from Level
 * 08, when he joins); the autopilot flies the level with the campaign's own loadout, armour, special charges and
 * escort; a failed attempt is retried as the mission failed screen offers it (from the boss checkpoint where the
 * difficulty has one; Rook with his level-start armour, raised to the floor); the won level is banked with Rook's
 * armour as it ended. The campaign goes through a save and a load at the hangar's autosave and before the launch.
 * Round 34, P3 = A: the expert's bound is carried level by level ({@link #bound}).
 */
final class Playthrough {
    static final Content CONTENT = ContentLoader.fromClasspath();
    static final Catalogue CATALOGUE = Catalogue.of(CONTENT);
    static final CampaignRules RULES = CampaignRules.of(CONTENT);
    /** The game's seed for every level (LevelScreen). */
    private static final long SEED = 2185;
    /** Rook joins at the hangar visit before Level 08 (design/player/wingmen). */
    static final int ESCORT_FROM = 8;
    /** Steps one attempt may take at most (20 minutes at 60 steps/s). */
    private static final int MAX_STEPS = 60 * 60 * 20;
    /** Attempts one level may take at most before the playthrough gives up. */
    static final int MAX_ATTEMPTS = 12;

    /** One hangar visit and the level that followed. */
    record Visit(
            int level,
            int before,
            BalancePlan.Visit plan,
            int repaired,
            int repairs,
            int escortRepaired,
            int escortRepairs,
            double armour,
            String escort,
            double maxArmour,
            String fit,
            int attempts,
            int gameOvers,
            List<String> failures,
            int earned,
            long expert,
            String grade,
            int kills,
            int enemies) {}

    private final Difficulty difficulty;
    private final BalancePlan plan = BalancePlan.load(CATALOGUE);
    private final boolean checkpoints;
    private final double incomeFactor;
    private final List<String> problems = new ArrayList<>();
    private Campaign campaign;
    private long bound = CONTENT.economy().startingCredits();

    Playthrough(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.checkpoints = CONTENT.difficulty().bossCheckpoint().of(difficulty);
        this.incomeFactor = CONTENT.difficulty().creditIncome().of(difficulty);
        this.campaign = Campaign.start(RULES, difficulty);
    }

    Difficulty difficulty() {
        return difficulty;
    }

    BalancePlan plan() {
        return plan;
    }

    Campaign campaign() {
        return campaign;
    }

    /** The expert's bound after the levels flown so far (round 34, P3 = A). */
    long bound() {
        return bound;
    }

    /** The plan's purchases that failed at the visits (a full special's charge aside, printed as accepted). */
    List<String> problems() {
        return problems;
    }

    /** The autosave after the debrief: the campaign through a save and a load. */
    void autosave() {
        campaign = roundTrip(campaign);
    }

    /**
     * The hangar visit before the campaign's next level and the level flown until it is won.
     *
     * @throws AssertionError when the level is not won in {@link #MAX_ATTEMPTS} attempts
     */
    Visit fly() {
        int level = campaign.nextLevel();
        // The hangar opens: the free charges, then its autosave.
        campaign.giveFreeCharges(CONTENT.specials());
        campaign = roundTrip(campaign);
        Hangar hangar = new Hangar(CATALOGUE, campaign);
        int before = campaign.credits();
        BalancePlan.Visit bought = plan.buy(hangar, level);
        for (String problem : bought.problems()) {
            if (problem.contains("charge: " + Hangar.Refusal.FULL.name() + " (")) {
                // The autopilot found or kept charges the plan expects used: nothing to buy, accepted.
                System.out.printf(
                        "%s L%02d visit: %s: accepted, the special carries its most%n", difficulty, level, problem);
            } else {
                problems.add("L" + level + " visit: " + problem);
            }
        }
        int repaired = hangar.affordableRepair();
        int credits = campaign.credits();
        if (repaired > 0) {
            check(hangar.repair(repaired), "the ship's repair is paid");
        }
        int repairs = credits - campaign.credits();
        // Rook's repair line, from what the ship's repairs left.
        check(level >= ESCORT_FROM == hangar.escortHired(), "Rook is hired from Level 08 on");
        int escortRepaired = hangar.affordableEscortRepair();
        credits = campaign.credits();
        if (escortRepaired > 0) {
            check(hangar.repairEscort(escortRepaired), "Rook's repair is paid");
        }
        int escortRepairs = credits - campaign.credits();
        String fit = fit(campaign);
        // The pre-launch save.
        campaign = roundTrip(campaign);
        double armour = campaign.armour();
        double escortMax = campaign.escortMaxArmour();
        String escort = campaign.escortFlight()
                .map(rook -> String.format("%.0f/%.0f", rook.armour(), escortMax))
                .orElse(hangar.escortHired() ? "grounded" : "-");
        String key = CampaignRoute.launch(CONTENT, campaign)
                .orElseThrow(() -> new AssertionError("Level " + level + " is not built"));
        long expert = expertBound(key);
        bound += expert - bought.spent() - repairs - escortRepairs;

        Rules rules = SimSpecs.rules(CONTENT, key, difficulty);
        campaign.launch();
        Sortie sortie = sortie(key, rules);
        int attempts = 1;
        int gameOvers = 0;
        List<String> failures = new ArrayList<>();
        int steps = 0;
        while (!sortie.complete() && attempts <= MAX_ATTEMPTS && steps++ < MAX_STEPS * MAX_ATTEMPTS) {
            if (!sortie.flying() || sortie.primaryFailed()) {
                attempts++;
                failures.add(String.format(
                        "%s at %.0f s%s",
                        sortie.flying() ? "primary failed" : "destroyed",
                        sortie.levelSeconds(),
                        sortie.bossCheckpoint() ? " (boss)" : ""));
                if (campaign.fail() == Campaign.Failure.GAME_OVER) {
                    // Continue: the hangar before the level, launched again with the gear of then.
                    gameOvers++;
                    campaign = roundTrip(campaign);
                    campaign.launch();
                    sortie = sortie(key, rules);
                    continue;
                }
                if (checkpoints && sortie.bossCheckpoint()) {
                    sortie.retryFromBoss();
                } else {
                    // Rook starts over with his level-start armour, raised to the floor (LevelScreen.retry).
                    double rook = campaign.escortFlight()
                            .map(Campaign.EscortFlight::armour)
                            .orElse(0.0);
                    // M8 part A: the Bulwark the same.
                    if ((sortie.wingman().isPresent() || sortie.bulwark().isPresent()) && rook > 0) {
                        sortie.retry(campaign.armour(), rook);
                    } else {
                        sortie.retry(campaign.armour());
                    }
                }
            }
            sortie.step(Autopilot.commands(sortie));
        }
        check(
                sortie.complete(),
                difficulty + ": Level " + level + " is not completed in " + attempts + " attempts: " + failures);
        LevelResult result = sortie.result();
        Sortie won = sortie;
        campaign.complete(
                result,
                sortie.ship().defences().armour(),
                sortie.special().used(),
                sortie.special().found(),
                won.wingman()
                        .map(rook -> OptionalDouble.of(rook.armour()))
                        .or(() -> won.bulwark().map(bulwark -> OptionalDouble.of(bulwark.armour())))
                        .orElse(OptionalDouble.empty()));
        return new Visit(
                level,
                before,
                bought,
                repaired,
                repairs,
                escortRepaired,
                escortRepairs,
                armour,
                escort,
                campaign.maxArmour(),
                fit,
                attempts,
                gameOvers,
                failures,
                result.credits().total() + result.gradeBonus(),
                expert,
                result.grade().letter(),
                result.kills(),
                result.enemies());
    }

    /**
     * Round 34, P3 = A: what the expert earns in level {@code key}: its perfect run × the difficulty's income × the
     * expert's collection, rounded, then the expert's grade bonus on it, rounded.
     */
    private long expertBound(String key) {
        long collected = Math.round(
                Math.rint(BalancePlan.perfectRun(CONTENT, key, difficulty) * incomeFactor * plan.expertCollection()));
        return collected + Math.round(Math.rint(collected * plan.expertGradeBonus()));
    }

    private Sortie sortie(String key, Rules rules) {
        Flight flight = Flight.of(CONTENT, CATALOGUE, campaign);
        return new Sortie(
                SEED, flight.loadout(), SimSpecs.level(CONTENT, key, campaign.difficulty()), rules, campaign.armour());
    }

    /** The campaign written as a save and loaded again; the save reads back unchanged. */
    static Campaign roundTrip(Campaign campaign) {
        SaveGame save = campaign.save(Instant.EPOCH);
        try {
            SaveGame read = SaveFormat.read(SaveFormat.write(save));
            check(Objects.equals(save, read), "the save reads back unchanged");
            return Campaign.load(RULES, read);
        } catch (SaveException e) {
            throw new AssertionError(e);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** The fitted items with their levels and the special charges, as the report prints them. */
    private static String fit(Campaign campaign) {
        String items = campaign.gear().loadout().entrySet().stream()
                .map(entry -> {
                    String name = CATALOGUE
                            .item(entry.getKey().kind(), entry.getValue().item())
                            .name();
                    return entry.getValue().level() > 1
                            ? name + " L" + entry.getValue().level()
                            : name;
                })
                .collect(Collectors.joining(", "));
        String charges = campaign.gear().specials().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getValue() + "× " + entry.getKey())
                .sorted()
                .collect(Collectors.joining(", "));
        return charges.isEmpty() ? items : items + "; " + charges;
    }
}
