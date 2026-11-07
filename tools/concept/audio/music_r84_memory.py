#!/usr/bin/env python3
"""M11 part G, step MUS-G (2026-11-07): Level 49's five music memory copies (design/audio/music/README.md, *Act 7
music*, "Level 49, the memory"), offline renders of the built files the earlier acts play, through round 84's chosen
memory filter (M.6 a: music_r84.py's `memory("a")`, the numbers below unchanged):

  afterburner-memory-r84-a      track 4 "Afterburner"          (assets/music/afterburner.ogg)      Luna
  red-dust-run-memory-r84-a     track 8 "Red Dust Run"         (assets/music/red-dust-run.ogg)     Mars
  hollow-rock-memory-r84-a      track 12 "Hollow Rock"         (assets/music/hollow-rock.ogg)      Ceres
  choir-descends-memory-r84-a   track 18 "The Choir Descends"  (assets/music/choir-descends.ogg)   the Brood Carrier's
                                                                                                  and the Dust Colossus's echoes
  iron-sovereign-memory-r84-a   track 19 "Iron Sovereign"      (assets/music/iron-sovereign.ogg)   the Iron Sovereign's echo

The filter: the whole file slowed 6 % with its pitch (about a semitone down), a low-pass at 2.5 kHz (12 dB/octave),
a 3.5 s hall at 40 % wet, the stereo narrowed to 60 %, -4 dB against the original (the file levelled to the original's
own loudness minus 4 dB; the originals are mastered to about -14 LUFS).

Loop points: each original's LOOPSTART/LOOPLENGTH scaled by the slowing. The loop length becomes L' = round(L / 0.94)
samples and the speed is set to exactly L / L' (0.94 to 1e-7), the loop start A' = ceil(A / speed): the intro is played
once, then the loop is unrolled (intro + two passes), so every slowed sample past A' reads the loop at the same phase
one period later (the copy is exactly periodic there). The low-pass and the hall run over the unrolled signal and the
file's loop is the SECOND pass: the steady state, its hall tail coming from the loop's own end, so the loop seam is
seamless. The first pass's entry (intro -> loop) swaps the intro's hall tail for the loop end's: the step is ramped out
over the intro's last 20 ms and the entry measured. The tail past the loop end is the loop start again, faded out (the
originals' `add_tail`, its length scaled), for a player that ignores the loop points.

Usage: python3 tools/concept/audio/music_r84_memory.py [name ...]     writes into design/audio/music/concept/
(copied into assets/music/<track>-memory.ogg by :pipeline:copyPlaceholderMusic). Deterministic (the hall's seed,
bit-exact ffmpeg flags). Not listened to by Claude: checked by loudness, peaks, the energy above 3 kHz, the loop seam
and the intro's entry.
"""
import math
import os
import subprocess
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music_r02 import lp, seam_ratio  # noqa: E402
from synth import SR, db, decode, lufs, reverb, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
ASSETS = ROOT / "assets" / "music"

TRACKS = {  # copy -> (track number, the built file it remembers)
    "afterburner-memory-r84-a": (4, "afterburner"),
    "red-dust-run-memory-r84-a": (8, "red-dust-run"),
    "hollow-rock-memory-r84-a": (12, "hollow-rock"),
    "choir-descends-memory-r84-a": (18, "choir-descends"),
    "iron-sovereign-memory-r84-a": (19, "iron-sovereign"),
}
# round 84 M.6 a (music_r84.py `memory("a")`)
SPEED, CUT, REVERB_S, WET, WIDTH, GAIN_DB = 0.94, 2500.0, 3.5, 0.40, 0.6, -4.0
CEILING_DB = -2.0  # the originals' sample-peak ceiling
ENTRY_RAMP = int(0.02 * SR)


def loop_tags(path):
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "stream_tags=LOOPSTART,LOOPLENGTH", "-of",
                          "default=noprint_wrappers=1", str(path)], capture_output=True, text=True, check=True).stdout
    tags = dict(line.split("=", 1) for line in out.split())
    return int(tags["TAG:LOOPSTART"]), int(tags["TAG:LOOPLENGTH"])


