#!/usr/bin/env python3
"""Concept round 65 music (M9 part E, step AUD-E): track 12 "Hollow Rock"'s fourth stem, `conveyor`, for Level 32's
forks. Brief: design/audio/music/README.md, *Act 5 music*, Level 32's row ("the conveyor breakdown with the lone horn at
the forks (sections 3 and 5)"). The game plays a theme's stems sample-aligned and switches them at times
(`music.stem_times`, as Level 26's dark stem), so the breakdown cannot be jumped to: this stem carries the sketch's
conveyor breakdown (music_r62.py's sections C and C2) over the whole loop, on each section's own chords, so it sounds
like the breakdown wherever the fork lands in the loop.

The stem (music_r62.py's render, the same song, voices, chains and seeds; new groups added after it):
  the conveyor (anvil 16ths at 3.1 kHz), the anvils' pattern at the breakdown's weight (1.2), the distorted bass held
  on each bar's root, the cold pad, the machine's arpeggio dark and quiet (as in C), the steam hiss every 4 bars, and
  the **lone human horn** (humanity's motif on the flute, music_r62's horn chain) twice in every 8-bar section: on
  bars 1 and 5 where the chord holds (bars 2 and 6 in C and C2, as the sketch), from the chord's root, minor or major
  with the chord. No kick, snare, hats, riser or brass (the motif stem can still play on top).

Mastering is music_r62.py's, linked: the full mix is rendered again and its gain (and the encoder's peak correction)
applied to the stem, so the stem shares the full mix's length, LOOPSTART / LOOPLENGTH and gain. The default run checks
that the re-rendered full mix decodes to design/audio/music/concept/hollow-rock-r62-a.ogg sample for sample (so the
shipped files are unchanged and the stem lines up with them); it writes only the new stem and its aids.

Outputs (the --out directory, default design/audio/music/concept/):
  hollow-rock-conveyor-r65-a.ogg        the conveyor stem (same length, loop and gain as the r62 files)
  hollow-rock-conveyor-seam-r65-a.ogg   listening aid: the last 8 s of the loop, then the first 8 s after LOOPSTART
  hollow-rock-conveyor-join-r65-a.ogg   listening aid: loop bars 1-16, the full mix crossfading over 2 s into the
                                        conveyor stem alone at bar 9 (as a fork would switch), 28.4 s
No SOURCE comment: a concept file; tools/art/themes.py copies it into assets/music with it once picked.

Usage: python3 tools/concept/audio/music_r65.py [--out DIR]   (~6 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seam and the alignment.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
import music_r62 as r62  # noqa: E402
from music import transpose, v_pad  # noqa: E402
from music_r02 import seam_ratio, v_dbass, v_flute  # noqa: E402
from music_r03 import BE_CH, BE_METAL, PERC, anvil, figure, human_motif, mixdown, steam  # noqa: E402
from music_r41 import fade_ends, master_linked, tag_values  # noqa: E402
from music_r51b import v_pluck_soft  # noqa: E402
from synth import SR, db, decode, lufs, note_freq, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "hollow-rock-conveyor-r65-a"
SEAM_SECONDS = 8.0
JOIN_BARS = (0, 16)  # loop bars of the join aid; the switch at bar 9 over 2 s
CHAINS = {
    "cv_metal": r62.CHAINS["metal_low"],
    "cv_conveyor": r62.CHAINS["conveyor"],
    "cv_bass": dict(hp=40, gain=0.65),                                  # held, no kick to duck under
    "cv_pad": dict(hp=100, rev=(3.0, 0.4), wid=1.6, seed=212, gain=0.5),
    "cv_arp": r62.CHAINS["arp"],
    "cv_perc": PERC,
    "cv_horn": r62.CHAINS["horn"],
}
HORN_ROOT = {"Bm": "B3", "Gm": "G3", "G": "G3", "Em": "E4", "F#": "F#3"}  # near the sketch's B3, in the flute's range
MAJOR = {"G", "F#"}


def horn_bars(kind):
    return (1, 5) if kind in ("C", "C2") else (0, 4)


def add_conveyor(s):
    """The conveyor stem's hits on new groups of the rendered song (the r62 groups are untouched)."""
    for base in s.sections():
        off = 0
        for kind, chords in r62.PLAN:
            for j, name in enumerate(chords):
                bar = base + off + j
                pad, root, arp = BE_CH[name]
                for st, p, g in BE_METAL:
                    s.hit("cv_metal", anvil(float(p)), bar, st, g * 1.2, (p - 1350) / 1500)
                for st in range(16):
                    s.hit("cv_conveyor", anvil(3100.0, 3), bar, st, 0.1 if st % 2 else 0.16, 0.5)
                s.hit("cv_bass", v_dbass(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.3)
                notes = figure(transpose(root, 24), arp)
                for st in range(16):
                    s.hit("cv_arp", v_pluck_soft(note_freq(notes[st % 8]), round(s.step * 0.6, 4), 900.0),
                          bar, st, 0.15, 0.35 if st % 2 else -0.35)
                s.chord("cv_pad", v_pad, pad, bar, dur_steps=16, gain=0.4, bright=900.0)
                if j % 4 == 3:
                    s.hit("cv_perc", steam(), bar, 12, 0.35, 0.5 if j % 8 == 3 else -0.5)
            for k in horn_bars(kind):
                chord = chords[k]
                s.play("cv_horn", v_flute, human_motif(HORN_ROOT[chord], major=chord in MAJOR), base + off + k,
                       gain=0.4, position=0.4)
            off += len(chords)


def render():
    s, groups = r62.hollow_rock()
    add_conveyor(s)
    conveyor = sum(mixdown(s, {g: spec}, [], depth=0.45) for g, spec in CHAINS.items())
    full = sum(groups[g] for g in r62.FULL_GROUPS)
    return s, full, conveyor


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
    out.mkdir(parents=True, exist_ok=True)
    s, full, conveyor = render()
    full, loop_start, loop_len = r11.prepare(s, full)
    conveyor, _, _ = r11.prepare(s, conveyor)
    full, conveyor = master_linked([full, conveyor], ceiling_db=r62.CEILING)
    for a in (full, conveyor):
        a[:, :int(0.005 * SR)] *= np.linspace(0, 1, int(0.005 * SR))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    scratch = out / f"{NAME}-check-full.ogg"
    for _ in range(4):  # music_r62's peak correction for the full mix, applied to the stem
        write_ogg(scratch, full, max_peak_db=None, tags=tags)
        over = db(np.abs(decode(scratch)).max()) + 1.0
        if over <= 0:
            break
        full, conveyor = (x * undb(-over - 0.1) for x in (full, conveyor))
    shipped = decode(OUT / f"{r62.FULL_NAME}.ogg")
    mine = decode(scratch)
    scratch.unlink()
    same = mine.shape == shipped.shape and float(np.abs(mine - shipped).max()) == 0.0
    print(f"the re-rendered full mix vs {r62.FULL_NAME}.ogg: "
          f"{'identical' if same else 'DIFFERENT: max %.2e' % float(np.abs(mine - shipped).max())}", flush=True)
    path = write_ogg(out / f"{NAME}.ogg", conveyor, max_peak_db=None, tags=tags)
    d = decode(path)
    t = tag_values(path)
    aligned = d.shape == shipped.shape and int(t["LOOPSTART"]) == loop_start and int(t["LOOPLENGTH"]) == loop_len
    loud, tp = lufs(d)
    full_lufs = lufs(shipped)[0]
    print(f"{path.name}: {d.shape[1] / SR:.2f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s  seam "
          f"{seam_ratio(d, loop_start, loop_len):.2f}  {loud:.1f} LUFS ({loud - full_lufs:+.1f} LU vs the full mix)  "
          f"true peak {tp:.1f} dBTP  {path.stat().st_size / 1e6:.2f} MB  {'aligned' if aligned else 'NOT ALIGNED'}",
          flush=True)
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    seam = np.concatenate([d[:, end - n:end], d[:, loop_start:loop_start + n]], axis=1)
    aid = write_ogg(out / "hollow-rock-conveyor-seam-r65-a.ogg", fade_ends(seam), max_peak_db=None)
    print(f"aid: {aid.name}", flush=True)
    bar_n = 16 * s.step_n
    a, b = loop_start + JOIN_BARS[0] * bar_n, loop_start + JOIN_BARS[1] * bar_n
    k, x = loop_start + 8 * bar_n, int(2.0 * SR)
    w = np.ones(b - a)
    w[k - a:k - a + x] = np.linspace(1, 0, x)
    w[k - a + x:] = 0
    join = shipped[:, a:b] * w + d[:, a:b] * (1 - w)
    print(f"aid: {write_ogg(out / 'hollow-rock-conveyor-join-r65-a.ogg', fade_ends(join), max_peak_db=None).name} "
          f"({(b - a) / SR:.1f} s)", flush=True)
    # the horn's statements against the full mix's breakdown C (its sketch level): the stem's C bars vs the stem elsewhere
    at = 0
    for kind, chords in r62.PLAN:
        i0, i1 = loop_start + at * bar_n, loop_start + (at + len(chords)) * bar_n
        print(f"  {kind:<3} stem {lufs(d[:, i0:i1])[0]:6.1f} LUFS   full {lufs(shipped[:, i0:i1])[0]:6.1f} LUFS",
              flush=True)
        at += len(chords)


if __name__ == "__main__":
    main(sys.argv[1:])
