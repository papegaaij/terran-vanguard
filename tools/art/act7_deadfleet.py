#!/usr/bin/env python3
"""Production art: Level 46 "Graveyard of the Choir" (design/campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir;
M11 part D batch, concept round 91, step ART-D): the dead-fleet backdrop kit, the Husk (round 84 A.3 a "punctured"),
the seed casket, the warm hull with its living core and four sinews, the Helix wreck and its hatch, Vorne's courier and
the dying Leviathan's grade.

Everything is in round 84 Q10 b's **one Vrell palette** with Level 46's light (design/world/vrell-space, *Light per
level*): one cold white-violet key light from the top left on the dead fleet's hulls (their own dull grey-brown), the
world beyond in the palette's darkest violet, pale crystal at the hulls' cracks, no light inside them. The scenery hulks
are paler and flatter than the Husks (``SCENERY_GRADE`` vs ``HUSK_GRADE``), so the living threat reads a step darker
and harder. The Husk is round 84's model (tools/concept/enemies_r84.py ``husk_model("a", shape, crack)``, imported
unchanged) on the production path (4x, 1-bit alpha, one palette per hull shape); the hulks reuse its plates and bent-lip
helpers. Every glow and haze is a float field written unquantised (far more than 32 steps, no ordered dither:
DitherGuardTest's measure) and fades to 0 inside its canvas (``--check`` fails on a lit border pixel of an additive
set). The motion budget is very low: the hulks rock slowly, no glow pulses but the warm core's (and the Leviathan's
faint blowhole).

Sprites (assets/sprites/ once the data claims the root, else build/art-held/sprites/: belt_units.py's rule; ``additive``
= premultiplied on black, GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha; steps = game steps of 1/60 s):

  The Husk (design/enemies/space/husk; ``radial``, 32 headings: frame k turned k x 11.25 degrees clockwise from its
  nose pointing down the screen; the hull 88x72 inside a 104x104 canvas, centred on the unit)
  husk_0..31, husk-b_0..31, husk-c_0..31      104x104 normal: the three hull shapes whole (1 hole; 2; 3), dull ash
                              grey-brown plates punched open from inside, pale crystal along the cracks (faint)
  husk-cracked_0..31, husk-b-cracked_0..31, husk-c-cracked_0..31   104x104 normal: the same hulls cracked (at 50 %
                              HP: the holes torn wider, the crystal split longer and lit)
  husk-flare_0..7             112x112 additive: the crack's 0.5 s tell (4 steps a frame; hard 0.4 s: 3), a pale cold
                              flare swelling round the hull with crystal glints, peaking at frame 3, gone by frame 7;
                              drawn over the cracked frame (the tell shows the cracked hull)
  husk-slabs_0..15            128x128 normal: the ``medium`` collapse (EnemyLooks' ``-slabs``, 4 steps a frame): the
                              hull folds in and breaks into nine slow grey slabs turning under the fixed light and
                              pale crystal splinters flung farther, all shrinking away
  husk-death_0..15            128x128 additive: the splinters' cold glints and a faint cold haze, decaying from the
                              first frame (never a flash); with ``-slabs``
  husk-shard_0..15            23x23 normal: the crack fan's shard (the fast class, ``small``): a pale ice crystal
                              shard with a white core and a dark outline, 16 headings (k x 22.5 degrees clockwise from
                              straight down, needle's order), the point leading
  husk-orb_0..3               15x15 normal: the death ring's slow orb: a pale crystal ring on a dark rim, its white
                              core pulsing over the 4 frames (BulletLooks' small orb timing)
  The seed casket (design/enemies/space/seed-casket, step D2's unit; fixed, drifting)
  seed-casket                 40x28 normal (single): a chitin capsule in the dead hulls' dull grey-brown, its long seam
                              a faint teal line (the one thing in the wreckage worth taking)
  seed-casket-broken          40x28 normal (single): split along the seam, the halves parted, the hollow empty
  seed-casket-tatters_0..7    64x64 normal (``-tatters``, 4 steps a frame): the halves drifting apart and shrinking,
                              chips flung out
  seed-casket-death_0..7      64x64 additive: a faint teal breath out of the seam, decaying from the first frame
  The warm hull's core (Level 46's story pickup ``vrell-core``, StoryLooks' frame conventions) and its sinews
  vrell-core_0..7             128x104 normal: the living core (48 px, violet membrane over gold veins) in its four
                              sinews to the knots at ``SINEWS`` px from its centre (step D2 places the four sinew
                              objects there), breathing (8 frames, 0.5 s each: one breath in 4 s); its warm light is
                              the warm hull's ``-glow`` piece while it sits in the hull
  vrell-core-rise_0..9        88x88 normal: the sinews cut: the core lifting out of the ribs over its 2.0 s rise, seen
                              growing from 1.0 to 1.2, the cut sinew ends hanging
  vrell-core-drift_0..7       88x88 normal: loose at 1.2, breathing (8 fps)
  vrell-core-glow_0..15       128x128 additive: its violet-gold halo: 0..9 the rise (growing), 10..15 the drift's slow
                              pulse (6 fps loop); the only warm light in the level
  warm-hull-sinew_0..2        28x28 normal: a sinew knot where the sinew grips a rib: whole, hit (gashed, gold
                              ichor), cut (a severed stump)
  warm-hull-sinew-break_0..7  48x48 normal: the sinew torn through: tissue shreds and ichor flecks flung out
  The Helix wreck's hatch (secret 1: a 20 HP trigger on the ``helix-wreck`` piece at ``HATCH`` from its centre)
  helix-hatch_0..2            24x24 normal: the survey craft's hatch with the gold Helix hex: shut, dented, blown open
  helix-hatch-break_0..7      48x48 normal: the hatch blown out: panel chips and gold flakes, a brief hot core
  Vorne's courier (secret 2: a debris chunk with HP, Level 43's Helix pod's kind, or a trigger's look by frame 0)
  debris-vorne-courier_0..15  40x40 normal: a gold Ascendancy courier drone, gloss gold with black panel lines, its
                              drive scorched and dead, tumbling (16 turns, rocking)
  debris-vorne-courier-break_0..7  64x64 normal: shot apart: gold fragments, a brief hot core, no smoke (vacuum)
  The dying Leviathan (design/enemies/space/leviathan, *Act 7: the dying Leviathan*; no new drawing)
  dying-leviathan-*           a graded copy of every ``leviathan-*`` frame (``-down``, ``-cross``, the vents, fins and
                              fluke whole and wrecked, ``-blowhole-glow``, ``-chunk-1..5``, ``-ichor``) at their sizes:
                              desaturated toward Level 46's grey-brown, darkened a step, its violet glows dimmed to a
                              third; pivots/dying-leviathan.json is leviathan.json with the chunks and the ichor
                              renamed (step D4), so SetPieceLooks and SetPieceDeath work unchanged with the slug
                              ``dying-leviathan``
  dying-leviathan-cross-glow  558x428 additive (the ``-cross`` canvas): the blowhole's faint violet glow on the first
                              pass's frames, for the game's slow pulse (k(t) <= 0.25 Hz, 0..1; the frames' own baked
                              glow is dimmed)

Backdrop (assets/backdrop/level-46/; rows y-up as the game reads them; ``--proposal`` writes the ``backdrop:`` block, the
sections and the warm hull's, the sinews' and the hatch's places to concept/backdrop-proposal.yaml: step D2 places
them, step D4 wires the hooks):
  deep-1 .. deep-5     ``deep`` 0.15, opaque: near-black violet space, sparse stars, the faintest violet dust and the
                       far dead fleet small and dark on it (densest in section 3), cut from one continuous field at the
                       sections' seams; the starless patch is code-drawn by the game (D4), no art
  world-far            ``deep`` 96x96: the withered grey world small and far, section 1
  world-near           ``deep`` 640x480: the same world filling the lower sky in section 5, its pale cracked surface;
                       on a path that slows it to ~5 px/s, its limb at y~250 by the end
  hulk-a .. hulk-d     ``low-air`` 1.3, 16 frames at 2.5 fps: the hollowed hulks (a long spindle, a broad hull, a hull
                       broken in two: its fore and aft halves), plates bent outward round the holes, crystal at the
                       cracks, no light inside; rocking slowly (9 turns of 1.75 degrees played forth and back)
  warm-hull            ``low-air`` 280x330: the dead capital hull, not hollowed: scarred by weapons, its rib cage open,
                       its prow to the top left; on a path that holds it to the ground's 90 px/s (the sinews' pinned
                       scroll); the core and the sinews are sprites (above) at ``CORE`` from its centre
  warm-hull-glow       ``low-air`` 280x330, 8 frames at 2 fps, additive: the core's warm light on the ribs, pulsing
                       (D4 fades it out as the core rises)
  helix-wreck          ``low-air`` 200x160: a Vrell hulk's breach with the Helix survey craft wedged in it, its lights
                       long dead; the hatch trigger at ``HATCH``

Reviews (round 91, ``*-final-r91-a``): design/enemies/space/husk/concept/husk; the level's concept/ backdrop and
l46-props; design/enemies/space/leviathan/concept/leviathan-dying. The intel picture (30x30) is tools/art/intel.py's,
from ``husk_intel_hi`` here; the two briefing images are tools/art/briefing_images.py's.
Run: nice -n 19 python3 tools/art/act7_deadfleet.py [husk] [casket] [warm] [wrecks] [leviathan] [fleet]
     [--review | --proposal | --check]   (no part = all, a few minutes; --review only rebuilds the sheets)
"""
import hashlib
import json
import os
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
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
from act7_reef import add, blur3, chips_frames, grid, label, lit_border, shaded_dot, to_img

u84 = reef.u84
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
from render import enemy_models as em  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_models import hx  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "act7_deadfleet.py"
BATCH = "M11 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r91"
artkit.REVIEW_ROUND = ROUND
L46 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-46-graveyard-of-the-choir"
CONCEPT = L46 / "concept"
HUSK_C = DESIGN / "enemies" / "space" / "husk" / "concept"
LEV_C = DESIGN / "enemies" / "space" / "leviathan" / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-46"
HELD = ROOT / "build" / "art-held"
W, SCREEN, SPEED = 480, 540, 90.0

# Level 46's light: one cold white-violet key from the top left (the renderer's key is warm white: graded after)
COLD = np.array((0.90, 0.92, 1.06))
SPACE = np.array((6.0, 5.0, 10.0))
DUST = np.array((24.0, 17.0, 34.0))                 # the palette's darkest violet
WORLD_K = np.array((70.0, 62.0, 84.0))              # the withered world, lit
CRYSTAL = np.array((184.0, 200.0, 212.0))           # #b8c8d4, the Husk's crystal
ICE = np.array((200.0, 222.0, 250.0))
GOLD_W = np.array((255.0, 196.0, 96.0))             # the warm core's gold
VIOLET_W = np.array((176.0, 96.0, 236.0))           # and its violet
TEAL_S = np.array((70.0, 170.0, 160.0))             # the casket's seam
HULL_RGB = np.array((110.0, 100.0, 92.0))           # #6e645c

ADDITIVE = {"husk-flare", "husk-death", "seed-casket-death", "vrell-core-glow", "dying-leviathan-blowhole-glow",
            "dying-leviathan-ichor", "dying-leviathan-cross-glow"}


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def load(name):
    return l23kit.load(name)


def turned_by(scene, mats, deg):
    """A +Y-forward model turned ``deg`` clockwise from facing down the screen (belt_units.turned's sense)."""
    rot = model_rotation(np.pi / 2 + np.radians(deg))
    return (lambda p, s=scene: s(rotate_z(p, rot))), model_space_materials(mats, rot)


def render_soft(scene, mats, size, extent, factor=2):
    """A backdrop piece: rendered at ``factor`` x and box-filtered, alpha kept soft (an antialiased edge)."""
    w, h = size
    hi = sdf.render(scene, mats, (w * factor, h * factor), extent)
    a = hi.reshape(h, factor, w, factor, 4)
    alpha = a[..., 3].mean(axis=(1, 3))
    rgb = (a[..., :3] * a[..., 3:4]).sum(axis=(1, 3)) / np.maximum(a[..., 3].sum(axis=(1, 3)), 1e-6)[..., None]
    return np.clip(rgb * 255, 0, 255), np.clip(alpha, 0, 1)


