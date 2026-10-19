#!/usr/bin/env python3
"""Concept round 37 (M7 part A "Act 3 systems and arsenal", step AUD-A): a/b proposals for the Act 3
equipment's sounds (design/audio/sfx/README.md, *Act 3 sounds*, *Player equipment*, and the drones' row
under *Enemies*): the Ion Beam's start and stop around its chosen loop (round 36, S.4 = a), the beam's
contact sizzle (a loop, plain and on rock), the Deflector Pod's tick and charge return, the EMP Burst's
ring and a machine's power-down, the Orbital Lance's rumble under its column, the Shield Overcharge's
activation and end, the cooldown-ready chime, the Evasive thrusters' dash and the light drones' break
and rebuild. Recorded layers (Freesound originals, CC0, in CREDITS.md) and synthesized layers mixed;
no bubbly, popping or squelching layers (user rule): no short pitch-swept sine blips, no low clicks,
no wet sounds.

Outputs (design/audio/sfx/concept/, the unchosen variants in concept/rejected/ since the round closed
(CHOSEN, the user's picks of 2026-10-13); OGG Vorbis q6, 44.1 kHz stereo; each one-shot starts at its event):

  ion-beam-start-r37-a.ogg   a "charge snap": the round-04 start's source ("Machine Charge.wav",
                             Glitchedtones, CC0) cut to its rise and played 1.8x fast, an ignition hiss
                             (2-7 kHz noise, no low end) and the loop's ion body rising 55 -> 110 Hz into
                             the loop's pitch; 0.4 s, played WITH the loop's first pass (the beam is at
                             full length in 0.05 s, so the start is a layer over the loop, not a lead-in)
  ion-beam-start-r37-b.ogg   b "ion strike", synthesized: a bright arc crack (fused 3-8 kHz grains),
                             the ion body falling 220 -> 110 Hz onto the loop's pitch and a hiss swell;
                             0.45 s, with the loop as a
  ion-beam-stop-r37-a.ogg    a "power down, short": the round-04 stop's source ("Power Down", noirenex,
                             CC0) cut to its first fall (0.5 s) over the ion body falling 110 -> 45 Hz;
                             0.55 s, at release (the loop fades in 0.08 s under it)
  ion-beam-stop-r37-b.ogg    b "ion collapse", synthesized: the ion body sliding 110 -> 35 Hz as it
                             dies and its hiss falling 4 -> 1 kHz; 0.6 s
  beam-impact-r37-a.ogg      a "arc scorch", synthesized: a dense dry sizzle (2-7 kHz noise with a fast
                             random flutter, circular) over a low crackle body (300-1200 Hz); a seamless
                             1.50 s LOOP, a layer under the beam loop while the beam touches a target
  beam-impact-r37-b.ogg      b "cutting torch": a plasma cutter's gas roar ("Plasma cutter gas.wav",
                             jokallset, CC0, 800 Hz-7 kHz) with a synthesized fine sizzle; a seamless
                             1.50 s LOOP
  beam-impact-rock-r37-a.ogg a on rock: a with a grit crackle (fused 1.5-5 kHz grains, ~150 a second)
                             and a low stone roar; a seamless 1.50 s LOOP (walls, rock islands)
  beam-impact-rock-r37-b.ogg b on rock: b with the same grit crackle and stone roar; 1.50 s LOOP
  deflector-tick-r37-a.ogg   a "the shield hit, up": the shield hit's source ("ELECTRIC_ZAP_001.wav",
                             JoelAudio, CC0) played 1.6x fast and cut short, a glassy tick on top; 0.13 s
  deflector-tick-r37-b.ogg   b "dry deflect", synthesized: a high noise snap and three inharmonic metal
                             partials (1.9 / 3.3 / 5.2 kHz) ringing 35 ms; 0.1 s
  deflector-return-r37-a.ogg a "soft rise", synthesized: two soft detuned voices easing up a fifth
                             (520 -> 780 Hz, smoothstep, no fast sweep) with an airy shimmer; 0.45 s
  deflector-return-r37-b.ogg b "charge settle", synthesized: a held E5-B5 fifth fading in over a rising
                             air whoosh (800 -> 3000 Hz); no glide; 0.45 s
  emp-ring-r37-a.ogg         a "the tesla, layered": the tesla shot's source ("Electric zap.wav",
                             michael_grinnell, CC0) four times, slower and later each time (1.0 / 0.8 /
                             0.65 / 0.5x at 0 / 0.08 / 0.2 / 0.35 s) spreading in the stereo field, an
                             electric thump, a crackle tail thinning out and a fading 100 Hz buzz; 1.4 s
  emp-ring-r37-b.ogg         b "arc ring", synthesized: a discharge crack, a mains-like buzz (60 -> 45 Hz
                             harmonics, 400 Hz-3 kHz) dying over 0.9 s, crackling arcs running out to
                             both sides and a thump; 1.4 s
  emp-powerdown-r37-a.ogg    a "spin-down whine", synthesized: a relay clunk (300-1500 Hz, no sub) and a
                             whine falling 1400 -> 180 Hz as it fades; 0.9 s
  emp-powerdown-r37-b.ogg    b "buzz dies", synthesized: a 100 Hz mains buzz flickering (a smooth 9 Hz
                             wobble, deepening) and sagging to 70 Hz under a faint falling whine; 0.9 s
  lance-rumble-r37-a.ogg     a "orbital roar", synthesized: a deep roar (circular noise peaking at 60 Hz)
                             over two low harmonic voices on 41 Hz beating once a loop, harmonics up to
                             400 Hz so small speakers carry it; a seamless 3.00 s LOOP under the column's
                             loop c
  lance-rumble-r37-b.ogg     b "the ground answers": the root burst's earthquake ("Cracking Earthquake",
                             uagadugu, CC0, 1.2 kHz low-pass) looped over a synthesized sub roar; a
                             seamless 3.00 s LOOP
  overcharge-whoosh-r37-a.ogg a "the restore, charged": the shield-restore chime's source ("Power Up
                             Charge", qubodup, CC0) a little slower, a rising whoosh (400 -> 4000 Hz) and a
                             white shimmer bloom at the top; 1.0 s
  overcharge-whoosh-r37-b.ogg b "double shell", synthesized: a soft supersaw pad rising an octave
                             (220 -> 440 Hz) as its filter opens, the same whoosh and a brighter bloom; 1.0 s
  overcharge-end-r37-a.ogg   a "shimmer fades", synthesized: high partials (2-7 kHz) fluttering and
                             gliding down to 0.7x, ringing out; 0.9 s
  overcharge-end-r37-b.ogg   b "the charge drains": the shield-restore source played backwards (a falling
                             charge), band-passed 200 Hz-4 kHz and faded out over its length, under a falling shimmer; 0.9 s
  cooldown-ready-r37-a.ogg   a "two soft bells", synthesized: A5 then E6 70 ms apart, each a sine with a
                             quiet 2.76x partial (a small bell), decay 0.18 s; 0.5 s
  cooldown-ready-r37-b.ogg   b "glass fifth", synthesized: one soft C6-G6 fifth (8 ms attack) with a
                             high shimmer partial, decay 0.25 s; 0.5 s
  dash-r37-a.ogg             a "thruster puff", synthesized: a jet noise burst whose band sweeps 600 ->
                             2500 -> 900 Hz in 0.3 s over a short low roar; 0.35 s
  dash-r37-b.ogg             b "rocket kick": a rocket's thrust ("RocketThrustMaxx.wav", Maxx222, CC0)
                             and an afterburner's scream ("Afterburner (Close, Screamer, Quick Onset)",
                             JSK_Productions, CC0, 300 Hz high-pass) cut to a 0.35 s burst
  drone-break-r37-a.ogg      a "ping and blast": the metal hit's source ("HeavyBulletPing.mp3",
                             wilhellboy, CC0) and the `tiny` blast's source ("Small Explosion", Cyberios,
                             CC0) with a spark spray, as the brief says; 0.6 s
  drone-break-r37-b.ogg      b "crunch": struck plating ("Impact on metal", JoMungus, CC0, its first
                             0.4 s), the `tiny` b blast ("Small explosion", dinodilopho, CC0) and sparks;
                             0.6 s
  drone-rebuild-r37-a.ogg    a "materialise", synthesized: a soft filtered saw pad easing up a fourth
                             (330 -> 440 Hz) as its filter opens 600 -> 3000 Hz; 0.6 s
  drone-rebuild-r37-b.ogg    b "three bells", synthesized: E5, A5, E6 sine bells 60 ms apart over a
                             faint sparkle fading in; 0.6 s

Levels (sfx_r24.py's `level`: the loudest 100 ms of the 200 Hz-5 kHz band, limited at a ceiling; set
against assets/sfx/, measured the same way): the beam start -24 dB and stop -25 dB around the loop's
-29 (round 04's start -28.4, stop -22.7); the deflector tick -24 dB (the shield hit -22.2; it may come
several times a second), the return -27 dB; the EMP ring -18 dB at the Smart Bomb's -1.5 dBFS ceiling (the Smart Bomb -14.4 dB, -13.6 LUFS; a reaches -18.6 dB / -14.1 LUFS, b -18.0 dB / -19.2 LUFS, its buzz less dense), a machine's
power-down -24 dB (several can play at once); the overcharge activation -19 dB (the shield restore
-19.0), its end -24 dB; the ready chime -22 dB (UI: the equip sound -24.7, the sonde chime -15.9); the
dash -20 dB (the overdrive start -19.9); the drone break -20 dB (the `tiny` blasts -18.1 / -14.4), the
rebuild -25 dB. The loops are levelled as import_sfx.py levels them (`band_rms`, the whole loop's
200 Hz-5 kHz RMS, one gain, no limiter that would break the seam): the impact sizzle at -38 dB (most of its
energy lies above 5 kHz, so at the beam loops' -30 it would be 4 LU louder than the loop it sits under:
at -38 they reach -27.7 to -29.1 LUFS, 1-2.5 LU under the chosen loop's -26.7), the lance rumble at -36 dB like the ambiences (its weight is under 200 Hz;
its LUFS is printed).

The chosen Ion Beam loop (ion-beam-loop-r36-a.ogg, sfx_r36b.py) is not rebuilt: `--check-loop`
measures it as the game will import it (band RMS, seam, length).

Usage: python3 tools/concept/audio/sfx_r37.py [subject-or-file ...]   e.g. dash beam-impact-r37-b
       python3 tools/concept/audio/sfx_r37.py --check-loop
Deterministic. Needs the cached Freesound originals (tools/concept/audio/freesound_fetch.py
--download); without one a layer falls back to the public HQ preview (printed).
"""
import os
import sys
import tempfile
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
from import_sfx import band_rms_db, make_loop, trim_leading_silence  # noqa: E402
from sfx_r24 import level  # noqa: E402
from sfx_r25 import fs, source  # noqa: E402
from sfx_r32 import loudest_db, recorded, report, shimmer, whoosh  # noqa: E402
from sfx_r32 import thump as sub_thump  # noqa: E402
from sfx_r36 import edge, seam_db, unit  # noqa: E402
from sfx_r36b import circular_noise, harmonic  # noqa: E402
from synth import (SR, decode, exp_decay, lufs, noise, pan, ramp, saw, sine, supersaw, svf,  # noqa: E402
                   undb, write_ogg)

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r37.py"
CC0 = "CC0 1.0"

