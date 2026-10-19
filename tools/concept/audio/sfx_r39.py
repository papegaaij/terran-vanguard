#!/usr/bin/env python3
"""Concept round 39 (M7 part C, Level 16 "Valles Canyon Run", step AUD-C): one proposal each (option a) for the
Tendril barrier's hum, burn loop and break, the Threadcrawler's walk and the canyon ambience `mars-canyon`.
All fully synthesized (no third-party layers, so nothing new for CREDITS.md); no bubbly, popping or squelch
layers (user rule): every transient is a band-noise grain with a fast decay, every tone is steady or
glides with harmonics, never a falling sine "bloop". The canyon wall scrape is the chosen round-36 b
(canyon-scrape-r36-b.ogg, Halleck's CC-BY scrape, already credited) and is only copied to assets/sfx.
Pending the user's review in round 39.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  tendril-hum-r39-a.ogg    the tendril barrier live (a loop; the nearest one plays): a taut electric-organic
                           drone, 120 Hz and its harmonics up to 5 kHz (odd partials stronger, a sawtooth-like
                           tilt, random fixed phases) over a faint steady fizz (2.5-6 kHz). No wobble: every
                           partial is steady (no vibrato, no beating pair, no amplitude modulation), so it
                           cannot read as bubbling; a seamless 6.0 s LOOP, -38 dB band RMS (the beam
                           impact loops' level). The game raises its gain over the 1.0 s dormant tell
  tendril-burn-r39-a.ogg   a beam on a tendril (a loop while it burns): a searing crackle, 3-9 ms noise
                           grains (2-8 kHz, fast decay, ~220 a second, two channels apart) over a dry sizzle
                           bed (3-9 kHz); a seamless 2.0 s LOOP, -34 dB band RMS (over the beam's own loop)
  tendril-break-r39-a.ogg  the tendril or an anchor dies: a whip-crack (a 2 ms broadband snap and a 25 ms
                           band-noise tail) and the line going slack: a rough harmonic whine falling
                           2.4 -> 0.55 kHz over 0.7 s (sawtooth partials, a noise component, fading out
                           before it reaches a low tone), 0.9 s, -17 dB loudest 100 ms (the enemy death level)
  crawler-walk-r39-a.ogg   the Threadcrawler's walk (a loop while on screen): a dry many-legged skitter on
                           rock: twelve legs in two staggered rows at 8.4 steps a second, each step a 2-6 ms
                           noise tick (1.8-7 kHz, fast decay), under a faint rock rustle (0.9-2.8 kHz);
                           a seamless 3.0 s LOOP (25 whole steps), -36 dB band RMS
  mars-canyon-r39-a.ogg    ambience `mars-canyon` (Level 16): the dust wind (pinkish body 120 Hz-3 kHz, a grit
                           hiss) with a canyon howl: a resonant moan wandering 260-420 Hz with a second voice
                           an octave up, breathing with the gusts, steadier and lower than `mars-dust`;
                           a seamless 16.0 s LOOP, -36 dB band RMS like every ambience loop

Levels as sfx_r38.py: one-shots on the loudest 100 ms of the 200 Hz-5 kHz band, loops by the whole loop's
band RMS (one gain, no limiter).

Usage: python3 tools/concept/audio/sfx_r39.py [subject-or-file ...]   e.g. tendril-hum tendril-break-r39-a
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
from synth import SR, decode, noise, pan, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r39.py"

HUM, BURN, WALK, CANYON = 6.0, 2.0, 3.0, 16.0


# =========================================================================== loops (periodic in L)

def tendril_hum(L, seed=3901):
    """120 Hz and its harmonics (whole cycles in L), odd ones stronger, fixed phases per channel; steady fizz."""
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    for ch in range(2):
        for h in range(1, int(5000 / 120) + 1):
            amp = (1.0 / h ** 0.9) * (1.0 if h % 2 else 0.55)
            if h == 3:
                amp *= 1.3          # the taut, reedy third
            out[ch] += amp * np.sin(2 * np.pi * 120.0 * h * t + rng.uniform(0, 2 * np.pi))
        fizz = circular_noise(n, rng, 2500.0, 6000.0)
        out[ch] = unit(out[ch]) + 0.12 * fizz
    return out


def grain_loop(L, rate, lo, hi, dur, seed, decay):
    """Noise grains placed circularly in a loop of L s: `rate` a second, band lo-hi Hz, `dur` (min, max) s."""
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for _ in range(int(rate * L)):
        a = int(rng.uniform(0, L) * SR)
        m = int(rng.uniform(*dur) * SR)
        x = svf(svf(noise(m, rng), np.full(m, lo), mode="hp"), np.full(m, hi))
        x = unit(x) * np.exp(-decay * np.arange(m) / m) * rng.uniform(0.15, 1.0) ** 1.5
        idx = (a + np.arange(m)) % n
        out[:, idx] += pan(x, rng.uniform(-0.7, 0.7))
    return out


def tendril_burn(L, seed=3902):
    n = int(round(L * SR))
    grains = grain_loop(L, 220.0, 2000.0, 8000.0, (0.003, 0.009), seed, 5.0)
    rng = np.random.default_rng(seed + 1)
    bed = np.array([circular_noise(n, rng, 3000.0, 9000.0) for _ in range(2)])
    g = np.array([gusts(L, n, [(2, 0.2, 0.3), (7, 0.1, 1.2)], ch * 0.8) for ch in range(2)])
    return unit(grains) * 1.0 + 0.35 * bed * g


def crawler_walk(L, seed=3903):
    """Twelve legs in two rows; every leg a tick once per step cycle; rows half a cycle apart, with jitter."""
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    steps = 25
    period = L / steps
    out = np.zeros((2, n))
    for leg in range(12):
        row = leg % 2
        phase = (leg // 2) / 6.0 * period + row * period / 2
        side = -0.6 + 1.2 * (leg // 2) / 5.0
        for s in range(steps):
            at = phase + s * period + rng.normal(0, 0.006)
            if rng.random() < 0.06:     # an occasional skipped or doubled tick keeps it from marching
                continue
            a = int((at % L) * SR)
            m = int(rng.uniform(0.002, 0.006) * SR)
            x = svf(svf(noise(m, rng), np.full(m, rng.uniform(1600, 2400)), mode="hp"),
                    np.full(m, rng.uniform(4500, 7500)))
            x = unit(x) * np.exp(-6.0 * np.arange(m) / m) * rng.uniform(0.35, 1.0)
            idx = (a + np.arange(m)) % n
            out[:, idx] += pan(x, side + rng.uniform(-0.15, 0.15))
    bed = np.array([circular_noise(n, rng, 900.0, 2800.0) for _ in range(2)])
    g = gusts(L, n, [(steps, 0.25, 0.0)])
    return unit(out) + 0.12 * bed * g


def mars_canyon(L, seed=3905):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for ch in range(2):
        g = gusts(L, n, [(1, 0.2, 0.9), (2, 0.14, 2.1), (5, 0.06, 0.4)], ch * 0.7)
        body = circular_noise(n, rng, 120.0, 3000.0, 380.0, 600.0)
        grit = circular_noise(n, rng, 3000.0, 7000.0)
        moan = wander(n, rng, L, 100.0, 1800.0, 340.0, 9.0, [(1, 60.0, 0.4), (3, 25.0, 1.9)], ch * 0.8)
        moan2 = wander(n, rng, L, 300.0, 2400.0, 680.0, 7.0, [(2, 90.0, 1.1), (4, 40.0, 0.2)], ch * 0.6)
        breath = gusts(L, n, [(1, 0.5, 2.6 + ch * 0.4), (3, 0.2, 0.7)])
        out[ch] = (0.75 * body * g + 0.15 * grit * g ** 2.5
                   + 0.9 * moan * np.clip(breath, 0.1, None) * g + 0.45 * moan2 * np.clip(breath, 0.1, None) ** 1.5)
    return out


# =========================================================================== one-shot

def tendril_break():
    rng = np.random.default_rng(3904)
    n = int(0.9 * SR)
    out = np.zeros((2, n))
    # the whip-crack: a 2 ms broadband snap and a short band-noise tail
    m = int(0.002 * SR)
    snap = unit(svf(noise(m, rng), np.full(m, 900.0), mode="hp")) * np.hanning(m * 2)[m:] * 3.0
    out[:, :m] += pan(snap, 0.0)
    m = int(0.03 * SR)
    tail = unit(svf(svf(noise(m, rng), np.full(m, 1800.0), mode="hp"), np.full(m, 9000.0)))
    tail = tail * np.exp(-6.0 * np.arange(m) / m)
    out[:, :m] += pan(tail * 1.1, 0.0)
    # the slack line's whine: sawtooth partials gliding 2400 -> 550 Hz, plus a band-noise component riding it
    k = int(0.7 * SR)
    t = np.arange(k) / k
    f = 2400.0 * (550.0 / 2400.0) ** (t ** 0.8)
    phase = np.cumsum(f) / SR
    env = np.minimum(1.0, t / 0.03) * (1 - t) ** 1.5
    for ch in range(2):
        whine = sum(np.sin(2 * np.pi * h * phase + rng.uniform(0, 6.28)) / h ** 1.1
                    for h in range(1, 7) if h * 2400.0 < 12000.0)
        rough = svf(noise(k, rng), f * 1.6, q=4.0, mode="bp")
        whine = unit(whine) + 0.5 * unit(rough)
        whine = svf(svf(whine, np.full(k, 300.0), mode="hp"), np.full(k, 7000.0))
        out[ch, int(0.012 * SR):int(0.012 * SR) + k] += 0.5 * whine * env
    return level(edge(out, 0.0005, 0.06), -17.0, -4.0)


# =========================================================================== treatment

SOUNDS = {
    "tendril-hum": ("loop", lambda: tendril_hum(HUM), -38.0, "a steady harmonic drone (120 Hz and its partials)"),
    "tendril-burn": ("loop", lambda: tendril_burn(BURN), -34.0, "noise-grain crackle and a sizzle bed"),
    "crawler-walk": ("loop", lambda: crawler_walk(WALK), -36.0, "noise-tick legs and a rock rustle"),
    "mars-canyon": ("loop", lambda: mars_canyon(CANYON), -36.0, "dust wind, resonant canyon howl"),
    "tendril-break": ("shot", tendril_break, None, "whip-crack and a falling harmonic whine"),
}


def build(subject):
    kind, make, band, what = SOUNDS[subject]
    st = make()
    name = f"{subject}-r39-a"
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
        if argv and subject not in argv and f"{subject}-r39-a" not in argv:
            continue
        build(subject)


if __name__ == "__main__":
    main(sys.argv[1:])
