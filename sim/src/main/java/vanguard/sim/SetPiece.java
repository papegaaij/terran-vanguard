package vanguard.sim;

import java.util.Arrays;

/**
 * A huge set-piece unit flying its passes (design/enemies/space/leviathan): its centre follows
 * each pass's path with the pass's fixed heading; its parts are hit boxes at fixed offsets around
 * it that take the hits, fire while it is on the player's layer and stay wrecked once destroyed.
 * Its position is a function of the level time, so a restart only resets the parts. One instance
 * per {@link SetPieceSpec}, made when the sortie is.
 *
 * <p>A boss ({@link BossSpec}, design/enemies/bosses) is a set piece without passes: it arrives on
 * the level clock, descends (taking no damage), settles and sways; its chains hang parts on necks
 * that bend toward the player, and its phases fire their attacks and streams through the
 * {@link BossActions} until its vital part is destroyed. Everything it needs is allocated here, so
 * stepping it does not allocate.
 *
 * <p>This is the coordinator: its {@link SetPieceMotion motion}, {@link SetPieceParts parts} and {@link SetPieceGuns
 * guns} (with the phases) are helpers that own their state, next to the bosses' chains, windows, hatches, sweeps, aim
 * turns and arenas; it keeps the order of a step, the reset, the kill and the state hash, and every accessor the
 * presentation reads.
 */
public final class SetPiece implements Hashed {
    /** How a boss moves now (design/enemies/bosses, the poses of part G). */
    public enum Motion {
        /** Not arrived yet, or destroyed. */
        NONE,
        /** Its entrance: in from above the top edge to its hover height. */
        PASS,
        /** At its station, swaying. */
        HOLD,
        /** A phase's move: gliding to its new station and sinking (or rising) to its new layer. */
        DESCEND,
        /** A phase's move: turning in place into its new pose. */
        TURN
    }

    private final SetPieceSpec spec;
    private final BossSpec boss;
    private boolean destroyed;
    private int hitCooldown;
    /** The steps from its arrival (the bar appearing) to its kill; -1 while it lives. */
    private int killTicks;

    /** Its chains: the necks its parts hang on and the slam arms. */
    final BossChains chains;
    /** Its phases' windows (which parts they hold, which are open, the windows' clock). */
    final BossWindows windows;
    /** M5 part E: an arena boss's lanes, slams and surfacing; null for any other. */
    final SlamArena arena;
    /** {@link #arena} for the presentation, made once (stepping allocates nothing). */
    private final java.util.Optional<SlamArena> arenaView;
    /** M7 part F: a walking boss's passes, plate, vents and cone; null for any other. */
    final StrideArena stride;
    /** {@link #stride} for the presentation, made once. */
    private final java.util.Optional<StrideArena> strideView;
    /** M7 part H: a breaching boss's breach cycle, ridge and suction; null for any other. */
    final BreachArena breach;
    /** {@link #breach} for the presentation, made once. */
    private final java.util.Optional<BreachArena> breachView;
    /** M8 part H: a coiled boss's body, wall and script; null for any other. */
    final CoilArena coil;
    /** {@link #coil} for the presentation, made once. */
    private final java.util.Optional<CoilArena> coilView;
    /** M9 part H: a ringed boss's rings, fire arc and locks; null for any other. */
    final RingArena ring;
    /** {@link #ring} for the presentation, made once. */
    private final java.util.Optional<RingArena> ringView;
    /** M9 part H, step H1b: its rail lines; null for a boss without a line attack. */
    final BossLines lines;
    /** {@link #lines} for the presentation, made once. */
    private final java.util.Optional<BossLines> linesView;
    /** M9 part H, step H1b: its part shields; null for a boss without one. */
    final PartShields shields;
    /** {@link #shields} for the presentation, made once. */
    private final java.util.Optional<PartShields> shieldsView;
    /** M7 part F: a walking boss's hatches (its homing salvos' tell; round 44: its pods' armour). */
    final BossHatches hatches;
    /** M5 part H: its attacks' sweeps (design/enemies, Boss attacks and root bursts). */
    final BossSweeps sweeps;
    /** Round 40: its attacks' aim turns (the Siege Spire's pods). */
    final BossAimTurns aimTurns;
    /** Its parts: HP, hit clocks, places, contacts and damage. */
    final SetPieceParts parts;
    /** Its flight: passes, a boss's arrival, sway, moves, lift and pose. */
    final SetPieceMotion motion;
    /** Its guns and a boss's phases and attacks. */
    final SetPieceGuns guns;

