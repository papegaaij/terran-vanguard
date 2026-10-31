#!/usr/bin/env python3
"""Production art: Level 38's units and objects (M10 part D batch, concept round 76, step ART-D): the Harrow
(design/enemies/air/harrow, round 72 **A.3 a** "dart and sac"), Aurelia's cargo pod (Level 38's secret) and the CDF
supply canister on its parachute (Level 38's armour patch).

The Harrow is round 72's model (tools/concept/enemies_r72.py ``harrow_model`` a, imported unchanged): a short gunmetal
dart with gold leading edges and swept fins, a swollen sac of dark teal tissue on its back behind the nose, its veins
glowing; rendered at the concept's 40 px canvas and 2.3 model units across (17.4 px a unit: the dart 32 px long, the
README's 34x34) with the Q.11 b rim. Its sac glow is an emission-only render of the tissue (``glow_only``) at strength 1,
drawn additively over the body at a strength the game sets from the state (the concept's ``add_glow`` k): **dim 0.2**
(EMP-stunned), **hunting 0.45-0.75** (a slow pulse, once a second: 0.6 + 0.15 sin; teal 5-10 % of the silhouette, Q9 a's
5-12 %), **the lock tell 0.75 -> 1.0** over the tell (plus the halo and its fast pulse in ``harrow-tell``), **ramming
0.9**. The tell is a second additive layer over it, played over
the tell's length (0.6 s; easy 0.8, hard 0.5: the frame is the tell's progress x 12). The shell (the dart without its
sac, the cradle scorched) is the death's tumbling body; the burst is the teal flash and the tissue flecks over the
small explosion; the ring of 8 is the game's ``small`` bullets.

Aurelia's cargo pod and the CDF canister are new models on the frozen render package (Level 38's README, *Secrets and
pickups*; no round-72 proposal): the pod in Aurelia's livery a step tarnished and sooty, its lamps dead (dark against
the storm: the README's "dark against the dark grade"), with the lightning flash's blue-white outline as an additive set;
the canister in the CDF's grey with the blue band and orange end stripes (the pathfinder pod's family) under a white and
CDF-blue parachute that trails up the screen, the latch (the 10 HP target) a **separate overlay** so round 76 QD-4 can
drop it (b: the canister opens by itself; the overlay is then simply not drawn).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its own
render under the fixed key light. Glows are smooth float fields written unquantised (no ordered dither, far more than
32 steps); additive sets are premultiplied on black (GL_SRC_ALPHA, GL_ONE).

Outputs (build/art-held/sprites/ until the data claims the root - D2 writes the Harrow's unit data and Level 38's
debris chunks ``cargo-pod`` and ``canister``; rerun then and the files move to assets/sprites/):
  harrow_0..15               40x40, 16 headings: the gunmetal dart with gold leading edges, the sac's tissue dark (40
                             colours); drawn under the sac glow
  harrow-sac_0..15           40x40 additive: the sac's vein glow at strength 1 (teal 00FF9A), over the body at the
                             state's strength (above)
  harrow-tell_0..11          48x48 additive, centred on the sac (pivots ``sac``): the lock tell's halo, brightening over
                             the tell with three fast pulses, the last two frames the lock's flare
  harrow-burst_0..7          64x64 additive, centred on the sac: the death's teal flash and the tissue flecks flung out,
                             3 steps a frame (0.4 s), over the small explosion
  harrow-shell_0..11         40x40 normal: the dart's shell without its sac (the cradle torn and scorched, the engine
                             dead) tumbling, one turn in 12 frames at 12 fps; the game drifts it on along the old heading
                             and fades it over ~1 s (frame 0 = heading 0: the burst's flash covers the switch)
  pivots/harrow.json         per heading the sac's centre (the tell's and the burst's place) and the nose tip, [x, y] px
                             from the top left; the ring's origin is the unit's centre (the sac sits 1.4 px behind it)
  debris-cargo-pod_0..15     36x36: Aurelia's cargo pod (tarnished gold and soot-darkened cream panels, gold bands, a
                             torn cable stub, dead lamps), its slow tumble (frame k = k x 22.5 degrees clockwise)
  debris-cargo-pod-outline_0..15  36x36 additive: the lightning flash's blue-white outline of each tumble frame (the
                             README's 0.5 s: the game fades it from the flash)
  debris-cargo-pod-break_0..7  64x64 normal: the pod broken open in air: gold shell chips flung out, a hot core, a puff
                             of smoke rising and thinning, 2 steps a frame (the crate is the game's)
  debris-canister_0..7       64x128 normal, centred on the canister: the CDF supply canister under its parachute (white
                             and CDF-blue gores, six risers), the rig swaying +-9 degrees under the canopy, 8 frames at
                             4 fps (a 2 s loop)
  debris-canister-latch_0..7 64x128 normal overlay, the same frames: the latch on the canister's top (an orange clamp
                             ring, an amber lamp): the 10 HP target; QD-4 b leaves it out
  debris-canister-release_0..9  64x128 normal, from sway frame 0: the latch shot (a spark), the lid halves swing open and
                             fall away, the canopy torn loose drifts up the screen shrinking and fading, a puff; 10 fps
                             (1 s; the armour patch is the game's)
  assets/sprites/intel/harrow.png  30x30, the hangar intel's sensor-L2 portrait (tools/art/intel.py's card: the plate,
                             the scan tint): the Harrow nose down, the sac's veins unlit. Written here through intel.py's
                             own ``portrait`` (registered at run time) while another step edits intel.py; folding it into
                             intel.py's ``HI``/``BATCHES``/``ROUNDS`` later gives the same pixels
Review (round 76): design/enemies/air/harrow/concept/harrow-final-r76-a.png/.gif; Level 38's concept/
cargo-pod-final-r76-a and canister-final-r76-a (.png/.gif), on Level 38's production backdrop (tools/art/
backdrop_l38.py's tiles, when built; a flat storm plate otherwise); the portrait on design/ui/hangar/concept/
intel-final-r76-a.png (intel.py's review sheet).

Run: python3 tools/art/storm_layer_units.py [harrow] [pod] [canister] [intel] [--review] [--check]
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

import enemies_r72 as e72  # noqa: E402  (concept: the Harrow's model, imported unchanged)
from render import raster  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GOLD, A_HULL  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_plate, sd_sphere, union)  # noqa: E402

SCRIPT = "storm_layer_units.py"
BATCH = "M10 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r76"
artkit.REVIEW_ROUND = ROUND
L38 = DESIGN / "campaign" / "act-6-jovian-storm" / "level-38-lightning-belt" / "concept"
CONCEPT = {"harrow": DESIGN / "enemies" / "air" / "harrow" / "concept", "pod": L38, "canister": L38}
PX = 20.0
HAR, HAR_EXT = 40, 2.3                       # the concept's 40 px canvas, 2.3 units across (HAR_N / HAR_SCALE)
HAR_PX = HAR / HAR_EXT
SAC_Y = -0.08                                # the sac's centre on the model's axis (harrow_model a's ellipsoid)
NOSE_Y = 1.0
TELL, BURST = 48, 64
TEAL = np.array((0, 255, 154), float)
GOLD = np.array((255, 196, 64), float)
SAC_STATES = {"dim": 0.2, "hunt-low": 0.45, "hunt-high": 0.75, "tell": 0.75, "tell-peak": 1.0, "ram": 0.9}
HUNT = 0.6


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


def rotate_axis(p, axis, ang):
    """Points ``p`` (N x 3) turned by ``ang`` about the unit ``axis`` (Rodrigues)."""
    k = np.asarray(axis, float)
    k = k / np.linalg.norm(k)
    c, s = np.cos(ang), np.sin(ang)
    return p * c + np.cross(k, p) * s + np.outer(p @ k, k) * (1 - c)


def heading_dir(k, n=16):
    """The screen direction (x right, y down) of heading k: k x 360/n degrees clockwise from straight down."""
    th = TAU * k / n
    return np.array((-np.sin(th), np.cos(th)))


# =========================================================================== models

A_SOOT = 7                                    # the shell's scorched cradle (an extra material slot)


def shell_model(tumble=0.0):
    """The Harrow's dart without its sac (harrow_model a's plate, fins and engine): the cradle where the sac sat torn
    and scorched, the engine dead; turned by ``tumble`` radians about a tilted axis (the death's tumble)."""
    body = [(0.0, 1.0), (0.16, 0.45), (0.56, -0.52), (0.44, -0.66), (0.16, -0.44), (0.12, -0.82), (0.0, -0.74)]
    roof = lambda x, y: np.clip(1.0 - 3.0 * np.abs(x), 0.3, 1.0)  # noqa: E731

    def scene(p):
        if tumble:
            p = rotate_axis(p, (0.8, 0.35, 0.5), -tumble)
        q = mirror_x(p)
        d, m = union((sd_plate(q, body, 0.0, 0.12, 0.0, taper=roof), A_HULL),
                     (e72.sd_cylinder_y(q, (0.07, -0.78, 0.0), 0.06, 0.05), A_ENGINE))
        lead = e72.seg_dist2(q, [(0.0, 1.0, 0.16, 0.45), (0.16, 0.45, 0.56, -0.52)]) < 0.075
        m = np.where((m == A_HULL) & lead, A_GOLD, m)
        m = np.where((m == A_HULL) & (q[:, 1] < -0.4) & (q[:, 0] > 0.2), A_FACET, m)
        cradle = (q[:, 0] / 0.3) ** 2 + ((q[:, 1] - SAC_Y) / 0.46) ** 2 < 1.0
        m = np.where(cradle & (m != A_GOLD), A_SOOT, m)
        return d, m
    mats = e72.regular_mats("c8202a", 1.0)
    mats[A_ENGINE] = Material((0.07, 0.07, 0.08), metal=0.4, shininess=20, spec=0.2)
    mats = mats + [Material((0.1, 0.075, 0.07), metal=0.1, shininess=12, spec=0.12)]
    return scene, mats


POD, POD_EXT = 36, 36 / PX
POD_LAMP = 6


def pod_model():
    """Aurelia's cargo pod, its long axis +Y: an octagonal-section container (Aurelia's octagons) in tarnished gold
    with soot-darkened cream side panels, three gold bands, a torn cable stub at one end, two lamp lenses dead."""
    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        q = np.stack([np.abs(x), y, z], axis=-1)
        r = np.maximum(np.maximum(np.abs(x), np.abs(z)), (np.abs(x) + np.abs(z)) * 0.7071)
        body = np.maximum(r - 0.36, np.abs(y) - 0.6)
        body = np.maximum(body, (r + np.abs(y)) - 0.86)                   # chamfered ends
        cap = sd_sphere(p, (0, 0.62, 0.0), 0.15)
        stub = sd_capsule(p, (0, 0.62, 0.05), (0.08, 0.92, 0.12), 0.05, 0.035)
        lamps = sd_sphere(q, (0.26, -0.5, 0.2), 0.055)
        d, m = union((body, 0), (cap, 1), k=0.05)
        d, m = union((d, m), (stub, 4), k=0.02)
        d, m = union((d, m), (lamps, POD_LAMP), k=0.0)
        band = (np.abs(np.abs(y) - 0.46) < 0.06) | (np.abs(y) < 0.05)
        m = np.where((m == 0) & band, 1, m)
        panel = (m == 0) & (np.abs(z) < 0.22) & (np.abs(x) > 0.3)
        m = np.where(panel, 2, m)
        top = (m == 0) & (z > 0.3) & (np.abs(x) < 0.12) & (np.abs(y) < 0.4)
        m = np.where(top, 3, m)
        return d, m
    mats = [Material((0.40, 0.31, 0.15), metal=0.75, shininess=60, spec=0.6),      # tarnished gold body
            Material((0.62, 0.48, 0.20), metal=0.85, shininess=90, spec=0.9),      # the gold bands, the end cap
            Material((0.36, 0.33, 0.27), metal=0.1, shininess=30, spec=0.3),       # soot-darkened cream panels
            Material((0.18, 0.15, 0.12), metal=0.3, shininess=20, spec=0.2),       # the spine's dark seam
            Material((0.12, 0.11, 0.11), metal=0.6, shininess=40, spec=0.4),       # the torn cable stub
            Material((0.0, 0.0, 0.0)),
            Material((0.10, 0.09, 0.08), metal=0.2, shininess=160, spec=1.1)]      # dead lamp lenses
    return scene, mats


CAN = (64, 128)
CAN_EXT = CAN[0] / PX
CHUTE_AXIS = np.array((0.0, 0.75, 0.66))
CHUTE_AXIS = CHUTE_AXIS / np.linalg.norm(CHUTE_AXIS)
CHUTE_L, CHUTE_R = 2.25, 1.22               # the load to the canopy's centre, the canopy's radius (model units)
SWAY = np.radians(9.0)
C_GREY, C_DARK, C_BLUE, C_ORANGE, C_WHITE, C_CBLUE, C_LINE, C_LATCH, C_LAMP = range(9)


def canister_model(sway=0.0, part="all"):
    """The CDF supply canister (a grey capsule, the blue band, orange end stripes, a white panel) under its parachute:
    the canopy a shallow dome of white and CDF-blue gores whose axis trails up the screen and toward the camera
    (``CHUTE_AXIS``), six risers from the canopy's rim to the canister's top; the whole rig turned ``sway`` radians
    about the canopy's centre (the pendulum). ``part``: 'all', 'rig' (no latch), 'latch' (the latch only),
    'canister', 'chute' (canopy and risers)."""
    top = np.array((0.0, 0.42, 0.0))
    centre = top + CHUTE_AXIS * CHUTE_L
    u = np.cross(CHUTE_AXIS, (1.0, 0.0, 0.0))
    u = u / np.linalg.norm(u)
    v = np.cross(CHUTE_AXIS, u)
    rim_r = CHUTE_R * 0.92
    rim = [centre - CHUTE_AXIS * 0.35 + rim_r * (np.cos(a) * u + np.sin(a) * v)
           for a in np.linspace(0, TAU, 6, endpoint=False) + 0.26]

    def scene(p):
        if sway:
            p = rotate_axis(p - centre, (0, 0, 1), -sway) + centre
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        parts = []
        if part in ("all", "rig", "canister"):
            body = sd_capsule(p, (0, -0.4, 0.0), (0, 0.36, 0.0), 0.3, 0.3)
            fins = sd_box(p, (0, -0.5, 0.0), (0.4, 0.07, 0.03), 0.02)
            d, m = union((body, C_GREY), (fins, C_DARK), k=0.04)
            m = np.where((m == C_GREY) & (np.abs(y + 0.02) < 0.08), C_BLUE, m)
            m = np.where((m == C_GREY) & (np.abs(np.abs(y + 0.02) - 0.5) < 0.07), C_ORANGE, m)
            m = np.where((m == C_GREY) & (z > 0.2) & (np.abs(x) < 0.12) & (np.abs(y - 0.22) < 0.1), C_WHITE, m)
            parts.append((d, m))
        if part in ("all", "rig", "chute"):
            w = p - centre
            along = w @ CHUTE_AXIS
            dist = np.linalg.norm(w, axis=1)
            shell = np.maximum(np.abs(dist - CHUTE_R) - 0.035, -(along + 0.35))
            az = np.arctan2(w @ v, w @ u)
            gore = np.where((np.floor((az + np.pi) / (TAU / 12)) % 2) == 0, C_WHITE, C_CBLUE)
            parts.append((shell, gore))
            for r in rim:
                parts.append((sd_capsule(p, tuple(top + (0, 0.04, 0.02)), tuple(r), 0.022, 0.022),
                              np.full(len(p), C_LINE)))
        if part in ("all", "latch"):
            lat = sd_capsule(p, (-0.16, 0.44, 0.0), (0.16, 0.44, 0.0), 0.075, 0.075)
            clamp = sd_box(p, (0, 0.5, 0.0), (0.06, 0.06, 0.06), 0.02)
            lamp = sd_sphere(p, (0.0, 0.47, 0.1), 0.045)
            d, m = union((lat, C_LATCH), (clamp, C_LATCH), k=0.03)
            d, m = union((d, m), (lamp, C_LAMP), k=0.0)
            parts.append((d, m))
        return union(*parts, k=0.0) if len(parts) > 1 else parts[0]
    mats = [Material((0.62, 0.64, 0.68), metal=0.35, shininess=60, spec=0.55),
            Material((0.3, 0.32, 0.36), metal=0.5, shininess=30, spec=0.35),
            Material((0.16, 0.40, 0.90), metal=0.3, shininess=50, spec=0.5),
            Material((0.95, 0.52, 0.12), metal=0.2, shininess=40, spec=0.4),
            Material((0.92, 0.92, 0.94), metal=0.0, shininess=20, spec=0.2),
            Material((0.20, 0.42, 0.86), metal=0.0, shininess=20, spec=0.2),
            Material((0.55, 0.55, 0.58), metal=0.0, shininess=10, spec=0.1),
            Material((0.95, 0.42, 0.10), metal=0.5, shininess=70, spec=0.7),
            Material((0.2, 0.12, 0.02), emission=(2.2, 1.5, 0.35))]
    return scene, mats


# =========================================================================== jobs

def _harrow(k, glow_only):
    scene, mats = e72.harrow_model("a", only_glow=glow_only, tg=1.0 if glow_only else 0.0)
    if not glow_only:
        return bu.render(*bu.turned(scene, mats, k, 16), HAR, HAR_EXT)
    sc, mm = bu.turned(scene, mats, k, 16)
    hi, f = artkit.render_hi(sc, mm, (HAR, HAR), HAR_EXT)
    return artkit.native(hi, f, 0)


def _shell(f):
    scene, mats = shell_model(TAU * f / 12)
    return bu.render(*bu.turned(scene, mats, 0, 16), HAR, HAR_EXT)


def _pod(k):
    scene, mats = pod_model()
    return bu.render(*bu.turned(scene, mats, k, 16), POD, POD_EXT)


def _canister(f, part):
    sway = SWAY * np.sin(TAU * f / 8)
    scene, mats = canister_model(sway, part)
    return bu.render(scene, mats, CAN, CAN_EXT)


JOBS = {"harrow": _harrow, "shell": _shell, "pod": _pod, "canister": _canister}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== Harrow

def sac_layer(img):
    """The emission-only render -> the additive sac glow at strength 1: the veins' teal plus a soft halo (smooth,
    written unquantised)."""
    a = np.array(img).astype(np.float64)
    e = a[..., :3] * (a[..., 3:4] > 0)
    halo = np.dstack([blur(e[..., c], 1.1) for c in range(3)])
    return np.clip(e * 0.85 + halo * 0.6, 0, 255)


def tell_frames(n=12, w=TELL):
    """The lock tell's halo round the sac: rising from 0.3 to 1 over the tell with three fast pulses; the last two
    frames the lock's flare (a tighter, whiter core)."""
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        pulse = 0.5 + 0.5 * np.cos(TAU * 3 * u)
        k = (0.3 + 0.7 * u) * (0.55 + 0.45 * pulse)
        light = np.zeros((w, w, 3))
        glow(light, w / 2, w / 2, 9 + 4 * u, TEAL, 0.55 * k)
        glow(light, w / 2, w / 2, 4.5 + 1.5 * u, TEAL * 0.7 + 70, 0.5 * k)
        if f >= n - 2:
            flare = 1.0 if f == n - 2 else 0.6
            glow(light, w / 2, w / 2, 2.6, (225, 255, 240), 0.9 * flare)
            glow(light, w / 2, w / 2, 14, TEAL, 0.25 * flare)
        frames.append(additive_rgb(light))
    return frames


def burst_frames(n=8, w=BURST, seed=3801):
    """The sac splitting: a teal flash (the first three frames) and tissue flecks flung out, slowing and falling a
    little, fading; a few gold sparks off the dart's leading edges."""
    rng = np.random.default_rng(seed)
    bits = [(rng.uniform(0, TAU), rng.uniform(70, 140), rng.uniform(0.9, 1.7), "teal") for _ in range(16)]
    bits += [(rng.uniform(0, TAU), rng.uniform(120, 200), rng.uniform(0.5, 0.8), "gold") for _ in range(6)]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        light = np.zeros((w, w, 3))
        flash = max(0.0, 1 - u / 0.42)
        glow(light, w / 2, w / 2, 7 + 9 * u, TEAL, 0.95 * flash)
        glow(light, w / 2, w / 2, 3 + 3 * u, (200, 255, 230), 0.65 * flash ** 1.5)
        for ang, speed, size, kind in bits:
            dist = speed * 0.4 * (1 - (1 - u) ** 2) / 2.2
            x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 5 * u * u
            fade = (1 - u) ** (1.1 if kind == "teal" else 1.8)
            if kind == "teal":
                glow(light, x, y, size, TEAL * 0.9, 1.25 * fade)
                glow(light, x, y, size * 0.5, (210, 255, 230), 0.7 * fade)
            else:
                for s in np.linspace(0, 1, 5):
                    back = dist * (1 - 0.18 * s)
                    glow(light, w / 2 + np.cos(ang) * back, w / 2 + np.sin(ang) * back + 5 * u * u, 0.6, GOLD,
                         1.1 * fade * (1 - 0.7 * s))
        frames.append(additive_rgb(light))
    return frames


def sac_pivots(sacs):
    """Per heading the sac's centre: the glow layer's intensity centroid (the model's axis point SAC_Y as a check)."""
    out = []
    for k, s in enumerate(sacs):
        lum = s.sum(axis=-1)
        yy, xx = np.mgrid[0:lum.shape[0], 0:lum.shape[1]].astype(np.float64) + 0.5
        cx, cy = (lum * xx).sum() / lum.sum(), (lum * yy).sum() / lum.sum()
        out.append([round(float(cx), 1), round(float(cy), 1)])
    return out


def build_harrow():
    res = run([("harrow", k, False) for k in range(16)] + [("harrow", k, True) for k in range(16)]
              + [("shell", f) for f in range(12)])
    body = artkit.quantize_set([bu.rim(f) for f in res[:16]], 40)
    save_set("harrow", body)
    sacs = [sac_layer(g) for g in res[16:32]]
    save_set("harrow-sac", [additive_rgb(s) for s in sacs])
    save_set("harrow-tell", tell_frames())
    save_set("harrow-burst", burst_frames())
    shell = artkit.quantize_set([bu.rim(f) for f in res[32:44]], 32)
    save_set("harrow-shell", shell)
    noses = [[round(float(HAR / 2 + NOSE_Y * HAR_PX * heading_dir(k)[0]), 1),
              round(float(HAR / 2 + NOSE_Y * HAR_PX * heading_dir(k)[1]), 1)] for k in range(16)]
    write_pivots("harrow", {"size": [HAR, HAR], "headings": 16, "sac": sac_pivots(sacs), "nose": noses,
                            "note": "per heading the sac's centre (harrow-tell and harrow-burst are drawn centred "
                                    "there) and the nose tip, [x, y] px from the frame's top left; the ring of 8 "
                                    "spawns at the unit's centre (the sac is 1.4 px behind it)"})
    hunt = add(body[0], sacs[0], HUNT)
    print(f"harrow: teal {e72.teal_share(hunt) * 100:.0f} % hunting ({HUNT}), "
          f"{e72.teal_share(add(body[0], sacs[0], 1.0)) * 100:.0f} % at the tell's peak; "
          f"gold {e72.gold_share(hunt) * 100:.0f} %")


def add(img, light, k):
    a = np.array(img).astype(np.float64)
    a[..., :3] = np.clip(a[..., :3] + np.asarray(light)[..., :3] * k, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== the cargo pod

def outline_frames(frames):
    """The flash's outline per tumble frame: a blue-white light on the silhouette's edge and a soft halo out of it."""
    out = []
    for fr in frames:
        a = np.array(fr)[..., 3].astype(np.float64) / 255
        inner = blur(a, 1.0)
        edge = np.clip(a - np.clip(inner * 1.6 - 0.6, 0, 1), 0, 1)
        halo = blur(a, 1.8) * (1 - a)
        k = edge * 1.0 + halo * 0.9 + a * 0.12
        out.append(additive_rgb(k[..., None] * np.array((190, 215, 255.0))))
    return out


def pod_break(n=8, w=64, seed=3811):
    rng = np.random.default_rng(seed)
    chips = [(rng.uniform(0, TAU), rng.uniform(40, 95), rng.uniform(2.2, 4.8), rng.uniform(0, TAU), rng.uniform())
             for _ in range(13)]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
        yy, xx = np.mgrid[0:w, 0:w].astype(np.float64)
        r = 6 + 17 * u ** 0.7
        smoke = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2 + 10 * u) ** 2) / (r * r)) * 0.8 * (1 - u) ** 0.9
        tex = raster.fbm(w, w, 8, seed + f, octaves=3, period=False)
        smoke = np.clip(smoke * (0.7 + 0.6 * tex), 0, 1)
        hot = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2) ** 2) / (5 + 6 * u) ** 2) * max(0.0, 1 - 2.6 * u)
        rgb = np.array((104, 102, 108.0)) * (1 - hot[..., None]) + np.array((255, 170, 70.0)) * hot[..., None]
        img.alpha_composite(rgba(rgb, np.clip(smoke + hot, 0, 1)))
        d = ImageDraw.Draw(img)
        for ang, speed, size, spin, kind in chips:
            dist = speed * 0.32 * (1 - (1 - u) ** 2)
            x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 14 * u * u
            a = spin + u * 7
            pts = [(x + np.cos(a + k * 2.1) * size, y + np.sin(a + k * 2.1) * size * 0.7) for k in range(3)]
            lit = 0.5 + 0.5 * np.cos(a + 0.8)
            base = np.array((96, 74, 36)) if kind < 0.7 else np.array((84, 80, 70))
            col = tuple(int(c) for c in base + np.array((120, 96, 50)) * lit)
            if u < 0.92:
                d.polygon(pts, fill=col + (255,))
        frames.append(img)
    return frames


