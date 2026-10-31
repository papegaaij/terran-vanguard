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
 *     fading out over 1 s as the page opens and back over 1 s on the next page); the briefing music when not given
 */
public record BriefingPage(
        String speaker,
        String line,
        Optional<Expression> expression,
        Optional<String> image,
        Optional<Channel> channel,
        Optional<PageMusic> music) {
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
        this(speaker, line, expression, image, channel, Optional.empty());
    }

    /** M10 part B: what a page plays in place of the briefing music. */
    public enum PageMusic {
        /** The act's A theme's motif stem alone (Act 6's intro, page 3: Vorne's invitation). */
        MOTIF
    }

    public BriefingPage {
        Check.that(!speaker.isBlank(), "speaker: must not be empty");
        Check.that(!line.isBlank(), "line: must not be empty");
        Expression.checkSpeaker(speaker, expression);
        Check.that(
                image.map(name -> name.matches("[a-z0-9]+(-[a-z0-9]+)*")).orElse(true),
                "image: a lower-case kebab-case name");
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
