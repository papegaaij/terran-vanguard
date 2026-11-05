#!/usr/bin/env python3
"""Concept round 84 (M11 "Act 7 - Beyond the Gate" design phase, step AUD-R, 2026-11-04): the Act 7 music sketches of
the music README's *Act 7 music (draft)*; pending the user's review. Two sketches each for track 16 (the Act 7 A theme),
track 17 (the Act 7 B theme), track 34 (Vorne's last stand, Level 48), track 27 (the Ending) and track 28 (the Credits),
and the Level 49 memory filter heard on one echo two ways.

The themes use round 72's format (music_r72.py): intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH Vorbis comments
(samples), a sample-exact loop (each tempo gives a whole-sample 16th step at 44.1 kHz), -14 LUFS with a -2 dBFS
sample-peak ceiling. The memory files are cut from the game's own track 19 (assets/music/iron-sovereign.ogg, read
only) and levelled to -14 LUFS like the sketches (the game would play the copy 4 dB under: the README's rule).

Outputs (design/audio/music/concept/, or --out DIR):
  act7-a-theme-r84-a.ogg  "Far Shore"  140 BPM, D minor (the main motif's home key)
      Synth-orchestral over a driving breakbeat: a low string ostinato in 8ths, the main motif (D A D E F) in horns at
      loop bars 0, 8, 20 and 24, answered by the Choir motif (E D C B, F E: E-rooted, its F rubbing the key's E) in the
      formant choir at bars 4, 12 and 28; a breakdown (bars 16-19) of the choir alone over a pad.
  act7-a-theme-r84-b.ogg  "Event Horizon"  126 BPM, D minor
      Darker and electronic: an industrial-trance pulse (four-on-the-floor, an off-beat reese, struck sheet metal on the
      backbeat), reversed pad swells into every 4th bar, the main motif as a supersaw lead (bars 8, 12, 24, 28), the
      Choir motif's tritone drop (B F E) as a falling choir riff under it from bar 8.
  act7-b-theme-r84-a.ogg  "Inner Sphere"  105 BPM half-time, E Phrygian
      Slow heavy drums (a deep kick, a huge half-time snare), a sub drone on E, reversed choir and string swells, the
      Choir motif AUGMENTED (one note a half bar, four bars) in low brass at bars 0, 8 and 16, the main motif's first
      three notes only (E B E, never completed) in a muted horn at bars 5, 13 and 21.
  act7-b-theme-r84-b.ogg  "Hollow Light"  98 BPM, E minor
      Ambient and glassy: bowed-glass pads (sine partials, slow bow), a soft heartbeat kick, the Choir motif in one
      wordless voice ("oo", bars 4 and 12), the main motif once per loop in a distant trumpet (bar 16).
  vorne-last-stand-r84-a.ogg  "Fused"  150 BPM, E minor (track 18's key)
      Orchestral-martial (track 19's family): snare rudiments, timpani, low-brass 8ths, galloping strings, the choir in
      long chords over Em - C - Am - B; Vorne's motif (E G B D-sharp) in cold brass at bars 0, 8, 16 and 24, its last
      note bending a semitone down, the choir answering with the Choir motif's tritone drop (B F E).
  vorne-last-stand-r84-b.ogg  "Overgrown"  147 BPM, E minor
      Industrial: track 19's kind of 16th machine riff, distorted and slowly detuning over the loop (down a third of a
      semitone, back at the loop), a four-on-the-floor kit with sheet metal, the choir chanting the Choir motif in 8ths;
      in the loop's last third (bars 22-31) the choir sings Vorne's motif.
  ending-r84-a.ogg  "Stand Down, Aegis"  75 BPM, D minor to D major
      A solo horn alone (the intro, the main motif), then strings carry it, piano joins, then the full orchestral
      statement in D major (horns, trumpet, strings, timpani, a wordless choir; loop bars 8-11); the Choir motif once,
      at peace, in the strings (bars 12-13, its flat-2 resolving onto the tonic over a D major chord); the last bars
      over a quiet low E-flat under D that does not resolve before the loop.
  ending-r84-b.ogg  "Home Light"  90 BPM, D major
      The hangar theme's colours (its flute and soft electric piano) grown into an orchestra: the main motif in the
      flute over the piano, strings in, the major statement with horns and timpani, the Choir motif at peace in the
      flute, the low E-flat under the last bars.
  credits-r84-a.ogg  "Coalition Forever"  140 BPM, D minor
      The title theme (track 1's chords and melodies) as tracker trance (track 4's family): a four-on-the-floor kick,
      an off-beat bass, a 16th pulse arpeggio, claps, a supersaw pad; the main motif and the title's melodies in the
      supersaw lead; a breakdown (bars 16-23) on the title's slow C melody.
  credits-r84-b.ogg  "Seven Acts"  150 BPM, D minor
      Synth-orchestral over a breakbeat: the main motif told in each act's colour, four bars each (Earth's brass and
      break, Mars's double reed and hand drums, Europa's sonar and pad, the belt's struck metal and drop bass,
      Jupiter's storm strings and thunder, the Choir's voices), ending on the title's brass statement (bars 24-31).
  memory-filter-r84-a.ogg  (about 40 s) Level 49's memory filter as drafted, on the Iron Sovereign's echo (track 19,
      from its loop start): 6 s of the built track, a 2 s crossfade as the memory forms, then the memory copy: slowed
      6 % with its pitch (about a semitone down), a low-pass at 2.5 kHz (12 dB/octave), a 3.5 s hall at 40 % wet, the
      stereo narrowed to 60 %.
  memory-filter-r84-b.ogg  the same, a heavier memory: slowed 10 % (about 1.8 semitones) with a slow tape wow
      (+-0.3 %, 0.5 Hz), a low-pass at 1.4 kHz (24 dB/octave), a 5 s hall at 55 % wet, the stereo at 30 %.

The `choir` stem of tracks 16 and 17 (the Choir motif's voices) is its own group; its level against the whole mix over
its 2-bar statements is printed (the README's "about 6 dB under"). No bubbly or popping sounds: no bubble layer, no
resonant short plucks, struck metal with an inharmonic ring, thunder only as band noise, every pitched voice with a
soft attack except the basses.

Usage: python3 tools/concept/audio/music_r84.py [a16-a|a16-b|b17-a|b17-b|vls-a|vls-b|end-a|end-b|cred-a|cred-b|mem-a|mem-b ...]
       [--out DIR]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks, the loop seam and a spectrogram.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (clap, crash, hat, hp, kick, ns, riser, snare, taiko, timpani, tom, transpose,  # noqa: E402
                   v_arp, v_bass, v_brass, v_lead, v_pad, v_strings_long, v_strings_short)
from music_r02 import (GALLOP, T_CH, T_MEL_B, T_MEL_C, LoopSong, add_tail, doum, lp, seam_ratio, tek,  # noqa: E402
                       v_choir, v_dbass, v_epiano, v_flute, v_reed, v_reese, v_sub)
from music_r02 import sonar  # noqa: E402
from music_r03 import (anvil, choir_motif, heartbeat, human_motif, mixdown, thunder, v_organ,  # noqa: E402
                       v_tremolo, v_trumpet, vorne_motif)
from music_r48 import deep_kick  # noqa: E402
from music_r60 import sheet_metal, snare_roll, v_seq  # noqa: E402
from music_r72 import v_brass_bend  # noqa: E402
from synth import (SR, adsr, db, decode, fade, lufs, master, reverb, saturate, sine, svf,  # noqa: E402
                   write_ogg, note_freq)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
ASSETS = ROOT / "assets" / "music"

MAIN_D4 = human_motif("D4")          # D A D E F
MAIN_D4_MAJ = human_motif("D4", True)  # D A D E F-sharp
CHOIR_E4 = choir_motif("E4")         # E D C B, F E


# ================================================================ shared voices

@lru_cache(None)
def v_glass(freq, dur):
    """Bowed glass: a few sine partials (slightly stretched, as a glass's), a slow bow, a gentle beating."""
    n = ns(dur + 1.2)
    t = np.arange(n) / SR
    x = sum(a * sine(freq * r, n) for r, a in ((1.0, 1.0), (2.01, 0.35), (3.03, 0.12), (4.2, 0.05)))
    x *= 1 + 0.12 * np.sin(2 * np.pi * 0.7 * t + freq)
    return x * adsr(n, 0.9, 0.5, 0.85, 1.0, gate=dur) * 0.6


@lru_cache(None)
def v_horn(freq, dur):
    """A soft horn: v_brass with a slower attack, low-passed (no brassy edge)."""
    n = ns(dur + 0.4)
    x = v_brass(freq, dur)
    x = np.pad(x, (0, max(0, n - len(x))))[:n]
    x = svf(svf(x, 1500, q=0.6), 1500, q=0.6)
    return x * np.minimum(1, np.arange(n) / ns(0.08))


@lru_cache(None)
def v_muted(freq, dur):
    """A muted horn: the soft horn through a nasal band-pass, quieter."""
    x = v_horn(freq, dur)
    return svf(x, 900, q=1.6, mode="bp") * 1.6


def reversed_swell(voice, freq, dur):
    """A note played backwards: it swells into the bar line."""
    return np.ascontiguousarray(voice(freq, dur)[::-1])


def _sec(i, bounds):
    for k, b in enumerate(bounds):
        if i < b:
            return "ABCDEFGHI"[k]
    return "ABCDEFGHI"[len(bounds)]


# ================================================================ track 16 a: Far Shore (140 BPM, D minor)

FS_CH = {  # pad voicing, bass root
    "Dm": (["D3", "F3", "A3", "D4"], "D2"),
    "Bb": (["D3", "F3", "Bb3", "D4"], "Bb1"),
    "Gm": (["D3", "G3", "Bb3", "D4"], "G1"),
    "A": (["E3", "A3", "C#4", "E4"], "A1"),
    "F": (["C3", "F3", "A3", "C4"], "F1"),
    "C": (["E3", "G3", "C4", "E4"], "C2"),
}
FS_INTRO = ["Dm", "Dm", "Bb", "A"]
FS_LOOP = (["Dm", "Dm", "Bb", "Bb", "Gm", "Gm", "A", "A"]    # A: ostinato and break, the horns at 0, the choir at 4
           + ["Dm", "Dm", "Bb", "Bb", "F", "C", "A", "A"]    # B: the horns at 8, the choir at 12
           + ["Dm", "Bb", "Gm", "A"]                         # C: the breakdown, the choir alone over a pad
           + ["Dm", "Dm", "Bb", "Bb", "Gm", "Gm", "A", "A"]  # D: all in, the horns at 20 and 24
           + ["Dm", "Dm", "Bb", "A"])                        # E: the choir at 28 over the groove
FS_KICK = [(0, 1.0), (6, 0.55), (10, 0.85)]
FS_SNARE = [(4, 0.85), (12, 0.85), (7, 0.15), (9, 0.12), (14, 0.2)]
FS_HORNS = (0, 8, 20, 24)
FS_CHOIR = (4, 12, 16, 28)


def far_shore():
    s = LoopSong(140, 4, len(FS_LOOP))
    kicks = []
    for bar, name in enumerate(FS_INTRO):  # intro: the ostinato alone, a pad swelling, the break from bar 2
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
                for st in range(0, 16, 2):  # the low string ostinato: root, root, octave, fifth
                    semis = (0, 0, 12, 0, 7, 0, 12, 10)[st // 2]
                    s.hit("ost", v_strings_short(note_freq(transpose(root, 12 + semis)), round(s.step * 1.6, 4)),
                          bar, st, 0.42 if st % 4 == 0 else 0.32, -0.2)
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 7.5, 4)), bar, 0, 0.36)
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 5.5, 4)), bar, 10, 0.3)
                s.chord("pad", v_strings_long, pad, bar, gain=0.26, spread=0.7)
                if i % 4 == 3:
                    s.hit("perc", timpani("A1"), bar, 12, 0.6)
            else:
                s.chord("pad", v_pad, pad, bar, gain=0.4, spread=0.8)
                s.hit("sub", v_sub(note_freq(transpose(root, 12 if root.endswith("1") else 0)),
                                   round(s.step * 15, 4)), bar, 0, 0.4)
                if i == 19:
                    for st in range(16):
                        s.hit("drums", snare(True), bar, st, 0.12 + 0.04 * st, 0.05)
                    s.hit("fx", riser(s.bar), bar, 0, 0.25)
        for k in FS_HORNS:  # the main motif in horns
            s.play("horn", v_brass, MAIN_D4, base + k, gain=0.6, position=0.15)
            s.play("horn", v_horn, MAIN_D4, base + k, gain=0.5, position=-0.15, octave=-1)
        for k in FS_CHOIR:  # the Choir motif answers in the formant choir (the `choir` stem)
            g = 0.75 if k == 16 else 0.6
            s.play("choir", v_choir, CHOIR_E4, base + k, gain=g, position=-0.1, legato=0.95)
            s.play("choir", v_choir, CHOIR_E4, base + k, gain=g * 0.6, position=0.1, octave=-1, legato=0.95,
                   vowel="oo")
            s.play("choir", v_brass, CHOIR_E4, base + k, gain=0.2, octave=-2)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "hats": dict(hp=700, gain=0.35),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=8401, gain=0.7),
        "fx": dict(gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.6, 0.2), seed=8402, gain=0.55),
        "bass": dict(hp=30, duck=True, gain=0.5),
        "sub": dict(hp=25, gain=0.5),
        "pad": dict(hp=90, duck=True, rev=(3.2, 0.4), wid=1.5, seed=8403, gain=0.45),
        "horn": dict(hp=70, rev=(2.8, 0.3), seed=8404, gain=0.8),
    }, kicks, depth=0.4)
    choir = s.tl.group("choir")
    choir = reverb(hp(choir, 110), seconds=4.0, mix=0.45, seed=8405)[:, :s.tl.n]
    return s, mix, choir * 0.7, FS_CHOIR


