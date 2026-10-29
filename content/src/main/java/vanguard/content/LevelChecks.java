package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import vanguard.sim.PlayField;

/**
 * A level's checks across files: its waves, targets, pickups, radio and music name what exists and fit its sections
 * and play field.
 */
final class LevelChecks extends ContentChecks {
    private final BossChecks bosses;
    private final EscortChecks escorts;

    LevelChecks(Content content, Problems problems, BossChecks bosses, EscortChecks escorts) {
        super(content, problems);
        this.bosses = bosses;
        this.escorts = escorts;
    }

    void checkLevel(LevelData level) {
        Set<Double> waveTimes = new TreeSet<>();
        Set<String> levelEnemies = new TreeSet<>();
        double previous = 0;
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            checkTime(level, field + ".t", wave.t());
            if (wave.t() < previous) {
                problem(level, field + ".t", "waves are listed in time order");
            }
            previous = wave.t();
            waveTimes.add(wave.t());
            List<LevelData.Group> groups = wave.groupList();
            for (int g = 0; g < groups.size(); g++) {
                LevelData.Group group = groups.get(g);
                String groupField = wave.enemy().isPresent() ? field : field + ".groups[" + g + "]";
                checkFormation(level, groupField + ".formation", group.formation());
                checkEnemyName(level, groupField + ".enemy", group.enemy());
                levelEnemies.add(group.enemy());
                if (group.formation().equals("whirl cluster") && wave.at().isEmpty()) {
                    problem(level, field + ".at", "a whirl cluster needs its release point");
                }
            }
            wave.at().ifPresent(at -> {
                // M5 part F: a whirl cluster on the wind is released just outside its side edge (Level 12).
                if (!sideRelease(wave)
                        && (at.x() < 0 || at.x() > PlayField.WIDTH || at.y() < 0 || at.y() > PlayField.HEIGHT)) {
                    problem(level, field + ".at", "[" + at.x() + ", " + at.y() + "] is outside the play field");
                }
            });
        }
        bosses.checkSetPieces(level, levelEnemies);
        // M5 part H: the root bursts' growths fly in the level too (Level 14).
        for (LevelData.RootBurst burst : level.bursts()) {
            for (Difficulty difficulty : Difficulty.values()) {
                levelEnemies.addAll(burst.growthsOn(difficulty));
            }
        }
        level.boss().ifPresent(boss -> {
            checkEnemyName(level, "boss.enemy", boss.enemy());
            levelEnemies.add(boss.enemy());
            if (content.enemies().containsKey(boss.enemy())
                    && content.enemy(boss.enemy()).boss().isEmpty()) {
                problem(level, "boss.enemy", "'" + boss.enemy() + "' has no boss script");
            }
            if (boss.section() > level.sections().size()) {
                problem(level, "boss.section", "no section " + boss.section());
            }
            checkTime(level, "boss.t", boss.t());
        });
        // M9 part B (Level 29, the rescue halt): or a held arena (its holders' group) without a boss.
        if (level.sections().stream().filter(LevelData.Section::isArena).count() > 1
                || (level.sections().stream().anyMatch(section -> section.isArena() && !section.held())
                        && level.boss().isEmpty())
                || (level.sections().stream().anyMatch(LevelData.Section::held)
                        && level.boss().isPresent())) {
            problem(level, "sections", "a level has at most one arena, a boss's or a held one (without a boss)");
        }
        bosses.checkDebris(level);
        checkLevelRules(level);
        level.prompts().ifPresent(prompts -> {
            for (int i = 0; i < prompts.size(); i++) {
                if (!prompts.get(i).onEvent()) {
                    checkTime(level, "prompts[" + i + "].t", prompts.get(i).t());
                }
            }
        });
        level.objectives().secondary().flatMap(LevelData.Secondary::escapes).ifPresent(slug -> {
            // M8 part B: or a ground target of it (Level 22's Drill Rigs).
            boolean placed = level.groundTargets().stream()
                    .anyMatch(target -> target.enemy().filter(slug::equals).isPresent());
            // M9 part G (Level 34's "Strip the graveyard"): or a ground object's look (an object escapes objective).
            if (!levelEnemies.contains(slug) && !placed && SalvageChecks.objectsOf(level, slug) == 0) {
                problem(
                        level,
                        "objectives.secondary.escapes",
                        "no wave, ground target or object look of '" + slug + "' in this level");
            }
        });
        level.crateSeconds().ifPresent(seconds -> {
            if (level.secrets().isEmpty()) {
                problem(level, "crate_seconds", "a level without secrets has no crate");
            } else if (seconds < content.player().pickupSeconds()) {
                problem(
                        level,
                        "crate_seconds",
                        "shorter than the player's pickup_seconds ("
                                + content.player().pickupSeconds() + ")");
            }
        });
        Set<String> secrets =
                level.secrets().stream().map(LevelData.Secret::name).collect(Collectors.toSet());
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            String field = "ground_targets[" + i + "]";
            if (target.section() < 1 || target.section() > level.sections().size()) {
                problem(level, field + ".section", "no section " + target.section());
            } else {
                for (int p = 0; p < target.at().size(); p++) {
                    LevelData.Placement placement = target.at().get(p);
                    if (level.sectionAt(placement.t()) != target.section()) {
                        problem(
                                level,
                                field + ".at[" + p + "]",
                                "t=" + placement.t() + " is not in section " + target.section());
                    }
                    // M5 part G: a nest from a side gives its units' y there instead of an x (PartGChecks).
                    if (target.side().isEmpty() && (placement.x() < 0 || placement.x() > PlayField.WIDTH)) {
                        problem(level, field + ".at[" + p + "]", "x=" + placement.x() + " is outside the play field");
                    }
                }
            }
            target.reveals().ifPresent(name -> {
                if (!secrets.contains(name)) {
                    problem(level, field + ".reveals", "no secret '" + name + "'");
                }
            });
            target.enemy().ifPresent(slug -> {
                checkEnemyName(level, field + ".enemy", slug);
                levelEnemies.add(slug);
                if (content.enemies().containsKey(slug)
                        && content.enemy(slug).movement().terrain().isEmpty()) {
                    problem(
                            level,
                            field + ".enemy",
                            "'" + slug + "' does not stand on the ground (no terrain movement)");
                }
            });
            if (target.enemy().isPresent()) {
                // M9 part B: or the group that holds a held arena (its section's holds: the clamp guns).
                target.group()
                        .filter(group -> level.sections().stream()
                                .noneMatch(section ->
                                        section.holds().filter(group::equals).isPresent()))
                        .ifPresent(group -> checkGroup(level, field + ".group", group));
            } else {
                target.group().ifPresent(group -> checkDestructibleGroup(level, field + ".group", group));
            }
            checkTargetLayer(level, field, target);
        }
        for (int i = 0; i < level.cranes().orElse(List.of()).size(); i++) {
            LevelData.CraneData crane = level.cranes().get().get(i);
            String field = "cranes[" + i + "]";
            crane.swings().forEach(t -> checkTime(level, field + ".swings", t));
            crane.clamp().ifPresent(clamp -> {
                if (!secrets.contains(clamp.reveals())) {
                    problem(level, field + ".clamp.reveals", "no secret '" + clamp.reveals() + "'");
                }
            });
        }
        checkCarriers(level, "pickups", level.pickups(), waveTimes);
        level.difficulty()
                .easy()
                .flatMap(LevelData.Variant::extraPickups)
                .ifPresent(pickups -> checkCarriers(level, "difficulty.easy.extra_pickups", pickups, waveTimes));
        level.difficulty()
                .hard()
                .flatMap(LevelData.Variant::extraPickups)
                .ifPresent(pickups -> checkCarriers(level, "difficulty.hard.extra_pickups", pickups, waveTimes));
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            String field = "radio[" + i + "]";
            cue.t().ifPresent(t -> checkTime(level, field + ".t", t));
            cue.enemy().ifPresent(enemy -> {
                if (!levelEnemies.contains(enemy)) {
                    problem(level, field + ".enemy", "no wave of '" + enemy + "' in this level");
                }
            });
            cue.group().ifPresent(group -> checkGroup(level, field + ".group", group));
        }
        escorts.checkEscort(level, levelEnemies);
        LevelData.Music music = level.music();
        if (music.fullSection() > level.sections().size()) {
            problem(level, "music.full_section", "no section " + music.fullSection());
        }
        music.stems().ifPresent(stems -> stems.keySet().forEach(section -> {
            if (section < 1 || section > level.sections().size()) {
                problem(level, "music.stems", "no section " + section);
            }
        }));
        music.voiceLoop().ifPresent(loop -> {
            if (loop.section() > level.sections().size()) {
                problem(level, "music.voice_loop.section", "no section " + loop.section());
            }
            if (level.radio().stream()
                    .noneMatch(cue -> cue.t().isPresent() && cue.speaker().equals(loop.speaker()))) {
                problem(level, "music.voice_loop.speaker", "no timed radio line of " + loop.speaker());
            }
        });
        music.ambienceFrom().ifPresent(t -> checkTime(level, "music.ambience_from", t));
        for (var variant : List.of(level.difficulty().easy(), level.difficulty().hard())) {
            variant.flatMap(LevelData.Variant::enemies)
                    .ifPresent(changes -> changes.keySet().forEach(slug -> checkEnemyName(level, "difficulty", slug)));
        }
        checkHoldsAndPacks(level, levelEnemies);
        checkSwarmsAndAmbushes(level);
        checkFieldsAndConvoy(level);
        checkWeatherAndSondes(level, levelEnemies);
        new PartGChecks(content, level, (field, message) -> problem(level, field, message)).run(levelEnemies);
        PartHChecks.level(content, level, (field, message) -> problem(level, field, message));
        TerrainChecks.level(content, level, (field, message) -> problem(level, field, message));
        SiegeChecks.level(content, level, (field, message) -> problem(level, field, message));
        StormChecks.level(content, level, (field, message) -> problem(level, field, message));
        FoundryChecks.level(content, level, (field, message) -> problem(level, field, message));
        ColossusChecks.level(content, level, (field, message) -> problem(level, field, message));
        MawChecks.level(content, level, (field, message) -> problem(level, field, message));
        RetreatChecks.level(content, level, (field, message) -> problem(level, field, message));
        bosses.checkArenaLevel(level);
        new BackdropCheck(level, (field, message) -> problem(level, field, message)).run();
    }

    /**
     * M5 part C's level keys (Level 09): hold zones wait for the objectives' groups, each group in
     * one hold at most, slower than every section on every difficulty, and not in a level with the
     * set pieces, cranes, sleds, tows or boss that the level clock's slowing was not built for; the
     * collapse waits for groups, falls with a tower placed once and leaves a backdrop piece; a scoped escapes objective has waves of
     * its enemy with its tag; a pack has a path for each unit on every difficulty; the radio's new
     * events, {@code music.full_on} and the threat profile's required traits have what they name.
     */
    private void checkHoldsAndPacks(LevelData level, Set<String> levelEnemies) {
        List<LevelData.Hold> holds = level.holds().orElse(List.of());
        Set<String> held = new TreeSet<>();
        double slowest = level.sections().stream()
                .mapToDouble(section -> section.speed().orElse(level.scrollSpeed()))
                .min()
                .orElseThrow();
        for (int i = 0; i < holds.size(); i++) {
            LevelData.Hold hold = holds.get(i);
            String field = "holds[" + i + "]";
            for (String group : hold.groups()) {
                checkGroup(level, field + ".groups", group);
                if (!held.add(group)) {
                    problem(level, field + ".groups", "group '" + group + "' is in another hold already");
                }
            }
            for (Difficulty difficulty : Difficulty.values()) {
                if (hold.speedOn(difficulty) >= slowest) {
                    problem(
                            level,
                            field + ".speed",
                            hold.speedOn(difficulty) + " px/s on "
                                    + difficulty.name().toLowerCase(Locale.ROOT)
                                    + " is not below every section's speed (" + slowest + ")");
                }
            }
        }
        if (!holds.isEmpty()) {
            List<String> unsupported = new ArrayList<>();
            level.setPieces().filter(pieces -> !pieces.isEmpty()).ifPresent(pieces -> unsupported.add("set_pieces"));
            level.cranes().filter(cranes -> !cranes.isEmpty()).ifPresent(cranes -> unsupported.add("cranes"));
            level.sleds().ifPresent(sleds -> unsupported.add("sleds"));
            level.tows().filter(tows -> !tows.isEmpty()).ifPresent(tows -> unsupported.add("tows"));
            level.boss().ifPresent(boss -> unsupported.add("boss"));
            if (!unsupported.isEmpty()) {
                problem(level, "holds", "hold zones are not built for a level with " + unsupported);
            }
        }
        level.collapse().ifPresent(collapse -> {
            collapse.groups().forEach(group -> checkGroup(level, "collapse.groups", group));
            if (!level.backdrop().pieces().containsKey(collapse.rubble())) {
                problem(level, "collapse.rubble", "no backdrop piece '" + collapse.rubble() + "'");
            }
            BackdropData.Piece tower = level.backdrop().pieces().get(collapse.tower());
            long placed = level.backdrop().placements().stream()
                    .filter(p -> p.piece().equals(collapse.tower()))
                    .count();
            if (tower == null || tower.tower().isEmpty()) {
                problem(level, "collapse.tower", "no backdrop tower piece '" + collapse.tower() + "'");
            } else if (placed != 1) {
                problem(level, "collapse.tower", "'" + collapse.tower() + "' is placed " + placed + " times, not once");
            }
        });
        level.objectives().secondary().ifPresent(secondary -> secondary.tag().ifPresent(tag -> {
            String slug = secondary.escapes().orElseThrow();
            boolean tagged = level.waves().stream()
                    .anyMatch(wave -> wave.tag().filter(tag::equals).isPresent()
                            && wave.groupList().stream()
                                    .anyMatch(group -> group.enemy().equals(slug)));
            if (!tagged) {
                problem(level, "objectives.secondary.tag", "no wave of '" + slug + "' tagged '" + tag + "'");
            }
        }));
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            for (LevelData.Group group : wave.groupList()) {
                if (!group.formation().equals("pack")) {
                    continue;
                }
                int paths = wave.paths().map(List::size).orElse(0);
                int most = 0;
                for (Difficulty difficulty : Difficulty.values()) {
                    most = Math.max(most, wave.fliesOn(difficulty) ? countOn(wave, group, difficulty) : 0);
                }
                if (paths < most) {
                    problem(
                            level,
                            "waves[" + i + "].paths",
                            "a pack needs a path per unit, as many as its largest difficulty's count: " + paths
                                    + " for " + most);
                }
            }
        }
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.CueEvent event = level.radio().get(i).event().orElse(null);
            String field = "radio[" + i + "].event";
            if (event == LevelData.CueEvent.HOLD_START && holds.isEmpty()) {
                problem(level, field, "hold-start in a level without holds");
            } else if (event == LevelData.CueEvent.COLLAPSE && level.collapse().isEmpty()) {
                problem(level, field, "collapse in a level without a collapse");
            } else if (event == LevelData.CueEvent.FIRST_POUNCE
                    && levelEnemies.stream().noneMatch(this::pounces)) {
                problem(level, field, "first-pounce in a level without an enemy that pounces");
            }
        }
        level.music().fullOn().ifPresent(events -> {
            if (events.contains(LevelData.Music.FullOn.HOLD) && holds.isEmpty()) {
                problem(level, "music.full_on", "hold in a level without holds");
            }
            if (events.contains(LevelData.Music.FullOn.COLLAPSE)
                    && level.collapse().isEmpty()) {
                problem(level, "music.full_on", "collapse in a level without a collapse");
            }
        });
    }

    /**
     * M5 part E's level keys (Level 11): a field wave's enemy drifts, its area lies across the play
     * field and holds its units at its spacing on every difficulty; a nest's current only for units
     * whose terrain drifts; the naval convoy (beside no escort): its units are allies that follow
     * stations, named once each, their stations on the play field and apart, their lanes the level
     * boss's (a boss with lanes), in their lanes on the play field; an afloat objective has a convoy
     * with a unit that can be damaged and a boss.
     */
    private void checkFieldsAndConvoy(LevelData level) {
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            if (wave.size().isEmpty()) {
                continue;
            }
            String field = "waves[" + i + "]";
            LevelData.Group group = wave.groupList().getFirst();
            if (!content.enemies().containsKey(group.enemy())) {
                continue;
            }
            EnemyData enemy = content.enemy(group.enemy());
            // M7 part B: or burrow (a Burrower field enters buried with the ground).
            if (enemy.movement().drift().isEmpty() && enemy.movement().burrow().isEmpty()) {
                problem(
                        level,
                        field + ".enemy",
                        "a field's units drift or burrow: '" + group.enemy() + "' has no movement.drift or .burrow");
            }
            Size size = wave.size().get();
            double x = wave.x().orElseThrow();
            if (x - size.width() / 2 < 0 || x + size.width() / 2 > PlayField.WIDTH) {
                problem(level, field + ".size", "the field reaches beyond the play field's sides");
            }
            double spacing =
                    wave.spacing().orElse(SimSpecs.fieldSpacing(enemy.hitbox().width()));
            int capacity = (int) Math.floor(size.width() / spacing) * (int) Math.floor(size.height() / spacing);
            for (Difficulty difficulty : Difficulty.values()) {
                Optional<LevelData.Change> change =
                        switch (difficulty) {
                            case EASY -> wave.easy();
                            case MEDIUM -> Optional.empty();
                            case HARD -> wave.hard();
                        };
                int count = change.flatMap(LevelData.Change::count)
                        .orElseGet(() -> content.difficulty().formationSize(group.count(), difficulty));
                if (count > capacity) {
                    problem(
                            level,
                            field + ".size",
                            count + " units on " + difficulty.name().toLowerCase(Locale.ROOT) + " do not fit "
                                    + size.width() + "×" + size.height() + " px " + spacing + " px apart (at most "
                                    + capacity + ")");
                }
            }
        }
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.current().isPresent()
                    && target.enemy().filter(content.enemies()::containsKey).isPresent()
                    && content.enemy(target.enemy().get())
                            .movement()
                            .terrain()
                            .flatMap(EnemyData.Terrain::drift)
                            .isEmpty()) {
                problem(level, "ground_targets[" + i + "].current", "its units do not drift (terrain.drift)");
            }
        }
        level.convoy().ifPresent(convoy -> checkConvoy(level, convoy));
        boolean afloat = level.objectives()
                .secondary()
                .flatMap(LevelData.Secondary::afloat)
                .orElse(false);
        if (afloat
                && (level.convoy().isEmpty()
                        || level.boss().isEmpty()
                        || level.convoy().map(this::damageable).orElse(0) == 0)) {
            problem(
                    level,
                    "objectives.secondary.afloat",
                    "an afloat objective needs a convoy with a unit that can be damaged, and a boss");
        }
    }

    /** M5 part E: a level's naval convoy (see {@link #checkFieldsAndConvoy(LevelData)}). */
    private void checkConvoy(LevelData level, LevelData.Convoy convoy) {
        if (level.objectives().escort().isPresent()) {
            problem(level, "convoy", "a naval convoy is not an escort: give one or the other");
        }
        Optional<EnemyData.Lanes> lanes = PartERules.lanes(content, level);
        Set<String> names = new TreeSet<>();
        List<double[]> boxes = new ArrayList<>();
        for (int k = 0; k < convoy.units().size(); k++) {
            LevelData.ConvoyUnit unit = convoy.units().get(k);
            String field = "convoy.units[" + k + "]";
            if (!names.add(unit.name())) {
                problem(level, field + ".name", "'" + unit.name() + "' names another unit already");
            }
            AlliesData.Ally ally = content.allies().allies().get(unit.ally());
            if (ally == null) {
                problem(level, field + ".ally", "unknown ally '" + unit.ally() + "'");
                continue;
            }
            if (!ally.naval()) {
                problem(level, field + ".ally", "a " + unit.ally() + " does not follow stations");
                continue;
            }
            double w = ally.hitbox().width();
            double h = ally.hitbox().height();
            Point at = unit.station();
            if (at.x() - w / 2 < 0
                    || at.x() + w / 2 > PlayField.WIDTH
                    || at.y() - h / 2 < 0
                    || at.y() + h / 2 > PlayField.HEIGHT) {
                problem(level, field + ".station", "its hit box leaves the play field");
            }
            for (double[] other : boxes) {
                if (Math.abs(other[0] - at.x()) < (other[2] + w) / 2
                        && Math.abs(other[1] - at.y()) < (other[3] + h) / 2) {
                    problem(level, field + ".station", "its hit box overlaps another unit's");
                }
            }
            boxes.add(new double[] {at.x(), at.y(), w, h});
            unit.hold().ifPresent(hold -> {
                if (hold.x() - w / 2 < 0
                        || hold.x() + w / 2 > PlayField.WIDTH
                        || hold.y() - h / 2 < 0
                        || hold.y() - h / 2 > PlayField.HEIGHT + h) {
                    problem(level, field + ".hold", "its hit box lies beside the play field or far below it");
                }
                if (convoy.holdClear().orElse(0.0) <= 0) {
                    problem(level, field + ".hold", "a hold point needs the convoy's hold_clear");
                }
            });
            unit.lane().ifPresent(lane -> {
                if (lanes.isEmpty()) {
                    problem(level, field + ".lane", "only a level whose boss has lanes gives lanes");
                } else if (lane > lanes.get().count()) {
                    problem(
                            level,
                            field + ".lane",
                            "the boss has " + lanes.get().count() + " lanes, not " + lane);
                } else if (lanes.get().width() < w) {
                    problem(level, field + ".lane", "a " + unit.ally() + " is wider than a lane");
                }
                double y = convoy.laneY().orElseThrow();
                if (y - h / 2 < 0 || y + h / 2 > PlayField.HEIGHT) {
                    problem(level, "convoy.lane_y", "a " + unit.ally() + " in its lane leaves the play field");
                }
            });
        }
        for (int a = 0; a < convoy.units().size(); a++) {
            for (int b = a + 1; b < convoy.units().size(); b++) {
                if (convoy.units().get(a).lane().isPresent()
                        && convoy.units()
                                .get(a)
                                .lane()
                                .equals(convoy.units().get(b).lane())) {
                    problem(level, "convoy.units[" + b + "].lane", "two units in one lane");
                }
            }
        }
    }

    /** M5 part F: a whirl cluster on the wind, released from its {@code at} just outside the side edge it names. */
    private static boolean sideRelease(LevelData.Wave wave) {
        if (wave.from() != LevelData.Entry.SIDES
                || wave.edge().isEmpty()
                || wave.edge().get() == LevelData.Edge.ALTERNATING
                || wave.groupList().stream()
                        .noneMatch(group -> group.formation().equals("whirl cluster"))) {
            return false;
        }
        return wave.at()
                .filter(point -> point.y() >= 0 && point.y() <= PlayField.HEIGHT)
                .filter(point -> wave.edge().get() == LevelData.Edge.LEFT
                        ? point.x() < 0 && point.x() >= -SIDE_RELEASE
                        : point.x() > PlayField.WIDTH && point.x() <= PlayField.WIDTH + SIDE_RELEASE)
                .isPresent();
    }

    /** M5 part F: how far outside its side edge a whirl cluster on the wind may be released, px. */
    private static final double SIDE_RELEASE = 40;

    /**
     * M5 part F's level keys (Level 12): the weather's times lie in the level and its drift's section
     * exists; the sondes lie in the play field during the level, not in a level with a boss (the
     * checkpoint), and a {@code collect: sondes} objective has them; a placed pickup's time and x; a
     * rear stream and the {@code first-latch} radio event and prompt only with a chaser that latches;
     * the music's section levels name sections.
     */
    private void checkWeatherAndSondes(LevelData level, Set<String> levelEnemies) {
        level.weather().ifPresent(weather -> {
            weather.gusts().ifPresent(gusts -> {
                List<LevelData.GustAt> all = new ArrayList<>(gusts.at());
                gusts.hard().flatMap(LevelData.GustChange::extra).ifPresent(all::addAll);
                for (int i = 0; i < all.size(); i++) {
                    checkTime(level, "weather.gusts.at[" + i + "].t", all.get(i).t());
                }
            });
            weather.drift().ifPresent(drift -> {
                if (drift.section() > level.sections().size()) {
                    problem(level, "weather.drift.section", "no section " + drift.section());
                }
            });
            weather.lightning().ifPresent(lightning -> {
                for (int i = 0; i < lightning.at().size(); i++) {
                    checkTime(
                            level,
                            "weather.lightning.at[" + i + "]",
                            lightning.at().get(i));
                }
                List<Double> at = lightning.at();
                for (int i = 3; i < at.size(); i++) {
                    if (at.get(i) - at.get(i - 3) < 1) {
                        // design/campaign Level 12 (flash safety): never more than 3 flashes a second.
                        problem(level, "weather.lightning.at[" + i + "]", "a fourth flash within a second");
                    }
                }
            });
        });
        boolean collect = level.objectives()
                .secondary()
                .flatMap(LevelData.Secondary::collect)
                .isPresent();
        if (collect && level.sondes().isEmpty()) {
            problem(level, "objectives.secondary.collect", "a collect: sondes objective needs the level's sondes");
        }
        level.sondes().ifPresent(sondes -> {
            if (level.boss().isPresent()) {
                problem(level, "sondes", "weather sondes in a level with a boss (its checkpoint) are not supported");
            }
            for (int i = 0; i < sondes.at().size(); i++) {
                LevelData.Placement at = sondes.at().get(i);
                checkTime(level, "sondes.at[" + i + "]", at.t());
                if (at.x() < 0 || at.x() > PlayField.WIDTH) {
                    problem(level, "sondes.at[" + i + "]", "x=" + at.x() + " is outside the play field");
                }
            }
        });
        for (int i = 0; i < level.pickups().size(); i++) {
            LevelData.PlacedPickup pickup = level.pickups().get(i);
            if (pickup.carried()) {
                continue;
            }
            LevelData.Placement at = pickup.at().orElseThrow();
            checkTime(level, "pickups[" + i + "].at", at.t());
            if (at.x() < 0 || at.x() > PlayField.WIDTH) {
                problem(level, "pickups[" + i + "].at", "x=" + at.x() + " is outside the play field");
            }
        }
        boolean latches = levelEnemies.stream()
                .filter(content.enemies()::containsKey)
                .anyMatch(slug -> content.enemy(slug).latch().isPresent());
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            for (LevelData.Group group : wave.groupList()) {
                boolean chaser = content.enemies().containsKey(group.enemy())
                        && content.enemy(group.enemy()).movement().chase().isPresent();
                boolean rear = wave.from() == LevelData.Entry.REAR
                        || wave.easy().flatMap(LevelData.Change::from).orElse(null) == LevelData.Entry.REAR
                        || wave.hard().flatMap(LevelData.Change::from).orElse(null) == LevelData.Entry.REAR;
                if (rear && group.formation().equals("stream") && !chaser) {
                    problem(level, "waves[" + i + "].from", "only chasers stream in from the rear");
                }
            }
        }
        for (int i = 0; i < level.radio().size(); i++) {
            if (level.radio().get(i).event().orElse(null) == LevelData.CueEvent.FIRST_LATCH && !latches) {
                problem(level, "radio[" + i + "].event", "first-latch needs a unit that latches in the level");
            }
        }
        List<LevelData.Prompt> prompts = level.prompts().orElse(List.of());
        for (int i = 0; i < prompts.size(); i++) {
            if (prompts.get(i).event().orElse(null) == LevelData.PromptEvent.FIRST_LATCH && !latches) {
                problem(level, "prompts[" + i + "].event", "first-latch needs a unit that latches in the level");
            }
        }
        level.music().sectionDb().ifPresent(levels -> levels.keySet().forEach(section -> {
            if (section > level.sections().size()) {
                problem(level, "music.section_db", "no section " + section);
            }
        }));
    }

    /**
     * M5 part D's wave keys (Level 10): a swarm's enemy has a flock, its wave one route ({@code
     * paths}) and at most the flock's members on every difficulty; a rear ambush's enemy has an
     * ambush, its wave enters {@code from: rear} with 1–4 units on every difficulty and no paths; a
     * flock or an ambush unit flies only its own formation; only a swarm loops back more than once
     * ({@code loop_back.count}, a change's {@code loops}); a snake's {@code paths} is one route, as a
     * stream's (its left-edge units' route, mirrored for the right-edge ones). M8 part G (step G3): a pincer of a hovering unit
     * that is not a side hover (the Barbfin) has a {@code hold} on every difficulty it flies.
     */
    private void checkSwarmsAndAmbushes(LevelData level) {
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            boolean swarm = false;
            for (LevelData.Group group : wave.groupList()) {
                if (!content.enemies().containsKey(group.enemy())) {
                    continue;
                }
                EnemyData enemy = content.enemy(group.enemy());
                Optional<EnemyData.Flock> flock = enemy.movement().flock();
                boolean ambush = enemy.movement().ambush().isPresent();
                // M8 part G (§5 G9): a unit that holds above the ship rises from below in a rear ambush (the Barbfin).
                boolean risesUp = enemy.movement()
                        .hover()
                        .flatMap(EnemyData.Hover::aboveShip)
                        .isPresent();
                // M8 part G (step G3): a hovering unit that is not a side hover (the Barbfin's hold above the ship).
                boolean plainHover = enemy.movement().hover().isPresent()
                        && enemy.movement()
                                .hover()
                                .flatMap(EnemyData.Hover::edgeX)
                                .isEmpty();
                // M5 part F (F9 = a): a swarm of chasers needs no flock and flies no route.
                // M9 part B (Level 29): a swarm on rocks (the Asteroid Mites) rides its rocks: no flock, no route.
                boolean chasers =
                        (enemy.movement().chase().isPresent() || wave.onRocks().isPresent()) && flock.isEmpty();
                swarm |= group.formation().equals("swarm") && !chasers;
                // M7 part D: a unit with a flock that also lists other formations (the Dust Kite) flies those too.
                // M7 part G: a flock's pursuit (the Mote Swarm in Level 20) is a swarm of its own (RetreatChecks).
                boolean onlySwarms = enemy.formations().stream()
                        .allMatch(
                                use -> use.name().equals("swarm") || use.name().equals(LevelData.Wave.PURSUIT));
                boolean pursuit = group.formation().equals(LevelData.Wave.PURSUIT);
                if (!chasers
                        && (group.formation().equals("swarm")
                                ? flock.isEmpty()
                                : flock.isPresent() && onlySwarms && !pursuit)) {
                    problem(
                            level,
                            field + ".formation",
                            flock.isPresent()
                                    ? "'" + group.enemy() + "' flies only a swarm"
                                    : "a swarm's enemy has a flock (movement.flock), '" + group.enemy() + "' has none");
                }
                boolean rearAmbush = group.formation().equals("rear ambush");
                if (ambush ? !rearAmbush : rearAmbush && !risesUp) {
                    problem(
                            level,
                            field + ".formation",
                            ambush
                                    ? "'" + group.enemy() + "' flies only a rear ambush"
                                    : "a rear ambush's enemy has an ambush (movement.ambush) or holds above the ship"
                                            + " (movement.hover.above_ship), '" + group.enemy() + "' has neither");
                }
                for (Difficulty difficulty : Difficulty.values()) {
                    if (!wave.fliesOn(difficulty)) {
                        continue;
                    }
                    int count = countOn(wave, group, difficulty);
                    String on = " on " + difficulty.name().toLowerCase(Locale.ROOT);
                    if (flock.isPresent() && count > flock.get().max()) {
                        problem(
                                level,
                                field + ".count",
                                count + " members" + on + ", the flock holds "
                                        + flock.get().max());
                    }
                    if (group.formation().equals("rear ambush") && (count < 1 || count > 4)) {
                        problem(level, field + ".count", "a rear ambush has 1 to 4 units, not " + count + on);
                    }
                    // M8 part G (step G3): a pincer of a hovering unit that is not a side hover (the Barbfin) holds at
                    // its edges for its wave's hold, which the simulation needs on every difficulty it flies.
                    Optional<LevelData.Change> change =
                            switch (difficulty) {
                                case EASY -> wave.easy();
                                case MEDIUM -> Optional.empty();
                                case HARD -> wave.hard();
                            };
                    if (group.formation().equals("pincer")
                            && plainHover
                            && change.flatMap(LevelData.Change::hold)
                                    .or(wave::hold)
                                    .isEmpty()) {
                        problem(
                                level,
                                field + ".hold",
                                "a pincer of '" + group.enemy()
                                        + "' (a hover, not at the side) holds at its edges: it needs a hold" + on);
                    }
                }
                if (group.formation().equals("swarm")
                        && !chasers
                        && wave.paths().map(List::size).orElse(0) != 1) {
                    problem(level, field + ".paths", "a swarm flies one route: one path");
                }
                if (group.formation().equals("swarm") && chasers && wave.paths().isPresent()) {
                    problem(level, field + ".paths", "a swarm of chasers flies straight in, no paths");
                }
                if (group.formation().equals("rear ambush")) {
                    if (wave.from() != LevelData.Entry.REAR
                            || wave.easy()
                                    .flatMap(LevelData.Change::from)
                                    .filter(f -> f != LevelData.Entry.REAR)
                                    .isPresent()
                            || wave.hard()
                                    .flatMap(LevelData.Change::from)
                                    .filter(f -> f != LevelData.Entry.REAR)
                                    .isPresent()) {
                        problem(level, field + ".from", "a rear ambush is from: rear");
                    }
                    if (wave.paths().isPresent()) {
                        problem(level, field + ".paths", "a rear ambush flies its lanes, no paths");
                    }
                }
                if (group.formation().equals("snake")
                        && wave.paths().map(List::size).orElse(1) != 1) {
                    problem(level, field + ".paths", "a snake flies one route: one path");
                }
                if (group.formation().equals("stream")
                        && wave.paths().map(List::size).orElse(1) != 1) {
                    // M5 part D: a unit from the right flies the route mirrored.
                    problem(level, field + ".paths", "a stream flies one route (from the left): one path");
                }
            }
            boolean loops = wave.loopBack()
                            .flatMap(LevelData.LoopBack::count)
                            .filter(n -> n > 1)
                            .isPresent()
                    || wave.easy().flatMap(LevelData.Change::loops).isPresent()
                    || wave.hard().flatMap(LevelData.Change::loops).isPresent();
            if (loops && !swarm) {
                problem(level, field, "only a swarm loops back more than once (loop_back.count, loops)");
            }
            if ((wave.easy().flatMap(LevelData.Change::loops).isPresent()
                            || wave.hard().flatMap(LevelData.Change::loops).isPresent())
                    && wave.loopBack().isEmpty()) {
                problem(level, field, "loops change a loop_back the wave does not have");
            }
        }
        checkEventSources(level);
    }

    /**
     * M5 part D's radio events and music keys have what they name: {@code scripted-loss} and the
     * music's duck a scripted loss, {@code first-decloak} a cloaked enemy, {@code first-loop-back} a
     * swarm that loops back; the ambience changes lie in the level's later sections.
     */
    private void checkEventSources(LevelData level) {
        boolean scripted = level.objectives()
                .escort()
                .flatMap(LevelData.Escort::scriptedLoss)
                .isPresent();
        boolean cloaked = level.waves().stream()
                .flatMap(wave -> wave.groupList().stream())
                .anyMatch(group -> content.enemies().containsKey(group.enemy())
                        && content.enemy(group.enemy()).cloak().isPresent());
        boolean loops = level.waves().stream()
                .anyMatch(wave -> wave.loopBack().isPresent()
                        && wave.groupList().stream()
                                .anyMatch(group -> group.formation().equals("swarm")));
        // M7 part D: an aura enemy (the Choir Herald) in a wave.
        boolean sings = level.waves().stream()
                .flatMap(wave -> wave.groupList().stream())
                .anyMatch(group -> content.enemies().containsKey(group.enemy())
                        && content.enemy(group.enemy()).attacks().stream()
                                .anyMatch(attack -> attack.aura().isPresent()));
        // M7 part B: a burrowing enemy (the Burrower) in a wave.
        boolean burrows = level.waves().stream()
                .flatMap(wave -> wave.groupList().stream())
                .anyMatch(group -> content.enemies().containsKey(group.enemy())
                        && content.enemy(group.enemy()).movement().burrow().isPresent());
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.CueEvent event = level.radio().get(i).event().orElse(null);
            String field = "radio[" + i + "].event";
            if (event == LevelData.CueEvent.SCRIPTED_LOSS && !scripted) {
                problem(
                        level,
                        field,
                        "scripted-loss in a level without a scripted loss (objectives.escort.scripted_loss)");
            } else if (event == LevelData.CueEvent.FIRST_DECLOAK && !cloaked) {
                problem(level, field, "first-decloak in a level without a cloaked enemy");
            } else if (event == LevelData.CueEvent.FIRST_LOOP_BACK && !loops) {
                problem(level, field, "first-loop-back in a level without a swarm that loops back");
            } else if (event == LevelData.CueEvent.FIRST_SURFACING && !burrows) {
                problem(level, field, "first-surfacing in a level without a burrowing enemy");
            } else if (event == LevelData.CueEvent.FIRST_RING && !sings) {
                problem(level, field, "first-ring in a level without an aura enemy");
            } else if (event == LevelData.CueEvent.FIRST_GEYSER && level.vents().isEmpty()) {
                problem(level, field, "first-geyser in a level without vents");
            } else if (event == LevelData.CueEvent.TARGET_DESTROYED
                    && level.objectives()
                            .secondary()
                            .flatMap(LevelData.Secondary::escapes)
                            .isEmpty()) {
                problem(level, field, "target-destroyed in a level without an escapes secondary");
            } else if (event == LevelData.CueEvent.WATERLINE
                    && level.waterlines().isEmpty()) {
                problem(level, field, "waterline in a level without a waterline (a section changing the medium)");
            } else if (event == LevelData.CueEvent.FIRST_RAY
                    && AbyssChecks.firstRise(content, level).isEmpty()) {
                problem(level, field, "first-ray in a level without a rising unit (movement.rise)");
            } else if ((event == LevelData.CueEvent.BOSS_ARRIVAL || event == LevelData.CueEvent.BOSS_DIVE)
                    && level.boss().isEmpty()) {
                problem(level, field, "boss-arrival or boss-dive in a level without a boss");
            }
        }
        if (!level.noSurfaceZones().isEmpty() && !burrows) {
            problem(level, "no_surface", "no-surface zones in a level without a burrowing enemy");
        }
        for (int i = 0; i < level.noSurfaceZones().size(); i++) {
            checkTime(
                    level,
                    "no_surface[" + i + "].t",
                    level.noSurfaceZones().get(i).t());
        }
        LevelData.Music music = level.music();
        if (music.duck().isPresent() && !scripted) {
            problem(level, "music.duck.on", "scripted-loss in a level without a scripted loss");
        }
        for (int i = 0; i < music.ambienceChangeList().size(); i++) {
            int section = music.ambienceChangeList().get(i).section();
            if (section > level.sections().size()) {
                problem(
                        level,
                        "music.ambience_changes[" + i + "].section",
                        "the level has " + level.sections().size() + " sections, not " + section);
            }
        }
    }

    /** The units of {@code group} of {@code wave} on {@code difficulty}, as {@link SimSpecs} plans them. */
    private int countOn(LevelData.Wave wave, LevelData.Group group, Difficulty difficulty) {
        Optional<LevelData.Change> change =
                switch (difficulty) {
                    case EASY -> wave.easy();
                    case MEDIUM -> Optional.empty();
                    case HARD -> wave.hard();
                };
        return change.flatMap(LevelData.Change::count)
                .orElseGet(() -> content.difficulty().formationSize(group.count(), difficulty));
    }

    /** Whether the enemy {@code slug} has a pounce. */
    private boolean pounces(String slug) {
        return content.enemies().containsKey(slug)
                && content.enemy(slug).attacks().stream()
                        .anyMatch(attack -> attack.pounce().isPresent());
    }

    /**
     * Part G's level rules (Level 07): a tow reveals a secret of the level and enters within it, and
     * a parts objective names parts and a phase of the level's boss.
     */
    private void checkLevelRules(LevelData level) {
        Set<String> secrets =
                level.secrets().stream().map(LevelData.Secret::name).collect(Collectors.toSet());
        List<LevelData.Tow> tows = level.tows().orElse(List.of());
        for (int i = 0; i < tows.size(); i++) {
            LevelData.Tow tow = tows.get(i);
            checkTime(level, "tows[" + i + "].t", tow.t());
            if (!secrets.contains(tow.reveals())) {
                problem(level, "tows[" + i + "].reveals", "no secret '" + tow.reveals() + "'");
            }
            if (tow.x() < 0 || tow.x() > PlayField.WIDTH) {
                problem(level, "tows[" + i + "].x", "x=" + tow.x() + " is outside the play field");
            }
        }
        level.objectives().secondary().ifPresent(secondary -> {
            secondary.parts().ifPresent(parts -> bosses.checkBossParts(level, "objectives.secondary.parts", parts));
            secondary.before().ifPresent(before -> level.boss()
                    .filter(boss -> content.enemies().containsKey(boss.enemy()))
                    .flatMap(boss -> content.enemy(boss.enemy()).boss())
                    .filter(script -> script.phases().stream()
                            .noneMatch(phase -> phase.name().equals(before)))
                    .ifPresent(
                            script -> problem(level, "objectives.secondary.before", "no boss phase '" + before + "'")));
        });
    }

    /**
     * M5 part E: a destructible's group is its own, not one of the objectives' (they never count it),
     * and a placed pickup is dropped by its last destructible (Level 11's floating containers).
     */
    private void checkDestructibleGroup(LevelData level, String field, String group) {
        if (level.objectives().groups().contains(group)) {
            problem(level, field, "'" + group + "' is an objectives' group: a destructible's group is its own");
        }
        boolean carried = java.util.stream.Stream.concat(
                        level.pickups().stream(),
                        java.util.stream.Stream.of(
                                        level.difficulty().easy(),
                                        level.difficulty().hard())
                                .flatMap(Optional::stream)
                                .flatMap(variant -> variant.extraPickups().orElse(List.of()).stream()))
                .filter(LevelData.PlacedPickup::carried)
                .anyMatch(
                        pickup -> pickup.carrier().group().filter(group::equals).isPresent());
        if (!carried) {
            problem(level, field, "no pickup is dropped by group '" + group + "'");
        }
    }

    private void checkCarriers(
            LevelData level, String field, List<LevelData.PlacedPickup> pickups, Set<Double> waveTimes) {
        for (int i = 0; i < pickups.size(); i++) {
            if (!pickups.get(i).carried()) {
                // M5 part F: a placed pickup (PartFRules checks its time and x).
                continue;
            }
            LevelData.Carrier carrier = pickups.get(i).carrier();
            if (carrier.parts().isPresent()) {
                bosses.checkBossParts(
                        level,
                        field + "[" + i + "].dropped_by.parts",
                        carrier.parts().get());
                continue;
            }
            if (carrier.group().isPresent()) {
                if (!SimSpecs.destructibleGroups(level).contains(carrier.group().get())) {
                    checkGroup(
                            level,
                            field + "[" + i + "].dropped_by.group",
                            carrier.group().get());
                }
                continue;
            }
            double wave = carrier.waveT();
            if (!waveTimes.contains(wave)) {
                problem(level, field + "[" + i + "].dropped_by.wave", "no wave at t=" + wave);
            }
        }
    }

    /**
     * A destructible or trigger lies on {@code ground}; M5 part E (Level 11's sunken pod): a trigger
     * may lie on {@code sub}, under the water of a level with {@code water: true}, where only
     * torpedoes reach it.
     */
    private void checkTargetLayer(LevelData level, String field, LevelData.GroundTarget target) {
        target.layer().ifPresent(layer -> {
            if (layer.equals(SUB)) {
                if (target.hits().isEmpty()) {
                    problem(level, field + ".layer", "only a trigger (hits) lies under the water");
                } else if (!level.overWater()) {
                    problem(level, field + ".layer", "a trigger on sub needs a level with water: true");
                }
            } else if (layer.equals("low-air") && target.drift().isPresent()) {
                // M5 part F: a trigger drifting on low-air (Level 12's canister), the record checks it is one.
                return;
            } else if (!layer.equals("ground")) {
                problem(
                        level,
                        field + ".layer",
                        "a destructible or trigger lies on ground (or a trigger on sub, or a drifting one on low-air),"
                                + " not " + layer);
            }
        });
    }
}
