#!/usr/bin/env python3
"""Concept round 68 music (M9 part H, step AUD-H1): track 19 "Iron Sovereign" (the Ascendancy boss theme: Level 35's
boss from the boss warning, later the Ascendant) at full length, one full mix, no stems, from the chosen round-03
sketch (music_r03.py `ascendancy`, ascendancy-boss-r03-a: 147 BPM, F-sharp minor; QH-3 a). Brief:
design/audio/music/README.md, *Act 5 music* (track 19: "full length (about 2:00 loop) from the round-03 generator as
track 18's full length was made in round 08: one full mix, no stems").

The method is music_r08.py's `choir_full` (track 18, round 08): the sketch's voices, grooves, chords, figures, mix chains
and seeds, laid out as its 4-bar intro + a 72-bar loop (1:57.6) of nine 8-bar sections, so the 2:00 does not play
the sketch's 32-bar loop twice. The sketch's four sections (A march, B machine, C menace and build, D tutti) stay as
they were; five sections are added on their material, with breakdowns and build-ups a three-ring boss fight can sit
under (the fight's phases switch nothing: the loop simply runs):

  intro 4 bars  the parade (the sketch's intro): snare rudiments growing, timpani, Vorne's motif in low brass on F#
          then C#, the organ below, a riser
  A    A  march: kick 1, 3, 3&, the rudiment snare, march bass and the low-brass 8ths, trumpets on the motif (F#, then
          B), short-short-long stab figures
  B    B  machine: four-on-the-floor, metal hits, hats, the 16th riff on the motif's shape, off-beat trumpet stabs
  C    C  menace (half time, the motif at one note a bar in low brass and organ, the G Neapolitan), then the build
          (march, a snare roll, a 4-bar riser)
  D    A  tutti: trumpets on the motif with the low-brass canon a bar behind, two octaves down; the riff under it
  Br   Br the machine alone (new): no drums, the riff on its own over a held bass, the organ holding the chords, a
          metal tick on the beat and the timpani once a bar; from bar 5 the motif in low brass and organ, the kick and
          the rudiments coming back, a snare roll and a riser into B2
  B2   B  machine with brass (new): B's groove, the trumpets and the low brass two octaves down playing the motif in
          diminution (one bar each, on F#m and Bm) over the riff
  E    E  parade ground (new): the snare corps in front (the rudiments at full, a heavier kick, tom runs), timpani on 1
          and 3, the low-brass 8ths; the motif in low brass and organ, then in trumpets with the canon a bar behind (its
          sustained E# lands as C#'s third); stab figures on the G Neapolitan and on C#; a roll and a riser
  F    A  the build (new): the kick and the timpani on every beat with the bass held, the march from bar 3, the riff,
          the low-brass 8ths and the trumpets' motif on B from bar 5, a 4-bar riser and a snare roll into D2
  D2   A  tutti again: D plus the organ on the motif an octave down and a second crash; the roll and riser into A

No bubbly or popping sounds (the sketch had none: the metal is struck, the brass and organ have soft attacks).
Every group goes through the sketch's own chain (music_r03.ascendancy's mixdown spec, same seeds, the same kick duck);
the render and mastering are music_r08.render_loop's (intro + loop + 2-bar fade tail, -14 LUFS; the ceiling -3 dBFS,
as music_r62's, to keep the true peak under -1 dBTP;
LOOPSTART / LOOPLENGTH Vorbis comments, sample-exact loop: a 16th at 147 BPM is exactly 4500 samples).

Output (the --out directory, default design/audio/music/concept/):
  iron-sovereign-full-r68-a.ogg   the full mix
No SOURCE comment: a concept file; the import into assets/music is part H's (step H4).

Usage: python3 tools/concept/audio/music_r68.py [--out DIR]   (~3 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seam and the loudness per
section (printed).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import crash, hat, kick, ns, riser, snare, timpani, tom, transpose, v_brass, v_pad  # noqa: E402
from music_r02 import LoopSong, add_tail, seam_ratio, v_dbass, v_riff  # noqa: E402
from music_r03 import (AS_AUG, AS_CH, AS_LOOP, AS_SNARE, AS_TRUMPETS, FX, PERC, anvil, figure,  # noqa: E402
                       mixdown, v_organ, v_trumpet, vorne_motif)
from music_r41 import segment_lufs  # noqa: E402
from synth import SR, db, decode, lufs, master, note_freq, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "iron-sovereign-full-r68-a"
REST = ". " * 15 + "."
CEILING = -3.0  # dBFS, the limiter's ceiling: at -2 (music_r08's) the encode reached -0.8 dBTP (the limit is -1)

SK_A, SK_B, SK_C = AS_LOOP[:8], AS_LOOP[8:16], AS_LOOP[16:24]
PLAN = [("A", SK_A), ("B", SK_B), ("C", SK_C), ("D", SK_A),
        ("Br", ["F#m", "F#m", "D", "D", "Bm", "Bm", "C#", "C#"]),
        ("B2", SK_B),
        ("E", ["F#m", "F#m", "G", "G", "F#m", "F#m", "C#", "C#"]),
        ("F", SK_A), ("D2", SK_A)]
CANON = [". . . . . . . ." + " ." * 8] + AS_TRUMPETS[:-1]  # the sketch's: a bar behind
MOTIF_CANON = [REST] + vorne_motif("F#4")


def dim_motif(root):
    """Vorne's motif in diminution: one bar (dotted 8th, 16th, quarter, half)."""
    n = [transpose(root, i) for i in (0, 3, 7, 11)]
    return [f"{n[0]} - - {n[1]} {n[2]} - - - {n[3]} - - - - - - -"]


