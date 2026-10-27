package vanguard.content.campaign;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import vanguard.content.Difficulty;
import vanguard.content.SpecialsData;
import vanguard.sim.LevelResult;
import vanguard.sim.WingmanSpec;

/**
 * The campaign state (design/systems/saves, design/systems/retry, design/systems/economy): plain
 * Java, saved as a {@link SaveGame} between levels. Nothing changes while a level is flown, so the
 * state is the level-start snapshot a retry returns to: credits earned in a level are banked only
 * when it is won, and a failed attempt's earnings are lost.
 *
 * <p>Retries: a failure ends the campaign when no retry is left (hard: 3 per level); otherwise it
 * uses one at once and the level is retried, at once or after the hangar, with its level-start
 * armour but at least the armour floor (50 %). A restart and an abort to the hangar each use one of
 * the level's retries on hard too. A game over returns to the hangar before the level: the gear
 * of the last launch with the level's retries renewed, which the autosave written at the failure
 * then holds, so Continue starts the level over instead of resuming it with none left.
 *
 * <p>The escort slot (design/player/wingmen, M5 part A): Rook is hired when the campaign reaches
 * the hangar before Level 08 (or is loaded past it), with the free Autocannon at L1 and full armour.
 * He flies from Level 08 on while his armour is above 0 (grounded after an ejection until a repair);
 * his armour is kept as a won level ended it, a retry raises it to the armour floor like the
 * player's, and a failed attempt leaves it as he launched.
 *
 * <p>M8 part A (design/player/wingmen, Heavy drone "Bulwark" and Rook's loss and absence): from Level 22 the slot holds
 * Rook or a bought Bulwark, the player's choice; Rook is missing in the levels after the one he is shot down in (Level
 * 26) until the one he is back in (Level 30): then the slot holds the owned Bulwark, or a free loaner at full armour,
 * and the story flag {@value #ROOK_MISSING} is set from Level 26's win to Level 29's. An owned Bulwark's armour is kept
 * between levels like Rook's; a destroyed one is rebuilt (full armour) for the next level, a loaner always flies
 * whole and keeps nothing.
 */
public final class Campaign {
    /** What a failed attempt leads to. */
    public enum Failure {
        /** The mission failed screen: retry, back to the hangar or quit. */
        MISSION_FAILED,
        /** No retry left: the campaign ends (hard). */
        GAME_OVER
    }

    private final CampaignRules rules;
    private final Difficulty difficulty;
    private double playtime;
    private int nextLevel;
    private long score;
    private Gear gear;
    /** The gear the level was last launched with: what a game over returns to. */
    private Gear launched;

    private final List<String> unlocks;
    private Optional<Integer> retriesLeft;
    private final Map<Integer, String> grades;
    private final List<String> dataCores;
    private final List<String> storyFlags;
    private int kills;
    private int deaths;
    /** What each won level brought, by level number (the act summary). */
    private final Map<Integer, SaveGame.LevelStats> levelStats;
    /** A debug option: the level whose win ends its act early ({@code --act-end}); never saved. */
    private Optional<Integer> debugActEnd = Optional.empty();
    /**
     * A debug option ({@code --escort}): the Rook every flight takes whatever the level, or none
     * (empty: the gear's, by the rules). It applies to the flights only: the gear, and so every save,
     * never holds it.
     */
    private Optional<DebugEscort> debugEscort = Optional.empty();

    /**
     * The {@code --escort} debug option's Rook.
     *
     * @param gun his gun at its level; empty for {@code none}: he does not fly
     * @param side his side; empty for the gear's
     * @param bulwark M8 part A: {@code --escort bulwark}: the Bulwark flies instead, at full armour
     */
    private record DebugEscort(Optional<Fitted> gun, Optional<WingmanSpec.Side> side, boolean bulwark) {}

    /** M8 part A: the story flag that records Rook as missing, from Level 26's win to Level 29's. */
    public static final String ROOK_MISSING = "rook-missing";

    /** M8 part A: the story flag that records the hydro-kit's one-off hangar notice as shown. */
    public static final String HYDRO_KIT = "hydro-kit";

