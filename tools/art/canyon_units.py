#!/usr/bin/env python3
"""Production art: Level 16's units and props (M7 part C batch, concept round 39, step ART-C): the Tendril
Anchor pair and its tendril (design/enemies/ground/tendril-anchor, the chosen round-36 look a "polyp clamps,
fleshy rope", tools/concept/enemies_r36.anchor_a imported unchanged), the Threadcrawler (design/enemies/
ground/threadcrawler, the chosen round-06 look a, tools/concept/render/r06_models.thread_head / _segment /
_tail unchanged), and the level's props (design/campaign/act-3-red-dust/level-16-valles-canyon-run: the
Candor relay hut, the stranded cargo rovers, the wall-contact spark; straight to production, no concept:
designed to the README, Mars light and readability rule 7 for loot and secret targets).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down (for 32:
8 = facing left, 16 = up, 24 = right); every heading its own render under the fixed key light.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.
SpriteUse: an enemy's sprites once design/enemies/*/<slug>/data.yaml exists, a ground target's `sprite`
once a level names it; `tendril` and `wall-spark` are the game's own looks of the `link` and the wall
contact, held until SpriteUse claims them, e.g. by a pattern root):
  tendril-anchor_0..95      40x40, 32 headings x 3 pulse frames (heading * 3 + pulse; the cone swelling,
                            its lip glowing brighter: 0 rest, 1 half, 2 full; 6 fps loop 0-1-2-1); the cone
                            points along the heading (the left anchor of a pair faces right, heading 24)
  tendril_0..3              64x16, the standing tendril: a straight strip along x, tiling every 64 px
                            (u = 0 at either anchor), the rope's centre on row 8 (8 px rope, a stepped
                            crimson halo): mauve sinew, two strands twisting every 16 px, the crimson vein
                            with pulses running along +x, 8 px a frame (12 fps). The game stretches it
                            between the anchors' tips as a chain of quads swaying slowly (the concept: at
                            most 5 px at the middle, none at the anchors)
  tendril-dormant_0..1      64x16, the 1.0 s dormant tell: a thin flickering thread (0 dim and whole,
                            1 brighter and broken), no halo
  tendril-flare_0..2        64x16, the flare to full width (0.15 s): the thread swelling, white-hot crimson
  tendril-wither_0..5       64x16, 0.4 s at 15 fps: a broken or released rope going limp, its vein dying,
                            darkening to a grey-brown cord and thinning out (frame 5 nearly gone)
  tendril-end_0..3          16x16, the frayed end of a cut or snapped rope, pointing +x (its stump at the
                            image's left edge, row 8): the fibres splayed, the vein's last embers flickering
                            (drawn at both cut ends, turned with the rope)
  tendril-burn_0..3         24x24, additive: the white-hot spot where a beam burns the tendril (flicker)
  threadcrawler-head_0..63  56x56, 16 headings x 4 jaw frames (heading * 4 + frame: the forcipules shut,
                            half, open, half; 8 fps)
  threadcrawler-segment_0..63  48x48, 16 headings x 4 leg frames (heading * 4 + phase; the leg ripple: a
                            segment's phase runs one step behind the one in front, 10 fps)
  threadcrawler-tail_0..15  40x40, 16 headings
  threadcrawler-pore_0..3   24x24, additive, centred on a segment: the spore pore's glow, 0-2 the 0.3 s tell
                            brightening, 3 the shot's flash
  threadcrawler-husk        112x112, the curled husk a killed crawler leaves on the ground for the rest of
                            the level (dead chitin, no glow)
  relay-hut_0..3            48x40, the Candor relay hut at the left wall's foot, its cargo door facing the
                            canyon (+x): 0 sealed (beacon on), 1 one hit (scorched), 2 two hits (the door
                            buckled), 3 burst (the door blown off, the amber crate in the dark hold); hard's 5
                            hits show 0, 1, 1, 2, 2, then 3; rule 7's light rim on every frame
  relay-hut-glow_0..7       48x40, additive: the beacon on its mast (0-3 a slow blink until the first hit,
                            4-7 a hurried flicker after it), as Level 13's hut
  canyon-rover_0..2         36x24, a stranded colony cargo rover (six wheels, a cab, two cargo pods, the
                            colonies' off-white with amber bands), slewed across the cliff road: 0 intact,
                            1 damaged (scorch, a wheel gone), 2 the burnt wreck left behind
  wall-spark_0..5           32x32, additive: the ship's (or a shot's) contact with rock: sparks fanning out
                            from the contact point (the image's centre) away from the wall toward +x (the
                            left wall: mirror it for the right; additive light, nothing lit), 30 fps
  wall-spark-dust_0..5      32x32, solid with stepped translucency: the dust kicked off the rock with it
  design/enemies/ground/concept/tendril-anchor-final-r39-a.png/.gif, threadcrawler-final-r39-a.png/.gif
  design/campaign/act-3-red-dust/level-16-valles-canyon-run/concept/l16-props-final-r39-a.png/.gif

Run: python3 tools/art/canyon_units.py [--review]   (~2 min; --review only rebuilds the review files)
"""
import contextlib
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r36 as u36  # noqa: E402  (concept round 36: the chosen anchor_a, imported unchanged)
import ground_targets as gt  # noqa: E402  (concept script, unchanged: the light rim, scorch, hazard stripes)
import l13_props as l13  # noqa: E402  (tools/art: the additive beacon disc)
import loot_targets as lt  # noqa: E402  (tools/art: scorched materials)
from render import r06_models as m6  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials, write_gif  # noqa: E402
from render.sdf import (Material, rotate_z, sd_box, sd_capsule, sd_cylinder_x, sd_ellipsoid, sd_sphere,  # noqa: E402
                        subtract, union)

