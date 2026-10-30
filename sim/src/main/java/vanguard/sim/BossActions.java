package vanguard.sim;

/** What a boss does to the rest of the level; the sortie implements it once. */
interface BossActions {
    /** One aimed shot from (x, y) with {@code gun}'s speed and damage. */
    void aimed(double x, double y, EnemyGun gun);

    /**
     * Round 34, P2: one shot of {@code gun} from (x, y) straight down the screen (a part with {@code
     * aim: down}); by default a bullet at its speed and damage.
     */
    default void down(double x, double y, EnemyGun gun) {
        bullet(x, y, -StrictMath.PI / 2, gun.bulletSpeed(), gun.damage());
    }

    /** A ring of {@code count} bullets from (x, y). */
    void ring(double x, double y, int count, double speed, double damage);

    /** One bullet from (x, y) at {@code angle} radians (0 = right, y up). */
    void bullet(double x, double y, double angle, double speed, double damage);

    /** One unit of {@code stream} from the left or the right edge. */
    void release(BossSpec.Stream stream, boolean left);

    /** It entered phase {@code phase} (after the first). */
    void phase(int phase);

    /**
     * Phase {@code phase} ended on its timer (a timeout), just before the next one is entered
     * with {@link #phase(int)}.
     */
    default void timeout(int phase) {}

    /**
     * One unit of {@code spawn} leaves an open window part at (x, y), flying straight out at
     * {@code angle} radians (0 = right, y up) at the spawn's speed, gliding and holding as the
     * spawn says.
     */
    default void launch(BossSpec.Spawn spawn, double x, double y, double angle) {}

    /** M5 part E: lane {@code lane} (1-based) is telegraphed for a slam. */
    default void telegraph(int lane) {}

    /**
     * M5 part E: a slam strikes lane {@code lane} (1-based): the impact on the ship, the escort and
     * the convoy ships in it (the splash bullets come through {@link #bullet}).
     */
    default void slam(int lane) {}

    /** M5 part E: part {@code part} starts to surface ({@code up}) or to dive. */
    default void surface(int part, boolean up) {}

    /** M5 part E: the phase's release (the arena's jelly field) is let in, once. */
    default void release(BossSpec.Release release) {}

    /** M5 part E: a phase drops {@code pickup} at (x, y) as it begins (the shield cell). */
    default void drop(PickupType pickup, double x, double y) {}

    /**
     * M5 part E: the lanes holding a convoy ship a slam hurts, bit {@code n} for lane {@code n}
     * (the slams' "nearest ship" choice); 0 without.
     */
    default int shipLanes() {
        return 0;
    }

    /** M5 part H: a lob of {@code gun}'s mortar from (x, y) at the ship's position now (the boss's {@code lob}). */
    default void lob(double x, double y, EnemyGun gun) {}

    /**
     * M5 part H: unit {@code unit} of {@code units} of {@code spawn}, an ambush launch, leaves window
     * part {@code part} at (x, y) (the Siege Spire's maw's Wraiths).
     */
    default void ambush(BossSpec.Spawn spawn, int part, int unit, int units, double x, double y) {}

    /**
     * M5 part H: attack {@code a}'s sweep starts its telegraph ({@code telegraph}) or its beam, from
     * (x, y).
     */
    default void sweep(int a, boolean telegraph, double x, double y) {}

    /**
     * Round 40: part {@code part} starts its aim turn's glow (the tell before its aimed shot; the Siege
     * Spire's pods), at (x, y).
     */
    default void aimTell(int part, double x, double y) {}

    /**
     * M7 part F: one volley of {@code gun} from (x, y) along {@code aim} (radians clockwise from straight
     * down; a walking boss's chin burst along its heading, its parts' {@code aim: down}); by default a bullet
     * at its speed and damage.
     */
    default void along(double x, double y, double aim, EnemyGun gun) {
        bullet(x, y, StrictMath.atan2(-Trig.cos(aim), -Trig.sin(aim)), gun.bulletSpeed(), gun.damage());
    }

    /** M7 part F: part {@code part}'s hatch opens at (x, y), its homing salvo's tell ({@link EnemyGun.HomingSpec}). */
    default void hatch(int part, double x, double y) {}

    /** M7 part F: a walking boss's flame cone starts its tell at its flamer (x, y). */
    default void coneTell(double x, double y) {}

    /** M7 part F: a walking boss's flame cone starts to burn at its flamer (x, y). */
    default void coneBurn(double x, double y) {}

    /** Round 44: a walking boss's rail lance starts its charge at (x, y). */
    default void lanceCharge(double x, double y) {}

    /** Round 44: the rail lance fires from (x, y). */
    default void lanceFire(double x, double y) {}

    /** Round 44: a walking boss braces for a stomp at (x, y). */
    default void stompTell(double x, double y) {}

