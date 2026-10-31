#!/usr/bin/env python3
"""Production art: Level 31's escort and units (M9 part D batch, concept round 64, step ART-D): the mining tug
*Bellwether* (design/allies/mining-tug, round 60 A.19 **b** "pressure hull", drawn at **200x300** per J.6 b), the
Hornet (design/enemies/air/hornet, A.2 **b** "delta hover-boat"), the Shard Drone and its air links
(design/enemies/space/shard-drone, A.9 **b** "tri-prism, lightning links"), the Buzzsaw Drone
(design/enemies/air/buzzsaw-drone, A.6 **b** "gold blades") and Dacosta's claim rock (Level 31's secret, a debris
chunk like Level 30's dead buoy).

The models are round 60's (tools/concept/enemies_r60.py ``hornet_model``, ``shard_model``, ``buzzsaw_model``,
tools/concept/belt_r60.py ``tug_scene``/``tug_mats``; imported unchanged) in **Q.11 b** for the Ascendancy units:
gunmetal and charcoal hulls (tools/art/belt_units.py ``gunmetal``) with their gold, red running lights and the 1 px
gold/red rim (belt_units.py ``rim``), gold kept to about 20-30 % of the silhouette (round 62 flagged the Talon's
42-50 % as heavy). The tug keeps its own friendly look (warm white and yellow, cool running lights, no red, no rim).

**The tug at 200x300** (m9d-gaps.md §6, QD-3 a, recommended): round 60's b hull (124x264 opaque in its 160x260
concept) re-proportioned to fill 200x300: the hull, the side tanks and the habitat window rows widened x1.61, the
hull lengthened x1.14, the three drive bells kept at their concept size (spread a little to the wider stern). One
heading (nose up; it never turns: the sway is a translation). Each section is its own frame set, rendered alone
(its parts clipped to its band) and cut to its band, so the game draws the three over each other at their offsets;
the section hit boxes are **measured from the production alpha** (``--boxes``; written to the proposal for D2).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down. Radial units spin
in frames over their symmetry's turn (frame k = k x 7.5 degrees clockwise).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule plus
an ally's sprites once a level's escort names it or the allies' data gives it a ``first_level``; an enemy's root once
``design/enemies/<layer>/<slug>/data.yaml`` exists; ``debris-<chunk>`` once a level's ``debris`` names the chunk):
  mining-tug-bow_0..3        208x96, nose up: the bow (bridge, crane, BELLWETHER painted on): 0 intact, 1 damaged
                             (below 50 %: burn patches, lights on), 2 the same with its lights out (the flicker's off
                             step), 3 lost (dark, a scorched outline)
  mining-tug-cargo_0..3      208x128: the cargo (the habitat hull, side tanks, window rows: the refugees), same order
  mining-tug-engines_0..3    208x96: the engines (fuel waist, one big and two small drive bells), same order
  mining-tug-glow_0..3       32x56, additive: one bell's blue-white drive glow pointing down (4 flicker steps), drawn
                             at each living bell (pivots ``bells``, the small ones at their ``scale``)
  mining-tug-flare_0..3      48x112, additive: the climb-out flare (the living engines at full burn), same points
  mining-tug-vent_0..7       24x64: a section's venting plume in vacuum (thin, pale, drifting back down the screen
                             with the scroll; a loop), drawn at the section's smoke points while below 50 % and lost
  mining-tug-burn_0..7       96x96, additive: a section's short fire flare when it is lost (the hull holds air)
  assets/pivots/mining-tug.json: the sections' frame offsets from the tug's centre, their measured opaque boxes and
                             hit boxes (90 %), the smoke points, the bells (y down, px from the tug's centre)
  hornet_0..15               80x80, facing down (fixed): hatch x 4 + fan (hatch 0 dark .. 3 lit gold: the 0.5 s
                             tell; fan 0..3: the four lift fans' rotors, 30 degrees a step over the 120 degree loop)
  hornet-hatch-glow_0..3     80x80, additive: the four hatches' gold bloom (tell 0.25 .. 1.0), over the hull
  hornet-launch_0..3         24x24, additive: a missile leaving a wing-root cell (pivots ``cells``)
  shard-drone_0..15          40x40: the tri-prism spinning (frame k = k x 7.5 degrees, the 120 degree loop)
  shard-drone-link_0..7      32x16: a lit link: crackling crimson lightning with a gold core (dark rim), a strip that
                             tiles along the link (both ends on the centre row), 8 crackle steps
  shard-drone-link-tell_0..3 32x16: the tell's flicker (a thin dim crimson thread, broken; frame 3 empty)
  shard-drone-link-snap_0..5 40x40, additive: a link snapping (a crimson-white flash and gold sparks)
  shard-drone-shard_0..7     12x12: a gold prism spinning off at the death (8 turns)
  buzzsaw-drone_0..7         36x36: six gold blades round a gunmetal hub, spinning (7.5 degrees a frame, the 60
                             degree loop)
  buzzsaw-drone-tell_0..7    36x36: the same, the gold flickered red (the spin-up tell)
  buzzsaw-drone-blur_0..3    36x36: the spun-up blur (dashing at 6 turns/s: four smears 15 degrees apart)
  buzzsaw-drone-ricochet_0..5  32x32, additive: a bounce's sparks fanning up (rotate to the bounce's normal)
  buzzsaw-drone-blade_0..7   12x12: a blade flung off at the death (8 turns)
  debris-claim-rock_0..15    40x40: Dacosta's claim (the secret, 12 HP): a small hollow rock, the prospector's claim
                             marker bolted to it (an orange plate on a post, a dim green lamp), 16 turns of the spin
  debris-claim-rock-break_0..7  56x56: the rock shot open: chips flung out, the crate's amber light
  Review (round 64): design/allies/mining-tug/concept/mining-tug-final-r64-a.png/.gif, design/enemies/air/concept/
  hornet-final-r64-a, buzzsaw-drone-final-r64-a, design/enemies/space/concept/shard-drone-final-r64-a,
  design/campaign/act-5-the-belt/level-31-mining-rig-escort/concept/claim-rock-final-r64-a (sheet only)

Run: python3 tools/art/belt_units_d.py [tug] [hornet] [shard] [saw] [claim] [--review] [--boxes] [--check]
     (no subject = all, ~4 min; --review rebuilds only the review files from the written frames; --boxes prints the
     tug's measured boxes; --check lists every set with its folder, size and frame count and fails on a missing one
     or a missing Source chunk)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import belt_units as bu
import belt_units_c as buc
import l23kit
from artkit import DESIGN, ROOT, TAU

import belt_r60 as b60  # noqa: E402  (concept scripts, imported unchanged: the round-60 models)
import enemies_r60 as e60  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_y, rotate_z, sd_box, sd_capsule, sd_cylinder_y,  # noqa: E402
                        sd_cylinder_z, sd_ellipsoid, sd_plate, sd_sphere, subtract, union)

SCRIPT = "belt_units_d.py"
BATCH = "M9 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r64"
artkit.REVIEW_ROUND = ROUND
FPS = 20
A_X = e60.A_X
L31 = DESIGN / "campaign" / "act-5-the-belt" / "level-31-mining-rig-escort"
CONCEPT = {"tug": DESIGN / "allies" / "mining-tug" / "concept", "hornet": DESIGN / "enemies" / "air" / "concept",
           "shard": DESIGN / "enemies" / "space" / "concept", "saw": DESIGN / "enemies" / "air" / "concept",
           "claim": L31 / "concept"}
TUG_PROPOSAL = CONCEPT["tug"] / "allies-proposal.yaml"
BACKDROP = ROOT / "assets" / "backdrop" / "level-31"
load, where = bu.load, bu.where
gunmetal, rim, turned, render = bu.gunmetal, bu.rim, bu.turned, bu.render
grid, field_img = buc.grid, buc.field_img


# =========================================================================== held sprites (+ the escorted allies)

def _ally_slugs():
    """The allies the game claims: a level's escort ally, or an ally with a ``first_level`` (SpriteUse)."""
    slugs = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        esc = ((l23kit._yaml(f) or {}).get("objectives") or {}).get("escort")
        if isinstance(esc, dict) and esc.get("ally"):
            slugs.add(str(esc["ally"]))
    allies = (l23kit._yaml(DESIGN / "allies" / "data.yaml") or {}).get("allies") or {}
    slugs |= {slug for slug, a in allies.items() if isinstance(a, dict) and a.get("first_level")}
    return slugs


_belt_claimed = bu.claimed


def claimed(name):
    return _belt_claimed(name) or any(l23kit._rooted(name, s) for s in _ally_slugs())


l23kit.claimed = claimed                    # l23kit's write/load/where decide by its module's ``claimed``


def save_set(name, frames, single=False):
    l23kit.write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    held = ROOT / "build" / "art-held" / "pivots"
    folder = artkit.PIVOTS if claimed(root) else held
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = held if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def pivot_path(root):
    p = artkit.PIVOTS / f"{root}.json"
    return p if p.exists() else ROOT / "build" / "art-held" / "pivots" / f"{root}.json"


