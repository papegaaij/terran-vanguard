package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.EnemySpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.LevelScript;
import vanguard.sim.PlayField;
import vanguard.sim.Range;
import vanguard.sim.Section;

/**
 * M7 part B's rules for the simulation (design/campaign Level 15 Olympus Descent, design/enemies/ground/burrower,
 * design/enemies/air/dust-kite; M7 = a and the user's Q1 = a of 2026-10-14, the defaults pending the user's review in
 * round 38): a Burrower's {@code burrow} with its rings and its difficulty's change, a Dust Kite's triggered swoop,
 * the level's moving sections easing in over their {@code ramp}, its no-surface zones and {@code launch: drop}.
 */
final class MarsRules {
    /**
     * A Burrower's longest life before it swims off the bottom edge even with surfacings left, s (not while it is
     * up): no stat-block key; step B1's default, pending the user's review in round 38.
     */
    static final double BURROW_LIFE_SECONDS = 30;

    /**
     * A triggered swoop's sideways bend, px (the curve's control point beside its start, halfway down): the stat
     * block's "shallow curve" has no number; step B1's default, pending the user's review in round 38.
     */
    static final double SWOOP_BEND = 60;

    private MarsRules() {}

    /** Whether {@code attack} is a burrowing unit's ring: fired by its burrow, not a gun. */
    static boolean burrowRing(EnemyData enemy, EnemyData.Attack attack) {
        return enemy.movement().burrow().isPresent() && attack.pattern().equals("ring");
    }

