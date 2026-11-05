#!/usr/bin/env python3
"""Concept round 86 (M11 "Act 7 - Beyond the Gate", step MUS-R86, 2026-11-05): three Act 7 tracks recomposed with real
tunes after the user's round-85 notes (motif cells repeated and varied instead of melodies; sections that start in the
middle of a phrase; a tempo switch; the Choir's flat-2 motif over chords it clashes with); pending the user's review.
music_r84.py and music_r85.py stay as they are; this one imports their voices, tables and helpers.

Every tune is written first as a melody (the note lists below, one string per bar, 16 steps a bar): a main theme in
phrases that ask and answer, each ending on a cadence; a second theme with its own tune; sections that change only
where a phrase has ended; one tempo per track. A self-check (`--check`, also printed before each render) gives per
track the phrase map with its cadences, the share of bars carrying a motif cell, every section start and whether the
previous phrase cadenced there, and every strong-beat (beats 1 and 3) note that is not in the chord under it.

Outputs (design/audio/music/concept/, or --out DIR):
  act7-a-theme-r86-a.ogg  "Far Shore" (track 16, loops in Levels 43/45/48), 140 BPM, D minor
      Round 84 a's background rhythm (the breakbeat, the low string ostinato, the reese bass). Theme A, a 16-bar horn
      tune grown from the main motif (an 8-bar antecedent to a half cadence, an 8-bar consequent to a full cadence);
      theme B, the strings' own tune in F major (falling thirds, syncopated), in cellos and then violins with a horn
      counter-line; a climbing bridge onto A major; theme A at its peak (horns, trumpets, violins); a breakdown (theme
      B's first phrase in glass, then the Choir motif alone as a colour, over C - Bb - A, the chords it fits); a
      rebuild (theme A's consequent, the drums back layer by layer); a 2-bar turnaround onto A, back to bar 0. No
      Choir answer over the main motif.
  vorne-last-stand-r86-a.ogg  "Fused" (track 34, loops in Level 48 until Vorne dies), 150 BPM throughout, E minor
      vorne-last-stand-r85-a's opening kept as it was to 1:16 (intro, the statement, the march bridge, the breakdown,
      the build); then theme V, "Vorne the man" (a 16-bar lament in cellos and horn, then violins: a rising sixth and
      a falling sigh); a development on the first theme (Vorne's motif head in the horns answered by theme V's sigh in
      the violins, a rising sequence Am - Bm - C - Em, a run); the climax (his motif in full brass bending into the
      Choir's tritone drop over Em - F - Em, the chords it fits, then theme V's cadence in full); a turnaround (C - Am
      - F - B, a build) back to bar 0.
  ending-r86-a.ogg  "Stand Down, Aegis" (track 27, plays once), 75 BPM, D minor to D major
      ending-r85-a kept as it was to 1:04 (bar 20); there the D major statement's antecedent is answered by its
      consequent (bars 20-23, a full cadence on D); then the Choir motif at peace in its own phrase (bars 24-25, over
      Gm and E-flat major resolving to D, the chords its notes belong to) answered by the horn (26-27, G - D); then the
      open ending (28-30: D major over a low E-flat, faded).

Loops use round 72's format (intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH in samples); the ending is a
one-shot. -14 LUFS, -2 dBFS sample-peak ceiling, as rounds 84 and 85.

Usage: python3 tools/concept/audio/music_r86.py [a16|vls|end ...] [--out DIR] [--check]
Deterministic (fixed seeds); not listened to by Claude: checked by the self-check, loudness, the loop seam and a
spectrogram.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import (crash, hat, kick, ns, riser, snare, timpani, transpose,  # noqa: E402
                   v_brass, v_pad, v_strings_long, v_strings_short)
from music_r02 import GALLOP, LoopSong, add_tail, seam_ratio, v_choir, v_epiano, v_reese, v_sub  # noqa: E402
from music_r03 import mixdown, v_trumpet  # noqa: E402
from music_r60 import snare_roll  # noqa: E402
from music_r84 import (CHOIR_E4, FS_INTRO, FS_KICK, FS_SNARE, MAIN_D4, PEACE, VL_SNARE, v_glass, v_horn,  # noqa: E402
                       v_muted)
from music_r85 import (BR_A_CH, BR_A_TUNE, CLIMB_A, ENA_CH, MOTIF_ON, PCS, STATEMENT, STRAIN_A, STRAIN_D,  # noqa: E402
                       VLA_PLAN, _report, drop, drop_tokens, mname, open_ending, place_chords, timp_note, v_trombone, vc,
                       vl_bar, vl_mix, vl_render, vorne)
from synth import SR, db, decode, master, note_freq, note_number, parse_track, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"


# ================================================================ the score and its self-check

def chord_pcs(name):
    ch, _, bass = name.partition("/")
    root = ch[:2] if len(ch) > 1 and ch[1] in "#b" else ch[:1]
    ivs = {"": (0, 4, 7), "m": (0, 3, 7), "dim": (0, 3, 6), "7": (0, 4, 7, 10)}[ch[len(root):]]
    pcs = {(PCS[root] + i) % 12 for i in ivs}
    if bass:
        pcs.add(PCS[bass])
    return pcs


class Line:
    """A melody line: tokens from a bar, its doublings (group, voice, gain, position, octave, kw) and the bars (from
    its start) that carry a motif cell. kept: from an earlier take, checked but not recomposed."""

    def __init__(self, bar, toks, role, orch=(), motif=(), kept=False):
        self.bar, self.toks, self.role, self.orch, self.motif, self.kept = bar, toks, role, orch, motif, kept
        self.events = [(bar * 16 + st, ln, nm) for st, ln, nm in parse_track(toks)]


class Score:
    def __init__(self, name, chords, loop):
        self.name, self.chords, self.loop = name, chords, loop
        self.lines, self.phrases, self.sections = [], [], []

    def add(self, bar, toks, role, orch=(), motif=(), kept=False):
        self.lines.append(Line(bar, toks, role, orch, motif, kept))

    def phrase(self, first, last, section, label, lead=None, cadence=None, kept=False):
        """cadence: (kind, approach chord, final chord) checked against the chords under the lead's last note."""
        self.phrases.append((first, last, section, label, lead, cadence, kept))

    def chord_at(self, step):
        bar, st = divmod(step, 16)
        if self.loop:
            bar %= len(self.chords)
        if bar >= len(self.chords) or self.chords[bar] is None:
            return None
        ch = self.chords[bar]
        return ch[st * len(ch) // 16] if isinstance(ch, tuple) else ch

    def play(self, s, base):
        for ln in self.lines:
            for group, voice, gain, pos, octave, kw in ln.orch:
                kw = dict(kw)
                legato = kw.pop("legato", 0.92)
                s.play(group, voice, ln.toks, base + ln.bar, gain=gain, position=pos, octave=octave, legato=legato,
                       **kw)

    # ---------------------------------------------------------------- the check
    def ncts(self):
        """Strong-beat notes outside the chord: (line, step, note, chord, verdict)."""
        out = []
        for ln in self.lines:
            ev = ln.events
            for i, (a, length, nm) in enumerate(ev):
                pc = note_number(nm) % 12
                p = -(-a // 8) * 8
                while p < a + length:
                    ch = self.chord_at(p)
                    if ch and pc not in chord_pcs(ch):
                        nxt = ev[i + 1] if i + 1 < len(ev) else None
                        ok = (nxt is not None and abs(note_number(nxt[2]) - note_number(nm)) <= 2
                              and nxt[0] - p <= 8 and self.chord_at(nxt[0])
                              and note_number(nxt[2]) % 12 in chord_pcs(self.chord_at(nxt[0])))
                        kind = ("suspension" if a < p else "appoggiatura") + " resolved" if ok else "UNRESOLVED"
                        out.append((ln, p, nm, ch, kind))
                    p += 8
        return out

    def check(self, intro_bars=0, bar_s=1.0):
        print(f"== self-check: {self.name} ({len(self.chords)} bars{', loop' if self.loop else ', once'})")
        t = lambda b: (intro_bars + b) * bar_s  # noqa: E731
        ts = lambda x: f"{int(x // 60)}:{x % 60:04.1f}"  # noqa: E731
        print("  phrase map:")
        for first, last, sec, label, lead, cad, kept in self.phrases:
            res = ""
            if cad and lead:
                evs = [e for ln in self.lines if ln.role == lead for e in ln.events
                       if first * 16 <= e[0] < (last + 1) * 16]
                a, length, nm = max(e for e in evs if e[1] >= 4)  # the last note of 4+ steps (not a pickup)
                fin = self.chord_at(a)
                k = a - 1
                while k >= first * 16 and self.chord_at(k) == fin:
                    k -= 1
                app = self.chord_at(k) if k >= 0 or self.loop else None
                tone = note_number(nm) % 12 in chord_pcs(fin)
                good = (app, fin) == (cad[1], cad[2]) and tone and length >= 4
                res = (f"  {cad[0]} {app}>{fin}, last note {nm} ({length} steps, "
                       f"{'chord tone' if tone else 'NOT a chord tone'}) {'ok' if good else '<-- CHECK'}")
            elif not cad:
                res = "  (no cadence: accompaniment / fill)"
            print(f"    {first:3d}-{last:3d} {ts(t(first))}  {sec:12s} {label}{' [kept]' if kept else ''}{res}")
        n = len(self.chords)
        motif = {ln.bar + b for ln in self.lines for b in ln.motif if 0 <= ln.bar + b < n}
        kept_bars = {b for p in self.phrases if p[6] for b in range(p[0], p[1] + 1)}
        new_bars = set(range(n)) - kept_bars
        mk = sorted(motif)
        print(f"  motif share: {len(motif)}/{n} bars = {100 * len(motif) / n:.0f} %"
              f" (new material {len(motif & new_bars)}/{len(new_bars)} = "
              f"{100 * len(motif & new_bars) / max(1, len(new_bars)):.0f} %); bars {mk}")
        print("  section starts:")
        mid = 0
        starts = [b for b, _ in self.sections if b > 0] + ([n] if self.loop else [])
        for b in starts:
            ended = [p for p in self.phrases if p[1] == b - 1]
            across = [p for p in self.phrases if p[0] < b <= p[1]]
            notes = [(ln.role, nm) for ln in self.lines for a, length, nm in ln.events if a < b * 16 < a + length]
            okk = ended and not across and not notes
            mid += 0 if okk else 1
            lab = "loop seam -> bar 0" if b == n else next(lb for bb, lb in self.sections if bb == b)
            print(f"    bar {b:3d} {ts(t(b))}  {'after ' + ended[0][3] + ' (' + (ended[0][5][0] if ended[0][5] else 'fill') + ')' if ended else 'NO PHRASE ENDS HERE'}"
                  f"{'  MID-PHRASE ' + str([p[3] for p in across]) if across else ''}"
                  f"{'  NOTES HELD ACROSS ' + str(notes) if notes else ''}  :: {lab[:60]}")
        bad = self.ncts()
        unres_new = [x for x in bad if x[4] == "UNRESOLVED" and not x[0].kept]
        unres_kept = [x for x in bad if x[4] == "UNRESOLVED" and x[0].kept]
        print(f"  strong-beat non-chord tones: {len(bad)} ({len(bad) - len(unres_new) - len(unres_kept)} resolved),"
              f" unresolved in new material {len(unres_new)}, in kept material {len(unres_kept)}")
        for ln, p, nm, ch, kind in bad:
            print(f"    bar {p // 16:3d} beat {p % 16 // 4 + 1}  {ln.role:14s} {nm:4s} over {ch:6s} {kind}"
                  f"{' [kept]' if ln.kept else ''}")
        print(f"  sections starting mid-phrase: {mid}")
        return dict(motif=len(motif), bars=n, unresolved=len(unres_new), unresolved_kept=len(unres_kept), mid=mid)


# ================================================================ track 16: Far Shore (140 BPM, D minor)

# Theme A: the horn tune grown from the main motif (D A D E F). Antecedent: the motif rising to F, falling to A; a
# second, longer rise to A5 sighing down onto E over A (half cadence). Consequent: the motif again, a sequence a third
# higher (F E D / C), the climb to B-flat, down through G E C-sharp to D (full cadence).
A_ANT = ["D4 - - - - - A4 - D5 - - - - - E5 -", "F5 - - - - - - - - - - - G5 - E5 -",
         "D5 - - - - - - - - - - - C5 - Bb4 -", "A4 - - - - - - - - - - - . . . .",
         "G4 - - - - - A4 - Bb4 - - - - - C5 -", "D5 - - - - - - - G5 - - - - - F5 -",
         "A5 - - - - - - - G5 - - - - - F5 -", "E5 - - - - - - - - - - - . . . ."]
A_ANT_CH = ["Dm", "Dm", "Bb", "F", "Gm", "Gm", ("Dm", "Gm"), "A"]
A_CONS = ["D4 - - - - - A4 - D5 - - - - - E5 -", "F5 - - - - - - - G5 - - - - - E5 -",
          "F5 - - - - - - - - - - - E5 - D5 -", "C5 - - - - - - - - - - - . . . .",
          "Bb4 - - - - - D5 - G5 - - - - - A5 -", "Bb5 - - - - - - - A5 - - - G5 - F5 -",
          "G5 - - - - - - - E5 - - - - - C#5 -", "D5 - - - - - - - - - - - . . . ."]
A_CONS_CH = ["Dm", ("Dm", "C"), "Bb", "F", "Gm", ("Bb", "F"), ("Gm", "A"), "Dm"]
# Theme B: the strings' tune in F major: falling thirds stepping down, the second note off the beat (step 10), a
# half cadence on C, then the answer climbing to D6 and a full cadence on F.
B_TUNE = ["C6 - - - - - - - - - A5 - - - - -", "Bb5 - - - - - - - - - G5 - - - - -",
          "A5 - - - - - - - F5 - - - G5 - - -", "E5 - - - - - - - - - - - . . . .",
          "C6 - - - - - - - - - A5 - - - - -", "D6 - - - - - - - - - Bb5 - - - - -",
          "D6 - - - - - - - C6 - - - Bb5 - - -", "A5 - - - - - - - - - - - . . . ."]
B_CH = ["F", "Gm", "F/A", "C", "F", "Bb", ("Gm", "C"), "F"]
B_COUNTER = ["F4 - - - - - - - A4 - - - - - - -", "D5 - - - - - - - - - - - - - - -",  # the horn under B'
             "C5 - - - - - - - - - - - - - - -", "- - - - - - - - G4 - - - - - - -",
             "A4 - - - - - - - - - - - - - - -", "F4 - - - - - - - Bb4 - - - - - - -",
             "Bb4 - - - - - - - G4 - - - - - - -", "F4 - - - - - - - - - - - . . . ."]
BRIDGE = ["F4 - - - Bb4 - - - D5 - - - F5 - - -", "G4 - - - C5 - - - E5 - - - G5 - - -",  # a climbing sequence
          "A4 - - - C#5 - - - E5 - - - G5 - - -", "A5 - - - - - - - - - - - . . . ."]   # onto the dominant
BRIDGE_CH = ["Bb", "C", "A7", "A"]
TURN16 = ["F4 - - - - - - - D4 - - - - - - -", "E4 - - - - - - - C#4 - - - - - - -"]  # the C-sharp leads to D
FS86_CH = (A_ANT_CH + A_CONS_CH + B_CH + B_CH + BRIDGE_CH + A_ANT_CH + A_CONS_CH + B_CH[:4] + ["C", ("Bb", "A")]
           + A_CONS_CH + ["Bb", "A"])
FS86_SECTIONS = [(0, "A: theme A in the horn over the break and the ostinato (sparse), hats and strings join at 8"),
                 (16, "B: theme B in the cellos (F major), then the violins with a horn counter-line (24)"),
                 (32, "bridge: a climbing string sequence onto A, a snare build and a riser"),
                 (36, "A peak: theme A in horns, trumpets and violins, a string descant, all drums"),
                 (52, "breakdown: drums out, theme B's first phrase in glass, then the Choir motif alone (56)"),
                 (58, "rebuild: theme A's consequent, the kick (58), the break (60), hats and strings (62)"),
                 (66, "turnaround: Bb - A, the horn's line onto bar 0, the break alone")]

HORN1 = [("horn", v_horn, 0.55, 0.15, 0, {}), ("horn", v_brass, 0.18, -0.15, 0, {})]
HORN2 = HORN1 + [("horn", v_horn, 0.38, -0.2, -1, {})]
PEAK = [("horn", v_brass, 0.45, 0.15, 0, {}), ("horn", v_horn, 0.42, -0.15, -1, {}),
        ("tpt", v_trumpet, 0.22, 0.0, 0, {}), ("tune", v_strings_long, 0.3, -0.25, 1, dict(attack=0.08)),
        ("tune", v_strings_long, 0.22, 0.25, 0, dict(attack=0.08))]


def far_shore_score():
    sc = Score("act7-a-theme-r86-a (Far Shore)", FS86_CH, loop=True)
    sc.sections = FS86_SECTIONS
    sc.add(0, A_ANT, "horn A", HORN1, motif=(0, 1))
    sc.add(8, A_CONS, "horn A", HORN2, motif=(0, 1))
    sc.add(16, B_TUNE, "strings B", [("tune", v_strings_long, 0.55, -0.2, -2, dict(attack=0.25)),
                                     ("tune", v_strings_long, 0.32, 0.2, -1, dict(attack=0.25))])
    sc.add(24, B_TUNE, "strings B", [("tune", v_strings_long, 0.45, 0.2, 0, dict(attack=0.2)),
                                     ("tune", v_strings_long, 0.32, -0.2, -1, dict(attack=0.2))])
    sc.add(24, B_COUNTER, "horn counter", [("horn", v_horn, 0.42, -0.1, 0, {})])
    sc.add(32, BRIDGE, "strings bridge", [("tune", v_strings_long, 0.45, 0.2, 0, dict(attack=0.06)),
                                          ("tune", v_strings_long, 0.35, -0.2, -1, dict(attack=0.06))])
    sc.add(36, A_ANT, "horn A", PEAK, motif=(0, 1))
    sc.add(44, A_CONS, "horn A", PEAK, motif=(0, 1))
    sc.add(52, B_TUNE[:4], "glass B", [("glass", v_glass, 0.6, 0.15, -1, dict(legato=0.97))])
    sc.add(56, CHOIR_E4, "choir colour", [("choir", v_choir, 0.55, -0.1, 0, dict(legato=0.95)),
                                          ("choir", v_choir, 0.35, 0.1, -1, dict(legato=0.95, vowel="oo"))],
           motif=(0, 1))
    sc.add(58, A_CONS[:4], "horn A", HORN1, motif=(0, 1))
    sc.add(62, A_CONS[4:], "horn A", HORN2 + [("tune", v_strings_long, 0.25, 0.25, 1, dict(attack=0.08))])
    sc.add(66, TURN16, "horn turn", [("horn", v_horn, 0.5, 0.1, 0, {})])
    P = sc.phrase
    P(0, 7, "A", "theme A antecedent (horn)", "horn A", ("HC", "Gm", "A"))
    P(8, 15, "A", "theme A consequent (horns)", "horn A", ("PAC", "A", "Dm"))
    P(16, 19, "B", "theme B antecedent (cellos)", "strings B", ("HC", "F/A", "C"))
    P(20, 23, "B", "theme B consequent (cellos)", "strings B", ("PAC", "C", "F"))
    P(24, 27, "B", "theme B antecedent (violins, horn counter)", "strings B", ("HC", "F/A", "C"))
    P(28, 31, "B", "theme B consequent (violins, horn counter)", "strings B", ("PAC", "C", "F"))
    P(32, 35, "bridge", "climbing sequence onto the dominant", "strings bridge", ("HC", "A7", "A"))
    P(36, 43, "A peak", "theme A antecedent (tutti)", "horn A", ("HC", "Gm", "A"))
    P(44, 51, "A peak", "theme A consequent (tutti)", "horn A", ("PAC", "A", "Dm"))
    P(52, 55, "breakdown", "theme B antecedent (glass)", "glass B", ("HC", "F/A", "C"))
    P(56, 57, "breakdown", "the Choir motif alone over C - Bb - A", "choir colour", ("Phrygian HC", "Bb", "A"))
    P(58, 65, "rebuild", "theme A consequent (horns)", "horn A", ("PAC", "A", "Dm"))
    P(66, 67, "turnaround", "the horn's line onto bar 0", "horn turn", ("HC", "Bb", "A"))
    return sc


def root_note(chord):
    """The chord's root (not a slash bass) in E1..D#2, as vc()'s bass."""
    name = chord.partition("/")[0]
    r = name[:2] if len(name) > 1 and name[1] in "#b" else name[:1]
    return mname(next(m for m in range(28, 40) if m % 12 == PCS[r]))


def ostinato_semis(chord):
    q = chord.partition("/")[0]
    seventh = 10 if q.endswith("m") or q.endswith("7") or q in ("A", "C") else 11
    return (0, 0, 12, 0, 7, 0, 12, seventh)


def fs_groove(s, kicks, bar, chord, kick_on=True, snare_on=True, hats=0, ost=True, bass=True, pad=None,
              strings=0.0, lvl=1.0):
    """One bar of round 84's groove; lvl scales the drums, the ostinato and the bass (the build-up's dynamics)."""
    halves = chord if isinstance(chord, tuple) else (chord, chord)
    if kick_on:
        for st, g in FS_KICK:
            s.hit("drums", kick(130), bar, st, g * lvl)
            kicks.append(s.pos(bar, st))
    if snare_on:
        for st, g in FS_SNARE:
            s.hit("drums", snare(True), bar, st, g * lvl, 0.05)
    if hats:
        for st in range(16):
            s.hit("hats", hat(hats > 1 and st % 8 == 6), bar, st, 0.14 if st % 2 == 0 else 0.07, 0.3)
    for st in range(0, 16, 2):
        ch = halves[st // 8]
        root = root_note(ch)
        if ost:
            semis = ostinato_semis(ch)[st // 2]
            s.hit("ost", v_strings_short(note_freq(transpose(root, 12 + semis)), round(s.step * 1.6, 4)), bar, st,
                  (0.42 if st % 4 == 0 else 0.32) * lvl, -0.2)
    for h, ch in enumerate(halves if halves[0] != halves[1] else halves[:1]):
        voicing, root = vc(ch)
        st0, ln = (h * 8, 8) if halves[0] != halves[1] else (0, 16)
        if bass:
            s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * (ln - 0.5), 4)), bar, st0, 0.36 * lvl)
            if ln == 16:
                s.hit("bass", v_reese(note_freq(transpose(root, 12)), round(s.step * 5.5, 4)), bar, 10, 0.3 * lvl)
        if pad:
            s.chord("pad", v_pad, voicing, bar, st0, ln, gain=pad, spread=0.7)
        if strings:
            s.chord("pad", v_strings_long, voicing, bar, st0, ln, gain=strings, spread=0.7)


def descant(s, bar, chord, gain=0.12):
    """8th-note string figuration on the chord's own tones, an octave above the voicing."""
    halves = chord if isinstance(chord, tuple) else (chord, chord)
    for st in range(0, 16, 2):
        v = vc(halves[st // 8])[0]
        s.hit("desc", v_strings_short(note_freq(transpose(v[(st // 2) % len(v)], 12)), round(s.step * 1.5, 4)),
              bar, st, gain, 0.35 - 0.1 * ((st // 2) % 4))


def far_shore_r86():
    sc = far_shore_score()
    s = LoopSong(140, 4, len(FS86_CH))
    kicks = []
    for bar, name in enumerate(FS_INTRO):  # round 84's intro: the ostinato alone, a pad swelling, the break from 2
        voicing, root = vc(name)
        for st in range(0, 16, 2):
            s.hit("ost", v_strings_short(note_freq(transpose(root, 12)), round(s.step * 1.6, 4)), bar, st,
                  (0.3 + 0.05 * bar) * 0.7)
        s.chord("pad", v_pad, voicing, bar, gain=0.15 + 0.05 * bar, spread=0.7)
        if bar >= 2:
            for st, g in FS_KICK:
                s.hit("drums", kick(130), bar, st, g * 0.6)
                kicks.append(s.pos(bar, st))
            for st, g in FS_SNARE:
                s.hit("drums", snare(True), bar, st, g * 0.5, 0.05)
    s.hit("fx", riser(s.bar * 2), 2, 0, 0.2)
    for base in s.sections():
        sc.play(s, base)
        for i, ch in enumerate(FS86_CH):
            bar = base + i
            if i < 8:        # A antecedent: the break, the ostinato, the bass, a soft synth pad
                fs_groove(s, kicks, bar, ch, pad=0.2, lvl=0.6)
            elif i < 16:     # A consequent: + hats, + strings
                fs_groove(s, kicks, bar, ch, hats=1, pad=0.14, strings=0.18, lvl=0.75)
            elif i < 32:     # B: the groove under the strings' tune; open hats and timpani from B'
                fs_groove(s, kicks, bar, ch, hats=2 if i >= 24 else 1, strings=0.14 if i < 24 else 0.2,
                          lvl=0.8 if i < 24 else 0.9)
                if i % 4 == 3 or i == 24:
                    s.hit("perc", timpani(timp_note(vc(ch if isinstance(ch, str) else ch[0])[1])), bar, 0, 0.5)
            elif i < 36:     # bridge: horn stabs on the chords, a snare build, a timpani roll
                fs_groove(s, kicks, bar, ch, hats=2, strings=0.22, lvl=0.95)
                for nm in vc(ch)[0][-3:]:
                    for st in (0, 6, 8):
                        s.hit("horn", v_brass(note_freq(nm), round(s.step * 1.6, 4), True), bar, st, 0.1, 0.0)
                if i == 35:
                    for st in range(16):
                        s.hit("drums", snare(True), bar, st, 0.1 + 0.035 * st, 0.05)
                        s.hit("perc", timpani("A1"), bar, st, 0.08 + 0.025 * st)
            elif i < 52:     # A peak: everything, the descant, timpani every second bar
                fs_groove(s, kicks, bar, ch, hats=2, strings=0.26, lvl=1.15)
                descant(s, bar, ch)
                if i % 2 == 0:
                    s.hit("perc", timpani(timp_note(vc(ch if isinstance(ch, str) else ch[0])[1])), bar, 0, 0.55)
            elif i < 58:     # breakdown: drums out, the pad and the sub
                fs_groove(s, kicks, bar, ch, kick_on=False, snare_on=False, ost=False, bass=False, pad=0.38)
                root = vc(ch if isinstance(ch, str) else ch[0])[1]
                s.hit("sub", v_sub(note_freq(transpose(root, 12)), round(s.step * 15, 4)), bar, 0, 0.4)
            elif i < 66:     # rebuild: the kick and the ostinato, the break from 60, hats and strings from 62
                fs_groove(s, kicks, bar, ch, snare_on=i >= 60, hats=1 if i >= 62 else 0, pad=0.2,
                          strings=0.2 if i >= 62 else 0.0, lvl=0.55 if i < 60 else 0.75 if i < 62 else 0.95)
                if i in (62, 64):
                    s.hit("perc", timpani(timp_note(vc(ch if isinstance(ch, str) else ch[0])[1])), bar, 0, 0.5)
            else:            # turnaround: the break and the ostinato, the pad (as the intro's last bars)
                fs_groove(s, kicks, bar, ch, pad=0.25, lvl=0.65)
                if i == 67:
                    for st in (12, 13, 14, 15):
                        s.hit("drums", snare(True), bar, st, 0.25 + 0.08 * (st - 12), 0.05)
        for b in (0, 16, 24, 36, 44, 62):
            s.hit("perc", crash(), base + b, 0, 0.4, 0.3)
        s.hit("fx", riser(s.bar * 2), base + 34, 0, 0.2)
        s.hit("fx", riser(s.bar * 2), base + 56, 0, 0.12)
    mix = mixdown(s, {
        "drums": dict(hp=32, gain=0.85),
        "hats": dict(hp=700, gain=0.35),
        "perc": dict(hp=35, rev=(2.6, 0.3), damp=0.8, seed=8401, gain=0.7),
        "fx": dict(gain=0.5),
        "ost": dict(hp=60, duck=True, rev=(1.6, 0.2), seed=8402, gain=0.55),
        "bass": dict(hp=30, duck=True, gain=0.5),
        "sub": dict(hp=25, gain=0.5),
        "pad": dict(hp=90, duck=True, rev=(3.2, 0.4), wid=1.5, seed=8403, gain=0.45),
        "horn": dict(hp=70, rev=(2.8, 0.3), seed=8404, gain=0.8),
        "tpt": dict(hp=120, rev=(2.8, 0.3), seed=8661, gain=0.7),
        "tune": dict(hp=60, rev=(3.0, 0.35), wid=1.3, seed=8662, gain=0.75),
        "desc": dict(hp=200, duck=True, rev=(2.4, 0.3), wid=1.6, seed=8663, gain=0.5),
        "glass": dict(hp=150, rev=(4.0, 0.45), wid=1.4, seed=8664, gain=0.8),
        "choir": dict(hp=110, rev=(4.0, 0.45), wid=1.5, seed=8405, gain=0.6),
    }, kicks, depth=0.4)
    secs = [(-4, "intro: the ostinato alone, a pad swelling, the break from its bar 2 (round 84's)")] + FS86_SECTIONS
    return s, mix, secs, sc


# ================================================================ track 34: Vorne's last stand (150 BPM, E minor)

# Theme V, "Vorne the man": a lament. A rising sixth (B to G), a falling sigh (F-sharp E D-sharp), a reach up to C6
# (the flat 6) and a long fall to a half cadence on B; the consequent rises a fourth, reaches D6 over D7 (a deceptive
# turn to Em) and falls through C6 A5 G5 F-sharp5 to E (full cadence).
V_ANT = ["B4 - - - - - - - G5 - - - - - - -", "F#5 - - - - - E5 - D#5 - - - - - - -",
         "E5 - - - - - - - G5 - - - - - - -", "C6 - - - - - - - B5 - - - A5 - - -",
         "G5 - - - - - - - - - - - F#5 - - -", "E5 - - - - - - - C5 - - - - - - -",
         "A4 - - - - - - - - - - - C5 - - -", "B4 - - - - - - - - - - - . . . ."]
V_ANT_CH = ["Em", "B7", "C", ("Am", "B7"), "Em/G", "Am", ("Am", "F#dim"), "B"]
V_CONS = ["B4 - - - - - - - G5 - - - - - - -", "F#5 - - - - - E5 - D#5 - - - - - - -",
          "E5 - - - - - - - A5 - - - - - - -", "D6 - - - - - - - - - - - C6 - B5 -",
          "B5 - - - - - - - G5 - - - - - E5 -", "C6 - - - - - - - A5 - - - - - - -",
          "G5 - - - - - - - F#5 - - - - - - -", "E5 - - - - - - - - - - - . . . ."]
V_CONS_CH = ["Em", "B7", ("C", "Am"), "D7", ("Em", "C"), "Am", ("Em/B", "B7"), "Em"]
# Development: the head of Vorne's motif (1 b3 5, the first theme) in the horns, answered by theme V's sigh in the
# violins, the pair climbing a step each time (Am - B7, Bm - C, C - D), the trumpets' head on Em and a run onto B.
HEAD = {"Am": "A3 - - - - - C4 - E4 - - - - - - -", "Bm": "B3 - - - - - D4 - F#4 - - - - - - -",
        "C": "C4 - - - - - E4 - G4 - - - - - - -", "Em": "E4 - - - - - G4 - B4 - - - - - - -"}
SIGH = {"B7": "F#5 - - - - - E5 - D#5 - - - - - - -", "C": "G5 - - - - - F5 - E5 - - - - - - -",
        "D": "A5 - - - - - G5 - F#5 - - - - - - -"}
RUN = "B4 - C#5 - D#5 - E5 - F#5 - G5 - A5 - - -"
DEV_CH = ["Am", "B7", "Bm", "C", "C", "D", "Em", "B7"]
# Climax: his motif in full brass (E G B, D-sharp bending down) under the violins' held B, the Choir's tritone drop
# (B over Em, F over F, E over Em: a Phrygian cadence), then theme V's cadence in the full orchestra.
VORNE_E = ["E3 - - - - - G3 - B3 - - - - - - -", "D#4 - - - - - - - - - - - - - . ."]  # vorne(..., "E3"): stately
VORNE_AUG = ["E3 - - - - - - - - - - - G3 - - -", "B3 - - - - - - - - - - - - - - -",
             "D#4 - - - - - - - - - - - - - - -", "- - - - - - - - - - - - . . . ."]
V_END = ["B5 - - - - - - - G5 - - - - - E5 -", "C6 - - - - - - - A5 - - - - - - -",
         "G5 - - - - - - - F#5 - - - - - - -", "E5 - - - - - - - - - - - . . . ."]
CLIMAX_CH = ["Em", "B", ("Em", "F"), "Em", ("Em", "C"), "Am", ("Em/B", "B7"), "Em"]
TURN34 = ["G4 - - - - - - - E4 - - - - - - -", "A4 - - - - - - - C5 - - - - - - -",
          "- - - - - - - - A4 - - - - - - -", "B4 - - - - - - - - - - - - - - -"]
TURN34_CH = ["C", "Am", "F", "B"]
KEPT_CH = [ch for first, chords, _, _ in VLA_PLAN[:4] for ch in chords]  # r85 a's bars 0-43
VL86_CH = KEPT_CH + V_ANT_CH + V_CONS_CH + DEV_CH + CLIMAX_CH + TURN34_CH
VL86_TEX = ["lyric"] * 8 + ["full"] * 24 + ["break"] * 2 + ["build"] * 2
VL86_SECTIONS = [(0, "A statement (r85 a, kept): Vorne's motif in cold brass at 0 and 8, the choir's drop at 2 and 10"),
                 (16, "B bridge (r85 a, kept): the march tune, trumpets (16-23), horns with a string descant (24-31)"),
                 (32, "C breakdown (r85 a, kept): his motif augmented in a muted horn over the choir (34, 38)"),
                 (42, "C build (r85 a, kept): timpani crescendo and snare roll"),
                 (44, "D theme V, Vorne the man: the lament in cellos and horn (44), then violins (52)"),
                 (60, "E development: his motif's head (horns) answered by the sigh (violins), climbing; a run"),
                 (68, "F climax: his motif in full brass bending into the Choir's drop (70), theme V's cadence (72)"),
                 (76, "turnaround: C - Am - F - B in the horn, the drums out, then the build back to bar 0")]

CELLO_HORN = [("tune", v_strings_long, 0.5, -0.15, -1, dict(attack=0.12)), ("tune", v_horn, 0.32, 0.15, -1, {})]
VIOLINS = [("tune", v_strings_long, 0.45, -0.1, 0, dict(attack=0.12)),
           ("tune", v_strings_long, 0.3, 0.1, -1, dict(attack=0.12)), ("vorne", v_horn, 0.36, 0.15, -1, {})]
TUTTI = VIOLINS + [("vorne", v_trumpet, 0.16, 0.0, 0, {})]


def last_stand_score():
    sc = Score("vorne-last-stand-r86-a (Fused)", VL86_CH, loop=True)
    sc.sections = VL86_SECTIONS
    # kept (r85 a), for the check only: rendered by r85's own calls below
    for k in (0, 8):
        sc.add(k, VORNE_E, "vorne", motif=(0, 1), kept=True)
        sc.add(k + 2, drop_tokens("E3"), "drop", motif=(0, 1), kept=True)
    sc.add(16, BR_A_TUNE, "march tune", kept=True)
    sc.add(34, VORNE_AUG, "vorne aug", motif=(0, 1, 2, 3), kept=True)
    sc.add(38, VORNE_AUG, "vorne aug", motif=(0, 1, 2, 3), kept=True)
    sc.add(40, drop_tokens("E3"), "drop", motif=(0, 1), kept=True)
    # new
    sc.add(44, V_ANT, "theme V", CELLO_HORN)
    sc.add(52, V_CONS, "theme V", VIOLINS)
    for k, (h, sg) in enumerate((("Am", "B7"), ("Bm", "C"), ("C", "D"))):
        sc.add(60 + 2 * k, [HEAD[h]], "head", [("vorne", v_horn, 0.55, -0.15, 1, {}),
                                               ("vorne", v_trombone, 0.38, 0.1, 0, {})], motif=(0,))
        sc.add(61 + 2 * k, [SIGH[sg]], "sigh", [("tune", v_strings_long, 0.48, 0.15, 0, dict(attack=0.1)),
                                                ("tune", v_strings_long, 0.28, -0.1, -1, dict(attack=0.1))])
    sc.add(66, [HEAD["Em"]], "head", [("vorne", v_trumpet, 0.4, 0.1, 0, {}), ("vorne", v_horn, 0.5, -0.15, 0, {}),
                                      ("vorne", v_trombone, 0.4, 0.0, -1, {})], motif=(0,))
    sc.add(67, [RUN], "sigh", [("tune", v_strings_long, 0.4, 0.15, 0, dict(attack=0.03)),
                               ("tune", v_strings_long, 0.28, -0.1, -1, dict(attack=0.03))])
    sc.add(68, VORNE_E, "vorne", motif=(0, 1))  # rendered by vorne() (its bend)
    sc.add(68, ["B5 " + "- " * 15, "- " * 15 + "-"], "pedal", [("tune", v_strings_long, 0.3, 0.0, 0, dict(attack=0.4))])
    sc.add(70, drop_tokens("E3"), "drop", [("tune", v_strings_long, 0.35, 0.0, 2, dict(attack=0.1, legato=0.97))],
           motif=(0, 1))  # the choir's drop by drop(full=True), the violins two octaves up
    sc.add(72, V_END, "theme V", TUTTI)
    sc.add(76, TURN34, "turn", [("vorne", v_horn, 0.48, 0.0, 0, {}), ("tune", v_strings_long, 0.3, -0.15, -1, {})])
    P = sc.phrase
    P(0, 3, "A", "Vorne's motif + the drop", "drop", None, kept=True)
    P(4, 7, "A", "orchestra (no melody)", None, None, kept=True)
    P(8, 11, "A", "Vorne's motif + the drop", "drop", None, kept=True)
    P(12, 15, "A", "orchestra (no melody)", None, None, kept=True)
    P(16, 31, "B", "the march tune", "march tune", ("HC", "F", "B"), kept=True)
    P(32, 41, "C", "the augmented motif, the drop", "drop", None, kept=True)
    P(42, 43, "C build", "timpani and snare roll (fill)", None, None, kept=True)
    P(44, 51, "D", "theme V antecedent (cellos, horn)", "theme V", ("HC", "F#dim", "B"))
    P(52, 59, "D", "theme V consequent (violins, horns)", "theme V", ("PAC", "B7", "Em"))
    P(60, 67, "E", "development: head / sigh dialogue, a run", "sigh", ("HC", "Em", "B7"))
    P(68, 71, "F", "climax: his motif bending into the drop", "drop", ("Phrygian", "F", "Em"))
    P(72, 75, "F", "theme V's cadence (tutti)", "theme V", ("PAC", "B7", "Em"))
    P(76, 79, "turnaround", "the horn's line, the build onto B", "turn", ("HC", "F", "B"))
    return sc


def vl_part(s, bar, chord, st0, ln, tex):
    """r85's 'full' orchestra (timpani, low-brass 8ths, the choir's chord, the gallop), or 'lyric' (the same pulse
    lighter: low strings for the low brass, the choir low and soft, a string pad) for steps st0..st0+ln of a bar."""
    voicing, bass = vc(chord)
    low = transpose(bass, 12)
    s.hit("perc", timpani(timp_note(bass)), bar, st0, (0.75 if st0 == 0 else 0.5) * (1 if tex == "full" else 0.75))
    for st in range(st0, st0 + ln, 2):
        if tex == "full":
            s.hit("ost", v_brass(note_freq(low), round(s.step * 1.4, 4), True), bar, st, 0.34 if st % 4 == 0 else 0.26)
        else:
            s.hit("ost", v_strings_short(note_freq(low), round(s.step * 1.4, 4)), bar, st, 0.3 if st % 4 == 0 else 0.2)
    if tex == "full":
        s.chord("choir", v_choir, voicing, bar, st0, ln, gain=0.4, spread=0.8)
    else:
        s.chord("choir", v_choir, voicing, bar, st0, ln, gain=0.24, spread=0.8, vowel="oo")
        s.chord("strings", v_strings_long, voicing, bar, st0, ln, gain=0.16, spread=0.8)
    for st in range(st0, st0 + ln):
        s.hit("gallop", v_strings_short(note_freq(transpose(low, GALLOP[st])), round(s.step * 0.75, 4)), bar, st,
              (0.36 if st % 3 == 0 else 0.22) * (1 if tex == "full" else 0.8), -0.25)


def vl_bar86(s, kicks, bar, chord, tex):
    """A bar of r85's textures (vl_bar, unchanged, for a bar on one chord) or of two chords a half bar each, and
    'lyric' under theme V. The drums keep r85's one pulse throughout."""
    halves = chord if isinstance(chord, tuple) else (chord,)
    if tex != "lyric" and len(halves) == 1:
        vl_bar(s, kicks, bar, chord, tex)
        return
    for h, ch in enumerate(halves):
        vl_part(s, bar, ch, h * 16 // len(halves), 16 // len(halves), tex)
    lyric = tex == "lyric"
    for beat in range(4):
        s.hit("drums", kick(130), bar, beat * 4, (0.7 if beat % 2 == 0 else 0.4) if lyric else
              (0.8 if beat % 2 == 0 else 0.5))
        kicks.append(s.pos(bar, beat * 4))
    for st, g in enumerate(VL_SNARE):
        s.hit("snare", snare(True), bar, st, g * (0.4 if lyric else 0.65), 0.1)


def last_stand_r86():
    sc = last_stand_score()
    s = LoopSong(150, 4, len(VL86_CH))
    kicks = []
    vl_render(s, kicks)
    for base in s.sections():
        # ---- bars 0-43: vorne-last-stand-r85-a's calls, unchanged
        for first, chords, tex, _ in VLA_PLAN[:4]:
            for k, ch in enumerate(chords):
                vl_bar(s, kicks, base + first + k, ch, tex)
        for b in (0, 16, 32):
            s.hit("perc", crash(), base + b, 0, 0.4, 0.3)
        for k in (0, 8):
            vorne(s, "vorne", v_brass, base + k, "E3", 0.7, -0.15)
            vorne(s, "vorne", v_brass, base + k, "E3", 0.35, 0.15, octave=-1)
            drop(s, base + k + 2)
        s.play("tune", v_trumpet, BR_A_TUNE[:8], base + 16, gain=0.42, position=0.15)
        s.play("tune", v_strings_long, BR_A_TUNE[:8], base + 16, gain=0.3, position=-0.15, octave=-1)
        s.play("tune", v_horn, BR_A_TUNE[8:], base + 24, gain=0.55, position=0.1, octave=-1)
        s.play("tune", v_brass, BR_A_TUNE[8:], base + 24, gain=0.25, position=-0.1, octave=-1)
        for k, ch in enumerate(BR_A_CH[8:]):
            v, _ = vc(ch)
            for st in range(0, 16, 2):
                s.hit("tune", v_strings_short(note_freq(transpose(v[(st // 2) % len(v)], 24)), round(s.step * 1.5, 4)),
                      base + 24 + k, st, 0.13, 0.3)
        vorne(s, "vorne", v_muted, base + 34, "E3", 0.75, 0.0, rhythm="aug", bend=False)
        vorne(s, "vorne", v_muted, base + 38, "E3", 0.75, 0.0, rhythm="aug", bend=True)
        drop(s, base + 40, gain=0.6)
        # ---- bars 44-79: new
        for k, ch in enumerate(VL86_CH[44:]):
            vl_bar86(s, kicks, base + 44 + k, ch, VL86_TEX[k])
        for b in (44, 52, 60, 68, 72):
            s.hit("perc", crash(), base + b, 0, 0.4 if b != 44 else 0.3, 0.3)
        sc.play(s, base)
        vorne(s, "vorne", v_brass, base + 68, "E3", 0.75, -0.15)
        vorne(s, "vorne", v_brass, base + 68, "E3", 0.4, 0.15, octave=-1)
        vorne(s, "vorne", v_trumpet, base + 68, "E4", 0.3, 0.0)
        drop(s, base + 70, full=True)
        s.hit("fx", riser(s.bar * 2), base + 66, 0, 0.2)
        for st in range(0, 16):  # the run's bar: a timpani roll into the climax
            s.hit("perc", timpani("B1"), base + 67, st, 0.1 + 0.03 * st)
    return s, vl_mix(s, kicks), [(-4, "intro (r85 a, kept): timpani, low-brass 8ths, the choir, a snare roll")] \
        + VL86_SECTIONS, sc


# ================================================================ track 27: the Ending (75 BPM), finished

# The D major statement (r85's STRAIN_D, bars 16-19) asked; its consequent answers, in the same rhythm, to D.
STRAIN_D2 = ["F#5 - - - - - E5 - D5 - - - - - A5 -", "B5 - - - - - - - A5 - - - G5 - - -",
             "G5 - - - - - F#5 - E5 - - - - - C#5 -", "D5 - - - - - - - - - - - - - - -"]
HORN_AMEN = ["B4 - - - - - - - G4 - - - A4 - - -", "F#4 - - - - - - - - - - - - - - -"]  # G - D: the horn's answer
END86_CH = (list(ENA_CH[:20]) + ["D", ("G", "D/F#"), ("Em", "A7"), "D",   # 20-23: the consequent, a full cadence
                                 "Gm", ("Eb/G", "D"), "G", "D",            # 24-27: the Choir at peace, the horn
                                 "D", "D", "D"])                           # 28-30: the open chord over a low E-flat
END86_SECTIONS = [(0, "the solo horn alone, the main motif (r85 a, kept)"),
                  (2, "the motif again, the strings in under the horn (kept)"),
                  (4, "the strings carry the motif, the piano joins (kept)"),
                  (6, "the motif in strings and horns, continuing into (kept)"),
                  (8, "the second strain (F major, the title's C-A-C-F phrase) (kept)"),
                  (12, "the climb: rising string phrases, horns, a timpani roll (kept)"),
                  (16, "the full statement in D major (kept to 1:04), answered by its consequent (new, 20-23)"),
                  (24, "the Choir motif at peace in the strings over Gm, E-flat - D; the horn's answer G - D (26)"),
                  (28, "the open ending: D major over a low E-flat, faded")]


def ending_score():
    sc = Score("ending-r86-a (Stand Down, Aegis)", END86_CH, loop=False)
    sc.sections = END86_SECTIONS
    for k in (0, 2, 4):
        sc.add(k, MAIN_D4, "motif", motif=(0, 1), kept=True)
    sc.add(6, MOTIF_ON + STRAIN_A + CLIMB_A, "strings lead", motif=(0, 1), kept=True)
    sc.add(16, STRAIN_D, "statement", motif=(0, 1), kept=True)
    sc.add(20, STRAIN_D2, "statement")
    sc.add(24, PEACE, "peace", motif=(0, 1))
    sc.add(26, HORN_AMEN, "horn amen")
    P = sc.phrase
    for k in (0, 2, 4):
        P(k, k + 1, "opening", "the main motif", "motif", None, kept=True)
    P(6, 7, "opening", "the motif, continuing", "strings lead", None, kept=True)
    P(8, 11, "strain", "second strain", "strings lead", None, kept=True)
    P(12, 15, "climb", "the climb", "strings lead", ("HC", "Bb", "A"), kept=True)
    P(16, 19, "statement", "D major statement, antecedent", "statement", ("HC", "Bm", "A"), kept=True)
    P(20, 23, "statement", "D major statement, consequent (new)", "statement", ("PAC", "A7", "D"))
    P(24, 25, "peace", "the Choir motif at peace", "peace", ("bII-I", "Eb/G", "D"))
    P(26, 27, "peace", "the horn's answer", "horn amen", ("plagal", "G", "D"))
    P(28, 30, "open end", "the open chord (no melody)", None, None)
    return sc


def stand_down_r86():
    sc = ending_score()
    s = LoopSong(75, 0, len(END86_CH))
    # ---- bars 0-19: ending-r85-a's calls, unchanged (the gains the same below bar 20)
    gains = lambda b: 0.0 if b < 2 else 0.22 if b < 4 else 0.28 if b < 8 else 0.32 if b < 16 else \
        0.45 if b < 23 else 0.38 if b < 24 else 0.3 if b < 28 else 0.26  # noqa: E731
    place_chords(s, END86_CH, v_strings_long, "strings", gains, attack=0.6)
    s.play("horn", v_horn, MAIN_D4, 0, gain=0.42, octave=-1)
    s.play("horn", v_horn, MAIN_D4, 2, gain=0.45, octave=-1)
    s.play("lead", v_strings_long, MAIN_D4, 4, gain=0.5, position=-0.1)
    s.play("horn", v_horn, MAIN_D4, 4, gain=0.25, position=0.25, octave=-1)
    s.play("lead", v_strings_long, MOTIF_ON + STRAIN_A + CLIMB_A, 6, gain=0.55, position=-0.1)
    s.play("lead", v_strings_long, MOTIF_ON, 6, gain=0.25, position=0.1, octave=1)
    s.play("horn", v_horn, MOTIF_ON + STRAIN_A, 6, gain=0.32, position=0.25, octave=-1)
    for b in range(4, 16):
        ch = END86_CH[b]
        for j, c in enumerate(ch if isinstance(ch, tuple) else (ch,)):
            v = vc(c)[0]
            ln = 16 // (2 if isinstance(ch, tuple) else 1)
            for st in range(j * ln, (j + 1) * ln, 2):
                s.hit("piano", v_epiano(note_freq(transpose(v[(st // 2) % len(v)], 12)), round(s.step * 3, 4), 0.5),
                      b, st, 0.2, -0.3 + 0.15 * ((st // 2) % 4))
    for k, nm in enumerate(("G3", "A3", "Bb3", "A3")):
        s.hit("horn", v_horn(note_freq(nm), round(s.bar * 0.95, 4)), 12 + k, 0, 0.35, 0.25)
    for st in range(0, 16):
        s.hit("perc", timpani("A1"), 15, st, 0.1 + 0.03 * st)
    s.hit("perc", crash(), 16, 0, 0.3, 0.2)
    # ---- the statement, its antecedent (16-19, kept) and its consequent (20-23, new), in the same instruments
    for toks, bar in ((STRAIN_D, 16), (STRAIN_D2, 20)):
        s.play("brass", v_brass, toks, bar, gain=0.55, position=0.15, octave=-1)
        s.play("brass", v_trumpet, toks, bar, gain=0.35, position=-0.15)
        s.play("lead", v_strings_long, toks, bar, gain=0.45, octave=1)
        s.play("lead", v_strings_long, toks, bar, gain=0.35)
    for b in range(16, 24):
        chs = END86_CH[b] if isinstance(END86_CH[b], tuple) else (END86_CH[b],)
        for j, c in enumerate(chs):
            st, ln = j * 16 // len(chs), 16 // len(chs)
            if j == 0:
                s.hit("perc", timpani(timp_note(vc(c)[1])), b, 0, 0.6 if b < 23 else 0.5)
            s.chord("choirpad", v_choir, vc(c)[0], b, st, ln, gain=0.35 if b < 23 else 0.28, spread=0.8)
    # ---- 24-27: the Choir motif at peace (strings), the horn's answer; 28-30: the open ending
    s.play("peace", v_strings_long, PEACE, 24, gain=0.55, position=0.1, legato=0.98)
    s.play("peace", v_strings_long, PEACE, 24, gain=0.35, position=-0.1, octave=-1, legato=0.98)
    s.play("horn", v_horn, HORN_AMEN, 26, gain=0.42, position=0.2, legato=0.97)
    for b in (26, 27, 28):
        v = vc(END86_CH[b])[0]
        for st in range(0, 16 if b < 28 else 8, 2):
            s.hit("piano", v_epiano(note_freq(transpose(v[(st // 2) % len(v)], 12)), round(s.step * 3, 4), 0.4),
                  b, st, 0.12 if b < 28 else 0.1, -0.3 + 0.15 * ((st // 2) % 4))
    open_ending(s, 28, 3)
    mix = mixdown(s, {
        "horn": dict(hp=60, rev=(4.0, 0.45), seed=8561, gain=0.9),
        "lead": dict(hp=70, rev=(3.5, 0.4), wid=1.3, seed=8562, gain=0.75),
        "brass": dict(hp=60, rev=(3.5, 0.4), wid=1.2, seed=8570, gain=0.8),
        "strings": dict(hp=60, rev=(4.0, 0.45), wid=1.6, seed=8563, gain=0.55),
        "bass": dict(hp=30, rev=(3.0, 0.3), seed=8564, gain=0.5),
        "piano": dict(hp=90, rev=(3.0, 0.35), wid=1.4, seed=8565, gain=0.6),
        "perc": dict(hp=30, rev=(3.0, 0.35), damp=0.8, seed=8566, gain=0.75),
        "choirpad": dict(hp=120, rev=(4.5, 0.5), wid=1.6, seed=8567, gain=0.5),
        "peace": dict(hp=80, rev=(4.5, 0.5), wid=1.5, seed=8568, gain=0.8),
        "eb": dict(hp=25, rev=(4.0, 0.4), seed=8569, gain=0.6),
    }, [], depth=0.0)
    return s, mix, len(END86_CH), END86_SECTIONS, sc


# ================================================================ render

LOOPS = {"a16": ("act7-a-theme-r86-a", far_shore_r86, far_shore_score, 140),
         "vls": ("vorne-last-stand-r86-a", last_stand_r86, last_stand_score, 150)}
ONCE = {"end": ("ending-r86-a", stand_down_r86, ending_score, 75)}
KEPT = {"vls": ("vorne-last-stand-r85-a.ogg", 4 + 44), "end": ("ending-r85-a.ogg", 20)}


def _sections(name, secs, t0, bar_s, end_s):
    print(f"{name} sections:")
    for k, (bar, label) in enumerate(secs):
        t = t0 + bar * bar_s
        nxt = t0 + secs[k + 1][0] * bar_s if k + 1 < len(secs) else end_s
        print(f"  {int(t // 60)}:{t % 60:05.2f}-{int(nxt // 60)}:{nxt % 60:05.2f}  {label}")


def _kept_compare(key, path, bar_s):
    """How the kept opening compares with the earlier take's file: level and residual over its span."""
    if key not in KEPT:
        return
    old_name, bars = KEPT[key]
    old = OUT / old_name
    if not old.exists():
        old = OUT / "rejected" / old_name
    a, b = decode(path), decode(old)
    n = int((bars * bar_s - 1.0) * SR)
    a, b = a[:, :n], b[:, :n]
    g = np.sum(a * b) / np.sum(b * b)
    res = a - g * b
    print(f"  kept opening vs {old_name} (0-{n / SR:.1f} s): gain {db(g):+.2f} dB, residual after gain "
          f"{db(np.sqrt(np.mean(res ** 2))) - db(np.sqrt(np.mean(a ** 2))):.1f} dB under the signal")


def render_loop(key, out, show):
    name, fn, _, _ = LOOPS[key]
    s, mix, secs, sc = fn()
    sc.check(s.intro, s.bar)
    mix = mix - mix.mean(axis=1, keepdims=True)
    ext = (s.intro + s.loop) * 16 * s.step_n + s.xf_steps * s.step_n
    mix = master(np.concatenate([mix, mix[:, ext:ext + SR]], axis=1), -14.0, -2.0)[:, :mix.shape[1]]
    audio, loop_start, loop_len = s.assemble(mix)
    audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
    audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{name}.ogg", audio, tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
    dec = decode(path)
    end = loop_start + loop_len
    rms = lambda x: db(np.sqrt(np.mean(x ** 2)))  # noqa: E731
    _report(path, f"  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s  (samples {loop_start}+{loop_len})"
                  f"  seam {seam_ratio(dec, loop_start, loop_len):.2f}  RMS {rms(dec[:, end - ns(0.25):end]):.1f} dB"
                  f" before the loop end, {rms(dec[:, loop_start:loop_start + ns(0.25)]):.1f} dB after the start")
    _kept_compare(key, path, s.bar)
    if show:
        _sections(name, secs, s.intro * s.bar, s.bar, (loop_start + loop_len) / SR)


def render_once(key, out, show):
    name, fn, _, _ = ONCE[key]
    s, mix, bars, secs, sc = fn()
    sc.check(0, s.bar)
    end = int(bars * s.bar * SR) + ns(3.0)
    mix = mix[:, :end]
    mix = mix - mix.mean(axis=1, keepdims=True)
    fo = int((s.bar * 2.0 + 3.0) * SR)
    mix[:, -fo:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fo))) ** 1.5
    mix = master(mix, -14.0, -2.0)
    mix[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))
    path = write_ogg(out / f"{name}.ogg", mix)
    _report(path, "  (one-shot, no loop)")
    _kept_compare(key, path, s.bar)
    if show:
        _sections(name, secs, 0.0, s.bar, end / SR)


def main(argv):
    out = OUT
    check_only = "--check" in argv
    argv = [a for a in argv if a != "--check"]
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    for key in argv or [*LOOPS, *ONCE]:
        table = LOOPS if key in LOOPS else ONCE
        name, _, score_fn, bpm = table[key]
        if check_only:
            bar_s = 240.0 / bpm
            score_fn().check(4 if key in LOOPS else 0, bar_s)
        elif key in LOOPS:
            render_loop(key, out, True)
        else:
            render_once(key, out, True)


if __name__ == "__main__":
    main(sys.argv[1:])
