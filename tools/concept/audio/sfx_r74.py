#!/usr/bin/env python3
"""Concept round 74 (M10 part B "Level 36 Cloud Divers", step AUD-B; the gap check's QB-2 and QB-3): Level 36's own
sounds and the Chimera's set, a and b each (design/audio/sfx/README.md, *Act 6 sounds*): the dive's wind (an ambience
layer over `jovian-sky`), the fleet's drop craft diving past, the Helium-3 harvester's scoop drone and its break-up,
and the Chimera's turn-in whine, twin crack and death (its healing creak is round 72's chosen `regrowth-creak`).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 74 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, sped, glided, layered, faded, filtered,
looped and levelled: nothing is synthesized. No bubbly or popping sources. The tools are sfx_r73.py's (copied: preview,
bed, build, the --docs rows).

The Chimera is the Talon's airframe overgrown, so its a variants are the Talon's built sounds rebuilt with their round-62
settings (sfx_r62.py `talon-whine` a and `talon-shot` b, the same previews and levels): the whine as is (QB-2 a) and the
twin crack as is (the unit's brief); the b variants are the same cuts played lower (QB-2 b, with a grit layer under the
whine). Its death is the Talon's crack with a short dry tear under it (a: the hybrids' chosen tear's cloth, round 72
S.2 a; b: heavy fabric), over the `small` rung the game plays with it.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  dive-wind-r74-{a,b}          seamless 12 s LOOPs: the roar of a fast descent (a skydiver's freefall / a car window
                               open on a motorway); Level 36's layer over `jovian-sky`, t=0-24, faded by the game
  drop-craft-pass-r74-{a,b}    ≈ 2.9 s: a pair of real jet fly-bys, fast, doppler (Eurofighters / two jets)
  harvester-scoop-r74-{a,b}    seamless 8 s LOOPs: a huge industrial intake's low drone (a dust extractor / a turbine
                               fan, slowed)
  harvester-break-r74-{a,b}    ≈ 2.8 s: tearing metal and a gas venting roar (no water, no steam hiss)
  chimera-whine-r74-{a,b}      ≈ 0.6 / 0.75 s: the turn-in whine (the Talon's as is / pitched down with grit)
  chimera-shot-r74-{a,b}       ≈ 0.35 / 0.4 s: the dry twin crack (the Talon's as is / pitched down)
  chimera-death-r74-{a,b}      ≈ 0.55 s: the Talon's crack with a short dry tear under it

Levels like round 73's (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the loops by
their band RMS): the two variants of a subject share the level, so they are loudness-matched; the Chimera's a variants
keep the Talon's levels (sfx_r62.py), so a is the built file.

Usage: python3 tools/concept/audio/sfx_r74.py [subject-or-file ...]   e.g. dive-wind chimera-whine-r74-b
       python3 tools/concept/audio/sfx_r74.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
from sfx_r43g import Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r74.py"
R = "r74"
ROUND = "round 74"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    162258: ('Mk 46 30mm gun - single shot', 'qubodup', CC0, P + '162/162258_71257-hq.ogg'),
    197127: ('Wood dust extractor.wav', 'paulw2k', BY4, P + '197/197127_755728-hq.ogg'),
    243763: ('gas vuur dakwerken gasbrander luchtballon.wav', 'NickPeeters', CC0, P + '243/243763_877079-hq.ogg'),
    453476: ('motor industrial turbine large fan blades spin sorta.flac', 'kyles', CC0, P + '453/453476_612689-hq.ogg'),
    491407: ('Jet engine startup', 'SamsterBirdies', CC0, P + '491/491407_5487341-hq.ogg'),
    556714: ('Bending Metal', 'NachtmahrTV', CC0, P + '556/556714_5620304-hq.ogg'),
    640499: ('Eurofighter Typhoon Flyby 003 – Close Proximity', 'TimoSchmied', BY4, P + '640/640499_4424897-hq.ogg'),
    640500: ('Eurofighter Typhoon Flyby 002 – Close Proximity', 'TimoSchmied', BY4, P + '640/640500_4424897-hq.ogg'),
    640501: ('Eurofighter Typhoon Flyby 001 – Close Proximity', 'TimoSchmied', BY4, P + '640/640501_4424897-hq.ogg'),
    669692: ('designed metal shearing, ripping.wav', 'vestibule-door', CC0, P + '669/669692_14100561-hq.ogg'),
    685077: ('Car driving in highway with muffled wind, front window open recorded from the inside', 'felix.blume',
             CC0, P + '685/685077_1661766-hq.ogg'),
    708104: ('heavy fabric tearing v2, thick fabric tearing, tearing.Recorded on H6Zoom XY capsule', 'Rinaldobowles56',
             CC0, P + '708/708104_14713648-hq.ogg'),
    719003: ('Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast', 'ChristopherJngs', CC0,
             P + '719/719003_8154783-hq.ogg'),
    805168: ('Jet Flyby 01', 'mrspivey', CC0, P + '805/805168_17325486-hq.ogg'),
    805169: ('Jet Flyby 02', 'mrspivey', CC0, P + '805/805169_17325486-hq.ogg'),
    811077: ('Solo Skydive Interior Plane and Freefall', 'DangerLaef', CC0, P + '811/811077_16473-hq.ogg'),
    854469: ('M142 HIMARS Rocket Launch 1', 'qubodup', CC0, P + '854/854469_71257-hq.ogg'),
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


# The Talon's built sounds (sfx_r62.py): the whine a and the twin crack b, the Chimera's a variants as is.
TALON_WHINE = [L(640499, 3.0, 0.6, speed=1.2, fin=0.15, fout=0.25, hp=600.0)]
TALON_SHOT = [L(162258, 0.0, 0.3, speed=1.4, fin=0.001, fout=0.15, hp=250.0),
              L(162258, 0.0, 0.3, gain=-2.0, at=0.05, speed=1.5, fin=0.001, fout=0.15, hp=250.0)]
L36 = "Level 36 (Cloud Divers)"
CHIMERA = "a Chimera (Levels 36-42, the Talon's airframe overgrown with Vrell tissue)"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
SOUNDS = {
    "dive-wind": ("loop", -38.0, -1.0, -28.0,
                  f"{L36}, section 1 (t=0-24): an ambience layer over the `jovian-sky` bed while the squadron dives "
                  "out of the carrier group into the bands, faded in at the start and out as the dive levels out (the "
                  "game fades the loop); under the music's intro",
                  "the roar of a fast descent, wide: a skydiver's wind or a car window open at speed on a motorway; "
                  "a steady rushing roar, no gusts that read as weather (the bed has those), nothing whistling", {
        "a": (([Loop(811077, 54.0, hp=50.0, lp=6000.0)], 12.0, 1.5),
              "a skydiver's freefall recorded on the jumper: a dense close roar of air with the buffeting in it, the "
              "highs rolled off at 6 kHz, 12.0 s loop"),
        "b": (([Loop(685077, 30.0, hp=50.0)], 12.0, 1.5),
              "a car on a motorway with the front window open, from inside: a smoother, wider rush with the road's "
              "low drone under it, less buffeting than a, 12.0 s loop"),
    }),
    "drop-craft-pass": ("shot", -24.0, -4.0, -22.0,
                        f"{L36}, t=4 and t=10: each pair of the fleet's drop craft (Stormhawks, Rook's airframe) diving "
                        "past the squadron into the bands; twice a level, over the music's intro and the dive's wind",
                        "a pair of real jet fly-bys, fast, with the doppler drop: two jets close together, the second "
                        "a beat behind the first", {
        "a": ([L(640500, 4.0, 2.6, speed=1.25, fin=0.25, fout=1.0, hp=60.0),
               L(640501, 1.0, 2.6, gain=-2.0, at=0.3, speed=1.3, fin=0.25, fout=1.0, hp=60.0)],
              "two Eurofighters' close fly-bys sped to 1.25 and 1.3, the second 0.3 s behind: a doubled roar with a "
              "sharp doppler fall, 2.9 s"),
        "b": ([L(805168, 0.3, 2.5, speed=1.2, fin=0.2, fout=1.0, hp=60.0),
               L(805169, 0.0, 2.5, gain=-2.0, at=0.3, speed=1.3, fin=0.2, fout=1.0, hp=60.0)],
              "two jet fly-bys sped to 1.2 and 1.3, the second 0.3 s behind: a broader, rumblier rush than a, the "
              "doppler gentler, 2.8 s"),
    }),
    "harvester-scoop": ("loop", -35.0, -1.0, -28.0,
                        f"{L36}'s secondary: the Helium-3 harvesters' scoops (one loop for the nearest harvester while "
                        "any scoops), through sections 3-5 (the harvester fields), under the fight",
                        "a huge industrial intake's low drone: a turbine or a dust extractor, slowed; steady and heavy, "
                        "no hiss of steam, no whine on top", {
        "a": (([Loop(197127, 10.0, speed=0.5, hp=40.0, lp=3000.0)], 8.0, 1.0),
              "a wood workshop's dust extractor slowed to half: a deep roaring suction drone, 8.0 s loop"),
        "b": (([Loop(453476, 5.0, speed=0.6, hp=40.0, lp=3500.0)], 8.0, 1.0),
              "a large industrial turbine fan slowed to 0.6: a lower, more mechanical hum with the blades' slow beat "
              "in it, 8.0 s loop"),
    }),
    "harvester-break": ("shot", -23.0, -5.0, -22.0,
                        f"{L36}: a Helium-3 harvester destroyed (the secondary's count), over its `large` rung; a few "
                        "times a level",
                        "tearing metal and a gas venting roar: the hull ripping open and the gas rushing out; no water, "
                        "no hiss of steam, no pop", {
        "a": ([L(669692, 4.0, 2.2, speed=0.7, fin=0.005, fout=0.8, hp=80.0),
               L(243763, 8.0, 2.6, gain=-3.0, at=0.15, speed=0.7, fin=0.05, fout=1.2, hp=40.0, lp=4000.0)],
              "metal shearing and ripping slowed to 0.7 over a big gas burner's roar slowed to 0.7: a grinding tear "
              "into a deep rushing roar, 2.8 s"),
        "b": ([L(556714, 0.32, 2.5, speed=0.75, fin=0.005, fout=0.8, hp=60.0),
               L(854469, 0.0, 2.8, gain=-3.0, at=0.1, speed=0.85, fin=0.03, fout=1.2, hp=40.0, lp=5000.0)],
              "a sheet of metal bending and groaning slowed to 0.75 over a rocket launch's roar slowed to 0.85: a "
              "slower, groaning break with a harder blast of gas than a, 2.9 s"),
    }),
    "chimera-whine": ("shot", -28.0, -8.0, -26.0,
                      f"{CHIMERA}: each swoop's turn-in (a pair, or one), throttled to one per wing; often, under the "
                      "fire",
                      "the Talon's short turn-in whine with a lower, rougher edge: the same airframe, a different rule "
                      "(QB-2: a the Talon's whine as is, b pitched down with a grit layer)", {
        "a": (TALON_WHINE,
              "the Talon's built whine as is (sfx_r62.py talon-whine a): a Eurofighter's approach sped to 1.2 and "
              "high-passed to its whine, a thin rising 'wheee', 0.6 s"),
        "b": ([L(640499, 3.0, 0.75, speed=0.95, fin=0.15, fout=0.3, hp=350.0),
               L(491407, 19.0, 0.75, gain=-9.0, speed=0.8, fin=0.12, fout=0.3, hp=300.0, lp=3000.0)],
              "the same approach played at 0.95 (about four semitones under the Talon's) with a lit jet engine's "
              "rough roar slowed to 0.8 under it: a lower, grittier whine, 0.75 s"),
    }),
    "chimera-shot": ("shot", -28.0, -6.0, -24.0,
                     f"{CHIMERA}: each twin shot (two shots side by side, heard as one); often",
                     "the Talon's dry twin crack: a real cannon, two shots heard as one doubled crack, the enemy shot "
                     "small's weight", {
        "a": (TALON_SHOT,
              "the Talon's built twin crack as is (sfx_r62.py talon-shot b): a 30 mm gun's single shot sped to 1.4, "
              "twice 50 ms apart, 0.35 s"),
        "b": ([L(162258, 0.0, 0.35, speed=1.15, fin=0.001, fout=0.15, hp=200.0),
               L(162258, 0.0, 0.35, gain=-2.0, at=0.05, speed=1.22, fin=0.001, fout=0.15, hp=200.0)],
              "the same gun's shot sped only to 1.15 and 1.22, twice 50 ms apart: a lower, boomier doubled crack, "
              "the whine b's family, 0.4 s"),
    }),
    "chimera-death": ("shot", -26.0, -6.0, -24.0,
                      f"{CHIMERA} destroyed, over the `small` rung the game plays with it; often",
                      "the Talon's crack with a short dry tearing sound under it (fabric or fibre, never flesh, never "
                      "wet)", {
        "a": ([L(162258, 0.0, 0.3, speed=1.4, fin=0.001, fout=0.15, hp=250.0),
               L(719003, 0.35, 0.45, gain=-4.0, at=0.06, speed=0.6, fin=0.005, fout=0.2, hp=100.0)],
              "the Talon's 30 mm crack with cotton cloth torn by hand, slowed to 0.6, under it (the hybrids' chosen "
              "tear, round 72 S.2 a, cut short): a crack and a short dry rip, 0.5 s"),
        "b": ([L(162258, 0.0, 0.3, speed=1.4, fin=0.001, fout=0.15, hp=250.0),
               L(708104, 2.33, 0.4, gain=2.0, at=0.06, speed=0.8, fin=0.01, fout=0.2, hp=120.0)],
              "the same crack with thick fabric torn in half, slowed to 0.8, under it: a coarser, heavier rip than "
              "a, 0.5 s"),
    }),
}


def layers_of(kind, spec):
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
    if kind == "loop":
        st = bed(*spec)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest: the
        # encoder's edges then make no step at the join
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
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
    if kind == "loop":
        return f"levelled to {num(band)} dB band RMS (200 Hz–5 kHz), at most {num(cap)} LUFS"
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    one_shot = "length" in layer
    if not one_shot:
        span = (spec[1] + spec[2]) * speed
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
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers_of(kind, spec)) > 1:
        parts.append("mixed with the other layers")
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
        lv = band_rms_db(x) if looped else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if looped else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if looped else 'the loudest 100 ms band'} {lv:.1f} dB, "
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
