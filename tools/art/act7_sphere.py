#!/usr/bin/env python3
"""Production art: Level 47 "Seraph Gauntlet" (design/campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet; M11 part E
batch, concept round 92, step ART-E): the Choir Seraph (round 84 A.4 a "blades"), the inner-sphere backdrop kit, its
core light and the memory pearl.

Everything is in round 84 Q10 b's **one Vrell palette** with Level 47's light (design/world/vrell-space, *Light per
level*): the inner sphere's Vrell stone in the palette's violets (a teal-black in its grooves), lit gold-white from the
core ahead (the top of the screen), the act's richest light; the shell kept a step darker than the Seraph's ivory, its
gold-white glow and the bullets' outline carrying the units; the arrival rings violet (ART-A's ``teleport-ring-large``,
built), never gold. The Seraph is round 84's model (tools/concept/enemies_r84.py ``seraph_model("a", fold, glow)``,
imported unchanged) on the production path (4x, 1-bit alpha, 40 colours over its normal sets). Every glow and haze is a
float field written unquantised (far more than 32 steps, no ordered dither: DitherGuardTest's measure) that fades to 0
inside its canvas (``--check`` fails on a lit border pixel of an additive set). No tile or piece has a straight seam or
a ruler-straight line: the ribs are arcs on a warped, unevenly spaced phase, the edges ragged (``--check`` runs a
row/column-difference scan of every backdrop file and fails on a straight run of 120 px or more, hard or soft).

Sprites (assets/sprites/ once the data claims the root, else build/art-held/sprites/: belt_units.py's rule; ``additive``
= premultiplied on black, GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha; steps = game steps of 1/60 s; every set is
centred on the unit):

  The Choir Seraph (design/enemies/air/choir-seraph; ``fixed``, facing down; 72x76 inside an 88x88 canvas)
  choir-seraph_0..3           88x88 normal: gliding level (the default animation, 10 fps): the heart's light breathing,
                              the blade wings' gold-white leading edges
  choir-seraph-bank_0..6      88x88 normal: mirroring, banked with its sideways speed: 0..2 moving toward the screen's
                              left, rolled 30/20/10 degrees (the screen-left wing dipped: shorter, darker), 3 level,
                              4..6 moving right, 10/20/30 degrees; rendered rolled under a mild perspective, the key
                              light staying top-left (never mirrored)
  choir-seraph-glow           104x104 additive (single): the heart's and the leading edges' soft gold-white light,
                              drawn over any body frame while it shows (its readability over the bright ribs)
  choir-seraph-flare_0..5     104x104 additive: the spiral's tell, a violet flare at the two fore wing roots swelling
                              and gone (4 steps a frame: 0.4 s), over the first mirroring frames
  choir-seraph-fold_0..5      88x88 normal: the jump's tell, the wings folding back along the body over the 0.3 s fold
                              (3 steps a frame); frame 5 = folded, held through the built 0.2 s fade (the unit's
                              opacity under ``teleport-fade``, VrellLooks; no frames here)
  choir-seraph-leave_0..11    88x88 normal: the leaving tell (its last fold, 0.8 s: 4 steps a frame): the wings folding
                              slower while they flare white; the 40 px rise is the simulation's
  choir-seraph-leave-glow_0..11  104x104 additive: the white-gold light of the leaving flare, growing to frame 9, held
  choir-seraph-tatters_0..15  128x128 normal (``-tatters``, 4 steps a frame): the blade wings breaking into ivory shards
                              with gold-white edges and bone segments flung out, turning, shrinking away
  choir-seraph-death_0..15    128x128 additive (``-death``): the gold-white feathers of light drifting out and fading,
                              decaying from the first frame (never a flash)
  choir-seraph-shot_0..3      15x15 normal: the spiral's violet orb (a violet-white core pulsing, a dark outline), for
                              BulletLooks' own looks (pulsing at the small orb's rate)
  The memory pearl (Level 47's secret: step E2's debris chunk ``memory-pearl``, 36x36, 20 HP, on the play plane as
  Level 46's courier; the ``debris-`` root is claimed by the level's ``debris``)
  debris-memory-pearl_0..15   36x36 normal: a shard of the shell's stone (ragged, ribbed, lit gold-white from the top)
                              with the pearl in a socket at its centre, milky nacre with the Moon faint inside it;
                              tumbling: frame k turned k x 22.5 degrees clockwise (the light fixed)
  debris-memory-pearl-glow_0..7   40x40 additive: the remembered image flickering faintly (a loop, 6 fps), centred on
                              the chunk (E4: over every tumble frame)
  debris-memory-pearl-break_0..7  64x64 normal: the shard shot apart: nacre and stone chips flung out
  debris-memory-pearl-release_0..7  64x64 additive: the memory's light let go, a pale ring widening and fading (E4: with
                              the break)

Backdrop (assets/backdrop/level-47/; rows y-up as the game reads them; ``--proposal`` writes the ``backdrop:`` block, the
sections and the core light's curve to concept/backdrop-proposal.yaml: step E4 wires them):
  deep-1 .. deep-4     ``deep`` 0.15, opaque: one continuous field cut at the sections' seams: section 1 near-black
                       space with the dead fleet's last hulls far and small, then the shell's ragged rim and its wall
                       curving away below; 2 the shell's ribs crossing (main ribs on an uneven arched phase, fine ribbing
                       between them), lit gold-white along their upper edges; 3 the ribs bending into the vault's rings
                       and spokes; 4 the richest light, warmed toward the core
  rib-a .. rib-c       ``far`` 0.5: the shell's great ribs (the third scale), arched bands of stone crossing the screen,
                       their ends tapering out inside the canvas (beyond the screen's sides), lit gold-white along the
                       upper flank, a soft shadow on the shell under them
  hulk-far-a, -b       ``far``: the dead fleet's last hulls falling behind (section 1): Level 46's hulks, small, warmed
  citadel-far          ``deep`` 380x150: Level 48's citadel, a dark mass grown over gold at the core light's far side
                       (section 4, at the top by t~255): drawn after the core light (E4)
  core-light           ``deep`` 720x720, additive, not placed: the core's light, a smooth gold-white disc of light; E4
                       draws it in screen space (BackdropAtmosphere), centred above the top edge at the curve's y,
                       scaled and weighted by it (the yaml's comment): a glow at the top edge from section 3, the top
                       third in section 4, the top half by the end
  light-ring-a, -b     ``low-air``: the Heralds' rings of light (scenery), soft gold-white tilted rings, still (never
                       pulsing, never in a bullet's colour), drifting slowly
  motes-a, -b          ``low-air``: drifting motes of light, faint warm specks (never bullet-sized dots)

Reviews (round 92, ``*-final-r92-a``): design/enemies/air/choir-seraph/concept/choir-seraph; the level's concept/
backdrop and l47-props. The intel picture (30x30) is tools/art/intel.py's, from ``seraph_intel_hi`` here; the two
briefing images are tools/art/briefing_images.py's.
Run: nice -n 19 python3 tools/art/act7_sphere.py [seraph] [pearl] [sphere] [--review | --proposal | --check]
     (no part = all, a few minutes; --review only rebuilds the sheets)
"""
import hashlib
import os
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
import yaml
from PIL import Image, ImageDraw

import act7_reef as reef
import artkit
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import checker_share, cut_tile, rgba, smoothstep, wrap_score
from act7_reef import add, blur3, edge_fade, grid, label, lit_border, to_img

u84 = reef.u84
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
from render import raster, sdf  # noqa: E402
from render.sdf import rotate_y  # noqa: E402

SCRIPT = "act7_sphere.py"
BATCH = "M11 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r92"
artkit.REVIEW_ROUND = ROUND
L47 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-47-seraph-gauntlet"
CONCEPT = L47 / "concept"
SERAPH_C = DESIGN / "enemies" / "air" / "choir-seraph" / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-47"
L46_OUT = ROOT / "assets" / "backdrop" / "level-46"
W, SCREEN = 480, 540

# Level 47's light: gold-white from the core ahead, over the palette's violets
GOLD_W = np.array((242.0, 220.0, 154.0))            # #F2DC9A, the Seraph's and the core's gold-white
CORE_W = np.array((255.0, 236.0, 186.0))
SPACE = np.array((6.0, 4.0, 11.0))
DUST = np.array((26.0, 14.0, 38.0))                 # the palette's darkest violet
STONE = [(0.0, (7, 4, 13)), (0.3, (24, 12, 38)), (0.62, (50, 26, 72)), (1.0, (82, 50, 104))]   # Vrell stone, palette
# the stone under the core's light: the palette's violets, the lit faces warmed toward the gold-white light
LIT = [(0.0, (8, 4, 14)), (0.28, (32, 15, 48)), (0.52, (64, 36, 84)), (0.76, (112, 82, 96)), (1.0, (188, 166, 120))]
TEAL_K = np.array((14, 40, 46), float)              # the palette's teal-black, in the grooves
VIOLET = np.array(u84.VIOLET, float)                # (154, 77, 255)
VIOLET_W = np.array(u84.VIOLET_W, float)
IVORY = np.array((236.0, 226.0, 200.0))
BONE = np.array((220.0, 207.0, 180.0))
BONE_D = np.array((92.0, 74.0, 92.0))
NACRE = np.array((226.0, 220.0, 236.0))

ADDITIVE = {"choir-seraph-glow", "choir-seraph-flare", "choir-seraph-leave-glow", "choir-seraph-death",
            "debris-memory-pearl-glow", "debris-memory-pearl-release"}


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def load(name):
    return l23kit.load(name)


def light_img(light, margin=4):
    return jovkit.additive_rgb(edge_fade(light, margin))


