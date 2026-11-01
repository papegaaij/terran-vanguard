#!/usr/bin/env python3
"""Concept round 78 music (M10 part F, step AUD-F): track 32 "Three Aces" (the duel theme: Level 40's Honour Guard,
after the mini-boss sting, for the whole fight) at full length, one full mix, no stems, from round 72's chosen sketch
(music_r72.py `three_aces`, duel-theme-r72-b: 175 BPM, A minor; M.2 b). Brief: design/audio/music/README.md, *Act 6
music* (the duel theme: "Vorne's motif in gold brass against the main motif's horn call, call and response like a
joust, then in canon"; the pick: "a drum and bass breakbeat with synth brass stabs and a distorted lead: Vorne's
motif as a fast sequencer arpeggio, the main motif in a soaring lead"; production "about 2:00 loop, one full mix, no
stems (a fight switches nothing, as track 19)").

The method is music_r68.py's (track 19): the sketch's voices, grooves, chords, figures, mix chains and seeds, laid out
as its 4-bar intro + an 88-bar loop (2:00.7) of eleven 8-bar sections, so the 2:00 does not play the sketch's 40-bar
loop three times. The sketch's five sections (A the break, B the sequencer, C the lead, D half time, E everything)
stay as they were; six sections are added on their material, with a breakdown and builds a three-ace duel can sit
under (the fight's deaths switch nothing: the loop simply runs). The brief's joust and canon, which the sketch left
to the sequencer and the lead, are written out with the sketch's own voices (A = Am Am F F Dm Dm E E,
C = Am F C E Am F Dm E):

  intro 4 bars  the sketch's: the sequencer alone (Vorne's motif, A C E G#, in 16ths) filtered up, the break from bar
          3, a riser
  A    A  the break (kick, snare and ghost notes, 16th hats), the reese sub bass, the synth-brass stabs on the
          off-beats
  B    A  A plus Vorne's motif in the sequencer, the stabs every other bar
  C    C  B plus the main motif (A E A B C) soaring in the distorted lead (A, then from C, then A)
  J    A  the joust (new): the sequencer out; Vorne's motif stated in held synth brass (bars 1 and 5) answered by the
          main motif in the lead (bars 3 and 7), call and response over the break and the reese
  D    A  half time: the sequencer and the stabs alone over the kick and snare, a snare roll in bar 8
  T    A  three aces (new): the sequencer in a three-voice canon (Vorne's motif entering on A3 in bar 1, an octave up
          an 8th behind in bar 3, an octave up a dotted 8th behind in bar 5, panned left, right, centre), the break in
          half time to bar 4 and full from bar 5, a 4-bar riser and a snare roll into E
  E    C  everything (the sketch's): the stabs every bar, the sequencer jumping an octave every other bar, the lead
  Br   A  breakdown (new): no drums for 4 bars, the reese held one note a bar under held brass chords, the sequencer
          low; from bar 5 the kick on the one and the three, the lead's main motif, a riser and a snare roll
  K    C  canon (new): both motifs at once: the lead's main motif with itself a bar behind an octave down, Vorne's
          motif in the held brass (bars 1 and 5) and the sequencer under them, the full break
  B2   A  B again
  A2   A  A again, a snare fill in bar 8 back into A

No bubbly or popping sounds (the sketch had none: no resonant plucks, the sequencer through a soft low-pass at q 0.8,
the stabs and brass with soft attacks). Every group goes through the sketch's own chain (music_r72.three_aces'
mixdown spec, same seeds, the same kick duck); the held brass, new, through the stabs' chain without its duck and its
own reverb seed. The render and mastering are music_r68.py's (intro + loop + 2-bar fade tail, -14 LUFS, the ceiling
-3 dBFS to keep the true peak under -1 dBTP; LOOPSTART / LOOPLENGTH Vorbis comments, sample-exact loop: a 16th at 175
BPM is exactly 3780 samples).

Output (the --out directory, default design/audio/music/concept/):
  duel-theme-full-r78-a.ogg   the full mix
  duel-theme-seam-r78-a.ogg   listening aid: the last 8 s of the loop, then the first 8 s after LOOPSTART
No SOURCE comment: a concept file; pipeline's copyPlaceholderMusic imports it into assets/music/duel-theme.ogg.

Usage: python3 tools/concept/audio/music_r78.py [--out DIR]   (~3 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seam and the loudness per
section (printed).
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import crash, hat, kick, ns, riser, snare, transpose, v_brass  # noqa: E402
from music_r02 import LoopSong, add_tail, seam_ratio, v_reese  # noqa: E402
from music_r03 import human_motif, mixdown, vorne_motif  # noqa: E402
from music_r41 import fade_ends, segment_lufs  # noqa: E402
from music_r60 import v_seq  # noqa: E402
from music_r72 import TA_CH, TA_KICK, TA_LOOP, TA_SNARE, TA_STABS, v_dlead  # noqa: E402
from synth import SR, db, decode, lufs, master, note_freq, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "duel-theme-full-r78-a"
CEILING = -3.0  # dBFS, the limiter's ceiling (music_r68's: at -2 the encode can reach the -1 dBTP limit)
SEAM_SECONDS = 8.0

SK_A, SK_C = TA_LOOP[:8], TA_LOOP[16:24]
PLAN = [("A", SK_A), ("B", SK_A), ("C", SK_C), ("J", SK_A), ("D", SK_A), ("T", SK_A), ("E", SK_C), ("Br", SK_A),
        ("K", SK_C), ("B2", SK_A), ("A2", SK_A)]
SEQ_IN = ("B", "C", "D", "E", "K", "B2")  # the sequencer's sections (T and Br play their own)
STABS_EVERY_BAR = ("A", "D", "E", "A2")   # the others every other bar (the sketch's B and C)
SEQ = (0, 3, 7, 11)                       # Vorne's motif, 1 - b3 - 5 - 7, as the sequencer's 16ths


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


# ---------------------------------------------------------------- the sketch's parts, one bar each

def break_kit(s, bar, kicks, odd, gain=1.0):
    """The sketch's break: kick on 1 and the and-of-3 (and the a-of-4 every other bar), snare and ghosts, 16th hats."""
    for st, g in TA_KICK:
        s.hit("drums", kick(150), bar, st, g * gain)
        kicks.append(s.pos(bar, st))
    if odd:
        s.hit("drums", kick(150), bar, 13, 0.5 * gain)
        kicks.append(s.pos(bar, 13))
    for st, g in TA_SNARE:
        s.hit("drums", snare(True), bar, st, g * gain, 0.05)
    for st in range(16):
        s.hit("hats", hat(st % 8 == 6), bar, st, (0.16 if st % 2 == 0 else 0.08) * gain, 0.3)


