#!/usr/bin/env python3
"""Production art: Level 48 "Vorne's End" (design/campaign/act-7-beyond-the-gate/level-48-vornes-end; M11 part F batch,
concept round 93, step ART-F): Vorne's Chimera (design/enemies/bosses/vornes-chimera, round 84 A.6 b "thorned graft"),
the citadel kit (tiles, the heart, the spire well and its lid, the cores' veins, the gold hulls), the reactor cores,
Vorne's archive and his fused portrait (round 84 A.12 a). The two briefing images are tools/art/briefing_images.py's.

Everything is in round 84 Q10 b's **one Vrell palette** with Level 48's light (design/world/vrell-space, *Light per
level*): teal-black growth over dull gold wreckage (its own colour, a step paler and less saturated than the boss's
gold: gold-on-gold), the cores' gold-white the brightest gold below the boss, the soft key light from the top left.
The Chimera's body is round 84's model (tools/concept/act7_r84.py ``chimera_model("b")``, imported unchanged; its places
set to the spec's: the bay (0, 50), the capsule (0, 10)) with more embedded gold plates (the production's 35 % gold),
the dead sockets torn open in phases 2-3, rendered 1:1 at 4x and re-rendered per bank (rolled about its long axis, the
key light fixed). Its grafts are the Ascendant's production parts (tools/art/ascendant.py, assets/sprites) in the
Chimera's sickly gold (``sick``: dulled, a step greener, veined with teal), so step F4's ChimeraLooks can follow
AscendantLooks (the panels' MirrorLooks frames and pivots unchanged). Every glow and haze is a float field written
unquantised (far more than 32 steps, no ordered dither: DitherGuardTest's measure) and fades to 0 inside its canvas
(``--check`` fails on a lit border pixel of an additive set). **No straight lines**: the growth's ridges, the plating's
seams (noise isolines, never Voronoi's straight edges), the chasms' edges, the mast's sides and the spire's rings all
bend; ``--check`` scans every tile and piece row by row and column by column (step FIXC-SPINE's measure: a run of
120 px stepping the same way is a straight line) and fails on one.

Sprites (build/art-held/sprites/ until step F2's data claims the root, then assets/sprites/: belt_units.py's rule;
``additive`` = premultiplied on black, GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha, 1-bit; offsets are px from
the piece's centre, y down; pivots/vornes-chimera.json holds them all):

  Vorne's Chimera (root ``vornes-chimera``; fixed, nose down; the body canvas 240x248 with the piece's centre at (120, 148))
  vornes-chimera_0..4            normal: phase 1's body (the gold capsule sealed in tissue, the sockets' gold collars,
                                 the thorned arms, the long thorned tail), banked -12, -6, 0, +6, +12 degrees (+ = rolled
                                 toward +x, moving right); the parts are drawn over it
  vornes-chimera-tissue_0..4     normal: phase 2's body: the three dead sockets torn open (bent gold shards), the bay dead
  vornes-chimera-open_0..4       normal: phase 3's body: the capsule's canopy open, Vorne's dark silhouette grown into the
                                 seat against the warm cockpit light
  vornes-chimera-opening_0..5    normal: the capsule opening (2 s, 3 fps, bank 0): 0 sealed (= tissue_2) .. 5 open (= open_2)
  vornes-chimera-battery_0..2    52x52 normal at (+-70, -10): whole, damaged, dead
  vornes-chimera-panel*_0..17    64x64 at the batteries: -panel, -panel-down, -panel-cracks (normal), -panel-glow,
                                 -panel-rim (additive), -panel-sweep (24x24 additive): the Ascendant's mirror panel sets
                                 (same headings: pivots ``panels``; the facings 40 degrees outward are headings 7 and
                                 57, frames 3 and 14)
  vornes-chimera-rack_0..5       56x32 normal at (0, -60): the spine rack's hatches 0 shut .. 3 open (the salvo's tell),
                                 4 damaged, 5 dead; -rack-lock 20x20 additive red at (0, -66)
  vornes-chimera-bay_0..5        80x36 normal at (0, 50): the belly bay's gold doors 0 shut .. 3 open (the Dart stream's
                                 tell), 4 damaged, 5 dead (phases 2-3 draw 5); -bay-light 84x40 additive gold
  vornes-chimera-sac_0..4        60x60 normal at the sockets (+-70, -10) and (0, -60): rest, swelling 1-3 (the brood's
                                 tell), split; -sac-dead (the husk), -sac-core 44x44 / -sac-heal / -sac-flare 60x60
                                 additive, -sac-burst_0..5 64x64 additive (the Ascendant's, graded)
  vornes-chimera-sac-grow_0..5   60x60 normal: the fusion's 2 s burst: tissue swelling out of a dead socket into the sac
  vornes-chimera-fusion_0..7     240x248 additive over the body: the fusion's pain, 0-1 the tell (teal light leaking from
                                 every gold seam), 2-7 the burst out of the three sockets and the centre, fading
  vornes-chimera-closing_0..8    96x112 normal at the capsule: K, the tissue closing over the open capsule (4.5 s, 2 fps)
  vornes-chimera-capsule-cracks  96x112 additive at the capsule: below half its HP, teal light in its gold shell's cracks
  vornes-chimera-shield_0..7     96x112 additive at the capsule: the gold hex shield's shimmer (8 frames, 10 fps loop)
  vornes-chimera-muzzle_0..5     48x48 additive at ``nose`` (0, 44): the lance's 0.8 s tell, the nose glowing gold-white
  vornes-chimera-port-flash_0..3 32x32 additive at the four ``ports``: the gold fan's tell and the port pair's muzzles
  vornes-chimera-pulse_0..7      96x96 additive at the centre: the spore spiral's tell (lime and teal)
  vornes-chimera-flare-l/-r_0..7 88x180 additive at ``flank`` (+-84, 6): the rear sweep's tell, the far flank flaring
  vornes-chimera-sweep-mark_0..7 192x96 additive, centred on the bottom edge at the rise's x: the 1.0 s out of sight
  vornes-chimera-burst_0..9      192x192 additive at the capsule: K + 4.5 s, the gold-white burst with teal fibres (12 fps),
                                 before the built explosion-vacuum-large chain
  vornes-chimera-plate_0..7      16x16 normal: a gold-edged plate tumbling (the fusion's and the burst's debris)
  The reactor core (root ``reactor-core``: step F2's unit dir; ground, 40x40, hitbox 32x32)
  reactor-core_0..1              40x40 normal: whole, cracked (below half); -stump (single): dead, the core gone
  reactor-core-glow_0..7         64x64 additive: the core's gold-white breath (4 fps; F4 flickers it in the bottom 120 px)
  reactor-core-death_0..7        96x96 additive: the gold-white flash and the debris ring (vacuum)
  reactor-core-tatters_0..7      64x64 normal: housing and tissue chips flung out
  Vorne's archive (root ``vorne-archive``: a 30 HP ground object with a hidden crate, as Level 45's courier cyst)
  vorne-archive_0..2             40x40 normal: sealed, cracked, broken open; -break_0..7 64x64: gold chips flung out
  Vorne's fused portrait: assets/sprites/portraits/radio-vorne-fused-<neutral|smug|rage> (72) and
  briefing-vorne-fused-<...> (144), round 84's ``fused(img, "a")`` over his production portraits

Backdrop (assets/backdrop/level-48/; rows y-up as the game reads them; ``--proposal`` writes the ``backdrop:`` block,
the sections and the cores' and the archive's places to concept/backdrop-proposal.yaml: step F2 places them, F4 wires
the hooks):
  deep-1 .. deep-4     ``deep`` 0.15, opaque: the void below the citadel, its towers far below (lobed growth round gold
                       wreckage), the spire (deep-3 spans sections 3-6, held in the halt), the pale shimmer by t~235
  outer-growth, gold-decks, spire-road, spire-wall   ``ground`` tiles 480x960 (sections 1, 2, 3, 7), + ``-veins``
                       (additive, the veins' pulsing share, C4's VeinPulse)
  seam-2, seam-3       ground 480x256 on the section 1/2 and 2/3 seams (the tiles' own rows blended along a ragged line)
  heart                ground 480x1060, exact rows: sections 4-6 (the spire road's rows below, the spire wall's above), the
                       veins converging on the well
  spire-well (+ -glow) ground 320x280, 12 frames: 0 shut (the lid veined with gold light), 1-8 the lid splitting (the
                       sting, 4 fps), 9-11 open on the gold-lit throat (4 fps loop); its centre at screen y 60 at the halt
  core-vein-l, -r      ground 80x560 normal: a core's vein channel, its bottom centre on the core; ``-glow`` 18 frames
                       additive: 0-11 the pulse up toward the spire (8 fps: one in 1.5 s), 12-17 dark from the core outward
  gold-hull-a..c, archive-hull   ground ~110x150: the open gold hulls, the hold at HOLD (0, 6) on the core / the archive

Reviews (round 93, ``*-final-r93-a``): design/enemies/bosses/vornes-chimera/concept/vornes-chimera; the level's
concept/ backdrop and l48-props; design/story/characters/concept/vorne-fused.
Run: nice -n 19 python3 tools/art/act7_citadel.py [chimera] [props] [portrait] [citadel] [--review | --proposal | --check]
     (no part = all; the Chimera's 19 renders take ~5 min on 6 workers, cached in the temp dir by the model code's hash)
"""
import hashlib
import json
import os
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

import act7_reef as reef
import artkit
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import additive_rgb, checker_share, cut_tile, rgba, smoothstep, wrap_score
from act7_reef import blob, blur3, chips_frames, edge_fade, grid, label, light_frame, lit_border, shaded_dot, to_img

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: the Chimera's round 84 b model, the fused portrait, the hex shimmer)
import act6_r72 as b6  # noqa: E402  (concept: the Ascendant's materials and key light)
from render import raster, sdf, sprite  # noqa: E402
from render.enemy_models import A_GLASS, A_GOLD  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_y, rotate_z, sd_box, sd_capsule, sd_cylinder_z,  # noqa: E402
                        sd_ellipsoid, sd_sphere, union)

SCRIPT = "act7_citadel.py"
BATCH = "M11 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r93"
artkit.REVIEW_ROUND = ROUND
L48 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-48-vornes-end"
CONCEPT = L48 / "concept"
CHIM_C = DESIGN / "enemies" / "bosses" / "vornes-chimera" / "concept"
CHARS_C = DESIGN / "story" / "characters" / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-48"
PORT = ROOT / "assets" / "sprites" / "portraits"
SPR = ROOT / "assets" / "sprites"
HELD = ROOT / "build" / "art-held"
W, SCREEN = 480, 540

# the spec's places (vornes-chimera README, *Arena*): the concept's model reads these module globals when it is built
a84.BAY, a84.CAPS = (0, 50), (0, 10)
BATT, RACK, BAY, CAPS = a84.BATT, a84.RACK, a84.BAY, a84.CAPS

# Level 48's light (Vrell space, *Light per level*): teal-black growth in the one palette over dull gold wreckage, a
# step paler and less saturated than the boss's gold; the cores' gold-white the brightest gold below the boss
TEAL_K = reef.TEAL_K                                 # teal-black growth, lit
VIOLET_K = reef.VIOLET_K
VEIN = reef.VEIN
MINT = reef.MINT
TEAL = np.array(a84.TEAL, float)                     # #00FF9A, the Chimera's seams
LIME = np.array(a84.LIME, float)
GOLD_W = np.array((255.0, 236.0, 178.0))             # the cores' gold-white
GOLD_L = np.array((255.0, 196.0, 90.0))              # warm gold light
WRECK = np.array((150.0, 128.0, 84.0))               # the wreckage's dull gold, lit (paler, less saturated)
VOID = np.array((7.0, 5.0, 12.0))

ADDITIVE = {"vornes-chimera-fusion", "vornes-chimera-panel-glow", "vornes-chimera-panel-rim", "vornes-chimera-panel-sweep",
            "vornes-chimera-rack-lock", "vornes-chimera-bay-light", "vornes-chimera-sac-core", "vornes-chimera-sac-heal",
            "vornes-chimera-sac-flare", "vornes-chimera-sac-burst", "vornes-chimera-port-flash", "vornes-chimera-muzzle",
            "vornes-chimera-pulse", "vornes-chimera-flare-l", "vornes-chimera-flare-r", "vornes-chimera-sweep-mark",
            "vornes-chimera-burst", "vornes-chimera-shield", "vornes-chimera-capsule-cracks",
            "reactor-core-glow", "reactor-core-death", "vorne-archive-glint"}


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def load(name):
    return l23kit.load(name)


def asc(name):
    """An Ascendant production frame set (assets/sprites, read only)."""
    frames = artkit.load_frames(name)
    if not frames:
        raise FileNotFoundError(name)
    return frames


def arr(img):
    return np.array(img.convert("RGBA")).astype(np.float64)


def img_of_arr(a):
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


def additive_of(light, margin=4):
    return light_frame(np.asarray(light, float), margin)


# =========================================================================== the Chimera's sickly gold (grade)

def sick(img, seed=9301, k=1.0):
    """The Ascendant's production gold made the Chimera's (Level 48's mood): dulled, a step greener, veined with teal
    (thin smooth veins over the warm pixels). Alpha unchanged."""
    a = arr(img)
    rgb = a[..., :3]
    h, w = rgb.shape[:2]
    lum = rgb @ np.array((0.299, 0.587, 0.114))
    warm = np.clip((rgb[..., 0] - rgb[..., 2]) / 90.0, 0, 1) * (a[..., 3] > 0)
    grey = lum[..., None] * np.ones(3)
    dull = rgb * (1 - 0.22 * k) + grey * 0.22 * k
    dull = dull * np.array((0.9, 0.9, 0.82)) ** k
    x, y = grid(w, h)
    n = raster.fbm(max(w, 8), max(h, 8), 8, seed, octaves=3, period=False)[:h, :w]
    vein = np.exp(-(np.abs(np.sin(n * 17.0 + (x + y) * 0.05)) / 0.16) ** 2)
    out = rgb * (1 - warm[..., None]) + dull * warm[..., None]
    out += (vein * warm * 0.42 * k)[..., None] * np.array((0, 150, 100.0))
    a[..., :3] = np.clip(out, 0, 255) * (a[..., 3:4] > 0)
    return img_of_arr(a)


def compose_parts(left, right, size, dx):
    """Two half-sprites (the Ascendant's -l/-r racks or bays) side by side on one canvas, centres at +-dx."""
    out = Image.new("RGBA", size, (0, 0, 0, 0))
    for im, sx in ((left, -dx), (right, dx)):
        l23kit.paste_c(out, im, size[0] / 2 + sx, size[1] / 2)
    return out


