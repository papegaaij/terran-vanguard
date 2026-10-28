package vanguard.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import vanguard.content.EnemyMovementData.AirLink;
import vanguard.content.EnemyMovementData.Aura;
import vanguard.content.EnemyMovementData.Homing;
import vanguard.content.EnemyMovementData.Link;
import vanguard.content.EnemyMovementData.Pores;
import vanguard.content.EnemyMovementData.Pounce;
import vanguard.content.EnemyMovementData.Slow;
import vanguard.content.EnemyMovementData.Sweep;

/**
 * An enemy's attack keys in its stat block ({@link EnemyData}): its attacks and their triggers, spawns, mines and
 * mortars, its drops, weak points, formations, chains, parts and armour.
 *
 * <p>It only holds these types: {@link EnemyData} implements it, so callers keep naming them {@code EnemyData.X}
 * (Java inherits member types from superinterfaces, JLS 8.5) while the record's file stays small. New code may name
 * the interface directly; an import must (the canonical name: {@code import vanguard.content.EnemyAttackData.X}).
 */
public sealed interface EnemyAttackData permits EnemyData {
    /** The movement patterns it uses, with their parameters (design/enemies/README.md, Movement pattern vocabulary). */
    /**
     * The armour: a text ({@code none}, {@code hardened}, ...), or a mapping with {@code front_arc},
     * the degrees each side of the facing from which direct shots glance off (dropped bombs, lobbed
     * shells and the specials ignore it; a walker's, Level 04). M5 part C: {@code hardened} flies for
     * an enemy (design/enemies/ground/hive-node): only anti-ground deliveries, the Airstrike and the
     * Smart Bomb damage it. M9 part C (design/enemies/air/gilded-gunship): with a {@code factor} it is facing armour on a
     * unit that is not a walker: a direct hit arriving within {@code front_arc} of its facing does {@code factor} of its
     * damage instead of glancing off (after its shield), a {@code rear_plate} giving its engines' arc and multiplier.
     */
    record Armour(Optional<String> text, Optional<Double> frontArc, Optional<Double> factor) {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Armour {
            frontArc.ifPresent(arc -> Check.notNegative("front_arc", arc));
            factor.ifPresent(f -> Check.positive("factor", f));
            Check.that(factor.isEmpty() || frontArc.isPresent(), "an armour factor goes with its front_arc");
        }

        /** M9 part C: whether it is facing armour (a front arc with a factor, not a walker's glance). */
        public boolean facing() {
            return factor.isPresent();
        }

        /** Whether it is {@code hardened}. */
        public boolean hardened() {
            return text.filter("hardened"::equals).isPresent();
        }

        /**
         * M7 part E: whether it is {@code vanes} (design/enemies/ground/dust-devil): while its spin cycle is closed
         * every shot glances off, a blast (the Smart Bomb, the Airstrike) excepted (round 36 Q.22e); open, none.
         */
        public boolean vanes() {
            return text.filter(VANES::equals).isPresent();
        }

        /** M7 part E: the closed vanes' armour. */
        public static final String VANES = "vanes";

        /**
         * M8 part E: whether it is {@code shell} (design/enemies/naval/spiral-nautilus): while its coil is closed every
         * shot glances off, the specials' blasts and {@code anti-sub} fire excepted (§5 E8); open, none.
         */
        public boolean shell() {
            return text.filter(SHELL::equals).isPresent();
        }

        /** M8 part E: the closed coil's shell. */
        public static final String SHELL = "shell";

        /** The armour written as a text. */
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static Armour of(String text) {
            return new Armour(Optional.of(text), Optional.empty(), Optional.empty());
        }
    }

