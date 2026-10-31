#!/usr/bin/env python3
"""Production art: Level 37's units and objects (M10 part C batch, concept round 75, step ART-C1): the Mirror
Interceptor (design/enemies/air/mirror-interceptor, round 72 **A.2 a** "crescent"), the Graft Turret
(design/enemies/ground/graft-turret, round 72 **A.4 b** "tissue first"), the evacuation shuttle in Aurelia's civil
livery (design/allies, Evacuation shuttle; Level 37's README *The shuttles*: round 75 QC-3 **a**, white with a gold
band and the city's crest in place of the CDF evac stripe, pending the user's review; b, a gold hull with white trim,
is a proposal sheet only) and the CDF locker (Level 37's secret).

The models are round 72's (tools/concept/enemies_r72.py ``mi_hull``/``mi_mirror`` a and ``graft_base``/``graft_head``/
``graft_root`` b, imported unchanged) at the concepts' scale (the Mirror 36.5 px per unit: its plate 76 px across; the
Graft 22.6 px per unit), in the regular livery (Q11 b's gunmetal, gold trim, red lights, the 1 px gold rim on the
sunward edges and red on the far ones: belt_units.rim). The shuttle is Level 10's production model (tools/art/
shuttle.py ``lifting_body``, unchanged) with the livery's decals and materials; the locker is new.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its own
render under the fixed key light. Glows are smooth float fields written unquantised (no ordered dither, far more than
32 steps); additive sets are premultiplied on black (GL_SRC_ALPHA, GL_ONE).

The game's part (step C4b, MirrorLooks and RegrowthLooks, today drawing A3a's stand-ins): the mirror's heat, its
cracks' opacity, the specular sweep, the regrowth's shimmer and the shards stay drawn by code, now over these frames
(the plate frame of the unit's heading instead of the generated crescent turned by the game); the root's pulse and
its shrinking ring stay RegrowthLooks', drawn with the root's glow frame at the pulse's strength.

Outputs (build/art-held/sprites/ until the data claims the root - C2 writes the units' data.yaml, Level 37's ground
target and the allies' livery; rerun the subject then and the files move to assets/sprites/):
  mirror-interceptor_0..15        84x84, 16 headings: the hull without its mirror (gunmetal, gold trim and chevron, red
                                  wing-tip lights, the pods dark) with the mirror's bare grey nose frame: the broken
                                  look on its own (48 colours)
  mirror-interceptor-plate_0..15  84x84, normal, drawn over the hull while the mirror stands: the polished silver-gold
                                  crescent 76 px across with its gold tip caps (40 colours); a regrowing plate is drawn
                                  at the regrown share's opacity
  mirror-interceptor-plate-glow_0..15  84x84 additive, white: the plate's own coverage with a soft 1 px halo, the mask
                                  the heat's colour is drawn through (dull red, orange, white at MirrorLooks.heatColour)
  mirror-interceptor-cracks_0..15 84x84 normal, white lines (alpha the lines' coverage): the cracks across the plate,
                                  drawn tinted dark from 70 % heat and in the heat's colour through a white-hot plate
  mirror-interceptor-pods_0..15   84x84 additive: the 0.4 s tell, the red glow in the two gun pods under the mirror's
                                  rim at strength 1 (the game ramps it)
  pivots/mirror-interceptor.json  per heading the two pod muzzles (the burst's spawn), [x, y] px from the top left
  graft-turret-base               52x52: the fixed gunmetal octagon mount with its gold ring, the fleshy collar and six
                                  roots across the deck, the seams lit teal (36 colours)
  graft-turret_0..31              52x52, 32 angles: the armoured dome a third overgrown, one barrel with a gold muzzle,
                                  the red sensor (drawn over the base)
  graft-turret-regrown_0..31      52x52: the regrown turret, more tissue than metal, the barrel bone-white and veined
  graft-turret-wreck              52x52: the burst shell, its two halves thrown apart and scorched, the barrel fallen
                                  off beside it; drawn over the base while the root is out
  graft-turret-root               52x52: the root, a dark knot of tissue (normal, drawn over the wreck's centre)
  graft-turret-root-glow          52x52 additive: the root's teal at strength 1 (RegrowthLooks pulses it, 1 -> 4 Hz)
  graft-turret-knit_0..7          52x52: the 0.8 s knit (8 frames, 10 steps each): the shell's halves drawn back in
                                  under spreading tissue, swelling into the regrown veined dome (no barrel: the regrown
                                  frame of the turret's heading follows, faded in over the knit's last two frames)
  graft-turret-stump              52x52: the dead mount, scorched, the collar and roots withered grey-brown, a charred
                                  crater where the root was
  graft-turret-crumble_0..7       48x48 additive: the root's death, teal tissue fibres torn out and falling, a soft
                                  teal flash (2 steps a frame), over the stump
  pivots/graft-turret.json        per angle the barrel's muzzle (the pair's spawn and the red glint's centre)
  evacuation-shuttle-aurelia_0..4, -aurelia-damaged_0..4, -aurelia-lift_0..3, -aurelia-wreck_0..7
                                  64x40: Level 10's shuttle sets in Aurelia's livery, frame for frame (one 40-colour
                                  palette); the flames, flare, pip and pivots stay Level 10's (the same model)
  cdf-locker_0..2                 32x28: the 2166 CDF supply locker (the secret, a 20 HP ground object): 0 sealed (CDF
                                  grey, a blue band, orange hazard ends, white stencil marks, a dim amber lamp), 1 hit
                                  (scorched, dented, the lamp dark), 2 broken open (the lid blown off, the empty foam
                                  cradle: the crate is the game's)
Review (round 75): mirror-interceptor-final-r75-a and graft-turret-final-r75-a (.png/.gif) in the units' concept/,
design/allies/concept/aurelia-shuttle-final-r75-a (.png/.gif) and the proposal aurelia-shuttle-r75-b.png, Level 37's
concept/cdf-locker-final-r75-a.png; on round 72's Level 37 deck stand-in (enemies_r72.deck_bg: ART-C2's tiles are made
beside this step).

Run: python3 tools/art/aurelia_units.py [mirror] [graft] [shuttle] [locker] [--review] [--check]
     (no subject = all, ~2 min; --review rebuilds only the review files; --check lists every set with its folder,
     size and frame count and fails on a missing one, a missing Source chunk, a checker over 5 % or an additive set
     with fewer than 32 steps)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
import shuttle as sh
from artkit import DESIGN, ROOT, TAU

import enemies_r72 as e72  # noqa: E402  (concept: the Mirror's and the Graft's models, imported unchanged)
from render import raster, sdf, sprite  # noqa: E402
from render.enemy_models import A_GOLD, A_GUN, A_HULL, hx  # noqa: E402
from render.sdf import (Material, panel_lines, rotate_y, rotate_z, sd_box, sd_capsule, sd_cylinder_z,  # noqa: E402
                        sd_sphere, union)

SCRIPT = "aurelia_units.py"
BATCH = "M10 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r75"
artkit.REVIEW_ROUND = ROUND
L37 = DESIGN / "campaign" / "act-6-jovian-storm" / "level-37-fall-of-aurelia" / "concept"
CONCEPT = {"mirror": DESIGN / "enemies" / "air" / "mirror-interceptor" / "concept",
           "graft": DESIGN / "enemies" / "ground" / "graft-turret" / "concept",
           "shuttle": DESIGN / "allies" / "concept", "locker": L37}
TEAL = np.array((0, 255, 154), float)
POD_RED = np.array((255, 70, 40), float)

MI, MI_EXT = 84, 2.3                     # the concept's scale: 36.5 px a unit, the plate 76 px across
A_MIRROR, A_TELL, A_FRAME = e72.A_MIRROR, 9, 10
MARK = 11                                # an extra slot: a point the pivots are measured from
GT, GT_EXT = 52, 2.3                     # 22.6 px a unit
A_TISSUE, A_BONE = e72.A_TISSUE, e72.A_BONE
SCORCH = 10
GLOW_STEPS = 32


def save_set(name, frames, single=False):
    l23kit.write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def blur(a, sigma):
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)), mode="constant")
    a = sum(k[i] * p[i:i + a.shape[0], :] for i in range(2 * r + 1))
    p = np.pad(a, ((0, 0), (r, r)), mode="constant")
    return sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))


def additive_rgb(light):
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def glow(light, x, y, r, col, k):
    h, w = light.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    light += (np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k)[..., None] * np.asarray(col, float)


def black(*_):
    return Material((0.0, 0.0, 0.0), spec=0.0)


BONE = Material(hx("d8cfb4"), metal=0.1, shininess=50, spec=0.6)


def pad(mats, n=12, bone=False):
    """The model's materials padded with black slots to ``n`` (the extra slots MARK, SCORCH); ``bone`` puts the regrown
    barrel's bone at A_BONE (enemies_r72's head appends it one slot early: its A_BONE slot was empty)."""
    mats = list(mats)
    if bone:
        mats = mats[:A_BONE] + [Material((0, 0, 0))] * (A_BONE - len(mats)) + [BONE] + mats[A_BONE + 1:]
    return mats + [black() for _ in range(n - len(mats))]


def emit_only(mats, keep, col=None):
    """Every slot black and unlit but ``keep``'s, which only emit (their own emission, or ``col``)."""
    out = []
    for i, m in enumerate(mats):
        if i in keep:
            out.append(Material((0.0, 0.0, 0.0), spec=0.0, emission=tuple(col) if col is not None else m.emission,
                                emission_pattern=m.emission_pattern if col is None else None))
        else:
            out.append(black())
    return out


