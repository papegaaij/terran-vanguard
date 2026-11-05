#!/usr/bin/env python3
"""Concept round 87 (M11 "Act 7 - Beyond the Gate" design phase, step SFX-R87, 2026-11-05): the Choir Resonator's
fire, third take, a and b. Round 85's glassy takes ("like children's toys") and round 86's punchy ones (a tank-gun
thump; a spark and thunder crack) were turned down: "I'm not sure this will sound great when played in a loop. Also,
the artwork make it look like some sort of soundwave. I'd expect more of a whoosh sound. Neither is one. Both have a
sharp part in it." So these are whooshes: a pressure wave pushed out and away, a soft quick swell (a 15-25 ms raised
cosine, no click, crack, snap or thump) falling away as the wave spreads, with a low-mid push for a weapon's weight.
The chosen hull hit (resonator-hit-r86-a, a hard metal clang) stays and is not rebuilt here.

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, cut, mix, levels, the --docs rows), as
sfx_r86.py. Every layer is a real recording from Freesound (CC0 or CC-BY) taken from its public HQ preview
(cdn.freesound.org, cached in ~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no
download quota used): heavy swings of a spear-hammer and of sticks through the air, a woosh of a blast of air, an
electric train's motor. The recordings are only cut, glided (a falling pitch: the doppler of a heavy thing passing),
layered, faded, filtered and levelled. No glass, bowls, forks or bells; nothing bubbly or popping.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):
  resonator-fire-r87-a.ogg, -b.ogg            the shot (the SOUNDS table says what each is)
  resonator-fire-r87-a-loop.ogg, -b-loop.ogg  4 s of that shot repeated 3 times a second (L4-L5's rate) with the
                                              game's own variation: a random pitch within 5 % of 1 a shot
                                              (PlayerCues: pitch(0.05); the fire plays at a fixed gain), so the user
                                              can judge it in a loop. Same level as the shot (not re-levelled).
Loudness matched to round 86's fire takes as the user hears them, in a loop: each variant's loudest 100 ms of the
200 Hz-5 kHz band (LEVEL, under a -6 dBFS ceiling, at most -22 LUFS) is set so its 3-a-second loop measures round 86's
two takes' mean (their loops: -19.6 and -20.7 LUFS, mean -20.2); a band-level match alone left the short, fast-falling
b 2.5 dB quieter. Each shot falls 20 dB under its peak inside 0.3 s, so 3.5 a second (0.29 s apart) leaves each wave
its own.

Usage: python3 tools/concept/audio/sfx_r87.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r87.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries (the loops' rows included)
Deterministic (the loops' pitches from a fixed seed). Not listened to by Claude: checked by level, envelope, attack,
spectrum, length against the fire rate and, for the loops, the level between the shots and their spectrum.
"""
import sys
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from sfx_r24 import resample  # noqa: E402
from sfx_r43g import L  # noqa: E402
from synth import SR, decode, write_ogg  # noqa: E402

CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    540300: ("Whoosh Heavy Spear Hammer Large", "EminYILDIRIM", BY4, P + "540/540300_10912485-hq.ogg"),
    475135: ("FX - Swoosh - Low Pitch", "bolkmar", CC0, P + "475/475135_2927958-hq.ogg"),
    475131: ("FX - Swoosh - Super Heavy (for swords, sticks, punches, etc)", "bolkmar", CC0,
             P + "475/475131_2927958-hq.ogg"),
    611474: ("Woosh - MediumBlast", "jwsounddesign", BY4, P + "611/611474_13558694-hq.ogg"),
    700034: ("Electric Train Motor Idle - Loop (Queensland Rail InterCity Express).wav", "JotrainG", CC0,
             P + "700/700034_14219512-hq.ogg"),
}

FIRE_USE = ("the Choir Resonator's fire (front gun, on sale from Level 44): one per wave front, 2.5 a second at L1-L3, "
            "3 at L4-L5, 3.5 in overdrive, so the most-heard sound of a Resonator fit; the hull hit (resonator-hit-r86-a, "
            "a hard metal clang) plays over it")
FIRE_SHOULD = ("a whoosh, like the widening wave front it fires: a pressure wave pushed out and away, with body and a "
               "soft quick swell instead of a sharp attack (no click, crack, snap or hard thump), falling away as the "
               "wave spreads; a weapon's weight (a low-mid push), never airy-thin or toy-like; from real recordings "
               "(whooshes, air blasts, pass-bys); 20 dB down inside 0.3 s and good repeated three times a second; no "
               "glass, bowls, forks or bells")

SOUNDS = {
    "resonator-fire": ("shot", -22.0, -6.0, -22.0, FIRE_USE, FIRE_SHOULD, {
        "a": ([L(540300, 0.64, 0.3, speed=(1.3, 0.8), fin=0.022, fout=0.2, hp=35.0, lp=2500.0),
               L(475135, 0.19, 0.28, gain=-4.0, speed=(1.2, 0.8), fin=0.025, fout=0.18, hp=40.0, lp=1500.0),
               L(611474, 0.57, 0.22, gain=-6.0, speed=(1.3, 0.9), fin=0.02, fout=0.15, hp=300.0, lp=5000.0)],
              "\"the heavy pass\": a large spear-hammer swung through the air, its pitch gliding down from 1.3 to 0.8 "
              "like a heavy thing rushing past, a low stick swoosh gliding with it for body and a little of a blast "
              "woosh's air on top: a deep, falling 'VWOOMmm'"),
        "b": ([L(475131, 0.07, 0.2, speed=1.2, fin=0.015, fout=0.13, hp=120.0, lp=6000.0),
               L(611474, 0.58, 0.2, gain=-5.0, speed=1.5, fin=0.015, fout=0.12, hp=250.0, lp=5000.0),
               L(700034, 0.5, 0.26, gain=-7.0, speed=0.8, fin=0.02, fout=0.18, hp=60.0, lp=900.0),
               L(475135, 0.2, 0.22, gain=-8.0, speed=1.1, fin=0.02, fout=0.15, hp=40.0, lp=1200.0)],
              "\"the fwoosh\": a heavy stick swoosh sped to 1.2 and a blast woosh sped to 1.5 for a tight, brighter "
              "rush of air, over an electric train motor's hum slowed to 0.8 and a low swoosh for the weapon's "
              "weight: a quick 'FWOOSH' with a low hum under it"),
    }),
}

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r87.py", "r87", "round 87"

