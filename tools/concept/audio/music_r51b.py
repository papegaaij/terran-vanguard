#!/usr/bin/env python3
"""Concept round 51 music (M8 part B, step AUD-B): the two Act 4 themes at full length with their stems, each from
one render. Pending the user's review in round 51. Brief: design/audio/music/README.md, *Act 4 music*.

Track 10 "Thera Deep" (Act 4 A: Levels 22-24 and 26), from the chosen round-02 sketch (music_r02.py `europa`,
europa-theme-r02-a: 125 BPM, F minor, 58.1 s), in the method of music_r38.py / music_r41.py: the sketch's voices,
grooves, chords, melodies, mix chains and seeds, its 4-bar intro and an 88-bar loop (2:49) of eleven 8-bar sections
(A = Fm9 Fm9 Db Db Bbm9 Bbm9 Csus C, B = Db Eb Fm9 Fm9 Db Eb Csus C, the sketch's two progressions):

  intro 4 bars  pad, sonar, a far whale, the bass stirring from bar 2, a riser (the sketch's, without its bubbles)
  A    A  the groove: four-on-the-floor kick, off-beat hats, rolling 16th bass, the arpeggio (the sketch's A)
  B    B  + claps on 2 and 4, the melody on lead and low flute, the water opening 1800 -> 4800 Hz (the sketch's B)
  A2   A  the groove under the water again (2400 Hz), the flute's echo phrase twice
  C    A  the deep breakdown: no kit, held sub, the arpeggio low, two whale calls, the flute echo; the kit back
          for the build in bars 5-8 under a 4-bar riser (the sketch's C)
  D    B  surfacing: the melody again with the claps, the water opening to 4800 Hz
  E    Bbm9 Bbm9 Fm9 Fm9 Db Db Csus C  drift (new): the kick alone, a quiet held sub, a new low flute melody,
          one whale call, the water at 1300 Hz
  F    A  build: the kick, then the hats and bass (bar 3), then the claps (bar 5); the water opening 1300 -> 4800 Hz
  B2   B  climax: the melody with the claps, the water open
  A3   A  the groove, the flute's echo phrase an octave up
  C2   A  deep again: six bars without the kit, two whale calls, the flute echo, a riser into the kick's return
  T    A  turn: the hats out from bar 5, the water closing 2400 -> 1800 Hz into A

Changes to the sketch's sound: **no bubbles** (round 48 Q12 a: the sketch's `bubble` hits are left out; the sonar
pings and whale calls stay); the sonar ping is music_r48's `sonar_soft` (a 6 ms attack, not 0.7 ms, so it cannot
click); the arpeggio's pluck has its filter resonance lowered (q 1.4, the sketch 3.0) and a slower filter envelope
(80 ms, the sketch 50 ms), so it cannot read as a resonant bloop (the user's rule, as track 9's stab). The kicks go to
their own group through the drums chain; every chain is linear (the water's moving low-pass too), so a stem is the
exact sum of its groups through the same filter.

Stems (the round-11 method; one render, linked mastering, the same length, gain and LOOPSTART / LOOPLENGTH):
  base   kick, bass, pad, the sonar pings and the whale calls (Level 22 from section 2, Levels 23-24)
  full   everything: + hats, claps, crashes, arpeggio, lead and flute, risers (Level 22 from section 3)
  dark   the darkness stem (Level 26, played `alone` through the third lockstep stream from t=8-18): the sonar
         pings, the deep pad (the same notes through the same water, without the kick's ducking, so it does not
         pump with no kick) and a drone (an F1 + C2 pedal, a dark pad at 300 Hz, only in this stem)

Track 11 "Pressure Line" (Act 4 B: Level 25, Levels 27-28), from the round-48 sketch a picked in round 48
(music_r48.py `pressure_line`, act4-b-theme-r48-a: 175 BPM, F minor, 52.5 s), its 4-bar intro and a 128-bar loop
(2:56) of sixteen 8-bar sections (A = Fm9 Fm9 Dbmaj7 Dbmaj7 Bbm9 Bbm9 Csus C, B = Fm9 Fm9 Dbmaj7 Ab Bbm9 Bbm9
Csus C):

  intro 4 bars  the current, the pad, sonar, a far whale; the break filtered in from bar 3 (the sketch's)
  A    A  the two-step break, reese and sub, the lamp arpeggio (the sketch's A)
  B    B  + open hats, the slow flute line (the sketch's B)
  A2   A  the break with open hats, the flute line an octave down
  C    A  breakdown: drums out until bar 7, two whale calls, the current swelling, the motif twice (the sketch's C)
  D    B  full: the flute an octave up, the hats louder (the sketch's D)
  E    A  pressure (new): half time (kick on 1, snare on 3), the reese held, the arpeggio in quarters, a new low flute
  B2   B  the flute line
  D2   B  full, the flute line at pitch and an octave up
  F    A  the deep: no drums but a fill in bar 8, the current and two whale calls, the new melody high and far
  G    A  build: kick, then the snares (bar 3), then ghosts and hats (bar 5), open hats (bar 7), a riser
  D3   B  climax: the flute at pitch and an octave up, the water at its most open (3200 Hz, never fully)
  A3   A  the break, the new melody an octave up
  E2   A  pressure again, the new melody an octave down
  B3   B  the flute line
  D4   B  full
  T    A  turn: the hats out from bar 5, a ghost-snare roll in bar 8, the water closing into A

Changes to the sketch's sound: none (no bubble layer, the lamp arpeggio, soft attacks, as round 48 made it).

Stems (the method of music_r41.py, track 9):
  base   kick and the main snares, sub, pad, the whale calls, the sonar pings and the deep current
  full   the base plus the ghost snares, hats, crashes, reese, arpeggio, flute and risers
  motif  Vorne's motif alone (not part of the full mix; the game adds it as the third stream): track 9's intercepted
         whistle under water (F4 Ab4 C5 E5 through the radio band-pass, a 1.8 kHz low-pass and a long reverb, the
         sketch's chain), on each section's first bar (every 8 bars, about 11 s apart) and on bar 5 of the breakdowns
         (C, F) and the climax D3. Its level is set by measurement, as track 9's: MOTIF_UNDER dB under the full mix in
         the same 2-bar windows, MOTIF_UNDER_C in the breakdowns C and F.

Outputs (the --out directory, default design/audio/music/concept/):
  thera-deep-r51-a.ogg, thera-deep-base-r51-a.ogg, thera-deep-dark-r51-a.ogg
  pressure-line-r51-a.ogg, pressure-line-base-r51-a.ogg, pressure-line-motif-r51-a.ogg
      intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH, sample-aligned per track
  <file>-seam (e.g. thera-deep-seam-r51-a.ogg)   listening aids: the loop's last 8 s, then its first 8 s
  thera-deep-dark-join-r51-a.ogg   listening aid: the full mix, then a 2 s crossfade to the dark stem alone (as Level
                                   26 strips back at t=8-18), loop bars 1-16
  pressure-line-motif-mix-r51-a.ogg   listening aid: the full mix with the motif stem on top, loop bars 25-48 (C, D, E)
  thera-deep-sheet-r51-a.png, pressure-line-sheet-r51-a.png   review sheets: waveforms (intro, loop, tail, loop
                                   points), loudness, true peak, seams, sizes, a per-section loudness table
No SOURCE comment: concept files; tools/art/themes.py adds it when the game step makes them production files.

Usage: python3 tools/concept/audio/music_r51b.py [thera|pressure ...] [--out DIR]   (~5 min each)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seams and the stems'
alignment.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
from music import clap, crash, hat, hp, kick, ns, riser, sidechain, snare, transpose, v_bass, v_lead, v_pad  # noqa: E402
from music import widen  # noqa: E402
from music_r02 import E_CH, E_ECHO, E_MEL, LoopSong, seam_ratio, v_flute, v_reese, v_sub, whale  # noqa: E402
from music_r03 import mixdown, vorne_motif  # noqa: E402
from music_r36b import bandpass, lowpass, static_swell, v_whistle  # noqa: E402
from music_r41 import fade_ends, master_linked, segment_lufs, tag_values, waveform, WAVE_H, WAVE_W  # noqa: E402
from music_r48 import A_BASS, A_CH, A_FLUTE, A_GHOST, A_KICK, A_SNARE, current, ghost_snare, sonar_soft, v_lamp  # noqa: E402
from synth import (SR, adsr, db, decode, delay, exp_decay, lufs, note_freq, pulse, reverb, saw, svf,  # noqa: E402
                   undb, write_ogg)
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
R = "r51-a"
XF_STEPS = 2             # LOOPSTART on step 2 of bar 0 (an off-beat), not on a kick (music_r41's reason)
SEAM_SECONDS = 8.0
MOTIF_UNDER = 19.0       # track 11's motif, dB under the full mix (the brief: 18-20)
MOTIF_UNDER_C = 16.0     # in the breakdowns C and F (the sketch: 16)


def validate(named):
    for name, bars in named:
        for i, bar in enumerate(bars):
            if len(bar.split()) != 16:
                raise SystemExit(f"{name} bar {i}: {len(bar.split())} steps, not 16")


def section_bars(plan, base):
    """[(kind, j, chord, bar)] of one loop pass from `base`."""
    out, off = [], 0
    for kind, chords in plan:
        for j, name in enumerate(chords):
            out.append((kind, j, name, base + off + j))
        off += len(chords)
    return out


def starts(plan):
    out, off = {}, 0
    for kind, chords in plan:
        out[kind] = off
        off += len(chords)
    return out


def water_filter(stereo, cutoff, q):
    return np.vstack([svf(ch, cutoff, q=q) for ch in stereo])


# ================================================================ track 10: Thera Deep (125 BPM, F minor)

T_A = ["Fm9", "Fm9", "Db", "Db", "Bbm9", "Bbm9", "Csus", "C"]
T_B = ["Db", "Eb", "Fm9", "Fm9", "Db", "Eb", "Csus", "C"]
T_PLAN = [("A", T_A), ("B", T_B), ("A2", T_A), ("C", T_A), ("D", T_B),
          ("E", ["Bbm9", "Bbm9", "Fm9", "Fm9", "Db", "Db", "Csus", "C"]), ("F", T_A), ("B2", T_B), ("A3", T_A),
          ("C2", T_A), ("T", T_A)]
T_DRIFT = [  # E: the new low flute melody (F minor; E natural over the C chord)
    "F4 - - - - - - - C#5 - - - C5 - - -",
    "A#4 - - - - - - - - - - - . . . .",
    "G#4 - - - - - - - G4 - - - F4 - - -",
    "C5 - - - - - - - - - - - . . . .",
    "C#5 - - - - - - - F5 - - - D#5 - - -",
    "C5 - - - - - - - G#4 - - - - - - -",
    "G4 - - - - - - - F4 - - - - - - -",
    "E4 - - - - - - - - - - - . . . .",
]
T_BASE_UNDER = ("kick", "bass", "pad")
T_UNDER = ("kick", "drums", "bass", "pad", "arp", "lead", "perc")


@lru_cache(None)
def v_pluck_soft(freq, dur, cutoff=2000.0):
    """The sketch's pluck (music_r02.v_pluck) with less resonance and a slower filter envelope: a note, not a bloop."""
    n = ns(dur + 0.2)
    x = pulse(freq, n, 0.35) * 0.7 + saw(freq * 1.004, n) * 0.4
    x = svf(x, cutoff * (0.25 + exp_decay(n, 0.08)), q=1.4)
    return x * adsr(n, 0.001, 0.15, 0.2, 0.12, gate=dur)


