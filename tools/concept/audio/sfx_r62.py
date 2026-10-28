#!/usr/bin/env python3
"""Concept round 62 (M9 part B "Level 29 Betrayal at Ceres + the Act 5 intro", step AUD-B): Level 29's new sounds, a
and b each (design/audio/sfx/README.md, *Act 5 sounds*; the Level 29 document's *Music & ambience*; the Talon's, the
SAM Nest's and the Asteroid Mite's sound briefs): the turning station's klaxon (the gap check's QB-3: a = a slow
two-tone horn, b = a fast electronic whoop) and its heavy relays, the SAM Nest's hatch, lock beep and wake, Dock 9's
clamp gun shot and clamp release, Rook's Ember launching from the tender's hold, the Bulwark's departure, the Talon's
turn-in whine, twin crack and the leader's squelch, the Asteroid Mite's tell and leap.

Every recorded layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 62 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, sped or slowed (or glided), layered, swelled,
faded, filtered and levelled. Synthesized: the SAM Nest's lock beep (the README allows a synthesized two-tone beep: a
signal, not a machine), also the beep at the end of the nest's wake. The tools are sfx_r60.py's (copied: preview,
build, the --docs rows) plus synthesized layers inside a mix and the round-62 row form.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  station-klaxon-r62-*     ≈ 4-5 s: three blasts of the station's alarm (Level 29, t=60)
  station-relays-r62-*     ≈ 1.5 s: a bank of heavy relays slamming one after another (the lights going red)
  nest-hatch-r62-*         ≈ 0.5 s: the SAM Nest's hatch, a short hydraulic clunk
  nest-lock-r62-*          0.4 s: the SAM Nest's two-tone lock beep (synthesized)
  nest-wake-r62-*          1.0 s: a dormant SAM Nest waking: a servo spin-up and the lock beep (a, b, and `ab`, the pick:
                           a's servo with b's lock beep)
  clamp-gun-shot-r62-*     ≈ 0.5 s: a clamp gun's aimed single shot
  clamp-release-r62-*      ≈ 1.2 s: a docking clamp springing open: a hydraulic release, a gas burst, a clang
  ember-launch-r62-*       ≈ 2.5 s: Rook's Ember: a jet spooling up into a launch roar
  bulwark-depart-r62-*     ≈ 2 s: the loaner Bulwark peeling away: a thruster burn going away
  talon-whine-r62-*        ≈ 0.6 s: a Talon's turn-in: a short jet whine
  talon-shot-r62-*         ≈ 0.3 s: a Talon pair's twin shot: a dry doubled crack
  leader-squelch-r62-*     ≈ 0.4 s: a Talon leader's death: a short radio squelch (over the `small` rung)
  mite-tell-r62-*          0.4 s: an Asteroid Mite's leap tell: a short gritty rock scrape
  mite-leap-r62-*          ≈ 0.4 s: the Mite's leap: a scuttle and a quick whoosh

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap): the klaxon, relays, release and launch as set-piece events, the clamp gun and the Talon's shot as the enemy
shot small, the Mite quiet (heard only close).

Usage: python3 tools/concept/audio/sfx_r62.py [subject-or-file ...]   e.g. talon-shot nest-lock-r62-b
       python3 tools/concept/audio/sfx_r62.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows, the prompts.md entries and the round rows
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks, onsets and
pitch tracks.
"""
import sys
import urllib.request
from pathlib import Path

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r43g  # noqa: E402
from sfx_r24 import level, short_term_band_db  # noqa: E402
from sfx_r32 import report  # noqa: E402
from sfx_r43g import cut  # noqa: E402
from sfx_r44 import loudness  # noqa: E402
from synth import SR, db, decode, lufs, svf, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r62.py"
R = "r62"
ROUND = "round 62"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    47646: ('End radio transmission', 'ReadeOnly', CC0, P + "47/47646_545448-hq.ogg"),
    75543: ('swish Bamboo pole w insect net 11.wav', 'ra_gun', CC0, P + "75/75543_94000-hq.ogg"),
    139970: ('Switch.Big.Power.wav', 'jessepash', CC0, P + "139/139970_968325-hq.ogg"),
    162258: ('Mk 46 30mm gun - single shot', 'qubodup', CC0, P + "162/162258_71257-hq.ogg"),
    174869: ('Aurora Colorado Whelen WPS-3016 Test Part Two: WHOOP Loop. READ DESCRIPTION.', 'theblockofsound235',
             CC0, P + "174/174869_2377160-hq.ogg"),
    364709: ('Rock on Rock Scrape', 'alegemaate', CC0, P + "364/364709_2531187-hq.ogg"),
    388568: ('Train Air Brake 01.wav', 'totalcult', CC0, P + "388/388568_14360-hq.ogg"),
    402500: ('door, open, space hatch, hydraulic.wav', 'pointparkcinema', CC0, P + "402/402500_7237186-hq.ogg"),
    413312: ('Afterburner', 'TiesWijnen', CC0, P + "413/413312_1511977-hq.ogg"),
    418881: ('Tank Turret Rotate', 'KieranKeegan', CC0, P + "418/418881_6616210-hq.ogg"),
    423108: ('Guns & Explosions Album - Autocannon - Shot 1.wav', 'OGsoundFX', BY4, P + "423/423108_3325582-hq.ogg"),
    437890: ('G08-27-Jet Idle or Taxi.wav', 'craigsmith', CC0, P + "437/437890_2524442-hq.ogg"),
    457294: ('Air (or steam) pressure release', 'brunoboselli', CC0, P + "457/457294_300738-hq.ogg"),
    464894: ('Gamewell Diaphone fire station horn', 'HootOwl', CC0, P + "464/464894_740212-hq.ogg"),
    524908: ('Starter_75hp.wav', 'Engineer_815', CC0, P + "524/524908_10345385-hq.ogg"),
    541015: ('hydraulic shears breaks concrete 001.wav', 'klankbeeld', BY4, P + "541/541015_1648170-hq.ogg"),
    640499: ('Eurofighter Typhoon Flyby 003 – Close Proximity', 'TimoSchmied', BY4, P + "640/640499_4424897-hq.ogg"),
    640503: ('Eurofighter Typhoon Flyby 007 – Close Proximity', 'TimoSchmied', BY4, P + "640/640503_4424897-hq.ogg"),
    667283: ('Stone Scrape 2', 'alegemaate', CC0, P + "667/667283_2531187-hq.ogg"),
    683101: ('quick woosh', 'florianreichelt', CC0, P + "683/683101_6253486-hq.ogg"),
    718440: ('Metal_Hatch_Small_Close_01', 'Rudmer_Rotteveel', CC0, P + "718/718440_4921277-hq.ogg"),
    752078: ('heavy_metal_hit_w_loud_ringout_noises_01_08242024', 'Artninja', BY4, P + "752/752078_10996917-hq.ogg"),
    760245: ('Walkie-talkie end of transmission', 'LukaCafuka', CC0, P + "760/760245_16236894-hq.ogg"),
    761559: ('crab walking', 'stuniverso', CC0, P + "761/761559_16036556-hq.ogg"),
    764849: ('motorized_door_servo_sound_11062024', 'Artninja', BY4, P + "764/764849_10996917-hq.ogg"),
    788132: ('SDL Jet Engine Spool Up', 'guitarduane', BY4, P + "788/788132_16872537-hq.ogg"),
    805169: ('Jet Flyby 02', 'mrspivey', CC0, P + "805/805169_17325486-hq.ogg"),
    807384: ('Contactor/motor overload relay noises', 'tt_runscript', CC0, P + "807/807384_5365416-hq.ogg"),
    814976: ('Closing large metal door-001', 'AudioPapkin', CC0, P + "814/814976_8698658-hq.ogg"),
    828623: ('bug legs tapping', 'c64cosmin', CC0, P + "828/828623_7834773-hq.ogg"),
    854183: ('Autocannon Shot 1', 'qubodup', CC0, P + "854/854183_71257-hq.ogg"),
    854184: ('Autocannon Shot 2', 'qubodup', CC0, P + "854/854184_71257-hq.ogg"),
}


