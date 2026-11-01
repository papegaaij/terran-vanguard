package vanguard.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import vanguard.content.SquadronBlock.AceData;
import vanguard.content.SquadronBlock.PlayEntry;
import vanguard.content.SquadronBlock.PoolEntry;
import vanguard.content.SquadronBlock.SquadronData;

/**
 * M10 part F's static checks (design/enemies/bosses/honour-guard, design/campaign Level 40): a squadron mid-boss's keys
 * ({@link SquadronBlock}) and a level's radio for it.
 *
 * <p>A unit's: only a boss has a {@code squadron}, engaging on its arrival; every part is an ace and no part is vital;
 * each ace's part named once; a pool per ace, its plays its role's, its signature among them; a firing play fires a
 * boss attack (the sniper line and its taps a {@code laser-line}, the spiral loop a {@code spiral}, the others none),
 * the slide none, a ribbon lays a trail; the pincer's attack a known one; the hook's {@code squadron} only on a
 * squadron, its joins naming aces; a line's {@code lock_before} only on a boss's attack.
 *
 * <p>A level's: a {@code boss-phase} radio line under a squadron names one of its subjects (an ace joining after the
 * first, {@code <call sign>-joins}; its death, {@code <call sign>-down}; the first loss spoken by it, {@code
 * first-loss-<call sign>}; the last ace's {@code last-ace-<call sign>}; lower case: the simulation's).
 */
final class SquadronChecks {
    private SquadronChecks() {}

