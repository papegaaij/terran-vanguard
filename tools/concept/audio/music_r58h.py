#!/usr/bin/env python3
"""Concept round 58 (M8 part H, step AUD-H): Level 28's music. Track 30, the reveal sting, in production from the
round-48 pick a "Black and Gold"; and the Abyssal Maw's boss theme for the gap check's Q.1: track 18 "The Choir
Descends" as built (a, the file the game plays: choir-descends-final-r25-a.ogg, no new file) or a pre-rendered **deep
copy** (b) from its own generator through the A theme's water and a longer reverb, on the same loop points (a new
file, no new hook).

Outputs (design/audio/music/concept/; OGG Vorbis q6):

  reveal-sting-r58-a.ogg       #30 "Black and Gold" in production: music_r48.py's sting_black_and_gold() unchanged
                               (B minor, 9.0 s: the tremolo strings, organ pedal and timpani roll swelling from muffled
                               to open over the cruisers' 4 s rise, the motif stacked into a chord at 4.0 s as the gun
                               ports open, the A-sharp left hanging), plus a voice pocket: from 4.6 s the 0.9-4 kHz band
                               eased down 5 dB over 0.6 s, so Varga's and Okafor's lines sit over the tail; -15 LUFS
                               as the sketch (the -2 dBFS ceiling holds it), a 1.2 s fade (the round-48 sting format)
  choir-descends-deep-r58-b.ogg  #18 "The Choir Descends", the deep copy: music_r08.py's choir_full() rendered as the
                               game's file is (the same intro, loop and 2-bar tail: LOOPSTART 299880, LOOPLENGTH
                               5080320), its whole mix through the A theme's water low-pass at its deepest (1.3 kHz,
                               q 1.1: "Thera Deep"'s drift section) and a 5.5 s reverb (mix 0.35, dark) before the assembly, so the
                               reverb's tail wraps round the loop seam; -14 LUFS, -2 dBFS ceiling
  choir-descends-deep-seam-r58-b.ogg  listening aid: b across its loop seam, 8 s each side
  boss-handoff-deep-r58-b.ogg  listening aid: the boss cue as the game mixes it (track 22 "Red Alert", then b from
                               Tracks.BOSS_WARNING_BARS_SECONDS, 8 bars), as round 25's boss-handoff-final-r25-a

Usage: python3 tools/concept/audio/music_r58h.py [sting] [deep] [--out DIR]
Deterministic (the generators' fixed seeds); not listened to by Claude: checked by loudness, true peak, the loop
points against the game's track 18, the seam and the voice band's level.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "art"))
import music_r08  # noqa: E402
import music_r48  # noqa: E402
from music_r02 import add_tail, seam_ratio  # noqa: E402
from music_r36b import bandpass  # noqa: E402
from music_r41 import fade_ends  # noqa: E402
from music import ns  # noqa: E402
from synth import SR, db, decode, fade, lufs, master, reverb, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
GAME_18 = ROOT / "assets" / "music" / "choir-descends.ogg"
WARNING = ROOT / "assets" / "music" / "boss-warning.ogg"

# the voice pocket under the sting's tail
POCKET = dict(start=4.6, ramp=0.6, lo=900.0, hi=4000.0, depth=5.0)
# the deep copy: the A theme's water at its deepest (track 10's drift section E: 1.3 kHz, q 1.1), a longer, darker reverb
DEEP = dict(cutoff=1300.0, q=1.1, seconds=5.5, mix=0.35, damping=0.7, seed=5801)
SEAM_SECONDS = 8.0
HANDOFF_BARS_AFTER = 8


def band_db(x, lo, hi, a, b):
    seg = bandpass(x[:, int(a * SR):int(b * SR)], lo, hi)
    return db(np.sqrt(np.mean(seg ** 2)) + 1e-12)


def band_zero_phase(x, lo, hi, edge=1 / 3):
    """The lo-hi band of the whole signal, zero phase (an FFT mask with raised-cosine edges `edge` octaves wide), so
    that subtracting a share of it lowers that band by exactly that share."""
    f = np.fft.rfftfreq(x.shape[1], 1 / SR)
    lf = np.log2(np.maximum(f, 1.0))
    up = np.clip((lf - np.log2(lo) + edge / 2) / edge, 0, 1)
    down = np.clip((np.log2(hi) + edge / 2 - lf) / edge, 0, 1)
    mask = (0.5 - 0.5 * np.cos(np.pi * up)) * (0.5 - 0.5 * np.cos(np.pi * down))
    return np.fft.irfft(np.fft.rfft(x, axis=1) * mask, x.shape[1], axis=1)


def sting(out):
    mix, length = music_r48.sting_black_and_gold()
    mix = mix[:, :int(length * SR)]
    before = band_db(mix, POCKET["lo"], POCKET["hi"], 5.0, 8.0)
    t = np.arange(mix.shape[1]) / SR
    w = np.clip((t - POCKET["start"]) / POCKET["ramp"], 0, 1) * (1 - undb(-POCKET["depth"]))
    mix = mix - band_zero_phase(mix, POCKET["lo"], POCKET["hi"]) * w
    after = band_db(mix, POCKET["lo"], POCKET["hi"], 5.0, 8.0)
    mix = mix - mix.mean(axis=1, keepdims=True)
    mix = master(mix, -14.0, -2.0)
    mix = fade(mix, 0.003, 1.2)
    path = write_ogg(out / "reveal-sting-r58-a.ogg", mix, tags={
        "COMMENT": "tools/concept/audio/music_r58h.py reveal-sting-r58-a: track 30 'Black and Gold' in production "
                   "(music_r48.py's sketch a with a voice pocket under its tail)"})
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak "
          f"{tp:.1f} dBTP; the 0.9-4 kHz band in the tail (5-8 s, before mastering) {after - before:+.1f} dB",
          flush=True)
    return path


def deep(out):
    s, mix = music_r08.choir_full()
    n = mix.shape[1]
    water = np.vstack([svf(ch, DEEP["cutoff"], q=DEEP["q"]) for ch in mix])
    mix = reverb(water, seconds=DEEP["seconds"], mix=DEEP["mix"], damping=DEEP["damping"], seed=DEEP["seed"])[:, :n]
    # render_loop's steps (music_r08.py), unchanged
    mix = mix - mix.mean(axis=1, keepdims=True)
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, int(round(16 * s.step_n)))
    audio = master(audio, -14.0, -2.0)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / "choir-descends-deep-r58-b.ogg", audio, tags={
        "LOOPSTART": loop_start, "LOOPLENGTH": loop_len,
        "COMMENT": "tools/concept/audio/music_r58h.py choir-descends-deep-r58-b: track 18 through the A theme's "
                   "water (1.3 kHz) and a 5.5 s reverb, the game's loop points"})
    dec = decode(path)
    loud, tp = lufs(dec)
    game = decode(GAME_18)
    print(f"{path.name}: {dec.shape[1] / SR:.1f}s ({game.shape[1] / SR:.1f}s the game's)  loop {loop_start}+{loop_len}"
          f"  seam {seam_ratio(dec, loop_start, loop_len):.2f}  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} "
          f"LUFS  true peak {tp:.1f} dBTP; above 2 kHz {band_db(dec, 2000, 16000, 10, 60):.1f} dB "
          f"(the game's {band_db(game, 2000, 16000, 10, 60):.1f})", flush=True)
    # the aids: the seam, and the boss cue as the game mixes it (art/themes.py's boss_cue)
    k = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    seam = np.concatenate([dec[:, end - k:end], dec[:, loop_start:loop_start + k]], axis=1)
    write_ogg(out / "choir-descends-deep-seam-r58-b.ogg", fade_ends(seam), max_peak_db=None)
    import themes  # noqa: E402  (tools/art: the boss cue and the game's hand-off second)
    warning = decode(WARNING)
    at = int(round(themes.handoff_seconds() * SR))
    bar = int(round(16 * 15 * SR / themes.WARNING_BPM))
    cue, clamped = themes.boss_cue(warning, dec, at, at + HANDOFF_BARS_AFTER * bar)
    write_ogg(out / "boss-handoff-deep-r58-b.ogg", themes.fade_out(cue, 1.0), max_peak_db=None)
    print(f"aids: the seam ({2 * SEAM_SECONDS:.0f} s), the hand-off at {at / SR:.2f} s ({clamped} clamped samples); "
          f"first bar of b {themes.segment_lufs(dec[:, :bar]):.1f} LUFS", flush=True)
    return path


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or ["sting", "deep"]:
        {"sting": sting, "deep": deep}[key](out)


if __name__ == "__main__":
    main(sys.argv[1:])
