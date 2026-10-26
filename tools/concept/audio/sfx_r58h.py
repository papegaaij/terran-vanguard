#!/usr/bin/env python3
"""Concept round 58 (M8 part H, step AUD-H): Level 28 "Abyssal Maw"'s sounds. The `europa-hatchery` bed (sections
1-3) and the `caldera-rumble` bed (the antechamber and the arena); the Abyssal Maw's arrival roar, gill breath, throat
charge, coil sweep (the groan of its tell and the swing), Eel Swarm shed, dive, lunge, Helix implant spark and zap and
its death (the boss document's *Sounds brief*); after the kill the shaft's climb, the breakout through the ice ceiling,
the Ascendancy cruisers' rise and their broadside, flak and point defence (above the ice: in the air again). The sfx are
cut from real recordings in the Freesound database (round 48's rules: heavy and big, muffled and pressured under water,
no bubbly, popping or squelching sounds, no low-pass on the sfx bus: each layer carries its own filters). Reused, no
row: the under-water explosion family (round 52's a), the *Marlin*'s crack for *Pelagic-3* (round 57's a),
`europa-ice` above the ice (round 51), the Depth Hunters' sounds (round 57).

Every sfx layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 58 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The sfx are only cut, slowed, glided, layered, swelled, faded, filtered,
looped, reverberated and levelled; nothing in them is synthesized. The tools are sfx_r57g.py's (copied: preview,
steady, bed, build, the --docs rows), plus a REVERB stage for the three sounds the brief gives a long tail.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table below says what
each variant is made of):

  europa-hatchery-r58-*   seamless 24 s LOOPs: a slow warm throb, a far Choir hum once a loop, the egg racks creaking
  caldera-rumble-r58-*    seamless 16 s LOOPs: the vent's deep hot roar
  shaft-climb-r58-*       10 s: a rush rising with the climb, the ice ceiling groaning ahead
  maw-arrival-r58-*       ≈ 8 s with the reverb: a vast subsonic groan (blue whale calls, a hull's stress)
  maw-gill-r58-*          ≈ 1 s: a deep thump and a soft exhale
  maw-throat-r58-*        0.9 s: a low rising drone with a growl
  maw-sweep-groan-r58-*   1.2 s: plates grinding (the sweep's tell)
  maw-sweep-swing-r58-*   ≈ 1.1 s: a heavy swish of displaced water
  maw-shed-r58-*          0.7 s: a wet tearing slide
  maw-dive-r58-*          ≈ 5 s with the reverb: a long falling roar and a pressure swell
  maw-lunge-r58-*         2.0 s: a rushing surge and a jaw snap
  implant-spark-r58-*     1.4 s: electrical crackle and a metal clunk (machine, not creature)
  implant-zap-r58-*       0.8 s: an arc building and a hard zap
  maw-death-r58-*         ≈ 8 s with the reverb: three electrical cracks, a dying groan, a pressure boom
  breakout-r58-*          ≈ 4.5 s: ice calving and shattering, water roaring into the open
  cruiser-rise-r58-*      5 s: a deep engine thrum, ice grinding off a hull
  cruiser-broadside-r58-* ≈ 2 s: a naval gun volley
  cruiser-flak-r58-*      ≈ 1 s: an airburst
  cruiser-pd-r58-*        0.8 s: a rapid autocannon burst

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the hatchery bed like `europa-vents`, the caldera rumble a little
over it (it swells in the antechamber), the boss's sounds like the Act 4 units' around it (the gill breath, throat and
shed under the fire, the sweep's swing and the lunge as hits, the arrival, dive and death the loudest), the breakout
and the cruisers like the act's set pieces.

Usage: python3 tools/concept/audio/sfx_r58h.py [subject-or-file ...]   e.g. maw-gill breakout-r58-b
       python3 tools/concept/audio/sfx_r58h.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                        rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets
and the loop seams.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import sfx_r43g  # noqa: E402
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, reverb, sine, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r58h.py"
R = "r58"
ROUND = "round 58"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    19486: ('JacobsLadderSingle1.flac', 'Halleck', BY4, P + "19/19486_6281-hq.ogg"),
    31566: ('Foundry Furnace.wav', 'Walter_Odington', CC0, P + "31/31566_48671-hq.ogg"),
    59966: ('Tearing.wav', 'juskiddink', BY4, P + "59/59966_649468-hq.ogg"),
    67516: ('Howitzer Gun Shots', 'qubodup', CC0, P + "67/67516_71257-hq.ogg"),
    74914: ('ferry engine room1.wav', 'mrmayo', CC0, P + "74/74914_271213-hq.ogg"),
    82682: ('Quarry blasting.wav', 'juskiddink', BY4, P + "82/82682_649468-hq.ogg"),
    94167: ('Oskar Eisbrecher.mp3', 'Master Block', CC0, P + "94/94167_1433412-hq.ogg"),
    113679: ('Breaking_large_ice.wav', 'CGEffex', BY4, P + "113/113679_1386366-hq.ogg"),
    145721: ('Mooring Rope.wav', 'Rmutt', CC0, P + "145/145721_981371-hq.ogg"),
    147873: ('DepthBomb_04.aif', 'ZeSoundResearchInc.', CC0, P + "147/147873_2101444-hq.ogg"),
    147876: ('DepthBomb_01.aif', 'ZeSoundResearchInc.', CC0, P + "147/147876_2101444-hq.ogg"),
    147880: ('DepthBomb_05.aif', 'ZeSoundResearchInc.', CC0, P + "147/147880_2101444-hq.ogg"),
    161341: ('Howitzer Artillery Gun Shot Sound Effect 03', 'qubodup', CC0, P + "161/161341_71257-hq.ogg"),
    162365: ('Navy Battleship Soundscape: Turret Gunshots, Mechanical Engine Humm, Radio Chatter, Officer Command '
             'Voices.flac', 'qubodup', CC0, P + "162/162365_71257-hq.ogg"),
    163119: ('Navy MK 15 Phalanx CIWS Anti-Air Fast Burst', 'qubodup', CC0, P + "163/163119_71257-hq.ogg"),
    169946: ('choir vocalising 01.wav', 'klankbeeld', BY4, P + "169/169946_1648170-hq.ogg"),
    193822: ('Deep Sea Ambience', 'jhumbucker', CC0, P + "193/193822_208303-hq.ogg"),
    268023: ('Breaking_Ice_01.wav', 'dheming', BY4, P + "268/268023_2035780-hq.ogg"),
    271008: ('glacierCalving.mp3', 'CCCanary', CC0, P + "271/271008_4119059-hq.ogg"),
    343682: ('Blue Whale B call @ 5X', 'MBARI_MARS', BY4, P + "343/343682_6181234-hq.ogg"),
    369485: ('Earthquake  **audible only with appropriate speakers**', 'MBARI_MARS', CC0,
             P + "369/369485_6181234-hq.ogg"),
    376799: ('Crashing Waves into Rocks 1', 'amholma', CC0, P + "376/376799_6128004-hq.ogg"),
    379905: ('NIAGARA WATERS ROARING.mp3', 'KevinT1001', CC0, P + "379/379905_571017-hq.ogg"),
    392307: ('20170507_horse.snort.wav', 'dobroide', BY4, P + "392/392307_8043-hq.ogg"),
    396240: ('Welding_Electric Sparks.wav', 'weaveofkev', CC0, P + "396/396240_453602-hq.ogg"),
    400997: ('Deep Thumps.wav', 'eardeer', CC0, P + "400/400997_2155630-hq.ogg"),
    403252: ('Taser/High Voltage discharge', 'The_Chemical_Workshop', CC0, P + "403/403252_7809148-hq.ogg"),
    407301: ('door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav', 'kyles', CC0,
             P + "407/407301_612689-hq.ogg"),
    415962: ('high-voltage-discharge-2.wav', 'follytowers', CC0, P + "415/415962_6668765-hq.ogg"),
    439280: ('Angry tiger.wav', 'schots', CC0, P + "439/439280_954919-hq.ogg"),
    440858: ('BreakingIce.wav', 'iainmccurdy', BY4, P + "440/440858_3655844-hq.ogg"),
    451398: ('fishing boat loud throaty engine onboard various speeds wandering perspective metal plastic hull '
             'creaks India.flac', 'kyles', CC0, P + "451/451398_612689-hq.ogg"),
    452107: ('wood creaks deep long door or ship hull1.flac', 'kyles', CC0, P + "452/452107_612689-hq.ogg"),
    481896: ('R24-18-Tent or Tarp Whoosh.wav', 'craigsmith', CC0, P + "481/481896_2524442-hq.ogg"),
    484178: ('Peeling corn on the cob', 'AlexLane', CC0, P + "484/484178_2617886-hq.ogg"),
    529620: ('Massive Structure Bend', 'Kostas17', BY3, P + "529/529620_3377875-hq.ogg"),
    535616: ('Loud Chomping or Biting Sound with Chewing (Animal)', 'OBXJohn', BY4, P + "535/535616_3719168-hq.ogg"),
    567148: ('Steel mill low frequency drone', 'sarana', CC0, P + "567/567148_385976-hq.ogg"),
    640817: ('Fireworks 1.wav', 'KandiKlover', BY4, P + "640/640817_4870240-hq.ogg"),
    675760: ('S16-18 Tesla coil sparking and humming.wav', 'craigsmith', CC0, P + "675/675760_2524442-hq.ogg"),
    681896: ('blue whale D calls **audible only with appropriate speakers**', 'MBARI_MARS', BY4,
             P + "681/681896_6181234-hq.ogg"),
    737896: ('Ice - Lake fractures', 'Vrymaa', CC0, P + "737/737896_13973196-hq.ogg"),
    751170: ('heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024', 'Artninja', BY4,
             P + "751/751170_10996917-hq.ogg"),
    763026: ('Bear Angry Growl', 'celldroid', CC0, P + "763/763026_1764719-hq.ogg"),
    772895: ('WATRMvmt underwater passby slosh medium light whoosh', 'jerry.berumen', BY4,
             P + "772/772895_6753194-hq.ogg"),
    816640: ('Throat Singing in a Cave', 'ItsThatRoboGuy', CC0, P + "816/816640_8175042-hq.ogg"),
    824087: ('Sea blowhole, breathe. Bufador de mar, respira.', 'SonoRec', CC0, P + "824/824087_2467044-hq.ogg"),
    847210: ('Birch bark tearing, patting, rubbing (denoised)', 'DeqstersLab', BY4, P + "847/847210_189858-hq.ogg"),
    849934: ('High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo', 'Hear_it_use_it', BY4,
             P + "849/849934_18634554-hq.ogg"),
    854186: ('Autocannon Three Shot Burst', 'qubodup', CC0, P + "854/854186_71257-hq.ogg"),
    863689: ('26.07.21- Real Human Heartbeat - Contact Mic Recording 4', 'Valerie-Vivegnis', BY4,
             P + "863/863689_15375955-hq.ogg"),
}

# A source used smoothed: sound -> (ms, seconds): its first `seconds` with each STFT bin's magnitude replaced by its
# running median over `ms` (any short pop or flutter in it smoothed out, the whoosh or breath kept) before anything is
# cut from it.
SMOOTH = {772895: (40.0, 24.0), 392307: (30.0, 2.3)}  # the hydrophone's slosh and the snort's flutter smoothed out

_decoded = {}


def preview(sound):
    """The decoded HQ preview (smoothed if in SMOOTH), fetched from the CDN into the cache when missing."""
    if sound not in _decoded:
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        x = decode(path)
        if sound in SMOOTH:
            ms, seconds = SMOOTH[sound]
            x = steady(x[:, :int(seconds * SR)], ms)
        _decoded[sound] = x
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() and loop() read their layers through this
L = sfx_r43g.L


def steady(x, ms):
    """The signal with its fast spectral wobble smoothed out: each STFT bin's magnitude replaced by its running median
    over `ms` (crackle and short clicks are short; a hum's body is not), the phase kept (sfx_r55f.py's)."""
    nfft, hop = 2048, 256
    k = max(3, int(ms / 1000 * SR / hop) | 1)
    win = np.hanning(nfft)
    out = np.zeros_like(x)
    for c in range(x.shape[0]):
        pad = np.concatenate([np.zeros(nfft), x[c], np.zeros(nfft)])
        frames = np.array([pad[i:i + nfft] * win for i in range(0, len(pad) - nfft, hop)])
        spec = np.fft.rfft(frames, axis=1)
        mag = np.abs(spec)
        h = k // 2
        padded = np.pad(mag, ((h, h), (0, 0)), mode="edge")
        med = np.median(np.stack([padded[i:i + len(mag)] for i in range(k)]), axis=0)
        y = np.fft.irfft(med * np.exp(1j * np.angle(spec)), nfft, axis=1) * win
        acc = np.zeros(len(pad))
        norm = np.zeros(len(pad))
        for j, i in enumerate(range(0, len(pad) - nfft, hop)):
            acc[i:i + nfft] += y[j]
            norm[i:i + nfft] += win ** 2
        out[c] = (acc / np.maximum(norm, 1e-6))[nfft:nfft + x.shape[1]]
    return out


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def bed(layers, length, xfade, extra=None):
    """sfx_r43g.loop() plus the extras: `events`, one-shot layers placed once in the loop at their `at` (wrapping
    round the seam), and `pulse` (period s, depth dB), a slow raised-cosine dip a whole number of times a loop."""
    st = loop(layers, length, xfade)
    extra = extra or {}
    n = st.shape[1]
    for ev in extra.get("events", []):
        x = cut(ev)
        a = int(ev["at"] * SR)
        idx = (a + np.arange(x.shape[1])) % n
        np.add.at(st[0], idx, x[0])
        np.add.at(st[1], idx, x[1])
    if "pulse" in extra:
        period, depth = extra["pulse"]
        cycles = round(length / period)
        t = np.arange(n) / n * cycles
        st = st * undb(-depth * (0.5 - 0.5 * np.cos(2 * np.pi * t)))
    return st


# A subject's mix through a reverb before levelling: subject -> (seconds, mix, damping, seed); the tail kept for
# 60 % of the reverb's length and faded out over its last 0.5 s.
REVERB = {"maw-arrival": (3.0, 0.35, 0.6, 5811), "maw-dive": (2.5, 0.3, 0.6, 5812), "maw-death": (3.0, 0.3, 0.6, 5813)}

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS.
ARENA = "Level 28's arena (t=104 on, the clock halted while the Maw lives; ≈ 130 s at medium)"
RUN = ("Level 28's run (t=122-157, above the ice: the medium is air again), the two Ascendancy cruisers keeping pace "
       "on both flanks")
SHOTS = [(0.0, 1.0, 0.0), (0.07, 0.92, -2.0), (0.15, 0.97, -1.0), (0.22, 0.88, -3.0), (0.30, 0.95, -2.0),
         (0.37, 0.9, -3.0), (0.45, 0.85, -2.0)]  # the broadside a's seven guns: (at s, speed, gain dB)
SOUNDS = {
    "europa-hatchery": ("loop", -42.0, -1.0, -29.0,
                        "Level 28 from the launch through the Brood Galleries and the Spawning Cavern (sections 1-3, "
                        "t=0-80): the hatchery's ambience bed under the music (alone in section 1), in place of "
                        "`europa-vents`; the caldera rumble takes over in the antechamber",
                        "a slow warm throb (the hatchery alive), a distant Choir hum now and then, the egg racks "
                        "creaking; deep, organic and close, quieter than the vent field; no bubbles, nothing that "
                        "pops, a seamless loop", {
        "a": (([Loop(863689, 14.0, speed=0.5, hp=30.0, lp=300.0),
                Loop(193822, 12.0, gain=-6.0, speed=0.7, hp=30.0, lp=350.0)], 24.0, 1.0,
               {"events": [L(816640, 0.2, 9.0, gain=-9.0, at=2.0, speed=0.5, fin=3.0, fout=3.5, hp=60.0, lp=700.0),
                           L(452107, 33.2, 3.0, gain=-11.0, at=12.5, speed=0.5, fin=0.3, fout=1.5, hp=60.0,
                             lp=900.0),
                           L(452107, 42.3, 3.0, gain=-12.0, at=19.0, speed=0.45, fin=0.3, fout=1.5, hp=60.0,
                             lp=800.0)]}),
              "a real heartbeat (a contact microphone on the chest) slowed to half speed and muffled at 300 Hz, a "
              "slow double throb, over a deep under-water drone slowed to 0.7; once a loop throat singing recorded "
              "in a cave, slowed to half and far off (muffled at 700 Hz), swelling in and out over 9 s (the Choir), "
              "and two deep wooden creaks of a ship's hull slowed to 0.5 (the egg racks): 24.0 s loop"),
        "b": (([Loop(529620, 1.0, speed=0.6, hp=25.0, lp=300.0),
                Loop(863689, 60.0, gain=-3.0, speed=0.42, hp=20.0, lp=160.0)], 24.0, 1.0,
               {"events": [L(169946, 24.0, 10.0, gain=-12.0, at=4.0, speed=0.5, fin=3.5, fout=4.0, hp=80.0,
                             lp=800.0),
                           L(145721, 33.4, 3.0, gain=-14.0, at=14.0, speed=0.55, fin=0.2, fout=1.5, hp=80.0,
                             lp=1000.0),
                           L(145721, 56.3, 3.0, gain=-15.0, at=20.0, speed=0.5, fin=0.2, fout=1.5, hp=80.0,
                             lp=1000.0)]}),
              "a cello's low bowed rumble slowed to 0.6 (a massive structure bending) muffled at 300 Hz, the "
              "heartbeat slower under it (0.42): darker and more of a drone than a, the throb further down; once a "
              "loop a church choir vocalising, slowed to half and far off (the Choir), and a ferry's mooring rope "
              "creaking on its cleat, slowed (the racks): 24.0 s loop"),
    }),
    "caldera-rumble": ("loop", -38.0, -1.0, -27.0,
                       "Level 28 from the antechamber (section 4, t=80) through the arena: the vent's rumble swelling "
                       "over the hatchery bed (the near-silence before the Choir at t=97.5), under the fight, cut at "
                       "the kill",
                       "the vent under the caldera: a deep, hot, steady roar of water, closer and heavier than the "
                       "hatchery, swelling now and then; no bubbles, no hiss-pops, a seamless loop", {
        "a": (([Loop(31566, 2.0, speed=0.5, hp=25.0, lp=700.0),
                Loop(849934, 60.0, gain=-4.0, speed=0.35, hp=25.0, lp=400.0)], 16.0, 1.0),
              "a foundry furnace's roar slowed to half and muffled at 700 Hz over the bottom-of-the-gorge waterfall "
              "(the vent field's bed) slowed further to 0.35: a hot, close furnace roar, 16.0 s loop"),
        "b": (([Loop(379905, 8.0, speed=0.3, hp=25.0, lp=380.0)], 16.0, 1.0,
               {"events": [L(369485, 4.8, 8.0, gain=-4.0, at=3.0, speed=0.6, fin=1.5, fout=3.0, hp=25.0,
                             lp=700.0)]}),
              "Niagara's roar slowed to 0.3 and muffled at 380 Hz, and once a loop a deep-sea earthquake (a "
              "hydrophone at 900 m) slowed to 0.6 swelling under it: lower and more of a water rumble than a, with a "
              "heave, 16.0 s loop"),
    }),
    "shaft-climb": ("shot", -24.0, -4.0, -20.0,
                    "Level 28 after the kill, once a run (t=105-115, K+5 to K+15): the climb up the Vrell drill "
                    "shaft under the ambience alone (the music silent), Okafor's \"up the shaft\" at ≈ K+8.6; "
                    "the breakout follows at its end",
                    "a long rush rising with the climb: the water roaring past, its pitch and brightness climbing "
                    "as the pressure eases and the light grows, the ice ceiling groaning and cracking ahead in its "
                    "second half; no bubbles", {
        "a": ([L(849934, 100.0, 10.0, speed=(0.3, 0.75), fin=1.0, fout=1.5, hp=25.0, lp=1400.0, swell=(-14.0, 1.0)),
               L(737896, 8.0, 5.0, gain=-5.0, at=4.5, speed=0.6, fin=0.2, fout=1.5, hp=40.0, lp=2500.0)],
              "the waterfall's roar gliding up from 0.3 to 0.75 and swelling, muffled at 1.4 kHz, and from 4.5 s a "
              "frozen lake's deep fracture booms (a real lake, 15 cm of ice) slowed to 0.6: a climbing rush with "
              "the ice booming ahead, 10.0 s"),
        "b": ([L(379905, 3.0, 10.0, speed=(0.3, 0.7), fin=1.0, fout=1.5, hp=25.0, lp=1200.0, swell=(-14.0, 1.0)),
               L(271008, 14.0, 6.0, gain=-4.0, at=3.5, speed=0.5, fin=0.5, fout=1.5, hp=40.0, lp=2000.0)],
              "Niagara's roar gliding up from 0.3 to 0.7 and swelling, and from 3.5 s a glacier's ice grinding "
              "against itself (Glacier Bay, Alaska) slowed to half: rounder than a, the ice groaning rather than "
              "booming, 10.0 s"),
    }),
    "maw-arrival": ("shot", -17.0, -2.0, -13.0,
                    f"{ARENA}: once, the Maw's arrival after the boss warning, the coil rising round the edges over "
                    "3 s and the head lifting last, jaw open (≈ 6 s)",
                    "a vast, slow, subsonic groan, a whale or a ship's hull under stress pitched down, with a long "
                    "reverb tail; huge and far below; no bubbles", {
        "a": ([L(343682, 3.6, 6.0, speed=(0.95, 0.7), fin=0.4, fout=2.0, hp=40.0, lp=1800.0),
               L(369485, 4.9, 6.0, gain=-9.0, speed=0.5, fin=0.3, fout=2.5, hp=20.0, lp=600.0)],
              "a blue whale's B call (a hydrophone at 900 m, recorded at 5×) slowed and sinking (0.95 to 0.7) over a "
              "deep-sea earthquake's rumble slowed to half, through a 3 s reverb: a long, moaning, rising-and-falling groan "
              "over a heave of the sea floor"),
        "b": ([L(681896, 12.0, 6.0, speed=3.0, fin=0.4, fout=2.0, hp=40.0, lp=1200.0),
               L(529620, 0.1, 6.0, gain=-4.0, speed=0.8, fin=0.2, fout=2.5, hp=30.0, lp=900.0),
               L(452107, 33.3, 4.0, gain=-4.0, at=0.8, speed=0.35, fin=0.2, fout=2.0, hp=50.0, lp=1200.0)],
              "a blue whale's D calls (real speed sub-bass, sped to 3 to be heard) sweeping down, a bowed cello's "
              "low structure groan and a ship's hull creak slowed to 0.35, through the same reverb: two falling "
              "groans, more of a hull straining than a"),
    }),
    "maw-gill": ("shot", -24.0, -6.0, -20.0,
                 f"{ARENA}, phases 1-2: each gill sac's 0.6 s swell before its ring of 10 (phase 1 a sac every "
                 "1.6 s, phase 2 every 2.4 s; a destroyed sac's turn is skipped), so often, under the fire",
                 "a deep pressure thump with a soft exhale: like a heartbeat under water, a breath going out; "
                 "never a bubble, nothing that pops", {
        "a": ([L(863689, 14.24, 0.8, speed=0.55, fin=0.005, fout=0.4, hp=25.0, lp=300.0),
               L(392307, 0.95, 0.8, gain=-5.0, at=0.12, speed=0.55, fin=0.12, fout=0.4, hp=80.0, lp=1000.0)],
              "the real heartbeat's double thump slowed to 0.55, then a horse's snort slowed and muffled at 1 kHz: "
              "a 'dum-dum, hhhff', 0.9 s"),
        "b": ([L(400997, 3.05, 0.8, fin=0.004, fout=0.4, hp=30.0, lp=400.0),
               L(824087, 1.35, 0.9, gain=-4.0, at=0.1, speed=0.6, fin=0.06, fout=0.45, hp=60.0, lp=900.0)],
              "a deep thump, then a sea blowhole breathing out through the rock, slowed to 0.6 and muffled: one "
              "heavier 'thoom' and a longer, airier exhale than a, 1.0 s"),
    }),
    "maw-throat": ("shot", -22.0, -5.0, -18.0,
                   f"{ARENA}: the throat's glow before each attack from the jaw: the phase-1 fan (0.6 s, the jaw "
                   "open every 5 s) and the phase-2 fan wall (0.8 s, every 4 s)",
                   "a rising low drone with a growl in it: the throat charging, building into the shot; animal, "
                   "deep, short", {
        "a": ([L(439280, 2.95, 0.9, speed=(0.45, 0.7), fin=0.08, fout=0.15, hp=40.0, lp=1500.0, swell=(-16.0, 1.0)),
               L(567148, 5.0, 0.9, gain=-6.0, speed=(1.0, 1.6), fin=0.1, fout=0.15, hp=25.0, lp=400.0,
                 swell=(-14.0, 1.0))],
              "a caged tiger's growl slowed and gliding up (0.45 to 0.7), swelling, over a steel mill's low drone "
              "rising with it: a growl climbing into the shot, 0.9 s"),
        "b": ([L(816640, 7.9, 0.9, speed=(0.4, 0.62), fin=0.08, fout=0.15, hp=50.0, lp=1400.0, swell=(-16.0, 1.0)),
               L(763026, 2.75, 0.9, gain=-3.0, speed=(0.45, 0.6), fin=0.08, fout=0.15, hp=50.0, lp=1500.0,
                 swell=(-14.0, 1.0))],
              "the cave's throat singing slowed and gliding up (0.4 to 0.62) with a black bear's angry growl "
              "(recorded in the wild) slowed in it: more of a sung drone than a, the growl under it, 0.9 s"),
    }),
    "maw-sweep-groan": ("shot", -21.0, -4.0, -17.0,
                        f"{ARENA}, phases 2-3: each coil sweep's tell (1.2 s; easy 1.5, hard 0.9), silt streaming "
                        "off the bight's plates and its glow line brightening (a sweep every 7 s, phase 3 every 6 s); "
                        "the swing follows (maw-sweep-swing)",
                        "a grinding groan of plates: the coil's armour straining before it swings, ice or a hull "
                        "creaking, slowed and heavy", {
        "a": ([L(271008, 15.0, 1.2, speed=(0.45, 0.6), fin=0.1, fout=0.2, hp=40.0, lp=1200.0, swell=(-12.0, 1.0)),
               L(452107, 33.35, 1.2, gain=-3.0, speed=0.4, fin=0.06, fout=0.25, hp=40.0, lp=900.0)],
              "the glacier's ice grinding against itself, slowed and rising (0.45 to 0.6), with a ship's hull creak "
              "slowed to 0.4: an icy grinding groan, 1.2 s"),
        "b": ([L(529620, 3.2, 1.2, speed=0.55, fin=0.1, fout=0.2, hp=25.0, lp=700.0, swell=(-10.0, 1.0)),
               L(452107, 50.65, 1.2, gain=-3.0, speed=0.35, fin=0.03, fout=0.25, hp=60.0, lp=1200.0)],
              "the cello's low structure groan slowed to 0.55 with another of the hull's deep creaks slowed to "
              "0.35: deeper and woodier than a, no ice in it, 1.2 s"),
    }),
    "maw-sweep-swing": ("shot", -19.0, -3.0, -15.0,
                        f"{ARENA}, phases 2-3: each coil sweep's swing right after its groan (in 0.5 s, held 0.8 s; "
                        "it hurts: `heavy` 10 and a push)",
                        "a heavy swish of displaced water: a huge body moving fast, a deep 'whoom' with weight; no "
                        "splash, no bubbles", {
        "a": ([L(772895, 7.3, 1.0, speed=0.6, fin=0.03, fout=0.4, hp=40.0, lp=900.0),
               L(147876, 0.62, 1.0, gain=-8.0, at=0.15, fin=0.01, fout=0.6, hp=25.0)],
              "a hydrophone's under-water pass-by slowed to 0.6 and muffled, a depth bomb's low boom under it: a "
              "deep 'whoomm', 1.15 s"),
        "b": ([L(481896, 3.1, 1.0, speed=0.5, fin=0.03, fout=0.4, hp=40.0, lp=800.0),
               L(400997, 0.22, 0.8, gain=-6.0, at=0.2, fin=0.004, fout=0.4, hp=30.0, lp=600.0)],
              "a tarp whooshed (the Abyss Ray's glide's source) slowed to half and muffled, a deep thump in it: "
              "drier and more of a sweep than a, 1.0 s"),
    }),
    "maw-shed": ("shot", -23.0, -5.0, -19.0,
                 f"{ARENA}: each Eel Swarm shed, as the coil plate's 1.0 s lime flare ends and the eel slips out "
                 "(phase 1 every 10 s, phase 2 every 12, phase 3 every 8)",
                 "a wet tearing slide, short: a plate peeling open and something long slipping out; no squelch, "
                 "no pop", {
        "a": ([L(847210, 3.55, 0.7, speed=0.5, fin=0.01, fout=0.25, hp=80.0, lp=2500.0),
               L(772895, 15.6, 0.7, gain=-7.0, speed=0.8, fin=0.05, fout=0.3, hp=60.0, lp=900.0)],
              "birch bark torn from a branch, slowed to half and muffled at 2.5 kHz, a hydrophone's slosh past the "
              "microphone under it: a fibrous 'krrrsh' sliding off, 0.7 s"),
        "b": ([L(484178, 37.05, 0.7, speed=0.6, fin=0.01, fout=0.25, hp=80.0, lp=2500.0),
               L(59966, 1.15, 0.6, gain=-6.0, at=0.05, speed=0.5, fin=0.01, fout=0.25, hp=80.0, lp=2000.0)],
              "a husk peeled off a corn cob (close, a Schoeps microphone) slowed to 0.6, with sole leather tearing "
              "on a cobbler's wheel slowed in it: wetter and more of a peel than a, 0.7 s"),
    }),
    "maw-dive": ("shot", -18.0, -3.0, -14.0,
                 f"{ARENA}, phase 2: once, as the head falls below 50 %: the roar and the coil sliding off the "
                 "edges into the dark (2 s) while the music drops away; the 8 s of darkness follow",
                 "a long falling roar and a pressure swell: the whole coil sinking, the sound dropping with it "
                 "into the deep; no bubbles", {
        "a": ([L(849934, 120.0, 3.5, speed=(0.6, 0.25), fin=0.15, fout=1.5, hp=25.0, lp=900.0),
               L(681896, 21.0, 3.5, gain=-3.0, speed=1.6, fin=0.2, fout=1.5, hp=25.0, lp=800.0),
               L(147880, 3.38, 2.5, gain=-6.0, at=0.5, fin=0.02, fout=1.5, hp=25.0)],
              "the waterfall's roar falling (0.6 to 0.25), a blue whale's D call sweeping down under it and a depth "
              "bomb's pressure boom, through a 2.5 s reverb: a roar sinking away, 5.0 s with its tail"),
        "b": ([L(271008, 73.4, 3.5, speed=(0.6, 0.3), fin=0.05, fout=1.5, hp=30.0, lp=1500.0),
               L(369485, 5.0, 3.5, gain=-3.0, speed=(0.8, 0.4), fin=0.1, fout=1.5, hp=20.0, lp=600.0)],
              "the glacier's calving roar (the ice splitting apart) falling from 0.6 to 0.3 over the deep-sea "
              "earthquake falling with it, the same reverb: rougher and more of a collapse than a"),
    }),
    "maw-lunge": ("shot", -17.0, -2.0, -13.0,
                  f"{ARENA}, phase 2: once, after the dive: the head bursting up through the play plane and racing "
                  "up the column in 0.8 s, jaws open (after the 2 s shadow tell; it hurts: `heavy` 10)",
                  "a rushing surge and a jaw snap: water tearing past, then a heavy bite; big, no click", {
        "a": ([L(772895, 7.2, 1.0, speed=(0.5, 1.0), fin=0.05, fout=0.1, hp=40.0, lp=1200.0, swell=(-14.0, 1.0)),
               L(535616, 7.38, 0.8, at=0.8, speed=0.45, fin=0.003, fout=0.4, hp=60.0, lp=2000.0),
               L(147876, 0.66, 1.2, gain=-5.0, at=0.78, fin=0.005, fout=0.7, hp=25.0)],
              "the hydrophone's pass-by surging up (0.5 to 1.0), then a dog's jaws snapping shut on a treat slowed "
              "to 0.45 on a depth bomb's boom: a 'whoooOSH-CHOMP', 2.0 s (the Glow Angler's lunge made huge)"),
        "b": ([L(379905, 5.0, 1.0, speed=(0.35, 0.9), fin=0.05, fout=0.1, hp=30.0, lp=1200.0, swell=(-16.0, 1.0)),
               L(400997, 5.98, 0.8, at=0.8, speed=0.7, fin=0.003, fout=0.4, hp=40.0, lp=1500.0),
               L(147873, 1.04, 1.2, gain=-5.0, at=0.78, fin=0.005, fout=0.7, hp=25.0)],
              "Niagara's roar surging up (0.35 to 0.9), then a huge gate's deep closing thump slowed to 0.7 on a "
              "depth bomb: a broader rush and a duller, heavier bite than a, 2.0 s"),
    }),
    "implant-spark": ("shot", -22.0, -5.0, -18.0,
                      f"{ARENA}, phase 3: each Helix implant's 1.0 s tell (neck, left, right in turn), sparks and an "
                      "amber flicker under the cracked casing, then its plate blowing aside and the core open",
                      "electrical crackle and an arcing buzz, a machine's sound, not a creature's (the clue), "
                      "ending in a hard metal clunk as the plate lets go; no bubbles", {
        "a": ([L(396240, 0.72, 0.36, fin=0.01, fout=0.05, hp=200.0, lp=4000.0),
               L(396240, 1.56, 0.36, at=0.32, fin=0.03, fout=0.05, hp=200.0, lp=4000.0),
               L(396240, 2.24, 0.4, at=0.64, fin=0.03, fout=0.1, hp=200.0, lp=4000.0),
               L(407301, 5.02, 0.4, gain=-3.0, at=0.98, speed=0.7, fin=0.003, fout=0.2, hp=60.0, lp=2500.0)],
              "an arc welder's sparks (a real welding machine), three bursts run together and muffled at 4 kHz, then a heavy "
              "steel door's slam slowed: crackling sparks and a 'CLANK', 1.4 s"),
        "b": ([L(675760, 2.0, 1.0, fin=0.01, fout=0.15, hp=150.0, lp=4000.0),
               L(751170, 12.72, 0.4, gain=-3.0, at=0.98, speed=0.6, fin=0.003, fout=0.2, hp=60.0, lp=2500.0)],
              "a Tesla coil sparking and humming (a vintage film library recording) muffled at 4 kHz, then a heavy "
              "file cabinet's metal impact slowed: more hum and buzz than a's crackle, 1.4 s"),
    }),
    "implant-zap": ("shot", -22.0, -5.0, -19.0,
                    f"{ARENA}, phase 3: each discharge of an open implant (every 1.5 s while one is open): the 0.4 s "
                    "amber arc crawling over the housing, then its burst of 3 electric bolts",
                    "a short arcing build and a hard electric zap: high voltage, dry and mechanical, not a laser "
                    "and not a creature", {
        "a": ([L(415962, 1.55, 0.45, speed=0.8, fin=0.05, fout=0.05, hp=100.0, lp=5000.0, swell=(-12.0, 1.0)),
               L(415962, 7.33, 0.4, at=0.4, fin=0.003, fout=0.25, hp=60.0, lp=5000.0)],
              "a Tesla coil's rotary spark-gap discharge building for 0.45 s, then one of its loudest cracks: a "
              "'zzzzt-KRAK', 0.8 s"),
        "b": ([L(19486, 0.2, 0.45, speed=0.8, fin=0.05, fout=0.05, hp=100.0, lp=5000.0, swell=(-12.0, 1.0)),
               L(403252, 1.66, 0.4, at=0.4, fin=0.003, fout=0.25, hp=60.0, lp=5000.0)],
              "a Jacob's ladder's arc climbing (slowed to 0.8), then a high-voltage taser's discharge: thinner and "
              "snappier than a, 0.8 s"),
    }),
    "maw-death": ("shot", -15.0, -1.5, -11.0,
                  f"{ARENA}: once, the kill: the three implants blowing out 0.4 s apart, the coil convulsing and "
                  "sinking segment by segment, the head last into the vent, whose glow flares (the 4 s chain; the "
                  "screen flash and the credit shower follow; the Choir's fragment at K+3)",
                  "three heavy electrical cracks, a long dying groan, a deep detonation-like pressure boom as the "
                  "vent flares; the biggest sound of the act; no bubbles", {
        "a": ([L(415962, 7.33, 0.6, at=0.01, speed=0.7, fin=0.005, fout=0.3, hp=60.0, lp=4000.0),
               L(415962, 9.42, 0.6, gain=-1.0, at=0.4, speed=0.7, fin=0.003, fout=0.3, hp=60.0, lp=4000.0),
               L(415962, 10.67, 0.6, gain=-2.0, at=0.8, speed=0.7, fin=0.003, fout=0.3, hp=60.0, lp=4000.0),
               L(681896, 31.0, 4.0, gain=-2.0, at=1.0, speed=2.5, fin=0.3, fout=1.5, hp=50.0, lp=1200.0),
               L(147876, 0.62, 3.0, gain=-3.0, at=3.4, fin=0.01, fout=2.0, hp=35.0)],
              "three Tesla-coil cracks slowed to 0.7, a blue whale's D call sweeping down (sped to 2.5) and a depth "
              "bomb's boom at 3.4 s, through a 3 s reverb: crack-crack-crack, a falling moan, a deep boom, 8.2 s "
              "with its tail"),
        "b": ([L(403252, 0.78, 0.5, speed=0.7, fin=0.003, fout=0.3, hp=60.0, lp=4000.0),
               L(403252, 1.66, 0.5, gain=-1.0, at=0.4, speed=0.7, fin=0.003, fout=0.3, hp=60.0, lp=4000.0),
               L(403252, 2.25, 0.5, gain=-2.0, at=0.8, speed=0.7, fin=0.003, fout=0.3, hp=60.0, lp=4000.0),
               L(343682, 14.0, 4.0, gain=-2.0, at=1.0, speed=(1.0, 0.6), fin=0.3, fout=1.5, hp=60.0, lp=1800.0),
               L(82682, 6.92, 3.0, gain=-4.0, at=3.4, speed=0.8, fin=0.01, fout=2.0, hp=50.0, lp=2000.0)],
              "three taser discharges slowed to 0.7, a blue whale's B call sinking (1.0 to 0.6) and a "
              "granite quarry blast slowed to 0.8 and muffled, the same reverb: a longer, more voiced groan than a and a "
              "rolling blast instead of a boom, 8.2 s with its tail"),
    }),
    "breakout": ("shot", -16.0, -2.0, -12.0,
                 "Level 28 at t=115 (K+15), once a run: the ice ceiling shattering in a white burst, slabs tumbling "
                 "out of the field, the medium switching to air; Europa's night-ice wind (`europa-ice`) from here",
                 "ice cracking and tearing (calving), a huge crash, a roar of water and air bursting into the "
                 "open: the biggest open sound after an act of muffled water", {
        "a": ([L(271008, 73.6, 4.5, speed=0.75, fin=0.01, fout=1.5, hp=30.0, lp=8000.0),
               L(113679, 18.8, 1.5, gain=-3.0, speed=0.6, fin=0.002, fout=0.8, hp=60.0),
               L(376799, 18.3, 4.0, gain=-5.0, at=0.5, speed=0.8, fin=0.3, fout=1.5, hp=60.0)],
              "a glacier splitting apart with a roar (Glacier Bay, Alaska) slowed to 0.75, a large ice sheet's "
              "crack stomped open slowed to 0.6 on top, and big waves crashing into rocks behind it: a calving "
              "roar with a wash of water, 4.5 s"),
        "b": ([L(737896, 22.9, 3.0, speed=0.6, fin=0.002, fout=1.5, hp=30.0, lp=6000.0),
               L(440858, 7.6, 3.0, gain=-3.0, speed=0.6, fin=0.002, fout=1.0, hp=60.0),
               L(849934, 150.0, 4.0, gain=-5.0, at=0.4, speed=0.9, fin=0.4, fout=1.5, hp=40.0)],
              "the frozen lake's biggest fracture boom slowed to 0.6, a sheet of ice breaking underfoot slowed "
              "with it, and the waterfall's roar unmuffled behind: a sharper boom-and-shatter than a, the water "
              "roaring in the open, 4.4 s"),
    }),
    "cruiser-rise": ("shot", -19.0, -3.0, -15.0,
                     "Level 28 at t=118 (K+18), once a run: the two Ascendancy cruisers rising through the ice on "
                     "both flanks (4 s), ice sheeting off their hulls, under the reveal sting (track 30)",
                     "a deep engine thrum swelling and ice grinding and sheeting off a hull: two huge machines "
                     "coming up; in the air now, heavy but not muffled", {
        "a": ([L(74914, 5.0, 5.0, speed=(0.35, 0.5), fin=0.5, fout=0.8, hp=25.0, lp=1500.0, swell=(-15.0, 1.0)),
               L(94167, 17.5, 4.5, gain=-3.0, at=0.3, speed=0.6, fin=0.3, fout=1.0, hp=60.0, lp=5000.0)],
              "a ferry's engine room slowed (0.35 to 0.5) and swelling, under a boat breaking through harbour ice "
              "slowed to 0.6: a thrumming engine and crunching, grinding ice, 5.0 s"),
        "b": ([L(451398, 20.0, 5.0, speed=(0.3, 0.45), fin=0.5, fout=0.8, hp=25.0, lp=1500.0, swell=(-15.0, 1.0)),
               L(271008, 16.0, 4.5, gain=-3.0, at=0.3, speed=0.6, fin=0.3, fout=1.0, hp=40.0, lp=4000.0),
               L(268023, 15.0, 2.0, gain=-8.0, at=2.0, speed=0.6, fin=0.01, fout=0.8, hp=60.0)],
              "a fishing boat's throaty engine slowed (0.3 to 0.45) and swelling, the glacier's ice grinding "
              "against itself and a sheet of ice cracking: a rougher engine and a slower grind than a, 5.0 s"),
    }),
    "cruiser-broadside": ("shot", -18.0, -3.0, -14.0,
                          f"{RUN}: each broadside, a cruiser's 7 shells crossing a 70 px band after the 1.2 s "
                          "tell (one every 3.0 s, the flanks in turn; from 20 s two bands at once)",
                          "a big naval gun volley: a ripple of heavy guns firing down the hull, deep booms with a "
                          "long rolling tail", {
        "a": ([L(161341, 0.09, 1.2, gain=g, at=at, speed=sp, fin=0.002, fout=0.8, hp=30.0) for at, sp, g in SHOTS],
              "a howitzer's shot (US Army footage) seven times down the hull, 0.07-0.08 s apart, each slowed a "
              "little differently (0.85 to 1.0): a rippling 'BA-BA-BA-BA-BOOM', 1.65 s"),
        "b": ([L(162365, 2.12, 2.2, fin=0.003, fout=1.0, hp=30.0),
               L(67516, 0.95, 0.7, gain=-4.0, at=0.1, fin=0.002, fout=0.4, hp=40.0),
               L(67516, 1.76, 0.7, gain=-5.0, at=0.25, fin=0.002, fout=0.4, hp=40.0),
               L(67516, 2.99, 0.7, gain=-5.0, at=0.42, fin=0.002, fout=0.4, hp=40.0)],
              "a battleship's turret firing (US Navy footage: one deep main-gun boom) with three howitzer shots "
              "rippling over it: one huge low blast and a crackle of guns, more weight and less ripple than a, "
              "2.2 s"),
    }),
    "cruiser-flak": ("shot", -21.0, -4.0, -17.0,
                     f"{RUN}: each flak burst, at a marker near the ship after its 1.0 s, a ring of 8 (two markers "
                     "every 4 s; hard every 3 s, easy one marker)",
                     "an airburst: a sharp, hollow high explosion with a short rolling echo, lighter than the "
                     "broadside", {
        "a": ([L(640817, 4.95, 1.15, fin=0.003, fout=0.6, hp=50.0)],
              "a firework shell's full burst recorded 1.2 km away (unprocessed): a deep, hollow 'BOOMF' with its "
              "echo, 1.15 s"),
        "b": ([L(67516, 3.38, 0.4, speed=1.1, fin=0.002, fout=0.2, hp=200.0),
               L(640817, 4.28, 0.85, gain=-2.0, at=0.03, speed=0.75, fin=0.005, fout=0.5, hp=40.0)],
              "a howitzer shot's crack sped to 1.1 and thinned, on the firework's other burst slowed to 0.75: a "
              "sharper crack and a lower boom than a, 0.9 s"),
    }),
    "cruiser-pd": ("shot", -23.0, -5.0, -19.0,
                   f"{RUN}: each point-defence burst of 3 from a cruiser's turret line (each cruiser every 2.5 s; "
                   "hard bursts of 4)",
                   "rapid autocannon: three quick heavy shots, lighter than the flak", {
        "a": ([L(854186, 0.22, 0.8, fin=0.003, fout=0.25, hp=60.0)],
              "a 25 mm Bushmaster autocannon's three-shot burst (US Marines footage) as recorded: a 'dak-dak-dak', "
              "0.8 s"),
        "b": ([L(163119, 0.08, 0.8, speed=0.9, fin=0.005, fout=0.3, hp=60.0)],
              "a Navy Phalanx CIWS's fast burst slowed to 0.9: a ripping buzz of rounds rather than three shots, "
              "0.8 s"),
    }),
}


def layers_of(kind, spec):
    if kind == "synth":
        return []
    if kind != "loop":
        return spec
    extra = spec[3] if len(spec) > 3 else {}
    return list(spec[0]) + list(extra.get("events", []))


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def reverberated(x, seconds, wet, damping, seed):
    """The mix through a stereo reverb, its tail kept for 60 % of the reverb's length, the last 0.5 s faded out."""
    n = x.shape[1] + int(0.6 * seconds * SR)
    y = reverb(x, seconds=seconds, mix=wet, damping=damping, seed=seed)[:, :n]
    k = int(0.5 * SR)
    y[:, -k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return y


def build(subject, v):
    kind, band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = bed(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest:
        # the encoder's edges then make no step at the join
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = mix(spec)
        if subject in REVERB:
            raw = reverberated(raw, *REVERB[subject])
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out



LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind == "loop":
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz), at most {num(cap)} LUFS"
    rev = ""
    if subject in REVERB:
        seconds, wet = REVERB[subject][:2]
        rev = f"the mix through a {num(seconds)} s reverb ({num(wet)} wet), "
    return (f"{rev}levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    one_shot = "length" in layer
    if not one_shot:
        length, xfade = spec[1], spec[2]
        span = (length + xfade) * speed
        parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
    else:
        span = layer["length"] * (np.mean(speed) if isinstance(speed, tuple) else speed)
        parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
    if isinstance(speed, tuple):
        parts.append(f"played at a speed gliding {num(speed[0])} → {num(speed[1])}")
    elif speed != 1.0:
        parts.append(f"slowed to {num(speed)}" if speed < 1 else f"sped to {num(speed)}")
    if layer.get("swell"):
        parts.append(f"swelling from {num(layer['swell'][0])} dB up to full over the layer")
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if layer["sound"] in SMOOTH:
        parts.append(f"each STFT bin's magnitude replaced by its {num(SMOOTH[layer['sound']][0])} ms running median "
                     "(short pops and flutter smoothed out, the phase kept)")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s" + (" in the loop (once a loop)" if kind == "loop" else ""))
    if kind == "loop":
        if not one_shot:
            parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the "
                         "start)")
        extra = spec[3] if len(spec) > 3 else {}
        if "pulse" in extra:
            parts.append(f"the loop pulsing every {num(extra['pulse'][0])} s ({num(extra['pulse'][1])} dB deep)")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers_of(kind, spec)) > 1:
        parts.append("mixed with the other layers")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(kind, spec):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(kind, spec)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        kind, _, _, _, use, should, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in layers_of(kind, spec):
            s = layer["sound"]
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, kind, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        lv = band_rms_db(x) if kind == "loop" else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if kind == 'loop' else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays at {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                    f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays at {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit.replace('-', '−')}.\n")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries in {outdir}")




def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[6]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
