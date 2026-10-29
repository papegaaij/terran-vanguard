#!/usr/bin/env python3
"""Concept round 67 (M9 part G "Level 34 Salvage Run", step AUD-G): Level 34's new sounds, a and b each
(design/audio/sfx/README.md, *Act 5 sounds*; the Level 34 document's *Music & ambience*): a cargo container shot
open, the freighter hold's clamps releasing, the Helix core's pickup and the wrecks' metal groans over the open-space
bed. Not proposed (used as they are): the open-space bed (round 60's pick S.18 a, belt-open-r60-a, in the game since
part C), the mining charges' blast (round 65, `charge-blast`), the Warden Tank's tracks, turret, burst and death
(Level 19's: the livery changes no sound), the secrets' `SECRET` cue and the large salvage pickup (the core's pickup
plays over it), Level 29's Dock 9 clamp release (round 62 S.7 b, concept/clamp-release-r62-b.ogg: offered in the round
as the hold clamps' fallback) and the music (track 12's base and motif stems, no new file).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 67 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, faded,
swelled, filtered and levelled; nothing is synthesized. The tools are sfx_r65.py's (copied: preview, build, the --docs
rows) plus a "layer" kind: one-shots placed on a silent canvas, so the file loops seamlessly (silent at both ends).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  container-burst-r67-*  ≈ 1.3-1.45 s: a cargo container shot open (12 HP), a latch blowing and a short metal clang,
                         over the container's own destruction sound
  hold-clamp-r67-*       ≈ 1.0-1.25 s: one of the freighter hold's four clamps destroyed (15 HP), a heavy hydraulic
                         unlock
  helix-core-r67-*       ≈ 2.0-2.2 s: the Helix core collected, a deep power hum swelling, over the large salvage
                         pickup
  wreck-groans-r67-*     30 s seamless LOOPs: three slow metal groans of the wrecks with silence between, an ambience
                         layer over `belt-open` (the level's `ambience_layers`, t=0-174)

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap): the container as the small ground target's crumble (−21), the clamp as round 62's Dock 9 clamp release
(−25.5), the core's hum a few dB under the large salvage pickup (−17.7). The groans (re-levelled 2026-10-29, round 67
S.4 re-shown: "both are too quiet") by their loudest short-term loudness (EBU R128, 3 s), both variants to the same
−13.2 LUFS under a −1.5 dBFS ceiling: in the game's mix (the layer and the bed at 0.35, track 12's base stem at 0.425)
a groan peaks 1 dB under the bed + music's short-term loudness, clearly heard over them
(tools/concept/audio/wreck_groans_mix_r67.py measures it and writes the review's solo and in-mix files).

Usage: python3 tools/concept/audio/sfx_r67.py [subject-or-file ...]   e.g. hold-clamp wreck-groans-r67-b
       python3 tools/concept/audio/sfx_r67.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets and
the loop seams.
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
from sfx_r43g import Loop, cut, loop  # noqa: E402,F401
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, limiter, lufs, undb, write_ogg  # noqa: E402
from wreck_groans_mix_r67 import ebur128  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r67.py"
R = "r67"
ROUND = "round 67"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    143615: ('industrial_nailgun_1shot+ring', 'gilly11', BY3, P + "143/143615_2047402-hq.ogg"),
    175663: ('big_metal_unlock_and_slam.wav', 'simpsi', BY3, P + "175/175663_2762119-hq.ogg"),
    380403: ('Metal Creaks_Groans.wav', 'Cell31_Sound_Productions', BY4, P + "380/380403_3571756-hq.ogg"),
    406197: ('door metal big heavy close kinda slam thud echo offmic.wav', 'kyles', CC0,
             P + "406/406197_612689-hq.ogg"),
    416080: ('Air release', 'davidlay1', CC0, P + "416/416080_7175960-hq.ogg"),
    500008: ('liner gangplank groaning.WAV', 'phonoflora', CC0, P + "500/500008_401348-hq.ogg"),
    516591: ('Large Bolt Cutter', 'jameswrowles', CC0, P + "516/516591_3874107-hq.ogg"),
    556714: ('Bending Metal', 'NachtmahrTV', CC0, P + "556/556714_5620304-hq.ogg"),
    581083: ('Pressure Hiss', 'magnuswaker', CC0, P + "581/581083_11537497-hq.ogg"),
    677736: ('Transformer buzz', 'logant547', CC0, P + "677/677736_14767014-hq.ogg"),
    709257: ('Large Industrial Machine Starting up and running', 'ryks88', BY4, P + "709/709257_8516444-hq.ogg"),
    784981: ('Motors_Hydraulic_Handycap_Ramp', 'SoundsofTheMachine', CC0, P + "784/784981_7602203-hq.ogg"),
    814977: ('Closing large metal door-002', 'AudioPapkin', CC0, P + "814/814977_8698658-hq.ogg"),
    863975: ('Automotive Two Post Lift', 'j_soundeffects', BY4, P + "863/863975_14030247-hq.ogg"),
}
SYNTH = {}  # nothing synthesized this round

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

# subject -> (kind, level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "layer": (layers, length): one-shots placed on a silent canvas of `length` s (a
#             seamless loop: silent at both ends), levelled to `level` LUFS of its loudest short-term loudness (3 s)
#             under the ceiling (no cap).
L34 = "../../campaign/act-5-the-belt/level-34-salvage-run/README.md"

SOUNDS = {
    "container-burst": ("shot", -22.0, -5.0, -20.0, "Level 34: a cargo container shot open",
                        "a cargo container destroyed (Level 34: six, t≈18, 38, 58, 80, 150 and 170; 12 HP, no "
                        "bounty; the secondary `CONTAINERS n/6` counts them), over its destruction, as its salvage "
                        "pickup of 50 (328) drifts out",
                        "a cargo latch blowing and a short metal clang: a latch or bolt cracking, then the "
                        "container's steel door slamming open; short and dry, no pop", f"{L34}#music--ambience", {
        "a": ([L(516591, 0.0, 0.35, speed=0.85, fin=0.001, fout=0.2, hp=80.0),
               L(814977, 0.12, 1.3, gain=-1.0, at=0.04, fin=0.003, fout=0.6, hp=50.0)],
              "a large bolt cutter snapping a bolt (slowed to 0.85), then a big metal door slammed 40 ms later: a "
              "sharp 'KTAK' into a ringing 'KLANGG', 1.35 s"),
        "b": ([L(143615, 1.67, 0.3, fin=0.002, fout=0.15, hp=100.0),
               L(175663, 5.44, 1.4, speed=0.9, at=0.05, fin=0.003, fout=0.7, hp=50.0)],
              "an industrial nail gun's shot (a pneumatic bang), then a heavy garage door's metal slam (slowed to "
              "0.9) 50 ms later: a drier 'PTAK' into a lower, boomier 'BLAMM' with the garage's echo, 1.45 s"),
    }),
    "hold-clamp": ("shot", -25.0, -5.0, -20.0, "Level 34: a hold clamp released",
                   "each of the freighter hold's four clamps destroyed (Level 34, t≈108-120; 15 HP, no bounty; a "
                   "player who shoots at once clears all four in about 2 s, so up to four close together); with the "
                   "fourth the Helix core floats up out of the hold",
                   "a heavy hydraulic unlock: a locking bolt clacking free and the pressure venting; heavier than "
                   "Level 29's Dock 9 clamp (concept/clamp-release-r62-b.ogg, the fallback: answer 'dock')",
                   f"{L34}#music--ambience", {
        "a": ([L(863975, 15.57, 0.9, speed=0.75, fin=0.002, fout=0.45, hp=45.0),
               L(406197, 0.31, 0.9, gain=-3.0, at=0.02, fin=0.003, fout=0.5, hp=35.0, lp=4000.0),
               L(581083, 0.0, 0.6, gain=-6.0, at=0.06, fin=0.005, fout=0.35, hp=300.0, lp=9000.0)],
              "a car lift's hydraulic carriage dropping onto its lock (slowed to 0.75) over a heavy steel door's "
              "thud, with a pressure valve's hiss over it: a heavy 'KLAKK-pssshh', 0.95 s"),
        "b": ([L(784981, 10.42, 1.25, speed=0.8, fin=0.01, fout=0.5, hp=40.0),
               L(416080, 0.0, 0.9, gain=-8.0, at=0.15, fin=0.01, fout=0.5, hp=400.0, lp=7000.0)],
              "a van's hydraulic ramp locking home (slowed to 0.8) with an air release venting under its tail: a "
              "lower, rattling 'kr-KUNK-unk' with a longer 'hhhhh', 1.25 s"),
    }),
    "helix-core": ("shot", -22.0, -6.0, -20.0, "Level 34: the Helix core salvaged",
                   "once, the Helix core collected (Level 34, t≈116-124: flown through as it drifts after the "
                   "clamps; `HELIX CORE SALVAGED`), over the large salvage pickup; Vorne's motif stem is on in the "
                   "music then; not the secrets' cue; a missed core plays nothing (Okafor's tug line)",
                   "a deep power hum swelling: a big machine or transformer powering up as the core comes alive, "
                   "about 2 s (the sfx README's row: the large salvage pickup with a short power-up hum)",
                   f"{L34}#music--ambience", {
        "a": ([L(677736, 0.7, 2.0, speed=(0.55, 1.0), fin=0.05, fout=0.5, hp=40.0, swell=(-18.0, 1.0))],
              "a power transformer's mains hum glided up from 0.55 to its own speed while it swells 18 dB: a deep, "
              "even 'mmmMMMM' rising in pitch, 2.0 s"),
        "b": ([L(709257, 1.97, 2.2, speed=(0.6, 0.9), fin=0.02, fout=0.6, hp=35.0, swell=(-10.0, 1.2))],
              "a large industrial air scrubber switching on, slowed (0.6 gliding to 0.9) and swelling: a rougher "
              "machine 'vrrumMMMM' with the motor's kick at its start, 2.2 s"),
    }),
    "wreck-groans": ("layer", -13.2, -1.5, None, "Level 34: the wrecks' metal groans (a layer over `belt-open`)",
                     "a sparse layer over the `belt-open` bed through the ship graveyard (Level 34, t=0-174: the "
                     "level's `ambience_layers`, faded in over 3 s and out over 3 s before section 6's clear "
                     "space), looping at the ambience's level",
                     "the wrecks' slow metal groans: huge hulls stressing in the cold, a few long low groans with "
                     "quiet between; not rhythmic, no clanks or pops; under the music, heard in the quiet stretches",
                     f"{L34}#music--ambience", {
        "a": (([L(380403, 1.4, 3.4, speed=0.6, at=1.0, fin=0.4, fout=1.2, hp=35.0, lp=6000.0),
                L(380403, 18.3, 3.0, speed=0.6, gain=-3.0, at=11.5, fin=0.4, fout=1.0, hp=35.0, lp=6000.0),
                L(380403, 28.9, 4.2, speed=0.6, gain=-1.0, at=20.5, fin=0.4, fout=1.4, hp=35.0, lp=6000.0)], 30.0),
              "three groans of stressed metal slowed to 0.6 (deep and slow), 1.0, 11.5 and 20.5 s into the loop "
              "with quiet between: long low 'grrroooann's, 30 s loop"),
        "b": (([L(500008, 26.7, 3.6, speed=0.55, at=1.5, fin=0.4, fout=1.2, hp=35.0, lp=5000.0),
                L(556714, 0.25, 4.0, speed=0.65, gain=-2.0, at=11.0, fin=0.3, fout=1.5, hp=35.0, lp=5000.0),
                L(500008, 37.0, 4.0, speed=0.55, gain=-1.0, at=20.0, fin=0.4, fout=1.4, hp=35.0, lp=5000.0)], 30.0),
              "a cruise liner's steel gangplank groaning (slowed to 0.55) twice and a bending metal sheet (slowed "
              "to 0.65) between them, 1.5, 11 and 20 s into the loop: creakier, more grinding groans than a, 30 s "
              "loop"),
    }),
}


def short_term_max(x):
    """The loudest short-term loudness (EBU R128, 3 s window) of a signal, LUFS."""
    return float(np.max(ebur128(x)[1]))


def layers_of(kind, spec):
    if kind == "synth":
        return []
    return spec[0] if kind in ("loop", "layer") else spec


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    spec, what = SOUNDS[subject][8][v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    elif kind == "layer":
        layers, length = spec
        raw = mix(layers)
        assert raw.shape[1] <= int(length * SR), f"{name}: the layers run past the loop's {length} s"
        raw = np.pad(raw, ((0, 0), (0, int(length * SR) - raw.shape[1])))
        st = raw - raw.mean(axis=1, keepdims=True)
        for _ in range(3):  # the limiter lowers the level a little; make it up
            st = limiter(st * undb(band - short_term_max(st)), ceiling)
    else:
        raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
        st = level(raw, band, ceiling)
        loud = lufs(st)[0]
        if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
            st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    if kind in ("loop", "layer"):
        x = decode(out)
        print(f"  loop: {st.shape[1]} samples written, {x.shape[1]} decoded; band RMS {band_rms_db(x):.1f} dB; "
              f"seam {seam_db(st):+.1f} dB written, {seam_db(x[:, :st.shape[1]]):+.1f} dB decoded")
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 67 ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    if kind == "layer":
        return (f"levelled to {num(band)} LUFS of its loudest short-term loudness (3 s) under a {num(ceiling)} dBFS "
                f"ceiling")
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
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
        if kind == "layer":
            parts.append(f"in a {num(spec[1])} s loop (silent at both ends)")
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
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        kind, _, _, _, _, use, should, _, variants = SOUNDS[subject]
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
        lv = band_rms_db(x) if kind == "loop" else short_term_band_db(x)
        looped = kind in ("loop", "layer")
        info[(subject, v)] = (secs, what)
        size = f"{secs:.2f} s{' loop' if looped else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-67/: the same depth as design/audio/sfx)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        kind, _, _, _, title, use, should, part, variants = SOUNDS[subject]
        what = " ".join(f"**{v}** ({info[(subject, v)][0]:.2f} s): {info[(subject, v)][1]}." for v in variants
                        if (subject, v) in info)
        files = ", ".join(f"[{v}](../../audio/sfx/concept/{subject}-{R}-{v}.ogg)" for v in variants)
        rnd.append(f"| S.{k} | [{title} (`{subject}`)]({part}) | **pick a or b**. **Plays:** {use}. **Should sound "
                   f"like:** {should}. {what} | {files} | | **a** / b / redo | |")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    (outdir / "round.md").write_text("\n".join(rnd) + "\n")
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries, {len(rnd)} "
          f"round rows in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[8]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