def soot(img, seed, k=0.5, dead=False):
    """A part damaged (soot and teal-lit cracks, k) or dead (darkened, burst open: teal tissue in the breach)."""
    a = arr(img)
    h, w = a.shape[:2]
    x, y = grid(w, h)
    n = raster.fbm(max(w, 8), max(h, 8), 6, seed, octaves=3, period=False)[:h, :w]
    on = a[..., 3] > 0
    dark = np.clip((n - 0.35) * 2.2, 0, 1) * k
    a[..., :3] *= (1 - 0.65 * dark)[..., None]
    crack = np.exp(-(np.abs(n - 0.55) / 0.018) ** 2) * k
    a[..., :3] += (crack * on)[..., None] * np.array((20, 120, 90.0))
    if dead:
        c = np.hypot(x - w / 2, y - h / 2) / (min(w, h) / 2)
        hole = (c < 0.42 + 0.12 * (n - 0.5)) & on
        tis = np.array((14, 46, 42.0)) * (0.7 + 0.6 * n)[..., None]
        a[..., :3] = np.where(hole[..., None], tis, a[..., :3] * 0.6)
    a[..., :3] *= on[..., None]
    return img_of_arr(a)


# =========================================================================== the Chimera's body (round 84 A.6 b)

PPU = 100.0                                      # px per model unit (the concept's: the body drawn 1:1)
BODY = (240, 248)                                # the body's canvas
BC = (120, 148)                                  # the station centre in the canvas (the piece's centre)
BANKS = (-12, -6, 0, 6, 12)                      # frame k: rolled (k - 2) x 6 degrees; + = toward +x (moving right)
OPENING = (0.2, 0.4, 0.6, 0.8)
KEY = tuple(b6.KEY)
MORE_PLATES = ((0.38, 0.06, 0.4, 0.11, 0.08), (0.12, -0.42, -0.6, 0.1, 0.07), (0.4, -0.44, 0.2, 0.1, 0.07),
               (0.06, 0.3, 0.3, 0.1, 0.07), (0.3, 0.62, 0.8, 0.1, 0.07), (0.16, -0.66, -0.2, 0.09, 0.06),
               (0.44, 0.34, 0.6, 0.09, 0.06))


def _ry(fn, rad):
    if fn is None:
        return None
    return lambda p, n, f=fn: f(rotate_y(p, rad), rotate_y(n, rad))


def body_scene(phase, open_, bank):
    """Round 84 b's body (a84.chimera_model, unchanged) with more embedded gold hull plates (the production's 35 %
    gold), in phases 2-3 the dead sockets torn open (bent gold shards round them), rolled by ``bank`` degrees."""
    base, mats = a84.chimera_model("b", open_)
    rad = -np.radians(bank)
    shards = []
    if phase >= 2:
        rng = np.random.default_rng(9310)
        for (sx_, sy_), r0 in ((BATT[0], 0.25), (BATT[1], 0.25), (RACK, 0.2)):
            cx, cy = sx_ / PPU, -sy_ / PPU
            for a in np.linspace(0, TAU, 9, endpoint=False) + rng.uniform(0, 0.5):
                L = rng.uniform(0.07, 0.13)
                a0 = (cx + np.cos(a) * r0, cy + np.sin(a) * r0, 0.34)
                a1 = (cx + np.cos(a + rng.uniform(-0.2, 0.2)) * (r0 + L), cy + np.sin(a) * (r0 + L), 0.3 + rng.uniform(0.0, 0.08))
                shards.append((a0, a1, rng.uniform(0.022, 0.032)))

    def scene(p):
        q = rotate_y(p, rad)
        d, m = base(q)
        s = mirror_x(q)
        plates = []
        for (x, y, a, w_, h_) in MORE_PLATES:
            z = 0.42 * np.sqrt(max(0.05, 1 - (x / 0.47) ** 2 - ((y - 0.02) / 0.85) ** 2))
            pr = rotate_z(s - np.array((x, y, z)), a)
            plates.append(sd_box(pr, (0, 0, 0), (w_, h_, 0.03), 0.02))
        items = [(d, m), (np.minimum.reduce(plates), A_GOLD)]
        if shards:
            items.append((np.minimum.reduce([sd_capsule(q, a0, a1, r, 0.004) for a0, a1, r in shards]), A_GOLD))
        return union(*items, k=0.01)
    mats = [replace(mt, pattern=_ry(mt.pattern, rad), emission_pattern=_ry(mt.emission_pattern, rad)) for mt in mats]
    return scene, mats


def _body(phase, open_, bank):
    scene, mats = body_scene(phase, open_, bank)
    w, h = BODY
    f = 4
    cx, cy = (w / 2 - BC[0]) / PPU, (BC[1] - h / 2) / PPU
    hi = sdf.render(scene, mats, (w * f, h * f), w / PPU, center=(cx, cy), z_top=2.0, steps=130, key_pos=KEY)
    return sprite.sharpen(sprite.to_image(sprite.downsample(hi, f), 0.5), 70)


def body_jobs():
    jobs = [("body", 1, 0.0, b) for b in BANKS] + [("body", 2, 0.0, b) for b in BANKS]
    jobs += [("body", 3, 1.0, b) for b in BANKS] + [("body", 3, o, 0) for o in OPENING]
    return jobs


# =========================================================================== the Chimera's parts, tells and effects

PART_SIZES = {"battery": (52, 52), "panel": (64, 64), "rack": (56, 32), "bay": (80, 36), "sac": (60, 60)}
CAPSULE_FX = (96, 112)                           # the capsule's overlays, centred on the capsule
NOSE = (0, 44)                                   # the lance's origin: the capsule's lower tip (px from the centre)
PORTS = [(-30, 2), (30, 2), (-22, 32), (22, 32)]  # the gun ports round the capsule (the port pair, the gold fan)
FLANK = {"l": (-84, 6), "r": (84, 6)}            # the rear sweep's flank flare, centred there
SOCKETS = {"sac-l": BATT[0], "sac-r": BATT[1], "sac-spine": RACK}


def chimera_parts():
    """The grafts: the Ascendant's production parts (assets/sprites, step ART-H1) in the Chimera's sickly gold."""
    bat = [sick(f, 9320 + k) for k, f in enumerate(asc("ascendant-battery"))]
    save_set("vornes-chimera-battery", bat)
    save_set("vornes-chimera-panel", [sick(f, 9330, 0.8) for f in asc("ascendant-panel")])
    save_set("vornes-chimera-panel-down", [sick(f, 9331, 0.8) for f in asc("ascendant-panel-down")])
    save_set("vornes-chimera-panel-cracks", asc("ascendant-panel-cracks"))
    for s in ("glow", "rim"):
        save_set(f"vornes-chimera-panel-{s}", [faded(f, 3) for f in asc(f"ascendant-panel-{s}")])
    save_set("vornes-chimera-panel-sweep", [faded(asc("ascendant-panel-sweep")[0], 4)], single=True)
    rl, rr = asc("ascendant-rack-l"), asc("ascendant-rack-r")
    rack = [sick(compose_parts(rl[k], rr[k], PART_SIZES["rack"], 13), 9340) for k in range(4)]
    rack += [soot(rack[0], 9341, 0.7), soot(rack[3], 9342, 1.0, dead=True)]
    save_set("vornes-chimera-rack", rack)
    save_set("vornes-chimera-rack-lock", [lamp((20, 20), (255, 60, 40), 3.0)], single=True)
    bl, br = asc("ascendant-bay-l"), asc("ascendant-bay-r")
    bay = [sick(compose_parts(bl[k], br[k], PART_SIZES["bay"], 18), 9350) for k in range(4)]
    bay += [soot(bay[0], 9351, 0.7), soot(bay[3], 9352, 1.0, dead=True)]
    save_set("vornes-chimera-bay", bay)
    light = asc("ascendant-bay-light")[0]
    save_set("vornes-chimera-bay-light", [compose_parts(light, light, (84, 40), 18)], single=True)
    sacs = [sick(f, 9360, 0.5) for f in asc("ascendant-sac")]
    save_set("vornes-chimera-sac", sacs)
    save_set("vornes-chimera-sac-dead", [sick(asc("ascendant-sac-dead")[0], 9361, 0.5)], single=True)
    for s in ("core", "heal", "flare"):
        save_set(f"vornes-chimera-sac-{s}", [faded(asc(f"ascendant-sac-{s}")[0], 6)], single=True)
    save_set("vornes-chimera-sac-burst", [faded(f, 6) for f in asc("ascendant-sac-burst")])
    save_set("vornes-chimera-port-flash", [lamp((32, 32), GOLD_L, r, k) for r, k in ((2.5, 0.7), (4.0, 1.0),
                                                                                      (3.2, 0.85), (2.0, 0.5))])
    save_set("vornes-chimera-plate", [sick(f, 9370) for f in asc("ascendant-plate")])
    save_set("vornes-chimera-sac-grow", sac_grow(sacs[0]))


def faded(img, margin):
    """An additive frame (premultiplied on black) faded to 0 over its canvas's outer ``margin`` px (the Ascendant's
    copies whose light reached the edge: the Act 7 rule, every glow fading to 0 inside its canvas)."""
    return light_frame(arr(img)[..., :3], margin)


def lamp(size, col, r, k=1.0):
    """A small smooth light (the rack's red lock, the ports' gold flash), fading to 0 inside its canvas."""
    w, h = size
    x, y = grid(w, h)
    light = blob(x, y, w / 2, h / 2, r)[..., None] * np.asarray(col, float) * 0.9 * k
    light += blob(x, y, w / 2, h / 2, r * 0.45)[..., None] * np.array((255, 245, 220.0)) * 0.6 * k
    light += blob(x, y, w / 2, h / 2, r * 2.4)[..., None] * np.asarray(col, float) * 0.25 * k
    return light_frame(light, 4)


def sac_grow(sac, n=6):
    """The fusion's pain: tissue bursting out of a dead socket and swelling into the sac (the 2 s burst, 3 fps):
    the sac's own frame grown from a third of its size, a ragged teal-black bud first."""
    out = []
    w, h = sac.size
    for f in range(n):
        u = (f + 1) / n
        s = 0.3 + 0.7 * u ** 0.8
        im = sac.resize((max(4, round(w * s)), max(4, round(h * s))), Image.LANCZOS)
        a = arr(im)
        a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
        a[..., :3] *= (0.55 + 0.45 * u) * (a[..., 3:4] > 0)
        c = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        l23kit.paste_c(c, img_of_arr(a), w / 2, h / 2)
        out.append(c)
    return out


def gold_mask(img):
    a = arr(img)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    return ((a[..., 3] > 128) & (r > 70) & (r > b * 1.4) & (g > b * 1.1)).astype(float)


def fusion_frames(body, n=8):
    """The fusion's pain over the phase-1 body (additive, BODY): 0-1 the tell (teal light leaking from every gold
    plate's seam), 2-7 the 2 s burst (teal-white light bursting out of the three sockets and the centre, fading)."""
    g = gold_mask(body)
    seam = np.clip(blur3(g[..., None], 1.2)[..., 0] - g * 0.85, 0, 1) * (arr(body)[..., 3] > 0)
    w, h = BODY
    x, y = grid(w, h)
    out = []
    for f in range(n):
        light = np.zeros((h, w, 3))
        k = (0.45, 1.0, 1.0, 0.9, 0.75, 0.55, 0.35, 0.15)[f]
        light += (seam * 2.2 * k)[..., None] * TEAL
        if f >= 2:
            u = (f - 2) / (n - 3)
            burst = np.sin(np.pi * min(1.0, 0.25 + u)) * (1 - 0.6 * u)
            for sx_, sy_ in list(SOCKETS.values()) + [(0, 10)]:
                r = 18 + 22 * u
                light += blob(x, y, BC[0] + sx_, BC[1] + sy_, r)[..., None] * TEAL * 0.65 * burst
                light += blob(x, y, BC[0] + sx_, BC[1] + sy_, r * 0.4)[..., None] * np.array((200, 255, 230.0)) \
                    * 0.5 * burst
        light += blur3(light, 3.0) * 0.6
        out.append(additive_of(light, 10))
    return out


def closing_frames(n=9):
    """K: the tissue closing over the open capsule (4.5 s, 2 fps; normal, CAPSULE_FX centred on the capsule): wet
    teal-black flesh growing in from the opening's rim to its centre, teal veins on it, lit from the top left."""
    w, h = CAPSULE_FX
    x, y = grid(w, h)
    cx, cy = w / 2, h / 2 - 6
    rr = np.hypot((x - cx) / 30.0, (y - cy) / 40.0)
    n_ = raster.fbm(w, h, 8, 9380, octaves=3, period=False)
    rim = 1.0 + 0.12 * (n_ - 0.5)
    out = []
    for f in range(n):
        u = (f + 1) / n
        cov = (rr < rim) & (rr > rim * (1 - u) + 0.25 * (n_ - 0.5) * (1 - u))
        z = np.sqrt(np.clip(1 - (rr / rim) ** 2, 0, 1))
        shade = np.clip(0.35 + 0.5 * z - 0.35 * (x - cx) / 30 - 0.35 * (y - cy) / 40, 0.15, 1.25)
        tis = np.array((16, 58, 54.0)) * shade[..., None] * (0.8 + 0.4 * n_)[..., None]
        vein = np.exp(-(np.abs(np.sin(n_ * 22 + rr * 3)) / 0.12) ** 2) * z
        tis += vein[..., None] * TEAL * 0.3
        out.append(to_img(tis, cov.astype(float)))
    return out


