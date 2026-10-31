#!/usr/bin/env python3
"""Production art: Level 36's units and objects (M10 part B batch, concept round 74, step ART-B): the Chimera
(design/enemies/air/chimera, round 72 **A.1 b** "overgrown"), the helium-3 harvester (Level 36's level-owned craft,
no round-72 proposal: m10b-gaps QB-1 **a**, the README's look, pending the user's review in round 74), the fleet's drop
craft (CDF Stormhawks in the fleet's grey and blue, marked friendly) and the pathfinder pod (the secret).

The Chimera is round 72's model (tools/concept/enemies_r72.py ``chimera_model`` b, imported unchanged): the
production Talon's airframe (enemies_r60 ``talon_model`` b in belt_units.py's gunmetal) with the tissue seams twice as
wide and veined, nodules at the wing roots, on the spine and the wing panels, the dark teal flesh; rendered at the
Talon's 48 px and 20 px per unit with the Talon's rim. Its seam glow is an emission-only render of the tissue
(``glow_only``), drawn additively over the body at a strength the game sets from the unit's state (the concept's
``add_glow`` k: **0.45 at full health**, **0.08 + 0.1 x HP share just hit** (dark seams for the 1.0 s delay), **0.3 +
0.7 x HP share while it heals**), plus the healing pulse frames (a brighter band running out from the body twice a
second). The harvester, the drop craft and the pod are new models on the frozen render package.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its own
render under the fixed key light. Glows are smooth float fields written unquantised (no ordered dither, far more than
32 steps); additive sets are premultiplied on black (GL_SRC_ALPHA, GL_ONE).

Outputs (build/art-held/sprites/ until the data claims the root - B2 writes the Chimera's, the harvester's and the drop
craft's unit data and Level 36's debris chunk; rerun the subject then and the files move to assets/sprites/):
  chimera_0..15              48x48, 16 headings: the hybrid Talon, gunmetal and gold, the dark teal tissue (40 colours)
  chimera-seams_0..15        48x48 additive: the seam glow at strength 1 (teal 00FF9A), drawn over the body at the
                             state's strength (above)
  chimera-heal_0..95         48x48 additive, heading x 6 + phase: the healing pulse (a band running out from the
                             centre), 6 frames at 12 fps per heading (two pulses a second), added over the seams while
                             it heals
  chimera-flecks_0..7        64x64 additive: the death's spray of teal tissue flecks and gold sparks, 3 steps a frame,
                             over the small explosion (the Talon's ``talon-wing`` is not used: the airframe tears)
  pivots/chimera.json        per heading the two gun muzzles (the pair's spawn, the Talon's red glint ``talon-glint``)
  harvester                  96x56: the helium-3 harvester flying right (its nose right): a long gunmetal hull with gold
                             trim and red running lights over a wide intake scoop, the twin engines at the stern
  harvester-left             96x56: the same flying left (its own render under the top-left light, never mirrored)
  harvester-plume_0..3       240x64 normal, centred on the hull: the pale gas plume trailing behind the scoop (to the
                             left), smooth (no dither), its flow in 4 frames at 6 fps; drawn under the hull
  harvester-left-plume_0..3  240x64: the plume of the left-flying hull (trailing right)
  harvester-break_0..11      128x96 normal, centred on the hull: it breaks in two along a jagged cut, the halves tip and
                             sink into the band (shrinking and fading toward the band's haze), smoke rising from the
                             cut; 4 steps a frame (0.8 s); the left-flying set ``harvester-left-break_0..11``
  harvester-flare_0..11      240x64 additive, centred on the hull: the plume flaring once along its length and the fire
                             at the cut, with the break's frames; ``harvester-left-flare_0..11``
  fleet-stormhawk_0..15      40x40, 16 headings: the CDF Stormhawk in the fleet's grey with the CDF blue band and white
                             wing bars (the drop craft; the player's ship stays its own colours), engines lit
  fleet-stormhawk-trail_0..1 16x48 additive: the engines' exhaust streak while it dives up the screen at 400 px/s,
                             drawn with its top centre on the hull's tail (12 px below the centre at heading 8, up)
  debris-pathfinder-pod_0..15  32x32: the pathfinder's cargo pod (CDF grey, a blue band, orange end stripes, a white
                             beacon), its slow tumble (frame k = k x 22.5 degrees clockwise)
  debris-pathfinder-pod-break_0..7  64x64 normal: the pod shot open in air: grey shell chips flung out, a puff of smoke
                             rising and thinning, 2 steps a frame (the crate is the game's)
Review (round 74): design/enemies/air/chimera/concept/chimera-final-r74-a.png/.gif; Level 36's concept/
harvester-final-r74-a, fleet-stormhawk-final-r74-a and pathfinder-pod-final-r74-a (.png/.gif), on Level 36's
production backdrop (tools/art/backdrop_l36.py's tiles).

Run: python3 tools/art/jovian_units.py [chimera] [harvester] [fleet] [pod] [--review] [--check]
     (no subject = all, ~1 min; --review rebuilds only the review files; --check lists every set with its folder,
     size and frame count and fails on a missing one, a missing Source chunk or a checker over 5 %)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r72 as e72  # noqa: E402  (concept: the Chimera's model, imported unchanged)
from render import models, raster  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.sdf import (Material, rotate_x, sd_box, sd_capsule, sd_ellipsoid, sd_sphere, union)  # noqa: E402

SCRIPT = "jovian_units.py"
BATCH = "M10 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r74"
artkit.REVIEW_ROUND = ROUND
L36 = DESIGN / "campaign" / "act-6-jovian-storm" / "level-36-cloud-divers" / "concept"
CONCEPT = {"chimera": DESIGN / "enemies" / "air" / "chimera" / "concept", "harvester": L36, "fleet": L36, "pod": L36}
PX = 20.0
TAL, TAL_EXT = 48, 48 / PX
TEAL = np.array((0, 255, 154), float)
GOLD = np.array((255, 196, 64), float)


def save_set(name, frames, single=False):
    l23kit.write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def additive_rgb(light):
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def glow(light, x, y, r, col, k):
    h, w = light.shape[:2]
    rr = int(np.ceil(r * 4))
    x0, x1, y0, y1 = max(0, int(x - rr)), min(w, int(x + rr) + 1), max(0, int(y - rr)), min(h, int(y + rr) + 1)
    if x0 >= x1 or y0 >= y1:
        return
    yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64) + 0.5
    light[y0:y1, x0:x1] += (np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k)[..., None] * np.asarray(col, float)


def blur(a, sigma):
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)), mode="edge")
    a = sum(k[i] * p[i:i + a.shape[0], :] for i in range(2 * r + 1))
    p = np.pad(a, ((0, 0), (r, r)), mode="edge")
    return sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))


def rgba(rgb, alpha):
    a = np.clip(np.round(np.asarray(alpha) * 255), 0, 255)
    rgb = np.where(a[..., None] > 0, np.clip(np.round(rgb), 0, 255), 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


# =========================================================================== jobs

def _chimera(k, glow_only):
    scene, mats = e72.chimera_model("b", only_glow=glow_only, tg=0.0 if not glow_only else 1.0)
    if not glow_only:
        mats = bu.gunmetal(mats)
        return bu.render(*bu.turned(scene, mats, k, 16), TAL, TAL_EXT)
    sc, mm = bu.turned(scene, mats, k, 16)
    hi, f = artkit.render_hi(sc, mm, (TAL, TAL), TAL_EXT)
    return artkit.native(hi, f, 0)


def _chimera_guns(k):
    scene, mats = e72.chimera_model("b", tg=0.0)
    mats = bu.emission_only(mats)
    mats[A_GUN] = Material((0, 0, 0), emission=(1.0, 0.0, 1.0))
    return bu.render(*bu.turned(scene, mats, k, 16), TAL, TAL_EXT, crisp=0)


HV = (96, 56)
HV_EXT = HV[0] / PX
DARK = 7                                    # the scoop's dark mouth (an extra material slot)


def harvester_model(part=None, cut_tilt=0.0):
    """The helium-3 harvester, nose +Y: a long gunmetal hull (the Ascendancy's regular livery), gold nose cap and trim
    bands, a dorsal spine, a wide intake scoop slung under its front half (its dark mouth forward), twin engines at
    the stern, a gold tail fin, red running lights on the scoop's tips and the tail. ``part`` 'front' / 'rear' keeps one
    side of a jagged cut across the hull (the break), tipped by ``cut_tilt`` radians about the cut."""
    def cut_y(x):
        return 0.08 + 0.13 * np.sin(x * 9.0 + 0.6) + 0.06 * np.sin(x * 23.0 + 1.9)

    def body(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        q = np.stack([np.abs(x), y, z], axis=-1)
        hull = sd_capsule(p, (0, -1.7, 0.18), (0, 1.55, 0.18), 0.36, 0.3)
        nose = sd_ellipsoid(p, (0, 1.62, 0.18), (0.27, 0.5, 0.24))
        spine = sd_box(p, (0, -0.25, 0.47), (0.09, 1.15, 0.07), 0.05)
        scoop = sd_ellipsoid(p, (0, 0.55, -0.1), (0.84, 0.56, 0.17))
        flare = sd_capsule(p, (-0.72, 0.98, -0.08), (0.72, 0.98, -0.08), 0.07, 0.07)
        nac = sd_capsule(q, (0.44, -1.95, 0.08), (0.44, -1.15, 0.08), 0.17, 0.15)
        fin = sd_box(p, (0, -1.55, 0.58), (0.035, 0.3, 0.17), 0.02)
        noz = sd_capsule(q, (0.44, -2.05, 0.08), (0.44, -1.98, 0.08), 0.13, 0.13)
        lamps = [sd_sphere(q, (0.8, 0.9, 0.0), 0.07), sd_sphere(q, (0.62, -1.6, 0.12), 0.06),
                 sd_sphere(p, (0, -1.85, 0.62), 0.06)]
        d, m = union((hull, A_HULL), (nose, A_GOLD), (spine, A_FACET), k=0.05)
        d, m = union((d, m), (scoop, A_FACET), (flare, A_GOLD), (nac, A_HULL), (fin, A_GOLD), k=0.06)
        d, m = union((d, m), (noz, A_ENGINE), *[(lp, A_RED) for lp in lamps], k=0.01)
        band = ((np.abs(y - 1.08) < 0.05) | (np.abs(y + 0.25) < 0.05) | (np.abs(y + 1.32) < 0.05)) & (m == A_HULL)
        m = np.where(band, A_GOLD, m)
        mouth = (y > 0.98) & (z < -0.02) & (m == A_FACET)
        m = np.where(mouth, DARK, m)
        return d, m

    if part is None:
        return body

    def half(p):
        q = p.copy()
        if cut_tilt:
            pivot = np.array((0.0, 0.08, 0.18))
            q = rotate_x(q - pivot, cut_tilt if part == "front" else -cut_tilt) + pivot
        d, m = body(q)
        c = cut_y(q[:, 0])
        side = (c - q[:, 1]) if part == "front" else (q[:, 1] - c)
        return np.maximum(d, side * 0.7), m
    return half


def harvester_mats():
    mats = e72.regular_mats("3a3036", 1.0)
    mats[A_ENGINE] = Material((0.08, 0.04, 0.02), emission=(1.6, 0.72, 0.26))
    return mats + [Material((0.05, 0.05, 0.06), metal=0.2, shininess=20, spec=0.2)]


HEAD = {"right": 12, "left": 4}             # heading k of 16: 12 = nose right, 4 = nose left (clockwise from down)


def _harvester(side, part=None, tilt=0.0, size=HV):
    scene = harvester_model(part, tilt)
    return bu.render(*bu.turned(scene, harvester_mats(), HEAD[side], 16), size, size[0] / PX)


FLEET_PAL = {"hull": (0.62, 0.64, 0.68), "hull_dark": (0.3, 0.32, 0.36), "accent": (0.16, 0.40, 0.90),
             "accent2": (0.92, 0.92, 0.94), "engine": (0.55, 0.8, 1.0)}
FLEET, FLEET_EXT, FLEET_CAM = 40, 2.3, 4.0


def _fleet(k):
    scene, mats = models.ship_c_model(bank=0.0, palette=FLEET_PAL)
    return bu.render(*bu.turned(scene, mats, k, 16), FLEET, FLEET_EXT, camera=FLEET_CAM)


POD, POD_EXT = 32, 32 / PX


def pod_model():
    """The pathfinder's cargo pod, its long axis +Y: a CDF-grey capsule with a blue band round its middle, orange
    hazard stripes at both ends, two short fins and a white beacon on top."""
    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        body = sd_capsule(p, (0, -0.42, 0.0), (0, 0.42, 0.0), 0.32, 0.3)
        fins = sd_box(p, (0, -0.52, 0.0), (0.44, 0.08, 0.03), 0.02)
        lamp = sd_sphere(p, (0, 0.08, 0.31), 0.07)
        d, m = union((body, 0), (fins, 1), k=0.04)
        d, m = union((d, m), (lamp, 5), k=0.0)
        m = np.where((m == 0) & (np.abs(y) < 0.09), 2, m)
        m = np.where((m == 0) & (np.abs(np.abs(y) - 0.55) < 0.07), 3, m)
        return d, m
    mats = [Material((0.62, 0.64, 0.68), metal=0.35, shininess=60, spec=0.55),
            Material((0.3, 0.32, 0.36), metal=0.5, shininess=30, spec=0.35),
            Material((0.16, 0.40, 0.90), metal=0.3, shininess=50, spec=0.5),
            Material((0.95, 0.52, 0.12), metal=0.2, shininess=40, spec=0.4),
            Material((0.1, 0.1, 0.12)),
            Material((0.2, 0.2, 0.2), emission=(2.4, 2.4, 2.4))]
    return scene, mats


def _pod(k):
    scene, mats = pod_model()
    return bu.render(*bu.turned(scene, mats, k, 16), POD, POD_EXT)


JOBS = {"chimera": _chimera, "guns": _chimera_guns, "harvester": _harvester, "fleet": _fleet, "pod": _pod}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== Chimera

SEAM_STATES = {"full": 0.45, "hit": 0.13, "heal-50": 0.65, "heal-95": 0.965}


def seam_layer(img):
    """The emission-only render -> the additive seam glow at strength 1: the teal on the seams plus a soft 1 px halo,
    smooth (48 steps of strength would do; written unquantised)."""
    a = np.array(img).astype(np.float64)
    e = a[..., :3] * (a[..., 3:4] > 0)
    halo = np.dstack([blur(e[..., c], 0.9) for c in range(3)])
    return np.clip(e * 0.9 + halo * 0.55, 0, 255)


def heal_ring(phase, w=TAL):
    """The concept's pulse: 0.9 x exp(-((r - 1.2 phase) / 0.16)^2), r from the centre over half the frame."""
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64)
    r = np.hypot(xx - w / 2 + 0.5, yy - w / 2 + 0.5) / (0.5 * w)
    return 0.9 * np.exp(-((r - phase * 1.2) / 0.16) ** 2)