# already credited for earlier rounds
MACHINE_CHARGE = dict(**fs("Glitchedtones", 375925, 375, 3294528), title="Machine Charge.wav",
                      author="Glitchedtones", licence=CC0)
POWER_DOWN = dict(**fs("noirenex", 159399, 159, 1656228), title="Power Down", author="noirenex", licence=CC0)
ZAP = dict(**fs("JoelAudio", 136542, 136, 1206321), title="ELECTRIC_ZAP_001.wav", author="JoelAudio", licence=CC0)
TESLA = dict(**fs("michael_grinnell", 512471, 512, 7372230), title="Electric zap.wav", author="michael_grinnell",
             licence=CC0)
EARTHQUAKE = dict(**fs("uagadugu", 222521, 222, 255863),
                  title="Cracking Earthquake (cracking soil, cracking stone)", author="uagadugu", licence=CC0)
POWER_UP = dict(**fs("qubodup", 172631, 172, 71257),
                title="Power Up Charge [Remix of LegoLunatic's Charged laser 151243]", author="qubodup", licence=CC0)
PING = dict(**fs("wilhellboy", 351371, 351, 4603244), title="HeavyBulletPing.mp3", author="wilhellboy", licence=CC0)
SMALL_EXPLOSION = dict(**fs("Cyberios", 145788, 145, 2483826), title="Small Explosion", author="Cyberios",
                       licence=CC0)
