#!/usr/bin/env python3
"""Production art: the Burrower, Level 15's Vrell sand-swimmer that surfaces ahead of the ship after
a bulge in the sand gives it away (design/enemies/ground/burrower; M7 part B batch, concept round 36,
U.1 = a "sand maw", slate/lime). Straight to production from its chosen concept: the model
``burrower_a`` of tools/concept/enemies_r36.py (imported unchanged), cut at the sand line by the model
itself (``rise``), at the concept's scale (68 px there, 64 px here at the same px per model unit
squeezed by 64/68 so the spikes fit the stat block's 64x64).

User decision Q1 = a (M7 part B, 2026-10-14): the Burrower is **screen-fixed** while it travels, bulges
and is surfaced; the ground scrolls down under it. So while it is up the sand piles against its top
side and spills round it (the bow wave), and its ripple, bulge and bow are drawn as decals that stay on
the screen with the unit; only the hole it leaves after a dive and the crater of a kill scroll with the
ground. Every sand mark is a relief decal: a height field shaded by the fixed top-left key light (a
pale flank toward the light, a dark one away from it), disturbed fresh sand and grit, with stepped
translucency (4 levels), so it sits on any Martian ground (the basalt regolith of the Olympus slopes,
the rust dunes of Tharsis).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the slug; every sprite is
centred on the unit's position unless noted):
  burrower_0..3          64x64, surfaced body (`radial`, one heading; NOT a spin set): the maw
                         0 shut, 1 half open, 2 open, 3 open firing (the lime throat and pores
                         brightest: the ring's moment); no shadow (it sits in the sand)
  burrower-glow_0..3     64x64, additive: the lime bloom of the throat and the eight pores, matching
                         burrower_0..3 (drawn over the body)
  burrower-bow_0..7      96x96, the bow-wave loop under the body while it is up (15 fps, 4 steps a
                         frame): sand piling against its top side with dune ripples running into it,
                         spilling round both sides as streams of grit, closing in a short wake trough
                         below it, and the dark contact ring round the body
  burrower-ripple_0..63  48x48, the submerged ripple: 16 headings x 4 frames, indexed heading * 4 +
                         frame (12 fps, 5 steps a frame); heading k travels k x 22.5 degrees
                         clockwise from straight down (4 left, 8 up, 12 right) as tools/art/README.md.
                         A low mound at the head with grit kicked off it, a darker furrow with lit
                         lips trailing 30 px behind and fading; no shadow. The head (the unit's
                         position) is RIPPLE_HEAD = 12 px ahead of the sprite's centre along the
                         heading. The heading is the ground-relative travel (the screen velocity
                         minus the scroll's), since the furrow is cut into the moving ground
  burrower-bulge_0..9    72x72, the 1.0 s tell (10 fps, 6 steps a frame): a mound of sand growing,
                         the top side piling a little (the ground flows down past it), seven radial
                         cracks from frame 4 with the lime glow leaking through them, grit trickling
  burrower-breach_0..5   96x96, the break-out over the surfaced window's first 0.3 s (20 fps, 3
                         steps a frame), body included: the maw rising shut through the sand, the
                         crater rim thrown up round it, a ring of sand clods and a dust ring flung
                         out; then burrower_<maw> takes over
  burrower-dive_0..7     96x96, the 0.4 s dive (20 fps, 3 steps a frame), body included: the maw
                         sinking shut, sand slumping over its edge and pouring in, a little dust;
                         the layer flips to `buried` at frame 4 (the middle); the last frame is the
                         open hole (burrower-hole_0's look)
  burrower-hole_0..7     64x64, the hole a dive leaves, on the ground (scrolls with it): filling in
                         over 1.6 s (5 fps, 12 steps a frame), sand sliding in from the rim; after
                         the last (a faint dimple) it is gone
  burrower-crater        80x80, single: the collapsed crater a kill leaves for the rest of the level
                         (a ground decal that scrolls with the ground; placed when the unit dies,
                         under the death's sets): a slumped pit with chitin bits, a lit rim, ejecta
  burrower-death_0..13   96x96, additive: the `medium` organic burst's lime flash, lime droplets and
                         a haze (4 steps a frame, with explosion-medium)
  burrower-tatters_0..13 96x96, solid: the sand spout (a column of sand toward the camera that
                         collapses into a falling curtain and clods) with the dome shards, bone
                         spikes and the throat flung out and buried by the falling sand (4 steps
                         a frame)
  design/enemies/ground/concept/burrower-final-r38-a.png/.gif   review sheet and a play-field GIF
                         (480x540 at 1x, 10 fps, over the L15 ground stand-in scrolling at 150 px/s)

How the game draws it (step B4, BurrowerLooks; ground layer, no shadow anywhere):
  travel     burrower-ripple_<heading * 4 + frame>, centred RIPPLE_HEAD px behind the unit
  bulge      burrower-bulge_<age / 0.1 s> at the spot (with the rumble)
  surfaced   burrower-breach_<age / 0.05 s> for 0.3 s; then burrower-bow_<loop> under
             burrower_<maw> + burrower-glow_<maw> (additive); maw timeline: 0.30 s half, 0.35 s open,
             0.40-0.50 s firing (the ring), open, 1.20 s half, 1.30 s shut (hard: open again
             1.30-1.40 s, firing 1.40-1.50 s, half 1.70 s); the bow also under breach frames 2-5
  dive       burrower-dive_<age / 0.05 s>; burrower-bow under frames 0-3; at its end a
             burrower-hole on the ground at the spot (it scrolls), played once
  killed     burrower-crater on the ground at the spot (it scrolls, kept for the level), with
             explosion-medium + burrower-death (additive) + burrower-tatters (solid) over it

Run: python3 tools/art/burrower.py [--review]   (~2 min; --review only rebuilds the review files)
"""
import contextlib
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
from PIL import Image, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import driftjelly  # noqa: E402  (tools/art: the 4x Field with stepped translucency)
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged)
import vrell_deaths as vd  # noqa: E402  (tools/art: the death pieces' pose and the motes)
import vrell_fx as fx  # noqa: E402  (tools/art: easing, timing)
import enemies_r36 as u36  # noqa: E402  (concept round 36: the chosen burrower_a, imported unchanged)
from render import enemy_models as em  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_GLOW, V_SEAM  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import Material, rotate_z, sd_capsule, sd_plate, sd_sphere  # noqa: E402

# =========================================================================== parameters

SCRIPT = "burrower.py"
BATCH = "M7 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r38"
SLUG = "burrower"
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
HELD = ROOT / "build" / "art-held" / "sprites"
STEP = 60                                       # game steps per second

SIZE = (64, 64)
EXTENT = 2.3 * 64 / 68                          # the concept's 68 px scale, squeezed into 64 px
PX = SIZE[0] / EXTENT                           # px per model unit (29.6)
ROT = model_rotation(np.pi / 2)                 # the concept's facing (DOWN): the pores' pattern
MAW = [(0.0, 0.8), (0.5, 1.0), (1.0, 1.0), (1.0, 1.35)]   # (open, glow): shut, half, open, firing
GLOW_GAIN = [0.2, 0.32, 0.45, 0.9]
BODY_COLOURS = 48

BOW_SIZE, BOW_FRAMES = (96, 96), 8
RIPPLE_SIZE, RIPPLE_FRAMES, HEADINGS, RIPPLE_HEAD, RIPPLE_TRAIL = (48, 48), 4, 16, 12.0, 30.0
BULGE_SIZE, BULGE_FRAMES = (72, 72), 10
FX_SIZE, BREACH_FRAMES, DIVE_FRAMES = (96, 96), 6, 8
HOLE_SIZE, HOLE_FRAMES = (64, 64), 8
CRATER_SIZE = (80, 80)
DEATH_SIZE, DEATH_FRAMES = (96, 96), 14
DEATH_EXTENT = EXTENT * DEATH_SIZE[0] / SIZE[0]
STEPS = {"bow": 4, "ripple": 5, "bulge": 6, "breach": 3, "dive": 3, "hole": 12, "death": 4}

