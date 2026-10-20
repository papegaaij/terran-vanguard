package vanguard.sim;

import java.util.Arrays;
import java.util.List;

/**
 * M5 part C (design/campaign Level 09, user decisions D2, D3 and D5 = a): a {@link Sortie}'s hold zones
 * and collapse. A hold starts when the first unit of its groups reaches its depth, eases the scroll to its
 * speed ({@link #easedSpeed}) while it runs and ends once its groups are decided; the collapse leans,
 * drops and blasts the ground units in its band once its groups are cleared, a hold over its groups
 * staying active until its dust has settled.
 */
final class HoldZones {
    /** A hold's states: not yet started, running, and over (or never needed: its groups were gone first). */
    static final int HOLD_WAITING = 0;

    static final int HOLD_RUNNING = 1;
    static final int HOLD_OVER = 2;

    private final Sortie s;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Objectives objectives;
    /** What the collapse's blast destroys is handed here, as an Airstrike's kills. */
    private final PlayerFire.Hits hits;
    /** M5 part C: the hold zones, their ease in steps and their state ({@link #HOLD_WAITING} ...). */
    final ObjectiveSpec.Hold[] holds;

    private final int[] holdRampTicks;
    final int[] holdState;
    /** Per group, the hold that waits for it; -1 for none. */
    private final int[] holdOfGroup;
    /** The hold running now (started, its groups not all gone); -1 for none. */
    int activeHold = -1;
    /** The hold whose speed the scroll eases to or back from; -1 while the scroll runs at the section's speed. */
    int easingHold = -1;
    /** Steps into the ease, 0 (section speed) to the easing hold's ramp (its speed). */
    int easeTicks;
    /** Whether a hold started in this attempt (the radio's {@code hold-start} is the first one's). */
    boolean holdStarted;
    /** M5 part C: the collapse; null without one. */
    final ObjectiveSpec.Collapse collapse;

    /**
     * The collapse's warning (the lean) in steps; the impact, the blast's end and the dust's settling,
     * steps from its start.
     */
    private final int collapseWarningTicks;

    private final int collapseImpactTicks;
    private final int collapseEndTicks;
    private final int collapseSettledTicks;
    /**
     * Per hold, whether its groups include one of the collapse's: such a hold stays active through
     * the collapse's warning, drop and blast until its dust has settled (user decision, 2026-10-07),
     * so the ground (and the band and the heap on it) barely moves while it falls and settles.
     */
    private final boolean[] holdThroughCollapse;
    /** Real steps since the collapse's warning started; -1 before. */
    int collapseTicks = -1;

    HoldZones(
            Sortie s,
            LevelScript script,
            Ship ship,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Objectives objectives,
            PlayerFire.Hits hits) {
        this.s = s;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.objectives = objectives;
        this.hits = hits;
        holds = script.objectives().holds().toArray(ObjectiveSpec.Hold[]::new);
        holdRampTicks = new int[holds.length];
        holdState = new int[holds.length];
        holdOfGroup = new int[script.groups().size()];
        Arrays.fill(holdOfGroup, -1);
        for (int h = 0; h < holds.length; h++) {
            holdRampTicks[h] = Math.max(1, SimStep.ticks(holds[h].rampSeconds()));
            for (int group : holds[h].groups()) {
                holdOfGroup[group] = h;
            }
        }
        collapse = script.objectives().collapse().orElse(null);
        collapseWarningTicks = collapse == null ? 0 : SimStep.ticks(collapse.warningSeconds());
        collapseImpactTicks =
                collapse == null ? 0 : collapseWarningTicks + Math.max(1, SimStep.ticks(collapse.dropSeconds()));
        collapseEndTicks =
                collapse == null ? 0 : collapseImpactTicks + Math.max(1, SimStep.ticks(collapse.blastSeconds()));
        collapseSettledTicks = collapse == null
                ? 0
                : Math.max(collapseEndTicks, collapseImpactTicks + SimStep.ticks(collapse.settleSeconds()));
        holdThroughCollapse = new boolean[holds.length];
        if (collapse != null) {
            for (int group : collapse.groups()) {
                if (holdOfGroup[group] >= 0) {
                    holdThroughCollapse[holdOfGroup[group]] = true;
                }
            }
        }
    }

