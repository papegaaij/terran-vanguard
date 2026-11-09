#!/usr/bin/env python3
"""Concept round 98 (M11, step SND-HB2, 2026-11-08): the Choir Heart's two heartbeats from real recorded hearts. Round
97's verdict (the user): all six rejected, "these sound like beats on a drum" (S.1) and "doesn't sound like a heart at
all" (S.2); round 84's slowed timpani in the game now is the same family. So no drum, timpani, taiko, door or knock:
each beat here is a real heart's lub-dub (the "lub" longer and deeper, the "dub" shorter, softer and a little brighter),
cut from a Freesound recording's HQ preview (CC0 or CC-BY, cached in ~/.cache/terran-vanguard/freesound-previews/),
slowed for a giant heart, the lub and the dub placed 0.7 s apart (a slow heart's spacing: about a quarter of the 3 s
beat, a third of phase 3's 2 s), and made to carry on a phone (round 95) without losing the heart:

  heart-beat-r98-a   a stethoscope heartbeat (Benboncan's "Heartbeat Mono.wav", CC-BY 4.0): one lub-dub, the lub
                     slowed to 0.7, the dub to 0.78 (its sub thinned: the brighter valve click), big and close; its
                     own deep thump kept as the sub
  heart-beat-r98-b   an ultrasound heartbeat (a foetal doppler recording, SignatureSoundsOrg, CC0): two pulses of
                     rushing blood slowed to 0.5 (the second to 0.55, softer and shorter), a soft stethoscope thump
                     under them (shapingwaves' "Clean At Heart", CC-BY 4.0, low-passed: the sub)
  heart-beat-r98-c   a contact-microphone heartbeat (Schacht, CC-BY 4.0: the heart moving inside the body), the lub
                     slowed to 0.55, the dub to 0.65, with a wet organic layer on the lub: a real goat's heart
                     squeezed (magnuswaker, CC0), slowed and darkened to a soft squelch, the pulse pushing through
                     tissue
      each: split at 120 Hz; the low band (the flesh's thump, the sub) stays clean; the rest gets a harmonics layer
      from the low band (sfx_r97.harmonics: the missing fundamental), a low-mid lift (b: 150-400 Hz raised; a and c:
      220-600 Hz raised, 120-220 Hz lowered, where a phone plays nothing but the peak is spent), sfx_r97's lift above
      250 Hz, a gentle tanh (the glue); the low band back under it (a -4 dB, c -3 dB); the whole to its loudness under
      a -3 dBFS ceiling by a limiter
  vrell-hive-heartbeat-r98-{a,b,c}   each beat as round 84's hive layer (slowed to 0.85, one lub-dub every 4 s, a
                     seamless 16 s loop, -36.5 LUFS: 3 dB under the beds), low-passed at 2.5 kHz, a mid band
                     and the band above 250 Hz lifted, so the audible band stays (sfx_r97's hive treatment)
  heart-beat-mix-r98-{a,b,c,r84b}, vrell-hive-heartbeat-mix-r98-{a,b,c,r84b}   20 s review previews at the game's gains
                     (sfx_r97's: the beat every 3 s over track 20, the layer over the `vrell-hive` bed; the previews of
                     a kind share one gain, the loudest peaking at -1 dBFS)

The measure (also run after a build): sfx_r97's phone-speaker model (a 300 Hz high-pass, 24 dB/octave, before ffmpeg's
EBU R128 meter) at the game's gains, the beat alone and over track 20, the hive layer over the bed. --compare: each beat
against its source recording's lub-dub (raw, and slowed only): the lub and dub peaks, their -10 dB lengths and
brightness (spectral centroid), the spacing, and the spectral tilt (dB per octave, 40 Hz-2.5 kHz).

Usage: python3 tools/concept/audio/sfx_r98.py [name ...]     builds (all by default), measures and compares
       python3 tools/concept/audio/sfx_r98.py --measure      measures and compares the files on disk
Deterministic. Not listened to by Claude: checked by loudness, peaks, the phone model, the loop seams and the source
comparison.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r97 as R  # noqa: E402  (its measures, previews and treatments)
import sfx_r72 as M  # noqa: E402
from music_r02 import lp  # noqa: E402
from sfx_r84_beds import level_to, periodic_speed  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from synth import SR, decode, lufs, master, svf, undb, write_ogg  # noqa: E402

OUT = R.OUT
CC0, BY4 = "CC0 1.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    62912: ('Heartbeat Mono.wav', 'Benboncan', BY4, P + '62/62912_634166-hq.ogg'),
    362374: ('SW002_Stethoscope_Chest_Heart_Heartbeat_Clean_At_Heart.wav', 'shapingwaves', BY4,
             P + '362/362374_6667285-hq.ogg'),
    871122: ('Foetal_Heartbeat_Through_Doppler_Machine', 'SignatureSoundsOrg', CC0, P + '871/871122_19205925-hq.ogg'),
    793474: ('Heartbeat recorded with Contact Microphone', 'Schacht', BY4, P + '793/793474_4092359-hq.ogg'),
    641046: ('Gore Impact - "LOT OF HEART"', 'magnuswaker', CC0, P + '641/641046_11537497-hq.ogg'),
}
M.SOURCES = SOURCES
L = M.L
LENGTH = 1.8  # s: the dub's tail ends before phase 3's next beat (2 s)
DUB_AT = 0.7  # s: the dub's layer after the lub's

# variant -> (layers, (harmonics gain dB, drive), (EQ bands [(from Hz, to Hz, dB), ...], lift above 250 Hz dB), glue
# drive, the low band's gain dB (the sub under the body), target LUFS, the source cycle for --compare (sound, lub peak
# s, dub peak s), what it sounds like). Each layer is peak-normalised; the dub's gain is set so that after the
# treatment (which lifts the brighter dub more) the dub stays 1-3 dB under the lub, as in the recordings.
BEATS = {
    "a": ([L(62912, 6.37, 0.45, speed=0.7, fin=0.02, fout=0.28, hp=25.0, lp=4000.0),           # lub (peak 6.47 s)
           L(62912, 6.74, 0.36, gain=-10.0, at=DUB_AT, speed=0.78, fin=0.02, fout=0.25, hp=90.0, lp=5000.0)],  # dub
          (0.0, 8.0), ([(120.0, 220.0, -6.0), (220.0, 600.0, 10.0)], 10.0), 3.0, -4.0, -13.5, (62912, 6.47, 6.82),
          "a stethoscope heartbeat slowed: a deep, soft lub and a shorter, slightly brighter dub, close and dry, the "
          "chest's thump under both, 1.8 s"),
    "b": ([L(871122, 2.42, 0.85, speed=0.5, fin=0.03, fout=0.4, hp=60.0, lp=3500.0),             # lub whoosh
           L(871122, 2.92, 0.6, gain=-3.0, at=DUB_AT, speed=0.55, fin=0.03, fout=0.35, hp=60.0, lp=4500.0),  # dub
           L(362374, 5.42, 0.5, gain=-2.0, speed=0.5, fin=0.02, fout=0.3, hp=20.0, lp=150.0),     # thump under lub
           L(362374, 5.78, 0.45, gain=-6.0, at=DUB_AT, speed=0.5, fin=0.02, fout=0.3, hp=20.0, lp=150.0)],
          (-6.0, 5.0), ([(150.0, 400.0, 4.0)], 2.0), 2.0, 0.0, -12.0, (871122, 2.56, 3.06),
          "an ultrasound heartbeat slowed: two whooshing pulses of rushing blood, the second shorter and softer, a "
          "soft deep thump under them, 1.8 s"),
    "c": ([L(793474, 6.78, 0.45, speed=0.55, fin=0.02, fout=0.3, hp=25.0, lp=4000.0),            # lub (peak 6.86 s)
           L(793474, 7.03, 0.4, gain=-11.0, at=DUB_AT, speed=0.65, fin=0.02, fout=0.28, hp=90.0, lp=5000.0),  # dub
           L(641046, 0.0, 0.5, gain=-14.0, at=0.03, speed=0.6, fin=0.04, fout=0.3, hp=100.0, lp=900.0)],  # squelch
          (-2.0, 6.0), ([(120.0, 220.0, -6.0), (220.0, 600.0, 10.0)], 10.0), 3.0, -3.0, -13.5, (793474, 6.86, 7.10),
          "a heartbeat through the body (a contact microphone on the chest) slowed, a soft wet squelch in the lub: "
          "the pulse pushing through tissue, longer, rounder and wetter than a, 1.8 s"),
}
# the hive layers: round 84's treatment with sfx_r97's 2.5 kHz low-pass; variant -> (band from Hz, to Hz, its lift dB,
# lift above 250 Hz dB, LUFS)
HIVE_LP = 2500.0
HIVE = {"a": (300.0, 900.0, 8.0, 6.0, -36.5), "b": (150.0, 400.0, 4.0, 5.0, -36.5),
        "c": (150.0, 400.0, 6.0, 11.0, -36.5)}


def band(x, lo, hi):
    return np.array([svf(svf(svf(svf(ch, lo, mode="hp"), lo, mode="hp"), hi), hi) for ch in x])


def lowmid(x, gain, lo=150.0, hi=400.0):
    """The low-mid band (150-400 Hz unless given) raised by `gain` dB (a parallel band-pass, 24 dB/octave each
    side)."""
    return x + band(x, lo, hi) * (undb(gain) - 1)


def split(x, f=120.0):
    low = np.array([svf(svf(ch, f), f) for ch in x])
    return low, x - low


def beat(v):
    layers, harm, lift, glue, sub_db, target = BEATS[v][:6]
    n = int(LENGTH * SR)
    x = np.pad(M.mix(layers), ((0, 0), (0, n)))[:, :n]
    low, body = split(x)
    gain, drive = harm
    body = body + R.harmonics(x, drive) * np.max(np.abs(body)) * undb(gain)
    bands, shelf = lift
    body = body + sum(band(body, lo, hi) * (undb(g) - 1) for lo, hi, g in bands)
    if shelf:
        body = R.lifted(body, shelf)
    peak = np.max(np.abs(body))
    body = np.tanh(glue * body / peak) / np.tanh(glue) * peak
    x = body + low * undb(sub_db)
    k = int(0.3 * SR)  # the tail faded to 0 at 1.8 s
    x[:, -k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    x = x - x.mean(axis=1, keepdims=True)
    x = level_to(x, target)
    over = 20 * np.log10(np.max(np.abs(x))) + 3.0
    print(f"  beat {v}: the limiter takes {max(0.0, over):.1f} dB off the peak", flush=True)
    return master(x, target, -3.0)


def hive(x_beat, v):
    """Round 84's hive treatment with sfx_r97's 2.5 kHz low-pass, the low-mid band lifted: slowed to 0.85, one lub-dub
    every 4 s, a seamless 16 s loop."""
    lo, hi, mid, shelf, target = HIVE[v]
    b = periodic_speed(np.pad(R.lifted(lowmid(x_beat, mid, lo, hi), shelf), ((0, 0), (0, SR))), 0.85)
    b = lp(lp(b, HIVE_LP), HIVE_LP)
    n = 16 * SR
    loop = np.zeros((2, n))
    for k in range(4):
        idx = (k * int(R.HIVE_EVERY * SR) + np.arange(b.shape[1])) % n
        np.add.at(loop[0], idx, b[0])
        np.add.at(loop[1], idx, b[1])
    loop = loop - loop.mean(axis=1, keepdims=True)
    return level_to(loop, target)


def write_previews(prefix, mixes):
    """sfx_r97.write_previews with round 98's names: one gain per kind, the loudest peaking at -1 dBFS."""
    gain = undb(-1.0) / max(np.max(np.abs(x)) for x in mixes.values())
    for name, x in mixes.items():
        write_ogg(OUT / f"{prefix}-mix-r98-{name}.ogg", x * gain, max_peak_db=-1.0)
    print(f"{prefix} previews: x{gain:.2f} ({20 * np.log10(gain):+.1f} dB)", flush=True)