# ---- the synthesized lock beep (a signal: the SAM Nest's README allows a synthesized two-tone beep)

def _tone(freq, length, square=0.5):
    """A short beep: a sine with some square in it (a hard electronic edge), 3 ms attack and release."""
    t = np.arange(int(length * SR)) / SR
    s = np.sin(2 * np.pi * freq * t)
    x = (1 - square) * s + square * np.tanh(4 * s)
    env = np.minimum(1.0, t / 0.003) * np.minimum(1.0, (length - t) / 0.003)
    return x * env


def lock_beep(v):
    """a: 'bip-beeep', a low tone then a held high one a fifth up (1.5 kHz 0.11 s, a 40 ms gap, 2.25 kHz 0.2 s);
    b: 'bidi-bidi', two quick high-low pairs (2.2 / 1.65 kHz, 60 ms each, no gap; 40 ms between the pairs)."""
    x = np.zeros(int(0.42 * SR))
    if v == "a":
        parts = [(0.01, 1500.0, 0.11), (0.16, 2250.0, 0.2)]
    else:
        parts = [(0.01, 2200.0, 0.06), (0.07, 1650.0, 0.06), (0.17, 2200.0, 0.06), (0.23, 1650.0, 0.06)]
    for at, f, ln in parts:
        b = _tone(f, ln)
        a = int(at * SR)
        x[a:a + len(b)] += b
    x = svf(x, 5000.0, q=0.6)
    return np.vstack([x, x])


