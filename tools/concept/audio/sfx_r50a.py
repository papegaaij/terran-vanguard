#!/usr/bin/env python3
"""Concept round 50 (M8 part A, step AUD-A): the Act 4 arsenal's sounds. The Harpoon Torpedoes' launch, run (a
seamless loop) and hit, the Depth Charges' roll-out clank and under-water burst and the Torpedo Pod's chosen launch
with its bubbles cut (round 48, Q12 = a: no low-pass on the sfx bus, heavy real under-water recordings instead, nothing
bubbly or popping), a heavier Sonar Pulse ping as an alternative b to the chosen special-sonar-r04-a, all cut from
real recordings in the Freesound database; and the special row's limit chime (a UI sound, synthesized).

Every recorded layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 50 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. This script only cuts, slows, layers, fades, filters and levels them (and,
for the Torpedo Pod's b, smooths the phaser's fast wobble out of the chosen launch's spectrum); nothing recorded is
synthesized. The tools are sfx_r49m.py's (preview, build, the --docs rows) plus sfx_r43g.py's seamless loop. Pending
the user's review in round 50; the game keeps its current files until the picks.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  shot-harpoon-r50-a        a pneumatic tube's air blast and a cannon's deep thump, the sub's motor whirr running off
  shot-harpoon-r50-b        a vintage submarine torpedo firing (hiss and thumps) over the dry torpedo launch, slowed
  harpoon-run-r50-a         a seamless 4 s LOOP: a submarine motor's muffled whirr over a deep submarine rumble
  harpoon-run-r50-b         a seamless 4 s LOOP: a real ship propeller's thrum through a hydrophone, the motor under it
  harpoon-hit-r50-a         an under-water deep impact on a depth bomb's sub-bass boom
  harpoon-hit-r50-b         a vintage torpedo explosion, muffled, on a depth bomb's sub-bass boom
  shot-depth-charge-r50-a   a steel barrel slammed down, an oil drum's hollow boom under it
  shot-depth-charge-r50-b   a heavy steel hatch slamming shut on a big robot's metal stomp
  depth-charge-burst-r50-a  a depth bomb's sub-bass boom with a low-passed real explosion's body
  depth-charge-burst-r50-b  a vintage torpedo explosion slowed and muffled, on a depth bomb's boom
  shot-torpedo-r50-a        the Torpedo Pod's chosen launch without its phaser: the dry launch, muffled the same way
  shot-torpedo-r50-b        the chosen launch itself with the phaser's fast wobble smoothed out of it
  special-sonar-r50-b       a real 625 Hz active sonar ping slowed to 0.85, over a heavy low sonar hit
  special-limit-r50-a       synthesized: two soft bells falling, E6 then A5 (the ready chime turned round)
  special-limit-r50-b       synthesized: one low soft glass fifth, C5-G5, muted

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
little over their loudness, so heavier sources do not stand out of the mix): the launches like shot-torpedo-r03-a, the
hit and the burst like explosion-water-r03-a (the torpedo impact's sound today), the clank like shot-mine-r03-a, the
ping like special-sonar-r04-a, the chime like cooldown-ready-r37-a; the run loop by its band RMS, 2 dB under a unit's
loop (the tank's tracks, -32 dB). Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r50a.py [subject-or-file ...]   e.g. harpoon-run shot-harpoon-r50-b
       python3 tools/concept/audio/sfx_r50a.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
from sfx_r37 import bell, glass  # noqa: E402
from sfx_r43g import L, Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r50a.py"
R = "r50"
ROUND = "round 50"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    35531: ("Torpedo launch.wav", "jobro", BY3, P + "35/35531_35187-hq.ogg"),
    35530: ("Torpedo launch underwater.wav", "jobro", BY3, P + "35/35530_35187-hq.ogg"),
    863981: ("Tire Bead Seater Air Blast", "j_soundeffects", BY4, P + "863/863981_14030247-hq.ogg"),
    870742: ("Cannon Fires, Take 1, Quarter Speed", "CHallSmith", CC0, P + "870/870742_18412227-hq.ogg"),
    438722: ("G45-20-Submarine Motor Underwater.wav", "craigsmith", CC0, P + "438/438722_2524442-hq.ogg"),
    438715: ("G45-13-Submarine Torpedo Fire.wav", "craigsmith", CC0, P + "438/438715_2524442-hq.ogg"),
    432514: ("Submarine Passing 1.wav", "Schmaddio", CC0, P + "432/432514_8489619-hq.ogg"),
    680037: ("underwater ship propeller 3.45 PM 220920_0532.wav", "klankbeeld", BY4, P + "680/680037_1648170-hq.ogg"),
    470084: ("Underwater Deep Impact", "Sheyvan", CC0, P + "470/470084_3248005-hq.ogg"),
    147876: ("DepthBomb_01.aif", "ZeSoundResearchInc.", CC0, P + "147/147876_2101444-hq.ogg"),
    438716: ("G45-14-Torpedo Explosion.wav", "craigsmith", CC0, P + "438/438716_2524442-hq.ogg"),
    147873: ("DepthBomb_04.aif", "ZeSoundResearchInc.", CC0, P + "147/147873_2101444-hq.ogg"),
    178838: ("barrelhits.wav", "j1987", CC0, P + "178/178838_367313-hq.ogg"),
    524817: ("Oil Drum Soft Impact.wav", "DeVern", CC0, P + "524/524817_2866779-hq.ogg"),
    828127: ("hatchclose.wav", "Mihacappy", CC0, P + "828/828127_10594370-hq.ogg"),
    815039: ("Big robot footstep 005", "AudioPapkin", CC0, P + "815/815039_8698658-hq.ogg"),
    147874: ("DepthBomb_03.aif", "ZeSoundResearchInc.", CC0, P + "147/147874_2101444-hq.ogg"),
    546957: ("NổLớnDướiNước1", "SieuAmThanh", CC0, P + "546/546957_7554526-hq.ogg"),
    147875: ("DepthBomb_02.aif", "ZeSoundResearchInc.", CC0, P + "147/147875_2101444-hq.ogg"),
    12677: ("Sonar_pings.ogg", "Peter_Gross", CC0, P + "12/12677_33429-hq.ogg"),
    414831: ("submarine sonar.mp3", "Link-Boy", CC0, P + "414/414831_2785350-hq.ogg"),
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


def S(**kw):
    """A synthesized layer (sfx_r37's bell or glass), placed at `at` s, `gain` dB."""
    return dict(synth=True, **kw)


def synth_mix(layers):
    out = np.zeros((2, max(int((p["at"] + p["length"]) * SR) for p in layers)))
    for p in layers:
        x = (bell if p["kind"] == "bell" else glass)(p)
        if p.get("lp"):
            x = sfx_r43g.filtered(x, None, p["lp"])
        a = int(p["at"] * SR)
        out[:, a:a + x.shape[1]] += x * undb(p.get("gain", 0.0))
    return out


TORPEDO = "shot-torpedo-r03-a's level"
# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS;
#             "synth": synthesized layers, levelled like a shot.
SOUNDS = {
    "shot-harpoon": ("shot", -22.4, -4.0, -18.0,
                     "every Harpoon Torpedoes shot (the front `anti-sub` weapon, Act 4 from L23's waterline; 0.8-1 a "
                     "second, two torpedoes at L5), mostly under water: the ship's own fire, so often",
                     "the `torpedo` family made heavier and without bubbles: a pneumatic tube's thump as the torpedo "
                     "is pushed out and its motor's whirr running off ahead; muffled and pressured, short, never "
                     "tiring at one a second", {
        "a": ([L(863981, 0.14, 0.6, fin=0.004, fout=0.35, hp=40.0, lp=2500.0),
               L(870742, 0.0, 0.7, gain=-5.0, fin=0.004, fout=0.4, hp=30.0, lp=1200.0),
               L(438722, 10.0, 0.7, gain=-9.0, at=0.12, speed=(0.9, 1.35), fin=0.08, fout=0.45, hp=120.0, lp=3000.0)],
              "a tyre-bead seater's compressed-air blast (a pneumatic tube's hard 'thoomp'), muffled at 2.5 kHz, on "
              "a cannon's deep thump played at quarter speed, and from 0.12 s a submarine motor's whirr rising in "
              "pitch as it runs off and fades: a punchy pneumatic launch with a motor leaving, 0.82 s"),
        "b": ([L(438715, 1.45, 0.9, fin=0.004, fout=0.45, hp=50.0, lp=3000.0),
               L(35531, 0.0, 0.9, gain=-6.0, speed=0.75, fin=0.004, fout=0.45, hp=30.0, lp=900.0)],
              "a vintage film recording of a submarine torpedo firing (a pressurised hiss with thumps and thunks "
              "in it), muffled at 3 kHz, over the Torpedo Pod's launch in its dry version slowed to 0.75 and "
              "muffled at 900 Hz: the pod's launch made bigger and lower, more hiss and mechanism than a, 0.9 s"),
    }),
    "harpoon-run": ("loop", -34.0, -1.0, -28.0,
                    "while a Harpoon Torpedo runs ahead of the ship (one loop for all of them, as long as any is on "
                    "the screen: about 2.5 s a torpedo under water, so nearly all the time while the weapon fires)",
                    "a torpedo's motor and propeller running away through the water: a steady, muffled, heavy whirr "
                    "or thrum, low in the mix under the launches and hits, no bubbles, no clicks, a seamless loop", {
        "a": (([Loop(438722, 10.0, hp=60.0, lp=4000.0), Loop(432514, 8.0, gain=-4.0, hp=30.0)], 4.0, 0.5),
              "a submarine's motor under water (a steady, high, muffled whirr, a vintage film recording) over a "
              "deep submarine rumble: a machine's whine running on, the most 'motor' of the two"),
        "b": (([Loop(680037, 5.0, hp=30.0, lp=1500.0), Loop(438722, 14.0, gain=-8.0, speed=0.8, hp=60.0, lp=3000.0)],
               4.0, 0.5),
              "a real ship's propeller recorded through a hydrophone (a low churning thrum, the blades' beat in "
              "it), muffled at 1.5 kHz, with the submarine motor slowed to 0.8 quietly under it: lower and rougher "
              "than a, the most 'propeller in water'"),
    }),
    "harpoon-hit": ("shot", -23.4, -4.0, -18.0,
                    "a Harpoon Torpedo striking its target (with the 20 px splash), under water and at the "
                    "surface of a `water: true` level: up to about one a second, more with two weapons' worth of "
                    "targets dying around it; the target's own death plays its explosion rung over it",
                    "a heavy thud on impact: a deep, pressured under-water blast with a muffled body and a short "
                    "low tail, no splash and no bubble rattle; short enough to repeat", {
        "a": ([L(470084, 0.0, 1.1, fin=0.002, fout=0.5, hp=30.0, lp=3000.0),
               L(147876, 0.0, 1.2, gain=-8.0, fin=0.002, fout=0.6, hp=25.0)],
              "an under-water deep impact (a massive dull hit with a churning body) on a depth bomb's sub-bass "
              "boom: a heavy 'thoom' with a low rolling tail, 1.2 s"),
        "b": ([L(438716, 3.8, 1.0, fin=0.003, fout=0.5, hp=40.0, lp=2000.0),
               L(147873, 0.0, 1.2, gain=-3.0, fin=0.002, fout=0.6, hp=25.0)],
              "a vintage film recording of a torpedo exploding (a big blast with rumble in it), muffled at 2 kHz, "
              "on a depth bomb's sub-bass boom: rougher and more 'explosion' than a, with more grit in it, 1.2 s"),
    }),
    "shot-depth-charge": ("shot", -26.9, -4.0, -26.0,
                          "every Depth Charges volley (the rear `anti-sub` weapon, Act 4 from L23's waterline): a "
                          "barrel rolling off the stern, 1 to 1.4 times a second (two barrels at L5); the ship's own "
                          "fire, so often",
                          "a heavy barrel clank as the charge rolls off its rail: steel on steel, weighty and short, "
                          "slightly muffled; the burst comes later (its own sound)", {
        "a": ([L(178838, 20.29, 0.45, speed=0.85, fin=0.002, fout=0.25, hp=50.0, lp=4000.0),
               L(524817, 0.0, 0.5, gain=-4.0, speed=0.75, fin=0.002, fout=0.3, hp=40.0)],
              "a piece of a steel barrel slammed down (a hard metal clank and a smaller rebound 0.1 s later) slowed to 0.85, over an oil drum's soft "
              "hit slowed to 0.75 (its hollow boom ringing under the clank): a heavy barrel thrown off, 0.5 s"),
        "b": ([L(828127, 0.93, 0.5, speed=0.9, fin=0.002, fout=0.3, hp=40.0, lp=3500.0),
               L(815039, 0.91, 0.5, gain=-5.0, fin=0.002, fout=0.3, hp=30.0, lp=3000.0)],
              "a heavy metal hatch slamming shut slowed to 0.9, on a big robot's metal footstep (a deep steel "
              "stomp): a darker, more 'latch and weight' clunk than a, less ring, 0.55 s"),
    }),
    "depth-charge-burst": ("shot", -23.4, -4.0, -18.0,
                           "a depth charge bursting on its fuse (0.7-0.9 s after the roll-out, behind the ship), "
                           "about once a second, two at L5; also on the `sub` layer above water",
                           "a heavy, low under-water thud: a pressure wave rather than a fireball, deep and muffled "
                           "with a short low tail; no splash, no bubbles", {
        "a": ([L(147874, 0.0, 1.5, fin=0.002, fout=0.7, hp=25.0),
               L(546957, 0.0, 1.3, gain=-3.0, fin=0.002, fout=0.6, hp=30.0)],
              "a depth bomb's sub-bass boom with a real explosion low-passed at 500 Hz (its body only) over it: a "
              "deep, round pressure 'whumpf', the softest and lowest, 1.5 s"),
        "b": ([L(438716, 10.135, 1.2, speed=0.75, fin=0.003, fout=0.6, hp=30.0, lp=1200.0),
               L(147875, 0.0, 1.5, gain=-6.0, fin=0.002, fout=0.7, hp=25.0)],
              "a vintage torpedo explosion slowed to 0.75 and muffled at 1.2 kHz, on a depth bomb's sub-bass "
              "boom: a deeper, rumbling 'thoom' with a rough edge, more weight than a, 1.5 s"),
    }),
    "shot-torpedo": ("shot", -22.4, -4.0, -19.0,
                     "every Torpedo Pod shot (wing mount, Levels 11 on and Act 4; Rook's torpedo gun too): its "
                     "chosen launch (shot-torpedo-r03-a) today",
                     "the chosen launch as it is, with its bubbly character cut (Q12): the source's 'water' is a "
                     "phaser sweep over a low-passed launch, so the cut keeps the muffled launch and drops the "
                     "phaser's gurgle", {
        "a": ([L(35531, 0.0, 1.0, fin=0.002, fout=0.5, hp=30.0, lp=550.0)],
              "the same launch without the phaser: its author's dry version ('Torpedo launch', from which the "
              "chosen one was made by filtering and a phaser) low-passed at 550 Hz like the chosen one: the same "
              "muffled rush and thump, smooth, no warble, 1.0 s"),
        "b": ([dict(L(35530, 0.0, 1.0, fin=0.002, fout=0.5, hp=30.0), steady=90)],
              "the chosen launch itself with the phaser's fast sweep and gurgle smoothed out of its spectrum (each "
              "frequency's level held steady over 90 ms): closest to the chosen file, a little duller, 1.0 s"),
    }),
    "special-sonar": ("shot", -10.3, -4.0, -15.0,
                      "the Sonar Pulse call (the special, from L22's unlock; in water only, at most 4 charges, 1.5 s "
                      "between pulses): with the teal ring sweeping the field in 0.5 s",
                      "a soft, heavy, low ping that fills the water, no bubbles, no pops; a = the chosen "
                      "special-sonar-r04-a (a clean single ping, kept), b only if clearly heavier", {
        "b": ([L(12677, 0.0, 2.4, speed=0.85, fin=0.003, fout=1.0, hp=200.0),
               L(414831, 0.0, 3.0, gain=-5.0, fin=0.004, fout=1.4, hp=30.0, lp=1500.0)],
              "a real 625 Hz active sonar ping (a long-range anti-submarine type) slowed to 0.85 (about 530 Hz, "
              "a lower and longer 'pinnng' than a's) over a heavy low sonar hit's thump and rumble: the same "
              "role as the chosen a, a deeper and weightier ping, 3.0 s"),
    }),
    "special-limit": ("synth", -22.0, -8.0, -20.0,
                      "the special row changing state at once as the ship crosses a waterline (greyed with `UNDER "
                      "ICE` or `NO WATER`, or usable again; the row flashes once): a few times in Levels 23 and 28. "
                      "A press on a greyed or empty special keeps the chosen denied buzz (special-denied-r08-a)",
                      "a short soft UI chime (synthesized, never bubbly), the cooldown-ready chime's family but "
                      "clearly 'changed', not 'ready'", {
        "a": ([S(kind="bell", at=0.0, length=0.45, f=1318.51, tau=0.18),
               S(kind="bell", at=0.08, length=0.45, f=880.0, tau=0.2)],
              "two soft bells falling, E6 then A5 80 ms later (the ready chime's two bells turned round): "
              "'switched off', light, 0.53 s"),
        "b": ([S(kind="glass", at=0.0, length=0.6, notes=(523.25, 783.99), tau=0.2, lp=2500.0)],
              "one soft glass fifth an octave lower, C5-G5, muted at 2.5 kHz: a darker, rounder 'tonk' than a, "
              "0.6 s"),
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
        raw = synth_mix(spec) if kind == "synth" else mix(spec)
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
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly.\n"
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
