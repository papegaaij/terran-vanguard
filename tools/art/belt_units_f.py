#!/usr/bin/env python3
"""Production art: Level 33's units and objects (M9 part F batch, concept round 66, step ART-F2): the Void Leech
(design/enemies/space/void-leech, round 60 A.10 **a** "ringed leech"), the Rail Bunker (design/enemies/ground/
rail-bunker, the chosen r04 bunker with round 60's A.13 **a** solid lock line), the belt's rocks overgrown by the
Vrell (Level 33's drifting rocks: part B's small rocks and part C's large ones with the growth), the prospector's
shack *Claim 114* (Level 33's secret) and the CDF nav buoy that releases the armour patch.

The models are round 60's (tools/concept/enemies_r60.py ``leech_model``; tools/concept/render/enemy_models.py
``rail_bunker`` as enemies_r60.py's ``bunker_model`` reads it; tools/art/belt_units.py ``rock_model``;
tools/art/l25_props.py ``buoy_scene``; imported unchanged). The bunker in **Q.11 b**: gunmetal and charcoal (belt_units.py
``gunmetal``) with its gold coils and rim, red sensor light, the 1 px gold/red rim (belt_units.py ``rim``). The
**leech is Vrell** and keeps round 60's black-violet chitin and violet sac (as part B's Asteroid Mite kept its plum
and teal: Q.11 b is the Ascendancy's look), with a 1 px lavender light rim on the edges facing the sun so it reads
over the dark growth. The growth on the rocks is round 60's L33 growth (enemies_r60.py ``rock_bg``: black-violet
lumps, violet and teal specks) as a model: lumps on the rock's sunward side and a few violet leech sacs.

The prey Talon (QF-2 a, pending round 66): no frames of its own. The game can tint a sprite (SpriteBatch colour, as
DeepCoreLooks' dormant branch and the dimmed shield ring do), so the prey Talon is the Talon's own frame drawn
darkened (the review shows ``PREY_TINT``) and turned slowly (its heading frames cycling: the tumble); the leech on it
is ``void-leech-prey``, drawn over the Talon's frame at the Talon's centre.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its
own render under the fixed key light.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule, an
enemy's root once ``design/enemies/<layer>/<slug>/data.yaml`` exists, ``debris-<chunk>`` once a level's ``debris``
names the chunk, a ground target's or a drop's look once a level's data names it; pivots likewise):
  void-leech_0..63           32x32, 16 headings x 4 swim frames (heading * 4 + frame, 10 fps): five tapering
                             black-violet chitin rings, the sucker disc with its bone hooks, the violet sac on the back
  void-leech-latched_0..11   32x32, latched on the Stormhawk (spot * 4 + frame; spots 0 left, 1 right, 2 tail, the
                             Lamprey's): curled, facing in (left spot heading 12, right 4, tail 8), the sac pulsing
                             (frames 0..3 one pulse: dim, swell, full, fade; the game plays one pulse a second);
                             drawn at the ship's centre plus ``pivots/void-leech.json`` ``spots``
  void-leech-drain_0..3      32x32, additive: the violet glow at the mouth while it drains (the contact point)
  void-leech-prey_0..63      48x48 (the Talon's frame), latched on a prey Talon (talon heading * 4 + frame): the leech
                             curled on the Talon's back behind the cockpit, its sac pulsing; drawn at the Talon's
                             centre over the Talon's frame of the same heading
  void-leech-stunned_0..7    32x32, thrown off: curled, the sac dark, tumbling a full turn in 8 frames
  void-leech-death_0..7      48x48, additive: the `small` dry crack's violet spark (with explosion-small)
  void-leech-tatters_0..7    48x48: the chitin rings cracked apart and flung out, tumbling (4 steps a frame)
  rail-bunker                80x80: the hardened octagon in gunmetal with its gold rim and the fixed gold strip
  rail-bunker-turret_0..31   112x112, 32 headings: the turret block with the twin-rail barrel and its three gold
                             coils, the red sensor; drawn on the bunker's centre turned to its aim (``muzzle[k]``)
  rail-bunker-coils_0..31    112x112, additive: the coils' gold charge glow per heading (the game scales it with the
                             telegraph's progress, 0 -> 1 over the telegraph)
  rail-bunker-rail_0..3      32x14: the rail shot's strip (a dark rim, crimson, a white-hot core), tiled from the
                             muzzle along the locked line; 0-1 live (the 0.2 s), 2-3 fading
  rail-bunker-flash_0..3     32x32, additive: the muzzle flash at ``muzzle[k]`` when the rail fires
  rail-bunker-wreck          80x80: the cracked, scorched shell with the barrel's stump (the wreck that stays)
  debris-grown-<a|b|c>_0..15 44x44 / 40x40 / 36x36: part B's small belt rocks overgrown (16 spin turns)
  debris-grown-<a|b|c>-break_0..7  56x56: shot apart (part B's break with the growth's chips)
  debris-grown-large-<a|b>_0..15   96x96 / 72x72: part C's large rocks overgrown (walls: they do not break)
  claim-shack_0..3           48x48: the prospector's prefab shack on its pad, half grown over, its amber beacon
                             blinking (4 steps at 4 fps: on, fading, off, off)
  claim-shack-open_0..5      48x48: shot open: the roof thrown off, the walls burst, the strongbox's light rising out
                             (the crate pickup is the game's); the last frame stays (the burnt shell)
  nav-buoy_0..2              40x40: the CDF nav buoy (Level 25's moored buoy model in CDF grey with a blue band):
                             0 shut, 1 the hatch swinging open, 2 open and empty (BuoyLooks' three frames)
  nav-buoy-glow_0..3         40x40, additive: its white strobe (4 fps)
  pivots/void-leech.json, pivots/rail-bunker.json
  Review (round 66): design/enemies/space/concept/void-leech-final-r66-a.png/.gif, design/enemies/ground/concept/
  rail-bunker-final-r66-a.png/.gif, design/campaign/act-5-the-belt/level-33-leech-field/concept/
  grown-rocks-final-r66-a.png, ground-objects-final-r66-a.png/.gif (the shack and the buoy)

Run: python3 tools/art/belt_units_f.py [leech] [bunker] [rocks] [shack] [buoy] [--review] [--check]
     (no subject = all, a few minutes on 20 cores, renders cached in the system temp dir,
     conceptgame-cache/belt-units-f/, by the model code's hash; --review rebuilds only the review files from the
     written frames; --check lists every set with its folder, size and frame count and fails on a missing one or a
     missing Source chunk)
"""
import hashlib
import json
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import belt_units_c as buc
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged: the round-60 models)
from render import raster  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.sdf import Material, rotate_x, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

l23kit.claimed = bu.claimed                 # belt_units' rule (debris chunks) for this process

SCRIPT = "belt_units_f.py"
BATCH = "M9 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r66"
artkit.REVIEW_ROUND = ROUND
FPS = 20
A_X = e60.A_X
L33 = DESIGN / "campaign" / "act-5-the-belt" / "level-33-leech-field"
CONCEPT = {"leech": DESIGN / "enemies" / "space" / "concept", "bunker": DESIGN / "enemies" / "ground" / "concept",
           "rocks": L33 / "concept", "objects": L33 / "concept"}
