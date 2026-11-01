#!/usr/bin/env python3
"""Production art: Level 39's refinery units and Io's eruptions (M10 part E batch, concept round 77, step ART-E):
the cracking tower and the pump station (design/campaign/act-6-jovian-storm/level-39-io-flyby, *The cracking tower and
the pump station*), the supply depot's latch and the survey hut's lock (its *Secrets and pickups*), and the eruption's
vent tell, burst, umbrella and burning rocks (design/world/jovian, *Hazards & set pieces*: eruptions, J3), replacing
part A's stand-ins (game/.../EruptionLooks.java's discs).

New models on the frozen render package (no round-72 proposal: Level 39's README asks for production art straight
away), lit from the top left as every sprite: the Ascendancy's refinery steel in gunmetal and charcoal with its gold
desaturated a step (Q10 a's L39 rule: gold units against sulphur read by their dark hulls), red lamps. Io is a
near-vacuum (design/world/jovian *Media per level*: the belt's vacuum rules): deaths flash short and throw debris in a
ring that does not rise, wrecks do not smoke, an umbrella of sulphur haze opens over a vent and settles. Every glow,
haze and shadow is a smooth float field written unquantised (far more than 32 steps, no ordered dither: the 2026-10-30
dither audit, DitherGuardTest's measure); additive sets are premultiplied on black (GL_SRC_ALPHA, GL_ONE).

The cracking tower is seen from straight above through a mild perspective (the camera 11 model units up, as
artkit.perspective): a tapered column, its side a ring band round the top, three gold collars, the gold flare cap with
its red lamps at the middle, three pipe stubs leaving the base toward the yard's pipes (east, west, south: Level 39's
tower pads continue them), its long soft shadow to the bottom right in the sprite's alpha (smooth). Its fall (vacuum):
a short white flash, the column snapping above its second collar, the top with the flare cap tumbling off to the bottom
right and landing beside the stump, a ring of burning chunks flung out along the ground (they do not rise), the fires
at the break going out; the wreck (the stump, scorched, the cap lying beside it, no glow) stays.

Outputs (build/art-held/sprites/ until the data claims the root - E2 writes ``design/enemies/ground/cracking-tower`` and
``.../pump-station`` (their roots then go to assets/sprites/); the depot's latch, the hut's lock and the eruption sets
are claimed by the game's looks (E4), held until then):
  cracking-tower            96x96 normal: the tower, its shadow in the alpha (hitbox 72x72 centred)
  cracking-tower-flare_0..7 48x48 additive, centred on the tower: the flare cap's flame flickering, 10 fps
  cracking-tower-fall_0..11 160x160 normal, centred: the fall (the body, the stump, the cap, the chunks), 12 fps
  cracking-tower-fall-glow_0..11 160x160 additive: the flash, the fires at the break, the burning chunks
  cracking-tower-wreck      96x96 normal: the stump with the cap lying beside it (the fall's last frame), stays
  pump-station              40x40 normal: the pump housing straddling the rack's pipes (hitbox 32x32), its lamp lit
  pump-station-wreck        40x40 normal: the housing burst open and scorched, the lamp dead
  depot-latch_0..1          24x24 normal: the supply depot's latch (an orange clamp, an amber lamp), then shot open
  survey-hut-lock_0..1      24x24 normal: the survey hut's dark door lock (no lights), then blown
  eruption-vent-tell_0..11  96x96 additive, on the vent: the mouth glowing orange-white and the cracks spreading over
                            the tell (frame = the tell's progress x 12)
  eruption-vent-burst_0..7  96x96 additive: the throw (the mouth's flare, a spray of sparks), 16 fps (0.5 s, the rise)
  eruption-umbrella_0..11   208x208 normal, smooth alpha: the umbrella of sulphur haze opening and settling over the
                            vent, 3 fps (4 s, EruptionLooks' UMBRELLA_SECONDS; peak alpha 0.5)
  eruption-rock_0..15       24x24 normal: a burning rock (dark basalt, glowing cracks), its tumble (frame k = k x 22.5
                            degrees), 12 fps; the game scales it over the rise and the fall (EruptionLooks.rockScale)
  eruption-rock-glow        40x40 additive: its ember halo (drawn over the rock at the rock's scale)
  eruption-rock-break_0..7  48x48 normal: the rock shot apart (or struck): chips flung out, a dust ring, 16 fps
  eruption-rock-break-glow_0..7  48x48 additive: the hot core's flash and the embers of the break
Review (round 77): design/campaign/act-6-jovian-storm/level-39-io-flyby/concept/refinery-final-r77-a.png/.gif (the
tower burning, its fall and wreck, the pump, the latch and the lock on Level 39's backdrop) and
design/world/jovian/concept/eruption-final-r77-a.png/.gif (a vent's tell, burst, rocks in their fan, a rock shot, the
umbrella, at 1x on Level 39's plains).

Run: python3 tools/art/refinery_units.py [tower] [pump] [props] [eruption] [--review] [--check]
     (no subject = all, about 1 min; --review rebuilds only the review files; --check lists every set with its folder,
     size and frame count and fails on a missing one, a missing Source chunk or a checker share of 5 % or more)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import l23kit
from artkit import DESIGN, ROOT, TAU

from jovkit import blur, checker_share, glow, rgba, smoothstep  # noqa: E402
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "refinery_units.py"
BATCH = "M10 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r77"
artkit.REVIEW_ROUND = ROUND
L39_CONCEPT = DESIGN / "campaign" / "act-6-jovian-storm" / "level-39-io-flyby" / "concept"
JOV_CONCEPT = DESIGN / "world" / "jovian" / "concept"
BACKDROP = ROOT / "assets" / "backdrop" / "level-39"
PX = 20.0                                  # px per model unit (the belt's play scale)
CAMERA = 11.0                              # the tower's mild perspective (artkit.perspective)
TOWER, FALL, FLARE = 96, 160, 48
PUMP, PROP = 40, 24
VENT, UMB = 96, 208
ROCK, ROCK_GLOW, BREAK = 24, 40, 48

# materials (the Ascendancy's refinery steel; Q10 a's L39 rule: the gold a step desaturated against the sulphur)
STEEL = Material((0.24, 0.24, 0.25), metal=0.55, shininess=70, spec=0.55)
CHARCOAL = Material((0.15, 0.15, 0.16), metal=0.3, shininess=40, spec=0.3)
GOLD = Material((0.6, 0.49, 0.25), metal=0.85, shininess=90, spec=0.85)
CONCRETE = Material((0.46, 0.43, 0.37), metal=0.0, shininess=12, spec=0.12)
SOOT = Material((0.08, 0.07, 0.065), metal=0.1, shininess=10, spec=0.08)
SCORCHED = Material((0.16, 0.13, 0.11), metal=0.3, shininess=20, spec=0.15)
FLARE_TIP = Material((0.2, 0.1, 0.02), emission=(1.6, 0.95, 0.42))
RED_LAMP = Material((0.2, 0.02, 0.02), emission=(1.4, 0.18, 0.12))
AMBER_LAMP = Material((0.2, 0.12, 0.02), emission=(1.4, 0.8, 0.25))
DEAD_LAMP = Material((0.09, 0.08, 0.08), metal=0.2, shininess=120, spec=0.9)
ORANGE = Material((0.75, 0.36, 0.08), metal=0.3, shininess=50, spec=0.5)
BASALT = Material((0.17, 0.14, 0.12), shininess=10, spec=0.12)
SHADOW = np.array((14, 11, 8.0))
FIRE = np.array((255, 128, 40.0))
FIRE_CORE = np.array((255, 232, 170.0))
EMBER = np.array((255, 104, 30.0))
SULPHUR_HAZE = np.array((226, 212, 150.0))


def save_set(name, frames, single=False):
    l23kit.write(name, frames, SOURCE, single=single)


def render(scene, mats, size, extent, crisp=80, camera=None, z_top=4.6):
    if camera:
        scene = artkit.perspective(scene, camera)
    w, h = (size, size) if isinstance(size, int) else size
    hi, factor = artkit.render_hi(scene, mats, (w, h), extent, z_top=z_top)
    return artkit.native(hi, factor, crisp)


def sd_torus_z(p, c, big, small):
    q = p - np.asarray(c, float)
    return np.hypot(np.hypot(q[:, 0], q[:, 1]) - big, q[:, 2]) - small


def oct_r(x, y):
    return np.maximum(np.maximum(np.abs(x), np.abs(y)), (np.abs(x) + np.abs(y)) * 0.7071)


def rot_z(p, a):
    c, s = np.cos(a), np.sin(a)
    q = p.copy()
    q[:, 0], q[:, 1] = c * p[:, 0] + s * p[:, 1], -s * p[:, 0] + c * p[:, 1]
    return q


def rot_axis(p, axis, a):
    k = np.asarray(axis, float)
    k = k / np.linalg.norm(k)
    c, s = np.cos(a), np.sin(a)
    return p * c + np.cross(k, p) * s + np.outer(p @ k, k) * (1 - c)


# =========================================================================== the cracking tower

COL_Z0, COL_Z1 = 0.4, 3.1                  # the column's foot and top (model units)
COL_R0, COL_R1 = 1.05, 0.5                 # its radius there (tapered)
COLLARS = (1.1, 1.9, 2.6)
BREAK_Z = 2.25                             # the fall snaps it between the second and the third collar
T_MATS = [STEEL, CHARCOAL, GOLD, CONCRETE, FLARE_TIP, RED_LAMP, SOOT, SCORCHED]
M_STEEL, M_CHAR, M_GOLD, M_CONC, M_TIP, M_RED, M_SOOT, M_SCORCH = range(8)


def col_r(z):
    return COL_R0 + (COL_R1 - COL_R0) * (z - COL_Z0) / (COL_Z1 - COL_Z0)


def column_sd(p, z0, z1):
    r = np.hypot(p[:, 0], p[:, 1])
    slope = (COL_R0 - COL_R1) / (COL_Z1 - COL_Z0)
    side = (r - col_r(np.clip(p[:, 2], COL_Z0, COL_Z1))) / np.sqrt(1 + slope * slope)
    return np.maximum(side, np.maximum(p[:, 2] - z1, z0 - p[:, 2]))


def tower_scene(part="whole"):
    """``whole`` the intact tower; ``stump`` the column snapped at BREAK_Z (a jagged lip, scorched); ``top`` the part
    above the break (the cap, the upper collars), its own origin at the break's centre."""
    def ribs(p, n):
        a = np.arctan2(p[:, 1], p[:, 0])
        return 0.86 + 0.14 * (np.cos(a * 18) > 0.6)

    steel = Material(STEEL.albedo, STEEL.metal, STEEL.shininess, STEEL.spec, pattern=ribs)
    mats = [steel] + T_MATS[1:]

    def lower(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        slab = np.maximum(oct_r(x, y) - 1.62, np.abs(z - 0.08) - 0.08)
        foot = sd_cylinder_z(p, (0, 0, 0.27), 1.18, 0.13)
        d, m = union((slab, M_CONC), (foot, M_CHAR), k=0.02)
        for ang in (0.0, np.pi, -np.pi / 2):                     # pipe stubs east, west, south
            c, s = np.cos(ang), np.sin(ang)
            pipe = sd_capsule(p, (0.9 * c, 0.9 * s, 0.3), (1.62 * c, 1.62 * s, 0.3), 0.12)
            flange = sd_cylinder_z(rot_z(p - np.array((1.28 * c, 1.28 * s, 0.3)), ang)[:, [2, 1, 0]],
                                   (0, 0, 0), 0.17, 0.035)
            d, m = union((d, m), (pipe, M_STEEL), (flange, M_GOLD), k=0.0)
        return d, m

    def upper(p, z_from):
        d = column_sd(p, z_from, COL_Z1)
        m = np.full(len(p), M_STEEL)
        for zc in COLLARS:
            if zc < z_from:
                continue
            tor = sd_torus_z(p, (0, 0, zc), col_r(zc) + 0.02, 0.085)
            d, m = union((d, m), (tor, M_GOLD))
        cap = sd_cylinder_z(p, (0, 0, COL_Z1 + 0.1), 0.52, 0.1)
        hole = sd_cylinder_z(p, (0, 0, COL_Z1 + 0.2), 0.3, 0.08)
        cap = np.maximum(cap, -hole)
        tip = sd_sphere(p, (0, 0, COL_Z1 + 0.14), 0.2)
        d, m = union((d, m), (cap, M_GOLD), (tip, M_TIP))
        for a in np.linspace(0, TAU, 4, endpoint=False) + TAU / 8:
            lamp = sd_sphere(p, (0.47 * np.cos(a), 0.47 * np.sin(a), COL_Z1 + 0.22), 0.065)
            d, m = union((d, m), (lamp, M_RED))
        return d, m

    if part == "whole":
        def scene(p):
            d, m = lower(p)
            return union((d, m), upper(p, COL_Z0))
    elif part == "stump":
        def scene(p):
            d, m = lower(p)
            a = np.arctan2(p[:, 1], p[:, 0])
            lip = BREAK_Z + 0.12 * np.sin(a * 5) + 0.07 * np.sin(a * 11 + 1.3)
            col = column_sd(p, COL_Z0, BREAK_Z + 0.2)
            col = np.maximum(col, p[:, 2] - lip)
            inner = np.hypot(p[:, 0], p[:, 1]) - (col_r(BREAK_Z) - 0.12)
            col = np.maximum(col, -np.maximum(inner, BREAK_Z - 0.35 - p[:, 2]))   # hollow: the torn shell
            tor = [(sd_torus_z(p, (0, 0, zc), col_r(zc) + 0.02, 0.085), M_SOOT) for zc in COLLARS if zc < BREAK_Z]
            d, m = union((d, m), (col, M_SCORCH), *tor)
            soot = (p[:, 2] > BREAK_Z - 0.45) | ((oct_r(p[:, 0], p[:, 1]) < 1.4) & (p[:, 2] < 0.2)
                                                 & (np.sin(p[:, 0] * 7) * np.sin(p[:, 1] * 6) > 0.1))
            m = np.where(soot & (m != M_GOLD), M_SOOT, m)
            m = np.where(m == M_GOLD, M_SOOT, m)
            m = np.where(m == M_STEEL, M_SCORCH, m)
            return d, m
    else:
        def scene(p):
            q = p + np.array((0, 0, BREAK_Z))
            d, m = upper(q, BREAK_Z)
            d = np.maximum(d, BREAK_Z - q[:, 2])
            m = np.where(m == M_STEEL, M_SCORCH, m)
            m = np.where(m == M_TIP, M_SOOT, m)
            m = np.where(m == M_RED, M_CHAR, m)
            return d, m
    return scene, mats


def tower_shadow(size, top_z, cx, cy, length=0.5, alpha=0.48):
    """The column's soft shadow on the ground to the bottom right (the sun at the top left): discs of the column's
    radius along its height, offset by the height x ``length``; a smooth float alpha."""
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float64) + 0.5
    a = np.zeros((size, size))
    for z in np.linspace(0, top_z, 40):
        r = (1.62 if z < 0.18 else col_r(max(z, COL_Z0))) * PX
        off = z * PX * length / np.sqrt(2)
        a = np.maximum(a, smoothstep(r + 1.5, r - 1.5, np.hypot(xx - cx - off, yy - cy - off)))
    return blur(a, 1.6) * alpha


