#!/usr/bin/env python3
"""Step MUS-H (M11 part H "Level 50 Choir Heart", 2026-11-07): track 20 "Choir Heart", the final boss, at full length
with its `choir` stem and the race's one-shot coda; pending the user's review in round 95. The user's pick QH-1 a: the
chosen round-03 sketch (music_r03.py `final`, final-boss-r03-a) **extended as written**: its tempo (150 BPM), its key
path (E minor, E major at the climax), its orchestra (the organ, the heartbeat, the choir, the low brass and the
trumpets, the strings' gallop and 16th figures, the taikos, timpani and the four-on-the-floor beat) and its order of
events (the Choir's call, humanity answers, both at once, the heart with Vorne's motif sung by the Choir, the build, E
major), written as tunes under the user's standing music rules (rounds 84-85): 8-16 bar phrases that ask and answer
and end on cadences, a B theme (the Choir's hymn, grown from the Choir motif), motifs only as quotes that open a
phrase, sections changing only where a phrase ended, no strong-beat note outside its chord unless it resolves by step
(the Choir motif's F always over F, its E over E minor: the Phrygian close, never the sketch's F over E minor), one
tempo and a steady pulse (the heartbeat on 1 and 3 where the drums rest). music_r03.py stays as it is: this file only
imports its voices, tables, chains and seeds.

Form (loop 112 bars = 2:59.2 at 1.6 s a bar, after the sketch's 4-bar intro):
  intro    the sketch's intro: organ, heartbeat, the Choir motif in choir and low brass, the main motif in a trumpet
           (its last note now F-sharp over B: a half cadence into the loop), a snare build and a riser
  A   0    the Choir's call (16): the Choir motif opens the choir's line (to a half cadence F - B), answered by the
           orchestral stabs; the motif again, a full cadence on E minor; the sketch's A groove
  B  16    humanity answers (16): theme H, the sketch's fanfare as written (two 4-bar phrases to B) answered by a new
           8-bar consequent to a full cadence on E minor; trumpets and lead over the four-on-the-floor and the gallop
  C  32    both at once (16): the Choir motif in the choir answered by the main motif in the trumpets, whose line goes
           on to a half cadence; again, the main motif now on G major, a full cadence (the two motifs alternate: never
           stacked, so the Choir's F never sounds against humanity's G)
  D  48    the Choir's hymn, the B theme (16): the choir's own tune in A minor grown from the Choir motif's falling
           fourth (a Phrygian half cadence Dm - E), answered with the whole motif (its tritone drop over F - Em) to a
           full cadence on E minor; half-time drums, organ, cellos and low brass doubling the choir
  E  64    the heart (8): drums out, the heartbeat on 1 and 3; Vorne's motif sung by the Choir, answered by the Choir
           motif low; a half cadence C - B
     72    the build (8): the horns climbing onto B over taikos, a snare roll and a 4-bar riser
  F  80    E major (16): the sketch's climax tune (theme H in major) as an antecedent to B, a consequent to a full
           cadence on E major; organ, full orchestra
  G  96    the Choir returns (8): the Choir motif over C pulls E major back to E minor, its line to a full cadence
    104    the turnaround (8): the trumpets' last call over the duel groove, a riser and a roll, a half cadence onto B,
           back to bar 0
The coda (one-shot, the race; 22.5 s): a 1.0 s pickup (timpani and snare swelling on B), then the main motif alone in
horns and strings over a driving pulse (kick, the strings' 16ths, off-beat bass, timpani), rising E minor - G - A -
B minor, climbing onto A7 and landing at exactly 17.0 s (K+29 when started at K+12) on one long held D major chord,
held to 18.5 s (the near side, K+30.5) and faded out by 22.5 s (the near side's first 4 s).

Stems: the full mix and the `choir` stem (the `choir` and `lowbrass` groups: the choir's pads, which sing in every bar,
its tunes and Vorne's motif, and the low brass doubling the Choir motif and the hymn), mastered with one linked gain
curve (music_r86.master_gain, measured on the full mix), sample-aligned, the same loop tags: the full mix holds the
choir, so the game's crossfade full -> choir at the Heart's death leaves the choir's level unchanged. The rest (the
full mix without the choir) is rendered to the scratch directory only, to measure rest + choir against the full mix.

Outputs (design/audio/music/concept/, or --out DIR):
  final-boss-full-r95-a.ogg    the full mix, LOOPSTART / LOOPLENGTH (intro + loop + 2-bar fade tail, round 72's format)
  final-boss-choir-r95-a.ogg   the `choir` stem, same length and tags
  final-boss-coda-r95-a.ogg    the race's coda, a one-shot (no loop tags)
-14 LUFS; the sample-peak ceiling -2 dBFS (the true peak checked under -1 dBTP).

Usage: python3 tools/concept/audio/music_r95.py [loop|coda ...] [--out DIR] [--rest DIR] [--check]
Deterministic (fixed seeds); not listened to by Claude: checked by the self-check (round 86/87's MUS-CHK measures:
the phrase map with its cadences, section starts against phrase ends, strong-beat non-chord tones, the motif share and
the repetition), loudness, true peak, the seam and the stem's sum.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (clap, crash, hat, kick, ns, riser, snare, taiko, timpani, tom, transpose, v_bass,  # noqa: E402
                   v_brass, v_lead, v_strings_long, v_strings_short)
from music_r02 import GALLOP, LoopSong, add_tail, seam_ratio, v_choir, v_dbass  # noqa: E402
from music_r03 import (DRUMS, FI_BASS, FI_CH, FX, PERC, heartbeat, mixdown, v_organ,  # noqa: E402
                       v_trumpet)
from music_r85 import PCS, _report  # noqa: E402
from music_r86 import Score, master_gain, tag_values  # noqa: E402
from music_r87 import full_check  # noqa: E402
from synth import SR, db, decode, lufs, master, note_freq, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
FULL, CHOIR, CODA = "final-boss-full-r95-a", "final-boss-choir-r95-a", "final-boss-coda-r95-a"
CEILING = -2.0
LOOP_BARS = 112
STEM_GROUPS = ("choir", "lowbrass")

# the sketch's mix chains and seeds (music_r03.final), plus the two groups the stem needs apart
SPEC = {
    "drums": DRUMS, "perc": PERC, "fx": FX,
    "heart": dict(hp=45, rev=(2.0, 0.2), seed=241, gain=0.9),
    "bass": dict(hp=40, duck=True, gain=0.55),
    "ost": dict(hp=80, rev=(1.8, 0.2), seed=242, gain=0.5),
    "strings": dict(hp=110, rev=(3.0, 0.35), wid=1.5, seed=243, gain=0.45),
    "cello": dict(hp=60, rev=(3.0, 0.35), wid=1.3, seed=249, gain=0.45),
    "choir": dict(hp=140, rev=(3.8, 0.45), wid=1.6, seed=244, gain=0.65),
    "lowbrass": dict(hp=70, rev=(2.6, 0.28), seed=248, gain=0.8),
    "brass": dict(hp=70, rev=(2.6, 0.28), seed=245, gain=0.8),
    "lead": dict(hp=180, dly=(3, 0.3, 0.22, 4500), rev=(2.2, 0.22), wid=1.4, seed=246, gain=0.55),
    "organ": dict(hp=50, rev=(3.5, 0.4), seed=247, gain=0.45),
}


# ================================================================ notation and chords

def bar_(spec):
    """One bar from 'NOTE:steps ...' ('.' a rest), 16 steps: 'E5:4 D5:4 C5:4 B4:4'."""
    toks = []
    for item in spec.split():
        nm, _, ln = item.partition(":")
        ln = int(ln)
        toks += ([nm] + ["-"] * (ln - 1)) if nm != "." else ["."] * ln
    if len(toks) != 16:
        raise ValueError(f"{spec!r} has {len(toks)} steps")
    return " ".join(toks)


def L(*specs):
    return [bar_(x) for x in specs]


QUAL = {"": (0, 4, 7), "m": (0, 3, 7), "7": (0, 4, 7, 10), "dim": (0, 3, 6)}
SHARP = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"]


def midi_name(m):
    return f"{SHARP[m % 12]}{m // 12 - 1}"


def voicing(name):
    """(pad notes, bass root): the sketch's FI_CH voicing where it has one, else the chord closed from D#3 up and its
    root between A1 and G#2 (as FI_CH's)."""
    if name in FI_CH:
        return FI_CH[name]
    root = name[:2] if len(name) > 1 and name[1] in "#b" else name[:1]
    pc = PCS[root]
    tones = {(pc + i) % 12 for i in QUAL[name[len(root):]]}
    notes, m = [], 51
    while len(notes) < 4:
        if m % 12 in tones:
            notes.append(midi_name(m))
        m += 1
    return notes, midi_name(33 + (pc - 9) % 12)


