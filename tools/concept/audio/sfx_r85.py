#!/usr/bin/env python3
"""Concept round 85 (M11 "Act 7 - Beyond the Gate" design phase, step RES, 2026-11-05): the Choir Resonator's sounds,
a and b each: its fire (one wave front, 2.5-3.5 a second, so short and soft-edged: the `resonator` family) and the
ring where a wave passes through a hull (throttled, quiet, stepped down in pitch per hull by the game).

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, cut, mix, levels, the --docs rows), as
sfx_r84.py. Every layer is a Freesound sound (CC0 or CC-BY) taken from its public HQ preview (cdn.freesound.org, cached
in ~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no download quota used). The
recordings are only cut, slowed, glided, layered, faded, filtered and levelled. Struck and bowed glass and singing
bowls, never bubbly or popping; nothing wet.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each is).
Levels like round 84's player-side sounds (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS
cap): the fire at the player shots' level (the built shots' loudest 100 ms sit at -21 to -27 dB), the ring 6 dB under
it; the two variants of a subject share the level. The fire is at most 0.34 s, inside the 0.33-0.4 s between waves.

Usage: python3 tools/concept/audio/sfx_r85.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r85.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, onsets and length against the fire rate.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from sfx_r43g import L  # noqa: E402

CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    271370: ('singing bowl strike sound', 'inoshirodesign', CC0, P + '271/271370_5159066-hq.ogg'),
    415138: ('singing bowl - single strike 3', 's-light', CC0, P + '415/415138_2154914-hq.ogg'),
    719636: ('Fast whoosh, bamboo swoosh through air, version 1', 'zapsplat.com', CC0,
             P + '719/719636_15601358-hq.ogg'),
    711891: ('Wine Glass Resonances by Violin Bow - 03', 'robin.kaf', CC0, P + '711/711891_14988552-hq.ogg'),
    740418: ('Tuning fork 432Hz smack 4', 'FOSSarts', CC0, P + '740/740418_14631530-hq.ogg'),
    549899: ('small wine glass.wav', 'Tairblenn', CC0, P + '549/549899_11663790-hq.ogg'),
}

FIRE_USE = ("the Choir Resonator's fire (front gun, on sale from Level 44): one per wave front, 2.5 a second at L1, 3 at "
            "L5, 3.5 in overdrive, so the most-heard sound of a Resonator fit")
FIRE_SHOULD = ("a short sung resonance with a soft front: struck or bowed glass or a singing bowl, the wave's push under "
               "it; at most 0.34 s and soft-edged so three a second never tire; never a laser zap, never bubbly or "
               "popping")
RING_USE = ("the Choir Resonator's wave passing through a hull (each hull it meets, throttled to one per 0.12 s; the "
            "game plays each next hull of a wave 6 % lower, as the wave steps down)")
RING_SHOULD = "a quiet clear glass ring: a tuning fork or a glass lightly struck, short, under the fire and the hits"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}), as sfx_r72.py's SOUNDS
SOUNDS = {
    "resonator-fire": ("shot", -24.0, -8.0, -24.0, FIRE_USE, FIRE_SHOULD, {
        "a": ([L(271370, 0.0, 0.34, speed=0.8, fin=0.004, fout=0.2, hp=180.0),
               L(415138, 0.0, 0.34, gain=-7.0, speed=1.25, fin=0.006, fout=0.2, hp=150.0),
               L(719636, 0.03, 0.3, gain=-8.0, fin=0.01, fout=0.15, hp=300.0)],
              "\"the bell\": a singing bowl struck, slowed to 0.8, over a deeper bowl's body and a bamboo swish's "
              "air: a round, ringing 'hmmng' that falls away quickly, 0.34 s"),
        "b": ([L(711891, 1.6, 0.34, speed=(1.0, 0.93), fin=0.035, fout=0.2, hp=250.0, lp=6000.0),
               L(740418, 0.1, 0.28, gain=-9.0, fin=0.003, fout=0.16, hp=400.0, lp=5000.0)],
              "\"the chord\": a wine glass bowed with a violin bow, gliding a little down (1.0 → 0.93), its front "
              "marked by a tuning fork's light tap: a sung, glassy 'aaah' with a soft start, airier and higher than "
              "a, 0.34 s"),
    }),
    "resonator-ring": ("shot", -30.0, -12.0, -30.0, RING_USE, RING_SHOULD, {
        "a": ([L(740418, 0.1, 0.22, fin=0.003, fout=0.14, hp=400.0, lp=4000.0)],
              "a tuning fork struck: a short pure 'ting' near 960 Hz, 0.22 s"),
        "b": ([L(549899, 0.0, 0.2, speed=0.8, fin=0.006, fout=0.12, hp=500.0, lp=6000.0)],
              "a small wine glass tapped, slowed to 0.8: a brighter, glassier 'tink' than a, 0.2 s"),
    }),
}

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r85.py", "r85", "round 85"

if __name__ == "__main__":
    M.main(sys.argv[1:])