# ---- the comparison with the source recordings

def envelope(x, hop=0.005):
    m = x.mean(axis=0)
    m = svf(svf(m, 20.0, mode="hp"), 2500.0)
    h = int(hop * SR)
    n = len(m) // h
    e = (m[:n * h].reshape(n, h) ** 2).mean(axis=1)
    k = 6  # 30 ms smoothing
    return np.sqrt(np.convolve(e, np.ones(k) / k, mode="same")), hop


def two_beats(x, min_gap):
    """The lub and the dub: the two loudest envelope peaks (each the loudest within `min_gap` s either side), in
    time order."""
    e, hop = envelope(x)
    w = int(min_gap / hop)
    tops = [i for i in range(len(e)) if e[i] > 0 and e[i] >= e[max(0, i - w):i + w + 1].max()]
    i, j = sorted(sorted(tops, key=lambda k: -e[k])[:2])
    return e, hop, i, j


def width(e, i, hop):
    """The -10 dB length of the envelope peak at i."""
    thr = e[i] * undb(-10.0)
    a = i
    while a > 0 and e[a - 1] > thr:
        a -= 1
    b = i
    while b < len(e) - 1 and e[b + 1] > thr:
        b += 1
    return (b - a + 1) * hop


def centroid(x, t, w):
    m = x.mean(axis=0)
    a = max(0, int((t - 0.25 * w) * SR))
    seg = m[a:a + int(w * SR)]
    sp = np.abs(np.fft.rfft(seg * np.hanning(len(seg)))) ** 2
    f = np.fft.rfftfreq(len(seg), 1 / SR)
    keep = (f > 20) & (f < 5000)
    return float((f[keep] * sp[keep]).sum() / sp[keep].sum())


