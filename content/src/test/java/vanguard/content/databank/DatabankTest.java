package vanguard.content.databank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Fitted;
import vanguard.content.campaign.ItemKind;
import vanguard.content.campaign.LoadoutSlot;
import vanguard.content.campaign.SaveException;
import vanguard.content.campaign.SaveFormat;
import vanguard.content.campaign.SaveGame;
import vanguard.sim.Enemy;
import vanguard.sim.Loadout;
import vanguard.sim.Sortie;

/**
 * Step DB1 (design/ui/databank): the index, a campaign's view (what it knows, the sensor level, the lists), the field
 * model by sensor level, the numbers at a level against the simulation's spawn, and each advice rule.
 */
class DatabankTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final Databank DATABANK = Databank.of(CONTENT);
    private static final CampaignRules RULES = CampaignRules.of(CONTENT);

    /** The v0.3 release's save at the Level 36 hangar, moved on to the Level 40 hangar, migrated to version 7. */
    static SaveGame level40() throws IOException, SaveException {
        try (var in = DatabankTest.class.getResourceAsStream("/saves/v0.3-medium-level-36-hangar.json")) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\"nextLevel\" : 36", "\"nextLevel\" : 40");
            return SaveFormat.read(json, DATABANK.roster());
        }
    }

    /** {@code save} with a sensor suite of {@code level} fitted in the second utility slot, or stored. */
    static SaveGame withSuite(SaveGame save, int level, boolean fitted) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(save.loadout());
        Map<ItemKind, List<Fitted>> inventory = new EnumMap<>(ItemKind.class);
        inventory.putAll(save.inventory());
        if (fitted) {
            loadout.put(LoadoutSlot.UTILITY_2, new Fitted(Databank.SENSOR_SUITE, level));
        } else {
            List<Fitted> utilities = new java.util.ArrayList<>(inventory.getOrDefault(ItemKind.UTILITY, List.of()));
            utilities.add(new Fitted(Databank.SENSOR_SUITE, level));
            inventory.put(ItemKind.UTILITY, utilities);
        }
        return new SaveGame(
                save.version(),
                save.created(),
                save.playtime(),
                save.difficulty(),
                save.nextLevel(),
                save.credits(),
                save.score(),
                loadout,
                inventory,
                save.unlocks(),
                save.specials(),
                save.armour(),
                save.escort(),
                save.bays(),
                save.retriesLeft(),
                save.grades(),
                save.dataCores(),
                save.storyFlags(),
                save.stats(),
                save.databank());
    }

    private static DatabankView view(SaveGame save) {
        return DATABANK.view(Campaign.load(RULES, save));
    }

    private static Entry entry(String id) {
        return DATABANK.entry(id).orElseThrow();
    }

    @Test
    void theIndexHoldsEveryUnitBossItemAndConceptOnce() {
        // M10 part G: the Shield Pylon and the Halo Platform (a held set piece: SET PIECE on the enemies' tab). M11
        // part B: the Gate Warden and the Rift Skater.
        assertEquals(
                73,
                DATABANK.entries(Entry.Tab.ENEMIES)
                        .size()); // M11 part C: and the spawning organ; M11 part D: the Husk, the seed casket
        // M10 part H: the Ascendant.
        assertEquals(10, DATABANK.entries(Entry.Tab.BOSSES).size());
        assertEquals(
                CONTENT.weapons().size() + CONTENT.specials().specials().size(),
                DATABANK.entries(Entry.Tab.WEAPONS).size());
        assertEquals(14, DATABANK.entries(Entry.Tab.CONCEPTS).size());
        assertEquals(Entry.Kind.SENSOR_SUITE, entry("utility:sensor-suite").kind());
        assertEquals(Optional.of("SET PIECE"), entry("leviathan").tag());
        assertEquals(Optional.of("NEUTRAL"), entry("wavering-stormhawk").tag());
        assertEquals(Optional.of("ALLY"), entry("fleet-stormhawk").tag());
        assertEquals(Optional.of("MID-BOSS"), entry("honour-guard").tag());
        assertEquals(Optional.empty(), entry("iron-sovereign").tag());
        assertEquals("WINGS", entry("tesla-coil-pod").group());
        assertEquals("SPECIALS", entry("special:emp-burst").group());
        assertEquals("CORE", entry("generator:mk-v-plus").group());
        assertEquals(7, entry("generator:mk-vi").act());
        assertEquals(6, entry("concept:mirrors").act());
    }

    @Test
    void aLevel40CampaignKnowsWhatItFlewAndCountsTheRestOfTheActsReached() throws Exception {
        DatabankView view = view(level40());

        assertTrue(view.known(entry("mirror-interceptor")));
        assertFalse(view.known(entry("honour-guard")));
        assertTrue(view.unknown(entry("honour-guard")));
        // An item of a later act is neither known nor counted (nothing of a later act is named or hinted).
        assertFalse(view.known(entry("generator:mk-vi")));
        assertFalse(view.unknown(entry("generator:mk-vi")));
        Pages.TabList bosses = view.list(Entry.Tab.BOSSES);
        assertEquals(8, bosses.known());
        // The Honour Guard and (M10 part H) the Ascendant, both of Act 6, reached but not flown.
        assertEquals(2, bosses.unknown());
        assertEquals("ACT VI", bosses.rows().getLast().right());
        assertFalse(bosses.rows().getLast().known());
        // The migration read every entry it knew: no NEW tags.
        assertTrue(DATABANK.entries().stream().filter(view::known).noneMatch(view::fresh));
        Pages.TabList enemies = view.list(Entry.Tab.ENEMIES);
        assertEquals("L01", enemies.rows().getFirst().right());
        assertEquals("ACT I", enemies.rows().getFirst().group());
        // Concepts open after their level is flown.
        assertTrue(view.known(entry("concept:mirrors")));
    }

    @Test
    void aMigratedCampaignHasBeatenEveryBossBehindIt() throws Exception {
        // A version 6 save (and a playthroughSave campaign) starts every kill count at 0: a boss whose level is behind
        // the campaign is beaten all the same.
        DatabankView view = view(level40());
        List<Entry> met =
                DATABANK.entries(Entry.Tab.BOSSES).stream().filter(view::known).toList();
        assertEquals(8, met.size());
        for (Entry boss : met) {
            String beaten =
                    view.enemyPage(boss).field("BEATEN").orElseThrow().value().getFirst();
            assertTrue(beaten.startsWith("L"), boss.id() + ": " + beaten);
            assertTrue(boss.act() <= 5, boss.id());
        }
        assertTrue(view.enemyPage(entry("iron-sovereign"))
                .field("BEATEN")
                .orElseThrow()
                .value()
                .getFirst()
                .startsWith("L35  "));
        // Met in a failed attempt at its own level: not beaten yet.
        try (var in = DatabankTest.class.getResourceAsStream("/saves/v0.3-medium-level-36-hangar.json")) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\"nextLevel\" : 36", "\"nextLevel\" : 35");
            Campaign campaign = Campaign.load(RULES, SaveFormat.read(json, DATABANK.roster()));
            campaign.recordAttempt(35, List.of("iron-sovereign"), Map.of());
            assertTrue(DATABANK.view(campaign)
                    .enemyPage(entry("iron-sovereign"))
                    .field("BEATEN")
                    .orElseThrow()
                    .value()
                    .getFirst()
                    .startsWith("NOT YET"));
        }
    }

    @Test
    void theFieldsFollowTheSensorLadder() throws Exception {
        SaveGame save = level40();
        Entry mirror = entry("mirror-interceptor");
        for (int level = 0; level <= 3; level++) {
            DatabankView view = level == 0 ? view(save) : view(withSuite(save, level, true));
            assertEquals(level, view.sensorLevel());
            Pages.EnemyPage page = view.enemyPage(mirror);
            int at = level;
            for (String label : List.of("CLASS", "LAYER", "FROM", "KILLS")) {
                assertEquals(at < 1, page.field(label).orElseThrow().locked(at), label + " at L" + at);
            }
            for (String label : List.of("HP", "MIRROR", "BOUNTY", "ATTACKS")) {
                assertEquals(at < 2, page.field(label).orElseThrow().locked(at), label + " at L" + at);
            }
            assertEquals(at < 3, page.field("WEAK").orElseThrow().locked(at));
            assertEquals(at < 2, page.picture().silhouette());
            assertEquals(at == 0, page.suiteCallout());
            assertEquals(at < 3, page.advice().isEmpty());
            assertEquals(
                    at < 3
                            ? CONTENT.databank()
                                    .enemies()
                                    .get("mirror-interceptor")
                                    .vague()
                            : CONTENT.databank()
                                    .enemies()
                                    .get("mirror-interceptor")
                                    .tip(),
                    page.varga());
            assertEquals(List.of(Pages.LOCKED), page.field("WEAK").orElseThrow().shown(Math.min(at, 2)));
        }
        assertEquals(
                "SENSOR L2",
                view(save).enemyPage(mirror).field("HP").orElseThrow().tag());
        // A stored suite counts too (the databank is read at home); a boss adds its phases at L3.
        DatabankView stored = view(withSuite(save, 3, false));
        assertEquals(3, stored.sensorLevel());
        assertEquals(
                CONTENT.databank().bosses().get("iron-sovereign").phases(),
                stored.enemyPage(entry("iron-sovereign")).phases());
        assertTrue(view(save).enemyPage(entry("iron-sovereign")).phases().isEmpty());
    }

    @Test
    void numbersAreTheLastLevelMetsAtTheCampaignsDifficulty() throws Exception {
        DatabankView view = view(withSuite(level40(), 2, true));
        Pages.EnemyPage mirror = view.enemyPage(entry("mirror-interceptor"));

        // design/ui/databank, Enemy entries: 30 x 1.6^5 x 0.32 = 101 at Level 37.
        assertEquals(
                List.of("120  AT L37, MEDIUM"), mirror.field("HP").orElseThrow().value());
        assertEquals(List.of("101 CR"), mirror.field("BOUNTY").orElseThrow().value());
        assertEquals(37, mirror.lastMet());
        // The Iron Sovereign's parts at Level 35 (m10-db-gaps.md §2): core 1,373 + 13 x 23.
        Pages.EnemyPage sovereign = view.enemyPage(entry("iron-sovereign"));
        assertEquals(1672, sovereign.numbers().orElseThrow().bounty());
        assertEquals(
                List.of("1 672 CR"), sovereign.field("BOUNTY").orElseThrow().value());
        // A returning unit: its numbers at the last level flown that holds it.
        Pages.EnemyPage talon = view.enemyPage(entry("talon"));
        assertEquals(39, talon.lastMet());
        assertTrue(talon.numbers().orElseThrow().shield().isPresent());
    }

    @Test
    void theHpAndShieldAreThoseTheSimulationSpawns() {
        record Case(int level, String slug) {}
        for (Case c : List.of(
                new Case(37, "mirror-interceptor"),
                new Case(8, "needler"),
                new Case(19, "ghost-drone"),
                new Case(39, "talon"),
                new Case(30, "gilded-gunship"))) {
            for (Difficulty difficulty : List.of(Difficulty.EASY, Difficulty.HARD)) {
                UnitNumbers numbers = DATABANK.units(c.level(), difficulty)
                        .orElseThrow()
                        .numbers(c.slug())
                        .orElseThrow();
                Enemy spawned = spawned(c.level(), difficulty, c.slug());
                assertEquals(spawned.spec().hp(), numbers.hp(), 1e-9, c + " " + difficulty);
                assertEquals(spawned.spec().shield().map(s -> s.hp()), numbers.shield(), c + " " + difficulty);
                assertEquals(spawned.spec().mirror().map(m -> m.heat()), numbers.mirrorHeat(), c + " " + difficulty);
                assertEquals(
                        (int) Math.rint(spawned.spec().bounty()
                                * SimSpecs.rules(CONTENT, key(c.level()), difficulty)
                                        .scoring()
                                        .creditFactor()
                                * CONTENT.level(key(c.level())).bounties()),
                        numbers.bounty(),
                        c + " " + difficulty);
            }
        }
    }

    private static String key(int level) {
        return CONTENT.levelKey(level).orElseThrow();
    }

    /** The first unit of {@code slug} the simulation spawns in Level {@code level} (an invulnerable ship, no input). */
    private static Enemy spawned(int level, Difficulty difficulty, String slug) {
        String key = key(level);
        Loadout loadout = SimSpecs.starterLoadout(CONTENT, difficulty);
        Sortie sortie = new Sortie(
                1,
                loadout,
                SimSpecs.level(CONTENT, key, difficulty),
                SimSpecs.rules(CONTENT, key, difficulty).withInvulnerableShip(),
                loadout.plating().maxArmour());
        for (int step = 0; step < 60 * 60 * 6; step++) {
            sortie.step(0);
            for (int i = 0; i < sortie.enemyCount(); i++) {
                Enemy enemy = sortie.enemy(i);
                if (enemy.spec().slug().equals(slug) && !enemy.leader()) {
                    return enemy;
                }
            }
        }
        throw new AssertionError("no " + slug + " in Level " + level);
    }

    @Test
    void aLevelsUnitsAreItsKindsAndSetPieces() {
        LevelUnits l35 = DATABANK.units(35, Difficulty.MEDIUM).orElseThrow();
        assertTrue(l35.slugs().contains("iron-sovereign"));
        LevelUnits l03 = DATABANK.units(3, Difficulty.MEDIUM).orElseThrow();
        assertTrue(l03.slugs().contains("leviathan"));
        assertTrue(DATABANK.units(41, Difficulty.MEDIUM).isEmpty() || DATABANK.lastLevel() >= 41);
        LevelUnits.Edges mirrors =
                DATABANK.units(37, Difficulty.MEDIUM).orElseThrow().edges().get("mirror-interceptor");
        assertEquals(0, mirrors.front(), 1e-9);
        assertTrue(mirrors.sides() > 0 && mirrors.rear() > 0);
    }

    @Test
    void gearShowsInFull() throws Exception {
        DatabankView view = view(level40());

        Pages.GearPage beam = view.gearPage(entry("ion-beam"));
        assertEquals("L4  FITTED AT THE FRONT", beam.yours());
        assertTrue(beam.hits().contains("HIGH-AIR"));
        assertTrue(beam.not().contains("SUB"));
        assertFalse(beam.strongAgainst().isEmpty());
        assertTrue(beam.strongAgainst().size() <= 4);
        Pages.GearPage bombs = view.gearPage(entry("bomb-rack"));
        assertEquals(List.of("GROUND X2", "HARDENED"), bombs.hits());
        assertTrue(bombs.yours().startsWith("NOT OWNED - "));
        Pages.GearPage suite = view.gearPage(entry("utility:sensor-suite"));
        assertEquals(4, suite.ladder().size());
        assertEquals(CONTENT.databank().sensorSuite().varga(), suite.varga());
        assertTrue(view(withSuite(level40(), 1, false))
                .gearPage(entry("utility:sensor-suite"))
                .varga()
                .isEmpty());
        assertEquals("HIRED - ON THE LEFT", view.gearPage(entry("escort:rook")).yours());
        assertEquals(4, view.gearPage(entry("escort:rook")).items().size());
        assertEquals(
                CONTENT.databank().concepts().get("mirrors").lines(),
                view.conceptPage(entry("concept:mirrors")).lines());
    }

    @Test
    void everyKnownPageFitsTheLabelWidth() throws Exception {
        DatabankView view = view(withSuite(level40(), 3, true));
        for (Entry entry : DATABANK.entries()) {
            if (!view.known(entry)) {
                continue;
            }
            if (entry.kind().unit()) {
                Pages.EnemyPage page = view.enemyPage(entry);
                for (Pages.Field field : page.fields()) {
                    for (String line : field.value()) {
                        assertTrue(
                                line.length() <= vanguard.content.DatabankData.LINE_WIDTH,
                                entry.id() + " " + field.label() + ": " + line);
                    }
                }
            } else if (entry.kind().gear()) {
                view.gearPage(entry);
            } else {
                view.conceptPage(entry);
            }
        }
    }
}
