package vanguard.sim;

import java.util.List;
import java.util.Optional;

/**
 * M10 part F (design/enemies/bosses/honour-guard, round 72 Q5 a): a squadron mid-boss, one set piece whose parts are
 * its aces ({@link Squadron}). Each ace is a part of the piece flying its own position; the piece itself is parked off
 * the field. The aces join one after another, each first told {@code tellSeconds} ahead at its edge ({@link
 * SimEvents.Type#ACE_TELL}); they hold their stations and fly their moves ({@link AceFlight}) with their hulls always
 * {@code separation} px apart, edge to edge; each carries its own hex {@code shield}; touching the ship it deals the
 * {@code contact} damage once and flies on. The one boss bar sums the aces (a segment each, in their order); each pays
 * its part's bounty as it dies; the last death destroys the piece. Built per difficulty: the numbers are the
 * difficulty's.
 *
 * <p>Heights are px up from the bottom edge (the design's "y 70–170" from the top is 370–470 here).
 *
 * @param aces the aces in their join order (the bar's segment order); each on its own part, every part an ace
 * @param tellSeconds how long an ace's first entry is told at its edge before it crosses it (the spec's 2.5 s)
 * @param separation the least gap between two aces' hulls, edge to edge, px (the Act 6 formation rule: 80)
 * @param hull an ace's hull, px (the sprite, 60×54): the box the separation and "wholly on the field" read; its hit
 *     box is its part's
 * @param shield each ace's hex shield (60, refilling after 2 s; the EMP Burst and a {@code shield-breaker} empty it)
 * @param contact what touching the ship does
 * @param playbook step F1b: what the aces fire and when ({@link AcePlaybook}); empty: they only join and hold (a
 *     caller starts their moves)
 */
