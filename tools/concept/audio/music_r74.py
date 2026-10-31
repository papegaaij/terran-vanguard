#!/usr/bin/env python3
"""Concept round 74 music (M10 part B, step AUD-B): track 14 "Eye of the Storm" (the Act 6 A theme: Levels 36, 37 to
the turn, 39 and 40) at full length, with its three stems from one render, from the chosen round-03 sketch
(music_r03.py `jovian`, jovian-theme-r03-a: 140 BPM, G minor, the motif on G B-flat D F-sharp). Brief:
design/audio/music/README.md, *Act 6 music* (track 14: "its full length (about 3:00) and stems come with M10 part B
(Level 36) by the method of tracks 9-13 (the round-03 generator extended, one render, linked mastering, the loop
comments)").

The sketch has a 32-bar loop (A the hunt, B Vorne against the Coalition, C the storm, D both motifs), so the full length
is new arrangement on its material, in the method of music_r62.py (track 12): the sketch's voices, grooves, chords,
figures, mix chains and seeds, laid out as its 4-bar intro + a 96-bar loop (2:44.6) of twelve 8-bar sections
(A = Gm Eb Bb F Gm Eb Cm D; B = GmM7 GmM7 Eb Eb CmM7 CmM7 D D; C = Gm Ab Gm Ab Eb Cm D D; D = Gm GmF# Bb F Gm GmF#
Cm D; the drift E = Eb Eb Cm Cm Gm Gm D D):

  intro 4 bars  the storm strings swelling, a far thunder roll (bars 1 and 3), the wind, Vorne's motif in low brass
          (bars 1-2), humanity's motif answering in trumpets (bars 3-4), a timpani roll and a riser in bar 4 (the
          sketch's intro)
  A    A  the hunt: four-on-the-floor kick, clap and snare on the backbeat, 16th hats, the taiko pattern, tom fills
          every 4th bar, the gallop strings, the bass, the pad and the choir's chords; the main motif and its melody
          in horns with trumpets over it
  B    B  Vorne against the Coalition: the groove; Vorne's motif in low brass and choir (bars 1-2 on G, 5-6 on C),
          humanity's motif in major answering in trumpets (bars 3-4 on E-flat, 7-8 on D)
  A2   A  the hunt again
  C    C  the storm: bars 1-4 calm (no kit, a taiko on the one, a thunder roll every other bar, the bass held, the
          storm strings up, the choir's chords up), the choir's storm line (G to A-flat) over the whole section, the
          groove back in bars 5-8 with a riser and a snare roll in bars 7-8 (the sketch's C)
  D    D  both motifs: the groove, the main motif's melody in trumpets and the lead an octave up, Vorne's motif in
          low brass under it (bars 1-2 and 5-6) (the sketch's D)
  B2   B  B again
  E    E  drift (new): half time (the kick on 1 and 3, the snare on 3), no hats, no gallop, the bass held, the
          storm strings, the pad and the choir; humanity's motif alone in a quiet horn (bars 1-2 in E-flat major,
          5-6 in G minor)
  F    A  build (new): the kick alone, the hats from bar 3, the backbeat, taiko and the gallop strings from bar 5,
          a riser (bars 5-8) and a snare roll in bar 8
  D2   D  climax: D again
  B3   B  B again
  C2   C  the storm again
  T    A  turn: A's groove and melody, the hats out from bar 5, back into A

No bubbly or popping sounds and no resonant plucks (the sketch had none). The thunder is the sketch's thunder roll
**without its crack** (the brief: "the wind and thunder as band noise, no crackle"); the wind is the sketch's
band-passed wind under its own automation, made periodic over the loop (its seam would jump otherwise). Every group
goes through the sketch's own chain (music_r03.jovian's mixdown spec, same seeds, the kick ducking one fixed envelope),
all linear, so a stem is the exact sum of its groups.

Stems (the brief): one render, linked mastering (each loudness pass and the encoder's peak correction measure the full
mix and apply its gain to all three files; the limiter at CEILING), so the files share length, LOOPSTART / LOOPLENGTH
and gain and play sample-aligned:
  base   the storm strings (the tremolo strings, held under every bar: the storm's ostinato), the big drums' low half
         (the kick and the taiko), the bass, the pad, the wind and the thunder rolls
  full   the base plus the gallop strings, the clap, snare, toms and hats, crashes, timpani and risers, the main
         motif and its melody in horns and its trumpet answers, the lead, and the choir (its chords and the storm's
         line, the breakdown's choir)
  motif  Vorne's motif in low brass and choir alone (not part of the full mix; the game adds it on top as a third
         stream): **in the open**, its statements MOTIF_UNDER (6) dB under the full mix (EBU R128 over the statement
         windows, as track 12; set once before mastering), the B statements with their choir, D's in low brass
         alone (raised 4.4 dB over the sketch, D_LOW_BRASS, so they hold against D's tutti). Played alone it
         carries the Act 6 intro's page 3 (Vorne's intercepted invitation, `music: motif`); its first statement is in
         the intro.

Outputs (the --out directory, default design/audio/music/concept/):
  eye-of-the-storm-r74-a.ogg             the full mix (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH)
  eye-of-the-storm-base-r74-a.ogg        the base stem (same length, loop and gain)
  eye-of-the-storm-motif-r74-a.ogg       the motif stem (same length, loop and gain)
  eye-of-the-storm{,-base,-motif}-seam-r74-a.ogg   listening aids: the last 8 s of the loop, then the first 8 s after
                                         LOOPSTART (0.5 s fades at both ends)
  eye-of-the-storm-motif-mix-r74-a.ogg   listening aid: the full mix with the motif stem on top (as Level 36 plays
                                         them from section 3), loop bars 1-24 (A, B, A2)
  eye-of-the-storm-sheet-r74-a.png       review sheet: the files' waveforms, loudness, true peak, seam, size, and a
                                         per-section loudness table
No SOURCE comment: these are concept files; tools/art/themes.py copies them into assets/music with it.

Usage: python3 tools/concept/audio/music_r74.py [--out DIR]   (~5 min)
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
from music import clap, crash, hat, kick, ns, riser, snare, taiko, timpani, tom, transpose  # noqa: E402
from music import v_bass, v_brass, v_lead, v_pad, v_strings_short  # noqa: E402
from music_r02 import LoopSong, seam_ratio, v_choir, wind  # noqa: E402
from music_r03 import (DRUMS, GALLOP, JO_B_HIGH, JO_B_LOW, JO_CH, JO_MEL, JO_STORM, PERC, human_motif,  # noqa: E402
                       mixdown, v_tremolo, v_trumpet, vorne_motif)
from music_r41 import fade_ends, master_linked, segment_lufs, tag_values, waveform, window, WAVE_H, WAVE_W  # noqa: E402
from synth import SR, db, decode, fade, lufs, noise, note_freq, saturate, svf, undb, write_ogg  # noqa: E402
from render import raster  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
NAME = "eye-of-the-storm"
FULL_NAME, BASE_NAME, MOTIF_NAME = f"{NAME}-r74-a", f"{NAME}-base-r74-a", f"{NAME}-motif-r74-a"
SHEET = f"{NAME}-sheet-r74-a.png"
SEAM_SECONDS = 8.0
MIX_AID_BARS = (0, 24)  # loop bars of the motif-mix aid
MOTIF_UNDER = 6.0       # dB, the motif stem's statements under the full mix (the brief: about 6, in the open)
CEILING = -3.0          # dBFS, the linked limiter's ceiling (track 12's; the true-peak limit is -1 dBTP)

A = ["Gm", "Eb", "Bb", "F", "Gm", "Eb", "Cm", "D"]
B = ["GmM7", "GmM7", "Eb", "Eb", "CmM7", "CmM7", "D", "D"]
C = ["Gm", "Ab", "Gm", "Ab", "Eb", "Cm", "D", "D"]
D = ["Gm", "GmF#", "Bb", "F", "Gm", "GmF#", "Cm", "D"]
E = ["Eb", "Eb", "Cm", "Cm", "Gm", "Gm", "D", "D"]
PLAN = [("A", A), ("B", B), ("A2", A), ("C", C), ("D", D), ("B2", B), ("E", E), ("F", A), ("D2", D), ("B3", B),
        ("C2", C), ("T", A)]
LOOP_BARS = sum(len(c) for _, c in PLAN)
INTRO = ["Gm", "Gm", "Eb", "D"]
# D's low brass: the sketch's 0.6 put its statements 10 dB under D's tutti (B's, with their choir, 3 dB under: 7 dB
# apart around the -6 dB mean); 1.0 (+4.4 dB) brings the two nearer the brief's "about 6 dB under"
D_LOW_BRASS = 1.0
GROOVE_CRASH = ("A", "B", "A2", "D", "B2", "D2", "B3", "T")  # a crash and timpani on the section's one (the sketch's)
MELODY = ("A", "A2", "T")
BOTH = ("D", "D2")
VORNE = ("B", "B2", "B3")
STORM = ("C", "C2")
# the sketch's mix chains (music_r03.jovian's mixdown spec), one per group; the stems split the sketch's groups
FX_CHAIN = dict(hp=40, rev=(3.5, 0.35), seed=222, gain=0.8)
BRASS_CHAIN = dict(hp=60, rev=(2.8, 0.3), seed=227, gain=0.8)
CHOIR_CHAIN = dict(hp=140, rev=(3.8, 0.45), wid=1.6, seed=226, gain=0.55)
CHAINS = {
    # the sketch's "drums" group
    "kick": DRUMS, "snare": DRUMS, "hats": DRUMS,
    # its "perc" group
    "taiko": PERC, "perc": PERC,
    # its "fx" group
    "thunder": FX_CHAIN, "fx": FX_CHAIN,
    "ost": dict(hp=60, rev=(1.8, 0.2), seed=223, gain=0.55),
    "trem": dict(hp=90, rev=(3.5, 0.4), wid=1.5, seed=224, gain=0.5),
    "bass": dict(hp=40, duck=True, gain=0.6),
    "pad": dict(hp=120, duck=True, rev=(3.0, 0.35), wid=1.6, seed=225, gain=0.45),
    # its "choir" and "brass" groups, each split into the full mix's part and Vorne's (the motif stem)
    "choir": CHOIR_CHAIN, "vchoir": CHOIR_CHAIN,
    "horns": BRASS_CHAIN, "vbrass": BRASS_CHAIN,
    "lead": dict(hp=180, dly=(3, 0.3, 0.22, 4500), rev=(2.4, 0.25), wid=1.4, seed=228, gain=0.6),
    # the sketch's wind, added after the mixdown at 0.3
    "air": dict(gain=0.3),
}
BASE_GROUPS = ("kick", "taiko", "bass", "trem", "pad", "thunder", "air")
FULL_GROUPS = BASE_GROUPS + ("snare", "hats", "perc", "fx", "ost", "choir", "horns", "lead")
MOTIF_GROUPS = ("vbrass", "vchoir")


@lru_cache(None)
def thunder_roll(seed):
    """music_r03.thunder without its crack: the band-limited rumble only (the brief: no crackle)."""
    rng = np.random.default_rng(seed)
    n = ns(3.5)
    t = np.arange(n) / SR
    brown = np.cumsum(noise(n, rng))
    brown -= np.convolve(brown, np.ones(4096) / 4096, mode="same")
    brown /= np.max(np.abs(brown)) + 1e-9
    env = np.exp(-t / 1.1) * (0.6 + 0.4 * np.abs(np.sin(2 * np.pi * rng.uniform(1.5, 3) * t)))
    rumble = svf(brown, 220, q=0.8) * env * 2.0
    return fade(saturate(rumble, 1.3), 0.002, 0.3)


def looped_wind(s, xf_seconds=2.0):
    """The sketch's wind (music_r02.wind, seed 221) made periodic over the loop, so the seam has no jump: one loop's
    length with its last xf_seconds cross-faded into its start, tiled over the passes; the intro plays the loop's
    end, so it runs into the loop's start unbroken."""
    l_n, i_n = s.loop * 16 * s.step_n, s.intro * 16 * s.step_n
    x_n = int(xf_seconds * SR)
    raw = wind(l_n + x_n, seed=221)
    w = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, x_n))
    one = raw[:, :l_n].copy()
    one[:, :x_n] = raw[:, :x_n] * w + raw[:, l_n:] * (1 - w)
    reps = int(np.ceil((s.tl.n - i_n) / l_n)) + 1
    return np.concatenate([one[:, l_n - i_n:]] + [one] * reps, axis=1)[:, :s.tl.n]


