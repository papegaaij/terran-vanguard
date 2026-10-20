package vanguard.sim;

import java.util.Optional;

/**
 * The special slot in flight (design/player/specials): the charges carried, the special button and
 * the Airstrike. A press calls a strike when the slot has a charge and no strike is flying; while
 * one flies the press waits for the input buffer, and a press that finds no charge, no special or
 * a strike still flying after the buffer is denied. A call uses a charge; after the strike's delay
 * two bombers enter at the bottom edge left and right of the ship's x at the call and fly up,
 * dropping a bomb every bomb spacing. A bomb bursts after its fall where the ground scroll has
 * carried its release point: it hits the {@code ground} and {@code low-air} targets (hardened
 * included) and the {@code air} targets in its radius, never {@code high-air} nor (M5 part E)
 * {@code sub} under the water or a sunken trigger, with at most the layer's cap per target in one
 * strike. Its kills pay their bounty and keep the chain, as the
 * guns' kills do ({@link PlayerFire.Hits}). A new strike can be called once the bombers have left
 * the screen.
 *
 * <p>Charges found in the level (the special charge pickup) count up to the most carried. The
 * sortie reports the charges used and found; the campaign applies them for a won level only, so a
 * retry starts with the level-start charges again. Without a fitted special nothing here is part
 * of the state hash, so recordings without the special button replay as before.
 *
 * <p>M7 part A adds a kind per special ({@link SpecialSpec.Kind}): the EMP Burst (a ring like the
 * Smart Bomb's that stuns the units it reaches), the Orbital Lance (a steered column burning what
 * it overlaps) and the Shield Overcharge, the first cooldown special: no charges, ready at every
 * attempt's start (the level start, a retry, a boss checkpoint) and again its cooldown after each
 * activation, counted in simulation steps; a press while it cools is denied unless it is ready
 * within the input buffer, and a special charge pickup takes its pickup time off a running
 * cooldown. The Airstrike's and the Smart Bomb's state hashes as before.
 */
public final class SpecialSlot {
    /** The two bombers. */
    public static final int BOMBERS = 2;
    /** The targets one strike can track for its caps; more are not damaged. */
    private static final int LEDGER_CAPACITY = 128;

    private final SpecialSpec spec;
    private final AirstrikeSpec airstrike;
    /** The Smart Bomb's numbers; null for an Airstrike or no special. */
    private final SmartBombSpec smartBomb;
    /** The ship's defences, which a Smart Bomb guards; null without one. */
    private final Defences defences;
    /** M7 part A: the EMP Burst's numbers; null for another special. */
    private final EmpSpec emp;
    /** M7 part A: the Orbital Lance's numbers; null for another special. */
    private final LanceSpec lance;
    /** M7 part A: the Shield Overcharge's numbers; null for another special. */
    private final OverchargeSpec overcharge;
    /** M7 part A: whether the fitted special is a cooldown special. */
    private final boolean cooldown;
    /** M7 part A: a cooldown special's cooldown and what a charge pickup takes off it, in steps. */
    private final int cooldownTicks;

    private final int pickupTicks;
    /** M7 part A: the Orbital Lance's lock, burn, fade and repeat, in steps. */
    private final int lockTicks;

    private final int burnTicks;
    private final int fadeTicks;
    private final int lanceRepeatTicks;

    private final int ringTicks;
    private final int repeatTicks;
    private double bombX;
    private double bombY;
    /** The ring's radius when it covers the whole play field from the bomb's centre. */
    private double ringReach;

    private final SimEvents events;
    private final Pool<AirstrikeBomb> bombs;
    private final int bufferTicks;
    private final int delayTicks;
    private final int fallTicks;

    private int charges;
    private int used;
    private int found;
    /** Whether the special button was down in the last step: a press is a step it goes down. */
    private boolean held;
    /** Steps a press still waits for the strike in flight; 0 for none. */
    private int buffer;
    /** Steps since the call of the strike in flight; -1 when none flies. */
    private int sinceCall = -1;

    private double callX;
    private boolean bombersIn;
    private final double[] bomberX = new double[BOMBERS];
    private double bomberY;
    private double prevBomberY;
    private double nextDrop;
    private int blasts;

    /** Per target struck in this strike: its key (as {@link Shot#struck}'s) and the damage dealt. */
    private final int[] ledgerKeys = new int[LEDGER_CAPACITY];

    private final double[] ledgerDealt = new double[LEDGER_CAPACITY];
    private int ledgerSize;

    /** M7 part A: steps a cooldown special still cools; 0 when it is ready. */
    private int cooling;
    /** M7 part A: the Orbital Lance's centre now and in the last step. */
    private double lanceX;

