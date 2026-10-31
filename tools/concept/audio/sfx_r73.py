#!/usr/bin/env python3
"""Concept round 73 (M10 part A "Act 6 systems", step AUD-A; the gap check's QA-1): the mirror set beyond round 72's
ping and the Shield Pylon's dome hit, a and b each (design/audio/sfx/README.md, *Act 6 sounds*): the mirror's heat loop
(with a review render of the climb the game plays), its break, its regrow chime, the pylon dome's hit and the Reflex
bolt absorbed on a mirror's face.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 73 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, sped, glided, layered, faded, filtered,
looped and levelled: nothing is synthesized. No bubbly or popping sources. The tools are sfx_r72.py's (copied: preview,
bed, build, the --docs rows) plus the heat climb's review render ("demo").

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  mirror-heat-r73-{a,b}        seamless 2 s LOOPs: a beam held on a mirror face (a gas torch on sheet steel / an
                               arc welder's hum); the game raises its pitch with the heat (0.85 at 0 % to 1.35 at
                               100 %, the break)
  mirror-heat-demo-r73-{a,b}   ≈ 1.2 s review renders: the loop with its pitch gliding 0.85 → 1.35 over 1.0 s, the
                               climb as the game plays it for a mirror heated to its break in 1.0 s
  mirror-break-r73-{a,b}       ≈ 2.8 s / 0.95 s: a long glass pane shattering / tempered glass bursting into grains
  mirror-regrow-r73-{a,b}      ≈ 1.0 s: a singing bowl struck softly (a low soft chime)
  pylon-dome-hit-r73-{a,b}     ≈ 0.45 s: a short electric zap with a glassy ring
  bolt-absorbed-r73-a          ≈ 0.1 s: a short dry electric tick, quiet

Levels like round 72's (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the loops and
the climb renders by their band RMS): the two variants of a subject share the level, so they are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r73.py [subject-or-file ...]   e.g. mirror-break mirror-heat-r73-b
       python3 tools/concept/audio/sfx_r73.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets
and the loop seams.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import Loop, cut, glide, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r73.py"
R = "r73"
ROUND = "round 73"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    62923: ('welder 2.wav', 'SpeedY', CC0, P + '62/62923_6479-hq.ogg'),
    136542: ('ELECTRIC_ZAP_001.wav', 'JoelAudio', CC0, P + '136/136542_1206321-hq.ogg'),
    198401: ('ANI - Wine Glass - Hard', 'ani_music', CC0, P + '198/198401_3008343-hq.ogg'),
    244230: ('Gas Torch Burn', 'SyntaxSeed', CC0, P + '244/244230_4465058-hq.ogg'),
    264779: ('Civilian Stun Gun 1/10s zap', 'AnthonyChan0', CC0, P + '264/264779_2886479-hq.ogg'),
    271370: ('singing bowl strike sound', 'inoshirodesign', CC0, P + '271/271370_5159066-hq.ogg'),
    336425: ('Glass Shattering and Falling', '221Beimesche', CC0, P + '336/336425_4663534-hq.ogg'),
    349688: ('070916 Machine room transformer hum 1.wav', 'thaighaudio', CC0, P + '349/349688_2205380-hq.ogg'),
    386382: ('Torch - Full work cycle of cutting metal', 'ldezem', CC0, P + '386/386382_1655965-hq.ogg'),
    448070: ('23cm glass bowl - tone - wooden spoon 01.wav', 'Anthousai', CC0, P + '448/448070_5923045-hq.ogg'),
    456827: ('Quick static noise', 'Rvgerxini', CC0, P + '456/456827_9453283-hq.ogg'),
    587928: ('BROKEN CAR WINDOW.wav', 'thecommondanger', CC0, P + '587/587928_8805852-hq.ogg'),
    616335: ('Singing Bowl - Soft Stick Meditative Hit', 'Rimych', CC0, P + '616/616335_11045601-hq.ogg'),
}
_decoded = {}


def preview(sound):
    """The decoded HQ preview, fetched from the CDN into the cache when missing."""
    if sound not in _decoded:
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        _decoded[sound] = decode(path)
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() and loop() read their layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def bed(layers, length, xfade):
    """sfx_r43g.loop(), its mean removed."""
    st = loop(layers, length, xfade)
    return st - st.mean(axis=1, keepdims=True)


# The heat climb the game plays (A3): the loop's pitch from HEAT_PITCH[0] at 0 % heat to HEAT_PITCH[1] at the break.
HEAT_PITCH = (0.85, 1.35)


def climb(loop_spec, seconds, rise):
    """The review render of a heat loop: the loop played for `seconds` with its speed gliding over HEAT_PITCH in the
    first `rise` s (then held), faded in 0.05 s and out 0.15 s."""
    st = bed(*loop_spec)
    n = int(seconds * SR)
    reps = int(np.ceil(n * HEAT_PITCH[1] / st.shape[1])) + 1
    x = np.tile(st, reps)
    k = int(rise * SR)
    out = np.concatenate([glide(x, HEAT_PITCH[0], HEAT_PITCH[1], k),
                          np.zeros((2, n - k))], axis=1)
    # the held top: the source continues where the glide ended, at the top speed
    pos = HEAT_PITCH[0] * k + (HEAT_PITCH[1] - HEAT_PITCH[0]) * k / 2 + HEAT_PITCH[1] * np.arange(n - k)
    out[:, k:] = np.array([np.interp(pos, np.arange(x.shape[1]), ch) for ch in x])
    fi, fo = int(0.05 * SR), int(0.15 * SR)
    out[:, :fi] *= 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, fi))
    out[:, n - fo:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fo))
    return out


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS;
#             "demo": (loop subject, seconds, rise), a loop's climb render levelled as that loop.
HEAT_USE = ("a beam held on a Mirror Interceptor's mirror face (Levels 38-42; the Ascendant's panels too): one loop "
            "while any mirror heats, for the nearest, its pitch rising with the heat (0.85 at 0 % to 1.35 at the "
            "break, about 1.0 s of the plan's beam) and stopping when the beam leaves or the mirror breaks; often in "
            "a mirror fight, under the beam's own loop")
HEAT_SHOULD = ("a rising hot-metal hiss: a gas torch on sheet steel, its pitch climbing as the plate goes red to "
               "white; dry (no quench, no steam, nothing bubbly)")
SOUNDS = {
    "mirror-heat": ("loop", -34.0, -1.0, -28.0, HEAT_USE, HEAT_SHOULD + "; this file is the steady loop the game "
                    "pitches", {
        "a": (([Loop(244230, 4.0, hp=80.0), Loop(386382, 44.0, gain=-4.0, hp=200.0)], 2.0, 0.5),
              "a gas torch's steady burn over the hiss of a cutting torch biting into steel plate: a dry roaring "
              "hiss, bright on top, 2.0 s loop"),
        "b": (([Loop(349688, 5.0, hp=180.0, lp=1500.0), Loop(62923, 4.0, gain=-2.0, hp=150.0)], 2.0, 0.5),
              "an arc welder at work: a machine-room transformer's hum under the welding arc's dry crackle, more of "
              "an electric hum than a, 2.0 s loop"),
    }),
    "mirror-heat-demo": ("demo", -34.0, -1.0, -28.0,
                         "review file for the heat loop: what the game plays while a beam heats a mirror to its break "
                         "in 1.0 s (the loop gliding 0.85 → 1.35); the game plays the loop itself",
                         HEAT_SHOULD, {
        "a": (("mirror-heat", "a", 1.25, 1.0),
              "loop a (the gas torch on steel) climbing over 1.0 s, as heard in the game, 1.25 s"),
        "b": (("mirror-heat", "b", 1.25, 1.0),
              "loop b (the arc welder's hum) climbing over 1.0 s, as heard in the game, 1.25 s"),
    }),
    "mirror-break": ("shot", -22.0, -5.0, -22.0,
                     "a Mirror Interceptor's mirror breaking (Levels 38-42): broken by its heat, a `shield-breaker` "
                     "hit or the EMP, as its shards fly (and the Ascendant's panels); a reward, a few a fight",
                     "a real glass pane shattering, heavier and longer than the enemy shield's break (round 60's "
                     "glass break) and apart from the player shield's break (a falling zap), so the three never mix", {
        "a": ([L(336425, 0.0, 2.8, speed=0.85, fin=0.002, fout=0.8, hp=80.0)],
              "a glass pane shattering and its shards falling, slowed to 0.85 so it reads as a big heavy plate: "
              "a long crash with a rain of shards, 2.8 s"),
        "b": ([L(587928, 0.02, 0.95, fin=0.002, fout=0.3, hp=120.0)],
              "a car's side window (tempered glass) breaking: a bright burst into small grains, shorter and drier "
              "than a, 0.95 s"),
    }),
    "mirror-regrow": ("shot", -28.0, -8.0, -28.0,
                      "a mirror coming back (Levels 38-42): with its 0.4 s silver shimmer, 3 s after a break; now "
                      "and then, the tell that the mirror stands again",
                      "a low soft chime: a singing bowl struck softly, short; never a ping (the bounce) or a bell's "
                      "alarm", {
        "a": ([L(616335, 0.0, 1.0, speed=1.5, fin=0.03, fout=0.6, hp=150.0)],
              "a deep singing bowl struck with a soft stick, sped to 1.5: a warm low hum-tone that blooms and fades, "
              "1.0 s"),
        "b": ([L(271370, 0.03, 1.0, fin=0.03, fout=0.6, hp=200.0)],
              "a smaller singing bowl struck softly: a brighter, clearer chime than a, ringing higher, 1.0 s"),
    }),
    "pylon-dome-hit": ("shot", -26.0, -6.0, -26.0,
                       "a shot hitting a Shield Pylon's dome (Level 41; throttled: once per 0.15 s), the dome "
                       "absorbing it; often in a pylon gate",
                       "a short bright electric zap with a glassy ring; never the Level 17 colony dome's big glass "
                       "boom, never the mirror's ping", {
        "a": ([L(264779, 0.1, 0.2, fin=0.002, fout=0.08, hp=300.0),
               L(448070, 0.0, 0.45, gain=-3.0, speed=1.5, at=0.02, fin=0.002, fout=0.3, hp=400.0)],
              "a stun gun's tenth-of-a-second zap with a glass bowl struck under it (sped to 1.5): a crackling snap "
              "and a short clear ring, 0.47 s"),
        "b": ([L(136542, 0.05, 0.15, fin=0.002, fout=0.06, hp=300.0),
               L(198401, 0.0, 0.45, gain=-3.0, at=0.02, fin=0.002, fout=0.3, hp=400.0)],
              "an electric zap's buzzy snap with a wine glass struck hard under it: a thinner, higher ring than a, "
              "0.47 s"),
    }),
    "bolt-absorbed": ("shot", -32.0, -10.0, -32.0,
                      "a Reflex Shield bolt hitting a standing mirror's face (Levels 38-42; a spark, no bounce): "
                      "now and then while the Reflex turns enemy fire, quiet, under the fire",
                      "a short dry electric tick; quiet", {
        "a": ([L(456827, 0.0, 0.1, fin=0.001, fout=0.05, hp=800.0)],
              "a quick burst of static cut to a tick: a dry electric crackle, 0.1 s"),
    }),
}


def layers_of(kind, spec):
    if kind == "demo":
        subject, v = spec[0], spec[1]
        return layers_of("loop", SOUNDS[subject][6][v][0])
    if kind != "loop":
        return spec
    return list(spec[0])


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec))
    if kind in ("loop", "demo"):
        if kind == "loop":
            st = bed(*spec)
            # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest:
            # the encoder's edges then make no step at the join
            w = st[:, :int(0.2 * SR)]
            k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
            st = np.roll(st, -k, axis=1)
        else:
            st = climb(SOUNDS[spec[0]][6][spec[1]][0], spec[2], spec[3])
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = mix(spec)
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind == "loop":
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind in ("loop", "demo"):
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz), at most {num(cap)} LUFS"
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    one_shot = "length" in layer
    loop_spec = SOUNDS[spec[0]][6][spec[1]][0] if kind == "demo" else spec
    if not one_shot:
        span = (loop_spec[1] + loop_spec[2]) * speed
    else:
        span = layer["length"] * (np.mean(speed) if isinstance(speed, tuple) else speed)
    parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
    if isinstance(speed, tuple):
        parts.append(f"played at a speed gliding {num(speed[0])} → {num(speed[1])}")
    elif speed != 1.0:
        parts.append(f"slowed to {num(speed)}" if speed < 1 else f"sped to {num(speed)}")
    if layer.get("swell"):
        parts.append(f"swelling from {num(layer['swell'][0])} dB up to full over the layer")
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
    else:
        parts.append(f"made a seamless {num(loop_spec[1])} s loop (its last {num(loop_spec[2])} s cross-faded into "
                     "the start)")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers_of(kind, spec)) > 1:
        parts.append("mixed with the other layers")
    if kind == "demo":
        parts.append(f"the loop played {num(spec[2])} s with its speed gliding {num(HEAT_PITCH[0])} → "
                     f"{num(HEAT_PITCH[1])} over {num(spec[3])} s (a review render of the game's climb)")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(kind, spec):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(kind, spec)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        kind, _, _, _, use, should, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in layers_of(kind, spec):
            s = layer["sound"]
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, kind, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        looped = kind == "loop"
        lv = band_rms_db(x) if kind in ("loop", "demo") else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if looped else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if kind in ('loop', 'demo') else 'the loudest 100 ms band'} {lv:.1f} dB, "
                f"peak {db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit.replace('-', '−')}.\n")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[6]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
