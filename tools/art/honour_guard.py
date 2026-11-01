#!/usr/bin/env python3
"""Production art: the Honour Guard, Level 40's mid-boss (M10 part F batch, concept round 78, step ART-F1): Vorne's
three aces Harker, Vesper and Kestrel (design/enemies/bosses/honour-guard, round 72 **A.8 a** "marks on one airframe"),
their tells, wrecks and entry tells; the intel silhouette (through intel.py) and the three radio portraits (A.15 a,
through portraits.py).

The aces are round 72's model (tools/concept/act6_r72.py ``ace_model`` a, imported unchanged): the Talon's airframe at
1.5x (30 px a model unit: the 60x54 airframe of the spec, Harker's barrel 14 px under the nose) in Q9 a's elite
livery (gloss black, heavy gold, red running lights) with the concept's strong rim (enemies_r60 ``belt_rim(True)``:
a 2 px gold rim on the sunward edges, red on the far ones; here without its per-frame palette, the set takes one
48-colour palette). The aces differ by paint only: Harker's barrel under the nose and his red nose band, Vesper's
white ribbon stripes, Kestrel's gold chevrons, red tail band and twin fins. Harker also wears the spec's **Ascendancy
crest on his tail fins** (a small gold disc on each fin, added here over the concept model: the pincer's tell dims it;
pending the user's review in round 78).

Headings as tools/art/README.md: heading k turns k x 360/64 degrees clockwise from straight down, every heading its own
render under the fixed key light. Glows are smooth float fields written unquantised (no ordered dither, far more than
32 steps); additive sets are premultiplied on black (GL_SRC_ALPHA, GL_ONE). Nothing here draws a shield: the aces wear
the enemy hex shield's ``enemy-shield-medium`` set (tools/art/act5_gear.py; the medium tier holds bodies up to 84 px,
the enemy-shield rule's "thin gold hex outline at 40 %"), its ``-refill``, ``-break`` and ``enemy-shield-hit``. Their
death's flash and debris ring are ``explosion-vacuum-medium`` and ``explosion-vacuum-debris-medium`` (the belt's vacuum
rules, explosions.py), the last ace's chain ``explosion-vacuum-medium``/``-large`` placed by the pivots' ``chain``.

Outputs (build/art-held/sprites/ until the boss's data.yaml claims the root ``honour-guard`` (step F2); rerun then and
the files move to assets/sprites/; step F4 draws them):
  honour-guard-<ace>_0..63            72x72 normal, ace = harker | vesper | kestrel, 64 headings: the ace's body (48
                                      colours a set; the engines lit, the running lights on)
  honour-guard-harker-crest_0..63     72x72 additive: the crest's gold sheen on both tail fins at strength 1; drawn over
                                      the body at 1.0, dimmed to 0.15 through the pincer's 0.8 s tell (he holds fire)
  honour-guard-vesper-ribbons_0..63   72x72 additive: the ribbon stripes flaring white at strength 1; the game ramps it
                                      0 -> 1 over the spiral loop's 0.8 s tell and fades it over 0.3 s after
  honour-guard-kestrel-band_0..63     72x72 additive: the tail band's red glow at strength 1, ramped over the rear rake's
                                      0.8 s tell
  honour-guard-barrel-glint_0..5      24x24 additive, centred on the pivots' ``barrel``: Harker's barrel glinting red
                                      over the sniper line's 0.8 s glint (frame = progress x 6), before the line
  honour-guard-engine-flare_0..3      24x24 additive, centred on each of the pivots' ``engines``: the engines flaring
                                      white (Kestrel's dive tell, both flankers' in the pincer, each first entry's
                                      glint), flickering every 2 steps
  honour-guard-wingtip_0..3           16x16 additive: Vesper's wingtip trail, one puff by age (0 new .. 3 old), laid at
                                      the pivots' ``wingtips`` every 2 steps while she banks for the roll fan or the
                                      ribbon (0.8 s tell) and through the pass; a puff lives 0.4 s
  honour-guard-<ace>-wreck_0..15      72x72 normal: the dead ace's hull (gold dulled, soot, engines and lights dead,
                                      the cockpit glowing) tumbling: one roll and one turn in 16 frames at 12 fps; the
                                      game drifts it down and out of the field (no hit, no contact); frame 0 = heading 0
  honour-guard-cockpit-fire_0..7      24x24 additive, centred on the wreck frame's ``cockpit`` pivot (when ``visible``):
                                      the fire inside the canopy, a small flickering flame (air inside, no smoke), 12 fps
  honour-guard-chevron-<edge>         normal, edge = top | bottom | left | right (22x14, 14x22): the gold entry chevron
                                      pointing into the field, drawn at the edge an ace enters by, 2.5 s before it
  honour-guard-chevron-glow-<edge>    additive (40x32, 32x40), centred on the chevron: its pulse (the game's 2 Hz)
  honour-guard-callsign-<ace>         normal: the call sign in the HUD's label font (assets/fonts/label-8x12), gold with
                                      a 1 px dark outline; beside the chevron (pivots ``callsign_gap``)
  honour-guard-edge-glint_0..3        48x48 additive, centred on the edge point: the engines' glint at the entry edge
                                      through the tell, 4 flicker frames
  pivots/honour-guard.json            per ace and heading [x, y] px from the frame's top left: ``engines`` (2),
                                      ``wingtips`` (2), ``muzzles`` (the Talon's twin guns; Harker's hidden in his
                                      wings), ``cockpit``, Harker's ``barrel`` (the sniper line's origin) and
                                      ``crests``, Kestrel's ``band``; per wreck frame the ``cockpit`` and whether it
                                      faces up; the chevrons' anchor and the call signs' offset; the last ace's
                                      ``chain`` (t, dx, dy, size)
  assets/sprites/intel/boss-honour-guard.png   40x40, the sensor-L2 silhouette (intel.py's ``BOSSES``, M10 part F
                                      batch, round 78): the three aces' outlines, Harker leading
  assets/sprites/portraits/radio-<harker-guard|vesper|kestrel>-neutral.png   72x72, the aces on the amber enemy
                                      channel (portraits.py ``ACT6F``, round 72 A.15 a: act6_r72.py's busts)
Review (round 78): design/enemies/bosses/honour-guard/concept/honour-guard-final-r78-a.png/.gif (the aces) and
honour-guard-fx-final-r78-a.png/.gif (entry tells, deaths, wrecks, the chain), on round 72's Level 40 stand-in
(act6_r72.py ``space_l40``: Level 40's production backdrop is ART-F2's); intel.py's intel-final-r78-a.png and
portraits.py's portraits-final-r78-a.png.

Run: python3 tools/art/honour_guard.py [aces] [fx] [intel] [portraits] [--review] [--check]
     (no subject = all, ~2 min on 20 cores; --review rebuilds only the review files; --check lists every set with its
     folder, size and frame count and fails on a missing one, a missing Source chunk, a checker over 5 % or an
     additive set under 32 glow steps)
"""
import json
import re
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU, sprite