def thera_deep():
    validate([("T_DRIFT", T_DRIFT)])
    loop_bars = sum(len(c) for _, c in T_PLAN)
    s = LoopSong(125, 4, loop_bars, xf_steps=XF_STEPS)
    at = starts(T_PLAN)
    kicks = []
    # intro: the sketch's (pad, sonar, a far whale, the bass stirring), without its bubbles
    for bar, name in enumerate(["Fm9", "Fm9", "Db", "Csus"]):
        pad, root, _ = E_CH[name]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.45, bright=1200.0)
        if bar >= 2:
            for st in range(16):
                s.hit("bass", v_bass(note_freq(transpose(root, 12 if st % 4 == 3 else 0)),
                                     round(s.step * 0.7, 4)), bar, st, 0.25 + 0.1 * (bar - 2))
    s.hit("ping", sonar_soft(), 0, 0, 0.5)
    s.hit("ping", sonar_soft(), 2, 0, 0.5)
    s.hit("fx", whale(180.0, 120.0, 2.8), 0, 8, 0.35, -0.5)
    s.hit("riser", riser(s.bar), 3, 0, 0.2)
    for bar in range(0, 4, 2):  # the dark stem's drone from the start
        s.chord("drone", v_pad, ["F1", "C2"], bar, dur_steps=32, gain=0.5, spread=0.3, bright=300.0)

    for base in s.sections():
        for kind, j, name, bar in section_bars(T_PLAN, base):
            pad, root, arp = E_CH[name]
            deep = (kind == "C" and j < 4) or (kind == "C2" and j < 6)
            drift = kind == "E"
            waking = kind == "F" and j < 2
            if not deep:
                for beat in range(4):
                    s.hit("kick", kick(120), bar, beat * 4, 0.85)
                    kicks.append(s.pos(bar, beat * 4))
            if not (deep or drift or waking or (kind == "T" and j >= 4)):
                for st in range(2, 16, 4):
                    s.hit("drums", hat(True), bar, st, 0.18, 0.3)
            if kind in ("B", "D", "B2") or (kind == "F" and j >= 4):
                for st in (4, 12):
                    s.hit("drums", clap(), bar, st, 0.35)
            if j == 0 and kind in ("A", "B", "D", "B2", "A3"):
                s.hit("perc", crash(), bar, 0, 0.3, 0.3)
            if deep or drift or waking:
                s.hit("bass", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.38 if drift else 0.6)
            else:
                for st in range(16):
                    if st % 4:
                        nt = transpose(root, 12 if st % 4 == 3 else 0)
                        s.hit("bass", v_bass(note_freq(nt), round(s.step * 0.7, 4)), bar, st, 0.45)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.45, bright=1600.0)
            arp_gain = 0.14 if (deep or drift) else 0.14 + 0.02 * j if kind == "F" else 0.26
            for st in range(16):
                s.hit("arp", v_pluck_soft(note_freq(arp[st % 8]), round(s.step * 0.6, 4), 2200.0),
                      bar, st, arp_gain, 0.4 if st % 2 else -0.4)
            if j % 2 == 0:
                s.hit("ping", sonar_soft(), bar, 0, 0.4)
                s.chord("drone", v_pad, ["F1", "C2"], bar, dur_steps=32, gain=0.5, spread=0.3, bright=300.0)
            if (kind == "C" and j == 3) or (kind == "C2" and j == 4):
                s.hit("riser", riser(s.bar * 4), bar, 0, 0.25 if kind == "C" else 0.22)
            if kind == "F" and j == 6:
                s.hit("riser", riser(s.bar * 2), bar, 0, 0.22)
        # melodies and whale calls
        s.play("lead", v_lead, E_MEL, base + at["B"], gain=0.4)
        s.play("lead", v_flute, E_MEL, base + at["B"], gain=0.25, octave=-1)
        s.play("lead", v_flute, E_ECHO, base + at["A2"], gain=0.35)
        s.play("lead", v_flute, E_ECHO, base + at["A2"] + 4, gain=0.3)
        s.play("lead", v_flute, E_ECHO, base + at["C"], gain=0.4)
        s.hit("fx", whale(160.0, 240.0, 3.2), base + at["C"], 4, 0.35, 0.5)
        s.hit("fx", whale(220.0, 130.0, 3.6), base + at["C"] + 2, 8, 0.3, -0.5)
        s.play("lead", v_lead, E_MEL, base + at["D"], gain=0.4)
        s.play("lead", v_flute, E_MEL, base + at["D"], gain=0.25, octave=-1)
        s.play("lead", v_flute, T_DRIFT, base + at["E"], gain=0.45)
        s.hit("fx", whale(140.0, 210.0, 3.6), base + at["E"] + 2, 8, 0.3, 0.4)
        s.play("lead", v_lead, E_MEL, base + at["B2"], gain=0.42)
        s.play("lead", v_flute, E_MEL, base + at["B2"], gain=0.3, octave=-1)
        s.play("lead", v_flute, E_ECHO, base + at["A3"], gain=0.28, octave=1)
        s.play("lead", v_flute, E_ECHO, base + at["C2"], gain=0.35)
        s.hit("fx", whale(180.0, 120.0, 3.0), base + at["C2"] + 1, 4, 0.32, -0.5)
        s.hit("fx", whale(150.0, 230.0, 3.4), base + at["C2"] + 4, 0, 0.3, 0.5)
        s.play("lead", v_flute, E_ECHO, base + at["T"], gain=0.3, octave=-1)

    lb = loop_bars
    a2, c0, d0, e0, f0, a3, c2, t0 = (at[k] for k in ("A2", "C", "D", "E", "F", "A3", "C2", "T"))
    muffle = s.curve([(-4, 700.0), (0, 1800.0), (8, 1800.0), (12, 4800.0), (a2 - 0.01, 4800.0), (a2, 2400.0),
                      (c0 - 0.01, 2400.0), (c0, 1300.0), (c0 + 4, 1300.0), (d0, 1800.0), (d0 + 4, 4800.0),
                      (e0 - 0.01, 4800.0), (e0, 1300.0), (f0, 1300.0), (f0 + 8, 4800.0), (a3 - 0.01, 4800.0),
                      (a3, 2400.0), (c2 - 0.01, 2400.0), (c2, 1300.0), (c2 + 6, 1300.0), (t0 - 0.01, 1800.0),
                      (t0, 2400.0), (t0 + 4, 2400.0), (lb, 1800.0)])
    tl = s.tl
    n = tl.n
    duck = sidechain(n, kicks, depth=0.5, release=0.15)

    def pad_chain(x):
        return widen(reverb(hp(x, 120), seconds=4.0, mix=0.45, seed=81)[:, :n], 1.6) * 0.55

    lead = delay(tl.group("lead"), s.step * 6, feedback=0.5, mix=0.4, damp=2500, tail=0)[:, :n]
    groups = {  # the sketch's chains and gains (music_r02.europa), each group alone
        "kick": hp(tl.group("kick"), 35) * 0.8,
        "drums": hp(tl.group("drums"), 35) * 0.8,
        "bass": hp(tl.group("bass"), 38) * duck * 0.75,
        "pad": pad_chain(tl.group("pad") * duck),
        "arp": delay(tl.group("arp"), s.step * 3, feedback=0.55, mix=0.45, damp=1800, tail=0)[:, :n] * 0.4,
        "lead": widen(reverb(hp(lead, 150), seconds=4.0, mix=0.4, seed=82)[:, :n], 1.5) * 0.7,
        "perc": tl.group("perc") * 0.5,
    }
    ping = delay(tl.group("ping"), s.step * 6, feedback=0.6, mix=0.5, damp=2500, tail=0)[:, :n]
    ping = reverb(ping, seconds=4.5, mix=0.6, seed=83)[:, :n] * 0.25
    fx = reverb(tl.group("fx"), seconds=4.0, mix=0.5, seed=84)[:, :n] * 0.4
    rise = reverb(tl.group("riser"), seconds=4.0, mix=0.5, seed=84)[:, :n] * 0.4
    drone = widen(reverb(hp(tl.group("drone"), 30), seconds=4.0, mix=0.4, seed=85)[:, :n], 1.2) * 0.5
    print("thera deep: rendered, filtering the stems through the water", flush=True)
    full = water_filter(sum(groups[g] for g in T_UNDER), muffle, 1.1) + ping + fx + rise
    base = water_filter(sum(groups[g] for g in T_BASE_UNDER), muffle, 1.1) + ping + fx
    dark = water_filter(pad_chain(tl.group("pad")), muffle, 1.1) + ping + drone
    return s, [("FULL MIX", f"thera-deep-{R}", full), ("BASE STEM", f"thera-deep-base-{R}", base),
               ("DARK STEM", f"thera-deep-dark-{R}", dark)]


