package vanguard.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import vanguard.sim.PlayField;

/**
 * design/campaign/&lt;act&gt;/&lt;level&gt;/data.yaml: a level's script. Times are seconds from
 * the level start.
 *
 * <p>Its nested records are declared per data block in the sealed {@code *Block} interfaces it implements ({@link
 * WavesBlock}, {@link RadioBlock}, {@link ObjectivesBlock}, …), so they are still named {@code LevelData.Wave} and so
 * on; only those that are imported by their canonical name ({@link Entry}, {@link Music}, {@link Visibility}) are
 * declared here, with the level-wide {@code difficulty} changes ({@link Difficulties}).
 *
 * @param scrollSpeed px/s unless a section sets its own
 * @param launchSeconds the non-playable launch before control starts
 * @param bountyScale multiplies every bounty paid in the level (design/systems/economy), 1 if not
 *     given
 * @param crateSeconds how long an uncollected secret's crate (or data core) stays, the player's
 *     {@code pickup_seconds} if not given (Level 10's ferry cache: long enough to drift from the top
 *     edge to the ship's start line)
 * @param controlPrompts the control prompts shown in the first section
 * @param prompts contextual prompts shown at a time (design/ui/hud: one line, an action and its keys)
 * @param sections back to back from t = 0
 * @param cranes the crane hazards (Level 02: Crane Four)
 * @param debris the debris field's chunks (Level 03)
 * @param setPieces the huge set-piece units and their passes (Level 03's Leviathan)
 * @param road the road on the ground layer that an escort objective's convoy follows (Level 04)
 * @param boss the level's boss and where it arrives (Level 05's Gorgon Frigate)
 * @param sleds the mass-driver sleds on a rail (Level 05)
 * @param rocks the rocks destroyed ground units throw in low gravity (Level 05)
 * @param pickups pickups placed by the script (normal drops come from the enemies)
 * @param tows part G: friendly craft drifting on the air layer that tow a secret's crate on a
 *     cable (Level 07's lifeboat tow)
 * @param radio the radio chatter
 * @param backdrop the parallax layers behind and above the play plane
 * @param threatProfile what the hangar intel panel shows before the level (design/ui/hangar)
 * @param briefing the mission briefing before the level (design/ui/briefing)
 * @param holds M5 part C: the hold zones, where the scroll eases down until their groups are gone
 *     and the level clock (script time) slows with it (Level 09's node clusters)
 * @param collapse M5 part C: the collapse once its groups are cleared (Level 09's arcology)
 * @param water M5 part E (user decision E1 = a): the whole play field is water (Levels 11–13), where
 *     the torpedo runs; false if not given
 * @param convoy M5 part E: a naval convoy outside the objectives (Level 11's cargo ships and frigate)
 * @param weather M5 part F: the level's wind, lightning, cloud-bank occlusion and rain (Level 12's storm)
 * @param sondes M5 part F: the weather sondes to fly through (Level 12)
 * @param dropFactor M5 part F: each enemy's drop {@code every} is divided by it, rounded to the nearest, at
 *     least 1 (Level 12's shield cells, 1.5); 1 if not given
 * @param pointDrops M5 part G (design/allies, CDF supply drone; user decision G6 = a): the data's {@code
 *     drops}, pickups released at a point in the field at a time (Level 13's supply drones' armour
 *     patches); named so beside {@link #drops()}, the drop factor
 * @param waterSplash M5 part G (user decision G5 = a): which bursts on a {@code water: true} level play the
 *     water splash: {@code all} (if not given, Levels 11 and 12) or {@code naval-only} (Level 13's ice: only
 *     a burst on a naval unit and a naval unit's death); presentation only
 * @param rootBursts M5 part H (design/campaign Level 14, Hazards; decision H2 = a): the root bursts, ground
 *     units grown at a point of the field after a telegraph instead of entering at the top edge
 * @param launch M7 part B (design/campaign Level 15, Layout; a default pending the user's review in round 38):
 *     {@code drop}, the ship (and the escort in his slot) drops from above the top edge to the start spot over the
 *     launch instead of rising from the bottom edge; the bottom launch if not given
 * @param noSurface M7 part B (Level 15's station pads, hab and rail line): the areas of the ground where no
 *     Burrower surfaces (the spot rule picks the nearest allowed spot, else skips that surfacing)
 * @param terrain M7 part C (design/campaign Level 16, Terrain): the canyon's walls and islands, rock on the play
 *     plane, and what a touch of it costs; none if not given
 * @param allies M7 part D (design/campaign Level 17, The Ares Landing domes; design/allies, Ares Landing dome): the
 *     ground allies that scroll through with the ground outside the objectives (Level 17's five domes) and the
 *     target-the-objective hook that goes for them; none if not given
 * @param flak M7 part D, presentation (Level 17's militia flak guns at the spaceport): flak bursts on {@code low-air}
 *     from a time on, hitting nothing; none if not given
 * @param visibility M7 part E, presentation (design/campaign Level 18, Visibility in the storm; M6 = a): the dust
 *     storm's view radius round the ship by level time; the simulation never reads it; full view if not given
 * @param belts M7 part F (design/campaign Level 19, Conveyors): the conveyor belts on the ground that carry ground
 *     units; none if not given
 * @param vents M7 part F (design/campaign Level 19, Hazards): the furnace vent rows; none if not given
 * @param ridge M7 part G, presentation (Level 20's travelling ridge, Level 21's ridge pass; round 43 Q.4): the Dust
 *     Colossus's ridge crossing the ground; none if not given
 * @param medium M8 part A (design/world/europa, Under water rules): the level's medium, {@code air} if not given; a
 *     section may set its own ({@link #mediumOf})
 * @param underground M9 part A (design/player/specials, Act 5 specials; Level 32): the level lies inside rock (the
 *     setting {@code underground}, its specials greyed {@code IN ROCK}); a section may set its own ({@link
 *     #undergroundOf})
 * @param specialLimits M8 part A (design/player/specials, Setting restrictions): the specials the level forbids, by
 *     their slugs, replacing its setting's list (a section's own replaces it again; {@link #specialLimitsOf}); the
 *     setting's if not given
 * @param underwaterLook M8 part C (design/world/europa, Palette and lighting): how the game grades the play field under
 *     water (presentation only), a section's {@code underwater_look} changing it ({@link #underwaterLookOf}); only on a
 *     level that is under water somewhere; none if not given
 * @param rookLoss M8 part F (design/campaign Level 26, Rook's loss; design/player/wingmen): Rook's scripted loss; none
 *     if not given
 * @param cruisers M8 part H (design/campaign Level 28, the run; design/enemies/bosses/abyssal-maw, The cruisers): the
 *     finale's cruiser run on the level clock; none if not given
 * @param armourFloor M8 part H (Level 28, The run cannot be lost; J2): from the boss's kill to the level's end the
 *     armour stops at this many points (only in a level with a boss); none (0) if not given
 * @param relay M9 part H (design/campaign Level 35): the mass-relay flash, once on the level clock; none if not given
 */
