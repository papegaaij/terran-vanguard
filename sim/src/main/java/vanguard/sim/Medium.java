package vanguard.sim;

/**
 * M8 part A (design/world/europa, Under water rules): the medium the sortie flies in now and the water's factors,
 * from the level's {@link MediumSpec} on the level clock ({@link #at}, stepped by the {@link Sortie} before the ship
 * flies and fires). Under water the ship's plane stays {@code air}; the factors apply where a projectile is spawned
 * (its speed, damage and range are fixed then, so a shot keeps them) and per step for the beam and the drag:
 *
 * <ul>
 *   <li>every bullet's speed (the ship's, Rook's, the drones' and the enemies' bullets and missiles) × the rules'
 *       {@code bulletSpeed};
 *   <li>a weapon without {@code anti-sub} does × {@code damage} (after {@code anti-ground}'s ×2; specials and the
 *       enemies' damage untouched);
 *   <li>the ship's and Rook's acceleration and braking × {@code acceleration} (the drag);
 *   <li>a reach-limited weapon (the beam, the lances) reaches {@code reach} × the field's height from its muzzle;
 *   <li>a torpedo-type weapon runs {@code torpedoRange} × its range (a depth charge rolls out that much faster);
 *   <li>{@link #reaches}: a torpedo reaches every layer in the water but {@code high-air}; an {@code anti-sub} weapon
 *       reaches {@code sub} (in either medium).
 * </ul>
 *
 * <p>At a crossing (a switch after 0 s) the factors ease from the old medium's to the new over the rules' {@code
 * easeSeconds} (smoothstep, on the level clock: step k of n at old + (new − old) × smoothstep((k + 1) ÷ n)); the
 * layers it reaches switch at once. Its state is a function of the level clock, so a retry or a boss checkpoint
 * restores it; it is hashed only in a level with water ({@link MediumSpec#water()}). In the air every factor is
 * exactly 1 and nothing is scaled ({@link #wet()} is false), so the levels before Act 4 fly as before.
 */
final class Medium {
    private final MediumSpec spec;
    private final MediumSpec.Rules rules;
    private final int[] switchTicks;
    private final boolean[] switchUnder;
    private final int easeTicks;
    private final boolean water;

    /** Whether the ship is under water now (the layers it reaches). */
    private boolean underwater;
    /** The water's share of the factors now: 0 in the air, 1 under water, between them in an ease. */
    private double blend;
    /** M8 part A: the Sonar Pulse's window is open (every weapon full damage, {@code sub} open to all). */
    private boolean sonar;
    /** M8 part A: the ship flies with the Pressure hull: no drag on it (Rook and the Bulwark keep theirs). */
    private boolean hull;

    Medium(MediumSpec spec) {
        this.spec = spec;
        rules = spec.rules();
        switchTicks = new int[spec.switches().size()];
        switchUnder = new boolean[switchTicks.length];
        for (int i = 0; i < switchTicks.length; i++) {
            switchTicks[i] = SimStep.ticks(spec.switches().get(i).atSeconds());
            switchUnder[i] = spec.switches().get(i).underwater();
        }
        easeTicks = SimStep.ticks(rules.easeSeconds());
        water = spec.water();
        at(0);
    }

    /** The air: no water at all (a sortie's default). */
    static Medium air() {
        return new Medium(MediumSpec.AIR);
    }

    /** The medium at level clock step {@code levelTick}: whether the ship is under water and the factors' blend. */
    void at(int levelTick) {
        int start = 0;
        double from = 0;
        double target = 0;
        boolean under = false;
        boolean eases = false;
        for (int i = 0; i < switchTicks.length && switchTicks[i] <= levelTick; i++) {
            from = blendAt(start, from, target, eases, switchTicks[i]);
            start = switchTicks[i];
            target = switchUnder[i] ? 1 : 0;
            under = switchUnder[i];
            eases = start > 0 && easeTicks > 0;
        }
        underwater = under;
        blend = blendAt(start, from, target, eases, levelTick);
    }

    /** The blend at {@code tick} of an ease from {@code from} to {@code target} that started at {@code start}. */
    private double blendAt(int start, double from, double target, boolean eases, int tick) {
        int k = tick - start;
        if (!eases || k + 1 >= easeTicks) {
            return target;
        }
        double s = (k + 1.0) / easeTicks;
        return from + (target - from) * s * s * (3 - 2 * s);
    }

