#!/usr/bin/env python3
"""Concept round 75 music (M10 part C, step AUD-C): track 15 "Storm Wall" (the Act 6 B theme: Level 37 from the turn,
Levels 38 and 41 and the Level 42 approach) at full length, with its four stems from one render, from round 72's chosen
sketch (music_r72.py `storm_wall`, act6-b-theme-r72-a: 135 BPM, C minor, Vorne's motif on C E-flat G B). Brief:
design/audio/music/README.md, *Act 6 music* (track 15: "four stems: base, full and motif as track 13's (a's motif stem
is the brass and organ line lifted out of the full mix), and a `fall` stem (the choir and the low strings only, the
breakdown's material under the whole loop) for Level 37's falling city, as track 10's dark stem was for Level 26").

The sketch has a 28-bar loop (A the riff and the war drums, B the choir in, C a 4-bar breakdown, D all in), so the full
length is new arrangement on its material in the method of music_r74.py (track 14): the sketch's voices, riff, chords,
mix chains and seeds, laid out as its 4-bar intro + a 96-bar loop (2:50.7) of twelve 8-bar sections
(A = Cm Cm Ab Ab Fm Fm G G; C = Cm Ab Fm G Cm Ab Fm G; E = Ab Ab Fm Fm Cm Cm G G):

  intro 4 bars  the sketch's: the storm strings swelling, two far thunder rolls, Vorne's motif in low brass and organ,
          the deep kick, a taiko and the riff's chug from bar 3, a low tom fill and a riser in bar 4
  A    A  the war drums (the deep kick on 1 and the and-of-2 and 3, a huge half-time snare and clap on 3, taiko), the
          drop-tuned riff in half time and its sub, the storm strings, low tom fills every 4th bar
  B    A  A plus the 8th hats and the wordless choir in long minor chords
  A2   A  A again
  C    C  the breakdown, drawn out to 8 bars: the choir alone over the low strings and the storm wind (up), a thunder
          roll in bars 1 and 3; the deep kick on the one and the sub back in bars 5-8, a taiko build and a riser in bar 8
  D    A  all in: B plus the double kick under every other bar
  B2   A  B again
  E    E  drift (new): half time on the sketch's material (the deep kick on the one, a soft snare on 3, no hats, no
          riff), the sub held, the storm strings and the choir up, the low strings under them
  F    A  build (new): the war drums' kick alone, the snare from bar 3, the riff, sub and taiko from bar 5, a riser
          (bars 5-8) and a snare roll in bar 8
  D2   A  D again
  B3   A  B again
  C2   C  the breakdown again
  T    A  turn: A's groove, a longer tom fill in bar 8, back into A

Vorne's motif (in the open, the sketch's "every 8 bars"): cold low brass, the brass an octave down and the organ, at
the first bar of every section but the breakdowns, the drift and the build (A, B, A2, D, B2, D2, B3, T), at bar 5 of D
and D2 and at bar 5 of the build, where the riff comes back (over the build's lone kick it stood only 1.6 dB under the
mix): 11 statements a loop, never more than 20 bars apart. No bubbly or popping sounds and no resonant plucks (the
sketch had none); the thunder is music_r74.py's roll **without its crack** (the brief: "thunder and static only as band
noise", where the sketch used music_r03.thunder with its crack); the storm wind is the sketch's periodic current
(music_r48.current, same seed and band) under its own automation. Every group goes through the sketch's own chain (its
mixdown spec, same seeds, the kicks ducking one fixed envelope), all linear, so a stem is the exact sum of its groups.

Stems (the brief): one render, linked mastering (each loudness pass and the encoder's peak correction measure the full
mix and apply its gain to all four files; the limiter at CEILING), so the files share length, LOOPSTART / LOOPLENGTH
and gain and play sample-aligned:
  base   the storm strings, the deep kick and the taiko (the big drums' low half), the riff and its sub, the low
         strings, the storm wind and the thunder rolls
  full   the base plus the snare and clap, hats, toms, crashes, timpani and risers, and the choir
  motif  Vorne's motif in low brass and organ alone (lifted out of the full mix; the game adds it on top as a third
         stream): its statements MOTIF_UNDER (6) dB under the full mix (EBU R128 over the statement windows, as
         tracks 12 and 14; set once before mastering). Level 37 plays the track without it (the Choir has spoken).
  fall   the breakdown's material under the whole loop: the choir's chords and the low strings at the breakdown's
         gains on every bar (the intro's chords too), nothing else (not part of the full mix: Level 37 plays it alone
         from the fall, t=160, as Level 26 plays track 10's dark stem `alone`)

Outputs (the --out directory, default design/audio/music/concept/):
  storm-wall-r75-a.ogg                   the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  storm-wall-{base,motif,fall}-r75-a.ogg the stems (same length, loop and gain)
  storm-wall{,-base,-motif,-fall}-seam-r75-a.ogg   listening aids: the last 8 s of the loop, then the first 8 s after
                                         LOOPSTART (0.5 s fades at both ends)
  storm-wall-motif-mix-r75-a.ogg         listening aid: the full mix with the motif stem on top (as Levels 38, 41 and
                                         42 play them), loop bars 1-24 (A, B, A2)
  storm-wall-fall-join-r75-a.ogg         listening aid: loop bars 1-16, the full mix crossfading over 2 s into the fall
                                         stem alone at bar 9 (as Level 37 switches at the fall)
  storm-wall-sheet-r75-a.png             review sheet: the files' waveforms, loudness, true peak, seam, size, and a
                                         per-section loudness table
No SOURCE comment: these are concept files; tools/art/themes.py copies them into assets/music with it.

Usage: python3 tools/concept/audio/music_r75.py [--out DIR]   (~6 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seams and the stems'
alignment.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
from music import clap, crash, hat, ns, riser, snare, taiko, timpani, tom, transpose  # noqa: E402
from music import v_brass, v_strings_long  # noqa: E402
from music_r02 import LoopSong, seam_ratio, v_choir, v_dbass, v_sub  # noqa: E402
from music_r03 import mixdown, v_organ, v_tremolo, vorne_motif  # noqa: E402
from music_r41 import fade_ends, master_linked, segment_lufs, tag_values, waveform, window, WAVE_H, WAVE_W  # noqa: E402
from music_r48 import current, deep_kick  # noqa: E402
from music_r72 import SW_CH, SW_INTRO, SW_RIFF  # noqa: E402
from music_r74 import thunder_roll  # noqa: E402
from synth import SR, db, decode, lufs, note_freq, undb, write_ogg  # noqa: E402
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "storm-wall"
STEMS = ("base", "motif", "fall")
SHEET = f"{NAME}-sheet-r75-a.png"
SEAM_SECONDS = 8.0
MIX_AID_BARS = (0, 24)   # loop bars of the motif-mix aid
JOIN_AID_BARS = (0, 16)  # loop bars of the fall-join aid; the crossfade ends at bar 8
MOTIF_UNDER = 6.0        # dB, the motif stem's statements under the full mix (the brief: about 6, in the open)
CEILING = -3.0           # dBFS, the linked limiter's ceiling (tracks 12 and 14's; the true-peak limit is -1 dBTP)

A = ["Cm", "Cm", "Ab", "Ab", "Fm", "Fm", "G", "G"]
C = ["Cm", "Ab", "Fm", "G", "Cm", "Ab", "Fm", "G"]
E = ["Ab", "Ab", "Fm", "Fm", "Cm", "Cm", "G", "G"]
PLAN = [("A", A), ("B", A), ("A2", A), ("C", C), ("D", A), ("B2", A), ("E", E), ("F", A), ("D2", A), ("B3", A),
        ("C2", C), ("T", A)]
LOOP_BARS = sum(len(c) for _, c in PLAN)
GROOVE_CRASH = ("A", "B", "A2", "D", "B2", "D2", "B3", "T")  # a crash and timpani on the section's one (the sketch's)
CHOIR_IN = ("B", "D", "B2", "D2", "B3")
DOUBLE = ("D", "D2")
BREAK = ("C", "C2")
MOTIF_AT = {"A": (0,), "B": (0,), "A2": (0,), "D": (0, 4), "B2": (0,), "F": (4,), "D2": (0, 4), "B3": (0,), "T": (0,)}
# the sketch's mix chains (music_r72.storm_wall's mixdown spec), one per group; the stems split the sketch's groups
PERC_CHAIN = dict(hp=35, rev=(2.8, 0.3), damp=0.8, seed=7203, gain=0.75)
FX_CHAIN = dict(hp=40, rev=(3.5, 0.35), seed=7204, gain=0.75)
CHOIR_CHAIN = dict(hp=140, rev=(4.0, 0.45), wid=1.6, seed=7206, gain=0.6)
LOW_CHAIN = dict(hp=40, rev=(3.5, 0.4), wid=1.4, seed=7207, gain=0.55)
CHAINS = {
    "drums": dict(hp=30, gain=0.9),
    "snare": dict(hp=110, rev=(3.2, 0.4), damp=0.7, seed=7202, gain=0.7),
    "taiko": PERC_CHAIN, "perc": PERC_CHAIN,
    "hats": dict(hp=700, gain=0.35),
    "thunder": FX_CHAIN, "fx": FX_CHAIN,
    "bass": dict(hp=38, duck=True, gain=0.55),
    "sub": dict(hp=25, duck=True, gain=0.5),
    "trem": dict(hp=90, rev=(3.5, 0.4), wid=1.5, seed=7205, gain=0.5),
    "choir": CHOIR_CHAIN, "fchoir": CHOIR_CHAIN,
    "low": LOW_CHAIN, "flow": LOW_CHAIN,
    # the sketch's motif: reverb(hp(motif, 50), 2.8 s, 0.3, seed 7208) * 0.5
    "motif": dict(hp=50, rev=(2.8, 0.3), seed=7208, gain=0.5),
}
AIR_GAIN = 0.05  # the sketch's storm current, added after the mixdown
BASE_GROUPS = ("drums", "taiko", "bass", "sub", "trem", "low", "thunder", "air")
FULL_GROUPS = BASE_GROUPS + ("snare", "hats", "perc", "fx", "choir")
MOTIF_GROUPS = ("motif",)
FALL_GROUPS = ("fchoir", "flow")


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def motif_bars():
    """Loop bars where a statement of Vorne's motif starts (for the calibration and the level table)."""
    at = starts()
    return sorted(at[k] + b for k, bars in MOTIF_AT.items() for b in bars)


