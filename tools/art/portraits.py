#!/usr/bin/env python3
"""Production art: the speakers' portraits of Acts 1-2 (design/story/characters, design/ui/briefing,
design/ui/hud), in the chosen style B with retained colour (round 04) and the round-08 extras.

Outputs (assets/sprites/portraits/, packed onto the shared sprite pages as ``portraits/<name>``):
  radio-<slug>-<expression>.png      72x72 radio portrait (HUD), for okafor, rook, varga in
                                     neutral, grim and fierce; generic-cdf and generic-civilian
                                     in neutral; generic-colonial in neutral (Ares Control, the
                                     colony's controller of Act 3; M7 part B batch, round 36's
                                     chosen b "dust-suit controller", tools/concept/portraits_r36.py)
  briefing-<slug>-<expression>.png   144x144 briefing portrait (the 72 px portrait at 2x with CRT
                                     scanlines), for the briefing speakers okafor, rook (from
                                     Act 2: Level 08's hangar teaser, round 30) and varga
  radio-the-choir-neutral_0..31.png  the Choir's interference glyph, a 32-frame loop (12 fps)
  radio-conamara-neutral.png         72x72, Dr. Ilse Brandt, Conamara Station's director (Levels 22-23, the
                                     speaker `conamara`; M8 part B batch, round 51, straight to production):
                                     a woman in her late fifties, dark ash bob with a side fringe, tired
                                     eyes, a quilted red polar parka with a high collar and an amber station
                                     patch, a headset with a boom mic; lit by the dome control room's
                                     sodium lamps (a warm amber rim from the right) with the dome's ribs,
                                     two sodium lamps and the consoles' screens behind her
  radio-thera-deep-neutral.png       72x72, Thera Deep Control, the under-ice city's harbour controller (Level 23 on,
                                     the speaker `thera-control`, portrait `radio-thera-deep`; M8 part C batch,
                                     round 52, straight to production): a calm, heavy-set man in his fifties, olive
                                     skin, short dark hair and a close dark beard, the city's grey-blue
                                     harbour coverall with a pressure suit's steel neck ring and a headset; lit by
                                     the control room's warm grow-lamp light from the left with a cool teal rim from
                                     the round viewport behind him (dark water, a lamp-lit farm rack in it, the
                                     consoles' screens below)
  design/story/characters/concept/portraits-final-r13-a.png/.gif   review sheet and loop
  design/story/characters/rook/concept/rook-briefing-portrait-final-r30-a.png   Rook's briefing
                                     portraits (round 30), next to his radio portraits and the
                                     other briefing speakers'
  design/story/characters/concept/portraits-final-r38-a.png   the colonial generic's review
                                     (round 38): 1x, 2x, 4x and a line-up with the other generics
  design/story/characters/concept/portraits-final-r51-a.png   Dr. Brandt's review (round 51): 1x, 2x,
                                     4x and a line-up with the other minor speakers
  design/story/characters/concept/portraits-final-r52-a.png   Thera Deep Control's review (round 52), the same
                                     layout

The busts are the concept SDF busts (tools/concept/portraits_r03.py, the generic speakers of
portraits_r08.py), imported unchanged; the expressions replace their face with this script's
``face``, the concept face with expression parameters: **grim** lowers the head a little, lowers
and draws in the inner brows, narrows the eyes and pulls the mouth's corners down; **fierce**
slants the brows hard, opens the eyes wide and opens the mouth (a shout, the upper teeth showing). Neutral is the concept face
unchanged. The style-B treatment is round 04's (portraits_r04.style_b2: the channel tint over the
character's own colours, soft ordered dither, outline, interference rows, a faint channel glow),
split so that **one 36-colour palette** serves all expressions of a character and an expression
change never shifts its colours; the interference rows are seeded per character, so they stay put
too. The Choir is round 08's glyph (portraits_r08.choir_frame), all 32 frames of its loop.

Vorne does not speak in Acts 1-2 and gets his portraits with Act 6.

Run: python3 tools/art/portraits.py [speaker ...] [--review]   (~1 min; with speaker slugs, e.g.
generic-colonial, only their portraits; --review only rebuilds the sheets)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from contextlib import contextmanager

import numpy as np
from PIL import Image

import artkit
from artkit import ROOT, SPRITES, sprite

import portraits_r03 as r03  # noqa: E402  (concept scripts, imported unchanged)
import portraits_r04 as r04  # noqa: E402
import portraits_r08 as r08  # noqa: E402  (registers the generic speakers in r03.CAST)
import portraits_r36 as r36  # noqa: E402  (registers the colonial generic's variants in r03.CAST)
from render import raster, sdf  # noqa: E402

SCRIPT = "portraits.py"
SOURCE = artkit.source_note(SCRIPT, "UI batch")
ROUND = "r13"
OUT = SPRITES / "portraits"
CONCEPT = ROOT / "design" / "story" / "characters" / "concept"
N = r04.N
EXPRESSIONS = ("neutral", "grim", "fierce")
# speaker slug -> (expressions, sizes): the main cast in three expressions, the minor speakers
# neutral; the briefing size only for who speaks in a briefing of Acts 1-2
SPEAKERS = {
    "okafor": (EXPRESSIONS, ("radio", "briefing")),
    "rook": (EXPRESSIONS, ("radio", "briefing")),
    "varga": (EXPRESSIONS, ("radio", "briefing")),
    "generic-cdf": (("neutral",), ("radio",)),
    "generic-civilian": (("neutral",), ("radio",)),
    "generic-colonial": (("neutral",), ("radio",)),
    "conamara": (("neutral",), ("radio",)),
    "thera-deep": (("neutral",), ("radio",)),
}
# M7 part B: the colonial generic is round 36's chosen variant under the game's slug
r03.CAST["generic-colonial"] = dict(r03.CAST[f"generic-colonial-{r36.CHOSEN}"])
# the batch each speaker was made in (the Source note); the UI batch's are left out
BATCHES = {"generic-colonial": "M7 part B batch", "conamara": "M8 part B batch", "thera-deep": "M8 part C batch"}
CONAMARA_ROUND = "r51"
THERA_ROUND = "r52"
COLONIAL_ROUND = "r38"
CONCEPT_COLONIAL = CONCEPT
CHOIR = "the-choir"
# the briefing speakers as round 13 approved them: its review sheet stays as it was
R13_BRIEFING = ("okafor", "varga")
ROOK_ROUND = "r30"
ROOK_CONCEPT = ROOT / "design" / "story" / "characters" / "rook" / "concept"

# Face parameters per expression, as offsets on the concept face (world units; the head is about
# 0.6 across, 1 unit = 48 px at 72 px): the brows' inner and outer ends up (+) or down (-), the
# inner ends drawn towards the nose, the brows' radius, the upper and lower lids up (+) or down,
# and the head's extra pitch (+ lowers it: grim looks out from under the brows).
FACES = {
    "neutral": dict(brow_inner=0.0, brow_outer=0.0, brow_in=0.0, brow_r=0.013, lid=0.0, lower=0.0,
                    eyes=1.0, mouth=None, pitch=0.0),
    "grim": dict(brow_inner=-0.045, brow_outer=0.004, brow_in=0.016, brow_r=0.024, lid=-0.016, lower=0.008,
                 eyes=0.7, mouth="frown", pitch=0.07),
    "fierce": dict(brow_inner=-0.06, brow_outer=0.03, brow_in=0.02, brow_r=0.026, lid=0.01, lower=-0.002,
                   eyes=1.25, mouth="shout", pitch=0.03),
}


# --------------------------------------------------------------------------- Dr. Ilse Brandt (Conamara)

SKIN, EYE, IRIS, LIPS, HAIR, CLOTH, ACCENT, METAL, GLOW, EXTRA, BROW = range(11)
SODIUM = (255, 168, 56)


def brandt():
    """Dr. Ilse Brandt, Conamara Station's director (L22-23): a tired, precise woman in her late fifties; an
    dark ash bob going grey with a side fringe, a headset with a boom mic, a quilted red polar station parka with a high
    collar and an amber station patch, the inner fleece showing at the throat."""
    mats = [
        sdf.Material((0.84, 0.68, 0.6), shininess=16, spec=0.2),           # fair skin
        sdf.Material((0.86, 0.84, 0.8), shininess=60, spec=0.5),
        sdf.Material((0.16, 0.22, 0.28), shininess=90, spec=0.9),          # grey-blue iris
        sdf.Material((0.62, 0.38, 0.36), shininess=24, spec=0.25),
        sdf.Material((0.46, 0.42, 0.38), shininess=14, spec=0.18, pattern=r03.noise_pattern(220, 0.22, 4)),  # dark ash
        sdf.Material((0.66, 0.16, 0.1), shininess=12, spec=0.15, pattern=r03.noise_pattern(60, 0.1, 5)),  # red parka
        sdf.Material((0.95, 0.6, 0.16), shininess=18, spec=0.25),          # amber station patch
        sdf.Material((0.22, 0.22, 0.24), metal=0.5, shininess=60, spec=0.6),  # headset
        sdf.Material((0.1, 0.1, 0.1), emission=(1.0, 0.55, 0.15)),         # on-air LED (sodium)
        sdf.Material((0.74, 0.72, 0.66), shininess=6, spec=0.05, pattern=r03.noise_pattern(150, 0.18, 2)),
        sdf.Material((0.56, 0.54, 0.5), shininess=8, spec=0.08),           # grey brows
    ]

    def scene(p):
        q = r03.turn(p, -0.2, 0.04)                    # three-quarter, as the other speakers
        items, skin = r03.face(q, jaw=0.9, mouth="smile")
        hair = sdf.sd_ellipsoid(q, (0, 0.5, -0.05), (0.33, 0.38, 0.34))
        hair = np.maximum(hair, -(q[:, 1] - 0.56 + 0.42 * np.maximum(q[:, 2] - 0.02, 0)))
        bob = sdf.sd_ellipsoid(q, (0, 0.38, -0.07), (0.315, 0.17, 0.27))  # the bob down to the jaw
        bob = np.maximum(bob, q[:, 2] - 0.12)
        hair = sdf.smin(hair, bob, 0.08)
        fringe = sdf.sd_ellipsoid(q, (0.1, 0.63, 0.2), (0.22, 0.08, 0.12))    # swept to her left
        fringe = np.maximum(fringe, q[:, 1] - 0.72 - 0.25 * (q[:, 0] - 0.1))
        hair = sdf.smin(hair, fringe, 0.06)
        band = np.maximum(r03.sd_torus_z(q, (0, 0.44, -0.04), 0.355, 0.02), -q[:, 1] + 0.44)
        cup = sdf.sd_cylinder_x(q, (-0.34, 0.42, -0.02), 0.07, 0.03)
        boom = sdf.sd_capsule(q, (-0.35, 0.38, 0.03), (-0.24, 0.27, 0.2), 0.011)
        mic = sdf.sd_sphere(q, (-0.235, 0.265, 0.2), 0.02)
        led = sdf.sd_sphere(q, (-0.36, 0.39, 0.05), 0.013)
        pb = r03.turn(p, -0.08)
        neck, torso = r03.body(pb, neck_r=0.115, shoulders=0.84)
        collar = sdf.sd_cylinder_y(pb, (0, -0.07, -0.04), 0.25, 0.08)
        collar = np.maximum(collar, -sdf.sd_cylinder_y(pb, (0, -0.07, -0.04), 0.17, 0.3))
        collar = np.maximum(collar, -(pb[:, 2] - 0.06 - 0.9 * np.abs(pb[:, 0])))   # open at the front
        fleece = sdf.sd_cylinder_y(pb, (0, -0.06, -0.03), 0.17, 0.07)
        quilt = [sdf.sd_ellipsoid(pb, (s * 0.5, -0.3, -0.02), (0.24, 0.1, 0.23)) for s in (-1, 1)]
        patch = sdf.sd_box(pb, (0.42, -0.4, 0.22), (0.07, 0.05, 0.02), 0.012)
        zip_ = sdf.sd_box(pb, (0.0, -0.5, 0.24), (0.012, 0.32, 0.01), 0.004)
        return sdf.union(*items, (hair, HAIR), (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH),
                         (np.minimum(*quilt), CLOTH), (fleece, EXTRA), (collar, CLOTH), (patch, ACCENT),
                         (zip_, METAL), (band, METAL), (cup, METAL), (boom, METAL), (mic, METAL), (led, GLOW))
    return scene, mats


r03.CAST["conamara"] = dict(build=brandt, name="DR. I. BRANDT", role="CONAMARA STATION",
                            key=(-1.4, 2.2, 3.2), rim=(2.6, 0.9, -1.0), rim_col=(1.0, 0.6, 0.2),
                            bg=((18, 10, 4), (90, 52, 14)), fill_col=(1.0, 0.66, 0.3), channel="cdf",
                            exposure=1.15)


def conamara_room(ramp):
    """The radio portrait's background for Brandt: the sodium-lit dome control room, drawn in the channel's
    dark tones: the dome's curved ribs, two sodium lamps (warm amber with a small halo) and a row of console
    screens low behind her shoulders."""
    yy, xx = np.mgrid[0:N, 0:N].astype(np.float64)
    bg = np.zeros((N, N, 3)) + ramp[0] * 255
    for cx in (-30.0, 36.0, 102.0):                    # the dome's ribs: arcs converging overhead
        r = np.hypot(xx - cx, (yy + 40) * 0.9)
        bg[np.abs(r - 78) < 1.0] = ramp[1] * 255 * 0.9
    bg[(yy > 7) & (yy < 9)] = ramp[1] * 255 * 0.6      # a ring beam
    for lx, ly in ((10, 14), (60, 10)):                # the sodium lamps
        d = np.hypot(xx - lx, yy - ly)
        halo = np.clip(1 - d / 9, 0, 1) ** 1.5
        bg = bg * (1 - halo[..., None] * 0.55) + np.array(SODIUM) * halo[..., None] * 0.55
        bg[d < 1.6] = (255, 226, 160)
    for k, x0 in enumerate(range(2, N, 13)):           # consoles: screens low on the wall
        lit = ramp[3] if k % 2 else ramp[2]
        bg[52:57, x0:x0 + 8] = lit * 255 * 0.7
        bg[57:58, x0:x0 + 8] = np.array(SODIUM) * 0.35
    return bg


ROOMS = {"conamara": conamara_room}


# --------------------------------------------------------------------------- Thera Deep Control (M8 part C)

GROW = (255, 206, 132)
TEAL_W = (90, 230, 210)


def thera_controller():
    """Thera Deep Control, the city's harbour controller (L23 on): a calm, heavy-set man in his fifties, olive skin,
    dark hair cropped short, a close dark beard along the jaw with a moustache; the grey-blue
    harbour coverall with a pressure suit's steel neck ring and the city's amber shoulder tab, a headset with a boom
    mic and a teal on-air lamp."""
    mats = [
        sdf.Material((0.66, 0.47, 0.34), shininess=14, spec=0.18),           # olive skin
        sdf.Material((0.86, 0.84, 0.8), shininess=60, spec=0.5),
        sdf.Material((0.16, 0.1, 0.06), shininess=90, spec=0.9),             # brown iris
        sdf.Material((0.5, 0.3, 0.26), shininess=24, spec=0.25),
        sdf.Material((0.17, 0.16, 0.16), shininess=6, spec=0.05, pattern=r03.noise_pattern(260, 0.45, 4)),  # dark, greying
        sdf.Material((0.3, 0.34, 0.42), shininess=14, spec=0.2, pattern=r03.noise_pattern(70, 0.08, 5)),    # coverall
        sdf.Material((0.95, 0.62, 0.18), shininess=18, spec=0.25),           # the city's amber shoulder tab
        sdf.Material((0.5, 0.52, 0.54), metal=0.7, shininess=80, spec=0.8),  # the neck ring, headset
        sdf.Material((0.1, 0.1, 0.1), emission=(0.3, 1.0, 0.8)),             # on-air lamp (teal)
        sdf.Material((0.18, 0.2, 0.22), shininess=8, spec=0.08),             # the ring's dark seal
        sdf.Material((0.16, 0.15, 0.14), shininess=8, spec=0.08),            # dark brows
    ]

    def scene(p):
        q = r03.turn(p, 0.2, 0.02)                     # three-quarter, turned the other way from Brandt
        items, skin = r03.face(q, jaw=1.1, mouth="neutral")
        hair = sdf.sd_ellipsoid(q, (0, 0.51, -0.06), (0.3, 0.36, 0.33))
        hair = np.maximum(hair, -(q[:, 1] - 0.57 + 0.38 * np.maximum(q[:, 2], 0)))   # a slightly receding hairline
        hair = np.maximum(hair, -sdf.sd_ellipsoid(q, (0, 0.40, 0.25), (0.26, 0.2, 0.2)))
        beard = sdf.sd_ellipsoid(q, (0, 0.25, 0.06), (0.245, 0.16, 0.27))
        beard = np.maximum(beard, q[:, 1] - 0.36 + 0.25 * np.maximum(q[:, 2] - 0.15, 0))  # up the cheeks, not the face
        beard = np.maximum(beard, -sdf.sd_ellipsoid(q, (0, 0.272, 0.3), (0.08, 0.03, 0.08)))  # the lips show
        moust = sdf.sd_capsule(q, (-0.075, 0.296, 0.295), (0.075, 0.296, 0.295), 0.017)
        band = np.maximum(r03.sd_torus_z(q, (0, 0.46, -0.03), 0.33, 0.02), -q[:, 1] + 0.46)
        cup = sdf.sd_cylinder_x(q, (0.32, 0.44, -0.02), 0.075, 0.03)
        boom = sdf.sd_capsule(q, (0.33, 0.4, 0.03), (0.12, 0.29, 0.27), 0.012)
        mic = sdf.sd_sphere(q, (0.11, 0.285, 0.275), 0.022)
        led = sdf.sd_sphere(q, (0.34, 0.41, 0.05), 0.013)
        pb = r03.turn(p, 0.08)
        neck, torso = r03.body(pb, neck_r=0.14, shoulders=0.9)
        ring_p = np.stack([pb[:, 0], pb[:, 2] + 0.03, pb[:, 1] + 0.06], axis=-1)
        ring = r03.sd_torus_z(ring_p, (0, 0, 0), 0.21, 0.045)
        seal = sdf.sd_cylinder_y(pb, (0, -0.07, -0.03), 0.2, 0.025)
        tab = sdf.sd_box(pb, (-0.52, -0.2, 0.1), (0.1, 0.05, 0.04), 0.012)
        pocket = sdf.sd_box(pb, (0.3, -0.5, 0.25), (0.09, 0.07, 0.015), 0.01)
        return sdf.union(*items, (hair, HAIR), (beard, HAIR), (moust, HAIR), (sdf.smin(neck, skin, 0.05), SKIN),
                         (torso, CLOTH), (seal, EXTRA), (ring, METAL), (tab, ACCENT), (pocket, CLOTH),
                         (band, METAL), (cup, METAL), (boom, METAL), (mic, METAL), (led, GLOW))
    return scene, mats


r03.CAST["thera-deep"] = dict(build=thera_controller, name="THERA DEEP CONTROL", role="THERA DEEP HARBOUR",
                              key=(-1.6, 2.0, 3.0), rim=(2.4, 1.2, -1.2), rim_col=(0.2, 0.5, 0.46),
                              bg=((2, 14, 18), (16, 60, 66)), fill_col=(1.0, 0.8, 0.52), channel="cdf",
                              exposure=1.1)


def thera_room(ramp):
    """The radio portrait's background for Thera Deep Control: the harbour control room under the ice: a round
    viewport behind him (its steel rim, the dark water, a lamp-lit farm rack and kelp in it), warm grow-lamp light
    on the wall from the left, the consoles' screens low behind his shoulders."""
    yy, xx = np.mgrid[0:N, 0:N].astype(np.float64)
    bg = np.zeros((N, N, 3)) + ramp[0] * 255
    vx, vy, vr = 50.0, 20.0, 19.0                      # the viewport, up to the right
    d = np.hypot(xx - vx, yy - vy)
    inside = d < vr
    water = np.array((6, 40, 46), float) + np.array((10, 30, 30), float) * np.clip(1 - (yy - vy + vr) / (2 * vr), 0, 1)[..., None]
    bg = np.where(inside[..., None], water, bg)
    rack = inside & (np.abs(yy - (vy + 7)) < 1.2) & (np.abs(xx - vx) < 15)
    bg[rack] = np.array(GROW) * 0.55
    for lx in range(int(vx) - 12, int(vx) + 13, 6):    # the rack's grow lamps
        dl = np.hypot(xx - lx, yy - (vy + 5))
        halo = np.clip(1 - dl / 4.5, 0, 1) ** 1.6 * inside
        bg = bg * (1 - halo[..., None] * 0.6) + np.array(GROW) * halo[..., None] * 0.6
    for kx in (vx - 9, vx + 3, vx + 11):               # kelp fronds rising
        kelp = inside & (np.abs(xx - kx - 2 * np.sin(yy / 4.0)) < 0.8) & (yy > vy - 4)
        bg[kelp] = (20, 70, 52)
    rim = (np.abs(d - vr) < 1.6)
    bg[rim] = np.array((120, 140, 146), float) * np.where((xx - vx + yy - vy) < 0, 1.0, 0.55)[rim][..., None]
    for k in range(8):                                  # rim bolts
        a = k * np.pi / 4
        bx, by = int(round(vx + np.cos(a) * vr)), int(round(vy + np.sin(a) * vr))
        if 0 <= bx < N and 0 <= by < N:
            bg[by, bx] = (190, 200, 200)
    warm = np.clip(1 - xx / 40, 0, 1) ** 1.4 * np.clip(1 - np.abs(yy - 24) / 40, 0, 1)
    bg = np.where(inside[..., None], bg, bg * (1 - warm[..., None] * 0.45) + np.array(GROW) * 0.32 * warm[..., None])
    for k, x0 in enumerate(range(2, N, 13)):           # consoles: screens low on the wall
        lit = ramp[3] if k % 2 else ramp[2]
        bg[54:58, x0:x0 + 8] = lit * 255 * 0.7
        bg[58:59, x0:x0 + 8] = np.array(TEAL_W) * 0.3
    return np.clip(bg, 0, 255)


