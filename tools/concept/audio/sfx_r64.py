#!/usr/bin/env python3
"""Concept round 64 (M9 part D "Level 31 Mining Rig Escort", step AUD-D): Level 31's new sounds, a and b each
(design/audio/sfx/README.md, *Act 5 sounds*; the Level 31 document's *Music & ambience*; the Shard Drone's, the Buzzsaw
Drone's and the mining tug's sound briefs): the Shard links' hum loop, their flicker tell and a broken link's snap, the
Buzzsaw's saw whine loop, its dash spin-up and its ricochet off a rock, and the tug's hull hit, section alarm, section
loss and engine bed. Round 60's picks are used as they are, not proposed again: the Hornet's launch and missile run (the
shared homing launch homing-launch-r60-b, pitched higher for the Hornet's pods, and the run homing-run-r60-a), the shot-
down crack (homing-down-r60-b), the enemy shield set and the Ascendancy turbine (the Hornet's hover hum, pitched up).

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 64 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, faded,
filtered, looped and levelled; nothing is synthesized. The tools are sfx_r63.py's (copied: preview, build, the --docs
rows).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  link-hum-r64-*       seamless LOOPs (4-6 s): the Shard links while lit, a crackling transformer buzz
  link-flicker-r64-*   ≈ 0.8 s: a link's 0.8 s flicker tell before it lights, a stuttering buzz (cut from the hum's
                       sources)
  link-snap-r64-*      ≈ 0.35-0.45 s: a broken link, a sharp electric snap
  saw-whine-r64-*      seamless LOOPs (2-3 s): the Buzzsaw's spin, a real grinder or saw (the game pitches it)
  saw-spinup-r64-*     ≈ 0.6 s: the Buzzsaw's 0.6 s spin-up tell, the same machine glided up to the loop's pitch
                       (the pick should match the whine's)
  saw-ricochet-r64-*   ≈ 0.5-0.6 s: a dash bouncing off a big rock or the edge, a metal scrape-clang
  tug-hit-r64-*        ≈ 0.7 s: a hit on a tug section, a heavy hull impact with a groan
  tug-alarm-r64-*      ≈ 1.6-2 s: a section below 50 % and lost, an alarm's two blasts
  tug-lost-r64-*       ≈ 1.8 s: a section lost (over the alarm), tearing metal and a fire flare's roar
  tug-engine-r64-*     seamless LOOPs (6-8 s): the tug's engines while they live, a diesel-electric drive rumble

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; a loop by its band RMS): the link hum and the saw whine as the Ascendancy turbine loop, the engine bed a
little over the belt-open bed's band level, the hits and the ricochet a step under the enemy shield's break, the alarm
as round 62's station klaxon, the loss as a `medium` rung's companion.

Usage: python3 tools/concept/audio/sfx_r64.py [subject-or-file ...]   e.g. link-hum tug-hit-r64-b
       python3 tools/concept/audio/sfx_r64.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets and
the loop seams.
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
SCRIPT = "tools/concept/audio/sfx_r64.py"
R = "r64"
ROUND = "round 64"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    19487: ('JacobsLadderSingle2.flac', 'Halleck', BY4, P + "19/19487_6281-hq.ogg"),
    235731: ('TF_Flame.flac', 'copyc4t', BY4, P + "235/235731_1904290-hq.ogg"),
    344592: ('Kicking_Dumpster.wav', 'kmyers1316', CC0, P + "344/344592_6125852-hq.ogg"),
    367453: ('Flash 1,2MV_3.wav', 'jensfelger', CC0, P + "367/367453_6750383-hq.ogg"),
    367456: ("Jacob's ladder.wav", 'jensfelger', CC0, P + "367/367456_6750383-hq.ogg"),
    384528: ('Moscow monorail_Interior.WAV', 'softwalls', CC0, P + "384/384528_6070740-hq.ogg"),
    399450: ('cemeterygate15.aif', 'celadon_noise', CC0, P + "399/399450_1835119-hq.ogg"),
    417553: ('Engine Train Idle', 'Wakerone', CC0, P + "417/417553_7399650-hq.ogg"),
    453333: ('metal crank groan.flac', 'kyles', CC0, P + "453/453333_612689-hq.ogg"),
    462819: ('HUTCH HD SFX 2014 0143', 'leonelmail', CC0, P + "462/462819_4437257-hq.ogg"),
    472688: ('Fire Burst.wav', 'SilverIllusionist', BY4, P + "472/472688_7395592-hq.ogg"),
    474747: ('subway train ride', 'mycompasstv', CC0, P + "474/474747_3902754-hq.ogg"),
    516593: ('9in Angle Grinder', 'jameswrowles', CC0, P + "516/516593_3874107-hq.ogg"),
    520807: ('Engine-Room-door-018.wav', 'Astounded', BY4, P + "520/520807_10201334-hq.ogg"),
    554366: ('Foley_Mechanism_Circular_Saw_Powering_Stereo.wav', 'Nox_Sound', CC0, P + "554/554366_9250976-hq.ogg"),
    559381: ('electric train engine sound effect', 'Garuda1982', BY4, P + "559/559381_2061858-hq.ogg"),
    577494: ('120616 Boat horn, harbour tour, NYC.flac', 'TRP', CC0, P + "577/577494_97550-hq.ogg"),
    641208: ('transformer at night.wav', 'ideaph', CC0, P + "641/641208_1478305-hq.ogg"),
    653294: ('Big Structure Collision Heard from Inside', 'Osvoldon', CC0, P + "653/653294_9808878-hq.ogg"),
    669692: ('designed metal shearing, ripping.wav', 'vestibule-door', CC0, P + "669/669692_14100561-hq.ogg"),
    697758: ('METLImpt_Metal Resonant Scrape Impact_SDFY_Metal Resonant.wav', 'SoundDesignForYou', CC0,
             P + "697/697758_13085623-hq.ogg"),
    720829: ('METLFric-Circular Saw Blade, Scrape on Concrete_02_With Impact_RG_2023_NTG4', 'Riley_Garinger', BY4,
             P + "720/720829_15172097-hq.ogg"),
    736492: ('TTC - Subway Ride, Union to Dundas, Ambience - Toronto, Canada', 'Podel1', BY4,
             P + "736/736492_1251642-hq.ogg"),
    740198: ('Zapping - High Voltage Electricity Buzz Isolated', 'el_boss', CC0, P + "740/740198_9129912-hq.ogg"),
    773336: ('Diesel Locomotive V16 GE Engine Idling (Loop)', 'WeenyBeany', BY4, P + "773/773336_15119557-hq.ogg"),
    832414: ('Factory Warning Buzzer', 'qubodup', CC0, P + "832/832414_71257-hq.ogg"),
}
SYNTH = {}  # nothing synthesized this round (sfx_r63.py's build() reads it)

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


# subject -> (kind, level, ceiling dBFS, cap LUFS, title, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
L31 = "../../campaign/act-5-the-belt/level-31-mining-rig-escort/README.md"
SHARD = "../../enemies/space/shard-drone/README.md"
BUZZ = "../../enemies/air/buzzsaw-drone/README.md"
TUG = "../../allies/mining-tug/README.md"
SERPENT = "../../enemies/air/rail-serpent/README.md"
# the flicker's stutter: (at, length, gain dB) of each burst, 0.8 s in all (the link's flicker tell)
FLICK = [(0.00, 0.05, -8.0), (0.09, 0.04, -6.0), (0.16, 0.07, -5.0), (0.30, 0.05, -4.0), (0.38, 0.09, -3.0),
         (0.53, 0.06, -2.0), (0.62, 0.18, 0.0)]


def flicker(sound, start, hp, lp, other=None):
    """The stutter: FLICK's bursts cut from `sound` (each from a later spot), every other one from `other` if given."""
    out = []
    for k, (at, ln, g) in enumerate(FLICK):
        s = other if other and k % 2 else sound
        last = k == len(FLICK) - 1
        out.append(L(s[0], s[1] + 0.37 * k, ln, gain=g, at=at, fin=0.004, fout=0.03 if last else 0.012, hp=hp, lp=lp))
    return out


