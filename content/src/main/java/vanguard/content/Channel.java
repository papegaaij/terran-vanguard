package vanguard.content;

/**
 * M9 part A (design/ui/briefing, Intercepted transmissions; design/ui/hud, Act 5 additions; design/audio/voice, Act 5
 * voices): the channel a radio line or a briefing page comes over. {@code friendly} (the default) is the CDF's teal;
 * {@code enemy} is the intercepted Ascendancy feed (Vorne, Harker): the amber plate, the {@code INTERCEPT} tag or the
 * {@code INTERCEPTED · ALL CHANNELS} header, the steady interference, never urgent.
 */
public enum Channel {
    FRIENDLY,
    ENEMY
}