LIME = np.array(em.hx(em.GLOWS["lime"])) * 255
WHITE_LIME = (236, 255, 210)
SAND_LIT = np.array([240, 192, 140], float)     # the flank toward the key light
SAND_FRESH = np.array([214, 150, 96], float)    # disturbed sand, lighter than the regolith
SAND_DARK = np.array([58, 24, 14], float)       # the flank away from it, furrows
PIT = np.array([30, 12, 8], float)
GRIT_LIGHT = np.array([244, 204, 156], float)
GRIT_DARK = np.array([78, 36, 20], float)


# =========================================================================== held sprites

def claimed(slug):
    """Whether the game data claims an enemy's sprite root (vanguard.pipeline.SpriteUse: an enemy's
    sprites are claimed once ``design/enemies/<category>/<slug>/data.yaml`` exists, by the content
    loader's enemy pattern)."""
    return any((DESIGN / "enemies").glob(f"*/{slug}/data.yaml"))


@contextlib.contextmanager
def sprites_for(slug):
    """artkit's sprite folder for ``slug``'s frame sets: assets/sprites once the data claims it,
    build/art-held/sprites until then (the atlas packer fails on unclaimed sprites)."""
    target = artkit.SPRITES if claimed(slug) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def where(slug):
    return "assets/sprites" if claimed(slug) else "build/art-held/sprites (not claimed by the data yet)"


# =========================================================================== the body

def model(rise=1.0, open_=0.0, glow=1.0):
    """(scene, mats): the concept's burrower_a, facing as the concept drew it."""
    scene0, mats = u36.burrower_a(rise, open_, glow)
    return (lambda p: scene0(rotate_z(p, ROT))), model_space_materials(mats, ROT)


def render_body(rise, open_, glow):
    return artkit.native(*artkit.render_hi(*model(rise, open_, glow), SIZE, EXTENT))


def render_glow(i):
    """The lime bloom of maw frame i: the emissive parts alone, blurred into a halo, stepped."""
    open_, glow = MAW[i]
    scene, mats = model(1.0, open_, glow)
    black = Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0)
    em_mats = [Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0, emission=m.emission,
                        emission_pattern=m.emission_pattern) if max(m.emission) > 0 else black for m in mats]
    hi, factor = artkit.render_hi(scene, em_mats, SIZE, EXTENT, shadows=False)
    small = sprite.downsample(hi, factor)
    lum = (small[..., :3] * small[..., 3:4]).max(axis=-1)
    img = Image.fromarray((np.clip(lum, 0, 1) * 255).astype(np.uint8))
    halo = np.asarray(img.filter(ImageFilter.GaussianBlur(2.6)), float) / 255
    core = np.asarray(img.filter(ImageFilter.GaussianBlur(0.8)), float) / 255
    gain = GLOW_GAIN[i]
    k = np.clip(halo * 1.3 - core * 0.7, 0, 1) * gain               # round the throat, not on it
    rgb = LIME[None, None, :] * k[..., None]
    if i == 3:                                                  # firing: a white-lime flash
        rgb = rgb + np.array(WHITE_LIME) * np.clip(core - 0.5, 0, 1)[..., None] * 0.35
    q = np.floor(np.clip(rgb / 255, 0, 1) * 6 + 0.5) / 6 * 255
    a = np.where(q.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([q, a]).astype(np.uint8), "RGBA")


def intel_model():
    """(scene, mats) for the hangar intel portrait: surfaced, the maw open."""
    return model(1.0, 1.0, 1.0)


# =========================================================================== relief decals

class Relief(driftjelly.Field):
    """A 4x field for sand marks: a height field shaded by the top-left key light, fresh sand,
    grit, glow; composited to native size with stepped translucency."""

    def image(self, levels=4):
        """Native size; the translucency stepped to ``levels`` with 4x4 ordered dithering (the art
        direction allows it on translucency steps), so a soft relief has no hard contour."""
        S = self.SS
        a = self.a.reshape(self.h, S, self.w, S).mean(axis=(1, 3))
        prem = (self.rgb * self.a[..., None]).reshape(self.h, S, self.w, S, 3).mean(axis=(1, 3))
        rgb = prem / np.maximum(a, 1e-6)[..., None]
        q = artkit.ordered_dither(np.where(a < 0.16, 0.0, a), levels)   # no speckled fringe
        out = np.dstack([np.clip(rgb, 0, 255), q * 255])
        out[q <= 0] = 0
        return Image.fromarray(out.astype(np.uint8), "RGBA")

    def centred(self):
        return self.x - self.w / 2, self.y - self.h / 2

    def shade(self, hgt, k=1.6, lit=0.85, dark=0.8):
        gy, gx = np.gradient(hgt, 1.0 / self.SS)
        slope = np.clip((gx + gy) * 0.7071 * k, -1, 1)          # facing up-left is lit
        self.over(SAND_LIT, np.clip(slope, 0, 1) * lit)
        self.over(SAND_DARK, np.clip(-slope, 0, 1) * dark)

    def dot(self, x, y, r, colour, cover=1.0):
        d = np.hypot(self.x - x, self.y - y)
        self.over(colour, (d <= r) * cover)


def gauss2(dx, dy, rx, ry):
    return np.exp(-(dx * dx) / (rx * rx) - (dy * dy) / (ry * ry))


def crack_paths(seed, n=7):
    """Seven radial crack polylines as the concept's sand_bump: per crack its angle wobble along
    the way out (9 points, u = 0..1) and its reach (0.7..1)."""
    rng = np.random.default_rng(seed)
    out = []
    for k in range(n):
        a = k * TAU / n + rng.uniform(-0.3, 0.3)
        reach = rng.uniform(0.7, 1.0)
        out.append(([(u, a + 0.18 * np.sin(u * 7 + k)) for u in np.linspace(0, 1, 9)], reach))
    return out


def seg_dist(f, x0, y0, x1, y1):
    px, py = f.x - x0, f.y - y0
    bx, by = x1 - x0, y1 - y0
    h = np.clip((px * bx + py * by) / max(bx * bx + by * by, 1e-9), 0, 1)
    return np.hypot(px - bx * h, py - by * h)


