#!/usr/bin/env python3
"""Concept round 36 (M7 "Act 3 - Red Dust" design phase, step A-audio): a/b proposals for four Act 3
sounds: the Burrower's eruption (Level 15 on; its 1.0 s bulge tell, then the break-out), the canyon wall
scrape (Level 16: the ship touching rock), the Ion Beam's loop (the Act 3 front beam, heard for as long
as fire is held) and the dust-storm bed (Level 18's `mars-storm` ambience). Recorded layers (Freesound
originals, CC0 / CC-BY, in CREDITS.md) and synthesized layers mixed. The round-36 file names already used
by Level 14's sounds (sfx_r36.py) are different subjects, hence this script's `b` suffix.

Outputs (design/audio/sfx/concept/, the unchosen variants in concept/rejected/; OGG Vorbis q6, 44.1 kHz stereo; each one-shot starts at its event):

  burrow-eruption-r36-a.ogg  a "sand burst": the tell's ground rumble swelling 14 dB over 1.0 s ("Cracking
                             Earthquake", uagadugu, CC0, low-passed) with a synthesized pressure tone
                             rising 38 -> 64 Hz and sand hissing out of the cracks (a dense synthesized
                             grain hiss, 2-7 kHz, thickening); at 1.0 s the break-out: a heavy impact on
                             sand ("Hard Impact on Sand", Elements-Library, CC0), a sub thump and the
                             sand raining back down ("SAND POUR.wav", nicoproson, CC0); 2.1 s
  burrow-eruption-r36-b.ogg  b "the swimmer breaks out": a low churn of sand drifting under the ground
                             (a synthesized noise band, 70-320 Hz, slowly wandering) and a muffled growl
                             under it just before the break ("Deep Roar Echo 2", noahpardo, CC0, slowed,
                             low-passed); at 1.0 s gravel and rock thrown up and falling ("Gravel Impacts
                             and Falls.wav", lolamadeus, CC0) over a synthesized spray (a wide noise
                             burst whose high-pass rises as it thins) and a sub thump; 2.1 s
  canyon-scrape-r36-a.ogg    a "rock grind": the hull dragged along stone ("Metal pipe along concrete
                             2.wav", deleted Freesound user, CC0), a dull hit under it ("Hard Impact on
                             Sand", Elements-Library, CC0, high-passed) and grit falling ("Gravel Impacts
                             and Falls.wav", lolamadeus, CC0), a short thump; 0.65 s
  canyon-scrape-r36-b.ogg    b "metal screech": a metal side impact scraping ("big metal side impact 3
                             scraping.wav", Halleck, CC-BY 4.0) with a synthesized spark hiss (a fine
                             dense noise spray, 3-9 kHz, dying in 0.25 s; no clicks) and a thump; 0.6 s
  ion-beam-loop-r36-a.ogg    a "the chosen hum, heavier": the round-04 Ion Beam loop's source and window
                             ("laser beam", peepholecircus, CC0; 2.60 s, 0.12 s cross-fade, as
                             shot-beam-r04-a) over a synthesized ion body (two harmonic voices on 110.00 /
                             110.38 Hz beating once a loop, harmonics peaking at 220-440 Hz) and a faint
                             circular hiss (2-6 kHz); a seamless 2.60 s LOOP
  ion-beam-loop-r36-b.ogg    b "ion stream", synthesized: a buzzing core (two saw-like voices on 165.0 /
                             165.5 Hz, a formant at 1.2 kHz), a shimmer band at 1.8 kHz fluttering 16
                             times a second (a smooth sine flutter, not pulses), a circular hiss
                             (3-8 kHz) and a slow swell twice a loop; a seamless 2.00 s LOOP
  dust-storm-bed-r36-a.ogg   a "recorded front": a sandstorm's high, steady wind ("G56-22-Sandstorm.wav",
                             craigsmith, CC0, 80 Hz high-pass) over a furious low wind howl ("20101121.
                             wind.howl.furious.01.wav", dobroide, CC-BY 4.0, 900 Hz low-pass), each cut to
                             16 s and looped with a 1.5 s equal-power cross-fade; a seamless 16.0 s LOOP
  dust-storm-bed-r36-b.ogg   b "synthesized front": a low roar (noise peaking at 120 Hz), a grit hiss
                             (2.5-9 kHz) and a howl band wandering 450-900 Hz, all swelling with gusts
                             (sinusoids of 1, 3 and 5 cycles a loop); the noise made circularly (inverse
                             FFT) and filtered over three passes, the middle one kept, so the loop joins
                             with no cross-fade; a seamless 16.0 s LOOP

Levels (sfx_r24.py's `level`: the loudest 100 ms of the 200 Hz-5 kHz band, limited at a ceiling; set
against assets/sfx/, measured the same way): the burrow eruption -19 dB (a telegraph heard several times
a wave: under the root burst's -17 and the edge warning's -16.2, over the ship sinking's -20; a reaches
-19.6 and b -20.3, their dense lows held by the -2 dBFS ceiling, at -16.3 / -14.9 LUFS as loud as the
root burst's -17.2 LUFS); the wall
scrape -18 dB (a hit on the player: the armour hit -20.5, the shield hit -22.6, the ship hit -18). The
loops are levelled as import_sfx.py levels them (`band_rms`, the whole loop's 200 Hz-5 kHz RMS, one gain,
no limiter that would break the seam): the beam loops at -30 dB like the round-04 beam loops (all three
at -30.0), the storm beds at -36 dB like every ambience loop (-36.0 to -36.2; the rain bed -38.8). No
bubble, popping or squelch layers (user rule).

Usage: python3 tools/concept/audio/sfx_r36b.py [subject-or-file ...]   e.g. canyon-scrape ion-beam-loop-r36-b
Deterministic. Needs the cached Freesound originals (tools/concept/audio/freesound_fetch.py
--download); without one a layer falls back to the public HQ preview (printed).
"""
import os
import sys
import tempfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from import_sfx import band_rms_db, make_loop, trim_leading_silence  # noqa: E402
from sfx_r24 import level  # noqa: E402
from sfx_r25 import fs, source  # noqa: E402
from sfx_r32 import loudest_db, recorded, report, thump  # noqa: E402
from sfx_r36 import edge, glide, seam_db, unit  # noqa: E402
from synth import SR, decode, noise, pan, ramp, sine, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r36b.py"
CC0 = "CC0 1.0"
BY4 = "CC-BY 4.0"

