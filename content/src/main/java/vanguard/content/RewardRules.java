package vanguard.content;

import vanguard.sim.PickupRules;
import vanguard.sim.Rules;
import vanguard.sim.ScoringRules;

/**
 * The rules that do not depend on a level's script ({@link SimSpecs#rules}): the bullet budget, the
 * pickups, scoring and credits at a difficulty.
 */
final class RewardRules {
    private RewardRules() {}

    /** The rules that do not depend on the level's script: bullet budget, pickups, scoring and credits. */
    static Rules rules(Content content, String levelKey, Difficulty difficulty) {
        DifficultyData levers = content.difficulty();
        PlayerData player = content.player();
        PlayerData.Pickups pickups = player.pickups();
        PickupRules pickupRules = new PickupRules(
                pickups.salvage().credits().small(),
                pickups.salvage().credits().medium(),
                pickups.overdrive().seconds(),
                pickups.shieldCell().shieldPercent() / 100,
                pickups.armourPatch().armour(),
                player.pickupSeconds(),
                player.pickupDriftSpeed(),
                content.ship().collectionRadius(),
                pickups.salvage().credits().large(),
                content.level(levelKey).crateSeconds().orElse(player.pickupSeconds()));
        int act = ScriptRules.levelKey(levelKey, 1);
        double creditFactor = levers.creditIncome().of(difficulty)
                * Math.pow(content.economy().actFactor(), act - 1);
        return new Rules(
                levers.bulletBudget().of(difficulty),
                Math.toRadians(levers.aimedSpreadDegrees().of(difficulty)),
                pickupRules,
                scoring(
                        content.scoring(),
                        levers.score().of(difficulty),
                        creditFactor,
                        content.level(levelKey).bounties()));
    }

    private static ScoringRules scoring(
            ScoringData scoring, double scoreFactor, double creditFactor, double bountyScale) {
        ScoringData.Chain chain = scoring.chain();
        ScoringData.Rating rating = scoring.rating();
        return new ScoringRules(
                scoring.killScore(),
                scoring.pickupScore(),
                new ScoringRules.Chain(chain.window(), chain.step(), chain.increment(), chain.max()),
                scoreFactor,
                creditFactor,
                new ScoringRules.Weights(
                        rating.killRatio(),
                        rating.armourDamage(),
                        rating.secrets(),
                        rating.maxChain(),
                        rating.fullChain()),
                scoring.bonuses().stream()
                        .map(bonus -> new ScoringRules.Bonus(
                                bonusKind(bonus.name()),
                                bonus.name(),
                                bonus.points(),
                                bonus.perKillPercent().orElse(false),
                                bonus.scale() == ScoringData.Scale.LEVEL))
                        .toList(),
                scoring.grades().stream()
                        .map(grade -> new ScoringRules.Grade(
                                grade.grade(), grade.rating().orElse(0), grade.creditBonus()))
                        .toList(),
                bountyScale);
    }

    private static ScoringRules.BonusKind bonusKind(String name) {
        return switch (name) {
            case "Destruction" -> ScoringRules.BonusKind.DESTRUCTION;
            case "Untouched" -> ScoringRules.BonusKind.UNTOUCHED;
            case "Explorer" -> ScoringRules.BonusKind.EXPLORER;
            case "Boss rush" -> ScoringRules.BonusKind.BOSS_RUSH;
            default -> throw new IllegalArgumentException("no rule for the bonus '" + name + "'");
        };
    }
}