def pad(img, size):
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(img, ((size - img.width) // 2, (size - img.height) // 2))
    return out


# =========================================================================== the Choir Seraph

SE, SE_PX = 88, 34.0
SE_EXT = SE / SE_PX
GL = 104                                             # the glow sets' canvas (room for the halo to fade out)
GLIDE = (0.9, 1.0, 1.12, 1.0)
BANK = (30, 20, 10, 0, -10, -20, -30)                # roll: + dips the screen-left wing
FOLD_N, LEAVE_N, FLARE_N = 6, 12, 6
ROOTS = [(-5.0, 7.0), (5.0, 7.0)]                    # the fore wing roots from the centre, screen px (y down)
CAMERA = 3.5                                         # the bank's perspective (model units above the plane)


def _whitened(mats, w):
    """The leaving flare: every material lifted toward white light, the bone glowing white-gold."""
    out = []
    for m in mats:
        alb = tuple(np.array(m.albedo) * (1 - 0.35 * w) + 0.35 * w)
        em = tuple(np.array(m.emission or (0.0, 0.0, 0.0)) + np.array((1.0, 0.96, 0.86)) * 0.3 * w)
        out.append(replace(m, albedo=alb, emission=em))
    return out


def seraph_scene(fold=0.0, glow=1.0, roll=0.0, white=0.0):
    scene, mats = u84.seraph_model("a", fold, glow)
    if white:
        mats = _whitened(mats, white)
    scene, mats = bu.turned(scene, mats, 0, 16)
    if roll:
        a = np.radians(roll)
        base = scene
        scene = lambda p: base(rotate_y(p, a))  # noqa: E731
    return scene, mats


def _seraph(fold, glow, roll, white, emit):
    scene, mats = seraph_scene(fold, glow, roll, white)
    if emit:
        mats = bu.emission_only(mats)
    return bu.render(scene, mats, (SE, SE), SE_EXT, crisp=80, camera=CAMERA if roll else None)


def seraph_jobs():
    jobs = [("seraph", 0.0, g, 0.0, 0.0, False) for g in GLIDE]
    jobs += [("seraph", 0.0, 1.0, float(r), 0.0, False) for r in BANK]
    jobs += [("seraph", f / (FOLD_N - 1), 1.0, 0.0, 0.0, False) for f in range(FOLD_N)]
    for f in range(LEAVE_N):
        fold, white = leave_state(f)
        jobs.append(("seraph", fold, 1.0, 0.0, white, False))
    jobs.append(("seraph", 0.0, 1.0, 0.0, 0.0, True))                         # the level frame's light
    for f in range(LEAVE_N):
        fold, white = leave_state(f)
        jobs.append(("seraph", fold, 1.0, 0.0, white, True))
    return jobs


def leave_state(f):
    """The leaving tell's frame f of 12 (0.8 s): the fold eased over the whole tell, the white flare growing to frame 9."""
    u = f / (LEAVE_N - 1)
    fold = smoothstep(0.0, 1.0, u) * 0.92
    white = float(smoothstep(0.0, 0.75, u)) * 0.8
    return float(fold), white


def emission_light(img):
    a = np.array(img.convert("RGBA")).astype(np.float64)
    return a[..., :3] * (a[..., 3:4] / 255.0)


def seraph_glow(em_img, k=1.0):
    """The heart's and the leading edges' halo: the emission blurred (only the halo: the body carries its own light)."""
    light = np.zeros((GL, GL, 3))
    o = (GL - SE) // 2
    light[o:o + SE, o:o + SE] = emission_light(em_img)
    halo = blur3(light, 2.4) * 0.55 + blur3(light, 6.0) * 0.45
    return light_img(halo * k, 10)


def flare_frames():
    out = []
    for f in range(FLARE_N):
        u = (f + 0.5) / FLARE_N
        k = np.sin(np.pi * u) ** 1.3 * (1 - u) ** 0.4
        light = np.zeros((GL, GL, 3))
        x, y = grid(GL, GL)
        for rx, ry in ROOTS:
            cx, cy = GL / 2 + rx, GL / 2 + ry
            rr = np.hypot(x - cx, y - cy)
            add(light, VIOLET, (np.exp(-(rr / (5.0 + 3 * u)) ** 2) * 0.9 + np.exp(-(rr / 12.0) ** 2) * 0.3) * k)
            add(light, VIOLET_W, np.exp(-(rr / 2.2) ** 2) * 0.8 * k)
        out.append(light_img(light, 12))
    return out


def leave_glow(em_imgs):
    out = []
    for f, em in enumerate(em_imgs):
        _, white = leave_state(f)
        light = np.zeros((GL, GL, 3))
        o = (GL - SE) // 2
        light[o:o + SE, o:o + SE] = emission_light(em)
        halo = blur3(light, 3.0) * 0.5 + blur3(light, 7.5) * 0.45
        x, y = grid(GL, GL)
        rr = np.hypot(x - GL / 2, (y - GL / 2) * 0.8)
        add(halo, CORE_W, np.exp(-(rr / 20.0) ** 2) * 0.22 * white)
        out.append(light_img(halo, 10))
    return out


def _blade_parts(rng):
    """The shards the wings break into: (x0, y0) from the centre (y down), a polygon in unit size, its size, a gold edge."""
    parts = []
    for side in (-1, 1):
        for wing, (ang, L) in enumerate(((-0.35, 34.0), (-1.05, 30.0))):
            for j in range(3):
                d = (j + 0.6) / 3.2 * L                              # along the blade from its root
                x0, y0 = side * np.cos(ang) * d, np.sin(ang) * d + 6 - wing * 4   # the wings sweep back (up)
                a = np.arctan2(y0, x0)
                parts.append(dict(x0=x0, y0=y0, ang=a, len=rng.uniform(7, 10), wid=rng.uniform(2.2, 3.2), edge=True,
                                  sp=rng.uniform(18, 30), w=rng.uniform(-3, 3), rot=a + rng.uniform(-0.4, 0.4)))
    for j in range(4):
        parts.append(dict(x0=rng.uniform(-3, 3), y0=-14 + j * 8, ang=rng.uniform(0, TAU), len=rng.uniform(3.5, 5),
                          wid=rng.uniform(2.6, 3.4), edge=False, sp=rng.uniform(8, 14), w=rng.uniform(-2, 2),
                          rot=rng.uniform(0, TAU)))
    return parts


def tatters_frames(n=16, size=128, ss=4):
    rng = np.random.default_rng(4711)
    parts = _blade_parts(rng)
    out = []
    for f in range(n):
        u = f / (n - 1) * 0.94
        im = Image.new("RGBA", (size * ss, size * ss), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        for p in parts:
            r0 = np.hypot(p["x0"], p["y0"])
            a = np.arctan2(p["y0"], p["x0"]) if r0 > 1 else p["ang"]
            dist = p["sp"] * (1 - (1 - u) ** 1.5)
            x = size / 2 + p["x0"] + np.cos(a) * dist
            y = size / 2 + p["y0"] + np.sin(a) * dist + 10 * u * u
            L, wd = p["len"] * (1 - u) ** 0.8, p["wid"] * (1 - u) ** 0.8
            if L < 0.9:
                continue
            ra = p["rot"] + p["w"] * u
            c, s = np.cos(ra), np.sin(ra)
            pts = [(x + c * L, y + s * L), (x - s * wd, y + c * wd), (x - c * L * 0.7, y - s * L * 0.7),
                   (x + s * wd * 0.6, y - c * wd * 0.6)]
            lam = 0.75 + 0.25 * np.cos(ra + 0.8)
            col = np.clip(BONE * lam * (1 - 0.3 * u), 0, 255)
            d.polygon([(px * ss, py * ss) for px, py in pts], fill=tuple(int(v) for v in col) + (255,),
                      outline=tuple(int(v) for v in BONE_D) + (255,))
            if p["edge"]:
                d.line([(pts[0][0] * ss, pts[0][1] * ss), (pts[1][0] * ss, pts[1][1] * ss)],
                       fill=tuple(int(v) for v in GOLD_W) + (255,), width=ss)
        a = np.array(im.resize((size, size), Image.BOX)).astype(np.float64)
        rgb = a[..., :3] / np.maximum(a[..., 3:4] / 255, 1e-6)
        out.append(to_img(np.clip(rgb, 0, 255), a[..., 3] / 255))
    return artkit.quantize_set(out, 32)


def death_frames(n=16, size=128):
    """The feathers of light: soft gold-white streaks drifting out from the wings, turning, fading."""
    rng = np.random.default_rng(4712)
    feathers = []
    for _ in range(20):
        a = rng.uniform(np.pi * 0.85, np.pi * 2.15)                     # mostly from the wings (up and out)
        r0 = rng.uniform(8, 32)
        feathers.append((a, r0, rng.uniform(10, 22), rng.uniform(5.5, 9.0), rng.uniform(0, np.pi),
                         rng.uniform(-1.6, 1.6), rng.uniform(0.6, 1.0)))
    out = []
    x, y = grid(size, size)
    for f in range(n):
        u = f / (n - 1) * 0.95
        light = np.zeros((size, size, 3))
        rr = np.hypot(x - size / 2, y - size / 2)
        add(light, GOLD_W, np.exp(-(rr / (12 + 18 * u)) ** 2) * 0.32 * (1 - u) ** 2.2)
        for a, r0, sp, L, ang, w, k in feathers:
            d = r0 + sp * (1 - (1 - u) ** 1.3)
            cx = size / 2 + np.cos(a) * d * 1.1
            cy = size / 2 + np.sin(a) * d * 0.75 + 16 * u * u
            th = ang + w * u
            c, s = np.cos(th), np.sin(th)
            du, dv = (x - cx) * c + (y - cy) * s, -(x - cx) * s + (y - cy) * c
            Lu = L * (1 - 0.4 * u)
            f_ = np.exp(-(du / Lu) ** 2 * 2.2) * np.exp(-(dv / 1.1) ** 2)
            add(light, GOLD_W * 0.9 + 25, f_ * 1.5 * k * (1 - u) ** 1.3)
        out.append(light_img(light, 12))
    return out


SHOT = 15


def shot_frames(n=4, ss=8):
    out = []
    for f in range(n):
        x, y = grid(SHOT, SHOT, ss)
        rr = np.hypot(x - SHOT / 2, y - SHOT / 2)
        rgb = np.zeros(x.shape + (3,))
        al = (rr < 6.6).astype(float)
        rgb[rr < 6.6] = (22, 12, 40)
        rgb[rr < 5.4] = VIOLET
        rgb[rr < 4.0] = (196, 150, 255)
        c = 2.0 + 0.8 * np.sin(np.pi * f / n) ** 2
        rgb[rr < c] = (244, 234, 255)
        rgb = rgb.reshape(SHOT, ss, SHOT, ss, 3).mean(axis=(1, 3))
        al = al.reshape(SHOT, ss, SHOT, ss).mean(axis=(1, 3))
        out.append(to_img(rgb / np.maximum(al[..., None], 1e-6), al))
    return artkit.quantize_set(out, 12)


def build_seraph(res):
    i = 0
    glide = res[i:i + len(GLIDE)]
    i += len(GLIDE)
    bank = res[i:i + len(BANK)]
    i += len(BANK)
    fold = res[i:i + FOLD_N]
    i += FOLD_N
    leave = res[i:i + LEAVE_N]
    i += LEAVE_N
    em = res[i]
    i += 1
    leave_em = res[i:i + LEAVE_N]
    body = artkit.quantize_set(glide + bank + fold, 40)
    save_set("choir-seraph", body[:4])
    save_set("choir-seraph-bank", body[4:11])
    save_set("choir-seraph-fold", body[11:])
    save_set("choir-seraph-leave", artkit.quantize_set(leave, 40))
    save_set("choir-seraph-glow", [seraph_glow(em, 0.9)], single=True)
    save_set("choir-seraph-flare", flare_frames())
    save_set("choir-seraph-leave-glow", leave_glow(leave_em))
    save_set("choir-seraph-tatters", tatters_frames())
    save_set("choir-seraph-death", death_frames())
    save_set("choir-seraph-shot", shot_frames())


def seraph_intel_hi():
    """The Seraph level, nose down, wings open, for the hangar intel's 30 px portrait (an 8x render)."""
    scene, mats = seraph_scene()
    return sdf.render(scene, mats, (240, 240), 2.5)


# =========================================================================== the memory pearl

CHUNK, PEARL_R, LIP_R = 36, 4.8, 7.0
CHUNK_N = 16


def stone_rgb(v):
    return raster.ramp(STONE, np.clip(v, 0, 1))


def moon(x, y, cx, cy, r, k=1.0):
    """The Moon as the Vrell saw it in Act 1: a pale disc lit from the right, a few maria (a value 0..1 field)."""
    dx, dy = (x - cx) / r, (y - cy) / r
    q = dx * dx + dy * dy
    disc = np.clip((1 - q) * 6, 0, 1)
    nz = np.sqrt(np.clip(1 - q, 0, 1))
    lam = np.clip(0.25 + 0.75 * (0.7 * dx - 0.2 * dy + 0.45 * nz), 0, 1)
    maria = 1 - 0.35 * (np.exp(-((dx + 0.25) ** 2 + (dy + 0.2) ** 2) / 0.08)
                        + np.exp(-((dx - 0.2) ** 2 + (dy - 0.25) ** 2) / 0.05))
    return disc * lam * maria * k


def chunk_frame(k, ss=8):
    """The memory pearl in its shard of the shell's stone, turned k x 22.5 degrees clockwise (the shape and its ribs
    turn, the light stays: the core's gold-white from the top, the key from the top left). The shard's outline is
    ragged (no straight edge), ribbed like the shell; the pearl sits in a socket at its centre, milky nacre with the
    Moon faint inside it (the remembered image keeps its own way up)."""
    w = h = CHUNK
    x, y = grid(w, h, ss)
    cx = cy = CHUNK / 2
    th = k * TAU / CHUNK_N
    c, s_ = np.cos(th), np.sin(th)
    X, Y = x - cx, y - cy
    u, v = X * c + Y * s_, -X * s_ + Y * c                            # shard coords (the shape turns)
    ang = np.arctan2(v / 11.5, u / 15.0)
    rng = np.random.default_rng(4721)
    rad = 1.0 + sum(rng.uniform(0.04, 0.1) * np.sin(m * ang + rng.uniform(0, TAU)) for m in (2, 3, 5, 7))
    rad = rad - 0.18 * np.exp(-((ang - 2.3) / 0.3) ** 2)            # a bite out of one end
    q = np.hypot(u / 15.0, v / 11.5) / rad
    inside = q < 1.0
    hgt = np.sqrt(np.clip(1 - q ** 2, 0, 1)) * 0.5
    rib = np.exp(-(((v + 2.2 * (u / 15.0) ** 2 * 6 + 1.3 * np.sin(u / 4.0)) / 5.5 - 0.5) % 1.0 - 0.5) ** 2 / 0.03)
    hgt = hgt + 0.16 * rib * (q < 0.85)
    rr = np.hypot(X, Y)
    hgt = hgt + 0.25 * np.exp(-((rr - LIP_R + 0.8) / 1.3) ** 2) - 0.3 * (rr < LIP_R - 0.6)
    gy, gx = np.gradient(hgt, axis=0) * ss, np.gradient(hgt, axis=1) * ss
    lam = np.clip(0.45 + 14.0 * gy + 7.0 * gx, 0, 1.4)               # y down: the top-facing slopes lit
    edge = np.clip(16.0 * gy - 0.3, 0, 1) ** 1.5
    tex = raster.fbm(w * ss, h * ss, 14, 4723 + k, octaves=3, period=False)
    v = 0.1 + 0.48 * lam * (0.5 + 0.5 * np.clip(hgt * 2, 0, 1)) + 0.3 * edge + 0.06 * (tex - 0.5)
    rgb = raster.ramp(LIT, np.clip(v, 0, 0.9)) + TEAL_K * (0.25 * (1 - np.clip(lam, 0, 1)))[..., None]
    al = (inside | (rr < LIP_R)).astype(float)
    # a dark outline round the shard (it reads as a breakable thing over the shell, as the bullets do)
    ring = (q > 0.9) & inside & (rr >= LIP_R)
    rgb[ring] = rgb[ring] * 0.35 + np.array((10.0, 5.0, 18.0)) * 0.65
    p = rr < PEARL_R
    nz = np.sqrt(np.clip(1 - (rr / PEARL_R) ** 2, 0, 1))
    plam = np.clip(0.35 + 0.65 * (-0.45 * X - 0.6 * Y) / PEARL_R + 0.45 * nz, 0, 1.2)
    m = moon(x, y, cx + 0.5, cy + 0.3, PEARL_R * 0.64, 0.6)
    col = NACRE[None, None, :] * (0.42 + 0.4 * plam[..., None])
    col = col * (1 - 0.7 * m[..., None]) + np.array((240.0, 240.0, 246.0)) * m[..., None]
    col = col + 255 * (np.exp(-((X + 1.9) ** 2 + (Y + 2.0) ** 2) / 1.2) * 0.6)[..., None]
    rgb[p] = np.clip(col, 0, 255)[p]
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    al = al.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return to_img(rgb, al)


def pearl_glow_frames(n=8, size=40):
    x, y = grid(size, size)
    rr = np.hypot(x - size / 2, y - size / 2)
    out = []
    for f in range(n):
        ph = TAU * f / n
        k = 0.62 + 0.16 * np.sin(ph) + 0.1 * np.sin(2 * ph + 1.1) + 0.06 * np.sin(3 * ph + 2.3)
        light = np.zeros((size, size, 3))
        add(light, np.array((214.0, 214.0, 236.0)), np.exp(-(rr / 6.0) ** 2) * 0.3 * k
            + np.exp(-(rr / 11.0) ** 2) * 0.14 * k)
        add(light, np.array((236.0, 236.0, 244.0)), moon(x, y, size / 2 + 0.5, size / 2 + 0.3, PEARL_R * 0.64) * 0.3 * k)
        out.append(light_img(light, 8))
    return out


def pearl_release_frames(n=8, size=64):
    x, y = grid(size, size)
    rr = np.hypot(x - size / 2, y - size / 2)
    out = []
    for f in range(n):
        u = (f + 1) / n
        light = np.zeros((size, size, 3))
        R = 6 + 20 * u
        add(light, np.array((210.0, 212.0, 240.0)), np.exp(-((rr - R) / (1.6 + 2 * u)) ** 2) * 0.7 * (1 - u) ** 1.3)
        add(light, NACRE, np.exp(-(rr / (5 + 6 * u)) ** 2) * 0.55 * (1 - u) ** 2)
        out.append(light_img(light, 6))
    return out


def build_pearl():
    save_set("debris-memory-pearl", artkit.quantize_set([chunk_frame(k) for k in range(CHUNK_N)], 32))
    save_set("debris-memory-pearl-glow", pearl_glow_frames())
    cols = [tuple(NACRE), tuple(NACRE * 0.8), tuple(stone_rgb(np.array(0.5))), tuple(stone_rgb(np.array(0.75)))]
    save_set("debris-memory-pearl-break", artkit.quantize_set(reef.chips_frames(8, 64, cols, 4724, count=14,
                                                                                speed=(26, 44), size=(1.3, 2.6)), 16))
    save_set("debris-memory-pearl-release", pearl_release_frames())


# =========================================================================== the level clock

FACTORS = {"deep": 0.15, "far": 0.5, "low-air": 1.3}
DATA_PATH = L47 / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Shell's Edge", "end": 48, "atmosphere": "clear", "tiles": ["deep-1"]},
    {"name": "The Ribs", "end": 120, "speed": 120, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-2"]},
    {"name": "The Choir Hall", "end": 200, "speed": 110, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-3"]},
    {"name": "The Core Light", "end": 266, "speed": 140, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-4"]},
]
SECTIONS = DATA["sections"] if DATA and DATA.get("sections") else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 130)) if DATA else 130.0
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END = CLOCK.end
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
DEEP = [f"deep-{i + 1}" for i in range(len(SECTIONS))]
SEED = 4751


def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


# =========================================================================== the deep field: the inner sphere's shell

RIM_ROW = 900.0                      # the shell's rim (section 1): its ragged lip crosses the top at t~18
RIB_P = 112.0                        # the main ribs' mean spacing, deep rows
BOW = 64.0                           # the arcs' bow over the half width (the ends lower: the sphere's curve)


def _hash(k, seed):
    return ((np.sin(k * 12.9898 + seed * 78.233) * 43758.5453) % 1.0 + 1.0) % 1.0


def _ridge(phase, width, seed):
    """Unevenly sized rounded ridges on a phase: a ridge per whole step, its width and height from a hash of its index;
    the height field, smooth everywhere (a bump of zero slope at its feet)."""
    k = np.floor(phase)
    t = phase - k - 0.5
    w = width * (0.75 + 0.5 * _hash(k, seed))
    hk = 0.7 + 0.45 * _hash(k, seed + 1)
    q = np.clip(np.abs(t) / w, 0, 1)
    return hk * (1 - q * q * (3 - 2 * q)) ** 1.3


def light_level(rows):
    """The core's light by layer row: dim at the rim, richer rib by rib, the richest in section 4."""
    pts = [(seam("deep", 1) - 300, 0.62), (seam("deep", 1) + 200, 0.78), (seam("deep", 2) + 200, 0.9),
           (seam("deep", 3), 1.0), (seam("deep", 3) + 600, 1.12)]
    return np.interp(rows, [p[0] for p in pts], [p[1] for p in pts])


def far_hull(rgb, rows, x, cx, cr, L, wd, ang, k):
    """A far dead hull on the space before the shell: a dark sliver, its upper rim caught by the gold light ahead."""
    ca, sa = np.cos(ang), np.sin(ang)
    X, Y = x[None, :] - cx, rows[:, None] - cr
    u, v = X * ca + Y * sa, -X * sa + Y * ca
    prof = wd * np.sqrt(np.clip(1 - (u / L) ** 2, 0, 1)) * (0.75 + 0.25 * (u > 0))
    inside = smoothstep(0.7, -0.7, np.abs(v) - prof) * (np.abs(u) < L)
    col = np.array((30.0, 24.0, 36.0)) * k + GOLD_W * (np.exp(-((v - prof) / 1.1) ** 2) * 0.22 * k)[..., None]
    rgb[:] = rgb * (1 - inside[..., None]) + col * inside[..., None]


def shell_field():
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rows = (np.arange(n) + r0).astype(np.float64)
    R = rows[:, None]
    x = np.arange(W) + 0.5
    X = (x[None, :] - 240.0) / 240.0
    warp = (raster.fbm(W, n, 230, SEED, octaves=3, period=False) - 0.5) * 80
    warp2 = (raster.fbm(W, n, 80, SEED + 1, octaves=3, period=False) - 0.5) * 18
    tex = raster.fbm(W, n, 34, SEED + 2, octaves=4, period=False)
    patch = raster.fbm(W, n, 320, SEED + 8, octaves=3, period=False)             # where the core's light falls
    # the stone's grain: ridged noise, broken into patches (never a continuous line)
    grain = 1 - np.abs(2 * raster.fbm(W, n, 18, SEED + 9, octaves=3, period=False) - 1)
    grain = jovkit.blur(grain, 1.0) * smoothstep(0.35, 0.65, raster.fbm(W, n, 50, SEED + 10, octaves=2, period=False))
    # the main ribs: arcs on an uneven phase; their bow and tilt rock slowly along the shell (never a constant
    # spacing, never straight, never two alike)
    bow = BOW * (1 + 0.45 * np.sin(R / 760.0 + 0.7))
    tilt = 38 * np.sin(R / 1130.0 + 2.1)
    wob = (raster.fbm(W, n, 36, SEED + 11, octaves=2, period=False) - 0.5) * 9      # the edges meander (never flat)
    u = R + bow * X ** 2 + tilt * X + warp + wob
    phase = u / RIB_P + 0.34 * np.sin(u / 540.0 + 1.3) + 0.18 * np.sin(u / 233.0 + 0.4)
    main = _ridge(phase, 0.4, SEED)
    # the vault (section 3): the ribs bending into warped rings round its boss and six curved spokes of uneven width
    s2, s3 = seam("deep", 2), seam("deep", 3)
    rv = s2 + 0.55 * (s3 - s2)
    vx = 240.0 + 30 * np.sin(R / 300.0)
    dxv, dyv = x[None, :] - vx, (R - rv) * 1.05
    ang = np.arctan2(dyv, dxv)
    dist = np.hypot(dxv, dyv) * (1 + 0.13 * np.sin(3 * ang + 0.8) + 0.07 * np.sin(5 * ang + 2.0)) + warp * 0.9
    rings = _ridge(dist / 120.0 + 0.25 * np.sin(dist / 230.0 + ang), 0.34, SEED + 3) * smoothstep(420.0, 260.0, dist)
    spoke_ph = (ang * 6 / TAU) + dist / 700.0 + warp2 / 90.0 + 0.06 * np.sin(dist / 60.0)
    spokes = _ridge(spoke_ph, 0.21, SEED + 4) * smoothstep(40.0, 140.0, dist) * smoothstep(520.0, 300.0, dist)
    vault = np.maximum(np.maximum(rings, spokes * 1.05), 1.15 * np.clip(1 - (dist / 92.0) ** 2, 0, 1) ** 1.6)
    wv = np.exp(-((R - rv) / 430.0) ** 2) * np.ones_like(X)
    keep = smoothstep(0.0, 0.6, wv) * smoothstep(250.0, 420.0, dist)        # the arcs run on round the vault's edge
    hgt = np.maximum((1 - wv) * main + keep * main * wv, wv * vault) + 0.06 * grain + 0.08 * (tex - 0.5)
    # the rim (section 1): the shell begins at a ragged lip; space and the far hulls before it
    rim = RIM_ROW + 60 * np.sin(x / 97.0 + 0.4)[None, :] - 90 * X ** 2 + warp * 1.6
    shell = smoothstep(rim - 14, rim + 14, R)
    lip = np.exp(-((R - rim - 22) / 18.0) ** 2) * 0.9
    hgt = hgt * shell + lip
    gy = np.gradient(hgt, axis=0)                   # d/d row (up the screen)
    gx = np.gradient(hgt, axis=1)
    L = light_level(R) * (0.8 + 0.4 * patch)
    lam = np.clip(0.4 - gy * 36.0 + gx * 10.0, 0.0, 1.5)          # lit from the core ahead (up) and a little the left
    edge = np.clip(-gy * 34.0 - 0.3, 0, 1) ** 2 * smoothstep(0.3, 0.75, hgt)
    groove = smoothstep(0.3, 0.0, hgt)
    v = 0.06 + 0.42 * lam * L * (0.45 + 0.55 * np.clip(hgt, 0, 1)) + 0.3 * edge * L + 0.06 * (tex - 0.5)
    rgb = raster.ramp(LIT, np.clip(v, 0, 0.97)) + TEAL_K * (groove * 0.22)[..., None]
    rgb = rgb + GOLD_W * (0.06 * smoothstep(s3, hi + 200, R) * (0.5 + 0.5 * lam))[..., None]   # warmed toward the core
    # space before the rim: near-black violet, faint dust, sparse dim stars, the dead fleet's last hulls
    sp = np.zeros((n, W, 3)) + SPACE
    dust = raster.fbm(W, n, 150, SEED + 5, octaves=4, period=False)
    sp += (np.clip((dust - 0.42) * 1.8, 0, 1))[..., None] * (DUST - SPACE)
    rng = np.random.default_rng(SEED + 6)
    for _ in range(int(W * (RIM_ROW + 300 - r0) * 0.0009)):
        ix, iy = int(rng.uniform(0, W)), int(rng.uniform(0, RIM_ROW + 300 - r0))
        sp[iy, ix] += (30 + 150 * rng.uniform() ** 3) * np.array((0.9, 0.86, 1.0))
    for _ in range(9):
        far_hull(sp, rows, x, rng.uniform(-10, W + 10), rng.uniform(lo + 60, RIM_ROW - 120), rng.uniform(10, 30),
                 rng.uniform(2.5, 6.0), rng.uniform(-1.2, 1.2), rng.uniform(0.7, 1.1))
    sp += GOLD_W * (0.06 * smoothstep(RIM_ROW - 500, RIM_ROW, R))[..., None]       # the glow off the lip ahead
    rgb = sp * (1 - shell[..., None]) + rgb * shell[..., None]
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = shell_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== the great ribs and the far pieces

RIBS = {"rib-a": (620, 190, 40.0, 4761), "rib-b": (620, 210, 48.0, 4762), "rib-c": (580, 180, 34.0, 4763)}


def shade_band(hgt, ss, scale):
    """The far pieces' light (y down here): the slopes facing up (the core ahead) and a little the left lit, the gold
    edge on the steep lit flank; ``scale`` the band's thickness in px (the slopes normalised by it)."""
    gy, gx = np.gradient(hgt, axis=0) * ss * scale, np.gradient(hgt, axis=1) * ss * scale
    lam = np.clip(0.45 + gy * 1.1 + gx * 0.35, 0, 1.5)
    edge = np.clip(gy * 1.3 - 0.35, 0, 1) ** 1.5
    return lam, edge


def lit_stone(hgt, lam, edge, tex, L=1.0, top=0.8):
    """The far pieces' stone: kept a step under the Seraph's ivory (``top`` caps the ramp: the gold-white only as a
    thin edge, never a cream band behind a unit)."""
    v = 0.08 + 0.4 * lam * L * (0.45 + 0.55 * np.clip(hgt, 0, 1)) + 0.24 * edge * L + 0.06 * (tex - 0.5)
    return raster.ramp(LIT, np.clip(v, 0, top))


def great_rib(w, h, th0, seed, ss=2):
    """An arched band of the shell's stone crossing the screen (the third, nearest scale of rib), ragged and uneven along
    its length, its ends tapering out inside the canvas, lit gold-white along its upper flank, its shadow soft on the
    shell below it; a few broad segments across it (never evenly spaced)."""
    x, y = grid(w, h, ss)
    X = (x - w / 2) / (w / 2)
    n1 = raster.fbm(w * ss, h * ss, 60 * ss, seed, octaves=3, period=False) - 0.5
    rng = np.random.default_rng(seed)
    bow = rng.uniform(26, 40)
    yc = h * 0.36 + bow * X ** 2 + 10 * np.sin(x / 83.0 + rng.uniform(0, TAU)) + 8 * n1
    th = th0 * (0.8 + 0.35 * (raster.fbm(w * ss, 1, 90 * ss, seed + 1, octaves=2, period=False)[0][None, :] - 0.2)
                ) * smoothstep(1.0, 0.7, np.abs(X))
    v = (y - yc) / np.maximum(th, 1e-3)
    q = np.clip(np.abs(v), 0, 1)
    hgt = (1 - q * q) ** 0.75 * (th / th0)
    seg = _ridge((x + 9 * np.sin(y / 13.0)) / 52.0 + 0.4 * np.sin(x / 150.0), 0.46, seed + 2) * 0.06
    hgt = hgt + seg * (q < 1)
    body = smoothstep(1.0, 0.9, np.abs(v) + 0.06 * n1) * smoothstep(0.995, 0.88, np.abs(X))
    lam, edge = shade_band(hgt, ss, th0)
    tex = raster.fbm(w * ss, h * ss, 16 * ss, seed + 3, octaves=3, period=False)
    rgb = lit_stone(hgt, lam, edge, tex) + TEAL_K * (0.25 * smoothstep(0.5, 1.0, q))[..., None]
    # its shadow on the shell below (y down: under the band), soft, fading inside the canvas
    sh = np.exp(-(np.clip(y - yc - th * 0.6, 0, None) / (th * 0.7)) ** 2) * smoothstep(-0.2, 0.6, v) * 0.5
    sh = sh * smoothstep(1.0, 0.75, np.abs(X)) * smoothstep(h - 2, h - 30, y)
    a = np.clip(body + sh * (1 - body), 0, 1)
    rgb = (rgb * body[..., None] + np.array((4.0, 2.0, 8.0)) * (sh * (1 - body))[..., None]) / np.maximum(a, 1e-6)[..., None]
    rgb = (rgb * a[..., None]).reshape(h, ss, w, ss, 3).sum(axis=(1, 3))
    a = a.reshape(h, ss, w, ss).sum(axis=(1, 3))
    rgb = rgb / np.maximum(a, 1e-6)[..., None]
    return rgba(np.clip(rgb, 0, 255), a / (ss * ss))


CITADEL = (400, 170)


def citadel_far(ss=2):
    """Level 48's citadel far away: a dark teal-black mass of Vrell growth over the gold wreckage, knotted into a few
    bulging towers of different heights and girths (no straight edge, no two alike), its upper edge rimmed gold by the
    core's light behind it, a few gold glints of the wreckage inside; its foot fading into the light below."""
    w, h = CITADEL
    x, y = grid(w, h, ss)
    rng = np.random.default_rng(4768)
    n1 = raster.fbm(w * ss, h * ss, 14 * ss, 4769, octaves=3, period=False) - 0.5
    n2 = raster.fbm(w * ss, 1, 30 * ss, 4770, octaves=3, period=False)[0][None, :] - 0.5
    top = h * 0.62 - 22 * np.exp(-((x - w * 0.5) / 120.0) ** 2) + 14 * n2
    for cx, hh, wd in ((110, 58, 16), (168, 82, 22), (214, 52, 13), (262, 90, 26), (318, 62, 18), (78, 36, 12)):
        cx += rng.uniform(-8, 8)
        bulge = 1 + 0.3 * np.sin((y / hh) * 5 + cx)                     # knotted, swelling along its height
        prof = np.exp(-((x - cx) / (wd * bulge)) ** 2)
        top = np.minimum(top, h * 0.62 - hh * prof ** 0.6 - 4)
    top = top + 3 * n1
    side = smoothstep(10, 70, x) * smoothstep(w - 10, w - 70, x)
    body = smoothstep(top, top + 1.4, y) * smoothstep(0.0, 0.4, side + 0.15 * n1) * side
    fade = smoothstep(h - 2, h - 46, y)
    a = body * fade
    rimlit = np.exp(-((y - top) / 2.4) ** 2) * body
    rgb = np.zeros(x.shape + (3,)) + np.array((10.0, 22.0, 26.0)) * (0.8 + 0.6 * (n1[..., None] + 0.5))
    rgb = rgb + GOLD_W * (0.6 * rimlit)[..., None]
    for _ in range(16):
        gx_, gy_ = rng.uniform(60, w - 60), rng.uniform(h * 0.5, h - 30)
        rgb += GOLD_W * (np.exp(-((x - gx_) ** 2 + (y - gy_) ** 2) / 1.6) * 0.55)[..., None]
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    a = a.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return rgba(np.clip(rgb, 0, 255), a)


CORE_N = 720


def core_light():
    """The core's light: a smooth gold-white disc of light (additive), its own falloff fading to 0 inside its canvas."""
    x, y = grid(CORE_N, CORE_N)
    r = np.hypot(x - CORE_N / 2, y - CORE_N / 2)
    f = 0.5 * np.exp(-(r / 150.0) ** 2) + 0.38 * np.exp(-(r / 250.0) ** 2) + 0.12 * np.exp(-(r / 330.0) ** 1.6)
    f = f * smoothstep(CORE_N / 2 - 2, CORE_N / 2 - 70, r)
    light = CORE_W[None, None, :] * (0.5 * f)[..., None]
    return jovkit.additive_rgb(light)


# the core light's curve for E4: (t, the disc's centre y on the screen (down), its scale, its weight 0..1)
CORE_CURVE = [(118.0, -330.0, 1.0, 0.0), (130.0, -320.0, 1.0, 0.3), (175.0, -300.0, 1.0, 0.45),
              (200.0, -280.0, 1.05, 0.55), (230.0, -230.0, 1.15, 0.75), (266.0, -150.0, 1.3, 1.0)]


def core_at(t):
    ts = [c[0] for c in CORE_CURVE]
    return tuple(float(np.interp(t, ts, [c[i] for c in CORE_CURVE])) for i in (1, 2, 3))


RING_SIZES = {"light-ring-a": (132, 72, 52.0, 24.0), "light-ring-b": (92, 54, 36.0, 17.0)}


def light_ring(w, h, rx, ry, ss=4):
    """A Herald's ring of light as scenery: a tilted soft gold-white ring (straight alpha), still."""
    x, y = grid(w, h, ss)
    th = np.radians(-14)
    dx, dy = x - w / 2, y - h / 2
    u, v = dx * np.cos(th) + dy * np.sin(th), -dx * np.sin(th) + dy * np.cos(th)
    q = np.hypot(u / rx, v / ry)
    d = (q - 1) * min(rx, ry)
    core = np.exp(-(d / 1.0) ** 2)
    halo = np.exp(-(d / 4.0) ** 2) * 0.45
    near = np.clip(0.75 + 0.25 * np.sin(np.arctan2(v, u)), 0, 1)       # the near side a little brighter
    a = np.clip((core * 0.7 + halo) * near, 0, 1)
    rgb = np.zeros(x.shape + (3,)) + GOLD_W * 0.85 + CORE_W * 0.15 * core[..., None]
    a = edge_fade(a.reshape(h, ss, w, ss).mean(axis=(1, 3)), 3)
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    return rgba(np.clip(rgb, 0, 255), np.clip(a, 0, 1))


MOTES = {"motes-a": (200, 4771, 14), "motes-b": (200, 4772, 10)}


def motes(size, seed, count):
    x, y = grid(size, size)
    rng = np.random.default_rng(seed)
    a = np.zeros((size, size))
    for _ in range(count):
        cx, cy = rng.uniform(12, size - 12), rng.uniform(12, size - 12)
        r = rng.uniform(0.8, 1.6)
        a += np.exp(-((x - cx) ** 2 + (y - cy) ** 2) / (r * r)) * rng.uniform(0.25, 0.45)
        a += np.exp(-((x - cx) ** 2 + (y - cy) ** 2) / (16 * r * r)) * 0.06
    rgb = np.zeros((size, size, 3)) + CORE_W * 0.95
    return rgba(rgb, np.clip(edge_fade(a, 6), 0, 0.6))


HULKS_FAR = {"hulk-far-a": ("hulk-a_0", 0.5), "hulk-far-b": ("hulk-c_0", 0.55)}


def hulk_far(src, scale):
    """Level 46's scenery hulk, small and warmed for the gold light ahead, a step darker (it falls behind)."""
    im = Image.open(L46_OUT / f"{src}.png").convert("RGBA")
    im = im.resize((round(im.width * scale), round(im.height * scale)), Image.BOX)
    a = np.array(im).astype(np.float64)
    rgb = a[..., :3] * np.array((1.0, 0.9, 0.82)) * 0.72 + 4
    lum = rgb.mean(-1, keepdims=True)
    rgb = rgb + GOLD_W * np.clip((lum - 40) / 120, 0, 1) * 0.12
    al = np.where(a[..., 3] >= 128, 1.0, 0.0)
    return rgba(np.clip(rgb, 0, 255), al)


def build_sphere():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    for name, (w, h, th, seed) in RIBS.items():
        save(name, great_rib(w, h, th, seed))
    save("citadel-far", citadel_far())
    save("core-light", core_light())
    for name, (w, h, rx, ry) in RING_SIZES.items():
        save(name, light_ring(w, h, rx, ry))
    for name, (size, seed, count) in MOTES.items():
        save(name, motes(size, seed, count))
    for name, (src, sc) in HULKS_FAR.items():
        save(name, hulk_far(src, sc))
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


def pieces_spec():
    spec = {}
    for name in [*RIBS, *HULKS_FAR]:
        spec[name] = {"layer": "far", "size": list(Image.open(OUT / f"{name}.png").size)}
    spec["citadel-far"] = {"layer": "deep", "size": list(CITADEL)}
    spec["core-light"] = {"layer": "deep", "size": [CORE_N, CORE_N]}
    for name in [*RING_SIZES, *MOTES]:
        spec[name] = {"layer": "low-air", "size": list(Image.open(OUT / f"{name}.png").size)}
    return spec


# the far layer's great ribs and hulks: (time at mid-screen, x, piece); the low-air rings and motes: (t, x, piece, drift)
FAR_PLAN = [(4.0, 120, "hulk-far-a"), (17.0, 360, "hulk-far-b"), (31.0, 90, "hulk-far-a"),
            (52.0, 230, "rib-b"), (63.0, 260, "rib-a"), (76.0, 220, "rib-c"), (89.0, 250, "rib-b"),
            (103.0, 230, "rib-a"), (115.0, 250, "rib-c"),
            (128.0, 240, "rib-b"), (141.0, 230, "rib-a"), (158.0, 250, "rib-c"), (176.0, 250, "rib-c"),
            (190.0, 230, "rib-b"),
            (207.0, 240, "rib-a"), (219.0, 230, "rib-c"), (232.0, 250, "rib-b"), (245.0, 240, "rib-a")]
LOW_PLAN = [(6.0, 380, "motes-a", -2), (20.0, 120, "motes-b", 2), (36.0, 330, "motes-a", -1),
            (58.0, 90, "light-ring-b", 3), (70.0, 400, "motes-b", -2), (84.0, 380, "light-ring-a", -3),
            (98.0, 110, "motes-a", 2), (112.0, 70, "light-ring-b", 2),
            (126.0, 410, "light-ring-a", -2), (138.0, 70, "light-ring-b", 3), (150.0, 400, "light-ring-b", -3),
            (164.0, 80, "light-ring-a", 2), (182.0, 420, "light-ring-a", -2), (195.0, 70, "light-ring-b", 3),
            (210.0, 380, "motes-a", -2), (226.0, 100, "motes-b", 2), (240.0, 360, "motes-a", -1),
            (254.0, 140, "motes-b", 1)]
PEARL_T, PEARL_X = 170.0, 360.0                      # the pearl (E2's debris) enters at t=170, x 360
CITADEL_T, CITADEL_Y = 258.0, 62.0                   # the citadel's centre at y 62 at t=258 (in at the top by t~252)


def citadel_t():
    return CLOCK.t_centre_at(CITADEL_T, CITADEL_Y, FACTORS["deep"])


def placements():
    out = []
    fd, fl = FACTORS["far"], FACTORS["low-air"]
    for k, (t, x, name) in enumerate(FAR_PLAN):
        note = None
        if k == 0:
            note = "far: the dead fleet's last hulls falling behind (section 1)"
        elif name.startswith("rib") and FAR_PLAN[k - 1][2].startswith("hulk"):
            note = "far: the shell's great ribs crossing (sections 2-4; the vault's rings and spokes are the deep's)"
        out.append({"piece": name, "t": CLOCK.t_centre_at(t, 270.0, fd), "x": x, "note": note})
    for k, (t, x, name, vx) in enumerate(LOW_PLAN):
        p = {"piece": name, "t": CLOCK.t_centre_at(t, 270.0, fl), "x": x,
             "note": "low-air: motes of light; the Heralds' rings of light (scenery, still), drifting slowly"
             if k == 0 else None}
        if vx:
            p["path"] = [[round(t - 8, 3), round(-8 * vx, 1), 0], [round(t + 8, 3), round(8 * vx, 1), 0]]
        out.append(p)
    out.append({"piece": "citadel-far", "t": citadel_t(), "x": 240,
                "note": "section 4: Level 48's citadel, a dark mass over gold at the top by t~255 (E4: after core-light)"})
    return out


def fmt(v):
    return reef.fmt(v)


def proposal_text():
    src = "the level's data.yaml" if DATA and DATA.get("sections") else "the README's Layout table"
    lines = ["backdrop:",
             f"  # Presentation only: Level 47's inner sphere (tools/art/act7_sphere.py sphere, step ART-E, sections from {src}).",
             "  # Vacuum: no haze. No ground layer. Deep: one continuous field (space and the far hulls, the shell's rim, its",
             "  # ribs at two scales lit gold-white along their upper edges, the vault's rings and spokes in section 3) cut",
             "  # at the sections' seams (re-run the generator after moving a section). Far: the great ribs (the third",
             "  # scale); the dead fleet's last hulls in section 1. Low-air: motes of light, the Heralds'",
             "  # rings of light (still, drifting).",
             "  # E4 hooks: (1) `core-light` (additive, not placed) is the core's light in screen space (BackdropAtmosphere):",
             "  # its centre at x 240 and the curve's y (down; above the top edge), scaled and weighted by the curve,",
             "  # linear between its points, nothing before the first: [t, y, scale, weight]",
             "  #   " + fmt([list(c) for c in CORE_CURVE]),
             "  # drawn after the deep tiles and before the deep pieces, so `citadel-far` (dark over gold) lies on it;",
             "  # (2) the memory pearl is E2's debris chunk (`debris-memory-pearl`, 16 tumble frames, -break): E4 adds",
             "  # `debris-memory-pearl-glow` (additive, 6 fps loop) over it and `-release` (additive) with its break.",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # ground, high-air unused",
             "  ramp: 2",
             "  haze_colour: 06040b",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        h = Image.open(OUT / f"{name}.png").height
        lines.append(f"    {name}: {{layer: deep, height: {h}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    lines.append("  pieces:")
    for name, spec in pieces_spec().items():
        extra = "   # additive, not placed: E4's screen-space core light" if name == "core-light" else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]}}{extra}")
    lines.append("  placed:")
    for p in placements():
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        extra = f", path: {fmt(p['path'])}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['piece']}, t: {fmt(p['t'])}, x: {fmt(p['x'])}{extra}}}")
    lines.append("sections:   # ART-E's reading of the README's Layout (130/120/110/140 px/s, 2 s eases); E2 writes the real ones")
    for i, sec in enumerate(SECTIONS):
        sec = {k: v for k, v in sec.items() if k != "notes"} | {"tiles": [DEEP[i]]}
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k not in ("notes", "purpose")) + "}")
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


