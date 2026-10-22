#!/usr/bin/env python3
"""Concept round 48 (M8 "Act 4 - Deep Water" design phase, step AUD48): two sketches for track 11, the Act 4 B
theme ("muffled ambient drum & bass, Vorne's motif as a faint layer in Levels 27-28"), and two proposals for the
reveal sting (Level 28: the Ascendancy cruisers rising through the ice, Vorne's motif hidden in it).

The themes use the round-02/03/08/36 format: intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH Vorbis comments
(samples), a sample-exact loop, -14 LUFS with a -2 dBFS sample-peak ceiling. The stings are one-shots like the
round-08 stings (-14 LUFS, -2 dBFS ceiling, a short fade at the end).

Outputs (design/audio/music/concept/, or --out DIR):
  act4-b-theme-r48-a.ogg  "Pressure Line"  175 BPM, F minor (the A theme "Thera Deep"'s key and pads)
      Liquid ambient drum & bass: a two-step break (kick 1 and the "and" of 3, snares on 2 and 4, ghost notes)
      under a slowly moving water filter that never opens fully, a deep reese and sub, warm wide pads, a soft
      lamp-like arpeggio of sine-and-triangle notes through a dotted-8th delay (no resonant plucks), the A theme's
      sonar ping every two bars, a slow flute line, a far synthesized whale call in the breakdown and a deep
      current of low noise looped with the loop.
      Vorne's motif: the intercepted whistle of track 9 "Grey Signal" (the same signal, now heard through water):
      F4 A-flat4 C5 E5 through a radio band-pass and then a 1.8 kHz low-pass with a long reverb.
  act4-b-theme-r48-b.ogg  "Black Current"  140 BPM, C minor
      Half-time deep drum & bass: a heavy kick, one snare a bar (on 3) with a long dark reverb, rolling 16th
      shakers and broken kick pick-ups, a growling reese that swells over each pair of bars, dub chord stabs with
      low resonance through a long ping-pong delay, a dark pad, a pressure drone; more space than a, closer to
      Level 26's darkness.
      Vorne's motif: not a signal but the machines themselves, far behind the hull: cold organ and low brass,
      C3 E-flat3 G3 B3, under a 900 Hz low-pass and a 5 s reverb.

  reveal-sting-r48-a.ogg  "Black and Gold" (B minor, about 9 s)
      Low tremolo strings, an organ pedal and a timpani roll swelling for 4.0 s as the cruisers rise (a muffled
      start opening as the water lifts), then at 4.0 s (the gun ports open) a tutti whose voices enter one by one
      in the motif's rhythm, B D F-sharp A-sharp: the motif stacked into a chord rather than played as a tune,
      the A-sharp left on top unresolved in the brass while the low strings fall away.
  reveal-sting-r48-b.ogg  "Ours" (B minor, about 9 s)
      Track 9's intercepted whistle comes out of static on the motif (B D F-sharp A-sharp) while a sub swell and a
      low brass cluster rise under it; at 4.0 s a deep impact, and the whistle's radio band-pass opens into full
      low brass and organ playing the motif's last two notes in unison, the A-sharp held over a B minor drone:
      the signal heard in Act 3 is the fleet.

Vorne's motif (1 - b3 - 5 - 7; see design/audio/music/README.md#motifs) is in its own group ("motif") in both
themes so that a production version can carry it as a third stem (track 9's method), entering in the loop's C
section; the intro and the A and B sections have none. Its level is measured in its 2-bar statements against the whole
mix (printed): a 16 dB under in the breakdown, 20 in D; b 17.5-18.4 throughout (Act 3's target: 17 and 19).

No bubbly or popping sounds (the user's rule, round 48 Q12): no bubble layer (the round-02 A theme's `bubble` is
not used), no resonant short plucks, every voice with a soft attack except the drums.

Usage: python3 tools/concept/audio/music_r48.py [a|b|sting-a|sting-b ...] [--out DIR]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks and the loop seam.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import Song, crash, hat, hp, kick, ns, riser, snare, taiko, timpani, transpose, v_brass, v_pad  # noqa: E402
from music import v_strings_long, widen  # noqa: E402
from music_r02 import LoopSong, add_tail, seam_ratio, shaker, v_flute, v_reese, v_sub, whale  # noqa: E402
from music_r03 import mixdown, v_organ, v_tremolo, vorne_motif  # noqa: E402
from music_r36b import bandpass, lowpass, static_swell, v_whistle  # noqa: E402
from synth import (SR, adsr, db, decode, delay, exp_decay, fade, lufs, master, noise, note_freq,  # noqa: E402
                   ramp, reverb, saw, sine, svf, triangle, write_ogg)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"


# ================================================================ shared voices

@lru_cache(None)
def v_lamp(freq, dur):
    """A soft lamp-like arpeggio note: sine and triangle, a 4 ms attack, no filter resonance (never a bloop)."""
    n = ns(dur + 0.25)
    x = sine(freq, n) * 0.7 + triangle(freq * 2, n) * 0.18 + sine(freq * 3, n) * 0.06
    return x * adsr(n, 0.004, 0.18, 0.35, 0.2, gate=dur)


@lru_cache(None)
def v_stab_low(freq, dur, cutoff=1200.0):
    """A dub chord stab with low resonance (q 1.0): two detuned saws, the low-pass closing."""
    n = ns(dur + 0.3)
    x = saw(freq * 1.003, n) + saw(freq * 0.997, n, start=0.3)
    x = svf(x, cutoff * (0.4 + 0.6 * exp_decay(n, 0.06)), q=1.0)
    return x * adsr(n, 0.004, 0.12, 0.3, 0.18, gate=dur) * 0.6


@lru_cache(None)
def sonar_soft():
    """The A theme's sonar ping with a softer 6 ms attack and a longer ring."""
    n = ns(1.2)
    x = sine(ramp(n, 1420, 1395), n) * exp_decay(n, 0.35)
    x *= np.clip(np.arange(n) / ns(0.006), 0, 1)
    return fade(x, 0.0005, 0.08)


