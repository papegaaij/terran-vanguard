#!/usr/bin/env python3
"""Concept round 41 (M7 part E, Level 18 "Dust Storm", step AUD-E): one proposal each (option a) for the Dust
Devil's spin loop, its opening and its grit spiral and the Ghost Drone's drop, eye glint, burst and death. All fully
synthesized (nothing new for CREDITS.md). No bubbly, popping or squelch layers (user rule): every transient is a
band-noise grain or a steady inharmonic partial set with a fast attack and an exponential decay, every tone is
steady or glides slowly with harmonics. The devil is grown (wind, grit, a low hollow tone), the drone is built
(clean turbine, electronic tick, dry shots, metal and sparks). Pending the user's review in round 41.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  devil-spin-r41-a         the Dust Devil's spin (a loop, the nearest one plays): a whirling wind roar (band noise
                           300-2200 Hz whose resonance turns 3 times a loop, the channels half a turn apart so it
                           circles) over a low 50-130 Hz rumble and a grit hiss of 3-8 kHz; seamless 3.0 s LOOP,
                           -32 dB band RMS
  devil-open-r41-a         the vanes parting: a gust (band noise swelling 400 -> 1800 Hz over 0.25 s), a few vane
                           scrapes (1-3 kHz) and a low hollow tone (a steady 110 Hz and 165 Hz with a 220 Hz
                           overtone, slow attack, 0.5 s decay); 0.7 s, -20 dB
  devil-spiral-r41-a       the grit spiral (the devil's small shot): a hiss of 2-7 kHz grit that turns (a
                           resonance wandering 3.5 -> 1.8 kHz) with a few sand ticks; 0.45 s, -22 dB
  ghost-drone-drop-r41-a   the drone dropping into the lane: a clean turbine whine descending (a harmonic set,
                           f0 1100 -> 520 Hz, slow glide, smooth attack) with a faint air hiss; 0.9 s, -24 dB
  ghost-drone-glint-r41-a  the 0.3 s eye glint: a short electronic lock tick (two steady inharmonic pings at 2.6
                           and 3.9 kHz, 2 ms attack, 60 ms decay, a 10 ms gap); 0.15 s, -24 dB
  ghost-drone-burst-r41-a  three dry precise shots (human guns): a 2 ms broadband snap, a 1.5-4 kHz crack and a
                           short 150-400 Hz body each, 65 ms apart, no tail; 0.35 s, -17 dB
  ghost-drone-death-r41-a  the mechanical break-up on the `small` rung: a dry bang (a 100-300 Hz thud and a
                           snap), metal pieces (inharmonic ringing 1.4-6 kHz, fast decays) flung out and a spatter
                           of sparks (2-9 kHz grains), no fireball rumble; 0.8 s, -17 dB

Levels as sfx_r40.py: one-shots on the loudest 100 ms of the 200 Hz-5 kHz band, loops by the whole loop's
band RMS (one gain, no limiter).

Usage: python3 tools/concept/audio/sfx_r41.py [subject-or-file ...]   e.g. devil-spin ghost-drone-death-r41-a
Deterministic (fixed seeds, bit-exact ffmpeg flags).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import edge, seam_db, unit  # noqa: E402
from sfx_r36b import circular_noise  # noqa: E402
from sfx_r38 import gusts, wander  # noqa: E402
from sfx_r40 import bp, grain, ping, put  # noqa: E402
from synth import SR, decode, noise, pan, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r41.py"

SPIN = 3.0


def devil_spin(L, seed=4101):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    for ch in range(2):
        # the roar circles: the same turn, the channels half a turn apart
        turn = [(3, 0.45, ch * np.pi)]
        roar = wander(n, rng, L, 300.0, 2600.0, 1100.0, 1.4, [(3, 500.0, ch * np.pi), (1, 250.0, 0.7)], 0.0)
        g = gusts(L, n, turn)
        rumble = circular_noise(n, rng, 50.0, 130.0, 80.0, 40.0)
        grit = circular_noise(n, rng, 3000.0, 8000.0)
        gg = gusts(L, n, [(3, 0.5, ch * np.pi + 1.0), (7, 0.2, 0.3)])
        out[ch] = 1.0 * roar * g + 0.45 * rumble * gusts(L, n, [(1, 0.2, 0.5)]) + 0.22 * grit * np.clip(gg, 0.05, None) ** 2
    return out


def devil_open():
    rng = np.random.default_rng(4102)
    n = int(0.7 * SR)
    out = np.zeros((2, n))
    k = int(0.5 * SR)
    u = np.arange(k) / k
    centre = 400.0 + 1400.0 * np.sqrt(u)
    gust = unit(svf(noise(k, rng), centre, q=1.6, mode="bp")) * np.minimum(1.0, u / 0.25) * np.exp(-3.0 * u)
    put(out, gust, 0.0, 0.0, 1.0)
    for i in range(5):    # the vanes parting
        at = 0.02 + i * 0.045
        side = -0.7 + 0.35 * i
        m = int(0.12 * SR)
        x = unit(bp(noise(m, rng), 1000.0 + 150 * i, 3000.0)) * np.minimum(1.0, np.arange(m) / (0.01 * SR))
        put(out, x * np.exp(-5.0 * np.arange(m) / m), at, side, 0.35)
    # the low hollow tone, slow attack
    m = int(0.6 * SR)
    t = np.arange(m) / SR
    tone = sum(a * np.sin(2 * np.pi * f * t + rng.uniform(0, 6.28)) for f, a in [(110, 1.0), (165, 0.55), (220, 0.3)])
    put(out, unit(tone) * np.minimum(1.0, t / 0.08) * np.exp(-t / 0.22), 0.05, 0.0, 0.8)
    return level(edge(out, 0.002, 0.08), -20.0, -4.0)


def devil_spiral():
    rng = np.random.default_rng(4103)
    n = int(0.45 * SR)
    out = np.zeros((2, n))
    u = np.arange(n) / n
    centre = 3500.0 - 1700.0 * u
    hiss = unit(svf(noise(n, rng), centre, q=2.5, mode="bp")) * np.minimum(1.0, u / 0.08) * (1 - u) ** 1.3
    pos = -0.6 + 1.2 * u
    out[0] += hiss * (1 - pos) / 2
    out[1] += hiss * (1 + pos) / 2
    for _ in range(14):
        put(out, grain(rng, rng.uniform(0.002, 0.006), 2500.0, 8000.0, 5.0), rng.uniform(0.0, 0.35),
            rng.uniform(-0.7, 0.7), rng.uniform(0.1, 0.4))
    return level(edge(out, 0.002, 0.06), -22.0, -4.0)


def drone_drop():
    rng = np.random.default_rng(4104)
    n = int(0.9 * SR)
    t = np.arange(n) / SR
    u = t / 0.9
    f0 = 1100.0 - 580.0 * u ** 0.8
    ph = 2 * np.pi * np.cumsum(f0) / SR
    whine = sum(a * np.sin(h * ph + rng.uniform(0, 6.28)) for h, a in [(1, 1.0), (2, 0.4), (3, 0.22), (5, 0.1)])
    env = np.minimum(1.0, t / 0.12) * np.exp(-1.6 * u)
    air = unit(bp(noise(n, rng), 3000.0, 7000.0)) * 0.12 * env
    mono = unit(whine) * env + air
    out = np.array([mono * (1.0 - 0.15), mono * (1.0 + 0.15)]) / 1.0
    return level(edge(out, 0.004, 0.05), -24.0, -4.0)


def drone_glint():
    n = int(0.15 * SR)
    out = np.zeros((2, n))
    for f, at, g in [(2600.0, 0.0, 1.0), (3900.0, 0.012, 0.7)]:
        m = int(0.1 * SR)
        put(out, ping(f, m, 0.06 / 2.5, 0.002), at, 0.0, g)
    return level(edge(out, 0.0008, 0.02), -24.0, -4.0)


def drone_burst():
    rng = np.random.default_rng(4105)
    n = int(0.35 * SR)
    out = np.zeros((2, n))
    for i in range(3):
        at = i * 0.065
        pos = (-0.15, 0.0, 0.15)[i]
        put(out, grain(rng, 0.002, 800.0, 12000.0, 3.0), at, pos, 0.15)
        put(out, grain(rng, 0.03, 1500.0, 4000.0, 6.0), at, pos, 1.0)
        put(out, grain(rng, 0.04, 150.0, 400.0, 6.0), at, pos, 1.6)
    return level(edge(out, 0.0004, 0.03), -17.0, -4.0)


def drone_death():
    rng = np.random.default_rng(4106)
    n = int(0.8 * SR)
    out = np.zeros((2, n))
    put(out, grain(rng, 0.12, 100.0, 300.0, 6.0), 0.0, 0.0, 2.0)
    put(out, grain(rng, 0.004, 700.0, 9000.0, 3.0), 0.0, 0.0, 0.25)
    put(out, grain(rng, 0.05, 1200.0, 5000.0, 6.0), 0.005, 0.0, 1.0)
    # metal pieces flung out: inharmonic ringing, fast decays
    for _ in range(9):
        at = rng.uniform(0.01, 0.4)
        f = rng.uniform(1400.0, 3200.0)
        m = int(rng.uniform(0.08, 0.2) * SR)
        x = sum(a * ping(f * r, m, rng.uniform(0.03, 0.08), 0.002) for r, a in [(1.0, 1.0), (1.59, 0.6), (2.33, 0.35), (2.0, 0.2)])
        put(out, x, at, rng.uniform(-0.8, 0.8), rng.uniform(0.1, 0.35))
    # sparks
    for _ in range(70):
        at = 0.02 + rng.uniform(0, 1.0) ** 1.8 * 0.7
        put(out, grain(rng, rng.uniform(0.002, 0.005), 2500.0, 9000.0, 5.0), at, rng.uniform(-0.9, 0.9),
            rng.uniform(0.1, 0.45) * (1.0 - at))
    return level(edge(out, 0.0005, 0.12), -17.0, -4.0)


SOUNDS = {
    "devil-spin": ("loop", lambda: devil_spin(SPIN), -32.0, "a whirling wind roar circling over a low rumble and grit"),
    "devil-open": ("shot", devil_open, None, "a gust, vane scrapes and a low hollow tone"),
    "devil-spiral": ("shot", devil_spiral, None, "a turning grit hiss with sand ticks"),
    "ghost-drone-drop": ("shot", drone_drop, None, "a clean turbine whine descending"),
    "ghost-drone-glint": ("shot", drone_glint, None, "a short electronic lock tick"),
    "ghost-drone-burst": ("shot", drone_burst, None, "three dry precise shots"),
    "ghost-drone-death": ("shot", drone_death, None, "a dry bang, flung metal and sparks"),
}


def build(subject):
    kind, make, band, what = SOUNDS[subject]
    st = make()
    name = f"{subject}-r41-a"
    if kind == "loop":
        st = st - st.mean(axis=1, keepdims=True)
        st = st * undb(band - band_rms_db(st))
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=-1.0 if kind == "loop" else -4.0,
                    tags={"COMMENT": f"{SCRIPT} {name}: {what}, synthesized"})
    report(out, f"{what}, synthesized")
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")


def main(argv):
    for subject in SOUNDS:
        if argv and subject not in argv and f"{subject}-r41-a" not in argv:
            continue
        build(subject)


if __name__ == "__main__":
    main(sys.argv[1:])
