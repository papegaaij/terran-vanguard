#!/usr/bin/env python3
"""Concept round 36 - Mars (Act 3 "Red Dust"): mood stills per sub-location and the a/b looks
the Act 3 level drafts ask for.

Outputs (design/world/mars/concept/, the unchosen a/b looks of round 36's review (CHOSEN: canyon
walls b, storm radius b, dome a) in concept/rejected/), every PNG a review sheet with the play
field at 1x:
  mars-olympus-r36-a.png         L15: the descent past Olympus Mons through two cloud decks
  mars-valles-r36-a.png          L16: Valles Marineris, the Helix research station on the north
                                 rim (canyon model a, the decided art-direction rule)
  mars-ares-landing-r36-a.png    L17: Ares Landing, the outer domes, rail line, greenhouses, gun
                                 pits, the Helix compound untouched while Vrell growth creeps in
  mars-storm-plains-r36-a.png    L18: the northern plains in the dust storm (radius look a)
  mars-tharsis-foundry-r36-a.png L19: slag fields, roofless conveyor halls carrying grey Warden
                                 Tanks up the screen, furnace row and vents, the smelter tower
  mars-hellas-r36-a.png          L20/21: the Hellas basin: terraces, the dune sea, the
                                 travelling ridge of the Colossus, the lime hollow
  mars-panorama-r36-a.png        a wide still (960x540, low camera): Ares Landing's domes, the
                                 canyon, Olympus Mons on the horizon, a storm front
  canyon-walls-r36-{a,b}.png/.gif  L16 camera model: (a) the rim at scale 1.0 is the collision
                                 line, the faces drop to the floor at 0.6 (art-direction rule);
                                 (b) the floor at 1.0 is the collision line, the walls rise
                                 toward the camera to 1.25 like Level 08's towers
  storm-radius-r36-{a,b}.png/.gif  L18 visibility radius 170 px: (a) a clear bubble with a hard
                                 edge, (b) a soft, ragged falloff with dust tongues; air units
                                 outside as dark silhouettes with their glow, bullets always drawn
  dome-r36-{a,b}.png             L17 Ares Landing domes (120 px, Eden 144) with the ally's
                                 states (intact, hit, broken): (a) geodesic glass, (b) ribbed
                                 pressure dome on a regolith berm

GIFs are seamless 4 s loops at 25 fps with the bullet colours reserved (as scenes_r10.make_gif10).
Units on the stills are the round-36 proposals of their own parts, variant a (enemies_r36.py),
and the chosen round 04 Vrell; they only give scale. Rules: design/art-direction/README.md
(layer model, Perspective between layers, decoration and atmosphere intensity, readability).
Run: python3 tools/concept/mars_r36.py [olympus] [valles] [ares] [storm] [foundry] [hellas]
     [panorama] [canyon] [radius] [dome] [--no-gif]      (no subject = everything)
"""
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r36 as u36  # noqa: E402  (round-36 unit proposals, lazily rendered)
import parallax_r02 as r02  # noqa: E402
import parallax_r03 as r03  # noqa: E402
import scenes_r06 as s6  # noqa: E402
from parallax_r02 import H, W, posterize, scroll_tex  # noqa: E402
from render import raster, sdf, sprite, station  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.palette import B  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

OUT = ROOT / "design" / "world" / "mars" / "concept"
N, FPS, TAU = 80, 20, 2 * np.pi        # scene time base: 80 units per 4 s loop
GIF_N, GIF_FPS = 100, 25
CX, CY = 240.0, 297.0                  # perspective centre (the towers' projection centre)
DOWN, UP = np.pi / 2, -np.pi / 2
SUB = "CONCEPT ROUND 36 - MARS"

# Mars ramps (dark -> light). Rust for the plains and canyon, ochre for Hellas, basalt for the
# volcano and the slag, butterscotch for the sky and the dust.
RUST = [(44, 18, 12), (88, 36, 20), (136, 60, 32), (178, 92, 52), (212, 132, 82), (236, 180, 126)]
OCHRE = [(76, 50, 30), (128, 88, 54), (172, 124, 78), (204, 160, 108), (226, 194, 144),
         (244, 222, 182)]
BASALT = [(26, 18, 18), (52, 34, 30), (82, 54, 44), (116, 76, 58), (150, 104, 78), (190, 146, 108)]
SKY = (214, 166, 112)
CLOUD_D = [(96, 72, 68), (146, 114, 100), (186, 156, 134), (216, 192, 168), (234, 214, 190)]
DUST = [(64, 30, 18), (112, 58, 32), (156, 92, 52), (196, 132, 82), (226, 176, 120)]
CLOUD = [(118, 92, 84), (166, 136, 120), (206, 182, 160), (236, 220, 200), (250, 242, 228)]
LIME, TEAL, GOLD = (168, 255, 42), (0, 255, 154), B["ASCEND. GOLD"]
STRATA = [(196, 120, 72), (150, 78, 44), (176, 104, 62), (118, 58, 34), (164, 96, 56),
          (132, 70, 40), (102, 50, 30)]


# =========================================================================== small helpers

def nfield(w, h, cell, seed, octaves=4, period=False):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=period)


def shade_rgb(hgt, stops, strength=40.0, shadow=0.0, periodic=False):
    """Colour a height field through ``stops`` and light it from the top-left; ``shadow`` > 0
    adds long low-sun cast shadows (drop per px)."""
    if periodic:
        sh = s6.hillshade(hgt, strength)
    else:
        gy, gx = np.gradient(hgt)
        sh = np.clip(1 - (gx + gy) * strength, 0.3, 1.6)
    col = raster.ramp([(i / (len(stops) - 1), c) for i, c in enumerate(stops)],
                      np.clip(hgt, 0, 1)) * sh[..., None]
    if shadow:
        lit = s6.cast_shadows(hgt, steps=24, drop=shadow) if periodic else cast_np(hgt, 24, shadow)
        col *= (0.6 + 0.4 * lit)[..., None]
    return col


def cast_np(hgt, steps, drop):
    """scenes_r06.cast_shadows for a non-tiling still (edges replicated, no wrap)."""
    lit = np.ones_like(hgt)
    pad = np.pad(hgt, ((steps, 0), (steps, 0)), mode="edge")
    h, w = hgt.shape
    for k in range(1, steps + 1):
        src = pad[steps - k:steps - k + h, steps - k:steps - k + w]
        lit = np.minimum(lit, np.clip(1 - (src - hgt - k * drop) * 25, 0, 1))
    return lit


def rgba(col, alpha=None):
    return raster.to_rgba_image(col, alpha)


def tone(img, colors, mode=5):
    out = posterize(img, colors)
    return out.filter(ImageFilter.ModeFilter(mode)) if mode else out


def blank():
    return Image.new("RGBA", (W, H), (0, 0, 0, 0))


def mix_to(img, color, amount):
    return r02.haze(img, color, amount)


def silhouette(sp, dark=(46, 24, 16), alpha=0.82):
    """A sprite seen through dust: a dark shape; only its glow (Vrell teal/lime/violet light,
    white running lights, red sensor eyes) keeps its colour and blooms a little."""
    a = np.array(sp).astype(np.float64)
    r, g, b, al = a[..., 0], a[..., 1], a[..., 2], a[..., 3]
    lum = 0.3 * r + 0.59 * g + 0.11 * b
    mx, mn = a[..., :3].max(-1), a[..., :3].min(-1)
    sat = (mx - mn) / np.maximum(mx, 1)
    glow = (al > 128) & ((lum > 205) | ((sat > 0.6) & (mx > 200)))
    out = np.zeros_like(a)
    out[..., :3] = dark
    out[..., 3] = al * alpha
    out[glow, :3] = a[glow, :3]
    out[glow, 3] = 255
    img = Image.fromarray(out.astype(np.uint8), "RGBA")
    gl = np.zeros_like(a)
    gl[glow] = a[glow]
    gl_img = Image.fromarray(gl.astype(np.uint8), "RGBA")
    pad = Image.new("RGBA", (sp.width + 12, sp.height + 12), (0, 0, 0, 0))
    pad.alpha_composite(img, (6, 6))
    halo = Image.new("RGBA", pad.size, (0, 0, 0, 0))
    halo.alpha_composite(gl_img, (6, 6))
    return raster.glow(pad, 3, 0.0) if not glow.any() else _add_glow(pad, halo)


def _add_glow(img, halo):
    b = np.array(halo.filter(ImageFilter.GaussianBlur(2.5))).astype(np.float64)
    a = np.array(img).astype(np.float64)
    k = b[..., 3:4] / 255 * 1.6
    a[..., :3] = a[..., :3] * (1 - np.clip(k, 0, 1) * 0.3) + b[..., :3] * np.clip(k, 0, 1)
    a[..., 3] = np.maximum(a[..., 3], np.clip(b[..., 3] * 1.6, 0, 255) * 0.85)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


_sil = {}


def sil(sp, lights_heading=None):
    """Cached silhouette; with ``lights_heading`` the Ghost Drone's red sensor eye (nose) and
    white running lights (wingtips) are added, as its spec says they show through the dust."""
    key = (id(sp), None if lights_heading is None else round(lights_heading, 2))
    if key not in _sil:
        img = silhouette(sp)
        if lights_heading is not None:
            img = img.copy()
            c = img.width / 2
            r = sp.width * 0.42
            h = lights_heading
            glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
            dg = ImageDraw.Draw(glow)
            pts = [((c + r * np.cos(h), c + r * np.sin(h)), (255, 40, 40), 1.6)]
            for side in (2.3, -2.3):
                pts.append(((c + r * 0.85 * np.cos(h + side), c + r * 0.85 * np.sin(h + side)),
                            (255, 255, 255), 1.2))
            for (x, y), col, rr in pts:
                dg.ellipse([x - rr, y - rr, x + rr, y + rr], fill=col + (255,))
            img = _add_glow(img, glow)
            img.alpha_composite(glow)
        _sil[key] = (sp, img)
    return _sil[key][1]


def unit(slug, anim=0.0):
    """The chosen round 04 Vrell (role colours)."""
    return CAST.unit(slug, anim)


def ghost(h):
    return u36.ghost["a"].get(h)


def herald(h, beat=0.0):
    return u36.herald["a"].get(h, beat=beat, crest=1.0)


def bolts(img, x, y, n=6, step=60, phase=0.0):
    """The player's twin bolts in front of the ship (screen space, a still or a frame)."""
    for k in range(n):
        yy = y - 26 - ((k * step + phase) % (n * step))
        if yy > -10:
            sprite.paste_center(img, CAST.base.bolt, x - 9, yy)
            sprite.paste_center(img, CAST.base.bolt, x + 9, yy)


def player(img, casters, x, y, bank=0.0, shots=True, phase=0.0):
    frame = "r" if bank > 0.5 else "l" if bank < -0.5 else "c"
    if shots:
        bolts(img, x, y, phase=phase)
    sp = CAST.base.player[frame]
    casters.append((sp, x, y))
    sprite.paste_center(img, sp, x, y)
    sprite.paste_center(img, CAST.base.flare, x, y + 24)


def aimed_orbs(img, src, dst, n=3, gap=34, start=30, r=5, ring=None):
    sx, sy = src
    dx, dy = dst[0] - sx, dst[1] - sy
    d = np.hypot(dx, dy) or 1
    for k in range(n):
        t = start + k * gap
        if t < d - 30:
            e3.orb(img, sx + dx / d * t, sy + dy / d * t, r, ring=ring)


def place(img, shadows, sp, x, y, layer="air", opacity=0.5):
    if shadows is not None and layer in s6.SHADOW:
        dx, dy = s6.SHADOW[layer]
        sprite.paste_center(shadows, sprite.shadow_of(sp, opacity=opacity, blur=1.4,
                                                      scale=0.85 if layer != "ground" else 1.0),
                            x + dx, y + dy)
    sprite.paste_center(img, sp, x, y)


def sdf_sprite(scene, mats, w, h, factor=4, colors=40, z_top=None, crisp=60):
    hi = sdf.render(scene, mats, (w * factor, h * factor), float(w), z_top=z_top or max(w, h),
                    steps=160)
    return sprite.make_sprite(hi, factor, colors, crisp=crisp)


def flat(rgbf):
    return tuple(c / 255 for c in rgbf)


CAST = s6.Cast()


# =========================================================================== structures

