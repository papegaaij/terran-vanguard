package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.DebugFit;
import vanguard.content.campaign.Flight;
import vanguard.sim.Armament;
import vanguard.sim.BulwarkSpec;
import vanguard.sim.Command;
import vanguard.sim.Enemy;
import vanguard.sim.EnemySpec;
import vanguard.sim.FlaresSpec;
import vanguard.sim.GroundSpec;
import vanguard.sim.GroundUnit;
import vanguard.sim.Hitbox;
import vanguard.sim.Layer;
import vanguard.sim.LevelScript;
import vanguard.sim.Loadout;
import vanguard.sim.MediumSpec;
import vanguard.sim.NaniteSpec;
import vanguard.sim.ObjectiveSpec;
import vanguard.sim.PlayField;
import vanguard.sim.Secondary;
import vanguard.sim.Section;
import vanguard.sim.SimStep;
import vanguard.sim.SonarSpec;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialLimits;
import vanguard.sim.SpecialSpec;
import vanguard.sim.WeaponSpec;
import vanguard.sim.WingmanSpec;

/**
 * M8 part A (step A4): Act 4's systems flown from the data through {@link SimSpecs} and {@link Flight}: the water rules
 * and a level's waterlines and special limits, the Harpoon Torpedoes from the nose with their splash, the Depth Charges'
 * barrel on its fuse, the {@code anti-sub} trait, the Sonar Pulse and the Decoy Flares with their settings, the
 * Pressure hull, the Auto-repair nanites and the Bulwark (owned, or the loaner while Rook is missing). The flights use
 * a test range: one endless section with dummies that neither move nor fire at the ship's x, under water or not.
 */
