package vanguard.content;

import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.GroundObject;
import vanguard.sim.Pickup;
import vanguard.sim.PlayField;
import vanguard.sim.SetPiece;
import vanguard.sim.Sondes;
import vanguard.sim.Sortie;

/**
 * A simple pilot for headless runs of a whole level: fires all the time, lines up under the lowest
 * enemy on screen (or a ground object when the air is clear), sidesteps bullets, rammers, armed
 * spores and large debris that come close, stays under a set piece on the player's layer and
 * aims at its vital part (a boss's lowest part that takes damage; in a boss fight each move is
 * chosen by a short look-ahead over the bullets and air enemies, keeping to the field's centre
 * band), and picks up what drops when nothing threatens. At Level 05's batteries it flies low, so
 * a unit stays in its line of fire as long as possible. With a convoy (Level 04) it
 * cruises among the column, so more of the ground enemies' aimed shots (at the nearer of the ship
 * and the convoy) go for it, and it shoots the ground enemies nearest the convoy first. With a segment chain on the
 * screen (Level 06) it slips sideways to the side clear of its members and lines up under a
 * chain's head rather than its body (a cut grows a new head). Over a hardened destroy-targets unit
 * (Level 09's Hive Nodes, which only bombs and mortars crack) it flies over the unit, where its bombs
 * fall, leading it by the scroll during the bombs' fall, holding its line as over a battery; when
 * one is about to get away it calls the Airstrike (if fitted and charged) with a bomber over it; M8 part B: the same over
 * a hardened unit of the escapes objective (Level 22's Drill Rigs, pumps first), over it only with an {@code anti-ground}
 * gun, the strike in any case. It reads each unit's current layer (a
 * pouncing Ravager is in the air for part of its leap). With an air escort (Level 10's shuttles) it
 * cruises low, below the shuttle band; it never lines up under a cloaked unit (a Wraith on
 * {@code high-air}, which only homing reaches and which cannot ram), and a decloaked Wraith holding
 * below it gets its x first, so the rear gun hits it; it sidesteps bullets coming up from below as
 * well as from above; it lines up under the ferry hatch while it is on the upper half of the screen,
 * to shoot it open. Against an arena boss with slam lanes (M5 part E, Level 11's Harbour Kraken) it
 * keeps out of every telegraphed lane until its impact, lines up under what is exposed (an arm risen
 * or awash in its lane, else the surfaced head's nearer eye, else the head's place) and dodges the
 * bullets with the boss planner, a move into a lane about to be slammed counting as a hit; played
 * {@link ArenaPlay#ARMS arm-first} it cuts the slam arm guarding the convoy's ships first, baiting
 * the slams into the lanes where they cost least. In weather (M5 part F, Level 12's storm) it shakes off a
 * latched Lamprey at once (left and right in turn, {@link #SHAKE_SECONDS} each, before anything else), leans
 * against a gust from its telegraph on (a little upwind, and clear of the downwind edge) and flies through
 * the weather sondes when nothing threatens. Guarding a defended ally (M5 part G, Level 13's Nansen Relay)
 * it cruises just below it and lines up on what attacks it first: a mortar lobbing at it, then a diver
 * sent at it before its dive, then a walker with it in its arc, then the hooked unit nearest it (lower
 * than itself: it drops below the unit to shoot it), and with nothing hooked on the screen it holds
 * station under the ally, in the way of what comes up at it from below; ground units, which cannot ram,
 * are not dodged there. The plays live beside it: {@link DodgePlay}, {@link TargetPlay}, {@link HazardPlay},
 * {@link KrakenPlay}, {@link SpirePlay}, {@link WalkerPlay}, {@link SiegePlay}, {@link RetreatPlay} (M7 part G: the
 * reversed scroll, Level 20), {@link ColossusPlay} (M7 part H: the breaching boss, Level 21), {@link WaterPlay} (M8 part C:
 * the drag, the siphon pumps and the Glassfin's rams, Level 23), {@link AbyssPlay} (M8 part D: the Abyss Ray's shadow,
 * the Barbfins, Level 24), {@link SirenPlay} (M8 part E: the nests' bodies, the trench's rock and the slow, Level 25), {@link DarkPlay} (M8 part
 * F: every move planned among the Glow Anglers' lunges and the jellies' rings, Level 26) and the
 * moves' {@link MovePlanner}. It reads the sortie's state only, so it is deterministic; the recorded replay stores its commands. Public for the campaign tests (the
 * Act 1 playthrough, {@code ActPlaythroughTest}).
 */
