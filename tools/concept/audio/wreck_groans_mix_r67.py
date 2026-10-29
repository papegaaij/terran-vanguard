#!/usr/bin/env python3
"""Concept round 67, row S.4 (re-shown 2026-10-29: "both are too quiet, I can hardly hear them"): the `wreck-groans`
layer's review files, rendered offline from the files the game plays, at the game's own gains (not a capture).

The game's mix at Level 34 (default audio settings: master 1, music 1, effects 0.5; game/.../LevelMusic.java):
  music   track 12's base stem (assets/music/hollow-rock-base.ogg) at MUSIC_VOLUME 0.6 x the level's section_db -3 dB
          (0.425, -7.4 dB), looping from level t=0 (the radio's -4 dB duck left out: no radio here)
  bed     `belt-open` (assets/sfx/belt-open-r60-a.ogg) at AMBIENCE_VOLUME 0.7 x the effects bus 0.5 (0.35, -9.1 dB),
          looping from t=0
  layer   `wreck-groans` (design/audio/sfx/concept/wreck-groans-r67-a|b.ogg) at the same 0.35, looping by the level
          time (the level's ambience_layers: in over t=0-3)
No sound effects or radio: the stretch t=8-28 of the graveyard's edge as it sounds between the fights.

Outputs (design/audio/sfx/concept/, OGG Vorbis q6, 44.1 kHz stereo):
  wreck-groans-mix-r67-a.ogg, -b.ogg    20 s: bed + music + the layer at its in-game level, t=8-28
  wreck-groans-solo-r67-a.ogg, -b.ogg   the 30 s layer alone, boosted so its loudest momentary loudness (400 ms)
                                        matches the mean of the other sound rows' (S.1-S.3's a: container-burst,
                                        hold-clamp, helix-core)
and prints the measure: per variant the groans' short-term loudness (3 s, EBU R128) at its loudest in the mix against
the bed + music's short-term loudness over the same stretch (median), all in LUFS.

Run: python3 tools/concept/audio/wreck_groans_mix_r67.py [--measure]   (--measure writes nothing)
"""
import re
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from synth import SR, db, decode, undb, write_ogg, write_wav  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/wreck_groans_mix_r67.py"
MUSIC = ROOT / "assets" / "music" / "hollow-rock-base.ogg"
BED = ROOT / "assets" / "sfx" / "belt-open-r60-a.ogg"
# round 67's S.2 "dock": a hold clamp plays Level 29's Dock 9 clamp release (step FIX3)
OTHERS = [OUT / "container-burst-r67-a.ogg", ROOT / "assets" / "sfx" / "clamp-release-r62-b.ogg",
          OUT / "helix-core-r67-a.ogg"]
G_MUSIC = 0.6 * 10 ** (-3 / 20)
G_AMB = 0.7 * 0.5
T0, T1 = 8.0, 28.0


def ebur128(x):
    """(momentary, short-term) loudness per 0.1 s (LUFS arrays) of a stereo signal, by ffmpeg's ebur128."""
    with tempfile.TemporaryDirectory() as tmp:
        p = Path(tmp) / "m.wav"
        write_wav(p, x)
        r = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", str(p), "-af", "ebur128", "-f", "null", "-"],
                           capture_output=True, text=True, check=True)
    m, s = [], []
    for line in r.stderr.splitlines():
        mm = re.search(r"M:\s*(\S+)\s+S:\s*(\S+)", line)
        if mm and "t:" in line:
            m.append(_num(mm.group(1)))
            s.append(_num(mm.group(2)))
    return np.array(m), np.array(s)


def _num(text):
    try:
        v = float(text)
    except ValueError:
        return -np.inf
    return v if np.isfinite(v) else -np.inf


def looped(x, start, n):
    idx = (np.arange(n) + int(start * SR)) % x.shape[1]
    return x[:, idx]


def stems(layer):
    n = int((T1 - T0) * SR)
    music = looped(decode(MUSIC), T0, n) * G_MUSIC
    bed = looped(decode(BED), T0, n) * G_AMB
    groans = looped(decode(layer), T0, n) * G_AMB
    return music, bed, groans


def measure(layer):
    music, bed, groans = stems(layer)
    _, s_bg = ebur128(music + bed)
    _, s_g = ebur128(groans)
    mix = music + bed + groans
    _, s_mix = ebur128(mix)
    k = 30                                                  # skip the first 3 s (the short-term window filling)
    return {"bg": float(np.median(s_bg[k:])), "groan": float(np.max(s_g[k:])), "mix_max": float(np.max(s_mix[k:])),
            "mix_bg_max": float(np.max(s_bg[k:])), "mix": mix}


def main(argv):
    only = "--measure" in argv
    m_others = [float(np.max(ebur128(decode(f))[0])) for f in OTHERS]
    target = float(np.mean(m_others))
    print(f"other rows' loudest momentary: {', '.join(f'{v:.1f}' for v in m_others)} LUFS; mean {target:.1f}")
    for v in "ab":
        layer = OUT / f"wreck-groans-r67-{v}.ogg"
        r = measure(layer)
        print(f"{v}: in the mix (t {T0:.0f}-{T1:.0f}) the groans' short-term max {r['groan']:.1f} LUFS vs bed + music "
              f"{r['bg']:.1f} (median; max {r['mix_bg_max']:.1f}): {r['groan'] - r['bg']:+.1f} dB; the mix's "
              f"short-term max {r['mix_max']:.1f}")
        if only:
            continue
        mix = r["mix"]
        write_ogg(OUT / f"wreck-groans-mix-r67-{v}.ogg", mix, max_peak_db=-1.0,
                  tags={"COMMENT": f"{SCRIPT}: wreck-groans {v} in the game's mix (bed + music + layer), t 8-28"})
        solo = decode(layer)
        m_solo = float(np.max(ebur128(solo)[0]))
        gain = target - m_solo
        solo = solo * undb(gain)
        peak = db(np.abs(solo).max())
        if peak > -1.0:
            solo = solo * undb(-1.0 - peak)
        write_ogg(OUT / f"wreck-groans-solo-r67-{v}.ogg", solo, max_peak_db=-1.0,
                  tags={"COMMENT": f"{SCRIPT}: wreck-groans {v} solo, boosted {gain:+.1f} dB to the other rows' "
                                   f"loudest momentary {target:.1f} LUFS"})
        print(f"   solo: +{gain:.1f} dB (momentary max {m_solo:.1f} -> {m_solo + gain:.1f} LUFS, peak "
              f"{db(np.abs(solo).max()):.1f} dBFS)")


if __name__ == "__main__":
    main(sys.argv[1:])