def with_shadow(body, shadow):
    """A body (1-bit alpha) over its smooth shadow: the shadow where the body is not."""
    b = np.array(body.convert("RGBA")).astype(np.float64)
    op = b[..., 3] > 0
    rgb = np.where(op[..., None], b[..., :3], SHADOW)
    alpha = np.where(op, 1.0, shadow)
    return rgba(rgb, alpha)


def _tower(part, size, tumble=0.0, axis=(1, -1, 0.3)):
    scene, mats = tower_scene(part)
    if tumble:
        base = scene
        scene = lambda p: base(rot_axis(p, axis, -tumble))  # noqa: E731
    return render(scene, mats, size, size / PX, camera=CAMERA if part != "top" else None)


def flare_frames(n=8, w=FLARE, seed=3911):
    """The flare cap's flame from above: a white-hot core, an orange corona with ragged tongues flickering."""
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    r, a = np.hypot(x, y), np.arctan2(y, x)
    out = []
    phases = rng.uniform(0, TAU, (4, 2))
    for f in range(n):
        t = TAU * f / n
        tongue = sum(0.18 / (k + 1) * np.sin(a * (3 + 2 * k) + phases[k, 0] + t * (k + 1)) for k in range(4))
        reach = 10.5 * (1 + tongue + 0.06 * np.sin(t * 2 + phases[0, 1]))
        corona = np.exp(-(r / np.maximum(reach, 3)) ** 2.2)
        core = np.exp(-(r / (4.2 + 0.5 * np.sin(t + 1))) ** 2)
        light = corona[..., None] * FIRE * 0.95 + core[..., None] * FIRE_CORE
        light *= (0.92 + 0.08 * np.sin(t * 3))
        out.append(artkit.additive(rgba(np.clip(light, 0, 255), np.ones((w, w)))))
    return out