    /** Round 44: it stomps at (x, y). */
    default void stomp(double x, double y) {}

    /**
     * M7 part H: a breaching boss's cycle event {@code type} ({@link SimEvents.Type#BREACH_TELL}, {@code
     * BREACH_RISE}, {@code BITE_TELL}, {@code BITE_SNAP}, {@code BREACH_DIVE}) at (x, y) with its {@code value}.
     */
    default void breach(SimEvents.Type type, double x, double y, int value) {}

    /**
     * M7 part H: a breaching boss's jaws snap at (x, y): the ship and Rook within {@code radius} px of it take
     * {@code damage}; returns what it caught ({@link BreachArena#BITE_SHIP}, {@link BreachArena#BITE_WINGMAN} bits).
     */
    default int bite(double x, double y, double radius, double damage) {
        return 0;
    }

    /**
     * Step SIM-K: a breaching boss's eruption ring of {@code radius} px about (x, y) reaches the ship and Rook within
     * it, except those in {@code spared} (bits as {@link #bite}'s, hit before), who take {@code damage}; returns
     * whom it caught now.
     */
    default int strike(double x, double y, double radius, double damage, int spared) {
        return 0;
    }

    /**
     * M7 part H, step H3: a breaching boss tears a rock of {@code rocks} loose toward its mouth at (mouthX,
     * mouthY) ({@link PulledDebris#tear}).
     */
    default void tearLoose(PulledDebris rocks, double mouthX, double mouthY) {}

    /** M8 part H: a coiled boss's event {@code type} at (x, y) with its {@code value} ({@link CoilArena}). */
    default void coil(SimEvents.Type type, double x, double y, int value) {}

    /** M9 part H: a ringed boss's event {@code type} at (x, y) with its {@code value} ({@link RingArena}). */
    default void ring(SimEvents.Type type, double x, double y, int value) {}

    /**
     * M9 part H, step H1b: a ringed boss's SAM battery at (x, y) launches one missile of the homing {@code gun} at the
     * ship, under the station's own cap (its gun's most alive, across its batteries); by default an aimed shot.
     */
    default void stationMissile(double x, double y, EnemyGun gun) {
        aimed(x, y, gun);
    }

    /**
     * M9 part H, step H1b: a ringed boss's rim bay at (x, y) launches one unit of {@code launcher} along {@code angle}
     * (radians, y up: (cos, sin) of it), flying straight its launcher's time before its chase.
     */
    default void bayLaunch(EnemySpec.Launcher launcher, double x, double y, double angle) {}

    /**
     * Step BOSS70: a draw from the simulation's seeded random, in [0, 1) (a replay draws the same); by default 0.
     */
    default double draw() {
        return 0;
    }

    /**
     * Step BOSS70 (design/enemies/bosses/iron-sovereign, the mine ring): one line mine of {@code gun} thrown from (x, y)
     * to (toX, toY) over {@code throwSeconds}, held there (it arms, blinks and bursts by its own rule), gone after {@code
     * lifeSeconds} untouched.
     */
    default void coreMine(
            EnemyGun gun, double x, double y, double toX, double toY, double throwSeconds, double lifeSeconds) {}

    /**
     * Step BOSS70 (the wreck throw): boss part {@code part}'s wreck at (x, y) is hurled at the ship's spot as a missile of
     * {@code gun}; returns whether it flies (false while the gun's most thrown wrecks fly).
     */
    default boolean throwWreck(EnemyGun gun, double x, double y, int part) {
        return false;
    }

    /** Step BOSS70: the open core drew {@code play} (the radio's first-of-a-kind lines), {@code low} below its share. */
    default void corePlay(RingSpec.Play play, boolean low) {}

    /** M8 part H: a coiled boss dives (the radio's {@code boss-dive}). */
    default void dive() {}

    /**
     * M8 part H: a coiled boss's swinging bight or lunge ({@code what}: {@link CoilArena#SWEEP}, {@link
     * CoilArena#LUNGE}) reaches the ship and the escort where {@link CoilArena#reaches} says, except those in
     * {@code spared} ({@link CoilArena#STRUCK_SHIP}, {@link CoilArena#STRUCK_ESCORT} bits, hit before): they take
     * {@code damage}; returns whom it caught now.
     */
    default int coilStrike(CoilArena coil, int what, double damage, int spared) {
        return 0;
    }

    /**
     * M8 part H: a coiled boss sheds its Eel Swarm at shed point {@code point} ({@link CoilSpec.Shed}); returns the
     * chain's serial (-1 when none could be let in).
     */
    default int shed(int point) {
        return -1;
    }

    /** M8 part H: whether the shed chain with serial {@code serial} still swims. */
    default boolean shedAlive(int serial) {
        return false;
    }
}
