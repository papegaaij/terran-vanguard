#!/usr/bin/env python3
"""Concept round 40 (step MUS-D): two proposals for the new track 29 "Debrief", a quiet music loop under
the debrief after the mission complete jingle (track 23). Same loop format as the round-02/03/08/36
themes: intro + loop + 2-bar fade tail, LOOPSTART / LOOPLENGTH Vorbis comments (samples), sample-exact
loop; mastered to -19 LUFS (the brief: -18...-20, under the debrief's sounds and voice) with a -3 dBFS
sample-peak ceiling. Both are in D major, the key of the jingle's last chord (D major, humanity's motif
1-5-8-9-10 in its major form), so they follow it; neither reuses the hangar theme "Dry Dock" (swung
90 BPM D dorian, FM electric piano, flute and vibes).

Outputs (design/audio/music/concept/ unless --out names another directory):
  debrief-theme-r40-a.ogg  "Homeward"     63 BPM (half the jingle's 126), D major, 2-bar intro + 16-bar loop
      Warm synth-orchestral: slow string and choir pads, a soft harp in eighths, low strings on the
      roots; humanity's motif slow on a solo horn (D4 A4 D5 E5 F#5) over D - Bm, an answer, the motif
      climbing on to A5; in the B half a solo string counter-melody and the horn's 8-9-10 fragment;
      one soft timpani note and a cymbal swell into the B half. No beat.
  debrief-theme-r40-b.ogg  "Stand Down"   84 BPM, D major, 2-bar intro + 24-bar loop
      Laid-back electronic: maj9 / m9 pads pumping gently on a soft kick, eighth-note pulses on the chord
      tones through a dotted-eighth ping-pong delay (soft attack, a low-pass with no resonance), a round
      sub bass, a brushed snare, closed hats and a shaker; the motif only hinted (5-8-9-10, A4 D5 E5 F#5)
      on a soft glass lead in B, stated once in full in the drumless C section.
  debrief-join-r40-{a,b}.ogg   listening aids, 15 s: the mission complete jingle
      (mission-complete-final-r26-a.ogg, = assets/music/mission-complete.ogg) at its own level, then the
      loop file from JOIN_AT = 5.714 s (three jingle bars at 126 BPM: the downbeat after the jingle's final
      chord, while it rings out), both at their mastered levels as the game would mix them; 1 s fade out.

Usage: python3 tools/concept/audio/music_r40.py [a|b ...] [--out DIR]
Deterministic (fixed seeds). Not listened to by Claude: checked by loudness, peaks, the loop seam, the
loudness per section, a band balance against the chosen hangar and briefing themes, and the join's levels.
"""
import sys
from functools import lru_cache
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from music import crash, hat, kick, ns, riser, timpani, transpose, v_pad, v_strings_long  # noqa: E402
from music_r02 import LoopSong, add_tail, bass_line, seam_ratio, shaker, v_choir, v_sub  # noqa: E402
from music_r03 import human_motif, mixdown  # noqa: E402
from synth import (SR, adsr, db, decode, exp_decay, lufs, master, noise, note_freq, pulse, saturate,  # noqa: E402
                   saw, sine, supersaw, svf, triangle, write_ogg)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "music" / "concept"
JINGLE = OUT / "mission-complete-final-r26-a.ogg"
JOIN_AT = 3 * 16 * 15 / 126          # 5.714 s: three bars of the jingle at 126 BPM
TARGET = -19.0


# ================================================================ voices

@lru_cache(None)
def v_harp(freq, dur):
    """A soft harp: five decaying partials, a 6 ms attack (no pluck transient), low-passed; rings on."""
    n = ns(1.8)
    t = np.arange(n) / SR
    x = sum((1 / k ** 1.6) * sine(freq * k * (1 + 0.0007 * k), n) * np.exp(-t * k / 1.3)
            for k in range(1, 6))
    x = svf(x, 4200, q=0.6)
    return x * np.clip(t / 0.006, 0, 1) * np.clip((1.8 - t) / 0.3, 0, 1)