public record LevelData(
        double scrollSpeed,
        double launchSeconds,
        Optional<Double> bountyScale,
        Optional<Double> crateSeconds,
        List<String> controlPrompts,
        Optional<List<Prompt>> prompts,
        List<Section> sections,
        List<Wave> waves,
        List<GroundTarget> groundTargets,
        List<Secret> secrets,
        List<PlacedPickup> pickups,
        List<RadioCue> radio,
        Optional<List<CraneData>> cranes,
        Optional<DebrisField> debris,
        Optional<List<SetPieceData>> setPieces,
        Optional<Road> road,
        Optional<BossPlacement> boss,
        Optional<Sleds> sleds,
        Optional<Rocks> rocks,
        Objectives objectives,
        Music music,
        Difficulties difficulty,
        BackdropData backdrop,
        ThreatProfile threatProfile,
        Briefing briefing,
        Optional<Darkness> darkness,
        Optional<List<Tow>> tows,
        Optional<List<Hold>> holds,
        Optional<Collapse> collapse,
        Optional<Boolean> water,
        Optional<Convoy> convoy,
        Optional<Weather> weather,
        Optional<SondesData> sondes,
        Optional<Double> dropFactor,
        @JsonProperty("drops") Optional<List<Drop>> pointDrops,
        Optional<String> waterSplash,
        Optional<RootBursts> rootBursts,
        Optional<String> launch,
        Optional<List<NoSurface>> noSurface,
        Optional<TerrainBlock> terrain,
        Optional<GroundAllies> allies,
        Optional<Flak> flak,
        Optional<Visibility> visibility,
        Optional<Belts> belts,
        Optional<Vents> vents,
        Optional<Ridge> ridge,
        Optional<Medium> medium,
        Optional<List<String>> specialLimits,
        Optional<UnderwaterLook> underwaterLook,
        Optional<RookLoss> rookLoss,
        Optional<Cruisers> cruisers,
        Optional<Double> armourFloor,
        Optional<Boolean> underground,
        Optional<HaltWaves> haltWaves,
        Optional<EscortSwap> escortSwap,
        Optional<String> ground,
        Optional<Relay> relay)
        implements SectionsBlock,
                WavesBlock,
                SetPiecesBlock,
                GroundTargetsBlock,
                PickupsBlock,
                RadioBlock,
                ObjectivesBlock,
                EscortBlock,
                AlliesBlock,
                HazardsBlock,
                WeatherBlock,
                BriefingBlock {
    /** M9 part C: {@code ground: none}, a space-only level (Level 30): no ground layer, the bombs idle. */
    public static final String GROUND_NONE = "none";

    /** M9 part C: whether the level has no ground layer ({@code ground: none}). */
    public boolean noGround() {
        return ground.isPresent();
    }

    public LevelData {
        launch.ifPresent(kind -> Check.that(LAUNCH_DROP.equals(kind), "launch: only 'drop', was '" + kind + "'"));
        // M9 part C (design/campaign Level 30): ground: none, a level without a ground layer.
        ground.ifPresent(g -> Check.that(GROUND_NONE.equals(g), "ground: only 'none', was '" + g + "'"));
        noSurface = noSurface.map(List::copyOf);
        specialLimits = specialLimits.map(List::copyOf);
        dropFactor.ifPresent(factor -> Check.positive("drop_factor", factor));
        waterSplash.ifPresent(splash -> Check.that(
                WATER_SPLASH_ALL.equals(splash) || WATER_SPLASH_NAVAL.equals(splash),
                "water_splash: 'all' or 'naval-only', was '" + splash + "'"));
        Check.that(waterSplash.isEmpty() || water.orElse(false), "water_splash: only on a water: true level");
        pointDrops = pointDrops.map(List::copyOf);
        pointDrops.ifPresent(list -> {
            for (int i = 1; i < list.size(); i++) {
                Check.that(list.get(i).t() >= list.get(i - 1).t(), "drops[" + i + "]: listed in time order");
            }
        });
        armourFloor.ifPresent(floor -> Check.positive("armour_floor", floor));
        Check.positive("scroll_speed", scrollSpeed);
        Check.notNegative("launch_seconds", launchSeconds);
        bountyScale.ifPresent(scale -> Check.positive("bounty_scale", scale));
        crateSeconds.ifPresent(seconds -> Check.positive("crate_seconds", seconds));
        Check.notEmpty("sections", sections);
        for (int i = 1; i < sections.size(); i++) {
            Check.that(
                    sections.get(i).end() > sections.get(i - 1).end(),
                    "sections[" + i + "]: must end after the section before it");
        }
    }

    /**
     * After the level end the scroll runs on under the radio until its last message has been shown
     * (the outro), at most this long before the debrief, so the backdrop has to hold until then.
     * Lines that must play in flight, such as an act boss's closing lines (Level 07), need a last
     * section long enough for them: the aftermath after the arena is a section like any other, as
     * long as the data makes it.
     */
    public static final double OUTRO_SECONDS = 15;

    /** M7 part B: {@code launch: drop}, the ship drops in from above the top edge (Level 15's transport bay). */
    public static final String LAUNCH_DROP = "drop";

    /** M7 part B: whether the launch drops the ship in from above ({@code launch: drop}); false if not given. */
    public boolean dropLaunch() {
        return launch.filter(LAUNCH_DROP::equals).isPresent();
    }

    /**
     * M7 part C: the row of the ground (the walls' rows, {@link TerrainBlock#row}) a ground placement's centre lies on:
     * it enters at the top edge at {@code t} with its bottom edge there, so its centre is {@code height / 2} px above
     * the row at the top edge then.
     */
    public double placementRow(double t, double height) {
        return TerrainBlock.row(scrollAt(t), PlayField.HEIGHT + height / 2);
    }

    /** M8 part A: the level's medium, {@code air} if not given. */
    public Medium levelMedium() {
        return medium.orElse(Medium.AIR);
    }

    /** M8 part A: the medium of section {@code index}: its own, else the level's. */
    public Medium mediumOf(int index) {
        return sections.get(index).medium().orElse(levelMedium());
    }

    /** M8 part A: whether any of the level is under water (the level's medium or a section's). */
    public boolean underwater() {
        return levelMedium() == Medium.UNDERWATER
                || sections.stream().anyMatch(section -> section.medium().orElse(null) == Medium.UNDERWATER);
    }

    /**
     * M8 part A: the special limits of section {@code index} (specials by slug), its own, else the level's; empty when
     * neither gives them (the setting's list applies: {@link SpecialsData#forbidden}).
     */
    public Optional<List<String>> specialLimitsOf(int index) {
        return sections.get(index).specialLimits().or(() -> specialLimits);
    }

    /**
     * M8 part A: the specials (by name) forbidden in section {@code index}: its special limits, else its setting's
     * (its medium and the level's {@code water}).
     */
    public List<String> forbiddenSpecials(int index, SpecialsData specials) {
        return specials.forbidden(mediumOf(index), water.orElse(false), undergroundOf(index), specialLimitsOf(index));
    }

    /**
     * M9 part A (design/player/specials, Act 5 specials): whether section {@code index} lies inside rock (the setting
     * {@code underground}): its own {@code underground}, else the level's; false when neither gives it.
     */
    public boolean undergroundOf(int index) {
        return sections.get(index).underground().or(() -> underground).orElse(false);
    }

    /**
     * M9 part A: the setting of section {@code index} (design/player/specials, Setting restrictions): underground,
     * under water, dry or none (above water).
     */
    public Optional<SpecialsData.Setting> settingOf(int index) {
        return SpecialsData.Setting.of(mediumOf(index), water.orElse(false), undergroundOf(index));
    }

    /**
     * M9 part A: the HUD's reason word for {@code special} where section {@code index} forbids it: the reason of its
     * setting's limit when that lists it ({@code IN ROCK} inside the asteroid, {@code UNDER ICE} under water), else the
     * reason of the limit that lists it ({@link SpecialsData#reason(String)}); empty where it is not forbidden.
     */
    public Optional<String> forbiddenReason(int index, SpecialsData specials, String special) {
        if (!forbiddenSpecials(index, specials).contains(special)) {
            return Optional.empty();
        }
        return specials.reason(special, settingOf(index)).or(() -> specials.reason(special));
    }

    /**
     * M8 part C: the waterlines, the level times at which the medium changes: the start of each section whose medium
     * differs from the one before it (none at the start: a level may begin under water). Level 23's descent at 25.5 s.
     */
    public List<Double> waterlines() {
        List<Double> times = new java.util.ArrayList<>();
        Medium before = mediumOf(0);
        for (int i = 1; i < sections.size(); i++) {
            Medium now = mediumOf(i);
            if (now != before) {
                times.add(sectionStart(i));
            }
            before = now;
        }
        return List.copyOf(times);
    }

    /**
     * M8 part C (the {@code waterline} radio event, gap check D5): the level time of the first waterline, where the
     * event's cue plays as a timed line.
     */
    public Optional<Double> firstWaterline() {
        return waterlines().stream().findFirst();
    }

    /**
     * M8 part C: the under-water look of section {@code index}: the level's {@code underwater_look} with the section's
     * changes; none on a section in the air or without the level's block.
     */
    public Optional<UnderwaterLook> underwaterLookOf(int index) {
        if (mediumOf(index) != Medium.UNDERWATER) {
            return Optional.empty();
        }
        return underwaterLook.map(
                look -> sections.get(index).underwaterLook().map(look::with).orElse(look));
    }

    /** M7 part B: the no-surface zones; none if not given. */
    public List<NoSurface> noSurfaceZones() {
        return noSurface.orElse(List.of());
    }

    /** M7 part D: the ground allies' units (Level 17's domes) in the order the data lists them; none if not given. */
    public List<GroundAlly> groundAllies() {
        return allies.map(GroundAllies::units).orElse(List.of());
    }

    /** M5 part E: whether the whole play field is water, its {@code water}; false if not given. */
    public boolean overWater() {
        return water.orElse(false);
    }

    /** M5 part G: {@code water_splash: all}, every burst on a water level is the water splash. */
    public static final String WATER_SPLASH_ALL = "all";

    /** M5 part G: {@code water_splash: naval-only}, only a burst on a naval unit and its death (Level 13). */
    public static final String WATER_SPLASH_NAVAL = "naval-only";

    /**
     * M5 part G (user decision G5 = a): whether only a burst on a naval unit (and a naval unit's death)
     * plays the water splash on this water level, every other burst the ground burst (Level 13's ice).
     */
    public boolean navalSplashOnly() {
        return waterSplash.filter(WATER_SPLASH_NAVAL::equals).isPresent();
    }

    /** M5 part G: the pickups released at a point in the field (the data's {@code drops}), in time order. */
    public List<Drop> released() {
        return pointDrops.orElse(List.of());
    }

    /** M5 part H: the level's root bursts, in time order; none if not given. */
    public List<RootBurst> bursts() {
        return rootBursts.map(RootBursts::bursts).orElse(List.of());
    }

    /** M5 part F: the level's drop factor, its {@code drop_factor}; 1 if not given. */
    public double drops() {
        return dropFactor.orElse(1.0);
    }

    /** The factor on every bounty paid in the level: its {@code bounty_scale}, 1 if not given. */
    public double bounties() {
        return bountyScale.orElse(1.0);
    }

    /**
     * Round 45 B.a: the seconds the radio holds cue {@code cue} after its event ({@link RadioCue#delaySeconds()}),
     * indexed as the simulation's script radio ({@code script().radio()}: the level's cues first, in order, then the
     * secrets' lines, which wait none); 0 without a delay.
     */
    public double radioDelay(int cue) {
        return cue < radio.size() ? radio.get(cue).delaySeconds() : 0;
    }

    /**
     * Step FIX6 (Level 33's leech line): the level second the radio holds cue {@code cue} until after its event ({@link
     * RadioCue#notBeforeSeconds()}), indexed as {@link #radioDelay}; 0 without one.
     */
    public double radioNotBefore(int cue) {
        return cue < radio.size() ? radio.get(cue).notBeforeSeconds() : 0;
    }

    /** The level's length in seconds: the end of the last section. */
    public double seconds() {
        return sections.getLast().end();
    }

    /** When the longest outro ends and the debrief takes over: the last moment the backdrop is on screen. */
    public double outroEnd() {
        return seconds() + OUTRO_SECONDS;
    }

    /** The time section {@code index} (0-based) starts. */
    public double sectionStart(int index) {
        return index == 0 ? 0 : sections.get(index - 1).end();
    }

    /**
     * M7 part G (design/campaign Level 20, Reverse scroll; M5 = a): the direction of travel at {@code t}: +1 while the
     * ground scrolls down the screen (or stands), −1 while it scrolls up (a reversed section, through its ease). At
     * the turnabout's zero the sign just after it.
     */
    public int travelAt(double t) {
        return scrollAt(t + TRAVEL_PROBE) < scrollAt(t) ? -1 : 1;
    }

    /** M7 part G: s ahead {@link #travelAt} looks for the ground's direction. */
    private static final double TRAVEL_PROBE = 1e-3;

    /** M7 part G: whether a section scrolls backwards (a negative speed: Level 20's retreat). */
    public boolean hasReversal() {
        return sections.stream().anyMatch(Section::reversed);
    }

    /**
     * The ground layer's scroll distance at {@code t} seconds, px; before the start and after the
     * end the first and the last section's speed carry on. M5 part G: around a timed halt (a section of
     * speed 0 without an arena) the scroll eases down to 0 over the halt's {@code ramp} before it and
     * back up over as long after it (smoothstep, as the simulation's {@code Sortie.timedHaltSpeed}, here
     * integrated exactly: within a pixel of the simulation's sum over its steps).
     */
    public double scrollAt(double t) {
        return LevelScroll.scrollAt(sections, scrollSpeed, t);
    }

    /**
     * Where the centre of a placed set piece lies on its layer: px from the screen's bottom edge
     * at the level start, so that it passes the middle of the screen at its time. M7 part G: a piece placed by its
     * strip row lies at that row less the strip's origin (on the ground layer).
     */
    public double pieceCentre(BackdropData.PlacedPiece placed) {
        if (placed.byRow()) {
            // M7 part G: a strip's ground piece by its centre's strip row (the row at the bottom edge at the start:
            // origin).
            return placed.row().orElseThrow()
                    - backdrop.strip().map(BackdropData.Strip::origin).orElse(0.0);
        }
        BackdropLayer layer = backdrop.pieces().get(placed.piece()).layer();
        return scrollAt(placed.t()) * backdrop.factor(layer) + PlayField.HEIGHT / 2.0;
    }

    /**
     * Where section {@code index}'s tile set begins on {@code layer} (px from the screen's bottom
     * edge at the level start): it enters at the top edge when the section starts; the first
     * section's reaches back without end.
     */
    public double seam(BackdropLayer layer, int index) {
        return index == 0
                ? Double.NEGATIVE_INFINITY
                : scrollAt(sectionStart(index)) * backdrop.factor(layer) + PlayField.HEIGHT;
    }

    /** The atmosphere along the level: every section's, split around its peak, in time order. */
    public List<Stretch> atmosphereStretches() {
        return atmosphereStretches(false);
    }

    /**
     * As {@link #atmosphereStretches()}; on {@code easy} a peak with an {@code easy} length is that long,
     * centred on its authored stretch (M5 part G: Level 13's whiteout, 6 s on easy; presentation only).
     */
    public List<Stretch> atmosphereStretches(boolean easy) {
        return atmosphereStretches(sections, easy);
    }

    /** As {@link #atmosphereStretches(boolean)} for back-to-back {@code sections} from t = 0. */
    public static List<Stretch> atmosphereStretches(List<Section> sections, boolean easy) {
        return LevelScroll.atmosphereStretches(sections, easy);
    }

    /** The 1-based number of the section that is running at {@code t}, or 0 after the level end. */
    public int sectionAt(double t) {
        for (int i = 0; i < sections.size(); i++) {
            if (t < sections.get(i).end()) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * The collapse's band: its tower's footprint along the scroll, centred where the placed tower's
     * footprint lies on the ground.
     */
    public Optional<Band> collapseBand() {
        return collapse.map(c -> {
            BackdropData.PlacedPiece placed = backdrop.placements().stream()
                    .filter(p -> p.piece().equals(c.tower()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("collapse.tower: '" + c.tower() + "' is not placed"));
            double centre = pieceCentre(placed);
            double half = backdrop.pieces().get(c.tower()).size().height() / 2;
            return new Band(centre - half, centre + half, placed.x());
        });
    }

    public enum Entry {
        FRONT,
        SIDES,
        REAR,
        /** M5 part G: a {@code cross} wave's, from all four edges (its units' lanes say which). */
        ALL
    }

    /**
     * M7 part E, presentation (design/campaign Level 18, Visibility in the storm; M6 = a, MA.2 = b): the dust storm's
     * clear radius round the ship. {@code radii} names the radii, px ({@code deep} 170, {@code wall} 150 at medium);
     * {@code ramps} ease the radius by level time, each from the radius before it (full view before the first) to its
     * {@code radius} (a name of {@code radii} or {@link #FULL}) over {@code [from, to]} s (smoothstep); {@code
     * falloff} is the soft ragged edge's width, px, centred on the radius (broken by the look's drifting noise
     * fields); {@code easy} and {@code hard} give the radii on those difficulties instead (by name). A look only:
     * nothing in the simulation changes (ground and {@code low-air} drawn only inside it, {@code air} fading to
     * silhouettes outside it, bullets, telegraphs, pickups and the HUD always drawn).
     */
    public record Visibility(
            Map<String, Double> radii,
            List<Ramp> ramps,
            double falloff,
            Optional<Map<String, Double>> easy,
            Optional<Map<String, Double>> hard) {
        /** The full view's name in a ramp: no radius. */
        public static final String FULL = "full";

        public Visibility {
            radii = Map.copyOf(radii);
            ramps = List.copyOf(ramps);
            easy = easy.map(Map::copyOf);
            hard = hard.map(Map::copyOf);
            Check.that(!radii.isEmpty(), "radii: at least one radius");
            Check.that(!radii.containsKey(FULL), "radii: '" + FULL + "' is the full view, not a radius");
            radii.forEach((name, radius) -> Check.positive("radii." + name, radius));
            Check.positive("falloff", falloff);
            Check.notEmpty("ramps", ramps);
            Map<String, Double> named = radii;
            for (int i = 0; i < ramps.size(); i++) {
                Ramp ramp = ramps.get(i);
                Check.that(
                        ramp.radius().equals(FULL) || named.containsKey(ramp.radius()),
                        "ramps[" + i + "].radius: one of radii or '" + FULL + "', was '" + ramp.radius() + "'");
                Check.that(
                        i == 0 || ramp.from() >= ramps.get(i - 1).to(),
                        "ramps[" + i + "]: after the ramp before it, in time order, not overlapping");
            }
            for (var change : List.of(easy, hard)) {
                change.ifPresent(map -> map.forEach((name, radius) -> {
                    Check.that(named.containsKey(name), "easy/hard: '" + name + "' is not one of radii");
                    Check.positive(name, radius);
                }));
            }
        }

        /** One ease of the radius: to {@code radius} (a name, or {@link #FULL}) over {@code [from, to]} s. */
        public record Ramp(double from, double to, String radius) {
            public Ramp {
                Check.notNegative("from", from);
                Check.that(to > from, "to: after from");
            }
        }

        /**
         * The full view as a radius, px: the play field's diagonal plus the falloff, so that no point of the field
         * lies in the edge.
         */
        public double fullRadius() {
            return Math.hypot(PlayField.WIDTH, PlayField.HEIGHT) + falloff;
        }

        /** The radius named {@code name} on {@code difficulty}, px ({@link #FULL}: {@link #fullRadius()}). */
        public double radius(String name, Difficulty difficulty) {
            if (name.equals(FULL)) {
                return fullRadius();
            }
            Optional<Map<String, Double>> change =
                    switch (difficulty) {
                        case EASY -> easy;
                        case HARD -> hard;
                        case MEDIUM -> Optional.empty();
                    };
            return change.map(map -> map.get(name)).orElse(radii.get(name));
        }

        /** The radius at level time {@code t} at medium, px ({@link #radiusAt(double, Difficulty)}). */
        public double radiusAt(double t) {
            return radiusAt(t, Difficulty.MEDIUM);
        }

        /**
         * The radius at level time {@code t} on {@code difficulty}, px: the full view ({@link #fullRadius()}) before
         * the first ramp, each ramp easing (smoothstep) from the radius before it to its own.
         */
        public double radiusAt(double t, Difficulty difficulty) {
            double radius = fullRadius();
            for (Ramp ramp : ramps) {
                double target = radius(ramp.radius(), difficulty);
                if (t <= ramp.from()) {
                    return radius;
                }
                if (t < ramp.to()) {
                    double x = (t - ramp.from()) / (ramp.to() - ramp.from());
                    return radius + (target - radius) * x * x * (3 - 2 * x);
                }
                radius = target;
            }
            return radius;
        }

        /** The smallest radius on {@code difficulty}, px. */
        public double smallest(Difficulty difficulty) {
            return radii.keySet().stream()
                    .mapToDouble(name -> radius(name, difficulty))
                    .min()
                    .orElseThrow();
        }
    }

    /**
     * The music cues.
     *
     * @param track the track number of design/audio/music
     * @param startSection the section the music starts in (ambience only before)
     * @param startDb the theme's level through its start section, dB (full when absent); it rises
     *     to full at the next section
     * @param fullSection the section from which all stems play (the base stem before)
     * @param stems sections that override that: {@code base} drops to the base stem (Level 03's
     *     first Leviathan pass), {@code full} forces every stem on
     * @param bossSting the sting that cuts in over the level track when the boss arrives
     *     ({@code miniboss-sting}, Level 05), the track returning after it
     * @param ambienceFrom the level time from which only the ambience plays: the theme fades out
     *     (Level 06's last seconds)
     * @param voiceLoop a radio line looping faintly under a section (Level 06's perimeter beacon)
     * @param bossWarning part G: the warning track (by file name, {@code boss-warning}: track 22 with
     *     the klaxon and the warning banner) when an act boss arrives, the theme crossfading out
     * @param bossTrack part G: the boss theme (by file name, {@code choir-descends}: track 18) that
     *     comes in after the warning and fades out at the kill, leaving the ambience
     * @param fullOn M5 part C: run-time events that play the full mix: {@code hold} while a hold zone
     *     runs (fading in over 1 s as it starts, out over 4 s after it ends), {@code collapse} from
     *     the collapse to the level's end
     * @param duck M5 part D: the theme's duck on a run-time event (Level 10's scripted loss)
     * @param ambienceChanges M5 part D: the ambience crossfading to another at a section's start
     * @param ambienceLayers M7 part G: second ambience loops over the level's own for a while (Level 20's basin
     *     rumble from t=12, faded out by t=40)
     * @param sectionDb M5 part F: the theme's level by section (1-based section: dB, at most 0), from that
     *     section until the next entry, over a 1 s ramp (Level 12's eye at -6); full where none is given
     * @param jingle M5 part G (user decision G8 = a): the {@code end_jingle} plays inside the level at
     *     {@code jingle.t} instead of at the debrief (Level 13's "Grid sync complete"): the theme fades
     *     out over {@code fade} s (1 if not given), the jingle plays once, then the ambience alone; the
     *     debrief then starts without it
     */
    public record Music(
            int track,
            int startSection,
            Optional<Double> startDb,
            int fullSection,
            Optional<Map<Integer, Stems>> stems,
            String ambience,
            String endJingle,
            Optional<String> bossSting,
            Optional<Double> ambienceFrom,
            Optional<VoiceLoop> voiceLoop,
            Optional<String> bossWarning,
            Optional<String> bossTrack,
            Optional<List<FullOn>> fullOn,
            Optional<Duck> duck,
            Optional<List<AmbienceChange>> ambienceChanges,
            Optional<Map<Integer, Double>> sectionDb,
            Optional<Jingle> jingle,
            Optional<Motif> motif,
            Optional<List<AmbienceLayer>> ambienceLayers,
            Optional<List<StemTime>> stemTimes,
            Optional<MotifAfterBoss> motifAfterBoss) {

        /** Without M8 part H's motif after the boss. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges,
                Optional<Map<Integer, Double>> sectionDb,
                Optional<Jingle> jingle,
                Optional<Motif> motif,
                Optional<List<AmbienceLayer>> ambienceLayers,
                Optional<List<StemTime>> stemTimes) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    sectionDb,
                    jingle,
                    motif,
                    ambienceLayers,
                    stemTimes,
                    Optional.empty());
        }

        /**
         * M8 part H (design/campaign Level 28, Music &amp; ambience: "the run under Vorne's motif ... out over the
         * orbit"; m8h-gaps.md §1.6, the post-boss key): after an act boss's kill the theme, which stays out after its
         * boss music, comes back as its motif stem alone (the stems silent, the timed stem changes and the section
         * levels no longer applied) from level time {@code from} to {@code to}, fading in over its first {@code
         * fade_in} s and out over its last {@code fade_out} s; silent outside it. The level clock resumes after the
         * arena's hold, so {@code from} lies after the arena.
         */
        public record MotifAfterBoss(double from, double to, double fadeIn, double fadeOut) {
            public MotifAfterBoss {
                Check.notNegative("from", from);
                Check.that(to > from, "motif_after_boss.to: after from");
                Check.notNegative("fade_in", fadeIn);
                Check.notNegative("fade_out", fadeOut);
                Check.that(fadeIn + fadeOut <= to - from, "motif_after_boss: fade_in + fade_out within from-to");
            }

            /** Its level at level time {@code t}, 0–1. */
            public double level(double t) {
                if (t < from || t >= to) {
                    return 0;
                }
                double in = fadeIn > 0 ? Math.min(1, (t - from) / fadeIn) : 1;
                double out = fadeOut > 0 ? Math.min(1, (to - t) / fadeOut) : 1;
                return Math.min(in, out);
            }
        }

        /** Without M8 part F's timed stems. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges,
                Optional<Map<Integer, Double>> sectionDb,
                Optional<Jingle> jingle,
                Optional<Motif> motif,
                Optional<List<AmbienceLayer>> ambienceLayers) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    sectionDb,
                    jingle,
                    motif,
                    ambienceLayers,
                    Optional.empty(),
                    Optional.empty());
        }

        /**
         * M8 part F (design/campaign Level 26, Music &amp; ambience; design/audio/music, track 10's dark stem and warm pad;
         * m8f-gaps.md §1.5, §5 F12): a change of the theme at level time {@code t}, eased over {@code fade} s: {@code
         * stems} what plays from then on ({@code full} or {@code base}: the theme's stems; another name, {@code dark} or
         * {@code warm}: that extra stem of the theme, {@code <theme>-<name>.ogg} on the same grid, alone through the
         * third lockstep stream (Level 19's machinery), the theme's stems out; {@code none}: the theme silent, the
         * ambience alone) and {@code db} its level from then on (at most 0); each change gives at least one of them.
         */
        public record StemTime(double t, Optional<String> stems, Optional<Double> db, double fade) {
            /** The theme silent from then on, the ambience alone. */
            public static final String NONE = "none";

            public StemTime {
                Check.notNegative("t", t);
                Check.notNegative("fade", fade);
                Check.that(stems.isPresent() || db.isPresent(), "stem_times: a change gives its stems or its db");
                stems.ifPresent(name -> Check.that(
                        name.matches("[a-z][a-z0-9-]*"), "stem_times: stems a name in lower case, was '" + name + "'"));
                db.ifPresent(level -> Check.that(level <= 0, "stem_times: db at most 0, was " + level));
            }

            /** Whether it plays an extra stem of the theme alone (not {@code full}, {@code base} or {@code none}). */
            public boolean extraStem() {
                return stems.filter(name -> !name.equals("full") && !name.equals("base") && !name.equals(NONE))
                        .isPresent();
            }
        }

        /** M8 part F: the timed stem changes in time order, none if not given. */
        public List<StemTime> stemTimeList() {
            return stemTimes.orElse(List.of());
        }

        /** Without M7 part G's ambience layers. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges,
                Optional<Map<Integer, Double>> sectionDb,
                Optional<Jingle> jingle,
                Optional<Motif> motif) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    sectionDb,
                    jingle,
                    motif,
                    Optional.empty());
        }

        /**
         * M7 part G, presentation (design/campaign Level 20, Music &amp; ambience: the basin rumble): a second ambience
         * loop over the level's own from level time {@code from} to {@code to}, fading in over its first {@code
         * fade_in} s and out over its last {@code fade_out} s.
         */
        public record AmbienceLayer(String ambience, double from, double to, double fadeIn, double fadeOut) {
            public AmbienceLayer {
                Check.that(!ambience.isEmpty(), "ambience: an ambience key");
                Check.notNegative("from", from);
                Check.that(to > from, "to: after from");
                Check.notNegative("fade_in", fadeIn);
                Check.notNegative("fade_out", fadeOut);
                Check.that(fadeIn + fadeOut <= to - from, "fade_in + fade_out: within from-to");
            }

            /** Its level at level time {@code t}: 0 outside it, 0–1 over its fades (linear), 1 between them. */
            public double level(double t) {
                if (t <= from || t >= to) {
                    return 0;
                }
                double in = fadeIn > 0 ? Math.min(1, (t - from) / fadeIn) : 1;
                double out = fadeOut > 0 ? Math.min(1, (to - t) / fadeOut) : 1;
                return Math.min(in, out);
            }
        }

        /**
         * M7 part E (design/campaign Level 18, Music; track 9's motif stem): the motif layer plays over the theme from
         * level time {@code from} on, in every section the theme plays except those {@code sections} turns off
         * ({@code {4: false}}: Level 18's eye); a section listed {@code true} plays it as well. M9 part G (design/campaign
         * Level 34, Music &amp; ambience): {@code to}, the level time it goes out again (Level 34's window t=106-124 over
         * the freighter's hold, ending inside a section); to the end of the level if not given.
         */
        public record Motif(double from, Map<Integer, Boolean> sections, Optional<Double> to) {
            public Motif {
                Check.notNegative("from", from);
                sections = Map.copyOf(sections);
                sections.keySet()
                        .forEach(section -> Check.that(section >= 1, "sections: count from 1, was " + section));
                to.ifPresent(end -> Check.that(end > from, "motif.to: after from (" + from + "), was " + end));
            }

            /** Without M9 part G's end time: the motif plays to the end of the level. */
            public Motif(double from, Map<Integer, Boolean> sections) {
                this(from, sections, Optional.empty());
            }

            /** Whether the motif plays at level time {@code t} in section {@code section} (1-based). */
            public boolean on(int section, double t) {
                return t >= from && to.map(end -> t < end).orElse(true) && sections.getOrDefault(section, true);
            }
        }

        /** Without M7 part E's motif layer. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges,
                Optional<Map<Integer, Double>> sectionDb,
                Optional<Jingle> jingle) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    sectionDb,
                    jingle,
                    Optional.empty());
        }

        /** M7 part E: whether the motif layer plays at level time {@code t} in section {@code section}. */
        public boolean motifOn(int section, double t) {
            return motif.map(m -> m.on(section, t)).orElse(false);
        }

        /** M5 part G: the jingle inside the level at {@code t}, the theme fading out over {@code fade} s first. */
        public record Jingle(double t, Optional<Double> fade) {
            public Jingle {
                Check.notNegative("t", t);
                fade.ifPresent(f -> Check.positive("fade", f));
            }

            /** The theme's fade before it, s: its {@code fade}, 1 if not given. */
            public double fadeSeconds() {
                return fade.orElse(1.0);
            }
        }

        /** Without M5 part G's mid-level jingle. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges,
                Optional<Map<Integer, Double>> sectionDb) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    sectionDb,
                    Optional.empty(),
                    Optional.empty());
        }

        /** M5 part G: whether the debrief plays the end jingle (not when it played inside the level). */
        public boolean debriefJingle() {
            return jingle.isEmpty();
        }

        /**
         * M5 part D: on the event {@code on} the theme ducks by {@code db} for {@code seconds}, no sting;
         * with the radio's duck the lower applies (they do not add up).
         */
        public record Duck(DuckOn on, double db, double seconds) {
            public Duck {
                Check.that(db < 0, "db: a duck lowers the theme, below 0, was " + db);
                Check.positive("seconds", seconds);
            }
        }

        /** M5 part D: the run-time events a duck can follow. */
        public enum DuckOn {
            @JsonProperty("scripted-loss")
            SCRIPTED_LOSS
        }

        /** M5 part D: at section {@code section}'s start (1-based) the ambience crossfades to {@code ambience} over {@code crossfade} s. */
        public record AmbienceChange(int section, String ambience, double crossfade) {
            public AmbienceChange {
                Check.that(section >= 2, "section: a later section than the first, was " + section);
                Check.that(!ambience.isEmpty(), "ambience: an ambience key");
                Check.positive("crossfade", crossfade);
            }
        }

        /** Without M5 part F's section levels. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn,
                Optional<Duck> duck,
                Optional<List<AmbienceChange>> ambienceChanges) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    duck,
                    ambienceChanges,
                    Optional.empty());
        }

        /**
         * M5 part F: the theme's level in section {@code section} (1-based), dB: the last {@code
         * section_db} entry at or before it; 0 (full) without one.
         */
        public double sectionLevel(int section) {
            double db = 0;
            int best = 0;
            for (Map.Entry<Integer, Double> entry : sectionDb.orElse(Map.of()).entrySet()) {
                if (entry.getKey() <= section && entry.getKey() > best) {
                    best = entry.getKey();
                    db = entry.getValue();
                }
            }
            return db;
        }

        /** Without M5 part D's duck and ambience changes. */
        public Music(
                int track,
                int startSection,
                Optional<Double> startDb,
                int fullSection,
                Optional<Map<Integer, Stems>> stems,
                String ambience,
                String endJingle,
                Optional<String> bossSting,
                Optional<Double> ambienceFrom,
                Optional<VoiceLoop> voiceLoop,
                Optional<String> bossWarning,
                Optional<String> bossTrack,
                Optional<List<FullOn>> fullOn) {
            this(
                    track,
                    startSection,
                    startDb,
                    fullSection,
                    stems,
                    ambience,
                    endJingle,
                    bossSting,
                    ambienceFrom,
                    voiceLoop,
                    bossWarning,
                    bossTrack,
                    fullOn,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty());
        }

        /** The ambience changes, in section order; empty for none. */
        public List<AmbienceChange> ambienceChangeList() {
            return ambienceChanges.orElse(List.of());
        }

        /**
         * The timed radio line of {@code speaker} (its first), looping at {@code db} (below full)
         * while section {@code section} (1-based) plays; silent while the speaker has no voice file.
         */
        public record VoiceLoop(String speaker, int section, double db) {
            public VoiceLoop {
                Check.positive("section", section);
                Check.that(db <= 0, "db: must not be above full level");
            }
        }

        /** M5 part C: a run-time event that plays the full mix ({@code full_on}). */
        public enum FullOn {
            @JsonProperty("hold")
            HOLD,
            @JsonProperty("collapse")
            COLLAPSE
        }

        /** Whether {@code event} plays the full mix (its {@code full_on} names it). */
        public boolean fullOn(FullOn event) {
            return fullOn.map(events -> events.contains(event)).orElse(false);
        }

        /** The stems a section plays. */
        public enum Stems {
            @JsonProperty("base")
            BASE,
            @JsonProperty("full")
            FULL,
            /**
             * M7 part F (design/campaign Level 19, the wreck crawl): the theme's stems silent, the motif layer alone
             * over the ambience.
             */
            @JsonProperty("motif")
            MOTIF
        }

        /** M7 part F: whether section {@code section} (1-based) plays the motif layer alone ({@code stems: motif}). */
        public boolean motifAlone(int section) {
            return stems.map(map -> map.get(section)).orElse(null) == Stems.MOTIF;
        }

        /** Whether section {@code section} (1-based) plays every stem. */
        public boolean full(int section) {
            Stems override = stems.map(map -> map.get(section)).orElse(null);
            return override != null ? override == Stems.FULL : section >= fullSection;
        }

        public Music {
            ambienceLayers = ambienceLayers.map(List::copyOf);
            stemTimes = stemTimes.map(List::copyOf);
            stemTimes.ifPresent(times -> {
                Check.notEmpty("stem_times", times);
                for (int i = 1; i < times.size(); i++) {
                    Check.that(
                            times.get(i).t()
                                    >= times.get(i - 1).t() + times.get(i - 1).fade(),
                            "stem_times[" + i + "]: in time order, each after the fade before it");
                }
            });
            Check.that(startSection <= fullSection, "full_section must not come before start_section");
            startDb.ifPresent(db -> Check.that(db <= 0, "start_db: must not be above full level"));
            ambienceFrom.ifPresent(t -> Check.positive("ambience_from", t));
            Check.that(bossTrack.isEmpty() || bossWarning.isPresent(), "a boss_track comes in after its boss_warning");
            Check.that(
                    motifAfterBoss.isEmpty() || bossTrack.isPresent(),
                    "motif_after_boss: the theme comes back after an act boss's music (a boss_track)");
            fullOn.ifPresent(events -> {
                Check.notEmpty("full_on", events);
                Check.that(events.size() == new java.util.HashSet<>(events).size(), "full_on: each event once");
            });
            sectionDb = sectionDb.map(Map::copyOf);
            sectionDb.ifPresent(levels -> {
                Check.that(!levels.isEmpty(), "section_db: at least one section");
                levels.forEach((section, db) -> {
                    Check.that(section >= 1, "section_db: sections count from 1, was " + section);
                    Check.that(db <= 0, "section_db: must not be above full level, was " + db);
                });
            });
            ambienceChanges = ambienceChanges.map(List::copyOf);
            ambienceChanges.ifPresent(changes -> {
                Check.notEmpty("ambience_changes", changes);
                for (int i = 1; i < changes.size(); i++) {
                    Check.that(
                            changes.get(i).section() > changes.get(i - 1).section(),
                            "ambience_changes: in section order, one per section");
                }
            });
        }
    }

    /** The level-wide changes on easy and hard. */
    public record Difficulties(Optional<Variant> easy, Optional<Variant> hard) {}

    /** Changes to enemies (by slug) and extra placed pickups. */
    public record Variant(Optional<Map<String, EnemyChange>> enemies, Optional<List<PlacedPickup>> extraPickups) {}

    /**
     * Aimed shots fired in bursts of {@code burst}; walking at {@code speedFactor} times its speed; M8 part H: a segment
     * chain of {@code segments} segments instead of its stat block's (a coiled boss's shed Eel Swarms; {@link
     * MawRules}).
     */
    public record EnemyChange(Optional<Integer> burst, Optional<Double> speedFactor, Optional<Integer> segments) {
        public EnemyChange {
            burst.ifPresent(b -> Check.positive("burst", b));
            speedFactor.ifPresent(f -> Check.positive("speed_factor", f));
            segments.ifPresent(n -> Check.positive("segments", n));
        }

        /** A burst and a speed change. */
        public EnemyChange(Optional<Integer> burst, Optional<Double> speedFactor) {
            this(burst, speedFactor, Optional.empty());
        }

        /** Only a burst change. */
        public EnemyChange(Optional<Integer> burst) {
            this(burst, Optional.empty());
        }
    }
}