def helix_site(w=120, h=96, pad=True):
    """Helix Dynamics site (research station / compound, drawn alike): glossy black blocks
    with gold chines, warm lit skylight strips, a gold-tinted glass atrium, a dish, a
    gold-ringed landing pad, a mast with a red light. The only gloss on the planet."""
    black = Material((0.13, 0.13, 0.15), metal=0.45, shininess=22, spec=1.0,
                     pattern=sdf.panel_lines(0.25, 0.05, 0.6))
    gold = Material(flat(GOLD[3]), metal=0.85, shininess=90, spec=1.0)
    lit = Material((0.1, 0.08, 0.05), emission=(1.6, 1.2, 0.75))
    padm = Material((0.24, 0.24, 0.26), metal=0.2, shininess=30, spec=0.3)
    red = Material((0.1, 0.1, 0.1), emission=(1.7, 0.2, 0.2))
    glass = Material(flat((150, 110, 40)), metal=0.9, shininess=160, spec=1.4,
                     emission=(0.35, 0.25, 0.08))
    BL, GO, LI, PA, RE, GL = range(6)

    def scene(p):
        items = [
            (sd_box(p, (-18, 8, 6), (30, 20, 6), 1.5), BL),          # main block
            (sd_box(p, (-18, 8, 12.2), (30.6, 20.6, 0.8), 0.3), GO),  # gold roof chine
            (sd_box(p, (-18, 8, 13.2), (28.5, 18.5, 0.8), 0.4), BL),
            (sd_sphere(p, (-30, 12, 12), 10), GL),                  # atrium
            (sd_box(p, (22, 22, 4), (12, 10, 4), 1.2), BL),          # annex, two tiers
            (sd_box(p, (22, 22, 8.4), (12.5, 10.5, 0.6), 0.2), GO),
            (sd_box(p, (24, 24, 10), (7, 6, 2.5), 0.8), BL),
            (sd_box(p, (-46, 26, 4), (8, 8, 4), 1.0), BL),           # pump house
            (sd_capsule(p, (-38, 26, 3), (-12, 30, 3), 1.6), GO),    # gold-clad pipe
            (sd_cylinder_z(p, (40, -4, 0), 2.0, 22), BL),            # mast
            (sd_sphere(p, (40, -4, 23), 2.4), RE),
            (np.maximum(sd_sphere(p, (2, 14, 22), 8.0), -(sd_sphere(p, (2, 14, 25), 7.6))), GO),
        ]
        for k in range(6):                                           # lit skylight strips
            items.append((sd_box(p, (-14 + k * 5, 4, 14.2), (0.9, 7, 0.6), 0.2), LI))
        for k in range(3):                                           # annex window slits
            items.append((sd_box(p, (16 + k * 6, 30, 8.6), (1.6, 0.8, 0.5), 0.2), LI))
        if pad:
            items += [(sd_cylinder_z(p, (16, -26, 0), 18, 1.2), PA),
                      (np.maximum(np.abs(np.hypot(p[:, 0] - 16, p[:, 1] + 26) - 15.5) - 1.2,
                                  np.abs(p[:, 2] - 1.4) - 0.4), GO),
                      (sd_box(p, (11, -26, 1.4), (1.2, 6, 0.4)), GO),
                      (sd_box(p, (21, -26, 1.4), (1.2, 6, 0.4)), GO),
                      (sd_box(p, (16, -26, 1.4), (5, 1.2, 0.4)), GO)]
        return union(*items)
    return sdf_sprite(scene, [black, gold, lit, padm, red, glass], w, h, colors=48)


def greenhouse(length=60, radius=9, burnt=False):
    pal = B
    sp = r03.greenhouse_sprite(pal, length=length, radius=radius)
    if burnt:
        a = np.array(sp).astype(np.float64)
        grey = a[..., :3].mean(-1, keepdims=True)
        a[..., :3] = (a[..., :3] * 0.2 + grey * 0.8) * 0.45
        rng = np.random.default_rng(length)
        holes = rng.random(a.shape[:2]) < 0.25
        a[holes & (a[..., 3] > 0), 3] = 0
        sp = Image.fromarray(a.astype(np.uint8), "RGBA")
    return sp


# =========================================================================== domes (L17)

def geodesic_mask(p, c, freq=6.5, width=0.075):
    """Struts of a geodesic dome: three families of great circles round horizontal axes at
    60 degrees, which together read as the triangle lattice of a geodesic shell."""
    q = p - np.array(c)
    n = q / np.maximum(np.linalg.norm(q, axis=-1, keepdims=True), 1e-6)
    m = np.zeros(len(p), bool)
    for a in (0.0, TAU / 6, TAU / 3):
        ax = np.array([np.cos(a), np.sin(a), 0.0])
        v = np.arcsin(np.clip(n @ ax, -1, 1)) * freq / np.pi
        m |= np.abs(v - np.round(v)) < width
    m |= np.abs(n[:, 2]) < 0.06                                     # base ring
    return m


def dome_a(R=46, annex=True):
    """Variant a - geodesic glass: a glass hemisphere on white struts, green-lit crop rows
    inside, on a concrete ring with four airlocks and two greenhouse annexes."""
    glass = Material((0.05, 0.16, 0.1), metal=0.75, shininess=160, spec=1.2,
                     emission=(0.12, 0.85, 0.3),
                     emission_pattern=lambda p, n: (0.35 + 0.65 * (1 - n[:, 2]) ** 0.7)
                     * (0.65 + 0.35 * (np.sin(p[:, 1] * 0.55) > 0)))
    strut = Material((0.86, 0.88, 0.86), metal=0.3, shininess=60, spec=0.6)
    conc = Material((0.62, 0.56, 0.5), metal=0.05, shininess=20, spec=0.15,
                    pattern=lambda p, n: np.where((np.abs(np.arctan2(p[:, 1], p[:, 0]) * 8 / np.pi
                                                          % 1 - 0.5) < 0.04), 0.7, 1.0))
    hull = Material(flat((176, 174, 168)), metal=0.3, shininess=50, spec=0.5,
                    pattern=sdf.panel_lines(0.12, 0.05, 0.8))
    warm = Material((0.1, 0.1, 0.1), emission=(1.6, 1.2, 0.5))
    GL, ST, CO, HU, WA = range(5)
    Rr = R + 9

    def scene(p):
        shell = np.maximum(sd_sphere(p, (0, 0, 2), R), -(p[:, 2] - 2))
        d_ring = sd_cylinder_z(p, (0, 0, 0), Rr, 2.4)
        items = [(d_ring, CO)]
        for k, ang in enumerate((0.45, 2.0, 3.6, 5.2)):
            c = (np.cos(ang) * (R + 4), np.sin(ang) * (R + 4))
            items.append((sd_box(p, (c[0], c[1], 4), (5.5, 5.5, 4), 1.0), HU))
            items.append((sd_sphere(p, (c[0], c[1], 8.6), 1.4), WA))
        if annex:
            for ang, ln in ((2.75, 28), (5.9, 22)):
                a0 = (np.cos(ang) * (R - 2), np.sin(ang) * (R - 2), 2)
                a1 = (np.cos(ang) * (R + ln), np.sin(ang) * (R + ln), 2)
                tube = np.maximum(sd_capsule(p, a0, a1, 7.5), -(p[:, 2] - 2))
                items.append((tube, GL))
        items.append((sd_cylinder_z(p, (0, 0, R + 2), 3.5, 1.5), HU))
        d, m = union(*items)
        on_shell = shell < d
        d2 = np.minimum(d, shell)
        strut_m = geodesic_mask(p, (0, 0, 2))
        m2 = np.where(on_shell, np.where(strut_m, ST, GL), m)
        return d2, m2
    size = int(2 * (Rr + 32) if annex else 2 * Rr + 6)
    return scene, [glass, strut, conc, hull, warm], size


def dome_b(R=48, annex=True):
    """Variant b - ribbed pressure dome: a low white composite shell with twelve ribs, a green
    glazed band and skylight, banked with regolith (radiation and impact shielding)."""
    shell = Material(flat((214, 206, 192)), metal=0.15, shininess=40, spec=0.35)
    rib = Material(flat((150, 146, 140)), metal=0.4, shininess=60, spec=0.5)
    glaze = Material((0.04, 0.14, 0.08), metal=0.7, shininess=150, spec=1.1,
                     emission=(0.12, 0.9, 0.32))
    berm = Material(flat((150, 88, 52)), metal=0.0, shininess=8, spec=0.05,
                    pattern=lambda p, n: 0.85 + 0.15 * np.sin(p[:, 0] * 1.7) * np.sin(p[:, 1] * 1.3))
    hull = Material(flat((176, 174, 168)), metal=0.3, shininess=50, spec=0.5)
    SH, RI, GZ, BE, HU = range(5)
    rz = R * 0.6

    def scene(p):
        q = p - np.array([0, 0, 1.0])
        e = sdf.sd_ellipsoid(q, (0, 0, 0), (R, R, rz))
        e = np.maximum(e, -q[:, 2])
        ang = np.arctan2(q[:, 1], q[:, 0])
        rr = np.hypot(q[:, 0], q[:, 1])
        z = q[:, 2] / rz
        ribm = np.abs((ang * 12 / TAU) % 1 - 0.5) > 0.44
        band = (z > 0.28) & (z < 0.5) & ~ribm
        sky = rr < R * 0.26
        mat = np.where((sky & ~ribm) | band, GZ, np.where(ribm, RI, SH))
        tor = np.hypot(rr - (R + 2), q[:, 2]) - 9.0                 # regolith berm
        items = [(tor, BE)]
        if annex:
            for ang0 in (0.9, 4.0):
                a0 = (np.cos(ang0) * (R - 4), np.sin(ang0) * (R - 4), 4)
                a1 = (np.cos(ang0) * (R + 26), np.sin(ang0) * (R + 26), 4)
                items.append((np.maximum(sd_capsule(p, a0, a1, 5.5), -(p[:, 2] - 1)), HU))
                items.append((sd_box(p, (a1[0], a1[1], 5), (5, 5, 5), 1.2), HU))
        d, m = union(*items)
        on = e < d
        return np.minimum(d, e), np.where(on, mat, m)
    size = int(2 * (R + 40) if annex else 2 * (R + 14))
    return scene, [shell, rib, glaze, berm, hull], size


_domes = {}


def dome_sprite(v, footprint=120, annex=True):
    key = (v, footprint, annex)
    if key not in _domes:
        scale = footprint / 120
        if v == "a":
            scene, mats, size = dome_a(R=46 * scale, annex=annex)
        else:
            scene, mats, size = dome_b(R=48 * scale, annex=annex)
        _domes[key] = sdf_sprite(scene, mats, size, size, factor=4, colors=48,
                                 z_top=120 * scale)
    return _domes[key]


def damaged(sp, v, state, seed=3):
    """The ally's states as overlays on the intact render (concept only; the production art
    renders them): 'hit' = amber cracks round an impact, a few dark panes; 'broken' = the
    shell shattered (a) or breached (b), the interior dark, debris and venting dust."""
    if state == "intact":
        return sp
    rng = np.random.default_rng(seed)
    img = sp.copy()
    w, h = img.size
    cx, cy = w / 2, h / 2
    R = w * (0.29 if v == "a" else 0.27)
    a = np.array(img).astype(np.float64)
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.hypot(xx - cx, yy - cy)
    if state == "hit":
        ix, iy = cx + R * 0.35, cy - R * 0.25
        dd = np.hypot(xx - ix, yy - iy)
        dark = (dd < R * 0.28) & (d < R)
        a[dark, :3] *= 0.45
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
        cracks = Image.new("RGBA", img.size, (0, 0, 0, 0))
        dr = ImageDraw.Draw(cracks)
        for k in range(7):
            ang = rng.uniform(0, TAU)
            x, y = ix, iy
            pts = [(x, y)]
            for _ in range(5):
                ang += rng.uniform(-0.5, 0.5)
                x, y = x + np.cos(ang) * R * 0.11, y + np.sin(ang) * R * 0.11
                pts.append((x, y))
            dr.line(pts, fill=(255, 190, 70, 255), width=1)
        dr.ellipse([ix - 3, iy - 3, ix + 3, iy + 3], fill=(255, 236, 170, 255))
        img.alpha_composite(raster.glow(cracks, 2, 0.6))
        dr = ImageDraw.Draw(img)
        dr.ellipse([ix - 3, iy - 3, ix + 3, iy + 3], fill=(255, 236, 170, 255))
        return img
    # broken
    inside = d < R * 0.98
    grey = a[..., :3].mean(-1, keepdims=True)
    a[inside, :3] = (a[inside, :3] * 0.25 + grey[inside] * 0.75) * 0.32
    hole = inside & (raster.fbm(w, h, 8, seed, octaves=3, period=False) * 0.5
                     + (1 - d / R) * 0.7 > 0.55)
    a[hole, :3] = np.array([18, 14, 12]) + 8 * rng.random((hole.sum(), 1))
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    dr = ImageDraw.Draw(img)
    for _ in range(26):
        ang, rr = rng.uniform(0, TAU), rng.uniform(R * 0.6, R * 1.25)
        x, y = cx + np.cos(ang) * rr, cy + np.sin(ang) * rr
        c = (200, 214, 206, 255) if v == "a" else (190, 180, 168, 255)
        dr.polygon([(x, y), (x + rng.uniform(-4, 4), y + rng.uniform(-4, 4)),
                    (x + rng.uniform(-4, 4), y + rng.uniform(-4, 4))], fill=c)
    return img


# =========================================================================== GIF + sheets

def make_gif(scene, path, colors=128, max_mb=8.0, n=GIF_N, fps=GIF_FPS, min_colors=64):
    """Seamless 4 s loop sampled at ``fps`` (frame i = scene time i * N / n); palette as
    scenes_r06.make_gif with the bullet colours reserved and key sprites kept."""
    with tempfile.TemporaryDirectory() as tmp:
        frames = []
        for i in range(n):
            img = scene.compose(i * N / n)
            img.convert("RGB").save(Path(tmp) / f"f{i:03d}.png")
            if i % 10 == 0:
                frames.append(img)
        while True:
            s6.gif_palette(frames, colors, scene.key_sprites()).save(Path(tmp) / "palette.png")
            cmd = ["ffmpeg", "-v", "error", "-y", "-framerate", str(fps),
                   "-i", str(Path(tmp) / "f%03d.png"), "-i", str(Path(tmp) / "palette.png"),
                   "-lavfi", "[0][1]paletteuse=dither=none:diff_mode=rectangle",
                   "-loop", "0", str(path)]
            subprocess.run(cmd, check=True)
            if path.stat().st_size <= max_mb * 1e6 or colors <= min_colors:
                return colors
            colors -= 8


