#!/usr/bin/env python3
"""Concept round 55 (M8 part F, step AUD-F): Level 26 "Blackwater"'s sounds and the warm pad. The Glow Angler's lure
hum (a quiet loop, the nearest) and its lunge (a rushing surge into a heavy jaw snap); the Spiral Nautilus's crown hum
in the dark (a quiet loop, the nearest); the three torpedo trails rising at Rook (a long, distant rising hiss); Rook's
loss: the three hits on his lamp and the lamp's flicker and spin-down (his engine winding down into silence); the
headlight click (a heavy relay clunk); the Choir's static lead (the portrait's interference, 2 s before its line); the
`europa-black` bed. All cut from real recordings in the Freesound database (round 48's rules: heavy and big, muffled
and pressured under water, no bubbly, popping or squelching sounds, no low-pass on the sfx bus: each layer carries its
own filters). Reused, no row: the Angler's ring (the enemy small shot), the Driftjelly's pulse (round 46), the Siren's
ring (round 54) and `europa-trench` (round 54, the scarp).

Every sfx layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 55 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The sfx are only cut, slowed, glided, layered, gated, pulsed, faded,
filtered, looped and levelled; nothing in them is synthesized. The tools are sfx_r54e.py's (copied: preview, build,
the --docs rows), plus a loop's one-shot `events` (a far call placed once in a bed), a loop's slow `pulse` (the lure's
bead) and a layer's `gate` (the lamp's flicker: the recording switched on and off with 6 ms ramps).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  lure-hum-r55-a         a seamless 5 s LOOP: a neon sign's buzz slowed to 0.6 and muffled, pulsing slowly
                         (1.25 s, 5 dB deep) with the lure's teal bead
  lure-hum-r55-b         a seamless 5 s LOOP: a transformer's hum in a large room slowed to 0.8, a singing wine
                         glass's tone faint on top, pulsing the same (4 dB)
  angler-lunge-r55-a     a hydrophone's under-water pass-by surge slowed to 0.65, swelling into a dog's loud jaw snap
                         slowed to 0.5 on a depth bomb's thump (1.3 s)
  angler-lunge-r55-b     a sea wave's rush before it breaks, slowed to 0.5 and muffled, swelling into a book slammed
                         down slowed to 0.45 on a depth bomb's thump (1.3 s)
  crown-hum-r55-a        a seamless 4 s LOOP: a singing bowl rubbed round its rim, slowed to 0.5 (a deep violet hum)
  crown-hum-r55-b        a seamless 4 s LOOP: a singing wine glass slowed to 0.45 (lower, glassier, beating)
  torpedo-trails-r55-a   an airliner's approach far off, slowed and gliding up (0.6 to 0.95), swelling in (1.2 s)
  torpedo-trails-r55-b   a long steam hiss gliding up (0.55 to 0.85) over a submarine motor's whirr speeding up
                         (0.8 to 1.3), swelling in (1.2 s)
  rook-hit-r55-a         three depth bombs 0.13 s apart, each on a heavy steel door's slam slowed to 0.6 (2.6 s)
  rook-hit-r55-b         three real under-water impacts 0.13 s apart over a big metal side impact scraping, slowed
                         to 0.5 (the hull tearing) (2.6 s)
  rook-spin-r55-a        a faulty fluorescent tube's buzz gated into a flicker, then a submarine's motors winding
                         down (0.75 to 0.4) into silence (3.0 s)
  rook-spin-r55-b        a fluorescent fixture flickering on, slowed to 0.8 and muffled, then a machine's motor
                         winding down (0.8 to 0.5) into silence (3.0 s)
  headlight-click-r55-a  a 90 A motor contactor's thunk slowed to 0.6, on a deep thump (0.6 s)
  headlight-click-r55-b  a heavy rattling lever switch thrown, slowed to 0.55, a transformer's hum swelling after
                         it (0.8 s)
  choir-static-r55-a     AM radio interference slowed to 0.8, its crackle smoothed out, muffled, swelling over 2 s
                         (2.4 s)
  choir-static-r55-b     a radio's white-noise hiss between stations slowed to 0.6 and muffled, over a transformer
                         hum, swelling over 2 s (2.4 s)
  europa-black-r55-a     a seamless 24 s LOOP: a submarine's air conditioner (a steady low rumble) slowed to 0.5 and
                         muffled at 500 Hz; once a loop a deep-sea earthquake's rumble (a hydrophone at 900 m)
                         swelling and a blue whale's D call far off (sped up 2.2 times to be heard)
  europa-black-r55-b     a seamless 24 s LOOP: a giant air pump's drone slowed to 0.55, a submarine engine room's
                         undulating hum slowed to 0.45 under it; once a loop a blue whale's B call far off

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the lure and crown hums under the Nautilus's spin loop
(nautilus-spin-r54-a), the lunge a little over the Siren's death, the trails far and quiet, Rook's hits over the Siren's
death (the act's beat), the spin under the hits, the click like the hydro-kit refit, the static well under the voice,
the bed like `europa-trench`.

The warm pad (music; design/audio/music/concept/): the vent field's warm pad from t=168 in Level 26 (after the silence
at t=160), as a fourth stem of track 10 "Thera Deep" so the game can play it through the third lockstep stream as it
plays the dark stem (`alone`): the same render grid as music_r51b.py's track 10 (125 BPM, 4-bar intro, 88-bar loop,
LOOPSTART / LOOPLENGTH sample-equal to thera-deep-r51-a.ogg), the music tool's voices (music.py, music_r02.py), no
Freesound source. Pending the user's pick in round 55 (gap check F12).

  thera-deep-warm-r55-a  "the dark stem's pad opened": the dark stem's own pad (the same chords, v_pad) brighter (its
                         filter 3200 Hz, the dark stem's 1600) through the water held open at 4800 Hz (the dark stem's
                         moves 1300-4800), its F pedal drone quieter and warmer (900 Hz, the dark stem's 300); no pings
  thera-deep-warm-r55-b  "a synth pad": a new warm voice (two detuned saws and a triangle through a soft 1400 Hz
                         low-pass, 1.4 s attack, slow vibrato) over a sine sub, on a warm 8-bar progression in D-flat
                         (Dbmaj9, Ab/C, Bbm9, Gbmaj9, two bars each), a long reverb
  thera-deep-warm-join-r55-a / -b  listening aids: Level 26 from t=146 to 186: the dark stem (thinning 6 dB at 150,
                         out at 160), 8 s of silence, the warm pad creeping in from 168 over 10 s

Usage: python3 tools/concept/audio/sfx_r55f.py [subject-or-file ...]   e.g. lure-hum rook-hit-r55-b warm
       python3 tools/concept/audio/sfx_r55f.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                        rows and the prompts.md entries for the sfx
Deterministic (no randomness but the music voices' fixed seeds, bit-exact ffmpeg flags). Not listened to by Claude:
checked by level, peaks, onsets, the loop seams and the stems' alignment.
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
from synth import SR, adsr, db, decode, limiter, lufs, note_freq, saw, sine, svf, triangle, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
MUSIC = ROOT / "design" / "audio" / "music" / "concept"
SCRIPT = "tools/concept/audio/sfx_r55f.py"
R = "r55"
ROUND = "round 55"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    453491: ("neon sign buzz hum stereo close lowcut to taste4.flac", "kyles", CC0, P + "453/453491_612689-hq.ogg"),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0, P + "453/453444_612689-hq.ogg"),
    797583: ("Glass Harp - Singing Vibration", "Vrymaa", CC0, P + "797/797583_13973196-hq.ogg"),
    573805: ("Singing Bowl, long without reverb", "hollandm", CC0, P + "573/573805_5309408-hq.ogg"),
    772895: ("WATRMvmt underwater passby slosh medium light whoosh", "jerry.berumen", BY4,
             P + "772/772895_6753194-hq.ogg"),
    535616: ("Loud Chomping or Biting Sound with Chewing (Animal)", "OBXJohn", BY4, P + "535/535616_3719168-hq.ogg"),
    376799: ("Crashing Waves into Rocks 1", "amholma", CC0, P + "376/376799_6128004-hq.ogg"),
    732154: ("Book_Slammed_Down_01", "LewisEmmott5", CC0, P + "732/732154_15889796-hq.ogg"),
    147879: ("DepthBomb_06.aif", "ZeSoundResearchInc.", CC0, P + "147/147879_2101444-hq.ogg"),
    147880: ("DepthBomb_05.aif", "ZeSoundResearchInc.", CC0, P + "147/147880_2101444-hq.ogg"),
    147876: ("DepthBomb_01.aif", "ZeSoundResearchInc.", CC0, P + "147/147876_2101444-hq.ogg"),
    147875: ("DepthBomb_02.aif", "ZeSoundResearchInc.", CC0, P + "147/147875_2101444-hq.ogg"),
    147873: ("DepthBomb_04.aif", "ZeSoundResearchInc.", CC0, P + "147/147873_2101444-hq.ogg"),
    51491: ("airliner_approach_2.aif", "Heigh-hoo", CC0, P + "51/51491_21830-hq.ogg"),
    438641: ("G39-24-Steam Hiss.wav", "craigsmith", CC0, P + "438/438641_2524442-hq.ogg"),
    438720: ("G45-18-Submarine Motor Stop.wav", "craigsmith", CC0, P + "438/438720_2524442-hq.ogg"),
    407300: ("door metal heavy close both sides same time impact rattle2 +bg voices.wav", "kyles", CC0,
             P + "407/407300_612689-hq.ogg"),
    400793: ("Underwater Impact", "deleted_user_7709760", BY3, P + "400/400793_7709760-hq.ogg"),
    121623: ("big metal side impact 3 scraping.wav", "Halleck", BY4, P + "121/121623_6281-hq.ogg"),
    737060: ("Buzzing Faulty and Sparking Fluorescent Light", "iainmccurdy", BY4, P + "737/737060_3655844-hq.ogg"),
    577277: ("Fluorescent light fixture, buzz, clicks, noisy, lofi, 2011.wav", "TRP", CC0,
             P + "577/577277_97550-hq.ogg"),
    151288: ("Wind-down 1.aif", "timgormly", CC0, P + "151/151288_2578041-hq.ogg"),
    807384: ("Contactor/motor overload relay noises", "tt_runscript", CC0, P + "807/807384_5365416-hq.ogg"),
    400997: ("Deep Thumps.wav", "eardeer", CC0, P + "400/400997_2155630-hq.ogg"),
    623712: ("switching_a_rattling_lever_with_room_ambience.wav", "mediatheksuche", CC0,
             P + "623/623712_13793250-hq.ogg"),
    724710: ("Washy Radio Interference - Industrial SFX", "9voltplasma", CC0, P + "724/724710_14554953-hq.ogg"),
    691079: ("Radio interference when searching for a station 001", "Wastefield", CC0,
             P + "691/691079_15046244-hq.ogg"),
    438724: ("G45-22-Submarine Air Conditioner.wav", "craigsmith", CC0, P + "438/438724_2524442-hq.ogg"),
    369485: ("Earthquake  **audible only with appropriate speakers**", "MBARI_MARS", CC0,
             P + "369/369485_6181234-hq.ogg"),
    681896: ("blue whale D calls **audible only with appropriate speakers**", "MBARI_MARS", BY4,
             P + "681/681896_6181234-hq.ogg"),
    101278: ("air pump drone and hum.aif", "Tomlija", BY3, P + "101/101278_1163166-hq.ogg"),
    438726: ("G45-24-Submarine Interior Engine Room.wav", "craigsmith", CC0, P + "438/438726_2524442-hq.ogg"),
    343682: ("Blue Whale B call @ 5X", "MBARI_MARS", BY4, P + "343/343682_6181234-hq.ogg"),
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


def L(sound, start, length, steady=None, gate=None, **kw):
    """sfx_r43g.L plus `steady` (ms, the crackle smoothed out) and `gate` ([(on, off)] s, a flicker)."""
    layer = sfx_r43g.L(sound, start, length, **kw)
    layer.update(steady=steady, gate=gate)
    return layer


def steady(x, ms):
    """The layer with its fast spectral wobble smoothed out: each STFT bin's magnitude replaced by its running median
    over `ms` (crackle and short clicks are short; a hiss's body is not), the phase kept."""
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


RAMP = 0.006  # a gate's on and off ramps (raised cosine): long enough not to click


def gate_env(n, spans):
    """An envelope that is 1 inside each (on, off) span (seconds) and 0 outside, with RAMP-long raised-cosine ramps."""
    env = np.zeros(n)
    k = int(RAMP * SR)
    up = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, k))
    for on, off in spans:
        a, b = int(on * SR), min(n, int(off * SR))
        seg = np.ones(b - a)
        seg[:k] = up
        seg[-k:] = up[::-1]
        env[a:b] = np.maximum(env[a:b], seg)
    return env