# synthesized layers a mix can use like a recording (key -> stereo signal)
SYNTH_SOURCES = {"lock-a": lambda: lock_beep("a"), "lock-b": lambda: lock_beep("b")}
SYNTH = {"nest-lock": lock_beep}

_decoded = {}


def preview(sound):
    """The decoded HQ preview, fetched from the CDN into the cache when missing (or a synthesized layer)."""
    if sound not in _decoded:
        if isinstance(sound, str):
            _decoded[sound] = SYNTH_SOURCES[sound]()
            return _decoded[sound]
        path = CACHE / f"{sound}-hq.ogg"
        if not path.exists():
            CACHE.mkdir(parents=True, exist_ok=True)
            with urllib.request.urlopen(SOURCES[sound][3], timeout=120) as r:
                path.write_bytes(r.read())
        _decoded[sound] = decode(path)
    return _decoded[sound]


sfx_r43g.source = preview  # sfx_r43g.cut() reads its layers through this
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like, part link,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "synth": None (SYNTH[subject](v)), levelled as a shot.
L29 = "../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md"
TALON = "../../enemies/air/talon/README.md"
NEST = "../../enemies/ground/sam-nest/README.md"
MITE = "../../enemies/space/asteroid-mite/README.md"
RELAY_HITS = [(0.00, 1.00, 0.0), (0.21, 0.92, -2.0), (0.39, 1.06, -1.0), (0.56, 0.88, -3.0), (0.73, 0.97, -2.0),
              (0.90, 0.85, -1.5)]  # (at s, speed, gain dB) of the six slams