    /**
     * The escort as a flight takes it (design/player/wingmen): Rook with his gun, or (M8 part A) the Bulwark.
     *
     * @param escorter who flies
     * @param gun Rook's gun at its level; empty for the Bulwark
     * @param side the side of the slot
     * @param armour the escort's armour at the start of the attempt
     * @param loaner M8 part A: a Bulwark on loan while Rook is missing and none is owned (free, whole every level)
     */
    public record EscortFlight(
            Escort.Escorter escorter, Optional<Fitted> gun, WingmanSpec.Side side, double armour, boolean loaner) {
        public EscortFlight {
            if (gun.isPresent() != (escorter == Escort.Escorter.ROOK)
                    || loaner && escorter != Escort.Escorter.BULWARK) {
                throw new IllegalArgumentException(
                        "Rook flies with a gun, the Bulwark without; only a Bulwark is lent");
            }
        }

        /** Rook with his gun. */
        public static EscortFlight rook(Fitted gun, WingmanSpec.Side side, double armour) {
            return new EscortFlight(Escort.Escorter.ROOK, Optional.of(gun), side, armour, false);
        }

        /** The Bulwark heavy drone, owned or on loan. */
        public static EscortFlight bulwark(WingmanSpec.Side side, double armour, boolean loaner) {
            return new EscortFlight(Escort.Escorter.BULWARK, Optional.empty(), side, armour, loaner);
        }

        /** Whether Rook flies (the {@code requires: escort} radio lines are his). */
        public boolean rook() {
            return escorter == Escort.Escorter.ROOK;
        }
    }
    /** The replay this campaign flies, if it is one: a throwaway copy of a save. */
    private Optional<Replay> replay = Optional.empty();

    /**
     * The escort choice of a replay (the user's playtest of 2026-10-26, design/ui/mission-select): a replay of a level
     * Rook flew flies him unless the Bulwark is fitted in that replay's hangar, whatever the campaign's own choice
     * ({@link Escort#flies()}) is now. Never saved; the replay hangar opens with Rook ({@link #replayEscort(Escort.Escorter)}).
     */
    private Escort.Escorter replayEscort = Escort.Escorter.ROOK;

    /**
     * A replay of a level the campaign has flown (design/ui/mission-select): it flies with the save's
     * loadout and special charges at full armour, its retries are unlimited, and nothing of it is
     * kept but a better grade, which the mission select writes into {@code slot}.
     *
     * @param slot the save slot the replay was started from
     */
    public record Replay(SaveSlots.Slot slot, int level) {}

    private Campaign(CampaignRules rules, SaveGame save) {
        this.rules = rules;
        difficulty = save.difficulty();
        playtime = save.playtime();
        nextLevel = save.nextLevel();
        score = save.score();
        gear = gear(save);
        hireWhenDue();
        launched = gear;
        unlocks = new ArrayList<>(save.unlocks());
        retriesLeft = save.retriesLeft();
        grades = new TreeMap<>(save.grades());
        dataCores = new ArrayList<>(save.dataCores());
        storyFlags = new ArrayList<>(save.storyFlags());
        kills = save.stats().kills();
        deaths = save.stats().deaths();
        levelStats = new TreeMap<>(save.stats().levels());
    }

    /** The gear of a save: Rook's fitted gun into the escort slot, his other guns into the escort inventory. */
    private static Gear gear(SaveGame save) {
        SaveGame.EscortSlot slot = save.escort();
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(LoadoutSlot.class);
        loadout.putAll(save.loadout());
        Map<ItemKind, List<Fitted>> inventory = new EnumMap<>(ItemKind.class);
        inventory.putAll(save.inventory());
        List<Fitted> guns = new ArrayList<>();
        for (Fitted gun : slot.guns()) {
            if (slot.fitted().filter(gun.item()::equals).isPresent()) {
                loadout.put(LoadoutSlot.ESCORT, gun);
            } else {
                guns.add(gun);
            }
        }
        inventory.put(ItemKind.ESCORT, guns);
        Escort escort = new Escort(
                slot.hired(),
                side(slot.side()),
                slot.armour(),
                Escort.Escorter.valueOf(slot.flies().toUpperCase(java.util.Locale.ROOT)),
                new Escort.Bulwark(slot.bulwark().owned(), slot.bulwark().armour()));
        return new Gear(save.credits(), loadout, inventory, save.specials(), save.armour(), escort, save.bays());
    }

    private static WingmanSpec.Side side(String saved) {
        return WingmanSpec.Side.valueOf(saved.toUpperCase(java.util.Locale.ROOT));
    }