public final class Autopilot {
    static final double CRUISE_Y = 110;

    /** Its cruise height over a convoy (the column spans y = 118-526). */
    private static final double CONVOY_CRUISE = 220;

    /**
     * Its height while a battery unit is its target (Level 05): low, so a unit scrolling down stays
     * above it, in its line of fire, as long as possible.
     */
    private static final double BATTERY_Y = 45;

    /** Level 09: the bombs' fall, s (design/player/weapons/bomb-rack): it leads a node by the scroll over it. */
    private static final double BOMB_FALL = 0.5;

    /** Level 10: how far it keeps above a holding Wraith it lines up over, px. */
    static final double AMBUSH_ABOVE = 55;

    /** Level 10: its height over a Wraith still cloaked below it, px: clear of the highest hold point and the decloak. */
    private static final double AMBUSH_WAIT = 135;

    /** Level 10: with an air escort it keeps out of the side lanes, where the Wraiths leave, px from either edge. */
    private static final double SIDE_LANE = 96;

    /** M5 part F: while latched it holds each direction of its shake this long, s (4 reversals in about 0.4 s). */
    private static final double SHAKE_SECONDS = 0.1;

    /** M5 part G: it drops this far below a hooked unit lower than its cruise to shoot it, px. */
    private static final double GUARD_BELOW = 70;

    private Autopilot() {}

    /**
     * M5 part E: how it plays an arena boss with slam lanes (Level 11's Harbour Kraken); no other
     * level has one, so the choice moves no other level's results.
     */
    public enum ArenaPlay {
        /**
         * Step E2c's pilot: it lines up under an arm out of the water, else under the surfaced head's
         * nearer eye, and lets the slams fall where its x sends them.
         */
        EYES,
        /**
         * Arm-first (2026-10-09, the user's convoy-bonus measurement): it cuts the slam arm guarding
         * the most ships afloat first. It baits each slam (or volley) to where it costs least by the
         * boss's own lane rules (the healthiest ship of that arm's half, a free lane), waits beside a
         * telegraphed lane and cuts the arm as it lies awash after its impact; the surfaced head's
         * eyes while its window is open between slams. Once some place keeps every slam off the ships
         * (the guarding arm severed) it baits there and plays the head; when the ships of the arm's
         * half cannot spare the slams its cut takes (hard), it plays as {@link #EYES}.
         */
        ARMS
    }

    /**
     * M7 part C: the channel it takes where rock splits the canyon (Level 16's fork mesa and rock pillar); the
     * tests fly both. Without terrain the choice changes nothing.
     */
    public enum Channel {
        /** The left channel (the default: Level 16's relay hut, the secret, stands in it). */
        LEFT,
        /** The right channel. */
        RIGHT
    }

    /** M7 part C: how low it holds while a standing tendril comes down at it, px. */
    private static final double TENDRIL_HOLD = 40;

    /**
     * One step's commands, an arena boss played as step E2c's pilot ({@link ArenaPlay#EYES}): the
     * measurements the levels' tests and the balance were made with.
     */
    public static int commands(Sortie sortie) {
        return commands(sortie, ArenaPlay.EYES);
    }

    /** One step's commands, an arena boss played as {@code play} says. */
    public static int commands(Sortie sortie, ArenaPlay play) {
        return commands(sortie, play, Channel.LEFT);
    }

