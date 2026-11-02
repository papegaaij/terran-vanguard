#!/usr/bin/env python3
"""Concept round 80 music (M10 part H, step AUD-H; the gap check's #18 and #20): track 33, the escape sting, in
production from round 72's pick a "Collapse" (music_r72.py `sting_collapse`, M.4 a). Brief: design/audio/music/README.md,
*Act 6 music* (the escape sting: "as the lifeboat leaves the top edge the sting cuts track 18: Vorne's motif in E
(E G B D-sharp) stated once in brass, its last note bending down, then falling apart into the Choir motif's tritone
drop in the choir, ending on a held low E with the flat-2 over it, unresolved, about 6 s"; a one-shot from Level 42's
music block at `ESCAPED`; after it the ambience alone, Varga's closing line from E + 2 s).

The method is music_r58h.py's (track 30, the reveal sting): the sketch's notes, voices, groups, chains and seeds
unchanged (126 BPM, E minor: the motif once in low brass and organ over a timpani roll, the D-sharp bending down a
semitone and dying; the choir's E D C B and the drop to F; the held low E in strings, organ, a sub and a timpani hit,
the F over it), with the production's additions on their own groups:

  the cut      a low orchestral hit on the one (a deep kick and a timpani on E under the roll), since the sting cuts
               track 18 dead: the downbeat the cut lands on
  horns        the motif doubled an octave down in soft brass (the sketch's brass voice, panned the other way), the
               D-sharp bending with it, so the statement has the weight of a section
  cellos       low strings under the choir's descent (E3 D3 C3 B2, then the F2 of the drop): the choir's line with a
               floor, falling apart together
  tremolo      the held F over the low E in tremolo strings (the act's storm strings, quiet), the unresolved rub kept
               alive to the end
  the tail     the held chord left to ring 0.5 s longer than the sketch (7.0 s, a 1.4 s fade instead of a cut at 6.6 s)

and the reveal sting's **voice pocket**: from 1.8 s the 0.9-4 kHz band eased down 4 dB over 0.6 s, so Varga's closing
line (E + 2 s) sits over the choir and the held chord (the radio's own duck still applies in the game). Mastered as the
sketch (-14 LUFS, the -2 dBFS ceiling, true peak under -1 dBTP).

Output (the --out directory, default design/audio/music/concept/):
  escape-sting-r80-a.ogg   #33 "Collapse" in production; pipeline's copyPlaceholderMusic imports it into
                           assets/music/escape-sting.ogg
No SOURCE comment: a concept file.

Usage: python3 tools/concept/audio/music_r80.py [--out DIR]   (~20 s)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the pocket's depth and the
level of the cut, the motif, the choir and the held end (printed).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import note_freq, timpani, transpose, v_brass, v_strings_long  # noqa: E402
from music_r03 import v_tremolo  # noqa: E402
from music_r48 import deep_kick  # noqa: E402
from music_r58h import band_zero_phase  # noqa: E402
from music_r60 import _bus  # noqa: E402
from music_r72 import sting_collapse, v_brass_bend  # noqa: E402
from synth import SR, db, decode, fade, lufs, master, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "escape-sting-r80-a"
LENGTH, FADE = 7.0, 1.4
POCKET = dict(start=1.8, ramp=0.6, lo=900.0, hi=4000.0, depth=4.0)

# the sketch's chains and seeds (music_r72._sting_mix), the production's groups after them
MIX = {
    "vorne": dict(hp=50, rev=(3.2, 0.35), seed=7251, gain=0.85),
    "choir": dict(hp=110, rev=(4.2, 0.5), wid=1.6, seed=7252, gain=0.8),
    "low": dict(hp=30, rev=(3.5, 0.4), wid=1.4, seed=7253, gain=0.6),
    "organ": dict(hp=30, rev=(4.0, 0.45), wid=1.3, seed=7254, gain=0.5),
    "perc": dict(hp=30, rev=(2.8, 0.3), damp=0.8, seed=7255, gain=0.8),
    "sub": dict(hp=25, gain=0.5),
    "cut": dict(hp=28, rev=(2.4, 0.2), damp=0.85, seed=8001, gain=0.7),
    "horns": dict(hp=60, rev=(3.4, 0.4), wid=1.2, seed=8002, gain=0.55),
    "cellos": dict(hp=40, rev=(3.6, 0.42), wid=1.3, seed=8003, gain=0.5),
    "trem": dict(hp=120, rev=(3.8, 0.45), wid=1.5, seed=8004, gain=0.22),
}


def escape_sting():
    """The sketch's Song with the production's groups added (cut, horns, cellos, tremolo)."""
    s, _ = sting_collapse()
    # the cut: a low hit on the one under the sketch's timpani roll
    s.hit("cut", deep_kick(), 0, 0, 0.8)
    s.hit("cut", timpani("E2", 2.2), 0, 0, 0.7)
    # horns: the motif an octave down in soft brass, the D-sharp bending with the sketch's
    for nm, st, ln in [("E3", 0, 6), ("G3", 6, 2), ("B3", 8, 4)]:
        s.hit("horns", v_brass(note_freq(transpose(nm, -12)), round(s.step * ln * 0.95, 4)), 0, st, 0.45, 0.25)
    s.hit("horns", v_brass_bend(note_freq("D#3"), round(s.step * 10, 4), -1.0, 0.35), 0, 12, 0.45, 0.25)
    # cellos: the choir's descent an octave and a half down, then the drop's F
    for k, nm in enumerate(["E3", "D3", "C3", "B2"]):
        s.hit("cellos", v_strings_long(note_freq(nm), round(s.step * 3.4, 4), attack=0.12), 1, 2 + 3 * k, 0.5,
              -0.2)
    s.hit("cellos", v_strings_long(note_freq("F2"), round(s.step * 22, 4), attack=0.2), 1, 14, 0.35, -0.2)
    # tremolo: the held F over the low E, alive to the end
    s.hit("trem", v_tremolo(note_freq("F4"), round(s.step * 26, 4)), 1, 14, 0.4, 0.3)
    s.hit("trem", v_tremolo(note_freq("E4"), round(s.step * 26, 4)), 1, 14, 0.3, -0.3)
    return s


def segment(x, a, b):
    return db(np.sqrt(np.mean(x[:, int(a * SR):int(b * SR)] ** 2)) + 1e-12)


def main(argv):
    out = OUT
    if "--out" in argv:
        out = Path(argv[argv.index("--out") + 1])
    out.mkdir(parents=True, exist_ok=True)
    s = escape_sting()
    mix = _bus(s, MIX)[:, :int(LENGTH * SR)]
    before = segment(band_zero_phase(mix, POCKET["lo"], POCKET["hi"]), 2.5, 6.0)
    t = np.arange(mix.shape[1]) / SR
    w = np.clip((t - POCKET["start"]) / POCKET["ramp"], 0, 1) * (1 - undb(-POCKET["depth"]))
    mix = mix - band_zero_phase(mix, POCKET["lo"], POCKET["hi"]) * w
    after = segment(band_zero_phase(mix, POCKET["lo"], POCKET["hi"]), 2.5, 6.0)
    mix = mix - mix.mean(axis=1, keepdims=True)
    mix = master(mix, -14.0, -2.0)
    mix = fade(mix, 0.003, FADE)
    path = write_ogg(out / f"{NAME}.ogg", mix, tags={
        "COMMENT": f"tools/concept/audio/music_r80.py {NAME}: track 33 'Collapse', the escape sting, in production "
                   "(music_r72.py's sketch a with a cut, horns, cellos, tremolo and a voice pocket)"})
    dec = decode(path)
    loud, tp = lufs(dec)
    bar = s.bar
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak "
          f"{tp:.1f} dBTP; the 0.9-4 kHz band 2.5-6 s {after - before:+.1f} dB (the voice pocket)", flush=True)
    for label, a, b in [("the cut and the motif", 0.0, bar), ("the choir's descent", bar + 2 * s.step, bar + 14 * s.step),
                        ("the held end", bar + 14 * s.step, LENGTH - FADE), ("the fade", LENGTH - FADE, LENGTH)]:
        print(f"  {label:<22} {a:5.2f}-{b:5.2f} s  {segment(dec, a, b):6.1f} dB RMS", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
