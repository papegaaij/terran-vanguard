#!/usr/bin/env python3
"""Concept round 38 (M7 part B, Level 15 "Olympus Descent", step AUD-B): a/b proposals for the Burrower's
bulge and dive, Level 15's entry roar and the Mars dust wind (`mars-dust`), from the cached Freesound
originals (all credited before: CC0, the dobroide howl and smidoid's cracking CC-BY) plus synthesized
layers, in the round-36 method of sfx_r36b.py (whose helpers this imports). Pending the user's review.

Outputs (design/audio/sfx/concept/, the unchosen variants in concept/rejected/ since the round closed
(CHOSEN, the user's picks of 2026-10-14); OGG Vorbis q6, 44.1 kHz stereo; each one-shot starts at its event):

  burrow-bulge-r38-b.ogg       b "crack layer": the Burrower's 1.0 s bulge tell as a layer **over** the chosen
                               eruption a (which already opens with the tell: its first 1.0 s is a ground
                               rumble swelling 14 dB, a pressure tone and a sand hiss, the impact at 1.0 s):
                               sand and crust cracking, coming faster and louder towards the break
                               (smidoid's "cracking.wav", CC-BY 4.0, slowed, and synthesized cracks of 4-18 ms
                               band noise), a little gritty trickle, no rumble of its own (no doubling); 1.0 s.
                               a is no file: the eruption a alone, started at the bulge (its own swell is the
                               tell); see burrow-bulge-mix-r38-b for b as the game would mix it
  burrow-bulge-mix-r38-b.ogg   listening aid: the eruption a and the bulge b started together, as the game
                               would play b (not loaded by the game)
  burrow-dive-r38-a.ogg        a "sand pour": a falling sand hiss as it dives (0.4 s): the sand pour ("SAND
                               POUR.wav", nicoproson, CC0) under a low-pass falling 9 -> 1.6 kHz, small
                               stones dropping in ("Bricks/Stones/Rocks/Gravel Falling", iwanPlays, CC0,
                               high-passed) and a soft synthesized sand slump (a 70-250 Hz noise band); 0.6 s
  burrow-dive-r38-b.ogg        b "grain slide", synthesized: a dense rain of short noise grains whose band
                               falls 5 -> 1 kHz and whose density thins (a hiss sliding down, never single
                               clicks), over the same slump; 0.55 s
  entry-roar-r38-a.ogg         a "recorded roar": a high thin wind roar over the hull for Level 15's
                               descent through the thin air (sections 1-2): the furious wind howl
                               ("20101121.wind.howl.furious.01.wav", dobroide, CC-BY 4.0; a later stretch
                               than the dust-storm bed's, high-passed at 650 Hz so its body is gone and the
                               thin roar remains) over a synthesized hull buffet (30-180 Hz noise riding
                               slow gusts) 10 dB under; a seamless 12.0 s LOOP
  entry-roar-r38-b.ogg         b "synthesized roar": a thin air roar (circular noise 700 Hz-6 kHz), a
                               resonant whistle band wandering 1.6-2.6 kHz (the air tearing past the hull)
                               and the same buffet; a seamless 12.0 s LOOP
  mars-dust-r38-a.ogg          a "recorded dust wind" (`mars-dust`: Level 15 from section 3, Levels 20-21):
                               the sandstorm recording ("G56-22-Sandstorm.wav", craigsmith, CC0; another
                               stretch than the storm bed's, band 300 Hz-6 kHz) riding slow gusts (1 and 3
                               cycles a loop), over the low moan of a cold wind ("Wind__Artic__Cold.wav",
                               cobratronik, CC0, 650 Hz low-pass) 6 dB under: thinner and calmer than the
                               storm bed, without its furious howl; a seamless 16.0 s LOOP
  mars-dust-r38-b.ogg          b "synthesized dust wind": a thin wind (circular noise 150 Hz-3 kHz, pinkish),
                               a faint whistle band wandering 900-1400 Hz and a grit hiss (3-8 kHz) that
                               rises with the gusts (1, 2 and 5 cycles a loop); a seamless 16.0 s LOOP

Levels (as sfx_r36b.py): the one-shots on the loudest 100 ms of the 200 Hz-5 kHz band, limited at a
ceiling: the bulge layer -25 dB (under the eruption's -19: a layer, not a second event), the dive -22 dB
(a P2 cue under the eruption's -19 and the root burst's -17, with the ship sinking's -20 nearest). The
loops by the whole loop's 200 Hz-5 kHz RMS, one gain, no limiter: both ambiences at -36 dB like every
ambience loop (the entry roar is the ambience of sections 1-2; the game ramps it up as the air thickens
and crossfades it into `mars-dust` at section 3). No bubble, popping or squelch layers (user rule): the
grains and cracks are noise bursts, no tone falls, no low "plop" under the dive.

Usage: python3 tools/concept/audio/sfx_r38.py [subject-or-file ...]   e.g. mars-dust burrow-dive-r38-b
Deterministic. Needs the cached Freesound originals (tools/concept/audio/freesound_fetch.py --download);
every source used here is cached already (credited in earlier rounds).
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
from sfx_r32 import loudest_db, recorded, report  # noqa: E402
from sfx_r36 import cracks, edge, seam_db, unit  # noqa: E402
from sfx_r36b import CC0, BY4, HOWL, SAND_POUR, SANDSTORM, churn, circular_noise  # noqa: E402
from synth import SR, decode, noise, pan, ramp, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r38.py"
ERUPTION = OUT / "burrow-eruption-r36-a.ogg"   # the chosen eruption (round 36, S.2 = a)

# all credited before (CREDITS.md)
CRACKING = dict(**fs("smidoid", 49139, 49, 485393), title="cracking.wav", author="smidoid", licence=BY4)
STONES = dict(**fs("iwanPlays", 567249, 567, 7108319), title="Bricks/Stones/Rocks/Gravel Falling",
              author="iwanPlays", licence=CC0)
WIND = dict(**fs("cobratronik", 117136, 117, 732072), title="Wind__Artic__Cold.wav", author="cobratronik",
            licence=CC0)

ROAR = 12.0    # loop lengths (s): every periodic synthesized part completes whole cycles in its loop
DUST = 16.0

# As sfx_r36b.py's SOUNDS: a one-shot's `level` (dB, the loudest 100 ms of the 200 Hz-5 kHz band) and
# `ceiling` (dBFS), a loop's `band_rms` and length; per variant its layers (a recorded layer: source, cut
# (import_sfx.py's process keys, offsets after its leading-silence trim), optional speed, `sweep` (a
# low-pass falling from..to Hz over the layer), pan; a looped recorded layer: `loop` = (start, length,
# cross-fade), optional high/low-pass before the cut and `gust` = [(cycles a loop, depth, phase)]; a
# synthesized layer: `synth` and its parameters; each enters at `at`, the others `gain` dB from the first,
# measured on the loudest 100 ms).
SOUNDS = {
    "burrow-bulge": dict(level=-25.0, ceiling=-6.0, variants={
        "b": [dict(synth="cracks", at=0.0, length=1.0, count=30, seed=3801),
              dict(**CRACKING, cut=dict(offset=0.0, length=0.62, fade=0.25, fadein=0.01, highpass=250), at=0.5,
                   gain=1.0, speed=0.8, pan=-0.25),
              dict(synth="trickle", at=0.15, gain=-8.0, length=0.85, seed=3802)]}),
    "burrow-dive": dict(level=-22.0, ceiling=-4.0, variants={
        "a": [dict(**SAND_POUR, cut=dict(offset=11.6, length=0.6, fade=0.32, fadein=0.012, highpass=350), at=0.0,
                   sweep=(9000.0, 1600.0)),
              dict(**STONES, cut=dict(offset=0.45, length=0.42, fade=0.25, fadein=0.01, highpass=300), at=0.05,
                   gain=-7.0, pan=0.3),
              dict(synth="churn", at=0.0, length=0.5, rise=0.08, seed=3803, gain=-9.0)],
        "b": [dict(synth="slide", at=0.0, length=0.55, f=(5000.0, 1000.0), seed=3804),
              dict(synth="churn", at=0.0, length=0.5, rise=0.08, seed=3805, gain=-9.0)]}),
    "entry-roar": dict(band_rms=-36.0, variants={
        "a": dict(length=ROAR, layers=[
            dict(**HOWL, loop=(150.0, ROAR, 1.5), highpass=650.0, at=0.0),
            dict(synth="buffet", gain=-10.0, seed=3806)]),
        "b": dict(length=ROAR, layers=[
            dict(synth="thin-roar", seed=3807),
            dict(synth="buffet", gain=-10.0, seed=3808)])}),
    "mars-dust": dict(band_rms=-36.0, variants={
        "a": dict(length=DUST, layers=[
            dict(**SANDSTORM, loop=(44.0, DUST, 1.5), highpass=300.0, lowpass=6000.0,
                 gust=[(1, 0.25, 0.4), (3, 0.12, 1.9)], at=0.0),
            dict(**WIND, loop=(30.0, DUST, 1.5), lowpass=650.0, gain=-6.0)]),
        "b": dict(length=DUST, layers=[
            dict(synth="dust-wind", seed=3809)])}),
}


# =========================================================================== synthesized layers (one-shots)

def trickle(p):
    """A gritty trickle under the cracks: fine noise (2.5-7 kHz), swelling over the layer with a slow
    random flutter (a control signal low-passed at 25 Hz), cut short at its end (the break follows)."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    flutter = svf(rng.normal(0, 1, n), np.full(n, 25.0))
    flutter = 0.6 + 0.4 * flutter / (np.abs(flutter).max() + 1e-12)
    chans = [unit(svf(svf(noise(n, rng), np.full(n, 2500.0), mode="hp"), np.full(n, 7000.0))) for _ in range(2)]
    env = (t / p["length"]) ** 1.5 * flutter * np.clip((p["length"] - t) / 0.03, 0, 1)
    return edge(np.array(chans) * env, 0.01, 0.02)


