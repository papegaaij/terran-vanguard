#!/usr/bin/env python3
"""Concept round 97 (M11, step SND-HB, 2026-11-08): the Choir Heart's two heartbeats redone so they carry on a phone or
laptop speaker. The user (round 95, S.1 and S.2): the fight's beat and the hive's heartbeat layer are too weak on a
phone, "probably due to the low frequency": round 84's `heart-beat-r84-b` and `vrell-hive-heartbeat-r84-b` keep nearly
all their energy below ~120 Hz, which a small speaker barely plays. The brief stays (a huge, slow, muffled heartbeat
thud, a real recording, heavy, nothing bubbly, popping, sharp or toy-like); each variant keeps r84-b's sub under it and
adds what a small speaker plays (150-600 Hz and the drum's skin above). Built on sfx_r72.py's layer machinery; every
layer a Freesound sound (CC0 or CC-BY) cut from its public HQ preview (cached in ~/.cache/terran-vanguard/
freesound-previews/):

  heart-beat-r97-a   the timpani's skin kept: round 84's Massive Timpani 4 slowed only to 0.75 (r84-b: 0.5), high-passed
                     at 110 Hz and low-passed at 5 kHz (r84-b: 400 Hz), so its mallet attack and ringing body stay;
                     r84-b's muffled hit (0 dB) and earthquake swell (-7 dB) under it: one deep, round, tonal boom
  heart-beat-r97-b   a lub-dub: two taiko hits (taiko1.aif, CC-BY 3.0) slowed to 0.8 and 0.85, the second 0.36 s after
                     the first and 5 dB softer, lifted 7 dB above 250 Hz; r84-b's muffled hit and swell under the first
  heart-beat-r97-c   r84-b's thud (the timpani slowed to half, muffled; the swell) under a heavy wooden door's thud
                     (kyles, CC0) slowed to 0.75, lifted 8 dB above 250 Hz: a dead, heavy knock in a hollow room over
                     the boom
      each body (what a small speaker plays) also gets a harmonics layer from the low band (the missing fundamental:
      the band under 200 Hz through a tanh, its fundamental filtered out again) and a gentle tanh (drive 3-4); the sub
      stays clean; the whole to its loudness (-13 to -12.5 LUFS; r84-b: -18.1) under a -3 dBFS ceiling by a limiter
  vrell-hive-heartbeat-r97-{a,b,c}   each beat as round 84's hive layer was made (slowed to 0.85, one thud every 4 s,
                     a seamless 16 s loop, -36.5 LUFS: 3 dB under the beds) but lifted 6-8 dB above 250 Hz and
                     low-passed at 2.5 kHz instead of 400 Hz, so the audible band stays
  heart-beat-mix-r97-{a,b,c,r84b}, vrell-hive-heartbeat-mix-r97-{a,b,c,r84b}   20 s review previews at the game's
                     gains: the beat every 3 s over a 20 s stretch of track 20 (assets/music/final-boss.ogg: the music
                     bus 1.0 x LevelMusic's 0.6 against the effects bus 0.5 x HeartCues' CUE_LEVEL 1); the hive layer
                     over the `vrell-hive` bed (assets/sfx/vrell-hive-r90-a.ogg; both at the beds' 0.70 x the effects
                     bus 0.5, the layer at its full level); the previews of a kind share one gain, the loudest peaking
                     at -1 dBFS: the music (the bed) plays at one level in all four and only the beat differs

The measure (--measure, also run after a build): a phone-speaker model, a high-pass at 300 Hz, 24 dB/octave (two
2-pole Butterworth stages), before ffmpeg's EBU R128 meter. Per beat: (1) the beat alone at the game's gain, its
loudest momentary (400 ms) loudness full range and through the model; (2) over the whole of track 20, the beat every
3 s (phases 1-2): at each hit, the beat's loudest momentary loudness in the 0.6 s after it against the music's at that
moment, and the short-term (3 s) loudness of the beat track against the music's over the 3 s from the hit; the median
and the worst tenth of the hits. Per hive layer: against the bed, the same momentary comparison at each thud through
the model, and the integrated loudness full range (the layer must stay under the bed there).

Usage: python3 tools/concept/audio/sfx_r97.py [name ...]     builds (all by default) and measures
       python3 tools/concept/audio/sfx_r97.py --measure      measures the files on disk
Deterministic. Not listened to by Claude: checked by loudness, peaks, the phone model and the loop seams.
"""
import re
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from music_r02 import lp  # noqa: E402
from sfx_r84_beds import level_to, periodic_speed  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from synth import SR, decode, lufs, master, svf, undb, write_ogg, write_wav  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
MUSIC = ROOT / "assets" / "music" / "final-boss.ogg"  # track 20 "Choir Heart"
BED = ROOT / "assets" / "sfx" / "vrell-hive-r90-a.ogg"  # the `vrell-hive` bed
R84_BEAT = OUT / "heart-beat-r84-b.ogg"
R84_HIVE = OUT / "vrell-hive-heartbeat-r84-b.ogg"

