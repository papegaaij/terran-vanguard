package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * M7 part G's static checks (design/campaign Level 20, Retreat from Hellas; design/enemies/air/mote-swarm, the pursuit;
 * design/player/wingmen, Rear-guard drone).
 *
 * <p>A level's: a reversed section (a negative speed) never first, and only in a level without terrain (walls, gates),
 * a road, belts, vents, water, no-surface zones, cranes, debris, sleds, rocks, darkness, sondes, hold zones, a
 * collapse, tows, an escort, a convoy, a defend primary, ground allies, set pieces, a boss's arena, a timed halt or
 * segment chains (the travel frame's scope, as the simulation's {@code LevelScript} cross-check: what Level 20
 * needs); no ground target entering in a reversed stretch carries an {@code arc}
 * (the travel frame turns a nest's arc); a {@code pursuit} wave enters {@code from: rear} on every difficulty while
 * the travel is reversed, its unit has a flock and a {@code pursuit} block, its {@code break_at} after its entry,
 * inside the level and in a reversed stretch, and no two pursuits overlap on any difficulty; a front wave's {@code
 * warning} without a {@code loop_back} (the BOTTOM lesson) only while the travel is reversed.
 *
 * <p>A unit's: a {@code pursuit} block only with a flock and the {@code pursuit} formation listed, the hook {@code
 * pursuit} only with the block. A weapon's: a trailing drone's bay is a wing pod with the {@code rear} trait.
 */
final class RetreatChecks {
    private RetreatChecks() {}

    /** A unit's M7 part G keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        if (enemy.pursuit().isPresent()) {
            if (enemy.movement().flock().isEmpty()) {
                problem.accept("pursuit", "a pursuit is a flock's (movement.flock)");
            }
            if (enemy.formations().stream().noneMatch(use -> use.name().equals(LevelData.Wave.PURSUIT))) {
                problem.accept("formations", "its pursuit block flies 'pursuit', which its formations do not list");
            }
        }
        for (var hook : List.of(
                enemy.difficulty().flatMap(EnemyData.Hooks::easy),
                enemy.difficulty().flatMap(EnemyData.Hooks::hard))) {
            if (hook.flatMap(EnemyData.Hook::pursuit).isPresent()
                    && enemy.pursuit().isEmpty()) {
                problem.accept("difficulty.pursuit", "a pursuit's lunge or tell without a pursuit block");
            }
        }
    }

    /** A weapon's M7 part G keys: a trailing drone's bay. */
    static void weapon(WeaponData weapon, BiConsumer<String, String> problem) {
        if (weapon.drone().filter(WeaponData.Drone::trails).isEmpty()) {
            return;
        }
        if (weapon.slot() != WeaponData.Slot.WING || !weapon.pod().orElse(false)) {
            problem.accept("slot", "a trailing drone's bay is a wing pod (slot: wing, pod: true)");
        }
        if (!weapon.traits().contains("rear")) {
            problem.accept("traits", "a drone that fires back has the rear trait");
        }
    }