def build_pod():
    res = run([("pod", k) for k in range(16)])
    frames = artkit.quantize_set(res, 32)
    save_set("debris-cargo-pod", frames)
    save_set("debris-cargo-pod-outline", outline_frames(frames))
    save_set("debris-cargo-pod-break", pod_break())


# =========================================================================== the canister

def release_frames(canister, chute, latch, n=10, seed=3821):
    """From sway frame 0: the latch spark (frames 0-1), the canister's lid halves swinging open and falling away
    (split down its axis, each half turning outward and fading), the canopy drifting up the screen as the wind takes it
    (shrinking, fading), a grey puff at the canister's top."""
    w, h = CAN
    cx, cy = w / 2, h / 2
    top_y = cy - 0.42 * PX
    can = np.array(canister).astype(np.float64)
    left, right = can.copy(), can.copy()
    left[:, int(cx):, 3] = 0
    right[:, :int(cx), 3] = 0
    halves = [Image.fromarray(left.astype(np.uint8), "RGBA"), Image.fromarray(right.astype(np.uint8), "RGBA")]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        # the canopy: up the screen, shrinking, fading
        s = 1.0 - 0.45 * u
        cc = chute.resize((max(1, int(w * s)), max(1, int(h * s))), Image.BICUBIC)
        ca = np.array(cc).astype(np.float64)
        ca[..., 3] *= (1 - u) ** 1.2
        cc = Image.fromarray(np.clip(ca, 0, 255).astype(np.uint8), "RGBA")
        img.alpha_composite(cc, (int(round(cx - cc.width / 2 + 6 * u)), int(round(cy - cc.height / 2 - 34 * u * s))))
        # the halves: turning outward about the canister's bottom, sliding apart and falling, fading
        for side, half in ((-1, halves[0]), (1, halves[1])):
            ang = side * 55 * u ** 0.8
            hh = half.rotate(-ang, resample=Image.BICUBIC, center=(cx + side * 3, cy + 0.4 * PX))
            ha = np.array(hh).astype(np.float64)
            ha[..., 3] *= (1 - smoothstep(0.45, 1.0, u))
            hh = Image.fromarray(np.clip(ha, 0, 255).astype(np.uint8), "RGBA")
            img.alpha_composite(hh, (int(round(side * 7 * u)), int(round(10 * u * u))))
        # the puff at the top, and the latch's spark
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
        r = 3 + 10 * u ** 0.7
        tex = raster.fbm(w, h, 6, seed + f, octaves=3, period=False)
        puff = np.exp(-((xx - cx) ** 2 + (yy - top_y + 6 * u) ** 2) / (r * r)) * 0.75 * (1 - u) ** 1.1
        puff = np.clip(puff * (0.7 + 0.6 * tex), 0, 1)
        img.alpha_composite(rgba(np.zeros((h, w, 3)) + np.array((150, 150, 156.0)), puff))
        if f < 2:
            light = np.zeros((h, w, 3))
            glow(light, cx, top_y, 3.5 + 2 * f, (255, 200, 110), 1.3 - 0.5 * f)
            glow(light, cx, top_y, 1.4, (255, 250, 230), 1.2 - 0.5 * f)
            la = np.array(img).astype(np.float64)
            add_a = np.clip(light.max(axis=-1) / 255, 0, 1)
            la[..., :3] = np.clip(la[..., :3] * (1 - add_a[..., None]) + np.clip(light, 0, 255) * 1.0, 0, 255)
            la[..., 3] = np.maximum(la[..., 3], add_a * 255)
            img = Image.fromarray(la.astype(np.uint8), "RGBA")
        if f == 0:
            img.alpha_composite(latch)
        frames.append(img)
    return frames