def grade(img, cold=True, contrast=1.0, pivot=60.0, lift=0.0, flat=0.0):
    """Level 46's grade of a lit render (straight alpha): the cold key, a contrast round ``pivot``, a lift and a
    flattening toward the frame's mean (the scenery's paler, flatter hulks)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3]
    op = a[..., 3] > 0
    if cold:
        rgb = rgb * COLD
    if flat and op.any():
        mean = rgb[op].mean(axis=0)
        rgb = rgb * (1 - flat) + mean * flat
    rgb = (rgb - pivot) * contrast + pivot + lift
    a[..., :3] = np.clip(rgb, 0, 255) * op[..., None]
    return Image.fromarray(np.round(a).astype(np.uint8), "RGBA")


HUSK_GRADE = dict(contrast=1.18, pivot=70.0, lift=-4.0)
SCENERY_GRADE = dict(contrast=0.72, pivot=40.0, lift=14.0, flat=0.1)


def light_img(light, margin=4):
    return jovkit.additive_rgb(reef.edge_fade(light, margin))


def glint(light, x, y, r, col, k):
    """A small four-point crystal glint (additive) into ``light``."""
    h, w = light.shape[:2]
    xx, yy = grid(w, h)
    dx, dy = np.abs(xx - x), np.abs(yy - y)
    core = np.exp(-(dx * dx + dy * dy) / (0.6 * r * r))
    arms = np.exp(-dy * dy / 0.5) * np.exp(-dx / (r * 1.4)) + np.exp(-dx * dx / 0.5) * np.exp(-dy / (r * 1.4))
    add(light, col, (core + 0.45 * arms) * k)


# =========================================================================== the Husk

HU, HU_PX, HU_N = 104, 40.0, 32
HU_EXT = HU / HU_PX
SHAPES = ("husk", "husk-b", "husk-c")
FLARE, SLAB = 112, 128


def _husk(shape, k, crack):
    scene, mats = u84.husk_model("a", shape, crack)
    scene, mats = bu.turned(scene, mats, k, HU_N)
    return bu.render(scene, mats, (HU, HU), HU_EXT, crisp=80)


def husk_jobs():
    return [("husk", s, k, c) for s in range(3) for c in (0.0, 1.0) for k in range(HU_N)]


def flare_frames(n=8):
    out = []
    rng = np.random.default_rng(4601)
    pts = [(rng.uniform(0, TAU), rng.uniform(12, 30), rng.uniform(1.6, 2.6)) for _ in range(7)]
    for f in range(n):
        u = f / (n - 1)
        k = np.sin(np.pi * min(1.0, u * 1.15)) ** 1.5 if u < 0.87 else 0.0
        light = np.zeros((FLARE, FLARE, 3))
        x, y = grid(FLARE, FLARE)
        rr = np.hypot((x - FLARE / 2) / 1.0, (y - FLARE / 2) / 1.15)
        add(light, ICE, np.exp(-(rr / 26.0) ** 2) * 0.42 * k + np.exp(-(rr / 12.0) ** 2) * 0.25 * k)
        for a, r, s in pts:
            glint(light, FLARE / 2 + np.cos(a) * r, FLARE / 2 + np.sin(a) * r * 1.1, s * (0.7 + 0.5 * k),
                  (225, 240, 255), 0.85 * k)
        out.append(light_img(light, 8))
    return out


def _slab_poly(rng, size):
    n = rng.integers(4, 6)
    ang = np.sort(rng.uniform(0, TAU, n))
    rad = size * rng.uniform(0.6, 1.0, n)
    return np.stack([np.cos(ang) * rad, np.sin(ang) * rad], axis=1)


def _collapse_parts():
    rng = np.random.default_rng(4602)
    slabs, splinters = [], []
    for j in range(9):
        a = j * TAU / 9 + rng.uniform(-0.3, 0.3)
        r0 = rng.uniform(10, 30)
        slabs.append(dict(x0=np.cos(a) * r0 * 1.1, y0=np.sin(a) * r0 * 0.9, a=a, sp=rng.uniform(14, 26),
                          rot0=rng.uniform(0, TAU), w=rng.uniform(-1.6, 1.6), size=rng.uniform(11, 16),
                          poly=_slab_poly(rng, 1.0), tilt=rng.uniform(0, TAU), shade=rng.uniform(0.85, 1.1)))
    for j in range(13):
        a = rng.uniform(0, TAU)
        splinters.append(dict(a=a, sp=rng.uniform(30, 52), r0=rng.uniform(4, 18), rot=a + rng.uniform(-0.4, 0.4),
                              len=rng.uniform(4, 7), w=rng.uniform(-3, 3)))
    return slabs, splinters


def _slab_state(s, u):
    fold = 0.18 * np.sin(np.pi * min(u * 3, 1))                  # the hull folds in first
    d = s["sp"] * (1 - (1 - u) ** 1.6)
    x = s["x0"] * (1 - fold) + np.cos(s["a"]) * d
    y = s["y0"] * (1 - fold) + np.sin(s["a"]) * d
    return x, y, s["rot0"] + s["w"] * u, s["size"] * (1 - u) ** 0.7


def slabs_frames(n=16, ss=4):
    slabs, splinters = _collapse_parts()
    hull = HULL_RGB * COLD
    out = []
    for f in range(n):
        u = f / (n - 1) * 0.94
        im = Image.new("RGBA", (SLAB * ss, SLAB * ss), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        for s in splinters:
            dist = s["r0"] + s["sp"] * (1 - (1 - u) ** 1.4)
            x, y = SLAB / 2 + np.cos(s["a"]) * dist, SLAB / 2 + np.sin(s["a"]) * dist
            L = s["len"] * (1 - u) ** 1.2
            if L < 0.8:
                continue
            ra = s["rot"] + s["w"] * u
            cx, cy = np.cos(ra), np.sin(ra)
            pts = [(x + cx * L, y + cy * L), (x - cy * 0.9, y + cx * 0.9), (x - cx * L * 0.6, y - cy * L * 0.6),
                   (x + cy * 0.9, y - cx * 0.9)]
            d.polygon([(px * ss, py * ss) for px, py in pts], fill=tuple(int(v) for v in CRYSTAL * 1.05) + (255,))
        for s in slabs:
            x, y, rot, size = _slab_state(s, u)
            if size < 1.2:
                continue
            c, si = np.cos(rot), np.sin(rot)
            pts = [(SLAB / 2 + x + (px * c - py * si) * size, SLAB / 2 + y + (px * si + py * c) * size)
                   for px, py in s["poly"]]
            lam = s["shade"] * (0.62 + 0.38 * np.cos(s["tilt"] + rot * 1.3)) * (1 - 0.35 * u)
            d.polygon([(px * ss, py * ss) for px, py in pts], fill=tuple(int(v) for v in np.clip(hull * lam, 0, 255))
                      + (255,), outline=tuple(int(v) for v in np.clip(hull * lam * 0.45, 0, 255)) + (255,))
            # the plate's lit top-left edge
            best = min(range(len(pts)), key=lambda i: pts[i][0] + pts[i][1])
            q = pts[best]
            nxt = pts[(best + 1) % len(pts)]
            d.line([(q[0] * ss, q[1] * ss), (nxt[0] * ss, nxt[1] * ss)],
                   fill=tuple(int(v) for v in np.clip(hull * lam * 1.45 + 14, 0, 255)) + (255,), width=ss)
        a = np.array(im.resize((SLAB, SLAB), Image.BOX)).astype(np.float64)
        rgb = a[..., :3] / np.maximum(a[..., 3:4] / 255, 1e-6)
        out.append(to_img(np.clip(rgb, 0, 255), a[..., 3] / 255))
    return artkit.quantize_set(out, 32)


def death_frames(n=16):
    _, splinters = _collapse_parts()
    out = []
    for f in range(n):
        u = f / (n - 1) * 0.94
        light = np.zeros((SLAB, SLAB, 3))
        x, y = grid(SLAB, SLAB)
        rr = np.hypot(x - SLAB / 2, y - SLAB / 2)
        add(light, ICE * 0.8, np.exp(-(rr / (16 + 26 * u)) ** 2) * 0.28 * (1 - u) ** 1.6)
        for j, s in enumerate(splinters):
            dist = s["r0"] + s["sp"] * (1 - (1 - u) ** 1.4)
            k = (1 - u) ** 1.3 * (0.6 + 0.4 * np.sin(j * 1.7 + u * 9) ** 2)
            glint(light, SLAB / 2 + np.cos(s["a"]) * dist, SLAB / 2 + np.sin(s["a"]) * dist, 1.4, ICE, 0.7 * k)
        out.append(light_img(light, 10))
    return out


SHARD = 23


def shard_frames(n=16, ss=8):
    out = []
    for k in range(n):
        th = k * TAU / n
        dx, dy = -np.sin(th), np.cos(th)                     # clockwise from straight down (needle's order)
        x, y = grid(SHARD, SHARD, ss)
        u = (x - SHARD / 2) * dx + (y - SHARD / 2) * dy      # along, + = the point
        v = -(x - SHARD / 2) * dy + (y - SHARD / 2) * dx
        half = np.where(u > 0, 2.7 * np.clip(1 - u / 7.5, 0, 1), 2.7 * np.clip(1 + u / 5.0, 0, 1))
        body = np.abs(v) < half
        outline = (np.abs(v) < half + 1.1) & (u > -6.2) & (u < 8.6)
        outline |= (u > -6.2) & (u < 8.6) & (np.abs(v) < np.where(u > 0, 3.8 * np.clip(1 - u / 8.6, 0, 1),
                                                                    3.8 * np.clip(1 + u / 6.2, 0, 1)))
        core = (np.abs(v) < 0.9 * np.clip(1 - np.abs(u) / 6.5, 0, 1)) & body
        rgb = np.zeros(x.shape + (3,))
        rgb[outline] = (18, 22, 36)
        facet = np.where(v * 0.7 - u * 0.2 < 0, 1.08, 0.82)        # the top-left facet lit
        rgb[body] = (np.array((168.0, 200, 232))[None, :] * facet[body][:, None])
        rgb[core] = (245, 250, 255)
        al = outline.astype(float)
        rgb = rgb.reshape(SHARD, ss, SHARD, ss, 3).mean(axis=(1, 3))
        al = al.reshape(SHARD, ss, SHARD, ss).mean(axis=(1, 3))
        rgb = rgb / np.maximum(al[..., None], 1e-6)
        out.append(to_img(rgb, al))
    return artkit.quantize_set(out, 16)


ORB = 15


def orb_frames(n=4, ss=8):
    out = []
    for f in range(n):
        x, y = grid(ORB, ORB, ss)
        rr = np.hypot(x - ORB / 2, y - ORB / 2)
        rgb = np.zeros(x.shape + (3,))
        al = (rr < 6.6).astype(float)
        rgb[rr < 6.6] = (20, 24, 40)
        ring = (rr < 5.4)
        rgb[ring] = (150, 182, 214)
        rgb[rr < 4.2] = (200, 224, 246)
        c = 2.2 + 0.7 * np.sin(np.pi * f / n) ** 2
        rgb[rr < c] = (250, 252, 255)
        rgb = rgb.reshape(ORB, ss, ORB, ss, 3).mean(axis=(1, 3))
        al = al.reshape(ORB, ss, ORB, ss).mean(axis=(1, 3))
        out.append(to_img(rgb / np.maximum(al[..., None], 1e-6), al))
    return artkit.quantize_set(out, 12)


def build_husk(res):
    n = HU_N
    for s, name in enumerate(SHAPES):
        whole = res[(2 * s) * n:(2 * s + 1) * n]
        cracked = res[(2 * s + 1) * n:(2 * s + 2) * n]
        frames = artkit.quantize_set([grade(f, **HUSK_GRADE) for f in whole + cracked], 48)
        save_set(name, frames[:n])
        save_set(name + "-cracked", frames[n:])
    save_set("husk-flare", flare_frames())
    save_set("husk-slabs", slabs_frames())
    save_set("husk-death", death_frames())
    save_set("husk-shard", shard_frames())
    save_set("husk-orb", orb_frames())


def husk_intel_hi():
    """The Husk nose down (heading 0), shape 1 cracked, for the hangar intel's 30 px portrait (an 8x render)."""
    scene, mats = bu.turned(*u84.husk_model("a", 0, 1.0), 0, 16)
    return sdf.render(scene, mats, (240, 240), 2.45)


# =========================================================================== the seed casket

CK = (40, 28)
CK_PX = 20.0