    /** Whether the level has hold zones. */
    boolean holds() {
        return holds.length > 0;
    }

    /** Whether the level has hold zones or a collapse (their state is hashed only then). */
    boolean present() {
        return holds.length > 0 || collapse != null;
    }

    /** A new attempt: every hold waiting, no ease, the collapse not started. */
    void reset() {
        Arrays.fill(holdState, HOLD_WAITING);
        activeHold = -1;
        easingHold = -1;
        easeTicks = 0;
        holdStarted = false;
        collapseTicks = -1;
    }

    /** The holds' and the collapse's state, after the sortie's clock (only when {@link #present()}). */
    void addTo(StateHash hash) {
        hash.add(activeHold)
                .add(easingHold)
                .add(easeTicks)
                .add(holdStarted ? 1 : 0)
                .add(collapseTicks);
        for (int state : holdState) {
            hash.add(state);
        }
    }

    /** {@code speed} eased (smoothstep) toward the easing hold's speed over a hold zone, and back. */
    double easedSpeed(double speed) {
        if (easeTicks > 0) {
            double share = (double) easeTicks / holdRampTicks[easingHold];
            speed += (holds[easingHold].speed() - speed) * share * share * (3 - 2 * share);
        }
        return speed;
    }

    /** How far the scroll has eased toward a hold's speed, 0 (section speed) to 1 (the hold's). */
    double holdEase() {
        return easeTicks == 0 ? 0 : (double) easeTicks / holdRampTicks[easingHold];
    }

    /** The ease toward the running hold's speed, or back to the section's once no hold runs. */
    void ease() {
        if (activeHold >= 0) {
            easingHold = activeHold;
            easeTicks = Math.min(easeTicks + 1, holdRampTicks[activeHold]);
        } else if (easeTicks > 0) {
            if (--easeTicks == 0) {
                easingHold = -1;
            }
        }
    }