def plan_len():
    return sum(len(c) for _, c in PLAN)


def walk(base):
    off = 0
    for kind, chords in PLAN:
        for j, name in enumerate(chords):
            yield kind, j, name, base + off + j
        off += len(chords)


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def timp_note(root):
    return {"F#1": "F#2", "G1": "D2"}.get(root, "C#2")


# ---------------------------------------------------------------- the sketch's parts, one bar each

def march_kit(s, bar, kicks, scale=0.7, kick_steps=(0, 8, 10)):
    for st in kick_steps:
        s.hit("drums", kick(), bar, st, 0.9 if st in (0, 8) else 0.65)
        kicks.append(s.pos(bar, st))
    for st, v in enumerate(AS_SNARE):
        s.hit("drums", snare(v < 0.5), bar, st, v * scale, 0.05)


def machine_kit(s, bar, kicks):
    for st in (0, 4, 8, 12):
        s.hit("drums", kick(), bar, st, 0.9)
        kicks.append(s.pos(bar, st))
    for st in (4, 12):
        s.hit("drums", snare(), bar, st, 0.55, 0.05)
        s.hit("metal", anvil(1900.0, 4), bar, st, 0.3, -0.2)
    for st in range(2, 16, 4):
        s.hit("metal", anvil(1200.0, 5), bar, st, 0.2, 0.4)
    for st in range(16):
        s.hit("drums", hat(), bar, st, 0.16 if st % 2 == 0 else 0.1, 0.3)


def half_kit(s, bar, kicks):
    s.hit("drums", kick(), bar, 0, 0.95)
    kicks.append(s.pos(bar, 0))
    s.hit("drums", snare(), bar, 8, 0.7, 0.05)
    for st in range(0, 16, 2):
        s.hit("drums", snare(True), bar, st, 0.12, -0.1)


def roll(s, bar, eighths=False):
    for st in (range(0, 16, 2) if eighths else range(16)):
        s.hit("drums", snare(True), bar, st, 0.2 + 0.02 * st, 0.05)


def march_bass(s, bar, root, gain=0.4):
    for st in range(0, 16, 2):
        nm = transpose(root, 12 if st in (6, 14) else 0)
        s.hit("bass", v_dbass(note_freq(nm), round(s.step * 1.2, 4)), bar, st, gain)