def casket_scene(broken=False):
    plates = u84.hull_plates(cell=0.24, seed=5.0)
    mats = [Material(hx("6e645c"), metal=0.1, shininess=16, spec=0.2, pattern=plates),
            Material(hx("2a2622"), metal=0.05, shininess=10, spec=0.1),
            Material((0.06, 0.12, 0.12), shininess=40, spec=0.4, emission=(0.10, 0.36, 0.34)),
            Material((0.02, 0.02, 0.02)),
            Material(hx("8a8078"), metal=0.1, shininess=24, spec=0.3)]
    R = (0.9, 0.58, 0.42)

    def scene(p):
        if not broken:
            ell = sd_ellipsoid(p, (0, 0, 0), R)
            caps = union((sd_ellipsoid(p, (0.82, 0, 0.02), (0.16, 0.34, 0.3)), 4),
                         (sd_ellipsoid(p, (-0.82, 0, 0.02), (0.16, 0.34, 0.3)), 4), k=0.02)
            seam = sd_capsule(p, (-0.78, 0.0, 0.43), (0.78, 0.0, 0.43), 0.035)
            groove = np.maximum(ell, -sd_capsule(p, (-0.8, 0, 0.42), (0.8, 0, 0.42), 0.07))
            d, m = union((groove, 0), caps, (seam, 2), k=0.01)
            m = np.where((m == 0) & (p[:, 2] < 0.1), 1, m)
            return d, m
        ell = sd_ellipsoid(p, (0, 0, 0), R)
        jag = 0.05 * np.sin(p[:, 0] * 17.0) + 0.03 * np.sin(p[:, 0] * 41.0 + 1.0)
        opening = sd_ellipsoid(p, (0, 0, 0.42), (0.74, 0.22, 0.34)) - jag
        shell = np.maximum(np.abs(ell) - 0.05, -opening)
        caps = union((sd_ellipsoid(p, (0.82, 0, 0.02), (0.16, 0.34, 0.3)), 4),
                     (sd_ellipsoid(p, (-0.82, 0, 0.02), (0.16, 0.34, 0.3)), 4), k=0.02)
        d, m = union((shell, 0), caps, k=0.01)
        m = np.where((m == 0) & (ell < -0.02), 3, m)
        m = np.where((m == 0) & (p[:, 2] < 0.1), 1, m)
        return d, m
    return scene, mats


def casket_intel_hi():
    """The seed casket whole, seam up, for the hangar intel's 30 px portrait (an 8x render; step D4: Level 46's intel
    lists every unit of its waves)."""
    scene, mats = casket_scene(False)
    return sdf.render(scene, mats, (240, 240), 2.3)


def _casket(broken):
    scene, mats = casket_scene(broken)
    return reef.render(scene, mats, CK, CK[0] / CK_PX, turn=False)


