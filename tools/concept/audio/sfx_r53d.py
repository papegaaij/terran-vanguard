#!/usr/bin/env python3
"""Concept round 53 (M8 part D, step AUD-D): Level 24's sounds. The Abyss Ray's rising horn (the shadow's tell), its
glide, its fan and its death; the Barbfin's needle; the spawning reef's iris and its collapse; the relay beacons'
pulse; the wrecked survey sub *Meridian*'s hatch. All cut from real recordings in the Freesound database (round 48's
rules: heavy and big, muffled and pressured under water, no bubbly, popping or squelching sounds, no low-pass on the
sfx bus: each layer carries its own filters). No new bed: Level 24 plays `europa-under` (round 52). The Scuttler's walk
on silt reuses its chosen walk with a low-pass (the gap check's call), so it has no row here.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 53 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. This script only cuts, slows, layers, swells, fades, filters and levels
them; nothing is synthesized. The tools are sfx_r52c.py's (copied: preview, build, the --docs rows). Pending the user's
review in round 53; the game plays the a variants until the picks (step D4).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  ray-rise-r53-a         a cruise ship's fog horn slowed to 0.7 and muffled, swelling out of a deep-sea rumble (2.9 s)
  ray-rise-r53-b         a conch shell blown, slowed to 0.55 and rising to 0.7, over a real sea-floor earthquake heard
                         by a deep-sea hydrophone, sped up 2.5 times into a rumble (2.9 s)
  ray-glide-r53-a        two whooshes of a big tarp shaken through the air, slowed to half and muffled, over a
                         faint deep-sea rumble (3.5 s)
  ray-glide-r53-b        a winged beast's large wing beats (foley) slowed to half and muffled, over a slowed
                         hydrophone wave surge (3.6 s)
  ray-fan-r53-a          a tyre bead seater's compressed-air blast slowed and muffled over a depth bomb's boom (0.65 s)
  ray-fan-r53-b          a forced breath through a didgeridoo slowed and muffled over a depth bomb's boom (0.65 s)
  ray-death-r53-a        a camel's groan slowed into a falling moan over a depth bomb's sub boom (2.8 s)
  ray-death-r53-b        the conch call slowed and falling away over a snapping shrimp's click slowed 32 times (2.8 s)
  barbfin-needle-r53-a   a blowgun dart shot, slowed a little, its plosive thump cut (0.3 s)
  barbfin-needle-r53-b   an industrial nail gun's shot slowed and muffled (0.25 s)
  reef-iris-r53-a        a heavy stone grind slowed, with a handful of seashells jammed together under it (0.6 s)
  reef-iris-r53-b        stone dragged on stone slowed, with a seashell clatter slowed under it (0.65 s)
  reef-death-r53-a       a rockslide of large granite rocks slowed a little and muffled over a depth bomb's boom (2.2 s)
  reef-death-r53-b       a load of kilned clay bricks falling, slowed and muffled, over a wall falling (2.4 s)
  beacon-pulse-r53-a     a real 625 Hz active sonar ping slowed to 0.6 (about 400 Hz) and muffled (1.9 s)
  beacon-pulse-r53-b     a large singing bowl's soft-stick hit (a round 156 Hz tone) sped to 1.25, muffled (1.9 s)
  wreck-hatch-r53-a      a submarine hatch opening (metal clanks and a creak), slowed a little, on an under-water
                         impact (1.6 s)
  wreck-hatch-r53-b      a metal hatch slammed, slowed to 0.6, and a bunker door's low hinge groan (1.65 s)

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the rise like the Leviathan's cry and the Herald's song
(enemy-leviathan-cry-r47-b, herald-song-r47-a), the glide like the Overcharge whoosh (overcharge-whoosh-r46-b) a few
dB under, the fan like the heavy enemy shot (enemy-shot-heavy-r08-a), the Ray's and the reef's death like the
Warden's death (tank-death-r46-a) and the Drill Rig's (rig-death-r51-b), the needle like the small enemy shots
(enemy-shot-small-r08-a/b), the iris like the Dust Devil's opening (devil-open-r46-a), the beacon like the Sonar
Pulse ping (special-sonar-r04-a) 6 dB under (the game plays it lower still, under the music), the hatch like the
hydro-kit refit (hydro-refit-r52-a). Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r53d.py [subject-or-file ...]   e.g. ray-rise beacon-pulse-r53-b
       python3 tools/concept/audio/sfx_r53d.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
from sfx_r43g import L, cut, loop  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, undb, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r53d.py"
R = "r53"
ROUND = "round 53"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    636075: ("Ship Horn - Fog Horn - Cruise Ship Grand Princess", "coalcon", CC0, P + "636/636075_3320549-hq.ogg"),
    193822: ("Deep Sea Ambience", "jhumbucker", CC0, P + "193/193822_208303-hq.ogg"),
    78974: ("Conch.wav", "RoofDog", CC0, P + "78/78974_1227539-hq.ogg"),
    369485: ("Earthquake  **audible only with appropriate speakers**", "MBARI_MARS", CC0, P + "369/369485_6181234-hq.ogg"),
    481896: ("R24-18-Tent or Tarp Whoosh.wav", "craigsmith", CC0, P + "481/481896_2524442-hq.ogg"),
    596541: ("Large Wings Flapping - Foley.wav", "tothrec2", CC0, P + "596/596541_9801597-hq.ogg"),
    258376: ("bolivar_waves_underwater.flac", "wjoojoo", BY4, P + "258/258376_93137-hq.ogg"),
    863981: ("Tire Bead Seater Air Blast", "j_soundeffects", BY4, P + "863/863981_14030247-hq.ogg"),
    147880: ("DepthBomb_05.aif", "ZeSoundResearchInc.", CC0, P + "147/147880_2101444-hq.ogg"),
    824961: ("HMNBrth-Blue Snowball Microphone, MCU_Thru Didgeridoo, Elephant Like_Nicholas Judy_TDC",
             "designerschoice", BY4, P + "824/824961_6951162-hq.ogg"),
    147878: ("DepthBomb_07.aif", "ZeSoundResearchInc.", CC0, P + "147/147878_2101444-hq.ogg"),
    385912: ("S035_Camels_Mono.wav", "Pól", CC0, P + "385/385912_7097737-hq.ogg"),
    147879: ("DepthBomb_06.aif", "ZeSoundResearchInc.", CC0, P + "147/147879_2101444-hq.ogg"),
    45439: ("Snapping_Shrimp_Slowed_32X.mp3", "digifishmusic", BY4, P + "45/45439_29541-hq.ogg"),
    531973: ("blowgun - pipeblow - dart shotting", "ramonmineiro", CC0, P + "531/531973_5020041-hq.ogg"),
    143615: ("industrial_nailgun_1shot+ring", "gilly11", BY3, P + "143/143615_2047402-hq.ogg"),
    760672: ("Guterton - Heavy Stone Grind", "guterton.wav", BY4, P + "760/760672_2392999-hq.ogg"),
    667184: ("Jammed seashells", "MBPL", CC0, P + "667/667184_14585550-hq.ogg"),
    29986: ("stone_on_stone.aif", "thanvannispen", BY4, P + "29/29986_13258-hq.ogg"),
    807564: ("FOLYProp-Blue Snowball Microphone, CU_Seashell, Clatter 08_Nicholas Judy_TDC", "designerschoice", BY4,
             P + "807/807564_6951162-hq.ogg"),
    79783: ("Rockslide 2.wav", "juskiddink", BY4, P + "79/79783_649468-hq.ogg"),
    77086: ("Bricks Falling 2.wav", "Benboncan", BY4, P + "77/77086_634166-hq.ogg"),
    675899: ("S30-08 Wall falls down from fire damage.wav", "craigsmith", CC0, P + "675/675899_2524442-hq.ogg"),
    12677: ("Sonar_pings.ogg", "Peter_Gross", CC0, P + "12/12677_33429-hq.ogg"),
    616335: ("Singing Bowl - Soft Stick Meditative Hit", "Rimych", CC0, P + "616/616335_11045601-hq.ogg"),
    438732: ("G45-30-Submarine Hatch Opening.wav", "craigsmith", CC0, P + "438/438732_2524442-hq.ogg"),
    400793: ("Underwater Impact", "deleted_user_7709760", BY3, P + "400/400793_7709760-hq.ogg"),
    718440: ("Metal_Hatch_Small_Close_01", "Rudmer_Rotteveel", CC0, P + "718/718440_4921277-hq.ogg"),
    496836: ("Metallic Groan", "hinchinbrook", CC0, P + "496/496836_9847211-hq.ogg"),
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




# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS.
RAY_USE = ("Level 24, each Abyss Ray (three a run, the first at t≈57.6, the second over the beacon line at t=112; one "
           "on the screen at a time)")
SOUNDS = {
    "ray-rise": ("shot", -21.0, -4.0, -18.0,
                 f"{RAY_USE}: from its entry, while its shadow grows 1.5 s under the ship's lane and through the 0.9 s "
                 "rise (the audio tell before its fans; Varga's first-sight line comes with the first)",
                 "a low, heavy, swelling horn-like call from deep below: it grows over the shadow's 1.5 s, is loudest "
                 "as the Ray breaks out (about 2.4 s) and lets go; huge, far down and muffled, no bubbles", {
        "a": ([L(636075, 0.5, 2.9, speed=0.7, fin=0.3, fout=0.5, hp=40.0, lp=900.0, swell=(-24.0, 1.6)),
               L(193822, 22.0, 2.9, gain=-6.0, fin=0.2, fout=0.6, hp=25.0, swell=(-14.0, 1.0))],
              "a cruise ship's fog horn (a real one, recorded on deck) slowed to 0.7 (its 106 Hz note down to about "
              "75 Hz) and muffled at 900 Hz, swelling up out of a deep-sea rumble and letting go: a vast mechanical "
              "'hoooo' from the dark, 2.9 s"),
        "b": ([L(78974, 0.45, 2.9, speed=(0.55, 0.7), fin=0.25, fout=0.5, hp=40.0, lp=1400.0, swell=(-22.0, 1.4)),
               L(369485, 5.0, 2.9, gain=-10.0, speed=2.5, fin=0.2, fout=0.6, hp=30.0, swell=(-12.0, 1.0))],
              "a conch shell blown on a rooftop, slowed to 0.55 and rising to 0.7 as it swells (about 160 to 220 Hz), "
              "over a real earthquake heard by a deep-sea hydrophone at 900 m, sped up 2.5 times into an audible "
              "rumble: an animal call, rounder and more alive than a, 2.9 s"),
    }),
    "ray-glide": ("shot", -24.0, -6.0, -21.0,
                  f"{RAY_USE}: once as it levels out after the rise, under its 5 s glide across the field at 70 px/s "
                  "(its fans fire during it)",
                  "the huge wings pushing water: two or three slow, deep strokes, a soft rushing swell rather than a "
                  "flap, about 3.5 s; no splash, no bubbles", {
        "a": ([L(481896, 4.35, 2.0, speed=0.5, fin=0.1, fout=0.7, hp=30.0, lp=600.0),
               L(481896, 8.15, 2.0, speed=0.5, at=1.5, fin=0.1, fout=0.8, hp=30.0, lp=600.0),
               L(193822, 40.0, 3.5, gain=-12.0, fin=0.3, fout=1.0, hp=25.0)],
              "two whooshes of a big tarp shaken through the air (a vintage film-library foley), slowed to half and "
              "muffled at 600 Hz, 1.5 s apart, over a faint deep-sea rumble: two deep soft 'whooom' strokes, 3.5 s"),
        "b": ([L(596541, 0.1, 3.6, speed=0.5, fin=0.05, fout=0.6, hp=30.0, lp=900.0),
               L(258376, 1.3, 3.6, gain=-8.0, speed=0.6, fin=0.3, fout=0.8, hp=40.0, lp=800.0)],
              "a winged beast's large wing beats (foley) slowed to half and muffled at 900 Hz, three heavy beats "
              "1.5 s apart, over a wave surging past a hydrophone slowed and muffled: more of a beat than a, "
              "3.6 s"),
    }),
    "ray-fan": ("shot", -23.0, -6.0, -20.0,
                f"{RAY_USE}: each fan it fires, three 1.2 s apart at the top of its glide (two on easy, four on hard), "
                "7-11 slow bullets spreading 110 degrees from its mouth",
                "a heavy pressure push from the Ray's mouth: a deep, soft-edged whump with a short rushing exhale, "
                "the heavy enemy shot's weight, about 0.6 s; no gunshot crack, no pop", {
        "a": ([L(863981, 0.13, 0.65, speed=0.7, fin=0.004, fout=0.35, hp=40.0, lp=1400.0),
               L(147880, 0.0, 0.65, gain=-8.0, fin=0.004, fout=0.4, hp=25.0)],
              "a tyre bead seater's compressed-air blast (an air tank with a bazooka nozzle) slowed to 0.7 and "
              "muffled at 1.4 kHz, over a depth bomb's sub boom: a round heavy 'whumf' with a short rush, 0.65 s"),
        "b": ([L(824961, 6.25, 0.65, speed=0.75, fin=0.01, fout=0.35, hp=50.0, lp=1800.0),
               L(147878, 0.0, 0.65, gain=-6.0, fin=0.004, fout=0.4, hp=25.0)],
              "a forced breath blown through a didgeridoo (an elephant-like exhale) slowed to 0.75 and muffled, over "
              "a depth bomb's sub boom: breathier and more animal than a, a 'hhhff' with a thud under it, 0.65 s"),
    }),
    "ray-death": ("shot", -19.0, -3.0, -15.0,
                  f"{RAY_USE}: killed (210 HP; the first drops the Overdrive), with the under-water `large` burst, as "
                  "its wings fold and the body sinks",
                  "a huge creature's dying groan sinking away over a deep boom: a long low moan falling in pitch, "
                  "heavy and muffled, about 2.5 s; no squeal, no bubbles", {
        "a": ([L(385912, 0.15, 2.8, speed=(0.6, 0.42), fin=0.03, fout=1.0, hp=40.0, lp=1200.0),
               L(147879, 0.0, 2.8, gain=-5.0, fin=0.004, fout=1.4, hp=25.0)],
              "a camel's groan (a real one, recorded at a bazaar) slowed from 0.6 to 0.42 so it sinks as it goes and "
              "muffled at 1.2 kHz, over a depth bomb's sub boom: a deep dying moan with a body, 2.8 s"),
        "b": ([L(78974, 3.0, 2.8, speed=(0.65, 0.4), fin=0.05, fout=1.0, hp=40.0, lp=1200.0),
               L(45439, 19.34, 2.8, gain=-9.0, fin=0.004, fout=1.2, hp=25.0)],
              "the conch call of rise b slowed from 0.65 to 0.4 so it falls away (about 210 down to 130 Hz), muffled, "
              "over a snapping shrimp's click slowed 32 times (a real hydrophone recording: a clean deep boom): the "
              "Ray's own call dying, more tonal than a, 2.8 s"),
    }),
    "barbfin-needle": ("shot", -27.0, -8.0, -22.0,
                       "Level 24 on, every needle a Barbfin fires: an aimed small violet needle every 2.4 s per "
                       "Barbfin (four with each Ray, columns of three to five; hard: pairs 0.12 s apart), so often "
                       "and many at once",
                       "a short, sharp spit: a dart blown hard from a tube, tight and dry with a little body, "
                       "0.2-0.3 s; not a laser zap, not a pop, never tiring when many fire", {
        "a": ([L(531973, 1.85, 0.3, speed=0.85, fin=0.002, fout=0.12, hp=250.0, lp=7000.0)],
              "a blowgun dart shot (a real blowpipe) slowed to 0.85, its breath plosive's low thump cut at 250 Hz: a "
              "quick 'fsst-tk', 0.3 s"),
        "b": ([L(143615, 1.66, 0.25, speed=0.8, fin=0.002, fout=0.12, hp=150.0, lp=3500.0)],
              "an industrial nail gun's shot slowed to 0.8 and muffled at 3.5 kHz: a harder pneumatic 'tchk' than "
              "a, more mechanical, 0.25 s"),
    }),
    "reef-iris": ("shot", -22.0, -5.0, -18.0,
                  "Level 24, each spawning reef's 0.5 s telegraph before it releases its fry (four Glassfins, three "
                  "on easy; every 3.5 s while a reef lives and the ship is not within 96 px of it): the iris of "
                  "chitin plates grinding open, the fry darting out at its end",
                  "a slow grinding of heavy chitin plates sliding apart: a rough stone-and-shell grind, dry and "
                  "heavy, 0.6 s; nothing bubbly, no squelch", {
        "a": ([L(760672, 0.75, 0.6, speed=0.75, fin=0.06, fout=0.15, hp=60.0, lp=3000.0),
               L(667184, 1.95, 0.6, gain=-8.0, speed=0.7, fin=0.03, fout=0.15, hp=400.0, lp=4000.0)],
              "a heavy stone ground on stone, slowed to 0.75 and muffled at 3 kHz, with a handful of seashells "
              "jammed together under it (slowed, quiet): a gritty shell-plate grind, 0.6 s"),
        "b": ([L(29986, 0.15, 0.65, speed=0.6, fin=0.05, fout=0.15, hp=60.0, lp=2500.0),
               L(807564, 0.3, 0.5, gain=-8.0, at=0.1, speed=0.6, fin=0.02, fout=0.15, hp=300.0, lp=4000.0)],
              "stone dragged on stone slowed to 0.6 and muffled, with a seashell clattering slowed under it: a "
              "deeper, smoother drag than a with a brittle shell rattle in it, 0.65 s"),
    }),
    "reef-death": ("shot", -19.0, -3.0, -15.0,
                   "Level 24, a spawning reef razed (200 HP, the secondary REEFS RAZED; three of four, two on easy), "
                   "with the under-water `large` burst, as the coral tower falls to a stump and its iris shuts",
                   "a heavy crumbling of coral-like rock: a big brittle break and rubble sliding down, muffled by "
                   "the water, about 2 s; no splash, nothing that pops", {
        "a": ([L(79783, 2.25, 2.2, speed=0.9, fin=0.01, fout=0.8, hp=90.0, lp=3500.0),
               L(147878, 0.0, 2.2, gain=-12.0, fin=0.004, fout=1.0, hp=25.0)],
              "a rockslide of large granite rocks slowed to 0.9 and muffled at 3.5 kHz, over a depth bomb's boom: "
              "a heavy rumbling collapse, rock on rock, 2.2 s"),
        "b": ([L(77086, 0.0, 2.4, speed=0.8, fin=0.005, fout=0.8, hp=50.0, lp=2500.0),
               L(675899, 3.4, 2.4, gain=-4.0, speed=0.8, fin=0.01, fout=1.0, hp=30.0, lp=1500.0)],
              "a load of kilned clay bricks falling (a brittle, chalky break) slowed to 0.8 and muffled, over a "
              "wall falling down (a vintage film-library recording) slowed and muffled: sharper and more brittle "
              "than a, more like breaking coral, 2.4 s"),
    }),
    "beacon-pulse": ("shot", -16.0, -4.0, -16.0,
                     "Level 24's section 3 (t≈95-135): each relay beacon's 2 s lamp pulse while a beacon is on the "
                     "screen (three beacons, about 6 s each), at a low gain under the music (the 'sonar ping on the "
                     "beat'; DeepWaterCues)",
                     "a deep, far, clean sonar ping: one round low tone with a long soft tail, heard from far off, "
                     "about 1.9 s so pulses 2 s apart do not pile up; not a beep, not a pop", {
        "a": ([L(12677, 0.0, 1.9, speed=0.6, fin=0.02, fout=0.8, hp=100.0, lp=1500.0)],
              "a real 625 Hz active sonar ping (a long-range anti-submarine type) slowed to 0.6 (about 400 Hz) and "
              "muffled at 1.5 kHz: a clean far 'pinnng' with its own water echo, 1.9 s"),
        "b": ([L(616335, 0.0, 1.9, speed=1.25, fin=0.01, fout=1.0, hp=60.0, lp=1500.0)],
              "a large singing bowl's soft-stick hit (a round 156 Hz tone, recorded close) sped to 1.25 (about 195 Hz) and "
              "muffled at 1.5 kHz: lower, "
              "softer and warmer than a, a deep 'bommm' rather than a ping, 1.9 s"),
    }),
    "wreck-hatch": ("shot", -20.0, -4.0, -17.0,
                    "Level 24 at t≈105, once a run (the secret): the wrecked survey sub *Meridian* destroyed (20 HP) "
                    "- its hatch lets go and the hidden crate floats up",
                    "a heavy metal hatch giving way under water: a deep clunk of a breaking latch, then a slow "
                    "creak as it swings open, about 1.5 s, muffled; built, not grown", {
        "a": ([L(438732, 2.45, 1.6, speed=0.85, fin=0.004, fout=0.5, hp=40.0, lp=2500.0),
               L(400793, 0.06, 1.0, gain=-8.0, fin=0.004, fout=0.6, hp=30.0, lp=1200.0)],
              "a real submarine hatch opening (heavy metal clanks and a creak; a vintage film-library recording) "
              "slowed to 0.85 and muffled at 2.5 kHz, on a soft under-water impact: clank, clank and the hatch "
              "free, 1.6 s"),
        "b": ([L(718440, 0.7, 0.8, speed=0.6, fin=0.004, fout=0.5, hp=40.0, lp=2500.0),
               L(496836, 1.65, 1.4, gain=-4.0, at=0.25, speed=0.8, fin=0.1, fout=0.6, hp=40.0, lp=2000.0)],
              "a metal hatch slammed, slowed to 0.6 into a deep clunk, then a heavy bunker door's low hinge groan "
              "slowed to 0.8 and muffled: one big clunk and a long creak, 1.65 s"),
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