ROOMS["thera-deep"] = thera_room


# --------------------------------------------------------------------------- the expressive face

def face(q, skin_k=0.06, jaw=1.0, mouth="neutral", eyes_open=1.0, expression="neutral"):
    """portraits_r03.face with an expression: the same head, the brows, lids and mouth moved by
    ``FACES[expression]``; the caller's mouth stays for neutral (Rook's grin, Vorne's smile)."""
    SKIN, EYE, IRIS, LIPS, BROW = r03.SKIN, r03.EYE, r03.IRIS, r03.LIPS, r03.BROW
    f = FACES[expression]
    mouth = f["mouth"] or mouth
    eyes_open *= f["eyes"]
    head = sdf.sd_ellipsoid(q, (0, 0.47, -0.02), (0.29, 0.37, 0.32))
    jawd = sdf.sd_ellipsoid(q, (0, 0.30, 0.03), (0.215 * jaw, 0.21, 0.25))
    parts = [
        (sdf.sd_ellipsoid(q, (-0.15, 0.40, 0.17), (0.085, 0.06, 0.08)), 0.05),
        (sdf.sd_ellipsoid(q, (0.15, 0.40, 0.17), (0.085, 0.06, 0.08)), 0.05),
        (sdf.sd_capsule(q, (0, 0.49, 0.29), (0, 0.375, 0.365), 0.028, 0.042), 0.03),
        (sdf.sd_ellipsoid(q, (0, 0.365, 0.325), (0.058, 0.03, 0.04)), 0.03),
        (sdf.sd_capsule(q, (-0.20, 0.54 + f["brow_inner"] * 0.4, 0.22), (0.20, 0.54 + f["brow_inner"] * 0.4, 0.22),
                        0.035), 0.05),
        (sdf.sd_ellipsoid(q, (0, 0.175, 0.19), (0.07, 0.05, 0.05)), 0.04),
        (sdf.sd_ellipsoid(q, (-0.29, 0.44, -0.03), (0.035, 0.075, 0.055)), 0.02),
        (sdf.sd_ellipsoid(q, (0.29, 0.44, -0.03), (0.035, 0.075, 0.055)), 0.02),
    ]
    skin = sdf.smin(head, jawd, skin_k)
    for d, k in parts:
        skin = sdf.smin(skin, d, k)
    for x in (-0.11, 0.11):
        skin = np.maximum(skin, -sdf.sd_ellipsoid(q, (x, 0.463, 0.31), (0.062, 0.03 * eyes_open + 0.01, 0.05)))
    inside = None
    if mouth == "grin":       # crooked grin: right corner up
        cut = np.minimum(sdf.sd_capsule(q, (-0.075, 0.268, 0.29), (0.0, 0.262, 0.31), 0.011),
                         sdf.sd_capsule(q, (0.0, 0.262, 0.31), (0.09, 0.29, 0.285), 0.013))
    elif mouth == "smile":    # faint, thin smile
        cut = np.minimum(sdf.sd_capsule(q, (-0.07, 0.272, 0.29), (0.0, 0.266, 0.31), 0.007),
                         sdf.sd_capsule(q, (0.0, 0.266, 0.31), (0.075, 0.28, 0.29), 0.007))
    elif mouth == "frown":    # tight line, both corners pulled down
        cut = np.minimum(sdf.sd_capsule(q, (-0.08, 0.243, 0.282), (0.0, 0.272, 0.31), 0.011),
                         sdf.sd_capsule(q, (0.0, 0.272, 0.31), (0.08, 0.243, 0.282), 0.011))
    elif mouth == "shout":    # open mouth: a dark cavity with the upper teeth along its top
        cut = sdf.sd_ellipsoid(q, (0, 0.252, 0.31), (0.075, 0.056, 0.07))
        inside = [(sdf.sd_ellipsoid(q, (0, 0.252, 0.255), (0.066, 0.05, 0.03)), IRIS),
                  (sdf.sd_box(q, (0, 0.293, 0.275), (0.055, 0.01, 0.02), 0.004), EYE)]
    else:
        cut = sdf.sd_capsule(q, (-0.07, 0.27, 0.29), (0.07, 0.27, 0.29), 0.008)
    lips = sdf.sd_ellipsoid(q, (0, 0.27, 0.28), (0.075, 0.03 + (0.016 if mouth == "shout" else 0), 0.035))
    skin = np.maximum(skin, -cut)
    items = [(skin, SKIN), (np.maximum(lips, -cut) + 0.004, LIPS)] + (inside or [])
    for s in (-1, 1):
        x = s * 0.11
        ball = sdf.sd_sphere(q, (x, 0.462, 0.25), 0.052)
        iris = sdf.sd_sphere(q, (x + 0.004, 0.462, 0.281), 0.029)
        lid = sdf.sd_capsule(q, (x - 0.05, 0.488 + f["lid"], 0.283), (x + 0.05, 0.49 + f["lid"], 0.28), 0.02)
        lower = sdf.sd_capsule(q, (x - 0.045, 0.438 + f["lower"], 0.286), (x + 0.045, 0.437 + f["lower"], 0.283),
                               0.012)
        items += [(ball, EYE), (iris, IRIS), (lid, SKIN), (lower, SKIN)]
        # the concept brow runs from (x - 0.06, 0.525) to (x + 0.06, 0.53); its inner end is the
        # one towards the nose
        ends = {-1: (x - 0.06, 0.525, 0.275), 1: (x + 0.06, 0.53, 0.268)}
        brow = []
        for side, (bx, by, bz) in ends.items():
            inner = side == -s
            dy = f["brow_inner"] if inner else f["brow_outer"]
            brow.append((bx - s * f["brow_in"] if inner else bx, by + dy, bz))
        items.append((sdf.sd_capsule(q, brow[0], brow[1], f["brow_r"]), BROW))
    return items, skin