def starts():
    out, off = {}, 0
    for kind, chords in PLAN:
        out[kind] = off
        off += len(chords)
    return out


def motif_bars():
    """Loop bars where a statement of Vorne's motif starts (for the calibration and the level table)."""
    at, out = starts(), []
    for kind, _ in PLAN:
        if kind in VORNE or kind in BOTH:
            out += [at[kind], at[kind] + 4]
    return out


def eye_of_the_storm():
    s = LoopSong(140, len(INTRO), LOOP_BARS)
    kicks = []
    # intro: the sketch's (the thunder without its crack)
    for bar, name in enumerate(INTRO):
        pad, _ = JO_CH[name]
        s.chord("trem", v_tremolo, pad, bar, dur_steps=16, gain=0.35 + 0.08 * bar, spread=0.7)
    s.hit("thunder", thunder_roll(1), 0, 0, 0.6, -0.3)
    s.hit("thunder", thunder_roll(2), 2, 4, 0.5, 0.4)
    s.play("vbrass", v_brass, vorne_motif("G3"), 0, gain=0.45)
    s.play("horns", v_trumpet, human_motif("Eb4", major=True), 2, gain=0.3, position=0.3)
    for st in range(8, 16):
        s.hit("perc", timpani("D2", 0.4), 3, st, 0.25 + 0.08 * (st - 8))
    s.hit("fx", riser(s.bar), 3, 0, 0.25)

    for base in s.sections():
        off = 0
        for kind, chords in PLAN:
            for j, name in enumerate(chords):
                bar = base + off + j
                pad, root = JO_CH[name]
                calm = kind in STORM and j < 4
                drift = kind == "E"
                build = kind == "F"
                if j == 0 and kind in GROOVE_CRASH:
                    s.hit("perc", crash(), bar, 0, 0.55, 0.3)
                    s.hit("perc", timpani("G1" if root in ("G1", "F#1") else "D2"), bar, 0, 0.8)
                if calm:
                    s.hit("taiko", taiko(), bar, 0, 0.7)
                    if j % 2 == 0:
                        s.hit("thunder", thunder_roll(3 + j + (8 if kind == "C2" else 0)), bar, 0, 0.6,
                              (-1) ** j * 0.4)
                elif drift:
                    for beat in (0, 2):
                        s.hit("kick", kick(), bar, beat * 4, 0.8)
                        kicks.append(s.pos(bar, beat * 4))
                    s.hit("snare", clap(), bar, 8, 0.4)
                    s.hit("snare", snare(), bar, 8, 0.3, 0.05)
                    s.hit("taiko", taiko(), bar, 0, 0.5)
                else:
                    for beat in range(4):
                        s.hit("kick", kick(), bar, beat * 4, 0.9)
                        kicks.append(s.pos(bar, beat * 4))
                    backbeat = not (build and j < 4)
                    if backbeat:
                        for st in (4, 12):
                            s.hit("snare", clap(), bar, st, 0.5)
                            s.hit("snare", snare(), bar, st, 0.35, 0.05)
                        for st in (0, 6, 10):
                            s.hit("taiko", taiko(), bar, st, 0.6)
                    if not (build and j < 2) and not (kind == "T" and j >= 4):
                        for st in range(16):
                            s.hit("hats", hat(), bar, st, 0.18 if st % 2 == 0 else 0.1, 0.3)
                    if j % 4 == 3 and backbeat:
                        for st, p in ((12, 160), (13, 140), (14, 110), (15, 90)):
                            s.hit("snare", tom(p), bar, st, 0.5, (st - 13.5) / 3)
                # snare rolls: the storm's bars 7-8 (the sketch's) and the build's last bar
                if (kind in STORM and j >= 6) or (build and j == 7):
                    k = j - 6 if kind in STORM else 1
                    for st in range(16):
                        s.hit("snare", snare(True), bar, st, 0.2 + 0.02 * st + 0.08 * k, 0.05)
                if (kind in STORM or build) and j == 4:
                    s.hit("fx", riser(s.bar * 4), bar, 0, 0.3)
                # the gallop strings and the bass (held in the calm and the drift)
                gallop = not calm and not drift and not (build and j < 4)
                if gallop:
                    for st, sm in enumerate(GALLOP):
                        s.hit("ost", v_strings_short(note_freq(transpose(root, 12 + sm)), round(s.step * 0.75, 4)),
                              bar, st, 0.4 if st % 3 == 0 else 0.26, -0.25)
                if not calm and not drift:
                    for st in range(16):
                        if st % 4:
                            nm = transpose(root, 12 if st % 4 == 3 else 0)
                            s.hit("bass", v_bass(note_freq(nm), round(s.step * 0.8, 4)), bar, st, 0.38)
                else:
                    s.hit("bass", v_bass(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.4)
                # the storm strings: the sketch's tremolo under every bar (up in the calm and the drift)
                s.chord("trem", v_tremolo, pad, bar, dur_steps=16, gain=0.55 if calm or drift else 0.3, spread=0.7)
                s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4, bright=2200.0)
                s.chord("choir", v_choir, pad[2:], bar, dur_steps=16, gain=0.45 if calm or drift else 0.3)
            # the motifs and the melody
            first = base + off
            if kind in MELODY:
                s.play("horns", v_brass, JO_MEL, first, gain=0.6, position=-0.1)
                s.play("horns", v_trumpet, JO_MEL, first, gain=0.25, position=0.15)
            if kind in VORNE:
                s.play("vbrass", v_brass, JO_B_LOW, first, gain=0.7, octave=-1)
                s.play("vbrass", v_brass, JO_B_LOW, first, gain=0.45)
                s.play("vchoir", v_choir, JO_B_LOW, first, gain=0.4)
                s.play("lead", v_trumpet, JO_B_HIGH, first, gain=0.55, position=0.2)
            if kind in STORM:
                s.play("choir", v_choir, JO_STORM, first, gain=0.55)
                s.play("choir", v_choir, JO_STORM, first + 4, gain=0.55)
            if kind in BOTH:
                s.play("horns", v_trumpet, JO_MEL, first, gain=0.55, position=0.15)
                s.play("lead", v_lead, JO_MEL, first, gain=0.4, octave=1)
                s.play("vbrass", v_brass, vorne_motif("G3"), first, gain=D_LOW_BRASS, octave=-1)
                s.play("vbrass", v_brass, vorne_motif("G3"), first + 4, gain=D_LOW_BRASS, octave=-1)
            if drift:
                s.play("horns", v_brass, human_motif("Eb4", major=True), first, gain=0.4, position=-0.2)
                s.play("horns", v_brass, human_motif("G4"), first + 4, gain=0.4, position=-0.2)
            off += len(chords)
    # the sketch's wind under its automation: up in the intro and in the storm's calm bars
    points = [(-4, 1.2), (0, 0.4)]
    for kind, at in starts().items():
        if kind in STORM:
            points += [(at, 0.4), (at + 0.01, 1.3), (at + 4, 1.3), (at + 6, 0.4)]
    points.append((LOOP_BARS, 0.4))
    s.tl.add("air", looped_wind(s) * s.curve(points), 0.0)
    groups = {g: mixdown(s, {g: spec}, kicks) for g, spec in CHAINS.items()}
    return s, groups