def held_bass(s, bar, root, gain=0.35):
    s.hit("bass", v_dbass(note_freq(root), round(s.step * 15, 4)), bar, 0, gain)


def low_ostinato(s, bar, root, gain=0.3):
    for st in (0, 2, 4, 8, 10, 12):
        s.hit("lowbrass", v_brass(note_freq(transpose(root, 12)), round(s.step * 1.2, 4), stab=True), bar, st, gain)


def machine_riff(s, bar, root, riff_iv, gain):
    notes = figure(transpose(root, 24), riff_iv)
    for st in range(16):
        s.hit("riff", v_riff(note_freq(notes[st % 8]), round(s.step * 0.8, 4)), bar, st, gain,
              0.3 if st % 2 else -0.3)


def offbeat_stabs(s, bar, pad):
    for st in (6, 14):
        s.chord("brass", v_trumpet, [transpose(n, 12) for n in pad[1:]], bar, st, dur_steps=1, gain=0.45,
                spread=0.4, stab=True)


def stab_figure(s, bar, pad):
    for st in (0, 2, 4):
        s.chord("brass", v_trumpet, [transpose(n, 12) for n in pad[1:]], bar, st, dur_steps=1 if st < 4 else 4,
                gain=0.45, spread=0.4, stab=True)


# ---------------------------------------------------------------- the song

def intro(s):
    """The sketch's 4-bar parade, unchanged."""
    for bar in range(4):
        s.hit("perc", timpani("F#2" if bar < 2 else "C#2"), bar, 0, 0.9)
        s.chord("pad", v_pad, AS_CH["F#mM7" if bar < 2 else "C#"][0], bar, dur_steps=16, gain=0.4, bright=800.0)
        for st, v in enumerate(AS_SNARE):
            s.hit("drums", snare(True), bar, st, v * (0.4 + 0.15 * bar), 0.05)
    s.play("brass", v_brass, vorne_motif("F#3"), 0, gain=0.6)
    s.play("brass", v_brass, vorne_motif("C#3"), 2, gain=0.6)
    s.play("organ", v_organ, vorne_motif("F#3") + vorne_motif("C#3"), 0, gain=0.25, octave=-1)
    s.hit("fx", riser(s.bar), 3, 0, 0.25)