def emit_render(scene, mats, size, extent):
    """An emission-only render -> float premultiplied light (0..255 a channel): smooth coverage at the edges."""
    w, h = (size, size) if isinstance(size, int) else size
    hi, f = artkit.render_hi(scene, mats, (w, h), extent)
    ds = sprite.downsample(hi, f)
    return np.clip(ds[..., :3], 0, None) * ds[..., 3:4] * 255


def coverage(scene, mats, size, extent):
    w, h = (size, size) if isinstance(size, int) else size
    hi, f = artkit.render_hi(scene, mats, (w, h), extent)
    return sprite.downsample(hi, f)[..., 3]


def centroids(light, n=2):
    """The centres of the ``n`` lit blobs of an emission render (one, or two split across their widest axis)."""
    lum = light.max(axis=-1)
    ys, xs = np.nonzero(lum > 0.25 * lum.max()) if lum.max() > 0 else (np.array([]), np.array([]))
    if len(xs) == 0:
        return [[light.shape[1] / 2, light.shape[0] / 2]] * n
    wgt = lum[ys, xs]
    pts = np.stack([xs + 0.5, ys + 0.5], axis=1)
    c = (pts * wgt[:, None]).sum(0) / wgt.sum()
    if n == 1:
        return [[round(float(c[0]), 1), round(float(c[1]), 1)]]
    far = pts[np.argmax(((pts - c) ** 2).sum(1))]
    axis = (far - c) / max(1e-6, np.hypot(*(far - c)))
    side = (pts - c) @ axis > 0
    out = []
    for sel in (~side, side):
        p, ww = pts[sel], wgt[sel]
        m = (p * ww[:, None]).sum(0) / ww.sum()
        out.append([round(float(m[0]), 1), round(float(m[1]), 1)])
    return sorted(out)


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


# =========================================================================== Mirror Interceptor

def mirror_crack_segs():
    """The crack network in the model plane (x, y; +Y forward): seven jagged radial cracks across the crescent and two
    runs along it between them (seeded, fixed for every heading)."""
    rng = np.random.default_rng(3751)
    cy, r0, hw, arc = e72.MI_CY, e72.MI_R, e72.MI_HW, e72.MI_ARC
    segs = []
    for phi in np.linspace(-arc * 0.82, arc * 0.82, 7) + rng.normal(0, 0.03, 7):
        pts = []
        for u in np.linspace(-1.3, 1.3, 4):
            ph = phi + rng.normal(0, 0.035)
            rr = r0 + u * hw
            pts.append((np.sin(ph) * rr, cy + np.cos(ph) * rr))
        segs += [(a[0], a[1], b[0], b[1]) for a, b in zip(pts, pts[1:])]
    for a0, a1 in ((-0.62, -0.08), (0.1, 0.55)):
        pts = []
        for ph in np.linspace(a0 * arc, a1 * arc, 6):
            rr = r0 + rng.uniform(-0.7, 0.7) * hw
            pts.append((np.sin(ph) * rr, cy + np.cos(ph) * rr))
        segs += [(a[0], a[1], b[0], b[1]) for a, b in zip(pts, pts[1:])]
    return segs


CRACKS = mirror_crack_segs()
PLATE_MAT = Material((0.62, 0.64, 0.7), metal=1.0, shininess=200, spec=2.6)   # polished steel-silver: reads on the cream decks
CRACK_W = 0.022


def mirror_plate(cracks=False):
    scene, mats = e72.mi_mirror("a")
    mats = pad(mats)
    mats[A_MIRROR] = PLATE_MAT
    if not cracks:
        return scene, mats

    def cracked(p):
        d, m = scene(p)
        hit = e72.seg_dist2(p, CRACKS) < CRACK_W            # (the helper returns the distance)
        return d, np.where(hit & (p[:, 2] > 0.1), MARK, m)
    return cracked, mats


def mirror_hull(tell=0.0, mark=False):
    scene, mats = e72.mi_hull("a", tell=tell)
    mats = pad(mats)
    if not mark:
        return scene, mats

    def marked(p):
        d, m = scene(p)
        return d, np.where(m == A_TELL, MARK, m)
    return marked, mats


def _mirror(k, part):
    if part == "hull":
        scene, mats = mirror_hull()
        return bu.render(*bu.turned(scene, mats, k, 16), MI, MI_EXT)
    if part == "frame":                                         # the bare frame's pixels (kept out of the rim)
        scene, mats = mirror_hull()
        return emit_render(*bu.turned(scene, emit_only(mats, {A_FRAME}, (1.0, 1.0, 1.0)), k, 16), MI, MI_EXT)
    if part == "plate":
        scene, mats = mirror_plate()
        return bu.render(*bu.turned(scene, mats, k, 16), MI, MI_EXT)
    if part == "mask":
        scene, mats = mirror_plate()
        return coverage(*bu.turned(scene, mats, k, 16), MI, MI_EXT)
    if part == "cracks":
        scene, mats = mirror_plate(cracks=True)
        return emit_render(*bu.turned(scene, emit_only(mats, {MARK}, (1.0, 1.0, 1.0)), k, 16), MI, MI_EXT)
    scene, mats = mirror_hull(tell=1.0, mark=True)               # the pods
    return emit_render(*bu.turned(scene, emit_only(mats, {MARK}, (1.0, 0.27, 0.16)), k, 16), MI, MI_EXT)


def rim_except(img, keep):
    """belt_units.rim on the hull, the bare frame's thin arc (``keep``, its emission render) left in its own grey."""
    rimmed = np.array(bu.rim(img))
    own = np.array(img)
    sel = keep.max(axis=-1) > 60
    rimmed[sel] = own[sel]
    return Image.fromarray(rimmed, "RGBA")


def plate_glow(cov):
    """The plate's coverage (white, premultiplied) with a soft 1 px halo: the heat's mask."""
    halo = blur(cov, 0.9)
    v = np.clip(cov * 0.85 + halo * 0.45, 0, 1) * 255
    return additive_rgb(np.dstack([v, v, v]))


def crack_layer(light):
    """White lines whose alpha is their coverage (smooth, unquantised)."""
    cov = np.clip(light.max(axis=-1) / 255 * 1.35, 0, 1)
    a = np.round(cov * 255)
    rgb = np.where(a[..., None] > 0, 255, 0)
    return Image.fromarray(np.dstack([rgb, rgb, rgb, a]).astype(np.uint8), "RGBA")


def pod_layer(light, muzzles):
    out = light * 0.9
    for x, y in muzzles:
        glow(out, x, y, 3.2, POD_RED, 1.2)
        glow(out, x, y, 1.3, (255, 210, 180), 0.7)
    return additive_rgb(out)


# =========================================================================== Graft Turret

def scorched(mats, k=0.45):
    """The burst shell: the metal darkened, the tissue's glow nearly out, a soot slot at SCORCH."""
    out = []
    for i, m in enumerate(mats):
        if i == A_TISSUE:
            out.append(e72.tissue_mat(0.12, veins=True))
        elif m.emission == (0.0, 0.0, 0.0) or i in (A_HULL, A_GOLD, A_GUN):
            out.append(Material(tuple(np.asarray(m.albedo) * k), metal=m.metal * 0.5, shininess=m.shininess * 0.5,
                                spec=m.spec * 0.4, pattern=m.pattern))
        else:
            out.append(Material(tuple(np.asarray(m.albedo) * k), spec=0.2))
    out = pad(out)
    out[SCORCH] = Material((0.07, 0.055, 0.05), spec=0.1, shininess=8)
    return out


def graft_head(regrown=False, tg=0.6, barrel=True, mark=False):
    scene, mats = e72.graft_head("b", regrown, tg=tg)
    mats = pad(mats, bone=True)

    def s(p):
        d, m = scene(p)
        if not barrel:
            d = np.maximum(d, p[:, 1] - 0.5)
        if mark:
            m = np.where(np.isin(m, (A_GOLD, A_BONE, A_TISSUE)) & (p[:, 1] > 0.9), MARK, m)
        return d, m
    return s, mats


def wreck_scene(spread=1.0, tissue=0.0, tg=0.2):
    """The burst shell's two halves thrown apart (``spread`` 1) and tipped, their cut faces sooty; the barrel fallen off
    beside them. ``tissue`` 0..1 covers them with knitting tissue (the knit)."""
    head, mats = e72.graft_head("b", False, tg=tg)
    mats = pad(mats, bone=True)
    halves = (((-0.36, 0.06, -0.06), 0.16, 0.3), ((0.38, -0.05, -0.07), -0.14, -0.26))
    sc = 0.86                                         # the shell's halves a little smaller than the standing dome

    def scene(p):
        parts = []
        for side, ((ox, oy, oz), tilt, turn) in zip((-1, 1), halves):
            q = rotate_z(rotate_y(p - np.array((ox * spread, oy * spread, oz * spread)), tilt * spread),
                         turn * spread) / sc
            d, m = head(q)
            d = np.maximum(d, side * -q[:, 0]) * sc
            m = np.where(np.abs(q[:, 0]) < 0.03, SCORCH, m)
            m = np.where(m == A_GUN, A_HULL, m)
            parts.append((d, m))
        if spread > 0.05:
            barrel = sd_capsule(p, (0.36 * spread + 0.2, -0.86, 0.08), (0.78 * spread + 0.25, -0.48, 0.08), 0.07, 0.06)
            cap = sd_sphere(p, (0.82 * spread + 0.25, -0.44, 0.08), 0.085)
            parts += [(barrel, A_GUN), (cap, A_GOLD)]
        d, m = union(*parts, k=0.0)
        if tissue > 0:
            n = (np.sin(p[:, 0] * 11 + 1.3) * np.sin(p[:, 1] * 9 - 0.4) + 0.6 * np.sin((p[:, 0] + p[:, 1]) * 17)) * 0.5
            m = np.where(n < -0.9 + 2.2 * tissue, A_TISSUE, m)
        return d, m
    return scene, mats


