package vanguard.game.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.List;
import java.util.Locale;
import vanguard.content.campaign.Flight;
import vanguard.game.level.LowArmour;
import vanguard.sim.Defences;
import vanguard.sim.Drones;
import vanguard.sim.Sortie;
import vanguard.sim.SpecialSlot;
import vanguard.sim.Wingman;

/**
 * The right HUD panel (design/ui/hud, ship): armour and shield bars with their numbers (the armour
 * gauge flashes red at 30 % and faster at 15 %, {@link LowArmour}; the shield bar flickers while it
 * is down after a break), the spare power with its shield regen bonus (lit
 * up during an overdrive), the four weapon slots with their level pips and the overdrive timer,
 * and the special's row in the same style: its 16 px hangar icon, name and charges, greyed while
 * its strike flies or with no charge left, flashing red when the special button is denied. Under
 * the special the escort box (M5 part A) while Rook flies in the level: the {@code ESCORT} plate, his
 * 16 px hangar icon and {@code ROOK}, his armour as a bar with the number, flashing red at 30 % as
 * the player's; after he ejects the bar is empty and the well reads {@code EJECTED} in red. Without
 * an escort its region stays empty. Under it: the fitted items the sortie leaves out, as "not yet
 * available".
 *
 * <p>M7 part A: a cooldown special's ring and its seconds or {@code READY} in a compact special row
 * ({@link #compact}), the Shield Overcharge's white cores in the shield bar, the Evasive thrusters'
 * dash pip right of the shield's label plate, a drone bay's hits strip under its weapons row.
 */
final class ShipPanel {
    private static final int X = PixelScreen.WIDTH - HudKit.PANEL_WIDTH;
    private static final int BAR_HEIGHT = 12;
    private static final int LEVEL_PIPS = 5;
    /** The spare power that gives the most regen bonus, MW (design/player/generator: +10 % per MW, at most +50 %). */
    private static final double FULL_SPARE = 5;
    /** The fitted items that do not fly yet: two bays and the special. */
    private static final int MAX_NOT_FLOWN = 3;
    /** The shield bar flickers at this many frames per phase while it is down after a break. */
    private static final int FLICKER_FRAMES = 8;

    private static final int ROW = 14;
    private static final int OVERDRIVE_PIPS = 10;
    /** The special's row flashes red this many frames after a denied press, switching every few. */
    private static final int DENIED_FRAMES = 30;

    private static final int DENIED_PHASE_FRAMES = 5;
    /**
     * The armour gauge's red flash: lit for this many simulation steps, then dark as long, at 30 %
     * and at 15 %: half the low-armour beep's period (1.2 s and 0.6 s, FlightSounds), one flash a beep.
     */
    static final int LOW_ARMOUR_PHASE_TICKS = 36;

    static final int CRITICAL_ARMOUR_PHASE_TICKS = 18;
    private static final float GREYED = 0.35f;
    /**
     * The special's row: the icon's and the name's left after the word {@code SPECIAL}, or (M7 part A)
     * in the compact row without it; the count right-aligned 6 px in; the longest name beside the word.
     */
    static final int ICON_X = 64;

    static final int NAME_X = 84;
    static final int COMPACT_ICON_X = 8;
    static final int COMPACT_NAME_X = 32;
    static final int COUNT_RIGHT = HudKit.INNER_WIDTH - 6;
    static final int NAME_CHARS = 10;
    /** The HUD font's glyph width, px; M8 part A: the gap between a cut name and the reason word. */
    static final int GLYPH_WIDTH = 8;

    static final int REASON_GAP = 6;
    /** M7 part A: a cooldown special's ring: its dots and radius round the 16 px icon, its colours. */
    private static final int RING_DOTS = 28;

    private static final float RING_RADIUS = 10.5f;
    static final Color RING_COOLING = Color.valueOf("1E7A80");
    static final Color RING_READY = Color.valueOf("00C8D0");
    /**
     * M7 part A: how long the ring's and the dash pip's white flash last as they get ready, simulation
     * ticks (0.2 s); it counts time, not rendered frames, so an uncapped frame rate does not shorten it.
     */
    static final int READY_FLASH_TICKS = Math.round(0.2f * vanguard.sim.SimStep.PER_SECOND);
    /** What a ready cooldown special's row reads. */
    static final String READY = "READY";
    /** M7 part A: the overcharge's second layer, white cores in the shield gauge's cells. */
    static final Color OVERCHARGE_CORE = Color.WHITE;
    /** M7 part A: the dash pip right of the shield's label plate, its size and its flash. */
    static final int DASH_PIP_X = 126;

