#!/usr/bin/env python3
"""Production art: Level 17's units, ally and props (M7 part D batch, concept round 40, step ART-D):
the Ares Landing dome (design/allies, the chosen round-36 look a "geodesic glass on a concrete ring",
tools/concept/mars_r36.dome_a and geodesic_mask imported unchanged), the Choir Herald (design/enemies/
air/choir-herald, the chosen round-36 look b "seraph", tools/concept/enemies_r36.herald_b unchanged),
the Shellback (design/enemies/ground/shellback, the chosen round-05 look a, tools/concept/render/
beast_models.shellback unchanged for the walk and the ball; the curl's in-between frames from a copy of
that model with the tuck as a continuous value, see ``shell_scene``) and the level's secret (design/
campaign/act-3-red-dust/level-17-dome-siege: the collapsed greenhouse frame over the seed-vault capsule;
straight to production, no concept: designed to the README, Mars light and readability rule 7).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down (for 16:
4 = facing left, 8 = up, 12 = right); every heading its own render under the fixed key light.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.
SpriteUse; step G1: a level's `allies` block claims its domes' sprite sets and the HUD's `dome-pip`, the
greenhouse frame's look its contents `seed-capsule`):
  ares-dome_0..3            120x120, the intact dome (footprint 120, the ring 110 across, the glass 92):
                            the inner green light breathing (a slow loop 0-1-2-3, ~4 fps); the airlocks'
                            warm lamps; no annexes (the greenhouse annexes and the apron are the backdrop's
                            `dome-apron` piece, which stays when the dome breaks)
  ares-dome-crack-1..3      120x120 overlays, cumulative (crack-2 holds crack-1's): the crack pattern
                            spreading per hit, a dark shattered pane at each impact with pale crack lines
                            running out over the glass; drawn over whichever body frame shows (hard breaks
                            on its 2nd hit, so it shows crack-1 only; easy shows all three)
  ares-dome-flicker_0..3    120x120, the body at its last hit before breaking: the inner light stuttering
                            (0 dim, 1 half, 2 nearly out, 3 half; the game steps them irregularly)
  ares-dome-flash           120x120, additive: the white hit flash over the dome's shell (0.1 s)
  ares-dome-broken_0..5     120x120, the break: the glass caving in pane by pane as the struts sag, the
                            light going out (10 fps); frame 5 is the wreck that stays (the ring and its
                            airlocks intact, a low jagged cage, dark beds, glass shards)
  ares-dome-dust_0..9       160x160, stepped translucency: the dust plume rising out of the broken dome
                            and spreading, lit from the upper left (8 fps; drawn over the wreck on the
                            ground pass, no debris on the player's plane)
  ares-dome-eden*           the same set for Eden at 144 (the dust 192): its glass lit brightest
  dome-pip                  12x12 flat white HUD icon (a dome on its ring), tinted by the DOMES tracker
  choir-herald_0..7         72x72, one heading (faces down, `fixed`): the two wing pairs' beat loop
                            (10 fps), the crest's six nodes lit
  choir-herald-crest_0..3   72x72, additive: the halo crest's violet bloom (a pulse loop 0-1-2-1... 6 fps);
                            off while the EMP stuns it
  choir-herald-ring_0..6    64x64, additive: a ringed unit's segmented violet shield ring, radius 26 (the
                            game scales it to the unit), frame k = k of its 6 segments lit over a faint
                            track (the ring thinning with its share, Q.22g: 6 full, 0 empty)
  choir-herald-ring-flash_0..2  64x64, additive: the absorb flash (white-violet, widening)
  choir-herald-aura         184x184, additive: the faint dashed circle of the 90 px aura radius (the game
                            scales it for easy 75 / hard 105 and pulses it)
  choir-herald-thread_0..3  16x8, additive: the thread of light from the Herald to a ringed unit, a strip
                            along x tiling every 16 px with motes running toward +x (the unit), 12 fps
  shellback_0..95           112x112, 16 headings x 6 heavy-walk frames (heading * 6 + frame; diagonal pairs)
  shellback-curl_0..63      112x112, 16 headings x 4 curl frames (heading * 4 + frame: head and legs drawn
                            in by a third, two thirds, fully tucked, half way into the ball): the 0.6 s
                            curl; the uncurl plays them backwards
  shellback-roll_0..127     80x80, 16 headings x 8 roll frames (heading * 8 + frame), the ball rolling
                            along the heading; 8 frames turn it by one band pair (72 degrees), so frame
                            k = (distance rolled / 3.7 px) mod 8 (the ball's radius 34 px)
  shellback-belly_0..15     112x112, additive, one per heading: the lime glow along the belly seam (the
                            shell's skirt) for the 1.5 s belly window after the uncurl
  shellback-vent_0..2       32x32, additive: the vent's swell glow (0.5 s tell: 0 low, 1 half, 2 full),
                            centred on the vent (assets/pivots/shellback.json)
  shellback-vent-dead       32x32: the destroyed vent, a burst stump with dead lime pulp, drawn over the
                            vent at its pivot for the rest of the unit's life
  shellback-husk_0..15      112x112, one per heading: the cracked dead shell, tucked, no glow
  greenhouse-frame_0..3     64x48, the secret's collapsed greenhouse frame over the seed-vault capsule: 0
                            whole (the capsule's amber light under the bent ribs), 1 hit (scorched), 2 badly
                            hit (ribs broken), 3 lifted aside (the bare scorched pad)
  seed-capsule              24x16, the freed seed-vault capsule (amber bands, a rule-7 light rim)
  seed-capsule-glow_0..5    24x16, additive: the capsule's beacon blinking under the frame (6 fps)
  assets/pivots/shellback.json   per heading the vent's centre (px from the 112x112 frame's top left) and
                            the ball's centre (the roll's 80x80 frame centred on the walk frame's)
  design/allies/ares-landing-dome/concept/ares-dome-final-r40-a.png/.gif
  design/enemies/air/concept/choir-herald-final-r40-a.png/.gif
  design/enemies/ground/concept/shellback-final-r40-a.png/.gif
  design/campaign/act-3-red-dust/level-17-dome-siege/concept/l17-props-final-r40-a.png/.gif

Run: python3 tools/art/siege_units.py [--review]   (~3 min; --review only rebuilds the review files)
"""
import contextlib
import json
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r36 as u36  # noqa: E402  (concept round 36: the chosen herald_b, imported unchanged)
import mars_r36 as m36  # noqa: E402  (concept round 36: the chosen dome_a and geodesic_mask, unchanged)
from render import beast_models as bm  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC, V_SEAM  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials, write_gif  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_x, rotate_z, sd_box, sd_capsule, sd_cylinder_z,  # noqa: E402
                        sd_ellipsoid, sd_sphere, union)

