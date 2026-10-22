#!/usr/bin/env python3
"""Concept round 47 (step AUD-L): five sounds redone from real recordings in the Freesound database after the user's
round 46 notes, an a/b pair each. Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public
HQ preview (cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 47 (freesound_fetch.py --download) and the pick is rebuilt
from them with the same settings. This script only cuts, slows or speeds (a fixed speed or a gliding one), layers,
fades, filters and levels them: nothing is synthesized. No bubbly or popping sources. The tools are sfx_r43g.py's
(L, mix) and sfx_r46k.py's levelling. Pending the user's review in round 47; the game keeps the old files until the
pick.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), an a and a b each:

  herald-song-r47-*          the Choir Herald's song with each ring of bullets (Levels 17, 19, 21): real voices
                             (a concert-hall choir, throat singing, a chamber choir) slowed, detuned and layered
  enemy-leviathan-cry-r47-*  the Leviathan's death cry (Level 03): a real humpback whale's moan (a 3D hydrophone
                             recording), slowed a little
  integrity-alarm-r47-*      an ally's or a protected object's integrity alarm at 50 % and 25 % (Levels 13, 17): a
                             real alarm (a submarine's submersion alarm, a mains klaxon hooter) cut into a two-tone
                             high-low call, twice
  dome-hit-r47-*             a Level 17 colony dome struck: a sharp hard glass hit with a bright crack, no thud
  launch-rail-r47-*          the catapult launch at every level start (4.9 s, the buffer stop at 3.6 s): a real
                             aircraft-carrier launch over a real afterburner, a rail car's hard slam or a coupler's clank (the stop), steam

Usage: python3 tools/concept/audio/sfx_r47l.py [subject-or-file ...]   e.g. dome-hit launch-rail-r47-b
       python3 tools/concept/audio/sfx_r47l.py --docs OUTDIR   also writes the CREDITS.md rows, the sfx README rows and
                                                               the prompts.md entries for the files made into OUTDIR
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
SCRIPT = "tools/concept/audio/sfx_r47l.py"
R = "r47"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    502946: ("Vocal_crescendo.wav", "cellokratzer", CC0, P + "502/502946_4882074-hq.ogg"),
    816640: ("Throat Singing in a Cave", "ItsThatRoboGuy", CC0, P + "816/816640_8175042-hq.ogg"),
    65195: ("chamber_choir_chord_o.wav", "uair01", BY3, P + "65/65195_717853-hq.ogg"),
    797762: ("humpback_whales_and_orcas_ambient_underwater_recording", "raiden04", BY4,
             P + "797/797762_17082821-hq.ogg"),
    480898: ("Submarine submersion alarm_mono_wave.wav", "Guialgarve", CC0, P + "480/480898_10296283-hq.ogg"),
    514981: ("Klaxon on axis Long.wav", "jameswrowles", CC0, P + "514/514981_3874107-hq.ogg"),
    418194: ("Hard Glass Impact", "deleted_user_3656686", CC0, P + "418/418194_3656686-hq.ogg"),
    406261: ("hit - glass - window 01.wav", "Anthousai", CC0, P + "406/406261_5923045-hq.ogg"),
    379008: ("Crack_glass.wav", "13FPanska_Cerny_Jan", CC0, P + "379/379008_7020756-hq.ogg"),
    658607: ("Tapping on window - 3 types.WAV", "bpwebster", CC0, P + "658/658607_969182-hq.ogg"),
    121366: ("glass_kappa_03.wav", "mugwood", BY4, P + "121/121366_2159663-hq.ogg"),
    162449: ("Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac", "qubodup", CC0,
             P + "162/162449_71257-hq.ogg"),
    104883: ("Afterburner sound.wav", "StoneyJ", CC0, P + "104/104883_1786799-hq.ogg"),
    750751: ("Rail Cars Slam Hard and Couple Together Air Released", "WeenyBeany", BY4, P + "750/750751_15119557-hq.ogg"),
    198605: ("Train car coupling sounds", "toam", BY3, P + "198/198605_2214556-hq.ogg"),
    457294: ("Air (or steam) pressure release", "brunoboselli", CC0, P + "457/457294_300738-hq.ogg"),
    803061: ("AIRHiss-Blue Snowball Microphone, CU_Steam Release, Acme Windmaster_Nicholas Judy_TDC",
             "designerschoice", BY4, P + "803/803061_6951162-hq.ogg"),
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


def two_tone(sound, start, hi, lo, blast, gap, cycles, **kw):
    """A two-tone call cut from one real alarm: `blast` s at speed `hi`, then at speed `lo`, `cycles` times."""
    out, at = [], 0.0
    for _ in range(cycles):
        for speed in (hi, lo):
            out.append(L(sound, start, blast, at=round(at, 3), speed=speed, **kw))
            at += blast + gap
    return out


# subject -> (level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like, {variant: (layers, what)}).
# The level is the loudest 100 ms of the 200 Hz-5 kHz band (sfx_r24.level), at most `cap` LUFS, like the r46 levels.
SOUNDS = {
    "herald-song": (-20.0, -4.0, -18.0,
                    "the Choir Herald's song (`Sfx.HERALD_SONG`), a one-shot with each 10-bullet ring the Herald fires "
                    "(every 4 s, 3.2 s on hard) while it lives, Levels 17, 19 and 21",
                    "the Vrell Choir's voice: eerie and inhuman, several voices in a slow drifting chord", {
        "a": ([L(502946, 3.3, 2.4, speed=0.7, fin=0.25, fout=0.7, hp=80.0),
               L(816640, 6.0, 2.4, gain=-7.0, speed=0.85, fin=0.3, fout=0.7, hp=80.0)],
              "a concert-hall choir's chord swelling in a crescendo while its voices slide upward, slowed to 0.7 "
              "(deeper and slower, the slide drags), with throat singing in a cave under it slowed to 0.85: its whistling overtones drift above "
              "the drone, which no human choir does"),
        "b": ([L(65195, 0.1, 2.4, speed=(0.66, 0.6), fin=0.3, fout=0.8, hp=80.0),
               L(65195, 0.25, 2.4, gain=-2.0, speed=(0.70, 0.62), fin=0.4, fout=0.8, hp=80.0)],
              "a chamber choir's held 'oh' chord played twice at once, slowed to about 0.65 and a semitone apart, "
              "both sagging slowly in pitch: two choirs out of tune with each other, beating and smeared, a chord "
              "that slides downward and never settles"),
    }),
    "enemy-leviathan-cry": (-24.0, -4.0, -20.5,
                            "the Leviathan's death cry (Level 03), under its large burst when it dies",
                            "a whale's cry: deep, long and mournful", {
        "a": ([L(797762, 198.8, 4.0, speed=0.85, fin=0.2, fout=0.9, hp=50.0, lp=3500.0)],
              "a humpback whale's moan from a clean 3D hydrophone recording: a cry that falls and settles on a long "
              "low tone with its overtones, slowed to 0.85 (a bigger animal)"),
        "b": ([L(797762, 235.6, 4.0, speed=0.8, fin=0.2, fout=0.9, hp=50.0, lp=3500.0)],
              "another humpback moan from the same recording: it starts high, sinks into a long deep groan and "
              "lifts a little at its end, slowed to 0.8 (deeper)"),
    }),
    "integrity-alarm": (-17.0, -4.0, -13.0,
                        "an ally or an object the player protects (Level 13's relay, Level 17's domes) dropping below "
                        "50 % and again below 25 % of its health: the cockpit's warning that it is in danger",
                        "a short urgent alarm, two tones, about 1 s, repeating", {
        "a": (two_tone(480898, 1.0, 1.0, 0.84, 0.45, 0.05, 2, fin=0.015, fout=0.04, hp=150.0),
              "a real submarine's submersion alarm, a harsh buzzing horn, cut into a high-low call (the low a third "
              "under, by slowing it to 0.84), twice: 'BAAP-baap, BAAP-baap', 2 s"),
        "b": (two_tone(514981, 1.5, 1.0, 0.8, 0.45, 0.05, 2, fin=0.015, fout=0.04, hp=150.0, lp=7000.0),
              "a real mains-driven klaxon hooter (the old 'ah-oo-gah' horn's steady blare), cut into a high-low call "
              "(the low slowed to 0.8), twice: rougher and more nasal than a, 2 s"),
    }),
    "dome-hit": (-19.5, -4.0, -22.0,
                 "an enemy shot hitting a Level 17 colony dome (`ALLY_HIT`), a greenhouse of thick glass",
                 "glass being hit: sharp, a hard knock with a bright crack, maybe a faint stress creak; not a thud", {
        "a": ([L(418194, 0.0, 0.7, fin=0.002, fout=0.35, hp=150.0),
               L(406261, 0.0, 0.9, gain=-7.0, fin=0.002, fout=0.5, hp=300.0),
               L(379008, 2.90, 0.45, gain=-10.0, at=0.1, fin=0.003, fout=0.25, hp=1500.0)],
              "a hard glass impact (a metal object on glass, with cracks forming), the ring of a window pane under "
              "it and a sharp glass crack 0.1 s later: bright and hard, 0.9 s"),
        "b": ([L(406261, 0.0, 1.0, fin=0.002, fout=0.5, hp=400.0),
               L(658607, 21.19, 0.8, gain=-4.0, fin=0.002, fout=0.4, hp=400.0),
               L(121366, 2.60, 0.5, gain=-9.0, at=0.08, fin=0.003, fout=0.3, hp=1000.0)],
              "a window struck hard: the knock and the pane ringing, a second hard tap's ring on it, and a stress "
              "crack in the glass right after (a cooling glass cracking): a glassy 'tock' with a ring, 1 s"),
    }),
    "launch-rail": (-20.8, -4.0, -13.0,
                    "every level start (4.9 s): the ship is catapulted off the carrier; the shuttle stops at 3.6 s",
                    "an aircraft-carrier catapult launch: the jet at full power, the run, the hard stop, steam", {
        "a": ([L(162449, 299.9, 4.9, gain=-5.0, fin=0.3, fout=1.6, hp=30.0),
               L(104883, 8.0, 2.4, gain=-12.0, fin=0.3, fout=0.9, hp=30.0),
               L(750751, 0.05, 1.3, at=3.55, fin=0.004, fout=0.8, hp=25.0),
               L(457294, 7.0, 1.3, gain=-9.0, at=3.6, fin=0.08, fout=0.9, hp=200.0)],
              "a real carrier launch (USS George H. W. Bush) with a real F-4's afterburner at full power under its start, then the catapult "
              "fires at 1.2 s and the jet's roar tears past; at 3.55 s a rail car's hard steel slam (the shuttle hitting "
              "its stop) and a steam release hissing as the roar fades"),
        "b": ([L(162449, 365.6, 4.9, gain=-4.0, fin=0.3, fout=1.6, hp=30.0),
               L(104883, 20.0, 1.9, gain=-11.0, fin=0.3, fout=0.7, hp=30.0),
               L(198605, 7.03, 1.0, at=3.55, fin=0.004, fout=0.6, hp=40.0),
               L(750751, 0.05, 1.2, gain=-6.0, at=3.56, fin=0.004, fout=0.8, hp=25.0, lp=800.0),
               L(803061, 0.3, 1.3, gain=-8.0, at=3.65, fin=0.05, fout=0.9, hp=200.0)],
              "another real launch from the same carrier: an afterburner at full power, the catapult firing at 1.2 s "
              "and the jet's roar rising more slowly; at 3.55 s a train coupler's sharp steel clank over a rail car "
              "slam's low body (a brighter, more metallic stop than a), then a close steam release"),
    }),
}


def sources_of(layers):
    return list(dict.fromkeys(layer["sound"] for layer in layers))


def build(subject, v):
    band, ceiling, cap, _, _, variants = SOUNDS[subject]
    layers, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(layers))
    raw = mix(layers)
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


def change_text(subject, layers, layer):
    band, ceiling, cap = SOUNDS[subject][:3]
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
    parts.append(f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} dBFS "
                 f"ceiling, at most {num(cap)} LUFS")
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(layers):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(layers)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        _, _, _, use, should, variants = SOUNDS[subject]
        layers, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        seen = set()
        for layer in layers:
            s = layer["sound"]
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            key = (s, layer["start"], layer["speed"], layer["at"]) if subject != "integrity-alarm" else s
            if key in seen:
                continue
            seen.add(key)
            change = change_text(subject, layers, layer)
            if subject == "integrity-alarm":
                change = change.replace("placed at", "").replace(
                    "cut ", "cut into four 0.45 s blasts alternating speed 1 and "
                    f"{num(layers[1]['speed'])} (a two-tone call, twice) from ", 1)
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | {change} |")
        x = decode(path)
        secs = x.shape[1] / SR
        size = f"{secs:.2f} s ({short_term_band_db(x):.0f} dB)".replace("(-", "(−")
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in sources_of(layers))
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(layers)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays at {use}; should sound like {should}. "
                    f"This one: {what}; {names}, {size} | proposed (round 47) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in sources_of(layers))
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in sources_of(layers))
        label = "Sources" if len(sources_of(layers)) > 1 else "Source"
        edit = (f"{secs:.2f} s, the loudest 100 ms band {short_term_band_db(x):.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        prompts.append(f"### {name} — {by}\n\nProposed (round 47), cut from the HQ previews by `{SCRIPT}`.\n"
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
