#!/usr/bin/env python3
"""Concept round 89 (M11 part B, step AUD-B, 2026-11-05; QB-1): the `vrell-gate` ambience bed, a and b
(design/audio/sfx/README.md, *Ambience (per setting)* and *Act 7 sounds (draft)*). It plays under Levels 43 and 44 for
the whole level, under the music (Level 44 with the `event-plane` layer over it): open space heard through the ship's
hull (the belt's convention: the cockpit's low hum, no wind) with a slow stone resonance from the gate ring.

  vrell-gate-r89-a   seamless 32 s LOOP: a low hull room tone with a slow bowed-stone drone under it
  vrell-gate-r89-b   seamless 40 s LOOP: a thin airy space tone with a distant deep stone hum swelling every 8 s

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, loop, levels, the --docs rows), as
sfx_r84.py is. Every layer is a Freesound sound (CC0 or CC-BY) taken from its public HQ preview (cdn.freesound.org,
cached in ~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no download quota
used). The recordings are only cut, slowed, filtered, looped, swelled and levelled. The stone is a real singing stone
(a stone rubbed until it rings, thanvannispen's recordings), slowed; no bubbly or popping sources (the cave room tone
with its drips was left out).

The extra `swells` (layers, period s, depth dB) of a loop: those layers looped like the others, then swelled from
`depth` dB under up to full and back once every `period` s (a raised cosine, a whole number of times a loop).

Levelled like the Act 5-6 beds (belt-open, jovian-sky, callisto-fortress: -42 dB band RMS 200 Hz-5 kHz), at most
-33.5 LUFS (the loudest Act 6 beds, aurelia and io-refinery, are -33.4/-33.5 LUFS; a's deep hull tone would reach
-30.4 LUFS at the band level alone), so the two variants and the earlier beds are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r89.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r89.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, the swell's period and the loop seams.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from synth import SR, undb  # noqa: E402

CC0, BY4 = "CC0 1.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    30019: ('stone_singing_sample2.aif', 'thanvannispen', BY4, P + '30/30019_13258-hq.ogg'),
    30020: ('stone_singing_sample3.aif', 'thanvannispen', BY4, P + '30/30020_13258-hq.ogg'),
    203281: ('EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV', 'metrostock99', CC0, P + '203/203281_1256155-hq.ogg'),
    437285: ('Large Empty Church Hall – Silence, Ambiance, Air, Tone, Buzz', 'senorstudy', CC0,
             P + '437/437285_2262806-hq.ogg'),
    452099: ('airplane dual prop cabin in flight hum +PA landing message f', 'kyles', CC0,
             P + '452/452099_612689-hq.ogg'),
    752273: ('Low Roomtone', 'IENBA', CC0, P + '752/752273_9616576-hq.ogg'),
}

L, Lp = M.L, M.Loop
BEDS = ("the bed of Levels 43 and 44 for the whole level (Level 44 with the `event-plane` layer over it), on the "
        "effects bus at the beds' 0.70, under the music")
SHOULD = ("open space heard through the ship's hull (the belt's convention: the cockpit's low hum, no wind) with a "
          "slow stone resonance from the gate ring; a seamless loop of 30-60 s")

SOUNDS = {
    "vrell-gate": ("loop", -42.0, -1.0, -33.5, BEDS, SHOULD, {
        "a": (([Lp(752273, 6.0, gain=-3.0, hp=30.0, lp=800.0),
                Lp(452099, 20.0, gain=-6.0, speed=0.8, hp=40.0, lp=500.0),
                Lp(30020, 2.0, speed=0.5, hp=60.0, lp=1200.0)], 32.0, 1.0,
               {"events": [L(203281, 10.0, 6.0, gain=-14.0, at=20.0, fin=2.0, fout=3.0, hp=25.0, lp=200.0)]}),
              "a low room tone and a prop plane's cabin hum slowed to 0.8 and muffled (the hull), a singing stone "
              "slowed to half under them (a slow bowed-stone drone an octave down), once a loop a far deep rumble "
              "rising and fading: 32.0 s loop"),
        "b": (([Lp(437285, 4.0, gain=-4.0, hp=700.0, lp=6000.0),
                Lp(752273, 6.0, gain=-12.0, hp=30.0, lp=300.0)], 40.0, 1.0,
               {"swells": ([Lp(30019, 3.0, speed=0.35, hp=30.0, lp=500.0)], 8.0, 14.0)}),
              "an empty church hall's air, thinned to its hiss (an airy, open tone), over a faint low hull tone; a "
              "singing stone slowed to 0.35 and muffled (a deep, distant stone hum) swelling up and away every 8 s: "
              "40.0 s loop"),
    }),
}

_bed72 = M.bed
_layers72 = M.layers_of
_change72 = M.change_text


def bed(layers, length, xfade, extra=None):
    """sfx_r72's bed plus `swells`: (layers, period, depth) looped alike, then swelled every `period` s."""
    extra = dict(extra or {})
    swells = extra.pop("swells", None)
    st = _bed72(layers, length, xfade, extra)
    if swells:
        sw_layers, period, depth = swells
        sw = M.loop(sw_layers, length, xfade)
        cycles = round(length / period)
        t = np.arange(sw.shape[1]) / sw.shape[1] * cycles
        st = st + sw * undb(-depth * (0.5 + 0.5 * np.cos(2 * np.pi * t)))
    return st


def layers_of(kind, spec):
    out = _layers72(kind, spec)
    if kind == "loop" and len(spec) > 3 and "swells" in spec[3]:
        out = out + list(spec[3]["swells"][0])
    return out


def change_text(subject, kind, spec, layer):
    text = _change72(subject, kind, spec, layer)
    extra = spec[3] if kind == "loop" and len(spec) > 3 else {}
    if "swells" in extra and any(layer is s for s in extra["swells"][0]):
        _, period, depth = extra["swells"]
        text = text.replace(", peak-normalised",
                            f", swelling from {num(depth)} dB under up to full and back every {num(period)} s, "
                            "peak-normalised")
    return text


num = M.num

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r89.py", "r89", "round 89"
M.bed, M.layers_of, M.change_text = bed, layers_of, change_text

if __name__ == "__main__":
    M.main(sys.argv[1:])
