package vanguard.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.ItemKind;
import vanguard.sim.Armament;
import vanguard.sim.DroneSpec;

/**
 * M7 part G (step G2): the Rear-guard Drone Bay's data (design/player/weapons/rear-guard-drone-bay; the wingmen
 * document's numbers, approved in round 36 D.26): a wing pod with the {@code rear} trait whose drone trails the ship
 * ({@code station: trail}) and fires straight back, priced and unlocked from the data, in the live shop and flown on
 * its trail station (step G4, {@link RetreatRules#trail}).
 */
class RearGuardDroneBayDataTest {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String BAY = "rear-guard-drone-bay";

    @Test
    void theBayIsAWingPodWithATrailingDroneThatFiresBack() {
        WeaponData bay = CONTENT.weapon(BAY);
        assertEquals(WeaponData.Slot.WING, bay.slot());
        assertEquals(List.of("rear"), bay.traits());
        assertEquals(2400, bay.price());
        assertEquals(20, bay.unlock());
        assertEquals(Optional.of(true), bay.pod());
        WeaponData.Drone drone = bay.drone().orElseThrow();
        assertTrue(drone.trails());
        assertEquals(Optional.of(40.0), drone.behind());
        assertEquals(Optional.of(12.0), drone.side());
        assertEquals(Optional.of(0.15), drone.lag());
        assertEquals(Optional.of(WeaponData.Fire.BACK), drone.fire());
        assertTrue(drone.orbit().isEmpty()
                && drone.period().isEmpty()
                && drone.converge().isEmpty());
        assertEquals(
                drone.contact(),
                CONTENT.weapon("light-drone-bay").drone().orElseThrow().contact());
    }

    @Test
    void itsLevelsAreTheWingmenTables() {
        WeaponData bay = CONTENT.weapon(BAY);
        assertEquals(
                List.of(9.0, 12.0, 16.2, 21.0, 25.8),
                bay.levels().stream()
                        .map(level -> Math.round(level.rate() * level.damage() * 10) / 10.0)
                        .toList());
        assertEquals(
                List.of(8, 9, 10, 11, 12),
                bay.levels().stream()
                        .map(level -> level.droneHits().orElseThrow())
                        .toList());
        assertEquals(
                List.of(6.0, 5.5, 5.0, 4.5, 4.0),
                bay.levels().stream()
                        .map(level -> level.rebuild().orElseThrow())
                        .toList());
    }

    @Test
    void theShopSellsItFromTheLevelTwentyVisitAndTheSimulationFliesItsTrail() {
        Catalogue.Item item = Catalogue.of(CONTENT).item(ItemKind.WING, BAY);
        assertEquals(List.of(1200, 2400, 4800, 9600), item.upgrades());
        assertEquals(List.of(2.0, 2.5, 2.5, 3.0, 3.0), item.draws());
        assertTrue(SimSpecs.flies(CONTENT, BAY), "the trail station is mapped (step G4)");
        assertTrue(SimSpecs.flies(CONTENT, "light-drone-bay"));
    }

    @Test
    void itsMountFliesTheDroneOnItsTrailStationFiringBack() {
        Armament.Mount mount = SimSpecs.mount(CONTENT, Armament.Slot.LEFT_WING, BAY, 3, 0);
        assertTrue(mount.bay());
        DroneSpec drone = mount.drone();
        assertEquals(DroneSpec.Station.TRAIL, drone.station());
        assertEquals(40, drone.behind());
        assertEquals(12, drone.side());
        assertEquals(0.15, drone.lagSeconds());
        assertTrue(drone.fireBack());
        assertEquals(10, drone.hits());
        assertEquals(5, drone.rebuildSeconds());
        assertFalse(mount.droneOverdrive().station() == DroneSpec.Station.ORBIT, "the overdrive trails too");
        assertEquals(11, mount.droneOverdrive().hits());
        assertEquals(6 * 2.7, mount.weapon().rate() * mount.weapon().damage(), 1e-9);
    }
}
