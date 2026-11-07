package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import vanguard.sim.EscapeRun;

/**
 * M10 part H, step H2 (design/enemies/bosses/ascendant, design/campaign Level 42; m10h-gaps.md §1 rows 1–13 and 18): a
 * bow boss's keys ({@link BowBlock}) name its parts, phases and attacks; a line move fires a {@code laser-line}, a
 * salvo a homing attack, the batteries' attack is fired by parts behind a panel; an escaping boss is an act boss whose
 * hull breaks on a part of its last phase, its lifeboat in no phase. In a level: its {@code boss-phase} subjects, the
 * escape's {@code boss-escaped} in place of {@code boss-destroyed}, the music's boss track changes on its subjects and
 * its end sting only with an escape; and, campaign-wide, Vorne's shout (design/audio/voice: no {@code shout: true} on
 * his cues but the one at an escaping boss's untouchable opening: Level 42's line 5).
 */
final class BowChecks {
    /** The speaker whose shout the voice check rejects (design/audio/voice, Vorne). */
    static final String VORNE = "Vorne";

    private BowChecks() {}

    /** A bow boss's keys in its stat block. */
    static void enemy(Content content, EnemyData enemy, BiConsumer<String, String> problem) {
        Optional<EnemyData.BossData> script = enemy.boss();
        if (script.isEmpty()) {
            if (hook(enemy).isPresent()) {
                problem.accept("difficulty.bow", "only a boss has bow changes");
            }
            return;
        }
        EnemyData.BossData boss = script.get();
        boolean bow = boss.wedge().isPresent()
                || boss.fedShield().isPresent()
                || boss.mirrors().isPresent()
                || boss.playbooks().isPresent()
                || boss.cues().isPresent()
                || boss.regeneration().isPresent()
                || boss.escape().isPresent()
                || boss.rearSweep().isPresent()
                || boss.lastWords().isPresent();
        if (!bow) {
            if (hook(enemy).isPresent()) {
                problem.accept(
                        "difficulty.bow", "only a boss with bow keys (playbooks, regeneration, escape) has them");
            }
            return;
        }
        List<String> phases =
                boss.phases().stream().map(EnemyData.PhaseData::name).toList();
        if (boss.wedge().isPresent() && enemy.movement().hover().isEmpty()) {
            problem.accept("boss.wedge", "a wedge body hangs at its hover height (movement.hover)");
        }
        boss.fedShield().ifPresent(fed -> {
            part(enemy, "boss.fed_shield.part", fed.part(), problem);
            fed.fedBy().forEach(name -> part(enemy, "boss.fed_shield.fed_by", name, problem));
        });
        List<String> mirrored = new ArrayList<>();
        boss.mirrors()
                .ifPresent(places -> places.forEach(place -> {
                    part(enemy, "boss.mirrors", place.part(), problem);
                    if (mirrored.contains(place.part())) {
                        problem.accept("boss.mirrors", "one panel a part: " + place.part());
                    }
                    mirrored.add(place.part());
                }));
        boss.batteryFire().ifPresent(fire -> {
            if (enemy.attack(fire.attack()).isEmpty()) {
                problem.accept("boss.battery_fire.attack", "no attack named '" + fire.attack() + "'");
                return;
            }
            List<Integer> firing = BowRules.firing(enemy, fire.attack());
            if (firing.isEmpty()) {
                problem.accept("boss.battery_fire.attack", "no part fires '" + fire.attack() + "'");
            }
            for (int p : firing) {
                String name = enemy.partList().orElseThrow().get(p).name();
                if (!mirrored.contains(name)) {
                    problem.accept("boss.battery_fire", "a battery stands behind a panel (mirrors): " + name);
                }
            }
        });
        List<EnemyData.BowPlaybook> books = boss.playbooks().orElse(List.of());
        Set<String> booked = new TreeSet<>();
        for (int b = 0; b < books.size(); b++) {
            EnemyData.BowPlaybook book = books.get(b);
            String field = "boss.playbooks[" + b + "]";
            phase(phases, field + ".phase", book.phase(), problem);
            if (!booked.add(book.phase())) {
                problem.accept(field + ".phase", "one playbook a phase: " + book.phase());
            }
            book.sources().forEach(name -> part(enemy, field + ".sources", name, problem));
            book.lowParts().ifPresent(list -> list.forEach(name -> part(enemy, field + ".low_parts", name, problem)));
            book.fallback().ifPresent(list -> list.forEach(name -> part(enemy, field + ".fallback", name, problem)));
            for (int m = 0; m < book.moves().size(); m++) {
                move(content, enemy, field + ".moves[" + m + "]", book.moves().get(m), mirrored, problem);
            }
        }
        List<EnemyData.CueData> cues = boss.cues().orElse(List.of());
        Set<String> subjects = new TreeSet<>(phases);
        for (int c = 0; c < cues.size(); c++) {
            EnemyData.CueData cue = cues.get(c);
            String field = "boss.cues[" + c + "]";
            phase(phases, field + ".phase", cue.phase(), problem);
            cue.parts().ifPresent(list -> list.forEach(name -> part(enemy, field + ".parts", name, problem)));
            if (cue.deaths().orElse(0) > cue.parts().map(List::size).orElse(0)) {
                problem.accept(field + ".deaths", "at most its parts");
            }
            if (!subjects.add(cue.subject())) {
                problem.accept(field + ".subject", "a subject once (not a phase's name): " + cue.subject());
            }
        }
        boss.regeneration()
                .ifPresent(list -> list.forEach(regen -> part(enemy, "boss.regeneration", regen.part(), problem)));
        partF(enemy, boss, phases, books, problem);
        for (int f = 0; f < boss.phases().size(); f++) {
            EnemyData.PhaseData phase = boss.phases().get(f);
            if (phase.untouchable().isPresent() && f == 0) {
                problem.accept("boss.phases[0].untouchable", "an opening opens a later phase");
            }
        }
        boss.escape().ifPresent(escape -> escape(enemy, boss, escape, problem));
    }