def capsule_cracks(open_body):
    """Below half the capsule's HP: teal light in the cracks of its gold shell (additive, CAPSULE_FX)."""
    w, h = CAPSULE_FX
    c = open_body.crop((BC[0] + CAPS[0] - w // 2, BC[1] + CAPS[1] - h // 2, BC[0] + CAPS[0] + w // 2,
                        BC[1] + CAPS[1] + h // 2))
    g = gold_mask(c)
    n_ = raster.fbm(w, h, 6, 9385, octaves=3, period=False)
    crack = np.exp(-(np.abs(n_ - 0.5) / 0.02) ** 2) * g
    light = crack[..., None] * TEAL * 0.9
    light += blur3(light, 1.6) * 0.8
    return additive_of(light, 6)


def shield_frames(n=8):
    """The capsule's gold hex shield (120, the enemy shield rule; additive, CAPSULE_FX): faint hex lines on a soft disc
    over the open capsule, shimmering (a84.hex_shimmer, round 84's), fading to 0 inside the canvas."""
    w, h = CAPSULE_FX
    out = []
    for f in range(n):
        s = a84.hex_shimmer(w, h, w / 2, h / 2 - 2, 34, f / n * TAU / 5)
        out.append(additive_of(s[..., None] * np.array((255, 214, 120.0)) * 0.55, 8))
    return out


def muzzle_frames(n=6, w=48):
    """The lance's 0.8 s tell: the capsule's nose glowing gold-white (frame = progress x 6; held at 5 under the line)."""
    x, y = grid(w, w)
    out = []
    for f in range(n):
        u = (f + 1) / n
        light = blob(x, y, w / 2, w / 2, 4 + 9 * u)[..., None] * GOLD_L * 0.7 * u
        light += blob(x, y, w / 2, w / 2, 2 + 3 * u)[..., None] * GOLD_W * 0.8 * u
        out.append(additive_of(light, 6))
    return out


def pulse_frames(n=8, w=96):
    """The spore spiral's 0.8 s tell: the body's centre pulsing lime and teal (8 frames, 10 fps)."""
    x, y = grid(w, w)
    out = []
    for f in range(n):
        k = 0.5 - 0.5 * np.cos(TAU * (f + 0.5) / n * 2)
        light = blob(x, y, w / 2, w / 2, 14 + 10 * k)[..., None] * TEAL * 0.45 * (0.4 + 0.6 * k)
        light += blob(x, y, w / 2, w / 2, 7 + 4 * k)[..., None] * LIME * 0.5 * (0.3 + 0.7 * k)
        out.append(additive_of(light, 8))
    return out


def flare_frames(side, n=8, size=(88, 180)):
    """The rear sweep's 0.8 s tell: the far flank's tissue flaring teal (10 fps, peaking at frame 4)."""
    w, h = size
    x, y = grid(w, h)
    sgn = -1 if side == "l" else 1
    out = []
    for f in range(n):
        k = (0.25, 0.5, 0.75, 0.95, 1.0, 0.9, 0.7, 0.45)[f]
        band = np.exp(-((x - w / 2 - sgn * 8 * (1 - ((y - h / 2) / (h / 2)) ** 2)) / 14.0) ** 2) \
            * np.exp(-((y - h / 2) / (h * 0.36)) ** 2)
        light = band[..., None] * TEAL * 0.8 * k + blur3(band[..., None] * TEAL, 6) * 0.5 * k
        out.append(additive_of(light, 10))
    return out


def sweep_mark_frames(n=8, size=(192, 96)):
    """The rear sweep's 1.0 s out of sight: a teal glow at the bottom-edge point it will rise from (the boss's own
    tell, never the edge warning); drawn centred on the bottom edge, so its upper half shows; growing (8 fps)."""
    w, h = size
    x, y = grid(w, h)
    out = []
    for f in range(n):
        u = (f + 1) / n
        light = np.exp(-(((x - w / 2) / (28 + 34 * u)) ** 2 + ((y - h / 2) / (14 + 14 * u)) ** 2))[..., None] \
            * TEAL * (0.35 + 0.5 * u)
        light += np.exp(-(((x - w / 2) / (10 + 8 * u)) ** 2 + ((y - h / 2) / 6.0) ** 2))[..., None] \
            * np.array((180, 255, 220.0)) * 0.45 * u
        out.append(additive_of(light, 10))
    return out


def burst_frames(n=10, w=192):
    """K + 4.5 s: the capsule bursting gold-white with torn teal fibres flung out (12 fps; additive, never a screen
    flash), before the built ``explosion-vacuum-large`` chain through the tissue."""
    x, y = grid(w, w)
    c = w / 2
    rr, ang = np.hypot(x - c, y - c), np.arctan2(y - c, x - c)
    rng = np.random.default_rng(9390)
    fib = [(rng.uniform(0, TAU), rng.uniform(0.6, 1.0)) for _ in range(14)]
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        k = (1 - u) ** 1.4
        light = blob(x, y, c, c, 10 + 30 * u)[..., None] * GOLD_W * 0.95 * k
        light += blob(x, y, c, c, 22 + 50 * u)[..., None] * GOLD_L * 0.45 * k
        for a, sp in fib:
            d = rr / (12 + 70 * u * sp)
            line = np.exp(-((((ang - a + np.pi) % TAU) - np.pi) * rr / 2.0) ** 2) * np.exp(-((d - 0.8) / 0.35) ** 2)
            light += line[..., None] * TEAL * 0.7 * k
        out.append(additive_of(light, 12))
    return out


def build_chimera(res):
    """The bodies from the renders (phase 1, 2, 3 by bank; the opening), the parts and every overlay."""
    bodies = {}
    jobs = body_jobs()
    for job, img in zip(jobs, res):
        bodies[job[1:]] = img
    p1 = [bodies[(1, 0.0, b)] for b in BANKS]
    p2 = [bodies[(2, 0.0, b)] for b in BANKS]
    p3 = [bodies[(3, 1.0, b)] for b in BANKS]
    opening = [p2[2]] + [bodies[(3, o, 0)] for o in OPENING] + [p3[2]]
    allb = artkit.quantize_set(p1 + p2 + p3 + opening, 96)
    save_set("vornes-chimera", allb[:5])
    save_set("vornes-chimera-tissue", allb[5:10])
    save_set("vornes-chimera-open", allb[10:15])
    save_set("vornes-chimera-opening", allb[15:])
    chimera_parts()
    save_set("vornes-chimera-fusion", fusion_frames(p1[2]))
    save_set("vornes-chimera-closing", closing_frames())
    save_set("vornes-chimera-capsule-cracks", [capsule_cracks(p3[2])], single=True)
    save_set("vornes-chimera-shield", shield_frames())
    save_set("vornes-chimera-muzzle", muzzle_frames())
    save_set("vornes-chimera-pulse", pulse_frames())
    save_set("vornes-chimera-flare-l", flare_frames("l"))
    save_set("vornes-chimera-flare-r", flare_frames("r"))
    save_set("vornes-chimera-sweep-mark", sweep_mark_frames())
    save_set("vornes-chimera-burst", burst_frames())
    write_chimera_pivots()


def write_chimera_pivots():
    piv = json.loads((ROOT / "assets" / "pivots" / "ascendant.json").read_text(encoding="utf-8"))
    left, right = 7, 57                           # 39.4 degrees outward of straight down on its own side (spec: 40)
    data = {
        "canvas": list(BODY), "centre": list(BC),
        "note": "px from the piece's centre, y down; the body sprites are drawn with BC on the piece's centre",
        "banks": list(BANKS), "bank_note": "frame k rolled (k - 2) x 6 degrees, + toward +x (moving right)",
        "bodies": {"phase-1": "vornes-chimera", "phase-2": "vornes-chimera-tissue", "phase-3": "vornes-chimera-open",
                   "opening": "vornes-chimera-opening (6 frames over the 2 s, bank 0: 0 sealed .. 5 open)"},
        "parts": {"battery-l": list(BATT[0]), "battery-r": list(BATT[1]), "rack": list(RACK), "bay": list(BAY),
                  "capsule": list(CAPS), **{k: list(v) for k, v in SOCKETS.items()}},
        "part_sizes": {k: list(v) for k, v in PART_SIZES.items()},
        "battery_states": {"whole": 0, "damaged": 1, "dead": 2},
        "rack_frames": {"shut": 0, "open": 3, "damaged": 4, "dead": 5, "lock": [0, -6]},
        "bay_frames": {"shut": 0, "open": 3, "damaged": 4, "dead": 5},
        "sac_frames": {"rest": 0, "swell": [1, 2, 3], "split": 4},
        "panels": {"headings": piv["panels"]["headings"], "left": piv["panels"]["left"],
                   "right": piv["panels"]["right"], "frames": piv["panels"]["frames"],
                   "facing": {"battery-l": left, "battery-r": right},
                   "facing_frames": {"battery-l": piv["panels"]["headings"].index(left),
                                     "battery-r": piv["panels"]["headings"].index(right)}},
        "nose": list(NOSE), "ports": [list(p) for p in PORTS], "flank": {k: list(v) for k, v in FLANK.items()},
        "capsule_fx": list(CAPSULE_FX),
        "fusion": {"tell": [0, 1], "burst": [2, 3, 4, 5, 6, 7], "sac_grow": 6},
        "closing": {"frames": 9, "fps": 2}, "burst": {"frames": 10, "fps": 12},
    }
    write_pivots("vornes-chimera", data)


def write_pivots(root, data):
    """belt_units' rule (assets/pivots once the root is claimed, else build/art-held/pivots) with this batch's source."""
    folder = artkit.PIVOTS if bu.claimed(root) else HELD / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = HELD / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


# =========================================================================== the reactor core and Vorne's archive

M_GOLD, M_TISSUE, M_CORE, M_DARK, M_BONE = range(5)


def prop_mats(core=1.0, tissue_glow=1.0):
    """The wreckage's dull gold (paler and less saturated than the Chimera's), the growth's teal-black with smooth teal
    veins, the core's gold-white, the soot."""
    return [Material((0.6, 0.52, 0.36), metal=0.7, shininess=60, spec=0.7),
            Material((0.05, 0.17, 0.16), spec=0.55, shininess=50, emission=tuple(TEAL / 255 * 0.3 * tissue_glow),
                     emission_pattern=reef.em._veins(26, 0.4, 0.14)),
            Material((0.1, 0.1, 0.1), emission=tuple(np.array((1.0, 0.93, 0.72)) * 1.4 * core)),
            Material((0.035, 0.03, 0.03), spec=0.1),
            Material((0.62, 0.6, 0.52), spec=0.3, shininess=30)]


def core_scene(state):
    """The reactor core (40x40, 40 px a unit): a round gold reactor housing, dulled and cracked, four teal tissue clamps
    grown over it, the core's gold-white in its throat. state 0 whole, 1 cracked, 2 dead (the core gone, the ring
    burst open)."""
    rng = np.random.default_rng(9400)
    clamps = [(a, rng.uniform(0.44, 0.5)) for a in (0.5, 2.1, 3.6, 5.1)]
    cracks = [(rng.uniform(0, TAU), rng.uniform(0.3, 0.45)) for _ in range(5)]

    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        ring = np.maximum(sd_cylinder_z(p, (0, 0, 0.0), 0.4, 0.16), -sd_cylinder_z(p, (0, 0, 0.0), 0.25, 0.4))
        bevel = sd_cylinder_z(p, (0, 0, 0.0), 0.3, 0.2)
        ring = np.minimum(ring, np.maximum(bevel, -sd_cylinder_z(p, (0, 0, 0.0), 0.24, 0.4)))
        if state >= 1:
            ang = np.arctan2(p[:, 1], p[:, 0])
            for a, w_ in cracks[:3 if state == 1 else 5]:
                gap = np.abs(((ang - a + np.pi) % TAU) - np.pi) * r - 0.022 * (1 + 0.5 * state)
                ring = np.maximum(ring, -np.maximum(gap, (0.26 - r)))
        if state == 2:
            ang = np.arctan2(p[:, 1], p[:, 0])
            wedge = np.abs(((ang - 2.6 + np.pi) % TAU) - np.pi) * r - 0.09
            ring = np.maximum(ring, -wedge)
        floor = sd_cylinder_z(p, (0, 0, -0.08), 0.26, 0.04)
        items = [(ring, M_GOLD), (floor, M_DARK)]
        if state < 2:
            items.append((sd_sphere(p, (0.0, 0.0, 0.02), 0.17 if state == 0 else 0.14), M_CORE))
        cl = []
        for a, rr in clamps:
            c, s = np.cos(a), np.sin(a)
            cl.append(sd_capsule(p, (c * 0.56, s * 0.56, -0.02), (c * 0.33, s * 0.33, 0.2), 0.075, 0.045))
            cl.append(sd_capsule(p, (c * 0.33, s * 0.33, 0.2), (c * 0.24, s * 0.24, 0.14), 0.045, 0.03))
        items.append((np.minimum.reduce(cl), M_TISSUE))
        return union(*items, k=0.01)
    return scene, prop_mats(core=(1.0, 0.75, 0.0)[state], tissue_glow=(1.0, 0.8, 0.3)[state])


def archive_scene(state):
    """Vorne's archive (40x40): an Ascendancy data vault, gold and sealed, half overgrown by the growth. state 0
    whole, 1 cracked (dark splits across the lid), 2 broken open (the lid blown off, the hollow dark)."""
    def scene(p):
        q = rotate_z(p, 0.35)
        box = sd_box(q, (0, 0, 0.0), (0.3, 0.2, 0.13), 0.04)
        lid = sd_box(q, (0, 0, 0.13), (0.24, 0.14, 0.03), 0.02)
        vault = np.minimum(box, lid)
        if state >= 1:
            for k, (a, o) in enumerate(((0.6, 0.02), (-0.9, -0.06), (0.2, 0.09))[:2 if state == 1 else 3]):
                qq = rotate_z(q, a)
                vault = np.maximum(vault, -sd_box(qq, (o, 0, 0.16), (0.026, 0.3, 0.08)))
        if state == 2:
            vault = np.maximum(vault, -sd_box(q, (0, 0, 0.14), (0.22, 0.13, 0.12), 0.02))
        bands = np.maximum(sd_box(q, (0, 0, 0.0), (0.31, 0.21, 0.14), 0.04),
                           -sd_box(q, (0, 0, 0.0), (0.33, 0.23, 0.12)))
        bands = np.maximum(bands, np.abs(np.abs(q[:, 0]) - 0.16) - 0.02)
        items = [(vault, M_GOLD), (bands, M_GOLD)]
        if state == 2:
            items.append((sd_box(q, (0, 0, 0.035), (0.21, 0.12, 0.012)), M_DARK))
        growth = [sd_ellipsoid(p, (-0.22, 0.1, 0.06), (0.2, 0.16, 0.15)), sd_ellipsoid(p, (-0.3, -0.12, 0.02), (0.14, 0.14, 0.1)),
                  sd_capsule(p, (-0.2, 0.06, 0.14), (0.12, 0.2, 0.12), 0.05, 0.02),
                  sd_capsule(p, (-0.26, -0.1, 0.1), (0.04, -0.22, 0.12), 0.045, 0.015),
                  sd_capsule(p, (-0.1, 0.16, 0.16), (0.06, -0.04, 0.17), 0.035, 0.012)]
        items.append((np.minimum.reduce(growth), M_TISSUE))
        return union(*items, k=0.012)
    return scene, prop_mats(core=0.0)


def _prop(kind, state):
    scene, mats = (core_scene if kind == "core" else archive_scene)(state)
    return bu.render(scene, mats, (40, 40), 1.0, crisp=70)


def prop_jobs():
    return [("prop", "core", s) for s in range(3)] + [("prop", "archive", s) for s in range(3)]


def core_glow(n=8, w=64):
    """The live core's gold-white light (additive, 64x64 on the core): a slow breath (8 frames, 4 fps; the game
    flickers it while the core is in the field's bottom 120 px)."""
    x, y = grid(w, w)
    out = []
    for f in range(n):
        k = 0.8 + 0.2 * np.cos(TAU * f / n)
        light = blob(x, y, w / 2, w / 2, 6.0)[..., None] * GOLD_W * 0.55 * k
        light += blob(x, y, w / 2, w / 2, 15.0)[..., None] * GOLD_L * 0.22 * k
        out.append(additive_of(light, 6))
    return out


def core_death(n=8, w=96):
    """Destroyed (vacuum): a short gold-white flash and a ring of gold debris light spreading and fading."""
    x, y = grid(w, w)
    rr = np.hypot(x - w / 2, y - w / 2)
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        light = blob(x, y, w / 2, w / 2, 8 + 10 * u)[..., None] * GOLD_W * 0.9 * (1 - u) ** 2
        light += np.exp(-((rr - (8 + 34 * u)) / (2.5 + 3 * u)) ** 2)[..., None] * GOLD_L * 0.55 * (1 - u) ** 1.2
        out.append(additive_of(light, 6))
    return out


def build_props(res):
    core, arch = res[:3], res[3:]
    save_set("reactor-core", core[:2])
    save_set("reactor-core-stump", [core[2]], single=True)
    save_set("reactor-core-glow", core_glow())
    save_set("reactor-core-death", core_death())
    save_set("reactor-core-tatters", chips_frames(8, 64, [(150, 132, 92), (110, 96, 66), (30, 80, 74)], 9410,
                                                  count=12, speed=(30, 46)))
    save_set("vorne-archive", arch)
    gold = [(176, 150, 96), (130, 110, 70), (24, 70, 64)]
    save_set("vorne-archive-break", chips_frames(8, 64, gold, 9420, count=12, speed=(28, 44)))


# =========================================================================== Vorne's fused portrait (round 84 A.12 a)

MOODS = ("neutral", "smug", "rage")


def build_portrait():
    """``a84.fused`` ("the edge", round 84's, unchanged) on his production portraits (portraits.py, round 60): the
    teal-black tissue grown in from the left edge, veins reaching over his cheek and collar. Written as
    ``radio-vorne-fused-<mood>`` (72) and ``briefing-vorne-fused-<mood>`` (144) in assets/sprites/portraits."""
    for i, m in enumerate(MOODS):
        for kind, seed in (("briefing", 8401), ("radio", 8411)):
            src = Image.open(PORT / f"{kind}-vorne-{m}.png").convert("RGBA")
            artkit.save_png(a84.fused(src, "a", seed + i), PORT / f"{kind}-vorne-fused-{m}.png", SOURCE)
    print(f"portraits: vorne-fused x {len(MOODS)} (radio, briefing) -> {PORT.relative_to(ROOT)}")


# =========================================================================== the backdrop: clock and sections

FACTORS = {"deep": 0.15, "ground": 1.0, "low-air": 1.3}
DATA_PATH = L48 / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
# the README's Layout; section 4 split where the ease to the halt starts (t=180), as Level 40's braking section
PROPOSED_SECTIONS = [
    {"name": "The Outer Growth", "end": 44, "speed": 120, "atmosphere": "clear", "tiles": ["deep-1", "outer-growth"]},
    {"name": "The Gold Decks", "end": 116, "speed": 110, "ramp": 2, "atmosphere": "clear",
     "tiles": ["deep-2", "gold-decks"]},
    {"name": "The Spire Road", "end": 176, "speed": 100, "ramp": 2, "atmosphere": "clear",
     "tiles": ["deep-3", "spire-road"]},
    {"name": "The Spire Well", "end": 180, "speed": 100, "atmosphere": "clear", "tiles": ["deep-3"]},
    {"name": "The Spire Well, braking", "end": 186, "speed": 0, "ramp": 6, "atmosphere": "clear", "tiles": ["deep-3"]},
    {"name": "The Heart", "end": 187, "speed": 0, "arena": True, "hold_after_boss": 8, "atmosphere": "clear",
     "tiles": ["deep-3"]},
    {"name": "Through the Spire", "end": 248, "speed": 140, "ramp": 2, "atmosphere": "clear",
     "tiles": ["deep-4", "spire-wall"]},
]
SECTIONS = DATA["sections"] if DATA and DATA.get("sections") else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
DEEP = ["deep-1", "deep-2", "deep-3", "deep-4"]
TILE_H = 960
GROUND_TILES = ("outer-growth", "gold-decks", "spire-road", "spire-wall")
SEED = 4801
VEIN_BASE, VEIN_PULSE = 0.42, 0.55          # the veins' share in the tiles, and in the pulsing -veins layer
HALT_T = 187.0
WELL_Y = 60                                 # the spire well's centre on the screen at the halt (the rise's start)


def first_section(tile):
    return next(i for i, s in enumerate(SECTIONS) if tile in (s.get("tiles") or []))


def last_section(tile):
    return max(i for i, s in enumerate(SECTIONS) if tile in (s.get("tiles") or []))


def ground_row_at(t, y_down):
    return scroll(t) + SCREEN - y_down


def t_ground(row):
    return CLOCK.t_for_layer_row(row, 1.0)


def section_at_t(t):
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]):
            return i
    return len(SECTIONS) - 1