@lru_cache(None)
def ghost_snare():
    return snare(True) * 0.35


@lru_cache(None)
def deep_kick():
    """A heavier, longer kick for the half-time groove (music.kick at a low tone plus a sub tail)."""
    k = kick(95)
    n = ns(0.5)
    tail = sine(ramp(n, 62, 44, tau=0.15), n) * exp_decay(n, 0.18) * 0.5
    out = np.zeros(max(len(k), n))
    out[:len(k)] += k
    out[:n] += tail
    return fade(out, 0.0005, 0.05)


def current(s, centre, octaves, gain_points, seed):
    """A deep current of noise made periodic in the loop (music_r36b.dust_wind's method, a lower band): one loop
    length of circular noise round `centre` Hz, its level and band moving with sinusoids that complete whole cycles
    per loop, tiled so that a tile boundary falls on the loop start; no jump at the seam."""
    l_n = int(round(s.loop * 16 * s.step_n))
    i_n = int(round(s.intro * 16 * s.step_n))
    rng = np.random.default_rng(seed)
    t = np.arange(l_n) / SR
    base = SR / l_n
    freqs = np.fft.rfftfreq(l_n, 1 / SR)
    shape = np.exp(-((np.log(np.maximum(freqs, 1.0)) - np.log(centre)) / octaves) ** 2)
    chans = []
    for ch in range(2):
        src = np.fft.irfft(shape * np.exp(2j * np.pi * rng.random(len(freqs))), l_n)
        src /= np.std(src) + 1e-12
        level = 0.6 + 0.4 * np.sin(2 * np.pi * 2 * base * t + ch * 1.3) ** 2
        chans.append(src * level)
    one = np.array(chans)
    reps = int(np.ceil(s.tl.n / l_n)) + 2
    start = (-i_n) % l_n
    return np.tile(one, reps)[:, start:start + s.tl.n] * s.curve(gain_points)


def muffle(stereo, cutoff):
    return np.vstack([svf(ch, cutoff, q=0.9) for ch in stereo])


# ================================================================ a: Pressure Line (175 BPM, F minor)