    /** M5 part H: a sweep's beam hit the ship ({@link #sweepHit}). */
    static final int SWEPT_SHIP = 1;
    /** M5 part H: a sweep's beam hit the wingman. */
    static final int SWEPT_WINGMAN = 2;

    SetPiece(SetPieceSpec spec) {
        this.spec = spec;
        int count = spec.parts().size();
        boss = spec.boss().orElse(null);
        int phases = boss == null ? 0 : boss.phases().size();
        chains = new BossChains(this, boss, spec.parts());
        windows = new BossWindows(this, boss, count, phases);
        arena = boss != null && boss.arena().isPresent()
                ? new SlamArena(this, boss, boss.arena().get(), count)
                : null;
        arenaView = java.util.Optional.ofNullable(arena);
        stride = boss != null && boss.stride().isPresent() ? new StrideArena(this, boss, spec) : null;
        strideView = java.util.Optional.ofNullable(stride);
        breach = boss != null && boss.breach().isPresent() ? new BreachArena(this, boss, spec) : null;
        breachView = java.util.Optional.ofNullable(breach);
        coil = boss != null && boss.coil().isPresent() ? new CoilArena(this, boss, spec) : null;
        coilView = java.util.Optional.ofNullable(coil);
        ring = boss != null && boss.rings().isPresent() ? new RingArena(this, boss, spec) : null;
        ringView = java.util.Optional.ofNullable(ring);
        lines = BossLines.any(boss) ? new BossLines(this, boss, count) : null;
        linesView = java.util.Optional.ofNullable(lines);
        shields = PartShields.any(boss) ? new PartShields(this, boss, count) : null;
        shieldsView = java.util.Optional.ofNullable(shields);
        hatches = new BossHatches(
                this, boss, count, stride != null && boss.stride().get().hatchArmour());
        sweeps = new BossSweeps(this, boss);
        aimTurns = new BossAimTurns(this, boss);
        // The helpers read the ones made before them.
        parts = new SetPieceParts(this, spec, boss);
        motion = new SetPieceMotion(this, spec, boss);
        guns = new SetPieceGuns(this, spec, boss);
        reset();
    }

    /** Back to the level start: before its first pass, every part whole. */
    void reset() {
        motion.reset();
        destroyed = false;
        hitCooldown = 0;
        parts.reset();
        guns.reset();
        if (boss != null) {
            windows.reset();
            killTicks = -1;
            chains.reset();
            aimTurns.reset();
            sweeps.reset();
            if (arena != null) {
                arena.reset();
                motion.placeAt(motion.x, motion.stumpY());
            }
            hatches.reset();
            if (stride != null) {
                // M7 part F: off the screen above the top edge until it arrives.
                stride.reset();
                motion.placeAt(stride.x(), stride.y());
            }
            if (breach != null) {
                // M7 part H: buried above the top edge until it arrives.
                breach.reset();
                motion.placeAt(breach.mouthX(), breach.mouthY());
            }
            if (coil != null) {
                // M8 part H: under the water at its rest until it arrives.
                coil.reset();
                motion.placeAt(coil.headX(), coil.headY());
            }
            if (ring != null) {
                // M9 part H: the rings at rest, none lit or locked (the boss checkpoint restarts them fresh).
                ring.reset();
            }
            if (lines != null) {
                lines.reset();
            }
            if (shields != null) {
                shields.reset();
            }
            chains.place();
        }
    }

    /**
     * M5 part E: an arena boss's place on the ground layer: its centre's height above the screen's
     * bottom edge at the level start, so that it lies {@link BossSpec#hoverY()} above the bottom
     * edge once the scroll has halted in its arena (the sortie works it out).
     */
    void anchorAt(double ground) {
        motion.anchorAt(ground);
    }

    /**
     * M5 part E: the ground's scroll this step and whether the scroll has halted in the arena, read
     * by an arena boss as it moves (before {@link #update}).
     */
    void ground(double scroll, boolean halt) {
        motion.ground(scroll, halt);
    }

    /**
     * Moves it to where its pass has it at {@code levelTick}; returns whether its last pass ended
     * with it alive this step (it escaped). A boss arrives at its time and then flies on its own
     * clock (the level clock may halt in its arena); it never escapes.
     */
    boolean update(int levelTick) {
        if (boss != null) {
            motion.keepPrev(false);
            parts.tickHits();
            if (hitCooldown > 0) {
                hitCooldown--;
            }
            motion.moveBoss(levelTick);
            return false;
        }
        motion.keepPrev(true);
        if (hitCooldown > 0) {
            hitCooldown--;
        }
        parts.tickHits();
        if (destroyed || motion.escaped) {
            return false;
        }
        return motion.fly(levelTick);
    }