FALL_N = 12
CAP_DIR = np.array((0.78, 0.62))           # the top tumbles off to the bottom right (screen x right, y down)
CAP_LAND = 34.0                            # px from the centre where the cap lies
CAP_W = 64


def cap_place(f):
    """The top's flight in fall frame f: (offset px, scale, tumble angle, on the ground)."""
    u = min(1.0, max(0.0, (f - 1) / 7))
    off = CAP_LAND * (1 - (1 - u) ** 2)
    scale = 1.38 - 0.38 * u ** 1.6                # from the break's height (perspective) to the ground
    return off, scale, 0.25 + 1.65 * u, f >= 8


def chunks(seed=3913):
    rng = np.random.default_rng(seed)
    return [(TAU * k / 16 + rng.uniform(-0.18, 0.18), rng.uniform(46, 74), rng.uniform(2.0, 4.2),
             rng.uniform(0, TAU), rng.uniform()) for k in range(16)]


def fall_frames(whole, stump, tops):
    """The fall's body and glow frames (FALL x FALL, centred) and the wreck (TOWER x TOWER)."""
    c = FALL / 2
    shadow_whole = tower_shadow(FALL, COL_Z1 + 0.2, c, c)
    shadow_stump = tower_shadow(FALL, BREAK_Z + 0.1, c, c)
    bodies, glows = [], []
    ch = chunks()
    yy, xx = np.mgrid[0:FALL, 0:FALL].astype(np.float64) + 0.5
    for f in range(FALL_N):
        u = f / (FALL_N - 1)
        img = Image.new("RGBA", (FALL, FALL))
        if f < 2:
            body = Image.new("RGBA", (FALL, FALL))
            body.alpha_composite(whole, (int(c - whole.width / 2), int(c - whole.height / 2)))
            img = with_shadow(body, shadow_whole * (1 - 0.4 * f))
        else:
            body = Image.new("RGBA", (FALL, FALL))
            body.alpha_composite(stump, (int(c - stump.width / 2), int(c - stump.height / 2)))
            img = with_shadow(body, shadow_stump)
        light = np.zeros((FALL, FALL, 3))
        # the top tumbling off (from frame 1), landing beside the stump at frame 8, lying there
        if f >= 1:
            off, scale, ang, landed = cap_place(f)
            top = tops[min(f - 1, len(tops) - 1)]
            sz = max(8, int(round(CAP_W * scale)))
            top = top.resize((sz, sz), Image.LANCZOS)
            ta = np.array(top)
            ta[..., 3] = np.where(ta[..., 3] > 110, 255, 0)
            top = Image.fromarray(ta, "RGBA")
            px, py = c + CAP_DIR[0] * off, c + CAP_DIR[1] * off
            if not landed:                         # its shadow on the ground, a little ahead of it
                sh = smoothstep(sz * 0.33, sz * 0.18, np.hypot(xx - px - 6 * scale, yy - py - 6 * scale)) * 0.4
                arr = np.array(img).astype(np.float64)
                a0 = arr[..., 3] / 255
                a1 = a0 + sh * (1 - a0)
                arr[..., :3] = (arr[..., :3] * a0[..., None] + SHADOW * (sh * (1 - a0))[..., None]) / np.maximum(
                    a1, 1e-6)[..., None]
                arr[..., 3] = a1 * 255
                img = Image.fromarray(np.clip(np.round(arr), 0, 255).astype(np.uint8), "RGBA")
            l23kit.paste_c(img, top, px, py)
            if f < 10:
                glow(light, px, py, 5 * scale, FIRE, 0.5 * (1 - u))
        # the burning chunks flung out along the ground (no rise: they do not grow), slowing (vacuum: dust settles)
        d = ImageDraw.Draw(img)
        for a, speed, s, spin, kind in ch:
            dist = speed * (1 - (1 - min(1.0, u * 1.15)) ** 2) + 10
            x, y = c + np.cos(a) * dist, c + np.sin(a) * dist
            sp = spin + 5 * u
            pts = [(x + np.cos(sp + j * TAU / 4) * s * (1 + 0.35 * (j % 2)), y + np.sin(sp + j * TAU / 4) * s)
                   for j in range(4)]
            lit = 0.55 + 0.45 * np.cos(sp - 2.4)
            base = np.array((70, 66, 62)) if kind < 0.6 else np.array((96, 80, 44))
            col = tuple(int(v) for v in np.clip(base * (0.5 + 0.7 * lit) * (0.7 + 0.3 * (1 - u)), 0, 255))
            if f >= 1:
                d.polygon(pts, fill=col + (255,))
                glow(light, x, y, 2.6, EMBER, 0.9 * max(0.0, 1 - 1.1 * u) + 0.15)
        # the flash (short: the vacuum's), then the fires at the break dying down
        if f <= 2:
            k = (1.0, 0.55, 0.18)[f]
            glow(light, c, c, 34, (255, 236, 200), k)
            glow(light, c, c, 12, (255, 255, 240), k * 1.2)
        if f >= 2:
            fire = max(0.0, 1 - (f - 2) / 9)
            rr = col_r(BREAK_Z) * PX * (CAMERA / (CAMERA - BREAK_Z))
            ring = np.exp(-((np.hypot(xx - c, yy - c) - rr * 0.82) / 3.0) ** 2) * fire
            light += ring[..., None] * FIRE * 0.8
            glow(light, c, c, 7, FIRE, 0.6 * fire)
        bodies.append(img)
        glows.append(artkit.additive(rgba(np.clip(light, 0, 255), np.ones((FALL, FALL)))))
    lo = int(FALL / 2 - TOWER / 2)
    wreck = bodies[-1].crop((lo, lo, lo + TOWER, lo + TOWER))
    return bodies, glows, wreck