BACKDROP = ROOT / "assets" / "backdrop" / "level-33"
write, load, where = bu.write, bu.load, bu.where
gunmetal, rim, turned, render = bu.gunmetal, bu.rim, bu.turned, bu.render
grid, field_img, screen_point = buc.grid, buc.field_img, buc.screen_point
VIOLET = np.array(e60.VIOLET, float)
CRIMSON = np.array(e60.CRIMSON, float)
GOLD = np.array((255, 196, 64), float)
AMBER = np.array((255, 170, 60), float)
LAVENDER = np.array((206, 184, 236), float)
PREY_TINT = (0.5, 0.46, 0.56)                # the prey Talon's darkening (multiplied, as SpriteBatch.setColor)


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def pivot_path(root):
    p = artkit.PIVOTS / f"{root}.json"
    return p if p.exists() else ROOT / "build" / "art-held" / "pivots" / f"{root}.json"


def pivots(root):
    return json.loads(pivot_path(root).read_text(encoding="utf-8"))


def light_rim(img, col=LAVENDER, k=0.45):
    """A 1 px light rim on the silhouette's pixels facing the top-left sun (the Vrell units' readability rim)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    a[tl, :3] = a[tl, :3] * (1 - k) + col * k
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== the Void Leech (A.10 a)

LEECH_SIZE, LEECH_EXT = 32, 2.3               # round 60's sprite scale (32 px for 2.3 units)
SPOTS = {"left": (-21, 13), "right": (20, 13), "tail": (0, 34)}      # the Lamprey's (pivots/lamprey.json)
SPOT_HEADING = {"left": 12, "right": 4, "tail": 8}
PULSE = (0.15, 0.55, 1.0, 0.5)                # one sac pulse in 4 frames
TAL_SIZE, TAL_EXT = bu.TAL_SIZE, bu.TAL_EXT
PREY_AT = (0.05, 0.1)                         # Talon model units: the leech's mouth on the Talon's back
PREY_YAW = 0.3                                # the leech lies along the spine, its mouth toward the cockpit


def _leech(k, f, bend=0.0, pulse=0.35, glow=1.0, n=16):
    scene, mats = e60.leech_model("a", phase=f * TAU / 4, bend=bend, pulse=pulse, glow=glow)
    return render(*turned(scene, mats, k, n), LEECH_SIZE, LEECH_EXT, crisp=60)


def _leech_latched(spot, f):
    return _leech(SPOT_HEADING[spot], 1, bend=1.0, pulse=PULSE[f])


def _leech_stunned(i):
    return _leech(2 * i, 2, bend=1.0, pulse=0.0, glow=0.25)


def _talon_top(x, y):
    """The Talon's upper surface height (model units) over the model point (x, y): its SDF sampled down a column."""
    scene, _ = e60.talon_model("b", False)
    zs = np.linspace(1.2, -0.4, 161)
    p = np.stack([np.full_like(zs, x), np.full_like(zs, y), zs], axis=-1)
    d, _ = scene(p)
    inside = np.nonzero(d < 0)[0]
    return float(zs[inside[0]]) if len(inside) else 0.2


def _leech_prey(k, f):
    """The leech on a Talon at Talon heading k: the leech model scaled into the Talon's units, curled on its back."""
    scene0, mats = e60.leech_model("a", phase=TAU / 4, bend=1.0, pulse=PULSE[f])
    s = (LEECH_SIZE / LEECH_EXT) / (TAL_SIZE / TAL_EXT)              # leech units -> Talon units
    z = _talon_top(*PREY_AT) + 0.08
    mouth = 0.85 * s                                                # the sucker's centre ahead of the leech's origin
    ox = PREY_AT[0] - np.sin(-PREY_YAW) * mouth
    oy = PREY_AT[1] - np.cos(-PREY_YAW) * mouth

    def scene(p):
        q = rotate_z(p - np.array((ox, oy, z)), -PREY_YAW)
        q = rotate_x(q, 0.25) / s                                   # the mouth tipped down onto the hull
        d, m = scene0(q)
        return d * s, m
    return render(*turned(scene, mats, k, 16), TAL_SIZE, TAL_EXT, crisp=60)


def drain_glow(i):
    """The violet glow at the mouth while it drains: a soft disc and motes drawn in (4 steps)."""
    x, y = grid(32)
    r = np.hypot(x, y)
    k = (0.7, 1.0, 0.85, 0.6)[i]
    f = np.exp(-(r / 4.5) ** 2) * k
    rng = np.random.default_rng(3301 + i)
    for _ in range(7):
        a, d0 = rng.uniform(0, TAU), rng.uniform(5, 13) * (1 - 0.18 * i)
        f += np.exp(-((x - np.cos(a) * d0) ** 2 + (y - np.sin(a) * d0) ** 2) / 1.1) * rng.uniform(0.4, 0.9)
    return field_img(f, VIOLET, white=0.75)


def death_spark(i, n=8):
    """The dry crack: a short violet-white spark burst and a ring, no smoke (vacuum)."""
    x, y = grid(48)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    u = (i + 1) / n
    f = np.exp(-(r / (3 + 6 * u)) ** 2) * (1 - u) * 1.2
    f += np.exp(-((r - 18 * u) / 1.3) ** 2) * (1 - u) * 0.5
    rays = np.clip(np.cos(a * 5 + 1.3) ** 16, 0, 1) * np.exp(-((r - 16 * u) / (5 + 6 * u)) ** 2)
    f += rays * (1 - u) * 0.9
    return field_img(f, VIOLET, white=0.7)


def tatters(body, i, n=8):
    """The chitin rings cracked apart: five dark shards in the body's own colours flung out, tumbling, fading."""
    w = 48
    img = Image.new("RGBA", (w, w))
    d = ImageDraw.Draw(img)
    src = np.array(body)
    cols = src[src[..., 3] > 0][:, :3]
    cols = cols[np.argsort(cols.sum(1))]
    u = (i + 1) / n
    rng = np.random.default_rng(3307)
    for k in range(6):
        a = k * TAU / 6 + rng.uniform(-0.3, 0.3)
        dist = (3 + 15 * u) * rng.uniform(0.7, 1.1)
        cx, cy = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
        s = rng.uniform(2.4, 4.0) * (1 - 0.35 * u)
        spin = a + 2.5 * u * (1 if k % 2 else -1)
        pts = [(cx + np.cos(spin + j * TAU / 3) * s * (1.3 if j == 0 else 0.8), cy + np.sin(spin + j * TAU / 3) * s)
               for j in range(3)]
        col = cols[int(len(cols) * (0.8 if np.cos(a - 3.9) > 0 else 0.35))]
        alpha = 255 if u < 0.75 else int(255 * (1 - u) * 4)
        d.polygon(pts, fill=tuple(int(c) for c in col) + (max(alpha, 0),))
    return img


