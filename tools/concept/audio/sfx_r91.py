#!/usr/bin/env python3
"""Concept round 91 (M11 part D, step AUD-CD, 2026-11-06; QD-1): the `dead-fleet` ambience bed, a and b
(design/audio/sfx/README.md, *Ambience (per setting)*). It plays under Level 46 for the whole level and alone through
its 24 s quiet (the music out): a low, empty space drone with the slow creak and tick of cold hulls, very sparse; no
wind, no organic sound, no hum.

  dead-fleet-r91-a   seamless 24 s LOOP: a low room tone slowed and muffled (an empty hull's drone) with three slow
                     creaks a loop, one every 7.5-8.5 s: stressed metal, lake ice groaning, metal groaning
  dead-fleet-r91-b   seamless 24 s LOOP: near silence, a very low hull tone (the room tone slowed to half, deeply
                     muffled) with five far, faint metal ticks (a cooling engine's) and one slow far creak a loop

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, loop, levels, the --docs rows), as
sfx_r89.py and sfx_r90.py are. Every layer is a Freesound sound (CC0 or CC-BY) taken from its public HQ preview
(cdn.freesound.org, cached in ~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no
download quota used). The recordings are only cut, slowed, filtered, looped, placed and levelled; no bubbly or popping
sources (the frozen-lake recordings with water in them were left out).

Levelled to -33.5 LUFS, the loudness of round 89's chosen `vrell-gate` a, as sfx_r90.py's `vrell-hive` (the subject's
band level of -30 dB only guards, the loudness cap always applies), so the beds are loudness-matched.

Usage: python3 tools/concept/audio/sfx_r91.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r91.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, the events' spacing and the loop seams.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
import sfx_r89  # noqa: E402,F401  (installs the `swells` bed, layers and CREDITS text into sfx_r72)

CC0, BY4 = "CC0 1.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    172407: ('Creaking Metal - With Metallic Reverb', 'EagleStealthTeam', CC0, P + '172/172407_2915633-hq.ogg'),
    172411: ('Creaking Metal - Slow', 'EagleStealthTeam', CC0, P + '172/172411_2915633-hq.ogg'),
    380403: ('Metal Creaks_Groans.wav', 'Cell31_Sound_Productions', BY4, P + '380/380403_3571756-hq.ogg'),
    539514: ('Clicking Engine Cooldown', 'CmdRobot', CC0, P + '539/539514_214099-hq.ogg'),
    737896: ('Ice - Lake fractures', 'Vrymaa', CC0, P + '737/737896_13973196-hq.ogg'),
    752273: ('Low Roomtone', 'IENBA', CC0, P + '752/752273_9616576-hq.ogg'),
}

L, Lp = M.L, M.Loop
BEDS = ("the bed of Level 46 for the whole level, alone through its 24 s quiet (the music out), on the effects bus at "
        "the beds' 0.70")
SHOULD = ("a low, empty space drone with the slow creak and tick of cold hulls (real stressed metal and ice, slowed), "
          "very sparse: long stretches of near nothing between the creaks; no wind, no organic sound, no hum; a "
          "seamless 24 s loop")


def tick(start, at, gain):
    """One far tick of the cooling engine: 0.5 s from just before it, slowed to 0.8, thinned and muffled."""
    return L(539514, start, 0.5, gain=gain, at=at, speed=0.8, fin=0.01, fout=0.3, hp=300.0, lp=2500.0)


SOUNDS = {
    "dead-fleet": ("loop", -30.0, -1.0, -33.5, BEDS, SHOULD, {
        "a": (([Lp(752273, 6.0, speed=0.8, hp=30.0, lp=400.0),
                Lp(752273, 30.0, gain=-4.0, speed=0.5, hp=25.0, lp=150.0)], 24.0, 1.0,
               {"events": [L(172411, 2.0, 4.0, gain=4.0, at=1.5, speed=0.6, fin=0.5, fout=1.5, hp=60.0, lp=2500.0),
                           L(737896, 23.2, 3.0, gain=4.0, at=9.0, speed=0.6, fin=0.3, fout=1.2, hp=40.0, lp=1200.0),
                           L(380403, 18.3, 3.0, gain=4.0, at=17.0, speed=0.6, fin=0.4, fout=1.0, hp=35.0,
                             lp=3000.0)]}),
              "a low room tone slowed to 0.8 and under it to half, muffled (an empty hull's low drone); three slow "
              "creaks a loop, one every 7.5-8.5 s: stressed metal slowed to 0.6, lake ice groaning slowed to 0.6 and "
              "muffled, metal groaning slowed to 0.6: 24.0 s loop"),
        "b": (([Lp(752273, 30.0, speed=0.5, hp=25.0, lp=200.0)], 24.0, 1.0,
               {"events": [tick(85.38, 1.2, 0.0), tick(88.32, 4.9, -4.0), tick(90.55, 9.4, -2.0),
                           tick(96.50, 17.8, -5.0), tick(101.48, 21.1, -2.0),
                           L(172407, 0.4, 4.0, gain=6.0, at=12.0, speed=0.5, fin=0.5, fout=1.5, hp=40.0, lp=1500.0)]}),
              "near silence: a very low hull tone (the room tone slowed to half, deeply muffled); five far, faint "
              "metal ticks (a cooling engine's, slowed to 0.8 and muffled) and once a loop a slow far metal creak "
              "(slowed to half, muffled): 24.0 s loop"),
    }),
}

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r91.py", "r91", "round 91"


def level_text(subject):
    return f"levelled to {M.num(SOUNDS[subject][3])} LUFS (round 89's chosen `vrell-gate` a)"


M.level_text = level_text

if __name__ == "__main__":
    M.main(sys.argv[1:])