# =========================================================================== the tug (A.19 b at 200x300)

T_PX = 40.0                                  # px per model unit (the concept's)
SX, SY = 1.61, 1.14                          # QD-3 a: across, along (the hull fills 200x300)
WAIST = 1.35 * SY                            # the section joints (model y, up)
TUG_W = 208
SECTION_FRAMES = {"bow": (2.65, 96), "cargo": (0.0, 128), "engines": (-2.65, 96)}   # frame centre (units, y up), px
BANDS = {"bow": (WAIST, 9.0), "cargo": (-WAIST, WAIST), "engines": (-9.0, -WAIST)}
STATES = ("intact", "damaged", "flicker", "lost")
T_HULL, T_YEL, T_GREY, T_DARK, T_WIN, T_LAMP, T_ENG, T_OCHRE, T_BLUE, T_GLASS = range(10)
BELLS = [(0.0, -3.3, 1.0), (-0.95, -3.12, 0.6), (0.95, -3.12, 0.6)]   # the bells' mouths (units, y up), glow scale


def _stripes(y, m_on, m_off, width=0.22):
    return np.where(((y / width) % 2) < 1, m_on, m_off)


def tug_scene(section):
    """Round 60's b hull (belt_r60.tug_scene 'b') re-proportioned for 200x300: one enclosed hull (an ellipsoid
    pinched at the two waists), the bow's bridge glass, crane and running lights, the cargo's habitat hull with
    window rows and two side tanks, the engines' hazard waist and the three bells (concept size). Nose up = +y."""
    y0, y1 = BANDS[section]

    def clip(d, y):
        return np.maximum(d, np.maximum(y0 - y, y - y1))

    def scene(p):
        x, y = p[:, 0], p[:, 1]
        q = mirror_x(p)
        waist = 0.2 * np.exp(-((y - WAIST) / (0.12 * SY)) ** 2) + 0.2 * np.exp(-((y + WAIST) / (0.12 * SY)) ** 2)
        body = sd_ellipsoid(p, (0, 0.06, 0.05), (1.3 * SX, 3.62, 0.56)) + waist * 0.5
        body = np.maximum(body, np.abs(x) - 1.15 * SX + waist * 1.4)
        items = []
        if section == "bow":
            m = np.where(y < 1.62 * SY, _stripes(y + x * 0.7, T_YEL, T_DARK, 0.16), T_HULL)
            items += [(clip(body, y), m),
                      (sd_box(p, (0, 2.62 * SY, 0.46), (0.5 * SX, 0.19, 0.08), 0.05), T_GLASS),
                      (sd_box(p, (0, 2.65 * SY, 0.52), (0.44 * SX, 0.06, 0.05), 0.02), T_WIN),
                      (sd_box(q, (0.62 * SX, 2.25 * SY, 0.4), (0.16, 0.05, 0.04), 0.02), T_WIN),
                      (sd_capsule(p, (-0.62 * SX, 1.92 * SY, 0.55), (-0.98 * SX, 2.75 * SY, 0.6), 0.085), T_YEL),
                      (sd_capsule(p, (-0.98 * SX, 2.75 * SY, 0.6), (-0.82 * SX, 2.98 * SY, 0.62), 0.06), T_GREY),
                      (sd_box(p, (0.72 * SX, 2.0 * SY, 0.38), (0.22, 0.2, 0.1), 0.04), T_GREY),
                      (sd_sphere(q, (0.86 * SX, 2.55 * SY, 0.32), 0.075), T_LAMP)]
        elif section == "cargo":
            m = np.where(np.abs(x) < 0.55 * SX, T_HULL, T_BLUE)
            items.append((clip(body, y), m))
            for cx in (-1.25 * SX, 1.25 * SX):              # the side tanks, widened
                items.append((sd_capsule(p, (cx, -1.06 * SY, 0.15), (cx, 1.06 * SY, 0.15), 0.38), T_GREY))
                items.append((sd_box(p, (cx, 0.0, 0.42), (0.05, 1.0 * SY, 0.04)), T_DARK))
            wy = (y + 1.0) % 0.3
            for wx in (0.38 * SX, 1.0 * SX):                # the habitat window rows (inner and outer)
                win = sd_box(q, (wx, 0.0, 0.5 if wx < 0.8 else 0.38), (0.05, 1.12 * SY, 0.08))
                items.append((np.maximum(win, np.abs(wy - 0.15) - 0.06), T_WIN))
            for cy in (-0.7 * SY, 0.0, 0.7 * SY):
                items.append((sd_capsule(p, (-1.25 * SX, cy, 0.46), (1.25 * SX, cy, 0.46), 0.03), T_DARK))
            items.append((sd_sphere(q, (1.25 * SX, 1.2 * SY, 0.5), 0.07), T_LAMP))
        else:
            m = np.where(y > -1.62 * SY, _stripes(y + x * 0.7, T_YEL, T_DARK, 0.16), T_GREY)
            items.append((clip(body, y), m))
            items.append((sd_capsule(p, (0, -2.75, 0.2), (0, -3.3, 0.2), 0.3, 0.46), T_GREY))
            items.append((sd_cylinder_z(p, (0, -3.3, 0.2), 0.36, 0.06), T_ENG))
            for cx in (-0.95, 0.95):
                items.append((sd_capsule(p, (cx, -2.72, 0.15), (cx, -3.12, 0.15), 0.14, 0.24), T_GREY))
                items.append((sd_cylinder_z(p, (cx, -3.12, 0.15), 0.18, 0.05), T_ENG))
            items.append((sd_box(q, (0.9 * SX, -2.15 * SY, 0.36), (0.26, 0.32, 0.12), 0.05), T_GREY))
            items.append((sd_sphere(q, (1.0 * SX, -1.9 * SY, 0.38), 0.075), T_LAMP))
        return union(*items)
    return scene


def _burns(seed, k, dim):
    """Damage at 200x300: a few broad soft scorch patches with soot streaks trailing back down the hull (the concept's
    finer blotches read as spots at this size); the whole section dimmed when lost."""
    rng = np.random.default_rng(seed)
    spots = [(rng.uniform(-1.6, 1.6), rng.uniform(-3.4, 3.4), rng.uniform(0.28, 0.5)) for _ in range(14)]

    def f(p, n):
        x, y = p[:, 0], p[:, 1]
        burn = np.zeros(len(p))
        for sx_, sy_, r in spots:
            dy = sy_ - y                                  # soot trails below a patch (down the screen: -y)
            d = np.hypot((x - sx_) / r, np.where(dy > 0, dy / (r * 2.6), dy / r))
            burn = np.maximum(burn, np.clip(1.25 - d, 0, 1))
        grain = 0.85 + 0.15 * np.sin(x * 23 + seed) * np.sin(y * 19 - seed)
        return (1 - 0.62 * k * burn * grain) * (1 - 0.45 * dim)
    return f


def tug_mats(state, glow, seed):
    """belt_r60.tug_mats with the production's damage pattern (``_burns``)."""
    k = {"intact": 0.0, "damaged": 0.5, "lost": 1.0}[state]
    mats = b60.tug_mats("intact", glow * (0.0 if state == "lost" else 1.0), seed)
    if not k:
        return mats
    pat = _burns(seed * 31 + 7, 0.9 if state == "damaged" else 1.0, 0.0 if state == "damaged" else 1.0)
    hull_p = (lambda p, n, a=sdf.panel_lines(5.0), b=pat: a(p, n) * b(p, n))
    out = []
    for i, m in enumerate(mats):
        if i == T_HULL:
            out.append(replace(m, pattern=hull_p))
        elif i in (T_YEL, T_GREY, T_OCHRE, T_BLUE):
            out.append(replace(m, pattern=pat))
        else:
            out.append(m)
    return out


def tug_frame_job(job):
    section, state = job
    cy, h = SECTION_FRAMES[section]
    concept_state = {"intact": "intact", "damaged": "damaged", "flicker": "damaged", "lost": "lost"}[state]
    glow = 0.25 if state == "flicker" else 1.0
    mats = tug_mats(concept_state, glow, seed={"bow": 1, "cargo": 2, "engines": 3}[section])
    scene = tug_scene(section)

    def shifted(p, s=scene):
        q = p.copy()
        q[:, 1] = q[:, 1] + cy
        return s(q)
    return render(shifted, mats, (TUG_W, h), TUG_W / T_PX, crisp=70)