    static final int DASH_PIP_WIDTH = 16;
    static final int DASH_PIP_HEIGHT = 6;
    private static final int DASH_PIP_DROP = 9;
    /** M7 part A: a drone bay's hits strip under its row's level pips. */
    private static final int DRONE_STRIP_DROP = 12;

    private static final int DRONE_STRIP_HEIGHT = 2;
    /** The special's row: its well's top below the panel's top inset, and its height. */
    static final int SPECIAL_TOP = 258;

    static final int SPECIAL_HEIGHT = ROW + 10;
    /** The escort's plate, below the special's row, and its well under the plate. */
    static final int ESCORT_PLATE = 292;

    static final int ESCORT_TOP = ESCORT_PLATE + 22;
    static final int ESCORT_HEIGHT = 38;
    /** The items that do not fly yet, under the escort's region (empty without an escort). */
    static final int NOT_FLOWN_PLATE = ESCORT_TOP + ESCORT_HEIGHT + 18;
    /** The escort's well: the icon's and the name's left, the bar's inset and height. */
    static final int ESCORT_ICON_X = 6;

    static final int ESCORT_NAME_X = 28;
    static final int ESCORT_BAR_INSET = 6;
    static final int ESCORT_BAR_HEIGHT = 8;
    static final String ESCORT_NAME = "ROOK";
    static final String EJECTED = "EJECTED";
    /** M8 part F: what his box reads from his loss on (Level 26, design/player/wingmen, Rook's loss): missing. */
    static final String MIA = "MIA";
    /** M8 part A: the Bulwark's name in the escort box, and what it reads once it is lost. */
    static final String BULWARK_NAME = "BULWARK";

    static final String LOST = "LOST";
    /** M8 part A: the Auto-repair nanites' pip, its drop below the armour's label top. */
    static final String REPAIR_PIP = "+";

    private static final int REPAIR_PIP_DROP = 2;
    /** M8 part A: the special row's flash at a change of its setting limit: 0.5 s at 60 frames a second. */
    private static final int LIMIT_FLASH_FRAMES = 30;

    private static final Color ARMOUR = Color.valueOf("FF4400");
    private static final Color ARMOUR_EMPTY = Color.valueOf("2A0B00");
    /** The low-armour flash laid over the armour gauge: the HUD's alert red, see-through. */
    private static final Color ALARM = new Color(HudKit.ALERT.r, HudKit.ALERT.g, HudKit.ALERT.b, 0.45f);

    private static final Color SHIELD = Color.valueOf("00FFFF");
    private static final Color SHIELD_EMPTY = Color.valueOf("002A2A");
    private static final Color POWER = Color.valueOf("40FF80");
    private static final Color POWER_EMPTY = Color.valueOf("0B2A15");
    private static final Color PIP_EMPTY = Color.valueOf("3A3A20");
    /**
     * M5 part E: what a weapon that cannot fire in the level reads in its row instead of its pips: a
     * Torpedo Pod without water (design/ui/hud; it still draws its power).
     */
    static final String NO_WATER = "NO WATER";
    /** M8 part A: the room for a weapon's name before {@code NO WATER} in its row, px (14 of the font's 8 px glyphs). */
    static final int NO_WATER_NAME_WIDTH = HudKit.INNER_WIDTH - 6 - 20 - (NO_WATER.length() + 1) * GLYPH_WIDTH;
    /**
     * M5 part F: the shield gauge's label while a Lamprey drains the shield (design/ui/hud, Level 12), in
     * the warning colour; it fits the label plate ({@code PartFHudTest}).
     */
    static final String SHIELD_DRAIN = "SHIELD DRAIN";

    static final Color DRAIN_COLOUR = HudKit.AMBER;
    /** The slot letters of the mock, front, rear, left and right (design/ui/hud). */
    private static final String[] SLOT_LETTERS = {"F", "R", "L", "R"};

    private final HudKit kit;
    /** How long an overdrive lasts (design/player, in-level pickups), the timer's full length. */
    private final double overdriveLength;

