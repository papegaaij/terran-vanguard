#!/usr/bin/env python3
"""Concept round 85 (M11 "Act 7 - Beyond the Gate", step MUS-R85, 2026-11-05): re-takes of three round-84 music sketches
after the user's answers (round 84 M.1, M.3, M.4); pending the user's review. The round-84 generator
(music_r84.py) stays as it is; this one imports its voices and tables.

- Track 16, the Act 7 A theme "Far Shore" (M.1, a picked): kept as round 84's a, but the Choir motif's answer is no
  longer the formant choir (it did not sit with the main motif in the horns). Two voices for the answer:
    act7-a-theme-r85-a.ogg  low brass (trombones and a bass trombone, E-rooted like the Choir) doubled by a detuned
        synth lead (Event Horizon's family): the brass section answering its own horns, the synth giving it the cold.
    act7-a-theme-r85-b.ogg  the orchestra's strings (violins and cellos in octaves) with a bowed-glass shimmer: the
        horns call, the strings answer, the Choir's otherness kept by its E-rooted flat 2 against the key and the glass.
  The breakdown (loop bars 16-19) is kept, carried by the same answer voice (bar 16, then an octave lower at 18) over
  the pad, with only a faint "oo" choir chord under it: never the formant choir alone.
- Track 34 "Vorne's last stand" (M.3, a "Fused" picked): Fused's sound (150 BPM, E minor, orchestral-martial: timpani,
  snare rudiments, low-brass 8ths, galloping strings, the choir in long chords, Vorne's motif E G B D-sharp in cold
  brass bending into the Choir motif's tritone drop B F E) as a 72-bar loop (1:55) in five or six sections, never the
  same 8 bars more than twice in a row. Two arrangements of that plan:
    vorne-last-stand-r85-a.ogg  A statement (0-15) / B bridge, a new march tune in trumpets then horns (16-31) /
        C breakdown, his motif augmented in a muted horn over the choir (32-41) and a build (42-43) / D the motif
        varied in F minor, the key a semitone up (the Choir's flat 2), syncopated, over a 16th riff made of the motif
        (44-57), the whole orchestra bending down a semitone into B (58-59) / E the bend: E minor, his motif in full
        brass bending into the full choir's drop, the Choir's whole motif sung over him (60-71).
    vorne-last-stand-r85-b.ogg  A the motif in canon, cellos, horns, trumpets (0-15) / B a chorale bridge, the choir
        and strings singing a new hymn over a falling bass (16-27) / C breakdown, the choir singing Vorne's motif
        (28-33) and a build (34-35) / D the motif in G minor in a 3-against-4 rhythm (36-51) / E the bend, his motif
        augmented in full brass bending into an augmented drop (52-63) / F turnaround, the roles swapped: the choir
        sings his motif, the brass plays the Choir's (64-71).
- Track 27 "Ending" (M.4, neither picked: the motif too short to loop): through-composed, played once, never
  restarting. A few statements of the main motif while the instruments join (as round 84's b opened), then from the
  last of them the melody continues into new phrases (a second strain, a climb, the full statement in D major
  extended, the Choir motif at peace) and ends on the open low chord (D major over a low E-flat), faded. The model is
  credits-r84-a, where the title's B melody takes over from the motif at 0:23.
    ending-r85-a.ogg  "Stand Down, Aegis" 75 BPM: horn, strings, piano (round 84's a voice).
    ending-r85-b.ogg  "Home Light" 90 BPM: flute and soft electric piano grown into the orchestra (round 84's b voice).

Tracks 16 and 34 use round 72's loop format (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH in samples, a
sample-exact loop); the endings are one-shots (no loop tags). All at -14 LUFS with a -2 dBFS sample-peak ceiling, as
round 84. --sections prints each file's section list with timestamps (from the score). No bubbly or popping sounds:
every pitched voice has a soft attack except the basses and the staccato riff.

Usage: python3 tools/concept/audio/music_r85.py [a16-a|a16-b|vls-a|vls-b|end-a|end-b ...] [--out DIR] [--sections]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks, the loop seam and a spectrogram.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (crash, hat, hp, kick, ns, riser, snare, timpani, transpose,  # noqa: E402
                   v_brass, v_pad, v_strings_long, v_strings_short)
from music_r02 import GALLOP, LoopSong, add_tail, seam_ratio, v_choir, v_epiano, v_flute, v_reese, v_sub  # noqa: E402
from music_r03 import choir_motif, mixdown, v_trumpet  # noqa: E402
from music_r60 import snare_roll  # noqa: E402
from music_r72 import v_brass_bend  # noqa: E402
from music_r84 import (CHOIR_E4, FS_CH, FS_CHOIR, FS_HORNS, FS_INTRO, FS_KICK, FS_LOOP, FS_SNARE, MAIN_D4,  # noqa: E402
                       MAIN_D4_MAJ, PEACE, VL_SNARE, _sec, v_glass, v_horn, v_muted)
from synth import (SR, adsr, db, decode, exp_decay, lufs, master, reverb, supersaw, svf,  # noqa: E402
                   write_ogg, note_freq)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"

PCS = {"C": 0, "C#": 1, "Db": 1, "D": 2, "D#": 3, "Eb": 3, "E": 4, "F": 5, "F#": 6, "Gb": 6, "G": 7, "G#": 8,
       "Ab": 8, "A": 9, "A#": 10, "Bb": 10, "B": 11}
NAMES = ["C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"]


def mname(m):
    return f"{NAMES[m % 12]}{m // 12 - 1}"


def vc(name):
    """A chord name ("Em", "Bb", "F#dim", "Em/D#") -> (voicing E3..E4, bass E1..D#2)."""
    ch, _, bass = name.partition("/")
    root = ch[:2] if len(ch) > 1 and ch[1] in "#b" else ch[:1]
    ivs = {"": (0, 4, 7), "m": (0, 3, 7), "dim": (0, 3, 6), "7": (0, 4, 7, 10)}[ch[len(root):]]
    pcs = {(PCS[root] + i) % 12 for i in ivs}
    b = PCS[bass or root]
    return [mname(m) for m in range(52, 65) if m % 12 in pcs], mname(next(m for m in range(28, 40) if m % 12 == b))


def timp_note(bass):
    m = PCS[bass[:-1]] + 12 * (int(bass[-1]) + 1)
    return mname(m + 12 if m < 36 else m)


# ================================================================ new voices

@lru_cache(None)
def v_detuned(freq, dur):
    """A detuned synth lead: a wide supersaw, dark and slowly opening, a soft attack, a slow wide vibrato."""
    n = ns(dur + 0.4)
    t = np.arange(n) / SR
    f = freq * (1 + 0.007 * np.sin(2 * np.pi * 4.2 * t) * np.clip((t - 0.2) / 0.4, 0, 1))
    x = supersaw(f, n, voices=7, detune=0.022, rng=np.random.default_rng(31))
    x = svf(x, 900 + 900 * (1 - exp_decay(n, 0.4)), q=0.9)
    return x * adsr(n, 0.05, 0.3, 0.8, 0.3, gate=dur) * 0.9


@lru_cache(None)
def v_trombone(freq, dur):
    """Low brass: v_brass darker (a lower filter), a slower lip into the note."""
    n = ns(dur + 0.3)
    x = v_brass(freq, dur)
    x = np.pad(x, (0, max(0, n - len(x))))[:n]
    x = svf(x, 1100, q=0.7)
    return x * np.minimum(1, np.arange(n) / ns(0.05)) * 1.3


def drop_tokens(root):
    """The Choir motif's tritone drop (5, flat 2, 1) from root (octave-3 name), two bars."""
    r = PCS[root[:-1]] + 12 * (int(root[-1]) + 1)
    return [f"{mname(r + 7)} - - - - - - - {mname(r + 1)} - - - - - - -", f"{mname(r)} " + "- " * 14 + "-"]


# ================================================================ track 16: Far Shore with a new answer (140 BPM)

def far_shore_r85(answer):
    """Round 84's far_shore() unchanged but for the Choir motif's answer (the `choir` stem) and the breakdown."""
    s = LoopSong(140, 4, len(FS_LOOP))
    kicks = []
    for bar, name in enumerate(FS_INTRO):
        pad, root = FS_CH[name]
        for st in range(0, 16, 2):
            s.hit("ost", v_strings_short(note_freq(transpose(root, 12)), round(s.step * 1.6, 4)), bar, st,
                  0.3 + 0.05 * bar)
        s.chord("pad", v_pad, pad, bar, gain=0.15 + 0.05 * bar, spread=0.7)
        if bar >= 2:
            for st, g in FS_KICK:
                s.hit("drums", kick(130), bar, st, g)
                kicks.append(s.pos(bar, st))
            for st, g in FS_SNARE:
                s.hit("drums", snare(True), bar, st, g * 0.8, 0.05)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.2)
    for base in s.sections():
        for i, name in enumerate(FS_LOOP):
            bar = base + i
            pad, root = FS_CH[name]
            sec = _sec(i, (8, 16, 20, 28))
            if i in (0, 8, 20, 28):
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            if sec != "C":
                for st, g in FS_KICK:
                    s.hit("drums", kick(130), bar, st, g)
                    kicks.append(s.pos(bar, st))
                for st, g in FS_SNARE:
                    s.hit("drums", snare(True), bar, st, g, 0.05)
                for st in range(16):
                    s.hit("hats", hat(st % 8 == 6), bar, st, 0.14 if st % 2 == 0 else 0.07, 0.3)
                for st in range(0, 16, 2):
                    semis = (0, 0, 12, 0, 7, 0, 12, 10)[st // 2]
                    s.hit("ost", v_strings_short(note_freq(transpose(root, 12 + semis)), round(s.step * 1.6, 4)),
                          bar, st, 0.42 if st % 4 == 0 else 0.32, -0.2)
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 7.5, 4)), bar, 0, 0.36)
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 5.5, 4)), bar, 10, 0.3)
                s.chord("pad", v_strings_long, pad, bar, gain=0.26, spread=0.7)
                if i % 4 == 3:
                    s.hit("perc", timpani("A1"), bar, 12, 0.6)
            else:  # the breakdown: the pad, the sub, a faint "oo" choir chord under the answer (never alone)
                s.chord("pad", v_pad, pad, bar, gain=0.4, spread=0.8)
                s.chord("bed", v_choir, pad, bar, gain=0.16, spread=0.8, vowel="oo")
                s.hit("sub", v_sub(note_freq(transpose(root, 12 if root.endswith("1") else 0)),
                                   round(s.step * 15, 4)), bar, 0, 0.4)
                if i == 19:
                    for st in range(16):
                        s.hit("drums", snare(True), bar, st, 0.12 + 0.04 * st, 0.05)
                    s.hit("fx", riser(s.bar), bar, 0, 0.25)
        for k in FS_HORNS:  # the main motif in horns (as round 84)
            s.play("horn", v_brass, MAIN_D4, base + k, gain=0.6, position=0.15)
            s.play("horn", v_horn, MAIN_D4, base + k, gain=0.5, position=-0.15, octave=-1)
        for k in FS_CHOIR + (18,):  # the Choir motif's answer (the `choir` stem); 16 and 18 the breakdown
            low = -1 if k == 18 else 0
            g = 0.85 if k in (16, 18) else 1.0
            if answer == "a":  # low brass doubled by a detuned synth lead
                s.play("choir", v_trombone, CHOIR_E4, base + k, gain=0.5 * g, position=-0.15, octave=-1 + low,
                       legato=0.95)
                s.play("choir", v_trombone, CHOIR_E4, base + k, gain=0.22 * g, position=0.1, octave=-2 + low,
                       legato=0.95)
                s.play("choir", v_detuned, CHOIR_E4, base + k, gain=(0.42 if k == 16 else 0.3) * g, position=0.2,
                       octave=low, legato=0.97)
            else:  # strings in octaves with a bowed-glass shimmer
                s.play("choir", v_strings_long, CHOIR_E4, base + k, gain=0.5 * g, position=0.2, octave=1 + low,
                       legato=0.98)
                s.play("choir", v_strings_long, CHOIR_E4, base + k, gain=0.42 * g, position=-0.2, octave=-1 + low,
                       legato=0.98)
                s.play("choir", v_glass, CHOIR_E4, base + k, gain=0.3 * g, position=0.0, octave=1 + low, legato=0.98)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "hats": dict(hp=700, gain=0.35),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=8401, gain=0.7),
        "fx": dict(gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.6, 0.2), seed=8402, gain=0.55),
        "bass": dict(hp=30, duck=True, gain=0.5),
        "sub": dict(hp=25, gain=0.5),
        "pad": dict(hp=90, duck=True, rev=(3.2, 0.4), wid=1.5, seed=8403, gain=0.45),
        "bed": dict(hp=120, rev=(4.0, 0.5), wid=1.6, seed=8406, gain=0.5),
        "horn": dict(hp=70, rev=(2.8, 0.3), seed=8404, gain=0.8),
    }, kicks, depth=0.4)
    stem = s.tl.group("choir")
    stem = reverb(hp(stem, 50 if answer == "a" else 70), seconds=3.0, mix=0.32, seed=8505)[:, :s.tl.n]
    sections = [(-4, "intro: string ostinato, pad, the break from bar 2"),
                (0, "A: groove; horns' main motif at 0, the answer at 4"),
                (8, "B: horns at 8, the answer at 12"),
                (16, "C: breakdown, the answer over the pad (16, an octave lower 18), snare build"),
                (20, "D: all in, horns at 20 and 24"),
                (28, "E: the answer at 28 over the groove")]
    return s, mix, stem * 0.85, FS_CHOIR, sections