    private static void move(
            Content content,
            EnemyData enemy,
            String field,
            EnemyData.BowMove play,
            List<String> batteries,
            BiConsumer<String, String> problem) {
        play.racks().ifPresent(list -> list.forEach(name -> part(enemy, field + ".racks", name, problem)));
        boolean racked = play.move().equals("salvo")
                || play.move().equals("mounts")
                || play.move().equals("barrage");
        if (racked != play.racks().isPresent() && !play.move().equals("brood")) {
            problem.accept(
                    field + ".racks",
                    "a salvo, the grafted guns (mounts) and a barrage, and only they, fire from racks");
        }
        if (play.move().equals("barrage")) {
            // M10 round 80 fixes, R80a: a barrage cues the batteries' own bursts (the parts behind panels).
            play.racks().ifPresent(list -> list.stream()
                    .filter(name -> !batteries.contains(name))
                    .forEach(name -> problem.accept(field + ".racks", "a barrage cues batteries: " + name)));
        }
        play.with().ifPresent(name -> {
            Optional<EnemyData.Attack> with = enemy.attack(name);
            if (with.isEmpty() || !with.get().pattern().equals("fan")) {
                problem.accept(field + ".with", "a twin's volley is a fan attack: " + name);
            }
        });
        play.cover().ifPresent(name -> {
            Optional<EnemyData.Attack> cover = enemy.attack(name);
            if (cover.isEmpty() || !cover.get().pattern().equals("fan")) {
                problem.accept(field + ".cover", "a lance's cover is a fan attack: " + name);
            }
        });
        play.bays().ifPresent(bays -> {
            if (!content.enemies().containsKey(bays.from())) {
                problem.accept(field + ".bays.from", "no enemy '" + bays.from() + "'");
            }
        });
        play.attack().ifPresent(name -> {
            Optional<EnemyData.Attack> attack = enemy.attack(name);
            if (attack.isEmpty()) {
                problem.accept(field + ".attack", "no attack named '" + name + "'");
                return;
            }
            String pattern = attack.get().pattern();
            String wanted =
                    switch (play.move()) {
                        case "line", "sweep", "twin" -> EnemyData.Attack.LASER_LINE;
                        case "spiral" -> "spiral";
                        case "lobs" -> "mortar";
                        case "volley", "mounts" -> "fan";
                        default -> "";
                    };
            if (!wanted.isEmpty() && !pattern.equals(wanted)) {
                problem.accept(field + ".attack", "a " + play.move() + " fires a " + wanted + ", not a " + pattern);
            }
            if (play.move().equals("salvo") && attack.get().from().isEmpty()) {
                problem.accept(field + ".attack", "a salvo fires a homing unit's missiles (from)");
            }
        });
    }