def war_drums(s, kicks, bar, gain=1.0, snare_on=True, taiko_on=True):
    """The sketch's war drums: the deep kick on 1, the and-of-2 and 3, the half-time snare and clap on 3, taiko."""
    for st, g in ((0, 1.0), (6, 0.6), (10, 0.75)):
        s.hit("drums", deep_kick(), bar, st, 0.95 * g * gain)
        kicks.append(s.pos(bar, st))
    if snare_on:
        s.hit("snare", snare(), bar, 8, 0.75 * gain, 0.05)
        s.hit("snare", clap(), bar, 8, 0.35 * gain)
    if taiko_on:
        for st in (0, 8, 11):
            s.hit("taiko", taiko(), bar, st, (0.55 if st != 11 else 0.35) * gain, -0.2 if st else 0.2)


def riff(s, root, bar, gain=0.38):
    for st, semis, ln in SW_RIFF:
        s.hit("bass", v_dbass(note_freq(transpose(root, semis)), round(s.step * ln * 0.85, 4)), bar, st, gain)


def fall_bar(s, voicing, bar):
    """The fall stem: the breakdown's choir and low strings at the breakdown's gains."""
    s.chord("fchoir", v_choir, voicing, bar, dur_steps=16, gain=0.6, spread=0.8)
    s.chord("flow", v_strings_long, [transpose(voicing[0], -12), voicing[0]], bar, dur_steps=16, gain=0.5,
            attack=0.4)