    /** Whether part {@code p} fires a shot this step: it lives, has a gun and the unit is on the player's layer. */
    boolean trigger(int p) {
        return guns.trigger(p);
    }

    /** M8 part H: the coiled boss reads where the ship is before it moves (a bight, a gap, the column follow it). */
    void coilTargets(double shipX, double shipY) {
        motion.coilTargets(shipX, shipY);
    }

    /** M7 part H: the breaching boss reads where the ship is before it moves (its next point may follow it). */
    void breachTargets(double shipX, double shipY) {
        motion.breachTargets(shipX, shipY);
    }

    /** M7 part F: the walking boss reads where the ship and Rook are ({@code rookIn}: he flies) before it moves. */
    void strideTargets(double shipX, double shipY, double rookX, double rookY, boolean rookIn) {
        motion.strideTargets(shipX, shipY, rookX, rookY, rookIn);
    }

    /**
     * A boss's step after it moved: its necks bend toward the ship, its windows close in time, its
     * phase ends on its parts or its timer, and once engaged (and not moving) its attacks, windows
     * and streams run; they reach the level through {@code actions} only while {@code firing}.
     */
    void act(double shipX, double shipY, BossActions actions, boolean firing) {
        guns.act(shipX, shipY, actions, firing);
    }

    /**
     * M9 part H: whether part {@code p} may fire now (for the looks, Rook and the autopilot): a ringed boss's battery
     * while it lives inside its fire arc ({@link RingArena#armed}); any other boss's part while it lives.
     */
    public boolean partArmed(int p) {
        return parts.partHp[p] > 0 && (ring == null || ring.battery(p) < 0 || ring.armed(p));
    }

    /** M7 part F: whether part {@code p}'s hatch is open (its homing salvo's tell and the salvo). */
    public boolean partHatchOpen(int p) {
        return hatches.open(p);
    }

    /** M5 part H: whether attack {@code a}'s sweep is in its telegraph now. */
    public boolean sweepTelegraphing(int a) {
        return sweeps.telegraphing(a);
    }

    /** M5 part H: whether attack {@code a}'s beam sweeps now (it hurts what it touches, once per sweep each). */
    public boolean sweepBeaming(int a) {
        return sweeps.beaming(a);
    }

    /** M5 part H: the attacks whose sweeps run, for the presentation (as {@link BossSpec#attacks()}). */
    public int attackCount() {
        return sweeps.count();
    }

    /** Round 40: the part glowing in attack {@code a}'s aim turn (its aimed shot comes next); -1 for none. */
    public int aimPart(int a) {
        return aimTurns.part(a);
    }

    /** Round 40: how far attack {@code a}'s aim glow has run, 0–1 (0 without a glowing part). */
    public double aimGlowShare(int a) {
        return aimTurns.glowShare(a);
    }

    /** Round 40: whether part {@code p} glows in an aim turn (the tell before its aimed shot). */
    public boolean partAimGlowing(int p) {
        return aimTurns.glowing(p);
    }

    /** M5 part H: attack {@code a}'s beam's origin now, px from the left: its part's centre. */
    public double sweepOriginX(int a) {
        return sweeps.originX(a);
    }

    /** M5 part H: attack {@code a}'s beam's origin now, px up: its sweep's origin below its part's centre. */
    public double sweepOriginY(int a) {
        return sweeps.originY(a);
    }

    /** M5 part H: attack {@code a}'s sweep's first edge, radians clockwise from straight down (the telegraph's line). */
    public double sweepFrom(int a) {
        return sweeps.from(a);
    }

    /** M5 part H: attack {@code a}'s sweep's last edge, radians clockwise from straight down. */
    public double sweepTo(int a) {
        return sweeps.to(a);
    }

    /**
     * M5 part H: attack {@code a}'s beam's heading, radians clockwise from straight down, moving evenly
     * from {@link #sweepFrom} to {@link #sweepTo} over the sweep; {@code alpha} interpolates between steps.
     */
    public double sweepBeam(int a, double alpha) {
        return sweeps.beam(a, alpha);
    }

    /** M5 part H: whether attack {@code a}'s current sweep hit {@code what} ({@link #SWEPT_SHIP}, {@link #SWEPT_WINGMAN}) already. */
    boolean sweepHit(int a, int what) {
        return sweeps.hit(a, what);
    }

    /** M5 part H: attack {@code a}'s current sweep hit {@code what}: it does not again in this sweep. */
    void markSweepHit(int a, int what) {
        sweeps.markHit(a, what);
    }

