package vanguard.sim;

/**
 * A {@link Sortie}'s level clock and scroll in play (split out of {@link Sortie}): the clock's advance (a fraction of
 * a step per step over a hold zone, {@link Sortie#CLOCK_ONE}), the arena's halt while its boss lives, an early death's
 * jump, the hold after the boss, the ramp or ease out of the arena, a crawling arena's crawl, the eases into an arena
 * of speed 0 and the timed halts, and the direction of travel. The clock itself ({@link Sortie#levelTick} ...) and the
 * ground's scroll stay on the sortie, which the passes and the checkpoint read.
 *
 * <p>M5 part C (design/campaign Level 09, user decisions D2, D3 and D5 = a): the level clock is <em>script
 * time</em>. Over a {@link ObjectiveSpec.Hold hold zone} the scroll eases down and the level clock advances at the
 * current scroll speed ÷ the section's speed, a fraction of a step per step in fixed point ({@link
 * Sortie#CLOCK_ONE}), so everything keyed to it (waves, ground targets, radio times, edge warnings, the level end)
 * waits with the scroll, while units, bullets, spawn cycles, pounces and the wingman run on the real steps. Outside a
 * hold the clock advances exactly one step per step, so a level without holds steps and hashes as before; a hold over
 * a collapse's groups stays active until its dust has settled.
 *
 * <p>M5 part G (design/campaign Level 13, user decisions G1–G6 = a): a <em>timed halt</em> is a section of speed 0
 * without an arena: the level clock runs on one step per step while the scroll eases into it and out of it ({@link
 * Sortie#timedHaltSpeed}).
 */
final class LevelClock {
    private final Sortie s;
    /** M7 part H: whether the arena crawls ({@link Section#crawls()}): its ground scrolls on while the clock is held. */
    final boolean crawling;
    /**
     * Step SIM-K (round 44, K.c): a crawling arena's ground stands still while the breaching boss is out of the sand:
     * how far its crawl is eased in, steps (0 still, {@link #crawlEaseTicks} at full speed; smoothstep between).
     */
    int crawlLevel;

    private final int crawlEaseTicks;
    /** M5 part E: the arena boss (anchored, with lanes); null without one. */
    final SetPiece arenaBoss;
    /** M7 part H: the breaching boss (Level 21's Dust Colossus); null without one. */
    final SetPiece breachBoss;
    /** M5 part E: the speed the scroll eases into an arena of speed 0 from (the section before's); 0 without one. */
    final double arenaFromSpeed;
    /** The arena section's end and start, steps; -1 without an arena. */
    final int arenaEndTicks;

    final int arenaStartTicks;
    final double arenaSpeed;
    /** How far the level clock advanced in the last step, in 1/{@link Sortie#CLOCK_ONE} steps. */
    int clockStep = Sortie.CLOCK_ONE;
    /** The scroll ramps from this speed ... */
    double rampFrom;
    /** ... for this many steps so far; -1 without a ramp. */
    int rampTicks = -1;
    /**
     * M7 part F: the ramp's length out of the arena, steps: {@link Sortie#ARENA_RAMP_SECONDS}, or the next section's
     * ease (smoothstep) when it has one ({@link #easeOutOfArena}).
     */
    private final int rampLength;
    /** M7 part F: whether the section after the arena eases up from the halt (smoothstep over its ease). */
    private final boolean easeOutOfArena;
    /** M7 part F: the arena section's hold after its boss, s; 0 without one (or without an arena). */
    final double holdAfterBoss;
    /**
     * M7 part F: steps the level clock stays halted after an arena boss's death (its death chain, then the arena's
     * hold after the boss); 0 for none. Hashed only in a level whose arena holds.
     */
    int postBossHold;
    /** M5 part G: whether the level has a timed halt: its scroll then follows {@link Sortie#timedHaltSpeed}. */
    final boolean timedHalts;
    /** M7 part B: whether a section eases in from the one before (Level 15's speed changes). */
    private final boolean eased;
    /**
     * M7 part G (design/campaign Level 20, Reverse scroll): the direction of travel, +1 or -1: the scroll's sign, the
     * last non-zero one through the turnabout's zero. Follows the scroll (not hashed: the scroll decides it).
     */
    int travel = 1;

