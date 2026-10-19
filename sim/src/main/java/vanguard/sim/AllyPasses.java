package vanguard.sim;

import java.util.List;

/**
 * The convoy's passes for a {@link Sortie} (design/allies): what hurts an escort's, an air escort's, a
 * defend primary's, a naval convoy's and the ground allies' units, the scripted loss, the defend's end,
 * and the lines, events and objective outcomes that follow. Made in every sortie; its passes run only
 * with a {@link Convoy} (null without one).
 */
final class AllyPasses {
    private final Sortie s;
    private final Convoy convoy;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Objectives objectives;
    private final Rules rules;
    /** M5 part G: the shares the {@code ally-below} cues name, the cues' subjects, and which fired in this attempt. */
    private final double[] belowShares;

    private final String[] belowSubjects;
    private final boolean[] belowFired;

    AllyPasses(
            Sortie s,
            LevelScript script,
            Convoy convoy,
            Ship ship,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Objectives objectives,
            Rules rules) {
        this.s = s;
        this.convoy = convoy;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.objectives = objectives;
        this.rules = rules;
        List<String> subjects = new java.util.ArrayList<>();
        for (LevelScript.RadioCue cue : script.radio()) {
            if (cue.trigger() == LevelScript.CueTrigger.ALLY_BELOW && !subjects.contains(cue.subject())) {
                subjects.add(cue.subject());
            }
        }
        belowSubjects = subjects.toArray(String[]::new);
        belowShares = new double[belowSubjects.length];
        for (int i = 0; i < belowShares.length; i++) {
            belowShares[i] = LevelScript.CueTrigger.share(belowSubjects[i]);
        }
        belowFired = new boolean[belowShares.length];
    }

    /** A new attempt: no {@code ally-below} cue fired. */
    void reset() {
        java.util.Arrays.fill(belowFired, false);
    }

    /** M5 part G: only with ally-below cues (a defend primary), so the other levels hash as before. */
    void addTo(StateHash hash) {
        for (boolean fired : belowFired) {
            hash.add(fired ? 1 : 0);
        }
    }

    /**
     * The convoy takes its damage (design/allies, civilian crawler): an enemy shot the
     * target-the-objective hook aimed at a unit hits the first unit it touches and is spent (shots
     * aimed at the ship pass over), and a walker (a ground unit that is not fixed to the ground)
     * claws every unit its hit box overlaps, per second.
     */
    void hitAllies() {
        AllySpec spec = convoy.spec(0);
        if (convoy.air()) {
            hitAirAllies(spec);
            return;
        }
        if (spec.objectiveAimed()) {
            Pool<EnemyBullet> bullets = force.bullets();
            for (int i = bullets.size() - 1; i >= 0 && !s.failed; i--) {
                EnemyBullet bullet = bullets.get(i);
                if (!bullet.objectiveAimed()) {
                    continue;
                }
                int k = convoy.touching(bullet.x(), bullet.y(), EnemyGun.BULLET);
                if (k >= 0) {
                    bullets.free(i);
                    hurt(k, bullet.damage(), true);
                }
            }
        }
        if (spec.clawsPerSecond() > 0) {
            double claws = spec.clawsPerSecond() * SimStep.SECONDS;
            Pool<Enemy> enemies = force.enemies();
            for (int j = 0; j < enemies.size() && !s.failed; j++) {
                Enemy enemy = enemies.get(j);
                // M7 part B: a Burrower is up on the screen, not on the road: it claws nothing; M7 part E: nor a
                // vortex.
                if (enemy.layer() != Layer.GROUND || enemy.grounded() || enemy.burrowing() || enemy.vortex()) {
                    continue;
                }
                for (int k = 0; k < convoy.size() && !s.failed; k++) {
                    if (convoy.touches(k, enemy.x(), enemy.y(), enemy.hitbox())) {
                        // A new contact flashes and sounds; a continuing one only takes its damage.
                        hurt(k, claws, convoy.get(k).ticksSinceHit() > 1);
                    }
                }
            }
        }
        if (convoy.defend()) {
            hitDefended(spec);
        }
        if (convoy.groundAllies()) {
            hitGroundAllies();
        }
    }

    /**
     * M7 part D (design/allies, Ares Landing dome): each standing ground ally takes the hits the hooked
     * attacks dealt it this step (a bomb run's load, a lob's direct hit, D3's rolls), in the tracker's order.
     */
    private void hitGroundAllies() {
        for (int k = 0; k < convoy.size(); k++) {
            double hits = force.allyHits(k);
            if (hits > 0 && convoy.get(k).alive()) {
                hurtGround(k, hits);
            }
        }
    }