def slide(p):
    """A grain slide: 2-6 ms noise grains, band-passed at a centre falling f[0] -> f[1] Hz, coming
    900 -> 150 a second (dense enough to fuse into a hiss), fading over the layer, across the field."""
    rng = np.random.default_rng(p["seed"])
    L = p["length"]
    n = int(L * SR)
    out = np.zeros((2, n + int(0.01 * SR)))
    k = 0.0
    while True:
        u = k / L
        k += rng.exponential(1.0 / (900.0 - 750.0 * u))
        if k >= L:
            break
        u = k / L
        m = int(rng.uniform(0.002, 0.006) * SR)
        a = int(k * SR)
        centre = p["f"][0] * (p["f"][1] / p["f"][0]) ** u * rng.uniform(0.8, 1.25)
        x = unit(svf(noise(m, rng), np.full(m, centre), q=1.0, mode="bp")) * np.hanning(m)
        out[:, a:a + m] += pan(x * rng.uniform(0.3, 1.0) * (1 - u) ** 1.3, rng.uniform(-0.5, 0.5))
    return edge(out[:, :n], 0.02, 0.04)


# =========================================================================== synthesized layers (loops)
# Periodic in the loop length L: whole cycles a loop, circular noise; a time-varying filter runs over
# three loop lengths and keeps the middle one (sfx_r36b.py's storm()).