def section_bar(s, kicks, kind, j, name, bar):
    pad, root, riff_iv = AS_CH[name]
    # crashes and timpani
    if j == 0 and kind != "C" or (j == 4 and kind in ("F", "D2")):
        s.hit("perc", crash(), bar, 0, 0.4 if kind == "Br" else 0.55, 0.3)
    if kind == "Br":
        s.hit("perc", timpani(timp_note(root)), bar, 0, 0.45)
    elif kind == "E":
        for st in (0, 8):
            s.hit("perc", timpani(timp_note(root) if st == 0 else "C#2"), bar, st, 0.6 if st == 0 else 0.45)
    elif kind == "F" and j < 2:
        for st in (0, 4, 8, 12):
            s.hit("perc", timpani(timp_note(root)), bar, st, 0.45 + 0.1 * (st == 0))
    elif j % 2 == 0:
        s.hit("perc", timpani(timp_note(root)), bar, 0, 0.7)

    # drums
    if kind in ("A", "D", "D2"):
        march_kit(s, bar, kicks)
    elif kind in ("B", "B2"):
        machine_kit(s, bar, kicks)
    elif kind == "C":
        if j < 4:
            half_kit(s, bar, kicks)
        else:
            march_kit(s, bar, kicks)
    elif kind == "Br":
        for st in (0, 4, 8, 12):
            s.hit("metal", anvil(1200.0, 5), bar, st, 0.2 if st % 8 == 0 else 0.14, 0.4)
        s.hit("metal", anvil(1900.0, 4), bar, 10, 0.12, -0.2)
        if 2 <= j < 4:
            for st in (4, 12):
                s.hit("drums", snare(True), bar, st, 0.15, 0.05)
        elif 4 <= j < 6:
            for st in (0, 8):
                s.hit("drums", kick(), bar, st, 0.85)
                kicks.append(s.pos(bar, st))
            for st, v in enumerate(AS_SNARE):
                s.hit("drums", snare(True), bar, st, v * 0.35, 0.05)
        elif j >= 6:
            march_kit(s, bar, kicks, scale=0.55)
    elif kind == "E":
        march_kit(s, bar, kicks, scale=0.85, kick_steps=(0, 4, 8, 10, 12))
        if j % 4 == 3:
            for st, p in ((12, 160), (13, 140), (14, 110), (15, 90)):
                s.hit("drums", tom(p), bar, st, 0.5, (st - 13.5) / 3)
    elif kind == "F":
        if j < 2:
            s.hit("drums", kick(), bar, 0, 0.9)
            kicks.append(s.pos(bar, 0))
        elif j < 4:
            march_kit(s, bar, kicks, scale=0.4, kick_steps=(0, 8))
        else:
            march_kit(s, bar, kicks)

    # rolls and risers (the build-ups)
    if kind == "C" and j >= 4 or kind in ("D", "D2") and j >= 6 or kind == "F" and j >= 6:
        roll(s, bar)
    elif kind in ("Br", "E") and j >= 6:
        roll(s, bar, eighths=(j == 6))
    if (kind, j) in (("C", 4), ("F", 4)):
        s.hit("fx", riser(s.bar * 4), bar, 0, 0.3)
    elif (kind, j) in (("D", 6), ("D2", 6), ("Br", 6), ("E", 6)):
        s.hit("fx", riser(s.bar * 2), bar, 0, 0.3)

    # bass and low brass
    if kind == "C" and j < 4 or kind == "Br" or kind == "F" and j < 2:
        held_bass(s, bar, root, 0.3 if kind == "Br" else 0.35)
    else:
        march_bass(s, bar, root)
    if kind in ("A", "D", "D2", "E") or kind == "F" and j >= 4:
        low_ostinato(s, bar, root)

    # the machine
    riff_gain = {"B": 0.4, "B2": 0.4, "D": 0.18, "D2": 0.22, "Br": 0.3}.get(kind)
    if kind == "F" and j >= 4:
        riff_gain = 0.15 + 0.03 * (j - 4)
    if riff_gain:
        machine_riff(s, bar, root, riff_iv, riff_gain)

    # trumpet stabs
    if kind in ("B", "B2"):
        offbeat_stabs(s, bar, pad)
    if kind in ("A", "D", "D2", "E") and j % 4 in (2, 3):
        stab_figure(s, bar, pad)

    # pad and organ
    s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4 if kind == "Br" else 0.35, bright=1000.0)
    if kind == "Br" and j % 2 == 0 and j < 4:
        s.chord("organ", v_organ, pad, bar, dur_steps=32, gain=0.3, spread=0.5)


