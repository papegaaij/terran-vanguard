#!/usr/bin/env python3
"""Concept round 41 music (M7 part E, step MUS-E): track 9 "Grey Signal" (Act 3 B: Levels 18, 19 and the
Level 21 approach) at full length, with its three stems from one render, from the chosen round-36 sketch b
(music_r36b.py `grey_signal`, act3-b-theme-r36-b: 125 BPM, D minor, 10 % swing, 65.8 s). Pending the
user's review in round 41. Brief: design/audio/music/README.md, "Track 9, the Act 3 B theme: brief" and
"Vorne's motif layer (Levels 18-19)".

The sketch has a 28-bar loop (A groove, B with the hand drums, a 4-bar breakdown C, D full), so the full
length is new arrangement on its material, in the method of music_r38.py: the sketch's voices, grooves,
chords, mix chains and seeds, laid out as its 4-bar intro + an 88-bar loop (2:49) of eleven 8-bar
sections (P = Dm9 Dm9 Bbmaj7 Bbmaj7 Gm9 Gm9 A7(b9) A7(b9), the breakdown Dm9 Dm9 Bbmaj7 A7(b9) twice):

  intro 4 bars  wind, the stabs closed and opening, the sub; the kick from bar 2 (the sketch's intro)
  A    P  the groove: kick, sub, rimshots, swung 16th hats, stabs opening 800 -> 1500 Hz (the sketch's A)
  B    P  + track 8's hand drums (doum, tek), stabs at 1500 Hz (the sketch's B)
  B2   P  + a fourth stab on step 7, stabs at 1700 Hz, the pad up
  C    BRK breakdown: stabs, pad and held sub alone, the wind swelling, hand drums back in its last two bars,
          a kick fill (the sketch's C, doubled)
  D    P  the full groove, open hats on the off-beats, stabs at 2300 Hz (the sketch's D)
  D2   P  as D, a ghost rimshot on step 15, stabs at 2500 Hz
  E    Gm9 Gm9 Dm9 Dm9 Bbmaj7 Bbmaj7 A7 A7  dust: the kit muffled (a 700 Hz low-pass on kick and rims, as
          track 8's storm), no hats, the hand drums forward, held sub, closed stabs, the wind swelling
  F    P  build: kick alone, then the rimshots (bar 2), then the hats (bar 4); the stabs opening
          900 -> 2300 Hz, a riser and a rimshot roll into D3
  D3   P  climax: as D2, stabs at 2600 Hz
  A3   P  the groove with the hand drums, stabs at 1800 Hz
  T    P  turn: open hats out, hand drums out from bar 4, the stabs closing 1800 -> 800 Hz into A

Changes to the sketch's sound: the chord stab's resonance is lower (q 1.1, not 1.8) and its filter
envelope slower (80 ms, not 50 ms), so it cannot read as a resonant "bloop" (the brief: no bubbly or
popping sounds), and its mix gain 1 dB up (0.85, not 0.75) to keep the sketch's 250 Hz - 2 kHz balance
(measured per section against act3-b-theme-r36-b); everything else is the sketch's voices and chains. The kicks go to their own group
through the drums chain, and every chain is linear (the kick ducking is one fixed envelope), so a stem is
the exact sum of its groups.

Stems (the brief; the round-11 method of music_r11.py / music_r38.py): one render, every group through the
full mix's own chain, linked mastering (each loudness pass and the encoder's peak correction measure the
full mix and apply its gain to all three files), so the files share length, LOOPSTART / LOOPLENGTH and
gain and play sample-aligned:
  base   kick, sub, pad, shaker and hand drums, the wind
  full   the base plus the hats, rimshots, stabs, crashes and risers
  motif  Vorne's motif layer alone (not part of the full mix; the game adds it on top as a third stream)
Vorne's motif (D4 F4 A4 C#5, music_r03.vorne_motif): the sketch's intercepted signal, a thin whistle
through a radio band-pass (700 Hz - 2.6 kHz) over a 2-bar swell of band-noise static (no crackle), one
statement every 8 bars through the whole loop (≈ 15.4 s apart; on each section's first Dm9 bar), two more
in the breakdown C and the climax D3 (bar 4). No motif in the intro: the game turns the stem on and off
by cue, so it is spread over the loop. Its level is set by measurement: the statements sit MOTIF_UNDER dB
(MOTIF_UNDER_C in the breakdown) under the full mix in the same 2-bar windows (EBU R128 loudness).

Outputs (the --out directory, default design/audio/music/concept/):
  grey-signal-r41-a.ogg               the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  grey-signal-base-r41-a.ogg          the base stem (same length, loop and gain)
  grey-signal-motif-r41-a.ogg         the motif stem (same length, loop and gain)
  grey-signal{,-base,-motif}-seam-r41-a.ogg   listening aids: the last 8 s of the loop, then the first 8 s
                                      after LOOPSTART (0.5 s fades at both ends)
  grey-signal-motif-mix-r41-a.ogg     listening aid: the full mix with the motif stem on top (as Level 18
                                      plays them), loop bars 16-40 (B2, the breakdown, D)
  music-r41-a.png                     review sheet: the files' waveforms (intro, loop, tail, loop points),
                                      loudness, true peak, seam, size, and a per-section loudness table
No SOURCE comment: these are concept files; tools/art/themes.py adds it when the game step makes them
production files.

Usage: python3 tools/concept/audio/music_r41.py [--out DIR]   (~5 min)
Deterministic (fixed seeds); not listened to by Claude: checked by loudness, true peak, the seams and the
stems' alignment.
"""
import re
import subprocess
import sys
import tempfile
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import music_r11 as r11  # noqa: E402
from music import crash, hat, hp, kick, ns, riser, sidechain, v_pad, widen  # noqa: E402
from music_r02 import LoopSong, bass_line, doum, rim, seam_ratio, shaker, tek, v_sub  # noqa: E402
from music_r03 import vorne_motif  # noqa: E402
from music_r36b import B_CH, B_STABS, B_SUB, B_TRIBAL, bandpass, dust_wind, static_swell, v_whistle  # noqa: E402
from synth import (SR, adsr, db, decode, delay, exp_decay, limiter, lufs, note_freq, reverb, saw, svf,  # noqa: E402
                   undb, write_ogg, write_wav)
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "grey-signal"
FULL_NAME, BASE_NAME, MOTIF_NAME = f"{NAME}-r41-a", f"{NAME}-base-r41-a", f"{NAME}-motif-r41-a"
SHEET = "music-r41-a.png"
CEILING = -2.0           # dBFS, the linked limiter's ceiling (synth.master's default for the themes)
MOTIF_UNDER = 19.0       # dB under the full mix (the brief: 18-20)
MOTIF_UNDER_C = 17.0     # dB under the full mix in the breakdown (the brief: 17)
SEAM_SECONDS = 8.0
XF_STEPS = 2             # 16ths of crossfade at the loop start: LOOPSTART falls on step 2 of bar 0, not on
                         # step 4 (a kick: the seam measure reads its click as a jump, base stem 1.7)