def gusts(L, n, terms, ph=0.0):
    t = np.arange(n) / SR
    return 1.0 + sum(depth * np.sin(2 * np.pi * cycles * t / L + phase + ph) for cycles, depth, phase in terms)


def buffet(p, L):
    """The hull buffeting: low noise (30-180 Hz, peaking at 70 Hz) riding slow gusts (2, 5 and 9 cycles a
    loop), the two channels apart."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    return np.array([circular_noise(n, rng, 30.0, 180.0, 70.0, 60.0)
                     * gusts(L, n, [(2, 0.3, 0.0), (5, 0.18, 1.1), (9, 0.1, 2.3)], ch * 0.9) for ch in range(2)])


def wander(n, rng, L, lo, hi, centre, q, terms, ph):
    """Circular noise (lo-hi Hz) through a band-pass whose centre wanders by `terms` round `centre`."""
    t = np.arange(n) / SR
    src = np.tile(circular_noise(n, rng, lo, hi), 3)
    c = np.tile(centre + sum(a * np.sin(2 * np.pi * k * t / L + phase + ph) for k, a, phase in terms), 3)
    return unit(svf(src, c, q=q, mode="bp")[n:2 * n])


def thin_roar(p, L):
    """Thin air tearing past the hull: a roar band (700 Hz-6 kHz, rising gently towards 2 kHz) and a
    resonant whistle wandering 1.6-2.6 kHz, both riding gusts (1, 3 and 7 cycles a loop)."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for ch in range(2):
        g = gusts(L, n, [(1, 0.15, 0.3), (3, 0.12, 1.4), (7, 0.07, 2.8)], ch * 0.8)
        roar = circular_noise(n, rng, 700.0, 6000.0, 2000.0, 2600.0)
        whistle = wander(n, rng, L, 900.0, 4000.0, 2100.0, 6.0, [(2, 300.0, 0.0), (5, 150.0, 1.7)], ch * 1.1)
        out[ch] = 0.8 * roar * g + 0.35 * whistle * g ** 2
    return out


