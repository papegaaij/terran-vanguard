#!/usr/bin/env python3
"""Concept round 75 (M10 part C "Level 37 Fall of Aurelia", step AUD-C; the gap check's QC-1): Level 37's own sounds
and the Mirror Interceptor's and the Graft Turret's sets, a and b each (design/audio/sfx/README.md, *Act 6 sounds*, and
the units' briefs): the evacuation's PA chime, the growth bursting over the decks, the balloons tearing, the deck's
structural groan, the burning-deck layer over `aurelia`, the Mirror Interceptor's turbine hum and its pods' triple
crack, the Graft Turret's shot pair, its shell cracking, its root's heartbeat and the root crumbling.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 75 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, sped, layered, swelled, faded, filtered,
looped and levelled: nothing is synthesized (the PA chimes are recorded signals, the brief's "airport chime"). The
hybrids' tissue is never wet (the enemies README's regeneration rule): the growth's burst and the root's death are
cloth, canvas and wood, the tears fabric. No bubbly or popping sources: the balloons tear with no pop (the cables'
snap is a chain or a cut wire, a dry crack), the fire is a roar low-passed under its crackle. The tools are
sfx_r72.py's (copied: preview, bed with its once-a-loop events, build, the --docs rows).

The Graft Turret's shot is "the Ascendancy's small gun pair (the Talon's twin crack)" (the unit's brief), so its a
variant is the Talon's built twin crack rebuilt with its round-62 settings (sfx_r62.py `talon-shot` b, the same preview
and level, as round 74's chimera-shot a): the game plays the Talon's file for it until the pick; b is another real
autocannon's pair. The root's crumble a is round 72's chosen tissue tear (S.2 a, its cloth and settings) with dry twigs
crushed under it (the brief's "the tissue tear with its crunch").

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  pa-chime-r75-{a,b}             ≈ 3.2 / 3.6 s: a calm three-note PA chime through the decks' speakers (band-passed)
  growth-burst-r75-{a,b}         ≈ 3.6 s: a fibrous tearing rush: cloth torn long and slow, timbers cracking, air
  balloon-tear-r75-{a,b}         ≈ 2.8 s: a huge fabric tear in a gale with a cable snapping (no pop)
  deck-groan-r75-{a,b}           ≈ 7 s: a long deep structural groan, a falling rumble receding under its end
  aurelia-burning-r75-{a,b}      seamless 16 s LOOPs: a big fire's roar, a distant two-tone alarm, metal settling
  mirror-hum-r75-{a,b}           seamless 8 s LOOPs: a heavy turbine hum (a jet's idle, slowed)
  mirror-pods-r75-{a,b}          ≈ 1.0 s: a dry triple crack (a real autocannon, three shots close)
  graft-shot-r75-{a,b}           ≈ 0.35 s: the small gun pair (the Talon's twin crack / another autocannon's pair)
  graft-shell-crack-r75-{a,b}    ≈ 0.5 s: a short metal crack with a spark hiss
  graft-heartbeat-r75-{a,b}      ≈ 0.3 s: one low muffled thump (a felt-beater bass drum, dry); the game plays one per
                                 root pulse, so it quickens with the window (1 Hz -> 4 Hz)
  graft-root-crumble-r75-{a,b}   ≈ 0.9 s: a dry fibrous tear with a crunch of dry twigs under it

Levels like rounds 72-74's (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the
loops by their band RMS): the two variants of a subject share the level, so they are loudness-matched; graft-shot a
keeps the Talon's level (sfx_r62.py), so a is the built file.

Usage: python3 tools/concept/audio/sfx_r75.py [subject-or-file ...]   e.g. deck-groan graft-shot-r75-b
       python3 tools/concept/audio/sfx_r75.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
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
SCRIPT = "tools/concept/audio/sfx_r75.py"
R = "r75"
ROUND = "round 75"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    56441: ('Kick-FeltBeater-Soft3.wav', 'Surfjira', BY4, P + '56/56441_438946-hq.ogg'),
    130972: ('Large, Low Rumble.wav', 'duckduckpony', BY4, P + '130/130972_1653827-hq.ogg'),
    138430: ('Air Rush (longer).wav', 'Henrythetrain', CC0, P + '138/138430_2332249-hq.ogg'),
    161650: ('snapping-chain', 'CosmicEmbers', BY3, P + '161/161650_2895542-hq.ogg'),
    162258: ('Mk 46 30mm gun - single shot', 'qubodup', CC0, P + '162/162258_71257-hq.ogg'),
    172411: ('Creaking Metal - Slow', 'EagleStealthTeam', CC0, P + '172/172411_2915633-hq.ogg'),
    187734: ('Idle F-22 Jet Plane.flac', 'qubodup', CC0, P + '187/187734_71257-hq.ogg'),
    203281: ('EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV', 'metrostock99', CC0, P + '203/203281_1256155-hq.ogg'),
    344887: ('Strong wind', 'lextrack', CC0, P + '344/344887_4930786-hq.ogg'),
    354103: ('few small branches snapping, close up, high frequency,H6.wav', 'Bini_trns', CC0,
             P + '354/354103_6544544-hq.ogg'),
    370179: ('3-tone chime DOWN', 'mpaol2023', CC0, P + '370/370179_6430986-hq.ogg'),
    376534: ('Strong Wind Recorded with Soundfield Microphone Manchester 2', 'keirofinch', CC0,
             P + '376/376534_3256739-hq.ogg'),
    396470: ('Electric Sparks.wav', 'kev_durr', CC0, P + '396/396470_5415774-hq.ogg'),
    423108: ('Guns & Explosions Album - Autocannon - Shot 1.wav', 'OGsoundFX', BY4, P + '423/423108_3325582-hq.ogg'),
    437890: ('G08-27-Jet Idle or Taxi.wav', 'craigsmith', CC0, P + '437/437890_2524442-hq.ogg'),
    451068: ('siren ambulance european distant echo nice with steps to clean.wav', 'kyles', CC0,
             P + '451/451068_612689-hq.ogg'),
    483323: ('R30-09-Fires Burns Wood and Metal Crackling.wav', 'craigsmith', CC0, P + '483/483323_2524442-hq.ogg'),
    493697: ('BongBingBung.wav', 'stib', CC0, P + '493/493697_470543-hq.ogg'),
    496836: ('Metallic Groan', 'hinchinbrook', CC0, P + '496/496836_9847211-hq.ogg'),
    518731: ('Cracking, snapping, and clanging (metal can)', 'Ashley12275', CC0, P + '518/518731_11141750-hq.ogg'),
    523291: ('Wirecutter 1', 'Sheyvan', CC0, P + '523/523291_3248005-hq.ogg'),
    596518: ('Sticks Breaking.wav', 'EugeneEverett', CC0, P + '596/596518_10974703-hq.ogg'),
    637911: ('tree cut crack and fall in forest2 stereo.flac', 'kyles', CC0, P + '637/637911_612689-hq.ogg'),
    638636: ('Broken Metallic.wav', 'datasoundsample', BY4, P + '638/638636_433684-hq.ogg'),
    643861: ('spark2.ogg', 'Kneeling', CC0, P + '643/643861_7811934-hq.ogg'),
    688735: ('Heartbeat 97 BPM', 'Cloud-10', CC0, P + '688/688735_10403690-hq.ogg'),
    703217: ('ripping fabric.wav', 'GeerkeC', BY4, P + '703/703217_11418333-hq.ogg'),
    708104: ('heavy fabric tearing v2, thick fabric tearing, tearing.Recorded on H6Zoom XY capsule', 'Rinaldobowles56',
             CC0, P + '708/708104_14713648-hq.ogg'),
    719003: ('Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast', 'ChristopherJngs', CC0,
             P + '719/719003_8154783-hq.ogg'),
    719005: ('Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Slow / Staccato',
             'ChristopherJngs', CC0, P + '719/719005_8154783-hq.ogg'),
    728158: ('Tearing fabric', 'Coo01', CC0, P + '728/728158_6033218-hq.ogg'),
    811916: ('Martinshorn (Siren) 2', 'TitanKaempfer', CC0, P + '811/811916_9713112-hq.ogg'),
    854184: ('Autocannon Shot 2', 'qubodup', CC0, P + '854/854184_71257-hq.ogg'),
    854186: ('Autocannon Three Shot Burst', 'qubodup', CC0, P + '854/854186_71257-hq.ogg'),
    784053: ('Plane Crash Ambiance v1', 'thompsonse', CC0, P + '784/784053_3087113-hq.ogg'),
    869103: ('Tall dead tree splintering then falling to ground with crashing Impact', 'ozymandias37', CC0,
             P + '869/869103_11162537-hq.ogg'),
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


# The Talon's built twin crack (sfx_r62.py talon-shot b), the Graft Turret's shot a as is.
TALON_SHOT = [L(162258, 0.0, 0.3, speed=1.4, fin=0.001, fout=0.15, hp=250.0),
              L(162258, 0.0, 0.3, gain=-2.0, at=0.05, speed=1.5, fin=0.001, fout=0.15, hp=250.0)]
L37 = "Level 37 (Fall of Aurelia)"
MIRROR = "a Mirror Interceptor (Levels 37-42, the Ascendancy's gold mirror fighter)"
GRAFT = "a Graft Turret (Levels 37-42, an Ascendancy turret grafted onto a Vrell root)"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS.
SOUNDS = {
    "pa-chime": ("shot", -26.0, -6.0, -26.0,
                 f"{L37}: the city's public-address chime as each shuttle pair is called to its pads (t=42, 70 and "
                 "125, 2 s before each pair lifts, with the HOLD FIRE banner); three times a level, over the music",
                 "a calm three-note PA chime (an airport's or a station's attention chime) through the decks' "
                 "loudspeakers: clear, unhurried, a civil signal in the middle of a battle", {
        "a": ([L(493697, 0.0, 3.2, fin=0.002, fout=1.2, hp=300.0, lp=4500.0)],
              "an old airport's attention chime, bing-bong-bung (three notes 0.6 s apart, the last one lower), "
              "band-passed 300 Hz-4.5 kHz like a hall's loudspeakers, 3.2 s"),
        "b": ([L(370179, 0.0, 3.6, fin=0.002, fout=1.2, hp=300.0, lp=4500.0)],
              "a three-tone chime stepping down, a note a second, each ringing out: slower and softer than a, the "
              "same loudspeaker band, 3.6 s"),
    }),
    "growth-burst": ("shot", -17.0, -3.0, -18.0,
                     f"{L37}, t=100: the Vrell growth bursting over Aurelia's decks as the Choir turns on Vorne (the "
                     "turn); once a level, under the Choir's line and the music's duck",
                     "the hybrids' tissue tear drawn long and wide, with timbers cracking and a rush of air: a fibrous "
                     "tearing rush, never wet or bubbly", {
        "a": ([L(719005, 2.5, 3.0, speed=0.6, fin=0.05, fout=1.0, hp=60.0),
               L(637911, 3.0, 2.6, gain=-3.0, at=0.3, speed=0.9, fin=0.2, fout=1.0, hp=60.0),
               L(138430, 5.8, 3.6, gain=-5.0, speed=1.2, fin=0.4, fout=1.6, hp=60.0)],
              "cotton cloth torn long and slow by hand slowed to 0.6 (the hybrids' chosen tear's family), a tree "
              "splintering as it is felled, and a rush of air swelling under them: a wide tearing rush, 3.6 s"),
        "b": ([L(728158, 11.8, 3.0, speed=0.7, fin=0.05, fout=1.0, hp=60.0),
               L(869103, 2.5, 2.6, gain=-3.0, at=0.4, speed=0.9, fin=0.2, fout=1.0, hp=60.0),
               L(344887, 2.0, 3.6, gain=-6.0, fin=0.5, fout=1.6, hp=80.0)],
              "a heavier fabric ripped in long pulls slowed to 0.7, a tall dead tree splintering, and a strong wind "
              "gusting through: coarser and windier than a, 3.6 s"),
    }),
    "balloon-tear": ("shot", -19.0, -4.0, -20.0,
                     f"{L37}, the falling city (t=160-175): each of Aurelia's lift balloons tearing open as the deck "
                     "goes down (a few times over the fall), over the fall stem and the burning layer",
                     "a huge fabric tear, a sail ripping in a gale, with cables snapping; no pop, nothing bursting", {
        "a": ([L(719005, 4.0, 2.4, speed=0.5, fin=0.05, fout=0.8, hp=50.0),
               L(376534, 2.0, 2.8, gain=-8.0, fin=0.3, fout=1.0, hp=80.0),
               L(161650, 0.3, 0.6, gain=-5.0, at=0.9, speed=0.8, fin=0.002, fout=0.3, hp=120.0, lp=6000.0)],
              "cloth torn by hand slowed to half (a sail's size) in a strong wind, a chain snapping under tension "
              "slowed to 0.8 halfway through (a cable parting): 2.8 s"),
        "b": ([L(703217, 6.0, 2.4, speed=0.55, fin=0.05, fout=0.8, hp=50.0),
               L(344887, 6.0, 2.8, gain=-8.0, fin=0.3, fout=1.0, hp=80.0),
               L(523291, 1.55, 0.35, gain=-4.0, at=1.0, speed=0.6, fin=0.002, fout=0.2, hp=150.0, lp=6000.0)],
              "a fabric ripped in short pulls slowed to 0.55 in a strong wind, a wire cut through slowed to 0.6 "
              "(a cable's dry snap): more staccato than a, 2.8 s"),
    }),
    "deck-groan": ("shot", -20.0, -3.0, -21.5,
                   f"{L37}, t=160: the deck's structural groan as Aurelia breaks along its cut and tilts, the falling "
                   "rumble receding below as the halves drift down; once a level, at the start of the fall",
                   "a long deep structural groan (a ship's hull straining, a bridge creaking, slowed) and a falling "
                   "rumble receding below", {
        "a": ([L(496836, 3.0, 7.0, speed=0.6, fin=0.4, fout=2.0, hp=35.0, lp=3000.0),
               L(130972, 8.0, 4.5, gain=-4.0, at=3.0, speed=0.8, fin=0.8, fout=2.5, hp=25.0, lp=300.0)],
              "a big metallic groan slowed to 0.6 (a hull straining), a large low rumble at its peak and dying "
              "away slowed to 0.8 under its second half: 7.5 s"),
        "b": ([L(172411, 2.0, 6.5, speed=0.5, fin=0.4, fout=2.0, hp=35.0, lp=3000.0),
               L(203281, 4.0, 4.5, gain=-11.0, at=3.0, speed=0.7, fin=1.0, fout=2.5, hp=25.0, lp=250.0)],
              "metal creaking slowly under load slowed to half (a long grinding creak rather than a's single "
              "groan), a deep earthquake rumble slowed to 0.7 fading under it: 7.5 s"),
    }),
    "aurelia-burning": ("loop", -40.0, -1.0, -29.0,
                        f"{L37}, from the turn (t=100) to the level end: a layer over the `aurelia` bed (the beds' "
                        "0.70, under the music) while the city burns",
                        "a big fire's roar, a distant two-tone city alarm through the decks, metal groaning and "
                        "settling now and then; a seamless loop, no crackle that reads as popping", {
        "a": (([Loop(483323, 20.0, hp=60.0, lp=3500.0)], 16.0, 1.0,
               {"events": [L(451068, 4.0, 3.0, gain=-12.0, at=2.0, fin=0.5, fout=1.0, hp=300.0, lp=2000.0),
                           L(496836, 4.0, 3.5, gain=-9.0, at=9.0, speed=0.7, fin=0.6, fout=1.2, hp=40.0,
                             lp=2500.0)]}),
              "a large fire burning wood and metal, its steady roar with the crackle rolled off at 3.5 kHz; once a loop a European two-tone "
              "siren far off, muffled through the decks, and a metallic groan slowed to 0.7: 16.0 s loop"),
        "b": (([Loop(784053, 4.0, hp=50.0, lp=3000.0)], 16.0, 1.0,
               {"events": [L(811916, 0.5, 12.0, gain=-15.0, at=1.0, fin=3.0, fout=3.0, hp=300.0, lp=1500.0),
                           L(172411, 20.0, 4.0, gain=-9.0, at=6.0, speed=0.6, fin=0.8, fout=1.2, hp=40.0,
                             lp=2500.0)]}),
              "a burning wreck's ambience (a fire's lower, rumbling roar with metal creaking in it) with a German two-tone siren swelling and fading behind the "
              "decks for 12 s of every 16 (heavily muffled); once a loop slow metal creaking under load: 16.0 s "
              "loop"),
    }),
    "mirror-hum": ("loop", -36.0, -1.0, -28.0,
                   f"{MIRROR}: its engine, one loop for the nearest Mirror Interceptor on the screen (once per kind), "
                   "under the fire; often on through Levels 37-42",
                   "a heavy turbine hum, lower than the Gunship's: a big airframe hanging at its station", {
        "a": (([Loop(187734, 2.0, speed=0.7, hp=40.0, lp=2500.0)], 8.0, 1.0),
              "a fighter jet idling on the ground slowed to 0.7: a deep steady turbine hum with the whine pulled "
              "down, 8.0 s loop"),
        "b": (([Loop(437890, 8.0, speed=0.75, hp=40.0, lp=3000.0)], 8.0, 1.0),
              "a jet idling and taxiing slowed to 0.75: a rougher, airier turbine rumble than a, 8.0 s loop"),
    }),
    "mirror-pods": ("shot", -26.0, -6.0, -24.0,
                    f"{MIRROR}: each burst of its pods (3 shots within 30° every 2.4 s, after the 0.4 s pod glow); "
                    "often",
                    "a dry triple crack: a real cannon, three shots close together", {
        "a": ([L(854186, 0.0, 1.0, fin=0.001, fout=0.4, hp=150.0)],
              "an autocannon's three-shot burst as recorded, the shots 0.25 s apart, its tail cut: 1.0 s"),
        "b": ([L(423108, 0.0, 0.45, speed=1.3, fin=0.001, fout=0.25, hp=180.0),
               L(423108, 0.0, 0.45, gain=-1.0, at=0.16, speed=1.35, fin=0.001, fout=0.25, hp=180.0),
               L(423108, 0.0, 0.6, gain=-2.0, at=0.32, speed=1.3, fin=0.001, fout=0.35, hp=180.0)],
              "another autocannon's single shot sped to 1.3, three times 0.16 s apart: a tighter, boomier triple "
              "than a, 0.9 s"),
    }),
    "graft-shot": ("shot", -28.0, -6.0, -24.0,
                   f"{GRAFT}: each shot pair (every 2.2 s while it bears, after the red glint); often",
                   "the Ascendancy's small gun pair: the Talon's dry twin crack (the unit's brief)", {
        "a": (TALON_SHOT,
              "the Talon's built twin crack as is (sfx_r62.py talon-shot b): a 30 mm gun's single shot sped to 1.4, "
              "twice 50 ms apart, 0.35 s; the game plays the Talon's file for it"),
        "b": ([L(854184, 0.0, 0.3, speed=1.3, fin=0.001, fout=0.15, hp=250.0),
               L(854184, 0.0, 0.3, gain=-2.0, at=0.06, speed=1.38, fin=0.001, fout=0.15, hp=250.0)],
              "another autocannon's single shot sped to 1.3 and 1.38, twice 60 ms apart: a brighter, harder pair "
              "than the Talon's, telling a ground gun from the fighters, 0.36 s"),
    }),
    "graft-shell-crack": ("shot", -24.0, -5.0, -22.0,
                          f"{GRAFT}: its shell bursting when the turret is killed (the root left exposed), over its "
                          "size rung; often",
                          "a short metal crack with a spark hiss", {
        "a": ([L(638636, 0.0, 0.45, fin=0.001, fout=0.25, hp=150.0),
               L(643861, 0.1, 0.32, gain=-8.0, at=0.05, fin=0.01, fout=0.15, hp=2000.0)],
              "a metal casing breaking (a sharp metallic crack) with a short burst of electric sparks after it: "
              "0.5 s"),
        "b": ([L(518731, 0.12, 0.45, speed=0.7, fin=0.001, fout=0.2, hp=100.0),
               L(396470, 0.1, 0.45, gain=-9.0, at=0.04, fin=0.02, fout=0.2, hp=1500.0)],
              "a metal can cracking and snapping slowed to 0.7 with a sputter of sparks: thinner and tinnier than "
              "a, 0.5 s"),
    }),
    "graft-heartbeat": ("shot", -34.0, -8.0, -30.0,
                        f"{GRAFT}: the exposed root's pulse, one thump per pulse while the root shows (the nearest "
                        "root), quickening from 1 a second to 4 a second as its 5 s window closes",
                        "a low muffled heartbeat thump (a felt-beater bass drum, dry); short enough for 4 a second", {
        "a": ([L(56441, 0.0, 0.3, fin=0.001, fout=0.15, hp=30.0, lp=600.0)],
              "a bass drum struck softly with a felt beater, muffled under 600 Hz: one soft low thump, 0.3 s"),
        "b": ([L(688735, 0.0, 0.28, fin=0.001, fout=0.12, hp=30.0, lp=600.0)],
              "a heartbeat's first beat (a kick with its highs cut, a deeper, rounder thump than a): 0.28 s"),
    }),
    "graft-root-crumble": ("shot", -22.0, -5.0, -22.0,
                           f"{GRAFT}: its root destroyed inside the window (the turret stays dead); often",
                           "the hybrids' tissue tear with a crunch under it (dry twigs crushed): a fibrous tearing "
                           "crunch, never wet", {
        "a": ([L(719003, 0.35, 0.9, speed=0.6, fin=0.005, fout=0.3, hp=100.0),
               L(354103, 1.0, 0.5, gain=-3.0, at=0.1, fin=0.002, fout=0.2, hp=200.0)],
              "the hybrids' chosen tear (round 72 S.2 a: cotton cloth torn by hand, slowed to 0.6) with small dry "
              "branches snapping under it: 0.9 s"),
        "b": ([L(708104, 2.33, 0.8, speed=0.8, fin=0.01, fout=0.3, hp=120.0),
               L(596518, 2.2, 0.6, gain=-3.0, at=0.1, fin=0.002, fout=0.25, hp=150.0)],
              "thick fabric torn in half slowed to 0.8 with sticks breaking under it: coarser and woodier than a, "
              "0.9 s"),
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