    /** The fitted special's 16 px hangar icon; {@code null} without a special that flies. */
    private final TextureRegion specialIcon;
    /** Rook's 16 px hangar icon. */
    private final TextureRegion escortIcon;
    /** M8 part A: the Bulwark's 16 px hangar icon (Rook's without it). */
    private final TextureRegion bulwarkIcon;
    /** M8 part A: the special row's one flash at a change of its setting limit, frames left. */
    private int limitFlash;
    /** M8 part C: the weapons panel's one flash as its {@code NO WATER} rows clear at a waterline, frames left. */
    private int waterFlash;
    /** "×" when the font has it, else "x". */
    private final String times;

    private int frame;
    private int denied;
    /**
     * M7 part A: the tick the cooldown ring's white ready flash started, and the dash pip's; a ready
     * special waits for the next draw to take its tick.
     */
    private long readyFlashTick = NO_FLASH;

    private long dashFlashTick = NO_FLASH;
    private boolean readyFlashPending;
    private boolean dashWasReady = true;
    /** The tick of a flash that never started (far enough back not to overflow). */
    private static final long NO_FLASH = Long.MIN_VALUE / 2;
    /** M7 part A: the special's name as its row shows it, from the first draw (it fits its room). */
    private String specialLabel;

    ShipPanel(
            HudKit kit,
            double overdriveLength,
            TextureRegion specialIcon,
            TextureRegion escortIcon,
            TextureRegion bulwarkIcon) {
        this.kit = kit;
        this.overdriveLength = overdriveLength;
        this.specialIcon = specialIcon;
        this.escortIcon = escortIcon;
        this.bulwarkIcon = bulwarkIcon == null ? escortIcon : bulwarkIcon;
        times = kit.small.getData().hasGlyph('\u00d7') ? "\u00d7" : "x";
    }

    /** The special button was denied: the special's row flashes. */
    void specialDenied() {
        denied = DENIED_FRAMES;
    }

    /** M8 part A: the special's setting limit changed: its row flashes once (white). */
    void specialLimit() {
        limitFlash = LIMIT_FLASH_FRAMES;
    }

    /** M8 part C: the ship crossed into the water: the weapons panel flashes once (white) as NO WATER clears. */
    void waterline() {
        waterFlash = LIMIT_FLASH_FRAMES;
    }

    /** M8 part C: whether the weapons panel shows its waterline flash now. */
    boolean waterFlashing() {
        return waterFlash > 0;
    }

    /** M7 part A: a cooldown special is ready: its ring flashes white once. */
    void specialReady() {
        readyFlashPending = true;
    }

    /** M7 part A: whether a ready flash started at tick {@code start} still shows at tick {@code tick}. */
    static boolean flashing(long tick, long start) {
        return tick - start >= 0 && tick - start < READY_FLASH_TICKS;
    }