def build_canister():
    res = run([("canister", f, "rig") for f in range(8)] + [("canister", f, "latch") for f in range(8)]
              + [("canister", 0, "canister"), ("canister", 0, "chute")])
    rig = artkit.quantize_set(res[:8], 40)
    save_set("debris-canister", rig)
    save_set("debris-canister-latch", artkit.quantize_set(res[8:16], 16))
    save_set("debris-canister-release", release_frames(res[16], res[17], res[8]))


def harrow_hi():
    """The Harrow nose down (heading 0) for the intel card: the gunmetal dart, gold leading edges, the sac unlit."""
    import intel  # noqa: E402
    scene, mats = e72.harrow_model("a", tg=0.0)
    return intel.belt_hi(*bu.turned(scene, mats, 0, 16), HAR_EXT * 1.04)


INTEL_BATCH = BATCH


def _intel():
    import intel  # noqa: E402
    intel.HI.setdefault("harrow", harrow_hi)
    intel.BATCHES.setdefault("harrow", INTEL_BATCH)
    intel.ROUNDS.setdefault(INTEL_BATCH, ROUND)
    return intel


def build_intel():
    intel = _intel()
    intel.OUT.mkdir(parents=True, exist_ok=True)
    artkit.save_png(intel.portrait("harrow"), intel.OUT / "harrow.png", SOURCE)
    print(f"harrow: intel portrait -> {(intel.OUT / 'harrow.png').relative_to(ROOT)}")