    /** Finds the level's arena and its bosses; an anchored boss is anchored at the halt's scroll. */
    LevelClock(Sortie sortie, LevelScript script, SetPiece[] setPieces) {
        this.s = sortie;
        int arenaEnd = -1;
        int arenaStart = -1;
        double arenaSpeedFound = 0;
        boolean crawls = false;
        for (int i = 0; i < script.sections().size(); i++) {
            if (script.sections().get(i).arena()) {
                crawls = script.sections().get(i).crawls();
                arenaStart =
                        i == 0 ? 0 : SimStep.ticks(script.sections().get(i - 1).end());
                arenaEnd = SimStep.ticks(script.sections().get(i).end());
                arenaSpeedFound = script.sections().get(i).speed();
            }
        }
        arenaEndTicks = arenaEnd;
        arenaStartTicks = arenaStart;
        arenaSpeed = arenaSpeedFound;
        crawling = crawls;
        crawlEaseTicks = Math.max(1, SimStep.ticks(Sortie.CRAWL_EASE_SECONDS));
        crawlLevel = crawlEaseTicks;
        double hold = 0;
        double easeOut = 0;
        for (int i = 0; i < script.sections().size(); i++) {
            if (script.sections().get(i).arena()) {
                hold = script.sections().get(i).holdAfterBossSeconds();
                easeOut = i + 1 < script.sections().size()
                        ? script.sections().get(i + 1).easeSeconds()
                        : 0;
            }
        }
        holdAfterBoss = hold;
        easeOutOfArena = easeOut > 0;
        rampLength = easeOutOfArena ? Math.max(1, SimStep.ticks(easeOut)) : SimStep.ticks(Sortie.ARENA_RAMP_SECONDS);
        SetPiece anchored = null;
        for (SetPiece piece : setPieces) {
            if (anchored == null && piece.arena().isPresent()) {
                anchored = piece;
            }
        }
        arenaBoss = anchored;
        arenaFromSpeed = anchored == null ? 0 : ScrollPlan.arenaFromSpeed(script);
        SetPiece breaching = null;
        for (SetPiece piece : setPieces) {
            if (breaching == null && piece.breach().isPresent()) {
                breaching = piece;
            }
        }
        breachBoss = breaching;
        if (anchored != null
                && (anchored.breach().isPresent() || anchored.coil().isPresent())) {
            // M7 part H: a breaching boss needs an arena (it may crawl); it is not anchored to the ground. M8 part H:
            // nor
            // a coiled one (its coil lies round the screen's edges).
            if (arenaStartTicks < 0) {
                throw new IllegalArgumentException("a breaching or coiled boss needs an arena");
            }
        } else if (anchored != null) {
            if (arenaStartTicks < 0 || arenaSpeed != 0) {
                throw new IllegalArgumentException("an anchored boss needs an arena of speed 0");
            }
            anchored.anchorAt(ScrollPlan.haltScroll(script, arenaFromSpeed)
                    + anchored.boss().orElseThrow().hoverY());
        }
        if (crawling && (anchored == null || anchored.breach().isEmpty())) {
            throw new IllegalArgumentException("only a breaching boss's arena crawls");
        }
        timedHalts = script.timedHalts();
        eased = script.sections().stream().anyMatch(section -> section.easeSeconds() > 0);
    }

    /**
     * The level clock's and the scroll's step: an early boss death's jump to the arena's end, the hold zones' ease,
     * the arena's halt (or a crawling arena's crawl), the ramp out of it, the travel's sign and the clock's advance;
     * the ground scrolls on. Returns how far it scrolled in this step, px.
     */
    double step() {
        double scrollStep;
        if (s.arenaJump) {
            // The boss died before the arena's end: the next section starts now.
            s.arenaJump = false;
            if (!crawling) {
                s.groundScroll += (s.arenaEndTicks - 1 - s.levelTick) * arenaSpeed * SimStep.SECONDS;
            }
            // M7 part H: a crawling arena's jump moves the clock only (the ground crawls on unbroken).
            s.levelTick = s.arenaEndTicks - 1;
            s.tickFraction = 0;
            rampFrom = arenaSpeed;
            rampTicks = 0;
        }
        if (s.holdZones.holds()) {
            s.holdZones.ease();
        }
        boolean held = clockHeld();
        if (postBossHold > 0) {
            postBossHold--;
        }
        if (held && crawling) {
            // M7 part H: the clock is held, the ground crawls on at the arena's speed (and ramps on from it).
            scrollStep = arenaSpeed * SimStep.SECONDS;
            rampFrom = arenaSpeed;
            rampTicks = 0;
            clockStep = 0;
        } else if (held) {
            scrollStep = 0;
            rampFrom = 0;
            rampTicks = 0;
            clockStep = 0;
        } else {
            double speed = scrollSpeed();
            scrollStep = speed * SimStep.SECONDS;
            if (speed > 0) {
                travel = 1;
            } else if (speed < 0) {
                // M7 part G: the scroll runs reversed (it keeps its sign through a halt).
                travel = -1;
            }
            advanceClock(speed);
            if (rampTicks >= 0 && ++rampTicks >= rampLength) {
                rampTicks = -1;
            }
        }
        if (crawling) {
            // Step SIM-K: the crawl eases to a stop while the breaching boss is out of the sand, and back.
            crawlLevel = Math.clamp(crawlLevel + (groundStill() ? -1 : 1), 0, crawlEaseTicks);
            scrollStep *= crawlFactor();
        }
        s.groundScroll += scrollStep;
        return scrollStep;
    }