CONTACTORS = [34.40, 49.94, 66.745, 77.565, 86.12, 101.27]  # onsets of six contactor slams in freesound 807384
SOUNDS = {
    "station-klaxon": ("shot", -22.0, -6.0, -20.0,
                       "Level 29, once at t=60: Block C declares for the Ascendancy, its lights switching white to red "
                       "over 1 s and the dormant SAM Nests waking; the music turns to the full mix with Vorne's motif "
                       "(the gap check's QB-3 asks for the register: a or b)",
                       "a reverberant station klaxon, three blasts: the station turning on the player, industrial, "
                       "heard over the music; no bubbly or popping sounds",
                       f"{L29}#music--ambience", {
        "a": ([L(464894, 0.92, 4.7, fin=0.01, fout=0.9, hp=150.0)],
              "a fire station's Gamewell diaphone air horn: three slow two-tone blasts ('bee-OHH', a low grunt "
              "rising into a big brassy tone), each 1.4 s with its echo off the buildings, 4.7 s"),
        "b": ([L(174869, 0.0, 1.25, speed=2.0, fin=0.01, fout=0.15, hp=200.0),
               L(174869, 0.0, 1.25, at=1.3, speed=2.0, fin=0.01, fout=0.15, hp=200.0),
               L(174869, 0.0, 1.25, at=2.6, speed=2.0, fin=0.01, fout=0.4, hp=200.0),
               L(174869, 0.0, 1.25, gain=-12.0, at=2.72, speed=2.0, fin=0.05, fout=0.6, hp=200.0, lp=2500.0)],
              "a real Whelen electronic siren's 'whoop' sped to double: three fast rising whoops (650 Hz up to "
              "1.4 kHz in 1.25 s each), the last with a dull echo, 4.0 s; harder and more urgent than a"),
    }),
    "station-relays": ("shot", -22.0, -5.0, -21.0,
                       "Level 29, t=60, with the klaxon: the block's lights switching white to red over 1 s (and "
                       "the red running up the canyon after it)",
                       "a bank of heavy relays slamming one after another over about 1 s: big power switches, "
                       "mechanical and heavy, no electronic tone", f"{L29}#music--ambience", {
        "a": ([L(139970, 1.33, 0.75, gain=g, at=at, speed=sp, fin=0.002, fout=0.4, hp=80.0)
               for at, sp, g in RELAY_HITS],
              "a 50 A three-phase breaker thrown in a concrete metal shop, six times over 0.9 s at slightly "
              "different pitches: six heavy 'KLUNK's with the hall's ring, 1.6 s"),
        "b": ([L(524908, 1.0, 0.5, gain=-3.0, at=0.0, speed=0.9, fin=0.002, fout=0.3, hp=60.0)]
              + [L(807384, t, 0.3, gain=-1.0 - (k % 3), at=0.08 + 0.17 * k, speed=0.7, fin=0.002, fout=0.18,
                   hp=60.0) for k, t in enumerate(CONTACTORS)],
              "a 75 hp motor starter's contactor pulling in, then six motor contactors slammed in turn, slowed to "
              "0.7 for weight: drier and quicker clacks than a, with an electric thud, 1.3 s"),
    }),
    "nest-hatch": ("shot", -25.0, -6.0, -24.0,
                   "each SAM Nest salvo: the hatch opening at the start of its 0.4 s lock (the lock beep plays with "
                   "it); up to three nests firing at once in Levels 29-35",
                   "a short hydraulic clunk: a hatch thrown open by a ram, dry, about 0.5 s", f"{NEST}#sounds", {
        "a": ([L(718440, 0.76, 0.5, fin=0.002, fout=0.3, hp=120.0),
               L(402500, 0.08, 0.35, gain=-7.0, fin=0.01, fout=0.2, hp=600.0)],
              "an old heater's small metal hatch slammed, a hydraulic hatch's hiss under it: a hissing 'chunk', "
              "0.5 s"),
        "b": ([L(541015, 0.0, 0.45, fin=0.002, fout=0.25, hp=80.0, lp=4000.0),
               L(718440, 0.76, 0.4, gain=-5.0, at=0.02, speed=0.8, fin=0.002, fout=0.25, hp=120.0)],
              "an excavator's hydraulic shears biting (the ram's groan and crunch) with the hatch's clunk slowed "
              "under it: heavier and more of a machine than a, 0.47 s"),
    }),
    "nest-lock": ("synth", -27.0, -8.0, -26.0,
                  "each SAM Nest salvo: the 0.4 s lock after the hatch opens, before the two missiles launch; also "
                  "at the end of a dormant nest's wake",
                  "a two-tone electronic lock beep (synthesized allowed: a signal, not a machine); dry, short, no "
                  "melody", f"{NEST}#sounds", {
        "a": (None, "a low beep and a held high one a fifth up (1.5 kHz, then 2.25 kHz): 'bip-beeep', 0.4 s"),
        "b": (None, "two quick high-low pairs (2.2 and 1.65 kHz, 60 ms each): 'bidi-bidi', 0.3 s; busier than a"),
    }),
    "nest-wake": ("shot", -25.0, -6.0, -24.0,
                  "Level 29, t=60: each dormant SAM Nest on Block C waking (its 1 s tell: the rack lifts and lights "
                  "red); three nests at once, so it plays once for the group",
                  "a servo spin-up as the rack lifts, ending on the lock beep", f"{NEST}#behaviour", {
        "a": ([L(764849, 0.2, 0.8, speed=(0.8, 1.25), fin=0.05, fout=0.12, hp=100.0),
               L("lock-a", 0.0, 0.4, gain=-5.0, at=0.66, fin=0.002, fout=0.05, hp=30.0)],
              "a motorised door's servo gliding up from 0.8 to 1.25 speed (a whirring rise), then the lock beep a: "
              "'whirrr - bip-beeep', 1.06 s"),
        # round 62's pick (S.5 a with S.4 b): the wake ends on the lock beep, so a's servo gets b's beep
        "ab": ([L(764849, 0.2, 0.8, speed=(0.8, 1.25), fin=0.05, fout=0.12, hp=100.0),
                L("lock-b", 0.0, 0.32, gain=-5.0, at=0.66, fin=0.002, fout=0.05, hp=30.0)],
               "a's motorised door servo gliding up from 0.8 to 1.25 speed (a whirring rise), then b's lock beep: "
               "'whirrr - bidi-bidi', about 1.0 s"),
        "b": ([L(418881, 0.0, 0.8, speed=(0.7, 1.15), fin=0.05, fout=0.12, hp=100.0),
               L("lock-b", 0.0, 0.32, gain=-5.0, at=0.66, fin=0.002, fout=0.05, hp=30.0)],
              "a geared motor (recorded from a printer) gliding up from 0.7 to 1.15 speed: a grittier, toothier "
              "whine than a, then the lock beep b: 'grrrr - bidi-bidi', 1.0 s"),
    }),
    "clamp-gun-shot": ("shot", -27.0, -6.0, -22.0,
                       "Level 29's Dock 9 halt: each clamp gun's aimed single shot (four guns, every 2.0 s; 2.6 s on "
                       "easy), the enemy shot small's bullets",
                       "a turret's single cannon shot, short and dry; the enemy shot small's weight "
                       "(concept/enemy-shot-small-r08-a.ogg plays if neither is picked)", f"{L29}#ground-targets", {
        "a": ([L(854184, 0.0, 0.45, fin=0.001, fout=0.25, hp=150.0)],
              "a 25 mm autocannon's single shot (a LAV-25's Bushmaster): a hard dry 'BAK' with a short tail, 0.45 s"),
        "b": ([L(423108, 0.02, 0.55, fin=0.001, fout=0.3, hp=100.0)],
              "a heavy autocannon's single shot: deeper and boomier than a, 0.55 s"),
    }),
    "clamp-release": ("shot", -21.0, -5.0, -20.0,
                      "Level 29's Dock 9 halt: each clamp gun destroyed, its docking clamp springing open with a "
                      "burst of gas and sparks (four times); the gun's own death plays its `medium` rung",
                      "a heavy hydraulic release with a gas burst and a clang: a big clamp letting go", f"{L29}#ground-targets", {
        "a": ([L(752078, 0.1, 1.2, fin=0.002, fout=0.8, hp=60.0),
               L(388568, 0.05, 1.0, gain=-3.0, at=0.03, fin=0.01, fout=0.6, hp=200.0),
               L(541015, 0.0, 0.6, gain=-8.0, fin=0.005, fout=0.3, hp=60.0, lp=2000.0)],
              "a heavy metal hit ringing out, a train's air brake venting over it and an excavator's hydraulic "
              "ram groaning under: 'KLANG-psshhh', 1.2 s"),
        "b": ([L(814976, 0.0, 1.2, fin=0.002, fout=0.7, hp=60.0),
               L(457294, 6.62, 0.9, gain=-2.0, at=0.04, fin=0.004, fout=0.5, hp=250.0),
               L(402500, 0.08, 0.5, gain=-8.0, fin=0.01, fout=0.3, hp=300.0)],
              "a large metal door slammed (a clang with its latch rattling), an air valve blowing open and a "
              "hydraulic hatch's hiss: more of a rattle and a longer gas burst than a, 1.2 s"),
    }),
    "ember-launch": ("shot", -21.0, -5.0, -19.0,
                     "Level 29, once: the last clamp gun falls, the prison tender's hold opens and Rook's Ember "
                     "launches out of it (with Rook's return sting, track 31, over the music)",
                     "a jet engine spooling up into a launch whoosh, about 2.5 s: a fighter lighting up and going",
                     f"{L29}#objective", {
        "a": ([L(788132, 3.5, 1.4, speed=(1.2, 2.0), fin=0.3, fout=0.25, hp=150.0, swell=(-10.0, 1.0)),
               L(413312, 0.0, 1.4, gain=-1.0, at=1.15, fin=0.02, fout=0.9, hp=60.0)],
              "a jet engine's spool-up at an airport, its whine rising (sped from 1.2 to 2.0 and swelling), into a "
              "turbojet's afterburner lighting: 'wheeeeee-WHOOSH', 2.55 s"),
        "b": ([L(437890, 2.0, 1.3, speed=(0.8, 1.8), fin=0.3, fout=0.3, hp=100.0, swell=(-12.0, 1.0)),
               L(640499, 3.1, 1.4, gain=-1.0, at=1.0, fin=0.05, fout=0.8, hp=80.0)],
              "a jet idling and taxiing, sped from 0.8 to 1.8 into a rising roar, then a Eurofighter's close pass: "
              "rougher spool and a bigger, more airborne roar than a, 2.4 s"),
    }),
    "bulwark-depart": ("shot", -25.0, -6.0, -23.0,
                       "Level 29, once, right after the Ember's launch: the loaner Bulwark drone in the escort slot "
                       "peels away to its side's edge ('Bulwark, RTB') as Rook takes the slot",
                       "a thruster burn going away: a short roar fading off to the side, about 2 s",
                       f"{L29}#objective", {
        "a": ([L(640503, 4.4, 2.0, fin=0.05, fout=1.1, hp=80.0)],
              "a Eurofighter's close flyby from just after its peak: the roar falling away into the distance, 2.0 s"),
        "b": ([L(413312, 0.0, 0.35, gain=-6.0, fin=0.01, fout=0.2, hp=100.0),
               L(805169, 2.9, 2.0, at=0.05, fin=0.05, fout=1.2, hp=80.0)],
              "an afterburner's light-up, then a jet flyby going away: a sharper kick at the start and a thinner, "
              "whinier tail than a, 2.05 s"),
    }),
    "talon-whine": ("shot", -28.0, -8.0, -26.0,
                    "each Talon wing's swoop turning in (a V of 5, or a pair): often in Levels 29-35, throttled to "
                    "one per wing",
                    "a short jet whine on the turn-in, quiet, under the fire", f"{TALON}#behaviour", {
        "a": ([L(640499, 3.0, 0.6, speed=1.2, fin=0.15, fout=0.25, hp=600.0)],
              "a Eurofighter's approach just before its pass, sped to 1.2 and high-passed to its whine: a thin "
              "rising 'wheee', 0.6 s"),
        "b": ([L(805169, 2.4, 0.6, speed=(1.0, 1.35), fin=0.12, fout=0.25, hp=500.0)],
              "a jet flyby's approach gliding up from 1.0 to 1.35 speed: a whine bending up into the turn, more "
              "pitch movement than a, 0.6 s"),
    }),
    "talon-shot": ("shot", -28.0, -6.0, -24.0,
                   "each Talon pair's twin shot (two shots 12 px apart, heard as one): often, Levels 29-35",
                   "a dry twin crack: a real cannon, two shots heard as one doubled crack; the enemy shot small's "
                   "weight", f"{TALON}#behaviour", {
        "a": ([L(854183, 0.0, 0.28, speed=1.25, fin=0.001, fout=0.15, hp=300.0),
               L(854183, 0.0, 0.28, gain=-2.0, at=0.045, speed=1.32, fin=0.001, fout=0.15, hp=300.0)],
              "a 25 mm autocannon's shot sped to 1.25, twice 45 ms apart: a tight doubled 'ka-RAK', 0.33 s"),
        "b": ([L(162258, 0.0, 0.3, speed=1.4, fin=0.001, fout=0.15, hp=250.0),
               L(162258, 0.0, 0.3, gain=-2.0, at=0.05, speed=1.5, fin=0.001, fout=0.15, hp=250.0)],
              "a 30 mm gun's single shot sped to 1.4, twice 50 ms apart: a heavier, boomier double crack than a, "
              "0.35 s"),
    }),
    "leader-squelch": ("shot", -24.0, -7.0, -23.0,
                       "a Talon squad leader's death (over its `small` rung): the wing scatters; a few times a level",
                       "a short radio squelch: the leader's channel cut off, a dry static burst, no wet squelch "
                       "(the README drafted the chosen radio close, concept/ui-radio-close-r08-a.ogg, which every "
                       "radio line already ends with)", f"{TALON}#behaviour", {
        "a": ([L(760245, 0.0, 0.43, fin=0.001, fout=0.12, hp=300.0)],
              "a walkie-talkie's end of transmission: a sharp static 'kshh' falling off, 0.43 s"),
        "b": ([L(47646, 0.0, 0.37, fin=0.001, fout=0.1, hp=300.0)],
              "a toy walkie-talkie's burst of static at the end of a transmission: brighter and buzzier than a, "
              "0.37 s"),
    }),
    "mite-tell": ("shot", -30.0, -8.0, -28.0,
                  "an Asteroid Mite's 0.4 s leap tell (it crouches, its back glow flares teal); heard only close",
                  "a short gritty rock scrape starting with the tell, 0.4 s, quiet", f"{MITE}#sounds", {
        "a": ([L(364709, 0.0, 0.4, speed=0.75, fin=0.005, fout=0.12, hp=300.0)],
              "two rocks scraped together, slowed to 0.75: a low gritty 'krrsh', 0.4 s"),
        "b": ([L(667283, 0.2, 0.4, fin=0.01, fout=0.12, hp=300.0)],
              "a stone scraped over stone: a longer, finer and more hissing grind than a, 0.4 s"),
    }),
    "mite-leap": ("shot", -29.0, -8.0, -27.0,
                  "an Asteroid Mite leaping off its rock after the tell (the dive at the ship); heard only close",
                  "a quick scuttle and a short whoosh: a small creature springing, dry, no squeak, no pop",
                  f"{MITE}#behaviour", {
        "a": ([L(761559, 2.47, 0.08, gain=-3.0, fin=0.001, fout=0.04, hp=600.0),
               L(683101, 0.14, 0.32, at=0.04, fin=0.01, fout=0.15, hp=200.0)],
              "a crab's claw click, then a quick woosh: 'tk-fwip', 0.36 s"),
        "b": ([L(828623, 5.1, 0.36, gain=-4.0, speed=1.3, fin=0.002, fout=0.08, hp=500.0),
               L(75543, 0.0, 0.36, at=0.1, speed=1.2, fin=0.005, fout=0.12, hp=300.0)],
              "fingertips tapping like insect legs sped to 1.3 (a scuttle), then a bamboo pole's swish: a busier "
              "skitter and a thinner whoosh than a, 0.46 s"),
    }),
}