def motif(s, bar):
    """Vorne's motif as the sketch states it: cold low brass, the brass an octave down, the organ."""
    s.play("motif", v_brass, vorne_motif("C3"), bar, gain=0.6, position=-0.1)
    s.play("motif", v_brass, vorne_motif("C3"), bar, gain=0.35, position=0.15, octave=-1)
    s.play("motif", v_organ, vorne_motif("C3"), bar, gain=0.4, octave=-1)


def storm_wall():
    s = LoopSong(135, len(SW_INTRO), LOOP_BARS)
    kicks = []
    # intro: the sketch's (the thunder without its crack)
    for bar, name in enumerate(SW_INTRO):
        voicing, root, sub = SW_CH[name]
        s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.3 + 0.08 * bar, spread=0.7)
        fall_bar(s, voicing, bar)
        if bar >= 2:
            s.hit("drums", deep_kick(), bar, 0, 0.9)
            kicks.append(s.pos(bar, 0))
            s.hit("taiko", taiko(), bar, 8, 0.6)
            for st, semis, ln in SW_RIFF[:3]:
                s.hit("bass", v_dbass(note_freq(transpose(root, semis)), round(s.step * ln * 0.85, 4)), bar, st,
                      0.3)
    s.hit("thunder", thunder_roll(721), 0, 0, 0.55, -0.3)
    s.hit("thunder", thunder_roll(722), 2, 6, 0.45, 0.35)
    s.play("motif", v_brass, vorne_motif("C3"), 0, gain=0.55, position=-0.1)
    s.play("motif", v_organ, vorne_motif("C3"), 0, gain=0.4, octave=-1)
    for st in range(8, 16):
        s.hit("perc", tom(150 - 8 * (st - 8)), 3, st, 0.3 + 0.06 * (st - 8), (st - 11.5) / 5)
    s.hit("fx", riser(s.bar), 3, 0, 0.2)

    at = starts()
    for base in s.sections():
        for kind, chords in PLAN:
            for j, name in enumerate(chords):
                bar = base + at[kind] + j
                voicing, root, sub = SW_CH[name]
                fall_bar(s, voicing, bar)
                if j == 0 and kind in GROOVE_CRASH:
                    s.hit("perc", crash(), bar, 0, 0.45, 0.3)
                    s.hit("perc", timpani("C2" if name == "Cm" else "G1"), bar, 0, 0.8)
                if kind in BREAK:
                    # the breakdown: the choir alone over low strings and the wind; the kick and sub back in bars 5-8
                    s.chord("choir", v_choir, voicing, bar, dur_steps=16, gain=0.6, spread=0.8)
                    s.chord("low", v_strings_long, [transpose(voicing[0], -12), voicing[0]], bar, dur_steps=16,
                            gain=0.5, attack=0.4)
                    if j in (0, 2):
                        s.hit("thunder", thunder_roll(730 + j + (10 if kind == "C2" else 0)), bar, 2, 0.5,
                              0.4 if j == 0 else -0.4)
                    if j >= 4:
                        s.hit("drums", deep_kick(), bar, 0, 0.7)
                        kicks.append(s.pos(bar, 0))
                        s.hit("sub", v_sub(note_freq(sub), round(s.step * 15, 4)), bar, 0, 0.35)
                    if j == 7:
                        s.hit("fx", riser(s.bar), bar, 0, 0.25)
                        for st in range(8, 16):
                            s.hit("taiko", taiko(), bar, st, 0.25 + 0.06 * (st - 8))
                    continue
                if kind == "E":
                    # the drift: half time, no riff, the sub held, the strings and the choir up
                    s.hit("drums", deep_kick(), bar, 0, 0.8)
                    kicks.append(s.pos(bar, 0))
                    s.hit("snare", snare(), bar, 8, 0.4, 0.05)
                    s.hit("taiko", taiko(), bar, 0, 0.45)
                    s.hit("sub", v_sub(note_freq(sub), round(s.step * 15, 4)), bar, 0, 0.45)
                    s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.5, spread=0.7)
                    s.chord("choir", v_choir, voicing, bar, dur_steps=16, gain=0.5, spread=0.8)
                    s.chord("low", v_strings_long, [transpose(voicing[0], -12), voicing[0]], bar, dur_steps=16,
                            gain=0.35, attack=0.4)
                    continue
                if kind == "F":
                    # the build: the kick alone, the snare from bar 3, the riff, sub and taiko from bar 5
                    war_drums(s, kicks, bar, snare_on=j >= 2, taiko_on=j >= 4)
                    if j >= 4:
                        riff(s, root, bar)
                        s.hit("sub", v_sub(note_freq(sub), round(s.step * 15, 4)), bar, 0, 0.45)
                    if j == 4:
                        s.hit("fx", riser(s.bar * 4), bar, 0, 0.3)
                    if j == 7:
                        for st in range(16):
                            s.hit("snare", snare(True), bar, st, 0.2 + 0.03 * st, 0.05)
                    s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.32, spread=0.7)
                    continue
                # the groove sections (A, B, D, T and their repeats): the sketch's A, B and D
                war_drums(s, kicks, bar)
                if kind in CHOIR_IN:
                    for st in range(0, 16, 2):
                        s.hit("hats", hat(st % 4 == 2), bar, st, 0.14, 0.3)
                if j % 4 == 3:  # low tom fills into the next phrase (the turn's last bar a longer one)
                    fill = ((8, 170), (9, 160), (10, 150), (11, 140)) if kind == "T" and j == 7 else ()
                    for st, p in fill + ((12, 140), (13, 120), (14, 100), (15, 85)):
                        s.hit("perc", tom(p), bar, st, 0.5, (st - 13.5) / 3)
                if kind in DOUBLE and j % 2 == 1:  # the double kick under the climax
                    for st in range(12, 16):
                        s.hit("drums", deep_kick(), bar, st, 0.45)
                        kicks.append(s.pos(bar, st))
                riff(s, root, bar)
                s.hit("sub", v_sub(note_freq(sub), round(s.step * 15, 4)), bar, 0, 0.45)
                s.chord("trem", v_tremolo, voicing, bar, dur_steps=16, gain=0.32, spread=0.7)
                if kind in CHOIR_IN:
                    s.chord("choir", v_choir, voicing, bar, dur_steps=16, gain=0.42, spread=0.8)
        # Vorne's motif in the open
        for lb in motif_bars():
            motif(s, base + lb)
    # the sketch's storm current under its automation: up in the intro, the breakdowns' calm bars and the drift
    points = [(-4, 1.0), (0, 0.45)]
    for kind, a in sorted(at.items(), key=lambda kv: kv[1]):
        if kind in BREAK:
            points += [(a, 0.45), (a + 0.01, 1.1), (a + 4, 1.1), (a + 6, 0.45)]
        elif kind == "E":
            points += [(a, 0.45), (a + 2, 0.8), (a + 6, 0.8), (a + 8, 0.45)]
    points.append((LOOP_BARS, 0.45))
    storm = current(s, 700.0, 0.8, points, 7201)
    groups = {g: mixdown(s, {g: spec}, kicks, depth=0.4) for g, spec in CHAINS.items()}
    groups["air"] = storm * AIR_GAIN
    return s, groups


