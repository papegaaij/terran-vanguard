package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M9 part H, step H1a (design/enemies/bosses/iron-sovereign; round 60 Q7 a): a ringed arena boss's rings at one
 * difficulty, run by its {@link RingArena} ({@link BossSpec#rings()}). Concentric rings turn round the boss's centre
 * ({@link BossSpec#x()}, {@link BossSpec#hoverY()}); each carries its batteries (boss parts) at even spacing, their
 * mounts turning with it; a battery fires only while its mount lies inside the <b>fire arc</b>, an arc of {@code
 * arcRadians} centred on the bearing from the centre to the ship, that bearing clamped to {@code bearing}. A ring
 * whose every battery is dead <b>locks</b>: it grinds to a halt over {@code grindSeconds}; the boss's phase follows
 * the count of locked rings ({@code phaseLocks}) in any order, and the rings still turning speed up by {@code
 * speedUps}. The boss needs an arena without lanes ({@link BossSpec.Arena}: its parts' layers), no stride, breach, coil
 * or phase moves.
 *
 * <p>Angles are radians clockwise (as seen on the screen) from straight down the screen, as a sweep's heading: a mount
 * at angle {@code a} on a ring of radius {@code r} lies at offset ({@code -r sin a}, {@code -r cos a}) from the centre
 * (y up). The design's bearings (90° straight down, measured clockwise from the right) are these plus 90°: its clamp
 * 30°–150° is {@code [-60°, 60°]} here.
 *
 * @param rings the rings in the order they light up in the intro and cook off in the death (the outer first)
 * @param arcRadians the fire arc's width (the design's 120°; easy 90°, hard 150°)
 * @param bearing the clamp on the arc's centre, radians clockwise from straight down
 * @param spinUpSeconds from the engagement (the scroll halted) until every ring turns at its speed: ring {@code i}
 *     lights at {@code i × spinUpSeconds ÷ rings} and speeds up evenly from rest until the spin-up's end; the boss is
 *     invulnerable and silent until then; a stepping ring takes its first step (after its tell) at the end
 * @param grindSeconds a locked ring slows evenly from its speed at the lock to rest over this time
 * @param speedUps the turning rings' speed factor by the count of locked rings (index 0: none locked, 1.0); a missing
 *     entry is the last one's. A stepping ring's step takes its time ÷ the factor, its hold unchanged
 * @param phaseLocks per boss phase, the count of locked rings it ends at (0: it does not end on locks); a lock phase
 *     ends as soon as that many rings are locked, whichever rings they are
 * @param cookoffSeconds the death: the batteries cook off one after the other in the rings' order (each ring's in its
 *     parts' order), this far apart, from the kill ({@link SimEvents.Type#RING_COOKOFF})
 * @param core M9 part H, step H1b: the core's opening, shield, fan glow, spiral cycle, rail and bays ({@link Core},
 *     {@link RingCore}); empty: a plain part
 */
public record RingSpec(
        List<Ring> rings,
        double arcRadians,
        BossSpec.Clamp bearing,
        double spinUpSeconds,
        double grindSeconds,
        List<Double> speedUps,
        List<Integer> phaseLocks,
        double cookoffSeconds,
        Optional<Core> core) {
    /** At most this many rings. */
    public static final int MAX_RINGS = 3;

    /** Step H1a's rings: no core block. */
    public RingSpec(
            List<Ring> rings,
            double arcRadians,
            BossSpec.Clamp bearing,
            double spinUpSeconds,
            double grindSeconds,
            List<Double> speedUps,
            List<Integer> phaseLocks,
            double cookoffSeconds) {
        this(
                rings,
                arcRadians,
                bearing,
                spinUpSeconds,
                grindSeconds,
                speedUps,
                phaseLocks,
                cookoffSeconds,
                Optional.empty());
    }

    /** M9 part H, step H1b: these rings with the core block {@code withCore}. */
    public RingSpec withCore(Optional<Core> withCore) {
        return new RingSpec(
                rings,
                arcRadians,
                bearing,
                spinUpSeconds,
                grindSeconds,
                speedUps,
                phaseLocks,
                cookoffSeconds,
                withCore);
    }

    public RingSpec {
        rings = List.copyOf(rings);
        speedUps = List.copyOf(speedUps);
        phaseLocks = List.copyOf(phaseLocks);
        if (rings.isEmpty() || rings.size() > MAX_RINGS) {
            throw new IllegalArgumentException("a ringed boss has 1 to " + MAX_RINGS + " rings: " + rings.size());
        }
        if (!(arcRadians > 0) || arcRadians > 2 * StrictMath.PI) {
            throw new IllegalArgumentException("a fire arc is wider than 0 and at most a full turn: " + arcRadians);
        }
        if (!(spinUpSeconds >= 0) || !(grindSeconds >= 0) || !(cookoffSeconds >= 0)) {
            throw new IllegalArgumentException("the spin-up, the grind and the cook-offs take 0 s or more");
        }
        if (speedUps.isEmpty() || speedUps.stream().anyMatch(f -> !(f > 0))) {
            throw new IllegalArgumentException("the speed-ups are factors above 0, from no ring locked on");
        }
        java.util.Objects.requireNonNull(core);
    }

    /**
     * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Phases and Tells): the core, boss part {@code part},
     * armoured until the phase that exposes it ({@link BossSpec.Phase#exposes()}). Shut, its {@link BossSpec.Pattern#FAN}
     * attacks glow {@code glowSeconds} before each volley ({@link SimEvents.Type#CORE_GLOW}). In its phase it first
     * opens over {@code openingSeconds} (untouchable, the Smart Bomb included, and silent: the phase's clocks wait);
     * open it carries {@code shield} (a hex shield on the enemy shield rule, {@link PartShields}). Its {@code spiral}
     * attack (-1 none) runs in a cycle from the opening's end: a {@code spiralTellSeconds} tell ({@link
     * SimEvents.Type#CORE_SPIRAL_TELL}), {@code spiralSeconds} on, then rests until the cycle of {@code spiralTellSeconds
     * + spiralSeconds + restSeconds} comes round. Its {@code rail} attack (-1 none; a {@link BossSpec.Pattern#LINE})
     * fires only while the core holds less than {@code railBelow} of its HP. Its {@code bays} launch Dart streams.
     */
    public record Core(
            int part,
            double glowSeconds,
            double openingSeconds,
            Optional<EnemySpec.Shield> shield,
            int spiral,
            double spiralSeconds,
            double restSeconds,
            double spiralTellSeconds,
            int rail,
            double railBelow,
            Optional<Bays> bays,
            Optional<Pool> pool) {
        /** Step H1b's core: no playbook (its spiral cycle, its rail and its bays' timed streams). */
        public Core(
                int part,
                double glowSeconds,
                double openingSeconds,
                Optional<EnemySpec.Shield> shield,
                int spiral,
                double spiralSeconds,
                double restSeconds,
                double spiralTellSeconds,
                int rail,
                double railBelow,
                Optional<Bays> bays) {
            this(
                    part,
                    glowSeconds,
                    openingSeconds,
                    shield,
                    spiral,
                    spiralSeconds,
                    restSeconds,
                    spiralTellSeconds,
                    rail,
                    railBelow,
                    bays,
                    Optional.empty());
        }

        /** Step BOSS70: this core with the playbook {@code withPool}. */
        public Core withPool(Optional<Pool> withPool) {
            return new Core(
                    part,
                    glowSeconds,
                    openingSeconds,
                    shield,
                    spiral,
                    spiralSeconds,
                    restSeconds,
                    spiralTellSeconds,
                    rail,
                    railBelow,
                    bays,
                    withPool);
        }

        public Core {
            java.util.Objects.requireNonNull(shield);
            java.util.Objects.requireNonNull(bays);
            java.util.Objects.requireNonNull(pool);
            if (part < 0
                    || !(glowSeconds >= 0)
                    || !(openingSeconds >= 0)
                    || !(spiralSeconds > 0)
                    || !(restSeconds >= 0)
                    || !(spiralTellSeconds >= 0)
                    || !(railBelow > 0 && railBelow <= 1)) {
                throw new IllegalArgumentException("a core has a part, a glow, an opening, a spiral cycle and a rail"
                        + " share in (0, 1]: " + part + ", " + glowSeconds + ", " + openingSeconds + ", "
                        + spiralSeconds + ", " + restSeconds + ", " + spiralTellSeconds + ", " + railBelow);
            }
        }

        /** The spiral's cycle: its tell, its run and its rest, s. */
        public double cycleSeconds() {
            return spiralTellSeconds + spiralSeconds + restSeconds;
        }
    }

    /**
     * Step BOSS70 (design/enemies/bosses/iron-sovereign, The round 70 rework, decided in round 70): the open core's
     * attacks ({@link RingCore}'s playbook).
     */
    public enum Play {
        /** The core's spiral ({@link Core#spiral()}) for its {@link Core#spiralSeconds()}. */
        SPIRAL,
        /** A fan of rail lines from the core through the ship's spot ({@link Volley}). */
        VOLLEY,
        /** Every rim bay's Dart stream at once ({@link Surge}). */
        SURGE,
        /** A ring of line mines thrown out from the core ({@link MineRing}). */
        MINES,
        /** Dead batteries' wrecks hurled at the ship ({@link WreckThrow}). */
        WRECKS;

        /** Its slug (the data's and the radio's name). */
        public String slug() {
            return switch (this) {
                case SPIRAL -> "spiral";
                case VOLLEY -> "rail-volley";
                case SURGE -> "dart-surge";
                case MINES -> "mine-ring";
                case WRECKS -> "wreck-throw";
            };
        }
    }

    /**
     * Step BOSS70: the open core's playbook. From its opening's end the core rests, then draws its next attack from
     * {@code moves} by their weights with the simulation's seeded random (a replay draws the same), never the one it drew
     * last nor one drawn fewer than its {@code spacing} draws ago; each attack starts with a {@code tellSeconds} tell
     * ({@link SimEvents.Type#CORE_PLAY_TELL}), runs, then the core rests {@code restSeconds} ({@code lowRestSeconds} while
     * it holds less than {@code lowBelow} of its HP) before the next draw. With a pool the core's timed spiral cycle, its
     * rail below its share and its bays' timed streams give way: its spiral and its bays fire only in the playbook.
     */
    public record Pool(
            double tellSeconds,
            double restSeconds,
            double lowRestSeconds,
            double lowBelow,
            List<Move> moves,
            Optional<Volley> volley,
            Optional<Surge> surge,
            Optional<MineRing> mines,
            Optional<WreckThrow> wrecks) {
        public Pool {
            moves = List.copyOf(moves);
            java.util.Objects.requireNonNull(volley);
            java.util.Objects.requireNonNull(surge);
            java.util.Objects.requireNonNull(mines);
            java.util.Objects.requireNonNull(wrecks);
            if (!(tellSeconds >= 0)
                    || !(restSeconds >= 0)
                    || !(lowRestSeconds >= 0)
                    || !(lowBelow >= 0 && lowBelow <= 1)) {
                throw new IllegalArgumentException("a playbook has a tell, a rest, a low rest and its share: "
                        + tellSeconds + ", " + restSeconds + ", " + lowRestSeconds + ", " + lowBelow);
            }
            if (moves.size() < 2) {
                throw new IllegalArgumentException("a playbook draws from two attacks or more (never one twice)");
            }
            java.util.Set<Play> seen = java.util.EnumSet.noneOf(Play.class);
            for (Move move : moves) {
                if (!seen.add(move.play())) {
                    throw new IllegalArgumentException("a playbook names each attack once: " + move.play());
                }
                boolean given =
                        switch (move.play()) {
                            case SPIRAL -> true;
                            case VOLLEY -> volley.isPresent();
                            case SURGE -> surge.isPresent();
                            case MINES -> mines.isPresent();
                            case WRECKS -> wrecks.isPresent();
                        };
                if (!given) {
                    throw new IllegalArgumentException("a playbook's " + move.play() + " has its block");
                }
            }
        }

        /** Its move for {@code play}; null when it is not in the pool. */
        public Move move(Play play) {
            for (Move move : moves) {
                if (move.play() == play) {
                    return move;
                }
            }
            return null;
        }
    }

    /**
     * Step BOSS70: an attack in the playbook, drawn by its {@code weight} and at most every {@code spacing}-th draw (2:
     * never twice running, the least; 3: at most every third draw).
     */
    public record Move(Play play, double weight, int spacing) {
        public Move {
            java.util.Objects.requireNonNull(play);
            if (!(weight > 0) || spacing < 2) {
                throw new IllegalArgumentException("a move has a weight above 0 and a spacing of 2 or more: " + play);
            }
        }
    }

    /**
     * Step BOSS70: the rail volley: {@code lines} rail lines of the boss's line attack {@code attack} (its telegraph, live
     * time, band and damage), from {@code fromPx} out of the core's centre, fanned {@code fanRadians} apart and the middle
     * one through the ship's spot at the telegraph's start ({@link CoreVolley}).
     */
    public record Volley(int attack, int lines, double fanRadians, double fromPx) {
        public Volley {
            if (attack < 0 || lines < 1 || lines > CoreVolley.MOST || !(fanRadians >= 0) || !(fromPx >= 0)) {
                throw new IllegalArgumentException("a volley has a line attack, 1 to " + CoreVolley.MOST
                        + " lines, a fan and an origin: " + lines);
            }
        }
    }

    /** Step BOSS70: the Dart surge: every rim bay at once launches a stream of {@code stream} (the bays' launcher). */
    public record Surge(int stream) {
        public Surge {
            if (stream < 1) {
                throw new IllegalArgumentException("a surge's streams carry a unit or more: " + stream);
            }
        }
    }

    /**
     * Step BOSS70: the mine ring: {@code count} line mines of {@code gun} (the Minelayer's: its arming, proximity, blink,
     * blast, ring and chain) thrown out from the core's centre at even bearings (a seeded turn), each to a seeded
     * distance from {@code nearPx} to {@code farPx}, over {@code throwSeconds}; one untouched is gone after {@code
     * lifeSeconds}.
     */
    public record MineRing(
            EnemyGun gun, int count, double nearPx, double farPx, double throwSeconds, double lifeSeconds) {
        public MineRing {
            java.util.Objects.requireNonNull(gun);
            if (gun.mine().isEmpty() || gun.mine().get().line().isEmpty()) {
                throw new IllegalArgumentException("a mine ring throws line mines");
            }
            if (count < 1
                    || !(nearPx >= 0)
                    || !(farPx >= nearPx)
                    || !(throwSeconds > 0)
                    || !(lifeSeconds > throwSeconds)) {
                throw new IllegalArgumentException("a mine ring has a count, its distances, a throw and a life");
            }
        }
    }

    /**
     * Step BOSS70: the wreck throw: {@code fewest} to {@code most} (seeded) wrecks of the dead batteries on locked rings
     * (seeded, any ring and side) are torn off and hurled at the ship's spot one after another, {@code gapSeconds} apart,
     * each glowing that long before it flies (the first in the play's tell); a wreck is a shootable missile of {@code
     * gun} (its speed, its contact damage, its homing spec's HP and life, no turn; at most its {@code mostAlive} in the
     * air: a wreck due while that many fly stays on its socket). A thrown wreck leaves an empty socket; with none left the
     * throw is out of the pool.
     */
    public record WreckThrow(EnemyGun gun, int fewest, int most, double gapSeconds) {
        public WreckThrow {
            java.util.Objects.requireNonNull(gun);
            if (gun.homing().isEmpty()) {
                throw new IllegalArgumentException("a thrown wreck flies as a missile");
            }
            if (fewest < 1 || most < fewest || !(gapSeconds > 0)) {
                throw new IllegalArgumentException("a wreck throw hurls 1 or more wrecks, a gap apart");
            }
        }
    }

    /**
     * M9 part H, step H1b (design/enemies/bosses/iron-sovereign, Phases and Dart Drones): the open core's {@code count}
     * rim bays, {@code radius} px from the centre, the first at {@code startRadians} (clockwise from straight down) and
     * the others evenly spaced after it. From the opening's end, after the {@code launcher}'s first time and then every
     * its {@code everySeconds}, one bay launches a stream of the launcher's {@code stream} units, its {@code gapSeconds}
     * apart, at its speed outward through the bay (spread over its spread), each flying straight its {@code
     * straightSeconds} before its chase: the bay nearest the ship that lies farther than {@code skipWithinPx} from it
     * (none: the turn passes). The bay's doors open {@code tellSeconds} before the stream ({@link
     * SimEvents.Type#BAY_TELL}). A unit is skipped while the launcher's {@code mostAlive} of the boss's own are alive.
     * The units count among the level's enemies (as a boss's stream's do, unless the kill ratio leaves the bosses out),
     * not its density.
     */
    public record Bays(
            int count,
            double radius,
            double startRadians,
            EnemySpec.Launcher launcher,
            double tellSeconds,
            double skipWithinPx) {
        public Bays {
            java.util.Objects.requireNonNull(launcher);
            if (count < 1 || !(radius >= 0) || !(tellSeconds >= 0) || !(skipWithinPx >= 0)) {
                throw new IllegalArgumentException("bays have a count, a radius, a tell and a skip distance");
            }
            if (tellSeconds > launcher.everySeconds() - (launcher.stream() - 1) * launcher.gapSeconds()) {
                throw new IllegalArgumentException("a bay's tell fits between two streams: " + tellSeconds);
            }
        }

        /** Bay {@code k}'s angle round the centre, radians clockwise from straight down. */
        public double radians(int k) {
            return startRadians + 2 * StrictMath.PI * k / count;
        }
    }

    /**
     * A ring: its {@code radius} (px from the centre to the mounts), its turning {@code direction} (1 clockwise on the
     * screen, -1 the other way) at {@code radiansPerSecond} (unless it steps), its first battery's angle at rest {@code
     * startRadians} (the others evenly spaced clockwise after it), its batteries' {@code parts}, the boss attack its
     * batteries fire ({@code attack}, -1 none: a stepping ring's attack fires once at each hold's start, from its armed
     * batteries; any other ring's on its own interval, gated by the arc) and {@code step}, empty for a ring that turns
     * evenly.
     */
    public record Ring(
            String name,
            double radius,
            int direction,
            double radiansPerSecond,
            double startRadians,
            List<Integer> parts,
            int attack,
            Optional<Step> step) {
        public Ring {
            parts = List.copyOf(parts);
            if (!(radius > 0) || (direction != 1 && direction != -1) || !(radiansPerSecond >= 0)) {
                throw new IllegalArgumentException(name + ": a ring has a radius, a direction (1 or -1) and a speed");
            }
            if (parts.isEmpty()) {
                throw new IllegalArgumentException(name + ": a ring carries batteries");
            }
        }

        /** Battery {@code k}'s angle on the ring at rest, radians clockwise from straight down. */
        public double mountRadians(int k) {
            return startRadians + 2 * StrictMath.PI * k / parts.size();
        }
    }

    /**
     * A stepping ring's cycle (the inner ring's ratchet): a turn of {@code radians} in {@code seconds} (at an even
     * speed), then a hold of {@code holdSeconds} whose last {@code tellSeconds} are the next step's tell (the teeth
     * flash, {@link SimEvents.Type#RING_STEP_TELL}).
     */
    public record Step(double radians, double seconds, double holdSeconds, double tellSeconds) {
        public Step {
            if (!(radians > 0)
                    || !(seconds > 0)
                    || !(holdSeconds > 0)
                    || !(tellSeconds >= 0)
                    || tellSeconds > holdSeconds) {
                throw new IllegalArgumentException("a step turns in its time, then holds, its tell inside the hold: "
                        + radians + ", " + seconds + ", " + holdSeconds + ", " + tellSeconds);
            }
        }
    }

    /** The speed factor with {@code locked} rings locked. */
    public double speedUp(int locked) {
        return speedUps.get(Math.min(locked, speedUps.size() - 1));
    }

    /** The count of locked rings phase {@code phase} ends at; 0: it does not end on locks. */
    public int locks(int phase) {
        return phase < phaseLocks.size() ? phaseLocks.get(phase) : 0;
    }

    /**
     * The checks against its boss: a lock count per phase, rising, within the rings, the last phase not on locks;
     * every battery a distinct part of the boss; an attack an attack of the boss (a stepping ring's a sweep).
     */
    void check(String name, List<BossSpec.Phase> phases, List<BossSpec.Attack> attacks) {
        if (phaseLocks.size() != phases.size()) {
            throw new IllegalArgumentException(name + ": a lock count (0: none) for each phase");
        }
        int last = 0;
        for (int f = 0; f < phaseLocks.size(); f++) {
            int n = phaseLocks.get(f);
            if (n < 0 || n > rings.size() || (n > 0 && n <= last)) {
                throw new IllegalArgumentException(
                        name + ": the phases end on more locked rings each, at most the rings: " + phaseLocks);
            }
            last = Math.max(last, n);
        }
        if (phaseLocks.getLast() != 0) {
            throw new IllegalArgumentException(name + ": the last phase does not end on locks");
        }
        core.ifPresent(c -> checkCore(name, c, phases, attacks));
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (Ring ring : rings) {
            for (int p : ring.parts()) {
                if (p < 0 || !seen.add(p)) {
                    throw new IllegalArgumentException(name + ": every battery is a part on one ring only: " + p);
                }
            }
            if (ring.attack() >= attacks.size() || ring.attack() < -1) {
                throw new IllegalArgumentException(name + ": " + ring.name() + "'s attack is one of its attacks");
            }
            if (ring.step().isPresent()
                    && ring.attack() >= 0
                    && attacks.get(ring.attack()).pattern() != BossSpec.Pattern.SWEEP) {
                throw new IllegalArgumentException(name + ": a stepping ring's batteries sweep in its holds");
            }
            if (ring.step().isPresent() && ring.attack() >= 0) {
                EnemySpec.Sweep sweep = attacks.get(ring.attack()).sweep().orElseThrow();
                if (sweep.telegraphSeconds() + sweep.sweepSeconds()
                        > ring.step().get().holdSeconds()) {
                    throw new IllegalArgumentException(
                            name + ": " + ring.name() + "'s sweep ends inside its hold (its wedge never moves)");
                }
            }
        }
    }

    /** Step H1b: the core is a part no ring carries, exposed by a phase; its spiral a spiral, its rail a line. */
    private void checkCore(String name, Core core, List<BossSpec.Phase> phases, List<BossSpec.Attack> attacks) {
        for (Ring ring : rings) {
            if (ring.parts().contains(core.part())) {
                throw new IllegalArgumentException(name + ": the core is no battery: " + core.part());
            }
        }
        if (phases.stream().noneMatch(phase -> phase.exposes().contains(core.part()))) {
            throw new IllegalArgumentException(name + ": a phase exposes the core (it opens then)");
        }
        if (core.spiral() >= attacks.size()
                || core.spiral() < -1
                || (core.spiral() >= 0 && attacks.get(core.spiral()).pattern() != BossSpec.Pattern.SPIRAL)) {
            throw new IllegalArgumentException(name + ": the core's spiral is one of its spirals");
        }
        if (core.pool().isPresent()) {
            RingSpec.Pool pool = core.pool().get();
            if (pool.move(Play.SPIRAL) != null && core.spiral() < 0) {
                throw new IllegalArgumentException(name + ": the playbook's spiral is the core's spiral");
            }
            if (pool.volley().isPresent()) {
                int a = pool.volley().get().attack();
                if (a >= attacks.size() || attacks.get(a).pattern() != BossSpec.Pattern.LINE) {
                    throw new IllegalArgumentException(name + ": the playbook's volley fires one of its lines");
                }
            }
            if (pool.surge().isPresent() && core.bays().isEmpty()) {
                throw new IllegalArgumentException(name + ": the playbook's surge needs the core's bays");
            }
        }
        if (core.rail() >= attacks.size()
                || core.rail() < -1
                || (core.rail() >= 0 && attacks.get(core.rail()).pattern() != BossSpec.Pattern.LINE)) {
            throw new IllegalArgumentException(name + ": the core's rail is one of its lines");
        }
    }

    /** Step H1b: the phase that exposes the core (it opens then); -1 without a core. */
    public int corePhase(List<BossSpec.Phase> phases) {
        if (core.isEmpty()) {
            return -1;
        }
        for (int f = 0; f < phases.size(); f++) {
            if (phases.get(f).exposes().contains(core.get().part())) {
                return f;
            }
        }
        return -1;
    }
}