def knit_scene(u):
    """Knit frame at ``u`` (0..1): the halves drawn back in under spreading tissue (tendrils from the root to each half),
    morphing into the regrown veined dome (no barrel)."""
    w = smoothstep(0.25, 1.0, u)
    wreck, mats = wreck_scene(spread=1.0 - 0.85 * smoothstep(0.0, 0.8, u), tissue=min(1.0, 0.25 + 1.1 * u))
    dome, _ = graft_head(regrown=True, barrel=False)
    root, _ = e72.graft_root("b")

    def scene(p):
        d0, m0 = wreck(p)
        r = (1.0 + 0.6 * u)
        dr, _ = root(p / r)
        dr = dr * r
        tend = [sd_capsule(p, (0, 0, 0.1), (side * 0.42 * (1 - 0.8 * u), 0.03, 0.2), 0.06 + 0.04 * u, 0.04)
                for side in (-1, 1)]
        dt = np.minimum(dr, np.minimum(*tend))
        da = sdf.smin(d0, dt, 0.08)
        ma = np.where(dt < d0 + 0.02, A_TISSUE, m0)
        d1, m1 = dome(p)
        d = (1 - w) * da + w * d1
        m = np.where(w > 0.55, m1, ma)
        return d, m
    return scene, mats


def graft_stump():
    """The dead mount: the base's collar and roots withered grey-brown, the metal scorched, a charred crater."""
    scene, mats = e72.graft_base("b", tg=0.0)
    mats = scorched(pad(mats), 0.62)
    mats[A_TISSUE] = Material(hx("4a4034"), spec=0.15, shininess=10)

    def s(p):
        d, m = scene(p)
        crater = sd_cylinder_z(p, (0.0, 0.0, 0.1), 0.34, 0.04)
        d, m = union((d, m), (crater, SCORCH), k=0.04)
        ash = np.hypot(p[:, 0], p[:, 1]) < 0.46 + 0.08 * np.sin(np.arctan2(p[:, 1], p[:, 0]) * 5)
        m = np.where(ash & (m == A_HULL), SCORCH, m)
        return d, m
    return s, mats


def _graft(kind, k=0, u=0.0):
    if kind == "base":
        scene, mats = e72.graft_base("b", tg=0.6)
        return bu.render(scene, mats, GT, GT_EXT)
    if kind in ("head", "regrown"):
        scene, mats = graft_head(regrown=kind == "regrown", tg=0.75 if kind == "regrown" else 0.6)
        return bu.render(*bu.turned(scene, mats, k, 32), GT, GT_EXT)
    if kind == "muzzle":
        scene, mats = graft_head(mark=True)
        return emit_render(*bu.turned(scene, emit_only(mats, {MARK}, (1.0, 1.0, 1.0)), k, 32), GT, GT_EXT)
    if kind == "wreck":
        scene, mats = wreck_scene()
        return bu.render(scene, scorched(mats, 0.62), GT, GT_EXT)
    if kind == "root":
        scene, mats = e72.graft_root("b", tg=0.0)
        return bu.render(scene, mats, GT, GT_EXT)
    if kind == "root-glow":
        scene, mats = e72.graft_root("b", only_glow=True, tg=1.0)
        return emit_render(scene, mats, GT, GT_EXT)
    if kind == "knit":
        scene, mats = knit_scene(u)
        mats = scorched(mats, 0.45 + 0.55 * smoothstep(0.3, 1.0, u))
        mats[A_TISSUE] = e72.tissue_mat(0.75 + 0.7 * np.sin(np.pi * u), veins=True)
        mats[A_BONE] = BONE
        return bu.render(scene, mats, GT, GT_EXT)
    scene, mats = graft_stump()
    return bu.render(scene, mats, GT, GT_EXT)


def root_glow_layer(light):
    halo = np.dstack([blur(light[..., c], 1.2) for c in range(3)])
    return additive_rgb(np.clip(light * 0.85 + halo * 0.6, 0, 255))


def crumble(n=8, w=48, seed=3771):
    """The root's death: torn teal tissue fibres flung out and falling, fading; a soft teal flash at the start."""
    rng = np.random.default_rng(seed)
    fibres = [(rng.uniform(0, TAU), rng.uniform(30, 70), rng.uniform(2.5, 5.0), rng.uniform(0, TAU), rng.uniform(4, 9))
              for _ in range(13)]
    motes = [(rng.uniform(0, TAU), rng.uniform(15, 45), rng.uniform(0.6, 1.1)) for _ in range(10)]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n                          # 0.27 s: 2 steps a frame
        light = np.zeros((w, w, 3))
        glow(light, w / 2, w / 2, 7 + 6 * u, TEAL * 0.8, 1.1 * max(0.0, 1 - 1.8 * u))
        for ang, speed, length, spin, sp in fibres:
            dist = speed * 0.3 * (1 - (1 - u) ** 2)
            cx, cy = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 8 * u * u
            a = spin + sp * u
            fade = (1 - u) ** 1.3
            for s in np.linspace(-0.5, 0.5, 7):
                glow(light, cx + np.cos(a) * length * s, cy + np.sin(a) * length * s, 0.75,
                     TEAL * 0.85, 0.9 * fade * (1 - 0.5 * abs(s)))
        for ang, speed, size in motes:
            dist = speed * 0.3 * u
            glow(light, w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 12 * u * u, size,
                 (170, 255, 210), 0.8 * (1 - u) ** 1.6)
        frames.append(additive_rgb(light))
    return frames


# =========================================================================== shuttle livery

GOLD_BAND = Material((0.92, 0.7, 0.24), metal=0.75, shininess=90, spec=0.9)
CREST_FIELD = Material((0.08, 0.11, 0.24), metal=0.2, shininess=60, spec=0.5)
LIVERIES = {"a": "A - WHITE, GOLD BAND, THE CITY'S CREST", "b": "B - GOLD HULL, WHITE TRIM"}
LIVERY = "evacuation-shuttle-aurelia"
CREST = (0.0, -3.5)                       # the crest on the spine, model px (x, y)


def livery_mats(v, lit=True, damaged=False):
    mats = sh.materials(lit, damaged)
    if v == "a":
        mats[sh.STRIPE] = GOLD_BAND
    else:
        mats[sh.HULL] = Material((0.86, 0.66, 0.26), metal=0.7, shininess=80, spec=0.8,
                                 pattern=panel_lines(0.16, 0.05, 0.82))
        mats[sh.STRIPE] = Material((0.9, 0.9, 0.88), metal=0.1, shininess=45, spec=0.45)
    mats[sh.BLUE] = CREST_FIELD
    return mats


def livery_scene(base):
    """Level 10's lifting body with Aurelia's decals: the CDF spine stripe painted over, the evac chevron in the
    livery's band colour (the STRIPE slot), the city's crest on the spine: a ring in the band colour round a dark
    field with a small domed city in the band colour."""
    def scene(p):
        d, m = base(p)
        m = np.where(m == sh.BLUE, sh.HULL, m)
        r = np.hypot(p[:, 0] - CREST[0], p[:, 1] - CREST[1])
        top = (p[:, 2] > 4.6) & (m == sh.HULL)
        m = np.where(top & (r < 3.3), sh.BLUE, m)
        ring = (r > 2.25) & (r < 3.3)
        dome = (r < 1.55) & (p[:, 1] - CREST[1] > -0.75)
        m = np.where(top & (ring | dome), sh.STRIPE, m)
        return d, m
    return scene


def livery_posed(v, bank=0.0, scale=1.0, state="ok", yaw=0.0):
    base = livery_scene(sh.lifting_body(dead_engine=state != "ok"))
    if state != "ok":
        base = sh.scorched(base, sh.SPOTS_A if state == "wreck" else sh.SPOTS_A[:3])
    b, w = np.radians(bank), np.radians(yaw)

    def scene(p):
        q = rotate_y(rotate_z(p, -w), b) / (scale * sh.FIT)
        d, m = base(q)
        return d * scale * sh.FIT, m
    return artkit.perspective(scene, sh.CAMERA), livery_mats(v, lit=state != "wreck", damaged=state != "ok")


def _shuttle(v, kind, arg):
    if kind == "wreck":
        scale, roll, yaw = sh.wreck_pose(arg)
        scene, mats = livery_posed(v, roll, scale, "wreck", yaw)
    elif kind == "lift":
        scene, mats = livery_posed(v, 0.0, arg)
    else:
        scene, mats = livery_posed(v, arg, state="damaged" if kind == "damaged" else "ok")
    hi = sdf.render(scene, mats, (sh.SIZE[0] * 8, sh.SIZE[1] * 8), sh.EXTENT, z_top=sh.Z_TOP, steps=160)
    img = artkit.native(hi, 8, crisp=70)
    return sh.darken(img, arg / (sh.WRECK_STEPS - 1)) if kind == "wreck" else img


def shuttle_jobs(v):
    return ([("shuttle", v, "bank", b) for b in sh.BANKS] + [("shuttle", v, "damaged", b) for b in sh.BANKS]
            + [("shuttle", v, "lift", s) for s in sh.LIFT_SCALES]
            + [("shuttle", v, "wreck", i) for i in range(sh.WRECK_STEPS)])


