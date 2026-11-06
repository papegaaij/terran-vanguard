#!/usr/bin/env python3
"""Concept round 90 (M11 part C, step AUD-CD, 2026-11-06; QC-1): the `vrell-hive` ambience bed, a and b
(design/audio/sfx/README.md, *Ambience (per setting)*). It plays under Level 45 for the whole level (later Levels 48 and
50), on the effects bus under the music: a slow, deep organic breathing under a faint dry creak of growing tissue; no
wet sound, no voices.

  vrell-hive-r90-a   seamless 16 s LOOP: a horse's breathing slowed to 0.44 and muffled (a big animal's slow, deep
                     breath, one every 8 s) over a faint low room tone, with three quiet leather and rope creaks
  vrell-hive-r90-b   seamless 16 s LOOP: a chimney's air draught slowed and muffled, swelling in and out every 8 s
                     (a cave breathing), over a faint low room tone, with two sparse leather creaks (less animal)

Built on sfx_r72.py's machinery with sfx_r89.py's `swells` (imported and pointed at this round: preview, loop, levels,
the --docs rows). Every layer is a Freesound sound (CC0) taken from its public HQ preview (cdn.freesound.org, cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg; found with an API-key search, no download quota used). The
recordings are only cut, slowed, filtered, looped, swelled and levelled; no bubbly or popping sources.

Levelled to -33.5 LUFS, the loudness of round 89's chosen `vrell-gate` a (and of the loudest Act 6 beds), so the
variants and the earlier beds are loudness-matched (a breath bed levelled by the Act 5-6 beds' -42 dB band RMS instead
would sit 5 dB under them; the subject's band level of -30 dB only guards, the loudness cap always applies).

Usage: python3 tools/concept/audio/sfx_r90.py [subject-or-file ...]
       python3 tools/concept/audio/sfx_r90.py --docs OUTDIR     also writes the CREDITS rows, the sfx README rows and
                                                                the prompts.md entries
Deterministic. Not listened to by Claude: checked by level, peaks, the swell's period and the loop seams.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import sfx_r72 as M  # noqa: E402
import sfx_r89  # noqa: E402,F401  (installs the `swells` bed, layers and CREDITS text into sfx_r72)

CC0 = "CC0 1.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    479677: ('R13-05-Horse Breathing.wav', 'craigsmith', CC0, P + '479/479677_2524442-hq.ogg'),
    454020: ('fireplace air rush into chimney steady wind current hum.flac', 'kyles', CC0,
             P + '454/454020_612689-hq.ogg'),
    559079: ('Foley_Leather_Stress_Mono.wav', 'Nox_Sound', CC0, P + '559/559079_9250976-hq.ogg'),
    596513: ('Rope Tightening.wav', 'EugeneEverett', CC0, P + '596/596513_10974703-hq.ogg'),
    734549: ('Leather - Creak, heavy jacket', 'Vrymaa', CC0, P + '734/734549_13973196-hq.ogg'),
    752273: ('Low Roomtone', 'IENBA', CC0, P + '752/752273_9616576-hq.ogg'),
}

L, Lp = M.L, M.Loop
BEDS = ("the bed of Level 45 for the whole level (later Levels 48 and 50), on the effects bus at the beds' 0.70, under "
        "the music")
SHOULD = ("a slow, deep organic breathing under a faint dry creak of growing tissue; no wet sound, no crackle of fluid, "
          "no voices; a seamless 16 s loop")

SOUNDS = {
    "vrell-hive": ("loop", -30.0, -1.0, -33.5, BEDS, SHOULD, {
        "a": (([Lp(479677, 0.3, speed=0.4375, hp=40.0, lp=1500.0),
                Lp(752273, 6.0, gain=-12.0, hp=30.0, lp=400.0)], 16.0, 1.0,
               {"events": [L(596513, 11.0, 2.5, gain=6.0, at=5.6, speed=0.7, fin=0.4, fout=0.8, hp=80.0, lp=3000.0),
                           L(559079, 2.6, 2.5, gain=5.0, at=13.4, speed=0.6, fin=0.4, fout=0.8, hp=60.0, lp=3000.0),
                           L(559079, 9.4, 2.0, gain=0.0, at=2.0, speed=0.6, fin=0.4, fout=0.8, hp=60.0,
                             lp=3000.0)]}),
              "a horse's breathing slowed to 0.44 and muffled (a big animal's slow, deep breath over an octave down, "
              "one every 8 s) over a faint low room tone; a rope tightening and a slow leather creak in the pauses "
              "and a fainter leather creak under a breath, each once a loop: 16.0 s loop"),
        "b": (([Lp(752273, 6.0, gain=-6.0, hp=30.0, lp=400.0)], 16.0, 1.0,
               {"events": [L(734549, 7.8, 2.0, gain=8.0, at=6.5, speed=0.7, fin=0.4, fout=0.8, hp=60.0, lp=3000.0),
                           L(559079, 12.6, 2.0, gain=10.0, at=14.6, speed=0.6, fin=0.4, fout=0.8, hp=60.0,
                             lp=3000.0)],
                "swells": ([Lp(454020, 1.0, speed=0.6, hp=30.0, lp=700.0)], 8.0, 14.0)}),
              "a chimney's air draught slowed to 0.6 and muffled, swelling in and out every 8 s (a deep cave "
              "breathing, not an animal), over a faint low room tone; two sparse slow leather creaks a loop: 16.0 s "
              "loop"),
    }),
}

M.SOURCES, M.SOUNDS, M.SYNTH = SOURCES, SOUNDS, {}
M.SCRIPT, M.R, M.ROUND = "tools/concept/audio/sfx_r90.py", "r90", "round 90"


def level_text(subject):
    return f"levelled to {M.num(SOUNDS[subject][3])} LUFS (round 89's chosen `vrell-gate` a)"


M.level_text = level_text

if __name__ == "__main__":
    M.main(sys.argv[1:])