# ================================================================ track 11: Pressure Line (175 BPM, F minor)

P_A = ["Fm9", "Fm9", "Dbmaj7", "Dbmaj7", "Bbm9", "Bbm9", "Csus", "C"]
P_B = ["Fm9", "Fm9", "Dbmaj7", "Ab", "Bbm9", "Bbm9", "Csus", "C"]
P_PLAN = [("A", P_A), ("B", P_B), ("A2", P_A), ("C", P_A), ("D", P_B), ("E", P_A), ("B2", P_B), ("D2", P_B),
          ("F", P_A), ("G", P_A), ("D3", P_B), ("A3", P_A), ("E2", P_A), ("B3", P_B), ("D4", P_B), ("T", P_A)]
P_LOW = [  # E, F, A3, E2: the new low flute melody over A
    "F4 - - - - - - - - - - - G#4 - - -",
    "G4 - - - - - - - F4 - - - - - - -",
    "C#4 - - - - - - - F4 - - - G#4 - - -",
    "C5 - - - - - - - - - - - . . . .",
    "C#5 - - - - - - - C5 - - - A#4 - - -",
    "F4 - - - - - - - - - - - . . . .",
    "G4 - - - - - - - F4 - - - - - - -",
    "E4 - - - - - - - - - - - . . . .",
]
P_HALF_KICK, P_HALF_SNARE, P_HALF_GHOST = [(0, 1.0), (11, 0.6)], [(8, 1.0)], [(14, 0.3)]
P_BREAKDOWNS = ("C", "F")
P_BASE_UNDER = ("kick", "sub", "pad", "fx")
P_UNDER = ("kick", "drums", "hats", "perc", "fx", "riser", "bass", "sub", "pad", "arp", "lead")
P_CHAINS = {  # music_r48.pressure_line's mixdown, per group
    "kick": dict(hp=35, gain=0.85), "drums": dict(hp=35, gain=0.85), "hats": dict(hp=500, gain=0.45),
    "perc": dict(hp=38, rev=(2.6, 0.25), damp=0.8, gain=0.5, seed=4811), "fx": dict(gain=0.5),
    "riser": dict(gain=0.5), "bass": dict(hp=40, duck=True, gain=0.5), "sub": dict(hp=25, duck=True, gain=0.55),
    "pad": dict(hp=110, duck=True, rev=(4.5, 0.45), wid=1.6, seed=4812, gain=0.5),
    "arp": dict(hp=180, dly=(3, 0.5, 0.4, 1800), rev=(3.0, 0.3), wid=1.3, seed=4813, gain=0.5),
    "lead": dict(hp=180, dly=(6, 0.45, 0.35, 2400), rev=(4.0, 0.4), wid=1.4, seed=4814, gain=0.6),
}