# ================================================================ track 34: Vorne's last stand, two arrangements

VORNE_RHYTHMS = {  # (semitones, bar offset, step, length in steps)
    "stately": [(0, 0, 0, 6), (3, 0, 6, 2), (7, 0, 8, 8), (11, 1, 0, 14)],
    "aug": [(0, 0, 0, 12), (3, 0, 12, 4), (7, 1, 0, 16), (11, 2, 0, 28)],
    "sync": [(0, 0, 0, 3), (3, 0, 3, 3), (7, 0, 6, 4), (11, 0, 10, 20)],
    "hemiola": [(0, 0, 0, 6), (3, 0, 6, 6), (7, 0, 12, 6), (11, 1, 2, 14)],
}
MARCH_SNARE = [0.7, 0, 0.25, 0, 0.5, 0, 0.25, 0.2, 0.7, 0, 0.25, 0, 0.5, 0.2, 0.4, 0.25]
DRIVE_SNARE = [0.3, 0.12, 0.18, 0.12, 0.8, 0.12, 0.18, 0.25, 0.3, 0.12, 0.18, 0.12, 0.8, 0.3, 0.45, 0.6]


def vorne(s, group, voice, bar, root, gain, pos=0.0, rhythm="stately", bend=True, octave=0):
    """Vorne's motif (1 b3 5 7) from root, its last note bending a semitone down and dying (brass) or held."""
    r = transpose(root, 12 * octave)
    notes = VORNE_RHYTHMS[rhythm]
    for k, (semis, bo, st, ln) in enumerate(notes):
        f = note_freq(transpose(r, semis))
        last = k == len(notes) - 1
        if last and bend:
            s.hit(group, v_brass_bend(f, round(s.step * ln, 4), -1.0, 0.4), bar + bo, st, gain, pos)
        else:
            s.hit(group, voice(f, round(s.step * ln * 0.95, 4)), bar + bo, st, gain, pos)


