package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.content.SquadronBlock.AceData;
import vanguard.content.SquadronBlock.JoinChange;
import vanguard.content.SquadronBlock.PlayEntry;
import vanguard.content.SquadronBlock.PoolEntry;
import vanguard.content.SquadronBlock.SquadronChange;
import vanguard.content.SquadronBlock.SquadronData;
import vanguard.content.SquadronBlock.StationData;
import vanguard.content.UnitRules.InLevel;
import vanguard.sim.EnemySpec;
import vanguard.sim.PlayField;
import vanguard.sim.SquadronPlaybook;
import vanguard.sim.SquadronSpec;

/**
 * M10 part F (design/enemies/bosses/honour-guard; m10f-gaps.md §1 rows 1–7): a squadron mid-boss's keys ({@link
 * SquadronBlock}) for the simulation at a difficulty: its aces with their joins (the difficulty's {@code joins}) and
 * stations (heights turned px up, the feinter's {@code pull_out}), the hex shield by the HP lever, the contact by its
 * tier, and the playbook (the rests × the difficulty's {@code rests}, the sniper line's {@code taps}, the dives' {@code
 * dive_tell}).
 */
final class SquadronRules {
    private SquadronRules() {}

    /** {@code enemy}'s squadron on {@code difficulty}; empty if it has none. */
    static Optional<SquadronSpec> squadron(Content content, EnemyData enemy, Difficulty difficulty, InLevel inLevel) {
        Optional<SquadronData> data = enemy.squadron();
        if (data.isEmpty()) {
            return Optional.empty();
        }
        SquadronData squadron = data.get();
        Optional<SquadronChange> change = enemy.squadronChange(difficulty);
        List<SquadronSpec.Ace> aces = new ArrayList<>();
        for (AceData ace : squadron.aces()) {
            Optional<JoinChange> join = change.flatMap(SquadronChange::joins).map(joins -> joins.get(ace.part()));
            double pullOut = ace.role().equals("feinter")
                    ? change.flatMap(SquadronChange::pullOut)
                            .orElse(ace.station().keepOff().orElse(0.0))
                    : ace.station().keepOff().orElse(0.0);
            aces.add(new SquadronSpec.Ace(
                    RingRules.part(enemy, ace.part()),
                    ace.callSign(),
                    SquadronSpec.Role.values()[SquadronBlock.ROLES.indexOf(ace.role())],
                    new SquadronSpec.Join(
                            SquadronSpec.Edge.values()[
                                    SquadronBlock.EDGES.indexOf(ace.join().from())],
                            join.flatMap(JoinChange::after).orElse(ace.join().after()),
                            join.flatMap(JoinChange::atShare)
                                    .or(() -> ace.join().atShare())
                                    .orElse(0.0),
                            ace.join().onFirstDeath().orElse(false)),
                    station(ace.station(), pullOut)));
        }
        EnemyBasisData.Shields shared = content.enemyBasis().shields();
        EnemySpec.Shield shield = new EnemySpec.Shield(
                UnitRules.enemyHp(
                        content, enemy, squadron.shield() * UnitRules.actHpFactor(content, enemy, inLevel), difficulty),
                shared.delay(),
                shared.refill(),
                shared.breakerHold());
        SquadronSpec spec = new SquadronSpec(
                aces,
                squadron.tell(),
                squadron.separation(),
                SimSpecs.hitbox(squadron.hull()),
                shield,
                new SquadronSpec.Contact(
                        content.enemyBasis()
                                .contactDamage()
                                .get(squadron.contact().damage()),
                        squadron.contact().self(),
                        squadron.contact().push(),
                        squadron.contact().interval()));
        if (squadron.playbook().isEmpty()) {
            return Optional.of(spec);
        }
        return Optional.of(spec.withPlaybook(playbook(enemy, squadron, change)));
    }

    /** A station in the simulation's px up (the design's band is px below the top edge). */
    private static SquadronSpec.Station station(StationData station, double keepOff) {
        Span span = station.span().orElse(new Span(0, 0));
        return new SquadronSpec.Station(
                PlayField.HEIGHT - station.y().max(),
                PlayField.HEIGHT - station.y().min(),
                station.x(),
                station.speed(),
                station.dash(),
                station.climb().orElse(station.speed()),
                span.min(),
                span.max(),
                keepOff,
                station.committedKeepOff().orElse(keepOff),
                station.loop().orElse(0.0));
    }

    private static SquadronPlaybook playbook(EnemyData enemy, SquadronData squadron, Optional<SquadronChange> change) {
        SquadronBlock.PlaybookData book = squadron.playbook().orElseThrow();
        double rests = change.flatMap(SquadronChange::rests).orElse(1.0);
        List<SquadronPlaybook.Pool> pools = new ArrayList<>();
        for (AceData ace : squadron.aces()) {
            PoolEntry pool = book.pools().stream()
                    .filter(entry -> entry.ace().equals(ace.part()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": no pool for " + ace.part()));
            List<SquadronPlaybook.Play> plays = new ArrayList<>();
            for (PlayEntry entry : pool.plays()) {
                plays.add(play(enemy, entry, change));
            }
            int signature = pool.playIndex(pool.signature());
            if (signature < 0) {
                throw new IllegalArgumentException(
                        enemy.name() + ": " + ace.part() + "'s signature is no play of its pool: " + pool.signature());
            }
            pools.add(new SquadronPlaybook.Pool(plays, signature));
        }
        return new SquadronPlaybook(
                pools,
                book.tell(),
                book.rests().alone() * rests,
                book.rests().company() * rests,
                book.losses().rest(),
                book.losses().lastRest(),
                book.pincer()
                        .map(pincer -> new SquadronPlaybook.Pincer(
                                pincer.weight(),
                                RingRules.attack(enemy, pincer.attack()),
                                pincer.gap(),
                                pincer.every())),
                new SquadronPlaybook.Conductor(
                        book.conductor().told(), book.conductor().firing()));
    }

    private static SquadronPlaybook.Play play(EnemyData enemy, PlayEntry entry, Optional<SquadronChange> change) {
        SquadronPlaybook.Kind kind = entry.kind();
        boolean sniper = kind == SquadronPlaybook.Kind.SNIPER_LINE;
        boolean dive = kind == SquadronPlaybook.Kind.FEINT_DIVE || kind == SquadronPlaybook.Kind.OVERHEAD;
        int count = sniper
                ? change.flatMap(SquadronChange::taps).orElse(entry.count().orElse(0))
                : entry.count().orElse(0);
        int edge = sniper
                ? change.flatMap(SquadronChange::edgeTaps).orElse(entry.edge().orElse(0))
                : entry.edge().orElse(0);
        double tell = dive
                ? change.flatMap(SquadronChange::diveTell).or(entry::tell).orElse(Double.NaN)
                : entry.tell().orElse(Double.NaN);
        SquadronPlaybook.Play play = new SquadronPlaybook.Play(
                kind,
                entry.weight().orElse(1.0),
                entry.attack().map(name -> RingRules.attack(enemy, name)).orElse(-1),
                entry.follow().map(name -> RingRules.attack(enemy, name)).orElse(-1),
                count,
                edge,
                entry.gap().orElse(0.0),
                entry.seconds().orElse(0.0),
                tell,
                Optional.empty());
        return entry.trail()
                .map(trail -> play.withTrail(
                        new SquadronPlaybook.Trail(trail.count(), trail.speed(), trail.gaps(), trail.gap())))
                .orElse(play);
    }
}