def casket_tatters(broken, n=8, w=64):
    a = np.array(broken)
    top, bot = a.copy(), a.copy()
    top[CK[1] // 2:] = 0
    bot[:CK[1] // 2] = 0
    chips = chips_frames(n, w, [HULL_RGB, HULL_RGB * 0.6, TEAL_S * 0.6], 4611, count=10, speed=(26, 40),
                         size=(1.4, 2.4))
    out = []
    for f in range(n):
        u = (f + 1) / n
        im = Image.new("RGBA", (w, w), (0, 0, 0, 0))
        s = (1 - u) ** 0.8
        for half, sgn in ((top, -1), (bot, 1)):
            if s < 0.08:
                continue
            h = Image.fromarray(half, "RGBA")
            h = h.resize((max(1, round(CK[0] * s)), max(1, round(CK[1] * s))), Image.NEAREST)
            l23kit.paste_c(im, h, w / 2 + sgn * 3 * u, w / 2 + sgn * 12 * u)
        im.alpha_composite(chips[f])
        out.append(im)
    return artkit.quantize_set(out, 32)


def casket_death(n=8, w=64):
    out = []
    for f in range(n):
        u = f / (n - 1)
        light = np.zeros((w, w, 3))
        x, y = grid(w, w)
        r = np.hypot((x - w / 2) / 1.6, y - w / 2)
        add(light, TEAL_S, np.exp(-(r / (5 + 12 * u)) ** 2) * 0.55 * (1 - u) ** 1.5)
        out.append(light_img(light, 6))
    return out


def build_casket(res):
    whole, broken = artkit.quantize_set([grade(r, contrast=1.05, pivot=70.0) for r in res], 32)
    save_set("seed-casket", [whole], single=True)
    save_set("seed-casket-broken", [broken], single=True)
    save_set("seed-casket-tatters", casket_tatters(broken))
    save_set("seed-casket-death", casket_death())


# =========================================================================== the warm core and its sinews

CORE_PX = 40.0
SHUT = (128, 104)
RISE = (88, 88)
SINEWS = [(-46, -34), (44, -38), (-44, 36), (46, 32)]    # the knots round the core's centre, px (y down)
N_SHUT, N_RISE, N_DRIFT = 8, 10, 8


def core_mats():
    return [Material((0.30, 0.15, 0.40), metal=0.1, shininess=90, spec=0.8, pattern=em._ridges(16, 0.18),
                     emission=(0.55, 0.36, 0.10), emission_pattern=em._veins(8, 0.6, 0.32)),
            Material((0.05, 0.02, 0.04), emission=(1.0, 0.70, 0.28)),
            Material((0.40, 0.18, 0.26), metal=0.05, shininess=50, spec=0.5, pattern=em._ridges(26, 0.25)),
            Material((0.22, 0.09, 0.14), shininess=20, spec=0.2)]


def core_scene(breath=0.0, sinews=True, stubs=False):
    s = 1.0 + 0.04 * breath
    R = 0.6 * s

    def scene(p):
        body = sd_ellipsoid(p, (0, 0, 0.12), (R, R * 0.9, R * 0.7))
        slits = [sd_capsule(p, (np.cos(a) * 0.12, np.sin(a) * 0.12, 0.5 * s), (np.cos(a) * 0.42 * s,
                                                                                 np.sin(a) * 0.38 * s, 0.38 * s), 0.05)
                 for a in (0.3, 1.9, 3.5, 5.0)]
        cut = slits[0]
        for c in slits[1:]:
            cut = np.minimum(cut, c)
        items = [(np.maximum(body, -cut), 0), (sd_ellipsoid(p, (0, 0, 0.12), (R * 0.8, R * 0.72, R * 0.55)), 1)]
        for kx, ky in SINEWS:
            ex, ey = kx / CORE_PX, -ky / CORE_PX
            dirx, diry = ex / np.hypot(ex, ey), ey / np.hypot(ex, ey)
            a = (dirx * R * 0.8, diry * R * 0.75, 0.1)
            if sinews:
                items.append((sd_capsule(p, a, (ex, ey, -0.05), 0.09, 0.06), 2))
            elif stubs:
                b = (a[0] + dirx * 0.24, a[1] + diry * 0.24 - 0.1, -0.08)
                items.append((sd_capsule(p, a, b, 0.085, 0.05), 2))
                items.append((sd_sphere(p, b, 0.06), 3))
        return union(*items, k=0.05)
    return scene, core_mats()


def _core(kind, breath, scale):
    if kind == "shut":
        scene, mats = core_scene(breath, sinews=True)
        return reef.render(scene, mats, SHUT, SHUT[0] / CORE_PX, turn=False)
    scene, mats = core_scene(breath, sinews=False, stubs=True)
    return reef.render(reef.scaled_scene(scene, scale), mats, RISE, RISE[0] / CORE_PX, turn=False)


def core_jobs():
    jobs = [("core", "shut", 0.5 - 0.5 * np.cos(TAU * i / N_SHUT), 1.0) for i in range(N_SHUT)]
    jobs += [("core", "rise", 0.5, 1.0 + 0.2 * (i + 1) / N_RISE) for i in range(N_RISE)]
    jobs += [("core", "drift", 0.5 - 0.5 * np.cos(TAU * i / N_DRIFT), 1.2) for i in range(N_DRIFT)]
    jobs += [("knot", st) for st in range(3)]
    return jobs


def core_glow(k, scale, w=128):
    light = np.zeros((w, w, 3))
    x, y = grid(w, w)
    rr = np.hypot(x - w / 2, y - w / 2) / scale
    add(light, GOLD_W, np.exp(-(rr / 17.0) ** 2) * 0.55 * k)
    add(light, VIOLET_W, np.exp(-(rr / 34.0) ** 2) * 0.42 * k)
    return light_img(light, 10)


def knot_scene(state):
    mats = core_mats()[2:] + [Material((0.05, 0.03, 0.02), emission=(0.9, 0.6, 0.22)),
                              Material(hx("4a423c"), metal=0.1, shininess=14, spec=0.15)]

    def scene(p):
        items = []
        grips = [sd_capsule(p, (0, 0, 0.05), (np.cos(a) * 0.46, np.sin(a) * 0.42, -0.08), 0.07, 0.04)
                 for a in (0.4, 1.9, 3.3, 4.8)]
        grip = grips[0]
        for g in grips[1:]:
            grip = np.minimum(grip, g)
        if state < 2:
            knot = sd_ellipsoid(p, (0, 0, 0.12), (0.34, 0.3, 0.24))
            body = sdf.smin(knot, grip, 0.06)
            if state == 1:
                gash = sd_capsule(p, (-0.2, -0.15, 0.36), (0.22, 0.12, 0.34), 0.07)
                items += [(np.maximum(body, -gash), 0), (np.maximum(gash, knot - 0.01), 2)]
            else:
                items.append((body, 0))
        else:
            stump = sd_ellipsoid(p, (0.02, 0.0, 0.05), (0.26, 0.22, 0.14))
            items += [(sdf.smin(stump, grip, 0.05), 0), (sd_sphere(p, (0.04, 0.02, 0.17), 0.1), 1)]
        return union(*items, k=0.04)
    return scene, mats


def _knot(state):
    scene, mats = knot_scene(state)
    return reef.render(scene, mats, (28, 28), 28 / 24.0, turn=False)


def build_core(res):
    shut, rise = res[:N_SHUT], res[N_SHUT:N_SHUT + N_RISE]
    drift, knots = res[N_SHUT + N_RISE:N_SHUT + N_RISE + N_DRIFT], res[N_SHUT + N_RISE + N_DRIFT:]
    allf = artkit.quantize_set(shut + rise + drift, 48)
    save_set("vrell-core", allf[:N_SHUT])
    save_set("vrell-core-rise", allf[N_SHUT:N_SHUT + N_RISE])
    save_set("vrell-core-drift", allf[N_SHUT + N_RISE:])
    glows = [core_glow(0.35 + 0.65 * (i + 1) / N_RISE, 1.0 + 0.2 * (i + 1) / N_RISE) for i in range(N_RISE)]
    glows += [core_glow(0.85 + 0.15 * np.sin(TAU * i / 6), 1.2) for i in range(6)]
    save_set("vrell-core-glow", glows)
    save_set("warm-hull-sinew", artkit.quantize_set(knots, 32))
    flesh = np.array((0.40, 0.18, 0.26)) * 255
    save_set("warm-hull-sinew-break", chips_frames(8, 48, [flesh, flesh * 0.6, GOLD_W * 0.7], 4621, count=11))


# =========================================================================== the scenery hulks

HK_PX = 60.0
H_HULL, H_DARK, H_EDGE, H_HOLLOW, H_CRYS = range(5)
ROCK_N, ROCK_STEP = 9, 1.75                                # renders, degrees between them
ROCK_SEQ = list(range(ROCK_N)) + list(range(ROCK_N - 2, 0, -1))    # 16 frames forth and back
ROCK_FPS = 2.5
# per hulk: its ellipsoids (centre, radii), the holes ((x, y), r), a tear (y0, side: the half kept is side * (y - y0) < 0),
# its base heading (degrees clockwise from facing down), its canvas
HULKS = {
    "hulk-a": dict(ells=[((0, 0.2, 0), (0.46, 2.1, 0.34)), ((0, -1.4, -0.04), (0.38, 0.8, 0.3)),
                         ((0.0, 0.6, 0.12), (0.18, 1.2, 0.3))],
                   holes=[((0.1, 0.9), 0.24), ((-0.12, -0.3), 0.3), ((0.12, -1.35), 0.2)], tear=None, head=148,
                   size=(300, 300), seed=21),
    "hulk-b": dict(ells=[((0, 0, 0), (1.0, 1.35, 0.36)), ((0, 0.9, 0.05), (0.55, 0.7, 0.3)),
                         ((0.7, -0.6, -0.04), (0.45, 0.55, 0.26)), ((-0.7, -0.6, -0.04), (0.45, 0.55, 0.26))],
                   holes=[((0.15, 0.1), 0.48), ((-0.5, 0.75), 0.2), ((0.62, -0.7), 0.18)], tear=None, head=28,
                   size=(230, 230), seed=22),
    "hulk-c": dict(ells=[((0, -0.8, 0), (0.6, 1.9, 0.38)), ((0, 0.5, 0.04), (0.32, 0.6, 0.26))],
                   holes=[((0.08, -0.2), 0.22)], tear=(-1.5, -1), head=62, size=(260, 260), seed=23),
    "hulk-d": dict(ells=[((0, 0.8, 0), (0.6, 1.9, 0.38)), ((0.36, -0.5, -0.05), (0.26, 0.55, 0.24)),
                         ((-0.36, -0.5, -0.05), (0.26, 0.55, 0.24))],
                   holes=[((-0.1, 0.4), 0.2)], tear=(1.7, 1), head=-118, size=(240, 240), seed=24),
    "helix-hulk": dict(ells=[((0, 0, 0), (0.7, 1.5, 0.4)), ((0.0, 1.2, 0.03), (0.34, 0.5, 0.26))],
                       holes=[((0.0, 0.05), 0.5), ((0.3, -0.95), 0.16)], tear=(-1.15, -1), head=0, size=(0, 0),
                       seed=25),
}


def hulk_model(name):
    spec = HULKS[name]
    ells = spec["ells"]
    rng = np.random.default_rng(spec["seed"])

    def surf_z(x, y, lift=0.0):
        z = -1.0
        for (cx, cy, cz), (rx, ry, rz) in ells:
            q = 1 - ((x - cx) / rx) ** 2 - ((y - cy) / ry) ** 2
            if q > 0:
                z = max(z, cz + rz * np.sqrt(q))
        return max(z, 0.0) + lift

    lips, crys = [], []

    def inside(x, y, margin=0.06):
        ok = any(1 - ((x - cx) / rx) ** 2 - ((y - cy) / ry) ** 2 > margin for (cx, cy, _), (rx, ry, _) in ells)
        if tear:
            ok = ok and tear[1] * (y - tear[0]) < -0.05
        return ok

    def crystal(x0, y0, a, length):
        b = a + rng.uniform(-0.35, 0.35)
        x1, y1 = x0 + np.cos(b) * length, y0 + np.sin(b) * length
        xm, ym, c2 = (x0 + x1) / 2, (y0 + y1) / 2, b + rng.choice((-0.8, 0.8))
        x2, y2 = xm + np.cos(c2) * length * 0.45, ym + np.sin(c2) * length * 0.45
        for (ax, ay), (bx, by) in (((x0, y0), (x1, y1)), ((xm, ym), (x2, y2))):
            if inside(ax, ay) and inside(bx, by):
                crys.append(((ax, ay, surf_z(ax, ay, 0.012)), (bx, by, surf_z(bx, by, 0.012))))

    tear = spec["tear"]
    for (cx, cy), r in spec["holes"]:
        for j in range(9):
            a = j * TAU / 9 + rng.uniform(-0.2, 0.2)
            px, py = cx + np.cos(a) * (r + 0.08), cy + np.sin(a) * (r + 0.08)
            lips.append(((px, py, surf_z(px, py, 0.03)), a, (0.12, 0.08 * rng.uniform(0.8, 1.2), 0.02),
                         rng.uniform(0.5, 0.85)))
        for j in range(3):
            a = rng.uniform(0, TAU)
            crystal(cx + np.cos(a) * (r + 0.14), cy + np.sin(a) * (r + 0.14), a, 0.32 + 0.25 * r)
    if tear:
        y0, side = tear
        xs = np.linspace(-0.55, 0.55, 8)
        for x in xs:
            px, py = x, y0 - side * 0.06
            lips.append(((px, py, surf_z(px, py, 0.02)), np.pi / 2 if side > 0 else -np.pi / 2,
                         (0.1, 0.07, 0.02), rng.uniform(0.6, 1.0)))
        for x in xs[::2]:
            crystal(x, y0 - side * 0.12, (-np.pi / 2 if side > 0 else np.pi / 2) + rng.uniform(-0.5, 0.5), 0.3)

    def scene(p):
        body = None
        for c, r in ells:
            e = sd_ellipsoid(p, c, r)
            body = e if body is None else sdf.smin(body, e, 0.14)
        d = np.abs(body) - 0.06
        for (cx, cy), r in spec["holes"]:
            d = np.maximum(d, -sd_sphere(p, (cx, cy, surf_z(cx, cy)), r * 1.05))
        if tear:
            y0, side = tear
            jag = (side * (p[:, 1] - y0) + 0.07 * np.sin(p[:, 0] * 11.0 + spec["seed"])
                   + 0.04 * np.sin(p[:, 0] * 27.0 + 1.3)) * 0.7
            d = np.maximum(d, jag)
        m = np.where(body < 0, H_HOLLOW, H_HULL)
        items = [(d, m)]
        for c, a, size, tilt in lips:
            items.append((u84.bent_plate(p, c, a, size, tilt), H_EDGE))
        for a, b in crys:
            items.append((sd_capsule(p, a, b, 0.05, 0.02), H_CRYS))
        d, m = union(*items, k=0.012)
        m = np.where((m == H_HULL) & (p[:, 2] < 0.1), H_DARK, m)
        return d, m
    mats = [Material(hx("8f8273"), metal=0.05, shininess=10, spec=0.08, pattern=u84.hull_plates(0.3, 3.0)),
            Material(hx("4a4440"), metal=0.05, shininess=8, spec=0.05),
            Material(hx("a09890"), metal=0.05, shininess=12, spec=0.12),
            Material((0.03, 0.028, 0.03)),
            Material(hx("b8c8d4"), metal=0.3, shininess=160, spec=0.9,
                     emission=tuple(np.array((0.72, 0.82, 0.9)) * 0.22))]
    return scene, mats


def _hulk(name, j):
    spec = HULKS[name]
    scene, mats = hulk_model(name)
    deg = spec["head"] + (j - (ROCK_N - 1) / 2) * ROCK_STEP
    scene, mats = turned_by(scene, mats, deg)
    w, h = spec["size"]
    rgb, a = render_soft(scene, mats, (w, h), w / HK_PX)
    return rgba(rgb, a)


def crop_set(frames, margin=3):
    """Crop a set of frames to their common box (+ margin), even sizes, centred as before when possible."""
    boxes = [f.getbbox() for f in frames if f.getbbox()]
    x0, y0 = min(b[0] for b in boxes), min(b[1] for b in boxes)
    x1, y1 = max(b[2] for b in boxes), max(b[3] for b in boxes)
    w, h = frames[0].size
    x0, y0, x1, y1 = max(0, x0 - margin), max(0, y0 - margin), min(w, x1 + margin), min(h, y1 + margin)
    if (x1 - x0) % 2:
        x1 = min(w, x1 + 1) if x1 < w else x1
        x0 = x0 - 1 if (x1 - x0) % 2 and x0 > 0 else x0
    if (y1 - y0) % 2:
        y1 = min(h, y1 + 1) if y1 < h else y1
        y0 = y0 - 1 if (y1 - y0) % 2 and y0 > 0 else y0
    return [f.crop((x0, y0, x1, y1)) for f in frames]


# =========================================================================== the warm hull

WH = (280, 330)
WH_HEAD = 145.0                                           # the prow to the top left
CORE_LOCAL = (0.0, 0.1)                                    # the core in the hull's rib cage, model units


def warm_hull_model():
    rng = np.random.default_rng(4631)
    scars = [(rng.uniform(-0.55, 0.55), rng.uniform(-2.2, 2.3), rng.uniform(0.06, 0.15)) for _ in range(10)]
    scars = [sc for sc in scars if abs(sc[1] - CORE_LOCAL[1]) > 1.25 or abs(sc[0]) > 0.5]
    ribs = [(-0.75, 0.5), (-0.38, 0.62), (0.0, 0.66), (0.38, 0.62), (0.75, 0.5)]

    def top(x, y):
        q = 1 - (x / 0.78) ** 2 - (y / 2.5) ** 2
        return 0.4 * np.sqrt(max(q, 0.0))

    def scene(p):
        body = sdf.smin(sd_ellipsoid(p, (0, 0, 0), (0.78, 2.5, 0.42)), sd_ellipsoid(p, (0, 2.0, 0.02),
                                                                                    (0.36, 0.9, 0.3)), 0.2)
        body = sdf.smin(body, sd_ellipsoid(p, (0, -2.15, -0.02), (0.6, 0.6, 0.34)), 0.18)
        for sx in (-0.42, 0.42):
            body = sdf.smin(body, sd_ellipsoid(p, (sx, -2.45, -0.06), (0.2, 0.35, 0.2)), 0.08)
        shell = np.abs(body) - 0.07
        cage = sd_ellipsoid(p, (CORE_LOCAL[0], CORE_LOCAL[1], 0.42), (0.6, 0.95, 0.5))
        d = np.maximum(shell, -cage)
        for sx, sy, r in scars:
            d = np.maximum(d, -sd_sphere(p, (sx, sy, top(sx, sy) + 0.02), r))
        m = np.where(body < 0, 3, 0)
        items = [(d, m)]
        for ry, half in ribs:
            y = CORE_LOCAL[1] + ry * 1.25
            items.append((sd_capsule(p, (-half - 0.05, y, 0.2), (0.0, y + 0.05, 0.44), 0.06), 2))
            items.append((sd_capsule(p, (0.0, y + 0.05, 0.44), (half + 0.05, y, 0.2), 0.06), 2))
        items.append((sd_capsule(p, (0, CORE_LOCAL[1] - 0.9, 0.05), (0, CORE_LOCAL[1] + 0.9, 0.05), 0.12), 2))
        d, m = union(*items, k=0.03)
        scar = np.zeros(len(p), bool)
        for sx, sy, r in scars:
            scar |= np.hypot(p[:, 0] - sx, (p[:, 1] - sy) * 0.8) < r * 1.5 + 0.03 * np.sin(p[:, 0] * 31 + sy * 7)
        m = np.where((m == 0) & scar, 4, m)
        m = np.where((m == 0) & (p[:, 2] < 0.12), 1, m)
        return d, m
    mats = [Material(hx("6a5e56"), metal=0.08, shininess=14, spec=0.15, pattern=u84.hull_plates(0.26, 7.0)),
            Material(hx("2c2622"), metal=0.05, shininess=8, spec=0.08),
            Material(hx("7a6c66"), metal=0.05, shininess=30, spec=0.3, pattern=em._ridges(20, 0.2)),
            Material((0.035, 0.025, 0.03)),
            Material((0.12, 0.09, 0.08), metal=0.0, shininess=6, spec=0.05)]
    return scene, mats


def to_screen(mx, my, deg, ppu):
    """A model point (units, +Y forward) of a model ``turned_by(deg)`` -> px from the render's centre (x right, y
    down): the inverse of the scene's rotation."""
    rot = model_rotation(np.pi / 2 + np.radians(deg))
    p = rotate_z(np.array([[mx, my, 0.0]]), -rot)[0]
    return float(p[0]) * ppu, float(-p[1]) * ppu


def _warm_hull():
    scene, mats = turned_by(*warm_hull_model(), WH_HEAD)
    rgb, a = render_soft(scene, mats, WH, WH[0] / HK_PX)
    return rgba(rgb, a)


WH_GLOW_N = 8


def warm_hull_glow(body):
    cx, cy = core_xy()
    a = np.array(body).astype(np.float64)[..., 3] / 255
    x, y = grid(*WH)
    rr = np.hypot(x - (WH[0] / 2 + cx), y - (WH[1] / 2 + cy))
    out = []
    for i in range(WH_GLOW_N):
        k = 0.8 + 0.2 * np.sin(TAU * i / WH_GLOW_N)
        light = np.zeros(WH[::-1] + (3,))
        add(light, GOLD_W, np.exp(-(rr / 40.0) ** 2) * 0.32 * k * a)
        add(light, VIOLET_W, np.exp(-(rr / 70.0) ** 2) * 0.16 * k * a)
        out.append(light_img(light, 6))
    return out


def core_xy():
    """The core's centre from the warm hull piece's centre, px (y down)."""
    x, y = to_screen(*CORE_LOCAL, WH_HEAD, HK_PX)
    return round(x, 1), round(y, 1)


# =========================================================================== the Helix wreck, its hatch, Vorne's courier

HX = (220, 220)
HX_HEAD = 120.0
CRAFT_AT, CRAFT_TURN = (0.02, 0.1), 0.45                   # the craft in the hulk's breach (model units, radians)


def helix_wreck_model():
    hk_scene, hk_mats = hulk_model("helix-hulk")
    craft = [Material((0.70, 0.70, 0.68), metal=0.4, shininess=80, spec=0.7, pattern=em._ridges(30, 0.08)),
             Material((0.95, 0.72, 0.26), metal=0.9, shininess=140, spec=1.2),
             Material((0.05, 0.06, 0.08), metal=0.5, shininess=120, spec=0.9),
             Material((0.30, 0.30, 0.30), metal=0.5, shininess=40, spec=0.4)]
    n = len(hk_mats)

    def scene(p):
        q = rotate_z(p - np.array((CRAFT_AT[0], CRAFT_AT[1], 0.0))[None, :], CRAFT_TURN)
        d0, m0 = hk_scene(p)
        body = sd_capsule(q, (0, -0.5, 0.3), (0, 0.5, 0.36), 0.19, 0.15)
        band = np.maximum(body - 0.012, np.abs(q[:, 1] + 0.12) - 0.05)
        wings = np.minimum(sd_box(q, (0.0, -0.25, 0.28), (0.5, 0.11, 0.03), 0.02),
                           sd_box(q, (0.0, -0.48, 0.36), (0.05, 0.12, 0.14), 0.02))
        hatch = sd_box(q, (0.0, 0.12, 0.5), (0.09, 0.09, 0.02), 0.01)
        canopy = sd_ellipsoid(q, (0, 0.36, 0.44), (0.1, 0.12, 0.06))
        d1, m1 = union((body, n), (band, n + 1), (wings, n + 3), (hatch, n + 3), (canopy, n + 2), k=0.01)
        return np.minimum(d0, d1), np.where(d1 < d0, m1, m0)
    return scene, hk_mats + craft


def _helix_wreck():
    scene, mats = turned_by(*helix_wreck_model(), HX_HEAD)
    rgb, a = render_soft(scene, mats, HX, HX[0] / HK_PX)
    return rgba(rgb, a)


def hatch_scene(state):
    mats = [Material((0.62, 0.62, 0.60), metal=0.4, shininess=80, spec=0.6),
            Material((0.95, 0.72, 0.26), metal=0.9, shininess=140, spec=1.2),
            Material((0.03, 0.03, 0.035)),
            Material((0.30, 0.29, 0.28), metal=0.4, shininess=30, spec=0.3)]

    def scene(p):
        frame = sd_box(p, (0, 0, 0), (0.42, 0.42, 0.06), 0.03)
        if state == 2:
            hole = sd_box(p, (0, 0, 0.06), (0.3, 0.3, 0.1), 0.02)
            return union((np.maximum(frame, -hole), 3), (sd_box(p, (0, 0, -0.06), (0.3, 0.3, 0.02)), 2), k=0.0)
        lid = sd_box(p, (0, 0, 0.08), (0.32, 0.32, 0.03), 0.02)
        if state == 1:
            lid = np.maximum(lid, -sd_sphere(p, (0.12, -0.1, 0.2), 0.14))
        hexa = np.maximum(sd_capsule(p, (-0.12, 0, 0.115), (0.12, 0, 0.115), 0.09), np.abs(p[:, 1]) - 0.1)
        return union((frame, 3), (lid, 0), (hexa, 1), k=0.01)
    return scene, mats


def _hatch(state):
    scene, mats = hatch_scene(state)
    return reef.render(scene, mats, (24, 24), 24 / 24.0, turn=False)


def courier_scene():
    mats = [Material((0.95, 0.72, 0.26), metal=0.9, shininess=150, spec=1.4, pattern=em._ridges(22, 0.06)),
            Material((0.04, 0.04, 0.05), metal=0.6, shininess=120, spec=0.9),
            Material((0.10, 0.07, 0.05), shininess=6, spec=0.05)]

    def scene(p):
        body = sd_ellipsoid(p, (0, 0.05, 0), (0.36, 0.62, 0.2))
        fins = sd_box(p, (0, -0.2, 0.0), (0.56, 0.1, 0.04), 0.02)
        nozzle = sd_capsule(p, (0, -0.52, 0.0), (0, -0.74, 0.0), 0.13, 0.16)
        lines = np.maximum(np.abs(body) - 0.015, np.minimum(np.abs(p[:, 1] - 0.2), np.abs(p[:, 0])) - 0.02)
        d, m = union((body, 0), (fins, 0), (nozzle, 2), (lines - 0.0, 1), k=0.02)
        return d, m
    return scene, mats


def _courier(k):
    scene, mats = courier_scene()
    tilt = 0.45 * np.sin(TAU * k / 16)
    rot_x = lambda p: sdf.rotate_x(p, tilt)  # noqa: E731
    scene2 = lambda p: scene(rot_x(p))  # noqa: E731
    s, m = turned_by(scene2, mats, k * 22.5 + 30)
    return bu.render(s, m, (40, 40), 40 / 26.0, crisp=80)


def wreck_jobs():
    return [("hatch", st) for st in range(3)] + [("courier", k) for k in range(16)]


def build_wrecks(res):
    save_set("helix-hatch", artkit.quantize_set(res[:3], 32))
    save_set("helix-hatch-break", chips_frames(8, 48, [np.array((160, 160, 155.0)), np.array((90, 90, 88.0)),
                                                       np.array((242, 184, 66.0))], 4641, count=12))
    save_set("debris-vorne-courier", artkit.quantize_set(res[3:], 40))
    save_set("debris-vorne-courier-break", chips_frames(8, 64, [np.array((242, 184, 66.0)), np.array((150, 110, 40.0)),
                                                                np.array((20, 20, 24.0))], 4642, count=14,
                                                        speed=(30, 46), size=(1.8, 3.2)))


# =========================================================================== the dying Leviathan

LEV_SETS = ["leviathan-down", "leviathan-cross", "leviathan-fluke", "leviathan-fluke-wrecked", "leviathan-blowhole-glow",
            "leviathan-fin-left", "leviathan-fin-left-wrecked", "leviathan-fin-right", "leviathan-fin-right-wrecked",
            *[f"leviathan-vent-{i}" for i in range(1, 5)], *[f"leviathan-vent-{i}-wrecked" for i in range(1, 5)],
            *[f"leviathan-chunk-{i}" for i in range(1, 6)], "leviathan-ichor"]
DYING_TINT = np.array((1.08, 1.0, 0.88))                    # toward Level 46's grey-brown


def dying_grade(img, additive=False):
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3]
    lum = rgb @ np.array((0.3, 0.59, 0.11))
    if additive:
        out = (rgb * 0.5 + lum[..., None] * np.array((1.0, 0.92, 1.1)) * 0.5) * 0.33
        a[..., :3] = np.clip(np.round(out), 0, 255)
        a[..., 3] = np.where(a[..., :3].max(axis=-1) >= 1, 255, 0)
        return Image.fromarray(a.astype(np.uint8), "RGBA")
    violet = np.clip((rgb[..., 2] - rgb[..., 1] - 20) / 60, 0, 1) * np.clip((rgb.max(axis=-1) - 90) / 80, 0, 1)
    out = rgb * 0.22 + lum[..., None] * DYING_TINT * 0.78
    out = out * (0.66 - 0.36 * violet[..., None])
    a[..., :3] = np.clip(np.round(out), 0, 255) * (a[..., 3:4] > 0)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


CROSS_HEADING = -58.01
CROSS_SCALE = 1.25


def blowhole_on_cross(size):
    """The blowhole's place on a ``leviathan-cross`` frame (centred unit): its data offset (0, 60) towards the tail,
    turned by the first pass's heading and drawn 1.25x."""
    h = np.radians(-CROSS_HEADING)
    fx, fy = np.sin(h), np.cos(h)                          # the head's direction (y down)
    return size[0] / 2 - fx * 60 * CROSS_SCALE, size[1] / 2 - fy * 60 * CROSS_SCALE


def build_leviathan():
    for name in LEV_SETS:
        frames = load(name)
        single = (ROOT / "assets" / "sprites" / f"{name}.png").exists()
        add_ = name in ("leviathan-blowhole-glow", "leviathan-ichor")
        save_set("dying-" + name, [dying_grade(f, add_) for f in frames], single=single)
    cross = load("leviathan-cross")[0]
    w, h = cross.size
    bx, by = blowhole_on_cross(cross.size)
    light = np.zeros((h, w, 3))
    x, y = grid(w, h)
    rr = np.hypot(x - bx, y - by)
    add(light, np.array((150.0, 90.0, 200.0)), np.exp(-(rr / 18.0) ** 2) * 0.45 + np.exp(-(rr / 7.0) ** 2) * 0.3)
    save_set("dying-leviathan-cross-glow", [light_img(light, 6)], single=True)
    piv = json.loads((artkit.PIVOTS / "leviathan.json").read_text(encoding="utf-8"))
    death = piv.get("death", {})
    for ch in death.get("chunks", []):
        ch["sprite"] = "dying-" + ch["sprite"]
    # step D4: the death's ichor clouds in their graded copy too (the explosions stay the shared ones)
    for blast in death.get("blasts", []):
        if blast["sprite"].startswith("leviathan-"):
            blast["sprite"] = "dying-" + blast["sprite"]
    steps = death.get("blast_frame_steps", {})
    for name in [n for n in steps if n.startswith("leviathan-")]:
        steps["dying-" + name] = steps.pop(name)
    piv["source"] = SOURCE + ", graded from leviathan.json"
    held = not bu.claimed("dying-leviathan")
    folder = HELD / "pivots" if held else artkit.PIVOTS
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "dying-leviathan.json").write_text(json.dumps(piv, indent=1) + "\n", encoding="utf-8")
    other = artkit.PIVOTS if held else HELD / "pivots"
    (other / "dying-leviathan.json").unlink(missing_ok=True)
    print(f"pivots: {(folder / 'dying-leviathan.json').relative_to(ROOT)}")


