#!/usr/bin/env python3
"""Concept round 62 music (M9 part B, step AUD-B): track 12 "Hollow Rock" (Act 5 A: Levels 29, 30, 32 and 34) at
full length, with its three stems from one render, from the chosen round-03 sketch (music_r03.py `belt`,
belt-theme-r03-a: 135 BPM, B minor, the motif's home key). Brief: design/audio/music/README.md, *Act 5 music*
(track 12: "its full length (≈ 3:00) and stems ... by the method of tracks 9-11").

The sketch has a 28-bar loop (A the machine, B Vorne's motif, a 4-bar conveyor breakdown C, D the build with the
motif tutti), so the full length is new arrangement on its material, in the method of music_r41.py / music_r51b.py:
the sketch's voices, grooves, chords, figures, mix chains and seeds, laid out as its 4-bar intro + a 96-bar loop
(2:51) of twelve 8-bar sections (A = Bm Bm G G Em Em F# F#, B = Bm Gm Bm F# Bm Gm Em F#, the breakdown Bm x 8,
D = G G F# F# Bm Gm Em F#):

  intro 4 bars  radio static (band noise), two beeps, the pad, Vorne's motif in brass and organ, the anvils from bar
          3, a snare roll in bar 4 (the sketch's intro)
  A    A  the machine: four-on-the-floor kick, snare and an anvil on the backbeat, open hats, the syncopated
          distorted bass, the anvils' pattern, the distorted machine line and the arpeggio (Vorne's 1-b3-5-7 at
          speed, 16ths); the motif once in cold low brass and organ (bars 1-2)
  B    B  Vorne's motif: the sketch's 8-bar statement (brass, brass an octave down, organ), timpani, the arpeggio
          darker (900 Hz)
  A2   A  the machine again, the motif in low brass (bars 1-2)
  C    Bm breakdown (the sketch's C, doubled): no kit, the conveyor (anvil 16ths), the bass held, the lone human
          horn (humanity's motif on the flute, bars 2-3 and 6-7), a riser into D (bars 5-8)
  D    D  the build, the motif tutti (brass, trumpets an octave up, brass an octave down; bars 1-4), timpani, a
          snare roll in bars 7-8 (the sketch's D)
  B2   B  Vorne's motif again
  E    Em Em G G Bm Bm F# F#  drift (new): half time (kick on 1 and 3, snare on 3), no hats, no machine line, the bass
          held, the arpeggio dark and quiet, the anvils and the pad
  F    A  build: kick alone, hats from bar 3, the snare and the bright arpeggio and machine line from bar 5, a riser
          (bars 5-8) and a snare roll in bar 8
  D2   D  climax: the motif tutti, the machine line too
  B3   B  Vorne's motif
  C2   Bm the breakdown again with the lone horn, a riser into T
  T    A  turn: the machine, the hats out from bar 5, back into A

Changes to the sketch's sound: **no resonant plucks** (the user's rule, as tracks 9-11): the arpeggio is
music_r51b.v_pluck_soft (resonance q 1.4, the sketch 3.0; an 80 ms filter envelope, the sketch 50 ms); the intro's
static is the sketch's band-passed radio noise **without its crackle** (the brief: static only as band noise). Every
group goes through the sketch's own chain (music_r03.mixdown's per-group steps, same seeds, the kick ducking one fixed
envelope), all linear, so a stem is the exact sum of its groups.

Stems (the brief; the round-11 method): one render, linked mastering (each loudness pass and the encoder's peak
correction measure the full mix and apply its gain to all three files; the limiter at CEILING, -3 dBFS, as the encode at
-2 reached -0.8 dBTP), so the files share length, LOOPSTART /
LOOPLENGTH and gain and play sample-aligned:
  base   kick, the distorted bass, the anvils' low hits (900 Hz), the pad, the conveyor, the radio static and beeps
  full   the base plus the snare, hats, the anvils' top (1350-2200 Hz), crashes, steam, timpani, risers, the
         machine line, the arpeggio (the motif at speed: the groove, so it stays here) and the lone human horn
  motif  Vorne's motif in cold low brass and organ alone (not part of the full mix; the game adds it on top as a
         third stream): **in the open**, its statements MOTIF_UNDER (6) dB under the full mix (the brief's "about 6 dB
         under", measured as track 9's: EBU R128 over the statement windows; the statements keep the sketch's balance,
         the B and D tutti louder than A's low brass), not Acts 3-4's 18-20 dB under. Muting it leaves the machines and drops the brass. Played alone it also
         carries Vorne's broadcast (the Act 5 intro's pages 2-3) and its statements start in the intro.

Outputs (the --out directory, default design/audio/music/concept/):
  hollow-rock-r62-a.ogg                the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  hollow-rock-base-r62-a.ogg           the base stem (same length, loop and gain)
  hollow-rock-motif-r62-a.ogg          the motif stem (same length, loop and gain)
  hollow-rock{,-base,-motif}-seam-r62-a.ogg   listening aids: the last 8 s of the loop, then the first 8 s after
                                       LOOPSTART (0.5 s fades at both ends)
  hollow-rock-motif-mix-r62-a.ogg      listening aid: the full mix with the motif stem on top (as Level 29 plays
                                       them from the turn), loop bars 1-24 (A, B, A2)
  hollow-rock-sheet-r62-a.png          review sheet: the files' waveforms, loudness, true peak, seam, size, and a
                                       per-section loudness table
No SOURCE comment: these are concept files; tools/art/themes.py copies them into assets/music with it.

Usage: python3 tools/concept/audio/music_r62.py [--out DIR]   (~5 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seams and the stems'
alignment.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
from music import crash, hat, kick, ns, riser, snare, timpani, transpose, v_brass, v_pad  # noqa: E402
from music_r02 import LoopSong, seam_ratio, v_dbass, v_flute, v_riff  # noqa: E402
from music_r03 import (BE_CH, BE_METAL, BE_VORNE, PERC, FX, anvil, beep, figure, human_motif,  # noqa: E402
                       mixdown, steam, v_organ, v_trumpet, vorne_motif)
from music_r41 import fade_ends, master_linked, segment_lufs, tag_values, waveform, window, WAVE_H, WAVE_W  # noqa: E402
from music_r51b import v_pluck_soft  # noqa: E402
from synth import SR, db, decode, lufs, noise, note_freq, svf, undb, write_ogg  # noqa: E402
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "hollow-rock"
FULL_NAME, BASE_NAME, MOTIF_NAME = f"{NAME}-r62-a", f"{NAME}-base-r62-a", f"{NAME}-motif-r62-a"
SHEET = f"{NAME}-sheet-r62-a.png"
SEAM_SECONDS = 8.0
MIX_AID_BARS = (0, 24)  # loop bars of the motif-mix aid
MOTIF_UNDER = 6.0       # dB, the motif stem's statements under the full mix (the brief: about 6, in the open)
CEILING = -3.0          # dBFS, the linked limiter's ceiling: at -2 the encode reached -0.8 dBTP (the limit is -1)

A = ["Bm", "Bm", "G", "G", "Em", "Em", "F#", "F#"]
B = ["Bm", "Gm", "Bm", "F#", "Bm", "Gm", "Em", "F#"]
C = ["Bm"] * 8
D = ["G", "G", "F#", "F#", "Bm", "Gm", "Em", "F#"]
E = ["Em", "Em", "G", "G", "Bm", "Bm", "F#", "F#"]
PLAN = [("A", A), ("B", B), ("A2", A), ("C", C), ("D", D), ("B2", B), ("E", E), ("F", A), ("D2", D), ("B3", B),
        ("C2", C), ("T", A)]
LOOP_BARS = sum(len(c) for _, c in PLAN)
# the sketch's mix chains (music_r03.belt's mixdown spec), one per group; the stems split the sketch's groups
CHAINS = {
    "kick": dict(hp=35, gain=0.9), "snare": dict(hp=35, gain=0.9), "hats": dict(hp=35, gain=0.9),
    "perc": PERC, "fx": FX,
    "metal_low": dict(hp=200, rev=(1.2, 0.2), seed=211, gain=0.55),
    "metal_top": dict(hp=200, rev=(1.2, 0.2), seed=211, gain=0.55),
    "conveyor": dict(hp=200, rev=(1.2, 0.2), seed=211, gain=0.55),
    "radio": dict(hp=300, gain=0.8),
    "bass": dict(hp=40, duck=True, gain=0.65),
    "arp": dict(dly=(3, 0.35, 0.3, 2500), gain=0.45),
    "riff": dict(hp=120, dly=(3, 0.25, 0.2, 3000), gain=0.5),
    "pad": dict(hp=100, duck=True, rev=(3.0, 0.4), wid=1.6, seed=212, gain=0.5),
    "brass": dict(hp=70, rev=(2.8, 0.3), seed=213, gain=0.8),
    "organ": dict(hp=60, rev=(3.0, 0.35), seed=214, gain=0.5),
    "horn": dict(hp=150, dly=(6, 0.4, 0.3, 2500), rev=(3.5, 0.45), seed=215, gain=0.7),
}
BASE_GROUPS = ("kick", "bass", "metal_low", "pad", "conveyor", "radio")
FULL_GROUPS = BASE_GROUPS + ("snare", "hats", "metal_top", "perc", "fx", "riff", "arp", "horn")
MOTIF_GROUPS = ("brass", "organ")
TIMPANI = {"Bm": "B1", "F#": "F#2"}  # the sketch's timpani per chord, G1 otherwise


def band_static(n, seed=131):
    """music_r03.radio_static without its crackle: the band-passed broadcast noise with its slow fading."""
    rng = np.random.default_rng(seed)
    t = np.arange(n) / SR
    x = svf(noise(n, rng), 1900, q=0.8, mode="bp") * (0.5 + 0.5 * np.sin(2 * np.pi * 0.7 * t) ** 2)
    return np.vstack([x, x * 0.9])


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def motif_bars():
    """Loop bars where a motif statement starts (for the level table)."""
    at, out = starts(), []
    for kind, _ in PLAN:
        if kind in ("A", "A2", "B", "B2", "B3", "D", "D2"):
            out.append(at[kind] + (4 if kind in ("B", "B2", "B3") else 0))
    return out


def metal(s, bar, gain_scale):
    for st, p, g in BE_METAL:
        s.hit("metal_low" if p == 900 else "metal_top", anvil(float(p)), bar, st, g * gain_scale, (p - 1350) / 1500)


def hollow_rock():
    s = LoopSong(135, 4, LOOP_BARS)
    kicks = []
    # intro: the sketch's
    n = int(4 * s.bar * SR)
    s.tl.add("radio", band_static(n) * np.linspace(1, 0.3, n), 0, 0, 0.18)
    for st in (0, 2):
        s.hit("radio", beep(), 0, st, 0.25)
    s.chord("pad", v_pad, ["B1", "F#2", "B2"], 0, dur_steps=64, gain=0.45, bright=700.0)
    s.play("brass", v_brass, vorne_motif("B3"), 0, gain=0.55)
    s.play("organ", v_organ, vorne_motif("B3"), 0, gain=0.3, octave=-1)
    for bar in (2, 3):
        metal(s, bar, 0.5 + 0.25 * (bar - 2))
        s.hit("perc", steam(), bar, 12, 0.3, 0.5)
    for st in range(8, 16):
        s.hit("snare", snare(True), 3, st, 0.2 + 0.06 * (st - 8), 0.05)

    for base in s.sections():
        off = 0
        for kind, chords in PLAN:
            for j, name in enumerate(chords):
                bar = base + off + j
                pad, root, arp = BE_CH[name]
                conveyor = kind in ("C", "C2")
                drift = kind == "E"
                build = kind == "F"
                if j == 0 and kind in ("A", "B", "D", "B2", "D2", "B3"):
                    s.hit("perc", crash(), bar, 0, 0.45, 0.3)
                if not conveyor:
                    for beat in ((0, 2) if drift else range(4)):
                        s.hit("kick", kick(130), bar, beat * 4, 0.95 if not drift else 0.85)
                        kicks.append(s.pos(bar, beat * 4))
                    if drift:
                        s.hit("snare", snare(), bar, 8, 0.5, 0.05)
                        s.hit("metal_top", anvil(2200.0, 2), bar, 8, 0.25, -0.2)
                    elif not (build and j < 4):
                        for st in (4, 12):
                            s.hit("snare", snare(), bar, st, 0.5, 0.05)
                            s.hit("metal_top", anvil(2200.0, 2), bar, st, 0.25, -0.2)
                    if not drift and not (build and j < 2) and not (kind == "T" and j >= 4):
                        for st in range(2, 16, 4):
                            s.hit("hats", hat(True), bar, st, 0.2, 0.3)
                elif j == 7:  # a kick fill back into the groove
                    for st in (8, 10, 12, 14, 15):
                        s.hit("kick", kick(130), bar, st, 0.4 + 0.08 * (st - 8))
                        kicks.append(s.pos(bar, st))
                metal(s, bar, 1.2 if conveyor else 0.6 if drift else 0.8)
                if conveyor:
                    for st in range(16):
                        s.hit("conveyor", anvil(3100.0, 3), bar, st, 0.1 if st % 2 else 0.16, 0.5)
                if j % 4 == 3:
                    s.hit("perc", steam(), bar, 12, 0.35, 0.5 if j % 8 == 3 else -0.5)
                if (kind in ("D", "D2") and j >= 6) or (build and j == 7):
                    for st in range(16):
                        s.hit("snare", snare(True), bar, st, 0.2 + 0.02 * st + 0.08 * (j - 6 if j >= 6 else 1), 0.05)
                if (conveyor or build) and j == 4:
                    s.hit("fx", riser(s.bar * 4), bar, 0, 0.3)
                # distorted bass: syncopated 8ths, held in the breakdowns and the drift
                if not conveyor and not drift:
                    for st in (0, 3, 6, 8, 10, 13, 14):
                        nm = transpose(root, 12 if st == 14 else 0)
                        s.hit("bass", v_dbass(note_freq(nm), round(s.step * 1.4, 4)), bar, st, 0.42)
                else:
                    s.hit("bass", v_dbass(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.3)
                # the machine plays Vorne's arpeggio
                notes = figure(transpose(root, 24), arp)
                bright = kind in ("A", "A2", "D", "D2", "T") or (build and j >= 4)
                if kind in ("A", "A2", "D2", "T") or (build and j >= 4):  # the distorted machine line
                    for st in range(0, 16, 2):
                        s.hit("riff", v_riff(note_freq(transpose(notes[(st // 2) % 4], -12)),
                                             round(s.step * 1.4, 4)), bar, st, 0.3, 0.2)
                arp_gain = 0.15 if conveyor or drift else 0.3 if kind in ("A", "A2", "T") else 0.25
                for st in range(16):
                    s.hit("arp", v_pluck_soft(note_freq(notes[st % 8]), round(s.step * 0.6, 4),
                                              1500.0 if bright else 900.0),
                          bar, st, arp_gain, 0.35 if st % 2 else -0.35)
                s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4, bright=900.0)
                if kind in ("B", "D", "B2", "D2", "B3") and j % 2 == 0:
                    s.hit("perc", timpani(TIMPANI.get(name, "G1")), bar, 0, 0.7)
            # Vorne's motif (the motif stem) and the lone human horn (the full mix)
            first = base + off
            if kind in ("A", "A2"):
                s.play("brass", v_brass, vorne_motif("B3"), first, gain=0.4, position=-0.1, octave=-1)
                s.play("organ", v_organ, vorne_motif("B3"), first, gain=0.25)
            if kind in ("B", "B2", "B3"):
                s.play("brass", v_brass, BE_VORNE, first, gain=0.6, position=-0.1)
                s.play("brass", v_brass, BE_VORNE, first, gain=0.3, position=0.15, octave=-1)
                s.play("organ", v_organ, BE_VORNE, first, gain=0.25)
            if kind in ("D", "D2"):
                s.play("brass", v_brass, BE_VORNE[4:], first, gain=0.65, position=-0.1)
                s.play("brass", v_trumpet, BE_VORNE[4:], first, gain=0.35, position=0.15, octave=1)
                s.play("brass", v_brass, BE_VORNE[4:], first, gain=0.35, octave=-1)
            if kind in ("C", "C2"):
                for k in (1, 5):
                    s.play("horn", v_flute, human_motif("B3"), first + k, gain=0.4, position=0.4)
            off += len(chords)
    groups = {g: mixdown(s, {g: spec}, kicks, depth=0.45) for g, spec in CHAINS.items()}
    return s, groups


def render():
    """One render; returns (song, full mix, base stem, motif stem) before mastering."""
    s, groups = hollow_rock()
    full = sum(groups[g] for g in FULL_GROUPS)
    base = sum(groups[g] for g in BASE_GROUPS)
    motif = sum(groups[g] for g in MOTIF_GROUPS)
    # the stem leaves the brass out of the full mix, so the sketch's gains would put the motif above it: its
    # statements are set MOTIF_UNDER dB under the full mix (EBU R128, the mean over the statement windows)
    spans = [window(s, lb) for lb in motif_bars()]
    diffs = [segment_lufs(motif[:, a:b]) - segment_lufs(full[:, a:b]) for a, b in spans]
    offset = float(np.mean(diffs))
    motif = motif * undb(-MOTIF_UNDER - offset)
    print(f"motif calibration: raw {offset:+.1f} dB vs the full mix -> {-MOTIF_UNDER:.0f} dB", flush=True)
    return s, full, base, motif


def sections_table(s, dec, i0):
    """[(section, bars, full LUFS, base LUFS, motif vs full dB in its statement windows)] over the decoded loop."""
    at, rows = starts(), []
    full, base, motif = dec
    bar_n = 16 * s.step_n
    for kind, chords in PLAN:
        a, b = i0 + at[kind] * bar_n, i0 + (at[kind] + len(chords)) * bar_n
        stmts = [lb for lb in motif_bars() if at[kind] <= lb < at[kind] + len(chords)]
        under = [segment_lufs(motif[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n])
                 - segment_lufs(full[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n]) for lb in stmts]
        rows.append((kind, f"{at[kind] + 1}-{at[kind] + len(chords)}", segment_lufs(full[:, a:b]),
                     segment_lufs(base[:, a:b]), float(np.mean(under)) if under else None))
    return rows


def review(out, rows, table, motif_peak):
    block = 40 + WAVE_H + 26
    table_h = 30 + 12 * (len(table) + 2)
    img = raster.sheet(WAVE_W + 40, 40 + block * len(rows) + table_h + 20,
                       "HOLLOW ROCK - TRACK 12, ACT 5 A, AND ITS THREE STEMS (FULL LENGTH)",
                       "ROUND 62, M9 PART B - MUSIC_R62.PY - MEASURED ONLY")
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
        motif = f"{m:>+8.1f} DB" if m is not None else "       -"
        raster.draw_text(img, 20, y, f"{kind:<9} {bars:<7} {f:>8.1f}    {b:>8.1f}    {b - f:>+8.1f} LU    {motif}",
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
    s, full, base, motif = render()
    full, loop_start, loop_len = r11.prepare(s, full)
    base, _, _ = r11.prepare(s, base)
    motif, _, _ = r11.prepare(s, motif)
    full, base, motif = master_linked([full, base, motif], ceiling_db=CEILING)
    for a in (full, base, motif):
        a[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    tags = {"LOOPSTART": loop_start, "LOOPLENGTH": loop_len}
    path = out / f"{FULL_NAME}.ogg"
    for _ in range(4):  # the encoder's peak correction for the full mix, applied to all three
        write_ogg(path, full, max_peak_db=None, tags=tags)
        over = db(np.abs(decode(path)).max()) + 1.0
        if over <= 0:
            break
        full, base, motif = (x * undb(-over - 0.1) for x in (full, base, motif))
    paths = [path, write_ogg(out / f"{BASE_NAME}.ogg", base, max_peak_db=None, tags=tags),
             write_ogg(out / f"{MOTIF_NAME}.ogg", motif, max_peak_db=None, tags=tags)]
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
        aid = write_ogg(out / p.name.replace("-r62-a", "-seam-r62-a"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)
    bar_n = 16 * s.step_n
    a, b = loop_start + MIX_AID_BARS[0] * bar_n, loop_start + MIX_AID_BARS[1] * bar_n
    aid = write_ogg(out / f"{NAME}-motif-mix-r62-a.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {MIX_AID_BARS[0] + 1}-{MIX_AID_BARS[1]}, {(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, dec, loop_start)
    for kind, bars, f, bl, m in table:
        motif_txt = f"{m:+.1f} dB" if m is not None else "-"
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  motif {motif_txt}",
              flush=True)
    print(f"review: {review(out, rows, table, motif_peak).name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