    private double prevLanceX;
    /** M7 part A: whether the Shield Overcharge raised the capacity in the last step (for its end's event). */
    private boolean overcharged;

    SpecialSlot(Optional<SpecialSpec> fitted, SimEvents events) {
        this(fitted, events, null);
    }

    /** @param defences the ship's, which a Smart Bomb guards */
    SpecialSlot(Optional<SpecialSpec> fitted, SimEvents events, Defences defences) {
        spec = fitted.orElse(null);
        airstrike = spec == null ? null : spec.airstrike();
        smartBomb = spec == null ? null : spec.smartBomb();
        this.defences = defences;
        emp = spec == null ? null : spec.emp();
        lance = spec == null ? null : spec.lance();
        overcharge = spec == null ? null : spec.overcharge();
        cooldown = spec != null && spec.cooldown();
        cooldownTicks = cooldown ? Math.max(1, SimStep.ticks(spec.cooldownSeconds())) : 0;
        pickupTicks = cooldown ? SimStep.ticks(spec.cooldownPickupSeconds()) : 0;
        if (smartBomb != null) {
            ringTicks = Math.max(1, SimStep.ticks(smartBomb.ringSeconds()));
            repeatTicks = Math.max(1, SimStep.ticks(smartBomb.repeatSeconds()));
        } else if (emp != null) {
            ringTicks = Math.max(1, SimStep.ticks(emp.ringSeconds()));
            repeatTicks = Math.max(ringTicks, SimStep.ticks(emp.repeatSeconds()));
        } else {
            ringTicks = 0;
            repeatTicks = 0;
        }
        lockTicks = lance == null ? 0 : SimStep.ticks(lance.lockSeconds());
        burnTicks = lance == null ? 0 : Math.max(1, SimStep.ticks(lance.seconds()));
        fadeTicks = lance == null ? 0 : SimStep.ticks(lance.fadeSeconds());
        lanceRepeatTicks = lance == null ? 0 : SimStep.ticks(lance.repeatSeconds());
        this.events = events;
        int capacity = airstrike == null ? 0 : BOMBERS * ((int) (PlayField.HEIGHT / airstrike.bombSpacing()) + 1);
        bombs = new Pool<>(capacity, AirstrikeBomb::new, AirstrikeBomb[]::new);
        bufferTicks = spec == null ? 0 : SimStep.ticks(spec.bufferSeconds());
        delayTicks = airstrike == null ? 0 : Math.max(1, SimStep.ticks(airstrike.delaySeconds()));
        fallTicks = airstrike == null ? 0 : Math.max(1, SimStep.ticks(airstrike.fallSeconds()));
        reset();
    }

    /** Back to a boss checkpoint: its charges, those used and found until then, nothing flying. */
    void restore(int charges, int used, int found) {
        reset();
        this.charges = charges;
        this.used = used;
        this.found = found;
    }

    /** Back to the level start: the level-start charges, nothing flying. */
    void reset() {
        charges = spec == null ? 0 : spec.charges();
        used = 0;
        found = 0;
        held = false;
        buffer = 0;
        sinceCall = -1;
        bombersIn = false;
        ledgerSize = 0;
        blasts = 0;
        bombX = 0;
        bombY = 0;
        ringReach = 0;
        Pools.clear(bombs);
        cooling = 0;
        lanceX = 0;
        prevLanceX = 0;
        overcharged = false;
    }

    /**
     * The special button for one step: a press calls a strike, waits for the one in flight or is
     * denied. Only while {@code active} (the ship flies the level); other presses are ignored. M7
     * part A: a cooldown special's cooldown runs down here every step ({@link SimEvents.Type#SPECIAL_READY}
     * when it is over); a press while it cools waits only when it is ready within the buffer.
     */
    void command(int commands, boolean active, double shipX, double shipY) {
        boolean down = Command.SPECIAL.in(commands);
        boolean pressed = down && !held;
        held = down;
        if (cooling > 0 && --cooling == 0) {
            events.add(SimEvents.Type.SPECIAL_READY, shipX, shipY);
        }
        if (overcharge != null) {
            boolean now = defences.overcharged();
            if (overcharged && !now) {
                events.add(SimEvents.Type.OVERCHARGE_ENDED, shipX, shipY);
            }
            overcharged = now;
        }
        if (!active) {
            buffer = 0;
            return;
        }
        if (pressed) {
            if (spec == null || (!cooldown && charges == 0) || cooling > bufferTicks) {
                events.add(SimEvents.Type.SPECIAL_DENIED, shipX, shipY);
                return;
            }
            buffer = bufferTicks + 1;
        }
        if (buffer == 0) {
            return;
        }
        if (sinceCall < 0 && cooling == 0) {
            buffer = 0;
            call(shipX, shipY);
        } else if (--buffer == 0) {
            events.add(SimEvents.Type.SPECIAL_DENIED, shipX, shipY);
        }
    }

