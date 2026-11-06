#!/usr/bin/env python3
"""Production art: Level 45 "Hive Reef" (design/campaign/act-7-beyond-the-gate/level-45-hive-reef; M11 part C batch,
concept round 90, step ART-C): the hive-reef backdrop kit, the spawning organ (round 84 A.5 b "reef polyp"), the call
organ and its four roots, the courier's cyst and the spore cloud's production art (replacing part A's stand-in from
tools/art/act7_gear.py, same keys and sizes, so the game's SporeLooks picks it up unchanged).

Everything is in round 84 Q10 b's **one Vrell palette** with Level 45's light (design/world/vrell-space, *Light per
level*: teal-black and violet chitin, glowing mint-teal veins, the pits' glow, violet spore clouds; the reef a step
darker than the units; veins pulse slowly and are never bullet-sized dots). The organ is round 84's model
(tools/concept/enemies_r84.py ``organ_model("b")``, imported unchanged) recoloured into the palette and rendered on the
production path (artkit: 8x up to 64 px, 4x above, 1-bit alpha); the reef's spore sacs are Level 43's
(tools/art/backdrop_l43.py ``spore_sac``), its nebula round 84's (tools/concept/act7_r84.py ``space_rgb``). Every glow,
haze and veil is a float field written unquantised (far more than 32 steps, no ordered dither: DitherGuardTest's
measure) and fades to 0 inside its canvas (``--check`` fails on a lit border pixel of an additive set; the reef's cut
ground pieces, which abut their tiles row for row, are the stated exception for their ``-veins``).

Sprites (assets/sprites/ once the data claims the root, else build/art-held/sprites/: belt_units.py's rule; ``additive``
= premultiplied on black, GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha; steps = game steps of 1/60 s):

  The spawning organ (ground, medium, 80x80; the Hive Node's frame conventions, EnemyLooks' periodic spawner)
  spawning-organ_0..23        80x80 normal: the reef polyp, a tall ribbed violet sac on nine roots, six pale mint seed
                              pods round its crown iris; 6 iris states x 4 breath frames, indexed iris * 4 + breath
                              (iris 0 shut, 1-4 opening 0.2..0.8, 5 open: the Skitter pair's 0.5 s telegraph; breath
                              0, 0.4, 1, 0.6 of a slow swell)
  spawning-organ-glow_0..23   80x80 additive, same order: the veins, the root veins and the iris throat, with a soft
                              mint halo over the iris growing as it opens
  spawning-organ-swell_0..5   80x80 normal: the seed pods' 0.5 s swell before a Whirl Seed cluster (5 steps a frame)
  spawning-organ-swell-glow_0..5  80x80 additive: the pods lighting up mint as they swell
  spawning-organ-release_0..5 80x80 normal: the pods bursting open (the cluster's release) and shrinking back, 0.3 s
                              (3 steps a frame), then the breath frames again
  spawning-organ-release-glow_0..5  80x80 additive: the burst's mint flare in the pods, decaying
  spawning-organ-tatters_0..15  128x128 normal: the death: the sac tearing and collapsing into its husk, the six pods
                              and flesh scraps flung out and shrinking (4 steps a frame); its last frame is the husk
  spawning-organ-death_0..15  128x128 additive: a short mint-white flash and a ring of ichor spreading and fading (vacuum:
                              it spreads, never rises), ichor droplets flung out
  spawning-organ-stump        80x80 normal (single): the torn husk left on the plates (the remains' 10 s), dark
  spawning-organ-creep_0..8   128x128 normal: the root veins spreading into the plates round it; 0 alive (mint), 1-8
                              going dark outward after the death (EnemyLooks.wither, 15 steps a frame), 8 all dark
  The call organ (Level 45's story pickup ``call-organ``, StoryLooks' frame conventions) and its roots
  call-organ_0..7             96x64 normal: the pale ribbed horn (64x40) rooted on the plates, four tendons to its root
                              knots at (-34, -24), (30, -30), (-34, 24), (30, 26) px from its
                              centre (``ROOTS``: C2 places the four root objects there), the ribs breathing slowly (8 frames, 0.5 s each:
                              one breath in 4 s); no glow
  call-organ-rise_0..9        88x60 normal: the roots cut: the horn lifting off the plates over its 2.0 s rise, seen
                              growing from 1.0 to 1.2, the cut tendon ends hanging
  call-organ-drift_0..7       88x60 normal: loose, drifting at 1.2, the ribs breathing (8 fps: one breath a second)
  call-organ-glow_0..15       128x96 additive: a faint pale violet-white halo round the loose horn: 0..9 the rise
                              (growing), 10..15 the drift's slow pulse (StoryLooks: 6 fps loop)
  call-organ-root_0..2        28x28 normal: a root knot where a tendon dives into the plates: whole, hit (gashed, dark
                              ichor), cut (a severed stump)
  call-organ-root-break_0..7  48x48 normal: the root torn through: tendon shreds and ichor flecks flung out, shrinking
  The courier's cyst (Level 45's secret: a 30 HP ground object)
  courier-cyst_0..2           40x40 normal: a lump of the reef's chitin grown over a gold Ascendancy courier pod, a gold
                              edge showing, no glow: whole, cracked (the gold showing along the splits), broken open
  courier-cyst-break_0..7     64x64 normal: the chitin bursting: chips flung out, gold fragments glinting
  The spore cloud (design/world/vrell-space, Hazards & set pieces; SporeLooks; keys and sizes as part A's stand-in)
  spore-cloud-veil            176x176 normal, a cloud of radius 64 (scaled by r / 64): lobed billows in the palette's
                              dark violet (30, 12, 44), alpha at most 25 % (64)
  spore-cloud_0..7            176x176 additive: the billows' dim violet glow and fine pale motes drifting round (never
                              bullet-sized), a 4 s loop (30 steps a frame)
  spore-cloud-burn_0..9       176x176 additive: the 1 s burn (6 steps a frame): a ragged ember edge eating inward from
                              the rim with sparks drifting off, the cloud's glow left inside it

Backdrop (assets/backdrop/level-45/; rows y-up as the game reads them; ``--proposal`` writes the ``backdrop:`` block,
the sections and the pits' places to concept/backdrop-proposal.yaml: step C2 places them, step C4 wires the hooks):
  deep-1 .. deep-6     ``deep`` 0.15, opaque: the spore nebula (violet and teal) as one continuous field cut at the
                       sections' seams; two far reefs dark in it in section 1 (seen through the rim's gaps)
  rim-plates           ``ground``, 480x960, section 1: chitin plates rising out of the nebula, transparent gaps
                       between them and more of them missing toward the sides (the nebula shows), veins along them
  brood-fields         ``ground``, 480x960, section 2: smaller, darker violet plates packed tight, veins in the seams
  spine                ``ground``, 480x960, section 3: the reef's ridge, a raised backbone of plates down the middle
                       (x 160-320) in vertebra-like segments, lit from the top left
  brood-heart          ``ground``, 480x960, section 5: dark teal plates, the veins at their densest
  <tile>-veins         additive, the same size: the veins' pulsing share (the tiles hold them at about 45 %); **C4:**
                       BackdropAtmosphere adds every ``-veins`` image of the ground (the tiles' and the ground pieces')
                       with one slow smooth k(t) <= 0.5 Hz; not listed in ``tile_sets`` (one ground set a section)
  seam-2, seam-3       ground pieces 480x256 on the section 1/2 and 2/3 ground seams (exact rows): the tiles' own rows
                       blended along a ragged line, so no straight cut shows
  reef-edge            ground 480x352 at the section 3/4 seam: the spine's rows ending in the first reef's ragged edge,
                       falling away; beyond it nothing (the nebula on ``deep``)
  second-reef-1 .. -5  ground 480x872, section 4 (exact rows 17380-21740, no ground tile set in section 4): the second
                       reef, its rim's ragged lip at the bottom, its top blending into the brood heart's tile at the
                       section 4/5 seam
  far-rim              ground 480x480 at the section 5/6 seam: the brood heart's rows ending in the reef's far edge
  brood-well           ground 320x320 (+ ``-glow``, steady): the brood heart's deep well, ribbed walls round a dark
                       throat with a faint mint glow far down
  pit                  ground 72x72 (+ ``-glow``), 24 frames: the level's spawning pits: 0-7 breathing shut (2 fps
                       loop), 8-15 the tell (4 fps: 2 s, the lips glowing and parting; HELD holds frame 15), 16-19
                       open (4 fps loop, the throat lit), 20-23 closing (8 fps: 0.5 s), then 0-7 again. **C4:**
                       BackdropPieces picks the frame by ``Pits``' state (the placed ``pit`` pieces are the level's
                       pits in their order); until then the data's loop plays them all
  pit-shut             ground 72x72 (+ ``-glow``), 8 frames at 2 fps: a scenery pit, breathing, never opening
  bridge               ``low-air`` 1.3, 300x1400: the living bridge of tendon and chitin arching over the gap between
                       the reefs (on the screen t~160-175), a step darker than the units
  spore-sac-a..c       low-air 40x52: Level 43's drifting spore sacs (its generator's ``spore_sac``)
  dead-fleet           ``deep`` 400x170: the first dark hulls of the dead fleet far ahead (Level 46), lit cold from
                       the top left, seen beyond the far rim

Reviews (round 90, ``*-final-r90-a``): the level's concept/ spawning-organ, l45-props (the call organ, its roots, the
cyst) and backdrop; design/world/vrell-space/concept/spore-cloud.
Run: python3 tools/art/act7_reef.py [organ] [call] [cyst] [spore] [reef] [--review | --proposal | --check]
     (no part = all, a few minutes; --review only rebuilds the sheets from the written frames)
"""
import hashlib
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

import artkit
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import additive_rgb, checker_share, cut_tile, rgba, smoothstep, wrap_score

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: the palette and the nebula, imported unchanged)
import enemies_r84 as u84  # noqa: E402  (concept: the organ's model, imported unchanged)
from render import enemy_models as em  # noqa: E402
from render import raster  # noqa: E402
from render.sdf import Material, sd_capsule, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "act7_reef.py"
BATCH = "M11 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r90"
artkit.REVIEW_ROUND = ROUND
L45 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-45-hive-reef"
CONCEPT = L45 / "concept"
VRELL = DESIGN / "world" / "vrell-space" / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-45"
W, SCREEN, SPEED = 480, 540, 100.0

# the one Vrell palette (a84.R06: black-violet, violet, teal, pale mint) as the reef's materials
MINT = np.array((70, 190, 170), float)              # the mint-teal of glows
VEIN = np.array((56, 160, 145), float)              # the reef's veins, a step under the units' glow
MINT_W = np.array((170, 240, 220), float)
MINT_F = np.array((0.40, 1.0, 0.85))                # the same as a model emission colour
VIOLET_K = np.array((50, 24, 68), float)            # violet chitin, lit
TEAL_K = np.array((16, 44, 50), float)              # teal-black chitin, lit
ICHOR = np.array((90, 210, 180), float)
EMBER = np.array((255, 140, 50), float)
EMBER_W = np.array((255, 220, 150), float)
VIOLET = np.array(u84.VIOLET, float)
VIOLET_W = np.array(u84.VIOLET_W, float)
GOLD = (0.95, 0.72, 0.26)

ADDITIVE = {"spawning-organ-glow", "spawning-organ-swell-glow", "spawning-organ-release-glow", "spawning-organ-death",
            "call-organ-glow", "spore-cloud", "spore-cloud-burn"}


# =========================================================================== small kit

SPRITE_USE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
_bu_claimed = bu.claimed


def claimed(name):
    """belt_units' rule, plus a root SpriteUse names itself (the spore cloud's ``SPORE_ROOT``, part A)."""
    if l23kit._rooted(name, "spore-cloud") and '"spore-cloud"' in SPRITE_USE.read_text(encoding="utf-8"):
        return True
    return _bu_claimed(name)


l23kit.claimed = claimed                    # l23kit's write/load/where decide by its module's ``claimed``


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def load(name):
    return l23kit.load(name)


def grid(w, h, ss=1):
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64)
    return (xx + 0.5) / ss, (yy + 0.5) / ss


def edge_fade(light, margin=4):
    h, w = light.shape[:2]
    xx, yy = grid(w, h)
    d = np.minimum.reduce([xx, w - xx, yy, h - yy])
    f = smoothstep(0.5, margin + 0.5, d)
    return light * (f[..., None] if light.ndim == 3 else f)


def add(light, col, amount):
    light += np.asarray(col, float)[None, None, :] * np.clip(amount, 0, None)[..., None]