SCRIPT = "canyon_units.py"
BATCH = "M7 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r39"
GROUND_CONCEPT = DESIGN / "enemies" / "ground" / "concept"
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-16-valles-canyon-run"
HELD = ROOT / "build" / "art-held" / "sprites"
BACKDROP = ROOT / "assets" / "backdrop" / "level-16"

ANCHOR, ANCHOR_HEADINGS, PULSES, ANCHOR_EXTENT = 40, 32, (0.0, 0.5, 1.0), 2.4
STRIP = (64, 16)
HEAD, SEG, TAIL, CRAWL_HEADINGS, CRAWL_EXTENT = 56, 48, 40, 16, 2.3
JAW = (0.0, 0.5, 1.0, 0.5)
LEG_PHASES = 4
PORE = 24
HUSK = 112
HUT = (48, 40)
ROVER = (36, 24)
SPARK = 32
CRIMSON = np.array(u36.CRIMSON, float)
LIME = np.array((168, 255, 42), float)


# =========================================================================== held sprites

def level_sprite_roots():
    roots = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        data = yaml.safe_load(f.read_text(encoding="utf-8")) or {}
        for g in data.get("ground_targets") or []:
            if g.get("sprite"):
                roots.add(str(g["sprite"]))
    return roots


def pattern_used(pattern):
    """Whether an enemy's data has an attack of that pattern (SpriteUse.PATTERN_ROOTS)."""
    for f in (DESIGN / "enemies").glob("*/*/data.yaml"):
        data = yaml.safe_load(f.read_text(encoding="utf-8")) or {}
        if any((a or {}).get("pattern") == pattern for a in data.get("attacks") or []):
            return True
    return False


def terrain_used():
    """Whether a level's data has a `terrain` block (SpriteUse.TERRAIN_ROOT)."""
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        if (yaml.safe_load(f.read_text(encoding="utf-8")) or {}).get("terrain"):
            return True
    return False


def _rooted(name, root):
    return name == root or name.startswith(root + "-")


def claimed(name):
    """Whether the data claims the sprite (SpriteUse): an enemy's root once its data.yaml exists, a ground
    target's look once a level names it, the tendril once an enemy has a `link` attack (PATTERN_ROOTS),
    the wall spark once a level has `terrain` (TERRAIN_ROOT; step G1)."""
    for slug in ("tendril-anchor", "threadcrawler"):
        if _rooted(name, slug):
            return any((DESIGN / "enemies").glob(f"*/{slug}/data.yaml"))
    if _rooted(name, "tendril"):
        return pattern_used("link")
    if _rooted(name, "wall-spark"):
        return terrain_used()
    return any(_rooted(name, r) for r in level_sprite_roots())


@contextlib.contextmanager
def sprites_of(name):
    target = artkit.SPRITES if claimed(name) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            stem = old.stem
            if stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit()):
                old.unlink()
    with sprites_of(name) as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) -> {where.relative_to(ROOT)}")
    return where


def load(name):
    for folder in (artkit.SPRITES, HELD):
        single = folder / f"{name}.png"
        if single.exists():
            return [Image.open(single).convert("RGBA")]
        files = [f for f in folder.glob(f"{name}_*.png") if f.stem.rsplit("_", 1)[0] == name]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


# =========================================================================== models

def turned(scene, mats, h):
    """A +Y-forward model turned to point ``h`` clockwise from straight down, its patterns with it."""
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def anchor_frame(k, j):
    scene, mats = turned(*u36.anchor_a(PULSES[j], 1.0 + 0.4 * PULSES[j]), TAU * k / ANCHOR_HEADINGS)
    return artkit.native(*artkit.render_hi(scene, mats, (ANCHOR, ANCHOR), ANCHOR_EXTENT))


def anchor_intel_model():
    """(scene, mats) for the hangar intel portrait: the cone pointing down, at its full pulse."""
    return turned(*u36.anchor_a(1.0, 1.4), 0.0)


def crawl_frame(kind, k, j):
    h = TAU * k / CRAWL_HEADINGS
    if kind == "head":
        model, size = m6.thread_head(JAW[j]), HEAD
    elif kind == "segment":
        model, size = m6.thread_segment(TAU * j / LEG_PHASES), SEG
    else:
        model, size = m6.thread_tail(), TAIL
    scene, mats = turned(*model, h)
    return artkit.native(*artkit.render_hi(scene, mats, (size, size), CRAWL_EXTENT))


def threadcrawler_intel_model():
    """The head nose down with its first segment trailing up behind it (as intel.coilwyrm_model)."""
    head, mats = turned(*m6.thread_head(0.6), 0.0)
    seg, _ = turned(*m6.thread_segment(0.0), 0.0)
    parts = [(head, (0.0, -0.42), 0.8), (seg, (0.06, 0.36), 0.55), (seg, (0.2, 0.84), 0.46)]

    def scene(p):
        out = []
        for model, (x, y), k in parts:
            d, m = model((p - np.array([x, y, 0.0])) / k)
            out.append((d * k, m))
        return sdf.union(*out[::-1])
    return scene, mats