    /**
     * A part of a multi-part unit ({@code parts: multi}): a hit box around the unit's centre in its
     * own frame (facing down the screen: dx right, dy up towards its tail).
     *
     * @param kind {@code armoured}, {@code destroyable} or {@code vital} (destroying it destroys the
     *     rest); part G: a boss's {@code armoured} part is fire-only (it fires its attack, never
     *     takes damage, is not in the bar and pays nothing), written without {@code hp} and
     *     {@code bounty} (0 here)
     * @param attack the name of the unit's attack it fires
     * @param multiplier the damage it takes is multiplied by this (a weak point); 1 when not given
     * @param layer M5 part E: an arena boss's part's starting layer ({@code sub} or {@code ground});
     *     its script moves it
     * @param spots M5 part E: weak spots on it (the Kraken's eyes)
     * @param aim round 34 (P2, the kinder Siege Spire): {@code down}, the part fires its aimed attack
     *     straight down the screen instead of at the ship; empty: as its attack aims
     * @param slows M8 part B (design/enemies/ground/drill-rig: each pump lost slows the collar's ring 0.5 s): on a
     *     unit's part that carries its {@code attack}, the seconds its loss adds to that attack's interval; the attack
     *     stops when every part that carries it is lost; none if not given
     * @param attacks M8 part C (design/enemies/naval/eel-swarm: the head's aimed shot and its fan): the unit's attacks
     *     the part fires, by name, instead of one {@code attack} ({@link #attackNames})
     */
    record PartData(
            String name,
            Point offset,
            Size hitbox,
            double hp,
            String kind,
            int bounty,
            Optional<String> attack,
            Optional<Double> multiplier,
            Optional<Integer> firstBonus,
            Optional<String> layer,
            Optional<List<SpotData>> spots,
            Optional<String> aim,
            Optional<Double> slows,
            Optional<List<String>> attacks) {
        public PartData {
            Check.that(attack.isEmpty() || attacks.isEmpty(), "a part fires one attack or a list of attacks, not both");
            attacks.ifPresent(names -> Check.that(names.size() >= 2, "attacks: two or more (else write attack)"));
            attacks = attacks.map(List::copyOf);
            aim.ifPresent(a -> Check.that(a.equals("down"), "a part's aim is down, was '" + a + "'"));
            slows.ifPresent(v -> Check.positive("slows", v));
            Check.that(slows.isEmpty() || attack.isPresent(), "only a part that carries an attack slows it (slows)");
            Check.that(aim.isEmpty() || attack.isPresent(), "a part aimed down fires an attack");
            firstBonus.ifPresent(b -> Check.notNegative("first_bonus", b));
            // M7 part H: or buried (a breaching boss's mouth until its rise, its vents until they rise with it).
            layer.ifPresent(l -> Check.that(
                    l.equals("sub") || l.equals("ground") || l.equals("buried"),
                    "a part's layer is sub, ground or buried, was '" + l + "'"));
            Check.that(
                    kind.equals("destroyable") || kind.equals("vital") || kind.equals("armoured"),
                    "kind must be armoured, destroyable or vital, was '" + kind + "'");
            if (kind.equals("armoured")) {
                Check.that(hp == 0 && bounty == 0, "an armoured part has no hp and no bounty");
            } else {
                Check.positive("hp", hp);
            }
            multiplier.ifPresent(m -> Check.positive("multiplier", m));
        }

        /** A part as written: an {@code armoured} one without {@code hp} and {@code bounty}. */
        @JsonCreator
        static PartData of(
                @com.fasterxml.jackson.annotation.JsonProperty("name") String name,
                @com.fasterxml.jackson.annotation.JsonProperty("offset") Point offset,
                @com.fasterxml.jackson.annotation.JsonProperty("hitbox") Size hitbox,
                @com.fasterxml.jackson.annotation.JsonProperty("hp") Optional<Double> hp,
                @com.fasterxml.jackson.annotation.JsonProperty("kind") String kind,
                @com.fasterxml.jackson.annotation.JsonProperty("bounty") Optional<Integer> bounty,
                @com.fasterxml.jackson.annotation.JsonProperty("attack") Optional<String> attack,
                @com.fasterxml.jackson.annotation.JsonProperty("multiplier") Optional<Double> multiplier,
                @com.fasterxml.jackson.annotation.JsonProperty("first_bonus") Optional<Integer> firstBonus,
                @com.fasterxml.jackson.annotation.JsonProperty("layer") Optional<String> layer,
                @com.fasterxml.jackson.annotation.JsonProperty("spots") Optional<List<SpotData>> spots,
                @com.fasterxml.jackson.annotation.JsonProperty("aim") Optional<String> aim,
                @com.fasterxml.jackson.annotation.JsonProperty("slows") Optional<Double> slows,
                @com.fasterxml.jackson.annotation.JsonProperty("attacks") Optional<List<String>> attacks) {
            Check.that(
                    name != null && offset != null && hitbox != null && kind != null,
                    "a part has a name, an offset, a hit box and a kind");
            boolean armoured = kind.equals("armoured");
            Optional<Double> hpValue = hp == null ? Optional.empty() : hp;
            Optional<Integer> bountyValue = bounty == null ? Optional.empty() : bounty;
            Check.that(armoured || (hpValue.isPresent() && bountyValue.isPresent()), "a part has hp and a bounty");
            Check.that(
                    !armoured || (hpValue.isEmpty() && bountyValue.isEmpty()),
                    "an armoured part has no hp and no bounty");
            return new PartData(
                    name,
                    offset,
                    hitbox,
                    hpValue.orElse(0.0),
                    kind,
                    bountyValue.orElse(0),
                    attack == null ? Optional.empty() : attack,
                    multiplier == null ? Optional.empty() : multiplier,
                    firstBonus == null ? Optional.empty() : firstBonus,
                    layer == null ? Optional.empty() : layer,
                    spots == null ? Optional.empty() : spots,
                    aim == null ? Optional.empty() : aim,
                    slows == null ? Optional.empty() : slows,
                    attacks == null ? Optional.empty() : attacks);
        }

        /** M8 part C: the names of the attacks it fires, its {@code attack} or its {@code attacks}; none if neither. */
        public List<String> attackNames() {
            return attack.map(List::of).or(() -> attacks).orElse(List.of());
        }

        /** Whether it is a fire-only part. */
        public boolean armoured() {
            return kind.equals("armoured");
        }
    }