def blob(x, y, cx, cy, r):
    return np.exp(-((x - cx) ** 2 + (y - cy) ** 2) / (r * r))


def light_frame(light, margin=4):
    return additive_rgb(edge_fade(light, margin))


def shaded_dot(img_rgb, img_a, cx, cy, r, col, light=(-0.6, -0.6)):
    """A small shaded sphere (a pod, a chip) drawn into float rgb/alpha arrays, 1-bit edge."""
    h, w = img_a.shape
    xx, yy = grid(w, h)
    dx, dy = (xx - cx) / max(r, 1e-6), (yy - cy) / max(r, 1e-6)
    q = dx * dx + dy * dy
    inside = q < 1.0
    nz = np.sqrt(np.clip(1 - q, 0, 1))
    lam = np.clip(0.35 + 0.65 * (-dx * light[0] - dy * light[1] + nz * 0.6) / 1.3, 0.15, 1.2)
    img_rgb[inside] = (np.asarray(col, float)[None, :] * lam[inside][:, None])
    img_a[inside] = 1.0


def to_img(rgb, a):
    return rgba(rgb, (a >= 0.5).astype(float))


def scaled_scene(scene, s, sz=None):
    """A scene scaled by s in x, y and sz in z (a breath); the distance kept a safe bound."""
    sz = s if sz is None else sz
    k = np.array((s, s, sz))

    def f(p):
        d, m = scene(p / k)
        return d * min(s, sz), m
    return f


def render(scene, mats, size, extent, crisp=80, turn=True):
    """A model on the production path; ``turn``: a unit facing down (belt_units.turned), else as modelled (model x
    to the right, model y up the screen)."""
    if turn:
        scene, mats = bu.turned(scene, mats, 0, 16)
    return bu.render(scene, mats, size, extent, crisp=crisp)


def glow_of(img, halo=2.2, k_halo=0.55):
    """An emission-only render (black albedo) -> its light field (0..255 floats) plus a soft halo round it."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    light = a[..., :3] * (a[..., 3:4] / 255.0)
    return light + blur3(light, halo) * k_halo


def blur3(light, sigma):
    return np.dstack([jovkit.blur(light[..., c], sigma) for c in range(light.shape[-1])])


# =========================================================================== the spawning organ

OR_SIZE, OR_PX = (80, 80), 40.0                      # 40 px a model unit (round 84's scale)
OR_EXT = OR_SIZE[0] / OR_PX
BIG = (128, 128)
BREATH = (0.0, 0.4, 1.0, 0.6)
IRIS = (0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
FLESH, DARK, POD = (0.44, 0.22, 0.56), (0.09, 0.05, 0.13), (0.62, 0.86, 0.80)
ROOTC = tuple(np.array(FLESH) * 0.45 + np.array(DARK) * 0.55)


def organ_scene(swell=0.0, iris=0.0, opened=0.0, torn=0.0, breath=0.0, emissive=1.0):
    """round 84 A.5 b's reef polyp in the one Vrell palette: violet flesh, dark roots, pale mint pods, mint veins."""
    scene, mats = u84.organ_model("b", swell, iris, opened, torn)
    k = (1 - torn) * emissive
    mats = list(mats)
    mats[0] = replace(mats[0], albedo=FLESH, emission=tuple(MINT_F * 0.5 * k))
    mats[1] = replace(mats[1], albedo=DARK)
    mats[2] = replace(mats[2], albedo=ROOTC, emission=tuple(MINT_F * 0.55 * k), spec=0.2, shininess=20)
    mats[3] = replace(mats[3], albedo=POD, emission=tuple(MINT_F * 0.9 * swell * k))
    mats[4] = replace(mats[4], emission=tuple(MINT_F * 1.5 * k))
    if breath:
        scene = scaled_scene(scene, 1 + 0.015 * breath, 1 + 0.06 * breath)
    return scene, mats


def _organ(swell, iris, opened, torn, breath, glow_only, size):
    scene, mats = organ_scene(swell, iris, opened, torn, breath)
    if glow_only:
        mats = bu.emission_only(mats)
    return render(scene, mats, size, size[0] / OR_PX)


def organ_light(em_img, iris=0.0, swell=0.0, burst=0.0, size=OR_SIZE):
    """The organ's additive light: its emission with a halo, the iris's growing halo, the pods' ring."""
    w, h = size
    light = glow_of(em_img) * 0.55
    x, y = grid(w, h)
    c = w / 2
    if iris > 0:
        light += blob(x, y, c, c, 5 + 9 * iris)[..., None] * MINT * 0.75 * iris
    ring = np.exp(-((np.hypot(x - c, y - c) - 12) / 6.0) ** 2)
    if swell > 0:
        light += (ring * 0.55 * swell)[..., None] * MINT
    if burst > 0:
        light += (ring * 0.9 * burst)[..., None] * MINT_W * 0.8 + blob(x, y, c, c, 18)[..., None] * MINT * 0.4 * burst
    return light_frame(light, 6)


RELEASE = [(1.0, 1.0, 1.0), (1.0, 0.85, 0.8), (0.8, 0.65, 0.55), (0.6, 0.45, 0.35), (0.4, 0.25, 0.18),
           (0.2, 0.1, 0.06)]                         # (opened, swell, burst) per release frame


def organ_jobs():
    jobs = []
    for s in IRIS:
        for b in BREATH:
            jobs += [("organ", 0.0, s, 0.0, 0.0, b, False, OR_SIZE), ("organ", 0.0, s, 0.0, 0.0, b, True, OR_SIZE)]
    for k in range(6):
        sw = (k + 1) / 6
        jobs += [("organ", sw, 0.0, 0.0, 0.0, 0.0, False, OR_SIZE), ("organ", sw, 0.0, 0.0, 0.0, 0.0, True, OR_SIZE)]
    for op, sw, _ in RELEASE:
        jobs += [("organ", sw, 0.0, op, 0.0, 0.0, False, OR_SIZE), ("organ", sw, 0.0, op, 0.0, 0.0, True, OR_SIZE)]
    jobs.append(("organ", 0.0, 0.0, 0.0, 1.0, 0.0, False, OR_SIZE))                     # the husk
    for torn in (0.0, 0.3, 0.6, 1.0):
        jobs.append(("organ", 0.0, 0.0, 0.0, torn, 0.0, False, BIG))                    # the tatters' sac
    return jobs