    /**
     * Whether part {@code p} takes no damage now: a boss's parts before it engages (its descent)
     * and during a phase's move, its fire-only parts, the parts a later phase exposes before it,
     * and a window part while its window is shut. A shot on it glances off.
     */
    public boolean partShielded(int p) {
        return parts.partShielded(p);
    }

    /** Round 44: whether part {@code p} takes damage only while its hatch is open (a walking boss's pod). */
    public boolean partHatchArmoured(int p) {
        return hatches.armoured(p);
    }

    /** Whether part {@code p} is fire-only: it fires, never takes damage, is not in the bar and pays nothing. */
    public boolean partArmoured(int p) {
        return parts.armoured[p];
    }

    /** Whether the current phase's windows hold part {@code p} (it opens and shuts). */
    public boolean partWindowed(int p) {
        return parts.partWindowed(p);
    }

    /** Whether part {@code p}'s window is open now and the part lives (it takes damage, unless the boss is moving). */
    public boolean partOpen(int p) {
        return parts.partOpen(p);
    }

    /** Seconds part {@code p}'s window stays open; 0 shut. */
    public double partOpenSeconds(int p) {
        return windows.openTicks(p) * SimStep.SECONDS;
    }

    /** The current phase's window open time, s (0 without windows): with {@link #partOpenSeconds} the opening's progress. */
    public double windowOpenSeconds() {
        if (boss == null) {
            return 0;
        }
        var windows = boss.phases().get(guns.phase).windows();
        return windows.isPresent() ? windows.get().openSeconds() : 0;
    }

    /** Whether a shot of {@code size} at (sx, sy) touches one of its armoured neck segments (not a slam arm's). */
    boolean neckTouches(Hitbox size, double sx, double sy) {
        return chains.neckTouches(size, sx, sy);
    }

    /**
     * M7 part A: where a beam's column (centred on {@code cx}, {@code halfWidth} either side, running up
     * from {@code fromY}) first touches one of its armoured neck segments (not a slam arm's); {@link
     * Double#POSITIVE_INFINITY} for none.
     */
    double neckColumn(double cx, double halfWidth, double fromY) {
        return chains.neckColumn(cx, halfWidth, fromY);
    }

    /**
     * M7 part A: where a beam's column (see {@link #neckColumn}) first touches part {@code p}: its hit
     * box, or a slam arm's segments; {@link Double#POSITIVE_INFINITY} for none (a lifted boss's stump
     * is scenery, as for shots).
     */
    double partColumn(int p, double cx, double halfWidth, double fromY) {
        return parts.partColumn(p, cx, halfWidth, fromY);
    }

    /**
     * M5 part E: part {@code p}'s current layer: an arena boss's part's own ({@code sub} under the
     * water, {@code ground} on the surface), else the unit's.
     */
    public Layer partLayer(int p) {
        return parts.partLayer(p);
    }

    /**
     * Whether a shot of {@code size} at (sx, sy) touches part {@code p}: its hit box, and (M5 part E)
     * a slam arm's segments, which are the arm's own hit boxes.
     */
    boolean partTouches(int p, Hitbox size, double sx, double sy) {
        return parts.partTouches(p, size, sx, sy);
    }

    /** Whether a blast of {@code radius} at (bx, by) reaches part {@code p} (its hit box or a slam arm's segment). */
    boolean partInBlast(int p, double bx, double by, double radius) {
        return parts.partInBlast(p, bx, by, radius);
    }

    /** Whether part {@code p} overlaps the play field (its hit box, or a slam arm's segment). */
    boolean partOnField(int p) {
        return parts.partOnField(p);
    }

    /**
     * M7 part F: whether a shot of {@code size} at (sx, sy) meets a walking boss's front plate (it glances); M8 part H:
     * or a coiled boss's armoured coil.
     */
    boolean plateTouches(Hitbox size, double sx, double sy) {
        return parts.plateTouches(size, sx, sy);
    }

    /** M7 part F: where a beam's column first touches a walking boss's front plate (M8 part H: a coil); +∞ for none. */
    double plateColumn(double cx, double halfWidth, double fromY) {
        return parts.plateColumn(cx, halfWidth, fromY);
    }

    /**
     * Part {@code p} takes damage from a blast at (hx, hy): times an open weak spot's multiplier there
     * (M5 part E: the Kraken's eyes), else the part's own; returns whether that destroyed it.
     */
    boolean damagePartAt(int p, double amount, double hx, double hy) {
        return parts.damagePartAt(p, amount, hx, hy, 0, 0);
    }