class Act4ArsenalFlightTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);
    private static final Catalogue CATALOGUE = Catalogue.complete(CONTENT);
    private static final int FIRE = Command.FIRE.bit();
    private static final int SPECIAL = Command.SPECIAL.bit();
    private static final double SHIP_X = PlayField.WIDTH / 2.0;
    private static final String HARPOON = "harpoon-torpedoes";
    private static final String CHARGES = "depth-charges";

    private static Loadout loadout(SimSpecs.FittedWeapon... weapons) {
        return ActThreeArsenalFlightTest.loadout(weapons);
    }

    /** The range at {@code scroll} px/s, in {@code medium}, with {@code units} (nothing hits the ship). */
    private static Sortie range(Loadout loadout, MediumSpec medium, double scroll, GroundUnit... units) {
        LevelScript level = LevelScript.builder(1, 1)
                .sections(List.of(new Section(3600, scroll)))
                .groundUnits(List.of(units))
                .objectives(ObjectiveSpec.NONE.withSecondary(new Secondary(1, 0)))
                .ground(GroundSpec.NONE)
                .medium(medium)
                .build();
        vanguard.sim.Rules rules = SimSpecs.rules(CONTENT, Level01Test.LEVEL, Difficulty.MEDIUM);
        return new Sortie(
                1,
                loadout,
                level,
                rules.withInvulnerableShip(),
                loadout.plating().maxArmour());
    }

    private static MediumSpec underwater() {
        return MediumSpec.underwater(SimSpecs.waterRules(CONTENT));
    }

    /** A dummy on {@code layer}: plenty of HP, {@code size} square. */
    private static EnemySpec dummy(Layer layer, double size) {
        return new EnemySpec(
                "dummy",
                1e6,
                new Hitbox(size, size),
                layer,
                0,
                false,
                0,
                0,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true);
    }

    /** The HP {@code target} has lost after {@code seconds} of {@code commands} (0 when it never came on). */
    private static double lost(Sortie sortie, EnemySpec target, double seconds, int commands) {
        double least = target.hp();
        for (int i = 0; i < SimStep.ticks(seconds); i++) {
            sortie.step(commands);
            Optional<Enemy> on = ActThreeArsenalFlightTest.find(sortie, target);
            if (on.isPresent()) {
                least = Math.min(least, on.get().hp());
            }
        }
        return target.hp() - least;
    }

    /** Whether {@code lost} is a whole number of hits of {@code damage}. */
    private static boolean hitsOf(double lost, double damage) {
        double hits = lost / damage;
        return Math.abs(hits - Math.rint(hits)) < 1e-6;
    }

    @Test
    void theWaterRulesComeFromEuropasData() {
        MediumSpec.Rules rules = SimSpecs.waterRules(CONTENT);
        assertEquals(
                new MediumSpec.Rules(
                        0.7,
                        0.6,
                        0.7,
                        0.75,
                        1.5,
                        1,
                        Set.of("ion-beam", "lance-laser", "rear-lance"),
                        Set.of("torpedo-pod", HARPOON, CHARGES)),
                rules);
        assertEquals(
                new MediumSpec(rules, List.of(new MediumSpec.Switch(12.5, true))),
                SimSpecs.underwaterFrom(CONTENT, 12.5));
    }

    /** Every built level (Acts 1–3) flies in the air with the specials' own settings: their scripts are unchanged. */
    @Test
    void everyBuiltLevelFliesInTheAirWithoutLimits() {
        for (String key : CONTENT.levels().keySet()) {
            LevelScript script = SimSpecs.level(CONTENT, key, Difficulty.MEDIUM);
            assertEquals(MediumSpec.AIR, script.medium(), key);
            assertEquals(SpecialLimits.NONE, script.specialLimits(), key);
        }
    }

    /**
     * A level's waterlines are its sections' starts and its special limits switch there: Level 01's data flown as an
     * Act 4 level (Level 22, in a stand-in act data file) whose second and third sections are under water and whose
     * third forbids the Orbital Lance.
     */
    @Test
    void aLevelsWaterlinesAndSpecialLimitsAreAtItsSectionsStarts() {
        String source = "campaign/act-1-first-contact/level-01-break-at-dawn/data.yaml";
        String path = "campaign/act-4-deep-water/level-22-ice-shelf/data.yaml";
        List<DataFile> files = new ArrayList<>(DesignTree.dataFiles());
        String text = files.stream()
                .filter(file -> file.path().equals(source))
                .findFirst()
                .orElseThrow()
                .text()
                .replace("    end: 60\n", "    end: 60\n    medium: underwater\n")
                .replace(
                        "    end: 110\n",
                        "    end: 110\n    medium: underwater\n    special_limits: [orbital-lance]\n");
        files.add(new DataFile(path, text));
        // Act 4's data file (none yet: part B writes it): Act 3's, holding Level 22 only.
        String act3 = files.stream()
                .filter(file -> file.path().equals("campaign/act-3-red-dust/data.yaml"))
                .findFirst()
                .orElseThrow()
                .text();
        files.add(new DataFile(
                "campaign/act-4-deep-water/data.yaml",
                act3.replace("levels: [15, 21]", "levels: [22, 22]").replaceAll("(?m)^next_act:.*$", "")));
        Content content = ContentLoader.load(files);
        String key = "act-4-deep-water/level-22-ice-shelf";
        LevelData level = content.level(key);
        assertTrue(level.underwater());

        LevelScript script = SimSpecs.level(content, key, Difficulty.MEDIUM);
        assertEquals(
                new MediumSpec(
                        SimSpecs.waterRules(content),
                        List.of(new MediumSpec.Switch(20, true), new MediumSpec.Switch(110, false))),
                script.medium());
        assertEquals(
                new SpecialLimits(List.of(
                        new SpecialLimits.Switch(60, Optional.of(Set.of(SpecialSpec.Kind.LANCE))),
                        new SpecialLimits.Switch(110, Optional.empty()))),
                script.specialLimits());
        // The rest of the script is Level 01's.
        LevelScript level01 = SimSpecs.level(CONTENT, Level01Test.LEVEL, Difficulty.MEDIUM);
        assertEquals(level01.sections(), script.sections());
        assertEquals(level01.waves().size(), script.waves().size());
    }

    @Test
    void theHarpoonTorpedoesAreATorpedoFromTheNoseWithASplash() {
        for (int level = 1; level <= 6; level++) {
            WeaponSpec harpoon = SimSpecs.weapon(CONTENT, Armament.Slot.FRONT, HARPOON, level);
            assertEquals(WeaponSpec.Delivery.TORPEDO, harpoon.delivery());
            assertTrue(harpoon.antiSub());
            assertEquals(0.5, harpoon.splash());
            assertEquals(20, harpoon.blast());
            assertEquals(600, harpoon.range());
            assertEquals(Math.toRadians(35), harpoon.coneHalfAngle(), 1e-12);
            assertEquals(Math.toRadians(90), harpoon.turnRate(), 1e-12, "no Targeting computer bonus on a torpedo");
            assertEquals(level >= 5 ? 2 : 1, harpoon.muzzles().size());
        }
        WeaponSpec first = SimSpecs.weapon(CONTENT, Armament.Slot.FRONT, HARPOON, 1);
        assertEquals(
                List.of(0.8, 37.5, 360.0, 520.0),
                List.of(first.rate(), first.damage(), first.speed(), first.endSpeed()));
        WeaponSpec pod = SimSpecs.weapon(CONTENT, Armament.Slot.LEFT_WING, "torpedo-pod", 1);
        assertTrue(pod.antiSub());
        assertEquals(0, pod.splash(), "the Torpedo Pod has no splash");
        assertFalse(SimSpecs.weapon(CONTENT, Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1)
                .antiSub());
        assertFalse(SimSpecs.weapon(CONTENT, Armament.Slot.REAR, "proximity-mines", 1)
                .antiSub());
    }

    @Test
    void theDepthChargesAreABarrelOnAFuse() {
        double[] fuse = {0.9, 0.9, 0.8, 0.8, 0.7, 0.7};
        double[] blast = {40, 46, 52, 58, 64, 64};
        for (int level = 1; level <= 6; level++) {
            WeaponSpec charges = SimSpecs.weapon(CONTENT, Armament.Slot.REAR, CHARGES, level);
            assertEquals(WeaponSpec.Delivery.DEPTH_CHARGE, charges.delivery());
            assertTrue(charges.antiSub());
            assertEquals(140, charges.speed());
            assertEquals(blast[level - 1], charges.blast());
            assertEquals(
                    WeaponSpec.Mines.charge(0.6, fuse[level - 1], WeaponRules.DEPTH_CHARGES_LIVE), charges.mines());
            assertEquals(level >= 5 ? 2 : 1, charges.muzzles().size());
            assertEquals(Math.PI, charges.muzzles().getFirst().angle(), 1e-12, "rolled out backwards");
        }
    }

    /** Under water the harpoon hits a {@code sub} target at its full damage; in a dry level it fires nothing. */
    @Test
    void theHarpoonHitsUnderWaterAtFullDamageAndIsIdleInADryLevel() {
        EnemySpec sub = dummy(Layer.SUB, 40);
        Loadout harpoon = loadout(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, HARPOON, 1));
        double wet = lost(range(harpoon, underwater(), 30, new GroundUnit(0, SHIP_X, sub, -1)), sub, 6, FIRE);
        assertTrue(wet > 0, "it hits under water");
        assertTrue(hitsOf(wet, 37.5), "full damage: " + wet);

        EnemySpec dry = dummy(Layer.SUB, 40);
        assertEquals(0, lost(range(harpoon, MediumSpec.AIR, 30, new GroundUnit(0, SHIP_X, dry, -1)), dry, 6, FIRE));
    }

    /** The Pulse Cannon (no {@code anti-sub}) does × 0.7 under water. */
    @Test
    void aGunWithoutAntiSubDoesSevenTenthsUnderWater() {
        EnemySpec target = dummy(Layer.GROUND, 40);
        Loadout pulse = loadout(new SimSpecs.FittedWeapon(Armament.Slot.FRONT, SimSpecs.PULSE_CANNON, 1));
        double wet = lost(range(pulse, underwater(), 30, new GroundUnit(0, SHIP_X, target, -1)), target, 4, FIRE);
        assertTrue(wet > 0);
        assertTrue(hitsOf(wet, 2 * 0.7), "1.4 a bolt: " + wet);
        assertFalse(hitsOf(wet, 2), "not 2 a bolt: " + wet);
    }

    /**
     * Under water the barrels burst on what passes behind the ship, at their full damage (the ship climbs first, so the
     * barrels burst on the field: one sinking off it is lost).
     */
    @Test
    void theDepthChargesBurstOnWhatPassesBehindTheShip() {
        Loadout charges = loadout(new SimSpecs.FittedWeapon(Armament.Slot.REAR, CHARGES, 1));
        for (boolean water : new boolean[] {true, false}) {
            EnemySpec target = dummy(Layer.GROUND, 60);
            Sortie sortie =
                    range(charges, water ? underwater() : MediumSpec.AIR, 60, new GroundUnit(0, SHIP_X, target, -1));
            for (int i = 0; i < SimStep.ticks(1.5); i++) {
                sortie.step(Command.UP.bit());
            }
            double lost = lost(sortie, target, 12, FIRE);
            if (water) {
                assertTrue(lost > 0, "they burst on it");
                assertTrue(hitsOf(lost, 25), "full damage: " + lost);
            } else {
                assertEquals(0, lost, "NO WATER in a dry level");
            }
        }
    }

    @Test
    void theSonarPulseAndTheDecoyFlaresFlyWithTheirSettings() {
        assertTrue(SimSpecs.fliesSpecial(SimSpecs.SONAR_PULSE));
        assertTrue(SimSpecs.fliesSpecial(SimSpecs.DECOY_FLARES));
        SpecialSpec sonar = SimSpecs.special(CONTENT, SimSpecs.SONAR_PULSE, 9);
        assertEquals(SpecialSpec.Kind.SONAR, sonar.kind());
        assertEquals(new SonarSpec(0.5, 6, 1.5), sonar.sonar());
        assertEquals(4, sonar.charges(), "at most its most");
        assertEquals(SpecialSpec.Setting.WATER_ONLY, sonar.setting());
        SpecialSpec flares = SimSpecs.special(CONTENT, SimSpecs.DECOY_FLARES, 2);
        assertEquals(SpecialSpec.Kind.FLARES, flares.kind());
        assertEquals(new FlaresSpec(4, 120, 300, 0.6, 4, 1.0), flares.flares());
        assertEquals(SpecialSpec.Setting.ANYWHERE, flares.setting());
        assertEquals(
                SpecialSpec.Setting.NOT_UNDER_WATER,
                SimSpecs.special(CONTENT, SimSpecs.AIRSTRIKE, 1).setting());
        assertEquals(
                SpecialSpec.Setting.NOT_UNDER_WATER,
                SimSpecs.special(CONTENT, SimSpecs.ORBITAL_LANCE, 1).setting());
        for (String name : List.of(SimSpecs.SMART_BOMB, SimSpecs.EMP_BURST, SimSpecs.SHIELD_OVERCHARGE)) {
            assertEquals(
                    SpecialSpec.Setting.ANYWHERE,
                    SimSpecs.special(CONTENT, name, 1).setting(),
                    name);
        }
    }

    /** The Sonar Pulse is refused where there is no water and works under it; the Airstrike the other way round. */
    @Test
    void aForbiddenSpecialIsRefusedWithItsReasonAndNoChargeSpent() {
        Loadout sonar = loadout().withSpecial(SimSpecs.special(CONTENT, SimSpecs.SONAR_PULSE, 2));
        Sortie dry = range(sonar, MediumSpec.AIR, 30);
        press(dry);
        assertEquals(SpecialLimits.Reason.NO_WATER, dry.special().limit());
        assertEquals(2, dry.special().charges());
        Sortie wet = range(sonar, underwater(), 30);
        press(wet);
        assertEquals(SpecialLimits.Reason.NONE, wet.special().limit());
        assertEquals(1, wet.special().charges());
        assertTrue(wet.special().sonarActive());

        Loadout airstrike = loadout().withSpecial(SimSpecs.special(CONTENT, SimSpecs.AIRSTRIKE, 2));
        Sortie under = range(airstrike, underwater(), 30);
        press(under);
        assertEquals(SpecialLimits.Reason.UNDER_ICE, under.special().limit());
        assertEquals(2, under.special().charges());
        Sortie above = range(airstrike, MediumSpec.AIR, 30);
        press(above);
        assertEquals(1, above.special().charges());
    }

    /** One press of the special button after a second, then a second more. */
    private static void press(Sortie sortie) {
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
        sortie.step(SPECIAL);
        for (int i = 0; i < SimStep.ticks(1); i++) {
            sortie.step(Command.NONE);
        }
    }

    @Test
    void theHullTheNanitesAndTheArsenalFlyFromTheHangar() {
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 24);
        DebugFit.parse(
                        "front=harpoon-torpedoes:2,rear=depth-charges:3,utility=pressure-hull,utility2=auto-repair-nanites:2")
                .withSpecial("sonar-pulse:3")
                .applyTo(campaign, CATALOGUE);
        Flight flight = Flight.of(CONTENT, CATALOGUE, campaign);
        Loadout loadout = flight.loadout();
        assertTrue(loadout.pressureHull());
        assertEquals(Optional.of(new NaniteSpec(2, 0.5, 1)), loadout.nanites());
        assertEquals(SpecialSpec.Kind.SONAR, loadout.special().orElseThrow().kind());
        assertEquals(3, loadout.special().orElseThrow().charges());
        assertEquals(
                List.of(HARPOON, CHARGES),
                loadout.armament().mounts().stream()
                        .map(mount -> mount.weapon().slug())
                        .filter(slug -> slug.equals(HARPOON) || slug.equals(CHARGES))
                        .toList());
        assertEquals(List.of(), flight.notFlown());
        assertEquals(SimSpecs.nanites(CONTENT, 1), new NaniteSpec(4, 0.5, 1));

        Campaign plain = DebugFit.startAt(RULES, Difficulty.MEDIUM, 24);
        Loadout without = Flight.of(CONTENT, CATALOGUE, plain).loadout();
        assertFalse(without.pressureHull());
        assertEquals(Optional.empty(), without.nanites());
    }

    @Test
    void theBulwarkFliesWithItsCannonAndALoanerWhileRookIsMissing() {
        BulwarkSpec bulwark = SimSpecs.bulwark(CONTENT, WingmanSpec.Side.LEFT, 80);
        assertEquals(WingmanSpec.Side.LEFT, bulwark.side());
        assertEquals(
                List.of(80.0, 120.0, 36.0, 200.0),
                List.of(bulwark.armour(), bulwark.maxArmour(), bulwark.size(), bulwark.speed()));
        assertEquals(new Hitbox(14, 14), bulwark.hitbox());
        assertEquals(new WingmanSpec.Offset(56, 20), bulwark.slot());
        assertEquals(List.of(150.0, 0.3), List.of(bulwark.drawRadius(), bulwark.drawShare()));
        WeaponSpec gun = bulwark.gun();
        assertEquals(List.of(1.0, 14.0), List.of(gun.rate(), gun.damage()));
        assertEquals(WeaponSpec.Delivery.BOLT, gun.delivery());
        assertTrue(gun.antiSub());
        assertEquals(1, gun.muzzles().size());
        assertEquals(0, gun.muzzles().getFirst().angle());

        // Level 27: Rook is missing, the loaner flies at full armour in his slot.
        Campaign campaign = DebugFit.startAt(RULES, Difficulty.MEDIUM, 27);
        Loadout loadout = Flight.of(CONTENT, CATALOGUE, campaign).loadout();
        assertEquals(Optional.empty(), loadout.wingman());
        assertEquals(120, loadout.bulwark().orElseThrow().armour());
        assertEquals(
                campaign.gear().escort().side(), loadout.bulwark().orElseThrow().side());
        // Level 26: Rook.
        Loadout rook = Flight.of(CONTENT, CATALOGUE, DebugFit.startAt(RULES, Difficulty.MEDIUM, 26))
                .loadout();
        assertTrue(rook.wingman().isPresent());
        assertEquals(Optional.empty(), rook.bulwark());
    }

    /** Under water the Bulwark's anti-sub cannon hits at its full 14. */
    @Test
    void theBulwarksCannonKeepsItsFullDamageUnderWater() {
        Loadout loadout = loadout().withBulwark(SimSpecs.bulwark(CONTENT, WingmanSpec.Side.RIGHT, 120));
        // Where the Bulwark settles beside the ship.
        Sortie probe = range(loadout, underwater(), 30);
        for (int i = 0; i < SimStep.ticks(3); i++) {
            probe.step(Command.NONE);
        }
        double x = probe.bulwark().orElseThrow().x();
        assertTrue(Math.abs(x - SHIP_X) > 40, "beside the ship: " + x);
        EnemySpec target = dummy(Layer.GROUND, 30);
        double wet = lost(range(loadout, underwater(), 30, new GroundUnit(1, x, target, -1)), target, 6, FIRE);
        assertTrue(wet > 0, "it fires while the player fires");
        assertTrue(hitsOf(wet, 14), "full damage: " + wet);
    }
}