MIX_AID_BARS = (16, 40)  # loop bars of the motif-mix aid

P = ["Dm9", "Dm9", "Bbmaj7", "Bbmaj7", "Gm9", "Gm9", "A7", "A7"]
BRK = ["Dm9", "Dm9", "Bbmaj7", "A7", "Dm9", "Dm9", "Bbmaj7", "A7"]
PLAN = [("A", P), ("B", P), ("B2", P), ("C", BRK), ("D", P), ("D2", P),
        ("E", ["Gm9", "Gm9", "Dm9", "Dm9", "Bbmaj7", "Bbmaj7", "A7", "A7"]), ("F", P), ("D3", P), ("A3", P),
        ("T", P)]
LOOP_BARS = sum(len(c) for _, c in PLAN)
# stab filter cutoff (Hz) per section; a function of the bar for the opening/closing ones
CUT = {"A": lambda j: 800.0 + 100 * j, "B": lambda j: 1500.0, "B2": lambda j: 1700.0, "C": lambda j: 1100.0,
       "D": lambda j: 2300.0, "D2": lambda j: 2500.0, "E": lambda j: 900.0,
       "F": lambda j: 900.0 + 200 * j, "D3": lambda j: 2600.0, "A3": lambda j: 1800.0,
       "T": lambda j: 1800.0 - 1000.0 * j / 7}