def husk():
    """The curled husk: the chain's parts posed along a tightening spiral, rendered with the glow off and
    the chitin dulled toward a dead grey-brown, a little sand drifted against it."""
    seg_scene, mats = m6.thread_segment(TAU * 0.25, glow=0.0)
    head_scene, _ = m6.thread_head(1.0, glow=0.0)
    tail_scene, _ = m6.thread_tail(glow=0.0)
    dead = []
    for m in mats:
        a = np.array(m.albedo) * 0.55 + np.array((0.32, 0.27, 0.22)) * 0.45
        dead.append(Material(tuple(a), metal=0.0, shininess=10, spec=0.1, pattern=m.pattern))
    parts = []
    n = 12
    for i in range(n + 2):
        a = 0.6 + i * 0.52
        r = 1.55 - i * 0.08
        x, y = r * math.cos(a), r * math.sin(a)
        heading = a + np.pi                     # tangent of the spiral, roughly
        k = 0.62 - 0.012 * i
        model = head_scene if i == 0 else tail_scene if i == n + 1 else seg_scene
        rot = model_rotation(heading)
        parts.append((model, (x, y), k, rot))

    def scene(p):
        out = []
        for model, (x, y), k, rot in parts:
            q = rotate_z((p - np.array([x, y, -0.05])) / k, rot)
            d, m = model(q)
            out.append((d * k, m))
        return sdf.union(*out, k=0.05)
    sand = Material((0.62, 0.40, 0.26), metal=0.0, shininess=6, spec=0.03)
    hi, factor = artkit.render_hi(scene, dead + [sand], (HUSK, HUSK), 3.9)
    img = artkit.native(hi, factor)
    a = np.array(img)
    return Image.fromarray(a, "RGBA")


# --------------------------------------------------------------------------- the tendril strips

def strip_canvas(scale=4):
    w, h = STRIP
    return Image.new("RGBA", (w * scale, h * scale), (0, 0, 0, 0)), scale


