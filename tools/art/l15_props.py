#!/usr/bin/env python3
"""Production art: Level 15's props (design/campaign/act-3-red-dust/level-15-olympus-descent, "Ground targets"
and "Secrets and pickups"; M7 part B batch, concept round 38, step ART-B; straight to production, no concept:
designed to the README, the Olympus still's Mars light and readability rule 7 for loot and secret targets).

Lycus Survey Station's supply containers are Level 01's cargo container (tools/art/loot_targets.py's model,
damage and break-apart, imported unchanged) in the colonies' dusty off-white with the matte amber hazard ends;
the secret is a colonial supply drop pod half buried under basalt rubble on the escarpment's ledge, which four
hits (hard 6) knock away (TriggerBreak.hitByHit: a frame per hit taken, then cleared and opened).

Outputs (the `sprite` names of Level 15's ground targets):
  lycus-container_0..2         32x24 intact, damaged (scorch, a hole, a cracked roof: loot_targets' damage),
                               wrecked (burnt out, the roof gone: the frame the game leaves as the wreck)
  lycus-container-break_0..7   48x48 the damaged container in six pieces flying apart and crumbling
                               (loot_targets.break_frame with the colony materials)
  drop-pod-rubble_0..5         48x40 the rubble over the crashed pod: 0 the pile (the pod's nose and its
                               beacon showing), 1-3 one boulder knocked off per hit (scorched), 4 cleared (the
                               pod uncovered, the rubble scattered, dust), 5 opened (the hatch sprung, the amber
                               crate inside); medium/easy show a frame per hit, hard (6 hits) falls back to
                               intact / hit / open; rule 7's light rim on every frame
  drop-pod-rubble-glow_0..7    48x40 additive: the pod's beacon (GroundGlow: 0-3 a slow blink until the first
                               hit, 4-7 a hurried flicker after it), as Level 13's hut
  drop-pod                     40x28 the uncovered pod alone, hatch open (for a look of its own; the rubble's
                               frames 4-5 already show it)
  design/campaign/act-3-red-dust/level-15-olympus-descent/concept/l15-props-final-r38-a.png/.gif

Held sprites (tools/art/README.md, Act 3 arsenal): a root no level data claims yet (vanguard.pipeline.SpriteUse:
a ground target's `sprite` claims its name and every name starting with it and a hyphen) is written to
build/art-held/sprites/ instead of assets/sprites/; rerun once the data names it. Level 15's data claims
`lycus-container` and `drop-pod-rubble`; `drop-pod` stays held until a look names it.

Run: python3 tools/art/l15_props.py [--review]   (~1 min; --review rebuilds only the review from the files)
"""
import contextlib
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, sprite

import ground_targets as gt  # noqa: E402  (concept script, imported unchanged: materials, light rim, scorch)
import l13_props as l13  # noqa: E402  (tools/art: the additive beacon disc)
import loot_targets as lt  # noqa: E402  (tools/art: Level 01's container model, damage and break-apart)
from render import sdf  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_x, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "l15_props.py"
BATCH = "M7 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r38"
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-15-olympus-descent"
CONCEPT = LEVEL / "concept"
HELD = ROOT / "build" / "art-held" / "sprites"
CONTAINER = lt.CONTAINER                  # 32 x 24, as Level 01's
RUBBLE = (48, 40)
POD = (40, 28)
COLOURS = 40

# --------------------------------------------------------------------------- held sprites


def claimed_roots():
    data = yaml.safe_load((LEVEL / "data.yaml").read_text(encoding="utf-8")) if (LEVEL / "data.yaml").exists() else {}
    roots = set()
    for g in (data or {}).get("ground_targets") or []:
        if g.get("sprite"):
            roots.add(g["sprite"])
    for d in (data or {}).get("drops") or []:
        if d.get("sprite"):
            roots.add(d["sprite"])
    return roots


def claimed(name):
    return any(name == r or name.startswith(r + "-") for r in claimed_roots())


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
    with sprites_of(name) as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD):
        single = folder / f"{name}.png"
        if single.exists():
            return [Image.open(single).convert("RGBA")]
        files = sorted(folder.glob(f"{name}_*.png"), key=lambda p: int(p.stem.rsplit("_", 1)[1]))
        files = [f for f in files if f.stem.rsplit("_", 1)[0] == name]
        if files:
            return [Image.open(f).convert("RGBA") for f in files]
    return []


# =========================================================================== the station's containers

_GT_MATERIALS = gt.materials


