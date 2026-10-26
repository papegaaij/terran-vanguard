package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.ItemKind;

/**
 * M8 part A (step A2): Act 4's systems data: Europa's under-water rules, the Harpoon Torpedoes and the Depth Charges,
 * the Sonar Pulse, the Decoy Flares and the setting restrictions, the pressure hull and the Auto-repair nanites, the Mk
 * IV at 20 MW, the Bulwark and Rook's absence, the reference DPS of Levels 22–28, and a level's {@code medium} and
 * {@code special_limits}.
 */
class ActFourDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String LEVEL_01 = "campaign/act-1-first-contact/level-01-break-at-dawn/data.yaml";
    private static final String DEPTH_CHARGES = "player/weapons/depth-charges/data.yaml";

    @Test
    void europasWaterRulesAreTheDecidedNumbers() {
        EuropaData.Underwater water = CONTENT.europa().underwater();
        assertEquals(0.7, water.bulletSpeed());
        assertEquals(0.6, water.acceleration());
        assertEquals(0.7, water.damage());
        assertEquals(0.2, water.energyDamage(), "M8 AW: the energy weapons' factor, in place of the guns'");
        assertEquals(0.4, water.reach());
        assertEquals(1.5, water.torpedoRange());
        assertEquals(1, water.ease());
        assertEquals(23, water.hydroKit());
        assertEquals(List.of("ion-beam", "lance-laser", "rear-lance"), water.energyWeapons());
        assertFalse(water.energy("pulse-cannon"));
        assertTrue(
                water.torpedo("harpoon-torpedoes") && water.torpedo("depth-charges") && water.torpedo("torpedo-pod"));
        assertFalse(water.torpedo("micro-missile-pod"));
    }

    @Test
    void theHarpoonIsAFrontTorpedoWithASplash() {
        WeaponData harpoon = CONTENT.weapon("harpoon-torpedoes");
        assertEquals(WeaponData.Slot.FRONT, harpoon.slot());
        assertEquals(WeaponData.TORPEDO, harpoon.hits());
        assertTrue(harpoon.antiSub());
        // M8 AW (the user, 2026-10-23): faster, lighter, a larger blast and a bigger splash.
        assertEquals(Optional.of(0.6), harpoon.splash());
        assertEquals(
                List.of(40.0, 40.0, 44.0, 48.0, 48.0),
                harpoon.levels().stream()
                        .map(level -> level.blast().orElseThrow())
                        .toList());
        assertEquals(Optional.of(52.0), harpoon.overdrive().blast());
        double[] dps = {64.8, 75.4, 86.8, 99, 118.8};
        for (int i = 0; i < dps.length; i++) {
            assertEquals(dps[i], harpoon.volleyDps(harpoon.levels().get(i)), 1e-9, "L" + (i + 1));
        }
        assertTrue(SimSpecs.flies(CONTENT, "harpoon-torpedoes"), "the Torpedo Pod's delivery, from the nose");
        Catalogue.Item item = Catalogue.complete(CONTENT).item(ItemKind.FRONT, "harpoon-torpedoes");
        assertEquals(List.of(500, 1000, 2000, 4000), item.upgrades());
        assertEquals(List.of(3.0, 3.5, 4.0, 4.5, 5.0), item.draws());
        assertEquals(22, item.unlock());
    }

    @Test
    void theDepthChargesBurstOnAFusePerLevel() {
        WeaponData charges = CONTENT.weapon("depth-charges");
        assertEquals(WeaponData.Slot.REAR, charges.slot());
        assertEquals(WeaponData.DEPTH_CHARGE, charges.hits());
        assertTrue(charges.antiSub());
        assertEquals(Optional.empty(), charges.range());
        // M8 AW (the user, 2026-10-23): tossed forward, a longer fuse, a larger blast.
        assertEquals(
                List.of(1.4, 1.4, 1.3, 1.3, 1.2),
                charges.levels().stream()
                        .map(level -> level.fuse().orElseThrow())
                        .toList());
        assertEquals(
                List.of(48.0, 52.0, 56.0, 60.0, 64.0),
                charges.levels().stream()
                        .map(level -> level.blast().orElseThrow())
                        .toList());
        assertEquals(
                List.of(28.0, 34.0, 42.0, 50.0, 52.0),
                charges.levels().stream().map(WeaponData.Level::damage).toList());
        assertEquals(Optional.of(68.0), charges.overdrive().blast());
        assertEquals(Optional.of(1.2), charges.overdrive().fuse());
        assertEquals(2, charges.levels().get(4).pattern().size(), "L5 tosses two charges");
        Catalogue.Item item = Catalogue.complete(CONTENT).item(ItemKind.REAR, "depth-charges");
        assertEquals(List.of(400, 800, 1600, 3200), item.upgrades());
        assertEquals(List.of(2.0, 2.5, 2.5, 3.0, 3.0), item.draws());
        assertTrue(
                Catalogue.of(CONTENT).items(ItemKind.REAR).stream()
                        .anyMatch(live -> live.id().equals("depth-charges")),
                "in the live shop: its icon is in (step A5)");
    }

    @Test
    void aDepthChargeWithoutAFuseOrAWeaponWithAFuseIsRejected() {
        assertProblem(DEPTH_CHARGES, text -> text.replace(", fuse: 1.4}", "}"), "fuse and a blast");
        assertProblem(
                "player/weapons/torpedo-pod/data.yaml",
                text -> text.replace("damage: 12.5, turn: 60}", "damage: 12.5, turn: 60, fuse: 1}"),
                "a fuse belongs to depth charges");
        assertProblem(
                "player/weapons/pulse-cannon/data.yaml", text -> text + "splash: 0.5\n", "a splash is a torpedo's");
    }

    @Test
    void theSpecialsSettingRestrictionsGiveTheirReasonWords() {
        SpecialsData specials = CONTENT.specials();
        assertEquals(
                List.of("Airstrike", "Orbital Lance"),
                specials.forbidden(LevelData.Medium.UNDERWATER, false, Optional.empty()));
        assertEquals(List.of("Sonar Pulse"), specials.forbidden(LevelData.Medium.AIR, false, Optional.empty()));
        assertEquals(
                List.of(), specials.forbidden(LevelData.Medium.AIR, true, Optional.empty()), "above water (L11–13)");
        assertEquals(
                List.of("Orbital Lance"),
                specials.forbidden(LevelData.Medium.UNDERWATER, false, Optional.of(List.of("orbital-lance"))));
        assertEquals(Optional.of("UNDER ICE"), specials.reason("Airstrike"));
        assertEquals(Optional.of("NO WATER"), specials.reason("Sonar Pulse"));
        assertEquals(Optional.empty(), specials.reason("Smart Bomb"));
        assertEquals(new SpecialsData.Sonar(0.5, 6, 1.5), specials.sonar());
        assertEquals(new SpecialsData.Flares(4, 120, 300, 0.6, 4, 1.0), specials.flares());
        SpecialsData.Special sonar = specials.specials().stream()
                .filter(special -> special.name().equals("Sonar Pulse"))
                .findFirst()
                .orElseThrow();
        assertEquals(
                List.of(250, 4, 22, 1),
                List.of(
                        sonar.chargePrice().orElseThrow(),
                        sonar.maxCharges().orElseThrow(),
                        sonar.unlock(),
                        sonar.free()));
    }

    @Test
    void everyLevelSoFarFliesInTheAirWithTheDrySettingsLimits() {
        CONTENT.levels().forEach((key, level) -> {
            // M8 part C: Level 23 dives at its waterline (25.5 s): from there under water, the Airstrike and the Lance
            // forbidden, the Sonar Pulse free.
            boolean descent = key.equals("act-4-deep-water/level-23-through-the-ice");
            // M8 part D: Level 24 is under water from its start (the level's medium); M8 part E: Level 25 too; M8 part
            // F: Level 26 too; M8 part G: Level 27 too.
            boolean plain = key.equals("act-4-deep-water/level-24-abyssal-plain")
                    || key.equals("act-4-deep-water/level-25-siren-trench")
                    || key.equals("act-4-deep-water/level-26-blackwater")
                    || key.equals("act-4-deep-water/level-27-hydrothermal-vents");
            // M8 part H: Level 28 under water from its start until its breakout (115 s), then in the air again.
            boolean breakout = key.equals("act-4-deep-water/level-28-abyssal-maw");
            assertEquals(descent || plain || breakout, level.underwater(), key);
            for (int i = 0; i < level.sections().size(); i++) {
                if ((descent && level.sectionStart(i) >= 25.5) || plain || (breakout && level.sectionStart(i) < 115)) {
                    assertEquals(LevelData.Medium.UNDERWATER, level.mediumOf(i), key);
                    List<String> forbidden = level.forbiddenSpecials(i, CONTENT.specials());
                    assertTrue(forbidden.containsAll(List.of("Airstrike", "Orbital Lance")), key + ": " + forbidden);
                    assertFalse(forbidden.contains("Sonar Pulse"), key + ": " + forbidden);
                    continue;
                }
                assertEquals(LevelData.Medium.AIR, level.mediumOf(i), key);
                assertEquals(
                        level.water().orElse(false) ? List.of() : List.of("Sonar Pulse"),
                        level.forbiddenSpecials(i, CONTENT.specials()),
                        key);
            }
        });
    }

    @Test
    void aLevelUnderWaterBeforeActFourOrAnUnknownSpecialLimitIsRejected() {
        assertProblem(LEVEL_01, text -> text + "medium: underwater\n", "under water only from Act 4");
        assertProblem(LEVEL_01, text -> text + "special_limits: [warp-drive]\n", "no special 'warp-drive'");
        assertProblem(LEVEL_01, text -> text + "special_limits: [smart-bomb]\n", "no reason word");
        List<DataFile> files = edited(LEVEL_01, text -> text + "special_limits: [airstrike]\n");
        LevelData level = ContentLoader.load(files).level("act-1-first-contact/level-01-break-at-dawn");
        assertEquals(List.of("Airstrike"), level.forbiddenSpecials(0, CONTENT.specials()));
    }

    @Test
    void thePressureHullAndTheNanitesCarryTheirNumbers() {
        SystemsData.Utility hull = utility("Pressure hull");
        assertEquals(Optional.of(new SystemsData.Hull(false)), hull.hull());
        assertEquals(22, CONTENT.player().firstLevel(hull.available()));
        SystemsData.Utility nanites = utility("Auto-repair nanites");
        SystemsData.Repair repair = nanites.repair().orElseThrow();
        assertEquals(List.of(4.0, 2.0), repair.interval());
        assertEquals(0.5, repair.cap());
        assertEquals(1, repair.pause());
        assertEquals(3, nanites.draw());
        assertEquals(24, CONTENT.player().firstLevel(nanites.available()));
    }

    @Test
    void theMkIvGivesTwentyMegawatts() {
        assertEquals(
                20,
                CONTENT.generators().models().stream()
                        .filter(model -> model.name().startsWith("Mk IV"))
                        .findFirst()
                        .orElseThrow()
                        .output());
    }

    @Test
    void theBulwarkAndRooksAbsenceAreInTheWingmensData() {
        WingmenData.Bulwark bulwark = CONTENT.wingmen().bulwark();
        assertEquals(120, bulwark.armour());
        assertEquals(new Size(14, 14), bulwark.hitbox());
        assertEquals(new Point(56, 20), bulwark.slot());
        assertEquals(new WingmenData.BulwarkGun(1, 14, true), bulwark.gun());
        assertEquals(14, bulwark.gun().dps());
        assertEquals(new WingmenData.Draw(150, 0.3), bulwark.draw());
        assertEquals(6000, bulwark.price());
        assertEquals(8, bulwark.repair());
        WingmenData.Missing missing = CONTENT.wingmen().rook().missing();
        assertFalse(missing.in(26));
        assertTrue(missing.in(27) && missing.in(29));
        assertFalse(missing.in(30));
    }

    @Test
    void theReferenceDpsRunsToLevel28() {
        assertEquals(
                List.of(96.0, 100.0, 104.0, 108.0, 112.0, 116.0, 120.0),
                java.util.stream.IntStream.rangeClosed(22, 28)
                        .mapToObj(level ->
                                CONTENT.enemyBasis().referenceDps().get(level).doubleValue())
                        .toList());
    }

    private static SystemsData.Utility utility(String name) {
        return CONTENT.systems().utility().stream()
                .filter(module -> module.name().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static List<DataFile> edited(String path, UnaryOperator<String> edit) {
        List<DataFile> files = DesignTree.dataFiles().stream()
                .map(f -> f.path().equals(path) ? new DataFile(path, edit.apply(f.text())) : f)
                .toList();
        assertTrue(files.stream().anyMatch(f -> f.path().equals(path)), path);
        return files;
    }

    /** Loads the design tree with one file edited; exactly one problem is reported, containing {@code part}. */
    private static void assertProblem(String path, UnaryOperator<String> edit, String part) {
        var e = assertThrows(ContentException.class, () -> ContentLoader.load(edited(path, edit)));
        assertEquals(1, e.problems().size(), e.getMessage());
        assertTrue(
                e.problems().getFirst().contains(part),
                "'" + part + "' in " + e.problems().getFirst());
    }
}