def vl_bar(s, kicks, bar, chord, tex):
    """One bar of Fused's orchestra. tex: full (round 84's), march, drive, break, build."""
    voicing, bass = vc(chord)
    tn = timp_note(bass)
    low = transpose(bass, 12)
    if tex in ("full", "march", "drive"):
        s.hit("perc", timpani(tn), bar, 0, 0.75)
        s.hit("perc", timpani(transpose(tn, 7)), bar, 8, 0.45)
        for beat in range(4):
            if tex != "march" or beat % 2 == 0:
                s.hit("drums", kick(130), bar, beat * 4, 0.8 if beat % 2 == 0 else 0.5)
                kicks.append(s.pos(bar, beat * 4))
        pat = {"full": VL_SNARE, "march": MARCH_SNARE, "drive": DRIVE_SNARE}[tex]
        for st, g in enumerate(pat):
            if g:
                s.hit("snare", snare(True), bar, st, g * 0.65, 0.1)
        if tex == "march":  # dotted low brass, sustained strings
            for st in (0, 3, 4, 8, 11, 12):
                s.hit("ost", v_brass(note_freq(low), round(s.step * (2.4 if st % 4 == 0 else 0.8), 4), True), bar,
                      st, 0.34 if st % 4 == 0 else 0.24)
            s.chord("strings", v_strings_long, voicing, bar, gain=0.3, spread=0.8)
            s.chord("choir", v_choir, voicing, bar, gain=0.22, spread=0.8)
        else:
            for st in range(0, 16, 2):
                s.hit("ost", v_brass(note_freq(low), round(s.step * 1.4, 4), True), bar, st,
                      0.34 if st % 4 == 0 else 0.26)
            s.chord("choir", v_choir, voicing, bar, gain=0.4 if tex == "full" else 0.34, spread=0.8)
        if tex == "full":
            for st, sm in enumerate(GALLOP):
                s.hit("gallop", v_strings_short(note_freq(transpose(low, sm)), round(s.step * 0.75, 4)), bar, st,
                      0.36 if st % 3 == 0 else 0.22, -0.25)
        if tex == "drive":  # the motif diminished into a 16th-and-8th riff in the low strings
            for st, sm in enumerate((0, 3, 7, 11, 12, 11, 7, 3, 0, 3, 7, 11, 12, 15, 12, 11)):
                s.hit("gallop", v_strings_short(note_freq(transpose(low, sm)), round(s.step * 0.7, 4)), bar, st,
                      0.34 if st % 4 == 0 else 0.22, -0.25)
    elif tex == "break":
        s.hit("perc", timpani(tn), bar, 0, 0.4)
        s.hit("perc", timpani(tn), bar, 8, 0.22)
        s.hit("strings", v_strings_long(note_freq(low), round(s.bar, 4), 0.5), bar, 0, 0.32)
        s.chord("choir", v_choir, voicing, bar, gain=0.5, spread=0.9)
    elif tex == "build":
        s.chord("choir", v_choir, voicing, bar, gain=0.45, spread=0.9)
        s.hit("strings", v_strings_long(note_freq(low), round(s.bar, 4), 0.5), bar, 0, 0.32)
        for st in range(0, 16, 2):
            s.hit("perc", timpani(tn), bar, st, 0.2 + 0.035 * st)
        s.hit("snare", snare_roll(s.bar * 0.95), bar, 0, 0.55)


def vl_render(s, kicks, intro_chord="Em"):
    for bar in range(4):  # intro (round 84's): timpani, low brass 8ths, a snare roll into the loop
        s.hit("perc", timpani("E2" if bar < 2 else "B1"), bar, 0, 0.9)
        for st in range(0, 16, 2):
            s.hit("ost", v_brass(note_freq("E2"), round(s.step * 1.4, 4), True), bar, st, 0.3 + 0.04 * bar)
        s.chord("choir", v_choir, vc(intro_chord)[0], bar, gain=0.2 + 0.08 * bar, spread=0.8)
    s.hit("snare", snare_roll(s.bar * 0.95), 3, 0, 0.6)


def vl_mix(s, kicks):
    return mixdown(s, {
        "drums": dict(hp=35, gain=0.7),
        "snare": dict(hp=140, rev=(1.6, 0.2), seed=8441, gain=0.6),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=8442, gain=0.8),
        "fx": dict(gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.8, 0.2), seed=8443, gain=0.5),
        "gallop": dict(hp=70, rev=(1.8, 0.2), seed=8444, gain=0.45),
        "strings": dict(hp=50, duck=True, rev=(3.0, 0.4), wid=1.5, seed=8550, gain=0.5),
        "choir": dict(hp=120, duck=True, rev=(4.0, 0.45), wid=1.6, seed=8445, gain=0.5),
        "vorne": dict(hp=55, rev=(2.8, 0.3), seed=8446, gain=0.8),
        "drop": dict(hp=110, rev=(4.2, 0.5), wid=1.5, seed=8447, gain=0.75),
        "tune": dict(hp=80, rev=(2.6, 0.3), wid=1.3, seed=8551, gain=0.7),
        "sing": dict(hp=110, rev=(4.2, 0.5), wid=1.4, seed=8552, gain=0.75),
    }, kicks, depth=0.3)