def bulge_frame(i):
    """The tell, frame i of 10 over 1.0 s: the mound grows, piles a little on its top side, cracks
    split it from frame 4 and the lime glow leaks through them; grit trickles off and down."""
    w, h = BULGE_SIZE
    s = (i + 0.5) / BULGE_FRAMES
    f = Relief(w, h)
    dx, dy = f.centred()
    amount = 0.6 + 0.6 * s
    R = 12.0 + 8.0 * s
    r = np.hypot(dx, dy) + 1e-6
    hgt = amount * R * 0.5 * gauss2(dx, dy, R, R)
    hgt += amount * 0.1 * R * np.exp(-((r - R * 0.8) / (R * 0.35)) ** 2) * np.clip(-dy / r, 0, 1) ** 1.5
    f.over(SAND_FRESH, np.clip(amount * gauss2(dx, dy, R * 0.75, R * 0.75) * 0.75, 0, 0.85))
    f.shade(hgt, k=1.6, lit=0.9, dark=0.7)
    cracks = max(0.0, s - 0.35) / 0.65
    if cracks > 0:
        glow = np.zeros_like(f.x)
        core = np.zeros_like(f.x)
        for pts, reach in crack_paths(1501):
            r0, r1 = 0.12 * R, R * (0.3 + 0.6 * cracks) * reach
            xy = [(w / 2 + np.cos(a) * (r0 + (r1 - r0) * u), h / 2 + np.sin(a) * (r0 + (r1 - r0) * u)) for u, a in pts]
            for (x0, y0), (x1, y1) in zip(xy, xy[1:]):
                d = seg_dist(f, x0, y0, x1, y1)
                core = np.maximum(core, (d <= 0.8).astype(float))
                glow = np.maximum(glow, np.exp(-(d / 1.8) ** 2))
        f.over(PIT, core)
        f.over(LIME * 0.9, glow * 0.75 * cracks * (1 - core))
        f.over(LIME, core * np.clip(cracks * 1.3 - 0.15, 0, 1))
        crown = gauss2(dx, dy, 3 + 2 * cracks, 3 + 2 * cracks) * cracks ** 2
        f.over(np.array(WHITE_LIME, float), crown * 0.9)
    rng = np.random.default_rng(1502)
    for k in range(int(6 + 14 * amount)):                      # grit trickling off, carried down
        a = rng.uniform(0, TAU)
        u = (s * 1.6 + rng.uniform(0, 1)) % 1.0
        rr = R * (0.35 + 0.9 * u)
        x = w / 2 + np.cos(a) * rr
        y = h / 2 + np.sin(a) * rr + 6 * u * u
        f.dot(x, y, 0.6, GRIT_LIGHT if k % 3 else GRIT_DARK, 1 - u * 0.6)
    return f.image()


def bow_frame(i):
    """The bow-wave loop, frame i of 8: the ground scrolls down under the screen-fixed body, so the
    sand piles against its top side (dune ripples running into it), spills round both sides as
    streams of grit and closes in a short wake trough below; a dark contact ring round the body."""
    w, h = BOW_SIZE
    ph = TAU * i / BOW_FRAMES
    f = Relief(w, h)
    dx, dy = f.centred()
    r = np.hypot(dx, dy) + 1e-6
    up = np.clip(-dy / r, 0, 1)                                  # 1 on the top side
    side = np.clip(np.abs(dx) / r, 0, 1)
    ridge = 3.4 * np.exp(-((r - 24.5) / 4.2) ** 2) * up ** 0.7
    apron = 1.6 * np.exp(-((r - 31) / 8.5) ** 2) * up ** 1.2
    ripples = 0.45 * np.sin(dy * TAU / 6.5 - ph * 1.0 + 0.6 * np.sin(dx / 5.0)) * np.exp(-((r - 34) / 9) ** 2) * up
    arms = 1.5 * np.exp(-((np.abs(dx) - 26.5) / 3.6) ** 2) * np.exp(-((dy - 4) / 16) ** 2) * side
    wake = -1.3 * np.exp(-(dx / 5.5) ** 2) * np.clip((dy - 20) / 6, 0, 1) * np.exp(-((dy - 30) / 12) ** 2)
    lips = 0.7 * np.exp(-((np.abs(dx) - 8.5) / 3.0) ** 2) * np.clip((dy - 18) / 6, 0, 1) * np.exp(-((dy - 30) / 12) ** 2)
    hgt = ridge + apron + ripples + arms + wake + lips
    outer = np.clip((40 - r) / 6, 0, 1)
    hgt *= outer
    f.over(SAND_FRESH, np.clip((ridge * 0.3 + apron * 0.3 + arms * 0.35 + lips * 0.4) * outer, 0, 0.85))
    f.shade(hgt, k=1.5, lit=0.9, dark=0.7)
    # contact ring: the gap round the body, darker toward the bottom right (shadow side)
    ring = np.exp(-((r - 21.0) / 1.8) ** 2)
    sh = 0.55 + 0.35 * np.clip((dx + dy) / (r * 1.414), 0, 1)
    f.over(PIT, ring * sh)
    rng = np.random.default_rng(1511)
    a_r = 27.0
    for k in range(34):                                          # grit streaming round the body
        x0 = rng.choice([-1, 1]) * rng.uniform(3, 34)
        u = (i / BOW_FRAMES + rng.uniform(0, 1)) % 1.0
        y = -44 + 88 * u
        x = np.sign(x0) * np.sqrt(x0 * x0 + max(0.0, a_r * a_r - y * y) * (1.0 if y < 6 else 0.6))
        fade = np.clip(np.sin(np.pi * u) * 1.6, 0, 1)
        if np.hypot(x, y) < 22:
            continue
        f.dot(w / 2 + x, h / 2 + y, 0.65, GRIT_LIGHT if k % 3 else GRIT_DARK, fade)
    return f.image()


def ripple_frame(k, j):
    """The submerged ripple at heading k, frame j of 4: a mound at the head, grit kicked off it, a
    darker furrow with lit lips trailing behind and fading. Shaded in screen space per heading."""
    w, h = RIPPLE_SIZE
    a = np.pi / 2 + TAU * k / HEADINGS                           # screen angle of travel (y down)
    ux, uy = np.cos(a), np.sin(a)
    f = Relief(w, h)
    dx, dy = f.centred()
    u = dx * ux + dy * uy - RIPPLE_HEAD                          # 0 at the head, negative behind
    v = -dx * uy + dy * ux
    fade = np.clip((u + RIPPLE_TRAIL) / RIPPLE_TRAIL, 0, 1) ** 1.3 * (u < -1.0) + (u >= -1.0) * 1.0
    mound = 2.6 * gauss2(u + 0.5, v, 4.2, 5.4)
    behind = np.clip(-u / 3.0, 0, 1)
    vc = 2.9 + 0.12 * np.clip(-u, 0, None)                        # the wake's two ridges diverge
    ridges = 0.9 * np.exp(-((np.abs(v) - vc) / 1.5) ** 2) * fade * behind
    trench = -1.2 * np.exp(-(v / 1.8) ** 2) * fade * behind
    flow = 0.25 * np.sin(u * 1.3 + TAU * j / RIPPLE_FRAMES) * np.exp(-(v / 3.5) ** 2) * fade * behind
    hgt = mound + ridges + trench + flow
    f.over(SAND_FRESH, np.clip(0.7 * gauss2(u, v, 4.4, 5.0) + 0.5 * ridges, 0, 0.85))
    f.shade(hgt, k=1.6, lit=0.9, dark=0.75)
    f.over(SAND_DARK, np.clip(0.5 * np.exp(-(v / 1.5) ** 2) * fade * behind, 0, 1))
    rng = np.random.default_rng(1521)
    cx, cy = w / 2 + ux * RIPPLE_HEAD, h / 2 + uy * RIPPLE_HEAD
    for m in range(9):                                           # grit kicked up at the head
        side = rng.choice([-1, 1])
        ph = (j / RIPPLE_FRAMES + m / 9) % 1.0
        back = -1.5 - 9 * ph
        out = side * (2.0 + rng.uniform(2, 6) * ph)
        x = cx + ux * back - uy * out
        y = cy + uy * back + ux * out
        f.dot(x, y, 0.8, GRIT_LIGHT if m % 3 else GRIT_DARK, 1 - ph * 0.6)
    for m in range(5):                                           # specks along the furrow
        ph = (j / RIPPLE_FRAMES + m / 5) % 1.0
        back = -4 - (RIPPLE_TRAIL - 8) * ph
        out = rng.uniform(-3.5, 3.5)
        f.dot(cx + ux * back - uy * out, cy + uy * back + ux * out, 0.55, GRIT_LIGHT, 0.8 * (1 - ph))
    return f.image()