    /**
     * @param weapons the flown weapons by slot (front, rear, left wing, right wing), {@code null} for an empty slot
     * @param sparePower MW
     * @param regenBonus the shield regen bonus of the spare power, 0..0.5
     */
    void draw(
            SpriteBatch batch,
            Sortie sortie,
            Flight.Weapon[] weapons,
            boolean[] idle,
            double sparePower,
            double regenBonus,
            List<String> notFlown) {
        frame++;
        if (readyFlashPending) {
            readyFlashTick = sortie.tick();
            readyFlashPending = false;
        }
        Defences defences = sortie.ship().defences();
        kit.rightPanel(batch);
        int x = X + HudKit.INSET;
        int y = HudKit.TOP - HudKit.INSET;
        boolean alarm = armourFlash(LowArmour.of(defences), sortie.tick());
        gauge(
                batch,
                "ARMOUR",
                HudKit.LABEL,
                defences.armour(),
                defences.maxArmour(),
                ARMOUR,
                ARMOUR_EMPTY,
                x,
                y,
                true,
                alarm);
        // M5 part F: a Lamprey draining the shield flickers it as a break does, under SHIELD DRAIN.
        boolean draining = sortie.draining();
        boolean flicker = (defences.broken() || draining) && frame / FLICKER_FRAMES % 2 == 0;
        gauge(
                batch,
                draining ? SHIELD_DRAIN : "SHIELD",
                draining ? DRAIN_COLOUR : HudKit.LABEL,
                defences.shield(),
                defences.maxShield(),
                SHIELD,
                SHIELD_EMPTY,
                x,
                y - 52,
                !flicker,
                false);
        if (!flicker && defences.shield() > defences.maxShield()) {
            // M7 part A: the Shield Overcharge's second layer as white cores in the filled cells (the bar
            // keeps its scale).
            kit.cores(
                    batch,
                    OVERCHARGE_CORE,
                    x,
                    y - 52 - 34,
                    HudKit.INNER_WIDTH,
                    BAR_HEIGHT,
                    secondLayer(defences.shield(), defences.maxShield()));
        }
        if (sortie.ship().thrustersFitted()) {
            dashPip(batch, sortie.ship().dashReadiness(), sortie.ship().dashReady(), sortie.tick(), x, y - 52);
        }
        power(batch, sparePower, regenBonus, sortie.overdriveSeconds() > 0, x, y - 104);
        weapons(batch, sortie, weapons, idle, sortie.overdriveSeconds(), x, y - 148);
        special(batch, sortie.special(), defences, flashing(sortie.tick(), readyFlashTick), x, y - SPECIAL_TOP);
        if (denied > 0) {
            denied--;
        }
        if (limitFlash > 0) {
            limitFlash--;
        }
        if (waterFlash > 0) {
            waterFlash--;
        }
        if (defences.repairing()) {
            // M8 part A: the Auto-repair nanites' + pip right of the armour's label plate while they repair.
            kit.text(batch, kit.small, REPAIR_PIP, POWER, x + DASH_PIP_X + 4, y - REPAIR_PIP_DROP);
        }
        Wingman rook = sortie.wingman().orElse(null);
        if (rook != null) {
            escort(batch, rook, sortie.tick(), x, y);
        }
        // M8 part A: the Bulwark in the escort slot, the same box.
        sortie.bulwark()
                .ifPresent(bulwark -> escortBox(
                        batch,
                        bulwarkIcon,
                        BULWARK_NAME,
                        bulwark.destroyed(),
                        LOST,
                        bulwark.armour(),
                        bulwark.maxArmour(),
                        sortie.tick(),
                        x,
                        y));
        if (!notFlown.isEmpty()) {
            kit.label(batch, "NOT YET AVAILABLE", x, y - NOT_FLOWN_PLATE);
            for (int i = 0; i < Math.min(notFlown.size(), MAX_NOT_FLOWN); i++) {
                String name = notFlown.get(i).toUpperCase(Locale.ROOT);
                kit.text(batch, kit.small, name, HudKit.LABEL, x + 8, y - NOT_FLOWN_PLATE - 18 - i * 13);
            }
        }
    }

    /**
     * The escort box under the special's row: {@code ESCORT}, then {@code [icon] ROOK} with his armour
     * number over his armour bar; the bar empty and {@code EJECTED} in red once he is out; M8 part F: {@code MIA} from
     * his loss (not an eject).
     */
    private void escort(SpriteBatch batch, Wingman rook, long tick, int x, int y) {
        boolean lost = rook.loss().lost();
        escortBox(
                batch,
                escortIcon,
                ESCORT_NAME,
                rook.ejected() || lost,
                lost ? MIA : EJECTED,
                rook.armour(),
                rook.maxArmour(),
                tick,
                x,
                y);
    }

    /**
     * The escort box: {@code ESCORT}, then the escort's icon and name with its armour number over its armour bar; the
     * bar empty and {@code outWord} in red once it is out (Rook ejected, the Bulwark lost).
     */
    private void escortBox(
            SpriteBatch batch,
            TextureRegion icon,
            String name,
            boolean out,
            String outWord,
            double armour,
            double maxArmour,
            long tick,
            int x,
            int y) {
        kit.label(batch, "ESCORT", x, y - ESCORT_PLATE);
        int top = y - ESCORT_TOP;
        kit.lcd(batch, x, top - ESCORT_HEIGHT, HudKit.INNER_WIDTH, ESCORT_HEIGHT);
        boolean alarm = !out && armourFlash(LowArmour.of(armour, maxArmour), tick);
        kit.glow(batch, out ? HudKit.ALERT : HudKit.READOUT, x, top - ESCORT_HEIGHT, HudKit.INNER_WIDTH, ESCORT_HEIGHT);
        int rowTop = top - 5;
        batch.draw(icon, x + ESCORT_ICON_X, top - 4 - icon.getRegionHeight());
        kit.text(batch, kit.small, name, out ? HudKit.LABEL : HudKit.READOUT, x + ESCORT_NAME_X, rowTop);
        String number = out ? outWord : Integer.toString((int) Math.ceil(armour));
        kit.textRight(
                batch,
                kit.small,
                number,
                out || alarm ? HudKit.ALERT : HudKit.LABEL,
                x,
                rowTop,
                HudKit.INNER_WIDTH - ESCORT_BAR_INSET);
        int barX = x + ESCORT_BAR_INSET;
        int barWidth = HudKit.INNER_WIDTH - 2 * ESCORT_BAR_INSET;
        int barY = top - ESCORT_HEIGHT + ESCORT_BAR_INSET;
        kit.fill(batch, ARMOUR_EMPTY, barX, barY, barWidth, ESCORT_BAR_HEIGHT);
        double share = out ? 0 : Math.clamp(armour / maxArmour, 0, 1);
        kit.fill(batch, ARMOUR, barX, barY, (float) Math.floor(barWidth * share), ESCORT_BAR_HEIGHT);
        if (alarm) {
            kit.fill(batch, ALARM, barX, barY, barWidth, ESCORT_BAR_HEIGHT);
            kit.glow(batch, HudKit.ALERT, barX - 4, barY - 4, barWidth + 8, ESCORT_BAR_HEIGHT + 8);
        }
    }