@contextmanager
def expression(name):
    """The concept busts in ``name``'s expression: their scene functions look ``face`` and ``turn``
    up in the concept module when they are called, so both are swapped for the time of a render.
    The head's turn is the call that gives a pitch (the bodies turn by yaw alone); it gets the
    expression's extra pitch."""
    concept_face, concept_turn = r03.face, r03.turn
    extra = FACES[name]["pitch"]
    r03.face = lambda q, **kw: face(q, expression=name, **kw)
    r03.turn = lambda p, yaw, pitch=None, **kw: concept_turn(p, yaw, 0.0 if pitch is None else pitch + extra, **kw)
    try:
        yield
    finally:
        r03.face, r03.turn = concept_face, concept_turn


# --------------------------------------------------------------------------- style B, split

def figure(slug, busts):
    """portraits_r04.style_b2 up to its palette: the 72 px figure in the channel tint over the
    character's own colours with the soft dither, unquantised, and its 1-bit alpha."""
    bust, keyonly = busts
    ch = r03.CAST[slug]["channel"]
    ramp = channel_ramp(slug)
    small = sprite.downsample(bust, r03.RENDER // N)
    rgb, a = np.clip(small[..., :3], 0, 1), small[..., 3] > 0.5
    lum = rgb @ np.array([0.3, 0.55, 0.15])
    base = np.clip(rgb * 1.18 + 0.03, 0, 1)
    grey = base.mean(-1, keepdims=True)
    base = np.clip(grey + (base - grey) * 1.15, 0, 1)
    tint = r04.ramp_at(ramp, np.clip((lum - 0.02) / 0.75, 0, 1) ** 0.85)
    krgb = sprite.downsample(keyonly, r03.RENDER // N)[..., :3]
    mx, mn = krgb.max(-1), krgb.min(-1)
    sat = (mx - mn) / np.maximum(mx, 1e-6)
    bluish = (krgb[..., 2] > krgb[..., 0]) & (krgb[..., 2] > krgb[..., 1])
    accent = a & (sat > 0.5) & (mx > 0.35) & ~bluish
    w = np.where(accent, r04.ACCENT_TINT[ch], r04.TINT[ch])[..., None]
    col = base * (1 - w) + tint * w
    thr = r03.BAYER4[np.arange(N)[:, None] % 4, np.arange(N)[None, :] % 4] - 0.5
    col = np.clip(col + thr[..., None] * r04.DITHER, 0, 1)
    fig = Image.fromarray((col * 255 + 0.5).astype(np.uint8), "RGB").convert("RGBA")
    fig.putalpha(Image.fromarray((a * 255).astype(np.uint8)))
    return fig


def channel_ramp(slug):
    return np.array([raster.hexrgb(h) for h in r03.RAMPS[r03.CAST[slug]["channel"]]], dtype=np.float64) / 255


def screen(slug, fig):
    """portraits_r04.style_b2 after its palette: the quantised figure on the comm screen with its
    outline, interference and glow -> (72 px radio portrait, 144 px briefing portrait)."""
    ramp = channel_ramp(slug)
    arr = np.array(fig).astype(np.float64)
    a = arr[..., 3] > 0
    yy, xx = np.mgrid[0:N, 0:N]
    grid = (xx % 6 == 0) & (yy % 6 == 0)
    bg = ROOMS[slug](ramp) if slug in ROOMS else np.where(grid[..., None], ramp[1] * 255 * 0.8, ramp[0] * 255)
    img = np.where(a[..., None], arr[..., :3], bg)
    pad = np.pad(a, 1)
    touch = (pad[:-2, 1:-1] | pad[2:, 1:-1] | pad[1:-1, :-2] | pad[1:-1, 2:]) & ~a
    img[touch] = ramp[0] * 255 * 0.4
    rng = np.random.default_rng(sum(map(ord, slug)))
    band = 50 + (sum(map(ord, slug)) % 14)
    img[band:band + 3] = np.clip(img[band:band + 3] * 0.75 + ramp[4] * 255 * 0.35, 0, 255)
    for y in rng.choice(np.arange(N - 8, N), 1, replace=False):
        img[y, rng.random(N) > 0.6] = ramp[-2] * 255
    for y in list(rng.choice(np.arange(2, 12), 1)) + list(rng.choice(np.arange(44, 66), 1)):
        img[y] = np.roll(img[y], int(rng.integers(1, 3)), axis=0)
    img = img * 0.94 + ramp[3] * 255 * 0.06
    radio = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    big = np.array(sprite.enlarge(radio, 2)).astype(np.float64)
    big[1::2, :, :3] *= 0.74
    return radio, Image.fromarray(np.clip(big, 0, 255).astype(np.uint8), "RGBA")


# --------------------------------------------------------------------------- build

def render(job):
    slug, name = job
    with expression(name):
        return figure(slug, r03.render_bust(slug))


def build(only=()):
    OUT.mkdir(parents=True, exist_ok=True)
    speakers = {slug: v for slug, v in SPEAKERS.items() if not only or slug in only}
    for old in OUT.glob("*.png"):
        if not only or any(old.name.startswith(f"{size}-{slug}-") for slug in only for size in ("radio", "briefing")):
            old.unlink()
    jobs = [(slug, name) for slug, (names, _) in speakers.items() for name in names]
    with ProcessPoolExecutor() as pool:
        figures = dict(zip(jobs, pool.map(render, jobs)))
    count = 0
    for slug, (names, sizes) in speakers.items():
        shared = artkit.quantize_set([figures[slug, name] for name in names], r04.COLORS)
        source = artkit.source_note(SCRIPT, BATCHES[slug]) if slug in BATCHES else SOURCE
        for name, fig in zip(names, shared):
            radio, briefing = screen(slug, fig)
            artkit.save_png(radio, OUT / f"radio-{slug}-{name}.png", source)
            count += 1
            if "briefing" in sizes:
                artkit.save_png(briefing, OUT / f"briefing-{slug}-{name}.png", source)
                count += 1
    if only and CHOIR not in only:
        print(f"{count} portrait files in {OUT.relative_to(ROOT)}")
        return
    for k in range(r08.CHOIR_FRAMES):
        artkit.save_png(r08.choir_frame(k)[1], OUT / f"radio-{CHOIR}-neutral_{k}.png", SOURCE)
        count += 1
    print(f"{count} portrait files in {OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- review

def load(name):
    return Image.open(OUT / f"{name}.png").convert("RGBA")


def review():
    cast = [slug for slug, (names, _) in SPEAKERS.items() if len(names) > 1]
    width = 16 + 3 * (144 + 12) + 3 * (72 + 8) + 3 * (144 + 8) + 16
    row = 172
    height = 60 + len(cast) * row + 150 + 100
    sheet = raster.sheet(width, height, "PORTRAITS (FINAL R13): NEUTRAL, GRIM, FIERCE",
                         f"PRODUCTION ART, UI BATCH - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, "BRIEFING 144X144 AT 1X | RADIO 72X72 AT 1X | RADIO AT 2X; "
                                    "ONE 36-COLOUR PALETTE PER CHARACTER OVER ITS EXPRESSIONS", raster.LABEL)
    y = 56
    for slug in cast:
        names = SPEAKERS[slug][0]
        radios = [load(f"radio-{slug}-{n}") for n in names]
        raster.draw_text(sheet, 16, y, f"{r03.CAST[slug]['name']}  ({artkit.colour_count(radios)} COLOURS)",
                         raster.LABEL)
        x = 16
        for n in names:
            if slug in R13_BRIEFING:
                sheet.alpha_composite(load(f"briefing-{slug}-{n}"), (x, y + 14))
            raster.draw_text(sheet, x, y + 14 + 146, n.upper(), raster.LABEL_DIM)
            x += 144 + 12
        for img in radios:
            sheet.alpha_composite(img, (x, y + 14))
            x += 72 + 8
        for img in radios:
            sheet.alpha_composite(sprite.enlarge(img, 2), (x, y + 14))
            x += 144 + 8
        y += row
    raster.draw_text(sheet, 16, y, "MINOR SPEAKERS, NEUTRAL: GENERIC CDF OFFICER, GENERIC CIVILIAN (1X, 2X)",
                     raster.LABEL)
    x = 16
    for slug in ("generic-cdf", "generic-civilian"):
        img = load(f"radio-{slug}-neutral")
        sheet.alpha_composite(img, (x, y + 14))
        sheet.alpha_composite(sprite.enlarge(img, 2), (x + 80, y + 14))
        x += 80 + 144 + 16
    y += 172 - 22
    raster.draw_text(sheet, 16, y, f"THE CHOIR: 8 OF THE {r08.CHOIR_FRAMES} LOOP FRAMES (72X72, 12 FPS IN THE GAME)",
                     raster.LABEL)
    for i in range(8):
        sheet.alpha_composite(load(f"radio-{CHOIR}-neutral_{i * 4}"), (16 + i * 80, y + 14))
    png = CONCEPT / f"portraits-final-{ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    frames = []
    for k in range(r08.CHOIR_FRAMES * 3):
        frame = Image.new("RGBA", (4 * (144 + 8) + 8, 160), (12, 14, 20, 255))
        name = EXPRESSIONS[k // r08.CHOIR_FRAMES]
        for i, slug in enumerate(cast):
            frame.alpha_composite(sprite.enlarge(load(f"radio-{slug}-{name}"), 2), (8 + i * 152, 8))
        frame.alpha_composite(sprite.enlarge(load(f"radio-{CHOIR}-neutral_{k % r08.CHOIR_FRAMES}"), 2), (8 + 3 * 152, 8))
        raster.draw_text(frame, 8, 148, name.upper(), raster.LABEL_DIM)
        frames.append(frame)
    gif = png.with_suffix(".gif")
    artkit.write_gif(frames, gif, fps=12, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.relative_to(ROOT)}")
    review_rook()


def review_rook():
    """Round 30: Rook's briefing portraits (he speaks Level 08's hangar teaser), at 1x next to his
    radio portraits, and the other briefing speakers' neutral portraits for comparison."""
    names = SPEAKERS["rook"][0]
    width = 16 + 3 * (144 + 12) + 3 * (72 + 8) + 8
    height = 56 + 14 + 144 + 20 + 30 + 144 + 20 + 12
    sheet = raster.sheet(width, height, f"ROOK BRIEFING PORTRAITS (FINAL {ROOK_ROUND.upper()})",
                         f"PRODUCTION ART, ACT 2 - {ROOK_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, "BRIEFING 144X144 AT 1X (THE RADIO PORTRAIT AT 2X WITH SCANLINES) | RADIO 72X72; "
                                    "ONE 36-COLOUR FIGURE PALETTE", raster.LABEL)
    y = 56
    radios = [load(f"radio-rook-{n}") for n in names]
    briefings = [load(f"briefing-rook-{n}") for n in names]
    raster.draw_text(sheet, 16, y, f"{r03.CAST['rook']['name']}  ({artkit.colour_count(radios)} COLOURS)",
                     raster.LABEL)
    x = 16
    for n, img in zip(names, briefings):
        sheet.alpha_composite(img, (x, y + 14))
        raster.draw_text(sheet, x, y + 14 + 146, n.upper(), raster.LABEL_DIM)
        x += 144 + 12
    for img in radios:
        sheet.alpha_composite(img, (x, y + 14))
        x += 72 + 8
    y += 14 + 144 + 20 + 8
    others = [slug for slug, (_, sizes) in SPEAKERS.items() if "briefing" in sizes and slug != "rook"]
    raster.draw_text(sheet, 16, y, "THE OTHER BRIEFING SPEAKERS, NEUTRAL: " + ", ".join(
        r03.CAST[slug]["name"] for slug in others), raster.LABEL)
    x = 16
    for slug in others + ["rook"]:
        sheet.alpha_composite(load(f"briefing-{slug}-neutral"), (x, y + 14))
        raster.draw_text(sheet, x, y + 14 + 146, slug.upper(), raster.LABEL_DIM)
        x += 144 + 12
    png = ROOK_CONCEPT / f"rook-briefing-portrait-final-{ROOK_ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")


def review_colonial():
    """Round 38: the colonial generic (Ares Control) at 1x, 2x and 4x, and at 1x next to the other
    generic speakers."""
    img = load("radio-generic-colonial-neutral")
    width = 16 + 72 + 12 + 144 + 12 + 288 + 16 + 3 * (72 + 12) + 16
    sheet = raster.sheet(width, 56 + 288 + 40, f"COLONIAL GENERIC RADIO PORTRAIT (FINAL {COLONIAL_ROUND.upper()})",
                         f"PRODUCTION ART, M7 PART B BATCH - {COLONIAL_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"ARES CONTROL, NEUTRAL: 72X72 AT 1X, 2X, 4X ({artkit.colour_count([img])} COLOURS) "
                                    "| THE GENERIC SPEAKERS AT 1X (CDF, CIVILIAN, COLONIAL)", raster.LABEL)
    x, y = 16, 56
    for zoom in (1, 2, 4):
        sheet.alpha_composite(sprite.enlarge(img, zoom), (x, y))
        x += 72 * zoom + 12
    x += 4
    for slug in ("generic-cdf", "generic-civilian", "generic-colonial"):
        sheet.alpha_composite(load(f"radio-{slug}-neutral"), (x, y))
        raster.draw_text(sheet, x, y + 76, slug.replace("generic-", "").upper(), raster.LABEL_DIM)
        x += 72 + 12
    png = CONCEPT_COLONIAL / f"portraits-final-{COLONIAL_ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")


def review_conamara():
    """Round 51: Dr. Brandt (Conamara) at 1x, 2x and 4x, and at 1x next to the other minor speakers."""
    img = load("radio-conamara-neutral")
    others = ("generic-cdf", "generic-civilian", "generic-colonial", "conamara")
    width = 16 + 72 + 12 + 144 + 12 + 288 + 16 + len(others) * (72 + 12) + 16
    sheet = raster.sheet(width, 56 + 288 + 40, f"CONAMARA RADIO PORTRAIT (FINAL {CONAMARA_ROUND.upper()})",
                         f"PRODUCTION ART, M8 PART B BATCH - {CONAMARA_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"DR. ILSE BRANDT, NEUTRAL: 72X72 AT 1X, 2X, 4X ({artkit.colour_count([img])} "
                                    "COLOURS) | THE MINOR SPEAKERS AT 1X", raster.LABEL)
    x, y = 16, 56
    for zoom in (1, 2, 4):
        sheet.alpha_composite(sprite.enlarge(img, zoom), (x, y))
        x += 72 * zoom + 12
    x += 4
    for slug in others:
        sheet.alpha_composite(load(f"radio-{slug}-neutral"), (x, y))
        raster.draw_text(sheet, x, y + 76, slug.replace("generic-", "").upper(), raster.LABEL_DIM)
        x += 72 + 12
    png = CONCEPT / f"portraits-final-{CONAMARA_ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")


def review_thera():
    """Round 52: Thera Deep Control at 1x, 2x and 4x, and at 1x next to the other minor speakers."""
    img = load("radio-thera-deep-neutral")
    others = ("generic-cdf", "generic-civilian", "generic-colonial", "conamara", "thera-deep")
    width = 16 + 72 + 12 + 144 + 12 + 288 + 16 + len(others) * (72 + 12) + 16
    sheet = raster.sheet(width, 56 + 288 + 40, f"THERA DEEP RADIO PORTRAIT (FINAL {THERA_ROUND.upper()})",
                         f"PRODUCTION ART, M8 PART C BATCH - {THERA_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"THERA DEEP CONTROL, NEUTRAL: 72X72 AT 1X, 2X, 4X ({artkit.colour_count([img])} "
                                    "COLOURS) | THE MINOR SPEAKERS AT 1X", raster.LABEL)
    x, y = 16, 56
    for zoom in (1, 2, 4):
        sheet.alpha_composite(sprite.enlarge(img, zoom), (x, y))
        x += 72 * zoom + 12
    x += 4
    for slug in others:
        sheet.alpha_composite(load(f"radio-{slug}-neutral"), (x, y))
        raster.draw_text(sheet, x, y + 76, slug.replace("generic-", "").upper(), raster.LABEL_DIM)
        x += 72 + 12
    png = CONCEPT / f"portraits-final-{THERA_ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")


if __name__ == "__main__":
    only = tuple(a for a in sys.argv[1:] if not a.startswith("--"))
    unknown = set(only) - set(SPEAKERS) - {CHOIR}
    if unknown:
        sys.exit(f"unknown speakers: {', '.join(sorted(unknown))}")
    if "--review" not in sys.argv[1:]:
        build(only)
    if not only or set(only) - {"generic-colonial", "conamara", "thera-deep"}:
        review()
    if not only or "generic-colonial" in only:
        review_colonial()
    if not only or "conamara" in only:
        review_conamara()
    if not only or "thera-deep" in only:
        review_thera()
