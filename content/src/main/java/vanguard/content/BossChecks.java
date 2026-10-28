package vanguard.content;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import vanguard.sim.PlayField;

/**
 * A boss's and a set piece's checks: chains, phases, poses and parts resolve, an arena fits its boss, debris and set
 * pieces fit their level.
 */
final class BossChecks extends ContentChecks {
    BossChecks(Content content, Problems problems) {
        super(content, problems);
    }

    /**
     * A boss's chains end on its parts, its phases name its parts and attacks, its streams known
     * enemies, an alternating spiral has a duration; a difficulty's attack changes name its attacks
     * and its spawn counts its spawns; its poses place its parts (part G).
     */
    void checkBoss(EnemyData enemy) {
        List<EnemyData.ChainData> chains = enemy.chains().orElse(List.of());
        for (int i = 0; i < chains.size(); i++) {
            checkPartName(enemy, "chains[" + i + "].to", chains.get(i).to());
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            hook.flatMap(EnemyData.Hook::attacks)
                    .ifPresent(changes -> changes.keySet().forEach(name -> {
                        if (enemy.attack(name).isEmpty()) {
                            problem(enemy, "difficulty.attacks", "no attack named '" + name + "'");
                        }
                    }));
        }
        if (enemy.boss().isEmpty()) {
            return;
        }
        if (enemy.partList().isEmpty()) {
            problem(enemy, "boss", "a boss is a multi-part unit (part_list)");
        }
        List<EnemyData.PhaseData> phases = enemy.boss().get().phases();
        for (int i = 0; i < phases.size(); i++) {
            EnemyData.PhaseData phase = phases.get(i);
            String field = "boss.phases[" + i + "]";
            phase.until()
                    .parts()
                    .ifPresent(parts -> parts.forEach(part -> checkPartName(enemy, field + ".until.parts", part)));
            phase.exposes().ifPresent(parts -> parts.forEach(part -> checkPartName(enemy, field + ".exposes", part)));
            phase.attacks()
                    .or(phase::alternate)
                    .ifPresent(names -> names.forEach(name -> {
                        if (enemy.attack(name).isEmpty()) {
                            problem(enemy, field + ".attacks", "no attack named '" + name + "'");
                        }
                    }));
            phase.alternate()
                    .ifPresent(names -> names.forEach(name -> enemy.attack(name)
                            .filter(attack -> attack.pattern().equals("spiral")
                                    && attack.duration().isEmpty())
                            .ifPresent(attack -> problem(
                                    enemy,
                                    field + ".alternate",
                                    "spiral '" + name + "' alternates without a duration"))));
            phase.streams().ifPresent(stream -> checkEnemyName(enemy, field + ".streams.enemy", stream.enemy()));
            checkPhasePoseAndWindows(enemy, field, phase);
        }
        EnemyData.BossData boss = enemy.boss().get();
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            hook.flatMap(EnemyData.Hook::spawns)
                    .ifPresent(counts -> counts.keySet().forEach(name -> {
                        boolean known = boss.phases().stream()
                                .flatMap(f -> f.windows().flatMap(EnemyData.WindowData::spawns).stream())
                                .flatMap(List::stream)
                                .anyMatch(spawn -> spawn.name().equals(name));
                        if (!known) {
                            problem(enemy, "difficulty.spawns", "no spawn named '" + name + "'");
                        }
                    }));
        }
        boss.poses().ifPresent(poses -> {
            for (int i = 0; i < poses.size(); i++) {
                for (String part : poses.get(i).offsets().keySet()) {
                    checkPartName(enemy, "boss.poses[" + i + "].offsets", part);
                }
            }
        });
    }

    /**
     * Part G: a phase's move turns into a pose of the boss, and its windows hold its parts and
     * release known enemies.
     */
    private void checkPhasePoseAndWindows(EnemyData enemy, String field, EnemyData.PhaseData phase) {
        EnemyData.BossData boss = enemy.boss().orElseThrow();
        phase.move().flatMap(EnemyData.MoveData::pose).ifPresent(pose -> {
            if (boss.poseIndex(pose) < 0) {
                problem(enemy, field + ".move.pose", "no pose named '" + pose + "'");
            }
        });
        phase.windows().ifPresent(windows -> {
            windows.groups()
                    .forEach(group -> group.forEach(part -> checkPartName(enemy, field + ".windows.groups", part)));
            windows.spawns()
                    .ifPresent(spawns -> spawns.forEach(
                            spawn -> checkEnemyName(enemy, field + ".windows.spawns.enemy", spawn.enemy())));
        });
    }

    /**
     * M5 part E, step E2c: an arena boss's keys (design/enemies/bosses/harbour-kraken). Only an
     * anchored boss (movement {@code anchored}, without a hover or a straight descent) has lanes, part
     * layers, weak spots, slam chains or phases that slam, surface, release or drop; its lanes have
     * arms that are slam chains' parts and each slam chain's part owns lanes; a phase that slams needs
     * lanes, one that ends on slams slams; a surfacing names a part, a release a known enemy that forms
     * fields and lanes of the boss, a drop a pickup.
     */
    void checkArenaBoss(EnemyData enemy) {
        boolean anchored = enemy.movement().anchored().isPresent();
        if (anchored
                && (enemy.boss().isEmpty()
                        || enemy.movement().hover().isPresent()
                        || enemy.movement().straight().isPresent())) {
            problem(enemy, "movement.anchored", "only a boss is anchored, without a hover or a straight descent");
        }
        List<EnemyData.PartData> parts = enemy.partList().orElse(List.of());
        for (int i = 0; i < parts.size(); i++) {
            EnemyData.PartData part = parts.get(i);
            // M7 part F: or a walking boss's (the Revenant Walker's vents).
            if (!enemy.arenaBoss() && (part.layer().isPresent() || part.spots().isPresent())) {
                problem(enemy, "part_list[" + i + "]", "only an arena boss's parts have a layer or spots");
            }
        }
        List<EnemyData.ChainData> chains = enemy.chains().orElse(List.of());
        Optional<EnemyData.Lanes> lanes = enemy.boss().flatMap(EnemyData.BossData::lanes);
        if (lanes.isPresent() && !anchored) {
            problem(enemy, "boss.lanes", "only an anchored boss has lanes");
        }
        java.util.Map<String, List<Integer>> arms =
                lanes.flatMap(EnemyData.Lanes::arms).orElse(java.util.Map.of());
        for (int i = 0; i < chains.size(); i++) {
            EnemyData.ChainData chain = chains.get(i);
            if (chain.slams() && !arms.containsKey(chain.to())) {
                problem(enemy, "chains[" + i + "].motion", "a slam arm's part owns lanes (boss.lanes.arms)");
            }
        }
        if (lanes.isPresent() && arms.isEmpty()) {
            problem(enemy, "boss.lanes.arms", "lanes are slammed by arms");
        }
        for (String part : arms.keySet()) {
            boolean slammer = chains.stream()
                    .anyMatch(chain -> chain.slams() && chain.to().equals(part));
            if (!slammer) {
                problem(enemy, "boss.lanes.arms", "'" + part + "' is no slam chain's part (motion: slam)");
            }
        }
        // Round 40: a rooted arm's root stays on the surface (always hittable).
        if (lanes.map(EnemyData.Lanes::isRooted).orElse(false)) {
            for (String part : arms.keySet()) {
                int index = enemy.partIndex(part);
                if (index >= 0
                        && !parts.get(index).layer().orElse(enemy.layer()).equals("ground")) {
                    problem(enemy, "boss.lanes.arms", "a rooted arm's root '" + part + "' is a ground part");
                }
            }
        }
        // Round 40: aim turns are a boss's (its parts take turns).
        for (EnemyData.Attack attack : enemy.attacks()) {
            if (attack.aimTurns().isPresent() && enemy.boss().isEmpty()) {
                problem(
                        enemy,
                        "attacks." + attack.name().orElse(attack.pattern()) + ".aim_turns",
                        "only a boss's parts take aim turns");
            }
        }
        if (enemy.boss().isEmpty()) {
            return;
        }
        List<EnemyData.PhaseData> phases = enemy.boss().get().phases();
        for (int i = 0; i < phases.size(); i++) {
            EnemyData.PhaseData phase = phases.get(i);
            String field = "boss.phases[" + i + "]";
            boolean arena = phase.slamming().isPresent()
                    || phase.surface().isPresent()
                    || phase.release().isPresent()
                    || phase.drop().isPresent()
                    || phase.until().slams().isPresent()
                    || phase.until().below().isPresent()
                    || phase.until().share().isPresent();
            if (arena && !enemy.arenaBoss()) {
                problem(enemy, field, "only an arena boss's phases slam, surface, release, drop or end on a share");
            }
            if ((phase.slamming().isPresent()
                            || phase.surface().isPresent()
                            || phase.release().isPresent())
                    && !anchored) {
                problem(enemy, field, "only an anchored boss's phases slam, surface or release");
            }
            if (phase.slamming().isPresent() && lanes.isEmpty()) {
                problem(enemy, field + ".slamming", "a phase slams only with the boss's lanes");
            }
            if (phase.until().slams().isPresent() && phase.slamming().isEmpty()) {
                problem(enemy, field + ".until.slams", "a phase ends on slams only when it slams");
            }
            if (phase.slamming()
                            .filter(slamming -> slamming.mode().equals("after_dive"))
                            .isPresent()
                    && phase.surface()
                            .filter(surface -> !surface.stay().orElse(false))
                            .isEmpty()) {
                problem(enemy, field + ".slamming", "after_dive needs a surfacing that dives");
            }
            phase.surface().ifPresent(surface -> checkPartName(enemy, field + ".surface.part", surface.part()));
            phase.release().ifPresent(release -> {
                checkEnemyName(enemy, field + ".release.enemy", release.enemy());
                if (content.enemies().containsKey(release.enemy())
                        && content.enemy(release.enemy()).movement().drift().isEmpty()) {
                    problem(
                            enemy,
                            field + ".release.enemy",
                            "a field's units drift: '" + release.enemy() + "' does not");
                }
                if (phase.surface().isEmpty()) {
                    problem(enemy, field + ".release.on", "first-surface needs the phase's surfacing");
                }
                int count = lanes.map(EnemyData.Lanes::count).orElse(0);
                release.lanes().stream()
                        .filter(lane -> lane < 1 || lane > count)
                        .forEach(lane -> problem(enemy, field + ".release.lanes", "the boss has no lane " + lane));
            });
        }
    }

    /**
     * M5 part E, step E2c: a section's speed 0 only on the arena of a level whose boss is anchored, and
     * such a boss has it: its arena of speed 0 lasting at least the ease into the halt, after a moving
     * section, the boss arriving at the arena's start in it; the radio's {@code first-telegraph} only with
     * a boss with lanes, {@code boss-part-destroyed} naming parts of the level's boss other than its
     * vital one.
     */
    void checkArenaLevel(LevelData level) {
        Optional<EnemyData> boss = level.boss()
                .map(LevelData.BossPlacement::enemy)
                .filter(content.enemies()::containsKey)
                .map(content::enemy);
        // M7 part F: a walking boss (the Revenant Walker) is fought in a halted arena too.
        boolean anchored = boss.map(EnemyData::arenaBoss).orElse(false);
        List<LevelData.Section> sections = level.sections();
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            // M5 part G: or on a timed halt (no arena) of a level with a defend primary (PartGChecks).
            if (section.speed().filter(speed -> speed == 0).isPresent()
                    && !(section.isArena() && anchored)
                    // M9 part B (Level 29, the rescue halt): or on a held arena (its holders hold the clock).
                    && !section.held()
                    && !(section.timedHalt() && level.objectives().defend().isPresent())) {
                problem(
                        level,
                        "sections[" + i + "].speed",
                        "speed 0 only on the arena of a level whose boss is anchored, or on a timed halt with a"
                                + " defend primary");
            }
        }
        // M7 part H: a breaching boss's arena crawls instead (ColossusChecks).
        if (anchored && !boss.map(EnemyData::breaches).orElse(false)) {
            LevelData.BossPlacement placement = level.boss().orElseThrow();
            int arena = -1;
            for (int i = 0; i < sections.size(); i++) {
                arena = sections.get(i).isArena() ? i : arena;
            }
            if (arena < 1
                    || sections.get(arena).speed().filter(speed -> speed == 0).isEmpty()) {
                problem(level, "sections", "an anchored boss's arena has speed 0, after a section that scrolls");
            } else {
                double start = level.sectionStart(arena);
                if (sections.get(arena).end() - start < vanguard.sim.Sortie.ARENA_RAMP_SECONDS) {
                    problem(
                            level,
                            "sections[" + arena + "].end",
                            "an arena of speed 0 lasts at least the ease into the halt (1 s)");
                }
                if (Math.abs(placement.t() - start) > 1e-9 || placement.section() != arena + 1) {
                    problem(
                            level,
                            "boss.t",
                            "an anchored boss arrives at its arena's start (" + start + ", section " + (arena + 1)
                                    + ")");
                }
            }
        }
        boolean lanes =
                boss.flatMap(EnemyData::boss).flatMap(EnemyData.BossData::lanes).isPresent();
        // M7 part F: a walking boss's first flame tell is its first telegraph (the radio's first-telegraph).
        boolean telegraphs = lanes
                || boss.flatMap(EnemyData::boss)
                        .flatMap(EnemyData.BossData::cone)
                        .isPresent();
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            LevelData.CueEvent event = cue.event().orElse(null);
            if (event == LevelData.CueEvent.FIRST_TELEGRAPH && !telegraphs) {
                problem(
                        level,
                        "radio[" + i + "].event",
                        "first-telegraph in a level without a boss with lanes or a flame cone");
            }
            if (event == LevelData.CueEvent.FIRST_SHIP_TELEGRAPH
                    && (!lanes || level.convoy().isEmpty())) {
                problem(
                        level,
                        "radio[" + i + "].event",
                        "first-ship-telegraph in a level without a boss with lanes and a convoy");
            }
            if (event == LevelData.CueEvent.BOSS_PART_DESTROYED) {
                for (String part : cue.parts().orElse(List.of())) {
                    int index = boss.map(enemy -> enemy.partIndex(part)).orElse(-1);
                    if (index < 0
                            || boss.get()
                                    .partList()
                                    .orElseThrow()
                                    .get(index)
                                    .kind()
                                    .equals("vital")) {
                        problem(
                                level,
                                "radio[" + i + "].parts",
                                "'" + part + "' is no part of the level's boss but its vital one");
                    }
                }
            }
        }
    }

    /**
     * A set piece is a multi-part enemy; its passes fit their sections and the level, a descent
     * lies on its path, and no wave starts while it is on the player's layer on any difficulty
     * (design/campaign, Level 03: the fight has the screen to itself).
     */
    void checkSetPieces(LevelData level, Set<String> levelEnemies) {
        List<LevelData.SetPieceData> pieces = level.setPieces().orElse(List.of());
        for (int i = 0; i < pieces.size(); i++) {
            LevelData.SetPieceData piece = pieces.get(i);
            String field = "set_pieces[" + i + "]";
            checkEnemyName(level, field + ".enemy", piece.enemy());
            levelEnemies.add(piece.enemy());
            if (content.enemies().containsKey(piece.enemy())
                    && content.enemy(piece.enemy()).partList().isEmpty()) {
                problem(level, field + ".enemy", "'" + piece.enemy() + "' is no multi-part unit (no part_list)");
            }
            for (int p = 0; p < piece.passes().size(); p++) {
                LevelData.PassData pass = piece.passes().get(p);
                String passField = field + ".passes[" + p + "]";
                if (pass.section() < 1 || pass.section() > level.sections().size()) {
                    problem(level, passField + ".section", "no section " + pass.section());
                }
                if (p > 0
                        && pass.path().getFirst().t()
                                < piece.passes().get(p - 1).path().getLast().t()) {
                    problem(level, passField + ".path", "starts before the pass before it ends");
                }
                pass.path().forEach(point -> checkTime(level, passField + ".path", point.t()));
                pass.descend().ifPresent(descent -> {
                    if (descent.at() < pass.path().getFirst().t()
                            || descent.at() > pass.path().getLast().t()) {
                        problem(level, passField + ".descend.at", "t=" + descent.at() + " is not on the pass's path");
                    }
                    double from = descent.at() + descent.seconds();
                    double to = descent.at();
                    for (Difficulty difficulty : Difficulty.values()) {
                        to = Math.max(to, descent.at() + pass.holdOn(difficulty).orElseThrow());
                    }
                    for (int w = 0; w < level.waves().size(); w++) {
                        double t = level.waves().get(w).t();
                        if (t >= from && t < to) {
                            problem(
                                    level,
                                    "waves[" + w + "].t",
                                    "t=" + t + " while " + piece.enemy() + " is on the player's layer");
                        }
                    }
                });
            }
        }
    }

    /** Parts named by a level (a part drop, a parts objective) are parts of its boss. */
    void checkBossParts(LevelData level, String field, List<String> parts) {
        if (level.boss().isEmpty()) {
            problem(level, field, "boss parts need the level's boss");
            return;
        }
        String slug = level.boss().get().enemy();
        if (!content.enemies().containsKey(slug)) {
            return;
        }
        for (String part : parts) {
            if (content.enemy(slug).partIndex(part) < 0) {
                problem(level, field, "'" + slug + "' has no part '" + part + "'");
            }
        }
    }

    /**
     * The debris chunks resolve, enter inside the play field and within the level, and no more
     * large ones than allowed are on the screen at once, on any difficulty.
     */
    void checkDebris(LevelData level) {
        if (level.debris().isEmpty()) {
            return;
        }
        LevelData.DebrisField field = level.debris().get();
        List<LevelData.PlacedChunk> placed = field.placed();
        for (int i = 0; i < placed.size(); i++) {
            LevelData.PlacedChunk chunk = placed.get(i);
            String at = "debris.placed[" + i + "]";
            checkTime(level, at + ".t", chunk.t());
            if (!field.chunks().containsKey(chunk.chunk())) {
                problem(
                        level,
                        at + ".chunk",
                        "no chunk '" + chunk.chunk() + "' in debris.chunks "
                                + field.chunks().keySet());
            } else if (chunk.x() < 0 || chunk.x() > PlayField.WIDTH) {
                problem(level, at + ".x", "x=" + chunk.x() + " is outside the play field");
            }
        }
        if (field.maxLarge().isEmpty()
                || !placed.stream().allMatch(c -> field.chunks().containsKey(c.chunk()))) {
            return;
        }
        int max = field.maxLarge().get();
        for (Difficulty difficulty : Difficulty.values()) {
            double factor = field.driftFactor(difficulty);
            List<LevelData.PlacedChunk> chunks = field.placedOn(difficulty);
            for (double t = 0; t < level.seconds(); t += 0.05) {
                int large = 0;
                for (LevelData.PlacedChunk chunk : chunks) {
                    LevelData.Chunk kind = field.chunks().get(chunk.chunk());
                    if (kind.large() && onScreen(chunk, kind.size(), factor, t)) {
                        large++;
                    }
                }
                if (large > max) {
                    problem(
                            level,
                            "debris.placed",
                            large + " large chunks on the screen at t=" + Math.round(t * 100) / 100.0 + " on "
                                    + difficulty.name().toLowerCase(Locale.ROOT) + " (at most " + max + ")");
                    break;
                }
            }
        }
    }

    /** Whether a chunk drifting from the top edge is on the screen at {@code t}. */
    private static boolean onScreen(LevelData.PlacedChunk chunk, Size size, double factor, double t) {
        if (t < chunk.t()) {
            return false;
        }
        double age = t - chunk.t();
        double x = chunk.x() + chunk.drift().x() * factor * age;
        double y = PlayField.HEIGHT + size.height() / 2 + chunk.drift().y() * factor * age;
        return y + size.height() / 2 > 0
                && y - size.height() / 2 < PlayField.HEIGHT
                && x + size.width() / 2 > 0
                && x - size.width() / 2 < PlayField.WIDTH;
    }
}
