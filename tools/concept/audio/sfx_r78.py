#!/usr/bin/env python3
"""Concept round 78 (M10 part F "Level 40 Honour Guard", step AUD-F; the gap check's QF-1): the Honour Guard's sounds
and Level 40's own, a and b each (the mid-boss's sounds brief in design/enemies/bosses/honour-guard/README.md and
design/audio/sfx/README.md, *Act 6 sounds*): an ace's fly-by and Kestrel's rougher one, Harker's sniper charge and
shot, Kestrel's dive scream and its pull-out roar, Vesper's looping cannon, an ace's death and the last ace's chain,
the recorder's crack (Level 40's secret pod) and the fleet's layer under Level 40's approach (`fleet-comms`).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 78 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, sped, glided, layered, swelled,
faded, filtered, repeated, looped and levelled: nothing is synthesized. Jets and guns as the brief asks: real fly-bys
(doppler), afterburners and take-offs; the sniper's charge is a camera flash's charging whine over the rail line's
rising hum (the enemy `laser-line` family, sharper), its shot a rifle's or a whip's crack with a spark's sizzle. No
bubbly or popping sources: the chain's explosions are deep and low-passed, the fizz a short circuit or sparks.
The tools are sfx_r75.py's (copied: preview, bed, build, the --docs rows), plus a "rhythm" kind for Vesper's cannon.

Vesper's cannon is a seamless loop of one real autocannon's shots (two shots alternating) at the spiral's rate (2
arms x 4 bullets a second = 8 shots a second), the channels summed to mono so the game can pan it with her loop
(SfxBank.loopPan) while the 2.5 s spiral runs. The sniper's charge is the 1.0 s line (medium; the game starts it
later or plays it faster for hard's 0.8 s and holds easy's 1.2 s at its start). Kestrel's dive rises over 1.8 s and
is cut by the pull-out's roar wherever the dive ends. An ace's death plays over its `medium` rung and the last ace's
chain before the `large` rung (the sfx README's explosion ladder): the death is the engine failing and metal
tumbling (no blast of its own), the chain the 2 s of detonations round the tumbling hull.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  ace-flyby-r78-{a,b}        ≈ 1.5 s: a real jet fly-by, short, doppler
  kestrel-flyby-r78-{a,b}    ≈ 1.7 s: a rougher jet fly-by (crackling, more low end)
  sniper-charge-r78-{a,b}    1.0 s: a rising electric whine, sharper than the rail line's, cut at the shot
  sniper-shot-r78-{a,b}      ≈ 0.7 s: a hard crack with a short sizzling tail
  kestrel-dive-r78-{a,b}     ≈ 1.8 s: a jet's dive scream rising (cut by the pull-out)
  kestrel-pullout-r78-{a,b}  ≈ 1.8 s: the pull-out's afterburner roar
  vesper-cannon-r78-{a,b}    seamless 2 s LOOPs: a light autocannon's fast rhythmic fire, 8 shots a second, mono
  ace-death-r78-{a,b}        ≈ 2.2 s: a jet engine failing (spooling down) and metal tumbling
  ace-last-chain-r78-{a,b}   ≈ 2.6 s: a 2 s chain of detonations (deep, low-passed) round a tumbling hull
  recorder-crack-r78-{a,b}   ≈ 0.8 s: a small metal housing splitting with an electrical fizz
  fleet-comms-r78-{a,b}      seamless 16 s LOOPs: a big ship's drive far through the hull and faint radio traffic as a
                             band-limited murmur (no words to follow)

Levels like the accepted picks (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the
loops by their band RMS): the fly-bys and the dive as Level 36's drop-craft pass (-24 dB), the charge and the shot as
the rail line's (-25 / -18 dB), the deaths as the units' over their rungs, the recorder as the Graft Turret's shell
crack (a little under: a P3 sound), the cannon as a loop under the pods' burst, the fleet layer as Level 37's burning
layer (under the beds). The two variants of a subject share the level, so they are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r78.py [subject-or-file ...]   e.g. sniper-shot ace-flyby-r78-b
       python3 tools/concept/audio/sfx_r78.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r78.py"
R = "r78"
ROUND = "round 78"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    62592: ("Engine Room .wav", "bsumusictech", CC0, P + '62/62592_791247-hq.ogg'),
    119902: ("Camera Flash Charging.aif", "ftpalad", CC0, P + '119/119902_1990698-hq.ogg'),
    121686: ("metal tumbling hits 1.wav", "Halleck", BY4, P + '121/121686_6281-hq.ogg'),
    121687: ("metal tumbling hits 2.wav", "Halleck", BY4, P + '121/121687_6281-hq.ogg'),
    125326: ("camera flash charge.wav", "adeluc4", CC0, P + '125/125326_2266204-hq.ogg'),
    162246: ("Red Arrows Hawk overhead", "surrey_film", CC0, P + '162/162246_1328296-hq.ogg'),
    162262: ("Firing 25mm from LAVS during training", "qubodup", CC0, P + '162/162262_71257-hq.ogg'),
    199272: ("F-15TakeoffBinaural.WAV", "daveincamas", BY4, P + '199/199272_111413-hq.ogg'),
    210878: ("highvoltagearc.wav", "Sclolex", CC0, P + '210/210878_985466-hq.ogg'),
    276283: ("atc004.flac", "kwahmah_02", BY3, P + '276/276283_4486188-hq.ogg'),
    276285: ("atc002.flac", "kwahmah_02", BY3, P + '276/276285_4486188-hq.ogg'),
    276291: ("F16 takeoff 2.WAV", "Sandermotions", CC0, P + '276/276291_1402315-hq.ogg'),
    336007: ("Multiple Deep Explosions (Noisy rec)", "Rudmer_Rotteveel", CC0, P + '336/336007_4921277-hq.ogg'),
    349712: ("Canadian CF-18 Highspeed Pass", "lonemonk", BY4, P + '349/349712_230160-hq.ogg'),
    378670: ("Falling scrap metal", "13FPanská_Tolar_David", CC0, P + '378/378670_7023873-hq.ogg'),
    403252: ("Taser/High Voltage discharge", "The_Chemical_Workshop", CC0, P + '403/403252_7809148-hq.ogg'),
    413312: ("Afterburner", "TiesWijnen", CC0, P + '413/413312_1511977-hq.ogg'),
    431473: ("F35 Jet Low-Flyby.mp3", "johnnythesalesman", BY3, P + '431/431473_6593477-hq.ogg'),
    438716: ("G45-14-Torpedo Explosion.wav", "craigsmith", CC0, P + '438/438716_2524442-hq.ogg'),
    456200: ("Radio Chatter RELIEF NEEDED.wav", "cpark12", CC0, P + '456/456200_4029822-hq.ogg'),
    477132: ("propulsion jet engine", "mycompasstv", CC0, P + '477/477132_3902754-hq.ogg'),
    529925: ("Whip Crack.m4a", "SciFiSounds", CC0, P + '529/529925_11751070-hq.ogg'),
    534856: ("Jet Engine", "m_cel", CC0, P + '534/534856_7108357-hq.ogg'),
    536809: ("Jet.ogg", "egomassive", CC0, P + '536/536809_1415754-hq.ogg'),
    560510: ("Explosion_Debris_Short_Stereo.wav", "Nox_Sound", CC0, P + '560/560510_9250976-hq.ogg'),
    576590: ("Short circuit, A", "Moulaythami", BY4, P + '576/576590_10196790-hq.ogg'),
    616084: ("Rising electric hum.aif", "jukkako", CC0, P + '616/616084_2981994-hq.ogg'),
    643856: ("spark3.ogg", "Kneeling", CC0, P + '643/643856_7811934-hq.ogg'),
    788131: ("SDL Jet Engine Spool Down", "guitarduane", BY4, P + '788/788131_16872537-hq.ogg'),
    829068: ("Afterburner (Close, Screamer, Quick Onset)", "JSK_Productions", CC0,
             P + '829/829068_17949749-hq.ogg'),
    854183: ("Autocannon Shot 1", "qubodup", CC0, P + '854/854183_71257-hq.ogg'),
    854185: ("Autocannon Shot 3", "qubodup", CC0, P + '854/854185_71257-hq.ogg'),
    855606: ("Sniper Shot in Field 1 (M2010 Enhanced Sniper Rifle ESR)", "qubodup", CC0,
             P + '855/855606_71257-hq.ogg'),
    865883: ("Crunching sound of a crushed aluminum can", "tosha73", BY4, P + '865/865883_10725617-hq.ogg'),
    868271: ("Breaking Plastic Hollow Crunching Crackling Break Snap - Fast & Soft", "ChristopherJngs", CC0,
             P + '868/868271_8154783-hq.ogg'),
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


def rhythm(shots, period, length):
    """A seamless loop of `length` s: a shot every `period` s, the `shots` taking turns, each wrapping round the
    seam; the channels summed to mono (the game pans it)."""
    n = int(round(length * SR))
    out = np.zeros(n)
    cuts = [cut(s).mean(axis=0) for s in shots]
    for k in range(int(round(length / period))):
        x = cuts[k % len(cuts)]
        idx = (int(round(k * period * SR)) + np.arange(len(x))) % n
        np.add.at(out, idx, x)
    return np.vstack([out, out])


SEAM_TRIES = 12  # a bed loop's candidate starts (see build)
ACES = "the Honour Guard (Level 40's mid-boss: Vorne's three black-and-gold aces)"
L40 = "Level 40 (Honour Guard)"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]) and "rhythm": (shots, period, length),
#             levelled by the band RMS at most `cap` LUFS.
SOUNDS = {
    "ace-flyby": ("shot", -24.0, -4.0, -22.0,
                  f"{ACES}: an ace (Harker or Vesper) passing near the ship, and Vesper's roll and ribbon passes; "
                  "often through the duel (one at a time)",
                  "a real jet fly-by, short, with its doppler: the whine rising in, the roar at the pass, the pitch "
                  "falling away", {
        "a": ([L(349712, 0.45, 1.5, speed=1.1, fin=0.25, fout=0.6, hp=60.0)],
              "a CF-18's high-speed pass at an air show sped to 1.1: a bright whine rushing in, a hard roar at 0.6 s "
              "and the pitch dropping away, 1.5 s"),
        "b": ([L(536809, 0.1, 1.5, speed=1.15, fin=0.12, fout=0.6, hp=60.0)],
              "a modern fighter screaming by sped to 1.15: on the ship almost at once, a tearing scream that falls "
              "away more slowly than a, 1.5 s"),
    }),
    "kestrel-flyby": ("shot", -24.0, -4.0, -22.0,
                      f"{ACES}: Kestrel passing near the ship and climbing back to its station after a pull-out; "
                      "often while it flies",
                      "the aces' fly-by, rougher: a jet with a crackling, growling exhaust, more low end than Harker's "
                      "and Vesper's", {
        "a": ([L(431473, 4.0, 1.7, speed=1.25, fin=0.3, fout=0.6, hp=40.0)],
              "an F-35's low fly-by sped to 1.25: a high whine coming in, then the engine's crackling, rumbling "
              "roar at the pass (a third of it under 200 Hz), 1.7 s"),
        "b": ([L(162246, 3.5, 1.7, speed=1.2, fin=0.3, fout=0.6, hp=40.0)],
              "a Hawk jet trainer roaring overhead sped to 1.2: a duller, throatier pass with a rumbling tail, "
              "less crackle than a, 1.7 s"),
    }),
    "sniper-charge": ("shot", -25.0, -5.0, -24.0,
                      f"{ACES}: Harker's sniper line, under the thin red line's 1.0 s (the medium telegraph; easy 1.2 "
                      "s, hard 0.8 s), cut by the shot; a few times a minute, twice or three times running in a "
                      "double or triple tap",
                      "a rising electric whine under the line, the enemy rail line's charge family but sharper and "
                      "higher, ending at the shot", {
        "a": ([L(616084, 12.0, 1.0, speed=(0.9, 1.9), fin=0.02, fout=0.02, hp=150.0, swell=(-12.0, 1.0)),
               L(119902, 2.0, 1.0, gain=-3.0, speed=(0.7, 1.3), fin=0.05, fout=0.02, hp=1500.0,
                 swell=(-14.0, 1.2))],
              "the rail line's rising electric hum (the same recording) gliding up twice as fast, with a camera "
              "flash's charging whine climbing from 3 to 7 kHz on top: a thin, sharp rising whine, 1.0 s"),
        "b": ([L(125326, 2.8, 1.0, speed=(0.75, 1.25), fin=0.05, fout=0.02, hp=2000.0, swell=(-14.0, 1.2)),
               L(210878, 0.5, 1.0, gain=-8.0, speed=(0.8, 1.2), fin=0.3, fout=0.02, hp=800.0, swell=(-18.0, 1.5))],
              "another camera flash charging, its whine gliding from 4 to 8 kHz, over a 20 kV transformer's arc "
              "buzz swelling in: brighter and more crackling than a, 1.0 s"),
    }),
    "sniper-shot": ("shot", -18.0, -4.0, -18.0,
                    f"{ACES}: Harker's sniper line firing (the 0.3 s laser line after the charge); a few times a "
                    "minute",
                    "a hard crack with a short sizzling tail", {
        "a": ([L(855606, 0.0, 0.6, fin=0.001, fout=0.3, hp=120.0),
               L(643856, 0.15, 0.6, gain=-9.0, at=0.04, fin=0.01, fout=0.4, hp=2000.0)],
              "a sniper rifle's shot in a field (a hard crack and its short boom) with a broken cord's sparks "
              "sizzling after it: 0.65 s"),
        "b": ([L(529925, 0.08, 0.4, fin=0.001, fout=0.2, hp=150.0),
               L(403252, 0.78, 0.5, gain=-8.0, at=0.05, fin=0.005, fout=0.35, hp=1500.0)],
              "a whip's crack (dry and sharp, no boom) with a high-voltage discharge's zap sizzling after it: "
              "lighter and more electric than a, 0.55 s"),
    }),
    "kestrel-dive": ("shot", -24.0, -4.0, -22.0,
                     f"{ACES}: Kestrel's feint dive and overhead feint, from the dive's start (after its 0.8 s tell) "
                     "until the pull-out, which cuts it; every few draws",
                     "a jet's dive scream rising, cut by the pull-out's roar", {
        "a": ([L(829068, 0.9, 1.8, speed=(0.95, 1.15), fin=0.3, fout=0.15, hp=80.0, swell=(-10.0, 1.0))],
              "a jet's afterburner screaming up from its quick onset, gliding a little faster as it goes (its "
              "pitch climbing from 1.4 to 3.5 kHz): a rising scream, 1.8 s"),
        "b": ([L(199272, 2.6, 1.8, speed=(1.4, 1.7), fin=0.3, fout=0.15, hp=80.0, swell=(-8.0, 1.0))],
              "an F-15's take-off as its whine climbs, sped up and gliding faster: a higher, thinner scream than a, "
              "1.8 s"),
    }),
    "kestrel-pullout": ("shot", -22.0, -4.0, -21.0,
                        f"{ACES}: Kestrel pulling out of a dive (110 px from the ship, 85 px once it commits) and "
                        "banking off as it fires its burst back; every few draws",
                        "the pull-out's roar: a jet at full power, an afterburner's roar cutting in", {
        "a": ([L(276291, 8.8, 1.8, fin=0.03, fout=0.8, hp=40.0)],
              "an F-16 at full power on its take-off roll: a broad tearing roar cutting in hard and fading as it "
              "banks away, 1.8 s"),
        "b": ([L(413312, 0.0, 1.8, fin=0.02, fout=0.8, hp=40.0)],
              "a turbojet's afterburner lighting: a bright blast at its onset, then a deep steady roar (more low "
              "end than a), 1.8 s"),
    }),
    "vesper-cannon": ("rhythm", -30.0, -6.0, -26.0,
                      f"{ACES}: Vesper's spiral loop, a loop for the 2.5 s of the loop while her tail fires the "
                      "2-arm spiral (8 shots a second), panned by the game with her as she loops; every few draws",
                      "a fast rhythmic cannon (a light autocannon), panning with the loop", {
        "a": (([L(854183, 0.0, 0.18, speed=1.3, fin=0.001, fout=0.08, hp=200.0),
                L(854185, 0.0, 0.18, gain=-1.5, speed=1.35, fin=0.001, fout=0.08, hp=200.0)], 0.125, 2.0),
              "a 25 mm autocannon's two single shots sped to 1.3 and 1.35, taking turns 8 times a second: a "
              "tight, dry, even hammering, 2.0 s loop"),
        "b": (([L(162262, 0.0, 0.18, speed=1.2, fin=0.001, fout=0.08, hp=150.0),
                L(162262, 0.22, 0.18, gain=-1.5, speed=1.25, fin=0.001, fout=0.08, hp=150.0)], 0.125, 2.0),
              "a light armoured vehicle's 25 mm firing on a range, two of its shots sped to 1.2 and 1.25 taking "
              "turns 8 times a second: rougher and boomier than a, with the range's echo in each shot, 2.0 s loop"),
    }),
    "ace-death": ("shot", -24.0, -5.0, -22.0,
                  f"{ACES}: an ace at 0 (its shield shatters, the hull tumbles away burning at the cockpit), over "
                  "the `medium` rung; twice a duel (the third is the chain)",
                  "a jet's engine failing and metal tumbling, over the medium explosion", {
        "a": ([L(788131, 0.0, 2.2, speed=(1.0, 0.4), fin=0.01, fout=0.8, hp=60.0),
               L(121686, 0.45, 1.8, gain=-4.0, at=0.25, fin=0.01, fout=0.6, hp=150.0)],
              "a jet engine spooling down, its whine gliding down to 0.4 (failing, not shut down), and metal pieces "
              "tumbling and knocking under it: 2.2 s"),
        "b": ([L(534856, 1.0, 2.2, speed=(1.0, 0.5), fin=0.01, fout=0.9, hp=120.0, lp=6000.0),
               L(378670, 1.1, 1.4, gain=-6.0, at=0.3, fin=0.01, fout=0.5, hp=200.0, lp=7000.0)],
              "a jet engine's roar on a test stand dying away (gliding down to half, darker than a's whine), "
              "scrap metal falling and rattling over it: 2.2 s"),
    }),
    "ace-last-chain": ("shot", -18.0, -3.0, -19.5,
                       f"{ACES}: the last ace at 0, the 2 s chain round its tumbling hull before the `large` rung "
                       "and the credit shower; once a duel",
                       "a longer chain: rapid deep detonations round a tumbling hull, never popping", {
        "a": ([L(336007, 0.15, 0.8, gain=-6.0, fin=0.005, fout=0.45, hp=40.0, lp=4000.0),
               L(336007, 1.75, 0.8, gain=-5.0, at=0.5, fin=0.005, fout=0.45, hp=40.0, lp=4000.0),
               L(336007, 4.4, 0.8, gain=-3.5, at=0.9, fin=0.005, fout=0.45, hp=40.0, lp=4000.0),
               L(336007, 5.95, 0.8, gain=-2.0, at=1.25, fin=0.005, fout=0.45, hp=40.0, lp=4000.0),
               L(336007, 9.0, 1.2, at=1.55, fin=0.005, fout=0.8, hp=40.0, lp=4000.0),
               L(121687, 0.8, 2.4, gain=-9.0, at=0.2, fin=0.05, fout=0.8, hp=150.0)],
              "five deep explosions muffled at 4 kHz, coming faster and louder (0.5, 0.4, 0.35 and 0.3 s apart), "
              "with metal tumbling under them: 2.75 s"),
        "b": ([L(560510, 0.0, 1.0, gain=-4.0, speed=1.15, fin=0.005, fout=0.5, hp=40.0, lp=5000.0),
               L(560510, 3.0, 1.0, gain=-2.0, at=0.65, speed=1.15, fin=0.005, fout=0.5, hp=40.0, lp=5000.0),
               L(560510, 8.0, 1.4, at=1.3, speed=1.1, fin=0.005, fout=0.8, hp=40.0, lp=5000.0),
               L(438716, 3.7, 2.6, gain=-6.0, at=0.1, speed=1.2, fin=0.1, fout=1.0, hp=30.0, lp=1500.0)],
              "three explosions with debris raining after each (0.65 s apart), over a torpedo's rolling deep "
              "detonations: fewer, bigger blasts than a, 2.7 s"),
    }),
    "recorder-crack": ("shot", -26.0, -6.0, -24.0,
                       f"{L40}: the dead scout's flight recorder (the secret pod) broken open, over the built crate "
                       "break; once a level at most",
                       "a small metal housing splitting with an electrical fizz", {
        "a": ([L(865883, 0.0, 0.25, fin=0.001, fout=0.1, hp=150.0),
               L(576590, 1.08, 0.8, gain=-5.0, at=0.06, fin=0.005, fout=0.5, hp=300.0)],
              "an aluminium can crushed under a heel (a sharp metal crunch) with a short circuit's fizz and its "
              "falling buzz after it: 0.85 s"),
        "b": ([L(868271, 0.0, 0.5, speed=0.7, fin=0.001, fout=0.15, hp=200.0, lp=7000.0),
               L(643856, 0.15, 0.7, gain=-6.0, at=0.08, fin=0.005, fout=0.4, hp=1500.0, lp=7000.0)],
              "a hollow plastic case cracking and snapping slowed to 0.7 (a lighter, drier crackle of small "
              "breaks than a's crunch) with a broken cord's sparks spitting after it: 0.8 s"),
    }),
    "fleet-comms": ("loop", -42.0, -1.0, -30.0,
                    f"{L40}, section 1 (the approach with Vorne's fleet, t=0 to the picket line): a layer over the "
                    "`belt-open` bed, under the music",
                    "the fleet's distant comms and engine rumble: a big ship's drive heard far through the hull, "
                    "faint radio traffic as a band-limited murmur (no words to follow); a seamless loop", {
        "a": (([Loop(477132, 4.0, speed=0.5, hp=30.0, lp=450.0),
                Loop(456200, 2.0, gain=-16.0, hp=500.0, lp=1400.0)], 16.0, 1.0),
              "a ferry's propulsion engines slowed to half and muffled at 450 Hz (a big ship's deep drive), with "
              "relief workers' radio chatter far under it, band-passed 500 Hz-1.4 kHz to a murmur all through the "
              "loop: 16.0 s loop"),
        "b": (([Loop(62592, 1.0, speed=0.45, hp=25.0, lp=400.0)], 16.0, 1.0,
               {"events": [L(276283, 1.0, 4.0, gain=-14.0, at=2.0, fin=0.3, fout=0.6, hp=500.0, lp=1400.0),
                           L(276285, 0.5, 4.0, gain=-15.0, at=9.5, fin=0.3, fout=0.6, hp=500.0, lp=1400.0)]}),
              "an engine room's machinery slowed to 0.45 and muffled at 400 Hz (a lower, steadier drive than a), "
              "with two air-traffic-control calls far off, band-passed to a murmur, twice a loop: 16.0 s loop"),
    }),
}


def layers_of(kind, spec):
    if kind == "rhythm":
        return list(spec[0])
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
                     for s in sources_of(kind, spec))
    looped = kind in ("loop", "rhythm")
    if looped:
        st = bed(*spec) if kind == "loop" else rhythm(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (a rhythm's within its first 10 ms so the shots stay on the grid) to where both
        # channels are nearest zero and flattest: the encoder's edges then make no step. A bed's start is the best
        # of its SEAM_TRIES quietest, flattest points (at least 0.5 s apart) by the seam measured on the decoded file
        # (Vorbis' first and last frames shift the edges a little; a low, muffled bed shows that most).
        w = st if kind == "loop" else st[:, :int(0.01 * SR)]
        score = np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)
        starts, gap = [], int(0.5 * SR)
        for k in np.argsort(score):
            if all(abs(int(k) - j) >= gap for j in starts):
                starts.append(int(k))
            if len(starts) == (SEAM_TRIES if kind == "loop" else 1):
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
            if len(starts) > 1:
                path = write_ogg(OUT / f"{name}.ogg", trial, max_peak_db=ceiling)
                seam = seam_db(decode(path)[:, :trial.shape[1]])
            else:
                seam = 0.0
            if best is None or seam < best[0]:
                best = (seam, trial)
        st = best[1]
    else:
        raw = mix(spec)
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
    if kind in ("loop", "rhythm"):
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
        if layer["at"] and kind != "rhythm":
            parts.append(f"placed at {num(layer['at'])} s" + (" in the loop (once a loop)" if kind == "loop" else ""))
    if kind == "loop" and not one_shot:
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
    if kind == "rhythm":
        shots, period, length = spec
        parts.append(f"repeated every {num(period * len(shots))} s (the {len(shots)} shots taking turns, one every "
                     f"{num(period)} s) round a seamless {num(length)} s loop, the channels summed to mono")
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
        looped = kind in ("loop", "rhythm")
        lv = band_rms_db(x) if looped else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if looped else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if looped else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, nothing bubbly or "
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