# =========================================================================== the citadel's ground (fields)

def fbm(w, h, cell, seed, octaves=4, period=True):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=period)


STYLES = {   # gold: share of buried gold showing; gap: side chasms (x from the edge); ridge: the growth's fold scale
    "outer": dict(gold=0.3, ridge=1.0, gap=(70, 30), vein=0.7, seed=1),
    "decks": dict(gold=0.56, ridge=0.8, gap=(28, 14), vein=1.0, seed=2, dim=0.86),
    "road": dict(gold=0.42, ridge=0.9, road=168, vein=1.0, seed=3),
    "heart": dict(gold=0.16, ridge=1.15, vein=0.7, seed=4),
    "wall": dict(gold=0.0, ridge=1.0, mast=True, vein=0.8, seed=5),
}


def wavy_plates(rows, seed, per):
    """The wreckage's hull plating as noise isolines, never Voronoi's straight edges: rows of plates (40 px) whose
    seams wander +-15 px over a plate's width, each row's vertical seams (72 px apart, staggered by half a plate on
    every other row) bending as much. Returns the distance to the nearest seam (px), two per-plate tones."""
    n = len(rows)
    R = np.asarray(rows, float)[:, None]
    x = (np.arange(W) + 0.5)[None, :]
    fa = fbm(W, n, 48, seed, 3, per)
    fb = fbm(W, n, 32, seed + 1, 3, per)
    v = R / 40.0 + 1.6 * (fa - 0.5)
    b = np.floor(v)
    hs = np.abs(v - np.round(v)) * 40.0 / (1 + 0.6 * np.abs(np.gradient(fa, axis=1)) * 40)
    u = x / 72.0 + 0.5 * (b % 2) + 1.2 * (fb - 0.5)
    c = np.floor(u)
    vs = np.abs(u - np.round(u)) * 72.0
    e = np.minimum(hs, vs) / 2
    bm = b % (n // 40) if per else b
    h = reef._hash
    return e, h(c.astype(np.int64), bm.astype(np.int64), seed + 2), h(c.astype(np.int64), bm.astype(np.int64), seed + 3)


def citadel(rows, style, period_rows=None):
    """A citadel field over ground rows (y-up, one per image row from the top): rgb (n, 480, 3), alpha, veins 0..1.
    The growth is ridged teal-black tissue (folds as smooth isolines of a noise field, never straight); the wreckage
    under it dull gold plates with wavy seams, a step paler and less saturated than the Chimera's gold; teal veins
    meander up the screen; side chasms show the deep. ``period_rows``: a tile wrapping in those rows."""
    st = STYLES[style]
    n = len(rows)
    per = period_rows is not None
    s0 = SEED + 100 * st["seed"]
    R = np.asarray(rows, float)[:, None]
    x = (np.arange(W) + 0.5)[None, :]
    f1 = fbm(W, n, 96 if per else 96, s0 + 1, 4, per)
    f2 = fbm(W, n, 48, s0 + 2, 4, per)
    f3 = fbm(W, n, 160, s0 + 3, 3, per)
    fine = fbm(W, n, 16, s0 + 4, 3, per)
    wob = fbm(W, n, 16, s0 + 5, 3, per)
    # the growth: a raised network of ridged ropes (ridged noise at two scales: the one isoline of each field, so
    # the ridges branch and merge, never run straight), over lobed swellings
    r1 = (1 - np.abs(2 * fbm(W, n, 80, s0 + 7, 2, per) - 1)) ** 3
    r2 = (1 - np.abs(2 * fbm(W, n, 40, s0 + 8, 2, per) - 1)) ** 4
    ridge = np.clip((0.75 * r1 + 0.4 * r2) * st["ridge"], 0, 1.2)
    lobes = smoothstep(0.25, 0.75, f3)
    tissue_h = 0.9 * ridge + 0.55 * lobes + 0.18 * fine
    # the gold: where the growth thins
    goldm = smoothstep(0.03, -0.03, (1 - st["gold"]) - (0.55 * f3 + 0.45 * f2) - 0.15 * (fine - 0.5))
    if style == "decks":                       # decks laid over whole hulls: long gold bands under the growth
        band = 0.5 + 0.5 * np.sin(TAU * (x - 240) / 240.0 + 3.0 * (f1 - 0.5))
        goldm = np.clip(goldm + 0.35 * smoothstep(0.55, 0.85, band) * smoothstep(0.3, 0.6, f3), 0, 1)
    e, tone, tone2 = wavy_plates(rows, s0 + 6, per)
    if per and period_rows % 40:
        raise ValueError("tile rows must be a multiple of 64")
    seam_ = smoothstep(0.6, 2.2, e)
    gold_h = 0.4 * seam_ + 0.12 * tone2 + 0.1 * fine
    alpha = np.ones((n, W))
    mast = None
    if st.get("mast"):                         # the spire's dead gold mast down the middle, ringed by tissue
        cxm = 240 + 7 * np.sin(TAU * R / 960.0 * 2 + 0.4) + 10 * (f3 - 0.5) + 12 * (f2 - 0.5)
        half = 30 + 16 * (fbm(W, n, 32, s0 + 13, 3, per) - 0.5) + 6 * (fine - 0.5)
        dm = np.abs(x - cxm) / half
        mast = smoothstep(1.05, 0.9, dm)
        cyl = np.sqrt(np.clip(1 - dm ** 2, 0, 1))
        goldm = np.maximum(goldm, mast)
        gold_h = gold_h * 0.4 + 0.9 * cyl + 0.15 * seam_
        arch = 0.012 * (x - 240) ** 2 + 26 * (f1 - 0.5)
        ph = R + arch + 40 * f3
        ring = (0.5 + 0.5 * np.cos(TAU * ph / (960.0 / 7))) ** 6 * smoothstep(150, 40, np.abs(x - cxm))
        ring = ring * smoothstep(0.2, 0.5, f2 + 0.25)
        tissue_h = tissue_h * 0.8 + 1.1 * ring
        goldm = goldm * (1 - smoothstep(0.25, 0.55, ring))
        wall = smoothstep(70, 150, np.abs(x - 240))
        tissue_h = tissue_h + 0.5 * wall * (0.5 + 0.5 * np.sin(np.pi * (6 * f2 + 0.02 * np.abs(x - 240))))
        win = (smoothstep(0.62, 0.7, f3 + 0.3 * smoothstep(380, 470, np.abs(x - 240) * 2 - 0)) *
               smoothstep(150, 200, np.abs(x - 240)))
        alpha = 1 - win
    if st.get("road"):                         # the causeway: x ~ 240 +- road, the sides falling away
        edge = st["road"] + 22 * (f3 - 0.5) * 2 + 34 * (f2 - 0.5) + 8 * (fine - 0.5)
        d = edge - np.abs(x - 240)
        alpha = smoothstep(-0.5, 1.5, d)
        fall = smoothstep(0, 30, d)
        tissue_h = tissue_h * (0.4 + 0.6 * fall)
    if st.get("gap"):
        g0, g1 = st["gap"]
        side = np.minimum(x, W - x)
        reach = g0 * (0.55 + 0.9 * f3) + g1 * (fine - 0.5) * 2 + 34 * (f2 - 0.5)
        alpha = smoothstep(-0.5, 1.5, side - reach + 10)
    # height and the top-left light
    edge_bump = np.exp(-((goldm - 0.5) / 0.3) ** 2)
    hgt = gold_h * goldm * 0.8 + (tissue_h + 0.5) * (1 - goldm) + 0.35 * edge_bump

    def grad(a):
        if per:
            gi = (np.roll(a, -1, 0) - np.roll(a, 1, 0)) / 2
        else:
            gi = np.gradient(a, axis=0)
        return np.gradient(a, axis=1), gi
    gx, gi = grad(hgt * 9.0)
    shade = np.clip(0.62 + 0.5 * (gx + gi), 0.2, 1.45)
    mix = np.clip(0.25 + 0.5 * (tone - 0.5) + 0.3 * (f2 - 0.5), 0, 1)[..., None]
    tis = (TEAL_K * (1 - mix) + VIOLET_K * mix * 0.8) * (0.55 + 0.6 * ridge + 0.15 * fine)[..., None]
    grime = 0.72 + 0.28 * smoothstep(0.2, 0.7, f2)
    gol = WRECK * st.get("dim", 1.0) * (0.72 + 0.36 * tone2)[..., None] * (0.3 + 0.7 * seam_)[..., None] * (grime * (0.85 + 0.3 * fine))[..., None]
    if mast is not None:
        gol = gol * (0.55 + 0.25 * mast)[..., None]         # the dead mast: dark, dull
    gm = goldm[..., None]
    sheen = np.clip(shade - 1.0, 0, 0.45)[..., None] * np.array((150, 130, 90.0)) * gm
    col = (tis * (1 - gm) + gol * gm) * shade[..., None] + sheen
    # the growth's lip over the gold (a dark rim where it overlaps)
    lip = np.exp(-((goldm - 0.5) / 0.18) ** 2)
    col = col * (1 - 0.35 * lip)[..., None]
    # veins: thin teal channels meandering up the screen (isolines of a field running along y), on growth and gold
    vf = (x - 240) / 72.0 + 1.6 * (f1 - 0.5) + 1.5 * (f2 - 0.5) + 0.5 * (wob - 0.5)
    vein = np.exp(-(np.abs(np.sin(np.pi * vf)) / 0.045) ** 2) * smoothstep(0.45, 0.7, f2 + 0.25 * f3) \
        * (1 - 0.7 * goldm)
    if style == "heart" or style == "road":
        vein = vein * (0.6 + 0.4 * smoothstep(200, 60, np.abs(x - 240)))
    vein = np.clip(vein * st["vein"], 0, 1)
    # the chasms' lip
    if not np.all(alpha >= 1):
        lipd = np.exp(-((alpha - 0.75) / 0.2) ** 2)
        col = col * (1 - 0.45 * lipd)[..., None] + lipd[..., None] * VIOLET_K * 0.25
    vein = vein * (alpha > 0.5)
    rgb = col + (vein * VEIN_BASE)[..., None] * VEIN
    return rgb, alpha, vein


_TILES = {}
TILE_STYLE = {"outer-growth": "outer", "gold-decks": "decks", "spire-road": "road", "spire-wall": "wall"}


def tile(name):
    if name not in _TILES:
        rows = TILE_H - 1 - np.arange(TILE_H)
        rgb, a, v = citadel(rows, TILE_STYLE[name], TILE_H)
        _TILES[name] = (np.clip(np.round(rgb), 0, 255), np.round(np.clip(a, 0, 1) * 255) / 255, v)
    return _TILES[name]


def sample(name, rows):
    rgb, a, v = tile(name)
    idx = TILE_H - 1 - (np.asarray(rows, int) % TILE_H)
    return rgb[idx], a[idx], v[idx]


def ragged(n, seed, cell=64, amp=1.0):
    return (raster.fbm(W, max(n, 8), cell, seed, octaves=3, period=False)[:n] - 0.5) * amp


blend = reef.blend


def seam_piece(a_name, b_name, s, h=256, seed=0):
    """Rows [s - h, s): tile a's rows blended into tile b's along a ragged line; its top rows are b's own."""
    rows = s - 1 - np.arange(h)
    u = ((rows - (s - h)) / h)[:, None]
    m = smoothstep(-0.07, 0.07, u - 0.5 + 0.34 * ragged(h, SEED + 900 + seed))
    m = np.clip(m + smoothstep(0.88, 1.0, u), 0, 1) * smoothstep(0.0, 0.1, u)
    return blend(sample(a_name, rows), sample(b_name, rows), m)


def g_seam(i):
    return seam("ground", i)


def well_row():
    return ground_row_at(HALT_T, WELL_Y)


HEART_LO, HEART_HI = 160, 200             # the heart piece's rows below the section 3/4 seam and above the 6/7 seam


def heart_piece():
    """Sections 4-6's ground (exact rows): the spire road's own rows at the bottom blended into the citadel's heart,
    a dense deck of growth whose veins converge on the spire well (WELL_Y on the screen at the halt), the spire's
    inner wall's own rows at the top. Returns (rgb, alpha, vein), its top row."""
    s4, s7 = g_seam(first_section("deep-3") + 1), g_seam(first_section("spire-wall"))
    lo, hi = s4 - HEART_LO, s7 + HEART_HI
    rows = hi - 1 - np.arange(hi - lo)
    rgb, a, v = citadel(rows, "heart")
    wr = well_row()
    R = np.asarray(rows, float)[:, None]
    x = (np.arange(W) + 0.5)[None, :]
    dx, dy = x - 240, R - wr
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    nn = raster.fbm(W, len(rows), 48, SEED + 410, octaves=3, period=False)
    spokes = np.exp(-(np.abs(np.sin(6 * ang + 3.0 * (nn - 0.5) + rho / 140.0)) / 0.035) ** 2)
    conv = 0.7 * spokes * smoothstep(120, 180, rho) * smoothstep(800, 380, rho) * smoothstep(160, 60, dy)
    v = np.clip(v * 0.45 + conv, 0, 1)
    rgb = rgb + (conv * VEIN_BASE)[..., None] * VEIN
    field = (rgb, a, v)
    # the bottom: the spire road's own rows; the top: the spire wall's
    u = ((R - lo) / (hi - lo))
    m_lo = smoothstep(-0.07, 0.07, (R - lo) / HEART_LO - 0.55 + 0.34 * ragged(len(rows), SEED + 920)[:, :])
    field = blend(sample("spire-road", rows), field, np.clip(m_lo, 0, 1))
    m_hi = smoothstep(-0.07, 0.07, (R - (hi - HEART_HI)) / HEART_HI - 0.45 + 0.34 * ragged(len(rows), SEED + 921))
    m_hi = np.clip(m_hi + smoothstep(0.985, 1.0, u), 0, 1)
    field = blend(field, sample("spire-wall", rows), m_hi)
    return field, hi - 1


def piece_images(field):
    rgb, a, v = field
    return rgba(rgb, a), additive_rgb((v * VEIN_PULSE)[..., None] * VEIN)


_GP = {}


def ground_pieces():
    if not _GP:
        out = {}
        s2, s3 = g_seam(first_section("gold-decks")), g_seam(first_section("spire-road"))
        out["seam-2"] = (seam_piece("outer-growth", "gold-decks", s2, seed=2), s2 - 1)
        out["seam-3"] = (seam_piece("gold-decks", "spire-road", s3, seed=3), s3 - 1)
        out["heart"] = heart_piece()
        _GP.update(out)
    return _GP


# =========================================================================== the spire well and its lid

WELL = (320, 280)
WELL_FRAMES = 12                            # 0 shut; 1-8 the lid splitting (4 fps, 2 s); 9-11 open (4 fps loop)


def well_state(f):
    if f == 0:
        return 0.0, 0.0
    if f <= 8:
        return f / 8.0, 0.0
    return 1.0, (f - 9) / 3.0


def well_frame(f):
    """The citadel's heart: a raised ring of growth round the spire well, its tissue lid of six lobes veined with gold
    light (shut), splitting from the centre with gold light under it (the sting), open on a gold-lit throat; the
    piece fades into the heart's deck at its rim. Returns (normal, additive)."""
    w, h = WELL
    x, y = grid(w, h)
    cx, cy = w / 2, h / 2
    dx, dy = x - cx, (y - cy) * 1.12
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    nn = raster.fbm(w, h, 20, SEED + 430, octaves=4, period=False)
    split, flick = well_state(f)
    R = 108 + 6 * np.sin(5 * ang + 1.2) + 5 * (nn - 0.5)
    rim = np.exp(-((rho - R) / 14.0) ** 2)
    outer = smoothstep(R + 40, R + 6, rho)
    open_r = (R - 10) * split ** 0.9
    lobe = 0.5 + 0.5 * np.cos(6 * ang + 2.0 * (nn - 0.5))
    edge_r = open_r * (0.85 + 0.25 * lobe)
    hole = rho < edge_r
    lid = (rho < R) & ~hole
    lid_h = np.sqrt(np.clip(1 - (rho / R) ** 2, 0, 1)) * (1 - 0.6 * split) + 0.12 * lobe
    hgt = 1.2 * rim + np.where(lid, lid_h * 0.8, 0.0) + 0.2 * nn
    gx, gy = np.gradient(hgt * 8.0, axis=1), np.gradient(hgt * 8.0, axis=0)
    shade = np.clip(0.62 + 0.5 * (gx + gy), 0.2, 1.45)
    tis = TEAL_K * (0.7 + 0.6 * nn)[..., None] * shade[..., None]
    seams = np.exp(-(np.abs(np.sin(3 * ang + 0.6 * (nn - 0.5))) / 0.05) ** 2) * lid
    tis = tis * (1 - 0.6 * seams)[..., None]
    throat_d = np.clip(rho / np.maximum(edge_r, 1), 0, 1)
    throat = (WRECK * 0.25 * (0.4 + 0.6 * throat_d)[..., None]) * (0.7 + 0.5 * nn)[..., None]
    rgb = np.where(hole[..., None], throat, tis)
    alpha = np.clip(outer + hole, 0, 1)
    # the light: gold through the lid's veins (shut), under the parting lobes (splitting), up the throat (open)
    vein = np.exp(-(np.abs(np.sin(9 * ang + 4 * nn + rho / 30.0)) / 0.08) ** 2) * lid * smoothstep(R, R * 0.4, rho)
    # the sting's light peaks as the lid parts (frames 5-8), then the open throat glows dim under the fight (9-11), so
    # the boss's gold reads over it (gold-on-gold)
    k_open = split * (0.85 + 0.15 * np.sin(TAU * flick + 0.5)) * (0.28 if f >= 9 else 1.0)
    light = vein[..., None] * GOLD_L * (0.35 + 0.4 * split)
    light += seams[..., None] * GOLD_L * 0.3 * split
    light += (np.exp(-((rho - edge_r) / 6.0) ** 2) * (split > 0))[..., None] * GOLD_W * 0.55 * split \
        * (0.35 if f >= 9 else 1.0)
    light += blob(x, y, cx, cy, 30 + 60 * split)[..., None] * GOLD_L * 0.55 * k_open
    light += blob(x, y, cx, cy, 14 + 24 * split)[..., None] * GOLD_W * 0.4 * k_open
    light = light * smoothstep(R + 30, R - 10, rho)[..., None]
    return rgba(rgb, alpha), light_frame(light, 8)


# =========================================================================== the cores' veins and the gold hulls

VEIN_P = (80, 560)
VEIN_LOOP, VEIN_DARK = 12, 6                # 0-11 the pulse up the vein (8 fps: one in 1.5 s); 12-17 going dark


def vein_path(side, seed):
    """x of the vein per image row (0 = top) of a VEIN_P piece: from the core at the bottom centre up the screen,
    leaning toward the spire (the field's centre) and meandering."""
    w, h = VEIN_P
    rng = np.random.default_rng(seed)
    y = np.arange(h) + 0.5
    u = 1 - y / h                                      # 0 at the core, 1 at the top
    lean = (16 if side == "l" else -16) * u ** 1.3
    wig = (13 * np.sin(u * 9.0 + rng.uniform(0, TAU)) + 5 * np.sin(u * 23 + rng.uniform(0, TAU))) * np.sqrt(u)
    return w / 2 + lean + wig, u


def core_vein(side):
    """A core's vein (ground piece, VEIN_P, its bottom centre on the core): a raised channel of growth (normal, soft
    alpha: dark when the core is dead) and its light (additive, 18 frames): 0-11 the steady glow with one brighter
    pulse running up toward the spire every 1.5 s (8 fps), 12-17 going dark from the core outward over 1 s (6 fps);
    nothing after."""
    w, h = VEIN_P
    xs, u = vein_path(side, SEED + (510 if side == "l" else 520))
    x, y = grid(w, h)
    d = np.abs(x - xs[:, None])
    width = 1.6 + 1.2 * (1 - u)[:, None]
    fade_top = smoothstep(1.0, 0.82, u)[:, None]
    fade_bot = smoothstep(0.0, 0.035, u)[:, None]
    body = np.exp(-(d / (width + 2.5)) ** 2) * fade_top * fade_bot
    hgt = np.exp(-(d / (width + 2.0)) ** 2)
    gx, gy = np.gradient(hgt * 6, axis=1), np.gradient(hgt * 6, axis=0)
    shade = np.clip(0.62 + 0.5 * (gx + gy), 0.2, 1.4)
    rgb = TEAL_K * 0.8 * shade[..., None]
    normal = rgba(rgb, np.clip(body * 1.2, 0, 1) * smoothstep(0.08, 0.3, body))
    core_line = np.exp(-(d / width) ** 2) * fade_top * fade_bot
    frames = []
    L = h
    s = (1 - u)[:, None] * 0 + u[:, None] * L          # arclength from the core (px up)
    for f in range(VEIN_LOOP + VEIN_DARK):
        if f < VEIN_LOOP:
            pos = (f / VEIN_LOOP) * (L + 160) - 80
            pulse = np.exp(-((s - pos) / 34.0) ** 2)
            k = 0.26 + 0.55 * pulse
            cut = 1.0
        else:
            g = (f - VEIN_LOOP + 1) / VEIN_DARK
            k = 0.26
            cut = smoothstep(g * L - 40, g * L + 10, s)
        light = (core_line * k * cut)[..., None] * VEIN * 1.1
        light += blur3(light, 2.5) * 0.45
        frames.append(light_frame(light, 4))
    return normal, frames


HULLS = {   # name: (size, prow angle (deg, 0 = up, clockwise), length, beam, seed)
    "gold-hull-a": ((112, 152), -18.0, 66, 34, 1),
    "gold-hull-b": ((120, 148), 24.0, 62, 38, 2),
    "gold-hull-c": ((104, 144), 168.0, 60, 32, 3),
    "archive-hull": ((116, 156), -150.0, 64, 36, 4),
}
HOLD = (0, 6)                                 # the hold's centre from the piece's centre (the core or the archive there)


def gold_hull(name):
    """An Ascendancy wreck's hull half buried in the decks (ground piece): a dull gold hull (a step paler than the
    Chimera's gold), its plating split open over an empty round hold at HOLD where a reactor core (or Vorne's
    archive) sits, teal-black growth lapping over its sides; lit from the top left, its outline never straight."""
    (w, h), prow, L, B, sd = HULLS[name]
    x, y = grid(w, h)
    cx, cy = w / 2, h / 2
    a = np.radians(prow)
    ux, uy = np.sin(a), -np.cos(a)                     # along the hull, toward the prow (screen, y down)
    px, py = x - cx, y - cy
    along = px * ux + py * uy
    across = -px * uy + py * ux
    nn = raster.fbm(w, h, 12, SEED + 600 + sd, octaves=4, period=False)
    taper = np.clip(1 - np.clip(along / L, 0, 1) ** 2 * 0.85, 0.05, 1) * np.clip(1 + along / (L * 1.25), 0, 1) ** 0.4
    half = B * taper + 3 * (nn - 0.5)
    q = (np.abs(across) / np.maximum(half, 1)) ** 2 + (along / (L * 1.02)) ** 8
    hull = q < 1
    cyl = np.sqrt(np.clip(1 - np.abs(across) / np.maximum(half, 1), 0, 1))
    hx_, hy_ = cx + HOLD[0], cy + HOLD[1]
    hr = np.hypot(x - hx_, y - hy_)
    hold = hr < 21 + 2.5 * (nn - 0.5) * 2
    lip = np.exp(-((hr - 22) / 3.5) ** 2) * hull
    e, tone, tone2 = reef.plates(np.arange(h)[::-1] + 7 * sd, 26, 22, SEED + 610 + sd, None)
    e = e[:, :w] + 2.6 * (nn - 0.5)
    seam_ = smoothstep(0.8, 2.6, e)
    hgt = 0.9 * cyl + 0.25 * seam_ + 0.5 * lip - 0.6 * hold
    gx, gy = np.gradient(hgt * 9, axis=1), np.gradient(hgt * 9, axis=0)
    shade = np.clip(0.62 + 0.5 * (gx + gy), 0.2, 1.45)
    gol = WRECK * (0.75 + 0.4 * tone2[:, :w])[..., None] * (0.35 + 0.65 * seam_)[..., None] * shade[..., None]
    inner = WRECK * 0.18 * (0.5 + 0.5 * smoothstep(0, 21, hr))[..., None]
    rgb = np.where(hold[..., None], inner, gol)
    # growth lapping over the hull's sides and stern
    over = smoothstep(0.55, 0.75, nn + 0.35 * np.abs(across) / np.maximum(half, 1) - 0.25 * (along / L)) * (hull & ~hold)
    gm = over
    tis = TEAL_K * (0.6 + 0.7 * nn)[..., None] * shade[..., None]
    rgb = rgb * (1 - gm[..., None]) + tis * gm[..., None]
    rim = np.exp(-((q - 1) / 0.08) ** 2) * (q > 0.85)
    rgb = rgb * (1 - 0.45 * rim)[..., None]
    alpha = smoothstep(1.06, 0.94, q)
    return rgba(rgb, alpha)


# =========================================================================== the deep layer: towers, the spire, the shimmer

def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def deep_field():
    """One continuous deep field (y-up): the void below the citadel in the palette's darkest violet with a faint
    nebula, the citadel's towers far below (lobed growth round gold wreckage, lit from the top left, rims teal),
    seen through the decks' side chasms; the spire (section 3 on) a vast tower round a gold mast; the pale
    shimmer (Level 49's memory) rising at the top by t~235."""
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rgb = a84.space_rgb(W, n, SEED + 1, k=0.55)[::-1] * 0.8 + VOID * 0.2
    rows = (np.arange(n) + r0)[:, None]
    x = (np.arange(W) + 0.5)[None, :]
    rng = np.random.default_rng(SEED + 700)
    sp_row = scroll(176) * FACTORS["deep"] + SCREEN + 140
    towers = []
    for _ in range(46):
        r = rng.uniform(r0 + 100, sp_row - 300)
        side = rng.choice((-1, 1))
        cx = 240 + side * rng.uniform(150, 250)
        towers.append((cx, r, rng.uniform(26, 58)))
    towers.append((240, sp_row, 205.0))
    nn = raster.fbm(W, n, 32, SEED + 710, octaves=4, period=False)
    for cx, cr, rad in towers:
        d = np.hypot(x - cx, (rows - cr)) / rad
        if d.min() > 1.6:
            continue
        sl = slice(max(0, int(cr - r0 - rad * 1.7)), min(n, int(cr - r0 + rad * 1.7)))
        dd = d[sl]
        wob = 1 + 0.12 * (nn[sl] - 0.5) * 2 + 0.06 * np.sin(np.arctan2(rows[sl] - cr, x - cx) * 7)
        body = smoothstep(1.0, 0.94, dd / wob)
        z = np.sqrt(np.clip(1 - (dd / wob) ** 2, 0, 1))
        lx = (x - cx) / rad
        ly = (rows[sl] - cr) / rad                        # y-up: + toward the top of the screen
        lam = np.clip(0.3 + 0.55 * (-lx * 0.6 + ly * 0.6 + z * 0.6), 0.1, 1.2)
        col = TEAL_K * 0.55 * lam[..., None] * (0.7 + 0.6 * nn[sl])[..., None]
        gold = smoothstep(0.62, 0.7, nn[sl]) * smoothstep(0.85, 0.5, dd)
        col = col * (1 - gold[..., None]) + WRECK * 0.42 * lam[..., None] * gold[..., None]
        if rad > 100:                                     # the spire's gold mast at its heart
            m = smoothstep(0.2, 0.15, dd)
            col = col * (1 - m[..., None]) + WRECK * 0.5 * lam[..., None] * m[..., None]
        rimk = np.exp(-((dd / wob - 0.93) / 0.05) ** 2) * np.clip(ly - lx + 0.3, 0, 1)
        col = col + rimk[..., None] * MINT * 0.18
        shadow = smoothstep(1.6, 1.0, dd) * (1 - body)
        rgb[sl] = rgb[sl] * (1 - 0.5 * shadow[..., None]) * (1 - body[..., None]) + col * body[..., None]
    shim0 = scroll(222) * FACTORS["deep"] + SCREEN
    shim1 = scroll(240) * FACTORS["deep"] + SCREEN
    k = smoothstep(shim0, shim1, rows) * (0.75 + 0.25 * nn)
    pale = np.array((196, 186, 226.0)) * (0.5 + 0.5 * raster.fbm(W, n, 64, SEED + 720, octaves=3, period=False))[..., None]
    rgb = rgb * (1 - 0.6 * k[..., None]) + pale * 0.6 * k[..., None]
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    out = {}
    for name in DEEP:
        a = lo if name == "deep-1" else seam("deep", first_section(name))
        b = hi if name == DEEP[-1] else seam("deep", last_section(name) + 1)
        out[name] = cut_tile(field, r0, a, max(b, a + 1))
    return out


# =========================================================================== build the backdrop

def build_backdrop():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    for name in GROUND_TILES:
        rgb, a, v = tile(name)
        save(name, rgba(rgb, a))
        save(name + "-veins", additive_rgb((v * VEIN_PULSE)[..., None] * VEIN))
    for name, (field, _) in ground_pieces().items():
        body, veins = piece_images(field)
        save(name, body)
        save(name + "-veins", veins)
    for f in range(WELL_FRAMES):
        body, light = well_frame(f)
        save(f"spire-well_{f}", body)
        save(f"spire-well-glow_{f}", light)
    for side in "lr":
        body, frames = core_vein(side)
        save(f"core-vein-{side}", body)
        for k, fr in enumerate(frames):
            save(f"core-vein-{side}-glow_{k}", fr)
    for name in HULLS:
        save(name, gold_hull(name))
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


def pieces_spec():
    spec = {name: {"layer": "ground", "size": list(Image.open(OUT / f"{name}.png").size)}
            for name in ground_pieces()}
    spec["spire-well"] = {"layer": "ground", "size": list(WELL), "frames": WELL_FRAMES, "fps": 4}
    spec["spire-well-glow"] = {"layer": "ground", "size": list(WELL), "frames": WELL_FRAMES, "fps": 4}
    for side in "lr":
        spec[f"core-vein-{side}"] = {"layer": "ground", "size": list(VEIN_P)}
        spec[f"core-vein-{side}-glow"] = {"layer": "ground", "size": list(VEIN_P), "frames": VEIN_LOOP + VEIN_DARK,
                                          "fps": 8}
    for name, (size, *_rest) in HULLS.items():
        spec[name] = {"layer": "ground", "size": list(size)}
    return spec


# The reactor cores (the README: five pairs at t~50, 70, 92, 126, 150, one in each half, each in a gold wreck's open
# hull beside a turret nest, never under it): id, the time its centre crosses the top edge, x
CORES = [("C1", 50.0, 118), ("C2", 51.5, 356), ("C3", 70.0, 344), ("C4", 71.5, 128), ("C5", 92.0, 104),
         ("C6", 93.5, 372), ("C7", 126.0, 156), ("C8", 127.5, 326), ("C9", 150.0, 330), ("C10", 151.5, 150)]
ARCHIVE = (138.0, 380)                       # Vorne's archive: t at the top edge, x (the README)
CORE_HULLS = ("gold-hull-a", "gold-hull-b", "gold-hull-c")


def core_row(t_top):
    """The ground row (y-up) of a core whose centre crosses the screen's top edge at t_top."""
    return scroll(t_top) + SCREEN


def placements():
    out = []
    for name, (field, top) in ground_pieces().items():
        h = field[1].shape[0]
        centre = top - (h - 1) / 2
        note = {"seam-2": "the section 1/2 ground seam (exact rows)", "seam-3": "the section 2/3 ground seam (exact rows)",
                "heart": f"sections 4-6's ground, rows {int(top - h + 1)}-{int(top + 1)} (exact): the citadel's heart, "
                         f"its veins converging on the spire well"}[name]
        out.append({"piece": name, "t": t_ground(centre + 0.5), "x": 240, "note": note})
    wr = well_row()
    out.append({"piece": "spire-well", "t": t_ground(wr), "x": 240,
                "note": f"the spire well (row {wr:g}: its centre at screen y {WELL_Y} at the halt, the Chimera's rise "
                        f"from (240, {WELL_Y})); F4: frame 0 until the sting, 1-8 at the sting (4 fps), then 9-11 loop"})
    for k, (cid, tt, x) in enumerate(CORES):
        r = core_row(tt)
        hull = CORE_HULLS[k % 3]
        out.append({"piece": hull, "t": t_ground(r - HOLD[1]), "x": x - HOLD[0],
                    "note": f"core {cid}: its open hull (the hold on the core, row {r:g})"})
        side = "l" if x < 240 else "r"
        out.append({"piece": f"core-vein-{side}", "t": t_ground(r + VEIN_P[1] / 2 - 4), "x": x,
                    "note": f"core {cid}: its vein up toward the spire (bottom on the core); F4: -glow frame by the "
                            f"core's state"})
    r = core_row(ARCHIVE[0])
    out.append({"piece": "archive-hull", "t": t_ground(r - HOLD[1]), "x": ARCHIVE[1] - HOLD[0],
                "note": f"Vorne's archive: its open hull (the hold on the archive, row {r:g})"})
    return out


def fmt(v):
    return reef.fmt(v)


def proposal_text():
    src = "the level's data.yaml" if DATA and DATA.get("sections") else "the README's Layout table"
    lines = ["backdrop:",
             f"  # Presentation only: Level 48's citadel (tools/art/act7_citadel.py citadel, step ART-F, sections from {src}).",
             "  # Vacuum: no haze. Deep: one continuous field (the void below the citadel, its towers, the spire, the pale",
             "  # shimmer by t~235) cut at the sections' seams: deep-3 spans sections 3-6 (the spire held in the halt).",
             "  # Ground: a tile set a section (1 outer-growth, 2 gold-decks, 3 spire-road, 7 spire-wall); sections 4-6's",
             "  # ground is the `heart` piece (exact rows), which ends in the spire wall's own rows; seam pieces blend the",
             "  # tiles' own rows along a ragged line. Re-run the generator after moving a section.",
             "  # F4 hooks: (1) every ground `<tile>-veins` / `<piece>-veins` image is the veins' pulsing share (additive,",
             "  # not listed: C4's VeinPulse, k(t) <= 0.5 Hz); (2) `spire-well` + `-glow` by the boss: frame 0 shut until",
             "  # the sting, 1-8 the lid splitting (4 fps, 2 s), then 9-11 open (4 fps loop); (3) each `core-vein-l/r`",
             "  # piece belongs to the core whose place is its bottom centre (the order below, C1..C10): its `-glow` (drawn",
             "  # additive at the piece) loops 0-11 (8 fps: one pulse up the vein in 1.5 s) while the core lives, plays",
             "  # 12-17 once (6 fps: dark from the core outward over 1 s) at its death, then is not drawn; the vein piece",
             "  # itself stays (the dark channel). Until then the data's loop plays every frame.",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, high-air unused",
             "  ramp: 2",
             "  haze_colour: 07050c",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for name in DEEP:
        h = Image.open(OUT / f"{name}.png").height
        secs = [i + 1 for i, s in enumerate(SECTIONS) if name in (s.get("tiles") or [])]
        lines.append(f"    {name}: {{layer: deep, height: {h}}}   # section(s) {', '.join(map(str, secs))}")
    notes = {"outer-growth": "section 1: ridged growth over buried gold plating, side chasms",
             "gold-decks": "section 2: decks over whole gold hulls", "spire-road": "section 3: the causeway (x ~75-405)",
             "spire-wall": "section 7: the spire's inner wall round the dead gold mast"}
    for name in GROUND_TILES:
        lines.append(f"    {name}: {{layer: ground, height: {TILE_H}}}   # {notes[name]}")
    lines.append("  pieces:")
    for name, spec in pieces_spec().items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placements():
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {fmt(p['x'])}}}")
    lines.append("sections:   # ART-F's reading of the README's Layout (section 4 split at t=180 where the ease to the halt")
    lines.append("            # starts, as Level 40's braking section); F2 writes the real ones")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    lines.append("# F2: the reactor cores' places (ground, 40x40), each in its hull's hold; `at` = the scroll with its centre")
    lines.append("# at the top edge (row - 540), t = the time it crosses the top edge. Move a core and its two pieces together.")
    for cid, tt, x in CORES:
        r = core_row(tt)
        lines.append(f"#   - {{core: {cid}, t: {tt:g}, x: {x}, row: {r:g}, at: {r - SCREEN:g}}}")
    r = core_row(ARCHIVE[0])
    lines.append(f"#   - {{archive: vorne-archive, t: {ARCHIVE[0]:g}, x: {ARCHIVE[1]}, row: {r:g}, at: {r - SCREEN:g}}}")
    lines.append(f"# ground: section 1's chasms x < ~90 / > ~390 at their widest; section 2 x < ~40 / > ~440; the causeway "
                 f"(section 3) x ~75-405 (nothing ground-borne beyond); the spire well at row {well_row():g}, x 240 "
                 f"(r 110; keep the turret nests 150 px off it); the spire wall's windows x < ~90 / > ~390")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== composite (the reviews' screens)

_CACHE = {}


def img_of(name):
    if name not in _CACHE:
        _CACHE[name] = Image.open(OUT / f"{name}.png").convert("RGBA")
    return _CACHE[name]


def sprites(name):
    if ("spr", name) not in _CACHE:
        _CACHE[("spr", name)] = load(name)
    return _CACHE[("spr", name)]


def tile_rows(name, offset):
    key = ("tile", name)
    if key not in _CACHE:
        _CACHE[key] = np.array(img_of(name))
    a = _CACHE[key]
    h = len(a)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return a[h - 1 - (rows % h)], rows


def layer_tiles(t, layer, suffix=""):
    off = scroll(t) * FACTORS[layer]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec.get("tiles") or [] if (n in DEEP) == (layer == "deep")]
        if not names:
            continue
        a, rows = tile_rows(names[0] + suffix, off)
        sel = np.array([section_at_row(layer, r) == i for r in rows])
        out[sel] = a[sel]
    return Image.fromarray(out, "RGBA")