def tile_rows(name, offset):
    key = ("tile", name)
    if key not in _CACHE:
        _CACHE[key] = np.array(img_of(name))
    arr = _CACHE[key]
    h = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[h - 1 - (rows % h)], rows


def deep_layer(t):
    off = scroll(t) * FACTORS["deep"]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, name in enumerate(DEEP):
        arr, rows = tile_rows(name, off)
        sel = np.array([section_at_row("deep", r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def path_off(p, t):
    if not p.get("path"):
        return 0.0, 0.0
    pts = p["path"]
    if t <= pts[0][0]:
        return pts[0][1], pts[0][2]
    for a, b in zip(pts, pts[1:]):
        if t < b[0]:
            f = (t - a[0]) / (b[0] - a[0])
            return a[1] + (b[1] - a[1]) * f, a[2] + (b[2] - a[2]) * f
    return pts[-1][1], pts[-1][2]


def piece_xy(p, layer, t):
    f = FACTORS[layer]
    dx, dy = path_off(p, t)
    centre = scroll(p["t"]) * f + SCREEN / 2 + dy
    return p["x"] + dx, SCREEN - (centre - scroll(t) * f)


def core_on(img, t):
    y, s, k = core_at(t)
    if k <= 0:
        return img
    g = img_of("core-light")
    size = int(round(CORE_N * s))
    g = g.resize((size, size), Image.BILINEAR) if size != CORE_N else g
    return l23kit.add_c(img, g, 240, y, k)


def compose(t, units=True):
    spec = pieces_spec()
    placed = placements()
    img = deep_layer(t)
    img = core_on(img, t)
    for layer in ("deep", "far", "low-air"):
        for p in placed:
            name = p["piece"]
            if spec[name]["layer"] != layer:
                continue
            x, y = piece_xy(p, layer, t)
            fr = img_of(name)
            if y < -fr.height or y > SCREEN + fr.height:
                continue
            img = l23kit.paste_c(img, fr, x, y)
    if units:
        img = pearl_on(img, t)
        img = stand_ins(img, t)
    return img


def pearl_xy(t):
    """The review's stand-in for E2's debris: in at the top edge at PEARL_T, drifting down the right third."""
    dt = t - PEARL_T
    return PEARL_X - 4 * dt, -18 + 55 * dt


def pearl_on(img, t, broken_at=None):
    try:
        x, y = pearl_xy(t)
        if broken_at is not None and t >= broken_at:
            f = int((t - broken_at) * 15)
            x, y = pearl_xy(broken_at)
            if f < 8:
                img = l23kit.paste_c(img, load("debris-memory-pearl-break")[f], x, y)
                img = l23kit.add_c(img, load("debris-memory-pearl-release")[f], x, y)
            return img
        if -30 < y < SCREEN + 30:
            img = l23kit.paste_c(img, load("debris-memory-pearl")[int(t * 4) % CHUNK_N], x, y)
            img = l23kit.add_c(img, load("debris-memory-pearl-glow")[int(t * 6) % 8], x, y)
    except FileNotFoundError:
        pass
    return img


def seraph_on(img, x, y, frame=None, glow=True):
    try:
        se = load("choir-seraph") if frame is None else frame
        fr = se[0] if isinstance(se, list) else se
        img = l23kit.paste_c(img, fr, x, y)
        if glow:
            img = l23kit.add_c(img, load("choir-seraph-glow")[0], x, y)
    except FileNotFoundError:
        pass
    return img


PAIRS = [(40, 1), (66, 2), (92, 2), (134, 2), (160, 2), (186, 2), (212, 2), (238, 2)]


def stand_ins(img, t):
    """A pair mirroring the ship (the review's stand-in for the waves: E2 sets the real ones), its shots, the ship."""
    try:
        ship = load("ship")
        bank = load("choir-seraph-bank")
        shot = load("choir-seraph-shot")
    except FileNotFoundError:
        return img
    sx = 240 + 120 * np.sin(t * 0.7)
    vx = 84 * np.cos(t * 0.7)
    for t0, n in PAIRS:
        dt = t - t0
        if not 0 <= dt < 14:
            continue
        mx = W - sx
        for i in range(n):
            y = 150 + 95 * i
            k = int(np.clip(3 - vx / 40, 0, 6))
            for j in range(10):
                a = j * 0.63 + dt * 2.1 + i * np.pi / 2
                r = 22 + 26 * ((dt * 1.3 + j * 0.37) % 2.4)
                img = l23kit.paste_c(img, shot[j % 4], mx + np.cos(a) * r, y + 6 + np.sin(a) * r)
            img = seraph_on(img, mx, y, bank[k])
    return l23kit.paste_c(img, ship[2] if len(ship) > 2 else ship[0], sx, 470)


# =========================================================================== reviews

def sheet_of(title, rows, width=1000):
    hgt = 40 + sum(r.height + 30 for _, r in rows)
    sheet = raster.sheet(width, hgt, title, f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for text, r in rows:
        label(sheet, 12, y, text)
        if r.width > width - 24:
            r = r.resize((width - 24, int(r.height * (width - 24) / r.width)), Image.NEAREST)
        sheet.alpha_composite(r, (12, y + 12))
        y += r.height + 30
    return sheet.crop((0, 0, width, y))


def bg_cell(w, h, t=150.0, x0=0, y0=0):
    try:
        return compose(t, units=False).crop((x0, y0, x0 + w, y0 + h))
    except FileNotFoundError:
        return Image.new("RGBA", (w, h), (20, 12, 30, 255))


def strip(frames, cell, scale=2, add=None, under=None, step=6, t=150.0):
    out = Image.new("RGBA", (len(frames) * (cell[0] * scale + step), cell[1] * scale), (0, 0, 0, 0))
    base = bg_cell(*cell, t=t)
    for i, fr in enumerate(frames):
        c = base.copy()
        if under is not None:
            c = l23kit.paste_c(c, under[i] if isinstance(under, list) else under, cell[0] / 2, cell[1] / 2)
        if fr is not None:
            c = l23kit.paste_c(c, fr, cell[0] / 2, cell[1] / 2)
        if add is not None:
            c = l23kit.add_c(c, add[i] if isinstance(add, list) else add, cell[0] / 2, cell[1] / 2)
        out.alpha_composite(c.resize((cell[0] * scale, cell[1] * scale), Image.NEAREST),
                            (i * (cell[0] * scale + step), 0))
    return out


def band_windows(t, size=120):
    """The brightest and the darkest window of the screen at t (the backdrop only, the stand-ins left out)."""
    img = np.array(compose(t, units=False)).astype(np.float64)
    lum = img[..., :3] @ np.array((0.299, 0.587, 0.114))
    best = worst = None
    for y in range(0, SCREEN - size, 20):
        for x in range(0, W - size, 20):
            m = lum[y:y + size, x:x + size].mean()
            if best is None or m > best[0]:
                best = (m, x, y)
            if worst is None or m < worst[0]:
                worst = (m, x, y)
    return best, worst


def readability():
    """The Seraph over the level's brightest and darkest band at 1:1: the ring, a shot, the leaving flare, the ship."""
    cells = []
    for t in (262.0, 160.0, 90.0):
        (bm, bx, by), (dm, dx, dy) = band_windows(t)
        for (m, x0, y0), name in (((bm, bx, by), "BRIGHTEST"), ((dm, dx, dy), "DARKEST")):
            c = bg_cell(120, 120, t, x0, y0)
            c = seraph_on(c, 46, 50)
            try:
                c = l23kit.add_c(c, load("teleport-ring-large")[3], 96, 24, 1.0)
                c = l23kit.paste_c(c, load("choir-seraph-shot")[0], 100, 96)
                c = l23kit.paste_c(c, load("choir-seraph-shot")[2], 84, 104)
                c = l23kit.paste_c(c, load("ship")[2], 98, 100) if name == "DARKEST" else c
            except FileNotFoundError:
                pass
            cells.append((f"T={t:g} {name} (MEAN {m:.0f})", c))
    row = Image.new("RGBA", (len(cells) * 128, 120), (0, 0, 0, 0))
    for i, (_, c) in enumerate(cells):
        row.alpha_composite(c, (i * 128, 0))
    return row, [n for n, _ in cells]


def review_seraph():
    gl, bank, fold = load("choir-seraph"), load("choir-seraph-bank"), load("choir-seraph-fold")
    leave, lg = load("choir-seraph-leave"), load("choir-seraph-leave-glow")
    glow, flare = load("choir-seraph-glow")[0], load("choir-seraph-flare")
    ta, de, shot = load("choir-seraph-tatters"), load("choir-seraph-death"), load("choir-seraph-shot")
    rows = [("GLIDING (4 FRAMES) WITH ITS GLOW, 2X", strip([g for g in gl], (GL, GL), 2, add=glow)),
            ("BANKED: 30/20/10 LEFT, LEVEL, 10/20/30 RIGHT (MIRRORING), 1X", strip(bank, (GL, GL), 1, add=glow)),
            ("THE SPIRAL'S TELL: THE VIOLET FLARE AT THE WING ROOTS (6 FRAMES, 0.4 S), 1X",
             strip([gl[1]] * FLARE_N, (GL, GL), 1, add=flare)),
            ("THE FOLD (6 FRAMES, 0.3 S; THE LAST HELD THROUGH THE FADE), 1X", strip(fold, (GL, GL), 1)),
            ("THE LEAVING TELL (12 FRAMES, 0.8 S, EVERY 2ND) WITH ITS WHITE-GOLD FLARE, 1X",
             strip(leave[::2], (GL, GL), 1, add=lg[::2])),
            ("DEATH: TATTERS + FEATHERS OF LIGHT (16 FRAMES, EVERY 2ND), 1X",
             strip(ta[::2], (128, 128), 1, add=de[::2])),
            ("THE SPIRAL'S SHOT (4 FRAMES) AND THE ARRIVAL RING (BUILT, TELEPORT-RING-LARGE), 3X",
             strip(shot, (SHOT + 9, SHOT + 9), 3) if shot else Image.new("RGBA", (10, 10)))]
    try:
        portrait = Image.open(ROOT / "assets" / "sprites" / "intel" / "choir-seraph.png").convert("RGBA")
        rows.append(("INTEL PORTRAIT 30X30, 1X AND 3X", strip([portrait, portrait.resize((90, 90), Image.NEAREST)],
                                                              (96, 96), 1)))
    except FileNotFoundError:
        pass
    rd, names = readability()
    rows.append(("READABILITY 1:1 (BRIGHTEST / DARKEST WINDOW AT T=262, 160, 90): " + ", ".join(
        n.split(" (")[0] for n in names[:2]) + "; RING, SHOTS, SHIP", rd))
    sheet = sheet_of("CHOIR SERAPH (FINAL R92): A BLADES, MIRRORING, FOLDING, LEAVING", rows)
    frames = []
    ring = load("teleport-ring-large")
    for i in range(110):
        t = 150.0 + i / 10.0
        img = compose(t, units=False)
        u = i / 10.0
        sx = 240 + 110 * np.sin(u * 0.9)
        vx = 99 * np.cos(u * 0.9)
        mx = W - sx
        k = int(np.clip(3 - vx / 40, 0, 6))
        for j, y in enumerate((160, 255)):
            if u < 1.0:
                img = l23kit.add_c(img, ring[min(9, int(u * 10))], mx, y)
            elif u < 3.5:
                img = seraph_on(img, mx, y, bank[k])
                if u < 1.4:
                    img = l23kit.add_c(img, flare[min(FLARE_N - 1, int((u - 1.0) / 0.4 * FLARE_N))], mx, y)
                for s in range(int((u - 1.3) * 6)):
                    a = s * 0.5 + j * np.pi / 2 + np.pi * (s % 2)
                    r = 20 + 120 * (u - 1.3 - s / 6)
                    if r < 260:
                        img = l23kit.paste_c(img, shot[s % 4], mx + np.cos(a) * r, y + np.sin(a) * r)
            elif u < 3.8 and j == 0:
                img = seraph_on(img, mx, y, fold[min(5, int((u - 3.5) / 0.3 * 6))])
            elif u < 3.8:
                f = int((u - 3.5) / 0.3 * 16)
                img = l23kit.paste_c(img, ta[f], mx, y)
                img = l23kit.add_c(img, de[f], mx, y)
            elif j == 1 and u < 4.6:
                f = min(15, 5 + int((u - 3.8) / 0.8 * 11))
                img = l23kit.paste_c(img, ta[f], mx, y)
                img = l23kit.add_c(img, de[f], mx, y)
            elif j == 0 and 5.0 <= u < 5.5:
                img = l23kit.add_c(img, ring[min(9, int((u - 5.0) * 20))], mx, 140)
            elif j == 0 and 5.5 <= u < 8.2:
                img = seraph_on(img, mx, 140, bank[k])
            elif j == 0 and 8.2 <= u < 9.0:
                f = min(LEAVE_N - 1, int((u - 8.2) / 0.8 * LEAVE_N))
                img = seraph_on(img, mx, 140 - 50 * (u - 8.2), leave[f], glow=False)
                img = l23kit.add_c(img, lg[f], mx, 140 - 50 * (u - 8.2))
        img = l23kit.paste_c(img, load("ship")[2], sx, 470)
        frames.append(img)
    artkit.save_review(sheet, frames, SERAPH_C, "choir-seraph", fps=10)


SCREENS = (8.0, 30.0, 62.0, 100.0, 140.0, 160.0, 230.0, 262.0)


def review_sphere():
    shots = [compose(t) for t in SCREENS]
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * 290 + 30 + 330, "LEVEL 47 BACKDROP (FINAL R92): THE INNER SPHERE",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, (t, s) in enumerate(zip(SCREENS, shots)):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        sec = next((q["name"] for q in SECTIONS if t < float(q["end"])), SECTIONS[-1]["name"])
        raster.draw_text(sheet, x, y, f"T={t:g} S ({sec.upper()})", raster.LABEL)
        sheet.alpha_composite(s.resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + 2 * 290 + 6
    raster.draw_text(sheet, 12, y, "SCREENS AT HALF SIZE WITH STAND-INS (A SERAPH PAIR, ITS SHOTS, THE SHIP); THE CORE "
                                   "LIGHT BY ITS CURVE (E4'S CODE). PIECES AT A QUARTER/HALF:", raster.LABEL)
    x = 12
    for name in ("rib-a", "rib-b", "rib-c", "citadel-far", "core-light", "hulk-far-a", "hulk-far-b",
                 "light-ring-a", "light-ring-b"):
        im = img_of(name)
        bg = Image.new("RGBA", im.size, (14, 8, 22, 255))
        if name == "core-light":
            bg = l23kit.add_c(bg, im, im.width / 2, im.height / 2)
        else:
            bg.alpha_composite(im)
        sc = 4 if max(im.size) > 300 else 2
        sm = bg.resize((max(1, im.width // sc), max(1, im.height // sc)), Image.LANCZOS)
        if x + sm.width > w - 12:
            x = 12
            y += 150
        sheet.alpha_composite(sm, (x, y + 14))
        x += sm.width + 8
    frames = [compose(t).resize((240, 270), Image.LANCZOS) for t in np.arange(0, END, 3.0)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=4)


def review_props():
    ch, gl = load("debris-memory-pearl"), load("debris-memory-pearl-glow")
    rows = [("MEMORY PEARL 36X36: 16 TUMBLE FRAMES (EVERY 2ND), 3X", strip(ch[::2], (40, 40), 3, t=170.0)),
            ("ITS FLICKER: 8 GLOW FRAMES OVER ONE TUMBLE FRAME, 3X",
             strip([ch[0]] * 8, (40, 40), 3, add=gl, t=170.0)),
            ("THE BREAK (8 FRAMES) WITH THE RELEASE OF ITS LIGHT, 2X",
             strip(load("debris-memory-pearl-break"), (64, 64), 2, add=load("debris-memory-pearl-release"), t=170.0))]
    sheet = sheet_of("LEVEL 47 PROPS (FINAL R92): THE MEMORY PEARL", rows)
    frames = []
    for i in range(70):
        t = PEARL_T + i * 0.1
        img = compose(t, units=False)
        img = pearl_on(img, t, broken_at=PEARL_T + 5.0)
        img = l23kit.paste_c(img, load("ship")[2], 340 + 10 * np.sin(i / 9), 470)
        frames.append(img)
    artkit.save_review(sheet, frames, CONCEPT, "l47-props", fps=10)


# =========================================================================== check

SETS = {"seraph": [("choir-seraph", 4, (SE, SE)), ("choir-seraph-bank", 7, (SE, SE)), ("choir-seraph-fold", FOLD_N, (SE, SE)),
                   ("choir-seraph-leave", LEAVE_N, (SE, SE)), ("choir-seraph-glow", 1, (GL, GL)),
                   ("choir-seraph-flare", FLARE_N, (GL, GL)), ("choir-seraph-leave-glow", LEAVE_N, (GL, GL)),
                   ("choir-seraph-tatters", 16, (128, 128)), ("choir-seraph-death", 16, (128, 128)),
                   ("choir-seraph-shot", 4, (SHOT, SHOT))],
        "pearl": [("debris-memory-pearl", CHUNK_N, (CHUNK, CHUNK)), ("debris-memory-pearl-glow", 8, (40, 40)),
                  ("debris-memory-pearl-break", 8, (64, 64)), ("debris-memory-pearl-release", 8, (64, 64))]}
LINE_RUN = 120                                       # a straight run this long or longer fails the scan
LINE_STEP = 9.0                                      # a lum step across one row (or column) that counts as an edge


def straight_runs(img):
    """The longest straight row and column edge of a backdrop file (FIXC-SPINE's scan): the longest run of pixels along
    one row (column) whose step to the next row (column) is a same-signed jump of at least LINE_STEP (premultiplied
    luminance) that is **hard**, at least 1.8x the steps either side of it (a ruled line or a cut, not the soft flank
    of a curved rib, whose step spreads over several rows). Returns (rows, columns, the soft runs' longest)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    lum = (a[..., :3] @ np.array((0.299, 0.587, 0.114))) * a[..., 3] / 255

    def longest(d, hard):
        best = 0
        nb = np.maximum(np.abs(np.roll(d, 1, axis=0)), np.abs(np.roll(d, -1, axis=0)))
        for sign in (1, -1):
            m = (d * sign) >= LINE_STEP
            if hard:
                m &= np.abs(d) >= 1.8 * nb
            for row in m:
                if not row.any():
                    continue
                padded = np.concatenate(([0], row.astype(np.int8), [0]))
                edges = np.flatnonzero(np.diff(padded))
                best = max(best, int((edges[1::2] - edges[::2]).max()))
        return best
    dr, dc = np.diff(lum, axis=0), np.diff(lum, axis=1).T
    return longest(dr, True), longest(dc, True), max(longest(dr, False), longest(dc, False))


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
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} frames "
                  f"{frames[0].size if frames else None}, checker {share * 100:.1f} %{f', {lit} lit border px' if lit else ''}")
    spec = pieces_spec() if OUT.exists() else {}
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        v, c = checker_share(im)
        share = c / max(v, 1)
        ok, msg = im.text.get(artkit.SOURCE_KEY) == SOURCE and share < 0.05, []
        stem = p.stem
        if stem in DEEP:
            ws = wrap_score(np.array(im.convert("RGBA")))
            ok &= ws <= 1.5 and np.array(im)[..., 3].min() == 255
            msg.append(f"wrap {ws:.2f}")
        else:
            border = np.concatenate([np.array(im)[0, :, 3], np.array(im)[-1, :, 3], np.array(im)[:, 0, 3],
                                     np.array(im)[:, -1, 3]]).max() if stem != "core-light" else lit_border(im)
            ok &= border == 0
            msg.append(f"edge {border}")
        rows, cols, soft = straight_runs(im)
        ok &= rows < LINE_RUN and cols < LINE_RUN and soft < LINE_RUN
        msg.append(f"hard straight runs {rows}/{cols} px (soft {soft})")
        if stem in spec:
            ok &= list(im.size) == spec[stem]["size"]
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {p.name}: {im.size}, checker {share * 100:.1f} %, {', '.join(msg)}")
    return bad == 0


# =========================================================================== jobs and main

JOBS = {"seraph": _seraph}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "act7-sphere"
# at most 4 render workers here (ART-F renders beside this step; run it under ``nice -n 19``); ART_WORKERS overrides
WORKERS = max(1, min(int(os.environ.get("ART_WORKERS", "4")), os.cpu_count() or 1))


def _model_hash(kind):
    text = Path(__file__).read_text(encoding="utf-8")
    rule = "# =========================================================================== "
    a, b = text.index(rule + "the Choir Seraph"), text.index(rule + "the memory pearl")
    return hashlib.sha1((text[a:b] + repr(u84.seraph_model.__code__.co_code)).encode()).hexdigest()[:12]


def run(jobs):
    """The renders, cached in the system temp dir by the model code's hash and the job."""
    CACHE.mkdir(parents=True, exist_ok=True)
    paths = [CACHE / f"{hashlib.sha1((_model_hash(j[0]) + repr(j)).encode()).hexdigest()[:20]}.png" for j in jobs]
    todo = [i for i, path in enumerate(paths) if not path.exists()]
    if todo:
        with ProcessPoolExecutor(max_workers=WORKERS) as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(path).convert("RGBA") for path in paths]


PARTS = ("seraph", "pearl", "sphere")

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    parts = [a for a in args if a in PARTS] or list(PARTS)
    if "--review" not in args:
        if "seraph" in parts:
            build_seraph(run(seraph_jobs()))
        if "pearl" in parts:
            build_pearl()
        if "sphere" in parts:
            build_sphere()
            write_proposal()
    if "sphere" in parts:
        review_sphere()
    if "seraph" in parts:
        review_seraph()
    if "pearl" in parts:
        review_props()
