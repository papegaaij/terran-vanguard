#!/usr/bin/env python3
"""Concept round 42 (M7 part F, Level 19 "Foundry of Tharsis", step AUD-F): one proposal each (option a) for the
Revenant Walker's stomp, pod hatch, missile launch, flamer tell and flame loop, the furnace vents' tell and burst,
the Warden Tank's tracks loop, turret whine, burst and death, the conveyor belt loop and the ambience
`mars-foundry`. All fully synthesized (nothing new for CREDITS.md). No bubbly, popping or squelch layers (user
rule): every transient is a band-noise grain with a fast attack and an exponential decay or a steady inharmonic
partial set, every tone is steady or glides slowly with harmonics, never a falling sine "bloop"; every blast and
roar has a ramped attack (no pop at the start). Pending the user's review in round 42.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  walker-stomp-r42-a         the walker's footfall: a dull heavy stomp (a 30-120 Hz thud and a 120-400 Hz body),
                             a low metal clank of the leg (inharmonic partials round 260 Hz, fast decay) and a
                             hydraulic hiss (2-6 kHz, 25 ms attack) easing off; 1.0 s, -16 dB
  walker-hatch-r42-a         a pod's hatch flipping open (the 0.4 s tell): a latch tick, a short pneumatic hiss
                             and a hinge clack (inharmonic partials round 900 Hz); 0.45 s, -22 dB
  walker-missile-r42-a       one pod missile leaving: a dry ignition crack, a rising rocket whoosh (500 -> 2600 Hz,
                             20 ms attack) that slides off to one side and a thin exhaust hiss; 0.8 s, -19 dB
  walker-flame-tell-r42-a    the flamer's 0.6 s tell: a gas hiss rising (band noise 800 -> 3200 Hz, swelling) over
                             a low gas rush; 0.7 s, -22 dB
  walker-flame-r42-a         the flame cone (a loop while it burns): a roaring flame (80-1400 Hz roar flickering
                             at 3, 7 and 11 cycles a loop, channels apart) over a low 40-120 Hz body and a soft
                             3-7 kHz gas hiss; seamless 2.0 s LOOP, -28 dB band RMS
  vent-tell-r42-a            a furnace vent's 1.0 s tell (the glow): a rising gas roar (resonance 250 -> 1400 Hz,
                             swelling) over a deepening rumble; 1.05 s, -22 dB
  vent-burst-r42-a           the vent's burst: a fire blast roaring out (40 ms attack, no pop; 60-3000 Hz, the
                             resonance falling 1400 -> 300 Hz) and dying away; 1.5 s, -16 dB
  tank-tracks-r42-a          the Warden Tank's tracks (a loop, the nearest tank): grinding treads (a 400-1600 Hz
                             grind and track-link ticks 14 a second), a diesel hum (an 41 Hz harmonic set) and a
                             faint turbine whine (1.9 kHz); seamless 2.0 s LOOP, -32 dB band RMS
  tank-turret-r42-a          the turret slew: a servo whine (a harmonic set gliding 420 -> 640 Hz, smooth attack
                             and release) with faint gear ticks; 0.6 s, -24 dB
  tank-burst-r42-a           the hold's burst: a heavy autocannon, four rounds 0.12 s apart (a snap, a 0.8-3 kHz
                             crack and a 50-250 Hz body each) with a short low echo; 0.75 s, -16 dB
  tank-death-r42-a           the tank's hull explosion: a deep boom (30-200 Hz), a fire roar falling 1.5 -> 0.3 kHz,
                             a heavy metal clang (inharmonic partials round 420 Hz), debris clatter and sparks;
                             1.8 s, -15 dB
  belt-rumble-r42-a          the conveyor belts (a loop while belts are on screen): a low roller rumble (40-160 Hz),
                             plate seams passing (a soft 250-900 Hz knock and a 1.2-2.5 kHz rattle, 6 a second)
                             and the drive motor's 50 Hz hum; seamless 2.4 s LOOP, -34 dB band RMS
  mars-foundry-r42-a         ambience `mars-foundry` (Level 19): a conveyor rumble, a far furnace roar, a slow
                             distant hammer (a heavy hit every 4.0 s with a hall echo, far off and soft) and the
                             plateau wind; a seamless 16.0 s LOOP, -36 dB band RMS like every ambience loop

Levels as sfx_r41.py: one-shots on the loudest 100 ms of the 200 Hz-5 kHz band, loops by the whole loop's
band RMS (one gain, no limiter).

Usage: python3 tools/concept/audio/sfx_r42.py [subject-or-file ...]   e.g. walker-stomp mars-foundry-r42-a
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
SCRIPT = "tools/concept/audio/sfx_r42.py"

FLAME, TRACKS, BELT, FOUNDRY = 2.0, 2.0, 2.4, 16.0


# =========================================================================== helpers

def put_wrap(out, x, at, position, gain=1.0):
    """Add the mono x at `at` s, panned, wrapping round the end (for loops)."""
    n = out.shape[1]
    idx = (int(at * SR) + np.arange(len(x))) % n
    np.add.at(out, (slice(None), idx), pan(x * gain, position))


def clang(rng, f, n, partials, decay_s, attack=0.002):
    """Struck metal: a steady inharmonic partial set (ratios, amplitudes), fast attack, exponential decay."""
    return sum(a * ping(f * r, n, decay_s / (1 + 0.6 * i), attack) * np.cos(rng.uniform(0, 6.28))
               for i, (r, a) in enumerate(partials))


def sweep_noise(rng, n, start, end, q, shape=1.0):
    """Band noise through a resonance moving start -> end Hz over n samples."""
    u = np.arange(n) / n
    return unit(svf(noise(n, rng), start + (end - start) * u ** shape, q=q, mode="bp"))


def whole(f, L):
    """f rounded to whole cycles in a loop of L s."""
    return round(f * L) / L


LEG = [(1.0, 1.0), (1.47, 0.6), (2.09, 0.4), (2.76, 0.25)]
PLATE = [(1.0, 1.0), (1.59, 0.55), (2.33, 0.35), (3.1, 0.2)]


# =========================================================================== the walker

def walker_stomp():
    rng = np.random.default_rng(4201)
    n = int(1.0 * SR)
    out = np.zeros((2, n))
    m = int(0.6 * SR)
    t = np.arange(m) / SR
    att = np.minimum(1.0, t / 0.006)
    put(out, unit(bp(noise(m, rng), 30.0, 120.0)) * att * np.exp(-t / 0.16), 0.0, 0.0, 1.5)
    put(out, unit(bp(noise(m, rng), 120.0, 400.0)) * att * np.exp(-t / 0.06), 0.0, 0.0, 1.0)
    put(out, grain(rng, 0.012, 600.0, 3000.0, 6.0), 0.0, 0.0, 0.25)
    put(out, clang(rng, 260.0, int(0.4 * SR), LEG, 0.09), 0.01, 0.1, 0.35)
    k = int(0.5 * SR)
    u = np.arange(k) / SR
    hiss = unit(bp(noise(k, rng), 2000.0, 6000.0)) * np.minimum(1.0, u / 0.025) * np.exp(-u / 0.13)
    put(out, hiss, 0.06, -0.25, 0.3)
    put(out, unit(bp(noise(k, rng), 2200.0, 6500.0)) * np.minimum(1.0, u / 0.025) * np.exp(-u / 0.13), 0.06, 0.25, 0.3)
    return level(edge(out, 0.002, 0.1), -16.0, -4.0)


def walker_hatch():
    rng = np.random.default_rng(4202)
    n = int(0.45 * SR)
    out = np.zeros((2, n))
    put(out, grain(rng, 0.006, 2000.0, 5000.0, 5.0), 0.0, 0.0, 0.6)
    k = int(0.18 * SR)
    u = np.arange(k) / SR
    put(out, unit(bp(noise(k, rng), 1800.0, 6000.0)) * np.minimum(1.0, u / 0.01) * np.exp(-u / 0.05), 0.015, 0.0, 0.45)
    put(out, clang(rng, 900.0, int(0.3 * SR), PLATE, 0.05), 0.11, 0.0, 0.8)
    put(out, grain(rng, 0.04, 150.0, 500.0, 6.0), 0.11, 0.0, 0.8)
    return level(edge(out, 0.0008, 0.05), -22.0, -4.0)


def walker_missile():
    rng = np.random.default_rng(4203)
    n = int(0.8 * SR)
    out = np.zeros((2, n))
    put(out, grain(rng, 0.004, 1000.0, 9000.0, 3.0), 0.0, 0.0, 0.3)
    put(out, grain(rng, 0.06, 120.0, 500.0, 5.0), 0.0, 0.0, 0.9)
    k = int(0.78 * SR)
    t = np.arange(k) / SR
    u = t / (k / SR)
    whoosh = sweep_noise(rng, k, 500.0, 2600.0, 1.8, 0.6) * np.minimum(1.0, t / 0.02) * np.exp(-3.2 * u)
    exhaust = unit(bp(noise(k, rng), 3000.0, 8000.0)) * np.minimum(1.0, t / 0.03) * np.exp(-4.0 * u)
    mono = whoosh + 0.3 * exhaust
    pos = 0.6 * u ** 0.7                      # it slides off to one side
    a = int(0.01 * SR)
    out[0, a:a + k] += mono * np.cos((pos + 1) * np.pi / 4)
    out[1, a:a + k] += mono * np.sin((pos + 1) * np.pi / 4)
    return level(edge(out, 0.0008, 0.08), -19.0, -4.0)


def walker_flame_tell():
    rng = np.random.default_rng(4204)
    n = int(0.7 * SR)
    out = np.zeros((2, n))
    t = np.arange(n) / SR
    u = t / 0.7
    swell = np.minimum(1.0, u / 0.85) ** 1.6
    for ch, side in enumerate((-0.2, 0.2)):
        hiss = sweep_noise(rng, n, 800.0, 3200.0, 1.5, 0.8)
        rush = unit(bp(noise(n, rng), 80.0, 400.0))
        mono = (hiss + 0.5 * rush) * swell
        out += pan(mono, side) * 0.7
    return level(edge(out, 0.005, 0.04), -22.0, -4.0)


def walker_flame(L, seed=4205):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for ch in range(2):
        roar = wander(n, rng, L, 80.0, 1400.0, 420.0, 1.2, [(3, 150.0, ch * 1.3), (7, 80.0, 0.4)], 0.0)
        flicker = gusts(L, n, [(3, 0.25, ch * 1.1), (7, 0.18, 0.5 + ch), (11, 0.12, 2.0)])
        body = circular_noise(n, rng, 40.0, 120.0)
        hiss = circular_noise(n, rng, 3000.0, 7000.0)
        out[ch] = roar * flicker + 0.6 * body * gusts(L, n, [(2, 0.2, ch)]) + 0.18 * hiss * flicker
    return out


# =========================================================================== the furnace vents

def vent_tell():
    rng = np.random.default_rng(4206)
    n = int(1.05 * SR)
    out = np.zeros((2, n))
    t = np.arange(n) / SR
    u = t / 1.05
    swell = (0.08 + 0.92 * u ** 1.8)
    for side in (-0.3, 0.3):
        roar = sweep_noise(rng, n, 250.0, 1400.0, 1.3, 1.2)
        rumble = unit(bp(noise(n, rng), 40.0, 160.0))
        out += pan((roar + 0.7 * rumble * u) * swell, side) * 0.7
    return level(edge(out, 0.01, 0.03), -22.0, -4.0)


def vent_burst():
    rng = np.random.default_rng(4207)
    n = int(1.5 * SR)
    out = np.zeros((2, n))
    t = np.arange(n) / SR
    env = np.minimum(1.0, t / 0.04) ** 1.5 * np.exp(-t / 0.45)
    for side in (-0.35, 0.35):
        roar = sweep_noise(rng, n, 1400.0, 300.0, 0.9, 0.5)
        broad = unit(bp(noise(n, rng), 60.0, 3000.0))
        low = unit(bp(noise(n, rng), 40.0, 150.0))
        out += pan((roar + 0.5 * broad + 0.8 * low * np.exp(-t / 0.25)) * env, side) * 0.7
    return level(edge(out, 0.002, 0.15), -16.0, -4.0)


# =========================================================================== the Warden Tank

def tank_tracks(L, seed=4208):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    hum = sum(a * np.sin(2 * np.pi * whole(41.0 * h, L) * t + rng.uniform(0, 6.28))
              for h, a in [(1, 1.0), (2, 0.8), (3, 0.5), (4, 0.3), (6, 0.15)])
    whine = np.sin(2 * np.pi * whole(1900.0, L) * t) + 0.3 * np.sin(2 * np.pi * whole(3800.0, L) * t)
    for ch in range(2):
        grind = circular_noise(n, rng, 400.0, 1600.0, 800.0, 500.0)
        out[ch] = 0.6 * grind * gusts(L, n, [(4, 0.25, ch), (6, 0.15, 1.0)]) + 0.35 * unit(hum) + 0.05 * whine
    links = int(round(14 * L))                # track links passing the sprockets
    for i in range(links):
        at = (i + 0.5) * L / links + rng.normal(0, 0.003)
        for side in (-0.5, 0.5):
            x = grain(rng, rng.uniform(0.012, 0.025), 700.0, 2600.0, 6.0)
            put_wrap(out, x, at + rng.uniform(0, 0.01), side, rng.uniform(0.25, 0.45))
    return out


def tank_turret():
    rng = np.random.default_rng(4209)
    n = int(0.6 * SR)
    t = np.arange(n) / SR
    u = t / 0.6
    f0 = 420.0 + 220.0 * np.minimum(1.0, u / 0.5) ** 0.7
    ph = 2 * np.pi * np.cumsum(f0) / SR
    whine = sum(a * np.sin(h * ph + rng.uniform(0, 6.28)) for h, a in [(1, 1.0), (2, 0.45), (3, 0.25), (4, 0.12)])
    env = np.minimum(1.0, t / 0.08) * np.minimum(1.0, (0.6 - t) / 0.15)
    mono = unit(whine) * env
    out = pan(mono, 0.0)
    for i in range(8):                        # gear ticks
        put(out, grain(rng, 0.004, 2500.0, 6000.0, 5.0), 0.05 + i * 0.06, rng.uniform(-0.2, 0.2), 0.12)
    return level(edge(out, 0.003, 0.03), -24.0, -4.0)


def tank_burst():
    rng = np.random.default_rng(4210)
    n = int(0.75 * SR)
    out = np.zeros((2, n))
    for i in range(4):
        at = 0.002 + i * 0.12
        pos = (-0.1, 0.05, -0.05, 0.1)[i]
        put(out, grain(rng, 0.003, 800.0, 10000.0, 3.0), at, pos, 0.2)
        put(out, grain(rng, 0.045, 800.0, 3000.0, 6.0), at, pos, 1.0)
        put(out, grain(rng, 0.09, 50.0, 250.0, 5.0), at, pos, 1.4)
        put(out, grain(rng, 0.12, 60.0, 400.0, 4.0), at + 0.07, -pos, 0.35)   # a short low echo
    return level(edge(out, 0.0005, 0.08), -16.0, -4.0)


def tank_death():
    rng = np.random.default_rng(4211)
    n = int(1.8 * SR)
    out = np.zeros((2, n))
    t = np.arange(n) / SR
    att = np.minimum(1.0, t / 0.01)
    boom = unit(bp(noise(n, rng), 30.0, 200.0)) * att * np.exp(-t / 0.4)
    put(out, boom, 0.0, 0.0, 2.2)
    for side in (-0.4, 0.4):
        roar = sweep_noise(rng, n, 1500.0, 300.0, 0.9, 0.5) * np.minimum(1.0, t / 0.02) * np.exp(-t / 0.35)
        put(out, roar, 0.0, side, 0.7)
    put(out, grain(rng, 0.005, 800.0, 10000.0, 3.0), 0.0, 0.0, 0.3)
    put(out, clang(rng, 420.0, int(1.0 * SR), LEG, 0.25), 0.02, 0.0, 0.6)
    for _ in range(8):                        # debris clatter: pieces of hull landing
        at = rng.uniform(0.25, 1.2)
        f = rng.uniform(700.0, 1800.0)
        put(out, clang(rng, f, int(0.15 * SR), PLATE, rng.uniform(0.02, 0.05)), at, rng.uniform(-0.8, 0.8),
            rng.uniform(0.08, 0.2) * (1.4 - at))
        put(out, grain(rng, 0.03, 150.0, 600.0, 6.0), at, 0.0, 0.15)
    for _ in range(50):                       # sparks
        at = 0.02 + rng.uniform(0, 1.0) ** 1.8 * 0.9
        put(out, grain(rng, rng.uniform(0.002, 0.005), 2500.0, 9000.0, 5.0), at, rng.uniform(-0.9, 0.9),
            rng.uniform(0.08, 0.3) * (1.0 - at))
    return level(edge(out, 0.0008, 0.3), -15.0, -4.0)


# =========================================================================== the foundry

def belt_rumble(L, seed=4212):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    hum = sum(a * np.sin(2 * np.pi * whole(50.0 * h, L) * t + rng.uniform(0, 6.28))
              for h, a in [(1, 0.7), (2, 1.0), (3, 0.4), (4, 0.2)])
    for ch in range(2):
        rumble = circular_noise(n, rng, 40.0, 160.0, 80.0, 50.0)
        grind = circular_noise(n, rng, 200.0, 1200.0, 450.0, 300.0)
        out[ch] = 0.35 * rumble * gusts(L, n, [(3, 0.15, ch)]) + 0.12 * unit(hum) + 0.3 * grind
    seams = int(round(6 * L))                 # plate seams passing over the rollers
    for i in range(seams):
        at = (i + 0.5) * L / seams + rng.normal(0, 0.004)
        side = rng.uniform(-0.5, 0.5)
        m = int(0.12 * SR)
        u = np.arange(m) / SR
        knock = unit(bp(noise(m, rng), 250.0, 900.0)) * np.minimum(1.0, u / 0.003) * np.exp(-u / 0.025)
        put_wrap(out, knock, at, side, 1.0)
        put_wrap(out, grain(rng, 0.03, 1200.0, 2500.0, 6.0), at + 0.01, side, 0.4)
    return out


def mars_foundry(L, seed=4213):
    rng = np.random.default_rng(seed)
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    for ch in range(2):
        g = gusts(L, n, [(1, 0.22, 0.6 + ch), (2, 0.14, 1.9), (5, 0.06, 0.2)], ch * 0.8)
        wind = circular_noise(n, rng, 120.0, 2400.0, 350.0, 500.0)
        conveyor = circular_noise(n, rng, 35.0, 180.0, 70.0, 50.0) * gusts(L, n, [(8, 0.12, ch), (13, 0.08, 1.0)])
        furnace = circular_noise(n, rng, 80.0, 900.0, 240.0, 200.0) * gusts(L, n, [(1, 0.15, 2.0 + ch), (3, 0.1, 0.7)])
        out[ch] = 0.55 * wind * g + 0.8 * conveyor + 0.55 * furnace
    # a slow distant hammer: a heavy hit every 4.0 s (four in the loop), far off, with a hall echo
    for k in range(4):
        at = 1.3 + k * L / 4.0
        side = 0.35 if k % 2 == 0 else 0.25
        m = int(1.2 * SR)
        u = np.arange(m) / SR
        thud = unit(bp(noise(m, rng), 60.0, 220.0)) * np.minimum(1.0, u / 0.006) * np.exp(-u / 0.09)
        ring = clang(rng, 520.0, m, LEG, 0.35, attack=0.004)
        hit = unit(svf(1.0 * thud + 0.35 * unit(ring), np.full(m, 1400.0)))
        for dt, gain in [(0.0, 1.0), (0.27, 0.35), (0.55, 0.15)]:
            put_wrap(out, hit, at + dt, side - 0.3 * dt, 0.5 * gain)
    return out


# =========================================================================== treatment

SOUNDS = {
    "walker-stomp": ("shot", walker_stomp, None, "a dull heavy stomp, a leg clank and a hydraulic hiss"),
    "walker-hatch": ("shot", walker_hatch, None, "a latch tick, a pneumatic hiss and a hinge clack"),
    "walker-missile": ("shot", walker_missile, None, "an ignition crack and a rocket whoosh sliding away"),
    "walker-flame-tell": ("shot", walker_flame_tell, None, "a gas hiss rising over a low rush"),
    "walker-flame": ("loop", lambda: walker_flame(FLAME), -28.0, "a roaring, flickering flame"),
    "vent-tell": ("shot", vent_tell, None, "a rising gas roar over a deepening rumble"),
    "vent-burst": ("shot", vent_burst, None, "a fire blast roaring out and dying"),
    "tank-tracks": ("loop", lambda: tank_tracks(TRACKS), -32.0, "grinding treads, a diesel hum and a turbine whine"),
    "tank-turret": ("shot", tank_turret, None, "a servo whine with gear ticks"),
    "tank-burst": ("shot", tank_burst, None, "a heavy autocannon, four rounds"),
    "tank-death": ("shot", tank_death, None, "a hull explosion, a metal clang, debris and sparks"),
    "belt-rumble": ("loop", lambda: belt_rumble(BELT), -34.0, "a roller rumble, plate seams knocking, a motor hum"),
    "mars-foundry": ("loop", lambda: mars_foundry(FOUNDRY), -36.0,
                     "a conveyor rumble, a far furnace roar, a slow distant hammer and the plateau wind"),
}


def build(subject):
    kind, make, band, what = SOUNDS[subject]
    st = make()
    name = f"{subject}-r42-a"
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
        if argv and subject not in argv and f"{subject}-r42-a" not in argv:
            continue
        build(subject)


if __name__ == "__main__":
    main(sys.argv[1:])
