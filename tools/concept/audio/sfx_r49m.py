#!/usr/bin/env python3
"""Concept round 49 (step AUD-M): the Level 17 colony dome hit, third try, from real recordings in the Freesound
database after the user's round 46 ("glass being hit is a sharp sound, not a thud") and round 47 ("too small: a is a
bottle cracking, b is nothing like glass breaking; a dome is a very large structure") notes. So each variant is both
sharp and huge: a hard bright strike, then a big structure answering. Three variants, a, b and c.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's pick are fetched after round 49 (freesound_fetch.py --download) and the pick rebuilt from
them with the same settings. This script only cuts, slows, layers, fades, filters and levels them, and (variant b)
places them in a real cathedral by convolving with a real recorded impulse response: nothing is synthesized. No bubbly
or popping sources. The tools are sfx_r47l.py's (copied: preview, build, the --docs rows). Pending the user's review in
round 49; the game keeps the old file (assets/sfx/dome-hit-r40-a.ogg) until the pick.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  dome-hit-r49-a   a hammer striking a glass pane, then a frozen lake's ice cracking with the cracks running away
  dome-hit-r49-b   a hammer striking a glass pane over a struck glass table's ringing an octave down, in a cathedral
  dome-hit-r49-c   a rock slammed onto lake ice, a deep ice boom under it and a tall steel door's groan

Usage: python3 tools/concept/audio/sfx_r49m.py [file ...]          e.g. dome-hit-r49-b
       python3 tools/concept/audio/sfx_r49m.py --docs OUTDIR       also writes the CREDITS.md rows, the sfx README rows
                                                                   and the prompts.md entries for the files made
Deterministic (no randomness, bit-exact ffmpeg flags).
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r43g import L, mix  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r49m.py"
R = "r49"
ROUND = "round 49"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    155619: ("IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav", "samueljustice00", CC0,
             P + "155/155619_2123163-hq.ogg"),
    737896: ("Ice - Lake fractures", "Vrymaa", CC0, P + "737/737896_13973196-hq.ogg"),
    636555: ("R4_00328-8_GlassTable.wav", "kevp888", BY4, P + "636/636555_9034501-hq.ogg"),
    648945: ("IR_Cathedral_5m_Stereo.wav", "Nox_Sound", CC0, P + "648/648945_9250976-hq.ogg"),
    591152: ("rock hitting icy lake", "ultraaxvii", CC0, P + "591/591152_11076342-hq.ogg"),
    264014: ("Door_Metal_Groans_Ext.wav", "Cell31_Sound_Productions", BY4, P + "264/264014_3571756-hq.ogg"),
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


def in_space(dry, ir_sound, wet_db, length):
    """The dry mix plus its convolution with a real recorded impulse response (a stereo IR, scaled to unit energy per
    channel, `wet_db` under the dry), cut to `length` s with a 0.35 s raised-cosine fade-out."""
    ir = preview(ir_sound)
    ir = ir - ir.mean(axis=1, keepdims=True)
    n = dry.shape[1] + ir.shape[1] - 1
    size = 1 << (n - 1).bit_length()
    wet = np.array([np.fft.irfft(np.fft.rfft(dry[c], size) * np.fft.rfft(ir[c] / np.sqrt((ir[c] ** 2).sum()), size),
                                 size)[:n] for c in range(2)])
    out = wet * 10 ** (wet_db / 20)
    out[:, :dry.shape[1]] += dry
    out = out[:, :int(length * SR)]
    k = int(0.35 * SR)
    out[:, -k:] *= 0.5 + 0.5 * np.cos(np.linspace(0, np.pi, k))
    return out


# subject -> (level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (layers, what, space)}); space = None or (impulse-response sound, wet dB, total length s).
# The level is the loudest 100 ms of the 200 Hz-5 kHz band (sfx_r24.level), at most `cap` LUFS, like round 47's.
SOUNDS = {
    "dome-hit": (-19.5, -4.0, -22.0,
                 "every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): "
                 "often, several in a row",
                 "a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a "
                 "hard bright strike and then the big structure answering (a long low glassy resonance or boom, "
                 "cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at "
                 "most 1.2 s, no shatter, not a thud, not a bottle", {
        "a": ([L(155619, 9.95, 0.6, fin=0.002, fout=0.4, hp=120.0),
               L(737896, 10.85, 1.2, gain=-1.0, at=0.02, speed=0.8, fin=0.003, fout=0.55, hp=40.0)],
              "a steel hammer striking a big glass pane on a wall (loud, bright, not breaking) and, right under it, a "
              "frozen lake's ice cracking: one deep crack and then a run of smaller cracks racing away across the "
              "sheet, each with the ice's falling 'zing', slowed to 0.8 so the sheet sounds bigger: sharp, glassy "
              "and wide, 1.2 s",
              None),
        "b": ([L(155619, 3.40, 0.7, speed=0.85, fin=0.002, fout=0.45, hp=100.0),
               L(636555, 0.0, 1.2, gain=-3.0, speed=0.5, fin=0.004, fout=0.6, hp=60.0, lp=4000.0)],
              "the hammer on the glass pane slowed to 0.85 (heavier), over a glass table struck and ringing (a "
              "contact-mic recording) slowed to 0.5, an octave down, so a large glass body hums on after the hit; "
              "both played into a real cathedral (convolved with its recorded impulse response), so it rings out "
              "in a huge space as if heard from far below: a bright 'clank' with a long low glassy 'hmmm', 1.2 s",
              (648945, -4.0, 1.2)),
        "c": ([L(591152, 0.0, 0.8, fin=0.003, fout=0.4, hp=80.0),
               L(737896, 9.40, 1.2, gain=-8.0, at=0.03, speed=0.85, fin=0.003, fout=0.6, hp=120.0),
               L(264014, 10.55, 0.9, gain=-12.0, at=0.25, speed=0.9, fin=0.08, fout=0.5, hp=120.0, lp=2500.0)],
              "a rock slammed onto a frozen lake (a hard icy crack with the ice's 'zing'), on a deep crack and boom "
              "of the lake's thick ice slowed to 0.85, and from 0.25 s a tall steel door's low groan under it, "
              "quiet, like the dome's frame taking the blow: harder and darker than a, no glass pane, 1.2 s",
              None),
    }),
}


def sources_of(layers, space=None):
    return list(dict.fromkeys([layer["sound"] for layer in layers] + ([space[0]] if space else [])))


def build(subject, v):
    band, ceiling, cap, _, _, variants = SOUNDS[subject]
    layers, what, space = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(layers, space))
    raw = mix(layers)
    if space:
        raw = in_space(raw, *space)
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
    band, ceiling, cap = SOUNDS[subject][:3]
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} dBFS "
            f"ceiling, at most {num(cap)} LUFS")


def change_text(subject, layers, layer, space):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
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
    parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
    if layer["at"]:
        parts.append(f"placed at {num(layer['at'])} s")
    parts.append("peak-normalised" + (f" then {num(layer['gain'])} dB" if layer["gain"] else ""))
    if len(layers) > 1:
        parts.append("mixed with the other layers")
    if space:
        parts.append(f"convolved with a cathedral impulse response (freesound {space[0]}) added {num(space[1])} dB "
                     f"under the dry sound, cut to {num(space[2])} s")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def space_text(subject, space):
    return (f"Freesound HQ preview (the original to be fetched if picked), used whole as a convolution impulse response "
            f"(scaled to unit energy per channel): the mixed layers convolved with it and added {num(space[1])} dB under "
            f"the dry sound, cut to {num(space[2])} s with a 0.35 s fade-out, {level_text(subject)}, OGG Vorbis q6")


def licences(layers, space):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(layers, space)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        _, _, _, use, should, variants = SOUNDS[subject]
        layers, what, space = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        entries = [(layer["sound"], change_text(subject, layers, layer, space)) for layer in layers]
        if space:
            entries.append((space[0], space_text(subject, space)))
        for s, change in entries:
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | {change} |")
        x = decode(path)
        secs = x.shape[1] / SR
        size = f"{secs:.2f} s ({short_term_band_db(x):.0f} dB)".replace("(-", "(−")
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in sources_of(layers, space))
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(layers, space)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays at {use}; should sound like {should}. "
                    f"This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        srcs = sources_of(layers, space)
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        edit = (f"{secs:.2f} s, the loudest 100 ms band {short_term_band_db(x):.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, big, nothing bubbly.\n"
                       f"This one: {what}.\nEdit: {edit.replace('-', '−')}.\n")
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
        for v in spec[5]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