def render():
    """One render; returns (song, [full mix, base, motif, fall]) before mastering."""
    s, groups = storm_wall()
    full = sum(groups[g] for g in FULL_GROUPS)
    base = sum(groups[g] for g in BASE_GROUPS)
    mot = sum(groups[g] for g in MOTIF_GROUPS)
    fall = sum(groups[g] for g in FALL_GROUPS)
    spans = [window(s, lb) for lb in motif_bars()]
    diffs = [segment_lufs(mot[:, a:b]) - segment_lufs(full[:, a:b]) for a, b in spans]
    offset = float(np.mean(diffs))
    mot = mot * undb(-MOTIF_UNDER - offset)
    print(f"motif calibration: raw {offset:+.1f} dB vs the full mix -> {-MOTIF_UNDER:.0f} dB", flush=True)
    return s, [full, base, mot, fall]


def sections_table(s, dec, i0):
    """[(section, bars, full, base, fall LUFS, motif vs full dB in its statement windows)] over the decoded loop."""
    at, rows = starts(), []
    full, base, mot, fall = dec
    bar_n = 16 * s.step_n
    for kind, chords in PLAN:
        a, b = i0 + at[kind] * bar_n, i0 + (at[kind] + len(chords)) * bar_n
        stmts = [lb for lb in motif_bars() if at[kind] <= lb < at[kind] + len(chords)]
        under = [segment_lufs(mot[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n])
                 - segment_lufs(full[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n]) for lb in stmts]
        rows.append((kind, f"{at[kind] + 1}-{at[kind] + len(chords)}", segment_lufs(full[:, a:b]),
                     segment_lufs(base[:, a:b]), segment_lufs(fall[:, a:b]),
                     float(np.mean(under)) if under else None))
    return rows