def piece_xy(p, layer, t):
    f = FACTORS[layer]
    return p["x"], SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)


def vein_k(t):
    return 0.5 + 0.5 * np.sin(TAU * 0.25 * t)


def well_frame_at(t):
    """The review's stand-in for F4's hook: shut before the sting (t=186), the lid splitting over 2 s, then open."""
    if t < 186.0:
        return 0
    if t < 188.0:
        return 1 + min(7, int((t - 186.0) * 4))
    return 9 + int(t * 4) % 3


def compose(t, k_veins=None, units=True, dead=(), well=None):
    spec = pieces_spec()
    kv = vein_k(t) if k_veins is None else k_veins
    img = layer_tiles(t, "deep")
    img.alpha_composite(layer_tiles(t, "ground"))
    a = np.array(img).astype(np.float64)
    a[..., :3] += np.array(layer_tiles(t, "ground", "-veins")).astype(np.float64)[..., :3] * kv
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    core_k = {}
    for p in placements():
        name = p["piece"]
        x, y = piece_xy(p, "ground", t)
        hh = spec[name]["size"][1]
        if y < -hh or y > SCREEN + hh:
            continue
        if name == "spire-well":
            fr = well_frame_at(t) if well is None else well
            img = l23kit.paste_c(img, img_of(f"spire-well_{fr}"), x, y)
            img = l23kit.add_c(img, img_of(f"spire-well-glow_{fr}"), x, y)
            continue
        img = l23kit.paste_c(img, img_of(name), x, y)
        if name.startswith("core-vein"):
            cid = p["note"].split()[1].rstrip(":")
            fr = (VEIN_LOOP + VEIN_DARK - 1) if cid in dead else int(t * 8) % VEIN_LOOP
            img = l23kit.add_c(img, img_of(f"{name}-glow_{fr}"), x, y)
        if (OUT / f"{name}-veins.png").exists():
            img = l23kit.add_c(img, img_of(name + "-veins"), x, y, kv)
    if units:
        img = stand_ins(img, t, dead)
    return img