def half_kit(s, bar, kicks, steps=(0,)):
    """The sketch's D: the kick on the one, the snare on the three."""
    for st in steps:
        s.hit("drums", kick(150), bar, st, 0.9)
        kicks.append(s.pos(bar, st))
    s.hit("drums", snare(), bar, 8, 0.6, 0.05)


def roll(s, bar, start=8):
    for st in range(start, 16):
        s.hit("drums", snare(True), bar, st, 0.25 + 0.06 * (st - 8), 0.05)


def reese(s, root, bar, held=False):
    if held:
        s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 15.5, 4)), bar, 0, 0.35)
        return
    s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 7.5, 4)), bar, 0, 0.42)
    s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 5.5, 4)), bar, 10, 0.36)


def stabs(s, chord, bar):
    for st in TA_STABS:
        s.chord("stabs", v_brass, chord, bar, st, dur_steps=1, gain=0.42, stab=True)


def seq(s, bar, gain=1.0, shift=0, delay=0, pan=0.3):
    """Vorne's motif as the sketch's fast sequencer arpeggio (A3 C4 E4 G#4 in 16ths), `delay` 16ths late."""
    for st in range(16 - delay):
        nm = transpose("A3", SEQ[st % 4] + shift)
        s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st + delay,
              (0.3 if st % 4 == 0 else 0.2) * gain, pan if st % 2 else -pan)
    for st in range(16 - delay, 16):  # the canon's tail: the bar's last notes run on into the next bar's start
        nm = transpose("A3", SEQ[st % 4] + shift)
        s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar + 1, st + delay - 16,
              (0.3 if st % 4 == 0 else 0.2) * gain, pan if st % 2 else -pan)


def lead(s, bar, root="A4", gain=0.5, position=0.1):
    s.play("lead", v_dlead, human_motif(root), bar, gain=gain, position=position)


def brass_call(s, bar, gain=0.5):
    """Vorne's motif stated in held synth brass (the joust's call), the stabs' voice without the stab."""
    s.play("brass", v_brass, vorne_motif("A3"), bar, gain=gain, position=-0.15)
    s.play("brass", v_brass, vorne_motif("A3"), bar, gain=gain * 0.55, position=0.15, octave=-1)


def intro(s, kicks):
    """The sketch's intro: the sequencer alone, filtered up, the break from bar 3."""
    for bar in range(4):
        for st in range(16):
            nm = transpose("A3", SEQ[st % 4])
            s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st, 0.12 + 0.05 * bar,
                  0.3 if st % 2 else -0.3)
        if bar >= 2:
            for st, g in TA_KICK:
                s.hit("drums", kick(150), bar, st, g)
                kicks.append(s.pos(bar, st))
            for st, g in TA_SNARE:
                s.hit("drums", snare(True), bar, st, g * 0.8, 0.05)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.2)


