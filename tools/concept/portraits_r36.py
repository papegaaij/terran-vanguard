#!/usr/bin/env python3
"""Concept round 36 - the colonial generic radio portrait (Level 15's proposal
`radio-generic-colonial`), the speaker portrait of Ares Control, two variants.

Outputs (the chosen b of round 36's review in concept/, the rejected a in concept/rejected/):
  design/story/characters/concept/rejected/generic-colonial-r36-a.png   a: the old controller - grey
                                                                stubble, dust-orange militia field
                                                                cap, single-cup headset and boom mic,
                                                                dust scarf, rust work jacket
  design/story/characters/concept/generic-colonial-r36-b.png   b: the dust-suit controller - cropped
                                                                grey hair, dust goggles pushed up,
                                                                respirator hanging at the throat,
                                                                padded suit collar with a seal ring

Same SDF bust + style-B pipeline as the other radio speakers (tools/concept/portraits_r03.py busts,
tools/concept/portraits_r04.py style B with retained colour, tools/concept/portraits_r08.py's
generic-speaker frames): the channel stays the friendly teal radio feed, the colonist is told apart
from the CDF officer (navy beret, grey-green jacket) by his Mars dust-orange and rust clothes, his age
and his silhouette. Each sheet shows the 72x72 HUD radio portrait in the HUD A frame at 1x, the
portrait at 4x, the frame at 2x, and a 1x line-up next to the game's generic CDF and civilian radio
portraits (assets/sprites/portraits/) and the other variant.

Run: python3 tools/concept/portraits_r36.py [a] [b]   (~25 s)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import portraits_r03 as r03  # noqa: E402
import portraits_r04 as r04  # noqa: E402
import portraits_r08 as r08  # noqa: E402  (registers the generic-speaker frames)
import hud_r02  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.sdf import Material  # noqa: E402

ROUND = "CONCEPT ROUND 36"
SKIN, EYE, IRIS, LIPS, HAIR, CLOTH, ACCENT, METAL, GLOW, EXTRA, BROW = range(11)
PORTRAITS = ROOT / "assets" / "sprites" / "portraits"
LINE = "FIVE WEEKS ALONE. NICE OF EARTH TO DROP BY."


def _stubble(q, y_top=0.36):
    """Close grey stubble over the jaw and upper lip, kept off the lips and cheeks."""
    beard = sdf.sd_ellipsoid(q, (0, 0.24, 0.06), (0.235, 0.17, 0.26))
    beard = np.maximum(beard, q[:, 1] - y_top + 0.25 * np.maximum(np.abs(q[:, 0]) - 0.12, 0))
    beard = np.maximum(beard, -sdf.sd_ellipsoid(q, (0, 0.27, 0.3), (0.07, 0.024, 0.06)))
    return beard


def colonial_a():
    """Variant a, the old controller: weathered tan skin, grey stubble and grey temples under a
    dust-orange militia field cap with a short peak, a single-cup headset with a boom mic, a rust
    work jacket with a dust scarf wound at the throat and an ochre colony patch."""
    mats = [
        Material((0.62, 0.42, 0.3), shininess=14, spec=0.18),            # weathered skin
        Material((0.82, 0.8, 0.74), shininess=60, spec=0.5),             # eye white
        Material((0.14, 0.12, 0.1), shininess=90, spec=0.9),             # iris
        Material((0.46, 0.28, 0.24), shininess=24, spec=0.25),           # lips
        Material((0.7, 0.69, 0.66), shininess=8, spec=0.08, pattern=r03.noise_pattern(240, 0.45, 2)),
        Material((0.6, 0.28, 0.12), shininess=10, spec=0.12, pattern=r03.noise_pattern(80, 0.14, 4)),
        Material((0.95, 0.5, 0.16), shininess=18, spec=0.25),            # dust-orange cap / patch
        Material((0.2, 0.19, 0.18), metal=0.4, shininess=50, spec=0.5),  # worn headset
        Material((0.1, 0.1, 0.1), emission=(1.0, 0.3, 0.12)),            # on-air LED
        Material((0.72, 0.55, 0.38), shininess=6, spec=0.05, pattern=r03.noise_pattern(130, 0.2, 9)),
        Material((0.5, 0.48, 0.45), shininess=8, spec=0.08),             # grey brows
    ]

    def scene(p):
        q = r03.turn(p, -0.26, 0.05)      # turned the other way from the CDF officer, chin down
        items, skin = r03.face(q, jaw=1.1)
        beard = _stubble(q)
        hair = sdf.sd_ellipsoid(q, (0, 0.49, -0.06), (0.305, 0.34, 0.33))
        hair = np.maximum(hair, -(q[:, 1] - 0.5 + 0.5 * np.maximum(q[:, 2], 0)))
        hair = sdf.smin(hair, np.minimum(*[sdf.sd_capsule(q, (s * 0.27, 0.6, 0.05), (s * 0.255, 0.4, 0.1), 0.045)
                                         for s in (-1, 1)]), 0.03)       # grey temples / sideburns
        cap = sdf.sd_ellipsoid(q, (0, 0.66, -0.04), (0.33, 0.2, 0.35))
        cap = np.maximum(cap, -(q[:, 1] - 0.6))                         # flat-ish band line
        cap = sdf.smin(cap, sdf.sd_box(q, (0, 0.8, -0.04), (0.24, 0.05, 0.25), 0.05), 0.04)
        peak = sdf.sd_ellipsoid(q, (0, 0.61, 0.3), (0.25, 0.02, 0.13))
        peak = np.maximum(peak, -(q[:, 2] - 0.2))
        patch = sdf.sd_box(q, (0, 0.71, 0.33), (0.05, 0.035, 0.015), 0.01)
        cup = sdf.sd_cylinder_x(q, (0.315, 0.44, -0.02), 0.075, 0.035)
        band = sdf.sd_capsule(q, (0.31, 0.5, -0.03), (0.3, 0.78, -0.04), 0.018)
        boom = sdf.sd_capsule(q, (0.32, 0.40, 0.03), (0.13, 0.28, 0.28), 0.013)
        mic = sdf.sd_sphere(q, (0.12, 0.275, 0.285), 0.026)
        led = sdf.sd_sphere(q, (0.335, 0.41, 0.05), 0.013)
        pb = r03.turn(p, -0.1)
        neck, torso = r03.body(pb, shoulders=0.86)
        scarf = sdf.smin(sdf.sd_cylinder_y(pb, (0, 0.0, -0.04), 0.205, 0.08),
                         sdf.sd_ellipsoid(pb, (-0.06, -0.1, 0.13), (0.2, 0.08, 0.1)), 0.05)
        tail = sdf.sd_capsule(pb, (-0.1, -0.12, 0.2), (-0.16, -0.42, 0.24), 0.05, 0.035)
        lapel = sdf.sd_capsule(pb, (0.22, -0.08, 0.16), (0.08, -0.5, 0.26), 0.05)
        colony = sdf.sd_box(pb, (0.48, -0.36, 0.2), (0.08, 0.08, 0.02), 0.012)
        return sdf.union(*items, (beard - 0.009, BROW), (hair, HAIR), (cap, ACCENT),
                         (peak, ACCENT), (patch, CLOTH),
                         (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH), (lapel, CLOTH),
                         (sdf.smin(scarf, tail, 0.04), EXTRA), (colony, ACCENT),
                         (band, METAL), (cup, METAL), (boom, METAL), (mic, METAL), (led, GLOW))
    return scene, mats


def colonial_b():
    """Variant b, the dust-suit controller: cropped grey hair and a heavy grey moustache, dust
    goggles with amber lenses pushed up on the forehead, a respirator hanging at the throat from
    one strap, the padded dust-orange collar of an environment suit with a metal seal ring, and a
    headset band with a stubby mic."""
    mats = [
        Material((0.6, 0.42, 0.3), shininess=14, spec=0.18),             # weathered skin
        Material((0.82, 0.8, 0.74), shininess=60, spec=0.5),
        Material((0.1, 0.08, 0.06), shininess=90, spec=0.9),
        Material((0.4, 0.24, 0.2), shininess=24, spec=0.25),
        Material((0.36, 0.33, 0.3), shininess=6, spec=0.05, pattern=r03.noise_pattern(260, 0.6, 6)),
        Material((0.85, 0.42, 0.14), shininess=12, spec=0.15, pattern=r03.noise_pattern(70, 0.12, 3)),
        Material((0.75, 0.42, 0.1), metal=0.2, shininess=90, spec=0.7),  # amber goggle lenses
        Material((0.3, 0.3, 0.3), metal=0.7, shininess=60, spec=0.7),    # seal ring / buckles
        Material((0.1, 0.1, 0.1), emission=(1.0, 0.3, 0.12)),            # on-air LED
        Material((0.3, 0.31, 0.29), metal=0.2, shininess=30, spec=0.35),  # rubber: straps, mask
        Material((0.72, 0.7, 0.66), shininess=8, spec=0.08),             # grey brows / moustache
    ]

    def scene(p):
        q = r03.turn(p, -0.2, 0.02)
        items, skin = r03.face(q, jaw=1.12)
        hair = sdf.sd_ellipsoid(q, (0, 0.5, -0.06), (0.3, 0.345, 0.325))
        hair = np.maximum(hair, -(q[:, 1] - 0.62 + 0.3 * np.maximum(q[:, 2], 0)))
        moust = sdf.sd_capsule(q, (-0.095, 0.305, 0.3), (0.095, 0.305, 0.3), 0.03)
        moust = sdf.smin(moust, sdf.sd_capsule(q, (-0.09, 0.3, 0.28), (-0.1, 0.23, 0.26), 0.016), 0.02)
        moust = sdf.smin(moust, sdf.sd_capsule(q, (0.09, 0.3, 0.28), (0.1, 0.23, 0.26), 0.016), 0.02)
        strap = np.maximum(r03.sd_torus_z(q, (0, 0.69, -0.02), 0.325, 0.022),
                           np.abs(q[:, 1] - 0.69) - 0.03)
        # goggles pushed up: tilted back on the brow, lenses facing up and forward, one rubber frame
        frame = sdf.sd_box(q, (0, 0.73, 0.25), (0.2, 0.05, 0.05), 0.04)
        lenses = np.minimum(sdf.sd_ellipsoid(q, (-0.095, 0.745, 0.3), (0.07, 0.04, 0.03)),
                            sdf.sd_ellipsoid(q, (0.095, 0.745, 0.3), (0.07, 0.04, 0.03)))
        rims = sdf.smin(frame, strap, 0.03)
        band = np.maximum(r03.sd_torus_z(q, (0, 0.44, -0.04), 0.335, 0.02), -q[:, 1] + 0.44)
        cup = sdf.sd_cylinder_x(q, (-0.315, 0.44, -0.02), 0.07, 0.03)
        boom = sdf.sd_capsule(q, (-0.32, 0.40, 0.03), (-0.22, 0.31, 0.22), 0.014)
        mic = sdf.sd_sphere(q, (-0.215, 0.305, 0.23), 0.028)
        led = sdf.sd_sphere(q, (-0.335, 0.41, 0.05), 0.013)
        pb = r03.turn(p, -0.08)
        neck, torso = r03.body(pb, shoulders=0.88)
        collar = sdf.sd_cylinder_y(pb, (0, 0.02, -0.04), 0.25, 0.1)
        collar = np.maximum(collar, -sdf.sd_cylinder_y(pb, (0, 0.02, -0.04), 0.16, 0.2))
        ring = np.maximum(sdf.sd_cylinder_y(pb, (0, 0.13, -0.04), 0.255, 0.018),
                          -sdf.sd_cylinder_y(pb, (0, 0.13, -0.04), 0.18, 0.1))
        mask = sdf.sd_ellipsoid(pb, (0.2, -0.2, 0.26), (0.12, 0.1, 0.08))
        filt = sdf.sd_cylinder_z(pb, (0.2, -0.22, 0.34), 0.05, 0.022)
        mstrap = sdf.sd_capsule(pb, (0.08, -0.07, 0.27), (-0.18, 0.06, 0.16), 0.014)
        pads = [sdf.sd_ellipsoid(pb, (s * 0.52, -0.22, -0.04), (0.2, 0.08, 0.22)) for s in (-1, 1)]
        return sdf.union(*items, (moust, BROW), (hair, HAIR), (rims, EXTRA),
                         (lenses, ACCENT),
                         (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH), (collar, CLOTH),
                         (np.minimum(*pads), CLOTH), (ring, METAL), (mask, EXTRA), (filt, METAL),
                         (mstrap, EXTRA),
                         (band, METAL), (cup, METAL), (boom, METAL), (mic, METAL), (led, GLOW))
    return scene, mats


LIGHT = dict(key=(-1.6, 2.4, 3.2), rim=(2.4, 0.8, -1.2), rim_col=(1.0, 0.6, 0.3),
             bg=((18, 8, 4), (80, 36, 14)), fill_col=(1.0, 0.65, 0.4), channel="cdf",
             exposure=1.2)
VARIANTS = {"a": colonial_a, "b": colonial_b}
CHOSEN = "b"                     # the user's pick (round 36's review, 2026-10-13); a goes to rejected/
for v, build in VARIANTS.items():
    slug = f"generic-colonial-{v}"
    r03.CAST[slug] = dict(build=build, name="ARES CONTROL", role="ARES LANDING", **LIGHT)
    r08.EXTRA[slug] = dict(line=LINE, name_col=hud_r02.AMBER, role_col=hud_r02.CYAN,
                           line_col=hud_r02.LCD)

NOTE = {
    "a": ["A: THE OLD CONTROLLER. GREY STUBBLE, DUST-ORANGE MILITIA",
          "FIELD CAP, SINGLE-CUP HEADSET + BOOM MIC, DUST SCARF, RUST",
          "WORK JACKET WITH AN OCHRE COLONY PATCH. READ BY THE CAP."],
    "b": ["B: THE DUST-SUIT CONTROLLER. CROPPED GREY HAIR, HEAVY",
          "MOUSTACHE, AMBER DUST GOGGLES UP, RESPIRATOR AT THE THROAT,",
          "PADDED SUIT COLLAR WITH A SEAL RING. READ BY THE GOGGLES."],
}
COMMON = ["SAME BUST + STYLE-B PIPELINE AS THE OTHER RADIO SPEAKERS;",
          "TEAL FRIENDLY CHANNEL, TOLD APART FROM THE CDF OFFICER BY",
          "RUST/DUST-ORANGE CLOTHES, AGE AND SILHOUETTE."]


def sheet(v, huds):
    slug = f"generic-colonial-{v}"
    hud = huds[v]
    img = raster.sheet(1216, 540, f"ARES CONTROL - COLONIAL GENERIC SPEAKER, VARIANT {v.upper()}", ROUND)
    raster.draw_text(img, 16, 38, "HUD RADIO 72X72 IN HUD A FRAME (1X)", raster.LABEL_DIM)
    img.alpha_composite(r03.hud_radio(slug, hud), (16, 50))
    raster.draw_text(img, 16, 246, "LINE-UP 72X72 (1X)", raster.LABEL_DIM)
    row = [("CDF", Image.open(PORTRAITS / "radio-generic-cdf-neutral.png").convert("RGBA")),
           ("CIVILIAN", Image.open(PORTRAITS / "radio-generic-civilian-neutral.png").convert("RGBA"))]
    row += [(f"COLONY {k.upper()}", huds[k]) for k in sorted(huds)]
    for i, (label, im) in enumerate(row):
        x, y = 16 + (i % 2) * 124, 262 + (i // 2) * 104
        r03.MET.well(img, (x, y, x + 71, y + 71))
        img.alpha_composite(im, (x, y))
        raster.draw_text(img, x, y + 78, label, raster.LABEL if "COLONY" in label else raster.LABEL_DIM)
    raster.draw_text(img, 276, 38, "HUD PORTRAIT 4X", raster.LABEL_DIM)
    img.alpha_composite(sprite.enlarge(hud, 4), (276, 50))
    raster.draw_text(img, 580, 38, "HUD RADIO 2X", raster.LABEL_DIM)
    img.alpha_composite(sprite.enlarge(r03.hud_radio(slug, hud), 2), (580, 50))
    for i, line in enumerate(NOTE[v] + [""] + COMMON):
        raster.draw_text(img, 580, 414 + i * 12, line, raster.LABEL)
    raster.draw_text(img, 276, 350, "SPEAKS: L15-17, L19-21 (ARES CONTROL,", raster.LABEL_DIM)
    raster.draw_text(img, 276, 362, "ALSO THE DRAFTS' ARES WARDEN LINES)", raster.LABEL_DIM)
    raster.draw_text(img, 276, 386, "PROCEDURAL MOCKUP - SEE CONCEPT/", raster.LABEL_DIM)
    raster.draw_text(img, 276, 398, "PROMPTS.MD FOR THE PORTRAIT PROMPT.", raster.LABEL_DIM)
    return img


def main(args):
    wanted = [v for v in args if v in VARIANTS] or list(VARIANTS)
    huds = {}
    for v in VARIANTS:                       # both are rendered: each sheet shows the other at 1x
        huds[v] = r04.style_b2(f"generic-colonial-{v}", r03.render_bust(f"generic-colonial-{v}"))[1]
    for v in wanted:
        out_dir = r03.CHAR_DIR / "concept" if v == CHOSEN else r03.CHAR_DIR / "concept" / "rejected"
        out_dir.mkdir(parents=True, exist_ok=True)
        png = out_dir / f"generic-colonial-r36-{v}.png"
        sheet(v, huds).convert("RGB").save(png, optimize=True)
        print("wrote", png.relative_to(ROOT))


if __name__ == "__main__":
    main(sys.argv[1:])