    /**
     * The special's row below the weapons box: {@code SPECIAL [icon] AIRSTRIKE ×2}; M7 part A: a cooldown
     * special's icon in its ring (design/player/specials, Cooldown specials and the HUD ring), filling
     * clockwise from 12 o'clock in dim cyan while it cools with the whole seconds left ({@code 27s}),
     * full and {@code READY} once it is ready (a white flash as it gets there), and while the Shield
     * Overcharge lasts a white arc draining anticlockwise over it. The Shield Overcharge's name is too
     * wide for the row: it reads {@code OVERCHARGE}.
     */
    private void special(
            SpriteBatch batch, SpecialSlot special, Defences defences, boolean readyFlash, int x, int top) {
        int height = SPECIAL_HEIGHT;
        kit.lcd(batch, x, top - height, HudKit.INNER_WIDTH, height);
        boolean flash = denied > 0 && denied / DENIED_PHASE_FRAMES % 2 == 0;
        // M8 part A: one white flash as the setting limit changes at a waterline (the denied flash wins).
        boolean limitFlashing = !flash && limitFlash > 0;
        kit.glow(
                batch,
                flash ? HudKit.ALERT : limitFlashing ? Color.WHITE : HudKit.READOUT,
                x,
                top - height,
                HudKit.INNER_WIDTH,
                height);
        int rowTop = top - 5;
        if (!special.fitted() || specialIcon == null) {
            kit.text(batch, kit.small, "SPECIAL", flash ? HudKit.ALERT : HudKit.LABEL, x + 6, rowTop);
            kit.text(batch, kit.small, "-", HudKit.LABEL, x + NAME_X, rowTop);
            return;
        }
        if (specialLabel == null) {
            specialLabel = specialLabel(special.name());
        }
        // M7 part A: a cooldown special (its ring) or a name too wide beside the word: the icon takes
        // the word's place at the row's left.
        // M8 part A: a special the setting forbids is greyed with its reason word where the count stands, in the
        // compact
        // row (its name needs the room).
        String reason = special.limit().word();
        boolean compact = compact(specialLabel, special.cooldown()) || !reason.isEmpty();
        if (!compact) {
            kit.text(batch, kit.small, "SPECIAL", flash ? HudKit.ALERT : HudKit.LABEL, x + 6, rowTop);
        }
        int iconX = x + (compact ? COMPACT_ICON_X : ICON_X);
        boolean ready = special.ready() && reason.isEmpty();
        float shade = ready ? 1 : GREYED;
        batch.setColor(1, 1, 1, shade);
        batch.draw(specialIcon, iconX, top - 4 - specialIcon.getRegionHeight());
        batch.setColor(Color.WHITE);
        Color colour = flash ? HudKit.ALERT : ready ? HudKit.READOUT : HudKit.LABEL;
        String count;
        if (special.cooldown()) {
            ring(
                    batch,
                    special,
                    defences,
                    iconX + specialIcon.getRegionWidth() / 2f,
                    top - 4 - specialIcon.getRegionHeight() / 2f,
                    readyFlash);
            count = cooldownText(special.ready(), special.cooldownSeconds());
        } else {
            count = times + special.charges();
        }
        int nameX = compact ? COMPACT_NAME_X : NAME_X;
        if (!reason.isEmpty()) {
            // The reason word takes the count's place, the name the room up to it (its first word if it is too long).
            int room = COUNT_RIGHT - nameX - reasonWidth(reason) - REASON_GAP;
            kit.text(batch, kit.small, cutLabel(specialLabel, room), colour, x + nameX, rowTop, room);
            kit.textRight(batch, kit.small, reason, flash ? HudKit.ALERT : HudKit.AMBER, x, rowTop, COUNT_RIGHT);
            return;
        }
        kit.text(batch, kit.small, specialLabel, colour, x + nameX, rowTop, COUNT_RIGHT - nameX);
        kit.textRight(batch, kit.small, count, colour, x, rowTop, COUNT_RIGHT);
    }