RATE = 3.0  # shots a second in the loop (L4-L5)
LOOP_S = 4.0
PITCH = 0.05  # the game's pitch variation for the player's shots (PlayerCues: pitch(0.05)); no gain variation
SEED = 87
# the loudest 100 ms of the band per variant (dB): their 3-a-second loops at -20.2 LUFS, round 86's fire takes' mean
LEVEL = {"a": -22.6, "b": -19.6}
LOOP_LUFS = -20.2


def set_level(subject, v):
    """Points the shared SOUNDS row (read by sfx_r72's build and its --docs rows) at variant v's level."""
    row = SOUNDS[subject]
    SOUNDS[subject] = (row[0], LEVEL[v]) + row[2:]


def level_text(subject):
    _, band, ceiling, cap = SOUNDS[subject][:4]
    return (f"levelled to {M.num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band; its 3-a-second loop at "
            f"{M.num(LOOP_LUFS)} LUFS, round 86's fire takes' mean) under a {M.num(ceiling)} dBFS ceiling, at most "
            f"{M.num(cap)} LUFS")


M.level_text = level_text


def shot_train(x, rate=RATE, seconds=LOOP_S, pitch=PITCH, seed=SEED):
    """`seconds` s of the decoded shot `x` played `rate` times a second, each at a random speed within `pitch` of 1."""
    rng = np.random.default_rng(seed)
    out = np.zeros((2, int(seconds * SR)))
    k = 0
    while k / rate < seconds:
        y = resample(x, rng.uniform(1 - pitch, 1 + pitch))
        a = int(round(k / rate * SR))
        n = min(y.shape[1], out.shape[1] - a)
        out[:, a:a + n] += y[:, :n]
        k += 1
    return out


def build_loop(subject, v, shot_path):
    name = f"{subject}-{M.R}-{v}-loop"
    st = shot_train(decode(shot_path))
    what = (f"{subject}-{M.R}-{v} repeated {RATE:g} times a second for {LOOP_S:g} s, each shot at a random pitch "
            f"within {PITCH:.0%} of 1 (the game's variation)")
    out = write_ogg(M.OUT / f"{name}.ogg", st, max_peak_db=-1.0, tags={"COMMENT": f"{M.SCRIPT} {name}: {what}"})
    M.report(out, what)
    return out


def loop_docs(outdir, made):
    """The loops' rows: CREDITS (the shot's sources, the loop made of it), the README rows, the prompts entries."""
    outdir = Path(outdir)
    credits, rows, prompts = [], [], []
    for subject, v, _ in made:
        kind, _, _, _, _, _, variants = SOUNDS[subject]
        spec = variants[v][0]
        shot, name = f"{subject}-{M.R}-{v}", f"{subject}-{M.R}-{v}-loop"
        how = (f"the shot {shot}.ogg (its own edit) played {RATE:g} times a second for {LOOP_S:g} s, each at a random "
               f"pitch within {PITCH:.0%} of 1 (seeded), not re-levelled, OGG Vorbis q6; a review aid only")
        for s in M.sources_of(kind, spec):
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| design/audio/sfx/concept/{name}.ogg | {title} | {author} | [freesound]({url}) | "
                           f"[{lic}]({M.LICENCE_URL[lic]}) | {how} |")
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Review aid for `{subject}` {v} (the same sources, "
                    f"[CREDITS.md](../../../CREDITS.md)): [{shot}](concept/{shot}.ogg) played {RATE:g} times a second "
                    f"for {LOOP_S:g} s with the game's ±{PITCH:.0%} pitch variation, to judge it in a loop, 4.00 s | "
                    f"proposed ({M.ROUND}) |")
        prompts.append(f"### {name} — review loop of {shot}\n\nProposed ({M.ROUND}), rendered by `{M.SCRIPT}` from "
                       f"{shot}.ogg: {how}.\n")
    with open(outdir / "credits.md", "a") as f:
        f.write("\n".join(credits) + "\n")
    with open(outdir / "rows.md", "a") as f:
        f.write("\n".join(rows) + "\n")
    with open(outdir / "prompts.md", "a") as f:
        f.write("\n" + "\n".join(prompts))
    print(f"docs: {len(credits)} loop CREDITS rows, {len(rows)} loop README rows, {len(prompts)} loop prompts entries")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[6]:
            if argv and subject not in argv and f"{subject}-{M.R}-{v}" not in argv:
                continue
            set_level(subject, v)
            path = M.build(subject, v)
            build_loop(subject, v, path)
            made.append((subject, v, path))
    if outdir:
        parts = {"credits.md": [], "rows.md": [], "prompts.md": []}
        for item in made:  # one variant at a time: each one's level in its rows
            set_level(item[0], item[1])
            M.docs(outdir, [item])
            for name, texts in parts.items():
                texts.append((Path(outdir) / name).read_text())
        for name, texts in parts.items():
            (Path(outdir) / name).write_text(("\n" if name == "prompts.md" else "").join(texts))
        loop_docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