    private static String side(WingmanSpec.Side side) {
        return side.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** The save's escort slot of the gear. */
    private static SaveGame.EscortSlot escortSlot(Gear gear) {
        Optional<Fitted> fitted = Optional.ofNullable(gear.loadout().get(LoadoutSlot.ESCORT));
        List<Fitted> guns = new ArrayList<>();
        fitted.ifPresent(guns::add);
        guns.addAll(gear.inventory(ItemKind.ESCORT));
        Escort escort = gear.escort();
        return new SaveGame.EscortSlot(
                escort.hired(),
                side(escort.side()),
                fitted.map(Fitted::item),
                guns,
                escort.armour(),
                escort.flies().name().toLowerCase(java.util.Locale.ROOT),
                new SaveGame.BulwarkSlot(
                        escort.bulwark().owned(), escort.bulwark().armour()));
    }

    /** The gear without Rook's guns, for the save's loadout and inventory. */
    private static Map<LoadoutSlot, Fitted> shipLoadout(Gear gear) {
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(LoadoutSlot.class);
        loadout.putAll(gear.loadout());
        loadout.remove(LoadoutSlot.ESCORT);
        return loadout;
    }

    private static Map<ItemKind, List<Fitted>> shipInventory(Gear gear) {
        Map<ItemKind, List<Fitted>> inventory = new EnumMap<>(ItemKind.class);
        inventory.putAll(gear.inventory());
        inventory.remove(ItemKind.ESCORT);
        return inventory;
    }

    /**
     * Rook joins when the hangar opens with his level (or a later one) next: the free gun at L1
     * fitted, full armour, on the side the campaign has (a new campaign's: the data's).
     */
    private void hireWhenDue() {
        CampaignRules.EscortRules escort = rules.escort();
        if (gear.escort().hired() || nextLevel < escort.joins()) {
            return;
        }
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(LoadoutSlot.class);
        loadout.putAll(gear.loadout());
        loadout.put(LoadoutSlot.ESCORT, new Fitted(escort.starterGun(), 1));
        gear = new Gear(
                gear.credits(),
                loadout,
                gear.inventory(),
                gear.specials(),
                gear.armour(),
                new Escort(
                        true,
                        gear.escort().side(),
                        escort.maxArmour(),
                        gear.escort().flies(),
                        gear.escort().bulwark()),
                gear.bays());
    }

    /** A new campaign: Level 01 next, the starting credits, the starter loadout at full armour. */
    public static Campaign start(CampaignRules rules, Difficulty difficulty) {
        return new Campaign(
                rules,
                new SaveGame(
                        SaveFormat.VERSION,
                        Instant.EPOCH,
                        0,
                        difficulty,
                        1,
                        rules.startingCredits(),
                        0,
                        rules.starterLoadout(),
                        Map.of(),
                        List.of(),
                        Map.of(),
                        rules.starterArmour(),
                        new SaveGame.EscortSlot(
                                false,
                                side(rules.escort().side()),
                                Optional.empty(),
                                List.of(),
                                rules.escort().maxArmour(),
                                SaveFormat.ESCORT_FLIES,
                                new SaveGame.BulwarkSlot(false, rules.bulwark().maxArmour())),
                        rules.retries(difficulty),
                        Map.of(),
                        List.of(),
                        List.of(),
                        new SaveGame.Stats(0, 0)));
    }

    /**
     * A replay of a level the save has flown: a copy of the save's campaign that is never saved,
     * earns no credits and leaves the save's progress alone (main-agent choice, for the user to
     * confirm: design/ui/mission-select).
     */
    public static Campaign replay(CampaignRules rules, SaveSlots.Slot slot, SaveGame save, int level) {
        return replay(rules, slot, save, level, Escort.Escorter.ROOK);
    }

    /**
     * A replay as {@link #replay(CampaignRules, SaveSlots.Slot, SaveGame, int)}, with the escort its replay hangar chose
     * ({@link #replayEscort()}): Rook, or an owned Bulwark fitted there (from its unlock).
     */
    public static Campaign replay(
            CampaignRules rules, SaveSlots.Slot slot, SaveGame save, int level, Escort.Escorter escort) {
        if (!Missions.played(save, level)) {
            throw new IllegalArgumentException("level " + level + " has not been flown yet");
        }
        Campaign campaign = new Campaign(rules, save);
        campaign.replay = Optional.of(new Replay(slot, level));
        campaign.nextLevel = level;
        campaign.retriesLeft = Optional.empty();
        campaign.gear = campaign.gear.withArmour(campaign.maxArmour());
        campaign.launched = campaign.gear;
        campaign.replayEscort = escort;
        return campaign;
    }

    /** The replay this campaign flies; empty for the campaign itself. */
    public Optional<Replay> replay() {
        return replay;
    }

    /** The campaign of a save. */
    public static Campaign load(CampaignRules rules, SaveGame save) {
        return new Campaign(rules, save);
    }

    /** The state as a save written at {@code now}; a replay is never saved. */
    public SaveGame save(Instant now) {
        if (replay.isPresent()) {
            throw new IllegalStateException("a replay is never saved");
        }
        return new SaveGame(
                SaveFormat.VERSION,
                now,
                playtime,
                difficulty,
                nextLevel,
                gear.credits(),
                score,
                shipLoadout(gear),
                shipInventory(gear),
                unlocks,
                gear.specials(),
                gear.armour(),
                escortSlot(gear),
                gear.bays(),
                retriesLeft,
                grades,
                dataCores,
                storyFlags,
                new SaveGame.Stats(kills, deaths, levelStats));
    }

    /** The story flag that records a special's free charges as given (a save flag). */
    static String freeChargesFlag(String special) {
        return "free-charges:" + special;
    }

    /**
     * Free charges given at a special's unlock (design/player/specials: the first Airstrike charge).
     *
     * @param fitted whether the special was fitted into the empty special slot
     */
    public record FreeCharges(String special, int charges, boolean fitted) {}

    /**
     * Gives the free charges of every special that is unlocked by the next level and has not given
     * them yet, once per campaign (a story flag), up to its most charges; a special that gave some
     * is fitted when the special slot is empty. Called as the hangar opens, before its autosave.
     *
     * @return what was given, for the hangar's notice; empty when nothing was
     */
    public List<FreeCharges> giveFreeCharges(SpecialsData specials) {
        List<FreeCharges> given = new ArrayList<>();
        Map<String, Integer> charges = new HashMap<>(gear.specials());
        Map<LoadoutSlot, Fitted> loadout = new EnumMap<>(LoadoutSlot.class);
        loadout.putAll(gear.loadout());
        for (SpecialsData.Special special : specials.specials()) {
            String flag = freeChargesFlag(special.name());
            if (special.free() == 0 || special.unlock() > nextLevel || storyFlags.contains(flag)) {
                continue;
            }
            storyFlags.add(flag);
            int carried = charges.getOrDefault(special.name(), 0);
            int added = Math.min(special.free(), special.maxCharges().orElseThrow() - carried);
            if (added <= 0) {
                continue;
            }
            charges.put(special.name(), carried + added);
            boolean fit = !loadout.containsKey(LoadoutSlot.SPECIAL);
            if (fit) {
                loadout.put(LoadoutSlot.SPECIAL, new Fitted(special.name(), 1));
            }
            given.add(new FreeCharges(special.name(), added, fit));
        }
        if (!given.isEmpty()) {
            gear = new Gear(
                    gear.credits(), loadout, gear.inventory(), charges, gear.armour(), gear.escort(), gear.bays());
        }
        return given;
    }

    /** Adds time played; every campaign screen counts its frames. */
    public void play(double seconds) {
        playtime += seconds;
    }

    /** Whether a failed or abandoned attempt can be tried again: always, except on hard with no retry left. */
    public boolean canRetry() {
        return retriesLeft.map(left -> left > 0).orElse(true);
    }

    /** A sortie of the next level leaves the hangar: its gear is what a game over returns to. */
    public void launch() {
        launched = gear;
    }

    /**
     * The Stormhawk was destroyed: counts the death and says whether the campaign goes on. When it
     * does, the attempt's retry is used at once (hard), so it is gone even if the player quits from
     * the mission failed screen, and the next attempt starts with at least the armour floor. When
     * no retry is left (game over), the state returns to the last launch's gear with the level's
     * retries renewed: the hangar before the level, as the pre-launch save had it.
     */
    public Failure fail() {
        deaths++;
        if (!canRetry()) {
            gear = launched;
            retriesLeft = rules.retries(difficulty);
            return Failure.GAME_OVER;
        }
        useRetry();
        return Failure.MISSION_FAILED;
    }

    /**
     * An attempt is abandoned to try the level again from its start state, at once (the pause
     * menu's restart) or after the hangar (abort): uses a retry on hard and raises the armour to
     * the floor.
     *
     * @return the armour the next attempt starts with
     */
    public double retry() {
        if (!canRetry()) {
            throw new IllegalStateException("no retry left");
        }
        useRetry();
        return gear.armour();
    }

    private void useRetry() {
        retriesLeft = retriesLeft.map(left -> left - 1);
        gear = gear.withArmour(Math.max(gear.armour(), rules.armourFloor() * maxArmour()));
        Escort escort = gear.escort();
        if (escort.hired() && escort.armour() > 0) {
            // Rook too (design/player/wingmen): a grounded Rook stays home until a repair.
            gear = gear.withEscort(
                    escort.withArmour(Math.max(escort.armour(), rules.armourFloor() * escortMaxArmour())));
        }
        Escort.Bulwark bulwark = gear.escort().bulwark();
        if (bulwark.owned()) {
            // M8 part A: an owned Bulwark too (a loaner flies whole anyway).
            gear = gear.withEscort(gear.escort()
                    .withBulwark(
                            bulwark.withArmour(Math.max(bulwark.armour(), rules.armourFloor() * bulwarkMaxArmour()))));
        }
    }

    /**
     * The level was won: banks the credits it earned with the grade bonus, adds its score and kills,
     * keeps the armour that is left, records a better grade and moves on to the next level with
     * the retries renewed.
     *
     * @param armourLeft the ship's armour at the end of the level
     * @return whether the grade is a new best for the level
     */
    public boolean complete(LevelResult result, double armourLeft) {
        return complete(result, armourLeft, 0, 0);
    }

    /**
     * The level was won, as {@link #complete(LevelResult, double)}, and the fitted special's charges
     * change by what the level used and found. A failed attempt changes nothing, so a retry starts
     * with the level-start charges (design/player/specials).
     *
     * @param chargesUsed the fitted special's charges used in the winning attempt
     * @param chargesFound its charges found there (already limited to its most)
     */
    public boolean complete(LevelResult result, double armourLeft, int chargesUsed, int chargesFound) {
        return complete(result, armourLeft, chargesUsed, chargesFound, java.util.OptionalDouble.empty());
    }

    /**
     * The level was won, as {@link #complete(LevelResult, double, int, int)}, and Rook's armour is
     * kept as the level ended it (0 after an ejection: grounded until a repair).
     *
     * @param escortArmourLeft Rook's armour at the end of the level; empty when he did not fly
     */
    public boolean complete(
            LevelResult result,
            double armourLeft,
            int chargesUsed,
            int chargesFound,
            java.util.OptionalDouble escortArmourLeft) {
        if (!(armourLeft > 0)) {
            throw new IllegalArgumentException("a won level leaves armour, not " + armourLeft);
        }
        if (replay.isPresent()) {
            // A replay banks nothing: no credits, score, kills or charges, and no progress.
            return recordGrade(result.grade().letter());
        }
        gear = gear.withCredits(gear.credits() + result.credits().total() + result.gradeBonus())
                .withArmour(Math.min(armourLeft, maxArmour()));
        Fitted special = gear.loadout().get(LoadoutSlot.SPECIAL);
        // M7 part A: a cooldown special is owned, not charged: a level neither uses nor finds charges of it.
        if (special != null
                && !rules.cooldownSpecials().contains(special.item())
                && (chargesUsed != 0 || chargesFound != 0)) {
            Map<String, Integer> charges = new HashMap<>(gear.specials());
            charges.put(special.item(), Math.max(0, gear.charges(special.item()) - chargesUsed + chargesFound));
            gear = new Gear(
                    gear.credits(),
                    gear.loadout(),
                    gear.inventory(),
                    charges,
                    gear.armour(),
                    gear.escort(),
                    gear.bays());
        }
        Optional<EscortFlight> flown = escortFlight();
        if (escortArmourLeft.isPresent() && gear.escort().hired() && debugEscort.isEmpty() && flown.isPresent()) {
            // The --escort debug option's escort is not the gear's: its armour is not kept.
            if (flown.get().rook()) {
                gear = gear.withEscort(
                        gear.escort().withArmour(Math.clamp(escortArmourLeft.getAsDouble(), 0, escortMaxArmour())));
            } else if (!flown.get().loaner()) {
                // M8 part A: an owned Bulwark keeps its armour; a destroyed one is rebuilt for the next level.
                double left = Math.clamp(escortArmourLeft.getAsDouble(), 0, bulwarkMaxArmour());
                gear = gear.withEscort(gear.escort()
                        .withBulwark(gear.escort().bulwark().withArmour(left > 0 ? left : bulwarkMaxArmour())));
            }
        }
        // A data core collected in a won level: the core and its unlock are kept at once
        // (design/systems/economy, Data cores), even before the item exists.
        for (LevelResult.DataCore core : result.dataCores()) {
            if (!dataCores.contains(core.name())) {
                dataCores.add(core.name());
            }
            if (!unlocks.contains(core.unlocks())) {
                unlocks.add(core.unlocks());
            }
        }
        score += result.score();
        kills += result.kills();
        levelStats.put(
                nextLevel, new SaveGame.LevelStats(result.credits().total() + result.gradeBonus(), result.kills()));
        boolean newBest = recordGrade(result.grade().letter());
        nextLevel++;
        // M8 part A: Rook is shot down at the end of his last level and back for the level he returns in.
        if (nextLevel == rules.escort().lostAfter() + 1 && !storyFlags.contains(ROOK_MISSING)) {
            storyFlags.add(ROOK_MISSING);
        } else if (nextLevel == rules.escort().back()) {
            storyFlags.remove(ROOK_MISSING);
        }
        retriesLeft = rules.retries(difficulty);
        hireWhenDue();
        launched = gear;
        return newBest;
    }

    /** Records the next level's grade when it beats the best; returns whether it did. */
    private boolean recordGrade(String grade) {
        String best = grades.get(nextLevel);
        boolean newBest =
                best == null || rules.grades().indexOf(grade) < rules.grades().indexOf(best);
        if (newBest) {
            grades.put(nextLevel, grade);
        }
        return newBest;
    }

    /** The grades from best to worst. */
    public List<String> gradeOrder() {
        return rules.grades();
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    /** The level the campaign goes on with, 1–50. */
    public int nextLevel() {
        return nextLevel;
    }

    public int credits() {
        return gear.credits();
    }

    public long score() {
        return score;
    }

    /** The armour the next level starts with. */
    public double armour() {
        return gear.armour();
    }

    /** The fitted plating's maximum armour. */
    public double maxArmour() {
        return rules.maxArmour(gear.loadout().get(LoadoutSlot.ARMOUR).item());
    }

    /** What the hangar changes: credits, loadout, inventory, special charges and armour. */
    public Gear gear() {
        return gear;
    }

    /** Replaces the hangar's state: a transaction, or its undo. */
    void gear(Gear changed) {
        gear = changed;
    }

    /** Rook's full armour. */
    public double escortMaxArmour() {
        return rules.escort().maxArmour();
    }

    /**
     * Whether Rook is hired and grounded: his armour is 0 after an ejection, he stays home until a repair; M8 part A:
     * only while he is the slot's choice and not missing.
     */
    public boolean escortGrounded() {
        return escortGrounded(nextLevel);
    }

    /** Whether Rook is grounded for Level {@code level} (the replay hangar's mission), as {@link #escortGrounded()}. */
    public boolean escortGrounded(int level) {
        return rookChosen(level) && rookArmour(level) <= 0;
    }

    /**
     * Whether the launch warns about Rook's armour: he is hired and below the warning share (grounded too); M8 part A:
     * only while he is the slot's choice and not missing.
     */
    public boolean escortArmourLow() {
        return escortArmourLow(nextLevel);
    }

    /** Whether Rook's armour is low for Level {@code level} (the replay hangar's mission), as {@link #escortArmourLow()}. */
    public boolean escortArmourLow(int level) {
        return rookChosen(level) && rookArmour(level) < rules.escort().launchWarning() * escortMaxArmour();
    }

    /**
     * Rook's armour as a flight of Level {@code level} takes it: his campaign armour; a replay while he is lost (the
     * story flag {@value #ROOK_MISSING}, Levels 27–29) flies him whole, as neither the replay nor the campaign can
     * repair him then (the user's playtest of 2026-10-26).
     */
    public double rookArmour(int level) {
        return replays(level) && storyFlags.contains(ROOK_MISSING)
                ? escortMaxArmour()
                : gear.escort().armour();
    }

    /**
     * M8 part A: whether the slot is Rook's at Level {@code level}: he is hired, the level is from his joining level on
     * (an Act 1 replay has no escort, so no warning: the user's playtest of 2026-10-26), he is not missing, and no owned
     * Bulwark is chosen instead.
     */
    private boolean rookChosen(int level) {
        return gear.escort().hired() && escortJoined(level) && !rookMissing(level) && !bulwarkChosen(level);
    }

    /** M8 part A: whether an owned Bulwark is the slot's choice at {@code level} (it is for sale by then). */
    private boolean bulwarkChosen(int level) {
        return escortChoice(level) == Escort.Escorter.BULWARK
                && gear.escort().bulwark().owned()
                && level >= rules.bulwark().unlock();
    }

    /**
     * Who the player chose for the escort slot of Level {@code level}: the campaign's own choice for its next level,
     * the replay's ({@link #replayEscort()}) for a replayed one.
     */
    public Escort.Escorter escortChoice(int level) {
        return replays(level) ? replayEscort : gear.escort().flies();
    }

    /**
     * Whether Level {@code level} is a replay's: this campaign is a replay, or the level is an earlier one than the
     * next (the replay hangar's mission: only a level already flown can be replayed).
     */
    public boolean replays(int level) {
        return replay.isPresent() || level < nextLevel;
    }

    /** The escort choice of the replay hangar and its flight (Rook unless the Bulwark is fitted there); not saved. */
    public Escort.Escorter replayEscort() {
        return replayEscort;
    }

    /** Sets the replay hangar's escort choice (Rook as it opens; the Bulwark when fitted there). */
    public void replayEscort(Escort.Escorter escort) {
        replayEscort = escort;
    }

    /** Whether Level {@code level} is from Rook's joining level on: an escort can fly it once he is hired. */
    public boolean escortJoined(int level) {
        return level >= rules.escort().joins();
    }

    /** M8 part A: whether Rook is missing in the next level (design/player/wingmen, Rook's loss and absence). */
    public boolean rookMissing() {
        return rookMissing(nextLevel);
    }

    /** M8 part A: whether Rook is missing in Level {@code level} (Levels 27–29: lost at Level 26's end). */
    public boolean rookMissing(int level) {
        return rules.escort().missing(level);
    }

    /** M8 part A: the Bulwark's rules: its full armour, price, unlock and repair cost. */
    public CampaignRules.BulwarkRules bulwarkRules() {
        return rules.bulwark();
    }

    /** M8 part A: the Bulwark's full armour. */
    public double bulwarkMaxArmour() {
        return rules.bulwark().maxArmour();
    }

    /**
     * M8 part A: the hydro-kit's one-off hangar notice (design/player/systems, Hydro-kit): true once, at the first visit
     * whose next level is the kit's or a later one (its story flag {@value #HYDRO_KIT} records it as shown). Called as
     * the hangar opens, beside {@link #giveFreeCharges}.
     */
    public boolean hydroKitNotice() {
        if (replay.isPresent() || nextLevel < rules.hydroKit() || storyFlags.contains(HYDRO_KIT)) {
            return false;
        }
        storyFlags.add(HYDRO_KIT);
        return true;
    }

    /**
     * Whether an escort flies the next level ({@link #escortFlight()}): Rook hired, not grounded, and the level is his
     * joining level or later (not an Act 1 replay), or (M8 part A) the Bulwark; the {@code --escort} debug option
     * decides instead when given.
     */
    public boolean escortFlies() {
        return escortFlight().isPresent();
    }

    /** M8 part A: whether Rook himself flies the next level (the {@code requires: escort} radio lines are his). */
    public boolean rookFlies() {
        return escortFlight().filter(EscortFlight::rook).isPresent();
    }

    /** The escort as the next flight takes it, see {@link #escortFlight(int)}. */
    public Optional<EscortFlight> escortFlight() {
        return escortFlight(nextLevel);
    }

    /**
     * The escort as a flight of Level {@code level} takes it: Rook, the gear's, with his fitted gun, side and armour,
     * when he flies by the rules; the {@code --escort} debug option's instead when given (his gun, the given side or
     * the gear's, full armour, whatever the level; none for {@code --escort none}; M8 part A: the Bulwark at full
     * armour for {@code --escort bulwark}).
     *
     * <p>M8 part A: from the Bulwark's unlock an owned Bulwark flies when the player chose it; while Rook is missing
     * (Levels 27–29) the slot holds the owned Bulwark, or a free loaner at full armour when none is owned. A replay
     * (the user's playtest of 2026-10-26) takes the replay hangar's choice ({@link #escortChoice(int)}), Rook unless the
     * Bulwark is fitted there, and flies Rook whole while he is lost ({@link #rookArmour(int)}).
     */
    public Optional<EscortFlight> escortFlight(int level) {
        Escort escort = gear.escort();
        if (debugEscort.isPresent()) {
            DebugEscort debug = debugEscort.get();
            WingmanSpec.Side side = debug.side().orElse(escort.side());
            if (debug.bulwark()) {
                return Optional.of(EscortFlight.bulwark(side, bulwarkMaxArmour(), false));
            }
            return debug.gun().map(gun -> EscortFlight.rook(gun, side, escortMaxArmour()));
        }
        if (!escort.hired() || level < rules.escort().joins()) {
            return Optional.empty();
        }
        Escort.Bulwark bulwark = escort.bulwark();
        double bulwarkArmour = Math.min(bulwark.armour(), bulwarkMaxArmour());
        if (rookMissing(level)) {
            return Optional.of(
                    bulwark.owned()
                            ? EscortFlight.bulwark(escort.side(), bulwarkArmour, false)
                            : EscortFlight.bulwark(escort.side(), bulwarkMaxArmour(), true));
        }
        if (bulwarkChosen(level)) {
            return Optional.of(EscortFlight.bulwark(escort.side(), bulwarkArmour, false));
        }
        Fitted gun = gear.loadout().get(LoadoutSlot.ESCORT);
        double armour = rookArmour(level);
        if (!(armour > 0) || gun == null) {
            return Optional.empty();
        }
        return Optional.of(EscortFlight.rook(gun, escort.side(), Math.min(armour, escortMaxArmour())));
    }

    /**
     * A debug option for testing ({@code --escort}): Rook with {@code gun} at its level and full
     * armour, on the given side or the gear's, flies every level whatever its number; or, with
     * {@code gun} empty ({@code --escort none}), he does not fly. It applies to the flights only: the
     * gear is left alone (he is not hired by it, his guns and armour are the gear's as before), so no
     * save holds it.
     */
    public void debugEscort(Optional<Fitted> gun, Optional<WingmanSpec.Side> side) {
        debugEscort = Optional.of(new DebugEscort(gun, side, false));
    }

    /**
     * M8 part A: the {@code --escort bulwark} debug option: the Bulwark at full armour, on the given side or the gear's,
     * flies every level whatever its number; for the flights only, as {@link #debugEscort(Optional, Optional)}.
     */
    public void debugBulwark(Optional<WingmanSpec.Side> side) {
        debugEscort = Optional.of(new DebugEscort(Optional.empty(), side, true));
    }

    /** The story state, saved as the save's story flags (among them the free charges given). */
    public List<String> storyFlags() {
        return List.copyOf(storyFlags);
    }

    /**
     * M7 part B (user decision L.6 of round 36; the flag mechanism a default pending the user's review in round 38):
     * the story flag that records act {@code act}'s intro (its title card and intro pages) as shown.
     */
    public static String actIntroFlag(int act) {
        return "act-intro:" + act;
    }

    /** M7 part B: whether act {@code act}'s intro has been shown in this campaign (its story flag). */
    public boolean actIntroShown(int act) {
        return storyFlags.contains(actIntroFlag(act));
    }

    /** M7 part B: records act {@code act}'s intro as shown (a story flag, saved with the next autosave). */
    public void markActIntroShown(int act) {
        String flag = actIntroFlag(act);
        if (!storyFlags.contains(flag)) {
            storyFlags.add(flag);
        }
    }

    /** The data cores found, by name, in the order they were found. */
    public List<String> dataCores() {
        return List.copyOf(dataCores);
    }

    /** Shop items unlocked ahead of their normal unlock (data cores, story), by id. */
    public List<String> unlocks() {
        return List.copyOf(unlocks);
    }

    /** Retries left in the current level; empty where they are unlimited. */
    public Optional<Integer> retriesLeft() {
        return retriesLeft;
    }

    /** The retries a level starts with; empty where they are unlimited. */
    public Optional<Integer> retriesPerLevel() {
        return rules.retries(difficulty);
    }

    public Map<LoadoutSlot, Fitted> loadout() {
        return gear.loadout();
    }

    /** The best grade of a completed level. */
    public Optional<String> grade(int level) {
        return Optional.ofNullable(grades.get(level));
    }

    public double playtime() {
        return playtime;
    }

    public int deaths() {
        return deaths;
    }

    public int kills() {
        return kills;
    }

    /** What a won level brought the campaign, if it was recorded (saves of format version 1 have none). */
    public Optional<SaveGame.LevelStats> levelStats(int level) {
        return Optional.ofNullable(levelStats.get(level));
    }

    /**
     * A debug option for testing ({@code --act-end}): winning the next level ends its act as the
     * act's last level would, with the act summary and the outro. It is not saved.
     */
    public void debugActEnd() {
        debugActEnd = Optional.of(nextLevel);
    }

    /** The level whose win ends its act early, see {@link #debugActEnd()}. */
    Optional<Integer> debugActEndLevel() {
        return debugActEnd;
    }
}