    /**
     * M8 part A: a special's label cut to {@code room} px beside the reason word: whole, else its first word ({@code
     * ORBITAL}), else as much as fits.
     */
    static String cutLabel(String label, int room) {
        int chars = Math.max(0, room / GLYPH_WIDTH);
        if (label.length() <= chars) {
            return label;
        }
        int space = label.indexOf(' ');
        if (space > 0 && space <= chars) {
            return label.substring(0, space);
        }
        return label.substring(0, chars);
    }

    /** M8 part A: the reason word's width in the HUD font (8 px glyphs). */
    static int reasonWidth(String reason) {
        return reason.length() * GLYPH_WIDTH;
    }

    /**
     * M7 part A: whether the special's row is compact, its icon at the row's left in place of the word
     * {@code SPECIAL}: for a cooldown special (its icon in the ring, its seconds or {@code READY} after
     * the name) and for a name longer than {@value #NAME_CHARS} characters ({@code ORBITAL LANCE}); the
     * HUD font's glyphs are 8 px wide.
     */
    static boolean compact(String label, boolean cooldown) {
        return cooldown || label.length() > NAME_CHARS;
    }

    /** M7 part A: a special's name as the HUD's special row shows it (design/ui/hud). */
    static String specialLabel(String name) {
        String label = name.toUpperCase(Locale.ROOT);
        return label.equals("SHIELD OVERCHARGE") ? "OVERCHARGE" : label;
    }

    /** M7 part A: what a cooldown special's row reads after its name: the whole seconds left, or {@code READY}. */
    static String cooldownText(boolean ready, double secondsLeft) {
        return ready || secondsLeft <= 0 ? READY : (int) Math.ceil(secondsLeft) + "s";
    }

    /** M7 part A: the share of the shield gauge's cells that show the overcharge's white cores. */
    static double secondLayer(double shield, double maxShield) {
        return maxShield <= 0 ? 0 : Math.clamp((shield - maxShield) / maxShield, 0, 1);
    }

    /**
     * M7 part A: the cooldown ring round the special's icon centred on ({@code cx}, {@code cy}): dots
     * clockwise from 12 o'clock, lit in dim cyan as far as the cooldown has run, all lit (cyan) when
     * ready, white for a moment as it gets there; the overcharge's white arc on it while that lasts.
     */
    private void ring(
            SpriteBatch batch, SpecialSlot special, Defences defences, float cx, float cy, boolean readyFlash) {
        double share = special.cooldownShare();
        boolean ready = special.ready();
        double effect = 0;
        if (special.overcharge() != null && defences.overcharged()) {
            effect = defences.overchargeTicks()
                    / Math.max(1.0, special.overcharge().seconds() * vanguard.sim.SimStep.PER_SECOND);
        }
        for (int k = 0; k < RING_DOTS; k++) {
            double angle = 2 * Math.PI * (k + 0.5) / RING_DOTS;
            Color colour;
            if (readyFlash) {
                colour = Color.WHITE;
            } else if ((k + 0.5) / RING_DOTS < effect) {
                colour = Color.WHITE;
            } else if (ready) {
                colour = RING_READY;
            } else if ((k + 0.5) / RING_DOTS < share) {
                colour = RING_COOLING;
            } else {
                colour = PIP_EMPTY;
            }
            float dx = (float) (Math.sin(angle) * RING_RADIUS);
            float dy = (float) (Math.cos(angle) * RING_RADIUS);
            kit.flat(batch, colour, Math.round(cx + dx - 1), Math.round(cy + dy - 1), 2, 2);
        }
    }