# =========================================================================== the level clock and the deep layer

FACTORS = {"deep": 0.15, "low-air": 1.3}
DATA_PATH = L46 / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Outer Wrecks", "end": 48, "atmosphere": "clear", "tiles": ["deep-1"]},
    {"name": "The Graveyard Lanes", "end": 118, "atmosphere": "clear", "tiles": ["deep-2"]},
    {"name": "The Quiet", "end": 144, "atmosphere": "clear", "tiles": ["deep-3"]},
    {"name": "The Remnants", "end": 214, "atmosphere": "clear", "tiles": ["deep-4"]},
    {"name": "The Dead World", "end": 268, "atmosphere": "clear", "tiles": ["deep-5"]},
]
SECTIONS = DATA["sections"] if DATA and DATA.get("sections") else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END = CLOCK.end
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
DEEP = [f"deep-{i + 1}" for i in range(len(SECTIONS))]
SEED = 4651


def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def far_hull(rgb, rows, x, cx, cr, L, wd, ang, broken, k):
    """A far dead hull on deep: a dark flat sliver, its top-left rim caught by the cold light, a break in it."""
    ca, sa = np.cos(ang), np.sin(ang)
    X, Y = x[None, :] - cx, rows[:, None] - cr                  # y-up rows
    u, v = X * ca + Y * sa, -X * sa + Y * ca
    prof = wd * np.sqrt(np.clip(1 - (u / L) ** 2, 0, 1)) * (0.75 + 0.25 * (u > 0))
    inside = smoothstep(0.7, -0.7, np.abs(v) - prof) * (np.abs(u) < L)
    if broken:
        inside *= smoothstep(1.5, 3.5, np.abs(u - L * 0.3))
    litk = np.clip(0.45 + v / np.maximum(prof, 1) * 0.35, 0.15, 1.0)
    col = np.array((34.0, 32.0, 40.0)) * litk[..., None] * k + np.array((120.0, 122.0, 150.0)) * \
        (np.exp(-((v - prof) / 1.1) ** 2) * 0.3 * k)[..., None]
    rgb[:] = rgb * (1 - inside[..., None]) + col * inside[..., None]