# =========================================================================== the pump station

def pump_scene(wrecked=False):
    mats = [STEEL, CHARCOAL, GOLD, RED_LAMP, SOOT, SCORCHED, DEAD_LAMP]

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        house = sd_box(p, (0, 0, 0.28), (0.5, 0.62, 0.26), 0.06)
        skid = sd_box(p, (0, 0, 0.04), (0.62, 0.74, 0.04), 0.02)
        motor = sd_capsule(p, (-0.34, 0.12, 0.62), (0.3, 0.12, 0.62), 0.2)
        band = sd_cylinder_z(rot_z(p - np.array((0.0, 0.12, 0.62)), 0)[:, [2, 1, 0]], (0, 0, 0), 0.23, 0.04)
        pipe = sd_capsule(p, (0.0, -0.95, 0.2), (0.0, 0.95, 0.2), 0.13)
        flanges = np.minimum(sd_cylinder_z(p[:, [0, 2, 1]], (0, 0.2, 0.7), 0.19, 0.035),
                             sd_cylinder_z(p[:, [0, 2, 1]], (0, 0.2, -0.7), 0.19, 0.035))
        lamp = sd_sphere(p, (0.34, -0.42, 0.56), 0.075)
        d, m = union((skid, 1), (pipe, 0), (house, 1), (flanges, 2), (motor, 0), (band, 2), k=0.0)
        d, m = union((d, m), (lamp, 3 if not wrecked else 6))
        fins = (np.abs(np.sin(x * 26)) > 0.75) & (z > 0.5) & (np.abs(y + 0.3) < 0.22) & (np.abs(x) < 0.42)
        m = np.where((m == 1) & fins, 0, m)
        if wrecked:
            torn = 0.3 + 0.08 * np.sin(x * 9 + 1) * np.cos(y * 7) + 0.05 * np.sin(x * 23 + y * 17)
            d = np.where((np.abs(x) < 0.48) & (np.abs(y) < 0.6), np.maximum(d, z - torn - 0.1), d)
            burnt = np.hypot(x, y) < 0.62
            m = np.where(burnt & (m != 6), 5, m)
            m = np.where((z > 0.3) & burnt, 4, m)
        return d, m
    return scene, mats


def _pump(wrecked):
    return render(*pump_scene(wrecked), PUMP, PUMP / PX)


# =========================================================================== the depot latch and the hut lock