B2_STABS = B_STABS + [(7, 0.35)]
BASE_GROUPS = ("kick", "bass", "pad", "tribal", "air")
FULL_GROUPS = BASE_GROUPS + ("drums", "hats", "stab", "perc", "fx")


@lru_cache(None)
def v_stab(freq, dur, cutoff=1400.0):
    """The sketch's dub-techno stab (two detuned saws, a low-pass closing on each hit) with less resonance
    and a slower filter envelope: a chord, not a bloop."""
    n = ns(dur + 0.3)
    x = saw(freq * 1.003, n) + saw(freq * 0.997, n, start=0.3)
    x = svf(x, cutoff * (0.35 + exp_decay(n, 0.08)), q=1.1)
    return x * adsr(n, 0.002, 0.12, 0.25, 0.15, gate=dur) * 0.6


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def motif_bars():
    """[(loop bar, in the breakdown)] of the motif statements."""
    at, out = starts(), []
    for kind, _ in PLAN:
        out.append((at[kind] + (2 if kind == "E" else 0), kind == "C"))  # E starts on Gm9: its Dm9 is bar 2
        if kind in ("C", "D3"):
            out.append((at[kind] + 4, kind == "C"))
    return out


def grey_signal():
    s = LoopSong(125, 4, LOOP_BARS, swing=0.1, xf_steps=XF_STEPS)
    kicks = []
    for bar in range(4):  # intro: the sketch's
        voicing, root, _ = B_CH["Dm9"]
        for st, g in B_STABS:
            s.chord("stab", v_stab, voicing, bar, st, dur_steps=1, gain=g * 0.8, spread=0.5,
                    cutoff=500.0 + 200 * bar)
        bass_line(s, "bass", v_sub, root, bar, B_SUB, 0.5)
        if bar >= 2:
            for st in (0, 4, 8, 12):
                s.hit("kick", kick(95), bar, st, 0.85)
                kicks.append(s.pos(bar, st))
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.22)

    for base in s.sections():
        off = 0
        for kind, chords in PLAN:
            for j, name in enumerate(chords):
                bar = base + off + j
                voicing, root, pad = B_CH[name]
                dust = kind == "C"
                storm = kind == "E"
                build = kind == "F"
                if j == 0 and kind in ("A", "B", "C", "D", "D2", "D3", "A3"):
                    s.hit("perc", crash(), bar, 0, 0.35 if kind != "D3" else 0.42, 0.3)
                kit = not dust
                rims = kit and not (build and j < 2)
                hats = kit and not storm and not (build and j < 4)
                open_hats = kind in ("D", "D2", "D3") or (kind == "T" and j < 4)
                if kit:
                    for st in (0, 4, 8, 12):
                        s.hit("kick", kick(95), bar, st, 0.95 if not storm else 0.85)
                        kicks.append(s.pos(bar, st))
                if rims:
                    for st in (4, 12):
                        s.hit("drums", rim(), bar, st, 0.45, 0.15)
                    s.hit("drums", rim(), bar, 7, 0.18, -0.2)
                    if kind in ("D2", "D3"):
                        s.hit("drums", rim(), bar, 15, 0.14, 0.25)
                if hats:
                    for st in range(16):
                        s.hit("hats", hat(), bar, st, 0.1 if st % 2 else 0.06, 0.3)
                    if open_hats:
                        for st in (2, 6, 10, 14):
                            s.hit("hats", hat(True), bar, st, 0.18, -0.25)
                if dust and j == 7:  # the sketch's kick fill back in
                    for st in (8, 10, 12, 14, 15):
                        s.hit("kick", kick(95), bar, st, 0.4 + 0.08 * (st - 8))
                        kicks.append(s.pos(bar, st))
                if build and j == 7:  # a rimshot roll into the climax
                    for st in range(8, 16):
                        s.hit("drums", rim(), bar, st, 0.12 + 0.04 * (st - 8), 0.05)
                if build and j == 6:
                    s.hit("fx", riser(s.bar * 2), bar, 0, 0.22)
                # hand drums (track 8's doum and tek): from B on, not in A, out in T's second half
                hands = (kind in ("B", "B2", "D", "D2", "E", "F", "D3", "A3") or (dust and j >= 6)
                         or (kind == "T" and j < 4))
                if hands:
                    fwd = 1.3 if storm else 1.0
                    for st, k in B_TRIBAL:
                        s.hit("tribal", doum() if k == "d" else tek(), bar, st,
                              (0.38 if k == "d" else 0.22) * fwd, -0.25 if k == "d" else 0.3)
                for st in range(16):
                    s.hit("tribal", shaker(), bar, st, 0.05 if st % 2 else 0.08, -0.4)
                # chord stabs
                cut = round(CUT[kind](j), -1)
                for st, g in (B2_STABS if kind == "B2" else B_STABS):
                    s.chord("stab", v_stab, voicing, bar, st, dur_steps=1, gain=g, spread=0.5, cutoff=cut)
                # sub: the sketch's line, held notes in the breakdown and the dust
                if dust or storm:
                    s.hit("bass", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.45)
                else:
                    bass_line(s, "bass", v_sub, root, bar, B_SUB, 0.55)
                pad_gain = 0.35 if (dust or storm) else 0.28 if kind == "B2" else 0.22
                s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=pad_gain, bright=650.0)
            off += len(chords)
        # the intercepted signal (its own groups: the breakdown's statements get their own level)
        for lb, in_c in motif_bars():
            g = "motif_c" if in_c else "motif"
            s.play(g, v_whistle, vorne_motif("D4"), base + lb, gain=0.5, position=0.25)
            s.hit(g + "_static", static_swell(round(2 * s.bar, 4), 3681 + lb), base + lb, 0, 0.3, 0.25)

    at = starts()
    lb = LOOP_BARS
    c0, e0 = at["C"], at["E"]
    kit_cut = [(-4, 12000.0), (e0, 12000.0), (e0 + 0.01, 700.0), (e0 + 7, 700.0), (e0 + 8, 12000.0),
               (lb, 12000.0)]
    air = [(-4, 1.0), (-2, 0.7), (0, 0.4), (c0, 0.4), (c0 + 1, 1.0), (c0 + 7, 1.0), (c0 + 8, 0.4),
           (e0, 0.4), (e0 + 1, 0.9), (e0 + 7, 0.9), (e0 + 8, 0.4), (lb, 0.4)]
    return s, mix_groups(s, kicks, kit_cut, air)