@lru_cache(None)
def v_horn(freq, dur):
    """A warm solo French horn: narrow supersaw and pulse, a slow-opening dark filter, late vibrato."""
    n = ns(dur + 0.45)
    t = np.arange(n) / SR
    vib = 1 + 0.004 * np.sin(2 * np.pi * 4.8 * t) * np.clip((t - 0.45) / 0.5, 0, 1)
    f = freq * vib
    x = supersaw(f, n, voices=3, detune=0.003, rng=np.random.default_rng(4001)) + pulse(f, n, 0.45) * 0.35
    x = svf(x, 380 + 1150 * (1 - exp_decay(n, 0.12)), q=0.8)
    return saturate(x * adsr(n, 0.11, 0.3, 0.85, 0.4, gate=dur) * 1.2, 1.2)


@lru_cache(None)
def v_solo_str(freq, dur):
    """A solo string (violin-ish): two close saws, delayed vibrato, a gentle bow attack."""
    n = ns(dur + 0.5)
    t = np.arange(n) / SR
    vib = 1 + 0.005 * np.sin(2 * np.pi * 5.4 * t) * np.clip((t - 0.25) / 0.4, 0, 1)
    x = saw(freq * vib, n) + saw(freq * vib * 1.0025, n, start=0.4)
    x = svf(svf(x, 2600, q=0.7), 300, mode="hp")
    return x * adsr(n, 0.18, 0.3, 0.85, 0.45, gate=dur) * 0.5


@lru_cache(None)
def v_glass(freq, dur):
    """A soft glass lead: a sine with a gently fading octave FM sheen, slow attack, long release."""
    n = ns(dur + 0.8)
    t = np.arange(n) / SR
    vib = 1 + 0.004 * np.sin(2 * np.pi * 5.0 * t) * np.clip((t - 0.35) / 0.4, 0, 1)
    ph = np.cumsum(freq * vib) / SR
    index = 0.3 + 0.6 * np.exp(-t / 0.9)
    x = np.sin(2 * np.pi * ph + index * np.sin(2 * np.pi * 2 * ph)) + 0.12 * np.sin(4 * np.pi * ph)
    return x * adsr(n, 0.05, 0.5, 0.7, 0.7, gate=dur)


@lru_cache(None)
def v_softpulse(freq, dur, cutoff=1200.0):
    """A soft synth pulse for the eighth-note pattern: 8 ms attack, an unresonant low-pass."""
    n = ns(dur + 0.3)
    x = pulse(freq, n, 0.4) * 0.6 + triangle(freq * 1.003, n) * 0.5
    x = svf(x, cutoff * (0.55 + 0.45 * exp_decay(n, 0.15)), q=0.7)
    return x * adsr(n, 0.008, 0.28, 0.35, 0.22, gate=dur)


@lru_cache(None)
def brush():
    """A brushed snare: band noise with a slow swish attack, no tonal body."""
    n = ns(0.35)
    t = np.arange(n) / SR
    x = svf(noise(n, np.random.default_rng(4002)), 3200, q=0.6, mode="bp")
    return x * np.clip(t / 0.012, 0, 1) * np.exp(-t / 0.09) * np.clip((0.35 - t) / 0.05, 0, 1)


# ================================================================ a: Homeward (63 BPM, D major)

A_CH = {  # pad voicing (strings), low root
    "D": (["D3", "A3", "D4", "F#4"], "D2"),
    "Bm": (["D3", "F#3", "B3", "D4"], "B1"),
    "G": (["D3", "G3", "B3", "D4"], "G1"),
    "A": (["C#3", "E3", "A3", "C#4"], "A1"),
    "Asus": (["D3", "E3", "A3", "D4"], "A1"),
    "Em7": (["D3", "G3", "B3", "E4"], "E2"),
    "F#m": (["C#3", "F#3", "A3", "C#4"], "F#1"),
    "D/F#": (["D3", "A3", "D4", "F#4"], "F#1"),
}
A_LOOP = (["D", "Bm", "G", "A", "D", "Bm", "Em7", ("Asus", "A")]       # A: the horn's motif and answer
          + ["G", "A", "F#m", "Bm", "G", "D/F#", "Em7", ("Asus", "A")])  # B: solo string, the horn's fragment
A_HORN = (human_motif("D4", major=True)[:1] + ["F#5 - - - - - - - - - - - . . . ."]
          + ["E5 - - - - - D5 - B4 - - - - - - -", "C#5 - - - - - - - - - - - . . . .",
             "D4 - - - - - A4 - D5 - - - - - E5 -", "F#5 - - - - - G5 - A5 - - - - - - -",
             "G5 - - - F#5 - - - E5 - - - D5 - - -", "E5 - - - - - - - - - - - . . . ."])