def tug_name(img):
    """BELLWETHER hand-painted on the bow's upper hull (behind the bridge), dark on the white."""
    nm = Image.new("RGBA", (64, 9), (0, 0, 0, 0))
    raster.draw_text(nm, 1, 1, "BELLWETHER", (44, 40, 34))
    nm = nm.resize((52, 7), Image.LANCZOS)
    a = np.array(nm)
    a[..., 3] = np.where(a[..., 3] > 90, 230, 0)
    out = img.copy()
    out.alpha_composite(Image.fromarray(a, "RGBA"), (TUG_W // 2 - 26, 56))
    return out


def scorch_outline(img):
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    edge = op & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    a[edge, :3] = a[edge, :3] * 0.3 + np.array((120, 54, 20)) * 0.7
    a[op & ~edge, :3] *= 0.78
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def tug_boxes(frames):
    """The measured boxes: per section the opaque box in the tug's px (y down from its centre) and the hit box at
    90 % of it (m9d-gaps.md §6's convention)."""
    out = {}
    for s, fr in frames.items():
        a = np.array(fr[0])[..., 3] > 0
        ys, xs = np.nonzero(a)
        cy = -SECTION_FRAMES[s][0] * T_PX
        h = SECTION_FRAMES[s][1]
        x0, x1 = xs.min() - TUG_W / 2, xs.max() + 1 - TUG_W / 2
        y0, y1 = ys.min() - h / 2 + cy, ys.max() + 1 - h / 2 + cy
        out[s] = {"opaque": [int(x1 - x0), int(y1 - y0)], "centre": [round((x0 + x1) / 2, 1), round((y0 + y1) / 2, 1)],
                  "hit": [int(round(0.9 * (x1 - x0))), int(round(0.9 * (y1 - y0)))]}
    return out


def smoke_points():
    """Per section the vents' points (y down, px from the tug's centre): the waist seams and the tank tops."""
    s = T_PX
    return {"bow": [[round(-0.7 * SX * s), round(-1.75 * SY * s)], [round(0.55 * SX * s), round(-2.3 * SY * s)]],
            "cargo": [[round(-1.25 * SX * s), round(-0.6 * SY * s)], [round(1.25 * SX * s), round(0.35 * SY * s)],
                      [round(0.3 * SX * s), round(-0.2 * SY * s)]],
            "engines": [[round(-0.7 * SX * s), round(1.8 * SY * s)], [round(0.8 * SX * s), round(2.1 * SY * s)]]}


def bell_glow(f, n=4):
    """One bell's drive glow, pointing down: a short blue-white cone with a white core at the mouth."""
    w, h = 32, 56
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    x, y = xx - w / 2, yy - 10
    k = (0.9, 1.0, 0.85, 0.95)[f % n]
    length = 26 + 4 * (f % 2)
    cone = np.exp(-(x / (5.5 + 0.12 * np.clip(y, 0, None))) ** 2) * np.exp(-np.clip(y, 0, None) / length) * (y > -3)
    core = np.exp(-(np.hypot(x, y) / 5.0) ** 2)
    return field_img((0.7 * cone + core) * k, (120, 180, 255), white=0.72)


def bell_flare(f, n=4):
    w, h = 48, 112
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    x, y = xx - w / 2, yy - 12
    k = (0.95, 1.0, 0.9, 1.0)[f % n]
    length = 60 + 8 * (f % 2)
    cone = np.exp(-(x / (7 + 0.12 * np.clip(y, 0, None))) ** 2) * np.exp(-np.clip(y, 0, None) / length) * (y > -4)
    core = np.exp(-(np.hypot(x, y * 0.7) / 8.0) ** 2) * 1.2
    return field_img((0.85 * cone + core) * k, (150, 200, 255), white=0.6)


def vent(f, n=8):
    """A thin venting plume in vacuum: a pale grey jet leaving the point and widening as it drifts down (the scroll
    carries it back), its density rippling down the plume (a loop); 3 translucency steps."""
    w, h = 24, 64
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    u = np.clip((yy - 3) / (h - 6), 0, 1)
    width = 1.2 + 6.0 * u
    wobble = 1.6 * np.sin(yy * 0.19 - TAU * f / n) * u
    core = np.exp(-((xx - w / 2 - wobble) / width) ** 2)
    ripple = 0.7 + 0.3 * np.sin(yy * 0.42 - TAU * f / n * 2)
    dens = core * ripple * (1 - u) ** 1.2 * (yy > 2)
    a = np.zeros((h, w, 4))
    a[..., :3] = np.array((200, 204, 210.0))
    a[..., 3] = artkit.smooth_steps(dens * 1.1) * 200
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def burn(i, n=8):
    """A section lost: a short fire flare (air burning out of the hull) and sparks, then dark."""
    w = 96
    x, y = grid(w)
    u = (i + 0.5) / n
    r = np.hypot(x, y * 1.15)
    flash = np.exp(-(r / (10 + 26 * u)) ** 2) * max(0.0, 1.25 - 1.2 * u)
    tongues = np.clip(np.sin(np.arctan2(y, x) * 5 + 1.3) * 0.5 + 0.5, 0, 1) * np.exp(-((r - 18 - 20 * u) / 7) ** 2)
    img = field_img(flash + 0.5 * tongues * max(0.0, 1 - u), (255, 140, 50), white=0.72)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(640 + i)
    for _ in range(10):
        a = rng.uniform(0, TAU)
        dist = (8 + 36 * u) * rng.uniform(0.7, 1.1)
        if i < 7:
            d.point((w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist), fill=(255, 210, 120, 255))
    return img


def build_tug():
    jobs = [(s, st) for s in SECTION_FRAMES for st in STATES]
    with ProcessPoolExecutor() as pool:
        res = dict(zip(jobs, pool.map(tug_frame_job, jobs, chunksize=1)))
    frames = {}
    for s in SECTION_FRAMES:
        fr = [res[(s, st)] for st in STATES]
        if s == "bow":
            fr = [tug_name(f) for f in fr[:3]] + [fr[3]]
        fr[3] = scorch_outline(fr[3])
        frames[s] = artkit.quantize_set(fr[:3], 64) + artkit.quantize_set(fr[3:], 32)
        save_set(f"mining-tug-{s}", frames[s])
    save_set("mining-tug-glow", [bell_glow(f) for f in range(4)])
    save_set("mining-tug-flare", [bell_flare(f) for f in range(4)])
    save_set("mining-tug-vent", [vent(f) for f in range(8)])
    save_set("mining-tug-burn", [burn(i) for i in range(8)])
    boxes = tug_boxes(frames)
    smoke = smoke_points()
    pivots = {"size": [200, 300], "headings": 1, "sections": {}, "bells": [], "frames": list(STATES),
              "note": "y down, px from the tug's centre: each section's frame is drawn centred at its offset; box the "
                      "measured opaque box (centre and size), hit 90 % of it (centred on the box); smoke the vents' "
                      "points; bells the drive bells' mouths with the glow's scale"}
    for s, (cy, h) in SECTION_FRAMES.items():
        pivots["sections"][s] = {"offset": [0, int(round(-cy * T_PX))], "frame": [TUG_W, h],
                                 "box": {"centre": boxes[s]["centre"], "size": boxes[s]["opaque"]},
                                 "hit": boxes[s]["hit"], "smoke": smoke[s]}
    for bx, by, k in BELLS:
        pivots["bells"].append({"at": [round(bx * T_PX, 1), round(-by * T_PX, 1)], "scale": k})
    write_pivots("mining-tug", pivots)
    write_tug_proposal(pivots)
    return frames


def write_tug_proposal(pivots):
    """The allies' data proposal for step D2 (never data): the tug's size and its sections with the boxes measured
    from the production alpha."""
    lines = ["# Proposal (step ART-D, tools/art/belt_units_d.py): the mining tug's entry for design/allies/data.yaml,",
             "# measured from the production sprites (the sections' opaque boxes from their alpha; the hit boxes 90 %",
             "# of them, m9d-gaps.md §6's convention). Offsets and boxes in px from the tug's centre, y DOWN; the",
             "# smoke points are the vents'. D2 merges it with the README's numbers (damaged_by, hp) into the data.",
             "mining-tug:",
             "  size: [200, 300]",
             "  follows: hull",
             "  layer: ground",
             "  sections:"]
    for s, sec in pivots["sections"].items():
        cx, cy = sec["box"]["centre"]
        w, h = sec["hit"]
        ow, oh = sec["box"]["size"]
        pts = ", ".join(f"[{x}, {y}]" for x, y in sec["smoke"])
        lines.append(f"    - {{name: {s}, offset: [{cx:g}, {cy:g}], box: [{w}, {h}], smoke: [{pts}]}}"
                     f"   # opaque {ow}x{oh}")
    lines.append("# bells (the drive glow's points, the look's): " + ", ".join(
        f"[{b['at'][0]:g}, {b['at'][1]:g}] x{b['scale']}" for b in pivots["bells"]))
    lines.append("# §6's concept-measured reference: bow 130x76 at (0, -108), cargo 180x110 at (0, -4), "
                 "engines 134x84 at (0, +103)")
    TUG_PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    TUG_PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {TUG_PROPOSAL.relative_to(ROOT)}")


def tug_composite(states=None, flicker=False):
    """The whole tug (208x308) from its three section frames, as the game composes it."""
    states = states or {s: "intact" for s in SECTION_FRAMES}
    img = Image.new("RGBA", (TUG_W, 308), (0, 0, 0, 0))
    for s in ("engines", "cargo", "bow"):
        fr = load(f"mining-tug-{s}")
        st = states[s]
        idx = {"intact": 0, "damaged": 2 if flicker else 1, "lost": 3}[st]
        cy, h = SECTION_FRAMES[s]
        img.alpha_composite(fr[idx], (0, int(round(154 - cy * T_PX - h / 2))))
    return img


# =========================================================================== the Hornet (A.2 b, gunmetal)

HOR_SIZE, HOR_EXT = 80, 2.13                 # the delta about 72 x 62 px
HOR_FANS = [(0.42, -0.3), (0.22, 0.22)]      # model (x, y) of the lift fans (mirrored), +Y forward
HOR_CELLS = [(0.55, 0.05), (0.66, -0.12)]    # the missile cells' hatches (mirrored)


def hornet_model(hatch=0.0, fan=0.0):
    """Round 60's Hornet b (enemies_r60.hornet_model 'b') with turning rotors in its four lift fans (3 blades,
    ``fan`` the rotor's turn in radians) and the hatches lit gold by ``hatch``; Q.11 b's gunmetal."""
    def scene(p):
        q = mirror_x(p)
        delta = [(0.0, 1.0), (0.95, -0.5), (0.82, -0.72), (0.0, -0.6)]
        d, m = union((sd_plate(q, delta, 0.0, 0.12, 0.0, taper=e60.am._roof(0.9)), A_HULL))
        lead = (q[:, 1] > 1.0 - 1.58 * q[:, 0] - 0.3) & (m == A_HULL)
        m = np.where(lead, A_GOLD, m)
        holes = np.minimum(*[sd_cylinder_z(q, (fx, fy, 0.1), 0.12, 0.12) for fx, fy in HOR_FANS])
        d, m = subtract((d, m), holes)
        items = [(d, m)]
        for j, (fx, fy) in enumerate(HOR_FANS):
            items.append((sd_cylinder_z(q, (fx, fy, -0.02), 0.13, 0.03), A_FACET))
            for side in (1, -1):                        # each rotor its own (the turn is not mirrored)
                c = np.array((side * fx, fy, 0.0))
                for b in range(3):
                    r = rotate_z(p - c, fan * side + b * TAU / 3 + j * 0.5)
                    items.append((sd_box(r, (0.06, 0.0, 0.01), (0.06, 0.018, 0.02)), A_GUN))
                items.append((sd_cylinder_z(p, (side * fx, fy, 0.02), 0.03, 0.03), A_GUN))
        items += [(sd_plate(p, [(0, 0.7), (0.09, 0.45), (0, 0.32), (-0.09, 0.45)], 0.12, 0.03), A_GLASS),
                  (sd_sphere(q, (0.86, -0.55, 0.06), 0.05), A_RED),
                  (sd_cylinder_y(q, (0.1, -0.6, 0.0), 0.08, 0.05), A_ENGINE)]
        for hx_, hy in HOR_CELLS:
            items.append((sd_box(q, (hx_, hy, 0.12), (0.06, 0.07, 0.02)), A_X))
        return union(*items)
    mats = gunmetal(e60.belt_mats("c8202a", 1.0, extra=[e60.emissive(e60.GOLD_F, 0.08 + 1.8 * hatch)]))
    mats[A_GUN] = Material((0.3, 0.31, 0.34), metal=0.7, shininess=80, spec=0.9)
    return scene, mats


def _hornet(hatch, fan):
    return render(*turned(*hornet_model(hatch, fan), 0, 16), HOR_SIZE, HOR_EXT)


def _hornet_cells():
    """The hatches alone, lit (an emission mask) for the additive hatch bloom."""
    scene, mats = hornet_model(1.0)
    mats = bu.emission_only(mats)
    mats[A_RED] = Material((0, 0, 0))
    mats[A_ENGINE] = Material((0, 0, 0))
    return render(*turned(scene, mats, 0, 16), HOR_SIZE, HOR_EXT, crisp=0)


def hatch_glow(mask, k):
    a = np.array(mask).astype(np.float64)
    m = (a[..., :3].max(axis=-1) > 40).astype(float)
    blur = np.array(Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.2))) / 255.0
    return field_img(np.clip((blur * 2.4 + m * 0.6) * k, 0, 1), (255, 190, 70), white=0.78)


