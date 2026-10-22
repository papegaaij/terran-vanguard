package vanguard.sim;

/**
 * The fitted special (design/player/specials): its name, what it does ({@link #effect()}), and either
 * its charges (a charge special: the charges carried into the level and the most it carries) or, M7
 * part A, its cooldown (a cooldown special: no charges, ready at every attempt's start and again
 * {@code cooldownSeconds} after each activation; a special charge pickup takes
 * {@code cooldownPickupSeconds} off a running cooldown). A press waits {@code bufferSeconds} while the
 * special is busy.
 *
 * @param charges the charges at the level start; 0 for a cooldown special
 * @param maxCharges the most charges carried; 0 for a cooldown special
 * @param bufferSeconds how long a press of the special button waits while the special is busy
 * @param cooldownSeconds M7 part A: a cooldown special's cooldown from each activation; 0 for a charge special
 * @param cooldownPickupSeconds M7 part A: what a special charge pickup takes off a running cooldown
 * @param setting M8 part A: where the special works (design/player/specials, Setting restrictions): a level's or
 *     section's {@link SpecialLimits} list replaces it there
 */
public record SpecialSpec(
        String name,
        int charges,
        int maxCharges,
        double bufferSeconds,
        double cooldownSeconds,
        double cooldownPickupSeconds,
        Effect effect,
        Setting setting) {
    /**
     * What a special does: the Airstrike, the Smart Bomb, (M7 part A) the EMP Burst, the Orbital Lance and the Shield
     * Overcharge, and (M8 part A) the Sonar Pulse and the Decoy Flares.
     */
    public sealed interface Effect
            permits AirstrikeSpec, SmartBombSpec, EmpSpec, LanceSpec, OverchargeSpec, SonarSpec, FlaresSpec {}

    /** M7 part A: the kind of a special, from its {@link #effect()}. */
    public enum Kind {
        AIRSTRIKE,
        SMART_BOMB,
        EMP,
        LANCE,
        OVERCHARGE,
        /** M8 part A: the Sonar Pulse. */
        SONAR,
        /** M8 part A: the Decoy Flares. */
        FLARES
    }

    /**
     * M8 part A (design/player/specials, Setting restrictions and the greyed state): where a special works by the
     * medium; the HUD's reason word where it does not ({@link SpecialLimits.Reason}).
     */
    public enum Setting {
        /** Everywhere (every special of Acts 1–3 but the two below). */
        ANYWHERE,
        /** Not under the ice, in water: {@code UNDER ICE} there (the Airstrike, the Orbital Lance). */
        NOT_UNDER_WATER,
        /** Only where there is water (under it, or a level over water): {@code NO WATER} elsewhere (the Sonar Pulse). */
        WATER_ONLY;

        /** The reason word the HUD writes where this setting forbids the special. */
        public SpecialLimits.Reason reason() {
            return this == WATER_ONLY ? SpecialLimits.Reason.NO_WATER : SpecialLimits.Reason.UNDER_ICE;
        }
    }

    public SpecialSpec {
        if (effect == null || setting == null) {
            throw new IllegalArgumentException("a special needs its effect and its setting");
        }
        if (cooldownSeconds > 0) {
            if (charges != 0 || maxCharges != 0) {
                throw new IllegalArgumentException("a cooldown special carries no charges");
            }
            if (!(cooldownPickupSeconds >= 0)) {
                throw new IllegalArgumentException("a pickup takes 0 s or more off a cooldown");
            }
        } else if (!(cooldownSeconds == 0)) {
            throw new IllegalArgumentException("a cooldown is 0 s (a charge special) or more");
        } else if (charges < 0 || charges > maxCharges) {
            throw new IllegalArgumentException("charges " + charges + " outside 0.." + maxCharges);
        }
    }

    /** A special that works anywhere ({@link Setting#ANYWHERE}). */
    public SpecialSpec(
            String name,
            int charges,
            int maxCharges,
            double bufferSeconds,
            double cooldownSeconds,
            double cooldownPickupSeconds,
            Effect effect) {
        this(
                name,
                charges,
                maxCharges,
                bufferSeconds,
                cooldownSeconds,
                cooldownPickupSeconds,
                effect,
                Setting.ANYWHERE);
    }

    /** A charge special: {@code charges} at the level start, at most {@code maxCharges}. */
    public SpecialSpec(String name, int charges, int maxCharges, double bufferSeconds, Effect effect) {
        this(name, charges, maxCharges, bufferSeconds, 0, 0, effect);
    }

    /** M8 part A: the same special working only in {@code where} (the specials data's {@code limits}). */
    public SpecialSpec withSetting(Setting where) {
        return new SpecialSpec(
                name, charges, maxCharges, bufferSeconds, cooldownSeconds, cooldownPickupSeconds, effect, where);
    }

    /**
     * M7 part A: a cooldown special (the Shield Overcharge): ready at the start, {@code cooldownSeconds}
     * from each activation, {@code pickupSeconds} off a running cooldown per special charge pickup.
     */
    public static SpecialSpec cooldown(
            String name, double bufferSeconds, double cooldownSeconds, double pickupSeconds, Effect effect) {
        if (!(cooldownSeconds > 0)) {
            throw new IllegalArgumentException("a cooldown special needs its cooldown");
        }
        return new SpecialSpec(name, 0, 0, bufferSeconds, cooldownSeconds, pickupSeconds, effect);
    }

    /** M7 part A: whether it is a cooldown special (no charges). */
    public boolean cooldown() {
        return cooldownSeconds > 0;
    }

    /** M7 part A: its kind. */
    public Kind kind() {
        return switch (effect) {
            case AirstrikeSpec a -> Kind.AIRSTRIKE;
            case SmartBombSpec b -> Kind.SMART_BOMB;
            case EmpSpec e -> Kind.EMP;
            case LanceSpec l -> Kind.LANCE;
            case OverchargeSpec o -> Kind.OVERCHARGE;
            case SonarSpec s -> Kind.SONAR;
            case FlaresSpec f -> Kind.FLARES;
        };
    }

    /** The Airstrike's numbers; null for another special. */
    public AirstrikeSpec airstrike() {
        return effect instanceof AirstrikeSpec airstrike ? airstrike : null;
    }

    /** The Smart Bomb's numbers; null for another special. */
    public SmartBombSpec smartBomb() {
        return effect instanceof SmartBombSpec bomb ? bomb : null;
    }

    /** M7 part A: the EMP Burst's numbers; null for another special. */
    public EmpSpec emp() {
        return effect instanceof EmpSpec emp ? emp : null;
    }

    /** M7 part A: the Orbital Lance's numbers; null for another special. */
    public LanceSpec lance() {
        return effect instanceof LanceSpec lance ? lance : null;
    }

    /** M7 part A: the Shield Overcharge's numbers; null for another special. */
    public OverchargeSpec overcharge() {
        return effect instanceof OverchargeSpec overcharge ? overcharge : null;
    }

    /** M8 part A: the Sonar Pulse's numbers; null for another special. */
    public SonarSpec sonar() {
        return effect instanceof SonarSpec sonar ? sonar : null;
    }

    /** M8 part A: the Decoy Flares' numbers; null for another special. */
    public FlaresSpec flares() {
        return effect instanceof FlaresSpec flares ? flares : null;
    }

    /** The same special with {@code count} charges at the level start; a cooldown special is returned as it is. */
    public SpecialSpec withCharges(int count) {
        if (cooldown()) {
            return this;
        }
        return new SpecialSpec(
                name, count, maxCharges, bufferSeconds, cooldownSeconds, cooldownPickupSeconds, effect, setting);
    }
}