    private void call(double shipX, double shipY) {
        if (cooldown) {
            // M7 part A: a cooldown special uses no charge; its cooldown counts from the activation.
            cooling = cooldownTicks;
        } else {
            charges--;
            used++;
        }
        sinceCall = 0;
        callX = shipX;
        events.add(SimEvents.Type.SPECIAL_CALLED, shipX, shipY);
        if (smartBomb != null) {
            bombX = shipX;
            bombY = shipY;
            double dx = Math.max(shipX, PlayField.WIDTH - shipX);
            double dy = Math.max(shipY, PlayField.HEIGHT - shipY);
            ringReach = Math.sqrt(dx * dx + dy * dy);
            ledgerSize = 0;
            if (defences != null) {
                defences.guard(SimStep.ticks(smartBomb.invulnerableSeconds()));
            }
            events.add(SimEvents.Type.SMART_BOMB, shipX, shipY);
        } else if (emp != null) {
            bombX = shipX;
            bombY = shipY;
            double dx = Math.max(shipX, PlayField.WIDTH - shipX);
            double dy = Math.max(shipY, PlayField.HEIGHT - shipY);
            ringReach = Math.sqrt(dx * dx + dy * dy);
            ledgerSize = 0;
            events.add(SimEvents.Type.EMP_BURST, shipX, shipY);
        } else if (lance != null) {
            lanceX = prevLanceX = shipX;
            ledgerSize = 0;
            events.add(SimEvents.Type.LANCE_LOCK, shipX, shipY);
        } else if (overcharge != null) {
            // Instant: nothing flies; the defences hold the raised capacity, the cooldown runs.
            sinceCall = -1;
            defences.overcharge(
                    overcharge.factor(), SimStep.ticks(overcharge.seconds()), SimStep.ticks(overcharge.bleedSeconds()));
            overcharged = true;
            events.add(SimEvents.Type.OVERCHARGE, shipX, shipY);
        }
    }

