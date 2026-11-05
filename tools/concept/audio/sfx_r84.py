#!/usr/bin/env python3
"""Concept round 84 (M11 "Act 7 - Beyond the Gate" design phase, step AUD-R, 2026-11-04): the first half of the Act 7
sounds (design/audio/sfx/README.md, *Act 7 sounds (draft)*), a and b each: the shared jump set (jump out, arrive), the
Gate Warden's window shell (grind, tick), telegraph, beam and death, the Rift Skater's burst, the Husk's drift, crack,
shards and death, the Choir Seraph's mirroring, arrival, spiral tick and leaving tell, the gate transit and the warp
home, and the reversal's tell (synthesized: a UI signal). Step AUD-R2 (2026-11-05) added the second half: Vorne's
Chimera (rise, pain, rear sweep, capsule, burst), the Choir Heart (heartbeat, membrane, closing, the Heart's line, node
cut, opening, death), the echoes' forming and dissolving shimmers, the Level 43-50 set pieces and hazards (glyph stones,
spore clouds, spawning pits and organs, the call organ, seed caskets, the warm hull, reactor cores, the closing gate),
the `event-plane` bed (a seamless 16 s loop) and the reversal groan, the Swarm Call and its drones' burst, and the
secret pods' layers (the memory pearl, the archive, the seed vault). A layer's speed of three values glides down and
back through the middle one (the reversal groan's bend).

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, cut, mix, levels, the --docs rows). Every
layer is a Freesound sound (CC0 or CC-BY) taken from its public HQ preview (cdn.freesound.org, cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no download quota used). The
recordings are only cut, slowed, glided, layered, swelled, faded, filtered, looped, reversed and levelled. No bubbly or
popping sources; nothing wet. A layer marked `rev` plays the whole sound backwards (the arrive and the warp home are
their counterparts reversed, one file each way).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each is).
Levels like round 72's (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the loops by
their band RMS): the two variants of a subject share the level.

Usage: python3 tools/concept/audio/sfx_r84.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r84.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, onsets and the loop seams.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from sfx_r43g import L  # noqa: E402
from synth import SR, adsr, sine  # noqa: E402

CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    105281: ('Stein Aufschlag mit langem Decay 04.wav', 'mbezzola', CC0, P + '105/105281_537412-hq.ogg'),
    146421: ('Frozen Lake Ice and Water Sounds (+12.0dB) Omni Part 1.wav', 'Andrew5DMII', BY3, P + '146/146421_1392098-hq.ogg'),
    150454: ('Crystal bowl F#3 1.wav', 'caiogracco', CC0, P + '150/150454_2703701-hq.ogg'),
    203281: ('EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV', 'metrostock99', CC0, P + '203/203281_1256155-hq.ogg'),
    222521: ('Cracking Earthquake (cracking soil, cracking stone)', 'uagadugu', CC0, P + '222/222521_255863-hq.ogg'),
    249549: ('glass_break2', 'FngerSounds', CC0, P + '249/249549_4149794-hq.ogg'),
    251464: ('twig snap classic.wav', 'martian', CC0, P + '251/251464_84709-hq.ogg'),
    322411: ('Twig Snap 04.wav', 'Glitchedtones', CC0, P + '322/322411_3294528-hq.ogg'),
    369870: ('Plastic Quartz clock ticking.wav', 'SpliceSound', CC0, P + '369/369870_1480854-hq.ogg'),
    372206: ('drone8_bowed_cymbal2.wav', 'decembered', CC0, P + '372/372206_454894-hq.ogg'),
    390704: ('Breaking Glass 16.mp3', 'FunWithSound', BY4, P + '390/390704_6456158-hq.ogg'),
    448070: ('23cm glass bowl - tone - wooden spoon 01.wav', 'Anthousai', CC0, P + '448/448070_5923045-hq.ogg'),
    455757: ('door wood old slow creaks open close or ship hull list2.flac', 'kyles', CC0, P + '455/455757_612689-hq.ogg'),
    475133: ('FX - Swoosh - Slow', 'bolkmar', CC0, P + '475/475133_2927958-hq.ogg'),
    485277: ('Wooden Stick Swing 1_2', 'Joao_Janz', CC0, P + '485/485277_9961300-hq.ogg'),
    530354: ('Stone.wav', 'danielpodlovics', CC0, P + '530/530354_10106687-hq.ogg'),
    542582: ('Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3', 'adr1911', CC0, P + '542/542582_10366986-hq.ogg'),
    567249: ('Bricks/Stones/Rocks/Gravel Falling', 'iwanPlays', CC0, P + '567/567249_7108319-hq.ogg'),
    572753: ('Knocking into Pile of Large Stones 1.wav', 'F.M.Audio', BY4, P + '572/572753_7805242-hq.ogg'),
    573805: ('Singing Bowl, long without reverb', 'hollandm', CC0, P + '573/573805_5309408-hq.ogg'),
    578491: ('Heavy stone door opens 2', 'PostProdDog', CC0, P + '578/578491_6911631-hq.ogg'),
    608677: ('Small Crack', 'eben-frostey', CC0, P + '608/608677_8905624-hq.ogg'),
    616335: ('Singing Bowl - Soft Stick Meditative Hit', 'Rimych', CC0, P + '616/616335_11045601-hq.ogg'),
    675480: ('S37-25 Car body crushed by machine; leaks and moans.wav', 'craigsmith', CC0, P + '675/675480_2524442-hq.ogg'),
    675760: ('S16-18 Tesla coil sparking and humming.wav', 'craigsmith', CC0, P + '675/675760_2524442-hq.ogg'),
    730943: ('Soda Can (Crushing)', 'SoundBiterSFX', CC0, P + '730/730943_15857333-hq.ogg'),
    737896: ('Ice - Lake fractures', 'Vrymaa', CC0, P + '737/737896_13973196-hq.ogg'),
    738784: ('Heavy Stone Scraping, Being Dragged', 'CheatinSloth', CC0, P + '738/738784_13230621-hq.ogg'),
    740198: ('Zapping - High Voltage Electricity Buzz Isolated', 'el_boss', CC0, P + '740/740198_9129912-hq.ogg'),
    790019: ('Foley_Object_Clock_Old_Tick_Sequence_Stereo', 'Nox_Sound', CC0, P + '790/790019_9250976-hq.ogg'),
    797583: ('Glass Harp - Singing Vibration', 'Vrymaa', CC0, P + '797/797583_13973196-hq.ogg'),
    818918: ('frozen lake', 'mentos987', CC0, P + '818/818918_15684938-hq.ogg'),
    849934: ('High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo', 'Hear_it_use_it', BY4, P + '849/849934_18634554-hq.ogg'),
    # step AUD-R2 (the bosses, the echoes, the set pieces and hazards, the event-plane bed, the Swarm Call, the pods)
    65420: ('1240_walnut_crack.WAV', 'reinsamba', BY4, P + '65/65420_18799-hq.ogg'),
    78974: ('Conch.wav', 'RoofDog', CC0, P + '78/78974_1227539-hq.ogg'),
    121193: ('crack nut.wav', 'gusgus26', CC0, P + '121/121193_2147722-hq.ogg'),
    136494: ('FOL_IMPACT_DROP_COCONUT_HIT_RODENTG3_JACKMENHORN.wav', 'JackMenhorn', BY4, P + '136/136494_154322-hq.ogg'),
    161650: ('snapping-chain', 'CosmicEmbers', BY3, P + '161/161650_2895542-hq.ogg'),
    169946: ('choir vocalising 01.wav', 'klankbeeld', BY4, P + '169/169946_1648170-hq.ogg'),
    179338: ('Dry Leaves Rustling.wav', 'lolamadeus', CC0, P + '179/179338_544580-hq.ogg'),
    191497: ('Creaking pier', 'morosopher', CC0, P + '191/191497_3226149-hq.ogg'),
    217796: ('Door slam far.aiff', 'kd_jack', CC0, P + '217/217796_1560047-hq.ogg'),
    248250: ('shortzap2.wav', 'jameswrowles', BY4, P + '248/248250_3874107-hq.ogg'),
    317058: ('Cymbal_Swell_010.wav', 'FiatLuxx', CC0, P + '317/317058_2486193-hq.ogg'),
    317059: ('Cymbal_Swell_009.wav', 'FiatLuxx', CC0, P + '317/317059_2486193-hq.ogg'),
    326303: ('sandfall1.wav', 'Wagna', CC0, P + '326/326303_3271346-hq.ogg'),
    337668: ('String tightens and whips.wav', 'romeoplank', CC0, P + '337/337668_2932059-hq.ogg'),
    351614: ('Rope Tightening Venice Water Bus Parking.mp3', 'stujun', CC0, P + '351/351614_5252281-hq.ogg'),
    354110: ('Sandpaper, paintsticks.wav', 'This_Is_A_Hoax', CC0, P + '354/354110_6570179-hq.ogg'),
    376926: ('Ticking_Explosion_Debris_Metal Groan.wav', 'Cell31_Sound_Productions', BY4, P + '376/376926_3571756-hq.ogg'),
    399080: ('Clay Pottery Drop ’n’ Break', 'Kinoton', CC0, P + '399/399080_2247456-hq.ogg'),
    410698: ('Symphonic_Concert_Bass_Drum_Vel32.wav', 'pjcohen', BY4, P + '410/410698_394391-hq.ogg'),
    410705: ('Symphonic_Concert_Bass_Drum_Vel07.wav', 'pjcohen', BY4, P + '410/410705_394391-hq.ogg'),
    411087: ('creaking sailing boat.wav', 'jimsim', BY4, P + '411/411087_130385-hq.ogg'),
    415201: ('Crackling Fire (Crushing Bark).mp3', 'Yin_Yang_Jake007', CC0, P + '415/415201_7919598-hq.ogg'),
    434101: ('Wind Turbine Whine 2.wav', 'DeVern', CC0, P + '434/434101_2866779-hq.ogg'),
    450791: ('door slam distant roomy boom.wav', 'kyles', CC0, P + '450/450791_612689-hq.ogg'),
    452586: ('crowbar hits with zing.wav', 'kyles', CC0, P + '452/452586_612689-hq.ogg'),
    453330: ('metal bender creaks clacks bending creaks harder.flac', 'kyles', CC0, P + '453/453330_612689-hq.ogg'),
    453444: ('hum electric transformer in large room1.flac', 'kyles', CC0, P + '453/453444_612689-hq.ogg'),
    457294: ('Air (or steam) pressure release', 'brunoboselli', CC0, P + '457/457294_300738-hq.ogg'),
    461076: ('1.11_burning-grass.wav', '15GKovacovaJulie', CC0, P + '461/461076_9676039-hq.ogg'),
    471828: ('24_SeRompeCable.wav', 'Lextao', BY4, P + '471/471828_9306869-hq.ogg'),
    476851: ('Rainstick 39 sec.aiff', 'AncientOracle', CC0, P + '476/476851_8152631-hq.ogg'),
    479998: ('R12-46-Blowing Note on Cow Horn.wav', 'craigsmith', CC0, P + '479/479998_2524442-hq.ogg'),
    489903: ('RockCrack01', 'falcospizaetus', CC0, P + '489/489903_10570192-hq.ogg'),
    489904: ('RockCrack02', 'falcospizaetus', CC0, P + '489/489904_10570192-hq.ogg'),
    500143: ('liner mooring hawsers creak, gangplank groans.WAV', 'phonoflora', CC0, P + '500/500143_401348-hq.ogg'),
    527423: ('muffled kick.wav', 'Logicogonist', CC0, P + '527/527423_3797507-hq.ogg'),
    528853: ('crush dry leaves sound effect', 'Garuda1982', CC0, P + '528/528853_2061858-hq.ogg'),
    533385: ('Rattle_Hit_strong.wav', 'Cmart94', BY4, P + '533/533385_6829346-hq.ogg'),
    536187: ('Leather Creak / Stretching', 'IENBA', CC0, P + '536/536187_9616576-hq.ogg'),
    539973: ('Flint Strike', 'Za-Games', CC0, P + '539/539973_12029332-hq.ogg'),
    540336: ('Clay Jar Break', 'Clearwavsound', BY3, P + '540/540336_11502151-hq.ogg'),
    555745: ('Gas Pressure Release.wav', 'MGA95', BY4, P + '555/555745_11537559-hq.ogg'),
    559079: ('Foley_Leather_Stress_Mono.wav', 'Nox_Sound', CC0, P + '559/559079_9250976-hq.ogg'),
    573251: ('Massive Timpani 4.wav', 'Sorinious_Genious', CC0, P + '573/573251_12517458-hq.ogg'),
    591525: ('Cloth Rip Uneven Long', 'Rudmer_Rotteveel', CC0, P + '591/591525_4921277-hq.ogg'),
    596513: ('Rope Tightening.wav', 'EugeneEverett', CC0, P + '596/596513_10974703-hq.ogg'),
    610143: ('mg_SeedPods.wav', 'Michel1980', CC0, P + '610/610143_8578826-hq.ogg'),
    625399: ('Female singing Ooo', 'drotzruhn', BY4, P + '625/625399_6616150-hq.ogg'),
    640499: ('Eurofighter Typhoon Flyby 003 – Close Proximity', 'TimoSchmied', BY4, P + '640/640499_4424897-hq.ogg'),
    640503: ('Eurofighter Typhoon Flyby 007 – Close Proximity', 'TimoSchmied', BY4, P + '640/640503_4424897-hq.ogg'),
    648726: ('power transformer in basement.wav', 'itinerantmonk108', CC0, P + '648/648726_2397507-hq.ogg'),
    656529: ('Heavy Door Squeaking.wav', 'not_yr', CC0, P + '656/656529_8154780-hq.ogg'),
    674035: ('230104-044_normalized_ship_sail_rip_tear_1.wav', 'mmmtoop', CC0, P + '674/674035_11007330-hq.ogg'),
    675727: ('S28-19 Engine room; high whine from turbines.wav', 'craigsmith', CC0, P + '675/675727_2524442-hq.ogg'),
    685680: ('Humming in F# Minor', 'xkeril', CC0, P + '685/685680_13504080-hq.ogg'),
    697749: ('METLImpt_Metal Resonant Deep Impact With Rattle_SDFY_Metal R', 'SoundDesignForYou', CC0, P + '697/697749_13085623-hq.ogg'),
    697754: ('METLImpt_Metal Resonant Impact With Rattle_SDFY_Metal Resona', 'SoundDesignForYou', CC0, P + '697/697754_13085623-hq.ogg'),
    703248: ('Fall debris (crash)', 'xkeril', CC0, P + '703/703248_13504080-hq.ogg'),
    719003: ('Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast', 'ChristopherJngs', CC0, P + '719/719003_8154783-hq.ogg'),
    734549: ('Leather - Creak, heavy jacket', 'Vrymaa', CC0, P + '734/734549_13973196-hq.ogg'),
    734630: ('Leather - Rub & creaks', 'Vrymaa', CC0, P + '734/734630_13973196-hq.ogg'),
    752072: ('Metal_heavy_punch_with_crunch_and_glass_debris_sounds_082420', 'Artninja', BY4, P + '752/752072_10996917-hq.ogg'),
    767058: ('Metal gate opening and closing', 'ThabzMalik', CC0, P + '767/767058_15764451-hq.ogg'),
    771130: ('Rustling of dry leaves', 'SpinOpel', CC0, P + '771/771130_16303686-hq.ogg'),
    797919: ('LETHRCreak_Leather Stretching Creak 01_KVV AUDIO_FREE', 'KVV_Audio', BY4, P + '797/797919_12846320-hq.ogg'),
    810170: ('Heavy object drop', 'mokasza', BY4, P + '810/810170_17437502-hq.ogg'),
    835919: ('OBJCont-Samsung Galaxy Smartphone, CU_Toolbox, Unlatch, Open', 'designerschoice', BY4, P + '835/835919_6951162-hq.ogg'),
    844244: ('Electric Failure – High Voltage Power Down #2 ATP2 (CC0)', 'ATP2-kh', CC0, P + '844/844244_18042200-hq.ogg'),
    847733: ('Toolbox Big Drawer Open', 'j_soundeffects', BY4, P + '847/847733_14030247-hq.ogg'),
    851222: ('Jet fighter flyby over Bardenas Reales Spain', 'JoanCalsina', CC0, P + '851/851222_15370528-hq.ogg'),
    862894: ('Dry leaves rustle and break', 'lreeve', CC0, P + '862/862894_17191970-hq.ogg'),
}


def R(*args, **kw):
    """A layer of a sound played backwards as a whole (the first layer of a mix marks the whole mix)."""
    return dict(L(*args, **kw), rev=True)


def reversal_tell(v):
    """The reversal's tell: a soft rising two-tone, twice in 2 s on the chevron's 2 Hz grid (at 0 and 1.0 s).
    a: two sine tones a fifth apart (A5 then E6), 0.12 s each, soft attack; b: a softer triangle-ish minor third
    (D6 then F6) with a slow swell, 0.15 s each."""
    n = int(2.0 * SR)
    x = np.zeros(n)
    tones = ((880.0, 1318.5), 0.12) if v == "a" else ((1174.7, 1396.9), 0.15)
    (f1, f2), d = tones
    m = int(d * SR)
    for start in (0.0, 1.0):
        for k, f in enumerate((f1, f2)):
            a = int((start + k * d) * SR)
            env = adsr(m, 0.012 if v == "a" else 0.03, 0.04, 0.7, 0.04, gate=d * 0.85)[:m]
            tone = sine(f, m) + (0.25 * sine(3 * f, m) / 3 if v == "b" else 0.15 * sine(2 * f, m))
            x[a:a + m] += tone * env * (0.8 if k == 0 else 1.0)
    return np.vstack([x, x])


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}), as sfx_r72.py's SOUNDS
SOUNDS = {
    "jump-out": ("shot", -24.0, -6.0, -24.0,
                 "the shared `teleport` set: a Rift Skater's or a Choir Seraph's jump away (Levels 44-50, often)",
                 "a short dry air-rush: a stick or a cloth swung fast past the mic, cut short; never a pop or a bubble", {
        "a": ([L(485277, 0.0, 0.35, fin=0.002, fout=0.12, hp=200.0)],
              "a wooden stick swung past the mic: a thin, quick 'whff', 0.35 s"),
        "b": ([L(475133, 0.0, 0.4, speed=1.4, fin=0.002, fout=0.12, hp=150.0)],
              "a slow swoosh sped to 1.4: a fuller, breathier rush than a, 0.4 s"),
    }),
    "arrive": ("shot", -24.0, -6.0, -24.0,
               "the shared `teleport` set: the unit landing at its ring's end (the Seraph adds its glass shimmer)",
               "the jump's whoosh reversed: rising into the landing, one file", {
        "a": ([R(485277, 0.0, 0.35, fin=0.002, fout=0.12, hp=200.0)],
              "jump-out a played backwards: the stick's rush swelling into a cut-off landing, 0.35 s"),
        "b": ([R(475133, 0.0, 0.4, speed=1.4, fin=0.002, fout=0.12, hp=150.0)],
              "jump-out b played backwards: a breathier rise into the landing, 0.4 s"),
    }),
    "shell-grind": ("shot", -24.0, -5.0, -24.0,
                    "the Gate Warden's window shell opening and closing (Levels 43, 47, 50; its length the shell's "
                    "motion, about 1 s)",
                    "a slow stone grind: a heavy stone slab dragged over stone; never the hex shield's chime", {
        "a": ([L(738784, 5.0, 1.0, fin=0.15, fout=0.25, hp=60.0)],
              "a heavy stone dragged over stone: a rough, gritty scrape, 1.0 s"),
        "b": ([L(578491, 1.0, 1.0, speed=0.85, fin=0.15, fout=0.25, hp=50.0)],
              "a heavy stone door opening, slowed to 0.85: deeper and smoother than a, more rumble than grit, 1.0 s"),
    }),
    "shell-tick": ("shot", -28.0, -8.0, -28.0,
                   "a shot on the Gate Warden's closed shell (throttled: once per 0.15 s per unit); often in its "
                   "fights, under the fire",
                   "a dull stone tick: a small stone knocked on a slab; never the hex shield's chime", {
        "a": ([L(572753, 0.08, 0.25, speed=1.3, fin=0.001, fout=0.1, hp=150.0)],
              "a knock into a pile of large stones, sped to 1.3 (a smaller stone): a dull hard 'tok' with a little "
              "rattle, 0.25 s"),
        "b": ([L(530354, 0.0, 0.2, fin=0.001, fout=0.08, hp=150.0)],
              "a single stone struck: a drier, cleaner click than a, 0.2 s"),
    }),
    "warden-telegraph": ("shot", -26.0, -8.0, -26.0,
                         "the Gate Warden's 0.8 s beam telegraph (the thin crimson line, its eye glowing); the warning "
                         "the player must hear",
                         "a low resonant hum rising: a bowed metal plate or a rubbed singing bowl, its length the "
                         "telegraph's", {
        "a": ([L(573805, 30.0, 0.8, speed=(0.85, 1.15), fin=0.2, fout=0.05, hp=80.0, swell=(-14.0, 1.2))],
              "a rubbed singing bowl, glided up (0.85 → 1.15) and swelling: a pure, ringing hum rising, 0.8 s"),
        "b": ([L(372206, 3.0, 0.8, speed=(0.8, 1.2), fin=0.2, fout=0.05, hp=80.0, swell=(-14.0, 1.2))],
              "a bowed cymbal, glided up (0.8 → 1.2) and swelling: a rougher metallic drone rising, 0.8 s"),
    }),
    "warden-beam": ("shot", -22.0, -5.0, -22.0,
                    "the Gate Warden's beam (its 1.0 s burn after the telegraph); every 4 s per Warden",
                    "a sustained dry electric crackle: a high-voltage arc", {
        "a": ([L(675760, 10.0, 1.0, fin=0.01, fout=0.15, hp=150.0)],
              "a Tesla coil sparking: a dense crackle with a hum under it, 1.0 s"),
        "b": ([L(740198, 5.0, 1.0, fin=0.01, fout=0.15, hp=150.0)],
              "a high-voltage buzz: a smoother, steadier zap than a, less crackle, 1.0 s"),
    }),
    "warden-death": ("shot", -18.0, -3.0, -18.0,
                     "a Gate Warden's death, over the `medium` explosion rung (Levels 43, 47, 50)",
                     "a heavy rockfall: stones tumbling", {
        "a": ([L(567249, 0.0, 2.8, fin=0.005, fout=0.8, hp=50.0)],
              "bricks, stones and gravel falling: a tumbling pile, 2.8 s"),
        "b": ([L(222521, 0.4, 2.5, fin=0.01, fout=0.8, hp=40.0),
               L(105281, 0.3, 1.2, gain=-4.0, fin=0.002, fout=0.5, hp=60.0)],
              "stone cracking under an earthquake with one heavy stone impact on top: lower and more of a crack-and-"
              "rumble than a's tumble, 2.5 s"),
    }),
    "skater-burst": ("shot", -22.0, -5.0, -22.0,
                     "a Rift Skater's three-shot burst (Levels 44-50, often)",
                     "a dry triple snap: three twigs or a cap gun's cracks, close; never a pop", {
        "a": ([L(251464, 0.03, 0.15, fin=0.001, fout=0.06, hp=300.0),
               L(251464, 0.03, 0.15, speed=1.12, gain=-2.0, at=0.08, fin=0.001, fout=0.06, hp=300.0),
               L(251464, 0.03, 0.15, speed=0.92, gain=-3.0, at=0.16, fin=0.001, fout=0.06, hp=300.0)],
              "one twig snap three times, 80 ms apart, each a little higher or lower: crisp, light and woody, 0.3 s"),
        "b": ([L(322411, 0.08, 0.15, fin=0.001, fout=0.06, hp=300.0),
               L(322411, 0.08, 0.15, speed=1.1, gain=-2.0, at=0.07, fin=0.001, fout=0.06, hp=300.0),
               L(322411, 0.08, 0.15, speed=0.9, gain=-3.0, at=0.14, fin=0.001, fout=0.06, hp=300.0)],
              "another twig snap three times, 70 ms apart: thicker and more of a crack than a, 0.3 s"),
    }),
    "husk-drift": ("loop", -32.0, -10.0, -30.0,
                   "while a Husk drifts (one loop, the nearest Husk; Level 46)",
                   "a slow hull creak: stressed ice or old metal, slowed; no voice, no hum", {
        "a": (([L(455757, 3.2, 8.0, speed=0.6, hp=60.0)], 8.0, 1.0),
               "an old wooden door and a ship's hull creaking, slowed to 0.6: long, low groaning creaks, a seamless "
               "8 s loop"),
        "b": (([L(818918, 4.5, 8.0, speed=0.7, hp=60.0)], 8.0, 1.0),
               "a frozen lake's ice groaning, slowed to 0.7: deep booming moans with the ice's ring, a seamless 8 s "
               "loop"),
    }),
    "husk-crack": ("shot", -20.0, -4.0, -20.0,
                   "a Husk cracking (its tell before the shard fan; Level 46): the cue of the quiet's end",
                   "a real ice fracture: lake ice splitting", {
        "a": ([L(737896, 15.6, 1.0, fin=0.002, fout=0.4, hp=80.0)],
              "lake ice fracturing: a sharp split with the ice's twanging ring running off, 1.0 s"),
        "b": ([L(146421, 30.55, 1.0, fin=0.002, fout=0.4, hp=80.0)],
              "frozen lake ice cracking: a duller, deeper crack than a, less ring, 1.0 s"),
    }),
    "husk-shards": ("shot", -24.0, -6.0, -24.0,
                    "a Husk's shard fan (after the crack)",
                    "a short glassy splinter", {
        "a": ([L(608677, 0.15, 0.4, fin=0.001, fout=0.15, hp=400.0)],
              "a small glass crack: a tight, bright splinter, 0.4 s"),
        "b": ([L(390704, 0.15, 0.45, fin=0.001, fout=0.18, hp=400.0)],
              "glass breaking: a fuller splinter with a few shards falling, 0.45 s"),
    }),
    "husk-death": ("shot", -20.0, -4.0, -20.0,
                   "a Husk's death (Level 46), instead of its size rung's crack",
                   "a hollow collapse: a metal tank crumpling, slowed, with a faint glass tinkle", {
        "a": ([L(675480, 20.3, 2.0, speed=0.6, fin=0.01, fout=0.6, hp=40.0),
               L(249549, 1.0, 0.8, gain=-14.0, at=0.3, fin=0.002, fout=0.3, hp=2000.0)],
              "a car body crushed by a machine, slowed to 0.6, with a faint glass tinkle: a deep hollow metal crumple, "
              "2.0 s"),
        "b": ([L(730943, 8.5, 1.8, speed=0.4, fin=0.01, fout=0.6, hp=40.0),
               L(390704, 0.3, 0.5, gain=-14.0, at=0.25, fin=0.002, fout=0.25, hp=2000.0)],
              "a soda can crushed, slowed to 0.4 (a big thin tank), with a faint glass tinkle: crinklier and lighter "
              "than a, 1.8 s"),
    }),
    "seraph-mirror": ("loop", -32.0, -10.0, -30.0,
                      "while a Choir Seraph mirrors the ship (one loop, the nearest; pitched with its sideways speed; "
                      "Levels 47-50)",
                      "a bowed glass or singing-bowl tone", {
        "a": (([L(797583, 12.0, 4.0, hp=150.0)], 4.0, 0.8),
               "a glass harp's singing vibration: a pure, airy glass tone, a seamless 4 s loop"),
        "b": (([L(573805, 40.0, 4.0, speed=1.5, hp=150.0)], 4.0, 0.8),
               "a rubbed singing bowl sped to 1.5: a warmer, beating metal-glass ring, a seamless 4 s loop"),
    }),
    "seraph-arrive": ("shot", -24.0, -6.0, -24.0,
                      "a Choir Seraph arriving (under the arrive whoosh)",
                      "a rising glass shimmer: a wet-finger glass harp swept up (no water heard)", {
        "a": ([L(797583, 13.0, 1.0, speed=(0.8, 1.3), fin=0.3, fout=0.2, hp=200.0, swell=(-18.0, 1.0))],
              "the glass harp glided up (0.8 → 1.3) and swelling: a bright glassy rise, 1.0 s"),
        "b": ([L(573805, 50.0, 1.0, speed=(1.4, 2.2), fin=0.3, fout=0.2, hp=200.0, swell=(-18.0, 1.0))],
              "the singing bowl glided up high (1.4 → 2.2) and swelling: a thinner, beating shimmer, 1.0 s"),
    }),
    "seraph-spiral": ("shot", -32.0, -10.0, -32.0,
                      "each shot of a Choir Seraph's spiral (fast, quiet, under the fire)",
                      "a fast soft tick: a light clock tick, quiet", {
        "a": ([L(790019, 3.08, 0.12, fin=0.001, fout=0.05, hp=400.0)],
              "an old clock's tick: a woody, hollow tick, 0.12 s"),
        "b": ([L(369870, 8.98, 0.1, fin=0.001, fout=0.04, hp=400.0)],
              "a quartz clock's tick: a lighter plastic click than a, 0.1 s"),
    }),
    "seraph-leave": ("shot", -26.0, -8.0, -26.0,
                     "a Choir Seraph's leaving tell (its last fold before it goes; Level 47's secondary)",
                     "a short rising chime: the bowl struck softly, pitched up", {
        "a": ([L(150454, 0.0, 0.6, speed=1.6, fin=0.002, fout=0.3, hp=300.0)],
              "a crystal bowl struck, sped to 1.6: a clear high bell tone, 0.6 s"),
        "b": ([L(616335, 0.0, 0.8, speed=1.5, fin=0.002, fout=0.4, hp=300.0)],
              "a singing bowl's soft stick hit, sped to 1.5: rounder and longer than a, 0.8 s"),
    }),
    "gate-transit": ("shot", -16.0, -1.0, -16.0,
                     "the gate transit at Level 43's end: the event plane filling the field, then the 1.5 s warp; "
                     "the music fades under it; a big moment",
                     "a deep rushing pull rising into a held pressure: a real low roar (a jet engine's spool-up or a "
                     "large waterfall, dry, heard far) pitched down an octave and swelled; no flash of noise at the "
                     "peak", {
        "a": ([L(542582, 2.0, 3.5, speed=0.5, fin=0.5, fout=0.7, hp=30.0, swell=(-24.0, 1.5)),
               L(203281, 5.0, 3.5, gain=-6.0, fin=0.8, fout=0.7, hp=25.0, swell=(-18.0, 1.0))],
              "a tank's gas turbine spooling up, an octave down, swelling over a deep rumble: a rising mechanical "
              "roar into a held pressure, 3.5 s"),
        "b": ([L(849934, 80.0, 3.5, speed=0.5, fin=0.5, fout=0.7, hp=30.0, swell=(-24.0, 1.5)),
               L(203281, 5.0, 3.5, gain=-6.0, fin=0.8, fout=0.7, hp=25.0, swell=(-18.0, 1.0))],
              "a waterfall's massive roar, an octave down, swelling over the same rumble: a broader, noisier rush "
              "than a, 3.5 s"),
    }),
    "warp-home": ("shot", -16.0, -1.0, -16.0,
                  "Level 50's warp home (K+29): the transit reversed, under the coda's held chord",
                  "the transit's file reversed: the pressure releasing into a falling rush", {
        "a": ([R(542582, 2.0, 3.5, speed=0.5, fin=0.5, fout=0.7, hp=30.0, swell=(-24.0, 1.5)),
               L(203281, 5.0, 3.5, gain=-6.0, fin=0.8, fout=0.7, hp=25.0, swell=(-18.0, 1.0))],
              "gate-transit a played backwards: the held roar falling away, 3.5 s"),
        "b": ([R(849934, 80.0, 3.5, speed=0.5, fin=0.5, fout=0.7, hp=30.0, swell=(-24.0, 1.5)),
               L(203281, 5.0, 3.5, gain=-6.0, fin=0.8, fout=0.7, hp=25.0, swell=(-18.0, 1.0))],
              "gate-transit b played backwards: the waterfall's rush falling away, 3.5 s"),
    }),
    "reversal-tell": ("synth", -28.0, -8.0, -28.0,
                      "with the HUD chevron, 2 s ahead of each reversal (Level 44, the Heart's phase 2), under Rook's "
                      "call",
                      "a soft rising two-tone, twice in the 2 s on the chevron's 2 Hz rhythm, quiet; never the hostile "
                      "edge warning's tone", {
        "a": (None, "two soft sine tones a fifth apart (A5 then E6), 0.12 s each, twice: a plain, clear signal, 2 s"),
        "b": (None, "a softer minor third (D6 then F6) with a slower attack, 0.15 s each, twice: rounder and higher "
                    "than a, 2 s"),
    }),
}
# ---- step AUD-R2: the second half (the bosses, the echoes, the Level 43-50 set pieces and hazards, the event-plane
# bed and the reversal groan, the Swarm Call and the pods). A speed of three values glides through the middle one
# (down and back: the reversal groan's bend).
Lp = M.Loop
CHIMERA = "Level 48, Vorne's Chimera (the mid-boss): "
HEART = "Level 50, the Choir Heart (the final boss): "
SOUNDS.update({
    "chimera-rise": ("shot", -18.0, -3.0, -18.0,
                     CHIMERA + "the spire well splitting and the Chimera rising, once (the fight's start)",
                     "a deep tearing groan of thick hide, leather stretched to tearing, with a strained engine whine "
                     "under it; mechanical, then organic, never wet", {
        "a": ([L(797919, 5.5, 2.4, speed=0.5, fin=0.3, fout=0.4, hp=40.0),
               L(719003, 0.35, 0.9, speed=0.5, gain=-3.0, at=1.8, fin=0.01, fout=0.4, hp=80.0),
               L(675727, 3.0, 2.8, speed=(0.7, 1.0), gain=-10.0, fin=0.8, fout=0.4, hp=150.0, swell=(-18.0, 1.0))],
              "a leather stretching creak slowed to half (a deep groan) that tears at the end (cloth torn, slowed), "
              "a turbine's whine gliding up under it: groan, then rip, a thin strained whine, 2.8 s"),
        "b": ([L(734549, 23.5, 2.4, speed=0.45, fin=0.3, fout=0.4, hp=40.0),
               L(674035, 1.45, 1.0, speed=0.6, gain=-3.0, at=1.8, fin=0.01, fout=0.4, hp=80.0),
               L(434101, 5.5, 3.0, speed=1.2, gain=-8.0, fin=0.6, fout=0.4, hp=120.0)],
              "a heavy leather jacket's creak slowed below half, a sail rip slowed at the end, a wind turbine's "
              "rising whine under it: rougher and lower than a, the whine louder and more mechanical, 3.0 s"),
    }),
    "chimera-pain": ("shot", -20.0, -4.0, -20.0,
                     CHIMERA + "phase 2's start: the fusion's pain, once",
                     "metal plates buckling and tearing over a long dry creak (stretched leather, slowed); never wet", {
        "a": ([L(453330, 9.8, 2.5, speed=0.7, fin=0.05, fout=0.8, hp=60.0),
               L(559079, 17.0, 2.5, speed=0.5, gain=-4.0, fin=0.3, fout=0.8, hp=40.0)],
              "a metal bender's creaks and clacks slowed to 0.7 (plates bending and snapping) over a leather stress "
              "creak slowed to half: busy, clanking metal on a low creak, 2.5 s"),
        "b": ([L(376926, 7.8, 2.5, speed=0.6, fin=0.05, fout=0.8, hp=50.0),
               L(536187, 1.0, 2.5, speed=0.5, gain=-4.0, fin=0.3, fout=0.8, hp=40.0)],
              "a metal groan with debris slowed to 0.6 over a leather stretch slowed to half: one long heavy "
              "buckling groan instead of a's clanks, 2.5 s"),
    }),
    "chimera-sweep": ("shot", -20.0, -4.0, -20.0,
                      CHIMERA + "the rear sweep: its drop past the ship and its climb back up from below",
                      "a rushing jet pass fading out above, then rising from below: a dopplered whine", {
        "a": ([L(640503, 4.4, 2.0, fin=0.02, fout=1.4, hp=60.0),
               L(640503, 2.4, 2.0, speed=(0.8, 1.1), at=1.7, fin=1.0, fout=0.05, hp=60.0, swell=(-20.0, 1.0))],
              "a close Eurofighter pass: its roar fading out, then its approach played again gliding up into a cut-"
              "off: a sharp, bright pass, 3.7 s"),
        "b": ([L(640499, 3.7, 2.2, fin=0.02, fout=1.5, hp=60.0),
               L(640499, 1.5, 2.2, speed=(0.8, 1.1), at=1.9, fin=1.0, fout=0.05, hp=60.0, swell=(-20.0, 1.0))],
              "another close pass of the same jet: a broader, longer roar than a, darker in its fade, 4.1 s"),
    }),
    "chimera-capsule": ("shot", -22.0, -5.0, -22.0,
                        CHIMERA + "phase 3's start: the capsule opening (Vorne's cockpit bared), once",
                        "a canopy's seal cracking, a short hiss of escaping air, metal hinges straining", {
        "a": ([L(608677, 0.15, 0.25, fin=0.001, fout=0.1, hp=300.0),
               L(457294, 6.3, 1.2, gain=-4.0, at=0.1, fin=0.02, fout=0.6, hp=300.0),
               L(656529, 0.3, 1.5, speed=0.8, gain=-3.0, at=0.6, fin=0.05, fout=0.6, hp=80.0)],
              "a small sharp crack, an air release's hiss, a heavy door's squeak slowed to 0.8: a crisp seal, a "
              "bright hiss, a low groaning hinge, 2.1 s"),
        "b": ([L(121193, 0.5, 0.3, fin=0.001, fout=0.1, hp=300.0),
               L(555745, 2.5, 1.2, gain=-4.0, at=0.1, fin=0.02, fout=0.6, hp=200.0),
               L(767058, 3.9, 1.2, gain=-3.0, at=0.6, fin=0.05, fout=0.5, hp=100.0)],
              "a dry shell crack, a gas release's softer hiss, a metal gate's creak: drier and more metallic than "
              "a, 1.8 s"),
    }),
    "chimera-burst": ("shot", -16.0, -1.0, -16.0,
                      CHIMERA + "its death (the burst, on Vorne's last line's cut), once",
                      "a sharp metallic bang cut short, debris rattling, then a low settling rumble", {
        "a": ([L(697749, 0.0, 0.5, fin=0.001, fout=0.1, hp=40.0),
               L(703248, 0.0, 1.8, gain=-5.0, at=0.15, fin=0.01, fout=1.0, hp=100.0),
               L(203281, 5.0, 3.0, gain=-6.0, at=0.3, fin=0.3, fout=2.0, hp=20.0, lp=300.0)],
              "a deep resonant metal impact cut short, falling debris, a far shuttle rumble settling: 3.3 s"),
        "b": ([L(752072, 1.75, 0.5, fin=0.001, fout=0.1, hp=60.0),
               L(376926, 8.5, 1.8, gain=-5.0, at=0.15, fin=0.01, fout=1.0, hp=80.0),
               L(222521, 3.0, 3.0, gain=-6.0, at=0.3, fin=0.3, fout=2.0, hp=20.0, lp=300.0)],
              "a heavy metal punch with a crunch, metal debris groaning down, a cracking-earth rumble: brighter "
              "bang and more rattle than a, 3.3 s"),
    }),
    "heart-beat": ("shot", -18.0, -3.0, -18.0,
                   HEART + "every pulse of its light (every 3 s in phases 1-2, every 2 s in phase 3), with the "
                   "heartbeat ring; under the whole fight",
                   "a huge, slow, muffled thud: a bass drum through a wall, slowed, a low sub swell under it", {
        "a": ([L(410698, 0.0, 1.6, speed=0.55, fin=0.002, fout=0.8, hp=25.0, lp=500.0),
               L(203281, 5.0, 1.6, gain=-8.0, fin=0.35, fout=1.0, hp=20.0, lp=120.0)],
              "a concert bass drum's hard hit slowed to 0.55 and muffled, a rumble swelling under it: a deep round "
              "boom, 1.6 s"),
        "b": ([L(573251, 0.0, 1.6, speed=0.5, fin=0.002, fout=0.8, hp=25.0, lp=400.0),
               L(203281, 9.0, 1.6, gain=-8.0, fin=0.35, fout=1.0, hp=20.0, lp=120.0)],
              "a massive timpani hit slowed to half and muffled, the same swell under it: a tighter, more tonal "
              "thud than a, 1.6 s"),
    }),
    "heart-membrane": ("shot", -26.0, -8.0, -26.0,
                       HEART + "each song's tell: a membrane's petals irising open (phase 1, every few seconds)",
                       "a dry stretching creak: leather or a sail under strain, short", {
        "a": ([L(536187, 6.8, 0.8, speed=0.8, fin=0.1, fout=0.25, hp=80.0)],
              "a leather creak, stretched, slowed to 0.8: a tight dry squeak-creak, 0.8 s"),
        "b": ([L(734630, 11.2, 0.8, speed=0.8, fin=0.1, fout=0.25, hp=80.0)],
              "leather rubbed and creaking, slowed to 0.8: softer and grainier than a, 0.8 s"),
    }),
    "heart-closing": ("shot", -18.0, -3.0, -18.0,
                      HEART + "phase 2's start: the walls grinding in, once",
                      "a vast groaning creak, the walls grinding in: stone and rope under strain, slowed", {
        "a": ([L(500143, 18.0, 3.5, speed=0.5, fin=0.6, fout=1.0, hp=30.0),
               L(738784, 5.0, 3.5, speed=0.6, gain=-4.0, fin=0.6, fout=1.0, hp=40.0)],
              "a liner's mooring hawsers creaking and a gangplank groaning, slowed to half, over a heavy stone "
              "dragged: a long rope groan with a stone grind, 3.5 s"),
        "b": ([L(411087, 26.0, 3.5, speed=0.5, fin=0.6, fout=1.0, hp=30.0),
               L(578491, 1.0, 3.5, speed=0.6, gain=-4.0, fin=0.6, fout=1.0, hp=40.0)],
              "a wooden sailing boat creaking, slowed to half, over a heavy stone door slowed: woodier and deeper "
              "than a, the stone a rumble, 3.5 s"),
    }),
    "heart-line": ("shot", -20.0, -4.0, -20.0,
                   HEART + "its told line: the charge (about 1.5 s) and the shot",
                   "a rising choral hum (a choir pitched up), then a hard electric crack", {
        "a": ([L(169946, 26.0, 1.5, speed=(0.9, 1.35), fin=0.4, fout=0.05, hp=150.0, swell=(-18.0, 1.2)),
               L(844244, 0.5, 0.8, gain=-1.0, at=1.45, fin=0.002, fout=0.5, hp=150.0)],
              "a choir vocalising, glided up (0.9 → 1.35) and swelling, cut by a high-voltage power-down's crack: "
              "2.3 s"),
        "b": ([L(625399, 13.0, 1.5, speed=(0.9, 1.35), fin=0.4, fout=0.05, hp=150.0, swell=(-18.0, 1.2)),
               L(625399, 13.0, 1.5, speed=(1.2, 1.8), gain=-5.0, fin=0.4, fout=0.05, hp=150.0, swell=(-18.0, 1.2)),
               L(675760, 10.0, 0.5, at=1.45, fin=0.002, fout=0.3, hp=150.0)],
              "a woman's sung 'ooo' twice, a fourth apart, glided up and swelling, cut by a Tesla coil's crackle: "
              "a purer, thinner hum than a, the crack buzzier, 1.95 s"),
    }),
    "heart-node-cut": ("shot", -20.0, -4.0, -20.0,
                       HEART + "phase 2: each of the four nodes' deaths",
                       "a taut cable snapping, then the wall's groan easing back", {
        "a": ([L(337668, 0.4, 0.5, fin=0.002, fout=0.2, hp=150.0),
               L(500143, 10.0, 2.0, speed=(0.55, 0.4), gain=-6.0, at=0.3, fin=0.3, fout=1.4, hp=30.0, lp=1500.0)],
              "a string tightening and whipping, then the hawsers' groan gliding down: a whip-crack and a sagging "
              "creak, 2.3 s"),
        "b": ([L(161650, 0.35, 0.6, fin=0.002, fout=0.25, hp=150.0),
               L(191497, 31.0, 2.0, speed=(0.5, 0.35), gain=-6.0, at=0.3, fin=0.3, fout=1.4, hp=30.0, lp=1500.0)],
              "a chain snapping, then a creaking pier gliding down: a metallic snap and a woodier sag than a, 2.3 s"),
    }),
    "heart-opening": ("shot", -18.0, -3.0, -18.0,
                      HEART + "phase 3's start: the Heart torn open, the light returning, once",
                      "a long fibrous tearing rip, slowed, with a rising tone as the light returns", {
        "a": ([L(719003, 1.4, 3.0, speed=0.35, fin=0.02, fout=1.0, hp=60.0),
               L(372206, 3.0, 3.0, speed=(0.7, 1.2), gain=-6.0, fin=1.0, fout=0.8, hp=120.0, swell=(-18.0, 1.0))],
              "the shared tissue tear's cloth slowed to 0.35 (a long heavy rip), a bowed cymbal gliding up under "
              "it: 3.0 s"),
        "b": ([L(674035, 1.2, 3.0, speed=0.4, fin=0.02, fout=1.0, hp=60.0),
               L(591525, 1.6, 2.5, speed=0.5, gain=-6.0, at=0.3, fin=0.05, fout=1.0, hp=100.0),
               L(317059, 1.0, 3.0, speed=(0.8, 1.3), gain=-6.0, fin=1.0, fout=0.8, hp=120.0, swell=(-18.0, 1.0))],
              "a ship's sail ripping slowed to 0.4 with a cloth rip, a cymbal swell gliding up: a coarser, "
              "lower rip than a, the tone smoother, 3.0 s"),
    }),
    "heart-death": ("shot", -16.0, -1.0, -16.0,
                    HEART + "its death chain, once (the campaign's last kill)",
                    "the heartbeat stopping mid-beat, then deep collapsing rumbles and a long fading choral tone", {
        "a": ([L(410698, 0.0, 0.18, speed=0.55, fin=0.002, fout=0.03, hp=25.0, lp=500.0),
               L(203281, 2.0, 4.0, at=0.5, fin=0.2, fout=2.0, hp=20.0, lp=800.0),
               L(222521, 0.4, 3.0, speed=0.6, gain=-4.0, at=0.7, fin=0.05, fout=1.5, hp=30.0),
               L(169946, 26.0, 5.0, speed=0.8, gain=-8.0, at=1.2, fin=1.0, fout=3.5, hp=120.0)],
              "heart-beat a's boom cut off as it starts, a deep rumble and cracking earth, the choir slowed to 0.8 "
              "fading out: 6.2 s"),
        "b": ([L(573251, 0.0, 0.18, speed=0.5, fin=0.002, fout=0.03, hp=25.0, lp=400.0),
               L(203281, 8.0, 4.0, at=0.5, fin=0.2, fout=2.0, hp=20.0, lp=800.0),
               L(567249, 0.0, 3.0, speed=0.5, gain=-4.0, at=0.7, fin=0.05, fout=1.5, hp=30.0),
               L(625399, 13.0, 5.0, speed=0.7, gain=-8.0, at=1.2, fin=1.0, fout=3.5, hp=120.0)],
              "heart-beat b's thud cut off, the rumble and stones tumbling slowed to half, a sung 'ooo' slowed to "
              "0.7 fading out: a rockier collapse and a single voice, 6.2 s"),
    }),
    "echo-form": ("shot", -26.0, -8.0, -26.0,
                  "Level 49 (the boss rush): the memory forming the next echo out of violet motes, once per echo",
                  "a soft rising shimmer: a bowed cymbal swell or a rainstick turned slowly; no chime", {
        "a": ([L(317058, 0.8, 2.5, fin=0.8, fout=0.3, hp=200.0)],
              "a cymbal swell, its natural rise: a soft metallic wash growing, 2.5 s"),
        "b": ([L(476851, 0.0, 2.5, fin=0.5, fout=0.3, hp=300.0, lp=6000.0, swell=(-20.0, 1.0))],
              "a rainstick turned slowly, swelling: a grainy trickle rising, 2.5 s"),
    }),
    "echo-dissolve": ("shot", -26.0, -8.0, -26.0,
                      "Level 49: an echo dissolving into violet motes (its death; the original's chain, shortened, "
                      "plays too)",
                      "a soft falling shimmer, echo-form's counterpart; no chime", {
        "a": ([R(317058, 0.8, 2.5, fin=0.8, fout=0.3, hp=200.0)],
              "echo-form a played backwards: the cymbal wash falling away, 2.5 s"),
        "b": ([L(476851, 35.5, 2.5, fin=0.3, fout=1.0, hp=300.0, lp=6000.0)],
              "the rainstick's last grains running out: a trickle thinning to nothing, 2.5 s"),
    }),
    "spore-inside": ("loop", -34.0, -12.0, -32.0,
                     "Level 45: while the ship is inside a spore cloud (a quiet loop, stopped when it leaves)",
                     "a soft dry rustle (dry leaves or sand stirred), quiet", {
        "a": (([Lp(179338, 0.5, speed=0.8, hp=300.0, lp=6000.0)], 8.0, 1.0),
              "dry leaves rustling, slowed to 0.8: a soft busy crackle-rustle, a seamless 8 s loop"),
        "b": (([Lp(771130, 1.0, speed=0.7, hp=200.0, lp=5000.0)], 8.0, 1.0),
              "dry leaves stirred, slowed to 0.7 and darker: a smoother, hissier rustle than a, a seamless 8 s "
              "loop"),
    }),
    "spore-burn": ("shot", -26.0, -8.0, -26.0,
                   "Level 45: an `area` hit burning a spore cloud",
                   "a soft dry crackle (a dry grass fire, short); never a pop", {
        "a": ([L(415201, 1.0, 1.0, fin=0.05, fout=0.4, hp=300.0)],
              "a fire crackling over crushed bark: a dense dry crackle, 1.0 s"),
        "b": ([L(528853, 0.4, 0.9, fin=0.01, fout=0.4, hp=300.0)],
              "dry leaves crushed: a crunchier, drier crackle than a, 0.9 s"),
    }),
    "pit-tell": ("shot", -26.0, -8.0, -26.0,
                 "Level 45: a spawning pit's 2 s tell before it opens",
                 "a low dry creak of parting hide (stretched leather slowly pulled)", {
        "a": ([L(559079, 0.2, 2.0, speed=0.6, fin=0.3, fout=0.3, hp=60.0)],
              "leather under stress slowed to 0.6: a low drawn-out creak, 2.0 s"),
        "b": ([L(734630, 11.0, 2.0, speed=0.6, fin=0.3, fout=0.3, hp=60.0)],
              "leather rubbed and creaking slowed to 0.6: grainier and more broken than a, 2.0 s"),
    }),
    "pit-open": ("shot", -20.0, -4.0, -20.0,
                 "Level 45: a spawning pit opening (after its tell)",
                 "a deep dry tearing groan: the tissue tear slowed 2×; never wet", {
        "a": ([L(719003, 0.35, 1.8, speed=0.3, fin=0.01, fout=0.5, hp=40.0),
               L(203281, 5.0, 1.8, gain=-8.0, fin=0.1, fout=0.8, hp=20.0, lp=200.0)],
              "the tissue tear a (cloth) slowed to 0.3 over a low rumble: a deep slow rip, 1.8 s"),
        "b": ([L(674035, 1.45, 1.5, speed=0.5, fin=0.01, fout=0.5, hp=40.0),
               L(591525, 1.6, 1.2, speed=0.35, gain=-8.0, at=0.05, fin=0.01, fout=0.5, hp=60.0)],
              "the tissue tear b (sail and cloth) slowed to half: a coarser, shorter rip than a, 1.5 s"),
    }),
    "organ-swell": ("shot", -26.0, -8.0, -26.0,
                    "Level 45: a spawning organ's 0.5 s swell before each release",
                    "a low rising creak under strain (rope tightening)", {
        "a": ([L(351614, 2.0, 0.5, speed=(0.7, 0.9), fin=0.2, fout=0.05, hp=80.0, swell=(-12.0, 1.0))],
              "a mooring rope tightening, slowed and rising: a short taut creak, 0.5 s"),
        "b": ([L(596513, 10.8, 0.5, speed=(0.6, 0.8), fin=0.2, fout=0.05, hp=80.0, swell=(-12.0, 1.0))],
              "another rope tightening, slowed and rising: a higher, scratchier creak than a, 0.5 s"),
    }),
    "organ-release": ("shot", -22.0, -5.0, -22.0,
                      "Level 45: each release of a spawning organ (Whirl Seeds thrown out), often",
                      "the tissue tear, short, with a dull dry thud; no wet tear, no pop", {
        "a": ([L(719003, 0.35, 0.4, speed=0.6, fin=0.005, fout=0.15, hp=100.0),
               L(527423, 0.0, 0.5, speed=0.8, gain=-3.0, fin=0.002, fout=0.3, hp=30.0, lp=400.0)],
              "a short cloth rip over a muffled kick: a quick tear and a soft thump, 0.5 s"),
        "b": ([L(674035, 1.45, 0.4, fin=0.005, fout=0.15, hp=100.0),
               L(810170, 0.3, 0.6, gain=-3.0, fin=0.002, fout=0.3, hp=30.0, lp=500.0)],
              "a short sail rip over a heavy object dropped: a lower tear and a heavier thud than a, 0.6 s"),
    }),
    "organ-death": ("shot", -20.0, -4.0, -20.0,
                    "Level 45: a spawning organ's death (the level's targets), over the `medium` explosion rung",
                    "the tissue tear, long; never wet", {
        "a": ([L(719003, 0.35, 1.5, speed=0.5, fin=0.005, fout=0.5, hp=80.0)],
              "the tissue tear a (cloth) slowed to half: a long dry rip, 1.5 s"),
        "b": ([L(674035, 1.2, 1.4, speed=0.7, fin=0.005, fout=0.5, hp=80.0),
               L(591525, 1.6, 1.0, speed=0.6, gain=-6.0, at=0.1, fin=0.01, fout=0.4, hp=100.0)],
              "the tissue tear b (sail and cloth) slowed to 0.7: a rougher, fibrous rip than a, 1.4 s"),
    }),
    "call-organ-taken": ("shot", -20.0, -4.0, -20.0,
                         "Level 45 (t≈127): the call organ taken (its roots torn: the tissue tear each), once; it "
                         "gives the Swarm Call",
                         "a low breathy horn note: a real conch or a low horn, pitched down (the Swarm Call's source)", {
        "a": ([L(78974, 1.0, 2.5, speed=0.7, fin=0.2, fout=0.8, hp=50.0)],
              "a conch blown, slowed to 0.7: a breathy, hollow low note, 2.5 s"),
        "b": ([L(479998, 0.2, 2.5, speed=0.7, fin=0.2, fout=0.8, hp=50.0)],
              "a cow horn blown, slowed to 0.7: a rougher, buzzier note than a, 2.5 s"),
    }),
    "swarm-call": ("shot", -20.0, -4.0, -20.0,
                   "the Vrell Swarm Call special (from Level 46): the call when it is fired (the drones come); the "
                   "thorns are the Needler's spit at −6 dB",
                   "a low breathy horn swell: a real conch or a low horn, pitched down (the call organ's source)", {
        "a": ([L(78974, 0.5, 3.0, speed=0.6, fin=0.1, fout=0.8, hp=40.0, swell=(-18.0, 0.8)),
               L(78974, 0.5, 3.0, speed=0.4, gain=-6.0, fin=0.1, fout=0.8, hp=30.0, swell=(-18.0, 0.8))],
              "the conch slowed to 0.6 with itself a fifth lower, swelling: a deep breathy call, 3.0 s"),
        "b": ([L(479998, 0.0, 3.0, speed=0.6, fin=0.1, fout=0.8, hp=40.0, swell=(-18.0, 0.8)),
               L(479998, 0.0, 3.0, speed=0.4, gain=-6.0, fin=0.1, fout=0.8, hp=30.0, swell=(-18.0, 0.8))],
              "the cow horn slowed to 0.6 with itself a fifth lower, swelling: a rougher, reedier call than a, "
              "3.0 s"),
    }),
    "swarm-drone-burst": ("shot", -28.0, -10.0, -28.0,
                          "the Vrell Swarm Call: one of its drones bursting (shot down or spent)",
                          "a soft dry rustle; no pop", {
        "a": ([L(862894, 30.0, 0.5, fin=0.02, fout=0.3, hp=300.0, lp=6000.0)],
              "dry leaves rustling and breaking: a quick soft crunch, 0.5 s"),
        "b": ([L(179338, 8.0, 0.45, fin=0.02, fout=0.3, hp=300.0, lp=6000.0)],
              "dry leaves rustled: a smoother, softer brush than a, 0.45 s"),
    }),
    "glyph-stone-break": ("shot", -22.0, -5.0, -22.0,
                          "Level 43's paying ground: a glyph stone's death, over the `small` explosion rung",
                          "a stone crack and crumble: a rock split by a hammer, gravel falling", {
        "a": ([L(105281, 0.6, 0.6, fin=0.001, fout=0.3, hp=80.0),
               L(567249, 0.5, 1.0, gain=-6.0, at=0.1, fin=0.02, fout=0.5, hp=100.0)],
              "a stone struck hard, then stones and gravel falling: a heavy knock and a short tumble, 1.1 s"),
        "b": ([L(530354, 0.0, 0.2, fin=0.001, fout=0.08, hp=150.0),
               L(326303, 1.5, 1.2, gain=-8.0, at=0.08, fin=0.02, fout=0.6, hp=150.0, lp=6000.0)],
              "a single stone click, then sand and grit trickling: lighter and drier than a, 1.3 s"),
    }),
    "casket-crack": ("shot", -24.0, -6.0, -24.0,
                     "Level 46's secondary: a seed casket broken",
                     "a dry crack (a large dry gourd or a ceramic pot cracking), a short rattle of seeds", {
        "a": ([L(540336, 0.25, 0.6, fin=0.001, fout=0.25, hp=100.0),
               L(533385, 0.0, 0.5, gain=-6.0, at=0.08, fin=0.002, fout=0.3, hp=200.0)],
              "a clay jar breaking with a rattle's shake: a hard ceramic crack and a seed rattle, 0.6 s"),
        "b": ([L(399080, 2.7, 0.8, fin=0.001, fout=0.3, hp=100.0),
               L(610143, 8.4, 0.6, gain=-6.0, at=0.1, fin=0.005, fout=0.3, hp=200.0)],
              "clay pottery dropped and breaking, dry seed pods rattling: a duller, longer crack than a, 0.8 s"),
    }),
    "warm-hull-taken": ("shot", -30.0, -10.0, -30.0,
                        "Level 46 (t≈184): the warm hull's core taken (its sinews torn: the tissue tear each), once",
                        "a soft low hum fading in and out (a transformer or a bowed metal bowl), very soft", {
        "a": ([L(453444, 5.0, 3.0, fin=1.0, fout=1.5, hp=40.0)],
              "an electric transformer humming in a large room: a soft buzzing hum, 3.0 s"),
        "b": ([L(573805, 30.0, 3.0, speed=0.5, fin=1.0, fout=1.5, hp=40.0)],
              "the rubbed singing bowl slowed to half: a pure, beating low tone, warmer than a, 3.0 s"),
    }),
    "reactor-core-death": ("shot", -22.0, -5.0, -22.0,
                           "Level 48's secondary: a reactor core's death (quieter than the Ascendant's generator)",
                           "a short metallic crack and a fading electrical hum (a transformer failing)", {
        "a": ([L(697754, 0.2, 0.5, fin=0.002, fout=0.3, hp=100.0),
               L(844244, 0.4, 1.4, gain=-4.0, at=0.05, fin=0.01, fout=1.0, hp=100.0)],
              "a resonant metal impact, then a high-voltage power-down: a clank and a falling electric whine, "
              "1.45 s"),
        "b": ([L(452586, 16.6, 0.4, fin=0.002, fout=0.2, hp=150.0),
               L(453444, 10.0, 1.5, speed=(1.0, 0.6), gain=-4.0, at=0.05, fin=0.01, fout=1.2, hp=40.0)],
              "a crowbar's hit with a zing, then a transformer hum sagging in pitch: brighter crack and a lower, "
              "buzzier fade than a, 1.55 s"),
    }),
    "gate-groan": ("shot", -18.0, -3.0, -18.0,
                   "Level 50 (K+10.5 on): the gate's groan as it starts to close (a big moment, under the music)",
                   "a vast stone-and-metal stress: a dam's or a ship hull's groan, slowed", {
        "a": ([L(376926, 7.5, 4.0, speed=0.4, fin=0.8, fout=1.2, hp=30.0),
               L(578491, 1.0, 4.0, speed=0.5, gain=-4.0, fin=0.8, fout=1.2, hp=30.0)],
              "a metal groan slowed to 0.4 over a heavy stone door slowed to half: a deep metal moan over grinding "
              "stone, 4.0 s"),
        "b": ([L(455757, 3.2, 4.0, speed=0.35, fin=0.8, fout=1.2, hp=30.0),
               L(222521, 2.0, 4.0, speed=0.5, gain=-5.0, fin=0.8, fout=1.2, hp=30.0)],
              "the ship's hull creak slowed to 0.35 over cracking earth slowed to half: woodier, more of a creak "
              "than a, with a crackling rumble, 4.0 s"),
    }),
    "gate-greying": ("shot", -26.0, -8.0, -26.0,
                     "Level 50 (K+10.5-K+38): the gate's tissue greying (once, under the gate's groan)",
                     "a long receding dry creak", {
        "a": ([L(734549, 23.0, 4.0, speed=0.5, fin=0.3, fout=3.0, hp=50.0, lp=3000.0)],
              "a heavy leather jacket's creak slowed to half, darkened and fading away: 4.0 s"),
        "b": ([L(411087, 26.0, 4.0, speed=0.5, fin=0.3, fout=3.0, hp=50.0, lp=2500.0)],
              "the sailing boat's creak slowed to half, darkened and fading away: woodier and steadier than a, "
              "4.0 s"),
    }),
    "glyph-dark": ("shot", -24.0, -6.0, -24.0,
                   "Level 50: each of the gate's glyphs going dark (one by one; the game plays each quieter)",
                   "one soft low thump: a heavy door closing far away", {
        "a": ([L(217796, 0.6, 1.0, fin=0.005, fout=0.6, hp=40.0, lp=1200.0)],
              "a door slammed far away, muffled: a soft round thump, 1.0 s"),
        "b": ([L(450791, 18.3, 1.5, fin=0.005, fout=0.9, hp=40.0, lp=1000.0)],
              "a distant door slam with a roomy boom, muffled: deeper and longer than a, 1.5 s"),
    }),
    "event-plane": ("loop", -30.0, -10.0, -28.0,
                    "the `event-plane` layer over `vrell-gate` for the whole of Level 44 (the beds' bus, under the "
                    "music)",
                    "a slow, deep groan of stressed space: a real low structural creak (a ship's hull or a wooden pier "
                    "under load) slowed 4×, under a faint high shimmer (a bowed cymbal's ring, very quiet); seamless "
                    "16 s", {
        "a": (([Lp(191497, 30.0, speed=0.25, hp=25.0, lp=2000.0),
                Lp(372206, 0.3, gain=-18.0, speed=0.6, hp=1500.0)], 16.0, 1.0),
              "a creaking wooden pier slowed 4× (long, low groans) under a bowed cymbal's faint ring: a seamless "
              "16 s loop"),
        "b": (([Lp(500143, 5.0, speed=0.25, hp=25.0, lp=2000.0),
                Lp(372206, 0.3, gain=-18.0, speed=0.6, hp=1500.0)], 16.0, 1.0),
              "a liner's hawsers and gangplank slowed 4× (a steadier, denser groan than a) under the same faint "
              "ring: a seamless 16 s loop"),
    }),
    "reversal-groan": ("shot", -20.0, -4.0, -20.0,
                       "Level 44: each of the two reversals (the game cuts it to the run, 4 s and 5 s, with a 0.5 s "
                       "fade); the music ducks 3 dB under it",
                       "the `event-plane` layer's creak bent down 3 semitones and back over 5 s, heavier", {
        "a": ([L(191497, 30.0, 5.0, speed=(0.25, 0.21, 0.25), fin=0.5, fout=1.0, hp=25.0, lp=1500.0),
               L(191497, 30.0, 5.0, speed=(0.125, 0.105, 0.125), gain=-6.0, fin=0.5, fout=1.0, hp=20.0,
                 lp=800.0)],
              "event-plane a's pier creak bent down 3 semitones and back, with itself an octave below: 5.0 s"),
        "b": ([L(500143, 5.0, 5.0, speed=(0.25, 0.21, 0.25), fin=0.5, fout=1.0, hp=25.0, lp=1500.0),
               L(500143, 5.0, 5.0, speed=(0.125, 0.105, 0.125), gain=-6.0, fin=0.5, fout=1.0, hp=20.0,
                 lp=800.0)],
              "event-plane b's hawser groan bent the same way with its octave below: denser than a, 5.0 s"),
    }),
    "pod-pearl": ("shot", -24.0, -6.0, -24.0,
                  "Level 47's secret: the memory pearl broken, over the built crate break; once a level at most",
                  "a stone bead cracking with a short high ring", {
        "a": ([L(539973, 0.0, 0.3, fin=0.001, fout=0.15, hp=300.0),
               L(150454, 0.0, 0.6, speed=2.0, gain=-6.0, at=0.02, fin=0.002, fout=0.35, hp=500.0)],
              "a flint struck, a crystal bowl an octave up ringing: a sharp click and a clear high ring, 0.6 s"),
        "b": ([L(530354, 0.0, 0.2, speed=1.5, fin=0.001, fout=0.08, hp=300.0),
               L(616335, 0.0, 0.7, speed=2.0, gain=-6.0, at=0.02, fin=0.002, fout=0.4, hp=500.0)],
              "a stone struck, sped up (smaller), a singing bowl an octave up: a duller click and a rounder ring "
              "than a, 0.7 s"),
    }),
    "pod-archive": ("shot", -22.0, -5.0, -22.0,
                    "Level 48's secret: the archive opened, over the built crate break; once a level at most",
                    "a metal case wrenched open", {
        "a": ([L(835919, 3.6, 1.5, fin=0.005, fout=0.4, hp=100.0)],
              "a toolbox unlatched and opened: clacking latches and a metal lid, 1.5 s"),
        "b": ([L(656529, 0.3, 1.0, speed=1.2, fin=0.01, fout=0.3, hp=150.0),
               L(847733, 0.8, 0.7, gain=-2.0, at=0.5, fin=0.005, fout=0.3, hp=100.0)],
              "a heavy hinge squeaking, then a big metal drawer pulled: more of a wrench than a, 1.2 s"),
    }),
    "pod-seed-vault": ("shot", -22.0, -5.0, -22.0,
                       "Level 50's secret: the seed vault broken, over the built crate break; once a level at most",
                       "a dry chitin crack: a dry seed pod or a coconut shell split", {
        "a": ([L(136494, 23.3, 0.6, fin=0.001, fout=0.3, hp=100.0),
               L(610143, 8.5, 0.5, gain=-8.0, at=0.05, fin=0.005, fout=0.3, hp=200.0)],
              "a coconut hit and split with dry seed pods rattling: a hollow knock-crack, 0.6 s"),
        "b": ([L(65420, 0.0, 0.8, speed=0.7, fin=0.001, fout=0.3, hp=100.0)],
              "a walnut cracked, slowed to 0.7 (a bigger shell): a sharper, splintering crack than a, 0.8 s"),
    }),
})


def glide(x, *args):
    """sfx_r43g.glide(), plus three speeds: s0 → s1 over the first half, s1 → s2 over the second."""
    *speeds, n = args
    if len(speeds) == 2:
        return _glide43(x, *speeds, n)
    s0, s1, s2 = speeds
    h = n // 2
    steps = np.concatenate([np.linspace(s0, s1, h), np.linspace(s1, s2, n - h)])[:n - 1]
    pos = np.concatenate([[0.0], np.cumsum(steps)])
    return np.array([np.interp(pos, np.arange(x.shape[1]), ch) for ch in x])


_glide43 = M.sfx_r43g.glide
M.sfx_r43g.glide = glide

_mix72 = M.mix
_change72 = M.change_text


def mix(layers):
    """sfx_r72's mix, the whole mix played backwards when its first layer is marked `rev`."""
    out = _mix72(layers)
    return np.ascontiguousarray(out[:, ::-1]) if layers and layers[0].get("rev") else out


def change_text(subject, kind, spec, layer):
    text = _change72(subject, kind, spec, layer)
    speed = layer["speed"]
    if isinstance(speed, tuple) and len(speed) == 3:
        two = f"gliding {speed[0]:g} → {speed[1]:g}"
        text = text.replace(two, two + f" → {speed[2]:g} (down and back)")
    first = M.layers_of(kind, spec)[0]
    return text.replace(", peak-normalised", ", the whole mix reversed, peak-normalised") if first.get("rev") else text


M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {"reversal-tell": reversal_tell}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r84.py", "r84", "round 84"
M.mix, M.change_text = mix, change_text

if __name__ == "__main__":
    M.main(sys.argv[1:])