    /** One step's commands, the canyon's split taken by {@code channel} (M7 part C, Level 16). */
    public static int commands(Sortie sortie, Channel channel) {
        return commands(sortie, ArenaPlay.EYES, channel);
    }

    /**
     * One step's commands, an arena boss played as {@code play} says and (M7 part C) the canyon's split taken by
     * {@code channel}: in a canyon it keeps its hull {@link HazardPlay#ROCK_MARGIN} px off the rock at its height now and over
     * the next {@link HazardPlay#ROCK_AHEAD} s, whatever else it does.
     */
    public static int commands(Sortie sortie, ArenaPlay play, Channel channel) {
        int commands = pilot(sortie, play);
        if (sortie.groundAllies() && SiegePlay.rollThreat(sortie)) {
            // M7 part D: a wounded Shellback's roll would crush a dome: no shot at it until the dome is out of reach.
            commands &= ~Command.FIRE.bit();
        }
        if (sortie.vents().grates() > 0) {
            commands = HazardPlay.ventFilter(sortie, commands);
        }
        return sortie.terrain() == null ? commands : HazardPlay.canyon(sortie, commands, channel);
    }

    private static int pilot(Sortie sortie, ArenaPlay play) {
        double shipX = sortie.ship().x();
        double shipY = sortie.ship().y();
        int commands = Command.FIRE.bit();
        if (sortie.latched() > 0) {
            // M5 part F: a latched Lamprey drains the shield; shake it off at once (4 reversals within 1.2 s).
            boolean left = ((long) Math.floor(sortie.realSeconds() / SHAKE_SECONDS)) % 2 == 0;
            return commands | (left ? Command.LEFT.bit() : Command.RIGHT.bit());
        }
        if (DarkPlay.dark(sortie)) {
            // M8 part F: a level with lunging units (Level 26's Glow Anglers): every move planned (DarkPlay).
            return commands | DarkPlay.move(sortie);
        }
        double lob = DodgePlay.lobThreat(sortie, shipX, shipY);
        if (lob != 0) {
            return commands | (lob < 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
        }
        double coil = DodgePlay.chainThreat(sortie, shipX, shipY);
        if (coil != 0) {
            return commands | (coil < 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
        }
        // M8 part C: out of a Glassfin's ram dive (Level 23, WaterPlay).
        double ram = WaterPlay.ramThreat(sortie, shipX, shipY);
        if (ram != 0) {
            return commands | (ram < 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
        }
        double hazard = DodgePlay.hazard(sortie, shipX, shipY);
        if (hazard != 0) {
            return commands | (hazard < 0 ? Command.LEFT.bit() : Command.RIGHT.bit()) | Command.DOWN.bit();
        }
        if (sortie.reversed()) {
            // M7 part G: out of the pursuit's lunge before anything else (Level 20, RetreatPlay).
            double lunge = RetreatPlay.lungeThreat(sortie, shipX, shipY);
            if (lunge != 0) {
                return commands | (lunge < 0 ? Command.LEFT.bit() : Command.RIGHT.bit());
            }
        }
        SetPiece walking = WalkerPlay.walkingBoss(sortie);
        if (walking != null) {
            return commands | WalkerPlay.walkerMove(sortie, walking);
        }
        SetPiece colossus = ColossusPlay.breachingBoss(sortie);
        if (colossus != null) {
            // M7 part H: the Dust Colossus (Level 21, ColossusPlay), before the rooted arena bosses' play.
            return commands | ColossusPlay.move(sortie, colossus);
        }
        SetPiece kraken = KrakenPlay.arenaBoss(sortie);
        if (kraken != null) {
            return commands | KrakenPlay.arenaMove(sortie, kraken, play);
        }
        SetPiece spire = SpirePlay.rootedBoss(sortie);
        if (spire != null) {
            return commands | SpirePlay.spireMove(sortie, spire);
        }
        SetPiece piece = descended(sortie);
        if (piece != null && piece.boss().isPresent()) {
            return commands | MovePlanner.bossMove(sortie, vitalX(piece), Math.min(CRUISE_Y, bodyBottom(piece) - 70));
        }
        if (sortie.reversed()) {
            // M7 part G: the retreat's middle band, the rear guns on what comes up from below, each move weighed
            // against the bullets and units from both edges (RetreatPlay).
            return commands | RetreatPlay.move(sortie, shipX, shipY);
        }
        double threat = DodgePlay.threat(sortie, shipX, shipY);
        if (threat != 0) {
            // With an air escort a threat may come from below (a Wraith's burst, a looping swarm, a Wraith
            // holding at the bottom edge): no lower then, nor below its cruise.
            int vertical = sortie.airEscort() && (shipY < CRUISE_Y || DodgePlay.fromBelow(sortie, shipX, shipY))
                    ? 0
                    : Command.DOWN.bit();
            // M8 part B (step B3b): a rig's strike goes in during the dodge too (the plan's fit has no anti-ground
            // gun).
            int strike = TargetPlay.rigStrikeNow(sortie, shipX) ? Command.SPECIAL.bit() : 0;
            return commands | (threat < 0 ? Command.LEFT.bit() : Command.RIGHT.bit()) | vertical | strike;
        }
        double targetX = shipX;
        double targetY = cruise(sortie);
        if (piece != null) {
            return commands
                    | MovePlanner.steer(shipX, shipY, vitalX(piece), Math.min(CRUISE_Y, bodyBottom(piece) - 70));
        }
        Enemy lowest = null;
        for (int i = 0; i < sortie.enemyCount(); i++) {
            Enemy enemy = sortie.enemy(i);
            double y = enemy.renderY(1);
            // M5 part H: a root burst's growth takes no damage while it grows (Level 14); M7 part B: nothing
            // hits a buried Burrower.
            if (!enemy.cloaked()
                    && !enemy.growing()
                    && enemy.layer().touchable()
                    && !TargetPlay.closedVortex(enemy)
                    && y < PlayField.HEIGHT - 10
                    && y > shipY + 40
                    && (lowest == null || y < lowest.renderY(1))) {
                lowest = enemy;
            }
        }
        Enemy core = TargetPlay.openVortex(sortie, shipY);
        if (core != null) {
            // M7 part E: a Dust Devil rising or open (Level 18): its core in the short window, from below its pull.
            lowest = core;
        }
        Enemy maw = TargetPlay.burrower(sortie, shipY);
        if (maw != null) {
            // M7 part B: a Burrower bulging or up (Level 15): under its spot before it breaks out, to hit it in
            // its window; its ring's bullets are dodged as any.
            lowest = maw;
        }
        Enemy behind = TargetPlay.rearAmbusher(sortie, shipY);
        if (behind != null) {
            lowest = behind;
            targetY = TargetPlay.holding(behind) ? behind.renderY(1) + AMBUSH_ABOVE : AMBUSH_WAIT;
            int pair = TargetPlay.ambushPair(sortie);
            if (pair >= 0) {
                Enemy a = sortie.enemy(pair / 1024);
                Enemy b = sortie.enemy(pair % 1024);
                double strikeX = (a.renderX(1) + b.renderX(1)) / 2;
                boolean rising = a.ambushPhase() == Enemy.AmbushPhase.RISE && b.ambushPhase() == Enemy.AmbushPhase.RISE;
                if (rising && Math.abs(strikeX - shipX) <= 3 * MovePlanner.DEAD_ZONE) {
                    return commands | Command.SPECIAL.bit();
                }
                return commands | MovePlanner.steer(shipX, shipY, strikeX, AMBUSH_WAIT);
            }
        }
        Enemy battery = TargetPlay.battery(sortie, shipY);
        if (battery != null) {
            lowest = battery;
            targetY = Math.min(targetY, BATTERY_Y);
        }
        Enemy growth = TargetPlay.growth(sortie, shipY);
        if (growth != null) {
            // M5 part H: a root burst's growth (Level 14's secondary) before it scrolls past, from low down,
            // waiting under it while it grows.
            lowest = growth;
            targetY = Math.min(targetY, BATTERY_Y);
        }
        Enemy node = TargetPlay.hardenedTarget(sortie);
        if (node != null) {
            lowest = node;
            targetY = Math.max(40, node.renderY(1) - sortie.groundSpeed() * BOMB_FALL);
        }
        // M8 part B: an escapes objective's hardened unit (Level 22's Drill Rigs): with an anti-ground gun over it as
        // over a Hive Node, its pumps first, then its core; without one only the Airstrike below.
        Enemy rig = node == null ? TargetPlay.hardenedEscapee(sortie) : null;
        boolean overRig = rig != null && TargetPlay.overRig(sortie);
        if (overRig) {
            lowest = rig;
            // the bombs land where the pod is as they land (measured): over the part, no lead
            targetY = Math.max(40, TargetPlay.partsFirstY(rig));
        }
        Enemy head = TargetPlay.chainHead(sortie, shipY);
        if (head != null) {
            lowest = head;
        }
        // M8 part D: a Barbfin holding above the ship before the mute fry (Level 24, AbyssPlay).
        Enemy holder = AbyssPlay.holder(sortie, shipY);
        if (holder != null) {
            lowest = holder;
        }
        // M8 part C: the escapes objective's unit low on the screen before it gets away (Level 23's siphon pumps).
        Enemy escapee = WaterPlay.escapee(sortie, shipY);
        if (escapee != null) {
            lowest = escapee;
            // M8 part D (step E4): close under a tough spawning reef, where it is shut, then over it (Level 24).
            targetY = WaterPlay.escapeeY(sortie, escapee, targetY);
        }
        // M8 part D: an Abyss Ray's shadow and rise, the torpedoes reaching it under the play plane (Level 24).
        Enemy shadow = AbyssPlay.shadow(sortie);
        if (shadow != null) {
            lowest = shadow;
        }
        Enemy anchor = TargetPlay.gateAnchor(sortie, shipX, shipY);
        if (anchor != null) {
            // M7 part C: a gate's anchor before its tendril reaches the ship, holding low meanwhile (Level 16).
            lowest = anchor;
            targetY = Math.min(targetY, TENDRIL_HOLD);
        }
        // M7 part D: Level 17's domes: the hooked bomber about to reach its dome, the Choir Herald, any hooked
        // bomber, a Shellback's vent or belly (never while it is armoured), instead of a convoy's threat.
        Enemy walker =
                sortie.groundAllies() ? SiegePlay.siegeTarget(sortie, shipY) : TargetPlay.convoyThreat(sortie, shipY);
        Enemy guard = TargetPlay.guardTarget(sortie);
        if (guard != null) {
            // M5 part G: the defended ally's attackers first, from below them.
            walker = guard;
            targetY = Math.min(targetY, Math.max(40, guard.renderY(1) - GUARD_BELOW));
        }
        if (walker != null) {
            targetX = walker.renderX(1);
        } else if (lowest != null) {
            targetX = overRig && lowest == rig ? TargetPlay.partsFirstX(rig) : lowest.renderX(1);
        } else {
            GroundObject ground = TargetPlay.lowestGround(sortie, shipY);
            Pickup pickup = sortie.pickupCount() > 0 ? sortie.pickup(0) : null;
            if (pickup != null) {
                targetX = pickup.renderX();
                targetY = Math.max(40, pickup.renderY(1) - 10);
            } else if (ground != null) {
                targetX = ground.renderX();
            } else if (TargetPlay.guarding(sortie)) {
                // M5 part G: nothing to shoot: on station under the defended ally.
                targetX = sortie.ally(0).renderX(1);
            }
        }
        Enemy struck = node != null ? node : rig;
        if (struck != null && TargetPlay.strikeDue(sortie, struck)) {
            double strikeX = TargetPlay.strikeX(sortie, struck);
            if (Math.abs(strikeX - shipX) <= MovePlanner.DEAD_ZONE || TargetPlay.rigStrikeNow(sortie, shipX)) {
                return commands | Command.SPECIAL.bit();
            }
            return commands | MovePlanner.steer(shipX, shipY, strikeX, targetY);
        }
        if ((lowest != null || walker != null) && behind == null && sortie.pickupCount() > 0) {
            Pickup pickup = sortie.pickup(0);
            if (Math.abs(pickup.renderX() - shipX) < 120 && pickup.renderY(1) < 260) {
                targetX = pickup.renderX();
                targetY = Math.max(40, pickup.renderY(1) - 10);
            }
        }
        GroundObject cache = (sortie.airEscort() || sortie.terrain() != null) && behind == null
                ? TargetPlay.secretTarget(sortie, shipY)
                : null;
        if (cache != null) {
            // Level 10's ferry hatch (2026-10-08: 50 HP): it lines up under it to shoot it open; M7 part C: Level 16's
            // relay hut (in the left channel; the rock keeps a pilot in the right one from it).
            targetX = cache.renderX();
        }
        if (sortie.airEscort()) {
            targetX = Math.clamp(targetX, SIDE_LANE, PlayField.WIDTH - SIDE_LANE);
        }
        int sonde = TargetPlay.sondeToTake(sortie, shipY);
        if (sonde >= 0 && behind == null) {
            // M5 part F: the weather sondes (Level 12's secondary): under it, waiting at its height or the cruise's.
            Sondes sondes = sortie.sondes();
            targetX = sondes.x(sonde);
            targetY = Math.clamp(sondes.y(sonde), 40, Math.max(40, targetY));
        }
        // M8 part C: under water with its slide in mind (the drag; in the air the plain steer, WaterPlay).
        return commands
                | WaterPlay.steer(
                        sortie,
                        shipX,
                        shipY,
                        DodgePlay.clearOfRail(sortie, HazardPlay.leanX(sortie, targetX)),
                        targetY);
    }

    /**
     * Its cruise height: low, or with a convoy alive among the column, high enough that the air
     * enemies above still give it time and close enough that the ground enemies' aimed shots go for
     * it rather than the crawlers; with an air escort low, below the shuttle band.
     */
    private static double cruise(Sortie sortie) {
        if (sortie.airEscort() || sortie.groundAllies()) {
            return CRUISE_Y;
        }
        for (int k = 0; k < sortie.allyCount(); k++) {
            if (sortie.ally(k).alive()) {
                return CONVOY_CRUISE;
            }
        }
        return CRUISE_Y;
    }

    static int vitalPart(SetPiece boss) {
        for (int p = 0; p < boss.partCount(); p++) {
            if (boss.spec().parts().get(p).vital()) {
                return p;
            }
        }
        return -1;
    }

    /** A set piece on the player's layer, or null. */
    private static SetPiece descended(Sortie sortie) {
        for (int i = 0; i < sortie.setPieceCount(); i++) {
            SetPiece piece = sortie.setPiece(i);
            if (piece.present() && piece.onPlane()) {
                return piece;
            }
        }
        return null;
    }

    /**
     * Where to line up under a set piece: its vital part, while it lives, else its centre; under a
     * boss, its lowest part that takes damage now (a head, then the core).
     */
    static double vitalX(SetPiece piece) {
        if (piece.boss().isPresent()) {
            int lowest = -1;
            for (int p = 0; p < piece.partCount(); p++) {
                if (!piece.partWrecked(p)
                        && !piece.partShielded(p)
                        && (lowest < 0 || piece.partY(p) < piece.partY(lowest))) {
                    lowest = p;
                }
            }
            return lowest < 0 ? piece.renderX(1) : piece.partX(lowest);
        }
        for (int p = 0; p < piece.partCount(); p++) {
            if (piece.spec().parts().get(p).vital() && !piece.partWrecked(p)) {
                return piece.partX(p);
            }
        }
        return piece.renderX(1);
    }

    private static double bodyBottom(SetPiece piece) {
        return piece.renderY(1) - piece.spec().body().height() / 2;
    }
}