SCRIPT = "siege_units.py"
BATCH = "M7 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r40"
HELD = ROOT / "build" / "art-held" / "sprites"
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-17-dome-siege"
AIR_CONCEPT = DESIGN / "enemies" / "air" / "concept"
GROUND_CONCEPT = DESIGN / "enemies" / "ground" / "concept"
DOME_CONCEPT = DESIGN / "allies" / "ares-landing-dome" / "concept"
BACKDROP = ROOT / "assets" / "backdrop" / "level-17"

DOMES = {"ares-dome": (120, 1.0, 160), "ares-dome-eden": (144, 1.25, 192)}   # footprint, glass light, dust
DOME_R = 46.0                                   # the glass's radius at the 120 footprint (mars_r36.dome_sprite)
BREATH = (0.92, 1.0, 1.08, 1.0)
FLICKER = (0.3, 0.62, 0.08, 0.45)
BROKEN = 6
DUST = 10
HERALD, HERALD_EXTENT, BEATS = 72, 2.1, 8
CREST = (0.55, 0.8, 1.0, 0.8)
RING, RING_R, RING_SEGMENTS = 64, 26, 6
AURA, AURA_R = 184, 90
SHELL, SHELL_EXTENT, HEADINGS, WALK = 112, 2.3, 16, 6
CURL = (0.34, 0.67, 1.0, 1.5)                    # tuck share; above 1 the morph into the ball
BALL, ROLL = 80, 8
ROLL_STEP = TAU / 5 / ROLL                       # one band pair (72 degrees) over the 8 frames
VENT = 32
FRAME, CAPSULE = (64, 48), (24, 16)
VIOLET = np.array(u36.VIOLET, float)
VIOLET_HOT = np.array((226, 206, 255), float)
LIME = np.array(u36.LIME, float)
AMBER = np.array((255, 176, 64), float)
PX = SHELL / SHELL_EXTENT                        # the Shellback's px per model unit


# =========================================================================== held sprites

def _yaml(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


# A trigger look over contents with sprites of their own (vanguard.pipeline.SpriteUse.TRIGGER_CONTENTS).
TRIGGER_CONTENTS = {"greenhouse-frame": "seed-capsule"}


def level_sprite_roots():
    """The roots the levels' data claims (SpriteUse): the ground targets' looks with a trigger's contents, and a
    level's ground allies' sprite sets (their own `sprite`, else their ally's) with the HUD's `dome-pip`."""
    allies = _yaml(DESIGN / "allies" / "data.yaml")
    roots = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        data = _yaml(f)
        for g in data.get("ground_targets") or []:
            if g.get("sprite"):
                roots.add(str(g["sprite"]))
                if str(g["sprite"]) in TRIGGER_CONTENTS:
                    roots.add(TRIGGER_CONTENTS[str(g["sprite"])])
        block = data.get("allies")
        if isinstance(block, dict) and block.get("units"):
            for unit in block["units"]:
                spec = allies.get(unit.get("ally")) or {}
                roots.add(str(unit.get("sprite") or spec.get("sprite") or unit.get("ally")))
            roots.add("dome-pip")
    return roots


def _rooted(name, root):
    return name == root or name.startswith(root + "-")


def ally_roots():
    """The allies with a first_level (SpriteUse claims their slug in that level's atlas)."""
    data = _yaml(DESIGN / "allies" / "data.yaml")
    return {slug for slug, spec in data.items() if isinstance(spec, dict) and spec.get("first_level")}


def claimed(name):
    """Whether the data claims the sprite (SpriteUse): an enemy's root once its data.yaml exists, a ground
    target's look (and a trigger's contents) once a level names it, an ally's slug once it has a first_level, a
    level's ground allies' sprite sets and `dome-pip` once its `allies` block names them (step G1)."""
    for slug in ("choir-herald", "shellback"):
        if _rooted(name, slug):
            return any((DESIGN / "enemies").glob(f"*/{slug}/data.yaml"))
    if any(_rooted(name, r) for r in ally_roots()):
        return True
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


def _owned(stem, name):
    return stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit())


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            if _owned(old.stem, name):
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
        files = [f for f in folder.glob(f"{name}_*.png") if _owned(f.stem, name) and f.stem != name]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


# =========================================================================== shared

def turned(scene, mats, h):
    """A +Y-forward model turned to point ``h`` clockwise from straight down, its patterns with it."""
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def emissive_only(mats, override=None):
    """The materials with every non-emissive surface black (for an additive glow pass); ``override``
    maps a material index to the emission it gets instead."""
    black = Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0)
    out = []
    for i, m in enumerate(mats):
        if override is not None:
            out.append(Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0, emission=override[i])
                       if i in override else black)
        elif max(m.emission) > 0:
            out.append(Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0, emission=m.emission,
                                emission_pattern=m.emission_pattern))
        else:
            out.append(black)
    return out


def glow_image(lum, colour, halo=2.4, core=0.8, gain=1.0, levels=6, hot=None):
    """Additive bloom from a luminance array (0..1): a blurred halo round the bright core, stepped."""
    img = Image.fromarray((np.clip(lum, 0, 1) * 255).astype(np.uint8))
    h = np.asarray(img.filter(ImageFilter.GaussianBlur(halo)), float) / 255
    c = np.asarray(img.filter(ImageFilter.GaussianBlur(core)), float) / 255
    k = np.clip(h * 1.5 + c * 0.6, 0, 1) * gain
    rgb = np.asarray(colour, float)[None, None, :] * k[..., None]
    if hot is not None:
        rgb = rgb + np.asarray(hot, float) * np.clip(c * gain - 0.55, 0, 1)[..., None] * 0.8
    q = np.floor(np.clip(rgb / 255, 0, 1) * levels + 0.5) / levels * 255
    a = np.where(q.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([q, a]).astype(np.uint8), "RGBA")


def lum_of(hi, factor):
    small = sprite.downsample(hi, factor)
    return (small[..., :3] * small[..., 3:4]).max(axis=-1)


# =========================================================================== the dome

def dome_scale(name):
    return DOMES[name][0] / 120.0


