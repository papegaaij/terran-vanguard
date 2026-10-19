#!/usr/bin/env python3
"""Concept round 38 music (M7 part B, step AUD-B): track 8 "Red Dust Run" (Act 3 A: Levels 15-17 and 20) at
full length, and its provisional base stem, from the chosen round-02 sketch (music_r02.py `mars`,
mars-theme-r02-a: 135 BPM, E phrygian dominant, 53.8 s). Pending the user's review in round 38.

The sketch has only a 24-bar loop (A, the Andalusian-cadence B, the dust-storm breakdown and build), so
the full length is new composition on its material, in the method of music_r08.py's full versions: the
sketch's voices, grooves, chords and melodies, its mix chains and seeds, arranged into a 96-bar loop
(2:51) after its 4-bar intro, with two new melodies (the "caravan" D section and the bridge E):

  intro  4 bars: wind, drone, hand drums, the lonely reed's call (the sketch's intro)
  A      8  E F E Dm Am F Dm E      breakbeat, reese bass, the reed's A melody (the sketch's A)
  B      8  Am G F E x2             Andalusian cadence, 16th arpeggio, the reed's B melody (the sketch's B)
  D      8  E E F F E E Dm E        "caravan": half-time, the hand drums forward, a new low reed melody
  A2     8  as A                     the A melody an octave down, the arpeggio under it
  C      8  E E F F Dm Dm F F       dust storm (muffled drums, wind swell, the call) and build (the sketch's C)
  E      8  Dm Dm Am Am F G E E     bridge: a new high reed melody, full drums, the arpeggio
  B2     8  as B                     the B melody with the strings doubling it
  F      8  E E F F E E Dm E        desert: no kit, hand drums, wind and drone, the call alone
  G      8  as A                     build: the kit returns bar by bar, the arpeggio's filter opening
  B3     8  as B                     climax: the B melody, strings and arpeggio
  A3     8  as A                     the A melody with the strings an octave under it, the arpeggio
  T      8  Am G F E Dm F E E       turn: the call answered, a snare roll into the loop start (A)

Groups and chains are the sketch's (drum high-pass and storm filter, reverbs, delays, widening and gains,
same seeds); new: the "drone" (an E or A pedal in low strings, the sketch's intro chord held through the
sections) and the wind made loop-periodic (its last 0.5 s cross-faded into its first, tiled from the loop
start), so the seam is clean. The kicks go to their own "kick" group through the drums chain (every chain
is linear, so full = the sum of all groups exactly).

Base stem (the round-11 method of music_r11.py / music_r31.py; provisional, see below): the same render,
the base groups through the full mix's own chains, linked mastering (every loudness pass and the
encoder's peak correction measure the full mix and apply its gain to both), so both files share length,
LOOPSTART / LOOPLENGTH and gain and crossfade sample-aligned. Kept (the music README's guidance): the hand
drums and shaker, the reese bass, the wind and the drone, plus the string pads and the kicks; dropped: the
breakbeat top (snares, hats, toms), the crashes, the reed melody and the 16th arpeggio. The music README
wants the full mix reviewed before the stem; Level 15 plays the base stem from section 2, so this stem is
rendered now from the same pass (M7 part B's default, pending round 38) and is re-rendered if the review
changes the full mix.

Outputs (the --out directory, default design/audio/music/concept/):
  red-dust-run-full-r38-a.ogg   the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  red-dust-run-base-r38-a.ogg   the base stem
tools/art/themes.py (keys red-dust, red-dust-base) calls render() and writes the production files
assets/music/red-dust-run.ogg and red-dust-run-base.ogg with their review copies instead; the concept
directory gets no files from it.

Usage: python3 tools/concept/audio/music_r38.py [--out DIR]   (~3 min)
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import music_r11 as r11  # noqa: E402
from music import crash, hat, hp, kick, ns, snare, tom, v_arp, v_strings_long, widen  # noqa: E402
from music_r02 import (M_BASS, M_BREAK, M_CALL, M_CH, M_MEL_A, M_MEL_B, M_TRIBAL, LoopSong,  # noqa: E402
                       bass_line, doum, seam_ratio, shaker, tek, v_reed, v_reese, wind)
from synth import SR, db, decode, delay, lufs, note_freq, reverb, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
FULL_NAME, BASE_NAME = "red-dust-run-full-r38-a", "red-dust-run-base-r38-a"

A_PROG = ["E", "F", "E", "Dm", "Am", "F", "Dm", "E"]
B_PROG = ["Am", "G", "F", "E", "Am", "G", "F", "E"]
PLAN = [("A", A_PROG), ("B", B_PROG), ("D", ["E", "E", "F", "F", "E", "E", "Dm", "E"]), ("A2", A_PROG),
        ("C", ["E", "E", "F", "F", "Dm", "Dm", "F", "F"]), ("E", ["Dm", "Dm", "Am", "Am", "F", "G", "E", "E"]),
        ("B2", B_PROG), ("F", ["E", "E", "F", "F", "E", "E", "Dm", "E"]), ("G", A_PROG), ("B3", B_PROG),
        ("A3", A_PROG), ("T", ["Am", "G", "F", "E", "Dm", "F", "E", "E"])]
LOOP_BARS = sum(len(c) for _, c in PLAN)

# new melodies (E phrygian dominant: E F G# A B C D; the bridge borrows the sketch's G for its G chord)
D_MEL = [  # caravan: low and patient, the reed's lower fifth
    "E4 - - - - - F4 - G#4 - - - - - - -",
    "A4 - - - G#4 - F4 - E4 - - - - - - -",
    "F4 - - - - - G#4 - A4 - - - C5 - - -",
    "B4 - - - A4 - G#4 - A4 - - - - - - -",
    "G#4 - - - - - A4 - B4 - - - - - C5 -",
    "B4 - - - A4 - - - G#4 - F4 - E4 - - -",
    "D4 - - - F4 - - - A4 - - - G#4 - F4 -",
    "E4 - - - - - - - - - - - . . . .",
]
BR_MEL = [  # bridge: high, open, a turn of the A melody's sigh at its end
    "D5 - - - - - F5 - A5 - - - - - - -",
    "C6 - - - - - A5 - F5 - - - D5 - - -",
    "E5 - - - - - A5 - C6 - - - - - - -",
    "B5 - - - - - A5 - E5 - - - - - - -",
    "F5 - - - A5 - - - C6 - - - A5 - - -",
    "B5 - - - - - G5 - D5 - - - - - - -",
    "G#5 - - - - - F5 - E5 - - - - - - -",
    "E5 - - - - - - - - - - - . . . .",
]
T_CALL = M_CALL + [  # the call, then its answer falling home
    "C5 - - - - - B4 - A4 - - - G#4 - - -",
    "F4 - - - - - - - A4 - - - G#4 - F4 -",
    "E4 - - - - - - - - - - - - - - -",
    ". . . . . . . . . . . . . . . .",
]
HALF_KICK, HALF_SNARE = [0, 10], [8]          # the caravan's half-time kit


def validate():
    for name, bars in (("D_MEL", D_MEL), ("BR_MEL", BR_MEL), ("T_CALL", T_CALL)):
        for i, bar in enumerate(bars):
            if len(bar.split()) != 16:
                raise SystemExit(f"{name} bar {i}: {len(bar.split())} steps, not 16")


def looped_wind(s, gain_curve, seed=81):
    """The sketch's desert wind made loop-periodic: one loop length plus 0.5 s rendered, its last 0.5 s
    cross-faded into its first, tiled so a tile boundary falls on each loop pass's start."""
    l_n, i_n, m = s.loop * 16 * s.step_n, s.intro * 16 * s.step_n, ns(0.5)
    one = wind(l_n + m, seed)
    w = np.linspace(0, 1, m)
    one[:, :m] = one[:, :m] * w + one[:, l_n:] * (1 - w)
    one = one[:, :l_n]
    reps = int(np.ceil(s.tl.n / l_n)) + 2
    start = (-i_n) % l_n
    return np.tile(one, reps)[:, start:start + s.tl.n] * gain_curve