SOUNDS = {
    "link-hum": ("loop", -35.0, -1.0, -30.0, "Shard Drone: the links (loop)",
                 "one loop while any Shard link is lit, panned to the nearest group (Level 31's groups of 3-4 drones "
                 "joined by crimson-gold links, sweeping down as barriers and across the tug)",
                 "a steady electrical transformer hum (a 50/60 Hz buzz) that crackles: live energy between the drones, "
                 "under the fire; distinct from the generator brown-out's sagging hum "
                 "(concept/brownout-hum-r60-a.ogg), which can play with it", f"{SHARD}#sounds", {
        "a": (([Loop(367456, 2.0, hp=60.0, lp=7000.0)], 4.0, 0.5),
              "a 50 kV Jacob's ladder: a raw 100 Hz arc buzz with sharp crackles running through it, 4.0 s loop"),
        "b": (([Loop(641208, 20.0, hp=80.0, lp=6000.0), Loop(740198, 3.0, gain=-5.0, hp=120.0, lp=8000.0)], 6.0, 0.6),
              "a pedestal transformer's 240 Hz hum at night under high-voltage lines buzzing in fog: a smoother, "
              "more tonal hum than a with a fine fizzing crackle on top, 6.0 s loop"),
    }),
    "link-flicker": ("shot", -28.0, -8.0, -26.0, "Shard Drone: a link's flicker tell",
                     "a group's links flickering for 0.8 s before they light (0.5 s on hard): after the group's last "
                     "drone enters, and when the links relight after the EMP's stun; then the hum loop takes over",
                     "a stuttering buzz: the links' hum cutting in and out, then holding as they light",
                     f"{SHARD}#behaviour", {
        "a": (flicker((367456, 3.0), 0, 60.0, 7000.0),
              "the Jacob's ladder's buzz (the hum a) cut into seven bursts, longer and louder towards the end: "
              "'bzt . bzt . bzzt .. bzzzzt', 0.8 s"),
        "b": (flicker((641208, 22.0), 0, 80.0, 7000.0, other=(740198, 4.0)),
              "the transformer hum and the power lines' buzz (the hum b) alternating in seven bursts: a hummier, "
              "less crackling stutter than a, 0.8 s"),
    }),
    "link-snap": ("shot", -24.0, -5.0, -22.0, "Shard Drone: a broken link",
                  "a link breaking as one of its two drones dies (a middle drone drops both its links: one snap); "
                  "a few times a group, over the drone's own death (its `small` rung)",
                  "a sharp electric snap: the energy shorting out, dry and short; no pop", f"{SHARD}#sounds", {
        "a": ([L(367453, 0.0, 0.45, fin=0.001, fout=0.3, hp=80.0)],
              "a 1.2 MV flashover's crack, cut short: a big dry 'KSSHAK' with a short rolling tail, 0.45 s"),
        "b": ([L(19487, 0.07, 0.35, fin=0.001, fout=0.2, hp=150.0)],
              "a Jacob's ladder arc striking and buzzing out: a thinner, brighter 'TSSK-zzz' than a, 0.35 s"),
    }),
    "saw-whine": ("loop", -35.0, -1.0, -30.0, "Buzzsaw Drone: the spin (loop)",
                  "one loop while a Buzzsaw Drone is on the screen, for the nearest; its pitch follows the spin (the "
                  "game pitches it: 2 turns/s chasing, up to 6 when spun up)",
                  "a real circular saw or angle grinder's whine, steady, under the fire", f"{BUZZ}#behaviour", {
        "a": (([Loop(516593, 6.0, hp=80.0)], 3.0, 0.5),
              "a 9-inch angle grinder running free: a rough, heavy whine with a gritty motor under it, 3.0 s loop"),
        "b": (([Loop(554366, 3.0, speed=0.85, hp=100.0, lp=7000.0)], 2.0, 0.4),
              "a circular saw running free, slowed to 0.85: a thinner, shriller and airier whine than a, 2.0 s loop"),
    }),
    "saw-spinup": ("shot", -26.0, -6.0, -24.0, "Buzzsaw Drone: the dash spin-up",
                   "each Buzzsaw's 0.6 s spin-up tell before a dash (0.8 s on easy, 0.5 s on hard; the blades speed up "
                   "and the gold rim flickers red); a group staggers its tells 0.4 s apart",
                   "the saw's whine rising sharply to a scream: the machine spinning up, ending on the loop's pitch "
                   "(the a/b should match the whine's)", f"{BUZZ}#behaviour", {
        "a": ([L(516593, 6.5, 0.6, speed=(0.35, 1.0), fin=0.02, fout=0.03, hp=80.0, swell=(-14.0, 1.0))],
              "the angle grinder (the whine a) glided from 0.35 to 1.0 speed and swelling: a rough 'wrrrRRIIII' "
              "rising to its run, 0.6 s"),
        "b": ([L(554366, 3.5, 0.6, speed=(0.3, 0.85), fin=0.02, fout=0.03, hp=100.0, lp=7000.0, swell=(-14.0, 1.0))],
              "the circular saw (the whine b) glided from 0.3 to 0.85 speed and swelling: a thinner 'wheeeEEE' "
              "rising to a shriek, 0.6 s"),
    }),
    "saw-ricochet": ("shot", -27.0, -6.0, -25.0, "Buzzsaw Drone: a ricochet",
                     "a Buzzsaw's dash bouncing off a big drifting rock or the field's edge (up to 3 bounces a dash, 2 "
                     "on easy; small rocks are smashed instead and play their own break)",
                     "a metal scrape-clang: the spinning blade striking stone and skidding off", f"{BUZZ}#behaviour", {
        "a": ([L(720829, 1.23, 0.55, speed=0.85, fin=0.001, fout=0.15, hp=150.0, lp=8000.0)],
              "a circular saw blade slammed on concrete and skidding off, slowed to 0.85: a hard 'KLANG-shhk', 0.55 s"),
        "b": ([L(697758, 0.62, 0.6, speed=1.2, fin=0.01, fout=0.25, hp=100.0)],
              "a resonant metal scrape into a heavy hit, sped to 1.2: a scrape first, then a deeper, ringing "
              "'skrr-DONG' than a, 0.6 s"),
    }),
    "tug-hit": ("shot", -25.0, -6.0, -23.0, "The mining tug: a hull hit",
                "each hit on one of the tug's three sections (a Hornet missile bursting on it, a Dart diving it, a "
                "Shard link dragged across it), throttled to once per 0.2 s; the HUD's section bar flashes white",
                "a heavy hull impact: metal struck hard with a groan, big and hollow (a ship's hull, not a tin can)",
                f"{TUG}#design", {
        "a": ([L(462819, 0.15, 0.7, speed=0.9, fin=0.002, fout=0.35, hp=40.0),
               L(453333, 0.8, 0.55, gain=-7.0, at=0.1, speed=0.6, fin=0.05, fout=0.3, hp=100.0)],
              "a deep, hollow metal impact (slowed to 0.9) with a crank's metal groan slowed to 0.6 under its tail: "
              "a booming 'DOOMM-mmrrr', 0.7 s"),
        "b": ([L(344592, 0.64, 0.6, speed=0.7, fin=0.002, fout=0.3, hp=50.0),
               L(399450, 0.0, 0.6, gain=-8.0, at=0.1, speed=0.55, fin=0.06, fout=0.3, hp=100.0)],
              "a kicked steel dumpster slowed to 0.7 with an iron gate's hinge groan slowed to 0.55: a harder, "
              "clangier 'KLONGG' than a with a creaking groan, 0.7 s"),
    }),
    "tug-alarm": ("shot", -22.0, -6.0, -20.0, "The mining tug: the section alarm",
                  "a tug section falling below 50 % (it starts to smoke) and again when it is lost (with the loss "
                  "below): up to six times a level; over the music, with the radio's `first-ally-hit` / `ally-lost` "
                  "line",
                  "a klaxon burst: a ship's alarm, two blasts; distinct from Level 29's station klaxon "
                  "(concept/station-klaxon-r62-a.ogg). The relay's chosen integrity alarm (concept/integrity-alarm-"
                  "r35-a.ogg, the same 'an ally is hurt' event, kept in round 47) is the third option",
                  f"{TUG}#design", {
        "a": ([L(832414, 0.0, 0.75, fin=0.005, fout=0.15, hp=150.0),
               L(832414, 0.0, 1.2, at=0.9, fin=0.005, fout=0.6, hp=150.0)],
              "a bridge crane's warning buzzer, twice: two harsh electric 'BRAAAP's 0.9 s apart, the second ringing "
              "out, 2.1 s"),
        "b": ([L(577494, 0.8, 0.6, fin=0.01, fout=0.12, hp=120.0),
               L(577494, 0.8, 0.85, at=0.8, fin=0.01, fout=0.35, hp=120.0)],
              "a tour boat's horn heard on board, cut into two blasts: two deep 215 Hz 'HOOONK's, lower and rounder "
              "than a, 1.65 s"),
    }),
    "tug-lost": ("shot", -21.0, -4.0, -19.0, "The mining tug: a section lost",
                 "a tug section lost (its lights go out, a short fire flare, then it vents): up to three times a "
                 "level, with the alarm above; the third loss breaks the hull and fails the escort",
                 "a structural failure: tearing metal and a fire flare's roar; heavy, no pop", f"{TUG}#design", {
        "a": ([L(653294, 0.0, 1.8, fin=0.002, fout=0.8, hp=30.0),
               L(669692, 1.28, 0.9, gain=-4.0, at=0.05, speed=0.8, fin=0.01, fout=0.4, hp=100.0, lp=6000.0),
               L(472688, 0.02, 0.9, gain=-5.0, at=0.25, speed=0.8, fin=0.01, fout=0.4, hp=60.0)],
              "a massive structure's collision heard from inside, metal shearing (slowed to 0.8) and a burst of fire "
              "(slowed to 0.8): a deep 'BOOM' with a rip and a flaring whoosh, 1.8 s"),
        "b": ([L(344592, 0.64, 0.6, gain=-3.0, speed=0.55, fin=0.002, fout=0.3, hp=40.0),
               L(669692, 2.28, 1.3, at=0.05, speed=0.7, fin=0.01, fout=0.5, hp=100.0, lp=5000.0),
               L(235731, 0.0, 1.5, gain=-3.0, at=0.2, fin=0.1, fout=0.7, hp=30.0)],
              "a steel dumpster kicked (slowed to 0.55), a longer metal rip (slowed to 0.7) and a flamethrower's "
              "roar: more tearing and a lower, longer roar than a, a smaller impact, 1.8 s"),
    }),
    "tug-engine": ("loop", -38.0, -1.0, -30.0, "The mining tug: the engine bed (loop)",
                   "Level 31: a layer over the `belt-open` bed while the tug's engines section lives (it stops when "
                   "the engines are lost), from the tug's rise into its station until its climb-out",
                   "a huge mining machine's low drive rumble, a diesel-electric thrum heard through the hull; "
                   "seamless, steady, under everything", f"{TUG}#design", {
        "a": (([Loop(417553, 5.0, speed=0.8, hp=30.0, lp=3000.0)], 8.0, 1.0),
              "a diesel locomotive idling in a station, slowed to 0.8: a deep throbbing diesel drone, 8.0 s loop"),
        "b": (([Loop(773336, 0.5, speed=0.6, hp=30.0, lp=4000.0),
                Loop(520807, 10.0, gain=-8.0, speed=0.6, hp=60.0, lp=4000.0)], 6.0, 0.8),
              "a GE V16 diesel-electric locomotive idling, slowed to 0.6, over a contact microphone on an engine-room "
              "door: a smoother, more humming thrum than a with a machine's whine in the hull, 6.0 s loop"),
    }),
    "serpent-rumble": ("loop", -34.0, -1.0, -29.0, "Rail Serpent: the train's rumble (redo of round 63 S.5)",
                       "one loop while a Rail Serpent is on the screen, for the nearest (the Act 3 rule: once per "
                       "kind); Level 30 and later Act 5 levels",
                       "a heavy rail car's rumble with a rhythmic coupling clatter (a real train), under the fire, "
                       "seamless; this redo is to carry the engine's electric whine, which neither round-63 loop had", f"{SERPENT}#behaviour", {
        "a": (([Loop(559381, 12.0, hp=150.0, lp=6000.0, gain=-4.0),
                Loop(736492, 14.0, hp=40.0, lp=5000.0)], 6.0, 0.8),
              "an electric locomotive's traction-motor whine (a steady 2 kHz tone with its 12 Hz flutter) over a subway "
              "train's steady wheel clatter on the rail joints (about 2.5 clacks a second): a train felt as a machine, "
              "closer and more regular than the round-63 a, 6.0 s loop"),
        "b": (([Loop(384528, 100.0, speed=0.8, hp=70.0, lp=1200.0),
                Loop(736492, 80.0, gain=-3.0, hp=120.0, lp=900.0)], 6.0, 0.8),
              "a monorail car's low electric drone (slowed to 0.8) with the soft thumps of a subway car over the rail "
              "joints, low-passed to 900 Hz: a hum and a faint rhythmic thud, no clatter and no whine, 6.0 s loop"),
    }),
}


