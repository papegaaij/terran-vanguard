package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.HITS;
import static vanguard.game.audio.FlightSounds.PICKUPS;
import static vanguard.game.audio.FlightSounds.PLAYER_DAMAGE;
import static vanguard.game.audio.FlightSounds.PLAYER_FIRE;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import vanguard.game.render.TriggerBreak;
import vanguard.sim.Armament;
import vanguard.sim.Defences;
import vanguard.sim.PickupType;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;
import vanguard.sim.WeaponSpec;

/**
 * The player's side of {@link FlightSounds}: the shots by their weapon families (a rear gun lower), the
 * proximity mines, the hits on the ship and its loss, the ground targets' and the debris' hits and
 * breaks, the pickups, a restart; and the ship's warnings that follow its state (the low-armour beeps,
 * the shield's restore chime).
 */
final class PlayerCues extends SoundFamily {
    /** The depth charge's burst at full gain (the user's playtest of 2026-10-24: large explosions that stand out). */
    static final float BURST = 1f;

    /** A proximity mine's arming beep: a quiet tick above the shots' level, below the hits'. */
    private static final float MINE_ARMING = FlightSounds.decibels(-10);
    /** A proximity mine's drop: at the arming beep's level, or the front gun firing on the same tick masks it. */
    private static final float MINE_DROPPING = MINE_ARMING;

    private static final PickupType[] PICKUP_TYPES = PickupType.values();
    /** Rear guns play their family's sound about 10 % lower (design/audio/sfx, Weapon sound families). */
    private static final float REAR_PITCH = 0.9f;
    /** A destroyed ground target at least this large (px²) crumbles like a structure, a smaller one bursts into rubble. */
    private static final double LARGE_GROUND_AREA = 1000;

    private static final float LOW_ARMOUR_LEVEL = FlightSounds.decibels(-6);
    /** M8 part A: the Depth Charges' and the Bulwark's cannon's sound families. */
    static final String DEPTH_CHARGE = "depth-charge";