def layers_of(kind, spec):
    return [] if kind == "synth" else spec


def sources_of(kind, spec):
    """The Freesound ids a variant uses (synthesized layers left out)."""
    return list(dict.fromkeys(layer["sound"] for layer in layers_of(kind, spec) if not isinstance(layer["sound"], str)))


def synth_layers(kind, spec):
    return kind == "synth" or any(isinstance(layer["sound"], str) for layer in spec)


def build(subject, v):
    kind, band, ceiling, cap, _, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})'
                     for s in sources_of(kind, spec))
    if synth_layers(kind, spec):
        used = (used + "; " if used else "") + "the lock beep synthesized"
    raw = SYNTH[subject](v) if kind == "synth" else mix(spec)
    st = level(raw, band, ceiling)
    loud = lufs(st)[0]
    if np.isfinite(loud) and loud > cap:  # level the unlimited mix by its loudness instead (no extra squashing)
        st = loudness(raw, cap, ceiling)
    out = write_ogg(OUT / f"{name}.ogg", st, max_peak_db=ceiling, tags={"COMMENT": f"{SCRIPT} {name}: {what}; {used}"})
    report(out, what)
    return out


# ---- the documentation rows (--docs): CREDITS.md, the sfx README's Concept art table, prompts.md, round 62 ----