def dome_parts(name, glow=1.0, squash=1.0, shell=True, ring=True):
    """mars_r36.dome_a without annexes at this dome's scale, its glass light x ``glow`` (and the dome's own
    brightness: Eden's lit brightest), the shell squashed to ``squash`` of its height (the cave-in); with
    ``shell`` False only the ring and airlocks, with ``ring`` False only the shell."""
    s = dome_scale(name)
    scene0, mats, _ = m36.dome_a(R=DOME_R * s, annex=False)
    light = glow * DOMES[name][1]
    mats = list(mats)
    mats[0] = replace(mats[0], emission=tuple(np.array(mats[0].emission) * light))
    R = DOME_R * s

    def scene(p):
        q = p.copy()
        q[:, 2] = 2 + (p[:, 2] - 2) / max(squash, 0.05)
        d, m = scene0(q)
        d = d * min(1.0, squash) if squash < 1 else d
        if not shell or not ring:
            on_shell = (np.hypot(p[:, 0], p[:, 1]) < R + 0.6) & (p[:, 2] > 2.2)
            if not shell:
                d = np.where(on_shell, 1e3, d)
            else:
                d = np.where(on_shell, d, 1e3)
        return d, m
    return scene, mats


def dome_render(name, glow=1.0, squash=1.0, shell=True, ring=True, mats_fn=None):
    size = DOMES[name][0]
    scene, mats = dome_parts(name, glow, squash, shell, ring)
    if mats_fn is not None:
        mats = mats_fn(mats)
    return m36.sdf.render(scene, mats, (size * 4, size * 4), float(size), z_top=120 * dome_scale(name),
                          steps=160), 4


def dome_body(job):
    name, glow = job
    return artkit.native(*dome_render(name, glow), crisp=60)


def dome_flash(name):
    """The white hit flash: the shell's silhouette (glass, struts and hub), white, brighter toward its rim
    (the glass's sheen), stepped."""
    hi, f = dome_render(name, 1.0, ring=False,
                        mats_fn=lambda mats: emissive_only(mats, {0: (1.0, 1.0, 1.0), 1: (1.0, 1.0, 1.0),
                                                                  3: (1.0, 1.0, 1.0)}))
    lum = lum_of(hi, f)
    size = DOMES[name][0]
    yy, xx = np.mgrid[0:size, 0:size]
    r = np.hypot(xx - size / 2 + 0.5, yy - size / 2 + 0.5) / (DOME_R * dome_scale(name))
    k = np.clip(lum, 0, 1) * (0.45 + 0.4 * np.clip(r, 0, 1) ** 2)
    q = artkit.ordered_dither(np.clip(k, 0, 1), 7)
    rgb = np.array((235, 255, 242), float) * q[..., None]
    a = np.where(q > 0, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


IMPACTS = [((0.38, -0.3), 31), ((-0.42, 0.12), 37), ((0.08, 0.5), 43)]


def crack_overlay(name, hits):
    """Cumulative cracks: a dark shattered pane at each impact, pale crack lines running out (each with a
    dark lower-right edge for contrast), clipped to the glass."""
    size = DOMES[name][0]
    R = DOME_R * dome_scale(name)
    c = size / 2
    S = 4
    big = Image.new("RGBA", (size * S, size * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    for (ix, iy), seed in IMPACTS[:hits]:
        rng = np.random.default_rng(seed)
        px, py = c + ix * R, c + iy * R
        pane = []
        for k in range(9):
            a = TAU * k / 9 + rng.uniform(-0.2, 0.2)
            rr = R * rng.uniform(0.1, 0.17)
            pane.append(((px + math.cos(a) * rr) * S, (py + math.sin(a) * rr) * S))
        d.polygon(pane, fill=(14, 40, 28, 255), outline=(150, 196, 172, 255))
        lines = []
        for k in range(9):
            a = TAU * k / 9 + rng.uniform(-0.3, 0.3)
            x, y = px, py
            pts = [(x, y)]
            for _ in range(int(rng.integers(4, 8))):
                a += rng.uniform(-0.45, 0.45)
                step = R * rng.uniform(0.07, 0.11)
                x, y = x + math.cos(a) * step, y + math.sin(a) * step
                pts.append((x, y))
                if rng.random() < 0.25:
                    b = a + rng.choice((-1, 1)) * rng.uniform(0.6, 1.1)
                    lines.append([(x, y), (x + math.cos(b) * step * 1.4, y + math.sin(b) * step * 1.4)])
            lines.append(pts)
        for pts in lines:
            d.line([((x + 0.7) * S, (y + 0.7) * S) for x, y in pts], fill=(10, 30, 20, 255), width=S)
        for pts in lines:
            d.line([(x * S, y * S) for x, y in pts], fill=(214, 250, 230, 255), width=S)
        d.ellipse([(px - 1.6) * S, (py - 1.6) * S, (px + 1.6) * S, (py + 1.6) * S], fill=(255, 255, 240, 255))
    a = np.array(big.resize((size, size), Image.BOX)).astype(np.float64)
    yy, xx = np.mgrid[0:size, 0:size]
    inside = np.hypot(xx - c + 0.5, yy - c + 0.5) < R * 0.95
    alpha = np.where((a[..., 3] >= 110) & inside, 255, 0)
    out = np.dstack([np.clip(a[..., :3], 0, 255), alpha]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def _cells(n):
    """The geodesic cell of unit directions n (the triangle lattice of mars_r36.geodesic_mask's three
    great-circle families): an integer triple per point, and the strut mask."""
    ids, strut = [], np.zeros(len(n), bool)
    for a in (0.0, TAU / 6, TAU / 3):
        ax = np.array([np.cos(a), np.sin(a), 0.0])
        v = np.arcsin(np.clip(n @ ax, -1, 1)) * 6.5 / np.pi
        ids.append(np.floor(v).astype(int))
        strut |= np.abs(v - np.round(v)) < 0.075
    return ids, strut


def _hash(ids, seed):
    h = (ids[0] * 73856093) ^ (ids[1] * 19349663) ^ (ids[2] * 83492791) ^ seed
    return ((h * 2654435761) % 4294967296) / 4294967296.0


def dome_interior(name, k):
    """The dome's floor as the falling glass lays it open: the crop beds (dark green rows, their light
    going out), the central hub, glass shards lying on them; frame k of the break."""
    size = DOMES[name][0]
    s = dome_scale(name)
    R = DOME_R * s
    rng = np.random.default_rng(170 + k)
    light = max(0.0, 1 - (k + 1) / BROKEN) ** 1.5 * DOMES[name][1]
    bed = Material((0.12, 0.2, 0.09), metal=0.0, shininess=8, spec=0.05,
                   emission=(0.02 * light, 0.32 * light, 0.1 * light),
                   pattern=lambda p, n: np.where(np.sin(p[:, 1] * 0.55) > 0, 1.0, 0.55))
    path = Material((0.42, 0.38, 0.34), metal=0.0, shininess=10, spec=0.1)
    shard = Material((0.62, 0.78, 0.7), metal=0.7, shininess=120, spec=1.2)
    strut = Material((0.7, 0.72, 0.7), metal=0.3, shininess=60, spec=0.6)
    shards = [(rng.uniform(-R * 0.8, R * 0.8), rng.uniform(-R * 0.8, R * 0.8), rng.uniform(0, TAU),
               rng.uniform(2.0, 4.5)) for _ in range(int(14 + 4 * k))]
    beams = [(rng.uniform(0, TAU), rng.uniform(0.4, 0.95)) for _ in range(5 + k)]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0), R, 2.6), 0),
                 (sd_box(p, (0, 0, 2.8), (R, 2.2, 0.4)), 1),
                 (sd_box(p, (0, 0, 2.8), (2.2, R, 0.4)), 1),
                 (sd_cylinder_z(p, (0, 0, 2.6), 6 * s, 1.2), 1)]
        for x, y, a, ln in shards:
            if math.hypot(x, y) < R * 0.92:
                q = rotate_z(p - np.array([x, y, 3.0]), a)
                items.append((sd_box(q, (0, 0, 0), (ln, ln * 0.45, 0.35)), 2))
        for a, rr in beams:                        # fallen struts across the beds
            x0, y0 = math.cos(a) * R * rr, math.sin(a) * R * rr
            x1, y1 = math.cos(a + 1.2) * R * rr * 0.6, math.sin(a + 1.2) * R * rr * 0.6
            items.append((sd_capsule(p, (x0, y0, 3.4), (x1, y1, 3.2), 0.9), 3))
        return union(*items)
    hi = m36.sdf.render(scene, [bed, path, shard, strut], (size * 4, size * 4), float(size), z_top=40, steps=120)
    return hi


