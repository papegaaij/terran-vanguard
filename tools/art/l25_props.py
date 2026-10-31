#!/usr/bin/env python3
"""Production art: Level 25's props (design/campaign/act-4-deep-water/level-25-siren-trench, "Ground targets",
"Secrets and pickups"; M8 part E batch, concept round 54, step ART-E; straight to production, no concept of their own:
designed to the README, the round-48 trench mood still and Conamara's look of Levels 22-23 (the survey probe's yellow
instrument body with a white band), readability rule 7 (the light rim) for the secret).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse: a
ground target's ``sprite`` or a drop's ``look`` once Level 25's data names it):
  survey-sub_0..2         56x40, the secret: Conamara's small survey sub nose-down in the left cave's mouth (a 20 HP
                          `ground` trigger inside the notch; the data's `sprite: survey-sub`, `size: [56, 40]`): its bow
                          pointing up-left into the cave and down into the silt, the stern raised toward the camera; 0
                          lying (the yellow hull gone dull, the white band, the dark viewport, the sail, the cargo pod on
                          its back shut, a torn propeller shroud, silt over the bow), 1 hit (scorched, the pod's lid
                          dented), 2 opened (the pod's lid blown off beside it, the hold dark and empty: the hidden crate
                          rises from it as the pickup); rule 7's light rim
  survey-sub-glow_0..7    56x40, additive, aligned: 0-3 its lamp is dead: only a faint red emergency pinger on the sail
                          (one short blip a cycle, until the first hit), 4-7 sparks flickering at the dent after it, as
                          the other trigger looks' glows
  survey-buoy_0..2        40x40, the armour patch's carrier (the `drops:` look `survey-buoy`): a moored Conamara survey
                          buoy floating on the play plane (a yellow float with a white band, a short mast with a strobe
                          and a radar reflector, the mooring cable going down out of sight); 0 shut, 1 the side hatch
                          swinging open (the patch rises out), 2 open and empty
  survey-buoy-glow_0..3   40x40, additive, aligned: the strobe's blink (a flash, an afterglow, two dark frames; 4 fps),
                          drawn ungraded
  design/campaign/act-4-deep-water/level-25-siren-trench/concept/l25-props-final-r54-a.png/.gif   the review sheet and
                          GIF (in the trench under the review grade: the sub in its cave shot open and its crate rising,
                          the buoy blinking at the narrows' end and releasing the patch)

Run: python3 tools/art/l25_props.py [sub] [buoy] [--review]   (~1 min; no subject = all; --review rebuilds only the
review; run backdrop_l25.py first: the review stands on its trench)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import l24kit as kit
import l24_props as p24
from artkit import DESIGN

from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "l25_props.py"
BATCH = "M8 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r54"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-25-siren-trench"
CONCEPT = LEVEL / "concept"
SUB = (56, 40)
BUOY = 40
RED = np.array((255, 60, 40), float)
STROBE = np.array((255, 236, 170), float)
SUB_AXIS = (-np.cos(0.55), np.sin(0.55))     # the bow points up-left (model y up)
render, rimmed = p24.render, p24.rimmed


# =========================================================================== the survey sub

def sub_mats(state):
    scorch = (lambda p, n: np.where(np.hypot(p[:, 0] - 2, p[:, 1] - 2) < 7, 0.38, 1.0)) if state else None
    return [Material((0.82, 0.66, 0.12), metal=0.25, shininess=40, spec=0.5, pattern=scorch),   # 0 Conamara yellow
            Material((0.46, 0.47, 0.46), metal=0.6, shininess=50, spec=0.6),                    # 1 steel: shroud, fittings
            Material((0.05, 0.08, 0.1), metal=0.6, shininess=120, spec=1.2),                    # 2 the viewport
            Material((0.32, 0.38, 0.36), metal=0.0, shininess=8, spec=0.1),                     # 3 silt
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),                     # 4 the dark hold
            Material((0.6, 0.6, 0.56), metal=0.3, shininess=40, spec=0.5),                      # 5 the pod's lid
            Material((0.88, 0.88, 0.86), metal=0.15, shininess=40, spec=0.5)]                   # 6 the white band


def sub_scene(state):
    ax, ay = SUB_AXIS
    nx, ny = -ay, ax

    def at(u, v, z):                                   # u along the hull (bow +), v across, z up; the bow sinks
        return (ax * u + nx * v + 2, ay * u + ny * v - 1, z - 0.22 * u)

    def sc(p):
        hull = sd_capsule(p, at(-15, 0, 7.5), at(15, 0, 7.5), 6.5, 5.8)
        items = []
        lid = sd_box(p, at(-3, 0, 14.0), (5.5, 3.0, 1.0), 0.8)
        if state == 2:
            hull = subtract((hull, 0), sd_ellipsoid(p, at(-3, 0, 14.5), (5.0, 3.0, 3.6)))[0]
            items.append((sd_ellipsoid(p, at(-3, 0, 11.5), (4.4, 2.6, 2.6)), 4))
            items.append((sd_box(p, at(-6, 11, 3.0), (5.0, 3.0, 0.8), 0.8), 5))     # the lid, blown off
        elif state == 1:
            items.append((subtract((lid, 0), sd_sphere(p, at(-2, 1, 16.0), 2.6))[0], 5))
        else:
            items.append((lid, 5))
        items.append((hull, 0))
        items.append((sd_capsule(p, at(5, 0, 7.5), at(7, 0, 7.5), 6.6, 6.4), 6))       # the white band
        items.append((sd_sphere(p, at(16, 0, 8.0), 4.2), 2))                           # the viewport bow
        items.append((sd_box(p, at(7, 0, 13.5), (3.0, 1.4, 3.0), 0.8), 6))             # the sail
        items.append((sd_cylinder_z(p, at(-19, 0, 7.0), 4.2, 2.6), 1))                 # the shroud, torn
        items.append((sd_box(p, at(-19, 5.5, 6.0), (1.2, 2.0, 0.6), 0.3), 1))
        items.append((sd_ellipsoid(p, at(15, 0, 1.5), (9.0, 9.5, 4.2)), 3))              # silt over the bow
        items.append((sd_ellipsoid(p, at(4, -7, 0.5), (12.0, 4.0, 2.4)), 3))
        return union(*items, k=0.7)
    return sc


def sub_frame(state):
    return rimmed(render(sub_scene(state), sub_mats(state), SUB, z_top=36))


def sub_points():
    ax, ay = SUB_AXIS
    nx, ny = -ay, ax

    def px(u, v):
        x, y = ax * u + nx * v + 2, ay * u + ny * v - 1
        return SUB[0] / 2 + x, SUB[1] / 2 - y
    return {"pinger": px(7, 0), "dent": px(-2, 1), "hold": px(-3, 0)}


def sub_glow(i):
    w, h = SUB
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    pts = sub_points()
    if i < 4:                                   # the dead lamp: a faint red emergency pinger, one blip a cycle
        on = [0.7, 0.2, 0.0, 0.0][i]
        mx, my = pts["pinger"]
        r = np.hypot(xx - mx, yy - my - 3)
        f = (np.exp(-(r / 1.0) ** 2) + np.exp(-(r / 3.5) ** 2) * 0.35) * on
        rgb = RED[None, None] * artkit.smooth_steps(f)[..., None]
    else:                                       # sparks at the dent after the hit
        rng = np.random.default_rng(2630 + i)
        mx, my = pts["dent"]
        f = np.zeros((h, w))
        for _ in range(int(rng.integers(2, 5))):
            sx, sy = mx + rng.normal(0, 2.2), my + rng.normal(0, 1.8)
            f += np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / 0.9) * rng.uniform(0.6, 1.0)
        f += np.exp(-((xx - mx) ** 2 + (yy - my) ** 2) / 16) * (0.35 if i % 2 == 0 else 0.15)
        rgb = np.array((200, 230, 255.0))[None, None] * artkit.smooth_steps(f)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the survey buoy

def buoy_mats():
    return [Material((0.86, 0.68, 0.12), metal=0.2, shininess=50, spec=0.6),     # 0 the yellow float
            Material((0.88, 0.88, 0.86), metal=0.15, shininess=40, spec=0.5),    # 1 the white band
            Material((0.4, 0.42, 0.44), metal=0.6, shininess=50, spec=0.6),      # 2 steel: mast, reflector
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),      # 3 the empty hatch
            Material((0.6, 0.6, 0.56), metal=0.3, shininess=40, spec=0.5),       # 4 the hatch door
            Material((0.9, 0.86, 0.7), metal=0.2, shininess=120, spec=1.2)]      # 5 the strobe's lens


def buoy_scene(state):
    def sc(p):
        items = [(sd_sphere(p, (0, 0, 6.0), 9.0), 0),
                 (sd_cylinder_z(p, (0, 0, 6.0), 9.3, 1.6), 1),
                 (sd_cylinder_z(p, (2.5, 2.5, 15.0), 1.1, 6.0), 2),                 # the mast (leaning to the light)
                 (sd_sphere(p, (3.0, 3.0, 21.5), 1.8), 5),                           # the strobe on top
                 (sd_box(p, (-2.5, 3.5, 15.5), (2.6, 0.5, 2.6), 0.3), 2)]            # the radar reflector
        hatch = sd_box(p, (5.5, -5.0, 9.0), (3.2, 2.6, 1.6), 0.5)
        if state == 0:
            items.append((hatch, 4))
        else:
            items[0] = (subtract((items[0][0], 0), sd_ellipsoid(p, (5.0, -4.6, 9.0), (3.4, 3.0, 2.6)))[0], 0)
            items.append((sd_ellipsoid(p, (4.6, -4.2, 7.8), (2.6, 2.2, 1.4)), 3))
            swing = 4.0 if state == 1 else 7.5
            items.append((sd_box(p, (5.5 + swing * 0.6, -5.0 - swing, 6.0), (3.2, 2.6, 0.6), 0.4), 4))
        return union(*items, k=0.5)
    return sc


def buoy_frame(state):
    return rimmed(render(buoy_scene(state), buoy_mats(), (BUOY, BUOY), z_top=40))


def buoy_glow(i):
    w = BUOY
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    mx, my = w / 2 + 3.0, w / 2 - 3.0 - 2.0                     # the strobe (model (3, 3) at the top, y up)
    on = [1.0, 0.35, 0.0, 0.0][i]
    r = np.hypot(xx - mx, yy - my)
    f = (np.exp(-(r / 1.3) ** 2) + np.exp(-(r / 6.0) ** 2) * 0.5) * on
    rgb = STROBE[None, None] * artkit.smooth_steps(f)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


# =========================================================================== build

def _job(job):
    kind, a = job
    return sub_frame(a) if kind == "sub" else buoy_frame(a)


def build(parts):
    jobs = ([("sub", s) for s in range(3)] if "sub" in parts else []) + \
        ([("buoy", s) for s in range(3)] if "buoy" in parts else [])
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(_job, jobs))
    i = 0
    if "sub" in parts:
        kit.write("survey-sub", artkit.quantize_set(frames[0:3], 40), SOURCE)
        kit.write("survey-sub-glow", [sub_glow(k) for k in range(8)], SOURCE)
        i = 3
    if "buoy" in parts:
        kit.write("survey-buoy", artkit.quantize_set(frames[i:i + 3], 32), SOURCE)
        kit.write("survey-buoy-glow", [buoy_glow(k) for k in range(4)], SOURCE)


# =========================================================================== review

FPS = 12


def review():
    import backdrop_l25 as b25                              # noqa: E402  (tools/art: the trench)
    sub, sglow = kit.load("survey-sub"), kit.load("survey-sub-glow")
    buoy, bglow = kit.load("survey-buoy"), kit.load("survey-buoy-glow")
    crate = artkit.load_frames("pickup-crate")
    patch = artkit.load_frames("pickup-armour-patch")
    try:
        burst = kit.load("explosion-deep-small")
    except FileNotFoundError:
        burst = artkit.load_frames("explosion-small")
    rows = [("SURVEY SUB: LYING, HIT, OPENED", sub, 3, False),
            ("SURVEY SUB GLOW (ADDITIVE): PINGER X 4, SPARKS X 4", sglow, 2, True),
            ("SURVEY BUOY: SHUT, OPENING, OPEN", buoy, 3, False),
            ("SURVEY BUOY STROBE (ADDITIVE)", bglow, 3, True)]
    sheet = artkit.review_sheet("LEVEL 25 PROPS - FINAL", rows, width=1000, batch=BATCH)
    pieces = b25.placed_pieces()
    sub_t, sub_x = next(((t, x) for k, t, x in b25.TARGETS if k == "survey-sub"), (78, 78))
    frames = []
    # part 1: the sub's cave (6 s from t=sub_t+1.2), shot at +2.0 and +3.2, opened, the crate rising
    for fi in range(int(6 * FPS)):
        t = fi / FPS
        lt = sub_t + 1.2 + t
        img = b25.composite(lt, pieces, ship=False).copy()
        y = b25.scroll(lt) + 540 - b25.target_centre(sub_t, SUB[1])
        state = 0 if t < 2.0 else (1 if t < 3.2 else 2)
        kit.paste_c(img, kit.grade(sub[state], 4), sub_x, y)
        g = sglow[int(t * 4) % 4] if state == 0 else (sglow[4 + int(t * 12) % 4] if state == 1 else None)
        if g is not None:
            img = kit.add_c(img, g, sub_x, y)
        for th in (2.0, 3.2):
            s = t - th
            if 0 <= s < 0.5 and int(s * 20) < len(burst):
                img = kit.add_c(img, burst[int(s * 20)], sub_x, y)
        if t >= 3.2 and crate:
            pts = sub_points()
            hx, hy = pts["hold"]
            rise = min(1.0, (t - 3.2) / 0.8)
            kit.paste_c(img, crate[int(t * 8) % len(crate)], sub_x - SUB[0] / 2 + hx + 30 * rise,
                        y - SUB[1] / 2 + hy - 10 * rise)
        img.alpha_composite(kit.ship(2), (240 - 24 - int(40 * min(1, t / 3)), 444 - 24))
        raster.draw_text(img, 8, 8, "SURVEY SUB - L25 T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    # part 2: the buoy at the narrows' end (moored on the play plane, scrolling with the floor), opening at +2.4
    for fi in range(int(5 * FPS)):
        t = fi / FPS
        lt = 116.0 + t
        img = b25.composite(lt, pieces, ship=False).copy()
        by = 380 - (b25.scroll(lt) - b25.scroll(118.0))           # (240, 380 y-up) at t=118 -> screen y 160
        by = 540 - by
        state = 0 if t < 2.4 else (1 if t < 2.6 else 2)
        kit.paste_c(img, kit.grade(buoy[state], 4), 240, by)
        img = kit.add_c(img, bglow[int(t * 4) % 4], 240, by)
        if t >= 2.4 and patch:
            u = min(1.0, (t - 2.4) / 0.6)
            kit.paste_c(img, patch[int(t * 8) % len(patch)], 240 + 18 * u, by + 14 * u)
        img.alpha_composite(kit.ship(2), (240 - 24, 444 - 24))
        raster.draw_text(img, 8, 8, "SURVEY BUOY - L25 T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (1.0, 4.4)] + [frames[int((6 + 3.0) * FPS)]]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE SUB IN ITS CAVE, OPENED AND ITS CRATE RISING, THE BUOY "
                                            "RELEASING THE PATCH; 2X CROP; REVIEW GRADE 0.14", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    y0 = int(b25.scroll(sub_t + 2.2) + 540 - b25.target_centre(sub_t, SUB[1]))
    crop = frames[int(1.0 * FPS)].crop((0, max(0, y0 - 100), 180, max(0, y0 - 100) + 202))
    big.alpha_composite(crop.resize((240, 270), Image.Resampling.NEAREST).convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l25-props", fps=FPS)


def main(argv):
    parts = [a for a in argv if a in ("sub", "buoy")] or ["sub", "buoy"]
    if "--review" not in argv:
        build(parts)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