    /**
     * M5 part E, a weak spot on a part (the Kraken's eyes): a box of {@code hitbox} at {@code offset}
     * ({@code [dx, dy]} px from the part's centre, dx right, dy up) that routes the damage to the part
     * at its {@code multiplier}; with {@code while: open} only while the part's surfacing window is
     * open (the part's own multiplier elsewhere).
     */
    record SpotData(
            String name,
            Point offset,
            Size hitbox,
            double multiplier,

            @com.fasterxml.jackson.annotation.JsonProperty("while")
            Optional<String> during) {
        public SpotData {
            Check.positive("multiplier", multiplier);
            during.ifPresent(w -> Check.that(w.equals("open"), "while must be open, was '" + w + "'"));
        }
    }

    /**
     * An attack pattern (design/enemies/README.md, Attack pattern vocabulary).
     *
     * @param bullet the bullet class, which sets the damage
     * @param interval seconds between shots
     * @param speed bullet speed in px/s
     * @param firstShotDelay seconds from stopping to the first shot
     * @param count a {@code fan}'s bullets
     * @param spread a {@code fan}'s angle from its first to its last bullet, degrees; M5 part D: an aimed
     *     attack's with {@code aim: up}, from its burst's first shot to its last
     * @param turnRate a turret's barrel turn rate, °/s; the shots leave along the barrel
     * @param arc a turret fires while the player is within this many degrees of its facing (down the screen)
     */
    /**
     * M9 part A, a {@code laser-line}'s line (design/enemies/ground/rail-bunker; the vocabulary's {@code laser-line}):
     * at the telegraph's start a line is locked from the unit through the ship's position to the screen's edge and drawn
     * for {@code telegraph} s (at least 0.8, the vocabulary's minimum; a difficulty's hook {@code telegraph} replaces
     * it), then it is live {@code live} s as a band {@code band} px wide that hits the ship once for the attack's bullet
     * class; the unit holds fire while the ship is within {@code hold_within} px of it.
     */
    record Line(double telegraph, double live, double band, double holdWithin) {
        /** The vocabulary's shortest telegraph (design/enemies, Attack pattern vocabulary). */
        public static final double MIN_TELEGRAPH = 0.8;

        public Line {
            Check.that(telegraph >= MIN_TELEGRAPH, "line.telegraph: at least " + MIN_TELEGRAPH + " s");
            Check.positive("line.live", live);
            Check.positive("line.band", band);
            Check.notNegative("line.hold_within", holdWithin);
        }
    }

