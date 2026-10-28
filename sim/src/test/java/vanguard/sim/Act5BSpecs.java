package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M9 part B, step B1: the Level 29 fixtures the sim tests fly (the drafts' numbers, approved in round 60): the clamp
 * guns holding a held arena with its halt cycle, the Talon wing with its leader and scatter, the Asteroid Mite on its
 * rocks, the wavering Stormhawk (a neutral craft), a dormant SAM-Nest-like turret and the rescue's escort swap. Content
 * builds the same from the data (B2).
 */
final class Act5BSpecs {
    private Act5BSpecs() {}

    /** The clamp gun (Level 29): 80 HP, an aimed shot every 2.0 s, bounty 10; it scrolls with the ground. */
    static final EnemySpec CLAMP = EnemySpec.of("clamp-gun", 80, new Hitbox(30, 30), Layer.GROUND, 0, false, 10, 0)
            .withTerrain(true)
            .withGun(Optional.of(EnemyGun.aimed(2.0, 0.3, 1, 160, 4, false)));

    /** The scroll into the arena, the arena section's end (the halt) and the level's end, s. */
    static final double ARENA_START = 10;

    static final double HALT = 12;
    static final double END = 30;

    /** 100 px/s to 10 s, the held arena eased to a halt by 12 s, 120 px/s after it (eased up over 2 s). */
    static final List<Section> SECTIONS =
            List.of(new Section(ARENA_START, 100), new Section(HALT, 0, true), new Section(END, 120, false, 2, 2));

    /** The four clamp guns, entering at 9 s (two) and 9.5 s (two), holding the arena. */
    static List<GroundUnit> clamps() {
        return List.of(
                new GroundUnit(9, 150, CLAMP, -1).asHolder(),
                new GroundUnit(9, 330, CLAMP, -1).asHolder(),
                new GroundUnit(9.5, 150, CLAMP, -1).asHolder(),
                new GroundUnit(9.5, 330, CLAMP, -1).asHolder());
    }

    /** The halt cycle: three Skitters at H+1 and H+3, every 4 s. */
    static final HaltCycle CYCLE = new HaltCycle(
            4,
            List.of(
                    TestSpecs.wave(
                            1,
                            WaveSpec.Formation.SNAKE,
                            TestSpecs.SKITTER,
                            3,
                            WaveSpec.Entry.FRONT,
                            WaveSpec.Edge.LEFT),
                    TestSpecs.wave(
                            3,
                            WaveSpec.Formation.SNAKE,
                            TestSpecs.SKITTER,
                            3,
                            WaveSpec.Entry.FRONT,
                            WaveSpec.Edge.RIGHT)));

    /** The tender's hold on the halted screen and the 1 s join. */
    static final EscortSwapSpec SWAP = new EscortSwapSpec(240, 300, 1);

    /** A held-arena level with its clamps, its halt cycle (or none) and its escort swap (or none). */
    static LevelScript heldLevel(Optional<HaltCycle> cycle, Optional<EscortSwapSpec> swap, List<RadioCue> radio) {
        return LevelScript.builder(29, 5)
                .sections(SECTIONS)
                .groundUnits(clamps())
                .radio(radio)
                .objectives(ObjectiveSpec.NONE)
                .haltCycle(cycle)
                .escortSwap(swap)
                .build();
    }

    /** The Talon's scatter (design/enemies/air/talon): 260 px/s, out within 1.5 s; {@code parting} on hard. */
    static EnemySpec.SquadScatter scatter(boolean parting) {
        return new EnemySpec.SquadScatter(260, 1.5, parting);
    }

    /** A hovering Talon (32 HP, its 2-point shield) firing an aimed pair every 1.6 s, scattering by {@code rule}. */
    static EnemySpec wingTalon(EnemySpec.SquadScatter rule) {
        return EnemySpec.of("talon", 32, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 220)
                .withHover(Optional.of(new EnemySpec.Hover(new Range(6, 6), new Range(160, 160))))
                .withGun(Optional.of(EnemyGun.aimed(1.6, 0.25, 1, 160, 4, false)))
                .withMachine(true)
                .withShield(Optional.of(Act5Specs.shield(2)))
                .withScatter(Optional.of(rule));
    }

    static final EnemySpec WING_TALON = wingTalon(scatter(false));

    /** The leader of {@code talon}'s airframe: 48 HP, its 4-point shield, bounty 25. */
    static EnemySpec leader(EnemySpec talon) {
        EnemySpecCopy copy = new EnemySpecCopy(talon);
        copy.hp = 48;
        copy.bounty = 25;
        copy.shield = Optional.of(Act5Specs.shield(4));
        return copy.build();
    }

    /** A V-wing of five of {@code talon} at {@code t}, led by its leader at the tip. */
    static WaveSpec vWing(double t, EnemySpec talon) {
        return TestSpecs.wave(t, WaveSpec.Formation.V_WING, talon, 5, WaveSpec.Entry.FRONT, WaveSpec.Edge.NONE)
                .withLeader(leader(talon));
    }

    /** The Mite's ride (design/enemies/space/asteroid-mite): within 160 px, 0.4 s tell, 0.15 s apart, 260 px/s. */
    static final EnemySpec.Ride RIDE = new EnemySpec.Ride(160, 0.4, 0.15, 260, 0.3, 30);

    /** The Asteroid Mite: tiny, 1 HP, contact 6, destroyed by ramming, 3 credits. */
    static final EnemySpec MITE = EnemySpec.of("asteroid-mite", 1, new Hitbox(14, 14), Layer.AIR, 6, true, 3, 260)
            .withRide(Optional.of(RIDE));

    /** A small belt rock: 40 px, 6 HP, 4 on a hit (its t, x and drift are the wave's). */
    static final HazardSpec.DebrisSpec ROCK =
            new HazardSpec.DebrisSpec(0, 0, "belt-small-a", new Hitbox(40, 40), 0, -40, 6, 4, 0);

    /** A wave at {@code t} of {@code riders} Mites on each of {@code rocks}. */
    static WaveSpec onRocks(double t, int riders, WaveSpec.Rock... rocks) {
        return TestSpecs.wave(
                        t,
                        WaveSpec.Formation.SWARM,
                        MITE,
                        riders * rocks.length,
                        WaveSpec.Entry.FRONT,
                        WaveSpec.Edge.NONE)
                .withRocks(new WaveSpec.OnRocks(List.of(rocks), riders, ROCK));
    }

    /** The wavering Stormhawk (Level 29): 40 HP, holding 4 s 150 px below the top edge; neutral. */
    static final EnemySpec STORMHAWK = EnemySpec.of(
                    "wavering-stormhawk", 40, new Hitbox(28, 28), Layer.AIR, 0, false, 0, 90)
            .withHover(Optional.of(new EnemySpec.Hover(new Range(4, 4), new Range(150, 150))))
            .withNeutral(true);

    /**
     * Step B1b: the wavering Stormhawk as the Level 29 draft flies it: holding 6 s 150 px below the top edge, then
     * peeling off to the nearer side edge at 260 px/s.
     */
    static final EnemySpec WAVERING =
            STORMHAWK.withHover(Optional.of(new EnemySpec.Hover(new Range(6, 6), new Range(150, 150)).withPeel(260)));

    /** Step B1b: the Talon's swoop path: lowest 200–260 px below the top edge, 200 px across, firing in 300 px. */
    static final EnemySpec.SwoopPath SWOOP = new EnemySpec.SwoopPath(new Range(200, 260), 200, 300);

    /** Step B1b: a Talon (32 HP, its 2-point shield, 220 px/s) on {@code path}, an aimed shot every 1.6 s. */
    static EnemySpec swoopTalon(EnemySpec.SwoopPath path) {
        return EnemySpec.of("talon", 32, new Hitbox(28, 24), Layer.AIR, 10, true, 12, 220)
                .withGun(Optional.of(EnemyGun.aimed(1.6, 0.25, 1, 160, 4, false)))
                .withMachine(true)
                .withShield(Optional.of(Act5Specs.shield(2)))
                .withScatter(Optional.of(scatter(false)))
                .withSwoopPath(Optional.of(path));
    }

    static final EnemySpec SWOOP_TALON = swoopTalon(SWOOP);

    /**
     * Step B1b: the SAM Nest's launcher (design/enemies/ground/sam-nest): a salvo of 2 every {@code interval} s
     * ({@code burst} missiles), the rack turning 120°/s all round; the homing diamond (110 px/s, 80°/s, 4 s, 2 HP);
     * holding fire within {@code holdWithin} px; at most {@code most} in flight, its own ({@code perShooter}) or all.
     */
    static EnemyGun samGun(double interval, int burst, int most, double holdWithin, boolean perShooter) {
        return new EnemyGun(interval, 0, burst, 110, 6, false, 1, 0, Math.toRadians(120), Math.PI)
                .withHoming(Optional.of(new EnemyGun.HomingSpec(Math.toRadians(80), 4, 2, most, 0.4, true)
                        .withHoldWithin(holdWithin)
                        .withPerShooter(perShooter)));
    }

    /** Step B1b: a SAM Nest firing {@code gun}: 20 HP, bounty 15, scrolling with the ground. */
    static EnemySpec samNest(EnemyGun gun) {
        return EnemySpec.of("sam-nest", 20, new Hitbox(32, 32), Layer.GROUND, 0, false, 15, 0)
                .withTerrain(true)
                .withMachine(true)
                .withGun(Optional.of(gun));
    }

    /** A dormant-able turret (the SAM Nest's place): 20 HP, an aimed shot every 1 s, bounty 15. */
    static final EnemySpec NEST = EnemySpec.of("sam-nest", 20, new Hitbox(32, 32), Layer.GROUND, 0, false, 15, 0)
            .withTerrain(true)
            .withMachine(true)
            .withGun(Optional.of(EnemyGun.aimed(1.0, 0, 1, 160, 4, false)));

    /** The samples that set the part-B components (for the copy test). */
    static List<EnemySpec> samples() {
        return List.of(WING_TALON, MITE, STORMHAWK, SWOOP_TALON);
    }
}
