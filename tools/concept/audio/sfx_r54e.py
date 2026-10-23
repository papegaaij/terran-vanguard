#!/usr/bin/env python3
"""Concept round 54 (M8 part E, step AUD-E): Level 25's sounds. The Siren's song (the sac's tell swelling into the
sonar ring), the slow on the ship (a falling tone and a hull shimmer) and the Siren's death; the Spiral Nautilus's shell
spin (a seamless loop), its uncoil and its death (the shell cracking); the `europa-trench` bed. All cut from real
recordings in the Freesound database (round 48's rules: heavy and big, muffled and pressured under water, no bubbly,
popping or squelching sounds, no low-pass on the sfx bus: each layer carries its own filters). Reused, no row: the
Barbfin's needle (round 53), the Glassfin's shatter and the wall scrape under water (round 52), the Nautilus's spiral
shots (the enemy small shot), the survey sub wreck in the left cave (the *Meridian*'s hatch, round 53); the caves have
no sound of their own (a cave wave's 3 s warning is the edge warning every side wave has). The Sirens' "song drone" of
the music text is the ring's own sung sweep (gap check E10), so there is no music stem here.

Every layer is a Freesound sound (CC0 or CC-BY, in CREDITS.md) taken from its public HQ preview (cdn.freesound.org,
OGG ~192 kbps; found with an API-key search, no download quota used), cached in
~/.cache/terran-vanguard/freesound-previews/<id>-hq.ogg and fetched from the CDN when missing (no credentials needed).
The originals of the user's picks are fetched after round 54 (freesound_fetch.py --download --ids ...) and the picks
rebuilt from them with the same settings. This script only cuts, slows, glides, layers, fades, filters, loops and
levels them; nothing is synthesized. The tools are sfx_r53d.py's (copied: preview, build, the --docs rows). Pending
the user's review in round 54; the game plays the a variants until the picks.

Outputs (design/audio/sfx/concept/; OGG Vorbis q6, 44.1 kHz stereo):

  siren-ring-r54-a       Tuvan throat singing (kargyraa) slowed to 0.62 and sliding down to 0.5, swelling over the
                         0.8 s tell and fading as the ring spreads, over a deep-sea rumble (2.8 s)
  siren-ring-r54-b       a Tibetan bowl's rubbed ring slowed to 0.8 and sliding down to 0.6, swelling and fading
                         the same, over a deep-sea rumble (2.8 s)
  siren-slow-r54-a       a hand-cranked siren's note slowed to 0.5 and sliding down to 0.28, with a bowed cymbal's
                         high shimmer over it (2.0 s)
  siren-slow-r54-b       a bowed cymbal's ring sliding down from 0.9 to 0.45, over an electro-mechanical fire
                         siren winding down, slowed to 0.6 (2.0 s)
  siren-death-r54-a      the throat singing slowed and falling away (0.6 to 0.25) over a depth bomb's boom (2.2 s)
  siren-death-r54-b      a humpback whale's call (a hydrophone recording) slowed and falling (0.75 to 0.45) over a
                         depth bomb's boom (2.2 s)
  nautilus-spin-r54-a    a seamless 4 s LOOP: a water mill's millstone grinding (recorded inside the stone) slowed
                         to 0.6, over a cement mixer's empty drum turning, slowed to 0.5
  nautilus-spin-r54-b    a seamless 4 s LOOP: a wheeled bin rolled over pavement (a boulder roll) slowed to 0.5,
                         over a rotary rock tumbler slowed to 0.5
  nautilus-uncoil-r54-a  a stone sarcophagus lid sliding, slowed to 0.7, with shells scraping marble under it (0.7 s)
  nautilus-uncoil-r54-b  a stone slab door grinding open, slowed to 0.6, over a heavy stone door's low drag (0.75 s)
  nautilus-death-r54-a   a big clay pot smashed, slowed to 0.65 and muffled, over a depth bomb's boom (1.8 s)
  nautilus-death-r54-b   a large sheet of ice cracking underfoot, slowed to 0.6, on a coconut's hollow knock slowed
                         to 0.5, over a depth bomb's boom (1.8 s)
  europa-trench-r54-a    a seamless 16 s LOOP: the deep-sea rumble slowed to 0.75 (lower), rock and concrete
                         scraping slowed to 0.5 far off, humpback calls far off slowed to 0.7
  europa-trench-r54-b    a seamless 16 s LOOP: a dark cave drone (layered thunder-drum recordings) slowed to 0.8, a
                         heavy stone door dragging slowed to 0.6 far off, a blue whale's B call far off

Levels like the files they stand beside (their loudest 100 ms of the 200 Hz-5 kHz band under a ceiling, at most a
LUFS cap; the round-46/47 imports as the scale): the ring a few dB under the Abyss Ray's rise (ray-rise-r53-a; it
sounds every 4.5 s a nest, up to four nests), the slow like the hydro-kit refit (hydro-refit-r52-a) a little under,
the Siren's and the Nautilus's death like the reef's (reef-death-r53-a) a little under (the `medium` under-water burst
plays with them), the spin loop like the siphon pump's hum (pump-hum-r52-b), the uncoil like the reef's iris
(reef-iris-r53-a), the bed like `europa-under` (europa-under-r52-a). Each layer is peak-normalised before its gain.

Usage: python3 tools/concept/audio/sfx_r54e.py [subject-or-file ...]   e.g. siren-ring nautilus-spin-r54-b
       python3 tools/concept/audio/sfx_r54e.py --docs OUTDIR            also writes the CREDITS.md rows, the sfx README
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
SCRIPT = "tools/concept/audio/sfx_r54e.py"
R = "r54"
ROUND = "round 54"
CACHE = Path.home() / ".cache" / "terran-vanguard" / "freesound-previews"

# Freesound sound id -> (title, author, licence, HQ preview URL).
CC0, BY3, BY4 = "CC0 1.0", "CC-BY 3.0", "CC-BY 4.0"
P = "https://cdn.freesound.org/previews/"
SOURCES = {
    19517: ("alfonso_low_01.wav", "Bram", BY4, P + "19/19517_2-hq.ogg"),
    193822: ("Deep Sea Ambience", "jhumbucker", CC0, P + "193/193822_208303-hq.ogg"),
    417115: ("Tibetan bowl_rubbing rim.wav", "dersinnsspace", CC0, P + "417/417115_8224400-hq.ogg"),
    239498: ("Hand cranked siren.wav", "mikewest", CC0, P + "239/239498_109726-hq.ogg"),
    372202: ("drone6_bowed_cymbal1.wav", "decembered", CC0, P + "372/372202_454894-hq.ogg"),
    372206: ("drone8_bowed_cymbal2.wav", "decembered", CC0, P + "372/372206_454894-hq.ogg"),
    715561: ("Real electro-mechanical fire siren", "AudioPapkin", CC0, P + "715/715561_8698658-hq.ogg"),
    147879: ("DepthBomb_06.aif", "ZeSoundResearchInc.", CC0, P + "147/147879_2101444-hq.ogg"),
    147880: ("DepthBomb_05.aif", "ZeSoundResearchInc.", CC0, P + "147/147880_2101444-hq.ogg"),
    797762: ("humpback_whales_and_orcas_ambient_underwater_recording", "raiden04", BY4,
             P + "797/797762_17082821-hq.ogg"),
    342821: ("Water mill - the inside of a grind stone", "borralbi", BY4, P + "342/342821_2774517-hq.ogg"),
    407295: ("cement mixer small empty turning with horrible grinding metal friction on and offmic1 from front.wav",
             "kyles", CC0, P + "407/407295_612689-hq.ogg"),
    823071: ("Trash Can Wheels Rolling", "Filmscore", CC0, P + "823/823071_843915-hq.ogg"),
    763459: ("Tumbler Rotary (Bryte) -low Speed - 1", "FOSSarts", CC0, P + "763/763459_14631530-hq.ogg"),
    457529: ("Opening the sarcophagus", "Breviceps", CC0, P + "457/457529_9159316-hq.ogg"),
    800116: ("shells and quartz crystals delicately scraping marble and tapping ceramic plate.wav", "CVLTIV8R", CC0,
             P + "800/800116_2520418-hq.ogg"),
    844329: ("Stone Slab Door Grinding - Heavy Rock Scrape (Mono)", "NahuelMartinez", CC0, P + "844/844329_17689204-hq.ogg"),
    578490: ("Heavy stone door opens", "PostProdDog", CC0, P + "578/578490_6911631-hq.ogg"),
    810042: ("Breaking pot low", "anna_bliss", CC0, P + "810/810042_17435303-hq.ogg"),
    113679: ("Breaking_large_ice.wav", "CGEffex", BY4, P + "113/113679_1386366-hq.ogg"),
    136494: ("FOL_IMPACT_DROP_COCONUT_HIT_RODENTG3_JACKMENHORN.wav", "JackMenhorn", BY4, P + "136/136494_154322-hq.ogg"),
    715671: ("130525 rock stone moving scraping concrete shifting tectonic plate, 77mel", "TRP", CC0,
             P + "715/715671_97550-hq.ogg"),
    421826: ("Dark Cave Drone", "Kinoton", CC0, P + "421/421826_2247456-hq.ogg"),
    343682: ("Blue Whale B call @ 5X", "MBARI_MARS", BY4, P + "343/343682_6181234-hq.ogg"),
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
#             at most `cap` LUFS; "loop": (layers, length, xfade), levelled by its band RMS at most `cap` LUFS.
SIREN_USE = ("Level 25 (and Levels 26-27), each Siren nest in the trench walls: every 4.5 s a nest (6 s on easy, 3.6 s "
             "on hard), up to four nests on the screen, their rings at least 1.5 s apart")
NAUTILUS_USE = "Level 25 on, each Spiral Nautilus (five pairs in Level 25, orbiting the ship in opposite directions)"
SOUNDS = {
    "siren-ring": ("shot", -24.0, -6.0, -21.0,
                   f"{SIREN_USE}: from the sac's 0.8 s teal tell through the ring spreading out (120 px/s to 230 px, "
                   "about 1.9 s); shooting the sac during the tell ends it (the Sirens' 'song drone' of the music "
                   "text is this sound)",
                   "a soft, heavy, low sweep, sung rather than pinged: a deep voice-like tone swelling over the tell, "
                   "loudest as the ring leaves the sac, then spreading and sinking away; muffled, no bubbles, no "
                   "sonar ping", {
        "a": ([L(19517, 5.0, 2.8, speed=(0.62, 0.5), fin=0.8, fout=1.6, hp=40.0, lp=1200.0),
               L(193822, 30.0, 2.8, gain=-10.0, fin=0.6, fout=1.2, hp=25.0)],
              "Tuvan throat singing (kargyraa, a real singer's growling low drone with its overtones) slowed to 0.62 "
              "and sliding down to 0.5 and muffled at 1.2 kHz, swelling in over the 0.8 s tell and fading as the "
              "ring spreads, over a deep-sea rumble: a deep sung 'hmmmoooaa' from the wall, 2.8 s"),
        "b": ([L(417115, 17.2, 2.8, speed=(0.8, 0.6), fin=0.8, fout=1.5, hp=40.0, lp=2000.0),
               L(193822, 30.0, 2.8, gain=-10.0, fin=0.6, fout=1.2, hp=25.0)],
              "a Tibetan bowl's rubbed rim ring (a long sustained sung tone, 276 Hz) slowed to 0.8 and sliding "
              "down to 0.6 (about 220 to 165 Hz) and muffled at 2 kHz, swelling and fading the same over a "
              "deep-sea rumble: purer and rounder than a, a sung 'ooooom' rather than a voice, 2.8 s"),
    }),
    "siren-slow": ("shot", -23.0, -5.0, -19.0,
                   "Level 25 on, each time a Siren's ring slows the ship (60 % speed and acceleration for 2 s, 1.5 s "
                   "on easy; then 3 s immune): once, as the ring's band passes the ship, with the sonar shimmer on "
                   "the hull",
                   "the ship dragged down: a falling tone, as of the hull's engines losing power, with a metallic "
                   "shimmer on the hull over it, about 2 s; muffled, nothing that pops", {
        "a": ([L(239498, 7.0, 2.0, speed=(0.5, 0.28), fin=0.03, fout=0.8, hp=40.0, lp=1500.0),
               L(372202, 1.0, 2.0, gain=-10.0, fin=0.05, fout=0.8, hp=1500.0, lp=7000.0)],
              "a rural fire brigade's hand-cranked siren at the top of its note, slowed to 0.5 and sliding down to "
              "0.28 (its tone falling about an octave) and muffled at 1.5 kHz, with a bowed cymbal's high shimmer "
              "over it: a dropping 'wooouuuh' with a glassy ring, 2.0 s"),
        "b": ([L(372206, 1.5, 2.0, speed=(0.9, 0.45), fin=0.03, fout=0.8, hp=80.0, lp=4000.0),
               L(715561, 16.2, 2.0, gain=-6.0, speed=0.6, fin=0.05, fout=0.8, hp=40.0, lp=900.0)],
              "a bowed cymbal's ring sliding down from 0.9 to 0.45 (the metal's own tone falling) over an "
              "electro-mechanical fire siren winding down between two wails, slowed to 0.6 and muffled at 900 Hz: "
              "more metal and less engine than a, a sinking 'shiiiing', 2.0 s"),
    }),
    "siren-death": ("shot", -20.0, -4.0, -16.0,
                    f"{SIREN_USE}: a Siren killed (90 HP, its throat sac shot), with the under-water `medium` burst, "
                    "as the sac bursts and the nest goes dark; its song stops",
                    "the song choked off: the sung tone falling away and dying over a deep soft boom, about 2 s, "
                    "muffled; no squeal, no splash, no bubbles", {
        "a": ([L(19517, 6.0, 2.2, speed=(0.6, 0.25), fin=0.01, fout=1.0, hp=40.0, lp=1200.0),
               L(147879, 0.0, 2.2, gain=-6.0, fin=0.004, fout=1.1, hp=25.0)],
              "the throat singing of ring a slowed from 0.6 to 0.25 so it sinks and dies, muffled, over a depth "
              "bomb's sub boom: the Siren's own voice choked off, 2.2 s; pairs with ring a"),
        "b": ([L(797762, 199.2, 2.2, speed=(0.75, 0.45), fin=0.02, fout=1.0, hp=50.0, lp=1200.0),
               L(147880, 0.0, 2.2, gain=-9.0, fin=0.004, fout=1.1, hp=25.0)],
              "a humpback whale's call (a real hydrophone recording) slowed from 0.75 to 0.45 so it falls away "
              "(about 170 down to 95 Hz), muffled, over a depth bomb's boom: more of an animal's dying moan than "
              "a, 2.2 s"),
    }),
    "nautilus-spin": ("loop", -34.0, -1.0, -27.0,
                      f"{NAUTILUS_USE}: while one is on the screen, as its shell spins round the ship (one quiet "
                      "loop for all of them, the nearest; it stops with the last)",
                      "a heavy rolling grind: a big stone shell turning, a rough slow rumble with grit in it, low "
                      "in the mix, no clicks, a seamless loop", {
        "a": (([Loop(342821, 20.0, speed=0.6, hp=30.0, lp=900.0),
                Loop(407295, 10.5, gain=-6.0, speed=0.5, hp=30.0, lp=1000.0)], 4.0, 0.5),
              "a water mill's millstone grinding (recorded inside the stone; a mill from 1863) slowed to 0.6 and "
              "muffled at 900 Hz (its feeder's taps dulled), over a small cement mixer's empty drum turning, slowed to 0.5 and muffled: a "
              "steady deep stone grind, 4.0 s loop"),
        "b": (([Loop(823071, 2.0, speed=0.5, hp=30.0, lp=1000.0),
                Loop(763459, 3.0, gain=-8.0, speed=0.5, hp=60.0, lp=2500.0)], 4.0, 0.5),
              "a wheeled bin rolled over pavement (a boulder-roll foley) slowed to 0.5 and muffled at 1 kHz, over a "
              "rotary rock tumbler slowed to 0.5: rougher and more of a roll than a, rocks churning in a drum, "
              "4.0 s loop"),
    }),
    "nautilus-uncoil": ("shot", -22.0, -5.0, -18.0,
                        f"{NAUTILUS_USE}: each uncoil, every cycle (about 5 s) at the start of its 0.6 s opening, "
                        "with the 0.4 s crown tell before it fires its spiral",
                        "a shell scraping open: a heavy stone-on-shell scrape, dry and gritty, about 0.6 s; nothing "
                        "bubbly, no squelch", {
        "a": ([L(457529, 2.75, 0.7, speed=0.7, fin=0.04, fout=0.2, hp=60.0, lp=2500.0),
               L(800116, 3.0, 0.7, gain=-8.0, speed=0.8, fin=0.03, fout=0.2, hp=300.0, lp=4000.0)],
              "a heavy stone sarcophagus lid sliding, slowed to 0.7 and muffled at 2.5 kHz, with shells and quartz "
              "scraping marble quietly under it: a gritty stone-and-shell scrape, 0.7 s"),
        "b": ([L(844329, 5.0, 0.75, speed=0.6, fin=0.01, fout=0.25, hp=50.0, lp=2000.0),
               L(578490, 2.0, 0.75, gain=-6.0, speed=0.6, fin=0.03, fout=0.25, hp=40.0, lp=1200.0)],
              "a stone slab door grinding open, slowed to 0.6 and muffled at 2 kHz, over a heavy stone door's low "
              "drag: deeper and harder at the start than a, more stone than shell, 0.75 s"),
    }),
    "nautilus-death": ("shot", -20.0, -4.0, -16.0,
                       f"{NAUTILUS_USE}: one killed (100 HP; the closed shell glances off all but the specials and "
                       "the anti-sub weapons), with the under-water `medium` burst, as the shell cracks into two "
                       "halves that sink and the crown goes dark",
                       "a big shell cracking: a hard, hollow, brittle crack and the halves breaking away, heavy and "
                       "muffled, about 1.5 s; no splash, nothing that pops", {
        "a": ([L(810042, 1.38, 1.8, speed=0.65, fin=0.003, fout=0.8, hp=60.0, lp=3000.0),
               L(147879, 0.0, 1.8, gain=-8.0, fin=0.004, fout=1.0, hp=25.0)],
              "a big clay pot smashed with a hammer (a hit, then the pot breaking apart), slowed to 0.65 and muffled "
              "at 3 kHz, over a depth bomb's boom: a hollow ceramic crack and pieces breaking away, 1.8 s"),
        "b": ([L(113679, 8.84, 1.8, speed=0.6, fin=0.003, fout=0.8, hp=60.0, lp=2500.0),
               L(136494, 14.6, 0.8, gain=-4.0, speed=0.5, fin=0.003, fout=0.5, hp=50.0, lp=2000.0),
               L(147880, 0.0, 1.8, gain=-3.0, fin=0.004, fout=1.0, hp=25.0)],
              "a large sheet of ice cracking underfoot (meaty cracks and snaps) slowed to 0.6 and muffled, on a "
              "coconut's hollow knock on concrete slowed to 0.5, over a depth bomb's boom: sharper and more "
              "splintering than a, 1.8 s"),
    }),
    "europa-trench": ("loop", -36.0, -1.0, -28.0,
                      "Level 25 throughout (and Level 26 at the scarp): the ambience bed of the Siren Trench, in place "
                      "of `europa-under`",
                      "the under-water bed lower and closer: a deep cut's heavy rumble close round the ship, far rock "
                      "grinding now and then and distant whale calls; quiet, under the music, no bubbles, no drips, a "
                      "seamless loop", {
        "a": (([Loop(193822, 20.0, speed=0.75, hp=20.0, lp=1200.0),
                Loop(715671, 43.3, gain=-12.0, speed=0.5, hp=40.0, lp=700.0),
                Loop(797762, 214.0, gain=-16.0, speed=0.7, hp=100.0, lp=1200.0)], 16.0, 1.0),
              "the deep-sea rumble of `europa-under` slowed to 0.75 (lower and heavier), rock and concrete scraping "
              "and shifting slowed to 0.5 and muffled at 700 Hz far off, and humpback whales calling far off (a "
              "real hydrophone recording, other calls than `europa-under`'s) slowed to 0.7: 16.0 s loop"),
        "b": (([Loop(421826, 10.0, speed=0.8, hp=20.0, lp=1200.0),
                Loop(578490, 1.0, gain=-12.0, speed=0.6, hp=40.0, lp=600.0),
                Loop(343682, 3.0, gain=-14.0, speed=0.5, hp=50.0, lp=1000.0)], 16.0, 1.0),
              "a dark cave drone (layered thunder-drum recordings, a low rumble with slow air swells) slowed to 0.8, "
              "a heavy stone door dragging slowed to 0.6 and muffled at 600 Hz far off, and a blue whale's B call "
              "(a deep-sea hydrophone recording at 900 m, played at 2.5 times its speed) far off: more of a closed "
              "cave than a, two long moans a loop, 16.0 s loop"),
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