def colony_materials(lit=False):
    """gt.materials with the colonies' dusty off-white body (Coalition structures dusty grey-white) for the
    rust one; the matte amber hazard ends, the steel and the lens as Level 01's."""
    mats = _GT_MATERIALS(lit)
    mats[gt.BODY] = replace(mats[gt.BODY], albedo=(0.66, 0.63, 0.57), metal=0.25, shininess=26, spec=0.25)
    return mats


@contextlib.contextmanager
def colony_kit():
    before = gt.materials
    gt.materials = colony_materials
    try:
        yield
    finally:
        gt.materials = before


def container_frame(state):
    with colony_kit():
        if state < 2:
            return lt.container_frame(state == 1)
        return wreck_frame()


def wreck_frame():
    """The burnt-out container: the damaged model with its roof gone, charred inside, scorched all over."""
    base = lt.container_scene(CONTAINER, True)
    mats = lt.scorched(colony_materials(False), lt.CONTAINER_SCORCH + [(0.5, 0.5, 14)], CONTAINER)
    mats = [replace(m, albedo=tuple(np.array(m.albedo) * 0.55)) for m in mats]
    w, h = CONTAINER

    def scene(p):
        d, m = base(p)
        return subtract((d, m), sd_box(p, (0, 0, 5.5), (w / 2 - 2.2, h / 2 - 2.2, 4.0), 0.5))
    return lt.render(scene, mats, CONTAINER)


def container_break(f):
    with colony_kit():
        return lt.break_frame(f)


# =========================================================================== the drop pod and its rubble

POD_MATS = 7
HULL, BAND, DARK, ROCK, SOIL, CRATE, LENS = range(POD_MATS)


def pod_materials(lit=True):
    lens = (Material((0.3, 0.12, 0.02), emission=(1.6, 0.8, 0.2)) if lit
            else Material((0.32, 0.18, 0.08), shininess=80, spec=0.8))
    return [Material((0.70, 0.67, 0.60), metal=0.3, shininess=34, spec=0.35, pattern=sdf.panel_lines(0.33, 0.05, 0.8)),
            Material((0.98, 0.66, 0.14), metal=0.0, shininess=8, spec=0.05, pattern=gt.hazard_stripes),
            Material((0.15, 0.15, 0.17), metal=0.4, shininess=30, spec=0.3),
            Material((0.34, 0.25, 0.21), metal=0.0, shininess=14, spec=0.15),
            Material((0.50, 0.30, 0.19), metal=0.0, shininess=6, spec=0.03),
            Material((0.95, 0.64, 0.16), metal=0.2, shininess=40, spec=0.6),
            lens]


# the pod lies along x, nose (beacon end) to the upper left, half buried; model px, y up
POD_TILT = 0.38
POD_LEN, POD_R = 15.0, 6.5
BEACON_AT = np.array([-12.5, 6.0, 7.0])   # in the scene, after the pod's turn
# the boulders over it, knocked off in this order by the hits (x, y, rx, ry, rz)
BOULDERS = [(4.0, 3.0, 8.0, 6.5, 6.5), (-3.0, -4.0, 7.5, 6.0, 6.0), (10.0, -5.0, 6.5, 5.5, 5.5),
            (-8.0, 1.0, 6.0, 5.0, 5.0)]
KEEP = [(-17.0, -10.0, 4.5, 3.5, 3.5), (17.0, 9.0, 5.0, 4.0, 4.0), (19.0, -11.0, 3.5, 3.0, 3.0)]   # stay
SCATTER = [(-20.0, 12.0, 5.0, 4.0, 3.5), (21.0, -2.0, 6.0, 4.5, 4.0), (-15.0, -15.0, 5.0, 4.0, 3.5),
           (12.0, -16.0, 4.5, 3.5, 3.0)]  # where the knocked-off boulders lie after the clearing


def pod_local(p):
    return sdf.rotate_z(p, POD_TILT)


def pod_parts(p, opened, buried):
    """The pod (capsule along x), its hazard bands, the hatch (open: swung up), the crate inside, its beacon."""
    q = pod_local(p)
    z0 = 1.0 if buried else 4.5
    body = sd_capsule(q, (-POD_LEN / 2, 0, z0), (POD_LEN / 2, 0, z0), POD_R)
    bands = np.minimum(sd_box(q, (-5.0, 0, z0), (1.2, POD_R + 0.4, POD_R + 0.4)),
                       sd_box(q, (6.0, 0, z0), (1.2, POD_R + 0.4, POD_R + 0.4)))
    parts = [(body, HULL), (np.maximum(bands, body - 0.45), BAND)]
    fin = sd_box(q, (POD_LEN / 2 + 3.5, 0, z0), (1.0, 4.5, 4.5), 0.4)
    parts.append((fin, DARK))
    lens = sd_sphere(q, (-POD_LEN / 2 - 4.2, 0, z0 + 1.0), 2.3)
    parts.append((lens, LENS))
    d, m = union(*parts, k=0.3)
    if opened:
        well = sd_box(q, (0.5, 0, z0 + POD_R), (5.5, 3.6, 3.0), 0.5)
        d, m = subtract((d, m), well)
        hatch = sd_box(sdf.rotate_x(q - np.array([0.5, 4.2, z0 + POD_R]), -1.2), (0, 0, 2.6), (5.5, 0.4, 2.6), 0.2)
        crate = sd_box(q, (0.5, 0, z0 + 2.4), (4.2, 2.8, 1.8), 0.4)
        d, m = union((d, m), (hatch, HULL), (crate, CRATE))
    return d, m