def flecks(n_frames=8, w=64, seed=3661):
    rng = np.random.default_rng(seed)
    bits = [(rng.uniform(0, TAU), rng.uniform(60, 130), rng.uniform(0.9, 1.6), "teal") for _ in range(14)]
    bits += [(rng.uniform(0, TAU), rng.uniform(110, 190), rng.uniform(0.5, 0.9), "gold") for _ in range(9)]
    frames = []
    for f in range(n_frames):
        u = (f + 0.5) / n_frames                       # 0.4 s: 3 steps a frame
        light = np.zeros((w, w, 3))
        for ang, speed, size, kind in bits:
            dist = speed * 0.4 * (1 - (1 - u) ** 2) / 2.2
            x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 6 * u * u
            fade = (1 - u) ** (1.2 if kind == "teal" else 1.8)
            if kind == "teal":
                glow(light, x, y, size, TEAL * 0.9, 1.3 * fade)
                glow(light, x, y, size * 0.5, (210, 255, 230), 0.8 * fade)
            else:
                for s in np.linspace(0, 1, 5):
                    back = dist * (1 - 0.18 * s)
                    glow(light, w / 2 + np.cos(ang) * back, w / 2 + np.sin(ang) * back + 6 * u * u, 0.6,
                         GOLD, 1.1 * fade * (1 - 0.7 * s))
        frames.append(additive_rgb(light))
    return frames