A_STRING = ["B4 - - - - - - - D5 - - - G5 - - -", "F#5 - - - - - - - E5 - - - - - - -",
            "C#5 - - - - - - - E5 - - - A5 - - -", "F#5 - - - - - - - - - - - . . . ."]
A_HORN_B = ["D5 - - - - - - - E5 - - - - - - -", "F#5 - - - - - - - - - - - . . . .",
            "G5 - - - F#5 - - - E5 - - - - - - -", "E5 - - - - - - - C#5 - - - . . . ."]
A_HARP = [0, 1, 2, 3, 2, 1, 3, 2]        # eighth-note pattern: indices into the voicing an octave up


def a_bar(s, bar, name, sec):
    pad, root = A_CH[name]
    half = 16
    s.chord("pad", v_strings_long, pad, bar, dur_steps=half, gain=0.5, spread=0.7, attack=0.6)
    s.chord("pad", v_pad, [transpose(n, -12) for n in pad[1:]], bar, dur_steps=half, gain=0.28, bright=700.0)
    if sec == "B":
        s.chord("choir", v_choir, [transpose(n, 12) for n in pad[1:]], bar, dur_steps=half, gain=0.3, vowel="oo")
    s.hit("low", v_strings_long(note_freq(transpose(root, 12)), round(16 * s.step, 4), attack=0.4), bar, 0, 0.32)
    s.hit("low", v_sub(note_freq(root), round(15 * s.step, 4)), bar, 0, 0.3)


def a_harp(s, bar, name, gain):
    pad, _ = A_CH[name]
    tones = [transpose(n, 12) for n in pad]
    for k, idx in enumerate(A_HARP):
        s.hit("harp", v_harp(note_freq(tones[idx]), 0.4), bar, 2 * k, gain * (1.0 if k % 2 == 0 else 0.75),
              0.35 if k % 2 else -0.35)


def homeward():
    s = LoopSong(63, 2, len(A_LOOP))
    # intro: the jingle's D major chord held on in strings and choir, the harp coming in on bar 1
    for bar in range(2):
        s.chord("pad", v_strings_long, A_CH["D"][0], bar, dur_steps=16, gain=0.45, spread=0.7, attack=0.8)
        s.chord("choir", v_choir, ["D4", "F#4", "A4"], bar, dur_steps=16, gain=0.22, vowel="oo")
        s.hit("low", v_strings_long(note_freq("D3"), round(16 * s.step, 4), attack=0.6), bar, 0, 0.3)
    a_harp(s, 1, "D", 0.22)

    for base in s.sections():
        for i, entry in enumerate(A_LOOP):
            bar = base + i
            sec = "A" if i < 8 else "B"
            if isinstance(entry, tuple):  # half a bar each
                for k, name in enumerate(entry):
                    pad, root = A_CH[name]
                    s.chord("pad", v_strings_long, pad, bar, 8 * k, dur_steps=8, gain=0.5, spread=0.7, attack=0.4)
                    s.chord("pad", v_pad, [transpose(n, -12) for n in pad[1:]], bar, 8 * k, dur_steps=8,
                            gain=0.28, bright=700.0)
                    if sec == "B":
                        s.chord("choir", v_choir, [transpose(n, 12) for n in pad[1:]], bar, 8 * k, dur_steps=8,
                                gain=0.3, vowel="oo")
                s.hit("low", v_strings_long(note_freq("A2"), round(16 * s.step, 4), attack=0.4), bar, 0, 0.32)
                s.hit("low", v_sub(note_freq("A1"), round(15 * s.step, 4)), bar, 0, 0.3)
                a_harp(s, bar, entry[0], 0.24 if sec == "A" else 0.28)
            else:
                a_bar(s, bar, entry, sec)
                a_harp(s, bar, entry, 0.24 if sec == "A" else 0.28)
        s.play("horn", v_horn, A_HORN, base, gain=0.5, position=-0.1)
        s.play("solo", v_solo_str, A_STRING, base + 8, gain=0.42, position=0.2)
        s.play("horn", v_horn, A_HORN_B, base + 12, gain=0.46, position=-0.1)
        s.play("solo", v_solo_str, A_HORN_B[2:], base + 14, gain=0.28, position=0.2, octave=-1)
        s.hit("perc", timpani("G1", 2.5), base + 8, 0, 0.28)
        s.hit("perc", riser(s.bar * 0.75), base + 7, 4, 0.06)
        s.hit("perc", crash(), base + 8, 0, 0.12, 0.2)

    return s, mixdown(s, {
        "pad": dict(hp=70, rev=(3.5, 0.4), wid=1.4, seed=4011, gain=0.6),
        "choir": dict(hp=150, rev=(4.0, 0.5), wid=1.5, seed=4012, gain=0.45),
        "low": dict(hp=32, rev=(2.5, 0.25), seed=4013, gain=0.85),
        "harp": dict(hp=150, rev=(3.0, 0.35), wid=1.3, seed=4014, gain=0.55),
        "horn": dict(hp=90, rev=(3.2, 0.38), seed=4015, gain=0.75),
        "solo": dict(hp=200, rev=(3.0, 0.38), seed=4016, gain=0.6),
        "perc": dict(hp=35, rev=(3.0, 0.35), damp=0.8, seed=4017, gain=0.7),
    }, [], depth=0.0)


