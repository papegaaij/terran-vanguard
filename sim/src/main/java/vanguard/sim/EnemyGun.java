package vanguard.sim;

import java.util.Optional;

/**
 * An enemy's attack (design/enemies, attack vocabulary), with the difficulty levers already
 * applied: an {@code aimed} shot, a {@code burst} of them, or an n-way {@code fan} centred on the
 * player. A hovering unit fires a while after it stops and then at the interval; a diver fires
 * once in its dive; a turret fires along its barrel, which turns towards the player at its turn
 * rate while the player is inside its arc.
 *
 * @param intervalSeconds time between two volleys; infinite for a one-shot attack
 * @param firstShotDelay time from stopping (a turret: entering the screen) to the first volley
 * @param burst shots per volley, {@code burstGapSeconds} apart
 * @param bulletSpeed px/s
 * @param damage damage of a hit, by the bullet class (design/enemies, balancing basis)
 * @param leadsTargetInCircle whether selected units in a {@code circle} formation aim where the
 *     player is going rather than where it is (a hard-mode hook)
 * @param fan bullets of a fan; 1 for aimed shots
 * @param spreadRadians a fan's angle from its first to its last bullet
 * @param turnRate a turret's barrel turn rate in radians per second; infinite for units that aim at once
 * @param arcRadians a turret fires while the player is within this angle of its facing (straight
 *     down); infinite for units that fire every way
 * @param mine a mine layer's spore: it drops one at the interval instead of firing
 * @param mortar a mortar's lob (design/enemies/ground/polyp-mortar): it lobs one at the interval at
 *     the player's position instead of firing; {@code damage} is the direct hit's, {@code bulletSpeed}
 *     the ring's
 * @param burstGapSeconds M5 part D: the gap between the shots of a burst (the stat block's {@code
 *     burst_gap}; {@link #BURST_GAP_SECONDS} when it has none)
 * @param up M5 part D: a burst fired straight up the screen as a fixed fan (the Wraith)
 * @param spiral M7 part E: a spiral gun on a unit that is not a boss (design/enemies/ground/dust-devil), fired
 *     only while the unit's vortex is open (a unit without a vortex never fires it): its arms turn and each fires a bullet at its interval, at the
 *     attack's bullet speed and damage ({@code intervalSeconds}, {@code burst} and the fan are not read)
 * @param passWithinX M7 part E, a pass trigger (design/enemies/air/ghost-drone; a lane strafer's gun): one burst
 *     per pass, started when the unit, strafing on the play field, is within this many px of the ship's x
 *     ({@code intervalSeconds} and {@code firstShotDelay} are not read); NaN for a gun without one
 * @param tellSeconds M7 part E: a pass trigger's tell, the time from the trigger (the {@link
 *     SimEvents.Type#EYE_GLINT} event) to the burst's first shot; 0 for none. M8 part C: a chain head's attack's
 *     tell, its {@link SimEvents.Type#EYE_GLINT} that long before each volley ({@link HeadAttacks}); other guns do
 *     not read it
 * @param reaim M7 part E: a pass trigger's burst aims each shot at the ship anew; otherwise its shots follow the
 *     first one's line (every other aimed burst already aims each of its shots anew)
 * @param homing M7 part F (design/enemies/bosses/revenant-walker, homing missiles): a boss's aimed shots of this
 *     gun are homing {@link Missile}s instead of bullets ({@code bulletSpeed} their speed, {@code damage} a hit's);
 *     M8 part G (design/enemies/naval/depth-hunter): a unit's volley of such a gun is one missile (a torpedo) launched
 *     along the unit's facing ({@link Missiles#launchAlong}); empty for every other gun
 * @param arcFacingRadians M7 part F (design/enemies/ground/spine-turret, Turned arc): the direction its {@code
 *     arcRadians} is measured from, radians clockwise from straight down; 0 (straight down) for every turret before
 *     Level 19, {@code PI} for a turret that faces up the screen ({@code arc: up}); its barrel starts along it
 * @param aimedShards M8 part B (design/enemies/ground/drill-rig, hard): bullets fired straight along the volley's aim
 *     (at the ship, within the difficulty's aim spread) besides its fan, with each volley; 0 for every other gun
 */
