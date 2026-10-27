#!/usr/bin/env python3
"""Concept round 60 (M9 "Act 5 - The Belt" design phase, step AUD-P): two sketches for track 13, the Act 5 B theme
("colder, more industrial: the enemy is human now"; design/audio/music/README.md, *Act 5 music*), and two proposals
for Rook's return sting (new track 31, Level 29: the prison tender opens and the Ember launches).

The themes use the round-02/03/08/36/48 format: intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH Vorbis
comments (samples), a sample-exact loop, -14 LUFS with a -2 dBFS sample-peak ceiling. The stings are one-shots like
the round-08 and round-48 stings (-14 LUFS, -2 dBFS ceiling, a short fade at the end).

Outputs (design/audio/music/concept/, or --out DIR):
  act5-b-theme-r60-a.ogg  "Gilded Cage"  126 BPM (the brief's 128: the nearest tempo with a whole-sample 16th step,
      which the sample-exact loop needs), E minor (Vorne's motif on E G B D-sharp)
      EBM and industrial techno: a four-on-the-floor kick, a gated, distorted 16th bass sequence, struck sheet metal on
      the backbeat, chains rattling on the off-16ths and a press hammer on every other bar as the percussion, a cold
      string pad over Em - C - Am - B. Vorne's motif IN THE OPEN: stated by low brass and organ every 8 bars over the
      groove (bars 0, 8 and 16 of the loop, and once in the intro), and the machines answer it: the struck metal plays
      the motif back at speed in the next two bars.
  act5-b-theme-r60-b.ogg  "Iron Vein"  140 BPM half-time, G minor (Hollow Rock's shift key; the motif on G B-flat D
      F-sharp)
      Darker and roomier: a heavy half-time beat (the kick on 1 and the "and" of 3, one big snare a bar on 3 with a
      long dark reverb), a sub bass, a grinding drill-like texture (band noise chopped at 16ths over a growling detuned
      saw, its band sweeping), a cold pad over Gm - Eb - Cm - D. Vorne's motif ONLY IN THE MACHINES: a sequencer
      arpeggio inside the groove (G B-flat D F-sharp in 16ths, two detuned saws through a soft low-pass, no resonant
      plucks) from the B section on; no brass.

  rook-return-r60-a.ogg  "Horn Call" (about 5.5 s)
      A timpani roll and low strings on B swell for one beat, then humanity's motif's horn call (1 - 5 - 8 - 9 - 10
      in D major, the relative major of the theme's B minor) in horns and trumpet over D - A - G, its last note
      (F-sharp) held over a B minor chord: the theme's tonic, so the level's full mix carries on under it.
  rook-return-r60-b.ogg  "Answer" (about 6 s)
      Vorne's motif first, low and quiet in organ and cellos (B D F-sharp A-sharp), then the horn call answers it
      over a snare roll and a cymbal swell, brighter (trumpets on top), landing on the same B minor chord with a
      timpani hit.

Vorne's motif (1 - b3 - 5 - 7) is in its own group ("motif") in both themes so that a production version can carry it
as the third stem (track 12's method): a's brass and organ statements, b's sequencer arpeggio (lifted out of the full
mix). Its level against the whole mix in its 2-bar statements is printed: a about 6 dB under ("in the open", the
README's target), b inside the groove.

No bubbly or popping sounds: no bubble layer, no resonant short plucks, the metal hits with a 0.5 ms attack and an
inharmonic ring (struck, not plucked), every pitched voice with a soft attack except the bass sequence.

Usage: python3 tools/concept/audio/music_r60.py [a|b|sting-a|sting-b ...] [--out DIR]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks and the loop seam.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import Song, crash, hat, hp, kick, ns, riser, snare, timpani, transpose, v_brass, v_pad  # noqa: E402
from music import v_strings_long, widen  # noqa: E402
from music_r02 import LoopSong, add_tail, seam_ratio, v_dbass, v_sub  # noqa: E402
from music_r03 import human_motif, mixdown, v_organ, v_trumpet, vorne_motif  # noqa: E402
from music_r48 import deep_kick, timp_roll  # noqa: E402
from synth import (SR, adsr, db, decode, exp_decay, fade, lufs, master, noise, note_freq, ramp, reverb,  # noqa: E402
                   saturate, saw, sine, svf, write_ogg)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"


# ================================================================ industrial percussion (synthesized, struck)

@lru_cache(None)
def sheet_metal(pitch=310.0, seed=1, decay=0.22):
    """A struck steel sheet: inharmonic partials with a long uneven ring and a short noise strike (0.5 ms attack)."""
    n = ns(decay * 4)
    rng = np.random.default_rng(seed)
    x = sum(a * sine(pitch * r * (1 + 0.003 * k), n) * exp_decay(n, decay * t)
            for k, (r, a, t) in enumerate([(1, 1.0, 1.0), (1.51, 0.7, 0.8), (2.33, 0.6, 0.6), (3.17, 0.45, 0.45),
                                           (4.62, 0.3, 0.3), (6.85, 0.2, 0.2)]))
    x += svf(noise(n, rng), 2500, q=0.8, mode="hp") * exp_decay(n, 0.012) * 1.4
    x *= np.clip(np.arange(n) / ns(0.0005), 0, 1)
    return fade(saturate(x * 0.7, 1.3), 0.0005, min(0.05, decay * 2))


@lru_cache(None)
def chain(seed):
    """A short chain rattle: five small metal ticks over 50 ms, uneven, high-passed."""
    rng = np.random.default_rng(seed)
    n = ns(0.12)
    out = np.zeros(n)
    t = 0.0
    for _ in range(5):
        a = int(t * SR)
        tick = sheet_metal(float(rng.uniform(2400, 3600)), int(rng.integers(1, 9)), 0.012)
        m = min(len(tick), n - a)
        out[a:a + m] += tick[:m] * rng.uniform(0.4, 1.0)
        t += rng.uniform(0.006, 0.014)
    return fade(svf(out, 1800, q=0.7, mode="hp"), 0.0005, 0.02)


@lru_cache(None)
def press():
    """A press hammer: a deep falling thud with a heavy metal clank on it."""
    n = ns(0.7)
    x = sine(ramp(n, 95, 42, tau=0.05), n) * exp_decay(n, 0.16)
    x += svf(noise(n, np.random.default_rng(601)), 400, q=0.7) * exp_decay(n, 0.02) * 0.5
    m = sheet_metal(190.0, 7, 0.12)
    x[:len(m)] += m * 0.5
    x *= np.clip(np.arange(n) / ns(0.001), 0, 1)
    return fade(saturate(x * 1.2, 1.4), 0.0005, 0.08)


@lru_cache(None)
def v_seq(freq, dur):
    """The sequencer: two detuned saws through a soft low-pass (q 0.8, never resonant), a 3 ms attack."""
    n = ns(dur + 0.08)
    x = saw(freq * 1.004, n) + saw(freq * 0.996, n, start=0.4)
    x = svf(x, 1400 + 900 * exp_decay(n, 0.05), q=0.8)
    return x * adsr(n, 0.003, 0.08, 0.5, 0.06, gate=dur) * 0.5


@lru_cache(None)
def drill(root_hz, seconds, seed):
    """One bar of the grinding drill texture: band noise (its band sweeping 700 -> 1500 Hz) chopped at 16ths over a
    growling detuned saw two octaves up the root, saturated."""
    n = ns(seconds)
    t = np.arange(n) / SR
    step = seconds / 16
    chop = 0.35 + 0.65 * (0.5 + 0.5 * np.cos(2 * np.pi * t / step)) ** 2
    band = svf(noise(n, np.random.default_rng(seed)), ramp(n, 700, 1500, curve="lin"), q=3.0, mode="bp")
    growl = saw(root_hz * 4 * 1.01, n) + saw(root_hz * 4 * 0.99, n, start=0.3)
    growl = svf(growl, 900, q=0.9) * (0.6 + 0.4 * np.sin(2 * np.pi * t * 3 / seconds))
    x = saturate((band * chop * 1.6 + growl * 0.35) * 1.5, 2.0)
    return fade(x, 0.01, 0.01)


# ================================================================ a: Gilded Cage (126 BPM, E minor)

A_CH = {  # pad voicing, bass root
    "Em": (["E3", "G3", "B3", "E4"], "E1"),
    "C": (["E3", "G3", "C4", "E4"], "C2"),
    "Am": (["E3", "A3", "C4", "E4"], "A1"),
    "B": (["D#3", "F#3", "B3", "D#4"], "B1"),
}
A_LOOP = (["Em", "Em", "C", "C", "Am", "Am", "B", "B"]     # A: the groove, the motif at bar 0
          + ["Em", "Em", "C", "C", "Am", "Am", "B", "B"]   # B: hats and the high metal in, the motif at bar 8
          + ["Em", "Em", "C", "C", "Am", "Am", "B", "B"])  # C: the press doubles, the motif at bar 16
A_GATE = [1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1]  # the gated 16th bass sequence
A_BASS = [0, 0, 12, 0, 0, 7, 0, 12, 0, 0, 12, 0, 3, 0, 12, 7]  # semitones over the root
A_MOTIF_BARS = (0, 8, 16)


def gilded_cage():
    s = LoopSong(126, 4, len(A_LOOP))
    kicks = []
    # intro: the pad and the chains, the motif's first statement in the open, the kick from bar 2
    for bar in range(4):
        s.chord("pad", v_strings_long, A_CH["Em"][0], bar, dur_steps=16, gain=0.35, attack=0.4)
        for st in range(1, 16, 2):
            s.hit("chain", chain(st + 16 * bar), bar, st, 0.25 + 0.1 * bar, 0.4 if st % 4 == 1 else -0.4)
        if bar >= 2:
            for beat in range(4):
                s.hit("drums", kick(120), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    s.hit("perc", press(), 0, 0, 0.8)
    s.hit("perc", press(), 2, 0, 0.8)
    s.play("motif", v_brass, vorne_motif("E3"), 0, gain=0.6, position=-0.1)
    s.play("motif", v_organ, vorne_motif("E3"), 0, gain=0.45, octave=-1)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.15)

    for base in s.sections():
        for i, name in enumerate(A_LOOP):
            bar = base + i
            pad, root = A_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C"
            if i in (0, 8, 16):
                s.hit("perc", crash(), bar, 0, 0.3, 0.3)
            for beat in range(4):
                s.hit("drums", kick(120), bar, beat * 4, 0.95)
                kicks.append(s.pos(bar, beat * 4))
            for st in (4, 12):  # struck sheet metal on the backbeat, with a tight snare under it
                s.hit("metal", sheet_metal(310.0, 1 + (i % 3)), bar, st, 0.55, -0.15)
                s.hit("drums", snare(True), bar, st, 0.3, 0.05)
            for st in range(1, 16, 2):  # chains on the off-16ths
                s.hit("chain", chain(st + 16 * (i % 4)), bar, st, 0.32 if st % 4 == 3 else 0.22,
                      0.4 if st % 4 == 1 else -0.4)
            if i % 2 == 0 or sec == "C":
                s.hit("perc", press(), bar, 0, 0.75)
            if sec == "C" and i % 2 == 1:
                s.hit("perc", press(), bar, 10, 0.5)
            if sec != "A":
                for st in range(2, 16, 4):
                    s.hit("hats", hat(True), bar, st, 0.18, 0.3)
                for st in (7, 15):
                    s.hit("metal", sheet_metal(1150.0, 4, 0.08), bar, st, 0.25, 0.35)
            # the gated, distorted 16th bass sequence
            for st in range(16):
                if A_GATE[st]:
                    nm = transpose(root, 12 + A_BASS[st])
                    s.hit("bass", v_dbass(note_freq(nm), round(s.step * 0.7, 4)), bar, st, 0.36)
            s.chord("pad", v_strings_long, pad, bar, dur_steps=16, gain=0.3, attack=0.3)
            # the machines answer the motif: the struck metal plays it back at speed in the two bars after it
            if i - 2 in A_MOTIF_BARS or i - 3 in A_MOTIF_BARS:
                notes = [transpose("E4", k) for k in (0, 3, 7, 11)]
                for st in range(0, 16, 2):
                    nm = notes[(st // 2) % 4] if i - 2 in A_MOTIF_BARS else notes[3 - (st // 2) % 4]
                    s.hit("answer", sheet_metal(note_freq(nm), 3, 0.1), bar, st, 0.3, 0.25 if st % 4 else -0.25)
        # Vorne's motif in the open: low brass and organ every 8 bars over the groove
        for bar in A_MOTIF_BARS:
            s.play("motif", v_brass, vorne_motif("E3"), base + bar, gain=0.65, position=-0.1)
            s.play("motif", v_brass, vorne_motif("E3"), base + bar, gain=0.35, position=0.15, octave=-1)
            s.play("motif", v_organ, vorne_motif("E3"), base + bar, gain=0.4, octave=-1)

    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.9),
        "perc": dict(hp=30, rev=(1.8, 0.2), damp=0.8, seed=6011, gain=0.7),
        "metal": dict(hp=150, rev=(1.4, 0.25), seed=6012, gain=0.6),
        "chain": dict(hp=1500, gain=0.4),
        "hats": dict(hp=600, gain=0.4),
        "fx": dict(gain=0.5),
        "bass": dict(hp=38, duck=True, gain=0.6),
        "pad": dict(hp=110, duck=True, rev=(3.5, 0.45), wid=1.6, seed=6013, gain=0.45),
        "answer": dict(hp=300, dly=(3, 0.3, 0.25, 3000), rev=(1.6, 0.25), seed=6014, gain=0.55),
    }, kicks, depth=0.4)
    motif = s.tl.group("motif")
    motif = reverb(hp(motif, 60), seconds=2.8, mix=0.3, seed=6015)[:, :s.tl.n]
    return s, mix, motif * 0.45, A_MOTIF_BARS


# ================================================================ b: Iron Vein (140 BPM half-time, G minor)

B_CH = {  # pad voicing, root
    "Gm": (["G2", "D3", "A#3"], "G1"),
    "Eb": (["G2", "D#3", "A#3"], "D#2"),
    "Cm": (["G2", "C3", "D#3"], "C2"),
    "D": (["F#2", "A2", "D3"], "D2"),
}
B_LOOP = (["Gm", "Gm", "Eb", "Eb", "Cm", "Cm", "D", "D"]    # A: the half-time beat, the drill
          + ["Gm", "Gm", "Eb", "Eb", "Cm", "Cm", "D", "D"]  # B: the sequencer (the motif) in, hats
          + ["Gm", "Eb", "Cm", "D"]                         # C: drums out, the drill and the sequencer alone
          + ["Gm", "Gm", "Eb", "Eb", "Cm", "Cm", "D", "D"])  # D: full
B_KICK = [(0, 1.0), (10, 0.65), (14, 0.35)]
B_MOTIF_BARS = (8, 16, 20)


def iron_vein():
    s = LoopSong(140, 4, len(B_LOOP))
    kicks = []
    for bar in range(4):  # intro: the pad and the drill, the sub; the beat from bar 2
        pad, root = B_CH["Gm"]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.35, bright=650.0)
        s.hit("drill", drill(note_freq(root), round(s.bar, 4), 6020 + bar), bar, 0, 0.12 + 0.06 * bar)
        if bar >= 2:
            s.hit("drums", deep_kick(), bar, 0, 0.9)
            kicks.append(s.pos(bar, 0))
            s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.5)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.15)

    seq_notes = [transpose("G3", k) for k in (0, 3, 7, 11)]  # G B-flat D F-sharp
    for base in s.sections():
        for i, name in enumerate(B_LOOP):
            bar = base + i
            pad, root = B_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 20 else "D"
            if i in (0, 8, 20):
                s.hit("perc", crash(), bar, 0, 0.25, 0.3)
            if sec != "C":
                for st, g in B_KICK:
                    s.hit("drums", deep_kick(), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                s.hit("snare", snare(), bar, 8, 0.65, 0.05)
                s.hit("metal", sheet_metal(240.0, 5, 0.3), bar, 8, 0.25, -0.2)
                if sec != "A":
                    for st in range(2, 16, 4):
                        s.hit("hats", hat(), bar, st, 0.12, 0.3)
                if sec == "D":
                    for st in range(1, 16, 2):
                        s.hit("hats", hat(), bar, st, 0.06, -0.3)
            elif i == 19:
                for st in (8, 11, 12, 14):
                    s.hit("drums", deep_kick(), bar, st, 0.5 + 0.08 * (st - 8))
                    kicks.append(s.pos(bar, st))
            s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.55)
            s.hit("drill", drill(note_freq(root), round(s.bar, 4), 6030 + i), bar, 0,
                  0.3 if sec == "C" else 0.22)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.3 if sec != "C" else 0.42, bright=650.0)
            # Vorne's motif only in the machines: the sequencer in 16ths, from the B section on
            if sec != "A":
                shift = 12 if sec == "D" and i % 2 else 0
                for st in range(16):
                    nm = transpose(seq_notes[st % 4], shift)
                    s.hit("motif", v_seq(note_freq(nm), round(s.step * 0.8, 4)), bar, st,
                          0.32 if st % 4 == 0 else 0.22, 0.3 if st % 2 else -0.3)

    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.9),
        "snare": dict(hp=120, rev=(3.4, 0.45), damp=0.7, seed=6021, gain=0.7),
        "metal": dict(hp=150, rev=(3.0, 0.4), seed=6022, gain=0.5),
        "hats": dict(hp=700, gain=0.4),
        "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.45, seed=6023),
        "fx": dict(gain=0.5),
        "sub": dict(hp=25, duck=True, gain=0.6),
        "drill": dict(hp=200, duck=True, rev=(2.0, 0.25), wid=1.3, seed=6024, gain=0.55),
        "pad": dict(hp=90, duck=True, rev=(4.0, 0.45), wid=1.6, seed=6025, gain=0.45),
    }, kicks, depth=0.45)
    motif = s.tl.group("motif")
    motif = reverb(hp(motif, 120), seconds=2.2, mix=0.3, seed=6026)[:, :s.tl.n]
    return s, mix, motif * 1.5, B_MOTIF_BARS


# ================================================================ Rook's return sting (one-shot, 135 BPM)

STING_BPM = 135


def _bus(s, spec):
    total = np.zeros((2, s.tl.n))
    for name, o in spec.items():
        x = s.tl.group(name)
        if not x.any():
            continue
        if o.get("hp"):
            x = hp(x, o["hp"])
        if o.get("rev"):
            sec, mx = o["rev"]
            x = reverb(x, seconds=sec, mix=mx, damping=o.get("damp", 0.5), seed=o.get("seed", 7))[:, :s.tl.n]
        if o.get("wid"):
            x = widen(x, o["wid"])
        total += x * o.get("gain", 1.0)
    return total


def snare_roll(seconds, seed_gain=0.5):
    n = ns(seconds + 0.3)
    out = np.zeros(n)
    hit = snare(True)
    k = int(seconds * 24)
    for j in range(k):
        i = int(j * SR / 24)
        seg = hit[: n - i]
        out[i:i + len(seg)] += seg * (0.15 + 0.6 * (j / k) ** 1.5) * seed_gain
    return out


def horn_call(s, start_bar, start_step, gain, bright):
    """Humanity's motif's horn call in D (D A D E F-sharp) on horns, a trumpet an octave up when `bright`,
    over D - A - G, the last note held over B minor."""
    call = human_motif("D4", major=True)
    s.play("horn", v_brass, call, start_bar, gain=0.7 * gain, position=-0.15, legato=1.0)
    s.play("horn", v_brass, call, start_bar, gain=0.4 * gain, position=0.15, octave=-1, legato=1.0)
    if bright:
        s.play("trumpet", v_trumpet, call, start_bar, gain=0.35 * gain, octave=1, legato=1.0)
    b0 = start_bar
    s.chord("strings", v_strings_long, ["D3", "A3", "F#4"], b0, start_step, dur_steps=6, gain=0.45, attack=0.05)
    s.chord("strings", v_strings_long, ["C#3", "A3", "E4"], b0, start_step + 6, dur_steps=8, gain=0.45, attack=0.05)
    s.chord("strings", v_strings_long, ["B2", "G3", "D4"], b0, start_step + 14, dur_steps=2, gain=0.45, attack=0.03)
    # the last note (F-sharp) held over B minor: the theme's tonic
    s.chord("strings", v_strings_long, ["B1", "B2", "F#3", "D4"], b0 + 1, start_step, dur_steps=14, gain=0.55,
            attack=0.08)
    s.chord("organ", v_organ, ["B1", "F#2", "B2", "D3"], b0 + 1, start_step, dur_steps=14, gain=0.4)
    s.hit("perc", timpani("B1", 1.8), b0 + 1, start_step, 0.9)
    s.hit("perc", crash(), b0 + 1, start_step, 0.35, 0.2)
    s.hit("low", v_sub(note_freq("B1"), 2.0), b0 + 1, start_step, 0.35)


def sting_horn_call():
    s = Song(STING_BPM, 3, tail=1.6)
    s.hit("perc", timp_roll("B1", 0.85), 0, 0, 0.8)
    s.chord("strings", v_strings_long, ["B1", "F#2"], 0, 0, dur_steps=4, gain=0.4, attack=0.2)
    horn_call(s, 0, 4, 1.0, bright=False)
    return s, 5.6


def sting_answer():
    s = Song(STING_BPM, 3, tail=1.6)
    # Vorne's motif first, low and quiet: organ and cellos, B D F-sharp A-sharp in eighths
    for k, nm in enumerate(["B2", "D3", "F#3", "A#3"]):
        s.hit("vorne", v_organ(note_freq(nm), 0.24), 0, 2 * k, 0.4, -0.2)
        s.hit("vorne", v_strings_long(note_freq(transpose(nm, -12)), 0.24, 0.02), 0, 2 * k, 0.3, 0.2)
    s.hit("perc", snare_roll(0.9), 0, 8, 0.6)
    s.hit("fx", riser(0.9), 0, 8, 0.15)
    horn_call(s, 1, 0, 1.0, bright=True)
    return s, 6.2


def _render_sting_mix(s):
    return _bus(s, {
        "horn": dict(hp=70, rev=(3.0, 0.35), seed=6031, gain=0.85),
        "trumpet": dict(hp=200, rev=(3.0, 0.35), seed=6032, gain=0.7),
        "strings": dict(hp=40, rev=(3.0, 0.4), wid=1.5, seed=6033, gain=0.6),
        "organ": dict(hp=35, rev=(4.0, 0.45), wid=1.3, seed=6034, gain=0.5),
        "vorne": dict(hp=50, rev=(3.5, 0.45), seed=6035, gain=0.7),
        "perc": dict(hp=30, rev=(2.8, 0.3), damp=0.8, seed=6036, gain=0.85),
        "fx": dict(rev=(2.5, 0.3), seed=6037, gain=0.6),
        "low": dict(hp=25, gain=0.6),
    })


# ================================================================ main

SONGS = {"a": ("act5-b-theme-r60-a", gilded_cage), "b": ("act5-b-theme-r60-b", iron_vein)}
STINGS = {"sting-a": ("rook-return-r60-a", sting_horn_call), "sting-b": ("rook-return-r60-b", sting_answer)}


def render_song(key, out):
    name, fn = SONGS[key]
    s, rest, motif, motif_bars = fn()
    mix = rest + motif
    base = s.sections()[1]
    gaps = []
    for bar in motif_bars:
        a, b = int(s.pos(base + bar) * SR), int(s.pos(base + bar + 2) * SR)
        gaps.append(db(np.sqrt(np.mean(mix[:, a:b] ** 2))) - db(np.sqrt(np.mean(motif[:, a:b] ** 2))))
    print(f"{name}: Vorne's motif " + ", ".join(f"{g:.1f}" for g in gaps) + " dB under the mix (bars "
          + ", ".join(str(b) for b in motif_bars) + ")", flush=True)
    mix = mix - mix.mean(axis=1, keepdims=True)
    # mastered with 1 s of the next loop pass appended (the render is periodic), so the limiter's gain at the loop's
    # end matches the gain at the loop start
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    mix = master(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, -2.0)[:, :mix.shape[1]]
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))  # fade-in only: the end loops
    path = write_ogg(out / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {audio.shape[1] / SR:.1f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
          f"  (samples {loop_start}+{loop_len})  seam {seam_ratio(dec, loop_start, loop_len):.2f}"
          f"  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak {tp:.1f} dBTP", flush=True)


def render_sting(key, out):
    name, fn = STINGS[key]
    s, length = fn()
    mix = _render_sting_mix(s)[:, :int(length * SR)]
    mix = mix - mix.mean(axis=1, keepdims=True)
    mix = master(mix, -14.0, -2.0)
    mix = fade(mix, 0.003, 1.0)
    path = write_ogg(out / f"{name}.ogg", mix)
    dec = decode(path)
    loud, tp = lufs(dec)
    print(f"{path.name}: {mix.shape[1] / SR:.2f}s  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS"
          f"  true peak {tp:.1f} dBTP", flush=True)


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or [*SONGS, *STINGS]:
        if key in SONGS:
            render_song(key, out)
        else:
            render_sting(key, out)


if __name__ == "__main__":
    main(sys.argv[1:])