import act6_r72 as a6  # noqa: E402  (concept: round 72's ace model, imported unchanged)
import enemies_r60 as u60  # noqa: E402  (concept: the elite rim's colours, gold_share)
from render import raster  # noqa: E402
from render.enemy_models import A_ENGINE, A_GLASS, A_GOLD, A_HULL, A_FACET, A_RED  # noqa: E402
from render.enemy_rigs import local_to_screen  # noqa: E402
from render.sdf import Material, mirror_x, rotate_y, sd_cylinder_z, union  # noqa: E402

SCRIPT = "honour_guard.py"
BATCH = "M10 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r78"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "bosses" / "honour-guard" / "concept"
ROOT_NAME = "honour-guard"
ACES = a6.ACES                                # harker, vesper, kestrel
PX = a6.ACE_PX                                # 30 px a model unit: 1.5x the Talon's 20
CELL = 72                                     # the 60x54 airframe and Harker's barrel (33 px from the centre)
EXT = CELL / PX
N = 64
WRECK_N = 16
CREST_SLOT = a6.W_BARREL + 1                  # an 11th material: the crest
CREST = (0.30, -0.66)                         # on each tail fin (model units, nose +Y)
GOLD = np.array((255, 196, 64), float)
GOLD_W = np.array((255, 236, 170), float)
WHITE = np.array((255, 255, 255), float)
RED = np.array((255, 48, 40), float)
FIRE = np.array((255, 120, 30), float)
CREST_DIM = 0.15
TELL_K = 6                                     # the heading the review shows the tells at
# model points (x right, y forward = the nose) the game attaches to
POINTS = {"engines": [(0.12, -0.78), (-0.12, -0.78)], "wingtips": [(0.97, 0.27), (-0.97, 0.27)],
          "muzzles": [(0.6, 0.48), (-0.6, 0.48)], "cockpit": [(0.0, 0.36)]}
EXTRA = {"harker": {"barrel": [(0.0, 1.16)], "crests": [(CREST[0], CREST[1]), (-CREST[0], CREST[1])]},
         "vesper": {}, "kestrel": {"band": [(0.0, -0.63)]}}
SHIP = (240.0, 470.0)                          # the review's ship
EDGES = {"top": (22, 14), "bottom": (22, 14), "left": (14, 22), "right": (14, 22)}


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def write_pivots(data):
    folder = artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{ROOT_NAME}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n",
                                              encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{ROOT_NAME}.json").exists():
        (other / f"{ROOT_NAME}.json").unlink()
    print(f"pivots -> {(folder / f'{ROOT_NAME}.json').relative_to(ROOT)}")


def load_pivots():
    for folder in (artkit.PIVOTS, ROOT / "build" / "art-held" / "pivots"):
        if (folder / f"{ROOT_NAME}.json").exists():
            return json.loads((folder / f"{ROOT_NAME}.json").read_text(encoding="utf-8"))
    raise FileNotFoundError(f"pivots/{ROOT_NAME}.json")


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def additive_rgb(light):
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def blur(a, sigma):
    """A separable gaussian blur of a 2D float array (edges zero-padded)."""
    r = int(np.ceil(sigma * 3))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, r)
    p = np.apply_along_axis(lambda v: np.convolve(v, k, mode="same"), 0, p)
    p = np.apply_along_axis(lambda v: np.convolve(v, k, mode="same"), 1, p)
    return p[r:-r, r:-r]


def grid(w, h):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    return xx, yy


# =========================================================================== models

CREST_MAT = Material((1.0, 0.8, 0.32), metal=0.85, shininess=110, spec=1.2)


def ace_scene(ace):
    """Round 72's ace a (unchanged) with the 11th material slot; Harker gets the crest disc on each tail fin."""
    scene0, mats = a6.ace_model("a", ace)
    mats = list(mats) + [CREST_MAT]
    if ace != "harker":
        return scene0, mats

    def scene(p):
        d, m = scene0(p)
        crest = sd_cylinder_z(mirror_x(p), (CREST[0], CREST[1], 0.062), 0.072, 0.012)
        return union((d, m), (crest, CREST_SLOT))
    return scene, mats


def glow_mats(mats, slot, colour=(1.0, 1.0, 1.0)):
    """Every material black and unlit but ``slot``, which emits ``colour``: an emission mask of one paint."""
    out = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None, emission=(0.0, 0.0, 0.0),
                   emission_pattern=None) for m in mats]
    out[slot] = Material((0.0, 0.0, 0.0), emission=colour)
    return out


def soot(p, n):
    """The wreck's soot: a blotchy darkening in model space."""
    x, y = p[:, 0], p[:, 1]
    v = np.sin(x * 9.1 + 1.3) * np.sin(y * 7.3 - 0.4) + 0.5 * np.sin(x * 17.0 + y * 13.0 + 2.1)
    return 0.55 + 0.45 * smoothstep(-0.6, 0.9, v)


def wreck_mats(mats):
    """The dead ace: gold dulled and sooty, the hull's gloss gone, engines and lights dead, the canopy lit by the
    fire inside."""
    out = []
    for i, m in enumerate(mats):
        alb = np.asarray(m.albedo, float)
        if i in (A_GOLD, CREST_SLOT):
            alb = alb * 0.5
        elif i in (a6.W_WHITE,):
            alb = alb * 0.55
        else:
            alb = alb * 0.8
        out.append(replace(m, albedo=tuple(alb), spec=m.spec * 0.45, shininess=max(10.0, m.shininess * 0.5),
                           emission=(0.0, 0.0, 0.0), emission_pattern=None, pattern=soot))
    out[A_GLASS] = Material((0.08, 0.03, 0.02), shininess=60, spec=0.6, emission=(1.0, 0.38, 0.08))
    return out


def rolled(scene, mats, roll):
    """The model rolled about its long axis by ``roll`` (the wreck's tumble), its patterns rolling with it."""
    def wrap(fn):
        if fn is None:
            return None
        return lambda p, n, f=fn: f(rotate_y(p, roll), rotate_y(n, roll))
    return (lambda p: scene(rotate_y(p, roll))), [replace(m, pattern=wrap(m.pattern),
                                                           emission_pattern=wrap(m.emission_pattern)) for m in mats]


def heading_angle(k, n=N):
    return np.pi / 2 + TAU * k / n


def to_px(lx, ly, k, n=N):
    dx, dy = local_to_screen(lx, ly, heading_angle(k, n), PX)
    return [round(float(CELL / 2 + dx), 2), round(float(CELL / 2 + dy), 2)]


# =========================================================================== jobs (one render each)

def _body(ace, k):
    scene, mats = ace_scene(ace)
    return bu.render(*bu.turned(scene, mats, k, N), CELL, EXT)


def _mask(ace, slot, k):
    """The coverage (0..1, CELL x CELL floats) of one paint seen from above at heading k, occlusion included."""
    scene, mats = ace_scene(ace)
    hi, f = artkit.render_hi(*bu.turned(scene, glow_mats(mats, slot), k, N), (CELL, CELL), EXT, shadows=False)
    m = hi[..., 0] * (hi[..., 3] > 0.5)
    return m.reshape(CELL, f, CELL, f).mean(axis=(1, 3))