    /** A level's M7 part G keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.hasReversal()) {
            reversal(content, level, problem);
        }
        List<double[]> pursuits = new ArrayList<>();
        for (int i = 0; i < level.waves().size(); i++) {
            LevelData.Wave wave = level.waves().get(i);
            String field = "waves[" + i + "]";
            if (wave.pursues()) {
                pursuit(content, level, wave, field, problem);
                pursuits.add(
                        new double[] {wave.t(), wave.pursuit().orElseThrow().breakAt(), i});
            }
            boolean front = wave.from() == LevelData.Entry.FRONT;
            boolean warned = wave.warning().isPresent()
                    || wave.easy().flatMap(LevelData.Change::warning).isPresent()
                    || wave.hard().flatMap(LevelData.Change::warning).isPresent();
            if (front && warned && wave.loopBack().isEmpty() && level.travelAt(wave.t()) > 0) {
                problem.accept(
                        field + ".warning",
                        "a front wave is warned only in a reversed section (the BOTTOM lesson) or for its loop-back");
            }
        }
        for (int a = 0; a < pursuits.size(); a++) {
            for (int b = a + 1; b < pursuits.size(); b++) {
                double[] one = pursuits.get(a);
                double[] two = pursuits.get(b);
                if (one[0] < two[1] && two[0] < one[1]) {
                    problem.accept(
                            "waves[" + (int) two[2] + "]",
                            "one pursuit at a time: it overlaps waves[" + (int) one[2] + "]");
                }
            }
        }
    }

    private static void reversal(Content content, LevelData level, BiConsumer<String, String> problem) {
        if (level.sections().getFirst().reversed()) {
            problem.accept("sections[0].speed", "the level launches forward: its first section does not reverse");
        }
        List<String> features = new ArrayList<>();
        addIf(features, level.terrain().isPresent(), "terrain");
        addIf(features, level.road().isPresent(), "road");
        addIf(features, level.belts().isPresent(), "belts");
        addIf(features, level.vents().isPresent(), "vents");
        addIf(features, level.overWater(), "water");
        addIf(features, level.holds().filter(holds -> !holds.isEmpty()).isPresent(), "holds");
        addIf(features, level.boss().isPresent(), "boss");
        addIf(features, level.sections().stream().anyMatch(LevelData.Section::isArena), "arena");
        addIf(features, level.sections().stream().anyMatch(LevelData.Section::timedHalt), "timed halt");
        addIf(features, level.convoy().isPresent(), "convoy");
        addIf(features, level.allies().isPresent(), "allies");
        addIf(features, level.setPieces().filter(pieces -> !pieces.isEmpty()).isPresent(), "set_pieces");
        // As the simulation's cross-check (LevelScript, step G1): nothing else placed by the scroll's time.
        addIf(features, !level.noSurfaceZones().isEmpty(), "no_surface");
        addIf(features, level.cranes().filter(cranes -> !cranes.isEmpty()).isPresent(), "cranes");
        addIf(features, level.debris().isPresent(), "debris");
        addIf(features, level.sleds().isPresent(), "sleds");
        addIf(features, level.rocks().isPresent(), "rocks");
        addIf(features, level.darkness().isPresent(), "darkness");
        addIf(features, level.sondes().isPresent(), "sondes");
        addIf(features, level.collapse().isPresent(), "collapse");
        addIf(features, level.tows().filter(tows -> !tows.isEmpty()).isPresent(), "tows");
        addIf(features, level.objectives().escort().isPresent(), "an escort");
        addIf(features, level.objectives().defend().isPresent(), "a defend primary");
        addIf(
                features,
                level.waves().stream()
                        .flatMap(wave -> wave.groupList().stream())
                        .anyMatch(group -> content.enemies().containsKey(group.enemy())
                                && content.enemy(group.enemy()).segmentChain().isPresent()),
                "segment chains");
        if (!features.isEmpty()) {
            problem.accept(
                    "sections",
                    "a level that scrolls backwards has no " + String.join(", ", features)
                            + " (the travel frame's scope, Level 20)");
        }
        for (int i = 0; i < level.groundTargets().size(); i++) {
            LevelData.GroundTarget target = level.groundTargets().get(i);
            if (target.arc().isEmpty()) {
                continue;
            }
            for (LevelData.Placement at : target.at()) {
                if (level.travelAt(at.t()) < 0) {
                    problem.accept(
                            "ground_targets[" + i + "].arc",
                            "the travel frame turns a nest's arc in a reversed section: no arc key there");
                    break;
                }
            }
        }
    }

    private static void pursuit(
            Content content, LevelData level, LevelData.Wave wave, String field, BiConsumer<String, String> problem) {
        LevelData.Pursuit pursuit = wave.pursuit().orElseThrow();
        boolean rear = wave.from() == LevelData.Entry.REAR
                && wave.easy()
                        .flatMap(LevelData.Change::from)
                        .filter(f -> f != LevelData.Entry.REAR)
                        .isEmpty()
                && wave.hard()
                        .flatMap(LevelData.Change::from)
                        .filter(f -> f != LevelData.Entry.REAR)
                        .isEmpty();
        if (!rear) {
            problem.accept(field + ".from", "a pursuit presses from behind: from: rear on every difficulty");
        }
        if (level.travelAt(wave.t()) > 0) {
            problem.accept(field + ".t", "a pursuit enters while the scroll is reversed (behind = the top edge)");
        }
        if (pursuit.breakAt() <= wave.t() || pursuit.breakAt() >= level.seconds()) {
            problem.accept(field + ".pursuit.break_at", "after its entry and before the level's end");
        } else if (level.travelAt(pursuit.breakAt()) > 0) {
            problem.accept(field + ".pursuit.break_at", "the pursuit breaks off while the scroll is still reversed");
        }
        String slug = wave.groupList().getFirst().enemy();
        if (!content.enemies().containsKey(slug)) {
            return;
        }
        EnemyData enemy = content.enemy(slug);
        Optional<EnemyData.Pursuit> defaults = enemy.pursuit();
        if (enemy.movement().flock().isEmpty() || defaults.isEmpty()) {
            problem.accept(
                    field + ".enemy", "a pursuit's unit has a flock and a pursuit block, '" + slug + "' has not");
            return;
        }
        Span band = pursuit.band().orElse(defaults.get().band());
        if (band.max() >= vanguard.sim.PlayField.HEIGHT) {
            problem.accept(field + ".pursuit.band", "inside the play field");
        }
        double tell = pursuit.tell().orElse(defaults.get().tell());
        double every = pursuit.lungeEvery().orElse(defaults.get().lungeEvery());
        if (tell >= every) {
            problem.accept(field + ".pursuit.tell", "shorter than the time between lunges");
        }
    }

    private static void addIf(List<String> out, boolean present, String name) {
        if (present) {
            out.add(name);
        }
    }
}