    /** Whether the level has any water: then the medium is hashed. */
    boolean water() {
        return water;
    }

    /** Whether the ship flies under water now: the layers its torpedoes and depth charges reach. */
    boolean underwater() {
        return underwater;
    }

    /** Whether any water factor applies now (under water, or easing out of it): otherwise nothing is scaled. */
    boolean wet() {
        return underwater || blend > 0;
    }

    /** The water's share of the factors now, 0..1 (eased at a crossing). */
    double blend() {
        return blend;
    }

    /**
     * The Sonar Pulse's window opens or closes ({@link SpecialSlot}): while it is open every weapon does full damage
     * and {@code sub} is open to every weapon. Set by the special slot, which a retry resets.
     */
    void sonar(boolean open) {
        sonar = open;
    }

    boolean sonar() {
        return sonar;
    }

    /** Factor {@code f} at the current blend: exactly 1 in the air, exactly {@code f} under water. */
    private double factor(double f) {
        if (blend == 0) {
            return 1;
        }
        return blend == 1 ? f : 1 + (f - 1) * blend;
    }

    /**
     * M8 part A (design/player/systems, Pressure hull (L22)): the ship flies with the Pressure hull, which removes the
     * water's drag on it ({@link #shipAcceleration()}); a second hull adds nothing. Fixed for the sortie (the
     * loadout's), so not hashed.
     */
    void hull(boolean fitted) {
        hull = fitted;
    }

    /** The ship's acceleration and braking factor now: the drag, or exactly 1 with the Pressure hull. */
    double shipAcceleration() {
        return hull ? 1 : acceleration();
    }

    /** Every bullet's speed factor at its spawn now. */
    double bulletSpeed() {
        return factor(rules.bulletSpeed());
    }

    /** The ship's and Rook's acceleration and braking factor now (the drag). */
    double acceleration() {
        return factor(rules.acceleration());
    }

    /** {@code weapon}'s damage factor now: 1 for an {@code anti-sub} weapon (and in the Sonar Pulse's window). */
    double damage(WeaponSpec weapon) {
        return weapon.antiSub() || sonar ? 1 : factor(rules.damage());
    }

    /** {@code weapon}'s reach as a share of the field's height now: below 1 only for a reach-limited weapon. */
    double reach(WeaponSpec weapon) {
        return rules.reachWeapons().contains(weapon.slug()) ? factor(rules.reach()) : 1;
    }

    /** {@code weapon}'s range factor now: above 1 only for a torpedo-type weapon. */
    double torpedoRange(WeaponSpec weapon) {
        return rules.torpedoWeapons().contains(weapon.slug()) ? factor(rules.torpedoRange()) : 1;
    }

    /**
     * Whether {@code weapon}'s projectiles hit enemies on {@code layer} on their way ({@link
     * WeaponSpec.Delivery#reaches}, widened by the water): an {@code anti-sub} weapon (and, in the Sonar Pulse's
     * window, every weapon) reaches {@code sub}; under water a torpedo reaches every layer in the water, never {@code
     * high-air}. Bombs, shells, mines and depth charges hit nothing on their way.
     */
    boolean reaches(WeaponSpec weapon, Layer layer) {
        WeaponSpec.Delivery delivery = weapon.delivery();
        if (delivery.landing() || delivery.laid()) {
            return delivery.reaches(layer);
        }
        if (layer == Layer.SUB && (weapon.antiSub() || sonar)) {
            return true;
        }
        if (underwater && delivery == WeaponSpec.Delivery.TORPEDO) {
            return inWater(layer);
        }
        return delivery.reaches(layer);
    }

    /**
     * The layers a depth charge's burst reaches: under water every layer in the water ({@code air}, {@code low-air},
     * the sea floor {@code ground} and {@code sub}, never the ice ceiling {@code high-air}); above water {@code sub}
     * and the naval {@code ground}.
     */
    boolean chargeReaches(Layer layer) {
        return underwater ? inWater(layer) : layer == Layer.SUB || layer == Layer.GROUND;
    }

    private static boolean inWater(Layer layer) {
        return layer != Layer.HIGH_AIR && layer.touchable();
    }

    /** Only in a level with water, so the other levels hash as before. */
    void addTo(StateHash hash) {
        if (water) {
            hash.add(underwater ? 1 : 0).add(blend).add(sonar ? 1 : 0);
        }
    }

    MediumSpec spec() {
        return spec;
    }
}