    /**
     * @param aim where a fan points, {@code target} (the default), {@code down} or {@code facing}; a walker's fan
     *     (M5 part B) is aimed at the player with {@code target} and along its facing with {@code facing};
     *     planned (part D) for the other units; M5 part D: an aimed attack's {@code up} (the Wraith, user
     *     decision of 2026-10-08) sends its bursts straight up the screen as a fixed fan of {@code spread},
     *     its shots in turn from the left edge to the right, aimed at nobody and fired however close the
     *     ship is
     * @param away planned (part D): an aimed attack fires only while the player is more than this many ° off its facing
     * @param spawn planned (part D): the {@code spawn} pattern's release, which has no bullet, interval or speed
     * @param mortar the {@code mortar} pattern's lob (its {@code bullet} is the direct hit, its {@code speed} the ring's)
     * @param burst an aimed attack's shots per volley
     * @param burstGap seconds between the shots of a burst
     * @param rotate the parts sharing the attack take turns, one volley every interval among the living ones
     * @param arms a {@code spiral}'s arms; its {@code interval} is between two bullets of an arm
     * @param duration seconds a {@code spiral} runs before it hands over in an alternation; part G:
     *     without it a spiral fires for as long as its phase lasts (it cannot alternate)
     * @param stagger M5 part B, a walker's fan (design/enemies/ground/creeper): the units of one wave
     *     share a volley clock, unit i (in the order they enter) firing i × this many seconds after
     *     the first; a unit off the screen skips its turn
     * @param pounce M5 part C, the {@code pounce} pattern's leap (design/enemies/ground/ravager): no
     *     bullet and no speed; its {@code interval} is the s from landing until it may leap again
     * @param within M5 part E, a {@code ring}'s reach (design/enemies/naval/driftjelly): it fires only
     *     while the ship's centre is this close, px, on either of its layers; its {@code interval}
     *     the cooldown from the last ring
     * @param rate M5 part F, the {@code drain} pattern (design/enemies/air/lamprey): shield points a
     *     second it drains while latched, no bullet
     * @param staggered M5 part H, a boss's {@code aimed} or {@code mortar} attack fired by several parts
     *     (design/enemies/bosses/siege-spire: the pods' orbs, the mortar roots' lobs): each part on its own
     *     clock, part k of n {@code k × interval ÷ n} after the first; not with {@code rotate}
     * @param link M7 part C, the {@code link} pattern (design/enemies/ground/tendril-anchor): the tendril between two
     *     anchors of a linked nest, its touch the attack's {@code bullet} class; no speed or interval
     * @param pores M7 part C, the {@code pores} pattern (design/enemies/ground/threadcrawler): a chain's pore wave,
     *     each segment's pore an aimed {@code bullet} at {@code speed} in turn head to tail, {@code interval} s apart
     * @param only M7 part C: the difficulties it fires on (the Tendril Anchor's hard orbs); every one if not given
     * @param aura M7 part D, the {@code aura} pattern (design/enemies/air/choir-herald): the shield rings it sings onto
     *     the enemies near it; no bullet, speed, interval or count
     * @param aimTurns round 40 (design/enemies/bosses/siege-spire, proposal 4a of the Act 2 playtest): a boss's
     *     {@code aimed} attack fired by several parts takes turns to aim, one part at a time, each after a glow
     *     (the tell); between its turns a part fires as the attack says ({@code aim: down}: straight down its
     *     column); not with {@code rotate}
     * @param whileState M7 part E ({@code while}, design/enemies/ground/dust-devil): the state of its unit it fires
     *     in, {@code open} (a spin cycle's open window: the Dust Devil's grit spiral); always if not given
     * @param trigger M7 part E (design/enemies/air/ghost-drone): an aimed attack fired once per pass, its burst when
     *     the ship's x comes within {@code within_x} px of the unit's, instead of on an interval
     * @param tell M7 part E: s of the tell before the burst's first shot (the Ghost Drone's red eye glint)
     * @param reaim M7 part E: every shot of the burst is aimed at the ship anew (not the first shot's line)
     * @param parts M7 part F: a boss's parts that fire it, besides those naming it as their {@code attack} (the
     *     walker's pods fire one salvo pattern in its first phase, another later)
     * @param homing M7 part F: an aimed attack's shots are homing missiles (the walker's pods)
     */
    record Attack(
            String pattern,
            Optional<String> name,
            Optional<String> bullet,
            Optional<Double> interval,
            Optional<Double> speed,
            Optional<Double> firstShotDelay,
            Optional<Integer> count,
            Optional<Double> spread,
            Optional<Double> turnRate,
            Optional<Double> arc,
            Optional<Mine> mine,
            Optional<String> aim,
            Optional<Double> away,
            Optional<Spawn> spawn,
            Optional<Mortar> mortar,
            Optional<Integer> burst,
            Optional<Double> burstGap,
            Optional<Boolean> rotate,
            Optional<Integer> arms,
            Optional<Double> duration,
            Optional<Sweep> sweep,
            Optional<Double> stagger,
            Optional<Pounce> pounce,
            Optional<Double> within,
            Optional<Double> rate,
            Optional<Boolean> staggered,
            Optional<Link> link,
            Optional<Pores> pores,
            Optional<List<String>> only,
            Optional<Aura> aura,
            Optional<AimTurns> aimTurns,
            @JsonProperty("while") Optional<String> whileState,
            Optional<Trigger> trigger,
            Optional<Double> tell,
            Optional<Boolean> reaim,
            Optional<List<String>> parts,
            Optional<Homing> homing,
            Optional<Slow> slow,
            Optional<Line> line,
            Optional<AirLink> airLink) {
        /** M7 part E: {@code while: open}, a spin cycle's open window. */
        public static final String WHILE_OPEN = "open";

        /** M9 part A: the {@code laser-line} pattern (design/enemies/ground/rail-bunker), with its {@link Line}. */
        public static final String LASER_LINE = "laser-line";

        /** M9 part D: the {@code air-link} pattern (design/enemies/space/shard-drone), with its {@link AirLink}. */
        public static final String AIR_LINK = "air-link";

        public Attack {
            Check.that(
                    pattern.equals(AIR_LINK) == airLink.isPresent(),
                    "an air-link attack has its air_link, the others none");
            Check.that(
                    !pattern.equals(AIR_LINK) || (bullet.isPresent() && speed.isEmpty() && interval.isEmpty()),
                    "an air-link attack names the bullet class of its touch, no speed or interval");
            whileState.ifPresent(state -> Check.that(
                    WHILE_OPEN.equals(state), "while: only 'open' (a spin cycle's window), was '" + state + "'"));
            Check.that(
                    trigger.isEmpty() || (pattern.equals("aimed") && interval.isEmpty()),
                    "a triggered attack is aimed and fires once per pass, no interval");
            tell.ifPresent(t -> Check.positive("tell", t));
            Check.that(reaim.isEmpty() || burst.isPresent(), "only a burst re-aims its shots (reaim needs a burst)");
            parts.ifPresent(list -> Check.notEmpty("parts", list));
            Check.that(
                    homing.isEmpty()
                            || (pattern.equals("aimed")
                                    && aim.filter("up"::equals).isEmpty()),
                    "only an aimed attack fires homing missiles (not straight up)");
            Check.that(pattern.equals("aura") == aura.isPresent(), "an aura attack has its aura, the others none");
            Check.that(
                    !pattern.equals("aura")
                            || (bullet.isEmpty()
                                    && speed.isEmpty()
                                    && interval.isEmpty()
                                    && count.isEmpty()
                                    && firstShotDelay.isEmpty()),
                    "an aura has no bullet, speed, interval, count or first shot");
            Check.that(pattern.equals("link") == link.isPresent(), "a link attack has its link, the others none");
            Check.that(
                    !pattern.equals("link")
                            || (bullet.isPresent()
                                    && speed.isEmpty()
                                    && interval.isEmpty()
                                    && firstShotDelay.isEmpty()
                                    && only.isEmpty()),
                    "a link has its touch's bullet class and no speed, interval, first shot or difficulties");
            Check.that(pattern.equals("pores") == pores.isPresent(), "a pores attack has its pores, the others none");
            Check.that(
                    !pattern.equals("pores") || interval.isPresent(),
                    "a pores attack has its interval (s between two pores)");
            only.ifPresent(names -> {
                Check.notEmpty("only", names);
                names.forEach(Difficulty::of);
            });
            Check.that(
                    staggered.isEmpty() || pattern.equals("aimed") || pattern.equals("mortar"),
                    "only an aimed or a mortar attack is staggered");
            Check.that(
                    !staggered.orElse(false) || !rotate.orElse(false), "an attack rotates or is staggered, not both");
            // M5 part F: a drain (the Lamprey's, while latched) has its rate of shield points a second and nothing
            // else.
            Check.that(pattern.equals("drain") == rate.isPresent(), "a drain has its rate, the others none");
            rate.ifPresent(r -> Check.positive("rate", r));
            Check.that(
                    !pattern.equals("drain")
                            || (bullet.isEmpty() && speed.isEmpty() && interval.isEmpty() && count.isEmpty()),
                    "a drain has no bullet, speed, interval or count");
            Check.that(pattern.equals("ring") || within.isEmpty(), "only a ring fires within a reach");
            within.ifPresent(w -> Check.positive("within", w));
            Check.that(within.isEmpty() || interval.isPresent(), "a ring fired within a reach has its interval");
            Check.that(
                    pattern.equals("laser-sweep") == sweep.isPresent(), "a laser-sweep has its sweep, the others none");
            // M9 part A (design/enemies/ground/rail-bunker): a laser-line's locked line, its bullet class once.
            Check.that(pattern.equals(LASER_LINE) == line.isPresent(), "a laser-line has its line, the others none");
            Check.that(
                    !pattern.equals(LASER_LINE)
                            || (bullet.isPresent() && speed.isEmpty() && interval.isPresent() && count.isEmpty()),
                    "a laser-line has a bullet and an interval and no speed or count");
            Check.that(
                    line.isEmpty()
                            || interval.filter(i -> i
                                            >= line.get().telegraph()
                                                    + line.get().live())
                                    .isPresent(),
                    "a laser-line's interval holds its telegraph and its live time");
            Check.that(
                    !pattern.equals("laser-sweep") || (bullet.isPresent() && speed.isEmpty()),
                    "a laser-sweep has a bullet and no speed");
            Check.that(
                    pattern.equals("aimed")
                            || pattern.equals("fan")
                            || pattern.equals("mine")
                            || pattern.equals("spawn")
                            || pattern.equals("mortar")
                            || pattern.equals("ring")
                            || pattern.equals("spiral")
                            || pattern.equals("laser-sweep")
                            || pattern.equals(LASER_LINE)
                            || pattern.equals("pounce")
                            || pattern.equals("drain")
                            || pattern.equals("link")
                            || pattern.equals("pores")
                            || pattern.equals("aura")
                            || pattern.equals(AIR_LINK),
                    "pattern must be aimed, fan, mine, spawn, mortar, ring, spiral, laser-sweep, laser-line, pounce,"
                            + " drain, link, pores, aura or air-link, was '" + pattern + "'");
            Check.that(pattern.equals("pounce") == pounce.isPresent(), "a pounce has its pounce, the others none");
            Check.that(
                    !pattern.equals("pounce") || (bullet.isEmpty() && speed.isEmpty() && interval.isPresent()),
                    "a pounce has an interval and no bullet or speed");
            Check.that(pattern.equals("mine") == mine.isPresent(), "a mine attack has its mine, the others none");
            Check.that(
                    pattern.equals("mortar") == mortar.isPresent(), "a mortar attack has its mortar, the others none");
            // Round 43: a boss's fan may come in a burst too (the walker's chin and flank guns; FoundryChecks).
            Check.that(
                    pattern.equals("aimed")
                            || (pattern.equals("fan") && rotate.isEmpty())
                            || (burst.isEmpty() && burstGap.isEmpty() && rotate.isEmpty()),
                    "only an aimed attack (or a boss's fan) has a burst, a burst gap or rotates");
            burst.ifPresent(b -> Check.positive("burst", b));
            burstGap.ifPresent(g -> Check.positive("burst_gap", g));
            Check.that(pattern.equals("spiral") == arms.isPresent(), "a spiral has its arms, the others none");
            Check.that(pattern.equals("spiral") || duration.isEmpty(), "only a spiral has a duration");
            arms.ifPresent(a -> Check.positive("arms", a));
            duration.ifPresent(d -> Check.positive("duration", d));
            Check.that(!pattern.equals("spiral") || turnRate.isPresent(), "a spiral has a turn rate");
            Check.that(pattern.equals("spawn") == spawn.isPresent(), "a spawn attack has its spawn, the others none");
            Check.that(
                    pattern.equals("laser-sweep")
                            || pattern.equals(LASER_LINE)
                            || pattern.equals("pounce")
                            || pattern.equals("drain")
                            || pattern.equals("link")
                            || pattern.equals(AIR_LINK)
                            || pattern.equals("aura")
                            // M8 part E: a slowing ring is sound, no bullet (its speed below).
                            || slow.isPresent()
                            || pattern.equals("spawn") != (bullet.isPresent() && speed.isPresent()),
                    "an attack has a bullet and a speed, a spawn attack neither");
            Check.that(!pattern.equals("spawn") || interval.isEmpty(), "a spawn attack has no interval");
            aim.ifPresent(a -> Check.that(
                    a.equals("target") || a.equals("down") || a.equals("facing") || a.equals("up"),
                    "aim must be target, down, facing or up, was '" + a + "'"));
            away.ifPresent(a -> Check.notNegative("away", a));
            interval.ifPresent(i -> Check.positive("interval", i));
            speed.ifPresent(s -> Check.positive("speed", s));
            firstShotDelay.ifPresent(d -> Check.notNegative("first_shot_delay", d));
            Check.that(
                    (pattern.equals("fan") || (pattern.equals("ring") && slow.isEmpty())) == count.isPresent(),
                    "a fan or a ring of bullets has a count, the others none");
            Check.that(slow.isEmpty() || pattern.equals("ring"), "only a ring slows");
            Check.that(
                    slow.isEmpty()
                            || (bullet.isEmpty()
                                    && speed.isPresent()
                                    && interval.isPresent()
                                    && tell.isPresent()
                                    && within.isEmpty()),
                    "a slowing ring has its speed, interval and tell, and no bullet or reach");
            boolean up = aim.filter("up"::equals).isPresent();
            Check.that(!up || pattern.equals("aimed"), "only an aimed attack fires straight up");
            Check.that(
                    (pattern.equals("fan") || up) == spread.isPresent(),
                    "a fan or an aimed attack fired up has a spread, the others none");
            count.ifPresent(c -> Check.positive("count", c));
            Check.that(pattern.equals("fan") || stagger.isEmpty(), "only a fan has a stagger");
            stagger.ifPresent(st -> Check.positive("stagger", st));
            Check.that(
                    aimTurns.isEmpty() || (pattern.equals("aimed") && !rotate.orElse(false)),
                    "only an aimed attack that does not rotate takes aim turns");
        }

        /** M7 part C: whether it fires on {@code difficulty}: its {@code only}, every one if not given. */
        public boolean firesOn(Difficulty difficulty) {
            return only.map(names -> names.stream().map(Difficulty::of).anyMatch(difficulty::equals))
                    .orElse(true);
        }

        /** M7 part E: whether it fires only while its unit's spin cycle is open ({@code while: open}). */
        public boolean whileOpen() {
            return whileState.filter(WHILE_OPEN::equals).isPresent();
        }

        /** M7 part E: the pass trigger's reach, px ({@code trigger.within_x}); NaN without a trigger. */
        public double passWithinX() {
            return trigger.map(Trigger::withinX).orElse(Double.NaN);
        }
    }