A_CH = {  # pad voicing, bass root, arpeggio (8 eighths)
    "Fm9": (["F3", "G#3", "C4", "G4"], "F1", ["F4", "C5", "G#4", "C5", "G5", "C5", "G#4", "C5"]),
    "Dbmaj7": (["C#3", "F3", "G#3", "C4"], "C#2", ["F4", "C5", "G#4", "C5", "F5", "C5", "G#4", "C5"]),
    "Bbm9": (["A#2", "C#3", "F3", "C4"], "A#1", ["F4", "C#5", "A#4", "C#5", "C5", "C#5", "A#4", "F4"]),
    "Csus": (["C3", "F3", "G3", "C4"], "C2", ["F4", "C5", "G4", "C5", "F5", "C5", "G4", "C5"]),
    "C": (["C3", "E3", "G3", "C4"], "C2", ["E4", "C5", "G4", "C5", "E5", "C5", "G4", "C5"]),
    "Ab": (["C3", "D#3", "G#3", "C4"], "G#1", ["D#4", "C5", "G#4", "C5", "D#5", "C5", "G#4", "C5"]),
}
A_LOOP = (["Fm9", "Fm9", "Dbmaj7", "Dbmaj7", "Bbm9", "Bbm9", "Csus", "C"]    # A: the break, the arpeggio
          + ["Fm9", "Fm9", "Dbmaj7", "Ab", "Bbm9", "Bbm9", "Csus", "C"]      # B: the flute line
          + ["Fm9", "Fm9", "Dbmaj7", "Dbmaj7", "Bbm9", "Bbm9", "Csus", "C"]  # C: breakdown, the motif
          + ["Fm9", "Fm9", "Dbmaj7", "Ab", "Bbm9", "Bbm9", "Csus", "C"])     # D: full, the motif
A_FLUTE = [
    "C5 - - - - - - - - - - - G#4 - - -",
    "G4 - - - - - - - - - - - . . . .",
    "F4 - - - - - - - G#4 - - - C5 - - -",
    "D#5 - - - - - - - - - - - C5 - - -",
    "C#5 - - - - - - - - - - - C5 - - -",
    "A#4 - - - - - - - - - - - G#4 - - -",
    "G4 - - - - - - - - - - - - - - -",
    "E4 - - - - - - - - - - - . . . .",
]
A_KICK = [(0, 1.0), (10, 0.9)]
A_SNARE = [(4, 1.0), (12, 1.0)]
A_GHOST = [(7, 0.4), (9, 0.3), (14, 0.35)]
A_BASS = [(0, 0, 9), (10, 0, 4), (14, 12, 2)]  # (step, semitones, length in steps)