    /**
     * Advances the level clock for a step at {@code speed} px/s: one whole step at the section's
     * speed, the share of one at a hold's eased speed (the clock never runs faster than real time).
     */
    private void advanceClock(double speed) {
        if (s.holdZones.easeTicks == 0) {
            clockStep = Sortie.CLOCK_ONE;
        } else {
            double share = speed / sectionSpeed();
            clockStep = Math.clamp(Math.round(share * Sortie.CLOCK_ONE), 1, Sortie.CLOCK_ONE);
        }
        s.tickFraction += clockStep;
        int whole = s.tickFraction >> 16;
        s.tickFraction &= Sortie.CLOCK_ONE - 1;
        s.levelTick += whole;
        s.clockTicked = whole > 0;
    }

    /**
     * Whether the level clock halts: at the end of the arena section while the boss that fights
     * there lives (the scroll stops until it dies).
     */
    boolean clockHeld() {
        if (postBossHold > 0) {
            // M7 part F: the arena's hold after its boss's death chain.
            return true;
        }
        if (s.arenaEndTicks < 0 || s.levelTick + 1 < s.arenaEndTicks || s.levelTick >= s.arenaEndTicks) {
            return false;
        }
        for (SetPiece piece : s.setPieces) {
            if (piece.boss().isPresent() && piece.present() && !piece.destroyed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * M5 part E: whether the scroll has come to a halt in an arena of speed 0: its ease is over (or the
     * level clock is halted at the arena's end).
     */
    boolean scrollHalted() {
        return clockHeld()
                || (s.arenaStartTicks >= 0
                        && s.levelTick < s.arenaEndTicks
                        && s.levelTick - s.arenaStartTicks >= SimStep.ticks(Sortie.ARENA_RAMP_SECONDS) - 1);
    }

    /**
     * Step SIM-K: whether the breaching boss wants a crawling arena's ground still now or within the crawl's ease
     * ({@link BreachArena#groundStill(int)}).
     */
    private boolean groundStill() {
        return breachBoss != null && breachBoss.breach().orElseThrow().groundStill(crawlEaseTicks);
    }

    /** Step SIM-K: the share of its speed a crawling arena's ground scrolls at now, 0–1 (1 in any other level). */
    double crawlFactor() {
        return crawling ? ScrollPlan.smoothstep((double) crawlLevel / crawlEaseTicks) : 1;
    }

    /** The scroll's speed now, px/s (a held clock aside). */
    double scrollSpeed() {
        if (timedHalts) {
            // M5 part G: a level with a timed halt has no holds or arena (LevelScript checks it).
            return Sortie.timedHaltSpeed(s.script.sections(), s.levelTick);
        }
        // M7 part B: a section that eases in from the one before (Level 15's speed changes).
        double speed = eased ? Sortie.timedHaltSpeed(s.script.sections(), s.levelTick) : sectionSpeed();
        if (arenaBoss != null
                && !crawling
                && rampTicks < 0
                && s.levelTick >= s.arenaStartTicks
                && s.levelTick < s.arenaEndTicks) {
            // M5 part E: into an arena of speed 0 the scroll eases to a halt over the ramp.
            int n = SimStep.ticks(Sortie.ARENA_RAMP_SECONDS);
            speed = arenaFromSpeed * Math.max(0, n - 1 - (s.levelTick - s.arenaStartTicks)) / n;
        }
        if (rampTicks >= 0 && easeOutOfArena) {
            // M7 part F: out of the arena the next section eases up over its ease (smoothstep), as a moving one does.
            speed = rampFrom + (speed - rampFrom) * ScrollPlan.smoothstep((rampTicks + 1.0) / rampLength);
        } else if (rampTicks >= 0) {
            // Out of the arena the scroll ramps up to the section's speed.
            speed = rampFrom + (speed - rampFrom) * rampTicks / SimStep.ticks(Sortie.ARENA_RAMP_SECONDS);
        }
        // M5 part C: over a hold zone it eases (smoothstep) to the hold's speed and back.
        return s.holdZones.easedSpeed(speed);
    }

    /** The speed of the section the level clock is in, px/s. */
    private double sectionSpeed() {
        return s.script.sections().get(s.section() - 1).speed();
    }

    /**
     * M7 part F: arena boss {@code boss} died at the level clock's tick now: in the arena (or at its halted end), an
     * arena with a hold after its boss keeps the clock halted for the boss's death chain and then the hold.
     */
    void bossDownInArena(BossSpec boss) {
        if (holdAfterBoss > 0
                && s.arenaEndTicks >= 0
                && s.levelTick >= s.arenaStartTicks
                && s.levelTick < s.arenaEndTicks) {
            postBossHold = SimStep.ticks(boss.deathSeconds() + holdAfterBoss);
        }
    }

    /** The arena's state back to an attempt's start (a restart, the boss checkpoint). */
    void reset() {
        crawlLevel = crawlEaseTicks;
        postBossHold = 0;
        rampFrom = 0;
        rampTicks = -1;
    }

    /** See {@link Sortie#groundSpeed()}. */
    double groundSpeed() {
        if (!crawling) {
            return scrollSpeed();
        }
        return (clockHeld() ? arenaSpeed : scrollSpeed()) * crawlFactor();
    }
}