# ================================================================ track 16 b: Event Horizon (126 BPM, D minor)

EH_LOOP = (["Dm", "Dm", "Bb", "Bb", "Gm", "Gm", "A", "A"]    # A: the pulse
           + ["Dm", "Dm", "Bb", "Bb", "Gm", "Gm", "A", "A"]  # B: the lead's motif, the choir's drop riff under it
           + ["Dm", "Bb", "Gm", "A", "Dm", "Bb", "Gm", "A"]  # C: kick out, the pads and the riff
           + ["Dm", "Dm", "Bb", "Bb", "Gm", "Gm", "A", "A"])  # D: all in
EH_LEAD = (8, 12, 24, 28)
EH_DROP = ["B3 - - - F3 - - - E3 - - - - - - -"]  # the tritone drop as a riff, one bar


def event_horizon():
    s = LoopSong(126, 4, len(EH_LOOP))
    kicks = []
    for bar in range(4):  # intro: the off-beat reese and a reversed swell, the kick from bar 2
        pad, root = FS_CH[FS_INTRO[bar]]
        for st in range(2, 16, 4):
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 1.6, 4)), bar, st, 0.25 + 0.05 * bar)
        if bar >= 2:
            for beat in range(4):
                s.hit("drums", kick(120), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    for nm in FS_CH["A"][0]:
        s.hit("swell", reversed_swell(v_strings_long, note_freq(nm), round(s.bar * 2, 4)), 2, 0, 0.18)
    for base in s.sections():
        for i, name in enumerate(EH_LOOP):
            bar = base + i
            pad, root = FS_CH[name]
            sec = _sec(i, (8, 16, 24))
            if i in (0, 8, 24):
                s.hit("perc", crash(), bar, 0, 0.35, 0.3)
            if sec != "C":
                for beat in range(4):
                    s.hit("drums", kick(120), bar, beat * 4, 0.95)
                    kicks.append(s.pos(bar, beat * 4))
                for st in (4, 12):
                    s.hit("drums", clap(), bar, st, 0.45)
                    s.hit("metal", sheet_metal(240.0, 5 + (i % 3)), bar, st, 0.35, 0.2)
                for st in range(2, 16, 4):
                    s.hit("hats", hat(True), bar, st, 0.15, 0.3)
                for st in range(16):
                    s.hit("hats", hat(), bar, st, 0.06, -0.3)
            elif i % 4 == 3:
                for st in range(8, 16):
                    s.hit("drums", kick(120), bar, st, 0.2 + 0.08 * (st - 8))
                    kicks.append(s.pos(bar, st))
            for st in range(2, 16, 4):  # the off-beat bass
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 1.6, 4)), bar, st, 0.4)
            for st in range(16):  # the 16th pulse, filtered low
                s.hit("pulse", v_seq(note_freq(transpose(root, 24 + (0, 7, 12, 7)[st % 4])), round(s.step * 0.6, 4)),
                      bar, st, 0.18 if sec != "A" else 0.12, 0.35 if st % 2 else -0.35)
            s.chord("pad", v_pad, pad, bar, gain=0.22 if sec != "C" else 0.38, spread=0.8, bright=1200.0)
            if i % 4 == 3:  # reversed pad swells into every fourth bar line
                nxt = FS_CH[EH_LOOP[(i + 1) % len(EH_LOOP)]][0]
                for nm in nxt:
                    s.hit("swell", reversed_swell(v_strings_long, note_freq(nm), round(s.bar, 4)), bar, 0, 0.2)
            if sec != "A":  # the Choir motif's tritone drop as a falling riff (the `choir` stem)
                s.play("choir", v_choir, EH_DROP, bar, gain=0.5, position=-0.1, legato=0.95)
                s.play("choir", v_choir, EH_DROP, bar, gain=0.3, position=0.1, octave=-1, legato=0.95, vowel="oo")
        for k in EH_LEAD:
            s.play("lead", v_lead, human_motif("D5"), base + k, gain=0.42, position=0.1)
            s.play("lead", v_lead, MAIN_D4, base + k, gain=0.25, position=-0.1)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "metal": dict(hp=160, rev=(1.8, 0.3), seed=8411, gain=0.5),
        "hats": dict(hp=700, gain=0.35),
        "perc": dict(hp=35, gain=0.5),
        "bass": dict(hp=30, duck=True, gain=0.55),
        "pulse": dict(hp=120, duck=True, dly=(3, 0.3, 0.2, 3500), seed=8412, gain=0.5),
        "pad": dict(hp=100, duck=True, rev=(3.5, 0.45), wid=1.6, seed=8413, gain=0.45),
        "swell": dict(hp=90, rev=(3.0, 0.4), wid=1.5, seed=8414, gain=0.5),
        "lead": dict(hp=200, dly=(3, 0.3, 0.2, 4500), rev=(2.2, 0.25), wid=1.3, seed=8415, gain=0.55),
    }, kicks, depth=0.45)
    choir = s.tl.group("choir")
    choir = reverb(hp(choir, 120), seconds=3.8, mix=0.45, seed=8416)[:, :s.tl.n]
    return s, mix, choir * 0.75, (8, 16, 24)