def mix_groups(s, kicks, kit_cut, air_points):
    """Each group through the sketch's chain (music_r03.mixdown's per-group steps, same seeds), scaled by
    its gain: {group: stereo}. All linear: a stem is the sum of its groups."""
    tl = s.tl
    n = tl.n
    duck = sidechain(n, kicks, depth=0.4, release=0.12)
    cut = s.curve(kit_cut)

    def kit(x):
        return np.vstack([svf(ch, cut, q=0.9) for ch in hp(x, 35)]) * 0.85

    def motif(group):
        x = tl.group(group) + tl.group(group + "_static")
        return reverb(bandpass(x, 700.0, 2600.0), seconds=3.5, mix=0.45, seed=3682)[:, :n]

    return {
        "kick": kit(tl.group("kick")),
        "drums": kit(tl.group("drums")),
        "hats": hp(tl.group("hats"), 400) * 0.5,
        "perc": reverb(hp(tl.group("perc"), 38), seconds=2.6, mix=0.25, damping=0.8, seed=3683)[:, :n] * 0.55,
        "tribal": reverb(hp(tl.group("tribal"), 60), seconds=1.4, mix=0.25, seed=3684)[:, :n] * 0.6,
        "fx": tl.group("fx") * 0.7,
        "bass": hp(tl.group("bass"), 30) * duck * 0.7,
        "stab": widen(reverb(delay(hp(tl.group("stab"), 140), s.step * 3, feedback=0.5, mix=0.35, damp=2200,
                                   tail=0)[:, :n], seconds=3.2, mix=0.35, seed=3685)[:, :n], 1.4) * 0.85,
        "pad": widen(reverb(hp(tl.group("pad"), 90) * duck, seconds=3.5, mix=0.45, seed=3686)[:, :n], 1.6) * 0.4,
        "air": dust_wind(s, air_points, 3687) * 0.06,
        "motif": motif("motif"),
        "motif_c": motif("motif_c"),
    }