# ================================================================ b: Stand Down (84 BPM, D major)

B_CH = {  # pad voicing, sub root, pulse tones
    "Dmaj9": (["D3", "F#3", "A3", "C#4", "E4"], "D2", ["D4", "F#4", "A4", "C#5"]),
    "Bm9": (["D3", "F#3", "A3", "C#4"], "B1", ["B3", "D4", "F#4", "C#5"]),
    "Gmaj9": (["B2", "D3", "F#3", "A3"], "G1", ["G3", "B3", "D4", "F#4"]),
    "Em9": (["D3", "F#3", "G3", "B3"], "E2", ["E4", "G4", "B4", "F#4"]),
    "A11": (["G3", "B3", "D4", "E4"], "A1", ["A3", "D4", "E4", "G4"]),
    "A6": (["C#3", "E3", "F#3", "A3"], "A1", ["A3", "C#4", "E4", "F#4"]),
    "F#m7": (["C#3", "E3", "F#3", "A3"], "F#1", ["F#3", "A3", "C#4", "E4"]),
    "A": (["C#3", "E3", "A3", "C#4"], "A1", ["A3", "C#4", "E4", "A4"]),
}
B_LOOP = (["Dmaj9", "Dmaj9", "Bm9", "Bm9", "Gmaj9", "Gmaj9", "Em9", "A11"]   # A: pulses, pads, sub, kit
          + ["Dmaj9", "Dmaj9", "Bm9", "Bm9", "Gmaj9", "Gmaj9", "Em9", "A11"]  # B: the motif hinted
          + ["Gmaj9", "A6", "F#m7", "Bm9", "Gmaj9", "A6", "Em9", "A"])        # C: no kit, the motif in full
B_PULSE = [0, 2, 1, 3, 2, 1, 3, 2]     # eighths: indices into the pulse tones
B_SUB = [(0, 0, 6), (6, 0, 2), (10, 7, 4)]
B_HINT = ["A4 - - - D5 - - - - - - - E5 - - -", "F#5 - - - - - - - - - - - . . . ."]
B_HINT2 = ["F#5 - - - E5 - - - D5 - - - - - - -", "C#5 - - - - - - - A4 - - - . . . ."]
B_FULL = (human_motif("D4", major=True)[:1] + ["F#5 - - - - - - - - - - - . . . ."]
          + ["E5 - - - - - D5 - C#5 - - - A4 - - -", "D5 - - - - - - - - - - - . . . ."])