# the game's gains (game/src/main/java/vanguard/game): AudioSettings.defaults() music 1.0, effects 0.5; LevelMusic
# MUSIC_VOLUME 0.6, AMBIENCE_VOLUME 0.70 (a bed and its layer at full alike); HeartCues CUE_LEVEL 1
GAIN_MUSIC, GAIN_BEAT, GAIN_BED = 1.0 * 0.6, 0.5 * 1.0, 0.5 * 0.70
BEAT_EVERY, HIVE_EVERY = 3.0, 4.0  # the fight's phases 1-2; the hive layer's thuds
PHONE = "highpass=f=300:poles=2,highpass=f=300:poles=2"

CC0, BY3 = "CC0 1.0", "CC-BY 3.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    573251: ('Massive Timpani 4.wav', 'Sorinious_Genious', CC0, P + '573/573251_12517458-hq.ogg'),
    203281: ('EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV', 'metrostock99', CC0, P + '203/203281_1256155-hq.ogg'),
    95624: ('taiko1.aif', 'whatsanickname4u', BY3, P + '95/95624_734703-hq.ogg'),
    407303: ('door wood heavy open close soft slide thuds roomy.wav', 'kyles', CC0, P + '407/407303_612689-hq.ogg'),
}
M.SOURCES = SOURCES
L = M.L

# r84-b's own layers (sfx_r84.py "heart-beat" b): the sub kept under every variant
R84B_HIT = dict(sound=573251, start=0.0, length=1.6, speed=0.5, fin=0.002, fout=0.8, hp=25.0, lp=400.0)


def sub(hit, swell):
    """r84-b's muffled timpani hit and its earthquake swell at these gains (dB against the body's peak)."""
    return [L(gain=hit, **R84B_HIT), L(203281, 9.0, 1.6, gain=swell, fin=0.35, fout=1.0, hp=20.0, lp=120.0)]


# variant -> (body layers, sub layers, harmonics (gain dB, drive) or None, lift dB, glue drive, target LUFS, what it
# sounds like). The body (what a small speaker plays) gets the harmonics, the lift and a gentle tanh (the glue); the
# sub stays clean under it. Then the whole to the target loudness under a -3 dBFS ceiling by a limiter.
BEATS = {
    "a": ([L(573251, 0.0, 1.6, speed=0.75, fin=0.002, fout=0.8, hp=110.0, lp=5000.0)], sub(0.0, -7.0),
          (-3.0, 8.0), 0.0, 3.0, -13.0,
          "the timpani's skin kept: one deep, round, tonal boom, the mallet's soft attack and the drum's ringing "
          "body left in (what a small speaker plays), the old muffled thud and the earth swell under it, 1.6 s"),
    "b": ([L(95624, 4.72, 0.9, speed=0.8, fin=0.002, fout=0.5, hp=90.0, lp=3000.0),
           L(95624, 6.855, 1.24, gain=-5.0, at=0.36, speed=0.85, fin=0.002, fout=0.7, hp=90.0, lp=2500.0)],
          sub(-2.0, -8.0), (-2.0, 6.0), 7.0, 4.0, -12.5,
          "a lub-dub: two big taiko hits, the second softer a third of a second after the first, the old muffled "
          "thud and the swell under the first: a heartbeat by its rhythm, woody and deep, 1.6 s"),
    "c": ([L(407303, 3.07, 1.2, speed=0.75, fin=0.002, fout=0.5, hp=90.0, lp=2500.0)], sub(-7.0, -12.0),
          (-3.0, 6.0), 8.0, 4.0, -13.0,
          "the old boom with a knock in it: r84-b's muffled thud and swell under a heavy wooden door's thud slowed "
          "to 0.75 (a fist on a huge door in a hollow room): the boom felt, the knock heard, darker and drier than "
          "a, 1.6 s"),
}
# the hive layers: round 84's treatment with the low-pass at HIVE_LP (r84: 400 Hz) after a lift above 250 Hz;
# variant -> (lift dB, LUFS); r84's layer sat 3 dB under the Act 7 beds (-33.5 LUFS) and so do these
HIVE_LP = 2500.0
HIVE = {"a": (6.0, -36.5), "b": (8.0, -36.5), "c": (6.0, -36.5)}


