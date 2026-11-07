package vanguard.sim;

/**
 * A {@link Sortie}'s boss fights (M11 part A, step A1b: split out of {@link Sortie}; {@link Sortie#bosses()}): which
 * boss records the boss checkpoint as it arrives, whether this attempt has recorded it and whether the next step
 * restarts there, the breaching and the coiled boss, and the boss time the debrief and the Boss rush bonus read.
 *
 * <p>M11 part A, step A1b (design/enemies/bosses, Boss rush; Q6 a; Level 49): a {@link VrellLevel.Rush rush} level
 * fights its bosses one after another, each holding a held gate of its own ({@link LevelScript#gatePiece}), in the
 * gates' order. Each fight records the checkpoint as its boss's gate halts (one checkpoint, the latest fight's: a Retry
 * from boss restarts the fight in progress, the earlier fights' bosses staying dead); before a later fight's record
 * comes the refill (the shield and armour to full, the special's charges back to the level's start and a cooldown
 * special ready, Rook's armour full: {@link SimEvents.Type#BOSS_REFILL}), so a restore starts the fight full. Each
 * fight's record raises {@link SimEvents.Type#FIGHT_NEXT}. The level's one boss time runs from the first boss's arrival
 * (its bar) to the last's kill on the real steps, the gaps included, against the rush's par.
 */
public final class BossFights {
    private final Sortie s;
    /** The boss whose arrival records the checkpoint (the first boss); -1 without one. */
    final int first;
    /** The level's clock (M11 part G, step G1: a rush's breaching boss is its current gate's). */
    private final LevelClock clock;
    /** M8 part H: the coiled boss (Level 28's Abyssal Maw); null without one. */
    final SetPiece coil;
    /** The boss checkpoint; null in a level without a boss (made once the sortie's parts exist: {@link #open()}). */
    BossCheckpoint checkpoint;
    /** Whether this attempt recorded the boss checkpoint. */
    boolean taken;
    /** Whether the next step restarts at the boss checkpoint. */
    boolean retry;
    /** The rush; null in any other level. */
    private final VrellLevel.Rush rush;
    /** The rush's fights in order: each its boss's index among the set pieces; {@code first} alone otherwise. */
    private final int[] fights;
    /** The latest fight whose checkpoint this attempt recorded; -1 before the first. */
    private int recorded = -1;
    /** Per fight, the real step of its boss's arrival; -1 before it arrived (a rush's only). */
    private final int[] arrivals;
    /** Per fight, its boss's kill steps from its arrival as the latest checkpoint found it; -1 for alive. */
    private final int[] kills;

    BossFights(Sortie sortie, SetPiece[] setPieces, LevelClock clock) {
        this.s = sortie;
        int firstBoss = -1;
        for (int k = setPieces.length - 1; k >= 0; k--) {
            firstBoss = setPieces[k].boss().isPresent() ? k : firstBoss;
        }
        first = firstBoss;
        this.clock = clock;
        SetPiece coiled = null;
        for (SetPiece piece : setPieces) {
            if (coiled == null && piece.coil().isPresent()) {
                coiled = piece;
            }
        }
        coil = coiled;
        rush = s.script.vrell().rush().orElse(null);
        if (rush == null) {
            fights = first < 0 ? new int[0] : new int[] {first};
        } else {
            int n = 0;
            for (int g = 0; g < clock.gates(); g++) {
                n += s.script.gatePiece(g) >= 0 ? 1 : 0;
            }
            fights = new int[n];
            for (int g = 0, f = 0; g < clock.gates(); g++) {
                int k = s.script.gatePiece(g);
                if (k >= 0) {
                    fights[f++] = k;
                }
            }
            if (n == 0 || setPieces[fights[0]].held()) {
                throw new IllegalArgumentException("a rush's gates are held by its bosses, each with its bar");
            }
        }
        arrivals = new int[fights.length];
        kills = new int[fights.length];
        java.util.Arrays.fill(arrivals, -1);
        java.util.Arrays.fill(kills, -1);
    }

    /**
     * M7 part H: the breaching boss (Level 21's Dust Colossus); M11 part G, step G1: a rush's current gate's (the Dust
     * Colossus's echo in its gate only); null without one.
     */
    SetPiece breach() {
        return clock.breachNow();
    }

    /** Whether the level has a boss checkpoint (a boss). */
    boolean any() {
        return first >= 0;
    }

    /** Makes the checkpoint, once the sortie's parts it copies exist. */
    void open() {
        checkpoint = first < 0 ? null : new BossCheckpoint(s);
    }

    /** At a step's start: the checkpoint's restore when a Retry from boss asked for it. */
    void restoreIfAsked() {
        if (retry) {
            checkpoint.restore();
            if (rush != null) {
                // The earlier fights' bosses stay dead, their kill times as they were; the fight's boss arrives anew.
                for (int f = 0; f < fights.length; f++) {
                    if (f < recorded) {
                        s.setPieces[fights[f]].restoreDestroyed(kills[f]);
                    } else {
                        arrivals[f] = -1;
                    }
                }
            }
        }
    }

