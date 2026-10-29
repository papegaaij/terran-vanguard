#!/usr/bin/env python3
"""Concept round 68 (M9 part H "Level 35 Iron Sovereign", step AUD-H2): the Iron Sovereign's new sounds and Level 35's
mass-relay flash, a and b each (the boss's *Sounds brief*, design/enemies/bosses/iron-sovereign/README.md; the sfx
README's *Act 5 sounds*, its Iron Sovereign block and the rail line's row; the Level 35 document's *Music & ambience*).
Not proposed (used as they are): the ring bed (round 60 S.16 a, concept/sovereign-ring-turn-r60-a.ogg), the rail line's
charge and shot (round 60 S.7 b and S.8 b) as the rail batteries' and the core rail's, the hex shield set (round 60 S.1
a, S.2 b, S.3 a), the homing launch, run and kill (round 60 S.4 b, S.5 a, S.6 b) as the SAM batteries', the Sentinel's
sweep (round 65) as the laser batteries', the Dart's launch (round 63, concept/dart-launch-r63-a.ogg) as the rim bays'
(four in a row), and the `huge` rung under the death. The mass relay's charge and flash are the
rail line's *long version* (the rail line's row): the chosen charge's own two recordings stretched to the relay's 1.2 s
telegraph, and the chosen shot's three recordings with a long rolling thunder tail.

Every recorded layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 68 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), reversed (a layer
whose id is negative: the implosion's suck), layered, faded, filtered, swelled, levelled and, for the klaxons, played
into a real recorded space (convolved with a 300 m steel tunnel's impulse response, sfx_r49m.py's in_space). Nothing
is synthesized. The tools are sfx_r66.py's (preview, build, the --docs rows) plus sfx_r49m.py's in_space.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  sovereign-spinup-r68-*     ≈ 2.5 s: a ring starting to turn in the intro (outer → inner), a huge bearing grinding up
                             to speed
  sovereign-step-r68-*       ≈ 1.0-1.1 s: the inner ring unlocking for its 120° step (the 0.4 s tell), a ratchet clank
                             into the first clicks of the turn (the ring bed carries the turn)
  sovereign-hold-r68-*       ≈ 1.0 s: the inner ring locking into its hold, a heavy clunk
  sovereign-lock-r68-*       ≈ 2.6 s: a ring's last battery killed, the ring grinding to a halt in 2 s (brake
                             screech, groan, sparks, the stop's thud at 2.0 s)
  sovereign-klaxon-r68-*     ≈ 3.4 s: the intro's klaxon and the core's 3 s opening, a real alarm horn in a steel space
  sovereign-shutters-r68-*   ≈ 3.5 s: the core's six petals opening over 3 s, a hydraulic hiss and slide, the thud as
                             they reach open at ≈ 2.9 s
  sovereign-fan-r68-*        ≈ 0.75-0.9 s: the shut core's aimed `fan` of 5, a hard autocannon burst
  sovereign-spiral-r68-*     ≈ 0.15-0.2 s: one shot of the open core's `spiral`, a lighter gun crack (throttled)
  sovereign-cookoff-r68-*    ≈ 2.6-2.7 s: the death's cook-offs, 13 batteries ring by ring 0.15 s apart (6 outer, 4
                             middle, 3 inner), tearing metal under them
  sovereign-implosion-r68-*  ≈ 2.1-2.2 s: the core collapsing inward, a rising suck into a deep whoomph at 1.15 s (the
                             `huge` rung starts there)
  relay-charge-r68-*         1.2 s: the mass relay's telegraph, the rail charge's long version
  relay-flash-r68-*          ≈ 3.9-4.0 s: the mass relay's flash, the rail shot with a long rolling thunder tail

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap): the relay a little over the rail line's chosen pair (charge −25, shot −18), the klaxon as Level 29's
station klaxon (−22), the fan as round 65's Strider cannon, the spiral's shot well under it (it repeats), the
cook-offs and the implosion at the top of the ladder (under the `huge` rung's own level), the ring sounds between.

Usage: python3 tools/concept/audio/sfx_r68.py [subject-or-file ...]   e.g. sovereign-lock relay-flash-r68-b
       python3 tools/concept/audio/sfx_r68.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r43g import cut  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r68.py"
R = "r68"
ROUND = "round 68"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    67516: ('Howitzer Gun Shots', 'qubodup', CC0, P + "67/67516_71257-hq.ogg"),
    93100: ('Whip Crack 01.wav', 'CGEffex', BY4, P + "93/93100_1386366-hq.ogg"),
    101246: ("metal glass makes long 'PING!' sound.aif", 'Tomlija', BY3, P + "101/101246_1163166-hq.ogg"),
    101934: ('Dry thunder2.wav', 'juskiddink', BY4, P + "101/101934_649468-hq.ogg"),
    104882: ('Submarie dive horn_better.wav', 'CGEffex', BY4, P + "104/104882_1386366-hq.ogg"),
    117081: ('Metallic Screech.wav', 'astronaut_13579', BY3, P + "117/117081_1417012-hq.ogg"),
    119902: ('Camera Flash Charging.aif', 'ftpalad', CC0, P + "119/119902_1990698-hq.ogg"),
    160355: ('Air Defense Artillery Avenger Training .50cal (tH4YBTw8vk8).flac', 'qubodup', CC0,
             P + "160/160355_71257-hq.ogg"),
    169666: ('electricspark.wav', 'knova', BY4, P + "169/169666_3133255-hq.ogg"),
    175663: ('big_metal_unlock_and_slam.wav', 'simpsi', BY3, P + "175/175663_2762119-hq.ogg"),
    331357: ('Large hydraulic door', 'nooly', BY3, P + "331/331357_1999691-hq.ogg"),
    336004: ('Sharp Explosion 3 (of 5)', 'Rudmer_Rotteveel', CC0, P + "336/336004_4921277-hq.ogg"),
    336005: ('Sharp Explosion 2 (of 5)', 'Rudmer_Rotteveel', CC0, P + "336/336005_4921277-hq.ogg"),
    336006: ('Sharp Explosion 1 (of 5)', 'Rudmer_Rotteveel', CC0, P + "336/336006_4921277-hq.ogg"),
    336007: ('Multiple Deep Explosions (Noisy rec)', 'Rudmer_Rotteveel', CC0, P + "336/336007_4921277-hq.ogg"),
    336010: ('Sharp Explosion 5 (of 5)', 'Rudmer_Rotteveel', CC0, P + "336/336010_4921277-hq.ogg"),
    336011: ('Sharp Explosion 4 (of 5)', 'Rudmer_Rotteveel', CC0, P + "336/336011_4921277-hq.ogg"),
    386206: ('Ratchet - Stahlwille - Tighten 1', 'ldezem', CC0, P + "386/386206_1655965-hq.ogg"),
    406197: ('door metal big heavy close kinda slam thud echo offmic.wav', 'kyles', CC0,
             P + "406/406197_612689-hq.ogg"),
    453444: ('hum electric transformer in large room1.flac', 'kyles', CC0, P + "453/453444_612689-hq.ogg"),
    457294: ('Air (or steam) pressure release', 'brunoboselli', CC0, P + "457/457294_300738-hq.ogg"),
    482740: ('R03-30-Large Factory Machines Starts, Works, Stops.wav', 'craigsmith', CC0,
             P + "482/482740_2524442-hq.ogg"),
    496836: ('Metallic Groan', 'hinchinbrook', CC0, P + "496/496836_9847211-hq.ogg"),
    505113: ('Thunder, Close', 'Fission9', CC0, P + "505/505113_9395330-hq.ogg"),
    514981: ('Klaxon on axis Long.wav', 'jameswrowles', CC0, P + "514/514981_3874107-hq.ogg"),
    556714: ('Bending Metal', 'NachtmahrTV', CC0, P + "556/556714_5620304-hq.ogg"),
    616084: ('Rising electric hum.aif', 'jukkako', CC0, P + "616/616084_2981994-hq.ogg"),
    640998: ('Train Brakes', 'KodyTron', BY4, P + "640/640998_14125872-hq.ogg"),
    672776: ('Thunder Clap', 'DaniloSFX', CC0, P + "672/672776_1357949-hq.ogg"),
    683179: ('Fireball', 'NearTheAtmoshphere', CC0, P + "683/683179_2578040-hq.ogg"),
    732992: ('Huge Thunder Crack [XY + MS]', '-CASK-', BY4, P + "732/732992_13779022-hq.ogg"),
    755384: ('ratchet_winching_and_turning_sound_slow_speed', 'Artninja', BY4, P + "755/755384_10996917-hq.ogg"),
    812360: ('Rail Grinding Machine with Rythmic Clang Berlin Night', 'giuliorasi', CC0,
             P + "812/812360_14865205-hq.ogg"),
    814976: ('Closing large metal door-001', 'AudioPapkin', CC0, P + "814/814976_8698658-hq.ogg"),
    841192: ('Hydraulic Door', 'Autobot8299', CC0, P + "841/841192_18079032-hq.ogg"),
    846029: ('Balloon Pop, 300m Long Steel Tunnel (192khz IR Sample / Impulse Response)', 'modusmogulus', CC0,
             P + "846/846029_15956618-hq.ogg"),
    849772: ('Electric Sparks', 'mikiko850', BY4, P + "849/849772_18554592-hq.ogg"),
    852991: ('Hydraulic Hiss, TTC bus door', 'OttisG', BY4, P + "852/852991_18014982-hq.ogg"),
    854183: ('Autocannon Shot 1', 'qubodup', CC0, P + "854/854183_71257-hq.ogg"),
    854186: ('Autocannon Three Shot Burst', 'qubodup', CC0, P + "854/854186_71257-hq.ogg"),
}

_decoded = {}


def preview(sound):
    """The decoded HQ preview, fetched from the CDN into the cache when missing; a negative id is the same recording
    reversed (its `start` then counts from the reversed file's start)."""
    if sound not in _decoded:
        if sound < 0:
            _decoded[sound] = np.ascontiguousarray(preview(-sound)[:, ::-1])
            return _decoded[sound]
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        _decoded[sound] = decode(path)
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() reads its layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def in_space(dry, ir_sound, wet_db, length):
    """The dry mix plus its convolution with a real recorded impulse response (scaled to unit energy per channel,
    `wet_db` under the dry), cut to `length` s with a 0.35 s raised-cosine fade-out (sfx_r49m.py's)."""
    ir = preview(ir_sound)
    ir = ir - ir.mean(axis=1, keepdims=True)
    n = dry.shape[1] + ir.shape[1] - 1
    size = 1 << (n - 1).bit_length()
    wet = np.array([np.fft.irfft(np.fft.rfft(dry[c], size) * np.fft.rfft(ir[c] / np.sqrt((ir[c] ** 2).sum()), size),
                                 size)[:n] for c in range(2)])
    out = wet * 10 ** (wet_db / 20)
    out[:, :dry.shape[1]] += dry
    out = out[:, :int(length * SR)]
    k = int(0.35 * SR)
    out[:, -k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return out


def chain(onsets, at0=0.0, gap=0.15, length=0.5, fout=0.35, gains=None):
    """The cook-off chain: one layer per battery, `gap` s apart; `onsets` [(sound, start, speed)] in firing order."""
    gains = gains or [0.0] * len(onsets)
    return [L(s, start, length, gain=g, at=at0 + k * gap, speed=sp, fin=0.001, fout=fout, hp=40.0)
            for k, ((s, start, sp), g) in enumerate(zip(onsets, gains))]


# the cook-offs, ring by ring: 6 SAM batteries (outer, sped up: lighter), 4 rail (middle), 3 laser (inner, slowed:
# heavier); a: the five sharp explosions in turn, b: the deep explosions of one recording (their onsets)
SHARP = [336006, 336011, 336004, 336005, 336010]
SPEEDS = [1.2] * 6 + [1.0] * 4 + [0.8] * 3
GAINS = [-3.0] * 6 + [-1.5] * 4 + [0.0] * 3
COOK_A = [(SHARP[k % 5], 0.0, SPEEDS[k]) for k in range(13)]
DEEP = [0.45, 1.80, 2.40, 4.45, 5.33, 6.04, 7.64, 9.16, 11.68, 12.55, 12.99, 0.45, 5.33]
COOK_B = [(336007, DEEP[k], SPEEDS[k]) for k in range(13)]

# subject -> (level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (layers, what[, space])}); space = (impulse-response sound, wet dB, total length s). Levelled by
#             the loudest 100 ms of the 200 Hz-5 kHz band, at most `cap` LUFS.
BOSS = "../../enemies/bosses/iron-sovereign/README.md"
L35 = "../../campaign/act-5-the-belt/level-35-iron-sovereign/README.md"

SOUNDS = {
    "sovereign-spinup": (-26.0, -3.0, -22.0, "Iron Sovereign: a ring's spin-up",
                         "the boss's ≈ 8 s intro: each ring starting to turn as its lights come on, outer → middle → "
                         "inner (three times, a few seconds apart), panned to nothing (the station fills the field); "
                         "the ring bed (concept/sovereign-ring-turn-r60-a.ogg) takes over once the rings reach speed",
                         "a huge bearing grinding up to speed: a crane's slewing ring or a turntable, pitched down, a "
                         "rising rumble", f"{BOSS}#sounds-brief", {
        "a": ([L(812360, 120.0, 2.5, speed=(0.15, 0.5), fin=0.05, fout=0.35, hp=40.0, lp=2500.0, swell=(-18.0, 0.8)),
               L(175663, 0.9, 0.6, gain=-7.0, speed=0.7, fin=0.002, fout=0.35, hp=60.0)],
              "the ring bed's own recording (a rail-grinding machine with its rhythmic clang) glided from 0.15 up to "
              "the bed's 0.5 and swelling, a big latch's clank as it lets go: 'KLANK ... grrrrRRRR', the clangs "
              "speeding up into the bed's ticks, 2.5 s"),
        "b": ([L(482740, 0.05, 2.5, speed=(0.45, 0.9), fin=0.01, fout=0.35, hp=40.0, lp=5000.0, swell=(-10.0, 1.0))],
              "a big factory machine starting up, an octave down rising to near its own pitch: a rougher, airier "
              "motor whine winding up 'vvvoooOOOMM' than a, no clangs, 2.5 s"),
    }),
    "sovereign-step": (-24.0, -4.0, -22.0, "Iron Sovereign: the inner ring's step",
                       "the inner ring unlocking for each 120° step (every 8 s: 4 s turning, 4 s holding; phase 2 "
                       "3.2 s, phase 3 2.7 s turning): with the 0.4 s tell (its gold teeth flash), the turn's first "
                       "clicks under it; the ring bed carries the turn, the hold's clunk below ends it",
                       "a heavy ratchet clank, then the grind starting: a giant pawl lifting off a toothed rim",
                       f"{BOSS}#tells", {
        "a": ([L(175663, 0.9, 0.6, speed=0.75, fin=0.002, fout=0.3, hp=50.0),
               L(755384, 0.2, 0.8, gain=-4.0, at=0.3, speed=0.55, fin=0.05, fout=0.45, hp=80.0)],
              "a big metal latch unlocked, slowed to 0.75, then a winch's ratchet turning, slowed to 0.55: "
              "'KLA-KLANK, tak-tak-tak', 1.1 s"),
        "b": ([L(406197, 0.3, 0.8, gain=-2.0, speed=0.8, fin=0.002, fout=0.4, hp=40.0),
               L(386206, 0.1, 0.9, at=0.12, speed=0.45, fin=0.002, fout=0.3, hp=60.0)],
              "a heavy steel door's slam, slowed to 0.8, then a socket ratchet tightening slowed to 0.45 (each "
              "click a big clack): a deeper 'DOMM, krrk-krrk-krrk' than a, 1.0 s"),
    }),
    "sovereign-hold": (-22.0, -4.0, -18.0, "Iron Sovereign: the inner ring's hold",
                       "the inner ring locking into its hold at the end of each 120° step (then 4 s still while its "
                       "laser batteries sweep)",
                       "a heavy clunk: the giant pawl dropping back into the rim", f"{BOSS}#tells", {
        "a": ([L(175663, 5.35, 1.0, speed=0.85, fin=0.002, fout=0.5, hp=40.0)],
              "a big metal bolt slammed home, slowed to 0.85: a hard ringing 'KLANNG', 1.0 s"),
        "b": ([L(814976, 0.05, 1.0, speed=0.75, fin=0.003, fout=0.5, hp=30.0)],
              "a large metal door closing, slowed to 0.75: a deeper, duller 'DOOMF' than a, less ring, 1.0 s"),
    }),
    "sovereign-lock": (-21.0, -3.0, -16.0, "Iron Sovereign: a ring locked",
                       "a ring's last battery killed (three times a fight, in any order; a `boss-phase`): the ring "
                       "grinds to a halt in 2 s, sparks, its lights going out, then stays still; the bed goes on for "
                       "the rings still turning",
                       "a long metal brake screech and a groan as the ring grinds to a halt, sparks crackling; the "
                       "stop at 2 s", f"{BOSS}#phases", {
        "a": ([L(640998, 2.75, 2.0, speed=(1.0, 0.6), fin=0.05, fout=0.4, hp=300.0),
               L(812360, 120.0, 2.2, gain=-4.0, speed=(0.5, 0.12), fin=0.02, fout=0.6, hp=40.0, lp=2500.0),
               L(496836, 4.4, 2.4, gain=-7.0, speed=0.7, fin=0.3, fout=0.6, hp=60.0),
               L(849772, 3.0, 1.2, gain=-13.0, at=0.2, fin=0.02, fout=0.5, hp=1500.0),
               L(406197, 0.3, 0.6, gain=-4.0, at=2.0, speed=0.7, fin=0.002, fout=0.3, hp=40.0)],
              "a train's brakes screeching, glided down from its own pitch to 0.6 (slowing), over the ring bed's own "
              "grind winding down from 0.5 to 0.12, a metal groan and sparks crackling, a heavy door's thud as it "
              "stops at 2.0 s: 'SKREEEEeeeoww ... DOMM', 2.6 s"),
        "b": ([L(117081, 0.0, 1.1, speed=(0.85, 0.7), fin=0.02, fout=0.25, hp=200.0),
               L(117081, 2.2, 1.0, at=0.95, speed=(0.7, 0.5), fin=0.1, fout=0.5, hp=200.0),
               L(556714, 0.2, 2.4, gain=-3.0, speed=0.6, fin=0.1, fout=0.6, hp=40.0),
               L(169666, 0.0, 0.6, gain=-12.0, at=0.3, fin=0.01, fout=0.3, hp=1500.0),
               L(169666, 0.0, 0.6, gain=-14.0, at=1.1, fin=0.01, fout=0.3, hp=1500.0),
               L(814976, 0.05, 0.8, gain=-3.0, at=1.95, speed=0.7, fin=0.003, fout=0.4, hp=30.0)],
              "a metal screech glided down from 0.85 to 0.5 (two of its strokes run together), over bending "
              "metal slowed to 0.6 (a deep creaking "
              "groan), two spark bursts, a large door's closing thud at 2.0 s: a harsher, more grinding 'KRRIIIIIK' "
              "with a lower groan than a, 2.75 s"),
    }),
    "sovereign-klaxon": (-22.0, -3.0, -19.0, "Iron Sovereign: the klaxon",
                         "twice a fight: the intro (as the rings spin up, from the core) and the core's opening (3 s "
                         "while the petals part and the core is invulnerable); over the music (no change at the "
                         "opening: the klaxon and Vorne's line carry it)",
                         "an industrial or ship's alarm horn, reverberant, 3 s: a real klaxon, the warnings' family; "
                         "distinct from Level 29's station klaxon (concept/station-klaxon-r62-a.ogg, a fire station's "
                         "diaphone) and the tug's buzzer (concept/tug-alarm-r64-a.ogg)", f"{BOSS}#phases", {
        "a": ([L(514981, 1.0, 0.8, speed=0.85, fin=0.02, fout=0.08, hp=150.0, lp=7000.0),
               L(514981, 3.0, 0.8, at=1.05, speed=0.85, fin=0.02, fout=0.08, hp=150.0, lp=7000.0),
               L(514981, 5.0, 0.8, at=2.1, speed=0.85, fin=0.02, fout=0.08, hp=150.0, lp=7000.0)],
              "an industrial hooter (a mains-driven klaxon horn) slowed to 0.85, three 0.8 s blasts a second apart, "
              "played into a 300 m steel tunnel: a harsh buzzing 'BRAAAP BRAAAP BRAAAP' ringing on between the "
              "blasts, 3.4 s", (846029, -6.0, 3.4)),
        "b": ([L(104882, 0.0, 1.88, fin=0.01, fout=0.1, hp=100.0),
               L(104882, 0.0, 0.95, at=1.86, fin=0.01, fout=0.15, hp=100.0)],
              "a submarine's dive alarm (an archive recording), three of its rising-and-falling 'aaOOO-gah' cycles, "
              "in the same steel tunnel: lower and more of a ship's horn than a, 3.3 s", (846029, -9.0, 3.3)),
    }),
    "sovereign-shutters": (-22.0, -3.0, -19.0, "Iron Sovereign: the core's shutters",
                           "the core's opening (phase 4): its six armoured petals part over 3 s (with the klaxon); "
                           "the thud is the petals reaching open at ≈ 2.9 s",
                           "massive hydraulic doors: a hiss and a deep sliding thud (six petals moving together)",
                           f"{BOSS}#phases", {
        "a": ([L(457294, 0.0, 1.6, gain=-5.0, fin=0.005, fout=0.8, hp=200.0, lp=9000.0),
               L(331357, 0.0, 2.8, at=0.1, speed=0.6, fin=0.1, fout=0.3, hp=40.0),
               L(814976, 0.05, 0.9, gain=-2.0, at=2.75, speed=0.7, fin=0.003, fout=0.5, hp=30.0),
               L(406197, 0.3, 0.7, gain=-5.0, at=2.85, speed=0.8, fin=0.002, fout=0.4, hp=40.0)],
              "an air pressure release's hiss, a large hypermarket door's hydraulic run slowed to 0.6, then a large "
              "metal door's closing thud and a steel door's slam as they reach open: 'PSSSHHH-rrrrrrrr-DOMM', "
              "3.65 s"),
        "b": ([L(852991, 0.1, 2.3, gain=-3.0, speed=0.75, fin=0.05, fout=0.4, hp=120.0),
               L(841192, 0.0, 2.8, speed=0.7, fin=0.05, fout=0.3, hp=30.0),
               L(841192, 7.0, 1.0, at=2.75, speed=0.7, fin=0.003, fout=0.5, hp=30.0)],
              "a bus door's hydraulic hiss slowed to 0.75 over a hydraulic door's run slowed to 0.7 and its own "
              "heavy stop: a longer hiss and a lower, rumbling slide 'hsssss-mmmmmm-THUMM' than a, 3.75 s"),
    }),
    "sovereign-fan": (-20.0, -3.0, -18.0, "Iron Sovereign: the core's `fan`",
                      "the shut core's aimed `fan` of 5 through its shutter slits (phases 1–3, every 3.0 / 2.5 / "
                      "2.0 s, after a 0.5 s gold glow)",
                      "a hard mechanical gun burst: an autocannon", f"{BOSS}#stat-block-draft", {
        "a": ([L(854186, 0.0, 0.9, speed=0.9, fin=0.001, fout=0.3, hp=50.0)],
              "an autocannon's three-shot burst slowed to 0.9: 'BAM-BAM-BAM' 0.3 s apart, 0.9 s"),
        "b": ([L(160355, 0.58, 0.75, speed=0.85, fin=0.002, fout=0.3, hp=50.0)],
              "a .50 calibre air-defence gun's burst slowed to 0.85: five quicker, tighter shots 'BRRRAP', 0.75 s"),
    }),
    "sovereign-spiral": (-28.0, -6.0, -26.0, "Iron Sovereign: the core's `spiral` (one shot)",
                         "the open core's 2-arm `spiral` (phase 4: 5 bullets a second an arm, 4 s on, 2 s rest; "
                         "3 arms on hard): one shot per bullet, throttled in the game",
                         "the `fan`'s gun, lighter: a short single shot that can repeat without drowning the fight",
                         f"{BOSS}#phases", {
        "a": ([L(854183, 0.0, 0.2, speed=1.25, fin=0.001, fout=0.12, hp=120.0)],
              "one autocannon shot sped to 1.25: a short hard 'tak', 0.2 s"),
        "b": ([L(160355, 0.58, 0.15, speed=1.1, fin=0.001, fout=0.08, hp=150.0)],
              "one shot of the .50 calibre's burst sped to 1.1: a thinner, drier 'tk' than a, 0.15 s"),
    }),
    "sovereign-cookoff": (-18.0, -2.0, -15.0, "Iron Sovereign: the cook-offs (death)",
                          "the core at 0: the wrecked batteries cook off ring by ring, outer → middle → inner, 0.15 s "
                          "apart (6 SAM, 4 rail, 3 laser: 13 in 1.8 s), each a short vacuum flash; the rings crack "
                          "at their joints; the implosion follows",
                          "rapid secondary detonations and tearing metal: a chain, each ring a little heavier",
                          f"{BOSS}#death", {
        "a": (chain(COOK_A, gains=GAINS) +
              [L(556714, 0.2, 2.4, gain=-6.0, at=0.2, speed=0.8, fin=0.1, fout=0.8, hp=40.0)],
              "five sharp explosion recordings in turn, 13 of them 0.15 s apart: the outer six sped to 1.2, the "
              "middle four as recorded, the inner three slowed to 0.8 (deeper), bending metal creaking under them: "
              "a rattling 'ba-ba-ba-ba-ba-ba-BA-BA-BA-BA-BOM-BOM-BOM', 2.6 s"),
        "b": (chain(COOK_B, gains=GAINS) +
              [L(496836, 4.4, 2.4, gain=-8.0, at=0.2, speed=0.8, fin=0.1, fout=0.8, hp=60.0)],
              "13 deep explosions cut from one field recording of a series, 0.15 s apart, the outer six sped to 1.2, "
              "the inner three slowed to 0.8, a metal groan under them: a deeper, rounder rolling chain than a "
              "(the recording's distant road hiss under it), 2.6 s"),
    }),
    "sovereign-implosion": (-18.0, -2.0, -15.0, "Iron Sovereign: the core's implosion",
                            "after the cook-offs: the core collapses inward (the petals folding in, a white-gold "
                            "implosion) and then blows; the `huge` rung starts at the "
                            "whoomph, 1.15 s in, under the screen flash",
                            "a deep implosion: the air sucked inward, metal folding, a deep 'whoomph' (no pop)",
                            f"{BOSS}#death", {
        "a": ([L(-101934, 21.8, 1.2, gain=-4.0, speed=0.8, fin=0.3, fout=0.01, hp=40.0, lp=3000.0, swell=(-16.0, 2.0)),
               L(556714, 0.2, 1.2, gain=-6.0, speed=0.6, fin=0.2, fout=0.1, hp=40.0),
               L(683179, 0.0, 1.0, at=1.15, speed=0.5, fin=0.002, fout=0.6, hp=30.0)],
              "a dry thunderclap reversed and slowed to 0.8 (its rumble sucked back into the crack), bending metal "
              "under it, cut at the crack into a recorded fireball slowed to 0.5: 'hhhhwwuuUUP-WHOOMF', 2.15 s"),
        "b": ([L(-505113, 15.14, 1.2, gain=-3.0, speed=0.7, fin=0.3, fout=0.01, hp=40.0, lp=3000.0, swell=(-20.0, 2.5)),
               L(814976, 0.05, 1.0, gain=-2.0, at=1.15, speed=0.5, fin=0.003, fout=0.5, hp=30.0),
               L(336005, 0.0, 0.9, gain=-3.0, at=1.15, speed=0.45, fin=0.002, fout=0.5, hp=30.0)],
              "a close thunder reversed and slowed to 0.7, then a large metal door's thud slowed to 0.5 (the petals "
              "slamming in) with an explosion slowed to 0.45: a darker suck and a heavier metal 'DOOMF' than a, "
              "2.15 s"),
    }),
    "relay-charge": (-24.0, -5.0, -21.0, "Mass relay: the charge (its telegraph)",
                     "Level 35, once at t=59.8: the mass relay's 1.2 s telegraph (a thin red line across the whole "
                     "field through the ship; easy 1.5 s: it starts 0.3 s into the telegraph); the flash below at "
                     "its end",
                     "the rail line's chosen charge (concept/rail-charge-r60-b.ogg, a rising hum over a slowed "
                     "transformer) in its long version: a huge capacitor whine building for 1.2 s",
                     f"{L35}#layout", {
        "a": ([L(616084, 12.0, 1.2, speed=(0.7, 1.5), fin=0.12, fout=0.03, hp=150.0, swell=(-10.0, 1.4)),
               L(453444, 4.0, 1.2, gain=-5.0, speed=0.8, fin=0.25, fout=0.03, hp=40.0, lp=900.0)],
              "the chosen rail charge's own two recordings stretched to 1.2 s: the rising electric hum gliding up "
              "from 0.7 to 1.5 and swelling, over the transformer hum slowed to 0.8: the bunker's charge, longer, "
              "1.2 s"),
        "b": ([L(616084, 12.0, 1.2, speed=(0.5, 1.2), fin=0.12, fout=0.03, hp=100.0, swell=(-12.0, 1.6)),
               L(453444, 4.0, 1.2, gain=-3.0, speed=0.6, fin=0.25, fout=0.03, hp=40.0, lp=700.0),
               L(119902, 2.0, 1.2, gain=-6.0, speed=(0.35, 0.75), fin=0.1, fout=0.03, hp=200.0,
                 swell=(-14.0, 1.5))],
              "the same two recordings lower (the hum gliding from 0.5 to 1.2, the transformer at 0.6) with a camera "
              "flash's charging whine pitched well down rising over them: bigger and deeper than a, a whine on top, "
              "1.2 s"),
    }),
    "relay-flash": (-16.0, -2.0, -14.0, "Mass relay: the flash",
                    "Level 35, once at t=61: the mass relay's flash along its line (16 px wide, live 0.2 s, 20 "
                    "damage), at the charge's end; the thunder's tail runs under the next seconds (no wave until "
                    "t=63.5)",
                    "the rail line's chosen shot (concept/rail-shot-r60-b.ogg, a whip crack on a howitzer shot with "
                    "a long metal ping) in its long version: the crack with a long rolling thunder tail",
                    f"{L35}#layout", {
        "a": ([L(93100, 0.25, 0.4, fin=0.001, fout=0.25, hp=150.0),
               L(67516, 0.0, 0.9, gain=-2.0, fin=0.002, fout=0.5, hp=40.0),
               L(101246, 0.0, 1.5, gain=-10.0, at=0.01, speed=0.9, fin=0.001, fout=0.9, hp=400.0),
               L(672776, 0.2, 3.8, gain=-3.0, at=0.08, fin=0.02, fout=1.5, hp=30.0, lp=5000.0)],
              "the chosen rail shot's three recordings (the whip crack, the howitzer, the metal ping, its ping "
              "longer) with a thunderclap's roll under them: 'KRAKK-rrrrooooommmm', 3.9 s"),
        "b": ([L(93100, 0.25, 0.4, fin=0.001, fout=0.25, hp=150.0),
               L(67516, 0.0, 0.9, gain=-2.0, fin=0.002, fout=0.5, hp=40.0),
               L(101246, 0.0, 1.5, gain=-10.0, at=0.01, speed=0.9, fin=0.001, fout=0.9, hp=400.0),
               L(732992, 0.95, 4.0, gain=-2.0, speed=0.8, fin=0.01, fout=1.6, hp=30.0)],
              "the same shot with a huge thunder crack slowed to 0.8 under it: a second, deeper crack and a longer, "
              "lower rumble than a, 4.0 s"),
    }),
}


def sources_of(layers, space=None):
    return list(dict.fromkeys([abs(layer["sound"]) for layer in layers] + ([space[0]] if space else [])))


def variant(subject, v):
    spec = SOUNDS[subject][7][v]
    return spec[0], spec[1], (spec[2] if len(spec) > 2 else None)


def build(subject, v):
    band, ceiling, cap = SOUNDS[subject][:3]
    layers, what, space = variant(subject, v)
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(layers, space))
    raw = mix(layers)
    if space:
        raw = in_space(raw, *space)
    st = level(raw, band, ceiling)
    loud = lufs(st)[0]
    if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
        st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 68 ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def speed_text(speed):
    return f"{num(speed[0])} → {num(speed[1])}" if isinstance(speed, tuple) else num(speed)


def level_text(subject):
    band, ceiling, cap = SOUNDS[subject][:3]
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, layers, layer, space, multi=False):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    if layer is None:  # the impulse response
        parts.append(f"used as a recorded space: the mix convolved with it (scaled to unit energy, {num(space[1])} dB "
                     f"under the dry), cut to {num(space[2])} s with a 0.35 s fade-out")
        parts.append(level_text(subject))
        parts.append("OGG Vorbis q6")
        return ", ".join(parts)
    speed = layer["speed"]
    span = layer["length"] * (np.mean(speed) if isinstance(speed, tuple) else speed)
    if multi:
        pass
    elif layer["sound"] < 0:
        parts.append(f"reversed, cut {num(layer['start'])}–{layer['start'] + span:.2f} s of the reversed file")
    else:
        parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
    if multi:
        parts.append(f"each {num(layer['length'])} s long at its speed")
    elif isinstance(speed, tuple):
        parts.append(f"played at a speed gliding {num(speed[0])} → {num(speed[1])}")
    elif speed != 1.0:
        parts.append(f"slowed to {num(speed)}" if speed < 1 else f"sped to {num(speed)}")
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
    if layer["swell"]:
        parts.append(f"swelled from {num(layer['swell'][0])} dB")
    if layer["at"] and not multi:
        parts.append(f"placed at {num(layer['at'])} s")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] and not multi else ""))
    if len(layers) > 1:
        parts.append("mixed with the other layers")
    if space:
        parts.append("played into a recorded space (a steel tunnel's impulse response)")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(layers, space):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(layers, space)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        _, _, _, _, use, should, _, _ = SOUNDS[subject]
        layers, what, space = variant(subject, v)
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        # one row per recording; a recording cut several times (the klaxon's blasts, the cook-offs) says so
        for s in sources_of(layers, space):
            uses = [layer for layer in layers if abs(layer["sound"]) == s]
            change = change_text(subject, layers, uses[0] if uses else None, space, multi=len(uses) > 1)
            if len(uses) > 1:
                cuts = "; ".join(f"from {num(u['start'])} s, placed at {num(u['at'])} s, speed {speed_text(u['speed'])}"
                                 + (f", {num(u['gain'])} dB" if u["gain"] else "")
                                 for u in uses)
                change = f"cut {len(uses)} times ({cuts}): {change}"
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change} |")
        x = decode(path)
        secs = x.shape[1] / SR
        lv = short_term_band_db(x)
        info[(subject, v)] = (secs, what)
        size = f"{secs:.2f} s ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(layers, space)
        edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(layers, space)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-68/: the same depth as design/audio/sfx)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        _, _, _, title, use, should, part, variants = SOUNDS[subject]
        what = " ".join(f"**{v}** ({info[(subject, v)][0]:.2f} s): {info[(subject, v)][1]}." for v in variants
                        if (subject, v) in info)
        files = ", ".join(f"[{v}](../../audio/sfx/concept/{subject}-{R}-{v}.ogg)" for v in variants)
        rnd.append(f"| S.{k} | [{title} (`{subject}`)]({part}) | **pick a or b**. **Plays:** {use}. **Should sound "
                   f"like:** {should}. {what} | {files} | | **a** / b / redo | |")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    (outdir / "round.md").write_text("\n".join(rnd) + "\n")
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries, {len(rnd)} "
          f"round rows in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[7]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