def hole_relief(f, depth, R, seed, rim=1.0):
    """A pit of radius R and ``depth`` with a thrown-up rim, sand runnels sliding in."""
    dx, dy = f.centred()
    r = np.hypot(dx, dy) + 1e-6
    th = np.arctan2(dy, dx)
    wob = 1 + 0.08 * np.sin(5 * th + seed) + 0.05 * np.sin(9 * th + 2 * seed)
    rr = r / (R * wob)
    pit = -depth * R * 0.55 * np.clip(1 - rr ** 2, 0, 1) ** 1.2
    rim_h = rim * 1.6 * np.exp(-((rr - 1.12) / 0.2) ** 2)
    runnels = 0.25 * depth * np.sin(th * 11 + seed) * np.exp(-((rr - 0.8) / 0.25) ** 2)
    return pit + rim_h + runnels, rr


def hole_frame(i):
    """The hole a dive leaves, filling in: frame 0 open and dark, the last a faint dimple."""
    w, h = HOLE_SIZE
    s = i / (HOLE_FRAMES - 1)
    f = Relief(w, h)
    depth = (1 - s) ** 1.2
    R = 15.0 - 4.0 * s
    hgt, rr = hole_relief(f, depth, R, 1.3, rim=1.0 - 0.75 * s)
    fade = 1 - 0.55 * s
    f.over(SAND_FRESH, np.clip(np.exp(-((rr - 0.9) / 0.5) ** 2) * 0.55 * fade, 0, 1))
    f.shade(hgt * fade, k=0.7, lit=0.75 * fade, dark=0.6 * fade)
    f.over(PIT, np.clip((0.7 - rr) / 0.35, 0, 1) * depth * 0.85)
    rng = np.random.default_rng(1531)
    for m in range(int(14 * (1 - s)) + 3):                       # sand sliding in from the rim
        a = rng.uniform(0, TAU)
        u = (s * 2.5 + rng.uniform(0, 1)) % 1.0
        rad = R * (1.15 - 0.8 * u)
        f.dot(w / 2 + np.cos(a) * rad, h / 2 + np.sin(a) * rad, 0.6, GRIT_LIGHT if m % 3 else GRIT_DARK,
              (1 - u) * fade)
    return f.image()


def crater_frame():
    """The collapsed crater of a kill (kept for the rest of the level): a slumped pit with terraces,
    dark chitin bits and a broken spike on its floor, a lit rim and ejecta rays round it."""
    w, h = CRATER_SIZE
    f = Relief(w, h)
    dx, dy = f.centred()
    r = np.hypot(dx, dy) + 1e-6
    th = np.arctan2(dy, dx)
    n = raster.fbm(w * f.SS, h * f.SS, 10 * f.SS, 1541, octaves=3, period=False)
    R = 22.0 * (1 + 0.07 * np.sin(4 * th + 0.7) + 0.05 * np.sin(7 * th + 2.1)) + (n - 0.5) * 3
    rr = r / R
    pit = -9.0 * np.clip(1 - rr ** 2, 0, 1) ** 1.1
    terrace = 0.9 * np.sin(rr * 9.0 + 2.0 * n) * np.clip(1 - rr, 0, 1) * (rr > 0.25)
    rim = 3.2 * np.exp(-((rr - 1.1) / 0.17) ** 2)
    rays = 0.9 * np.clip(np.sin(th * 9 + 3 * n), 0, 1) ** 3 * np.exp(-((rr - 1.45) / 0.35) ** 2)
    hgt = pit + terrace + rim + rays + (n - 0.5) * 1.2 * (rr < 1.8)
    edge = np.clip((1.85 - rr) / 0.35, 0, 1)
    f.over(SAND_FRESH, np.clip(np.exp(-((rr - 1.2) / 0.38) ** 2) * 0.6 + rays * 0.25, 0, 0.75) * edge)
    f.over(PIT, np.clip((0.95 - rr) / 0.35, 0, 1) * 0.85)
    f.shade(hgt * edge, k=0.9, lit=0.85, dark=0.85)
    rng = np.random.default_rng(1542)
    for m in range(7):                                           # chitin bits on the floor
        a, rad = rng.uniform(0, TAU), rng.uniform(2, 13)
        x, y = w / 2 + np.cos(a) * rad, h / 2 + np.sin(a) * rad
        f.over(np.array([46, 50, 58], float), (gauss2(f.x - x, f.y - y, 1.8, 1.2) > 0.5).astype(float))
        f.over(np.array([118, 126, 138], float), (gauss2(f.x - x + 0.6, f.y - y + 0.6, 0.9, 0.6) > 0.5).astype(float))
    x0, y0 = w / 2 - 6, h / 2 + 3                                # a broken bone spike
    d = seg_dist(f, x0, y0, x0 + 7, y0 - 4)
    f.over(np.array([196, 186, 160], float), (d <= 1.0).astype(float))
    f.over(np.array([96, 88, 74], float), ((d <= 1.0) & (f.y > (y0 - (f.x - x0) * 4 / 7) + 0.3)).astype(float))
    for m in range(16):                                          # ejecta clods
        a, rad = rng.uniform(0, TAU), rng.uniform(28, 36)
        f.dot(w / 2 + np.cos(a) * rad, h / 2 + np.sin(a) * rad, rng.uniform(0.6, 1.1),
              GRIT_LIGHT if m % 3 else GRIT_DARK, 0.9)
    return f.image()


# =========================================================================== breach / dive

def clods(f, s, seed, count, reach, cx, cy, rise=8.0, size=(0.6, 1.4)):
    """Sand clods flung out (0 <= s <= 1): lit top-left, thrown up (drawn a little higher and
    larger mid-flight) and falling back."""
    rng = np.random.default_rng(seed)
    for k in range(count):
        a = rng.uniform(0, TAU)
        dist = reach * rng.uniform(0.55, 1.0) * fx.ease_out(s, 1.6)
        hop = np.sin(np.pi * min(1.0, s * rng.uniform(0.9, 1.2))) * rise * rng.uniform(0.5, 1.0)
        x, y = cx + np.cos(a) * dist, cy + np.sin(a) * dist * 0.92 - hop * 0.35
        sz = rng.uniform(*size) * (1 + 0.25 * hop / max(rise, 1e-6))
        cover = np.clip(1.3 - s, 0, 1)
        f.dot(x, y, sz, GRIT_DARK, cover)
        f.dot(x - sz * 0.3, y - sz * 0.3, sz * 0.6, GRIT_LIGHT, cover)


def dust_ring(f, cx, cy, r, width, alpha, seed):
    dx, dy = f.x - cx, f.y - cy
    rad = np.hypot(dx, dy)
    th = np.arctan2(dy, dx)
    brk = 0.6 + 0.4 * np.sin(5 * th + seed) * np.sin(3 * th - seed)
    band = np.exp(-((rad - r) / width) ** 2) * brk * alpha
    lit = np.clip(-(dx + dy) / np.maximum(rad, 1e-6) * 0.7071, -1, 1)
    f.over(SAND_FRESH * 0.85 + SAND_LIT * 0.15 * np.clip(lit, 0, 1)[..., None], band)


def paste_into(base, img, cx, cy):
    base.alpha_composite(img, (int(round(cx - img.width / 2)), int(round(cy - img.height / 2))))


