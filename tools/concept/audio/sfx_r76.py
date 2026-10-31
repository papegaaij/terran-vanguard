#!/usr/bin/env python3
"""Concept round 76 (M10 part D "Level 38 Lightning Belt", step AUD-D; the gap check's QD-2): the Harrow's two sounds
round 72 did not propose, a and b each (design/audio/sfx/README.md, *Act 6 sounds*, the Harrow's row): its hunting
whistle (one loop for the nearest hunting Harrow) and its ram's whoosh. Round 72's picks for its lock tell
(`harrow-lock-r72-b`) and its burst (`harrow-burst-r72-a`) are not rebuilt here (sfx_r72.py).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 76 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped, filtered, faded, looped and levelled:
nothing is synthesized. No bubbly or popping sources: the whistle is a jet's turbine whine or falling bombs' fins,
the whoosh a heavy swing or a car's close pass. The tools are sfx_r75.py's (copied: preview, bed, build, the --docs
rows).

The hunting whistle is a seamless loop, steady in level (a loop cannot rise: the game raises its pitch over the
chase, as the Buzzsaw's whine follows its spin), high-passed so it stays thin over the storm bed and above the lock's
lower whine.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  harrow-whistle-r76-{a,b}   seamless 2 s LOOPs: a thin high whistle (a jet's approach whine / falling bombs' fins)
  harrow-ram-r76-{a,b}       ≈ 0.9 s: a hard whoosh (a heavy swing / a car's close pass)

Levels like the accepted picks: the whistle loop as the Buzzsaw's whine and the Mirror's hum (its band RMS, −36 dB),
the ram as a one-shot over the lock (the loudest 100 ms of the 200 Hz-5 kHz band −20 dB, at most −20 LUFS); the two
variants of a subject share the level, so they are loudness-matched.

--bed-check mixes Level 38's storm as the game plays it (the `jovian-sky` bed at the ambience level +3 dB, the
storm's thunder at −2 dB, the strike's own roll) and prints how far the strike's close crack and the Harrow's sounds
stand over it (each sound's loudest 100 ms against the storm's band level under it).

Usage: python3 tools/concept/audio/sfx_r76.py [subject-or-file ...]   e.g. harrow-ram harrow-whistle-r76-b
       python3 tools/concept/audio/sfx_r76.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
       python3 tools/concept/audio/sfx_r76.py --bed-check              only the storm check (reads the built files)
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
SCRIPT = "tools/concept/audio/sfx_r76.py"
R = "r76"
ROUND = "round 76"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    479924: ('R12-27-Falling Bombs Whistle.wav', 'craigsmith', CC0, P + '479/479924_2524442-hq.ogg'),
    517738: ('Fast Car Passing by', 'danlucaz', CC0, P + '517/517738_9996727-hq.ogg'),
    540300: ('Whoosh Heavy Spear Hammer Large', 'EminYILDIRIM', BY4, P + '540/540300_10912485-hq.ogg'),
    805168: ('Jet Flyby 01', 'mrspivey', CC0, P + '805/805168_17325486-hq.ogg'),
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


def bed(layers, length, xfade, extra=None):
    """sfx_r43g.loop() plus `events`: one-shot layers placed once in the loop at their `at` (wrapping round the
    seam)."""
    st = loop(layers, length, xfade)
    n = st.shape[1]
    for ev in (extra or {}).get("events", []):
        x = cut(ev)
        idx = (int(ev["at"] * SR) + np.arange(x.shape[1])) % n
        np.add.at(st[0], idx, x[0])
        np.add.at(st[1], idx, x[1])
    return st


HARROW = "a Harrow (Levels 38-42, the hybrid kamikaze dart that locks on and rams; often several at once)"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS.
SOUNDS = {
    "harrow-whistle": ("loop", -36.0, -1.0, -30.0,
                       f"{HARROW}: while it hunts (its 1.5-2.5 s chase before the lock), one loop for the nearest "
                       "hunting Harrow on the screen, panned to it; the game raises its pitch over the chase and "
                       "stops it at the lock tell (round 72's turbine whine takes over)",
                       "a thin rising wind whistle: a real jet's fly-by or a whistling dive, cut thin (the low roar "
                       "taken away), under the fire and over the storm's wind", {
        "a": (([Loop(805168, 1.1, hp=1000.0, lp=8000.0)], 2.0, 0.3),
              "a jet's fly-by at its approach, the roar cut away under 1 kHz: a thin, airy turbine whistle "
              "around 1.7 kHz, 2.0 s loop"),
        "b": (([Loop(479924, 3.2, hp=1000.0, lp=8000.0)], 2.0, 0.3),
              "falling bombs' fins whistling, cut thin the same way: a purer, higher whistle around 2.1 kHz with "
              "the bombs' slow pitch drift in it, 2.0 s loop"),
    }),
    "harrow-ram": ("shot", -20.0, -3.0, -20.0,
                   f"{HARROW}: each ram, at the lock tell's end as it shoots straight along its bearing at 300 px/s "
                   "(after the turbine whine); a miss flies out through the far edge; at most two at once",
                   "a hard whoosh: something heavy cutting the air fast past the ship, short and forceful; never a "
                   "pop", {
        "a": ([L(540300, 0.5, 0.8, speed=1.1, fin=0.02, fout=0.3, hp=60.0)],
              "a large heavy spear or hammer swung hard, sped to 1.1: a deep, forceful whoosh with a whistle in "
              "its edge, peaking 0.2 s in, 0.8 s"),
        "b": ([L(517738, 3.3, 0.9, speed=1.25, fin=0.02, fout=0.3, hp=80.0)],
              "a car passing close at speed, sped to 1.25: a rougher, roaring rush with the pass's pitch drop, "
              "peaking 0.25 s in, 0.9 s"),
    }),
}


def layers_of(kind, spec):
    if kind != "loop":
        return spec
    extra = spec[3] if len(spec) > 3 else {}
    return list(spec[0]) + list(extra.get("events", []))


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
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest:
        # the encoder's edges then make no step at the join
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


# ---- --bed-check: the close crack and the Harrow's sounds over Level 38's storm, as the game mixes them ----

ASSETS = ROOT / "assets" / "sfx"
AMBIENCE_VOLUME = 0.70  # vanguard.game.audio.LevelMusic.AMBIENCE_VOLUME: the beds' level
STORM_DB = 3.0  # Level 38's `ambience_db` in its storm sections (the level README's Music)
THUNDER_DB = -2.0  # vanguard.game.audio.FlightSounds.EXPLOSIONS: the flashes' thunder (Level 12's files)
OCTAVES = [(177.0, 354.0), (354.0, 707.0), (707.0, 1414.0), (1414.0, 2828.0), (2828.0, 5657.0)]


def band_signal(x, lo, hi):
    mono = x.mean(axis=0)
    spec = np.fft.rfft(mono)
    freqs = np.fft.rfftfreq(len(mono), 1 / SR)
    spec[(freqs < lo) | (freqs >= hi)] = 0
    return np.fft.irfft(spec, len(mono))


def window_db(sig, at, n):
    return db(np.sqrt(np.mean(sig[at:at + n] ** 2)) + 1e-12)


def loudest_at(x, upto=None):
    """The start sample of the loudest 100 ms of the 200 Hz-5 kHz band (within the first `upto` s)."""
    n = int(0.1 * SR)
    sig = band_signal(x, 200.0, 5000.0)
    end = sig.shape[0] - n if upto is None else min(sig.shape[0] - n, int(upto * SR))
    starts = range(0, max(1, end + 1), n // 4)
    return max(starts, key=lambda i: window_db(sig, i, n))


def margins(sound, storm, at):
    """(band margin, best octave margin, its octave) of `sound`'s 100 ms from sample `at` over `storm`'s same
    window (both as the game plays them)."""
    n = int(0.1 * SR)
    whole = window_db(band_signal(sound, 200.0, 5000.0), at, n) - window_db(band_signal(storm, 200.0, 5000.0), at, n)
    best = max((window_db(band_signal(sound, lo, hi), at, n) - window_db(band_signal(storm, lo, hi), at, n), lo, hi)
               for lo, hi in OCTAVES)
    return whole, best


def bed_check():
    bed = decode(ASSETS / "jovian-sky-r72-b.ogg") * AMBIENCE_VOLUME * undb(STORM_DB)
    thunder = decode(ASSETS / "thunder-1-r34-b.ogg") * undb(THUNDER_DB)
    strike = decode(ASSETS / "lightning-strike-r72-b.ogg")
    concept = ROOT / "design" / "audio" / "sfx" / "concept"
    tests = [("lightning-strike-r72-b (the close crack)", strike, 0.25)]
    for name in ["harrow-lock-r72-b", "harrow-burst-r72-a", "harrow-whistle-r76-a", "harrow-whistle-r76-b",
                 "harrow-ram-r76-a", "harrow-ram-r76-b"]:
        tests.append((name, decode(concept / f"{name}.ogg"), None))
    # for reference: the Buzzsaw's whine, the built loop of a unit that hunts (Levels 26 and 38)
    tests.append(("saw-whine-r64-a (reference)", decode(ASSETS / "saw-whine-r64-a.ogg"), None))
    n = bed.shape[1]
    # the storm under each sound: the bed alone (its loudest 100 ms), with a flash's thunder at its loudest, and with
    # the previous strike's roll 1.5 s on (the strikes' least spacing)
    t_peak = loudest_at(thunder)
    print(f"storm: jovian-sky-r72-b at {AMBIENCE_VOLUME} x +{STORM_DB:g} dB (band RMS "
          f"{band_rms_db(bed):.1f} dB), thunder-1-r34-b at {THUNDER_DB:g} dB, the strike's roll 1.5 s after its crack")
    print(f"{'sound':42s} {'over the bed':>20s} {'+ thunder at its peak':>24s} {'+ a strike 1.5 s before':>26s}")
    for name, x, upto in tests:
        at = loudest_at(x, upto)
        bed_at = loudest_at(bed)
        cols = []
        for extra, offset in ((None, 0), (thunder, t_peak), (strike, int(1.5 * SR))):
            # the sound placed so its loudest 100 ms lies on the bed's loudest 100 ms (and the extra's chosen point)
            start = bed_at - at
            storm = np.roll(bed, -start, axis=1)
            if extra is not None:
                e = np.zeros_like(storm)
                k = offset - at
                seg = extra[:, max(0, k):]
                pos = max(0, -k)
                e[:, pos:pos + seg.shape[1]] = seg[:, :n - pos]
                storm = storm + e
            sound = np.zeros_like(storm)
            sound[:, :x.shape[1]] = x[:, :n]
            whole, (best, lo, hi) = margins(sound, storm, at)
            cols.append(f"{whole:+5.1f} / {best:+5.1f} ({lo / 1000:.1f}-{hi / 1000:.1f}k)")
        print(f"{name:42s} {cols[0]:>20s} {cols[1]:>24s} {cols[2]:>26s}")
    print("each column: the 200 Hz-5 kHz band margin / the best octave's margin (dB) of the sound's loudest 100 ms "
          "over the storm in that window")


def main(argv):
    if "--bed-check" in argv:
        bed_check()
        return
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
