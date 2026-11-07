#!/usr/bin/env python3
"""Concept round 92 (M11 part E, step AUD-EF, 2026-11-06; QE-1): the `vrell-inner-sphere` ambience bed, a and b
(design/audio/sfx/README.md, *Ambience (per setting)*). It plays under Level 47 for the whole level (the stone shell
round the Choir's core), on the effects bus under the music: a warm, low resonance like a vast hollow space with a faint
far choir-like hum of air in pipes; never a melody, never a voice.

  vrell-inner-sphere-r92-a   seamless 16 s LOOP: a large hall's room tone slowed to 0.7 and muffled (lowered), with a
                             real organ pipe's held A2 slowed to half (A1, 55 Hz) and muffled under it: a far, warm
                             hum with the pipe's slow tremble, swelling a little every 8 s
  vrell-inner-sphere-r92-b   seamless 16 s LOOP: a large tank's resonance: an industrial room's air hiss played through
                             a 1000-litre steel tank's recorded impulse response (slowed to 0.6: a bigger tank) over a
                             faint low room tone, with an organ's wind (the air in its pipes, no note) thinned to a
                             faint hiss on top; no pitched hum

Built on sfx_r72.py's machinery with sfx_r89.py's `swells` (imported and pointed at this round: preview, loop, levels,
the --docs rows) and one extra here, `tank`: excitation loop layers played through an impulse response by circular
convolution (the loop is periodic, so the result is the steady state and the seam stays seamless). Every layer is a
Freesound sound (CC0 or CC-BY) taken from its public HQ preview (cdn.freesound.org, cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no download quota used). The
recordings are only cut, slowed, filtered, looped, swelled, convolved with a recorded impulse response and levelled;
no bubbly or popping sources.

Levelled to -33.5 LUFS, the loudness of round 89's chosen `vrell-gate` a, round 90's `vrell-hive` a and round 91's
`dead-fleet` a, so the Act 7 beds are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r92.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r92.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, spectral peaks (b: no pitched hum) and the loop
seams.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
import sfx_r89  # noqa: E402,F401  (installs the `swells` bed, layers and CREDITS text into sfx_r72)

CC0, BY4 = "CC0 1.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    273513: ('Church Organ, Off, A.wav', 'InspectorJ', BY4, P + '273/273513_5121236-hq.ogg'),
    373730: ('Organ - quiet - A2 (NT5_PedalQuiet_073_rr1.wav)', 'sgossner', CC0, P + '373/373730_2475994-hq.ogg'),
    451614: ('room tone large convention hall vent large almost empty.flac', 'kyles', CC0,
             P + '451/451614_612689-hq.ogg'),
    453544: ('room tone large industrial hiss and ventilation.flac', 'kyles', CC0, P + '453/453544_612689-hq.ogg'),
    560377: ('1000 Liter Wine Tank "Plate" "Spring" Impulse', 'manysounds', CC0, P + '560/560377_3665456-hq.ogg'),
    752273: ('Low Roomtone', 'IENBA', CC0, P + '752/752273_9616576-hq.ogg'),
}

L, Lp = M.L, M.Loop
BEDS = ("the bed of Level 47 for the whole level (the stone shell round the Choir's core), on the effects bus at the "
        "beds' 0.70, under the music")
SHOULD = ("a warm, low resonance like a vast hollow space (a cathedral's or a large tank's room tone, lowered) with a "
          "faint far choir-like hum of air in pipes; never a melody, never a voice; a seamless 16 s loop")

SOUNDS = {
    "vrell-inner-sphere": ("loop", -30.0, -1.0, -33.5, BEDS, SHOULD, {
        "a": (([Lp(451614, 148.0, speed=0.7, hp=30.0, lp=1500.0)], 16.0, 1.0,
               {"swells": ([Lp(373730, 1.2, gain=-4.0, speed=0.5, hp=30.0, lp=500.0)], 8.0, 4.0)}),
              "a large hall's room tone slowed to 0.7 and muffled (a vast room, lowered), a real organ pipe's held "
              "A2 slowed to half (a far A1 hum at 55 Hz with the pipe's slow tremble) under it, swelling a little "
              "every 8 s: 16.0 s loop"),
        "b": (([Lp(752273, 6.0, gain=-10.0, hp=30.0, lp=300.0),
                Lp(273513, 0.3, gain=-14.0, speed=0.8, hp=1500.0, lp=7000.0)], 16.0, 1.0,
               {"tank": ([Lp(453544, 2.0, hp=40.0, lp=3000.0)],
                         L(560377, 0.0, 6.0, speed=0.6, fin=0.0, fout=2.0, hp=30.0, lp=2000.0), 0.0)}),
              "a large tank's resonance: an industrial room's air hiss played through a 1000-litre steel tank's "
              "recorded impulse response slowed to 0.6 (a bigger tank: a deep, hollow wash), over a faint low room "
              "tone, an organ's wind (the air in its pipes before it sounds, no note) thinned to a faint high hiss on "
              "top; no pitched hum: 16.0 s loop"),
    }),
}

_bed89 = M.bed
_layers89 = M.layers_of
_change89 = M.change_text


def bed(layers, length, xfade, extra=None):
    """sfx_r89's bed plus `tank`: (excitation loop layers, impulse-response layer, gain dB): the excitation looped
    alike, then circularly convolved with the impulse response (per channel), peak-normalised, `gain` dB, added."""
    extra = dict(extra or {})
    tank = extra.pop("tank", None)
    st = _bed89(layers, length, xfade, extra)
    if tank:
        exc_layers, ir_layer, gain = tank
        exc = M.loop(exc_layers, length, xfade)
        n = exc.shape[1]
        ir = M.cut(ir_layer)[:, :n]
        wet = np.array([np.fft.irfft(np.fft.rfft(exc[c]) * np.fft.rfft(ir[c], n), n) for c in range(2)])
        st = st + wet / (np.max(np.abs(wet)) + 1e-12) * M.undb(-1.0 + gain)
    return st


def layers_of(kind, spec):
    out = _layers89(kind, spec)
    if kind == "loop" and len(spec) > 3 and "tank" in spec[3]:
        exc_layers, ir_layer, _ = spec[3]["tank"]
        out = out + list(exc_layers) + [ir_layer]
    return out


def change_text(subject, kind, spec, layer):
    text = _change89(subject, kind, spec, layer)
    extra = spec[3] if kind == "loop" and len(spec) > 3 else {}
    if "tank" in extra:
        exc_layers, ir_layer, _ = extra["tank"]
        if layer is ir_layer:
            text = text.replace(", peak-normalised",
                                ", used as the impulse response the hiss is played through (circular convolution over "
                                "the loop, so its seam stays seamless), the result peak-normalised")
        elif any(layer is e for e in exc_layers):
            text = text.replace(", peak-normalised",
                                ", played through the tank's impulse response (circular convolution), "
                                "peak-normalised")
    return text


num = M.num

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r92.py", "r92", "round 92"
M.bed, M.layers_of, M.change_text = bed, layers_of, change_text


def level_text(subject):
    return (f"levelled to {M.num(SOUNDS[subject][3])} LUFS (the Act 7 beds' level: round 89's `vrell-gate` a, round "
            "90's `vrell-hive` a, round 91's `dead-fleet` a)")


M.level_text = level_text

if __name__ == "__main__":
    M.main(sys.argv[1:])