def latch_scene(open_=False):
    mats = [CHARCOAL, ORANGE, AMBER_LAMP, SCORCHED, DEAD_LAMP]

    def scene(p):
        plate = sd_box(p, (0, 0, 0.06), (0.5, 0.5, 0.06), 0.03)
        if not open_:
            clamp = sd_box(p, (0, 0, 0.18), (0.34, 0.14, 0.08), 0.03)
            jaws = np.minimum(sd_box(p, (-0.3, 0, 0.2), (0.06, 0.26, 0.08), 0.02),
                              sd_box(p, (0.3, 0, 0.2), (0.06, 0.26, 0.08), 0.02))
            lamp = sd_sphere(p, (0.0, 0.32, 0.16), 0.08)
            return union((plate, 0), (clamp, 1), (jaws, 1), (lamp, 2))
        jaws = np.minimum(sd_box(rot_z(p - np.array((-0.36, 0.02, 0.16)), 0.6), (0, 0, 0), (0.05, 0.24, 0.06), 0.02),
                          sd_box(rot_z(p - np.array((0.38, -0.04, 0.16)), -0.5), (0, 0, 0), (0.05, 0.22, 0.06), 0.02))
        lamp = sd_sphere(p, (0.04, 0.3, 0.12), 0.07)
        d, m = union((plate, 3), (jaws, 1), (lamp, 4))
        m = np.where((m == 3) & (np.hypot(p[:, 0], p[:, 1]) > 0.3), 0, m)
        return d, m
    return scene, mats


def lock_scene(blown=False):
    mats = [CHARCOAL, Material((0.3, 0.3, 0.3), metal=0.6, shininess=60, spec=0.5), DEAD_LAMP, SCORCHED]

    def scene(p):
        plate = sd_box(p, (0, 0, 0.05), (0.46, 0.46, 0.05), 0.02)
        if blown:
            a = np.arctan2(p[:, 1], p[:, 0])
            hole = np.hypot(p[:, 0], p[:, 1]) - (0.2 + 0.05 * np.sin(a * 7))
            plate = np.maximum(plate, -hole)
            return plate, np.where(np.hypot(p[:, 0], p[:, 1]) < 0.34, 3, 0)
        box = sd_box(p, (0, 0, 0.16), (0.26, 0.3, 0.08), 0.03)
        lens = sd_sphere(p, (0.0, 0.16, 0.24), 0.07)
        bolt = sd_capsule(p, (-0.16, -0.12, 0.25), (0.16, -0.12, 0.25), 0.045)
        return union((plate, 0), (box, 1), (lens, 2), (bolt, 1))
    return scene, mats


def _prop(kind, state):
    scene, mats = latch_scene(state) if kind == "latch" else lock_scene(state)
    return render(scene, mats, PROP, PROP / PX * 1.15)


# =========================================================================== the eruption

def rock_scene(seed=3931, radius=0.46):
    """A lumpy basalt rock (belt_units.rock_model's bumps and cuts) with glowing cracks: an emission pattern of thin
    seeded lines."""
    rng = np.random.default_rng(seed)
    bumps = [(v / np.linalg.norm(v), rng.uniform(0.14, 0.3), rng.uniform(0.12, 0.28)) for v in rng.normal(size=(9, 3))]
    cuts = [(v / np.linalg.norm(v), rng.uniform(0.72, 0.86)) for v in rng.normal(size=(4, 3))]
    planes = [v / np.linalg.norm(v) for v in rng.normal(size=(5, 3))]
    offs = rng.uniform(-0.2, 0.2, 5)

    def cracks(p, n):
        q = p / radius
        line = np.min([np.abs(q @ v - o) for v, o in zip(planes, offs)], axis=0)
        return np.clip(1.0 - line / 0.07, 0, 1) ** 1.5

    def speckle(p, n):
        cell = np.floor(p * 14.0)
        h = np.sin(cell @ np.array((12.9898, 78.233, 37.719))) * 43758.5453
        return 0.82 + 0.3 * (h - np.floor(h))
    mats = [Material(BASALT.albedo, shininess=10, spec=0.14, pattern=speckle,
                     emission=(1.5, 0.55, 0.12), emission_pattern=cracks)]

    def scene(p):
        q = p * np.array((1.0, 1.06, 1.25))
        r = np.linalg.norm(q, axis=-1)
        n = q / np.maximum(r, 1e-6)[:, None]
        disp = sum(a * np.exp(-(1 - n @ v) / w) for v, a, w in bumps)
        d = (r - radius * (0.8 + disp)) / 1.6
        for v, k in cuts:
            d = np.maximum(d, p @ v - radius * k)
        return d, np.zeros(len(p), dtype=int)
    return scene, mats


def _rock(k):
    scene, mats = rock_scene()
    ang = TAU * k / 16
    return render(lambda p: scene(rot_axis(p, (0.6, 0.35, 0.72), -ang)), mats, ROCK, ROCK / PX * 1.08, crisp=60)


def rock_glow(w=ROCK_GLOW):
    light = np.zeros((w, w, 3))
    glow(light, w / 2, w / 2, 9.0, EMBER, 0.55)
    glow(light, w / 2, w / 2, 4.0, (255, 170, 80), 0.35)
    return artkit.additive(rgba(np.clip(light, 0, 255), np.ones((w, w))))


def rock_breaks(rock, n=8, w=BREAK, seed=3933):
    """The rock shot apart: chips in its own colours flung out in a ring, a short grey-ochre dust ring that settles
    (vacuum); the glow set the hot core's flash and the embers."""
    src = np.array(rock)
    cols = src[src[..., 3] > 0][:, :3]
    cols = cols[np.argsort(cols.sum(1))]
    rng = np.random.default_rng(seed)
    parts = [(TAU * k / 11 + rng.uniform(-0.2, 0.2), rng.uniform(0.75, 1.1), rng.uniform(1.6, 3.4), rng.uniform())
             for k in range(11)]
    bodies, glows = [], []
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    for f in range(n):
        u = (f + 1) / n
        rr = 5 + 15 * u
        dust = np.exp(-((np.hypot(xx - w / 2, yy - w / 2) - rr) / (2.0 + 2 * u)) ** 2) * 0.5 * (1 - u) ** 1.2
        img = rgba(np.zeros((w, w, 3)) + np.array((118, 104, 78.0)), dust)
        d = ImageDraw.Draw(img)
        light = np.zeros((w, w, 3))
        for a, sp, s, kind in parts:
            dist = (4 + 17 * (1 - (1 - u) ** 2)) * sp
            cx, cy = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
            spin = a + 4 * u * (1 if kind > 0.5 else -1)
            pts = [(cx + np.cos(spin + j * TAU / 4) * s * (1 + 0.3 * (j % 2)), cy + np.sin(spin + j * TAU / 4) * s)
                   for j in range(4)]
            lit = cols[int((len(cols) - 1) * (0.85 if np.cos(a - 3.9) > 0 else 0.35))]
            d.polygon(pts, fill=tuple(int(v) for v in lit) + (255,))
            if kind > 0.45:
                glow(light, cx, cy, 1.6, EMBER, 0.9 * (1 - u) + 0.1)
        glow(light, w / 2, w / 2, 7 * (1 - 0.6 * u), (255, 200, 120), max(0.0, 1.0 - 1.6 * u))
        bodies.append(img)
        glows.append(artkit.additive(rgba(np.clip(light, 0, 255), np.ones((w, w)))))
    return bodies, glows