    /**
     * The hold zones after the step's hits: the running one ends once every unit of its groups is
     * gone (destroyed, or away: a failed destroy-targets primary), or, for a hold over the collapse's
     * groups whose clearing started the collapse, once its dust has settled; a waiting one starts when the
     * first unit of its groups reaches its depth below the top edge (one hold runs at a time).
     */
    void updateHolds() {
        if (activeHold >= 0 && groupsDecided(holds[activeHold].groups(), false) && !collapseHolds(activeHold)) {
            holdState[activeHold] = HOLD_OVER;
            events.add(SimEvents.Type.HOLD_END, ship.x(), ship.y(), activeHold);
            activeHold = -1;
        }
        for (int h = 0; h < holds.length; h++) {
            if (holdState[h] == HOLD_WAITING && groupsDecided(holds[h].groups(), false)) {
                holdState[h] = HOLD_OVER;
            }
        }
        if (activeHold >= 0) {
            return;
        }
        Pool<Enemy> enemies = force.enemies();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            int group = enemy.group();
            int h = group >= 0 ? holdOfGroup[group] : -1;
            if (h >= 0 && holdState[h] == HOLD_WAITING && enemy.y() <= PlayField.HEIGHT - holds[h].depth()) {
                holdState[h] = HOLD_RUNNING;
                activeHold = h;
                events.add(SimEvents.Type.HOLD_START, ship.x(), ship.y(), h);
                if (!holdStarted) {
                    holdStarted = true;
                    radio.cue(CueTrigger.HOLD_START, "");
                }
                return;
            }
        }
    }

    /** Whether hold {@code h} waits for the collapse: over its groups, the collapse started and its dust not yet settled. */
    private boolean collapseHolds(int h) {
        return holdThroughCollapse[h] && collapseTicks >= 0 && collapseTicks < collapseSettledTicks;
    }

    /** Whether every one of {@code groups} is decided (with {@code cleared}: cleared, none of its units got away). */
    private boolean groupsDecided(List<Integer> groups, boolean cleared) {
        for (int i = 0; i < groups.size(); i++) {
            int state = objectives.groupState(groups.get(i));
            if (state == Objectives.OPEN || (cleared && state != Objectives.CLEARED)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The collapse (user decision D5 = a, round 31's look c): once its groups are cleared its tower
     * leans (the warning and the radio's {@code collapse}), then drops; from the impact its blast's
     * ring grows round the tower's foot, destroying every ground unit (on the ground layer now) in the
     * band whose centre it reaches, paid and scored as an Airstrike kill; then it is over, its count
     * of real steps running on for the dust's ramp out ({@link #collapseSeconds()}).
     */
    void updateCollapse() {
        if (collapseTicks < 0) {
            if (!s.complete && groupsDecided(collapse.groups(), true)) {
                collapseTicks = 0;
                events.add(SimEvents.Type.COLLAPSE_WARNING, ship.x(), ship.y());
                radio.cue(CueTrigger.COLLAPSE, "");
            }
            return;
        }
        collapseTicks++;
        if (collapseTicks > collapseEndTicks) {
            // Over: the count runs on as real time since the warning started (the dust's ramp out).
            return;
        }
        if (collapseTicks == collapseWarningTicks) {
            events.add(SimEvents.Type.COLLAPSE_FALL, ship.x(), ship.y());
        }
        if (collapseTicks == collapseImpactTicks) {
            events.add(SimEvents.Type.COLLAPSE_IMPACT, ship.x(), ship.y());
        }
        if (collapseTicks >= collapseImpactTicks) {
            double radius = collapseBlastRadius();
            double reach = radius * radius;
            double x = collapse.x();
            double y = collapseFootY();
            double top = collapseBandTop();
            double bottom = collapseBandBottom();
            Pool<Enemy> enemies = force.enemies();
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                double dx = enemy.x() - x;
                double dy = enemy.y() - y;
                if (enemy.layer() == Layer.GROUND
                        && !Chain.doomed(enemy)
                        && enemy.y() <= top
                        && enemy.y() >= bottom
                        && dx * dx + dy * dy <= reach) {
                    hits.enemyDestroyed(j);
                }
            }
        }
        if (collapseTicks == collapseEndTicks) {
            events.add(SimEvents.Type.COLLAPSE_END, ship.x(), ship.y());
        }
    }

    /** See {@link Sortie#collapseStarted()}. */
    boolean collapseStarted() {
        return collapseTicks >= 0;
    }

    /** See {@link Sortie#collapseSeconds()}. */
    double collapseSeconds() {
        return collapseTicks < 0 ? -1 : collapseTicks * SimStep.SECONDS;
    }

    /** See {@link Sortie#collapseWarning()}. */
    boolean collapseWarning() {
        return collapseTicks >= 0 && collapseTicks < collapseWarningTicks;
    }

    /** See {@link Sortie#collapseImpacted()}. */
    boolean collapseImpacted() {
        return collapseTicks >= collapseImpactTicks && collapseTicks >= 0;
    }

    /** See {@link Sortie#collapseBlastRadius()}. */
    double collapseBlastRadius() {
        if (collapse == null || !collapseImpacted()) {
            return 0;
        }
        return collapse.blastRadius((collapseTicks - collapseImpactTicks) * SimStep.SECONDS);
    }

    /** See {@link Sortie#collapseFootX()}. */
    double collapseFootX() {
        return collapse == null ? 0 : collapse.x();
    }

    /** See {@link Sortie#collapseFootY()}. */
    double collapseFootY() {
        return collapse == null ? 0 : (collapse.top() + collapse.bottom()) / 2 - s.groundScroll;
    }

    /** See {@link Sortie#collapseBandTop()}. */
    double collapseBandTop() {
        return collapse == null ? 0 : collapse.top() - s.groundScroll;
    }

    /** See {@link Sortie#collapseBandBottom()}. */
    double collapseBandBottom() {
        return collapse == null ? 0 : collapse.bottom() - s.groundScroll;
    }
}
