#!/usr/bin/env python3
"""Concept round 52 (M8 part C, step AUD-C): Level 23's sounds and the under-water explosions. The waterline crossing,
the hydro-kit refit's clamps, the Glassfin's glass shatter, the Eel Swarm's head pulse shot and a segment's death, the
siphon pump's hum (a seamless loop while one lives) and its break, the wall scrape under water, the `europa-under` bed
(Level 23 from the waterline, Level 24) and the explosion ladder's under-water rungs `tiny` to `large` (every kill
below the waterline, Levels 23-28), all cut from real recordings in the Freesound database (round 48's rules: heavy
and big, muffled and pressured under water, no bubbly, popping or squelching sounds, no low-pass on the sfx bus: each
layer carries its own filters).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 52 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. This script only cuts, slows, layers, swells, fades, filters and levels
them; nothing is synthesized. The tools are sfx_r51b.py's (preview, build, the --docs rows; sfx_r43g.py's seamless
loop). Pending the user's review in round 52; the game plays the a variants until the picks (step C4).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  waterline-cross-r52-a      a fighter jet's roar cut off into a slowed under-water impact over a depth bomb's boom
  waterline-cross-r52-b      an airliner's roar cut off into a slowed snapping-shrimp boom and a hydrophone wave surge
  hydro-refit-r52-a          a heavy deadbolt slam, a heavy mechanical clunk and a truck's hydraulic brake hiss (0.6 s)
  hydro-refit-r52-b          a vault's bolts, a heavy door lock and a hydraulic cutter closing (0.6 s)
  glassfin-shatter-r52-a     a small glass sheet shattered by a rock (0.5 s)
  glassfin-shatter-r52-b     a glass shattering in a bucket, slowed a little (0.45 s)
  eel-pulse-r52-a            a washing-machine door's deep thump over an under-water impact (0.4 s)
  eel-pulse-r52-b            an under-water impact over an air cannon's slowed whump (0.45 s)
  eel-segment-r52-a          a long fabric rip slowed, over an under-water impact's tail (0.6 s)
  eel-segment-r52-b          a stretched rubber ripping, slowed (0.6 s)
  pump-hum-r52-a             a seamless 4 s LOOP: a basement water pump's throb slowed, a centrifugal pump's hum
  pump-hum-r52-b             a seamless 4 s LOOP: a big hydraulic piston pump slowed, a lowered pump drone
  pump-break-r52-a           a heavy clunk, a submarine motor winding down to a stop, a pressure release (1.85 s)
  pump-break-r52-b           a hydraulic cutter's crunch, a straining pipe and a hydraulic brake's hiss (1.4 s)
  scrape-under-r52-a         the chosen canyon scrape's recording slowed and muffled on an under-water impact (0.6 s)
  scrape-under-r52-b         a knife scraping freezer ice, slowed and muffled, on a deep thump (0.6 s)
  europa-under-r52-a         a seamless 16 s LOOP: a deep-sea rumble, slowed steel creaks, far humpback calls
  europa-under-r52-b         a seamless 16 s LOOP: a large vessel's rumble, slowed deep wood creaks, a far blue whale
  explosion-underwater-<tiny|small|medium|large>-r52-a   the depth bomb: a depth bomb's sub boom with a real
                             under-water impact as its body (and, medium and large, a quarry blast's roar slowed and
                             muffled under it)
  explosion-underwater-<tiny|small|medium|large>-r52-b   the hydrophone: a snapping shrimp's click slowed 32 times (a
                             real hydrophone recording, a clean deep boom) with a granite quarry blast heard from 800 m
                             slowed and muffled as its body

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap): the explosion rungs like the game's files of the same rung (tiny like explosion-tiny-r03-a/b, small like
explosion-r02-a/b and explosion-small-r03-a, medium like explosion-r02-c and explosion-medium-r03-b, large like
explosion-r02-d and explosion-large-r03-a) under the loudest of them by LUFS; the crossing a little over Level 01's
launch rail (launch-rail-r47-b); the refit like the Drill Rig's ring (rig-ring-r51-b); the shatter a little under the
`tiny` rung it plays with; the pulse shot like the heavy enemy shot (enemy-shot-heavy-r08-a); the segment's death
like the ring; the scrape like the chosen canyon scrape (canyon-scrape-r36-b); the break like the Drill Rig's death
(rig-death-r51-b); the pump loop by its band RMS like the rig's collar (rig-collar-r51-b, -34 dB), the bed like
`europa-ice` (-36 dB band RMS, at most -28 LUFS). Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r52c.py [subject-or-file ...]   e.g. pump-hum waterline-cross-r52-b
       python3 tools/concept/audio/sfx_r52c.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
SCRIPT = "tools/concept/audio/sfx_r52c.py"
R = "r52"
ROUND = "round 52"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    640505: ("Eurofighter Typhoon Flyby 005 – Close Proximity", "TimoSchmied", BY4, P + "640/640505_4424897-hq.ogg"),
    54967: ("Passenger jet fly-by.wav", "digifishmusic", BY4, P + "54/54967_29541-hq.ogg"),
    400793: ("Underwater Impact", "deleted_user_7709760", BY3, P + "400/400793_7709760-hq.ogg"),
    147877: ("DepthBomb_08.aif", "ZeSoundResearchInc.", CC0, P + "147/147877_2101444-hq.ogg"),
    147878: ("DepthBomb_07.aif", "ZeSoundResearchInc.", CC0, P + "147/147878_2101444-hq.ogg"),
    147879: ("DepthBomb_06.aif", "ZeSoundResearchInc.", CC0, P + "147/147879_2101444-hq.ogg"),
    147880: ("DepthBomb_05.aif", "ZeSoundResearchInc.", CC0, P + "147/147880_2101444-hq.ogg"),
    45439: ("Snapping_Shrimp_Slowed_32X.mp3", "digifishmusic", BY4, P + "45/45439_29541-hq.ogg"),
    258376: ("bolivar_waves_underwater.flac", "wjoojoo", BY4, P + "258/258376_93137-hq.ogg"),
    193822: ("Deep Sea Ambience", "jhumbucker", CC0, P + "193/193822_208303-hq.ogg"),
    407293: ("deadbolt on bathroom wall castle dungeon heavy metal door pull in and out echo latch lock unlock squeak "
             "on offmic more hollow.wav", "kyles", CC0, P + "407/407293_612689-hq.ogg"),
    728170: ("Heavy_mechanical_impact_sounds_various_pitch_sound", "Artninja", BY4, P + "728/728170_10996917-hq.ogg"),
    823069: ("Hydraulic Truck Brakes", "Filmscore", CC0, P + "823/823069_843915-hq.ogg"),
    844296: ("Open Vault", "Funkelfang", BY4, P + "844/844296_18309744-hq.ogg"),
    818290: ("Door Lock On, Off", "microman502", CC0, P + "818/818290_13610145-hq.ogg"),
    453303: ("machine hydraulic metal cutter close crunch1.flac", "kyles", CC0, P + "453/453303_612689-hq.ogg"),
    221528: ("Glass Break", "unfa", CC0, P + "221/221528_1038806-hq.ogg"),
    203372: ("glass-shattering_04.ogg", "C_Rogers", CC0, P + "203/203372_3569783-hq.ogg"),
    400997: ("Deep Thumps.wav", "eardeer", CC0, P + "400/400997_2155630-hq.ogg"),
    870742: ("Cannon Fires, Take 1, Quarter Speed", "CHallSmith", CC0, P + "870/870742_18412227-hq.ogg"),
    455680: ("clothing rip tear fabric various long short or electric short circuit crackle zap or masking tape.flac",
             "kyles", CC0, P + "455/455680_612689-hq.ogg"),
    253327: ("Rubber Stretch Rip 1.wav", "mickdow", CC0, P + "253/253327_2636846-hq.ogg"),
    189314: ("Basement Water Pump.flac", "qubodup", CC0, P + "189/189314_71257-hq.ogg"),
    177195: ("Industrial Centrifugal Water Pump contactmic 130203_00.wav", "klankbeeld", BY4,
             P + "177/177195_1648170-hq.ogg"),
    346815: ("indutrial hydraulic pump LOOP 160531_0886.wav", "klankbeeld", BY4, P + "346/346815_1648170-hq.ogg"),
    140217: ("big_drone_lowered.wav", "decembered", CC0, P + "140/140217_454894-hq.ogg"),
    438720: ("G45-18-Submarine Motor Stop.wav", "craigsmith", CC0, P + "438/438720_2524442-hq.ogg"),
    457294: ("Air (or steam) pressure release", "brunoboselli", CC0, P + "457/457294_300738-hq.ogg"),
    193820: ("Faucet or pipes strain", "jhumbucker", CC0, P + "193/193820_208303-hq.ogg"),
    121623: ("big metal side impact 3 scraping.wav", "Halleck", BY4, P + "121/121623_6281-hq.ogg"),
    380252: ("Freezer Ice Scrape Knife 22.wav", "Glitchedtones", CC0, P + "380/380252_3294528-hq.ogg"),
    232021: ("Stereotypical sinking submarine sound (NO REVERB).aif", "Ubehag", CC0, P + "232/232021_3839046-hq.ogg"),
    797762: ("humpback_whales_and_orcas_ambient_underwater_recording", "raiden04", BY4,
             P + "797/797762_17082821-hq.ogg"),
    130506: ("Large Vessel.wav", "duckduckpony", BY4, P + "130/130506_1653827-hq.ogg"),
    452107: ("wood creaks deep long door or ship hull1.flac", "kyles", CC0, P + "452/452107_612689-hq.ogg"),
    343682: ("Blue Whale B call @ 5X", "MBARI_MARS", BY4, P + "343/343682_6181234-hq.ogg"),
    82682: ("Quarry blasting.wav", "juskiddink", BY4, P + "82/82682_649468-hq.ogg"),
    584594: ("Explosive works.wav", "tosha73", BY4, P + "584/584594_10725617-hq.ogg"),
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




UW_SHOULD = ("a deep, heavy pressure boom heard under water: a muffled body and a long low tail, no splash, no bubble "
             "rattle, nothing that pops")


def uw_use(rung, examples):
    return (f"every kill of a `{rung}` unit below the waterline (Levels 23-28, e.g. {examples}), in place of the "
            f"air rung (the game picks a random variant per kill, ±4 % pitch)")


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
SOUNDS = {
    "waterline-cross": ("shot", -20.0, -3.0, -16.0,
                        "Level 23 at t=25.5, once a run: the ship (and Rook) crossing the waterline at the foot of the "
                        "dry shaft mouth into the water, as the screen's grade wipes to under water and the NO WATER "
                        "rows flash back on",
                        "a heavy submersion swell: the air's roar cut off into a deep, muffled whoomph, the world "
                        "closing in; about 2 s, no splash, no plop, no bubbles", {
        "a": ([L(640505, 3.55, 0.55, fin=0.05, fout=0.012, hp=60.0, swell=(-10.0, 1.0)),
               L(400793, 0.06, 1.5, at=0.53, speed=0.8, fin=0.004, fout=0.8, hp=30.0, lp=1500.0),
               L(147877, 0.0, 1.4, gain=-4.0, at=0.53, fin=0.004, fout=0.8, hp=25.0),
               L(193822, 22.0, 1.3, gain=-8.0, at=0.6, fin=0.1, fout=0.6, hp=25.0)],
              "a fighter jet's roar (a close Eurofighter flyby) swelling for half a second and cut off dead, and in "
              "its place an under-water impact slowed to 0.8 and muffled at 1.5 kHz over a depth bomb's sub boom, "
              "with a deep-sea rumble settling in under it: the roar shut off by the water, a deep whoomph and the "
              "pressure, 2.0 s"),
        "b": ([L(54967, 42.0, 0.5, gain=-4.0, fin=0.05, fout=0.012, hp=60.0, swell=(-10.0, 1.0)),
               L(45439, 19.34, 1.4, at=0.48, fin=0.004, fout=0.7, hp=25.0),
               L(258376, 1.3, 1.2, gain=-2.0, at=0.5, speed=0.8, fin=0.01, fout=0.6, hp=60.0, lp=1500.0)],
              "an airliner's roar at its loudest (a passenger jet passing over) swelling and cut off dead, and in "
              "its place a snapping shrimp's click slowed 32 times (a real hydrophone recording: a clean deep boom) "
              "with a wave surging over a hydrophone, slowed and muffled: rounder and wetter than a, the boom lower "
              "and cleaner, 1.88 s"),
    }),
    "hydro-refit": ("shot", -21.0, -4.0, -18.0,
                    "Level 23 at the waterline crossing, once a run, with the crossing: the Stormhawk's hydro-kit "
                    "locking for water (the refit tell)",
                    "heavy clamps closing and a hydraulic seal: two hard metal locks and a short pressure hiss, "
                    "0.6 s, mechanical, no clicky toy sound", {
        "a": ([L(407293, 2.38, 0.3, fin=0.003, fout=0.2, hp=80.0),
               L(728170, 1.17, 0.45, at=0.15, speed=0.85, fin=0.003, fout=0.3, hp=50.0),
               L(823069, 4.15, 0.4, gain=-10.0, at=0.2, fin=0.01, fout=0.25, hp=300.0)],
              "a heavy dungeon-door deadbolt slammed home, then a heavy mechanical clunk (a microwave door slammed, "
              "a big low impact) slowed to 0.85, and a truck's hydraulic brake hiss under it: clamp, clamp and the "
              "seal, 0.6 s"),
        "b": ([L(844296, 11.45, 0.45, fin=0.003, fout=0.25, hp=80.0),
               L(818290, 2.35, 0.4, gain=-2.0, at=0.18, fin=0.003, fout=0.25, hp=60.0),
               L(453303, 0.45, 0.6, gain=-6.0, speed=0.8, fin=0.02, fout=0.3, hp=60.0)],
              "a vault's heavy bolts thrown, then a heavy door lock engaging, over a hydraulic metal cutter closing "
              "slowed to 0.8 (the press): more bolt-work and grind than a, less hiss, 0.6 s"),
    }),
    "glassfin-shatter": ("shot", -20.0, -4.0, -19.5,
                         "every Glassfin killed (Level 23 on; schools of ten or more tiny glassy fish, so often), "
                         "with the under-water `tiny` burst",
                         "a thin glass shatter: small, dry and bright, short, never a clink-and-tinkle tail that "
                         "lingers", {
        "a": ([L(221528, 0.31, 0.5, fin=0.002, fout=0.25, hp=300.0)],
              "a small sheet of glass shattered with a rock: one sharp crack and the shards falling, 0.5 s"),
        "b": ([L(203372, 0.03, 0.45, speed=0.9, fin=0.002, fout=0.22, hp=250.0)],
              "a glass shattering in a bucket (a dull, boxed-in break) slowed to 0.9: shorter and drier than a, "
              "less ring, 0.45 s"),
    }),
    "eel-pulse": ("shot", -23.0, -6.0, -20.0,
                  "each pulse shot an Eel Swarm's head fires (Level 23 on)",
                  "a dull pressure thump: short, heavy and soft-edged, a push of water rather than a gunshot; no "
                  "squelch", {
        "a": ([L(400997, 3.05, 0.4, fin=0.003, fout=0.25, hp=40.0, lp=1500.0),
               L(400793, 0.06, 0.4, gain=-4.0, fin=0.003, fout=0.25, hp=30.0, lp=1200.0)],
              "a deep thump (a washing-machine door slammed, recorded close: a huge-gate-like boom) muffled at "
              "1.5 kHz, over an under-water impact: a short round thud, 0.4 s"),
        "b": ([L(400793, 0.06, 0.45, fin=0.003, fout=0.25, hp=30.0, lp=1500.0),
               L(870742, 0.05, 0.45, gain=-3.0, fin=0.003, fout=0.25, hp=40.0, lp=1000.0)],
              "an under-water impact over an air cannon's blast at quarter speed, muffled at 1 kHz: a soft, "
              "pushing whump with more air in it than a, 0.45 s"),
    }),
    "eel-segment": ("shot", -21.0, -4.0, -18.0,
                    "each Eel Swarm segment destroyed (Level 23 on; segments die in quick runs along the chain), "
                    "with the under-water `tiny` or `small` burst",
                    "a wet tearing slide, short: something tough tearing loose; no squelch, no slurp", {
        "a": ([L(455680, 3.52, 0.6, speed=0.8, fin=0.004, fout=0.25, hp=150.0, lp=5000.0),
               L(400793, 0.25, 0.5, gain=-10.0, at=0.05, fin=0.02, fout=0.3, hp=30.0, lp=1200.0)],
              "a long rip of heavy fabric slowed to 0.8 and softened at 5 kHz, over an under-water impact's tail: "
              "a tearing slide with a damp body, 0.6 s"),
        "b": ([L(253327, 5.95, 0.6, speed=0.75, fin=0.004, fout=0.25, hp=120.0, lp=4500.0)],
              "stretched rubber ripping (a live recording) slowed to 0.75 and softened at 4.5 kHz: rougher and more "
              "rubbery than a, a gristly tear, 0.6 s"),
    }),
    "pump-hum": ("loop", -34.0, -1.0, -27.0,
                 "while a siphon pump lives on the screen (Level 23's section 3, five farm racks, each with a pump; "
                 "one quiet loop for all of them, as the Drill Rig's collar, stopping with the last pump)",
                 "a deep pump throb: a big water pump labouring in a steady slow rhythm, muffled, low in the mix "
                 "under the fire, no clicks, a seamless loop", {
        "a": (([Loop(189314, 1.4, speed=0.7, hp=30.0, lp=2000.0),
                Loop(177195, 5.0, gain=-6.0, speed=0.7, hp=30.0, lp=1500.0)], 4.0, 0.5),
              "a basement water pump's rhythmic throb slowed to 0.7 (a surge about every 2 s) over an industrial "
              "centrifugal water pump's hum (a contact-mic recording) slowed the same: a labouring pump, 4.0 s "
              "loop"),
        "b": (([Loop(346815, 10.0, speed=0.6, hp=30.0, lp=1800.0),
                Loop(140217, 10.0, gain=-6.0, speed=0.8, hp=25.0, lp=1200.0)], 4.0, 0.5),
              "a large industrial hydraulic piston pump slowed to 0.6 (its whine dropped to a groan) over a water "
              "pump's drone pitched down: steadier and more of a machine-hall groan than a's throb, 4.0 s loop"),
    }),
    "pump-break": ("shot", -18.0, -2.0, -15.0,
                   "a siphon pump destroyed (Level 23, five, the secondary; with its `medium` under-water burst): "
                   "the pump stops and its line vents",
                   "a heavy mechanical failure with a pressure release: a hard clunk, the machine dying and a "
                   "rush of pressure; about 2 s, a few dB under the burst it plays with", {
        "a": ([L(728170, 6.81, 0.6, fin=0.003, fout=0.35, hp=50.0),
               L(438720, 9.0, 1.8, gain=-2.0, at=0.05, speed=0.75, fin=0.02, fout=0.5, hp=40.0, lp=4000.0),
               L(457294, 6.0, 1.4, gain=-8.0, at=0.3, fin=0.15, fout=0.6, hp=300.0)],
              "a heavy mechanical clunk, then a submarine's motor running down to a stop with a rattle (a vintage "
              "film-library recording) slowed to 0.75, and a pressure release's hiss rising under it: the pump "
              "seizing and venting, 1.85 s"),
        "b": ([L(453303, 0.42, 1.0, speed=0.8, fin=0.003, fout=0.4, hp=50.0),
               L(193820, 2.2, 0.6, gain=-3.0, at=0.25, fin=0.005, fout=0.3, hp=80.0, lp=5000.0),
               L(823069, 4.15, 1.0, gain=-6.0, at=0.4, fin=0.02, fout=0.5, hp=200.0)],
              "a hydraulic metal cutter's crunch slowed to 0.8, a pipe straining and giving, and a truck's "
              "hydraulic brake hiss: more crunch and burst than a, less motor, 1.4 s"),
    }),
    "scrape-under": ("shot", -18.0, -4.0, -15.5,
                     "the ship (or Rook) touching the shaft or trench wall under water (Levels 23 and 25; the canyon "
                     "scrape's rule: once per contact, a little under the armour or shield hit, panned to the "
                     "touching side)",
                     "metal grinding on ice or rock, muffled by the water: a hard onset and a rough tail that reads as "
                     "damage, 0.6 s", {
        "a": ([L(121623, 0.08, 0.6, speed=0.8, fin=0.002, fout=0.25, hp=60.0, lp=2500.0),
               L(400793, 0.06, 0.35, gain=-3.0, fin=0.002, fout=0.2, hp=30.0, lp=1200.0)],
              "the chosen canyon scrape's recording (a big metal side impact scraping) slowed to 0.8 and muffled at "
              "2.5 kHz, on an under-water impact: the screech gone dull and heavy, 0.6 s"),
        "b": ([L(380252, 1.15, 0.6, speed=0.6, fin=0.002, fout=0.25, hp=150.0, lp=3000.0),
               L(400997, 3.05, 0.3, gain=-4.0, fin=0.002, fout=0.2, hp=40.0, lp=1200.0)],
              "a knife scraping freezer ice slowed to 0.6 and muffled at 3 kHz, on a deep thump: metal on ice, "
              "grittier and more juddering than a, 0.6 s"),
    }),
    "europa-under": ("loop", -36.0, -1.0, -28.0,
                     "Level 23 from the waterline on (crossfading in from `europa-ice` at the crossing) and Level 24: "
                     "the ambience bed under Europa's ice",
                     "muffled deep water: a heavy pressure rumble, hull creaks now and then and far whale-like "
                     "calls; quiet, under the music, no bubbles, no drips, a seamless loop", {
        "a": (([Loop(193822, 20.0, hp=25.0),
                Loop(232021, 0.0, gain=-14.0, speed=0.6, hp=60.0, lp=1500.0),
                Loop(797762, 64.0, gain=-16.0, speed=0.8, hp=120.0, lp=1500.0)], 16.0, 1.0),
              "a deep-sea rumble (low, heavy, a steady pressure) with a sinking submarine's steel creaks slowed to "
              "0.6 and muffled, and humpback whales calling far off (a real hydrophone recording) slowed to 0.8: "
              "16.0 s loop"),
        "b": (([Loop(130506, 7.0, gain=-6.0, hp=25.0, lp=1500.0),
                Loop(452107, 0.0, gain=-4.0, speed=0.5, hp=40.0, lp=1200.0),
                Loop(343682, 3.0, gain=-14.0, speed=0.7, hp=60.0, lp=1200.0)], 16.0, 1.0),
              "a large vessel's low rumble passing under water (deep, a little metallic) muffled at 1.5 kHz, a deep "
              "wooden hull creaking slowed to 0.5 and muffled, and a blue whale's B call (a real hydrophone "
              "recording, played at 3.5 times its speed) far off: darker and emptier than a, two long moans a loop, "
              "16.0 s loop"),
    }),
    "explosion-underwater-tiny": ("shot", -17.0, -2.0, -13.0,
                                  uw_use("tiny", "Glassfin, eel segments, shootable torpedoes and mines"),
                                  UW_SHOULD + "; at most 0.6 s", {
        "a": ([L(400793, 0.06, 0.5, fin=0.003, fout=0.3, hp=30.0, lp=1500.0),
               L(147879, 0.0, 0.5, gain=-3.0, fin=0.003, fout=0.3, hp=25.0)],
              "the depth bomb: an under-water impact muffled at 1.5 kHz over a depth bomb's sub boom, cut short: a "
              "small deep thud, 0.5 s"),
        "b": ([L(45439, 12.13, 0.5, fin=0.003, fout=0.3, hp=25.0),
               L(400997, 6.0, 0.35, gain=-3.0, fin=0.003, fout=0.2, hp=40.0, lp=1500.0)],
              "the hydrophone: a snapping shrimp's click slowed 32 times (a clean, deep boom) with a deep thump "
              "muffled at 1.5 kHz: tighter and lower than a, 0.5 s"),
    }),
    "explosion-underwater-small": ("shot", -16.5, -1.5, -13.0,
                                   uw_use("small", "Driftjelly"), UW_SHOULD + "; 0.7-1.1 s", {
        "a": ([L(400793, 0.06, 0.9, speed=0.85, fin=0.003, fout=0.5, hp=30.0, lp=1500.0),
               L(147877, 0.0, 0.9, gain=-2.0, fin=0.003, fout=0.5, hp=25.0)],
              "the depth bomb: an under-water impact slowed to 0.85 and muffled over a depth bomb's sub boom: a "
              "round deep thud with a short tail, 0.9 s"),
        "b": ([L(45439, 3.52, 0.9, fin=0.003, fout=0.5, hp=25.0),
               L(82682, 3.87, 0.9, gain=-5.0, speed=0.8, fin=0.003, fout=0.5, hp=40.0, lp=1500.0)],
              "the hydrophone: a snapping shrimp's click slowed 32 times (a deep boom) with a granite quarry blast "
              "heard from 800 m, slowed to 0.8 and muffled at 1.5 kHz, as its body: a boom with a rough rolling "
              "body, 0.9 s"),
    }),
    "explosion-underwater-medium": ("shot", -16.0, -1.5, -12.0,
                                    uw_use("medium", "Reef Spitter, Minelayer, the siphon pump"),
                                    UW_SHOULD + "; 1.6-2 s", {
        "a": ([L(147878, 0.0, 1.8, fin=0.003, fout=0.9, hp=25.0),
               L(400793, 0.06, 1.4, gain=-2.0, speed=0.7, fin=0.003, fout=0.7, hp=30.0, lp=1200.0),
               L(584594, 50.11, 1.8, gain=-9.0, speed=0.7, fin=0.02, fout=0.9, hp=40.0, lp=1000.0)],
              "the depth bomb: a depth bomb's sub boom with an under-water impact slowed to 0.7 and muffled at 1.2 "
              "kHz as its body and a quarry blast's roar slowed to 0.7 and muffled at 1 kHz low under it: a heavy "
              "pressure boom with a rolling tail, 1.8 s"),
        "b": ([L(45439, 7.89, 1.8, fin=0.003, fout=0.9, hp=25.0),
               L(82682, 6.95, 1.8, gain=-4.0, speed=0.8, fin=0.003, fout=0.9, hp=40.0, lp=1800.0)],
              "the hydrophone: a snapping shrimp's click slowed 32 times (a deep, clean boom) with a granite quarry "
              "blast heard from 800 m, slowed to 0.8 and muffled at 1.8 kHz: more crack and roll in the body than a, "
              "1.8 s"),
    }),
    "explosion-underwater-large": ("shot", -14.0, -1.5, -11.0,
                                   uw_use("large", "Abyss Ray, mid-boss parts, boss phase ends"),
                                   UW_SHOULD + "; 2.8-3.1 s", {
        "a": ([L(147880, 0.0, 3.0, fin=0.003, fout=1.4, hp=25.0),
               L(400793, 0.06, 2.0, gain=-2.0, speed=0.6, fin=0.003, fout=1.0, hp=30.0, lp=1000.0),
               L(584594, 50.11, 3.0, gain=-5.0, speed=0.6, fin=0.02, fout=1.4, hp=35.0, lp=900.0)],
              "the depth bomb: a long depth-bomb boom with an under-water impact slowed to 0.6 and a quarry blast's "
              "roar slowed to 0.6, both muffled under 1 kHz: a huge pressure boom and a long low rumble, 3.0 s"),
        "b": ([L(45439, 19.34, 3.0, fin=0.003, fout=1.4, hp=25.0),
               L(82682, 6.95, 3.0, gain=-3.0, speed=0.65, fin=0.003, fout=1.4, hp=35.0, lp=1400.0)],
              "the hydrophone: a snapping shrimp's click slowed 32 times (a deep boom and a second one 0.7 s after, "
              "rolling) with a granite quarry blast heard from 800 m slowed to 0.65 and muffled at 1.4 kHz: a "
              "double boom with a long rough rumble, 3.0 s"),
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