def section_melody(s, kind, st0):
    if kind == "A":
        s.play("brass", v_trumpet, AS_TRUMPETS, st0, gain=0.6, position=0.1)
        s.play("brass", v_brass, AS_TRUMPETS, st0, gain=0.35, octave=-1)
    elif kind == "C":
        s.play("brass", v_brass, AS_AUG, st0, gain=0.6)
        s.play("organ", v_organ, AS_AUG, st0, gain=0.3, octave=1)
    elif kind in ("D", "D2"):
        s.play("brass", v_trumpet, AS_TRUMPETS, st0, gain=0.65, position=0.1)
        s.play("lowbrass", v_brass, CANON, st0, gain=0.45, octave=-2)
        if kind == "D2":
            s.play("organ", v_organ, AS_TRUMPETS, st0, gain=0.25, octave=-1)
    elif kind == "Br":
        s.play("brass", v_brass, vorne_motif("F#3"), st0 + 4, gain=0.45)
        s.play("organ", v_organ, vorne_motif("F#3"), st0 + 4, gain=0.25, octave=-1)
    elif kind == "B2":
        for off, root in ((0, "F#4"), (2, "B3"), (4, "F#4"), (6, "B4")):
            s.play("brass", v_trumpet, dim_motif(root), st0 + off, gain=0.5, position=0.1)
            s.play("lowbrass", v_brass, dim_motif(root), st0 + off, gain=0.32, octave=-2)
    elif kind == "E":
        s.play("brass", v_brass, vorne_motif("F#3"), st0, gain=0.5)
        s.play("organ", v_organ, vorne_motif("F#3"), st0, gain=0.25, octave=-1)
        s.play("brass", v_trumpet, vorne_motif("F#4"), st0 + 4, gain=0.6, position=0.1)
        s.play("lowbrass", v_brass, MOTIF_CANON, st0 + 4, gain=0.45, octave=-2)
    elif kind == "F":
        s.play("brass", v_brass, vorne_motif("F#3"), st0, gain=0.4)
        s.play("organ", v_organ, vorne_motif("F#3"), st0, gain=0.2, octave=-1)
        s.play("brass", v_trumpet, vorne_motif("B3"), st0 + 4, gain=0.5, position=0.1)
        s.play("brass", v_brass, vorne_motif("B3"), st0 + 4, gain=0.3, octave=-1)


def iron_sovereign():
    s = LoopSong(147, 4, plan_len())
    kicks = []
    intro(s)
    at = starts()
    for base in s.sections():
        for kind, j, name, bar in walk(base):
            section_bar(s, kicks, kind, j, name, bar)
        for kind, off in at.items():
            section_melody(s, kind, base + off)
    return s, mixdown(s, {  # the sketch's chains and seeds
        "drums": dict(hp=35, rev=(1.0, 0.1), seed=231, gain=0.9), "perc": PERC, "fx": FX,
        "metal": dict(hp=200, rev=(1.2, 0.2), seed=232, gain=0.55),
        "bass": dict(hp=40, duck=True, gain=0.6),
        "lowbrass": dict(hp=60, rev=(2.0, 0.2), seed=233, gain=0.6),
        "riff": dict(hp=150, dly=(3, 0.25, 0.2, 3500), gain=0.5),
        "pad": dict(hp=100, duck=True, rev=(3.0, 0.35), wid=1.5, seed=234, gain=0.45),
        "brass": dict(hp=70, rev=(2.4, 0.27), seed=235, gain=0.8),
        "organ": dict(hp=60, rev=(3.0, 0.35), seed=236, gain=0.5),
    }, kicks, depth=0.35)


def main(argv):
    out = OUT
    if "--out" in argv:
        out = Path(argv[argv.index("--out") + 1])
    s, mix = iron_sovereign()
    mix = mix - mix.mean(axis=1, keepdims=True)
    audio, loop_start, loop_len = s.assemble(mix)
    bar_n = 16 * s.step_n
    audio = add_tail(audio, loop_start, 2, bar_n)
    audio = master(audio, -14.0, CEILING)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{NAME}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
          f"  (samples {loop_start}+{loop_len}, {s.loop} bars)  seam {seam_ratio(dec, loop_start, loop_len):.2f}"
          f"  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak {tp:.1f} dBTP"
          f"  {path.stat().st_size / 1e6:.2f} MB", flush=True)
    for kind, off in starts().items():
        n = len(dict(PLAN)[kind])
        a = loop_start + off * bar_n
        halves = [segment_lufs(dec[:, a + h * 4 * bar_n:a + (h + 1) * 4 * bar_n]) for h in (0, 1)]
        print(f"  {kind:<3} bars {off + 1}-{off + n}  {segment_lufs(dec[:, a:a + n * bar_n]):6.1f} LUFS"
              f"  (bars 1-4 {halves[0]:.1f}, 5-8 {halves[1]:.1f})", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
