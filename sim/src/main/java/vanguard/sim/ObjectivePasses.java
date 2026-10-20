package vanguard.sim;

import java.util.List;

/**
 * The outcomes of a {@link Sortie}'s kills and losses: an enemy destroyed (its pay, drops, death effects,
 * lines and groups), a set piece's part wrecked or shot off, a spore mine shot, a unit escaped or burst on
 * its own, a destroy-targets group lost, the tendrils' outcomes and the sondes; with the objectives they
 * decide, the secondary's pay ({@link #paySecondary()}) and the wingman's first kill.
 */
final class ObjectivePasses {
    private final Sortie s;
    private final LevelScript script;
    private final Rules rules;
    private final Ship ship;
    private final EnemyForce force;
    private final SimEvents events;
    private final Radio radio;
    private final Tally tally;
    private final Objectives objectives;
    private final SetPiece[] setPieces;
    /** M5 part A: the wingman in the escort slot (Rook); null without one. */
    private final Wingman wingman;
    /** The mount index the wingman's shots carry. */
    private final int wingmanMount;
    /** M7 part C: the level's tendril gates; null without. */
    private final Links links;

    private final Sondes sondes;
    /** Part G: per part drop of the script, the index of its set piece; -1 when the level has none of that slug. */
    private final int[] partDropPiece;
    /** Part G: the set piece of a parts objective (Level 07's carrier); -1 without one. */
    private final int partsPiece;
    /** Whether a group's outcome was paid and called in this attempt. */
    final boolean[] groupCalled;
    /**
     * M5 part B: whether the wingman's shot or blast made a kill in this attempt (the radio's
     * {@code escort-first-kill} fired then); hashed with a wingman and kept by the boss checkpoint.
     */
    boolean escortKilled;

    ObjectivePasses(
            Sortie s,
            LevelScript script,
            Rules rules,
            Ship ship,
            EnemyForce force,
            SimEvents events,
            Radio radio,
            Tally tally,
            Objectives objectives,
            SetPiece[] setPieces,
            Wingman wingman,
            int wingmanMount,
            Links links,
            Sondes sondes,
            int[] partDropPiece,
            int partsPiece) {
        this.s = s;
        this.script = script;
        this.rules = rules;
        this.ship = ship;
        this.force = force;
        this.events = events;
        this.radio = radio;
        this.tally = tally;
        this.objectives = objectives;
        this.setPieces = setPieces;
        this.wingman = wingman;
        this.wingmanMount = wingmanMount;
        this.links = links;
        this.sondes = sondes;
        this.partDropPiece = partDropPiece;
        this.partsPiece = partsPiece;
        groupCalled = new boolean[script.groups().size()];
    }

    /** A new attempt: no group called, no kill of the wingman's. */
    void reset() {
        escortKilled = false;
        java.util.Arrays.fill(groupCalled, false);
    }

    /**
     * M7 part D: the unit part of enemy {@code index} was destroyed (design/enemies/ground/shellback, the vent):
     * its event and its part bounty, paid as a chain's part is; the unit stays (its gun silenced for good when
     * the part carries it).
     */
    void partOff(int index) {
        Enemy enemy = force.enemies().get(index);
        events.add(SimEvents.Type.UNIT_PART_DESTROYED, enemy.partX(), enemy.partY(), enemy.kind());
        tally.partKill(enemy.spec().part().orElseThrow().bounty());
    }

    /**
     * A kill by {@code mount}'s shot or blast (M5 part B, design/player/wingmen, Scripted lines about
     * him): the wingman's first, while he flies, starts the radio's {@code escort-first-kill} cues,
     * once per attempt. Called just before the kill is paid.
     */
    void escortKill(int mount) {
        if (wingman == null || mount != wingmanMount || escortKilled || wingman.ejected()) {
            return;
        }
        escortKilled = true;
        radio.cue(CueTrigger.ESCORT_FIRST_KILL, "");
    }