# ================================================================ track 17 a: Inner Sphere (105 BPM half-time, E Phrygian)

IS_CH = {  # string voicing, sub root
    "Em": (["E3", "B3", "E4", "G4"], "E1"),
    "F": (["F3", "A3", "C4", "F4"], "F1"),
    "Dm": (["D3", "F3", "A3", "D4"], "D2"),
    "C": (["E3", "G3", "C4", "E4"], "C2"),
}
IS_LOOP = (["Em", "Em", "F", "Em", "Em", "Em", "Dm", "F"]   # A: the drums and the augmented motif
           + ["Em", "Em", "F", "Em", "C", "C", "Dm", "F"]  # B: the reversed choir in
           + ["Em", "Em", "F", "Em", "Em", "F", "Dm", "Em"])  # C: thinning to the pulse
IS_AUG = ["E3 - - - - - - - D3 - - - - - - -", "C3 - - - - - - - B2 - - - - - - -",
          "F2 - - - - - - - - - - - - - - -", "E2 - - - - - - - - - - - - - - -"]
IS_FRAG = ["E4 - - - - - B4 - E5 - - - - - - -"]  # the main motif's first three notes, never completed
IS_MOTIF = (0, 8, 16)


def inner_sphere():
    s = LoopSong(105, 4, len(IS_LOOP))
    kicks = []
    for bar in range(4):  # intro: the drone and a reversed choir swell; the drums from bar 2
        s.hit("sub", v_sub(note_freq("E1"), round(s.bar, 4)), bar, 0, 0.45)
        s.chord("strings", v_strings_long, IS_CH["Em"][0], bar, gain=0.2 + 0.05 * bar, attack=0.8, spread=0.8)
        if bar >= 2:
            s.hit("drums", deep_kick(), bar, 0, 0.95)
            kicks.append(s.pos(bar, 0))
            s.hit("snare", snare(), bar, 8, 0.6)
    for nm in ("E4", "B3", "G3"):
        s.hit("rev", reversed_swell(v_choir, note_freq(nm), round(s.bar * 2, 4)), 2, 0, 0.3)
    for base in s.sections():
        for i, name in enumerate(IS_LOOP):
            bar = base + i
            pad, root = IS_CH[name]
            sec = _sec(i, (8, 16))
            thin = sec == "C" and i >= 20
            # slow heavy drums: the deep kick on 1 (and the and-of-2), the half-time snare on 3
            s.hit("drums", deep_kick(), bar, 0, 1.0)
            kicks.append(s.pos(bar, 0))
            if not thin:
                s.hit("drums", deep_kick(), bar, 6, 0.55)
                kicks.append(s.pos(bar, 6))
                s.hit("snare", snare(), bar, 8, 0.8, 0.05)
                s.hit("snare", clap(), bar, 8, 0.3)
                s.hit("perc", taiko(), bar, 11, 0.35, -0.2)
                for st in range(2, 16, 4):
                    s.hit("hats", hat(), bar, st, 0.08, 0.3)
            if i % 8 == 7:
                for st, p in ((12, 120), (13, 105), (14, 92), (15, 80)):
                    s.hit("perc", tom(p), bar, st, 0.45, (st - 13.5) / 3)
            s.hit("sub", v_sub(note_freq(root), round(s.bar, 4)), bar, 0, 0.5)
            s.chord("strings", v_strings_long, pad, bar, gain=0.22 if not thin else 0.14, attack=0.6, spread=0.8)
            if sec != "A" and i % 2 == 0:  # reversed choir and string swells into the bar
                for nm in pad[1:]:
                    s.hit("rev", reversed_swell(v_choir, note_freq(transpose(nm, 12)), round(s.bar * 2, 4)), bar, 0,
                          0.22)
            if i % 4 == 1:
                s.hit("rev", reversed_swell(v_strings_long, note_freq(transpose(root, 24)), round(s.bar, 4)), bar, 0,
                      0.25)
        for k in IS_MOTIF:  # the Choir motif augmented in low brass (the `choir` stem: brass + choir double)
            s.play("choir", v_brass, IS_AUG, base + k, gain=0.55, position=-0.1)
            s.play("choir", v_organ, IS_AUG, base + k, gain=0.3, position=0.1)
            s.play("choir", v_choir, IS_AUG, base + k, gain=0.35, octave=1, legato=0.97, vowel="oo")
        for k in (5, 13, 21):  # the main motif in fragments, a muted horn
            s.play("horn", v_muted, IS_FRAG, base + k, gain=0.55, position=0.3)
    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.95),
        "snare": dict(hp=110, rev=(3.8, 0.45), damp=0.7, seed=8421, gain=0.7),
        "perc": dict(hp=35, rev=(3.0, 0.35), damp=0.8, seed=8422, gain=0.7),
        "hats": dict(hp=800, gain=0.3),
        "sub": dict(hp=25, duck=True, gain=0.55),
        "strings": dict(hp=80, duck=True, rev=(4.0, 0.45), wid=1.6, seed=8423, gain=0.5),
        "rev": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8424, gain=0.5),
        "horn": dict(hp=120, rev=(4.0, 0.5), seed=8425, gain=0.8),
    }, kicks, depth=0.35)
    choir = s.tl.group("choir")
    choir = reverb(hp(choir, 50), seconds=3.5, mix=0.35, seed=8426)[:, :s.tl.n]
    return s, mix, choir * 0.8, IS_MOTIF