def pressure_line():
    s = LoopSong(175, 4, len(A_LOOP))
    kicks = []
    # intro: the current, the pad, sonar, a far whale; the break filtered in from bar 2
    for bar, name in enumerate(["Fm9", "Fm9", "Dbmaj7", "Csus"]):
        pad, root, arp = A_CH[name]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.45, bright=1100.0)
        if bar >= 2:
            for st, g in A_KICK:
                s.hit("drums", kick(110), bar, st, 0.7 * g)
                kicks.append(s.pos(bar, st))
            for st, g in A_SNARE:
                s.hit("drums", snare(True), bar, st, 0.3 * g, 0.05)
        for st in range(0, 16, 2):
            s.hit("arp", v_lamp(note_freq(arp[st // 2]), round(s.step * 1.6, 4)), bar, st, 0.18 + 0.04 * bar,
                  0.35 if st % 4 else -0.35)
    s.hit("ping", sonar_soft(), 0, 0, 0.5)
    s.hit("ping", sonar_soft(), 2, 0, 0.5)
    s.hit("fx", whale(170.0, 120.0, 3.0), 0, 8, 0.3, -0.5)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.18)

    for base in s.sections():
        for i, name in enumerate(A_LOOP):
            bar = base + i
            pad, root, arp = A_CH[name]
            sec = "ABCD"[i // 8]
            deep = sec == "C"
            if i in (0, 8, 24):
                s.hit("perc", crash(), bar, 0, 0.25, 0.3)
            if not deep or i >= 30:
                for st, g in A_KICK:
                    s.hit("drums", kick(110), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                for st, g in A_SNARE:
                    s.hit("drums", snare(True), bar, st, 0.5 * g, 0.05)
                for st, g in A_GHOST:
                    s.hit("drums", ghost_snare(), bar, st, 0.5 * g, -0.1)
                for st in range(16):
                    s.hit("hats", hat(), bar, st, (0.1 if st % 2 == 0 else 0.05) * (1.3 if sec == "D" else 1.0),
                          0.3)
                if sec in ("B", "D"):
                    for st in (2, 6, 10, 14):
                        s.hit("hats", hat(True), bar, st, 0.1, -0.3)
            if not deep:
                for st, semis, ln in A_BASS:
                    s.hit("bass", v_reese(note_freq(transpose(root, 12 + semis)), round(s.step * ln * 0.95, 4)),
                          bar, st, 0.4)
            s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.55)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.42 if not deep else 0.5, bright=1400.0)
            for st in range(0, 16, 2):
                s.hit("arp", v_lamp(note_freq(arp[st // 2]), round(s.step * 1.6, 4)), bar, st,
                      0.26 if not deep else 0.16, 0.35 if st % 4 else -0.35)
            if i % 2 == 0:
                s.hit("ping", sonar_soft(), bar, 0, 0.4)
            if i == 30:
                s.hit("fx", riser(s.bar * 2), bar, 0, 0.22)
        s.play("lead", v_flute, A_FLUTE, base + 8, gain=0.5)
        s.play("lead", v_flute, A_FLUTE, base + 24, gain=0.5, octave=1)
        s.hit("fx", whale(150.0, 230.0, 3.4), base + 17, 4, 0.32, 0.5)
        s.hit("fx", whale(210.0, 125.0, 3.8), base + 20, 8, 0.28, -0.5)
        # Vorne's motif: the intercepted whistle under water, C (bars 16, 20) and D (24, 28)
        for bar in (16, 20, 24, 28):
            s.play("motif", v_whistle, vorne_motif("F4"), base + bar, gain=0.5, position=0.25)
            s.hit("motif_static", static_swell(round(2 * s.bar, 4), 4801 + bar), base + bar, 0, 0.25, 0.25)

    tl = s.tl
    water = s.curve([(-4, 600.0), (-1, 1300.0), (0, 1500.0), (8, 1800.0), (15, 2600.0), (16, 1100.0),
                     (23, 1400.0), (24, 2400.0), (31, 3200.0), (32, 1500.0)])
    under = mixdown(s, {
        "drums": dict(hp=35, gain=0.85),
        "hats": dict(hp=500, gain=0.45),
        "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.5, seed=4811),
        "fx": dict(gain=0.5),
        "bass": dict(hp=40, duck=True, gain=0.5),
        "sub": dict(hp=25, duck=True, gain=0.55),
        "pad": dict(hp=110, duck=True, rev=(4.5, 0.45), wid=1.6, seed=4812, gain=0.5),
        "arp": dict(hp=180, dly=(3, 0.5, 0.4, 1800), rev=(3.0, 0.3), wid=1.3, seed=4813, gain=0.5),
        "lead": dict(hp=180, dly=(6, 0.45, 0.35, 2400), rev=(4.0, 0.4), wid=1.4, seed=4814, gain=0.6),
    }, kicks, depth=0.5)
    under = np.vstack([svf(ch, water, q=0.9) for ch in under])  # the water: never fully open
    ping = delay(tl.group("ping"), s.step * 6, feedback=0.55, mix=0.5, damp=2400, tail=0)[:, :tl.n]
    ping = reverb(ping, seconds=4.5, mix=0.6, seed=4815)[:, :tl.n]
    motif = tl.group("motif") + tl.group("motif_static")
    if motif.any():
        motif = reverb(lowpass(bandpass(motif, 700.0, 2600.0), 1800.0), seconds=4.5, mix=0.55, seed=4816)[:, :tl.n]
    deep_water = current(s, 160.0, 0.7, [(-4, 1.0), (0, 0.6), (16, 0.6), (16.01, 1.0), (24, 1.0), (24.5, 0.6),
                                          (32, 0.6)], 4817)
    return s, under + ping * 0.22 + deep_water * 0.05, motif * 0.21, (16, 20, 24, 28)


# ================================================================ b: Black Current (140 BPM, C minor)

B_CH = {  # stab voicing, root, pad voicing
    "Cm9": (["D#3", "G3", "A#3", "D4"], "C2", ["C3", "G3", "D#4"]),
    "Abmaj7": (["C3", "D#3", "G3"], "G#1", ["G#2", "D#3", "C4"]),
    "Fm9": (["G#2", "C3", "D#3", "G3"], "F1", ["F2", "C3", "G#3"]),
    "G7b9": (["B2", "D3", "F3", "G#3"], "G1", ["G2", "D3", "B3"]),
}
B_LOOP = (["Cm9", "Cm9", "Abmaj7", "Abmaj7", "Fm9", "Fm9", "G7b9", "G7b9"]    # A: the half-time groove
          + ["Cm9", "Cm9", "Abmaj7", "Abmaj7", "Fm9", "Fm9", "G7b9", "G7b9"]  # B: stabs open, shakers
          + ["Cm9", "Cm9", "Abmaj7", "G7b9"]                                  # C: drums out, the motif
          + ["Cm9", "Cm9", "Abmaj7", "Abmaj7", "Fm9", "Fm9", "G7b9", "G7b9"])  # D: full, the motif
B_STABS = [(3, 0.85), (6, 0.5), (11, 0.7)]
B_KICK = [(0, 1.0), (10, 0.55), (13, 0.4)]


def black_current():
    s = LoopSong(140, 4, len(B_LOOP))
    kicks = []
    for bar in range(4):  # intro: the drone, the stabs closed, the reese stirring; the kick from bar 2
        voicing, root, pad = B_CH["Cm9"]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4, bright=700.0)
        for st, g in B_STABS:
            s.chord("stab", v_stab_low, voicing, bar, st, dur_steps=1, gain=g * 0.7, spread=0.5,
                    cutoff=500.0 + 150 * bar)
        if bar >= 2:
            s.hit("drums", deep_kick(), bar, 0, 0.85)
            kicks.append(s.pos(bar, 0))
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 15, 4)), bar, 0, 0.3)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.18)

    for base in s.sections():
        for i, name in enumerate(B_LOOP):
            bar = base + i
            voicing, root, pad = B_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 20 else "D"
            deep = sec == "C"
            if i in (0, 8, 20):
                s.hit("perc", crash(), bar, 0, 0.25, 0.3)
            if not deep:
                for st, g in B_KICK:
                    s.hit("drums", deep_kick(), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                s.hit("snare", snare(), bar, 8, 0.6, 0.05)
                if sec != "A":
                    s.hit("drums", ghost_snare(), bar, 15, 0.4, -0.1)
                for st in range(16):
                    s.hit("hats", shaker(), bar, st, 0.08 if st % 2 else 0.12, -0.35)
                if sec == "D":
                    for st in range(1, 16, 2):
                        s.hit("hats", hat(), bar, st, 0.07, 0.35)
            elif i == 19:
                for st in (8, 11, 12, 14):
                    s.hit("drums", deep_kick(), bar, st, 0.5 + 0.08 * (st - 8))
                    kicks.append(s.pos(bar, st))
            cut = {"A": 700.0 + 80 * (i % 8), "B": 1300.0, "C": 900.0, "D": 1900.0}[sec]
            for st, g in B_STABS:
                s.chord("stab", v_stab_low, voicing, bar, st, dur_steps=1, gain=g, spread=0.5, cutoff=round(cut, -1))
            if i % 2 == 0:  # the reese swells over each pair of bars
                nxt = B_CH[B_LOOP[(i + 1) % len(B_LOOP)]][1]
                ln = 16 if nxt != root else 32
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * (ln - 1), 4)), bar, 0, 0.42)
                if nxt != root:
                    s.hit("bass", v_reese(note_freq(transpose(nxt, 12)), round(s.step * 15, 4)), bar + 1, 0, 0.42)
            s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.5)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.25 if not deep else 0.38, bright=650.0)
        # Vorne's motif: the machines far behind the hull, C (bar 16) and D (20, 24)
        for bar in (16, 20, 24):
            s.play("motif", v_organ, vorne_motif("C4"), base + bar, gain=0.55, octave=-1)
            s.play("motif", v_brass, vorne_motif("C3"), base + bar, gain=0.35, position=-0.2)

    tl = s.tl
    swell = s.curve([(-4, 0.4), (0, 0.8)] + [(2 * k, 0.75) for k in range(14)] + [(2 * k + 1.5, 1.0) for k in range(14)])
    tl.groups["bass"] = tl.group("bass") * swell if "bass" in tl.groups else tl.group("bass")
    mix = mixdown(s, {
        "drums": dict(hp=30, gain=0.85),
        "snare": dict(hp=120, rev=(3.2, 0.4), damp=0.7, seed=4821, gain=0.7),
        "hats": dict(hp=600, gain=0.45),
        "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.45, seed=4822),
        "fx": dict(gain=0.5),
        "bass": dict(hp=35, duck=True, gain=0.55),
        "sub": dict(hp=25, duck=True, gain=0.55),
        "stab": dict(hp=150, dly=(3, 0.55, 0.4, 1800), rev=(3.5, 0.4), wid=1.4, seed=4823, gain=0.7),
        "pad": dict(hp=90, duck=True, rev=(4.0, 0.45), wid=1.6, seed=4824, gain=0.45),
    }, kicks, depth=0.45)
    water = s.curve([(-4, 700.0), (0, 1600.0), (8, 2200.0), (16, 1200.0), (20, 2600.0), (28, 1600.0)])
    mix = np.vstack([svf(ch, water, q=0.9) for ch in mix])
    motif = tl.group("motif")
    if motif.any():
        motif = reverb(lowpass(hp(motif, 70), 900.0), seconds=5.0, mix=0.6, seed=4825)[:, :tl.n]
    drone = current(s, 90.0, 0.5, [(-4, 1.0), (0, 0.7), (16, 0.7), (16.01, 1.0), (20, 1.0), (20.5, 0.7),
                                    (28, 0.7)], 4826)
    return s, mix + drone * 0.06, motif * 0.1, (16, 20, 24)