def drop(s, bar, root="E3", gain=0.75, full=False, aug=False):
    """The choir's tritone drop (the Choir motif's 5, flat 2, 1); full adds the low brass and a third octave."""
    toks = drop_tokens(root)
    if aug:  # half speed: the fifth two bars, the flat 2 and the root one bar each
        n7, n1, r = toks[0].split()[0], toks[0].split()[8], toks[1].split()[0]
        hold = " " + "- " * 14 + "-"
        toks = [n7 + hold, "- " * 15 + "-", n1 + hold, r + hold]
    s.play("drop", v_choir, toks, bar, gain=gain, position=0.1, legato=0.97)
    s.play("drop", v_choir, toks, bar, gain=gain * 0.6, position=-0.1, octave=-1, legato=0.97, vowel="oo")
    if full:
        s.play("drop", v_choir, toks, bar, gain=gain * 0.45, position=0.0, octave=1, legato=0.97)
        s.play("drop", v_trombone, toks, bar, gain=gain * 0.35, position=0.0, octave=-1, legato=0.95)


# Bridge tune (a): a new march melody in the relative major, trumpets then horns
BR_A_CH = ["G", "D", "Em", "C", "G", "D7", "C", "B", "Am", "Em", "C", "G", "Am", "F", "B", "B"]
BR_A_TUNE = ["B4 - - - - - D5 - G5 - - - - - F#5 -", "F#5 - - - - - - - A5 - - - F#5 - D5 -",
             "E5 - - - - - G5 - B5 - - - - - A5 -", "G5 - - - - - - - E5 - - - - - - -",
             "D5 - - - - - G5 - B5 - - - - - D6 -", "C6 - - - B5 - - - A5 - - - F#5 - - -",
             "G5 - - - - - - - E5 - - - C5 - - -", "D#5 - - - - - - - F#5 - - - B4 - - -",
             "C5 - - - - - E5 - A5 - - - - - G5 -", "G5 - - - - - - - B4 - - - E5 - - -",
             "E5 - - - - - G5 - C6 - - - - - B5 -", "B5 - - - - - - - D5 - - - G5 - - -",
             "A5 - - - - - - - C6 - - - A5 - - -", "A5 - - - - - - - F5 - - - C5 - - -",
             "B4 - - - - - - - D#5 - - - F#5 - - -", "A5 - - - - - - - - - - - F#5 - D#5 -"]
STATEMENT = ["Em", "Em", "C", "C", "Am", "Am", "B", "B", "Em", "Em", "C", "C", "Am", "F", "B", "Em"]

VLA_PLAN = (  # (first bar, chords, texture, section label)
    (0, STATEMENT, "full", "A statement: Vorne's motif in cold brass at 0 and 8, the choir's drop at 2 and 10"),
    (16, BR_A_CH, "march", "B bridge: a new march tune, trumpets (16-23), then horns with a string descant (24-31)"),
    (32, ["Em", "Em", "F", "Em", "Em", "Em", "C", "B", "Em", "F"], "break",
     "C breakdown: drums out, his motif augmented in a muted horn over the choir (34, 38 bending into the drop)"),
    (42, ["Am", "B"], "build", "C build: timpani crescendo and snare roll"),
    (44, ["Fm", "Fm", "Db", "Db", "Bbm", "Bbm", "C", "C", "Fm", "Fm", "Db", "Db", "Bbm", "Gb"], "drive",
     "D variation in F minor (a semitone up): the motif syncopated in horns, a 16th riff of it in the low strings"),
    (58, ["C", "B"], "full", "D turn: the whole orchestra bends down a semitone, C into B"),
    (60, ["Em", "Em", "C", "C", "Am", "F", "B", "Em", "Am", "Am", "B", "B"], "full",
     "E the bend: his motif in full brass into the full choir's drop; the Choir's whole motif over him (64)"),
)