SMALL_EXPLOSION_B = dict(**fs("dinodilopho", 328833, 328, 4732572), title="Small explosion", author="dinodilopho",
                         licence=CC0)
METAL_IMPACT = dict(**fs("JoMungus", 726486, 726, 11865776), title="Impact on metal", author="JoMungus", licence=CC0)
# new in this round
PLASMA = dict(**fs("jokallset", 395889, 395, 2808036), title="Plasma cutter gas.wav", author="jokallset",
              licence=CC0)
ROCKET = dict(**fs("Maxx222", 446764, 446, 9257296), title="RocketThrustMaxx.wav", author="Maxx222", licence=CC0)
AFTERBURNER = dict(**fs("JSK_Productions", 829068, 829, 17949749),
                   title="Afterburner (Close, Screamer, Quick Onset)", author="JSK_Productions", licence=CC0)

IMPACT = 1.5          # loop lengths (s): every periodic synthesized part completes whole cycles in its loop
RUMBLE = 3.0
ION = 286 / 2.6       # the chosen loop's ion body (sfx_r36b.py: 286 cycles in its 2.60 s loop, 110.0 Hz)

# subject: level (dB, the loudest 100 ms of the 200 Hz-5 kHz band) and ceiling (dBFS), or for a loop
# `band_rms` (the whole loop's 200 Hz-5 kHz RMS) and its length; per variant its layers (a recorded layer:
# source, cut (import_sfx.py's process keys, offsets after its leading-silence trim), optional speed,
# `reverse`, pan; a looped recorded layer: `loop` = (start, length, cross-fade) instead of a cut; a
# synthesized layer: `synth` and its parameters; each enters at `at`, the others `gain` dB from the
# first, measured on the loudest 100 ms).
GRIT = dict(synth="grit", gain=-5.0, seed=3711)
STONE = dict(synth="stone", gain=-10.0, seed=3712)
SOUNDS = {
    "ion-beam-start": dict(level=-24.0, ceiling=-6.0, variants={
        "a": [dict(**MACHINE_CHARGE, cut=dict(offset=0.12, length=0.72, fade=0.25, fadein=0.01, highpass=120),
                   speed=1.8, at=0.0),
              dict(synth="ignite", at=0.0, gain=-4.0, length=0.12, band=(2000.0, 7000.0), seed=3701),
              dict(synth="ion", at=0.0, gain=-3.0, length=0.4, f=(0.5, 1.0), rise=0.12, decay=0.28, seed=3702)],
        "b": [dict(synth="crack", at=0.0, length=0.08, band=(3000.0, 8000.0), seed=3703),
              dict(synth="ion", at=0.0, gain=0.0, length=0.45, f=(2.0, 1.0), rise=0.2, decay=0.25, seed=3704),
              dict(synth="hiss_swell", at=0.02, gain=-10.0, length=0.4, band=(2000.0, 6000.0), seed=3705)]}),
    "ion-beam-stop": dict(level=-25.0, ceiling=-6.0, variants={
        "a": [dict(**POWER_DOWN, cut=dict(offset=0.0, length=0.5, fade=0.3, fadein=0.004, highpass=60), at=0.0),
              dict(synth="ion", at=0.0, gain=-4.0, length=0.55, f=(1.0, 45.0 / ION), rise=0.0, decay=0.45,
                   seed=3706)],
        "b": [dict(synth="ion", at=0.0, length=0.6, f=(1.0, 35.0 / ION), rise=0.0, decay=0.5, seed=3707),
              dict(synth="hiss_fall", at=0.0, gain=-8.0, length=0.5, f=(4000.0, 1000.0), seed=3708)]}),
    "beam-impact": dict(band_rms=-38.0, variants={
        "a": dict(length=IMPACT, layers=[dict(synth="sizzle", seed=3709),
                                         dict(synth="crackle_body", gain=-7.0, seed=3710)]),
        "b": dict(length=IMPACT, layers=[
            dict(**PLASMA, loop=(2.0, IMPACT, 0.3), highpass=800.0, lowpass=7000.0, at=0.0),
            dict(synth="sizzle", gain=-6.0, seed=3713)])}),
    "beam-impact-rock": dict(band_rms=-38.0, variants={
        "a": dict(length=IMPACT, layers=[dict(synth="sizzle", seed=3709),
                                         dict(synth="crackle_body", gain=-7.0, seed=3710), GRIT, STONE]),
        "b": dict(length=IMPACT, layers=[
            dict(**PLASMA, loop=(2.0, IMPACT, 0.3), highpass=800.0, lowpass=7000.0, at=0.0),
            dict(synth="sizzle", gain=-6.0, seed=3713), GRIT, STONE])}),
    "deflector-tick": dict(level=-24.0, ceiling=-5.0, variants={
        "a": [dict(**ZAP, cut=dict(offset=0.0, length=0.2, fade=0.12, fadein=0.001, highpass=300, lowpass=9000),
                   speed=1.6, at=0.0),
              dict(synth="ping", at=0.0, gain=-5.0, length=0.1, partials=(2600.0, 4100.0), tau=0.025, seed=3714)],
        "b": [dict(synth="crack", at=0.0, length=0.03, band=(2000.0, 9000.0), seed=3715),
              dict(synth="ping", at=0.0, gain=-1.0, length=0.1, partials=(1900.0, 3300.0, 5200.0), tau=0.035,
                   droop=0.03, seed=3716)]}),
    "deflector-return": dict(level=-27.0, ceiling=-10.0, variants={
        "a": [dict(synth="ease", length=0.45, f=(520.0, 780.0), rise=0.3, attack=0.06, release=0.15, at=0.0),
              dict(synth="air", at=0.05, gain=-12.0, length=0.4, band=(3000.0, 8000.0), seed=3717)],
        "b": [dict(synth="fifth", length=0.45, notes=(659.26, 987.77), attack=0.18, release=0.2, at=0.0),
              dict(synth="whoosh", at=0.0, gain=-6.0, length=0.45, start=800.0, peak=3000.0, end=2500.0,
                   rise=0.35, fall=0.1, pan=(0.0, 0.0), seed=3718)]}),
    "emp-ring": dict(level=-18.0, ceiling=-1.5, variants={
        "a": [dict(**TESLA, cut=dict(offset=0.0, length=0.22, fade=0.06, fadein=0.001, highpass=80, lowpass=6000), at=0.0),
              dict(**TESLA, cut=dict(offset=0.0, length=0.22, fade=0.06, fadein=0.001, highpass=80, lowpass=6000), speed=0.8,
                   at=0.08, gain=-2.0, pan=-0.45),
              dict(**TESLA, cut=dict(offset=0.0, length=0.22, fade=0.06, fadein=0.001, highpass=80, lowpass=6000), speed=0.65,
                   at=0.2, gain=-4.0, pan=0.5),
              dict(**TESLA, cut=dict(offset=0.0, length=0.22, fade=0.06, fadein=0.001, highpass=80, lowpass=6000), speed=0.5,
                   at=0.35, gain=-7.0),
              dict(synth="thump", at=0.0, gain=-3.0, length=0.6, start=70.0, end=30.0, tau=0.18),
              dict(synth="arcs", at=0.05, gain=-6.0, length=1.3, rate=(900.0, 150.0), seed=3719),
              dict(synth="buzz", at=0.0, gain=-10.0, length=1.2, f=(100.0, 100.0), decay=0.45, seed=3720)],
        "b": [dict(synth="buzz", at=0.0, length=1.3, f=(60.0, 45.0), decay=0.6, seed=3722),
              dict(synth="crack", at=0.0, gain=-5.0, length=0.12, band=(1000.0, 10000.0), seed=3721),
              dict(synth="arcs", at=0.04, gain=-3.0, length=1.3, rate=(1200.0, 150.0), seed=3723),
              dict(synth="thump", at=0.0, gain=-8.0, length=0.6, start=70.0, end=30.0, tau=0.18)]}),
    "emp-powerdown": dict(level=-24.0, ceiling=-8.0, variants={
        "a": [dict(synth="clunk", at=0.0, length=0.12, seed=3724),
              dict(synth="spin_down", at=0.02, gain=0.0, length=0.88, f=(1400.0, 180.0), seed=3725)],
        "b": [dict(synth="flicker", at=0.0, length=0.9, f=(100.0, 70.0), rate=9.0, seed=3726),
              dict(synth="spin_down", at=0.0, gain=-9.0, length=0.85, f=(900.0, 250.0), seed=3727)]}),
    "lance-rumble": dict(band_rms=-36.0, variants={
        "a": dict(length=RUMBLE, layers=[dict(synth="roar", seed=3728),
                                         dict(synth="low_voices", gain=-3.0, f0=123 / RUMBLE, f1=124 / RUMBLE,
                                              seed=3729)]),
        "b": dict(length=RUMBLE, layers=[
            dict(**EARTHQUAKE, loop=(2.0, RUMBLE, 0.6), highpass=30.0, lowpass=1200.0, at=0.0),
            dict(synth="roar", gain=-4.0, seed=3730)])}),
    "overcharge-whoosh": dict(level=-19.0, ceiling=-3.0, variants={
        "a": [dict(**POWER_UP, cut=dict(offset=0.0, length=0.8, fade=0.25, fadein=0.01), speed=0.9, at=0.0),
              dict(synth="whoosh", at=0.0, gain=-5.0, length=0.9, start=400.0, peak=4000.0, end=2500.0, rise=0.7,
                   fall=0.2, pan=(0.0, 0.0), seed=3731),
              dict(synth="shimmer", at=0.55, gain=-8.0, length=0.45, low=2500.0, high=6500.0, partials=9,
                   glide=1.08, flutter=(8.0, 14.0), swell=0.12, seed=3732)],
        "b": [dict(synth="pad_rise", at=0.0, length=0.9, f=(220.0, 440.0), cut=(500.0, 5000.0), seed=3733),
              dict(synth="whoosh", at=0.0, gain=-5.0, length=0.9, start=400.0, peak=4000.0, end=2500.0, rise=0.7,
                   fall=0.2, pan=(0.0, 0.0), seed=3734),
              dict(synth="shimmer", at=0.6, gain=-5.0, length=0.4, low=3000.0, high=8000.0, partials=11,
                   glide=1.1, flutter=(10.0, 18.0), swell=0.1, seed=3735)]}),
    "overcharge-end": dict(level=-24.0, ceiling=-8.0, variants={
        "a": [dict(synth="shimmer", at=0.0, length=0.9, low=2000.0, high=7000.0, partials=10, glide=0.7,
                   flutter=(6.0, 12.0), swell=0.06, seed=3736)],
        "b": [dict(**POWER_UP, cut=dict(offset=0.0, length=0.6, fade=0.02, fadein=0.05, highpass=200, lowpass=4000),
                   reverse=True, at=0.0),
              dict(synth="shimmer", at=0.0, gain=-6.0, length=0.9, low=2500.0, high=6000.0, partials=8, glide=0.75,
                   flutter=(6.0, 10.0), swell=0.4, seed=3737)]}),
    "cooldown-ready": dict(level=-22.0, ceiling=-8.0, variants={
        "a": [dict(synth="bell", at=0.0, length=0.45, f=880.0, tau=0.18),
              dict(synth="bell", at=0.07, gain=0.0, length=0.43, f=1318.51, tau=0.18)],
        "b": [dict(synth="glass", at=0.0, length=0.5, notes=(1046.5, 1567.98), tau=0.25)]}),
    "dash": dict(level=-20.0, ceiling=-4.0, variants={
        "a": [dict(synth="jet", at=0.0, length=0.35, seed=3738),
              dict(synth="roar_burst", at=0.0, gain=-4.0, length=0.3, seed=3739)],
        "b": [dict(**ROCKET, cut=dict(offset=2.0, length=0.35, fade=0.25, fadein=0.008, highpass=40), at=0.0),
              dict(**AFTERBURNER, cut=dict(offset=2.2, length=0.32, fade=0.22, fadein=0.012, highpass=300), at=0.0,
                   gain=-4.0)]}),
    "drone-break": dict(level=-20.0, ceiling=-4.0, variants={
        "a": [dict(**PING, cut=dict(offset=0.0, length=0.3, fade=0.12, fadein=0.001), at=0.0),
              dict(**SMALL_EXPLOSION, cut=dict(offset=0.0, length=0.45, fade=0.2, fadein=0.003, highpass=90),
                   at=0.02, gain=-2.0),
              dict(synth="sparks", at=0.01, gain=-8.0, length=0.35, seed=3740)],
        "b": [dict(**METAL_IMPACT, cut=dict(offset=0.0, length=0.4, fade=0.25, fadein=0.001, highpass=120), at=0.0),
              dict(**SMALL_EXPLOSION_B, cut=dict(offset=0.0, length=0.6, fade=0.3, fadein=0.003, highpass=90),
                   at=0.03, gain=-3.0),
              dict(synth="sparks", at=0.02, gain=-7.0, length=0.4, seed=3741)]}),
    "drone-rebuild": dict(level=-25.0, ceiling=-10.0, variants={
        "a": [dict(synth="materialise", at=0.0, length=0.6, f=(330.0, 440.0), cut=(600.0, 3000.0), seed=3742)],
        "b": [dict(synth="bell", at=0.0, length=0.45, f=659.26, tau=0.15),
              dict(synth="bell", at=0.06, gain=0.0, length=0.45, f=880.0, tau=0.15),
              dict(synth="bell", at=0.12, gain=0.0, length=0.45, f=1318.51, tau=0.18),
              dict(synth="air", at=0.0, gain=-14.0, length=0.55, band=(4000.0, 9000.0), seed=3743)]}),
}