def _wreck(ace, f):
    scene, mats = ace_scene(ace)
    scene, mats = rolled(scene, wreck_mats(mats), TAU * f / WRECK_N)
    return bu.render(*bu.turned(scene, mats, f * N // WRECK_N, N), CELL, EXT)


JOBS = {"body": _body, "mask": _mask, "wreck": _wreck}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== the aces

def elite_rim(img):
    """enemies_r60.belt_rim(True) without its per-frame palette: a 2 px gold rim (bright edge, softer inner row) on
    the edges facing the top-left sun, red on the far ones."""
    a = np.array(img.convert("RGBA"))
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    out = a.astype(np.float64)
    out[tl, :3] = out[tl, :3] * 0.12 + u60.GOLD_HI * 0.88
    inner = op & ~tl & ~br & (np.roll(tl, 1, axis=1) | np.roll(tl, 1, axis=0))
    out[inner, :3] = out[inner, :3] * 0.55 + u60.GOLD_RIM * 0.45
    out[br, :3] = out[br, :3] * 0.45 + u60.RED_RIM * 0.55
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def paint_glow(masks, core, halo, sigma=1.6, halo_k=0.9, body=None):
    """Additive frames from paint masks: the paint itself lit in ``core``, a soft halo in ``halo`` around it (kept
    off the parts of the hull the paint does not cover only by its falloff)."""
    out = []
    for m in masks:
        h = blur(m, sigma)
        light = m[..., None] * core + h[..., None] * halo * halo_k
        out.append(additive_rgb(light))
    return out


def build_aces():
    jobs = ([("body", a, k) for a in ACES for k in range(N)]
            + [("mask", "harker", CREST_SLOT, k) for k in range(N)]
            + [("mask", "vesper", a6.W_WHITE, k) for k in range(N)]
            + [("mask", "kestrel", a6.W_RED, k) for k in range(N)])
    res = run(jobs)
    bodies = {a: res[i * N:(i + 1) * N] for i, a in enumerate(ACES)}
    crest, ribbons, band = res[3 * N:4 * N], res[4 * N:5 * N], res[5 * N:6 * N]
    for a in ACES:
        save_set(f"{ROOT_NAME}-{a}", artkit.quantize_set([elite_rim(f) for f in bodies[a]], 48))
    save_set(f"{ROOT_NAME}-harker-crest", paint_glow(crest, GOLD_W * 0.55, GOLD, sigma=1.4, halo_k=0.8))
    save_set(f"{ROOT_NAME}-vesper-ribbons", paint_glow(ribbons, WHITE * 1.0, np.array((235, 240, 255.0)),
                                                       sigma=2.2, halo_k=2.2))
    save_set(f"{ROOT_NAME}-kestrel-band", paint_glow(band, np.array((255, 110, 80.0)) * 0.8, RED, sigma=1.7,
                                                     halo_k=1.0))
    save_set(f"{ROOT_NAME}-barrel-glint", [barrel_glint(0.2 + 0.8 * (i / 5) ** 1.2) for i in range(6)])
    save_set(f"{ROOT_NAME}-engine-flare", [engine_flare(k) for k in (1.0, 0.86, 0.95, 0.8)])
    save_set(f"{ROOT_NAME}-wingtip", [wingtip(age) for age in range(4)])
    write_pivots(pivot_data())


def barrel_glint(k, w=24):
    """Harker's barrel glinting red before the sniper line: a four-point red star round a white-hot core, a soft red
    halo, growing and brightening with ``k`` (smooth)."""
    x, y = grid(w, w)
    x, y = x - w / 2, y - w / 2
    r = np.hypot(x, y)
    arm = 2.0 + 4.5 * k
    star = (np.exp(-np.abs(x) / 0.7) * np.exp(-np.abs(y) / arm) + np.exp(-np.abs(y) / 0.7) * np.exp(-np.abs(x) / arm))
    light = (star[..., None] * RED * 0.9 + np.exp(-(r / (1.2 + 1.3 * k)) ** 2)[..., None] * np.array((255, 220, 200.0))
             + np.exp(-(r / (3.0 + 3.0 * k)) ** 2)[..., None] * RED * 0.45) * k
    return additive_rgb(light)


def engine_flare(k, w=24):
    """A white-hot engine flare: a hot core, a pale warm halo (the engines' amber), smooth."""
    x, y = grid(w, w)
    r = np.hypot(x - w / 2, y - w / 2)
    light = (np.exp(-(r / 2.0) ** 2)[..., None] * WHITE * 1.1 + np.exp(-(r / 5.0) ** 2)[..., None]
             * np.array((255, 214, 160.0)) * 0.55 + np.exp(-(r / 9.0) ** 2)[..., None] * np.array((255, 170, 90.0))
             * 0.18) * k
    return additive_rgb(light)


def wingtip(age, w=16):
    """Vesper's wingtip trail: one soft white puff, smaller and dimmer with age."""
    x, y = grid(w, w)
    r = np.hypot(x - w / 2, y - w / 2)
    k = (1.0, 0.7, 0.42, 0.2)[age]
    s = (2.4, 2.1, 1.8, 1.5)[age]
    light = (np.exp(-(r / s) ** 2)[..., None] * WHITE * 0.9 + np.exp(-(r / (s * 2.3)) ** 2)[..., None]
             * np.array((220, 230, 255.0)) * 0.35) * k
    return additive_rgb(light)


# =========================================================================== deaths, wrecks, entry tells

def cockpit_fire(i, n=8, w=24, seed=7801):
    """The fire inside the dead ace's canopy: a small flame flickering (two licks wandering), no smoke (vacuum
    outside, air inside)."""
    rng = np.random.default_rng(seed + i)
    x, y = grid(w, w)
    c = w / 2
    ph = TAU * i / n
    light = np.zeros((w, w, 3))
    for j, (dx, dy, s, k) in enumerate(((0.0, 0.0, 2.6, 1.0), (1.6 * np.cos(ph), 1.2 * np.sin(2 * ph), 1.8, 0.7),
                                        (-1.4 * np.sin(ph + 1), 1.4 * np.cos(ph * 2 + 0.5), 1.5, 0.55))):
        k *= rng.uniform(0.8, 1.0)
        r = np.hypot(x - c - dx, (y - c - dy) * 0.85)
        light += np.exp(-(r / s) ** 2)[..., None] * FIRE * 1.2 * k
        light += np.exp(-(r / (s * 0.45)) ** 2)[..., None] * np.array((255, 230, 150.0)) * 0.9 * k
    r = np.hypot(x - c, y - c)
    light += np.exp(-(r / 6.0) ** 2)[..., None] * FIRE * 0.22 * (0.85 + 0.15 * np.sin(ph * 3))
    return additive_rgb(light)


def chevron(edge, glow=False):
    """The gold entry chevron pointing into the field from ``edge`` (8x supersampled, 1-bit alpha), or its glow."""
    w, h = EDGES[edge]
    S = 8
    pad = 9 if glow else 0
    W, H = w + 2 * pad, h + 2 * pad
    im = Image.new("L", (W * S, H * S), 0)
    d = ImageDraw.Draw(im)
    cx, cy = W * S / 2, H * S / 2
    if edge in ("top", "bottom"):
        s = -1 if edge == "bottom" else 1                       # top points down, bottom points up
        hw, hh = (w / 2 - 1) * S, (h / 2 - 1) * S
        pts = [(cx - hw, cy - s * hh), (cx, cy + s * hh), (cx + hw, cy - s * hh), (cx, cy - s * (hh - 5.5 * S))]
    else:
        s = -1 if edge == "right" else 1                        # left points right, right points left
        hw, hh = (w / 2 - 1) * S, (h / 2 - 1) * S
        pts = [(cx - s * hw, cy - hh), (cx + s * hw, cy), (cx - s * hw, cy + hh), (cx - s * (hw - 5.5 * S), cy)]
    d.polygon(pts, fill=255)
    cov = np.array(im, float).reshape(H, S, W, S).mean(axis=(1, 3)) / 255
    if glow:
        halo = blur(cov, 3.0)
        light = cov[..., None] * GOLD_W * 0.5 + halo[..., None] * GOLD * 1.6
        return additive_rgb(light)
    body = cov > 0.5
    pad1 = np.pad(body, 1)
    edge_px = body & ~(pad1[:-2, 1:-1] & pad1[2:, 1:-1] & pad1[1:-1, :-2] & pad1[1:-1, 2:])
    xx, yy = grid(W, H)
    shade = 0.82 + 0.18 * np.clip(((W - xx) + (H - yy)) / (W + H), 0, 1)
    rgb = np.where(body[..., None], GOLD * shade[..., None], 0)
    rgb[edge_px] = (60, 30, 4)
    a = np.where(body, 255, 0)
    return Image.fromarray(np.dstack([np.clip(rgb, 0, 255), a]).astype(np.uint8), "RGBA")


def label_font():
    """The HUD's label font (assets/fonts/label-8x12, fonts.py): glyph id -> (box, xoffset, yoffset, xadvance)."""
    fnt = (ROOT / "assets" / "fonts" / "label-8x12.fnt").read_text(encoding="utf-8")
    page = Image.open(ROOT / "assets" / "fonts" / "label-8x12.png").convert("RGBA")
    glyphs = {}
    for line in fnt.splitlines():
        if line.startswith("char "):
            v = dict(re.findall(r"(\w+)=(-?\d+)", line))
            v = {k: int(x) for k, x in v.items()}
            glyphs[v["id"]] = ((v["x"], v["y"], v["x"] + v["width"], v["y"] + v["height"]), v["xoffset"],
                               v["yoffset"], v["xadvance"])
    return page, glyphs


def callsign(ace):
    """The call sign in the label font, gold, a 1 px dark outline round it."""
    page, glyphs = label_font()
    text = a6.ACE_NAME[ace]
    w = sum(glyphs[ord(c)][3] for c in text) + 2
    mask = np.zeros((12 + 2, w), bool)
    x = 1
    for c in text:
        box, xo, yo, adv = glyphs[ord(c)]
        g = np.array(page.crop(box))[..., 3] > 127
        mask[1 + yo:1 + yo + g.shape[0], x + xo:x + xo + g.shape[1]] |= g
        x += adv
    ys, xs = np.nonzero(mask)
    mask = mask[max(0, ys.min() - 1):ys.max() + 2, max(0, xs.min() - 1):xs.max() + 2]
    mask = np.pad(mask, 1)
    pad = np.pad(mask, 1)
    ring = (pad[:-2, 1:-1] | pad[2:, 1:-1] | pad[1:-1, :-2] | pad[1:-1, 2:] | pad[:-2, :-2] | pad[2:, 2:]
            | pad[:-2, 2:] | pad[2:, :-2]) & ~mask
    rgb = np.zeros(mask.shape + (3,))
    rgb[mask] = (255, 214, 120)
    rgb[ring] = (34, 18, 2)
    a = np.where(mask | ring, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def edge_glint(k, w=48):
    """The engines' glint at the entry edge: a white-gold core and a wide soft gold halo, smooth."""
    x, y = grid(w, w)
    r = np.hypot(x - w / 2, y - w / 2)
    light = (np.exp(-(r / 3.0) ** 2)[..., None] * WHITE * 0.9 + np.exp(-(r / 8.0) ** 2)[..., None] * GOLD_W * 0.45
             + np.exp(-(r / 15.0) ** 2)[..., None] * GOLD * 0.22) * k
    return additive_rgb(light)


def chain_schedule(seed=7840):
    """The last ace's 2 s chain round its tumbling hull: (t s, dx, dy px from the hull's centre, size)."""
    rng = np.random.default_rng(seed)
    out = []
    times = [0.0, 0.22, 0.45, 0.62, 0.85, 1.05, 1.3, 1.52, 1.75, 2.0]
    for i, t in enumerate(times):
        a, r = rng.uniform(0, TAU), rng.uniform(6, 24) * (0.4 if i in (5, 9) else 1.0)
        size = "large" if i in (5, 9) else "medium"
        out.append({"t": t, "dx": round(float(np.cos(a) * r), 1), "dy": round(float(np.sin(a) * r), 1), "size": size})
    return out


def build_fx():
    res = run([("wreck", a, f) for a in ACES for f in range(WRECK_N)])
    for i, a in enumerate(ACES):
        save_set(f"{ROOT_NAME}-{a}-wreck", artkit.quantize_set([bu.rim(f) for f in res[i * WRECK_N:(i + 1) * WRECK_N]],
                                                               40))
    save_set(f"{ROOT_NAME}-cockpit-fire", [cockpit_fire(i) for i in range(8)])
    for edge in EDGES:
        save_set(f"{ROOT_NAME}-chevron-{edge}", [chevron(edge)], single=True)
        save_set(f"{ROOT_NAME}-chevron-glow-{edge}", [chevron(edge, glow=True)], single=True)
    for a in ACES:
        save_set(f"{ROOT_NAME}-callsign-{a}", [callsign(a)], single=True)
    save_set(f"{ROOT_NAME}-edge-glint", [edge_glint(k) for k in (1.0, 0.84, 0.94, 0.78)])
    write_pivots(pivot_data())


# =========================================================================== pivots

def wreck_cockpit(f):
    """Where the cockpit sits in wreck frame f and whether it faces the camera."""
    roll = TAU * f / WRECK_N
    m = np.array([[0.0, POINTS["cockpit"][0][1], 0.16]])
    p = rotate_y(m, -roll)[0]                              # the model rolled by ``roll``: its point moves the other way
    return {"cockpit": to_px(p[0], p[1], f * N // WRECK_N), "visible": bool(p[2] > 0.02)}


def pivot_data():
    aces = {}
    for a in ACES:
        pts = dict(POINTS, **EXTRA[a])
        aces[a] = [{key: [to_px(x, y, k) for x, y in v] if len(v) > 1 else to_px(*v[0], k)
                    for key, v in pts.items()} for k in range(N)]
    return {"frame": [CELL, CELL], "centre": [CELL / 2, CELL / 2], "headings": N,
            "heading_rule": "frame k = heading k x 360/64 deg clockwise from moving down the screen",
            "aces": aces,
            "wreck": [wreck_cockpit(f) for f in range(WRECK_N)],
            "wreck_fps": 12,
            "crest_dim": CREST_DIM,
            "chevron": {"inset": 10, "callsign_gap": 3,
                        "note": "the chevron's centre `inset` px inside the edge at the ace's entry point; the call "
                                "sign `callsign_gap` px beyond the chevron's inner edge, toward the field (below a "
                                "top chevron, above a bottom one, right of a left one, left of a right one), centred "
                                "on the chevron's axis"},
            "chain": chain_schedule()}


# =========================================================================== intel and portraits

def intel_mask():
    """The three aces' outlines for the sensor silhouette (intel.py's ``BOSSES``): Harker leading at the top facing
    down, Vesper banking in from the left, Kestrel rising from the rear on the right (their entry edges)."""
    layer = Image.new("RGBA", (190, 170), (0, 0, 0, 0))
    for ace, k, x, y in (("harker", 0, 95, 46), ("vesper", 52, 44, 118), ("kestrel", 28, 146, 124)):
        sprite.paste_center(layer, l23kit.load(f"{ROOT_NAME}-{ace}")[k], x, y)
    return np.array(layer)[..., 3] > 127


def build_intel():
    import intel  # noqa: E402  (registers boss-honour-guard; imported late: it imports this module)
    intel.build(["boss-honour-guard"])


def review_intel():
    import intel  # noqa: E402
    intel.review(["boss-honour-guard"])


def build_portraits():
    import portraits  # noqa: E402
    portraits.build_act6(portraits.ACT6F)


def review_portraits():
    import portraits  # noqa: E402
    portraits.review_act6f()


# =========================================================================== review

def stand_in():
    """Round 72's Level 40 stand-in (black space, Callisto's disc, Jupiter's cream limb, the fleet's lights)."""
    return a6.guard_backdrop()


def paste(dst, img, x, y):
    sprite.paste_center(dst, img, x, y)
    return dst


def addc(dst, img, x, y, k=1.0):
    if k <= 0:
        return dst
    if k < 1:
        a = np.array(img).astype(np.float64)
        a[..., :3] *= k
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return artkit.add_light(dst, img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def plate(w, h):
    return Image.new("RGBA", (w, h), artkit.PLATE)


def load(name):
    return l23kit.load(name)


def gold_shares():
    return {a: [u60.gold_share(f) for f in load(f"{ROOT_NAME}-{a}")] for a in ACES}


def ace_dressed(ace, k, tell=0.0, crest=1.0, flare=0.0, piv=None, shield=None, glint=None):
    """One ace on a 72 px cell (transparent): the body, its crest, a tell at ``tell``, engine flares at ``flare``."""
    piv = piv or load_pivots()
    cell = Image.new("RGBA", (CELL + 48, CELL + 48), artkit.PLATE)       # opaque: additive light needs a ground
    o = 24
    cell.alpha_composite(load(f"{ROOT_NAME}-{ace}")[k], (o, o))
    p = piv["aces"][ace][k]
    if ace == "harker":
        cell = addc(cell, load(f"{ROOT_NAME}-harker-crest")[k], o + CELL / 2, o + CELL / 2, crest)
        if glint is not None:
            cell = addc(cell, load(f"{ROOT_NAME}-barrel-glint")[glint], o + p["barrel"][0], o + p["barrel"][1])
    if ace == "vesper":
        cell = addc(cell, load(f"{ROOT_NAME}-vesper-ribbons")[k], o + CELL / 2, o + CELL / 2, tell)
    if ace == "kestrel":
        cell = addc(cell, load(f"{ROOT_NAME}-kestrel-band")[k], o + CELL / 2, o + CELL / 2, tell)
    if flare:
        for e in p["engines"]:
            cell = addc(cell, load(f"{ROOT_NAME}-engine-flare")[0], o + e[0], o + e[1], flare)
    if shield is not None:
        cell = addc(cell, load("enemy-shield-medium")[shield], o + CELL / 2, o + CELL / 2)
    return cell


def heading_index(dx, dy):
    """The nearest of the 64 headings for a velocity (0 = down, clockwise)."""
    ang = np.arctan2(dy, dx) - np.pi / 2
    return int(round((ang % TAU) / (TAU / N))) % N


def review_aces():
    piv = load_pivots()
    shares = gold_shares()
    W = 16 + 16 * 74 + 16
    rows_h = 3 * (18 + 4 * 74 + 8)
    tell_h = 18 + 3 * 194 + 8
    read_h = 18 + 570 + 10
    sheet = raster.sheet(W, 40 + rows_h + tell_h + read_h + 120,
                         "HONOUR GUARD (FINAL R78): THE THREE ACES, ROUND 72 A.8 A",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for a in ACES:
        fr = load(f"{ROOT_NAME}-{a}")
        raster.draw_text(sheet, 16, y, f"HONOUR-GUARD-{a.upper()}_0..63 ({fr[0].width}X{fr[0].height}, "
                                       f"{artkit.colour_count(fr)} COLOURS, 1X): 64 HEADINGS FROM DOWN, CLOCKWISE; "
                                       f"GOLD {100 * min(shares[a]):.0f}-{100 * max(shares[a]):.0f} %", raster.LABEL)
        for k, f in enumerate(fr):
            x0, y0 = 16 + (k % 16) * 74, y + 16 + (k // 16) * 74
            sheet.alpha_composite(artkit.checker(72, 72, 4), (x0, y0))
            sheet.alpha_composite(f, (x0, y0))
        y += 18 + 4 * 74 + 8
    raster.draw_text(sheet, 16, y, f"MARKS AND TELLS AT 2X (HEADING {TELL_K}): REST | TELL | WITH THE HEX SHIELD "
                                   "(ENEMY-SHIELD-MEDIUM) | THE ENGINES FLARING | HARKER: THE CREST DIMMED (PINCER) AND "
                                   "THE BARREL'S RED GLINT", raster.LABEL)
    y += 16
    for a in ACES:
        x = 16
        states = [dict(), dict(tell=1.0) if a != "harker" else dict(glint=5), dict(shield=0), dict(flare=1.0)]
        if a == "harker":
            states.append(dict(crest=CREST_DIM))
        for st in states:
            c = Image.new("RGBA", (CELL + 48, CELL + 48), artkit.PLATE)
            c.alpha_composite(ace_dressed(a, TELL_K, piv=piv, **st))
            sheet.alpha_composite(sprite.enlarge(c, 2).crop((26, 26, 214, 214)), (x, y))
            x += 194
        raster.draw_text(sheet, x + 4, y + 90, a.upper(), raster.LABEL_DIM)
        y += 194
    y += 4
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X OVER ROUND 72'S LEVEL 40 STAND-IN (BLACK SPACE, CALLISTO, "
                                   "JUPITER'S CREAM LIMB, THE FLEET'S LIGHTS): HARKER'S SNIPER LINE, THE SHIP AND ROOK",
                     raster.LABEL)
    t_read = 3.62                                  # Harker's line live, Vesper over Jupiter's limb, Kestrel climbing
    field = scene_frame(t_read, piv)
    sheet.alpha_composite(field, (16, y + 16))
    for i, a in enumerate(ACES):
        x, y1 = ace_path(a, t_read)
        bx, by = int(min(max(x - 75, 0), a6.FW - 150)), int(min(max(y1 - 44, 0), a6.FH - 88))
        box = (bx, by, bx + 150, by + 88)
        x0, y0 = 16 + 480 + 24, y + 16 + i * 190
        sheet.alpha_composite(sprite.enlarge(field.crop(box), 2), (x0, y0))
        raster.draw_text(sheet, x0 + 306, y0 + 4, f"2X: {a.upper()}", raster.LABEL_DIM)
    y += 18 + 570 + 10
    notes = [f"ROUND 72 A.8 A ON THE TALON'S AIRFRAME AT 1.5X (30 PX A UNIT, THE 60X54 AIRFRAME IN 72 PX CELLS); THE STRONG "
             f"TOP-LEFT GOLD RIM, RED ON THE FAR EDGES; ONE 48-COLOUR PALETTE A SET. GOLD SHARE: " +
             ", ".join(f"{a.upper()} {100 * np.mean(shares[a]):.0f} %" for a in ACES) + " (THE BRIEF 35-45 %).",
             "TELLS AS ADDITIVE LAYERS AT STRENGTH 1 (THE GAME RAMPS THEM): HARKER'S CREST (DIMMED TO 0.15 IN THE "
             "PINCER), VESPER'S RIBBONS, KESTREL'S TAIL BAND; THE BARREL GLINT, THE ENGINE FLARE AND THE WINGTIP PUFFS "
             "AT THE PIVOTS. EVERY GLOW A SMOOTH FLOAT FIELD, NO DITHER.",
             "THE SHIELD IS THE ENEMY HEX SHIELD'S MEDIUM TIER AS BUILT (ACT5_GEAR.PY); THE CREST ON HARKER'S FINS IS "
             "THE SPEC'S, ADDED OVER THE ROUND 72 MODEL (PENDING ROUND 78)."]
    for line in notes:
        for part in wrap(line, 190):
            raster.draw_text(sheet, 16, y, part, raster.LABEL_DIM)
            y += 10
        y += 4
    png, _ = artkit.review_paths(CONCEPT, ROOT_NAME)
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")
    frames = [scene_frame(t, piv) for t in np.arange(0, 4.8, 1 / 15)]
    from render.enemy_rigs import write_gif
    write_gif(frames, png.with_suffix(".gif"), fps=15, colors=128)
    print(f"review: {png.with_suffix('.gif').relative_to(ROOT)}")


def wrap(s, n):
    words, lines, cur = s.split(), [], ""
    for w in words:
        if len(cur) + len(w) + 1 > n:
            lines.append(cur)
            cur = w
        else:
            cur = (cur + " " + w).strip()
    return lines + [cur]


_BG = {}


def bg():
    if "l40" not in _BG:
        _BG["l40"] = stand_in()
    return _BG["l40"].copy()


def ace_path(ace, t):
    """Review flight paths over 4.8 s: Harker strafing the top third, Vesper's figure-eight through the middle band,
    Kestrel holding low then diving at the ship and pulling out."""
    if ace == "harker":
        x = 240 + 60 * np.sin(t * 1.4)
        return x, 120.0
    if ace == "vesper":
        s = t * 1.3
        return 175 + 105 * np.sin(s), 250 + 45 * np.sin(2 * s)
    sx, sy = 400.0, 505.0                          # its station right of the ship band
    if t < 1.6:                                    # holding (the tell from 0.8 s)
        return sx + 6 * np.sin(t * 3), sy
    if t < 2.1:                                    # the dive at the ship, pulled out 110 px short of it
        u = smoothstep(0.0, 1.0, (t - 1.6) / 0.5)
        return sx + (SHIP[0] + 0.67 * (sx - SHIP[0]) - sx) * u, sy + (SHIP[1] + 0.67 * (sy - SHIP[1]) - sy) * u
    px, py = SHIP[0] + 0.67 * (sx - SHIP[0]), SHIP[1] + 0.67 * (sy - SHIP[1])
    u = min(1.0, (t - 2.1) / 1.6)                  # the pull-out and the climb back past its station
    return px + (430 - px) * u, py + (400 - py) * u - 40 * np.sin(u * np.pi)


def scene_frame(t, piv):
    img = bg()
    ship_xy = SHIP
    paste(img, a6.Prod.get("ship", 2), *ship_xy)
    paste(img, a6.Prod.get("rook", 2), 290, 494)
    for a in ACES:
        x, y = ace_path(a, t)
        x2, y2 = ace_path(a, t + 0.05)
        if a == "harker":
            k = heading_index(ship_xy[0] - x, ship_xy[1] - y)
        else:
            k = heading_index(x2 - x, y2 - y)
        p = piv["aces"][a][k]
        cell = load(f"{ROOT_NAME}-{a}")[k]
        img.alpha_composite(cell, (int(round(x - CELL / 2)), int(round(y - CELL / 2))))
        ox, oy = x - CELL / 2, y - CELL / 2
        if a == "harker":
            img = addc(img, load(f"{ROOT_NAME}-harker-crest")[k], x, y)
            g = (t % 2.4)
            if g < 0.8:
                img = addc(img, load(f"{ROOT_NAME}-barrel-glint")[min(5, int(g / 0.8 * 6))], ox + p["barrel"][0],
                           oy + p["barrel"][1])
            elif g < 1.8:
                bx, by = ox + p["barrel"][0], oy + p["barrel"][1]
                img = a6.sniper_line(img, bx, by, bx + (ship_xy[0] - bx) * 4, by + (ship_xy[1] - by) * 4,
                                     min(1.0, (g - 0.8) / 1.0))
        if a == "vesper":
            tell = float(smoothstep(0.6, 1.4, t) * (1 - smoothstep(2.6, 3.0, t)))
            img = addc(img, load(f"{ROOT_NAME}-vesper-ribbons")[k], x, y, tell)
            for age in range(4):
                tt = t - 0.1 * (age + 1)
                if tt < 0.6 or tt > 3.0:
                    continue
                xa, ya = ace_path(a, tt)
                ka = heading_index(*(np.subtract(ace_path(a, tt + 0.05), (xa, ya))))
                for w in piv["aces"][a][ka]["wingtips"]:
                    img = addc(img, load(f"{ROOT_NAME}-wingtip")[age], xa - CELL / 2 + w[0], ya - CELL / 2 + w[1])
        if a == "kestrel" and 0.8 <= t < 1.6:
            for e in p["engines"]:
                img = addc(img, load(f"{ROOT_NAME}-engine-flare")[int(t * 10) % 4], ox + e[0], oy + e[1])
    return img


# --- the effects sheet

def review_fx():
    piv = load_pivots()
    W = 16 + 16 * 74 + 16
    sheet = raster.sheet(W, 2400, "HONOUR GUARD (FINAL R78): ENTRY TELLS, DEATHS, WRECKS, THE LAST ACE'S CHAIN",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for a in ACES:
        fr = load(f"{ROOT_NAME}-{a}-wreck")
        raster.draw_text(sheet, 16, y, f"HONOUR-GUARD-{a.upper()}-WRECK_0..15 ({artkit.colour_count(fr)} COLOURS, 1X "
                                       f"AND 2X): THE TUMBLE, ONE ROLL AND ONE TURN; THE COCKPIT FIRE AT ITS PIVOT",
                         raster.LABEL)
        for f, img in enumerate(fr):
            c = Image.new("RGBA", (72, 72), artkit.PLATE)
            c.alpha_composite(img)
            w = piv["wreck"][f]
            if w["visible"]:
                c = addc(c, load(f"{ROOT_NAME}-cockpit-fire")[f % 8], *w["cockpit"])
            sheet.alpha_composite(c, (16 + f * 74, y + 16))
        y += 16 + 74
        x = 16
        for f in range(0, WRECK_N, 2):
            c = Image.new("RGBA", (72, 72), artkit.PLATE)
            c.alpha_composite(fr[f])
            w = piv["wreck"][f]
            if w["visible"]:
                c = addc(c, load(f"{ROOT_NAME}-cockpit-fire")[f % 8], *w["cockpit"])
            sheet.alpha_composite(sprite.enlarge(c, 2), (x, y))
            x += 148
        y += 150
    raster.draw_text(sheet, 16, y, "ADDITIVE SETS AT 3X: COCKPIT FIRE 0..7 | BARREL GLINT 0..5 | ENGINE FLARE 0..3 | "
                                   "WINGTIP 0..3 | EDGE GLINT 0..3", raster.LABEL)
    y += 16
    x = 16
    for name in ("cockpit-fire", "barrel-glint", "engine-flare", "wingtip", "edge-glint"):
        for f in load(f"{ROOT_NAME}-{name}"):
            cell = artkit.on_background(f, 3 if f.width <= 24 else 1, glow=True)
            if x + cell.width > W - 16:
                x = 16
                y += 76
            sheet.alpha_composite(cell, (x, y))
            x += cell.width + 4
        x += 12
    y += 80
    raster.draw_text(sheet, 16, y, "ENTRY TELLS (2.5 S AHEAD): THE CHEVRON, ITS GLOW, THE CALL SIGN (THE LABEL FONT) "
                                   "AND THE ENGINES' GLINT AT EACH EDGE, 1X OVER THE STAND-IN, AND THE PIECES AT 3X",
                     raster.LABEL)
    y += 16
    field = entry_frame(1.0, piv)
    sheet.alpha_composite(field, (16, y))
    x0 = 16 + 480 + 24
    for i, edge in enumerate(EDGES):
        xx, yy = x0 + (i % 2) * 300, y + (i // 2) * 130
        for name in (f"chevron-{edge}", f"chevron-glow-{edge}"):
            f = load(f"{ROOT_NAME}-{name}")[0]
            cell = artkit.on_background(f, 3, glow="glow" in name)
            sheet.alpha_composite(cell, (xx, yy))
            xx += cell.width + 6
    yy = y + 270
    for a in ACES:
        f = load(f"{ROOT_NAME}-callsign-{a}")[0]
        sheet.alpha_composite(artkit.on_background(f, 3), (x0, yy))
        yy += 3 * f.height + 8
    y += 548
    raster.draw_text(sheet, 16, y, "AN ACE'S DEATH AND THE LAST ACE'S CHAIN (1X, KEY FRAMES): THE SHIELD SHATTERS, THE "
                                   "VACUUM FLASH AND DEBRIS RING, THE HULL TUMBLES AWAY DOWN AND OUT, THE COCKPIT BURNING;"
                                   " THE LAST: 2 S OF BURSTS ROUND IT", raster.LABEL)
    y += 16
    x = 16
    for t in (0.0, 0.15, 0.35, 0.7, 1.2, 1.9, 2.6):
        sheet.alpha_composite(death_frame(t, piv).crop((150, 190, 310, 430)), (x, y))
        raster.draw_text(sheet, x, y + 244, f"T {t:.2f} S", raster.LABEL_DIM)
        x += 166
    y += 260
    for line in wrap("THE SHIELD'S BREAK IS ENEMY-SHIELD-MEDIUM-BREAK; THE FLASH AND THE DEBRIS RING ARE EXPLOSION-VACUUM-"
                     "MEDIUM AND EXPLOSION-VACUUM-DEBRIS-MEDIUM (THE BELT'S VACUUM RULES: NO RISING SMOKE); THE CHAIN IS "
                     "TEN BURSTS OVER 2 S (EIGHT MEDIUM, TWO LARGE) AT THE PIVOTS' CHAIN OFFSETS ROUND THE TUMBLING HULL. "
                     "NO SCREEN FLASH (A MID-BOSS).", 190):
        raster.draw_text(sheet, 16, y, line, raster.LABEL_DIM)
        y += 10
    sheet = sheet.crop((0, 0, W, y + 10))
    png, _ = artkit.review_paths(CONCEPT, f"{ROOT_NAME}-fx")
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")
    frames = [entry_frame(t, piv) for t in np.arange(0, 3.4, 1 / 12)]
    frames += [death_frame(t, piv, full=True) for t in np.arange(0, 4.2, 1 / 12)]
    from render.enemy_rigs import write_gif
    write_gif(frames, png.with_suffix(".gif"), fps=12, colors=128)
    print(f"review: {png.with_suffix('.gif').relative_to(ROOT)}")


ENTRY = {"harker": ("top", 240), "vesper": ("right", 260), "kestrel": ("bottom", 330)}


def entry_point(edge, pos, inset):
    return {"top": (pos, inset), "bottom": (pos, a6.FH - inset), "left": (inset, pos),
            "right": (a6.FW - inset, pos)}[edge]


def entry_frame(t, piv):
    """All three entry tells at once over the stand-in (the game shows one at a time): t 0..2.5 the tell, then the
    ace flies in from its edge."""
    img = bg()
    paste(img, a6.Prod.get("ship", 2), 240, 470)
    inset = piv["chevron"]["inset"]
    for a, (edge, pos) in ENTRY.items():
        cx, cy = entry_point(edge, pos, inset)
        if t < 2.5:
            pulse = 0.55 + 0.45 * np.cos(TAU * 2 * t)
            img = addc(img, load(f"{ROOT_NAME}-edge-glint")[int(t * 10) % 4], *entry_point(edge, pos, 0),
                       0.5 + 0.5 * t / 2.5)
            img = addc(img, load(f"{ROOT_NAME}-chevron-glow-{edge}")[0], cx, cy, pulse)
            paste(img, load(f"{ROOT_NAME}-chevron-{edge}")[0], cx, cy)
            cs = load(f"{ROOT_NAME}-callsign-{a}")[0]
            gap = piv["chevron"]["callsign_gap"]
            cw, ch = EDGES[edge]
            off = {"top": (0, ch / 2 + gap + cs.height / 2), "bottom": (0, -(ch / 2 + gap + cs.height / 2)),
                   "left": (cw / 2 + gap + cs.width / 2, 0), "right": (-(cw / 2 + gap + cs.width / 2), 0)}[edge]
            paste(img, cs, cx + off[0], cy + off[1])
        else:
            u = min(1.0, (t - 2.5) / 0.9)
            ex, ey = entry_point(edge, pos, -40)
            tx, ty = {"harker": (240, 120), "vesper": (380, 250), "kestrel": (330, 470)}[a]
            x, y = ex + (tx - ex) * u, ey + (ty - ey) * u
            k = heading_index(tx - ex, ty - ey)
            img.alpha_composite(load(f"{ROOT_NAME}-{a}")[k], (int(round(x - CELL / 2)), int(round(y - CELL / 2))))
            for e in piv["aces"][a][k]["engines"]:
                img = addc(img, load(f"{ROOT_NAME}-engine-flare")[int(t * 10) % 4], x - CELL / 2 + e[0],
                           y - CELL / 2 + e[1], 1 - u * 0.6)
    return img


def explosion(name, i):
    fr = load(name)
    return fr[min(len(fr) - 1, i)]


def death_frame(t, piv, full=False):
    """Kestrel dies at (240, 280) at t 0 as the last ace: the shield's break, the vacuum flash and debris, the wreck
    tumbling down and out (12 fps), the cockpit fire, the chain's bursts over 2 s."""
    img = bg()
    paste(img, a6.Prod.get("ship", 2), 240, 470)
    x0, y0, k0 = 240.0, 280.0, 40
    if t < 0:
        return img
    x, y = x0 + 18 * t + 4 * t * t, y0 + 30 * t + 22 * t * t
    f = int(t * 12) % WRECK_N
    wk = load(f"{ROOT_NAME}-kestrel-wreck")[(f + k0 // 4) % WRECK_N] if t > 0.08 else load(f"{ROOT_NAME}-kestrel")[k0]
    img.alpha_composite(wk, (int(round(x - CELL / 2)), int(round(y - CELL / 2))))
    if t > 0.08:
        w = piv["wreck"][(f + k0 // 4) % WRECK_N]
        if w["visible"]:
            img = addc(img, load(f"{ROOT_NAME}-cockpit-fire")[int(t * 12) % 8], x - CELL / 2 + w["cockpit"][0],
                       y - CELL / 2 + w["cockpit"][1])
    brk = load("enemy-shield-medium-break")
    if t < 0.2:
        img = addc(img, brk[min(5, int(t / 0.2 * 6))], x0, y0)
    fl = int(t * 20)
    if fl < 12:
        img = addc(img, explosion("explosion-vacuum-medium", fl), x0, y0)
        paste(img, explosion("explosion-vacuum-debris-medium", fl), x0, y0)
    for c in piv["chain"]:
        dt = t - 0.3 - c["t"]
        if 0 <= dt < 0.6:
            img = addc(img, explosion(f"explosion-vacuum-{c['size']}", int(dt * 20)), x + c["dx"], y + c["dy"])
    return img


# =========================================================================== check

SETS = ([(f"{ROOT_NAME}-{a}", N, (CELL, CELL), False) for a in ACES]
        + [(f"{ROOT_NAME}-harker-crest", N, (CELL, CELL), True), (f"{ROOT_NAME}-vesper-ribbons", N, (CELL, CELL), True),
           (f"{ROOT_NAME}-kestrel-band", N, (CELL, CELL), True), (f"{ROOT_NAME}-barrel-glint", 6, (24, 24), True),
           (f"{ROOT_NAME}-engine-flare", 4, (24, 24), True), (f"{ROOT_NAME}-wingtip", 4, (16, 16), True)]
        + [(f"{ROOT_NAME}-{a}-wreck", WRECK_N, (CELL, CELL), False) for a in ACES]
        + [(f"{ROOT_NAME}-cockpit-fire", 8, (24, 24), True), (f"{ROOT_NAME}-edge-glint", 4, (48, 48), True)]
        + [(f"{ROOT_NAME}-chevron-{e}", 1, EDGES[e], False) for e in EDGES]
        + [(f"{ROOT_NAME}-chevron-glow-{e}", 1, (EDGES[e][0] + 18, EDGES[e][1] + 18), True) for e in EDGES]
        + [(f"{ROOT_NAME}-callsign-{a}", 1, None, False) for a in ACES])


def checker_share(img):
    """DitherGuardTest's measure: visible pixels that are a 1 px checker cell -> (visible, checker)."""
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 3] << 24) | (a[..., 0] << 16) | (a[..., 1] << 8) | a[..., 2]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    l, r, u, d = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (l == r) & (u == d) & (l != mid) & (u != mid)
    return int(vis.sum()), int(chk.sum())


def glow_steps(frames):
    vals = np.concatenate([np.array(f)[..., :3].max(axis=-1).ravel() for f in frames])
    return len(np.unique(vals[vals > 0]))


def atlas_area(frames):
    """Page area the packer takes for a set (2 px padding, no whitespace strip)."""
    return sum((f.width + 2) * (f.height + 2) for f in frames)


def check():
    bad = 0
    total = 0
    for name, n, size, add in SETS:
        where = l23kit.where(name)
        try:
            frames = l23kit.load(name)
        except FileNotFoundError:
            frames = []
        folder = ROOT / where
        files = [f for f in folder.glob(f"{name}*.png") if f.stem == name or (
            f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
        src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        steps = glow_steps(frames) if add and frames else None
        ok = (len(frames) == n and (size is None or all(fr.size == size for fr in frames)) and src_ok
              and share < 0.05 and (steps is None or steps >= 32))
        total += atlas_area(frames)
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}, "
              f"checker {share * 100:.1f} %" + (f", {steps} glow steps" if steps is not None else ""))
    try:
        piv = load_pivots()
        ok = all(len(piv["aces"][a]) == N for a in ACES) and len(piv["wreck"]) == WRECK_N
    except (FileNotFoundError, KeyError):
        ok = False
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} pivots/{ROOT_NAME}.json")
    for path in [artkit.SPRITES / "intel" / "boss-honour-guard.png"] + [
            artkit.SPRITES / "portraits" / f"radio-{s}-neutral.png" for s in ("harker-guard", "vesper", "kestrel")]:
        ok = path.exists() and Image.open(path).text.get(artkit.SOURCE_KEY, "").startswith("tools/art/")
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {path.relative_to(ROOT)}")
    print(f"atlas: {total:,} px = {100 * total / 2048 ** 2:.1f} % of one 2048x2048 page (2 px padding)")
    return bad == 0


BUILDERS = {"aces": build_aces, "fx": build_fx, "intel": build_intel, "portraits": build_portraits}
REVIEWS = {"aces": review_aces, "fx": review_fx, "intel": review_intel, "portraits": review_portraits}

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    for s in subjects:
        if "--review" not in args:
            BUILDERS[s]()
        REVIEWS[s]()
