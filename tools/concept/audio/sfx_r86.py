#!/usr/bin/env python3
"""Concept round 86 (M11 "Act 7 - Beyond the Gate" design phase, step SFX-R86, 2026-11-05): the Choir Resonator's
sounds re-taken, a and b each: its fire (one wave front, 2.5-3.5 a second) and the hit where a wave passes through a
hull (throttled; the game plays each next hull of a wave a step lower). Round 85's glass, bowl and tuning-fork sounds
were turned down ("these do not read like weapons at all", "like children's toys"): these read as a weapon first, a
hard punchy attack with weight like the other front guns' fire, the Vrell resonance at most a short low dark tail.

Built on sfx_r72.py's machinery (imported and pointed at this round: preview, cut, mix, levels, the --docs rows), as
sfx_r84.py and sfx_r85.py. Every layer is a real recording from Freesound (CC0 or CC-BY) taken from its public HQ
preview (cdn.freesound.org, cached in ~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key
search, no download quota used): a tank's and an autocannon's shot, a high-voltage discharge, a close thunder crack,
a whoosh, steel struck and torn. The recordings are only cut, slowed, layered, faded, filtered and levelled. No glass,
bowls, forks or bells; nothing bubbly or popping; nothing wet.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo), a and b each (the SOUNDS table says what each is).
Levels like round 84's player-side sounds (the loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a LUFS
cap): the fire at the heavier front guns' level (the built Vulcan, Lance and autocannon shots' loudest 100 ms sit at -21
to -23 dB), the hit 5 dB under it; the two variants of a subject share the level. The fire's body is gone inside
0.25 s and the whole shot inside 0.34 s, so 0.29-0.4 s between waves leaves a gap before the next.

Usage: python3 tools/concept/audio/sfx_r86.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r86.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, envelope, spectrum and length against the fire rate.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
from sfx_r43g import L  # noqa: E402

CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    168707: ('Tank Firing', 'qubodup', CC0, P + '168/168707_71257-hq.ogg'),
    386130: ('Anvil - Lokomo 125 kg - Hammer on 6mm steel 1 time short', 'ldezem', CC0,
             P + '386/386130_1655965-hq.ogg'),
    752078: ('heavy_metal_hit_w_loud_ringout_noises_01_08242024', 'Artninja', BY4, P + '752/752078_10996917-hq.ogg'),
    403252: ('Taser/High Voltage discharge', 'The_Chemical_Workshop', CC0, P + '403/403252_7809148-hq.ogg'),
    732992: ('Huge Thunder Crack [XY + MS]', '-CASK-', BY4, P + '732/732992_13779022-hq.ogg'),
    434869: ('Powerfull Whooshes 2.wav', 'Nic3_one', CC0, P + '434/434869_1358210-hq.ogg'),
    423117: ('Guns & Explosions Album - Metal impact 5.wav', 'OGsoundFX', BY4, P + '423/423117_3325582-hq.ogg'),
    669692: ('designed metal shearing, ripping.wav', 'vestibule-door', CC0, P + '669/669692_14100561-hq.ogg'),
    244394: ('Bang/Explosion Metallic', 'Werra', CC0, P + '244/244394_525427-hq.ogg'),
    702908: ('ELECSprk_Electrical Discharges With Sparking.Steady 2_EM.mp3', 'newlocknew', BY4,
             P + '702/702908_5828667-hq.ogg'),
}

FIRE_USE = ("the Choir Resonator's fire (front gun, on sale from Level 44): one per wave front, 2.5 a second at L1, 3 at "
            "L5, 3.5 in overdrive, so the most-heard sound of a Resonator fit")
FIRE_SHOULD = ("a weapon first: a punchy, hard shot with weight at the level of the other front guns (Vulcan, Lance), "
               "from real recordings (a cannon's thump, an electric discharge's crack, a pressure wave), the Vrell "
               "resonance at most a short low dark tail under it; the body gone inside 0.25 s so three a second never "
               "tire; no glass, bowls, forks or bells")
HIT_USE = ("the Choir Resonator's wave passing through a hull (each hull it meets, throttled to one per 0.12 s; the "
           "game plays each next hull of a wave 6 % lower, as the wave steps down)")
HIT_SHOULD = "a short hard impact, the wave tearing through metal; under the fire, never a ring or a chime"

# subject -> (kind, level, ceiling dBFS, cap LUFS, where/when it plays, what it should sound like,
#             {variant: (spec, what)}), as sfx_r72.py's SOUNDS
SOUNDS = {
    "resonator-fire": ("shot", -22.0, -6.0, -22.0, FIRE_USE, FIRE_SHOULD, {
        "a": ([L(168707, 0.015, 0.26, speed=0.9, fin=0.002, fout=0.17, hp=40.0, lp=7000.0),
               L(386130, 0.0, 0.07, gain=-5.0, speed=0.75, fin=0.001, fout=0.05, hp=250.0, lp=7000.0),
               L(752078, 0.12, 0.3, gain=-11.0, at=0.03, speed=0.6, fin=0.02, fout=0.2, hp=90.0, lp=1400.0)],
              "\"the pulse cannon\": a tank gun's blast slowed to 0.9 for a deep thump, a hammer on steel slowed to "
              "0.75 for its hard metal edge, and a heavy metal hit's ring-out slowed to 0.6 and darkened as a short "
              "low ring under it: a heavy 'DHOOM-mm', 0.33 s"),
        "b": ([L(403252, 2.255, 0.06, fin=0.001, fout=0.04, hp=300.0),
               L(732992, 1.04, 0.24, gain=-3.0, fin=0.002, fout=0.15, hp=50.0, lp=6000.0),
               L(434869, 4.6, 0.2, gain=-6.0, at=0.01, speed=1.25, fin=0.01, fout=0.12, hp=150.0),
               L(168707, 0.015, 0.2, gain=-8.0, fin=0.002, fout=0.13, hp=30.0, lp=250.0)],
              "\"the discharge\": a high-voltage spark's crack on the front, a close thunder crack's blast under it, "
              "a fast whoosh sped to 1.25 for the pressure wave and a tank gun's low thump beneath: a hard, "
              "splitting 'KSSHHAK' with a push of air, 0.24 s"),
    }),
    "resonator-hit": ("shot", -27.0, -10.0, -27.0, HIT_USE, HIT_SHOULD, {
        "a": ([L(423117, 0.045, 0.2, fin=0.001, fout=0.12, hp=80.0, lp=8000.0),
               L(669692, 1.22, 0.12, gain=-7.0, at=0.02, fin=0.005, fout=0.07, hp=700.0)],
              "a heavy metal impact cut short over a burst of metal shearing: a hard 'KLANG-shk', 0.2 s"),
        "b": ([L(244394, 0.03, 0.18, fin=0.001, fout=0.11, hp=150.0, lp=6000.0),
               L(702908, 0.1, 0.1, gain=-5.0, fin=0.002, fout=0.06, hp=1500.0)],
              "a hollow metallic bang cut short with a spit of electric sparks: a duller, deeper 'DUNK-tsk' than a, "
              "0.18 s"),
    }),
}

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r86.py", "r86", "round 86"

if __name__ == "__main__":
    M.main(sys.argv[1:])