def breach_frame(i, body):
    """Break-out frame i of 6 over 0.3 s: the maw rising shut through the sand (``body``: its
    render at this rise), the crater rim thrown up round it, clods and a dust ring flung out."""
    w, h = FX_SIZE
    s = (i + 1) / BREACH_FRAMES
    under = Relief(w, h)
    hgt, rr = hole_relief(under, 0.5 + 0.4 * s, 17 + 4 * s, 0.4, rim=0.6 + 0.9 * s)
    under.over(SAND_FRESH, np.clip(np.exp(-((rr - 1.1) / 0.35) ** 2) * 0.7, 0, 1))
    under.shade(hgt, k=0.8, lit=0.8, dark=0.65)
    under.over(PIT, np.clip((1.0 - rr) / 0.25, 0, 1) * 0.8)
    img = under.image()
    if body is not None:
        paste_into(img, body, w / 2, h / 2)
    over = Relief(w, h)
    dust_ring(over, w / 2, h / 2, 14 + 26 * fx.ease_out(s), 3.5 + 3 * s, 0.75 * (1 - s) ** 0.8, 1.1)
    if s < 0.5:                                                  # sand sheeting off the rising dome
        dx, dy = over.centred()
        r = np.hypot(dx, dy)
        over.over(SAND_FRESH, np.clip((19 - r) / 4, 0, 1) * (1 - s / 0.5) * 0.9)
    clods(over, s, 1551, 26, 40, w / 2, h / 2)
    img.alpha_composite(over.image())
    return img


def dive_frame(i, body):
    """Dive frame i of 8 over 0.4 s: the maw sinking shut (``body``: its render at this rise), the
    sand slumping in over its edge as a lip that closes to the open hole of burrower-hole_0 (the last
    frame is that hole), grit pouring in, a little dust."""
    w, h = FX_SIZE
    s = (i + 1) / DIVE_FRAMES
    R = 21.0 - 6.0 * s                                           # 15 at the end: burrower-hole_0's
    under = Relief(w, h)
    hgt, rr = hole_relief(under, 1.0, R, 1.3, rim=1.0 + 0.4 * (1 - s))
    under.over(SAND_FRESH, np.clip(np.exp(-((rr - 0.9) / 0.5) ** 2) * 0.55, 0, 1))
    under.shade(hgt, k=0.7, lit=0.75, dark=0.6)
    under.over(PIT, np.clip((0.7 - rr) / 0.35, 0, 1) * 0.85)
    img = under.image()
    if body is not None:
        paste_into(img, body, w / 2, h / 2)
    over = Relief(w, h)
    dx, dy = over.centred()
    r = np.hypot(dx, dy)
    th = np.arctan2(dy, dx)
    lobe = R - 3.0 - 1.2 * np.sin(6 * th + 1.0) - 0.8 * np.sin(11 * th)
    lip = np.clip((r - lobe) / 1.2, 0, 1) * np.clip((R + 1.5 - r) / 1.5, 0, 1) * (body is not None)
    sl = np.clip(-(dx + dy) / np.maximum(r, 1e-6) * 0.7071, -1, 1)
    over.over(SAND_FRESH, lip * 0.95)
    over.over(SAND_LIT, lip * np.clip(-sl, 0, 1) * 0.5)          # the lip's inner face toward the light
    over.over(SAND_DARK, lip * np.clip(sl, 0, 1) * 0.45)
    dust_ring(over, w / 2, h / 2, R + 4 + 6 * s, 3 + 2 * s, 0.5 * np.sin(np.pi * s), 2.2)
    rng = np.random.default_rng(1561)
    for m in range(22):                                          # grit pouring in
        a = rng.uniform(0, TAU)
        u = (s * 1.8 + rng.uniform(0, 1)) % 1.0
        rad = R + 6 - (R - 2) * u
        over.dot(w / 2 + np.cos(a) * rad, h / 2 + np.sin(a) * rad, 0.65, GRIT_LIGHT if m % 3 else GRIT_DARK,
                 (1 - u) * 0.9 * (1 - 0.5 * s))
    img.alpha_composite(over.image())
    return img


def rise_of(kind, i):
    if kind == "breach":
        return min(1.0, 0.2 + 0.8 * (i + 1) / BREACH_FRAMES)
    return max(0.0, 1.0 - (i + 1) / DIVE_FRAMES * 1.12)


# =========================================================================== death