# ================================================================ reveal stings (one-shot, B minor)

STING_BPM = 120          # a 4-bar timeline: bar 2 = 4.0 s, the hit as the gun ports open
HIT_BAR = 2


def _sting_bus(s, spec):
    tl = s.tl
    total = np.zeros((2, tl.n))
    for name, o in spec.items():
        x = tl.group(name)
        if not x.any():
            continue
        if o.get("hp"):
            x = hp(x, o["hp"])
        if o.get("lp"):
            x = muffle(x, o["lp"])
        if o.get("rev"):
            sec, mx = o["rev"]
            x = reverb(x, seconds=sec, mix=mx, damping=o.get("damp", 0.5), seed=o.get("seed", 7))[:, :tl.n]
        if o.get("wid"):
            x = widen(x, o["wid"])
        total += x * o.get("gain", 1.0)
    return total


@lru_cache(None)
def timp_roll(note, seconds):
    """A timpani roll swelling: hits every 1/16 s, the level rising with the square of time."""
    n = ns(seconds + 1.5)
    out = np.zeros(n)
    hit = timpani(note, 1.2)
    k = int(seconds * 16)
    for j in range(k):
        i = int(j * SR / 16)
        g = 0.15 + 0.85 * (j / k) ** 2
        seg = hit[: n - i]
        out[i:i + len(seg)] += seg * g * (0.85 + 0.15 * ((j * 7) % 3) / 2)
    return out * 0.35