    /**
     * M7 part A: the dash pip beside the shield's label (design/player/systems, Evasive thrusters): an
     * empty cell filling over the cooldown, full when a dash is ready, flashing white as it gets there.
     */
    private void dashPip(SpriteBatch batch, double readiness, boolean ready, long tick, int x, int y) {
        if (ready && !dashWasReady) {
            dashFlashTick = tick;
        }
        dashWasReady = ready;
        int pipX = x + DASH_PIP_X;
        int pipY = y - DASH_PIP_DROP;
        kit.lcd(batch, pipX, pipY, DASH_PIP_WIDTH, DASH_PIP_HEIGHT);
        kit.flat(batch, PIP_EMPTY, pipX, pipY, DASH_PIP_WIDTH, DASH_PIP_HEIGHT);
        Color colour = flashing(tick, dashFlashTick) ? Color.WHITE : ready ? SHIELD : RING_COOLING;
        float width = ready ? DASH_PIP_WIDTH : (float) Math.floor(DASH_PIP_WIDTH * Math.clamp(readiness, 0, 1));
        kit.flat(batch, colour, pipX, pipY, width, DASH_PIP_HEIGHT);
    }

    private void power(SpriteBatch batch, double spare, double bonus, boolean overdrive, int x, int y) {
        kit.label(batch, "POWER", x, y);
        String percent = "+" + Math.round(bonus * 100) + "%";
        kit.textRight(batch, kit.body, percent, HudKit.LABEL, x, y, HudKit.INNER_WIDTH);
        int barY = y - 34;
        kit.segments(
                batch,
                POWER,
                POWER_EMPTY,
                x,
                barY,
                HudKit.INNER_WIDTH,
                BAR_HEIGHT,
                Math.clamp(spare / FULL_SPARE, 0, 1));
        if (overdrive) {
            kit.glow(batch, HudKit.AMBER, x, barY, HudKit.INNER_WIDTH, BAR_HEIGHT);
        }
    }

    private void weapons(
            SpriteBatch batch,
            Sortie sortie,
            Flight.Weapon[] weapons,
            boolean[] idle,
            double overdriveSeconds,
            int x,
            int y) {
        kit.label(batch, "WEAPONS", x, y);
        int top = y - 22;
        int height = weapons.length * ROW + ROW + 10;
        kit.lcd(batch, x, top - height, HudKit.INNER_WIDTH, height);
        // M8 part C: one white flash as the NO WATER rows clear at a waterline.
        kit.glow(batch, waterFlash > 0 ? Color.WHITE : HudKit.READOUT, x, top - height, HudKit.INNER_WIDTH, height);
        int pipsX = x + HudKit.INNER_WIDTH - 6 - LEVEL_PIPS * 10;
        for (int slot = 0; slot < weapons.length; slot++) {
            int rowTop = top - 5 - slot * ROW;
            Flight.Weapon weapon = weapons[slot];
            Color colour = weapon == null ? HudKit.LABEL : HudKit.READOUT;
            kit.text(batch, kit.small, SLOT_LETTERS[slot], HudKit.LABEL, x + 6, rowTop);
            String name = weapon == null ? "-" : hudName(weapon.name());
            if (weapon != null && idle[slot]) {
                // M5 part E: it cannot fire here: the reason instead of its pips (M8 part A: the name cut before it).
                kit.text(batch, kit.small, name, colour, x + 20, rowTop, NO_WATER_NAME_WIDTH);
                kit.textRight(batch, kit.small, NO_WATER, HudKit.LABEL, x, rowTop, HudKit.INNER_WIDTH - 6);
                continue;
            }
            kit.text(batch, kit.small, name, colour, x + 20, rowTop, pipsX - x - 24);
            int level = weapon == null ? 0 : weapon.level();
            for (int i = 0; i < LEVEL_PIPS; i++) {
                kit.fill(batch, i < level ? HudKit.AMBER : PIP_EMPTY, pipsX + i * 10, rowTop - 9, 8, 7);
            }
            int bay = weapon == null ? -1 : droneBay(sortie, slot);
            if (bay >= 0) {
                droneHits(batch, sortie.drones(), bay, pipsX, rowTop);
            }
        }
        int rowTop = top - 7 - weapons.length * ROW;
        boolean on = overdriveSeconds > 0;
        kit.text(batch, kit.small, "OVERDRIVE", on ? HudKit.AMBER : HudKit.LABEL, x + 6, rowTop);
        int lit = (int) Math.ceil(overdriveSeconds / overdriveLength * OVERDRIVE_PIPS);
        int barX = x + 86;
        for (int i = 0; i < OVERDRIVE_PIPS; i++) {
            kit.fill(batch, i < lit ? HudKit.AMBER : PIP_EMPTY, barX + i * 8, rowTop - 9, 6, 7);
        }
        if (on) {
            kit.textRight(
                    batch,
                    kit.small,
                    (int) Math.ceil(overdriveSeconds) + "s",
                    HudKit.AMBER,
                    x,
                    rowTop,
                    HudKit.INNER_WIDTH - 6);
        }
    }

