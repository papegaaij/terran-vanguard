package vanguard.game.render;

import java.util.List;
import java.util.function.Predicate;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.sim.Armament;
import vanguard.sim.Command;
import vanguard.sim.SetPiece;
import vanguard.sim.Sortie;

/**
 * Step G6 (the looks tests of Levels 49 and 50): a blunt pilot for flying a boss fight to its later stages fast, on an
 * invulnerable ship with heavy guns: it sits low on the field under the weakest living part it can hurt (not
 * shielded) of the bosses {@code wanted} accepts, firing all the time. A test's tool, not a measure of the fight.
 */
final class BossPilot {
    private BossPilot() {}

    /** An invulnerable sortie of level {@code key} at medium with the ion beam and two missile pods at level 5. */
    static Sortie sortie(Content content, String key) {
        return new Sortie(
                1,
                SimSpecs.loadout(
                        content,
                        content.systems().engines().getLast().name(),
                        List.of(
                                new SimSpecs.FittedWeapon(Armament.Slot.FRONT, "ion-beam", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.LEFT_WING, "micro-missile-pod", 5),
                                new SimSpecs.FittedWeapon(Armament.Slot.RIGHT_WING, "micro-missile-pod", 5)),
                        content.shields().models().getFirst().name(),
                        content.armour().plating().getFirst().name(),
                        200,
                        Difficulty.MEDIUM),
                SimSpecs.level(content, key, Difficulty.MEDIUM),
                SimSpecs.rules(content, key, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
    }

    /** This step's commands: fire, low on the field, under the weakest hurtable part of a wanted boss (else centred). */
    static int commands(Sortie sortie, Predicate<SetPiece> wanted) {
        int commands = Command.FIRE.bit();
        double tx = 240;
        double low = Double.MAX_VALUE;
        for (int k = 0; k < sortie.setPieceCount(); k++) {
            SetPiece piece = sortie.setPiece(k);
            if (!wanted.test(piece) || !piece.present() || piece.destroyed()) {
                continue;
            }
            for (int p = 0; p < piece.partCount(); p++) {
                if (piece.partHp(p) > 0 && !piece.partShielded(p) && piece.partHp(p) < low) {
                    low = piece.partHp(p);
                    tx = piece.partX(p);
                }
            }
        }
        double x = sortie.ship().x();
        if (x < tx - 4) {
            commands |= Command.RIGHT.bit();
        } else if (x > tx + 4) {
            commands |= Command.LEFT.bit();
        }
        if (sortie.ship().y() > 80) {
            commands |= Command.DOWN.bit();
        } else if (sortie.ship().y() < 60) {
            commands |= Command.UP.bit();
        }
        return commands;
    }
}
