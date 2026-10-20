package vanguard.game.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.sim.BossSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PartSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Section;
import vanguard.sim.SetPieceSpec;
import vanguard.sim.Sortie;

/**
 * Round 40's hand-built sorties for the game's looks and sounds while Level 11's and Level 14's data are
 * reworked (step CB): the Harbour Kraken with four rooted one-lane arms (proposal 3a) laid out as the r40 a
 * art's pivots ({@code assets/pivots/harbour-kraken.json}, block {@code r40_a}: the platform's centre 46 px
 * above the top edge, the sockets 256 px below it at x ±180 and ±86, 40 × 40 roots, ×2 while raised), on
 * {@link ArenaFixture}'s convoy; and the Siege Spire of the data with its orbs taking aim turns (proposal
 * 4a: every 2 s, a 0.6 s glow, all four pods otherwise straight down).
 */
public final class RoundFortyFixture {
    static final Content CONTENT = ArenaFixture.CONTENT;
    public static final double ARRIVE = 3;
    static final double SPEED = 140;
    public static final int HEAD = 0;
    /** The root over lane n is part n. */
    public static final int ROOT_1 = 1;

    public static final double CENTRE_Y = PlayField.HEIGHT + 46;
    /** The sockets from the platform's centre (the pivots' {@code socket}, y up). */
    public static final double[] SOCKET_DX = {-180, -86, 86, 180};

    public static final double SOCKET_DY = -256;
    public static final double TELEGRAPH = 1.4;
    /** The Spire's aim turns. */
    public static final double AIM_EVERY = 2;

    public static final double AIM_GLOW = 0.6;

    private RoundFortyFixture() {}