def tilt(x):
    """The spectral tilt: a line through the octave bands' levels (40 Hz-2.5 kHz), dB per octave."""
    m = x.mean(axis=0)
    sp = np.abs(np.fft.rfft(m * np.hanning(len(m)))) ** 2
    f = np.fft.rfftfreq(len(m), 1 / SR)
    centres = 40.0 * 2.0 ** np.arange(7)  # 40 ... 2560 Hz
    lv = [10 * np.log10(sp[(f >= c / np.sqrt(2)) & (f < c * np.sqrt(2))].sum() + 1e-20) for c in centres]
    return float(np.polyfit(np.arange(len(centres)), lv, 1)[0])


def shape(x, min_gap):
    e, hop, i, j = two_beats(x, min_gap)
    wl, wd = width(e, i, hop), width(e, j, hop)
    return dict(sp=(j - i) * hop, dub=20 * np.log10(e[j] / e[i]), wl=wl, wd=wd,
                cl=centroid(x, i * hop, max(0.08, 2 * wl)), cd=centroid(x, j * hop, max(0.08, 2 * wd)), tilt=tilt(x))


def compare(beats):
    print("source comparison: lub-dub spacing, the dub's peak vs the lub's, -10 dB lengths, centroids (brightness), "
          "spectral tilt 40 Hz-2.5 kHz (dB/octave)")
    for v in BEATS:
        layers, sound, t_lub, t_dub = BEATS[v][0], *BEATS[v][6]
        src = M.preview(sound)
        raw = src[:, int((t_lub - 0.15) * SR):int((t_dub + 0.35) * SR)]
        n = int(LENGTH * SR)
        slowed = np.pad(M.mix(layers), ((0, 0), (0, n)))[:, :n]
        rows = (("raw " + str(sound), raw, 0.15), ("slowed only", slowed, 0.35), ("r98-" + v, beats[v], 0.35))
        for name, x, gap in rows:
            s = shape(x, gap)
            print(f"  {v} {name:14s} spacing {s['sp']:.2f} s, dub {s['dub']:+5.1f} dB, lengths lub {s['wl']:.2f} / "
                  f"dub {s['wd']:.2f} s, centroid lub {s['cl']:4.0f} / dub {s['cd']:4.0f} Hz, tilt "
                  f"{s['tilt']:+5.1f} dB/oct", flush=True)


