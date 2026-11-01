package vanguard.game.render;

import java.util.List;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;
import vanguard.sim.Squadron;
import vanguard.sim.Tow;

/**
 * M10 part F: Level 40 flown for the game's tests (step F4's sounds, step F6g's last ace): medium, the ship invulnerable
 * with the Ion Beam and two Autocannon Pods, steered under Sentry Two's recorder while it holds on the screen, else under
 * the nearest ace on the field, else the middle; low.
 */
public final class Level40Fixture {
    private static final Content CONTENT = ContentLoader.fromClasspath();
    private static final String KEY = CONTENT.levelKey(40).orElseThrow();

    private Level40Fixture() {}

    /** Medium, the ship invulnerable with the Ion Beam and two Autocannon Pods. */
    public static Sortie sortie() {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        CONTENT,
                        CONTENT.systems().engines().getFirst().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "autocannon-pod", 3),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "autocannon-pod", 3)),
                        CONTENT.shields().models().getFirst().name(),
                        CONTENT.armour().plating().getFirst().name(),
                        20,
                        Difficulty.MEDIUM),
                SimSpecs.level(CONTENT, KEY, Difficulty.MEDIUM),
                SimSpecs.rules(CONTENT, KEY, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** Under the recorder while it holds on the screen, else under the nearest ace on the field, else the middle; low. */
    public static int steer(Sortie sortie) {
        double target = 240;
        double y = 70;
        boolean recorder = false;
        for (int i = 0; i < sortie.towCount(); i++) {
            Tow tow = sortie.tow(i);
            if (tow.present() && tow.holding() && tow.renderY(1) < 600 && tow.renderY(1) > 150) {
                target = tow.renderX(1);
                recorder = true;
            }
        }
        for (int k = 0; k < sortie.setPieceCount() && !recorder; k++) {
            SetPiece piece = sortie.setPiece(k);
            Squadron squadron = piece.squadron().orElse(null);
            if (squadron == null || !piece.present()) {
                continue;
            }
            double best = Double.POSITIVE_INFINITY;
            for (int a = 0; a < squadron.aceCount(); a++) {
                if (squadron.touchable(a) && squadron.y(a) > sortie.ship().y() + 40) {
                    double d = Math.abs(squadron.x(a) - sortie.ship().x());
                    if (d < best) {
                        best = d;
                        target = squadron.x(a);
                    }
                }
            }
        }
        int commands = 0;
        if (sortie.ship().x() < target - 3) {
            commands |= Command.RIGHT.bit();
        } else if (sortie.ship().x() > target + 3) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() < y - 3) {
            commands |= Command.UP.bit();
        } else if (sortie.ship().y() > y + 3) {
            commands |= Command.DOWN.bit();
        }
        return commands;
    }
}