    /**
     * M11 part F, step F2 (Vorne's Chimera): a rear-sweep move under the boss's {@code rear_sweep} (and only there), its
     * climbing fire one of the boss's attacks; the part shields' and Rook's orders' parts and phases.
     */
    private static void partF(
            EnemyData enemy,
            EnemyData.BossData boss,
            List<String> phases,
            List<EnemyData.BowPlaybook> books,
            BiConsumer<String, String> problem) {
        boolean sweeps = books.stream().anyMatch(book -> book.moves().stream()
                .anyMatch(move -> move.move().equals(BowBlock.REAR_SWEEP)));
        if (sweeps != boss.rearSweep().isPresent()) {
            problem.accept("boss.rear_sweep", "a boss with a rear-sweep move has its rear_sweep, and only it");
        }
        boss.rearSweep().flatMap(EnemyData.RearSweepData::fire).ifPresent(fire -> {
            if (enemy.attack(fire.attack()).isEmpty()) {
                problem.accept("boss.rear_sweep.fire.attack", "no attack named '" + fire.attack() + "'");
            }
        });
        List<EnemyData.PartShieldData> shields = boss.partShields().orElse(List.of());
        Set<String> shielded = new TreeSet<>();
        boss.fedShield().ifPresent(fed -> shielded.add(fed.part()));
        for (EnemyData.PartShieldData shield : shields) {
            part(enemy, "boss.part_shields", shield.part(), problem);
            if (!shielded.add(shield.part())) {
                problem.accept("boss.part_shields", "one shield a part: " + shield.part());
            }
        }
        Set<String> ordered = new TreeSet<>();
        for (EnemyData.RookOrderData order : boss.rook().orElse(List.of())) {
            phase(phases, "boss.rook.phase", order.phase(), problem);
            order.order().forEach(name -> part(enemy, "boss.rook.order", name, problem));
            if (!ordered.add(order.phase())) {
                problem.accept("boss.rook", "one order a phase: " + order.phase());
            }
        }
        if (boss.lastWords().isPresent() && !boss.midBoss()) {
            problem.accept("boss.last_words", "a mid-boss dies on its last words (Vorne's Chimera)");
        }
    }

    private static void escape(
            EnemyData enemy, EnemyData.BossData boss, EnemyData.EscapeData escape, BiConsumer<String, String> problem) {
        part(enemy, "boss.escape.part", escape.part(), problem);
        part(enemy, "boss.escape.breaks_on", escape.breaksOn(), problem);
        if (boss.midBoss() || boss.held()) {
            problem.accept("boss.escape", "only an act boss escapes");
        }
        List<EnemyData.PhaseData> phases = boss.phases();
        for (int f = 0; f < phases.size(); f++) {
            EnemyData.PhaseData phase = phases.get(f);
            List<String> named = new ArrayList<>(phase.until().parts().orElse(List.of()));
            named.addAll(phase.exposes().orElse(List.of()));
            if (named.contains(escape.part())) {
                problem.accept("boss.phases[" + f + "]", "the lifeboat is in no phase (it climbs after the last)");
            }
        }
        List<String> last = phases.getLast().until().parts().orElse(List.of());
        if (!last.contains(escape.breaksOn())) {
            problem.accept("boss.escape.breaks_on", "the hull breaks on a part of its last phase");
        }
        enemy.partList()
                .flatMap(list -> list.stream()
                        .filter(part -> part.name().equals(escape.part()))
                        .findFirst())
                .filter(part -> !part.armoured())
                .ifPresent(part -> problem.accept(
                        "boss.escape.part", "the lifeboat is armoured (round 80: scenery, never hit, no pay)"));
    }