def shuttle_sets(frames):
    frames = artkit.quantize_set(frames, sh.HULL_COLOURS)
    n = len(sh.BANKS)
    return {LIVERY: frames[:n], f"{LIVERY}-damaged": frames[n:2 * n], f"{LIVERY}-lift": frames[2 * n:2 * n + 4],
            f"{LIVERY}-wreck": frames[2 * n + 4:]}


# =========================================================================== CDF locker

LK = (32, 28)
LK_EXT = 32 / 20.0
L_GREY, L_DARK, L_BLUE, L_ORANGE, L_WHITE, L_LAMP, L_SOOT, L_FOAM, L_INSIDE = range(9)


def locker_model(state):
    """The 2166 CDF supply locker lying on the deck, its long axis across (x): a chamfered CDF-grey box with a raised
    lid, a blue band across its middle, orange hazard ends, white stencil bars, two dark latches on the near side and
    a small amber lamp. ``state`` 0 sealed, 1 hit (soot, a dent, the lamp dark), 2 broken open (the lid gone: the
    walls round an empty foam cradle)."""
    def scene(p):
        x, y = p[:, 0], p[:, 1]
        box = artkit.chamfered_box(p, (0, 0, 0.2), (0.68, 0.46, 0.2), 0.06)
        if state == 2:
            cavity = sd_box(p, (0, 0, 0.36), (0.6, 0.38, 0.2), 0.02)
            box = np.maximum(box, -cavity)
            cradle = sd_box(p, (0, 0, 0.12), (0.58, 0.36, 0.06), 0.03)
            recess = sd_box(p, (0, 0, 0.2), (0.38, 0.24, 0.06), 0.02)
            d, m = union((box, L_GREY), (np.maximum(cradle, -recess), L_FOAM), k=0.0)
            m = np.where((m == L_GREY) & (p[:, 2] < 0.36) & (np.abs(x) < 0.62) & (np.abs(y) < 0.4), L_INSIDE, m)
            for hx_ in (-0.4, 0.4):                       # torn hinge stubs on the far wall
                d, m = union((d, m), (sd_box(p, (hx_, 0.48, 0.38), (0.06, 0.03, 0.03), 0.01), L_DARK))
        else:
            lid = artkit.chamfered_box(p, (0, 0, 0.44), (0.62, 0.4, 0.04), 0.03)
            if state == 1:
                lid = np.maximum(lid, -sd_sphere(p, (-0.18, 0.06, 0.62), 0.2))
            d, m = union((box, L_GREY), (lid, L_GREY), k=0.0)
            latches = [sd_box(p, (lx, -0.43, 0.5), (0.07, 0.04, 0.03), 0.01) for lx in (-0.3, 0.3)]
            lamp = sd_sphere(p, (0.47, 0.26, 0.5), 0.055)
            d, m = union((d, m), (np.minimum(*latches), L_DARK), (lamp, L_LAMP), k=0.0)
        m = np.where((m == L_GREY) & (np.abs(x) < 0.09), L_BLUE, m)
        m = np.where((m == L_GREY) & (np.abs(x) > 0.44) & (np.abs(x) < 0.6) & ((((x + y) * 9) % 2) < 1), L_ORANGE, m)
        stencil = (np.abs(y + 0.17) < 0.04) & (x > 0.14) & (x < 0.4) & (((x * 22) % 2) < 1.4)
        m = np.where((m == L_GREY) & stencil & (p[:, 2] > 0.4), L_WHITE, m)
        if state >= 1:
            for j, (sx, sy, r) in enumerate(((-0.3, 0.1, 0.24), (0.3, -0.18, 0.18), (0.05, 0.28, 0.14))):
                ang = np.arctan2(y - sy, x - sx)
                rag = r * (0.7 + 0.3 * np.sin(3 * ang + j) * np.cos(5 * ang - j))
                m = np.where((np.hypot(x - sx, y - sy) < rag) & np.isin(m, (L_GREY, L_BLUE, L_ORANGE, L_WHITE)),
                             L_SOOT, m)
        return d, m
    lamp = (Material((0.12, 0.08, 0.02), emission=(0.9, 0.55, 0.1)) if state == 0
            else Material((0.15, 0.12, 0.1), spec=0.3))
    mats = [Material((0.6, 0.62, 0.66), metal=0.35, shininess=60, spec=0.55, pattern=panel_lines(0.3, 0.04, 0.85)),
            Material((0.22, 0.23, 0.26), metal=0.5, shininess=30, spec=0.35),
            Material((0.16, 0.38, 0.86), metal=0.3, shininess=50, spec=0.5),
            Material((0.95, 0.52, 0.12), metal=0.2, shininess=40, spec=0.4),
            Material((0.92, 0.92, 0.9), shininess=30, spec=0.3),
            lamp,
            Material((0.16, 0.13, 0.11), spec=0.12, shininess=8),
            Material((0.36, 0.37, 0.34), shininess=6, spec=0.08),
            Material((0.2, 0.21, 0.23), metal=0.3, shininess=20, spec=0.2)]
    return scene, mats


def _locker(state):
    scene, mats = locker_model(state)
    return bu.render(scene, mats, LK, LK_EXT)


# =========================================================================== jobs