    /**
     * M7 part E, a pass trigger (design/enemies/air/ghost-drone): the attack fires its burst once per pass, when the
     * ship's centre comes within {@code within_x} px of the unit's x.
     */
    record Trigger(double withinX) {
        public Trigger {
            Check.positive("within_x", withinX);
        }
    }

    /**
     * Round 40, an aimed boss attack's aim turns (design/enemies/bosses/siege-spire, proposal 4a): every {@code every}
     * s the next living part in turn fires one shot at the ship, after glowing {@code glow} s (the tell).
     */
    record AimTurns(double every, double glow) {
        public AimTurns {
            Check.positive("every", every);
            Check.notNegative("glow", glow);
            Check.that(glow < every, "an aim turn's glow is shorter than its every");
        }
    }

    /**
     * A spawner's release: {@code count} units of {@code enemy} flying out at {@code speed} in an
     * {@code arc} of ° toward the ship. A brood (Level 04's Brood Pod) releases them when it is
     * killed or {@code after} s from entering (a self-burst paying {@code burst_bounty}, not a kill),
     * with a {@code telegraph} of s before it. M5 part C, a periodic spawner (the Hive Node): instead
     * of {@code after} and {@code burst_bounty}, {@code every} s from its centre crossing the top
     * edge, its iris opening over the {@code telegraph}, and an opening due while the ship's centre
     * is within {@code shut_within} px skipped; it never bursts on its own. M8 part D (the spawning reef): a periodic
     * spawn of a kind with a flock's ram (the Glassfin) may give its released units' {@code ram_after}, s on the field
     * before each draws its ram, instead of the kind's own (released mid-screen, the school's 3 s would see them gone).
     */
    record Spawn(
            String enemy,
            int count,
            Optional<Double> after,
            Optional<Double> every,
            double telegraph,
            double arc,
            double speed,
            Optional<Integer> burstBounty,
            Optional<Double> shutWithin,
            Optional<Double> ramAfter,
            Optional<Stream> stream) {
        public Spawn {
            // M9 part C: a launcher's streams are periodic, from its centre, never shut, never bursting.
            Check.that(
                    stream.isEmpty() || (every.isPresent() && shutWithin.isEmpty() && ramAfter.isEmpty()),
                    "a stream launcher has an every and no shut_within or ram_after");
            ramAfter.ifPresent(a -> Check.notNegative("ram_after", a));
            Check.that(every.isPresent() || ramAfter.isEmpty(), "only a periodic spawn has a ram_after");
            Check.positive("count", count);
            Check.that(after.isPresent() != every.isPresent(), "a spawn has an after (a brood) or an every, not both");
            after.ifPresent(a -> Check.positive("after", a));
            every.ifPresent(e -> Check.positive("every", e));
            Check.notNegative("telegraph", telegraph);
            Check.positive("arc", arc);
            Check.positive("speed", speed);
            Check.that(
                    after.isPresent() == burstBounty.isPresent(),
                    "a brood has a burst_bounty, a periodic spawn none (it never bursts)");
            burstBounty.ifPresent(b -> Check.notNegative("burst_bounty", b));
            Check.that(every.isPresent() || shutWithin.isEmpty(), "only a periodic spawn has a shut_within");
            shutWithin.ifPresent(d -> Check.notNegative("shut_within", d));
            every.ifPresent(e -> Check.that(telegraph < e, "the telegraph lies inside the cycle (every)"));
        }

        /** Whether it is a periodic spawn (with {@code every}). */
        public boolean periodic() {
            return every.isPresent();
        }
    }

