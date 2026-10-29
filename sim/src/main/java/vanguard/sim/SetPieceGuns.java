package vanguard.sim;

import java.util.List;

/**
 * A set piece's guns and a boss's phases (out of {@link SetPiece}): a pass-flying set piece's part guns; a boss's
 * phase machine (its start, move, engagement, end on its parts, its timer or its arena, the timeouts) and its attacks
 * (volleys, bursts, rotation, staggering, alternation, spirals, streams) through the {@link BossActions}. Its arrays
 * are allocated once; the coordinator hashes its fields.
 */
final class SetPieceGuns {
    private final SetPiece piece;
    private final SetPieceSpec spec;
    private final BossSpec boss;
    private final SetPieceParts parts;
    private final SetPieceMotion motion;
    private final BossChains chains;
    private final BossWindows windows;
    private final BossHatches hatches;
    private final BossSweeps sweeps;
    private final BossAimTurns aimTurns;
    private final SlamArena arena;
    private final StrideArena stride;
    private final BreachArena breach;
    private final CoilArena coil;
    private final RingArena ring;
    /** M9 part H, step H1b: the rail lines and the part shields; null without. */
    private final BossLines lines;

    private final PartShields shields;
    /** {@link SetPieceParts#partHp}: the guns of wrecked parts are silent. */
    private final double[] partHp;

    // A set piece's part guns (on its passes).
    final int[] volleyTicks;
    final int[] burstLeft;
    final int[] burstTicks;

    // A boss's attacks; the arrays are empty for a set piece flying passes.
    final int[] attackTicks;
    final int[] attackBurstLeft;
    final int[] attackBurstTicks;
    final int[] attackPart;
    final int[] attackNext;
    /** Per attack, a spiral's arm angle and its steps to the next bullet. */
    final double[] spiralAngles;

    final int[] spiralTicks;
    /** Whether its phases run (from its arrival or its settle). */
    boolean engaged;
    /** Steps since the phase engaged (after its move): its timeout's clock. */
    int phaseTicks;
    /** Bit f set: phase f ended on its timer. */
    long timeouts;

    int phase;
    int turn;
    int turnTicks;
    int streamTicks;
    int streamLeft;
    int streamUnitTicks;
    int streamUnits;

    SetPieceGuns(SetPiece piece, SetPieceSpec spec, BossSpec boss) {
        this.piece = piece;
        this.spec = spec;
        this.boss = boss;
        parts = piece.parts;
        motion = piece.motion;
        chains = piece.chains;
        windows = piece.windows;
        hatches = piece.hatches;
        sweeps = piece.sweeps;
        aimTurns = piece.aimTurns;
        arena = piece.arena;
        stride = piece.stride;
        breach = piece.breach;
        coil = piece.coil;
        ring = piece.ring;
        lines = piece.lines;
        shields = piece.shields;
        partHp = parts.partHp;
        int count = spec.parts().size();
        volleyTicks = new int[count];
        burstLeft = new int[count];
        burstTicks = new int[count];
        int attacks = boss == null ? 0 : boss.attacks().size();
        attackTicks = new int[attacks];
        attackBurstLeft = new int[attacks];
        attackBurstTicks = new int[attacks];
        attackPart = new int[attacks];
        attackNext = new int[attacks];
        spiralAngles = new double[attacks];
        spiralTicks = new int[attacks];
    }

    /** Back to the level start: every gun silent, a boss in its first phase, not engaged. */
    void reset() {
        for (int p = 0; p < volleyTicks.length; p++) {
            volleyTicks[p] = 0;
            burstLeft[p] = 0;
            burstTicks[p] = 0;
        }
        if (boss != null) {
            phase = 0;
            engaged = false;
            phaseTicks = 0;
            timeouts = 0;
            java.util.Arrays.fill(spiralAngles, 0);
            java.util.Arrays.fill(spiralTicks, 0);
            turn = -1;
            turnTicks = 0;
            streamTicks = 0;
            streamLeft = 0;
            streamUnitTicks = 0;
            streamUnits = 0;
            java.util.Arrays.fill(attackTicks, 0);
            java.util.Arrays.fill(attackBurstLeft, 0);
            java.util.Arrays.fill(attackBurstTicks, 0);
            java.util.Arrays.fill(attackPart, -1);
            java.util.Arrays.fill(attackNext, 0);
        }
    }