    void destroy(int index) {
        Pool<Enemy> enemies = force.enemies();
        Enemy enemy = enemies.get(index);
        EnemySpec spec = enemy.spec();
        events.add(SimEvents.Type.ENEMY_DESTROYED, enemy.x(), enemy.y(), enemy.kind());
        if (links != null && spec.link().isPresent()) {
            // M7 part C: a gate's anchor: its standing tendril breaks.
            links.anchorGone(enemy, true);
        }
        int headBonus = 0;
        if (enemy.grounded() && (enemy.driftX() != 0 || enemy.driftY() != 0)) {
            // M5 part G: its remains drift on with its floe (Level 13's mortars).
            events.add(
                    SimEvents.Type.WRECK_DRIFT,
                    enemy.x(),
                    enemy.y(),
                    SimEvents.driftValue(enemy.driftX(), enemy.driftY()));
        }
        if (enemy.chain() != null) {
            // A segment chain counts as one enemy, its wave's head: the other parts pay their bounty only.
            boolean kill = enemy.link() == 0 && enemy.chain().original();
            if (!kill) {
                int bonus = force.firstBonusDue(enemy) ? enemy.chain().spec().tailFirstBonus() : 0;
                tally.partKill(spec.bounty() + bonus);
                force.memberDestroyed(enemy);
                enemies.free(index);
                return;
            }
            // M7 part C: a head destroyed before any other part pays its first bonus (the whole chain's).
            headBonus = force.headFirstBonusDue(enemy) ? enemy.chain().spec().headFirstBonus() : 0;
            force.memberDestroyed(enemy);
        }
        if (spec.part().isPresent() && enemy.partAlive()) {
            // M7 part D: the body destroyed while its part stands pays the whole (the Shellback's 40 + 8).
            headBonus += spec.part().get().firstBonus();
        }
        tally.kill(spec.bounty() + headBonus, enemy.grounded() ? CreditSource.GROUND_TARGETS : CreditSource.KILLS);
        int kills = objectives.kill(enemy.kind());
        int group = enemy.group();
        String tag = enemy.tag();
        double x = enemy.x();
        double y = enemy.y();
        if (s.groundPasses.rock != null && enemy.grounded()) {
            s.groundPasses.throwRocks(x, y);
        }
        if (spec.pull().isPresent()) {
            // M7 part E: a Dust Devil's grit cloud drops every pickup it held.
            s.pickupPasses.release(enemy);
        }
        if (enemy.carried().isPresent()) {
            s.pickupPasses.drop(enemy.carried().get(), enemy.x(), enemy.y());
        }
        if (spec.drop().isPresent() && kills % dropEvery(spec.drop().get().every()) == 0) {
            s.pickupPasses.drop(spec.drop().get().pickup(), enemy.x(), enemy.y());
        }
        if (spec.deathBurst().isPresent()) {
            force.deathBurst(enemy, ship);
        }
        if (spec.brood().isPresent()) {
            force.hatch(enemy, ship);
        }
        if (spec.aura().isPresent()) {
            // M7 part D: a Herald's rings collapse, its escorts break off.
            force.heraldGone(enemy, true);
        }
        if (enemy.walking()) {
            events.add(
                    SimEvents.Type.WALKER_DOWN,
                    enemy.x(),
                    enemy.y(),
                    SimEvents.walkerValue(enemy.kind(), enemy.facing()));
        }
        if (enemy.laneWalking()) {
            // M5 part G: a lane walker's hull flips and sinks at its heading (the Skimmer).
            events.add(
                    SimEvents.Type.LANE_WALKER_DOWN,
                    enemy.x(),
                    enemy.y(),
                    SimEvents.walkerValue(enemy.kind(), enemy.facing()));
        }
        if (enemy.flock() != null) {
            // M5 part D: a swarm's member drops out of its flock.
            force.flockMemberGone(enemy);
        }
        enemies.free(index);
        if (kills == 1) {
            radio.cue(CueTrigger.FIRST_KILL, spec.slug());
        }
        if (group >= 0) {
            decided(group, objectives.groupUnitDestroyed(group), x, y);
        }
        if (objectives.escapeDestroyed(spec.slug(), tag)) {
            paySecondary();
        }
    }