def build_leech():
    jobs = [("leech", k, f) for k in range(16) for f in range(4)]
    jobs += [("latched", s, f) for s in SPOTS for f in range(4)]
    jobs += [("prey", k, f) for k in range(16) for f in range(4)]
    jobs += [("stunned", i) for i in range(8)]
    res = run(jobs)
    body = artkit.quantize_set([light_rim(f) for f in res[:64]], 28)
    save_set("void-leech", body)
    save_set("void-leech-latched", artkit.quantize_set([light_rim(f) for f in res[64:76]], 28))
    save_set("void-leech-prey", artkit.quantize_set([light_rim(f) for f in res[76:140]], 28))
    save_set("void-leech-stunned", artkit.quantize_set([light_rim(f, k=0.3) for f in res[140:148]], 24))
    save_set("void-leech-drain", [drain_glow(i) for i in range(4)])
    save_set("void-leech-death", [death_spark(i) for i in range(8)])
    save_set("void-leech-tatters", artkit.quantize_set([tatters(body[0], i) for i in range(8)], 16))
    write_pivots("void-leech", {
        "headings": 16, "swim_frames": 4, "latched_index": "spot * 4 + frame (spots left, right, tail)",
        "prey_index": "talon heading * 4 + frame, 48x48 at the Talon's centre", "stunned_frames": 8,
        "ship_frame": "ship_2", "spots": {k: list(v) for k, v in SPOTS.items()},
        "mouth": {k: [v[0] - 13 * (v[0] > 0) + 13 * (v[0] < 0), v[1] - (13 if k == "tail" else 0)]
                  for k, v in SPOTS.items()},
        "note": "spots: where a latched frame's centre sits relative to the ship's centre (px, y down; the Lamprey's "
                "spots); mouth: the contact point (void-leech-drain's centre) relative to the ship's centre. The prey "
                "frames are the Talon's size: draw them at the Talon's centre after its frame of the same heading"})


# =========================================================================== the Rail Bunker (r04 + A.13 a)

RB_PX = 48.0                                   # px per model unit: the octagon (radius 0.72 + the rim) about 72 px
RB_SIZE, RB_EXT = 80, 80 / RB_PX
TUR_SIZE, TUR_EXT = 112, 112 / RB_PX
MUZZLE = (0.0, 1.06)                           # model units: the rails' tip (+Y forward)
OCTA = [(np.cos(a) * 0.72, np.sin(a) * 0.72) for a in np.linspace(np.pi / 8, 2 * np.pi + np.pi / 8, 8,
                                                                   endpoint=False)]


def _rb_mats(charge=0.0):
    coil = Material(em.GOLD[3], metal=0.9, shininess=120, spec=1.2,
                    emission=tuple(np.array((1.0, 0.75, 0.25)) * 1.8 * charge))
    mats = gunmetal(e60.belt_mats("6a6e78", 1.0, extra=[coil]))
    mats[A_GOLD] = replace(mats[A_GOLD], albedo=(0.78, 0.6, 0.24))
    return mats


def _rb_base_scene(wreck=False):
    """The hardened octagon (r04's plates) with the Q11 gold rim round its edge and the fixed gold strip; the wreck
    cracked (a seeded crack pattern cut into the roof) with the turret gone."""
    inner = [(x * 0.6, y * 0.6) for x, y in OCTA]

    def scene(p):
        roof = lambda x, y: np.clip(1.0 - 0.9 * np.sqrt(x * x + y * y), 0.35, 1.0)
        d, m = union((em.sd_plate(p, OCTA, 0.1, 0.18, 0.0, taper=roof), A_HULL),
                     (em.sd_plate(p, inner, 0.28, 0.08, 0.0), A_FACET),
                     (sd_box(p, (0.0, -0.62, 0.2), (0.22, 0.05, 0.03), 0.0), A_GOLD),
                     (sd_cylinder_z(p, (0, 0, 0.36), 0.24, 0.04), A_GUN))
        r = np.hypot(p[:, 0], p[:, 1])
        m = np.where((m == A_HULL) & (r > 0.6), A_GOLD, m)
        for a in np.linspace(0, TAU, 4, endpoint=False) + TAU / 8:   # red running lights on four corners
            dl = sd_sphere(p, (0.66 * np.cos(a), 0.66 * np.sin(a), 0.2), 0.045)
            m = np.where(dl < d, A_RED, m)
            d = np.minimum(d, dl)
        if wreck:
            ang = np.arctan2(p[:, 1], p[:, 0])
            crack = np.abs(np.sin(ang * 3 + 0.7) * r - 0.05) - 0.025
            d = np.maximum(d, -np.where(p[:, 2] > 0.2, crack, 1.0))
            d = np.maximum(d, -(np.hypot(p[:, 0] - 0.05, p[:, 1] + 0.02) - 0.3))      # the turret's well torn open
        return d, m
    return scene


def _rb_turret_scene(stump=False):
    """r04's turret in its own frame (+Y forward): the block, the twin rails, three gold coils (the A_X slot: they
    glow with the charge), the red sensor."""
    def scene(p):
        q = p.copy()
        q[:, 0] = np.abs(p[:, 0])                                   # mirrored: the twin rails
        length = 0.16 if stump else 0.42
        items = [(sd_box(p, (0, 0.05, 0.4), (0.2, 0.26, 0.08), 0.02), A_FACET),
                 (sd_box(q, (0.07, 0.2 + length, 0.42), (0.025, length, 0.03), 0.0), A_GUN),
                 (sd_sphere(p, (0, -0.12, 0.5), 0.06), A_RED)]
        if not stump:
            items += [(sd_box(p, (0, y, 0.43), (0.12, 0.035, 0.05), 0.0), A_X) for y in (0.34, 0.55, 0.76)]
        else:
            items.append((sd_box(p, (0, 0.34, 0.43), (0.12, 0.035, 0.05), 0.0), A_GOLD))
        return union(*items)
    return scene


def _rb_base(wreck):
    return render(*turned(_rb_base_scene(wreck), _rb_mats(), 0, 32), RB_SIZE, RB_EXT)


def _rb_turret(k, stump=False):
    return render(*turned(_rb_turret_scene(stump), _rb_mats(), k, 32), TUR_SIZE, TUR_EXT)


def _rb_coils(k):
    mats = bu.emission_only(_rb_mats(1.0))
    for i in range(len(mats)):
        if i != A_X:
            mats[i] = Material((0, 0, 0))
    return render(*turned(_rb_turret_scene(), mats, k, 32), TUR_SIZE, TUR_EXT, crisp=0)


def rail_strip(i, w=32, h=14):
    """The rail shot as a strip tiled along the line: a dark rim (12 px), crimson (10), a white-hot core (4); frames
    0-1 live (a faint flicker along it), 2-3 fading."""
    k = (1.0, 0.95, 0.6, 0.3)[i]
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    dy = np.abs(yy - h / 2 - 0.25 * np.sin(xx / w * TAU * 2 + i * 1.9))
    rgb = np.zeros((h, w, 3))
    alpha = np.zeros((h, w))
    rimz, red, core = dy <= 6, dy <= 5, dy <= (2 if i < 2 else 1.4)
    rgb[rimz] = (40, 6, 10)
    alpha[rimz] = 0.9
    rgb[red] = CRIMSON * (0.85 + 0.15 * np.sin(xx[red] / w * TAU * 3 + i))[:, None]
    alpha[red] = 1.0
    rgb[core] = (255, 236, 220)
    alpha *= k
    return Image.fromarray(np.dstack([rgb, alpha * 255]).astype(np.uint8), "RGBA")