def tatters(sacs, n=16):
    """The sac (4 renders at 128: torn 0, .3, .6, 1) collapsing, the pods and flesh scraps flung out, shrinking."""
    out = []
    rng = np.random.default_rng(4590)
    pods = [(k * TAU / 6 + 0.3, rng.uniform(34, 48), rng.uniform(-0.2, 0.2)) for k in range(6)]
    scraps = [(rng.uniform(0, TAU), rng.uniform(26, 44), rng.uniform(2.0, 3.2)) for _ in range(8)]
    for f in range(n):
        u = f / (n - 1)
        base = sacs[min(3, f // 2)] if f < 6 else sacs[3]
        a = np.array(base).astype(np.float64)
        rgb, al = a[..., :3], a[..., 3] / 255.0
        if f >= 1:
            for ang, dist, _ in pods:
                d = 12 + dist * (1 - (1 - u) ** 2)
                r = 4.2 * (1 - u) ** 1.3
                if r > 0.7:
                    shaded_dot(rgb, al, 64 + np.cos(ang) * d, 64 + np.sin(ang) * d, r, np.array(POD) * 255)
            for ang, dist, r0 in scraps:
                d = 16 + dist * (1 - (1 - u) ** 2)
                r = r0 * (1 - u) ** 1.2
                if r > 0.7:
                    shaded_dot(rgb, al, 64 + np.cos(ang) * d, 64 + np.sin(ang) * d, r, np.array(FLESH) * 255)
        out.append(to_img(rgb, al))
    return out


def death_frames(n=16, w=128):
    """A short mint-white flash and a ring of ichor spreading and fading, droplets flung out (vacuum)."""
    x, y = grid(w, w)
    c = w / 2
    rho, ang = np.hypot(x - c, y - c), np.arctan2(y - c, x - c)
    rng = np.random.default_rng(4591)
    drops = [(rng.uniform(0, TAU), rng.uniform(70, 110), rng.uniform(0.9, 1.5)) for _ in range(18)]
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        light = np.zeros((w, w, 3))
        flash = np.exp(-f / 1.4)
        add(light, MINT_W, blob(x, y, c, c, 10 + 6 * u) * 1.1 * flash)
        R = 10 + 46 * (1 - (1 - u) ** 2)
        wob = 1 + 0.07 * np.sin(5 * ang + 1.3) + 0.04 * np.sin(9 * ang)
        ring = np.exp(-((rho - R * wob) / (2.5 + 5 * u)) ** 2)
        add(light, ICHOR, ring * 0.75 * (1 - u) ** 1.4)
        add(light, VIOLET, ring * 0.25 * (1 - u))
        for a0, sp, r in drops:
            d = 8 + sp * 0.5 * u
            if d < 58:
                add(light, ICHOR, blob(x, y, c + np.cos(a0) * d, c + np.sin(a0) * d, r) * 0.8 * (1 - u))
        out.append(light_frame(light, 6))
    return out


def creep_frames(n=9, w=128):
    """The root veins spreading into the plates (thin lines, never dots): frame 0 alive (mint), 1..8 going dark from
    the centre outward."""
    x, y = grid(w, w, 2)
    c = w / 2
    rho, ang = np.hypot(x - c, y - c), np.arctan2(y - c, x - c)
    rng = np.random.default_rng(4592)
    line = np.zeros_like(rho)
    for k in range(13):
        a0, bend, r1 = k * TAU / 13 + rng.uniform(-0.15, 0.15), rng.uniform(-0.5, 0.5), rng.uniform(44, 58)
        a = a0 + bend * (rho / 60) ** 1.5 + 0.08 * np.sin(rho / 6 + k)
        dd = np.abs(np.angle(np.exp(1j * (ang - a)))) * rho
        width = 1.3 * (1 - rho / (r1 + 6)) + 0.4
        seg = smoothstep(width + 0.6, width - 0.4, dd) * smoothstep(26, 32, rho) * smoothstep(r1 + 4, r1 - 6, rho)
        line = np.maximum(line, seg)
    out = []
    for f in range(n):
        front = -1 if f == 0 else 26 + 36 * (f / (n - 1)) ** 0.9
        alive = np.ones_like(rho) if f == 0 else smoothstep(front - 4, front + 4, rho)
        glowk = alive * (0.8 if f == 0 else 0.8 * (1 - 0.6 * f / (n - 1)))
        rgb = (np.array((14, 10, 22.0)) * (1 - glowk[..., None]) + MINT[None, None] * 0.75 * glowk[..., None])
        a = line * (0.9 - 0.15 * (1 - alive))
        a = a.reshape(w, 2, w, 2).mean(axis=(1, 3))
        rgb = rgb.reshape(w, 2, w, 2, 3).mean(axis=(1, 3))
        out.append(rgba(rgb, a))
    return out


def build_organ(res):
    k = 0
    main, glow = [], []
    for s in IRIS:
        for _ in BREATH:
            main.append(res[k])
            glow.append(organ_light(res[k + 1], iris=s))
            k += 2
    sw_main, sw_glow = [], []
    for j in range(6):
        sw_main.append(res[k])
        sw_glow.append(organ_light(res[k + 1], swell=(j + 1) / 6))
        k += 2
    rel_main, rel_glow = [], []
    for op, sw, burst in RELEASE:
        rel_main.append(res[k])
        rel_glow.append(organ_light(res[k + 1], swell=sw * 0.5, burst=burst))
        k += 2
    stump = res[k]
    sacs = res[k + 1:k + 5]
    save_set("spawning-organ", artkit.quantize_set(main, 64))
    save_set("spawning-organ-glow", glow)
    save_set("spawning-organ-swell", artkit.quantize_set(sw_main, 64))
    save_set("spawning-organ-swell-glow", sw_glow)
    save_set("spawning-organ-release", artkit.quantize_set(rel_main, 64))
    save_set("spawning-organ-release-glow", rel_glow)
    save_set("spawning-organ-tatters", artkit.quantize_set(tatters(sacs), 64))
    save_set("spawning-organ-death", death_frames())
    save_set("spawning-organ-stump", [stump], single=True)
    save_set("spawning-organ-creep", creep_frames())


# =========================================================================== the call organ

H_PX = 24.0                                       # px a model unit: the horn 64x40
ROOTS = [(-34, -24), (30, -30), (-34, 24), (30, 26)]  # the root knots round the horn's centre, px (y down)
HORN = (0.80, 0.76, 0.86)
HORN_IN = (0.12, 0.07, 0.15)
TENDON = (0.52, 0.45, 0.60)


def horn_scene(breath=0.0, tendons=True, stubs=0.0):
    """A pale ribbed horn lying on the plates (a conch-like curved cone, its bell to the right, open), ribs round its
    axis breathing; ``tendons``: four tendons from its body to the root knots; ``stubs``: the cut ends hanging."""
    n = 12
    xs = np.linspace(-1.32, 1.18, n + 1)
    u = (xs + 1.32) / 2.5
    ys = 0.24 * np.sin(u * np.pi * 1.15 - 0.5)
    rs = (0.07 + 0.52 * u ** 1.7) * (1 + 0.05 * breath)
    zs = rs * 0.8
    pts = list(zip(xs, ys, zs))
    attach = [(-0.55, 1), (0.3, 1), (-0.55, -1), (0.3, -1)]          # ROOTS' order (screen y down = -model y)
    mats = [Material(HORN, metal=0.05, shininess=60, spec=0.6, pattern=em._ridges(19, 0.30 + 0.1 * breath, axis=0)),
            Material(HORN_IN, shininess=20, spec=0.2),
            Material(TENDON, shininess=40, spec=0.4, pattern=em._ridges(30, 0.2, axis=1))]

    def scene(p):
        items = [(sd_capsule(p, pts[i], pts[i + 1], rs[i], rs[i + 1]), 0) for i in range(n)]
        mouth = sd_sphere(p, (1.18 + 0.16, ys[-1], zs[-1] + 0.02), rs[-1] * 0.78)
        for (ax, side), (rx, ry) in zip(attach, ROOTS):
            j = int(round((ax + 1.32) / 2.5 * n))
            a = (ax, ys[j] + side * rs[j] * 0.7, zs[j] * 0.6)
            end = (rx / H_PX, -ry / H_PX, 0.04)
            if tendons:
                items.append((sd_capsule(p, a, end, 0.09, 0.07), 2))
            elif stubs > 0:
                v = np.array(end) - np.array(a)
                b = tuple(np.array(a) + v * 0.22 + np.array((0, 0, -0.1 * stubs)))
                items.append((sd_capsule(p, a, b, 0.085, 0.05), 2))
        d, m = union(*items, k=0.05)
        inside = np.maximum(d, -mouth)
        m = np.where(mouth < 0.02, 1, m)
        return inside, m
    return scene, mats


SHUT, RISE_SIZE = (96, 64), (88, 60)
CALL_GLOW = (128, 96)
N_RISE, N_DRIFT, N_SHUT = 10, 8, 8


def _horn(kind, breath, scale, size):
    if kind == "shut":
        scene, mats = horn_scene(breath, tendons=True)
    else:
        scene, mats = horn_scene(breath, tendons=False, stubs=1.0)
    if scale != 1.0:
        scene = scaled_scene(scene, scale)
    return render(scene, mats, size, size[0] / H_PX, turn=False)


def call_jobs():
    jobs = [("horn", "shut", float(np.sin(np.pi * k / N_SHUT) ** 2), 1.0, SHUT) for k in range(N_SHUT)]
    jobs += [("horn", "rise", 0.3, round(1.0 + 0.2 * (i / (N_RISE - 1)) ** 0.8, 4), RISE_SIZE) for i in range(N_RISE)]
    jobs += [("horn", "drift", float(np.sin(np.pi * k / N_DRIFT) ** 2), 1.2, RISE_SIZE) for k in range(N_DRIFT)]
    jobs += [("root", s) for s in range(3)]
    return jobs


def call_glow(frame, k, size=CALL_GLOW):
    """A faint pale violet-white halo following the horn's own outline (its frame's alpha, blurred), k 0..1."""
    w, h = size
    a = np.zeros((h, w))
    fa = np.array(frame)[..., 3] / 255.0
    x0, y0 = (w - fa.shape[1]) // 2, (h - fa.shape[0]) // 2
    a[y0:y0 + fa.shape[0], x0:x0 + fa.shape[1]] = fa
    halo = jovkit.blur(a, 6.0) * 0.9 + jovkit.blur(a, 2.0) * 0.25
    light = np.clip(halo, 0, 1)[..., None] * VIOLET_W * 0.34 * k
    return light_frame(light, 8)


def root_scene(state):
    """A root knot where a tendon dives into the plates: a ribbed tendon bulb in a dark socket; 1 gashed, 2 cut."""
    mats = [Material(TENDON, shininess=25, spec=0.2, pattern=em._ridges(30, 0.35, axis=0)),
            Material((0.05, 0.03, 0.08), shininess=10, spec=0.1),
            Material((0.10, 0.30, 0.28), shininess=80, spec=0.8, emission=tuple(MINT_F * 0.12))]

    def scene(p):
        socket = sd_ellipsoid(p, (0, 0, -0.02), (0.5, 0.46, 0.08))
        h = 0.22 if state < 2 else 0.09
        bulb = sd_ellipsoid(p, (0, 0, h * 0.4), (0.38, 0.33, h))
        items = [(socket, 1), (bulb, 0)]
        if state >= 1:
            ichor = sd_ellipsoid(p, (0.12, 0.1, 0.0 + 0.02 * state), (0.22 + 0.1 * state, 0.16 + 0.08 * state, 0.05))
            items.append((ichor, 2))
        d, m = union(*items, k=0.04)
        if state == 1:
            gash = sd_capsule(p, (-0.25, -0.18, 0.4), (0.22, 0.12, 0.35), 0.06, 0.06)
            d = np.maximum(d, -gash)
        return d, m
    return scene, mats


def _root(state):
    scene, mats = root_scene(state)
    return render(scene, mats, (28, 28), 28 / H_PX, turn=False)


def chips_frames(n, w, cols, seed, count=10, speed=(26, 40), size=(1.6, 2.8), ring=None):
    """Chips flung out from the centre and shrinking (normal alpha, 1-bit), an optional fading ichor stain."""
    rng = np.random.default_rng(seed)
    parts = [(rng.uniform(0, TAU), rng.uniform(*speed), rng.uniform(*size), cols[k % len(cols)]) for k in range(count)]
    out = []
    for f in range(n):
        u = (f + 1) / n
        rgb, al = np.zeros((w, w, 3)), np.zeros((w, w))
        if ring is not None and u < 0.7:
            x, y = grid(w, w)
            rr = np.hypot(x - w / 2, y - w / 2)
            stain = rr < 5 + 8 * u
            rgb[stain] = np.asarray(ring, float) * (1 - 0.4 * u)
            al[stain] = 1
        for a, sp, sz, col in parts:
            d = 3 + sp * (1 - (1 - u) ** 2) * (w / 2 - 6) / 40
            r = sz * (1 - u) ** 1.1
            if r > 0.6:
                shaded_dot(rgb, al, w / 2 + np.cos(a) * d, w / 2 + np.sin(a) * d, r, col)
        out.append(to_img(rgb, al))
    return out


def build_call(res):
    shut, rise, drift = res[:N_SHUT], res[N_SHUT:N_SHUT + N_RISE], res[N_SHUT + N_RISE:N_SHUT + N_RISE + N_DRIFT]
    roots = res[N_SHUT + N_RISE + N_DRIFT:]
    save_set("call-organ", artkit.quantize_set(shut, 48))
    save_set("call-organ-rise", artkit.quantize_set(rise, 48))
    save_set("call-organ-drift", artkit.quantize_set(drift, 48))
    glows = [call_glow(rise[i], (i + 1) / N_RISE) for i in range(N_RISE)] + [
        call_glow(drift[(i * 4) // 3], 0.8 + 0.2 * np.sin(TAU * i / 6) ** 2) for i in range(6)]
    save_set("call-organ-glow", glows)
    save_set("call-organ-root", artkit.quantize_set(roots, 32))
    tend = np.array(TENDON) * 255
    save_set("call-organ-root-break", chips_frames(8, 48, [tend, tend * 0.7, ICHOR * 0.5], 4593, count=11))


# =========================================================================== the courier's cyst

def cyst_scene(state):
    """A lump of chitin grown over a gold courier pod, a gold edge showing; 1 cracked along its splits, 2 broken open
    (the chitin split away, the gold-lined hollow empty: the crate is out)."""
    mats = [Material((0.20, 0.10, 0.26), metal=0.1, shininess=60, spec=0.5, pattern=em._ridges(14, 0.22, axis=0)),
            Material(GOLD, metal=0.9, shininess=140, spec=1.3),
            Material((0.04, 0.02, 0.05), shininess=10, spec=0.1),
            Material((0.10, 0.28, 0.30), metal=0.1, shininess=50, spec=0.5)]

    def scene(p):
        pod = sd_ellipsoid(p, (0.2, 0.04, 0.18), (0.56, 0.28, 0.2))
        lump = sd_ellipsoid(p, (0, 0, 0.12), (0.66, 0.56, 0.36))
        knob = sd_sphere(p, (-0.3, -0.22, 0.2), 0.24)
        items = []
        if state == 0:
            shell = np.maximum(union((lump, 0), (knob, 3), k=0.08)[0], -sd_sphere(p, (0.62, 0.14, 0.2), 0.22))
            items = [(shell, 0), (pod, 1)]
        elif state == 1:
            cracks = np.minimum(sd_capsule(p, (-0.5, 0.3, 0.4), (0.5, -0.1, 0.45), 0.05, 0.05),
                                sd_capsule(p, (0.1, -0.5, 0.4), (0.25, 0.4, 0.45), 0.045, 0.045))
            shell = np.maximum(np.maximum(union((lump, 0), (knob, 3), k=0.08)[0],
                                          -sd_sphere(p, (0.62, 0.14, 0.2), 0.3)), -cracks)
            items = [(shell, 0), (pod, 1)]
        else:
            half_a = np.maximum(lump, p[:, 0] + 0.28)
            half_b = np.maximum(sd_ellipsoid(p, (0.1, 0.1, 0.0), (0.5, 0.45, 0.12)), -(p[:, 0] + 0.22))
            hollow = np.maximum(sd_ellipsoid(p, (0.18, 0.06, 0.06), (0.46, 0.3, 0.1)),
                                -sd_ellipsoid(p, (0.18, 0.06, 0.12), (0.4, 0.24, 0.1)))
            items = [(half_a, 0), (half_b, 3), (hollow, 1)]
        d, m = union(*items, k=0.02)
        return d, m
    return scene, mats


def _cyst(state):
    scene, mats = cyst_scene(state)
    return render(scene, mats, (40, 40), 40 / H_PX, turn=False)


def build_cyst(res):
    save_set("courier-cyst", artkit.quantize_set(res, 40))
    save_set("courier-cyst-break", chips_frames(8, 64, [np.array((60, 30, 76.0)), np.array((40, 22, 52.0)),
                                                        np.array(GOLD) * 255, np.array((20, 60, 64.0))], 4594,
                                                count=14, speed=(30, 46), size=(1.8, 3.2)))


# =========================================================================== the spore cloud (production)

SPORE_R, SPORE_N = 64, 176


def _puffs(phase):
    rng = np.random.default_rng(4595)
    out = []
    for k in range(8):
        a0, r0, rad, sp = rng.uniform(0, TAU), rng.uniform(8, 34), rng.uniform(24, 36), rng.choice((-1, 1))
        a = a0 + sp * phase / 4 + 0.15 * np.sin(phase + k)
        out.append((np.cos(a) * r0, np.sin(a) * r0, rad * (1 + 0.06 * np.sin(phase * (1 + k % 2) + a0))))
    return out


_TEX = {}


def _tex():
    if not _TEX:
        _TEX["a"] = raster.fbm(SPORE_N, SPORE_N, 44, 4596, octaves=4, period=True)
        _TEX["b"] = raster.fbm(SPORE_N, SPORE_N, 22, 4597, octaves=3, period=True)
    return _TEX["a"], _TEX["b"]


def cloud_density(phase, ss=1):
    """The cloud's density 0..1: eight soft billows round the centre, a slow fbm texture rolling through them, zero
    well inside the canvas."""
    n = SPORE_N
    x, y = grid(n, n, ss)
    c = n / 2
    d = np.zeros_like(x)
    for px, py, r in _puffs(phase):
        d += np.exp(-((x - c - px) ** 2 + (y - c - py) ** 2) / (r * r))
    ta, tb = _tex()
    sx = int(round(6 * np.cos(phase))) % n
    sy = int(round(6 * np.sin(phase))) % n
    t1 = np.roll(np.roll(ta, sx, 1), sy, 0)
    t2 = np.roll(np.roll(tb, -sy, 1), sx, 0)
    if ss > 1:
        t1, t2 = np.kron(t1, np.ones((ss, ss))), np.kron(t2, np.ones((ss, ss)))
    rho = np.hypot(x - c, y - c)
    dens = np.clip(d * 0.55, 0, 1.4) * (0.7 + 0.45 * (t1 - 0.5) + 0.25 * (t2 - 0.5))
    dens = 1 - np.exp(-1.6 * np.clip(dens, 0, None))
    return dens * smoothstep(c - 2, c - 24, rho), rho, np.arctan2(y - c, x - c)


def spore_frame(f, n=8, front=None, burn_u=0.0):
    phase = TAU * f / n
    dens, rho, ang = cloud_density(phase)
    inside = 1.0 if front is None else smoothstep(front + 4, front - 4, rho)
    x, y = grid(SPORE_N, SPORE_N)
    c = SPORE_N / 2
    light = np.zeros(x.shape + (3,))
    add(light, np.array((112, 56, 176.0)), dens * 0.36 * inside)
    add(light, np.array((170, 140, 220.0)), dens ** 3 * 0.10 * inside)
    rng = np.random.default_rng(4598)
    for _ in range(60):
        a0, r0, sp = rng.uniform(0, TAU), SPORE_R * np.sqrt(rng.uniform(0.02, 0.8)), rng.choice((-1, 1))
        a = a0 + sp * phase / 5
        px, py = c + np.cos(a) * r0, c + np.sin(a) * r0
        tw = 0.55 + 0.45 * np.sin(phase * 2 + a0 * 3)
        ins = 1.0 if front is None else float(np.hypot(px - c, py - c) < front - 2)
        add(light, VIOLET_W, blob(x, y, px, py, 0.8) * 0.26 * tw * ins)
    if front is not None:
        harm = ((3, 0.035, 0.4), (5, 0.03, 2.1), (8, 0.025, 4.0), (13, 0.016, 1.2), (19, 0.01, 5.3))
        wob = front * (1 + sum(amp * np.sin(k * ang + ph + (2 + k % 3) * burn_u) for k, amp, ph in harm))
        edge = np.exp(-((rho - wob) / 2.4) ** 2) * smoothstep(0.0, 0.08, dens + 0.02)
        add(light, EMBER, edge * 0.85)
        add(light, EMBER_W, edge ** 3 * 0.55)
        for _ in range(26):
            a0 = rng.uniform(0, TAU)
            r0 = front * 1.04 + rng.uniform(2, 10) + 20 * burn_u
            if r0 < c - 8:
                add(light, EMBER, blob(x, y, c + np.cos(a0) * r0, c + np.sin(a0) * r0, 0.9) * 0.6 * (1 - burn_u))
    return light_frame(light, 6)


def spore_veil():
    dens, _, _ = cloud_density(0.0)
    return rgba(np.zeros(dens.shape + (3,)) + (30, 12, 44), np.clip(dens * 0.25, 0, 0.25))


def build_spore():
    save_set("spore-cloud-veil", [spore_veil()], single=True)
    save_set("spore-cloud", [spore_frame(f) for f in range(8)])
    save_set("spore-cloud-burn", [spore_frame(f % 8, front=SPORE_R * 1.08 * (1 - (f + 0.5) / 10), burn_u=(f + 0.5) / 10)
                                  for f in range(10)])


# =========================================================================== the backdrop: clock and sections

FACTORS = {"deep": 0.15, "ground": 1.0, "low-air": 1.3}
DATA_PATH = L45 / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Reef Rim", "end": 40, "atmosphere": "clear", "tiles": ["deep-1", "rim-plates"]},
    {"name": "The Brood Fields", "end": 108, "atmosphere": "clear", "tiles": ["deep-2", "brood-fields"]},
    {"name": "The Spine", "end": 160, "atmosphere": "clear", "tiles": ["deep-3", "spine"]},
    {"name": "The Living Bridge", "end": 212, "atmosphere": "clear", "tiles": ["deep-4"]},
    {"name": "The Brood Heart", "end": 268, "atmosphere": "clear", "tiles": ["deep-5", "brood-heart"]},
    {"name": "The Far Rim", "end": 278, "atmosphere": "clear", "tiles": ["deep-6"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
DEEP = [f"deep-{i + 1}" for i in range(len(SECTIONS))]
TILE_H = 960
GROUND_TILES = {"rim-plates": 0, "brood-fields": 1, "spine": 2, "brood-heart": 4}   # tile -> section index
SEED = 4501
VEIN_BASE, VEIN_PULSE = 0.42, 0.55          # the veins' share in the tiles, and in the pulsing -veins layer


def ground_row_at(t, y_down):
    """The ground row under screen y (down) at level time t."""
    return scroll(t) + SCREEN - y_down


def t_ground(row):
    return CLOCK.t_for_layer_row(row, 1.0)


# =========================================================================== the reef's plates

def _hash(i, j, seed):
    h = (i.astype(np.int64) * 374761393 + j.astype(np.int64) * 668265263 + seed * 2246822519) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    h = h ^ (h >> 16)
    return (h & 0xFFFFFF) / float(0x1000000)


def plates(rows, cx, cy, seed, period=None):
    """Jittered-grid Voronoi plates on ground rows (y-up) x the 480 columns: the distance to the plate's edge e, its
    tone and a per-plate hash; ``period`` (cells) wraps the rows (a tile)."""
    X = (np.arange(W) + 0.5)[None, :]
    R = np.asarray(rows, float)[:, None]
    gi, gj = np.floor(X / cx).astype(np.int64), np.floor(R / cy).astype(np.int64)
    f1 = np.full((len(rows), W), 1e9)
    f2 = np.full((len(rows), W), 1e9)
    bi = np.zeros((len(rows), W), np.int64)
    bj = np.zeros((len(rows), W), np.int64)
    for di in (-1, 0, 1):
        for dj in (-1, 0, 1):
            ci, cj = gi + di, gj + dj
            cw = cj % period if period else cj
            px = (ci + 0.12 + 0.76 * _hash(ci, cw, seed)) * cx
            py = (cj + 0.12 + 0.76 * _hash(ci, cw, seed + 1)) * cy
            d = np.hypot(X - px, R - py)
            closer = d < f1
            f2 = np.where(closer, f1, np.minimum(f2, d))
            bi, bj = np.where(closer, ci, bi), np.where(closer, cw, bj)
            f1 = np.where(closer, d, f1)
    e = (f2 - f1) / 2
    return e, _hash(bi, bj, seed + 2), _hash(bi, bj, seed + 3)


STYLES = {
    "rim": dict(cx=66, cy=60, violet=0.55, lum=1.0, gap=2.4, drop=0.16, vein_gap=0.25, vein_in=0.85, seed=11),
    # step C4: the brood fields' plate rows shifted half a row (phase), so no row of plate seams lies on the tile's wrap
    # (BackdropSeamsSpaceTest scored the wrap 2.57 against its 1.5)
    "brood": dict(cx=44, cy=40, violet=0.78, lum=0.82, vein_gap=1.0, vein_in=0.35, seed=12, phase=20),
    "spine": dict(cx=52, cy=48, violet=0.5, lum=0.92, vein_gap=0.8, vein_in=0.3, ridge=True, seed=13),
    "second": dict(cx=60, cy=56, violet=0.35, lum=0.95, vein_gap=0.7, vein_in=0.6, seed=14),
    "heart": dict(cx=40, cy=40, violet=0.3, lum=0.78, vein_gap=1.0, vein_in=0.7, seed=15),
}


RIB_AMP = 0.4   # step FIXC-SPINE: the ridge ribs' height (0.2 faint, 0.7 corrugated)
RIB_PHASE = 10.0  # rows: puts the tile wrap where the ribs score lowest (wrap 1.15; 0 gave 1.49)


def reef(rows, style, period_rows=None):
    """A reef field over ground rows (y-up, one per image row from the top): rgb (n, 480, 3) float, alpha and the
    veins' 0..1 strength. ``period_rows``: a tile wrapping in those rows (rows = H-1 .. 0)."""
    st = STYLES[style]
    n = len(rows)
    seed = SEED + 100 * st["seed"]
    per = (period_rows // st["cy"]) if period_rows else None
    e, tone, tone2 = plates(np.asarray(rows) + st.get("phase", 0), st["cx"], st["cy"], seed, per)
    period = period_rows is not None
    tex = raster.fbm(W, n, 96, seed + 4, octaves=4, period=period)
    vmod = raster.fbm(W, n, 96, seed + 5, octaves=3, period=period)
    vin = raster.fbm(W, n, 48, seed + 6, octaves=3, period=period)
    x = (np.arange(W) + 0.5)[None, :]
    bw = 7.0
    bevel = smoothstep(0.0, bw, e)
    hgt = np.sqrt(np.clip(1 - (1 - np.clip(e / (bw * 1.8), 0, 1)) ** 2, 0, 1)) + 0.25 * tex
    if st.get("ridge"):
        rg = np.exp(-((x - 240) / 80.0) ** 2)
        rr = np.asarray(rows, float)[:, None]
        # step FIXC-SPINE: the ridge's ribbing follows its plates (it was a straight groove every 48 rows across the
        # ridge, read as ruled lines): soft raised ribs arching down off the crest, each plate with its own phase and
        # tilt (so no rib runs on across a seam) and fading into its bevel; rows wrap (16 | 960, tex periodic)
        arch = 14.0 * ((x - 240) / 90.0) ** 2 + 10.0 * (tex - 0.5) + (tone2 - 0.5) * 0.6 * (x - 240)
        rib = (0.5 + 0.5 * np.cos(2 * np.pi * ((rr + arch + RIB_PHASE) / 16.0 + 7.0 * tone))) ** 2
        hgt = hgt + 14.0 * rg + RIB_AMP * rib * smoothstep(2.0, 12.0, e) * rg

    def grad(a):
        if period:
            gi = (np.roll(a, -1, 0) - np.roll(a, 1, 0)) / 2
        else:
            gi = np.gradient(a, axis=0)
        return np.gradient(a, axis=1), gi
    gx, gi = grad(hgt * 7.0)
    shade = np.clip(0.62 + 0.5 * (gx + gi), 0.22, 1.4)
    mix = np.clip(st["violet"] + 0.5 * (tone - 0.5), 0, 1)[..., None]
    col = TEAL_K * (1 - mix) + VIOLET_K * mix
    col = col * (0.55 + 0.6 * tone2)[..., None] * shade[..., None] * st["lum"]
    col = col * (0.22 + 0.78 * bevel)[..., None]
    if st.get("ridge"):
        col = col * (1 + 0.25 * rg)[..., None]
    vg = np.exp(-(e / 1.5) ** 2) * np.clip((vmod - 0.38) * 2.4, 0, 1) * st["vein_gap"]
    vi = smoothstep(0.03, 0.0, np.abs(vin - 0.5)) * bevel * np.clip((vmod - 0.3) * 2.0, 0, 1) * st["vein_in"]
    vein = np.clip(vg + vi, 0, 1)
    alpha = np.ones_like(e)
    if style == "rim":
        side = smoothstep(130, 236, np.abs(x - 240))
        keep = (tone2 > st["drop"] + 0.62 * side).astype(float)
        alpha = smoothstep(st["gap"] - 0.6, st["gap"] + 0.8, e) * keep
        col = col * (0.5 + 0.5 * smoothstep(st["gap"], st["gap"] + 9, e))[..., None]
        vein = vein * alpha
    rgb = col + (vein * VEIN_BASE)[..., None] * VEIN
    return rgb, alpha, vein


_TILES = {}


def tile(name):
    """A ground tile set (cached): rgb (rounded), alpha (rounded) and the veins, image rows top = highest."""
    if name not in _TILES:
        style = {"rim-plates": "rim", "brood-fields": "brood", "spine": "spine", "brood-heart": "heart"}[name]
        rows = TILE_H - 1 - np.arange(TILE_H)
        rgb, a, v = reef(rows, style, TILE_H)
        _TILES[name] = (np.clip(np.round(rgb), 0, 255), np.round(np.clip(a, 0, 1) * 255) / 255, v)
    return _TILES[name]


def sample(name, rows):
    """The tile's pixels at ground rows (as BackdropTiles draws it: image row H-1 - row mod H)."""
    rgb, a, v = tile(name)
    idx = TILE_H - 1 - (np.asarray(rows, int) % TILE_H)
    return rgb[idx], a[idx], v[idx]


def ragged(n, seed, cell=64, amp=1.0):
    return (raster.fbm(W, max(n, 8), cell, seed, octaves=3, period=False)[:n] - 0.5) * amp


def blend(a, b, m):
    """Two (rgb, alpha, vein) fields over the same rows mixed by m (0 = a, 1 = b), straight alpha."""
    (ra, aa, va), (rb, ab, vb) = a, b
    al = aa * (1 - m) + ab * m
    rgb = (ra * (aa * (1 - m))[..., None] + rb * (ab * m)[..., None]) / np.maximum(al, 1e-6)[..., None]
    return rgb, al, va * (1 - m) + vb * m


def seam_piece(a_name, b_name, s, h=256, seed=0):
    """Rows [s - h, s): tile a's rows blended into tile b's along a ragged line; its top rows are b's own."""
    rows = s - 1 - np.arange(h)
    u = ((rows - (s - h)) / h)[:, None]
    m = smoothstep(-0.07, 0.07, u - 0.5 + 0.34 * ragged(h, SEED + 900 + seed))
    m = np.clip(m + smoothstep(0.88, 1.0, u), 0, 1) * smoothstep(0.0, 0.1, u)
    return blend(sample(a_name, rows), sample(b_name, rows), m)


def edge_cut(field, rows, edge_rows, lip=10.0):
    """A reef field ending at a ragged edge (``edge_rows`` per column, y-up): nothing beyond, the plates falling away
    (darker) toward it, a faint violet lip on it."""
    rgb, a, v = field
    d = edge_rows[None, :] - np.asarray(rows, float)[:, None]          # > 0 on the reef
    keep = smoothstep(-0.5, 0.8, d)
    fall = smoothstep(0, 46, d)
    rgb = rgb * (0.35 + 0.65 * fall)[..., None] + (np.exp(-((d - 3) / 2.2) ** 2) * keep)[..., None] * VIOLET_K * 0.6
    return rgb, a * keep, v * keep * (0.3 + 0.7 * fall)


def edge_line(seed, base, amp=45.0):
    x = np.arange(W) + 0.5
    n = raster.fbm(W, 8, 64, seed, octaves=3, period=False)[4]
    return base + amp * (n - 0.5) * 2 + 10 * np.sin(x / 23.0 + seed % 7)


# =========================================================================== the ground pieces

def g_seam(i):
    return seam("ground", i)


_GP = {}


def ground_pieces():
    if not _GP:
        _GP.update(_ground_pieces())
    return _GP


def _ground_pieces():
    """The cut ground pieces: name -> (field (rgb, alpha, vein), top row) (rows y-up, image top = its top row)."""
    out = {}
    s2, s3, s4, s5, s6 = (g_seam(i) for i in range(1, 6))
    out["seam-2"] = (seam_piece("rim-plates", "brood-fields", s2, seed=2), s2 - 1)
    out["seam-3"] = (seam_piece("brood-fields", "spine", s3, seed=3), s3 - 1)
    h = 352
    rows = s4 + 191 - np.arange(h)
    out["reef-edge"] = (edge_cut(sample("spine", rows), rows, edge_line(SEED + 31, s4 + 70)), s4 + 191)
    lo, hi = s4 + 840, s5                                               # the second reef: its lip near lo
    rows = hi - 1 - np.arange(hi - lo)
    field = reef(rows, "second")
    top = 220
    u = ((rows - (hi - top)) / top)[:, None]
    m = np.clip(smoothstep(-0.07, 0.07, u - 0.5 + 0.34 * ragged(len(rows), SEED + 905)) * (u > 0)
                + smoothstep(0.88, 1.0, u), 0, 1)
    field = blend(field, sample("brood-heart", rows), m)
    lip = edge_line(SEED + 32, lo + 90, 40)
    rgb, a, v = field
    d = np.asarray(rows, float)[:, None] - lip[None, :]                  # > 0 on the second reef (above its lip)
    keep = smoothstep(-0.5, 0.8, d)
    rise = smoothstep(0, 40, d)
    rgb = rgb * (0.35 + 0.65 * rise)[..., None] + (np.exp(-((d - 3) / 2.2) ** 2) * keep)[..., None] * VIOLET_K * 0.7
    field = (rgb, a * keep, v * keep * (0.3 + 0.7 * rise))
    k = 5
    ph = (hi - lo) // k
    for j in range(k):
        a0 = j * ph
        a1 = (hi - lo) if j == k - 1 else (j + 1) * ph
        # image rows from the top: the piece j counted from the bottom
        top_i, bot_i = (hi - lo) - a1, (hi - lo) - a0
        out[f"second-reef-{j + 1}"] = (tuple(f[top_i:bot_i] for f in field), hi - 1 - top_i)
    h = 480
    rows = s6 + 319 - np.arange(h)
    out["far-rim"] = (edge_cut(sample("brood-heart", rows), rows, edge_line(SEED + 33, s6 + 150)), s6 + 319)
    return out


def piece_images(field):
    rgb, a, v = field
    return rgba(rgb, a), additive_rgb((v * VEIN_PULSE)[..., None] * VEIN)


WELL = 320


def brood_well():
    """The brood heart's deep well: ribbed walls stepping down round a dark throat, fading into the plates."""
    x, y = grid(WELL, WELL)
    c = WELL / 2
    dx, dy = x - c, y - c
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    tex = raster.fbm(WELL, WELL, 40, SEED + 40, octaves=4, period=False)
    wob = rho * (1 + 0.05 * np.sin(5 * ang + 1.0) + 0.03 * np.sin(11 * ang))
    depth = smoothstep(150, 20, wob)
    ribs = 0.5 + 0.5 * np.cos(wob / 9.0 * np.pi)
    lit = np.clip(0.55 - 0.45 * (dx + dy) / np.maximum(rho, 1) * smoothstep(20, 120, rho), 0.15, 1.1)
    rgb = (TEAL_K * 0.6 + VIOLET_K * 0.4) * (0.6 + 0.35 * tex)[..., None] * (1 - 0.8 * depth)[..., None]
    rgb = rgb * (0.65 + 0.35 * ribs * (1 - depth))[..., None] * (1.25 - lit)[..., None]
    rgb = rgb + (np.exp(-((wob - 132) / 5.0) ** 2))[..., None] * VIOLET_K * 0.5
    alpha = smoothstep(158, 132, wob)
    vein = smoothstep(0.035, 0.0, np.abs(np.sin(ang * 9 + 0.6 * np.sin(rho / 17))) * 0.2) * smoothstep(40, 70, rho) \
        * smoothstep(140, 110, rho)
    rgb = rgb + (vein * VEIN_BASE * 0.6)[..., None] * VEIN
    light = blob(x, y, c, c, 46)[..., None] * MINT * 0.32 + blob(x, y, c, c, 16)[..., None] * MINT_W * 0.15
    return rgba(rgb, alpha), light_frame(light, 8)


PIT = 72
PIT_FRAMES = 24


def pit_state(f):
    """Frame f -> (open 0..1, lip glow 0..1, throat glow 0..1, breath 0..1)."""
    if f < 8:
        return 0.0, 0.10 + 0.06 * np.sin(TAU * f / 8), 0.0, np.sin(np.pi * f / 8) ** 2
    if f < 16:
        u = (f - 7) / 8
        return 0.35 * u ** 2, 0.15 + 0.85 * u, 0.25 * u, 0.5
    if f < 20:
        return 1.0, 0.85 + 0.15 * np.sin(TAU * (f - 16) / 4), 0.9 + 0.1 * np.sin(TAU * (f - 16) / 4), 0.6
    u = (f - 19) / 4
    return 1.0 - u, 0.85 * (1 - u) + 0.1 * u, 0.9 * (1 - u), 0.3


def pit_frame(f, ss=4):
    """A spawning pit seen from above: fleshy lips round a mouth in the plates (radial folds), the throat dark; the
    lips glowing mint in the tell and parting; open, the throat lit from below."""
    op, lip, throat, br = pit_state(f)
    x, y = grid(PIT, PIT, ss)
    c = PIT / 2
    dx, dy = x - c, y - c
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    r_lip = 15.0 * (1 + 0.03 * br)
    folds = 0.5 + 0.5 * np.cos(ang * 7 + 0.4)
    mouth = 2.0 + 10.5 * op
    pucker = mouth * (1 + 0.35 * (folds - 0.5) * (1 - op))
    hgt = np.exp(-((rho - r_lip) / 6.0) ** 2) * (0.8 + 0.2 * folds) + 0.25 * smoothstep(30, 18, rho)
    hole = smoothstep(pucker + 0.8, pucker - 0.8, rho)
    gy, gx = np.gradient(hgt * 9, axis=0) * ss, np.gradient(hgt * 9, axis=1) * ss
    shade = np.clip(0.6 + 0.5 * (gx + gy), 0.2, 1.4)
    rgb = (VIOLET_K * 0.7 + TEAL_K * 0.3)[None, None] * (0.6 + 0.6 * hgt)[..., None] * shade[..., None]
    rgb = rgb * (1 - hole)[..., None] + np.array((4, 2, 8.0)) * hole[..., None]
    alpha = smoothstep(33, 24, rho)
    rgb = rgb.reshape(PIT, ss, PIT, ss, 3).mean(axis=(1, 3))
    alpha = alpha.reshape(PIT, ss, PIT, ss).mean(axis=(1, 3))
    lx, ly = grid(PIT, PIT)
    lr = np.hypot(lx - c, ly - c)
    light = np.zeros((PIT, PIT, 3))
    add(light, MINT, np.exp(-((lr - (mouth + 3.5)) / 3.0) ** 2) * lip * 0.75)
    add(light, MINT, np.exp(-((lr - r_lip) / 7.0) ** 2) * lip * 0.25)
    add(light, MINT_W, blob(lx, ly, c, c, max(2.0, mouth * 0.8)) * throat * 0.55)
    add(light, MINT, blob(lx, ly, c, c, 18) * throat * 0.25)
    return rgba(rgb, alpha), light_frame(light, 6)


BRIDGE = (300, 1400)


def bridge():
    """The living bridge from above: a band of chitin segments along the scroll, tendon cables either side, swaying
    gently, its two ends thinning out (no straight cut)."""
    w, h = BRIDGE
    ss = 2
    x, y = grid(w, h, ss)
    yn = y / h
    cxl = w / 2 + 34 * np.sin(yn * TAU * 0.9 + 0.4) + 12 * np.sin(yn * TAU * 2.3)
    width = 40 * (0.55 + 0.45 * np.sin(np.pi * yn) ** 0.6)
    d = np.abs(x - cxl)
    body = smoothstep(width + 1, width - 1, d)
    rnd = np.sqrt(np.clip(1 - (d / np.maximum(width, 1)) ** 2, 0, 1))
    # step FIXC-SPINE: the segment joints arch over the rounded band (bowed along the scroll at its crown), unevenly
    # spaced and soft (they were straight, hard grooves every 34 px, read as ruled lines)
    seg = np.abs(((y + 4 - 9 * rnd + 6 * np.sin(yn * TAU * 5.0) + 3 * np.sin(x / 17 + yn * 40)) % 34) - 17)
    seg_g = smoothstep(4.5, 0.0, 17 - seg)
    lit = np.clip(0.55 + 0.55 * (-(x - cxl) / np.maximum(width, 1)) * 0.8, 0.2, 1.2)
    rgb = (VIOLET_K * 0.65 + TEAL_K * 0.35)[None, None] * (0.45 + 0.55 * rnd)[..., None] * lit[..., None]
    rgb = rgb * (1 - 0.35 * seg_g)[..., None]
    alpha = body.copy()
    for k, (off, th) in enumerate(((-1.35, 3.2), (-1.7, 2.2), (1.35, 3.2), (1.75, 2.0))):
        cx = cxl + off * width + 6 * np.sin(yn * TAU * 3 + k)
        dc = np.abs(x - cx)
        cab = smoothstep(th + 0.8, th - 0.6, dc)
        cr = np.array((60, 44, 76.0)) * (0.5 + 0.5 * np.clip(1 - dc / th, 0, 1))[..., None]
        rgb = rgb * (1 - cab[..., None]) + cr * cab[..., None]
        alpha = np.maximum(alpha, cab * 0.95)
    vein = smoothstep(1.4, 0.3, np.abs(x - cxl - 3 * np.sin(y / 40))) * body * 0.6
    rgb = rgb + (vein * 0.5)[..., None] * VEIN
    ends = smoothstep(0, 160, y) * smoothstep(h, h - 160, y)
    alpha = alpha * ends * 0.92
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    alpha = alpha.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return rgba(rgb, alpha)


FLEET = (400, 170)


def dead_fleet():
    """Far ahead on deep: the first dead hulls of the fleet (Level 46), dark, lit cold from the top left, edges soft."""
    w, h = FLEET
    x, y = grid(w, h, 2)
    rgb = np.zeros(x.shape + (3,))
    alpha = np.zeros(x.shape)
    hulls = [(90, 70, 70, 13, -0.25), (210, 50, 110, 18, 0.12), (320, 100, 58, 10, 0.4), (150, 125, 40, 8, -0.6)]
    for k, (cx, cy, L, wd, a) in enumerate(hulls):
        ca, sa = np.cos(a), np.sin(a)
        u, v = (x - cx) * ca + (y - cy) * sa, -(x - cx) * sa + (y - cy) * ca
        prof = wd * np.sqrt(np.clip(1 - (u / L) ** 2, 0, 1)) * (0.7 + 0.3 * (u < 0))
        inside = smoothstep(0.8, -0.8, np.abs(v) - prof) * (np.abs(u) < L)
        broken = (np.abs(u - L * 0.35) < 4 + 2 * k) & (v > 0)
        inside = inside * (~broken)
        litk = np.clip(0.4 - v / np.maximum(prof, 1) * 0.5, 0.1, 1.0)
        col = np.array((38, 36, 52.0)) * litk[..., None] + np.array((150, 150, 190.0)) * \
            (np.exp(-((v + prof) / 1.2) ** 2) * 0.35)[..., None]
        rgb = rgb * (1 - inside[..., None]) + col * inside[..., None]
        alpha = np.maximum(alpha, inside * 0.9)
    alpha = alpha * jovkit.soft_box(w * 2, h * 2, 20)
    rgb = rgb.reshape(h, 2, w, 2, 3).mean(axis=(1, 3))
    alpha = alpha.reshape(h, 2, w, 2).mean(axis=(1, 3))
    return rgba(rgb, alpha)


# =========================================================================== the deep layer

def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def deep_field():
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rgb = a84.space_rgb(W, n, SEED, k=1.25)[::-1]                        # row 0 = layer row r0 (y-up)
    rows = np.arange(n) + r0
    x = np.arange(W) + 0.5
    for cx, cr, rx, ry in ((120, 380, 90, 34), (360, 760, 70, 26)):    # two far reefs in section 1
        d = np.hypot((x[None, :] - cx) / rx, (rows[:, None] - cr) / ry)
        mass = smoothstep(1.0, 0.7, d)
        rim = np.exp(-((d - 0.8) / 0.12) ** 2) * (rows[:, None] > cr)
        rgb = rgb * (1 - 0.7 * mass[..., None]) + mass[..., None] * np.array((10, 16, 20.0)) * 0.7 \
            + rim[..., None] * MINT * 0.12
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== build the backdrop

def build_reef():
    import backdrop_l43 as bd43  # noqa: E402  (Level 43's spore sacs, unchanged)
    artkit.REVIEW_ROUND = ROUND
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    for name in GROUND_TILES:
        rgb, a, v = tile(name)
        save(name, rgba(rgb, a))
        save(name + "-veins", additive_rgb((v * VEIN_PULSE)[..., None] * VEIN))
    for name, (field, _) in ground_pieces().items():
        body, veins = piece_images(field)
        save(name, body)
        save(name + "-veins", veins)
    body, light = brood_well()
    save("brood-well", body)
    save("brood-well-glow", light)
    for f in range(PIT_FRAMES):
        body, light = pit_frame(f)
        save(f"pit_{f}", body)
        save(f"pit-glow_{f}", light)
        if f < 8:
            save(f"pit-shut_{f}", body)
            save(f"pit-shut-glow_{f}", light)
    save("bridge", bridge())
    save("dead-fleet", dead_fleet())
    for k, s in enumerate("abc"):
        save(f"spore-sac-{s}", bd43.spore_sac(SEED + 70 + k))
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


SAC = (40, 52)


def pieces_spec():
    gp = {name: Image.open(OUT / f"{name}.png").size for name in
          ("seam-2", "seam-3", "reef-edge", "far-rim", *[f"second-reef-{j}" for j in range(1, 6)])}
    spec = {name: {"layer": "ground", "size": list(size)} for name, size in gp.items()}
    spec["brood-well"] = {"layer": "ground", "size": [WELL, WELL]}
    spec["brood-well-glow"] = {"layer": "ground", "size": [WELL, WELL]}
    spec["pit"] = {"layer": "ground", "size": [PIT, PIT], "frames": PIT_FRAMES, "fps": 4}
    spec["pit-glow"] = {"layer": "ground", "size": [PIT, PIT], "frames": PIT_FRAMES, "fps": 4}
    spec["pit-shut"] = {"layer": "ground", "size": [PIT, PIT], "frames": 8, "fps": 2}
    spec["pit-shut-glow"] = {"layer": "ground", "size": [PIT, PIT], "frames": 8, "fps": 2}
    spec["bridge"] = {"layer": "low-air", "size": list(BRIDGE)}
    for s in "abc":
        spec[f"spore-sac-{s}"] = {"layer": "low-air", "size": list(SAC)}
    spec["dead-fleet"] = {"layer": "deep", "size": list(FLEET)}
    return spec


# The level's pits (the `pit` waves of the README's table): wave time, x; each opens with its centre 230 px down the
# screen (the tell from 30 px), clear of the spine's ridge (x 160-320) and the bridge's band
PIT_WAVES = [(12, 160), (43, 330), (56, 140), (70, 360), (93, 120), (117, 380), (142, 100), (158, 370), (173, 400),
             (195, 130), (210, 350), (222, 120), (239, 360), (258, 140)]
PIT_OPEN_Y = 230.0
WELL_ROW = 25000.0


def pits():
    """[(id, x, at)]: ``at`` = the scroll with the pit's centre at the top edge (VrellLevel.Pit)."""
    out = []
    for k, (t, x) in enumerate(PIT_WAVES):
        row = ground_row_at(t, PIT_OPEN_Y)
        out.append((f"P{k + 1}", x, round(row - SCREEN, 1), t))
    return out


def scenery_pits():
    """(row, x): breathing scenery pits: a few on the rim, rows of them in the brood fields, beside the spine, on the
    second reef, the ring of eight round the brood well."""
    rng = np.random.default_rng(SEED + 60)
    out = [(1500, 400), (3300, 70)]
    s2, s3 = g_seam(1), g_seam(2)
    r = s2 + 200
    while r < s3 - 150:
        for x in (rng.uniform(46, 90), rng.uniform(390, 434)):
            out.append((r + rng.uniform(-40, 40), x))
        r += 520
    for r in (12200, 13900, 15300):
        out.append((r, 62))
        out.append((r + 260, 418))
    for r in (18900, 20600):
        out.append((r, 60))
    for k in range(8):
        a = k * TAU / 8 + 0.2
        out.append((WELL_ROW + 175 * np.sin(a), 240 + 175 * np.cos(a)))
    level = [ground_row_at(t, PIT_OPEN_Y) for t, _ in PIT_WAVES]
    return [(round(float(r), 1), round(float(x), 1)) for r, x in out if min(abs(r - lr) for lr in level) > 90]


def placements():
    out = []
    pieces = ground_pieces()
    for name, (field, top) in pieces.items():
        h = field[1].shape[0]
        centre = top - (h - 1) / 2
        note = {"seam-2": "the section 1/2 ground seam (exact rows)", "seam-3": "the section 2/3 ground seam (exact rows)",
                "reef-edge": "the first reef's edge at the section 3/4 seam (exact rows); beyond it the gap (deep)",
                "far-rim": "the reef's far edge at the section 5/6 seam (exact rows); beyond it the nebula"}.get(
            name, f"section 4's ground: the second reef, rows {int(top - h + 1)}-{int(top + 1)} (exact)")
        out.append({"piece": name, "t": t_ground(centre + 0.5), "x": 240, "note": note})
    out.append({"piece": "brood-well", "t": t_ground(WELL_ROW), "x": 240,
                "note": "section 5: the brood well (organs and crawler paths keep 170 px clear of its centre)"})
    for pid, x, at, t in pits():
        out.append({"piece": "pit", "t": t_ground(at + SCREEN), "x": x,
                    "note": f"pit {pid}: the t={t} wave's (C4: frame by Pits' state)"})
    for k, (r, x) in enumerate(scenery_pits()):
        out.append({"piece": "pit-shut", "t": t_ground(r), "x": x,
                    "note": "scenery pits: breathing, never opening" if k == 0 else None})
    fl = FACTORS["low-air"]
    out.append({"piece": "bridge", "t": CLOCK.t_centre_at(167.5, 270.0, fl), "x": 240,
                "note": "section 4: the living bridge over the gap, on the screen t~160-175"})
    rng = np.random.default_rng(SEED + 90)
    for k, tc in enumerate((4.0, 9.5, 15.0, 22.0, 28.5, 35.0, 46.0, 63.0, 79.0, 98.0, 216.0, 233.0, 249.0, 262.0)):
        x = int(rng.integers(40, 440))
        out.append({"piece": f"spore-sac-{'abc'[k % 3]}", "t": CLOCK.t_centre_at(tc, 270.0, fl), "x": x,
                    "note": "low-air: drifting spore sacs" if k == 0 else None})
    out.append({"piece": "dead-fleet", "t": CLOCK.t_centre_at(273.0, 270.0, FACTORS["deep"]), "x": 250,
                "path": [[0.0, 0, 210.0]],
                "note": "section 6: the dead fleet far ahead (deep), seen beyond the far rim (held 210 px up: y 60 at t=273)"})
    return out


def fmt(v):
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, str) and ("'" in v or ":" in v or "," in v):
        return '"' + v + '"'
    return str(v)


def proposal_text():
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    lines = ["backdrop:",
             f"  # Presentation only: Level 45's hive reef (tools/art/act7_reef.py reef, step ART-C, sections from {src}).",
             "  # Vacuum: no haze. Deep: one continuous nebula field cut at the sections' seams (re-run the generator",
             "  # after moving a section). Ground: a tile set a section (1, 2, 3, 5); section 4's ground is the five",
             "  # second-reef pieces (exact rows) after the gap the first reef's edge leaves; section 6's is the far-rim",
             "  # piece, the nebula beyond. Seam pieces blend the tiles' own rows along a ragged line.",
             "  # C4 hooks: (1) BackdropAtmosphere adds every ground `<tile>-veins` and `<piece>-veins` image (additive,",
             "  # not listed here) with one slow smooth pulse k(t) <= 0.5 Hz (the veins' share above the tiles' 45 %);",
             "  # (2) BackdropPieces draws each placed `pit` (the level's pits P1.. in this order) at the frame of its",
             "  # Pits state: shut 0-7 (2 fps loop), tell 8-15 over 2 s (HELD holds 15), open 16-19 (4 fps loop), closing",
             "  # 20-23 over 0.5 s then shut. `pit-shut` pieces are scenery and just loop.",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, high-air unused",
             "  ramp: 2",
             "  haze_colour: 0a0612",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        h = Image.open(OUT / f"{name}.png").height
        lines.append(f"    {name}: {{layer: deep, height: {h}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    notes = {"rim-plates": "section 1: plates rising out of the nebula (gaps)", "brood-fields": "section 2",
             "spine": "section 3: the ridge (x 160-320)", "brood-heart": "section 5"}
    for name in GROUND_TILES:
        lines.append(f"    {name}: {{layer: ground, height: {TILE_H}}}   # {notes[name]}")
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
    lines.append("sections:   # ART-C's reading of the README's Layout (100 px/s throughout); C2 writes the real ones")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    lines.append("# vrell: the level's pits for C2 (VrellLevel.Pit: x, at = the scroll with its centre at the top edge);")
    lines.append(f"# each opens at its wave's t with its centre {PIT_OPEN_Y:g} px down the screen (its tell from "
                 f"{PIT_OPEN_Y - 200:g} px).")
    lines.append("# C2: check them against the organs' x and the Threadcrawlers' paths (VrellChecks) and move x if needed.")
    lines.append("# pits:")
    for pid, x, at, t in pits():
        lines.append(f"#   - {{x: {x}, at: {at}}}   # {pid}, the t={t} pit wave")
    lines.append(f"# ground: the first reef ends at rows ~{g_seam(3) + 25}-{g_seam(3) + 115}; the second reef's lip at "
                 f"~{g_seam(3) + 890}-{g_seam(3) + 970} (nothing ground-borne in between: the cyst, T2 and the t=173 pit "
                 f"lie above it); the far rim ends at ~{g_seam(5) + 105}-{g_seam(5) + 195}; the brood well at row "
                 f"{WELL_ROW:g}, x 240 (r 160)")
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


def frames_of(name, spec):
    n = spec.get("frames")
    return [img_of(f"{name}_{k}") for k in range(n)] if n else [img_of(name)]


def tile_rows(name, offset):
    key = ("tile", name)
    if key not in _CACHE:
        _CACHE[key] = np.array(img_of(name))
    arr = _CACHE[key]
    h = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[h - 1 - (rows % h)], rows


def layer_tiles(t, layer, suffix=""):
    f = FACTORS[layer]
    off = scroll(t) * f
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec.get("tiles") or [] if (n in DEEP) == (layer == "deep")]
        if not names:
            continue
        arr, rows = tile_rows(names[0] + suffix, off)
        sel = np.array([section_at_row(layer, r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def piece_xy(p, layer, t):
    f = FACTORS[layer]
    centre = scroll(p["t"]) * f + SCREEN / 2 + (p["path"][0][2] if p.get("path") else 0.0)
    return p["x"], SCREEN - (centre - scroll(t) * f)


def vein_k(t):
    return 0.5 + 0.5 * np.sin(TAU * 0.25 * t)


def pit_frame_at(t, wave_t):
    """The review's stand-in for C4's hook: a pit's frame at level time t for its wave at wave_t."""
    u = t - wave_t
    if u < -2.0 or u > 3.5:
        return int(t * 2) % 8
    if u < 0:
        return 8 + min(7, int((u + 2.0) * 4))
    if u < 3.0:
        return 16 + int(u * 4) % 4
    return 20 + min(3, int((u - 3.0) * 8))


def compose(t, k_veins=None, units=True):
    spec = pieces_spec()
    placed = placements()
    kv = vein_k(t) if k_veins is None else k_veins
    img = layer_tiles(t, "deep")
    for p in placed:
        if spec[p["piece"]]["layer"] == "deep":
            x, y = piece_xy(p, "deep", t)
            img = l23kit.paste_c(img, img_of(p["piece"]), x, y)
    img.alpha_composite(layer_tiles(t, "ground"))
    veins = np.array(layer_tiles(t, "ground", "-veins")).astype(np.float64)[..., :3]
    a = np.array(img).astype(np.float64)
    a[..., :3] += veins * kv
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    waves = {(p["x"], round(p["t"], 3)): wt for (pid, px, at, wt), p in
             zip(pits(), [q for q in placed if q["piece"] == "pit"])}
    for p in placed:
        name = p["piece"]
        if spec[name]["layer"] != "ground":
            continue
        x, y = piece_xy(p, "ground", t)
        if y < -400 or y > SCREEN + 400:
            continue
        if name in ("pit", "pit-shut"):
            fr = pit_frame_at(t, waves[(p["x"], round(p["t"], 3))]) if name == "pit" else int(t * 2) % 8
            img = l23kit.paste_c(img, img_of(f"{name}_{fr}"), x, y)
            img = l23kit.add_c(img, img_of(f"{name}-glow_{fr}"), x, y)
            continue
        img = l23kit.paste_c(img, img_of(name), x, y)
        if (OUT / f"{name}-veins.png").exists():
            img = l23kit.add_c(img, img_of(name + "-veins"), x, y, kv)
        if (OUT / f"{name}-glow.png").exists():
            img = l23kit.add_c(img, img_of(name + "-glow"), x, y)
    if units:
        img = stand_ins(img, t)
    for p in placed:
        if spec[p["piece"]]["layer"] == "low-air":
            x, y = piece_xy(p, "low-air", t)
            img = l23kit.paste_c(img, img_of(p["piece"]), x, y)
    return img


ORGANS_T = (28, 58, 80, 112, 152, 192, 226, 252)
ORGANS_X = (300, 250, 210, 260, 230, 300, 240, 280)            # the review's stand-in places (C2 sets the real ones)


def stand_ins(img, t):
    """The organs (shut, breathing), the call organ on the spine, the ship, a few Vrell bullets."""
    try:
        org, glw = load("spawning-organ"), load("spawning-organ-glow")
        horn = load("call-organ")
        root = load("call-organ-root")
        ship = load("ship")
    except FileNotFoundError:
        return img
    for to, x in zip(ORGANS_T, ORGANS_X):
        y = (t - to) * SPEED - 40
        if -50 < y < SCREEN + 50:
            k = int(t * 4) % 4
            img = l23kit.paste_c(img, org[k], x, y)
            img = l23kit.add_c(img, glw[k], x, y)
    y = (t - 127) * SPEED - 32
    if -40 < y < SCREEN + 40:
        img = l23kit.paste_c(img, horn[int(t * 2) % len(horn)], 240, y)
        for rx, ry in ROOTS:
            img = l23kit.paste_c(img, root[0], 240 + rx, y + ry)
    img = l23kit.paste_c(img, ship[2] if len(ship) > 2 else ship[0], 240, 470)
    for k, (bx, by) in enumerate(((120, 330), (200, 300), (360, 260), (330, 380))):
        u84.vorb(img, bx, by, 4, (u84.VIOLET, u84.TEAL)[k % 2])
    return img


# =========================================================================== reviews

def label(img, x, y, text):
    raster.draw_text(img, x, y, text.upper(), raster.LABEL)


def plate_bg(w, h, name="brood-fields"):
    try:
        arr = np.array(img_of(name))[:h, :w]
    except FileNotFoundError:
        return Image.new("RGBA", (w, h), artkit.PLATE)
    bg = Image.new("RGBA", (arr.shape[1], arr.shape[0]), (10, 6, 18, 255))
    bg.alpha_composite(Image.fromarray(arr, "RGBA"))
    if bg.size != (w, h):
        full = Image.new("RGBA", (w, h), (10, 6, 18, 255))
        full.alpha_composite(bg)
        bg = full
    return bg


def strip(frames, cell, scale=2, add=None, under=None, bg="brood-fields", step=6):
    out = Image.new("RGBA", (len(frames) * (cell[0] * scale + step), cell[1] * scale), (0, 0, 0, 0))
    base = plate_bg(cell[0], cell[1], bg)
    for i, fr in enumerate(frames):
        c = base.copy()
        if under is not None:
            c = l23kit.paste_c(c, under, cell[0] / 2, cell[1] / 2)
        if fr is not None:
            c = l23kit.paste_c(c, fr, cell[0] / 2, cell[1] / 2)
        if add is not None:
            c = l23kit.add_c(c, add[i] if isinstance(add, list) else add, cell[0] / 2, cell[1] / 2)
        out.alpha_composite(c.resize((cell[0] * scale, cell[1] * scale), Image.NEAREST),
                            (i * (cell[0] * scale + step), 0))
    return out


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


def review_organ():
    m, g = load("spawning-organ"), load("spawning-organ-glow")
    sw, swg = load("spawning-organ-swell"), load("spawning-organ-swell-glow")
    rl, rlg = load("spawning-organ-release"), load("spawning-organ-release-glow")
    ta, de = load("spawning-organ-tatters"), load("spawning-organ-death")
    st, cr = load("spawning-organ-stump")[0], load("spawning-organ-creep")
    irises = [m[s * 4] for s in range(6)]
    rows = [("THE IRIS (THE SKITTER PAIR'S 0.5 S TELL): SHUT .. OPEN, WITH ITS GLOW, 2X",
             strip(irises, OR_SIZE, add=[g[s * 4] for s in range(6)], under=cr[0])),
            ("THE BREATH (4 FRAMES), 2X", strip(m[:4], OR_SIZE, add=g[:4], under=cr[0])),
            ("THE PODS' SWELL (0.5 S) BEFORE A WHIRL SEED CLUSTER, 2X", strip(sw, OR_SIZE, add=swg, under=cr[0])),
            ("THE RELEASE: THE PODS BURST AND SHRINK BACK (0.3 S), 2X", strip(rl, OR_SIZE, add=rlg, under=cr[0])),
            ("THE DEATH: THE SAC TEARS, PODS FLUNG, THE ICHOR RING (EVERY OTHER FRAME), 1X",
             strip(ta[::2], BIG, scale=1, add=de[::2], under=cr[1])),
            ]
    husk = Image.new("RGBA", (5 * 134, 128), (0, 0, 0, 0))
    base = plate_bg(128, 128)
    for i, f in enumerate((0, 2, 4, 6, 8)):
        c = l23kit.paste_c(l23kit.paste_c(base.copy(), cr[f], 64, 64), st if f else m[0], 64, 64)
        husk.alpha_composite(c, (i * 134, 0))
    rows.append(("ALIVE OVER ITS ROOT VEINS (CREEP 0); THE TORN HUSK AS THE VEINS GO DARK OUTWARD (2, 4, 6, 8), 1X",
                 husk))
    # the play field: O2 coming down, swelling and releasing, then the iris; shot down at the end
    gif = []
    for i in range(70):
        t = 58.0 + i * 0.1
        f = compose(t, units=False)
        y = (t - 58) * SPEED - 40
        x = 250
        f = l23kit.paste_c(f, cr[0], x, y)
        u = t - 58 - 0.4
        if 63.4 <= t < 63.9:
            j = min(15, int((t - 63.4) / 0.5 * 16))
            f = l23kit.paste_c(f, cr[min(8, j // 2)], x, y)
            f = l23kit.paste_c(f, ta[j], x, y)
            f = l23kit.add_c(f, de[j], x, y)
        elif t >= 63.9:
            f = l23kit.paste_c(f, cr[8], x, y)
            f = l23kit.paste_c(f, st, x, y)
        elif 1.0 <= u < 1.5:
            j = min(5, int((u - 1.0) / 0.5 * 6))
            f = l23kit.add_c(l23kit.paste_c(f, sw[j], x, y), swg[j], x, y)
        elif 1.5 <= u < 1.8:
            j = min(5, int((u - 1.5) / 0.3 * 6))
            f = l23kit.add_c(l23kit.paste_c(f, rl[j], x, y), rlg[j], x, y)
        elif 2.5 <= u < 3.3:
            s = min(5, int((u - 2.5) / 0.5 * 6)) if u < 3.0 else 5
            f = l23kit.add_c(l23kit.paste_c(f, m[s * 4], x, y), g[s * 4], x, y)
        else:
            k = int(t * 4) % 4
            f = l23kit.add_c(l23kit.paste_c(f, m[k], x, y), g[k], x, y)
        ship = load("ship")
        f = l23kit.paste_c(f, ship[2], 200, 470)
        gif.append(f.crop((80, 0, 420, 400)))
    field = Image.new("RGBA", (3 * 346, 400), artkit.PLATE)
    for j, i in enumerate((16, 20, 30)):
        field.alpha_composite(gif[i], (j * 346, 0))
    rows.append(("1X OVER LEVEL 45'S REEF (PRODUCTION BACKDROP): THE SWELL, THE RELEASE, THE IRIS", field))
    artkit.save_review(sheet_of("SPAWNING ORGAN (FINAL R90): A.5 B, ONE PALETTE",
                                rows), gif, CONCEPT, "spawning-organ", fps=10)


def review_props():
    shut, rise, drift, glow = (load("call-organ"), load("call-organ-rise"), load("call-organ-drift"),
                               load("call-organ-glow"))
    root, rb = load("call-organ-root"), load("call-organ-root-break")
    cy, cb = load("courier-cyst"), load("courier-cyst-break")
    rooted = []
    base = plate_bg(*SHUT, name="spine")
    for k in (0, 2, 4, 6):
        c = l23kit.paste_c(base.copy(), shut[k], SHUT[0] / 2, SHUT[1] / 2)
        for rx, ry in ROOTS:
            c = l23kit.paste_c(c, root[0], SHUT[0] / 2 + rx, SHUT[1] / 2 + ry)
        rooted.append(c.resize((SHUT[0] * 2, SHUT[1] * 2), Image.NEAREST))
    rr = Image.new("RGBA", (4 * (SHUT[0] * 2 + 6), SHUT[1] * 2), (0, 0, 0, 0))
    for i, c in enumerate(rooted):
        rr.alpha_composite(c, (i * (SHUT[0] * 2 + 6), 0))
    rows = [("THE CALL ORGAN ROOTED ON THE SPINE, ITS RIBS BREATHING (FOUR ROOT KNOTS DRAWN OVER IT), 2X", rr),
            ("A ROOT: WHOLE, HIT, CUT, 2X", strip(root, (28, 28), bg="spine")),
            ("THE ROOT TORN THROUGH (EVERY OTHER FRAME), 2X", strip(rb[::2], (48, 48), bg="spine")),
            ("THE ROOTS CUT: THE HORN LIFTING OVER 2 S (EVERY OTHER FRAME) WITH ITS GLOW, 2X",
             strip(rise[::2], (128, 96), add=glow[0:10:2], bg="spine")),
            ("LOOSE: DRIFTING (EVERY OTHER FRAME) IN ITS GLOW'S LOOP, 2X",
             strip(drift[::2], (128, 96), add=glow[10:14], bg="second-reef-3")),
            ("THE COURIER'S CYST: WHOLE, CRACKED, BROKEN OPEN; ITS BREAK (EVERY OTHER FRAME), 2X",
             strip(cy, (40, 40), bg="second-reef-2")),
            ("", strip(cb[::2], (64, 64), bg="second-reef-2"))]
    gif = []
    base = plate_bg(260, 200, "spine")
    for i in range(60):
        t = i / 10
        c = base.copy()
        cx, cy0 = 130, 120
        cut = min(4, int(t / 0.5))
        if t < 2.0:
            c = l23kit.paste_c(c, shut[int(t * 2) % N_SHUT], cx, cy0)
        for j, (rx, ry) in enumerate(ROOTS):
            dead = j < cut
            c = l23kit.paste_c(c, root[2 if dead else 0], cx + rx, cy0 + ry)
            if dead and t - 0.5 * (j + 1) < 0.4:
                c = l23kit.paste_c(c, rb[min(7, int((t - 0.5 * (j + 1)) / 0.4 * 8))], cx + rx, cy0 + ry)
        if 2.0 <= t < 4.0:
            j = min(9, int((t - 2.0) / 2.0 * 10))
            c = l23kit.add_c(l23kit.paste_c(c, rise[j], cx, cy0 - 20 * (t - 2)), glow[j], cx, cy0 - 20 * (t - 2))
        elif t >= 4.0:
            j = int(t * 8) % 8
            c = l23kit.add_c(l23kit.paste_c(c, drift[j], cx, cy0 - 40), glow[10 + int(t * 6) % 6], cx, cy0 - 40)
        gif.append(c.resize((520, 400), Image.NEAREST))
    artkit.save_review(sheet_of("L45 OBJECTS (FINAL R90): CALL ORGAN, CYST", rows),
                       gif, CONCEPT, "l45-props", fps=10)


def review_spore():
    veil_, clouds, burns = load("spore-cloud-veil")[0], load("spore-cloud"), load("spore-cloud-burn")
    gif = []
    ship = load("ship")
    for i in range(90):
        t = i / 30
        f = compose(70.0 + t, units=False).crop((60, 60, 380, 400))
        for j, (cx, cy, sc) in enumerate(((110, 120 + 20 * t, 1.25), (240, 230 + 20 * t, 1.0))):
            if j == 1 and t > 2.0:
                continue
            v = veil_.resize((int(SPORE_N * sc), int(SPORE_N * sc)), Image.BILINEAR)
            burning = j == 1 and t >= 1.0
            k = 1.0 if not burning else max(0.0, 1 - (t - 1.0))
            vv = v.resize((max(1, int(v.width * k)), max(1, int(v.height * k))), Image.BILINEAR)
            f = l23kit.paste_c(f, vv, cx, cy)
            fr = burns[min(9, int((t - 1.0) * 10))] if burning else clouds[int(t / 0.5) % 8]
            f = l23kit.add_c(f, fr.resize((int(SPORE_N * sc), int(SPORE_N * sc)), Image.BILINEAR), cx, cy)
        f = l23kit.paste_c(f, ship[2], 120, 150 + 30 * t)
        u84.vorb(f, 200, 100 + 40 * t, 4, u84.VIOLET)
        gif.append(f)
    field = Image.new("RGBA", (3 * 326, 340), artkit.PLATE)
    for j, i in enumerate((10, 45, 70)):
        field.alpha_composite(gif[i], (j * 326, 0))
    rows = [("THE VEIL (NORMAL ALPHA, AT MOST 25 %) OVER THE BROOD FIELDS, 1X", strip([veil_], (176, 176), scale=1)),
            ("THE CLOUD'S 8 FRAMES (A 4 S LOOP), ADDITIVE OVER THE VEIL, 1X",
             strip([veil_] * 5, (176, 176), scale=1, add=clouds[:5])),
            ("", strip([veil_] * 3, (176, 176), scale=1, add=clouds[5:])),
            ("THE BURN (1 S, A RAGGED EMBER EDGE EATING INWARD), 1X", strip([None] * 5, (176, 176), scale=1,
                                                                             add=burns[:5])),
            ("", strip([None] * 5, (176, 176), scale=1, add=burns[5:])),
            ("1X OVER LEVEL 45'S REEF: THE SHIP INSIDE A CLOUD, A SECOND CLOUD BURNT AWAY, A VRELL BULLET ABOVE", field)]
    artkit.save_review(sheet_of("SPORE CLOUD (FINAL R90): LEVEL 45", rows), gif,
                       VRELL, "spore-cloud", fps=30)


SCREENS = (6.0, 30.0, 61.0, 128.0, 166.0, 190.0, 248.0, 276.0)


def section_at_t(t):
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]):
            return i
    return len(SECTIONS) - 1


def review_reef():
    shots = [compose(t, k_veins=1.0) for t in SCREENS]
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * 290 + 30 + 420, "LEVEL 45 BACKDROP (FINAL R90): HIVE REEF",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, (t, s) in enumerate(zip(SCREENS, shots)):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        raster.draw_text(sheet, x, y, f"T={t:g} S ({SECTIONS[section_at_t(t)]['name'].upper()})", raster.LABEL)
        sheet.alpha_composite(s.resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + 2 * 290 + 6
    raster.draw_text(sheet, 12, y, "SCREENS AT HALF SIZE, VEINS AT THEIR PULSE'S PEAK. THE PITS' 24 FRAMES "
                                   "(SHUT, TELL, OPEN, CLOSING) AT 1X:", raster.LABEL)
    row = Image.new("RGBA", (24 * 40, 72), (0, 0, 0, 0))
    base = plate_bg(72, 72)
    for f in range(PIT_FRAMES):
        c = l23kit.add_c(l23kit.paste_c(base.copy(), img_of(f"pit_{f}"), 36, 36), img_of(f"pit-glow_{f}"), 36, 36)
        row.alpha_composite(c.crop((16, 16, 56, 56)), (f * 40, 16))
    sheet.alpha_composite(row, (12, y + 10))
    y += 104
    raster.draw_text(sheet, 12, y, "PIECES (QUARTER SIZE): THE WELL, THE BRIDGE, THE DEAD FLEET, A SPORE SAC; THE "
                                   "TILES (RIM, BROOD FIELDS, SPINE, BROOD HEART)", raster.LABEL)
    x = 12
    for name in ("brood-well", "bridge", "dead-fleet", "spore-sac-a", "rim-plates", "brood-fields", "spine",
                 "brood-heart"):
        im = img_of(name)
        bg = Image.new("RGBA", im.size, (10, 6, 18, 255))
        bg.alpha_composite(im)
        if (OUT / f"{name}-glow.png").exists():
            bg = l23kit.add_c(bg, img_of(name + "-glow"), im.width / 2, im.height / 2)
        if (OUT / f"{name}-veins.png").exists():
            bg = l23kit.add_c(bg, img_of(name + "-veins"), im.width / 2, im.height / 2)
        sc = 4 if im.height > 200 else 1
        sm = bg.resize((max(1, im.width // sc), max(1, im.height // sc)), Image.LANCZOS)
        if sm.height > 300:
            sm = sm.crop((0, 0, sm.width, 300))
        sheet.alpha_composite(sm, (x, y + 14))
        x += sm.width + 8
    frames = [compose(t).resize((240, 270), Image.LANCZOS) for t in np.arange(0, END, 4.0)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=4)


# =========================================================================== check

SETS = {"organ": [("spawning-organ", 24, OR_SIZE), ("spawning-organ-glow", 24, OR_SIZE),
                  ("spawning-organ-swell", 6, OR_SIZE), ("spawning-organ-swell-glow", 6, OR_SIZE),
                  ("spawning-organ-release", 6, OR_SIZE), ("spawning-organ-release-glow", 6, OR_SIZE),
                  ("spawning-organ-tatters", 16, BIG), ("spawning-organ-death", 16, BIG),
                  ("spawning-organ-stump", 1, OR_SIZE), ("spawning-organ-creep", 9, BIG)],
        "call": [("call-organ", N_SHUT, SHUT), ("call-organ-rise", N_RISE, RISE_SIZE),
                 ("call-organ-drift", N_DRIFT, RISE_SIZE), ("call-organ-glow", 16, CALL_GLOW),
                 ("call-organ-root", 3, (28, 28)), ("call-organ-root-break", 8, (48, 48))],
        "cyst": [("courier-cyst", 3, (40, 40)), ("courier-cyst-break", 8, (64, 64))],
        "spore": [("spore-cloud-veil", 1, (SPORE_N, SPORE_N)), ("spore-cloud", 8, (SPORE_N, SPORE_N)),
                  ("spore-cloud-burn", 10, (SPORE_N, SPORE_N))]}
CUT = {"seam-2", "seam-3", "reef-edge", "far-rim", *[f"second-reef-{j}" for j in range(1, 6)]}


def lit_border(img):
    a = np.array(img.convert("RGBA"))
    edge = np.concatenate([a[0], a[-1], a[:, 0], a[:, -1]])
    return int((edge[:, :3].max(axis=-1) > 0).sum())


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
            veil = sum(int((np.array(fr)[[0, -1], :, 3] > 0).sum() + (np.array(fr)[:, [0, -1], 3] > 0).sum())
                       for fr in frames) if name.endswith("-veil") else 0
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src and share < 0.05 and not lit \
                and not veil
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %{f', {lit + veil} lit border px' if lit + veil else ''}")
    spec = pieces_spec() if OUT.exists() else {}
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        v, c = checker_share(im)
        share = c / max(v, 1)
        ok, msg = im.text.get(artkit.SOURCE_KEY) == SOURCE and share < 0.05, []
        stem = p.stem
        if stem in DEEP:
            ws = wrap_score(np.array(im.convert("RGBA")))
            ok &= ws < 3.0
            msg.append(f"wrap {ws:.2f}")
        if stem in GROUND_TILES:
            ws = jovkit.wrap_score_alpha(np.array(im.convert("RGBA"))) if stem == "rim-plates" else \
                wrap_score(np.array(im.convert("RGBA")))
            ok &= ws < (3.0 if stem == "rim-plates" else 1.5)   # step C4: the game's WRAP_LIMIT for the opaque tiles
            msg.append(f"wrap {ws:.2f}")
        base = stem.split("_")[0]
        if base.endswith("-glow") or (base.endswith("-veins") and base.removesuffix("-veins") not in CUT
                                      and base.removesuffix("-veins") not in GROUND_TILES):
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

JOBS = {"organ": _organ, "horn": _horn, "root": _root, "cyst": _cyst}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "act7-reef"


def _model_hash():
    text = Path(__file__).read_text(encoding="utf-8")
    a, b = text.index("# =========================================================================== the spawning organ"), \
        text.index("# =========================================================================== the spore cloud")
    return hashlib.sha1(text[a:b].encode()).hexdigest()[:12]


def run(jobs):
    """The renders, cached in the system temp dir by the model code's hash and the job."""
    CACHE.mkdir(parents=True, exist_ok=True)
    h = _model_hash()
    paths = [CACHE / f"{hashlib.sha1((h + repr(j)).encode()).hexdigest()[:20]}.png" for j in jobs]
    todo = [i for i, path in enumerate(paths) if not path.exists()]
    if todo:
        with ProcessPoolExecutor(max_workers=min(6, __import__("os").cpu_count() or 1)) as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(path).convert("RGBA") for path in paths]


PARTS = ("organ", "call", "cyst", "spore", "reef")

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    parts = [a for a in args if a in PARTS] or list(PARTS)
    build = "--review" not in args
    if build:
        jobs = []
        spans = {}
        for part, make in (("organ", organ_jobs), ("call", call_jobs), ("cyst", lambda: [("cyst", s) for s in range(3)])):
            if part in parts:
                spans[part] = (len(jobs), len(jobs) + len(make()))
                jobs += make()
        res = run(jobs) if jobs else []
        for part, fn in (("organ", build_organ), ("call", build_call), ("cyst", build_cyst)):
            if part in spans:
                fn(res[spans[part][0]:spans[part][1]])
        if "spore" in parts:
            build_spore()
        if "reef" in parts:
            build_reef()
            write_proposal()
    if "reef" in parts:
        review_reef()
    if "organ" in parts:
        review_organ()
    if "call" in parts or "cyst" in parts:
        review_props()
    if "spore" in parts:
        review_spore()