    /**
     * M9 part C (design/enemies/air/gilded-gunship, its Dart streams): a spawn that launches streams while its unit
     * flies the field (the unit keeps its other attacks): {@code count} units every {@code every} s, {@code gap} s apart,
     * the first stream {@code first} s after it entered the field, spread over the spawn's {@code arc} round its facing
     * (down); a launched unit flies straight {@code straight} s before its chase; none while {@code most} of its units
     * live.
     */
    record Stream(double gap, double first, double straight, int most) {
        public Stream {
            Check.positive("gap", gap);
            Check.notNegative("first", first);
            Check.notNegative("straight", straight);
            Check.positive("most", most);
        }
    }

    /** A formation it appears in, with the unit count: {@code [n]} or {@code [min, max]}. */
    record FormationUse(String name, Optional<List<Integer>> size) {
        public FormationUse {
            size.ifPresent(s -> Check.that(s.size() == 1 || s.size() == 2, "size must be [n] or [min, max]"));
        }
    }

    /**
     * The glowing weak point; only a part of a multi-part unit has a damage {@code multiplier}
     * (design/enemies, stat block template), a single-part unit's is drawn only.
     */
    record WeakPoint(String name, Optional<Double> multiplier) {
        public WeakPoint {
            multiplier.ifPresent(m -> Check.positive("multiplier", m));
        }
    }