def dome_broken(job):
    """Frame k of the break: the shell squashed (the struts sagging), its panes falling out cell by cell
    (a 2D cell mask from the projected hemisphere, so the shell's render stays a clean ray-march), the
    interior showing through; the light going out."""
    name, k = job
    size = DOMES[name][0]
    s = dome_scale(name)
    R = DOME_R * s
    c = (k + 1) / BROKEN
    squash = 1 - 0.62 * (c * c * (3 - 2 * c))
    glow = max(0.0, 1 - c) ** 1.5
    shell_hi, f = dome_render(name, glow, squash, ring=False)
    ring_hi, _ = dome_render(name, 0.0, 1.0, shell=False)
    inner = dome_interior(name, k)
    n_px = size * f
    yy, xx = np.mgrid[0:n_px, 0:n_px]
    x = (xx + 0.5 - n_px / 2) / f
    y = -(yy + 0.5 - n_px / 2) / f
    r = np.hypot(x, y) / R
    nz = np.sqrt(np.clip(1 - r * r, 0, 1))
    n = np.stack([x / R, y / R, nz], axis=-1).reshape(-1, 3)
    ids, strut = _cells(n)
    gone = (_hash(ids, 17) < 0.25 + 0.7 * c).reshape(n_px, n_px)
    broken_strut = (_hash([i // 2 for i in ids], 29) < 0.6 * c).reshape(n_px, n_px)
    keep = ~gone | (strut.reshape(n_px, n_px) & ~broken_strut)
    if k == BROKEN - 1:                            # the wreck: only a low jagged cage near the rim
        keep &= (r > 0.62 + 0.18 * _hash([i * 3 for i in ids], 41).reshape(n_px, n_px)) | (
            strut.reshape(n_px, n_px) & ~broken_strut & (r > 0.55))
    sa = shell_hi[..., 3] * keep
    out = inner.copy()
    out[..., :3] = shell_hi[..., :3] * sa[..., None] + inner[..., :3] * (1 - sa[..., None])
    out[..., 3] = np.maximum(inner[..., 3], sa)
    ra = ring_hi[..., 3:4] * (r > 0.985)[..., None]   # the ring's own floor disc stays under the interior
    out[..., :3] = ring_hi[..., :3] * ra + out[..., :3] * (1 - ra)
    out[..., 3] = np.maximum(out[..., 3], ra[..., 0])
    img = artkit.native(out, f, crisp=60)
    a = np.array(img).astype(np.float64)              # dust settling on what is left
    a[..., :3] = a[..., :3] * (1 - 0.18 * c) + np.array((150, 110, 80)) * 0.18 * c
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def dome_dust(name, k):
    """Frame k of the dust plume: a ragged ochre-grey cloud welling out of the broken dome and spreading
    as it rises, lit from the upper left, thinning; stepped translucency."""
    size = DOMES[name][2]
    s = dome_scale(name)
    t = (k + 1) / DUST
    n = m36.nfield(size, size, int(22 * s), 4170 + k, octaves=4)
    n2 = m36.nfield(size, size, int(9 * s), 4190, octaves=2)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float64)
    cx, cy = size / 2 - 4 * t * s, size / 2 - 6 * t * s          # it drifts up-left a little as it rises
    r = np.hypot(xx - cx, yy - cy) / (size / 2)
    reach = 0.32 + 0.6 * t ** 0.7
    body = np.clip(1 - r / reach, 0, 1) ** 0.5 * (0.15 + 1.1 * n + 0.3 * n2) * (1 - 0.7 * t ** 1.4)
    body = Image.fromarray((np.clip(body, 0, 1) * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.2))
    body = np.asarray(body, float) / 255
    lit = np.clip(0.55 + 6.0 * (body - np.roll(np.roll(body, 2, 0), 2, 1)), 0, 1)   # lit from the upper left
    ramp_ = np.array([(70, 56, 46), (116, 92, 72), (162, 130, 100), (204, 176, 140)], float)
    v = np.clip(lit * 0.8 + body * 0.3, 0, 0.999) * (len(ramp_) - 1)
    i = np.floor(v).astype(int)
    rgb = ramp_[i] + (ramp_[np.minimum(i + 1, len(ramp_) - 1)] - ramp_[i]) * (v - i)[..., None]
    alpha = artkit.ordered_dither(np.clip((body - 0.12) / 0.5, 0, 1) * 0.72, 5)
    out = np.dstack([rgb, alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def dome_pip():
    img = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.pieslice([6, 8, 42, 44], 180, 360, fill=(255, 255, 255, 255))
    d.rectangle([2, 27, 46, 34], fill=(255, 255, 255, 255))
    d.rectangle([0, 37, 48, 42], fill=(255, 255, 255, 255))
    for x in (14, 24, 34):                          # the struts read as gaps at 12 px
        d.line([(x, 12), (x, 25)], fill=(0, 0, 0, 0), width=2)
    small = img.resize((12, 12), Image.BOX)
    a = np.array(small)
    a[..., 3] = np.where(a[..., 3] > 100, 255, 0)
    a[..., :3] = 255
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


# =========================================================================== the Choir Herald

def herald_model(beat, crest=1.0):
    return turned(*u36.herald_b(beat, 1.0, crest), 0.0)


def herald_frame(k):
    beat = math.sin(TAU * k / BEATS)
    return artkit.native(*artkit.render_hi(*herald_model(beat), (HERALD, HERALD), HERALD_EXTENT))


def herald_crest(gain):
    scene, mats = herald_model(0.0, 1.0)
    hi, f = artkit.render_hi(scene, emissive_only(mats, {V_EYE: (1.0, 1.0, 1.0)}), (HERALD, HERALD),
                             HERALD_EXTENT, shadows=False)
    return glow_image(lum_of(hi, f), VIOLET, halo=2.2, core=0.7, gain=gain, hot=VIOLET_HOT)


def herald_intel_model():
    """(scene, mats) for the hangar intel portrait: facing down, wings level, the crest lit."""
    return herald_model(0.3)


def _ring_canvas(n, S=4):
    return Image.new("RGBA", (n * S, n * S), (0, 0, 0, 0)), S


def _finish_additive(big, n, levels=5):
    a = np.array(big.resize((n, n), Image.BOX)).astype(np.float64)
    rgb = a[..., :3] * a[..., 3:4] / 255
    q = np.floor(np.clip(rgb / 255, 0, 1) * levels + 0.5) / levels * 255
    alpha = np.where(q.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([q, alpha]).astype(np.uint8), "RGBA")


def ring_frame(lit):
    """k of the six segments lit, over the faint track of the rest; a soft violet halo round the lit."""
    big, S = _ring_canvas(RING)
    c, r = RING * S / 2, RING_R * S
    halo = Image.new("RGBA", big.size, (0, 0, 0, 0))
    dh = ImageDraw.Draw(halo)
    d = ImageDraw.Draw(big)
    for k in range(RING_SEGMENTS):
        a0 = -90 + k * 60 + 6
        box = [c - r, c - r, c + r, c + r]
        if k < lit:
            dh.arc(box, a0, a0 + 48, fill=tuple(VIOLET.astype(int)) + (150,), width=4 * S)
            d.arc(box, a0, a0 + 48, fill=tuple(VIOLET.astype(int)) + (255,), width=int(2.5 * S))
            d.arc(box, a0 + 4, a0 + 44, fill=tuple(VIOLET_HOT.astype(int)) + (255,), width=S)
        else:
            d.arc(box, a0, a0 + 48, fill=tuple((VIOLET * 0.32).astype(int)) + (255,), width=S)
    halo = halo.filter(ImageFilter.GaussianBlur(3 * S))
    halo.alpha_composite(big)
    return _finish_additive(halo, RING)


def ring_flash(k):
    big, S = _ring_canvas(RING)
    c = RING * S / 2
    r = (RING_R + 2 + 3 * k) * S
    d = ImageDraw.Draw(big)
    g = 1.0 - 0.3 * k
    col = tuple(int(v) for v in (VIOLET_HOT * g + 255 * (1 - g) * 0.2))
    d.ellipse([c - r, c - r, c + r, c + r], outline=col + (255,), width=int((3 - k) * S + S))
    d.ellipse([c - r + 6 * S, c - r + 6 * S, c + r - 6 * S, c + r - 6 * S],
              outline=tuple((VIOLET * 0.6 * g).astype(int)) + (255,), width=S)
    big = big.filter(ImageFilter.GaussianBlur(S * 0.8))
    return _finish_additive(big, RING)


def aura_circle():
    big, S = _ring_canvas(AURA, 2)
    c, r = AURA * S / 2, AURA_R * S
    d = ImageDraw.Draw(big)
    for k in range(45):                              # dashes every 8 degrees
        a0 = k * 8
        d.arc([c - r, c - r, c + r, c + r], a0, a0 + 5, fill=tuple((VIOLET * 0.55).astype(int)) + (255,),
              width=S * 2)
    d.ellipse([c - r + 3 * S, c - r + 3 * S, c + r - 3 * S, c + r - 3 * S],
              outline=tuple((VIOLET * 0.22).astype(int)) + (255,), width=S)
    return _finish_additive(big, AURA, levels=6)


def thread_frame(k):
    w, h = 16, 8
    S = 4
    big = Image.new("RGBA", (w * S * 3, h * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    y = h * S / 2
    d.line([(0, y), (big.width, y)], fill=tuple((VIOLET * 0.55).astype(int)) + (255,), width=S)
    for rep in range(3):
        x = (rep * w + 4 * k + 2) * S
        d.ellipse([x - 1.6 * S, y - 1.6 * S, x + 1.6 * S, y + 1.6 * S], fill=tuple(VIOLET_HOT.astype(int)) + (255,))
    big = big.filter(ImageFilter.GaussianBlur(S * 0.7))
    crop = big.crop((w * S, 0, 2 * w * S, h * S))
    a = np.array(crop.resize((w, h), Image.BOX)).astype(np.float64)
    rgb = a[..., :3] * a[..., 3:4] / 255 * 1.4
    q = np.floor(np.clip(rgb / 255, 0, 1) * 5 + 0.5) / 5 * 255
    alpha = np.where(q.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([q, alpha]).astype(np.uint8), "RGBA")


# =========================================================================== the Shellback

def shell_scene(phase=0.0, tuck=0.0, pulse=1.0, glow=1.0):
    """beast_models.shellback's walk pose with the tuck as a continuous value 0..1 (the head and legs drawn
    in by that share; the concept model knows only 0 and 1); above 1 the tucked body morphs into the ball
    (the share tuck - 1 of the way, the SDFs blended). At tuck 0 and 1 it is the concept model."""
    if tuck <= 0.0:
        return bm.shellback(phase, "walk", 0.0, pulse, glow)
    if tuck == 1.0:
        return bm.shellback(phase, "tuck", 0.0, pulse, glow)
    if tuck > 1.0:
        s = min(1.0, tuck - 1.0)
        body, mats = bm.shellback(0.0, "tuck", 0.0, pulse, glow)
        ball, _ = bm.shellback(0.0, "ball", 0.0, 1.0, glow)

        def morph(p):
            d0, m0 = body(p)
            d1, m1 = ball(p)
            return d0 * (1 - s) + d1 * s, np.where(s < 0.5, m0, m1)
        return morph, mats
    walk, mats = bm.shellback(phase, "walk", 0.0, pulse, glow)
    tucked, _ = bm.shellback(phase, "tuck", 0.0, pulse, glow)

    def scene(p):                                   # blending the two poses' fields draws the limbs in
        d0, m0 = walk(p)
        d1, m1 = tucked(p)
        return d0 * (1 - tuck) + d1 * tuck, np.where(tuck < 0.5, m0, m1)
    return scene, mats


def shell_frame(job):
    kind, k, j = job
    h = TAU * k / HEADINGS
    if kind == "walk":
        scene, mats = shell_scene(phase=TAU * j / WALK)
        size, extent = SHELL, SHELL_EXTENT
    elif kind == "curl":
        scene, mats = shell_scene(phase=0.0, tuck=CURL[j])
        size, extent = SHELL, SHELL_EXTENT
    elif kind == "roll":
        scene, mats = bm.shellback(0.0, "ball", j * ROLL_STEP)
        size, extent = BALL, SHELL_EXTENT * BALL / SHELL
    elif kind == "husk":
        return husk_frame(h)
    elif kind == "belly":
        return belly_frame(h)
    else:
        raise ValueError(kind)
    return artkit.native(*artkit.render_hi(*turned(scene, mats, h), (size, size), extent))


def husk_frame(h):
    """The dead shell: tucked, no glow, the slate darkened and dusted, cracks across the scutes, the vent
    burst."""
    scene, mats = bm.shellback(0.0, "tuck", 0.0, 0.6, 0.0)
    mats = [replace(m, albedo=tuple(np.array(m.albedo) * 0.62), emission=(0.0, 0.0, 0.0), spec=m.spec * 0.5)
            for m in mats]
    img = artkit.native(*artkit.render_hi(*turned(scene, mats, h), (SHELL, SHELL), SHELL_EXTENT))
    a = np.array(img).astype(np.float64)
    rng = np.random.default_rng(int(h * 100) + 7)
    crack = Image.new("L", img.size, 0)
    d = ImageDraw.Draw(crack)
    c = SHELL / 2
    for _ in range(6):
        ang = rng.uniform(0, TAU)
        x, y = c + rng.uniform(-6, 6), c + rng.uniform(-6, 6)
        pts = [(x, y)]
        for _ in range(5):
            ang += rng.uniform(-0.5, 0.5)
            x, y = x + math.cos(ang) * 5, y + math.sin(ang) * 5
            pts.append((x, y))
        d.line(pts, fill=255, width=1)
    m = (np.array(crack) > 0) & (a[..., 3] > 0)
    a[m, :3] = (22, 18, 18)
    dust = np.array((120, 84, 60), float)
    a[..., :3] = np.where(a[..., 3:4] > 0, a[..., :3] * 0.8 + dust * 0.2, 0)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def belly_frame(h):
    """The lime glow along the belly seam at heading h, for the belly window: the shell's skirt (the
    model's 0.72 x 0.88 ellipse round its rim) as a thin glowing seam, a little brighter at the sides."""
    S = 4
    n = SHELL * S
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float64)
    x = (xx + 0.5 - n / 2) / (PX * S)
    y = (yy + 0.5 - n / 2) / (PX * S)
    ang = np.pi / 2 + h                              # the concept's screen heading: local +Y along it
    c, s = math.cos(ang), math.sin(ang)
    ly = x * c + y * s
    lx = -x * s + y * c
    e = np.hypot(lx / 0.7, ly / 0.86)
    seam = np.clip(1 - np.abs(e - 1) / 0.035, 0, 1) * (0.75 + 0.25 * np.abs(lx) / np.maximum(np.hypot(lx, ly), 1e-6))
    lum = np.asarray(Image.fromarray((seam * 255).astype(np.uint8)).resize((SHELL, SHELL), Image.BOX), float) / 255
    return glow_image(lum * 1.6, LIME, halo=1.6, core=0.5, gain=1.0, hot=(230, 255, 190))


def vent_glow(k):
    gain = (0.45, 0.75, 1.0)[k]
    n, S = VENT, 4
    yy, xx = np.mgrid[0:n * S, 0:n * S]
    r = np.hypot(xx - n * S / 2, yy - n * S / 2) / S
    core = np.clip(1 - r / (5 + 3 * k), 0, 1)
    lum = Image.fromarray((core * 255).astype(np.uint8)).resize((n, n), Image.BOX)
    return glow_image(np.asarray(lum, float) / 255, LIME, halo=2.5 + k, core=1.0, gain=gain, hot=(240, 255, 200))


def vent_dead():
    """The burst vent: a ragged dark socket with torn rim plates and dead olive pulp, lit from the upper
    left, 1-bit alpha."""
    n, S = VENT, 4
    rng = np.random.default_rng(611)
    big = Image.new("RGBA", (n * S, n * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = n * S / 2
    rim = [(c + math.cos(a) * (10.5 + rng.uniform(-1.5, 1.8)) * S, c + math.sin(a) * (10.5 + rng.uniform(-1.5, 1.8)) * S)
           for a in np.linspace(0, TAU, 14, endpoint=False)]
    d.polygon(rim, fill=(58, 62, 66, 255))
    d.polygon([(x - 0.8 * S, y - 0.8 * S) for x, y in rim[:6]] + [(c, c)], fill=(104, 110, 116, 255))
    hole = [(c + math.cos(a) * (6.5 + rng.uniform(-1.5, 1.5)) * S, c + math.sin(a) * (6.5 + rng.uniform(-1.5, 1.5)) * S)
            for a in np.linspace(0, TAU, 11, endpoint=False)]
    d.polygon(hole, fill=(16, 14, 14, 255))
    for _ in range(5):
        x, y = c + rng.uniform(-4, 4) * S, c + rng.uniform(-4, 4) * S
        r = rng.uniform(1.0, 2.0) * S
        d.ellipse([x - r, y - r, x + r, y + r], fill=(70, 86, 30, 255))
    d.ellipse([c - 1.2 * S, c - 2.4 * S, c + 0.6 * S, c - 0.6 * S], fill=(120, 150, 40, 255))
    a = np.array(big.resize((n, n), Image.BOX)).astype(np.float64)
    alpha = np.where(a[..., 3] >= 128, 255, 0)
    out = np.dstack([np.clip(a[..., :3], 0, 255), alpha]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def vent_pivots():
    """Per heading the vent's centre in the 112 frame (px from the top left), from the model's vent at
    local (0, -0.3), and the ball's centre (the frame's)."""
    out = []
    for k in range(HEADINGS):
        h = np.pi / 2 + TAU * k / HEADINGS           # the concept's screen heading
        c, s = math.cos(h), math.sin(h)
        lx, ly = 0.0, -0.3
        dx, dy = PX * (ly * c - lx * s), PX * (ly * s + lx * c)
        out.append([round(SHELL / 2 + dx, 1), round(SHELL / 2 + dy, 1)])
    return out


def shellback_intel_model():
    """(scene, mats) for the hangar intel portrait: walking down, the vent swollen."""
    return turned(*bm.shellback(TAU / WALK, "walk", 0.0, 1.2), 0.0)


# =========================================================================== the secret

def frame_scene(state):
    """The collapsed greenhouse frame (state 0-2) over the capsule, or its lifted wreck (3): a scorched
    concrete pad, the tube greenhouse's white ribs fallen half flat across it, leaning, a ridge beam, glass
    shards, the capsule's amber bands showing between the ribs."""
    rib = Material((0.86, 0.86, 0.82), metal=0.3, shininess=60, spec=0.6)
    burnt = Material((0.32, 0.29, 0.26), metal=0.2, shininess=20, spec=0.2)
    pad = Material((0.5, 0.45, 0.4), metal=0.0, shininess=10, spec=0.1,
                   pattern=lambda p, n: 0.9 + 0.1 * np.sin(p[:, 0] * 0.35 + 1.3 * np.sin(p[:, 1] * 0.2)))
    glass = Material((0.5, 0.68, 0.62), metal=0.3, shininess=140, spec=1.0)
    cap = Material((0.62, 0.6, 0.56), metal=0.5, shininess=80, spec=0.9)
    band = Material((0.9, 0.55, 0.15), metal=0.3, shininess=60, spec=0.6, emission=(0.5, 0.25, 0.04))
    rng = np.random.default_rng(76)
    arches = [(-22 + 9 * k + rng.uniform(-1.5, 1.5), rng.uniform(-0.5, 0.5), rng.uniform(5.0, 8.0)) for k in range(6)]
    shards = [(rng.uniform(-22, 22), rng.uniform(-15, 15), rng.uniform(0, TAU)) for _ in range(9)]
    sag = (1.0, 0.8, 0.55, 0.0)[state]

    def arch(p, x, lean, h):
        pts = []
        for j in range(7):
            th = math.pi * j / 6
            pts.append((x + lean * 6 * math.sin(th), -13 * math.cos(th), 1.0 + h * sag * math.sin(th)))
        return np.minimum.reduce([sd_capsule(p, pts[j], pts[j + 1], 0.85) for j in range(6)])

    def scene(p):
        items = [(sd_box(p, (0, 0, 0.5), (29, 21, 0.5), 1.0), 2)]
        if state < 3:
            items.append((sd_capsule(p, (-8, -2, 3.4), (8, -2, 3.4), 3.0), 4))
            for k in range(3):
                items.append((sd_box(p, (-5 + 5 * k, -2, 3.4), (0.8, 3.1, 3.1)), 5))
            for k, (x, lean, h) in enumerate(arches):
                if state == 2 and k in (1, 4):
                    continue                          # two ribs broken off
                items.append((arch(p, x, lean, h), 0 if (state == 0 or k % 2) else 1))
            items.append((sd_capsule(p, (-24, 1, 1.0 + 6.5 * sag), (24, -1, 1.0 + 6.0 * sag), 0.8),
                          0 if state == 0 else 1))
            for x, y, a in shards[:9 - 2 * state]:
                q = rotate_z(p - np.array([x, y, 1.3]), a)
                items.append((sd_box(q, (0, 0, 0), (3.0, 1.8, 0.3)), 3))
        else:                                        # lifted aside: the wreck at the pad's edge
            for k in range(4):
                q = rotate_z(p - np.array([-24 + 3 * k, 15, 1.5]), 0.2 * k)
                items.append((sd_box(q, (0, 0, 0), (0.9, 6.0, 0.9)), 1))
        return union(*items)
    return scene, [rib, burnt, pad, glass, cap, band]


def frame_frame(state):
    scene, mats = frame_scene(state)
    w, h = FRAME
    hi = sdf.render(scene, mats, (w * 8, h * 8), float(w), z_top=30, steps=120, ambient=0.42)
    img = artkit.native(hi, 8)
    if state in (1, 2):                              # scorch: darkened blotches
        a = np.array(img).astype(np.float64)
        n = m36.nfield(w, h, 8, 77 + state, octaves=3)
        dark = (n > 0.62 - 0.08 * state) & (a[..., 3] > 0)
        a[dark, :3] *= 0.45
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    if state == 3:
        a = np.array(img).astype(np.float64)
        yy, xx = np.mgrid[0:h, 0:w]
        soot = np.hypot((xx - w / 2) / 14, (yy - h / 2) / 10) < 1
        a[soot & (a[..., 3] > 0), :3] *= 0.55
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    return rim_light(img)


def rim_light(img, colour=(255, 226, 170)):
    """Readability rule 7: a 1 px warm light rim on the upper-left edges of the silhouette."""
    a = np.array(img).astype(np.int32)
    al = a[..., 3] > 0
    edge = al & ~np.roll(al, 1, axis=0) | al & ~np.roll(al, 1, axis=1)
    a[edge, :3] = (a[edge, :3] * 0.4 + np.array(colour) * 0.6).astype(np.int32)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def capsule():
    band = Material((0.9, 0.55, 0.15), metal=0.3, shininess=60, spec=0.6, emission=(0.35, 0.18, 0.02))
    hull = Material((0.66, 0.64, 0.6), metal=0.55, shininess=90, spec=1.0)
    cap = Material((0.4, 0.42, 0.44), metal=0.6, shininess=80, spec=0.9)

    def scene(p):
        items = [(sd_capsule(p, (-6.5, 0, 3.6), (6.5, 0, 3.6), 3.6), 1)]
        for x in (-4.5, 0.0, 4.5):
            items.append((sd_box(p, (x, 0, 3.6), (0.8, 3.75, 3.75)), 0))
        items.append((sd_sphere(p, (-10.4, 0, 3.6), 1.6), 2))
        items.append((sd_sphere(p, (10.4, 0, 3.6), 1.6), 2))
        return union(*items)
    w, h = CAPSULE
    hi = sdf.render(scene, [band, hull, cap], (w * 8, h * 8), float(w), z_top=20, steps=100)
    return rim_light(artkit.native(hi, 8))


def capsule_glow(k):
    gain = (0.35, 0.7, 1.0, 0.7, 0.35, 0.15)[k]
    w, h = CAPSULE
    yy, xx = np.mgrid[0:h, 0:w]
    lum = np.zeros((h, w))
    for x in (8.0, 16.0):
        lum = np.maximum(lum, np.clip(1 - np.hypot(xx - x + 0.5, yy - h / 2 + 0.5) / 2.2, 0, 1))
    return glow_image(lum, AMBER, halo=1.8, core=0.6, gain=gain, hot=(255, 236, 180))


# =========================================================================== build

def _job(job):
    kind, *a = job
    if kind == "dome":
        return dome_body(tuple(a))
    if kind == "dome-broken":
        return dome_broken(tuple(a))
    if kind == "dome-flash":
        return dome_flash(a[0])
    if kind == "dome-dust":
        return dome_dust(*a)
    if kind == "herald":
        return herald_frame(a[0])
    if kind == "crest":
        return herald_crest(CREST[a[0]])
    if kind in ("walk", "curl", "roll", "husk", "belly"):
        return shell_frame((kind, *a))
    if kind == "frame":
        return frame_frame(a[0])
    raise ValueError(kind)


def dome_jobs(name):
    return ([("dome", name, g) for g in BREATH] + [("dome", name, g) for g in FLICKER]
            + [("dome-broken", name, k) for k in range(BROKEN)] + [("dome-flash", name)]
            + [("dome-dust", name, k) for k in range(DUST)])


def build(only=None):
    groups = {
        "domes": [j for name in DOMES for j in dome_jobs(name)],
        "herald": [("herald", k) for k in range(BEATS)] + [("crest", k) for k in range(len(CREST))],
        "shellback": ([("walk", k, j) for k in range(HEADINGS) for j in range(WALK)]
                      + [("curl", k, j) for k in range(HEADINGS) for j in range(len(CURL))]
                      + [("roll", k, j) for k in range(HEADINGS) for j in range(ROLL)]
                      + [("belly", k, 0) for k in range(HEADINGS)] + [("husk", k, 0) for k in range(HEADINGS)]),
        "props": [("frame", s) for s in range(4)],
    }
    wanted = [g for g in groups if not only or g in only]
    jobs = [j for g in wanted for j in groups[g]]
    with ProcessPoolExecutor() as pool:
        out = dict(zip(map(repr, jobs), pool.map(_job, jobs, chunksize=2)))

    def got(js):
        return [out[repr(j)] for j in js]
    sets = {}
    if "domes" in wanted:
        for name in DOMES:
            js = dome_jobs(name)
            n1, n2 = len(BREATH), len(FLICKER)
            body = artkit.quantize_set(got(js[:n1 + n2]), 48)
            sets[name] = body[:n1]
            sets[f"{name}-flicker"] = body[n1:]
            sets[f"{name}-broken"] = artkit.quantize_set(got(js[n1 + n2:n1 + n2 + BROKEN]), 48)
            sets[f"{name}-flash"] = got([("dome-flash", name)])
            sets[f"{name}-dust"] = artkit.quantize_set(got([("dome-dust", name, k) for k in range(DUST)]), 16)
            for hits in (1, 2, 3):
                sets[f"{name}-crack-{hits}"] = artkit.quantize_set([crack_overlay(name, hits)], 12)
        sets["dome-pip"] = [dome_pip()]
    if "herald" in wanted:
        sets["choir-herald"] = artkit.quantize_set(got([("herald", k) for k in range(BEATS)]), 36)
        sets["choir-herald-crest"] = got([("crest", k) for k in range(len(CREST))])
        sets["choir-herald-ring"] = [ring_frame(k) for k in range(RING_SEGMENTS + 1)]
        sets["choir-herald-ring-flash"] = [ring_flash(k) for k in range(3)]
        sets["choir-herald-aura"] = [aura_circle()]
        sets["choir-herald-thread"] = [thread_frame(k) for k in range(4)]
    if "shellback" in wanted:
        walk = [("walk", k, j) for k in range(HEADINGS) for j in range(WALK)]
        curl = [("curl", k, j) for k in range(HEADINGS) for j in range(len(CURL))]
        body = artkit.quantize_set(got(walk) + got(curl), 40)
        sets["shellback"] = body[:len(walk)]
        sets["shellback-curl"] = body[len(walk):]
        sets["shellback-roll"] = artkit.quantize_set(got([("roll", k, j) for k in range(HEADINGS) for j in range(ROLL)]), 32)
        sets["shellback-belly"] = got([("belly", k, 0) for k in range(HEADINGS)])
        sets["shellback-husk"] = artkit.quantize_set(got([("husk", k, 0) for k in range(HEADINGS)]), 28)
        sets["shellback-vent"] = [vent_glow(k) for k in range(3)]
        sets["shellback-vent-dead"] = artkit.quantize_set([vent_dead()], 16)
    if "props" in wanted:
        sets["greenhouse-frame"] = artkit.quantize_set(got([("frame", s) for s in range(4)]), 32)
        sets["seed-capsule"] = artkit.quantize_set([capsule()], 24)
        sets["seed-capsule-glow"] = [capsule_glow(k) for k in range(6)]
    held = []
    singles = {"dome-pip", "choir-herald-aura", "shellback-vent-dead", "seed-capsule"} | {
        f"{n}-flash" for n in DOMES} | {f"{n}-crack-{h}" for n in DOMES for h in (1, 2, 3)}
    for name, frames in sets.items():
        where = write(name, frames, single=name in singles)
        if where == HELD:
            held.append(name)
    if "shellback" in wanted:
        artkit.write_pivots("shellback", SOURCE, {"frame": [SHELL, SHELL], "vent": vent_pivots(),
                                                  "ball": [[SHELL / 2, SHELL / 2]] * HEADINGS})
    print("held (no data claims them yet): " + (", ".join(held) if held else "none"))


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--review" not in sys.argv[1:]:
        build(args or None)
    if "--build-only" not in sys.argv[1:]:
        import siege_review
        siege_review.review(args or None)