# ================================================================ track 17 b: Hollow Light (98 BPM, E minor)

HL_CH = {
    "Em": ["E3", "B3", "E4", "G4"],
    "C": ["E3", "G3", "C4", "E4"],
    "Am": ["E3", "A3", "C4", "E4"],
    "B": ["D#3", "F#3", "B3", "D#4"],
    "G": ["D3", "G3", "B3", "D4"],
}
HL_LOOP = ["Em", "Em", "C", "C", "Am", "Am", "B", "B",
           "Em", "Em", "C", "C", "G", "G", "B", "B",
           "Em", "C", "Am", "B"]
HL_CHOIR = (4, 12)


def hollow_light():
    s = LoopSong(98, 4, len(HL_LOOP))
    kicks = []
    for bar in range(4):  # intro: the glass alone, the heartbeat from bar 2
        s.chord("glass", v_glass, HL_CH["Em" if bar < 2 else "C"], bar, gain=0.35 + 0.08 * bar, spread=0.8)
        if bar >= 2:
            s.hit("drums", heartbeat(), bar, 0, 0.7)
            kicks.append(s.pos(bar, 0))
    for base in s.sections():
        for i, name in enumerate(HL_LOOP):
            bar = base + i
            voicing = HL_CH[name]
            s.chord("glass", v_glass, voicing, bar, gain=0.5, spread=0.8)
            s.chord("glass", v_glass, [transpose(voicing[2], 12)], bar, 8, dur_steps=8, gain=0.12)
            s.hit("drums", heartbeat(), bar, 0, 0.8)
            s.hit("drums", heartbeat(), bar, 8, 0.45)
            kicks.append(s.pos(bar, 0))
            s.hit("sub", v_sub(note_freq(transpose(voicing[0], -24)), round(s.bar, 4)), bar, 0, 0.3)
            if i % 4 == 0:
                s.hit("rev", reversed_swell(v_glass, note_freq(transpose(voicing[0], 24)), round(s.bar, 4)), bar, 0,
                      0.18)
            for st in (6, 14):  # a soft shaker-like breath, band noise
                s.hit("hats", hat(), bar, st, 0.03, 0.4)
        for k in HL_CHOIR:  # the Choir motif in one wordless voice (the `choir` stem)
            s.play("choir", v_choir, CHOIR_E4, base + k, gain=0.6, position=-0.15, legato=0.97, vowel="oo",
                   voices=1)
        s.play("horn", v_trumpet, human_motif("E4"), base + 16, gain=0.45, position=0.3)  # the distant trumpet
    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.8),
        "hats": dict(hp=2500, gain=0.3),
        "sub": dict(hp=25, gain=0.45),
        "glass": dict(hp=120, rev=(4.5, 0.5), wid=1.7, seed=8431, gain=0.55),
        "rev": dict(hp=200, rev=(4.5, 0.5), wid=1.7, seed=8432, gain=0.5),
        "horn": dict(hp=300, rev=(5.0, 0.65), seed=8433, gain=0.55),
    }, kicks, depth=0.2)
    choir = s.tl.group("choir")
    choir = reverb(hp(choir, 120), seconds=4.5, mix=0.5, seed=8434)[:, :s.tl.n]
    return s, mix, choir * 0.9, HL_CHOIR


# ================================================================ track 34 a: Fused (150 BPM, E minor)

VL_CH = {
    "Em": (["E3", "G3", "B3", "E4"], "E1"),
    "C": (["E3", "G3", "C4", "E4"], "C2"),
    "Am": (["E3", "A3", "C4", "E4"], "A1"),
    "B": (["D#3", "F#3", "B3", "D#4"], "B1"),
    "F": (["F3", "A3", "C4", "F4"], "F1"),
}
VL_LOOP = (["Em", "Em", "C", "C", "Am", "Am", "B", "B"]
           + ["Em", "Em", "C", "C", "Am", "F", "B", "Em"]
           + ["Em", "Em", "C", "C", "Am", "Am", "B", "B"]
           + ["Em", "Em", "C", "C", "Am", "F", "B", "Em"])
VL_SNARE = [0.35, 0.12, 0.12, 0.22, 0.8, 0.12, 0.25, 0.12, 0.35, 0.12, 0.12, 0.22, 0.8, 0.25, 0.4, 0.6]
VL_STATEMENTS = (0, 8, 16, 24)
DROP = ["B3 - - - - - - - F3 - - - - - - -", "E3 - - - - - - - - - - - - - - -"]  # the Choir motif's tritone drop


def _vorne_bending(s, group, bar, root, gain, pos, octave=0):
    """Vorne's motif (1 b3 5 7) with its last note bending a semitone down and dying."""
    r = transpose(root, 12 * octave)
    for semis, st, ln in ((0, 0, 6), (3, 6, 2), (7, 8, 8)):
        s.hit(group, v_brass(note_freq(transpose(r, semis)), round(s.step * ln * 0.95, 4)), bar, st, gain, pos)
    s.hit(group, v_brass_bend(note_freq(transpose(r, 11)), round(s.step * 14, 4), -1.0, 0.4), bar + 1, 0, gain, pos)