def memory(name):
    track, src = TRACKS[name]
    path = ASSETS / f"{src}.ogg"
    x = decode(path)
    a, n = loop_tags(path)
    tail_n = x.shape[1] - (a + n)
    x = x - x.mean(axis=1, keepdims=True)
    # the intro once, then the loop twice and a little more (the interpolation's last sample)
    s = np.concatenate([x[:, :a + n], x[:, a:a + n], x[:, a:a + SR]], axis=1)
    n2 = int(round(n / SPEED))
    speed = n / n2
    a2 = math.ceil(a / speed)
    m = a2 + 2 * n2
    pos = np.arange(m) * speed
    i = np.floor(pos).astype(int)
    f = pos - i
    y = s[:, i] * (1 - f) + s[:, i + 1] * f
    y = lp(y, CUT)
    y = reverb(y, seconds=REVERB_S, mix=WET, seed=8510)[:, :m]
    mid, side = (y[0] + y[1]) / 2, (y[0] - y[1]) / 2
    y = np.array([mid + side * WIDTH, mid - side * WIDTH])
    intro, loop = y[:, :a2].copy(), y[:, a2 + n2:a2 + 2 * n2]
    lin = y[:, a2:a2 + n2]  # the first pass as rendered: the entry's reference
    step = loop[:, 0] - lin[:, 0]
    if a2 >= ENTRY_RAMP:
        intro[:, -ENTRY_RAMP:] += np.linspace(0, 1, ENTRY_RAMP + 1)[1:] * step[:, None]
    tail = int(round(tail_n / speed))
    w = 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, tail))
    audio = np.concatenate([intro, loop, loop[:, :tail] * w], axis=1)
    audio[:, :int(0.005 * SR)] *= np.linspace(0, 1, int(0.005 * SR))
    # levelled like the original, -4 dB (measured twice: whole files, as the originals were)
    target = lufs(x)[0] + GAIN_DB
    for _ in range(2):
        audio = audio * undb(target - lufs(audio)[0])
    # the entry: how far the first pass's hall tail (the intro's) differs from the loop's (the loop end's), 0.5 s
    k = int(0.5 * SR)
    entry_db = db(np.sqrt(np.mean((loop[:, :k] - lin[:, :k]) ** 2))) - db(np.sqrt(np.mean(lin[:, :k] ** 2)))
    out = write_ogg(OUT / f"{name}.ogg", audio, max_peak_db=CEILING_DB, tags={
        "LOOPSTART": a2, "LOOPLENGTH": n2,
        "COMMENT": f"tools/concept/audio/music_r84_memory.py {name}: track {track} ({src}.ogg) through round 84 M.6 a's"
                   f" memory filter, its loop points scaled by the slowing ({a}+{n} -> {a2}+{n2})"})
    return report(out, x, a, n, a2, n2, target, entry_db)


def above_3k(x):
    spec = np.abs(np.fft.rfft(x.mean(axis=0)))
    freqs = np.fft.rfftfreq(x.shape[1], 1 / SR)
    return db(np.sqrt(np.sum(spec[freqs > 3000] ** 2) / np.sum(spec ** 2)))


def report(path, orig, a, n, a2, n2, target, entry_db):
    dec = decode(path)
    loud, tp = lufs(dec)
    end = a2 + n2
    k = int(0.02 * SR)
    around = np.concatenate([np.diff(dec[:, end - k:end], axis=1), np.diff(dec[:, a2:a2 + k], axis=1)], axis=1)
    seam = db(np.max(np.abs(dec[:, a2] - dec[:, end - 1])) + 1e-12) - db(np.sqrt(np.mean(around ** 2)))
    return (f"{path.name}: {dec.shape[1] / SR:.2f} s; loop {a2}+{n2} ({a2 / SR:.3f} s + {n2 / SR:.3f} s; the original"
            f" {a}+{n}, {a / SR:.3f} s + {n / SR:.3f} s); {loud:.1f} LUFS (target {target:.1f}), peak"
            f" {db(np.abs(dec).max()):.1f} dBFS, true peak {tp:.1f} dBTP; seam {seam:+.1f} dB, ratio"
            f" {seam_ratio(dec, a2, n2):.2f}; entry hall swap {entry_db:.1f} dB under the music; above 3 kHz"
            f" {above_3k(dec[:, a2:end]):.1f} dB (the original's {above_3k(orig[:, a:a + n]):.1f})")


def main(argv):
    names = argv or list(TRACKS)
    OUT.mkdir(parents=True, exist_ok=True)
    with ProcessPoolExecutor(max_workers=min(len(names), 6, os.cpu_count() or 1)) as pool:
        for line in pool.map(memory, names):
            print(line, flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
