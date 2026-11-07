package vanguard.sim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * M11 part F, step F1 (design/campaign Level 48, the reactor cores; {@link VrellLevel#bottomBand()}, {@link
 * Sortie#leaving()}): the HUD's leaving blink counts a live unit of the secondary's escapes kind while its centre is
 * within the level's bottom band (120 px) of the field's bottom edge, and never without a band.
 */
class LeavingBandTest {
    private static final EnemySpec CORE =
            EnemySpec.of("reactor-core", 40, new Hitbox(32, 32), Layer.GROUND, 0, false, 10, 0);

    @Test
    void aLiveCoreInTheBottomBandBlinks() {
        for (double band : new double[] {120, 0}) {
            LevelScript level = Act7CDSpecs.level(
                            30,
                            100,
                            List.of(),
                            List.of(new GroundUnit(0, 60, CORE, -1)),
                            VrellLevel.NONE.withBottomBand(band))
                    .toBuilder()
                    .objectives(ObjectiveSpec.NONE.withSecondary(Secondary.builder()
                            .credits(100)
                            .escapes("reactor-core")
                            .escapesAllowed(2)
                            .label("CORES")
                            .build()))
                    .build();
            Sortie sortie = Act7CDSpecs.sortie(level);
            boolean blinked = false;
            for (int tick = 0; tick < SimStep.ticks(6.5); tick++) {
                sortie.step(Command.NONE);
                Enemy core = Act7CDSpecs.find(sortie, "reactor-core");
                boolean low = core != null
                        && band > 0
                        && core.y() < band
                        && PlayField.overlaps(core.x(), core.y(), core.hitbox());
                assertEquals(low ? 1 : 0, sortie.leaving(), "at " + tick);
                blinked |= low;
            }
            assertEquals(band > 0, blinked, "band " + band);
        }
        assertTrue(VrellLevel.NONE.bottomBand() == 0);
    }
}