def section_bar(s, kicks, kind, j, name, bar):
    chord, root = TA_CH[name]
    odd = j % 2 == 1
    if j == 0:
        s.hit("perc", crash(), bar, 0, 0.35, 0.3)
    # the drums
    if kind == "D":
        half_kit(s, bar, kicks)
        if j == 7:
            roll(s, bar)
    elif kind == "T" and j < 4:
        half_kit(s, bar, kicks, steps=(0, 10))
    elif kind == "Br" and j < 4:
        pass
    elif kind == "Br":
        half_kit(s, bar, kicks, steps=(0, 8))
        if j == 7:
            roll(s, bar)
    else:
        break_kit(s, bar, kicks, odd)
        if j == 7 and kind in ("T", "A2"):
            roll(s, bar, 12 if kind == "A2" else 8)
    # the reese
    reese(s, root, bar, held=kind == "Br" and j < 4)
    # the stabs
    if kind in STABS_EVERY_BAR or (kind in ("B", "C", "K", "B2", "T") and odd):
        stabs(s, chord, bar)
    if kind in ("Br",) and j < 4:
        s.chord("brass", v_brass, chord, bar, 0, dur_steps=16, gain=0.3, spread=0.7)
    # the sequencer (Vorne's motif)
    if kind in SEQ_IN:
        seq(s, bar, shift=12 if kind == "E" and odd else 0)
    elif kind == "T":
        seq(s, bar, pan=-0.45)
        if j >= 2:
            seq(s, bar, gain=0.8, shift=12, delay=2, pan=0.45)
        if j >= 4:
            seq(s, bar, gain=0.7, shift=12, delay=3, pan=0.05)
    elif kind == "Br":
        seq(s, bar, gain=0.55 if j < 4 else 0.8)
    # risers
    if kind in ("T", "Br") and j == 4:
        s.hit("fx", riser(s.bar * 4), bar, 0, 0.25)


def section_melody(s, kind, b0):
    """The section's lead and brass lines from its first bar `b0`."""
    if kind in ("C", "E"):  # the sketch's: the main motif soaring in the lead, answered from C
        lead(s, b0)
        lead(s, b0 + 2, "C5", gain=0.4, position=-0.1)
        lead(s, b0 + 4)
    elif kind == "J":  # the joust: Vorne's call in the brass, the main motif's answer in the lead
        for k in (0, 4):
            brass_call(s, b0 + k)
            lead(s, b0 + k + 2, gain=0.48)
    elif kind == "Br":
        lead(s, b0 + 4, gain=0.45)
    elif kind == "K":  # canon: the lead with itself a bar behind an octave down, Vorne's motif in the brass
        for k, root in ((0, "A4"), (2, "C5"), (4, "A4")):
            lead(s, b0 + k, root, gain=0.48 if root == "A4" else 0.4)
            lead(s, b0 + k + 1, transpose(root, -12), gain=0.32, position=-0.25)
        for k in (0, 4):
            brass_call(s, b0 + k, gain=0.4)


def three_aces_full():
    s = LoopSong(175, 4, plan_len())
    kicks = []
    intro(s, kicks)
    at = starts()
    for base in s.sections():
        for kind, j, name, bar in walk(base):
            section_bar(s, kicks, kind, j, name, bar)
        for kind, off in at.items():
            section_melody(s, kind, base + off)
    return s, mixdown(s, {  # the sketch's chains and seeds (music_r72.three_aces), the held brass new
        "drums": dict(hp=35, gain=0.85),
        "hats": dict(hp=800, gain=0.38),
        "perc": dict(hp=40, rev=(2.4, 0.25), seed=7231, gain=0.6),
        "fx": dict(gain=0.5),
        "bass": dict(hp=30, duck=True, gain=0.55),
        "stabs": dict(hp=150, duck=True, rev=(1.6, 0.25), wid=1.4, seed=7232, gain=0.5),
        "brass": dict(hp=120, rev=(1.8, 0.28), wid=1.3, seed=7235, gain=0.5),
        "lead": dict(hp=200, dly=(3, 0.3, 0.2, 4500), rev=(2.2, 0.25), wid=1.3, seed=7233, gain=0.5),
        "motif": dict(hp=120, rev=(2.0, 0.25), seed=7234, gain=0.8),
    }, kicks, depth=0.4)


def main(argv):
    out = OUT
    if "--out" in argv:
        out = Path(argv[argv.index("--out") + 1])
    out.mkdir(parents=True, exist_ok=True)
    s, mix = three_aces_full()
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
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    seam = np.concatenate([dec[:, end - n:end], dec[:, loop_start:loop_start + n]], axis=1)
    aid = write_ogg(out / "duel-theme-seam-r78-a.ogg", fade_ends(seam), max_peak_db=None)
    print(f"aid: {aid.name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
