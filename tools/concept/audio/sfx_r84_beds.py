#!/usr/bin/env python3
"""M11 parts G and H, step AUD-GH (2026-11-07): Level 49's three bed memory copies and Level 50's `vrell-hive`
heartbeat layer (design/audio/sfx/README.md, *Ambience (per setting)*), rendered from files already in the repository
(no new recording, no download):

  luna-memory-r84-a        seamless loop (17.02 s): the built `luna` bed (assets/sfx/ambience-luna-r08-a.ogg)
  mars-dust-memory-r84-a   seamless loop (17.02 s): the built `mars-dust` bed (assets/sfx/mars-dust-r38-a.ogg)
  belt-open-memory-r84-a   seamless loop (17.02 s): the built `belt-open` bed (assets/sfx/belt-open-r60-a.ogg)
      each through round 84's chosen memory filter (M.6 a, the music README's *Act 7 music*: music_r84.py's
      `memory("a")`): slowed 6 % with its pitch (the loop length scaled with it: 16 s / 0.94), a low-pass at 2.5 kHz
      (12 dB/octave), a long hall (3.5 s, 40 % wet), the stereo narrowed to 60 %. Every step runs on the loop as a
      periodic signal (the slowing interpolates round the loop; the filter and the hall run over three periods and the
      last one is kept: the steady state), so the seam stays seamless.
  vrell-hive-heartbeat-r84-b   seamless 16 s loop: the Heart's heartbeat (round 84 S.25 b,
      design/audio/sfx/concept/heart-beat-r84-b.ogg) lower and quieter: slowed to 0.85 (about 3 semitones down), a
      low-pass at 400 Hz (12 dB/octave), one thud every 4 s (slower than the fight's 3 s), each thud's tail wrapping
      round the loop; a deep slow thud felt more than heard, the layer the game lays over `vrell-hive` chamber by chamber
      on Level 50's approach.

Levels: the memory copies at -33.5 LUFS, the Act 7 beds' level (rounds 89-92: `vrell-gate`, `vrell-hive`, `dead-fleet`,
`vrell-inner-sphere`); the heartbeat layer 3 dB under them, -36.5 LUFS (the game's layer gain rises over the approach).

Usage: python3 tools/concept/audio/sfx_r84_beds.py [name ...]       writes into design/audio/sfx/concept/
Deterministic (a seeded hall, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, spectrum and
the loop seams.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music_r02 import lp  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from synth import SR, db, decode, lufs, reverb, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
ASSETS = ROOT / "assets" / "sfx"

MEMORY = {  # name -> the built bed it remembers
    "luna-memory-r84-a": ASSETS / "ambience-luna-r08-a.ogg",
    "mars-dust-memory-r84-a": ASSETS / "mars-dust-r38-a.ogg",
    "belt-open-memory-r84-a": ASSETS / "belt-open-r60-a.ogg",
}
HEARTBEAT = "vrell-hive-heartbeat-r84-b"
BEDS_LUFS, LAYER_LUFS = -33.5, -36.5
# round 84 M.6 a (music_r84.py `memory("a")`)
SPEED, CUT, REVERB_S, WET, WIDTH = 0.94, 2500.0, 3.5, 0.40, 0.6


def periodic_speed(x, factor):
    """A loop x (stereo) played at `factor` speed, pitch with it, as a loop again: n / factor samples, interpolated
    round the loop (the last output sample reads towards the first input one)."""
    n = x.shape[1]
    m = int(round(n / factor))
    pos = np.arange(m) * (n / m)
    i = np.floor(pos).astype(int)
    f = pos - i
    return x[:, i % n] * (1 - f) + x[:, (i + 1) % n] * f


def steady(x, fn, periods=3):
    """fn (a filter, a hall) applied to the loop x tiled `periods` times; the last period: the steady state."""
    n = x.shape[1]
    y = fn(np.tile(x, (1, periods)))
    return y[:, (periods - 1) * n:periods * n]


def level_to(x, target):
    """Gain to the target loudness (measured twice: the encoder's gain step is not linear at these levels)."""
    for _ in range(2):
        x = x * undb(target - lufs(x)[0])
    return x


def memory(src):
    x = decode(src)
    x = x - x.mean(axis=1, keepdims=True)
    x = periodic_speed(x, SPEED)
    x = steady(x, lambda s: lp(s, CUT))
    x = steady(x, lambda s: reverb(s, seconds=REVERB_S, mix=WET, seed=8510)[:, :s.shape[1]])
    mid, side = (x[0] + x[1]) / 2, (x[0] - x[1]) / 2
    x = np.array([mid + side * WIDTH, mid - side * WIDTH])
    return level_to(x, BEDS_LUFS)


def heartbeat():
    beat = decode(ROOT / "design" / "audio" / "sfx" / "concept" / "heart-beat-r84-b.ogg")
    beat = periodic_speed(np.pad(beat, ((0, 0), (0, SR))), 0.85)  # the padding: the slowed tail stays whole
    beat = lp(lp(beat, 400.0), 400.0)
    n = 16 * SR
    loop = np.zeros((2, n))
    for k in range(4):
        idx = (k * 4 * SR + np.arange(beat.shape[1])) % n
        np.add.at(loop[0], idx, beat[0])
        np.add.at(loop[1], idx, beat[1])
    loop = loop - loop.mean(axis=1, keepdims=True)
    return level_to(loop, LAYER_LUFS)


def report(path):
    x = decode(path)
    loud, tp = lufs(x)
    spec = np.abs(np.fft.rfft(x.mean(axis=0)))
    freqs = np.fft.rfftfreq(x.shape[1], 1 / SR)
    above = db(np.sqrt(np.sum(spec[freqs > 3000] ** 2) / np.sum(spec ** 2)))
    print(f"{path.name}: {x.shape[1] / SR:.3f} s ({x.shape[1]} samples)  {loud:.1f} LUFS  true peak {tp:.1f} dBTP"
          f"  seam {seam_db(x):+.1f} dB  energy above 3 kHz {above:.1f} dB", flush=True)


def main(argv):
    names = argv or [*MEMORY, HEARTBEAT]
    OUT.mkdir(parents=True, exist_ok=True)
    for name in names:
        x = heartbeat() if name == HEARTBEAT else memory(MEMORY[name])
        path = write_ogg(OUT / f"{name}.ogg", x, max_peak_db=-3.0)
        report(path)


if __name__ == "__main__":
    main(sys.argv[1:])