def launch_flash(i):
    x, y = grid(24)
    k = (1.0, 0.75, 0.45, 0.2)[i]
    f = np.exp(-(np.hypot(x, y * 0.8) / (2.2 + 1.2 * i)) ** 2) * k
    img = field_img(f, (255, 170, 70), white=0.7)
    d = ImageDraw.Draw(img)
    if i < 3:
        for a in (1.2, 1.57, 1.94):                      # a few sparks thrown down and out of the cell
            r0, r1 = 3 + 2 * i, 6 + 3 * i
            d.line([12 + np.cos(a) * r0, 12 + np.sin(a) * r0, 12 + np.cos(a) * r1, 12 + np.sin(a) * r1],
                   fill=(255, 220, 140, 255))
    return img


# =========================================================================== the Shard Drone (A.9 b, gunmetal)

SH_SIZE, SH_EXT = 40, 2.35                   # the tri-prism about 36 px across its prisms


def shard_model():
    """Round 60's Shard Drone b (enemies_r60.shard_model 'b': a charcoal triangular frame, a gold prism at each
    corner, the red hub) in Q.11 b's gunmetal, its frame struts and hub thickened (strut 0.08, hub 0.36 units: the
    concept's 1-2 px struts took the gold rim all along and read 48-52 % gold; now about 26 %)."""
    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0.1), 0.36, 0.1), A_HULL), (sd_sphere(p, (0, 0, 0.2), 0.11), A_RED)]
        for a in np.linspace(0, TAU, 3, endpoint=False):
            r = rotate_z(p, a)
            items += [(sd_capsule(r, (0.25, 0.0, 0.1), (0.7, 0.0, 0.1), 0.1, 0.1), A_HULL),
                      (sd_ellipsoid(r, (0.84, 0.0, 0.12), (0.2, 0.13, 0.12)), A_GOLD)]
        for a in np.linspace(0, TAU, 3, endpoint=False) + np.pi / 3:
            r = rotate_z(p, a)
            items.append((sd_capsule(r, (0.42, -0.42, 0.08), (0.42, 0.42, 0.08), 0.08, 0.08), A_HULL))
        return union(*items, k=0.02)
    return scene, gunmetal(e60.belt_mats("c8202a"))


def _shard(k):
    return render(*turned(*shard_model(), k, 48), SH_SIZE, SH_EXT, crisp=60)


def _shard_piece(i):
    """One gold prism flung off (8 turns)."""
    mats = gunmetal(e60.belt_mats("c8202a"))

    def scene(p):
        q = rotate_y(rotate_z(p, TAU * i / 8), 0.4 + 0.8 * np.sin(TAU * i / 8))
        return union((sd_ellipsoid(q, (0, 0, 0), (0.42, 0.27, 0.25)), A_GOLD),
                     (sd_capsule(q, (-0.5, 0, 0), (-0.25, 0, 0), 0.1), A_HULL))
    return render(scene, mats, 12, 12 / 10.0, crisp=40)