def stand_down():
    s = LoopSong(84, 2, len(B_LOOP))
    kicks = []
    for bar in range(2):  # intro: the Dmaj9 pad swelling, the pulses' filter opening
        pad, root, tones = B_CH["Dmaj9"]
        s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4, bright=900.0)
        for k, idx in enumerate(B_PULSE):
            s.hit("pulse", v_softpulse(note_freq(tones[idx]), round(s.step * 1.6, 4), 500.0 + 300 * bar),
                  bar, 2 * k, 0.22 * (0.4 + 0.3 * bar + 0.04 * k), 0.3 if k % 2 else -0.3)
        if bar == 1:
            s.hit("bass", v_sub(note_freq(root), round(s.step * 14, 4)), bar, 0, 0.35)

    for base in s.sections():
        for i, name in enumerate(B_LOOP):
            bar = base + i
            pad, root, tones = B_CH[name]
            sec = "A" if i < 8 else "B" if i < 16 else "C"
            kit = sec != "C" or i == 23
            if kit and i != 23:
                for st in (0, 10):
                    s.hit("drums", kick(70), bar, st, 0.55 if st == 0 else 0.4)
                    kicks.append(s.pos(bar, st))
                for st in (4, 12):
                    s.hit("drums", brush(), bar, st, 0.32, 0.1)
                for st in range(0, 16, 2):
                    s.hit("hats", hat(), bar, st, 0.07 if st % 4 else 0.1, 0.3)
            elif i == 23:  # the kick back in under the last bar of C
                for st in (8, 12):
                    s.hit("drums", kick(70), bar, st, 0.35 + 0.05 * (st - 8) / 4)
                    kicks.append(s.pos(bar, st))
            for st in range(16):
                s.hit("perc", shaker(), bar, st, (0.04 if st % 2 else 0.06) * (0.6 if sec == "C" else 1.0), -0.35)
            if i in (0, 16):
                s.hit("perc", crash(), bar, 0, 0.1, 0.3)
            cut = {"A": 1400.0 + 100 * i, "B": 2400.0, "C": 1600.0}[sec]
            for k, idx in enumerate(B_PULSE):
                s.hit("pulse", v_softpulse(note_freq(tones[idx]), round(s.step * 1.6, 4), round(cut, -1)),
                      bar, 2 * k, 0.22 * (1.0 if k % 2 == 0 else 0.8), 0.3 if k % 2 else -0.3)
            s.chord("pad", v_pad, pad, bar, dur_steps=16, gain=0.4 if sec != "C" else 0.48, bright=1500.0)
            if sec != "C":
                bass_line(s, "bass", v_sub, root, bar, B_SUB, 0.42)
            else:
                s.hit("bass", v_sub(note_freq(root), round(s.step * 15, 4)), bar, 0, 0.36)
        s.play("lead", v_glass, B_HINT, base + 8, gain=0.38, position=0.15)
        s.play("lead", v_glass, B_HINT2, base + 12, gain=0.34, position=0.15)
        s.play("lead", v_glass, B_FULL, base + 16, gain=0.4, position=0.1)
        s.play("lead", v_glass, B_HINT, base + 20, gain=0.3, position=0.1, octave=-1)

    return s, mixdown(s, {
        "drums": dict(hp=40, rev=(1.2, 0.12), seed=4021, gain=0.6),
        "hats": dict(hp=500, gain=0.75),
        "perc": dict(hp=200, rev=(1.8, 0.2), seed=4022, gain=0.5),
        "bass": dict(hp=30, duck=True, gain=0.45),
        "pulse": dict(hp=150, dly=(3, 0.38, 0.28, 2200), rev=(2.2, 0.25), seed=4023, gain=0.6),
        "pad": dict(hp=90, duck=True, rev=(3.5, 0.4), wid=1.5, seed=4024, gain=0.45),
        "lead": dict(hp=200, dly=(6, 0.3, 0.2, 3000), rev=(3.0, 0.35), seed=4025, gain=0.55),
    }, kicks, depth=0.3, release=0.18)


# ================================================================ checks

BANDS = [(20, 60), (60, 250), (250, 2000), (2000, 6000), (6000, 16000)]