def p_motif_bars():
    """[(loop bar, in a breakdown)] of the motif statements."""
    at, out = starts(P_PLAN), []
    for kind, _ in P_PLAN:
        out.append((at[kind], kind in P_BREAKDOWNS))
        if kind in P_BREAKDOWNS + ("D3",):
            out.append((at[kind] + 4, kind in P_BREAKDOWNS))
    return out


def pressure_line():
    validate([("P_LOW", P_LOW)])
    loop_bars = sum(len(c) for _, c in P_PLAN)
    s = LoopSong(175, 4, loop_bars, xf_steps=XF_STEPS)
    at = starts(P_PLAN)
    kicks = []
    for bar, name in enumerate(["Fm9", "Fm9", "Dbmaj7", "Csus"]):  # intro: the sketch's
        pad, root, arp = A_CH[name]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.45, bright=1100.0)
        if bar >= 2:
            for st, g in A_KICK:
                s.hit("kick", kick(110), bar, st, 0.7 * g)
                kicks.append(s.pos(bar, st))
            for st, g in A_SNARE:
                s.hit("kick", snare(True), bar, st, 0.3 * g, 0.05)
        for st in range(0, 16, 2):
            s.hit("arp", v_lamp(note_freq(arp[st // 2]), round(s.step * 1.6, 4)), bar, st, 0.18 + 0.04 * bar,
                  0.35 if st % 4 else -0.35)
    s.hit("ping", sonar_soft(), 0, 0, 0.5)
    s.hit("ping", sonar_soft(), 2, 0, 0.5)
    s.hit("fx", whale(170.0, 120.0, 3.0), 0, 8, 0.3, -0.5)
    s.hit("riser", riser(s.bar * 2), 2, 0, 0.18)

    for base in s.sections():
        for kind, j, name, bar in section_bars(P_PLAN, base):
            pad, root, arp = A_CH[name]
            deep = (kind == "C" and j < 6) or kind == "F"
            half = kind in ("E", "E2")
            build = kind == "G"
            full = kind in ("D", "D2", "D3", "D4")
            if j == 0 and kind in ("A", "B", "D", "B2", "D2", "D3", "A3", "D4"):
                s.hit("perc", crash(), bar, 0, 0.25 if kind != "D3" else 0.3, 0.3)
            # the kit
            if half:
                for st, g in P_HALF_KICK:
                    s.hit("kick", kick(110), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                for st, g in P_HALF_SNARE:
                    s.hit("kick", snare(True), bar, st, 0.5 * g, 0.05)
                for st, g in P_HALF_GHOST:
                    s.hit("drums", ghost_snare(), bar, st, 0.5 * g, -0.1)
                for st in range(0, 16, 2):
                    s.hit("hats", hat(), bar, st, 0.08, 0.3)
            elif not deep:
                for st, g in A_KICK:
                    s.hit("kick", kick(110), bar, st, 0.95 * g)
                    kicks.append(s.pos(bar, st))
                if not (build and j < 2):
                    for st, g in A_SNARE:
                        s.hit("kick", snare(True), bar, st, 0.5 * g, 0.05)
                ghosts = not (build and j < 4) and not (kind == "T" and j == 7)
                if ghosts:
                    for st, g in A_GHOST:
                        s.hit("drums", ghost_snare(), bar, st, 0.5 * g, -0.1)
                if not (build and j < 4) and not (kind == "T" and j >= 4):
                    for st in range(16):
                        s.hit("hats", hat(), bar, st, (0.1 if st % 2 == 0 else 0.05) * (1.3 if full else 1.0), 0.3)
                opens = kind in ("B", "A2", "B2", "B3") or full or (build and j >= 6)
                if opens:
                    for st in (2, 6, 10, 14):
                        s.hit("hats", hat(True), bar, st, 0.1, -0.3)
            if (kind == "F" and j == 7) or (kind == "T" and j == 7):  # fills into the next section
                for st in range(8, 16):
                    s.hit("drums", ghost_snare(), bar, st, 0.3 + 0.06 * (st - 8), 0.05)
            # bass: the reese line; held in the half-time sections; none in the deep bars
            if half:
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 15, 4)), bar, 0, 0.3)
            elif not deep and not (build and j < 2):
                for st, semis, ln in A_BASS:
                    s.hit("bass", v_reese(note_freq(transpose(root, 12 + semis)), round(s.step * ln * 0.95, 4)),
                          bar, st, 0.4)
            s.hit("sub", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.55)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.5 if (deep or half) else 0.42, bright=1400.0)
            arp_steps = range(0, 16, 4) if half else range(0, 16, 2)
            for st in arp_steps:
                s.hit("arp", v_lamp(note_freq(arp[st // 2]), round(s.step * (3.2 if half else 1.6), 4)), bar, st,
                      0.2 if half else 0.16 if deep else 0.26, 0.35 if st % 4 else -0.35)
            if j % 2 == 0:
                s.hit("ping", sonar_soft(), bar, 0, 0.4)
            if (kind == "C" and j == 6) or (build and j == 6):
                s.hit("riser", riser(s.bar * 2), bar, 0, 0.22)
        # melodies and whale calls
        s.play("lead", v_flute, A_FLUTE, base + at["B"], gain=0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["A2"], gain=0.4, octave=-1)
        s.hit("fx", whale(150.0, 230.0, 3.4), base + at["C"] + 1, 4, 0.32, 0.5)
        s.hit("fx", whale(210.0, 125.0, 3.8), base + at["C"] + 4, 8, 0.28, -0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["D"], gain=0.5, octave=1)
        s.play("lead", v_flute, P_LOW, base + at["E"], gain=0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["B2"], gain=0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["D2"], gain=0.4)
        s.play("lead", v_flute, A_FLUTE, base + at["D2"], gain=0.4, octave=1)
        s.play("lead", v_flute, P_LOW, base + at["F"], gain=0.3, octave=1)
        s.hit("fx", whale(170.0, 120.0, 3.2), base + at["F"] + 1, 8, 0.3, -0.5)
        s.hit("fx", whale(140.0, 220.0, 3.6), base + at["F"] + 5, 0, 0.28, 0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["D3"], gain=0.42)
        s.play("lead", v_flute, A_FLUTE, base + at["D3"], gain=0.45, octave=1)
        s.play("lead", v_flute, P_LOW, base + at["A3"], gain=0.4, octave=1)
        s.play("lead", v_flute, P_LOW, base + at["E2"], gain=0.45, octave=-1)
        s.play("lead", v_flute, A_FLUTE, base + at["B3"], gain=0.5)
        s.play("lead", v_flute, A_FLUTE, base + at["D4"], gain=0.5, octave=1)
        # Vorne's motif: the intercepted whistle under water (the breakdowns' statements in their own groups)
        for lb, in_c in p_motif_bars():
            g = "motif_c" if in_c else "motif"
            s.play(g, v_whistle, vorne_motif("F4"), base + lb, gain=0.5, position=0.25)
            s.hit(g + "_static", static_swell(round(2 * s.bar, 4), 4801 + lb), base + lb, 0, 0.25, 0.25)

    lb = loop_bars
    w = {k: at[k] for k, _ in P_PLAN}
    water = s.curve([(-4, 600.0), (-1, 1300.0), (0, 1500.0), (w["B"], 1800.0), (w["A2"] - 0.01, 2600.0),
                     (w["A2"], 2000.0), (w["C"] - 0.01, 2000.0), (w["C"], 1100.0), (w["D"] - 1, 1400.0),
                     (w["D"], 2400.0), (w["E"] - 0.01, 3200.0), (w["E"], 1300.0), (w["B2"] - 0.01, 1300.0),
                     (w["B2"], 1800.0), (w["D2"] - 0.01, 2600.0), (w["D2"], 2800.0), (w["F"] - 0.01, 2800.0),
                     (w["F"], 1000.0), (w["G"], 1200.0), (w["D3"], 3000.0), (w["D3"] + 4, 3200.0),
                     (w["A3"] - 0.01, 3200.0), (w["A3"], 2200.0), (w["E2"] - 0.01, 2200.0), (w["E2"], 1300.0),
                     (w["B3"] - 0.01, 1300.0), (w["B3"], 2000.0), (w["D4"] - 0.01, 2600.0), (w["D4"], 3000.0),
                     (w["T"] - 0.01, 3000.0), (w["T"], 2400.0), (lb, 1500.0)])
    swell = [(-4, 1.0), (0, 0.6)]
    for k in ("C", "E", "F", "E2"):
        swell += [(w[k] - 0.5, 0.6), (w[k] + 0.01, 1.0), (w[k] + 7.5, 1.0), (w[k] + 8, 0.6)]
    swell.append((lb, 0.6))
    tl = s.tl
    n = tl.n
    groups = {g: mixdown(s, {g: spec}, kicks, depth=0.5) for g, spec in P_CHAINS.items()}
    ping = delay(tl.group("ping"), s.step * 6, feedback=0.55, mix=0.5, damp=2400, tail=0)[:, :n]
    ping = reverb(ping, seconds=4.5, mix=0.6, seed=4815)[:, :n] * 0.22
    deep_water = current(s, 160.0, 0.7, swell, 4817) * 0.05

    def motif(group):
        x = tl.group(group) + tl.group(group + "_static")
        return reverb(lowpass(bandpass(x, 700.0, 2600.0), 1800.0), seconds=4.5, mix=0.55, seed=4816)[:, :n]

    print("pressure line: rendered, filtering the stems through the water", flush=True)
    full = water_filter(sum(groups[g] for g in P_UNDER), water, 0.9) + ping + deep_water
    base = water_filter(sum(groups[g] for g in P_BASE_UNDER), water, 0.9) + ping + deep_water
    m, mc = motif("motif"), motif("motif_c")
    off = {}
    for g, x, in_c in (("motif", m, False), ("motif_c", mc, True)):
        diffs = []
        for lbar, c in p_motif_bars():
            if c != in_c:
                continue
            a = (s.intro + s.loop + lbar) * 16 * s.step_n
            b = a + 2 * 16 * s.step_n
            diffs.append(segment_lufs(x[:, a:b]) - segment_lufs(full[:, a:b]))
        off[g] = float(np.mean(diffs))
    motif_stem = m * undb(-MOTIF_UNDER - off["motif"]) + mc * undb(-MOTIF_UNDER_C - off["motif_c"])
    print(f"motif calibration: raw {off['motif']:+.1f} dB, {off['motif_c']:+.1f} dB (C, F) vs the full mix -> "
          f"{-MOTIF_UNDER:.0f} / {-MOTIF_UNDER_C:.0f} dB", flush=True)
    return s, [("FULL MIX", f"pressure-line-{R}", full), ("BASE STEM", f"pressure-line-base-{R}", base),
               ("MOTIF STEM", f"pressure-line-motif-{R}", motif_stem)]


# ================================================================ writing, aids, review

def write_set(out, s, stems):
    """r11 / r41: assemble, linked mastering, the encoder's peak correction for the full mix applied to all, write.
    Returns (paths, decoded, loop_start, loop_len)."""
    prepared = [r11.prepare(s, a) for _, _, a in stems]
    loop_start, loop_len = prepared[0][1], prepared[0][2]
    audios = master_linked([p[0] for p in prepared])
    for a in audios:
        a[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    path = out / f"{stems[0][1]}.ogg"
    for _ in range(4):
        write_ogg(path, audios[0], max_peak_db=None, tags=tags)
        over = db(np.abs(decode(path)).max()) + 1.0
        if over <= 0:
            break
        audios = [x * undb(-over - 0.1) for x in audios]
    paths = [path] + [write_ogg(out / f"{name}.ogg", a, max_peak_db=None, tags=tags)
                      for (_, name, _), a in zip(stems[1:], audios[1:])]
    return paths, [decode(p) for p in paths], loop_start, loop_len


def report(paths, dec, labels, loop_start, loop_len):
    full_lufs = lufs(dec[0])[0]
    rows = []
    for p, d, label in zip(paths, dec, labels):
        t = tag_values(p)
        aligned = (d.shape == dec[0].shape and int(t["LOOPSTART"]) == loop_start
                   and int(t["LOOPLENGTH"]) == loop_len)
        loud, tp = lufs(d)
        seam = seam_ratio(d, loop_start, loop_len)
        rel = "" if p == paths[0] else f" ({loud - full_lufs:+.1f} LU vs the full mix)"
        print(f"{p.name}: {d.shape[1] / SR:.2f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
              f"  (samples {loop_start}+{loop_len})  seam {seam:.2f}  peak {db(np.abs(d).max()):.1f} dBFS"
              f"  {loud:.1f} LUFS{rel}  true peak {tp:.1f} dBTP  {p.stat().st_size / 1e6:.2f} MB"
              f"  {'aligned' if aligned else 'NOT ALIGNED'}", flush=True)
        scale = max(1.0, round(0.8 / max(np.abs(d).max(), 1e-6))) if label == "MOTIF STEM" else 1.0
        head = f"{label}   CONCEPT/{p.name.upper()}"
        line = (f"{d.shape[1] / SR:.1f} S   INTRO {loop_start / SR:.2f} S + LOOP {loop_len / SR:.2f} S "
                f"(SAMPLES {loop_start} + {loop_len})   {loud:.1f} LUFS{rel.upper()}   TRUE PEAK {tp:.1f} DBTP   "
                f"SEAM {seam:.2f}   {p.stat().st_size / 1e6:.2f} MB")
        rows.append((head, line, d, loop_start, loop_start + loop_len, scale))
    return rows


def seam_aids(out, paths, dec, loop_start, loop_len):
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    for p, d in zip(paths, dec):
        seam = np.concatenate([d[:, end - n:end], d[:, loop_start:loop_start + n]], axis=1)
        aid = write_ogg(out / p.name.replace(f"-{R}", f"-seam-{R}"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)


def sections_table(s, plan, dec, i0, third):
    """[(section, bars, full LUFS, base LUFS, third)] over the decoded loop; `third` is ("dark", None) for the dark
    stem's LUFS or ("motif", statements) for the motif's mean level against the full mix in its 2-bar windows."""
    at, rows = starts(plan), []
    bar_n = 16 * s.step_n
    for kind, chords in plan:
        a, b = i0 + at[kind] * bar_n, i0 + (at[kind] + len(chords)) * bar_n
        if third[0] == "dark":
            x = segment_lufs(dec[2][:, a:b])
        else:
            stmts = [lb for lb, _ in third[1] if at[kind] <= lb < at[kind] + len(chords)]
            x = float(np.mean([segment_lufs(dec[2][:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n])
                               - segment_lufs(dec[0][:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n]) for lb in stmts]))
        rows.append((kind, f"{at[kind] + 1}-{at[kind] + len(chords)}", segment_lufs(dec[0][:, a:b]),
                     segment_lufs(dec[1][:, a:b]), x))
    return rows


def review(path, title, rows, table, third_head):
    block = 40 + WAVE_H + 26
    table_h = 30 + 12 * (len(table) + 2)
    img = raster.sheet(WAVE_W + 40, 40 + block * len(rows) + table_h + 20, title,
                       "ROUND 51, M8 PART B - MUSIC_R51B.PY - MEASURED ONLY")
    y = 40
    for head, line, audio, start, end, scale in rows:
        raster.draw_text(img, 20, y, head, raster.LABEL)
        raster.draw_text(img, 20, y + 12, line, raster.LABEL_DIM)
        waveform(img, 20, y + 30, audio, start, end, scale)
        y += block
    note = "   MOTIF WAVEFORM SHOWN x" + f"{rows[2][5]:.0f}" if rows[2][5] > 1 else ""
    raster.draw_text(img, 20, y - 14, "GREY: INTRO, PLAYED ONCE   BLUE: LOOP   DARK: FADE TAIL   "
                     "AMBER: LOOPSTART / LOOP END" + note, raster.LABEL_DIM)
    y += 10
    raster.draw_text(img, 20, y, "LOUDNESS PER LOOP SECTION (EBU R128, DECODED FILES)", raster.LABEL)
    y += 16
    raster.draw_text(img, 20, y, f"SECTION   BARS    FULL LUFS   BASE LUFS   BASE VS FULL   {third_head}",
                     raster.LABEL_DIM)
    for kind, bars, f, b, m in table:
        y += 12
        last = f"{m:>8.1f} LUFS" if third_head.startswith("DARK") else f"{m:>+8.1f} DB"
        raster.draw_text(img, 20, y, f"{kind:<9} {bars:<7} {f:>8.1f}    {b:>8.1f}    {b - f:>+8.1f} LU    {last}",
                         raster.LABEL)
    img.convert("RGB").save(path, optimize=True)
    return path


def make_thera(out):
    s, stems = thera_deep()
    paths, dec, ls, ll = write_set(out, s, stems)
    rows = report(paths, dec, [lab for lab, _, _ in stems], ls, ll)
    seam_aids(out, paths, dec, ls, ll)
    # the dark join: loop bars 1-16, the full mix crossfading over 2 s into the dark stem alone at bar 9
    bar_n = 16 * s.step_n
    a, b, x = ls, ls + 16 * bar_n, ls + 8 * bar_n
    k = int(2.0 * SR)
    w = np.clip((np.arange(b - a) - (x - a)) / k, 0, 1)
    join = dec[0][:, a:b] * (1 - w) + dec[2][:, a:b] * w
    aid = write_ogg(out / f"thera-deep-dark-join-{R}.ogg", fade_ends(join), max_peak_db=None)
    print(f"aid: {aid.name} ({(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, T_PLAN, dec, ls, ("dark", None))
    for kind, bars, f, bl, m in table:
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  dark {m:6.1f}", flush=True)
    sheet = review(out / f"thera-deep-sheet-{R}.png", "THERA DEEP - TRACK 10, ACT 4 A, AND ITS THREE STEMS (FULL "
                   "LENGTH)", rows, table, "DARK LUFS")
    print(f"review: {sheet.name}", flush=True)


def make_pressure(out):
    s, stems = pressure_line()
    paths, dec, ls, ll = write_set(out, s, stems)
    rows = report(paths, dec, [lab for lab, _, _ in stems], ls, ll)
    both = dec[0] + dec[2]
    loud, tp = lufs(both)
    print(f"full + motif summed (as the game adds them): {loud:.1f} LUFS, peak {db(np.abs(both).max()):.1f} dBFS, "
          f"true peak {tp:.1f} dBTP", flush=True)
    seam_aids(out, paths, dec, ls, ll)
    bar_n = 16 * s.step_n
    a, b = ls + 24 * bar_n, ls + 48 * bar_n
    aid = write_ogg(out / f"pressure-line-motif-mix-{R}.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars 25-48, {(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, P_PLAN, dec, ls, ("motif", p_motif_bars()))
    for kind, bars, f, bl, m in table:
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  motif {m:+.1f} dB",
              flush=True)
    sheet = review(out / f"pressure-line-sheet-{R}.png", "PRESSURE LINE - TRACK 11, ACT 4 B, AND ITS THREE STEMS "
                   "(FULL LENGTH)", rows, table, "MOTIF VS FULL")
    print(f"review: {sheet.name}", flush=True)


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or ("thera", "pressure"):
        {"thera": make_thera, "pressure": make_pressure}[key](out)


if __name__ == "__main__":
    main(sys.argv[1:])