    /**
     * M5 part F: an enemy's drop {@code every} with the level's drop factor: divided by it, rounded to
     * the nearest, at least 1 (Level 12's shield cells; the factor 1 elsewhere leaves it).
     */
    private int dropEvery(int every) {
        double factor = script.drops().dropFactor();
        return factor == 1 ? every : Math.max(1, (int) Math.round(every / factor));
    }

    /** A spore mine was shot: it pays its credits (no kill, no chain). */
    void shootMine(int index) {
        Pool<Mine> mines = force.mines();
        Mine mine = mines.get(index);
        events.add(SimEvents.Type.MINE_DESTROYED, mine.x(), mine.y());
        int credits = mine.spec().credits();
        tally.bounty(CreditSource.KILLS, credits);
        tally.scoreValue(credits);
        mines.free(index);
    }

    /**
     * Part {@code part} of set piece {@code k} was destroyed: it pays its bounty; the vital part
     * takes the rest with it, paying theirs, and the unit is destroyed.
     */
    void wreck(int k, int part) {
        SetPiece piece = setPieces[k];
        payPart(piece, part);
        shotOff(k, piece, part);
        if (!piece.spec().parts().get(part).vital()) {
            return;
        }
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.wreckPart(p)) {
                payPart(piece, p);
            }
        }
        SetPieceSpec spec = piece.spec();
        events.add(SimEvents.Type.SET_PIECE_DESTROYED, piece.x(), piece.y(), k);
        tally.countKill();
        if (spec.drop().isPresent()) {
            double half = EnemyGun.BULLET.width();
            s.pickupPasses.drop(
                    spec.drop().get(),
                    Math.clamp(piece.x(), half, PlayField.WIDTH - half),
                    Math.clamp(piece.y(), half, PlayField.HEIGHT - half));
        }
        piece.destroy();
        radio.cue(CueTrigger.FIRST_KILL, spec.slug());
        if (spec.isBoss()) {
            events.add(SimEvents.Type.BOSS_DESTROYED, piece.x(), piece.y(), bossCredits(spec));
            if (piece.stride().isPresent()) {
                // M7 part F: the walker topples along its heading; its wreck stays.
                events.add(
                        SimEvents.Type.BOSS_WRECK,
                        piece.x(),
                        piece.y(),
                        piece.stride().get().heading());
            }
            radio.cue(CueTrigger.BOSS_DESTROYED, spec.slug());
            if (s.arenaEndTicks >= 0 && s.levelTick >= s.arenaStartTicks && s.levelTick < s.arenaEndTicks - 1) {
                s.arenaJump = true;
            }
            // M7 part F: an arena that holds after its boss keeps the clock halted over the death chain and the hold.
            s.bossDownInArena(piece.boss().orElseThrow());
            // M5 part E (user decision E8 = a): an afloat objective is decided at the boss's death.
            if (objectives.bossDownAfloat()) {
                paySecondary();
            }
        }
        if (objectives.escapeDestroyed(spec.slug(), "")) {
            paySecondary();
        }
        if (k == partsPiece && objectives.partsSurvived()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, piece.x(), piece.y());
        }
    }

    /**
     * Part {@code part} of set piece {@code k} was shot off (not lost in the unit's death): the
     * pickup of a part drop whose turn it is falls where it broke (Level 07's first bay sac), and a
     * parts objective counts it.
     */
    private void shotOff(int k, SetPiece piece, int part) {
        List<DropSpec.PartDrop> drops = script.drops().partDrops();
        for (int d = 0; d < drops.size(); d++) {
            DropSpec.PartDrop drop = drops.get(d);
            if (partDropPiece[d] == k && among(drop.parts(), part) && wrecked(piece, drop.parts()) == drop.dropsAt()) {
                double half = EnemyGun.BULLET.width();
                s.pickupPasses.drop(
                        drop.pickup(),
                        Math.clamp(piece.partX(part), half, PlayField.WIDTH - half),
                        Math.clamp(piece.partY(part), half, PlayField.HEIGHT - half));
            }
        }
        if (k == partsPiece && among(script.objectives().secondary().parts(), part) && objectives.partShotOff()) {
            paySecondary();
        }
        if (piece.spec().isBoss() && !piece.spec().parts().get(part).vital()) {
            // M5 part E: the radio's boss-part-destroyed (the first of the parts its cue names).
            radio.cue(
                    CueTrigger.BOSS_PART_DESTROYED,
                    piece.spec().parts().get(part).name());
        }
    }

    private static boolean among(List<Integer> parts, int part) {
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i) == part) {
                return true;
            }
        }
        return false;
    }

    /** How many of {@code parts} of {@code piece} are wrecked. */
    private static int wrecked(SetPiece piece, List<Integer> parts) {
        int wrecked = 0;
        for (int i = 0; i < parts.size(); i++) {
            wrecked += piece.partWrecked(parts.get(i)) ? 1 : 0;
        }
        return wrecked;
    }

    /** What a boss's parts paid together, after the credit factor: the credit shower's number. */
    private int bossCredits(SetPieceSpec spec) {
        // Indexed: a for-each would allocate an iterator whenever a boss is wrecked.
        List<PartSpec> parts = spec.parts();
        int credits = 0;
        for (int i = 0; i < parts.size(); i++) {
            credits += (int) Math.rint(parts.get(i).bounty() * rules.scoring().creditFactor());
        }
        return credits;
    }

    private void payPart(SetPiece piece, int part) {
        tally.partKill(piece.spec().parts().get(part).bounty());
        events.add(SimEvents.Type.PART_DESTROYED, piece.partX(part), piece.partY(part), part);
    }

    /** The secondary objective is met: its credits, its event and its line. */
    void paySecondary() {
        int credits = script.objectives().secondary().credits();
        tally.earn(CreditSource.OBJECTIVES, credits);
        tally.scoreValue(credits);
        events.add(SimEvents.Type.OBJECTIVE_MET, ship.x(), ship.y());
        radio.cue(CueTrigger.SECONDARY_OBJECTIVE, "");
    }

    /**
     * A spawner burst on its own (design/enemies/air/brood-pod): it pays its burst bounty, which is
     * not a kill (no kill counter, no chain), and counts as an escape for an escapes objective.
     */
    void selfBurst(Enemy enemy) {
        int credits = enemy.spec().brood().orElseThrow().burstBounty();
        tally.unchained(credits);
        events.add(SimEvents.Type.BROOD_BURST, enemy.x(), enemy.y(), enemy.kind());
        escaped(enemy);
    }

    /**
     * A unit left the screen alive: a ground group's unit can lose its group, one of an escapes
     * objective's enemy fails it, and the first of an enemy calls its line.
     */
    void escaped(Enemy enemy) {
        String slug = enemy.spec().slug();
        if (enemy.spec().pull().isPresent()) {
            // M7 part E: a Dust Devil that escapes carries off the pickups it held.
            s.pickupPasses.carryOff(enemy);
        }
        if (links != null && enemy.spec().link().isPresent()) {
            // M7 part C: a gate's anchor left the screen: a tendril still standing is lost.
            links.anchorGone(enemy, false);
        }
        if (enemy.group() >= 0) {
            int group = enemy.group();
            int state = objectives.groupUnitEscaped(group);
            if (!script.objectives().targets().isEmpty()) {
                // A battery whose unit got away can no longer be destroyed: the mission fails at once.
                failTargets(group);
            } else {
                decided(group, state, enemy.x(), enemy.y());
            }
        }
        if (objectives.escapeLost(slug, enemy.tag())) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, enemy.x(), enemy.y());
        }
        radio.cue(CueTrigger.ENEMY_ESCAPED, slug);
    }

    /**
     * A group's outcome once it is decided: a cleared group pays its credits and calls its line,
     * the objective is met when every group is cleared; a lost group calls its line, and the first
     * lost one also the first-loss line.
     */
    private void decided(int group, int state, double x, double y) {
        String name = script.groups().get(group);
        if (state == Objectives.CLEARED && !groupCalled[group]) {
            groupCalled[group] = true;
            if (script.objectives().targets().isEmpty()) {
                int credits = script.objectives().secondary().credits();
                tally.earn(CreditSource.OBJECTIVES, credits);
                tally.scoreValue(credits);
            }
            // Indexed: a for-each would allocate an iterator whenever a group is cleared.
            List<DropSpec.GroupDrop> drops = script.drops().groupDrops();
            for (int i = 0; i < drops.size(); i++) {
                DropSpec.GroupDrop drop = drops.get(i);
                if (drop.group() == group) {
                    s.pickupPasses.drop(drop.pickup(), x, y);
                }
            }
            events.add(SimEvents.Type.GROUP_CLEARED, ship.x(), ship.y(), group);
            radio.cue(CueTrigger.GROUP_CLEARED, name);
            if (objectives.secondaryMet()) {
                events.add(SimEvents.Type.OBJECTIVE_MET, ship.x(), ship.y());
                radio.cue(CueTrigger.SECONDARY_OBJECTIVE, "");
            }
        } else if (state == Objectives.LOST && !groupCalled[group]) {
            groupCalled[group] = true;
            events.add(SimEvents.Type.GROUP_LOST, ship.x(), ship.y(), group);
            radio.cue(CueTrigger.GROUP_LOST, name);
            if (objectives.groupsLost() == 1) {
                radio.cue(CueTrigger.FIRST_GROUP_LOST, "");
            }
        }
    }

    /**
     * A unit of a destroy-targets group left the screen alive: the group is lost and the primary
     * objective fails at once (design/systems/retry, on a failed primary objective), with the
     * level's line naming the group.
     */
    private void failTargets(int group) {
        if (s.failed || s.complete || s.wrecked || groupCalled[group]) {
            return;
        }
        groupCalled[group] = true;
        events.add(SimEvents.Type.GROUP_LOST, ship.x(), ship.y(), group);
        if (rules.invulnerableShip()) {
            // The debug option that lets a capture see the level to its end keeps it going too.
            return;
        }
        s.failed = true;
        s.failedGroup = group;
        events.add(SimEvents.Type.PRIMARY_FAILED, ship.x(), ship.y(), group);
        radio.cue(CueTrigger.MISSION_FAILED, "");
    }

    /**
     * M7 part C, for part C's links (C3): a gate's tendril was broken; a links secondary counts it ({@link
     * Objectives#linkBroken}) and pays at the last.
     */
    void tendrilBroken() {
        if (objectives.linkBroken()) {
            paySecondary();
        }
    }

    /**
     * M7 part C (design/enemies/ground/tendril-anchor): a tendril burnt through by a beam pays its bounty (score
     * × the chain, extending it); one broken by an anchor's death pays nothing of its own.
     */
    void tendrilBurnt(Tendril tendril) {
        if (tendril.burnt()) {
            tally.partKill(tendril.spec.burntBounty());
        }
    }

    /** M7 part C, for C3: a tendril left the screen unbroken; a links secondary fails. */
    void tendrilLost() {
        if (objectives.linkLost()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, ship.x(), ship.y());
        }
    }

    /**
     * M5 part F: the weather sondes drift; the ship collects those it flies through (while it flies), and
     * a collect: sondes objective is met at the last and failed at the first one lost.
     */
    void updateSondes() {
        sondes.update(s.levelTick, s.flying() && !s.complete, ship.x(), ship.y(), events);
        for (int k = 0; k < sondes.collectedNow(); k++) {
            if (objectives.sondeCollected()) {
                paySecondary();
            }
        }
        if (sondes.lostNow() > 0 && objectives.sondeLost()) {
            events.add(SimEvents.Type.OBJECTIVE_FAILED, ship.x(), ship.y());
        }
    }
}
