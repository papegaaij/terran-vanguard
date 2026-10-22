#!/usr/bin/env python3
"""Concept round 51 (M8 part B, step AUD-B): Level 22's sounds. The ice geyser's tell and burst, the Drill Rig's
collar (a seamless loop while a rig is on the screen), its ring of ice shards and its core's death, and the two
Europa surface beds, `europa-ice` (Level 22 throughout, Level 23's shaft mouth, Level 28 after the breakout) and
`conamara-station` (Level 22's section 3: the ice bed with the station's ventilation and transformer hum), all cut
from real recordings in the Freesound database (round 48's rules: heavy and big, no bubbly, popping or squelching
sounds, no low-pass on the sfx bus).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 51 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. This script only cuts, slows, layers, swells, fades, filters and levels
them; nothing is synthesized. The tools are sfx_r50a.py's (preview, build, the --docs rows; sfx_r43g.py's seamless
loop). Pending the user's review in round 51; the game plays the a variants until the picks (step B4).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  geyser-tell-r51-a       a frozen lake's ice cracking, the cracks gathering, over a rising pressure hiss (1.2 s)
  geyser-tell-r51-b       ice breaking under a boot slowed down, a safety valve's hiss rising, a deep crack at the end
  geyser-burst-r51-a      a steam safety valve blowing over a sea blowhole's spout and a deep ice boom (1.5 s)
  geyser-burst-r51-b      a steam blast over a blowhole's surge and a deep earth rumble (1.5 s)
  rig-collar-r51-a        a seamless 4 s LOOP: an oil drill rig's whine over a deep digging rumble
  rig-collar-r51-b        a seamless 4 s LOOP: a deep boring drone with a big turbine's fan blades turning in it
  rig-ring-r51-a          a hammer smashing ice (the shards flying) on a compressed-air hose's burst
  rig-ring-r51-b          a large ice block chipped (crack and shards) on a steam piston's blast
  rig-death-r51-a         a heavy metal vehicle crash on a deep ice boom, then a metal groan and a steam release
  rig-death-r51-b         a big structure's collision heard from inside, ice breaking up, a steam release
  europa-ice-r51-a        a seamless 16 s LOOP: a glacial wind over a frozen lake's deep booms, an octave down
  europa-ice-r51-b        a seamless 16 s LOOP: a cold arctic wind over ice creaks and groans (hydrophones), slowed
  conamara-station-r51-a  a seamless 16 s LOOP: europa-ice a with an underground ventilation hum and a transformer
  conamara-station-r51-b  a seamless 16 s LOOP: europa-ice b with a large turbine fan and a big machine running

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap): the tell and the burst like Level 19's furnace vents (vent-tell-r43-a, vent-burst-r43-a), the ring like
the Dust Devil's spiral volley (devil-spiral-r46-a), the core's death a few dB under the `large` explosion it plays
with (explosion-large-r03-a); the collar loop by its band RMS like Level 19's belt loop (belt-rumble-r42-a,
-34 dB), the beds like `mars-dust` and `mars-colony` (-36 dB band RMS, at most -28 LUFS). Each layer is
peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r51b.py [subject-or-file ...]   e.g. rig-collar geyser-tell-r51-b
       python3 tools/concept/audio/sfx_r51b.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                        rows and the prompts.md entries for the files
Deterministic (no randomness, bit-exact ffmpeg flags).
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
from sfx_r43g import L, Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r51b.py"
R = "r51"
ROUND = "round 51"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    737896: ("Ice - Lake fractures", "Vrymaa", CC0, P + "737/737896_13973196-hq.ogg"),
    457294: ("Air (or steam) pressure release", "brunoboselli", CC0, P + "457/457294_300738-hq.ogg"),
    440858: ("BreakingIce.wav", "iainmccurdy", BY4, P + "440/440858_3655844-hq.ogg"),
    544239: ("Safety relief valve.wav", "tosha73", BY4, P + "544/544239_10725617-hq.ogg"),
    493558: ("steam_safety_valve.wav", "Engineer_815", CC0, P + "493/493558_10345385-hq.ogg"),
    222641: ("Spouting Horn2 Kauai.wav", "tec_studio", CC0, P + "222/222641_1431924-hq.ogg"),
    676005: ("S34-24 Steam or fire extinguisher blasts.wav", "craigsmith", CC0, P + "676/676005_2524442-hq.ogg"),
    824087: ("Sea blowhole, breathe. Bufador de mar, respira.", "SonoRec", CC0, P + "824/824087_2467044-hq.ogg"),
    82722: ("Digging.wav", "prozaciswack", CC0, P + "82/82722_122637-hq.ogg"),
    407454: ("Fracking.ND.DrillRig.Whine.mp3", "fkunze", CC0, P + "407/407454_3793224-hq.ogg"),
    130438: ("Drilling to the earth's core.wav", "reacthor", CC0, P + "130/130438_2092908-hq.ogg"),
    453476: ("motor industrial turbine large fan blades spin sorta.flac", "kyles", CC0,
             P + "453/453476_612689-hq.ogg"),
    608685: ("Wham n Shatter", "eben-frostey", CC0, P + "608/608685_8905624-hq.ogg"),
    25578: ("20061111AirHoseDisconnect1.wav", "daveincamas", BY4, P + "25/25578_111413-hq.ogg"),
    426205: ("Chipping large Ice Block 1.wav", "kev_durr", BY4, P + "426/426205_5415774-hq.ogg"),
    90143: ("steam_burst.wav", "pengo_au", CC0, P + "90/90143_793892-hq.ogg"),
    487143: ("R29-28-Large Metal Vehicle Crashes.wav", "craigsmith", CC0, P + "487/487143_2524442-hq.ogg"),
    390188: ("Metal Groan_Steam release.wav", "Cell31_Sound_Productions", BY4, P + "390/390188_3571756-hq.ogg"),
    653294: ("Big Structure Collision Heard from Inside", "Osvoldon", CC0, P + "653/653294_9808878-hq.ogg"),
    432086: ("Glacial Wind.wav", "Uzbazur", BY4, P + "432/432086_2812020-hq.ogg"),
    117136: ("Wind__Artic__Cold.wav", "cobratronik", CC0, P + "117/117136_732072-hq.ogg"),
    112628: ("Ice 3.wav", "Benboncan", BY4, P + "112/112628_634166-hq.ogg"),
    671456: ("Arena Underground Ventilation Hum and Rumble", "jamesdrake89", CC0, P + "671/671456_4980667-hq.ogg"),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0, P + "453/453444_612689-hq.ogg"),
    709258: ("Large machine running and powering down", "ryks88", BY4, P + "709/709258_8516444-hq.ogg"),
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


def steady(x, ms):
    """The layer with its fast spectral wobble smoothed out: each STFT bin's magnitude replaced by its running median
    over `ms` (a phaser's sweeping notches and gurgle are short; the launch's body is not), the phase kept."""
    nfft, hop = 2048, 256
    k = max(3, int(ms / 1000 * SR / hop) | 1)
    win = np.hanning(nfft)
    out = np.zeros_like(x)
    for c in range(x.shape[0]):
        pad = np.concatenate([np.zeros(nfft), x[c], np.zeros(nfft)])
        frames = np.array([pad[i:i + nfft] * win for i in range(0, len(pad) - nfft, hop)])
        spec = np.fft.rfft(frames, axis=1)
        mag = np.abs(spec)
        h = k // 2
        padded = np.pad(mag, ((h, h), (0, 0)), mode="edge")
        med = np.median(np.stack([padded[i:i + len(mag)] for i in range(k)]), axis=0)
        y = np.fft.irfft(med * np.exp(1j * np.angle(spec)), nfft, axis=1) * win
        acc = np.zeros(len(pad))
        norm = np.zeros(len(pad))
        for j, i in enumerate(range(0, len(pad) - nfft, hop)):
            acc[i:i + nfft] += y[j]
            norm[i:i + nfft] += win ** 2
        out[c] = (acc / np.maximum(norm, 1e-6))[nfft:nfft + x.shape[1]]
    return out


def mix(layers):
    parts = []
    for layer in layers:
        x = cut(layer)
        if layer.get("steady"):
            x = steady(x, layer["steady"])
            x = x / (np.max(np.abs(x)) + 1e-12) * undb(-1.0 + layer["gain"])
        parts.append((x, int(layer["at"] * SR)))
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


ICE_A = [Loop(432086, 2.0, hp=100.0), Loop(737896, 7.0, gain=-7.0, speed=0.5, hp=35.0, lp=900.0)]
ICE_B = [Loop(117136, 5.0, hp=30.0), Loop(112628, 4.0, gain=-6.0, speed=0.6, hp=25.0, lp=1200.0)]
# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
SOUNDS = {
    "geyser-tell": ("shot", -20.3, -4.0, -17.5,
                    "every Level 22 ice geyser's 1.2 s tell, as its crack darkens and glows lime-white before the "
                    "steam column (8 rows of 3 cracks, a row rippling 0.4 s a crack, every 4 s; every 3.2 s in "
                    "section 3), one per crack, so often",
                    "a deep ice crack running and splitting under the ship as the pressure builds, with a hiss "
                    "rising under it: heavy, 1.2 s, rising into the burst, no pop", {
        "a": ([L(737896, 10.0, 1.2, speed=0.85, fin=0.01, fout=0.12, hp=60.0, swell=(-10.0, 1.0)),
               L(457294, 6.6, 1.2, gain=-10.0, fin=0.2, fout=0.1, hp=400.0, swell=(-12.0, 1.0))],
              "a frozen lake's ice cracking (a field recording on a real lake) slowed to 0.85, quiet at first, the "
              "cracks gathering and growing heavier towards the end, over an air-pressure release's hiss rising "
              "with it: a deep, running, glassy crack building to the burst, 1.2 s"),
        "b": ([L(440858, 8.0, 1.2, speed=0.7, fin=0.01, fout=0.15, hp=80.0, lp=5000.0, swell=(-14.0, 1.2)),
               L(544239, 0.0, 1.2, gain=-12.0, fin=0.3, fout=0.1, hp=500.0, lp=6000.0, swell=(-14.0, 1.0)),
               L(737896, 9.1, 0.35, gain=-4.0, at=0.85, speed=0.8, fin=0.003, fout=0.2, hp=60.0, lp=3000.0)],
              "ice breaking (a slab cracking piece by piece) slowed to 0.7 and swelling, a steam safety valve's "
              "hiss rising under it, and at 0.85 s one deep lake-ice crack slowed to 0.8 as the crack gives: "
              "drier and more splintering than a, with a hard last crack, 1.2 s"),
    }),
    "geyser-burst": ("shot", -19.3, -4.0, -13.0,
                     "every Level 22 ice geyser's burst, as its 40 px steam column stands for 0.8 s (one per crack, "
                     "right after its tell)",
                     "a roaring column of steam and water slush tearing upward out of the ice: heavy, a soft "
                     "onset (no hiss-pop at the start), 1.5 s with the roar dying away", {
        "a": ([L(493558, 1.35, 1.5, fin=0.025, fout=0.55, hp=60.0),
               L(222641, 0.35, 1.5, gain=-3.0, fin=0.03, fout=0.6, hp=40.0),
               L(737896, 23.2, 1.2, gain=-5.0, speed=0.8, fin=0.01, fout=0.6, hp=30.0, lp=700.0)],
              "a boiler's steam safety valve blowing (a loud roaring hiss) over a sea blowhole's spout of water "
              "(Spouting Horn, Kauai: air and spray roaring up through rock) and under both a lake-ice boom "
              "slowed to 0.8, muffled: a big wet steam roar with a deep thump in it, 1.5 s"),
        "b": ([L(676005, 21.0, 1.5, fin=0.025, fout=0.6, hp=60.0),
               L(824087, 14.2, 1.5, gain=-2.0, fin=0.04, fout=0.6, hp=40.0),
               L(82722, 0.5, 1.5, gain=-9.0, fin=0.05, fout=0.6, hp=30.0, lp=400.0)],
              "a steam (or fire extinguisher) blast, a long steady roar, over a sea blowhole breathing out (a "
              "surging rush of air and water) and a deep earth rumble under it: more air and surge than a, "
              "less steam hiss, 1.5 s"),
    }),
    "rig-collar": ("loop", -34.0, -1.0, -27.0,
                   "while a Drill Rig's collar bores on the screen (Level 22, four rigs; one quiet loop for all of "
                   "them, as Level 19's belt loop, stopping with the last rig's core)",
                   "a low heavy drill whine with a grinding bite: a big machine boring into ice, steady, low in "
                   "the mix under the fire, no clicks, a seamless loop", {
        "a": (([Loop(407454, 2.0, hp=60.0), Loop(82722, 1.0, gain=-5.0, hp=30.0, lp=2500.0)], 4.0, 0.5),
              "a real oil drill rig's whine (a fracking rig in North Dakota: a steady droning whine with a grind "
              "in it) over a deep digging rumble of earth: an industrial drill hard at work, 4.0 s loop"),
        "b": (([Loop(130438, 3.0, hp=30.0), Loop(453476, 5.0, gain=-4.0, hp=60.0)], 4.0, 0.5),
              "a deep boring drone (a sound designer's drill sinking towards the earth's core: low, heavy, "
              "grinding) with a big industrial turbine's fan blades turning in it: lower and darker than a, "
              "more machine-hall than oil field, 4.0 s loop"),
    }),
    "rig-ring": ("shot", -22.0, -4.0, -18.5,
                 "each ring of 10 ice shards a Drill Rig's collar fires (every 3.5 s while its pumps live, slower "
                 "as they die; Level 22, up to two rigs at once)",
                 "the collar spitting a ring of ice shards: a hard icy crack-and-shatter with a burst of pressure "
                 "behind it, short and dry, not a gunshot, never bubbly", {
        "a": ([L(608685, 0.0, 0.7, fin=0.003, fout=0.35, hp=120.0),
               L(25578, 0.0, 0.6, gain=-6.0, fin=0.004, fout=0.3, hp=200.0)],
              "a hammer smashing a block of ice (the whack and the shards flying) on a compressed-air hose's "
              "burst: sharp, bright and icy, 0.7 s"),
        "b": ([L(426205, 0.0, 0.8, speed=0.8, fin=0.003, fout=0.35, hp=100.0),
               L(90143, 0.0, 0.7, gain=-5.0, fin=0.004, fout=0.35, hp=80.0, lp=3000.0)],
              "a large block of ice chipped (one hard crack and the chips scattering) slowed to 0.8, on a steam "
              "piston's blast muffled at 3 kHz: heavier and lower than a, more crack than shatter, 0.8 s"),
    }),
    "rig-death": ("shot", -16.0, -2.0, -15.0,
                  "a Drill Rig's core destroyed (Level 22, four rigs; with the `large` ground explosion): the "
                  "collar stops, the ice cracks and a steam plume rises",
                  "a big structure failing: metal crashing and tearing, ice cracking under it and a steam plume "
                  "rising, heavy, about 3 s, a few dB under the large explosion it plays with", {
        "a": ([L(487143, 7.9, 2.6, fin=0.005, fout=0.8, hp=40.0),
               L(737896, 23.2, 2.0, gain=-2.0, speed=0.75, fin=0.005, fout=0.9, hp=30.0, lp=900.0),
               L(390188, 5.5, 2.4, gain=-6.0, at=0.7, fin=0.05, fout=0.9, hp=80.0)],
              "a large metal vehicle crashing (a long tearing crash with debris, a vintage film recording) on a "
              "lake-ice boom slowed to 0.75 and muffled, then from 0.7 s a metal structure's groan ending in a "
              "steam release: the collar tearing apart and the plume escaping, 3.1 s"),
        "b": ([L(653294, 0.0, 3.0, fin=0.005, fout=1.0, hp=80.0),
               L(440858, 9.6, 2.0, gain=-2.0, at=0.15, speed=0.8, fin=0.01, fout=0.8, hp=80.0),
               L(457294, 0.0, 2.4, gain=-6.0, at=0.5, fin=0.1, fout=0.9, hp=300.0)],
              "a big structure's collision heard from inside (a vast deep boom and rumble), ice breaking up "
              "around it slowed to 0.8, and from 0.5 s a pressure release's hiss as the plume rises: deeper and "
              "less metallic than a, the ice in front, 3.0 s"),
    }),
    "europa-ice": ("loop", -36.0, -1.0, -28.0,
                   "Level 22 throughout (crossfading to `conamara-station` in section 3), Level 23's shaft mouth "
                   "and Level 28 after the breakout: the ambience bed of Europa's surface",
                   "a cold wind over open ice with deep ice groans and booms rolling under it far off; quiet, "
                   "under the music, no hiss-pops, no bubbles, a seamless loop", {
        "a": ((ICE_A, 16.0, 1.0),
              "a glacial wind (a steady, cold, hollow blow) over a frozen lake's fractures played an octave down "
              "and muffled, so the cracks become deep booms and groans rolling under it now and then: 16.0 s "
              "loop"),
        "b": ((ICE_B, 16.0, 1.0),
              "a cold arctic wind (a low moan with gusts; the same recording's moan is under `mars-dust`) over ice "
              "creaking and groaning (a hydrophone recording in lake ice) slowed to 0.6 and muffled: more creak "
              "and groan than boom, closer than a, 16.0 s loop"),
    }),
    "conamara-station": ("loop", -36.0, -1.0, -28.0,
                         "Level 22's section 3 (t=104 on), the run-in to Conamara Station: the ice bed with the "
                         "station's hum, crossfading in from `europa-ice` (one bed, no positional hum)",
                         "the ice bed with a big building's ventilation hum and a low transformer drone under the "
                         "wind: inhabited, mechanical, steady; quiet, a seamless loop", {
        "a": (([Loop(432086, 2.0, gain=-2.0, hp=100.0), Loop(737896, 7.0, gain=-9.0, speed=0.5, hp=35.0, lp=900.0),
                            Loop(671456, 2.0, gain=-6.0, hp=40.0), Loop(453444, 2.0, gain=-12.0, hp=40.0)],
               16.0, 1.0),
              "europa-ice a (the glacial wind and the deep booms, 2 dB quieter) with an underground arena's "
              "ventilation hum and rumble and an electric transformer's hum in a large room under it: the wind "
              "over a building that breathes, 16.0 s loop"),
        "b": (([Loop(117136, 5.0, gain=-2.0, hp=30.0), Loop(112628, 4.0, gain=-8.0, speed=0.6, hp=25.0, lp=1200.0),
                            Loop(453476, 10.0, gain=-8.0, hp=40.0), Loop(709258, 5.0, gain=-10.0, hp=40.0,
                                                                        lp=3000.0)], 16.0, 1.0),
              "europa-ice b (the arctic wind and the ice groans, 2 dB quieter) with a large industrial turbine "
              "fan turning and a big machine running under it, muffled: busier and more industrial than a, "
              "16.0 s loop"),
    }),
}


def layers_of(kind, spec):
    return spec[0] if kind == "loop" else spec


def sources_of(kind, spec):
    if kind == "synth":
        return []
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
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
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} dBFS "
            f"ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
    if kind == "loop":
        length, xfade = spec[1], spec[2]
        span = (length + xfade) * speed
        parts.append(f"cut {num(layer['start'])}–{layer['start'] + span:.2f} s")
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
    if layer.get("steady"):
        parts.append(f"each STFT bin's magnitude replaced by its {num(layer['steady'])} ms running median (the "
                     f"phaser's fast wobble smoothed out, the phase kept)")
    if kind == "loop":
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
    else:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
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
        for layer in ([] if kind == "synth" else layers_of(kind, spec)):
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
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | synthesized (no source) — `{subject}` {v}: "
                        f"plays at {use}; should sound like {should}. This one: {what}, {size} | proposed ({ROUND}) |")
        else:
            names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                        f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays at {use}; should sound like "
                        f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        if kind == "synth":
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (sfx_r37.py's "
                           f"bell and glass voices).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\n"
                           f"Edit: {edit.replace('-', '−')}.\n")
            continue
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or popping.\n"
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
        for v in spec[6]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