def death_glow(i):
    """The `medium` organic burst: a white-lime flash from the maw, a lime bloom, lime droplets
    flung out and falling short, a thin haze."""
    w, h = DEATH_SIZE
    t = fx.t_of(i, DEATH_FRAMES)
    c = w / 2
    cv = v8.Canvas(w, h)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 5.0, 0, 1)
    cv.add(WHITE_LIME, v8.gauss(d, 4 + 7 * t) * 1.5 * np.clip(1 - t * 2.6, 0, 1))
    cv.add(tuple(LIME), v8.gauss(d, 10 + 14 * fx.ease_out(t)) * 0.6 * (1 - t) ** 1.6 * fade)
    tex = v8._cart_noise(w, 1571, cell=w * v8.SS // 5)
    haze = np.clip(v8.gauss(d, 12 + 16 * fx.ease_out(t, 2.2)) * (0.5 + 0.8 * tex) - 0.25 - 0.6 * t, 0, 1)
    cv.add((60, 110, 20), haze * 0.8 * (1 - t) * fade)
    vd.motes(cv, np.random.default_rng(1572), c, t, 28, (14, 40), (0.6, 1.2), tuple(LIME), WHITE_LIME, fade,
             delay=0.06, gain=1.1)
    return artkit.additive(cv.image())


def death_pieces():
    """The dome in curved shards, the ten bone spikes, three teeth, the throat, column chunks."""
    rng = np.random.default_rng(1581)
    top = -0.5 + 0.72                                            # the model's head height at rise 1
    pieces = []
    for k in range(7):
        a = TAU * k / 7 + rng.uniform(-0.25, 0.25)
        c0 = (0.36 * np.cos(a), 0.36 * np.sin(a), top + 0.3)
        poly = fx.jagged(rng, rng.uniform(0.22, 0.3), 7)
        bend = rng.uniform(0.9, 1.6)

        def shard(q, c0=c0, poly=poly, bend=bend):
            r = q - np.array(c0)
            r[:, 2] += bend * (r[:, 0] ** 2 + r[:, 1] ** 2)
            return sd_plate(r, poly, 0.0, 0.05, 0.015)
        pieces.append(dict(geo=shard, mat=V_BODY, c0=c0, dir=vd.outward(rng, c0, 0.4), reach=rng.uniform(0.35, 0.6),
                           ease=2.4, axis=rng.uniform(0, TAU), tumble=rng.uniform(3, 6) * rng.choice([-1, 1]),
                           spin=rng.uniform(-3, 3), rise=0.6, shrink_at=0.5, least=0.3))
    for k in range(10):
        a = TAU * k / 10 + TAU / 20
        ca, sa = np.cos(a), np.sin(a)

        def spike(q, ca=ca, sa=sa):
            return sd_capsule(q, (0.52 * ca, 0.52 * sa, top - 0.06), (0.98 * ca, 0.98 * sa, top - 0.26), 0.11, 0.02)
        c0 = (0.75 * ca, 0.75 * sa, top - 0.16)
        pieces.append(dict(geo=spike, mat=V_BONE, c0=c0, dir=vd.outward(rng, c0, 0.3), reach=rng.uniform(0.2, 0.42),
                           ease=2.2, axis=rng.uniform(0, TAU), tumble=rng.uniform(2, 5) * rng.choice([-1, 1]),
                           spin=rng.uniform(-2, 2), rise=0.4, shrink_at=0.55, least=0.35))
    for k in range(3):
        a = TAU * k / 3 + 0.4
        ca, sa = np.cos(a), np.sin(a)

        def tooth(q, ca=ca, sa=sa):
            return sd_capsule(q, (0.42 * ca, 0.42 * sa, top + 0.3), (0.2 * ca, 0.2 * sa, top + 0.44), 0.1, 0.025)
        c0 = (0.3 * ca, 0.3 * sa, top + 0.37)
        pieces.append(dict(geo=tooth, mat=V_BONE, c0=c0, dir=vd.outward(rng, c0, 0.5), reach=rng.uniform(0.3, 0.55),
                           ease=2.6, axis=rng.uniform(0, TAU), tumble=rng.uniform(3, 7), spin=rng.uniform(-3, 3),
                           rise=0.8, shrink_at=0.5, least=0.3))
    for k in range(4):
        a = TAU * k / 4 + 0.8
        c0 = (0.45 * np.cos(a), 0.45 * np.sin(a), top - 0.25)

        def chunk(q, c0=c0):
            return sd_sphere(q, c0, 0.17)
        pieces.append(dict(geo=chunk, mat=V_SEAM, c0=c0, dir=vd.outward(rng, c0, 0.4), reach=rng.uniform(0.15, 0.3),
                           ease=2.0, axis=rng.uniform(0, TAU), tumble=rng.uniform(1, 3), spin=rng.uniform(-2, 2),
                           rise=0.2, shrink_at=0.45, least=0.3))
    pieces.append(dict(geo=lambda q: sd_sphere(q, (0.0, 0.0, top + 0.04), 0.24), mat=V_GLOW, c0=(0.0, 0.0, top + 0.04),
                       dir=(0.3, -0.95), reach=0.1, ease=3.0, axis=0.4, tumble=1.0, spin=1.0, rise=0.5,
                       shrink_at=0.15, least=0.05, glow=0.6))
    return pieces


@lru_cache(maxsize=None)
def spout_plan():
    """(t0, x0, y0, vx, vy, r0, r1, life, bright) per sand puff (t in units of the set, px):
    the column toward the camera first, then the curtain falling outward."""
    rng = np.random.default_rng(1591)
    plan = [(0.0, 0.0, 0.0, 0.0, 0.0, 7.0, 21.0, 0.5, 1.25),
            (0.02, -3.0, -3.0, 0.0, 0.0, 5.0, 13.0, 0.35, 1.4)]
    for j in range(12):
        a = j * TAU / 12 + rng.uniform(-0.2, 0.2)
        sp = rng.uniform(30, 44)
        plan.append((rng.uniform(0.12, 0.24), np.cos(a) * 6, np.sin(a) * 6, np.cos(a) * sp, np.sin(a) * sp * 0.92,
                     rng.uniform(4, 6), rng.uniform(8, 12), rng.uniform(0.5, 0.7), 1.0))
    return plan


def spout(i):
    """The sand spout's puffs at frame i: lit spheres of sand with a ragged edge, 4 translucency
    steps (ordered dither), over the pieces."""
    w, h = DEATH_SIZE
    S = 2
    t = fx.t_of(i, DEATH_FRAMES)
    yy, xx = np.mgrid[0:h * S, 0:w * S]
    noise = raster.fbm(w * S, h * S, 8 * S, 1592, octaves=3, period=False)
    rgb = np.zeros((h * S, w * S, 3))
    alpha = np.zeros((h * S, w * S))
    light = np.array([-0.5, -0.6, 0.62]) / np.linalg.norm([-0.5, -0.6, 0.62])
    for t0, x0, y0, vx, vy, r0, r1, life, bright in spout_plan():
        age = t - t0
        if not 0 <= age < life:
            continue
        u = age / life
        r = (r0 + (r1 - r0) * fx.ease_out(u, 2.0)) * S
        cx = w * S / 2 + (x0 + vx * age) * S
        cy = h * S / 2 + (y0 + vy * age) * S
        nx, ny = (xx - cx) / r, (yy - cy) / r
        rr = nx ** 2 + ny ** 2
        edge = 1 - rr + (noise - 0.5) * 1.3
        nz = np.sqrt(np.clip(1 - rr, 0, 1))
        lit = np.clip(nx * light[0] + ny * light[1] + nz * light[2], 0, 1)
        a = np.where(edge > 0, np.clip(edge * 2, 0, 1), 0) * min(1, age / 0.04) * (1 - u ** 1.6) * 0.95
        col = SAND_FRESH[None, None, :] * (0.45 + 0.7 * lit[..., None]) * bright
        rgb = rgb * (1 - a[..., None]) + col * a[..., None]
        alpha = alpha + a * (1 - alpha)
    q = artkit.ordered_dither(alpha, 4)
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), q * 255]).astype(np.uint8), "RGBA")
    return img.resize((w, h), Image.NEAREST)


def death_tatters(i):
    """Pieces flung out of the spout and buried by the falling sand, with the spout over them."""
    t = fx.t_of(i, DEATH_FRAMES)
    mats = u36.burrower_a(1.0, 0.5, 0.6)[1]
    scene, mats = vd.pieces_scene(death_pieces(), mats, t, lambda p: rotate_z(p, ROT))
    img = artkit.native(*artkit.render_hi(scene, mats, DEATH_SIZE, DEATH_EXTENT))
    bury = float(np.clip((t - 0.4) / 0.6, 0, 1))
    if bury > 0:                                                 # the falling sand covers them
        a = np.array(img).astype(np.float64)
        k = 0.75 * bury
        a[..., :3] = a[..., :3] * (1 - k) + SAND_FRESH * 0.8 * k
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
        if bury > 0.6:                                           # the last pieces sink into it
            al = np.array(img)
            keep = np.random.default_rng(1593 + i).random(al.shape[:2]) > (bury - 0.6) * 2.2
            al[..., 3] = np.where(keep, al[..., 3], 0)
            al[al[..., 3] == 0] = 0
            img = Image.fromarray(al, "RGBA")
    out = Image.new("RGBA", DEATH_SIZE, (0, 0, 0, 0))
    out.alpha_composite(img)
    out.alpha_composite(spout(i))
    clod = Relief(*DEATH_SIZE)
    clods(clod, t, 1594, 30, 44, DEATH_SIZE[0] / 2, DEATH_SIZE[1] / 2, rise=14, size=(0.7, 1.6))
    out.alpha_composite(clod.image())
    return out


# =========================================================================== build