def lifted(x, gain):
    """A shelf: the band above 250 Hz raised by `gain` dB (a parallel 24 dB/octave high-pass)."""
    high = np.array([svf(svf(ch, 250.0, mode="hp"), 250.0, mode="hp") for ch in x])
    return x + high * (undb(gain) - 1)


def harmonics(x, drive):
    """The missing fundamental: the band under 200 Hz driven into a tanh, the fundamental taken out again (high-pass
    150 Hz, 24 dB/octave) and the top softened (low-pass 1.5 kHz): only the low notes' overtones, which a small speaker
    plays and the ear hears as the low note."""
    low = np.array([svf(svf(ch, 200.0), 200.0) for ch in x])
    low = low / (np.max(np.abs(low)) + 1e-12)
    y = np.tanh(drive * low) / np.tanh(drive) - low
    y = np.array([svf(svf(svf(svf(ch, 150.0, mode="hp"), 150.0, mode="hp"), 1500.0), 1500.0) for ch in y])
    return y / (np.max(np.abs(y)) + 1e-12)


def beat(v):
    body_layers, sub_layers, harm, lift, glue, target, _ = BEATS[v]
    n = int(1.6 * SR)
    body = np.pad(M.mix(body_layers), ((0, 0), (0, n)))[:, :n]
    sub = np.pad(M.mix(sub_layers), ((0, 0), (0, n)))[:, :n]
    if harm:
        gain, drive = harm
        body = body + harmonics(body + sub, drive) * np.max(np.abs(body)) * undb(gain)
    if lift:
        body = lifted(body, lift)
    body = body / np.max(np.abs(body))
    if glue:
        body = np.tanh(glue * body) / np.tanh(glue)
    x = body * undb(-1.0) + sub
    k = int(0.3 * SR)  # the tail faded to 0 at 1.6 s
    x[:, -k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    x = x - x.mean(axis=1, keepdims=True)
    x = level_to(x, target)
    over = 20 * np.log10(np.max(np.abs(x))) + 3.0
    print(f"  beat {v}: the limiter takes {max(0.0, over):.1f} dB off the peak", flush=True)
    return master(x, target, -3.0)  # to the target loudness under a -3 dBFS ceiling (round 84's) by a limiter


def hive(x_beat, v):
    """Round 84's hive treatment (sfx_r84_beds.heartbeat) with the low-pass at HIVE_LP instead of 400 Hz, after a lift
    above 250 Hz: slowed to 0.85, one thud every 4 s, a seamless 16 s loop."""
    lift, target = HIVE[v]
    b = periodic_speed(np.pad(lifted(x_beat, lift), ((0, 0), (0, SR))), 0.85)
    b = lp(lp(b, HIVE_LP), HIVE_LP)
    n = 16 * SR
    loop = np.zeros((2, n))
    for k in range(4):
        idx = (k * int(HIVE_EVERY * SR) + np.arange(b.shape[1])) % n
        np.add.at(loop[0], idx, b[0])
        np.add.at(loop[1], idx, b[1])
    loop = loop - loop.mean(axis=1, keepdims=True)
    return level_to(loop, target)


# ---- the game's mix and the phone model

def ebur(x, phone):
    """ffmpeg's EBU R128 meter (optionally after the phone model): per 100 ms frame (the frame at index i ends at
    (i + 1) / 10 s) the momentary and short-term loudness, and the integrated loudness."""
    with tempfile.TemporaryDirectory() as tmp:
        p = Path(tmp) / "m.wav"
        write_wav(p, np.clip(x, -1, 1))
        af = (PHONE + "," if phone else "") + "ebur128=framelog=info"
        r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", str(p), "-af", af, "-f", "null", "-"],
                           capture_output=True, text=True, check=True)
    rows = re.findall(r"t:\s*([\d.]+)\s+TARGET.*?M:\s*(\S+)\s+S:\s*(\S+)", r.stderr)
    m = np.array([float(a) for _, a, _ in rows])
    s = np.array([float(b) for _, _, b in rows])
    integ = float(re.findall(r"I:\s+(\S+) LUFS", r.stderr)[-1])
    return m, s, integ