    /** M7 part A: the drone bay's mount in weapon slot {@code slot} (front, rear, left, right); -1 for none. */
    private static int droneBay(Sortie sortie, int slot) {
        if (!sortie.drones().any()) {
            return -1;
        }
        for (int m = 0; m < sortie.armament().size(); m++) {
            if (sortie.armament().mount(m).slot().ordinal() == slot
                    && sortie.drones().bay(m)) {
                return m;
            }
        }
        return -1;
    }

    /**
     * M7 part A: a drone's hits left as a thin strip of five pips under its row's level pips, draining
     * as it absorbs hits; while its bay rebuilds it the strip refills dim (pending the user's review:
     * the level pips stay).
     */
    private void droneHits(SpriteBatch batch, Drones drones, int m, int pipsX, int rowTop) {
        boolean broken = drones.broken(m);
        int lit = droneHitPips(drones.hitsLeft(m), drones.spec(m).hits(), broken, drones.rebuilt(m));
        for (int i = 0; i < LEVEL_PIPS; i++) {
            Color colour = i >= lit ? PIP_EMPTY : broken ? RING_COOLING : SHIELD;
            kit.flat(batch, colour, pipsX + i * 10, rowTop - DRONE_STRIP_DROP, 8, DRONE_STRIP_HEIGHT);
        }
    }

    /** M7 part A: the drone strip's lit pips: its hits left of five, or while broken how far it is rebuilt. */
    static int droneHitPips(int hitsLeft, int hits, boolean broken, double rebuilt) {
        if (broken) {
            return (int) Math.floor(LEVEL_PIPS * Math.clamp(rebuilt, 0, 1));
        }
        return hits <= 0 ? 0 : (int) Math.ceil(LEVEL_PIPS * Math.clamp((double) hitsLeft / hits, 0, 1));
    }

    /**
     * Whether the armour gauge shows its red flash at {@code tick}: never above 30 %, then lit and
     * dark in turns, faster at 15 % (design/ui/hud, right panel), lit from the stage's first step.
     */
    static boolean armourFlash(LowArmour stage, long tick) {
        return switch (stage) {
            case NONE -> false;
            case LOW -> tick / LOW_ARMOUR_PHASE_TICKS % 2 == 0;
            case CRITICAL -> tick / CRITICAL_ARMOUR_PHASE_TICKS % 2 == 0;
        };
    }

    /**
     * The HUD's short weapon names: a pod is named by what it fires ("Micro-missile"); M8 part A: the Harpoon
     * Torpedoes by their harpoons ("HARPOONS"), which leaves room for {@code NO WATER} beside the name.
     */
    static String hudName(String name) {
        String shortName = name.endsWith(" Pod") ? name.substring(0, name.length() - 4) : name;
        if (shortName.endsWith(" Torpedoes")) {
            shortName = shortName.substring(0, shortName.indexOf(' ')) + "s";
        }
        return shortName.toUpperCase(Locale.ROOT);
    }

    private void gauge(
            SpriteBatch batch,
            String name,
            Color nameColour,
            double value,
            double max,
            Color full,
            Color empty,
            int x,
            int y,
            boolean lit,
            boolean alarm) {
        kit.label(batch, name, nameColour, x, y);
        kit.textRight(
                batch,
                kit.body,
                Integer.toString((int) Math.ceil(value)),
                alarm ? HudKit.ALERT : HudKit.LABEL,
                x,
                y,
                HudKit.INNER_WIDTH);
        kit.segments(batch, full, empty, x, y - 34, HudKit.INNER_WIDTH, BAR_HEIGHT, lit ? value / max : 0);
        if (alarm) {
            kit.fill(batch, ALARM, x, y - 34, HudKit.INNER_WIDTH, BAR_HEIGHT);
            kit.glow(batch, HudKit.ALERT, x - 4, y - 38, HudKit.INNER_WIDTH + 8, BAR_HEIGHT + 8);
        }
    }
}
