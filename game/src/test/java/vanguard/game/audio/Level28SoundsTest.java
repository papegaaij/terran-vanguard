package vanguard.game.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.audio.mock.MockAudio;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import vanguard.content.Content;
import vanguard.content.Difficulty;
import vanguard.content.LevelData;
import vanguard.content.SimSpecs;
import vanguard.game.render.IceShelfFixture;
import vanguard.game.render.MawFixture;
import vanguard.game.render.SiegeUnitsFixture;
import vanguard.game.screen.LevelScreen;
import vanguard.game.settings.AudioSettings;
import vanguard.sim.LevelScript;
import vanguard.sim.Section;
import vanguard.sim.SimEvents;
import vanguard.sim.SimStep;
import vanguard.sim.Sortie;

/**
 * M8 part H's Level 28 sounds and music (design/audio/sfx and design/audio/music, Level 28's rows; design/campaign
 * Level 28, Music &amp; ambience; round 58's a variants as stand-ins until the user's pick): the hatchery's bed, the
 * caldera's rumble over it from the antechamber, held under the fight and cut at the kill, Europa's night ice from the
 * breakout; the Maw's arrival, gill breath, throat charge, sweep groan and swing, shed, dive, lunge, implant spark and
 * zap and death; the shaft's climb, the breakout, the cruisers' rise, broadside, flak and point defence; track 11 silent
 * before t=16 (the motif-alone section 1 plays the motif from its cue), the base stem from section 2, the full mix from
 * section 3, out at 80; track 18 dropping away under the dive and back after the lunge; the reveal sting (track 30) at
 * the cruisers' rise; the run under the motif stem alone.
 */
class Level28SoundsTest {
    private static final Path ASSETS = Path.of(System.getProperty("vanguard.assetsDir", "../assets"));
    private static final long NANOS_PER_STEP = 1_000_000_000L / SimStep.PER_SECOND;
    private static final Content CONTENT = MawFixture.CONTENT;
    private static final LevelData LEVEL = CONTENT.level(MawFixture.LEVEL);
    private final long[] now = {0};
    private final SfxBank bank =
            new SfxBank(new MockAudio(), new Lwjgl3Files(), new Mixer(AudioSettings.defaults()), () -> now[0]);

    private static final List<Sfx> SOUNDS = List.of(
            Sfx.AMBIENCE_EUROPA_HATCHERY,
            Sfx.AMBIENCE_CALDERA_RUMBLE,
            Sfx.SHAFT_CLIMB,
            Sfx.MAW_ARRIVAL,
            Sfx.MAW_GILL,
            Sfx.MAW_THROAT,
            Sfx.MAW_SWEEP_GROAN,
            Sfx.MAW_SWEEP_SWING,
            Sfx.MAW_SHED,
            Sfx.MAW_DIVE,
            Sfx.MAW_LUNGE,
            Sfx.IMPLANT_SPARK,
            Sfx.IMPLANT_ZAP,
            Sfx.MAW_DEATH,
            Sfx.BREAKOUT,
            Sfx.CRUISER_RISE,
            Sfx.CRUISER_BROADSIDE,
            Sfx.CRUISER_FLAK,
            Sfx.CRUISER_PD);