def last_stand_a():
    s = LoopSong(150, 4, 72)
    kicks = []
    vl_render(s, kicks)
    for base in s.sections():
        for first, chords, tex, _ in VLA_PLAN:
            for k, ch in enumerate(chords):
                vl_bar(s, kicks, base + first + k, ch, tex)
        for b in (0, 16, 32, 44, 60):
            s.hit("perc", crash(), base + b, 0, 0.4, 0.3)
        # A: round 84's statement
        for k in (0, 8):
            vorne(s, "vorne", v_brass, base + k, "E3", 0.7, -0.15)
            vorne(s, "vorne", v_brass, base + k, "E3", 0.35, 0.15, octave=-1)
            drop(s, base + k + 2)
        # B: the march tune, trumpets then horns with a descant
        s.play("tune", v_trumpet, BR_A_TUNE[:8], base + 16, gain=0.42, position=0.15)
        s.play("tune", v_strings_long, BR_A_TUNE[:8], base + 16, gain=0.3, position=-0.15, octave=-1)
        s.play("tune", v_horn, BR_A_TUNE[8:], base + 24, gain=0.55, position=0.1, octave=-1)
        s.play("tune", v_brass, BR_A_TUNE[8:], base + 24, gain=0.25, position=-0.1, octave=-1)
        for k, ch in enumerate(BR_A_CH[8:]):  # the descant: the chord's top notes in 8th-note strings
            v, _ = vc(ch)
            for st in range(0, 16, 2):
                s.hit("tune", v_strings_short(note_freq(transpose(v[(st // 2) % len(v)], 24)), round(s.step * 1.5, 4)),
                      base + 24 + k, st, 0.13, 0.3)
        # C: the augmented motif in a muted horn, the second one bending into the choir's drop
        vorne(s, "vorne", v_muted, base + 34, "E3", 0.75, 0.0, rhythm="aug", bend=False)
        vorne(s, "vorne", v_muted, base + 38, "E3", 0.75, 0.0, rhythm="aug", bend=True)
        drop(s, base + 40, gain=0.6)
        # D: F minor, the motif syncopated in horns and trumpets
        for k in (44, 48, 52):
            vorne(s, "vorne", v_horn, base + k, "F3", 0.75, -0.15, rhythm="sync", bend=False)
            vorne(s, "vorne", v_trumpet, base + k + 2, "F4", 0.35, 0.15, rhythm="sync", bend=False)
        vorne(s, "vorne", v_brass, base + 56, "F3", 0.7, -0.1, rhythm="stately", bend=False)
        # the turn: the C chord held by the brass, bending a semitone down into B
        for nm in vc("C")[0][:4]:
            s.hit("vorne", v_brass_bend(note_freq(transpose(nm, -12)), round(s.bar * 1.9, 4), -1.0, 0.45),
                  base + 58, 0, 0.32, 0.0)
        s.hit("fx", riser(s.bar * 2), base + 58, 0, 0.2)
        # E: the bend, in E minor again: full brass, the full choir
        for k in (60, 68):
            vorne(s, "vorne", v_brass, base + k, "E3", 0.75, -0.15)
            vorne(s, "vorne", v_brass, base + k, "E3", 0.4, 0.15, octave=-1)
            vorne(s, "vorne", v_trumpet, base + k, "E4", 0.3, 0.0)
            drop(s, base + k + 2, full=True)
        s.play("sing", v_choir, choir_motif("E5"), base + 64, gain=0.55, position=0.0, legato=0.97)
        s.play("sing", v_choir, choir_motif("E5"), base + 66, gain=0.5, position=0.0, octave=-1, legato=0.97,
               vowel="oo")
        vorne(s, "vorne", v_trombone, base + 64, "E2", 0.55, -0.1, rhythm="aug", bend=False)
        s.hit("snare", snare_roll(s.bar * 0.95), base + 71, 0, 0.55)
    secs = [(-4, "intro: timpani, low-brass 8ths, the choir swelling, a snare roll")]
    secs += [(first, label) for first, _, _, label in VLA_PLAN]
    return s, vl_mix(s, kicks), None, (), secs


# Chorale (b): a new hymn over a falling bass, the choir and strings
BR_B_CH = ["Em", "B/D#", "Em/D", "A/C#", "C", "G/B", "Am", "B", "C", "Am", "F", "B"]
BR_B_TUNE = ["G5 - - - - - - - F#5 - - - - - - -", "F#5 - - - - - - - D#5 - - - - - - -",
             "E5 - - - - - - - G5 - - - - - - -", "A5 - - - - - - - E5 - - - - - - -",
             "E5 - - - - - - - G5 - - - - - - -", "G5 - - - - - - - F#5 - - - - - - -",
             "E5 - - - - - - - A5 - - - - - - -", "F#5 - - - - - - - D#5 - - - - - - -",
             "E5 - - - - - - - G5 - - - - - - -", "C6 - - - - - - - A5 - - - - - - -",
             "A5 - - - - - - - F5 - - - - - - -", "F#5 - - - - - - - D#5 - - - - - - -"]

VLB_PLAN = (
    (0, STATEMENT, "full",
     "A statement in canon: his motif in cellos and low brass (0), horns (2), trumpets bending (4), the drop at 6; "
     "full brass at 8 and trumpets at 12, the drop at 10 and 14"),
    (16, BR_B_CH, "march", "B chorale: the choir and strings sing a new hymn over a falling bass, trumpet stabs"),
    (28, ["Em", "Em", "C", "Em", "Em", "F"], "break", "C breakdown: the choir sings Vorne's motif (28, 32)"),
    (34, ["B", "B"], "build", "C build: timpani crescendo and snare roll"),
    (36, ["Gm", "Gm", "Eb", "Eb", "Cm", "Cm", "D", "D", "Gm", "Gm", "Eb", "Eb", "Cm", "Ab", "D", "D"], "full",
     "D variation in G minor: the motif in horns in 3-against-4 (36, 40, 44), stately in trumpets (48), "
     "the choir's drop from G (50)"),
    (52, ["Em", "Em", "C", "C", "Am", "F", "B", "Em", "Am", "F", "B", "B"], "drive",
     "E the bend: his motif augmented in full brass (52) bending into the augmented drop (56), trumpets (60)"),
    (64, ["Em", "Em", "C", "C", "Am", "F", "B", "B"], "full",
     "F turnaround: the roles swapped, the choir sings his motif (64), the brass plays the Choir's (66)"),
)


def last_stand_b():
    s = LoopSong(150, 4, 72)
    kicks = []
    vl_render(s, kicks)
    for base in s.sections():
        for first, chords, tex, _ in VLB_PLAN:
            for k, ch in enumerate(chords):
                vl_bar(s, kicks, base + first + k, ch, tex)
        for b in (0, 16, 36, 52, 64):
            s.hit("perc", crash(), base + b, 0, 0.4, 0.3)
        # A: the canon
        vorne(s, "vorne", v_strings_long, base + 0, "E2", 0.55, -0.3, bend=False)
        vorne(s, "vorne", v_trombone, base + 0, "E2", 0.45, -0.1, bend=False)
        vorne(s, "vorne", v_horn, base + 2, "E3", 0.65, 0.2, bend=False)
        vorne(s, "vorne", v_trumpet, base + 4, "B3", 0.4, 0.0)
        drop(s, base + 6)
        vorne(s, "vorne", v_brass, base + 8, "E3", 0.7, -0.15)
        vorne(s, "vorne", v_brass, base + 8, "E3", 0.35, 0.15, octave=-1)
        drop(s, base + 10)
        vorne(s, "vorne", v_trumpet, base + 12, "E4", 0.38, 0.1)
        drop(s, base + 14, gain=0.6)
        # B: the chorale
        s.play("sing", v_choir, BR_B_TUNE, base + 16, gain=0.6, position=0.1, legato=0.97)
        s.play("sing", v_choir, BR_B_TUNE, base + 16, gain=0.4, position=-0.1, octave=-1, legato=0.97, vowel="oo")
        s.play("tune", v_strings_long, BR_B_TUNE, base + 16, gain=0.35, position=-0.2, legato=0.98)
        for k, ch in enumerate(BR_B_CH):
            for nm in vc(ch)[0][:3]:
                s.hit("tune", v_trumpet(note_freq(transpose(nm, 12)), round(s.step * 1.5, 4), True), base + 16 + k,
                      0, 0.16, 0.15)
        # C: the choir sings Vorne's motif
        for k in (28, 32):
            vorne(s, "sing", v_choir, base + k, "E4", 0.6, 0.0, bend=False)
            vorne(s, "sing", v_choir, base + k, "E4", 0.35, 0.0, bend=False, octave=-1)
        # D: G minor, hemiola horns, trumpets stately, the drop from G
        for k in (36, 40, 44):
            vorne(s, "vorne", v_horn, base + k, "G3", 0.75, -0.15, rhythm="hemiola", bend=False)
            vorne(s, "vorne", v_trombone, base + k, "G2", 0.35, 0.15, rhythm="hemiola", bend=False)
        vorne(s, "vorne", v_trumpet, base + 48, "G4", 0.4, 0.1)
        vorne(s, "vorne", v_brass, base + 48, "G3", 0.5, -0.1)
        drop(s, base + 50, root="G3", full=True)
        s.hit("fx", riser(s.bar * 2), base + 50, 0, 0.2)
        # E: the bend, augmented
        vorne(s, "vorne", v_brass, base + 52, "E3", 0.75, -0.15, rhythm="aug")
        vorne(s, "vorne", v_brass, base + 52, "E3", 0.4, 0.15, rhythm="aug", octave=-1)
        drop(s, base + 56, full=True, aug=True)
        vorne(s, "vorne", v_trumpet, base + 60, "E4", 0.35, 0.1)
        vorne(s, "vorne", v_brass, base + 60, "E3", 0.6, -0.1)
        drop(s, base + 62, full=True)
        # F: the roles swapped
        vorne(s, "sing", v_choir, base + 64, "E4", 0.6, 0.0, bend=False)
        s.play("vorne", v_trombone, choir_motif("E4"), base + 66, gain=0.5, position=-0.1, octave=-1, legato=0.95)
        s.play("vorne", v_brass, choir_motif("E4"), base + 66, gain=0.45, position=0.1, legato=0.95)
        vorne(s, "sing", v_choir, base + 68, "E4", 0.55, 0.0, bend=False)
        s.hit("snare", snare_roll(s.bar * 0.95), base + 71, 0, 0.55)
    secs = [(-4, "intro: timpani, low-brass 8ths, the choir swelling, a snare roll")]
    secs += [(first, label) for first, _, _, label in VLB_PLAN]
    return s, vl_mix(s, kicks), None, (), secs


# ================================================================ track 27: the Ending, through-composed

STRAIN_D = ["D4 - - - - - A4 - D5 - - - - - E5 -", "F#5 - - - - - - - G5 - - - A5 - - -",  # the D major statement,
            "B5 - - - - - A5 - F#5 - - - - - D5 -", "E5 - - - - - - - - - - - C#5 - E5 -"]  # extended to 4 bars


def place_chords(s, plan, voice, group, gain_fn, **kw):
    for bar, ch in enumerate(plan):
        if ch is None:
            continue
        parts = ch if isinstance(ch, tuple) else (ch,)
        for j, c in enumerate(parts):
            st, ln = j * 16 // len(parts), 16 // len(parts)
            g = gain_fn(bar)
            if g:
                s.chord(group, voice, vc(c)[0], bar, st, ln, gain=g, spread=0.8, **kw)
                s.hit("bass", v_strings_long(note_freq(vc(c)[1]), round(s.step * ln, 4), 0.5), bar, st, 0.3)


ENA_CH = [None, None, "Dm", "Bb", "Gm", "A", "Dm", ("Bb", "C"),  # 0-7: the motif four times, the instruments joining
          "F", "C", "Dm", ("Bb", "A"),                             # 8-11: the second strain (the title's C-A-C-F)
          "Gm", "A", "Bb", "A",                                    # 12-15: the climb
          "D", "G", "Bm", "A",                                     # 16-19: the full statement in D major, extended
          "Gm/D", "D",                                             # 20-21: the Choir motif at peace
          "D", "D", "D"]                                           # 22-24: the open chord over a low E-flat
ENA_SECTIONS = [(0, "the solo horn alone, the main motif"), (2, "the motif again, the strings in under the horn"),
                (4, "the strings carry the motif, the piano joins"),
                (6, "the motif in strings and horns; its second bar continues (no F held) into"),
                (8, "the second strain (F major, the title's C-A-C-F phrase), strings and horns"),
                (12, "the climb: rising string phrases, horns, a timpani roll"),
                (16, "the full statement in D major, extended to four bars: brass, trumpet, strings, choir, timpani"),
                (20, "the Choir motif at peace in the strings, its E-flat resolving to D"),
                (22, "the open ending: D major over a low E-flat, the horn's first two notes, faded")]
STRAIN_A = ["C5 - - - - - A4 - C5 - - - F5 - - -", "E5 - - - - - - - G5 - - - E5 - C5 -",
            "D5 - - - - - - - F5 - - - A5 - - -", "Bb5 - - - A5 - - - G5 - - - E5 - - -"]
CLIMB_A = ["D5 - - - - - - - Bb4 - - - D5 - G5 -", "E5 - - - - - - - C#5 - - - E5 - A5 -",
           "F5 - - - - - - - D5 - - - F5 - Bb5 -", "A5 - - - - - - - - - - - C#5 - E5 -"]
MOTIF_ON = [MAIN_D4[0], "F5 - - - - - - - E5 - - - D5 - - -"]  # the motif's 4th statement, continuing


def open_ending(s, first, n, gain=0.32):
    for k in range(n):  # the low E-flat under D that does not resolve
        s.hit("eb", v_strings_long(note_freq("Eb2"), round(s.bar, 4), 0.8), first + k, 0, gain + 0.04 * k)
        s.hit("eb", v_sub(note_freq("Eb1"), round(s.bar * 0.95, 4)), first + k, 0, 0.18)


def stand_down_r85():
    s = LoopSong(75, 0, len(ENA_CH))
    gains = lambda b: 0.0 if b < 2 else 0.22 if b < 4 else 0.28 if b < 8 else 0.32 if b < 16 else \
        0.45 if b < 20 else 0.3 - 0.03 * max(0, b - 21)  # noqa: E731
    place_chords(s, ENA_CH, v_strings_long, "strings", gains, attack=0.6)
    # 0-7: the motif four times while the instruments join
    s.play("horn", v_horn, MAIN_D4, 0, gain=0.42, octave=-1)
    s.play("horn", v_horn, MAIN_D4, 2, gain=0.45, octave=-1)
    s.play("lead", v_strings_long, MAIN_D4, 4, gain=0.5, position=-0.1)
    s.play("horn", v_horn, MAIN_D4, 4, gain=0.25, position=0.25, octave=-1)
    s.play("lead", v_strings_long, MOTIF_ON + STRAIN_A + CLIMB_A, 6, gain=0.55, position=-0.1)
    s.play("lead", v_strings_long, MOTIF_ON, 6, gain=0.25, position=0.1, octave=1)
    s.play("horn", v_horn, MOTIF_ON + STRAIN_A, 6, gain=0.32, position=0.25, octave=-1)
    for b in range(4, 16):  # the piano: broken chords in 8ths
        ch = ENA_CH[b]
        for j, c in enumerate(ch if isinstance(ch, tuple) else (ch,)):
            v = vc(c)[0]
            ln = 16 // (2 if isinstance(ch, tuple) else 1)
            for st in range(j * ln, (j + 1) * ln, 2):
                s.hit("piano", v_epiano(note_freq(transpose(v[(st // 2) % len(v)], 12)), round(s.step * 3, 4), 0.5),
                      b, st, 0.2, -0.3 + 0.15 * ((st // 2) % 4))
    # 12-15: the climb's horns, a timpani roll into the statement
    for k, nm in enumerate(("G3", "A3", "Bb3", "A3")):
        s.hit("horn", v_horn(note_freq(nm), round(s.bar * 0.95, 4)), 12 + k, 0, 0.35, 0.25)
    for st in range(0, 16):
        s.hit("perc", timpani("A1"), 15, st, 0.1 + 0.03 * st)
    # 16-19: the full statement in D major, extended
    s.hit("perc", crash(), 16, 0, 0.3, 0.2)
    s.play("brass", v_brass, STRAIN_D, 16, gain=0.55, position=0.15, octave=-1)
    s.play("brass", v_trumpet, STRAIN_D, 16, gain=0.35, position=-0.15)
    s.play("lead", v_strings_long, STRAIN_D, 16, gain=0.45, octave=1)
    s.play("lead", v_strings_long, STRAIN_D, 16, gain=0.35)
    for b in range(16, 20):
        s.hit("perc", timpani(timp_note(vc(ENA_CH[b])[1])), b, 0, 0.6)
        s.chord("choirpad", v_choir, vc(ENA_CH[b])[0], b, gain=0.35, spread=0.8)
    # 20-21: the Choir motif at peace; 22-24: the open ending
    s.play("peace", v_strings_long, PEACE, 20, gain=0.55, position=0.1, legato=0.98)
    s.play("peace", v_strings_long, PEACE, 20, gain=0.35, position=-0.1, octave=-1, legato=0.98)
    open_ending(s, 21, 4)
    s.play("horn", v_horn, ["D3 - - - - - A3 - - - - - - - - -"], 22, gain=0.4, octave=0)
    mix = mixdown(s, {
        "horn": dict(hp=60, rev=(4.0, 0.45), seed=8561, gain=0.9),
        "lead": dict(hp=70, rev=(3.5, 0.4), wid=1.3, seed=8562, gain=0.75),
        "brass": dict(hp=60, rev=(3.5, 0.4), wid=1.2, seed=8570, gain=0.8),
        "strings": dict(hp=60, rev=(4.0, 0.45), wid=1.6, seed=8563, gain=0.55),
        "bass": dict(hp=30, rev=(3.0, 0.3), seed=8564, gain=0.5),
        "piano": dict(hp=90, rev=(3.0, 0.35), wid=1.4, seed=8565, gain=0.6),
        "perc": dict(hp=30, rev=(3.0, 0.35), damp=0.8, seed=8566, gain=0.75),
        "choirpad": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8567, gain=0.5),
        "peace": dict(hp=80, rev=(4.5, 0.5), wid=1.5, seed=8568, gain=0.8),
        "eb": dict(hp=25, rev=(4.0, 0.4), seed=8569, gain=0.6),
    }, [], depth=0.0)
    return s, mix, len(ENA_CH), ENA_SECTIONS


ENB_CH = ["D", "A", "D", "Bm", "G", "A", "D", "Bm", "G", "A",   # 0-1 piano; 2-9 the motif four times, joining
          "G", "D/F#", "Em", "A",                               # 10-13: the second strain in the flute
          "G", "D/F#", "Em", "A",                               # 14-17: the strings take it, the flute's descant
          "Bm", "G", "Em", "A",                                 # 18-21: the climb
          "D", "G", "Bm", "A",                                  # 22-25: the full statement in D major, extended
          "Gm/D", "D",                                          # 26-27: the Choir motif at peace in the flute
          "D", "D", "D"]                                        # 28-30: the open chord over a low E-flat
ENB_SECTIONS = [(0, "the soft electric piano alone"), (2, "the flute's main motif (D major) over the piano"),
                (4, "the motif from A, the strings in"), (6, "the motif again, the cellos' counter-line"),
                (8, "the motif in flute and horn, timpani; its second bar continues into"),
                (10, "the second strain in the flute (G, D/F#, Em, A)"),
                (14, "the strings take the strain an octave lower, the flute's descant; climbing into"),
                (18, "the climb: flute and strings rising, horns, timpani"),
                (22, "the full statement in D major, extended: brass, strings, flute, choir, timpani"),
                (26, "the Choir motif at peace in the flute, its E-flat resolving to D"),
                (28, "the open ending: D major over a low E-flat, the piano's first notes of the motif, faded")]
STRAIN_B = ["B5 - - - - - D6 - B5 - - - A5 - G5 -", "A5 - - - - - - - F#5 - - - D5 - - -",
            "E5 - - - - - G5 - B5 - - - - - E6 -", "D6 - - - C#6 - - - A5 - - - - - - -"]
STRAIN_B2 = ["B4 - - - - - D5 - B4 - - - A4 - G4 -", "A4 - - - - - - - F#4 - - - D4 - - -",
             "E4 - - - - - G4 - B4 - - - - - E5 -", "E5 - - - F#5 - - - G5 - - - A5 - - -"]
DESCANT_B = ["D6 - - - - - - - - - - - - - - -", "D6 - - - - - - - A5 - - - - - - -",
             "B5 - - - - - - - - - - - G5 - - -", "A5 - - - - - - - C#6 - - - - - - -"]
CLIMB_B = ["F#5 - - - - - - - B5 - - - D6 - - -", "D6 - - - - - - - B5 - - - G5 - - -",
           "G5 - - - - - - - B5 - - - E6 - - -", "C#6 - - - - - - - D6 - - - E6 - - -"]
MOTIF_ON_B = ["D5 - - - - - A5 - D6 - - - - - E6 -", "F#6 - - - - - - - E6 - - - C#6 - - -"]


def home_light_r85():
    s = LoopSong(90, 0, len(ENB_CH))
    gains = lambda b: 0.0 if b < 4 else 0.24 if b < 10 else 0.3 if b < 18 else 0.36 if b < 22 else \
        0.45 if b < 26 else 0.3 - 0.03 * max(0, b - 27)  # noqa: E731
    place_chords(s, ENB_CH, v_strings_long, "strings", gains, attack=0.6)
    for b, ch in enumerate(ENB_CH):  # the soft electric piano throughout, quarter-note broken chords
        v = vc(ch)[0]
        for st in range(0, 16, 4 if b < 28 else 8):
            s.hit("piano", v_epiano(note_freq(transpose(v[(st // 4) % len(v)], 12)), round(s.step * 5, 4), 0.45),
                  b, st, 0.28 if b < 2 else 0.24 if b < 28 else 0.16, -0.2 + 0.13 * ((st // 4) % 4))
    # 2-9: the motif four times while the instruments join
    s.play("flute", v_flute, MAIN_D4_MAJ, 2, gain=0.5, position=0.1, octave=1)
    s.play("flute", v_flute, ["A4 - - - - - E5 - A5 - - - - - B5 -", "C#6 - - - - - - - - - - - . . . ."],
           4, gain=0.5, position=0.1)
    s.play("flute", v_flute, MAIN_D4_MAJ, 6, gain=0.5, position=0.1, octave=1)
    s.play("lead", v_strings_long, MAIN_D4_MAJ, 6, gain=0.3, position=-0.25, octave=-1)
    s.play("flute", v_flute, MOTIF_ON_B + STRAIN_B, 8, gain=0.55, position=0.1)
    s.play("horn", v_horn, MOTIF_ON_B, 8, gain=0.4, position=-0.2, octave=-2)
    for b in (8, 9):
        s.hit("perc", timpani(timp_note(vc(ENB_CH[b])[1])), b, 0, 0.35)
    # 14-17: the strings take the strain, the flute's descant
    s.play("lead", v_strings_long, STRAIN_B2, 14, gain=0.5, position=-0.1)
    s.play("lead", v_strings_long, STRAIN_B2, 14, gain=0.25, position=0.1, octave=-1)
    s.play("flute", v_flute, DESCANT_B, 14, gain=0.4, position=0.15)
    # 18-21: the climb
    s.play("flute", v_flute, CLIMB_B, 18, gain=0.5, position=0.1)
    s.play("lead", v_strings_long, CLIMB_B, 18, gain=0.4, position=-0.1, octave=-1)
    for k, nm in enumerate(("B3", "G3", "G3", "A3")):
        s.hit("horn", v_horn(note_freq(nm), round(s.bar * 0.95, 4)), 18 + k, 0, 0.35, -0.2)
    for st in range(0, 16):
        s.hit("perc", timpani("A1"), 21, st, 0.1 + 0.03 * st)
    # 22-25: the full statement in D major, extended
    s.hit("perc", crash(), 22, 0, 0.28, 0.2)
    s.play("brass", v_brass, STRAIN_D, 22, gain=0.5, position=0.15, octave=-1)
    s.play("lead", v_strings_long, STRAIN_D, 22, gain=0.45)
    s.play("flute", v_flute, STRAIN_D, 22, gain=0.4, position=0.1, octave=1)
    for b in range(22, 26):
        s.hit("perc", timpani(timp_note(vc(ENB_CH[b])[1])), b, 0, 0.55)
        s.chord("choirpad", v_choir, vc(ENB_CH[b])[0], b, gain=0.3, spread=0.8)
    # 26-27: the Choir motif at peace in the flute; 28-30 the open ending
    s.play("flute", v_flute, PEACE, 26, gain=0.5, position=0.1)
    s.play("peace", v_strings_long, PEACE, 26, gain=0.3, position=-0.1, octave=-1, legato=0.98)
    open_ending(s, 27, 4)
    s.play("piano", v_epiano, ["D4 - - - - - A4 - D5 - - - - - - -"], 28, gain=0.3, position=0.1)
    mix = mixdown(s, {
        "piano": dict(hp=90, rev=(2.8, 0.3), wid=1.4, seed=8571, gain=0.65),
        "flute": dict(hp=200, rev=(3.2, 0.35), seed=8572, gain=0.8),
        "lead": dict(hp=70, rev=(3.5, 0.4), wid=1.3, seed=8573, gain=0.7),
        "horn": dict(hp=60, rev=(3.5, 0.4), seed=8580, gain=0.8),
        "brass": dict(hp=60, rev=(3.5, 0.4), wid=1.2, seed=8581, gain=0.75),
        "strings": dict(hp=60, rev=(3.8, 0.4), wid=1.6, seed=8574, gain=0.5),
        "bass": dict(hp=30, rev=(3.0, 0.3), seed=8575, gain=0.45),
        "perc": dict(hp=30, rev=(3.0, 0.35), damp=0.8, seed=8576, gain=0.7),
        "choirpad": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8577, gain=0.45),
        "peace": dict(hp=80, rev=(4.5, 0.5), wid=1.5, seed=8578, gain=0.6),
        "eb": dict(hp=25, rev=(4.0, 0.4), seed=8579, gain=0.6),
    }, [], depth=0.0)
    return s, mix, len(ENB_CH), ENB_SECTIONS


# ================================================================ main

LOOPS = {"a16-a": ("act7-a-theme-r85-a", lambda: far_shore_r85("a")),
         "a16-b": ("act7-a-theme-r85-b", lambda: far_shore_r85("b")),
         "vls-a": ("vorne-last-stand-r85-a", last_stand_a), "vls-b": ("vorne-last-stand-r85-b", last_stand_b)}
ONCE = {"end-a": ("ending-r85-a", stand_down_r85), "end-b": ("ending-r85-b", home_light_r85)}


def _report(path, extra=""):
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s{extra}  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS"
          f"  true peak {tp:.1f} dBTP", flush=True)
    return dec


def _print_sections(name, secs, t0, bar_s, end_s):
    print(f"{name} sections:")
    for k, (bar, label) in enumerate(secs):
        t = t0 + bar * bar_s
        nxt = t0 + secs[k + 1][0] * bar_s if k + 1 < len(secs) else end_s
        print(f"  {int(t // 60)}:{t % 60:05.2f}-{int(nxt // 60)}:{nxt % 60:05.2f}  {label}")


STEM_UNDER_DB = 5.0


def render_loop(key, out, show):
    name, fn = LOOPS[key]
    s, rest, stem, stem_bars, secs = fn()
    if stem is not None:  # level the answer to round 84's choir stem: about 5 dB under the mix over its statements
        base = s.sections()[1]
        win = [(int(s.pos(base + b) * SR), int(s.pos(base + b + 2) * SR)) for b in stem_bars if b != 16]
        er = sum(np.sum(rest[:, a:b] ** 2) for a, b in win)
        es = sum(np.sum(stem[:, a:b] ** 2) for a, b in win)
        r = 10 ** (-STEM_UNDER_DB / 10)
        stem = stem * np.sqrt(r * er / (es * (1 - r)))
    mix = rest + stem if stem is not None else rest
    if stem is not None:
        gaps = []
        for bar in stem_bars:
            a, b = int(s.pos(base + bar) * SR), int(s.pos(base + bar + 2) * SR)
            gaps.append(db(np.sqrt(np.mean(mix[:, a:b] ** 2))) - db(np.sqrt(np.mean(stem[:, a:b] ** 2))))
        print(f"{name}: the answer (choir stem) " + ", ".join(f"{g:.1f}" for g in gaps) + " dB under the mix (bars "
              + ", ".join(str(b) for b in stem_bars) + ")", flush=True)
    mix = mix - mix.mean(axis=1, keepdims=True)
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    mix = master(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, -2.0)[:, :mix.shape[1]]
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    _report(path, f"  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s  (samples {loop_start}+{loop_len})"
                  f"  seam {seam_ratio(dec, loop_start, loop_len):.2f}")
    if show:
        _print_sections(name, secs, s.intro * s.bar, s.bar, (loop_start + loop_len) / SR)


def render_once(key, out, show):
    name, fn = ONCE[key]
    s, mix, bars, secs = fn()
    end = int(bars * s.bar * SR) + ns(3.0)  # the last chord's ring and the hall
    mix = mix[:, :end]
    mix = mix - mix.mean(axis=1, keepdims=True)
    fo = int((s.bar * 2.0 + 3.0) * SR)  # fade over the last two bars and the ring
    mix[:, -fo:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fo))) ** 1.5
    mix = master(mix, -14.0, -2.0)
    mix[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    _report(write_ogg(out / f"{name}.ogg", mix), "  (one-shot, no loop)")
    if show:
        _print_sections(name, secs, 0.0, s.bar, end / SR)


def main(argv):
    out = OUT
    show = "--sections" in argv
    argv = [a for a in argv if a != "--sections"]
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or [*LOOPS, *ONCE]:
        if key in LOOPS:
            render_loop(key, out, show)
        else:
            render_once(key, out, show)


if __name__ == "__main__":
    main(sys.argv[1:])