    /** A level's radio and music under its bow boss, and every level's Vorne shouts. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        Optional<EnemyData> boss = level.boss()
                .filter(placement -> content.enemies().containsKey(placement.enemy()))
                .map(placement -> content.enemy(placement.enemy()));
        boolean escapes = boss.flatMap(EnemyData::escape).isPresent();
        // M11 part F, step F2: Vorne's Chimera dies on his last words: its fusion's pain takes his one Act 7 shout.
        boolean lastWords = boss.flatMap(EnemyData::boss)
                .flatMap(EnemyData.BossData::lastWords)
                .isPresent();
        Set<String> eruptions = new TreeSet<>();
        boss.flatMap(EnemyData::boss).ifPresent(script -> script.phases().stream()
                .filter(phase -> phase.untouchable().isPresent())
                .forEach(phase -> eruptions.add(phase.name())));
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            LevelData.CueEvent event = cue.event().orElse(null);
            if (escapes && event == LevelData.CueEvent.BOSS_DESTROYED) {
                problem.accept("radio[" + i + "].event", "the boss escapes: its fight ends at boss-escaped");
            }
            if (!escapes && event == LevelData.CueEvent.BOSS_ESCAPED) {
                problem.accept("radio[" + i + "].event", "boss-escaped: only under an escaping boss");
            }
            if (cue.speaker().equals(VORNE) && cue.shout().orElse(false)) {
                boolean opening = (escapes || lastWords)
                        && event == LevelData.CueEvent.BOSS_PHASE
                        && cue.phase().filter(eruptions::contains).isPresent();
                if (!opening) {
                    problem.accept(
                            "radio[" + i + "].shout",
                            "Vorne never shouts (design/audio/voice) but at a phase opening of his own boss (Levels 42"
                                    + " and 48)");
                }
            }
        }
        if (boss.isEmpty()) {
            if (level.music().bossTrackChanges().isPresent()
                    || level.music().bossEndSting().isPresent()) {
                problem.accept("music", "boss_track_changes and boss_end_sting need a boss");
            }
            return;
        }
        Set<String> subjects = subjects(boss.get());
        if (escapes || boss.get().boss().flatMap(EnemyData.BossData::cues).isPresent()) {
            for (int i = 0; i < level.radio().size(); i++) {
                LevelData.RadioCue cue = level.radio().get(i);
                if (cue.event().orElse(null) == LevelData.CueEvent.BOSS_PHASE
                        && cue.phase().isPresent()
                        && !subjects.contains(cue.phase().get())) {
                    problem.accept(
                            "radio[" + i + "].phase",
                            "not a subject of the boss: " + cue.phase().get() + " (" + subjects + ")");
                }
            }
        }
        List<LevelData.Music.BossTrackChange> changes =
                level.music().bossTrackChanges().orElse(List.of());
        for (int i = 0; i < changes.size(); i++) {
            if (!subjects.contains(changes.get(i).on())) {
                problem.accept(
                        "music.boss_track_changes[" + i + "].on",
                        "not a subject of the boss: " + changes.get(i).on() + " (" + subjects + ")");
            }
        }
        if (level.music().bossEndSting().isPresent() && !escapes) {
            problem.accept("music.boss_end_sting", "only under an escaping boss (the sting at ESCAPED)");
        }
    }

    /** A bow boss's {@code boss-phase} subjects: its phases' names, its cues' and its escape's. */
    static Set<String> subjects(EnemyData enemy) {
        Set<String> subjects = new TreeSet<>();
        enemy.boss().ifPresent(script -> {
            script.phases().forEach(phase -> subjects.add(phase.name()));
            script.cues().orElse(List.of()).forEach(cue -> subjects.add(cue.subject()));
            if (script.lastWords().isPresent()) {
                // M11 part F, step F2: K, Vorne's last line (LastWords' boss-phase subject).
                subjects.add(vanguard.sim.LastWords.SUBJECT);
            }
            if (script.escape().isPresent()) {
                subjects.add(EscapeRun.HULL_BREAKS);
                subjects.add(EscapeRun.LIFEBOAT_AWAY);
            }
        });
        return subjects;
    }

    private static Optional<BowBlock.BowChange> hook(EnemyData enemy) {
        return enemy.bowChange(Difficulty.EASY).or(() -> enemy.bowChange(Difficulty.HARD));
    }

    private static void part(EnemyData enemy, String field, String name, BiConsumer<String, String> problem) {
        if (enemy.partIndex(name) < 0) {
            problem.accept(field, "no part '" + name + "'");
        }
    }

    private static void phase(List<String> phases, String field, String name, BiConsumer<String, String> problem) {
        if (!phases.contains(name)) {
            problem.accept(field, "no boss phase '" + name + "' " + phases);
        }
    }
}
