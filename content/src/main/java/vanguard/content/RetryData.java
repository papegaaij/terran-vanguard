package vanguard.content;

/**
 * design/systems/retry/data.yaml.
 *
 * @param armourFloor a retried level starts with its level-start armour, but at least this share of the maximum
 * @param gameOver Okafor's last transmission on the game over screen (hard, no retry left), spoken through
 *     the radio filter
 */
public record RetryData(double armourFloor, LevelData.RadioLine gameOver) {
    public RetryData {
        Check.share("armour_floor", armourFloor);
    }
}