def review_intel():
    _intel().review_batch(INTEL_BATCH, ["harrow"])


BUILDERS = {"harrow": build_harrow, "pod": build_pod, "canister": build_canister, "intel": build_intel}


# =========================================================================== review

def plate(t):
    """Level 38's screen at t without units (tools/art/backdrop_l38.py), or a flat storm plate before it is built."""
    try:
        import backdrop_l38 as bd  # noqa: E402
        return bd.compose(t, units=False)
    except (FileNotFoundError, ImportError, KeyError, OSError):
        yy = np.mgrid[0:540, 0:480][0].astype(np.float64)
        rgb = np.zeros((540, 480, 3)) + np.array((56, 58, 76.0)) + (yy / 540)[..., None] * 18
        return Image.fromarray(rgb.astype(np.uint8), "RGB").convert("RGBA")


def paste(dst, img, x, y):
    return l23kit.paste_c(dst, img, x, y)


def addc(dst, img, x, y, k=1.0):
    return l23kit.add_c(dst, img, x, y, k)


def lum_windows(bg, w=96, h=48):
    a = np.array(bg.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, a.shape[0] - h, 12):
        for x in range(0, a.shape[1] - w, 12):
            best.append((a[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1]


def review_harrow():
    body, sac, tell, burst, shell = (l23kit.load(n) for n in ("harrow", "harrow-sac", "harrow-tell", "harrow-burst",
                                                             "harrow-shell"))
    piv = json.loads(((artkit.PIVOTS / "harrow.json") if (artkit.PIVOTS / "harrow.json").exists()
                      else ROOT / "build" / "art-held" / "pivots" / "harrow.json").read_text())
    boom = artkit.load_frames("explosion-small")
    orb = artkit.load_frames("orb")
    ship = artkit.load_frames("ship")
    bg = plate(30.0)
    sheet = raster.sheet(1000, 1010, "HARROW (FINAL R76): DART AND SAC (A.3 A)", f"PRODUCTION ART, {BATCH.upper()} - R76")
    raster.draw_text(sheet, 16, 38, f"16 HEADINGS AT 2X, HUNTING (SAC {HUNT}), OVER LEVEL 38'S STORM (THE STORM WALL, T=30)",
                     raster.LABEL)
    for k in range(16):
        c = bg.crop((20 + k * 26, 120, 20 + k * 26 + 44, 164)).copy()
        c = addc(paste(c, body[k], 22, 22), sac[k], 22, 22, HUNT)
        sheet.alpha_composite(c.resize((88, 88), Image.NEAREST), (16 + (k % 8) * 96, 54 + (k // 8) * 96))
    y = 54 + 2 * 96 + 8
    raster.draw_text(sheet, 16, y, "THE SAC BY STATE (3X, HEADING 0): DIM 0.2 (EMP) / HUNTING 0.45-0.75 (1 HZ) / TELL "
                                   "0.75 -> 1.0 WITH THE HALO / LOCK FLARE / RAM 0.9", raster.LABEL)
    sx, sy = piv["sac"][0]
    states = [("DIM", 0.2, None), ("HUNT LOW", 0.45, None), ("HUNT HIGH", 0.75, None), ("TELL START", 0.75, 1),
              ("TELL PEAK", 1.0, 8), ("LOCK", 1.0, 10), ("RAM", 0.9, None)]
    for i, (lab, k, tf) in enumerate(states):
        c = bg.crop((300, 300, 348, 348)).copy()
        c = addc(paste(c, body[0], 24, 24), sac[0], 24, 24, k)
        if tf is not None:
            c = addc(c, tell[tf], sx + 4, sy + 4)
        sheet.alpha_composite(c.resize((144, 144), Image.NEAREST), (16 + i * 140, y + 16))
        raster.draw_text(sheet, 16 + i * 140, y + 164, lab, raster.LABEL_DIM)
    y += 186
    raster.draw_text(sheet, 16, y, "THE TELL'S 12 FRAMES (2X, PLAYED OVER THE TELL: 0.6 S; EASY 0.8, HARD 0.5) AND THE "
                                   "BURST'S 8 (1X, OVER EXPLOSION-SMALL)", raster.LABEL)
    for f in range(12):
        c = bg.crop((40 + f * 30, 360, 88 + f * 30, 408)).copy()
        c = addc(addc(paste(c, body[0], 24, 24), sac[0], 24, 24, 0.75 + 0.25 * f / 11), tell[f], 24, 24)
        sheet.alpha_composite(c.resize((76, 76), Image.NEAREST), (16 + f * 80, y + 16))
    for f in range(8):
        c = bg.crop((40 + f * 50, 380, 104 + f * 50, 444)).copy()
        if f < len(boom):
            c = addc(c, boom[min(len(boom) - 1, f)], 32, 32)
        c = addc(c, burst[f], 32, 32)
        sheet.alpha_composite(c.resize((96, 96), Image.NEAREST), (16 + f * 104, y + 100))
    y += 206
    raster.draw_text(sheet, 16, y, "THE SHELL'S TUMBLE (12 FRAMES, 2X) AND THE READ AT 1X: HUNTING AND IN THE TELL "
                                   "BESIDE THE SHIP AND ITS RING BULLETS OVER THE DARKEST AND BRIGHTEST WINDOWS", raster.LABEL)
    for f in range(12):
        c = bg.crop((60 + f * 30, 200, 100 + f * 30, 240)).copy()
        c = paste(c, shell[f], 20, 20)
        sheet.alpha_composite(c.resize((76, 76), Image.NEAREST), (16 + f * 80, y + 16))
    y += 100
    reads = []
    for j, t in enumerate((40.0, 125.0, 175.0)):
        g = plate(t)
        (dlum, dx, dy), (blum, bx, by) = lum_windows(g, 160, 80)
        for i, (lab, (wx, wy), lum) in enumerate((("DARKEST", (dx, dy), dlum), ("BRIGHTEST", (bx, by), blum))):
            c = g.crop((wx, wy, wx + 160, wy + 80)).copy()
            c = addc(paste(c, body[2], 30, 40), sac[2], 30, 40, HUNT)
            c = addc(addc(paste(c, body[14], 72, 40), sac[14], 72, 40, 1.0), tell[8], 72 + piv["sac"][14][0] - 20,
                     40 + piv["sac"][14][1] - 20)
            for b in range(3):
                c = paste(c, orb[0], 100 + b * 12, 22 + b * 16)
            if ship:
                c = paste(c, ship[2], 136, 50)
            x0 = 16 + (j * 2 + i) * 164
            sheet.alpha_composite(c, (x0, y + 16))
            raster.draw_text(sheet, x0, y + 100, f"T={t:.0f} {lab} {lum:.0f}", raster.LABEL_DIM)
            reads.append((t, lab, lum))
    hunt = add(body[0], np.array(sac[0]).astype(np.float64), HUNT)
    raster.draw_text(sheet, 16, y + 124, f"HUNTING: {e72.shares(hunt)}; AT THE TELL'S PEAK: "
                                         f"TEAL {e72.teal_share(add(body[0], np.array(sac[0]).astype(np.float64), 1.0)) * 100:.0f} %. "
                                         "THE RING OF 8 IS THE GAME'S SMALL BULLETS; IT SPAWNS AT THE UNIT'S CENTRE.",
                     raster.LABEL_DIM)
    frames = harrow_gif(body, sac, tell, burst, shell, boom, orb, ship, piv)
    for j, f in enumerate((frames[22], frames[40], frames[66])):
        sheet.alpha_composite(f.resize((240, 270), Image.BOX).crop((0, 0, 240, 270)), (16 + j * 250, y + 140))
    artkit.save_review(sheet, frames, CONCEPT["harrow"], "harrow", fps=20)
    print("review: harrow-final-r76-a")


def harrow_gif(body, sac, tell, burst, shell, boom, orb, ship, piv, t0=30.0, dur=4.4, fps=20):
    """Over Level 38 at t 30-34.4: one Harrow from the left hunts, tells and rams past the ship (missed: the ship
    sidesteps in the ram's last 0.3 s, it flies out); one from the top shot at range (its ring of 8, the gap on the ship's
    bearing, the shell tumbling)."""
    py = 470.0
    ship_x = [240.0]
    st = [dict(x=-20.0, y=180.0, h=0.0, phase="hunt", t0=0.0, chase=1.6),
          dict(x=300.0, y=-20.0, h=np.pi / 2, phase="hunt", t0=0.4, chase=9.0, hp=1.0)]
    rings, frames = [], []
    for i in range(int(dur * fps)):
        t = i / fps
        img = plate(t0 + t)
        px = ship_x[0]
        for s in st:
            if t < s["t0"]:
                continue
            if s["phase"] == "hunt":
                want = np.arctan2(py - s["y"], px - s["x"])
                dh = (want - s["h"] + np.pi) % TAU - np.pi
                s["h"] += float(np.clip(dh, -np.radians(90) / fps, np.radians(90) / fps))
                sp = 110
                if t - s["t0"] >= s["chase"]:
                    s["phase"], s["tell0"] = "tell", t
            elif s["phase"] == "tell":
                want = np.arctan2(py - s["y"], px - s["x"])
                dh = (want - s["h"] + np.pi) % TAU - np.pi
                s["h"] += float(np.clip(dh, -np.radians(240) / fps, np.radians(240) / fps))
                sp = 50
                if t - s["tell0"] >= 0.6:
                    s["phase"], s["h"] = "ram", float(np.arctan2(py - s["y"], px - s["x"]))
            elif s["phase"] == "ram":
                sp = 300
            else:
                sp = 0
            s["x"] += np.cos(s["h"]) * sp / fps
            s["y"] += np.sin(s["h"]) * sp / fps
            if s.get("hp") is not None and s["phase"] == "hunt" and t > 1.6:
                s["phase"], s["died"] = "dead", t
                bearing = np.arctan2(py - s["y"], px - s["x"])
                rings.append((t, s["x"], s["y"], bearing + np.pi / 8))
        # the ship: sidesteps once the first locks
        if st[0]["phase"] == "ram":
            ship_x[0] = min(330.0, ship_x[0] + 260 / fps)
        for s in st:
            # heading k from the direction (dx, dy): k x 22.5 deg clockwise from down
            dx, dy = np.cos(s["h"]), np.sin(s["h"])
            k = int(round((np.degrees(np.arctan2(-dx, dy)) % 360) / 22.5)) % 16
            if s["phase"] in ("hunt", "tell", "ram") and t >= s["t0"]:
                if s["phase"] == "hunt":
                    kk = HUNT + 0.15 * np.sin(TAU * t)
                elif s["phase"] == "tell":
                    kk = 0.75 + 0.25 * (t - s["tell0"]) / 0.6
                else:
                    kk = 0.9
                img = addc(paste(img, body[k], s["x"], s["y"]), sac[k], s["x"], s["y"], kk)
                if s["phase"] == "tell":
                    f = min(11, int((t - s["tell0"]) / 0.6 * 12))
                    sx, sy = piv["sac"][k]
                    img = addc(img, tell[f], s["x"] + sx - 20, s["y"] + sy - 20)
            elif s["phase"] == "dead":
                u = t - s["died"]
                f = int(u * 20)
                if f < len(boom):
                    img = addc(img, boom[f], s["x"], s["y"])
                if int(u * 60 / 3) < 8:
                    img = addc(img, burst[int(u * 20)] if int(u * 20) < 8 else burst[7], s["x"], s["y"])
                if u < 1.0:
                    sh = shell[int(u * 12) % 12]
                    a = np.array(sh).astype(np.float64)
                    a[..., 3] *= 1 - u
                    img = paste(img, Image.fromarray(a.astype(np.uint8), "RGBA"), s["x"] + 18 * u, s["y"] + 40 * u * u)
        for (tr, rx, ry, ph) in rings:
            u = t - tr
            for b in range(8):
                ang = ph + b * TAU / 8
                img = paste(img, orb[int(t * 10) % len(orb)], rx + np.cos(ang) * 90 * u, ry + np.sin(ang) * 90 * u)
        if ship:
            img = paste(img, ship[2 if st[0]["phase"] != "ram" else 3], ship_x[0], py)
        frames.append(img)
    return frames


def review_pod():
    pod, out, br = (l23kit.load(n) for n in ("debris-cargo-pod", "debris-cargo-pod-outline", "debris-cargo-pod-break"))
    bg = plate(118.0)
    sheet = raster.sheet(1000, 470, "AURELIA'S CARGO POD (FINAL R76): LEVEL 38'S SECRET",
                         f"PRODUCTION ART, {BATCH.upper()} - R76")
    raster.draw_text(sheet, 16, 38, "ITS TUMBLE (16 FRAMES, 2X) OVER THE LEFT EDGE AT T=118, LAMPS DEAD; THE SAME WITH THE "
                                    "LIGHTNING FLASH'S OUTLINE (ADDED AT FULL, THE GAME FADES IT OVER 0.5 S)", raster.LABEL)
    for k in range(16):
        c = bg.crop((4, 200 + k * 6, 44, 240 + k * 6)).copy()
        sheet.alpha_composite(paste(c, pod[k], 20, 20).resize((80, 80), Image.NEAREST),
                              (16 + (k % 8) * 86, 54 + (k // 8) * 86))
        c2 = addc(paste(c.copy(), pod[k], 20, 20), out[k], 20, 20)
        if k < 4:
            sheet.alpha_composite(c2.resize((80, 80), Image.NEAREST), (16 + 8 * 86 + (k % 2) * 86, 54 + (k // 2) * 86))
    raster.draw_text(sheet, 16, 230, "THE BREAK IN AIR (8 FRAMES, 2X): GOLD CHIPS, A HOT CORE, SMOKE; THE CRATE IS THE "
                                     "GAME'S", raster.LABEL)
    for f in range(8):
        c = bg.crop((4 + f * 20, 300, 68 + f * 20, 364)).copy()
        sheet.alpha_composite(paste(c, br[f], 32, 32).resize((112, 112), Image.NEAREST), (16 + f * 120, 246))
    frames = []
    for i in range(60):
        t = i / 10
        img = plate(118.0 + t)
        x, y = 40 - 2 * t, 60 + 60 * t
        flash = 1.6 <= t < 2.1
        if t < 4.2:
            img = paste(img, pod[int(t * 4) % 16], x, y)
            if flash:
                img = addc(img, out[int(t * 4) % 16], x, y, 1 - (t - 1.6) / 0.5)
        else:
            f = int((t - 4.2) * 5)
            if f < 8:
                img = paste(img, br[f], 40 - 8.4, 60 + 252)
        frames.append(img)
    sheet.alpha_composite(frames[18].resize((240, 270), Image.BOX).crop((0, 0, 240, 100)), (16, 366))
    sheet.alpha_composite(frames[30].resize((240, 270), Image.BOX).crop((0, 0, 240, 100)), (266, 366))
    artkit.save_review(sheet, frames, CONCEPT["pod"], "cargo-pod", fps=10)
    print("review: cargo-pod-final-r76-a")


def review_canister():
    rig, latch, rel = (l23kit.load(n) for n in ("debris-canister", "debris-canister-latch", "debris-canister-release"))
    bg = plate(100.0)
    sheet = raster.sheet(1000, 720, "CDF SUPPLY CANISTER (FINAL R76): LEVEL 38'S ARMOUR PATCH",
                         f"PRODUCTION ART, {BATCH.upper()} - R76")
    raster.draw_text(sheet, 16, 38, "THE SWAY (8 FRAMES AT 4 FPS, 1.5X) WITH THE LATCH OVERLAY (QD-4 A); THE SAME "
                                    "WITHOUT IT (QD-4 B: IT OPENS BY ITSELF)", raster.LABEL)
    for f in range(8):
        c = bg.crop((200, 200, 264, 328)).copy()
        c = paste(paste(c, rig[f], 32, 64), latch[f], 32, 64)
        sheet.alpha_composite(c.resize((96, 192), Image.NEAREST), (16 + f * 97, 54))
    for f in (0, 2):
        c = paste(bg.crop((200, 200, 264, 328)).copy(), rig[f], 32, 64)
        sheet.alpha_composite(c.resize((96, 192), Image.NEAREST), (16 + 8 * 97 + 8 + (f // 2) * 97, 54))
    raster.draw_text(sheet, 16, 256, "THE RELEASE (10 FRAMES AT 10 FPS, 1.5X): THE LATCH'S SPARK, THE LID HALVES OPEN AND "
                                     "FALL, THE CANOPY TORN LOOSE; THE PATCH IS THE GAME'S", raster.LABEL)
    for f in range(10):
        c = paste(bg.crop((200, 200, 264, 328)).copy(), rel[f], 32, 64)
        sheet.alpha_composite(c.resize((96, 192), Image.NEAREST), (16 + f * 97, 272))
    frames = []
    for i in range(60):
        t = i / 10
        img = plate(100.0 + t)
        x, y = 240.0, -40 + 70 * t
        if t < 3.6:
            img = paste(paste(img, rig[int(t * 4) % 8], x, y), latch[int(t * 4) % 8], x, y)
        else:
            f = int((t - 3.6) * 10)
            if f < 10:
                img = paste(img, rel[f], x, -40 + 70 * 3.6)
        frames.append(img)
    for j, k in enumerate((10, 25, 38, 40)):
        sheet.alpha_composite(frames[k].resize((240, 270), Image.BOX).crop((0, 0, 240, 250)), (16 + j * 245, 470))
    artkit.save_review(sheet, frames, CONCEPT["canister"], "canister", fps=10)
    print("review: canister-final-r76-a")


REVIEWS = {"harrow": review_harrow, "pod": review_pod, "canister": review_canister, "intel": review_intel}

SETS = {"harrow": [("harrow", 16, (HAR, HAR)), ("harrow-sac", 16, (HAR, HAR)), ("harrow-tell", 12, (TELL, TELL)),
                   ("harrow-burst", 8, (BURST, BURST)), ("harrow-shell", 12, (HAR, HAR))],
        "pod": [("debris-cargo-pod", 16, (POD, POD)), ("debris-cargo-pod-outline", 16, (POD, POD)),
                ("debris-cargo-pod-break", 8, (64, 64))],
        "canister": [("debris-canister", 8, CAN), ("debris-canister-latch", 8, CAN),
                     ("debris-canister-release", 10, CAN)]}


def check():
    import jovkit  # noqa: E402  (DitherGuardTest's measure)
    bad = 0
    for subject, sets in SETS.items():
        for name, n, size in sets:
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
                v, c = jovkit.checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src_ok and share < 0.05
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %")
    piv = [p for p in (artkit.PIVOTS / "harrow.json", ROOT / "build" / "art-held" / "pivots" / "harrow.json")
           if p.exists()]
    if not piv:
        print("BAD pivots/harrow.json missing")
        bad += 1
    else:
        data = json.loads(piv[0].read_text())
        ok = len(data["sac"]) == 16 and all(abs(x - HAR / 2) < 4 and abs(y - HAR / 2) < 4 for x, y in data["sac"])
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {piv[0].relative_to(ROOT)}: sac within 4 px of the centre on every heading")
    card = artkit.SPRITES / "intel" / "harrow.png"
    ok = card.exists() and Image.open(card).size == (30, 30) and Image.open(card).text.get(artkit.SOURCE_KEY) == SOURCE
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} assets/sprites/intel/harrow.png")
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