    static final String BULWARK_CANNON = "bulwark-cannon";
    /** M8 part A: the Bulwark's cannon plays the ballistic family this much lower. */
    private static final float BULWARK_PITCH = 0.8f;

    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.SHOT_FIRED,
            SimEvents.Type.PROXIMITY_MINE_ARMED,
            SimEvents.Type.SHOT_GLANCED,
            SimEvents.Type.BLAST,
            SimEvents.Type.OVERDRIVE_ENDED,
            SimEvents.Type.CLAMP_HIT,
            SimEvents.Type.GROUND_HIT,
            SimEvents.Type.GROUND_DESTROYED,
            SimEvents.Type.TRIGGER_SPENT,
            SimEvents.Type.DEBRIS_HIT,
            SimEvents.Type.DEBRIS_DESTROYED,
            SimEvents.Type.PICKUP_COLLECTED,
            SimEvents.Type.SHIELD_HIT,
            SimEvents.Type.SHIELD_BROKEN,
            SimEvents.Type.ARMOUR_HIT,
            SimEvents.Type.SHIP_DESTROYED,
            SimEvents.Type.SORTIE_RESTARTED,
            SimEvents.Type.BOSS_RETRY,
            SimEvents.Type.PRIMARY_FAILED);

    private final Sfx[] shots;
    private final float[] shotPitch;
    /** M8 part A: per mount, whether its bursts are depth charges (their own under-water thud, not the small blast). */
    private final boolean[] charges;
    /** M8 part A: the harpoons' hit on a ground object. */
    private final DeepWaterCues deepWater;
    /** The bosses' sounds, told of a restart. */
    private final BossCues bosses;
    /** Which ground objects of the script are large, from the first watch; null before. */
    private boolean[] largeGround;
    /** Which ground objects of the script are triggers that break as they are spent ({@link TriggerBreak}); null before. */
    private boolean[] breakingTrigger;
    /** Steps until the next low-armour beep; 0 while the armour is not low. */
    private int beepIn;
    /** Whether the shield broke and has not been full since: its restore chime is due. */
    private boolean shieldDown;

    PlayerCues(
            SoundMix mix, Armament armament, Optional<WeaponSpec> wingman, BossCues bosses, DeepWaterCues deepWater) {
        super(mix);
        this.bosses = bosses;
        this.deepWater = deepWater;
        int mounts = armament.size() + (wingman.isPresent() ? 1 : 0);
        shots = new Sfx[mounts];
        shotPitch = new float[mounts];
        charges = new boolean[mounts];
        wingman.ifPresent(gun -> {
            shots[armament.size()] = shot(gun.sfx());
            // M8 part A: the Bulwark's cannon: the ballistic family lowered, a heavier slug (accepted in round 50).
            shotPitch[armament.size()] = gun.sfx().equals(BULWARK_CANNON) ? BULWARK_PITCH : 1;
        });
        for (int m = 0; m < armament.size(); m++) {
            if (!armament.mount(m).fires()) {
                // M7 part A: a Deflector Pod fires nothing; its catch and return sounds are its own (step A6).
                shots[m] = Sfx.PULSE_SHOT;
                shotPitch[m] = 1;
                continue;
            }
            String family = armament.mount(m).weapon().sfx();
            shots[m] = shot(family);
            charges[m] = family.equals(DEPTH_CHARGE);
            // Rear guns play their family lower; a mine's drop is not a gun's report, nor a depth charge's roll-out.
            shotPitch[m] = armament.mount(m).slot() == Armament.Slot.REAR
                            && !family.equals("mine")
                            && !family.equals(DEPTH_CHARGE)
                    ? REAR_PITCH
                    : 1;
        }
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    /** The sound of a weapon sound family; the families of later weapons play the pulse until they have theirs. */
    static Sfx shot(String family) {
        return switch (family) {
            case "vulcan" -> Sfx.VULCAN_SHOT;
            case "ballistic" -> Sfx.BALLISTIC_SHOT;
            case "laser" -> Sfx.LASER_SHOT;
            case "micromissile" -> Sfx.MICROMISSILE_SHOT;
            case "mortar" -> Sfx.MORTAR_SHOT;
            case "bomb" -> Sfx.BOMB_SHOT;
            case "missile" -> Sfx.MISSILE_SHOT;
            case "mine" -> Sfx.MINE_DROP;
            case "torpedo" -> Sfx.SHOT_TORPEDO;
            // M8 part A: the Act 4 arsenal (round 50's picks) and the Bulwark's cannon.
            case "harpoon" -> Sfx.SHOT_HARPOON;
            // M9 part A: the Tesla Coil Pod's zap, one an arc (its SHOT_FIRED comes only with a target).
            case "tesla" -> Sfx.TESLA_SHOT;
            case DEPTH_CHARGE -> Sfx.SHOT_DEPTH_CHARGE;
            case BULWARK_CANNON -> Sfx.BALLISTIC_SHOT;
            default -> Sfx.PULSE_SHOT;
        };
    }

    /** Whether the ground objects are known yet (they are from the first watch). */
    boolean groundNoted() {
        return largeGround != null;
    }

    /** At the first watch: which ground objects are large, and which triggers break as they are spent. */
    void noteGround(Sortie sortie) {
        var objects = sortie.script().groundObjects();
        largeGround = new boolean[objects.size()];
        breakingTrigger = new boolean[objects.size()];
        for (int g = 0; g < largeGround.length; g++) {
            var size = objects.get(g).size();
            largeGround[g] = size.width() * size.height() >= LARGE_GROUND_AREA;
            // M7 part C: the relay hut's door bursts with a break's sound as well.
            breakingTrigger[g] = TriggerBreak.breaks(objects.get(g)) || TriggerBreak.doorBursts(objects.get(g));
        }
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case SHOT_FIRED -> {
                int mount = events.value(i);
                // M8 part A: a depth charge's roll-out is levelled with the mine's drop (round 50).
                float level = shots[mount] == Sfx.MINE_DROP || shots[mount] == Sfx.SHOT_DEPTH_CHARGE
                        ? MINE_DROPPING
                        : PLAYER_FIRE;
                bank.play(shots[mount], level, shotPitch[mount] * pitch(0.05), pan);
            }
            case PROXIMITY_MINE_ARMED -> bank.play(Sfx.MINE_ARM, MINE_ARMING, pitch(0.03), pan);
            case SHOT_GLANCED -> bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), HITS, 1.3f * pitch(0.05), pan);
            case BLAST -> {
                int mount = events.value(i);
                if (mount >= 0 && mount < charges.length && charges[mount]) {
                    // M8 part A: a depth charge bursts; since the user's playtest of 2026-10-24 ("hardly stands out")
                    // in the under-water family's large explosion at full gain, a little pitch jitter, its own
                    // instance limit (Sfx.DEPTH_CHARGE_BURST) so a pair bursting together is heard whole.
                    bank.play(Sfx.DEPTH_CHARGE_BURST, BURST, pitch(0.04), pan);
                } else {
                    bank.play(alternate(Sfx.EXPLOSION_SMALL_A, Sfx.EXPLOSION_SMALL_B), EXPLOSIONS, pitch(0.06), pan);
                }
            }
            case OVERDRIVE_ENDED -> bank.play(Sfx.OVERDRIVE_END, PICKUPS, 1, 0);
            case CLAMP_HIT -> bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), HITS, 0.8f * pitch(0.05), pan);
            case GROUND_HIT -> {
                if (!deepWater.harpoonHit(events.value(i), pan)) {
                    bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), HITS, pitch(0.05), pan);
                }
            }
            // A destroyed ground target: its blast with its crumble.
            case GROUND_DESTROYED -> groundBreak(events.value(i), pan);
            // A trigger whose spent frame is a wreck (Level 08's billboard topples) breaks the
            // same way (TriggerBreak); the others are spent without a sound of their own.
            case TRIGGER_SPENT -> {
                int index = events.value(i);
                if (breakingTrigger != null && index < breakingTrigger.length && breakingTrigger[index]) {
                    groundBreak(index, pan);
                }
            }
            case DEBRIS_HIT -> bank.play(alternate(Sfx.HIT_METAL_A, Sfx.HIT_METAL_B), HITS, pitch(0.05), pan);
            case DEBRIS_DESTROYED ->
                bank.play(alternate(Sfx.EXPLOSION_TINY_A, Sfx.EXPLOSION_TINY_B), EXPLOSIONS, 0.8f * pitch(0.04), pan);
            case PICKUP_COLLECTED -> {
                PickupType type = PICKUP_TYPES[events.value(i)];
                bank.play(FlightSounds.pickupSound(type), PICKUPS, 1, pan);
                if (type == PickupType.OVERDRIVE) {
                    bank.play(Sfx.OVERDRIVE_START, PICKUPS, 1, 0);
                }
            }
            case SHIELD_HIT -> bank.play(Sfx.SHIELD_HIT, PLAYER_DAMAGE, pitch(0.05), pan);
            case SHIELD_BROKEN -> bank.play(Sfx.SHIELD_BREAK, PLAYER_DAMAGE, 1, pan);
            case ARMOUR_HIT -> bank.play(Sfx.ARMOUR_HIT, PLAYER_DAMAGE, pitch(0.05), pan);
            case SHIP_DESTROYED -> {
                bank.play(Sfx.SHIP_DESTROYED, PLAYER_DAMAGE, 1, pan);
                bank.play(Sfx.MISSION_FAILED, PLAYER_DAMAGE, 1, 0);
            }
            case SORTIE_RESTARTED, BOSS_RETRY -> {
                mix.clearPending();
                mix.resync = true;
                bosses.restarted();
            }
            case PRIMARY_FAILED -> bank.play(Sfx.MISSION_FAILED, PLAYER_DAMAGE, 1, 0);
            default -> {}
        }
    }

    /**
     * A ground target's blast with its crumble (round 08): a structure's collapse or a small
     * target's rubble burst, under the blast.
     */
    private void groundBreak(int index, float pan) {
        bank.play(alternate(Sfx.EXPLOSION_SMALL_C, Sfx.EXPLOSION_SMALL_A), EXPLOSIONS, pitch(0.04), pan);
        boolean large = largeGround != null && index < largeGround.length && largeGround[index];
        bank.play(large ? Sfx.CRUMBLE_LARGE : Sfx.CRUMBLE_SMALL, 0.7f * EXPLOSIONS, pitch(0.05), pan);
    }

    /**
     * The ship's warnings: the low-armour beeps while it flies, and the shield's restore chime once
     * it is full again after a break (not after every hit: it regenerates after each).
     */
    void watchShip(Sortie sortie) {
        Defences defences = sortie.ship().defences();
        boolean flying = sortie.flying() && !sortie.complete();
        int period = flying ? FlightSounds.beepPeriod(defences.armour(), defences.maxArmour()) : 0;
        if (period == 0) {
            beepIn = 0;
        } else if (--beepIn <= 0) {
            bank.play(Sfx.LOW_ARMOUR, LOW_ARMOUR_LEVEL, 1, 0);
            beepIn = period;
        } else {
            beepIn = Math.min(beepIn, period);
        }
        if (mix.resync || !flying) {
            shieldDown = false;
        } else if (defences.broken()) {
            shieldDown = true;
        } else if (shieldDown && defences.shield() >= defences.maxShield()) {
            bank.play(Sfx.SHIELD_RESTORE, PICKUPS, 1, 0);
            shieldDown = false;
        }
    }
}