    /**
     * Part {@code p} takes damage from a shot at (hx, hy) flying along (dirX, dirY): times the
     * multiplier of an open weak spot on its line ahead (M5 part E: the Kraken's eyes), else the
     * part's own; returns whether that destroyed it.
     */
    boolean damagePartAt(int p, double amount, double hx, double hy, double dirX, double dirY) {
        return parts.damagePartAt(p, amount, hx, hy, dirX, dirY);
    }

    /**
     * Part {@code p} takes damage, times its multiplier; returns whether that destroyed it (M8 part H: the part whose
     * HP it took, {@link #hpPart}).
     */
    boolean damagePart(int p, double amount) {
        return parts.damagePart(p, amount);
    }

    /**
     * M8 part H: the part whose HP a hit on part {@code p} takes: a coiled boss's implant's is the head's; any other
     * part's its own. A hit that destroys reports this part.
     */
    int hpPart(int p) {
        return parts.hpPart(p);
    }

    /**
     * M7 part A, the Orbital Lance (design/player/specials): part {@code p} takes {@code amount} as it
     * is, no weak-point multiplier; returns whether that destroyed it.
     */
    boolean damagePartFlat(int p, double amount) {
        return parts.damagePartFlat(p, amount);
    }

    /** Destroys part {@code p} outright (the vital part took the rest with it); returns whether it still lived (and pays). */
    boolean wreckPart(int p) {
        return parts.wreckPart(p);
    }

    /** The unit is destroyed: it leaves the play field at once (its death plays where it was). */
    void destroy() {
        if (boss != null && killTicks < 0) {
            killTicks = Math.max(0, motion.bossTicks);
        }
        destroyed = true;
        motion.vanish();
        if (stride != null) {
            // M7 part F: its cone goes cold as it topples.
            stride.died();
        }
        if (breach != null) {
            // M7 part H: no suction, no bite.
            breach.died();
        }
        if (coil != null) {
            // M8 part H: nothing fires, no sweep, no wall.
            coil.died();
        }
        if (ring != null) {
            // M9 part H: the rings stop; the batteries cook off.
            ring.died();
        }
        if (lines != null) {
            // M9 part H, step H1b: no rail line after the kill.
            lines.reset();
        }
    }

    /** The body touching the ship: whether it deals its contact damage now (at most once per its interval). */
    boolean strike() {
        if (hitCooldown > 0) {
            return false;
        }
        hitCooldown = SimStep.ticks(SetPieceSpec.HIT_INTERVAL_SECONDS);
        return true;
    }

    /** The state in the order it was hashed before the helpers were split out (the golden replays pin it). */
    @Override
    public void addTo(StateHash hash) {
        SetPieceMotion m = motion;
        SetPieceGuns g = guns;
        hash.add(m.pass)
                .add(m.present ? 1 : 0)
                .add(destroyed ? 1 : 0)
                .add(m.escaped ? 1 : 0)
                .add(m.onPlane ? 1 : 0)
                .add(m.layer.ordinal())
                .add(m.x)
                .add(m.y)
                .add(m.altitude)
                .add(hitCooldown);
        for (int p = 0; p < parts.partHp.length; p++) {
            hash.add(parts.partHp[p])
                    .add(parts.partTicksSinceHit[p])
                    .add(g.volleyTicks[p])
                    .add(g.burstLeft[p])
                    .add(g.burstTicks[p]);
        }
        if (boss != null) {
            hash.add(m.bossTicks)
                    .add(m.swayTicks)
                    .add(g.phase)
                    .add(g.turn)
                    .add(g.turnTicks)
                    .add(m.swayStart)
                    .add(g.engaged ? 1 : 0)
                    .add(m.moveTicks)
                    .add(m.moveFromX)
                    .add(m.moveFromY)
                    .add(m.moveFromAltitude)
                    .add(m.moveFromLayer.ordinal())
                    .add(m.anchorX)
                    .add(m.anchorY)
                    .add(m.pose)
                    .add(m.fromPose)
                    .add(g.phaseTicks)
                    .add(g.timeouts)
                    .add(windows.ticks())
                    .add(windows.next())
                    .add(windows.openings())
                    .add(m.prevAltitude)
                    .add(g.streamTicks)
                    .add(g.streamLeft)
                    .add(g.streamUnitTicks)
                    .add(g.streamUnits)
                    .add(killTicks);
            chains.addTo(hash);
            for (int a = 0; a < g.attackTicks.length; a++) {
                hash.add(g.attackTicks[a])
                        .add(g.attackBurstLeft[a])
                        .add(g.attackBurstTicks[a])
                        .add(g.attackPart[a])
                        .add(g.attackNext[a])
                        .add(g.spiralAngles[a])
                        .add(g.spiralTicks[a]);
            }
            windows.addOpenTicks(hash);
            if (arena != null) {
                // M5 part E: only an arena boss (Level 11) hashes these.
                hash.add(m.approaching ? 1 : 0);
                arena.addTo(hash);
            }
            if (m.lifts) {
                // M5 part H: only a boss that lifts (Level 14) hashes these.
                hash.add(m.lifted ? 1 : 0).add(m.liftPose).add(m.liftEndTicks);
                for (boolean stay : m.stays) {
                    hash.add(stay ? 1 : 0);
                }
            }
            if (breach != null) {
                // M7 part H: only a breaching boss (Level 21) hashes these.
                breach.addTo(hash);
            }
            if (coil != null) {
                // M8 part H: only a coiled boss (Level 28) hashes these.
                coil.addTo(hash);
            }
            if (ring != null) {
                // M9 part H: only a ringed boss (Level 35) hashes these (step H1b: its hatches, lines and shields too).
                ring.addTo(hash);
                hatches.addTo(hash);
            }
            if (lines != null) {
                lines.addTo(hash);
            }
            if (shields != null) {
                shields.addTo(hash);
            }
            if (stride != null) {
                // M7 part F: only a walking boss (Level 19) hashes these.
                stride.addTo(hash);
                hatches.addTo(hash);
            }
            // Round 40: only a boss with aim turns hashes these; M5 part H: only a boss that sweeps (Level 14) these.
            aimTurns.addTo(hash);
            sweeps.addTo(hash);
        }
    }