public record EnemyGun(
        double intervalSeconds,
        double firstShotDelay,
        int burst,
        double bulletSpeed,
        double damage,
        boolean leadsTargetInCircle,
        int fan,
        double spreadRadians,
        double turnRate,
        double arcRadians,
        Optional<MineSpec> mine,
        Optional<MortarSpec> mortar,
        double burstGapSeconds,
        boolean up,
        Optional<SpiralSpec> spiral,
        double passWithinX,
        double tellSeconds,
        boolean reaim,
        Optional<HomingSpec> homing,
        double arcFacingRadians,
        int aimedShards) {
    public EnemyGun {
        if (aimedShards < 0
                || (aimedShards > 0
                        && (up
                                || mine.isPresent()
                                || mortar.isPresent()
                                || spiral.isPresent()
                                || homing.isPresent()))) {
            throw new IllegalArgumentException("aimed shards go with a volley of bullets, 0 or more: " + aimedShards);
        }
        if (!Double.isFinite(arcFacingRadians)) {
            throw new IllegalArgumentException("an arc faces a direction: " + arcFacingRadians);
        }
        if (!(burstGapSeconds > 0)) {
            throw new IllegalArgumentException("a burst's shots are apart in time: " + burstGapSeconds);
        }
        if (up && (fan != 1 || mine.isPresent() || mortar.isPresent() || Double.isFinite(turnRate))) {
            throw new IllegalArgumentException("only an aimed burst goes straight up");
        }
        if (spiral.isPresent() && (up || mine.isPresent() || mortar.isPresent())) {
            throw new IllegalArgumentException("a spiral gun fires its arms, not up, a mine or a lob");
        }
        if (!Double.isNaN(passWithinX)
                && (!(passWithinX >= 0)
                        || up
                        || mine.isPresent()
                        || mortar.isPresent()
                        || spiral.isPresent()
                        || Double.isFinite(turnRate))) {
            throw new IllegalArgumentException(
                    "a pass trigger fires an aimed burst within 0 px or more: " + passWithinX);
        }
        if (!(tellSeconds >= 0)) {
            throw new IllegalArgumentException("a tell lasts 0 s or more: " + tellSeconds);
        }
        if (homing.isPresent() && (up || mine.isPresent() || mortar.isPresent() || spiral.isPresent() || fan != 1)) {
            throw new IllegalArgumentException("a homing gun fires single missiles, not up, a fan, a mine or a lob");
        }
    }

    /** An attack without M8 part B's aimed shards (every gun before the Drill Rig's ring). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds,
            boolean up,
            Optional<SpiralSpec> spiral,
            double passWithinX,
            double tellSeconds,
            boolean reaim,
            Optional<HomingSpec> homing,
            double arcFacingRadians) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                homing,
                arcFacingRadians,
                0);
    }

    /**
     * M8 part B (design/enemies/ground/drill-rig, hard): this volley with {@code shards} more bullets aimed straight
     * at the ship besides its fan (the hard rig's aimed shard beside its ring); 0 for none.
     */
    public EnemyGun withAimedShards(int shards) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                homing,
                arcFacingRadians,
                shards);
    }

    /** An attack without M7 part F's homing missiles (every gun before the Revenant Walker's pods). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds,
            boolean up,
            Optional<SpiralSpec> spiral,
            double passWithinX,
            double tellSeconds,
            boolean reaim) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                Optional.empty());
    }

    /** An attack whose arc faces straight down (every turret before M7 part F's {@code arc: up}). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds,
            boolean up,
            Optional<SpiralSpec> spiral,
            double passWithinX,
            double tellSeconds,
            boolean reaim,
            Optional<HomingSpec> homing) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                homing,
                0);
    }

    /**
     * M7 part F (design/enemies/ground/spine-turret, Turned arc): this turret's arc measured from {@code
     * facingRadians} (clockwise from straight down; {@code PI}: {@code arc: up}) instead of straight down.
     */
    public EnemyGun withArcFacing(double facingRadians) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                homing,
                facingRadians,
                aimedShards);
    }

    /**
     * M7 part F, a homing missile (design/enemies/bosses/revenant-walker, Attacks): it flies at the gun's bullet
     * speed and turns toward the ship at up to {@code turnRadiansPerSecond}; after {@code lifeSeconds} it fizzles.
     * It has {@code hp} (every shot of the player's and Rook's that reaches the air hits it, the Ion Beam too) and
     * at most {@code mostAlive} of them fly at once (a launch beyond that is left out). A pod's hatch opens {@code
     * hatchSeconds} before its salvo (the tell, {@link SimEvents.Type#POD_HATCH}). M9 part A (the Act 5 homing
     * diamond; decided QA-1): a {@code seekable} missile is also a target the player's homing shots, Rook and the Tesla
     * arc pick, but only when no unit is in their reach (the Revenant Walker's and the Depth Hunter's are not). M9 part
     * B, step B1b (design/enemies/ground/sam-nest): a launcher holds its fire while the ship is within {@code
     * holdWithin} px (0: never), and with {@code perShooter} {@code mostAlive} counts only its own missiles in flight.
     * M9 part D (design/enemies/air/hornet): an {@code aimed} unit's missile is launched along the bearing to its target
     * (the ship, or under the target-the-objective hook's {@code always} the objective's nearest section, snapped to the
     * pods' 16 bearings) instead of along its facing, and homes on that target.
     */
    public record HomingSpec(
            double turnRadiansPerSecond,
            double lifeSeconds,
            double hp,
            int mostAlive,
            double hatchSeconds,
            boolean seekable,
            double holdWithin,
            boolean perShooter,
            boolean aimed) {
        /** M9 part B, step B1b: launched along the unit's facing (every unit's homing gun before part D). */
        public HomingSpec(
                double turnRadiansPerSecond,
                double lifeSeconds,
                double hp,
                int mostAlive,
                double hatchSeconds,
                boolean seekable,
                double holdWithin,
                boolean perShooter) {
            this(
                    turnRadiansPerSecond,
                    lifeSeconds,
                    hp,
                    mostAlive,
                    hatchSeconds,
                    seekable,
                    holdWithin,
                    perShooter,
                    false);
        }

        /** A missile no homing shot picks (every homing missile of Acts 1–4). */
        public HomingSpec(
                double turnRadiansPerSecond, double lifeSeconds, double hp, int mostAlive, double hatchSeconds) {
            this(turnRadiansPerSecond, lifeSeconds, hp, mostAlive, hatchSeconds, false);
        }

        /** M9 part A: without step B1b's hold distance and per-shooter cap. */
        public HomingSpec(
                double turnRadiansPerSecond,
                double lifeSeconds,
                double hp,
                int mostAlive,
                double hatchSeconds,
                boolean seekable) {
            this(turnRadiansPerSecond, lifeSeconds, hp, mostAlive, hatchSeconds, seekable, 0, false);
        }

        /** M9 part A: this missile seekable (the homing diamond) or not. */
        public HomingSpec withSeekable(boolean picked) {
            return new HomingSpec(
                    turnRadiansPerSecond,
                    lifeSeconds,
                    hp,
                    mostAlive,
                    hatchSeconds,
                    picked,
                    holdWithin,
                    perShooter,
                    aimed);
        }

        /**
         * M9 part B, step B1b (design/enemies/ground/sam-nest): a launcher that holds its fire (its gun's clock waiting)
         * while the ship's centre is within {@code within} px of its own (0: never).
         */
        public HomingSpec withHoldWithin(double within) {
            return new HomingSpec(
                    turnRadiansPerSecond,
                    lifeSeconds,
                    hp,
                    mostAlive,
                    hatchSeconds,
                    seekable,
                    within,
                    perShooter,
                    aimed);
        }

        /**
         * M9 part B, step B1b (design/enemies/ground/sam-nest): {@code mostAlive} counts only the shooter's own missiles
         * in flight (true: at most 8 per nest) instead of every missile in flight (false, Acts 1–4).
         */
        public HomingSpec withPerShooter(boolean own) {
            return new HomingSpec(
                    turnRadiansPerSecond, lifeSeconds, hp, mostAlive, hatchSeconds, seekable, holdWithin, own, aimed);
        }

        /**
         * M9 part D (design/enemies/air/hornet): a unit's missiles launched at its target and homing on it ({@code
         * true}) or along its facing ({@code false}, every unit's before part D).
         */
        public HomingSpec withAimed(boolean atTarget) {
            return new HomingSpec(
                    turnRadiansPerSecond,
                    lifeSeconds,
                    hp,
                    mostAlive,
                    hatchSeconds,
                    seekable,
                    holdWithin,
                    perShooter,
                    atTarget);
        }

        public HomingSpec {
            if (!(turnRadiansPerSecond >= 0)
                    || !(lifeSeconds > 0)
                    || !(hp > 0)
                    || mostAlive < 1
                    || !(hatchSeconds >= 0)
                    || !(holdWithin >= 0)) {
                throw new IllegalArgumentException("a homing missile turns, lives, has HP and a cap");
            }
        }
    }

    /** M7 part F: this attack firing homing missiles ({@code withHoming}); empty for bullets. */
    public EnemyGun withHoming(Optional<HomingSpec> withHoming) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tellSeconds,
                reaim,
                withHoming,
                arcFacingRadians,
                aimedShards);
    }

    /** An attack without M7 part E's pass trigger (every unit before the Ghost Drone). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds,
            boolean up,
            Optional<SpiralSpec> spiral) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                Double.NaN,
                0,
                false);
    }

    /**
     * M7 part E: this aimed burst fired by a pass trigger (design/enemies/air/ghost-drone): once per pass within
     * {@code withinX} px of the ship's x, {@code tell} s after its {@link SimEvents.Type#EYE_GLINT}, each shot
     * re-aimed when {@code reaimed}; see {@link #passWithinX()}.
     */
    public EnemyGun withPassTrigger(double withinX, double tell, boolean reaimed) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                withinX,
                tell,
                reaimed,
                homing,
                arcFacingRadians,
                aimedShards);
    }

    /**
     * M8 part C: this attack with a tell of {@code tell} s before each volley (design/enemies/naval/eel-swarm, the
     * stripe: a chain head's attack, {@link EnemySpec.ChainSpec#headAttacks()}); see {@link #tellSeconds()}.
     */
    public EnemyGun withTell(double tell) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                spiral,
                passWithinX,
                tell,
                reaim,
                homing,
                arcFacingRadians,
                aimedShards);
    }

    /** M7 part E: whether it fires by a pass trigger ({@link #passWithinX()} is a number). */
    public boolean passTrigger() {
        return !Double.isNaN(passWithinX);
    }

    /** An attack without a spiral (every unit before M7 part E). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds,
            boolean up) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                Optional.empty());
    }

    /**
     * M7 part E, a spiral gun on a unit that is not a boss (design/enemies/ground/dust-devil, the grit spiral):
     * {@code arms} arms evenly round, turning at {@code turnRadiansPerSecond} (negative: clockwise), each firing
     * a bullet every {@code intervalSeconds} while the unit's vortex is open; the first volley goes at the ship
     * as it opens.
     */
    public record SpiralSpec(int arms, double turnRadiansPerSecond, double intervalSeconds) {
        public SpiralSpec {
            if (arms < 1 || !(intervalSeconds > 0) || !Double.isFinite(turnRadiansPerSecond)) {
                throw new IllegalArgumentException("a spiral has arms and fires at an interval: " + arms);
            }
        }
    }

    /** M7 part E: this attack as a spiral gun ({@code spiral}); empty for an attack without one. */
    public EnemyGun withSpiral(Optional<SpiralSpec> withSpiral) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                up,
                withSpiral,
                passWithinX,
                tellSeconds,
                reaim,
                homing,
                arcFacingRadians,
                aimedShards);
    }

    /** An attack that is not fired straight up (every unit but the Wraith). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar,
            double burstGapSeconds) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                burstGapSeconds,
                false);
    }

    /** An attack with the default burst gap, {@link #BURST_GAP_SECONDS} (every unit before M5 part D). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine,
            Optional<MortarSpec> mortar) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                mortar,
                BURST_GAP_SECONDS);
    }

    /**
     * The gap between the shots of a burst. "Quick succession" in the attack vocabulary has no
     * number yet; this is a first value to tune.
     */
    public static final double BURST_GAP_SECONDS = 0.15;

    /** The hit box of a {@code small} enemy bullet (the 9 px orb or the 5x13 needle; no number in the design yet). */
    public static final Hitbox BULLET = new Hitbox(6, 6);

    /**
     * A spore mine: it drifts at {@code drift} px/s in a random direction, arms (rises to the player
     * plane, where it can be hit and burst on contact) after {@code armSeconds}, and after
     * {@code lifeSeconds} bursts into a ring of {@code ring} bullets at the attack's bullet speed,
     * or just fades when it never {@code bursts}; destroyed it pays {@code credits}.
     *
     * @param ringDamage the ring bullets' damage; a contact burst deals the attack's damage
     */
    public record MineSpec(
            double armSeconds,
            double lifeSeconds,
            double drift,
            double hp,
            int ring,
            double ringDamage,
            boolean bursts,
            int credits,
            Optional<LineMine> line) {
        /** A spore's hit box. */
        public static final Hitbox BOX = new Hitbox(10, 10);

        public MineSpec {
            java.util.Objects.requireNonNull(line);
            if (line.isPresent() && drift != 0) {
                throw new IllegalArgumentException("a line mine holds its place in the field: it does not drift");
            }
        }

        /** A spore: no line mine's rules (every mine before M9 part C). */
        public MineSpec(
                double armSeconds,
                double lifeSeconds,
                double drift,
                double hp,
                int ring,
                double ringDamage,
                boolean bursts,
                int credits) {
            this(armSeconds, lifeSeconds, drift, hp, ring, ringDamage, bursts, credits, Optional.empty());
        }

        /** M9 part C: this mine with a line mine's rules ({@link LineMine}); empty for a spore. */
        public MineSpec withLine(Optional<LineMine> withLine) {
            return new MineSpec(armSeconds, lifeSeconds, drift, hp, ring, ringDamage, bursts, credits, withLine);
        }
    }

    /**
     * M9 part C (design/enemies, Mine lines; design/enemies/space/minelayer; Level 30's C1 a and C2 a): a mine of a
     * laid line. It holds its place in the field and comes down with the scroll (no drift). Armed, while the ship's
     * centre or Rook's is within {@code proximity} px it blinks {@code blinkSeconds}, then bursts: its ring (as every
     * ring, none while the ship is within the bullets' no-fire distance) and a blast of {@code blastDamage} on the ship,
     * Rook and the Bulwark where their hulls touch the disc of {@code blastRadius} px round it (0: no blast). Touched
     * while armed it bursts at once with its ring (the contact deals the layer's attack damage). Shot (or blasted by a
     * special) it bursts with its ring and sets off the mines of any line within {@code chainRadius} px, hop by hop
     * {@code hopSeconds} apart; a set-off mine bursts without a ring and sets off its own neighbours (0: no chain).
     */
    public record LineMine(
            double proximity,
            double blinkSeconds,
            double blastRadius,
            double blastDamage,
            double chainRadius,
            double hopSeconds) {
        public LineMine {
            if (!(proximity > 0)
                    || !(blinkSeconds >= 0)
                    || !(blastRadius >= 0)
                    || !(blastDamage >= 0)
                    || !(chainRadius >= 0)
                    || !(hopSeconds > 0)) {
                throw new IllegalArgumentException("a line mine has a proximity, a blink, a blast, a chain and a hop");
            }
        }
    }

    /**
     * A mortar's lob: a blob (not shootable) flies {@code flightSeconds} to where the player was at
     * launch, its lime marker showing there all the while; it lands and bursts into a ring of
     * {@code ring} bullets of {@code ringDamage}, and a ship within {@code impactRadius} px of the
     * point takes the attack's damage (the direct hit).
     */
    public record MortarSpec(double flightSeconds, double impactRadius, int ring, double ringDamage) {}

    /** An attack without a mortar. */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians,
            Optional<MineSpec> mine) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                mine,
                Optional.empty());
    }

    /** An attack without a mine (aimed, a burst, a fan or a turret's). */
    public EnemyGun(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle,
            int fan,
            double spreadRadians,
            double turnRate,
            double arcRadians) {
        this(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                fan,
                spreadRadians,
                turnRate,
                arcRadians,
                Optional.empty());
    }

    /** An aimed attack that aims at once in every direction. */
    public static EnemyGun aimed(
            double intervalSeconds,
            double firstShotDelay,
            int burst,
            double bulletSpeed,
            double damage,
            boolean leadsTargetInCircle) {
        return new EnemyGun(
                intervalSeconds,
                firstShotDelay,
                burst,
                bulletSpeed,
                damage,
                leadsTargetInCircle,
                1,
                0,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }
}