def _job(job):
    kind = job[0]
    if kind == "mirror":
        return _mirror(*job[1:])
    if kind == "graft":
        return _graft(*job[1:])
    if kind == "shuttle":
        return _shuttle(*job[1:])
    return _locker(*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


def build_mirror():
    parts = ("hull", "plate", "mask", "cracks", "pods", "frame")
    res = run([("mirror", k, part) for part in parts for k in range(16)])
    hull, plate, mask, cracks, pods, frame = (res[i * 16:(i + 1) * 16] for i in range(6))
    save_set("mirror-interceptor", artkit.quantize_set([rim_except(h, f) for h, f in zip(hull, frame)], 48))
    save_set("mirror-interceptor-plate", artkit.quantize_set(plate, 40))
    save_set("mirror-interceptor-plate-glow", [plate_glow(c) for c in mask])
    save_set("mirror-interceptor-cracks", [crack_layer(c) for c in cracks])
    muzzles = [centroids(p) for p in pods]
    save_set("mirror-interceptor-pods", [pod_layer(p, m) for p, m in zip(pods, muzzles)])
    write_pivots("mirror-interceptor", {"size": [MI, MI], "headings": 16, "muzzles": muzzles,
                                        "note": "per heading the two gun pods' muzzles (the burst's spawn and the "
                                                "tell's centre), [x, y] px from the frame's top left"})


def build_graft():
    jobs = ([("graft", "base")] + [("graft", "head", k) for k in range(32)]
            + [("graft", "regrown", k) for k in range(32)] + [("graft", "muzzle", k) for k in range(32)]
            + [("graft", "wreck"), ("graft", "root"), ("graft", "root-glow"), ("graft", "stump")]
            + [("graft", "knit", 0, f / 7) for f in range(8)])
    res = run(jobs)
    base, heads, regrown, muz = res[0], res[1:33], res[33:65], res[65:97]
    wreck, root, root_glow, stump = res[97:101]
    knit = res[101:109]
    body = artkit.quantize_set([bu.rim(base)] + [bu.rim(f) for f in heads + regrown], 36)
    save_set("graft-turret-base", [body[0]], single=True)
    save_set("graft-turret", body[1:33])
    save_set("graft-turret-regrown", body[33:65])
    rest = artkit.quantize_set([bu.rim(wreck), root, stump] + [bu.rim(f) for f in knit], 40)
    save_set("graft-turret-wreck", [rest[0]], single=True)
    save_set("graft-turret-root", [rest[1]], single=True)
    save_set("graft-turret-stump", [rest[2]], single=True)
    save_set("graft-turret-knit", rest[3:])
    save_set("graft-turret-root-glow", [root_glow_layer(root_glow)], single=True)
    save_set("graft-turret-crumble", crumble())
    muzzles = [centroids(m, 1)[0] for m in muz]
    write_pivots("graft-turret", {"size": [GT, GT], "angles": 32, "muzzles": muzzles,
                                  "note": "per barrel angle the muzzle (the pair's spawn and the red glint's centre), "
                                          "[x, y] px from the frame's top left"})


def build_shuttle():
    sets = shuttle_sets(run(shuttle_jobs("a")))
    for name, frames in sets.items():
        save_set(name, frames)


def build_locker():
    frames = artkit.quantize_set([bu.rim(f) for f in run([("locker", s) for s in range(3)])], 32)
    save_set("cdf-locker", frames)


BUILDERS = {"mirror": build_mirror, "graft": build_graft, "shuttle": build_shuttle, "locker": build_locker}


# =========================================================================== review helpers

FW = 480
_DECK = {}


def deck(h=900, burnt=((560, 760),)):
    """Round 72's Level 37 deck stand-in (enemies_r72.deck_bg; ART-C2's production tiles are made beside this step):
    the gold-white plating with dome rims at the top, a burnt stretch after the turn lower down."""
    key = (h, burnt)
    if key not in _DECK:
        _DECK[key] = e72.deck_bg(371, h, burnt=burnt).convert("RGBA")
    return _DECK[key]


def paste(dst, img, x, y, alpha=1.0):
    if alpha < 1.0:
        a = np.array(img).astype(np.float64)
        a[..., 3] *= alpha
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    return l23kit.paste_c(dst, img, x, y)


def addc(dst, img, x, y, k=1.0, col=None):
    if col is not None:
        a = np.array(img).astype(np.float64)
        a[..., :3] = a[..., :3] * np.asarray(col, float) / 255
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return l23kit.add_c(dst, img, x, y, k)


def light_at(img, x, y, r, col, k=1.0):
    a = np.array(img).astype(np.float64)
    glow(a[..., :3], x, y, r, col, k)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def zoom(img, z):
    return img.resize((img.width * z, img.height * z), Image.NEAREST)


def wrap(s, n):
    words, out, cur = s.split(), [], ""
    for wd in words:
        if len(cur) + len(wd) + 1 > n:
            out.append(cur)
            cur = wd
        else:
            cur = (cur + " " + wd).strip()
    return out + [cur]


def notes(sheet, y, lines, width=170):
    for line in lines:
        for part in wrap(line, width):
            raster.draw_text(sheet, 16, y, part, raster.LABEL_DIM)
            y += 12
        y += 4
    return y


def on_black(img, z=1):
    c = Image.new("RGBA", img.size, (0, 0, 0, 255))
    return zoom(addc(c, img, img.width / 2, img.height / 2), z)


def shot(img, x, y, col=(255, 84, 40)):
    img = light_at(img, x, y, 3.2, col, 0.9)
    return light_at(img, x, y, 1.2, (255, 240, 220), 0.9)


def ship():
    return l23kit.ship()


# =========================================================================== review: Mirror Interceptor

DULL_RED, ORANGE, WHITE_HOT = np.array((158, 18, 5.0)), np.array((255, 133, 26.0)), np.array((255, 245, 219.0))


def heat_colour(s):
    """MirrorLooks.heatColour and heatOpacity: dull red, orange, then white; smoothstep opacity."""
    s = float(np.clip(s, 0, 1))
    col = DULL_RED + (ORANGE - DULL_RED) * s / 0.5 if s < 0.5 else ORANGE + (WHITE_HOT - ORANGE) * (s - 0.5) / 0.5
    return col, s * s * (3 - 2 * s)


def crack_opacity(s):
    return 0.0 if s < 0.7 else 0.4 + 0.6 * min(1.0, (s - 0.7) / 0.3)


def mi_to_screen(mx, my, k, cx=MI / 2, cy=MI / 2):
    """A model point (units, +Y the nose) on heading k's frame, px (heading 0 nose down, clockwise)."""
    th = TAU * k / 16
    px, py = -mx * MI / MI_EXT, my * MI / MI_EXT
    return cx + px * np.cos(th) - py * np.sin(th), cy + px * np.sin(th) + py * np.cos(th)


def plate_point(phi, k, cx, cy):
    return mi_to_screen(np.sin(phi) * e72.MI_R, e72.MI_CY + np.cos(phi) * e72.MI_R, k, cx, cy)


def mirror_at(img, sets, k, x, y, state="up", heat=0.0, regrow=1.0, tell=0.0, t=0.0):
    """The Mirror Interceptor as C4b will draw it: the hull, then the plate (standing or regrowing at its share), the
    cracks and the heat through the plate's glow mask, the specular sweep and the regrowth's shimmer as soft lights
    along the plate (MirrorLooks' code), the pods' tell."""
    hull, plate, pglow, cracks, pods = sets
    img = paste(img, hull[k], x, y)
    cx, cy = x - MI / 2, y - MI / 2
    if state == "up":
        img = paste(img, plate[k], x, y)
        c = crack_opacity(heat)
        if c > 0:
            cr = np.array(cracks[k]).astype(np.float64)
            cr[..., :3] = (31, 20, 15)
            cr[..., 3] *= c
            img = paste(img, Image.fromarray(cr.astype(np.uint8), "RGBA"), x, y)
        phi = -e72.MI_ARC + 2 * e72.MI_ARC * ((t / 2.5) % 1.0)
        img = light_at(img, *plate_point(phi, k, cx, cy), 4.0, (255, 250, 235), 0.35)
        if heat > 0:
            col, hot = heat_colour(heat)
            img = addc(img, pglow[k], x, y, hot, col)
            if c > 0:
                img = addc(img, cracks[k], x, y, c * hot, col)
    elif state == "regrow":
        img = paste(img, plate[k], x, y, regrow)
        phi = -e72.MI_ARC + 2 * e72.MI_ARC * regrow
        img = light_at(img, *plate_point(phi, k, cx, cy), 6.0, (235, 240, 255), 0.9)
    if tell > 0:
        img = addc(img, pods[k], x, y, tell)
    return img


def shards(img, x, y, facing_k, age, how):
    """MirrorLooks.shardOffset's nine shards (0.5 s), as small silver (or glowing) triangles."""
    d = ImageDraw.Draw(img)
    t = age
    if t >= 0.5:
        return img
    facing = TAU * facing_k / 16
    for k in range(9):
        spread = np.pi * ((k + 0.5) / 9 - 0.5)
        ang = facing + spread * 1.1
        speed = 70 + 60 * ((k * 7) % 9) / 9
        dist = speed * t * (1 - 0.45 * t / 0.5)
        sx, sy = x + np.sin(ang) * dist, y + np.cos(ang) * dist
        a = k * 1.7 + t * 14
        op = (1 - t / 0.5) ** 2
        col = (235, 240, 250) if how else (255, 190, 90)
        pts = [(sx + np.cos(a + j * 2.1) * 2.6, sy + np.sin(a + j * 2.1) * 2.6) for j in range(3)]
        d.polygon(pts, fill=col + (int(255 * op),))
    return img


def pivot_path(root):
    return (artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots") / f"{root}.json"


def mirror_sets():
    return [l23kit.load(n) for n in ("mirror-interceptor", "mirror-interceptor-plate", "mirror-interceptor-plate-glow",
                                     "mirror-interceptor-cracks", "mirror-interceptor-pods")]


def review_mirror():
    sets = mirror_sets()
    hull, plate, pglow, cracks, pods = sets
    bg = deck()
    talon = l23kit.load("talon")
    W = 1400
    sheet = raster.sheet(W, 900, "MIRROR INTERCEPTOR (FINAL R75): THE CRESCENT", f"PRODUCTION ART, {BATCH.upper()} - R75")
    raster.draw_text(sheet, 16, 38, "16 HEADINGS AT 1X OVER ROUND 72'S LEVEL 37 DECK STAND-IN, THE PLATE STANDING "
                                    "(HULL + PLATE FRAMES)", raster.LABEL)
    for k in range(16):
        c = bg.crop((60 + k * 20, 40, 60 + k * 20 + MI, 40 + MI)).copy()
        c = mirror_at(c, sets, k, MI / 2, MI / 2)
        sheet.alpha_composite(c, (16 + k * (MI + 2), 54))
    y = 54 + MI + 14
    states = [("STANDING", dict()), ("HEAT 40 %", dict(heat=0.4)), ("HEAT 78 %, CRACKS", dict(heat=0.78)),
              ("HEAT 100 %", dict(heat=1.0)), ("BROKEN (3 S)", dict(state="down")),
              ("REGROWING 50 %", dict(state="regrow", regrow=0.5)), ("POD TELL (0.4 S)", dict(tell=1.0))]
    raster.draw_text(sheet, 16, y, "THE STATES AT 2X (HEADING 1): THE HEAT, CRACKS, SWEEP, SHIMMER AND TELL AS THE GAME "
                                   "LAYERS THEM OVER THE FRAMES (MIRRORLOOKS' COLOURS AND OPACITIES)", raster.LABEL)
    for i, (lab, kw) in enumerate(states):
        c = bg.crop((150, 120, 150 + MI, 120 + MI)).copy()
        c = mirror_at(c, sets, 1, MI / 2, MI / 2, t=0.9, **kw)
        sheet.alpha_composite(zoom(c, 2), (16 + i * (2 * MI + 26), y + 16))
        raster.draw_text(sheet, 16 + i * (2 * MI + 26), y + 20 + 2 * MI, lab, raster.LABEL_DIM)
    y += 2 * MI + 40
    raster.draw_text(sheet, 16, y, "THE LAYERS AT 2X (HEADING 3): HULL WITH THE BARE FRAME / PLATE / PLATE GLOW "
                                   "(ADDITIVE MASK) / CRACKS (WHITE, TINTED BY THE GAME) / PODS (ADDITIVE)", raster.LABEL)
    layers = [hull[3], plate[3], None, None, None]
    for i, img in enumerate(layers):
        if i == 2:
            cell = on_black(pglow[3], 2)
        elif i == 3:
            cell = Image.new("RGBA", (MI, MI), (40, 40, 60, 255))
            cell.alpha_composite(cracks[3])
            cell = zoom(cell, 2)
        elif i == 4:
            cell = on_black(pods[3], 2)
        else:
            cell = Image.new("RGBA", (MI, MI), (40, 40, 60, 255))
            cell.alpha_composite(img)
            cell = zoom(cell, 2)
        sheet.alpha_composite(cell, (16 + i * (2 * MI + 12), y + 16))
    x0 = 16 + 5 * (2 * MI + 12) + 10
    raster.draw_text(sheet, x0, y, "1X OVER THE BRIGHT DECK AND THE BURNT DECK, BESIDE THE SHIP AND A TALON", raster.LABEL)
    for j, (by, k) in enumerate(((60, 0), (600, 0))):
        c = bg.crop((100, by, 300, by + 168)).copy()
        c = mirror_at(c, sets, 2, 60, 60)
        c = paste(c, talon[0], 140, 50)
        c = paste(c, ship(), 120, 130)
        sheet.alpha_composite(zoom(c, 1), (x0 + j * 210, y + 16))
    y += 2 * MI + 34
    notes(sheet, y, [
        "ROUND 72'S A 'CRESCENT' (ENEMIES_R72.PY MI_HULL / MI_MIRROR, UNCHANGED) AT THE CONCEPT'S SCALE: 36.5 PX A UNIT, "
        "THE PLATE 76 PX ACROSS, THE HULL ABOUT 72 X 68 IN AN 84 PX FRAME; THE REGULAR LIVERY (GUNMETAL, GOLD TRIM AND "
        "CHEVRON, RED LIGHTS, THE 1 PX GOLD RIM ON THE SUNWARD EDGES), THE BARE FRAME LEFT GREY.",
        "THE PLATE IS ITS OWN FRAME PER HEADING (THE GAME DRAWS FRAME K OF THE MIRROR'S FACING INSTEAD OF TURNING A "
        "GENERATED CRESCENT); HEAT, CRACKS, SWEEP, SHIMMER AND SHARDS STAY MIRRORLOOKS' CODE OVER IT. EVERY GLOW AND MASK "
        "A SMOOTH FLOAT FIELD (NO DITHER).",
        "PODS: THE TELL'S RED GLOW AT STRENGTH 1 ON THE TWO MUZZLES (PIVOTS/MIRROR-INTERCEPTOR.JSON, THE BURST'S SPAWN)."])
    sheet = sheet.crop((0, 0, W, min(900, y + 70)))
    frames = mirror_gif(sets)
    artkit.save_review(sheet, frames, CONCEPT["mirror"], "mirror-interceptor", fps=10)


def heading_toward(x0, y0, x1, y1):
    return l23kit.heading_index(x1 - x0, y1 - y0)


def turn_toward(cur, want, max_steps):
    d = (want - cur + 8) % 16 - 8
    return (cur + int(np.clip(d, -max_steps, max_steps))) % 16


def mirror_gif(sets):
    """A pincer over the deck: one from each side edge to its station (flank showing on the way in), turning to the
    ship at 60 deg/s; the right one's pods tell and fire bursts of 3; the ship holds a beam on the left one: the plate
    heats, cracks, breaks (shards), is down 3 s, regrows; both leave by their edges."""
    bg = deck(1400, burnt=())
    fps, dur, H = 10, 10.0, 400
    frames = []
    shots = []
    sides = [dict(edge=-60, sx=110, sy=150, k=4), dict(edge=FW + 60, sx=370, sy=190, k=12)]
    heat, broke = 0.0, None
    tells = [3.0, 5.6, 8.0]
    muzzles = json.loads(pivot_path("mirror-interceptor").read_text())["muzzles"]
    for i in range(int(dur * fps)):
        t = i / fps
        top = int(1400 - H - t * 30)
        img = bg.crop((0, top, FW, top + H)).copy()
        shx, shy = 240 + 90 * np.sin(t * 0.7), 340
        for n, s in enumerate(sides):
            u_in, u_out = smoothstep(0.0, 1.6, t), smoothstep(8.6, 10.0, t)
            x = s["edge"] + (s["sx"] - s["edge"]) * u_in + (s["edge"] - s["sx"]) * u_out
            y = s["sy"] - 50 * (1 - u_in) - 40 * u_out
            if t < 1.6 or t > 8.6:
                dx = (s["sx"] - s["edge"]) * (1 if t < 1.6 else -1)
                s["k"] = heading_toward(0, 0, dx, (50 if t < 1.6 else -40))
            else:
                s["k"] = turn_toward(s["k"], heading_toward(x, y, shx, shy), 1)
            state, regrow, h = "up", 1.0, 0.0
            if n == 0:
                if 2.4 <= t < 3.4 and broke is None:
                    heat = min(1.0, heat + 1.0 / fps)
                    if heat >= 1.0:
                        broke = t
                elif broke is None:
                    heat = max(0.0, heat - 0.5 / fps)
                h = heat
                if broke is not None:
                    since = t - broke
                    if since < 3.0:
                        state = "down"
                    elif since < 3.4:
                        state, regrow = "regrow", (since - 3.0) / 0.4
                    else:
                        heat, h = 0.0, 0.0
                if 2.4 <= t < (broke if broke else 3.4):
                    px, py = mi_to_screen(0, e72.MI_CY + e72.MI_R, s["k"], x, y)
                    d = ImageDraw.Draw(img)
                    d.line([(shx, shy - 14), (px, py)], fill=(120, 230, 255, 255), width=3)
                    d.line([(shx, shy - 14), (px, py)], fill=(235, 250, 255, 255), width=1)
            tell = 0.0
            if n == 1:
                for tt in tells:
                    if tt - 0.4 <= t < tt:
                        tell = (t - tt + 0.4) / 0.4
                    for j in range(3):                       # the burst of 3, 0.15 s apart, the pods in turn
                        if abs(t - tt - 0.15 * j) < 0.5 / fps:
                            mx, my = muzzles[s["k"]][j % 2]
                            ox, oy = x - MI / 2 + mx, y - MI / 2 + my
                            ang = np.arctan2(shy - oy, shx - ox)
                            shots.append((t, ox, oy, np.cos(ang) * 160, np.sin(ang) * 160))
            img = mirror_at(img, sets, s["k"], x, y, state=state, heat=h, regrow=regrow, tell=tell, t=t)
            if n == 0 and broke is not None and t - broke < 0.5:
                img = shards(img, x - np.sin(TAU * s["k"] / 16) * 30, y + np.cos(TAU * s["k"] / 16) * 30,
                             s["k"], t - broke, 0)
        for t0, ox, oy, vx, vy in shots:
            u = t - t0
            if 0 <= u < 2.5:
                img = shot(img, ox + vx * u, oy + vy * u)
        img = paste(img, ship(), shx, shy)
        frames.append(img)
    return frames


# =========================================================================== review: Graft Turret

GRAFT_SETS = ("graft-turret-base", "graft-turret", "graft-turret-regrown", "graft-turret-wreck", "graft-turret-root",
              "graft-turret-root-glow", "graft-turret-knit", "graft-turret-stump", "graft-turret-crumble")


def graft_sets():
    return {n.replace("graft-turret", "").lstrip("-") or "head": l23kit.load(n) for n in GRAFT_SETS}


def root_pulse(share, window=5.0):
    """RegrowthLooks.pulse: a heartbeat quickening 1 -> 4 Hz over the window (its phase the rate's integral)."""
    s = float(np.clip(share, 0, 1))
    phase = window * (s + 1.5 * s * s)
    return 0.5 + 0.5 * np.cos(TAU * phase)


def graft_at(img, g, x, y, state="turret", k=0, u=0.0, glint=0.0):
    """The Graft Turret as C4b will draw it: the base, then by state the head (or the regrown head), the wreck with the
    root over its centre (the root's glow at the pulse, the ring shrinking: RegrowthLooks' code), the knit frame, or
    the stump with the crumble."""
    if state != "stump":
        img = paste(img, g["base"][0], x, y)
    if state in ("turret", "regrown"):
        img = paste(img, g["head" if state == "turret" else "regrown"][k], x, y)
        if glint > 0:
            mx, my = MUZZLES[k]
            img = light_at(img, x - GT / 2 + mx, y - GT / 2 + my, 3.0, (255, 60, 50), 1.3 * glint)
    elif state == "root":
        img = paste(img, g["wreck"][0], x, y)
        img = paste(img, g["root"][0], x, y)
        img = addc(img, g["root-glow"][0], x, y, 0.35 + 0.75 * root_pulse(u))
        r = 1.1 * 12 * (1 - u)
        if r > 0.5:
            e72.ring_light(img, x, y, r, TEAL, 0.9)
    elif state == "knit":
        img = paste(img, g["knit"][min(7, int(u * 8))], x, y)
        if u > 0.75:
            img = paste(img, g["regrown"][k], x, y, (u - 0.75) / 0.25)
    else:
        img = paste(img, g["stump"][0], x, y)
        if u < 1.0:
            img = addc(img, g["crumble"][min(7, int(u * 8))], x, y)
    return img


MUZZLES = []


def review_graft():
    g = graft_sets()
    MUZZLES[:] = json.loads(pivot_path("graft-turret").read_text())["muzzles"]
    bg = deck()
    sam = l23kit.load("sam-nest") if (artkit.SPRITES / "sam-nest_0.png").exists() else None
    W = 1400
    sheet = raster.sheet(W, 1000, "GRAFT TURRET (FINAL R75): TISSUE FIRST", f"PRODUCTION ART, {BATCH.upper()} - R75")
    raster.draw_text(sheet, 16, 38, "THE TURRET'S 32 ANGLES AT 1X ON ITS BASE, OVER ROUND 72'S LEVEL 37 DECK STAND-IN "
                                    "(ROWS 1-2), THE REGROWN TURRET (ROWS 3-4)", raster.LABEL)
    for row, state in enumerate(("turret", "regrown")):
        for k in range(32):
            c = bg.crop((40 + (k % 8) * 40, 40 + row * 60, 40 + (k % 8) * 40 + GT, 40 + row * 60 + GT)).copy()
            c = graft_at(c, g, GT / 2, GT / 2, state, k)
            sheet.alpha_composite(c, (16 + (k % 16) * (GT + 4), 54 + (2 * row + k // 16) * (GT + 4)))
    y = 54 + 4 * (GT + 4) + 12
    cyc = [("TURRET", dict()), ("ITS TELL (0.25 S)", dict(glint=1.0)), ("DIES: ROOT OPEN", dict(state="root", u=0.0)),
           ("WINDOW 60 %", dict(state="root", u=0.6)), ("WINDOW 95 %", dict(state="root", u=0.95)),
           ("KNIT 0.1 S", dict(state="knit", u=0.1)), ("KNIT 0.4 S", dict(state="knit", u=0.5)),
           ("KNIT 0.7 S", dict(state="knit", u=0.88)), ("REGROWN", dict(state="regrown")),
           ("ROOT KILLED", dict(state="stump", u=0.3)), ("STUMP", dict(state="stump", u=1.0))]
    raster.draw_text(sheet, 16, y, "THE CYCLE AT 2X (ANGLE 3): THE ROOT'S GLOW AT THE PULSE AND THE SHRINKING RING ARE "
                                   "REGROWTHLOOKS' CODE OVER THE FRAMES; THE KNIT FADES THE REGROWN HEAD IN OVER ITS "
                                   "LAST TWO FRAMES", raster.LABEL)
    for i, (lab, kw) in enumerate(cyc):
        c = bg.crop((120, 330, 120 + GT + 8, 330 + GT + 8)).copy()
        c = graft_at(c, g, GT / 2 + 4, GT / 2 + 4, k=3, **kw)
        sheet.alpha_composite(zoom(c, 2), (16 + i * 124, y + 16))
        raster.draw_text(sheet, 16 + i * 124, y + 20 + 2 * (GT + 8), lab, raster.LABEL_DIM)
    y += 2 * (GT + 8) + 40
    raster.draw_text(sheet, 16, y, "THE FRAMES AT 1X: BASE, WRECK, ROOT, ROOT GLOW (ADDITIVE), STUMP, KNIT 0-7, "
                                   "CRUMBLE 0-7 (ADDITIVE)", raster.LABEL)
    cells = ([g["base"][0], g["wreck"][0], g["root"][0], None, g["stump"][0]] + list(g["knit"]))
    x = 16
    for img in cells:
        if img is None:
            cell = on_black(g["root-glow"][0])
        else:
            cell = Image.new("RGBA", (GT, GT), (40, 40, 60, 255))
            cell.alpha_composite(img)
        sheet.alpha_composite(cell, (x, y + 16))
        x += GT + 6
    for f in g["crumble"]:
        cell = on_black(f)
        sheet.alpha_composite(cell, (x, y + 16 + 2))
        x += 48 + 6
    y += GT + 30
    raster.draw_text(sheet, 16, y, "A NEST OF THREE AT 1X ON THE BRIGHT DECK AND THE BURNT DECK, BESIDE THE SHIP "
                                   "(ONE ROOT OPEN, ONE REGROWN)", raster.LABEL)
    for j, by in enumerate((80, 620)):
        c = bg.crop((60, by, 360, by + 150)).copy()
        c = graft_at(c, g, 60, 50, "turret", 2)
        c = graft_at(c, g, 130, 40, "root", u=0.3)
        c = graft_at(c, g, 200, 52, "regrown", 30)
        if sam:
            c = paste(c, sam[0], 260, 50)
        c = paste(c, ship(), 150, 118)
        sheet.alpha_composite(c, (16 + j * 316, y + 16))
    y += 150 + 26
    y = notes(sheet, y, [
        "ROUND 72'S B 'TISSUE FIRST' (ENEMIES_R72.PY GRAFT_BASE / GRAFT_HEAD / GRAFT_ROOT, UNCHANGED; THE REGROWN BARREL'S "
        "BONE GIVEN ITS SLOT) AT THE CONCEPT'S 22.6 PX A UNIT IN A 52 PX FRAME: THE BASE WITH ITS COLLAR AND SIX ROOTS, "
        "THE DOME A THIRD OVERGROWN, THE TEAL SEAMS LIT IN THE FRAMES (A STEADY GLOW); THE 1 PX GOLD RIM.",
        "THE WRECK'S HALVES ARE THE DOME CUT AND THROWN APART, SCORCHED, THE BARREL FALLEN BESIDE THEM; THE KNIT DRAWS "
        "THEM BACK UNDER SPREADING TISSUE INTO THE VEINED DOME (8 FRAMES OVER 0.8 S, NO BARREL: THE REGROWN HEAD OF THE "
        "TURRET'S ANGLE FADES IN OVER THE LAST TWO). THE ROOT'S DEATH: TORN TEAL FIBRES, NO POP.",
        "SAM NEST AT THE RIGHT FOR SCALE (LEVEL 37'S OTHER NEST)."])
    sheet = sheet.crop((0, 0, W, min(1000, y + 10)))
    artkit.save_review(sheet, graft_gif(g), CONCEPT["graft"], "graft-turret", fps=10)


def graft_gif(g):
    """A nest of three on the deck scrolling down under the ship: the turrets track it (120 deg/s) and fire pairs with
    the red glint; the left one dies, its root pulses and its ring shrinks until the ship kills the root (the
    crumble, the stump); the middle one dies, its window runs out, it knits and comes back regrown."""
    bg = deck(1400, burnt=())
    fps, dur, H = 10, 11.0, 400
    nest = [dict(x=150, y0=40, k=0, die=2.0, root=3.6), dict(x=250, y0=10, k=0, die=3.0, root=None),
            dict(x=350, y0=50, k=0, die=None, root=None)]
    shots, frames = [], []
    boom = artkit.load_frames("explosion-small")
    for i in range(int(dur * fps)):
        t = i / fps
        top = int(1400 - H - t * 20)
        img = bg.crop((0, top, FW, top + H)).copy()
        shx, shy = 250 + 80 * np.sin(t * 0.6), 340
        for n, s in enumerate(nest):
            x, y = s["x"], s["y0"] + 20 * t
            state, u = "turret", 0.0
            if s["die"] is not None and t >= s["die"]:
                since = t - s["die"]
                if s["root"] is not None and t >= s["root"]:
                    state, u = "stump", min(1.0, (t - s["root"]) / 0.27)
                elif since < 5.0:
                    state, u = "root", since / 5.0
                elif since < 5.8:
                    state, u = "knit", (since - 5.0) / 0.8
                else:
                    state = "regrown"
            if state in ("turret", "regrown", "knit"):
                want = int(round(np.arctan2(-(shx - x), shy - y) / TAU * 32)) % 32
                d = (want - s["k"] + 16) % 32 - 16
                s["k"] = (s["k"] + int(np.clip(d, -1, 1))) % 32 if state != "knit" else s["k"]
            glint = 0.0
            if state in ("turret", "regrown"):
                for tf in np.arange(0.9 + 0.7 * n, dur, 2.2):
                    if tf - 0.25 <= t < tf:
                        glint = 1.0
                    if abs(t - tf) < 0.5 / fps and (state == "turret" or t - s["die"] > 6.3):
                        mx, my = MUZZLES[s["k"]]
                        ox, oy = x - GT / 2 + mx, y - GT / 2 + my
                        ang = np.arctan2(shy - oy, shx - ox)
                        for sp in (-1, 1):
                            a = ang + sp * np.radians(2)
                            shots.append((t, ox, oy, np.cos(a) * 160, np.sin(a) * 160))
            img = graft_at(img, g, x, y, state, s["k"], u, glint)
            if s["die"] is not None and 0 <= t - s["die"] < 0.6:
                img = addc(img, boom[min(len(boom) - 1, int((t - s["die"]) * 20))], x, y)
        for t0, ox, oy, vx, vy in shots:
            uu = t - t0
            if 0 <= uu < 2.5:
                img = shot(img, ox + vx * uu, oy + vy * uu + 20 * uu)
        img = paste(img, ship(), shx, shy)
        frames.append(img)
    return frames


# =========================================================================== review: the shuttles' livery, the locker

def as_l10(sets):
    """The livery's sets under Level 10's names (shuttle.draw_shuttle's keys), with Level 10's flames and pip."""
    out = {sh.NAME + name[len(LIVERY):]: frames for name, frames in sets.items()}
    for extra in ("flame", "flare", "pip"):
        out[f"{sh.NAME}-{extra}"] = artkit.load_frames(f"{sh.NAME}-{extra}")
    return out


def load_livery():
    return {n: l23kit.load(n) for n in (LIVERY, f"{LIVERY}-damaged", f"{LIVERY}-lift", f"{LIVERY}-wreck")}


def livery_rows(v, sets, l10):
    pv = sh.pivots("a")
    lvl = sh.BANKS.index(0)
    gold = [f for f in (l23kit.load("talon")[0], l23kit.load("gilded-gunship")[0])]
    bg = deck()
    with_flames = []
    for b in range(len(sh.BANKS)):
        cell = Image.new("RGBA", (sh.SIZE[0] + 8, sh.SIZE[1] + 22), artkit.PLATE)
        with_flames.append(sh.draw_shuttle(cell, l10, pv, sh.NAME, b, cell.width / 2, sh.SIZE[1] / 2 + 3, flame=b,
                                           shadow=None))
    scenes = []
    for by in (60, 610):
        c = bg.crop((120, by, 420, by + 130)).copy()
        c = sh.draw_shuttle(c, l10, pv, sh.NAME, lvl, 60, 50, flame=1)
        c = paste(c, artkit.load_frames(sh.NAME)[lvl], 140, 50)
        c = paste(c, gold[0], 205, 45)
        c = paste(c, gold[1], 260, 70)
        scenes.append(c)
    return [
        (f"AURELIA LIVERY {v.upper()}: BANKING FRAMES -30 -15 0 +15 +30 DEG, FLYING UP", sets[LIVERY], 4, False),
        ("WITH LEVEL 10'S ENGINE FLAMES AT ITS PIVOTS (THE SAME MODEL, THE SAME MOUNTS)", with_flames, 3, False),
        ("DAMAGED (BELOW 50 %)", sets[f"{LIVERY}-damaged"], 4, False),
        ("LIFTOFF STEPS 0.70 / 0.775 / 0.85 / 0.925 X", sets[f"{LIVERY}-lift"], 4, False),
        ("THE GLIDE INTO FAR (8 STEPS)", sets[f"{LIVERY}-wreck"], 3, False),
        ("1X ON THE BRIGHT AND THE BURNT DECK: THE LIVERY, LEVEL 10'S CDF SCHEME, A TALON AND A GILDED GUNSHIP",
         scenes, 2, False),
        ("GREYSCALE CHECK", [scenes[0].convert("L").convert("RGBA")], 2, False)]


def review_shuttle():
    sets = load_livery()
    l10 = as_l10(sets)
    sheet = artkit.review_sheet("EVACUATION SHUTTLE IN AURELIA'S LIVERY A (FINAL R75): WHITE, A GOLD BAND, THE CREST",
                                livery_rows("a", sets, l10), width=1500, batch=BATCH)
    artkit.save_review(sheet, shuttle_gif(l10), CONCEPT["shuttle"], "aurelia-shuttle", fps=15)
    sets_b = shuttle_sets(run(shuttle_jobs("b")))
    sets_b = {n: f for n, f in sets_b.items()}
    sheet = artkit.review_sheet("EVACUATION SHUTTLE IN AURELIA'S LIVERY B (PROPOSAL R75): A GOLD HULL, WHITE TRIM",
                                livery_rows("b", sets_b, as_l10(sets_b)), width=1500, batch=BATCH)
    sheet.convert("RGB").save(CONCEPT["shuttle"] / "aurelia-shuttle-r75-b.png", optimize=True)
    print("review: design/allies/concept/aurelia-shuttle-r75-b.png")


def shuttle_gif(l10):
    """A pair lifting off its pads (x 190 and 290) as the deck scrolls in, climbing to its stations (240, 170) and
    (240, 240) with the flare, holding on Level 10's sway (+-16 px) and climbing out off the top edge."""
    pv = sh.pivots("a")
    bg = deck(1600, burnt=())
    fps, dur, H = 15, 9.0, 420
    lvl = sh.BANKS.index(0)
    frames = []
    pads = [(190, 330), (290, 360)]
    stations = [(240, 170), (240, 240)]
    for i in range(int(dur * fps)):
        t = i / fps
        top = int(1600 - H - t * 100)
        img = bg.crop((0, top, FW, top + H)).copy()
        for n, ((px, py0), (sx, sy)) in enumerate(zip(pads, stations)):
            py = py0 + t * 100 if t < 0.0 else py0
            if t < 3.0:                                           # the liftoff (5 s in the game; 3 s here)
                u = t / 3.0
                step = min(3, int(u * 4))
                x, y = px + (sx - px) * smoothstep(0.3, 1.0, u), py + (sy - py) * smoothstep(0.2, 1.0, u)
                img = sh.draw_shuttle(img, l10, pv, f"{sh.NAME}-lift", step, x, y, flame=i, flame_kind="flare",
                                      flame_scale=sh.LIFT_SCALES[step], shadow=(21 * u, 30 * u), shadow_alpha=0.5)
                continue
            sway = 16 * np.sin(0.9 * (t - 3.0) + n * 1.7)
            vx = 16 * 0.9 * np.cos(0.9 * (t - 3.0) + n * 1.7)
            bank = lvl + (1 if vx > 5 else -1 if vx < -5 else 0)
            y = sy - (0 if t < 7.0 else 90 * (t - 7.0) ** 2)
            kind = "flare" if t >= 7.0 else "flame"
            img = sh.draw_shuttle(img, l10, pv, sh.NAME, bank, sx + sway, y, flame=i + n, flame_kind=kind)
        frames.append(img)
    return frames


def review_locker():
    frames = l23kit.load("cdf-locker")
    bg = deck()
    crate = artkit.load_frames("pickup-crate")
    W = 1000
    sheet = raster.sheet(W, 360, "THE CDF LOCKER (FINAL R75): LEVEL 37'S SECRET", f"PRODUCTION ART, {BATCH.upper()} - R75")
    raster.draw_text(sheet, 16, 38, "SEALED / HIT / BROKEN OPEN AT 4X ON ROUND 72'S DECK STAND-IN (32X28)", raster.LABEL)
    for i, f in enumerate(frames):
        c = bg.crop((150, 200, 150 + LK[0] + 8, 200 + LK[1] + 8)).copy()
        c = paste(c, f, c.width / 2, c.height / 2)
        sheet.alpha_composite(zoom(c, 4), (16 + i * 170, 56))
    x0 = 16 + 3 * 170 + 10
    raster.draw_text(sheet, x0, 38, "1X: BESIDE THE SHIP AND A GRAFT TURRET; BROKEN, THE GAME'S CRATE", raster.LABEL)
    c = bg.crop((40, 240, 320, 400)).copy()
    c = paste(c, frames[0], 50, 50)
    c = paste(c, frames[2], 120, 50)
    if crate:
        c = paste(c, crate[0], 120, 30)
    g = graft_sets()
    MUZZLES[:] = json.loads(pivot_path("graft-turret").read_text())["muzzles"]
    c = graft_at(c, g, 200, 52, "turret", 2)
    c = paste(c, ship(), 140, 125)
    sheet.alpha_composite(zoom(c, 1), (x0, 56))
    y = notes(sheet, 230, [
        "A 2166 CDF SUPPLY LOCKER LEFT ON AURELIA'S DECKS (LEVEL 37, T=60, UNDER A DECK AWNING LEFT OF THE PADS): A "
        "CHAMFERED CDF-GREY BOX WITH A RAISED LID, THE BLUE BAND ACROSS ITS MIDDLE, ORANGE HAZARD ENDS, WHITE STENCIL "
        "MARKS, TWO LATCHES AND A DIM AMBER LAMP; HIT: SOOT, A DENT, THE LAMP DARK; BROKEN: THE LID BLOWN OFF, THE EMPTY "
        "FOAM CRADLE (THE CRATE IS THE GAME'S PICKUP). THE 1 PX GOLD/RED RIM AS THE DECK'S OTHER OBJECTS; 32 COLOURS.",
        "FRAMES AS LEVEL 20'S EMERGENCY CACHE: 0 INTACT, 1 HIT, 2 OPENED (A GROUND TARGET'S SPRITE: CDF-LOCKER, SIZE "
        "[32, 28])."], width=160)
    sheet = sheet.crop((0, 0, W, y + 8))
    path = CONCEPT["locker"] / f"cdf-locker-final-{ROUND}-a.png"
    if not artkit.ONLY:
        sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


REVIEWS = {"mirror": review_mirror, "graft": review_graft, "shuttle": review_shuttle, "locker": review_locker}


# =========================================================================== check

SETS = {"mirror": [("mirror-interceptor", 16, (MI, MI), False), ("mirror-interceptor-plate", 16, (MI, MI), False),
                   ("mirror-interceptor-plate-glow", 16, (MI, MI), True),
                   ("mirror-interceptor-cracks", 16, (MI, MI), False),
                   ("mirror-interceptor-pods", 16, (MI, MI), True)],
        "graft": [("graft-turret-base", 1, (GT, GT), False), ("graft-turret", 32, (GT, GT), False),
                  ("graft-turret-regrown", 32, (GT, GT), False), ("graft-turret-wreck", 1, (GT, GT), False),
                  ("graft-turret-root", 1, (GT, GT), False), ("graft-turret-root-glow", 1, (GT, GT), True),
                  ("graft-turret-knit", 8, (GT, GT), False), ("graft-turret-stump", 1, (GT, GT), False),
                  ("graft-turret-crumble", 8, (48, 48), True)],
        "shuttle": [(LIVERY, 5, sh.SIZE, False), (f"{LIVERY}-damaged", 5, sh.SIZE, False),
                    (f"{LIVERY}-lift", 4, sh.SIZE, False), (f"{LIVERY}-wreck", 8, sh.SIZE, False)],
        "locker": [("cdf-locker", 3, LK, False)]}
PIVOT_ROOTS = ("mirror-interceptor", "graft-turret")


def checker_share(img):
    """DitherGuardTest's measure: visible pixels whose left and right neighbours are alike and up and down alike, both
    pairs unlike the pixel (an ordered dither's cell) -> (visible, checker)."""
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 3] << 24) | (a[..., 0] << 16) | (a[..., 1] << 8) | a[..., 2]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    l, r, u, d = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (l == r) & (u == d) & (l != mid) & (u != mid)
    return int(vis.sum()), int(chk.sum())


def glow_steps(frames):
    """The distinct non-zero levels of an additive set's brightest channel (a smooth glow has >= 32)."""
    vals = np.concatenate([np.array(f)[..., :3].max(axis=-1).ravel() for f in frames])
    return len(np.unique(vals[vals > 0]))


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n, size, add in sets:
            where = l23kit.where(name)
            try:
                frames = l23kit.load(name)
            except FileNotFoundError:
                frames = []
            folder = ROOT / where
            files = [f for f in sorted(folder.glob(f"{name}*.png"))
                     if f.stem == name or (f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
            src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
            vis = chk = 0
            for f in files:
                v, c = checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            steps = glow_steps(frames) if add and frames else None
            ok = (len(frames) == n and all(fr.size == tuple(size) for fr in frames) and src_ok and share < 0.05
                  and (steps is None or steps >= GLOW_STEPS))
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %" + (f", {steps} glow steps" if steps is not None else ""))
    for root in PIVOT_ROOTS:
        ok = pivot_path(root).exists()
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} pivots {pivot_path(root).relative_to(ROOT)}")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    for s in subjects:
        if "--review" not in args:
            BUILDERS[s]()
        REVIEWS[s]()
