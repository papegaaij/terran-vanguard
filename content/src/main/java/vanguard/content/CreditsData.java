package vanguard.content;

/**
 * M11 part A, step A2b: design/ui/credits/data.yaml, the credits roll's music (design/ui/credits, After the final level):
 * track 28, a file {@code music/<music>.ogg} played once as the roll starts and again after {@code restart} s of silence
 * whenever the roll outlasts it; the track before it (the ending's) fades out over {@code fade} s as the roll starts.
 * The main menu's Credits item plays it the same way.
 */
public record CreditsData(String music, double restart, double fade) {
    public CreditsData {
        Check.that(music.matches("[a-z0-9]+(-[a-z0-9]+)*"), "music: a lower-case kebab-case name, was '" + music + "'");
        Check.notNegative("restart", restart);
        Check.notNegative("fade", fade);
    }
}
