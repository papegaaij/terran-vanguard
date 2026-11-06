package vanguard.sim;

/**
 * M11 part A, step A1b, the Vrell Swarm Call's numbers (design/player/specials, Vrell Swarm Call (L46)): a bloom of
 * {@code bloomSeconds} behind the ship, then {@code drones} friendly drones fan out from it and hunt for {@code
 * lifeSeconds}, then wither over {@code witherSeconds}. A drone: a {@code size} px box, {@code hp} HP, {@code speed}
 * px/s turning {@code turnDegrees} a second; it picks the enemy nearest the ship that fewer than {@code hunters} drones
 * hunt, holds an orbit within {@code orbit} px of it and spits a thorn every {@code thornEvery} s ({@code thornDamage},
 * {@code thornSpeed} px/s, aimed: a shot of the ship's). A boss takes at most {@code bossCap} from one call.
 */
public record SwarmSpec(
        int drones,
        double size,
        double hp,
        double speed,
        double turnDegrees,
        double orbit,
        double thornEvery,
        double thornDamage,
        double thornSpeed,
        double lifeSeconds,
        double witherSeconds,
        double bloomSeconds,
        double bossCap,
        int hunters)
        implements SpecialSpec.Effect {
    public SwarmSpec {
        if (drones < 1
                || drones > SwarmCall.MOST
                || !(size > 0)
                || !(hp > 0)
                || !(speed > 0)
                || !(turnDegrees > 0)
                || !(orbit > 0)
                || !(thornEvery > 0)
                || !(thornDamage > 0)
                || !(thornSpeed > 0)
                || !(lifeSeconds > 0)
                || !(witherSeconds >= 0)
                || !(bloomSeconds >= 0)
                || !(bossCap >= 0)
                || hunters < 1) {
            throw new IllegalArgumentException("a swarm call sends 1 to " + SwarmCall.MOST
                    + " drones with HP, a speed, a turn, an orbit, thorns, a life and a boss cap");
        }
    }
}
