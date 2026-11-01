package vanguard.content.databank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.DataFile;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRules;

/**
 * Step DB1 (design/ui/databank, Weapon advice): each rule of the generated advice, from the unit's data alone, and the
 * player's matching gear (the fitted first).
 */
class AdviceTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();

    /** The rows' labels and reasons of {@code slug}, as {@code LABEL | REASON}, a red row marked {@code !}. */
    private static List<String> rows(Content content, String slug, Optional<Double> beamDps) {
        var unit = content.enemy(slug);
        return Advice.rules(content, slug, unit, unit.mirror().map(m -> m.heat()), beamDps).stream()
                .map(rule -> (rule.red() ? "!" : "") + rule.label() + " | " + rule.reason())
                .toList();
    }

    private static List<String> rows(String slug) {
        return rows(CONTENT, slug, Optional.empty());
    }

    @Test
    void aGroundUnitTakesAntiGroundTwice() {
        assertEquals(
                List.of("ANTI-GROUND | X2 ON EVERY PART", "EMP BURST | STUNS IT 1.5 S", "FORWARD | EFFECTIVE"),
                rows("spine-turret"));
    }

    @Test
    void aHardenedTargetOnlyOpensToAntiGroundAndTheBlasts() {
        List<String> rows = rows("hive-node");
        assertEquals("ANTI-GROUND | THE ONLY WAY IN; OTHER SHOTS GLANCE OFF", rows.getFirst());
        var rule = Advice.rules(CONTENT, "hive-node", CONTENT.enemy("hive-node"), Optional.empty(), Optional.empty())
                .getFirst();
        assertTrue(rule.matches()
                .test(new Advice.Item(
                        vanguard.content.campaign.ItemKind.SPECIAL, "Airstrike", java.util.Set.of(), "", false)));
        assertTrue(rule.matches()
                .test(new Advice.Item(
                        vanguard.content.campaign.ItemKind.SPECIAL, "Smart Bomb", java.util.Set.of(), "", false)));
        assertFalse(rule.matches()
                .test(new Advice.Item(
                        vanguard.content.campaign.ItemKind.SPECIAL, "EMP Burst", java.util.Set.of(), "", false)));
    }

    @Test
    void aHighAirUnitIsReachedByHomingAndBeamOnly() {
        List<String> rows = rows("wraith");
        assertEquals("HOMING | THE ONLY WEAPONS THAT REACH IT", rows.get(0));
        assertEquals("BEAM | THE ONLY WEAPONS THAT REACH IT", rows.get(1));
        assertTrue(rows.contains("REAR | EFFECTIVE"));
    }

    @Test
    void aSubUnitIsReachedByAntiSubOnly() {
        assertEquals(
                "ANTI-SUB | THE ONLY WEAPON THAT REACHES IT", rows("abyss-ray").getFirst());
        // A unit that flies under water: the others' factor there.
        assertTrue(
                rows("driftjelly").contains("ANTI-SUB | UNDER WATER THE OTHERS DO X0.7"), rows("driftjelly")::toString);
    }

    @Test
    void aHexShieldIsEmptiedByTheBreakerAndTheEmp() {
        List<String> rows = rows("talon");
        assertTrue(rows.contains("SHIELD-BREAKER | EMPTIES IT, NO REFILL FOR 3 S"), rows::toString);
        assertTrue(rows.contains("EMP BURST | EMPTIES EVERY SHIELD"), rows::toString);
        assertTrue(rows.contains("ANY | WEARS IT DOWN; IT REFILLS AFTER 2 S"), rows::toString);
    }

    @Test
    void aMirrorIsShatteredHeatedFlankedAndBouncesTheForwardGuns() {
        List<String> rows = rows(CONTENT, "mirror-interceptor", Optional.of(130.0));
        assertEquals(
                List.of(
                        "SHIELD-BREAKER | SHATTERS IT",
                        "EMP BURST | SHATTERS IT",
                        "BEAM | HEATS IT: BREAKS IN 1 S",
                        "SIDE / REAR | THE FLANKS ARE OPEN",
                        "BOMBS / SHELLS | FALL PAST IT",
                        "!FORWARD | BOUNCES BACK AS A SMALL BULLET (4)",
                        "EMP BURST | STUNS IT 3 S"),
                rows);
        assertEquals(
                "BEAM | HEATS IT UNTIL IT BREAKS", rows("mirror-interceptor").get(2));
    }

    @Test
    void theEmpStunsAMachineLongerThanAVrellUnit() {
        assertTrue(rows("ghost-drone").contains("EMP BURST | STUNS IT 3 S"));
        assertTrue(rows("needler").contains("EMP BURST | STUNS IT 1.5 S"));
        assertTrue(rows("chimera").contains("EMP BURST | STUNS IT 3 S"));
    }

    @Test
    void frontalArmourAsksForTheSidesAndTheRear() {
        List<String> rows = rows("scuttler");
        assertTrue(rows.contains("SIDE / REAR | ITS ARMOUR FACES FORWARD"), rows::toString);
        assertFalse(rows.contains("SIDE | EFFECTIVE"));
    }

    @Test
    void aPlainTraitIsEffective() {
        assertEquals(
                List.of("EMP BURST | STUNS IT 1.5 S", "FORWARD | EFFECTIVE", "SPREAD | EFFECTIVE"), rows("needler"));
    }

    @Test
    void aBossTakesTheSameRules() {
        List<String> rows = rows("iron-sovereign");
        assertEquals("ANTI-GROUND | X2 ON EVERY PART", rows.getFirst());
        assertTrue(rows.contains("EMP BURST | STUNS IT 3 S"));
        assertTrue(rows.contains("HOMING | EFFECTIVE"));
    }

    @Test
    void aUnitWithoutTraitsGetsAnyWeapon() {
        Path root = Path.of(System.getProperty("vanguard.designDir"));
        List<DataFile> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(p -> p.getFileName().toString().equals("data.yaml"))
                    .sorted()
                    .map(p -> {
                        try {
                            String path = root.relativize(p).toString().replace('\\', '/');
                            String text = Files.readString(p);
                            return new DataFile(
                                    path,
                                    path.equals("enemies/air/skitter/data.yaml")
                                            ? text.replace("traits: [spread, forward]", "traits: []")
                                            : text);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Content edited = ContentLoader.load(files);
        assertEquals(List.of("EMP BURST | STUNS IT 1.5 S"), rows(edited, "skitter", Optional.empty()));
        // With no rule at all: a machine-free, Vrell-free unit with no traits.
        var rules = Advice.rules(
                edited, "fleet-stormhawk", edited.enemy("fleet-stormhawk"), Optional.empty(), Optional.empty());
        assertEquals(1, rules.size());
        assertEquals("ANY WEAPON", rules.getFirst().label());
        assertTrue(rules.getFirst().anyFront());
    }

    @Test
    void theRowsListThePlayersMatchingGearFittedFirst() throws Exception {
        var save = DatabankTest.level40();
        Campaign campaign = Campaign.load(CampaignRules.of(CONTENT), DatabankTest.withSuite(save, 3, true));
        DatabankView view = Databank.of(CONTENT).view(campaign);

        Pages.EnemyPage mirror =
                view.enemyPage(Databank.of(CONTENT).entry("mirror-interceptor").orElseThrow());
        Advice.Row emp = mirror.advice().stream()
                .filter(row -> row.label().equals("EMP BURST"))
                .findFirst()
                .orElseThrow();
        assertEquals("EMP Burst", emp.gear().getFirst().name());
        assertTrue(emp.gear().getFirst().fitted());
        Advice.Row beam = mirror.advice().stream()
                .filter(row -> row.label().equals("BEAM"))
                .findFirst()
                .orElseThrow();
        assertTrue(beam.reason().startsWith("HEATS IT: BREAKS IN "), beam.reason());
        assertEquals("Ion Beam", beam.gear().getFirst().name());
        Advice.Row flanks = mirror.advice().stream()
                .filter(row -> row.label().equals("SIDE / REAR"))
                .findFirst()
                .orElseThrow();
        // The fitted Swarm Tail first, then the stored rear guns.
        assertEquals("swarm-tail", flanks.gear().getFirst().id());
        assertTrue(flanks.gear().getFirst().fitted());
        assertFalse(flanks.gear().getLast().fitted());
    }
}