public record SquadronSpec(
        List<Ace> aces,
        double tellSeconds,
        double separation,
        Hitbox hull,
        EnemySpec.Shield shield,
        Contact contact,
        Optional<SquadronPlaybook> playbook) {
    /** A squadron without a playbook (step F1a's). */
    public SquadronSpec(
            List<Ace> aces,
            double tellSeconds,
            double separation,
            Hitbox hull,
            EnemySpec.Shield shield,
            Contact contact) {
        this(aces, tellSeconds, separation, hull, shield, contact, Optional.empty());
    }

    /** Step F1b: this squadron flying {@code withPlaybook}. */
    public SquadronSpec withPlaybook(SquadronPlaybook withPlaybook) {
        return new SquadronSpec(aces, tellSeconds, separation, hull, shield, contact, Optional.of(withPlaybook));
    }

    /**
     * How an ace flies: the leader holds the top band strafing with its nose on the ship and slides across; the dancer
     * flies figure-eights in the middle band, loops, rolls past the ship and lays a ribbon; the feinter holds the bottom
     * band's side lane and dives at the ship, pulling out, rakes up a side and loops out of the top edge.
     */
    public enum Role {
        SNIPER,
        DANCER,
        FEINTER
    }

    /** The edge an ace's first entry crosses: the top, the side away from the leader, or the bottom (the rear). */
    public enum Edge {
        TOP,
        SIDE,
        BOTTOM
    }

    /**
     * An ace ({@code part} of the piece) and its call sign (the bar's tag, at most 7 characters).
     *
     * @param station where and how fast it flies ({@link Station})
     */
    public record Ace(int part, String callSign, Role role, Join join, Station station) {
        public Ace {
            if (part < 0 || callSign.isEmpty() || callSign.length() > 7) {
                throw new IllegalArgumentException("an ace has a part and a call sign of 1–7 characters: " + callSign);
            }
        }
    }

    /**
     * When an ace's tell starts (its entry follows {@link SquadronSpec#tellSeconds} later). The first ace's tell starts
     * {@code afterSeconds} after the boss's arrival (the sting); a later ace's once the ace before it is told and
     * either {@code afterSeconds} have passed since that ace crossed its edge, that ace is down to {@code atShare} of
     * its HP (0: no share), or ({@code onFirstDeath}) an ace has died, whichever comes first.
     */
    public record Join(Edge from, double afterSeconds, double atShare, boolean onFirstDeath) {
        public Join {
            if (!(afterSeconds >= 0) || !(atShare >= 0 && atShare < 1)) {
                throw new IllegalArgumentException(
                        "a join has a time and a share below 1: " + afterSeconds + ", " + atShare);
            }
        }
    }

    /**
     * Where and how fast an ace flies; the meaning of a few fields depends on its {@link Role}.
     *
     * @param yMin its band's floor, px up (the leader 370, the dancer 210, the feinter 20)
     * @param yMax its band's ceiling, px up (470, 390, 120)
     * @param x the leader: its strafe's centre, px from the left (it slides to the mirrored x); the dancer: its
     *     eight's centre's distance from the field's centre, on the side away from the leader; the feinter: its side
     *     lane's centre from the edge (30: the outer 60 px)
     * @param speed px/s on its station: the leader's strafe (140), the dancer's loops (200), the feinter's drift
     * @param dashSpeed px/s: the leader's slide (280), the dancer's roll and ribbon pass, the feinter's dive (320);
     *     every first entry
     * @param climbSpeed px/s: the feinter's rake and overhead climb (260); the others' unused
     * @param spanMin the leader's strafe (40), the dancer's eight across (160)
     * @param spanMax the leader's strafe (80), the dancer's eight across (220)
     * @param keepOff the least distance from the ship's centre, px: the dancer's 120, the feinter's pull-out (110;
     *     easy 130, hard 90); 0 for none (the leader)
     * @param committedKeepOff the feinter's pull-out once an ace has died (85); the others' as {@code keepOff}
     * @param loopSeconds the dancer's full loop (2.5 s); the others' unused
     */
    public record Station(
            double yMin,
            double yMax,
            double x,
            double speed,
            double dashSpeed,
            double climbSpeed,
            double spanMin,
            double spanMax,
            double keepOff,
            double committedKeepOff,
            double loopSeconds) {
        public Station {
            if (!(yMax >= yMin)
                    || !(speed > 0)
                    || !(dashSpeed > 0)
                    || !(climbSpeed > 0)
                    || !(spanMax >= spanMin)
                    || !(spanMin >= 0)
                    || !(keepOff >= 0)
                    || !(committedKeepOff >= 0)
                    || !(loopSeconds >= 0)) {
                throw new IllegalArgumentException("a station has a band, speeds, a span and keep-offs");
            }
        }
    }

    /**
     * Touching the ship: {@code damage} to it (a collision, at most once per {@code intervalSeconds} per ace), the ship
     * pushed {@code push} px away from the ace, and {@code self} to the ace (its shield first).
     */
    public record Contact(double damage, double self, double push, double intervalSeconds) {
        public Contact {
            if (!(damage >= 0) || !(self >= 0) || !(push >= 0) || !(intervalSeconds > 0)) {
                throw new IllegalArgumentException("a contact has damages, a push and an interval");
            }
        }
    }

    public SquadronSpec {
        aces = List.copyOf(aces);
        if (aces.isEmpty() || !(tellSeconds >= 0) || !(separation >= 0)) {
            throw new IllegalArgumentException("a squadron has aces, a tell and a separation");
        }
        for (int a = 0; a < aces.size(); a++) {
            for (int b = 0; b < a; b++) {
                if (aces.get(a).part() == aces.get(b).part()) {
                    throw new IllegalArgumentException(
                            "two aces on part " + aces.get(a).part());
                }
            }
        }
    }

    /**
     * Checks it against its boss's {@code parts} and {@code attacks}: every part an ace, none vital (the last death ends
     * the piece); the playbook's pools and attacks.
     */
    void check(String barName, List<PartSpec> parts, List<BossSpec.Attack> attacks) {
        playbook.ifPresent(book -> book.check(barName, aces.size(), attacks));
        if (aces.size() != parts.size()) {
            throw new IllegalArgumentException(barName + ": every part of a squadron is an ace");
        }
        for (Ace ace : aces) {
            if (ace.part() >= parts.size() || parts.get(ace.part()).vital()) {
                throw new IllegalArgumentException(barName + ": an ace is a part, not vital: " + ace.callSign());
            }
        }
    }
}