CRACKS = 7


def crack_paths(seed=3941):
    rng = np.random.default_rng(seed)
    paths = []
    for k in range(CRACKS):
        a = TAU * k / CRACKS + rng.uniform(-0.3, 0.3)
        pts, x, y = [], 0.0, 0.0
        for j in range(9):
            a += rng.uniform(-0.45, 0.45)
            x, y = x + np.cos(a) * 5.2, y + np.sin(a) * 5.2
            pts.append((x, y))
        paths.append(pts)
    return paths


def tell_frames(n=12, w=VENT):
    """The tell over its length: the mouth glowing orange to white and widening, the cracks spreading out from it."""
    paths = crack_paths()
    out = []
    for f in range(n):
        p = (f + 0.5) / n
        light = np.zeros((w, w, 3))
        c = w / 2
        glow(light, c, c, 7 + 11 * p, FIRE, 0.4 + 0.5 * p)
        glow(light, c, c, 3 + 5 * p, FIRE_CORE, 0.25 + 0.85 * p)
        reach = 3 + 6 * p                         # how many crack segments glow
        for pts in paths:
            for j, (x, y) in enumerate(pts):
                k = np.clip(reach - j, 0, 1) * (1 - j / 12)
                if k <= 0:
                    break
                px, py = (pts[j - 1] if j else (0.0, 0.0))
                for s in np.linspace(0, 1, 5):
                    glow(light, c + px + (x - px) * s, c + py + (y - py) * s, 1.1, (255, 120, 36), 0.7 * k * (0.5 + p))
        light *= 1 + 0.12 * np.sin(TAU * p * 3) * p      # a throb toward the end
        out.append(artkit.additive(rgba(np.clip(light, 0, 255), np.ones((w, w)))))
    return out


def burst_frames(n=8, w=VENT, seed=3943):
    """The throw: the mouth's white flare, a spray of sparks fanning out, the mouth cooling to a dull glow."""
    rng = np.random.default_rng(seed)
    sparks = [(rng.uniform(0, TAU), rng.uniform(14, 40), rng.uniform(0.5, 1.0)) for _ in range(22)]
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        light = np.zeros((w, w, 3))
        c = w / 2
        glow(light, c, c, 16 + 10 * u, FIRE, 1.1 * (1 - u) ** 1.2)
        glow(light, c, c, 7, FIRE_CORE, 1.3 * max(0.0, 1 - 1.5 * u))
        glow(light, c, c, 9, (200, 70, 20), 0.35 * u)
        for a, sp, k in sparks:
            dist = sp * (1 - (1 - u) ** 2)
            glow(light, c + np.cos(a) * dist, c + np.sin(a) * dist, 1.3, (255, 190, 90), k * (1 - u))
        out.append(artkit.additive(rgba(np.clip(light, 0, 255), np.ones((w, w)))))
    return out


UMB_SECONDS, UMB_OPEN, UMB_FROM, UMB_TO, UMB_PEAK = 4.0, 1.0, 30.0, 170.0, 0.5


def umbrella_size(s):
    """EruptionLooks.umbrellaSize: opening fast, then settling a little wider (diameter, px)."""
    o = min(max(s / UMB_OPEN, 0), 1)
    eased = 1 - (1 - o) * (1 - o)
    settle = max(0.0, s - UMB_OPEN) / (UMB_SECONDS - UMB_OPEN)
    return UMB_FROM + (UMB_TO - UMB_FROM) * eased * (1 + 0.15 * min(1.0, settle))


def umbrella_alpha(s):
    if s < 0 or s >= UMB_SECONDS:
        return 0.0
    rise = min(max(s / 0.2, 0), 1)
    fade = 1 - max(0.0, s - UMB_OPEN) / (UMB_SECONDS - UMB_OPEN)
    return UMB_PEAK * rise * fade * fade


def umbrella_frames(n=12, w=UMB, seed=3945):
    """The umbrella from above (EruptionLooks' size and strength curves): a thin canopy of sulphur haze with a denser
    rim where it falls back, faint radial filaments, settling; smooth alpha."""
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    r, a = np.hypot(x, y), np.arctan2(y, x)
    fil = raster.fbm(w, w, 16, seed, octaves=3, period=False)
    out = []
    for f in range(n):
        s = (f + 0.5) * UMB_SECONDS / n
        rad = umbrella_size(s) / 2
        k = umbrella_alpha(s) / UMB_PEAK
        u = r / max(rad, 1)
        canopy = smoothstep(1.05, 0.8, u) * (0.45 + 0.25 * u)
        rim = np.exp(-((u - 0.88) / 0.12) ** 2) * 0.55
        rays = 0.94 + 0.06 * np.sin(a * 9 + fil * 7)
        alpha = np.clip((canopy + rim) * rays * (0.8 + 0.4 * fil), 0, 1) * k * UMB_PEAK / 0.75
        alpha = blur(alpha, 1.0)
        rgb = SULPHUR_HAZE * (0.9 + 0.12 * u[..., None].clip(0, 1))
        out.append(rgba(rgb, np.clip(alpha, 0, 1)))
    return out


# =========================================================================== jobs

def _job(job):
    kind = job[0]
    if kind == "tower":
        return _tower(*job[1:])
    if kind == "pump":
        return _pump(job[1])
    if kind == "prop":
        return _prop(job[1], job[2])
    if kind == "rock":
        return _rock(job[1])
    raise ValueError(kind)


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


def build_tower():
    jobs = [("tower", "whole", TOWER), ("tower", "stump", TOWER)]
    jobs += [("tower", "top", CAP_W, cap_place(f)[2]) for f in range(1, FALL_N)]
    res = run(jobs)
    whole, stump, tops = res[0], res[1], res[2:]
    body = with_shadow(whole, tower_shadow(TOWER, COL_Z1 + 0.2, TOWER / 2, TOWER / 2))
    save_set("cracking-tower", artkit.quantize_set([body], 40), single=True)
    save_set("cracking-tower-flare", flare_frames())
    bodies, glows, wreck = fall_frames(whole, stump, tops)
    bodies = artkit.quantize_set(bodies + [wreck], 48)
    save_set("cracking-tower-fall", bodies[:-1])
    save_set("cracking-tower-fall-glow", glows)
    save_set("cracking-tower-wreck", bodies[-1:], single=True)


