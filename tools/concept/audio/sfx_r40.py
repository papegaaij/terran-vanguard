#!/usr/bin/env python3
"""Concept round 40 (M7 part D, Level 17 "Dome Siege", step AUD-D): one proposal each (option a) for the dome hit
and break, the Choir Herald's song, aura loop, ring absorb and ring collapse, the Shellback's walk, lob, curl,
roll and uncurl and the ambience `mars-colony`. All fully synthesized (nothing new for CREDITS.md). No bubbly,
popping or squelch layers (user rule): every transient is a band-noise grain with a fast decay or a steady
inharmonic partial set with a fast attack and an exponential decay, every tone is steady or glides slowly with
harmonics, never a falling sine "bloop"; the "absorb" is a glassy ping and the "collapse" a falling chord of
sustained voices, neither a liquid or air-bubble sound. Pending the user's review in round 40.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  dome-hit-r40-a.ogg         a dome takes a hit: a heavy glass knock (a 70-190 Hz body thud and a 1.2-3 kHz
                             tick), a creak running round the dome (band noise, resonance wandering 700 ->
                             1400 Hz and panned across the field) and a fine crackle of stress lines (60
                             grains of 2-9 kHz); no shatter; 1.2 s, -18 dB loudest 100 ms
  dome-break-r40-a.ogg       a dome breaks: a crack, panes going in a cascade (a few hundred 3-10 ms glass
                             grains, thinning out), air rushing out as a falling hiss and a steel frame groan
                             (a rough sawtooth set sinking 150 -> 85 Hz); 3.6 s, -16 dB
  herald-song-r40-a.ogg      the Herald's sung phrase (no words): three voices a fifth apart in the Choir's
                             "ah" (formants 800/1150/2900 Hz), F3 -> A3 -> E3 over 1.6 s, a slow vibrato;
                             -20 dB
  herald-aura-r40-a.ogg      the aura (a loop while any ring is up): a soft high shimmer, steady partials
                             at 1.9-4.6 kHz each with its own slow amplitude swell, over a faint 5-9 kHz
                             air; a seamless 4.0 s LOOP, -40 dB band RMS
  aura-absorb-r40-a.ogg      a ring absorbs a hit: a glassy ping (inharmonic partials 2.1, 3.3, 4.7 kHz,
                             3-5 ms attack, 0.25 s decay); 0.4 s, -22 dB
  aura-collapse-r40-a.ogg    the rings collapse at the Herald's death: a falling chord, five sustained
                             voices sliding down together (A4 minor, a fourth lower) over 1.6 s, fading out
                             with a sheet of ring-glass shimmer; 2.0 s, -18 dB
  shellback-walk-r40-a.ogg   the Shellback's walk (a loop): heavy padded footfalls (a 60-160 Hz thud with a
                             soft 300-900 Hz pad) every 0.8 s, left and right, with shell plates clacking
                             (1-3.5 kHz ticks) after each; a seamless 3.2 s LOOP (4 steps), -34 dB band RMS
  shellback-lob-r40-a.ogg    the Shellback's mortar lob, lower than the Polyp Mortar's: a dull cough thump
                             (80-260 Hz) and a short rising-then-falling pressure hiss (300-1800 Hz); 0.6 s,
                             -17 dB
  shellback-curl-r40-a.ogg   plates grinding shut: eleven plate scrapes (rough band noise sweeps 600 ->
                             2500 Hz, staggered) ending in a heavy clack; 0.8 s, -20 dB
  shellback-roll-r40-a.ogg   the roll (a loop): a rumbling grind that crushes (a 40-140 Hz rumble turning
                             at 2.5 turns a loop and a gritty rock-crush noise on the same turn); seamless
                             2.5 s LOOP, -30 dB band RMS
  shellback-uncurl-r40-a.ogg plates opening: the curl's scrapes in reverse order with a looser clack and a
                             short exhale of grit; 0.7 s, -22 dB
  mars-colony-r40-a.ogg      ambience `mars-colony` (Level 17): the plains wind (lower and thinner than
                             `mars-dust`), the domes' air-plant hum (60 Hz and its 2nd-6th partials, steady)
                             and a distant rail clank (a 0.9 kHz metal tick pair every 5.3 s, far off and
                             soft); a seamless 16.0 s LOOP, -36 dB band RMS like every ambience loop

Levels as sfx_r39.py: one-shots on the loudest 100 ms of the 200 Hz-5 kHz band, loops by the whole loop's
band RMS (one gain, no limiter).

Usage: python3 tools/concept/audio/sfx_r40.py [subject-or-file ...]   e.g. dome-hit shellback-roll-r40-a
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
from synth import SR, decode, noise, pan, saw, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r40.py"

AURA, WALK, ROLL, COLONY = 4.0, 3.2, 2.5, 16.0


# =========================================================================== helpers

def bp(x, lo, hi):
    """Band-limit a signal to lo-hi Hz (high-pass then low-pass)."""
    return svf(svf(x, np.full(len(x), lo), mode="hp"), np.full(len(x), hi))


def grain(rng, dur, lo, hi, decay):
    """A band-noise grain: `dur` s, lo-hi Hz, exponential decay (fast)."""
    m = max(8, int(dur * SR))
    return unit(bp(noise(m, rng), lo, hi)) * np.exp(-decay * np.arange(m) / m)


def put(out, x, at, position, gain=1.0):
    """Add the mono x at `at` s, panned (clipped to the buffer)."""
    a = int(at * SR)
    if a < 0 or a >= out.shape[1]:
        return
    m = min(len(x), out.shape[1] - a)
    out[:, a:a + m] += pan(x[:m] * gain, position)


def tone(freq, n, partials, rng):
    """A steady harmonic tone: partials = [(multiple, amplitude)], random fixed phases."""
    t = np.arange(n) / SR
    return sum(a * np.sin(2 * np.pi * freq * h * t + rng.uniform(0, 2 * np.pi)) for h, a in partials)


def ping(f, n, decay_s, attack=0.003):
    t = np.arange(n) / SR
    return np.sin(2 * np.pi * f * t) * np.minimum(1.0, t / attack) * np.exp(-t / decay_s)


# =========================================================================== one-shots

def dome_hit():
    rng = np.random.default_rng(4001)
    n = int(1.2 * SR)
    out = np.zeros((2, n))
    # the knock: a low body thud (steady 70-190 Hz noise, hard decay) and a glass tick
    m = int(0.25 * SR)
    body = unit(bp(noise(m, rng), 70.0, 190.0)) * np.exp(-9.0 * np.arange(m) / m)
    put(out, body, 0.0, 0.0, 2.0)
    put(out, grain(rng, 0.03, 1200.0, 3000.0, 6.0), 0.0, 0.0, 1.6)
    # the creak running round: band noise, a resonance wandering 700 -> 1400 Hz, panned across the field
    k = int(0.9 * SR)
    u = np.arange(k) / k
    centre = 700.0 + 700.0 * u ** 1.2
    creak = unit(svf(noise(k, rng), centre, q=9.0, mode="bp")) * np.minimum(1.0, u / 0.08) * (1 - u) ** 1.6
    pos = -0.8 + 1.6 * u
    out[0, int(0.04 * SR):int(0.04 * SR) + k] += 0.55 * creak * (1 - pos) / 2
    out[1, int(0.04 * SR):int(0.04 * SR) + k] += 0.55 * creak * (1 + pos) / 2
    # the fine crackle of stress lines
    for _ in range(60):
        at = 0.05 + rng.uniform(0, 1.0) ** 1.4 * 1.0
        put(out, grain(rng, rng.uniform(0.002, 0.006), 2000.0, 9000.0, 5.0), at, rng.uniform(-0.8, 0.8),
            rng.uniform(0.1, 0.5))
    return level(edge(out, 0.0005, 0.12), -18.0, -4.0)


def dome_break():
    rng = np.random.default_rng(4002)
    n = int(3.6 * SR)
    out = np.zeros((2, n))
    # the crack
    put(out, grain(rng, 0.004, 900.0, 9000.0, 3.0), 0.0, 0.0, 3.0)
    put(out, grain(rng, 0.06, 1500.0, 8000.0, 6.0), 0.0, 0.0, 1.5)
    # the cascade of panes: glass grains, thinning out
    for _ in range(420):
        at = 0.03 + rng.uniform(0, 1.0) ** 2.0 * 2.2
        thin = 1.0 - (at / 2.4)
        if rng.random() > thin + 0.1:
            continue
        put(out, grain(rng, rng.uniform(0.003, 0.010), rng.uniform(2500, 4500), rng.uniform(6000, 10000), 5.0),
            at, rng.uniform(-0.9, 0.9), rng.uniform(0.15, 0.9) * (0.4 + 0.6 * thin))
    # air rushing out: a hiss falling 6 -> 1.2 kHz
    k = int(3.0 * SR)
    u = np.arange(k) / k
    hiss = unit(svf(noise(k, rng), 6000.0 * (0.2 ** u ** 0.7), q=1.4, mode="bp"))
    env = np.minimum(1.0, u / 0.05) * (1 - u) ** 1.3
    out[0, int(0.15 * SR):int(0.15 * SR) + k] += 0.45 * hiss * env
    hiss2 = unit(svf(noise(k, rng), 6000.0 * (0.2 ** u ** 0.7), q=1.4, mode="bp"))
    out[1, int(0.15 * SR):int(0.15 * SR) + k] += 0.45 * hiss2 * env
    # the steel frame groan: rough sawtooth set sinking 150 -> 85 Hz
    k = int(2.4 * SR)
    u = np.arange(k) / k
    f = 150.0 * (85.0 / 150.0) ** u
    g = saw(f, k) + 0.5 * saw(f * 1.5, k) + 0.3 * saw(f * 2.01, k)
    g = unit(svf(g, np.full(k, 1200.0))) * (1 + 0.15 * unit(svf(noise(k, rng), np.full(k, 14.0))))
    genv = np.minimum(1.0, u / 0.12) * (1 - u) ** 1.2
    for ch in range(2):
        out[ch, int(0.3 * SR):int(0.3 * SR) + k] += 0.35 * g * genv
    return level(edge(out, 0.0005, 0.25), -16.0, -4.0)


def voice_ah(f0, n, rng, formants=((800.0, 9.0, 1.0), (1150.0, 9.0, 0.6), (2900.0, 10.0, 0.25)), vib=5.2, depth=0.006):
    """A sung 'ah': a sawtooth (f0 an array) through three formants, with a slow vibrato."""
    t = np.arange(n) / SR
    f = f0 * (1 + depth * np.sin(2 * np.pi * vib * t + rng.uniform(0, 6.28)))
    src = saw(f, n)
    return unit(sum(a * svf(src, np.full(n, fc), q=q, mode="bp") for fc, q, a in formants))


def herald_song():
    rng = np.random.default_rng(4003)
    n = int(1.9 * SR)
    t = np.arange(n) / SR
    # F3 -> A3 -> E3, short slides between the notes (a held phrase, not a bloop)
    notes = [(0.0, 174.6), (0.55, 220.0), (1.1, 164.8)]
    f0 = np.full(n, notes[0][1])
    for at, fr in notes[1:]:
        s = np.clip((t - at) / 0.12, 0.0, 1.0)
        s = s * s * (3 - 2 * s)
        f0 = f0 + (fr - f0) * s
    env = np.minimum(1.0, t / 0.08) * np.clip((1.9 - t) / 0.4, 0.0, 1.0) ** 1.2
    out = np.zeros((2, n))
    for i, (ratio, position) in enumerate([(1.0, -0.4), (1.5, 0.0), (2.0, 0.4)]):
        v = voice_ah(f0 * ratio * (1 + 0.003 * (i - 1)), n, rng, vib=4.8 + 0.5 * i)
        out += pan(v * env * (0.9 if ratio == 1.0 else 0.6), position)
    return level(out, -20.0, -4.0)


def aura_absorb():
    n = int(0.4 * SR)
    out = np.zeros((2, n))
    for f, a, d in [(2100.0, 1.0, 0.20), (3300.0, 0.7, 0.13), (4700.0, 0.45, 0.08), (6100.0, 0.2, 0.05)]:
        p = ping(f, n, d)
        out[0] += a * p
        out[1] += a * ping(f * 1.004, n, d)
    return level(edge(out, 0.0005, 0.05), -22.0, -4.0)


def aura_collapse():
    rng = np.random.default_rng(4005)
    n = int(2.0 * SR)
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    glide = 0.75 ** (np.clip(t / 1.6, 0, 1) ** 1.5)           # a fourth lower over the fall
    for i, f in enumerate([440.0, 523.25, 659.25, 880.0, 1046.5]):
        ph = np.cumsum(f * glide) / SR + rng.uniform()
        v = np.sin(2 * np.pi * ph) + 0.3 * np.sin(4 * np.pi * ph) + 0.12 * np.sin(6 * np.pi * ph)
        v = v * np.minimum(1.0, t / 0.03) * np.clip((2.0 - t) / 1.6, 0.0, 1.0) ** 1.4
        out += pan(v, -0.6 + 0.3 * i) * 0.6
    # the sheet of ring glass shimmering away
    for _ in range(90):
        at = rng.uniform(0, 1.2)
        put(out, ping(rng.uniform(2200, 6500), int(0.2 * SR), 0.06), at, rng.uniform(-0.8, 0.8),
            rng.uniform(0.05, 0.25) * (1 - at / 1.4))
    return level(edge(out, 0.001, 0.2), -18.0, -4.0)


def shellback_lob():
    rng = np.random.default_rng(4006)
    n = int(0.6 * SR)
    out = np.zeros((2, n))
    m = int(0.3 * SR)
    cough = unit(bp(noise(m, rng), 80.0, 260.0)) * np.exp(-8.0 * np.arange(m) / m)
    put(out, cough, 0.0, 0.0, 2.0)
    put(out, grain(rng, 0.02, 600.0, 2500.0, 6.0), 0.0, 0.0, 1.0)
    k = int(0.45 * SR)
    u = np.arange(k) / k
    centre = 500.0 + 1100.0 * np.sin(np.pi * u) ** 1.2     # rising then falling (pressure, not a bloop)
    hiss = unit(svf(noise(k, rng), centre, q=1.2, mode="bp")) * np.minimum(1.0, u / 0.05) * (1 - u) ** 1.8
    out[:, int(0.02 * SR):int(0.02 * SR) + k] += 0.6 * np.array([hiss, hiss[::-1] * 0 + hiss])
    return level(edge(out, 0.0005, 0.08), -17.0, -4.0)


def plates(rng, order, start, step, lo_hi=(600.0, 2500.0), down=True):
    """Eleven plate scrapes, staggered; returns (stereo, length)."""
    n = int(0.9 * SR)
    out = np.zeros((2, n))
    for i in range(11):
        at = start + order[i] * step + rng.uniform(-0.006, 0.006)
        k = int(rng.uniform(0.05, 0.09) * SR)
        u = np.arange(k) / k
        a, b = lo_hi
        centre = (a + (b - a) * rng.uniform(0.3, 1.0)) * ((1.0 - 0.45 * u) if down else (0.55 + 0.45 * u))
        x = unit(svf(noise(k, rng), centre, q=3.0, mode="bp")) * np.sin(np.pi * u) ** 0.7
        put(out, x, at, -0.7 + 1.4 * i / 10.0, rng.uniform(0.5, 1.0))
    return out


def shellback_curl():
    rng = np.random.default_rng(4007)
    out = plates(rng, list(range(11)), 0.02, 0.045)
    n = out.shape[1]
    m = int(0.2 * SR)
    clack = unit(bp(noise(m, rng), 120.0, 700.0)) * np.exp(-10.0 * np.arange(m) / m)
    put(out, clack, 0.55, 0.0, 2.2)
    put(out, grain(rng, 0.012, 1200.0, 4000.0, 6.0), 0.55, 0.0, 1.4)
    return level(edge(out[:, :int(0.8 * SR)], 0.001, 0.08), -20.0, -4.0)


def shellback_uncurl():
    rng = np.random.default_rng(4008)
    out = plates(rng, list(range(10, -1, -1)), 0.06, 0.04, down=False)
    put(out, grain(rng, 0.025, 500.0, 2500.0, 5.0), 0.0, 0.0, 1.3)       # the loosening clack
    k = int(0.4 * SR)
    u = np.arange(k) / k
    exhale = unit(bp(noise(k, rng), 1500.0, 6000.0)) * np.minimum(1.0, u / 0.1) * (1 - u) ** 1.6
    put(out, exhale, 0.3, 0.0, 0.5)
    return level(edge(out[:, :int(0.7 * SR)], 0.001, 0.08), -22.0, -4.0)


# =========================================================================== loops (periodic in L)

def shellback_walk(L, seed=4009):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    out = np.zeros((2, n))
    steps = 4
    period = L / steps
    for s in range(steps):
        at = s * period + rng.normal(0, 0.008)
        side = -0.4 if s % 2 == 0 else 0.4
        m = int(0.3 * SR)
        thud = unit(bp(noise(m, rng), 60.0, 160.0)) * np.exp(-9.0 * np.arange(m) / m)
        pad = unit(bp(noise(m, rng), 300.0, 900.0)) * np.exp(-14.0 * np.arange(m) / m)
        idx = (int(at * SR) + np.arange(m)) % n
        out[:, idx] += pan(1.5 * thud + 0.5 * pad, side)
        for j in range(4):    # the shell plates clacking just after the footfall
            k = int(rng.uniform(0.004, 0.012) * SR)
            x = unit(bp(noise(k, rng), rng.uniform(1000, 1800), rng.uniform(2800, 3600)))
            x = x * np.exp(-6.0 * np.arange(k) / k)
            idx = (int((at + 0.04 + j * rng.uniform(0.02, 0.05)) * SR) + np.arange(k)) % n
            out[:, idx] += pan(x * rng.uniform(0.2, 0.55), side + rng.uniform(-0.3, 0.3))
    return out


def shellback_roll(L, seed=4010):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    turn = gusts(L, n, [(2, 0.45, 0.0)])                  # 2.5 turns: the grind swells as the plates meet rock
    turn5 = gusts(L, n, [(5, 0.4, 0.5), (2, 0.25, 1.0)])
    out = np.zeros((2, n))
    for ch in range(2):
        rumble = circular_noise(n, rng, 40.0, 140.0, 80.0, 40.0)
        crush = circular_noise(n, rng, 700.0, 4500.0, 1800.0, 1500.0)
        grit = circular_noise(n, rng, 3000.0, 8000.0)
        g = gusts(L, n, [(2, 0.45, ch * 0.7)])
        out[ch] = 1.1 * rumble * g + 0.7 * crush * np.clip(turn5, 0.1, None) ** 2 + 0.2 * grit * g ** 2
    return out


def aura_loop(L, seed=4011):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    freqs = [1900.0, 2350.0, 2800.0, 3400.0, 4000.0, 4600.0]
    for ch in range(2):
        for i, f in enumerate(freqs):
            f = round(f * L) / L            # whole cycles in the loop
            sw = 0.5 + 0.5 * np.sin(2 * np.pi * (1 + i % 3) * t / L + rng.uniform(0, 6.28))
            out[ch] += (0.4 + 0.6 * sw) * np.sin(2 * np.pi * f * t + rng.uniform(0, 6.28)) / (1 + 0.15 * i)
        out[ch] = unit(out[ch]) + 0.25 * circular_noise(n, rng, 5000.0, 9000.0)
    return out


def mars_colony(L, seed=4012):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    for ch in range(2):
        g = gusts(L, n, [(1, 0.2, 0.9 + ch), (2, 0.14, 2.1), (5, 0.06, 0.4)], ch * 0.7)
        wind = circular_noise(n, rng, 100.0, 2400.0, 300.0, 500.0)
        hiss = circular_noise(n, rng, 2400.0, 6000.0)
        hum = sum(a * np.sin(2 * np.pi * round(60.0 * h * L) / L * t + rng.uniform(0, 6.28))
                  for h, a in [(1, 0.6), (2, 1.0), (3, 0.55), (4, 0.3), (5, 0.18), (6, 0.1)])
        out[ch] = 0.9 * wind * g + 0.1 * hiss * g ** 2.5 + 0.25 * unit(hum)
    # a distant rail clank: two metal ticks every 5.3 s (three in the loop, 5.33 s apart), far off and soft
    for k in range(3):
        at = 2.0 + k * L / 3.0
        for dt, gain in [(0.0, 1.0), (0.11, 0.7)]:
            m = int(0.25 * SR)
            x = unit(svf(noise(m, rng), np.full(m, 900.0 * (1 + 0.1 * k)), q=14.0, mode="bp")) * np.exp(-14.0 * np.arange(m) / m)
            idx = (int((at + dt) * SR) + np.arange(m)) % n
            out[:, idx] += pan(x, 0.5) * 0.22 * gain
    return out


# =========================================================================== treatment

SOUNDS = {
    "dome-hit": ("shot", dome_hit, None, "a glass knock, a creak round the dome and a fine stress-line crackle"),
    "dome-break": ("shot", dome_break, None, "a crack, a cascade of panes, air rushing out and a frame groan"),
    "herald-song": ("shot", herald_song, None, "a sung 'ah' phrase, three voices, F3 A3 E3"),
    "herald-aura": ("loop", lambda: aura_loop(AURA), -40.0, "a soft high shimmer of steady partials"),
    "aura-absorb": ("shot", aura_absorb, None, "a glassy ping of inharmonic partials"),
    "aura-collapse": ("shot", aura_collapse, None, "a falling chord of sustained voices and a ring-glass shimmer"),
    "shellback-walk": ("loop", lambda: shellback_walk(WALK), -34.0, "padded footfalls and shell plates clacking"),
    "shellback-lob": ("shot", shellback_lob, None, "a dull cough thump and a pressure hiss"),
    "shellback-curl": ("shot", shellback_curl, None, "plates grinding shut and a heavy clack"),
    "shellback-roll": ("loop", lambda: shellback_roll(ROLL), -30.0, "a rumbling rock-crushing grind"),
    "shellback-uncurl": ("shot", shellback_uncurl, None, "plates opening and a short exhale of grit"),
    "mars-colony": ("loop", lambda: mars_colony(COLONY), -36.0, "plains wind, air-plant hum, a distant rail clank"),
}


def build(subject):
    kind, make, band, what = SOUNDS[subject]
    st = make()
    name = f"{subject}-r40-a"
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
        if argv and subject not in argv and f"{subject}-r40-a" not in argv:
            continue
        build(subject)


if __name__ == "__main__":
    main(sys.argv[1:])