    /** A unit's M10 part F keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        boolean boss = enemy.boss().isPresent();
        for (int i = 0; i < enemy.attacks().size(); i++) {
            if (enemy.attacks()
                            .get(i)
                            .line()
                            .flatMap(EnemyData.Line::lockBefore)
                            .isPresent()
                    && !boss) {
                problem.accept("attacks[" + i + "].line.lock_before", "only a boss's line tracks before it locks");
            }
        }
        Optional<SquadronData> data = enemy.squadron();
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            Optional<SquadronBlock.SquadronChange> change = enemy.squadronChange(difficulty);
            if (change.isEmpty()) {
                continue;
            }
            if (data.isEmpty()) {
                problem.accept("difficulty.squadron", "only a squadron (boss.squadron) has squadron changes");
                continue;
            }
            change.get().joins().ifPresent(joins -> joins.keySet().forEach(part -> {
                if (data.get().aceOn(part) < 0) {
                    problem.accept("difficulty.squadron.joins", "no ace on part '" + part + "'");
                }
            }));
        }
        if (data.isEmpty()) {
            return;
        }
        SquadronData squadron = data.get();
        EnemyData.BossData script = enemy.boss().orElseThrow();
        if (!script.engagesOnArrival().orElse(false)) {
            problem.accept("boss.engages_on_arrival", "a squadron engages on its arrival (its aces join on its clock)");
        }
        List<EnemyData.PartData> parts = enemy.partList().orElse(List.of());
        if (parts.size() != squadron.aces().size()) {
            problem.accept(
                    "boss.squadron.aces",
                    "every part is an ace: " + parts.size() + " parts, "
                            + squadron.aces().size() + " aces");
        }
        Set<String> seen = new HashSet<>();
        Set<String> signs = new HashSet<>();
        for (int a = 0; a < squadron.aces().size(); a++) {
            AceData ace = squadron.aces().get(a);
            String field = "boss.squadron.aces[" + a + "]";
            int part = enemy.partIndex(ace.part());
            if (part < 0) {
                problem.accept(field + ".part", "no part '" + ace.part() + "'");
            } else if (parts.get(part).kind().equals("vital")) {
                problem.accept(field + ".part", "an ace is not vital: the last ace's death ends the piece");
            }
            if (!seen.add(ace.part())) {
                problem.accept(field + ".part", "two aces on part '" + ace.part() + "'");
            }
            if (!signs.add(ace.callSign())) {
                problem.accept(field + ".call_sign", "two aces called '" + ace.callSign() + "'");
            }
        }
        squadron.playbook().ifPresent(book -> {
            for (AceData ace : squadron.aces()) {
                if (book.pools().stream().noneMatch(pool -> pool.ace().equals(ace.part()))) {
                    problem.accept("boss.squadron.playbook.pools", "no pool for '" + ace.part() + "'");
                }
            }
            for (int p = 0; p < book.pools().size(); p++) {
                pool(enemy, squadron, book.pools().get(p), "boss.squadron.playbook.pools[" + p + "]", problem);
            }
            book.pincer().ifPresent(pincer -> {
                Optional<EnemyData.Attack> attack = enemy.attack(pincer.attack());
                if (attack.isEmpty()) {
                    problem.accept("boss.squadron.playbook.pincer.attack", "no attack '" + pincer.attack() + "'");
                } else if (attack.get().pattern().equals(EnemyData.Attack.LASER_LINE)) {
                    problem.accept("boss.squadron.playbook.pincer.attack", "the pincer fires guns, not a line");
                }
            });
        });
    }

    private static void pool(
            EnemyData enemy, SquadronData squadron, PoolEntry pool, String field, BiConsumer<String, String> problem) {
        int a = squadron.aceOn(pool.ace());
        if (a < 0) {
            problem.accept(field + ".ace", "no ace on part '" + pool.ace() + "'");
            return;
        }
        String role = squadron.aces().get(a).role();
        if (pool.playIndex(pool.signature()) < 0) {
            problem.accept(field + ".signature", "not a play of its pool: " + pool.signature());
        }
        List<String> plays = new ArrayList<>();
        for (int i = 0; i < pool.plays().size(); i++) {
            PlayEntry play = pool.plays().get(i);
            String at = field + ".plays[" + i + "]";
            if (plays.contains(play.play())) {
                problem.accept(at, "a play once a pool: " + play.play());
            }
            plays.add(play.play());
            if (!SquadronBlock.ROLE_PLAYS.get(role).contains(play.play())) {
                problem.accept(
                        at, "a " + role + " flies " + SquadronBlock.ROLE_PLAYS.get(role) + ", not " + play.play());
            }
            boolean fires = play.kind().fires();
            if (fires != play.attack().isPresent()) {
                problem.accept(at + ".attack", fires ? "a firing play names its attack" : "the slide fires nothing");
            }
            String pattern =
                    switch (play.play()) {
                        case "sniper-line" -> EnemyData.Attack.LASER_LINE;
                        case "spiral-loop" -> "spiral";
                        default -> null;
                    };
            play.attack().ifPresent(name -> attack(enemy, name, pattern, at + ".attack", problem));
            if (play.play().equals("sniper-line")
                    && (play.count().orElse(0) > 0 || play.edge().orElse(0) > 0)) {
                if (play.follow().isEmpty()) {
                    problem.accept(at + ".follow", "the sniper line's taps fire a follow line");
                } else {
                    attack(enemy, play.follow().get(), EnemyData.Attack.LASER_LINE, at + ".follow", problem);
                }
            }
            if (play.play().equals("ribbon") && play.trail().isEmpty()) {
                problem.accept(at + ".trail", "a ribbon lays a trail");
            }
        }
    }

    private static void attack(
            EnemyData enemy, String name, String pattern, String field, BiConsumer<String, String> problem) {
        Optional<EnemyData.Attack> attack = enemy.attack(name);
        if (attack.isEmpty()) {
            problem.accept(field, "no attack '" + name + "'");
            return;
        }
        String own = attack.get().pattern();
        if (pattern != null ? !own.equals(pattern) : own.equals(EnemyData.Attack.LASER_LINE)) {
            problem.accept(
                    field,
                    pattern != null ? "fires a " + pattern + ", not a " + own : "fires guns, not a line: " + name);
        }
    }

    /** A level's radio under its squadron boss. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<SquadronData> squadron = level.boss()
                .filter(placement -> content.enemies().containsKey(placement.enemy()))
                .flatMap(placement -> content.enemy(placement.enemy()).squadron());
        if (squadron.isEmpty()) {
            return;
        }
        Set<String> subjects = subjects(squadron.get());
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            if (cue.event().orElse(null) == LevelData.CueEvent.BOSS_PHASE
                    && cue.phase().isPresent()
                    && !subjects.contains(cue.phase().get())) {
                problem.accept(
                        "radio[" + i + "].phase",
                        "not a subject of the squadron: " + cue.phase().get() + " (" + subjects + ")");
            }
        }
    }

    /** The squadron's radio subjects (the simulation's {@code Squadron}). */
    static Set<String> subjects(SquadronData squadron) {
        Set<String> subjects = new java.util.TreeSet<>();
        for (int a = 0; a < squadron.aces().size(); a++) {
            String sign = squadron.aces().get(a).callSign().toLowerCase(Locale.ROOT);
            if (a > 0) {
                subjects.add(sign + "-joins");
            }
            subjects.add(sign + "-down");
            subjects.add("first-loss-" + sign);
            subjects.add("last-ace-" + sign);
        }
        return subjects;
    }
}