# already credited for earlier rounds
EARTHQUAKE = dict(**fs("uagadugu", 222521, 222, 255863),
                  title="Cracking Earthquake (cracking soil, cracking stone)", author="uagadugu", licence=CC0)
DEEP_ROAR = dict(**fs("noahpardo", 345735, 345, 6246023), title="Deep Roar Echo 2.wav", author="noahpardo",
                 licence=CC0)
LASER_BEAM = dict(**fs("peepholecircus", 169991, 169, 2747497), title="laser beam", author="peepholecircus",
                  licence=CC0)
# new in this round
SAND_IMPACT = dict(**fs("Elements-Library", 683785, 683, 14876768), title="Hard Impact on Sand",
                   author="Elements-Library", licence=CC0)
SAND_POUR = dict(**fs("nicoproson", 627070, 627, 457982), title="SAND POUR.wav", author="nicoproson", licence=CC0)
GRAVEL = dict(**fs("lolamadeus", 179341, 179, 544580), title="Gravel Impacts and Falls.wav", author="lolamadeus",
              licence=CC0)
PIPE = dict(**fs("deleted_user_2104797", 164673, 164, 2104797), title="Metal pipe along concrete 2.wav",
            author="deleted_user_2104797", licence=CC0)
SIDE_SCRAPE = dict(**fs("Halleck", 121623, 121, 6281), title="big metal side impact 3 scraping.wav",
                   author="Halleck", licence=BY4)
SANDSTORM = dict(**fs("craigsmith", 438877, 438, 2524442), title="G56-22-Sandstorm.wav", author="craigsmith",
                 licence=CC0)
HOWL = dict(**fs("dobroide", 109371, 109, 8043), title="20101121.wind.howl.furious.01.wav", author="dobroide",
            licence=BY4)

BEAM_A = 2.6          # loop lengths (s): every periodic synthesized part completes whole cycles in its loop
BEAM_B = 2.0
STORM = 16.0

