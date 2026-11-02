#!/usr/bin/env python3
"""Concept round 80 (M10 part H "Level 42 Ascendant", step AUD-H; the gap check's #20): the Ascendant's sounds and
Level 42's secret, a and b each (the boss's *Sounds brief*, design/enemies/bosses/ascendant/README.md; Level 42's
*Music & ambience*; design/audio/sfx/README.md, *Act 6 sounds*): the hull's arrival, the spinal lance's charge and its
shot, a mirror panel's bounce (the Mirror Interceptor's ping, lower and heavier), a generator down, the eruption, a sac
healing (the regrowth creak deeper), the brood (the tissue tear and the Chimeras' engines), the hull breaking (rapid
detonations, shearing, a deep boom at the flash), the lifeboat (a tear, then a straining whine climbing away), and the
dead tender's strongbox cracking. The launched units and the borrowed attacks keep their own sounds; the hand-off and
the escape sting are the music's (music_r80.py).

The recorded sounds are Freesound sounds (CC0 or CC-BY, in CREDITS.md) taken from their public HQ previews
(cdn.freesound.org, OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 80 and the picks rebuilt from them with the same settings.
They are only cut, slowed, sped, glided, layered, swelled, faded, filtered and levelled. Heavy and mechanical, then
organic (dry leather, rope and cloth), never wet; no bubbly or popping sources.

Where the brief reuses a built sound the chosen file's own source and cut are the base: the panel bounce a is round
72's chosen `mirror-ping` a (Glass Ping) slowed to 0.6 over a big bowl; the sac healing a is the chosen
`regrowth-creak` a an octave down (its cut at half its speed); the brood and the lifeboat open on the chosen
`tissue-tear` a (cloth torn, slowed), the brood's engines are the chosen `chimera-whine` b's layers.

The lance is two files, as Harker's sniper (round 78): the charge (2.0 s, the medium tell: the 0.8 s muzzle glow and
the 1.2 s line; the game starts it later on hard and holds easy's extra 0.3 s, and cuts it with the shot) and the shot.
The hull breaking puts its deep boom at 2.5 s, the act boss's screen flash (K + 2.5 s), where the game plays the `huge`
rung with it.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  ascendant-arrival-r80-{a,b}         ≈ 6.5 s: a huge hull's groan, a deep engine drone rising, metal stress ticks
  ascendant-lance-charge-r80-{a,b}    2.0 s: a deep rising capacitor whine over a swelling hum
  ascendant-lance-shot-r80-{a,b}      ≈ 3.2 s: a heavy electric crack and a long thunder rumble
  ascendant-panel-bounce-r80-{a,b}    ≈ 0.7 s: a low, heavy glassy ping (a large bowl)
  ascendant-generator-down-r80-{a,b}  ≈ 2.1 s: a hum collapsing into crackles and a thud
  ascendant-eruption-r80-{a,b}        ≈ 4.2 s: tearing steel plates, a long rending groan, a low dry creak under it
  ascendant-sac-heal-r80-{a,b}        ≈ 2.4 / 1.7 s: a soft rising creak, deep
  ascendant-brood-r80-{a,b}           ≈ 2.1 s: a dry tearing rip, then the Chimeras' engines peeling away
  ascendant-hull-break-r80-{a,b}      ≈ 5.4 s: rapid detonations and shearing metal, a deep boom at 2.5 s
  ascendant-lifeboat-r80-{a,b}        ≈ 8.4 s: a tearing rip, then a straining engine whine climbing and fading away
  tender-strongbox-r80-{a,b}          ≈ 1.3 s: a steel lockbox's hinges straining and shearing, the lid clattering

Levels like the accepted picks (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap): the
arrival and the eruption as round 58's Maw set pieces, a little under; the charge as the rail line's charge, the shot a
little over the rail shot; the bounce as the chosen mirror ping; a generator as a mining-tug section lost; the sac as the
chosen regrowth creak; the brood as the tissue tear; the hull breaking as round 58's breakout; the lifeboat as a
fly-by; the strongbox as Level 41's vault crack. The two variants of a subject share the level, so they are
loudness-matched.

Usage: python3 tools/concept/audio/sfx_r80.py [subject-or-file ...]   e.g. ascendant-brood tender-strongbox-r80-b
       python3 tools/concept/audio/sfx_r80.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
                                                                       rows and the prompts.md entries
Deterministic (no randomness, bit-exact ffmpeg flags). Not listened to by Claude: checked by level, peaks and onsets.
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
from synth import SR, db, decode, lufs, write_ogg  # noqa: E402

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / "design" / "audio" / "sfx" / "concept"
SCRIPT = "tools/concept/audio/sfx_r80.py"
R = "r80"
ROUND = "round 80"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    80328: ("bigbowljuicerstrike04.HP.aif", "lsprice", BY3, P + '80/80328_193132-hq.ogg'),
    118262: ("Metal Bend.wav", "pwausc1", CC0, P + '118/118262_2034059-hq.ogg'),
    119902: ("Camera Flash Charging.aif", "ftpalad", CC0, P + '119/119902_1990698-hq.ogg'),
    125326: ("camera flash charge.wav", "adeluc4", CC0, P + '125/125326_2266204-hq.ogg'),
    133514: ("Spindown Huge", "Olaf464", CC0, P + '133/133514_1740744-hq.ogg'),
    150454: ("Crystal bowl F#3 1.wav", "caiogracco", CC0, P + '150/150454_2703701-hq.ogg'),
    172379: ("ExplosionBombBlastAmbientA.wav", "zimbot", BY4, P + '172/172379_1449999-hq.ogg'),
    172407: ("Creaking Metal - With Metallic Reverb", "EagleStealthTeam", CC0, P + '172/172407_2915633-hq.ogg'),
    172411: ("Creaking Metal - Slow", "EagleStealthTeam", CC0, P + '172/172411_2915633-hq.ogg'),
    209772: ("Metal Boom", "Johnnyfarmer", CC0, P + '209/209772_2012390-hq.ogg'),
    244394: ("Bang/Explosion Metallic", "Werra", CC0, P + '244/244394_525427-hq.ogg'),
    366093: ("Manhole Blows / Metallic Boom", "bevibeldesign", CC0, P + '366/366093_4557960-hq.ogg'),
    367454: ("Flashover discharge 280KV.wav", "jensfelger", CC0, P + '367/367454_6750383-hq.ogg'),
    411087: ("creaking sailing boat.wav", "jimsim", BY4, P + '411/411087_130385-hq.ogg'),
    415962: ("high-voltage-discharge-2.wav", "follytowers", CC0, P + '415/415962_6668765-hq.ogg'),
    453303: ("machine hydraulic metal cutter close crunch1.flac", "kyles", CC0, P + '453/453303_612689-hq.ogg'),
    453333: ("metal crank groan.flac", "kyles", CC0, P + '453/453333_612689-hq.ogg'),
    453444: ("hum electric transformer in large room1.flac", "kyles", CC0, P + '453/453444_612689-hq.ogg'),
    462819: ("HUTCH HD SFX 2014 0143", "leonelmail", CC0, P + '462/462819_4437257-hq.ogg'),
    483284: ("R11-55-Large Blasts.wav", "craigsmith", CC0, P + '483/483284_2524442-hq.ogg'),
    491407: ("Jet engine startup", "SamsterBirdies", CC0, P + '491/491407_5487341-hq.ogg'),
    496836: ("Metallic Groan", "hinchinbrook", CC0, P + '496/496836_9847211-hq.ogg'),
    508281: ("Old_Truck_Metal_Squeaks-Creaks-Rattle-Impact_Squeaky_Door_Hinge.wav", "dilsun", CC0,
             P + '508/508281_4810511-hq.ogg'),
    513944: ("Bolt Cutter.wav", "schots", CC0, P + '513/513944_954919-hq.ogg'),
    516591: ("Large Bolt Cutter", "jameswrowles", CC0, P + '516/516591_3874107-hq.ogg'),
    534023: ("Thunderclap", "Fission9", CC0, P + '534/534023_9395330-hq.ogg'),
    542582: ("Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3", "adr1911", CC0,
             P + '542/542582_10366986-hq.ogg'),
    587442: ("Dropping sheet metal", "SamsterBirdies", CC0, P + '587/587442_5487341-hq.ogg'),
    587886: ("Explosion large", "SamsterBirdies", CC0, P + '587/587886_5487341-hq.ogg'),
    632487: ("Closing a steel padlock shackle (Take A)", "ani_music", CC0, P + '632/632487_3008343-hq.ogg'),
    640499: ("Eurofighter Typhoon Flyby 003 – Close Proximity", "TimoSchmied", BY4, P + '640/640499_4424897-hq.ogg'),
    675480: ("S37-25 Car body crushed by machine; leaks and moans.wav", "craigsmith", CC0,
             P + '675/675480_2524442-hq.ogg'),
    719003: ("Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast", "ChristopherJngs", CC0,
             P + '719/719003_8154783-hq.ogg'),
    726486: ("Impact on metal", "JoMungus", CC0, P + '726/726486_11865776-hq.ogg'),
    745014: ("Glass Ping", "tix99", CC0, P + '745/745014_2573767-hq.ogg'),
    797919: ("LETHRCreak_Leather Stretching Creak 01_KVV AUDIO_FREE", "KVV_Audio", BY4, P + '797/797919_12846320-hq.ogg'),
    824870: ("Stovetop Heat Creaking 1", "Filmscore", CC0, P + '824/824870_843915-hq.ogg'),
    840628: ("Closeup Thunder Strike 01", "loganzsound", CC0, P + '840/840628_16682330-hq.ogg'),
    849772: ("Electric Sparks", "mikiko850", BY4, P + '849/849772_18554592-hq.ogg'),
    728157: ("130kV Power line short circuit", "prophecy77", CC0, P + '728/728157_2760781-hq.ogg'),
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
L = sfx_r43g.L


def mix(layers):
    parts = [(cut(layer), int(layer["at"] * SR)) for layer in layers]
    out = np.zeros((2, max(at + x.shape[1] for x, at in parts)))
    for x, at in parts:
        out[:, at:at + x.shape[1]] += x
    return out


ASC = "the Ascendant (Level 42's boss)"
TEAR = "the hybrids' chosen tear (round 72 S.2 a)"

# subject -> (level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like, {variant: (layers, what)});
# every subject a one-shot levelled by the loudest 100 ms of the 200 Hz-5 kHz band, at most `cap` LUFS.
SOUNDS = {
    "ascendant-arrival": (-20.0, -3.0, -18.0,
                          f"{ASC}: the hull sliding in bow-first over 6 s after the boss warning (invulnerable until "
                          "in place), the gold lights coming on; once a level",
                          "a huge ship's hull groan and a deep engine drone rising, metal stress ticks", {
        "a": ([L(491407, 2.0, 6.5, speed=(0.35, 0.6), fin=1.5, fout=1.2, hp=30.0, lp=2500.0, swell=(-18.0, 1.0)),
               L(496836, 4.0, 4.6, gain=-2.0, at=0.9, speed=0.7, fin=0.4, fout=1.2, hp=40.0),
               L(824870, 8.0, 4.5, gain=-12.0, at=1.2, fin=0.5, fout=1.0, hp=1500.0)],
              "a jet engine's start-up slowed to a third and gliding up (a deep drone rising), a long metallic groan "
              "slowed to 0.7 over it, a hot stove's metal ticking and pinging high above: 6.5 s"),
        "b": ([L(542582, 2.2, 6.5, speed=(0.3, 0.55), fin=1.5, fout=1.2, hp=30.0, lp=2000.0, swell=(-18.0, 1.0)),
               L(172411, 1.5, 5.0, gain=-2.0, at=0.6, speed=0.6, fin=0.6, fout=1.2, hp=40.0),
               L(172407, 0.5, 4.0, gain=-10.0, at=1.5, fin=0.3, fout=1.0, hp=1200.0)],
              "a tank's gas turbine starting, slowed to a third and gliding up (a rougher, lower drone than a), "
              "slow creaking metal slowed to 0.6, metal creaks in a hall high above: 6.5 s"),
    }),
    "ascendant-lance-charge": (-26.0, -5.0, -25.0,
                               f"{ASC}, phase 1: the spinal lance's charge, from the bow muzzle's 0.8 s white-gold "
                               "glow through the 1.2 s red line (medium; easy 1.5 s, hard 1.0 s), cut by the shot; one "
                               "draw of the bow cannon's playbook, every few seconds",
                               "a deep rising capacitor whine, the rail line's charge family but deeper and longer", {
        "a": ([L(119902, 2.0, 2.0, speed=(0.2, 0.42), fin=0.4, fout=0.02, hp=150.0, swell=(-14.0, 1.2)),
               L(453444, 8.0, 2.0, gain=-5.0, speed=(0.7, 1.3), fin=0.5, fout=0.02, hp=40.0, swell=(-12.0, 1.0))],
              "a camera flash's charging whine pitched down about two octaves and gliding up, over a transformer's "
              "hum swelling and rising: a deep whine climbing to the shot, 2.0 s"),
        "b": ([L(125326, 2.8, 2.0, speed=(0.22, 0.45), fin=0.4, fout=0.02, hp=150.0, swell=(-14.0, 1.2)),
               L(542582, 2.2, 2.0, gain=-5.0, speed=(0.45, 0.85), fin=0.5, fout=0.02, hp=60.0,
                 swell=(-12.0, 1.0))],
              "another camera flash charging, pitched down and gliding up, over a gas turbine spooling up: rougher "
              "and more mechanical than a, 2.0 s"),
    }),
    "ascendant-lance-shot": (-17.0, -3.0, -16.0,
                             f"{ASC}, phase 1: the spinal lance firing (the 0.5 s beam, 24 px wide, down the whole "
                             "field), after its charge",
                             "a heavy electric crack with a long rumble", {
        "a": ([L(415962, 9.62, 0.5, fin=0.002, fout=0.3, hp=80.0),
               L(840628, 0.68, 3.2, gain=-1.0, at=0.02, fin=0.003, fout=1.5, hp=30.0)],
              "a high-voltage discharge's loudest crack, with a close lightning strike's crack and its rumble rolling "
              "out under it: 3.2 s"),
        "b": ([L(367454, 0.3, 0.4, fin=0.002, fout=0.25, hp=200.0),
               L(534023, 0.85, 3.2, at=0.0, fin=0.003, fout=1.5, hp=30.0)],
              "a 280 kV flashover's sizzling snap over a thunderclap rolling and rumbling: a duller, rounder crack "
              "than a, a longer roll, 3.2 s"),
    }),
    "ascendant-panel-bounce": (-26.0, -6.0, -26.0,
                               f"{ASC}, phase 1: each shot a battery's mirror panel bounces back (throttled as the "
                               "Mirror Interceptor's: once per 0.08 s a panel); often while the batteries stand",
                               "the Mirror Interceptor's glassy ping (`mirror-ping`), lower and heavier: a larger "
                               "bowl; never the Reflex Shield's ricochet", {
        "a": ([L(745014, 0.0, 0.65, speed=0.6, fin=0.001, fout=0.3, hp=200.0),
               L(80328, 0.0, 0.6, gain=-8.0, speed=0.85, fin=0.001, fout=0.3, hp=150.0)],
              "the chosen mirror ping's glass struck once, slowed to 0.6, over a big metal bowl struck: the same "
              "'ping' lower, with a heavier body, 0.65 s"),
        "b": ([L(150454, 0.0, 0.7, speed=0.85, fin=0.001, fout=0.35, hp=150.0)],
              "a crystal singing bowl struck, slowed to 0.85: a rounder, deeper bell-like glass tone than a, 0.7 s"),
    }),
    "ascendant-generator-down": (-21.0, -4.0, -21.0,
                                 f"{ASC}, phase 1: a generator destroyed (the cannon's gold shield halves, then "
                                 "falls), over its explosion; twice a level",
                                 "a transformer failing: a hum collapsing into crackles and a thud", {
        "a": ([L(453444, 8.0, 1.0, gain=-3.0, speed=(1.0, 0.4), fin=0.03, fout=0.15, hp=80.0),
               L(849772, 3.0, 1.0, gain=-8.0, at=0.45, fin=0.05, fout=0.5, hp=1000.0),
               L(462819, 0.36, 1.0, gain=-4.0, at=1.0, speed=0.8, fin=0.002, fout=0.5, hp=90.0)],
              "a transformer's hum falling more than an octave, electric sparks crackling over its end, a deep "
              "hollow impact: 2.0 s"),
        "b": ([L(133514, 0.5, 1.1, gain=-2.0, speed=(1.0, 0.5), fin=0.03, fout=0.15, hp=40.0),
               L(728157, 1.0, 1.0, gain=-6.0, at=0.4, fin=0.05, fout=0.5, hp=400.0),
               L(209772, 0.3, 1.0, at=1.05, fin=0.002, fout=0.6, hp=30.0)],
              "a huge machine spinning down an octave, a 130 kV power line's short circuit crackling over it, a deep "
              "metal boom: rougher and boomier than a, 2.05 s"),
    }),
    "ascendant-eruption": (-19.0, -3.0, -17.0,
                           f"{ASC}: phase 2's start, the hull shuddering and the Vrell half erupting through the gold "
                           "plates (1 s, then 3 s), under the 19 → 18 crossfade, the Choir's line and Vorne's shout; "
                           "once a level",
                           "tearing steel plates and a long rending groan, a low organic creak under it (stretched "
                           "leather or rope, slowed); never wet", {
        "a": ([L(675480, 5.0, 4.2, speed=0.8, fin=0.3, fout=1.2, hp=40.0),
               L(118262, 13.9, 2.6, gain=-2.0, at=0.8, speed=0.85, fin=0.02, fout=0.8, hp=60.0),
               L(797919, 0.3, 3.4, gain=-8.0, at=0.4, speed=0.45, fin=0.6, fout=1.0, hp=50.0, swell=(-8.0, 1.0))],
              "a car body crushed by a machine, slowed to 0.8 (a long rending groan of steel), sheet steel bending "
              "and tearing over it, the chosen leather creak slowed to 0.45 under it: 4.2 s"),
        "b": ([L(453303, 1.9, 1.8, speed=0.7, fin=0.02, fout=0.6, hp=50.0),
               L(453303, 6.0, 1.4, gain=-3.0, at=1.6, speed=0.7, fin=0.02, fout=0.6, hp=50.0),
               L(172407, 0.4, 4.2, gain=-2.0, at=0.3, speed=0.5, fin=0.4, fout=1.2, hp=40.0),
               L(411087, 3.3, 3.4, gain=-8.0, at=0.5, speed=0.5, fin=0.5, fout=1.0, hp=50.0)],
              "a hydraulic cutter crunching through steel twice, slowed to 0.7, metal creaking in a hall slowed "
              "to half (a deeper, hollower groan than a), a sailing boat's rigging creaking slowed under it: 4.2 s"),
    }),
    "ascendant-sac-heal": (-28.0, -8.0, -28.0,
                           f"{ASC}, phase 2: a sac regenerating (repeated for the nearest healing sac), quiet under "
                           "the fire",
                           "the Chimera's soft rising creak (`regrowth-creak`), deeper; never wet", {
        "a": ([L(797919, 0.3, 2.4, speed=0.425, fin=0.5, fout=0.6, hp=60.0, swell=(-10.0, 1.0))],
              "the chosen regrowth creak an octave down (the same leather strap, the same cut, at half its speed): "
              "a slow, deep leather creak that rises, 2.4 s"),
        "b": ([L(797919, 0.3, 1.7, speed=0.6, fin=0.35, fout=0.4, hp=80.0, swell=(-10.0, 1.0)),
               L(411087, 3.4, 1.7, gain=-4.0, speed=0.6, fin=0.3, fout=0.4, hp=150.0, swell=(-8.0, 1.0))],
              "the chosen leather creak slowed to 0.6 with a sailing boat's rigging creaking at 0.6 under it: "
              "woodier and shorter than a, 1.7 s"),
    }),
    "ascendant-brood": (-22.0, -5.0, -21.0,
                        f"{ASC}, phase 2: a sac splitting (after its 0.8 s swell) and a wing of 4 Chimeras flying "
                        "out; at most every third draw",
                        "a dry tearing rip (the hybrids' tissue tear), then the Chimeras' engines; never wet", {
        "a": ([L(719003, 0.35, 1.3, speed=0.45, fin=0.005, fout=0.35, hp=80.0),
               L(640499, 3.0, 1.0, gain=-4.0, at=0.8, speed=0.95, fin=0.15, fout=0.4, hp=350.0),
               L(491407, 19.0, 1.0, gain=-13.0, at=0.8, speed=0.8, fin=0.12, fout=0.4, hp=300.0, lp=3000.0),
               L(640499, 3.0, 1.0, gain=-7.0, at=1.15, speed=0.9, fin=0.15, fout=0.4, hp=350.0)],
              f"{TEAR} slowed further to 0.45 (a heavier, longer rip), then the Chimera's chosen whine twice, the "
              "second lower: 2.15 s"),
        "b": ([L(719003, 0.35, 0.9, speed=0.6, fin=0.005, fout=0.25, hp=100.0),
               L(640499, 3.0, 0.75, gain=-3.0, at=0.55, speed=0.95, fin=0.15, fout=0.3, hp=350.0),
               L(491407, 19.0, 0.75, gain=-12.0, at=0.55, speed=0.8, fin=0.12, fout=0.3, hp=300.0, lp=3000.0),
               L(640499, 3.0, 0.75, gain=-5.0, at=0.8, speed=1.0, fin=0.15, fout=0.3, hp=350.0),
               L(640499, 3.0, 0.75, gain=-7.0, at=1.05, speed=0.9, fin=0.15, fout=0.3, hp=350.0),
               L(640499, 3.0, 0.75, gain=-9.0, at=1.3, speed=0.97, fin=0.15, fout=0.3, hp=350.0)],
              f"{TEAR} as it is, then the Chimera's chosen whine four times a quarter second apart (the wing "
              "peeling off one by one): a quicker rip and a busier tail than a, 2.05 s"),
    }),
    "ascendant-hull-break": (-16.0, -2.0, -13.0,
                             f"{ASC}: the heart at 0, the chain of detonations running down the keel for 3 s, the "
                             "act boss's screen flash at 2.5 s (the game's `huge` rung with it); once a level",
                             "rapid secondary detonations and metal shearing, a deep boom at the flash", {
        "a": ([L(483284, 5.6, 1.0, gain=-4.0, fin=0.002, fout=0.3, hp=40.0),
               L(483284, 7.5, 0.9, gain=-5.0, at=0.75, speed=1.1, fin=0.002, fout=0.3, hp=40.0),
               L(483284, 1.0, 1.2, gain=-4.0, at=1.4, speed=0.95, fin=0.002, fout=0.4, hp=40.0),
               L(675480, 10.5, 2.4, gain=-7.0, at=0.2, speed=1.15, fin=0.1, fout=0.6, hp=80.0),
               L(587886, 0.0, 3.0, at=2.5, speed=0.8, fin=0.002, fout=1.8, hp=25.0, lp=1800.0)],
              "large blasts recorded in a row, cut into three bursts close together, a crushed car body's steel "
              "shearing under them, a large explosion slowed to 0.8 and darkened as the deep boom at 2.5 s: 5.5 s"),
        "b": ([L(244394, 0.06, 0.5, gain=-4.0, fin=0.002, fout=0.25, hp=40.0),
               L(366093, 0.06, 0.5, gain=-5.0, at=0.3, speed=1.1, fin=0.002, fout=0.25, hp=40.0),
               L(244394, 0.06, 0.5, gain=-6.0, at=0.55, speed=1.2, fin=0.002, fout=0.25, hp=40.0),
               L(366093, 0.06, 0.5, gain=-5.0, at=0.95, speed=0.95, fin=0.002, fout=0.25, hp=40.0),
               L(244394, 0.06, 0.5, gain=-5.0, at=1.25, speed=1.05, fin=0.002, fout=0.25, hp=40.0),
               L(366093, 0.06, 0.5, gain=-6.0, at=1.6, speed=1.2, fin=0.002, fout=0.25, hp=40.0),
               L(244394, 0.06, 0.6, gain=-4.0, at=1.95, speed=0.9, fin=0.002, fout=0.3, hp=40.0),
               L(118262, 16.4, 2.2, gain=-7.0, at=0.3, speed=1.1, fin=0.1, fout=0.6, hp=80.0),
               L(172379, 0.62, 3.0, at=2.44, fin=0.002, fout=1.8, hp=25.0)],
              "seven sharp metallic bangs (a metallic explosion and a manhole cover blown, taking turns at "
              "varied pitch), sheet steel bending and tearing under them, a bomb blast's deep boom with a long tail "
              "at 2.5 s: tighter, more metallic detonations than a, 5.45 s"),
    }),
    "ascendant-lifeboat": (-22.0, -4.0, -20.0,
                           f"{ASC}, phase 3: the lifeboat tearing free of the heart's cavity (K + 3 s) and climbing "
                           "to the top edge (14 s); its spore wake keeps the Vrell spores' built sound; once a level",
                           "a tearing rip, then a straining engine whine fading up and away; never wet", {
        "a": ([L(719003, 0.35, 1.0, speed=0.5, fin=0.005, fout=0.3, hp=80.0),
               L(491407, 1.0, 8.0, gain=-1.0, at=0.4, speed=(0.8, 1.5), fin=0.8, fout=5.0, hp=250.0)],
              f"{TEAR} slowed to 0.5, then a jet engine starting up, gliding up from 0.8 to 1.5 and fading out "
              "over its last 5 s: a straining roar climbing away, 8.4 s"),
        "b": ([L(719003, 0.35, 1.0, speed=0.5, fin=0.005, fout=0.3, hp=80.0),
               L(542582, 3.0, 8.0, gain=-1.0, at=0.4, speed=(0.85, 1.6), fin=0.8, fout=5.0, hp=300.0)],
              f"{TEAR} slowed to 0.5, then a tank's gas turbine spooling, gliding up from 0.85 to 1.6 and fading "
              "out over its last 5 s: a cleaner, higher whine than a's roar, 8.4 s"),
    }),
    "tender-strongbox": (-24.0, -5.0, -22.0,
                         "Level 42's secret: the dead tender's strongbox shot open (its housing takes 4 hits), over "
                         "the built crate break; once a level at most",
                         "a steel lockbox's hinges shearing", {
        "a": ([L(453333, 0.6, 0.55, speed=1.15, fin=0.02, fout=0.1, hp=200.0),
               L(516591, 0.09, 0.35, at=0.45, speed=1.1, fin=0.001, fout=0.15, hp=150.0),
               L(632487, 0.18, 0.4, gain=-3.0, at=0.5, fin=0.001, fout=0.2, hp=300.0),
               L(726486, 0.06, 0.7, gain=-7.0, at=0.6, speed=1.3, fin=0.001, fout=0.35, hp=200.0)],
              "a metal crank groaning under load (the hinge straining), a large bolt cutter's snap, a steel padlock "
              "shackle's clack and a light metal knock as the lid drops: 1.3 s"),
        "b": ([L(508281, 1.62, 0.55, speed=0.9, fin=0.02, fout=0.1, hp=200.0),
               L(513944, 6.70, 0.3, at=0.45, fin=0.001, fout=0.12, hp=150.0),
               L(587442, 3.08, 0.8, gain=-6.0, at=0.55, fin=0.005, fout=0.4, hp=150.0, lp=7000.0)],
              "an old truck door's hinge squeaking (higher and thinner than a's groan), a bolt cutter's snap, sheet "
              "metal dropping with a ring: 1.35 s"),
    }),
}


def sources_of(spec):
    return list(dict.fromkeys(layer["sound"] for layer in spec))


def build(subject, v):
    band, ceiling, cap, _, _, variants = SOUNDS[subject]
    spec, what = variants[v]
    name = f"{subject}-{R}-{v}"
    used = "; ".join(f'"{SOURCES[s][0]}", {SOURCES[s][1]}, {SOURCES[s][2]} (freesound {s})' for s in sources_of(spec))
    raw = mix(spec)
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


def level_text(subject):
    band, ceiling, cap = SOUNDS[subject][:3]
    return (f"levelled to {num(band)} dB (the loudest 100 ms of the 200 Hz–5 kHz band) under a {num(ceiling)} "
            f"dBFS ceiling, at most {num(cap)} LUFS")


def change_text(subject, spec, layer):
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
    if len(spec) > 1:
        parts.append("mixed with the other layers")
    parts.append(level_text(subject))
    parts.append("OGG Vorbis q6")
    return ", ".join(parts)


def licences(spec):
    return ", ".join(dict.fromkeys(SOURCES[s][2].replace("CC0 1.0", "CC0") for s in sources_of(spec)))


def docs(outdir, made):
    outdir = Path(outdir)
    outdir.mkdir(parents=True, exist_ok=True)
    credits, rows, prompts = [], [], []
    for subject, v, path in made:
        _, _, _, use, should, variants = SOUNDS[subject]
        spec, what = variants[v]
        name = f"{subject}-{R}-{v}"
        rel = f"design/audio/sfx/concept/{name}.ogg"
        for layer in spec:
            s = layer["sound"]
            title, author, lic, _ = SOURCES[s]
            url = f"https://freesound.org/people/{author}/sounds/{s}/"
            credits.append(f"| {rel} | {title} | {author} | [freesound]({url}) | [{lic}]({LICENCE_URL[lic]}) | "
                           f"{change_text(subject, spec, layer)} |")
        x = decode(path)
        secs = x.shape[1] / SR
        lv = short_term_band_db(x)
        size = f"{secs:.2f} s ({lv:.0f} dB)".replace("(-", "(−")
        edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
        srcs = sources_of(spec)
        names = " + ".join(f"{SOURCES[s][0]} ({SOURCES[s][1]})" for s in srcs)
        rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Freesound HQ preview ({licences(spec)}, "
                    f"[CREDITS.md](../../../CREDITS.md)) — `{subject}` {v}: plays: {use}; should sound like "
                    f"{should}. This one: {what}; {names}, {size} | proposed ({ROUND}) |")
        by = " + ".join(f'"{SOURCES[s][0]}" by {SOURCES[s][1]}' for s in srcs)
        links = ", ".join(f"<https://freesound.org/people/{SOURCES[s][1]}/sounds/{s}/> ({SOURCES[s][0]} by "
                          f"{SOURCES[s][1]}, {SOURCES[s][2]})" for s in srcs)
        label = "Sources" if len(srcs) > 1 else "Source"
        prompts.append(f"### {name} — {by}\n\nProposed ({ROUND}), cut from the HQ previews by `{SCRIPT}`.\n"
                       f"{label}: {links}.\nUse: {use}.\nBrief: {should}; a real recording, nothing bubbly or "
                       f"popping.\nThis one: {what}.\nEdit: {edit}.\n")
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