def build_chimera():
    res = run([("chimera", k, False) for k in range(16)] + [("chimera", k, True) for k in range(16)]
              + [("guns", k) for k in range(16)])
    body = artkit.quantize_set([bu.rim(f) for f in res[:16]], 40)
    save_set("chimera", body)
    seams = [seam_layer(g) for g in res[16:32]]
    save_set("chimera-seams", [additive_rgb(s) for s in seams])
    heal = []
    for s in seams:
        for ph in range(6):
            heal.append(additive_rgb(s * heal_ring(ph / 6)[..., None]))
    save_set("chimera-heal", heal)
    save_set("chimera-flecks", flecks())
    muzzles = []
    for g in res[32:48]:
        a = np.array(g).astype(float)
        mag = (a[..., 0] > 60) & (a[..., 2] > 60) & (a[..., 3] > 0)
        ys, xs = np.nonzero(mag)
        if len(xs) == 0:
            muzzles.append([[24, 24], [24, 24]])
            continue
        cx = xs.mean()
        muzzles.append([[round(float(xs[sel].mean()) + 0.5, 1), round(float(ys[sel].mean()) + 0.5, 1)]
                        for sel in (xs < cx, xs >= cx)])
    write_pivots("chimera", {"size": [TAL, TAL], "headings": 16, "muzzles": muzzles,
                             "note": "per heading the two gun muzzles (the pair's spawn and talon-glint's centre), "
                                     "[x, y] px from the frame's top left"})
    full = add(body[0], seams[0], SEAM_STATES["full"])
    print(f"chimera: teal {e72.teal_share(full) * 100:.0f} % at full health, "
          f"{e72.teal_share(add(body[0], seams[0], SEAM_STATES['heal-95'])) * 100:.0f} % healing near full")


