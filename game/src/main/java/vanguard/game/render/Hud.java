package vanguard.game.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import java.util.Locale;
import vanguard.content.campaign.Flight;
import vanguard.game.level.PromptTexts;
import vanguard.game.level.RadioQueue;
import vanguard.game.ui.Fonts;
import vanguard.sim.Armament;
import vanguard.sim.IdleReason;
import vanguard.sim.Sortie;

/** The level HUD in the two side panels (design/ui/hud): mission on the left, ship on the right. */
public final class Hud {
    private final MissionPanel mission;
    private final ShipPanel ship;
    private final Flight flight;
    private final double regenBonus;
    /** The flown weapons by {@link Armament.Slot}. */
    private final Flight.Weapon[] weapons = new Flight.Weapon[Armament.Slot.values().length];
    /**
     * M5 part E: per {@link Armament.Slot}, why its weapon fires nothing in this level (the HUD's NO WATER; M9 part C:
     * NO GROUND), {@link IdleReason#NONE} when it fires.
     */
    private final IdleReason[] idle = new IdleReason[Armament.Slot.values().length];

    /**
     * @param number the level number
     * @param name the level's name
     * @param launchBalance the credits the player launched with
     * @param flight what the sortie flies
     * @param regenBonus the shield regen bonus of the flight's spare power
     * @param overdriveLength how long an overdrive lasts, s
     */
    public Hud(
            Sprites sprites,
            Fonts fonts,
            TransmissionStatic transmissionStatic,
            int number,
            String name,
            int launchBalance,
            Flight flight,
            double regenBonus,
            double overdriveLength) {
        HudKit kit = new HudKit(sprites, fonts);
        mission = new MissionPanel(kit, sprites, transmissionStatic, number, name, launchBalance);
        ship = new ShipPanel(
                kit,
                overdriveLength,
                flight.loadout()
                        .special()
                        .map(special -> sprites.region("icons/" + iconName(special.name())))
                        .map(TextureRegion.class::cast)
                        .orElse(null),
                sprites.region("icons/escort-rook"),
                sprites.has("icons/escort-bulwark") ? sprites.region("icons/escort-bulwark") : null);
        this.flight = flight;
        this.regenBonus = regenBonus;
        for (Flight.Weapon weapon : flight.weapons()) {
            weapons[weapon.slot().ordinal()] = weapon;
        }
    }

    /**
     * M5 part E: per slot, why the weapon there fires nothing in the sortie's level (a torpedo pod over land; M9 part
     * C: a bomb rack with no ground, {@link Sortie#mountIdleReason}), {@link IdleReason#NONE} where it fires.
     */
    static void idle(Sortie sortie, IdleReason[] bySlot) {
        java.util.Arrays.fill(bySlot, IdleReason.NONE);
        Armament armament = sortie.armament();
        for (int m = 0; m < armament.size(); m++) {
            IdleReason reason = sortie.mountIdleReason(m);
            if (reason != IdleReason.NONE) {
                bySlot[armament.mount(m).slot().ordinal()] = reason;
            }
        }
    }

    /** The hangar icon of a special: {@code special-} and its name in lower case, hyphenated (tools/art/icons.py). */
    static String iconName(String special) {
        return "special-" + special.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
    }

    /** The special button was denied: the special's row flashes. */
    public void specialDenied() {
        ship.specialDenied();
    }

    /** M8 part A: the special's setting limit changed at a waterline: its row flashes once. */
    public void specialLimit() {
        ship.specialLimit();
    }

    /**
     * M8 part C: the ship crossed a waterline into the water: the weapon rows' {@code NO WATER} clears with one flash
     * of the weapons panel.
     */
    public void waterline() {
        ship.waterline();
    }

    /** M7 part A: a cooldown special is ready again: its HUD ring flashes white once. */
    public void specialReady() {
        ship.specialReady();
    }

    /**
     * M8 part F: the radio portrait's interference ahead of a line with a {@code static_lead} (Level 26's Choir), 0 to 1
     * this frame ({@link vanguard.game.level.StaticLeads}).
     */
    public void staticLead(float strength) {
        mission.staticLead(strength);
    }

    /** @param prompts the control prompts to show */
    public void draw(SpriteBatch batch, Sortie sortie, RadioQueue radio, List<PromptTexts.Text> prompts) {
        mission.draw(batch, sortie, radio, prompts);
        idle(sortie, idle);
        ship.draw(batch, sortie, weapons, idle, flight.sparePower(), regenBonus, flight.notFlown());
        batch.setColor(1, 1, 1, 1);
    }
}
