package vanguard.game.audio;

import static vanguard.game.audio.FlightSounds.EXPLOSIONS;
import static vanguard.game.audio.FlightSounds.PICKUPS;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vanguard.sim.EnemySpec;
import vanguard.sim.SimEvents;
import vanguard.sim.StoryPickups;

/**
 * M11 part C, step C4 (design/campaign Level 45, Music &amp; ambience; design/audio/sfx, Act 7 sounds; round 84's picks
 * levelled in their files): the hive reef's sounds for {@link FlightSounds}:
 *
 * <ul>
 *   <li>a spawning organ's seed pod swelling before a Whirl Seed cluster ({@link Sfx#ORGAN_SWELL}, S.17 a, on {@code
 *       ORGAN_SWELL}) and the cluster's release ({@link Sfx#ORGAN_RELEASE}, S.18 a, on {@code ORGAN_RELEASE}); its
 *       Skitter pair keeps the Hive Node's iris and spawn ({@link UnitCues});
 *   <li>an organ torn open over its rung ({@link Sfx#ORGAN_DEATH}, S.19 b, {@link #destroyed});
 *   <li>a spawning pit's 2 s tell ({@link Sfx#PIT_TELL}, S.45 a, on {@code PIT_TELL}) and its opening as the wave rises
 *       ({@link Sfx#PIT_OPEN}, S.46 b, on {@code PIT_OPEN}); a held pit and its closing are silent;
 *   <li>a story pickup flown through by its own sound ({@link #TAKEN}: the call organ's {@link Sfx#CALL_ORGAN_TAKEN},
 *       S.47 b, on {@code STORY_COLLECTED}); a story without one (Level 34's Helix core) stays as it was, silent here.
 * </ul>
 */
final class ReefCues extends SoundFamily {
    private static final Set<SimEvents.Type> TYPES = EnumSet.of(
            SimEvents.Type.ORGAN_SWELL,
            SimEvents.Type.ORGAN_RELEASE,
            SimEvents.Type.PIT_TELL,
            SimEvents.Type.PIT_OPEN,
            SimEvents.Type.STORY_COLLECTED);

    /** The cues are levelled in their files and play as they are. */
    static final float CUE_LEVEL = 1f;

    static final String ORGAN = "spawning-organ";
    /** Per story pickup ({@code pickups: story}) the sound of it taken; a story not named here plays none. */
    static final Map<String, Sfx> TAKEN = Map.of(
            "call-organ",
            Sfx.CALL_ORGAN_TAKEN,
            // M11 part D, step D4: Level 46's warm hull's core (round 84 S.51 a; GraveyardCues).
            "vrell-core",
            Sfx.WARM_HULL_TAKEN);

    private boolean[] organs;

    ReefCues(SoundMix mix) {
        super(mix);
    }

    @Override
    Set<SimEvents.Type> types() {
        return TYPES;
    }

    private void noted() {
        if (organs == null && mix.watched != null) {
            List<EnemySpec> kinds = mix.watched.enemyKinds();
            organs = new boolean[kinds.size()];
            for (int k = 0; k < kinds.size(); k++) {
                organs[k] = kinds.get(k).slug().equals(ORGAN);
            }
        }
    }

    @Override
    void play(SimEvents events, int i, float pan) {
        switch (events.type(i)) {
            case ORGAN_SWELL -> bank.play(Sfx.ORGAN_SWELL, CUE_LEVEL, pitch(0.04), pan);
            case ORGAN_RELEASE -> bank.play(Sfx.ORGAN_RELEASE, CUE_LEVEL, pitch(0.04), pan);
            case PIT_TELL -> bank.play(Sfx.PIT_TELL, CUE_LEVEL, pitch(0.03), pan);
            case PIT_OPEN -> bank.play(Sfx.PIT_OPEN, CUE_LEVEL, pitch(0.04), pan);
            case STORY_COLLECTED -> {
                Sfx taken = taken(
                        mix.watched == null
                                ? null
                                : mix.watched
                                        .storyPickup()
                                        .map(StoryPickups::story)
                                        .orElse(null));
                if (taken != null) {
                    bank.play(taken, PICKUPS, 1, 0);
                }
            }
            default -> {}
        }
    }

    /**
     * Whether {@code sortie}'s story pickup has its own sound taken (played here on {@code STORY_COLLECTED}), in place of
     * the Helix core's hum over the large salvage ({@link PlayerCues}); false without a sortie or a story.
     */
    static boolean ownSound(vanguard.sim.Sortie sortie) {
        return sortie != null
                && taken(sortie.storyPickup().map(StoryPickups::story).orElse(null)) != null;
    }

    /** The sound of {@code story} taken, null for none (or no story). */
    static Sfx taken(StoryPickups.Story story) {
        return story == null ? null : TAKEN.get(story.name());
    }

    /** A unit of {@code kind} was destroyed (UnitCues, over its rung): an organ's tearing. */
    void destroyed(int kind, float pan) {
        noted();
        if (organs != null && kind >= 0 && kind < organs.length && organs[kind]) {
            bank.play(Sfx.ORGAN_DEATH, EXPLOSIONS, pitch(0.04), pan);
        }
    }
}