def review(out, rows, table, motif_peak):
    block = 40 + WAVE_H + 26
    table_h = 30 + 12 * (len(table) + 2)
    img = raster.sheet(WAVE_W + 40, 40 + block * len(rows) + table_h + 20,
                       "STORM WALL - TRACK 15, ACT 6 B, AND ITS FOUR STEMS (FULL LENGTH)",
                       "ROUND 75, M10 PART C - MUSIC_R75.PY - MEASURED ONLY")
    y = 40
    for head, line, audio, start, end, scale in rows:
        raster.draw_text(img, 20, y, head, raster.LABEL)
        raster.draw_text(img, 20, y + 12, line, raster.LABEL_DIM)
        waveform(img, 20, y + 30, audio, start, end, scale)
        y += block
    raster.draw_text(img, 20, y - 14, "GREY: INTRO, PLAYED ONCE   BLUE: LOOP   DARK: FADE TAIL   "
                     f"AMBER: LOOPSTART / LOOP END   MOTIF WAVEFORM SHOWN x{motif_peak:.0f}", raster.LABEL_DIM)
    y += 10
    raster.draw_text(img, 20, y, "LOUDNESS PER LOOP SECTION (EBU R128, DECODED FILES)", raster.LABEL)
    y += 16
    raster.draw_text(img, 20, y, "SECTION   BARS    FULL LUFS   BASE VS FULL   FALL VS FULL   MOTIF VS FULL",
                     raster.LABEL_DIM)
    for kind, bars, f, b, fl, m in table:
        y += 12
        mtxt = f"{m:>+8.1f} DB" if m is not None else "       -"
        raster.draw_text(img, 20, y, f"{kind:<9} {bars:<7} {f:>8.1f}    {b - f:>+8.1f} LU    {fl - f:>+8.1f} LU    "
                         f"{mtxt}", raster.LABEL)
    path = out / SHEET
    img.convert("RGB").save(path, optimize=True)
    return path


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
    out.mkdir(parents=True, exist_ok=True)
    s, files = render()
    prepared = []
    for x in files:
        y, loop_start, loop_len = r11.prepare(s, x)
        prepared.append(y)
    files = master_linked(prepared, ceiling_db=CEILING)
    for a in files:
        a[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    path = out / f"{NAME}-r75-a.ogg"
    for _ in range(4):  # the encoder's peak correction for the full mix, applied to all four
        write_ogg(path, files[0], max_peak_db=None, tags=tags)
        over = db(np.abs(decode(path)).max()) + 1.0
        if over <= 0:
            break
        files = [x * undb(-over - 0.1) for x in files]
    paths = [path] + [write_ogg(out / f"{NAME}-{stem}-r75-a.ogg", x, max_peak_db=None, tags=tags)
                      for stem, x in zip(STEMS, files[1:])]
    dec = [decode(p) for p in paths]
    full_lufs = lufs(dec[0])[0]
    rows = []
    motif_peak = 1.0
    for p, d, label in zip(paths, dec, ("FULL MIX", "BASE STEM", "MOTIF STEM", "FALL STEM")):
        t = tag_values(p)
        aligned = (d.shape == dec[0].shape and int(t["LOOPSTART"]) == loop_start
                   and int(t["LOOPLENGTH"]) == loop_len)
        loud, tp = lufs(d)
        seam = seam_ratio(d, loop_start, loop_len)
        rel = "" if p == path else f" ({loud - full_lufs:+.1f} LU vs the full mix)"
        print(f"{p.name}: {d.shape[1] / SR:.2f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
              f"  (samples {loop_start}+{loop_len})  seam {seam:.2f}  peak {db(np.abs(d).max()):.1f} dBFS"
              f"  {loud:.1f} LUFS{rel}  true peak {tp:.1f} dBTP  {p.stat().st_size / 1e6:.2f} MB"
              f"  {'aligned' if aligned else 'NOT ALIGNED'}", flush=True)
        if label == "MOTIF STEM":
            motif_peak = max(1.0, round(0.8 / max(np.abs(d).max(), 1e-6)))
        scale = motif_peak if label == "MOTIF STEM" else 1.0
        head = f"{label}   CONCEPT/{p.name.upper()}"
        line = (f"{d.shape[1] / SR:.1f} S   INTRO {loop_start / SR:.2f} S + LOOP {loop_len / SR:.2f} S "
                f"(SAMPLES {loop_start} + {loop_len})   {loud:.1f} LUFS{rel.upper()}   TRUE PEAK {tp:.1f} DBTP   "
                f"SEAM {seam:.2f}   {p.stat().st_size / 1e6:.2f} MB")
        rows.append((head, line, d, loop_start, loop_start + loop_len, scale))
    both = dec[0] + dec[2]
    loud, tp = lufs(both)
    at_bar = (int(np.argmax(np.abs(both).max(axis=0))) - loop_start) / (16 * s.step_n)
    print(f"full + motif summed (as the game adds them): {loud:.1f} LUFS, peak {db(np.abs(both).max()):.1f} dBFS "
          f"(loop bar {at_bar + 1:.1f}), true peak {tp:.1f} dBTP", flush=True)
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    for p, d in zip(paths, dec):
        seam = np.concatenate([d[:, end - n:end], d[:, loop_start:loop_start + n]], axis=1)
        aid = write_ogg(out / p.name.replace("-r75-a", "-seam-r75-a"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)
    bar_n = 16 * s.step_n
    a, b = loop_start + MIX_AID_BARS[0] * bar_n, loop_start + MIX_AID_BARS[1] * bar_n
    aid = write_ogg(out / f"{NAME}-motif-mix-r75-a.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {MIX_AID_BARS[0] + 1}-{MIX_AID_BARS[1]}, {(b - a) / SR:.1f} s)", flush=True)
    a, b = loop_start + JOIN_AID_BARS[0] * bar_n, loop_start + JOIN_AID_BARS[1] * bar_n
    mid = loop_start + 8 * bar_n - a
    xf = int(2.0 * SR)
    w = np.clip((np.arange(b - a) - (mid - xf)) / xf, 0.0, 1.0)
    join = dec[0][:, a:b] * np.cos(w * np.pi / 2) + dec[3][:, a:b] * np.sin(w * np.pi / 2)
    aid = write_ogg(out / f"{NAME}-fall-join-r75-a.ogg", fade_ends(join), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {JOIN_AID_BARS[0] + 1}-{JOIN_AID_BARS[1]}, the fall stem alone from bar 9, "
          f"{(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, dec, loop_start)
    for kind, bars, f, bl, fl, m in table:
        mtxt = f"{m:+.1f} dB" if m is not None else "-"
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  fall {fl:6.1f} "
              f"({fl - f:+.1f})  motif {mtxt}", flush=True)
    print(f"review: {review(out, rows, table, motif_peak).name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