    double x() {
        return motion.x;
    }

    /** M5 part E: its arena (lanes, slams and surfacing); null for any other boss. */
    SlamArena slamArena() {
        return arena;
    }

    double y() {
        return motion.y;
    }

    public SetPieceSpec spec() {
        return spec;
    }

    /** Its enemy's slug ({@code leviathan}): the sprites and the death effect go by it. */
    public String slug() {
        return spec.slug();
    }

    /** Whether it is on screen in a pass (not before, between or after its passes, nor once destroyed). */
    public boolean present() {
        return motion.present;
    }

    /**
     * M5 part E: whether an arena boss is scrolling in with the ground before it arrives: drawn (on
     * the water, with its platform) but not {@link #present()}: no bar, no hits, no targets.
     */
    public boolean approaching() {
        return motion.approaching;
    }

    /** M5 part E: an arena boss's lanes, slams and surfacing; empty for any other. */
    public java.util.Optional<SlamArena> arena() {
        return arenaView;
    }

    /** M7 part F: a walking boss's passes, plate, vents and flame cone; empty for any other. */
    public java.util.Optional<StrideArena> stride() {
        return strideView;
    }

    /** M7 part H: a breaching boss's breach cycle, ridge and suction; empty for any other. */
    public java.util.Optional<BreachArena> breach() {
        return breachView;
    }

    /** M8 part H: a coiled boss's body, wall and script; empty for any other. */
    public java.util.Optional<CoilArena> coil() {
        return coilView;
    }

    /** M9 part H: a ringed boss's rings, fire arc and locks; empty for any other. */
    public java.util.Optional<RingArena> ring() {
        return ringView;
    }

    /** M9 part H, step H1b: its rail lines (the look, Rook, the autopilot); empty for a boss without a line attack. */
    public java.util.Optional<BossLines> lines() {
        return linesView;
    }

    /** M9 part H, step H1b: its part shields (the core's hex shield); empty for a boss without one. */
    public java.util.Optional<PartShields> shields() {
        return shieldsView;
    }

    /** M9 part H, step H1b: its shield events go to the sortie's {@code events}. */
    void useEvents(SimEvents events) {
        if (shields != null) {
            shields.events(events);
        }
    }

    /**
     * M9 part H, step H1b: a {@code shield-breaker} hit on part {@code p} at (hx, hy) (the Tesla Coil Pod's arc): its
     * shield emptied and its refill held, the whole of {@code amount} to the part; returns whether that destroyed it.
     */
    boolean damagePartBreaking(int p, double amount, double hx, double hy) {
        if (shields == null || !shields.fitted(p)) {
            return damagePartAt(p, amount, hx, hy);
        }
        shields.breaking(p);
        boolean destroyed = damagePartAt(p, amount, hx, hy);
        // Its refill waits the breaker's hold, not the hold and then the delay (the hit just counted as one).
        shields.breaking(p);
        return destroyed;
    }