    /** Every {@code every}-th kill of this enemy in a level drops {@code pickup}. */
    record Drop(Pickup pickup, int every) {
        public Drop {
            Check.positive("every", every);
        }
    }

    /**
     * A dropped spore mine: it arms (rises to the player plane) after {@code arm} s, drifts
     * {@code drift} px/s in a random direction, has {@code hp}, and bursts after {@code life} s into
     * a {@code ring} of {@code ringBullet} bullets at the attack's speed; destroyed it pays {@code credits}.
     */
    record Mine(
            double arm,
            double life,
            double drift,
            double hp,
            int ring,
            String ringBullet,
            int credits,
            Optional<Double> proximity,
            Optional<Double> blink,
            Optional<Blast> blast,
            Optional<Double> chain,
            Optional<Double> hop,
            Optional<MineLine> line) {
        public Mine {
            Check.notNegative("arm", arm);
            Check.positive("life", life);
            Check.positive("hp", hp);
            // M9 part C: a line mine has its proximity, blink and hop; it holds its place in the field.
            Check.that(
                    proximity.isPresent() == blink.isPresent() && proximity.isPresent() == hop.isPresent(),
                    "a line mine has its proximity, blink and hop together");
            Check.that(
                    proximity.isPresent() || (blast.isEmpty() && chain.isEmpty() && line.isEmpty()),
                    "only a line mine (with a proximity) has a blast, a chain or a line");
            Check.that(proximity.isEmpty() || drift == 0, "a line mine holds its place: drift 0");
            proximity.ifPresent(p -> Check.positive("proximity", p));
            blink.ifPresent(b -> Check.notNegative("blink", b));
            chain.ifPresent(c -> Check.notNegative("chain", c));
            hop.ifPresent(h -> Check.positive("hop", h));
        }

        /** M9 part C: whether it is a line mine (design/enemies, Mine lines). */
        public boolean lineMine() {
            return proximity.isPresent();
        }
    }