def link_strip(i, n=8, w=32, h=16):
    """A lit link's tile: crimson lightning with a gold core and a dark rim, both ends on the centre row (so the
    strips join along a link); the crackle new every step."""
    k = 4
    big = Image.new("RGBA", (w * k, h * k), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    rng = np.random.default_rng(6400 + i)
    for layer in range(2):                                  # a faint second bolt under the main one
        pts = []
        segs = 6
        for s in range(segs + 1):
            off = 0.0 if s in (0, segs) else rng.uniform(-3.6, 3.6) * (0.55 if layer else 1.0)
            pts.append((s * w * k / segs, (h / 2 + off) * k))
        if layer:
            d.line(pts, fill=(140, 16, 24, 150), width=int(2 * k))
            continue
        d.line(pts, fill=(36, 6, 10, 255), width=int(6 * k))
        d.line(pts, fill=e60.CRIMSON + (255,), width=int(4 * k))
        d.line(pts, fill=(255, 214, 120, 255), width=int(1.3 * k))
    sp = big.resize((w, h), Image.LANCZOS)
    a = np.array(sp)
    a[..., 3] = np.where(a[..., 3] > 100, 255, np.where(a[..., 3] > 40, 128, 0))
    return Image.fromarray(a, "RGBA")


def link_tell(i, w=32, h=16):
    """The tell: a thin dim crimson thread, broken differently each step (the 0.8 s flicker); step 3 dark."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    if i == 3:
        return img
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(6500 + i)
    x = 0.0
    while x < w:
        seg = rng.uniform(3, 9)
        if rng.uniform() < 0.65:
            y = h / 2 + rng.choice((-0.5, 0.0, 0.5))
            d.line([x, y, min(w - 1, x + seg), y], fill=(255, 90, 80, 150 if i != 1 else 200))
        x += seg + rng.uniform(1, 4)
    return img


def link_snap(i, n=6):
    w = 40
    x, y = grid(w)
    u = (i + 0.5) / n
    img = field_img(np.exp(-(np.hypot(x, y) / (2.5 + 6 * u)) ** 2) * (1.25 - u), (255, 70, 60), white=0.65)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(6600 + i)
    for _ in range(8):
        a = rng.uniform(0, TAU)
        r = (3 + 15 * u) * rng.uniform(0.8, 1.1)
        if i < 5:
            d.point((w / 2 + np.cos(a) * r, w / 2 + np.sin(a) * r), fill=(255, 200, 90, 255))
    return img


# =========================================================================== the Buzzsaw Drone (A.6 b, gunmetal)

SAW_SIZE, SAW_EXT = 36, 2.37                 # the disc about 32 px across its blade tips


def saw_model():
    """Round 60's Buzzsaw b (enemies_r60.buzzsaw_model 'b': six gold blades round the hub) in Q.11 b's gunmetal:
    the hub gunmetal, the hub ring charcoal, the blades charcoal with gold only on their outer part beyond 0.8 of
    the radius (the cutting edges: about 33 % gold; the whole blade gold reads 47 %, over round 62's limit), the
    red eye."""
    scene0, mats = e60.buzzsaw_model("b")

    def scene(p):
        d, m = scene0(p)
        r = np.hypot(p[:, 0], p[:, 1])
        m = np.where((m == A_GOLD) & (r < 0.8), A_FACET, m)
        return d, m
    return scene, gunmetal(mats)


def _saw(k):
    return render(*turned(*saw_model(), k, 48), SAW_SIZE, SAW_EXT, crisp=60)


def _saw_blade(i):
    mats = gunmetal(e60.belt_mats("6a6e78"))
    blade = [(0.24, 0.12), (0.72, 0.42), (1.05, 0.3), (0.98, 0.12), (0.34, -0.1)]

    def scene(p):
        q = rotate_y(rotate_z(p, TAU * i / 8), 0.3 + 0.7 * np.sin(TAU * i / 8 + 1))
        q = q + np.array((0.64, 0.16, 0.0))
        return union((sd_plate(q, blade, 0.0, 0.05, 0.0), A_GOLD))
    return render(scene, mats, 12, 12 / 10.0, crisp=40)


def redden(img, k=0.8):
    return e60.redden(img, k)


def saw_blur(frames, k):
    """Four consecutive spin frames (22.5 degrees) averaged: the spun-up smear; alpha kept 1-bit."""
    arr = np.stack([np.array(frames[(k + j) % len(frames)]).astype(np.float64) for j in range(4)])
    a = arr[..., 3:4] / 255
    cov = a.mean(axis=0)
    rgb = (arr[..., :3] * a).sum(axis=0) / np.maximum(a.sum(axis=0), 1e-6)
    out = np.dstack([rgb, np.where(cov[..., 0] >= 0.5, 255, 0)])
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")


def ricochet(i, n=6):
    """A bounce off an edge or a big rock: a white-gold flash and sparks fanning up (away from the surface below:
    the game turns it to the bounce's normal)."""
    w = 32
    x, y = grid(w)
    y = y - 6
    u = (i + 0.5) / n
    img = field_img(np.exp(-(np.hypot(x, y) / (2 + 4 * u)) ** 2) * (1.2 - u), (255, 200, 120), white=0.6)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(6700 + i)
    cx, cy = w / 2, w / 2 + 6
    for _ in range(9):
        a = -np.pi / 2 + rng.uniform(-1.2, 1.2)
        r0, r1 = (2 + 12 * u) * rng.uniform(0.7, 1.0), (5 + 15 * u) * rng.uniform(0.8, 1.1)
        if i < 5:
            d.line([cx + np.cos(a) * r0, cy + np.sin(a) * r0, cx + np.cos(a) * r1, cy + np.sin(a) * r1],
                   fill=(255, 226, 150, 255) if i < 3 else (230, 150, 70, 255))
    return img


# =========================================================================== Dacosta's claim rock (the secret)

CLAIM_SIZE, CLAIM_RADIUS, CLAIM_SEED = 40, 0.82, 3131


def claim_model():
    """A small belt rock (belt_units.rock_model) with a hollow bored into it and the prospector's claim marker bolted
    on: a short post, an orange claim plate, a dim green lamp."""
    rock, mats = bu.rock_model(CLAIM_RADIUS, CLAIM_SEED)
    mats = list(mats) + [Material((0.55, 0.56, 0.58), metal=0.6, shininess=60, spec=0.7),
                         Material((0.88, 0.45, 0.12), shininess=30, spec=0.3),
                         Material((0.05, 0.1, 0.05), emission=(0.3, 1.0, 0.45))]

    def scene(p):
        d, m = rock(p)
        hollow = sd_sphere(p, (-0.12, -0.1, 0.55), 0.3)
        d, m = subtract((d, m), hollow)
        return union((d, m), (sd_capsule(p, (0.2, 0.15, 0.4), (0.42, 0.5, 0.62), 0.04), 2),
                     (sd_box(p, (0.46, 0.56, 0.64), (0.16, 0.1, 0.02), 0.01), 3),
                     (sd_sphere(p, (0.3, 0.32, 0.55), 0.05), 4))
    return scene, mats


def _claim(k):
    return render(*turned(*claim_model(), k, 16), CLAIM_SIZE, CLAIM_SIZE / bu.PX, crisp=70)


def claim_break(frames, f, n=8):
    img = bu.rock_break(frames, f, n)
    if f < 5:
        d = ImageDraw.Draw(img)
        k = 1 - (f + 1) / n
        d.ellipse([25, 25, 31, 31], fill=(255, 190, 90, 255) if k > 0.5 else (200, 140, 70, 255))
    return img


# =========================================================================== jobs, builders

JOBS = {"tug": tug_frame_job, "hornet": _hornet, "cells": _hornet_cells, "shard": _shard, "piece": _shard_piece,
        "saw": _saw, "blade": _saw_blade, "claim": _claim}


def _job(job):
    if job[0] == "tug":
        return tug_frame_job(job[1:])
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


GOLD = {}


def note_gold(name, frames):
    shares = [e60.gold_share(f) for f in frames]
    GOLD[name] = (min(shares), max(shares))
    print(f"gold {name}: {100 * min(shares):.0f}-{100 * max(shares):.0f} %")


def screen_point(local, size, extent):
    """A facing-down model point (x right, y forward) in a frame's px."""
    return buc.screen_point(local, 0, 16, size, extent)


def build_hornet():
    jobs = [("hornet", h, TAU / 3 * f / 4) for h in (0.0, 0.33, 0.67, 1.0) for f in range(4)] + [("cells",)]
    res = run(jobs)
    body = artkit.quantize_set([rim(f) for f in res[:16]], 48)
    note_gold("hornet", body[:4])
    save_set("hornet", body)
    save_set("hornet-hatch-glow", [hatch_glow(res[16], k) for k in (0.25, 0.5, 0.75, 1.0)])
    save_set("hornet-launch", [launch_flash(i) for i in range(4)])
    cells = [screen_point((s * (HOR_CELLS[0][0] + HOR_CELLS[1][0]) / 2, (HOR_CELLS[0][1] + HOR_CELLS[1][1]) / 2),
                          HOR_SIZE, HOR_EXT) for s in (-1, 1)]
    write_pivots("hornet", {
        "size": [HOR_SIZE, HOR_SIZE], "headings": 1, "frames": "hatch * 4 + fan (0..3 each)",
        "cells": cells,
        "fans": [screen_point((s * fx, fy), HOR_SIZE, HOR_EXT) for fx, fy in HOR_FANS for s in (-1, 1)],
        "note": "facing down (fixed): cells the left and right wing roots' missile cells (a salvo's launch points, "
                "alternating; the launch flash's centres), fans the four lift fans; [x, y] px from the frame's top "
                "left. Frame index = hatch step * 4 + fan step: the hatch step from Enemy.gunTell() (0, 1/3, 2/3, "
                "1), the fan step a loop at 12 fps"})


def build_shard():
    res = run([("shard", k) for k in range(16)] + [("piece", i) for i in range(8)])
    body = artkit.quantize_set([rim(f) for f in res[:16]], 32)
    note_gold("shard-drone", body)
    save_set("shard-drone", body)
    save_set("shard-drone-shard", artkit.quantize_set(res[16:], 16))
    save_set("shard-drone-link", artkit.quantize_set([link_strip(i) for i in range(8)], 16))
    save_set("shard-drone-link-tell", [link_tell(i) for i in range(4)])
    save_set("shard-drone-link-snap", [link_snap(i) for i in range(6)])


def build_saw():
    res = run([("saw", k) for k in range(8)] + [("blade", i) for i in range(8)])
    body = artkit.quantize_set([rim(f) for f in res[:8]], 32)
    note_gold("buzzsaw-drone", body)
    save_set("buzzsaw-drone", body)
    tell = artkit.quantize_set([redden(f) for f in body], 32)
    save_set("buzzsaw-drone-tell", tell)
    save_set("buzzsaw-drone-blur", artkit.quantize_set([saw_blur(body, k) for k in (0, 2, 4, 6)], 32))
    save_set("buzzsaw-drone-ricochet", [ricochet(i) for i in range(6)])
    save_set("buzzsaw-drone-blade", artkit.quantize_set(res[8:], 16))


def build_claim():
    res = run([("claim", k) for k in range(16)])
    frames = artkit.quantize_set(res, 28)
    save_set("debris-claim-rock", frames)
    save_set("debris-claim-rock-break", artkit.quantize_set([claim_break(frames, f) for f in range(8)], 24))


BUILDERS = {"tug": build_tug, "hornet": build_hornet, "shard": build_shard, "saw": build_saw, "claim": build_claim}


# =========================================================================== review

def plate(w=480, h=540):
    """The review ground: Level 31's production space when backdrop_l31.py has written it, else Level 30's."""
    tile = BACKDROP / "open-space.png"
    src = Image.open(tile).convert("RGBA") if tile.exists() else buc.plate(480, 540)
    out = Image.new("RGBA", (w, h))
    for y in range(0, h, src.height):
        for x in range(0, w, src.width):
            out.alpha_composite(src.crop((0, 0, min(src.width, w - x), min(src.height, h - y))), (x, y))
    return out


paste, add, caption, ship_sprite = buc.paste, buc.add, buc.caption, buc.ship_sprite


def gif_frames(draw, seconds, base):
    return [draw(base.copy(), i / FPS) for i in range(int(seconds * FPS))]


def save_review(kind, subject, sheet, gif=None):
    png, gif_path = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    msg = f"review: {png.relative_to(ROOT)}"
    if gif:
        size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
        msg += f", {gif_path.name} ({size / 1e6:.1f} MB)"
    print(msg)


def over_plate(frames, glows=(), gap=12):
    w = sum(f.width for f in frames) + gap * len(frames) + 8
    h = max(f.height for f in frames) + 16
    img = plate(w, h)
    x, centres = 8, []
    for f in frames:
        img = paste(img, f, x + f.width / 2, h / 2)
        centres.append(x + f.width / 2)
        x += f.width + gap
    for g, i, dx, dy in glows:
        img = add(img, g, centres[i] + dx, h / 2 + dy)
    return img


def share(name):
    lo, hi = GOLD.get(name, (None, None))
    if lo is None:
        vals = [e60.gold_share(f) for f in load(name)[:16]]
        lo, hi = min(vals), max(vals)
    return f"{100 * lo:.0f}" if round(lo * 100) == round(hi * 100) else f"{100 * lo:.0f}-{100 * hi:.0f}"


def boom(f, rung, i, x, y):
    return buc.boom(f, buc.burst_set(rung), i, x, y)


def diamond(h):
    """The homing diamond (act5_gear.py) at a screen heading h (radians, 0 = right, y down)."""
    fr = artkit.load_frames("homing-diamond")
    return fr[l23kit.heading_index(np.cos(h), np.sin(h))]


def pivots(root):
    return json.loads(pivot_path(root).read_text(encoding="utf-8"))


TUG_ST = (240, 380)


def tug_pos(t):
    th = TAU * t / 14.0
    return TUG_ST[0] + 10 * np.sin(th), TUG_ST[1] + 2.5 * np.sin(2 * th)


def draw_tug(f, x, y, states, t, flare=False):
    """The tug as the game would compose it: the three sections, the bells' glow, the vents; x, y its centre."""
    pv = pivots("mining-tug")
    flick = int(t * 9) % 3 == 0
    for s in ("engines", "cargo", "bow"):
        idx = {"intact": 0, "damaged": 2 if flick else 1, "lost": 3}[states[s]]
        ox, oy = pv["sections"][s]["offset"]
        f = paste(f, load(f"mining-tug-{s}")[idx], x + ox, y + oy)
    if states["engines"] != "lost":
        glow = load("mining-tug-flare" if flare else "mining-tug-glow")
        for b in pv["bells"]:
            g = glow[int(t * 12) % len(glow)]
            if b["scale"] != 1.0:
                g = g.resize((max(1, round(g.width * b["scale"])), max(1, round(g.height * b["scale"]))), Image.BOX)
            bx, by = b["at"]
            f = add(f, g, x + bx, y + by + g.height / 2 - (12 if flare else 10) * b["scale"])
    vents = load("mining-tug-vent")
    for s in ("bow", "cargo", "engines"):
        if states[s] in ("damaged", "lost"):
            for j, (px, py) in enumerate(pv["sections"][s]["smoke"]):
                v = vents[(int(t * 10) + 3 * j) % len(vents)]
                f = paste(f, v, x + px, y + py + v.height / 2 - 4)
    return f


def box_outline(img, x, y, pv, col=(120, 255, 170)):
    d = ImageDraw.Draw(img)
    for s, sec in pv["sections"].items():
        cx, cy = sec["box"]["centre"]
        w, h = sec["hit"]
        d.rectangle([x + cx - w / 2, y + cy - h / 2, x + cx + w / 2 - 1, y + cy + h / 2 - 1], outline=col + (255,))
    return img


def review_tug():
    pv = pivots("mining-tug")
    secs = {s: load(f"mining-tug-{s}") for s in SECTION_FRAMES}
    combos = [("INTACT", {"bow": "intact", "cargo": "intact", "engines": "intact"}),
              ("CARGO BELOW 50 %", {"bow": "intact", "cargo": "damaged", "engines": "intact"}),
              ("BOW LOST", {"bow": "lost", "cargo": "damaged", "engines": "intact"}),
              ("ENGINES LOST", {"bow": "intact", "cargo": "intact", "engines": "lost"}),
              ("ALL THREE LOST", {"bow": "lost", "cargo": "lost", "engines": "lost"})]
    cells = []
    for lab, st in combos:
        c = plate(232, 340)
        c = draw_tug(c, 116, 166, st, 0.3)
        cells.append(c)
    boxed = draw_tug(plate(232, 340), 116, 166, combos[0][1], 0.3)
    boxed = box_outline(boxed, 116, 166, pv)
    hits = "; ".join(f"{s.upper()} {sec['hit'][0]}X{sec['hit'][1]} AT ({sec['box']['centre'][0]:g}, "
                     f"{sec['box']['centre'][1]:g})" for s, sec in pv["sections"].items())
    rows = [("THE SECTIONS AT 1X (EACH: INTACT, BELOW 50 %, ITS LIGHTS OUT (THE FLICKER), LOST)",
             [over_plate(secs[s], gap=8) for s in ("bow", "cargo", "engines")], 1, False),
            ("THE TUG COMPOSED (1X, 200X300 ON LEVEL 31'S SPACE): " + ", ".join(lab for lab, _ in combos), cells, 1,
             False),
            ("THE MEASURED HIT BOXES (90 % OF THE OPAQUE BOXES, Y DOWN FROM THE CENTRE): " + hits, [boxed], 1, False),
            ("THE BELL GLOW, THE CLIMB-OUT FLARE (ADDITIVE, 2X), THE VENT LOOP, THE LOSS'S FIRE FLARE",
             [over_plate(load("mining-tug-glow") + load("mining-tug-flare")[:2]),
              over_plate(load("mining-tug-vent")), over_plate(load("mining-tug-burn")[::2])], 1, False),
            ("SCALE: THE LANCER, A HORNET, A DART AND A GUNSHIP BESIDE THE BOW (1X)",
             [over_plate([ship_sprite(), load("hornet")[0], load("dart-drone")[0], load("gilded-gunship")[0],
                          secs["bow"][0]])], 1, False)]
    sheet = artkit.review_sheet("THE MINING TUG BELLWETHER (FINAL R64): ROUND 60 A.19 B AT 200X300 (QD-3 A)", rows,
                                width=1300, batch=BATCH)
    hits_t = [(1.6, "bow"), (2.4, "cargo"), (3.0, "cargo"), (3.6, "cargo"), (4.4, "bow"), (5.0, "bow"),
              (5.6, "bow")]
    lose = {"cargo": 3.0, "bow": 5.6}
    hornets = [(150, 120), (330, 140)]
    homing = []
    for n, (th, s) in enumerate(hits_t):
        hx_, hy = hornets[n % 2]
        tx, ty = tug_pos(th)
        oy = pv["sections"][s]["box"]["centre"][1]
        fl = np.hypot(tx - hx_, ty + oy - hy) / 110.0
        homing.append((th - fl, th, hx_, hy, s))

    def states(t):
        st = {s: "intact" for s in SECTION_FRAMES}
        if t >= lose["cargo"]:
            st["cargo"] = "damaged"
        if t >= 4.4:
            st["bow"] = "damaged"
        if t >= lose["bow"]:
            st["bow"] = "lost"
        return st

    def draw(f, t):
        x, y = tug_pos(t)
        st = states(t)
        f = draw_tug(f, x, y, st, t)
        if lose["bow"] <= t < lose["bow"] + 0.8:
            f = add(f, load("mining-tug-burn")[int((t - lose["bow"]) * 10)], x, y + pv["sections"]["bow"]["offset"][1])
        for t0, t1, hx_, hy, s in homing:
            tx, ty = tug_pos(t1)
            ty += pv["sections"][s]["box"]["centre"][1]
            if t0 <= t < t1:
                u = (t - t0) / (t1 - t0)
                mx = hx_ + (tx - hx_) * u + 30 * np.sin(u * np.pi) * (1 if hx_ < 240 else -1)
                my = hy + (ty - hy) * u
                f = paste(f, diamond(np.arctan2(ty - my, tx - mx)), mx, my)
            elif t1 <= t < t1 + 0.25:
                f = add(f, artkit.load_frames("homing-diamond-pop")[min(4, int((t - t1) * 20))], tx, ty)
        hornet = load("hornet")
        for hx_, hy in hornets:
            f = paste(f, hornet[int(t * 12) % 4], hx_, hy)
        f = paste(f, ship_sprite(), 240 + 150 * np.sin(t * 0.8), 200)
        cap = ("ON STATION, SWAYING; ONLY HOOKED MISSILES HURT IT" if t < 3.0 else
               "CARGO BELOW 50 %: VENTING, ITS LIGHTS FLICKER" if t < lose["bow"] else
               "BOW LOST: FIRE, THEN DARK; THE TUG FLIES ON")
        return caption(f, cap)
    save_review("tug", "mining-tug", sheet, gif_frames(draw, 8.0, plate()))


def review_hornet():
    body = load("hornet")
    glow = load("hornet-hatch-glow")
    pv = pivots("hornet")
    shield = artkit.load_frames("enemy-shield-medium")
    lit = over_plate([body[12], body[13]], glows=[(glow[3], 0, 0, 0), (glow[3], 1, 0, 0)])
    sh = plate(120, 110)
    sh = paste(sh, body[0], 60, 55)
    sh = add(sh, shield[0], 60, 55, 0.8)
    rows = [(f"HORNET, FACING DOWN: HATCH 0..3 (FRAMES 0, 4, 8, 12) | FAN 0..3 (FRAMES 12..15) (GOLD {share('hornet')} %)",
             [body[0], body[4], body[8], body[12], body[13], body[14], body[15]], 2, False),
            ("THE HATCH BLOOM (ADDITIVE, 4 STEPS) | THE LAUNCH FLASH AT A CELL (4 STEPS)",
             [over_plate(glow), over_plate(load("hornet-launch"))], 1, False),
            ("ON LEVEL 31'S SPACE (1X): THE LANCER, A HORNET, ITS TELL (HATCHES LIT + BLOOM), UNDER ITS HEX SHIELD "
             "(PART A'S), A TALON, A HOMING DIAMOND",
             [over_plate([ship_sprite(), body[0], load("talon")[0], diamond(np.pi / 2)]), lit, sh], 1, False)]
    sheet = artkit.review_sheet("HORNET (FINAL R64): ROUND 60 A.2 B, THE DELTA HOVER-BOAT, IN GUNMETAL (Q.11 B)", rows,
                                batch=BATCH)
    cells = pv["cells"]
    salvo = 2.0

    def draw(f, t):
        x = 240 + 20 * np.sin(t * 0.6)
        y = min(130.0, -40 + 120 * t)
        hatch = 0
        if salvo - 0.5 <= t < salvo + 0.8:
            hatch = 3 if t >= salvo - 0.17 else (2 if t >= salvo - 0.33 else 1)
        f = paste(f, body[hatch * 4 + int(t * 12) % 4], x, y)
        if hatch:
            f = add(f, glow[hatch], x, y)
        f = add(f, shield[int(t * 3) % 4], x, y, 0.7)
        for j in range(4):
            t0 = salvo + 0.2 * j
            c = cells[j % 2]
            sx, sy = x - HOR_SIZE / 2 + c[0], y - HOR_SIZE / 2 + c[1]
            if t0 <= t < t0 + 0.2:
                f = add(f, load("hornet-launch")[int((t - t0) * 20)], sx, sy)
            if t >= t0:
                u = t - t0
                tx, ty = 240 + 120 * np.sin(t * 0.8), 470
                h = np.pi / 2 + (0.6 if j % 2 else -0.6) * np.exp(-u * 1.4)
                mx = sx + np.cos(h) * 0 + (tx - sx) * min(1, u / 3.0) * 0.6 + (30 if j % 2 else -30) * np.sin(min(u, 1.5))
                my = sy + 110 * u
                if my < 540:
                    f = paste(f, diamond(np.arctan2(110, (tx - sx) * 0.2 + (30 if j % 2 else -30) * np.cos(min(u, 1.5)))), mx, my)
        f = paste(f, ship_sprite(), 240 + 120 * np.sin(t * 0.8), 470)
        return caption(f, "HATCHES GLOW GOLD 0.5 S, THEN 4 HOMING DIAMONDS, THE CELLS ALTERNATING" if t >= 1.4 else
                       "HOVERS IN, FANS TURNING, UNDER ITS HEX SHIELD")
    save_review("hornet", "hornet", sheet, gif_frames(draw, 5.0, plate()))


def tile_link(img, a, b, frame):
    """A lit link drawn as the game would: the 32x16 strip tiled from a to b, rotated along the link."""
    (x0, y0), (x1, y1) = a, b
    length = int(round(np.hypot(x1 - x0, y1 - y0)))
    if length < 2:
        return img
    strip = Image.new("RGBA", (length, 16), (0, 0, 0, 0))
    for x in range(0, length, 32):
        strip.alpha_composite(frame.crop((0, 0, min(32, length - x), 16)), (x, 0))
    ang = np.degrees(np.arctan2(y1 - y0, x1 - x0))
    rot = strip.rotate(-ang, resample=Image.NEAREST, expand=True)
    return paste(img, rot, (x0 + x1) / 2, (y0 + y1) / 2)


def review_shard():
    body = load("shard-drone")
    link = load("shard-drone-link")
    tell = load("shard-drone-link-tell")
    snap = load("shard-drone-link-snap")
    lnk = plate(220, 60)
    lnk = tile_link(lnk, (30, 30), (190, 30), link[0])
    lnk = paste(lnk, body[0], 30, 30)
    lnk = paste(lnk, body[5], 190, 30)
    rows = [(f"SHARD DRONE, THE SPIN (16 FRAMES, 7.5 DEGREES: THE 120 DEGREE LOOP; GOLD {share('shard-drone')} %)",
             body, 2, False),
            ("THE LINK STRIP (32X16, TILES ALONG THE LINK; 8 CRACKLE STEPS) | THE TELL'S FLICKER (4) | THE SNAP "
             "(ADDITIVE, 6) | A GOLD SHARD (8)",
             [over_plate(link[:4], gap=4), over_plate(tell, gap=4), over_plate(snap, gap=4),
              over_plate(load("shard-drone-shard"), gap=4)], 2, False),
            ("1X: TWO DRONES 160 PX APART, THE LINK TILED BETWEEN; THE LANCER, A TALON", [lnk, over_plate(
                [ship_sprite(), load("talon")[0]])], 1, False)]
    sheet = artkit.review_sheet("SHARD DRONE (FINAL R64): ROUND 60 A.9 B, TRI-PRISM AND LIGHTNING LINKS, IN GUNMETAL",
                                rows, batch=BATCH)
    kill = (1, 3.6)

    def pos(t):
        base_y = -30 + 45 * t
        sway = 30 * np.sin(TAU * t / 5.0)
        return [(60 + 120 * k + sway, base_y) for k in range(4)]

    def draw(f, t):
        p = pos(t)
        lit_t = 1.2 + 0.8
        for k in range(3):
            if t >= kill[1] and k in (kill[0] - 1, kill[0]):
                continue
            if t > 1.2:
                fr = tell[int(t * 20) % 4] if t < lit_t else link[int(t * 20) % 8]
                f = tile_link(f, p[k], p[k + 1], fr)
        if kill[1] <= t < kill[1] + 0.3:
            for k in (kill[0] - 1, kill[0]):
                mx, my = (p[k][0] + p[k + 1][0]) / 2, (p[k][1] + p[k + 1][1]) / 2
                f = add(f, snap[int((t - kill[1]) * 20)], mx, my)
        for k, (x, y) in enumerate(p):
            if k == kill[0] and t >= kill[1]:
                if t < kill[1] + 0.6:
                    f = boom(f, "small", int((t - kill[1]) * 20), x, y)
                continue
            f = paste(f, body[int(t * 48) % 16], x, y)
        f = paste(f, ship_sprite(), 240 if t < 3.0 else 182, 470)
        cap = ("THE GROUP ENTERS" if t < 1.2 else "THE LINKS FLICKER 0.8 S (THE TELL)" if t < 2.0 else
               "LIT: CRIMSON LIGHTNING, GOLD CORE" if t < kill[1] else "A KILLED DRONE SNAPS ITS LINKS")
        return caption(f, cap)
    save_review("shard", "shard-drone", sheet, gif_frames(draw, 7.0, plate()))


def review_saw():
    body = load("buzzsaw-drone")
    tell = load("buzzsaw-drone-tell")
    blur = load("buzzsaw-drone-blur")
    ric = load("buzzsaw-drone-ricochet")
    rows = [(f"BUZZSAW DRONE, THE SPIN (8 FRAMES, 7.5 DEGREES: THE 60 DEGREE LOOP; GOLD {share('buzzsaw-drone')} %)",
             body, 3, False),
            ("THE SPIN-UP TELL (RED FLICKER, 8) | THE SPUN-UP BLUR (4)", [over_plate(tell, gap=4),
                                                                         over_plate(blur, gap=4)], 2, False),
            ("THE RICOCHET SPARKS (ADDITIVE, 6; FANNING UP FROM A SURFACE BELOW) | A FLUNG BLADE (8)",
             [over_plate(ric, gap=4), over_plate(load("buzzsaw-drone-blade"), gap=4)], 2, False),
            ("1X ON LEVEL 31'S SPACE: THE LANCER, A BUZZSAW, ITS TELL, ITS BLUR, A DART",
             [over_plate([ship_sprite(), body[0], tell[0], blur[0], load("dart-drone")[0]])], 1, False)]
    sheet = artkit.review_sheet("BUZZSAW DRONE (FINAL R64): ROUND 60 A.6 B, GOLD BLADES, IN GUNMETAL (Q.11 B)", rows,
                                batch=BATCH)
    rock = None
    try:
        rock = load("debris-belt-large-a")
    except FileNotFoundError:
        pass
    path = []
    pos = np.array((120.0, -20.0))
    ship = (300.0, 470.0)
    phase_t = []
    v = np.array((0.0, 1.0)) * 140
    t, dash = 0.0, None
    bounces = 0
    rock_c, rock_r = np.array((380.0, 250.0)), 40.0
    while t < 7.0:
        if t < 2.5:
            want = np.array(ship) - pos
            v = want / np.linalg.norm(want) * 140
            ph = "chase"
        elif t < 3.1:
            if dash is None:
                dash = (np.array(ship) - pos) / np.linalg.norm(np.array(ship) - pos)
            v = dash * 40
            ph = "tell"
        else:
            if ph != "dash":
                v = dash * 360
            ph = "dash"
            nxt = pos + v / FPS
            if (nxt[0] < 18 or nxt[0] > 462) and bounces < 3:
                v[0] = -v[0]
                bounces += 1
                phase_t.append((t, pos.copy(), np.array((1.0 if nxt[0] < 18 else -1.0, 0.0))))
            elif (nxt[1] > 522 or nxt[1] < 18) and bounces < 3:
                v[1] = -v[1]
                bounces += 1
                phase_t.append((t, pos.copy(), np.array((0.0, -1.0 if nxt[1] > 522 else 1.0))))
            elif np.linalg.norm(nxt - rock_c) < rock_r + 14 and bounces < 3:
                nrm = (pos - rock_c) / np.linalg.norm(pos - rock_c)
                v = v - 2 * (v @ nrm) * nrm
                bounces += 1
                phase_t.append((t, pos.copy(), nrm))
        pos = pos + v / FPS
        path.append((pos.copy(), ph))
        t += 1 / FPS

    def draw(f, t):
        if rock:
            f = paste(f, rock[int(t * 2) % len(rock)], *rock_c)
        i = min(int(t * FPS), len(path) - 1)
        p, ph = path[i]
        fr = body[int(t * 16) % 8] if ph == "chase" else (
            tell[int(t * 40) % 8] if ph == "tell" and int(t * 10) % 2 else body[int(t * 40) % 8]) if ph != "dash" else \
            blur[int(t * 20) % 4]
        f = paste(f, fr, *p)
        for t0, at, nrm in phase_t:
            if t0 <= t < t0 + 0.3:
                spark = ric[int((t - t0) * 20)]
                ang = np.degrees(np.arctan2(nrm[0], -nrm[1]))
                f = add(f, spark.rotate(-ang, resample=Image.NEAREST), *at)
        f = paste(f, ship_sprite(), *ship)
        cap = ("CHASE (2 TURNS/S)" if ph == "chase" else "SPIN-UP TELL 0.6 S: THE GOLD FLICKERS RED" if ph == "tell"
               else "DASH: A STRAIGHT LINE, RICOCHETS OFF THE EDGES AND BIG ROCKS")
        return caption(f, cap)
    save_review("saw", "buzzsaw-drone", sheet, gif_frames(draw, 6.0, plate()))


def review_claim():
    rock = load("debris-claim-rock")
    brk = load("debris-claim-rock-break")
    small = []
    for n in ("debris-belt-a", "debris-belt-b", "debris-belt-c"):
        try:
            small.append(load(n)[0])
        except FileNotFoundError:
            pass
    rows = [("DACOSTA'S CLAIM: A SMALL HOLLOW ROCK, THE CLAIM MARKER BOLTED ON (16 SPIN FRAMES)", rock, 2, False),
            ("SHOT OPEN (12 HP): CHIPS AND THE CRATE'S AMBER LIGHT (8)", brk, 2, False),
            ("1X BESIDE PART B'S SMALL BELT ROCKS AND THE LANCER", [over_plate([rock[0]] + small + [ship_sprite()])],
             1, False)]
    sheet = artkit.review_sheet("DACOSTA'S CLAIM ROCK (FINAL R64): LEVEL 31'S SECRET", rows, batch=BATCH)
    save_review("claim", "claim-rock", sheet)


REVIEWS = {"tug": review_tug, "hornet": review_hornet, "shard": review_shard, "saw": review_saw,
           "claim": review_claim}


# =========================================================================== check

SETS = {"tug": [("mining-tug-bow", 4), ("mining-tug-cargo", 4), ("mining-tug-engines", 4), ("mining-tug-glow", 4),
                ("mining-tug-flare", 4), ("mining-tug-vent", 8), ("mining-tug-burn", 8)],
        "hornet": [("hornet", 16), ("hornet-hatch-glow", 4), ("hornet-launch", 4)],
        "shard": [("shard-drone", 16), ("shard-drone-link", 8), ("shard-drone-link-tell", 4),
                  ("shard-drone-link-snap", 6), ("shard-drone-shard", 8)],
        "saw": [("buzzsaw-drone", 8), ("buzzsaw-drone-tell", 8), ("buzzsaw-drone-blur", 4),
                ("buzzsaw-drone-ricochet", 6), ("buzzsaw-drone-blade", 8)],
        "claim": [("debris-claim-rock", 16), ("debris-claim-rock-break", 8)]}


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n in sets:
            try:
                frames = load(name)
                folder = where(name)
                paths = [p for d in (l23kit.ASSETS_SPRITES, l23kit.HELD) for p in d.glob(f"{name}*.png")
                         if p.stem == name or (p.stem.rsplit("_", 1)[0] == name and p.stem.rsplit("_", 1)[1].isdigit())]
                ok = len(frames) == n and all(Image.open(p).text.get(artkit.SOURCE_KEY) == SOURCE for p in paths)
            except FileNotFoundError:
                frames, folder, ok = [], "-", False
            size = frames[0].size if frames else None
            print(f"{'ok ' if ok else 'BAD'} {name}: {len(frames)}/{n} {size} in {folder}")
            bad += not ok
    for root in ("mining-tug", "hornet"):
        ok = pivot_path(root).exists()
        print(f"{'ok ' if ok else 'BAD'} pivots/{root}.json in {pivot_path(root).parent.relative_to(ROOT)}")
        bad += not ok
    ok = TUG_PROPOSAL.exists()
    print(f"{'ok ' if ok else 'BAD'} {TUG_PROPOSAL.relative_to(ROOT)}")
    sys.exit(1 if bad + (not ok) else 0)


def print_boxes():
    pv = pivots("mining-tug")
    for s, sec in pv["sections"].items():
        print(f"{s}: opaque {sec['box']['size']} at {sec['box']['centre']}, hit {sec['hit']}")


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    if "--boxes" in args:
        print_boxes()
        sys.exit(0)
    subjects = [a for a in args if not a.startswith("--")] or list(BUILDERS)
    unknown = [s for s in subjects if s not in BUILDERS]
    if unknown:
        sys.exit(f"unknown: {unknown}; subjects: {', '.join(BUILDERS)}")
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    for s in subjects:
        REVIEWS[s]()