# subject: level (dB, the loudest 100 ms of the 200 Hz-5 kHz band) and ceiling (dBFS), or for a loop
# `band_rms` (the whole loop's 200 Hz-5 kHz RMS) and its length; per variant its layers as in sfx_r36.py
# (a recorded layer: source, cut (import_sfx.py's process keys, offsets after its leading-silence trim),
# optional speed, `approach` (dB, rising to full), pan; a looped recorded layer: `loop` = (start, length,
# cross-fade) instead of a cut; a synthesized layer: `synth` and its parameters; each enters at `at`, the
# others `gain` dB from the first, measured on the loudest 100 ms).
SOUNDS = {
    "burrow-eruption": dict(level=-19.0, ceiling=-2.0, variants={
        "a": [dict(**EARTHQUAKE, cut=dict(offset=1.3, length=1.2, fade=0.35, fadein=0.1, highpass=70, lowpass=2500),
                   at=0.0, approach=-14.0),
              dict(synth="pressure", at=0.0, gain=-5.0, length=1.06, f=(38.0, 64.0)),
              dict(synth="grains", at=0.25, gain=-9.0, length=0.8, seed=3651),
              dict(**SAND_IMPACT, cut=dict(offset=1.48, length=0.9, fade=0.55, fadein=0.005, highpass=60), at=0.874,
                   gain=4.0),
              dict(synth="thump", at=1.0, gain=-5.0, length=0.55, start=72.0, end=30.0, tau=0.16),
              dict(**SAND_POUR, cut=dict(offset=10.65, length=1.05, fade=0.6, fadein=0.06, highpass=300), at=1.06,
                   gain=-5.0, pan=0.25)],
        "b": [dict(synth="churn", at=0.0, length=1.15, rise=1.0, seed=3652, gain=0.0),
              dict(**DEEP_ROAR, cut=dict(offset=0.0, length=0.9, fade=0.3, fadein=0.25, highpass=50, lowpass=1500),
                   at=0.45, gain=-6.0, speed=0.7),
              dict(**GRAVEL, cut=dict(offset=0.35, length=1.15, fade=0.6, fadein=0.003, highpass=90), at=0.95,
                   gain=3.0, pan=-0.15),
              dict(synth="spray", at=0.98, gain=-3.0, length=0.9, seed=3653),
              dict(synth="thump", at=1.0, gain=-4.0, length=0.5, start=78.0, end=34.0, tau=0.14)]}),
    "canyon-scrape": dict(level=-18.0, ceiling=-3.0, variants={
        "a": [dict(**PIPE, cut=dict(offset=3.6, length=0.62, fade=0.3, fadein=0.006, highpass=150), at=0.0),
              dict(**SAND_IMPACT, cut=dict(offset=1.58, length=0.3, fade=0.2, fadein=0.003, highpass=90), at=0.0,
                   gain=-3.0),
              dict(**GRAVEL, cut=dict(offset=2.3, length=0.55, fade=0.35, fadein=0.01, highpass=250), at=0.08,
                   gain=-8.0, pan=0.3),
              dict(synth="thump", at=0.0, gain=-6.0, length=0.25, start=95.0, end=45.0, tau=0.06)],
        "b": [dict(**SIDE_SCRAPE, cut=dict(offset=0.08, length=0.6, fade=0.3, fadein=0.003, highpass=120), at=0.0),
              dict(synth="sparks", at=0.03, gain=-8.0, length=0.32, seed=3654),
              dict(synth="thump", at=0.0, gain=-6.0, length=0.25, start=95.0, end=45.0, tau=0.06)]}),
    "ion-beam-loop": dict(band_rms=-30.0, variants={
        "a": dict(length=BEAM_A, layers=[
            dict(**LASER_BEAM, loop=(5.60, BEAM_A, 0.12), at=0.0),
            dict(synth="body", gain=-5.0, f0=286 / BEAM_A, f1=287 / BEAM_A, peak=330.0, seed=3655),
            dict(synth="hiss", gain=-16.0, band=(2000.0, 6000.0), seed=3656)]),
        "b": dict(length=BEAM_B, layers=[
            dict(synth="core", f0=165.0, f1=165.5, formant=1200.0, seed=3657),
            dict(synth="shimmer", gain=-6.0, centre=1800.0, flutter=16.0, seed=3658),
            dict(synth="hiss", gain=-15.0, band=(3000.0, 8000.0), seed=3659)])}),
    "dust-storm-bed": dict(band_rms=-36.0, variants={
        "a": dict(length=STORM, layers=[
            dict(**SANDSTORM, loop=(20.0, STORM, 1.5), highpass=80.0, at=0.0),
            dict(**HOWL, loop=(41.0, STORM, 1.5), lowpass=900.0, gain=-4.0)]),
        "b": dict(length=STORM, layers=[
            dict(synth="storm", seed=3660)])}),
}


# =========================================================================== synthesized layers (one-shots)