def shot(layer):
    """One layer cut (sfx_r43g.cut), smoothed and gated if asked."""
    x = cut(layer)
    if layer.get("steady"):
        x = steady(x, layer["steady"])
        x = x / (np.max(np.abs(x)) + 1e-12) * undb(-1.0 + layer["gain"])
    if layer.get("gate"):
        x = x * gate_env(x.shape[1], layer["gate"])
    return x


def mix(layers):
    parts = [(shot(layer), int(layer["at"] * SR)) for layer in layers]
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
        x = shot(ev)
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


def flicker(*spans):
    return list(spans)


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS.
ANGLER_USE = "Level 26 on, each Glow Angler (Level 26: 78 of them; the dark: its teal lure is its only visible part)"
NAUTILUS_USE = "Level 26 on (the dark), each Spiral Nautilus, its violet crown the light it shows"
SOUNDS = {
    "lure-hum": ("loop", -38.0, -1.0, -31.0,
                 f"{ANGLER_USE}: while one is on the screen, its lure's pulsing teal bead (one quiet loop for all of "
                 "them, the nearest, louder as the ship comes close; it stops with the last)",
                 "a faint electric hum, the lure's glow made audible: soft, a little enticing, pulsing slowly with the "
                 "bead, low in the mix under the music; no crackle, no bubbles, a seamless loop", {
        "a": (([Loop(453491, 10.0, speed=0.6, hp=120.0, lp=1400.0)], 5.0, 0.5, {"pulse": (1.25, 5.0)}),
              "a neon sign's buzzing hum (recorded close) slowed to 0.6 and muffled at 1.4 kHz, pulsing slowly "
              "(every 1.25 s, 5 dB deep) with the bead: a low, soft electric buzz breathing in and out, 5.0 s loop"),
        "b": (([Loop(453444, 4.0, speed=0.8, hp=60.0, lp=1800.0),
                Loop(797583, 13.0, gain=-14.0, hp=600.0, lp=2500.0)], 5.0, 0.5, {"pulse": (1.25, 4.0)}),
              "a transformer's hum in a large room slowed to 0.8 and muffled at 1.8 kHz, with a singing wine "
              "glass's clear tone faint on top, pulsing the same (4 dB deep): rounder and sweeter than a, a hum "
              "with a glassy shine in it, 5.0 s loop"),
    }),
    "angler-lunge": ("shot", -19.0, -4.0, -16.0,
                     f"{ANGLER_USE}: each lunge (the ship within 130 px, 100 on easy, 160 on hard, or touching the "
                     "lure): from the start of its dive (up to 0.6 s at 220 px/s) to the jaws closing as it stops, "
                     "just before its ring of 8 (the ring plays the enemy small shot); every 5 s at most an Angler",
                     "a lunge out of the dark: a rushing surge of water building fast for half a second, then a "
                     "heavy jaw snap, dull and big (no click, no bite crunch), muffled under water; no bubbles, "
                     "no splash", {
        "a": ([L(772895, 12.36, 0.9, speed=0.65, fin=0.04, fout=0.3, hp=40.0, lp=1200.0, swell=(-14.0, 1.2)),
               L(535616, 63.38, 0.55, at=0.45, speed=0.5, fin=0.002, fout=0.3, hp=50.0, lp=2500.0),
               L(147879, 0.0, 0.85, gain=-7.0, at=0.45, fin=0.004, fout=0.5, hp=25.0)],
              "a hydrophone's under-water pass-by surge (a real recording of water rushing past the microphone) "
              "slowed to 0.65 and muffled, swelling for 0.45 s into a dog's loud jaw snap (catching a treat in the "
              "air, 'like a bear trap slamming shut') slowed to 0.5 and muffled at 2.5 kHz, on a depth bomb's "
              "thump: a 'whoosh-CHOMP', 1.3 s"),
        "b": ([L(376799, 18.6, 0.9, speed=0.5, fin=0.04, fout=0.3, hp=40.0, lp=900.0, swell=(-22.0, 1.5)),
               L(732154, 0.84, 0.5, at=0.45, speed=0.45, fin=0.002, fout=0.3, hp=40.0, lp=2000.0),
               L(147880, 0.0, 0.85, gain=-8.0, at=0.45, fin=0.004, fout=0.5, hp=25.0)],
              "a sea wave's rush as it rears against the rocks (before it breaks; the crash left out) slowed to "
              "0.5 and muffled at 900 Hz, swelling into a heavy book slammed down on a table slowed to 0.45 and "
              "muffled at 2 kHz, on a depth bomb's thump: deeper and flatter than a, a 'whooom-THUD' of a big "
              "mouth shutting, 1.3 s"),
    }),
    "crown-hum": ("loop", -38.0, -1.0, -31.0,
                  f"{NAUTILUS_USE}: while one is on the screen (one quiet loop for all of them, the nearest; with "
                  "its shell spin, nautilus-spin; it stops with the last)",
                  "the violet crown's glow made audible: a low, soft, resonant hum, sung rather than electric (the "
                  "lure's hum is electric), steady, under the spin's grind; no ring-out pings, a seamless loop", {
        "a": (([Loop(573805, 30.0, speed=0.5, hp=40.0, lp=1000.0)], 4.0, 0.5),
              "a Tibetan singing bowl rubbed round its rim (a long held tone, the bowl's strikes edited out by "
              "its recordist) slowed to 0.5 and muffled at 1 kHz: a deep, warm, steady 'ooom', 4.0 s loop"),
        "b": (([Loop(797583, 12.5, speed=0.45, hp=40.0, lp=1200.0)], 4.0, 0.5),
              "a singing wine glass (a finger round its wet rim, a long tonal note) slowed to 0.45 and muffled "
              "at 1.2 kHz: thinner and glassier than a, with a slow beating in the tone, 4.0 s loop"),
    }),
    "torpedo-trails": ("shot", -27.0, -8.0, -24.0,
                       "Level 26 at t=157 (the event ROOK_TRAILS): Rook's loss, three thin torpedo trails rising "
                       "from the bottom edge and converging on his lamp at t=157.5, where the hits take over "
                       "(rook-hit); with Rook only (not with the Bulwark, not after he ejected); the Depth Hunters "
                       "themselves are never seen",
                       "a long, distant, rising hiss: three torpedoes running up fast from far below, the hiss "
                       "growing and rising in pitch for half a second, then lost under the hits; far and muffled, "
                       "no whistle, no bubbles", {
        "a": ([L(51491, 6.0, 1.2, speed=(0.6, 0.95), fin=0.1, fout=0.55, hp=150.0, lp=2500.0, swell=(-18.0, 1.5))],
              "an airliner's jet approach heard from the ground (its roar swelling as it comes in) slowed and "
              "gliding up from 0.6 to 0.95, muffled at 2.5 kHz and without its low rumble: a far jet-like hiss "
              "rising fast, 1.2 s"),
        "b": ([L(438641, 4.0, 1.2, speed=(0.55, 0.85), fin=0.1, fout=0.55, hp=300.0, lp=3000.0, swell=(-20.0, 1.5)),
               L(438720, 4.0, 1.2, gain=-6.0, speed=(0.8, 1.3), fin=0.1, fout=0.55, hp=100.0, lp=2000.0,
                 swell=(-14.0, 1.2))],
              "a long steam hiss gliding up from 0.55 to 0.85, muffled at 3 kHz, over a submarine's motors "
              "speeding up (their whirr gliding 0.8 to 1.3): more machine than a, a torpedo's motor under the "
              "hiss, 1.2 s"),
    }),
    "rook-hit": ("shot", -17.0, -3.0, -14.0,
                 "Level 26 at t=157.5 (the event ROOK_LOST): the three torpedoes strike Rook's lamp, three "
                 "white-orange flashes, then the lamp spins down (rook-spin plays with it); with Rook only, once a "
                 "sortie (a retry replays it); harmless to the player",
                 "three heavy hits in quick succession on a small hull: deep under-water blasts with a hard metal "
                 "crunch in them, the last ringing off into a low rumble, about 2.5 s; the act's beat, bigger than a "
                 "kill but not a boss death; no splash, no bubbles", {
        "a": ([L(147876, 0.0, 2.6, gain=-4.0, fin=0.003, fout=1.2, hp=25.0),
               L(147875, 0.0, 2.47, at=0.13, gain=-6.0, fin=0.003, fout=1.2, hp=25.0),
               L(147873, 0.0, 2.33, at=0.27, gain=-5.0, fin=0.003, fout=1.2, hp=25.0),
               L(407300, 6.95, 1.0, speed=0.6, fin=0.002, fout=0.6, hp=60.0, lp=2000.0),
               L(407300, 31.95, 1.0, at=0.27, gain=-1.0, speed=0.6, fin=0.002, fout=0.6, hp=60.0, lp=2000.0)],
              "three depth bombs' sub booms 0.13 s apart, the first and last on a heavy steel door's double slam "
              "(both halves shut at once) slowed to 0.6 and muffled at 2 kHz: 'BOOM-boom-BOOM' with a hull's "
              "clang in it, 2.6 s"),
        "b": ([L(400793, 0.1, 1.6, speed=0.8, fin=0.003, fout=0.9, hp=30.0, lp=1500.0),
               L(400793, 0.1, 1.5, at=0.13, gain=-3.0, speed=0.7, fin=0.003, fout=0.9, hp=30.0, lp=1300.0),
               L(400793, 0.1, 2.3, at=0.27, gain=-1.0, speed=0.6, fin=0.003, fout=1.2, hp=30.0, lp=1200.0),
               L(121623, 0.08, 2.4, at=0.05, gain=-5.0, speed=0.5, fin=0.003, fout=1.0, hp=50.0, lp=1500.0),
               L(147880, 0.0, 2.6, gain=-6.0, fin=0.004, fout=1.2, hp=25.0)],
              "three real under-water impacts 0.13 s apart (each slower and deeper), over a big metal side impact "
              "scraping slowed to 0.5 (the hull torn open) and a depth bomb's boom: more tearing metal than a, "
              "2.6 s"),
    }),
    "rook-spin": ("shot", -22.0, -6.0, -19.0,
                  "Level 26 at t=157.5 (the event ROOK_LOST), with the hits (rook-hit): Rook's lamp flickers and "
                  "spins down off the bottom edge in 2.5 s, going dark; with Rook only",
                  "a dying lamp and engine: an electrical flicker (the lamp's buzz cutting in and out), then his "
                  "engine winding down into silence as he falls away, about 3 s, muffled; no pops, no bubbles", {
        "a": ([L(737060, 22.0, 1.3, fin=0.006, fout=0.2, hp=200.0, lp=2500.0,
                 gate=flicker((0.0, 0.16), (0.24, 0.31), (0.42, 0.66), (0.78, 0.83), (0.97, 1.08))),
               L(438720, 8.6, 3.0, gain=-8.0, speed=(0.75, 0.4), fin=0.02, fout=1.4, hp=30.0, lp=1500.0)],
              "a faulty fluorescent tube's buzz (iainmccurdy's recording) muffled at 2.5 kHz and switched on and off "
              "in five irregular bursts over the first second (the lamp flickering), over a submarine's motors "
              "running down to a stop (an engine-room recording) slowed from 0.75 to 0.4 and fading out: 3.0 s"),
        "b": ([L(577277, 0.4, 1.4, gain=-12.0, speed=0.8, fin=0.01, fout=0.3, hp=150.0, lp=1800.0),
               L(151288, 0.3, 3.0, speed=(0.8, 0.5), fin=0.02, fout=1.2, hp=40.0, lp=1800.0)],
              "a fluorescent fixture flickering on with its buzz (the tube's starter muffled at 1.8 kHz) slowed to "
              "0.8, over a machine's motor winding down after it is switched off, slowed from 0.8 to 0.5: lighter "
              "and higher than a, a smaller engine whining down, 3.0 s"),
    }),
    "headlight-click": ("shot", -22.0, -5.0, -18.0,
                        "Level 26: once as the ship's twin headlights come on at the level start (t=0), and as the "
                        "last pipeline lamps go out behind the ship (t=8-18, the dark coming down); not on the "
                        "Sonar Pulse",
                        "a heavy relay clunk: a big electrical switch closing, deep and solid, short; muffled, no "
                        "click-pop at the top", {
        "a": ([L(807384, 34.30, 0.6, speed=0.6, fin=0.002, fout=0.3, hp=50.0, lp=2500.0),
               L(400997, 3.05, 0.5, gain=-6.0, fin=0.003, fout=0.3, hp=40.0, lp=1200.0)],
              "a 90 A motor contactor (a Siemens industrial switch) closing with its loud thunk, slowed to 0.6 and "
              "muffled at 2.5 kHz, on a deep thump: a solid 'KA-CHUNK', 0.6 s"),
        "b": ([L(623712, 0.0, 0.5, speed=0.55, fin=0.002, fout=0.25, hp=50.0, lp=2500.0),
               L(453444, 8.0, 0.75, gain=-12.0, at=0.05, fin=0.15, fout=0.45, hp=50.0, lp=1500.0)],
              "a heavy rattling lever switch thrown, slowed to 0.55 and muffled at 2.5 kHz, with a transformer's "
              "hum swelling up after it for a moment (the lamps taking the power): a 'CLACK-hmmm', 0.8 s"),
    }),
    "choir-static": ("shot", -28.0, -10.0, -25.0,
                     "Level 26 at t=97: the Choir's static showing in the HUD portrait frame, the 2 s before its "
                     "whispered line (t=99, the music ducking 6 dB under the line); once a sortie. (Or no sound: "
                     "the static is a look; the round decides)",
                     "radio interference creeping into the comms: a soft washing hiss and murmur swelling for 2 s "
                     "and dropping away as the whisper starts; under the voices, no crackle bursts, no squeal", {
        "a": ([L(724710, 10.5, 2.4, speed=0.8, fin=0.3, fout=0.4, hp=200.0, lp=2500.0, swell=(-22.0, 1.3),
                 steady=40.0)],
              "an AM boombox's washy interference (a real recording) slowed to 0.8, its crackle smoothed out (a "
              "40 ms running median per frequency), muffled at 2.5 kHz and swelling over 2 s: a hollow, washing "
              "murmur, 2.4 s"),
        "b": ([L(691079, 2.0, 2.4, speed=0.6, fin=0.3, fout=0.4, hp=150.0, lp=1800.0, swell=(-22.0, 1.3)),
               L(453444, 12.0, 2.4, gain=-10.0, fin=0.5, fout=0.4, hp=50.0, lp=900.0, swell=(-16.0, 1.0))],
              "a radio's white-noise hiss between stations slowed to 0.6 and muffled at 1.8 kHz, over a "
              "transformer's hum, swelling over 2 s: a plainer, softer hiss than a with a low hum under it, 2.4 s"),
    }),
    "europa-black": ("loop", -44.0, -1.0, -30.0,
                     "Level 26 throughout but the scarp (where `europa-trench` takes over): the ambience bed of the "
                     "black water, in place of `europa-under`",
                     "total dark: a heavy pressure drone close round the ship, nothing to see and almost nothing to "
                     "hear, a very far call now and then (one a loop); quieter and emptier than `europa-under`, no "
                     "bubbles, no drips, a seamless loop", {
        "a": (([Loop(438724, 6.0, speed=0.5, hp=20.0, lp=500.0)], 24.0, 1.0,
               {"events": [L(369485, 4.8, 11.0, gain=-4.0, at=2.0, speed=0.85, fin=2.5, fout=4.0, hp=20.0,
                             lp=600.0),
                           L(681896, 139.0, 2.2, gain=-10.0, at=15.0, speed=2.2, fin=0.3, fout=0.8, hp=60.0,
                             lp=800.0)]}),
              "a submarine's air conditioner (a steady low rumble recorded inside the boat) slowed to 0.5 and "
              "muffled at 500 Hz; once a loop a deep-sea earthquake's rumble (a hydrophone at 900 m) swelling in "
              "and out, and a blue whale's D call far off (a hydrophone at 900 m, sped up 2.2 times to be heard, "
              "a low falling moan): 24.0 s loop"),
        "b": (([Loop(101278, 4.0, speed=0.55, hp=20.0, lp=400.0),
                Loop(438726, 20.0, gain=-8.0, speed=0.45, hp=20.0, lp=300.0)], 24.0, 1.0,
               {"events": [L(343682, 24.0, 4.0, gain=-10.0, at=11.0, speed=0.7, fin=0.6, fout=1.5, hp=50.0,
                             lp=700.0)]}),
              "a giant air pump's drone (inflating a marquee) slowed to 0.55 and muffled at 400 Hz, a submarine "
              "engine room's undulating hum slowed to 0.45 under it; once a loop a blue whale's B call far off (a "
              "deep-sea hydrophone, slowed to 0.7): rounder and more machine-like than a, 24.0 s loop"),
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


# ---- the warm pad (music): a fourth stem of track 10 on music_r51b.py's render grid ----

WARM_PLAN = ["Dbmaj9", "Dbmaj9", "Ab/C", "Ab/C", "Bbm9", "Bbm9", "Gbmaj9", "Gbmaj9"]
WARM_CH = {  # pad voicing, sub root
    "Dbmaj9": (["C#3", "F3", "G#3", "C4", "D#4"], "C#2"),
    "Ab/C": (["C3", "D#3", "G#3", "C4", "D#4"], "C2"),
    "Bbm9": (["A#2", "C#3", "F3", "C4"], "A#1"),
    "Gbmaj9": (["F#2", "A#2", "C#3", "F3", "G#3"], "F#1"),
}
WARM_LUFS = -20.0      # 2 LU under the dark stem (-18.1 LUFS): it creeps in under the aftermath's radio lines
WARM_CEILING = -2.0
PADS = {
    "a": "the dark stem's pad opened: its own pad (the same chords and voice) brighter (3200 Hz, the dark stem's "
         "1600) through the water held open at 4800 Hz, its F pedal drone quieter and warmer (900 Hz, the dark stem's "
         "300), no sonar pings",
    "b": "a synth pad: a new warm voice (two detuned saws and a triangle through a soft 1400 Hz low-pass, a 1.4 s "
         "attack, slow vibrato) over a sine sub on a warm 8-bar progression in D-flat (Dbmaj9, Ab/C, Bbm9, Gbmaj9, "
         "two bars each), a long reverb",
}


def v_warm(freq, dur):
    from music import ns
    n = ns(dur + 1.6)
    t = np.arange(n) / SR
    f = freq * (1 + 0.002 * np.sin(2 * np.pi * 0.3 * t + freq))
    x = 0.5 * (saw(f * 0.997, n) + saw(f * 1.003, n)) + 0.6 * triangle(f, n)
    x = svf(x, 1400.0, q=0.6)
    return x * adsr(n, 1.4, 0.6, 0.9, 1.5, gate=dur)


def v_low(freq, dur):
    from music import ns
    n = ns(dur + 1.0)
    return sine(freq, n) * adsr(n, 1.0, 0.5, 0.9, 1.0, gate=dur)


def warm_render(v):
    from music import hp, v_pad, widen
    from music_r02 import E_CH, LoopSong
    from synth import reverb
    import music_r51b as m
    s = LoopSong(125, 4, sum(len(c) for _, c in m.T_PLAN), xf_steps=m.XF_STEPS)
    n = s.tl.n
    if v == "a":
        for bar, name in enumerate(["Fm9", "Fm9", "Db", "Csus"]):
            s.chord("pad", v_pad, E_CH[name][0], bar, dur_steps=16, gain=0.45, bright=3200.0)
        for bar in range(0, 4, 2):
            s.chord("drone", v_pad, ["F1", "C2"], bar, dur_steps=32, gain=0.3, spread=0.3, bright=900.0)
        for base in s.sections():
            for _, j, name, bar in m.section_bars(m.T_PLAN, base):
                s.chord("pad", v_pad, E_CH[name][0], bar, dur_steps=16, gain=0.45, bright=3200.0)
                if j % 2 == 0:
                    s.chord("drone", v_pad, ["F1", "C2"], bar, dur_steps=32, gain=0.3, spread=0.3, bright=900.0)
        pad = widen(reverb(hp(s.tl.group("pad"), 120), seconds=4.0, mix=0.45, seed=81)[:, :n], 1.6) * 0.55
        drone = widen(reverb(hp(s.tl.group("drone"), 30), seconds=4.0, mix=0.4, seed=85)[:, :n], 1.2) * 0.5
        return s, m.water_filter(pad, np.full(n, 4800.0), 1.1) + drone
    total = s.intro + 2 * s.loop + 1
    for bar in range(0, total, 2):
        name = WARM_PLAN[(bar % 8)]
        notes, root = WARM_CH[name]
        s.chord("pad", v_warm, notes, bar, dur_steps=32, gain=0.5, spread=0.7)
        s.chord("sub", v_low, [root], bar, dur_steps=32, gain=0.2)
    pad = widen(reverb(hp(s.tl.group("pad"), 60), seconds=5.0, mix=0.5, seed=91)[:, :n], 1.4) * 0.6
    sub = hp(s.tl.group("sub"), 30) * 0.5
    return s, pad + sub


def track_slice(x, t0, t1, ls, ll):
    """Level time t0..t1 of a track file played from t=0 with its loop (LOOPSTART ls, LOOPLENGTH ll samples)."""
    i = np.arange(int(t0 * SR), int(t1 * SR))
    i = np.where(i < ls + ll, i, ls + (i - ls) % ll)
    return x[:, i]


def make_warm(v):
    import music_r11 as r11
    from music_r41 import fade_ends, tag_values
    s, raw = warm_render(v)
    audio, ls, ll = r11.prepare(s, raw)
    loop_part = audio[:, ls:ls + ll]
    audio = limiter(audio * undb(WARM_LUFS - lufs(loop_part)[0]), WARM_CEILING)
    audio[:, :int(0.005 * SR)] *= np.linspace(0, 1, int(0.005 * SR))
    path = write_ogg(MUSIC / f"thera-deep-warm-{R}-{v}.ogg", audio, max_peak_db=None,
                     tags={"LOOPSTART": ls, "LOOPLENGTH": ll})
    dark_path = MUSIC / "thera-deep-dark-r51-a.ogg"
    dark, d = decode(dark_path), decode(path)
    t = tag_values(path)
    td = tag_values(dark_path)
    aligned = d.shape == dark.shape and t["LOOPSTART"] == td["LOOPSTART"] and t["LOOPLENGTH"] == td["LOOPLENGTH"]
    from music_r02 import seam_ratio
    loud, tp = lufs(d)
    print(f"{path.name}: {d.shape[1] / SR:.2f}s  loop {ls}+{ll}  {loud:.1f} LUFS (dark stem {lufs(dark)[0]:.1f}), "
          f"loop {lufs(d[:, ls:ls + ll])[0]:.1f} LUFS, true peak {tp:.1f} dBTP, peak {db(np.abs(d).max()):.1f} dBFS, "
          f"seam {seam_ratio(d, ls, ll):.2f}, {path.stat().st_size / 1e6:.2f} MB, "
          f"{'aligned with the dark stem' if aligned else 'NOT ALIGNED'}", flush=True)
    # the listening aid: Level 26 t=146-186 as F5 would play it
    t0, t1 = 146.0, 186.0
    k = np.arange(int(t0 * SR), int(t1 * SR)) / SR
    g_dark = np.interp(k, [146, 150, 151, 158.5, 160], [1, 1, undb(-6), undb(-6), 0])
    g_warm = np.interp(k, [146, 168, 178, 186], [0, 0, 1, 1]) ** 2
    aid = track_slice(dark, t0, t1, ls, ll) * g_dark + track_slice(d, t0, t1, ls, ll) * g_warm
    out = write_ogg(MUSIC / f"thera-deep-warm-join-{R}-{v}.ogg", fade_ends(aid), max_peak_db=None)
    print(f"aid: {out.name} ({(t1 - t0):.0f} s: Level 26 t={t0:.0f}-{t1:.0f})", flush=True)
    return path


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
    if layer.get("steady"):
        parts.append(f"each STFT bin's magnitude replaced by its {num(layer['steady'])} ms running median (the "
                     f"crackle smoothed out, the phase kept)")
    if layer.get("gate"):
        parts.append(f"switched on and off in {len(layer['gate'])} bursts over its first "
                     f"{num(layer['gate'][-1][1])} s (a flicker, 6 ms ramps)")
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
    for v in PADS:
        if not argv or "warm" in argv or f"thera-deep-warm-{R}-{v}" in argv:
            make_warm(v)
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