LICENCE_URL = {CC0: "https://creativecommons.org/publicdomain/zero/1.0/",
               BY3: "https://creativecommons.org/licenses/by/3.0/", BY4: "https://creativecommons.org/licenses/by/4.0/"}


def num(x):
    return f"{x:g}"


def level_text(subject):
    _, band, ceiling, cap = SOUNDS[subject][:4]
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, kind, spec, layer):
    parts = ["Freesound HQ preview (the original to be fetched if picked)"]
    speed = layer["speed"]
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
    credits, rows, prompts, info = [], [], [], {}
    for subject, v, path in made:
        kind, _, _, _, use, should, _, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in layers_of(kind, spec):
            s = layer["sound"]
            if isinstance(s, str):
                continue
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, kind, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        lv = short_term_band_db(x)
        info[(subject, v)] = (secs, lv, db(np.abs(x).max()), lufs(x)[0], what)
        size = f"{secs:.2f} s ({lv:.0f} dB)".replace("(-", "(−")
        srcs = sources_of(kind, spec)
        edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
        extra = " + a synthesized lock beep" if synth_layers(kind, spec) else ""
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs) + extra
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(kind, spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs) + extra
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`"
                       f"{' (the lock beep synthesized)' if extra else ''}.\n{label}: {links}.\nUse: {use}.\n"
                       f"Brief: {should}; a real recording, heavy, nothing bubbly or popping.\nThis one: {what}.\n"
                       f"Edit: {edit}.\n")
    # the round's rows (links relative to design/concept-rounds/round-62/)
    rnd = []
    for k, subject in enumerate(dict.fromkeys(s for s, _, _ in made), 1):
        kind, _, _, _, use, should, part, variants = SOUNDS[subject]
        part = part.replace("../../", "../../")  # the part links are relative to design/audio/sfx: the same depth
        title = subject.replace("-", " ")
        what = " ".join(f"**{v}** ({info[(subject, v)][0]:.2f} s): {info[(subject, v)][4]}." for v in variants
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
        for v in spec[7]:
            if argv and subject not in argv and f"{subject}-{R}-{v}" not in argv:
                continue
            made.append((subject, v, build(subject, v)))
    if outdir:
        docs(outdir, made)


if __name__ == "__main__":
    main(sys.argv[1:])
