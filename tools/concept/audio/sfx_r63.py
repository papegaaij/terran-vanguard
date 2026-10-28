#!/usr/bin/env python3
"""Concept round 63 (M9 part C "Level 30 Minefield", step AUD-C): Level 30's new sounds, a and b each
(design/audio/sfx/README.md, *Act 5 sounds*; the Level 30 document's *Music & ambience*; the Minelayer's, the Gilded
Gunship's, the Rail Serpent's and the Dart Drone's sound briefs): the mine's arm tick, drop clunk and chain ripple,
the Rail Serpent's turret shot, rumble loop and split, the Gunship's ring shot and bay doors, the Dart's launch and the
beacon buoy's strobe tick. Round 60's picks are used as they are, not proposed again: the mine's blink
(mine-blink-r60-a) and burst (mine-burst-r60-a; the chain's ripple below is cut from it, as round 60 said) and the
Ascendancy turbine (ascendancy-turbine-r60-a: the Gunship's engine at its pitch, the Minelayer's drone lower).

Every recorded layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 63 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, faded,
filtered, looped and levelled. Synthesized: the mine's arm tick and the buoy's strobe tick (the briefs allow a
synthesized tick: a signal, not a machine). The tools are sfx_r62.py's (copied: preview, build, the --docs rows) plus
sfx_r60.py's loops.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  mine-arm-r63-*         ≈ 0.1 s: a laid mine arming (synthesized tick)
  mine-drop-r63-*        ≈ 0.45 s: a mine dropped from the Minelayer, a dull metal clunk
  mine-chain-r63-*       ≈ 0.8-1 s: a shot mine's chain: a ripple of three smaller blasts (from the chosen burst)
  serpent-shot-r63-*     ≈ 0.3 s: a Rail Serpent turret car's aimed single shot
  serpent-rumble-r63-*   seamless 6 s LOOPs: the train's rumble and coupling clatter
  serpent-split-r63-*    ≈ 1.5 s: a car destroyed: a coupler snap, a falling power-down whine, sparks
  gunship-ring-r63-*     ≈ 1 s: the Gilded Gunship's ring of 12: a hollow cannon thump
  gunship-doors-r63-*    ≈ 0.6 s: the Gunship's bay doors per Dart stream, a heavy metal clank
  dart-launch-r63-*      ≈ 0.3 s: a Dart Drone launched from a bay, a short pneumatic thump
  buoy-tick-r63-*        ≈ 0.1 s: a beacon buoy's strobe, a faint electronic tick (synthesized)

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; a loop by its band RMS): the chain as the burst's smaller cousin, the turret shot as the enemy shot small,
the rumble under the turbine loop, the drop, the arm, the Dart and the buoy quiet (they come many at a time).

Usage: python3 tools/concept/audio/sfx_r63.py [subject-or-file ...]   e.g. mine-chain dart-launch-r63-b
       python3 tools/concept/audio/sfx_r63.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets and
the loop seams.
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
from synth import SR, db, decode, lufs, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r63.py"
R = "r63"
ROUND = "round 63"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    104399: ('Flak_Shot.wav', 'kantouth', BY4, P + "104/104399_1752241-hq.ogg"),
    131304: ('air_nailer_single_shot_1.wav', 'reg7783', CC0, P + "131/131304_2394357-hq.ogg"),
    151288: ('Wind-down 1.aif', 'timgormly', CC0, P + "151/151288_2578041-hq.ogg"),
    161650: ('snapping-chain', 'CosmicEmbers', BY3, P + "161/161650_2895542-hq.ogg"),
    184382: ('Mortar Shots.flac', 'qubodup', CC0, P + "184/184382_71257-hq.ogg"),
    186961: ('engine3_down.wav', 'ReadeOnly', CC0, P + "186/186961_545448-hq.ogg"),
    244394: ('Bang/Explosion Metallic', 'Werra', CC0, P + "244/244394_525427-hq.ogg"),
    249425: ('freight train railway bridge 01 140916_0415.wav', 'klankbeeld', BY4, P + "249/249425_1648170-hq.ogg"),
    275441: ('Heavy steal door closing and locking.WAV', 'Sandermotions', CC0, P + "275/275441_1402315-hq.ogg"),
    396240: ('Welding_Electric Sparks.wav', 'weaveofkev', CC0, P + "396/396240_453602-hq.ogg"),
    412313: ('Electric Sparks, Railway, A.wav', 'InspectorJ', BY4, P + "412/412313_5121236-hq.ogg"),
    426162: ('M224 Mortar Firing Close 02', 'Nanashi', BY4, P + "426/426162_61794-hq.ogg"),
    386306: ('Pneumatic hammer - Atlas Copco r4n - Start 1', 'ldezem', CC0, P + "386/386306_1655965-hq.ogg"),
    434710: ('Carrige_Coupler.wav', 'pkri', CC0, P + "434/434710_2798390-hq.ogg"),
    437205: ('Pneumatic grease bomb', 'prnkys', BY3, P + "437/437205_688025-hq.ogg"),
    530209: ('Debris Falling on Tank 1.wav', 'Richard27', CC0, P + "530/530209_6027544-hq.ogg"),
    585575: ('Metal Thud', 'BennettFilmTeacher', CC0, P + "585/585575_7254895-hq.ogg"),
    640204: ('Heavy Metal Thud on Ground', '7of9Designs', CC0, P + "640/640204_12131847-hq.ogg"),
    668071: ('50 cal shot.wav', 'ajhartman42295', CC0, P + "668/668071_7842170-hq.ogg"),
    734930: ('Metallic door - Open & close', 'Vrymaa', CC0, P + "734/734930_13973196-hq.ogg"),
    769890: ('Fast Freight Train Clickety-Clack over Track Joint', 'WeenyBeany', BY4, P + "769/769890_15119557-hq.ogg"),
}


# ---- the synthesized ticks (signals: the briefs allow a synthesized electronic tick)

def _tick(freq, n, decay):
    """mine-blink-r60-a's tick: a sine with a 0.8 ms attack and an exponential decay."""
    t = np.arange(n) / SR
    return np.sin(2 * np.pi * freq * t) * np.minimum(1.0, t / 0.0008) * np.exp(-t / decay)


