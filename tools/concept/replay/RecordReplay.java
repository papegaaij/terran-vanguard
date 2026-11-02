/*
 * Records the content autopilot's commands for a level start, so the desktop game can fly it with
 * --replay <file> (a debug option): a real-time capture of the level as the autopilot plays it, with the
 * game's sound and music. Step m10-f6r (round 78's full duel recording, design/enemies/bosses/honour-guard).
 *
 * Run with tools/concept/replay/record_replay.sh (Java's single-file launcher on the content test classpath):
 *   record_replay.sh <level> <difficulty> <out.rec> [--loadout ...] [--special ...] [--escort ...]
 * The level start is built exactly as the game builds it (TerranVanguard's test level, LevelScreen's sortie:
 * DebugFit.startAt + applyTo, campaign.launch(), Flight.of, SimSpecs, seed 2185), each step flown with
 * Autopilot.commands; it stops at the level's completion and refuses a run in which the ship is destroyed
 * (the game would open the mission failed screen). It prints the final state hash, which the game logs as
 * "replay: ... hash ..." when the recording runs out, so the two can be compared.
 */
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import vanguard.content.Autopilot;
import vanguard.content.Content;
import vanguard.content.ContentLoader;
import vanguard.content.Difficulty;
import vanguard.content.SimSpecs;
import vanguard.content.campaign.Campaign;
import vanguard.content.campaign.CampaignRoute;
import vanguard.content.campaign.CampaignRules;
import vanguard.content.campaign.Catalogue;
import vanguard.content.campaign.DebugFit;
import vanguard.content.campaign.Flight;
import vanguard.sim.InputRecording;
import vanguard.sim.SimEvents;
import vanguard.sim.Sortie;

public final class RecordReplay {
    /** LevelScreen.SEED: every run of a level gets the same waves. */
    private static final long SEED = 2185;

    private static final int MAX_STEPS = 60 * 60 * 30;

    public static void main(String[] args) throws Exception {
        int levelNumber = Integer.parseInt(args[0]);
        Difficulty difficulty = Difficulty.of(args[1]);
        Path out = Path.of(args[2]);
        DebugFit fit = null;
        String special = null;
        String escort = null;
        for (int i = 3; i < args.length; i += 2) {
            switch (args[i]) {
                case "--loadout" -> fit = DebugFit.parse(args[i + 1]);
                case "--special" -> special = args[i + 1];
                case "--escort" -> escort = args[i + 1];
                default -> throw new IllegalArgumentException("unknown option " + args[i]);
            }
        }
        if (special != null) {
            fit = (fit == null ? DebugFit.NONE : fit).withSpecial(special);
        }
        if (escort != null) {
            fit = (fit == null ? DebugFit.NONE : fit).withEscort(escort);
        }
        Content content = ContentLoader.fromClasspath();
        CampaignRules campaignRules = CampaignRules.of(content);
        Catalogue catalogue = Catalogue.of(content);
        Campaign campaign = DebugFit.startAt(campaignRules, difficulty, levelNumber);
        if (fit != null) {
            fit.applyTo(campaign, catalogue);
        }
        String levelKey = CampaignRoute.launch(content, campaign).orElseThrow();
        var rules = SimSpecs.rules(content, levelKey, difficulty);
        campaign.launch();
        Flight flight = Flight.of(content, catalogue, campaign);
        Sortie sortie = new Sortie(
                SEED, flight.loadout(), SimSpecs.level(content, levelKey, difficulty), rules, campaign.armour());
        var recorder = new InputRecording.Recorder(SEED);
        int steps = 0;
        while (!sortie.complete() && steps++ < MAX_STEPS) {
            if (!sortie.flying() || sortie.primaryFailed()) {
                throw new IllegalStateException(String.format(
                        "the ship is lost at t %.1f (tick %d): no replay without a retry", sortie.levelSeconds(),
                        sortie.tick()));
            }
            int commands = Autopilot.commands(sortie);
            recorder.record(commands);
            sortie.step(commands);
            SimEvents events = sortie.events();
            for (int i = 0; i < events.size(); i++) {
                SimEvents.Type type = events.type(i);
                if (type.name().startsWith("BOSS") || type.name().startsWith("ACE")
                        || type == SimEvents.Type.SHIP_DESTROYED) {
                    System.out.printf("t %.2f (level %.2f) %s%n", sortie.realSeconds(), sortie.levelSeconds(), type);
                }
            }
        }
        if (!sortie.complete()) {
            throw new IllegalStateException("not complete after " + steps + " steps");
        }
        try (Writer writer = Files.newBufferedWriter(out, StandardCharsets.US_ASCII)) {
            recorder.writeTo(writer);
        }
        System.out.printf(
                "recorded %s: %s %s, %d steps (%.1f s), kills %d, credits %d, armour %.0f, hash %s%n",
                out, levelKey, difficulty, sortie.tick(), sortie.realSeconds(), sortie.result().kills(),
                sortie.result().credits().total(), sortie.ship().defences().armour(),
                Long.toHexString(sortie.stateHash()));
    }
}