# =========================================================================== synthesized layers (one-shots)

def smooth(t, a, b):
    """Smoothstep from a to b (0 before a, 1 after b)."""
    u = np.clip((t - a) / max(b - a, 1e-9), 0, 1)
    return u * u * (3 - 2 * u)


def ignite(p):
    """An ignition hiss: band noise (no low end, so no pop) with a 2 ms attack, tau 40 ms."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    lo, hi = p["band"]
    chans = [unit(svf(svf(noise(n, rng), np.full(n, lo), mode="hp"), np.full(n, hi))) for _ in range(2)]
    return edge(np.array(chans) * np.clip(t / 0.002, 0, 1) * np.exp(-t / 0.04), 0.001, 0.02)


def crack(p):
    """A bright arc crack: a dense rain of 1-3 ms grains in `band`, fused into one rough burst (never a
    single click), dying in its length."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    out = np.zeros((2, n + int(0.004 * SR)))
    for _ in range(int(400 * p["length"]) + 8):
        k = rng.uniform(0, p["length"]) ** 1.5 / p["length"] ** 0.5
        m = int(rng.uniform(0.001, 0.003) * SR)
        a = int(k * SR)
        x = unit(svf(noise(m, rng), np.full(m, rng.uniform(*p["band"])), q=1.2, mode="bp")) * np.hanning(m)
        out[:, a:a + m] += pan(x * (1 - k / p["length"]) * rng.uniform(0.4, 1.0), rng.uniform(-0.4, 0.4))
    return edge(out[:, :n], 0.001, 0.01)