def rock(p, x, y, rx, ry, rz, seed):
    """An angular basalt boulder: a rounded box turned about z and tipped a little, its faces roughened."""
    rng = np.random.default_rng(seed)
    q = sdf.rotate_x(sdf.rotate_z(p - np.array([x, y, rz * 0.6 + 0.5]), rng.uniform(0, np.pi)), rng.uniform(-0.3, 0.3))
    d = sd_box(q, (0, 0, 0), (rx * 0.8, ry * 0.8, rz * 0.8), min(rx, ry, rz) * 0.35)
    return d + 0.3 * np.sin(q[:, 0] * 2.1 + q[:, 1] * 1.3) * np.sin(q[:, 2] * 1.9 + 0.7)


def rubble_scene(state):
    """state 0..3: the pile with `state` boulders knocked off; 4 cleared; 5 opened."""
    on = BOULDERS[state:] if state < 4 else []
    off = SCATTER[:min(state, 4)]

    def scene(p):
        items = [pod_parts(p, state >= 5, buried=state < 4)]
        for k, (x, y, rx, ry, rz) in enumerate(on + KEEP + off):
            items.append((rock(p, x, y, rx, ry, rz, 1801 + hash((x, y)) % 997), ROCK))
        if state < 4:                         # dirt clods packed between the boulders
            for k, (x, y) in enumerate(((0, -1), (7, -2), (-5, 4), (2, 6), (-11, -3))):
                items.append((sd_ellipsoid(p, (x, y, 1.5), (4.5, 3.5, 2.6)), SOIL))
        return union(*items, k=0.5)
    return scene


def rubble_frame(state):
    mats = pod_materials(True)
    hits = min(state, 4)
    if 0 < state <= 4:            # where the hits landed: scorch on the soil and the rocks left
        spots = [(0.42 + 0.06 * k, 0.45 + 0.04 * ((k % 2) * 2 - 1), 4.5) for k in range(hits)]
        mats = lt.scorched(mats, spots, RUBBLE)
    hi, factor = artkit.render_hi(rubble_scene(state), mats, RUBBLE, float(RUBBLE[0]), z_top=30.0, steps=170)
    arr = np.array(artkit.native(hi, factor, crisp=60)).astype(np.float64)
    return Image.fromarray(gt.light_rim(arr).astype(np.uint8), "RGBA")


def beacon_px():
    """The pod's lens on the sprite, px from its top left."""
    c, s_ = np.cos(-POD_TILT), np.sin(-POD_TILT)
    x, y = -POD_LEN / 2 - 4.2, 0.0
    sx, sy = x * c + y * s_, -x * s_ + y * c
    return RUBBLE[0] / 2 + sx, RUBBLE[1] / 2 - sy


def rubble_glow(k):
    level = (1.0, 0.5, 0.0, 0.0, 1.0, 0.0, 0.6, 0.0)[k]
    bx, by = beacon_px()
    return l13.glow_disc(RUBBLE, [(bx, by, 7.0, level, l13.BEACON_RGB)])


def pod_alone():
    def scene(p):
        sand = sd_ellipsoid(p, (0, 0, -2.0), (17.0, 11.0, 3.0))
        return union((sand, SOIL), pod_parts(p, True, buried=False), k=0.5)
    hi, factor = artkit.render_hi(scene, pod_materials(False), POD, float(POD[0]), z_top=30.0, steps=160)
    arr = np.array(artkit.native(hi, factor, crisp=60)).astype(np.float64)
    return Image.fromarray(gt.light_rim(arr).astype(np.uint8), "RGBA")


# =========================================================================== build

def _job(job):
    kind, i = job
    if kind == "container":
        return container_frame(i)
    if kind == "break":
        return container_break(i)
    if kind == "rubble":
        return rubble_frame(i)
    if kind == "glow":
        return rubble_glow(i)
    return pod_alone()