def mine_arm(v):
    """a: the chosen blink's tick, lower and once doubled: two dry ticks 25 ms apart at 1.6 kHz ('tk-tk');
    b: a short two-note chirp, 1.2 then 1.8 kHz, 30 ms each, square-edged ('bi-dip')."""
    n = int(0.1 * SR)
    x = np.zeros(n)
    if v == "a":
        for at, g in ((0.005, 1.0), (0.030, 0.8)):
            a = int(at * SR)
            m = int(0.012 * SR)
            x[a:a + m] += g * (_tick(1600.0, m, 0.0025) + 0.3 * _tick(3300.0, m, 0.0015))
        x = svf(x, 500.0, q=0.7, mode="hp")
    else:
        for at, f in ((0.005, 1200.0), (0.040, 1800.0)):
            a = int(at * SR)
            m = int(0.030 * SR)
            tt = np.arange(m) / SR
            s = np.sin(2 * np.pi * f * tt)
            env = np.minimum(1.0, tt / 0.002) * np.minimum(1.0, (0.030 - tt) / 0.004)
            x[a:a + m] += (0.6 * s + 0.4 * np.tanh(4 * s)) * env
        x = svf(x, 4000.0, q=0.6)
    return np.vstack([x, x])


def buoy_tick(v):
    """a: one soft high tick at 3.2 kHz with a 40 ms shimmer (a second partial at 4.9 kHz ringing out: 'tink');
    b: two dry ticks at 2.4 kHz, 60 ms apart ('tik . tik'), drier than a."""
    n = int(0.1 * SR)
    x = np.zeros(n)
    if v == "a":
        a = int(0.005 * SR)
        m = int(0.06 * SR)
        x[a:a + m] += _tick(3200.0, m, 0.004) + 0.35 * _tick(4900.0, m, 0.014)
    else:
        for at in (0.005, 0.065):
            a = int(at * SR)
            m = int(0.012 * SR)
            x[a:a + m] += _tick(2400.0, m, 0.002)
    x = svf(x, 800.0, q=0.7, mode="hp")
    return np.vstack([x, x])


