#!/usr/bin/env python3
"""Concept round 57 (M8 part G, step AUD-G): Level 27 "Hydrothermal Vents"'s sounds. The `europa-vents` bed; a heat
plume's tell (a low rising rumble) and eruption (a heavy roar); the bend (a short swish of water as a plume turns a
shot); the Depth Hunter's torpedo launch, its torpedo's thruster (a loop, the nearest) and its impact; the relay
beacon's hum (a loop, pulsing with its 4 s lamp) and its break; the hauler *Marlin*'s resin crack (the secret); the
Hunter's hull break (over its `medium` under-water burst); the hull-code scan's single ping (synthesized). The sfx are
cut from real recordings in the Freesound database (round 48's rules: heavy and big, muffled and pressured under
water, no bubbly, popping or squelching sounds, no low-pass on the sfx bus: each layer carries its own filters).
Reused, no row: the Glassfin shatter (round 52's b), the under-water explosion family (round 52's a: the Hunter's
`medium`, the beacon's and the Marlin's rungs), the Decoy Flares' launch (special-flares-r08-a).

Every sfx layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 57 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The sfx are only cut, slowed, glided, layered, smoothed, pulsed, faded,
filtered, looped and levelled; nothing in them is synthesized but the scan ping. The tools are sfx_r55f.py's (copied:
preview, steady, bed, build, the --docs rows), plus a `SMOOTH` source (the power line's rain crackle smoothed out
before any cut) and a `synth` kind (the scan ping).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  europa-vents-r57-a     a seamless 24 s LOOP: a waterfall's massive roar at the bottom of its gorge slowed to 0.45 and
                         muffled at 350 Hz; once a loop a steam locomotive's boiler blowdown far off (slowed, muffled)
                         swelling and fading, and a bunker door's low metal groan slowed to 0.4
  europa-vents-r57-b     a seamless 24 s LOOP: Niagara's roar slowed to 0.4 and muffled at 300 Hz, a river's rapids
                         slowed to 0.3 under it; once a loop a geyser's eruption far off (slowed, muffled) and the
                         bunker door's other groan slowed to 0.35
  plume-tell-r57-a       a deep-sea earthquake's rumble (a hydrophone at 900 m) gliding up (1.0 to 1.6) and swelling,
                         the waterfall's roar low under it rising (1.4 s)
  plume-tell-r57-b       the boiler blowdown's roar slowed and gliding up (0.3 to 0.55), the rapids rising under it
                         (1.4 s)
  plume-eruption-r57-a   Iceland's Geysir erupting (a 1981 tape) slowed to 0.7, the waterfall's roar under it and a
                         depth bomb's low boom, a soft onset (2.0 s)
  plume-eruption-r57-b   the boiler blowdown slowed to 0.6 over Niagara's roar slowed, a depth bomb's low boom (2.0 s)
  bend-swish-r57-a       water whooshing by (a phone recording close up) slowed to 0.8, muffled (0.4 s)
  bend-swish-r57-b       a hydrophone's under-water pass-by whoosh sped to 1.1, muffled (0.4 s)
  torpedo-launch-r57-a   an air cannon's blast (compressed air through a pipe, quarter speed) slowed to 0.8, a
                         submarine motor's whirr speeding up after it (0.9 s)
  torpedo-launch-r57-b   a tyre bead seater's air blast slowed to 0.6 and muffled, on a deep thump (0.8 s)
  torpedo-run-r57-a      a seamless 4 s LOOP: a river boat's propeller under water (hydrophones) sped to 1.3, muffled
  torpedo-run-r57-b      a seamless 4 s LOOP: a Coast Guard Zodiac's outboards at full throttle under water
                         (hydrophone) slowed to 0.8, muffled
  torpedo-impact-r57-a   a real under-water impact slowed to 0.75 over a heavy file cabinet's metal impact slowed to
                         0.6 (1.5 s)
  torpedo-impact-r57-b   a depth bomb's boom with a heavy steel door's slam slowed to 0.5 in it (1.6 s)
  beacon-hum-r57-a       a seamless 4 s LOOP: big power lines buzzing in light rain, the rain's crackle smoothed out,
                         slowed to 0.7 and muffled, swelling once a loop with the lamp's pulse
  beacon-hum-r57-b       a seamless 4 s LOOP: a transformer's hum in a large room slowed to 0.7, swelling the same
  beacon-break-r57-a     the power line hum dying (0.7 to 0.3) under a tree trunk's crack as it falls, slowed to 0.5
                         (2.2 s)
  beacon-break-r57-b     the transformer hum dying the same under a metal can's snap slowed to 0.35 and a heavy steel
                         door's clang slowed (2.0 s)
  marlin-crack-r57-a     a tree trunk cracking as it is felled, slowed to 0.45, on a deep thump (1.6 s)
  marlin-crack-r57-b     a river's ice cracking and breaking up slowed to 0.4, a metal groan slowed under it (1.6 s)
  hunter-break-r57-a     a pile of scrap metal dropped and crashing, slowed to 0.55, a submarine's motors running
                         down under it (1.8 s)
  hunter-break-r57-b     PC side panels dropped on each other ('car crash') slowed to 0.5, a heavy file cabinet's
                         metal impact slowed in it (1.8 s)
  scan-ping-r57-a        synthesized: one clean 1.5 kHz ping with a soft second partial and a long water reverb
  scan-ping-r57-b        synthesized: a soft rising scan sweep (600 to 1800 Hz) landing on a glassy two-tone ping

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the tell like the geyser's tell, the eruption like the geyser's
burst, the swish far under a shot (it plays often), the launch and the run like the Harpoon's, the impact a little
over the Harpoon's hit (it is a hit on the ship), the beacon hum like the lure's hum, the break like the pump's
break, the crack like the *Meridian*'s hatch, the hull break like the Siren's death, the ping well under the voice,
the bed like `europa-trench`.

Usage: python3 tools/concept/audio/sfx_r57g.py [subject-or-file ...]   e.g. plume-tell bend-swish-r57-b
       python3 tools/concept/audio/sfx_r57g.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                        rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets
and the loop seams.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import sfx_r43g  # noqa: E402
from import_sfx import band_rms_db  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r36 import seam_db  # noqa: E402
from sfx_r43g import Loop, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, reverb, sine, svf, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r57g.py"
R = "r57"
ROUND = "round 57"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    849934: ('High Force Waterfall, Bottom of Gorge, Massive Roar, Teesdale UK - Stereo', 'Hear_it_use_it', BY4,
             P + "849/849934_18634554-hq.ogg"),
    379905: ('NIAGARA WATERS ROARING.mp3', 'KevinT1001', CC0, P + "379/379905_571017-hq.ogg"),
    263878: ('Islanti, geysir purkautuu, turistit / Iceland, geyser erupting twice, tourists reacting, talking', 'YleArkisto', BY4,
             P + "263/263878_4415905-hq.ogg"),
    686055: ('d_s478_blowboiler.wav', 'relwin', CC0, P + "686/686055_1024769-hq.ogg"),
    369485: ('Earthquake  **audible only with appropriate speakers**', 'MBARI_MARS', CC0,
             P + "369/369485_6181234-hq.ogg"),
    496836: ('Metallic Groan', 'hinchinbrook', CC0, P + "496/496836_9847211-hq.ogg"),
    438835: ('G53-24-River or Rapids or Waterfall.wav', 'craigsmith', CC0, P + "438/438835_2524442-hq.ogg"),
    870742: ('Cannon Fires, Take 1, Quarter Speed', 'CHallSmith', CC0, P + "870/870742_18412227-hq.ogg"),
    863981: ('Tire Bead Seater Air Blast', 'j_soundeffects', BY4, P + "863/863981_14030247-hq.ogg"),
    680037: ('underwater ship propeller 3.45 PM 220920_0532.wav', 'klankbeeld', BY4, P + "680/680037_1648170-hq.ogg"),
    52205: ('coast_guard_zodiac_14may2007_05.mp3', 'aguasonic', BY4, P + "52/52205_610919-hq.ogg"),
    400793: ('Underwater Impact', 'deleted_user_7709760', BY3, P + "400/400793_7709760-hq.ogg"),
    751170: ('heavy_mechanical_impacts_different_pitches_from_4_drawer_metal_file_cabinet_03232024', 'Artninja', BY4,
             P + "751/751170_10996917-hq.ogg"),
    147873: ('DepthBomb_04.aif', 'ZeSoundResearchInc.', CC0, P + "147/147873_2101444-hq.ogg"),
    147876: ('DepthBomb_01.aif', 'ZeSoundResearchInc.', CC0, P + "147/147876_2101444-hq.ogg"),
    147880: ('DepthBomb_05.aif', 'ZeSoundResearchInc.', CC0, P + "147/147880_2101444-hq.ogg"),
    760860: ('Standing under large power lines during light rain buzzing', 'Whats_The_Frequency_Kennett', CC0,
             P + "760/760860_8254017-hq.ogg"),
    637912: ('tree cut crack and fall in forest3 movement not thud.flac', 'kyles', CC0, P + "637/637912_612689-hq.ogg"),
    518731: ('Cracking, snapping, and clanging (metal can)', 'Ashley12275', CC0, P + "518/518731_11141750-hq.ogg"),
    637606: ('ice cracking breaking up on river styrofoam squeaking awesome great detail1 stereo XY.flac', 'kyles', CC0,
             P + "637/637606_612689-hq.ogg"),
    637910: ('tree cut crack and fall in forest1 mono.flac', 'kyles', CC0, P + "637/637910_612689-hq.ogg"),
    587443: ('Scrap metal dropping / crashing', 'SamsterBirdies', CC0, P + "587/587443_5487341-hq.ogg"),
    592388: ('Car Crash (with Glass)', 'magnuswaker', CC0, P + "592/592388_11537497-hq.ogg"),
    407301: ('door metal heavy close both sides same time impact rattle1 on offmic +bg voices.wav', 'kyles', CC0,
             P + "407/407301_612689-hq.ogg"),
    814845: ('WATRMvmt-Samsung Galaxy Smartphone, CU_Whoosh By_Nicholas Judy_TDC', 'designerschoice', BY4,
             P + "814/814845_6951162-hq.ogg"),
    772895: ('WATRMvmt underwater passby slosh medium light whoosh', 'jerry.berumen', BY4,
             P + "772/772895_6753194-hq.ogg"),
    400997: ('Deep Thumps.wav', 'eardeer', CC0, P + "400/400997_2155630-hq.ogg"),
    438720: ('G45-18-Submarine Motor Stop.wav', 'craigsmith', CC0, P + "438/438720_2524442-hq.ogg"),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0, P + "453/453444_612689-hq.ogg"),
}

# A source used smoothed: sound -> (ms, seconds): its first `seconds` with each STFT bin's magnitude replaced by its
# running median over `ms` (the power line's rain crackle smoothed out; its hum kept) before anything is cut from it.
SMOOTH = {760860: (60.0, 10.0)}

_decoded = {}


def preview(sound):
    """The decoded HQ preview (smoothed if in SMOOTH), fetched from the CDN into the cache when missing."""
    if sound not in _decoded:
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        x = decode(path)
        if sound in SMOOTH:
            ms, seconds = SMOOTH[sound]
            x = steady(x[:, :int(seconds * SR)], ms)
        _decoded[sound] = x
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() and loop() read their layers through this
L = sfx_r43g.L


def steady(x, ms):
    """The signal with its fast spectral wobble smoothed out: each STFT bin's magnitude replaced by its running median
    over `ms` (crackle and short clicks are short; a hum's body is not), the phase kept (sfx_r55f.py's)."""
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
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


def bed(layers, length, xfade, extra=None):
    """sfx_r43g.loop() plus the extras: `events`, one-shot layers placed once in the loop at their `at` (wrapping
    round the seam), and `pulse` (period s, depth dB), a slow raised-cosine dip a whole number of times a loop."""
    st = loop(layers, length, xfade)
    extra = extra or {}
    n = st.shape[1]
    for ev in extra.get("events", []):
        x = cut(ev)
        a = int(ev["at"] * SR)
        idx = (a + np.arange(x.shape[1])) % n
        np.add.at(st[0], idx, x[0])
        np.add.at(st[1], idx, x[1])
    if "pulse" in extra:
        period, depth = extra["pulse"]
        cycles = round(length / period)
        t = np.arange(n) / n * cycles
        st = st * undb(-depth * (0.5 - 0.5 * np.cos(2 * np.pi * t)))
    return st


# ---- the scan ping (synthesized) ----

def ping(freq, n, decay, attack=0.004):
    t = np.arange(n) / SR
    return np.sin(2 * np.pi * freq * t) * np.minimum(1.0, t / attack) * np.exp(-t / decay)


def scan_ping(v):
    """a: one clean ping; b: a soft rising sweep landing on a two-tone ping. Mono into a stereo water reverb."""
    n = int(2.2 * SR)
    x = np.zeros(n)
    if v == "a":
        x += ping(1500.0, n, 0.22) + 0.25 * ping(3010.0, n, 0.12)
    else:
        k = int(0.28 * SR)
        u = np.arange(k) / k
        f = 600.0 * 3.0 ** u  # 600 -> 1800 Hz
        sweep = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.sin(np.pi * u) ** 2 * 0.35
        x[:k] += sweep
        a = int(0.26 * SR)
        x[a:] += (ping(1800.0, n - a, 0.18) + 0.6 * ping(2700.0, n - a, 0.14))[:n - a]
    x = np.concatenate([np.zeros(int(0.01 * SR)), svf(x, 4000.0, q=0.6)])[:n]  # 10 ms lead: a clean edge
    st = np.vstack([x, x])
    return reverb(st, seconds=1.6, mix=0.35, damping=0.6, seed=57)[:, :n]


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS;
#             "synth": None (scan_ping), levelled as a shot.
PLUME_USE = ("Level 27 from t=26 to the gate (sections 2-5): each heat plume (16 groups of 2-3 vents rippling 0.5 s "
             "apart, each vent every 4.5 s; easy every 6 s, hard every 4 s), one per vent")
HUNTER_USE = "Level 27 (and Level 28 as escorts): each Depth Hunter (10 at L27, in columns of 4 rising from below)"
BEACON_USE = "Level 27's Beacon Ridge (t≈94-124): each of the four relay beacons, the secondary's targets"
SOUNDS = {
    "europa-vents": ("loop", -42.0, -1.0, -29.0,
                     "Level 27 throughout: the ambience bed of the vent field (a caldera variant may follow for "
                     "Level 28's arena), in place of `europa-under`; under the music, and alone with Vorne's motif "
                     "while Varga reads the hull code (t=68-82)",
                     "a deep rumble of hot water, a distant black-smoker roar swelling now and then, a slow groan of "
                     "the chimneys; heavy, warm and close, louder and busier than `europa-black`; no bubbles, no "
                     "boiling, no hiss-pops, a seamless loop", {
        "a": (([Loop(849934, 20.0, speed=0.45, hp=20.0, lp=350.0)], 24.0, 1.0,
               {"events": [L(686055, 1.0, 8.0, gain=-9.0, at=5.0, speed=0.5, fin=3.0, fout=3.5, hp=40.0, lp=500.0),
                           L(496836, 4.4, 8.0, gain=-9.0, at=15.0, speed=0.4, fin=1.0, fout=2.5, hp=30.0,
                             lp=500.0)]}),
              "a waterfall's massive roar recorded at the bottom of its gorge (High Force, Teesdale) slowed to 0.45 "
              "and muffled at 350 Hz: a deep, steady rumble of water; once a loop a steam locomotive's boiler "
              "blowdown far off (slowed to 0.5, muffled at 500 Hz) swelling in and out over 8 s, the black smoker "
              "roaring, and a bunker door's low metal groan slowed to 0.4 (the chimneys): 24.0 s loop"),
        "b": (([Loop(379905, 2.0, speed=0.4, hp=20.0, lp=300.0),
                Loop(438835, 10.0, gain=-6.0, speed=0.3, hp=20.0, lp=200.0)], 24.0, 1.0,
               {"events": [L(263878, 5.0, 7.0, gain=-9.0, at=4.0, speed=0.5, fin=2.5, fout=3.0, hp=40.0, lp=500.0),
                           L(496836, 1.7, 5.0, gain=-10.0, at=14.0, speed=0.35, fin=1.0, fout=2.0, hp=30.0,
                             lp=500.0)]}),
              "Niagara's roar (a recording layered for its deep lows) slowed to 0.4 and muffled at 300 Hz, a river's "
              "rapids slowed to 0.3 under it: rounder and more surging than a; once a loop a geyser's eruption far "
              "off (Iceland's Geysir, slowed to 0.5, muffled at 500 Hz) and the bunker door's other groan slowed to "
              "0.35: 24.0 s loop"),
    }),
    "plume-tell": ("shot", -21.0, -4.0, -17.5,
                   f"{PLUME_USE}: the 1.4 s tell (easy 1.8, hard 1.0) as the mouth glows amber and silt lifts off "
                   "it, right before the eruption (plume-eruption)",
                   "a low rising rumble from below: the vent waking, deep and heavy, growing and rising in pitch "
                   "into the eruption; no crack, no hiss-pop, no bubbles", {
        "a": ([L(369485, 5.0, 1.4, speed=(1.0, 1.6), fin=0.05, fout=0.15, hp=60.0, lp=1200.0, swell=(-18.0, 1.0)),
               L(849934, 30.0, 1.4, gain=-6.0, speed=(0.3, 0.5), fin=0.1, fout=0.15, hp=60.0, lp=500.0,
                 swell=(-20.0, 1.2))],
              "a deep-sea earthquake's rumble (a hydrophone at 900 m) gliding up from 1.0 to 1.6 and swelling, the "
              "waterfall's roar low under it rising with it (0.3 to 0.5, muffled at 500 Hz): a rumble from the "
              "sea floor climbing into a roar, 1.4 s"),
        "b": ([L(686055, 1.0, 1.4, speed=(0.3, 0.55), fin=0.05, fout=0.15, hp=30.0, lp=600.0, swell=(-20.0, 1.2)),
               L(438835, 20.0, 1.4, gain=-6.0, speed=(0.3, 0.5), fin=0.1, fout=0.15, hp=25.0, lp=300.0,
                 swell=(-18.0, 1.0))],
              "the boiler blowdown's roar slowed and gliding up from 0.3 to 0.55, muffled at 600 Hz, the rapids "
              "rising under it: more of a roar building than a's rumble, 1.4 s"),
    }),
    "plume-eruption": ("shot", -19.0, -3.0, -13.0,
                       f"{PLUME_USE}: the 1.6 s eruption, the column of shimmering water standing over the mouth "
                       "(it burns and bends shots), right after its tell",
                       "a heavy roar of superheated water tearing upward: big and dense, a soft onset (no hiss-pop "
                       "at the start), the roar dying away at the end; muffled, no bubbles, no boiling", {
        "a": ([L(263878, 5.0, 2.0, speed=0.7, fin=0.06, fout=0.7, hp=40.0, lp=1800.0),
               L(849934, 50.0, 2.0, gain=-4.0, speed=0.6, fin=0.08, fout=0.7, hp=30.0, lp=900.0),
               L(147876, 0.0, 2.0, gain=-8.0, fin=0.03, fout=1.0, hp=25.0)],
              "Iceland's Geysir erupting (a 1981 tape recording, the column itself) slowed to 0.7 and muffled at "
              "1.8 kHz, the waterfall's roar under it slowed to 0.6 and a depth bomb's low boom: a deep, wet roar "
              "with weight under it, 2.0 s"),
        "b": ([L(686055, 9.0, 2.0, speed=0.6, fin=0.06, fout=0.7, hp=40.0, lp=1500.0),
               L(379905, 5.0, 2.0, gain=-3.0, speed=0.55, fin=0.08, fout=0.7, hp=30.0, lp=700.0),
               L(147880, 0.0, 2.0, gain=-9.0, fin=0.03, fout=1.0, hp=25.0)],
              "the boiler blowdown (a steam locomotive blowing its boiler down) slowed to 0.6 and muffled at "
              "1.5 kHz over Niagara's roar slowed to 0.55, a depth bomb's low boom: steadier and more steam-like "
              "than a, a jet of pressure, 2.0 s"),
    }),
    "bend-swish": ("shot", -27.0, -8.0, -24.0,
                   "Level 27 from t=26: each bullet a plume's erupting disc turns 25° (the player's shots, enemy "
                   "bullets, torpedoes; once a bullet), so very often under fire; at most a few a second (the game "
                   "throttles it)",
                   "a short, soft swish of water as the shot is turned: quick and light, well under the shots; "
                   "muffled, no splash, no bubbles", {
        "a": ([L(814845, 0.35, 0.4, speed=0.8, fin=0.03, fout=0.2, hp=150.0, lp=1500.0)],
              "water whooshing by (a phone recording close up) slowed to 0.8 and muffled at 1.5 kHz: a soft 'fsshh', "
              "0.4 s"),
        "b": ([L(772895, 12.35, 0.4, speed=1.1, fin=0.03, fout=0.2, hp=120.0, lp=1300.0)],
              "a hydrophone's under-water pass-by whoosh (water rushing past the microphone) sped to 1.1 and muffled "
              "at 1.3 kHz: rounder and more under-water than a, a 'whoom', 0.4 s"),
    }),
    "torpedo-launch": ("shot", -23.0, -5.0, -19.0,
                       f"{HUNTER_USE}: each torpedo it fires up the screen (every 3.5 s a Hunter; easy 4.5, hard "
                       "2.8; up to 8 torpedoes in flight)",
                       "a heavy pneumatic launch thump: a tube blowing a torpedo out, deep and short, the motor "
                       "catching after it; built, not grown; muffled, no bubbles", {
        "a": ([L(870742, 0.04, 0.9, speed=0.8, fin=0.006, fout=0.5, hp=30.0, lp=1200.0),
               L(438720, 4.0, 0.8, gain=-10.0, at=0.1, speed=(0.8, 1.3), fin=0.1, fout=0.4, hp=80.0, lp=1500.0)],
              "an air cannon's blast (compressed air through a pipe, recorded at quarter speed) slowed to 0.8 and "
              "muffled at 1.2 kHz, a submarine motor's whirr speeding up after it: a deep 'THOOMP' and a motor "
              "spinning up, 0.9 s"),
        "b": ([L(863981, 0.0, 0.8, speed=0.6, fin=0.008, fout=0.5, hp=40.0, lp=1000.0),
               L(400997, 3.05, 0.5, gain=-6.0, fin=0.003, fout=0.3, hp=40.0, lp=1200.0)],
              "a tyre bead seater's air blast (a compressed-air bazooka) slowed to 0.6 and muffled at 1 kHz, on a "
              "deep thump: a harder, airier 'WHUMP' than a, no motor, 0.8 s"),
    }),
    "torpedo-run": ("loop", -38.0, -1.0, -31.0,
                    f"{HUNTER_USE}: while a torpedo runs (one quiet loop for all of them, the nearest, louder as it "
                    "closes on the ship; it stops with the last)",
                    "a torpedo's thruster: a small, fast propeller and motor churning through the water, a steady "
                    "muffled whirr, menacing as it nears; no bubbles, a seamless loop", {
        "a": (([Loop(680037, 50.0, speed=1.3, hp=80.0, lp=1500.0)], 4.0, 0.5),
              "a river boat's propeller recorded under water with hydrophones (its rhythmic churn) sped to 1.3 and "
              "muffled at 1.5 kHz: a smaller, faster screw beating, 4.0 s loop"),
        "b": (([Loop(52205, 5.0, speed=0.8, hp=80.0, lp=1400.0)], 4.0, 0.5),
              "a Coast Guard Zodiac's outboards at full throttle heard under water (a hydrophone) slowed to 0.8 and "
              "muffled at 1.4 kHz: a smoother, whining motor drone than a, 4.0 s loop"),
    }),
    "torpedo-impact": ("shot", -19.0, -3.0, -15.0,
                       f"{HUNTER_USE}: a torpedo bursting on the ship (`heavy` 10), the Bulwark or Rook; a torpedo "
                       "shot down plays the under-water `tiny` burst instead",
                       "a heavy hit on the hull under water: a deep pressure thud with a hard metal clang in it, "
                       "short; bigger than a bullet hit; no splash, no bubbles", {
        "a": ([L(400793, 0.1, 1.5, speed=0.75, fin=0.004, fout=0.8, hp=30.0, lp=1500.0),
               L(751170, 12.75, 1.0, gain=-4.0, speed=0.6, fin=0.003, fout=0.5, hp=60.0, lp=2000.0)],
              "a real under-water impact slowed to 0.75 and muffled, over a heavy metal file cabinet's impact slowed "
              "to 0.6: a deep 'DOOM' with a steel clang in it, 1.5 s"),
        "b": ([L(147873, 0.0, 1.6, gain=-3.0, fin=0.004, fout=0.9, hp=70.0),
               L(407301, 5.05, 1.0, gain=-3.0, speed=0.5, fin=0.003, fout=0.5, hp=50.0, lp=1800.0)],
              "a depth bomb's boom (its sub cut below 70 Hz) with a heavy steel door's slam (both halves at once, "
              "off-mic) slowed to 0.5 in it: rounder and more boom than a, the clang further off, 1.6 s"),
    }),
    "beacon-hum": ("loop", -38.0, -1.0, -31.0,
                   f"{BEACON_USE}: while a beacon lives on the screen (one quiet loop for all of them, the nearest; "
                   "its lamp pulses red-gold every 4 s; it stops with the last or when the ridge scrolls off)",
                   "a low electrical hum, the relay alive: steady, swelling once with each 4 s pulse of the lamp, "
                   "low in the mix; human-built (not the Vrell songs), no crackle, a seamless loop", {
        "a": (([Loop(760860, 3.0, speed=0.7, hp=60.0, lp=1200.0)], 4.0, 0.5, {"pulse": (4.0, 5.0)}),
              "big power lines buzzing in light rain (a real recording under them), the rain's crackle smoothed out, "
              "slowed to 0.7 and muffled at 1.2 kHz, swelling once a loop (5 dB) with the lamp: a high-voltage "
              "buzz breathing every 4 s, 4.0 s loop"),
        "b": (([Loop(453444, 4.0, speed=0.7, hp=50.0, lp=1000.0)], 4.0, 0.5, {"pulse": (4.0, 5.0)}),
              "a transformer's hum in a large room slowed to 0.7 and muffled at 1 kHz, swelling the same: lower "
              "and rounder than a, a hum rather than a buzz, 4.0 s loop"),
    }),
    "beacon-break": ("shot", -19.0, -3.0, -15.0,
                     f"{BEACON_USE}: a beacon destroyed (60 HP; with its under-water burst): the lamp goes dark and "
                     "the mast breaks (the hum loop stops)",
                     "the relay dying: its hum dropping away, a heavy mast snapping and falling; muffled, about 2 s; "
                     "no sparks crackle, no pops, no bubbles", {
        "a": ([L(760860, 5.0, 1.2, gain=-6.0, speed=(0.7, 0.3), fin=0.03, fout=0.8, hp=50.0, lp=1200.0),
               L(637912, 2.6, 2.2, at=0.05, speed=0.5, fin=0.005, fout=0.8, hp=60.0, lp=2500.0)],
              "the power line hum (smoothed) dying from 0.7 to 0.3, under a tree trunk's crack as it is felled and "
              "starts to fall, slowed to 0.5 and muffled at 2.5 kHz: the hum sinking away under a long splintering "
              "crack, 2.2 s"),
        "b": ([L(453444, 4.0, 1.2, gain=-6.0, speed=(0.7, 0.3), fin=0.03, fout=0.8, hp=50.0, lp=1000.0),
               L(518731, 0.45, 2.0, speed=0.35, fin=0.005, fout=0.6, hp=50.0, lp=2000.0),
               L(407301, 11.45, 1.0, gain=-4.0, at=0.5, speed=0.5, fin=0.003, fout=0.5, hp=50.0, lp=1500.0)],
              "the transformer hum dying the same, under a metal can's snap and clang slowed to 0.35 (a mast "
              "snapping) and a heavy steel door's clang slowed to 0.5 as it lands: more metal than a, 2.0 s"),
    }),
    "marlin-crack": ("shot", -20.0, -4.0, -17.0,
                     "Level 27 at t≈40, once a run (the secret): the hauler *Marlin* destroyed (20 HP, 28 on hard), "
                     "the Vrell resin sealing her hold cracking open; her crate floats up",
                     "a thick skin of resin cracking open: a deep, splintering crack, heavy and dull, with the hull "
                     "under it; muffled, about 1.5 s; no glassy tinkle, no pops", {
        "a": ([L(637910, 4.45, 1.6, speed=0.45, fin=0.005, fout=0.7, hp=50.0, lp=2000.0),
               L(400997, 3.05, 0.6, gain=-6.0, fin=0.003, fout=0.3, hp=40.0, lp=1200.0)],
              "a tree trunk cracking as it is felled (the trunk's last crack) slowed to 0.45 and muffled at 2 kHz, on "
              "a deep thump: a deep, woody 'KRRRACK', 1.6 s"),
        "b": ([L(637606, 43.8, 1.6, speed=0.4, fin=0.01, fout=0.7, hp=60.0, lp=2200.0),
               L(496836, 4.6, 1.2, gain=-8.0, at=0.2, speed=0.5, fin=0.1, fout=0.6, hp=40.0, lp=800.0)],
              "a river's ice cracking and breaking up (a field recording, close) slowed to 0.4 and muffled at 2.2 kHz, "
              "the bunker door's metal groan slowed under it (the hull): a crackling, glassier break than a, 1.6 s"),
    }),
    "hunter-break": ("shot", -19.0, -3.0, -15.0,
                     f"{HUNTER_USE}: each one destroyed, over its `medium` under-water burst (the explosion family's "
                     "a): its hull breaking up, grey debris and an oil bloom; no wreck",
                     "a machine breaking up: metal crumpling and tearing, its motor dying; it must sound built, not "
                     "grown (no screech); muffled, about 2 s; no bubbles", {
        "a": ([L(587443, 0.0, 1.8, speed=0.55, fin=0.004, fout=0.8, hp=50.0, lp=2200.0),
               L(438720, 8.6, 1.8, gain=-10.0, speed=(0.9, 0.4), fin=0.02, fout=0.8, hp=40.0, lp=1500.0)],
              "a pile of scrap metal dropped and crashing on concrete, slowed to 0.55 and muffled at 2.2 kHz, a "
              "submarine's motors running down under it: a heavy crumpling crash and a dying whine, 1.8 s"),
        "b": ([L(592388, 0.0, 1.8, speed=0.5, fin=0.004, fout=0.8, hp=50.0, lp=2000.0),
               L(751170, 8.65, 1.2, gain=-5.0, speed=0.5, fin=0.003, fout=0.6, hp=50.0, lp=1500.0)],
              "PC side panels dropped on each other (a 'car crash' foley) slowed to 0.5 and muffled at 2 kHz, a "
              "heavy metal file cabinet's double impact slowed in it: a tighter, more panel-like crunch than a, no "
              "motor, 1.8 s"),
    }),
    "scan-ping": ("synth", -24.0, -6.0, -22.0,
                  "Level 27 at t≈68, once a run: Varga's hull-code scan of the wreck as it scrolls in (her line at "
                  "t=70; Vorne's motif alone over the ambience)",
                  "a single scanner ping: clean, clear and short with a soft water tail, an instrument, not the "
                  "Sonar Pulse's sonar ping; well under the voice and the motif (synthesized: a UI-like sound)", {
        "a": (None, "synthesized: one clean 1.5 kHz ping (4 ms attack, 0.22 s decay) with a soft second partial, "
                    "through a 1.6 s water reverb: a bright 'pinnng', 2.2 s with its tail"),
        "b": (None, "synthesized: a soft rising sweep (600 to 1800 Hz in 0.28 s) landing on a glassy two-tone ping "
                    "(1800 and 2700 Hz), through the same reverb: a 'whee-ping', more of a scan locking on, 2.2 s"),
    }),
}


def layers_of(kind, spec):
    if kind == "synth":
        return []
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
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = bed(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 50 ms) to where both channels are nearest zero: the encoder's
        # edges then make no step at the join
        k = int(np.argmin(np.abs(st[:, :int(0.05 * SR)]).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = scan_ping(v) if kind == "synth" else mix(spec)
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
    one_shot = "length" in layer
    if not one_shot:
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
    if layer["sound"] in SMOOTH:
        parts.append(f"each STFT bin's magnitude replaced by its {num(SMOOTH[layer['sound']][0])} ms running median "
                     "(the rain's crackle smoothed out, the phase kept)")
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
            parts.append(f"the loop pulsing every {num(extra['pulse'][0])} s ({num(extra['pulse'][1])} dB deep)")
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
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays at {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                    f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays at {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
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