    /** It reached the player's layer: every living part's gun starts on its first-shot time. */
    void arm() {
        for (int p = 0; p < partHp.length; p++) {
            PartSpec part = spec.parts().get(p);
            volleyTicks[p] = SimStep.ticks(part.firstShotSeconds()) + 1;
            burstLeft[p] = 0;
            burstTicks[p] = 0;
        }
    }

    /** As {@link SetPiece#trigger}. */
    boolean trigger(int p) {
        PartSpec part = spec.parts().get(p);
        if (!motion.present || !motion.onPlane || partHp[p] <= 0 || part.gun().isEmpty()) {
            return false;
        }
        EnemyGun gun = part.gun().get();
        if (burstLeft[p] > 0) {
            if (--burstTicks[p] > 0) {
                return false;
            }
            burstLeft[p]--;
            burstTicks[p] = SimStep.ticks(EnemyGun.BURST_GAP_SECONDS);
            return true;
        }
        if (--volleyTicks[p] > 0) {
            return false;
        }
        volleyTicks[p] = SimStep.ticks(gun.intervalSeconds());
        burstLeft[p] = gun.burst() - 1;
        burstTicks[p] = SimStep.ticks(EnemyGun.BURST_GAP_SECONDS);
        return true;
    }

    /** Its phases run from now: the current phase starts. */
    void engage() {
        engaged = true;
        startPhase();
    }

    /** As {@link SetPiece#act}. */
    void act(double shipX, double shipY, BossActions actions, boolean firing) {
        if (ring != null && piece.destroyed()) {
            // M9 part H: the batteries cook off after the kill.
            ring.dying(actions);
            return;
        }
        if (boss == null || !motion.present || piece.destroyed()) {
            return;
        }
        chains.bend(
                shipX, shipY, !engaged ? Double.NaN : boss.phases().get(phase).bendRadians());
        if (!engaged) {
            return;
        }
        windows.tick();
        hatches.tick();
        if (shields != null) {
            // M9 part H, step H1b: the part shields refill on the real steps.
            shields.step();
        }
        if (motion.moveTicks >= 0) {
            return;
        }
        sweeps.aimAt(shipX, shipY);
        if (sweeps.any()) {
            sweeps.step(actions);
        }
        phaseTicks++;
        if (ring != null) {
            // M9 part H: the arc follows the ship, the rings turn, and a ring whose last battery died locks; step H1b:
            // the core opens, tells its spiral and launches from its bays.
            ring.step(shipX, shipY, phase, actions, firing);
        }
        if (lines != null) {
            // M9 part H, step H1b: the running rail line goes on; the oldest due one starts when the gate allows.
            lines.step(shipX, shipY, ring, actions);
        }
        while (phase < boss.phases().size() - 1 && phaseOver(boss.phases().get(phase))) {
            if (!partsDown(boss.phases().get(phase))
                    && !arenaOver(boss.phases().get(phase))
                    && (ring == null || !ring.phaseOver(phase))) {
                timeouts |= 1L << phase;
                actions.timeout(phase);
            }
            phase++;
            startPhase();
            actions.phase(phase);
            dropOnStart(actions);
            if (motion.moveTicks >= 0) {
                return;
            }
        }
        BossSpec.Phase current = boss.phases().get(phase);
        if (coil != null) {
            // M8 part H: the coil's events, strikes, fire and sheds on its own clocks.
            coil.act(actions, firing);
            return;
        }
        if (ring != null && ring.silent()) {
            // M9 part H: silent until the rings are up to speed (the phase's clocks start then); step H1b: and while
            // the
            // core opens.
            return;
        }
        if (breach != null) {
            // M7 part H: the breach's events and bite, then the phase's attacks from the open mouth.
            breach.act(actions, firing);
            breachAttacks(current, actions, firing);
            if (current.windows().isPresent()) {
                windows.step(current.windows().get(), shipX, shipY, actions, firing);
            }
            if (current.stream().isPresent()) {
                stream(current.stream().get(), actions, firing);
            }
            return;
        }
        if (stride != null) {
            // M7 part F: the flame cone and the vents after the walk.
            stride.act(phase, actions, firing);
        }
        if (arena != null) {
            boolean fans = arena.step(current, shipX, actions, firing);
            chains.place();
            if (current.arena().surface().isPresent()) {
                // The phase's attacks fire from its surfacing part while it is up, each after its tell.
                if (fans) {
                    for (int i = 0; i < current.attacks().size(); i++) {
                        volley(current.attacks().get(i), current, actions, firing);
                    }
                }
                return;
            }
        }
        if (current.alternate()) {
            alternate(current, actions, firing);
        } else {
            for (int i = 0; i < current.attacks().size(); i++) {
                int a = current.attacks().get(i);
                if (ring == null || !ring.firesAtHolds(a)) {
                    runAttack(a, current, actions, firing);
                }
            }
        }
        if (ring != null) {
            ringHolds(current, actions, firing);
        }
        if (current.windows().isPresent()) {
            windows.step(current.windows().get(), shipX, shipY, actions, firing);
        }
        if (current.stream().isPresent()) {
            stream(current.stream().get(), actions, firing);
        }
    }