def ion_voice(f, n, rng):
    """The chosen loop's ion body as a gliding voice: harmonics 1-8 of f (array), a spectral peak at
    330 Hz like the loop's (sfx_r36b.py's body)."""
    x = np.zeros(n)
    for h in range(1, 9):
        fh = h * f
        amp = (1.0 / h ** 0.7) * (1 + 1.4 * np.exp(-((fh - 330.0) / 140.0) ** 2)) * (fh < 9000)
        x += amp * sine(fh, n, start=rng.uniform())
    return x


def ion(p):
    """The ion body (the loop's 110 Hz voice) gliding f[0] -> f[1] x its pitch over `rise` s (smoothstep;
    with rise 0 an exponential slide over the whole layer), held, then decaying over `decay` s."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    if p["rise"] > 0:
        f = ION * (p["f"][0] + (p["f"][1] - p["f"][0]) * smooth(t, 0, p["rise"]))
    else:
        f = ION * ramp(n, p["f"][0], p["f"][1], curve="exp")
    x = ion_voice(f, n, rng)
    env = np.clip(t / 0.01, 0, 1) * np.where(t < p["length"] - p["decay"], 1.0,
                                            np.cos(0.5 * np.pi * smooth(t, p["length"] - p["decay"], p["length"])))
    return edge(np.vstack([x, x]) * env, 0.004, 0.01)


def hiss_swell(p):
    """A hiss in `band` swelling in over 0.1 s and fading out."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    lo, hi = p["band"]
    chans = [unit(svf(svf(noise(n, rng), np.full(n, lo), mode="hp"), np.full(n, hi))) for _ in range(2)]
    env = smooth(t, 0, 0.1) * (1 - smooth(t, 0.15, p["length"]))
    return edge(np.array(chans) * env)


def hiss_fall(p):
    """A hiss whose band falls f[0] -> f[1] Hz as it fades (a beam collapsing)."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    centre = ramp(n, *p["f"], curve="exp")
    chans = [unit(svf(noise(n, rng), centre, q=0.9, mode="bp")) for _ in range(2)]
    return edge(np.array(chans) * (1 - smooth(t, 0, p["length"])) ** 1.5, 0.003, 0.01)


def ping(p):
    """A glassy metal tick: inharmonic partials with a 1 ms attack, decaying tau s (drooping by `droop`),
    a breath of high noise on the onset. No low end."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    x = np.zeros(n)
    for k, f in enumerate(p["partials"]):
        fr = f * (1 - p.get("droop", 0.0) * smooth(t, 0, 0.05))
        x += sine(fr, n, start=rng.uniform()) * np.exp(-t / (p["tau"] * (1 - 0.2 * k))) / (1 + 0.4 * k)
    x += 0.4 * unit(svf(noise(n, rng), np.full(n, 6000.0), mode="hp")) * np.exp(-t / 0.004) * 0.3
    x *= np.clip(t / 0.001, 0, 1)
    return edge(np.vstack([x, x]), 0.0005, 0.01)


def ease(p):
    """Two soft detuned voices (sine + a quiet 2nd and 3rd harmonic) easing f[0] -> f[1] Hz over `rise`
    s (smoothstep: slow at both ends, never a fast sweep), soft attack and release."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    f = p["f"][0] * (p["f"][1] / p["f"][0]) ** smooth(t, 0.03, p["rise"])
    x = sum(sine(f * d, n) + 0.25 * sine(2 * f * d, n) + 0.1 * sine(3 * f * d, n) for d in (0.999, 1.001))
    env = smooth(t, 0, p["attack"]) * (1 - smooth(t, p["length"] - p["release"], p["length"]))
    return np.vstack([x, x]) * env


def air(p):
    """A soft airy noise band fading in and out."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    lo, hi = p["band"]
    chans = [unit(svf(svf(noise(n, rng), np.full(n, lo), mode="hp"), np.full(n, hi))) for _ in range(2)]
    return np.array(chans) * np.sin(np.pi * np.clip(t / p["length"], 0, 1)) ** 2