    /** At a step's start, after a restart or restore: the checkpoint's record as its boss arrives. */
    void recordIfDue() {
        if (rush != null) {
            recordFight();
            return;
        }
        // M10 part G, step G1a: a gate's set piece shot down before its halt leaves no checkpoint to record.
        if (checkpoint != null
                && !taken
                && s.levelTick + 1 == arrivalTicks(first)
                && !(s.clock.heldArena && s.setPieces[first].destroyed())) {
            checkpoint.record();
            taken = true;
        }
    }

    /** A rush: the next fight's refill and record as its gate halts; each boss's arrival on the real steps. */
    private void recordFight() {
        for (int f = 0; f < fights.length; f++) {
            SetPiece piece = s.setPieces[fights[f]];
            if (arrivals[f] < 0 && piece.bossTicks() >= 0) {
                arrivals[f] = s.realTick - piece.bossTicks();
            }
        }
        int next = recorded + 1;
        if (next >= fights.length || s.levelTick + 1 != arrivalTicks(fights[next])) {
            return;
        }
        recorded = next;
        if (s.setPieces[fights[next]].destroyed()) {
            // Shot down before its halt: no fight to restart.
            return;
        }
        if (next > 0 && rush.refill()) {
            refill();
        }
        for (int f = 0; f < next; f++) {
            kills[f] = s.setPieces[fights[f]].killTicks();
        }
        checkpoint.record();
        taken = true;
        s.events.add(SimEvents.Type.FIGHT_NEXT, s.ship.x(), s.ship.y(), next);
    }

    /** The refill between two fights: the defences, the special, Rook. */
    private void refill() {
        s.ship.defences().refill();
        s.special.refill();
        if (s.wingman != null) {
            s.wingman.refill();
        }
        s.events.add(SimEvents.Type.BOSS_REFILL, s.ship.x(), s.ship.y());
    }

    /**
     * When boss {@code k} arrives, steps on the level clock. M10 part G, step G1a (Level 41's Halo Platform): a boss
     * holding a held gate records it at that gate's halt (the clock standing one step before the gate's end), wherever
     * it arrives in the gate.
     */
    private int arrivalTicks(int k) {
        int gate = s.clock.heldArena ? s.clock.pieceGate(k) : -1;
        return gate >= 0
                ? s.clock.gateEndTicks(gate)
                : SimStep.ticks(s.setPieces[k].boss().orElseThrow().arriveSeconds());
    }

    /** A restart from the level start: no checkpoint recorded, none asked for. */
    void reset() {
        taken = false;
        retry = false;
        recorded = -1;
        java.util.Arrays.fill(arrivals, -1);
        java.util.Arrays.fill(kills, -1);
    }

    /** The level's boss's kill time and par; {@link LevelResult.BossTime#NONE} without a boss. */
    LevelResult.BossTime time() {
        if (rush != null) {
            int last = fights.length - 1;
            int kill = s.setPieces[fights[last]].killTicks();
            double seconds = kill < 0 || arrivals[0] < 0 || arrivals[last] < 0
                    ? -1
                    : (arrivals[last] + kill - arrivals[0]) * SimStep.SECONDS;
            return new LevelResult.BossTime(rush.parSeconds(), seconds);
        }
        if (first < 0 || s.setPieces[first].held()) {
            // M10 part G, step G1b: a held set piece (Level 41's Halo Platform) is no boss fight: no bar, no Boss rush.
            return LevelResult.BossTime.NONE;
        }
        SetPiece boss = s.setPieces[first];
        return new LevelResult.BossTime(boss.boss().orElseThrow().parSeconds(), boss.killSeconds());
    }

    /** Whether the level is a boss rush (its fights, the refill, a checkpoint per fight). */
    public boolean rush() {
        return rush != null;
    }

    /** The level's boss fights: a rush's bosses, 1 with one boss, 0 without. */
    public int fights() {
        return fights.length;
    }

    /** The fight whose boss's gate has halted last (0 first; the HUD's {@code ECHO n / 3} is n = this + 1); -1 before. */
    public int fight() {
        return rush != null ? recorded : taken ? 0 : -1;
    }

    /** Fight {@code f}'s boss: its index among the level's set pieces. */
    public int fightPiece(int f) {
        return fights[f];
    }

    /**
     * Whether the boss checkpoint is offered on every difficulty (the rush's exception to hard's none); otherwise the
     * difficulty's {@code boss_checkpoint} decides.
     */
    public boolean everyDifficulty() {
        return rush != null && rush.everyDifficulty();
    }

    /** Whether a boss's death plays without the screen flash (the rush's: three in a row). */
    public boolean noFlash() {
        return rush != null;
    }

    /** A rush's own state (the sortie hashes {@link #taken} in every level with a boss). */
    void addTo(StateHash hash) {
        if (rush != null) {
            hash.add(recorded);
            for (int f = 0; f < fights.length; f++) {
                hash.add(arrivals[f]).add(kills[f]);
            }
        }
    }
}