def render():
    """One render; returns (song, full mix, base stem, motif stem) before mastering."""
    s, groups = eye_of_the_storm()
    full = sum(groups[g] for g in FULL_GROUPS)
    base = sum(groups[g] for g in BASE_GROUPS)
    motif = sum(groups[g] for g in MOTIF_GROUPS)
    # the stem leaves the low brass out of the full mix, so the sketch's gains would put the motif above it: its
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
                       "EYE OF THE STORM - TRACK 14, ACT 6 A, AND ITS THREE STEMS (FULL LENGTH)",
                       "ROUND 74, M10 PART B - MUSIC_R74.PY - MEASURED ONLY")
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
        aid = write_ogg(out / p.name.replace("-r74-a", "-seam-r74-a"), fade_ends(seam), max_peak_db=None)
        print(f"aid: {aid.name}", flush=True)
    bar_n = 16 * s.step_n
    a, b = loop_start + MIX_AID_BARS[0] * bar_n, loop_start + MIX_AID_BARS[1] * bar_n
    aid = write_ogg(out / f"{NAME}-motif-mix-r74-a.ogg", fade_ends(both[:, a:b]), max_peak_db=None)
    print(f"aid: {aid.name} (loop bars {MIX_AID_BARS[0] + 1}-{MIX_AID_BARS[1]}, {(b - a) / SR:.1f} s)", flush=True)
    table = sections_table(s, dec, loop_start)
    for kind, bars, f, bl, m in table:
        motif_txt = f"{m:+.1f} dB" if m is not None else "-"
        print(f"  {kind:<3} bars {bars:<6} full {f:6.1f}  base {bl:6.1f} ({bl - f:+.1f})  motif {motif_txt}",
              flush=True)
    print(f"review: {review(out, rows, table, motif_peak).name}", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