def wrap(text, width=100):
    return r02.wrap(text, width)


def field_sheet(title, frame, layers, notes, text, extra=None):
    """Review sheet: the play field at 1x, the layer breakdown at 0.4x (back to front), notes.
    ``extra`` (image, label) replaces the second row of the breakdown with a panel."""
    img = raster.sheet(1140, 690, title, SUB)
    raster.draw_text(img, 16, 38, "PLAY FIELD 480X540 AT 1X", raster.LABEL_DIM)
    img.alpha_composite(frame.convert("RGBA"), (16, 50))
    raster.draw_text(img, 516, 38, "LAYER BREAKDOWN, BACK TO FRONT (0.4X)", raster.LABEL_DIM)
    tw, th_ = 192, 216
    slots = list(layers.items())[:6 if extra is None else 3]
    for i, (key, lay) in enumerate(slots):
        x = 516 + (i % 3) * (tw + 12)
        y = 50 + (i // 3) * (th_ + 44)
        cell = r02.checker(tw, th_)
        cell.alpha_composite(lay.resize((tw, th_), Image.BOX))
        img.alpha_composite(cell, (x, y + 12))
        raster.draw_text(img, x, y, f"{i + 1}. {key.upper()}", raster.ACCENT)
        for j, part in enumerate(wrap(notes.get(key, ""), 31)[:2]):
            raster.draw_text(img, x, y + th_ + 16 + 10 * j, part, raster.LABEL_DIM)
    if extra is not None:
        pimg, label = extra
        raster.draw_text(img, 516, 310, label, raster.ACCENT)
        img.alpha_composite(pimg.convert("RGBA"), (516, 322))
        y = 322 + pimg.height + 10
    else:
        y = 50 + 2 * (th_ + 44) + 10 if len(slots) > 3 else 330
    for line in text:
        for part in wrap(line, 102):
            raster.draw_text(img, 516, y, part, raster.LABEL)
            y += 11
        y += 5
    return img


# =========================================================================== canyon (L16)

class Canyon(s6.Scene):
    """Valles Marineris in true perspective round (240, 297). World units are the collision
    layer's (scale 1.0). Model a: the rim and plateau are at 1.0 (the collision line is the
    rim), the floor at 0.6. Model b: the floor is at 1.0 (the collision line is the wall's
    foot), the walls rise toward the camera to the plateau at 1.25 (Level 08's towers)."""

    TRAVEL = 640                               # 160 px/s
    NB = 8                                     # strata bands per wall face
    STEP = 4                                   # wall segments every 4 world px (world-aligned)
    ANCHORS = (150, 470)                       # world y of the tendril-anchor pairs

    def __init__(self, model="a", helix=True):
        self.model = model
        self.kt, self.kb = (1.0, 0.6) if model == "a" else (1.25, 1.0)
        self.FACTORS = {"floor": self.kb, "plateau": self.kt, "low-air": 1.35, "high-air": 2.2}
        super().__init__()
        self.helix = helix
        self.floor = self._floor_tex()
        self.plateau = self._plateau_tex()
        self.station = helix_site() if helix else None
        if self.station is not None and self.kt != 1.0:
            s = self.kt
            self.station = self.station.resize((int(self.station.width * s),
                                                int(self.station.height * s)), Image.LANCZOS)
        rng = np.random.default_rng(16)
        self.plumes = []
        for _ in range(5):
            r = rng.uniform(22, 40)
            blob = raster.fbm(int(r * 2), int(r * 2), 16, int(rng.integers(1000)), octaves=3,
                              period=False)
            yy, xx = np.mgrid[0:int(r * 2), 0:int(r * 2)]
            fall = np.clip(1 - np.hypot(xx - r, yy - r) / r, 0, 1)
            a = np.clip(blob * fall * 2.2 - 0.25, 0, 1) * 120
            img = rgba(np.broadcast_to(np.array(DUST[4], float), a.shape + (3,)), a)
            self.plumes.append((r03.step_alpha(img, 5), rng.uniform(-60, 60),
                                rng.uniform(0, 1.35 * self.TRAVEL)))
        self.streaks = [(rng.uniform(0, W), rng.uniform(0, 2.2 * self.TRAVEL),
                         rng.uniform(0.3, 1.0)) for _ in range(14)]
        self.anchor_sp = u36.anchors["a"]

    # canyon centre line and half width at the collision layer, periodic in world y
    def centre(self, yw):
        t = TAU * np.asarray(yw, float) / self.TRAVEL
        return CX + 58 * np.sin(t) + 20 * np.sin(2 * t + 0.7)

    def half(self, yw):
        t = TAU * np.asarray(yw, float) / self.TRAVEL
        return 123 + 28 * np.sin(2 * t + 2.2) - 10 * np.cos(3 * t)   # gap 180 .. 320 px

    def scr(self, xw, yw, k, off1):
        """Screen position of world (xw, yw) on the layer with scale ``k``."""
        return CX + (np.asarray(xw) - CX) * k, (np.asarray(yw) + off1) * k + CY * (1 - k)

    # textures in each layer's own coordinates: u = screen x, v = world y * k (period k*TRAVEL)
    def _layer_world(self, k, per):
        vv, uu = np.mgrid[0:per, 0:W].astype(np.float64)
        yw = vv / k
        xw = CX + (uu - CX) / k
        return xw, yw

    def _floor_tex(self):
        k = self.kb
        per = int(round(k * self.TRAVEL))
        xw, yw = self._layer_world(k, per)
        n = raster.fbm(W, per, 64 if per % 64 == 0 else 32, 161, octaves=4, period=True)
        rip = np.sin((xw * 0.3 + yw * 1.0 + n * 40) / 5.0) * 0.018
        dx = xw - self.centre(yw)
        river = 6 * np.sin(TAU * yw / self.TRAVEL * 3 + 1.0)
        bed = np.exp(-((dx - river) / 7.0) ** 2)
        talus = np.clip((np.abs(dx) - (self.half(yw) - 34)) / 30, 0, 1)
        hgt = 0.42 + 0.3 * n + rip - 0.22 * bed + 0.3 * talus * n
        col = shade_rgb(hgt, RUST[1:6], strength=12, periodic=True)
        col *= (0.86 + 0.14 * (1 - bed))[..., None]
        img = rgba(col)
        if self.model == "a":                                   # far below: hazed
            img = mix_to(img, SKY, 0.22)
        return tone(img, 14)

    def _plateau_tex(self):
        k = self.kt
        per = int(round(k * self.TRAVEL))
        xw, yw = self._layer_world(k, per)
        cell = 128 if per % 128 == 0 else 160
        n = raster.fbm(W, per, cell, 171, octaves=4, period=True)
        ridge = 1 - np.abs(2 * raster.fbm(W, per, cell // 2, 173, octaves=3, period=True) - 1)
        hgt = 0.6 * n + 0.3 * ridge ** 3
        rng = np.random.default_rng(31)
        for _ in range(6):
            cx, cy, r = rng.uniform(0, W), rng.uniform(0, per), rng.uniform(6, 22) * k
            dy = s6.periodic_dist(np.arange(per)[:, None], cy, per)
            d = np.hypot(np.arange(W)[None, :] - cx, dy) / r
            hgt = hgt + (np.where(d < 1, -(1 - d ** 2), 0.0) * 0.22
                         + np.exp(-((d - 1) / 0.2) ** 2) * 0.07) * (r / 22)
        edge = np.abs(xw - self.centre(yw)) - self.half(yw)          # 0 at the rim
        ledge = np.clip(1 - edge / 16, 0, 1) * (edge > 0)
        hgt = hgt + 0.08 * ledge * np.sin(yw * 0.35)
        col = shade_rgb(np.clip(hgt, 0, 1), RUST[:5], strength=16, periodic=True) * 0.95
        col *= (1 + 0.12 * ledge)[..., None]
        return tone(rgba(col), 16, 7)

    # ---------------------------------------------------------------- per frame
    def outlines(self, off1, margin=80):
        """World-aligned samples (so the wall segments never swim) covering the screen at both
        the plateau's and the floor's scale."""
        lo = min((-margin - CY * (1 - k)) / k - off1 for k in (self.kt, self.kb))
        hi = max((H + margin - CY * (1 - k)) / k - off1 for k in (self.kt, self.kb))
        lo = np.floor(lo / self.STEP) * self.STEP
        return np.arange(lo, hi + self.STEP, self.STEP)

    def ground_layers(self, f, ss=2):
        off1 = self.TRAVEL * f / N
        kt, kb = self.kt, self.kb
        floor = scroll_tex(self.floor, off1 * kb + CY * (1 - kb))
        yws = self.outlines(off1)
        cen, hw = self.centre(yws), self.half(yws)
        sides = {s: cen + s * hw for s in (-1, 1)}
        # cast shadow of the walls on the floor: the solid region at the floor's scale,
        # moved toward the bottom-right (key light from the top-left)
        S = 24 if self.model == "a" else 20
        sh = Image.new("L", (W * ss, H * ss), 0)
        dsh = ImageDraw.Draw(sh)
        for s in (-1, 1):
            x, y = self.scr(sides[s], yws, kb, off1)
            far = -400 if s < 0 else W + 400
            pts = list(zip(x + S, y + S * 0.8)) + [(far, y[-1] + S), (far, y[0] + S)]
            dsh.polygon([(px * ss, py * ss) for px, py in pts], fill=255)
        sh = sh.resize((W, H), Image.BOX).filter(ImageFilter.GaussianBlur(1.2))
        fa = np.array(floor).astype(np.float64)
        m = np.array(sh, float)[..., None] / 255
        fa[..., :3] *= 1 - 0.5 * m
        img = Image.fromarray(fa.astype(np.uint8), "RGBA").resize((W * ss, H * ss), Image.NEAREST)
        d = ImageDraw.Draw(img)
        # wall faces in strata bands between the plateau (kt) and the floor (kb)
        to_light = np.array([-0.7071, -0.7071])
        ks = [kt + (kb - kt) * i / self.NB for i in range(self.NB + 1)]
        for s in (-1, 1):
            xs = sides[s]
            pts = {k: self.scr(xs, yws, k, off1) for k in ks}
            for j in range(len(yws) - 1):
                tx, ty = xs[j + 1] - xs[j], yws[j + 1] - yws[j]
                nrm = np.array([ty, -tx]) / (np.hypot(tx, ty) or 1)
                if nrm[0] * s > 0:
                    nrm = -nrm                                   # face into the canyon
                lit = 0.42 + 0.58 * max(0.0, float(nrm @ to_light))
                lit = np.round(lit * 6) / 6                      # few light levels (palette)
                wj = int(round(yws[j] / self.STEP))
                g = ((wj * 7919) % 13) / 13.0
                gully = 0.84 if (wj * 37) % 11 == 0 else 0.95 + 0.06 * g
                for b in range(self.NB):
                    k0, k1 = ks[b], ks[b + 1]
                    q = [(pts[k0][0][j], pts[k0][1][j]), (pts[k0][0][j + 1], pts[k0][1][j + 1]),
                         (pts[k1][0][j + 1], pts[k1][1][j + 1]), (pts[k1][0][j], pts[k1][1][j])]
                    base = STRATA[b % len(STRATA)]
                    jit = 0.96 + 0.08 * (((wj * 131 + b * 71) % 17) / 17.0)
                    shade = lit * gully * jit * (1 - 0.05 * b)
                    col = tuple(int(min(255, c * shade)) for c in base)
                    d.polygon([(x * ss, y * ss) for x, y in q], fill=col)
        # plateau with the canyon cut out
        plat = scroll_tex(self.plateau, off1 * kt + CY * (1 - kt)).resize((W * ss, H * ss),
                                                                          Image.NEAREST)
        mask = Image.new("L", (W * ss, H * ss), 255)
        xl, yl = self.scr(sides[-1], yws, kt, off1)
        xr, yr = self.scr(sides[1], yws, kt, off1)
        ImageDraw.Draw(mask).polygon([(x * ss, y * ss) for x, y in
                                      list(zip(xl, yl)) + list(zip(xr, yr))[::-1]], fill=0)
        img.paste(plat, (0, 0), mask)
        for (x, y), col in (((xl, yl), (226, 160, 104)), ((xr, yr), (250, 196, 140))):
            d.line([(a * ss, b * ss) for a, b in zip(x, y)], fill=col, width=ss)
        img = img.resize((W, H), Image.BOX)
        # the Helix research station on the north (left) rim
        if self.station is not None:
            yw0 = 125.0
            for rep in (-1, 0, 1):
                ywr = yw0 + rep * self.TRAVEL
                x, y = self.scr(self.centre(yw0) - self.half(yw0) - 74, ywr, kt, off1)
                if -80 < y < H + 80:
                    sp = self.station
                    sprite.paste_center(img, sprite.shadow_of(sp, opacity=0.5, blur=1.2),
                                        x + 7, y + 9)
                    sprite.paste_center(img, sp, x, y)
        return img, floor

    def anchors(self, img, shadows, f):
        """Tendril-anchor pairs where gameplay puts them: on the collision line (the rim in
        model a, the wall's foot in model b), linked by the damaging tendril."""
        off1 = self.TRAVEL * f / N
        for yw0 in self.ANCHORS:
            for rep in (-1, 0, 1):
                yw = yw0 + rep * self.TRAVEL
                _, y = self.scr(0, yw, 1.0, off1)
                if not -60 < y < H + 60:
                    continue
                xl = self.centre(yw0) - self.half(yw0) + 8
                xr = self.centre(yw0) + self.half(yw0) - 8
                pts = []
                for i in range(41):
                    t = i / 40
                    x = xl + (xr - xl) * t
                    pts.append((x, y + 5 * np.sin(TAU * (t * 1.5 + f / N * 2)) * np.sin(np.pi * t)))
                dr = ImageDraw.Draw(img)
                dr.line(pts, fill=(60, 10, 20, 255), width=5)
                dr.line(pts, fill=(214, 52, 74, 255), width=3)
                dr.line(pts, fill=(255, 150, 160, 255), width=1)
                place(img, shadows, self.anchor_sp.get(0.0, pulse=0.0), xl, y, "ground")
                place(img, shadows, self.anchor_sp.get(np.pi, pulse=0.0), xr, y, "ground")

    def player_xy(self, f):
        off1 = self.TRAVEL * f / N
        yw = 450 - off1
        x = float(self.centre(yw)) + 0.35 * float(self.half(yw)) * np.sin(TAU * f / N * 2)
        vx = np.cos(TAU * f / N * 2)
        return x, 450.0, vx

    def layer_images(self, f):
        ground, floor = self.ground_layers(f)
        shadows = blank()
        self.anchors(ground, None, f)
        off1 = self.TRAVEL * f / N
        low = blank()
        for img, dx, y0 in self.plumes:
            for yy in self.s.ys("low-air", y0, f, margin=60):
                yw = (yy - CY * (1 - 1.35)) / 1.35 - off1
                sprite.paste_center(low, img, float(self.centre(yw)) + dx, yy)
        air = blank()
        casters = []
        for i in range(4):                                       # skitters down the canyon
            t = ((f / N) * 2 + i * 0.07) % 1.0
            y = -50 + 680 * t
            yw = y - off1
            x = float(self.centre(yw)) + (i - 1.5) * 26 + 20 * np.sin(TAU * t * 2)
            sp = unit("skitter-a", [-1.0, 0.0, 1.0, 0.0][int(f // 2 + i) % 4])
            casters.append((sp, x, y))
            sprite.paste_center(air, sp, x, y)
        px, py, vx = self.player_xy(f)
        player(air, casters, px, py, vx, phase=(f * 24) % 360)
        shadows.alpha_composite(r02.shadow_layer(casters, *s6.SHADOW["air"], opacity=0.45))
        high = blank()
        dh = ImageDraw.Draw(high)
        step = 2.2 * self.TRAVEL / N
        for x, y, b in self.streaks:
            for yy in self.s.ys("high-air", y, f, margin=40):
                dh.line([x, yy - step * b, x, yy], fill=DUST[4] + (int(30 + 40 * b),), width=2)
        name_f, name_g = ("far", "ground") if self.model == "a" else ("ground", "rim")
        return {name_f: floor, name_g: ground, "shadows": shadows, "low-air": low, "air": air,
                "high-air": high}

    def compose(self, f):
        L = self.layer_images(f)
        img = L["ground" if self.model == "a" else "rim"].copy()
        for k in ("shadows", "low-air", "air", "high-air"):
            img.alpha_composite(L[k])
        return img

    def key_sprites(self):
        return (list(CAST.base.player.values()) + [CAST.base.bolt, CAST.base.flare]
                + list(CAST._u.values())[:4] + [self.anchor_sp.get(0.0, pulse=0.0)])

    def collision_overlay(self, f):
        """The frame with the collision line and the no-fly region marked (for the sheet)."""
        img = self.compose(f).convert("RGBA")
        off1 = self.TRAVEL * f / N
        yws = self.outlines(off1)
        ov = blank()
        d = ImageDraw.Draw(ov)
        for s in (-1, 1):
            x, y = self.scr(self.centre(yws) + s * self.half(yws), yws, 1.0, off1)
            far = -50 if s < 0 else W + 50
            d.polygon(list(zip(x, y)) + [(far, y[-1]), (far, y[0])], fill=(255, 40, 60, 70))
            d.line(list(zip(x, y)), fill=(255, 255, 255, 255), width=2)
        img.alpha_composite(ov)
        return img


def canyon_diagram(model, w=612, h=150):
    """Side view of the camera model: rim, floor, the collision line and what scrolls at 1.0."""
    img = Image.new("RGBA", (w, h), (24, 28, 38, 255))
    d = ImageDraw.Draw(img)
    cam = (w / 2, 10)
    raster.draw_text(img, w / 2 - 15, 2, "CAMERA", raster.LABEL_DIM)
    if model == "a":
        rim_y, floor_y, ship_y = 62, 128, 56
        lab = [("PLATEAU / RIM X1.0 = COLLISION LINE", 8, rim_y - 12),
               ("FLOOR X0.6 (FAR, NOTHING TO HIT)", w / 2 - 95, floor_y + 6)]
    else:
        rim_y, floor_y, ship_y = 50, 112, 104
        lab = [("PLATEAU X1.25 (SCENERY)", 8, rim_y - 12),
               ("FLOOR X1.0 = COLLISION LINE (WALL FOOT)", w / 2 - 120, floor_y + 6)]
    gl, gr = w / 2 - 120, w / 2 + 120
    rock = (150, 80, 46, 255)
    d.polygon([(0, rim_y), (gl, rim_y), (gl + 8, floor_y), (0, floor_y)], fill=rock)
    d.polygon([(w, rim_y), (gr, rim_y), (gr - 8, floor_y), (w, floor_y)], fill=rock)
    d.rectangle([0, floor_y, w, floor_y + 4], fill=(110, 56, 30, 255))
    cy_line = rim_y if model == "a" else floor_y - 2
    xl, xr = (gl, gr) if model == "a" else (gl + 8, gr - 8)
    d.line([xl, cy_line, xr, cy_line], fill=(255, 255, 255, 255), width=1)
    for x in (xl, xr):
        d.line([x, cy_line - 8, x, cy_line + 8], fill=(255, 60, 70, 255), width=2)
    for x in (gl, gr):
        d.line([cam, (x, rim_y)], fill=(90, 110, 150, 255))
    d.polygon([(w / 2 - 6, ship_y + 4), (w / 2 + 6, ship_y + 4), (w / 2, ship_y - 6)],
              fill=(80, 170, 255, 255))
    raster.draw_text(img, w / 2 + 10, ship_y - 4, "SHIP", (120, 190, 255))
    for t, x, y in lab:
        raster.draw_text(img, int(x), int(y), t, raster.LABEL)
    return img


# =========================================================================== storm (L18)

class Storm(s6.Scene):
    """The northern plains in the dust storm. The ground and low-air vanish into a rust veil
    outside a radius round the ship; air units outside it are dark silhouettes that keep their
    glow; bullets, the ship and its shots are always drawn. Variant a: a clear bubble with a
    hard edge. Variant b: a soft, ragged falloff with dust tongues that drift."""

    TRAVEL = 480                               # 120 px/s
    FACTORS = {"ground": 1.0, "low-air": 1.3, "high-air": 2.0}
    R = 170

    def __init__(self, variant="a"):
        super().__init__()
        self.v = variant
        per = self.TRAVEL
        n = raster.fbm(W, per, 96, 181, octaves=5, period=True)
        ch = np.abs(raster.fbm(W, per, 96, 183, octaves=4, period=True) - 0.5)
        chan = np.clip(1 - ch / 0.035, 0, 1)                    # dry channels
        flat_lake = np.exp(-(((np.arange(W)[None, :] - 340) / 90) ** 2
                             + ((s6.periodic_dist(np.arange(per)[:, None], 320, per)) / 70) ** 2))
        hgt = 0.5 + 0.28 * n - 0.16 * chan - 0.12 * flat_lake
        col = shade_rgb(hgt, RUST[1:], strength=22, periodic=True)
        col = col * (1 - 0.12 * flat_lake[..., None]) + np.array(OCHRE[3]) * 0.12 * flat_lake[..., None]
        g = rgba(col)
        d = ImageDraw.Draw(g)
        for dy in (-per, 0, per):                                # half-buried pipeline
            d.line([(0, 60 + dy), (W, 200 + dy)], fill=(70, 62, 58, 255), width=7)
            d.line([(0, 58 + dy), (W, 198 + dy)], fill=(150, 140, 132, 255), width=3)
            for x in range(20, W, 60):
                y = 60 + dy + (200 - 60) * x / W
                d.rectangle([x - 3, y - 6, x + 3, y + 6], fill=(96, 88, 82, 255))
        self.ground_tex = tone(g, 18)
        rover = station.render_part(station.cargo_part(), B)
        self.rover = rover.rotate(28, expand=True, resample=Image.BICUBIC)
        rng = np.random.default_rng(18)
        vn = raster.fbm(W, H, 60, 185, octaves=3, period=True)
        vn2 = raster.fbm(W, H, 60, 187, octaves=3, period=True)
        self.veil_a = vn
        self.veil_b = vn2
        self.edge_noise = raster.fbm(W, 540, 60, 189, octaves=3, period=True)
        self.bank = s6.bank_tex(624, [208, 104, 52], 191, 0.62, 0.12, DUST[1:], max_alpha=160)
        self.streaks = [(rng.uniform(-100, W), rng.uniform(0, 960), rng.uniform(0.3, 1.0))
                        for _ in range(26)]
        self.devil_cache = {}

    def player_xy(self, f):
        t = TAU * f / N
        return CX + 50 * np.sin(t), 430 + 8 * np.sin(2 * t), np.cos(t)

    def devil(self, f, r=66):
        """A Dust Devil: a spinning column of dust with spiral arms, its open core glowing."""
        ph = int(round(f * 2)) % 40
        if ph in self.devil_cache:
            return self.devil_cache[ph]
        s = 2 * r + 2
        yy, xx = np.mgrid[0:s, 0:s] - r
        rr = np.hypot(xx, yy) / r
        th = np.arctan2(yy, xx)
        rot = TAU * ph / 40 * 3
        arm = (0.5 + 0.5 * np.sin(3 * th + rr * 7.0 - rot)) ** 2
        dens = np.clip((1 - rr) * 1.6, 0, 1) * (0.25 + 0.75 * arm)
        col = raster.ramp([(0, (96, 52, 30)), (0.45, DUST[3]), (0.8, DUST[4]), (1, (246, 220, 180))],
                          arm * (1 - rr * 0.35))
        a = dens * 235
        core = rr < 0.16
        col[core] = (30, 22, 18)
        ring = (rr >= 0.12) & (rr < 0.22)
        col[ring] = col[ring] * 0.3 + np.array(LIME) * 0.7 * (0.75 + 0.25 * np.sin(rot * 2))
        a[core | ring] = 255
        img = r03.step_alpha(rgba(col, a), 6)
        self.devil_cache[ph] = img
        return img

    def vis(self, f, px, py):
        yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
        d = np.hypot(xx - px, yy - py)
        if self.v == "a":
            return np.clip((self.R + 5 - d) / 10, 0, 1)
        off = int(round(f / N * 540))
        n = np.roll(self.edge_noise, off, axis=0)
        n2 = np.roll(self.edge_noise[::-1], -off, axis=0)
        dd = d + 70 * (n - 0.5) + 40 * (n2 - 0.5)
        return sdf.smoothstep(self.R + 55, self.R - 65, dd)

    def veil(self, f):
        a = np.roll(self.veil_a, int(round(f / N * H)), axis=0)
        b = np.roll(np.roll(self.veil_b, int(round(f / N * 2 * H)), axis=0),
                    int(round(-f / N * 480)), axis=1)
        t = 0.55 * a + 0.45 * b
        col = raster.ramp([(0, (138, 84, 50)), (0.5, (176, 118, 74)), (1, (214, 162, 110))], t)
        return np.round(col / 6) * 6                     # banded like a 90s translucency table

    def air_units(self, f):
        """(sprite, x, y) of the air units this frame."""
        out = []
        t = (f / N) % 1.0
        for i in range(3):                                  # Ghost Drones strafing in from the right
            u = (t + i * 0.08) % 1.0
            x = W + 40 - 640 * u
            y = 110 + i * 34 + 40 * np.sin(TAU * u)
            h = round(np.pi + 0.3 * np.cos(TAU * u), 1)
            out.append((ghost(h), x, y, h))
        for i in range(4):                                  # Needlers diving
            u = (t * 2 + i * 0.12) % 1.0
            x = 90 + i * 30 + 30 * np.sin(TAU * u)
            y = -40 + 640 * u
            out.append((unit("needler-a", [-1.0, 0.0, 1.0, 0.0][int(f // 2 + i) % 4]), x, y, None))
        return out

    def layer_images(self, f):
        off = self.TRAVEL * f / N
        g = scroll_tex(self.ground_tex, off)
        for yy in self.s.ys("ground", 200, f):
            place(g, None, self.rover, 60, yy, "ground")
            if int(f) % 10 < 5:
                g = raster.add_light(g, 60, yy - 10, 6, (255, 40, 40), 1.6)
        for yy in self.s.ys("ground", 60, f):
            sprite.paste_center(g, self.devil(f), 330 + 20 * np.sin(TAU * f / N), yy)
        low = blank()
        low.alpha_composite(scroll_tex(self.bank, 1.3 * off))
        return {"ground": g, "low-air": low}

    def compose(self, f, mask_view=False):
        f = f % N
        L = self.layer_images(f)
        base = L["ground"].copy()
        base.alpha_composite(L["low-air"])
        px, py, vx = self.player_xy(f)
        casters = []
        units = self.air_units(f)
        for sp, x, y, _ in units:
            casters.append((sp, x, y))
        sh = r02.shadow_layer(casters + [(CAST.base.player["c"], px, py)], *s6.SHADOW["air"],
                              opacity=0.45)
        base.alpha_composite(sh)
        v = self.vis(f, px, py)
        veil = self.veil(f)
        arr = np.array(base).astype(np.float64)[..., :3]
        arr = arr * 0.8 + np.array((176, 118, 74)) * 0.2       # the storm tints even the clear part
        out = arr * v[..., None] + veil * (1 - v[..., None])
        img = rgba(out)
        if mask_view:
            m = rgba(np.broadcast_to((v * 255)[..., None], v.shape + (3,)))
            return m
        for sp, x, y, lh in units:
            xi, yi = int(np.clip(x, 0, W - 1)), int(np.clip(y, 0, H - 1))
            vu = v[yi, xi]
            if vu >= 0.98:
                sprite.paste_center(img, sp, x, y)
            elif vu <= 0.02:
                sprite.paste_center(img, sil(sp, lh), x, y)
            else:
                sprite.paste_center(img, sil(sp, lh), x, y)
                sprite.paste_center(img, r02.alpha_scale(sp, vu), x, y)
        # bullets: always drawn (the layer rule)
        for i, (sp, x, y, _) in enumerate(units[:3]):
            if 0 < x < W:
                aimed_orbs(img, (x, y), (px, py), n=2, gap=60, start=34 + (f * 3 + i * 17) % 60)
        player(img, [], px, py, vx, phase=(f * 18) % 360)
        hi = blank()
        dh = ImageDraw.Draw(hi)
        off = self.TRAVEL * f / N
        for x, y, b in self.streaks:
            yy = (y + 2.0 * off) % 960 - 200
            xx = x + (yy + 200) * 0.35
            dh.line([xx, yy, xx - 10 * b, yy - 28 * b], fill=DUST[4] + (int(40 + 50 * b),), width=1)
        img.alpha_composite(hi)
        return img

    def key_sprites(self):
        return (list(CAST.base.player.values()) + [CAST.base.bolt, CAST.base.flare, ghost(np.pi)]
                + list(CAST._u.values())[:4] + [v[1] for v in list(_sil.values())[:4]]
                + [self.devil(0)])


def radius_panel(scene, f, w=612, h=170):
    """The look's rules beside the frame: visibility mask (white = drawn) and a legend."""
    m = scene.compose(f, mask_view=True).resize((W * h // H, h), Image.BOX)
    img = Image.new("RGBA", (w, h), (24, 28, 38, 255))
    img.alpha_composite(m.convert("RGBA"), (0, 0))
    x0 = m.width + 14
    lines = ["VISIBILITY MASK (WHITE = GROUND DRAWN)",
             f"RADIUS {scene.R} PX ROUND THE SHIP (DEEP STORM)",
             "OUTSIDE: GROUND + LOW-AIR -> RUST VEIL",
             "AIR UNITS -> DARK SILHOUETTE, GLOW KEPT",
             "ALWAYS DRAWN: BULLETS, SHIP, SHOTS",
             "HIGH-AIR STREAKS <= 40 % OVER ALL"]
    if scene.v == "a":
        lines.append("EDGE: HARD, 10 PX")
    else:
        lines.append("EDGE: SOFT, +-60 PX, DRIFTING TONGUES")
    for i, t in enumerate(lines):
        raster.draw_text(img, x0, 8 + i * 14, t, raster.LABEL if i else raster.ACCENT)
    sp = ghost(np.pi)
    sprite.paste_center(img, sprite.enlarge(sp, 2), x0 + 40, 140)
    sprite.paste_center(img, sprite.enlarge(sil(sp, np.pi), 2), x0 + 140, 140)
    raster.draw_text(img, x0 + 175, 136, "GHOST DRONE IN / OUT (2X)", raster.LABEL_DIM)
    return img


# =========================================================================== mood stills

def olympus():
    """L15: high over the Olympus Mons flank (deep, x0.4: the nested caldera pits, terraced
    walls, radial lava channels), a lower cloud deck (x0.7) with gaps, the upper deck's tongues
    on low-air (x1.3), Dust Kites' stand-ins and the ship on the play plane."""
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    n = nfield(W, H, 60, 151, octaves=5)
    cx, cy = 330.0, 170.0
    r = np.hypot(xx - cx, yy - cy)
    th = np.arctan2(yy - cy, xx - cx)
    hgt = 0.72 - r / 900 + 0.06 * n
    lava = 0.5 + 0.5 * np.sin(th * 46 + n * 9 + np.sin(r / 40))
    hgt += 0.035 * lava ** 6 * np.clip((r - 150) / 120, 0, 1)
    pits = np.zeros_like(hgt)
    n2 = nfield(W, H, 30, 155, octaves=3)
    for px, py, pr, dep in ((330, 170, 120, 0.16), (276, 220, 72, 0.12), (378, 112, 54, 0.09)):
        d = np.hypot(xx - px, yy - py) / (pr * (1 + 0.12 * (n2 - 0.5)))
        pit = sdf.smoothstep(1.0, 0.8, d)
        hgt -= dep * pit
        pits = np.maximum(pits, pit)
    hgt -= 0.03 * sdf.smoothstep(215, 235, r + 60 * n2)              # the flank's outer scarp
    gy, gx = np.gradient(hgt)
    sh = np.clip(1 - (gx + gy) * 90, 0.35, 1.7)
    t = np.clip(0.35 + 0.5 * n + 0.15 * (1 - r / 500), 0, 1)
    flank = [(0, BASALT[1]), (0.45, BASALT[2]), (0.7, (128, 66, 40)), (1, (170, 104, 66))]
    col = raster.ramp(flank, t) * sh[..., None] * (0.55 + 0.45 * cast_np(hgt, 40, 0.0012))[..., None]
    col *= (1 - 0.3 * pits)[..., None]
    deep = tone(mix_to(rgba(col), SKY, 0.26), 22)
    # cloud decks (Mars water-ice cloud: pale peach, cool shadowed undersides)
    cn = r03.noise(W, 540, [180, 90, 45, 15], 153, gains=[1, 0.55, 0.3, 0.12])
    gap = np.exp(-(((xx - 300) / 150) ** 2 + ((yy - 190) / 140) ** 2))
    far = r03.bank(np.clip(cn - 0.55 * gap, 0, 1), 0.47, 0.12, CLOUD_D, max_alpha=235)
    far = r03.step_alpha(tone(far, 16, 3), 6)
    fa = np.array(far)[..., 3].astype(np.float64)
    fa = np.pad(fa, ((34, 0), (26, 0)), mode="edge")[:H, :W]
    fa = np.array(Image.fromarray(fa.astype(np.uint8)).filter(ImageFilter.GaussianBlur(4)), float)
    cshadow = rgba(np.zeros((H, W, 3)), fa * 0.42)
    deep_sh = deep.copy()
    deep_sh.alpha_composite(cshadow)
    cn2 = r03.noise(W, 540, [180, 90, 45], 157)
    edge = np.clip((np.abs(xx - 240) - 120) / 120, 0, 1)
    low = r03.bank(cn2 * (0.55 + 0.6 * edge), 0.62, 0.12, CLOUD_D[1:], max_alpha=215)
    low = r03.step_alpha(tone(low, 12, 3), 6)
    air, casters = blank(), []
    for i, (x, y) in enumerate(((150, 120), (178, 146), (122, 146), (206, 172), (94, 172))):
        sp = unit("skitter-a", [-1.0, 0.0, 1.0][i % 3])
        casters.append((sp, x, y))
        sprite.paste_center(air, sp, x, y)
    sp = unit("stinger-a")
    casters.append((sp, 380, 250))
    sprite.paste_center(air, sp, 380, 250)
    aimed_orbs(air, (380, 250), (250, 440), n=2)
    player(air, casters, 250, 440, 0.0)
    shadows = r02.shadow_layer(casters, 21, 30, opacity=0.42)
    sh_on_cloud = r02.masked(r02.shadow_layer(casters, 12, 17, opacity=0.4), far)
    high = s6.wisp_tex(540, [90, 45], 159, CLOUD[3], thresh=0.7, max_a=70, stretch=4)
    frame = deep_sh.copy()
    for lay in (r02.masked(shadows, deep), far, sh_on_cloud, low, air, high):
        frame.alpha_composite(lay)
    layers = {"deep": deep_sh, "far": far, "low-air": low, "air": air, "high-air": high}
    notes = {"deep": "X0.4 OLYMPUS MONS FLANK, NESTED CALDERA PITS, LAVA CHANNELS, HAZED",
             "far": "X0.7 LOWER CLOUD DECK, GAP OVER THE CALDERA, SHADOWS ON THE FLANK",
             "low-air": "X1.3 UPPER DECK: TONGUES AT THE EDGES ONLY",
             "air": "PLAY PLANE; ROUND 04 STAND-INS FOR DUST KITES",
             "high-air": "X2 ICE WISPS UNDER 35 %"}
    text = ["L15 OLYMPUS DESCENT, SECTIONS 2-3: OUT OF THE ENTRY CORRIDOR, DOWN THROUGH TWO CLOUD "
            "DECKS PAST THE VOLCANO. THE CALDERA'S NESTED COLLAPSE PITS AND TERRACED WALLS READ AS "
            "OLYMPUS; THE FLANK IS DARK BASALT, HAZED TOWARD THE BUTTERSCOTCH SKY (IT IS FAR BELOW).",
            "THE LOWER DECK SITS BELOW THE PLAY PLANE (FAR), SO IT NEVER HIDES A BULLET; FLYERS "
            "SHADOW ITS TOPS (12,17). THE UPPER DECK KEEPS TO THE FIELD'S EDGES (LOW-AIR, ABOUT "
            "20 %). MARS CLOUD IS WATER ICE: PALE PEACH TOPS, COOL MAUVE UNDERSIDES.",
            "LATER SECTIONS (LYCUS ESCARPMENT, THARSIS DUNES AND THE RAIL LINE) USE THE PLAINS "
            "GROUND OF THE STORM AND HELLAS STILLS IN RUST."]
    return frame, layers, notes, text


def valles():
    sc = Canyon("a", helix=True)
    f = 26
    L = sc.layer_images(f)
    frame = sc.compose(f)
    layers = {"far": L["far"], "ground": L["ground"], "low-air": L["low-air"],
              "air": L["air"], "high-air": L["high-air"]}
    notes = {"far": "X0.6 CANYON FLOOR, DRY RIVERBED, TALUS",
             "ground": "X1.0 PLATEAU + RIM = COLLISION LINE; WALLS DRAWN 1.0 -> 0.6",
             "low-air": "X1.35 DUST PLUMES IN THE CANYON",
             "air": "PLAY PLANE; TENDRIL-ANCHOR PAIR ON THE RIMS",
             "high-air": "X2.2 DUST STREAKS"}
    text = ["L16 VALLES CANYON RUN, SECTION 4 (THE COPRATES RAPIDS): THE HELIX RESEARCH STATION ON "
            "THE NORTH RIM (LEFT; THE SHIP FLIES EAST), BLACK AND GOLD, LIT, UNTOUCHED: SCENERY, "
            "NEVER A TARGET. THE CANYON IS DRAWN IN TRUE PERSPECTIVE ROUND (240, 297): EIGHT "
            "STRATA BANDS FROM THE RIM AT X1.0 DOWN TO THE FLOOR AT X0.6, SO THE WALLS TURN AS "
            "THEY SCROLL; THE LEFT WALL'S FACE IS IN SHADOW AND THROWS A SHADOW ON THE FLOOR.",
            "THIS STILL USES CAMERA MODEL A (THE DECIDED RULE); CANYON-WALLS-R36-A/B SET IT AGAINST "
            "MODEL B. UNITS: THE ROUND-36 TENDRIL ANCHOR A, ROUND 04 SKITTERS."]
    return frame, layers, notes, text


PLAIN = [(62, 30, 20), (104, 54, 34), (144, 82, 52), (176, 112, 74), (204, 146, 100)]


def plains(seed, stops, w=W, h=H, relief=0.28, ripples=0.015):
    stops = PLAIN
    n = nfield(w, h, 90, seed, octaves=5)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rip = np.sin((xx * 0.4 + yy + n * 60) / 7.0) * ripples
    hgt = 0.45 + relief * n + rip
    return shade_rgb(hgt, stops, strength=24, shadow=0.01), hgt


def ares():
    """L17: Ares Landing: the outer domes on their rings with greenhouse annexes, the rail line
    from the north, the ring road, burnt outer greenhouses, a militia gun pit, Vrell growth
    creeping toward the rings, and the Helix compound untouched in a clean perimeter."""
    col, _ = plains(171, RUST[1:])
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    # Vrell nest growth from the lower left, kept 100 px clear of the compound
    vn = nfield(W, H, 45, 173, octaves=4)
    reach = np.clip(1 - np.hypot(xx - 20, yy - 520) / 330, 0, 1)
    growth = (vn * 0.6 + reach * 0.9) > 0.78
    veins = growth & (np.abs(nfield(W, H, 30, 175, 3) - 0.5) < 0.03)
    col[growth] = col[growth] * 0.35 + np.array((54, 20, 70)) * 0.65
    col[veins] = np.array((40, 200, 150))
    g = rgba(col)
    d = ImageDraw.Draw(g)
    # ring road (arc of the inner ring road) and the rail line from the north
    d.arc([180, 260, 1020, 1100], 180, 250, fill=(76, 70, 66, 255), width=16)
    d.arc([186, 266, 1014, 1094], 180, 250, fill=(200, 180, 120, 255), width=1)
    for off in (-6, 6):
        pts = [(64 + off + 10 * np.sin(t / 90), t) for t in range(-10, 560, 6)]
        d.line(pts, fill=(150, 146, 140, 255), width=2)
    for t in range(-10, 560, 8):
        x = 64 + 10 * np.sin(t / 90)
        d.line([x - 10, t, x + 10, t], fill=(70, 44, 30, 255), width=2)
    g = tone(g, 26, 3)
    shadows, ground = blank(), blank()
    # domes with concrete aprons
    for x, y in ((150, 150), (352, 290)):
        d2 = ImageDraw.Draw(g)
        d2.ellipse([x - 70, y - 70, x + 70, y + 70], fill=(116, 100, 88, 255))
        d2.ellipse([x - 66, y - 66, x + 66, y + 66], fill=(132, 116, 102, 255))
        sp = dome_sprite("a")
        place(ground, shadows, sp, x, y, "ground", opacity=0.55)
    for i, (x, y, burnt) in enumerate(((236, 92, False), (256, 116, False),
                                       (120, 420, True), (150, 440, True), (180, 410, True))):
        sp = greenhouse(56, 9, burnt).rotate(90, expand=True)
        place(ground, shadows, sp, x, y, "ground")
    gp = station.render_part(station.turret_part(), B)
    ImageDraw.Draw(g).ellipse([238, 212, 270, 244], outline=(150, 120, 80, 255), width=4)
    place(ground, shadows, gp, 254, 228, "ground")
    # the Helix compound: clean fence, lit, no growth within 100 px
    hx, hy = 392, 470
    dd = ImageDraw.Draw(g)
    dd.rectangle([hx - 82, hy - 62, W + 4, H + 4], fill=(150, 112, 80, 255))
    for x in range(hx - 82, W, 8):
        dd.point((x, hy - 62), fill=(230, 226, 214, 255))
    for y in range(hy - 62, H, 8):
        dd.point((hx - 82, y), fill=(230, 226, 214, 255))
    place(ground, shadows, helix_site(), hx + 6, hy + 10, "ground", opacity=0.55)
    ground = raster.add_light(ground, hx + 6, hy, 60, (255, 200, 120), 0.12)
    low = blank()
    sm = r03.bank(nfield(W, H, 30, 177, 3) * np.clip(1 - np.hypot(xx - 150, yy - 380) / 120, 0, 1)
                  * 1.6, 0.45, 0.2, [(40, 34, 32), (70, 62, 58), (104, 96, 90)], max_alpha=150)
    low.alpha_composite(r03.step_alpha(tone(sm, 8, 0), 5))
    air, casters = blank(), []
    for i, (x, y) in enumerate(((320, 176), (366, 160), (412, 182))):
        sp = unit("spore-bomber-a", i % 2)
        casters.append((sp, x, y))
        sprite.paste_center(air, sp, x, y)
    hsp = herald(DOWN, 0.3)
    casters.append((hsp, 120, 270))
    sprite.paste_center(air, hsp, 120, 270)
    for k in range(4):
        a = TAU * k / 4 + 0.4
        sp = unit("needler-a", k % 2)
        x, y = 120 + 52 * np.cos(a), 270 + 52 * np.sin(a)
        casters.append((sp, x, y))
        sprite.paste_center(air, sp, x, y)
    aimed_orbs(air, (366, 160), (230, 456), n=2)
    player(air, casters, 230, 456, 0.0)
    shadows.alpha_composite(r02.shadow_layer(casters, 21, 30, opacity=0.42))
    high = s6.wisp_tex(540, [90, 45], 179, DUST[4], thresh=0.66, max_a=70)
    frame = g.copy()
    for lay in (shadows, ground, low, air, high):
        frame.alpha_composite(lay)
    gl = g.copy()
    gl.alpha_composite(ground)
    layers = {"ground": gl, "low-air": low, "air": air, "high-air": high}
    notes = {"ground": "X1.0 DOMES, RINGS, RAIL, RING ROAD, GREENHOUSES, GUN PIT, HELIX COMPOUND",
             "low-air": "X1.3 SMOKE OVER THE BURNT GREENHOUSES",
             "air": "PLAY PLANE: SPORE BOMBERS ON A DOME, A CHOIR HERALD WITH NEEDLERS",
             "high-air": "X2 DUST WISPS UNDER 30 %"}
    text = ["L17 DOME SIEGE, SECTIONS 2-3: BRADBURY (TOP LEFT) AND LOWELL ON THEIR CONCRETE RINGS "
            "WITH GREENHOUSE ANNEXES (DOME A, SEE DOME-R36-A/B), THE RAIL LINE FROM THE NORTH, THE "
            "INNER RING ROAD, A MILITIA GUN PIT, BURNT OUTER GREENHOUSES UNDER THEIR SMOKE.",
            "VRELL NEST GROWTH (VIOLET WITH TEAL VEINS) CREEPS IN FROM THE LOWER LEFT; THE HELIX "
            "COMPOUND (LOWER RIGHT, SAME KIT AS THE VALLES STATION) STANDS IN A CLEAN, LIT PERIMETER "
            "WITH NO GROWTH WITHIN 100 PX: THE STORY HINT, NEVER A TARGET.",
            "UNITS: SPORE BOMBERS STEERING FOR LOWELL (THE HOOK), THE ROUND-36 CHOIR HERALD A WITH "
            "FOUR NEEDLERS IN ITS AURA."]
    return frame, layers, notes, text


def storm_still():
    sc = Storm("a")
    f = 44
    frame = sc.compose(f)
    L = sc.layer_images(f)
    layers = {"ground": L["ground"], "low-air": L["low-air"]}
    notes = {"ground": "X1.0 PLAINS, DRY CHANNELS, PIPELINE, ROVER WRECK, DUST DEVIL",
             "low-air": "X1.3 DUST BANKS"}
    text = ["L18 DUST STORM, SECTION 3 (DEEP STORM): THE NORTHERN PLAINS DIM INTO A RUST VEIL "
            "OUTSIDE 170 PX ROUND THE SHIP. INSIDE: DRY CHANNELS, THE HALF-BURIED PIPELINE, THE "
            "MILITIA ROVER WRECK WITH ITS RED BEACON (THE SECRET), A DUST DEVIL WITH ITS LIME CORE.",
            "OUTSIDE: THE GHOST DRONES (ROUND-36 PROPOSAL A) SHOW ONLY AS DARK SHAPES WITH THEIR "
            "WHITE LIGHTS AND RED EYE; VRELL KEEP THEIR GLOW. EVERY BULLET IS DRAWN. THIS IS LOOK A; "
            "STORM-RADIUS-R36-A/B COMPARE THE TWO EDGES IN MOTION."]
    panel = radius_panel(sc, f)
    return frame, layers, notes, text, panel


def foundry():
    """L19: Tharsis Foundry: slag fields with glowing cracks, two roofless conveyor halls
    (belts carry grey Warden Tanks up the screen; girders without lean), the furnace row with
    floor vents, the smelter tower's top, smoke on low-air."""
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    n = nfield(W, H, 60, 191, octaves=5)
    hgt = 0.4 + 0.35 * n
    col = shade_rgb(hgt, BASALT, strength=40, shadow=0.012)
    cr = (np.abs(nfield(W, H, 60, 193, octaves=3) - 0.5) < 0.008) & (nfield(W, H, 90, 197, 2) > 0.45)
    col[cr] = col[cr] * 0.45 + np.array((196, 74, 22)) * 0.55
    g = rgba(col)
    d = ImageDraw.Draw(g)
    halls = ((110, 70), (340, 70))          # (centre x, belt width)
    for hx, bw in halls:
        x0, x1 = hx - bw / 2, hx + bw / 2
        d.rectangle([x0 - 12, -2, x1 + 12, H + 2], fill=(96, 92, 88, 255))      # hall floor
        d.rectangle([x0, -2, x1, H + 2], fill=(36, 36, 40, 255))                # belt
        for y in range(-2, H, 9):
            d.line([x0 + 2, y, x1 - 2, y], fill=(58, 58, 64, 255), width=2)     # slats
        for y in range(0, H, 18):
            for x in (x0 - 4, x1 + 4):
                d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=(140, 136, 130, 255))  # rollers
        for x in (x0 - 12, x1 + 6):
            d.rectangle([x, -2, x + 6, H + 2], fill=(170, 164, 156, 255))         # low walls
            d.line([x, -2, x, H + 2], fill=(214, 208, 198, 255))
        for y in range(40, H, 30):                                               # up arrows
            d.polygon([(hx - 6, y + 6), (hx + 6, y + 6), (hx, y - 4)], fill=(170, 140, 40, 255))
    # furnace row and floor vents
    for i, x in enumerate((40, 220, 260, 450)):
        y = 70
        d.rectangle([x - 22, y - 16, x + 22, y + 16], fill=(70, 66, 64, 255))
        d.rectangle([x - 16, y - 10, x + 16, y + 10], fill=(255, 150, 40, 255))
        d.rectangle([x - 10, y - 5, x + 10, y + 5], fill=(255, 236, 170, 255))
    for x, y in ((225, 250), (232, 400), (440, 330)):
        d.ellipse([x - 15, y - 15, x + 15, y + 15], fill=(60, 56, 54, 255))
        d.ellipse([x - 11, y - 11, x + 11, y + 11], fill=(255, 128, 36, 255))
        for k in range(-8, 9, 4):
            d.line([x + k, y - 10, x + k, y + 10], fill=(70, 50, 40, 255), width=2)
    g = tone(g, 30, 3)
    for x, y in ((40, 70), (220, 70), (260, 70), (450, 70), (225, 250), (232, 400), (440, 330)):
        g = raster.add_light(g, x, y, 26, (255, 120, 30), 0.5)
    g = raster.add_light(g, 0, 0, 1, (0, 0, 0), 0)
    for cx, cy, rr in [(cx, cy, 1) for cx, cy in []]:
        pass
    ground, shadows = blank(), blank()
    # girders across the halls every 90 px (low structures, drawn without lean)
    gd = blank()
    dg = ImageDraw.Draw(gd)
    for hx, bw in halls:
        for y in range(30, H, 90):
            dg.rectangle([hx - bw / 2 - 12, y - 3, hx + bw / 2 + 12, y + 3], fill=(120, 116, 112, 255))
            dg.line([hx - bw / 2 - 12, y - 3, hx + bw / 2 + 12, y - 3], fill=(190, 186, 178, 255))
    # smelter tower top (round, at the top right)
    tower = sdf_sprite(lambda p: union((sd_cylinder_z(p, (0, 0, 0), 40, 14), 0),
                                       (sd_cylinder_z(p, (0, 0, 0), 26, 22), 1),
                                       (sd_cylinder_z(p, (0, 0, 22), 14, 2), 2)),
                       [Material(flat((92, 88, 84)), metal=0.5, shininess=60, spec=0.6,
                                 pattern=sdf.panel_lines(0.15, 0.05, 0.75)),
                        Material(flat((70, 66, 62)), metal=0.6, shininess=60, spec=0.6),
                        Material((0.1, 0.05, 0.02), emission=(1.8, 0.8, 0.2))], 96, 96, colors=32)
    place(ground, shadows, tower, 460, 200, "ground", opacity=0.6)
    for hx, ty, h_ in ((110, 300, UP), (110, 140, UP), (340, 420, UP)):
        place(ground, shadows, u36.g_hull.get(h_), hx, ty, "ground", opacity=0.6)
        sprite.paste_center(ground, u36.g_turret.get(np.arctan2(456 - ty, 240 - hx)), hx, ty)
    low = blank()
    sm = r03.bank(nfield(W, H, 30, 195, 3) * np.clip(1 - np.hypot((xx - 120) / 1.6, yy - 60) / 150, 0, 1)
                  * 1.5, 0.4, 0.2, [(30, 28, 28), (58, 54, 52), (92, 86, 82)], max_alpha=170)
    low.alpha_composite(r03.step_alpha(tone(sm, 8, 0), 5))
    air, casters = blank(), []
    for i, (x, y) in enumerate(((300, 120), (330, 96), (360, 120))):
        sp = unit("needler-a", i % 2)
        casters.append((sp, x, y))
        sprite.paste_center(air, sp, x, y)
    aimed_orbs(air, (110, 300), (240, 456), n=3, ring=GOLD[3], r=4)
    player(air, casters, 240, 456, 0.0)
    shadows.alpha_composite(r02.shadow_layer(casters, 21, 30, opacity=0.42))
    frame = g.copy()
    frame.alpha_composite(r02.masked(r02.shadow_layer([(gd, W / 2, H / 2)], 6, 8, 0.5), g))
    frame.alpha_composite(gd)
    for lay in (shadows, ground, low, air):
        frame.alpha_composite(lay)
    gl = g.copy()
    gl.alpha_composite(gd)
    gl.alpha_composite(ground)
    layers = {"ground": gl, "low-air": low, "air": air}
    notes = {"ground": "X1.0 SLAG, HALLS, BELTS (UP 40 PX/S), FURNACES, VENTS, TOWER, TANKS",
             "low-air": "X1.3 FURNACE SMOKE (HEAT SHIMMER IN MOTION)",
             "air": "PLAY PLANE"}
    text = ["L19 FOUNDRY OF THARSIS, SECTIONS 2-3: SLAG FIELDS OF DARK BASALT WITH GLOWING CRACKS; "
            "TWO ROOFLESS CONVEYOR HALLS WHOSE BELTS CARRY THE GREY WARDEN TANKS UP THE SCREEN "
            "(AMBER ARROWS, ROLLERS, LOW CONCRETE WALLS; THE GIRDERS ARE LOW STRUCTURES DRAWN "
            "WITHOUT LEAN, SO THE TANKS STAY READABLE UNDER THEM).",
            "THE FURNACE ROW ACROSS THE TOP AND FLOOR VENTS GLOW WHITE-ORANGE (THE VENT HAZARD); "
            "THE SMELTER TOWER'S TOP AT THE RIGHT EDGE. UNITS: THE ROUND-36 WARDEN TANK IN "
            "UNMARKED GREY (GOLD-RIMMED SHOTS ARE A STAND-IN), ROUND 04 NEEDLERS."]
    return frame, layers, notes, text


def hellas():
    """L20/21: the Hellas basin: terraces of the basin wall (top), the pale dune sea, the
    travelling ridge of something under the sand with its crest cracking lime, the lime hollow
    at the bottom glowing from below."""
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    n = nfield(W, H, 90, 201, octaves=5)
    slope = np.clip((175 - yy + 40 * n) / 175, 0, 1)
    steps = slope * 5
    terr = (np.floor(steps) + sdf.smoothstep(0.0, 0.12, steps % 1)) / 5
    rip = np.sin((xx * 0.35 + yy + n * 70) / 7.0) * 0.012
    hgt = 0.3 + 0.12 * n + 0.42 * terr + rip * (1 - slope)
    # the travelling ridge: a raised hump along a sinuous path
    path_x = 300 - 0.55 * (yy - 120) + 40 * np.sin(yy / 70)
    dr = (xx - path_x) / 30
    ridge = (np.exp(-dr ** 2) * sdf.smoothstep(150, 210, yy) * sdf.smoothstep(410, 350, yy))
    hgt += 0.3 * ridge
    # the lime hollow
    dh = np.hypot((xx - 250) / 1.4, yy - 560) / 140
    bowl = np.clip(1 - dh, 0, 1)
    hgt -= 0.35 * bowl ** 1.2
    gy, gx = np.gradient(hgt)
    sh = np.clip(1 - (gx + gy) * 70, 0.4, 1.6)
    tt = np.clip(0.45 + 0.35 * n - 0.25 * terr * (slope > 0.02) + 0.1 * ridge, 0, 1)
    col = raster.ramp([(i / 5, c) for i, c in enumerate(OCHRE)], tt) * sh[..., None]
    col *= (0.6 + 0.4 * cast_np(hgt, 30, 0.004))[..., None]
    col *= (1 - 0.45 * bowl)[..., None]
    g = tone(rgba(col), 28, 3)
    crack = (np.abs(nfield(W, H, 30, 203, 3) - 0.5) < 0.02) & (ridge > 0.75)
    hc = (np.abs(nfield(W, H, 30, 205, 3) - 0.5) < 0.008 + 0.02 * bowl) & (bowl > 0.1)
    cl = np.zeros((H, W, 4))
    cl[crack | hc, :3] = LIME
    cl[crack | hc, 3] = 220
    cl[hc & (bowl < 0.3), 3] = 140
    g.alpha_composite(raster.glow(Image.fromarray(cl.astype(np.uint8), "RGBA"), 2, 0.6))
    g = raster.add_light(g, 250, 560, 120, (90, 160, 20), 0.3)
    shadows, low = blank(), blank()
    hz = s6.wisp_tex(540, [90, 45], 207, OCHRE[4], thresh=0.5, gain=2.0, max_a=90, stretch=3)
    low.alpha_composite(hz)
    air, casters = blank(), []
    for i in range(9):
        a = TAU * i / 9
        x, y = 130 + 30 * np.cos(a), 150 + 22 * np.sin(a)
        sp = unit("skitter-a", [-1.0, 0.0, 1.0][i % 3])
        casters.append((sp, x, y))
        sprite.paste_center(air, sp, x, y)
    sp = unit("mantis-a")
    casters.append((sp, 400, 120))
    sprite.paste_center(air, sp, 400, 120)
    aimed_orbs(air, (400, 120), (230, 440), n=3)
    player(air, casters, 230, 440, 0.0)
    shadows.alpha_composite(r02.shadow_layer(casters, 21, 30, opacity=0.42))
    frame = g.copy()
    for lay in (shadows, low, air):
        frame.alpha_composite(lay)
    layers = {"ground": g, "low-air": low, "air": air}
    notes = {"ground": "X1.0 TERRACES, DUNE SEA, THE RIDGE, THE LIME HOLLOW",
             "low-air": "X1.3 OCHRE DUST HAZE, THICKER DOWN THE BASIN",
             "air": "PLAY PLANE"}
    text = ["L20 RETREAT FROM HELLAS / L21 DUST COLOSSUS: THE BASIN FLOOR IS PALER OCHRE THAN THE "
            "REST OF MARS. THE BASIN WALL STEPS DOWN IN TERRACES AT THE TOP (L20'S RETREAT CLIMBS "
            "THEM); BELOW, THE DUNE SEA WITH WIND RIPPLES.",
            "THE SIGNATURE: THE TRAVELLING RIDGE - A LONG HUMP OF SAND PUSHED UP BY SOMETHING "
            "MOVING UNDERNEATH, ITS CREST CRACKING LIME, SAND SLIDING OFF ITS FLANKS (IN MOTION IT "
            "TRAVELS ACROSS THE FIELD). AT THE BOTTOM THE LIME HOLLOW, THE BASIN'S LOWEST POINT, "
            "GLOWS LIME FROM BELOW THROUGH ITS CRACKS: WHERE THE DUST COLOSSUS RISES."]
    return frame, layers, notes, text


# =========================================================================== panorama

def panorama(w=960, h=540):
    """A wide still with a raised camera (voxel-space rendering, the late-90s terrain trick)
    over a pre-shaded height map: Ares Landing's domes in the foreground, a canyon across the
    middle distance, Olympus Mons on the horizon at the left, a dust-storm front at the right."""
    U = 4.0                                           # world units per map texel
    MW = 2048
    x0, y0 = -4096.0, -7680.0                         # map origin (world), y grows toward camera
    gy_, gx_ = np.mgrid[0:MW, 0:MW].astype(np.float64)
    X, Y = x0 + gx_ * U, y0 + gy_ * U
    n = raster.fbm(MW, MW, 256, 211, octaves=7, period=False)
    n2 = raster.fbm(MW, MW, 64, 212, octaves=4, period=False)
    hgt = n * 70 + n2 * 14
    ox, oy = -1700.0, -5200.0                         # Olympus Mons
    r = np.hypot(X - ox, Y - oy)
    hgt += 950 * np.clip(1 - r / 2600, 0, 1) ** 1.4 + 120 * sdf.smoothstep(2700, 2600, r)
    hgt -= 90 * sdf.smoothstep(330, 300, r)
    cyl = -760 + 80 * np.sin(X / 300)                 # the canyon
    dd = np.abs(Y - cyl)
    cut = sdf.smoothstep(150, 95, dd + 20 * n2)
    hgt -= 230 * cut
    gy, gx = np.gradient(hgt)
    sh = np.clip(1 + (-gx * 0.9 - gy * 0.5) * 0.06, 0.45, 1.6)    # sun from the left, low
    t = np.clip(0.3 + 0.55 * n + 0.2 * n2, 0, 1)
    col = raster.ramp([(0, RUST[1]), (0.35, RUST[2]), (0.65, RUST[3]), (1, RUST[4])], t)
    strata = 0.85 + 0.15 * np.sin(hgt / 9)
    col = np.where(cut[..., None] > 0.3, col * strata[..., None] * 0.9, col)
    vol = np.clip(1 - r / 2600, 0, 1)
    col = col * (1 - 0.4 * vol[..., None]) + np.array(BASALT[3]) * 0.4 * vol[..., None]
    col *= sh[..., None]
    domes = [(-200, -420, 40), (60, -520, 30), (250, -400, 34), (-420, -600, 24), (430, -640, 24)]
    for dx, dy, rr in domes:
        q = np.hypot(X - dx, Y - dy)
        m = q < rr
        if not m.any():
            continue
        base_h = hgt[m].min()
        cap = np.sqrt(np.clip(rr ** 2 - q ** 2, 0, None))
        nz = cap / rr
        nxv = (X - dx) / rr
        lit = np.clip(0.7 + 0.5 * (-nxv * 0.7 + nz * 0.5), 0.7, 1.35)
        strut = (np.abs(((np.arctan2(Y - dy, X - dx) * 6 / np.pi) % 1) - 0.5) > 0.44) | \
            (np.abs((nz * 4) % 1 - 0.5) > 0.42)
        dc = np.array([70, 210, 120])[None, None, :] * lit[..., None] + 30 * (lit[..., None] > 1.1)
        dc = np.where(strut[..., None], np.array([210, 214, 206]) * lit[..., None], dc)
        hgt = np.where(m, base_h + cap, hgt)
        col = np.where(m[..., None], dc, col)
        ring = (q >= rr) & (q < rr + 3)
        col = np.where(ring[..., None], np.array([140, 128, 116]), col)
    cam_x, cam_y, cam_h, horizon, scale = 0.0, 0.0, 300.0, 150, 340.0
    yy = np.arange(h)[:, None] / h
    sky = raster.ramp([(0, (140, 104, 94)), (0.18, (188, 140, 104)), (0.3, SKY), (1, SKY)],
                      np.broadcast_to(yy, (h, w))).copy()
    sx, sy = 190, 52
    dsun = np.hypot(np.arange(w)[None, :] - sx, np.arange(h)[:, None] - sy)
    sky += (np.exp(-(dsun / 26) ** 2) * 40)[..., None]
    img = sky.copy()
    ybuf = np.full(w, h, dtype=np.int64)
    ang = (np.arange(w) - w / 2) / (w / 2) * 0.6
    rows = np.arange(h)[:, None]
    haze = np.array(SKY, float)
    z = 20.0
    while z < 7600:
        x = cam_x + np.tan(ang) * z
        y = np.full(w, cam_y - z)
        ix = np.clip(((x - x0) / U).astype(int), 0, MW - 1)
        iy = np.clip(((y - y0) / U).astype(int), 0, MW - 1)
        hh = hgt[iy, ix]
        sy_ = np.clip((horizon + (cam_h - hh) / z * scale).astype(np.int64), 0, h)
        upd = sy_ < ybuf
        if upd.any():
            c = col[iy, ix]
            fog = min(1.0, (z / 9500) ** 1.1)
            c = c * (1 - fog) + haze * fog
            lo, hi = int(sy_[upd].min()), int(ybuf[upd].max())
            sub = rows[lo:hi]
            mask = (sub >= sy_[None, :]) & (sub < ybuf[None, :])
            blk = img[lo:hi]
            blk[mask] = np.broadcast_to(c[None, :, :], (hi - lo, w, 3))[mask]
            ybuf = np.minimum(ybuf, sy_)
        z += max(1.0, z * 0.006)
    # dust-storm front at the right horizon
    sn = raster.fbm(w, h, 60, 213, octaves=4, period=False)
    xs = np.arange(w)[None, :]
    top = 40 + 70 * sn + (w - xs) * 0.12
    front = sdf.smoothstep(600, 820, xs + 90 * sn) * sdf.smoothstep(top - 10, top + 30,
                                                                    np.arange(h)[:, None] * 1.0)
    front *= sdf.smoothstep(horizon + 90, horizon + 20, np.arange(h)[:, None] * 1.0)
    fc = raster.ramp([(0, (120, 66, 40)), (1, (176, 112, 70))], sn)
    img = img * (1 - 0.85 * front[..., None]) + fc * 0.85 * front[..., None]
    out = posterize(rgba(img), 96)
    ImageDraw.Draw(out).ellipse([sx - 7, sy - 7, sx + 7, sy + 7], fill=(250, 246, 236, 255))
    return out


def panorama_sheet():
    pano = panorama()
    img = raster.sheet(960 + 32, 540 + 120, "MARS - A WIDE STILL (R36): THE ACT IN ONE VIEW", SUB)
    img.alpha_composite(pano, (16, 38))
    text = ["A RAISED CAMERA OVER THE PLAINS EAST OF THE CANYON (A MOOD PIECE, NOT A MAP): ARES LANDING'S "
            "GREEN-LIT DOMES IN THE FOREGROUND, A CANYON CUT ACROSS THE MIDDLE DISTANCE, OLYMPUS MONS' "
            "SHIELD ON THE LEFT HORIZON, A DUST-STORM FRONT ROLLING IN FROM THE RIGHT, THE SMALL PALE "
            "SUN TOP LEFT IN THE BUTTERSCOTCH SKY.",
            "VOXEL-SPACE TERRAIN (THE LATE-90S FLIGHT-SIM TRICK), 96 COLOURS. A CANDIDATE LOOK FOR THE "
            "ACT 3 TITLE CARD AND THE BRIEFING IMAGES' HORIZONS."]
    y = 540 + 48
    for line in text:
        for part in wrap(line, 150):
            raster.draw_text(img, 16, y, part, raster.LABEL)
            y += 11
        y += 4
    return img


# =========================================================================== dome sheets

DOME_TEXT = {
    "a": ["DOME A - GEODESIC GLASS: A GLASS HEMISPHERE ON WHITE STRUTS (A TRIANGLE LATTICE), CROP "
          "ROWS GLOWING GREEN INSIDE, ON A CONCRETE RING WITH FOUR AIRLOCKS AND TWO GREENHOUSE "
          "ANNEXES. READS AS 'GLASS TO PROTECT' AT ONCE; THE GREEN GLOW IS THE BRIGHTEST THING ON "
          "THE GROUND (IT MUST STAY BELOW THE BULLETS' WHITE CORES).",
          "STATES (THE ALLY): HIT = AMBER CRACKS ROUND THE IMPACT, A FEW DARK PANES; BROKEN = THE "
          "GLASS GONE, THE INSIDE DARK AND DEAD, SHARDS ROUND THE RING."],
    "b": ["DOME B - RIBBED PRESSURE DOME: A LOWER WHITE COMPOSITE SHELL WITH TWELVE RIBS, A GREEN "
          "GLAZED BAND AND SKYLIGHT, BANKED WITH A REGOLITH BERM (RADIATION SHIELDING), TWO AIRLOCK "
          "TUBES. READS MORE AS A HARD COLONY HABITAT; LESS GLOW, SO HITS AND CRACKS SHOW BETTER.",
          "STATES (THE ALLY): HIT = AMBER CRACKS ON THE SHELL, A DARKENED SECTOR; BROKEN = THE SHELL "
          "BREACHED, THE GLAZING DEAD, DEBRIS ON THE BERM."],
}


def dome_sheet(v):
    img = raster.sheet(1140, 690, f"DOME {v.upper()} (R36): ARES LANDING DOMES - THE NEW ALLY", SUB)
    # field: Bradbury and Lowell over rust plain with a hooked bomber
    col, _ = plains(171 + (v == "b"), RUST[1:])
    fld = tone(rgba(col), 22, 3)
    shadows, ground = blank(), blank()
    for x, y in ((130, 150), (350, 330)):
        dd = ImageDraw.Draw(fld)
        dd.ellipse([x - 70, y - 70, x + 70, y + 70], fill=(116, 100, 88, 255))
        dd.ellipse([x - 66, y - 66, x + 66, y + 66], fill=(132, 116, 102, 255))
        place(ground, shadows, dome_sprite(v), x, y, "ground", opacity=0.55)
    air, casters = blank(), []
    sp = unit("spore-bomber-a")
    casters.append((sp, 350, 300))
    sprite.paste_center(air, sp, 350, 300)
    player(air, casters, 240, 470, 0.0)
    shadows.alpha_composite(r02.shadow_layer(casters, 21, 30, opacity=0.42))
    fr = fld.copy()
    for lay in (shadows, ground, air):
        fr.alpha_composite(lay)
    raster.draw_text(img, 16, 38, "PLAY FIELD 480X540 AT 1X: BRADBURY AND LOWELL, A HOOKED BOMBER "
                     "OVER LOWELL", raster.LABEL_DIM)
    img.alpha_composite(fr, (16, 50))
    x0 = 516
    raster.draw_text(img, x0, 38, "120 PX DOME, 2X", raster.ACCENT)
    big = sprite.enlarge(dome_sprite(v, annex=False), 2)
    cell = r02.checker(260, 260)
    sprite.paste_center(cell, big, 130, 130)
    img.alpha_composite(cell, (x0, 50))
    raster.draw_text(img, x0 + 280, 38, "EDEN, 144 PX (1X)", raster.ACCENT)
    cell = r02.checker(200, 200)
    sprite.paste_center(cell, dome_sprite(v, 144), 100, 100)
    img.alpha_composite(cell, (x0 + 280, 50))
    raster.draw_text(img, x0, 322, "STATES AT 1X: INTACT, HIT, BROKEN", raster.ACCENT)
    base = dome_sprite(v, annex=False)
    for i, st in enumerate(("intact", "hit", "broken")):
        cell = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
        cell.alpha_composite(tone(rgba(plains(300 + i, RUST[1:], 160, 160)[0]), 12, 3))
        sprite.paste_center(cell, damaged(base, v, st), 80, 80)
        img.alpha_composite(cell, (x0 + i * 172, 334))
        raster.draw_text(img, x0 + i * 172, 498, st.upper(), raster.LABEL_DIM)
    y = 520
    for line in DOME_TEXT[v] + ["FOOTPRINT 120X120 (EDEN 144), HIT BOX LESS 16 PX A SIDE; ON "
                                "GROUND, UNDER THE PLAY PLANE. HUD PIPS: GREEN, AMBER AFTER A "
                                "HIT, DARK WHEN BROKEN."]:
        for part in wrap(line, 102):
            raster.draw_text(img, x0, y, part, raster.LABEL)
            y += 11
        y += 5
    return img


# =========================================================================== a/b sheets

CANYON_TEXT = {
    "a": ["CANYON WALLS A - THE RIM IS THE COLLISION LINE (THE DECIDED ART-DIRECTION RULE, L16'S "
          "PROPOSAL): THE PLATEAU AND THE RIM SCROLL AT X1.0 WITH THE GROUND; THE FACES DROP IN "
          "TRUE PERSPECTIVE TO THE FLOOR AT X0.6, SO THE SHIP FLIES OVER A DEEP CANYON. THE FLOOR "
          "IS FAR (NOTHING ON IT IS HIT OR HITS); ANCHORS AND TURRETS SIT ON THE RIM AND PLATEAU.",
          "READS: THE GAP IS EXACTLY WHAT THE EYE SEES AS THE CANYON'S EDGE (THE WHITE LINE ON THE "
          "OVERLAY); THE PLATEAU IS SOLID GROUND. THE WALLS TURN AS THEY SCROLL (DEPTH)."],
    "b": ["CANYON WALLS B - THE WALL'S FOOT IS THE COLLISION LINE (LIKE LEVEL 08'S TOWERS): THE "
          "FLOOR SCROLLS AT X1.0 AND CARRIES THE GROUND UNITS; THE WALLS RISE TOWARD THE CAMERA TO "
          "THE PLATEAU AT X1.25, LEANING OUTWARD FROM THE CENTRE. THE SHIP FLIES DOWN IN THE "
          "CANYON. BY THE TOWER RULE NOTHING GAMEPLAY TOUCHES STANDS ON THE LEANING WALL OR THE "
          "PLATEAU: ANCHORS SIT AT THE FOOT.",
          "READS: MORE DRAMATIC AND CLOSER (THE PLATEAU RACES PAST AT 1.25X), BUT THE VISIBLE FACE "
          "LIES OVER THE NO-FLY ZONE, SO THE SOLID EDGE IS THE INNER LINE OF THE FACE, NOT THE RIM."],
}
CANYON_RULE = ("RULE (ART DIRECTION, PERSPECTIVE BETWEEN LAYERS): CANYON WALLS RUN FROM THE RIM AT "
               "SCALE 1.0 DOWN TO THE FLOOR AT THE FAR SCALE (MODEL A); MODEL B WOULD AMEND IT. "
               "WALL CONTACT (L16 DRAFT): 10 ARMOUR, 28 PX PUSH, 0.5 S; NARROWEST 180 PX.")


def canyon_sheet(v, sc, f=26):
    img = raster.sheet(1140, 690, f"CANYON WALLS {v.upper()} (R36): L16 CAMERA MODEL", SUB)
    raster.draw_text(img, 16, 38, f"PLAY FIELD 480X540 AT 1X - SCROLL 160 PX/S AT X1.0",
                     raster.LABEL_DIM)
    img.alpha_composite(sc.compose(f), (16, 50))
    raster.draw_text(img, 516, 38, "COLLISION OVERLAY (0.5X): WHITE = COLLISION LINE, RED = NO-FLY",
                     raster.LABEL_DIM)
    ov = sc.collision_overlay(f).resize((240, 270), Image.BOX)
    img.alpha_composite(ov, (516, 50))
    ov2 = sc.compose(f + 20).resize((240, 270), Image.BOX)
    img.alpha_composite(ov2, (516 + 252, 50))
    raster.draw_text(img, 516 + 252, 326, "1 S LATER: THE WALLS TURN", raster.LABEL_DIM)
    raster.draw_text(img, 516, 340, "SIDE VIEW", raster.ACCENT)
    img.alpha_composite(canyon_diagram(v), (516, 352))
    y = 512
    for line in CANYON_TEXT[v] + [CANYON_RULE]:
        for part in wrap(line, 102):
            raster.draw_text(img, 516, y, part, raster.LABEL)
            y += 11
        y += 4
    return img


RADIUS_TEXT = {
    "a": ["STORM RADIUS A - CLEAR BUBBLE: A HARD 10 PX EDGE AT 170 PX ROUND THE SHIP. INSIDE THE "
          "GROUND IS FULLY DRAWN; OUTSIDE A MOVING RUST VEIL. CLEAR TO READ: WHAT IS IN THE BUBBLE "
          "IS SEEN, WHAT IS OUT IS A SHAPE. CHEAP (ONE MASK). RISK: LOOKS LIKE A SPOTLIGHT/TUNNEL "
          "RATHER THAN DUST."],
    "b": ["STORM RADIUS B - SOFT FALLOFF: VISIBILITY FADES OVER ABOUT 120 PX AROUND 170 PX, THE EDGE "
          "BROKEN BY DRIFTING DUST TONGUES; UNITS FADE GRADUALLY TO SILHOUETTES. READS MORE AS WEATHER; "
          "THE EXACT REACH IS LESS CLEAR, AND A UNIT IN THE FADE IS HALF-SEEN."],
}


def radius_sheet(v, sc, f=44):
    frame = sc.compose(f)
    L = sc.layer_images(f)
    text = RADIUS_TEXT[v] + ["BOTH: AIR UNITS OUTSIDE ARE DARK SILHOUETTES THAT KEEP THEIR GLOW "
                             "(VRELL TEAL/LIME, THE GHOST DRONE'S WHITE LIGHTS AND RED EYE); "
                             "BULLETS, THE SHIP AND ITS SHOTS ARE ALWAYS DRAWN (M6 = A: EVERYTHING "
                             "STAYS HITTABLE). THE STREAKS ON HIGH-AIR STAY UNDER 40 %."]
    notes = {"ground": "X1.0 PLAINS, PIPELINE, ROVER, DEVIL", "low-air": "X1.3 DUST BANKS"}
    return field_sheet(f"STORM RADIUS {v.upper()} (R36): L18 VISIBILITY", frame,
                       {"ground": L["ground"], "low-air": L["low-air"]}, notes, text,
                       extra=(radius_panel(sc, f), "THE LOOK'S RULES"))


# =========================================================================== main

# the user's picks (round 36's review, 2026-10-13): the unchosen a/b looks are written to
# concept/rejected/; the single-variant stills (not in CHOSEN) stay in concept/
CHOSEN = {"canyon-walls": "b", "storm-radius": "b", "dome": "a"}


def dest(name):
    subject, variant = name.split(".")[0].rsplit("-r36-", 1)
    folder = OUT if CHOSEN.get(subject, variant) == variant else OUT / "rejected"
    folder.mkdir(parents=True, exist_ok=True)
    return folder / name


def save(img, name):
    p = dest(name)
    img.convert("RGB").save(p, optimize=True)
    print("wrote", p.relative_to(ROOT))


def save_gif(scene, name, colors=128):
    p = dest(name)
    cols = make_gif(scene, p, colors)
    print("wrote", p.relative_to(ROOT), f"{p.stat().st_size / 1e6:.1f} MB, {cols} colours")


TITLES = {"olympus": "MARS - OLYMPUS DESCENT (R36): THROUGH THE CLOUD DECKS",
          "valles": "MARS - VALLES MARINERIS (R36): THE HELIX STATION ON THE RIM",
          "ares": "MARS - ARES LANDING (R36): THE DOMES",
          "storm": "MARS - STORM PLAINS (R36): INSIDE THE DUST STORM",
          "foundry": "MARS - THARSIS FOUNDRY (R36): CONVEYORS AND FURNACES",
          "hellas": "MARS - HELLAS BASIN (R36): THE RIDGE AND THE LIME HOLLOW"}
FILES = {"olympus": "mars-olympus", "valles": "mars-valles", "ares": "mars-ares-landing",
         "storm": "mars-storm-plains", "foundry": "mars-tharsis-foundry", "hellas": "mars-hellas"}


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    gifs = "--no-gif" not in sys.argv
    everything = ["olympus", "valles", "ares", "storm", "foundry", "hellas", "panorama", "canyon",
                  "radius", "dome"]
    for what in args or everything:
        if what in FILES:
            fn = {"olympus": olympus, "valles": valles, "ares": ares, "storm": storm_still,
                  "foundry": foundry, "hellas": hellas}[what]
            res = fn()
            extra = (res[4], "THE LOOK'S RULES") if len(res) > 4 else None
            frame, layers, notes, text = res[:4]
            save(field_sheet(TITLES[what], frame, layers, notes, text, extra=extra),
                 f"{FILES[what]}-r36-a.png")
        elif what == "panorama":
            save(panorama_sheet(), "mars-panorama-r36-a.png")
        elif what == "canyon":
            for v in "ab":
                sc = Canyon(v, helix=True)
                save(canyon_sheet(v, sc), f"canyon-walls-r36-{v}.png")
                if gifs:
                    save_gif(sc, f"canyon-walls-r36-{v}.gif")
        elif what == "radius":
            for v in "ab":
                sc = Storm(v)
                save(radius_sheet(v, sc), f"storm-radius-r36-{v}.png")
                if gifs:
                    save_gif(sc, f"storm-radius-r36-{v}.gif")
        elif what == "dome":
            for v in "ab":
                save(dome_sheet(v), f"dome-r36-{v}.png")


if __name__ == "__main__":
    main()
