#!/usr/bin/env python3
"""Concept round 66 (M9 part F "Level 33 Leech Field", step AUD-F): Level 33's new sounds, a and b each
(design/audio/sfx/README.md, *Act 5 sounds*; the Level 33 document's *Music & ambience*; the Void Leech's and the Rail
Bunker's sound briefs; the generator's drain effects): the Void Leech's latch, drain whine, latched hum loop, shake-off
scrape and chitin death, the generator's brown-out warning (the one-shot round 60 left to the part) and the Rail
Bunker's death layer. Not proposed (used as they are): the brown-out hum loop (round 60 S.10 a, in assets/ since part A),
the rail line's charge and shot (round 60 S.7 b and S.8 b, chosen, imported by step F4), the hex shield's hit, break and
refill (round 60 S.1 a, S.2 b, S.3 a, in the game since part A), the belt's open-space bed (round 60 S.18 a, in the game
since part C), the Talon's sounds (round 62).

Every recorded layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 66 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, faded,
filtered, looped, pulsed (the hum's 1 Hz throb) and levelled. Synthesized: only the brown-out warning (the generator's
README asks for a synthesized two-note warning tone, the warnings' family: the edge warning's ping, sfx_r11.py). The
tools are sfx_r65.py's (copied: preview, build, the --docs rows) plus sfx_r62.py's synthesized sound.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  leech-latch-r66-*     ≈ 0.4-0.55 s: a Void Leech latching on the hull, a dry clamp
  leech-drain-r66-*     ≈ 1.0 s: the output dropping as a leech starts to drain, a falling electrical whine (a motor
                        spinning down)
  leech-hum-r66-*       seamless 4 s LOOPs: the drain while any leech is latched, a low electrical hum (a: throbbing
                        once a second with the sac's pulse); its level steps with the latched count in the game
  leech-shake-r66-*     ≈ 0.7 s: the shake-off throwing the leeches off, claws scraping on metal
  leech-death-r66-*     ≈ 0.5 s: over the `small` rung, a dry chitin crack with a short spark
  brownout-warn-r66-*   ≈ 0.4-0.5 s: the load first passing the drained output, a falling two-note warning (synthesized)
  bunker-death-r66-*    ≈ 1.0-1.2 s: over the `medium` rung, a heavy metal crash with the rail coil shorting out

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; a loop by its band RMS): the latch and the shake-off as the Lamprey's (the player-damage level), the whine a
few dB under the latch, the hum at the three-leech level a little over the brown-out hum (−35 dB band RMS), the death
as round 62's Talon shot, the warning at the warning tones' level, the bunker's crash as round 65's Sentinel death.

Usage: python3 tools/concept/audio/sfx_r66.py [subject-or-file ...]   e.g. leech-hum brownout-warn-r66-b
       python3 tools/concept/audio/sfx_r66.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
SCRIPT = "tools/concept/audio/sfx_r66.py"
R = "r66"
ROUND = "round 66"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    88564: ('staple_gun_02.wav', 'movingplaid', BY4, P + "88/88564_593444-hq.ogg"),
    121193: ('crack nut.wav', 'gusgus26', CC0, P + "121/121193_2147722-hq.ogg"),
    169666: ('electricspark.wav', 'knova', BY4, P + "169/169666_3133255-hq.ogg"),
    222676: ('Power Buzz SFX loop', 'unfa', CC0, P + "222/222676_1038806-hq.ogg"),
    336625: ('shell crunch - wide 05.wav', 'Anthousai', CC0, P + "336/336625_5923045-hq.ogg"),
    386084: ('Angle grinder - Milwaukee 125mm - Stop 1 time', 'ldezem', CC0, P + "386/386084_1655965-hq.ogg"),
    386196: ('Leg vise - Clang', 'ldezem', CC0, P + "386/386196_1655965-hq.ogg"),
    423117: ('Guns & Explosions Album - Metal impact 5.wav', 'OGsoundFX', BY4, P + "423/423117_3325582-hq.ogg"),
    438494: ('G32-10-Massive Electrical Discharges.wav', 'craigsmith', CC0, P + "438/438494_2524442-hq.ogg"),
    476943: ('Electric motor engine start stop', 'kentspublicdomain', CC0, P + "476/476943_5583936-hq.ogg"),
    512049: ('Bone crack', 'shtran', CC0, P + "512/512049_5259687-hq.ogg"),
    523058: ('Chambering 1', 'magnuswaker', CC0, P + "523/523058_11537497-hq.ogg"),
    551497: ('ratchet socket wrench tool', 'AlaskaRobotics', CC0, P + "551/551497_1282865-hq.ogg"),
    553075: ('Foley_Mechanism_Light_Buzz_Short_Loop_Mono_DR05.wav', 'Nox_Sound', CC0, P + "553/553075_9250976-hq.ogg"),
    567609: ('Hard-drive spin-up/spin-down', 'SamsterBirdies', CC0, P + "567/567609_5487341-hq.ogg"),
    672951: ('scratching metal with fingernails 01', 'rubberduck9999', CC0, P + "672/672951_14687392-hq.ogg"),
    707638: ('metal plate scratch and hit', 'Ridderick', CC0, P + "707/707638_5351360-hq.ogg"),
    707639: ('metal plate scratch', 'Ridderick', CC0, P + "707/707639_5351360-hq.ogg"),
    849772: ('Electric Sparks', 'mikiko850', BY4, P + "849/849772_18554592-hq.ogg"),
    859154: ('Loud Bang Crashing Metal Chunk Dropped in Metal Dumpster', 'qubodup', CC0, P + "859/859154_71257-hq.ogg"),
}


# ---- the synthesized brown-out warning (a signal: the generator's README asks for a synthesized two-note warning in
#      the warnings' family, the edge warning's ping: a sine with a faint 2.01 partial, a fast attack and a decay)

def _ping(freq, length, tau, sag=0.0):
    """A warning ping: a sine (and a 2.01 partial fading faster), 2 ms attack, exponential decay; `sag` lowers the
    pitch by that fraction over the note (the brown-out's droop)."""
    t = np.arange(int(length * SR)) / SR
    f = freq * (1.0 - sag * t / length)
    ph = 2 * np.pi * np.cumsum(f) / SR
    x = np.sin(ph) + 0.3 * np.sin(2.01 * ph) * np.exp(-t / 0.06)
    env = np.clip(t / 0.002, 0, 1) * np.exp(-t / tau) * np.clip((length - t) / 0.01, 0, 1)
    return x * env


def brownout_warn(v):
    """a: 'dee-doo', two pings a minor third down (1.6 kHz, then 1.35 kHz; 0.17 s apart), the edge warning's own ping;
    b: 'beep-bowww', a held beep and one a fifth lower (1.2 kHz, then 800 Hz sagging 8 % as it fades), longer and
    more of a sag than a."""
    x = np.zeros(int(0.55 * SR))
    if v == "a":
        notes = [(0.005, 1600.0, 0.16, 0.10, 0.0), (0.175, 1350.0, 0.30, 0.14, 0.0)]
    else:
        notes = [(0.005, 1200.0, 0.14, 0.25, 0.0), (0.165, 800.0, 0.34, 0.20, 0.08)]
    for at, f, ln, tau, sag in notes:
        b = _ping(f, ln, tau, sag)
        a = int(at * SR)
        x[a:a + len(b)] += b
    x = svf(x, 6000.0, q=0.6)
    return np.vstack([x, x])


SYNTH = {"brownout-warn": brownout_warn}

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


def pulsed(st, depth_db, rate=1.0):
    """The loop throbbing `rate` times a second: its level dipping `depth_db` between the beats (a raised cosine on
    dB, the beat at the loop's start); seamless when the loop is a whole number of beats long."""
    t = np.arange(st.shape[1]) / SR
    return st * undb(-depth_db * (0.5 - 0.5 * np.cos(2 * np.pi * rate * t)))


# subject -> (kind, level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, pulse dB]), levelled by its band RMS at most `cap`
#             LUFS; "synth": None (SYNTH[subject](v)), levelled as a shot.
LEECH = "../../enemies/space/void-leech/README.md"
BUNKER = "../../enemies/ground/rail-bunker/README.md"
DRAIN = "../../player/generator/README.md#enemy-drain-effects"

SOUNDS = {
    "leech-latch": ("shot", -24.0, -5.0, -22.0, "Void Leech: the latch",
                    "a Void Leech latching on the ship's hull (the left, right or tail spot; at most 3 latched, 2 on "
                    "easy; Level 33's rear and side streams), panned to its spot; a leech latching on a prey Talon "
                    "plays it quieter",
                    "a dry clamp: a heavy staple gun or a ratchet biting on metal; not the Lamprey's wet latch "
                    "(concept/lamprey-latch-r34-a.ogg, a suck and bite): this one is dry and mechanical",
                    f"{LEECH}#sounds", {
        "a": ([L(88564, 0.0, 0.4, speed=0.75, fin=0.001, fout=0.2, hp=60.0),
               L(386196, 0.0, 0.4, gain=-9.0, at=0.01, fin=0.001, fout=0.25, hp=300.0)],
              "a heavy staple gun fired into metal, slowed to 0.75, a vise's clang faint under it: one hard dry "
              "'TCHAK' with a short ring, 0.4 s"),
        "b": ([L(551497, 0.08, 0.3, speed=0.65, fin=0.003, fout=0.1, hp=150.0),
               L(523058, 0.17, 0.3, gain=-1.0, at=0.24, speed=0.85, fin=0.001, fout=0.2, hp=100.0)],
              "a socket wrench's ratchet slowed to 0.65 biting, then a rifle bolt's clack locking it: a two-stage "
              "'krrrk-CHAK', busier and longer than a, 0.55 s"),
    }),
    "leech-drain": ("shot", -28.0, -6.0, -26.0, "Void Leech: the drain whine",
                    "each leech starting to drain (with its latch: the generator's output −2 MW, stacking to −6): "
                    "once per latch, from 0.1 s after the clamp; the latched hum below carries the drain after it",
                    "a falling electrical whine: a motor spinning down, the power being pulled out of the ship",
                    f"{LEECH}#sounds", {
        "a": ([L(386084, 1.0, 1.1, speed=(1.0, 0.8), fin=0.03, fout=0.4, hp=200.0, lp=9000.0)],
              "an angle grinder spinning down after its trigger is let go, glided from its own speed to 0.8: a "
              "gritty high whine falling 'vwiiiooow', 1.1 s"),
        "b": ([L(476943, 4.3, 1.0, speed=(1.4, 1.0), fin=0.03, fout=0.4, hp=60.0)],
              "an electric motor stopping, sped from 1.4 to its own speed: a lower, smoother hum falling away "
              "'mmmmooowm' than a, 1.0 s"),
    }),
    "leech-hum": ("loop", -33.0, -1.0, -28.0, "Void Leech: the latched hum (loop)",
                  "one loop while any leech is latched on the ship (not on a Talon), its level stepping with the "
                  "latched count (the file is the three-leech level: one leech −6 dB, two −3 dB); under the "
                  "brown-out hum when the load passes the output (concept/brownout-hum-r60-a.ogg, a transformer's "
                  "sag), cut at once when the last leech is shaken off, killed or thrown off by the EMP",
                  "a low electrical hum, dry, the ship's power being sucked out; distinct from the brown-out hum and "
                  "the Lamprey's drain (concept/lamprey-drain-r46-b.ogg, a Tesla coil's hum)",
                  f"{LEECH}#sounds", {
        "a": (([Loop(222676, 1.0, hp=160.0, lp=6000.0)], 4.0, 0.5, 5.0),
              "a mains power buzz (its 50 Hz fundamental filtered out, its raw harmonics from 150 Hz up kept), throbbing once a second with the leech's "
              "sac (dipping 5 dB between the beats): 'bzzZZ bzzZZ', 4.0 s loop"),
        "b": (([Loop(553075, 2.0, speed=0.8, hp=100.0, lp=7000.0),
                Loop(567609, 3.0, gain=-4.0, hp=40.0, lp=3000.0)], 4.0, 0.5),
              "a light fitting's buzz slowed to 0.8 over a hard drive's 120 Hz spin hum: a steady, thinner and "
              "higher 'zzzzz' than a, no throb, 4.0 s loop"),
    }),
    "leech-shake": ("shot", -24.0, -5.0, -22.0, "Void Leech: the shake-off",
                    "the shake-off (four reversals in 1.2 s) throwing every latched leech off at once, and the "
                    "EMP throwing them off: once per throw, however many leeches",
                    "a scrape of claws on metal: the leeches torn off the hull; not the Lamprey's wet slap "
                    "(concept/lamprey-shake-r34-a.ogg)", f"{LEECH}#sounds", {
        "a": ([L(707638, 0.38, 0.25, gain=-4.0, fin=0.001, fout=0.15, hp=80.0),
               L(672951, 0.5, 0.7, at=0.04, speed=0.7, fin=0.01, fout=0.3, hp=300.0, lp=9000.0)],
              "a metal plate knocked, then fingernails dragged across metal, slowed to 0.7: a 'tok' into a raspy "
              "claw screech 'skrrrriik', 0.75 s"),
        "b": ([L(707639, 3.0, 0.7, speed=0.85, fin=0.01, fout=0.3, hp=120.0)],
              "a metal plate scraped hard, slowed to 0.85: a lower, grittier grinding scrape 'shhkrrrk' than a, no "
              "knock, 0.7 s"),
    }),
    "leech-death": ("shot", -24.0, -5.0, -22.0, "Void Leech: its death (over the `small` rung)",
                    "a Void Leech killed (free, latched on the ship or on a Talon), over its `small` death: up to "
                    "a stream of 4-6 in a few seconds",
                    "a dry chitin crack with a violet spark: a shell cracking, short; no splat, no pop",
                    f"{LEECH}#stat-block", {
        "a": ([L(121193, 0.63, 0.3, speed=0.5, fin=0.001, fout=0.12, hp=200.0),
               L(169666, 0.0, 0.45, gain=-9.0, at=0.04, fin=0.005, fout=0.25, hp=1500.0)],
              "a nut cracking, slowed to 0.5, a short electric spark fizzing after it: a hard 'KRAK-tsss', 0.5 s"),
        "b": ([L(512049, 0.33, 0.42, speed=0.8, fin=0.005, fout=0.15, hp=150.0),
               L(336625, 0.28, 0.2, gain=-4.0, at=0.03, fin=0.001, fout=0.12, hp=200.0),
               L(849772, 0.6, 0.25, gain=-10.0, at=0.05, fin=0.003, fout=0.15, hp=2000.0)],
              "a bone cracking, slowed to 0.8, with a shell crunch on it and a quick spark snap: a duller, "
              "crunchier 'KRRCH-tk' than a, 0.45 s"),
    }),
    "brownout-warn": ("synth", -25.0, -3.0, -23.0, "Generator brown-out: the warning",
                      "the ship's load first passing the drained output (a second latched leech on the hand plan; "
                      "once per drop below the load, not again while it stays below); the brown-out hum loop "
                      "(concept/brownout-hum-r60-a.ogg) starts with it",
                      "a short falling two-note warning tone (synthesized: the warnings' family, as the edge "
                      "warning's ping, concept/ui-edge-warning-r11-b.ogg); uneasy, not an alarm", DRAIN, {
        "a": (None, "two pings a minor third down (1.6 kHz, then 1.35 kHz, 0.17 s apart), the edge warning's own "
                    "ping: a light 'dee-doo', 0.5 s"),
        "b": (None, "a beep and one a fifth lower (1.2 kHz, then 800 Hz sagging 8 % as it fades): a heavier, "
                    "drooping 'beep-bowww' than a, 0.5 s"),
    }),
    "bunker-death": ("shot", -20.0, -4.0, -18.0, "Rail Bunker: its death (over the `medium` rung)",
                     "a Rail Bunker destroyed (hardened: only an `anti-ground` weapon, a special, a mining charge "
                     "or a Strider's fall kills one; Level 33's asteroid faces, later Level 35's hull), over its "
                     "`medium` ground explosion; its telegraph, if running, is cancelled",
                     "a heavy metal explosion: the turret wrecked, its rail coil shorting out", f"{BUNKER}#sounds", {
        "a": ([L(423117, 0.0, 1.0, fin=0.001, fout=0.5, hp=40.0),
               L(438494, 2.45, 0.9, gain=-6.0, at=0.1, fin=0.02, fout=0.6, hp=150.0)],
              "a heavy metal impact, a massive electrical discharge buzzing out under its tail (the coil "
              "shorting): 'KLANG-bzzzrt', 1.0 s"),
        "b": ([L(859154, 0.0, 1.2, fin=0.001, fout=0.6, hp=40.0),
               L(849772, 2.45, 0.3, gain=-8.0, at=0.12, fin=0.003, fout=0.15, hp=1500.0),
               L(849772, 3.0, 0.4, gain=-10.0, at=0.5, fin=0.003, fout=0.25, hp=1500.0)],
              "a metal chunk crashing into a steel dumpster, sparks spitting twice after it: a deeper, rattling "
              "'BLAMM ... tsk tsk' than a, 1.2 s"),
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
        st = loop(*spec[:3])
        if len(spec) > 3:
            st = pulsed(st, spec[3])
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


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 66 ----

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
        if len(spec) > 3:
            parts.append(f"pulsed once a second ({num(spec[3])} dB dips between the beats)")
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
    # the round's rows (links relative to design/concept-rounds/round-66/: the same depth as design/audio/sfx)
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