def build_pump():
    res = run([("pump", False), ("pump", True)])
    res = artkit.quantize_set(res, 28)
    save_set("pump-station", res[:1], single=True)
    save_set("pump-station-wreck", res[1:], single=True)


def build_props():
    res = run([("prop", "latch", False), ("prop", "latch", True), ("prop", "lock", False), ("prop", "lock", True)])
    save_set("depot-latch", artkit.quantize_set(res[:2], 20))
    save_set("survey-hut-lock", artkit.quantize_set(res[2:], 16))


def build_eruption():
    rocks = artkit.quantize_set(run([("rock", k) for k in range(16)]), 32)
    save_set("eruption-rock", rocks)
    save_set("eruption-rock-glow", [rock_glow()], single=True)
    bodies, glows = rock_breaks(rocks[0])
    save_set("eruption-rock-break", bodies)
    save_set("eruption-rock-break-glow", glows)
    save_set("eruption-vent-tell", tell_frames())
    save_set("eruption-vent-burst", burst_frames())
    save_set("eruption-umbrella", umbrella_frames())


BUILDERS = {"tower": build_tower, "pump": build_pump, "props": build_props, "eruption": build_eruption}


# =========================================================================== review

def load(name):
    return l23kit.load(name)


def plate(w, h, x0=300, t=None):
    """Level 39's ground for the review: the Loki yard's tile (tools/art/backdrop_l39.py) shifted so its rack is off
    the plate, when built; else a flat sulphur plate."""
    f = BACKDROP / "loki-yard.png"
    if f.exists():
        tile = np.roll(np.array(Image.open(f).convert("RGBA")), -(x0 % 480), axis=1)   # the rack (x 240) off the plate
        reps = (-(-h // tile.shape[0]), -(-w // tile.shape[1]), 1)
        return Image.fromarray(np.tile(tile, reps)[:h, :w], "RGBA")
    return Image.new("RGBA", (w, h), (150, 136, 92, 255))


def sheet_rows(rows, title, width=1300):
    return artkit.review_sheet(title, rows, width=width, batch=BATCH)


def review_tower():
    tower, flare = load("cracking-tower")[0], load("cracking-tower-flare")
    fall, fall_glow, wreck = load("cracking-tower-fall"), load("cracking-tower-fall-glow"), load("cracking-tower-wreck")[0]
    pump, pump_w = load("pump-station")[0], load("pump-station-wreck")[0]
    latch, lock = load("depot-latch"), load("survey-hut-lock")
    lit = []
    for k in range(len(flare)):
        c = plate(TOWER, TOWER)
        c = l23kit.add_c(l23kit.paste_c(c, tower, TOWER / 2, TOWER / 2), flare[k], TOWER / 2, TOWER / 2)
        lit.append(c)
    falls = []
    for k in range(len(fall)):
        c = plate(FALL, FALL)
        c = l23kit.add_c(l23kit.paste_c(c, fall[k], FALL / 2, FALL / 2), fall_glow[k], FALL / 2, FALL / 2)
        falls.append(c)
    rows = [("CRACKING TOWER BURNING (BODY + FLARE, ADDITIVE) ON LEVEL 39'S GROUND", lit, 2, False),
            ("THE FALL: FLASH, THE TOP TUMBLING OFF, BURNING CHUNKS IN A RING, THE FIRES DYING (BODY + GLOW)", falls[:7],
             1, False),
            ("THE FALL, FRAMES 7-11 (THE TOP LANDS AT FRAME 8)", falls[7:], 1, False),
            ("THE TOWER, THE FLARE (ON BLACK), THE WRECK (STAYS)", [tower, wreck], 3, False),
            ("FLARE FRAMES (ADDITIVE)", flare, 3, True),
            ("PUMP STATION, ITS WRECK; DEPOT LATCH (INTACT, OPEN); SURVEY HUT LOCK (DARK, BLOWN)",
             [pump, pump_w] + latch + lock, 4, False)]
    sheet = sheet_rows(rows, "LEVEL 39 REFINERY UNITS (FINAL R77): CRACKING TOWER, PUMP STATION, LATCH, LOCK")
    gif = []
    for i in range(48):
        c = plate(320, 200)
        t = i / 12
        for x, y in ((100, 96), (230, 110)):
            if t < 1.5 or x == 230:
                c = l23kit.add_c(l23kit.paste_c(c, tower, x, y), flare[i % len(flare)], x, y)
            else:
                k = min(len(fall) - 1, int((t - 1.5) * 12))
                if t - 1.5 < 1.0:
                    c = l23kit.add_c(l23kit.paste_c(c, fall[k], x, y), fall_glow[k], x, y)
                else:
                    c = l23kit.paste_c(c, wreck, x, y)
        c = l23kit.paste_c(c, pump if t < 2.5 else pump_w, 300, 40)
        gif.append(c.resize((640, 400), Image.NEAREST))
    artkit.save_review(sheet, gif, L39_CONCEPT, "refinery", fps=12)


def fan(rng_seed, n):
    rng = np.random.default_rng(rng_seed)
    return [np.radians(90 + rng.uniform(-50, 50)) for _ in range(n)]


def eruption_frame(t, vent=(160, 270), angles=None, shot=None):
    """A 320x400 plate at time t of one eruption at medium: tell 1.2 s, rise 0.5 s, flight 2.5 s at 120 -> 60 px/s
    in a fan up the screen, fall 0.4 s; ``shot`` (rock index, time) breaks one rock."""
    tell, burst = load("eruption-vent-tell"), load("eruption-vent-burst")
    umb, rocks, rglow = load("eruption-umbrella"), load("eruption-rock"), load("eruption-rock-glow")[0]
    brk, brk_glow = load("eruption-rock-break"), load("eruption-rock-break-glow")
    c = plate(320, 400)
    vx, vy = vent
    T, R, F, D = 1.2, 0.5, 2.5, 0.4
    if 0 <= t < T:
        c = l23kit.add_c(c, tell[min(11, int(t / T * 12))], vx, vy)
    s = t - T - R                                   # seconds since the throw (the umbrella opens then)
    if t >= T and s < UMB_SECONDS:
        if s >= 0:
            c = l23kit.paste_c(c, umb[min(11, int(s / UMB_SECONDS * 12))], vx, vy)
        b = t - T
        if b < 0.5:
            c = l23kit.add_c(c, burst[min(7, int(b / 0.5 * 8))], vx, vy)
    for i, ang in enumerate(angles or []):
        u = t - T
        if u < 0 or u > R + F + D:
            continue
        if shot and shot[0] == i and t >= shot[1]:
            k = int((t - shot[1]) * 16)
            if k < 8:
                fu = shot[1] - T - R
                x, y = rock_xy(vx, vy, ang, fu)
                c = l23kit.add_c(l23kit.paste_c(c, brk[k], x, y), brk_glow[k], x, y)
            continue
        if u < R:
            sc, x, y = 0.45 + 0.55 * u / R, vx, vy - 4 * u / R
        elif u < R + F:
            sc = 1.0
            x, y = rock_xy(vx, vy, ang, u - R)
        else:
            sc = 1 - 0.55 * (u - R - F) / D
            x, y = rock_xy(vx, vy, ang, F)
        fr = rocks[int(t * 12 + i * 3) % 16]
        if sc < 0.999:
            sz = max(4, int(round(ROCK * sc)))
            fr = fr.resize((sz, sz), Image.LANCZOS)
            g = rglow.resize((max(4, int(ROCK_GLOW * sc)),) * 2, Image.LANCZOS)
        else:
            g = rglow
        c = l23kit.add_c(l23kit.paste_c(c, fr, x, y), g, x, y)
    return c


def rock_xy(vx, vy, ang, s):
    """Distance flown at 120 easing to 60 px/s over 2.5 s, up the screen (y down in the image)."""
    F = 2.5
    dist = 120 * s - 60 * s * s / (2 * F)
    return vx + np.cos(ang) * dist, vy - 4 - np.sin(ang) * dist


def review_eruption():
    tell, burst, umb = load("eruption-vent-tell"), load("eruption-vent-burst"), load("eruption-umbrella")
    rocks, rglow = load("eruption-rock"), load("eruption-rock-glow")
    brk, brk_glow = load("eruption-rock-break"), load("eruption-rock-break-glow")
    on = lambda fr, w: [l23kit.add_c(plate(w, w), f, w / 2, w / 2) for f in fr]  # noqa: E731
    cold = BACKDROP / "vent-a.png"
    cold_img = [Image.open(cold).convert("RGBA")] if cold.exists() else []
    umb_on = [l23kit.paste_c(plate(UMB, UMB), f, UMB / 2, UMB / 2) for f in umb]
    rows = [("THE VENT COLD (LEVEL 39'S BACKDROP PIECE) AND ITS TELL OVER 1.2 S (ADDITIVE, ON THE PLAINS)",
             cold_img + on(tell, VENT), 1, False),
            ("THE BURST AT THE THROW (ADDITIVE, 16 FPS)", on(burst, VENT), 1, False),
            ("THE UMBRELLA, OPENING AND SETTLING OVER 4 S (SMOOTH ALPHA, PEAK 0.5)", umb_on[::2], 1, False),
            ("BURNING ROCK, ITS TUMBLE (16 FR), ITS EMBER HALO", rocks + [rglow[0]], 3, False),
            ("A ROCK SHOT APART (BODY + GLOW)", [l23kit.add_c(l23kit.paste_c(plate(BREAK, BREAK), b, 24, 24), g,
                                                               24, 24) for b, g in zip(brk, brk_glow)], 3, False)]
    sheet = sheet_rows(rows, "IO'S ERUPTIONS (FINAL R77): VENT TELL, BURST, UMBRELLA, BURNING ROCKS")
    angles = fan(3951, 4)
    gif = [eruption_frame(i / 20, angles=angles, shot=(1, 2.55)) for i in range(int(5.4 * 20))]
    artkit.save_review(sheet, gif, JOV_CONCEPT, "eruption", fps=20)


REVIEWS = {"tower": review_tower, "pump": review_tower, "props": review_tower, "eruption": review_eruption}

SETS = {"tower": [("cracking-tower", 1, (TOWER, TOWER)), ("cracking-tower-flare", 8, (FLARE, FLARE)),
                  ("cracking-tower-fall", FALL_N, (FALL, FALL)), ("cracking-tower-fall-glow", FALL_N, (FALL, FALL)),
                  ("cracking-tower-wreck", 1, (TOWER, TOWER))],
        "pump": [("pump-station", 1, (PUMP, PUMP)), ("pump-station-wreck", 1, (PUMP, PUMP))],
        "props": [("depot-latch", 2, (PROP, PROP)), ("survey-hut-lock", 2, (PROP, PROP))],
        "eruption": [("eruption-vent-tell", 12, (VENT, VENT)), ("eruption-vent-burst", 8, (VENT, VENT)),
                     ("eruption-umbrella", 12, (UMB, UMB)), ("eruption-rock", 16, (ROCK, ROCK)),
                     ("eruption-rock-glow", 1, (ROCK_GLOW, ROCK_GLOW)), ("eruption-rock-break", 8, (BREAK, BREAK)),
                     ("eruption-rock-break-glow", 8, (BREAK, BREAK))]}
SMOOTH = {"cracking-tower", "cracking-tower-flare", "cracking-tower-fall", "cracking-tower-fall-glow",
          "eruption-vent-tell", "eruption-vent-burst", "eruption-umbrella", "eruption-rock-glow",
          "eruption-rock-break-glow"}


def alpha_steps(frames):
    vals = set()
    for f in frames:
        a = np.array(f)[..., 3]
        vals |= set(np.unique(a[(a > 0) & (a < 255)]).tolist())
    return len(vals)


def check():
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
                v, c = checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            smooth = ""
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src_ok and share < 0.05
            if name in SMOOTH and frames:
                additive = all((np.array(fr)[..., 3] % 255 == 0).all() for fr in frames)
                if additive:
                    lv = len({tuple(c) for fr in frames for c in np.array(fr)[..., :3].reshape(-1, 3)})
                    smooth = f", {lv} additive colours"
                    ok = ok and lv >= 32
                else:
                    st = alpha_steps(frames)
                    smooth = f", {st} alpha steps"
                    ok = ok and st >= 32
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %{smooth}")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    for review in dict.fromkeys(REVIEWS[s] for s in subjects):
        review()
