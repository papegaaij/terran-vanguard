#!/usr/bin/env python3
"""Production art: Level 23's props and the returning units' under-water looks (design/campaign/act-4-deep-water/
level-23-through-the-ice, "Ground targets", "Secrets and pickups"; design/enemies/naval, "Returning units under
water"; M8 part C batch, concept round 52, step ART-C; straight to production, no concept of their own: designed to
the READMEs, the round-48 Thera Deep mood still (lamp-lit water, warm grow-lamp light) and readability rule 7 (the
light rim) for the secret).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse):
  survey-probe_0..2         48x40, the secret: Conamara's survey probe snagged on a wet ledge at the left wall's foot
                            (a 20 HP `ground` trigger; the data's `sprite: survey-probe`): 0 snagged (a yellow
                            instrument torpedo with a white band, its tether hooked on a rock spur to the left (the
                            ledge itself is the backdrop's),
                            the sensor mast up), 1 hit (scorched, the fairing dented, the cable frayed), 2 freed (the
                            shell split open and empty: the hidden crate pops out as the pickup); rule 7's light rim
  survey-probe-glow_0..7    48x40, additive, aligned: the probe's amber marker strobe on the mast (0-3 a slow blink
                            until the first hit, 4-7 a hurried flicker after it), as the other trigger looks' glows
  thera-tender_0..5         48x48, the Thera Deep tender (the drop's look: a pressure-hull supply drone in the city's
                            orange and white, the CDF supply drone's role) that releases the armour patch: index heading
                            x 3 + jet frame, heading 0 swimming up the screen, 1 down; its two pump-jets' rotors turning
                            (3 frames, 12 fps); the patch rides in the open cradle on its back
                            (assets/pivots/thera-tender.json ``cradle`` per heading)
  thera-tender-lights_0..3  64x64, additive, same centre: heading x 2 + (0 the amber beacon's flash with the warm
                            headlights, 1 the headlights alone); the beams point the way it swims
  reef-spitter-floor        56x56, normal: the Reef Spitter's `floor` look under water (the naval README: on the ledges
                            and racks, no raft): a knot of rock gripped by the spitter's violet-brown root plate, five
                            roots clamped over the edges, a few kelp fronds; the gun (reef-spitter_<heading>) sits on its
                            centre (28, 27); claimed by the unit's root
  driftjelly-deep_0..3      40x40, normal: the Driftjelly on the play plane under water (no foam collar, no `sub`
                            pass): the whole body at the four pulse frames, its bell's rim and the tentacles translucent
                            (stepped, 2/3), a cool rim light so it reads in the lamp-lit water; driftjelly-pulse_* added
                            over it as before
  design/campaign/act-4-deep-water/level-23-through-the-ice/concept/l23-props-final-r52-a.png/.gif   the review sheet
                            and GIF (on the farm floor under the review grade: the probe shot free, the tender swimming
                            up with the patch and back, a Reef Spitter on its floor base turning and firing, jellies)

Run: python3 tools/art/l23_props.py [probe] [tender] [floor] [jelly] [--review]   (~1 min; no subject = all;
--review rebuilds only the review from the written files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import driftjelly as dj  # noqa: E402  (tools/art: the Driftjelly's body renders and pulse glow)
import l23kit as kit
from artkit import DESIGN, TAU

import ground_targets as gt  # noqa: E402  (concept script, unchanged: the light rim)
from render import raster  # noqa: E402
from render.sdf import (Material, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract,  # noqa: E402
                        union)

SCRIPT = "l23_props.py"
BATCH = "M8 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r52"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-23-through-the-ice"
CONCEPT = LEVEL / "concept"
PROBE = (48, 40)
TENDER, TENDER_LIGHTS = 48, 64
FLOOR = 56
FLOOR_GUN = (28, 27)
AMBER = np.array((255, 170, 50), float)
WARM = np.array((255, 226, 170), float)


def render(scene, mats, size, z_top=None):
    hi, f = artkit.render_hi(scene, mats, size, float(size[0]), z_top=float(z_top or max(size)), steps=150)
    return artkit.native(hi, f, crisp=60)


def rimmed(img):
    return Image.fromarray(np.clip(gt.light_rim(np.array(img).astype(np.float64)), 0, 255).astype(np.uint8), "RGBA")


# =========================================================================== the survey probe

PROBE_AXIS = (np.cos(-0.35), np.sin(-0.35))          # the probe lies nose to the upper right (model y up)


def probe_mats(state):
    band = lambda p, n: np.where(np.abs((p[:, 0] * PROBE_AXIS[0] + p[:, 1] * PROBE_AXIS[1]) - 2.0) < 2.2, 1.35, 1.0)  # noqa: E731
    scorch = (lambda p, n: np.where(np.hypot(p[:, 0] - 4, p[:, 1] - 2) < 7, 0.45, 1.0) * band(p, n)) if state else band
    return [Material((0.86, 0.66, 0.12), metal=0.25, shininess=70, spec=0.8, pattern=scorch),   # 0 hull (yellow)
            Material((0.5, 0.52, 0.55), metal=0.6, shininess=60, spec=0.7),                     # 1 steel fins, mast
            Material((0.08, 0.12, 0.16), metal=0.6, shininess=120, spec=1.2),                   # 2 the nose's glass
            Material((0.1, 0.1, 0.1), metal=0.2, shininess=20, spec=0.2),                       # 3 the tether cable
            Material((0.46, 0.5, 0.52), metal=0.1, shininess=10, spec=0.15),                    # 4 the ledge's ice-rock spur
            Material((0.75, 0.62, 0.22), metal=0.3, shininess=40, spec=0.4)]                    # 5 the open shell's inside


def probe_scene(state):
    ax, ay = PROBE_AXIS
    nx, ny = -ay, ax

    def at(u, v, z):
        return (ax * u + nx * v + 2.0, ay * u + ny * v - 1.0, z)

    def sc(p):
        items = [(sd_ellipsoid(p, (-21.0, 1.0, 1.0), (3.2, 3.6, 2.4)), 4)]             # the rock spur the hook caught
        if state < 2:
            items.append((sd_capsule(p, at(-13, 0, 6), at(11, 0, 6), 5.2), 0))
            items.append((sd_sphere(p, at(12, 0, 6), 4.6), 2))
            for k in (-1, 1):
                items.append((sd_box(p, at(-13, k * 5.5, 6), (2.0, 2.5, 0.6), 0.3), 1))
            items.append((sd_capsule(p, at(-2, 0, 9), at(-2, 0, 15), 0.7), 1))       # the sensor mast
            items.append((sd_capsule(p, at(-14, 0, 5), (-20.0, -4.0, 3.0), 0.9), 3))  # the tether to the hook
            items.append((sd_capsule(p, (-20.0, -4.0, 3.0), (-23.0, 3.0, 2.0), 0.9), 3))
            d, m = union(*items, k=0.6)
            if state == 1:
                d, m = subtract((d, m), sd_sphere(p, at(3, 4, 10), 3.2))
            return d, m
        # freed: the shell split along its length, the halves fallen apart, empty inside
        for side, off in ((-1, -3.0), (1, 3.5)):
            shell = sd_capsule(p, at(-12, off, 4), at(10, off + side * 1.5, 4), 4.6)
            cut = (p[:, 0] - ax * 0) * nx + (p[:, 1]) * ny - (off * 1.0 - 1.0 * side) * 1.0
            items.append((np.maximum(shell, -side * cut), 0))
            items.append((sd_capsule(p, at(-11, off, 4.6), at(9, off + side * 1.5, 4.6), 3.6), 5))
        items.append((sd_sphere(p, at(15, 6, 3), 3.4), 2))
        items.append((sd_capsule(p, (-20.0, -4.0, 3.0), (-23.0, 3.0, 2.0), 0.9), 3))
        d, m = union(*items, k=0.4)
        return d, m
    return sc


def probe_frame(state):
    return rimmed(render(probe_scene(state), probe_mats(state), PROBE, z_top=30))


def mast_tip():
    ax, ay = PROBE_AXIS
    x, y = ax * -2 + 2.0, ay * -2 - 1.0
    return PROBE[0] / 2 + x, PROBE[1] / 2 - y - 6          # the mast leans toward the camera's top


def probe_glow(i):
    w, h = PROBE
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    mx, my = mast_tip()
    on = [1.0, 0.5, 0.0, 0.0][i] if i < 4 else [1.0, 0.0, 0.8, 0.0][i - 4]
    r = np.hypot(xx - mx, yy - my)
    f = (np.exp(-(r / 1.3) ** 2) + np.exp(-(r / 5.0) ** 2) * 0.5) * on
    q = artkit.ordered_dither(np.clip(f, 0, 1), 7)
    rgb = AMBER[None, None] * q[..., None] + 80 * np.clip(f - 0.8, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the Thera Deep tender

def tender_mats(phase):
    rotor = lambda p, n: np.where(np.cos(np.arctan2(p[:, 1] + 11, p[:, 0] - np.sign(p[:, 0]) * 9) * 4 + phase * 2.1) > 0.3, 1.3, 0.55)  # noqa: E731
    return [Material((0.92, 0.42, 0.08), metal=0.2, shininess=60, spec=0.7),                # 0 orange hull
            Material((0.86, 0.87, 0.88), metal=0.15, shininess=40, spec=0.5),               # 1 white top, cradle
            Material((0.32, 0.34, 0.36), metal=0.6, shininess=60, spec=0.6, pattern=rotor),  # 2 pump-jets
            Material((0.06, 0.1, 0.14), metal=0.6, shininess=120, spec=1.2),                # 3 viewport
            Material((0.1, 0.1, 0.1), emission=(1.3, 0.7, 0.18)),                          # 4 beacon
            Material((0.1, 0.1, 0.1), emission=(1.3, 1.2, 0.95))]                          # 5 lamps


def tender_scene(heading):
    flip = 1.0 if heading == 0 else -1.0

    def sc(p):
        q = p.copy()
        q[:, 0] *= flip
        q[:, 1] *= flip
        items = [(sd_capsule(q, (0, -12, 6), (0, 11, 6), 7.5, 6.5), 0),
                 (sd_ellipsoid(q, (0, 1, 10.5), (5.0, 11.0, 2.5)), 1),
                 (sd_sphere(q, (0, 15.5, 6.5), 4.6), 3)]
        for s in (-1, 1):
            items.append((sd_cylinder_z(q, (s * 10.5, -11, 6), 3.6, 2.6), 2))
            items.append((sd_capsule(q, (s * 6.5, -9, 6), (s * 9.0, -11, 6), 1.4), 1))
            items.append((sd_sphere(q, (s * 4.2, 14.5, 7.5), 1.3), 5))
        for s in (-1, 1):                                            # the cradle's rails
            items.append((sd_box(q, (s * 4.8, -2.0, 13.4), (0.6, 6.5, 0.8), 0.3), 1))
        items.append((sd_cylinder_z(q, (0, 8.5, 13.0), 1.3, 0.9), 4))
        return union(*items, k=0.5)
    return sc


def tender_frame(job):
    heading, phase = job
    return render(tender_scene(heading), tender_mats(phase), (TENDER, TENDER), z_top=40)


def tender_lights(heading, flash):
    w = TENDER_LIGHTS
    c = w / 2
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    sign = -1 if heading == 0 else 1
    v = np.zeros((w, w))
    for s in (-1, 1):
        hx, hy = c + s * 4.2, c + sign * 14.5
        ahead = (yy - hy) * sign
        spread = np.abs(xx - hx) - ahead * 0.32
        cone = np.clip(ahead / 10, 0, 1) * np.clip(1 - ahead / 17, 0, 1) * np.clip(1 - spread / 4.5, 0, 1)
        v += cone * 0.4 + np.exp(-((xx - hx) ** 2 + (yy - hy) ** 2) / 2.5) * 0.9
    col = WARM * np.clip(v, 0, 1)[..., None]
    if flash:
        bx, by = c, c - 8.5 * (1 if heading == 0 else -1)
        b = np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / 3.0) + np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / 40) * 0.5
        col += AMBER * np.clip(b, 0, 1)[..., None]
    q = artkit.ordered_dither(np.clip(col.max(axis=-1) / 255, 0, 1), 9)
    col = col / np.maximum(col.max(axis=-1, keepdims=True), 1) * q[..., None] * 255
    return artkit.additive(Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the Reef Spitter's floor base

def floor_mats():
    return [Material((0.42, 0.46, 0.4), metal=0.05, shininess=12, spec=0.2),      # 0 rock (silted, mossy)
            Material((0.3, 0.16, 0.26), metal=0.15, shininess=45, spec=0.6),      # 1 root plate (violet-brown)
            Material((0.12, 0.3, 0.2), metal=0.0, shininess=20, spec=0.3),        # 2 kelp
            Material((0.1, 0.06, 0.1), emission=(0.45, 0.25, 0.9))]               # 3 violet sap knots


def floor_scene():
    rng = np.random.default_rng(2331)
    rocks = [((rng.uniform(-10, 10), rng.uniform(-10, 10), 2.0), (rng.uniform(8, 13), rng.uniform(8, 13), 5.0))
             for _ in range(4)]
    roots = [(k * TAU / 5 + 0.3, rng.uniform(19, 24)) for k in range(5)]
    kelp = [(rng.uniform(0, TAU), rng.uniform(14, 22)) for _ in range(6)]

    def sc(p):
        items = [(sd_ellipsoid(p, c, r), 0) for c, r in rocks]
        items.append((sd_cylinder_z(p, (0, 1, 7.5), 9.5, 1.6), 1))
        for a, L in roots:
            c, s = np.cos(a), np.sin(a)
            items.append((sd_capsule(p, (6 * c, 6 * s, 7.0), (L * c, L * s, 2.5), 2.0, 1.1), 1))
            items.append((sd_sphere(p, ((L - 6) * c, (L - 6) * s, 5.0), 1.5), 3))
        for a, r in kelp:
            c, s = np.cos(a), np.sin(a)
            items.append((sd_capsule(p, (r * c, r * s, 3.0), ((r + 6) * c + 3 * s, (r + 6) * s - 3 * c, 4.0), 1.3, 0.6), 2))
        return union(*items, k=1.2)
    return sc


def floor_frame():
    return render(floor_scene(), floor_mats(), (FLOOR, FLOOR), z_top=30)


# =========================================================================== the Driftjelly under water

def jelly_frame(i):
    body = np.array(dj.render_body(dj.PULSES[i])).astype(np.float64)
    solid = body[..., 3] > 0
    h, w = solid.shape
    inner = solid.copy()
    for _ in range(2):                                   # two pixels in from the silhouette
        nxt = inner.copy()
        nxt[1:, :] &= inner[:-1, :]
        nxt[:-1, :] &= inner[1:, :]
        nxt[:, 1:] &= inner[:, :-1]
        nxt[:, :-1] &= inner[:, 1:]
        inner = nxt
    edge = solid & ~inner
    lum = body[..., :3].mean(axis=-1)
    thin = solid & (lum < 70)                            # the dark tentacle tips
    body[..., :3] = body[..., :3] + 4
    yy, xx = np.mgrid[0:h, 0:w]
    rim = edge & ((xx + yy) < (w + h) * 0.55)
    body[..., :3] = np.where(rim[..., None], body[..., :3] * 0.4 + np.array((190, 236, 240)) * 0.6, body[..., :3])
    body[..., 3] = np.where(solid, np.where(edge | thin, 170, 255), 0)
    out = np.clip(body, 0, 255).astype(np.uint8)
    out[~solid] = 0
    return Image.fromarray(out, "RGBA")


# =========================================================================== build

def _job(job):
    kind, *a = job
    if kind == "probe":
        return probe_frame(a[0])
    if kind == "tender":
        return tender_frame(a)
    if kind == "floor":
        return floor_frame()
    if kind == "jelly":
        return jelly_frame(a[0])
    raise ValueError(kind)


def build(parts):
    jobs = []
    if "probe" in parts:
        jobs += [("probe", s) for s in range(3)]
    if "tender" in parts:
        jobs += [("tender", h, k) for h in range(2) for k in range(3)]
    if "floor" in parts:
        jobs += [("floor",)]
    if "jelly" in parts:
        jobs += [("jelly", i) for i in range(4)]
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_job, jobs))
    i = 0
    if "probe" in parts:
        kit.write("survey-probe", artkit.quantize_set(out[i:i + 3], 36), SOURCE)
        kit.write("survey-probe-glow", [probe_glow(k) for k in range(8)], SOURCE)
        i += 3
    if "tender" in parts:
        kit.write("thera-tender", artkit.quantize_set(out[i:i + 6], 36), SOURCE)
        kit.write("thera-tender-lights", [tender_lights(h, f) for h in range(2) for f in (True, False)], SOURCE)
        artkit.write_pivots("thera-tender", SOURCE, {
            "size": [TENDER, TENDER],
            "cradle": [[24, 26], [24, 22]],
            "headings": ["up the screen", "down the screen"],
            "note": "per heading the cradle's centre (px from the frame's top left), where the armour patch rides",
        })
        i += 6
    if "floor" in parts:
        kit.write("reef-spitter-floor", artkit.quantize_set(out[i:i + 1], 32), SOURCE, single=True)
        i += 1
    if "jelly" in parts:
        kit.write("driftjelly-deep", artkit.quantize_set(out[i:i + 4], 32), SOURCE)


# =========================================================================== review

def review():
    probe, pglow = kit.load("survey-probe"), kit.load("survey-probe-glow")
    tender, tlights = kit.load("thera-tender"), kit.load("thera-tender-lights")
    floor, jelly = kit.load("reef-spitter-floor"), kit.load("driftjelly-deep")
    jpulse = artkit.load_frames("driftjelly-pulse")
    gun = artkit.load_frames("reef-spitter")
    patch = artkit.load_frames("pickup-armour-patch")
    crate = artkit.load_frames("pickup-crate")
    try:
        burst = kit.load("explosion-deep-small")
    except FileNotFoundError:
        burst = artkit.load_frames("explosion-small")
    rows = [("SURVEY PROBE: SNAGGED / HIT / FREED", probe, 3, False), ("PROBE STROBE (ADDITIVE)", pglow, 2, True),
            ("THERA TENDER: UP X3, DOWN X3 JET FRAMES", tender, 2, False),
            ("TENDER LIGHTS (ADDITIVE)", tlights, 1, True),
            ("REEF SPITTER FLOOR BASE (WITH THE GUN, REST)", [floor[0], _with_gun(floor[0], gun[0])], 2, False),
            ("DRIFTJELLY UNDER WATER: 4 PULSE FRAMES", jelly, 3, False)]
    sheet = artkit.review_sheet("LEVEL 23 PROPS - FINAL (PROBE, TENDER, SPITTER FLOOR, JELLY)", rows, width=1000,
                                batch=BATCH)
    frames = []
    hit_t, free_t = 1.2, 2.0
    for fi in range(90):
        t = fi / 15
        img = kit.plate(480, 540, 0, 2000)
        glows = []
        # the probe at the left wall's foot
        px, py = 96, 150
        st = 0 if t < hit_t else 1 if t < free_t else 2
        kit.paste_c(img, probe[st], px, py)
        if st < 2:
            glows.append((pglow[(int(t * 4) % 4) if st == 0 else 4 + int(t * 12) % 4], (px, py)))
        if hit_t <= t < hit_t + 0.4 or free_t <= t < free_t + 0.4:
            glows.append((burst[min(len(burst) - 1, int((t % 0.4) * 30))], (px + 4, py - 2)))
        if t >= free_t:
            cy = py - min(30, (t - free_t) * 60)
            kit.paste_c(img, crate[int(t * 8) % len(crate)], px + 10, cy)
        # the tender: up from the bottom, the patch released at t = 3, then back down
        if t < 3.0:
            ty, hd = 560 - t * 60, 0
        else:
            ty, hd = 380 + (t - 3.0) * 70, 1
        tx = 300
        kit.paste_c(img, tender[hd * 3 + int(t * 12) % 3], tx, ty)
        glows.append((tlights[hd * 2 + (0 if int(t * 3) % 2 == 0 else 1)], (tx, ty)))
        if t < 3.0:
            kit.paste_c(img, patch[0], tx, ty + (2 if hd == 0 else -2))
        else:
            kit.paste_c(img, patch[int(t * 8) % len(patch)], tx - 20, 380)
        # a Reef Spitter on its floor base, turning toward the ship
        sx, sy = 380, 160
        kit.paste_c(img, floor[0], sx, sy)
        k = int(round((np.arctan2(-(240 - sx), 488 - sy)) / TAU * 32 + t * 2)) % 32
        kit.paste_c(img, gun[k], sx, sy - 1)
        # two jellies
        for jx, jy, ph in ((170, 330, 0), (250, 230, 2)):
            j = (int(t * 6) + ph) % 4
            kit.paste_c(img, jelly[j], jx, jy + 4 * np.sin(t + ph))
            glows.append((jpulse[j], (jx, jy + 4 * np.sin(t + ph))))
        kit.paste_c(img, kit.ship(2), 240, 488)
        for g, p in glows:
            img = kit.add_c(img, g, *p)
        frames.append(img.convert("RGB"))
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2 ON THE FARM FLOOR (REVIEW GRADE): T=0.5, 2.3, 3.4, 5.5", raster.LABEL)
    for i, fi in enumerate((8, 35, 51, 82)):
        big.alpha_composite(frames[fi].convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (16 + i * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l23-props", fps=15)


def _with_gun(base, gun):
    out = base.copy()
    out.alpha_composite(gun, (FLOOR_GUN[0] - gun.width // 2, FLOOR_GUN[1] - gun.height // 2))
    return out


def main(argv):
    parts = [a for a in argv if a in ("probe", "tender", "floor", "jelly")] or ["probe", "tender", "floor", "jelly"]
    if "--review" not in argv:
        build(parts)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