def layers_of(kind, spec):
    if kind == "synth":
        return []
    return spec[0] if kind == "loop" else spec


def sources_of(kind, spec):
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec)))


def build(subject, v):
    kind, band, ceiling, cap = SOUNDS[subject][:4]
    spec, what = SOUNDS[subject][8][v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec)) or "synthesized"
    if kind == "loop":
        st = loop(*spec)
        st = st - st.mean(axis=1, keepdims=True)
        # the loop's start moved (within its first 200 ms) to where both channels are nearest zero and flattest
        w = st[:, :int(0.2 * SR)]
        k = int(np.argmin(np.abs(w).sum(axis=0) + 20.0 * np.abs(np.gradient(w, axis=1)).sum(axis=0)))
        st = np.roll(st, -k, axis=1)
        st = st * undb(band - band_rms_db(st))
        loud = lufs(st)[0]
        if loud > cap:
            st = st * undb(cap - loud)
    else:
        raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
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


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 64 ----

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
    if layer["hp"]:
        parts.append(f"{num(layer['hp'])} Hz high-pass")
    if layer["lp"]:
        parts.append(f"{num(layer['lp'])} Hz low-pass")
    if one_shot:
        parts.append(f"{num(layer['fin'])} s fade-in, {num(layer['fout'])} s fade-out")
        if layer["at"]:
            parts.append(f"placed at {num(layer['at'])} s")
    else:
        parts.append(f"made a seamless {num(spec[1])} s loop (its last {num(spec[2])} s cross-faded into the start)")
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
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        kind, _, _, _, _, use, should, _, variants = SOUNDS[subject]
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
        info[(subject, v)] = (secs, what)
        size = f"{secs:.2f} s{' loop' if kind == 'loop' else ''} ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        edit = (f"{secs:.2f} s, {'band RMS' if kind == 'loop' else 'the loudest 100 ms band'} {lv:.1f} dB, peak "
                f"{db(np.abs(x).max()):.1f} dBFS, {lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, heavy, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-64/: the same depth as design/audio/sfx)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        kind, _, _, _, title, use, should, part, variants = SOUNDS[subject]
        what = " ".join(f"**{v}** ({info[(subject, v)][0]:.2f} s): {info[(subject, v)][1]}." for v in variants
                        if (subject, v) in info)
        files = ", ".join(f"[{v}](../../audio/sfx/concept/{subject}-{R}-{v}.ogg)" for v in variants)
        rnd.append(f"| S.{k} | [{title} (`{subject}`)]({part}) | **pick a or b**. **Plays:** {use}. **Should sound "
                   f"like:** {should}. {what} | {files} | | **a** / b / redo | |")
    (outdir / "credits.md").write_text("\n".join(credits) + "\n")
    (outdir / "rows.md").write_text("\n".join(rows) + "\n")
    (outdir / "prompts.md").write_text("\n".join(prompts))
    (outdir / "round.md").write_text("\n".join(rnd) + "\n")
    print(f"docs: {len(credits)} CREDITS rows, {len(rows)} README rows, {len(prompts)} prompts entries, {len(rnd)} "
          f"round rows in {outdir}")


def main(argv):
    outdir = None
    if "--docs" in argv:
        i = argv.index("--docs")
        outdir = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    made = []
    for subject, spec in SOUNDS.items():
        for v in spec[8]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