@lru_cache(None)
def sub_swell(freq, seconds):
    n = ns(seconds)
    t = np.arange(n) / SR
    x = sine(freq * ramp(n, 0.85, 1.0, tau=seconds / 2), n) + 0.3 * triangle(freq * 2, n)
    return x * (t / seconds) ** 2


@lru_cache(None)
def impact():
    """A deep impact: a falling sine boom with a short noise body (no click: a 3 ms attack)."""
    n = ns(2.5)
    x = sine(ramp(n, 70, 32, tau=0.4), n) * exp_decay(n, 0.7)
    x += svf(noise(n, np.random.default_rng(4831)), 300, q=0.7) * exp_decay(n, 0.12) * 0.6
    x *= np.clip(np.arange(n) / ns(0.003), 0, 1)
    return fade(x, 0.0005, 0.2)


def sting_black_and_gold():
    s = Song(STING_BPM, 4, tail=2.0)
    # the rise (0-4 s): tremolo strings and an organ pedal swelling, a timpani roll, a riser
    s.chord("strings", v_tremolo, ["B1", "F#2", "B2"], 0, 0, dur_steps=32, gain=0.5)
    s.chord("organ", v_organ, ["B1", "B2"], 0, 0, dur_steps=40, gain=0.45)
    s.hit("perc", timp_roll("B1", 3.9), 0, 0, 0.9)
    s.hit("fx", riser(3.9), 0, 0, 0.3)
    # the hit (4.0 s): the motif stacked: B on the beat, D a dotted quarter later, F# an eighth after, A# on the bar
    for note, st, gain in (("B2", 0, 1.0), ("D3", 6, 0.9), ("F#3", 8, 0.9), ("A#3", 16, 1.0)):
        dur = 48 - st
        s.chord("brass", v_brass, [note, transpose(note, 12)], HIT_BAR, st, dur_steps=dur, gain=0.8 * gain, spread=0.3)
        s.hit("perc", timpani(transpose("B1", {"B2": 0, "D3": 3, "F#3": 7, "A#3": -1}[note]), 1.6), HIT_BAR, st,
              0.9 * gain)
    s.hit("perc", taiko(), HIT_BAR, 0, 1.0)
    s.hit("perc", crash(), HIT_BAR, 0, 0.5)
    s.chord("organ", v_organ, ["B1", "F#2", "B2", "D3", "F#3", "A#3"], HIT_BAR, 0, dur_steps=40, gain=0.5)
    s.chord("strings", v_strings_long, ["B1", "B2"], HIT_BAR, 0, dur_steps=20, gain=0.5, attack=0.02)
    s.hit("low", v_sub(note_freq("B1"), 4.5), HIT_BAR, 0, 0.4)
    mix = _sting_bus(s, {
        "strings": dict(hp=40, rev=(3.0, 0.4), wid=1.5, seed=4832, gain=0.65),
        "organ": dict(hp=35, rev=(4.0, 0.45), wid=1.3, seed=4833, gain=0.6),
        "brass": dict(hp=70, rev=(3.0, 0.35), seed=4834, gain=0.85),
        "perc": dict(hp=30, rev=(2.8, 0.3), damp=0.8, seed=4835, gain=0.85),
        "fx": dict(rev=(2.5, 0.3), seed=4836, gain=0.7),
        "low": dict(hp=25, gain=0.6),
    })
    # the water lifting: the rise starts muffled and opens over its last second (the grade lifts over 1 s)
    t = np.arange(mix.shape[1]) / SR
    cut = np.interp(t, [0.0, 3.0, 4.0, 20.0], [700.0, 1100.0, 12000.0, 12000.0])
    mix = np.vstack([svf(ch, cut, q=0.8) for ch in mix])
    return mix, 9.0


