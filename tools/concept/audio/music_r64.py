#!/usr/bin/env python3
"""Concept round 64 music (M9 part D, step AUD-D): track 13 "Gilded Cage" (the Act 5 B theme: Levels 31, 33 and the
Level 35 approach) at full length, with its three stems from one render, from the chosen round-60 sketch
(music_r60.py `gilded_cage`, act5-b-theme-r60-a: 126 BPM, E minor, the motif on E G B D-sharp; round 60 M.1 a). Brief:
design/audio/music/README.md, *Act 5 music* (track 13: "the production delivers three stems as track 12's (base, full,
motif) ... made in the part that first plays it (M9 part D, Level 31)").

The sketch has a 24-bar loop (A the groove, B with hats and the high metal, C with the press doubled; the motif in the
open every 8 bars, the struck metal answering it), so the full length is new arrangement on its material, in the method
of music_r62.py (track 12): the sketch's voices, grooves, chords, figures, mix chains and seeds, laid out as its 4-bar
intro + an 88-bar loop (2:48) of eleven 8-bar sections (A = Em Em C C Am Am B B; the breakdown Br = Em Em Em Em C C B
B; the drift E = Am Am C C Em Em B B):

  intro 4 bars  the pad and the chains, the motif's first statement in the open, the kick and a riser from bar 3, the
          press on bars 1 and 3 (the sketch's intro)
  A    A  the cage: four-on-the-floor kick, struck sheet metal on the backbeat with a tight snare under it, chains on
          the off-16ths, the press hammer every other bar, the gated distorted 16th bass sequence, the cold string pad;
          Vorne's motif in low brass and organ (bars 1-2), the struck metal playing it back at speed (bars 3-4)
  B    A  + hats and the high metal accents; the motif and its answer
  C    A  + the press doubled; the motif and its answer
  Br   Br the line stops (new): no kick, no bass sequence; the bass held, the pad, the chains thinned to the beat, the
          sheet metal once a bar, the struck metal playing the motif slowly (quarter notes, bars 1-4), a riser and a
          kick fill into D (bars 5-8)
  D    A  the hammer: C's groove with hats, the motif tutti (brass, trumpets an octave up, brass an octave down,
          organ), timpani, a snare roll in bars 7-8
  B2   A  B again
  E    E  drift (new): half time (the kick on 1 and 3, the sheet metal on 3), no hats, the chains on the beat, the bass
          held, the pad and the press every other bar
  F    A  build: the kick alone, the chains and the sheet metal from bar 3, the bass sequence and the hats from bar 5,
          a riser (bars 5-8) and a snare roll in bar 8
  D2   A  the hammer again, the motif tutti
  C2   A  C again
  T    A  turn: B's groove, the hats out from bar 5, back into A

No bubbly or popping sounds and no resonant plucks (the sketch had none: the metal is struck, every pitched voice has a
soft attack except the bass sequence). Every group goes through the sketch's own chain (music_r60.gilded_cage's mixdown
spec, same seeds, the kick ducking one fixed envelope), all linear, so a stem is the exact sum of its groups.

Stems (track 12's method): one render, linked mastering (each loudness pass and the encoder's peak correction measure
the full mix and apply its gain to all three files; the limiter at CEILING), so the files share length, LOOPSTART /
LOOPLENGTH and gain and play sample-aligned:
  base   the kick, the bass (the sequence and the held notes), the pad and the press hammer
  full   the base plus the struck sheet metal's backbeat, the snare, hats, chains, the high metal accents, crashes,
         timpani, risers and the struck metal's answer (the machines answering the motif: the groove, so it stays here)
  motif  Vorne's motif in low brass and organ alone (not part of the full mix; the game adds it on top as a third
         stream): in the open, its statements MOTIF_UNDER (6) dB under the full mix (EBU R128 over the statement
         windows, as track 12; set once before mastering and trimmed once after it, as the limiter works on the full
         mix and not on the quiet stem), the D tutti louder than the low-brass statements as sketched. Muting it
         leaves the machines and drops the brass. Played alone it carries Level 35's recruitment after the boss
         (`motif_after_boss`); its first statement is in the intro.

Outputs (the --out directory, default design/audio/music/concept/):
  gilded-cage-r64-a.ogg                the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  gilded-cage-base-r64-a.ogg           the base stem (same length, loop and gain)
  gilded-cage-motif-r64-a.ogg          the motif stem (same length, loop and gain)
  gilded-cage{,-base,-motif}-seam-r64-a.ogg   listening aids: the last 8 s of the loop, then the first 8 s after
                                       LOOPSTART (0.5 s fades at both ends)
  gilded-cage-motif-mix-r64-a.ogg      listening aid: the full mix with the motif stem on top (as Level 31 plays them
                                       from section 5), loop bars 1-24 (A, B, C)
  gilded-cage-sheet-r64-a.png          review sheet: the files' waveforms, loudness, true peak, seam, size, and a
                                       per-section loudness table
No SOURCE comment: these are concept files; tools/art/themes.py copies them into assets/music with it.

Usage: python3 tools/concept/audio/music_r64.py [--out DIR]   (~5 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seams and the stems'
alignment.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
from music import crash, hat, kick, ns, riser, snare, timpani, transpose, v_brass, v_strings_long  # noqa: E402
from music_r02 import LoopSong, seam_ratio, v_dbass  # noqa: E402
from music_r03 import mixdown, v_organ, v_trumpet, vorne_motif  # noqa: E402
from music_r41 import fade_ends, master_linked, segment_lufs, tag_values, waveform, window, WAVE_H, WAVE_W  # noqa: E402
from music_r60 import A_BASS, A_CH, A_GATE, chain, press, sheet_metal  # noqa: E402
from synth import SR, db, decode, lufs, note_freq, undb, write_ogg  # noqa: E402
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "gilded-cage"
FULL_NAME, BASE_NAME, MOTIF_NAME = f"{NAME}-r64-a", f"{NAME}-base-r64-a", f"{NAME}-motif-r64-a"
SHEET = f"{NAME}-sheet-r64-a.png"
SEAM_SECONDS = 8.0
MIX_AID_BARS = (0, 24)  # loop bars of the motif-mix aid
MOTIF_UNDER = 6.0       # dB, the motif stem's statements under the full mix (as track 12: in the open)
CEILING = -3.5          # dBFS, the linked limiter's ceiling (at -3 the encode reached -0.9 dBTP; the limit is -1)

A = ["Em", "Em", "C", "C", "Am", "Am", "B", "B"]
BR = ["Em", "Em", "Em", "Em", "C", "C", "B", "B"]
E = ["Am", "Am", "C", "C", "Em", "Em", "B", "B"]
PLAN = [("A", A), ("B", A), ("C", A), ("Br", BR), ("D", A), ("B2", A), ("E", E), ("F", A), ("D2", A), ("C2", A),
        ("T", A)]
LOOP_BARS = sum(len(c) for _, c in PLAN)
MOTIF_KINDS = ("A", "B", "C", "D", "B2", "D2", "C2", "T")  # a statement at the section's bar 1
TUTTI = ("D", "D2")
HATS = ("B", "D", "B2", "D2", "T")
PRESS2 = ("C", "D", "D2", "C2")
TIMPANI = {"Em": "E2", "C": "C2", "Am": "A1", "B": "B1"}
# the sketch's mix chains (music_r60.gilded_cage's mixdown spec), one per group; the stems split the sketch's groups
DRUMS = dict(hp=32, gain=0.9)
PERC = dict(hp=30, rev=(1.8, 0.2), damp=0.8, seed=6011, gain=0.7)
METAL = dict(hp=150, rev=(1.4, 0.25), seed=6012, gain=0.6)
CHAINS = {
    "kick": DRUMS, "snare": DRUMS, "press": PERC, "perc": PERC, "metal_low": METAL, "metal_top": METAL,
    "chain": dict(hp=1500, gain=0.4),
    "hats": dict(hp=600, gain=0.4),
    "fx": dict(gain=0.5),
    "bass": dict(hp=38, duck=True, gain=0.6),
    "pad": dict(hp=110, duck=True, rev=(3.5, 0.45), wid=1.6, seed=6013, gain=0.45),
    "answer": dict(hp=300, dly=(3, 0.3, 0.25, 3000), rev=(1.6, 0.25), seed=6014, gain=0.55),
    # the sketch's motif bus (hp 60, a 2.8 s reverb at 0.3, seed 6015), here one chain per instrument
    "brass": dict(hp=60, rev=(2.8, 0.3), seed=6015, gain=0.45),
    "organ": dict(hp=60, rev=(2.8, 0.3), seed=6016, gain=0.45),
}
BASE_GROUPS = ("kick", "bass", "pad", "press")
FULL_GROUPS = BASE_GROUPS + ("metal_low", "snare", "hats", "chain", "metal_top", "perc", "fx", "answer")
MOTIF_GROUPS = ("brass", "organ")


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def motif_bars():
    """Loop bars where a motif statement starts (for the calibration and the level table)."""
    at = starts()
    return [at[kind] for kind, _ in PLAN if kind in MOTIF_KINDS]


def motif(s, bar, tutti):
    """Vorne's motif in the open: low brass and organ (the sketch's statement); tutti adds trumpets an octave up."""
    s.play("brass", v_brass, vorne_motif("E3"), bar, gain=0.65, position=-0.1)
    s.play("brass", v_brass, vorne_motif("E3"), bar, gain=0.35, position=0.15, octave=-1)
    s.play("organ", v_organ, vorne_motif("E3"), bar, gain=0.4, octave=-1)
    if tutti:
        s.play("brass", v_trumpet, vorne_motif("E3"), bar, gain=0.3, position=0.2, octave=1)


def answer(s, bar, back):
    """The machines answer: the struck metal plays the motif at speed (8ths), up then (`back`) down."""
    notes = [transpose("E4", k) for k in (0, 3, 7, 11)]
    for st in range(0, 16, 2):
        nm = notes[3 - (st // 2) % 4] if back else notes[(st // 2) % 4]
        s.hit("answer", sheet_metal(note_freq(nm), 3, 0.1), bar, st, 0.3, 0.25 if st % 4 else -0.25)


def bass_seq(s, bar, root):
    for st in range(16):
        if A_GATE[st]:
            s.hit("bass", v_dbass(note_freq(transpose(root, 12 + A_BASS[st])), round(s.step * 0.7, 4)), bar, st, 0.36)


def gilded_cage():
    s = LoopSong(126, 4, LOOP_BARS)
    kicks = []
    # intro: the sketch's
    for bar in range(4):
        s.chord("pad", v_strings_long, A_CH["Em"][0], bar, dur_steps=16, gain=0.35, attack=0.4)
        for st in range(1, 16, 2):
            s.hit("chain", chain(st + 16 * bar), bar, st, 0.25 + 0.1 * bar, 0.4 if st % 4 == 1 else -0.4)
        if bar >= 2:
            for beat in range(4):
                s.hit("kick", kick(120), bar, beat * 4, 0.9)
                kicks.append(s.pos(bar, beat * 4))
    s.hit("press", press(), 0, 0, 0.8)
    s.hit("press", press(), 2, 0, 0.8)
    s.play("brass", v_brass, vorne_motif("E3"), 0, gain=0.6, position=-0.1)
    s.play("organ", v_organ, vorne_motif("E3"), 0, gain=0.45, octave=-1)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.15)

    for base in s.sections():
        off = 0
        for kind, chords in PLAN:
            for j, name in enumerate(chords):
                bar = base + off + j
                pad, root = A_CH[name]
                brk, drift, build = kind == "Br", kind == "E", kind == "F"
                if j == 0 and kind in MOTIF_KINDS:
                    s.hit("perc", crash(), bar, 0, 0.3, 0.3)
                # the kick
                if brk:
                    if j == 7:  # a kick fill back into the groove
                        for st in (8, 10, 12, 14, 15):
                            s.hit("kick", kick(120), bar, st, 0.4 + 0.08 * (st - 8))
                            kicks.append(s.pos(bar, st))
                else:
                    for beat in ((0, 2) if drift else range(4)):
                        s.hit("kick", kick(120), bar, beat * 4, 0.95 if not drift else 0.85)
                        kicks.append(s.pos(bar, beat * 4))
                # struck sheet metal on the backbeat with a tight snare under it
                if drift:
                    s.hit("metal_low", sheet_metal(310.0, 1 + (j % 3)), bar, 8, 0.55, -0.15)
                    s.hit("snare", snare(True), bar, 8, 0.3, 0.05)
                elif brk:
                    s.hit("metal_low", sheet_metal(310.0, 1 + (j % 3)), bar, 0, 0.45, -0.15)
                elif not (build and j < 2):
                    for st in (4, 12):
                        s.hit("metal_low", sheet_metal(310.0, 1 + (j % 3)), bar, st, 0.55, -0.15)
                        s.hit("snare", snare(True), bar, st, 0.3, 0.05)
                # chains: the off-16ths; on the beat in the breakdown and the drift; from bar 3 of the build
                if brk or drift:
                    for st in range(0, 16, 4):
                        s.hit("chain", chain(st + 16 * (j % 4)), bar, st, 0.22, 0.4 if st % 8 else -0.4)
                elif not (build and j < 2):
                    for st in range(1, 16, 2):
                        s.hit("chain", chain(st + 16 * (j % 4)), bar, st, 0.32 if st % 4 == 3 else 0.22,
                              0.4 if st % 4 == 1 else -0.4)
                # the press hammer
                if not brk and (j % 2 == 0 or kind in PRESS2) and not (build and j < 4):
                    s.hit("press", press(), bar, 0, 0.75)
                if kind in PRESS2 and j % 2 == 1:
                    s.hit("press", press(), bar, 10, 0.5)
                # hats and the high metal accents
                if (kind in HATS and not (kind == "T" and j >= 4)) or (build and j >= 4):
                    for st in range(2, 16, 4):
                        s.hit("hats", hat(True), bar, st, 0.18, 0.3)
                    for st in (7, 15):
                        s.hit("metal_top", sheet_metal(1150.0, 4, 0.08), bar, st, 0.25, 0.35)
                # snare rolls, risers, timpani
                if (kind in TUTTI and j >= 6) or (build and j == 7):
                    for st in range(16):
                        s.hit("snare", snare(True), bar, st,
                              0.15 + 0.015 * st + 0.06 * (j - 6 if kind in TUTTI else 1), 0.05)
                if (brk or build) and j == 4:
                    s.hit("fx", riser(s.bar * 4), bar, 0, 0.2)
                if kind in TUTTI and j % 2 == 0:
                    s.hit("perc", timpani(TIMPANI[name]), bar, 0, 0.6)
                # the bass: the gated sequence; held in the breakdown, the drift and the build's first half
                if brk or drift or (build and j < 4):
                    s.hit("bass", v_dbass(note_freq(transpose(root, 12)), round(s.step * 15, 4)), bar, 0, 0.3)
                else:
                    bass_seq(s, bar, root)
                s.chord("pad", v_strings_long, pad, bar, dur_steps=16, gain=0.36 if brk or drift else 0.3,
                        attack=0.3)
                # the machines answer the motif (two bars after each statement)
                if kind in MOTIF_KINDS and j in (2, 3):
                    answer(s, bar, j == 3)
                if brk and j < 4:  # the struck metal plays the motif slowly in the breakdown
                    notes = [transpose("E4", k) for k in (0, 3, 7, 11)]
                    for q in range(4):
                        s.hit("answer", sheet_metal(note_freq(notes[q]), 3, 0.25), bar, q * 4,
                              0.35 if j % 2 == 0 else 0.25, 0.2 if q % 2 else -0.2)
            if kind in MOTIF_KINDS:
                motif(s, base + off, kind in TUTTI)
            off += len(chords)
    groups = {g: mixdown(s, {g: spec}, kicks, depth=0.4) for g, spec in CHAINS.items()}
    return s, groups


def render():
    """One render; returns (song, full mix, base stem, motif stem) before mastering."""
    s, groups = gilded_cage()
    full = sum(groups[g] for g in FULL_GROUPS)
    base = sum(groups[g] for g in BASE_GROUPS)
    mot = sum(groups[g] for g in MOTIF_GROUPS)
    # the motif stem is not in the full mix, so its statements are set MOTIF_UNDER dB under it (EBU R128, the mean
    # over the statement windows)
    spans = [window(s, lb) for lb in motif_bars()]
    diffs = [segment_lufs(mot[:, a:b]) - segment_lufs(full[:, a:b]) for a, b in spans]
    offset = float(np.mean(diffs))
    mot = mot * undb(-MOTIF_UNDER - offset)
    print(f"motif calibration: raw {offset:+.1f} dB vs the full mix -> {-MOTIF_UNDER:.0f} dB", flush=True)
    return s, full, base, mot


def sections_table(s, dec, i0):
    """[(section, bars, full LUFS, base LUFS, motif vs full dB in its statement windows)] over the decoded loop."""
    at, rows = starts(), []
    full, base, mot = dec
    bar_n = 16 * s.step_n
    for kind, chords in PLAN:
        a, b = i0 + at[kind] * bar_n, i0 + (at[kind] + len(chords)) * bar_n
        stmts = [lb for lb in motif_bars() if at[kind] <= lb < at[kind] + len(chords)]
        under = [segment_lufs(mot[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n])
                 - segment_lufs(full[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n]) for lb in stmts]
        rows.append((kind, f"{at[kind] + 1}-{at[kind] + len(chords)}", segment_lufs(full[:, a:b]),
                     segment_lufs(base[:, a:b]), float(np.mean(under)) if under else None))
    return rows


def review(out, rows, table, motif_peak):
    block = 40 + WAVE_H + 26
    table_h = 30 + 12 * (len(table) + 2)
    img = raster.sheet(WAVE_W + 40, 40 + block * len(rows) + table_h + 20,
                       "GILDED CAGE - TRACK 13, ACT 5 B, AND ITS THREE STEMS (FULL LENGTH)",
                       "ROUND 64, M9 PART D - MUSIC_R64.PY - MEASURED ONLY")
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
    raster.draw_text(img, 20, y, "SECTION   BARS    FULL LUFS   BASE LUFS   BASE VS FULL   MOTIF VS FULL",
                     raster.LABEL_DIM)
    for kind, bars, f, b, m in table:
        y += 12
        mtxt = f"{m:>+8.1f} DB" if m is not None else "       -"
        raster.draw_text(img, 20, y, f"{kind:<9} {bars:<7} {f:>8.1f}    {b:>8.1f}    {b - f:>+8.1f} LU    {mtxt}",
                         raster.LABEL)
    path = out / SHEET
    img.convert("RGB").save(path, optimize=True)
    return path


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
    out.mkdir(parents=True, exist_ok=True)
    s, full, base, mot = render()
    full, loop_start, loop_len = r11.prepare(s, full)
    base, _, _ = r11.prepare(s, base)
    mot, _, _ = r11.prepare(s, mot)
    full, base, mot = master_linked([full, base, mot], ceiling_db=CEILING)
    # the limiter squeezed the full mix, not the quiet motif stem: trim the stem back to MOTIF_UNDER (the same windows
    # in the assembled file's loop)
    bar_n = 16 * s.step_n
    trim = float(np.mean([segment_lufs(mot[:, loop_start + lb * bar_n:loop_start + (lb + 2) * bar_n])
                          - segment_lufs(full[:, loop_start + lb * bar_n:loop_start + (lb + 2) * bar_n])
                          for lb in motif_bars()]))
    mot = mot * undb(-MOTIF_UNDER - trim)
    print(f"motif trim after mastering: {trim:+.1f} dB -> {-MOTIF_UNDER:.0f} dB", flush=True)
    for a in (full, base, mot):
        a[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    path = out / f"{FULL_NAME}.ogg"
    for _ in range(4):  # the encoder's peak correction for the full mix, applied to all three
        write_ogg(path, full, max_peak_db=None, tags=tags)
        over = db(np.abs(decode(path)).max()) + 1.0
        if over <= 0:
            break
        full, base, mot = (x * undb(-over - 0.1) for x in (full, base, mot))
    paths = [path, write_ogg(out / f"{BASE_NAME}.ogg", base, max_peak_db=None, tags=tags),
             write_ogg(out / f"{MOTIF_NAME}.ogg", mot, max_peak_db=None, tags=tags)]
    dec = [decode(p) for p in paths]
    full_lufs = lufs(dec[0])[0]
    rows = []
    motif_peak = 1.0
    for p, d, label in zip(paths, dec, ("FULL MIX", "BASE STEM", "MOTIF STEM")):
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
    print(f"full + motif summed (as the game adds them): {loud:.1f} LUFS, peak {db(np.abs(both).max()):.1f} dBFS, "
          f"true peak {tp:.1f} dBTP", flush=True)
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    for p, d in zip(paths, dec):
        seam = np.concatenate([d[:, end - n:end], d[:, loop_start:loop_start + n]], axis=1)
        aid = write_ogg(out / p.name.replace("-r64-a", "-seam-r64-a"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)
    a, b = loop_start + MIX_AID_BARS[0] * bar_n, loop_start + MIX_AID_BARS[1] * bar_n
    aid = write_ogg(out / f"{NAME}-motif-mix-r64-a.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {MIX_AID_BARS[0] + 1}-{MIX_AID_BARS[1]}, {(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, dec, loop_start)
    for kind, bars, f, bl, m in table:
        mtxt = f"{m:+.1f} dB" if m is not None else "-"
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  motif {mtxt}", flush=True)
    print(f"review: {review(out, rows, table, motif_peak).name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