    /**
     * M7 part H: while the mouth is open the phase's first attack fires on the breach's ring times and its other
     * attacks half an interval after each (a spiral among them runs on its own clock while the mouth is open).
     */
    private void breachAttacks(BossSpec.Phase current, BossActions actions, boolean firing) {
        int fires = breach.fires();
        boolean first = true;
        for (int i = 0; i < current.attacks().size(); i++) {
            int a = current.attacks().get(i);
            if (boss.attacks().get(a).pattern() == BossSpec.Pattern.SPIRAL) {
                // Step H3: from the glow's end while open, its first bullets at once.
                if (breach.spiralStarts()) {
                    spiralTicks[a] = 1;
                }
                if (breach.spiralling()) {
                    runAttack(a, current, actions, firing);
                }
            } else if (breach.timed(a)) {
                if ((fires & (first ? BreachArena.RING : BreachArena.OTHERS)) != 0) {
                    volley(a, current, actions, firing);
                }
                first = false;
            }
        }
        int burst = breach.ventBurst();
        if (burst >= 0) {
            // Step H3: the risen spine vents' aimed burst on its own clock while the mouth is open.
            if (breach.opened()) {
                attackTicks[burst] = SimStep.ticks(BreachArena.VENT_FIRST_SECONDS) + 1;
                attackBurstLeft[burst] = 0;
            }
            if (breach.ventsFiring()) {
                runAttack(burst, current, actions, firing);
            } else {
                attackBurstLeft[burst] = 0;
            }
        }
    }

    /**
     * M9 part H: a stepping ring's attack fires once at each hold's start, from its armed batteries (the laser
     * batteries' sweep: its wedge never moves under it), while the phase has it.
     */
    private void ringHolds(BossSpec.Phase current, BossActions actions, boolean firing) {
        for (int i = 0; i < ring.ringCount(); i++) {
            int a = ring.holdAttack(i);
            if (a < 0 || !ring.holdStarted(i) || !firing || !inPhase(current, a)) {
                continue;
            }
            BossSpec.Attack attack = boss.attacks().get(a);
            List<Integer> batteries = ring.spec().rings().get(i).parts();
            for (int k = 0; k < batteries.size(); k++) {
                int p = batteries.get(k);
                if (ring.armed(p)) {
                    fireFrom(a, p, attack, actions);
                }
            }
        }
    }