SYNTH = {"mine-arm": mine_arm, "buoy-tick": buoy_tick}

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


# subject -> (kind, level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "synth": None (SYNTH[subject](v)), levelled as a shot; "loop": (layers, length,
#             xfade), levelled by its band RMS at most `cap` LUFS.
L30 = "../../campaign/act-5-the-belt/level-30-minefield/README.md"
MINE = "../../enemies/space/minelayer/README.md"
GUNSHIP = "../../enemies/air/gilded-gunship/README.md"
SERPENT = "../../enemies/air/rail-serpent/README.md"
DART = "../../enemies/air/dart-drone/README.md"
CHAIN = [(0.00, 1.1, 0.0, 0.30), (0.08, 1.2, -2.0, 0.30), (0.16, 1.3, -4.0, 0.62)]  # (at, speed, gain, length)
SOUNDS = {
    "mine-arm": ("synth", -32.0, -10.0, -30.0, "Minelayer: a mine arming",
                 "each laid mine arming one second after it is dropped (its dim light turning to the red-gold "
                 "blink): a line of 12 arms over about 3 s, up to three lines at once in Level 30",
                 "a short dry electronic tick (synthesized allowed: a signal, not a machine), quiet; the family of "
                 "the chosen proximity blink (concept/mine-blink-r60-a.ogg), which follows when the ship comes near",
                 f"{MINE}#the-mine-line-q3-a", {
        "a": (None, "the chosen blink's tick, lower and doubled: two dry ticks at 1.6 kHz 25 ms apart: 'tk-tk', "
                    "0.05 s"),
        "b": (None, "a short two-note chirp, 1.2 then 1.8 kHz, 30 ms each, square-edged: 'bi-dip', 0.07 s; more "
                    "of a beep than a"),
    }),
    "mine-drop": ("shot", -30.0, -8.0, -28.0, "Minelayer: a mine dropped",
                  "each mine the Minelayer drops as it strafes across the field (12 in about 3.2 s, a line every "
                  "few seconds in Level 30)",
                  "a dull metal clunk, short and quiet: a heavy canister let go", f"{MINE}#sounds", {
        "a": ([L(640204, 0.14, 0.4, fin=0.002, fout=0.2, hp=60.0)],
              "a heavy metal weight dropped on dirt: a dull low 'thud', 0.4 s"),
        "b": ([L(585575, 0.08, 0.45, speed=0.75, fin=0.002, fout=0.25, hp=80.0)],
              "a metal bottle dropped on carpet, slowed to 0.75: a dull 'tunk' with a short metal ring, brighter "
              "than a, 0.45 s"),
    }),
    "mine-chain": ("shot", -21.0, -4.0, -19.0, "Mine line: the chain ripple",
                   "a shot mine setting off the mines within 40 px, hop by hop (0.08 s a hop): one file for the "
                   "chain, played 0.08 s after the shot mine's own burst (concept/mine-burst-r60-a.ogg); one shot "
                   "clears its cluster of 4",
                   "a quick ripple of smaller blasts: the chosen mine burst's metallic bang, three times, each "
                   "smaller (round 60: the ripple is cut from the pick); a crackle allowed, no popping",
                   f"{MINE}#the-mine-line-q3-a", {
        "a": ([L(244394, 0.0, ln, gain=g, at=at, speed=sp, fin=0.001, fout=0.15 if ln < 0.5 else 0.4, hp=110.0)
               for at, sp, g, ln in CHAIN],
              "the chosen burst's bang three times, 0.08 s apart, each smaller and higher (sped 1.1, 1.2, 1.3), "
              "the last ringing out: 'ba-ba-BANG' rippling off, 0.8 s"),
        "b": ([L(244394, 0.0, ln, gain=g - 2.0, at=at, speed=sp, fin=0.001, fout=0.15 if ln < 0.5 else 0.4,
                 hp=110.0) for at, sp, g, ln in CHAIN]
              + [L(530209, 0.0, 0.9, gain=-7.0, at=0.06, fin=0.005, fout=0.5, hp=400.0)],
              "the same ripple with a metal debris crackle after it (rocks and bricks rattling onto a steel plate, "
              "high-passed): a longer, crackling tail than a, 1.0 s"),
    }),
    "serpent-shot": ("shot", -28.0, -6.0, -24.0, "Rail Serpent: a turret car's shot",
                     "each turret car's aimed single shot (every 2.4 s, the turret cars 0.6 s apart: a shot about "
                     "every 0.8 s per train; hard: a burst of 2); Level 30 and later Act 5 levels",
                     "a turret's single shot, short and dry, the enemy shot small's weight (the unit document has "
                     "the turret cars fire the enemy shot small: concept/enemy-shot-small-r08-a.ogg plays if "
                     "neither is picked)", f"{SERPENT}#stat-block", {
        "a": ([L(668071, 0.0, 0.35, fin=0.001, fout=0.2, hp=150.0)],
              "a .50 BMG's single shot at an outdoor range: a sharp 'CRACK' with a short boom, 0.35 s"),
        "b": ([L(104399, 0.0, 0.22, speed=0.9, fin=0.001, fout=0.1, hp=100.0)],
              "an anti-aircraft gun's shot slowed to 0.9: a duller, deeper 'thok' than a, 0.22 s"),
    }),
    "serpent-rumble": ("loop", -34.0, -1.0, -29.0, "Rail Serpent: the train's rumble",
                       "one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per "
                       "kind); Level 30 and later Act 5 levels",
                       "a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, "
                       "seamless", f"{SERPENT}#behaviour", {
        "a": (([Loop(769890, 13.4, speed=0.85, hp=40.0, lp=6000.0)], 6.0, 0.8),
              "a cement freight train passing close at 90 km/h over a track joint, slowed to 0.85: a heavy rumble "
              "with a fast, irregular clickety-clack, 6.0 s loop"),
        "b": (([Loop(249425, 26.5, speed=0.9, hp=40.0, lp=6000.0)], 6.0, 0.8),
              "an empty freight train crossing a steel bridge, slowed to 0.9: a steady roaring iron rumble, the "
              "clatter blurred into it; smoother and further off than a, 6.0 s loop"),
    }),
    "serpent-split": ("shot", -24.0, -5.0, -22.0, "Rail Serpent: a car split",
                      "a car (not the engine) destroyed: the train cut, the cars behind the cut losing power and "
                      "drifting for 1.5 s, sparking and smoking, before they blow car by car (their `medium` "
                      "rungs); a few times a train",
                      "a coupler snap, then a falling power-down whine with electric sparks over the 1.5 s drift",
                      f"{SERPENT}#behaviour", {
        "a": ([L(434710, 6.12, 0.35, fin=0.001, fout=0.2, hp=80.0),
               L(151288, 0.2, 1.4, gain=-4.0, at=0.05, speed=(1.0, 0.6), fin=0.02, fout=0.6, hp=150.0),
               L(412313, 4.6, 1.5, gain=-9.0, at=0.1, fin=0.05, fout=0.6, hp=1000.0)],
              "a rail carriage's coupler clanking open, a machine's motor winding down (glided from 1.0 to 0.6) "
              "and a railway's power line sparking under them: 'KLANK - wheeoouu' over a crackle, 1.6 s"),
        "b": ([L(161650, 0.38, 0.3, fin=0.001, fout=0.15, hp=200.0),
               L(434710, 14.08, 0.3, gain=-3.0, speed=0.85, fin=0.001, fout=0.2, hp=80.0),
               L(186961, 0.8, 1.4, gain=-5.0, at=0.05, speed=(2.0, 1.4), fin=0.02, fout=0.6, hp=150.0),
               L(396240, 1.5, 1.5, gain=-9.0, at=0.1, fin=0.05, fout=0.6, hp=1000.0)],
              "a chain snapping over a coupler's clank (slowed to 0.85), a machine's whine stuttering and dropping "
              "(sped from 2.0 to 1.4) and an arc welder's sparks: a sharper snap and a harsher, buzzier crackle "
              "than a, 1.6 s"),
    }),
    "gunship-ring": ("shot", -25.0, -5.0, -22.0, "Gilded Gunship: the ring",
                     "each Gilded Gunship's ring of 12 slow bullets (every 3.0 s, its gold ring lamps lighting "
                     "0.4 s before); 2-3 Gunships in a column, Level 30's convoy",
                     "a hollow cannon thump, heavy and short: the ring fired at once", f"{GUNSHIP}#behaviour", {
        "a": ([L(426162, 0.0, 1.0, fin=0.001, fout=0.6, hp=60.0)],
              "an M224 mortar fired 15 feet away: a hollow 'thoomp' with a short rolling tail, 1.0 s"),
        "b": ([L(184382, 5.98, 0.8, fin=0.001, fout=0.5, hp=60.0)],
              "an 81 mm mortar's shot: darker and boomier than a, a shorter tail, 0.8 s"),
    }),
    "gunship-doors": ("shot", -27.0, -6.0, -25.0, "Gilded Gunship: the bay doors",
                      "each Dart stream (every 5 s a Gunship launches 3 Darts out of its belly bays, 0.25 s apart): "
                      "the doors once, before the first Dart's launch",
                      "a heavy metallic door clank", f"{GUNSHIP}#behaviour", {
        "a": ([L(275441, 2.36, 0.5, fin=0.002, fout=0.3, hp=60.0),
               L(275441, 4.17, 0.25, gain=-4.0, at=0.32, fin=0.001, fout=0.15, hp=200.0)],
              "a heavy steel door slamming and its lock snapping in: a deep 'KLUNK-tchak', 0.57 s"),
        "b": ([L(734930, 6.38, 0.7, speed=0.75, fin=0.002, fout=0.35, hp=80.0)],
              "an antique iron oven door's clank slowed to 0.75: a ringing iron 'KLANG', more metal and less "
              "weight than a, 0.7 s"),
    }),
    "dart-launch": ("shot", -31.0, -8.0, -29.0, "Dart Drone: the launch",
                    "each Dart launched from a Gunship's bay (streams of 3, 0.25 s apart, every 5 s per Gunship; "
                    "2-4 on easy/hard), and the Iron Sovereign's bays later",
                    "a short pneumatic launch thump (real pneumatics), quiet enough that a stream does not drown "
                    "the music", f"{DART}#behaviour", {
        "a": ([L(131304, 0.19, 0.3, speed=0.8, fin=0.001, fout=0.15, hp=120.0)],
              "a pneumatic nail gun's single shot slowed to 0.8: a dry 'tchunk' with a short air hiss, 0.3 s"),
        "b": ([L(386306, 0.0, 0.07, speed=0.7, fin=0.001, fout=0.03, hp=80.0),
               L(437205, 0.36, 0.25, gain=-10.0, at=0.02, speed=0.75, fin=0.002, fout=0.15, hp=300.0, lp=4000.0)],
              "a pneumatic hammer's first blow slowed to 0.7 with a grease gun's air blast under it (low-passed): "
              "a rounder, heavier 'thunk-ff' than a, 0.27 s"),
    }),
    "buoy-tick": ("synth", -40.0, -16.0, -38.0, "Beacon buoys: the strobe tick",
                  "a beacon buoy's strobe as it passes (one flash in 2 s, white), Level 30's shipping lane",
                  "a faint electronic tick (synthesized), barely heard", f"{L30}#layout", {
        "a": (None, "one soft high tick at 3.2 kHz with a short shimmer ringing out: 'tink', 0.06 s"),
        "b": (None, "two dry ticks at 2.4 kHz, 60 ms apart: 'tik . tik', drier than a, 0.08 s"),
    }),
}


def layers_of(kind, spec):
    if kind == "synth":
        return []
    return spec[0] if kind == "loop" else spec


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    spec, what = SOUNDS[subject][8][v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
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


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 63 ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind == "loop":
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
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
    else:
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
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        kind, _, _, _, _, use, should, _, variants = SOUNDS[subject]
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
        info[(subject, v)] = (secs, what)
        size = f"{secs:.2f} s{' loop' if kind == 'loop' else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-63/: the same depth as design/audio/sfx)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        kind, _, _, _, title, use, should, part, variants = SOUNDS[subject]
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
        for v in spec[8]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