def fifth(p):
    """A held two-note chord (sines with a quiet octave) fading in over `attack` s, released."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    x = sum(sine(f, n) + 0.2 * sine(2 * f, n) for f in p["notes"])
    env = smooth(t, 0, p["attack"]) * (1 - smooth(t, p["length"] - p["release"], p["length"]))
    return np.vstack([x, x]) * env


def arcs(p):
    """Crackling arcs running outward: fused 1-4 ms band grains (1.5-8 kHz) whose rate falls rate[0] ->
    rate[1] a second and whose pan spreads from the centre to both sides as the ring grows."""
    rng = np.random.default_rng(p["seed"])
    L = p["length"]
    n = int(L * SR)
    out = np.zeros((2, n + int(0.006 * SR)))
    k = 0.0
    while True:
        u = k / L
        k += rng.exponential(1.0 / (p["rate"][0] * (p["rate"][1] / p["rate"][0]) ** u))
        if k >= L:
            break
        m = int(rng.uniform(0.001, 0.004) * SR)
        a = int(k * SR)
        x = unit(svf(noise(m, rng), np.full(m, rng.uniform(1500, 8000)), q=1.0, mode="bp")) * np.hanning(m)
        side = rng.choice((-1.0, 1.0)) * min(0.9, 0.1 + u * 1.2)
        out[:, a:a + m] += pan(x * rng.uniform(0.3, 1.0) * (1 - u) ** 1.2, side)
    return edge(out[:, :n], 0.002, 0.05)


def buzz_wave(f, n, rng, top=4000.0):
    """A buzz with a saw's spectrum (harmonics of f at 1/h up to `top` Hz) but random harmonic phases, so
    its level is continuous: an aligned saw at 50 Hz turns into a train of clicks once band-passed."""
    x = np.zeros(n)
    for h in range(1, int(top / np.min(f)) + 1):
        x += (np.minimum(h * f, top) < top) / h * sine(h * f, n, start=rng.uniform())
    return x


def buzz(p):
    """An electric buzz: saw-spectrum harmonics on f[0] -> f[1] Hz with random phases, band-passed
    400 Hz-3 kHz, decaying tau `decay` s, with a slight slow wobble; a 3 ms attack."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    f = ramp(n, *p["f"], curve="exp") * (1 + 0.004 * np.sin(2 * np.pi * 5.0 * t))
    x = buzz_wave(f, n, rng)
    x = svf(svf(x, np.full(n, 400.0), mode="hp"), np.full(n, 3000.0))
    x = unit(x) * np.clip(t / 0.003, 0, 1) * np.exp(-t / p["decay"])
    return edge(np.vstack([x, x]), 0.002, 0.03)


def clunk(p):
    """A relay clunk: band noise 300-1500 Hz with a 1 ms attack, tau 20 ms (no sub, so no pop) and a
    short 900 Hz body."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    x = unit(svf(svf(noise(n, rng), np.full(n, 300.0), mode="hp"), np.full(n, 1500.0))) * np.exp(-t / 0.02)
    x += 0.5 * sine(np.full(n, 900.0), n) * np.exp(-t / 0.015)
    x *= np.clip(t / 0.001, 0, 1)
    return edge(np.vstack([x, x]), 0.0005, 0.01)


def spin_down(p):
    """A machine's whine running down f[0] -> f[1] Hz (exponential) with its 2nd harmonic and a breath of
    band noise, fading as it falls."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    f = ramp(n, *p["f"], curve="exp")
    x = sine(f, n) + 0.3 * sine(2 * f, n) + 0.15 * unit(svf(noise(n, rng), f * 1.5, q=5.0, mode="bp"))
    env = smooth(t, 0, 0.015) * (1 - smooth(t, 0.1, p["length"])) ** 1.3
    return edge(np.vstack([x, x]) * env, 0.003, 0.01)


def flicker(p):
    """Mains buzz as the lights go out: buzz_wave harmonics of f[0] -> f[1] Hz (up to 3 kHz)
    wobbling in level at `rate` Hz (a smooth sine, deepening from 10 % to 80 %), fading."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    f = ramp(n, *p["f"], curve="exp")
    x = svf(buzz_wave(f, n, np.random.default_rng(p["seed"]), 3000.0), np.full(n, 3000.0))
    depth = 0.1 + 0.7 * smooth(t, 0, p["length"] * 0.7)
    x = unit(x) * (1 - depth * (0.5 + 0.5 * np.sin(2 * np.pi * p["rate"] * t))) * (1 - smooth(t, 0.2, p["length"]))
    return edge(np.vstack([x, x]) * smooth(t, 0, 0.01), 0.002, 0.01)


def pad_rise(p):
    """A soft supersaw pad rising f[0] -> f[1] Hz (smoothstep over 0.75 s) as a low-pass opens
    cut[0] -> cut[1] Hz, swelling in and released at the end."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    u = smooth(t, 0.0, 0.75)
    f = p["f"][0] * (p["f"][1] / p["f"][0]) ** u
    x = supersaw(f, n, voices=5, detune=0.01, rng=rng)
    x = svf(x, p["cut"][0] * (p["cut"][1] / p["cut"][0]) ** u, q=0.9)
    env = smooth(t, 0, 0.25) * (1 - smooth(t, p["length"] - 0.15, p["length"]))
    return np.vstack([x, x]) * env


def bell(p):
    """A small soft bell: a sine and a quiet 2.76x partial (decaying three times faster), 4 ms attack."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    x = sine(np.full(n, p["f"]), n) * np.exp(-t / p["tau"])
    x += 0.18 * sine(np.full(n, p["f"] * 2.76), n) * np.exp(-t / (p["tau"] / 3))
    x *= smooth(t, 0, 0.004)
    return edge(np.vstack([x, x]), 0.002, 0.02)


def glass(p):
    """A soft glass chord: the notes as sines with an 8 ms attack, a quiet high partial (4.1x) shimmering,
    decaying tau s."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    x = sum(sine(np.full(n, f), n) for f in p["notes"])
    x += 0.12 * sine(np.full(n, p["notes"][0] * 4.1), n) * (0.7 + 0.3 * np.sin(2 * np.pi * 11 * t))
    x *= smooth(t, 0, 0.008) * np.exp(-t / p["tau"])
    return edge(np.vstack([x, x]), 0.002, 0.02)


