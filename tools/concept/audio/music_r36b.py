#!/usr/bin/env python3
"""Concept round 36 (M7 "Act 3 - Red Dust" design phase, step A-audio): two sketches for track 9, the
Act 3 B theme ("dusty mid-tempo techno, Vorne's motif as a faint layer"; Levels 18, 19 and the Level 21
approach). Same format as the round-02/03/08 themes: intro + loop + 2-bar fade tail, LOOPSTART /
LOOPLENGTH Vorbis comments (samples), sample-exact loop, -14 LUFS with a -2 dBFS sample-peak ceiling.

Outputs (design/audio/music/concept/; the chosen b of round 36's review stays there, the rejected a
goes to concept/rejected/ unless --out names another directory):
  act3-b-theme-r36-a.ogg   "Oxide Drift"   126 BPM, A minor (harmonic) / E phrygian dominant
      Track 8's world, slowed into techno: a muffled four-on-the-floor kick, a rolling 16th bass, a
      filtered pluck sequencer on the chord tones, Martian hand drums (track 8's doum and tek) and the
      lonely reed of "Red Dust Run" coming back as a call; a dust wind looped with the loop.
  act3-b-theme-r36-b.ogg   "Grey Signal"   125 BPM, D minor
      Dub techno: syncopated minor-ninth chord stabs through a dotted-8th ping-pong delay and a long
      reverb, a deep sub bass, rimshots and swung hats, the hand drums in the middle sections, the same
      looped dust wind.

Vorne's motif (1 - b3 - 5 - 7, dotted quarter, eighth, half | whole; see design/audio/music/README.md) is
the faint layer in both, in its own group ("motif") so the production stems can carry it separately
(levels with the motif muted, Level 21's approach):
  a: cold organ and low brass far back (1.4 kHz low-pass, 4.5 s reverb at 60 %): A3 C4 E4 G#4. A
     harmonic minor holds Vorne's minor-major-7th chord (A C E G#), and E phrygian dominant, track 8's
     Martian scale, is its fifth mode: the Ascendancy's motif hides inside the Martian scale; its G#
     sounds over the F chords that follow.
  b: an intercepted signal: a thin whistle voice through a radio band-pass (700 Hz - 2.6 kHz) with a
     swell of static under it (band noise only, no crackle): D4 F4 A4 C#5, the C# grinding against the
     C of the D minor-ninth chord.
Both enter in the loop's B section (bar 8) and come back in C and D; the intro and A section have no
motif, so the motif's entry can follow the level (Level 18 brings it in at the Ghost Drones' first sight).

Usage: python3 tools/concept/audio/music_r36b.py [a|b ...] [--out DIR]
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, peaks, the seam and
spectrograms.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import crash, hat, hp, kick, ns, riser, snare, transpose, v_arp, v_brass, v_pad  # noqa: E402
from music_r02 import (LoopSong, add_tail, bass_line, doum, rim, seam_ratio, shaker, tek, v_dbass,  # noqa: E402
                       v_pluck, v_reed, v_sub)
from music_r03 import mixdown, v_organ, vorne_motif  # noqa: E402
from synth import (SR, adsr, db, decode, exp_decay, lufs, master, noise, note_freq, reverb, saw,  # noqa: E402
                   sine, svf, triangle, write_ogg)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"


# ================================================================ shared parts

def dust_wind(s, gain_points, seed):
    """A desert wind made periodic in the loop: one loop length of circular noise (inverse FFT, a band
    round 300-1500 Hz) through a band-pass whose centre and level move with sinusoids completing whole
    cycles per loop, filtered over three loop lengths with the middle one kept, tiled so that a tile
    boundary falls on the loop start; the level follows `gain_points` (s.curve). Unlike music_r02's
    `wind`, it does not jump at the seam."""
    l_n = int(round(s.loop * 16 * s.step_n))
    i_n = int(round(s.intro * 16 * s.step_n))
    rng = np.random.default_rng(seed)
    t = np.arange(l_n) / SR
    base = SR / l_n
    freqs = np.fft.rfftfreq(l_n, 1 / SR)
    shape = np.exp(-((np.log(np.maximum(freqs, 1.0)) - np.log(700.0)) / 0.8) ** 2)
    chans = []
    for ch in range(2):
        src = np.fft.irfft(shape * np.exp(2j * np.pi * rng.random(len(freqs))), l_n)
        src /= np.std(src) + 1e-12
        centre = 700 + 300 * np.sin(2 * np.pi * 2 * base * t + ch) + 150 * np.sin(2 * np.pi * 5 * base * t + 1 + ch)
        band = svf(np.tile(src, 3), np.tile(centre, 3), q=2.2, mode="bp")[l_n:2 * l_n]
        level = 0.65 + 0.35 * np.sin(2 * np.pi * 3 * base * t + 2 + ch) ** 2
        chans.append(band * level)
    one = np.array(chans)
    reps = int(np.ceil(s.tl.n / l_n)) + 2
    start = (-i_n) % l_n
    wind = np.tile(one, reps)[:, start:start + s.tl.n]
    return wind * s.curve(gain_points)


@lru_cache(None)
def v_stab(freq, dur, cutoff=1400.0):
    """A dub-techno chord stab voice: two detuned saws through a resonant low-pass that snaps shut."""
    n = ns(dur + 0.3)
    x = saw(freq * 1.003, n) + saw(freq * 0.997, n, start=0.3)
    x = svf(x, cutoff * (0.35 + exp_decay(n, 0.05)), q=1.8)
    return x * adsr(n, 0.002, 0.12, 0.25, 0.15, gate=dur) * 0.6


@lru_cache(None)
def v_whistle(freq, dur):
    """A thin whistle voice for the intercepted signal: triangle and a little second harmonic, slow
    vibrato and a soft attack."""
    n = ns(dur + 0.4)
    t = np.arange(n) / SR
    vib = 1 + 0.004 * np.sin(2 * np.pi * 4.6 * t) * np.clip((t - 0.2) / 0.4, 0, 1)
    x = triangle(freq * vib, n) + 0.25 * sine(2 * freq * vib, n)
    return x * adsr(n, 0.12, 0.3, 0.8, 0.4, gate=dur)


@lru_cache(None)
def static_swell(seconds, seed):
    """Radio static without crackle: band noise (1.9 kHz) swelling in and out (a sine-squared arch)."""
    n = ns(seconds)
    t = np.arange(n) / SR
    x = svf(noise(n, np.random.default_rng(seed)), 1900, q=0.8, mode="bp")
    return x * np.sin(np.pi * t / seconds) ** 2


def bandpass(stereo, lo, hi):
    return np.vstack([svf(svf(ch, lo, mode="hp"), hi) for ch in stereo])


def lowpass(stereo, cutoff):
    return np.vstack([svf(svf(ch, cutoff), cutoff) for ch in stereo])


# ================================================================ a: Oxide Drift (126 BPM, A minor)

A_CH = {  # pad voicing, bass root, sequencer tones (intervals above the root, octave 3-4)
    "Am": (["A2", "E3", "A3", "C4"], "A1", [0, 3, 7, 12]),
    "F": (["F2", "C3", "F3", "A3"], "F1", [0, 4, 7, 12]),
    "Dm": (["D3", "F3", "A3", "D4"], "D2", [0, 3, 7, 12]),
    "E": (["E2", "B2", "E3", "G#3"], "E1", [0, 1, 4, 7]),     # phrygian dominant: the b2 (F) in the figure
    "G": (["D3", "G3", "B3", "D4"], "G1", [0, 4, 7, 12]),
}
A_LOOP = (["Am", "Am", "F", "F", "Dm", "Dm", "E", "E"]          # A: groove, sequencer
          + ["Am", "F", "Dm", "E", "Am", "F", "Dm", "E"]        # B: the reed's call, the motif enters
          + ["Am", "Am", "F", "E"]                              # C: dust breakdown (kick out), motif
          + ["F", "G", "Am", "Am", "Dm", "E", "Am", "E"])       # D: full, motif twice
A_FIG = [0, 2, 1, 3, 0, 2, 1, 2, 0, 2, 1, 3, 2, 1, 0, 3]       # 16th sequencer: indices into the tones
A_CALL = [
    "E5 - - - - - - - - - - - F5 - E5 -",
    "C5 - - - - - - - - - - - . . . .",
    "D5 - - - - - F5 - E5 - - - D5 - C5 -",
    "B4 - - - - - - - G#4 - - - - - - -",
    "A4 - - - - - - - - - - - C5 - E5 -",
    "F5 - - - - - - - E5 - - - D5 - - -",
    "F5 - - - E5 - D5 - C5 - - - B4 - - -",
    "G#4 - - - - - - - - - - - . . . .",
]
A_TRIBAL = [(0, "d"), (3, "t"), (6, "d"), (10, "t"), (11, "t"), (14, "d")]


def oxide():
    s = LoopSong(126, 4, len(A_LOOP))
    kicks = []
    # intro: wind, a drone, the sequencer opening, the kick muffled from bar 2
    s.chord("pad", v_pad, ["A1", "E2", "A2"], 0, dur_steps=64, gain=0.5, bright=600.0)
    for bar in range(4):
        _, _, tones = A_CH["Am"]
        notes = [transpose("A3", i) for i in tones]
        for st in range(16):
            s.hit("seq", v_pluck(note_freq(notes[A_FIG[st]]), round(s.step * 0.55, 4), 500.0 + 250 * bar),
                  bar, st, 0.25, 0.3 if st % 2 else -0.3)
        if bar >= 2:
            for st in (0, 4, 8, 12):
                s.hit("drums", kick(105), bar, st, 0.8)
                kicks.append(s.pos(bar, st))
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.25)

    for base in s.sections():
        for i, name in enumerate(A_LOOP):
            bar = base + i
            pad, root, tones = A_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 20 else "D"
            dust = sec == "C"
            if i in (0, 8, 20):
                s.hit("perc", crash(), bar, 0, 0.4, 0.3)
            if not dust or i == 19:
                for st in (0, 4, 8, 12):
                    s.hit("drums", kick(105), bar, st, 0.95 if not dust else 0.5)
                    kicks.append(s.pos(bar, st))
            if not dust:
                for st in (4, 12):
                    s.hit("drums", snare(True), bar, st, 0.32, 0.05)
                for st in (2, 6, 10, 14):
                    s.hit("hats", hat(True), bar, st, 0.24, 0.25)
                for st in range(16):
                    s.hit("hats", hat(), bar, st, 0.07 if st % 2 else 0.1, -0.25)
            for st in range(16):
                s.hit("tribal", shaker(), bar, st, 0.06 if st % 2 else 0.09, -0.4)
            if sec in ("B", "D", "C"):
                for st, kind in A_TRIBAL:
                    s.hit("tribal", doum() if kind == "d" else tek(), bar, st,
                          (0.4 if kind == "d" else 0.25) * (1.3 if dust else 1.0), -0.2 if kind == "d" else 0.3)
            if i == 19:
                for st in range(8, 16):
                    s.hit("drums", snare(True), bar, st, 0.15 + 0.04 * (st - 8), 0.05)
                s.hit("fx", riser(s.bar), bar, 0, 0.25)
            # rolling 16th bass: every 16th off the kick, the octave on the last one
            if not dust:
                for st in range(16):
                    if st % 4 == 0:
                        continue
                    nm = transpose(root, 12 if st == 15 else 0)
                    s.hit("bass", v_dbass(note_freq(nm), round(s.step * 0.7, 4)), bar, st,
                          0.34 if st % 4 == 2 else 0.26)
            else:
                s.hit("bass", v_sub(note_freq(transpose(root, 12)), round(s.step * 15, 4)), bar, 0, 0.45)
            # the sequencer: chord tones as a 16th figure, its filter opening through the loop
            cut = {"A": 900.0 + 120 * (i % 8), "B": 1500.0, "C": 650.0, "D": 2200.0}[sec]
            notes = [transpose(transpose(root, 24), k) for k in tones]
            for st in range(16):
                s.hit("seq", v_pluck(note_freq(notes[A_FIG[st]]), round(s.step * 0.55, 4), round(cut, -1)),
                      bar, st, 0.24, 0.3 if st % 2 else -0.3)
            if sec == "D" and i >= 22:
                for st in range(16):
                    s.hit("arp", v_arp(note_freq(transpose(notes[A_FIG[st]], 12)), round(s.step * 0.5, 4), 2600.0),
                          bar, st, 0.12, -0.35 if st % 2 else 0.35)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.3 if sec != "C" else 0.42, bright=900.0)
        s.play("lead", v_reed, A_CALL, base + 8, gain=0.42, position=0.1)
        s.play("lead", v_reed, A_CALL[4:], base + 24, gain=0.45, position=-0.1)
        # Vorne's motif, the faint layer: B (bars 8, 12), C (16), D (20, 24)
        for bar in (8, 12, 16, 20, 24):
            s.play("motif", v_organ, vorne_motif("A3"), base + bar, gain=0.5, octave=-1)
            s.play("motif", v_brass, vorne_motif("A3"), base + bar, gain=0.35, position=-0.2)

    tl = s.tl
    motif = tl.group("motif")
    if motif.any():
        motif = reverb(lowpass(hp(motif, 90), 1400.0), seconds=4.5, mix=0.6, seed=3671)[:, :tl.n]
    mix = mixdown(s, {
        "drums": dict(hp=35, gain=0.85),
        "hats": dict(hp=300, rev=(1.0, 0.15), seed=3672, gain=0.5),
        "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.6, seed=3673),
        "tribal": dict(hp=60, rev=(1.4, 0.25), seed=3674, gain=0.6),
        "fx": dict(gain=0.7),
        "bass": dict(hp=38, duck=True, gain=0.6),
        "seq": dict(hp=150, dly=(3, 0.35, 0.25, 2500), rev=(1.8, 0.2), seed=3675, gain=0.55),
        "arp": dict(dly=(3, 0.3, 0.25, 3000), gain=0.4),
        "pad": dict(hp=80, duck=True, rev=(3.0, 0.4), wid=1.5, seed=3676, gain=0.45),
        "lead": dict(hp=180, dly=(6, 0.3, 0.22, 3000), rev=(2.4, 0.28), seed=3677, gain=0.55),
    }, kicks, depth=0.45)
    air = dust_wind(s, [(-4, 1.0), (-2, 0.7), (0, 0.45), (16, 0.45), (16.01, 1.0), (20, 1.0), (20.5, 0.45),
                        (28, 0.45)], 3678)
    return s, mix + motif * 0.22 + air * 0.06


# ================================================================ b: Grey Signal (125 BPM, D minor)

B_CH = {  # stab voicing, sub root, pad voicing
    "Dm9": (["F3", "A3", "C4", "E4"], "D2", ["D3", "A3", "F4"]),
    "Bbmaj7": (["D3", "F3", "A3"], "A#1", ["A#2", "F3", "D4"]),
    "Gm9": (["A#2", "D3", "F3", "A3"], "G1", ["G2", "D3", "A#3"]),
    "A7": (["C#3", "E3", "G3", "A#3"], "A1", ["A2", "E3", "C#4"]),    # A7(b9): the B-flat on top
}
B_LOOP = (["Dm9", "Dm9", "Bbmaj7", "Bbmaj7", "Gm9", "Gm9", "A7", "A7"]    # A: stabs, sub, rim
          + ["Dm9", "Dm9", "Bbmaj7", "Bbmaj7", "Gm9", "Gm9", "A7", "A7"]  # B: hand drums, the signal
          + ["Dm9", "Dm9", "Bbmaj7", "A7"]                                # C: dust breakdown, stabs alone
          + ["Dm9", "Dm9", "Bbmaj7", "Bbmaj7", "Gm9", "Gm9", "A7", "A7"])  # D: full, open hats
B_STABS = [(3, 0.9), (10, 0.75), (14, 0.45)]                      # (step, gain): the dub syncopation
B_SUB = [(0, 0, 6), (7, 0, 2), (10, 0, 3), (14, 12, 2)]
B_TRIBAL = [(0, "d"), (4, "t"), (7, "t"), (8, "d"), (11, "t"), (14, "t")]


def grey_signal():
    s = LoopSong(125, 4, len(B_LOOP), swing=0.1)
    kicks = []
    for bar in range(4):  # intro: wind, the stabs closed, the sub; the kick from bar 2
        voicing, root, _ = B_CH["Dm9"]
        for st, g in B_STABS:
            s.chord("stab", v_stab, voicing, bar, st, dur_steps=1, gain=g * 0.8, spread=0.5,
                    cutoff=500.0 + 200 * bar)
        bass_line(s, "bass", v_sub, root, bar, B_SUB, 0.5)
        if bar >= 2:
            for st in (0, 4, 8, 12):
                s.hit("drums", kick(95), bar, st, 0.85)
                kicks.append(s.pos(bar, st))
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.22)

    for base in s.sections():
        for i, name in enumerate(B_LOOP):
            bar = base + i
            voicing, root, pad = B_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C" if i < 20 else "D"
            dust = sec == "C"
            if i in (0, 8, 20):
                s.hit("perc", crash(), bar, 0, 0.35, 0.3)
            if not dust:
                for st in (0, 4, 8, 12):
                    s.hit("drums", kick(95), bar, st, 0.95)
                    kicks.append(s.pos(bar, st))
                for st in (4, 12):
                    s.hit("drums", rim(), bar, st, 0.45, 0.15)
                s.hit("drums", rim(), bar, 7, 0.18, -0.2)
                for st in range(16):
                    s.hit("hats", hat(), bar, st, 0.1 if st % 2 else 0.06, 0.3)
                if sec == "D":
                    for st in (2, 6, 10, 14):
                        s.hit("hats", hat(True), bar, st, 0.18, -0.25)
            elif i == 19:
                for st in (8, 10, 12, 14, 15):
                    s.hit("drums", kick(95), bar, st, 0.4 + 0.08 * (st - 8))
                    kicks.append(s.pos(bar, st))
            if sec in ("B", "D") or (dust and i >= 18):
                for st, kind in B_TRIBAL:
                    s.hit("tribal", doum() if kind == "d" else tek(), bar, st,
                          0.38 if kind == "d" else 0.22, -0.25 if kind == "d" else 0.3)
            for st in range(16):
                s.hit("tribal", shaker(), bar, st, 0.05 if st % 2 else 0.08, -0.4)
            # chord stabs: the filter opening over the loop, wide open in D
            cut = {"A": 800.0 + 100 * (i % 8), "B": 1500.0, "C": 1100.0, "D": 2300.0}[sec]
            for st, g in B_STABS:
                s.chord("stab", v_stab, voicing, bar, st, dur_steps=1, gain=g, spread=0.5, cutoff=round(cut, -1))
            if not dust:
                bass_line(s, "bass", v_sub, root, bar, B_SUB, 0.55)
            else:
                s.hit("bass", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.45)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.22 if not dust else 0.35, bright=650.0)
        # the intercepted signal: Vorne's motif, faint, B (bars 8, 12), C (16), D (20, 24)
        for bar in (8, 12, 16, 20, 24):
            s.play("motif", v_whistle, vorne_motif("D4"), base + bar, gain=0.5, position=0.25)
            s.hit("motif_static", static_swell(round(2 * s.bar, 4), 3681 + bar), base + bar, 0, 0.3, 0.25)

    tl = s.tl
    motif = tl.group("motif") + tl.group("motif_static")
    if motif.any():
        motif = reverb(bandpass(motif, 700.0, 2600.0), seconds=3.5, mix=0.45, seed=3682)[:, :tl.n]
    mix = mixdown(s, {
        "drums": dict(hp=35, gain=0.85),
        "hats": dict(hp=400, gain=0.5),
        "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.55, seed=3683),
        "tribal": dict(hp=60, rev=(1.4, 0.25), seed=3684, gain=0.6),
        "fx": dict(gain=0.7),
        "bass": dict(hp=30, duck=True, gain=0.7),
        "stab": dict(hp=140, dly=(3, 0.5, 0.35, 2200), rev=(3.2, 0.35), wid=1.4, seed=3685, gain=0.75),
        "pad": dict(hp=90, duck=True, rev=(3.5, 0.45), wid=1.6, seed=3686, gain=0.4),
    }, kicks, depth=0.4)
    air = dust_wind(s, [(-4, 1.0), (-2, 0.7), (0, 0.4), (16, 0.4), (16.01, 1.0), (20, 1.0), (20.5, 0.4),
                        (28, 0.4)], 3687)
    return s, mix + motif * 0.45 + air * 0.06


# ================================================================ main

SONGS = {"a": ("act3-b-theme-r36-a", oxide), "b": ("act3-b-theme-r36-b", grey_signal)}
CHOSEN = "b"                     # the user's pick (round 36's review, 2026-10-13); a goes to rejected/


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    for key in argv or SONGS:
        name, fn = SONGS[key]
        s, mix = fn()
        mix = mix - mix.mean(axis=1, keepdims=True)
        audio, loop_start, loop_len = s.assemble(mix)
        audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
        audio = master(audio, -14.0, -2.0)
        audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))  # fade-in only: the end loops
        folder = out if key == CHOSEN or out != OUT else out / "rejected"
        folder.mkdir(parents=True, exist_ok=True)
        path = write_ogg(folder / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
        dec = decode(path)
        loud, tp = lufs(dec)
        print(f"{path.name}: {audio.shape[1] / SR:.1f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
              f"  (samples {loop_start}+{loop_len})  seam {seam_ratio(dec, loop_start, loop_len):.2f}"
              f"  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak {tp:.1f} dBTP", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