def place(x, every, n, first):
    out = np.zeros((2, n))
    t = first
    hits = []
    while t * SR < n:
        a = int(t * SR)
        k = min(x.shape[1], n - a)
        out[:, a:a + k] += x[:, :k]
        hits.append(t)
        t += every
    return out, hits


def fight(x_beat, music):
    """The beat every 3 s over the whole of track 20 at the game's gains, through the phone model (and full range):
    per hit the beat's loudest momentary loudness in the 0.6 s after it minus the music's at that frame, and the
    beat track's short-term loudness minus the music's over the 3 s from the hit."""
    n = music.shape[1]
    track, hits = place(x_beat * GAIN_BEAT, BEAT_EVERY, n, 1.5)
    res = {}
    for phone in (False, True):
        bm, bs, _ = ebur(track, phone)
        mm, ms, _ = ebur(music * GAIN_MUSIC, phone)
        dm, ds = [], []
        for t in hits[:-1]:
            i = int(round(t * 10))
            j = i + int(np.argmax(bm[i:i + 6]))
            dm.append(bm[j] - mm[j])
            e = i + 29  # the 3 s window from the hit
            if e < len(bs):
                ds.append(bs[e] - ms[e])
        res[phone] = (np.median(dm), np.percentile(dm, 10), np.median(ds), np.percentile(ds, 10))
    return res


def alone(x_beat):
    out = {}
    for phone in (False, True):
        m, _, _ = ebur(np.pad(x_beat * GAIN_BEAT, ((0, 0), (0, SR))), phone)
        out[phone] = float(np.nanmax(m))
    return out


def hive_vs_bed(layer, bed):
    """The layer against the bed (both at the beds' gain, the layer at full): through the phone model the layer's
    loudest momentary loudness after each thud minus the bed's at that frame (median); full range the integrated
    loudness of each."""
    n = 32 * SR
    lay = np.tile(layer, (1, 2))[:, :n] * GAIN_BED
    bd = np.tile(bed, (1, int(np.ceil(n / bed.shape[1]))))[:, :n] * GAIN_BED
    lm, _, li = ebur(lay, True)
    bm, _, _ = ebur(bd, True)
    dm = []
    for k in range(1, 8):  # skip the first thud (the meter's start)
        i = int(k * HIVE_EVERY * 10)
        j = i + int(np.argmax(lm[i:i + 8]))
        dm.append(lm[j] - bm[j])
    _, _, lf = ebur(lay, False)
    _, _, bf = ebur(bd, False)
    _, _, bp = ebur(bd, True)
    return np.median(dm), lf - bf, li - bp


# ---- the review previews