def section_bars(base):
    """[(kind, j, chord, bar)] of one loop pass from `base`."""
    out, off = [], 0
    for kind, chords in PLAN:
        for j, name in enumerate(chords):
            out.append((kind, j, name, base + off + j))
        off += len(chords)
    return out


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def red_dust():
    validate()
    s = LoopSong(135, 4, LOOP_BARS)
    at = starts()
    for bar in range(4):  # intro: the sketch's, unchanged
        for st, kind in M_TRIBAL:
            s.hit("tribal", doum() if kind == "d" else tek(), bar, st, 0.6 if kind == "d" else 0.4,
                  -0.2 if kind == "d" else 0.3)
    s.chord("pad", v_strings_long, ["E2", "B2", "E3"], 0, dur_steps=64, gain=0.45)
    s.play("lead", v_reed, M_CALL, 0, gain=0.5, position=0.1)
    for st in range(8, 16):
        s.hit("drums", snare(True), 3, st, 0.2 + 0.05 * (st - 8), 0.1)

    for base in s.sections():
        for kind, j, name, bar in section_bars(base):
            pad, root, arp = M_CH[name]
            storm = kind == "C" and j < 4
            build = kind == "C" and j >= 4
            desert = kind == "F"
            half = kind == "D"
            # the kit: full break, half time (D), none (F), returning bar by bar (G)
            kit = not desert and not (kind == "G" and j < 2)
            top = kit and not (kind == "G" and j < 4)
            if j == 0 and kind not in ("F", "D", "G"):
                s.hit("perc", crash(), bar, 0, 0.5, 0.3)
            if kind == "G" and j == 4:
                s.hit("perc", crash(), bar, 0, 0.45, -0.3)
            if half:
                for st in HALF_KICK:
                    s.hit("kick", kick(150), bar, st, 0.85 if st == 0 else 0.6)
                for st in HALF_SNARE:
                    s.hit("drums", snare(), bar, st, 0.55, 0.05)
                for st in range(0, 16, 4):
                    s.hit("drums", hat(st % 8 == 4), bar, st + 2, 0.14, 0.3)
            elif kit:
                for st in M_BREAK["kick"]:
                    s.hit("kick", kick(150), bar, st, 0.85 if st in (0, 10) else 0.6)
                if top:
                    for st in M_BREAK["snare"]:
                        s.hit("drums", snare(st in (7, 9, 15)), bar, st, 0.6 if st in (4, 12) else 0.3, 0.05)
                hats = top or (kind == "G" and j >= 2)
                if hats:
                    for st in range(0, 16, 2):
                        s.hit("drums", hat(st % 4 == 2), bar, st, 0.16 if st % 4 else 0.22, 0.3)
                if top and j % 4 == 3 and not storm and kind != "T":
                    for st, p in ((12, 180), (13, 150), (14, 120), (15, 95)):
                        s.hit("drums", tom(p), bar, st, 0.45, (st - 13.5) / 3)
            # hand drums and shaker everywhere; forward in the storm, the caravan and the desert
            fwd = 1.3 if (storm or half or desert) else 1.0
            for st, k in M_TRIBAL:
                s.hit("tribal", doum() if k == "d" else tek(), bar, st, (0.45 if k == "d" else 0.3) * fwd,
                      -0.2 if k == "d" else 0.3)
            for st in range(16):
                s.hit("tribal", shaker(), bar, st, (0.08 if st % 2 else 0.12) * (1.15 if half else 1.0), -0.4)
            # bass: the reese line, held notes in the storm, the caravan and the desert
            if storm or desert or half:
                s.hit("bass", v_reese(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.4 if not desert else 0.32)
            else:
                bass_line(s, "bass", v_reese, root, bar, M_BASS, 0.5)
            # pads and the drone (an E pedal; an A pedal under the cadence and the bridge's Am bars)
            s.chord("pad", v_strings_long, pad, bar, dur_steps=16,
                    gain=0.38 if kind in ("B", "B2", "B3") else 0.24 if desert else 0.3, spread=0.7)
            if j % 2 == 0:
                pedal = ["A1", "E2"] if (kind in ("B", "B2", "B3") or (kind == "E" and name == "Am")) else ["E2", "B2"]
                s.chord("drone", v_strings_long, pedal, bar, dur_steps=32, gain=0.4 if desert else 0.3, spread=0.3)
            # the 16th arpeggio
            if kind in ("B", "A2", "E", "B2", "B3", "A3", "T") or build or (kind == "G" and j >= 2):
                cutoff = 2800.0
                if kind == "G":
                    cutoff = 900.0 + 1900.0 * (j - 2) / 5
                for st in range(16):
                    s.hit("arp", v_arp(note_freq(arp[st % 8]), round(s.step * 0.7, 4), round(cutoff, -1)),
                          bar, st, 0.2, 0.35 if st % 2 else -0.35)
            # rolls into the next section
            if (build and j == 7) or (kind == "T" and j == 7) or (kind == "G" and j == 7):
                for st in range(16):
                    s.hit("drums", snare(True), bar, st, 0.2 + 0.03 * st, 0.05)
        # melodies
        b0 = base
        s.play("lead", v_reed, M_MEL_A, b0 + at["A"], gain=0.5, position=0.1)
        s.play("lead", v_reed, M_MEL_B, b0 + at["B"], gain=0.48, position=0.1)
        s.play("lead", v_reed, D_MEL, b0 + at["D"], gain=0.52, position=0.05)
        s.play("lead", v_reed, M_MEL_A, b0 + at["A2"], gain=0.5, position=-0.1, octave=-1)
        s.play("lead", v_reed, M_CALL, b0 + at["C"], gain=0.45, position=-0.1)
        s.play("lead", v_reed, BR_MEL, b0 + at["E"], gain=0.46, position=0.1)
        s.play("lead", v_reed, M_MEL_B, b0 + at["B2"], gain=0.48, position=0.1)
        s.play("strings_mel", v_strings_long, M_MEL_B, b0 + at["B2"], gain=0.3, octave=-1, attack=0.08)
        s.play("lead", v_reed, M_CALL, b0 + at["F"], gain=0.42, position=0.15)
        s.play("lead", v_reed, D_MEL[:4], b0 + at["F"] + 4, gain=0.4, position=-0.15)
        s.play("lead", v_reed, M_MEL_A[4:], b0 + at["G"] + 4, gain=0.48, position=0.1)
        s.play("lead", v_reed, M_MEL_B, b0 + at["B3"], gain=0.5, position=0.1)
        s.play("strings_mel", v_strings_long, M_MEL_B, b0 + at["B3"], gain=0.34, octave=-1, attack=0.08)
        s.play("lead", v_reed, M_MEL_A, b0 + at["A3"], gain=0.5, position=0.1)
        s.play("strings_mel", v_strings_long, M_MEL_A, b0 + at["A3"], gain=0.3, octave=-1, attack=0.08)
        s.play("lead", v_reed, T_CALL, b0 + at["T"], gain=0.46, position=-0.1)

    lb = LOOP_BARS
    # the storm filter on the kit (the sketch's curve, at C's bars) and the wind's swells (C and F)
    c0, f0 = at["C"], at["F"]
    drum_cut = [(-4, 900.0), (0, 12000.0), (c0, 12000.0), (c0 + 0.01, 700.0), (c0 + 4, 700.0),
                (c0 + 7.75, 12000.0), (lb, 12000.0)]
    air_curve = [(-4, 1.0), (0, 0.5), (c0, 0.5), (c0 + 0.01, 1.1), (c0 + 4, 1.1), (c0 + 5, 0.5),
                 (f0, 0.5), (f0 + 1, 1.0), (f0 + 7, 1.0), (f0 + 8, 0.5), (lb, 0.5)]
    return s, mix_groups(s, drum_cut, air_curve)


def mix_groups(s, drum_cut, air_curve):
    """Each group through its chain (the sketch's chains and seeds; new groups get their own), scaled by
    its mix gain: {group: stereo}. Every chain is linear, so the full mix is their sum."""
    tl = s.tl
    cut = s.curve(drum_cut)
    drum_chain = lambda x: reverb(np.vstack([svf(ch, cut, q=0.9) for ch in hp(x, 35)]),  # noqa: E731
                                  seconds=0.8, mix=0.1, seed=71)[:, :tl.n]
    pad_chain = lambda x, seed: widen(reverb(hp(x, 70), seconds=3.0, mix=0.4, seed=seed)[:, :tl.n], 1.5)  # noqa: E731
    lead = delay(tl.group("lead"), s.step * 6, feedback=0.3, mix=0.22, damp=3000, tail=0)[:, :tl.n]
    return {
        "drums": drum_chain(tl.group("drums")) * 0.85,
        "kick": drum_chain(tl.group("kick")) * 0.85,
        "tribal": reverb(tl.group("tribal"), seconds=1.4, mix=0.25, seed=72)[:, :tl.n] * 0.6,
        "bass": hp(tl.group("bass"), 38) * 0.7,
        "pad": pad_chain(tl.group("pad"), 73) * 0.5,
        "drone": widen(reverb(hp(tl.group("drone"), 35), seconds=3.0, mix=0.4, seed=75)[:, :tl.n], 1.2) * 0.45,
        "strings_mel": pad_chain(tl.group("strings_mel"), 76) * 0.4,
        "arp": delay(tl.group("arp"), s.step * 3, feedback=0.35, mix=0.3, damp=2500, tail=0)[:, :tl.n] * 0.45,
        "lead": reverb(lead, seconds=2.4, mix=0.28, seed=74)[:, :tl.n] * 0.58,
        "air": looped_wind(s, s.curve(air_curve)) * 0.35,
        "perc": tl.group("perc") * 0.6,
    }


# the base stem's groups (see the module doc)
BASE_GROUPS = ("tribal", "bass", "pad", "drone", "air", "kick")


def render():
    """One render; returns (song, full mix, base mix)."""
    s, groups = red_dust()
    full = sum(groups.values())
    base = sum(groups[g] for g in BASE_GROUPS)
    return s, full, base


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
    s, full, base = render()
    full, loop_start, loop_len = r11.prepare(s, full)
    base, _, _ = r11.prepare(s, base)
    full, base = r11.master_linked(full, base)
    for a in (full, base):
        a[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    path = out / f"{FULL_NAME}.ogg"
    for _ in range(4):  # the encoder's peak correction for the full mix, applied to both
        write_ogg(path, full, max_peak_db=None, tags=tags)
        over = db(np.abs(decode(path)).max()) + 1.0
        if over <= 0:
            break
        full, base = full * undb(-over - 0.1), base * undb(-over - 0.1)
    base_path = write_ogg(out / f"{BASE_NAME}.ogg", base, max_peak_db=None, tags=tags)
    for p, a in ((path, full), (base_path, base)):
        dec = decode(p)
        print(f"{p.name}: {a.shape[1] / SR:.1f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
              f"  (samples {loop_start}+{loop_len})  seam {seam_ratio(dec, loop_start, loop_len):.2f}"
              f"  peak {db(np.abs(dec).max()):.1f} dBFS  {lufs(a)[0]:.1f} LUFS", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