def jet(p):
    """A thruster puff: noise through a band-pass sweeping 600 -> 2500 Hz in 0.04 s and back to 900 Hz
    over the rest, two decorrelated channels, a 6 ms attack."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    rise = 0.04
    centre = np.where(t < rise, 600 * (2500 / 600) ** (t / rise),
                      2500 * (900 / 2500) ** np.clip((t - rise) / (p["length"] - rise), 0, 1))
    chans = [unit(svf(noise(n, rng), centre, q=0.8, mode="bp")) for _ in range(2)]
    env = smooth(t, 0, 0.006) * np.exp(-np.clip(t - rise, 0, None) / 0.09)
    return edge(np.array(chans) * env, 0.002, 0.02)


def roar_burst(p):
    """A short low roar under the puff: noise low-passed at 400 Hz, 10 ms attack, tau 0.1 s."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    chans = [unit(svf(svf(noise(n, rng), np.full(n, 400.0)), np.full(n, 400.0))) for _ in range(2)]
    return edge(np.array(chans) * smooth(t, 0, 0.01) * np.exp(-t / 0.1), 0.003, 0.02)


def sparks(p):
    """A spark spray (sfx_r36b.py's): dense fine noise (3-9 kHz) with a metallic comb, dying in 0.25 s."""
    rng = np.random.default_rng(p["seed"])
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    chans = []
    for _ in range(2):
        x = svf(svf(noise(n, rng), np.full(n, 3000.0), mode="hp"), np.full(n, 9000.0))
        for d in (0.00037, 0.00053, 0.00071):
            k = int(d * SR)
            x[k:] += 0.35 * x[:-k]
        chans.append(unit(x))
    env = np.clip(t / 0.004, 0, 1) * np.exp(-t / 0.08)
    return edge(np.array(chans) * env, 0.002, 0.04)


def materialise(p):
    """A soft filtered saw pad (two voices 0.4 % apart) easing f[0] -> f[1] Hz over 0.45 s as its low-pass
    opens cut[0] -> cut[1] Hz; a 0.15 s attack, faded out over the last 0.15 s."""
    n = int(p["length"] * SR)
    t = np.arange(n) / SR
    u = smooth(t, 0.02, 0.45)
    f = p["f"][0] * (p["f"][1] / p["f"][0]) ** u
    x = saw(f * 0.998, n) + saw(f * 1.002, n)
    x = svf(x, p["cut"][0] * (p["cut"][1] / p["cut"][0]) ** u, q=0.8)
    env = smooth(t, 0, 0.15) * (1 - smooth(t, p["length"] - 0.15, p["length"]))
    return np.vstack([x, x]) * env


def thump(p):
    return sub_thump(p)


SYNTHS = {"ignite": ignite, "crack": crack, "ion": ion, "hiss_swell": hiss_swell, "hiss_fall": hiss_fall,
          "ping": ping, "ease": ease, "air": air, "fifth": fifth, "whoosh": whoosh, "arcs": arcs, "buzz": buzz,
          "thump": thump, "clunk": clunk, "spin_down": spin_down, "flicker": flicker, "shimmer": shimmer,
          "pad_rise": pad_rise, "bell": bell, "glass": glass, "jet": jet, "roar_burst": roar_burst,
          "sparks": sparks, "materialise": materialise}


# =========================================================================== synthesized layers (loops)
# Every frequency below is a whole number of cycles per loop and the noise is circular (inverse FFT), so
# each layer's end continues its start exactly.

def circular_smooth(n, rng, cutoff):
    """Circular random control signal: white noise low-passed in the frequency domain at `cutoff` Hz,
    scaled to unit deviation."""
    freqs = np.fft.rfftfreq(n, 1 / SR)
    spec = (freqs < cutoff) * np.exp(2j * np.pi * rng.random(len(freqs)))
    spec[0] = 0
    return unit(np.fft.irfft(spec, n))


def sizzle(p, L):
    """A dry searing sizzle: circular noise 2-7 kHz whose level flutters fast and randomly (a circular
    control signal low-passed at 60 Hz, depth 50 %; a flutter, not pulses), per channel its own."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    out = []
    for _ in range(2):
        x = circular_noise(n, rng, 2000.0, 7000.0)
        flutter = np.clip(1 + 0.5 * circular_smooth(n, rng, 60.0), 0.2, 2.0)
        out.append(x * flutter)
    return np.array(out)


def crackle_body(p, L):
    """The burning contact's lower body: circular noise 300-1200 Hz breathing slowly (a 4 Hz-wide random
    swell, depth 30 %)."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    return np.array([circular_noise(n, rng, 300.0, 1200.0) * (1 + 0.3 * circular_smooth(n, rng, 4.0))
                     for _ in range(2)])


def grit(p, L):
    """A grit crackle on rock: 2-5 ms grains (1.5-5 kHz, Hann-windowed so none clicks) at ~150 a second,
    placed circularly (a grain over the end wraps to the start)."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    out = np.zeros((2, n))
    for _ in range(int(150 * L)):
        m = int(rng.uniform(0.002, 0.005) * SR)
        a = int(rng.uniform(0, n))
        x = unit(svf(noise(m, rng), np.full(m, rng.uniform(1500, 5000)), q=1.0, mode="bp")) * np.hanning(m)
        g = pan(x * rng.uniform(0.3, 1.0), rng.uniform(-0.5, 0.5))
        idx = (a + np.arange(m)) % n
        out[:, idx] += g
    return out


def stone(p, L):
    """A low stone roar under the grit: circular noise 150-600 Hz."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    return np.array([circular_noise(n, rng, 150.0, 600.0) for _ in range(2)])