def halves(ch):
    """[(first step, steps, chord)] of a bar's chord (a tuple: two half-bar chords)."""
    return [(0, 16, ch)] if isinstance(ch, str) else [(0, 8, ch[0]), (8, 8, ch[1])]


def chord_of(ch, st):
    return ch if isinstance(ch, str) else ch[st * len(ch) // 16]


# ================================================================ the score

CHOIR_MOTIF = L("E5:4 D5:4 C5:4 B4:4", "F4:8 E4:8")          # 8-b7-b6-5, the tritone drop to b2, 1 (over Em | F Em)
MAIN_E = "E4:6 B4:2 E5:6 F#5:2"                              # the main motif's head on E (1-5-8-9)

CHORDS = (
    # A: the Choir's call
    ["Em", ("F", "Em"), "C", ("Am", "B"), "Em", "C", "F", "B",
     "Em", ("F", "Em"), "Am", ("C", "B"), "Em", "C", ("Am", "B7"), "Em"]
    # B: humanity answers (the sketch's B chords, then the consequent)
    + ["Em", "C", "Am", "B", "Em", "C", "D", "B",
       "Em", "C", "Am", "D", "G", "C", ("Am", "B7"), "Em"]
    # C: both at once
    + ["Em", ("F", "Em"), "Em", "C", "Am", ("C", "D"), ("Am", "B"), "B",
       "Em", ("F", "Em"), "G", "Em", "C", "Am", "B7", "Em"]
    # D: the Choir's hymn (the B theme)
    + ["Am", "F", "C", "G", "Am", "F", "Dm", "E",
       "Am", ("F", "Em"), "C", "G", "Am", "C", ("Am", "B7"), "Em"]
    # E: the heart, then the build
    + ["Em", "B", "C", ("Am", "B"), "Em", ("F", "Em"), "C", "B"]
    + ["Em", "C", "Am", ("C", "D"), "G", "C", "Am", "B"]
    # F: E major
    + ["E", "E", "C", "B", "A", "E", ("C#m", "F#"), "B",
       "E", "E", "C", "A", "C#m", "A", "B7", "E"]
    # G: the Choir returns, the turnaround
    + ["C", ("F", "Em"), "Am", ("F", "B"), "Em", "C", ("Am", "B7"), "Em"]
    + ["Em", "C", "F", "B", "Em", "C", "Am", "B"])
assert len(CHORDS) == LOOP_BARS

SECTIONS = [(0, "A the Choir's call: the Choir motif opens the choir's line, the stabs answer; the sketch's A groove"),
            (16, "B humanity answers: theme H (the sketch's fanfare + a new consequent), four-on-the-floor, gallop"),
            (32, "C both at once: the Choir motif (choir) answered by the main motif (trumpets) and their line"),
            (48, "D the Choir's hymn (the B theme): the choir's tune on the Choir motif, half-time drums, organ"),
            (64, "E the heart: drums out, heartbeat; Vorne's motif sung by the Choir, the Choir motif answers"),
            (72, "E' the build: horns climbing onto B, taikos, a snare roll and a riser"),
            (80, "F E major: the sketch's climax tune as antecedent, a consequent to E; organ, tutti"),
            (96, "G the Choir returns: the Choir motif over C pulls E major back to E minor"),
            (104, "G' turnaround: the trumpets' last call, riser and roll, a half cadence onto B")]

CHOIR_LEAD = [("choir", v_choir, 0.6, -0.1, 0, dict(legato=0.95))]
MOTIF_LEAD = CHOIR_LEAD + [("lowbrass", v_brass, 0.38, 0.1, -1, {})]
HYMN = CHOIR_LEAD + [("lowbrass", v_brass, 0.26, 0.1, -1, {}),
                     ("cello", v_strings_long, 0.32, -0.2, -1, dict(attack=0.12))]
VORNE = [("choir", v_choir, 0.62, 0.0, 0, dict(vowel="oo", legato=0.95))]
LOW_CHOIR = [("choir", v_choir, 0.55, 0.0, 0, dict(vowel="oo", legato=0.95)), ("lowbrass", v_brass, 0.3, 0.1, -1, {})]
FANFARE = [("brass", v_trumpet, 0.6, 0.15, 0, {}), ("lead", v_lead, 0.4, -0.1, 0, {})]
COUNTER = [("brass", v_trumpet, 0.6, 0.2, 0, {}), ("brass", v_brass, 0.35, 0.1, -1, {})]
BUILD = [("brass", v_trumpet, 0.45, 0.15, 0, {}), ("brass", v_brass, 0.42, -0.1, -1, {}),
         ("strings", v_strings_long, 0.2, 0.25, 0, dict(attack=0.1))]
CLIMAX = [("brass", v_trumpet, 0.65, 0.15, 0, {}), ("brass", v_brass, 0.45, 0.0, -1, {}),
          ("lead", v_lead, 0.4, -0.1, 1, {})]

# the bars where the choir sings a line (its pads drop under it)
CHOIR_LINE_BARS = set(range(0, 16)) | {32, 33, 40, 41} | set(range(48, 72)) | set(range(96, 104))
# the motif cells' kinds by the line's first bar (as is: the motif's own notes on its own degree; re-keyed: moved to
# another degree or mode; for the cell report)
REKEYED = {42: "re-keyed", 68: "re-keyed", 80: "re-keyed", 88: "re-keyed"}


def score():
    sc = Score("final-boss-r95-a (Choir Heart, track 20)", CHORDS, loop=True)
    sc.sections = SECTIONS
    add = sc.add
    # A: the Choir's call
    add(0, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    add(2, L("G4:8 C5:8", "A4:8 B4:8", "B4:8 A4:4 G4:4", "G4:8 E4:8", "F4:8 A4:4 C5:4", "B4:12 .:4"),
        "choir", CHOIR_LEAD)
    add(8, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    add(10, L("A4:8 C5:8", "E5:8 D#5:8", "E5:8 B4:8", "C5:8 G4:8", "C5:8 B4:8", "E5:12 .:4"), "choir", CHOIR_LEAD)
    # B: theme H, the sketch's fanfare (FI_FANFARE) as written, then its consequent
    add(16, L(MAIN_E, "G5:12 F#5:2 E5:2", "E5:6 C5:2 E5:4 A5:4", "F#5:8 D#5:4 B4:4",
              MAIN_E, "G5:6 E5:2 G5:4 C6:4", "A5:6 F#5:2 D5:4 F#5:2 A5:2", "B5:8 D#5:4 F#5:4"),
        "tpt", FANFARE, motif=(0, 4))
    add(24, L(MAIN_E, "G5:8 E5:8", "C6:8 B5:4 A5:4", "A5:4 F#5:4 D5:4 F#5:4",
              "G5:8 B5:4 D6:4", "E6:8 C6:4 G5:4", "A5:8 F#5:4 D#5:4", "E5:12 .:4"), "tpt", FANFARE, motif=(0,))
    # C: both at once (alternating, never stacked)
    add(32, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    add(34, L(MAIN_E, "G5:16", "A5:4 C6:4 B5:4 A5:2 G5:2", "E5:8 F#5:8", "E5:8 D#5:8", "B4:12 .:4"),
        "tpt", COUNTER, motif=(0, 1))
    add(40, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    add(42, L("G4:6 D5:2 G5:6 A5:2", "B5:16", "C6:8 G5:4 E5:4", "A5:8 C6:4 B5:2 A5:2", "F#5:8 D#5:8", "E5:12 .:4"),
        "tpt", COUNTER, motif=(0, 1))
    # D: the Choir's hymn, the B theme
    add(48, L("E5:4 D5:4 C5:4 B4:4", "A4:8 C5:8", "E5:8 D5:4 C5:4", "B4:12 D5:4",
              "C5:8 E5:4 A5:4", "A5:8 G5:4 F5:4", "F5:8 D5:8", "E5:12 .:4"), "choir", HYMN, motif=(0,))
    add(56, CHOIR_MOTIF, "choir", HYMN, motif=(0, 1))
    add(58, L("G4:8 C5:4 E5:4", "D5:8 B4:8", "C5:8 E5:4 A5:4", "G5:8 E5:8", "C5:8 D#5:8", "E5:12 .:4"),
        "choir", HYMN)
    # E: the heart (Vorne's motif sung by the Choir), the build
    add(64, L("E3:6 G3:2 B3:8", "D#4:16"), "choir", VORNE, motif=(0, 1))
    add(66, L("C4:8 E4:8", "E4:8 D#4:8"), "choir", VORNE)
    add(68, L("E4:4 D4:4 C4:4 B3:4", "F3:8 E3:8"), "choir", LOW_CHOIR, motif=(0, 1))
    add(70, L("G3:8 C4:8", "B3:12 .:4"), "choir", LOW_CHOIR)
    add(72, L("B3:8 E4:8", "E4:8 G4:8", "A4:8 C5:8", "C5:8 D5:8", "D5:8 G5:8", "E5:8 G5:8", "A5:8 C6:8",
              "B5:12 .:4"), "horn", BUILD)
    # F: E major
    add(80, L(MAIN_E, "G#5:12 B5:4", "C6:8 G5:4 E5:4", "D#5:8 F#5:4 B5:4",
              "C#6:8 A5:4 E5:4", "B5:8 G#5:4 E5:4", "C#6:8 A#5:8", "B5:12 .:4"), "tpt", CLIMAX, motif=(0, 1))
    add(88, L(MAIN_E, "G#5:12 B5:4", "C6:8 G5:4 E5:4", "E5:8 A5:4 C#6:4",
              "C#6:8 G#5:4 E5:4", "A5:8 C#6:8", "B5:8 A5:4 F#5:2 D#5:2", "E5:12 .:4"), "tpt", CLIMAX, motif=(0, 1))
    # G: the Choir returns, the turnaround
    add(96, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    add(98, L("A4:8 C5:4 E5:4", "A4:8 B4:8", "G4:8 E4:8", "G4:8 C5:8", "C5:8 B4:8", "E4:12 .:4"),
        "choir", CHOIR_LEAD)
    add(104, L("B4:8 E5:8", "E5:8 G5:8", "A5:8 C6:8", "B5:8 F#5:8", "G5:8 E5:8", "E5:8 C5:8", "C5:8 A4:8",
               "B4:12 .:4"), "tpt", COUNTER)
    for ln in sc.lines:  # the motif cells by kind, for the cell report
        ln.cells = {b: REKEYED.get(ln.bar, "as is") for b in ln.motif}
    P = sc.phrase
    P(0, 7, "A", "the call: Choir motif, the choir's line down to F - B", "choir", ("HC", "F", "B"))
    P(8, 15, "A", "the call answered: Choir motif, a full cadence", "choir", ("PAC", "B7", "Em"))
    P(16, 19, "B", "theme H a (the sketch's fanfare, bars 1-4)", "tpt", ("HC", "Am", "B"))
    P(20, 23, "B", "theme H a' (the sketch's fanfare, bars 5-8)", "tpt", ("HC", "D", "B"))
    P(24, 31, "B", "theme H consequent (new)", "tpt", ("PAC", "B7", "Em"))
    P(32, 33, "C", "the Choir motif (choir, low brass)", "choir", ("Phrygian", "F", "Em"))
    P(34, 39, "C", "the main motif answers, the trumpets' line", "tpt", ("HC", "Am", "B"))
    P(40, 41, "C", "the Choir motif again", "choir", ("Phrygian", "F", "Em"))
    P(42, 47, "C", "the main motif on G, a full cadence", "tpt", ("PAC", "B7", "Em"))
    P(48, 55, "D hymn", "the hymn's antecedent (the Choir motif's falling fourth)", "choir",
      ("Phrygian HC", "Dm", "E"))
    P(56, 63, "D hymn", "the hymn's consequent (the whole motif, F - Em)", "choir", ("PAC", "B7", "Em"))
    P(64, 71, "E heart", "Vorne's motif sung by the Choir, the Choir motif answers", "choir", ("HC", "C", "B"))
    P(72, 79, "E build", "the horns climbing onto B", "horn", ("HC", "Am", "B"))
    P(80, 87, "F E major", "the sketch's climax tune, to B", "tpt", ("HC", "F#", "B"))
    P(88, 95, "F E major", "its consequent, a full cadence on E major", "tpt", ("PAC", "B7", "E"))
    P(96, 103, "G return", "the Choir motif over C, back to E minor", "choir", ("PAC", "B7", "Em"))
    P(104, 111, "G turn", "the trumpets' last call onto B (to bar 0)", "tpt", ("HC", "Am", "B"))
    return sc


def section_of(i):
    return [k for k, (b, _) in enumerate(SECTIONS) if b <= i][-1]


# ================================================================ the grooves (the sketch's, per section)

def pads(s, group, voice, bar, ch, gain, upper=False, **kw):
    for st, ln, name in halves(ch):
        pad, _ = voicing(name)
        s.chord(group, voice, pad[1:] if upper else pad, bar, st, dur_steps=ln, gain=gain, **kw)


def root_at(ch, st):
    return voicing(chord_of(ch, st))[1]


def kick_at(s, kicks, bar, st, gain):
    s.hit("drums", kick(), bar, st, gain)
    kicks.append(s.pos(bar, st))


def groove(s, kicks, bar, i, ch):
    k = section_of(i)
    name = SECTIONS[k][1][:2].strip()
    start = SECTIONS[k][0] == i
    tone = "E2" if voicing(chord_of(ch, 0))[1] == "E2" else "B1"
    if start and name not in ("E",):
        s.hit("perc", crash(), bar, 0, 0.6, 0.3)
        s.hit("perc", timpani(tone), bar, 0, 0.8)
    if name in ("A", "G") and i < 104:  # the sketch's A: kick 1, 2, 3, 3&, 4; half hats, taikos, the heartbeat
        for st in (0, 4, 8, 10, 12):
            kick_at(s, kicks, bar, st, 0.9 if st % 4 == 0 else 0.6)
        for st in (4, 12):
            s.hit("drums", snare(), bar, st, 0.55, 0.05)
        for st in range(0, 16, 2):
            s.hit("drums", hat(), bar, st, 0.22, 0.3)
        for st in (0, 8):
            s.hit("perc", taiko(), bar, st, 0.6)
        s.hit("heart", heartbeat(), bar, 0, 0.5)
        for st, sm in enumerate(FI_BASS):
            s.hit("bass", v_dbass(note_freq(transpose12(root_at(ch, st), sm)), round(s.step * 0.8, 4)), bar, st, 0.4)
        if i % 4 in (2, 3):  # the orchestral stabs answering the call
            for st in (0, 3, 6, 10):
                pad, _ = voicing(chord_of(ch, st))
                s.chord("brass", v_brass, pad[1:], bar, st, dur_steps=1, gain=0.45, spread=0.4, stab=True)
        if i % 4 == 3:
            toms(s, bar)
    elif name in ("B", "F"):  # four-on-the-floor, claps, 16th hats, off-beat bass
        for st in (0, 4, 8, 12):
            kick_at(s, kicks, bar, st, 0.9 if st % 8 == 0 else 0.75)
        for st in (4, 12):
            s.hit("drums", snare(), bar, st, 0.55, 0.05)
            s.hit("drums", clap(), bar, st, 0.45)
        for st in range(16):
            s.hit("drums", hat(), bar, st, 0.16 if st % 2 else 0.22, 0.3)
        for st in (0, 6, 10):
            s.hit("perc", taiko(), bar, st, 0.6)
        for st in range(16):
            if st % 4:
                nm = transpose12(root_at(ch, st), 12 if st % 4 == 3 else 0)
                s.hit("bass", v_bass(note_freq(nm), round(s.step * 0.8, 4)), bar, st, 0.42)
        if name == "B":
            for st, sm in enumerate(GALLOP):
                s.hit("ost", v_strings_short(note_freq(transpose12(root_at(ch, st), 12 + sm)),
                                             round(s.step * 0.75, 4)), bar, st, 0.38 if st % 3 == 0 else 0.25, -0.25)
        else:
            figures(s, bar, ch, 0.2)
            pads(s, "organ", v_organ, bar, ch, 0.35)
            if i == 88:
                s.hit("perc", crash(), bar, 0, 0.6, -0.3)
        if i % 4 == 3:
            toms(s, bar)
    elif name in ("C",) or i >= 104:  # the sketch's C: kick 1, 2, 3, 3&, 4, full hats, timpani on even bars
        for st in (0, 4, 8, 10, 12):
            kick_at(s, kicks, bar, st, 0.9 if st % 4 == 0 else 0.6)
        for st in (4, 12):
            s.hit("drums", snare(), bar, st, 0.55, 0.05)
        for st in range(16):
            s.hit("drums", hat(), bar, st, 0.16 if st % 2 else 0.22, 0.3)
        for st in (0, 6, 10):
            s.hit("perc", taiko(), bar, st, 0.6)
        if i % 2 == 0:
            s.hit("perc", timpani(tone), bar, 0, 0.7)
        for st, sm in enumerate(FI_BASS):
            s.hit("bass", v_dbass(note_freq(transpose12(root_at(ch, st), sm)), round(s.step * 0.8, 4)), bar, st, 0.4)
        figures(s, bar, ch, 0.2)
        if i >= 104:  # the turnaround: stabs, a 4-bar riser, the roll
            if i % 2 == 0:
                for st in (0, 3, 6, 10):
                    pad, _ = voicing(chord_of(ch, st))
                    s.chord("brass", v_brass, pad[1:], bar, st, dur_steps=1, gain=0.4, spread=0.4, stab=True)
            if i == 108:
                s.hit("fx", riser(s.bar * 4), bar, 0, 0.35)
            if i >= 110:
                for st in range(0 if i == 111 else 8, 16):
                    s.hit("drums", snare(True), bar, st, 0.2 + 0.02 * st + 0.08 * (i - 110), 0.05)
        if i % 4 == 3:
            toms(s, bar)
    elif name == "D":  # the hymn: half time (kick 1 and 3&, snare on 3), soft 8th hats, taikos, held bass, organ
        for st in (0, 10):
            kick_at(s, kicks, bar, st, 0.85)
        s.hit("drums", snare(), bar, 8, 0.5, 0.05)
        for st in range(0, 16, 2):
            s.hit("drums", hat(), bar, st, 0.13 if st % 4 else 0.18, 0.3)
        for st in (0, 8):
            s.hit("perc", taiko(), bar, st, 0.5)
        for st, ln, nm in halves(ch):
            s.hit("bass", v_dbass(note_freq(voicing(nm)[1]), round(s.step * (ln - 1), 4)), bar, st, 0.3)
        figures(s, bar, ch, 0.12)
        pads(s, "organ", v_organ, bar, ch, 0.3)
        if i == 63:
            toms(s, bar)
    elif name == "E" and i < 72:  # the heart: drums out, the lub-dub on 1 and 3, a held bass, organ
        for st in (0, 8):
            s.hit("heart", heartbeat(), bar, st, 0.95)
        for st, ln, nm in halves(ch):
            s.hit("bass", v_dbass(note_freq(voicing(nm)[1]), round(s.step * (ln - 1), 4)), bar, st, 0.3)
        pads(s, "organ", v_organ, bar, ch, 0.3)
        if i == 64:
            s.hit("perc", timpani("E2"), bar, 0, 0.6)
    else:  # the build (the sketch's D build): taikos, the heartbeat fading, snare 16ths, a 4-bar riser
        for st in (0, 3, 6, 8, 11, 14):
            s.hit("perc", taiko(), bar, st, 0.7)
        if i < 74:
            for st in (0, 8):
                s.hit("heart", heartbeat(), bar, st, 0.7 - 0.2 * (i - 72))
        for st in range(0 if i >= 76 else 8, 16):
            s.hit("drums", snare(True), bar, st, 0.18 + 0.02 * st + 0.04 * (i - 72), 0.05)
        s.hit("perc", timpani(tone), bar, 0, 0.7)
        for st, ln, nm in halves(ch):
            s.hit("bass", v_dbass(note_freq(voicing(nm)[1]), round(s.step * (ln - 1), 4)), bar, st, 0.3)
        if i == 76:
            s.hit("fx", riser(s.bar * 4), bar, 0, 0.35)
        if i == 79:
            toms(s, bar)
    # strings and the choir's pads in every bar (the choir stem is never silent)
    heart = name == "E" and i < 72
    pads(s, "strings", v_strings_long, bar, ch, 0.25 if heart else 0.4)
    sung = i in CHOIR_LINE_BARS
    pads(s, "choir", v_choir, bar, ch, (0.2 if heart else 0.22) if sung else 0.34, upper=True,
         vowel="oo" if heart or name == "D" else "ah")


def transpose12(name, semis):
    return transpose(name, semis) if semis else name


def figures(s, bar, ch, gain):
    """The sketch's 16th chord-tone figures in the strings (sections C and E)."""
    for st in range(16):
        pad, _ = voicing(chord_of(ch, st))
        tones = [transpose12(p, 12) for p in pad[1:]]
        s.hit("ost", v_strings_short(note_freq(tones[[0, 1, 2, 1][st % 4]]), round(s.step * 0.7, 4)), bar, st,
              gain, 0.3)


def toms(s, bar):
    for st, p in ((12, 160), (13, 140), (14, 110), (15, 90)):
        s.hit("drums", tom(p), bar, st, 0.5, (st - 13.5) / 3)


INTRO_CH = ["Em", ("F", "Em"), "Em", ("C", "B")]


def intro_score():
    sc = Score("final-boss-r95-a intro (the sketch's, its last bar now C - B)", INTRO_CH, loop=False)
    sc.add(0, CHOIR_MOTIF, "choir", MOTIF_LEAD, motif=(0, 1))
    sc.add(2, L(MAIN_E, "G5:8 F#5:8"), "tpt", [("brass", v_trumpet, 0.35, 0.3, 0, {})], motif=(0, 1))
    for ln in sc.lines:
        ln.cells = {b: "as is" for b in ln.motif}
    sc.phrase(0, 1, "intro", "the Choir motif (choir, low brass)", "choir", ("Phrygian", "F", "Em"))
    sc.phrase(2, 3, "intro", "the main motif (trumpet), a half cadence into the loop", "tpt", ("HC", "C", "B"))
    return sc


def intro(s, kicks):
    for bar, ch in enumerate(INTRO_CH):
        pads(s, "organ", v_organ, bar, ch, 0.5, spread=0.5)
        s.hit("heart", heartbeat(), bar, 0, 0.9)
        s.hit("heart", heartbeat(), bar, 8, 0.8)
        if bar >= 2:
            pads(s, "choir", v_choir, bar, ch, 0.3, upper=True)
    intro_score().play(s, 0)
    for st in range(16):
        s.hit("drums", snare(True), 3, st, 0.15 + 0.035 * st, 0.05)
    s.hit("fx", riser(s.bar), 3, 0, 0.3)


def choir_heart():
    s = LoopSong(150, 4, LOOP_BARS)
    kicks = []
    intro(s, kicks)
    sc = score()
    for base in s.sections():
        for i, ch in enumerate(CHORDS):
            groove(s, kicks, base + i, i, ch)
        sc.play(s, base)
    groups = {g: mixdown(s, {g: o}, kicks, depth=0.35) for g, o in SPEC.items()}
    return s, groups, sc


# ================================================================ the coda (the race, one-shot)

PICKUP = 10  # steps: 1.0 s at 150 BPM before the motif's first bar
CODA_CH = ["Em", "C", "G", "G", "A", "A", "Bm", "G", ("G", "A"), "A7", "D"]
CODA_TUNE = L("E3:6 B3:2 E4:6 F#4:2", "G4:16", "G3:6 D4:2 G4:6 A4:2", "B4:16", "A3:6 E4:2 A4:6 B4:2", "C#5:16",
              "B3:6 F#4:2 B4:6 C#5:2", "D5:16", "D5:8 E5:8", "E5:8 G5:4 C#5:4", "D5:16")
HOLD_AT, FADE_AT, END_AT = 17.0, 18.5, 22.5  # the D major chord (K+29), the near side (K+30.5), out (K+34.5)


class CodaSong(LoopSong):
    """The coda's timeline: every position PICKUP steps late, so bar 0 starts at 1.0 s and bar 10 at 17.0 s."""

    def pos(self, bar, step=0):
        return super().pos(bar, step + PICKUP)


def coda_score():
    sc = Score("final-boss-coda-r95-a (the race)", CODA_CH, loop=False)
    sc.sections = [(0, "the main motif alone in horns and strings, rising E minor - G - A - B minor, onto A7 and D")]
    sc.add(0, CODA_TUNE, "horn", [("brass", v_brass, 0.55, 0.15, 0, {}), ("brass", v_brass, 0.3, -0.15, -1, {}),
                                  ("strings", v_strings_long, 0.4, 0.0, 1, dict(attack=0.08))],
           motif=(0, 2, 4, 6))
    sc.lines[0].cells = {0: "as is", 2: "re-keyed", 4: "re-keyed", 6: "re-keyed"}
    sc.phrase(0, 10, "coda", "the main motif rising E - G - A - B, the climb onto D major", "horn", ("PAC", "A7", "D"))
    return sc


def coda():
    s = CodaSong(150, 0, 8)
    kicks = []
    sc = coda_score()
    hold = HOLD_AT * SR / s.step_n  # steps from the file start to the chord (170)
    # the pickup on B: the strings' swell, the timpani and the snare growing (bar -1, steps 6-15)
    s.chord("strings", v_strings_long, ["B2", "F#3", "B3"], -1, 16 - PICKUP, dur_steps=PICKUP, gain=0.35,
            attack=0.6)
    for st in range(16 - PICKUP, 16):
        s.hit("drums", snare(True), -1, st, 0.12 + 0.04 * (st - 6), 0.05)
        s.hit("perc", timpani("B1", 0.4), -1, st, 0.25 + 0.04 * (st - 6))
    for bar, ch in enumerate(CODA_CH[:-1]):
        lift = 0.55 + 0.05 * bar  # rising: about 5 dB from bar 0 to bar 9, the layers added every two bars
        for st in (0, 4, 8, 12) if bar < 8 else range(0, 16, 2):
            kick_at(s, kicks, bar, st, (0.9 if st % 8 == 0 else 0.7) * lift)
        if bar >= 4:
            for st in (4, 12):
                s.hit("drums", snare(), bar, st, 0.5 * lift, 0.05)
        if bar >= 2:
            for st in range(0, 16, 1 if bar >= 6 else 2):
                s.hit("drums", hat(), bar, st, (0.15 if st % 2 else 0.2) * lift, 0.3)
        for st in range(16):
            if st % 4:
                nm = transpose12(root_at(ch, st), 12 if st % 4 == 3 else 0)
                s.hit("bass", v_bass(note_freq(nm), round(s.step * 0.8, 4)), bar, st, 0.4 * lift)
        figures(s, bar, ch, 0.22 * lift)
        s.hit("perc", timpani("E2" if bar == 0 else voicing(chord_of(ch, 0))[1].replace("1", "2")), bar, 0,
              0.6 * lift)
        if bar % 2 == 0:
            s.hit("perc", taiko(), bar, 0, 0.55 * lift)
        pads(s, "strings", v_strings_long, bar, ch, 0.3 * lift)
        if bar == 8:
            s.hit("fx", riser(s.bar * 2), bar, 0, 0.35)
        if bar == 9:
            for st in range(16):
                s.hit("drums", snare(True), bar, st, 0.2 + 0.03 * st, 0.05)
            toms(s, bar)
    for bar, toks in enumerate(CODA_TUNE[:-1]):  # the tune bar by bar, rising with the pulse (the last bar's D is
        for group, voice, gain, pos, octave, kw in sc.lines[0].orch:  # the held chord below)
            kw = dict(kw)
            s.play(group, voice, [toks], bar, gain=gain * (0.62 + 0.042 * bar), position=pos, octave=octave,
                   legato=kw.pop("legato", 0.92), **kw)
    # the held D major chord at 17.0 s (bar 10): horns, strings, organ, the bass and the timpani; held to the end
    held = END_AT - HOLD_AT + 0.2
    s.hit("perc", crash(), 10, 0, 0.7, 0.2)
    s.hit("perc", timpani("D2", 3.0), 10, 0, 0.9)
    s.chord("brass", v_brass, ["D3", "F#3", "A3", "D4", "F#4"], 10, 0, dur_steps=held / s.step, gain=0.6, spread=0.5)
    s.chord("brass", v_brass, ["D5"], 10, 0, dur_steps=held / s.step, gain=0.5)
    s.chord("strings", v_strings_long, ["D4", "F#4", "A4", "D5", "F#5", "A5", "D6"], 10, 0,
            dur_steps=held / s.step, gain=0.75, spread=0.7, attack=0.05)
    s.chord("organ", v_organ, ["D2", "A2", "D3", "F#3", "A3"], 10, 0, dur_steps=held / s.step, gain=0.5)
    s.hit("bass", v_dbass(note_freq("D2"), round(held, 4)), 10, 0, 0.35)
    assert abs(s.pos(10, 0) - HOLD_AT) < 1e-9, (s.pos(10, 0), hold)
    mix = mixdown(s, {g: SPEC[g] for g in ("drums", "perc", "fx", "bass", "ost", "strings", "brass", "organ")},
                  kicks, depth=0.35)
    return s, mix, sc


# ================================================================ render

def rms(x):
    return db(np.sqrt(np.mean(x ** 2)))


def render_loop(out, rest_dir, show=True):
    s, groups, sc = choir_heart()
    full_check(intro_score(), 0, s.bar) if show else None
    r = full_check(sc, s.intro, s.bar)
    mix = sum(groups.values())
    stem = sum(groups[g] for g in STEM_GROUPS)
    n = mix.shape[1]
    centre = lambda x: x - x.mean(axis=1, keepdims=True)  # noqa: E731
    mix, stem = centre(mix), centre(stem)
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    g = master_gain(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, CEILING)[:n]

    def finish(x):
        audio, loop_start, loop_len = s.assemble(x * g)
        audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
        audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
        return audio, loop_start, loop_len

    out.mkdir(parents=True, exist_ok=True)
    decs = {}
    for name, x, d in ((FULL, mix, out), (CHOIR, stem, out), ("final-boss-rest-r95-a", mix - stem, rest_dir)):
        audio, loop_start, loop_len = finish(x)
        tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
        d.mkdir(parents=True, exist_ok=True)
        path = write_ogg(d / f"{name}.ogg", audio, max_peak_db=None, tags=tags)
        dec = decode(path)
        t = tag_values(path)
        end = loop_start + loop_len
        loud, tp = lufs(dec)
        print(f"{path.name}: {dec.shape[1] / SR:.2f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s (samples "
              f"{t['LOOPSTART']}+{t['LOOPLENGTH']})  seam {seam_ratio(dec, loop_start, loop_len):.2f}  RMS "
              f"{rms(dec[:, end - ns(0.25):end]):.1f} / {rms(dec[:, loop_start:loop_start + ns(0.25)]):.1f} dB around "
              f"the loop point  {loud:.1f} LUFS  peak {db(np.abs(dec).max()):.1f} dBFS  true peak {tp:.1f} dBTP",
              flush=True)
        decs[name] = dec
    full, choir, rest = decs[FULL], decs[CHOIR], decs["final-boss-rest-r95-a"]
    res = rest + choir - full
    print(f"rest + choir vs the full mix (decoded): residual {rms(res) - rms(full):.1f} dB under the mix (RMS), peak "
          f"error {db(np.abs(res).max()):.1f} dBFS; aligned {full.shape == choir.shape}")
    # the choir stem's level against the full mix, per section (the game plays it alone at the Heart's death)
    bar_n = 16 * s.step_n
    print("choir stem vs the full mix per section (loop pass in the file), and its quietest bar:")
    for k, (b, label) in enumerate(SECTIONS):
        e = SECTIONS[k + 1][0] if k + 1 < len(SECTIONS) else LOOP_BARS
        a0, a1 = (s.intro + b) * bar_n + s.xf_steps * s.step_n, (s.intro + e) * bar_n
        print(f"  bars {b:3d}-{e - 1:3d}  full {rms(full[:, a0:a1]):6.1f} dB  choir {rms(choir[:, a0:a1]):6.1f} dB"
              f"  :: {label[:50]}")
    q = min(range(LOOP_BARS), key=lambda b: rms(choir[:, (s.intro + b) * bar_n:(s.intro + b + 1) * bar_n]))
    print(f"  quietest choir bar {q}: {rms(choir[:, (s.intro + q) * bar_n:(s.intro + q + 1) * bar_n]):.1f} dB")
    if show:
        print(f"{FULL} sections:")
        for k, (b, label) in enumerate(SECTIONS):
            t0 = (s.intro + b) * s.bar
            print(f"  {int(t0 // 60)}:{t0 % 60:05.2f}  bar {b:3d}  {label}")
    return r


def render_coda(out, show=True):
    s, mix, sc = coda()
    full_check(sc, 0, s.bar) if show else None
    end = int(END_AT * SR)
    mix = mix[:, :end]
    mix = mix - mix.mean(axis=1, keepdims=True)
    a = int(FADE_AT * SR)
    mix[:, a:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, end - a))) ** 1.5
    mix = master(mix, -14.0, CEILING)
    mix[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{CODA}.ogg", mix)
    dec = _report(path, "  (one-shot, no loop)")
    t = int(HOLD_AT * SR)
    print(f"  the D major chord at {HOLD_AT:.2f} s (sample {t}): RMS {rms(dec[:, t - ns(0.5):t]):.1f} dB before, "
          f"{rms(dec[:, t:t + ns(0.5)]):.1f} dB after; at the near side ({FADE_AT} s) {rms(dec[:, a - ns(0.3):a]):.1f}"
          f" dB, the last 0.3 s {rms(dec[:, -ns(0.3):]):.1f} dB")


def main(argv):
    out, rest_dir = OUT, None
    check_only = "--check" in argv
    argv = [a for a in argv if a != "--check"]
    for flag in ("--out", "--rest"):
        if flag in argv:
            i = argv.index(flag)
            val = Path(argv[i + 1])
            argv = argv[:i] + argv[i + 2:]
            out, rest_dir = (val, rest_dir) if flag == "--out" else (out, val)
    rest_dir = rest_dir or out / ".rest"
    if check_only:
        full_check(intro_score(), 0, 1.6)
        full_check(score(), 4, 1.6)
        full_check(coda_score(), 0, 1.6)
        return
    for key in argv or ["loop", "coda"]:
        if key == "loop":
            render_loop(out, rest_dir)
        else:
            render_coda(out)


if __name__ == "__main__":
    main(sys.argv[1:])