def pressure(p):
    """The bulge's pressure: a low tone rising f[0] -> f[1] Hz with its 2nd-4th harmonics (so small
    speakers carry it), swelling over the layer and cut off at its end (the break-out follows)."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    f = ramp(n, *p["f"])
    x = sine(f, n) + 0.6 * sine(2 * f, n) + 0.35 * sine(3 * f, n) + 0.2 * sine(4 * f, n)
    env = (t / p["length"]) ** 1.8 * np.clip((p["length"] - t) / 0.04, 0, 1)
    return edge(pan(unit(x) * env, 0.0))


def grains(p):
    """Sand hissing out of the cracks: a dense rain of 2-6 ms noise grains (2-7 kHz, 60 -> 900 a second,
    so dense they fuse into a grainy hiss, never single clicks) thickening over the layer, across the field."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    out = np.zeros((2, n + int(0.01 * SR)))
    k = 0.0
    while True:
        k += rng.exponential(1.0 / (60.0 + 840.0 * (k / p["length"]) ** 1.5))
        if k >= p["length"]:
            break
        m = int(rng.uniform(0.002, 0.006) * SR)
        a = int(k * SR)
        x = unit(svf(noise(m, rng), np.full(m, rng.uniform(2000, 7000)), q=1.0, mode="bp")) * np.hanning(m)
        out[:, a:a + m] += pan(x * rng.uniform(0.3, 1.0) * (0.4 + 0.6 * k / p["length"]), rng.uniform(-0.6, 0.6))
    return edge(out[:, :n], 0.02, 0.03)


def churn(p):
    """Sand churning under the ground: noise through a low band-pass whose centre wanders slowly
    (70-320 Hz, a smoothed random walk; nothing fast, nothing bubbly), swelling over `rise` s."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    walk = np.cumsum(rng.normal(0, 1, n // 2205 + 2))
    walk = np.interp(np.arange(n) / 2205, np.arange(len(walk)), walk)
    centre = 70 * (320 / 70) ** np.clip(0.35 + 0.15 * walk + 0.4 * t / p["length"], 0, 1)
    chans = [unit(svf(noise(n, rng), centre, q=1.2, mode="bp")) for _ in range(2)]
    env = np.clip(t / p["rise"], 0, 1) ** 2 * np.clip((p["length"] - t) / 0.15, 0, 1)
    return edge(np.array(chans) * env)


def spray(p):
    """Sand thrown up: a wide noise burst (fast attack) whose high-pass rises 300 -> 2500 Hz as it thins
    out (tau 0.25 s)."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    cut = ramp(n, 300.0, 2500.0)
    chans = [unit(svf(noise(n, rng), cut, q=0.7, mode="hp")) for _ in range(2)]
    env = np.clip(t / 0.008, 0, 1) * np.exp(-t / 0.25)
    return edge(np.array(chans) * env, 0.002, 0.05)


def sparks(p):
    """A spark spray off the hull: dense fine noise (3-9 kHz, two decorrelated channels) with a fast
    attack dying away in 0.25 s and a slight shimmer from a 3-voice comb (no clicks, no pops)."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    chans = []
    for _ in range(2):
        x = svf(svf(noise(n, rng), np.full(n, 3000.0), mode="hp"), np.full(n, 9000.0))
        for d in (0.00037, 0.00053, 0.00071):                  # a short comb: a metallic sheen
            k = int(d * SR)
            x[k:] += 0.35 * x[:-k]
        chans.append(unit(x))
    env = np.clip(t / 0.004, 0, 1) * np.exp(-t / 0.08)
    return edge(np.array(chans) * env, 0.002, 0.04)


# =========================================================================== synthesized layers (loops)
# Every frequency below is a whole number of cycles per loop, and the noise is circular (inverse FFT), so
# each layer's end continues its start exactly.

def circular_noise(n, rng, lo, hi, peak=None, width=None):
    """A circular noise band (lo-hi Hz; with `peak`/`width` a Gaussian bump instead of a flat band)."""
    freqs = np.fft.rfftfreq(n, 1 / SR)
    shape = np.where((freqs > lo) & (freqs < hi), 1.0, 0.0)
    if peak:
        shape = shape * np.exp(-((freqs - peak) / width) ** 2)
    x = np.fft.irfft(shape * np.exp(2j * np.pi * rng.random(len(freqs))), n)
    return unit(x)


def harmonic(f0, n, count, amp, rng, vib_cycles, base):
    """A harmonic voice on f0 (every partial a multiple of the loop's base frequency, a slight periodic
    vibrato): sum of `count` partials with amplitude amp(h, f)."""
    t = np.arange(n) / SR
    x = np.zeros(n)
    for h in range(1, count + 1):
        f = h * f0
        vib = 0.002 * np.sin(2 * np.pi * vib_cycles * base * t + rng.uniform(0, 2 * np.pi))
        ph = np.concatenate([[0.0], np.cumsum(f * (1 + vib))[:-1]]) / SR + rng.uniform()
        x += amp(h, f) * np.sin(2 * np.pi * ph)
    return x


def body(p, L):
    """The ion body under the recorded hum: two voices (f0, f1, beating once a loop), harmonics 1-8 with
    a spectral peak around `peak` Hz, slightly apart in the stereo field."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    base = 1.0 / L
    amp = lambda h, f: (1.0 / h ** 0.7) * (1 + 1.4 * np.exp(-((f - p["peak"]) / 140.0) ** 2))  # noqa: E731
    out = pan(harmonic(p["f0"], n, 8, amp, rng, 2, base), -0.25) + pan(harmonic(p["f1"], n, 8, amp, rng, 3, base), 0.25)
    return out


