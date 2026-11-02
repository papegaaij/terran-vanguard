package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M10 part G's static checks (design/campaign Level 41 Callisto Fortress; design/enemies/ground/shield-pylon, The pylon
 * gates and the placement rule; design/enemies/ground/halo-platform): a level's held set piece, its gates' own halt
 * cycles, the archive cores' uplinks and the {@code files} secondary, the halt, file and halo-destroyed radio events,
 * and the gates' dome geometry on their halted screens.
 *
 * <p>A held set piece (a {@code boss} of {@code kind: held}) is the level's boss in a level of several held gates, one
 * gate's {@code holds} naming its slug, arriving inside that gate's section (no {@code incoming}: it has no warning). A
 * section's {@code halt_cycle} never beside the level's {@code halt_waves}. A {@code files} secondary counts no more
 * files than the level has archive cores, names itself (the briefing's bonus line) and its tracker. A {@code halt} cue
 * names {@code gate-n} of the level's held gates or the held piece; a {@code file} cue a count of the cores; a {@code
 * halo-destroyed} cue needs the held piece. On each pylon gate's halted screen (the clock standing at the gate's end) at
 * the widest dome of any difficulty: every pylon in the upper half and outside every dome, every dome's centre at y ≤
 * 360 (140 px above the ship's band), every archive core on that screen outside every dome.
 */
final class FortressChecks {
    private FortressChecks() {}

    /** The pylon placement rule's lowest dome centre on a halted screen, px below the top edge (design/enemies). */
    static final double DOME_LOWEST = 360;

    /**
     * The slug of the level's held set piece: its boss when that is known and of {@code kind: held}; empty otherwise.
     */
    static Optional<String> heldPiece(Content content, LevelData level) {
        return level.boss()
                .map(LevelData.BossPlacement::enemy)
                .filter(content.enemies()::containsKey)
                .filter(slug -> content.enemy(slug)
                        .boss()
                        .filter(EnemyData.BossData::held)
                        .isPresent());
    }

    /** A level's M10 part G keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        List<LevelData.Section> sections = level.sections();
        List<Integer> gates = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).held()) {
                gates.add(i);
            }
        }
        Optional<String> piece = heldPiece(content, level);
        piece.ifPresent(slug -> checkPiece(level, slug, gates, problem));
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).haltCycle().isPresent() && level.haltWaves().isPresent()) {
                problem.accept(
                        "sections[" + i + "].halt_cycle",
                        "a gate's own halt cycle, never beside the level's one (halt_waves)");
            }
        }
        int cores = (int) level.groundTargets().stream()
                .filter(target -> target.uplink().isPresent())
                .mapToLong(target -> target.at().size())
                .sum();
        level.objectives().secondary().filter(s -> s.files().isPresent()).ifPresent(files -> {
            if (files.files().get() > cores) {
                problem.accept(
                        "objectives.secondary.files",
                        "a files secondary counts the level's archive cores: "
                                + files.files().get() + " of " + cores);
            }
            if (files.name().isEmpty() || files.label().isEmpty()) {
                problem.accept("objectives.secondary", "a files secondary names itself (name) and its tracker (label)");
            }
        });
        checkRadio(level, gates.size(), piece, cores, problem);
        List<BackdropData.Door> doors = level.backdrop().doors().orElse(List.of());
        for (int i = 0; i < doors.size(); i++) {
            BackdropData.Door door = doors.get(i);
            BackdropData.Piece drawn = level.backdrop().pieces().get(door.piece());
            if (door.gate() > gates.size()
                    || drawn == null
                    || drawn.layer() != BackdropLayer.GROUND
                    || drawn.frames().orElse(1) < 2) {
                problem.accept(
                        "backdrop.doors[" + i + "]",
                        "a gate's doors are an animated ground piece of a held gate (1 to " + gates.size() + ")");
            }
        }
        for (int g = 0; g < gates.size(); g++) {
            checkDomes(content, level, gates.get(g), problem);
        }
    }

    private static void checkPiece(
            LevelData level, String slug, List<Integer> gates, BiConsumer<String, String> problem) {
        LevelData.BossPlacement boss = level.boss().orElseThrow();
        int held = -1;
        for (int i : gates) {
            held = level.sections().get(i).holds().filter(slug::equals).isPresent() ? i : held;
        }
        if (gates.size() < 2 || held < 0) {
            problem.accept(
                    "boss",
                    "a held set piece holds a gate of a held-gates level (its section's holds names '" + slug + "')");
            return;
        }
        double start = level.sectionStart(held);
        double end = level.sections().get(held).end();
        if (!(boss.t() >= start && boss.t() < end) || boss.section() != held + 1) {
            problem.accept(
                    "boss.t",
                    "a held set piece arrives inside its gate's section (section " + (held + 1) + ", " + start + " to "
                            + end + " s)");
        }
        if (boss.incoming().isPresent()) {
            problem.accept("boss.incoming", "a held set piece has no boss warning");
        }
    }

    private static void checkRadio(
            LevelData level, int gates, Optional<String> piece, int cores, BiConsumer<String, String> problem) {
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            LevelData.CueEvent event = cue.event().orElse(null);
            String field = "radio[" + i + "]";
            if (event == LevelData.CueEvent.HALT) {
                String subject = cue.subject().orElseThrow();
                boolean gate = gates >= 2
                        && (piece.filter(subject::equals).isPresent()
                                || (subject.matches("gate-[1-9][0-9]*")
                                        && Integer.parseInt(subject.substring(5)) <= gates));
                if (!gate) {
                    problem.accept(
                            field + ".subject",
                            "a halt cue names a held gate (gate-1 … gate-" + gates + ") or the held set piece, was '"
                                    + subject + "'");
                }
            } else if (event == LevelData.CueEvent.FILE
                    && Integer.parseInt(cue.subject().orElseThrow()) > cores) {
                problem.accept(field + ".subject", "a file cue counts the level's " + cores + " archive cores");
            } else if (event == LevelData.CueEvent.HALO_DESTROYED && piece.isEmpty()) {
                problem.accept(field + ".event", "a halo-destroyed cue needs the level's held set piece");
            }
        }
    }

    /** A point on a halted screen, px from the left and below the top edge. */
    private record At(double x, double y) {
        double to(At other) {
            return Math.hypot(x - other.x, y - other.y);
        }
    }

    private static void checkDomes(Content content, LevelData level, int gate, BiConsumer<String, String> problem) {
        LevelData.Section section = level.sections().get(gate);
        String group = section.holds().orElseThrow();
        double halt = level.scrollAt(section.end());
        String field = "sections[" + gate + "]";
        List<At> pylons = new ArrayList<>();
        List<At> domes = new ArrayList<>();
        double widest = 0;
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.enemy().isEmpty()
                    || target.group().filter(group::equals).isEmpty()
                    || !content.enemies().containsKey(target.enemy().get())) {
                continue;
            }
            EnemyData unit = content.enemy(target.enemy().get());
            if (unit.dome().isEmpty()) {
                continue;
            }
            EnemyData.Dome dome = unit.dome().get();
            double radius = dome.radius();
            for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
                radius = Math.max(
                        radius,
                        UnitRules.hook(unit, difficulty)
                                .flatMap(EnemyData.Hook::dome)
                                .flatMap(EnemyData.DomeChange::radius)
                                .orElse(dome.radius()));
            }
            widest = Math.max(widest, radius);
            double crown = Math.toRadians(target.crown().orElse(0.0));
            for (LevelData.Placement placement : placements(target)) {
                At pylon = onScreen(level, halt, placement, unit.size().height());
                pylons.add(pylon);
                At centre = new At(
                        pylon.x() + dome.offset() * Math.sin(crown), pylon.y() - dome.offset() * Math.cos(crown));
                domes.add(centre);
                if (pylon.y() > PlayField.HEIGHT / 2.0) {
                    problem.accept(
                            field,
                            "a gate's pylon stands in the halted screen's upper half, was y " + Math.round(pylon.y()));
                }
                if (centre.y() > DOME_LOWEST) {
                    problem.accept(
                            field,
                            "a dome's centre lies at y ≤ " + DOME_LOWEST + " on the halted screen, was "
                                    + Math.round(centre.y()));
                }
            }
        }
        for (At pylon : pylons) {
            for (At dome : domes) {
                if (pylon.to(dome) < widest) {
                    problem.accept(
                            field,
                            "a pylon stands outside every dome (" + Math.round(pylon.to(dome)) + " px from one)");
                }
            }
        }
        for (LevelData.GroundTarget target : level.groundTargets()) {
            if (target.uplink().isEmpty()) {
                continue;
            }
            for (LevelData.Placement placement : target.at()) {
                At core = onScreen(
                        level, halt, placement, target.size().orElseThrow().height());
                if (core.y() < 0 || core.y() > PlayField.HEIGHT) {
                    continue;
                }
                for (At dome : domes) {
                    if (core.to(dome) < widest) {
                        problem.accept(
                                "ground_targets",
                                "an archive core lies outside every dome (" + Math.round(core.to(dome))
                                        + " px from one of " + section.name() + "'s)");
                    }
                }
            }
        }
    }

    /** A nest's placements on every difficulty (the easy and hard lists beside medium's). */
    private static List<LevelData.Placement> placements(LevelData.GroundTarget target) {
        List<LevelData.Placement> all = new ArrayList<>(target.at());
        for (Optional<LevelData.Placements> change : List.of(target.easy(), target.hard())) {
            change.flatMap(LevelData.Placements::at).ifPresent(all::addAll);
        }
        return all;
    }

    /**
     * Where a placement entering at its {@code t} (its centre {@code height / 2} above the top edge) stands once the
     * ground has scrolled {@code halt} px: px from the left and below the top edge.
     */
    private static At onScreen(LevelData level, double halt, LevelData.Placement placement, double height) {
        return new At(placement.x(), halt - level.scrollAt(placement.t()) - height / 2);
    }
}