def segment_lufs(audio):
    """Integrated loudness (LUFS) of a stretch, by ffmpeg's ebur128 (400 ms blocks)."""
    with tempfile.TemporaryDirectory() as tmp:
        wav = Path(tmp) / "s.wav"
        write_wav(wav, audio)
        err = subprocess.run(["ffmpeg", "-hide_banner", "-nostats", "-i", str(wav), "-af", "ebur128",
                              "-f", "null", "-"], capture_output=True, text=True, check=True).stderr
    return float(re.findall(r"I:\s+(-?[\d.]+) LUFS", err)[-1])


def window(s, loop_bar, bars=2):
    """Samples of loop bars [loop_bar, loop_bar + bars) in the rendered second loop pass."""
    a = (s.intro + s.loop + loop_bar) * 16 * s.step_n
    return a, a + bars * 16 * s.step_n


def motif_offsets(s, full, motif_groups):
    """Mean level (dB) of each motif group's statements against the full mix in the same windows."""
    out = {}
    for g, in_c in (("motif", False), ("motif_c", True)):
        diffs = []
        for lb, c in motif_bars():
            if c != in_c:
                continue
            a, b = window(s, lb)
            diffs.append(segment_lufs(motif_groups[g][:, a:b]) - segment_lufs(full[:, a:b]))
        out[g] = float(np.mean(diffs))
    return out


def render():
    """One render; returns (song, full mix, base stem, motif stem) before mastering."""
    s, groups = grey_signal()
    full = sum(groups[g] for g in FULL_GROUPS)
    base = sum(groups[g] for g in BASE_GROUPS)
    offsets = motif_offsets(s, full, groups)
    motif = (groups["motif"] * undb(-MOTIF_UNDER - offsets["motif"])
             + groups["motif_c"] * undb(-MOTIF_UNDER_C - offsets["motif_c"]))
    print(f"motif calibration: raw {offsets['motif']:+.1f} dB (B..T), {offsets['motif_c']:+.1f} dB (C) vs the "
          f"full mix -> {-MOTIF_UNDER:.0f} / {-MOTIF_UNDER_C:.0f} dB", flush=True)
    return s, full, base, motif


def master_linked(files, ceiling_db=CEILING, target_lufs=-14.0, passes=3):
    """r11.master_linked for three files: the full mix's gain for all of them, each through the limiter."""
    full, *rest = files
    for _ in range(passes):
        gain = undb(target_lufs - lufs(full)[0])
        full = limiter(full * gain, ceiling_db)
        rest = [limiter(x * gain, ceiling_db) for x in rest]
    return [full, *rest]


def fade_ends(audio, seconds=0.5):
    k = int(seconds * SR)
    w = np.linspace(0, 1, k)
    audio = audio.copy()
    audio[:, :k] *= w
    audio[:, -k:] *= w[::-1]
    return audio


def tag_values(path):
    out = subprocess.run(["ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries",
                          "stream_tags", "-of", "default=nw=1", str(path)], capture_output=True, text=True,
                         check=True).stdout
    return dict(line.removeprefix("TAG:").split("=", 1) for line in out.splitlines() if "=" in line)


