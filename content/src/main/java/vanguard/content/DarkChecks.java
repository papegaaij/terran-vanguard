package vanguard.content;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import vanguard.sim.PlayField;

/**
 * M8 part F's static checks (design/campaign Level 26 Blackwater; design/enemies/naval/glow-angler; m8f-gaps.md §1.1–1.3,
 * §5, pending the user's review in round 55).
 *
 * <p>A unit's: a {@code lunge} is an {@code air} unit's that drifts ({@code drift}, a field's current), with exactly one
 * attack, its {@code ring} (a count, a bullet, a speed and the re-arm {@code interval}); a difficulty's {@code lunge}
 * change only with a lunge.
 *
 * <p>A level's: a {@code rook_loss} lies after the launch and its trails start before the level's end, in a level
 * without a boss (the simulation keeps no checkpoint of it); the look's {@code ramp} only on a section's own look,
 * within that section's time; the static {@code lights} on the level's rows (from the start's screen to the end); a
 * radio row's {@code static_lead} not before the level's start; a {@code unit: centre} carrier on a {@code field} wave of
 * one group; the music's {@code stem_times} inside the level.
 */
final class DarkChecks {
    private DarkChecks() {}

    /** A unit's M8 part F keys. */
    static void enemy(EnemyData enemy, BiConsumer<String, String> problem) {
        Optional<EnemyData.Lunge> lunge = enemy.movement().lunge();
        if (lunge.isPresent()) {
            if (!enemy.layer().equals("air")) {
                problem.accept("movement.lunge", "a lunging unit hangs in the water on air");
            }
            if (enemy.movement().drift().isEmpty()) {
                problem.accept(
                        "movement.lunge", "a lunging unit drifts with the water between lunges (movement.drift)");
            }
            List<EnemyData.Attack> attacks = enemy.attacks();
            Optional<EnemyData.Attack> ring = enemy.lungeRing();
            if (attacks.size() != 1 || ring.isEmpty()) {
                problem.accept("attacks", "a lunging unit's one attack is its ring, fired at the lunge's end");
            } else {
                EnemyData.Attack attack = ring.get();
                if (attack.count().isEmpty()
                        || attack.bullet().isEmpty()
                        || attack.speed().isEmpty()
                        || attack.interval().isEmpty()
                        || attack.within().isPresent()) {
                    problem.accept(
                            "attacks", "a lunge's ring has its count, bullet, speed and re-arm interval, and no reach");
                }
            }
        }
        for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
            if (UnitRules.hook(enemy, difficulty).flatMap(EnemyData.Hook::lunge).isPresent() && lunge.isEmpty()) {
                problem.accept("difficulty.lunge", "a lunge change without a lunge");
            }
        }
    }

    /** A level's M8 part F keys. */
    static void level(Content content, LevelData level, BiConsumer<String, String> problem) {
        double end = level.seconds();
        level.rookLoss().ifPresent(loss -> {
            if (loss.trails() < level.launchSeconds()) {
                problem.accept(
                        "rook_loss.trails", "Rook's loss comes after the launch (" + level.launchSeconds() + " s)");
            }
            if (loss.t() >= end) {
                problem.accept("rook_loss.t", "Rook's loss lies inside the level (it ends at " + end + " s)");
            }
            if (level.boss().isPresent()) {
                problem.accept("rook_loss", "Rook's loss only in a level without a boss (no checkpoint keeps it)");
            }
        });
        level.underwaterLook().ifPresent(look -> {
            if (look.ramp().isPresent()) {
                problem.accept("underwater_look.ramp", "only a section's own look ramps its ambient");
            }
            double bottom = -PlayField.HEIGHT;
            double top = level.scrollAt(end);
            List<SectionsBlock.Light> lights = look.lightList();
            for (int i = 0; i < lights.size(); i++) {
                double scroll = lights.get(i).scroll();
                if (scroll < bottom || scroll > top) {
                    problem.accept(
                            "underwater_look.lights[" + i + "]",
                            "a light lies on the level's rows (" + bottom + " to " + Math.round(top) + "), was "
                                    + scroll);
                }
            }
        });
        for (int i = 0; i < level.sections().size(); i++) {
            Optional<SectionsBlock.AmbientRamp> ramp =
                    level.sections().get(i).underwaterLook().flatMap(SectionsBlock.UnderwaterLookChange::ramp);
            if (ramp.isPresent()) {
                double start = level.sectionStart(i);
                double stop = level.sections().get(i).end();
                if (ramp.get().from() < start || ramp.get().until() > stop) {
                    problem.accept(
                            "sections[" + i + "].underwater_look.ramp",
                            "a ramp lies inside its section (" + start + "–" + stop + " s)");
                }
            }
        }
        for (int i = 0; i < level.radio().size(); i++) {
            LevelData.RadioCue cue = level.radio().get(i);
            if (cue.staticLead().isPresent()
                    && cue.t().orElse(0.0) - cue.staticLead().get() < 0) {
                problem.accept("radio[" + i + "].static_lead", "the static shows from the level's start on");
            }
        }
        for (int i = 0; i < level.pickups().size(); i++) {
            LevelData.PlacedPickup pickup = level.pickups().get(i);
            if (pickup.droppedBy()
                    .filter(by -> by.unit() == LevelData.CarrierUnit.CENTRE)
                    .isEmpty()) {
                continue;
            }
            double t = pickup.droppedBy().get().waveT();
            List<LevelData.Wave> at = level.waves().stream()
                    .filter(wave -> wave.t() == t)
                    .filter(wave -> wave.groupList().size() == 1
                            && wave.groupList().getFirst().formation().equals(WavesBlock.Wave.FIELD))
                    .toList();
            if (at.size() != 1) {
                problem.accept(
                        "pickups[" + i + "].dropped_by.unit",
                        "a centre unit is a field's: give the t of one field wave of one group, was " + t);
            }
        }
        List<LevelData.Music.StemTime> times = level.music().stemTimeList();
        for (int i = 0; i < times.size(); i++) {
            if (times.get(i).t() + times.get(i).fade() > end) {
                problem.accept("music.stem_times[" + i + "]", "a stem change lies inside the level (" + end + " s)");
            }
        }
    }
}
