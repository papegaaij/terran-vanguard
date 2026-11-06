package vanguard.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import java.util.Optional;

/**
 * design/campaign/&lt;act&gt;/data.yaml: an act's levels, its intro (the title card and the act
 * briefing that open the act) and its outro after the act's last level (design/campaign, act
 * documents: Act intro and outro).
 *
 * @param levels the act's first and last level, by their global numbers
 * @param outro the act-end outro, if the act has one yet
 * @param nextAct M7 part H (the user's round 43 answer H.7d): the act after this one as the release's end and the
 *     hangar before its first level name it while it is not built yet ({@code ACT IV - DEEP WATER. EUROPA. COMING
 *     SOON.}); given by the release's last act
 * @param ending M11 part A, step A2b (design/campaign Act 7, Act intro and ending; Q8 a): the campaign's ending pages,
 *     shown in the act outro's place after the last act's last debrief (the campaign end); only the campaign's last
 *     act gives them, with no outro and no {@code next_act}
 */
public record ActData(
        Levels levels,
        TitleCard titleCard,
        List<BriefingPage> briefing,
        Optional<Outro> outro,
        Optional<NextAct> nextAct,
        Optional<Outro> ending) {
    public ActData {
        Check.notEmpty("briefing", briefing);
        Check.that(
                ending.isEmpty() || outro.isEmpty() && nextAct.isEmpty(),
                "ending: the campaign's last act ends on its ending pages, with no outro and no next_act");
    }

    /**
     * M11 part A, step A2b: the pages after the act's last debrief: its outro, else (the campaign's last act) its ending
     * pages; none for an act without either.
     */
    public Optional<Outro> closing() {
        return outro.or(() -> ending);
    }

    /** The first and last level of the act, written {@code [first, last]}. */
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public record Levels(int first, int last) {
        public Levels {
            Check.that(
                    first >= 1 && first <= last,
                    "levels must be written [first, last], was [" + first + ", " + last + "]");
        }

        public boolean contains(int level) {
            return level >= first && level <= last;
        }
    }

    /**
     * The act title card (design/ui/briefing, act-title-r08-a).
     *
     * @param act the act's number as the card shows it, {@code ACT I}
     * @param name the act's title, {@code FIRST CONTACT}
     * @param line the settings and the date under the title
     */
    public record TitleCard(String act, String name, String line) {}

    /**
     * M7 part H: the next act, not built yet, as the release's end names it.
     *
     * @param act its number as its title card will show it, {@code ACT IV}
     * @param name its title, {@code DEEP WATER}
     * @param setting where it plays, {@code EUROPA}
     */
    public record NextAct(String act, String name, String setting) {
        public NextAct {
            Check.that(!act.isBlank() && !name.isBlank() && !setting.isBlank(), "next_act: act, name and setting");
        }

        /** The release end's line: {@code ACT IV - DEEP WATER. EUROPA. COMING SOON.} */
        public String comingSoon() {
            return act + " - " + name + ". " + setting + ". COMING SOON.";
        }

        /** The hangar's line before its first level: {@code ACT IV - DEEP WATER: COMING SOON}. */
        public String hangarLine() {
            return act + " - " + name + ": COMING SOON";
        }
    }

    /**
     * The act-end outro (design/campaign, Act intro and outro): briefing pages shown after the
     * debrief of the act's last level, before the hangar of the next act.
     *
     * @param music the track under the first page, a file {@code music/<music>.ogg} played once
     *     ({@code act-complete}, track 24; M11 part A: the ending's {@code ending}, track 27, on under the end card and
     *     the name entry)
     * @param pages the pages, as a briefing's
     */
    public record Outro(Optional<String> music, List<BriefingPage> pages) {
        public Outro {
            Check.notEmpty("pages", pages);
            Check.that(
                    music.map(name -> name.matches("[a-z0-9]+(-[a-z0-9]+)*")).orElse(true),
                    "music: a lower-case kebab-case name");
        }
    }
}