    /**
     * One step of a Smart Bomb: its ring grows; at once every enemy bullet (and mortar blob) goes,
     * then those the ring passes; the spore mines it passes pop as if shot; every enemy (M5 part E:
     * those under the water too) and boss part on the screen the ring reaches takes its damage once,
     * and a sunken trigger it reaches is spent at once (M5 part E). It is busy until its repeat time
     * has passed.
     */
    void bomb(
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            SetPiece[] setPieces,
            PlayerFire.Hits hits,
            Pool<EnemyBullet> bullets,
            Pool<Lob> lobs,
            Pool<Mine> mines) {
        if (emp != null) {
            burst(enemies, setPieces);
            return;
        }
        if (smartBomb == null || sinceCall < 0) {
            return;
        }
        sinceCall++;
        if (sinceCall > repeatTicks) {
            sinceCall = -1;
            return;
        }
        if (sinceCall > ringTicks) {
            return;
        }
        double radius = ringRadius();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            EnemyBullet bullet = bullets.get(i);
            if (sinceCall == 1 || within(bullet.x(), bullet.y(), radius)) {
                bullets.free(i);
            }
        }
        for (int i = lobs.size() - 1; i >= 0; i--) {
            lobs.free(i);
        }
        for (int i = mines.size() - 1; i >= 0; i--) {
            Mine mine = mines.get(i);
            if (within(mine.x(), mine.y(), radius)) {
                hits.mineDestroyed(i);
            }
        }
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            // M7 part B: the Smart Bomb hits the sub layer but never the buried one.
            if (!enemy.layer().touchable() || !PlayerFire.onField(enemy) || !within(enemy.x(), enemy.y(), radius)) {
                continue;
            }
            if (strike(2 * enemy.serial(), smartBomb.damage())) {
                // M7 part D: a unit part takes the bomb too (the Shellback's vent).
                if (enemy.partAlive() && enemy.damagePart(smartBomb.damage(), true)) {
                    hits.unitPartDestroyed(j);
                }
                // M7 part E (Q.22e): a blast, which a closed vortex's vanes let through.
                if (enemy.damage(smartBomb.damage(), true, true)) {
                    hits.enemyDestroyed(j);
                }
            }
        }
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            if (object.spec().submerged()
                    && object.hittable()
                    && PlayField.overlaps(object.x(), object.y(), object.spec().size())
                    && within(object.x(), object.y(), radius)
                    && object.spend()) {
                hits.triggerReleased(j);
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount() && piece.present(); p++) {
                Hitbox box = piece.spec().parts().get(p).box();
                double px = piece.partX(p);
                double py = piece.partY(p);
                if (piece.partWrecked(p)
                        || piece.partShielded(p)
                        || !PlayField.overlaps(px, py, box)
                        || !within(px, py, radius)) {
                    continue;
                }
                if (strike(-1 - k * SetPieceSpec.MAX_PARTS - p, smartBomb.damage())
                        && piece.damagePart(p, smartBomb.bossPartDamage())) {
                    hits.partDestroyed(k, p);
                }
            }
        }
    }

    private boolean within(double x, double y, double radius) {
        double dx = x - bombX;
        double dy = y - bombY;
        return dx * dx + dy * dy <= radius * radius;
    }

    /**
     * Records a strike on the target with {@code key} (with {@code dealt} in the ledger); false when the
     * bomb struck it already (or the ledger is full).
     */
    private boolean strike(int key, double dealt) {
        for (int entry = 0; entry < ledgerSize; entry++) {
            if (ledgerKeys[entry] == key) {
                return false;
            }
        }
        if (ledgerSize == LEDGER_CAPACITY) {
            return false;
        }
        ledgerKeys[ledgerSize] = key;
        ledgerDealt[ledgerSize] = dealt;
        ledgerSize++;
        return true;
    }

    /**
     * M7 part A, one step of an EMP Burst (design/player/specials, EMP Burst (L15)): its ring grows as
     * the Smart Bomb's; every unit on the screen whose centre it reaches is stunned once, a machine for
     * the machine stun and every other unit for the Vrell stun ({@link Enemy#empStun(int)}); a chain
     * member stuns its whole chain. Never units under the water ({@code sub}); enemy bullets and mines,
     * set pieces (bosses, mid-bosses) and allies are untouched, but the set pieces' shields are
     * stripped ({@link #strip}). It is busy until its repeat time has passed.
     */
    private void burst(Pool<Enemy> enemies, SetPiece[] setPieces) {
        if (sinceCall < 0) {
            return;
        }
        sinceCall++;
        if (sinceCall > repeatTicks) {
            sinceCall = -1;
            return;
        }
        if (sinceCall > ringTicks) {
            return;
        }
        if (sinceCall == 1) {
            strip(enemies, setPieces);
        }
        double radius = empRadius();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            // M7 part B: a buried unit is out of the ring's reach as one under the water.
            if (enemy.layer() == Layer.SUB
                    || !enemy.layer().touchable()
                    || !PlayerFire.onField(enemy)
                    || !within(enemy.x(), enemy.y(), radius)) {
                continue;
            }
            int ticks = SimStep.ticks(enemy.spec().machine() ? emp.machineStunSeconds() : emp.vrellStunSeconds());
            Chain chain = enemy.chain();
            if (chain != null) {
                // The whole chain stops once, as the ring reaches its first member.
                if (strike(Integer.MIN_VALUE + chain.serial(), 0)) {
                    chain.empStun(ticks);
                    events.add(SimEvents.Type.EMP_STUNNED, enemy.x(), enemy.y(), enemy.kind());
                }
            } else if (strike(2 * enemy.serial(), 0)) {
                enemy.empStun(ticks);
                events.add(SimEvents.Type.EMP_STUNNED, enemy.x(), enemy.y(), enemy.kind());
            }
        }
    }

    /**
     * M7 part A, a hook: the EMP Burst strips every enemy shield at once: M7 part D, the aura rings a Choir
     * Herald has given (design/enemies/air/choir-herald, EMP; {@link SimEvents.Type#RINGS_STRIPPED} when there
     * was one; the Herald's own stun then pauses its aura); a later act's part shields are still to come.
     */
    private void strip(Pool<Enemy> enemies, SetPiece[] setPieces) {
        int stripped = 0;
        for (int j = 0; j < enemies.size(); j++) {
            if (enemies.get(j).stripRing()) {
                stripped++;
            }
        }
        if (stripped > 0) {
            events.add(SimEvents.Type.RINGS_STRIPPED, bombX, bombY, stripped);
        }
    }

    /** M7 part A: the EMP Burst's ring radius now; 0 when none expands. */
    public double empRadius() {
        if (emp == null || sinceCall < 0 || sinceCall > ringTicks) {
            return 0;
        }
        return ringReach * sinceCall / ringTicks;
    }

    /** M7 part A: the EMP Burst's ring radius between the last step and this one; {@code alpha} in [0, 1]. */
    public double empRadius(double alpha) {
        if (emp == null || sinceCall < 0 || sinceCall > ringTicks) {
            return 0;
        }
        return ringReach * Math.max(0, sinceCall - 1 + alpha) / ringTicks;
    }

    /** M7 part A: seconds since the last EMP Burst went off, while it is busy; negative when none is. */
    public double empSeconds(double alpha) {
        return emp == null || sinceCall < 0 ? -1 : (sinceCall - 1 + alpha) * SimStep.SECONDS;
    }

    /** M7 part A: the EMP Burst's numbers; null when the fitted special is another. */
    public EmpSpec emp() {
        return emp;
    }

    /**
     * M7 part A, one step of an Orbital Lance (design/player/specials, Orbital Lance (L17)): its centre
     * follows {@code shipX} at up to its follow speed from the call on; after the lock it burns for its
     * time: every enemy on the screen whose hit box overlaps the column, on every layer but {@code sub}
     * (hardened included: it comes from above), takes its DPS for the step; a ground object its share
     * (a secret trigger counts one hit per lance); each set piece part in the column its boss-part DPS,
     * at most its cap per lance, without weak-point multipliers. Walls do not stop it (part C: nothing
     * to do); a tendril link in it would take its damage as from a {@code beam} (part C hook). It fades,
     * and is busy until its repeat time after the fade has passed.
     */
    void lance(
            double shipX, Pool<Enemy> enemies, Pool<GroundObject> ground, SetPiece[] setPieces, PlayerFire.Hits hits) {
        if (lance == null || sinceCall < 0) {
            return;
        }
        sinceCall++;
        prevLanceX = lanceX;
        double most = lance.follow() * SimStep.SECONDS;
        lanceX += Math.clamp(shipX - lanceX, -most, most);
        if (sinceCall == lockTicks + 1) {
            events.add(SimEvents.Type.LANCE_FIRED, lanceX, 0);
        }
        if (sinceCall > lockTicks + burnTicks + fadeTicks + lanceRepeatTicks) {
            sinceCall = -1;
            return;
        }
        if (sinceCall <= lockTicks || sinceCall > lockTicks + burnTicks) {
            if (sinceCall == lockTicks + burnTicks + 1) {
                events.add(SimEvents.Type.LANCE_ENDED, lanceX, 0);
            }
            return;
        }
        double half = lance.width() / 2;
        double damage = lance.dps() * SimStep.SECONDS;
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            if (enemy.layer() == Layer.SUB
                    || !enemy.layer().touchable()
                    || !PlayerFire.onField(enemy)
                    || PlayerFire.columnContact(lanceX, half, 0, enemy.x(), enemy.y(), enemy.hitbox())
                            == Double.POSITIVE_INFINITY) {
                continue;
            }
            if (enemy.partAlive()
                    && PlayerFire.columnContact(lanceX, half, 0, enemy.partX(), enemy.partY(), enemy.partBox())
                            < Double.POSITIVE_INFINITY
                    && enemy.damagePart(damage, true)) {
                // M7 part D: a unit part in the column burns too (the Shellback's vent).
                hits.unitPartDestroyed(j);
            }
            if (enemy.damage(damage, true)) {
                hits.enemyDestroyed(j);
            }
        }
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (!object.hittable()
                    || spec.submerged()
                    || !PlayField.overlaps(object.x(), object.y(), spec.size())
                    || PlayerFire.columnContact(lanceX, half, 0, object.x(), object.y(), spec.size())
                            == Double.POSITIVE_INFINITY) {
                continue;
            }
            if (spec.trigger()) {
                if (strike(2 * object.serial() + 1, 0) && object.countHit()) {
                    hits.triggerReleased(j);
                }
            } else if (object.damage(damage)) {
                hits.groundDestroyed(j);
            }
        }
        double partDamage = lance.bossPartDps() * SimStep.SECONDS;
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            for (int p = 0; p < piece.partCount() && piece.present(); p++) {
                if (piece.partLayer(p) == Layer.SUB
                        || piece.partWrecked(p)
                        || piece.partShielded(p)
                        || !piece.partOnField(p)
                        || piece.partColumn(p, lanceX, half, 0) == Double.POSITIVE_INFINITY) {
                    continue;
                }
                double amount = capped(-1 - k * SetPieceSpec.MAX_PARTS - p, partDamage, lance.bossPartCap());
                if (amount > 0 && piece.damagePartFlat(p, amount)) {
                    hits.partDestroyed(k, p);
                }
            }
        }
    }

    /** {@code damage} to the target with {@code key}, cut to what {@code cap} leaves after this call's earlier damage; recorded. */
    private double capped(int key, double damage, double cap) {
        int entry = 0;
        while (entry < ledgerSize && ledgerKeys[entry] != key) {
            entry++;
        }
        if (entry == ledgerSize) {
            if (ledgerSize == LEDGER_CAPACITY) {
                return 0;
            }
            ledgerKeys[entry] = key;
            ledgerDealt[entry] = 0;
            ledgerSize++;
        }
        double amount = Math.min(damage, cap - ledgerDealt[entry]);
        if (amount <= 0) {
            return 0;
        }
        ledgerDealt[entry] += amount;
        return amount;
    }

    /** M7 part A: the Orbital Lance's numbers; null when the fitted special is another. */
    public LanceSpec lance() {
        return lance;
    }

    /** M7 part A: whether the Orbital Lance's targeting line shows (called, before it fires). */
    public boolean lanceLocking() {
        return lance != null && sinceCall >= 0 && sinceCall <= lockTicks;
    }

    /** M7 part A: whether the Orbital Lance burns now (its column deals damage). */
    public boolean lanceBurning() {
        return lance != null && sinceCall > lockTicks && sinceCall <= lockTicks + burnTicks;
    }

    /**
     * M7 part A: the Orbital Lance column's strength for the presentation: 1 while it burns, falling to 0
     * over its fade; 0 when no column shows.
     */
    public double lanceStrength(double alpha) {
        if (lance == null || sinceCall <= lockTicks) {
            return 0;
        }
        double into = sinceCall - 1 + alpha - lockTicks - burnTicks;
        if (into <= 0) {
            return 1;
        }
        return fadeTicks == 0 ? 0 : Math.max(0, 1 - into / fadeTicks);
    }

    /** M7 part A: the Orbital Lance's centre between the last step and this one; {@code alpha} in [0, 1]. */
    public double lanceX(double alpha) {
        return prevLanceX + (lanceX - prevLanceX) * alpha;
    }

    /** The Smart Bomb ring's radius now; 0 when none expands. */
    public double ringRadius() {
        if (smartBomb == null || sinceCall < 0 || sinceCall > ringTicks) {
            return 0;
        }
        return ringReach * sinceCall / ringTicks;
    }

    /** The ring's radius between the last step and this one; {@code alpha} in [0, 1]. */
    public double ringRadius(double alpha) {
        if (smartBomb == null || sinceCall < 0 || sinceCall > ringTicks) {
            return 0;
        }
        return ringReach * Math.max(0, sinceCall - 1 + alpha) / ringTicks;
    }

    /** Seconds since the last Smart Bomb went off, while it is busy; negative when none is. */
    public double bombSeconds(double alpha) {
        return smartBomb == null || sinceCall < 0 ? -1 : (sinceCall - 1 + alpha) * SimStep.SECONDS;
    }

    /** The Smart Bomb's centre: the ship's position when it went off. */
    public double bombX() {
        return bombX;
    }

    public double bombY() {
        return bombY;
    }

    /** The Smart Bomb's numbers; null when the fitted special is none. */
    public SmartBombSpec smartBomb() {
        return smartBomb;
    }

    /**
     * One step of the strike: the delay runs, the bombers fly and drop their bombs, and the bombs
     * fall with the ground scroll and burst on {@code enemies}, {@code ground} and the set pieces'
     * parts; what a blast destroys goes to {@code hits}.
     */
    void update(
            double groundScroll,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            SetPiece[] setPieces,
            PlayerFire.Hits hits) {
        if (airstrike == null) {
            return;
        }
        // The bombs in the air first, so one released in this step falls from the next.
        for (int i = bombs.size() - 1; i >= 0; i--) {
            AirstrikeBomb bomb = bombs.get(i);
            if (bomb.fall(groundScroll)) {
                double x = bomb.x();
                double y = bomb.y();
                bombs.free(i);
                blast(x, y, enemies, ground, setPieces, hits);
            }
        }
        if (sinceCall >= 0) {
            // The call's own step makes it 1: the bombers enter the delay's steps after that one.
            sinceCall++;
            if (sinceCall == delayTicks + 1) {
                enter();
            } else if (bombersIn) {
                flyBombers();
            }
        }
    }

    /** The bombers enter at the bottom edge, below it by half their length; the new strike's caps start. */
    private void enter() {
        double half = airstrike.bomber().width() / 2;
        bomberX[0] = Math.clamp(callX - airstrike.offset(), half, PlayField.WIDTH - half);
        bomberX[1] = Math.clamp(callX + airstrike.offset(), half, PlayField.WIDTH - half);
        bomberY = prevBomberY = -airstrike.bomber().height() / 2;
        nextDrop = airstrike.bombSpacing() / 2;
        bombersIn = true;
        ledgerSize = 0;
        blasts = 0;
        events.add(SimEvents.Type.AIRSTRIKE_INBOUND, (bomberX[0] + bomberX[1]) / 2, 0);
    }

    /** The bombers climb a step, drop a bomb each at every spacing they pass and are gone past the top edge. */
    private void flyBombers() {
        prevBomberY = bomberY;
        bomberY += airstrike.speed() * SimStep.SECONDS;
        while (nextDrop < PlayField.HEIGHT && bomberY >= nextDrop) {
            for (int b = 0; b < BOMBERS; b++) {
                AirstrikeBomb bomb = bombs.obtain();
                if (bomb != null) {
                    bomb.release(bomberX[b], nextDrop, fallTicks);
                }
            }
            nextDrop += airstrike.bombSpacing();
        }
        if (bomberY - airstrike.bomber().height() / 2 >= PlayField.HEIGHT) {
            bombersIn = false;
            sinceCall = -1;
        }
    }

    private void blast(
            double x,
            double y,
            Pool<Enemy> enemies,
            Pool<GroundObject> ground,
            SetPiece[] setPieces,
            PlayerFire.Hits hits) {
        events.add(SimEvents.Type.AIRSTRIKE_BLAST, x, y, blasts++);
        double radius = airstrike.blastRadius();
        for (int j = enemies.size() - 1; j >= 0; j--) {
            Enemy enemy = enemies.get(j);
            // Its current layer (M5 part C: a pounce's air window takes the air damage).
            Layer layer = enemy.layer();
            if (layer == Layer.HIGH_AIR
                    || layer == Layer.SUB
                    || !layer.touchable()
                    || !PlayerFire.onField(enemy)
                    || !PlayerFire.inBlast(x, y, radius, enemy.x(), enemy.y(), enemy.hitbox())) {
                continue;
            }
            double amount = deal(2 * enemy.serial(), layer);
            if (amount > 0 && enemy.partInBlast(x, y, radius) && enemy.damagePart(amount, true)) {
                // M7 part D: a blast covering a unit part hurts it too (the Shellback's vent).
                hits.unitPartDestroyed(j);
            }
            // M7 part E (Q.22e): a blast, which a closed vortex's vanes let through.
            if (amount > 0 && enemy.damage(amount, true, true)) {
                hits.enemyDestroyed(j);
            }
        }
        for (int j = ground.size() - 1; j >= 0; j--) {
            GroundObject object = ground.get(j);
            GroundObjectSpec spec = object.spec();
            if (!object.hittable()
                    || spec.submerged()
                    || !PlayerFire.inBlast(x, y, radius, object.x(), object.y(), spec.size())) {
                continue;
            }
            if (spec.trigger()) {
                if (object.countHit()) {
                    hits.triggerReleased(j);
                }
            } else {
                double amount = deal(2 * object.serial() + 1, Layer.GROUND);
                if (amount > 0 && object.damage(amount)) {
                    hits.groundDestroyed(j);
                }
            }
        }
        for (int k = 0; k < setPieces.length; k++) {
            SetPiece piece = setPieces[k];
            if (!piece.present()) {
                continue;
            }
            for (int p = 0; p < piece.partCount() && piece.present(); p++) {
                // M5 part E: on the part's own layer (an arena boss's part under the water is out of reach).
                Layer layer = piece.partLayer(p);
                if (layer == Layer.HIGH_AIR
                        || layer == Layer.SUB
                        || piece.partWrecked(p)
                        || piece.partShielded(p)
                        || !piece.partOnField(p)
                        || !piece.partInBlast(p, x, y, radius)) {
                    continue;
                }
                double amount = deal(-1 - k * SetPieceSpec.MAX_PARTS - p, layer);
                if (amount > 0 && piece.damagePartAt(p, amount, x, y)) {
                    hits.partDestroyed(k, p);
                }
            }
        }
    }

    /**
     * The damage a blast deals to the target with {@code key} on {@code layer}: the layer's damage,
     * cut to what its cap leaves after this strike's earlier blasts; recorded against the cap.
     */
    private double deal(int key, Layer layer) {
        boolean air = layer == Layer.AIR;
        double damage = air ? airstrike.airDamage() : airstrike.groundDamage();
        double cap = air ? airstrike.airCap() : airstrike.groundCap();
        int entry = 0;
        while (entry < ledgerSize && ledgerKeys[entry] != key) {
            entry++;
        }
        if (entry == ledgerSize) {
            if (ledgerSize == LEDGER_CAPACITY) {
                return 0;
            }
            ledgerKeys[entry] = key;
            ledgerDealt[entry] = 0;
            ledgerSize++;
        }
        double amount = Math.min(damage, cap - ledgerDealt[entry]);
        if (amount <= 0) {
            return 0;
        }
        ledgerDealt[entry] += amount;
        return amount;
    }

    /** A special charge pickup: one more charge, up to the most carried; returns whether it counted. */
    boolean collect() {
        if (cooldown) {
            // M7 part A: a cooldown special: its pickup time off a running cooldown, nothing when it is
            // ready; at least a step stays, so the ready event comes from the command step.
            if (cooling == 0) {
                return false;
            }
            cooling = Math.max(1, cooling - pickupTicks);
            return true;
        }
        if (spec == null || charges >= spec.maxCharges()) {
            return false;
        }
        charges++;
        found++;
        return true;
    }

    void addTo(StateHash hash) {
        if (spec == null) {
            return;
        }
        if (airstrike == null && smartBomb == null) {
            // M7 part A's specials: their own state; the Airstrike and the Smart Bomb hash as before.
            hash.add(spec.kind().ordinal())
                    .add(charges)
                    .add(used)
                    .add(found)
                    .add(held ? 1 : 0)
                    .add(buffer)
                    .add(sinceCall)
                    .add(cooling)
                    .add(bombX)
                    .add(bombY)
                    .add(ringReach)
                    .add(lanceX)
                    .add(prevLanceX)
                    .add(overcharged ? 1 : 0)
                    .add(ledgerSize);
            for (int i = 0; i < ledgerSize; i++) {
                hash.add(ledgerKeys[i]).add(ledgerDealt[i]);
            }
            return;
        }
        if (smartBomb != null) {
            hash.add(bombX).add(bombY).add(ringReach);
        }
        hash.add(charges)
                .add(used)
                .add(found)
                .add(held ? 1 : 0)
                .add(buffer)
                .add(sinceCall)
                .add(callX)
                .add(bombersIn ? 1 : 0)
                .add(bomberX[0])
                .add(bomberX[1])
                .add(bomberY)
                .add(prevBomberY)
                .add(nextDrop)
                .add(blasts)
                .add(ledgerSize);
        for (int i = 0; i < ledgerSize; i++) {
            hash.add(ledgerKeys[i]).add(ledgerDealt[i]);
        }
        Pools.addAll(hash, bombs);
    }

    /** Whether a special is fitted. */
    public boolean fitted() {
        return spec != null;
    }

    /** The fitted special's name; empty without one. */
    public String name() {
        return spec == null ? "" : spec.name();
    }

    /** The charges left. */
    public int charges() {
        return charges;
    }

    public int maxCharges() {
        return spec == null ? 0 : spec.maxCharges();
    }

    /** Charges used in this attempt. */
    public int used() {
        return used;
    }

    /** Charges found in this attempt (special charge pickups that counted). */
    public int found() {
        return found;
    }

    /**
     * Whether a strike is flying: called and its bombers not yet past the top edge (M7 part A: or a
     * burst, a lance busy, a cooldown special cooling).
     */
    public boolean busy() {
        return sinceCall >= 0 || cooling > 0;
    }

    /** Whether a press now calls a strike. */
    public boolean ready() {
        return spec != null && (cooldown || charges > 0) && sinceCall < 0 && cooling == 0;
    }

    /** M7 part A: the fitted special's kind; null without one. */
    public SpecialSpec.Kind kind() {
        return spec == null ? null : spec.kind();
    }

    /** M7 part A: whether the fitted special is a cooldown special (the HUD's ring instead of charges). */
    public boolean cooldown() {
        return cooldown;
    }

    /** M7 part A: a cooldown special's seconds left to cool; 0 when it is ready (or a charge special). */
    public double cooldownSeconds() {
        return cooling * SimStep.SECONDS;
    }

    /** M7 part A: how far a cooldown special's cooldown has run, 0 just activated to 1 ready (the HUD ring). */
    public double cooldownShare() {
        return cooldownTicks == 0 ? 1 : 1 - (double) cooling / cooldownTicks;
    }

    /** M7 part A: the Shield Overcharge's numbers; null when the fitted special is another. */
    public OverchargeSpec overcharge() {
        return overcharge;
    }

    /** Whether the bombers are on their way up the screen. */
    public boolean bombersIn() {
        return bombersIn;
    }

    public double bomberX(int b) {
        return bomberX[b];
    }

    public double bomberRenderY(double alpha) {
        return prevBomberY + (bomberY - prevBomberY) * alpha;
    }

    /** The bomber's sprite box; meaningful with a special fitted. */
    public Hitbox bomber() {
        return airstrike.bomber();
    }

    public int bombCount() {
        return bombs.size();
    }

    public AirstrikeBomb bomb(int index) {
        return bombs.get(index);
    }
}