def sections_table(s, dec, i0):
    """[(section, bars, full LUFS, base LUFS, motif vs full dB)] over the decoded files' loop."""
    at, rows = starts(), []
    full, base, motif = dec
    bar_n = 16 * s.step_n
    for kind, chords in PLAN:
        a, b = i0 + at[kind] * bar_n, i0 + (at[kind] + len(chords)) * bar_n
        stmts = [lb for lb, _ in motif_bars() if at[kind] <= lb < at[kind] + len(chords)]
        under = [segment_lufs(motif[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n])
                 - segment_lufs(full[:, i0 + lb * bar_n:i0 + (lb + 2) * bar_n]) for lb in stmts]
        rows.append((kind, f"{at[kind] + 1}-{at[kind] + len(chords)}", segment_lufs(full[:, a:b]),
                     segment_lufs(base[:, a:b]), float(np.mean(under))))
    return rows


WAVE_W, WAVE_H = 1240, 90
INTRO, LOOP, TAIL = (120, 130, 150), (80, 200, 255), (90, 100, 120)


def waveform(img, x0, y0, audio, start, end, scale=1.0):
    d = ImageDraw.Draw(img)
    mono = audio.mean(axis=0) * scale
    n = len(mono)
    edges = np.linspace(0, n, WAVE_W + 1).astype(int)
    mid = y0 + WAVE_H // 2
    d.rectangle([x0, y0, x0 + WAVE_W - 1, y0 + WAVE_H - 1], fill=(10, 12, 20, 255))
    for col in range(WAVE_W):
        part = np.clip(mono[edges[col]:edges[col + 1]], -1, 1)
        colour = INTRO if edges[col] < start else LOOP if edges[col] < end else TAIL
        d.line([x0 + col, mid - int(part.max() * WAVE_H / 2), x0 + col, mid - int(part.min() * WAVE_H / 2)],
               fill=colour)
    for at in (start, end):
        x = x0 + int(at / n * WAVE_W)
        d.line([x, y0 - 4, x, y0 + WAVE_H + 3], fill=raster.ACCENT)


def review(out, rows, table, motif_peak):
    block = 40 + WAVE_H + 26
    table_h = 30 + 12 * (len(table) + 2)
    img = raster.sheet(WAVE_W + 40, 40 + block * len(rows) + table_h + 20,
                       "GREY SIGNAL - TRACK 9, ACT 3 B, AND ITS THREE STEMS (FULL LENGTH)",
                       "ROUND 41, M7 PART E - MUSIC_R41.PY - MEASURED ONLY")
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
        raster.draw_text(img, 20, y, f"{kind:<9} {bars:<7} {f:>8.1f}    {b:>8.1f}    {b - f:>+8.1f} LU    "
                         f"{m:>+8.1f} DB", raster.LABEL)
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
    full, base, motif = master_linked([full, base, motif])
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
        # the loop's first sample against the file's own continuation (the fade tail starts as a copy of
        # the loop start): how far the jump at the seam is from the step the music itself takes there
        end_ = loop_start + loop_len
        cont = np.abs((d[:, loop_start] - d[:, end_ - 1]) - (d[:, end_] - d[:, end_ - 1])).max()
        print(f"  seam vs continuation: {db(cont):.0f} dBFS difference", flush=True)
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
    # listening aids
    n = int(SEAM_SECONDS * SR)
    end = loop_start + loop_len
    for p, d in zip(paths, dec):
        seam = np.concatenate([d[:, end - n:end], d[:, loop_start:loop_start + n]], axis=1)
        aid = write_ogg(out / p.name.replace("-r41-a", "-seam-r41-a"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)
    bar_n = 16 * s.step_n
    a, b = loop_start + MIX_AID_BARS[0] * bar_n, loop_start + MIX_AID_BARS[1] * bar_n
    aid = write_ogg(out / f"{NAME}-motif-mix-r41-a.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {MIX_AID_BARS[0] + 1}-{MIX_AID_BARS[1]}, {(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, dec, loop_start)
    for kind, bars, f, bl, m in table:
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  motif {m:+.1f} dB",
              flush=True)
    print(f"review: {review(out, rows, table, motif_peak).name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