    /**
     * M9 part C (design/campaign Level 30, C2 a): a line mine's proximity blast: the ship, Rook and the Bulwark take
     * {@code bullet}'s damage once where their hulls touch the disc of {@code radius} px round the mine.
     */
    record Blast(double radius, String bullet) {
        public Blast {
            Check.positive("radius", radius);
        }
    }

    /**
     * M9 part C (design/enemies/space/minelayer, The mine line): a layer's line across the play field: {@code clusters}
     * clusters of {@code perCluster} mines {@code spacing} px apart ({@code middleShort} fewer in a middle cluster: easy's
     * 3 beside a wider gap), {@code clusterGap} px between two clusters and the one {@code gap} at the boundary its wave
     * names; at most {@code most} line mines alive (an unfinished line is not topped up).
     */
    record MineLine(
            int clusters,
            int perCluster,
            double spacing,
            double clusterGap,
            double gap,
            Optional<Integer> middleShort,
            int most) {
        public MineLine {
            Check.that(clusters >= 2, "a line has two clusters at least (its gap is at a boundary): " + clusters);
            Check.positive("per_cluster", perCluster);
            Check.positive("spacing", spacing);
            Check.positive("cluster_gap", clusterGap);
            Check.positive("gap", gap);
            middleShort.ifPresent(
                    m -> Check.that(m >= 0 && m < perCluster, "middle_short: 0 to per_cluster - 1, was " + m));
            Check.positive("most", most);
        }
    }

    /**
     * A mortar's lob: the impact marker shows {@code marker} s ahead (the blob's flight), a ship
     * inside the {@code impact} px circle when it lands takes the direct hit, and it bursts into a
     * ring of {@code ring} bullets of class {@code ringBullet}. Parsed; flown with the Polyp Mortar.
     */
    record Mortar(double marker, double impact, int ring, String ringBullet) {
        public Mortar {
            Check.positive("marker", marker);
            Check.positive("impact", impact);
            Check.positive("ring", ring);
        }
    }

    /**
     * An articulated neck: {@code segments} {@code armoured} hit boxes from the anchor {@code from}
     * on the body to the part {@code to} at its end (whose offset is the chain's rest end); each
     * segment follows the one before it {@code lag} s late, and the chain turns at most
     * {@code bend} degrees towards the player.
     */
    record ChainData(
            String name,
            Point from,
            String to,
            int segments,
            Size hitbox,
            double lag,
            double bend,
            Optional<String> motion) {
        public ChainData {
            Check.positive("segments", segments);
            Check.positive("lag", lag);
            Check.notNegative("bend", bend);
            motion.ifPresent(m -> Check.that(m.equals("slam"), "motion must be slam, was '" + m + "'"));
        }

        /** M5 part E: whether it is a slam arm ({@code motion: slam}): it follows the slam cycle instead of bending. */
        public boolean slams() {
            return motion.isPresent();
        }
    }
}
