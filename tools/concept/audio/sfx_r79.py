#!/usr/bin/env python3
"""Concept round 79 (M10 part G "Level 41 Callisto Fortress", step AUD-G; the gap check's #13): Level 41's new sounds,
a and b each (the Level 41 document's *Music & ambience*, the Shield Pylon's and the Halo Platform's *Sounds* briefs,
design/audio/sfx/README.md, *Act 6 sounds*): the fortress's machinery layer over the `callisto-fortress` bed, the
pylon dome's hum (a loop on the nearest dome), the pylon's death, the Halo's barrier collapsing and its open core's
whine, the gates' blast doors (Level 32's `door-grind`, deeper and longer), an archive core's hatch breaking, Varga's
lab vault cracking, and the uplink's three tones (the spike seating, the data chatter while it uploads, the file's
confirmation chirp). The dome's collapse is not proposed again: round 72's chosen `pylon-dome-down` b plays at the
pylon's death, an EMP and a `shield-breaker` hit already.

The recorded sounds are Freesound sounds (CC0 or CC-BY, in CREDITS.md) taken from their public HQ previews
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 79 and the picks rebuilt from them with the same settings.
They are only cut, slowed, sped, glided, layered, swelled, faded, filtered, looped and levelled. The uplink's three
tones are UI feedback and synthesized here (the brief allows it): clean electronic tones, no third-party source. No
bubbly or popping sources: the tones stay above 600 Hz with hard steps or slow glides, the impacts are metal.

The machinery layer and the dome hum are seamless loops; the chatter is a seamless 2 s loop the game plays while the
ship hovers on a core and stops when the file completes (2.0 s on medium) or the ship leaves. The blast doors play at
`GATE_OPENED`, over the doors' 2 s parting, their final thud at 2.4-2.5 s. The core's whine plays with the eye's 0.5 s
flare before each ring of the open core.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  fortress-machinery-r79-{a,b}     seamless 16 s LOOPs: deep machine hum, a door servo and a distant klaxon once a
                                   loop (a layer over `callisto-fortress` in sections 2-10)
  pylon-dome-hum-r79-{a,b}         seamless 6 s LOOPs: a low steady electrical hum (a transformer, a neon sign)
  pylon-death-r79-{a,b}            ≈ 2.3 s: a metal mast's blast and its crash (over the `medium` rung)
  halo-barrier-collapse-r79-{a,b}  ≈ 1.6 s: a descending electrical hum cut off by a heavy relay clunk
  halo-core-whine-r79-{a,b}        0.7 s: a short rising whine (the open core's ring)
  gate-doors-r79-{a,b}             ≈ 3.4 s: a huge steel door's rumble, hydraulic clunks, a final heavy thud
  hatch-break-r79-{a,b}            ≈ 1.4 s: a heavy armour plate cracking off
  vault-crack-r79-{a,b}            ≈ 1.6 s: a steel door's bolts shearing
  uplink-seat-r79-{a,b}            ≈ 0.45 s: a short rising electronic tone (synthesized)
  uplink-chatter-r79-{a,b}         seamless 2 s LOOPs: a steady data chatter (synthesized)
  uplink-chirp-r79-{a,b}           ≈ 0.3 s: a clean confirmation chirp (synthesized)

Levels like the accepted picks (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the
loops by their band RMS): the machinery layer as round 78's fleet layer (under the beds), the dome hum as the Mirror's
engine loop, the pylon's death and the barrier as round 72's `pylon-dome-down`, the whine as Harker's sniper charge,
the doors a little over Level 32's `door-grind`, the hatch and the vault as the Graft Turret's shell crack, the uplink's
seat as `pylon-dome-up`, its chirp as a P2 cue and its chatter well under the fire. The two variants of a subject share
the level, so they are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r79.py [subject-or-file ...]   e.g. gate-doors vault-crack-r79-b
       python3 tools/concept/audio/sfx_r79.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
Deterministic (no randomness but seeded generators, bit-exact ffmpeg flags). Not listened to by Claude: checked by
level, peaks, onsets and the loop seams.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, pulse, ramp, sine, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r79.py"
R = "r79"
ROUND = "round 79"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    50506: ("crash.wav", "Sparrer", BY4, P + '50/50506_446095-hq.ogg'),
    131599: ("Kill Switch (Large Breaker Switch) .WAV", "EchoCinematics", CC0, P + '131/131599_823769-hq.ogg'),
    209772: ("Metal Boom", "Johnnyfarmer", CC0, P + '209/209772_2012390-hq.ogg'),
    244394: ("Bang/Explosion Metallic", "Werra", CC0, P + '244/244394_525427-hq.ogg'),
    260815: ("Industrial Machine Cycle.wav", "iccleste", CC0, P + '260/260815_4858376-hq.ogg'),
    315943: ("open rolling door 150328_0543 MS.wav", "klankbeeld", BY4, P + '315/315943_1648170-hq.ogg'),
    331357: ("Large hydraulic door", "nooly", BY3, P + '331/331357_1999691-hq.ogg'),
    332547: ("Scaffold Collapse Mixdown.wav", "paulw2k", BY4, P + '332/332547_755728-hq.ogg'),
    366093: ("Manhole Blows / Metallic Boom", "bevibeldesign", CC0, P + '366/366093_4557960-hq.ogg'),
    381957: ("Security Alarm", "JSilverSound", CC0, P + '381/381957_6566796-hq.ogg'),
    445099: ("Vault 2", "lezaarth", CC0, P + '445/445099_2023214-hq.ogg'),
    445100: ("vault", "lezaarth", CC0, P + '445/445100_2023214-hq.ogg'),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0, P + '453/453444_612689-hq.ogg'),
    453445: ("hum electric transformer in large room2.flac", "kyles", CC0, P + '453/453445_612689-hq.ogg'),
    453491: ("neon sign buzz hum stereo close lowcut to taste4.flac", "kyles", CC0, P + '453/453491_612689-hq.ogg'),
    462819: ("HUTCH HD SFX 2014 0143", "leonelmail", CC0, P + '462/462819_4437257-hq.ogg'),
    491407: ("Jet engine startup", "SamsterBirdies", CC0, P + '491/491407_5487341-hq.ogg'),
    513944: ("Bolt Cutter.wav", "schots", CC0, P + '513/513944_954919-hq.ogg'),
    514981: ("Klaxon on axis Long.wav", "jameswrowles", CC0, P + '514/514981_3874107-hq.ogg'),
    516591: ("Large Bolt Cutter", "jameswrowles", CC0, P + '516/516591_3874107-hq.ogg'),
    542582: ("Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3", "adr1911", CC0,
             P + '542/542582_10366986-hq.ogg'),
    555767: ("Big circuit breaker_2-3.With reverb of a large hallway(mltprcssng).wav", "newlocknew", BY4,
             P + '555/555767_5828667-hq.ogg'),
    587442: ("Dropping sheet metal", "SamsterBirdies", CC0, P + '587/587442_5487341-hq.ogg'),
    709258: ("Large machine running and powering down", "ryks88", BY4, P + '709/709258_8516444-hq.ogg'),
    726486: ("Impact on metal", "JoMungus", CC0, P + '726/726486_11865776-hq.ogg'),
    764849: ("motorized_door_servo_sound_11062024", "Artninja", BY4, P + '764/764849_10996917-hq.ogg'),
    769191: ("electricity buzz automatic large metal door motor servo opening", "jerry.berumen", BY4,
             P + '769/769191_6753194-hq.ogg'),
    841192: ("Hydraulic Door", "Autobot8299", CC0, P + '841/841192_18079032-hq.ogg'),
    863134: ("Manhole cover (SoundAction 156)", "alexarje", BY4, P + '863/863134_262-hq.ogg'),
}
_decoded = {}


def preview(sound):
    """The decoded HQ preview, fetched from the CDN into the cache when missing."""
    if sound not in _decoded:
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        _decoded[sound] = decode(path)
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() and loop() read their layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def bed(layers, length, xfade, extra=None):
    """sfx_r43g.loop() plus `events`: one-shot layers placed once in the loop at their `at` (wrapping round the
    seam)."""
    st = loop(layers, length, xfade)
    n = st.shape[1]
    for ev in (extra or {}).get("events", []):
        x = cut(ev)
        idx = (int(ev["at"] * SR) + np.arange(x.shape[1])) % n
        np.add.at(st[0], idx, x[0])
        np.add.at(st[1], idx, x[1])
    return st


# ---- the uplink's synthesized tones (UI feedback) ----

def _env(n, a, r):
    """A linear attack of `a` s and a raised-cosine release over the last `r` s."""
    e = np.ones(n)
    ka, kr = max(1, int(a * SR)), max(1, int(r * SR))
    e[:ka] = np.linspace(0, 1, ka)
    e[n - kr:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, kr))
    return e


def _tone(freq, n, bright):
    """A clean electronic tone: a sine with a band-limited pulse under it (`bright` 0..1), low-passed at 6 kHz."""
    x = sine(freq, n) + bright * 0.35 * svf(pulse(freq, n, width=0.3), 6000.0)
    return x


def _click(n, seed):
    """A 4 ms connector click: seeded noise high-passed at 2 kHz, decaying."""
    rng = np.random.default_rng(seed)
    k = int(0.004 * SR)
    x = np.zeros(n)
    x[:k] = svf(rng.uniform(-1, 1, k), 2000.0, mode="hp") * np.exp(-np.arange(k) / (0.0012 * SR))
    return x


def _wrap(x, length):
    """A seamless loop of `length` s from x (longer by the cross-fade): its tail cross-faded into its start (equal
    power)."""
    n = int(length * SR)
    k = len(x) - n
    w = np.linspace(0, np.pi / 2, k)
    out = x[:n].copy()
    out[:k] = x[:k] * np.sin(w) + x[n:] * np.cos(w)
    return out


def uplink_seat(v):
    if v == "a":  # one glide up an octave, 700 -> 1400 Hz over 0.3 s, held 0.12 s, a click as the spike goes in
        n = int(0.45 * SR)
        f = np.concatenate([ramp(int(0.3 * SR), 700.0, 1400.0), np.full(n - int(0.3 * SR), 1400.0)])
        x = _tone(f, n, 0.8) * _env(n, 0.01, 0.12) + 0.5 * _click(n, 79)
    else:  # three hard steps up a fifth and an octave (660, 990, 1320 Hz), 0.1 s each, the last held
        steps = [(660.0, 0.1), (990.0, 0.1), (1320.0, 0.22)]
        f = np.concatenate([np.full(int(d * SR), hz) for hz, d in steps])
        n = len(f)
        x = _tone(f, n, 0.5) * _env(n, 0.005, 0.1) + 0.5 * _click(n, 80)
    return np.vstack([x, x])


def uplink_chatter(v):
    rng = np.random.default_rng(790 if v == "a" else 791)
    total = 2.4  # a 2 s loop with a 0.4 s cross-fade
    n = int(total * SR)
    if v == "a":  # a modem's FSK chatter: 300 baud on 1070/1270 Hz under a quieter answer on 2025/2225 Hz
        def fsk(lo, hi, baud):
            bits = rng.integers(0, 2, int(total * baud) + 1)
            per = SR / baud
            f = np.where(bits[(np.arange(n) / per).astype(int)] == 1, hi, lo)
            return sine(f, n)
        x = fsk(1070.0, 1270.0, 300.0) + 0.5 * fsk(2025.0, 2225.0, 300.0)
        x = svf(svf(x, 600.0, mode="hp"), 3500.0)
    else:  # a packet chirr: 30 ms bursts of a 2.4 kHz carrier with its phase flipped at 1200 baud, 12 ms gaps
        bits = rng.integers(0, 2, int(total * 1200) + 1)
        sign = np.where(bits[(np.arange(n) / (SR / 1200)).astype(int)] == 1, 1.0, -1.0)
        carrier = sine(2400.0, n) * sign
        gate = ((np.arange(n) / SR) % 0.042) < 0.030
        g = svf(gate.astype(float), 400.0)  # soft packet edges, no clicks
        x = svf(svf(carrier * g, 800.0, mode="hp"), 4500.0) + 0.2 * sine(1200.0, n) * g
    x = _wrap(x, 2.0)
    return np.vstack([x, x])


def uplink_chirp(v):
    if v == "a":  # two clean notes up a fourth, G6 then C7 (1568, 2093 Hz): 60 ms and a 180 ms ring
        a, b = int(0.06 * SR), int(0.18 * SR)
        x = np.concatenate([_tone(1568.0, a, 0.3) * _env(a, 0.003, 0.01),
                            _tone(2093.0, b, 0.3) * _env(b, 0.003, 0.02) * np.exp(-np.arange(b) / (0.07 * SR))])
    else:  # one quick sweep up an octave, 1.2 -> 2.4 kHz in 80 ms, ringing out at 2.4 kHz
        a, b = int(0.08 * SR), int(0.2 * SR)
        f = np.concatenate([ramp(a, 1200.0, 2400.0), np.full(b, 2400.0)])
        e = np.concatenate([np.ones(a), np.exp(-np.arange(b) / (0.07 * SR))]) * _env(a + b, 0.004, 0.02)
        x = _tone(f, a + b, 0.3) * e
    return np.vstack([x, x])


SYNTH = {"uplink-seat": uplink_seat, "uplink-chatter": uplink_chatter, "uplink-chirp": uplink_chirp}

SEAM_TRIES = 12  # a bed loop's candidate starts (see build)
L41 = "Level 41 (Callisto Fortress)"
PYLON = "a Shield Pylon (Level 41's gates: 3, 4 and 4 pylons, the domes over the guns)"
HALO = "the Halo Platform (Level 41's fourth held gate: a turret ring round a shielded core)"
UPLINK = f"{L41}: Varga's uplink spike on an archive core (three cores, one in each gate's halt)"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by the band RMS at most `cap`
#             LUFS; "synth" (a shot) and "synthloop" (a loop): None, SYNTH[subject](v).
SOUNDS = {
    "fortress-machinery": ("loop", -42.0, -1.0, -30.0,
                           f"{L41}, sections 2-10 (from the outpost to Helix HQ): a layer over the `callisto-fortress` "
                           "bed, under the music",
                           "the fortress's machinery heard through the hull: deep hums, a door servo now and then, a "
                           "distant klaxon; a seamless loop", {
        "a": (([Loop(260815, 4.0, speed=0.8, hp=50.0, lp=900.0)], 16.0, 1.0,
               {"events": [L(514981, 1.0, 2.2, gain=-16.0, at=3.0, speed=0.9, fin=0.4, fout=0.9, hp=300.0,
                             lp=1500.0),
                           L(769191, 2.5, 2.5, gain=-12.0, at=10.0, speed=0.85, fin=0.3, fout=0.6, hp=150.0,
                             lp=3000.0)]}),
              "an industrial machine's cycle slowed to 0.8 and muffled at 900 Hz (a deep, steady hum); once a loop a "
              "klaxon's long blast far off (low-passed at 1.5 kHz) and a big metal door's servo motor whining: "
              "16.0 s loop"),
        "b": (([Loop(709258, 4.0, speed=0.7, hp=50.0, lp=900.0)], 16.0, 1.0,
               {"events": [L(381957, 0.0, 2.6, gain=-17.0, at=4.5, speed=0.85, fin=0.2, fout=0.8, hp=300.0,
                             lp=1800.0),
                           L(764849, 1.4, 1.8, gain=-12.0, at=11.0, speed=0.85, fin=0.05, fout=0.3, hp=150.0,
                             lp=3000.0)]}),
              "a large machine running, slowed to 0.7 and muffled at 900 Hz (a rougher, throatier hum than a); once "
              "a loop a pulsing alarm far off (two pulses, low-passed) and a door servo's short motor bursts: "
              "16.0 s loop"),
    }),
    "pylon-dome-hum": ("loop", -37.0, -1.0, -29.0,
                       f"{PYLON}: its dome while it stands, one loop for the nearest dome on the screen (from the "
                       "gate's halt until the last pylon falls)",
                       "a low steady electrical hum, a transformer or a large neon sign; a seamless loop", {
        "a": (([Loop(453444, 4.0, speed=0.85, hp=60.0, lp=4000.0)], 6.0, 1.0),
              "an electric transformer humming in a large room, slowed to 0.85: a deep, even mains hum with the "
              "room's air round it, 6.0 s loop"),
        "b": (([Loop(453491, 10.0, speed=0.9, hp=80.0, lp=5000.0)], 6.0, 1.0),
              "a neon sign buzzing close, slowed to 0.9: a brighter, fizzier buzz than a over its 120 Hz hum, "
              "6.0 s loop"),
    }),
    "pylon-death": ("shot", -22.0, -5.0, -21.0,
                    f"{PYLON}: the pylon destroyed (240 HP), over the `medium` rung and its dome's collapse "
                    "(`pylon-dome-down`); 11 a level",
                    "a metal mast's blast and its crash", {
        "a": ([L(244394, 0.0, 1.4, fin=0.002, fout=0.6, hp=40.0),
               L(332547, 0.15, 2.2, gain=-3.0, at=0.15, fin=0.01, fout=0.8, hp=60.0, lp=8000.0)],
              "a metallic explosion's bang, then scaffolding collapsing (a heavy hit and poles clattering down): "
              "2.35 s"),
        "b": ([L(209772, 0.2, 1.3, fin=0.002, fout=0.6, hp=40.0),
               L(50506, 1.3, 1.6, gain=-2.0, at=0.2, fin=0.005, fout=0.6, hp=80.0, lp=8000.0)],
              "a deep metal boom, then a big metal crash (sheet metal and scrap smashing down): brighter and more "
              "clattering than a, 1.8 s"),
    }),
    "halo-barrier-collapse": ("shot", -22.0, -5.0, -22.0,
                              f"{HALO}: the red barrier on its core failing as the third turret falls (fading over "
                              "0.5 s); once a level",
                              "a descending electrical hum cut off by a heavy relay clunk", {
        "a": ([L(453445, 8.0, 0.75, gain=-5.0, speed=(1.0, 0.45), fin=0.03, fout=0.04, hp=60.0),
               L(131599, 0.28, 0.9, at=0.7, fin=0.002, fout=0.5, hp=40.0)],
              "a transformer's hum falling more than an octave over 0.75 s, cut by a large breaker switch thrown "
              "(a hard mechanical clunk): 1.6 s"),
        "b": ([L(453491, 20.0, 0.75, gain=-5.0, speed=(1.0, 0.4), fin=0.03, fout=0.04, hp=80.0, lp=7000.0),
               L(555767, 0.12, 1.0, at=0.7, fin=0.002, fout=0.5, hp=40.0)],
              "a neon sign's buzz falling more than an octave, cut by a big circuit breaker in a hallway (a "
              "clunk with a hall's tail): buzzier than a, 1.7 s"),
    }),
    "halo-core-whine": ("shot", -25.0, -6.0, -24.0,
                        f"{HALO}: the open core's eye flaring (0.5 s) before each ring of 10 bullets, every 3.0 s "
                        "after the barrier falls",
                        "a short rising whine", {
        "a": ([L(542582, 3.0, 0.7, speed=(1.0, 1.4), fin=0.04, fout=0.08, hp=300.0, swell=(-12.0, 1.0))],
              "a gas-turbine tank engine's start-up whine (its natural rise sped up to 1.4): a clean whine "
              "climbing from about 1.7 to 4.5 kHz, 0.7 s"),
        "b": ([L(491407, 1.0, 0.7, speed=(0.9, 1.3), fin=0.04, fout=0.08, hp=600.0, swell=(-12.0, 1.0))],
              "a jet engine's start-up whine sped from 0.9 to 1.3: an airier, rougher whine than a, rising from "
              "about 1.3 to 3 kHz, 0.7 s"),
    }),
    "gate-doors": ("shot", -23.0, -4.0, -21.0,
                   f"{L41}: a gate's blast doors parting as its last pylon falls (the scroll easing back over 2 s); "
                   "four times a level",
                   "Level 32's blast door, deeper and longer: a huge steel door's rumble on its rails, hydraulic "
                   "clunks, a final heavy thud", {
        "a": ([L(331357, 1.9, 0.6, gain=-4.0, fin=0.002, fout=0.3, hp=40.0),
               L(315943, 1.6, 2.5, at=0.25, speed=0.6, fin=0.15, fout=0.25, hp=30.0),
               L(462819, 0.08, 1.0, gain=-1.0, at=2.4, speed=0.7, fin=0.002, fout=0.5, hp=30.0)],
              "a large hydraulic door's clunk, then Level 32's industrial rolling door slowed further (0.6) and "
              "its deep hollow impact slowed to 0.7: a lower, longer 'k-chunk, rrrrrRRRR-DOMM', 3.4 s"),
        "b": ([L(331357, 1.9, 0.6, gain=-4.0, fin=0.002, fout=0.3, hp=40.0),
               L(841192, 4.0, 2.45, at=0.2, speed=0.8, fin=0.2, fout=0.1, hp=30.0),
               L(841192, 6.9, 1.0, at=2.55, speed=0.8, fin=0.01, fout=0.6, hp=30.0)],
              "a large hydraulic door's clunk, then a hydraulic door's run and slam slowed to 0.8: a smoother "
              "hydraulic rumble than a's rolling grind, the slam's thud at 2.8 s, 3.55 s"),
    }),
    "hatch-break": ("shot", -24.0, -5.0, -22.0,
                    f"{L41}: an archive core's armoured hatch broken (30 HP; the core under it open for the "
                    "uplink); three times a level",
                    "a heavy armour plate cracking off", {
        "a": ([L(726486, 0.0, 1.2, fin=0.001, fout=0.5, hp=60.0),
               L(863134, 2.75, 1.0, gain=-3.0, at=0.3, fin=0.005, fout=0.4, hp=80.0)],
              "a hard blow on heavy steel, then a manhole cover lifted and dropped clanging: 1.3 s"),
        "b": ([L(366093, 0.0, 1.2, fin=0.001, fout=0.5, hp=50.0),
               L(587442, 4.95, 1.0, gain=-6.0, at=0.25, fin=0.005, fout=0.5, hp=150.0, lp=7000.0)],
              "a manhole cover blown off (a deep metallic boom), then sheet metal dropping with a ringing clang: "
              "boomier than a, 1.25 s"),
    }),
    "vault-crack": ("shot", -24.0, -5.0, -22.0,
                    f"{L41}: Varga's lab vault (the secret, 20 HP) broken open, over the built crate break; once a "
                    "level at most",
                    "a steel door's bolts shearing", {
        "a": ([L(516591, 0.0, 0.35, speed=0.85, fin=0.001, fout=0.15, hp=120.0),
               L(516591, 0.0, 0.35, gain=-2.0, at=0.16, speed=0.75, fin=0.001, fout=0.15, hp=120.0),
               L(516591, 0.0, 0.35, gain=-1.0, at=0.3, speed=0.65, fin=0.001, fout=0.15, hp=120.0),
               L(445099, 0.1, 1.1, gain=-4.0, at=0.42, fin=0.005, fout=0.5, hp=60.0)],
              "a large bolt cutter's snap three times, each lower (three bolts shearing in a row), then a vault "
              "door's heavy metal clank: 1.5 s"),
        "b": ([L(513944, 2.12, 0.3, fin=0.001, fout=0.12, hp=150.0),
               L(513944, 5.14, 0.3, gain=-1.0, at=0.2, fin=0.001, fout=0.12, hp=150.0),
               L(445100, 0.8, 0.85, gain=-3.0, at=0.4, fin=0.005, fout=0.4, hp=60.0)],
              "a bolt cutter biting through two bolts (crunchier snaps than a), then a vault door's lock "
              "clanking open: 1.25 s"),
    }),
    "uplink-seat": ("synth", -27.0, -8.0, -26.0,
                    f"{UPLINK}: the spike seating as the ship's centre comes within 40 px of an open core",
                    "a short rising electronic tone; clean, never bubbly", {
        "a": (None, "a connector's click and a clean tone gliding up an octave (700 to 1400 Hz) and held: "
                    "0.45 s"),
        "b": (None, "a connector's click and three hard steps up (660, 990, 1320 Hz), the last held: more like a "
                    "UI cue than a's glide, 0.42 s"),
    }),
    "uplink-chatter": ("synthloop", -36.0, -1.0, -30.0,
                       f"{UPLINK}: while the file uploads (2.0 s on medium, easy 1.5, hard 2.5, counted only while "
                       "the ship hovers), a loop under the teal ring filling; stopped by the chirp or the ship "
                       "leaving",
                       "a steady data chatter, electronic, never bubbly; a seamless loop", {
        "a": (None, "a modem's two-tone data chatter (300 baud on 1070/1270 Hz under a quieter answer on "
                    "2025/2225 Hz): a busy, warbling hiss of tones, 2.0 s loop"),
        "b": (None, "packets of a 2.4 kHz data carrier, 30 ms on and 12 ms off: a brighter, raspier, more "
                    "rhythmic chirr than a, 2.0 s loop"),
    }),
    "uplink-chirp": ("synth", -25.0, -6.0, -24.0,
                     f"{UPLINK}: the file complete (`FILES n / 3` counting up), over the built objective tone",
                     "a clean confirmation chirp", {
        "a": (None, "two clean notes up a fourth (G6 then C7), the second ringing: 'bi-ding', 0.24 s"),
        "b": (None, "one quick sweep up an octave (1.2 to 2.4 kHz) ringing out: a single 'tweet', 0.28 s"),
    }),
}


def layers_of(kind, spec):
    if kind in ("synth", "synthloop"):
        return []
    if kind != "loop":
        return spec
    extra = spec[3] if len(spec) > 3 else {}
    return list(spec[0]) + list(extra.get("events", []))


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    looped = kind in ("loop", "synthloop")
    if looped:
        st = bed(*spec) if kind == "loop" else SYNTH[subject](v)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved to the best of its SEAM_TRIES quietest, flattest points (at least 0.5 s apart) by the
        # seam measured on the decoded file (Vorbis' first and last frames shift the edges a little).
        score = np.abs(st).sum(axis=0) + 20.0 * np.abs(np.gradient(st, axis=1)).sum(axis=0)
        starts, gap = [], int(0.5 * SR) if kind == "loop" else int(0.1 * SR)
        for k in np.argsort(score):
            if all(abs(int(k) - j) >= gap for j in starts):
                starts.append(int(k))
            if len(starts) == SEAM_TRIES:
                break
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
        peak = db(np.abs(st).max())
        if peak > ceiling:  # a loop is never limited (the limiter would break the seam): it goes down whole
            st = st * undb(ceiling - peak)
        best = None
        for k in starts:
            trial = np.roll(st, -k, axis=1)
            path = write_ogg(OUT / f"{name}.ogg", trial, max_peak_db=ceiling)
            seam = seam_db(decode(path)[:, :trial.shape[1]])
            if best is None or seam < best[0]:
                best = (seam, trial)
        st = best[1]
    else:
        raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if looped:
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"loudest 100 ms band {short_term_band_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind in ("loop", "synthloop"):
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz), at most {num(cap)} LUFS"
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    one_shot = "length" in layer
    if not one_shot:
        span = (spec[1] + spec[2]) * speed
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
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s" + (" in the loop (once a loop)" if kind == "loop" else ""))
    if kind == "loop" and not one_shot:
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
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
        looped = kind in ("loop", "synthloop")
        lv = band_rms_db(x) if looped else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if looped else ''} ({lv:.0f} dB)".replace("(-", "(−")
        edit = (f"{secs:.2f} s, {'band RMS' if looped else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind in ("synth", "synthloop"):
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source; UI feedback, the brief allows it).\nUse: {use}.\nBrief: {should}.\n"
                           f"This one: {what}.\nEdit: {edit}.\n")
            continue
        srcs = sources_of(kind, spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
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