def deep_field():
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rows = np.arange(n) + r0
    x = np.arange(W) + 0.5
    rgb = np.zeros((n, W, 3)) + SPACE
    dust = raster.fbm(W, n, 160, SEED, octaves=4, period=False)
    dust2 = raster.fbm(W, n, 60, SEED + 1, octaves=3, period=False)
    rgb += (np.clip((dust - 0.42) * 1.8, 0, 1) * (0.6 + 0.4 * dust2))[..., None] * (DUST - SPACE)
    rng = np.random.default_rng(SEED + 2)
    count = int(W * n * 0.0011)
    sx, sy = rng.uniform(0, W, count), rng.uniform(0, n, count)
    br = rng.uniform(0, 1, count) ** 3
    stars = np.zeros((n, W))
    for px, py, b in zip(sx, sy, br):
        ix, iy = int(px), int(py)
        stars[iy, ix] += 40 + 200 * b
        if b > 0.5:
            for ddx, ddy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if 0 <= ix + ddx < W and 0 <= iy + ddy < n:
                    stars[iy + ddy, ix + ddx] += 50 * b
    rgb += stars[..., None] * np.array((0.86, 0.86, 1.0))
    # the far dead fleet: few in section 1, more in 2, densest in 3, thinning in 4, almost none in 5
    dens = {0: 6, 1: 14, 2: 10, 3: 9, 4: 3}
    for i in range(len(SECTIONS)):
        a = seam("deep", i) if i else lo
        b = seam("deep", i + 1) if i + 1 < len(SECTIONS) else hi
        span = b - a
        for _ in range(int(dens.get(i, 4) * span / 700)):
            cr = rng.uniform(a, b)
            cx = rng.uniform(-20, W + 20)
            far_hull(rgb, rows, x, cx, cr, rng.uniform(10, 34) * (1.3 if i == 2 else 1.0), rng.uniform(2.5, 6.5),
                     rng.uniform(-1.2, 1.2), rng.uniform() < 0.4, rng.uniform(0.7, 1.1))
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


WF = 96
WN = (640, 480)
WN_R = 760.0


def world(w, h, cx, cy, r, seed, cracks=1.0, ss=2):
    """The withered grey world: a dark grey-violet disc lit from the top left, its surface cracked and pale."""
    x, y = grid(w, h, ss)
    dx, dy = (x - cx) / r, (y - cy) / r
    q = dx * dx + dy * dy
    inside = q < 1
    nz = np.sqrt(np.clip(1 - q, 0, 1))
    lam = np.clip(-0.55 * dx - 0.62 * dy + 0.56 * nz, 0, 1)
    tex = raster.fbm(w * ss, h * ss, max(8, int(r * ss / 5)), seed, octaves=4, period=False)
    crk = raster.fbm(w * ss, h * ss, max(6, int(r * ss / 9)), seed + 1, octaves=3, period=False)
    lines = np.exp(-((crk - 0.5) / 0.018) ** 2) * cracks
    base = WORLD_K * (0.18 + 0.82 * lam[..., None]) * (0.82 + 0.3 * (tex[..., None] - 0.5))
    base = base + lines[..., None] * np.array((150.0, 150.0, 170.0)) * (0.25 + 0.75 * lam[..., None]) * 0.35
    limb = np.exp(-((1 - np.sqrt(np.clip(q, 0, 1))) / 0.02) ** 2) * (lam > 0.05)
    base = base + limb[..., None] * np.array((90.0, 92.0, 120.0)) * 0.3
    base = base + (1 - lam[..., None]) * DUST * 0.3
    a = inside.astype(float)
    rgb = base.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    a = a.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return rgba(np.clip(rgb, 0, 255), a)


def world_far():
    return world(WF, WF, WF / 2, WF / 2, 40, SEED + 10, cracks=0.4)


def world_near():
    w, h = WN
    return world(w, h, w / 2 + 60, 20 + WN_R, WN_R, SEED + 11, cracks=1.0)


# =========================================================================== build the backdrop

def build_fleet(res):
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    save("world-far", world_far())
    save("world-near", world_near())
    i = 0
    for name in HULK_NAMES:
        frames = crop_set([grade(f, **SCENERY_GRADE) for f in res[i:i + ROCK_N]])
        i += ROCK_N
        for j, k in enumerate(ROCK_SEQ):
            save(f"{name}_{j}", frames[k])
    hull = grade(res[i], contrast=0.85, pivot=70.0, lift=8.0)
    save("warm-hull", hull)
    for j, g in enumerate(warm_hull_glow(hull)):
        save(f"warm-hull-glow_{j}", g)
    save("helix-wreck", grade(res[i + 1], **SCENERY_GRADE))
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


HULK_NAMES = ("hulk-a", "hulk-b", "hulk-c", "hulk-d")


def fleet_jobs():
    return [("hulk", name, j) for name in HULK_NAMES for j in range(ROCK_N)] + [("warm",), ("helix",)]


def pieces_spec():
    spec = {}
    for name in HULK_NAMES:
        im = Image.open(OUT / f"{name}_0.png")
        spec[name] = {"layer": "low-air", "size": list(im.size), "frames": len(ROCK_SEQ), "fps": ROCK_FPS}
    spec["warm-hull"] = {"layer": "low-air", "size": list(WH)}
    spec["warm-hull-glow"] = {"layer": "low-air", "size": list(WH), "frames": WH_GLOW_N, "fps": 2}
    spec["helix-wreck"] = {"layer": "low-air", "size": list(HX)}
    spec["world-far"] = {"layer": "deep", "size": [WF, WF]}
    spec["world-near"] = {"layer": "deep", "size": list(WN)}
    return spec


WARM_T = 188.4          # the warm hull's centre at mid-screen (enters at t~184, on the ground's 90 px/s)
WARM_X = 230
HELIX_T, HELIX_X = 88.0, 110
# the low-air hulks (time at mid-screen, x, hulk, sideways drift px/s): s1 scattered; s2 two loose lanes; s3 the
# densest, torn open; s4 the lanes breaking up; s5 thinning out
HULK_PLAN = [
    (5, 380, "hulk-b", -4), (13, 90, "hulk-a", 3), (24, 330, "hulk-d", 0), (33, 140, "hulk-c", -3), (43, 400, "hulk-a", 2),
    (52, 80, "hulk-c", 2), (57, 400, "hulk-a", -2), (63, 70, "hulk-d", 3), (69, 410, "hulk-b", -3), (75, 90, "hulk-a", 0),
    (81, 390, "hulk-c", -2), (94, 400, "hulk-d", -3), (99, 80, "hulk-b", 2), (105, 400, "hulk-a", 0),
    (111, 90, "hulk-c", 3), (116, 380, "hulk-d", -2),
    (121, 120, "hulk-a", 4), (123, 380, "hulk-c", -3), (126.5, 250, "hulk-b", 0), (129.5, 90, "hulk-d", 3),
    (132, 400, "hulk-a", -4), (135, 200, "hulk-c", 2), (137.5, 350, "hulk-d", -2), (140.5, 110, "hulk-b", 3),
    (143, 330, "hulk-a", -3),
    (150, 260, "hulk-c", 4), (158, 70, "hulk-a", 3), (166, 410, "hulk-b", -4), (174, 140, "hulk-d", 5),
    (198, 380, "hulk-c", -3), (205, 90, "hulk-a", 2), (212, 300, "hulk-d", -5),
    (220, 80, "hulk-b", 3), (236, 150, "hulk-c", 2), (250, 380, "hulk-a", -2),
]


def warm_path():
    """The warm hull on low-air moving at the ground's 90 px/s: dy (up) grows at (1.3 - 1) x 90 px/s."""
    rate = (FACTORS["low-air"] - 1.0) * BASE_SPEED
    return [[round(WARM_T - 14, 3), 0, round(-14 * rate, 1)], [round(WARM_T + 14, 3), 0, round(14 * rate, 1)]]


WORLD_NEAR_T0, WORLD_NEAR_LIMB = 214.0, (560.0, 250.0)     # its limb's screen y at t=214 and at the end


def world_near_path():
    """Waypoints (t, dx, dy up) placing world-near's limb at screen y ``limb(t)``: held below the bottom edge until
    t=214, then rising to y 250 by the end (the deep layer's scroll is undone by the path)."""
    f = FACTORS["deep"]
    t_end = END + 4

    def dy(t, limb):
        centre = limb + WN[1] / 2 - 20
        return round(SCREEN - SCREEN / 2 - centre - (scroll(WORLD_NEAR_T0) - scroll(t)) * f, 1)
    return [[0.0, 0, dy(0.0, WORLD_NEAR_LIMB[0])], [WORLD_NEAR_T0, 0, dy(WORLD_NEAR_T0, WORLD_NEAR_LIMB[0])],
            [t_end, 0, dy(t_end, WORLD_NEAR_LIMB[1])]]


def placements():
    out = []
    fl, fd = FACTORS["low-air"], FACTORS["deep"]
    out.append({"piece": "world-far", "t": CLOCK.t_centre_at(5.0, 80.0, fd), "x": 330,
                "note": "section 1: the withered world small and far (deep)"})
    # world-near rises from below the bottom edge (its lower part never shows): its limb (20 px under its top) held
    # at y 560 until t=214, then rising to y 250 by the end
    out.append({"piece": "world-near", "t": WORLD_NEAR_T0, "x": 300, "path": world_near_path(),
                "note": "section 5: the world filling the lower sky, rising from below the bottom edge from t=214"})
    for k, (t, x, name, vx) in enumerate(HULK_PLAN):
        p = {"piece": name, "t": CLOCK.t_centre_at(t, 270.0, fl), "x": x,
             "note": "low-air: the hollowed hulks, rocking slowly (2.5 fps)" if k == 0 else None}
        if vx:
            p["path"] = [[round(t - 6, 3), round(-6 * vx, 1), 0], [round(t + 6, 3), round(6 * vx, 1), 0]]
        out.append(p)
    out.append({"piece": "helix-wreck", "t": CLOCK.t_centre_at(HELIX_T, 270.0, fl), "x": HELIX_X,
                "note": f"secret 1: the Helix wreck (the hatch trigger at {HATCH_XY[0]:+g}, {HATCH_XY[1]:+g} px from "
                        "its centre, y down; D2: a low-air trigger on its scroll)"})
    t_w = CLOCK.t_centre_at(WARM_T, 270.0, fl)
    cx, cy = core_xy()
    out.append({"piece": "warm-hull", "t": t_w, "x": WARM_X, "path": warm_path(),
                "note": f"section 4: the warm hull, at the ground's 90 px/s (its path); the core at {cx:+g}, {cy:+g} px "
                        "from its centre (y down), the sinews at the core + SINEWS"})
    out.append({"piece": "warm-hull-glow", "t": t_w, "x": WARM_X, "path": warm_path(),
                "note": "its core's warm light (additive; D4 fades it out as the core rises)"})
    return out


def hatch_xy():
    """The craft's hatch (craft coords (0, 0.12)) from the Helix wreck piece's centre, px (y down)."""
    q = rotate_z(np.array([[0.0, 0.12, 0.0]]), -CRAFT_TURN)[0]
    x, y = to_screen(q[0] + CRAFT_AT[0], q[1] + CRAFT_AT[1], HX_HEAD, HK_PX)
    return round(x, 1), round(y, 1)


HATCH_XY = hatch_xy()


def fmt(v):
    return reef.fmt(v)


