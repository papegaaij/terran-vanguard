#!/usr/bin/env python3
"""Concept round 72 (M10 "Act 6 - Jovian Storm" design phase, step AUD-R): the Act 6 music sketches of the music
README's *Act 6 music (draft)*: two sketches for track 15, the Act 6 B theme ("heavy, stormy, choir and distorted
bass"), two for the duel theme (new track 32, Level 40's Honour Guard), the Ascendant's hand-off from track 19 to
track 18 two ways, and two for the escape sting (new track 33, Level 42's `ESCAPED`).

The themes use the round-02/03/08/36/48/60 format: intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH Vorbis
comments (samples), a sample-exact loop (each tempo gives a whole-sample 16th step), -14 LUFS with a -2 dBFS
sample-peak ceiling. The stings are one-shots like the round-08/48/60 stings (-14 LUFS, -2 dBFS ceiling, a short fade at
the end). The hand-offs are cut from the game's own files (assets/music/iron-sovereign.ogg, track 19, and
assets/music/choir-descends.ogg, track 18; read only) and levelled to -14 LUFS like them.

Outputs (design/audio/music/concept/, or --out DIR):
  act6-b-theme-r72-a.ogg  "Storm Wall"  135 BPM, C minor (Vorne's motif on C E-flat G B)
      An orchestral-metal hybrid: a drop-tuned distorted bass riff in half-time (a chug on the root with a flat-2 bend,
      the sub an octave down), war drums (a deep kick, taiko, a huge half-time snare, low tom fills), tremolo storm
      strings, a wordless choir in long minor chords over Cm - Ab - Fm - G; the storm's wind as band noise. Vorne's
      motif IN THE OPEN: cold low brass and organ every 8 bars (loop bars 0, 8 and 20, and once in the intro). A
      4-bar breakdown (loop bars 16-19): the choir alone over low strings and the wind.
  act6-b-theme-r72-b.ogg  "Red Spot"  150 BPM, E minor (track 18's key, the Choir's)
      Darker and driving: a distorted 16th bass, an industrial kit (a four-on-the-floor kick, a snare and struck sheet
      metal on the backbeat, toms), the choir chanting the Choir motif's tritone drop (8 - b7 - b6 - 5, the drop to b2,
      1: E D C B, F E) as a riff from the B section on. Vorne's motif ONLY inside the bass's machine arpeggio (E G B
      D-sharp in 16ths, two detuned saws through a soft low-pass; no brass): the Vrell have the upper hand. A drumless
      C section (the choir's drop slowed, the arpeggio, the toms).
  duel-theme-r72-a.ogg  "Honour Guard"  150 BPM, A minor
      Martial and orchestral (track 19's family): snare rudiments, timpani, low-brass 8ths, galloping strings over
      Am - F - Dm - E. Vorne's motif (A C E G-sharp) in gold brass, answered by the main motif's horn call (A E A B C)
      in the horns: call and response like a joust (loop bars 0-7); a three-voice brass fugato on Vorne's motif (the
      three aces, bars 8-15); both motifs at once and in canon (bars 16-23); the tutti (bars 24-31).
  duel-theme-r72-b.ogg  "Three Aces"  175 BPM, A minor
      A drum-and-bass breakbeat (kick, snare and ghost notes, 16th hats), a reese sub bass, synth-brass stabs and a
      distorted lead: Vorne's motif as a fast sequencer arpeggio (A C E G-sharp in 16ths), the main motif soaring in the
      lead; leaner and more arcade.
  ascendant-handoff-r72-a.ogg  (about 22 s) the hand-off as a CROSSFADE: 10 s of track 19 (the built file, from loop
      bar 8), then track 19 out over 2 s while track 18 starts from its beginning (its intro: the Choir motif alone
      over a drone), 10 s of it. The README's recommended hand-off: no new render.
  ascendant-handoff-r72-b.ogg  (about 24 s) the hand-off as a 4 s BRIDGE: the same 10 s of track 19, then its next
      4 s slowing like a tape stop (the speed gliding 1.0 -> 0.89, two semitones down: F-sharp minor onto track 18's
      E) and darkening (a low-pass closing 8 kHz -> 600 Hz), track 18's intro rising under its last 2 s, then 10 s of
      track 18.
  escape-sting-r72-a.ogg  "Collapse" (about 6.5 s) brass and choir: Vorne's motif in E (E G B D-sharp) stated once
      in low brass and organ, its last note bending down a semitone and dying; the choir takes over with the Choir
      motif's descent (E D C B) and the tritone drop to F, ending on a held low E (strings, organ, a sub) with the F
      over it, unresolved.
  escape-sting-r72-b.ogg  "Dying Engine" (about 6.5 s) the motif as a machine arpeggio (E G B D-sharp in 16ths)
      that slows and pitches down like a dying engine (a tape stop, the speed gliding 1.0 -> 0.45 over 2.4 s), into
      the choir's drop (C B, F E) and the same held low E with the F over it.

Vorne's motif (1 - b3 - 5 - 7) is in its own group ("motif") in both track-15 sketches so that a production version can
carry it as the motif stem (track 12's method): a's brass and organ statements, b's arpeggio (lifted out of the full
mix). Its level against the whole mix over its 2-bar statements is printed: a about 6 dB under ("in the open"), b
inside the groove. The duel theme is one full mix (no stems).

No bubbly or popping sounds: no bubble layer, no resonant short plucks, struck metal with a 0.5 ms attack and an
inharmonic ring, thunder and wind only as band noise, every pitched voice with a soft attack except the basses.

Usage: python3 tools/concept/audio/music_r72.py [b15-a|b15-b|duel-a|duel-b|handoff-a|handoff-b|sting-a|sting-b ...]
       [--out DIR]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks and the loop seam.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (Song, clap, crash, hat, hp, kick, ns, riser, snare, taiko, timpani, tom,  # noqa: E402
                   transpose, v_brass, v_lead, v_strings_long, v_strings_short)
from music_r02 import LoopSong, add_tail, seam_ratio, v_choir, v_dbass, v_reese, v_sub  # noqa: E402
from music_r03 import (GALLOP, choir_motif, human_motif, mixdown, thunder, v_organ, v_tremolo,  # noqa: E402
                       v_trumpet, vorne_motif)
from music_r48 import current, deep_kick, timp_roll  # noqa: E402
from music_r60 import _bus, sheet_metal, snare_roll, v_seq  # noqa: E402
from synth import (SR, adsr, db, decode, exp_decay, fade, lufs, master, pulse, ramp, reverb,  # noqa: E402
                   saturate, supersaw, svf, write_ogg, note_freq)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
ASSETS = ROOT / "assets" / "music"


# ================================================================ shared voices

@lru_cache(None)
def v_dlead(freq, dur):
    """The distorted lead: the round-01 supersaw lead driven hard into the saturator."""
    return saturate(v_lead(freq, dur) * 2.2, 2.4) * 0.6


@lru_cache(None)
def v_brass_bend(freq, dur, semis=-1.0, hold=0.45):
    """v_brass with its pitch bending `semis` semitones down after `hold` of the note, the level dying with it."""
    n = ns(dur + 0.3)
    t = np.arange(n) / SR
    u = np.clip((t - hold * dur) / max(1e-3, (1 - hold) * dur), 0, 1)
    vib = 1 + 0.005 * np.sin(2 * np.pi * 5.0 * t) * np.clip((t - 0.25) / 0.3, 0, 1)
    f = freq * vib * ramp(n, 0.94, 1.0, tau=0.025) * 2 ** (semis * u ** 1.5 / 12)
    x = supersaw(f, n, voices=3, detune=0.004, rng=np.random.default_rng(15))
    x += pulse(f, n, 0.42) * 0.4
    cut = (500 + 2600 * (1 - exp_decay(n, 0.02))) * (1 - 0.6 * u)
    x = svf(x, cut, q=1.1)
    env = adsr(n, 0.03, 0.12, 0.8, 0.25, gate=dur) * (1 - 0.7 * u ** 2)
    return saturate(x * env * 1.3, 1.3)


def glide_speed(x, s0, s1, n=None):
    """x (stereo) played at a speed gliding linearly s0 -> s1 over its length (a tape stop when falling)."""
    n = n or int(x.shape[1] / ((s0 + s1) / 2))
    pos = np.concatenate([[0.0], np.cumsum(np.linspace(s0, s1, n - 1))])
    pos = np.minimum(pos, x.shape[1] - 1)
    return np.array([np.interp(pos, np.arange(x.shape[1]), ch) for ch in x])


def sweep_lp(x, f0, f1):
    """A low-pass closing f0 -> f1 Hz over x (two passes, block-wise so the cutoff can move)."""
    n = x.shape[1]
    cut = np.geomspace(f0, f1, n)
    return np.array([svf(svf(ch, cut, q=0.7), cut, q=0.7) for ch in x])


# ================================================================ track 15 a: Storm Wall (135 BPM, C minor)

SW_CH = {  # choir/string voicing, riff root (octave 2), sub root (octave 1)
    "Cm": (["C3", "G3", "C4", "Eb4"], "C2", "C1"),
    "Ab": (["C3", "Ab3", "C4", "Eb4"], "Ab1", "Ab1"),
    "Fm": (["C3", "F3", "Ab3", "C4"], "F1", "F1"),
    "G": (["D3", "G3", "B3", "D4"], "G1", "G1"),
}
SW_INTRO = ["Cm", "Cm", "Ab", "G"]
SW_LOOP = (["Cm", "Cm", "Ab", "Ab", "Fm", "Fm", "G", "G"]    # A: the riff and the war drums, the motif at bar 0
           + ["Cm", "Cm", "Ab", "Ab", "Fm", "Fm", "G", "G"]  # B: the choir in, the motif at bar 8
           + ["Cm", "Ab", "Fm", "G"]                         # C: the breakdown, the choir alone over low strings
           + ["Cm", "Cm", "Ab", "Ab", "Fm", "Fm", "G", "G"])  # D: all in, the motif at bar 20
# the half-time riff: (step, semitones over the root, length in steps); the flat-2 is the drop-tuned bend
SW_RIFF = [(0, 0, 3), (3, 0, 1), (4, 0, 1), (6, 1, 2), (8, 0, 2), (11, 0, 1), (12, 12, 2), (14, 7, 2)]
SW_MOTIF_BARS = (0, 8, 20)


def storm_wall():
    s = LoopSong(135, 4, len(SW_LOOP))
    kicks = []
    # intro: storm strings swelling, far thunder, the motif's first statement; the riff's chug from bar 2
    for bar, name in enumerate(SW_INTRO):
        voicing, root, sub = SW_CH[name]
        s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.3 + 0.08 * bar, spread=0.7)
        if bar >= 2:
            s.hit("drums", deep_kick(), bar, 0, 0.9)
            kicks.append(s.pos(bar, 0))
            s.hit("perc", taiko(), bar, 8, 0.6)
            for st, semis, ln in SW_RIFF[:3]:
                s.hit("bass", v_dbass(note_freq(transpose(root, semis)), round(s.step * ln * 0.85, 4)), bar, st,
                      0.3)
    s.hit("fx", thunder(721), 0, 0, 0.55, -0.3)
    s.hit("fx", thunder(722), 2, 6, 0.45, 0.35)
    s.play("motif", v_brass, vorne_motif("C3"), 0, gain=0.55, position=-0.1)
    s.play("motif", v_organ, vorne_motif("C3"), 0, gain=0.4, octave=-1)
    for st in range(8, 16):
        s.hit("perc", tom(150 - 8 * (st - 8)), 3, st, 0.3 + 0.06 * (st - 8), (st - 11.5) / 5)
    s.hit("fx", riser(s.bar), 3, 0, 0.2)

    for base in s.sections():
        for i, name in enumerate(SW_LOOP):
            bar = base + i
            voicing, root, sub = SW_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 20 else "D"
            if i in (0, 8, 20):
                s.hit("perc", crash(), bar, 0, 0.45, 0.3)
                s.hit("perc", timpani("C2" if name == "Cm" else "G1"), bar, 0, 0.8)
            if sec != "C":
                # war drums: the deep kick on 1 and the and-of-2, a huge half-time snare on 3, taiko under it
                for st, g in ((0, 1.0), (6, 0.6), (10, 0.75)):
                    s.hit("drums", deep_kick(), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                s.hit("snare", snare(), bar, 8, 0.75, 0.05)
                s.hit("snare", clap(), bar, 8, 0.35)
                for st in (0, 8, 11):
                    s.hit("perc", taiko(), bar, st, 0.55 if st != 11 else 0.35, -0.2 if st else 0.2)
                if sec != "A":
                    for st in range(0, 16, 2):
                        s.hit("hats", hat(st % 4 == 2), bar, st, 0.14, 0.3)
                if i % 4 == 3:  # low tom fills into the next phrase
                    for st, p in ((12, 140), (13, 120), (14, 100), (15, 85)):
                        s.hit("perc", tom(p), bar, st, 0.5, (st - 13.5) / 3)
                if sec == "D" and i % 2 == 1:  # the double kick under the climax
                    for st in range(12, 16):
                        s.hit("drums", deep_kick(), bar, st, 0.45)
                        kicks.append(s.pos(bar, st))
                # the drop-tuned riff, half-time, and its sub an octave down
                for st, semis, ln in SW_RIFF:
                    s.hit("bass", v_dbass(note_freq(transpose(root, semis)), round(s.step * ln * 0.85, 4)), bar, st,
                          0.38)
                s.hit("sub", v_sub(note_freq(sub), round(s.step * 15, 4)), bar, 0, 0.45)
                s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.32, spread=0.7)
                if sec != "A":
                    s.chord("choir", v_choir, voicing, bar, dur_steps=16, gain=0.42, spread=0.8)
            else:
                # the breakdown: the choir alone over low strings (and the wind), a far thunder roll
                s.chord("choir", v_choir, voicing, bar, dur_steps=16, gain=0.6, spread=0.8)
                s.chord("low", v_strings_long, [transpose(voicing[0], -12), voicing[0]], bar, dur_steps=16,
                        gain=0.5, attack=0.4)
                if i == 16:
                    s.hit("fx", thunder(730 + i), bar, 2, 0.5, 0.4)
                if i == 19:
                    s.hit("fx", riser(s.bar), bar, 0, 0.25)
                    for st in range(8, 16):
                        s.hit("perc", taiko(), bar, st, 0.25 + 0.06 * (st - 8))
        # Vorne's motif in the open: cold low brass and organ every 8 bars
        for bar in SW_MOTIF_BARS:
            s.play("motif", v_brass, vorne_motif("C3"), base + bar, gain=0.6, position=-0.1)
            s.play("motif", v_brass, vorne_motif("C3"), base + bar, gain=0.35, position=0.15, octave=-1)
            s.play("motif", v_organ, vorne_motif("C3"), base + bar, gain=0.4, octave=-1)

    storm = current(s, 700.0, 0.8, [(-4, 1.0), (0, 0.45), (16, 0.45), (16.01, 1.1), (20, 1.1), (20.01, 0.45),
                                    (len(SW_LOOP), 0.45)], 7201)
    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.9),
        "snare": dict(hp=110, rev=(3.2, 0.4), damp=0.7, seed=7202, gain=0.7),
        "perc": dict(hp=35, rev=(2.8, 0.3), damp=0.8, seed=7203, gain=0.75),
        "hats": dict(hp=700, gain=0.35),
        "fx": dict(hp=40, rev=(3.5, 0.35), seed=7204, gain=0.75),
        "bass": dict(hp=38, duck=True, gain=0.55),
        "sub": dict(hp=25, duck=True, gain=0.5),
        "trem": dict(hp=90, rev=(3.5, 0.4), wid=1.5, seed=7205, gain=0.5),
        "choir": dict(hp=140, rev=(4.0, 0.45), wid=1.6, seed=7206, gain=0.6),
        "low": dict(hp=40, rev=(3.5, 0.4), wid=1.4, seed=7207, gain=0.55),
    }, kicks, depth=0.4)
    mix = mix + storm * 0.05
    motif = s.tl.group("motif")
    motif = reverb(hp(motif, 50), seconds=2.8, mix=0.3, seed=7208)[:, :s.tl.n]
    return s, mix, motif * 0.5, SW_MOTIF_BARS


# ================================================================ track 15 b: Red Spot (150 BPM, E minor)

RS_CH = {  # pad voicing, bass root
    "Em": (["E3", "G3", "B3", "E4"], "E1"),
    "C": (["E3", "G3", "C4", "E4"], "C2"),
    "Am": (["E3", "A3", "C4", "E4"], "A1"),
    "B": (["D#3", "F#3", "B3", "D#4"], "B1"),
    "F": (["F3", "A3", "C4", "F4"], "F1"),  # the flat-2, the tritone drop's chord
}
RS_INTRO = ["Em", "Em", "C", "B"]
RS_LOOP = (["Em", "Em", "C", "C", "Am", "Am", "B", "B"]    # A: the bass and the kit
           + ["Em", "Em", "C", "C", "Am", "Am", "F", "Em"]  # B: the choir's drop as a riff, the arpeggio in
           + ["Em", "Em", "C", "C", "Am", "Am", "F", "Em"]  # C: drums out, the choir slowed, the arpeggio, toms
           + ["Em", "Em", "C", "C", "Am", "Am", "F", "Em"])  # D: all in
RS_GATE = [1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1]
RS_BASS = [0, 0, 12, 0, 0, 12, 0, 0, 0, 0, 12, 0, 1, 0, 0, 12]  # semitones over the root (the 1 is the flat-2 rub)
RS_MOTIF_BARS = (8, 16, 24)


def red_spot():
    s = LoopSong(150, 4, len(RS_LOOP))
    kicks = []
    for bar, name in enumerate(RS_INTRO):  # intro: the bass alone, then the kick; the choir's drop once, far
        pad, root = RS_CH[name]
        for st in range(16):
            if RS_GATE[st] and (bar >= 1 or st % 2 == 0):
                s.hit("bass", v_dbass(note_freq(transpose(root, 12 + RS_BASS[st])), round(s.step * 0.7, 4)), bar,
                      st, 0.22 + 0.04 * bar)
        if bar >= 2:
            for beat in range(4):
                s.hit("drums", kick(110), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    s.play("choir", v_choir, choir_motif("E4"), 0, gain=0.35, legato=0.85)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.18)

    seq_notes = [transpose("E3", k) for k in (0, 3, 7, 11)]  # E G B D-sharp
    for base in s.sections():
        for i, name in enumerate(RS_LOOP):
            bar = base + i
            pad, root = RS_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 24 else "D"
            if i in (0, 8, 24):
                s.hit("perc", crash(), bar, 0, 0.35, 0.3)
            if sec != "C":
                for beat in range(4):
                    s.hit("drums", kick(110), bar, beat * 4, 0.95)
                    kicks.append(s.pos(bar, beat * 4))
                for st in (4, 12):
                    s.hit("drums", snare(True), bar, st, 0.5, 0.05)
                    s.hit("metal", sheet_metal(280.0, 2 + (i % 2)), bar, st, 0.45, -0.15)
                for st in range(2, 16, 4):
                    s.hit("hats", hat(True), bar, st, 0.16, 0.3)
                if sec == "D":
                    for st in range(1, 16, 2):
                        s.hit("hats", hat(), bar, st, 0.08, -0.3)
                if i % 4 == 3:
                    for st, p in ((12, 170), (13, 150), (14, 125), (15, 100)):
                        s.hit("perc", tom(p), bar, st, 0.5, (st - 13.5) / 3)
                # the distorted 16th bass
                for st in range(16):
                    if RS_GATE[st]:
                        s.hit("bass", v_dbass(note_freq(transpose(root, 12 + RS_BASS[st])), round(s.step * 0.7, 4)),
                              bar, st, 0.36)
            else:
                # drumless: the toms on the half bars, the sub held, the choir's drop slowed to whole bars
                for st, p in ((0, 110), (8, 95)):
                    s.hit("perc", tom(p), bar, st, 0.55, -0.1 if st else 0.1)
                s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.55)
                if i == 23:
                    for st in range(8, 16):
                        s.hit("drums", snare(True), bar, st, 0.2 + 0.05 * (st - 8), 0.05)
                    s.hit("fx", riser(s.bar), bar, 0, 0.2)
            s.chord("pad", v_strings_long, pad, bar, dur_steps=16, gain=0.28 if sec != "C" else 0.42, attack=0.3)
            # the Choir motif's tritone drop chanted as a riff (B and D), slowed in C
            if sec in ("B", "D") and i % 2 == 0:
                s.play("choir", v_choir, choir_motif("E4"), bar, gain=0.55, legato=0.85)
                s.play("choir", v_choir, choir_motif("E4"), bar, gain=0.3, octave=-1, legato=0.85)
            # Vorne's motif only in the machine: the arpeggio inside the bass, 16ths, from the B section on
            if sec != "A":
                shift = 12 if sec == "D" and i % 2 else 0
                for st in range(16):
                    nm = transpose(seq_notes[st % 4], shift)
                    s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st,
                          0.32 if st % 4 == 0 else 0.22, 0.3 if st % 2 else -0.3)
        for k, bar in enumerate((16, 20)):  # C: the drop at half speed, one note a half bar
            for j, nm in enumerate(["E4", "D4", "C4", "B3", "F3", "E3", "E3", "E3"][:8]):
                if j < 6:
                    s.hit("choir", v_choir(note_freq(nm), round(s.step * 7.5, 4)), base + bar + j // 2, 8 * (j % 2),
                          0.5)

    storm = current(s, 500.0, 0.7, [(-4, 0.8), (0, 0.4), (16, 0.4), (16.01, 0.9), (24, 0.9), (24.01, 0.4),
                                    (len(RS_LOOP), 0.4)], 7211)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.9),
        "metal": dict(hp=150, rev=(1.6, 0.25), seed=7212, gain=0.55),
        "hats": dict(hp=650, gain=0.38),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=7213, gain=0.7),
        "fx": dict(gain=0.5),
        "bass": dict(hp=38, duck=True, gain=0.58),
        "sub": dict(hp=25, duck=True, gain=0.55),
        "pad": dict(hp=100, duck=True, rev=(3.5, 0.45), wid=1.6, seed=7214, gain=0.42),
        "choir": dict(hp=130, rev=(3.8, 0.45), wid=1.5, seed=7215, gain=0.62),
    }, kicks, depth=0.42)
    mix = mix + storm * 0.04
    motif = s.tl.group("motif")
    motif = reverb(hp(motif, 120), seconds=2.2, mix=0.3, seed=7216)[:, :s.tl.n]
    return s, mix, motif * 1.4, RS_MOTIF_BARS


# ================================================================ the duel theme a: Honour Guard (150 BPM, A minor)

DU_CH = {  # string voicing, root
    "Am": (["A2", "E3", "A3", "C4"], "A1"),
    "F": (["A2", "F3", "A3", "C4"], "F1"),
    "Dm": (["A2", "D3", "F3", "A3"], "D2"),
    "E": (["G#2", "E3", "G#3", "B3"], "E1"),
}
DU_LOOP = (["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]    # A: the joust, call and response
           + ["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]  # B: the three-voice fugato (the three aces)
           + ["Am", "F", "Dm", "E", "Am", "F", "Dm", "E"]  # C: both motifs at once, then in canon
           + ["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"])  # D: the tutti
DU_SNARE = [0.35, 0.12, 0.12, 0.22, 0.8, 0.12, 0.25, 0.12, 0.35, 0.12, 0.12, 0.22, 0.8, 0.25, 0.4, 0.6]


def honour_guard():
    s = LoopSong(150, 4, len(DU_LOOP))
    kicks = []
    for bar in range(4):  # intro: timpani and a snare roll, low brass 8ths, the strings' gallop from bar 2
        s.hit("perc", timpani("A1" if bar < 2 else "E2"), bar, 0, 0.9)
        for st in range(0, 16, 2):
            s.hit("ost", v_brass(note_freq("A2"), round(s.step * 1.4, 4), True), bar, st, 0.32 + 0.04 * bar)
        if bar >= 2:
            for st, sm in enumerate(GALLOP):
                s.hit("gallop", v_strings_short(note_freq(transpose("A2", sm)), round(s.step * 0.75, 4)), bar, st,
                      0.38 if st % 3 == 0 else 0.24, -0.25)
    s.hit("perc", snare_roll(s.bar * 0.95), 3, 0, 0.6)
    s.hit("fx", riser(s.bar), 3, 0, 0.2)

    vorne = vorne_motif("A3")
    human = human_motif("A3")
    for base in s.sections():
        for i, name in enumerate(DU_LOOP):
            bar = base + i
            voicing, root = DU_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 24 else "D"
            if i in (0, 8, 16, 24):
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            s.hit("perc", timpani(transpose(root, 12) if root.endswith("1") else root), bar, 0, 0.75)
            if sec != "B" or i >= 12:
                s.hit("perc", timpani(transpose(root, 19) if root.endswith("1") else transpose(root, 7)), bar, 8,
                      0.5)
            for beat in range(4):
                s.hit("drums", kick(130), bar, beat * 4, 0.8 if beat % 2 == 0 else 0.55)
                kicks.append(s.pos(bar, beat * 4))
            for st, g in enumerate(DU_SNARE):  # the snare rudiments
                s.hit("snare", snare(True), bar, st, g * (0.55 if sec == "B" else 0.7), 0.1)
            # low-brass 8ths and the galloping strings
            for st in range(0, 16, 2):
                s.hit("ost", v_brass(note_freq(transpose(root, 12)), round(s.step * 1.4, 4), True), bar, st,
                      0.34 if st % 4 == 0 else 0.26)
            for st, sm in enumerate(GALLOP):
                s.hit("gallop", v_strings_short(note_freq(transpose(root, 12 + sm)), round(s.step * 0.75, 4)), bar,
                      st, 0.4 if st % 3 == 0 else 0.26, -0.25)
            s.chord("strings", v_strings_long, voicing, bar, dur_steps=16, gain=0.3, attack=0.2)
        # A: the joust, Vorne's motif in gold brass, the horn call answers
        for k in (0, 4):
            s.play("vorne", v_brass, vorne, base + k, gain=0.7, position=-0.25)
            s.play("vorne", v_trumpet, vorne, base + k, gain=0.3, position=-0.25, octave=1)
            s.play("horn", v_brass, human, base + k + 2, gain=0.65, position=0.25)
            s.play("horn", v_brass, human, base + k + 2, gain=0.35, position=0.25, octave=-1)
        # B: the fugato, three brass voices entering on Vorne's motif (A, E, then A an octave up)
        s.play("vorne", v_brass, vorne, base + 8, gain=0.6, position=-0.4, octave=-1)
        s.play("vorne", v_brass, vorne_motif("E3"), base + 10, gain=0.55, position=0.4)
        s.play("vorne", v_trumpet, vorne_motif("A4"), base + 12, gain=0.4, position=0.0)
        s.play("vorne", v_brass, ["G#3 - - - - - - - - - - - - - - -", "A3 - - - - - - - - - - - - - - -"],
               base + 14, gain=0.55, position=-0.4)
        s.play("vorne", v_brass, ["B3 - - - - - - - - - - - - - - -", "E4 - - - - - - - - - - - - - - -"],
               base + 14, gain=0.45, position=0.4)
        # C: both motifs at once, then in canon (the horn call a bar behind)
        for k in (16, 20):
            s.play("vorne", v_brass, vorne, base + k, gain=0.65, position=-0.25, octave=-1)
            s.play("horn", v_trumpet, human_motif("A4"), base + k, gain=0.45, position=0.25)
        s.play("vorne", v_brass, vorne, base + 18, gain=0.6, position=-0.25)
        s.play("horn", v_brass, human, base + 19, gain=0.6, position=0.25)
        # D: the tutti, the horn call on top, Vorne's motif under it
        for k in (24, 28):
            s.play("horn", v_trumpet, human_motif("A4"), base + k, gain=0.5, position=0.2)
            s.play("horn", v_brass, human, base + k, gain=0.55, position=0.2)
            s.play("vorne", v_brass, vorne, base + k + 2, gain=0.65, position=-0.2, octave=-1)
            s.play("vorne", v_organ, vorne, base + k + 2, gain=0.3, octave=-1)

    mix = mixdown(s, {
        "drums": dict(hp=35, gain=0.7),
        "snare": dict(hp=140, rev=(1.6, 0.2), seed=7221, gain=0.6),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=7222, gain=0.8),
        "fx": dict(gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.8, 0.2), seed=7223, gain=0.5),
        "gallop": dict(hp=70, rev=(1.8, 0.2), seed=7224, gain=0.5),
        "strings": dict(hp=80, duck=True, rev=(3.0, 0.4), wid=1.5, seed=7225, gain=0.45),
        "vorne": dict(hp=55, rev=(2.8, 0.3), seed=7226, gain=0.8),
        "horn": dict(hp=70, rev=(2.8, 0.3), seed=7227, gain=0.8),
    }, kicks, depth=0.3)
    return s, mix, None, ()


# ================================================================ the duel theme b: Three Aces (175 BPM, A minor)

TA_CH = {"Am": (["A3", "C4", "E4"], "A1"), "F": (["A3", "C4", "F4"], "F1"), "Dm": (["A3", "D4", "F4"], "D2"),
         "E": (["G#3", "B3", "E4"], "E1"), "C": (["G3", "C4", "E4"], "C2")}
TA_LOOP = (["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]    # A: the break, the reese, the stabs
           + ["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]  # B: Vorne's motif in the sequencer
           + ["Am", "F", "C", "E", "Am", "F", "Dm", "E"]   # C: the main motif soaring in the lead
           + ["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]  # D: half-time, the sequencer and the stabs alone
           + ["Am", "F", "C", "E", "Am", "F", "Dm", "E"])  # E: everything
TA_KICK = [(0, 1.0), (10, 0.8)]
TA_SNARE = [(4, 0.8), (12, 0.8), (7, 0.18), (9, 0.15), (15, 0.2)]
TA_STABS = [0, 3, 6]


def three_aces():
    s = LoopSong(175, 4, len(TA_LOOP))
    kicks = []
    for bar in range(4):  # intro: the sequencer alone, filtered up, the break from bar 2
        for st in range(16):
            nm = transpose("A3", (0, 3, 7, 11)[st % 4])
            s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st, 0.12 + 0.05 * bar,
                  0.3 if st % 2 else -0.3)
        if bar >= 2:
            for st, g in TA_KICK:
                s.hit("drums", kick(150), bar, st, g)
                kicks.append(s.pos(bar, st))
            for st, g in TA_SNARE:
                s.hit("drums", snare(True), bar, st, g * 0.8, 0.05)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.2)

    for base in s.sections():
        for i, name in enumerate(TA_LOOP):
            bar = base + i
            chord, root = TA_CH[name]
            sec = "ABCDE"[i // 8]
            if i % 8 == 0:
                s.hit("perc", crash(), bar, 0, 0.35, 0.3)
            if sec != "D":
                for st, g in TA_KICK:
                    s.hit("drums", kick(150), bar, st, g)
                    kicks.append(s.pos(bar, st))
                if i % 2 == 1:
                    s.hit("drums", kick(150), bar, 13, 0.5)
                    kicks.append(s.pos(bar, 13))
                for st, g in TA_SNARE:
                    s.hit("drums", snare(True), bar, st, g, 0.05)
                for st in range(16):
                    s.hit("hats", hat(st % 8 == 6), bar, st, 0.16 if st % 2 == 0 else 0.08, 0.3)
            else:
                s.hit("drums", kick(150), bar, 0, 0.9)
                kicks.append(s.pos(bar, 0))
                s.hit("drums", snare(), bar, 8, 0.6, 0.05)
                if i == 31:
                    for st in range(8, 16):
                        s.hit("drums", snare(True), bar, st, 0.25 + 0.06 * (st - 8), 0.05)
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 7.5, 4)), bar, 0, 0.42)
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 5.5, 4)), bar, 10, 0.36)
            # synth-brass stabs on the off-beats
            if sec in ("A", "D", "E") or i % 2 == 1:
                for st in TA_STABS:
                    s.chord("stabs", v_brass, chord, bar, st, dur_steps=1, gain=0.42, stab=True)
            # Vorne's motif as a fast sequencer arpeggio
            if sec != "A":
                shift = 12 if sec == "E" and i % 2 else 0
                for st in range(16):
                    nm = transpose("A3", (0, 3, 7, 11)[st % 4] + shift)
                    s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st,
                          0.3 if st % 4 == 0 else 0.2, 0.3 if st % 2 else -0.3)
        # the main motif soaring in the distorted lead (C and E), answered in the stabs' register
        for k in (16, 20, 32, 36):
            s.play("lead", v_dlead, human_motif("A4"), base + k, gain=0.5, position=0.1)
        s.play("lead", v_dlead, human_motif("C5"), base + 18, gain=0.4, position=-0.1)
        s.play("lead", v_dlead, human_motif("C5"), base + 34, gain=0.4, position=-0.1)

    mix = mixdown(s, {
        "drums": dict(hp=35, gain=0.85),
        "hats": dict(hp=800, gain=0.38),
        "perc": dict(hp=40, rev=(2.4, 0.25), seed=7231, gain=0.6),
        "fx": dict(gain=0.5),
        "bass": dict(hp=30, duck=True, gain=0.55),
        "stabs": dict(hp=150, duck=True, rev=(1.6, 0.25), wid=1.4, seed=7232, gain=0.5),
        "lead": dict(hp=200, dly=(3, 0.3, 0.2, 4500), rev=(2.2, 0.25), wid=1.3, seed=7233, gain=0.5),
        "motif": dict(hp=120, rev=(2.0, 0.25), seed=7234, gain=0.8),
    }, kicks, depth=0.4)
    return s, mix, None, ()


# ================================================================ the Ascendant's hand-off (from the built files)

def _asset(name):
    return decode(ASSETS / f"{name}.ogg")


def handoff(variant):
    t19 = _asset("iron-sovereign")
    t18 = _asset("choir-descends")
    loop19 = 306000                            # track 19's LOOPSTART
    bar19 = int(round(SR * 60 / 147 * 4))      # 147 BPM: 72000 samples a bar
    a = loop19 + 8 * bar19                     # from loop bar 8
    head = t19[:, a:a + 10 * SR].copy()
    head[:, :ns(0.3)] *= np.linspace(0, 1, ns(0.3))
    tail18 = t18[:, :10 * SR].copy()
    if variant == "a":
        # track 19 out over 2 s while track 18 starts from its beginning
        xf = 2 * SR
        out19 = t19[:, a + 10 * SR:a + 10 * SR + xf] * (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, xf)))
        out = np.zeros((2, head.shape[1] + tail18.shape[1]))
        out[:, :head.shape[1]] += head
        out[:, head.shape[1]:head.shape[1] + xf] += out19
        out[:, head.shape[1]:] += tail18
    else:
        # a 4 s bridge: track 19's next stretch slowing two semitones down (a tape stop) and darkening, track 18's
        # intro rising under its last 2 s
        n_bridge = 4 * SR
        src = t19[:, a + 10 * SR:a + 10 * SR + int(n_bridge * 1.0) + SR]
        bridge = glide_speed(src, 1.0, 2 ** (-2 / 12), n_bridge)
        bridge = sweep_lp(bridge, 8000.0, 600.0)
        bridge *= np.concatenate([np.ones(2 * SR), 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, 2 * SR))])
        rise = 2 * SR
        out = np.zeros((2, head.shape[1] + n_bridge - rise + tail18.shape[1]))
        out[:, :head.shape[1]] += head
        out[:, head.shape[1]:head.shape[1] + n_bridge] += bridge
        b18 = head.shape[1] + n_bridge - rise
        t = tail18.copy()
        t[:, :rise] *= np.linspace(0, 1, rise) ** 2
        out[:, b18:b18 + t.shape[1]] += t
    return out


# ================================================================ the escape sting (one-shot, 126 BPM, E)

STING_BPM = 126
HELD = ["E1", "E2", "B2", "E3"]  # the held low E (strings, organ) with the flat-2 (F) over it


def _held_end(s, bar, step):
    s.chord("low", v_strings_long, HELD, bar, step, dur_steps=24, gain=0.55, attack=0.15)
    s.chord("organ", v_organ, ["E2", "B2", "E3"], bar, step, dur_steps=24, gain=0.35)
    s.hit("sub", v_sub(note_freq("E1"), 3.0), bar, step, 0.4)
    s.hit("choir", v_choir(note_freq("F4"), 2.8), bar, step, 0.45, 0.2)
    s.hit("choir", v_choir(note_freq("F3"), 2.8, "oo"), bar, step, 0.3, -0.2)
    s.hit("perc", timpani("E2", 2.0), bar, step, 0.6)


def sting_collapse():
    s = Song(STING_BPM, 3, tail=2.6)
    # Vorne's motif once in low brass and organ: E (dotted quarter), G (8th), B (quarter), D-sharp bending down
    motif = [("E3", 0, 6), ("G3", 6, 2), ("B3", 8, 4)]
    for nm, st, ln in motif:
        s.hit("vorne", v_brass(note_freq(nm), round(s.step * ln * 0.95, 4)), 0, st, 0.7, -0.1)
        s.hit("vorne", v_organ(note_freq(transpose(nm, -12)), round(s.step * ln * 0.95, 4)), 0, st, 0.4, 0.1)
    s.hit("vorne", v_brass_bend(note_freq("D#4"), round(s.step * 10, 4), -1.0, 0.35), 0, 12, 0.7, -0.1)
    s.hit("vorne", v_organ(note_freq("D#3"), round(s.step * 6, 4)), 0, 12, 0.3, 0.1)
    s.hit("perc", timp_roll("E2", 0.9), 0, 0, 0.5)
    s.chord("low", v_strings_long, ["E2", "B2"], 0, 0, dur_steps=16, gain=0.35, attack=0.3)
    # the Choir motif's descent takes over as the bend dies: E D C B, then the tritone drop to F and E
    for k, nm in enumerate(["E4", "D4", "C4", "B3"]):
        s.hit("choir", v_choir(note_freq(nm), round(s.step * 3.6, 4)), 1, 2 + 3 * k, 0.75 - 0.03 * k,
              -0.2 + 0.13 * k)
        s.hit("choir", v_choir(note_freq(transpose(nm, -12)), round(s.step * 3.6, 4), "oo"), 1, 2 + 3 * k, 0.4)
    _held_end(s, 1, 14)
    return s, 6.6


def sting_dying_engine():
    s = Song(STING_BPM, 3, tail=2.6)
    # the machine arpeggio, rendered alone, then slowed like a dying engine
    arp = Song(STING_BPM, 2, tail=0.5)
    for bar in range(2):
        for st in range(16):
            nm = transpose("E3", (0, 3, 7, 11)[st % 4] + (12 if (st // 4) % 2 else 0))
            arp.hit("motif", v_seq(note_freq(nm), round(arp.step * 0.8, 4)), bar, st, 0.32 if st % 4 == 0 else 0.24,
                    0.3 if st % 2 else -0.3)
            if st % 4 == 0:
                arp.hit("motif", v_dbass(note_freq("E1" if st < 8 else "E2"), round(arp.step * 3, 4)), bar, st, 0.25)
    raw = _bus(arp, {"motif": dict(hp=60, rev=(1.6, 0.2), seed=7241, gain=1.0)})
    n_steady = int(1.0 * SR)
    steady, rest = raw[:, :n_steady], raw[:, n_steady:]
    dying = glide_speed(rest, 1.0, 0.45, int(2.4 * SR))
    dying = sweep_lp(dying, 7000.0, 500.0) * np.linspace(1, 0.15, dying.shape[1])
    engine = np.concatenate([steady, dying], axis=1)
    s.hit("perc", timpani("E2", 1.2), 0, 0, 0.6)
    # the choir's drop out of the dying machine: C B, then the tritone drop to F and E
    for k, nm in enumerate(["C4", "B3"]):
        s.hit("choir", v_choir(note_freq(nm), round(s.step * 3.6, 4)), 1, 2 + 4 * k, 0.5, -0.1 + 0.2 * k)
        s.hit("choir", v_choir(note_freq(transpose(nm, -12)), round(s.step * 3.6, 4), "oo"), 1, 2 + 4 * k, 0.25)
    _held_end(s, 1, 12)
    return s, 6.6, engine


def _sting_mix(s):
    return _bus(s, {
        "vorne": dict(hp=50, rev=(3.2, 0.35), seed=7251, gain=0.85),
        "choir": dict(hp=110, rev=(4.2, 0.5), wid=1.6, seed=7252, gain=0.8),
        "low": dict(hp=30, rev=(3.5, 0.4), wid=1.4, seed=7253, gain=0.6),
        "organ": dict(hp=30, rev=(4.0, 0.45), wid=1.3, seed=7254, gain=0.5),
        "perc": dict(hp=30, rev=(2.8, 0.3), damp=0.8, seed=7255, gain=0.8),
        "sub": dict(hp=25, gain=0.5),
    })


# ================================================================ main

SONGS = {"b15-a": ("act6-b-theme-r72-a", storm_wall), "b15-b": ("act6-b-theme-r72-b", red_spot),
         "duel-a": ("duel-theme-r72-a", honour_guard), "duel-b": ("duel-theme-r72-b", three_aces)}
HANDOFFS = {"handoff-a": ("ascendant-handoff-r72-a", "a"), "handoff-b": ("ascendant-handoff-r72-b", "b")}
STINGS = {"sting-a": ("escape-sting-r72-a", sting_collapse), "sting-b": ("escape-sting-r72-b", sting_dying_engine)}


def _report(path, extra=""):
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {dec.shape[1] / SR:.2f}s{extra}  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS"
          f"  true peak {tp:.1f} dBTP", flush=True)
    return dec


def render_song(key, out):
    name, fn = SONGS[key]
    s, rest, motif, motif_bars = fn()
    mix = rest + motif if motif is not None else rest
    if motif is not None:
        base = s.sections()[1]
        gaps = []
        for bar in motif_bars:
            a, b = int(s.pos(base + bar) * SR), int(s.pos(base + bar + 2) * SR)
            gaps.append(db(np.sqrt(np.mean(mix[:, a:b] ** 2))) - db(np.sqrt(np.mean(motif[:, a:b] ** 2))))
        print(f"{name}: Vorne's motif " + ", ".join(f"{g:.1f}" for g in gaps) + " dB under the mix (bars "
              + ", ".join(str(b) for b in motif_bars) + ")", flush=True)
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


def render_handoff(key, out):
    name, variant = HANDOFFS[key]
    mix = master(handoff(variant), -14.0, -2.0)
    mix = fade(mix, 0.003, 1.0)
    _report(write_ogg(out / f"{name}.ogg", mix))


def render_sting(key, out):
    name, fn = STINGS[key]
    res = fn()
    s, length = res[0], res[1]
    mix = _sting_mix(s)
    if len(res) > 2:
        engine = res[2]
        m = min(engine.shape[1], mix.shape[1])
        mix[:, :m] += engine[:, :m] * 2.0
    mix = mix[:, :int(length * SR)]
    mix = mix - mix.mean(axis=1, keepdims=True)
    mix = master(mix, -14.0, -2.0)
    mix = fade(mix, 0.003, 1.2)
    _report(write_ogg(out / f"{name}.ogg", mix))


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or [*SONGS, *HANDOFFS, *STINGS]:
        if key in SONGS:
            render_song(key, out)
        elif key in HANDOFFS:
            render_handoff(key, out)
        else:
            render_sting(key, out)


if __name__ == "__main__":
    main(sys.argv[1:])