def roar(p, L):
    """A deep roar: circular noise 20-500 Hz peaking at 60 Hz with a band up to 400 Hz kept (so small
    speakers carry it), swelling once a loop by 15 %."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    t = np.arange(n) / SR
    out = []
    for ch in range(2):
        x = circular_noise(n, rng, 20.0, 500.0, 60.0, 70.0) + 0.35 * circular_noise(n, rng, 120.0, 400.0)
        out.append(unit(x) * (1 + 0.15 * np.sin(2 * np.pi * t / L + ch)))
    return np.array(out)


def low_voices(p, L):
    """Two low harmonic voices (f0, f1, beating once a loop), harmonics 1-10 at 1/h with a bump at
    200 Hz."""
    rng = np.random.default_rng(p["seed"])
    n = int(round(L * SR))
    base = 1.0 / L
    amp = lambda h, f: (1.0 / h) * (1 + 1.5 * np.exp(-((f - 200.0) / 80.0) ** 2))  # noqa: E731
    return pan(harmonic(p["f0"], n, 10, amp, rng, 1, base), -0.2) + pan(harmonic(p["f1"], n, 10, amp, rng, 2, base),
                                                                         0.2)


LOOP_SYNTHS = {"sizzle": sizzle, "crackle_body": crackle_body, "grit": grit, "stone": stone, "roar": roar,
               "low_voices": low_voices}


# =========================================================================== treatment

def one_shot(subject, v, paths):
    sound = SOUNDS[subject]
    files = iter(paths)
    parts = []
    for layer in sound["variants"][v]:
        if "page" in layer:
            st = recorded(layer, next(files))
            if layer.get("reverse"):     # played backwards and faded out over its length, so it falls
                st = st[:, ::-1] * (1 - smooth(np.arange(st.shape[1]) / SR, 0, st.shape[1] / SR)) ** 1.2
                st = edge(st.copy(), 0.01, 0.01)
            if "pan" in layer:
                st = pan(st.mean(axis=0), layer["pan"])
        else:
            st = SYNTHS[layer["synth"]](layer)
        if parts:
            st = st * undb(loudest_db(parts[0][1]) + layer.get("gain", 0.0) - loudest_db(st))
        parts.append((int(layer["at"] * SR), st))
    out = np.zeros((2, max(at + st.shape[1] for at, st in parts)))
    for at, st in parts:
        out[:, at:at + st.shape[1]] += st
    return level(out, sound["level"], sound["ceiling"])


def loop(subject, v, paths):
    """A loop (sfx_r36b.py's): recorded layers cut with import_sfx.py's make_loop, synthesized layers
    periodic in the loop; summed, DC removed, one gain to the band RMS (no limiter)."""
    sound = SOUNDS[subject]
    spec = sound["variants"][v]
    L = spec["length"]
    n = int(round(L * SR))
    files = iter(paths)
    parts = []
    for layer in spec["layers"]:
        if "page" in layer:
            x = trim_leading_silence(decode(next(files)))
            if "highpass" in layer:
                x = np.array([svf(svf(ch, layer["highpass"], mode="hp"), layer["highpass"], mode="hp") for ch in x])
            if "lowpass" in layer:
                x = np.array([svf(svf(ch, layer["lowpass"]), layer["lowpass"]) for ch in x])
            x = make_loop(x, *layer["loop"], "auto")
            if x.shape[0] == 1:
                x = np.vstack([x, x])
            x = x[:, :n]
        else:
            x = LOOP_SYNTHS[layer["synth"]](layer, L)
        if parts:
            x = x * undb(loudest_db(parts[0]) + layer.get("gain", 0.0) - loudest_db(x))
        parts.append(x)
    out = sum(parts)
    out = out - out.mean(axis=1, keepdims=True)
    return out * undb(sound["band_rms"] - band_rms_db(out))


def layers_of(subject, v):
    spec = SOUNDS[subject]["variants"][v]
    return spec["layers"] if isinstance(spec, dict) else spec


# the user's picks (round 37's review, S.1-S.15, 2026-10-13): the unchosen variants go to concept/rejected/
CHOSEN = {"ion-beam-start": "a", "ion-beam-stop": "b", "beam-impact": "a", "beam-impact-rock": "b",
          "deflector-tick": "a", "deflector-return": "b", "emp-ring": "b", "emp-powerdown": "b",
          "lance-rumble": "b", "overcharge-whoosh": "b", "overcharge-end": "a", "cooldown-ready": "a",
          "dash": "a", "drone-break": "a", "drone-rebuild": "a"}


def out_path(subject, v):
    return (OUT if CHOSEN[subject] == v else OUT / "rejected") / f"{subject}-r37-{v}.ogg"


def build(subject, v, preview_cache):
    layers = [layer for layer in layers_of(subject, v) if "page" in layer]
    with tempfile.TemporaryDirectory() as tmp:
        found = [(layer["author"], *source(layer, preview_cache, tmp)) for layer in layers]
        paths = [path for _, path, _ in found]
        is_loop = "band_rms" in SOUNDS[subject]
        st = loop(subject, v, paths) if is_loop else one_shot(subject, v, paths)
    used = "; ".join(dict.fromkeys(f"{author} from {how}" for author, _, how in found))
    synths = [layer["synth"] for layer in layers_of(subject, v) if "page" not in layer]
    if synths:
        used += ("; " if used else "") + ", ".join(dict.fromkeys(synths)) + " synthesized"
    name = f"{subject}-r37-{v}"
    peak = -1.0 if is_loop else SOUNDS[subject]["ceiling"]
    out = write_ogg(out_path(subject, v), st, max_peak_db=peak, tags={"COMMENT": f"{SCRIPT} {name}: {used}"})
    report(out, used)
    if is_loop:
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")


def check_loop():
    """The chosen Ion Beam loop as the game imports it: length, band RMS, LUFS, seam (decoded)."""
    path = OUT / "ion-beam-loop-r36-a.ogg"
    x = decode(path)
    n = int(round(2.6 * SR))
    print(f"{path.relative_to(ROOT)}: {x.shape[1]} samples ({x.shape[1] / SR:.3f} s; {n} written), band RMS "
          f"{band_rms_db(x):.1f} dB, {lufs(x)[0]:.1f} LUFS, seam {seam_db(x[:, :n]):+.1f} dB decoded, "
          f"peak {20 * np.log10(np.abs(x).max()):.1f} dBFS")
    # the start and stop proposals next to it
    for v in "ab":
        for kind in ("start", "stop"):
            p = out_path(f"ion-beam-{kind}", v)
            if p.exists():
                y = decode(p)
                print(f"  {p.name}: {y.shape[1] / SR:.2f} s, loudest 100 ms {loudest_db(y):.1f} dB (full band)")


def main(argv):
    if argv[:1] == ["--check-loop"]:
        check_loop()
        return
    preview_cache = Path(os.environ.get("TMPDIR", tempfile.gettempdir())) / "conceptgame-sfx-cache"
    preview_cache.mkdir(parents=True, exist_ok=True)
    for subject, sound in SOUNDS.items():
        for v in sound["variants"]:
            if argv and subject not in argv and f"{subject}-r37-{v}" not in argv:
                continue
            build(subject, v, preview_cache)


if __name__ == "__main__":
    main(sys.argv[1:])