def bands(x):
    """Share of the spectral energy per band, in dB relative to the total (mono, Hann-windowed frames)."""
    mono = x.mean(axis=0)
    nfft = 8192
    win = np.hanning(nfft)
    frames = [mono[i:i + nfft] * win for i in range(0, len(mono) - nfft, nfft // 2)]
    power = (np.abs(np.fft.rfft(np.array(frames), axis=1)) ** 2).mean(axis=0)
    freqs = np.fft.rfftfreq(nfft, 1 / SR)
    total = power[(freqs >= 20) & (freqs < 16000)].sum()
    return [10 * np.log10(power[(freqs >= lo) & (freqs < hi)].sum() / total + 1e-12) for lo, hi in BANDS]


def section_lufs(x, s, edges):
    """Loudness of the loop sections (bar edges relative to the loop start) in the decoded file."""
    out = []
    start = s.intro * 16 * s.step_n + s.xf_steps * s.step_n
    bar_n = 16 * s.step_n
    for lo, hi in zip(edges, edges[1:]):
        out.append(lufs(x[:, start + lo * bar_n - (s.xf_steps * s.step_n if lo == 0 else 0):start + hi * bar_n])[0])
    return out


def join(path, out):
    """The jingle, then the loop from JOIN_AT, 15 s, both at their mastered levels; a 1 s fade out."""
    jingle = decode(JINGLE)
    loop = decode(path)
    n = ns(15.0)
    at = int(round(JOIN_AT * SR))
    mix = np.zeros((2, n))
    mix[:, :min(n, jingle.shape[1])] += jingle[:, :n]
    mix[:, at:] += loop[:, :n - at]
    mix[:, -ns(1.0):] *= np.linspace(1, 0, ns(1.0))
    name = path.name.replace("-theme-", "-join-")
    jp = write_ogg(out / name, mix, max_peak_db=-1.0)
    dec = decode(jp)
    win = ns(0.4)
    jingle_end = lufs(dec[:, at - ns(2.0):at])[0]
    loop_head = lufs(dec[:, at + ns(1.0):at + ns(5.0)])[0]
    print(f"  {jp.name}: peak {db(np.abs(dec).max()):.1f} dBFS; jingle's last 2 s {jingle_end:.1f} LUFS, "
          f"the loop's 1-5 s {loop_head:.1f} LUFS; quietest 0.4 s window around the handover "
          f"{min(db(np.sqrt(np.mean(dec[:, i:i + win] ** 2)) + 1e-9) for i in range(at - ns(1.0), at + ns(1.5), win // 2)):.1f} dBFS RMS",
          flush=True)


SONGS = {"a": ("debrief-theme-r40-a", homeward, [0, 8, 16]),
         "b": ("debrief-theme-r40-b", stand_down, [0, 8, 16, 24])}
REFERENCES = ["hangar-theme-full-r08-a.ogg", "briefing-theme-r08-a.ogg"]


def main(argv):
    out = OUT
    if "--out" in argv:
        i = argv.index("--out")
        out = Path(argv[i + 1])
        argv = argv[:i] + argv[i + 2:]
    out.mkdir(parents=True, exist_ok=True)
    print("band balance (dB of total): " + "  ".join(f"{lo}-{hi}" for lo, hi in BANDS))
    for ref in REFERENCES:
        print(f"  {ref:32}" + "".join(f"{v:9.1f}" for v in bands(decode(OUT / ref))))
    for key in argv or SONGS:
        name, fn, edges = SONGS[key]
        s, mix = fn()
        mix = mix - mix.mean(axis=1, keepdims=True)
        audio, loop_start, loop_len = s.assemble(mix)
        audio = add_tail(audio, loop_start, 2, 16 * s.step_n)
        audio = master(audio, TARGET, -3.0)
        audio[:, :ns(0.005)] *= np.linspace(0, 1, ns(0.005))  # fade-in only: the end loops
        path = write_ogg(out / f"{name}.ogg", audio, max_peak_db=-3.0,
                         tags={"LOOPSTART": loop_start, "LOOPLENGTH": loop_len})
        dec = decode(path)
        loud, tp = lufs(dec)
        print(f"{path.name}: {audio.shape[1] / SR:.1f}s  loop {loop_start / SR:.3f}s + {loop_len / SR:.3f}s"
              f"  (samples {loop_start}+{loop_len})  seam {seam_ratio(dec, loop_start, loop_len):.2f}"
              f"  peak {db(np.abs(dec).max()):.1f} dBFS  {loud:.1f} LUFS  true peak {tp:.1f} dBTP", flush=True)
        print(f"  intro {lufs(dec[:, :loop_start])[0]:.1f} LUFS; sections "
              + ", ".join(f"{v:.1f}" for v in section_lufs(dec, s, edges)) + " LUFS")
        print(f"  bands{'':27}" + "".join(f"{v:9.1f}" for v in bands(dec[:, :loop_start + loop_len])))
        join(path, out)


if __name__ == "__main__":
    main(sys.argv[1:])