def finish_strip(img, scale, levels=4):
    small = img.resize((img.width // scale, img.height // scale), Image.BOX)
    return artkit.stepped_alpha(small, levels)


def rope(d, s, xs, y, width, body, outline, strand, vein, pulse, alpha=255, phase=0.0, twist=True):
    """One rope across the canvas: outline, body, two twisted strands, the vein with its pulse."""
    for x0, x1 in zip(xs[:-1], xs[1:]):
        d.line([(x0 * s, y * s), (x1 * s, y * s)], fill=tuple(int(c) for c in outline) + (alpha,), width=int((width + 2) * s))
    for x0, x1 in zip(xs[:-1], xs[1:]):
        d.line([(x0 * s, y * s), (x1 * s, y * s)], fill=tuple(int(c) for c in body) + (alpha,), width=int(width * s))
    if twist:
        for ph in (0.0, np.pi):
            pts = [(x * s, (y + 0.32 * width * math.sin(TAU * 4 * x / STRIP[0] + ph)) * s) for x in np.linspace(0, STRIP[0], 129)]
            d.line(pts, fill=tuple(int(c) for c in strand) + (alpha,), width=max(1, int(0.25 * width * s)))
    for i in range(len(xs) - 1):
        k = pulse(xs[i])
        col = vein * (0.35 + 0.65 * k) + np.array((40, 8, 14)) * (1 - k) * 0.6
        d.line([(xs[i] * s, y * s), (xs[i + 1] * s, y * s)], fill=tuple(int(c) for c in np.clip(col, 0, 255)) + (alpha,),
               width=max(1, int(0.36 * width * s)))


def halo(img, s, y, width, strength, colour=CRIMSON):
    g = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(g).line([(0, y * s), (img.width, y * s)], fill=tuple(int(c) for c in colour) + (int(150 * strength),),
                           width=int(width * s))
    g = g.filter(ImageFilter.GaussianBlur(2.6 * s))
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.alpha_composite(g)
    out.alpha_composite(img)
    return out


def tendril_frame(f):
    img, s = strip_canvas()
    d = ImageDraw.Draw(img)
    xs = np.linspace(0, STRIP[0], 129)

    def pulse(x):
        return 0.5 + 0.5 * math.sin(TAU * (x / 32.0 - f / 4.0))
    rope(d, s, xs, 8, 7, (140, 112, 132), (40, 10, 18), (68, 44, 62), CRIMSON, pulse)
    return finish_strip(halo(img, s, 8, 12, 1.0), s)


def dormant_frame(f):
    img, s = strip_canvas()
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(1901 + f)
    level = (0.5, 0.85)[f]
    x = 0.0
    while x < STRIP[0]:
        ln = rng.uniform(5, 14) if f else STRIP[0]
        col = CRIMSON * level + np.array((60, 30, 40)) * (1 - level)
        d.line([(x * s, 8 * s), (min(STRIP[0], x + ln) * s, 8 * s)], fill=tuple(int(c) for c in col) + (255,), width=2 * s)
        x += ln + (rng.uniform(2, 5) if f else 0)
    return finish_strip(img, s)


def flare_frame(f):
    img, s = strip_canvas()
    d = ImageDraw.Draw(img)
    width = (3.0, 5.5, 8.0)[f]
    xs = np.linspace(0, STRIP[0], 129)
    hot = np.array((255, 180, 190), float)
    rope(d, s, xs, 8, width, (170, 120, 140), (60, 14, 24), (90, 54, 74), hot, lambda x: 1.0, twist=f == 2)
    return finish_strip(halo(img, s, 8, 8 + 3 * f, 0.6 + 0.3 * f, colour=np.array((255, 90, 110), float)), s)


def wither_frame(f):
    img, s = strip_canvas()
    d = ImageDraw.Draw(img)
    t = f / 5
    width = 7 * (1 - 0.7 * t)
    xs = np.linspace(0, STRIP[0], 129)
    body = np.array((140, 112, 132)) * (1 - t) + np.array((92, 78, 70)) * t
    vein = CRIMSON * (1 - t) ** 2 + np.array((70, 50, 44)) * (1 - (1 - t) ** 2)
    alpha = int(255 * (1 - 0.6 * t ** 2))
    rope(d, s, xs, 8 + 2 * t, width, body, (40 + 20 * t, 20 + 14 * t, 18 + 10 * t), body * 0.6, vein,
         lambda x: 0.5, alpha=alpha, twist=t < 0.8)
    out = img if t > 0.3 else halo(img, s, 8, 10, 0.6 * (1 - t / 0.3))
    return finish_strip(out, s)


def end_frame(f):
    """The frayed end, pointing +x: fibres splayed from the stump, embers on the vein's end."""
    s = 4
    img = Image.new("RGBA", (16 * s, 16 * s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(1911)
    d.line([(0, 8 * s), (5 * s, 8 * s)], fill=(40, 10, 18, 255), width=9 * s)
    d.line([(0, 8 * s), (5 * s, 8 * s)], fill=(140, 112, 132, 255), width=7 * s)
    for k in range(7):
        a = rng.uniform(-0.9, 0.9)
        ln = rng.uniform(5, 10) * (0.9 + 0.1 * math.sin(f * 1.7 + k))
        y0 = 8 + rng.uniform(-3, 3)
        d.line([(4 * s, y0 * s), ((4 + ln * math.cos(a)) * s, (y0 + ln * math.sin(a)) * s)],
               fill=(120 - 6 * k, 90, 110, 255), width=int(1.2 * s))
    ember = (0.5 + 0.5 * math.sin(f * TAU / 4))
    col = CRIMSON * (0.6 + 0.4 * ember) + np.array((255, 200, 160)) * 0.3 * ember
    d.ellipse([(4.5 - 1.6) * s, (8 - 1.6) * s, (4.5 + 1.6) * s, (8 + 1.6) * s], fill=tuple(int(c) for c in np.clip(col, 0, 255)) + (255,))
    small = img.resize((16, 16), Image.BOX)
    return artkit.stepped_alpha(small, 4)


def burn_frame(f):
    yy, xx = np.mgrid[0:24, 0:24].astype(np.float64) + 0.5
    d = np.hypot(xx - 12, yy - 12)
    r = 7.5 + (0.0, 1.2, 0.4, 1.8)[f]
    a = np.clip(1 - d / r, 0, 1) ** 1.4
    core = np.clip(1 - d / 2.6, 0, 1)
    rgb = np.array((255, 150, 90), float) * a[..., None] + np.array((255, 240, 220), float) * core[..., None]
    rgb = np.floor(np.clip(rgb / 255, 0, 1) * 6 + 0.5) / 6 * 255
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def pore_frame(f):
    level = (0.35, 0.65, 1.0, 1.4)[f]
    yy, xx = np.mgrid[0:PORE, 0:PORE].astype(np.float64) + 0.5
    d = np.hypot(xx - PORE / 2, yy - PORE / 2)
    a = (np.clip(1 - d / 9.5, 0, 1) ** 1.6 * 0.8 + np.clip(1 - d / 2.4, 0, 1) * 0.6) * level
    rgb = LIME * np.clip(a, 0, 1)[..., None]
    if f == 3:
        rgb = rgb + np.array((236, 255, 210)) * np.clip(1 - d / 4, 0, 1)[..., None] * 0.6
    rgb = np.floor(np.clip(rgb / 255, 0, 1) * 6 + 0.5) / 6 * 255
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


# --------------------------------------------------------------------------- the relay hut and the rovers

HU_BODY, HU_PANEL, HU_DARK, HU_DOOR, HU_HOLD, HU_CRATE, HU_BEACON, HU_ROCK, HU_BAND = range(9)


def hut_mats(beacon_on):
    return [Material((0.68, 0.65, 0.59), metal=0.25, shininess=26, spec=0.25, pattern=sdf.panel_lines(0.33, 0.05, 0.82)),
            Material((0.20, 0.24, 0.32), metal=0.6, shininess=70, spec=0.8),
            Material((0.15, 0.15, 0.17), metal=0.4, shininess=30, spec=0.3),
            Material((0.52, 0.53, 0.52), metal=0.6, shininess=50, spec=0.6),
            Material((0.04, 0.04, 0.05), metal=0.0, shininess=4, spec=0.0),
            Material((0.95, 0.64, 0.16), metal=0.2, shininess=40, spec=0.6),
            Material((0.3, 0.2, 0.1), emission=(2.2, 1.3, 0.4)) if beacon_on
            else Material((0.55, 0.36, 0.12), metal=0.2, shininess=90, spec=0.9),
            Material((0.38, 0.22, 0.15), metal=0.0, shininess=10, spec=0.08),
            Material((0.98, 0.66, 0.14), metal=0.0, shininess=8, spec=0.05, pattern=gt.hazard_stripes)]


BEACON_AT = (-13.0, 8.0)


def hut_scene(state):
    """Model units = px, y up. A colonial relay prefab against the wall on the left (-x): the main block with a
    small solar panel, the relay mast with its dish and the beacon; on its canyon side (+x) the cargo bay under
    a sloping amber-striped hatch (seen from above): sealed, scorched, buckled, then blown off with the amber
    crate in the dark bay."""
    def scene(p):
        body = sd_box(p, (-8.0, 0.0, 5.0), (8.0, 10.0, 5.0), 0.8)
        roof = sd_box(p, (-8.0, 0.0, 10.4), (8.6, 10.6, 0.6), 0.3)
        band = sd_box(p, (-8.0, 0.0, 1.2), (8.2, 10.2, 1.2), 0.3)
        panel = sd_box(p, (-11.0, -4.5, 11.6), (3.6, 4.0, 0.4), 0.1)
        bay = sd_box(p, (6.0, 0.0, 2.6), (6.0, 8.0, 2.6), 0.6)
        rocks = np.minimum(sd_ellipsoid(p, (-19.0, -7.0, 1.0), (3.5, 4.0, 2.8)), sd_ellipsoid(p, (-19.5, 6.0, 1.0), (3.0, 4.5, 2.6)))
        items = [(body, HU_BODY), (roof, HU_BODY), (band, HU_BAND), (panel, HU_PANEL), (bay, HU_BODY), (rocks, HU_ROCK)]
        slope = sdf.rotate_y(p - np.array([6.0, 0.0, 6.4]), 0.5)
        hatch = sd_box(slope, (0.0, 0.0, 0.0), (6.4, 7.0, 0.6), 0.3)
        if state < 2:
            items.append((hatch, HU_DOOR))
            items.append((sd_box(slope, (0.0, 0.0, 0.5), (5.0, 1.0, 0.3), 0.1), HU_BAND))
        elif state == 2:
            bent = sdf.rotate_x(slope, 0.18)
            items.append((sd_box(bent, (0.6, 0.6, 0.4), (6.2, 6.6, 0.6), 0.3), HU_DOOR))
        mast = sd_capsule(p, (-2.0, 7.0, 10.0), (-2.0, 7.0, 21.0), 0.5)
        dish = np.maximum(sd_sphere(p, (-2.0, 7.0, 19.0), 3.0), -sd_sphere(p, (-1.0, 7.5, 20.4), 3.0))
        items += [(mast, HU_DARK), (dish, HU_DOOR),
                  (sd_sphere(p, (BEACON_AT[0], BEACON_AT[1], 11.8), 1.3), HU_BEACON)]
        d, m = union(*items, k=0.4)
        if state == 3:                                  # the hatch blown off: the dark bay and the crate
            d, m = subtract((d, m), sd_box(p, (6.0, 0.0, 4.6), (5.0, 7.0, 3.4), 0.4))
            d, m = union((d, m), (sd_box(p, (6.0, 0.0, 1.0), (5.0, 7.0, 1.0), 0.2), HU_HOLD),
                         (sd_box(p, (6.4, 0.6, 3.0), (3.0, 3.2, 2.0), 0.5), HU_CRATE), k=0.2)
            off = sd_box(rotate_z(p - np.array([18.5, -6.0, 0.6]), 0.6), (0, 0, 0), (2.6, 5.5, 0.5), 0.2)
            d, m = union((d, m), (off, HU_DOOR), k=0.1)
        return d, m
    return scene


def hut_frame(state):
    mats = hut_mats(state == 0)
    hi, factor = artkit.render_hi(hut_scene(state), mats, HUT, float(HUT[0]), z_top=26.0, steps=170)
    arr = np.array(artkit.native(hi, factor, crisp=60)).astype(np.float64)
    dx, dy = HUT[0] / 2 + 7.0, HUT[1] / 2 - 0.0
    if state >= 1:
        arr = gt.scorch(arr, [(dx - 1, dy, 3.5 + 1.5 * state), (dx - 4, dy + 4, 2.5)], 3901 + state)
    return Image.fromarray(gt.light_rim(arr).astype(np.uint8), "RGBA")


def hut_glow(k):
    level = (1.0, 0.5, 0.0, 0.0, 1.0, 0.0, 0.6, 0.0)[k]
    bx, by = HUT[0] / 2 + BEACON_AT[0], HUT[1] / 2 - BEACON_AT[1]
    return l13.glow_disc(HUT, [(bx, by, 7.0, level, l13.BEACON_RGB)])


RV_BODY, RV_GLASS, RV_DARK, RV_WHEEL, RV_BAND, RV_POD, RV_BURNT = range(7)


def rover_mats():
    return [Material((0.70, 0.67, 0.60), metal=0.3, shininess=34, spec=0.35, pattern=sdf.panel_lines(0.33, 0.05, 0.8)),
            Material((0.22, 0.30, 0.38), metal=0.7, shininess=90, spec=1.0),
            Material((0.15, 0.15, 0.17), metal=0.4, shininess=30, spec=0.3),
            Material((0.12, 0.11, 0.11), metal=0.1, shininess=10, spec=0.1),
            Material((0.98, 0.66, 0.14), metal=0.0, shininess=8, spec=0.05),
            Material((0.62, 0.60, 0.55), metal=0.3, shininess=30, spec=0.3),
            Material((0.10, 0.08, 0.07), metal=0.1, shininess=6, spec=0.05)]


def rover_scene(state):
    """Model units = px, y up; the rover along x, its cab to the left."""
    def scene(p):
        items = []
        burnt = state == 2
        body_m = RV_BURNT if burnt else RV_BODY
        items.append((sd_box(p, (2.0, 0.0, 4.0), (12.0, 6.0, 1.6), 0.6), RV_DARK))             # chassis
        items.append((sd_box(p, (-10.0, 0.0, 6.0), (4.0, 5.6, 3.0), 1.4), body_m))             # cab
        if not burnt:
            items.append((sd_box(sdf.rotate_y(p - np.array([-12.6, 0.0, 7.6]), -0.6), (0, 0, 0), (1.6, 4.4, 0.6), 0.3), RV_GLASS))
            items.append((sd_box(p, (-7.4, 0.0, 9.0), (0.8, 5.8, 0.4), 0.2), RV_BAND))
            items.append((sd_box(p, (4.0, 0.0, 11.7), (0.6, 4.6, 0.3), 0.1), RV_BAND))
        for x in (-1.0, 9.0):                                                                   # cargo pods
            if burnt and x > 0:
                items.append((sd_box(p, (x, 0.0, 5.6), (4.4, 5.0, 1.2), 0.6), RV_BURNT))
            else:
                items.append((sd_cylinder_x(p, (x, 0.0, 7.4), 4.4, 4.2), RV_BURNT if burnt else RV_POD))
        wheels = []
        for x in (-10.0, 1.0, 11.0):
            for y in (-7.0, 7.0):
                if state == 1 and (x, y) == (11.0, 7.0):
                    continue                                                                   # a wheel gone
                wheels.append(np.maximum(np.hypot(p[:, 0] - x, p[:, 2] - 3.0) - 2.8, np.abs(p[:, 1] - y) - 1.3))
        items.append((np.minimum.reduce(wheels), RV_WHEEL))
        return union(*items, k=0.3)
    return scene


ROVER_YAW = -0.18


def rover_frame(state):
    mats = rover_mats()
    if state == 1:
        mats = lt.scorched(mats, [(0.62, 0.42, 5.0), (0.34, 0.6, 3.5)], ROVER)
    base = rover_scene(state)
    scene = (lambda p: base(rotate_z(p, ROVER_YAW)))
    hi, factor = artkit.render_hi(scene, mats, ROVER, float(ROVER[0]), z_top=20.0, steps=160)
    arr = np.array(artkit.native(hi, factor, crisp=60)).astype(np.float64)
    if state == 2:
        arr = gt.scorch(arr, [(18, 12, 9.0), (8, 13, 5.0)], 3911)
    return Image.fromarray(gt.light_rim(arr).astype(np.uint8), "RGBA")


# --------------------------------------------------------------------------- the wall spark

def spark_frame(f, dust=False):
    """Sparks fanning out from the contact (the centre) away from the wall (+x), falling and fading; or the
    dust kicked off with them."""
    s = 4
    n = SPARK * s
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(1931)
    t = (f + 0.5) / 6
    c = n / 2
    if dust:
        for k in range(12):
            a = rng.uniform(-1.2, 1.2)
            r0 = rng.uniform(1.5, 3.5) * s
            dist = (3 + 11 * t ** 0.6) * s * rng.uniform(0.5, 1.1)
            x, y = c + dist * math.cos(a), c + dist * math.sin(a) + 3 * t * s
            rr = r0 * (1 + 1.2 * t)
            al = int(130 * (1 - t) ** 1.4)
            d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=(214, 164, 114, al))
        small = img.resize((SPARK, SPARK), Image.BOX)
        return artkit.stepped_alpha(small, 4)
    for k in range(16):
        a = rng.uniform(-1.25, 1.25)
        v = rng.uniform(9, 17)
        dist = v * t * s
        x1 = c + dist * math.cos(a)
        y1 = c + dist * math.sin(a) + 8 * t * t * s
        tail = 0.5
        xt = c + dist * (1 - tail) * math.cos(a)
        yt = c + dist * (1 - tail) * math.sin(a) + 8 * (t * (1 - tail)) ** 2 * s
        heat = max(0.0, 1 - t * rng.uniform(0.8, 1.2))
        col = (255, int(150 + 100 * heat), int(60 + 150 * heat ** 2), 255)
        if heat > 0.05:
            d.line([(xt, yt), (x1, y1)], fill=col, width=int(1.5 * s))
    if f < 2:
        r = (3.5 - 1.5 * f) * s
        d.ellipse([c - r, c - r, c + r, c + r], fill=(255, 220, 160, 255))
    small = np.array(img.resize((SPARK, SPARK), Image.BOX)).astype(np.float64)
    rgb = small[..., :3] * small[..., 3:4] / 255 * 1.6
    rgb = np.floor(np.clip(rgb / 255, 0, 1) * 6 + 0.5) / 6 * 255
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


# =========================================================================== build

def _job(job):
    kind, *a = job
    if kind == "anchor":
        return anchor_frame(*a)
    if kind in ("head", "segment", "tail"):
        return crawl_frame(kind, *a)
    if kind == "husk":
        return husk()
    if kind == "hut":
        return hut_frame(a[0])
    if kind == "rover":
        return rover_frame(a[0])
    raise ValueError(kind)


def build():
    anchors = [("anchor", k, j) for k in range(ANCHOR_HEADINGS) for j in range(len(PULSES))]
    heads = [("head", k, j) for k in range(CRAWL_HEADINGS) for j in range(len(JAW))]
    segs = [("segment", k, j) for k in range(CRAWL_HEADINGS) for j in range(LEG_PHASES)]
    tails = [("tail", k, 0) for k in range(CRAWL_HEADINGS)]
    props = [("husk",)] + [("hut", i) for i in range(4)] + [("rover", i) for i in range(3)]
    jobs = anchors + heads + segs + tails + props
    with ProcessPoolExecutor() as pool:
        out = list(pool.map(_job, jobs, chunksize=4))
    i = 0

    def take(n):
        nonlocal i
        part = out[i:i + n]
        i += n
        return part
    anchor = artkit.quantize_set(take(len(anchors)), 32)
    chain = artkit.quantize_set(take(len(heads) + len(segs) + len(tails)), 40)
    hd, sg, tl = chain[:len(heads)], chain[len(heads):len(heads) + len(segs)], chain[len(heads) + len(segs):]
    hk = artkit.quantize_set(take(1), 28)
    hut = artkit.quantize_set(take(4), 40)
    rover = artkit.quantize_set(take(3), 32)
    sets = {
        "tendril-anchor": anchor,
        "tendril": artkit.quantize_set([tendril_frame(f) for f in range(4)], 20),
        "tendril-dormant": artkit.quantize_set([dormant_frame(f) for f in range(2)], 8),
        "tendril-flare": artkit.quantize_set([flare_frame(f) for f in range(3)], 20),
        "tendril-wither": artkit.quantize_set([wither_frame(f) for f in range(6)], 20),
        "tendril-end": artkit.quantize_set([end_frame(f) for f in range(4)], 16),
        "tendril-burn": [burn_frame(f) for f in range(4)],
        "threadcrawler-head": hd, "threadcrawler-segment": sg, "threadcrawler-tail": tl,
        "threadcrawler-pore": [pore_frame(f) for f in range(4)],
        "threadcrawler-husk": hk,
        "relay-hut": hut,
        "relay-hut-glow": artkit.quantize_set([hut_glow(k) for k in range(8)], 12),
        "canyon-rover": rover,
        "wall-spark": [spark_frame(f) for f in range(6)],
        "wall-spark-dust": artkit.quantize_set([spark_frame(f, dust=True) for f in range(6)], 12),
    }
    held = []
    for name, frames in sets.items():
        where = write(name, frames, single=name == "threadcrawler-husk")
        if where == HELD:
            held.append(name)
    print("held (no data claims them yet): " + (", ".join(held) if held else "none"))


# =========================================================================== review

def floor_plate(w, h, x0=0, y0=0):
    tile = BACKDROP / "canyon-floor.png"
    if tile.exists():
        img = Image.open(tile).convert("RGBA")
        return img.crop((x0, y0, x0 + w, y0 + h))
    return Image.new("RGBA", (w, h), (150, 96, 60, 255))


def paste_c(dst, img, x, y):
    dst.alpha_composite(img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def add_c(dst, img, x, y):
    return artkit.add_light(dst, img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def draw_rope(dst, frames, ax, ay, bx, by, f, sway=0.0, t=0.0):
    """The strip stretched between two points as the game will: column by column along the line, swaying."""
    strip = np.array(frames[f % len(frames)])
    length = math.hypot(bx - ax, by - ay)
    if length < 1:
        return
    ux, uy = (bx - ax) / length, (by - ay) / length
    out = np.array(dst).astype(np.float64)
    h, w = strip.shape[:2]
    for k in range(int(length)):
        u = k / length
        off = sway * math.sin(math.pi * u) * math.sin(TAU * (u * 1.5 + t * 0.5))
        cx, cy = ax + ux * k - uy * off, ay + uy * k + ux * off
        col = strip[:, k % w].astype(np.float64)
        for r in range(h):
            if col[r, 3] <= 0:
                continue
            x, y = int(round(cx - uy * (r - h / 2))), int(round(cy + ux * (r - h / 2)))
            if 0 <= x < out.shape[1] and 0 <= y < out.shape[0]:
                a = col[r, 3] / 255
                out[y, x, :3] = out[y, x, :3] * (1 - a) + col[r, :3] * a
    dst.paste(Image.fromarray(out.astype(np.uint8), "RGBA"))


def anchor_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    anc = load("tendril-anchor")
    sets = {n: load(n) for n in ("tendril", "tendril-dormant", "tendril-flare", "tendril-wither", "tendril-end", "tendril-burn")}
    rows = [("TENDRIL ANCHOR: 32 HEADINGS (PULSE 0)", anc[0::3], 2, False),
            ("PULSE FRAMES (HEADING 24, FACING RIGHT)", anc[72:75], 3, False),
            ("TENDRIL: STANDING LOOP (64X16, TILES ALONG X)", sets["tendril"], 3, False),
            ("DORMANT TELL, FLARE", sets["tendril-dormant"] + sets["tendril-flare"], 3, False),
            ("WITHER (0.4 S)", sets["tendril-wither"], 3, False),
            ("FRAYED END; BURN SPOT (ADDITIVE)", sets["tendril-end"], 4, False),
            ("", sets["tendril-burn"], 4, True)]
    sheet = artkit.review_sheet("TENDRIL ANCHOR - FINAL (ROUND 36 LOOK A)", rows, width=1400, batch=BATCH)
    frames = []
    W_, H_ = 320, 300
    for i in range(60):
        t = i / 10
        img = floor_plate(W_, H_, 80, (300 - int(t * 30)) % 600)
        y = 150
        ax, bx = 40, 280
        if t < 1.0:
            draw_rope(img, sets["tendril-dormant"], ax + 12, y, bx - 12, y, i % 2)
        elif t < 1.3:
            draw_rope(img, sets["tendril-flare"], ax + 12, y, bx - 12, y, min(2, int((t - 1.0) / 0.1)))
        elif t < 3.5:
            draw_rope(img, sets["tendril"], ax + 12, y, bx - 12, y, i % 4, sway=3, t=t)
        elif t < 4.0:
            cut = 160
            draw_rope(img, sets["tendril"], ax + 12, y, cut - 2, y, i % 4)
            draw_rope(img, sets["tendril"], cut + 2, y, bx - 12, y, i % 4)
            img = add_c(img, sets["tendril-burn"][i % 4], cut, y)
        else:
            k = min(5, int((t - 4.0) / 0.067))
            rec = 1 - (t - 4.0) / 2.0
            draw_rope(img, sets["tendril-wither"], ax + 12, y, ax + 12 + (148 - 12) * max(rec, 0.1), y, k)
            draw_rope(img, sets["tendril-wither"], bx - 12 - (148 - 12) * max(rec, 0.1), y, bx - 12, y, k)
        if 3.5 <= t < 4.0:
            img = add_c(img, sets["tendril-burn"][i % 4], 160, y)
        paste_c(img, anc[24 * 3 + (i // 2) % 3], ax, y)
        paste_c(img, anc[8 * 3 + (i // 2) % 3], bx, y)
        if 3.5 <= t < 4.0:
            ion = Image.new("RGBA", img.size, (0, 0, 0, 0))
            ImageDraw.Draw(ion).line([(160, y), (160, H_)], fill=(120, 200, 255, 255), width=4)
            img.alpha_composite(ion)
        frames.append(img)
    artkit.save_review(sheet, frames, GROUND_CONCEPT, "tendril-anchor", fps=10)


def chain_points(t, n):
    """Positions and headings along a wandering path down a 320x300 plate (crawling up-canyon: the head leads)."""
    def path(tt):
        return np.array([160 + 70 * math.sin(tt * 0.9) + 20 * math.sin(tt * 2.1), 320 - 55 * tt])
    gaps = [30] + [24] * (n - 2) + [22]
    pts, acc, tt, prev = [path(t)], 0.0, t, path(t)
    j, target = 1, gaps[0]
    while j < n and tt > t - 30:
        tt -= 0.002
        cur = path(tt)
        acc += np.linalg.norm(cur - prev)
        prev = cur
        if acc >= target:
            pts.append(cur)
            j += 1
            target += gaps[min(j - 1, len(gaps) - 1)]
    heads = []
    for i, p in enumerate(pts):
        ahead = pts[i - 1] if i else path(t + 0.01)
        d = ahead - p
        heads.append(float(math.atan2(-d[0], d[1])))
    return pts, heads


def crawler_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    hd, sg, tl = load("threadcrawler-head"), load("threadcrawler-segment"), load("threadcrawler-tail")
    pore, hk = load("threadcrawler-pore"), load("threadcrawler-husk")
    rows = [("HEAD: 16 HEADINGS (JAW 0)", hd[0::4], 1, False),
            ("HEAD JAW FRAMES (HEADING 0)", hd[0:4], 2, False),
            ("SEGMENT: 16 HEADINGS (PHASE 0)", sg[0::4], 1, False),
            ("SEGMENT LEG FRAMES (HEADING 4)", sg[16:20], 2, False),
            ("TAIL: 16 HEADINGS", tl, 1, False),
            ("PORE GLOW (ADDITIVE): TELL 0-2, FLASH 3", pore, 3, True),
            ("HUSK", hk, 1, False)]
    sheet = artkit.review_sheet("THREADCRAWLER - FINAL (ROUND 06 LOOK A)", rows, width=1400, batch=BATCH)
    frames = []
    n = 14
    for i in range(60):
        t = 2.0 + i / 10 * 0.7
        img = floor_plate(320, 300, 120, (400 - i * 3) % 600)
        pts, heads = chain_points(t, n)
        glow = []
        for k in range(len(pts) - 1, -1, -1):
            hk_ = int(round(heads[k] / TAU * 16)) % 16
            x, y = pts[k]
            if k == 0:
                fr = hd[hk_ * 4 + (i // 2) % 4]
            elif k == len(pts) - 1:
                fr = tl[hk_]
            else:
                ph = (i // 2 - k) % LEG_PHASES
                fr = sg[hk_ * 4 + ph]
                wave = (i / 10 / 0.25) % 16
                if 0 <= wave - k < 1.2:
                    glow.append((x, y, 2 if wave - k < 1 else 3))
            paste_c(img, fr, x, y)
        for x, y, g in glow:
            img = add_c(img, pore[g], x, y)
        frames.append(img)
    artkit.save_review(sheet, frames, GROUND_CONCEPT, "threadcrawler", fps=10)


def props_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    hut, hglow, rover = load("relay-hut"), load("relay-hut-glow"), load("canyon-rover")
    spark, dust = load("wall-spark"), load("wall-spark-dust")
    rows = [("RELAY HUT 48X40: SEALED, 1 HIT, 2 HITS (DOOR BUCKLED), BURST (THE CRATE)", hut, 3, False),
            ("RELAY HUT BEACON (ADDITIVE): SLOW BLINK 0-3, HURRIED 4-7", hglow, 2, True),
            ("CARGO ROVER 36X24: INTACT, DAMAGED, WRECK", rover, 3, False),
            ("WALL SPARK (ADDITIVE, 30 FPS)", spark, 3, True),
            ("WALL SPARK DUST", dust, 3, False)]
    sheet = artkit.review_sheet("LEVEL 16 PROPS - RELAY HUT, CARGO ROVERS, WALL SPARK", rows, width=1400, batch=BATCH)
    frames = []
    wall = BACKDROP / "wall-foot.png"
    foot = Image.open(wall).convert("RGBA") if wall.exists() else None
    for i in range(40):
        img = floor_plate(320, 240, 100, 200)
        if foot is not None:
            img.alpha_composite(foot.crop((0, 0, 24, 240)), (40, 0))
            dark = Image.new("RGBA", (40, 240), (60, 30, 20, 255))
            img.alpha_composite(dark, (0, 0))
        state = min(3, i // 8)
        paste_c(img, hut[state], 66, 70)
        img = add_c(img, hglow[(i // 2) % 4 if state == 0 else 4 + (i // 2) % 4], 66, 70)
        for k, (x, y) in enumerate(((200, 60), (196, 110), (204, 160))):
            paste_c(img, rover[min(2, max(0, (i - 10 * k) // 10))], x, y)
        f = i % 8
        if f < 6:
            paste_c(img, dust[f], 52, 200)
            img = add_c(img, spark[f], 52, 200)
        frames.append(img)
    artkit.save_review(sheet, frames, LEVEL / "concept", "l16-props", fps=10)


def review():
    anchor_review()
    crawler_review()
    props_review()


if __name__ == "__main__":
    if "--review" not in sys.argv[1:]:
        build()
    review()