    /**
     * M7 part D: ground ally {@code k} takes {@code hits}: its hit event, the attempt's first hit's line and
     * each unit's first hit's {@code ally-hit} line ({@link Sortie#lastAllyHit()}), an untouched secondary failed at
     * the first hit; when it breaks, the loss's event and lines. It never fails the mission.
     */
    private void hurtGround(int k, double hits) {
        Ally ally = convoy.get(k);
        boolean firstOfUnit = ally.ticksSinceHit() == Integer.MAX_VALUE;
        boolean first = convoy.firstHit() < 0;
        boolean broken = convoy.damage(k, hits);
        events.add(SimEvents.Type.ALLY_HIT, ally.x(), ally.y(), k);
        if (first) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_HIT, "");
        }
        if (firstOfUnit) {
            radio.cue(LevelScript.CueTrigger.ALLY_HIT, "");
        }
        if (objectives.allyHit()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, ally.x(), ally.y());
        }
        if (!broken) {
            return;
        }
        events.add(SimEvents.Type.ALLY_LOST, ally.x(), ally.y(), k);
        if (convoy.firstLost() == k && convoy.lostSaveable() == 1) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_LOST, "");
        }
        radio.cue(LevelScript.CueTrigger.ALLY_LOST, "");
    }

    /**
     * M5 part G (design/allies, Nansen Relay; user decision G1 = a): the defend primary's ally takes a
     * diver's dive the always hook sent at it, once as its body crosses the ally's hit box ({@link
     * AllySpec#dives()}), and the direct hits of the lobs aimed at it that landed on it this step.
     */
    private void hitDefended(AllySpec spec) {
        if (spec.dives() > 0) {
            Pool<Enemy> enemies = force.enemies();
            for (int j = 0; j < enemies.size() && !s.failed; j++) {
                Enemy enemy = enemies.get(j);
                if (enemy.divedAtObjective()
                        && !enemy.diveHit()
                        && convoy.touches(0, enemy.x(), enemy.y(), enemy.hitbox())) {
                    enemy.markDiveHit();
                    hurt(0, spec.dives(), true);
                }
            }
        }
        if (spec.lobs()
                && force.objectiveLobDamage() > 0
                && !s.failed
                && convoy.get(0).alive()) {
            hurt(0, force.objectiveLobDamage(), true);
        }
    }

    /**
     * M5 part G: the defend primary's synchronisation completes at its end with the ally standing: its
     * event, an integrity secondary met (its credits and line, after the timed lines of this step); from
     * now on nothing aims at the ally or hurts it.
     */
    void defendStep() {
        if (convoy.defended() || s.levelTick < s.defendEndTicks) {
            return;
        }
        convoy.defended(true);
        Ally ally = convoy.get(0);
        events.add(SimEvents.Type.DEFEND_COMPLETE, ally.x(), ally.y(), (int) Math.round(ally.hpShare() * 100));
        if (objectives.defended()) {
            s.objectivePasses.paySecondary();
        }
    }

    /**
     * M5 part G: after a hit on the defend primary's ally, the shares its {@code ally-below} cues name that
     * it dropped below for the first time (their event and cues), and an integrity secondary failed by it.
     */
    private void belowShares(Ally ally) {
        double share = ally.hpShare();
        for (int i = 0; i < belowShares.length; i++) {
            if (!belowFired[i] && share < belowShares[i]) {
                belowFired[i] = true;
                events.add(SimEvents.Type.ALLY_BELOW, ally.x(), ally.y(), (int) Math.round(belowShares[i] * 100));
                radio.cue(LevelScript.CueTrigger.ALLY_BELOW, belowSubjects[i]);
            }
        }
        if (objectives.integrityLeft(share)) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, ally.x(), ally.y());
        }
    }

    /**
     * M5 part D (design/allies, evacuation shuttle; user decision D2 = a): an air escort's units take
     * their damage. Every enemy bullet touching a unit that can be hit hurts the first such unit by its
     * damage and is spent; an enemy body on the player's plane hurts each unit it starts to overlap by
     * its contact damage, once per contact, and one that is destroyed by ramming the ship is destroyed
     * by the impact and paid as then. Units in their untouchable windows let fire and contact pass.
     */
    private void hitAirAllies(AllySpec spec) {
        if (spec.bullets()) {
            Pool<EnemyBullet> bullets = force.bullets();
            for (int i = bullets.size() - 1; i >= 0 && !s.failed; i--) {
                EnemyBullet bullet = bullets.get(i);
                int k = convoy.touchingInAir(bullet.x(), bullet.y(), EnemyGun.BULLET);
                if (k >= 0) {
                    bullets.free(i);
                    hurt(k, bullet.damage(), true);
                }
            }
        }
        if (!spec.contact()) {
            return;
        }
        Pool<Enemy> enemies = force.enemies();
        for (int j = enemies.size() - 1; j >= 0 && !s.failed; j--) {
            Enemy enemy = enemies.get(j);
            int touching = 0;
            if (enemy.layer().collidesWithPlayer() && !Chain.doomed(enemy) && !enemy.leaping()) {
                for (int k = 0; k < convoy.size(); k++) {
                    if (convoy.touchesInAir(k, enemy.x(), enemy.y(), enemy.hitbox())) {
                        touching |= 1 << k;
                    }
                }
            }
            int fresh = touching & ~enemy.allyContacts();
            enemy.allyContacts(touching);
            EnemySpec kind = enemy.spec();
            for (int k = 0; k < convoy.size() && fresh != 0 && !s.failed; k++) {
                if ((fresh & 1 << k) == 0) {
                    continue;
                }
                hurt(k, kind.contactDamage(), true);
                if (kind.destroyedByRamming()) {
                    s.objectivePasses.destroy(j);
                    break;
                }
            }
        }
    }

    /**
     * M5 part D (design/campaign Level 10, user decision D4 = a): the scripted loss's glow starts at its
     * time and its lance takes its unit at its own: the unit is lost without the loss cues, the pay
     * or the fail; the radio's {@code scripted-loss} cues start.
     */
    void scriptedLoss() {
        int k = convoy.scripted();
        if (k < 0) {
            return;
        }
        Ally ally = convoy.get(k);
        if (convoy.glowDue(s.levelTick)) {
            events.add(SimEvents.Type.LOSS_GLOW, ally.x(), ally.y(), k);
        }
        if (convoy.lossDue(s.levelTick)) {
            events.add(SimEvents.Type.SCRIPTED_LOSS, ally.x(), ally.y(), k);
            radio.cue(LevelScript.CueTrigger.SCRIPTED_LOSS, "");
        }
    }

    /**
     * Unit {@code k} of the convoy takes {@code damage}: the first hit's line, a loss's lines (M5 part
     * D: {@code first-ally-lost}'s on the first, then {@code ally-lost}'s on every one), the failure
     * when every saveable unit is lost.
     */
    private void hurt(int k, double damage, boolean announce) {
        if (convoy.groundAllies()) {
            // M7 part D: ground allies never fail the mission.
            hurtGround(k, damage);
            return;
        }
        Ally ally = convoy.get(k);
        boolean first = convoy.firstHit() < 0;
        boolean destroyed = convoy.damage(k, damage);
        if (announce || destroyed) {
            events.add(SimEvents.Type.ALLY_HIT, ally.x(), ally.y(), k);
        }
        if (first) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_HIT, "");
        }
        if (convoy.defend()) {
            belowShares(ally);
        }
        if (!destroyed) {
            return;
        }
        events.add(SimEvents.Type.ALLY_LOST, ally.x(), ally.y(), k);
        if (convoy.firstLost() == k && convoy.lostSaveable() == 1) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_LOST, "");
        }
        radio.cue(LevelScript.CueTrigger.ALLY_LOST, "");
        if (convoy.allLost()) {
            // M5 part G: a defend primary's too.
            if ((convoy.air() || convoy.defend()) && rules.invulnerableShip()) {
                // The debug option that lets a capture see the level to its end keeps it going too.
                return;
            }
            s.failed = true;
            events.add(SimEvents.Type.PRIMARY_FAILED, ship.x(), ship.y());
            radio.cue(LevelScript.CueTrigger.MISSION_FAILED, "");
        }
    }

    /**
     * M5 part E (design/allies, convoy cargo ship): a boss slam in lane {@code lane} (1-based) hits
     * every naval convoy unit afloat in it that slams hurt, once each (for E2c's slam cycle). Returns
     * the units hit; nothing without a naval convoy.
     */
    int slamAllies(int lane) {
        if (convoy == null || !convoy.naval() || s.complete) {
            return 0;
        }
        int hit = 0;
        for (int k = 0; k < convoy.size(); k++) {
            if (convoy.slammable(k) && convoy.lane(k) == lane) {
                hurtNaval(k, convoy.spec(k).slams());
                hit++;
            }
        }
        return hit;
    }

    /**
     * M5 part E: naval convoy unit {@code k} takes {@code damage}: its hit event, its first hit's
     * {@code ally-hit} line (each unit's, {@link Sortie#lastAllyHit()}) and the attempt's first hit's; when
     * it sinks, the loss's event and lines and the afloat objective's failure. It never fails the
     * mission, with {@code --invulnerable} or not.
     */
    private void hurtNaval(int k, double damage) {
        Ally ally = convoy.get(k);
        boolean firstOfUnit = ally.ticksSinceHit() == Integer.MAX_VALUE;
        boolean first = convoy.firstHit() < 0;
        boolean sunk = convoy.damage(k, damage);
        events.add(SimEvents.Type.ALLY_HIT, ally.x(), ally.y(), k);
        if (first) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_HIT, "");
        }
        if (firstOfUnit) {
            radio.cue(LevelScript.CueTrigger.ALLY_HIT, "");
        }
        if (!sunk) {
            return;
        }
        events.add(SimEvents.Type.ALLY_LOST, ally.x(), ally.y(), k);
        if (convoy.firstLost() == k && convoy.lostSaveable() == 1) {
            radio.cue(LevelScript.CueTrigger.FIRST_ALLY_LOST, "");
        }
        radio.cue(LevelScript.CueTrigger.ALLY_LOST, "");
        if (objectives.sunk()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, ally.x(), ally.y());
        }
    }
}