def rail_flash(i):
    x, y = grid(32)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    k = (1.0, 0.8, 0.5, 0.25)[i]
    f = np.exp(-(r / (4 + 2 * i)) ** 2) * 1.1 * k + np.clip(np.cos(a * 3) ** 12, 0, 1) * np.exp(-(r / 10) ** 2) * k
    return field_img(f, CRIMSON, white=0.6)


def build_bunker():
    jobs = [("rbbase", False), ("rbbase", True)] + [("rbturret", k) for k in range(32)] + [("rbcoils", k) for k in
                                                                                         range(32)]
    jobs.append(("rbstump", 5))
    res = run(jobs)
    base = artkit.quantize_set([rim(res[0])], 32)
    save_set("rail-bunker", base, single=True)
    tur = artkit.quantize_set([rim(f) for f in res[2:34]], 36)
    note_gold("rail-bunker (base + turret)", [composite(base[0], t) for t in tur])
    save_set("rail-bunker-turret", tur)
    save_set("rail-bunker-coils", [artkit.additive(f) for f in res[34:66]])
    wreck = buc.scorch(rim(res[1]), 3311, 0.5)
    wreck.alpha_composite(buc.scorch(res[66], 3313, 0.45).crop((16, 16, 96, 96)))
    save_set("rail-bunker-wreck", artkit.quantize_set([wreck], 28), single=True)
    save_set("rail-bunker-rail", [rail_strip(i) for i in range(4)])
    save_set("rail-bunker-flash", [rail_flash(i) for i in range(4)])
    write_pivots("rail-bunker", {
        "size": [RB_SIZE, RB_SIZE], "turret": [TUR_SIZE, TUR_SIZE], "headings": 32,
        "muzzle": [screen_point(MUZZLE, k, 32, TUR_SIZE, TUR_EXT) for k in range(32)],
        "hull": [72, 72],
        "note": "muzzle[k]: the rails' tip in turret frame k ([x, y] px from the turret frame's top left; the turret "
                "frame is centred on the bunker's centre): the lock line and the rail start there, the flash is "
                "centred on it. The coils' glow frame k goes over turret frame k, scaled by the charge"})


def composite(base, top):
    out = Image.new("RGBA", top.size)
    ox = (top.width - base.width) // 2
    out.alpha_composite(base, (ox, ox))
    out.alpha_composite(top)
    return out


# =========================================================================== the grown rocks

SMALL = {"a": (44, 0.92, 11), "b": (40, 0.82, 23), "c": (36, 0.74, 37)}       # part B's (belt_units.ROCKS)
LARGE = {"large-a": (96, 2.15, 61), "large-b": (72, 1.58, 67)}               # part C's (belt_units_c.ROCKS)
GROWTH = (0.17, 0.08, 0.2)                     # round 60's growth (2e1636 .. 46 22 54)


def grown_rock_model(radius, seed):
    """Part B's belt rock with the Leech Field's growth: black-violet lumps clustered on its sunward cap (seeded
    clusters of small spheres pushed out of the surface), violet glints in them and a leech sac (violet
    emissive bulbs; the larger rocks three)."""
    scene0, mats0 = bu.rock_model(radius, seed)
    rng = np.random.default_rng(seed + 500)
    blobs, sacs = [], []
    for c in range(2 if radius < 1.2 else 4):
        v = rng.normal(size=3)
        v[2] = abs(v[2]) + 0.6
        v /= np.linalg.norm(v)
        for _ in range(14):
            w = v + rng.normal(size=3) * 0.38
            w /= np.linalg.norm(w)
            blobs.append((w * radius * 0.8, radius * rng.uniform(0.07, 0.13)))
        w = v + rng.normal(size=3) * 0.2
        w /= np.linalg.norm(w)
        sacs.append((w * radius * 0.86, radius * (0.1 if radius < 1.2 else 0.075)))
    sacs = sacs[:1 if radius < 1.2 else 3]

    def speckle(p, n):
        cell = np.floor(p * 9.0)
        h = np.sin(cell @ np.array((12.9898, 78.233, 37.719)) + seed) * 43758.5453
        h = h - np.floor(h)
        return 0.75 + 0.5 * (h > 0.93)
    mats = list(mats0) + [Material(GROWTH, metal=0.2, shininess=90, spec=0.9, pattern=speckle),
                          Material((0.25, 0.1, 0.32), metal=0.1, shininess=60, spec=0.8,
                                   emission=tuple(VIOLET / 255.0 * 0.9))]

    def scene(p):
        d, m = scene0(p)
        g = np.full(len(p), 1e9)
        for c, r in blobs:
            g = np.minimum(g, np.linalg.norm(p - c, axis=-1) - r)
        g = np.maximum(g, -(d + 0.02))                              # the growth hugs the rock (no floating lumps)
        m = np.where(g < d, 2, m)
        d = np.minimum(d, g)
        for c, r in sacs:
            ds = np.linalg.norm(p - c, axis=-1) - r
            m = np.where(ds < d, 3, m)
            d = np.minimum(d, ds)
        return d, m
    return scene, mats


def _grown(name, k):
    size, radius, seed = {**SMALL, **LARGE}[name]
    scene, mats = grown_rock_model(radius, seed)
    return render(*turned(scene, mats, k, 16), size, size / bu.PX, crisp=70)


def build_rocks():
    names = list(SMALL) + list(LARGE)
    res = run([("grown", n, k) for n in names for k in range(16)])
    for i, n in enumerate(names):
        frames = artkit.quantize_set(res[16 * i:16 * (i + 1)], 28)
        save_set(f"debris-grown-{n}", frames)
        if n in SMALL:
            save_set(f"debris-grown-{n}-break", artkit.quantize_set([bu.rock_break(frames, f) for f in range(8)], 24))


# =========================================================================== Claim 114 and the nav buoy

SHACK = 48
SHACK_EXT = SHACK / 20.0


def shack_mats(beacon=1.0, inside=0.0):
    return [Material((0.62, 0.62, 0.6), metal=0.3, shininess=40, spec=0.4),        # 0 the prefab's pale panels
            Material((0.36, 0.37, 0.38), metal=0.5, shininess=50, spec=0.5),       # 1 steel: frame, mast, pad
            Material((0.5, 0.36, 0.12), metal=0.2, shininess=30, spec=0.3),        # 2 the claim stripe (faded ochre)
            Material((0.12, 0.16, 0.3), metal=0.4, shininess=110, spec=1.0),       # 3 the solar panel
            Material((0.12, 0.08, 0.04), emission=tuple(AMBER / 255.0 * (0.2 + 1.6 * beacon))),   # 4 the beacon
            Material(GROWTH, metal=0.2, shininess=90, spec=0.9),                   # 5 the growth
            Material((0.4, 0.33, 0.27), shininess=10, spec=0.1),                   # 6 the rock pad
            Material((0.05, 0.04, 0.03), emission=tuple(AMBER / 255.0 * 1.4 * inside))]   # 7 the strongbox's light


