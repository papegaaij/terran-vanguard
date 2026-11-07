#!/usr/bin/env python3
"""Step MUS-CHK (M11 "Act 7 - Beyond the Gate", 2026-11-05): round 86's standard applied to the two Act 7 tracks picked
in round 84 and never re-checked, track 17 b "Hollow Light" and track 28 a "Coalition Forever" (music_r84.py), and one
recomposed take of each that fails; pending the user's review (round 87). music_r84.py and music_r86.py stay as they
are; this one imports their voices, the round-86 Score and its self-check.

`--check-r84` ports round 86's self-check to the round-84 scores (read from music_r84.py's own tables): the phrase
map, the share of bars that carry a motif cell (and how many of those are the cell re-keyed or re-timed), every
section start and whether a phrase ended there, the strong-beat non-chord tones, and how long the loop runs before it
repeats wholesale (the bars whose melody and chord are new, the longest block heard twice).

Outputs (design/audio/music/concept/, or --out DIR):
  act7-b-theme-r87-a.ogg  "Hollow Light" (track 17, loops in Levels 44/46/47 and the Level 50 approach), 98 BPM,
      E minor. Round 84 b's sound kept (the bowed-glass pads, the soft heartbeat kick, the sub, the reversed glass
      swells, the breath; the distant trumpet; the wordless solo voice), written as tunes: theme G, a 16-bar bowed-glass
      tune in two answering phrases (a half cadence on B, then a full cadence on E); theme T, the distant trumpet's own
      tune in G major opened by the main motif (once per loop, as picked); theme G at its peak (glass, the voice an
      octave under, the trumpet's counter-line); a breakdown where the heartbeat stops and the wordless voice sings the
      Choir motif alone, its F over F major (a Phrygian cadence onto E minor), answered in its own phrase; a rebuild of
      theme G's consequent with the heartbeat back; a 2-bar turnaround onto B, back to bar 0.
  credits-r87-a.ogg  "Coalition Forever" (track 28, plays once), 140 BPM, D minor. Round 84 a's tracker trance kept
      (four-on-the-floor kick, off-beat bass, the 16th pulse arpeggio, claps, the supersaw pad and lead) and the title
      theme's approved tunes kept note for note (its A and B melodies, its slow C melody, the full title's bridge and
      calls), given a real form: an intro on the title's call, verse (title A answered by a new consequent, a full
      cadence on D), chorus (title B answered by a new consequent, a full cadence on F), the title's bridge, a
      breakdown (the slow C melody answered), a build on the title's two calls, the chorus and the verse again at the
      peak, and an outro on the title's brass call closing on D.

Loops use round 72's format (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH in samples); the credits are a
one-shot. -14 LUFS, -2 dBFS sample-peak ceiling, as rounds 84-86.

Usage: python3 tools/concept/audio/music_r87.py [b17|cred ...] [--out DIR] [--check] [--check-r84]
       python3 tools/concept/audio/music_r87.py --stems [--out DIR]   (M11 part B, step MUS-B: track 17's production
       stems act7-b-theme-{base,sparse}-r87-a.ogg from the chosen take's own render; see hollow_light_stems;
       M11 parts E and F, step MUS-EF, added act7-b-theme-{full,choir}-r87-a.ogg: the voice's Choir-motif statement
       lifted out of the chosen mix as its `choir` stem)
Deterministic (fixed seeds); not listened to by Claude: checked by the self-check, loudness, the loop seam and a
spectrogram.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (clap, crash, hat, kick, ns, riser, snare, transpose, v_arp, v_bass, v_brass, v_lead,  # noqa: E402
                   v_pad, v_strings_long)
from music_r02 import T_CH, T_MEL_A, T_MEL_B, T_MEL_C, LoopSong, add_tail, seam_ratio, v_choir, v_sub  # noqa: E402
from music_r03 import heartbeat, human_motif, mixdown, v_trumpet  # noqa: E402
from music_r08 import TF_BRIDGE, TF_CALL, TF_CALL2  # noqa: E402
from music_r84 import (ARP16, CF_LOOP, CHOIR_E4, HL_CH, HL_CHOIR, HL_LOOP, MAIN_D4, reversed_swell,  # noqa: E402
                       v_glass)
from music_r85 import _report  # noqa: E402
from music_r86 import Score, root_note  # noqa: E402
from synth import SR, adsr, db, decode, master, note_freq, sine, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"


# ================================================================ the check's additions

def cells(sc, line_index, bars):
    """Mark bars of a line as a motif cell: {bar from the line's start: 'as is' | 're-keyed' | 're-timed' | ...}."""
    ln = sc.lines[line_index]
    ln.motif = tuple(bars)
    ln.cells = dict(bars)


def repetition(sc, bar_s):
    """How long the loop runs before it repeats wholesale: bars whose melody and chord are new, the longest block of
    bars heard twice inside the loop, and the loop's length."""
    n = len(sc.chords)
    sig = []
    for b in range(n):
        ch = sc.chords[b]
        mel = tuple(sorted((a - b * 16, length, nm[:-1]) for ln in sc.lines for a, length, nm in ln.events
                           if b * 16 <= a < (b + 1) * 16))
        sig.append((ch, mel))
    seen, new = set(), 0
    for s in sig:
        if s not in seen and s[1]:
            new += 1
        seen.add(s)
    with_mel = sum(1 for s in sig if s[1])
    best = (0, 0, 0)
    for i in range(n):
        for j in range(i + 1, n):
            k = 0
            while j + k < n and i + k < j and sig[i + k] == sig[j + k]:
                k += 1
            if k > best[0] and any(sig[i + m][1] for m in range(k)):
                best = (k, i, j)
    print(f"  repetition: loop {n} bars = {n * bar_s:.1f} s; bars carrying a tune {with_mel}/{n}; new tune bars "
          f"(melody and chord not heard before) {new}; longest block heard twice {best[0]} bars "
          f"({best[1]} and {best[2]})")
    return dict(loop_s=n * bar_s, tune_bars=with_mel, new_bars=new, block=best[0])


def cell_report(sc):
    n = len(sc.chords)
    kinds = {}
    for ln in sc.lines:
        for b, kind in getattr(ln, "cells", {}).items():
            if 0 <= ln.bar + b < n:
                kinds.setdefault(ln.bar + b, set()).add(kind)
    other = sum(1 for k in kinds.values() if k - {"as is"})
    print(f"  motif-cell bars: {len(kinds)}/{n} ({100 * len(kinds) / n:.0f} %), of which re-keyed or re-timed {other}:"
          + "".join(f" {b}:{'/'.join(sorted(k))}" for b, k in sorted(kinds.items())))
    return dict(cells=len(kinds), rekeyed=other)


def full_check(sc, intro_bars, bar_s):
    r = sc.check(intro_bars, bar_s)
    r.update(cell_report(sc))
    r.update(repetition(sc, bar_s))
    return r


# ================================================================ the round-84 scores, read from music_r84.py

def hollow_light_r84_score():
    sc = Score("act7-b-theme-r84-b (Hollow Light, round 84)", HL_LOOP, loop=True)
    sc.sections = [(0, "A: glass, heartbeat, sub; the Choir motif at 4"), (8, "B: the same, the Choir motif at 12"),
                   (16, "C: the trumpet's main motif at 16")]
    for k in HL_CHOIR:
        sc.add(k, CHOIR_E4, "choir")
        cells(sc, -1, {0: "as is", 1: "as is"})
    sc.add(16, human_motif("E4"), "trumpet")
    cells(sc, -1, {0: "re-keyed", 1: "re-keyed"})
    for k in HL_CHOIR:
        sc.phrase(k, k + 1, "A" if k < 8 else "B", "the Choir motif (2 bars, unanswered)", "choir")
    sc.phrase(16, 17, "C", "the main motif in E (2 bars, unanswered)", "trumpet")
    return sc


def coalition_r84_score():
    sc = Score("credits-r84-a (Coalition Forever, round 84)", CF_LOOP, loop=True)
    sc.sections = [(0, "A: groove and arp, the lead's motif at 0 and 4"), (8, "B: the title's B melody"),
                   (16, "C: breakdown, the slow C melody twice"), (24, "D: all in, the B melody an octave up")]
    for k in (0, 4):
        sc.add(k, MAIN_D4, "lead")
        cells(sc, -1, {0: "as is", 1: "as is"})
    sc.add(8, T_MEL_B, "lead")
    cells(sc, -1, {0: "re-keyed", 1: "re-keyed", 4: "re-keyed"})
    sc.add(16, T_MEL_C * 2, "lead")
    cells(sc, -1, {0: "re-timed", 1: "re-timed", 2: "re-timed", 4: "re-timed", 5: "re-timed", 6: "re-timed"})
    sc.add(24, T_MEL_B, "lead")
    cells(sc, -1, {0: "re-keyed", 1: "re-keyed", 4: "re-keyed"})
    P = sc.phrase
    P(0, 3, "A", "the main motif, then 2 bars of silence (unanswered)", "lead")
    P(4, 7, "A", "the main motif again, then 2 bars of silence", "lead")
    P(8, 15, "B", "the title's B melody", "lead", ("HC", "Bb", "A"))
    P(16, 19, "C", "the slow C melody", "lead", ("HC", "F", "A"))
    P(20, 23, "C", "the slow C melody again", "lead", ("HC", "C", "A"))
    P(24, 31, "D", "the title's B melody", "lead", ("HC", "Bb", "A"))
    return sc


R84 = {"b17": (hollow_light_r84_score, 98), "cred": (coalition_r84_score, 140)}


# ================================================================ track 17: Hollow Light (98 BPM, E minor)

@lru_cache(None)
def v_glass_lead(freq, dur):
    """The bowed glass as a melody: v_glass's stretched partials and beating, a quicker bow (0.12 s) so that the
    tune's shorter notes speak, a long ring."""
    n = ns(dur + 1.0)
    t = np.arange(n) / SR
    x = sum(a * sine(freq * r, n) for r, a in ((1.0, 1.0), (2.01, 0.3), (3.03, 0.1), (4.2, 0.04)))
    x *= 1 + 0.1 * np.sin(2 * np.pi * 0.7 * t + freq)
    return x * adsr(n, 0.12, 0.6, 0.8, 0.9, gate=dur) * 0.6


HL87_V = dict(HL_CH, D=["D3", "F#3", "A3", "D4"], F=["F3", "A3", "C4", "F4"], B7=["D#3", "F#3", "A3", "B3"])
# Theme G, the glass tune: a rising call (B E G), a long fall to a half close on F-sharp; again higher (B5 over G),
# reaching C6 and settling on F-sharp over B7 (half cadence). The consequent climbs to D6 over G and falls through
# C6 A5 F-sharp5 D-sharp5 to E (full cadence).
G_ANT = ["B4 - - - - - - - E5 - - - - - F#5 -", "G5 - - - - - - - - - - - E5 - - -",
         "A5 - - - - - - - - - - - G5 - E5 -", "F#5 - - - - - - - - - - - . . . .",
         "B4 - - - - - - - E5 - - - - - G5 -", "B5 - - - - - - - - - - - A5 - G5 -",
         "A5 - - - - - - - C6 - - - - - B5 -", "A5 - - - - - - - F#5 - - - - - - -"]
G_ANT_CH = ["Em", "C", "Am", "B", "Em", "G", "Am", "B7"]
G_CONS = ["B4 - - - - - - - E5 - - - - - F#5 -", "G5 - - - - - - - - - - - E5 - - -",
          "C6 - - - - - - - - - - - B5 - A5 -", "F#5 - - - - - - - A5 - - - - - - -",
          "D6 - - - - - - - B5 - - - - - - -", "C6 - - - - - - - - - - - B5 - A5 -",
          "A5 - - - - - - - F#5 - - - D#5 - - -", "E5 - - - - - - - - - - - . . . ."]
G_CONS_CH = ["Em", "C", "Am", "D", "G", "C", ("Am", "B7"), "Em"]
# Theme T, the distant trumpet: opened by the main motif in E (E B E F-sharp G, its only statement in the loop), then
# its own dotted, speaking line down to a half close on D, and an answer that rises from G and falls to G (G major).
T_TUNE = ["E4 - - - - - B4 - E5 - - - - - F#5 -", "G5 - - - - - - - E5 - - - D5 - - -",
          "D5 - - - - - - - B4 - - - - - - -", "A4 - - - - - - - - - - - . . . .",
          "G4 - - - - - C5 - E5 - - - - - G5 -", "E5 - - - - - - - C5 - - - - - - -",
          "D5 - - - - - - - A4 - - - - - B4 -", "G4 - - - - - - - - - - - . . . ."]
T_TUNE_CH = ["Em", "C", "G", "D", "C", "Am", "D", "G"]
# The breakdown's voice: the Choir motif (E D C B, F E) over A minor and F major to E minor (a Phrygian cadence: its
# F over the chord it belongs to), answered in its own phrase (C E D, D-sharp over B).
VOICE_ANS = ["C4 - - - - - - - E4 - - - D4 - - -", "D#4 - - - - - - - - - - - . . . ."]
TURN17 = ["E5 - - - - - - - G5 - - - - - - -", "F#5 - - - - - - - D#5 - - - - - - -"]  # onto B4 at bar 0
HL87_CH = (G_ANT_CH + G_CONS_CH + T_TUNE_CH + G_ANT_CH + G_CONS_CH
           + ["Em", "C", "Am", ("F", "Em"), "C", "B"] + G_CONS_CH + ["C", "B"])
HL87_SECTIONS = [(0, "A: theme G in the glass over the glass pads; the heartbeat on beat 1, then both beats and the "
                    "sub (8)"),
                 (16, "T: the distant trumpet's tune (the main motif opens it), the breath and the reversed swells"),
                 (24, "peak: theme G in the glass doubled an octave down, a glass shimmer, the voice under the "
                      "consequent (32)"),
                 (40, "breakdown: the heartbeat and the sub stop; the glass alone, then the voice: the Choir motif "
                      "(42) and its answer"),
                 (46, "rebuild: theme G's consequent, the heartbeat back (46), both beats (48), the sub (50)"),
                 (54, "turnaround: C - B, the glass's line onto bar 0")]
GLASS = [("glassmel", v_glass_lead, 0.55, 0.12, 0, {})]
GLASS2 = GLASS + [("glassmel", v_glass_lead, 0.32, -0.15, -1, {})]
VOICE = [("choir", v_choir, 0.5, -0.15, -1, dict(legato=0.97, vowel="oo", voices=1))]


def hollow_light_score():
    sc = Score("act7-b-theme-r87-a (Hollow Light)", HL87_CH, loop=True)
    sc.sections = HL87_SECTIONS
    sc.add(0, G_ANT, "glass G", GLASS)
    sc.add(8, G_CONS, "glass G", GLASS)
    sc.add(16, T_TUNE, "trumpet T", [("horn", v_trumpet, 0.42, 0.3, 0, {})])
    cells(sc, -1, {0: "re-keyed", 1: "re-keyed"})
    sc.add(24, G_ANT, "glass G", GLASS2)
    sc.add(32, G_CONS, "glass G", GLASS2)
    sc.add(32, G_CONS, "voice under G", VOICE)
    sc.add(42, CHOIR_E4 + VOICE_ANS, "voice", [("choir", v_choir, 0.6, -0.15, 0,
                                                dict(legato=0.97, vowel="oo", voices=1))])
    cells(sc, -1, {0: "as is", 1: "as is"})
    sc.add(46, G_CONS, "glass G", GLASS)
    sc.add(54, TURN17, "glass turn", GLASS)
    P = sc.phrase
    P(0, 7, "A", "theme G antecedent (glass)", "glass G", ("HC", "Am", "B7"))
    P(8, 15, "A", "theme G consequent (glass)", "glass G", ("PAC", "B7", "Em"))
    P(16, 19, "T", "theme T antecedent (trumpet, the main motif opens it)", "trumpet T", ("HC", "G", "D"))
    P(20, 23, "T", "theme T consequent (trumpet)", "trumpet T", ("PAC", "D", "G"))
    P(24, 31, "peak", "theme G antecedent (glass doubled)", "glass G", ("HC", "Am", "B7"))
    P(32, 39, "peak", "theme G consequent (glass doubled, the voice under)", "glass G", ("PAC", "B7", "Em"))
    P(40, 41, "breakdown", "the glass chords alone", None)
    P(42, 45, "breakdown", "the Choir motif in the voice, F over F (Phrygian), answered", "voice", ("HC", "C", "B"))
    P(46, 53, "rebuild", "theme G consequent (glass)", "glass G", ("PAC", "B7", "Em"))
    P(54, 55, "turnaround", "the glass's line onto bar 0", "glass turn", ("HC", "C", "B"))
    return sc


def _halves(ch):
    return ch if isinstance(ch, tuple) else (ch,)


def hollow_light_r87():
    sc = hollow_light_score()
    s = LoopSong(98, 4, len(HL87_CH))
    kicks = []
    for bar, name in enumerate(["Em", "Em", "C", "B"]):  # round 84's intro: the glass alone, the heartbeat from 2
        s.chord("glass", v_glass, HL87_V[name], bar, gain=0.35 + 0.08 * bar, spread=0.8)
        if bar >= 2:
            s.hit("drums", heartbeat(), bar, 0, 0.6)
            kicks.append(s.pos(bar, 0))
    for base in s.sections():
        sc.play(s, base)
        for i, ch in enumerate(HL87_CH):
            bar = base + i
            halves = _halves(ch)
            ln = 16 // len(halves)
            peak = 24 <= i < 40
            brk = 40 <= i < 46
            for h, name in enumerate(halves):
                s.chord("glass", v_glass, HL87_V[name], bar, h * ln, ln, gain=0.5 if peak else 0.34 if brk else 0.42, spread=0.8)
                if not brk and (i >= 8 and i < 40 or i >= 50):
                    s.hit("sub", v_sub(note_freq(root_note(name)), round(s.step * (ln - 0.5), 4)), bar, h * ln, 0.3)
            if not brk:  # the heartbeat: beat 1 alone in the first phrase and the rebuild's first two bars
                s.hit("drums", heartbeat(), bar, 0, 0.75 if i >= 16 else 0.65)
                kicks.append(s.pos(bar, 0))
                if 8 <= i < 46 or i >= 48:
                    s.hit("drums", heartbeat(), bar, 8, 0.42)
            if 16 <= i < 40 or i >= 50:  # the breath: band noise on the off-beats
                for st in (6, 14):
                    s.hit("hats", hat(), bar, st, 0.03 if not peak else 0.04, 0.4)
            if (16 <= i < 40 or i >= 50) and i % 4 == 0 or i == 54:  # reversed glass swells into the bar line
                v = HL87_V[halves[0]]
                s.hit("rev", reversed_swell(v_glass, note_freq(transpose(v[0], 24)), round(s.bar, 4)), bar - 1, 0,
                      0.16)
            if peak:  # the shimmer: the chord's tones in quarters, an octave and a half up, very soft
                for st in range(0, 16, 4):
                    v = HL87_V[halves[st // ln]]
                    s.hit("shim", v_glass_lead(note_freq(transpose(v[(st // 4 + i) % len(v)], 24)),
                                               round(s.step * 3, 4)), bar, st, 0.07, 0.4 - 0.25 * (st // 4))
    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.8),
        "hats": dict(hp=2500, gain=0.3),
        "sub": dict(hp=25, gain=0.45),
        "glass": dict(hp=120, rev=(4.5, 0.5), wid=1.7, seed=8431, gain=0.5),
        "rev": dict(hp=200, rev=(4.5, 0.5), wid=1.7, seed=8432, gain=0.5),
        "glassmel": dict(hp=200, rev=(4.0, 0.42), wid=1.3, seed=8701, gain=0.6),
        "shim": dict(hp=600, rev=(4.5, 0.55), wid=1.8, seed=8702, gain=0.5),
        "horn": dict(hp=300, rev=(5.0, 0.65), seed=8433, gain=0.55),
        "choir": dict(hp=120, rev=(4.5, 0.5), seed=8434, gain=0.55),
    }, kicks, depth=0.2)
    secs = [(-4, "intro: the glass alone, the heartbeat from its bar 2 (round 84's)")] + HL87_SECTIONS
    return s, mix, secs, sc


# ================================================================ track 28: Coalition Forever (140 BPM, D minor)

CR_CH = dict(T_CH, Am=(["E3", "A3", "C4", "E4"], "A1", ["C4", "E4", "A4"]),
             Eb=(["D#3", "G3", "A#3", "D#4"], "D#2", ["D#4", "G4", "A#4"]))
# The title theme's tunes are kept note for note (T_MEL_A, T_MEL_B, T_MEL_C from music_r02.py; TF_BRIDGE, TF_CALL,
# TF_CALL2 from the full title, music_r08.py). New here: the answers that close them.
# A's consequent: starts high (A5 F5 C5) instead of on the motif, climbs to D6 and falls through G5 E5 C-sharp5 to D.
A_CONS28 = ["A5 - - - - - - - F5 - - - - - C5 -", "E5 - - - - - - - - - - - G5 - E5 -",
            "F5 - - - - - D5 - A5 - - - - - G5 -", "F5 - - - - - - - D5 - - - - - - -",
            "G5 - - - - - A5 - Bb5 - - - - - A5 -", "D6 - - - - - - - C6 - - - Bb5 - - -",
            "G5 - - - - - - - E5 - - - - - C#5 -", "D5 - - - - - - - - - - - . . . ."]
A_CONS28_CH = ["F", "C", "Dm", "Bb", "Gm", ("Bb", "F"), ("Gm", "A"), "Dm"]
# B's consequent (F major): opens on C6 falling, climbs again from D5 to D6 and closes C6 G5 B-flat5 G5 E5 onto F.
B_CONS28 = ["C6 - - - - - A5 - - - F5 - A5 - - -", "E5 - - - - - - - A5 - - - - - G5 -",
            "F5 - - - - - - - D5 - - - - - E5 -", "D5 - - - - - F5 - Bb5 - - - - - A5 -",
            "G5 - - - - - - - D6 - - - - - C6 -", "C6 - - - - - - - G5 - - - - - Bb5 -",
            "Bb5 - - - - - - - G5 - - - E5 - - -", "F5 - - - - - - - - - - - . . . ."]
B_CONS28_CH = ["F", "Am", "Dm", "Bb", "Gm", "C", ("Bb", "C"), "F"]
# The slow C melody's answer: G4 to D5, F5 D5, B-flat4 C-sharp5, D5 (a full cadence).
C_ANS28 = ["G4 - - - - - - - - - - - D5 - - -", "F5 - - - - - - - D5 - - - - - - -",
           "Bb4 - - - - - - - C#5 - - - - - - -", "D5 - - - - - - - - - - - - - - -"]
OUTRO28 = TF_CALL[:3] + ["E5 - - - - - - - C#5 - - - - - - -", "D5 - - - - - - - - - - - - - - -",
                         "- - - - - - - - - - - - - - - -"]
TA_CH = ["Dm", "Bb", "F", "C", "Dm", "Bb", "Gm", "A"]           # the title's A chords
TB_CH = ["F", "Am", "Dm", "Bb", "F", "Am", "Bb", "A"]           # the title's B chords, C under the melody's A -> Am
CR87_CH = (["Dm", "Dm", "Bb", "A"] + ["Dm", "Dm", "Bb", "A"]     # 0-7 intro (the arp; the title's call at 4)
           + TA_CH + A_CONS28_CH + TB_CH + B_CONS28_CH            # 8-39 verse, chorus
           + ["Gm", "Eb", "Bb", "F", "Gm", "Eb", "C", "A"]        # 40-47 bridge (the full title's D section)
           + ["Dm", "Bb", "F", "A", "Gm", "Bb", ("Gm", "A"), "Dm"]  # 48-55 breakdown (C melody, its answer)
           + ["Dm", "Dm", "Bb", "A", "Gm", "Bb", "Dm", "A"]       # 56-63 build (the title's two calls)
           + TB_CH + B_CONS28_CH + TA_CH + A_CONS28_CH            # 64-95 chorus, verse at the peak
           + ["Dm", "Dm", "Bb", "A", "Dm", "Dm"])                 # 96-101 outro (the call closing on D)
CR87_SECTIONS = [(0, "intro: the arp alone, filter opening; the kick and the title's call in the lead (4)"),
                 (8, "verse: the title's A melody answered by its new consequent (16), kick, off-beat bass, hats"),
                 (24, "chorus: the title's B melody (F major) answered (32), claps, 16th hats, brass under the lead"),
                 (40, "bridge: the full title's D section (Gm - Eb - Bb - F), strings doubling, rolling bass"),
                 (48, "breakdown: drums out, the slow C melody over the pad and a closed arp, answered (52)"),
                 (56, "build: the title's two calls, the kick back, a snare roll and a riser, the filter opening"),
                 (64, "chorus at the peak: everything, the lead doubled an octave up"),
                 (80, "verse at the peak: the title's A melody and its answer in lead and brass"),
                 (96, "outro: the title's brass call closing on D, the kick out (98), the last chord")]
LEAD = [("lead", v_lead, 0.45, 0.1, 0, {})]
LEAD_BR = LEAD + [("brass", v_brass, 0.22, -0.15, -1, {})]
LEAD_PEAK = LEAD_BR + [("lead", v_lead, 0.13, -0.2, 1, {})]
LEAD_STR = LEAD + [("strmel", v_strings_long, 0.35, -0.2, -1, dict(attack=0.05))]


def coalition_score():
    sc = Score("credits-r87-a (Coalition Forever)", CR87_CH, loop=False)
    sc.sections = CR87_SECTIONS
    sc.add(4, TF_CALL, "lead call", [("lead", v_lead, 0.32, 0.1, 0, {})])
    cells(sc, -1, {0: "re-timed", 1: "re-timed", 2: "re-timed"})
    for b, orch in ((8, LEAD), (80, LEAD_BR)):
        sc.add(b, T_MEL_A, "lead A", orch)
        cells(sc, -1, {0: "as is", 1: "as is", 4: "as is"})
        sc.add(b + 8, A_CONS28, "lead A", orch)
    for b, orch in ((24, LEAD_BR), (64, LEAD_PEAK)):
        sc.add(b, T_MEL_B, "lead B", orch)
        cells(sc, -1, {0: "re-keyed", 1: "re-keyed", 4: "re-keyed"})
        sc.add(b + 8, B_CONS28, "lead B", orch)
    sc.add(40, TF_BRIDGE, "lead bridge", LEAD_STR)
    sc.add(48, T_MEL_C + C_ANS28, "lead C", [("lead", v_lead, 0.42, 0.0, 0, {}),
                                              ("strmel", v_strings_long, 0.3, -0.2, -1, dict(attack=0.2))])
    cells(sc, -1, {0: "re-timed", 1: "re-timed", 2: "re-timed"})
    sc.add(56, TF_CALL + TF_CALL2, "lead call", LEAD_BR)
    cells(sc, -1, {0: "re-timed", 1: "re-timed", 2: "re-timed", 4: "re-keyed", 5: "re-keyed"})
    sc.add(96, OUTRO28, "lead call", LEAD_BR + [("brass", v_brass, 0.3, 0.15, 0, {})])
    cells(sc, -1, {0: "re-timed", 1: "re-timed", 2: "re-timed"})
    P = sc.phrase
    P(0, 3, "intro", "the arp alone", None)
    P(4, 7, "intro", "the title's call", "lead call", ("HC", "Bb", "A"))
    for b, sec in ((8, "verse"), (80, "verse 2")):
        P(b, b + 7, sec, "the title's A melody (antecedent)", "lead A", ("HC", "Gm", "A"))
        P(b + 8, b + 15, sec, "A's new consequent", "lead A", ("PAC", "A", "Dm"))
    for b, sec in ((24, "chorus"), (64, "chorus 2")):
        P(b, b + 7, sec, "the title's B melody (antecedent)", "lead B", ("HC", "Bb", "A"))
        P(b + 8, b + 15, sec, "B's new consequent", "lead B", ("PAC", "C", "F"))
    P(40, 47, "bridge", "the full title's bridge", "lead bridge", ("HC", "C", "A"))
    P(48, 51, "breakdown", "the title's slow C melody", "lead C", ("HC", "F", "A"))
    P(52, 55, "breakdown", "its answer", "lead C", ("PAC", "A", "Dm"))
    P(56, 59, "build", "the title's first call", "lead call", ("HC", "Bb", "A"))
    P(60, 63, "build", "the title's second call", "lead call", ("HC", "Dm", "A"))
    P(96, 101, "outro", "the call closing on D", "lead call", ("PAC", "A", "Dm"))
    return sc


def coalition_r87():
    sc = coalition_score()
    n = len(CR87_CH)
    s = LoopSong(140, 0, n)
    s.total_steps = 16 * (n + 3)  # a one-shot: the bars and a 3-bar ring, no second pass
    s.tl.n = s.total_steps * s.step_n
    kicks = []
    sc.play(s, 0)
    for i, ch in enumerate(CR87_CH):
        halves = _halves(ch)
        ln = 16 // len(halves)
        sec = next(lb for b, lb in reversed(CR87_SECTIONS) if b <= i).split(":")[0]
        drums = sec in ("verse", "chorus", "bridge", "chorus at the peak", "verse at the peak") or \
            (sec == "intro" and i >= 4) or (sec == "build") or (sec == "outro" and i < 98)
        peak = sec in ("chorus at the peak", "verse at the peak")
        if i in (8, 24, 40, 64, 80, 96) or (i % 8 == 0 and drums and i > 8):
            s.hit("perc", crash(), i, 0, 0.45 if i in (24, 64, 80) else 0.35, 0.3)
        if drums:
            kick_lvl = 0.7 if sec == "intro" or (sec == "build" and i < 60) else 0.95
            for beat in range(4):
                s.hit("drums", kick(150), i, beat * 4, kick_lvl)
                kicks.append(s.pos(i, beat * 4))
        if drums and sec not in ("intro", "build", "outro") and (sec != "verse" or i >= 16):
            for st in (4, 12):
                s.hit("drums", clap(), i, st, 0.5)
        if drums and sec not in ("intro", "outro"):
            for st in range(2, 16, 4):
                s.hit("hats", hat(True), i, st, 0.18 if sec != "build" else 0.12, 0.3)
            if sec in ("chorus", "chorus at the peak", "verse at the peak", "bridge"):
                for st in range(16):
                    s.hit("hats", hat(), i, st, 0.07, -0.3)
        if sec in ("build",) and i >= 60:  # the snare roll: 8ths, then 16ths in the last bar, rising
            for st in range(0, 16, 1 if i == 63 else 2 if i >= 62 else 4):
                s.hit("drums", snare(True), i, st, 0.1 + 0.04 * (i - 60) + 0.01 * st, 0.05)
        if i in (7, 23, 39, 47, 79):  # a 1-beat snare fill into the next section
            for st in (12, 13, 14, 15):
                s.hit("drums", snare(True), i, st, 0.18 + 0.05 * (st - 12), 0.05)
        for h, name in enumerate(halves):
            pad, root, tones = CR_CH[name]
            st0 = h * ln
            # the off-beat bass (rolling in the bridge and the peaks)
            if drums and sec not in ("intro", "build") and not (sec == "outro"):
                steps = [st for st in range(st0, st0 + ln) if st % 4] if sec in ("bridge",) or peak else \
                    list(range(st0 + 2, st0 + ln, 4))
                for st in steps:
                    nt = transpose(root, 12 if st % 4 != 3 else 24) if (sec == "bridge" or peak) else \
                        transpose(root, 12)
                    s.hit("bass", v_bass(note_freq(nt), round(s.step * (0.8 if len(steps) > 4 else 1.5), 4)), i,
                          st, 0.42)
            elif sec == "build" and i >= 60 or sec == "outro" and i < 98:
                for st in range(st0 + 2, st0 + ln, 4):
                    s.hit("bass", v_bass(note_freq(transpose(root, 12)), round(s.step * 1.5, 4)), i, st, 0.36)
            if sec == "breakdown" or sec == "outro" and i >= 98:
                s.hit("sub", v_sub(note_freq(transpose(root, 12)), round(s.step * (ln - 0.5), 4)), i, st0, 0.35)
            # the pad
            g = {"intro": 0.12 + 0.03 * i, "breakdown": 0.42, "outro": 0.32}.get(sec, 0.35 if peak else 0.27)
            if sec == "outro" and i == n - 2:
                s.chord("pad", v_pad, pad, i, 0, 32, gain=0.4, spread=0.8)
            elif not (sec == "outro" and i == n - 1) and (sec != "intro" or i >= 2):
                s.chord("pad", v_pad, pad, i, st0, ln, gain=g, spread=0.8)
            # the 16th pulse arp, its filter by section
            ext = tones + [transpose(tones[0], 12), transpose(tones[1], 12)]
            cut = {"intro": 700.0 + 130 * i, "verse": 1400.0, "chorus": 2000.0, "bridge": 1800.0,
                   "breakdown": 900.0, "build": 900.0 + 190 * (i - 56), "outro": 1600.0 - 120 * (i - 96)}.get(
                sec, 2400.0)
            if not (sec == "outro" and i >= n - 1):
                for st in range(st0, st0 + ln):
                    s.hit("arp", v_arp(note_freq(ext[ARP16[st]]), round(s.step * 0.7, 4), round(cut, -1)), i, st,
                          0.24 if sec in ("intro", "breakdown") else 0.3, 0.3 if st % 2 else -0.3)
    s.hit("perc", crash(), n - 2, 0, 0.45, -0.2)  # the landing on D
    for b, bars in ((2, 2), (22, 2), (38, 2), (54, 2), (60, 4), (78, 2)):
        s.hit("fx", riser(s.bar * bars), b, 0, 0.22)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "hats": dict(hp=700, gain=0.38),
        "perc": dict(hp=40, gain=0.5),
        "fx": dict(gain=0.5),
        "bass": dict(hp=35, duck=True, gain=0.55),
        "sub": dict(hp=25, gain=0.45),
        "arp": dict(hp=180, duck=True, dly=(3, 0.3, 0.2, 4000), seed=8481, gain=0.4),
        "pad": dict(hp=100, duck=True, rev=(3.0, 0.4), wid=1.6, seed=8482, gain=0.45),
        "lead": dict(hp=200, dly=(3, 0.3, 0.2, 4500), rev=(2.2, 0.25), wid=1.3, seed=8483, gain=0.55),
        "brass": dict(hp=90, rev=(2.6, 0.28), seed=8711, gain=0.6),
        "strmel": dict(hp=110, rev=(3.0, 0.38), wid=1.5, seed=8712, gain=0.5),
    }, kicks, depth=0.45)
    return s, mix, n, CR87_SECTIONS, sc


# ================================================================ render

LOOPS = {"b17": ("act7-b-theme-r87-a", hollow_light_r87)}
ONCE = {"cred": ("credits-r87-a", coalition_r87)}
SCORES = {"b17": (hollow_light_score, 98, 4), "cred": (coalition_score, 140, 0)}


def _sections(name, secs, t0, bar_s, end_s):
    print(f"{name} sections:")
    for k, (bar, label) in enumerate(secs):
        t = t0 + bar * bar_s
        nxt = t0 + secs[k + 1][0] * bar_s if k + 1 < len(secs) else end_s
        print(f"  {int(t // 60)}:{t % 60:05.2f}-{int(nxt // 60)}:{nxt % 60:05.2f}  {label}")


def render_loop(key, out):
    name, fn = LOOPS[key]
    s, mix, secs, sc = fn()
    full_check(sc, s.intro, s.bar)
    mix = mix - mix.mean(axis=1, keepdims=True)
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    mix = master(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, -2.0)[:, :mix.shape[1]]
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    end = loop_start + loop_len
    rms = lambda x: db(np.sqrt(np.mean(x ** 2)))  # noqa: E731
    _report(path, f"  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s  (samples {loop_start}+{loop_len})"
                  f"  seam {seam_ratio(dec, loop_start, loop_len):.2f}  RMS {rms(dec[:, end - ns(0.25):end]):.1f} dB"
                  f" before the loop end, {rms(dec[:, loop_start:loop_start + ns(0.25)]):.1f} dB after the start")
    _sections(name, secs, s.intro * s.bar, s.bar, (loop_start + loop_len) / SR)


def render_once(key, out):
    name, fn = ONCE[key]
    s, mix, bars, secs, sc = fn()
    full_check(sc, 0, s.bar)
    end = int(bars * s.bar * SR) + ns(3.0)
    mix = mix[:, :end]
    mix = mix - mix.mean(axis=1, keepdims=True)
    fo = ns(4.5)
    mix[:, -fo:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fo))) ** 1.5
    mix = master(mix, -14.0, -2.0)
    mix[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{name}.ogg", mix)
    _report(path, "  (one-shot, no loop)")
    _sections(name, secs, 0.0, s.bar, end / SR)


# ================================================================ production stems (M11 part B, step MUS-B)
# Added 2026-11-05; nothing above changes. music_r86.py's stems method: the chosen take rendered again by its own
# function, every mixdown group kept apart, the chosen mix's mastering gain on every stem, the chosen file reproduced
# byte for byte on the way. The full stem is the chosen file itself (act7-b-theme-r87-a.ogg).

# M11 parts E and F, step MUS-EF (2026-11-06): the `choir` stem. The voice's one Choir-motif statement (the
# breakdown, loop bars 42-45: the motif and its answer) is the only line played into the "choir" group with these
# tokens; the stems render plays it a second time into a "choirline" group of its own (v_choir is deterministic, the
# mix only reads the groups its spec names, so the chosen mix stays byte-identical), which goes through the choir
# group's own chain. The voice under theme G (bars 32-39) is a doubling of the glass tune, not the Choir motif: it
# stays in the full stem. full = the chosen mix without the statement, choir = the statement; full + choir = the mix.
STATEMENT17 = CHOIR_E4 + VOICE_ANS


def _statement_song(real):
    class StatementSong(real):
        def play(self, group, voice, bars_tokens, start_bar, gain=1.0, position=0.0, octave=0, legato=0.9, **kw):
            super().play(group, voice, bars_tokens, start_bar, gain, position, octave, legato, **kw)
            if group == "choir" and list(bars_tokens) == STATEMENT17:
                super().play("choirline", voice, bars_tokens, start_bar, gain, position, octave, legato, **kw)
    return StatementSong


def hollow_light_stems(out):
    """Track 17's production stems (design/audio/music, Act 7 music): base (the heartbeat, the sub, the glass pads:
    the pulse, drone and pad), sparse (the heartbeat and the sub alone: the pulse and the drone, Level 46's), and
    (step MUS-EF) full (the chosen mix without the voice's Choir-motif statement) and choir (the statement alone, the
    game's third, motif stream), full + choir = the chosen mix."""
    from music_r86 import capture_groups, write_stems
    me = sys.modules[__name__]
    real = me.LoopSong
    me.LoopSong = _statement_song(real)
    try:
        (s, mix, _, _), groups = capture_groups(me, hollow_light_r87, {"choirline": ("choir", [])})
    finally:
        me.LoopSong = real
    write_stems(s, mix, groups, OUT / "act7-b-theme-r87-a.ogg", {
        "act7-b-theme-base-r87-a.ogg": ["drums", "sub", "glass"],
        "act7-b-theme-sparse-r87-a.ogg": ["drums", "sub"],
        "act7-b-theme-full-r87-a.ogg": ["*", "-choirline"],
        "act7-b-theme-choir-r87-a.ogg": ["choirline"],
    }, out, sums=(("act7-b-theme-full-r87-a.ogg", "act7-b-theme-choir-r87-a.ogg"),))
    bar_n = 16 * s.step_n
    at = s.intro * bar_n + 42 * bar_n
    print(f"the choir statement: loop bar 42, file sample {at} ({at / SR:.3f} s; {at / SR + s.loop * s.bar:.3f} s on "
          f"the second pass)")


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    if "--stems" in argv:  # step MUS-B: track 17's production stems (hollow_light_stems)
        hollow_light_stems(out)
        return
    if "--check-r84" in argv:
        for key, (fn, bpm) in R84.items():
            full_check(fn(), 4, 240.0 / bpm)
        return
    keys = [a for a in argv if not a.startswith("--")] or [*LOOPS, *ONCE]
    if "--check" in argv:
        for key in keys:
            fn, bpm, intro = SCORES[key]
            full_check(fn(), intro, 240.0 / bpm)
        return
    out.mkdir(parents=True, exist_ok=True)
    for key in keys:
        if key in LOOPS:
            render_loop(key, out)
        else:
            render_once(key, out)


if __name__ == "__main__":
    main(sys.argv[1:])