def stand_ins(img, t, dead=()):
    """The cores and the archive in their hulls (the held sprites at their places), the ship, a few Vrell bullets."""
    try:
        core, stump, glow = sprites("reactor-core"), sprites("reactor-core-stump"), sprites("reactor-core-glow")
        arch = sprites("vorne-archive")
        ship = l23kit.ship()
    except FileNotFoundError:
        return img
    for cid, tt, x in CORES:
        y = SCREEN - (core_row(tt) - scroll(t))
        if -30 < y < SCREEN + 30:
            if cid in dead:
                img = l23kit.paste_c(img, stump[0], x, y)
            else:
                img = l23kit.paste_c(img, core[0], x, y)
                img = l23kit.add_c(img, glow[int(t * 4) % len(glow)], x, y)
    y = SCREEN - (core_row(ARCHIVE[0]) - scroll(t))
    if -30 < y < SCREEN + 30:
        img = l23kit.paste_c(img, arch[0], ARCHIVE[1], y)
    img = l23kit.paste_c(img, ship, 240, 470)
    for k, (bx, by) in enumerate(((150, 320), (210, 290), (330, 250))):
        a84.u84.vorb(img, bx, by, 4, (a84.TEAL, a84.VIOLET)[k % 2])
    return img


def chimera_at(img, x, y, phase=1, bank=2, dead=(), sacs=(1, 1, 1), opening=None, closing=None, extra=()):
    """The Chimera composited as F4 would draw it: body, parts in their states, overlays (review stand-in)."""
    body = {1: "vornes-chimera", 2: "vornes-chimera-tissue", 3: "vornes-chimera-open"}[phase]
    b = sprites(body)[bank] if opening is None else sprites("vornes-chimera-opening")[opening]
    ox, oy = x - BC[0] + BODY[0] / 2, y - BC[1] + BODY[1] / 2
    img = l23kit.paste_c(img, b, ox, oy)
    if phase == 1:
        for side, (px, py) in (("l", BATT[0]), ("r", BATT[1])):
            st = 2 if f"battery-{side}" in dead else 0
            img = l23kit.paste_c(img, sprites("vornes-chimera-battery")[st], x + px, y + py)
            if st < 2:
                pk = 3 if side == "l" else 14
                img = l23kit.paste_c(img, sprites("vornes-chimera-panel")[pk], x + px, y + py)
        img = l23kit.paste_c(img, sprites("vornes-chimera-rack")[5 if "rack" in dead else 0], x + RACK[0], y + RACK[1])
        img = l23kit.paste_c(img, sprites("vornes-chimera-bay")[5 if "bay" in dead else 0], x + BAY[0], y + BAY[1])
    else:
        img = l23kit.paste_c(img, sprites("vornes-chimera-bay")[5], x + BAY[0], y + BAY[1])
        for k, (px, py) in enumerate(SOCKETS.values()):
            s = sacs[k]
            if s == 0:
                img = l23kit.paste_c(img, sprites("vornes-chimera-sac-dead")[0], x + px, y + py)
            elif s < 0:
                img = l23kit.paste_c(img, sprites("vornes-chimera-sac-grow")[min(5, -s - 1)], x + px, y + py)
            else:
                img = l23kit.paste_c(img, sprites("vornes-chimera-sac")[0], x + px, y + py)
                img = l23kit.add_c(img, sprites("vornes-chimera-sac-core")[0], x + px, y + py, 0.8)
    if closing is not None:
        img = l23kit.paste_c(img, sprites("vornes-chimera-closing")[closing], x + CAPS[0], y + CAPS[1])
    for name, fr, dx, dy in extra:
        img = l23kit.add_c(img, sprites(name)[fr], x + dx, y + dy)
    return img