def add(img, light, k):
    """A body with its seam light added at strength ``k`` (the teal-share measure)."""
    a = np.array(img).astype(np.float64)
    a[..., :3] = np.clip(a[..., :3] + np.asarray(light)[..., :3] * k, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== harvester

PLUME = (240, 64)
SCOOP_DX = 2                                 # the plume's origin: px from the hull's centre toward the nose


def plume_field(phase, flare=0.0):
    """The gas plume of a right-flying hull as (rgb, alpha) on the 240x64 canvas centred on the hull: from just
    behind the scoop it trails left, widening from 16 to 46 px and thinning out over ~115 px; its turbulence flows
    back along it (``phase`` 0..1 a loop). Shading by density only (no direction), so the left set mirrors it."""
    w, h = PLUME
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    u = np.clip((w / 2 + SCOOP_DX - xx) / 118.0, -0.2, 1.4)        # 0 at the origin, 1 at the far end (left)
    width = 9 + 17 * np.clip(u, 0, None) ** 0.7
    v = (yy - h / 2 - 2 - 3 * np.clip(u, 0, None)) / width
    tex = raster.fbm(w, h, 12, 3671, octaves=4, period=True)
    tex = np.roll(tex, int(round(phase * w)), axis=1)
    tex2 = np.roll(raster.fbm(w, h, 24, 3672, octaves=3, period=True), int(round(phase * w / 2)), axis=1)
    shape = np.exp(-v ** 2 * (1.6 + 0.8 * (tex2 - 0.5))) * smoothstep(-0.06, 0.04, u) * np.exp(-np.clip(u, 0, None) * 1.25)
    dens = np.clip(shape * (0.75 + 0.6 * (tex - 0.5)) * 1.15, 0, 1)
    alpha = np.clip(dens * 0.88, 0, 0.88) * smoothstep(0, 6, np.minimum(xx, w - 1 - xx))
    rgb = np.zeros((h, w, 3)) + np.array((232, 222, 204.0))
    rgb = rgb * (0.86 + 0.18 * dens[..., None])
    return rgb, alpha, dens


def flare_frames(side):
    """The plume flaring once along its length (warm white-gold, added) and the fire at the cut, 12 frames."""
    k_plume = [0.45, 1.0, 0.85, 0.62, 0.42, 0.28, 0.17, 0.1, 0.05, 0.02, 0.0, 0.0]
    k_fire = [0.6, 1.0, 0.9, 0.75, 0.6, 0.48, 0.36, 0.26, 0.17, 0.1, 0.05, 0.0]
    _, _, dens = plume_field(0.0)
    frames = []
    for f in range(12):
        light = (blur(dens, 1.5) * 255 * k_plume[f])[..., None] * np.array((1.0, 0.82, 0.5))
        cx = PLUME[0] / 2 + 2 * (1 if side == "right" else -1)
        glow(light, cx, PLUME[1] / 2, 7 + 3 * f / 11, (255, 140, 50), 1.2 * k_fire[f])
        glow(light, cx, PLUME[1] / 2, 3, (255, 230, 170), 1.0 * k_fire[f])
        light = light if side == "right" else light[:, ::-1]
        frames.append(additive_rgb(light))
    return frames


BREAK = (128, 96)
TILTS = (0.0, 0.26, 0.5)


def break_frames(side, halves, band=(176, 96, 58)):
    """12 frames: the two halves part along the cut (the front one forward, the rear one back), tip (the renders at
    the tilts) and sink into the band: shrinking 1.0 -> 0.5 and hazed toward the band's colour, fading out at the end;
    grey smoke rising from the cut."""
    w, h = BREAK
    fwd = 1 if side == "right" else -1
    rng = np.random.default_rng(3681)
    puffs = [(rng.uniform(-6, 6), rng.uniform(-4, 4), rng.uniform(4, 8), rng.uniform(0, 0.25)) for _ in range(7)]
    frames = []
    for f in range(12):
        u = f / 11
        tilt = min(2, f // 4)
        s = 1.0 - 0.55 * u ** 1.1
        haze = 0.7 * u ** 1.1
        fade = 1.0 - smoothstep(0.7, 1.0, u)
        canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        for part, sign in (("rear", -1), ("front", 1)):
            img = halves[(part, tilt)]
            iw, ih = max(1, round(img.width * s)), max(1, round(img.height * s))
            im = img.resize((iw, ih), Image.BOX)
            a = np.array(im).astype(np.float64)
            a[..., :3] = a[..., :3] * (1 - haze) + np.array(band, float) * haze
            a[..., 3] = np.where(a[..., 3] >= 128 * (1 if fade > 0.99 else 1), a[..., 3] * fade, 0)
            im = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            dx = sign * fwd * (3 + 26 * u ** 0.8)
            dy = (4 + 6 * (sign == 1)) * u * u
            canvas.alpha_composite(im, (int(round(w / 2 - iw / 2 + dx)), int(round(h / 2 - ih / 2 + dy))))
        smoke = np.zeros((h, w))
        for px, py, r, t0 in puffs:
            v = (u - t0) / (1 - t0)
            if v <= 0:
                continue
            x = w / 2 + px * s
            y = h / 2 + py - 22 * v
            rr = r * (0.6 + 1.4 * v)
            yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
            smoke = np.maximum(smoke, np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (rr * rr)) * 0.75 * (1 - v) ** 0.8)
        sm = rgba(np.zeros((h, w, 3)) + np.array((70, 62, 58.0)) * (0.9 + 0.2 * smoke[..., None]), smoke)
        canvas.alpha_composite(sm)
        frames.append(canvas)
    return frames


def build_harvester():
    jobs = [("harvester", side) for side in ("right", "left")]
    jobs += [("harvester", side, part, t, BREAK) for side in ("right", "left") for part in ("front", "rear")
             for t in TILTS]
    res = run(jobs)
    bodies = artkit.quantize_set([bu.rim(r) for r in res[:2]], 40)
    save_set("harvester", [bodies[0]], single=True)
    save_set("harvester-left", [bodies[1]], single=True)
    for side, name in (("right", "harvester-plume"), ("left", "harvester-left-plume")):
        frames = []
        for f in range(4):
            rgb, alpha, _ = plume_field(f / 4)
            if side == "left":
                rgb, alpha = rgb[:, ::-1], alpha[:, ::-1]
            frames.append(rgba(rgb, alpha))
        save_set(name, frames)
    i = 2
    for side, prefix in (("right", "harvester"), ("left", "harvester-left")):
        halves = {}
        for part in ("front", "rear"):
            for t in range(len(TILTS)):
                halves[(part, t)] = bu.rim(res[i])
                i += 1
        frames = break_frames(side, halves)
        save_set(f"{prefix}-break", frames)
        save_set(f"{prefix}-flare", flare_frames(side))


# =========================================================================== fleet Stormhawk, pod

def trail(w=16, h=48):
    frames = []
    for f in range(2):
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
        light = np.zeros((h, w, 3))
        for sx in (-3.0, 3.0):
            core = np.exp(-((xx - w / 2 - sx) / (1.1 + yy / h)) ** 2) * np.exp(-yy / (20 + 6 * f))
            light += core[..., None] * np.array((150, 200, 255.0)) * (1.3 - 0.2 * f)
            light += (np.exp(-((xx - w / 2 - sx) / 0.6) ** 2) * np.exp(-yy / 7))[..., None] * np.array((235, 245, 255.0))
        frames.append(additive_rgb(light))
    return frames


def build_fleet():
    res = run([("fleet", k) for k in range(16)])
    save_set("fleet-stormhawk", artkit.quantize_set(res, 40))
    save_set("fleet-stormhawk-trail", trail())


def pod_break(n=8, w=64, seed=3691):
    rng = np.random.default_rng(seed)
    chips = [(rng.uniform(0, TAU), rng.uniform(40, 90), rng.uniform(2, 4.5), rng.uniform(0, TAU)) for _ in range(11)]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
        yy, xx = np.mgrid[0:w, 0:w].astype(np.float64)
        r = 6 + 16 * u ** 0.7
        smoke = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2 + 10 * u) ** 2) / (r * r)) * 0.8 * (1 - u) ** 0.9
        tex = raster.fbm(w, w, 8, seed + f, octaves=3, period=False)
        smoke = np.clip(smoke * (0.7 + 0.6 * tex), 0, 1)
        hot = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2) ** 2) / (5 + 6 * u) ** 2) * max(0.0, 1 - 2.6 * u)
        rgb = np.array((92, 86, 82.0)) * (1 - hot[..., None]) + np.array((255, 160, 60.0)) * hot[..., None]
        img.alpha_composite(rgba(rgb, np.clip(smoke + hot, 0, 1)))
        d = ImageDraw.Draw(img)
        for ang, speed, size, spin in chips:
            dist = speed * 0.32 * (1 - (1 - u) ** 2)
            x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 14 * u * u
            a = spin + u * 7
            pts = [(x + np.cos(a + k * 2.1) * size, y + np.sin(a + k * 2.1) * size * 0.7) for k in range(3)]
            lit = 0.5 + 0.5 * np.cos(a + 0.8)
            col = tuple(int(c) for c in np.array((90, 94, 102)) + np.array((110, 110, 108)) * lit)
            if u < 0.92:
                d.polygon(pts, fill=col + (255,))
        frames.append(img)
    return frames


