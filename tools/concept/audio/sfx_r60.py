#!/usr/bin/env python3
"""Concept round 60 (M9 "Act 5 - The Belt" design phase, step AUD-P): the Act 5 sounds the whole act shares, a and b
each (design/audio/sfx/README.md, *Act 5 sounds*): the Ascendancy's shared sets (the enemy shield's hit, break and
refill; the homing missile's launch, run and shot down; the rail line's charge and shot; the turbines), the generator
brown-out, the enemy channel's intercept open, close and carrier, the mine's blink and burst, the Iron Sovereign's
ring turn and the three belt beds. The units' own sounds come in their parts' rounds.

Every sfx layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 60 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, glided, layered, swelled, faded,
filtered, looped and levelled. The one synthesized subject is the mine's proximity blink (the README allows a
synthesized electronic tick: a signal, not a machine). The tools are sfx_r58h.py's (copied: preview, bed, build,
the --docs rows) plus a `synth` kind.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  enemy-shield-hit-r60-*      ≈ 0.2 s: a glass tapped with metal (the hex shield hit)
  enemy-shield-break-r60-*    ≈ 0.35 s: tempered glass bursting (the shield emptied)
  enemy-shield-refill-r60-*   ≈ 0.7 s: a soft rising electrical shimmer
  homing-launch-r60-*         ≈ 0.6 s: a short hissing rocket launch
  homing-run-r60-*            seamless 1 s LOOPs: a thin rocket-motor hiss
  homing-down-r60-*           ≈ 0.45 s: a small dry crack with a metal ring
  rail-charge-r60-*           1.0 s: a rising capacitor whine over a low hum (the telegraph)
  rail-shot-r60-*             ≈ 0.9 s: a violent electric crack with a metallic ring
  ascendancy-turbine-r60-*    seamless 6 s LOOPs: a turbine idle (pitched per hull in the game)
  brownout-hum-r60-*          seamless 8 s LOOPs: a sagging transformer hum with relay chatter
  intercept-open-r60-*        ≈ 0.35 s: a shortwave sweep landing on a carrier
  intercept-close-r60-*       ≈ 0.3 s: the sweep falling away
  intercept-carrier-r60-*     seamless 8 s LOOPs: a faint carrier hiss with a slow swell
  mine-blink-r60-*            0.6 s: a dry electronic tick, faster and rising (synthesized)
  mine-burst-r60-*            ≈ 1.0 s: a sharp metallic blast
  sovereign-ring-turn-r60-*   seamless 8 s LOOPs: a deep grind with slow gear-tooth ticks
  belt-station-r60-*          seamless 16 s LOOPs: a big station heard through the hull
  belt-open-r60-*             seamless 16 s LOOPs: open space heard through the hull
  belt-tunnel-r60-*           seamless 16 s LOOPs: an asteroid's interior

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the shield and the missile's run under the fire, the rail shot and
the mine burst as hits, the beds at the Act 4 beds' level.

Usage: python3 tools/concept/audio/sfx_r60.py [subject-or-file ...]   e.g. rail-shot mine-burst-r60-b
       python3 tools/concept/audio/sfx_r60.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets
and the loop seams.
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
SCRIPT = "tools/concept/audio/sfx_r60.py"
R = "r60"
ROUND = "round 60"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    19016: ('anvil strike 4.wav', 'l0calh05t', BY4, P + "19/19016_50162-hq.ogg"),
    19020: ('anvil strike 8.wav', 'l0calh05t', BY4, P + "19/19020_50162-hq.ogg"),
    60904: ('bottleRocket4.wav', 'nicStage', BY4, P + "60/60904_1198-hq.ogg"),
    67516: ('Howitzer Gun Shots', 'qubodup', CC0, P + "67/67516_71257-hq.ogg"),
    74914: ('ferry engine room1.wav', 'mrmayo', CC0, P + "74/74914_271213-hq.ogg"),
    74915: ('ferry engine room2.wav', 'mrmayo', CC0, P + "74/74915_271213-hq.ogg"),
    93100: ('Whip Crack 01.wav', 'CGEffex', BY4, P + "93/93100_1386366-hq.ogg"),
    101246: ("metal glass makes long 'PING!' sound.aif", 'Tomlija', BY3, P + "101/101246_1163166-hq.ogg"),
    118515: ('Windshield_Hit_02.wav', 'thefsoundman', CC0, P + "118/118515_2136023-hq.ogg"),
    119902: ('Camera Flash Charging.aif', 'ftpalad', CC0, P + "119/119902_1990698-hq.ogg"),
    162363: ('Distant Explosion of Propelled Grenade', 'qubodup', CC0, P + "162/162363_71257-hq.ogg"),
    172411: ('Creaking Metal - Slow', 'EagleStealthTeam', CC0, P + "172/172411_2915633-hq.ogg"),
    187734: ('Idle F-22 Jet Plane.flac', 'qubodup', CC0, P + "187/187734_71257-hq.ogg"),
    209772: ('Metal Boom', 'Johnnyfarmer', CC0, P + "209/209772_2012390-hq.ogg"),
    221528: ('Glass Break', 'unfa', CC0, P + "221/221528_1038806-hq.ogg"),
    244394: ('Bang/Explosion Metallic', 'Werra', CC0, P + "244/244394_525427-hq.ogg"),
    257879: ('Short Wave Radio Noise 2', 'zmobie', CC0, P + "257/257879_3262745-hq.ogg"),
    257880: ('Short Wave Radio Noise 1', 'zmobie', CC0, P + "257/257880_3262745-hq.ogg"),
    320788: ('distant explosions', 'Kostrava', CC0, P + "320/320788_1134415-hq.ogg"),
    345846: ('Inside a traditional Dutch windmill, 2', 'pcwvdmark', CC0, P + "345/345846_4622540-hq.ogg"),
    369485: ('Earthquake  **audible only with appropriate speakers**', 'MBARI_MARS', CC0,
             P + "369/369485_6181234-hq.ogg"),
    371353: ('Iron Hits.wav', 'Mrthenoronha', CC0, P + "371/371353_2402876-hq.ogg"),
    384698: ('A relay switch being activated.', 'bassmosphere', CC0, P + "384/384698_1067129-hq.ogg"),
    400997: ('Deep Thumps.wav', 'eardeer', CC0, P + "400/400997_2155630-hq.ogg"),
    403252: ('Taser/High Voltage discharge', 'The_Chemical_Workshop', CC0, P + "403/403252_7809148-hq.ogg"),
    437890: ('G08-27-Jet Idle or Taxi.wav', 'craigsmith', CC0, P + "437/437890_2524442-hq.ogg"),
    453328: ('machine metal vibrating grinding drilling hole maybe.flac', 'kyles', CC0,
             P + "453/453328_612689-hq.ogg"),
    453444: ('hum electric transformer in large room1.flac', 'kyles', CC0, P + "453/453444_612689-hq.ogg"),
    453541: ('room tone large industrial heavy ventilation fan like inflated building3 distant softer.flac', 'kyles',
             CC0, P + "453/453541_612689-hq.ogg"),
    453546: ('room tone large industrial ventilation fan like inflated building.flac', 'kyles', CC0,
             P + "453/453546_612689-hq.ogg"),
    500008: ('liner gangplank groaning.WAV', 'phonoflora', CC0, P + "500/500008_401348-hq.ogg"),
    529620: ('Massive Structure Bend', 'Kostas17', BY3, P + "529/529620_3377875-hq.ogg"),
    549895: ('large wine glass.wav', 'Tairblenn', CC0, P + "549/549895_11663790-hq.ogg"),
    552160: ('Radio tuning-static-interference', 'quantumriver', CC0, P + "552/552160_716433-hq.ogg"),
    587887: ('Explosion in a can', 'SamsterBirdies', CC0, P + "587/587887_5487341-hq.ogg"),
    609930: ('drilling a rock in a career', 'charleswassner', BY3, P + "609/609930_10475987-hq.ogg"),
    615171: ('radio02.wav', 'theplax', BY4, P + "615/615171_12172648-hq.ogg"),
    616084: ('Rising electric hum.aif', 'jukkako', CC0, P + "616/616084_2981994-hq.ogg"),
    639019: ('Jackhammer, pneumatic drill in underground mine, very loud motor', 'felix.blume', CC0,
             P + "639/639019_1661766-hq.ogg"),
    648726: ('power transformer in basement.wav', 'itinerantmonk108', CC0, P + "648/648726_2397507-hq.ogg"),
    675760: ('S16-18 Tesla coil sparking and humming.wav', 'craigsmith', CC0, P + "675/675760_2524442-hq.ogg"),
    709192: ('pulsating vibrating Shortwave radio noise', 'Mick_', CC0, P + "709/709192_8356325-hq.ogg"),
    730041: ('hobby rocket blastoff', 'reg7783', CC0, P + "730/730041_2394357-hq.ogg"),
    733736: ('50Hz  electrical mains hum', 'chungus43A', CC0, P + "733/733736_12024110-hq.ogg"),
    807384: ('Contactor/motor overload relay noises', 'tt_runscript', CC0, P + "807/807384_5365416-hq.ogg"),
    812360: ('Rail Grinding Machine with Rythmic Clang Berlin Night', 'giuliorasi', CC0,
             P + "812/812360_14865205-hq.ogg"),
    835810: ('knife-tapping-wine-glass', 'BogumilaMerc', CC0, P + "835/835810_18186550-hq.ogg"),
    840511: ('Rocket Launch', 'qubodup', CC0, P + "840/840511_71257-hq.ogg"),
    854699: ('50 Cal Sniper Shot 1', 'qubodup', CC0, P + "854/854699_71257-hq.ogg"),
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


# ---- the synthesized mine blink (a signal: the README allows a synthesized electronic tick)

def _tick(freq, n, decay):
    t = np.arange(n) / SR
    return np.sin(2 * np.pi * freq * t) * np.minimum(1.0, t / 0.0008) * np.exp(-t / decay)


def mine_blink(v):
    """a: dry ticks, 13 in 0.6 s, the gap shrinking from 110 to 25 ms and the pitch rising 2.0 -> 3.2 kHz;
    b: short square-ish beeps, the gap shrinking from 120 to 45 ms and the pitch rising 1.2 -> 2.4 kHz, the last
    one held 90 ms (the burst is next)."""
    n = int(0.62 * SR)
    x = np.zeros(n)
    t = 0.01
    if v == "a":
        k, gaps = 0, np.geomspace(0.110, 0.025, 13)
        for g in gaps:
            u = k / (len(gaps) - 1)
            a = int(t * SR)
            m = min(int(0.012 * SR), n - a)
            x[a:a + m] += (_tick(2000.0 * 1.6 ** u, m, 0.0025) + 0.3 * _tick(4100.0 * 1.6 ** u, m, 0.0015))[:m]
            t += g
            k += 1
        x = svf(x, 600.0, q=0.7, mode="hp")
    else:
        gaps = np.geomspace(0.120, 0.045, 7)
        for k, g in enumerate(gaps):
            u = k / (len(gaps) - 1)
            f = 1200.0 * 2.0 ** u
            ln = 0.09 if k == len(gaps) - 1 else 0.028
            a = int(t * SR)
            m = min(int(ln * SR), n - a)
            tt = np.arange(m) / SR
            beep = np.sign(np.sin(2 * np.pi * f * tt)) * 0.6 + np.sin(2 * np.pi * f * tt) * 0.4
            env = np.minimum(1.0, tt / 0.002) * np.minimum(1.0, (ln - tt) / 0.004)
            x[a:a + m] += beep * env
            t += g
        x = svf(x, 3500.0, q=0.6)
    return np.vstack([x, x])


SYNTH = {"mine-blink": mine_blink}

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS;
#             "synth": None (SYNTH[subject](v)), levelled as a shot.
SHIELD_UNITS = "the Talon, the Hornet, the Gilded Gunship and the Iron Sovereign's core (the shared hex shield)"
MISSILE_UNITS = ("the Hornet's pods, the SAM Nest, the Strider's pod and the Iron Sovereign's SAM batteries (the "
                 "shared homing diamond; Levels 29-35)")
RAIL_UNITS = "the Rail Bunker's rail, the Iron Sovereign's rail batteries and (its long version) Level 35's mass relay"
BEDS = ("the act's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, "
        "a level's start and the pause before a boss")
SOUNDS = {
    "enemy-shield-hit": ("shot", -27.0, -6.0, -26.0,
                         f"every hit on a hex shield of {SHIELD_UNITS}, throttled to once per 0.15 s per unit: often, "
                         "under the fire",
                         "a small glassy tick: a glass tapped with metal, dry and short; no ring-out, no pop", {
        "a": ([L(835810, 0.40, 0.18, fin=0.001, fout=0.1, hp=400.0)],
              "a knife tapping a wine glass: a bright dry 'tink', 0.18 s"),
        "b": ([L(549895, 0.0, 0.16, speed=0.85, fin=0.002, fout=0.09, hp=300.0),
               L(19020, 0.02, 0.1, gain=-12.0, speed=1.6, fin=0.001, fout=0.06, hp=1500.0)],
              "a large wine glass tapped, slowed to 0.85 (a little lower and fuller than a), with the tip of an "
              "anvil strike sped up under it for the metal: a 'tick' with more body, 0.16 s"),
    }),
    "enemy-shield-break": ("shot", -22.0, -5.0, -22.0,
                           f"a hex shield of {SHIELD_UNITS} emptied (by damage, a `shield-breaker` hit or the EMP): "
                           "now and then, a clear reward",
                           "a brittle shatter: tempered glass bursting, about 0.3 s, small enough for a Talon", {
        "a": ([L(118515, 0.0, 0.38, fin=0.001, fout=0.18, hp=250.0)],
              "a hit on a car's windshield: a hard crack and the safety glass crazing, 0.38 s"),
        "b": ([L(221528, 0.30, 0.36, fin=0.003, fout=0.16, hp=250.0),
               L(549895, 0.0, 0.2, gain=-9.0, speed=1.3, fin=0.001, fout=0.12, hp=600.0)],
              "a glass breaking, cut to the burst and its first shards, over the wine glass's tap sped up (the "
              "shield's own 'tick' in the burst): brighter and more splintery than a, 0.36 s"),
    }),
    "enemy-shield-refill": ("shot", -29.0, -8.0, -28.0,
                            f"a hex shield of {SHIELD_UNITS} starting to refill after its delay: now and then",
                            "a soft rising electrical shimmer, quiet: a feedback cue that the shield is back", {
        "a": ([L(616084, 4.0, 0.7, speed=(1.2, 2.4), fin=0.15, fout=0.25, hp=500.0, swell=(-8.0, 1.0)),
               L(549895, 0.0, 0.5, gain=-14.0, at=0.2, speed=0.7, fin=0.1, fout=0.3, hp=500.0)],
              "a rising electric hum, its pitch gliding up an octave over the cue and swelling in, the wine glass's "
              "ring slowed to 0.7 faint under its end: 0.7 s"),
        "b": ([L(675760, 3.0, 0.7, speed=(0.8, 1.4), fin=0.2, fout=0.25, hp=700.0, swell=(-10.0, 1.0))],
              "a Tesla coil humming and sparking, high-passed to its fizz and gliding up: a crackling shimmer, "
              "more electric than a, 0.7 s"),
    }),
    "homing-launch": ("shot", -22.0, -5.0, -22.0,
                      f"each homing-missile launch of {MISSILE_UNITS}; pitched per launcher in the game (the Hornet's "
                      "pods higher, the SAM racks lower); a salvo plays it 2-4 times in a row",
                      "a short hissing rocket launch (a model rocket or a firework's launch, cut short); the round "
                      "compares it with reusing the chosen enemy missile launch (concept/enemy-missile-r08-a.ogg)", {
        "a": ([L(840511, 0.0, 0.6, fin=0.002, fout=0.3, hp=120.0)],
              "a rocket's launch: an ignition and a hard rushing hiss, cut short, 0.6 s"),
        "b": ([L(60904, 1.76, 0.55, fin=0.002, fout=0.3, hp=150.0),
               L(730041, 4.4, 0.5, gain=-6.0, fin=0.02, fout=0.3, hp=200.0)],
              "a bottle rocket going off (a sharp ignition and a thin whooshing hiss) over a hobby rocket's burn: "
              "thinner and higher than a, 0.55 s"),
    }),
    "homing-run": ("loop", -36.0, -1.0, -30.0,
                   "one quiet loop while any homing missile flies (Level 31's 'missile run'), for the nearest",
                   "a thin rocket-motor hiss, quiet, steady, under the fire; no whistle", {
        "a": (([Loop(730041, 4.5, hp=400.0, lp=7000.0)], 1.0, 0.3),
              "a hobby rocket's burn at its steadiest, thinned (high-passed at 400 Hz): a fizzing hiss, 1.0 s "
              "loop"),
        "b": (([Loop(840511, 0.3, speed=1.1, hp=300.0, lp=6000.0)], 1.0, 0.3),
              "the rocket launch's sustained rush sped to 1.1: rougher and more of a roar than a, 1.0 s loop"),
    }),
    "homing-down": ("shot", -24.0, -5.0, -23.0,
                    f"a homing missile shot down (by the player's fire or a shield-breaker): often in a salvo",
                    "a small dry crack with a metal ring; no pop, smaller than an enemy's death", {
        "a": ([L(93100, 0.26, 0.3, fin=0.001, fout=0.15, hp=200.0),
               L(19016, 0.08, 0.45, gain=-8.0, at=0.005, speed=1.2, fin=0.001, fout=0.3, hp=500.0)],
              "a whip crack's dry snap with an anvil strike sped to 1.2 ringing under it: 'crack-ting', 0.45 s"),
        "b": ([L(162363, 0.0, 0.42, fin=0.001, fout=0.25, hp=150.0),
               L(371353, 0.55, 0.4, gain=-9.0, at=0.01, fin=0.001, fout=0.25, hp=400.0)],
              "a distant propelled grenade's burst (a dry thud-crack) with a struck iron bar's ring: darker and "
              "more of a blast than a, 0.42 s"),
    }),
    "rail-charge": ("shot", -25.0, -5.0, -24.0,
                    f"the rail line's 1.0 s telegraph: {RAIL_UNITS}; the shot follows at its end",
                    "a rising capacitor whine (a camera flash charging, pitched down) over a low hum, exactly as long "
                    "as the telegraph (1.0 s), building to the shot", {
        "a": ([L(119902, 2.0, 1.0, speed=(0.45, 0.9), fin=0.1, fout=0.03, hp=300.0, swell=(-12.0, 1.2)),
               L(733736, 1.0, 1.0, gain=-6.0, fin=0.15, fout=0.03, hp=40.0, lp=800.0, swell=(-6.0, 1.0))],
              "a camera flash's charging whine pitched down and gliding up an octave, swelling, over a 50 Hz mains "
              "hum: 'wheeeee' rising to the shot, 1.0 s"),
        "b": ([L(616084, 12.0, 1.0, speed=(0.7, 1.5), fin=0.1, fout=0.03, hp=150.0, swell=(-10.0, 1.4)),
               L(453444, 4.0, 1.0, gain=-5.0, speed=0.8, fin=0.2, fout=0.03, hp=40.0, lp=900.0)],
              "a rising electric hum gliding up, swelling hard at its end, over a transformer's hum slowed to 0.8: "
              "lower and more of a building drone than a, 1.0 s"),
    }),
    "rail-shot": ("shot", -18.0, -4.0, -18.0,
                  f"each rail shot: {RAIL_UNITS} (the mass relay plays it with a long thunder tail, made at the part)",
                  "a violent electric crack with a short metallic ring (a large spark gap or a whip crack layered on "
                  "a heavy gunshot)", {
        "a": ([L(403252, 0.80, 0.5, fin=0.003, fout=0.3, hp=150.0),
               L(854699, 0.0, 0.9, gain=-3.0, fin=0.001, fout=0.5, hp=50.0),
               L(19020, 0.02, 0.8, gain=-9.0, at=0.01, speed=0.8, fin=0.001, fout=0.5, hp=300.0)],
              "a high-voltage discharge's crack on a .50 calibre sniper shot, an anvil strike slowed to 0.8 ringing "
              "under them: a hard electric 'KRACK' with a metal ring and the rifle's boom, 0.9 s"),
        "b": ([L(93100, 0.25, 0.4, fin=0.001, fout=0.25, hp=150.0),
               L(67516, 0.0, 0.9, gain=-2.0, fin=0.002, fout=0.5, hp=40.0),
               L(101246, 0.0, 0.9, gain=-10.0, at=0.01, speed=0.9, fin=0.001, fout=0.6, hp=400.0)],
              "a whip crack on a howitzer's shot, a long metal 'PING' under them: heavier and more of a cannon than "
              "a, the ring brighter, 0.9 s"),
    }),
    "ascendancy-turbine": ("loop", -35.0, -1.0, -30.0,
                           "the engine loops of the Gilded Gunship, the Hornet and the Minelayer, once per kind, "
                           "for the nearest; pitched per hull in the game (the Gunship this pitch, the Hornet higher, "
                           "the Minelayer lower)",
                           "a real jet or turbine idle: a deep turbine drone, quiet, under the enemy fire", {
        "a": (([Loop(187734, 6.0, speed=0.6, hp=40.0, lp=5000.0)], 6.0, 0.8),
              "an F-22's idle slowed to 0.6: a deep steady turbine drone with its high whine kept faint, 6.0 s loop"),
        "b": (([Loop(437890, 8.0, speed=0.65, hp=40.0, lp=5000.0)], 6.0, 0.8),
              "a jet idling or taxiing slowed to 0.65: rougher, with a throbbing low end and a thinner whine than "
              "a, 6.0 s loop"),
    }),
    "brownout-hum": ("loop", -34.0, -1.0, -28.0,
                     "a loop while the generator's drained output is below the load (the Void Leech's drain, the "
                     "Shard Drone's links): the player's ship browning out",
                     "a low sagging hum (a transformer under load, its pitch drooping) with a faint relay chatter; "
                     "uneasy, not an alarm", {
        "a": (([Loop(453444, 6.0, speed=0.85, hp=40.0, lp=2500.0)], 8.0, 1.0,
               {"events": [L(384698, 0.0, 0.2, gain=-14.0, at=1.3, fin=0.001, fout=0.05, hp=300.0),
                           L(384698, 0.0, 0.2, gain=-16.0, at=1.55, speed=0.9, fin=0.001, fout=0.05, hp=300.0),
                           L(384698, 0.0, 0.2, gain=-15.0, at=5.2, speed=1.1, fin=0.001, fout=0.05, hp=300.0)],
                "pulse": (2.0, 5.0)}),
              "a transformer's hum in a large room slowed to 0.85, sagging 5 dB every 2 s, a relay clicking twice "
              "and once more now and then: 8.0 s loop"),
        "b": (([Loop(648726, 2.0, speed=0.8, hp=150.0, lp=2500.0)], 8.0, 1.0,
               {"events": [L(807384, 13.0, 3.0, gain=-10.0, at=2.0, fin=0.05, fout=0.4, hp=300.0)],
                "pulse": (4.0, 6.0)}),
              "a power transformer in a basement slowed to 0.8, sagging 6 dB every 4 s, a contactor and motor "
              "relays chattering once a loop: buzzier than a, 8.0 s loop"),
    }),
    "intercept-open": ("shot", -24.0, -6.0, -24.0,
                       "an enemy-channel line (the HUD) or an intercepted briefing page opening, instead of the "
                       "squelch: Vorne and Harker",
                       "a short static sweep: a shortwave receiver tuned across a band onto a strong carrier, 0.3 s, "
                       "no crackle", {
        "a": ([L(552160, 2.4, 0.26, speed=(1.8, 0.9), fin=0.02, fout=0.06, hp=300.0, lp=5000.0),
               L(257880, 3.0, 0.14, gain=-3.0, at=0.21, fin=0.04, fout=0.08, hp=400.0, lp=4000.0)],
              "a radio tuned through static, played gliding down from 1.8 to 0.9, landing on a steady shortwave "
              "noise: 'shhwip-hhh', 0.35 s"),
        "b": ([L(615171, 1.5, 0.3, speed=(0.7, 1.4), fin=0.02, fout=0.08, hp=300.0, lp=5000.0, swell=(-6.0, 1.0))],
              "an AM radio tuned across the band, gliding up and swelling in: a rising sweep, 0.3 s"),
    }),
    "intercept-close": ("shot", -26.0, -6.0, -25.0,
                        "an enemy-channel line or intercepted page closing",
                        "the open's sweep falling away, 0.3 s, no crackle", {
        "a": ([L(552160, 6.0, 0.3, speed=(1.0, 0.5), fin=0.005, fout=0.22, hp=300.0, lp=5000.0)],
              "the radio's static gliding down from 1.0 to 0.5 and fading: a falling 'hhwoo', 0.3 s"),
        "b": ([L(615171, 4.0, 0.3, speed=(1.2, 0.6), fin=0.005, fout=0.22, hp=300.0, lp=5000.0)],
              "the AM radio's sweep gliding down and fading, 0.3 s"),
    }),
    "intercept-carrier": ("loop", -38.0, -1.0, -32.0,
                          "an intercepted briefing page while its text types, in place of the typing blip (Vorne's "
                          "broadcast pages, the act's intro)",
                          "a faint steady carrier hiss with a slow swell; distinct from the Choir's static lead "
                          "(concept/choir-static-r55-b.ogg) so the two enemy voices never sound alike", {
        "a": (([Loop(257879, 4.0, hp=400.0, lp=4000.0)], 8.0, 1.0, {"pulse": (4.0, 3.0)}),
              "a shortwave radio's noise band-passed to 400 Hz-4 kHz, swelling 3 dB every 4 s: a soft even 'hhhh', "
              "8.0 s loop"),
        "b": (([Loop(709192, 20.0, hp=400.0, lp=4000.0)], 8.0, 1.0),
              "a pulsating shortwave noise band-passed the same: the interference's own slow throb in it, 8.0 s "
              "loop"),
    }),
    "mine-blink": ("synth", -24.0, -6.0, -24.0,
                   "an armed mine's 0.6 s proximity blink as the ship comes near (the Minelayer's mine lines, Level "
                   "30's minefield), before its burst",
                   "a faster tick, rising, over the 0.6 s blink (synthesized allowed: a signal, not a machine); dry, "
                   "no beep melody", {
        "a": (None, "13 dry ticks, the gap shrinking from 110 to 25 ms and the pitch rising from 2.0 to 3.2 kHz: "
                    "'tik . tik . tik tik-tiktiktik', 0.6 s"),
        "b": (None, "7 short square-ish beeps, the gap shrinking from 120 to 45 ms and the pitch rising an octave "
                    "from 1.2 kHz, the last held: 'bip . bip . bip-bip-bipbeeep', 0.6 s"),
    }),
    "mine-burst": ("shot", -19.0, -4.0, -19.0,
                   "a mine bursting (proximity, shot or in a chain); the chain's ripple is cut from the pick at the "
                   "part",
                   "a sharp metallic blast, the `small` rung's weight but harder (a mine must be heard)", {
        "a": ([L(244394, 0.0, 1.0, fin=0.001, fout=0.6, hp=110.0)],
              "a metallic bang: a hard crack with a steel body ringing into the boom, 1.0 s"),
        "b": ([L(587887, 0.0, 1.0, fin=0.003, fout=0.6, hp=50.0),
               L(209772, 0.35, 1.0, gain=-5.0, at=0.0, fin=0.002, fout=0.6, hp=35.0)],
              "an explosion in a metal can over a deep metal boom: tighter and tinnier at the top than a, heavier "
              "underneath, 1.0 s"),
    }),
    "sovereign-ring-turn": ("loop", -32.0, -1.0, -27.0,
                            "Level 35's boss fight: a low bed while the Iron Sovereign's rings turn (the spin-up, "
                            "the ring step and the lock are separate one-shots, made at the boss's part)",
                            "a deep grind with slow gear-tooth ticks: a huge bearing turning, heavy and mechanical", {
        "a": (([Loop(812360, 120.0, speed=0.5, hp=60.0, lp=2500.0)], 8.0, 1.0),
              "a rail-grinding machine at night with its rhythmic clang, slowed to half: a deep grinding drone, "
              "the clangs slowed into heavy ticks, 8.0 s loop"),
        "b": (([Loop(345846, 10.0, speed=0.55, hp=120.0, lp=2000.0),
                Loop(453444, 12.0, gain=-5.0, speed=0.6, hp=60.0, lp=600.0)], 8.0, 1.0),
              "inside a working windmill, its great gears and millstone turning, slowed to 0.55, over a "
              "transformer's hum slowed to 0.6: slower, creakier ticks than a over a heavier bed, 8.0 s loop"),
    }),
    "belt-station": ("loop", -42.0, -1.0, -29.0,
                     f"Level 29 (Ceres Hub's dock canyon) and Level 35 (the Helix yards in Ceres orbit): {BEDS}; "
                     "Level 29 adds the hub's alarm as a layer from the turn",
                     "a big station heard through the hull: a deep ventilation roar, a low transformer hum, distant "
                     "dock machinery (a crane's slew, a heavy hatch now and then), hull creaks; no voices, no PA; a "
                     "seamless 16 s loop", {
        "a": (([Loop(453541, 6.0, hp=30.0, lp=1500.0),
                Loop(453444, 10.0, gain=-8.0, speed=0.9, hp=40.0, lp=900.0)], 16.0, 1.0,
               {"events": [L(209772, 0.3, 1.8, gain=-12.0, at=4.0, speed=0.8, fin=0.01, fout=1.0, hp=35.0,
                             lp=600.0),
                           L(500008, 10.0, 4.0, gain=-13.0, at=10.0, speed=0.7, fin=0.6, fout=1.5, hp=60.0,
                             lp=1200.0)]}),
              "a large industrial ventilation fan's room tone muffled at 1.5 kHz over a transformer's hum; once a "
              "loop a far heavy metal boom (a hatch) and a liner's gangplank groaning, slowed (the dock): 16.0 s "
              "loop"),
        "b": (([Loop(74915, 10.0, speed=0.7, hp=60.0, lp=900.0),
                Loop(453546, 4.0, gain=-6.0, hp=60.0, lp=2000.0)], 16.0, 1.0,
               {"events": [L(812360, 200.0, 5.0, gain=-14.0, at=3.0, speed=0.6, fin=1.0, fout=1.5, hp=60.0,
                             lp=1000.0),
                           L(172411, 6.0, 4.0, gain=-14.0, at=11.0, speed=0.6, fin=0.5, fout=1.5, hp=80.0,
                             lp=1500.0)]}),
              "a ferry's engine room slowed to 0.7 and muffled (heard through a bulkhead) under a ventilation fan; "
              "once a loop a far rail-grinding machine's clanking (the dock machinery) and a slow metal creak: "
              "more of a working machine than a, 16.0 s loop"),
    }),
    "belt-open": ("loop", -42.0, -1.0, -29.0,
                  f"Levels 30, 31, 33 and 34 (the shipping lane, the rock fields, the leech field, the graveyard): "
                  f"{BEDS}",
                  "open space heard through the hull (no silent vacuum): the cockpit's low hum, hull creaks and "
                  "ticks of metal in the cold, distant dull impacts (rocks on rock, far off), a faint radio hiss; no "
                  "wind; a seamless 16 s loop", {
        "a": (([Loop(437890, 3.0, speed=0.5, hp=80.0, lp=600.0),
                Loop(709192, 2.0, gain=-18.0, hp=500.0, lp=3500.0)], 16.0, 1.0,
               {"events": [L(172411, 2.0, 4.0, gain=-11.0, at=2.5, speed=0.7, fin=0.4, fout=1.5, hp=80.0,
                             lp=2500.0),
                           L(320788, 26.5, 3.0, gain=-8.0, at=8.0, speed=0.8, fin=0.05, fout=1.5, hp=25.0,
                             lp=300.0),
                           L(172411, 20.0, 3.0, gain=-13.0, at=12.5, speed=0.8, fin=0.4, fout=1.2, hp=80.0,
                             lp=2500.0)]}),
              "a jet's idle slowed to half and muffled at 600 Hz (the cockpit's hum), a pulsating shortwave hiss far "
              "under it; twice a loop a slow metal creak, once a distant explosion's dull thud muffled to a far "
              "impact: 16.0 s loop"),
        "b": (([Loop(74914, 6.0, speed=0.5, hp=80.0, lp=600.0),
                Loop(709192, 40.0, gain=-20.0, hp=500.0, lp=3500.0)], 16.0, 1.0,
               {"events": [L(500008, 30.0, 4.0, gain=-12.0, at=3.0, speed=0.6, fin=0.6, fout=1.5, hp=60.0,
                             lp=1500.0),
                           L(400997, 0.0, 2.0, gain=-9.0, at=9.5, speed=0.8, fin=0.01, fout=1.0, hp=25.0,
                             lp=300.0)]}),
              "a ferry's engine room slowed to half and muffled at 600 Hz, a pulsating shortwave hiss far under "
              "it; once a loop a liner's gangplank groaning, slowed (the hull), and a deep far thump: darker and "
              "more of a hull than a, 16.0 s loop"),
    }),
    "belt-tunnel": ("loop", -42.0, -1.0, -29.0,
                    f"Level 32 (the hollowed asteroid; the refinery's furnace roar under its section 6 is Level 19's "
                    f"vent and furnace sounds): {BEDS}",
                    "an asteroid's interior: rock creaking and groaning, pumps and ventilation fans through the "
                    "walls, a distant drill biting rock now and then, a machine thump; no drips; a seamless 16 s loop",
                    {
        "a": (([Loop(453546, 12.0, speed=0.8, hp=30.0, lp=1200.0)], 16.0, 1.0,
               {"events": [L(639019, 20.0, 4.0, gain=-14.0, at=2.0, speed=0.8, fin=0.8, fout=1.2, hp=80.0,
                             lp=900.0),
                           L(529620, 1.0, 4.0, gain=-9.0, at=7.5, speed=0.7, fin=0.6, fout=1.5, hp=30.0, lp=600.0),
                           L(400997, 0.0, 1.5, gain=-10.0, at=13.0, speed=0.9, fin=0.01, fout=0.8, hp=30.0,
                             lp=400.0)]}),
              "a ventilation fan's room tone slowed to 0.8 and muffled; once a loop a jackhammer in an underground "
              "mine far off through the rock, a massive structure's slow bend (the rock groaning) and a deep "
              "machine thump: 16.0 s loop"),
        "b": (([Loop(453541, 20.0, speed=0.6, hp=60.0, lp=800.0),
                Loop(453328, 20.0, gain=-10.0, speed=0.6, hp=60.0, lp=700.0)], 16.0, 1.0,
               {"events": [L(369485, 4.8, 5.0, gain=-9.0, at=4.0, speed=0.7, fin=1.0, fout=2.0, hp=40.0, lp=500.0),
                           L(609930, 1.0, 4.0, gain=-12.0, at=11.0, speed=0.85, fin=0.5, fout=1.2, hp=80.0,
                             lp=1000.0)]}),
              "a large industrial ventilation fan's room tone slowed to 0.6 (the pumps and fans) over a machine grinding and drilling, slowed "
              "and muffled to a drone in the walls; once a loop a deep earthquake rumble (the rock heaving) and a "
              "quarry drill biting rock far off: lower and rockier than a, 16.0 s loop"),
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


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md ----

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
    if kind == "loop":
        if not one_shot:
            parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the "
                         "start)")
        extra = spec[3] if len(spec) > 3 else {}
        if "pulse" in extra:
            parts.append(f"the loop dipping every {num(extra['pulse'][0])} s ({num(extra['pulse'][1])} dB deep)")
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
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                    f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
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
