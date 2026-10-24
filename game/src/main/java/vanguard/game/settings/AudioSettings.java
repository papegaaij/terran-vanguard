package vanguard.game.settings;

/**
 * The Audio tab (design/ui/options): volumes 0..1 on top of the game's own mix. The master volume
 * scales the four others.
 *
 * @param radio the radio blips: squelch and typing
 * @param voice the spoken radio lines and briefing pages (design/audio/voice)
 */
public record AudioSettings(double master, double music, double effects, double radio, double voice) {
    /**
     * The defaults: the effects at 50 % and the radio blips at 90 %, the rest full (the user's playtest of 2026-10-24:
     * many explosions and much firing at once drowned the music and the radio); a saved settings file keeps its own.
     */
    public static AudioSettings defaults() {
        return new AudioSettings(1, 1, 0.5, 0.9, 1);
    }

    public AudioSettings withMaster(double volume) {
        return new AudioSettings(volume, music, effects, radio, voice);
    }

    public AudioSettings withMusic(double volume) {
        return new AudioSettings(master, volume, effects, radio, voice);
    }

    public AudioSettings withEffects(double volume) {
        return new AudioSettings(master, music, volume, radio, voice);
    }

    public AudioSettings withRadio(double volume) {
        return new AudioSettings(master, music, effects, volume, voice);
    }

    public AudioSettings withVoice(double volume) {
        return new AudioSettings(master, music, effects, radio, volume);
    }
}