# =========================================================================== reviews

def plate_bg(w, h, name="gold-decks", y0=200):
    try:
        a = np.array(img_of(name))[y0:y0 + h, :w]
    except FileNotFoundError:
        return Image.new("RGBA", (w, h), artkit.PLATE)
    bg = Image.new("RGBA", (a.shape[1], a.shape[0]), (10, 6, 18, 255))
    bg.alpha_composite(Image.fromarray(a, "RGBA"))
    full = Image.new("RGBA", (w, h), (10, 6, 18, 255))
    full.alpha_composite(bg)
    return full


def strip(frames, cell, scale=2, add=None, under=None, bg="gold-decks", step=6):
    out = Image.new("RGBA", (len(frames) * (cell[0] * scale + step), cell[1] * scale), (0, 0, 0, 0))
    base = plate_bg(cell[0], cell[1], bg)
    for i, fr in enumerate(frames):
        c = base.copy()
        if under is not None:
            c = l23kit.paste_c(c, under, cell[0] / 2, cell[1] / 2)
        if fr is not None:
            c = l23kit.paste_c(c, fr, cell[0] / 2, cell[1] / 2)
        if add is not None:
            c = l23kit.add_c(c, add[i] if isinstance(add, list) else add, cell[0] / 2, cell[1] / 2)
        out.alpha_composite(c.resize((cell[0] * scale, cell[1] * scale), Image.NEAREST),
                            (i * (cell[0] * scale + step), 0))
    return out