def shack_scene(broken=0.0):
    """Claim 114 from above: a prefab cabin (pale ribbed panels, a faded ochre claim stripe) on a cut rock pad, a solar
    panel on a strut, the antenna mast with the amber beacon on its tip; the growth creeping over its right end.
    ``broken`` 0 -> 1: the roof thrown off, the walls burst, the strongbox's light inside."""
    rng = np.random.default_rng(3321)
    blobs = [((0.62 + rng.uniform(-0.1, 0.3), rng.uniform(-0.5, 0.4), 0.18), rng.uniform(0.08, 0.15))
             for _ in range(9)]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, -0.05), 1.05, 0.1), 6)]
        if broken < 1:
            roof_up = broken * 0.8
            items += [(sd_box(p, (-0.05, 0.0, 0.32 + roof_up), (0.62, 0.38, 0.06), 0.03), 0),
                      (sd_box(p, (-0.05, 0.0, 0.39 + roof_up), (0.64, 0.05, 0.02)), 1),
                      (sd_box(p, (-0.62, 0.0, 0.36 + roof_up), (0.05, 0.4, 0.03)), 2)]
        items += [(sd_box(p, (-0.05, 0.0, 0.16), (0.6, 0.36, 0.16 * (1 - 0.6 * broken)), 0.02), 0 if not broken else 1),
                  (sd_box(p, (-0.62, -0.62, 0.32), (0.28, 0.18, 0.02)), 3),
                  (sd_capsule(p, (-0.62, -0.62, 0.0), (-0.62, -0.62, 0.3), 0.03), 1),
                  (sd_capsule(p, (-0.45, 0.45, 0.0), (-0.42, 0.48, 0.75), 0.03), 1),
                  (sd_sphere(p, (-0.42, 0.48, 0.78), 0.07), 4)]
        if broken:
            items.append((sd_box(p, (0.0, 0.05, 0.3), (0.16, 0.12, 0.08), 0.02), 7))
        for c, r in blobs:
            items.append((sd_sphere(p, c, r), 5))
        return union(*items)
    return scene


def _shack(beacon, broken):
    return render(*turned(shack_scene(broken), shack_mats(beacon, broken), 8, 16), SHACK, SHACK_EXT, crisp=70)


def shack_burst(img, i, n=6):
    """The shack shot open: panels flung out, a short grey dust ring (2D over the broken render)."""
    out = img.copy()
    u = (i + 1) / n
    d = ImageDraw.Draw(out)
    rng = np.random.default_rng(3323)
    if u < 0.9:
        rr = 6 + 16 * u
        d.ellipse([24 - rr, 24 - rr, 24 + rr, 24 + rr], outline=(150, 140, 130, int(160 * (1 - u))), width=2)
    for k in range(8):
        a = k * TAU / 8 + rng.uniform(-0.3, 0.3)
        dist = (6 + 16 * u) * rng.uniform(0.7, 1.1)
        cx, cy = 24 + np.cos(a) * dist, 24 + np.sin(a) * dist
        if u > 0.85 and k % 2:
            continue
        s = rng.uniform(1.6, 3.0)
        spin = a + 3 * u
        pts = [(cx + np.cos(spin + j * TAU / 4) * s * (1.4 if j % 2 else 0.6), cy + np.sin(spin + j * TAU / 4) * s)
               for j in range(4)]
        d.polygon(pts, fill=(186, 184, 178, 255) if np.cos(a - 3.9) > 0 else (96, 96, 92, 255))
    return out


BUOY = 40


def _buoy(state):
    import l25_props as p25  # noqa: E402  (tools/art: Level 25's buoy model; imported here, it installs its own rule)
    import l24_props as p24  # noqa: E402
    l23kit.claimed = bu.claimed
    mats = [Material((0.56, 0.58, 0.6), metal=0.45, shininess=55, spec=0.6),      # 0 the CDF grey float
            Material((0.18, 0.3, 0.55), metal=0.25, shininess=40, spec=0.5),      # 1 the CDF blue band
            Material((0.4, 0.42, 0.44), metal=0.6, shininess=50, spec=0.6),       # 2 steel: mast, reflector
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),       # 3 the empty hatch
            Material((0.46, 0.48, 0.5), metal=0.4, shininess=40, spec=0.5),       # 4 the hatch door
            Material((0.9, 0.92, 0.95), metal=0.2, shininess=120, spec=1.2)]      # 5 the strobe's lens
    return p24.rimmed(p24.render(p25.buoy_scene(state), mats, (BUOY, BUOY), z_top=40))


def buoy_glow(i):
    import l25_props as p25  # noqa: E402
    l23kit.claimed = bu.claimed
    img = np.array(p25.buoy_glow(i)).astype(np.float64)              # Level 25's strobe, made CDF white
    lum = img[..., :3].max(axis=-1, keepdims=True)
    img[..., :3] = lum * np.array((0.9, 0.95, 1.0))
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def build_shack():
    res = run([("shack", b, 0.0) for b in (1.0, 0.45, 0.0)] + [("shack", 0.0, u) for u in (0.35, 0.7, 1.0)])
    shut = artkit.quantize_set([res[0], res[1], res[2], res[2]], 32)
    save_set("claim-shack", shut)
    opened = [shack_burst(res[3], 0), shack_burst(res[4], 1), shack_burst(res[5], 2), shack_burst(res[5], 3),
              buc.scorch(shack_burst(res[5], 4), 3325, 0.75), buc.scorch(res[5], 3327, 0.6)]
    save_set("claim-shack-open", artkit.quantize_set(opened, 32))


def build_buoy():
    res = run([("buoy", s) for s in range(3)])
    save_set("nav-buoy", artkit.quantize_set(res, 32))
    save_set("nav-buoy-glow", [buoy_glow(i) for i in range(4)])


# =========================================================================== jobs

JOBS = {"leech": _leech, "latched": _leech_latched, "prey": _leech_prey, "stunned": _leech_stunned,
        "rbbase": _rb_base, "rbturret": _rb_turret, "rbcoils": _rb_coils,
        "rbstump": lambda k: _rb_turret(k, stump=True), "grown": _grown, "shack": _shack, "buoy": _buoy}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "belt-units-f"


def _model_key():
    """The renders depend on the code above the review section (and the imported concept models)."""
    text = Path(__file__).read_text(encoding="utf-8").split("# " + "=" * 75 + " review")[0]
    return hashlib.sha1(text.encode()).hexdigest()[:12]


def run(jobs):
    """Renders the jobs in parallel, cached in the system temp dir by the model code's hash and the job."""
    key = _model_key()
    paths = [CACHE / f"{hashlib.sha1((key + repr(j)).encode()).hexdigest()}.png" for j in jobs]
    todo = [i for i, p in enumerate(paths) if not p.exists()]
    if todo:
        CACHE.mkdir(parents=True, exist_ok=True)
        with ProcessPoolExecutor() as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(p).convert("RGBA") for p in paths]


GOLDS = {}