    /** M9 part H, step H1b: the EMP Burst empties its touchable parts' shields, their refill held {@code ticks}. */
    void stripShields(int ticks) {
        if (shields != null) {
            shields.strip(ticks);
        }
    }

    /**
     * M5 part H (design/enemies/bosses/siege-spire; decision H4 = a): whether an anchored boss has left
     * its anchor (from its lift's start on): the stump stands at ({@link #stumpX()}, {@link #stumpY()}).
     */
    public boolean lifted() {
        return motion.lifted;
    }

    /** M5 part H: whether the lift runs now (the glide off the anchor; invulnerable, holding its fire). */
    public boolean lifting() {
        return motion.lifted && motion.moveTicks >= 0;
    }

    /** Whether a phase's move runs now (a glide and a turn, or a lift): no damage, no fire. */
    boolean moving() {
        return motion.moveTicks >= 0;
    }

    /**
     * M5 part H: how far the lift's glide is, between the previous and the current step ({@code alpha}
     * in [0, 1]): 0 before it, 1 once the boss holds at its new station.
     */
    public double liftShare(double alpha) {
        return motion.liftShare(alpha);
    }

    /** M5 part H: whether the lift ended in this step (the boss engages at its new station). */
    boolean liftEndedNow() {
        return motion.liftEndTicks >= 0 && motion.liftEndTicks == motion.bossTicks;
    }

    /**
     * M5 part H: the stump's centre (where an anchored boss's centre lies on the ground), px from the
     * left; it scrolls with the ground after the kill.
     */
    public double stumpX() {
        return motion.stumpX();
    }

    /** M5 part H: the stump's centre now, px up from the bottom edge (y up), with the ground. */
    public double stumpY() {
        return motion.stumpY();
    }

    /** M5 part H: whether part {@code p} stayed at the anchor in the lift (the stump's: drawn there, never hit). */
    public boolean partStays(int p) {
        return motion.lifted && motion.stays[p];
    }

    public boolean destroyed() {
        return destroyed;
    }

    /** Whether it ended its last pass alive. */
    public boolean escaped() {
        return motion.escaped;
    }

    /** The index of the pass it flies (or will fly next) in {@link SetPieceSpec#passes()}. */
    public int pass() {
        return motion.pass;
    }

    /** The name of that pass ({@code cross}, {@code descend}); empty after the last. */
    public String passName() {
        return motion.pass < spec.passes().size()
                ? spec.passes().get(motion.pass).name()
                : "";
    }

    /** The layer it flies on now: {@code high-air} out of reach, {@code air} once descended. */
    public Layer layer() {
        return motion.layer;
    }

    /** Whether it is on the player's layer: its guns fire and its body collides. */
    public boolean onPlane() {
        return motion.onPlane;
    }

    /** Where it faces, radians clockwise from straight down the screen, as {@link Enemy#facing()}; fixed per pass. */
    public double facing() {
        return motion.present && boss == null ? spec.passes().get(motion.pass).headingRadians() : 0;
    }

    /** Its boss script; empty for a set piece flying passes. */
    public java.util.Optional<BossSpec> boss() {
        return spec.boss();
    }

    /** Seconds since a boss arrived (its bar appeared); negative before. */
    public double bossSeconds() {
        return motion.bossTicks * SimStep.SECONDS;
    }

    /** Steps since a boss arrived; -1 before. */
    int bossTicks() {
        return motion.bossTicks;
    }

    /** Whether a boss has settled after its descent (it takes damage and attacks). */
    public boolean settled() {
        return motion.swayTicks >= 0;
    }

    /** Steps since a boss settled; -1 before. */
    int swayTicks() {
        return motion.swayTicks;
    }

    /** A boss's phase, 0 for its first. */
    public int phase() {
        return guns.phase;
    }

    /** The seconds from a boss's arrival to its kill; negative while it lives (or before it arrived). */
    public double killSeconds() {
        return killTicks < 0 ? -1 : killTicks * SimStep.SECONDS;
    }

    /** The boss bar: its parts' remaining HP as a share of their total, 0–1 (fire-only parts left out). */
    public double barShare() {
        return parts.barShare();
    }

    /** A boss's neck segments, all chains in order. */
    public int segmentCount() {
        return chains.segmentCount();
    }

    /** Neck segment {@code i}'s centre offset from its centre, px to the right. */
    public double segmentOffsetX(int i) {
        return chains.segmentOffsetX(i);
    }

    /** Neck segment {@code i}'s centre offset from its centre, px up. */
    public double segmentOffsetY(int i) {
        return chains.segmentOffsetY(i);
    }

