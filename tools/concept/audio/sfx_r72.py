#!/usr/bin/env python3
"""Concept round 72 (M10 "Act 6 - Jovian Storm" design phase, step AUD-R): the Act 6 sounds the act shares, a and b
each (design/audio/sfx/README.md, *Act 6 sounds (draft)*): the shared hybrid set (the regrowth creak, the tissue tear),
the mirror's glassy ping, the Reflex Shield's ricochet, the Harrow's lock tell and burst, the lightning strike's tell and
strike, Io's eruption, the Shield Pylon's dome coming up and collapsing, the Graft Turret's regrowth and the four Act 6
beds (`jovian-sky`, `aurelia`, `io-refinery`, `callisto-fortress`). The rest come in their parts' rounds.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 72 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. The recordings are only cut, slowed, glided, layered, swelled, faded,
filtered, looped and levelled: nothing is synthesized. The hybrids' tissue is never wet (the enemies README's
regeneration rule): the creaks are leather, rope, wood and a ship's hull, the tears cloth and sailcloth. No bubbly or
popping sources (the Harrow's burst is a muffled blast with a firework mortar's crackle tail, its crack cut away). The
tools are sfx_r72.py's (copied: preview, bed, build, the --docs rows).

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each
variant is made of):

  regrowth-creak-r72-*     ≈ 1.2 s: a soft rising dry creak (leather or rope under load)
  tissue-tear-r72-*        ≈ 0.8 s: a dry fibrous tear (canvas, sailcloth)
  mirror-ping-r72-*        ≈ 0.4 s: a bright glassy ping (struck glass, a crystal bowl)
  reflex-ricochet-r72-*    ≈ 0.5 s: a bullet whining off steel
  harrow-lock-r72-*        0.6 s: a short rising whine (the lock tell)
  harrow-burst-r72-*       ≈ 0.9 s: a dull heavy thud with a crackle
  lightning-tell-r72-*     1.0 s: a faint high-voltage buzz rising
  lightning-strike-r72-*   ≈ 3.7 s: a close crack, then Level 12's thunder roll
  eruption-r72-*           ≈ 3.2 s: a dry rising ground rumble (the 1.2 s tell), then a volcanic blast's roar
  pylon-dome-up-r72-*      ≈ 0.45 s: an electrical hum rising (the dome back)
  pylon-dome-down-r72-*    ≈ 0.9 s: a falling hum into a hard electrical snap (the dome's collapse)
  graft-regrowth-r72-*     ≈ 1.8 s: a long twisting creak ending in a woody knock
  jovian-sky-r72-*         seamless 16 s LOOPs: high wind over a distant thunder bed (no strikes)
  aurelia-r72-*            seamless 16 s LOOPs: the floating city's machinery (engines, works, a far burner)
  io-refinery-r72-*        seamless 16 s LOOPs: furnace roar over a ground rumble
  callisto-fortress-r72-*  seamless 16 s LOOPs: fortress machinery heard through the hull

Levels like round 60's (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS cap; the
beds by their band RMS): the two variants of a subject share the level, so they are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r72.py [subject-or-file ...]   e.g. eruption harrow-burst-r72-b
       python3 tools/concept/audio/sfx_r72.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
SCRIPT = "tools/concept/audio/sfx_r72.py"
R = "r72"
ROUND = "round 72"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL). 710561 is pwigger0029's 28 s cut (CC0) of felix.blume's
# CC0 recording "Volcano Mount Yasur, Tanna Island" (freesound 661147, 30 min).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    30932: ('bullet ricochet.wav', 'aust_paul', CC0, P + '30/30932_15696-hq.ogg'),
    51429: ('volcano_eruption.wav', 'smcameron', BY4, P + '51/51429_322568-hq.ogg'),
    74915: ('ferry engine room2.wav', 'mrmayo', CC0, P + '74/74915_271213-hq.ogg'),
    95262: ('tree_creak_04.wav', 'Department64', CC0, P + '95/95262_861714-hq.ogg'),
    96631: ('Ricochet metal3.wav', 'CGEffex', BY4, P + '96/96631_1386366-hq.ogg'),
    130972: ('Large, Low Rumble.wav', 'duckduckpony', BY4, P + '130/130972_1653827-hq.ogg'),
    150454: ('Crystal bowl F#3 1.wav', 'caiogracco', CC0, P + '150/150454_2703701-hq.ogg'),
    161687: ('sw_Fireworks_Mortar_03.wav', 'jppi_Stu', BY4, P + '161/161687_56518-hq.ogg'),
    161688: ('sw_Fireworks_Mortar_02.wav', 'jppi_Stu', BY4, P + '161/161688_56518-hq.ogg'),
    191697: ('Muffled Boom', 'UnderlinedDesigns', CC0, P + '191/191697_2213158-hq.ogg'),
    193834: ('Door Knock - 18.wav', 'Ligidium', CC0, P + '193/193834_3276562-hq.ogg'),
    193863: ('Door Knock - 26.wav', 'Ligidium', CC0, P + '193/193863_3276562-hq.ogg'),
    203281: ('EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV', 'metrostock99', CC0, P + '203/203281_1256155-hq.ogg'),
    222521: ('Cracking Earthquake (cracking soil, cracking stone)', 'uagadugu', CC0, P + '222/222521_255863-hq.ogg'),
    243763: ('gas vuur dakwerken gasbrander luchtballon.wav', 'NickPeeters', CC0, P + '243/243763_877079-hq.ogg'),
    262510: ('Gym Lights Powering Up', 'OBXJohn', CC0, P + '262/262510_3719168-hq.ogg'),
    349688: ('070916 Machine room transformer hum 1.wav', 'thaighaudio', CC0, P + '349/349688_2205380-hq.ogg'),
    367451: ('Flash 1,2MV_1.wav', 'jensfelger', CC0, P + '367/367451_6750383-hq.ogg'),
    367453: ('Flash 1,2MV_3.wav', 'jensfelger', CC0, P + '367/367453_6750383-hq.ogg'),
    376534: ('Strong Wind Recorded with Soundfield Microphone Manchester 2', 'keirofinch', CC0, P + '376/376534_3256739-hq.ogg'),
    379738: ('Furnace Room 4.WAV', 'magnus589', BY4, P + '379/379738_2812229-hq.ogg'),
    411087: ('creaking sailing boat.wav', 'jimsim', BY4, P + '411/411087_130385-hq.ogg'),
    420538: ('Standing above a reactor', 'OSFX', BY4, P + '420/420538_7583126-hq.ogg'),
    420671: ('muffled_explosion.wav', 'SypherZent', BY4, P + '420/420671_6544098-hq.ogg'),
    434359: ('Close lightning strike 2018 07 07', 'csengeri', CC0, P + '434/434359_197070-hq.ogg'),
    439402: ('Industrial factory working 02.wav', 'dersinnsspace', CC0, P + '439/439402_8224400-hq.ogg'),
    455757: ('door wood old slow creaks open close or ship hull list2.flac', 'kyles', CC0, P + '455/455757_612689-hq.ogg'),
    520807: ('Engine-Room-door-018.wav', 'Astounded', BY4, P + '520/520807_10201334-hq.ogg'),
    520809: ('Engine-Room-Machine-020.wav', 'Astounded', BY4, P + '520/520809_10201334-hq.ogg'),
    542582: ('Start-up Sound - U.S. Army M1A1 Tank - Gas Turbine Engine.mp3', 'adr1911', CC0, P + '542/542582_10366986-hq.ogg'),
    573289: ('210523_1598_FR_StrongWind.wav', 'kevp888', BY4, P + '573/573289_9034501-hq.ogg'),
    578181: ('Airship propeller engine', 'ilm0player', CC0, P + '578/578181_8061009-hq.ogg'),
    591525: ('Cloth Rip Uneven Long', 'Rudmer_Rotteveel', CC0, P + '591/591525_4921277-hq.ogg'),
    674035: ('230104-044_normalized_ship_sail_rip_tear_1.wav', 'mmmtoop', CC0, P + '674/674035_11007330-hq.ogg'),
    675760: ('S16-18 Tesla coil sparking and humming.wav', 'craigsmith', CC0, P + '675/675760_2524442-hq.ogg'),
    710561: ('661147__felixblume__volcano-mount-yasur-tanna-island-short', 'pwigger0029', CC0, P + '710/710561_15406682-hq.ogg'),
    719003: ('Cloth Tearing and Ripping - Flannel, Cotton, Scraps - Close Mic - Long & Fast', 'ChristopherJngs', CC0, P + '719/719003_8154783-hq.ogg'),
    740198: ('Zapping - High Voltage Electricity Buzz Isolated', 'el_boss', CC0, P + '740/740198_9129912-hq.ogg'),
    745014: ('Glass Ping', 'tix99', CC0, P + '745/745014_2573767-hq.ogg'),
    752273: ('Low Roomtone', 'IENBA', CC0, P + '752/752273_9616576-hq.ogg'),
    797919: ('LETHRCreak_Leather Stretching Creak 01_KVV AUDIO_FREE', 'KVV_Audio', BY4, P + '797/797919_12846320-hq.ogg'),
    805169: ('Jet Flyby 02', 'mrspivey', CC0, P + '805/805169_17325486-hq.ogg'),
    840628: ('Closeup Thunder Strike 01', 'loganzsound', CC0, P + '840/840628_16682330-hq.ogg'),
    844244: ('Electric Failure – High Voltage Power Down #2 ATP2 (CC0)', 'ATP2-kh', CC0, P + '844/844244_18042200-hq.ogg'),
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


# ---- the synthesized mine blink (a signal: the README allows a synthesized electronic tick)

SYNTH = {}

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}); kind "shot": layers, levelled by the loudest 100 ms of the 200 Hz-5 kHz band
#             at most `cap` LUFS; "loop": (layers, length, xfade[, extra]), levelled by its band RMS at most `cap` LUFS.
BEDS = ("the level's ambience bed on the effects bus at the beds' 0.70, under the music; heard in the quiet stretches, "
        "a level's start and the pause before a boss")
SOUNDS = {
    "regrowth-creak": ("shot", -28.0, -8.0, -28.0,
                       "the shared hybrid set's healing sound: while a Chimera's HP regrows (repeated for the nearest "
                       "healing unit), a Graft Turret's regrowth tail and (an octave down) the Ascendant's sacs healing; "
                       "Levels 37-42, often but quiet, under the fire",
                       "a soft rising creak: stretched leather or a rope twisting under load, dry and close; never wet, "
                       "bubbly or squelching", {
        "a": ([L(797919, 0.3, 1.2, speed=0.85, fin=0.25, fout=0.3, hp=150.0, swell=(-10.0, 1.0))],
              "a leather strap stretched slowly, slowed to 0.85 and swelling up: a tight, grainy leather creak that "
              "rises, 1.2 s"),
        "b": ([L(411087, 3.4, 1.2, speed=0.9, fin=0.2, fout=0.3, hp=120.0, swell=(-8.0, 1.0))],
              "a sailing boat's rigging and hull creaking at the mooring, slowed to 0.9 and swelling up: a lower, "
              "woodier rope creak than a, 1.2 s"),
    }),
    "tissue-tear": ("shot", -22.0, -5.0, -22.0,
                    "the shared hybrid set's tear: a Graft Turret root's death, the Ascendant's brood splitting and "
                    "the lifeboat tearing free, Level 37's growth over the decks (drawn long); now and then",
                    "a dry fibrous tearing: heavy canvas or sailcloth ripping, rope fibres parting; never flesh, never "
                    "wet", {
        "a": ([L(719003, 0.35, 0.9, speed=0.6, fin=0.005, fout=0.25, hp=100.0)],
              "cotton cloth torn by hand, close, slowed to 0.6 so it reads as heavy canvas: a long dry rip, 0.9 s"),
        "b": ([L(674035, 1.45, 0.75, fin=0.01, fout=0.2, hp=150.0),
               L(591525, 1.6, 0.6, gain=-8.0, at=0.05, speed=0.7, fin=0.01, fout=0.2, hp=200.0)],
              "a ship's sail ripping, with a quick cloth rip slowed under it: rougher and wider than a, the fibres "
              "parting in bursts, 0.75 s"),
    }),
    "mirror-ping": ("shot", -26.0, -6.0, -26.0,
                    "every shot a Mirror Interceptor's mirror bounces (throttled: once per 0.08 s per unit) and, "
                    "lower, the Ascendant's panels; Levels 38-42, often in a mirror fight",
                    "a bright glassy ping: a struck glass or glass bowl, short; it must never sound like the Reflex "
                    "Shield's ricochet (glass, not a bullet)", {
        "a": ([L(745014, 0.0, 0.4, fin=0.001, fout=0.25, hp=400.0)],
              "a glass struck once: a clean high 'ping' with a short ring, 0.4 s"),
        "b": ([L(150454, 0.0, 0.45, speed=1.5, fin=0.001, fout=0.3, hp=300.0)],
              "a crystal singing bowl struck, sped to 1.5: a rounder, more bell-like glass tone than a, 0.45 s"),
    }),
    "reflex-ricochet": ("shot", -24.0, -5.0, -24.0,
                        "the Reflex Shield (Act 6's shield): each enemy bullet it turns into a bolt, throttled to once "
                        "per 0.06 s, over the shield hit; often while the player tanks fire",
                        "a real bullet ricochet (a rifle round whining off steel), bright and short: the player's side "
                        "of the mirrors, never the mirror's glassy ping", {
        "a": ([L(30932, 0.0, 0.45, fin=0.001, fout=0.25, hp=200.0)],
              "a bullet's ricochet: the hit and a thin whine flying off, 0.45 s"),
        "b": ([L(96631, 0.0, 0.5, speed=1.2, fin=0.001, fout=0.25, hp=200.0)],
              "a round glancing off metal, sped to 1.2: more of a metallic 'spang' than a's whine, 0.5 s"),
    }),
    "harrow-lock": ("shot", -24.0, -6.0, -24.0,
                    "the Harrow's 0.6 s lock tell (Levels 38-42): it slows, its sac pulses and its nose swings onto "
                    "the ship before the ram; the warning the player must hear",
                    "a short rising whine, thin and urgent, 0.6 s", {
        "a": ([L(805169, 0.4, 0.6, speed=(0.9, 1.5), fin=0.08, fout=0.06, hp=300.0, swell=(-10.0, 1.0))],
              "a jet's approach whine, glided up (0.9 → 1.5) and swelling: a thin rising jet scream, 0.6 s"),
        "b": ([L(542582, 2.8, 0.6, speed=(0.9, 1.6), fin=0.06, fout=0.06, hp=300.0, swell=(-10.0, 1.0))],
              "a tank's gas turbine spooling up, glided up (0.9 → 1.6) and swelling: a harder mechanical whine "
              "climbing, 0.6 s"),
    }),
    "harrow-burst": ("shot", -18.0, -3.0, -18.0,
                     "every Harrow death (shot, ramming, struck by lightning), instead of a size rung; Levels 38-42, "
                     "often in a swarm",
                     "a dull heavy thud with a crackle (a muffled blast, a firework mortar's thump); never a pop, never "
                     "wet", {
        "a": ([L(191697, 0.0, 0.9, fin=0.002, fout=0.4, hp=35.0),
               L(161688, 2.62, 0.8, gain=-8.0, at=0.04, fin=0.03, fout=0.4, hp=500.0)],
              "a muffled boom (a deep dull thud) with a firework shell's crackle tail over it (its burst crack cut "
              "away), 0.9 s"),
        "b": ([L(420671, 0.05, 0.9, fin=0.003, fout=0.4, hp=35.0),
               L(161687, 2.35, 0.7, gain=-9.0, at=0.05, fin=0.03, fout=0.35, hp=500.0)],
              "a muffled explosion (more body and mid than a's boom) with another mortar shell's crackle tail, "
              "0.9 s"),
    }),
    "lightning-tell": ("shot", -30.0, -8.0, -30.0,
                       "a lightning column's 1.0 s telegraph (Level 38, a few in Level 36): the strike lands where it "
                       "glows; quiet, under the fire",
                       "a faint high-voltage buzz rising (a Tesla coil, a power line's corona), its length the "
                       "telegraph's", {
        "a": ([L(675760, 4.0, 1.0, speed=(0.85, 1.15), fin=0.4, fout=0.03, hp=200.0, swell=(-14.0, 1.5))],
              "a Tesla coil sparking and humming, glided up (0.85 → 1.15) and swelling to the strike: a crackling "
              "buzz, 1.0 s"),
        "b": ([L(740198, 3.0, 1.0, speed=(0.8, 1.2), fin=0.4, fout=0.03, hp=200.0, swell=(-14.0, 1.5))],
              "a high-voltage buzz, glided up (0.8 → 1.2) and swelling: smoother and higher than a, more of a "
              "rising hum, 1.0 s"),
    }),
    "lightning-strike": ("shot", -14.0, -1.0, -16.0,
                         "a lightning strike (Level 38, a few in Level 36) as its telegraph ends; never two within "
                         "1.5 s; a big moment",
                         "a close crack (a real lightning strike recorded near: the sharp tearing crack, its first "
                         "0.3 s) with Level 12's chosen thunder (thunder-1-r34-b's source) keyed to it: the crack, then "
                         "the roll", {
        "a": ([L(434359, 3.10, 0.35, fin=0.002, fout=0.15, hp=60.0),
               L(840628, 0.62, 3.6, gain=-3.0, at=0.12, fin=0.01, fout=1.5, hp=30.0)],
              "a lightning strike recorded close in a storm, its crack, then Level 12's close thunder strike rolling "
              "away: a natural crack and roll, 3.7 s"),
        "b": ([L(367451, 0.0, 0.4, fin=0.001, fout=0.2, hp=80.0),
               L(840628, 0.62, 3.6, gain=-4.0, at=0.12, fin=0.01, fout=1.5, hp=30.0)],
              "a 1.2 MV flashover in a high-voltage lab (a sharper, more electric snap than a), then the same "
              "thunder roll, 3.7 s"),
    }),
    "eruption": ("shot", -16.0, -2.0, -17.0,
                 "Level 39 (Io): each eruption, its 1.2 s tell then the blast (the rocks' sounds are the belt's)",
                 "Level 27's tell and roar in a dry variant: a rising ground rumble, then a volcanic blast's roar; no "
                 "water, no steam hiss", {
        "a": ([L(222521, 0.0, 1.3, fin=0.05, fout=0.15, hp=30.0, lp=1500.0, swell=(-12.0, 1.0)),
               L(51429, 0.1, 2.0, at=1.2, fin=0.01, fout=0.9, hp=30.0)],
              "an earthquake's rumble swelling for 1.2 s (the tell), then a volcanic eruption's roar: dense and "
              "dry, 3.2 s"),
        "b": ([L(203281, 2.0, 1.3, speed=0.8, fin=0.1, fout=0.15, hp=25.0, lp=400.0, swell=(-14.0, 1.5)),
               L(710561, 11.9, 2.2, at=1.2, fin=0.01, fout=1.0, hp=30.0)],
              "a deep rumble (a distant shuttle or a quake, slowed) swelling for 1.2 s, then a real explosion of "
              "Mount Yasur: a rougher, rockier blast than a, 3.4 s"),
    }),
    "pylon-dome-up": ("shot", -28.0, -8.0, -28.0,
                      "a Shield Pylon's dome coming up (Level 41): at the level's start of a gate and after an EMP or "
                      "a `shield-breaker` hit, as the dome flickers back over 0.3 s",
                      "a short electrical hum rising, the tell that the dome is back", {
        "a": ([L(262510, 0.3, 0.45, speed=(0.8, 1.2), fin=0.15, fout=0.08, hp=150.0, swell=(-10.0, 1.0))],
              "a gym's lights powering up (the ballasts' hum and tick), glided up: a quick rising buzz, 0.45 s"),
        "b": ([L(349688, 5.0, 0.45, speed=(0.5, 1.0), fin=0.15, fout=0.08, hp=60.0, swell=(-12.0, 1.0)),
               L(740198, 6.0, 0.3, gain=-10.0, at=0.15, fin=0.1, fout=0.05, hp=1000.0)],
              "a machine room's transformer hum gliding up an octave (0.5 → 1.0) with a faint high-voltage buzz "
              "on its end: deeper and smoother than a, 0.45 s"),
    }),
    "pylon-dome-down": ("shot", -22.0, -5.0, -22.0,
                        "a Shield Pylon's dome collapsing (Level 41): the pylon's death, an EMP or a `shield-breaker` "
                        "hit; a reward",
                        "a falling hum into a hard electrical snap", {
        "a": ([L(844244, 0.55, 0.9, fin=0.005, fout=0.3, hp=100.0)],
              "a high-voltage power failure: a crackling discharge collapsing, 0.9 s"),
        "b": ([L(349688, 8.0, 0.45, speed=(1.0, 0.5), fin=0.01, fout=0.05, hp=50.0),
               L(367453, 0.0, 0.5, gain=-4.0, at=0.42, fin=0.001, fout=0.3, hp=200.0)],
              "the transformer hum falling an octave, cut by a 1.2 MV flash's hard snap: the brief's shape exactly, "
              "0.9 s"),
    }),
    "graft-regrowth": ("shot", -26.0, -6.0, -26.0,
                       "a Graft Turret regrowing on its root (Levels 39-42) as the turret stands again",
                       "a longer twisting creak that ends in a woody knock as the turret stands; dry", {
        "a": ([L(95262, 1.3, 1.4, speed=0.9, fin=0.2, fout=0.15, hp=120.0, swell=(-8.0, 1.0)),
               L(193834, 0.0, 0.35, gain=-2.0, at=1.4, fin=0.001, fout=0.2, hp=80.0)],
              "a tree creaking in the wind, slowed to 0.9 and swelling, then a single knock on a wooden door: 1.8 s"),
        "b": ([L(455757, 0.9, 1.5, speed=0.85, fin=0.2, fout=0.15, hp=80.0, swell=(-6.0, 1.0)),
               L(193863, 0.0, 0.4, gain=-3.0, at=1.45, speed=0.85, fin=0.001, fout=0.25, hp=60.0)],
              "an old wooden door's (or a ship hull's) slow creak, slowed to 0.85 (the source of round 36's chosen "
              "root growth), then a deeper door knock: lower and heavier than a, 1.9 s"),
    }),
    "jovian-sky": ("loop", -42.0, -1.0, -29.0,
                   f"Levels 36 and 38 (Jupiter's cloud bands): {BEDS}; Level 38 +3 dB in the storm sections",
                   "high wind and a distant thunder bed without baked strikes (the strikes' own thunder follows "
                   "them); a seamless 16 s loop", {
        "a": (([Loop(376534, 1.0, hp=80.0), Loop(203281, 2.0, gain=-10.0, speed=0.7, hp=25.0, lp=250.0)], 16.0, 1.0,
               {"events": [L(130972, 3.0, 9.0, gain=-8.0, at=5.0, speed=0.8, fin=2.0, fout=3.0, hp=25.0, lp=300.0)]}),
              "strong wind on a soundfield microphone (high, steady) over a deep rumble slowed to 0.7; once a loop a "
              "large low rumble swelling and fading (a far roll, no crack): 16.0 s loop"),
        "b": (([Loop(573289, 14.0, hp=60.0), Loop(203281, 10.0, gain=-8.0, speed=0.5, hp=25.0, lp=200.0)], 16.0,
               1.0,
               {"events": [L(130972, 4.0, 8.0, gain=-10.0, at=11.0, speed=0.7, fin=2.5, fout=3.0, hp=25.0,
                             lp=220.0)]}),
              "a gusting strong wind (lower and more uneven than a) over the deep rumble slowed to half; once a loop "
              "the far rumble, deeper: 16.0 s loop"),
    }),
    "aurelia": ("loop", -42.0, -1.0, -29.0,
                f"Level 37 (Aurelia, the floating capital): {BEDS}; the burning layer goes over it from the turn",
                "city machinery: the floating city's engines and works, distant; no voices, no alarms (they come "
                "with the burning layer); a seamless 16 s loop", {
        "a": (([Loop(578181, 0.5, speed=0.6, hp=40.0, lp=1500.0),
                Loop(439402, 22.0, gain=-6.0, hp=200.0, lp=2500.0)], 16.0, 1.0,
               {"events": [L(243763, 13.0, 3.5, gain=-12.0, at=7.0, fin=0.3, fout=1.2, hp=100.0, lp=2500.0)]}),
              "an airship's propeller engine slowed to 0.6 (big, slow engines holding the city up) over a factory "
              "working, muffled; once a loop a gas burner's roar far off (a lift balloon's): 16.0 s loop"),
        "b": (([Loop(520809, 3.0, speed=0.8, hp=40.0, lp=1800.0),
                Loop(578181, 0.5, gain=-4.0, speed=0.4, hp=30.0, lp=800.0)], 16.0, 1.0,
               {"events": [L(243763, 2.0, 4.0, gain=-11.0, at=3.0, fin=0.4, fout=1.5, hp=100.0, lp=2500.0)]}),
              "an engine room's machinery slowed to 0.8 over the propeller slowed to 0.4 (deeper, heavier than a); "
              "once a loop the far burner: 16.0 s loop"),
    }),
    "io-refinery": ("loop", -42.0, -1.0, -29.0,
                    f"Level 39 (Io's refineries): {BEDS}",
                    "ground rumble and furnace roar; dry (no water, no steam); a seamless 16 s loop", {
        "a": (([Loop(379738, 2.0, hp=40.0, lp=4000.0), Loop(203281, 5.0, gain=-6.0, speed=0.8, hp=25.0, lp=300.0)],
               16.0, 1.0,
               {"events": [L(130972, 3.0, 8.0, gain=-8.0, at=8.0, speed=0.9, fin=2.0, fout=2.5, hp=25.0,
                             lp=400.0)]}),
              "a furnace room's steady roar over a deep ground rumble; once a loop a large low rumble heaving up "
              "and away: 16.0 s loop"),
        "b": (([Loop(420538, 3.0, hp=30.0, lp=3000.0), Loop(379738, 20.0, gain=-8.0, speed=0.8, hp=40.0,
                                                            lp=2500.0)], 16.0, 1.0,
               {"events": [L(222521, 1.5, 3.0, gain=-10.0, at=4.0, speed=0.8, fin=0.3, fout=1.0, hp=60.0,
                             lp=2000.0),
                           L(243763, 1.0, 4.0, gain=-10.0, at=10.0, fin=0.5, fout=1.5, hp=80.0, lp=3000.0)]}),
              "standing above a reactor (a low industrial roar) with the furnace room under it; once a loop the "
              "ground cracking and a gas burner's roar: busier than a, 16.0 s loop"),
    }),
    "callisto-fortress": ("loop", -42.0, -1.0, -29.0,
                          f"Level 41 (Callisto's fortress decks; Levels 40 and 42 keep `belt-open`): {BEDS}",
                          "fortress machinery heard through the hull (the belt's vacuum treatment: no wind, sounds "
                          "as through the cockpit): deep machines, a transformer hum, hull creaks, a far heavy clunk "
                          "now and then; a seamless 16 s loop", {
        "a": (([Loop(74915, 10.0, speed=0.6, hp=60.0, lp=900.0), Loop(349688, 5.0, gain=-10.0, hp=60.0, lp=1500.0)],
               16.0, 1.0,
               {"events": [L(455757, 0.9, 3.0, gain=-12.0, at=4.0, speed=0.8, fin=0.3, fout=1.0, hp=80.0,
                             lp=2000.0),
                           L(191697, 0.0, 2.5, gain=-10.0, at=11.0, speed=0.8, fin=0.005, fout=1.0, hp=25.0,
                             lp=300.0)]}),
              "a ferry's engine room slowed to 0.6 and muffled (heard through a bulkhead) over a transformer hum; "
              "once a loop a slow hull creak and a far muffled boom: 16.0 s loop"),
        "b": (([Loop(520807, 7.0, speed=0.6, hp=60.0, lp=1000.0), Loop(752273, 10.0, gain=-4.0, hp=25.0, lp=400.0)],
               16.0, 1.0,
               {"events": [L(455757, 3.0, 3.0, gain=-12.0, at=2.5, speed=0.7, fin=0.3, fout=1.0, hp=80.0,
                             lp=1500.0),
                           L(193863, 0.0, 1.5, gain=-8.0, at=10.0, speed=0.5, fin=0.002, fout=0.8, hp=30.0,
                             lp=600.0)]}),
              "a ship's engine-room machinery slowed to 0.6 and muffled over a deep room tone; once a loop the hull "
              "creak and a door knock slowed to half (a far heavy clunk): darker than a, 16.0 s loop"),
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
        if kind == "synth":
            rows.append(f"| [concept/{name}.ogg](concept/{name}.ogg) | Synthesized by `{SCRIPT}` (no third-party "
                        f"source) — `{subject}` {v}: plays: {use}; should sound like {should}. This one: {what}, "
                        f"{size} | proposed ({ROUND}) |")
            edit = (f"{secs:.2f} s, the loudest 100 ms band {lv:.1f} dB, peak {db(np.abs(x).max()):.1f} dBFS, "
                    f"{lufs(x)[0]:.1f} LUFS").replace("-", "−")
            prompts.append(f"### {name} — synthesized\n\nProposed ({ROUND}), synthesized by `{SCRIPT}` (no "
                           f"third-party source).\nUse: {use}.\nBrief: {should}.\nThis one: {what}.\nEdit: {edit}.\n")
            continue
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