def music_window(music):
    """The 20 s of track 20 (whole seconds, from 10 s on) whose mean short-term loudness is the median of all such
    windows: a typical stretch of the fight."""
    _, s, _ = ebur(music, False)
    starts = list(range(10, int(music.shape[1] / SR) - 21))
    means = [np.mean(np.clip(s[(a + 3) * 10:(a + 20) * 10], -70, 0)) for a in starts]
    order = np.argsort(means)
    return starts[order[len(order) // 2]]


def fade_end(x, seconds=0.5):
    k = int(seconds * SR)
    x = x.copy()
    x[:, -k:] *= np.linspace(1, 0, k)
    return x


def mix_preview(x_beat, music, start):
    n = 20 * SR
    seg = music[:, start * SR:start * SR + n] * GAIN_MUSIC
    track, _ = place(x_beat * GAIN_BEAT, BEAT_EVERY, n, 1.0)
    return fade_end(seg + track)


def hive_preview(layer, bed):
    n = 20 * SR
    lay = np.tile(layer, (1, 2))[:, :n]
    bd = np.tile(bed, (1, 2))[:, :n]
    return fade_end((lay + bd) * GAIN_BED)


def write_previews(prefix, mixes):
    """The previews of one kind with one gain: the loudest of them peaks at -1 dBFS, so the music (the bed) plays at
    the same level in each and only the beat (the layer) differs, as in the game."""
    gain = undb(-1.0) / max(np.max(np.abs(x)) for x in mixes.values())
    for name, x in mixes.items():
        write_ogg(OUT / f"{prefix}-mix-r97-{name}.ogg", x * gain, max_peak_db=-1.0)
    print(f"{prefix} previews: x{gain:.2f} ({20 * np.log10(gain):+.1f} dB)", flush=True)


# ---- main

def measure(beats, hives, music, bed):
    print("phone model = high-pass 300 Hz, 24 dB/oct; LU differences; 'p10' = the worst tenth of the hits")
    for name, x in beats.items():
        a = alone(x)
        f = fight(x, music)
        print(f"beat {name:5s} alone max-M {a[False]:6.1f} LUFS full, {a[True]:6.1f} phone | over track 20: "
              f"hit M-diff full {f[False][0]:+5.1f} (p10 {f[False][1]:+5.1f}), phone {f[True][0]:+5.1f} "
              f"(p10 {f[True][1]:+5.1f}); S-diff full {f[False][2]:+5.1f}, phone {f[True][2]:+5.1f} "
              f"(p10 {f[True][3]:+5.1f})", flush=True)
    for name, x in hives.items():
        dm, full, phone_int = hive_vs_bed(x, bed)
        print(f"hive {name:5s} {lufs(x)[0]:6.1f} LUFS, seam {seam_db(x):+5.1f} dB | thud M over the bed, phone "
              f"{dm:+5.1f} LU | integrated vs the bed: full {full:+5.1f}, phone {phone_int:+5.1f}", flush=True)


def main(argv):
    only_measure = "--measure" in argv
    names = [a for a in argv if not a.startswith("--")] or list(BEATS)
    if not only_measure:
        for v in names:
            x = beat(v)
            tag = {"COMMENT": f"tools/concept/audio/sfx_r97.py heart-beat-r97-{v}: {BEATS[v][6]}"}
            path = write_ogg(OUT / f"heart-beat-r97-{v}.ogg", x, max_peak_db=-3.0, tags=tag)
            tag = {"COMMENT": f"tools/concept/audio/sfx_r97.py vrell-hive-heartbeat-r97-{v}: heart-beat-r97-{v} as "
                              f"the hive's layer (round 84's treatment, the audible band kept)"}
            write_ogg(OUT / f"vrell-hive-heartbeat-r97-{v}.ogg", hive(decode(path), v), max_peak_db=-3.0, tags=tag)
    music = decode(MUSIC)
    bed = decode(BED)
    beats = {"r84b": decode(R84_BEAT)}
    hives = {"r84b": decode(R84_HIVE)}
    for v in BEATS:
        beats[v] = decode(OUT / f"heart-beat-r97-{v}.ogg")
        hives[v] = decode(OUT / f"vrell-hive-heartbeat-r97-{v}.ogg")[:, :16 * SR]
        print(f"r97-{v}: beat {lufs(beats[v])[0]:.1f} LUFS, hive {lufs(hives[v])[0]:.1f} LUFS", flush=True)
    if not only_measure:
        start = music_window(music)
        print(f"track 20 preview window: {start}-{start + 20} s", flush=True)
        write_previews("heart-beat", {k: mix_preview(x, music, start) for k, x in beats.items()})
        write_previews("vrell-hive-heartbeat", {k: hive_preview(x, bed) for k, x in hives.items()})
    measure(beats, hives, music, bed)


if __name__ == "__main__":
    main(sys.argv[1:])
