package vanguard.content;

import java.util.Optional;

/**
 * A page of a briefing (design/ui/briefing): who speaks, with their portrait, what they say and
 * optionally the page's tactical map or mission image.
 *
 * @param speaker the speaker's short name, as on the radio ({@code Okafor}, {@code Varga})
 * @param expression the portrait's expression, neutral when not given
 * @param image the image's name, a file {@code ui/briefing/<image>.png} in the assets
 * @param channel M9 part A (design/ui/briefing, Intercepted transmissions): the channel the page comes over, {@code
 *     friendly} when not given; {@code enemy}: an intercepted page (Vorne's broadcast)
 * @param music M10 part B (design/campaign Act 6, Act intro and outro; design/audio/music, Act 6 music): what the page
 *     plays in place of the briefing music: {@code motif}, the act's A theme's motif stem alone (the briefing music
 *     fading out over 1 s as the page opens and back over 1 s on the next page); M11 part B: {@code choir}, the act's
 *     A theme's {@code choir} stem alone (Act 7's page 3); the briefing music when not given
 * @param clean M11 part B, step B2 (design/campaign Act 7, Act intro: page 3, the Choir "intercepted through the gate,
 *     clean", J3): an intercepted page without the steady interference (the Choir is close); only with {@code channel:
 *     enemy}; false if not given
 */
public record BriefingPage(
        String speaker,
        String line,
        Optional<Expression> expression,
        Optional<String> image,
        Optional<Channel> channel,
        Optional<PageMusic> music,
        Optional<Boolean> clean) {
    /** A page on the friendly channel (every page before Act 5). */
    public BriefingPage(String speaker, String line, Optional<Expression> expression, Optional<String> image) {
        this(speaker, line, expression, image, Optional.empty());
    }

    /** A page under the briefing music (every page before Act 6). */
    public BriefingPage(
            String speaker,
            String line,
            Optional<Expression> expression,
            Optional<String> image,
            Optional<Channel> channel) {
        this(speaker, line, expression, image, channel, Optional.empty(), Optional.empty());
    }

    /** M10 part B: what a page plays in place of the briefing music. */
    public enum PageMusic {
        /** The act's A theme's motif stem alone (Act 6's intro, page 3: Vorne's invitation). */
        MOTIF,
        /** M11 part B: the act's A theme's {@code choir} stem alone (Act 7's intro, page 3: the Choir, track 16). */
        CHOIR
    }

    public BriefingPage {
        Check.that(!speaker.isBlank(), "speaker: must not be empty");
        Check.that(!line.isBlank(), "line: must not be empty");
        Expression.checkSpeaker(speaker, expression);
        Check.that(
                image.map(name -> name.matches("[a-z0-9]+(-[a-z0-9]+)*")).orElse(true),
                "image: a lower-case kebab-case name");
        Check.that(
                !clean.orElse(false) || channel.orElse(Channel.FRIENDLY) == Channel.ENEMY,
                "clean: only on an intercepted page (channel: enemy)");
    }

    /** M11 part B: whether the intercepted page is drawn without its steady interference ({@code clean: true}). */
    public boolean cleanIntercept() {
        return clean.orElse(false);
    }

    /** M9 part A: the channel the page comes over, {@link Channel#FRIENDLY} when not given. */
    public Channel onChannel() {
        return channel.orElse(Channel.FRIENDLY);
    }

    /** The portrait's expression on this page. */
    public Expression portrait() {
        return expression.orElse(Expression.NEUTRAL);
    }
}