def sting_ours():
    s = Song(STING_BPM, 4, tail=2.0)
    # the signal (0-4 s): the whistle out of static on the motif, a sub and a low brass cluster rising under it
    s.play("signal", v_whistle, vorne_motif("B4"), 0, gain=0.6, position=0.2)
    s.hit("static", static_swell(4.4, 4841), 0, 0, 0.4, 0.2)
    s.hit("low", sub_swell(note_freq("B1"), 4.0), 0, 0, 0.8)
    s.chord("cluster", v_brass, ["B1", "C2", "F#2", "A#2"], 0, 0, dur_steps=32, gain=0.4, spread=0.5)
    s.hit("fx", riser(3.9), 0, 0, 0.25)
    # the hit (4.0 s): the impact, the motif's last two notes in full low brass and organ, the A# held
    s.hit("perc", impact(), HIT_BAR, 0, 1.0)
    s.hit("perc", taiko(), HIT_BAR, 0, 0.7)
    s.play("open", v_brass, ["F#3 - - - - - - - A#3 - - - - - - -", "- - - - - - - - - - - - - - - -",
                             "- - - - - - - - - - - - . . . ."], HIT_BAR, gain=0.7, legato=1.0)
    s.play("open", v_brass, ["F#2 - - - - - - - A#2 - - - - - - -", "- - - - - - - - - - - - - - - -",
                             "- - - - - - - - - - - - . . . ."], HIT_BAR, gain=0.6, legato=1.0)
    s.play("open", v_organ, ["F#3 - - - - - - - A#3 - - - - - - -", "- - - - - - - - - - - - - - - -",
                             "- - - - - - - - - - - - . . . ."], HIT_BAR, gain=0.6, legato=1.0)
    s.chord("drone", v_organ, ["B0", "B1", "F#2"], HIT_BAR, 0, dur_steps=44, gain=0.6)
    s.chord("drone", v_strings_long, ["B1", "D2", "F#2"], HIT_BAR, 0, dur_steps=40, gain=0.45, attack=0.3)
    mix = _sting_bus(s, {
        "signal": dict(rev=(3.0, 0.4), seed=4842, gain=0.6),
        "static": dict(gain=0.5),
        "low": dict(hp=25, gain=0.6),
        "cluster": dict(hp=40, lp=900.0, rev=(3.5, 0.45), seed=4843, gain=0.55),
        "fx": dict(rev=(2.5, 0.3), seed=4844, gain=0.6),
        "perc": dict(hp=25, rev=(3.0, 0.3), damp=0.8, seed=4845, gain=0.9),
        "open": dict(hp=60, rev=(3.5, 0.4), wid=1.3, seed=4846, gain=0.85),
        "drone": dict(hp=25, rev=(4.0, 0.45), wid=1.4, seed=4847, gain=0.55),
    })
    # the signal's radio band-pass (700 Hz - 2.6 kHz) before the hit, opening to full range across it
    sig = s.tl.group("signal") + s.tl.group("static")
    sig = reverb(sig, seconds=3.0, mix=0.4, seed=4842)[:, :s.tl.n]
    narrow = bandpass(sig, 700.0, 2600.0)
    t = np.arange(s.tl.n) / SR
    w = np.clip((t - 3.6) / 0.6, 0, 1)
    mix = mix - sig * 0.6 - s.tl.group("static") * 0.5 + (narrow * (1 - w) + sig * w) * 0.6
    return mix, 9.0


