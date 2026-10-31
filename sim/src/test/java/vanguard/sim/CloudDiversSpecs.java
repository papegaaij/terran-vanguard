package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part B, step B1: Level 36's unit rules' fixtures, after the approved docs at medium (design/enemies/air/chimera,
 * design/campaign Level 36: the harvesters and their escorts, the fleet's drop craft, the pathfinder pod, radio); the
 * content's own fixtures are step B2's.
 */
final class CloudDiversSpecs {
    private CloudDiversSpecs() {}

    /** The Chimera's swarm fire cap: at most 3 members' volleys in 0.3 s, the Talon's 300 px fire band. */
    static final EnemySpec.SwarmFire FIRE = new EnemySpec.SwarmFire(3, 0.3, 300);

    /** The Chimera's gun: an aimed shot every 1.6 s, its 0.25 s glint the first-shot delay, 160 px/s, 4. */
    static final EnemyGun CHIMERA_GUN = EnemyGun.aimed(1.6, 0.25, 1, 160, 4, false);

    /** A Chimera swarm's member under {@code fire} (40 HP, 14 credits, the Mote Swarm's flock at 220 px/s). */
    static EnemySpec chimera(EnemySpec.SwarmFire fire) {
        return EnemySpec.of("chimera", 40, new Hitbox(28, 24), Layer.AIR, 10, true, 14, 220)
                .withGun(Optional.of(CHIMERA_GUN))
                .withMachine(true)
                .withFlock(Optional.of(PartDSpecs.FLOCK.withFire(Optional.of(fire))));
    }

    static final EnemySpec CHIMERA = chimera(FIRE);

    /** A swarm of {@code count} {@code unit}s at {@code t} on the Level 10 swarm's route. */
    static WaveSpec swarm(double t, EnemySpec unit, int count) {
        return WaveSpec.builder(t, WaveSpec.Formation.SWARM, unit, count, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .paths(List.of(PartDSpecs.ROUTE))
                .build();
    }

    /** The drift of a harvester pair: 30 px/s sideways, mirrored (positive: toward each other). */
    static final EnemySpec.CarrierDrift DRIFT = new EnemySpec.CarrierDrift(30, true);

    /**
     * A harvester (design/campaign Level 36): {@code low-air}, 160 HP, a 72×40 hit box, no fire, no contact, 30 credits
     * paid with the ground targets, not counted as an enemy; down the screen at 50 px/s on {@code drift}.
     */
    static EnemySpec harvester(EnemySpec.CarrierDrift drift) {
        return EnemySpec.of("harvester", 160, new Hitbox(72, 40), Layer.LOW_AIR, 0, false, 30, 50)
                .withUncounted(true)
                .withHaul(Optional.of(CreditSource.GROUND_TARGETS))
                .withCarrierDrift(Optional.of(drift));
    }

    static final EnemySpec HARVESTER = harvester(DRIFT);

    /** A harvester escort: a Talon-like unit circling at 90 px, 60°/s (no gun: the escorts' fire is not this step's). */
    static final EnemySpec ESCORT = EnemySpec.of("talon", 32, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 220)
            .withOrbit(Optional.of(new EnemySpec.Orbit(90, 60)));

    /** A pair of {@code harvester}s at {@code t} and {@code escorts} escorts round them. */
    static List<WaveSpec> pair(double t, EnemySpec harvester, int escorts) {
        return List.of(
                TestSpecs.wave(
                        t, WaveSpec.Formation.CARRIER_ESCORTS, harvester, 2, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE),
                TestSpecs.wave(
                        t,
                        WaveSpec.Formation.CARRIER_ESCORTS,
                        ESCORT,
                        escorts,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE));
    }

    /** The fleet's drop craft: a neutral CDF Stormhawk (no gun, no contact, no bounty). */
    static final EnemySpec FLEET = EnemySpec.of("fleet-stormhawk", 40, new Hitbox(28, 28), Layer.AIR, 0, false, 0, 400)
            .withNeutral(true);

    /** A pair of drop craft up from the bottom edge at {@code t}, 400 px/s. */
    static WaveSpec dropCraft(double t) {
        return WaveSpec.builder(t, WaveSpec.Formation.LINE_ABREAST, FLEET, 2, WaveSpec.Entry.REAR, WaveSpec.Edge.NONE)
                .speed(Optional.of(400.0))
                .build();
    }

    /** The pathfinder pod at {@code t}, {@code x}: a 20-HP chunk drifting down at 40 px/s, its crate 100 credits. */
    static HazardSpec.DebrisSpec pod(double t, double x) {
        return new HazardSpec.DebrisSpec(t, x, "pod", new Hitbox(24, 24), 0, -40, 20, 0, 0)
                .withSecret("pathfinder-pod", 100);
    }

    /** A radio cue for {@code trigger} with {@code subject}. */
    static RadioCue cue(CueTrigger trigger, String subject) {
        return new RadioCue(trigger, 0, subject, "Varga", "A line.", false, "neutral", "Varga");
    }

    /** A level of {@code seconds} at 160 px/s with these hazards, ground units, waves, secrets and radio. */
    static LevelScript level(
            double seconds,
            HazardSpec hazards,
            List<GroundUnit> units,
            List<WaveSpec> waves,
            int secrets,
            List<RadioCue> radio) {
        return LevelScript.builder(1, 1)
                .sections(List.of(new Section(seconds, 160)))
                .hazards(hazards)
                .groundUnits(units)
                .waves(waves)
                .secrets(secrets)
                .radio(radio)
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(0.8, 50)))
                .build();
    }

    /** A level of {@code seconds} with {@code waves} only. */
    static LevelScript level(double seconds, List<WaveSpec> waves) {
        return level(seconds, HazardSpec.NONE, List.of(), waves, 0, List.of());
    }

    /** A sortie of {@code level} with the starter loadout, the ship untouchable. */
    static Sortie sortie(LevelScript level) {
        return new Sortie(1, TestSpecs.LOADOUT, level, TestSpecs.RULES.withInvulnerableShip(), TestSpecs.FULL_ARMOUR);
    }

    /** Every unit of {@code slug} on the field now. */
    static List<Enemy> all(Sortie sortie, String slug) {
        java.util.ArrayList<Enemy> out = new java.util.ArrayList<>();
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i).spec().slug().equals(slug)) {
                out.add(sortie.enemy(i));
            }
        }
        return out;
    }

    /** Destroys {@code enemy} as the player's kill would. */
    static void kill(Sortie sortie, Enemy enemy) {
        for (int i = 0; i < sortie.enemyCount(); i++) {
            if (sortie.enemy(i) == enemy) {
                sortie.objectivePasses.destroy(i);
                return;
            }
        }
        throw new IllegalStateException("not on the field: " + enemy.spec().slug());
    }
}