def _job(job):
    kind, i = job
    if kind == "body":
        return render_body(1.0, *MAW[i])
    if kind == "glow":
        return render_glow(i)
    if kind in ("breach-body", "dive-body"):
        rise = rise_of(kind.split("-")[0], i)
        return render_body(rise, 0.0, 0.7) if rise > 0.05 else None
    if kind == "bow":
        return bow_frame(i)
    if kind == "ripple":
        return ripple_frame(i // RIPPLE_FRAMES, i % RIPPLE_FRAMES)
    if kind == "bulge":
        return bulge_frame(i)
    if kind == "hole":
        return hole_frame(i)
    if kind == "crater":
        return crater_frame()
    if kind == "death":
        return death_glow(i)
    return death_tatters(i)


def build():
    jobs = ([("body", i) for i in range(4)] + [("glow", i) for i in range(4)]
            + [("breach-body", i) for i in range(BREACH_FRAMES)] + [("dive-body", i) for i in range(DIVE_FRAMES)]
            + [("bow", i) for i in range(BOW_FRAMES)] + [("ripple", i) for i in range(HEADINGS * RIPPLE_FRAMES)]
            + [("bulge", i) for i in range(BULGE_FRAMES)] + [("hole", i) for i in range(HOLE_FRAMES)]
            + [("crater", 0)] + [("death", i) for i in range(DEATH_FRAMES)]
            + [("tatters", i) for i in range(DEATH_FRAMES)])
    with ProcessPoolExecutor() as pool:
        out = dict(zip(jobs, pool.map(_job, jobs)))
    take = lambda kind, n: [out[(kind, i)] for i in range(n)]  # noqa: E731
    breach = [breach_frame(i, b) for i, b in enumerate(take("breach-body", BREACH_FRAMES))]
    dive = [dive_frame(i, b) for i, b in enumerate(take("dive-body", DIVE_FRAMES))]
    # one palette over the body and the frames that carry it (the body must not flicker)
    body = artkit.quantize_set(take("body", 4) + breach + dive, BODY_COLOURS)
    sets = [(SLUG, body[:4]), (f"{SLUG}-breach", body[4:4 + BREACH_FRAMES]), (f"{SLUG}-dive", body[4 + BREACH_FRAMES:]),
            (f"{SLUG}-glow", artkit.quantize_set(take("glow", 4), 12)),
            (f"{SLUG}-bow", artkit.quantize_set(take("bow", BOW_FRAMES), 24)),
            (f"{SLUG}-ripple", artkit.quantize_set(take("ripple", HEADINGS * RIPPLE_FRAMES), 24)),
            (f"{SLUG}-bulge", artkit.quantize_set(take("bulge", BULGE_FRAMES), 32)),
            (f"{SLUG}-hole", artkit.quantize_set(take("hole", HOLE_FRAMES), 24)),
            (f"{SLUG}-death", artkit.quantize_set(take("death", DEATH_FRAMES), 24)),
            (f"{SLUG}-tatters", artkit.quantize_set(take("tatters", DEATH_FRAMES), 40))]
    with sprites_for(SLUG):
        for name, frames in sets:
            artkit.write_frames(name, frames, SOURCE)
            print(f"{name}: {len(frames)} frames, {frames[0].width}x{frames[0].height}")
        crater = artkit.quantize_set([out[("crater", 0)]], 32)
        artkit.write_frames(f"{SLUG}-crater", crater, SOURCE, single=True)
        print(f"{SLUG}-crater: single, {crater[0].width}x{crater[0].height}")
    print(f"written to {where(SLUG)}")


# =========================================================================== review

def load(name):
    with sprites_for(SLUG):
        return artkit.load_frames(name)


@lru_cache(maxsize=None)
def mars_plate(w, h, seed=151, kind="dunes"):
    """The review's Level 15 ground stand-in until its backdrop exists, from the round-36 Mars stills
    (tools/concept/mars_r36.py, imported unchanged): ``dunes`` the rust dune sea of Tharsis (its
    plains ramp), ``slopes`` the basalt regolith of the Olympus slopes; posterised like the backdrops."""
    import mars_r36 as m36  # concept script, imported unchanged
    n = m36.nfield(w, h, 90, seed, octaves=5)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    if kind == "dunes":
        rip = np.sin((xx * 0.4 + yy + n * 60) / 7.0) * 0.015
        col = m36.shade_rgb(0.45 + 0.28 * n + rip, m36.PLAIN, strength=24, shadow=0.01)
    else:
        stops = [(40, 26, 24), (70, 46, 38), (104, 66, 50), (138, 88, 62), (176, 120, 84)]
        col = m36.shade_rgb(0.4 + 0.34 * n, stops, strength=30, shadow=0.01)
    return m36.tone(m36.rgba(col), 22).convert("RGBA")


def cell_on(img, kind, size, glow=False, under=None, seed=151, xy=(60, 60)):
    plate = mars_plate(480, 540, seed, kind).crop((xy[0], xy[1], xy[0] + size[0], xy[1] + size[1])).copy()
    if under is not None:
        paste_into(plate, under, size[0] / 2, size[1] / 2)
    if glow:
        return artkit.add_light(plate, img, ((size[0] - img.width) // 2, (size[1] - img.height) // 2))
    paste_into(plate, img, size[0] / 2, size[1] / 2)
    return plate


def surfaced(art, maw, bow_i, kind="dunes", size=(96, 96), xy=(60, 60)):
    cell = cell_on(art["bow"][bow_i], kind, size, xy=xy)
    paste_into(cell, art["body"][maw], size[0] / 2, size[1] / 2)
    return artkit.add_light(cell, art["glow"][maw], ((size[0] - 64) // 2, (size[1] - 64) // 2))


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    names = {"body": SLUG, "glow": f"{SLUG}-glow", "bow": f"{SLUG}-bow", "ripple": f"{SLUG}-ripple",
             "bulge": f"{SLUG}-bulge", "breach": f"{SLUG}-breach", "dive": f"{SLUG}-dive", "hole": f"{SLUG}-hole",
             "crater": f"{SLUG}-crater", "death": f"{SLUG}-death", "tatters": f"{SLUG}-tatters"}
    art = {k: load(v) for k, v in names.items()}
    boom = artkit.load_frames("explosion-medium")
    ripples = [art["ripple"][k * RIPPLE_FRAMES] for k in range(HEADINGS)]
    together = []
    for age in range(0, DEATH_FRAMES * 4, 4):
        cell = cell_on(art["crater"][0], "dunes", DEATH_SIZE)
        for frames, steps, glow in ((art["tatters"], 4, False), (boom, 3, True), (art["death"], 4, True)):
            fr = fx.timed(frames, steps, age)
            if fr is not None:
                xy = ((DEATH_SIZE[0] - fr.width) // 2, (DEATH_SIZE[1] - fr.height) // 2)
                cell = artkit.add_light(cell, fr, xy) if glow else (cell.alpha_composite(fr, xy) or cell)
        together.append(cell)
    rows = [
        ("SURFACED BODY (RADIAL, ONE HEADING): MAW SHUT, HALF, OPEN, FIRING", art["body"], 3, False),
        ("LIME BLOOM PER MAW FRAME (ADDITIVE)", art["glow"], 3, True),
        ("AS DRAWN, UP ON THE DUNES: BOW + BODY + BLOOM, MAW 0-3 (BOW FRAMES 0, 2, 4, 6)",
         [surfaced(art, m, 2 * m) for m in range(4)], 2, False),
        ("AS DRAWN ON THE BASALT REGOLITH OF THE SLOPES, MAW 0-3",
         [surfaced(art, m, 2 * m + 1, "slopes") for m in range(4)], 2, False),
        ("BOW-WAVE LOOP UNDER THE BODY (15 FPS), ON THE DUNES", [cell_on(b, "dunes", BOW_SIZE) for b in art["bow"]], 1, False),
        ("RIPPLE, 16 HEADINGS (FRAME 0), 0 = DOWN, CLOCKWISE; ON THE DUNES",
         [cell_on(r, "dunes", RIPPLE_SIZE, xy=(40 + 13 * k, 300)) for k, r in enumerate(ripples)], 2, False),
        ("RIPPLE HEADING 8 (UP: THE USUAL GROUND-RELATIVE TRAVEL), 4 FRAMES; HEADING 6, 10 ON THE SLOPES",
         [cell_on(r, "dunes", RIPPLE_SIZE) for r in art["ripple"][32:36]]
         + [cell_on(art["ripple"][k * 4], "slopes", RIPPLE_SIZE) for k in (6, 10)], 2, False),
        ("BULGE TELL, 10 FRAMES OVER 1.0 S (CRACKS + LIME GLOW FROM FRAME 4)",
         [cell_on(b, "dunes", BULGE_SIZE) for b in art["bulge"]], 2, False),
        ("BREACH, 6 FRAMES OVER 0.3 S (BODY INCLUDED)", [cell_on(b, "dunes", FX_SIZE) for b in art["breach"]], 2, False),
        ("DIVE, 8 FRAMES OVER 0.4 S (BUILT-IN BODY; LAYER FLIP AT FRAME 4)",
         [cell_on(b, "dunes", FX_SIZE) for b in art["dive"]], 1, False),
        ("FILLING HOLE AFTER A DIVE, 8 FRAMES OVER 1.6 S; CRATER OF A KILL (DUNES, SLOPES)",
         [cell_on(b, "dunes", HOLE_SIZE) for b in art["hole"]]
         + [cell_on(art["crater"][0], "dunes", CRATER_SIZE), cell_on(art["crater"][0], "slopes", CRATER_SIZE)], 1, False),
        ("DEATH: LIME BURST (ADDITIVE)", art["death"], 1, True),
        ("DEATH: SAND SPOUT AND PIECES (SOLID)", art["tatters"], 1, False),
        ("TOGETHER OVER THE CRATER WITH EXPLOSION-MEDIUM, EVERY 4TH STEP", together, 1, False),
    ]
    sheet = artkit.review_sheet("BURROWER - FINAL SPRITES (SAND MAW)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, play_field(art, boom), CONCEPT, SLUG, fps=GIF_FPS)


# --------------------------------------------------------------------------- the play-field GIF

FW, FH, SCROLL, GIF_FPS, GIF_T = 480, 540, 150.0, 10, 7.2   # 10 fps: the scrolling ground, under 8 MB
SHIP_Y = 470
T_BULGE, T_UP, T_DIVE, T_BREACH = 1.0, 2.0, 0.4, 0.3
# per unit: (entry point, [(travel start, spot, travel duration)...], kill time or None)
UNITS = [((110, 200), [(0.0, (150, 300), 1.2), (4.6, (290, 250), 1.6)], None),
         ((360, 160), [(0.8, (330, 260), 1.0)], 3.9),
         ((180, 140), [(2.6, (210, 240), 1.2)], None)]


def unit_events(unit):
    """Per unit a list of (t0, t1, state, data) segments; states: travel, bulge, up, dive."""
    entry, legs, kill = unit
    out, pos = [], entry
    for start, spot, travel in legs:
        out.append((start, start + travel, "travel", (pos, spot)))
        b = start + travel
        out.append((b, b + T_BULGE, "bulge", spot))
        u = b + T_BULGE
        if kill is not None and kill < u + T_UP:
            out.append((u, kill, "up", spot))
            out.append((kill, 99.0, "dead", spot))
            return out
        out.append((u, u + T_UP, "up", spot))
        out.append((u + T_UP, u + T_UP + T_DIVE, "dive", spot))
        pos = spot
    return out


def maw_at(age):
    if age < 0.35:
        return 1 if age >= 0.3 else 0
    if age < 0.4:
        return 2
    if age < 0.5:
        return 3
    if age < 1.2:
        return 2
    return 1 if age < 1.3 else 0


def play_field(art, boom):
    plate = mars_plate(FW, FH + int(SCROLL * GIF_T) + 8, 157, "dunes")
    ship = artkit.load_frames("ship")[2]
    orb = artkit.load_frames("orb")
    marks = []                                                   # (t0, kind, x, y_at_t0)
    for unit in UNITS:
        for t0, t1, state, data in unit_events(unit):
            if state == "dive":
                marks.append((t1, "hole", *data))
            if state == "dead":
                marks.append((t0, "crater", *data))
    shots = []
    for unit in UNITS:
        for t0, t1, state, data in unit_events(unit):
            if state == "up" and t1 - t0 > 0.4:
                for a in np.linspace(0, TAU, 10, endpoint=False) + 0.3:
                    shots.append((t0 + 0.4, data[0] + np.cos(a) * 24, data[1] + np.sin(a) * 24, np.cos(a) * 150, np.sin(a) * 150))
    frames = []
    for f in range(int(GIF_T * GIF_FPS)):
        t = f / GIF_FPS
        top = plate.height - FH - int(SCROLL * t)
        img = plate.crop((0, top, FW, top + FH)).copy()
        for t0, kind, x, y in marks:                             # ground decals scroll with the ground
            if t < t0:
                continue
            yy = y + SCROLL * (t - t0)
            if kind == "crater":
                paste_into(img, art["crater"][0], x, yy)
            else:
                fr = fx.timed(art["hole"], STEPS["hole"], int((t - t0) * STEP))
                if fr is not None:
                    paste_into(img, fr, x, yy)
        later = []
        for ui, unit in enumerate(UNITS):
            for t0, t1, state, data in unit_events(unit):
                if not t0 <= t < t1:
                    continue
                age = t - t0
                if state == "travel":
                    (ax, ay), (bx, by) = data
                    s = age / (t1 - t0)
                    x, y = ax + (bx - ax) * s, ay + (by - ay) * s
                    vx, vy = (bx - ax) / (t1 - t0), (by - ay) / (t1 - t0) - SCROLL
                    k = int(round(((np.arctan2(vy, vx) - np.pi / 2) % TAU) / (TAU / HEADINGS))) % HEADINGS
                    j = int(t * 12) % RIPPLE_FRAMES
                    dx, dy = np.cos(np.pi / 2 + TAU * k / HEADINGS), np.sin(np.pi / 2 + TAU * k / HEADINGS)
                    paste_into(img, art["ripple"][k * RIPPLE_FRAMES + j], x - dx * RIPPLE_HEAD, y - dy * RIPPLE_HEAD)
                elif state == "bulge":
                    paste_into(img, art["bulge"][min(BULGE_FRAMES - 1, int(age / 0.1))], *data)
                elif state == "up":
                    x, y = data
                    bow = art["bow"][int(t * 15) % BOW_FRAMES]
                    hit = ui == 1 and t > 3.4 and int(t * 14) % 2
                    if age < T_BREACH:
                        k = int(age / 0.05)
                        if k >= 2:
                            paste_into(img, bow, x, y)
                        later.append((art["breach"][k], x, y, None))
                    else:
                        paste_into(img, bow, x, y)
                        m = maw_at(age)
                        body = art["body"][m]
                        if hit:
                            a = np.array(body).astype(np.float64)
                            a[..., :3] = a[..., :3] * 0.45 + 255 * 0.55
                            body = Image.fromarray(a.astype(np.uint8), "RGBA")
                        later.append((body, x, y, art["glow"][m]))
                elif state == "dive":
                    x, y = data
                    k = min(DIVE_FRAMES - 1, int(age / 0.05))
                    if k <= 3:
                        paste_into(img, art["bow"][int(t * 15) % BOW_FRAMES], x, y)
                    later.append((art["dive"][k], x, y, None))
                elif state == "dead":
                    x, y = data
                    yy = y + SCROLL * age
                    st = int(age * STEP)
                    for layer, steps, glow in ((art["tatters"], 4, False), (boom, 3, True), (art["death"], 4, True)):
                        fr = fx.timed(layer, steps, st)
                        if fr is not None:
                            later.append((fr, x, yy, "add" if glow else None))
        for fr, x, y, glow in later:
            if glow == "add":
                img = artkit.add_light(img, fr, (int(round(x - fr.width / 2)), int(round(y - fr.height / 2))))
                continue
            paste_into(img, fr, x, y)
            if glow is not None:
                img = artkit.add_light(img, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))
        for t0, x, y, vx, vy in shots:
            if t >= t0:
                bx, by = x + vx * (t - t0), y + vy * (t - t0)
                if -10 < bx < FW + 10 and -10 < by < FH + 10:
                    paste_into(img, orb[int(t * 10) % len(orb)], bx, by)
        sx = 240 + 40 * np.sin(0.6 * t)
        shadow = sprite.shadow_of(ship)
        img.alpha_composite(shadow, (int(sx - 24 + 20), int(SHIP_Y - 24 + 28)))
        paste_into(img, ship, sx, SHIP_Y)
        raster.draw_text(img, 8, FH - 14, f"BURROWER FINAL R38 - GROUND SCROLLING 150 PX/S - {t:4.2f} S",
                         raster.LABEL)
        frames.append(img)
    return frames


if __name__ == "__main__":
    artkit.REVIEW_ROUND = REVIEW_ROUND
    if "--review" not in sys.argv[1:]:
        build()
    review()