# ================================================================ main

SONGS = {"a": ("act4-b-theme-r48-a", pressure_line), "b": ("act4-b-theme-r48-b", black_current)}
STINGS = {"sting-a": ("reveal-sting-r48-a", sting_black_and_gold), "sting-b": ("reveal-sting-r48-b", sting_ours)}


def render_song(key, out):
    name, fn = SONGS[key]
    s, rest, motif, motif_bars = fn()
    mix = rest + motif
    # Vorne's motif against the whole mix in its 2-bar statements of the second loop pass (before mastering)
    base = s.sections()[1]
    gaps = []
    for bar in motif_bars:
        a, b = int(s.pos(base + bar) * SR), int(s.pos(base + bar + 2) * SR)
        gaps.append(db(np.sqrt(np.mean(mix[:, a:b] ** 2))) - db(np.sqrt(np.mean(motif[:, a:b] ** 2))))
    print(f"{name}: Vorne's motif " + ", ".join(f"{g:.1f}" for g in gaps) + " dB under the mix (bars "
          + ", ".join(str(b) for b in motif_bars) + ")", flush=True)
    mix = mix - mix.mean(axis=1, keepdims=True)
    # mastered before the assembly, with 1 s of the next loop pass appended (the render is periodic): the limiter's
    # gain at the loop's end then sees what follows it in the game, so it matches the gain at the loop start
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
    mix, length = fn()
    mix = mix[:, :int(length * SR)]
    mix = mix - mix.mean(axis=1, keepdims=True)
    mix = master(mix, -14.0, -2.0)
    mix = fade(mix, 0.003, 1.2)
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