# ---- main

def main(argv):
    only_measure = "--measure" in argv
    names = [a for a in argv if not a.startswith("--")] or list(BEATS)
    if not only_measure:
        for v in names:
            x = beat(v)
            tag = {"COMMENT": f"tools/concept/audio/sfx_r98.py heart-beat-r98-{v}: {BEATS[v][7]}"}
            path = write_ogg(OUT / f"heart-beat-r98-{v}.ogg", x, max_peak_db=-3.0, tags=tag)
            tag = {"COMMENT": f"tools/concept/audio/sfx_r98.py vrell-hive-heartbeat-r98-{v}: heart-beat-r98-{v} as "
                              f"the hive's layer (round 84's treatment, the audible band kept)"}
            write_ogg(OUT / f"vrell-hive-heartbeat-r98-{v}.ogg", hive(decode(path), v), max_peak_db=-3.0, tags=tag)
    music = decode(R.MUSIC)
    bed = decode(R.BED)
    beats = {"r84b": decode(R.R84_BEAT)}
    hives = {"r84b": decode(R.R84_HIVE)}
    for v in BEATS:
        beats[v] = decode(OUT / f"heart-beat-r98-{v}.ogg")
        hives[v] = decode(OUT / f"vrell-hive-heartbeat-r98-{v}.ogg")[:, :16 * SR]
        print(f"r98-{v}: beat {lufs(beats[v])[0]:.1f} LUFS, hive {lufs(hives[v])[0]:.1f} LUFS, hive seam "
              f"{seam_db(hives[v]):+.1f} dB", flush=True)
    if not only_measure:
        start = R.music_window(music)
        print(f"track 20 preview window: {start}-{start + 20} s", flush=True)
        write_previews("heart-beat", {k: R.mix_preview(x, music, start) for k, x in beats.items()})
        write_previews("vrell-hive-heartbeat", {k: R.hive_preview(x, bed) for k, x in hives.items()})
    R.measure(beats, hives, music, bed)
    compare(beats)


if __name__ == "__main__":
    main(sys.argv[1:])