def sheet_of(title, rows, width=1000):
    hgt = 40 + sum(r.height + 30 for _, r in rows)
    sheet = raster.sheet(width, hgt, title, f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for text, r in rows:
        label(sheet, 12, y, text)
        if r.width > width - 24:
            r = r.resize((width - 24, int(r.height * (width - 24) / r.width)), Image.LANCZOS)
        sheet.alpha_composite(r, (12, y + 12))
        y += r.height + 30
    return sheet.crop((0, 0, width, y))


SCREENS = (8.0, 30.0, 52.0, 76.0, 100.0, 128.0, 152.0, 172.0, 186.5, 192.0, 222.0, 244.0)


def review_backdrop():
    dead = {"C1", "C4"}
    shots = [compose(t, k_veins=1.0, dead=dead if t > 60 else ()) for t in SCREENS]
    w = 1000
    rows_h = 3 * 290
    sheet = raster.sheet(w, 40 + rows_h + 30 + 240, "LEVEL 48 BACKDROP (FINAL R93): THE CITADEL",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, (t, s) in enumerate(zip(SCREENS, shots)):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        raster.draw_text(sheet, x, y, f"T={t:g} S ({SECTIONS[section_at_t(t)]['name'].upper()})", raster.LABEL)
        sheet.alpha_composite(s.resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + rows_h + 6
    raster.draw_text(sheet, 12, y, "SCREENS AT HALF SIZE, VEINS AT THEIR PULSE'S PEAK, CORES C1 AND C4 DEAD AFTER T=60 "
                                   "(THEIR VEINS DARK). THE TILES (QUARTER SIZE): OUTER GROWTH, GOLD DECKS, SPIRE ROAD, "
                                   "SPIRE WALL; THE HEART PIECE", raster.LABEL)
    x = 12
    for name in list(GROUND_TILES) + ["heart"]:
        im = img_of(name)
        bg = Image.new("RGBA", im.size, (10, 6, 18, 255))
        bg.alpha_composite(im)
        bg = l23kit.add_c(bg, img_of(name + "-veins"), im.width / 2, im.height / 2)
        sm = bg.resize((im.width // 4, im.height // 4), Image.LANCZOS)
        sheet.alpha_composite(sm.crop((0, 0, sm.width, 220)), (x, y + 14))
        x += sm.width + 8
    frames = [compose(t, dead={c for c, tt, _ in CORES[:4] if t > tt + 5}).resize((240, 270), Image.LANCZOS)
              for t in list(np.arange(0, 186, 3.0)) + [186.0, 186.5, 187.0, 187.5] + list(np.arange(190, END, 3.0))]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=6)


def review_props():
    vl = [img_of(f"core-vein-l-glow_{k}") for k in range(VEIN_LOOP + VEIN_DARK)]
    vein_row = Image.new("RGBA", (len(vl) * 50, VEIN_P[1] // 2), (0, 0, 0, 0))
    bg = plate_bg(VEIN_P[0], VEIN_P[1], "gold-decks", 0)
    for k, g in enumerate(vl):
        c = l23kit.paste_c(bg.copy(), img_of("core-vein-l"), VEIN_P[0] / 2, VEIN_P[1] / 2)
        c = l23kit.add_c(c, g, VEIN_P[0] / 2, VEIN_P[1] / 2)
        vein_row.alpha_composite(c.resize((VEIN_P[0] // 2 * 1, VEIN_P[1] // 2), Image.LANCZOS), (k * 50, 0))
    hulls = Image.new("RGBA", (4 * 130, 160), (0, 0, 0, 0))
    for k, name in enumerate(HULLS):
        im = img_of(name)
        c = plate_bg(124, 160, "gold-decks", 300)
        c = l23kit.paste_c(c, im, 62, 80)
        occ = sprites("vorne-archive")[0] if name == "archive-hull" else sprites("reactor-core")[0]
        c = l23kit.paste_c(c, occ, 62 + HOLD[0], 80 + HOLD[1])
        if name != "archive-hull":
            c = l23kit.add_c(c, sprites("reactor-core-glow")[0], 62 + HOLD[0], 80 + HOLD[1])
        hulls.alpha_composite(c, (k * 130, 0))
    well = Image.new("RGBA", (WELL_FRAMES * 84, 74), (0, 0, 0, 0))
    for f in range(WELL_FRAMES):
        c = plate_bg(WELL[0], WELL[1], "heart", 300)
        c = l23kit.add_c(l23kit.paste_c(c, img_of(f"spire-well_{f}"), WELL[0] / 2, WELL[1] / 2),
                         img_of(f"spire-well-glow_{f}"), WELL[0] / 2, WELL[1] / 2)
        well.alpha_composite(c.resize((80, 70), Image.LANCZOS), (f * 84, 0))
    core = sprites("reactor-core")
    rows = [("THE REACTOR CORE (40X40, 2X): WHOLE, CRACKED, DEAD (THE STUMP); ITS GLOW'S 8 FRAMES OVER THE WHOLE CORE",
             strip(core + sprites("reactor-core-stump"), (40, 40), 2) ),
            ("", strip([core[0]] * 8, (64, 64), 2, add=sprites("reactor-core-glow"))),
            ("DESTROYED (VACUUM): THE GOLD-WHITE FLASH AND DEBRIS RING (ADDITIVE), THE HOUSING'S CHIPS (2X)",
             strip([None] * 8, (96, 96), 1, add=sprites("reactor-core-death"))),
            ("", strip(sprites("reactor-core-tatters"), (64, 64), 2)),
            ("VORNE'S ARCHIVE (40X40, 2X): SEALED, CRACKED, BROKEN OPEN; ITS BREAK (64X64)",
             strip(sprites("vorne-archive"), (40, 40), 2)),
            ("", strip(sprites("vorne-archive-break"), (64, 64), 2)),
            ("THE OPEN GOLD HULLS (1X): A, B, C WITH A CORE IN THE HOLD; THE ARCHIVE'S HULL", hulls),
            ("A CORE'S VEIN (HALF SIZE): 0-11 THE PULSE UP TOWARD THE SPIRE (8 FPS), 12-17 GOING DARK FROM THE CORE "
             "OUTWARD (6 FPS)", vein_row),
            ("THE SPIRE WELL (QUARTER SIZE): 0 SHUT, 1-8 THE LID SPLITTING AT THE STING, 9-11 OPEN", well)]
    sheet = sheet_of("LEVEL 48 PROPS (FINAL R93): THE REACTOR CORES, THEIR VEINS, THE ARCHIVE, THE WELL", rows)
    gif = []
    for f in range(24):
        c = plate_bg(240, 200, "gold-decks", 100)
        c = l23kit.paste_c(c, img_of("gold-hull-a"), 70, 100)
        c = l23kit.paste_c(c, img_of("core-vein-l"), 70 + HOLD[0], 100 + HOLD[1] - VEIN_P[1] / 2 + 4)
        alive = f < 14
        fr = int(f * 8 / 6) % VEIN_LOOP if alive else min(VEIN_LOOP + VEIN_DARK - 1, VEIN_LOOP + (f - 14))
        c = l23kit.add_c(c, img_of(f"core-vein-l-glow_{fr}"), 70 + HOLD[0], 100 + HOLD[1] - VEIN_P[1] / 2 + 4)
        if alive:
            c = l23kit.paste_c(c, core[0], 70, 106)
            c = l23kit.add_c(c, sprites("reactor-core-glow")[f % 8], 70, 106)
        else:
            c = l23kit.paste_c(c, sprites("reactor-core-stump")[0], 70, 106)
            if f - 14 < 8:
                c = l23kit.add_c(c, sprites("reactor-core-death")[f - 14], 70, 106)
                c = l23kit.paste_c(c, sprites("reactor-core-tatters")[f - 14], 70, 106)
        c = l23kit.paste_c(c, img_of("archive-hull"), 180, 100)
        c = l23kit.paste_c(c, sprites("vorne-archive")[0 if f < 10 else 1 if f < 16 else 2], 180, 106)
        if 16 <= f < 24:
            c = l23kit.paste_c(c, sprites("vorne-archive-break")[f - 16], 180, 106)
        gif.append(c.resize((480, 400), Image.NEAREST))
    artkit.save_review(sheet, gif, CONCEPT, "l48-props", fps=6)


def review_chimera():
    x, y = 240, 140
    bg_t = HALT_T

    def field(t=bg_t, well=9):
        return compose(t, k_veins=0.6, units=False, well=well)
    base = field()
    gs = a84.gold_share(chimera_at(Image.new("RGBA", (W, SCREEN), (0, 0, 0, 0)), x, y)
                        .crop((x - 120, y - 148, x + 120, y + 100)))
    phases = [chimera_at(base.copy(), x, y, 1), chimera_at(base.copy(), x, y, 2),
              chimera_at(base.copy(), x, y, 3, extra=[("vornes-chimera-shield", 2, 0, 10)])]
    prow = Image.new("RGBA", (3 * 250, 280), (0, 0, 0, 0))
    for k, im in enumerate(phases):
        prow.alpha_composite(im.crop((x - 120, y - 150, x + 120, y + 120)), (k * 250, 0))
    banks = Image.new("RGBA", (5 * 200, 230), (0, 0, 0, 0))
    for k in range(5):
        im = chimera_at(base.copy(), x, y, 1, bank=k)
        banks.alpha_composite(im.crop((x - 120, y - 150, x + 120, y + 120)).resize((192, 216), Image.LANCZOS),
                              (k * 200, 0))
    p = PART_SIZES
    parts = [("BATTERY WHOLE/DAMAGED/DEAD, THE PANEL'S TWO FACINGS AND DOWN, RACK 0-5 (HATCH, DAMAGED, DEAD), "
              "BAY 0-5 (DOORS, DAMAGED, DEAD) (2X)",
              strip(sprites("vornes-chimera-battery") + [sprites("vornes-chimera-panel")[3],
                                                          sprites("vornes-chimera-panel")[14],
                                                          sprites("vornes-chimera-panel-down")[3]], (64, 64), 2)),
             ("", strip(sprites("vornes-chimera-rack"), p["rack"], 2)),
             ("", strip(sprites("vornes-chimera-bay"), p["bay"], 2)),
             ("SAC 0-4 (REST, SWELLING, SPLIT), DEAD; THE FUSION'S SAC GROWING OUT OF A SOCKET (2X)",
              strip(sprites("vornes-chimera-sac") + sprites("vornes-chimera-sac-dead") +
                    sprites("vornes-chimera-sac-grow"), (60, 60), 2, bg="heart"))]
    fus = Image.new("RGBA", (8 * 124, 132), (0, 0, 0, 0))
    for k in range(8):
        im = chimera_at(base.copy(), x, y, 1, dead=("battery-l", "battery-r", "rack", "bay"),
                        extra=[("vornes-chimera-fusion", k, 0, -24)])
        fus.alpha_composite(im.crop((x - 120, y - 150, x + 120, y + 120)).resize((120, 132), Image.LANCZOS),
                            (k * 124, 0))
    opn = Image.new("RGBA", (6 * 124, 132), (0, 0, 0, 0))
    for k in range(6):
        im = chimera_at(base.copy(), x, y, 2, sacs=(0, 0, 0), opening=k)
        opn.alpha_composite(im.crop((x - 120, y - 150, x + 120, y + 120)).resize((120, 132), Image.LANCZOS),
                            (k * 124, 0))
    clo = Image.new("RGBA", (9 * 110, 132), (0, 0, 0, 0))
    for k in range(9):
        im = chimera_at(base.copy(), x, y, 3, sacs=(0, 0, 0), closing=k)
        clo.alpha_composite(im.crop((x - 60, y - 70, x + 60, y + 80)).resize((100, 125), Image.LANCZOS), (k * 110, 0))
    tells = [("THE TELLS (1X OVER THE BODY): LANCE NOSE, PORT FLASH, SPIRAL PULSE, FAR-FLANK FLARE, RACK LOCK + BAY "
              "LIGHT, PANEL RIM; THE SWEEP MARK AT THE BOTTOM EDGE; K'S BURST", None)]
    tl = Image.new("RGBA", (6 * 164, 200), (0, 0, 0, 0))
    sets = [[("vornes-chimera-muzzle", 5, *NOSE)], [("vornes-chimera-port-flash", 2, *pt) for pt in PORTS],
            [("vornes-chimera-pulse", 4, 0, 0)], [("vornes-chimera-flare-r", 4, *FLANK["r"])],
            [("vornes-chimera-rack-lock", 0, RACK[0], RACK[1] - 6), ("vornes-chimera-bay-light", 0, *BAY)],
            [("vornes-chimera-panel-rim", 3, *BATT[0]), ("vornes-chimera-panel-rim", 14, *BATT[1])]]
    for k, ex in enumerate(sets):
        im = chimera_at(base.copy(), x, y, 2 if k in (2, 3) else 1, extra=ex)
        tl.alpha_composite(im.crop((x - 120, y - 150, x + 120, y + 120)).resize((160, 180), Image.LANCZOS), (k * 164, 0))
    sw = Image.new("RGBA", (8 * 124, 60), (0, 0, 0, 0))
    for k in range(8):
        c = field(150.0, None).crop((180, 480, 300, 540))
        c = l23kit.add_c(c, sprites("vornes-chimera-sweep-mark")[k], 60, 60)
        sw.alpha_composite(c, (k * 124, 0))
    br = strip([None] * 10, (192, 192), 1, add=sprites("vornes-chimera-burst"), bg="heart")
    rows = [(f"THE THREE PHASES AT 1X OVER THE HALTED HEART (PHASE 1 GOLD SHARE {gs * 100:.0f} %, TARGET 35 %); PHASE 3 "
             "WITH THE HEX SHIELD", prow),
            ("BANKING (PHASE 1): -12, -6, 0, +6, +12 DEGREES (RE-RENDERED, KEY LIGHT FIXED)", banks)] + parts + [
            ("THE FUSION'S PAIN (OVER THE DEAD PHASE-1 PARTS): 0-1 THE TELL, 2-7 THE BURST", fus),
            ("THE CAPSULE OPENS (2 S): SEALED TO OPEN, VORNE'S SILHOUETTE IN THE SEAT", opn),
            ("K: THE TISSUE CLOSING OVER THE CAPSULE (4.5 S, 2 FPS)", clo),
            (tells[0][0], tl), ("", sw), ("", br)]
    sheet = sheet_of("VORNE'S CHIMERA (FINAL R93)", rows)
    # the GIF: a scripted pass at 1x over the halted heart (the rise, phase 1 tells, the fusion, phase 2, the sweep's
    # mark, the opening, phase 3, K)
    gif = []
    for f in range(72):
        t = 186.0 + f * 0.1
        im = compose(min(t, HALT_T), k_veins=0.6, units=False, well=min(8, f // 2) if f < 16 else 9 + f % 3)
        if f < 16:                                         # the rise out of the well (4 s compressed)
            u = (f + 1) / 16
            yy = WELL_Y + (y - WELL_Y) * u
            lay = chimera_at(Image.new("RGBA", (W, SCREEN), (0, 0, 0, 0)), x, yy, 1)
            a = arr(lay)
            a[..., :3] *= 0.4 + 0.6 * u
            im.alpha_composite(img_of_arr(a))
        elif f < 28:
            bk = 2 + int(np.round(np.sin((f - 16) / 12 * TAU) * 1.4))
            ex = [("vornes-chimera-muzzle", min(5, f - 16), *NOSE)] if f < 22 else \
                [("vornes-chimera-port-flash", f % 4, *pt) for pt in PORTS]
            im = chimera_at(im, x + 30 * np.sin((f - 16) / 12 * TAU), y, 1, bank=bk, extra=ex)
        elif f < 36:
            im = chimera_at(im, x, y, 1, dead=("battery-l", "battery-r", "rack", "bay"),
                            extra=[("vornes-chimera-fusion", f - 28, 0, -24)])
        elif f < 42:
            im = chimera_at(im, x, y, 2, sacs=(-(f - 35),) * 3)
        elif f < 50:
            im = chimera_at(im, x, y, 2, extra=[("vornes-chimera-flare-r", f - 42, *FLANK["r"])])
        elif f < 56:
            im = l23kit.add_c(im, sprites("vornes-chimera-sweep-mark")[min(7, (f - 50) * 2)], 360, SCREEN)
        elif f < 62:
            im = chimera_at(im, x, y, 2, sacs=(0, 0, 0), opening=f - 56)
        elif f < 66:
            im = chimera_at(im, x, y, 3, sacs=(0, 0, 0), extra=[("vornes-chimera-shield", f % 8, 0, 10)])
        else:
            im = chimera_at(im, x, y, 3, sacs=(0, 0, 0), closing=min(8, (f - 66) * 2))
            if f >= 70:
                im = l23kit.add_c(im, sprites("vornes-chimera-burst")[(f - 70) * 3], x, y + CAPS[1])
        im = l23kit.paste_c(im, l23kit.ship(), 240, 470)
        gif.append(im)
    artkit.save_review(sheet, gif, CHIM_C, "vornes-chimera", fps=8)
    return gs


def review_portrait():
    big = [Image.open(PORT / f"briefing-vorne-fused-{m}.png").convert("RGBA") for m in MOODS]
    small = [Image.open(PORT / f"radio-vorne-fused-{m}.png").convert("RGBA") for m in MOODS]
    r1 = Image.new("RGBA", (3 * 152, 144), (0, 0, 0, 0))
    for k, im in enumerate(big):
        r1.alpha_composite(im, (k * 152, 0))
    r2 = Image.new("RGBA", (3 * 224 + 3 * 80, 216), (0, 0, 0, 0))
    for k, im in enumerate(small):
        r2.alpha_composite(im.resize((216, 216), Image.NEAREST), (k * 224, 0))
        r2.alpha_composite(im, (3 * 224 + k * 80, 0))
    sheet = sheet_of("VORNE FUSED (FINAL R93): ROUND 84 A.12 A ON HIS PRODUCTION PORTRAITS",
                     [("BRIEFING 144: NEUTRAL, SMUG, RAGE (THE SHOUT)", r1), ("RADIO 72 AT 3X, AND AT 1X", r2)])
    sheet.convert("RGB").save(CHARS_C / f"vorne-fused-final-{ROUND}-a.png", optimize=True)
    print(f"review: {(CHARS_C / f'vorne-fused-final-{ROUND}-a.png').relative_to(ROOT)}")


# =========================================================================== check

SETS = {"chimera": [("vornes-chimera", 5, BODY), ("vornes-chimera-tissue", 5, BODY), ("vornes-chimera-open", 5, BODY),
                    ("vornes-chimera-opening", 6, BODY), ("vornes-chimera-battery", 3, (52, 52)),
                    ("vornes-chimera-panel", 18, (64, 64)), ("vornes-chimera-panel-down", 18, (64, 64)),
                    ("vornes-chimera-panel-cracks", 18, (64, 64)), ("vornes-chimera-panel-glow", 18, (64, 64)),
                    ("vornes-chimera-panel-rim", 18, (64, 64)), ("vornes-chimera-panel-sweep", 1, (24, 24)),
                    ("vornes-chimera-rack", 6, PART_SIZES["rack"]), ("vornes-chimera-rack-lock", 1, (20, 20)),
                    ("vornes-chimera-bay", 6, PART_SIZES["bay"]), ("vornes-chimera-bay-light", 1, (84, 40)),
                    ("vornes-chimera-sac", 5, (60, 60)), ("vornes-chimera-sac-dead", 1, (60, 60)),
                    ("vornes-chimera-sac-core", 1, (44, 44)), ("vornes-chimera-sac-heal", 1, (60, 60)),
                    ("vornes-chimera-sac-flare", 1, (60, 60)), ("vornes-chimera-sac-burst", 6, (64, 64)),
                    ("vornes-chimera-sac-grow", 6, (60, 60)), ("vornes-chimera-port-flash", 4, (32, 32)),
                    ("vornes-chimera-plate", 8, (16, 16)), ("vornes-chimera-fusion", 8, BODY),
                    ("vornes-chimera-closing", 9, CAPSULE_FX), ("vornes-chimera-capsule-cracks", 1, CAPSULE_FX),
                    ("vornes-chimera-shield", 8, CAPSULE_FX), ("vornes-chimera-muzzle", 6, (48, 48)),
                    ("vornes-chimera-pulse", 8, (96, 96)), ("vornes-chimera-flare-l", 8, (88, 180)),
                    ("vornes-chimera-flare-r", 8, (88, 180)), ("vornes-chimera-sweep-mark", 8, (192, 96)),
                    ("vornes-chimera-burst", 10, (192, 192))],
        "props": [("reactor-core", 2, (40, 40)), ("reactor-core-stump", 1, (40, 40)), ("reactor-core-glow", 8, (64, 64)),
                  ("reactor-core-death", 8, (96, 96)), ("reactor-core-tatters", 8, (64, 64)),
                  ("vorne-archive", 3, (40, 40)), ("vorne-archive-break", 8, (64, 64))]}
CUT = {"seam-2", "seam-3", "heart"}
SCAN_RUN, SCAN_T = 120, 8.0


def _runs(d):
    """Per row of d: the longest run of consecutive entries with d > SCAN_T, or with d < -SCAN_T."""
    best = np.zeros(len(d), int)
    for sgn in (1, -1):
        on = (sgn * d) > SCAN_T
        run = np.zeros(len(d), int)
        cur = np.zeros(len(d), int)
        for j in range(d.shape[1]):
            cur = np.where(on[:, j], cur + 1, 0)
            run = np.maximum(run, cur)
        best = np.maximum(best, run)
    return best


def straight_scan(img, wrap=False):
    """The longest ruler-straight line (step FIXC-SPINE's measure): for every pair of adjacent rows (and columns), the
    longest run of pixels along them whose luminance (alpha-premultiplied) steps by more than SCAN_T the same way; a
    straight edge keeps it the whole way, a curved or organic one leaves the row within a few dozen px. Returns
    (longest row run px, its row, longest column run px, its column); SCAN_RUN px or more is a straight line."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    lum = (a[..., :3] @ np.array((0.299, 0.587, 0.114))) * a[..., 3] / 255.0
    out = []
    for k, L in enumerate((lum, lum.T)):
        d = np.diff(np.vstack([L, L[:1]]) if wrap and k == 0 else L, axis=0)
        r = _runs(d)
        i = int(np.argmax(r))
        out += [int(r[i]), i]
    return tuple(out)


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n, size in sets:
            try:
                frames = load(name)
            except FileNotFoundError:
                frames = []
            where = bu.where(name)
            files = [f for f in (ROOT / where).glob(f"{name}*.png")
                     if f.stem == name or (f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
            src = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
            vis = chk = 0
            for f in files:
                v, c = checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            lit = sum(lit_border(fr) for fr in frames) if name in ADDITIVE else 0
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src and share < 0.05 and not lit
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %{f', {lit} lit border px' if lit else ''}")
    for m in MOODS:
        for kind in ("radio", "briefing"):
            p = PORT / f"{kind}-vorne-fused-{m}.png"
            ok = p.exists() and Image.open(p).text.get(artkit.SOURCE_KEY) == SOURCE
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {p.relative_to(ROOT)}")
    spec = pieces_spec() if OUT.exists() else {}
    worst = (0.0, "")
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        v, c = checker_share(im)
        share = c / max(v, 1)
        ok, msg = im.text.get(artkit.SOURCE_KEY) == SOURCE and share < 0.05, []
        stem = p.stem
        base = stem.split("_")[0]
        if stem in DEEP or stem in GROUND_TILES:
            ws = wrap_score(np.array(im.convert("RGBA")))
            ok &= ws <= (1.5 if stem in GROUND_TILES else 3.0)
            msg.append(f"wrap {ws:.2f}")
        if not (base.endswith("-glow") or base.endswith("-veins")) and stem not in DEEP:
            rs, ri, cs, ci = straight_scan(im, wrap=stem in GROUND_TILES)
            ok &= max(rs, cs) < SCAN_RUN
            msg.append(f"longest straight run: rows {rs} px (row {ri}), columns {cs} px (column {ci})")
            worst = max(worst, (max(rs, cs), stem))
        if base.endswith("-glow") or (base.endswith("-veins") and base.removesuffix("-veins") not in CUT
                                      and base.removesuffix("-veins") not in GROUND_TILES):
            lit = lit_border(im)
            ok &= lit == 0
            msg.append(f"edge {lit}")
        if base in spec:
            ok &= list(im.size) == spec[base]["size"]
        bad += not ok
        if not ok or "_" not in stem or stem.endswith("_0"):
            print(f"{'ok ' if ok else 'BAD'} {p.name}: {im.size}, checker {share * 100:.1f} %, {', '.join(msg)}")
    print(f"straight-line scan (a step > {SCAN_T:g} the same way along a row or column; straight >= {SCAN_RUN} px): "
          f"longest {worst[0]} px ({worst[1]})")
    return bad == 0


# =========================================================================== jobs and main

JOBS = {"body": _body, "prop": _prop}


def _job(job):
    os.nice(0)
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "act7-citadel"


def _model_hash():
    text = Path(__file__).read_text(encoding="utf-8")
    a, b = text.index("# =========================================================================== the Chimera's body"), \
        text.index("# =========================================================================== the Chimera's parts")
    c, d = text.index("# =========================================================================== the reactor core"), \
        text.index("def core_glow(")
    return hashlib.sha1((text[a:b] + text[c:d]).encode()).hexdigest()[:12]


def run(jobs):
    """The renders, cached in the system temp dir by the model code's hash and the job (<= 6 workers)."""
    CACHE.mkdir(parents=True, exist_ok=True)
    h = _model_hash()
    paths = [CACHE / f"{hashlib.sha1((h + repr(j)).encode()).hexdigest()[:20]}.png" for j in jobs]
    todo = [i for i, path in enumerate(paths) if not path.exists()]
    if todo:
        with ProcessPoolExecutor(max_workers=min(6, os.cpu_count() or 1)) as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(path).convert("RGBA") for path in paths]


PARTS = ("chimera", "props", "portrait", "citadel")

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    parts = [a for a in args if a in PARTS] or list(PARTS)
    if "--review" not in args:
        jobs = (body_jobs() if "chimera" in parts else []) + (prop_jobs() if "props" in parts else [])
        res = run(jobs) if jobs else []
        if "chimera" in parts:
            build_chimera(res[:len(body_jobs())])
            res = res[len(body_jobs()):]
        if "props" in parts:
            build_props(res)
        if "portrait" in parts:
            build_portrait()
        if "citadel" in parts:
            build_backdrop()
            write_proposal()
    if "citadel" in parts:
        review_backdrop()
    if "props" in parts or "citadel" in parts:
        review_props()
    if "chimera" in parts:
        print(f"gold share (phase 1, bank 0, with its parts): {review_chimera() * 100:.1f} %")
    if "portrait" in parts:
        review_portrait()