    private static boolean inPhase(BossSpec.Phase current, int a) {
        for (int i = 0; i < current.attacks().size(); i++) {
            if (current.attacks().get(i) == a) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the phase is over: its parts are down, its timer ran out, (M5 part E) its slams or its HP share, or (M9
     * part H) its count of locked rings.
     */
    private boolean phaseOver(BossSpec.Phase current) {
        if (coil != null && coil.holdsPhase(phase)) {
            // M8 part H: the dive's phase does not end before its dive is over.
            return false;
        }
        if (ring != null && ring.phaseOver(phase)) {
            return true;
        }
        return partsDown(current)
                || arenaOver(current)
                || (current.timed() && phaseTicks >= SimStep.ticks(current.seconds()));
    }

    /**
     * M5 part E: whether an arena phase is over: it struck its slams (or, ending on slams, every arm
     * is severed), or its parts hold less than its share of their HP.
     */
    private boolean arenaOver(BossSpec.Phase current) {
        if (arena == null) {
            return false;
        }
        BossSpec.PhaseArena keys = current.arena();
        if (keys.slams() > 0 && (arena.slams() >= keys.slams() || !arena.armsLeft())) {
            return true;
        }
        if (!keys.onShare()) {
            return false;
        }
        double left = 0;
        double full = 0;
        for (int i = 0; i < keys.belowParts().size(); i++) {
            int p = keys.belowParts().get(i);
            left += Math.max(0, partHp[p]);
            full += spec.parts().get(p).hp();
        }
        return left < keys.below() * full;
    }

    /** M5 part E: a phase that drops a pickup as it begins drops it under its surfacing part (or the centre). */
    private void dropOnStart(BossActions actions) {
        if (arena == null) {
            return;
        }
        BossSpec.PhaseArena keys = boss.phases().get(phase).arena();
        if (keys.drop().isPresent() && breach != null) {
            // M7 part H, step H3: in the open field, not at the (maybe buried, edge) mouth.
            actions.drop(keys.drop().get(), breach.dropX(), breach.dropY());
        } else if (keys.drop().isPresent() && coil != null) {
            // M8 part H: in the open field inside the coil, not at the head by the top edge.
            actions.drop(keys.drop().get(), coil.dropX(), coil.dropY());
        } else if (keys.drop().isPresent()) {
            int p = keys.surface().isPresent() ? keys.surface().get().part() : -1;
            actions.drop(keys.drop().get(), p < 0 ? motion.x : parts.partX(p), p < 0 ? motion.y : parts.partY(p));
        }
    }

    /** Whether at most the phase's {@code left} of its parts are alive (never for a phase without parts). */
    private boolean partsDown(BossSpec.Phase current) {
        if (current.untilParts().isEmpty()) {
            return false;
        }
        int alive = 0;
        for (int i = 0; i < current.untilParts().size(); i++) {
            alive += partHp[current.untilParts().get(i)] > 0 ? 1 : 0;
        }
        return alive <= current.left();
    }

    /** A phase starts: its windows close; it begins its move ({@link SetPieceMotion#beginMove}), or engages at once. */
    private void startPhase() {
        windows.closeAll();
        BossSpec.Phase current = boss.phases().get(phase);
        if (arena != null) {
            arena.phaseStarted(current);
        }
        // M5 part H: a sweep under way ends with its phase.
        sweeps.endAll();
        if (motion.beginMove(current)) {
            return;
        }
        armPhase();
    }

    /**
     * A phase engages: its timer starts; after its delay its attacks fire (each a volley one
     * interval later), its alternation takes its first turn, its windows open and its stream sends.
     */
    void armPhase() {
        BossSpec.Phase current = boss.phases().get(phase);
        // Round 40: an aim turn's glow ends with its phase.
        aimTurns.endGlows();
        int delay = SimStep.ticks(current.delaySeconds());
        for (int i = 0; i < current.attacks().size(); i++) {
            int a = current.attacks().get(i);
            BossSpec.Attack attack = boss.attacks().get(a);
            attackTicks[a] = delay + SimStep.ticks(interval(attack, current));
            attackBurstLeft[a] = 0;
            spiralTicks[a] = delay;
            if (attack.stagger()) {
                // M5 part H: a staggered attack's parts take their turns from the first again.
                attackNext[a] = 0;
            }
            if (attack.aimTurns().isPresent()) {
                // Round 40: the first aimed shot one turn after the delay, from the first part.
                aimTurns.arm(a, attack.aimTurns().get(), delay);
            }
        }
        turn = -1;
        turnTicks = Math.max(1, delay);
        streamLeft = 0;
        streamTicks = Math.max(1, delay);
        windows.arm(current, delay);
        phaseTicks = 0;
    }

    /** The phase's attacks in turn: each runs its turn (a ring its interval, a spiral its duration), then hands over. */
    private void alternate(BossSpec.Phase current, BossActions actions, boolean firing) {
        if (--turnTicks <= 0) {
            turn = (turn + 1) % current.attacks().size();
            BossSpec.Attack attack = boss.attacks().get(current.attacks().get(turn));
            turnTicks = Math.max(1, SimStep.ticks(attack.turnSeconds()));
            spiralTicks[current.attacks().get(turn)] = 0;
            if (attack.pattern() != BossSpec.Pattern.SPIRAL) {
                volley(current.attacks().get(turn), current, actions, firing);
            }
        }
        if (turn < 0) {
            return;
        }
        int a = current.attacks().get(turn);
        BossSpec.Attack attack = boss.attacks().get(a);
        if (attack.pattern() == BossSpec.Pattern.SPIRAL) {
            spiral(a, attack, current, actions, firing);
        }
    }

    /** A spiral's step: its arms turn, and each fires a bullet every interval (each spiral on its own state). */
    private void spiral(int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        spiralAngles[a] = Math.IEEEremainder(
                spiralAngles[a] + attack.turnRadiansPerSecond() * SimStep.SECONDS, 2 * StrictMath.PI);
        if (--spiralTicks[a] > 0) {
            return;
        }
        spiralTicks[a] = Math.max(1, SimStep.ticks(attack.gun().intervalSeconds()));
        if (!firing) {
            return;
        }
        List<Integer> firers = firingParts(attack, current);
        for (int i = 0; i < firers.size(); i++) {
            int p = firers.get(i);
            if (partHp[p] <= 0) {
                continue;
            }
            for (int k = 0; k < attack.arms(); k++) {
                actions.bullet(
                        parts.partX(p),
                        parts.partY(p),
                        spiralAngles[a] + 2 * StrictMath.PI * k / attack.arms(),
                        attack.gun().bulletSpeed(),
                        attack.gun().damage());
            }
        }
    }

    /** An attack fired on its own interval: a volley when it is due, then the rest of its burst. */
    private void runAttack(int a, BossSpec.Phase current, BossActions actions, boolean firing) {
        BossSpec.Attack attack = boss.attacks().get(a);
        fireAttack(a, attack, current, actions, firing);
        if (attack.aimTurns().isPresent()) {
            // Round 40: after the regular fire, so a part holds it through its aimed shot's step as well.
            aimTurns.turn(a, attack, current, actions, firing);
        }
    }

    private void fireAttack(
            int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        if (attack.pattern() == BossSpec.Pattern.SPIRAL) {
            if (ring == null || ring.spiralling(a)) {
                // M9 part H, step H1b: the open core's spiral only in its cycle's run.
                spiral(a, attack, current, actions, firing);
            }
            return;
        }
        if (attackBurstLeft[a] > 0) {
            if (--attackBurstTicks[a] <= 0) {
                attackBurstLeft[a]--;
                attackBurstTicks[a] = Math.max(1, SimStep.ticks(attack.burstGapSeconds()));
                shoot(a, attack, current, actions, firing);
            }
        }
        if (--attackTicks[a] > 0) {
            if ((stride != null || ring != null) && attack.gun().homing().isPresent()) {
                // M9 part H, step H1b: a ringed boss's SAM batteries' hatches too (the SAM Nest's tell).
                hatches.tell(attack, current, attackTicks[a], attackNext[a], actions, firing);
            }
            if (ring != null && firing && ring.glowDue(attack, attackTicks[a])) {
                // M9 part H, step H1b: the shut core's slits glow before its fan.
                actions.ring(SimEvents.Type.CORE_GLOW, motion.x, motion.y, a);
            }
            return;
        }
        attackTicks[a] = Math.max(1, SimStep.ticks(interval(attack, current)));
        volley(a, current, actions, firing);
    }

    /**
     * The time between two volleys of {@code attack}: its gun's interval; M5 part H: a staggered
     * attack's turns come that interval ÷ its parts apart.
     */
    private static double interval(BossSpec.Attack attack, BossSpec.Phase current) {
        double interval = attack.gun().intervalSeconds();
        return attack.stagger()
                ? interval / Math.max(1, firingParts(attack, current).size())
                : interval;
    }

    /** The first shot of a volley: a rotating attack picks the next living part; a burst follows. */
    private void volley(int a, BossSpec.Phase current, BossActions actions, boolean firing) {
        BossSpec.Attack attack = boss.attacks().get(a);
        List<Integer> firers = firingParts(attack, current);
        attackPart[a] = -1;
        if (attack.stagger()) {
            // M5 part H: the next part's turn; a destroyed part's turn passes silently.
            int p = firers.get(attackNext[a] % firers.size());
            attackNext[a] = (attackNext[a] + 1) % firers.size();
            if (partHp[p] <= 0) {
                attackBurstLeft[a] = 0;
                return;
            }
            attackPart[a] = p;
        }
        if (attack.rotate()) {
            for (int i = 0; i < firers.size(); i++) {
                int candidate = (attackNext[a] + i) % firers.size();
                if (partHp[firers.get(candidate)] > 0) {
                    attackPart[a] = firers.get(candidate);
                    attackNext[a] = (candidate + 1) % firers.size();
                    break;
                }
            }
            if (attackPart[a] < 0) {
                return;
            }
        }
        attackBurstLeft[a] = attack.pattern() == BossSpec.Pattern.AIMED || attack.pattern() == BossSpec.Pattern.FAN
                ? attack.gun().burst() - 1
                : 0;
        attackBurstTicks[a] = Math.max(1, SimStep.ticks(attack.burstGapSeconds()));
        shoot(a, attack, current, actions, firing);
    }

    /** One shot (or ring) of attack {@code a} from its part, or from every living firing part. */
    private void shoot(int a, BossSpec.Attack attack, BossSpec.Phase current, BossActions actions, boolean firing) {
        if (!firing) {
            return;
        }
        if (attackPart[a] >= 0) {
            // Round 40: a part in its aim turn's glow holds its regular fire.
            if (partHp[attackPart[a]] > 0 && attackPart[a] != aimTurns.part(a)) {
                fireFrom(a, attackPart[a], attack, actions);
            }
            return;
        }
        List<Integer> firers = firingParts(attack, current);
        for (int i = 0; i < firers.size(); i++) {
            int p = firers.get(i);
            if (partHp[p] > 0 && p != aimTurns.part(a)) {
                fireFrom(a, p, attack, actions);
            }
        }
    }

    private void fireFrom(int a, int p, BossSpec.Attack attack, BossActions actions) {
        if (ring != null && (!ring.fires(p) || !ring.attackFires(a))) {
            // M9 part H: a battery fires only while armed (alive, its ring turning, inside the fire arc); step H1b: the
            // core's rail only below its HP share.
            return;
        }
        if (attack.pattern() == BossSpec.Pattern.LINE) {
            // M9 part H, step H1b: its rail line is due; the boss starts one at a time (BossLines).
            lines.request(a, p);
            return;
        }
        if (ring != null && attack.gun().homing().isPresent()) {
            // M9 part H, step H1b: a SAM battery's salvo only through its open hatch (told while armed), under the
            // station's cap of its gun's most missiles alive.
            if (attack.gun().homing().get().hatchSeconds() <= 0 || hatches.open(p)) {
                actions.stationMissile(parts.partX(p), parts.partY(p), attack.gun());
            }
            return;
        }
        if (breach != null && !arena.layer(p).touchable()) {
            // M7 part H, step H3: a breaching boss fires from no buried part (a sunk spine vent).
            return;
        }
        if (stride != null && !parts.partOnField(p)) {
            // M7 part F: a walking boss fires only from parts on the screen.
            return;
        }
        if (attack.pattern() == BossSpec.Pattern.LOB) {
            // M5 part H: the mortar root's lob at the ship's position now.
            actions.lob(parts.partX(p), parts.partY(p), attack.gun());
            return;
        }
        if (attack.pattern() == BossSpec.Pattern.SWEEP) {
            sweeps.start(a, p, attack, actions);
            return;
        }
        // M5 part E: an arena boss's surfacing part fires from its hit box's lower edge (the Kraken's beak).
        double fireY = arena != null && p == arena.surfacePart()
                ? parts.partY(p) - spec.parts().get(p).box().height() / 2
                : parts.partY(p);
        if (attack.pattern() == BossSpec.Pattern.RING) {
            actions.ring(
                    parts.partX(p),
                    fireY,
                    attack.count(),
                    attack.gun().bulletSpeed(),
                    attack.gun().damage());
        } else if (attack.firesDown(p) && stride != null) {
            // M7 part F: a walking boss's "down" is along its heading (its chin burst in its first phase).
            actions.along(parts.partX(p), fireY, stride.headingRadians(), attack.gun());
        } else if (attack.firesDown(p)) {
            // Round 34, P2: this part fires its aimed attack straight down (the kinder Siege Spire's pods).
            actions.down(parts.partX(p), fireY, attack.gun());
        } else {
            actions.aimed(parts.partX(p), fireY, attack.gun());
        }
    }

    /** The parts that fire {@code attack}: its own, or else the parts the phase ends on. */
    static List<Integer> firingParts(BossSpec.Attack attack, BossSpec.Phase current) {
        return attack.parts().isEmpty() ? current.untilParts() : attack.parts();
    }

    /** The phase's stream: the first at its start, then every {@code every} s, one unit per interval. */
    private void stream(BossSpec.Stream stream, BossActions actions, boolean firing) {
        if (--streamTicks <= 0) {
            streamTicks = Math.max(1, SimStep.ticks(stream.everySeconds()));
            streamLeft = stream.count();
            streamUnitTicks = 1;
        }
        if (streamLeft > 0 && --streamUnitTicks <= 0) {
            streamLeft--;
            streamUnitTicks = Math.max(1, SimStep.ticks(stream.intervalSeconds()));
            boolean left =
                    switch (stream.edge()) {
                        case LEFT -> true;
                        case RIGHT -> false;
                        case NONE, ALTERNATING, TOP, BOTTOM -> streamUnits % 2 == 0;
                    };
            streamUnits++;
            if (firing) {
                actions.release(stream, left);
            }
        }
    }
}