def fused():
    s = LoopSong(150, 4, len(VL_LOOP))
    kicks = []
    for bar in range(4):  # intro: timpani, low brass 8ths, a snare roll into the loop
        s.hit("perc", timpani("E2" if bar < 2 else "B1"), bar, 0, 0.9)
        for st in range(0, 16, 2):
            s.hit("ost", v_brass(note_freq("E2"), round(s.step * 1.4, 4), True), bar, st, 0.3 + 0.04 * bar)
        s.chord("choir", v_choir, VL_CH["Em"][0], bar, gain=0.2 + 0.08 * bar, spread=0.8)
    s.hit("perc", snare_roll(s.bar * 0.95), 3, 0, 0.6)
    for base in s.sections():
        for i, name in enumerate(VL_LOOP):
            bar = base + i
            voicing, root = VL_CH[name]
            if i % 8 == 0:
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            s.hit("perc", timpani(transpose(root, 12) if root.endswith("1") else root), bar, 0, 0.75)
            s.hit("perc", timpani(transpose(root, 19) if root.endswith("1") else transpose(root, 7)), bar, 8, 0.45)
            for beat in range(4):
                s.hit("drums", kick(130), bar, beat * 4, 0.8 if beat % 2 == 0 else 0.5)
                kicks.append(s.pos(bar, beat * 4))
            for st, g in enumerate(VL_SNARE):
                s.hit("snare", snare(True), bar, st, g * 0.65, 0.1)
            for st in range(0, 16, 2):
                s.hit("ost", v_brass(note_freq(transpose(root, 12)), round(s.step * 1.4, 4), True), bar, st,
                      0.34 if st % 4 == 0 else 0.26)
            for st, sm in enumerate(GALLOP):
                s.hit("gallop", v_strings_short(note_freq(transpose(root, 12 + sm)), round(s.step * 0.75, 4)), bar,
                      st, 0.36 if st % 3 == 0 else 0.22, -0.25)
            s.chord("choir", v_choir, voicing, bar, gain=0.4, spread=0.8)
        for k in VL_STATEMENTS:  # Vorne's motif in cold brass, bending into the choir's drop
            _vorne_bending(s, "vorne", base + k, "E3", 0.7, -0.15)
            _vorne_bending(s, "vorne", base + k, "E3", 0.35, 0.15, octave=-1)
            s.play("drop", v_choir, DROP, base + k + 2, gain=0.75, position=0.1, legato=0.97)
            s.play("drop", v_choir, DROP, base + k + 2, gain=0.45, position=-0.1, octave=-1, legato=0.97,
                   vowel="oo")
    mix = mixdown(s, {
        "drums": dict(hp=35, gain=0.7),
        "snare": dict(hp=140, rev=(1.6, 0.2), seed=8441, gain=0.6),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=8442, gain=0.8),
        "ost": dict(hp=60, duck=True, rev=(1.8, 0.2), seed=8443, gain=0.5),
        "gallop": dict(hp=70, rev=(1.8, 0.2), seed=8444, gain=0.45),
        "choir": dict(hp=120, duck=True, rev=(4.0, 0.45), wid=1.6, seed=8445, gain=0.5),
        "vorne": dict(hp=55, rev=(2.8, 0.3), seed=8446, gain=0.8),
        "drop": dict(hp=110, rev=(4.2, 0.5), wid=1.5, seed=8447, gain=0.75),
    }, kicks, depth=0.3)
    return s, mix, None, ()


# ================================================================ track 34 b: Overgrown (147 BPM, E minor)

OG_GATE = [1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1]
OG_BASS = [0, 0, 12, 0, 0, 12, 0, 0, 0, 0, 12, 0, 1, 0, 0, 12]


def overgrown():
    s = LoopSong(147, 4, len(VL_LOOP))
    kicks = []
    n_loop = len(VL_LOOP)
    for bar in range(4):  # intro: the machine riff alone, the kick from bar 2
        for st in range(16):
            if OG_GATE[st] and (bar >= 1 or st % 2 == 0):
                s.hit("bass", v_dbass(note_freq(transpose("E1", 12 + OG_BASS[st])), round(s.step * 0.7, 4)), bar,
                      st, 0.24 + 0.04 * bar)
        if bar >= 2:
            for beat in range(4):
                s.hit("drums", kick(110), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.18)
    for base in s.sections():
        for i, name in enumerate(VL_LOOP):
            bar = base + i
            voicing, root = VL_CH[name]
            detune = -0.33 * i / n_loop  # the machine slowly detuning over the loop (back at the loop start)
            if i % 8 == 0:
                s.hit("perc", crash(), bar, 0, 0.35, 0.3)
            for beat in range(4):
                s.hit("drums", kick(110), bar, beat * 4, 0.95)
                kicks.append(s.pos(bar, beat * 4))
            for st in (4, 12):
                s.hit("drums", snare(True), bar, st, 0.5, 0.05)
                s.hit("metal", sheet_metal(260.0, 2 + (i % 3)), bar, st, 0.45, -0.15)
            for st in range(2, 16, 4):
                s.hit("hats", hat(True), bar, st, 0.14, 0.3)
            for st in range(16):  # the 16th machine riff, distorted, its pitch sagging
                if OG_GATE[st]:
                    f = note_freq(transpose(root, 12 + OG_BASS[st])) * 2 ** (detune / 12)
                    s.hit("bass", v_dbass(round(f, 3), round(s.step * 0.7, 4)), bar, st, 0.36)
                    s.hit("seq", v_seq(round(f * 4, 3), round(s.step * 0.6, 4)), bar, st, 0.14,
                          0.3 if st % 2 else -0.3)
            s.chord("pad", v_strings_long, voicing, bar, gain=0.2, attack=0.3)
            # the choir chanting the Choir motif in 8ths (two bars per statement), Vorne's motif in the last third
            if i < 22 and i % 2 == 0:
                for j, nm in enumerate(["E4", "E4", "D4", "D4", "C4", "C4", "B3", "B3",
                                        "F3", "F3", "F3", "F3", "E3", "E3", "E3", "E3"]):
                    s.hit("choir", v_choir(note_freq(nm), round(s.step * 1.7, 4)), bar + j // 8, 2 * (j % 8),
                          0.42 if j % 2 == 0 else 0.3, -0.2 + 0.4 * ((j // 2) % 2))
        for k in (22, 24, 26, 28, 30):
            s.play("choir", v_choir, vorne_motif("E4"), base + k, gain=0.7, position=-0.1, legato=0.95)
            s.play("choir", v_choir, vorne_motif("E4"), base + k, gain=0.4, position=0.1, octave=-1, legato=0.95,
                   vowel="oo")
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.9),
        "metal": dict(hp=150, rev=(1.6, 0.25), seed=8451, gain=0.55),
        "hats": dict(hp=650, gain=0.36),
        "perc": dict(hp=35, gain=0.5),
        "fx": dict(gain=0.5),
        "bass": dict(hp=38, duck=True, gain=0.6),
        "seq": dict(hp=200, duck=True, rev=(1.5, 0.2), seed=8452, gain=0.5),
        "pad": dict(hp=100, duck=True, rev=(3.5, 0.45), wid=1.6, seed=8453, gain=0.4),
        "choir": dict(hp=120, rev=(3.8, 0.45), wid=1.5, seed=8454, gain=0.65),
    }, kicks, depth=0.42)
    return s, mix, None, ()


# ================================================================ track 27 a: Stand Down, Aegis (75 BPM)

EN_CH = {  # string voicing, bass
    "Dm": (["D3", "F3", "A3", "D4"], "D2"),
    "Bb": (["D3", "F3", "Bb3", "D4"], "Bb1"),
    "Gm": (["D3", "G3", "Bb3", "D4"], "G1"),
    "A": (["E3", "A3", "C#4", "E4"], "A1"),
    "D": (["D3", "F#3", "A3", "D4"], "D2"),
    "G": (["D3", "G3", "B3", "D4"], "G1"),
    "Bm": (["D3", "F#3", "B3", "D4"], "B1"),
    "Gm/D": (["D3", "G3", "Bb3", "D4"], "D2"),
}
EN_LOOP = (["Dm", "Bb", "Gm", "A"]     # 0-3: the strings carry the motif
           + ["Dm", "Bb", "Gm", "A"]   # 4-7: the piano joins
           + ["D", "G", "Bm", "A"]     # 8-11: the full statement in D major
           + ["Gm/D", "D", "D", "D"])  # 12-15: the Choir motif at peace, then the open ending (E-flat under D)