    /** The rooted Kraken (the slug the game draws: {@code harbour-kraken}). */
    static SetPieceSpec kraken() {
        BossSpec.Attack fan = SimSpecs.boss(
                        CONTENT,
                        new LevelData.BossPlacement("harbour-kraken", ARRIVE, PlayField.WIDTH / 2.0, 2),
                        Difficulty.MEDIUM)
                .boss()
                .orElseThrow()
                .attacks()
                .getFirst();
        List<PartSpec> parts = new ArrayList<>();
        parts.add(new PartSpec("head", 0, -216, new Hitbox(130, 200), 300, true, 124, Optional.empty(), 0, 0.5));
        List<BossSpec.Arm> arms = new ArrayList<>();
        List<BossSpec.Chain> chains = new ArrayList<>();
        double[] mantleDx = {-34.6, -12.4, 12.4, 34.6};
        double[] mantleDy = {-326.6, -333, -333, -326.6};
        for (int lane = 1; lane <= 4; lane++) {
            parts.add(new PartSpec(
                    "root " + lane,
                    SOCKET_DX[lane - 1],
                    SOCKET_DY,
                    new Hitbox(40, 40),
                    200,
                    false,
                    36,
                    Optional.empty(),
                    0,
                    1));
            arms.add(new BossSpec.Arm(lane, 1 << lane));
            chains.add(new BossSpec.Chain(
                    "arm " + lane, mantleDx[lane - 1], mantleDy[lane - 1], lane, 13, new Hitbox(30, 30), 0.1, 0, true));
        }
        var attack = new BossSpec.Attack(
                "beak-fan",
                fan.pattern(),
                fan.gun(),
                fan.burstGapSeconds(),
                false,
                fan.count(),
                0,
                0,
                0,
                List.of(HEAD));
        var armsPhase = new BossSpec.Phase(
                "Arms",
                List.of(1, 2, 3, 4),
                2,
                List.of(),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                25,
                1,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        List.of(),
                        Double.NaN,
                        BossSpec.Slamming.CHAIN,
                        0,
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
        var headUp = new BossSpec.Phase(
                "Head up",
                List.of(),
                0,
                List.of(0),
                false,
                Optional.empty(),
                List.of(),
                Double.NaN,
                Double.POSITIVE_INFINITY,
                0,
                Optional.empty(),
                Optional.empty(),
                new BossSpec.PhaseArena(
                        0,
                        List.of(HEAD),
                        0.4,
                        BossSpec.Slamming.AFTER_DIVE,
                        0,
                        Optional.of(new BossSpec.Surface(HEAD, 2, 8, 1.5, 0.8, false)),
                        Optional.empty(),
                        Optional.empty()));
        var arena = new BossSpec.Arena(
                new BossSpec.Lanes(4, 120, PlayField.HEIGHT - 30, arms, true, 2),
                new BossSpec.Slam(TELEGRAPH, 0.5, 1.5, 0.6, 10, 6, 120, 4, 2, 0.5),
                List.of(Layer.SUB, Layer.GROUND, Layer.GROUND, Layer.GROUND, Layer.GROUND),
                List.of());
        return new SetPieceSpec(
                "harbour-kraken",
                new Hitbox(344, 380),
                new Hitbox(208, 152),
                25,
                parts,
                List.of(),
                Optional.empty(),
                Optional.of(new BossSpec(
                        ARRIVE,
                        PlayField.WIDTH / 2.0,
                        CENTRE_Y,
                        0,
                        0,
                        1,
                        Layer.GROUND,
                        true,
                        "HARBOUR KRAKEN",
                        60,
                        chains,
                        List.of(attack),
                        List.of(armsPhase, headUp),
                        false,
                        List.of(),
                        List.of(),
                        0,
                        Optional.of(arena))));
    }

    /** The rooted Kraken's arena over water with the convoy, flown with the starter loadout, the ship invulnerable. */
    public static Sortie kraken(int seed) {
        LevelScript level = LevelScript.builder(11, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED), new Section(ARRIVE + 1, 0, true), new Section(ARRIVE + 30, SPEED)))
                .setPieces(List.of(kraken()))
                .objectives(ObjectiveSpec.NONE.withConvoy(Optional.of(ArenaFixture.convoy())))
                .ground(GroundSpec.NONE.withWater(true))
                .build();
        return sortie(level, seed);
    }

    /** The Spire of the data with its orbs' aim turns. */
    static SetPieceSpec spire() {
        SetPieceSpec base = SimSpecs.boss(
                CONTENT,
                new LevelData.BossPlacement("siege-spire", ARRIVE, PlayField.WIDTH / 2.0, 2),
                Difficulty.MEDIUM);
        BossSpec b = base.boss().orElseThrow();
        List<BossSpec.Attack> attacks = new ArrayList<>(b.attacks());
        int orbs = 0;
        while (attacks.get(orbs).pattern() != BossSpec.Pattern.AIMED) {
            orbs++;
        }
        BossSpec.Attack o = attacks.get(orbs);
        attacks.set(
                orbs,
                new BossSpec.Attack(
                        o.name(),
                        o.pattern(),
                        o.gun(),
                        o.burstGapSeconds(),
                        false,
                        o.count(),
                        o.arms(),
                        o.turnRadiansPerSecond(),
                        o.durationSeconds(),
                        o.parts(),
                        o.stagger(),
                        o.sweep(),
                        o.parts(),
                        Optional.of(new BossSpec.AimTurns(AIM_EVERY, AIM_GLOW))));
        BossSpec boss = new BossSpec(
                b.arriveSeconds(),
                b.x(),
                b.hoverY(),
                b.descentSpeed(),
                b.sineAmplitude(),
                b.sinePeriod(),
                b.layer(),
                b.midBoss(),
                b.barName(),
                b.parSeconds(),
                b.chains(),
                attacks,
                b.phases(),
                b.engagesOnArrival(),
                b.armoured(),
                b.poses(),
                b.deathSeconds(),
                b.arena());
        return new SetPieceSpec(
                base.slug(),
                base.size(),
                base.body(),
                base.contactDamage(),
                base.parts(),
                base.passes(),
                base.drop(),
                Optional.of(boss));
    }

    /** The Spire's arena: {@link #ARRIVE} s at 140 px/s, then the halt. */
    public static Sortie spire(int seed) {
        LevelScript level = LevelScript.builder(14, 2)
                .sections(List.of(
                        new Section(ARRIVE, SPEED),
                        new Section(ARRIVE + 200, 0, true),
                        new Section(ARRIVE + 210, SPEED)))
                .setPieces(List.of(spire()))
                .build();
        return sortie(level, seed);
    }

    private static Sortie sortie(LevelScript level, int seed) {
        return new Sortie(
                seed,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                level,
                SimSpecs.rules(CONTENT, ArenaFixture.RULES_LEVEL, Difficulty.MEDIUM)
                        .withInvulnerableShip(),
                60);
    }
}