    @Test
    void everySoundHasItsFileRoundFiftyEightsAAsAStandIn() {
        assertEquals(19, SOUNDS.size(), "AUD-H's 19 subjects");
        for (Sfx sfx : SOUNDS) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(sfx.path())), sfx.path());
            assertTrue(sfx.path().endsWith("-r58-a.ogg"), "round 58's a until the pick: " + sfx.path());
        }
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_EUROPA_HATCHERY.priority());
        assertEquals(Sfx.Priority.AMBIENCE, Sfx.AMBIENCE_CALDERA_RUMBLE.priority());
        assertTrue(Files.isRegularFile(ASSETS.resolve(Sfx.REVEAL_STING.path())), "track 30");
        assertEquals(Bus.MUSIC, Sfx.REVEAL_STING.bus());
        assertTrue(Files.isRegularFile(ASSETS.resolve(Tracks.motifPath("pressure-line"))), "track 11's motif stem");
    }

    /** The beds: the hatchery's to the breakout, the caldera's rumble over it from t=80, the night ice from section 7. */
    @Test
    void theHatcheryBedTheCalderasRumbleAndTheNightIce() {
        LevelData.Music music = LEVEL.music();
        assertEquals(Sfx.AMBIENCE_EUROPA_HATCHERY, Sfx.ambience(music.ambience()));
        LevelData.Music.AmbienceLayer rumble =
                music.ambienceLayers().orElseThrow().getFirst();
        assertEquals(Sfx.AMBIENCE_CALDERA_RUMBLE, Sfx.ambience(rumble.ambience()));
        assertEquals(0, rumble.level(79), 1e-9);
        assertEquals(1, rumble.level(95), 1e-9, "swelled in through the antechamber");
        assertEquals(1, rumble.level(104.5), 1e-9);
        assertEquals(0, rumble.level(105), 1e-9, "out once the clock resumes after the kill");
        assertEquals(7, music.ambienceChangeList().getFirst().section());
        assertEquals(
                Sfx.AMBIENCE_EUROPA_ICE,
                Sfx.ambience(music.ambienceChangeList().getFirst().ambience()));

        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic level = withBoss(audio, files, mixer, sfx)
                .ambienceLayers(List.of(new LevelMusic.AmbienceLayer(Sfx.AMBIENCE_CALDERA_RUMBLE, rumble::level)));
        level.update(4, 100, 0.1f, false);
        assertTrue(sfx.looping(Sfx.AMBIENCE_CALDERA_RUMBLE), "the rumble in the antechamber");
        assertTrue(level.bossArrived());
        level.update(5, 104.98, 0.1f, false);
        assertTrue(level.layerLevel(0) > 0.99f, "held under the fight at the clock's halt");
        level.bossDown();
        level.update(5, 104.98, 0.1f, false);
        assertEquals(0, level.layerLevel(0), "cut at the kill");
        assertFalse(sfx.looping(Sfx.AMBIENCE_CALDERA_RUMBLE));
        level.dispose();
        sfx.dispose();
    }

    /**
     * Track 11 from section 1 with section 1 the motif alone: silent before the Hunters' guard at t=16 (the stems out
     * from the theme's first sample, the motif off until its cue), the motif alone from 16, the base stem from section
     * 2, the full mix from section 3, out at 80 (−40 dB over 6 s) into the antechamber's near-silence.
     */
    @Test
    void theThemeIsSilentBeforeSixteenThenTheMotifTheBaseAndTheFullMix() {
        LevelData.Music data = LEVEL.music();
        assertEquals(11, data.track(), "Act 4 B, Pressure Line");
        assertEquals(1, data.startSection());
        assertTrue(data.motifAlone(1));
        assertFalse(data.full(2));
        assertTrue(data.full(3));
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music = theme(audio, files, mixer, sfx, Optional.empty());
        music.update(1, 5, 0.1f, false);
        assertFalse(music.stemsOn(), "no stems before t=16");
        assertFalse(music.motifOn(), "no motif before t=16");
        music.update(1, 16.05, 0.1f, false);
        assertFalse(music.stemsOn());
        assertTrue(music.motifOn(), "the motif from the Hunters' guard");
        music.update(2, 30, 0.1f, false);
        assertTrue(music.stemsOn() && music.motifOn(), "the base stem with the motif");
        music.update(3, 60, 0.1f, false);
        assertTrue(music.stemsOn());
        double t = 80;
        while (t < 86.5) {
            music.update(4, t, 0.1f, false);
            t += 0.1;
        }
        assertTrue(music.timedGain() < 0.02f, "out into the antechamber: " + music.timedGain());
        music.dispose();
        sfx.dispose();

        // The stems start silent where a section asks for the motif alone: nothing but the motif from the first sample.
        StemMix mix = new StemMix(
                new Constant((short) 1000, 100), new Constant((short) 3000, 100), new Constant((short) 400, 100), 1);
        mix.motif(false);
        mix.stems(false, 0);
        short[] out = new short[50];
        mix.read(out);
        for (short sample : out) {
            assertEquals(0, sample, "silent from the first sample");
        }
    }

    /**
     * The fight: track 18 drops away over the dive's 2 s slide and comes back over 2 s after the lunge; at the kill it
     * fades and the theme stays out until the run, which plays its motif stem alone (in from 126 over 3 s, out over the
     * orbit 157–162); the reveal sting plays once at the cruisers' rise, and a restart forgets it.
     */
    @Test
    void theBossThemeDropsUnderTheDiveAndTheRunPlaysTheMotifAlone() {
        LevelData.Music.MotifAfterBoss run = LEVEL.music().motifAfterBoss().orElseThrow();
        assertEquals(126, run.from(), 1e-9);
        assertEquals(162, run.to(), 1e-9, "to the level's end");
        assertEquals(LEVEL.seconds(), run.to(), 1e-9);
        MockAudio audio = new MockAudio();
        Lwjgl3Files files = new Lwjgl3Files();
        Mixer mixer = new Mixer(AudioSettings.defaults());
        SfxBank sfx = new SfxBank(audio, files, mixer);
        LevelMusic music =
                theme(audio, files, mixer, sfx, Optional.of(boss(files))).motifAfterBoss(run);
        music.update(5, 104, 0.1f, false);
        assertTrue(music.bossArrived());
        music.bossDrop(true);
        for (int i = 0; i < 10; i++) {
            music.update(5, 104.98, 0.1f, false);
        }
        assertEquals(0.5f, music.bossDropGain(), 0.01f, "half way down the slide");
        for (int i = 0; i < 11; i++) {
            music.update(5, 104.98, 0.1f, false);
        }
        assertEquals(0, music.bossDropGain(), "the ambience alone in the dark");
        music.bossDrop(false);
        for (int i = 0; i < 21; i++) {
            music.update(5, 104.98, 0.1f, false);
        }
        assertEquals(1, music.bossDropGain(), "back after the lunge");
        music.bossDown();
        music.update(6, 110, 0.1f, false);
        assertTrue(music.afterMode());
        assertEquals(0, music.afterLevel(), "the climb and the rise under the ambience alone");
        music.reveal();
        music.reveal();
        assertEquals(1, sfx.playing(Sfx.REVEAL_STING), "the sting once at the rise");
        music.update(9, 127.5, 0.1f, false);
        assertEquals(0.5f, music.afterLevel(), 1e-4f, "the motif comes in under the sting's tail");
        assertTrue(music.motifOn() && !music.stemsOn(), "the motif alone");
        music.update(9, 140, 0.1f, false);
        assertEquals(1, music.afterLevel(), 1e-6f);
        music.update(10, 159.5, 0.1f, false);
        assertEquals(0.5f, music.afterLevel(), 1e-4f, "out over the orbit");
        music.update(10, 162, 0.1f, false);
        assertEquals(0, music.afterLevel());
        music.restart();
        assertFalse(music.revealed(), "a restart plays the sting again at its rise");
        assertFalse(music.afterMode());
        music.dispose();
        sfx.dispose();
    }

    /** Each coil and cruiser event plays its round 58 sound. */
    @Test
    void theEventsPlayTheirSounds() {
        MawCues cues = new MawCues(new SoundMix(bank));
        assertEquals(
                EnumSet.of(
                        SimEvents.Type.SAC_SWELL,
                        SimEvents.Type.THROAT_GLOW,
                        SimEvents.Type.COIL_SWEEP_TELL,
                        SimEvents.Type.COIL_SWEEP,
                        SimEvents.Type.MAW_DIVE,
                        SimEvents.Type.LUNGE,
                        SimEvents.Type.IMPLANT_SPARK,
                        SimEvents.Type.IMPLANT_ARC,
                        SimEvents.Type.SHED_TELL,
                        SimEvents.Type.CRUISER_RISE,
                        SimEvents.Type.BROADSIDE,
                        SimEvents.Type.POINT_DEFENCE),
                cues.types());
        Set<Sfx> played = EnumSet.noneOf(Sfx.class);
        for (SimEvents.Type type : cues.types()) {
            played.add(MawCues.sound(type));
        }
        assertEquals(cues.types().size(), played.size(), "a sound of its own each");
        assertEquals(Sfx.MAW_SWEEP_GROAN, MawCues.sound(SimEvents.Type.COIL_SWEEP_TELL));
        assertEquals(Sfx.IMPLANT_ZAP, MawCues.sound(SimEvents.Type.IMPLANT_ARC));
        assertTrue(MawCues.defenceDue(Long.MIN_VALUE / 2, 0));
        assertFalse(MawCues.defenceDue(0, MawCues.PD_GAP_STEPS - 1), "the burst's next shot");
        assertTrue(MawCues.defenceDue(0, MawCues.PD_GAP_STEPS));
    }

    /**
     * Flown through the Maw's arena (the fixture: the starter loadout, invulnerable): its arrival roars after the
     * klaxon's first blast, a sac's swell breathes, the throat charges, a shed tears as its tell ends, a sweep groans
     * and swings; the coil does not dive in this stretch (the music's drop follows the dive's states only).
     */
    @Test
    void theMawsArenaSounds() {
        Sortie sortie = MawFixture.sortie();
        FlightSounds sounds = new FlightSounds(
                bank,
                SiegeUnitsFixture.quietLooks(sortie),
                sortie.armament(),
                List.of("abyssal-maw"),
                Optional.empty());
        assertEquals(
                SimStep.ticks(MawFixture.maw(sortie)
                        .boss()
                        .flatMap(boss -> boss.coil())
                        .flatMap(coil -> coil.shed())
                        .orElseThrow()
                        .tellSeconds()),
                MawCues.shedSteps(sortie));
        boolean roar = false;
        boolean gill = false;
        boolean throat = false;
        boolean shed = false;
        boolean groan = false;
        boolean swing = false;
        int sheds = 0;
        for (int i = 0; i < SimStep.ticks(150) && !(groan && swing && shed); i++) {
            int tears = bank.playing(Sfx.MAW_SHED);
            step(sortie, sounds, MawFixture.attack(sortie));
            roar |= bank.playing(Sfx.MAW_ARRIVAL) > 0;
            sheds += Math.max(0, bank.playing(Sfx.MAW_SHED) - tears);
            shed = sheds > 0;
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                switch (events.type(e)) {
                    case SAC_SWELL -> gill |= bank.playing(Sfx.MAW_GILL) > 0;
                    case THROAT_GLOW -> throat |= bank.playing(Sfx.MAW_THROAT) > 0;
                    case COIL_SWEEP_TELL -> groan |= bank.playing(Sfx.MAW_SWEEP_GROAN) > 0;
                    case COIL_SWEEP -> swing |= bank.playing(Sfx.MAW_SWEEP_SWING) > 0;
                    default -> {}
                }
            }
            assertEquals(
                    MawFixture.coil(sortie).diveState() != vanguard.sim.CoilArena.DiveState.READY
                            && MawFixture.coil(sortie).diveState() != vanguard.sim.CoilArena.DiveState.DONE
                            && MawFixture.coil(sortie).diveState() != vanguard.sim.CoilArena.DiveState.BODY
                            && MawFixture.coil(sortie).diveState() != vanguard.sim.CoilArena.DiveState.RETURN,
                    LevelScreen.coilDiving(sortie));
        }
        assertTrue(roar, "the arrival roar");
        assertTrue(gill, "a sac's breath");
        assertTrue(throat, "the throat's charge");
        assertTrue(shed, "a shed's tear");
        assertTrue(groan, "a sweep's groan");
        assertTrue(swing, "a sweep's swing");
    }

    /**
     * The finale flown on Level 28's own clock with the approach and the Maw left out (its arena a plain section): the
     * breakout as the medium turns to air at t=115, the cruisers' rise at 118, their broadsides, a point-defence burst
     * once a burst (not once a shot) and the flak's airbursts instead of a mortar's splat; the shaft's climb follows
     * the kill's hold only (none without a kill).
     */
    @Test
    void theFinalesBreakoutCruisersAndTheirFire() {
        LevelScript real = SimSpecs.level(CONTENT, MawFixture.LEVEL, Difficulty.MEDIUM);
        List<Section> sections = real.sections().stream()
                .map(section -> section.arena()
                        ? new Section(section.end(), 100)
                        : new Section(
                                section.end(), section.speed(), false, section.rampSeconds(), section.easeSeconds()))
                .toList();
        LevelScript finale = real.toBuilder()
                .sections(sections)
                .waves(List.of())
                .groundUnits(List.of())
                .setPieces(List.of())
                .radio(List.of())
                .armourFloor(0)
                .build();
        Sortie sortie = new Sortie(
                1,
                SimSpecs.starterLoadout(CONTENT, Difficulty.MEDIUM),
                finale,
                SimSpecs.rules(CONTENT, MawFixture.LEVEL, Difficulty.MEDIUM).withInvulnerableShip(),
                60);
        FlightSounds sounds = new FlightSounds(
                bank, SiegeUnitsFixture.quietLooks(sortie), sortie.armament(), List.of(), Optional.empty());
        int breakouts = 0;
        int rises = 0;
        int broadsides = 0;
        int defenceShots = 0;
        int defenceBursts = 0;
        int flak = 0;
        int splats = 0;
        double breakoutAt = Double.NaN;
        while (!sortie.complete() && sortie.levelSeconds() < 161) {
            int before = bank.playing(Sfx.BREAKOUT);
            int beforeRise = bank.playing(Sfx.CRUISER_RISE);
            int beforeBroadside = bank.playing(Sfx.CRUISER_BROADSIDE);
            int beforePd = bank.playing(Sfx.CRUISER_PD);
            int beforeFlak = bank.playing(Sfx.CRUISER_FLAK);
            int beforeSplat = bank.playing(Sfx.MORTAR_IMPACT);
            step(sortie, sounds, IceShelfFixture.toward(sortie, 240, 120));
            if (bank.playing(Sfx.BREAKOUT) > before) {
                breakouts++;
                breakoutAt = sortie.levelSeconds();
            }
            rises += Math.max(0, bank.playing(Sfx.CRUISER_RISE) - beforeRise);
            broadsides += Math.max(0, bank.playing(Sfx.CRUISER_BROADSIDE) - beforeBroadside);
            defenceBursts += Math.max(0, bank.playing(Sfx.CRUISER_PD) - beforePd);
            flak += Math.max(0, bank.playing(Sfx.CRUISER_FLAK) - beforeFlak);
            splats += Math.max(0, bank.playing(Sfx.MORTAR_IMPACT) - beforeSplat);
            SimEvents events = sortie.events();
            for (int e = 0; e < events.size(); e++) {
                if (events.type(e) == SimEvents.Type.POINT_DEFENCE) {
                    defenceShots++;
                }
            }
            assertFalse(bank.playing(Sfx.SHAFT_CLIMB) > 0, "no climb without the kill");
        }
        assertEquals(1, breakouts, "one breakout");
        assertEquals(115, breakoutAt, 0.1, "at the first section in the air");
        assertEquals(1, rises, "the cruisers rise once");
        assertTrue(broadsides > 5, "broadsides: " + broadsides);
        assertTrue(defenceBursts > 5 && defenceShots >= 3 * defenceBursts - 2, defenceBursts + " of " + defenceShots);
        assertTrue(flak > 3, "flak bursts: " + flak);
        assertEquals(0, splats, "no mortar's splat in the run");
    }

    /** As the level screen builds it (track 11's stems, its motif and timed stems from the data). */
    private static LevelMusic theme(
            MockAudio audio, Lwjgl3Files files, Mixer mixer, SfxBank sfx, Optional<LevelMusic.Boss> boss) {
        LevelData.Music data = LEVEL.music();
        return new LevelMusic(
                        audio,
                        mixer,
                        files.internal(Tracks.basePath("pressure-line")),
                        files.internal(Tracks.path("pressure-line")),
                        sfx,
                        Sfx.ambience(data.ambience()),
                        data.startSection(),
                        0,
                        data::full,
                        Double.POSITIVE_INFINITY,
                        Optional.empty(),
                        boss)
                .motif(files.internal(Tracks.motifPath("pressure-line")), data::motifOn)
                .motifAlone(data::motifAlone)
                .stemTimes(
                        data.stemTimeList(),
                        stem -> files.absolute(ASSETS.resolve(Tracks.stemPath("pressure-line", stem))
                                .toAbsolutePath()
                                .toString()));
    }

    private static LevelMusic.Boss boss(Lwjgl3Files files) {
        return new LevelMusic.Boss(
                Optional.of(files.internal(Tracks.path(Tracks.BOSS_WARNING))),
                Optional.of(files.internal(Tracks.path(Tracks.BOSS_VRELL))),
                Tracks.BOSS_WARNING_BARS_SECONDS);
    }

    private static LevelMusic withBoss(MockAudio audio, Lwjgl3Files files, Mixer mixer, SfxBank sfx) {
        return theme(audio, files, mixer, sfx, Optional.of(boss(files)));
    }

    private record Constant(short value, int sampleRate) implements PcmStream {
        @Override
        public void read(short[] out) {
            java.util.Arrays.fill(out, value);
        }

        @Override
        public int channels() {
            return 1;
        }

        @Override
        public void close() {}
    }

    private void step(Sortie sortie, FlightSounds sounds, int commands) {
        now[0] += NANOS_PER_STEP;
        sortie.step(commands);
        sounds.step();
        sounds.play(sortie.events());
        sounds.watch(sortie);
    }
}