PEACE = ["D5 - - - C5 - - - Bb4 - - - A4 - - -", "Eb5 - - - - - - - D5 - - - - - - -"]  # the Choir motif from D


def _ending_common(s, bars_major, peace_bar, eb_bar):
    for base in s.sections():
        for i, name in enumerate(s.layout):
            bar = base + i
            voicing, root = EN_CH[name]
            g = 0.3 if i < bars_major[0] else 0.45 if i < bars_major[1] else 0.32
            s.chord("strings", v_strings_long, voicing, bar, gain=g, attack=0.6, spread=0.8)
            s.hit("bass", v_strings_long(note_freq(root), round(s.bar, 4), 0.5), bar, 0, 0.3)
            if bars_major[0] <= i < bars_major[1]:
                s.hit("perc", timpani(transpose(root, 12) if root.endswith("1") else root), bar, 0, 0.6)
                s.chord("choirpad", v_choir, voicing, bar, gain=0.35, spread=0.8)
        s.play("peace", v_strings_long, PEACE, base + peace_bar, gain=0.55, position=0.1, legato=0.98)
        s.play("peace", v_strings_long, PEACE, base + peace_bar, gain=0.35, position=-0.1, octave=-1, legato=0.98)
        for k in range(eb_bar, len(s.layout)):  # the low E-flat under D that does not resolve
            s.hit("eb", v_strings_long(note_freq("Eb2"), round(s.bar, 4), 0.8), base + k, 0, 0.32 + 0.04 * (k - eb_bar))
            s.hit("eb", v_sub(note_freq("Eb1"), round(s.bar * 0.95, 4)), base + k, 0, 0.18)