def note_gold(name, frames):
    shares = [e60.gold_share(f) for f in frames]
    GOLDS[name] = (min(shares), max(shares))
    print(f"gold {name}: {100 * min(shares):.0f}-{100 * max(shares):.0f} %")


BUILDERS = {"leech": build_leech, "bunker": build_bunker, "rocks": build_rocks, "shack": build_shack,
            "buoy": build_buoy}


# =========================================================================== review

paste, add = buc.paste, buc.add


def plate(w=480, h=540):
    """The review ground: Level 33's production field (open space with a grown face) when backdrop_l33.py has written
    it, else round 60's grown rock."""
    space, face = BACKDROP / "field-space.png", BACKDROP / "grown-face-1.png"
    if space.exists() and face.exists():
        src = Image.open(space).convert("RGBA")
        fc = Image.open(face).convert("RGBA")
        out = Image.new("RGBA", (w, h))
        for x in range(0, w, src.width):
            for y in range(0, h, src.height):
                out.alpha_composite(src.crop((0, 0, min(src.width, w - x), min(src.height, h - y))), (x, y))
            for y in range(0, h, fc.height):
                out.alpha_composite(fc.crop((0, 0, min(fc.width, w - x), min(fc.height, h - y))), (x, y))
        return out
    col = e60.rock_bg(331, h, growth=True).convert("RGBA")
    out = Image.new("RGBA", (w, h))
    for x in range(0, w, col.width):
        out.alpha_composite(col.crop((0, 0, min(col.width, w - x), h)), (x, 0))
    return out


def caption(img, text):
    e60.caption(img, text)
    return img


def save_review(kind, subject, sheet, gif=None):
    artkit.REVIEW_ROUND = ROUND                 # (importing l25_props for the buoy sets its own round)
    png, gif_path = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    msg = f"review: {png.relative_to(ROOT)}"
    if gif:
        size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
        msg += f", {gif_path.name} ({size / 1e6:.1f} MB)"
    print(msg)


def explosion(rung):
    try:
        return artkit.load_frames(f"explosion-{rung}")
    except Exception:
        return []


def sheet_of(title, sub, rows, notes, width=1000):
    hgt = 70 + sum(r[1].height + 26 for r in rows) + 16 * len(notes) + 10
    sh = raster.sheet(width, hgt, title, sub)
    y = 40
    for label, img in rows:
        raster.draw_text(sh, 16, y, label, raster.LABEL)
        sh.alpha_composite(img.convert("RGBA"), (16, y + 14))
        y += img.height + 26
    for line in notes:
        raster.draw_text(sh, 16, y, line, raster.LABEL_DIM)
        y += 16
    return sh


def row_of(frames, w=968, gap=6, scale=1, bg=None):
    frames = [f.resize((f.width * scale, f.height * scale), Image.NEAREST) if scale != 1 else f for f in frames]
    h = max(f.height for f in frames) + 8
    img = (bg if bg is not None else plate(w, max(h, 540))).crop((0, 0, w, h))
    x = 4
    for f in frames:
        if x + f.width > w:
            break
        img.alpha_composite(f, (x, 4))
        x += f.width + gap
    return img


def tinted(img, tint=PREY_TINT):
    a = np.array(img).astype(np.float64)
    a[..., :3] *= np.array(tint)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def ship_frame():
    return artkit.load_frames("ship")[2]