def hiss(p, L):
    """A faint circular hiss in `band` Hz, a different spectrum per channel."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    return np.array([circular_noise(n, rng, *p["band"]) for _ in range(2)])


def core(p, L):
    """The ion stream's core: two saw-like voices (harmonics 1-14 at 1/h) on f0 / f1 Hz, a formant bump
    at `formant` Hz, swelling 10 % twice a loop."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    base = 1.0 / L
    t = np.arange(n) / SR
    amp = lambda h, f: (1.0 / h) * (1 + 2.0 * np.exp(-((f - p["formant"]) / 300.0) ** 2))  # noqa: E731
    out = pan(harmonic(p["f0"], n, 14, amp, rng, 2, base), -0.2) + pan(harmonic(p["f1"], n, 14, amp, rng, 4, base), 0.2)
    return out * (1 + 0.1 * np.sin(2 * np.pi * 2 * base * t))


def shimmer(p, L):
    """A shimmer band (circular noise, Gaussian round `centre` Hz) fluttering `flutter` times a second: a
    smooth sine amplitude modulation (depth 45 %), the two channels a quarter cycle apart."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    t = np.arange(n) / SR
    return np.array([circular_noise(n, rng, 600.0, 4000.0, p["centre"], 450.0)
                     * (1 - 0.45 * (0.5 + 0.5 * np.sin(2 * np.pi * p["flutter"] * t + ch * np.pi / 2)))
                     for ch in range(2)])


def storm(p, L):
    """A synthesized dust-storm bed, periodic in L: a low roar (noise peaking at 120 Hz), a grit hiss
    (2.5-9 kHz) and a howl (noise through a band-pass wandering 450-900 Hz), all riding gusts (sinusoids
    of 1, 3 and 5 cycles a loop). The noise is circular; the howl's time-varying filter runs over three
    loop lengths and the middle one is kept (its state has settled, its input and centre are periodic)."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    base = 1.0 / L
    t = np.arange(n) / SR
    out = np.zeros((2, n))
    for ch in range(2):
        ph = ch * 0.7
        gust = (0.62 + 0.2 * np.sin(2 * np.pi * base * t + ph) + 0.12 * np.sin(2 * np.pi * 3 * base * t + 1.3 + ph)
                + 0.06 * np.sin(2 * np.pi * 5 * base * t + 2.1 + ph))
        roar = circular_noise(n, rng, 25.0, 600.0, 120.0, 110.0)
        grit = circular_noise(n, rng, 2500.0, 9000.0)
        src = np.tile(circular_noise(n, rng, 200.0, 2000.0), 3)
        centre = np.tile(650 + 160 * np.sin(2 * np.pi * 2 * base * t + ph) + 90 * np.sin(2 * np.pi * 7 * base * t + ph),
                         3)
        howl = unit(svf(src, centre, q=4.0, mode="bp")[n:2 * n])
        out[ch] = (0.45 * roar * gust + 0.4 * grit * gust ** 2 + 0.5 * howl * gust ** 1.5)
    return out


SYNTHS = {"thump": thump, "pressure": pressure, "grains": grains, "churn": churn, "spray": spray,
          "sparks": sparks}
LOOP_SYNTHS = {"body": body, "hiss": hiss, "core": core, "shimmer": shimmer, "storm": storm}


# =========================================================================== treatment