def proposal_text():
    src = "the level's data.yaml" if DATA and DATA.get("sections") else "the README's Layout table"
    cx, cy = core_xy()
    lines = ["backdrop:",
             f"  # Presentation only: Level 46's dead fleet (tools/art/act7_deadfleet.py fleet, step ART-D, sections from {src}).",
             "  # Vacuum: no haze. No ground layer. Deep: one continuous field (near-black violet, sparse stars, the far",
             "  # dead fleet) cut at the sections' seams (re-run the generator after moving a section); the world small in",
             "  # section 1 (world-far) and filling the lower sky in section 5 (world-near, on a path rising against the",
             "  # layer). Low-air: the hollowed hulks (16 frames: 9 turns forth and back), the Helix wreck, the warm hull.",
             "  # D4 hooks: (1) the starless patch is code-drawn (Level 46 Layout: x 360, 150 px down, r ~60, a smooth",
             "  # edge), after the deep pieces and before low-air; (2) the warm hull and its glow move with the ground's",
             "  # scroll (their path), the core (story `vrell-core`) and the four sinews are sprites drawn on them;",
             "  # warm-hull-glow fades out as the core rises; (3) the dying Leviathan is drawn as `dying-leviathan` (the",
             "  # graded copy), with `dying-leviathan-cross-glow` added in a slow pulse (k(t) <= 0.25 Hz).",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, ground, high-air unused",
             "  ramp: 2",
             "  haze_colour: 06050a",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        h = Image.open(OUT / f"{name}.png").height
        lines.append(f"    {name}: {{layer: deep, height: {h}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    lines.append("  pieces:")
    for name, spec in pieces_spec().items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placements():
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        extra = f", path: {fmt(p['path'])}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {fmt(p['x'])}{extra}}}")
    lines.append("sections:   # ART-D's reading of the README's Layout (90 px/s throughout); D2 writes the real ones")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    t_w = WARM_T
    lines.append("# For D2 (the warm hull's objects, on the ground's scroll: `pinned`): the hull's centre crosses the")
    lines.append(f"# middle of the screen (y 270) at t={t_w:g}, x={WARM_X}; the core at x={WARM_X + cx:g}, "
                 f"{cy:+g} px from the hull's centre (y down);")
    lines.append("# the four sinews (`warm-hull-sinew`, 28x28) at the core +")
    for k, (sx, sy) in enumerate(SINEWS):
        lines.append(f"#   sinew {k + 1}: ({sx:+d}, {sy:+d}) -> x={WARM_X + cx + sx:g}, {cy + sy:+g} px from the hull's centre")
    lines.append(f"# the Helix hatch (`helix-hatch`, 24x24) at x={HELIX_X + HATCH_XY[0]:g}, {HATCH_XY[1]:+g} px from the "
                 f"wreck's centre (y down), the wreck's centre at mid-screen at t={HELIX_T:g} on low-air (117 px/s): a")
    lines.append("# trigger that drifts with it needs its own 27 px/s up-drift, or D2 moves the wreck onto the ground's scroll")
    lines.append("# with a path as the warm hull's.")
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


def piece_frame(name, spec, t):
    if spec.get("frames"):
        return img_of(f"{name}_{int(np.floor(t * spec['fps'])) % spec['frames']}")
    return img_of(name)


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


def patch(img):
    """The review's stand-in for D4's code-drawn starless patch: the deep's base colour over a soft disc."""
    a = np.array(img).astype(np.float64)
    x, y = grid(W, SCREEN)
    m = smoothstep(84.0, 36.0, np.hypot(x - 360, y - 150))
    a[..., :3] = a[..., :3] * (1 - m[..., None]) + SPACE * m[..., None]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def compose(t, units=True, warm_k=None):
    spec = pieces_spec()
    placed = placements()
    img = deep_layer(t)
    for p in placed:
        if spec[p["piece"]]["layer"] == "deep":
            x, y = piece_xy(p, "deep", t)
            img = l23kit.paste_c(img, img_of(p["piece"]), x, y)
    img = patch(img)
    for p in placed:
        name = p["piece"]
        if spec[name]["layer"] != "low-air":
            continue
        x, y = piece_xy(p, "low-air", t)
        fr = piece_frame(name, spec[name], t)
        if y < -fr.height or y > SCREEN + fr.height:
            continue
        if name == "warm-hull-glow":
            img = l23kit.add_c(img, fr, x, y, 1.0 if warm_k is None else warm_k)
        else:
            img = l23kit.paste_c(img, fr, x, y)
        if name == "warm-hull" and units:
            img = core_on(img, x, y, t)
        if name == "helix-wreck" and units:
            img = sprite_on(img, "helix-hatch", 0, x + HATCH_XY[0], y + HATCH_XY[1])
    if units:
        img = stand_ins(img, t)
    return img


def sprite_on(img, name, k, x, y):
    try:
        return l23kit.paste_c(img, load(name)[k], x, y)
    except FileNotFoundError:
        return img


def core_on(img, x, y, t):
    cx, cy = core_xy()
    try:
        core = load("vrell-core")
    except FileNotFoundError:
        return img
    img = l23kit.paste_c(img, core[int(t * 2) % len(core)], x + cx, y + cy)
    for sx, sy in SINEWS:
        img = sprite_on(img, "warm-hull-sinew", 0, x + cx + sx, y + cy + sy)
    return img


HUSK_PLAN = [(10, -50, 140, 18, 30, 0, 14), (30, 530, 300, -30, 4, 1, -10), (55, 300, -50, 6, 30, 2, 20),
             (118, -50, 80, 30, 6, 0, 12), (118, 230, -60, 4, 26, 1, -14), (128, 530, 380, -28, 8, 2, 18),
             (160, 530, 160, -34, 12, 0, -16), (230, 360, -50, -4, 32, 1, 10)]


def stand_ins(img, t):
    """The Husks drifting (the review's stand-in for the waves: D2 sets the real ones), the caskets, the ship."""
    try:
        sets = [load(n) for n in SHAPES]
        ship = load("ship")
        casket = load("seed-casket")[0]
    except FileNotFoundError:
        return img
    for t0, x0, y0, vx, vy, s, w in HUSK_PLAN:
        dt = t - t0
        x, y = x0 + vx * dt, y0 + vy * dt
        if 0 <= dt < 40 and -60 < x < 540 and -60 < y < 600:
            img = l23kit.paste_c(img, sets[s][int(round(w * dt / 11.25)) % HU_N], x, y)
    for tc, xc in ((30, 300), (66, 160), (100, 330), (160, 120), (205, 250), (238, 340)):
        dt = t - tc
        if -1 < dt < 12:
            img = l23kit.paste_c(img, casket, xc + 6 * dt, -20 + 22 * (dt + 1))
    return l23kit.paste_c(img, ship[2] if len(ship) > 2 else ship[0], 240, 470)


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


def bg_cell(w, h, t=128.0, x0=0, y0=0):
    """A cell of Level 46's deep at time t (the review's background)."""
    try:
        return deep_layer(t).crop((x0, y0, x0 + w, y0 + h))
    except FileNotFoundError:
        return Image.new("RGBA", (w, h), (8, 6, 12, 255))


def strip(frames, cell, scale=2, add=None, under=None, step=6, t=128.0):
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


def readability():
    """The Husks beside the scenery hulks at 1:1, over the deep, the brightest hulk behind one."""
    w, h = 480, 200
    c = bg_cell(w, h, t=130.0)
    c = l23kit.paste_c(c, img_of("hulk-b_0"), 120, 100)
    c = l23kit.paste_c(c, img_of("hulk-a_4"), 370, 100)
    sets = [load(n) for n in SHAPES]
    c = l23kit.paste_c(c, sets[0][5], 150, 100)
    c = l23kit.paste_c(c, load("husk-cracked")[20], 300, 70)
    c = l23kit.paste_c(c, sets[2][12], 420, 150)
    c = l23kit.paste_c(c, sets[1][28], 40, 150)
    for k, (bx, by) in enumerate(((200, 60), (214, 74), (228, 88), (90, 110), (102, 98))):
        c = l23kit.paste_c(c, load("husk-shard")[(k * 3) % 16], bx, by)
    c = l23kit.paste_c(c, load("husk-orb")[0], 330, 150)
    c = l23kit.paste_c(c, load("husk-orb")[1], 120, 60)
    return c


def review_husk():
    sets = {n: load(n) for n in SHAPES}
    cr = {n: load(n + "-cracked") for n in SHAPES}
    fl, sl, de = load("husk-flare"), load("husk-slabs"), load("husk-death")
    rows = []
    for n in SHAPES:
        rows.append((f"{n.upper()} WHOLE, 8 OF 32 HEADINGS (EVERY 4TH), 1X", strip(sets[n][::4], (HU, HU), 1)))
    rows.append(("CRACKED, THE SAME HEADINGS (SHAPE 1), AND THE TELL: CRACKED + FLARE 0-7 (4 STEPS A FRAME), 1X",
                 strip(cr["husk"][::4], (HU, HU), 1)))
    rows.append(("THE TELL", strip([cr["husk"][6]] * 8, (FLARE, FLARE), 1, add=fl)))
    rows.append(("THE COLLAPSE: SLABS + DEATH GLOW, 16 FRAMES (EVERY 2ND), 1X", strip(sl[::2], (SLAB, SLAB), 1,
                                                                                   add=de[::2])))
    shards = strip(load("husk-shard")[::2] + load("husk-orb"), (SHARD, SHARD), 3)
    rows.append(("SHARD (8 OF 16 HEADINGS) AND THE DEATH RING'S ORB (4 FRAMES), 3X", shards))
    try:
        portrait = Image.open(ROOT / "assets" / "sprites" / "intel" / "husk.png").convert("RGBA")
        rows.append(("INTEL PORTRAIT 30X30, 1X AND 3X", strip([portrait, portrait.resize((90, 90), Image.NEAREST)],
                                                              (96, 96), 1)))
    except FileNotFoundError:
        pass
    rows.append(("READABILITY 1:1: HUSKS (DARKER, HARDER) BESIDE THE PALER, FLATTER SCENERY HULKS, SHARDS, ORBS",
                 readability()))
    sheet = sheet_of("HUSK (FINAL R91): A PUNCTURED, 3 SHAPES X 32 HEADINGS", rows)
    frames = []
    for i in range(48):
        t = 120.0 + i * 0.25
        img = compose(t, units=False)
        for j, (x0, y0, vx, vy, s, w) in enumerate(((60, 120, 10, 26, 0, 14), (330, 60, -14, 30, 1, -12),
                                                    (200, 300, 6, 18, 2, 20))):
            dt = i * 0.25
            k = int(round(w * dt / 11.25)) % HU_N
            x, y = x0 + vx * dt, y0 + vy * dt
            if j == 0 and i >= 20:
                f = i - 20
                if f < 8:
                    img = l23kit.paste_c(img, cr["husk"][k], x, y)
                    img = l23kit.add_c(img, fl[f], x, y)
                elif f < 24:
                    img = l23kit.paste_c(img, sl[f - 8], x, y)
                    img = l23kit.add_c(img, de[f - 8], x, y)
                continue
            img = l23kit.paste_c(img, sets[SHAPES[s]][k], x, y)
        frames.append(img)
    artkit.save_review(sheet, frames, HUSK_C, "husk", fps=10)


SCREENS = (6.0, 40.0, 88.0, 128.0, 140.0, 188.0, 230.0, 262.0)


def review_fleet():
    shots = [compose(t) for t in SCREENS]
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * 290 + 30 + 360, "LEVEL 46 BACKDROP (FINAL R91): GRAVEYARD OF THE CHOIR",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, (t, s) in enumerate(zip(SCREENS, shots)):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        sec = next((q["name"] for q in SECTIONS if t < float(q["end"])), SECTIONS[-1]["name"])
        raster.draw_text(sheet, x, y, f"T={t:g} S ({sec.upper()})", raster.LABEL)
        sheet.alpha_composite(s.resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + 2 * 290 + 6
    raster.draw_text(sheet, 12, y, "SCREENS AT HALF SIZE WITH STAND-INS (HUSKS, CASKETS, THE SHIP); THE STARLESS PATCH "
                                   "IS D4'S CODE (A STAND-IN HERE). PIECES AT HALF SIZE:", raster.LABEL)
    x = 12
    for name in ("hulk-a_0", "hulk-b_0", "hulk-c_0", "hulk-d_0", "helix-wreck", "warm-hull", "world-far", "world-near"):
        im = img_of(name)
        bg = Image.new("RGBA", im.size, (8, 6, 12, 255))
        bg.alpha_composite(im)
        if name == "warm-hull":
            bg = l23kit.add_c(bg, img_of("warm-hull-glow_0"), im.width / 2, im.height / 2)
        sc = 2 if name != "world-near" else 4
        sm = bg.resize((max(1, im.width // sc), max(1, im.height // sc)), Image.LANCZOS)
        sheet.alpha_composite(sm, (x, y + 14))
        x += sm.width + 8
    frames = [compose(t).resize((240, 270), Image.LANCZOS) for t in np.arange(0, END, 4.0)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=4)


def review_props():
    rows = []
    ck = [load("seed-casket")[0], load("seed-casket-broken")[0]]
    rows.append(("SEED CASKET 40X28: WHOLE, BROKEN; ITS TATTERS + TEAL BREATH (8 FRAMES), 2X", strip(ck, (64, 48), 2)))
    rows.append(("", strip(load("seed-casket-tatters"), (64, 64), 2, add=load("seed-casket-death"))))
    core = load("vrell-core")
    rows.append(("THE CORE IN ITS SINEWS (8 BREATH FRAMES, EVERY 2ND), THE KNOTS DRAWN AT SINEWS, 2X",
                 strip([knotted(f) for f in core[::2]], SHUT, 2)))
    rows.append(("CUT: THE RISE (10, EVERY 2ND) AND LOOSE (DRIFT 0, 4) IN THEIR GLOW, 2X",
                 strip([None] * 7, (128, 128), 1, under=[r for r in load("vrell-core-rise")[1::2]]
                       + [load("vrell-core-drift")[0], load("vrell-core-drift")[4]],
                       add=load("vrell-core-glow")[1:10:2] + [load("vrell-core-glow")[10], load("vrell-core-glow")[13]])))
    rows.append(("SINEW KNOT: WHOLE, HIT, CUT; ITS TEAR (8 FRAMES), 3X",
                 strip(load("warm-hull-sinew") + load("warm-hull-sinew-break")[::2], (48, 48), 2)))
    wh = img_of("warm-hull")
    c = bg_cell(*wh.size, t=188.0)
    c.alpha_composite(wh)
    c = l23kit.add_c(c, img_of("warm-hull-glow_0"), wh.width / 2, wh.height / 2)
    c = core_on(c, wh.width / 2, wh.height / 2, 0.0)
    hx_ = img_of("helix-wreck")
    c2 = bg_cell(*hx_.size, t=88.0)
    c2.alpha_composite(hx_)
    c2 = sprite_on(c2, "helix-hatch", 0, hx_.width / 2 + HATCH_XY[0], hx_.height / 2 + HATCH_XY[1])
    both = Image.new("RGBA", (wh.width + hx_.width + 12, max(wh.height, hx_.height)), (0, 0, 0, 0))
    both.alpha_composite(c, (0, 0))
    both.alpha_composite(c2, (wh.width + 12, 0))
    rows.append(("THE WARM HULL (1X) WITH ITS CORE, SINEWS AND WARM LIGHT; THE HELIX WRECK WITH ITS HATCH", both))
    rows.append(("HELIX HATCH: SHUT, DENTED, BLOWN OPEN; ITS BREAK (EVERY 2ND), 2X",
                 strip(load("helix-hatch") + load("helix-hatch-break")[::2], (48, 48), 2)))
    rows.append(("VORNE'S COURIER: 16 TUMBLE FRAMES (EVERY 2ND) AND ITS BREAK (EVERY 2ND), 2X",
                 strip(load("debris-vorne-courier")[::2] + load("debris-vorne-courier-break")[::2], (64, 64), 1)))
    sheet = sheet_of("LEVEL 46 PROPS (FINAL R91)", rows)
    frames = []
    cut = [184.6 + 1.2 * i for i in range(4)]
    for i in range(70):
        t = 183.0 + i * 0.15
        img = compose(t, units=False, warm_k=1.0 if t < cut[-1] else max(0.0, 1 - (t - cut[-1]) / 1.5))
        p = [q for q in placements() if q["piece"] == "warm-hull"][0]
        x, y = piece_xy(p, "low-air", t)
        cx, cy = core_xy()
        if t < cut[-1]:
            img = l23kit.paste_c(img, load("vrell-core")[int(t * 2) % N_SHUT], x + cx, y + cy)
        else:
            u = (t - cut[-1]) / 2.0
            if u < 1:
                r = load("vrell-core-rise")[min(N_RISE - 1, int(u * N_RISE))]
                img = l23kit.paste_c(img, r, x + cx, y + cy - 30 * u)
                img = l23kit.add_c(img, load("vrell-core-glow")[min(N_RISE - 1, int(u * N_RISE))], x + cx,
                                   y + cy - 30 * u)
            else:
                d = load("vrell-core-drift")[int(t * 8) % N_DRIFT]
                img = l23kit.paste_c(img, d, x + cx, y + cy - 30)
                img = l23kit.add_c(img, load("vrell-core-glow")[10 + int(t * 6) % 6], x + cx, y + cy - 30)
        for k, (sx, sy) in enumerate(SINEWS):
            st = 0 if t < cut[k] else 2
            img = sprite_on(img, "warm-hull-sinew", st, x + cx + sx, y + cy + sy)
            if cut[k] <= t < cut[k] + 0.5:
                img = sprite_on(img, "warm-hull-sinew-break", int((t - cut[k]) / 0.5 * 8), x + cx + sx, y + cy + sy)
        img = l23kit.paste_c(img, load("ship")[2], 240, 470)
        frames.append(img)
    artkit.save_review(sheet, frames, CONCEPT, "l46-props", fps=10)


def knotted(core):
    c = Image.new("RGBA", SHUT, (0, 0, 0, 0))
    c.alpha_composite(core)
    for sx, sy in SINEWS:
        c = sprite_on(c, "warm-hull-sinew", 0, SHUT[0] / 2 + sx, SHUT[1] / 2 + sy)
    return c


def review_leviathan():
    orig, dying = load("leviathan-cross"), load("dying-leviathan-cross")
    glow = load("dying-leviathan-cross-glow")[0]
    w, h = orig[0].size
    cells = []
    for img, g in ((orig[0], None), (dying[0], None), (dying[0], glow)):
        c = bg_cell(w, h, t=130.0)
        c = blend(c, img, 0.75)
        if g is not None:
            c = l23kit.add_c(c, g, w / 2, h / 2)
        cells.append(c)
    row = Image.new("RGBA", (3 * w + 24, h), (0, 0, 0, 0))
    for i, c in enumerate(cells):
        row.alpha_composite(c, (i * (w + 12), 0))
    parts = [load(n)[0] for n in ("leviathan-down", "dying-leviathan-down")]
    pr = Image.new("RGBA", (2 * 300 + 12, 480), (0, 0, 0, 0))
    for i, p in enumerate(parts):
        c = bg_cell(300, 480, t=130.0)
        c.alpha_composite(p)
        pr.alpha_composite(c, (i * 312, 0))
    ch = strip([load("leviathan-chunk-2")[0], load("dying-leviathan-chunk-2")[0]], (120, 150), 1)
    rows = [("THE FIRST PASS AT 75 %: L03'S FRAME, THE DYING GRADE, THE GRADE WITH ITS BLOWHOLE GLOW AT ITS PEAK", row),
            ("THE SECOND PASS'S BODY (UNUSED ON L46, GRADED FOR SETPIECELOOKS) AND A CHUNK: L03, DYING", pr), ("", ch)]
    sheet = sheet_of("THE DYING LEVIATHAN (FINAL R91)", rows)
    frames = []
    for i in range(40):
        t = 122.0 + i * 0.5
        img = compose(t, units=False)
        u = (t - 122.0) / 20.0
        x, y = -265 + (846 + 265) * u, -150 + (544 + 150) * u
        k = 0.5 - 0.5 * np.cos(TAU * 0.2 * (t - 122.0))
        img = blend(img, dying[int(t / 0.3) % 3], 0.75, (x, y))
        img = l23kit.add_c(img, glow, x, y, k * 0.75)
        frames.append(img.resize((240, 270), Image.LANCZOS))
    artkit.save_review(sheet, frames, LEV_C, "leviathan-dying", fps=8)


def blend(base, img, k, at=None):
    """``img`` over ``base`` at opacity k (centred on ``at``, else at the origin)."""
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    if at is None:
        layer.alpha_composite(img)
    else:
        layer = l23kit.paste_c(layer, img, at[0], at[1])
    a = np.array(layer)
    a[..., 3] = (a[..., 3] * k).astype(np.uint8)
    out = base.copy()
    out.alpha_composite(Image.fromarray(a, "RGBA"))
    return out


# =========================================================================== check

SETS = {"husk": [*[(n, HU_N, (HU, HU)) for n in SHAPES], *[(n + "-cracked", HU_N, (HU, HU)) for n in SHAPES],
                 ("husk-flare", 8, (FLARE, FLARE)), ("husk-slabs", 16, (SLAB, SLAB)), ("husk-death", 16, (SLAB, SLAB)),
                 ("husk-shard", 16, (SHARD, SHARD)), ("husk-orb", 4, (ORB, ORB))],
        "casket": [("seed-casket", 1, CK), ("seed-casket-broken", 1, CK), ("seed-casket-tatters", 8, (64, 64)),
                   ("seed-casket-death", 8, (64, 64))],
        "warm": [("vrell-core", N_SHUT, SHUT), ("vrell-core-rise", N_RISE, RISE), ("vrell-core-drift", N_DRIFT, RISE),
                 ("vrell-core-glow", 16, (128, 128)), ("warm-hull-sinew", 3, (28, 28)),
                 ("warm-hull-sinew-break", 8, (48, 48))],
        "wrecks": [("helix-hatch", 3, (24, 24)), ("helix-hatch-break", 8, (48, 48)),
                   ("debris-vorne-courier", 16, (40, 40)), ("debris-vorne-courier-break", 8, (64, 64))],
        "leviathan": [("dying-leviathan-cross", 3, None), ("dying-leviathan-down", 3, None),
                      ("dying-leviathan-blowhole-glow", 1, None), ("dying-leviathan-ichor", 16, None),
                      ("dying-leviathan-cross-glow", 1, None), *[(f"dying-leviathan-chunk-{i}", 3, None)
                                                                 for i in range(1, 6)]]}


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
            ok = len(frames) == n and (size is None or all(fr.size == size for fr in frames)) and src \
                and share < 0.05 and not lit
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
            ok &= ws < 3.0 and np.array(im)[..., 3].min() == 255
            msg.append(f"wrap {ws:.2f}")
        base = stem.split("_")[0]
        if base.endswith("-glow"):
            lit = lit_border(im)
            ok &= lit == 0
            msg.append(f"edge {lit}")
        if base in spec:
            ok &= list(im.size) == spec[base]["size"]
        bad += not ok
        if not ok or "_" not in stem or stem.endswith("_0"):
            print(f"{'ok ' if ok else 'BAD'} {p.name}: {im.size}, checker {share * 100:.1f} %, {', '.join(msg)}")
    return bad == 0


# =========================================================================== jobs and main

JOBS = {"husk": _husk, "casket": lambda b: _casket(b), "core": _core, "knot": _knot, "hatch": _hatch,
        "courier": _courier, "hulk": _hulk, "warm": _warm_hull, "helix": _helix_wreck}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "act7-deadfleet"
# at most 6 render workers (the user's machine stays usable; run it under ``nice -n 19``); ART_WORKERS overrides
WORKERS = max(1, min(int(os.environ.get("ART_WORKERS", "6")), os.cpu_count() or 1))


def _model_hash(kind):
    text = Path(__file__).read_text(encoding="utf-8")
    marks = {"husk": ("the Husk", "the seed casket"), "casket": ("the seed casket", "the warm core"),
             "core": ("the warm core", "the scenery hulks"), "knot": ("the warm core", "the scenery hulks"),
             "hulk": ("the scenery hulks", "the warm hull"), "warm": ("the scenery hulks", "the Helix wreck"),
             "helix": ("the scenery hulks", "the dying Leviathan"), "hatch": ("the Helix wreck", "the dying Leviathan"),
             "courier": ("the Helix wreck", "the dying Leviathan")}[kind]
    rule = "# =========================================================================== "
    a, b = text.index(rule + marks[0]), text.index(rule + marks[1])
    return hashlib.sha1((text[a:b] + repr(u84.husk_model.__code__.co_code)).encode()).hexdigest()[:12]


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


PARTS = ("husk", "casket", "warm", "wrecks", "leviathan", "fleet")

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    parts = [a for a in args if a in PARTS] or list(PARTS)
    if "--review" not in args:
        makers = {"husk": husk_jobs, "casket": lambda: [("casket", False), ("casket", True)], "warm": core_jobs,
                  "wrecks": wreck_jobs, "fleet": fleet_jobs}
        jobs, spans = [], {}
        for part in PARTS:
            if part in parts and part in makers:
                js = makers[part]()
                spans[part] = (len(jobs), len(jobs) + len(js))
                jobs += js
        res = run(jobs) if jobs else []
        builders = {"husk": build_husk, "casket": build_casket, "warm": build_core, "wrecks": build_wrecks,
                    "fleet": build_fleet}
        for part, (a, b) in spans.items():
            builders[part](res[a:b])
        if "leviathan" in parts:
            build_leviathan()
        if "fleet" in parts:
            write_proposal()
    if "fleet" in parts:
        review_fleet()
    if "husk" in parts:
        review_husk()
    if "warm" in parts or "casket" in parts or "wrecks" in parts:
        review_props()
    if "leviathan" in parts:
        review_leviathan()
