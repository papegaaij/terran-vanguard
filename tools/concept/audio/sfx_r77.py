#!/usr/bin/env python3
"""Concept round 77 (M10 part E "Level 39 Io Flyby", step AUD-E; the gap check's QE-2): Level 39's four new sounds, a
and b each (design/audio/sfx/README.md, *Act 6 sounds*: the eruptions' rocks, the cracking towers, the pump stations):
an eruption rock's crack when it is shot, its thud on the hull, a cracking tower's collapse (a heavy metal groan, a
short blast and the crash of its fall) and a pump station's blast (a pressure line bursting). Round 72's picks for
the eruption itself (`eruption-r72-a`) and Level 39's bed (`io-refinery-r72-a`) are not rebuilt here (sfx_r72.py).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 77 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, filtered, faded, layered and
levelled: nothing is synthesized. Dry throughout (Io's near-vacuum, heard through the hull): rock, stone and metal,
no water, no steam, nothing bubbly or popping; the pump's gas is a rush of air or a rocket's blast, low-passed into
a roar. The tools are sfx_r76.py's (copied: preview, build, the --docs rows), without loops.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  rock-crack-r77-{a,b}       ≈ 0.5 s: a dry rock crack and crumble (a boulder split), no debris rattle tail
  rock-thud-r77-{a,b}        ≈ 0.45 s: a dull stone knock on the hull, played with the `small` impact
  tower-collapse-r77-{a,b}   ≈ 3.4 s: a heavy metal groan as the tower buckles, a short blast, the crash of its fall
  pump-blast-r77-{a,b}       ≈ 1.1 s: a sharp metal blast with a short gas-venting roar

Levels like the accepted picks (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap):
the rock's crack as the mirror's pod burst and the Chimera's shot (an often-heard small sound, -26 dB), its thud a
little over it (a hit on the ship, -24 dB, as the graft's shell crack), the tower's collapse between the harvester's
break and Aurelia's deck groan (a `large` part's death over its rung, -21 dB), the pump's blast as the graft's shell
crack (the `small` rung's weight, -24 dB). The two variants of a subject share the level, so they are
loudness-matched.

Usage: python3 tools/concept/audio/sfx_r77.py [subject-or-file ...]   e.g. rock-crack tower-collapse-r77-b
       python3 tools/concept/audio/sfx_r77.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets
and band balance.
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
from sfx_r43g import cut  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r77.py"
R = "r77"
ROUND = "round 77"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    131933: ('Slips air release v2', 'mcpable', CC0, P + '131/131933_1542102-hq.ogg'),
    186965: ('explosion1.wav', 'ReadeOnly', CC0, P + '186/186965_545448-hq.ogg'),
    209772: ('Metal Boom', 'Johnnyfarmer', CC0, P + '209/209772_2012390-hq.ogg'),
    262118: ('Kirkstall Rock Crumble .wav', 'rokenjocu', CC0, P + '262/262118_4883517-hq.ogg'),
    319222: ('Single Rock hit Dirt.wav', 'worthahep88', CC0, P + '319/319222_3443504-hq.ogg'),
    453333: ('metal crank groan.flac', 'kyles', CC0, P + '453/453333_612689-hq.ogg'),
    489918: ('MetalImpactAgainstRock01', 'falcospizaetus', CC0, P + '489/489918_10570192-hq.ogg'),
    522099: ('Concrete SMASH 2', 'magnuswaker', CC0, P + '522/522099_11537497-hq.ogg'),
    524312: ('Rock destroy', 'Bertsz', CC0, P + '524/524312_6714882-hq.ogg'),
    540091: ('Rocket Blast', 'magnuswaker', CC0, P + '540/540091_11537497-hq.ogg'),
    556714: ('Bending Metal', 'NachtmahrTV', CC0, P + '556/556714_5620304-hq.ogg'),
    570534: ('Laying Down Large Stone on Pile of Large Stones 1.wav', 'F.M.Audio', BY4,
             P + '570/570534_7805242-hq.ogg'),
    587887: ('Explosion in a can', 'SamsterBirdies', CC0, P + '587/587887_5487341-hq.ogg'),
    653294: ('Big Structure Collision Heard from Inside', 'Osvoldon', CC0, P + '653/653294_9808878-hq.ogg'),
    666673: ('Rock drop, rock foley', 'SoundGuy1billione', CC0, P + '666/666673_11885204-hq.ogg'),
    703247: ('Big falling debris (crash)', 'xkeril', CC0, P + '703/703247_13504080-hq.ogg'),
    810170: ('Heavy object drop', 'mokasza', BY4, P + '810/810170_17437502-hq.ogg'),
    843248: ('Explosion 2 (Burning Car rec by Ñado)', 'qubodup', CC0, P + '843/843248_71257-hq.ogg'),
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


sfx_r43g.source = preview  # sfx_r43g.cut() reads its layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


L39 = "Level 39 (Io Flyby)"
ROCKS = (f"{L39}: the eruptions' burning rocks (24 px, 6 HP; 3-5 a column, a vent every 5-7 s while it is on the "
         "screen; 27 vents)")

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (layers, what)}); every subject a "shot": levelled by the loudest 100 ms of the
#             200 Hz-5 kHz band, at most `cap` LUFS.
SOUNDS = {
    "rock-crack": ("shot", -26.0, -6.0, -24.0,
                   f"{ROCKS}: a rock shot to pieces in its flight (the player's or Rook's fire; it pays nothing); "
                   "often while a vent is on the screen, throttled like the other small breaks",
                   "a dry rock crack and crumble: a boulder split, a rockfall's start, short (0.5 s), no debris "
                   "rattle tail; never a pop", {
        "a": ([L(524312, 0.0, 0.5, fin=0.002, fout=0.25, hp=120.0),
               L(262118, 0.0, 0.4, gain=-4.0, at=0.01, fin=0.002, fout=0.2, hp=400.0)],
              "a rock smashed apart (its first half second) with a dry stone crumble on its attack: a hard crack "
              "and a short gritty break, 0.5 s"),
        "b": ([L(522099, 0.0, 0.5, fin=0.002, fout=0.25, hp=120.0),
               L(570534, 0.04, 0.4, gain=-3.0, fin=0.002, fout=0.2, hp=300.0)],
              "a concrete block smashed with large stones knocking on a pile: a heavier, duller break with a "
              "stony knock in it, 0.5 s"),
    }),
    "rock-thud": ("shot", -24.0, -5.0, -22.0,
                  f"{ROCKS}: a rock striking the ship or Rook (10 damage, the rock destroyed), with the `small` "
                  "impact and the armour or shield hit, panned to the ship",
                  "a dull stone knock on the hull: a heavy rock hitting metal, short and low, read as a hit; no "
                  "ring that lasts, no pop", {
        "a": ([L(810170, 0.57, 0.45, fin=0.002, fout=0.25, hp=60.0),
               L(666673, 0.02, 0.35, gain=-2.0, fin=0.002, fout=0.2, hp=150.0)],
              "a heavy object dropped on a floor (a dull low thud) with a rock's knock on its attack: a deep, "
              "blunt hit, 0.45 s"),
        "b": ([L(489918, 0.47, 0.33, fin=0.002, fout=0.2, hp=150.0, lp=7000.0),
               L(319222, 0.25, 0.42, gain=-2.0, fin=0.002, fout=0.2, hp=60.0)],
              "a steel bar striking rock (a hard metallic knock) over a rock thumping into dirt: brighter and "
              "harder than a, more metal than stone, 0.42 s"),
    }),
    "tower-collapse": ("shot", -21.0, -4.0, -22.0,
                       f"{L39}: a cracking tower destroyed (the six named primary targets, Loki, Pele and "
                       "Prometheus A and B; also a Strider toppled onto it), over its `large` rung as its flare cap "
                       "tumbles off and the pipes from it go dark; six times a level",
                       "a heavy metal groan as the tower buckles, a short blast and the crash of its fall; dry (Io's "
                       "near-vacuum, heard through the hull), no pop", {
        "a": ([L(453333, 0.6, 1.9, speed=0.65, fin=0.15, fout=0.6, hp=40.0, lp=4000.0),
               L(209772, 0.18, 1.2, gain=-2.0, at=0.7, fin=0.002, fout=0.6, hp=40.0),
               L(703247, 0.0, 2.0, gain=-3.0, at=1.4, speed=0.85, fin=0.01, fout=0.9, hp=35.0, lp=6000.0)],
              "a metal crank's groan slowed to 0.65 (a column buckling), a metal boom 0.7 s in, then big debris "
              "crashing down slowed to 0.85: groan, bang, crash, 3.4 s"),
        "b": ([L(556714, 0.3, 1.8, speed=0.7, fin=0.15, fout=0.6, hp=80.0, lp=4000.0),
               L(186965, 0.0, 1.0, gain=-2.0, at=0.6, fin=0.002, fout=0.5, hp=40.0),
               L(653294, 0.0, 2.2, gain=-2.0, at=1.3, speed=0.9, fin=0.01, fout=1.0, hp=90.0)],
              "a sheet of metal bending slowed to 0.7 (a longer, grinding buckle), an explosion's boom 0.6 s in, "
              "then a big structure's crash heard from inside slowed to 0.9: deeper and more muffled than a, 3.5 s"),
    }),
    "pump-blast": ("shot", -24.0, -5.0, -22.0,
                   f"{L39}: a pump station destroyed (the secondary's count, 8 on the pipeline racks), over its "
                   "`small` rung as the rack's pipe beside it goes dark",
                   "a sharp metal blast with a short gas-venting roar: a pressure line bursting, the `small` "
                   "rung's weight; dry, no water, no steam, no pop", {
        "a": ([L(587887, 0.0, 0.9, fin=0.002, fout=0.45, hp=60.0),
               L(540091, 0.05, 1.0, gain=-4.0, at=0.08, fin=0.02, fout=0.5, hp=60.0, lp=5000.0)],
              "an explosion in a metal can (a sharp, ringing blast) with a rocket's blast low-passed under it (the "
              "gas roaring out): 1.1 s"),
        "b": ([L(843248, 0.0, 0.5, fin=0.002, fout=0.25, hp=50.0),
               L(131933, 0.3, 0.9, gain=-3.0, at=0.05, speed=0.6, fin=0.02, fout=0.45, hp=150.0, lp=4500.0)],
              "a burning car's fuel blast (a short, deep bang) with a drilling rig's compressed-air release slowed "
              "to 0.6 and low-passed (a hard rush of gas): drier and airier than a, 1.0 s"),
    }),
}

def layers_of(kind, spec):
    return spec


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec))
    raw = mix(spec)
    st = level(raw, band, ceiling)
    loud = lufs(st)[0]
    if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
        st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
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
            parts.append(f"placed at {num(layer['at'])} s" + (" in the loop (once a loop)" if kind == "loop" else ""))
    if kind == "loop":
        if not one_shot:
            parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the "
                         "start)")
        extra = spec[3] if len(spec) > 3 else {}
        if "pulse" in extra:
            parts.append(f"the loop dipping every {num(extra['pulse'][0])} s ({num(extra['pulse'][1])} dB deep)")
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
        lv = band_rms_db(x) if kind == "loop" else short_term_band_db(x)
        size = f"{secs:.2f} s{' loop' if kind == 'loop' else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
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