def build_pod():
    res = run([("pod", k) for k in range(16)])
    frames = artkit.quantize_set(res, 32)
    save_set("debris-pathfinder-pod", frames)
    save_set("debris-pathfinder-pod-break", pod_break())


BUILDERS = {"chimera": build_chimera, "harvester": build_harvester, "fleet": build_fleet, "pod": build_pod}


# =========================================================================== review

def plate(t):
    import backdrop_l36 as bd  # noqa: E402  (the production backdrop's tiles)
    img = bd.layer_tiles("deep", t)
    img.alpha_composite(bd.layer_tiles("low-air", t))
    return img


def paste(dst, img, x, y):
    return l23kit.paste_c(dst, img, x, y)


def addc(dst, img, x, y, k=1.0):
    return l23kit.add_c(dst, img, x, y, k)


def review_chimera():
    body, seams, heal, fl = (l23kit.load(n) for n in ("chimera", "chimera-seams", "chimera-heal", "chimera-flecks"))
    talon = l23kit.load("talon")
    boom = artkit.load_frames("explosion-small")
    bg = plate(50.0)
    sheet = raster.sheet(1000, 860, "CHIMERA (FINAL R74): THE HYBRID TALON THAT HEALS",
                         f"PRODUCTION ART, {BATCH.upper()} - R74")
    raster.draw_text(sheet, 16, 38, "16 HEADINGS AT 2X, THE SEAMS AT FULL HEALTH (0.45), OVER LEVEL 36'S CREAM BANDS",
                     raster.LABEL)
    for k in range(16):
        c = bg.crop((k * 28, 40, k * 28 + 56, 96)).copy()
        c = addc(paste(c, body[k], 28, 28), seams[k], 28, 28, SEAM_STATES["full"])
        sheet.alpha_composite(c.resize((112, 112), Image.NEAREST), (16 + (k % 8) * 120, 54 + (k // 8) * 120))
    y = 54 + 2 * 120 + 8
    raster.draw_text(sheet, 16, y, "THE SEAMS BY STATE AT 3X (HEADING 0): JUST HIT 0.13 / FULL 0.45 / HEALING 50 % 0.65 / "
                                   "HEALING 95 % 0.97 / THE PULSE (PHASES 0-5 OVER 0.5 S)", raster.LABEL)
    x = 16
    for lab, k in SEAM_STATES.items():
        c = bg.crop((0, 200, 56, 256)).copy()
        c = addc(paste(c, body[0], 28, 28), seams[0], 28, 28, k)
        sheet.alpha_composite(c.resize((168, 168), Image.NEAREST), (x, y + 16))
        raster.draw_text(sheet, x, y + 188, lab.upper(), raster.LABEL_DIM)
        x += 176
    for ph in range(6):
        c = bg.crop((0, 200, 56, 256)).copy()
        c = addc(addc(paste(c, body[0], 28, 28), seams[0], 28, 28, 0.6), heal[ph], 28, 28)
        sheet.alpha_composite(c.resize((56, 56), Image.NEAREST), (x + (ph % 2) * 60, y + 16 + (ph // 2) * 60))
    y += 210
    raster.draw_text(sheet, 16, y, "BESIDE THE ACT 5 TALON (2X), THE DEATH: THE SMALL EXPLOSION WITH THE FLECKS "
                                   "(FRAMES 0-7, 1X)", raster.LABEL)
    c = bg.crop((0, 300, 112, 356)).copy()
    c = paste(c, talon[0], 28, 28)
    c = addc(paste(c, body[0], 84, 28), seams[0], 84, 28, 0.45)
    sheet.alpha_composite(c.resize((224, 112), Image.NEAREST), (16, y + 16))
    for f in range(8):
        c = bg.crop((0, 300, 64, 364)).copy()
        if f < len(boom):
            c = addc(c, boom[min(len(boom) - 1, f + 1)], 32, 32)
        c = addc(c, fl[f], 32, 32)
        sheet.alpha_composite(c.resize((96, 96), Image.NEAREST), (260 + f * 90, y + 16))
    y += 140
    lines = ["THE MODEL IS ROUND 72'S B 'OVERGROWN' (ENEMIES_R72.PY, UNCHANGED) ON THE PRODUCTION TALON'S AIRFRAME AT ITS "
             "48 PX; THE SEAMS ARE ONE ADDITIVE LAYER THE GAME DRAWS AT THE STATE'S STRENGTH, THE PULSE A SECOND LAYER "
             "WHILE IT HEALS (TWO PULSES A SECOND). EVERY GLOW SMOOTH (NO DITHER).",
             "UNDER A BANK THE BODY SHOWS AT ~35 %; THE SEAMS AND THE PULSE ARE DRAWN AGAIN AT FULL ABOVE THE BANK "
             "(THE TELLS RULE), AS THE TALON'S RED GLINT."]
    for i, line in enumerate(lines):
        for j, part in enumerate(wrap(line, 150)):
            raster.draw_text(sheet, 16, y + (i * 3 + j) * 13, part, raster.LABEL_DIM)
    # the GIF: a V of five swooping over the bands; scattered hits on #1 (it heals), a held stream kills #3
    frames = []
    hp = [1.0] * 5
    last = [-9.0] * 5
    dead = {}
    for i in range(80):
        t = i / 10
        img = plate(50.0 + t)
        for n, (lx, ly) in enumerate([(0, 0), (-34, -30), (34, -30), (-68, -60), (68, -60)]):
            def pos(tt, lx=lx, ly=ly):
                return (240 + lx + 60 * np.sin(tt * 0.7), 120 + ly + 120 * np.sin(min(tt, 6.0) / 6.0 * np.pi * 0.9))
            x, y = pos(t)
            x2, y2 = pos(t + 0.05)
            ang = np.arctan2(y2 - y, x2 - x + 1e-9)                   # screen angle of travel (0 = right, y down)
            k = int(round(((ang - np.pi / 2) % TAU) / TAU * 16)) % 16  # clockwise from straight down
            if n == 1 and (1.0 < t < 1.6 or 2.4 < t < 2.7):
                hp[n], last[n] = max(0.3, hp[n] - 0.06), t
            if n == 3 and 4.0 < t:
                hp[n], last[n] = hp[n] - 0.05, t
                if hp[n] <= 0 and n not in dead:
                    dead[n] = t
            if n in dead:
                u = int((t - dead[n]) * 20)
                if u < 12:
                    img = addc(img, boom[min(u, len(boom) - 1)], x, y)
                if u // 1 < 8 * 3:
                    img = addc(img, fl[min(7, u // 3)], x, y)
                continue
            since = t - last[n]
            if since > 1.0 and hp[n] < 1.0:
                hp[n] = min(1.0, hp[n] + 0.2 / 10)
                strength = 0.3 + 0.7 * hp[n]
                img = addc(addc(paste(img, body[k], x, y), seams[k], x, y, strength), heal[k * 6 + i % 6], x, y)
            else:
                strength = 0.08 + 0.1 * hp[n] if since < 1.0 else 0.45
                img = addc(paste(img, body[k], x, y), seams[k], x, y, strength)
        frames.append(img.crop((0, 0, 480, 400)))
    artkit.save_review(sheet, frames, CONCEPT["chimera"], "chimera", fps=10)
    print("review: chimera-final-r74-a")


def wrap(s, n):
    words, out, cur = s.split(), [], ""
    for wd in words:
        if len(cur) + len(wd) + 1 > n:
            out.append(cur)
            cur = wd
        else:
            cur = (cur + " " + wd).strip()
    return out + [cur]


def review_harvester():
    hv, hl = l23kit.load("harvester"), l23kit.load("harvester-left")
    pv, pl = l23kit.load("harvester-plume"), l23kit.load("harvester-left-plume")
    br, bl = l23kit.load("harvester-break"), l23kit.load("harvester-left-break")
    fr, flf = l23kit.load("harvester-flare"), l23kit.load("harvester-left-flare")
    boom = artkit.load_frames("explosion-medium")
    talon = l23kit.load("talon")
    bg = plate(100.0)
    sheet = raster.sheet(1000, 800, "HELIUM-3 HARVESTER (FINAL R74): LEVEL 36'S SECONDARY",
                         f"PRODUCTION ART, {BATCH.upper()} - R74")
    raster.draw_text(sheet, 16, 38, "THE HULLS AT 2X (FLYING RIGHT, FLYING LEFT: TWO RENDERS) WITH THEIR PLUMES, OVER "
                                    "THE RUST BAND (QB-1 A: GUNMETAL, GOLD TRIM, RED LIGHTS, THE SCOOP)", raster.LABEL)
    for j, (b, p) in enumerate(((hv, pv), (hl, pl))):
        c = bg.crop((0, 60 + j * 70, 240, 124 + j * 70)).copy()
        c = paste(paste(c, p[0], 120, 32), b[0], 120, 32)
        sheet.alpha_composite(c.resize((480, 128), Image.NEAREST), (16 + j * 488, 54))
    y = 54 + 128 + 10
    raster.draw_text(sheet, 16, y, "THE PLUME'S FLOW (4 FRAMES, 1X) AND THE BREAK: THE HULL PARTS ALONG A JAGGED CUT, THE "
                                   "HALVES TIP AND SINK INTO THE BAND, THE PLUME FLARES ONCE (FRAMES 0-11, 1X)",
                     raster.LABEL)
    for f in range(4):
        c = bg.crop((0, 200, 240, 264)).copy()
        c = paste(paste(c, pv[f], 120, 32), hv[0], 120, 32)
        sheet.alpha_composite(c, (16 + (f % 4) * 244, y + 16))
    y += 90
    for f in range(12):
        c = bg.crop((120, 260, 360, 356)).copy()
        c = addc(paste(c, br[f], 120, 48), fr[f], 120, 48)
        if f < len(boom):
            c = addc(c, boom[min(len(boom) - 1, f)], 120, 48, 0.8)
        sheet.alpha_composite(c.crop((40, 0, 200, 96)), (16 + (f % 6) * 164, y + 16 + (f // 6) * 102))
    y += 230
    raster.draw_text(sheet, 16, y, "A PAIR WITH ITS TWO TALON ESCORTS (ORBIT 90 PX ROUND THE PAIR'S MIDDLE) AT 1X; UNDER A "
                                   "BANK (THE BODIES THROUGH THE VEIL)", raster.LABEL)
    scene = plate(104.0)
    for side, b, p in ((-1, hl, pl), (1, hv, pv)):
        scene = paste(paste(scene, p[0], 240 + side * 80, 200), b[0], 240 + side * 80, 200)
    for a in (0.3, 3.4):
        scene = paste(scene, talon[(int(a / TAU * 16) + 4) % 16], 240 + np.cos(a) * 90, 200 + np.sin(a) * 90)
    sheet.alpha_composite(scene.crop((0, 60, 480, 340)), (16, y + 16))
    import backdrop_l36 as bd  # noqa: E402
    bank = Image.open(bd.OUT / "bank-e.png").convert("RGBA")
    s2 = plate(110.0)
    s2 = paste(paste(s2, pv[1], 160, 150), hv[0], 160, 150)
    s2 = paste(s2, bank, 200, 150)
    sheet.alpha_composite(s2.crop((0, 10, 480, 290)), (504, y + 16))
    frames = []
    for i in range(70):
        t = i / 10
        img = plate(100.0 + t)
        for side, b, p, bk, fk in ((-1, hl, pl, bl, flf), (1, hv, pv, br, fr)):
            x, y_ = 240 + side * 80 + side * 30 * t * 0.6, 40 + 50 * t
            if side == -1 and t > 4.0:
                f = int((t - 4.0) * 15)
                if f < 12:
                    img = addc(paste(img, bk[f], x, y_), fk[f], x, y_)
                    if f < len(boom):
                        img = addc(img, boom[f], x, y_, 0.8)
                continue
            img = paste(paste(img, p[i % 4], x, y_), b[0], x, y_)
        frames.append(img.crop((0, 0, 480, 420)))
    artkit.save_review(sheet, frames, CONCEPT["harvester"], "harvester", fps=10)
    print("review: harvester-final-r74-a")


def review_fleet():
    fl, tr = l23kit.load("fleet-stormhawk"), l23kit.load("fleet-stormhawk-trail")
    ship = artkit.load_frames("ship")
    bg = plate(4.0)
    sheet = raster.sheet(1000, 470, "FLEET STORMHAWK (FINAL R74): THE DROP CRAFT, MARKED FRIENDLY",
                         f"PRODUCTION ART, {BATCH.upper()} - R74")
    raster.draw_text(sheet, 16, 38, "16 HEADINGS AT 2X (CDF GREY, THE BLUE BAND, WHITE WING BARS) AND THE PLAYER'S "
                                    "STORMHAWK FOR COMPARISON; THE DIVE AT 1X: TWO PAIRS UP THE SCREEN WITH THEIR TRAILS",
                     raster.LABEL)
    for k in range(16):
        c = bg.crop((k * 24, 300, k * 24 + 44, 344)).copy()
        c = paste(c, fl[k], 22, 22)
        sheet.alpha_composite(c.resize((88, 88), Image.NEAREST), (16 + (k % 8) * 96, 54 + (k // 8) * 96))
    c = bg.crop((0, 300, 52, 352)).copy()
    sheet.alpha_composite(paste(c, ship[2], 26, 26).resize((104, 104), Image.NEAREST), (16 + 8 * 96 + 20, 54))
    raster.draw_text(sheet, 16 + 8 * 96 + 20, 162, "THE PLAYER", raster.LABEL_DIM)
    frames = []
    for i in range(30):
        t = i / 20
        img = plate(4.0 + t)
        for x0, dt in ((180, 0.0), (220, 0.08), (300, 0.5), (340, 0.58)):
            y = 560 - 400 * (t - dt)
            if -40 < y < 600:
                img = addc(img, tr[i % 2], x0, y + 12 + 24)
                img = paste(img, fl[8], x0, y)
        img = paste(img, ship[2], 240, 470)
        frames.append(img)
    for j, f in enumerate((frames[6], frames[14])):
        sheet.alpha_composite(f.resize((240, 270), Image.BOX).crop((0, 0, 240, 200)), (16 + j * 250, 250))
    artkit.save_review(sheet, frames, CONCEPT["fleet"], "fleet-stormhawk", fps=20)
    print("review: fleet-stormhawk-final-r74-a")


def review_pod():
    pod, br = l23kit.load("debris-pathfinder-pod"), l23kit.load("debris-pathfinder-pod-break")
    bg = plate(146.0)
    sheet = raster.sheet(1000, 330, "PATHFINDER POD (FINAL R74): LEVEL 36'S SECRET",
                         f"PRODUCTION ART, {BATCH.upper()} - R74")
    raster.draw_text(sheet, 16, 38, "ITS TUMBLE (16 FRAMES, 2X) AND THE BREAK IN AIR (8 FRAMES, 2X): SHELL CHIPS, A PUFF "
                                    "OF SMOKE RISING; THE CRATE IS THE GAME'S", raster.LABEL)
    for k in range(16):
        c = bg.crop((k * 30, 200, k * 30 + 40, 240)).copy()
        sheet.alpha_composite(paste(c, pod[k], 20, 20).resize((80, 80), Image.NEAREST), (16 + (k % 8) * 86, 54 + (k // 8) * 86))
    for f in range(8):
        c = bg.crop((f * 50, 300, f * 50 + 64, 364)).copy()
        sheet.alpha_composite(paste(c, br[f], 32, 32).resize((112, 112), Image.NEAREST), (16 + f * 120, 54 + 180))
    frames = []
    for i in range(50):
        t = i / 10
        img = plate(146.0 + t)
        x, y = 400 - 6 * t, -20 + 90 * t
        if t < 3.5:
            img = paste(img, pod[i % 16], x, y)
        else:
            f = int((t - 3.5) * 10)
            if f < 8:
                img = paste(img, br[f], 400 - 21, -20 + 315)
        frames.append(img)
    artkit.save_review(sheet, frames, CONCEPT["pod"], "pathfinder-pod", fps=10)
    print("review: pathfinder-pod-final-r74-a")


REVIEWS = {"chimera": review_chimera, "harvester": review_harvester, "fleet": review_fleet, "pod": review_pod}

SETS = {"chimera": [("chimera", 16, (48, 48)), ("chimera-seams", 16, (48, 48)), ("chimera-heal", 96, (48, 48)),
                    ("chimera-flecks", 8, (64, 64))],
        "harvester": [("harvester", 1, HV), ("harvester-left", 1, HV), ("harvester-plume", 4, PLUME),
                      ("harvester-left-plume", 4, PLUME), ("harvester-break", 12, BREAK),
                      ("harvester-left-break", 12, BREAK), ("harvester-flare", 12, PLUME),
                      ("harvester-left-flare", 12, PLUME)],
        "fleet": [("fleet-stormhawk", 16, (40, 40)), ("fleet-stormhawk-trail", 2, (16, 48))],
        "pod": [("debris-pathfinder-pod", 16, (32, 32)), ("debris-pathfinder-pod-break", 8, (64, 64))]}


def check():
    import backdrop_l36 as bd  # noqa: E402  (its checker measure)
    bad = 0
    for subject, sets in SETS.items():
        for name, n, size in sets:
            where = l23kit.where(name)
            try:
                frames = l23kit.load(name)
            except FileNotFoundError:
                frames = []
            folder = ROOT / where
            files = sorted(folder.glob(f"{name}*.png"))
            files = [f for f in files if f.stem == name or (f.stem.rsplit("_", 1)[0] == name and
                                                             f.stem.rsplit("_", 1)[-1].isdigit())]
            src_ok = all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
            vis = chk = 0
            for f in files:
                v, c = bd.checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src_ok and share < 0.05
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %")
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
