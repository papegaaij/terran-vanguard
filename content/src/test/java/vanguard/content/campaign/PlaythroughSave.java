package vanguard.content.campaign;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import vanguard.content.Difficulty;

/**
 * M7 part I (the user's I.3 of round 44): a real save for the user's playthrough, since a debug start writes none.
 * {@code ./gradlew :content:playthroughSave -Plevel=15 -Pdifficulty=medium -Pout=<settings dir>} flies the {@link
 * ActPlaythroughTest} campaign on the balance plan (the plan's purchases, the repairs, Rook hired from Level 08) up to
 * the level before {@code level} and writes it, as the autosave after that level's debrief, into {@code
 * <out>/saves/}: the autosave (Continue) and manual slot 1 (a spare copy to start over from). The game finds it with
 * {@code --settings <out>/settings.properties}; Continue then opens the next level's briefing (an act's intro first
 * when the level opens an act) and its hangar, and the mission select replays the levels won.
 */
public final class PlaythroughSave {
    private PlaythroughSave() {}

    public static void main(String[] args) throws IOException {
        int level = 15;
        Difficulty difficulty = Difficulty.MEDIUM;
        Path out = null;
        for (int i = 0; i + 1 < args.length; i += 2) {
            switch (args[i]) {
                case "--level" -> level = Integer.parseInt(args[i + 1]);
                case "--difficulty" -> difficulty = Difficulty.valueOf(args[i + 1].toUpperCase(Locale.ROOT));
                case "--out" -> out = Path.of(args[i + 1]);
                default -> throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }
        if (out == null || args.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "usage: --out <settings dir> [--level <n>, default 15] [--difficulty easy|medium|hard]");
        }
        if (level < 2 || Playthrough.CONTENT.levelKey(level).isEmpty()) {
            throw new IllegalArgumentException("Level " + level + " is not a built level after Level 01");
        }
        Playthrough playthrough = new Playthrough(difficulty);
        while (playthrough.campaign().nextLevel() < level) {
            Playthrough.Visit visit = playthrough.fly();
            playthrough.autosave();
            System.out.printf(
                    "L%02d won (%d attempt%s, grade %s, %d credits earned), %d credits%n",
                    visit.level(),
                    visit.attempts(),
                    visit.attempts() == 1 ? "" : "s",
                    visit.grade(),
                    visit.earned(),
                    playthrough.campaign().credits());
        }
        if (!playthrough.problems().isEmpty()) {
            System.out.println("the balance plan at the visits: " + playthrough.problems());
        }
        Campaign campaign = playthrough.campaign();
        SaveSlots slots = new SaveSlots(out.toAbsolutePath().resolve("saves"));
        Instant now = Instant.now();
        // The spare copy a second older, so that Continue picks the autosave.
        slots.write(new SaveSlots.Slot(1), campaign.save(now.minusSeconds(1)));
        slots.write(SaveSlots.Slot.AUTOSAVE, campaign.save(now));
        System.out.printf(
                "Saved the %s campaign before Level %02d (%d credits, Rook %s) into the autosave and slot 1 of %s%n",
                difficulty.name().toLowerCase(Locale.ROOT),
                level,
                campaign.credits(),
                campaign.gear().escort().hired() ? "hired" : "not hired",
                slots.directory());
        System.out.printf(
                "Start the game with --settings %s%n", out.toAbsolutePath().resolve("settings.properties"));
    }
}