def dust_wind(p, L):
    """A thin Martian wind: pinkish noise (150 Hz-3 kHz, peaking at 500 Hz), a faint whistle wandering
    900-1400 Hz and a grit hiss (3-8 kHz) that rises with the gusts (1, 2 and 5 cycles a loop)."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for ch in range(2):
        g = gusts(L, n, [(1, 0.22, 0.2), (2, 0.12, 1.5), (5, 0.06, 2.6)], ch * 0.7)
        body = circular_noise(n, rng, 150.0, 3000.0, 500.0, 700.0)
        whistle = wander(n, rng, L, 300.0, 3000.0, 1150.0, 5.0, [(1, 160.0, 0.5), (3, 90.0, 2.2)], ch * 0.9)
        grit = circular_noise(n, rng, 3000.0, 8000.0)
        out[ch] = 0.7 * body * g + 0.18 * whistle * g ** 1.5 + 0.22 * grit * g ** 2.5
    return out


# a variant levelled under its subject's level: the grain slide's energy lies higher than the sand pour's
# (at the same band level it measured 5 LU louder, -16.8 against -22.0 LUFS), so it is set 4 dB lower
LEVEL = {("burrow-dive", "b"): -26.0}

SYNTHS = {"cracks": cracks, "trickle": trickle, "slide": slide, "churn": churn}
LOOP_SYNTHS = {"buffet": buffet, "thin-roar": thin_roar, "dust-wind": dust_wind}


# =========================================================================== treatment

def sweep(st, f0, f1):
    """A low-pass falling f0 -> f1 Hz (exponentially) over the layer."""
    n = st.shape[1]
    cut = f0 * (f1 / f0) ** np.linspace(0, 1, n)
    return np.array([svf(svf(ch, cut), cut) for ch in st])


def one_shot(subject, v, paths):
    sound = SOUNDS[subject]
    files = iter(paths)
    parts = []
    for layer in sound["variants"][v]:
        if "page" in layer:
            st = recorded(layer, next(files))
            if "sweep" in layer:
                st = sweep(st, *layer["sweep"])
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
    return level(out, LEVEL.get((subject, v), sound["level"]), sound["ceiling"])


def loop(subject, v, paths):
    """sfx_r36b.py's loop(), with a periodic gust envelope on a recorded layer."""
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
            if "gust" in layer:
                x = x * np.array([gusts(L, n, layer["gust"], ch * 0.8) for ch in range(2)])
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
    name = f"{subject}-r38-{v}"
    peak = -1.0 if is_loop else SOUNDS[subject]["ceiling"]
    out = write_ogg(out_path(subject, v), st, max_peak_db=peak, tags={"COMMENT": f"{SCRIPT} {name}: {used}"})
    report(out, used)
    if is_loop:
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded "
              f"(the join's step against the steps around it)")
    if subject == "burrow-bulge":
        mix_aid(out)


# the user's picks (round 38's review, S.1-S.4, 2026-10-14): the unchosen variants go to concept/rejected/
CHOSEN = {"burrow-bulge": "b", "burrow-dive": "b", "entry-roar": "a", "mars-dust": "a"}


def out_path(subject, v):
    return (OUT if CHOSEN[subject] == v else OUT / "rejected") / f"{subject}-r38-{v}.ogg"


def mix_aid(bulge):
    """The eruption a and the bulge b started together (the game's mix of b), for listening only."""
    e, b = decode(ERUPTION), decode(bulge)
    out = np.zeros((2, max(e.shape[1], b.shape[1])))
    out[:, :e.shape[1]] += e
    out[:, :b.shape[1]] += b
    name = "burrow-bulge-mix-r38-b"
    path = write_ogg(OUT / f"{name}.ogg", out, max_peak_db=None,
                     tags={"COMMENT": f"{SCRIPT} {name}: burrow-eruption-r36-a + burrow-bulge-r38-b, summed"})
    report(path, "listening aid: the chosen eruption a + the bulge b")


def main(argv):
    preview_cache = Path(os.environ.get("TMPDIR", tempfile.gettempdir())) / "conceptgame-sfx-cache"
    preview_cache.mkdir(parents=True, exist_ok=True)
    for subject, sound in SOUNDS.items():
        for v in sound["variants"]:
            if argv and subject not in argv and f"{subject}-r38-{v}" not in argv:
                continue
            build(subject, v, preview_cache)


if __name__ == "__main__":
    main(sys.argv[1:])