    /** The chain neck segment {@code i} belongs to. */
    public int segmentChain(int i) {
        return chains.segmentChain(i);
    }

    /** Chain {@code c}'s angle at its piece {@code k} (0 at the anchor; its segments, then its tip), radians counter-clockwise. */
    public double chainAngle(int c, int k) {
        return chains.angle(c, k);
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderX(double alpha) {
        return motion.prevX + (motion.x - motion.prevX) * alpha;
    }

    /** Position between the previous and the current step; {@code alpha} in [0, 1]. */
    public double renderY(double alpha) {
        return motion.prevY + (motion.y - motion.prevY) * alpha;
    }

    /** How high above the player's layer it flies: 1 on {@code high-air}, 0 on {@code air}, in between while it descends or rises. */
    public double altitude(double alpha) {
        return motion.prevAltitude + (motion.altitude - motion.prevAltitude) * alpha;
    }

    public int partCount() {
        return parts.partHp.length;
    }

    /** Whether part {@code p} is destroyed: it no longer fires or takes hits and is drawn wrecked. */
    public boolean partWrecked(int p) {
        return parts.partHp[p] <= 0;
    }

    /** Part {@code p}'s HP left (at most its spec's, 0 or less once wrecked). */
    public double partHp(int p) {
        return parts.partHp[p];
    }

    /** Simulation steps since part {@code p} was last hit, for its hit flash; {@link Integer#MAX_VALUE} before. */
    public int partTicksSinceHit(int p) {
        return parts.partTicksSinceHit[p];
    }

    /** Part {@code p}'s centre on the screen now: its offset turned with the heading. */
    public double partX(int p) {
        return parts.partX(p);
    }

    /** Part {@code p}'s centre on the screen now (y up). */
    public double partY(int p) {
        return parts.partY(p);
    }

    /**
     * Part {@code p}'s offset from the centre on the screen, px to the right: its data offset turned
     * with the pass's heading (fixed per pass), so {@code renderX(alpha) + partOffsetX(p)} draws it.
     * A boss's is its pose's offset, grown by the high-air scale with its altitude ({@link #scale()}):
     * already the screen offset, not to be scaled again.
     */
    public double partOffsetX(int p) {
        return parts.partOffsetX(p);
    }

    /** Part {@code p}'s offset from the centre on the screen, px up. */
    public double partOffsetY(int p) {
        return parts.partOffsetY(p);
    }

    /** A boss's size factor now: {@link BossSpec#HIGH_AIR_SCALE} on high air, 1 on the play plane, between them as it descends. */
    public double scale() {
        return motion.scale();
    }

    /** Its armoured body's hit box now: a boss's pose's, else its spec's. */
    public Hitbox body() {
        return boss == null ? spec.body() : parts.poseBody[motion.pose];
    }

    /** How a boss moves now: its entrance, holding, or a phase's descent or turn. */
    public Motion motion() {
        return motion.motion();
    }

    /** A boss's pose: an index into {@link BossSpec#poses()} (0, the arrival pose, without poses); it switches half-way through a turn. */
    public int pose() {
        return motion.pose;
    }

    /** The pose a move turns it from (its pose when it holds). */
    public int turnFromPose() {
        return motion.fromPose;
    }

    /** The pose a move turns it into (its pose when it holds). */
    public int turnToPose() {
        return motion.turnToPose();
    }

    /** The name of its pose; empty without poses. */
    public String poseName() {
        return boss == null || boss.poses().isEmpty()
                ? ""
                : boss.poses().get(motion.pose).name();
    }

    /**
     * How far through its turn it is between the previous and the current step: 0 before the turn
     * (and when not moving), 1 at its end, between them the turn's frames.
     */
    public double turnShare(double alpha) {
        return motion.turnShare(alpha);
    }

    /** Whether its phases run (from its arrival or its settle). */
    public boolean engaged() {
        return guns.engaged;
    }

    /** Whether phase {@code phase} ended on its timer (a timeout) rather than on its parts. */
    public boolean endedOnTimeout(int phase) {
        return (guns.timeouts & (1L << phase)) != 0;
    }

    /** Seconds since the current phase engaged (after its move): its timeout's clock. */
    public double phaseSeconds() {
        return guns.phaseTicks * SimStep.SECONDS;
    }

    /** Whether every part is wrecked. */
    boolean allWrecked() {
        return parts.allWrecked();
    }

    @Override
    public String toString() {
        return spec.slug() + " pass " + motion.pass + " at " + motion.x + ", " + motion.y + " parts "
                + Arrays.toString(parts.partHp);
    }
}