def stand_down():
    s = LoopSong(75, 2, len(EN_LOOP))
    s.layout = EN_LOOP
    s.play("horn", v_horn, MAIN_D4, 0, gain=0.7, position=0.0, octave=-1)  # the solo horn, alone
    for base in s.sections():
        s.play("horn", v_horn, MAIN_D4, base + 0, gain=0.35, position=0.25, octave=-1)
        s.play("lead", v_strings_long, MAIN_D4, base + 0, gain=0.5, position=-0.1)
        s.play("lead", v_strings_long, MAIN_D4, base + 4, gain=0.5, position=-0.1)
        for i in range(4, 8):  # the piano: broken chords in 8ths
            voicing, root = EN_CH[EN_LOOP[i]]
            for st in range(0, 16, 2):
                nm = transpose(voicing[(st // 2) % 4], 12)
                s.hit("piano", v_epiano(note_freq(nm), round(s.step * 3, 4), 0.5), base + i, st, 0.22,
                      -0.3 + 0.2 * ((st // 2) % 4))
        # the full statement in D major: horns, trumpet, strings
        s.play("lead", v_brass, MAIN_D4_MAJ, base + 8, gain=0.6, position=0.15, octave=-1)
        s.play("lead", v_trumpet, MAIN_D4_MAJ, base + 8, gain=0.4, position=-0.15)
        s.play("lead", v_strings_long, MAIN_D4_MAJ, base + 8, gain=0.4, octave=1)
        s.play("lead", v_brass, human_motif("A3", True), base + 10, gain=0.5, position=0.15)
        s.play("lead", v_strings_long, human_motif("A4", True), base + 10, gain=0.4)
        s.hit("perc", crash(), base + 8, 0, 0.3, 0.2)
    _ending_common(s, (8, 12), 12, 14)
    mix = mixdown(s, {
        "horn": dict(hp=60, rev=(4.0, 0.45), seed=8461, gain=0.9),
        "lead": dict(hp=70, rev=(3.5, 0.4), wid=1.3, seed=8462, gain=0.75),
        "strings": dict(hp=60, rev=(4.0, 0.45), wid=1.6, seed=8463, gain=0.55),
        "bass": dict(hp=30, rev=(3.0, 0.3), seed=8464, gain=0.5),
        "piano": dict(hp=90, rev=(3.0, 0.35), wid=1.4, seed=8465, gain=0.6),
        "perc": dict(hp=30, rev=(3.0, 0.35), damp=0.8, seed=8466, gain=0.75),
        "choirpad": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8467, gain=0.5),
        "peace": dict(hp=80, rev=(4.5, 0.5), wid=1.5, seed=8468, gain=0.8),
        "eb": dict(hp=25, rev=(4.0, 0.4), seed=8469, gain=0.6),
    }, [], depth=0.0)
    return s, mix, None, ()


# ================================================================ track 27 b: Home Light (90 BPM, D major)

HM_LOOP = (["D", "Bm", "G", "A"]          # 0-3: the flute's motif over the piano
           + ["D", "Bm", "G", "A"]        # 4-7: the strings in
           + ["D", "G", "Bm", "A"]        # 8-11: the major statement, horns and timpani
           + ["Gm/D", "D", "G", "A"]      # 12-15: the Choir motif at peace in the flute
           + ["D", "G", "D", "D"])        # 16-19: settling, the E-flat under the last bars


def home_light():
    s = LoopSong(90, 2, len(HM_LOOP))
    s.layout = HM_LOOP
    for bar in range(2):  # intro: the piano alone, the flute's first notes
        voicing, _ = EN_CH["D" if bar == 0 else "A"]
        for st in range(0, 16, 4):
            s.hit("piano", v_epiano(note_freq(transpose(voicing[(st // 4) % 4], 12)), round(s.step * 5, 4), 0.45),
                  bar, st, 0.3, -0.2 + 0.13 * (st // 4))
    s.play("flute", v_flute, MAIN_D4_MAJ, 0, gain=0.45, position=0.1, octave=1)
    for base in s.sections():
        for i, name in enumerate(HM_LOOP):  # the soft electric piano throughout, quarter-note broken chords
            voicing, root = EN_CH[name]
            for st in range(0, 16, 4):
                s.hit("piano", v_epiano(note_freq(transpose(voicing[(st // 4) % 4], 12)), round(s.step * 5, 4), 0.45),
                      base + i, st, 0.26, -0.2 + 0.13 * (st // 4))
        s.play("flute", v_flute, MAIN_D4_MAJ, base + 0, gain=0.55, position=0.1, octave=1)
        s.play("flute", v_flute, human_motif("A4", True), base + 2, gain=0.5, position=0.1)
        s.play("flute", v_flute, MAIN_D4_MAJ, base + 4, gain=0.5, position=0.1, octave=1)
        s.play("lead", v_strings_long, MAIN_D4_MAJ, base + 4, gain=0.35, position=-0.1)
        s.play("lead", v_brass, MAIN_D4_MAJ, base + 8, gain=0.55, position=0.15, octave=-1)
        s.play("lead", v_strings_long, MAIN_D4_MAJ, base + 8, gain=0.45, octave=1)
        s.play("flute", v_flute, MAIN_D4_MAJ, base + 8, gain=0.4, octave=1)
        s.play("lead", v_brass, human_motif("A3", True), base + 10, gain=0.5, position=0.15)
        s.play("flute", v_flute, PEACE, base + 12, gain=0.5, position=0.1)
        s.play("flute", v_flute, MAIN_D4_MAJ, base + 16, gain=0.35, position=0.1, octave=1)
    _ending_common(s, (8, 12), 12, 18)
    mix = mixdown(s, {
        "piano": dict(hp=90, rev=(2.8, 0.3), wid=1.4, seed=8471, gain=0.65),
        "flute": dict(hp=200, rev=(3.2, 0.35), seed=8472, gain=0.8),
        "lead": dict(hp=70, rev=(3.5, 0.4), wid=1.3, seed=8473, gain=0.7),
        "strings": dict(hp=60, rev=(3.8, 0.4), wid=1.6, seed=8474, gain=0.5),
        "bass": dict(hp=30, rev=(3.0, 0.3), seed=8475, gain=0.45),
        "perc": dict(hp=30, rev=(3.0, 0.35), damp=0.8, seed=8476, gain=0.7),
        "choirpad": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8477, gain=0.45),
        "peace": dict(hp=80, rev=(4.5, 0.5), wid=1.5, seed=8478, gain=0.6),
        "eb": dict(hp=25, rev=(4.0, 0.4), seed=8479, gain=0.6),
    }, [], depth=0.0)
    return s, mix, None, ()


# ================================================================ track 28 a: Coalition Forever (140 BPM, D minor)

CF_LOOP = (["Dm", "Bb", "F", "C", "Dm", "Bb", "Gm", "A"]    # A: groove and arp, the lead's motif
           + ["F", "C", "Dm", "Bb", "F", "C", "Bb", "A"]    # B: the title's B melody
           + ["Dm", "Bb", "F", "A", "Dm", "Bb", "C", "A"]   # C: breakdown on the slow C melody
           + ["F", "C", "Dm", "Bb", "F", "C", "Bb", "A"])   # D: all in, the B melody an octave up
ARP16 = [0, 1, 2, 3, 2, 1, 0, 1, 0, 1, 2, 3, 4, 3, 2, 1]


def coalition_forever():
    s = LoopSong(140, 4, len(CF_LOOP))
    kicks = []
    for bar in range(4):  # intro: the arp filtered up, the kick from bar 2
        _, _, tones = T_CH[["Dm", "Dm", "Bb", "A"][bar]]
        ext = tones + [transpose(tones[0], 12), transpose(tones[1], 12)]
        for st in range(16):
            s.hit("arp", v_arp(note_freq(ext[ARP16[st]]), round(s.step * 0.7, 4), 700.0 + 300 * bar), bar, st, 0.3,
                  0.3 if st % 2 else -0.3)
        if bar >= 2:
            for beat in range(4):
                s.hit("drums", kick(150), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.25)
    for base in s.sections():
        for i, name in enumerate(CF_LOOP):
            bar = base + i
            pad, root, tones = T_CH[name]
            sec = _sec(i, (8, 16, 24))
            if i % 8 == 0:
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            if sec != "C":
                for beat in range(4):
                    s.hit("drums", kick(150), bar, beat * 4, 0.95)
                    kicks.append(s.pos(bar, beat * 4))
                for st in (4, 12):
                    s.hit("drums", clap(), bar, st, 0.5)
                for st in range(2, 16, 4):
                    s.hit("hats", hat(True), bar, st, 0.18, 0.3)
                for st in range(16):
                    s.hit("hats", hat(), bar, st, 0.07, -0.3)
                for st in range(2, 16, 4):
                    s.hit("bass", v_bass(note_freq(transpose(root, 12)), round(s.step * 1.5, 4)), bar, st, 0.45)
            elif i % 8 == 7:
                for st in range(16):
                    s.hit("drums", snare(True), bar, st, 0.12 + 0.03 * st, 0.05)
                s.hit("fx", riser(s.bar), bar, 0, 0.25)
            ext = tones + [transpose(tones[0], 12), transpose(tones[1], 12)]
            for st in range(16):
                s.hit("arp", v_arp(note_freq(ext[ARP16[st]]), round(s.step * 0.7, 4),
                                   1600.0 if sec != "C" else 1000.0), bar, st, 0.3, 0.3 if st % 2 else -0.3)
            s.chord("pad", v_pad, pad, bar, gain=0.25 if sec != "C" else 0.4, spread=0.8)
        for k in (0, 4):
            s.play("lead", v_lead, MAIN_D4, base + k, gain=0.45, position=0.1)
        s.play("lead", v_lead, T_MEL_B, base + 8, gain=0.42, position=0.1, octave=-1)
        s.play("lead", v_lead, T_MEL_C * 2, base + 16, gain=0.4, position=0.0)
        s.play("lead", v_lead, T_MEL_B, base + 24, gain=0.42, position=0.1)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "hats": dict(hp=700, gain=0.38),
        "perc": dict(hp=40, gain=0.5),
        "fx": dict(gain=0.5),
        "bass": dict(hp=35, duck=True, gain=0.55),
        "arp": dict(hp=180, duck=True, dly=(3, 0.3, 0.2, 4000), seed=8481, gain=0.4),
        "pad": dict(hp=100, duck=True, rev=(3.0, 0.4), wid=1.6, seed=8482, gain=0.45),
        "lead": dict(hp=200, dly=(3, 0.3, 0.2, 4500), rev=(2.2, 0.25), wid=1.3, seed=8483, gain=0.55),
    }, kicks, depth=0.45)
    return s, mix, None, ()


# ================================================================ track 28 b: Seven Acts (150 BPM, D minor)

SA_LOOP = ["Dm", "Bb", "Gm", "A"] * 8
SA_COLOURS = ["earth", "mars", "europa", "belt", "jupiter", "choir", "title", "title"]


def seven_acts():
    s = LoopSong(150, 4, len(SA_LOOP))
    kicks = []
    for bar in range(4):  # intro: timpani and strings swelling, the brass's first call
        pad, root, _ = T_CH[FS_INTRO[bar]]
        s.chord("strings", v_strings_long, pad, bar, gain=0.25 + 0.07 * bar, spread=0.7)
        s.hit("perc", timpani("D2" if bar < 2 else "A1"), bar, 0, 0.8)
    s.play("brass", v_brass, MAIN_D4, 0, gain=0.5)
    s.hit("perc", snare_roll(s.bar * 0.95), 3, 0, 0.5)
    for base in s.sections():
        for i, name in enumerate(SA_LOOP):
            bar = base + i
            pad, root, tones = T_CH[name]
            col = SA_COLOURS[i // 4]
            if i % 4 == 0:
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            # the breakbeat under every colour
            for st, g in FS_KICK:
                s.hit("drums", kick(140), bar, st, g)
                kicks.append(s.pos(bar, st))
            for st, g in FS_SNARE:
                s.hit("drums", snare(True), bar, st, g * 0.85, 0.05)
            for st in range(0, 16, 2):
                s.hit("hats", hat(st % 8 == 6), bar, st, 0.12, 0.3)
            s.chord("strings", v_strings_long, pad, bar, gain=0.2, spread=0.7)
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 7.5, 4)), bar, 0, 0.32)
            if col == "earth":
                for st in (0, 3, 6, 10):
                    s.chord("stabs", v_brass, tones, bar, st, dur_steps=1, gain=0.3, stab=True)
            elif col == "mars":
                for st, smp in ((0, doum()), (6, tek()), (8, doum()), (11, tek()), (14, tek())):
                    s.hit("perc", smp, bar, st, 0.4, 0.2)
            elif col == "europa":
                if i % 2 == 0:
                    s.hit("fx", sonar(), bar, 0, 0.18, -0.3)
                s.chord("pad", v_pad, pad, bar, gain=0.3, spread=0.8, bright=1100.0)
            elif col == "belt":
                for st in (4, 12):
                    s.hit("metal", anvil(700.0 + 40 * (i % 3), i), bar, st, 0.3, 0.25)
                for st in (0, 3, 8, 11):
                    s.hit("bass", v_dbass(note_freq(transpose(root, 12)), round(s.step * 2, 4)), bar, st, 0.3)
            elif col == "jupiter":
                s.chord("trem", v_tremolo, pad, bar, gain=0.4, spread=0.8)
                if i % 4 == 0:
                    s.hit("fx", thunder(840 + i), bar, 2, 0.35, 0.3)
            elif col == "choir":
                s.chord("choirpad", v_choir, pad, bar, gain=0.45, spread=0.8)
            else:
                s.hit("perc", timpani(transpose(root, 12) if root.endswith("1") else root), bar, 0, 0.6)
                for st in range(0, 16, 2):
                    s.hit("ost", v_brass(note_freq(transpose(root, 12)), round(s.step * 1.4, 4), True), bar, st, 0.25)
        # the main motif in each act's lead voice, then the title's brass statement
        s.play("lead", v_brass, MAIN_D4, base + 0, gain=0.55)
        s.play("lead", v_reed, MAIN_D4, base + 4, gain=0.4)
        s.play("lead", v_flute, human_motif("D5"), base + 8, gain=0.45)
        s.play("lead", v_dbass, human_motif("D3"), base + 12, gain=0.3)
        s.play("lead", v_seq, human_motif("D5"), base + 12, gain=0.35)
        s.play("lead", v_tremolo, MAIN_D4, base + 16, gain=0.5)
        s.play("lead", v_choir, CHOIR_E4, base + 20, gain=0.55, legato=0.97)
        s.play("lead", v_choir, MAIN_D4, base + 22, gain=0.45, legato=0.97)
        for k in (24, 28):
            s.play("lead", v_brass, MAIN_D4, base + k, gain=0.6, position=0.15)
            s.play("lead", v_trumpet, human_motif("D5"), base + k + 2, gain=0.4, position=-0.15)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.8),
        "hats": dict(hp=700, gain=0.33),
        "perc": dict(hp=40, rev=(2.4, 0.25), seed=8491, gain=0.6),
        "metal": dict(hp=200, rev=(1.8, 0.3), seed=8492, gain=0.4),
        "fx": dict(hp=40, rev=(3.0, 0.4), seed=8493, gain=0.6),
        "bass": dict(hp=30, duck=True, gain=0.5),
        "stabs": dict(hp=150, duck=True, rev=(1.6, 0.25), wid=1.4, seed=8494, gain=0.45),
        "strings": dict(hp=80, duck=True, rev=(3.0, 0.4), wid=1.5, seed=8495, gain=0.4),
        "pad": dict(hp=100, rev=(3.5, 0.45), wid=1.6, seed=8496, gain=0.4),
        "trem": dict(hp=90, rev=(3.5, 0.4), wid=1.5, seed=8497, gain=0.45),
        "choirpad": dict(hp=120, rev=(4.0, 0.45), wid=1.6, seed=8498, gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.8, 0.2), seed=8499, gain=0.45),
        "brass": dict(hp=60, rev=(2.8, 0.3), seed=8500, gain=0.75),
        "lead": dict(hp=80, rev=(2.6, 0.3), seed=8501, gain=0.75),
    }, kicks, depth=0.35)
    return s, mix, None, ()


# ================================================================ the memory filter (Level 49), on track 19

def _speed(x, factor, wow=0.0, wow_hz=0.5):
    """x (stereo) played at `factor` speed (pitch with it), with an optional slow tape wow (+-wow)."""
    n = int(x.shape[1] / factor)
    t = np.arange(n) / SR
    rate = factor * (1 + wow * np.sin(2 * np.pi * wow_hz * t))
    pos = np.concatenate([[0.0], np.cumsum(rate[:-1])])
    pos = np.minimum(pos, x.shape[1] - 1)
    return np.array([np.interp(pos, np.arange(x.shape[1]), ch) for ch in x])


def memory(variant):
    t19 = decode(ASSETS / "iron-sovereign.ogg")
    a = 306000  # track 19's LOOPSTART
    head_n, xf_n, total = 6 * SR, 2 * SR, 40 * SR
    if variant == "a":
        speed, wow, cut, poles, rev, wet, width = 0.94, 0.0, 2500.0, 1, 3.5, 0.40, 0.6
    else:
        speed, wow, cut, poles, rev, wet, width = 0.90, 0.003, 1400.0, 2, 5.0, 0.55, 0.3
    src = t19[:, a:a + int(total * 1.2)]
    mem = _speed(src, speed, wow)
    for _ in range(poles):
        mem = lp(mem, cut)
    mem = reverb(mem, seconds=rev, mix=wet, seed=8510)[:, :mem.shape[1]]
    mid, side = (mem[0] + mem[1]) / 2, (mem[0] - mem[1]) / 2
    mem = np.array([mid + side * width, mid - side * width])
    orig = t19[:, a:a + head_n + xf_n].copy()
    orig[:, head_n:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, xf_n))
    out = np.zeros((2, total))
    out[:, :orig.shape[1]] += orig
    # the memory copy starts where the built track would be at the crossfade (its own slower clock), at the level
    # the game gives it (-4 dB) against the built track before the file is levelled
    k0 = int(head_n / speed)
    seg = mem[:, k0:k0 + total - head_n] * 10 ** (-4 / 20)
    seg[:, :xf_n] *= 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, xf_n))
    out[:, head_n:head_n + seg.shape[1]] += seg
    return out


# ================================================================ main

SONGS = {"a16-a": ("act7-a-theme-r84-a", far_shore), "a16-b": ("act7-a-theme-r84-b", event_horizon),
         "b17-a": ("act7-b-theme-r84-a", inner_sphere), "b17-b": ("act7-b-theme-r84-b", hollow_light),
         "vls-a": ("vorne-last-stand-r84-a", fused), "vls-b": ("vorne-last-stand-r84-b", overgrown),
         "end-a": ("ending-r84-a", stand_down), "end-b": ("ending-r84-b", home_light),
         "cred-a": ("credits-r84-a", coalition_forever), "cred-b": ("credits-r84-b", seven_acts)}
MEMORY = {"mem-a": ("memory-filter-r84-a", "a"), "mem-b": ("memory-filter-r84-b", "b")}


def _report(path, extra=""):
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s{extra}  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS"
          f"  true peak {tp:.1f} dBTP", flush=True)
    return dec


def render_song(key, out):
    name, fn = SONGS[key]
    s, rest, stem, stem_bars = fn()
    mix = rest + stem if stem is not None else rest
    if stem is not None:
        base = s.sections()[1]
        gaps = []
        for bar in stem_bars:
            a, b = int(s.pos(base + bar) * SR), int(s.pos(base + bar + 2) * SR)
            gaps.append(db(np.sqrt(np.mean(mix[:, a:b] ** 2))) - db(np.sqrt(np.mean(stem[:, a:b] ** 2))))
        print(f"{name}: the choir stem " + ", ".join(f"{g:.1f}" for g in gaps) + " dB under the mix (bars "
              + ", ".join(str(b) for b in stem_bars) + ")", flush=True)
    mix = mix - mix.mean(axis=1, keepdims=True)
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    mix = master(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, -2.0)[:, :mix.shape[1]]
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))  # fade-in only: the end loops
    path = write_ogg(out / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    _report(path, f"  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s  (samples {loop_start}+{loop_len})"
                  f"  seam {seam_ratio(dec, loop_start, loop_len):.2f}")


def render_memory(key, out):
    name, variant = MEMORY[key]
    mix = master(memory(variant), -14.0, -2.0)
    mix = fade(mix, 0.003, 2.0)
    _report(write_ogg(out / f"{name}.ogg", mix))


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or [*SONGS, *MEMORY]:
        if key in SONGS:
            render_song(key, out)
        else:
            render_memory(key, out)


if __name__ == "__main__":
    main(sys.argv[1:])