    /**
     * {@code spec} with the stat block's {@code burrow} at {@code difficulty}: the hook's surfaced time and rings
     * instead; each ring of the attack's bullets (the hook's {@code attacks.<name>.count}, or the ring's own count),
     * its speed by the bullet-speed lever; the first ring and an optional second. A unit without a burrow is
     * returned as it is.
     */
    static EnemySpec burrower(
            Content content, EnemyData enemy, Optional<EnemyData.Hook> hook, Difficulty difficulty, EnemySpec spec) {
        Optional<EnemyData.Burrow> data = enemy.movement().burrow();
        if (data.isEmpty()) {
            return spec;
        }
        EnemyData.Burrow burrow = data.get();
        EnemyData.Attack attack = enemy.attacks().stream()
                .filter(a -> burrowRing(enemy, a))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(enemy.name() + ": a burrow fires a ring attack"));
        Optional<EnemyData.BurrowChange> change = hook.flatMap(EnemyData.Hook::burrow);
        int count = attack.name()
                .flatMap(name -> hook.flatMap(EnemyData.Hook::attacks).map(changes -> changes.get(name)))
                .flatMap(EnemyData.AttackChange::count)
                .orElse(attack.count().orElseThrow());
        double speed = attack.speed().orElseThrow()
                * content.difficulty().enemyBulletSpeed().of(difficulty);
        double damage = UnitRules.bulletDamage(content, attack.bullet().orElseThrow());
        List<EnemySpec.BurrowRing> rings = new ArrayList<>();
        for (EnemyData.Ring ring : change.flatMap(EnemyData.BurrowChange::rings).orElse(burrow.rings())) {
            rings.add(new EnemySpec.BurrowRing(ring.count().orElse(count), speed, damage, ring.at()));
        }
        if (rings.size() > 2) {
            throw new IllegalArgumentException(enemy.name() + ": a burrow fires at most two rings a surfacing");
        }
        EnemyData.Spot spot = burrow.spot();
        return spec.withBurrow(Optional.of(new EnemySpec.Burrow(
                burrow.swim(),
                new EnemySpec.SurfaceSpot(
                        spot.ahead().min(), spot.ahead().max(), spot.side(), spot.edge(), spot.clear()),
                new Range(burrow.travel().min(), burrow.travel().max()),
                burrow.bulge(),
                change.flatMap(EnemyData.BurrowChange::surfaced).orElse(burrow.surfaced()),
                burrow.dive(),
                burrow.surfacings(),
                rings.getFirst(),
                rings.size() > 1 ? Optional.of(rings.get(1)) : Optional.empty(),
                burrow.stagger(),
                burrow.maxUp(),
                BURROW_LIFE_SECONDS)));
    }

    /**
     * {@code spec} with the stat block's triggered {@code swoop} (the Dust Kite's): its trigger, its top speed (the
     * hook's {@code swoop_speed} instead) and {@link #SWOOP_BEND}. A unit without a triggered swoop is returned as it
     * is (an untriggered swoop is an entry curve).
     */
    static EnemySpec kite(EnemyData enemy, Optional<EnemyData.Hook> hook, EnemySpec spec) {
        Optional<EnemyData.Swoop> swoop =
                enemy.movement().swoop().filter(s -> s.trigger().isPresent());
        if (swoop.isEmpty()) {
            return spec;
        }
        EnemyData.Swoop data = swoop.get();
        return spec.withSwoop(Optional.of(new EnemySpec.Swoop(
                data.trigger().orElseThrow(),
                hook.flatMap(EnemyData.Hook::swoopSpeed).orElse(data.topSpeed().orElseThrow()),
                SWOOP_BEND)));
    }

    /**
     * The level's sections for the simulation: a timed halt with its {@code ramp} (the default 2 s if not given),
     * a moving section after the first easing in over its {@code ramp} ({@link LevelData.Section#easeSeconds()}); M7
     * part H: a moving arena of a breaching boss crawls ({@link Section#crawls()}), easing in over its {@code ramp}.
     */
    static List<Section> sections(Content content, LevelData level) {
        List<LevelData.Section> sections = level.sections();
        // M7 part H (Level 21): a moving arena crawls (its ground scrolls on while the clock is held) when the level's
        // boss breaches (ColossusRules).
        boolean breaching = ColossusRules.breaching(content, level);
        List<Section> out = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            LevelData.Section section = sections.get(i);
            double speed = section.speed().orElse(level.scrollSpeed());
            // The simulation eases only a section that follows a moving one (not an arena, not a halt). M7 part G: a
            // reversed section (a negative speed) moves too (Level 20's eases between its retreat speeds).
            boolean afterMoving = i > 0
                    && !sections.get(i - 1).isArena()
                    && sections.get(i - 1).speed().orElse(level.scrollSpeed()) != 0;
            // M7 part F (Level 19's wreck crawl): a moving section right after an arena with its own ramp eases up
            // from the halt over it (no other level gives one there: their 1 s ramp stays).
            boolean afterArena =
                    i > 0 && sections.get(i - 1).isArena() && section.ramp().isPresent();
            out.add(new Section(
                    section.end(),
                    speed,
                    section.isArena(),
                    section.rampSeconds(),
                    afterMoving || afterArena ? section.easeSeconds() : 0,
                    // M7 part F: an arena's hold after its boss's death chain (Level 19's wreck beat).
                    section.holdAfterBossSeconds(),
                    breaching && section.isArena() && speed > 0));
        }
        return out;
    }

    /**
     * The level's no-surface zones for the simulation: a zone {@code size} px across centred on its {@code x}, and
     * the level times its near (bottom) and far (top) rows cross the top edge: its centre passes the middle of the
     * screen at its {@code t}, so its rows lie half its height either side of that ground row, and a row crosses the
     * top edge half a play field's height of scroll before it passes the middle (the inverse of {@link
     * LevelData#scrollAt}, from 0 s on).
     */
    static List<GroundSpec.NoSurface> noSurface(LevelData level) {
        List<GroundSpec.NoSurface> zones = new ArrayList<>();
        for (LevelData.NoSurface zone : level.noSurfaceZones()) {
            double centre = level.scrollAt(zone.t());
            double half = zone.size().height() / 2.0;
            double from = timeAtScroll(level, centre - half - PlayField.HEIGHT / 2.0);
            double to = timeAtScroll(level, centre + half - PlayField.HEIGHT / 2.0);
            double halfWidth = zone.size().width() / 2.0;
            zones.add(new GroundSpec.NoSurface(from, to, zone.x() - halfWidth, zone.x() + halfWidth));
        }
        return zones;
    }

    /** When the ground has scrolled {@code scroll} px ({@link LevelData#scrollAt}), s; 0 for a scroll at or before 0. */
    static double timeAtScroll(LevelData level, double scroll) {
        if (scroll <= 0) {
            return 0;
        }
        double low = 0;
        double high = Math.max(1, level.seconds());
        while (level.scrollAt(high) < scroll) {
            high *= 2;
        }
        for (int i = 0; i < 60; i++) {
            double mid = (low + high) / 2;
            if (level.scrollAt(mid) < scroll) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return high;
    }

    /** {@code script} with the level's no-surface zones and its {@code launch: drop}, when it has them. */
    static LevelScript script(LevelData level, LevelScript script) {
        LevelScript out = script;
        if (!level.noSurfaceZones().isEmpty()) {
            out = out.toBuilder()
                    .ground(out.ground().withNoSurface(noSurface(level)))
                    .build();
        }
        if (level.dropLaunch()) {
            out = out.toBuilder().launchDrop(true).build();
        }
        return out;
    }
}