def one_shot(subject, v, paths):
    sound = SOUNDS[subject]
    files = iter(paths)
    parts = []
    for layer in sound["variants"][v]:
        if "page" in layer:
            st = recorded({k: val for k, val in layer.items() if k != "approach"}, next(files))
            if "glide" in layer:
                st = glide(st, *layer["glide"])
            if "approach" in layer:
                st = st * undb(layer["approach"] * (1 - np.linspace(0, 1, st.shape[1])))
            if "pan" in layer:
                st = pan(st.mean(axis=0), layer["pan"])
        else:
            st = SYNTHS[layer["synth"]](layer)
        if parts:
            st = st * undb(loudest_db(parts[0][1]) + layer.get("gain", 0.0) - loudest_db(st))
        parts.append((int(layer["at"] * SR), st))
    out = np.zeros((2, max(at + st.shape[1] for at, st in parts)))
    for at, st in parts:
        out[:, at:at + st.shape[1]] += st
    return level(out, sound["level"], sound["ceiling"])


def loop(subject, v, paths):
    """A loop: recorded layers cut with import_sfx.py's make_loop (equal-power or, for correlated ends,
    linear cross-fade), synthesized layers periodic in the loop; summed, DC removed, one gain to the band
    RMS (no limiter)."""
    sound = SOUNDS[subject]
    spec = sound["variants"][v]
    L = spec["length"]
    n = int(round(L * SR))
    files = iter(paths)
    parts = []
    for layer in spec["layers"]:
        if "page" in layer:
            x = trim_leading_silence(decode(next(files)))
            if "highpass" in layer:            # filtered before the loop is cut, so the seam stays continuous
                x = np.array([svf(svf(ch, layer["highpass"], mode="hp"), layer["highpass"], mode="hp") for ch in x])
            if "lowpass" in layer:
                x = np.array([svf(svf(ch, layer["lowpass"]), layer["lowpass"]) for ch in x])
            x = make_loop(x, *layer["loop"], "auto")
            if x.shape[0] == 1:
                x = np.vstack([x, x])
            x = x[:, :n]
        else:
            x = LOOP_SYNTHS[layer["synth"]](layer, L)
        if parts:
            x = x * undb(loudest_db(parts[0]) + layer.get("gain", 0.0) - loudest_db(x))
        parts.append(x)
    out = sum(parts)
    out = out - out.mean(axis=1, keepdims=True)
    return out * undb(sound["band_rms"] - band_rms_db(out))


def layers_of(subject, v):
    spec = SOUNDS[subject]["variants"][v]
    return spec["layers"] if isinstance(spec, dict) else spec


# the user's picks (round 36's review, 2026-10-13): the unchosen variants are written to concept/rejected/
CHOSEN = {"burrow-eruption": "a", "canyon-scrape": "b", "ion-beam-loop": "a", "dust-storm-bed": "a"}


def build(subject, v, preview_cache):
    layers = [layer for layer in layers_of(subject, v) if "page" in layer]
    with tempfile.TemporaryDirectory() as tmp:
        found = [(layer["author"], *source(layer, preview_cache, tmp)) for layer in layers]
        paths = [path for _, path, _ in found]
        is_loop = "band_rms" in SOUNDS[subject]
        st = loop(subject, v, paths) if is_loop else one_shot(subject, v, paths)
    used = "; ".join(dict.fromkeys(f"{author} from {how}" for author, _, how in found))
    synths = [layer["synth"] for layer in layers_of(subject, v) if "page" not in layer]
    if synths:
        used += ("; " if used else "") + ", ".join(dict.fromkeys(synths)) + " synthesized"
    name = f"{subject}-r36-{v}"
    peak = -1.0 if is_loop else SOUNDS[subject]["ceiling"]
    out_dir = OUT if CHOSEN[subject] == v else OUT / "rejected"
    out_dir.mkdir(parents=True, exist_ok=True)
    out = write_ogg(out_dir / f"{name}.ogg", st, max_peak_db=peak, tags={"COMMENT": f"{SCRIPT} {name}: {used}"})
    report(out, used)
    if is_loop:
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded "
              f"(the join's step against the steps around it)")


def main(argv):
    preview_cache = Path(os.environ.get("TMPDIR", tempfile.gettempdir())) / "conceptgame-sfx-cache"
    preview_cache.mkdir(parents=True, exist_ok=True)
    for subject, sound in SOUNDS.items():
        for v in sound["variants"]:
            if argv and subject not in argv and f"{subject}-r36-{v}" not in argv:
                continue
            build(subject, v, preview_cache)


if __name__ == "__main__":
    main(sys.argv[1:])