def review_leech():
    body = load("void-leech")
    lat = load("void-leech-latched")
    prey = load("void-leech-prey")
    stun = load("void-leech-stunned")
    drain = load("void-leech-drain")
    death, tat = load("void-leech-death"), load("void-leech-tatters")
    talon = artkit.load_frames("talon")
    small = explosion("small")
    pv = pivots("void-leech")
    spots = pv["spots"]
    ship = ship_frame()

    def ship_with(n, f):
        cell = Image.new("RGBA", (96, 96))
        cell.alpha_composite(ship, (48 - ship.width // 2, 40 - ship.height // 2))
        for i, s in enumerate(("left", "right", "tail")[:n]):
            ox, oy = spots[s]
            cell = paste(cell, lat[i * 4 + f], 48 + ox, 40 + oy)
            mx, my = pv["mouth"][s]
            cell = add(cell, drain[f], 48 + mx, 40 + my, 0.8)
        return cell

    def prey_cell(k, f):
        cell = Image.new("RGBA", (48, 48))
        cell.alpha_composite(tinted(talon[k]))
        cell.alpha_composite(prey[k * 4 + f])
        return cell

    bg = plate(968, 540)
    rows = [("16 HEADINGS, SWIM FRAME 0 (1X), THEN THE 4 SWIM FRAMES AT HEADING 0 (2X)",
             row_of([body[k * 4] for k in range(16)] + [b.resize((64, 64), Image.NEAREST) for b in body[:4]], bg=bg)),
            ("LATCHED ON THE STORMHAWK: ONE, TWO, THREE (SPOTS LEFT, RIGHT, TAIL; THE SAC'S PULSE, 4 FRAMES) (1X, 2X)",
             row_of([ship_with(1, 2), ship_with(2, 1), ship_with(3, 2)] +
                    [ship_with(3, f).resize((192, 192), Image.NEAREST) for f in (0, 2)], bg=bg)),
            ("ON A PREY TALON: THE TALON DARKENED (THE GAME'S TINT), THE LEECH ON ITS BACK AT 8 OF 16 HEADINGS; 2X",
             row_of([prey_cell(k, 2) for k in range(0, 16, 2)] +
                    [prey_cell(k, f).resize((96, 96), Image.NEAREST) for k, f in ((0, 0), (0, 2), (5, 2))], bg=bg)),
            ("STUNNED (8 FRAMES, THE TUMBLE), THE DRAIN GLOW (ADD.), THE DEATH: SPARK (ADD.) + SHARDS OVER EXPLOSION-SMALL",
             row_of(stun + [d.resize((32, 32)) for d in drain] + [death_cell(small, death, tat, i) for i in range(0, 8, 2)],
                    bg=bg))]
    gold_free = "VRELL: NO GOLD (BLACK-VIOLET CHITIN, THE VIOLET SAC); A 1 PX LAVENDER RIM ON THE SUNWARD EDGE"
    notes = ["ROUND 60'S A.10 A (RINGED LEECH) AS PRODUCTION FRAMES. " + gold_free + ".",
             "THE PREY TALON HAS NO FRAMES OF ITS OWN (QF-2 A): ITS OWN SPRITE DRAWN DARKENED (TINT 0.50/0.46/0.56) AND",
             "TURNING SLOWLY, THE LEECH'S PREY FRAME OVER IT AT THE SAME HEADING."]
    sheet = sheet_of("VOID LEECH (FINAL R66): SWIM, LATCH, PREY, STUN", f"PRODUCTION ART, {BATCH.upper()} - R66",
                     rows, notes)
    gif = leech_gif(body, lat, prey, stun, drain, death, tat, talon, small, spots, pv["mouth"])
    save_review("leech", "void-leech", sheet, gif)


def death_cell(small, death, tat, i):
    cell = Image.new("RGBA", (48, 48))
    if small:
        cell = add(cell, small[min(i, len(small) - 1)], 24, 24)
    cell = paste(cell, tat[i], 24, 24)
    return add(cell, death[i], 24, 24)


def _bez(a, b, c, u):
    u = min(max(u, 0.0), 1.0)
    return (1 - u) ** 2 * np.array(a) + 2 * (1 - u) * u * np.array(b) + u * u * np.array(c)


def leech_gif(body, lat, prey, stun, drain, death, tat, talon, small, spots, mouths):
    """6 s at 480x400: a prey Talon drifting down darkened with a leech on its back; two leeches coming in from
    behind and the side, latching left and tail; the shake (four reversals), both thrown off stunned; one shot."""
    base = plate(480, 400)
    frames = []
    W_, H_ = 480, 400
    for i in range(int(6.0 * FPS)):
        t = i / FPS
        f = base.copy()
        sx = 240 + 20 * np.sin(t * 0.7)
        if 4.0 <= t < 4.8:
            sx += 22 * np.sin(TAU * (t - 4.0) / 0.4)
        sy = 300
        # the prey Talon drifting down, tumbling slowly
        ty = 40 + 60 * t
        tk = int(t * 2) % 16
        fr = Image.new("RGBA", (48, 48))
        fr.alpha_composite(tinted(talon[tk]))
        fr.alpha_composite(prey[tk * 4 + int(t * 4) % 4])
        f = paste(f, fr, 380 + 6 * np.sin(t), ty)
        latched = []
        for (start, ctrl, spot, t0, t1) in (((60, H_ + 20), (80, 360), "left", 0.2, 1.8),
                                            ((W_ + 20, 330), (360, 380), "tail", 0.8, 2.6)):
            ox, oy = spots[spot]
            target = (sx + ox, sy + oy)
            if t < t1:
                u = (t - t0) / (t1 - t0)
                if u < 0:
                    continue
                p = _bez(start, ctrl, target, u)
                q = _bez(start, ctrl, target, min(1, u + 0.05))
                h = np.arctan2(q[1] - p[1], q[0] - p[0])
                k = int(round((h - np.pi / 2) / (TAU / 16))) % 16
                f = paste(f, body[k * 4 + int(t * 10) % 4], p[0], p[1])
            elif t < 4.8:
                latched.append(spot)
            else:
                u = t - 4.8
                if spot == "left" and u > 0.5:
                    if u < 0.9:
                        j = int((u - 0.5) / 0.4 * 8)
                        px_, py_ = sx + ox * 2.2, sy + oy * 2.2
                        f = add(f, small[min(j, len(small) - 1)], px_, py_) if small else f
                        f = paste(f, tat[min(j, 7)], px_, py_)
                        f = add(f, death[min(j, 7)], px_, py_)
                    continue
                d = 1.6 + 0.6 * u
                f = paste(f, stun[int(u * 8) % 8], sx + ox * d, sy + oy * d + 40 * max(0, u - 1))
        f = paste(f, ship_frame(), sx, sy)
        for spot in latched:
            ox, oy = spots[spot]
            fk = int(t * 4) % 4
            f = paste(f, lat[["left", "right", "tail"].index(spot) * 4 + fk], sx + ox, sy + oy)
            mx, my = mouths[spot]
            f = add(f, drain[fk], sx + mx, sy + my, 0.8)
        if latched:
            raster.draw_text(f, int(sx) + 30, sy - 8, f"GEN -{2 * len(latched)} MW", (200, 160, 255))
        if 4.0 <= t < 4.8:
            caption(f, "SHAKE: FOUR REVERSALS IN 1.2 S")
        elif t < 1.5:
            caption(f, "PREY: A TALON WITH A LEECH DRIFTS DOWN, DARK")
        frames.append(f)
    return frames


def review_bunker():
    base = load("rail-bunker")[0]
    tur = load("rail-bunker-turret")
    coils = load("rail-bunker-coils")
    wreck = load("rail-bunker-wreck")[0]
    strip, flash = load("rail-bunker-rail"), load("rail-bunker-flash")
    pv = pivots("rail-bunker")
    med = explosion("medium")
    bg = plate(968, 540)

    def bunker(k, charge=0.0):
        img = composite(base, tur[k])
        return add(img, coils[k], 56, 56, charge) if charge else img

    rows = [("8 OF 32 TURRET HEADINGS ON THE BASE (1X)", row_of([bunker(k) for k in range(0, 32, 4)], bg=bg)),
            ("THE CHARGE: THE COILS' GLOW AT 0, 1/3, 2/3, 1 (1X); THE WRECK; THE RAIL STRIP (4X) AND FLASH (ADD.)",
             row_of([bunker(2, c) for c in (0, 1 / 3, 2 / 3, 1.0)] + [wreck] +
                    [s.resize((128, 56), Image.NEAREST) for s in strip[:2]] + flash, bg=bg)),
            ("ON THE GROWN ROCK AT 1X AND 2X", row_of([bunker(5, 0.6), bunker(5).resize((224, 224), Image.NEAREST)],
                                                       bg=bg))]
    lo, hi = GOLDS.get("rail-bunker (base + turret)", (None, None))
    if lo is None:
        shares = [e60.gold_share(composite(base, t)) for t in tur]
        lo, hi = min(shares), max(shares)
    notes = [f"THE CHOSEN R04 BUNKER (A.13 A'S SOLID LOCK LINE) IN Q.11 B GUNMETAL; GOLD {100 * lo:.0f}-{100 * hi:.0f} % "
             "(THE OCTAGON'S RIM, THE STRIP, THE COILS).",
             "THE LOCK LINE (A THIN RED LINE, 1 -> 2 PX OVER THE TELEGRAPH) IS DRAWN BY THE GAME FROM MUZZLE[K]."]
    sheet = sheet_of("RAIL BUNKER (FINAL R66): TURRET, CHARGE, RAIL, WRECK", f"PRODUCTION ART, {BATCH.upper()} - R66",
                     rows, notes)

    def draw(f, t):
        bx, by = 140, 110
        sx, sy = 250 + 120 * np.sin(t * 0.8), 330
        want = np.arctan2(sy - by, sx - bx)
        tel = 1.0 <= t < 2.0 or 4.5 <= t < 5.5
        t0 = 1.0 if t < 3 else 4.5
        if t0 <= t < t0 + 1.0:
            ws = np.arctan2(330 - by, 250 + 120 * np.sin(t0 * 0.8) - bx)
        else:
            ws = want
        k = int(round((ws - np.pi / 2) / (TAU / 32))) % 32
        mx, my = pv["muzzle"][k]
        mx, my = bx - 56 + mx, by - 56 + my
        u = t - t0
        d = ImageDraw.Draw(f)
        if 0 <= u < 1.0:
            x1, y1 = mx + np.cos(ws) * 700, my + np.sin(ws) * 700
            d.line([mx, my, x1, y1], fill=(255, 48, 56, int(110 + 145 * u)), width=1 if u < 0.5 else 2)
        f = paste(f, base, bx, by)
        f = paste(f, tur[k], bx, by)
        if 0 <= u < 1.0:
            f = add(f, coils[k], bx, by, u)
        if 1.0 <= u < 1.45:
            fi = min(3, int((u - 1.0) / 0.45 * 4))
            n = 22
            row = Image.new("RGBA", (n * strip[fi].width, strip[fi].height))
            for j in range(n):
                row.paste(strip[fi], (j * strip[fi].width, 0))
            rot = row.rotate(-np.degrees(ws), resample=Image.BILINEAR, expand=True)
            f = paste(f, rot, mx + np.cos(ws) * row.width / 2, my + np.sin(ws) * row.width / 2)
            f = add(f, flash[fi], mx, my)
        f = paste(f, ship_frame(), sx, sy)
        if t >= 7.0:
            j = int((t - 7.0) * 10)
            if j < len(med):
                f = add(f, med[j], bx, by)
            f = paste(f, wreck, bx, by) if j > 3 else f
        if tel:
            caption(f, "LOCK: THE LINE IS FIXED, 1.0 S TO STEP OUT")
        return f

    gb = plate(480, 400)
    gif = [draw(gb.copy(), i / FPS) for i in range(int(8.4 * FPS))]
    save_review("bunker", "rail-bunker", sheet, gif)


def review_rocks():
    bg = plate(968, 540)
    small = {n: load(f"debris-grown-{n}") for n in SMALL}
    large = {n: load(f"debris-grown-{n}") for n in LARGE}
    brk = load("debris-grown-a-break")
    plain = [load(f"debris-belt-{n}")[0] for n in ("large-a", "a")]
    rows = [("LARGE A, EVERY 2ND OF 16 TURNS (1X)", row_of(large["large-a"][::2], bg=bg)),
            ("LARGE B, EVERY 2ND TURN; SMALL A, B, C EVERY 4TH TURN (1X)",
             row_of(large["large-b"][::2] + small["a"][::4] + small["b"][::4] + small["c"][::4], bg=bg)),
            ("SMALL A SHOT APART (8 STEPS); BESIDE THE PLAIN ROCKS OF PARTS B AND C (1X); THE GROWN ONES AT 2X",
             row_of(brk + plain + [large["large-a"][0].resize((192, 192), Image.NEAREST),
                                   small["a"][0].resize((88, 88), Image.NEAREST)], bg=bg))]
    notes = ["PART B'S SMALL ROCKS AND PART C'S LARGE ROCKS WITH THE LEECH FIELD'S GROWTH: BLACK-VIOLET LUMPS ON THE",
             "SUNWARD CAP, VIOLET GLINTS, ONE (SMALL) OR THREE (LARGE) VIOLET LEECH SACS. THE LARGE ONES ARE WALLS."]
    sheet = sheet_of("GROWN ROCKS (FINAL R66): THE LEECH FIELD'S DRIFTING ROCKS",
                     f"PRODUCTION ART, {BATCH.upper()} - R66", rows, notes)
    save_review("rocks", "grown-rocks", sheet)


def review_objects():
    shack, opened = load("claim-shack"), load("claim-shack-open")
    buoy, glow = load("nav-buoy"), load("nav-buoy-glow")
    bg = plate(968, 540)
    rows = [("CLAIM 114: THE BEACON'S BLINK (4), SHOT OPEN (6, THE LAST STAYS) (1X)", row_of(shack + opened, bg=bg)),
            ("THE SAME AT 2X", row_of([s.resize((96, 96), Image.NEAREST) for s in (shack[0], opened[2], opened[5])],
                                       bg=bg)),
            ("THE CDF NAV BUOY: SHUT, OPENING, EMPTY; ITS STROBE (ADD.) (1X, 2X)",
             row_of(buoy + glow + [b.resize((80, 80), Image.NEAREST) for b in buoy], bg=bg))]
    notes = ["CLAIM 114: A PROSPECTOR'S PREFAB SHACK ON ITS CUT PAD, A SOLAR PANEL, THE MAST WITH THE AMBER BEACON STILL",
             "BLINKING, THE GROWTH OVER ITS RIGHT END. 20 HP (28 HARD); THE STRONGBOX'S LIGHT IN THE BURST SHELL.",
             "THE NAV BUOY: LEVEL 25'S MOORED BUOY IN CDF GREY WITH A BLUE BAND AND A WHITE STROBE (BUOYLOOKS' FRAMES)."]
    sheet = sheet_of("CLAIM 114 AND THE NAV BUOY (FINAL R66)", f"PRODUCTION ART, {BATCH.upper()} - R66", rows, notes)
    gb = plate(480, 300)
    small = explosion("small")

    def draw(f, t):
        y = 20 + 70 * t
        if t < 2.0:
            f = paste(f, shack[int(t * 4) % 4], 360, y)
        else:
            j = min(5, int((t - 2.0) * 10))
            f = paste(f, opened[j], 360, y)
            if small and int((t - 2.0) * 20) < len(small):
                f = add(f, small[int((t - 2.0) * 20)], 360, y)
            if t > 2.2:
                c = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
                ImageDraw.Draw(c).rectangle([2, 2, 9, 9], fill=(200, 160, 70, 255), outline=(90, 70, 30, 255))
                f = paste(f, c, 360, y - 30 * (t - 2.2))
        by = 40 + 60 * t
        st = 0 if t < 2.6 else 1 if t < 2.8 else 2
        f = paste(f, buoy[st], 120, by)
        f = add(f, glow[int(t * 4) % 4], 120, by)
        return f
    gif = [draw(gb.copy(), i / FPS) for i in range(int(4.0 * FPS))]
    save_review("objects", "ground-objects", sheet, gif)


REVIEWS = {"leech": review_leech, "bunker": review_bunker, "rocks": review_rocks, "shack": review_objects,
           "buoy": review_objects}


# =========================================================================== check

SETS = {"leech": [("void-leech", 64), ("void-leech-latched", 12), ("void-leech-drain", 4), ("void-leech-prey", 64),
                  ("void-leech-stunned", 8), ("void-leech-death", 8), ("void-leech-tatters", 8)],
        "bunker": [("rail-bunker", 1), ("rail-bunker-turret", 32), ("rail-bunker-coils", 32), ("rail-bunker-rail", 4),
                   ("rail-bunker-flash", 4), ("rail-bunker-wreck", 1)],
        "rocks": [(f"debris-grown-{n}", 16) for n in list(SMALL) + list(LARGE)] +
                 [(f"debris-grown-{n}-break", 8) for n in SMALL],
        "shack": [("claim-shack", 4), ("claim-shack-open", 6)],
        "buoy": [("nav-buoy", 3), ("nav-buoy-glow", 4)]}


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
    for root in ("void-leech", "rail-bunker"):
        ok = pivot_path(root).exists()
        print(f"{'ok ' if ok else 'BAD'} pivots/{root}.json in {pivot_path(root).parent.relative_to(ROOT)}")
        bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    subjects = [a for a in args if not a.startswith("--")] or list(BUILDERS)
    unknown = [s for s in subjects if s not in BUILDERS]
    if unknown:
        sys.exit(f"unknown: {unknown}; subjects: {', '.join(BUILDERS)}")
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    done = set()
    for s in subjects:
        if REVIEWS.get(s) and REVIEWS[s] not in done:
            REVIEWS[s]()
            done.add(REVIEWS[s])