def build():
    jobs = ([("container", i) for i in range(3)] + [("break", i) for i in range(lt.BREAK_FRAMES)]
            + [("rubble", i) for i in range(6)] + [("glow", i) for i in range(8)] + [("pod", 0)])
    with ProcessPoolExecutor(max_workers=8) as pool:
        out = list(pool.map(_job, jobs))
    n = lt.BREAK_FRAMES
    sets = {"lycus-container": lt.finish(out[0:3]),
            "lycus-container-break": lt.finish(out[3:3 + n], rim=False),
            "drop-pod-rubble": artkit.quantize_set(out[3 + n:9 + n], COLOURS),
            "drop-pod-rubble-glow": artkit.quantize_set(out[9 + n:17 + n], 12),
            "drop-pod": artkit.quantize_set(out[17 + n:18 + n], 32)}
    for name, frames in sets.items():
        write(name, frames, single=name == "drop-pod")


# =========================================================================== review

def plate():
    """The rubble's spot: the escarpment's regolith with the ledge under it (the backdrop's files)."""
    base = ROOT / "assets" / "backdrop" / "level-15"
    tile = Image.open(base / "escarpment.png").convert("RGBA").crop((150, 300, 390, 480))
    ledge = Image.open(base / "drop-pod-ledge.png").convert("RGBA")
    tile.alpha_composite(ledge, (120 - ledge.width // 2, 90 - ledge.height // 2 + 6))
    return tile


def add_glow(img, glow, x, y):
    return artkit.add_light(img, glow, (x - glow.width // 2, y - glow.height // 2))


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    cont, brk = load("lycus-container"), load("lycus-container-break")
    rub, glow, pod = load("drop-pod-rubble"), load("drop-pod-rubble-glow"), load("drop-pod")
    base = plate()
    scenes = []
    for k in (0, 2, 4, 5):
        img = base.copy()
        sprite.paste_center(img, sprite.shadow_of(rub[k], opacity=0.4, blur=1.0), 123, 94)
        sprite.paste_center(img, rub[k], 120, 90)
        if k < 4:
            img = add_glow(img, glow[0 if k == 0 else 4], 120, 90)
        scenes.append(img)
    station = Image.open(ROOT / "assets" / "backdrop" / "level-15" / "escarpment.png").convert("RGBA").crop((40, 100, 280, 280))
    for k, x in enumerate((50, 110, 170)):
        sprite.paste_center(station, sprite.shadow_of(cont[min(k, 2)], opacity=0.4, blur=1.0), x + 2, 92)
        sprite.paste_center(station, cont[min(k, 2)], x, 90)
    rows = [("LYCUS-CONTAINER 0-2: INTACT, DAMAGED, WRECKED (LEVEL 01'S MODEL IN THE COLONY OFF-WHITE)", cont, 4, False),
            ("LYCUS-CONTAINER-BREAK 0-7", brk, 3, False),
            ("DROP-POD-RUBBLE 0-5: THE PILE, 1-3 HITS, CLEARED, OPENED (THE CRATE); DROP-POD (ALONE)", rub + pod, 3, False),
            ("DROP-POD-RUBBLE-GLOW 0-7 (ADDITIVE: 0-3 SLOW BLINK, 4-7 AFTER A HIT)", glow, 3, True),
            ("ON THE LEDGE (FRAMES 0, 2, 4, 5) AND ON THE ESCARPMENT'S REGOLITH (THE CONTAINERS)", scenes + [station], 1, False)]
    sheet = artkit.review_sheet("LEVEL 15 PROPS: LYCUS STATION'S CONTAINERS, THE DROP POD'S RUBBLE (SECRET)", rows,
                                width=1300, batch=BATCH)
    gif = []
    for k in [0, 0, 1, 2, 3, 4, 5, 5, 5]:
        img = base.copy()
        sprite.paste_center(img, sprite.shadow_of(rub[k], opacity=0.4, blur=1.0), 123, 94)
        sprite.paste_center(img, rub[k], 120, 90)
        for j in range(4):
            fr = img if k >= 4 else add_glow(img, glow[(j if k == 0 else 4 + j) % 8], 120, 90)
            gif.append(fr.resize((480, 360), Image.NEAREST))
    for f in range(len(brk)):
        img = station.copy()
        sprite.paste_center(img, brk[f], 110, 90)
        gif += [img.resize((480, 360), Image.NEAREST)] * 2
    artkit.save_review(sheet, gif, CONCEPT, "l15-props", fps=8)


def main(args):
    if "--review" not in args:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
