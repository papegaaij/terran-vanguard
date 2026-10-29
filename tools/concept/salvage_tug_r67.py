#!/usr/bin/env python3
"""Concept round 67, row G.2 (QG-2, re-shown 2026-10-29): the Helix core's salvage, both variants side by side
(design/campaign/act-5-the-belt/level-34-salvage-run, *Layout*).

  a (built, recommended): the clamps blow, the crate lifts out of the hold on its seal's gold glow, then drifts with a
    slow gold pulse until flown through; a missed crate is radio only (Okafor: "Salvage tug has the core. Good enough,
    Lancer.").
  b: the same, plus the CDF salvage tug drawn crossing the field to take a missed crate: a concept sprite (not in the
    game), a small CDF work tug in the CDF's khaki, white and blue (the containers' colours), seen from above under the
    top-left sun: a boxy khaki hull with the white band and the blue CDF mark, the cockpit glass at the nose, two side
    thruster pods with blue-white drive glows, and two steel grapple arms ahead of the nose with gold-yellow jaws
    (open on the way in, shut round the crate's ends).

Both panels are the same mockup at 1x in Level 34's freighter section: the game's backdrop at the level time
(tools/art/backdrop_l34.py ``compose``, scrolling at the section's 110 px/s), the production clamps, crate, rise,
drift and glow frames (tools/art/belt_units_g.py), the player's ship missing the crate. Only the miss differs: a's
crate drifts off the bottom and the radio line plays; b's tug flies in from the right, grips it and carries it off to
the left. The tug is rendered on the frozen render package (the SDF renderer the production sprites use).

Outputs (design/campaign/act-5-the-belt/level-34-salvage-run/concept/):
  salvage-tug-r67-b.png      the tug at 1x and 2x (arms open; shut on the crate), with b's key frames at 1x
  salvage-tug-r67-b.gif      a | b side by side at 1x (976x540, 15 fps, 8 s), 110 px/s
  salvage-objects-r67-ab.png a | b: the same five moments of both panels at half size, labelled

Run: python3 tools/concept/salvage_tug_r67.py   (about half a minute)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "art"))
sys.path.insert(1, str(HERE))
import artkit  # noqa: E402
import backdrop_l34 as bl  # noqa: E402
import belt_units as bu  # noqa: E402
import enemies_r60 as e60  # noqa: E402  (the GIF writer)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_sphere, union  # noqa: E402

CONCEPT = bl.CONCEPT
PX = 20.0                                   # px per model unit (belt_units_g.py's)
TUG = (200, 96)
FPS = 12
T0, DUR = 107.6, 8.0                        # level time of the GIF's start (the hold coming in), its length
SHOT = [0.5, 0.75, 1.0, 1.25]               # the four clamps shot (s into the GIF)
RISE_AT = 1.5
DRIFT_V = (6.0, 70.0)                       # the drifting crate's screen velocity (px/s)
TUG_IN, TUG_V = 3.0, 170.0                  # the tug enters from the right at this time, flies left at this speed
GRIP = -60                                  # the crate's centre from the tug's (x px) when gripped
KEYS = [0.4, 1.4, 2.6, 4.2, 6.2]           # the still sheets' moments
LINE = "OKAFOR: SALVAGE TUG HAS THE CORE. GOOD ENOUGH, LANCER."


def M(albedo, metal=0.3, shininess=40, spec=0.4, emission=(0, 0, 0)):
    return Material(tuple(albedo), metal=metal, shininess=shininess, spec=spec, emission=tuple(emission))


def tug_mats():
    return [M((0.5, 0.48, 0.34), 0.3, 40, 0.4),                 # 0 khaki hull (the CDF container's)
            M((0.88, 0.88, 0.84), 0.2, 30, 0.3),                # 1 the white band
            M((0.16, 0.3, 0.58), 0.2, 30, 0.3),                 # 2 the blue CDF mark
            M((0.42, 0.43, 0.44), 0.6, 60, 0.6),                # 3 steel (pods, arms)
            M((0.1, 0.14, 0.2), 0.85, 120, 1.1),                # 4 the cockpit glass
            M((0.9, 0.7, 0.18), 0.4, 50, 0.5),                  # 5 the jaws (hazard yellow)
            M((0.1, 0.1, 0.12), emission=(0.62, 0.9, 1.0)),     # 6 drive glow, cool lamps
            M((0.12, 0.12, 0.13), 0.4, 30, 0.3)]                # 7 dark (vents, bells)


def tug_scene(shut):
    """The tug, nose to the left (-x), y up the screen: the hull, the band, the mark, the cockpit, two pods with their
    bells and glows, the arms ahead of the nose (open: splayed; shut: along the crate's ends)."""
    def scene(p):
        items = [(sd_box(p, (1.2, 0, 0.42), (1.5, 0.82, 0.36), 0.16), 0),
                 (sd_box(p, (1.75, 0, 0.42), (0.14, 0.84, 0.38), 0.05), 1),
                 (sd_box(p, (2.25, 0.0, 0.79), (0.2, 0.2, 0.01)), 2),
                 (sd_box(p, (0.0, 0, 0.62), (0.32, 0.5, 0.18), 0.12), 4),
                 (sd_box(p, (0.95, 0, 0.8), (0.32, 0.4, 0.04), 0.03), 7)]
        for s in (-1, 1):
            items += [(sd_capsule(p, (0.2, s * 1.05, 0.3), (2.7, s * 1.05, 0.3), 0.26), 3),
                      (sd_capsule(p, (2.7, s * 1.05, 0.3), (3.05, s * 1.05, 0.3), 0.2, 0.26), 7),
                      (sd_sphere(p, (3.1, s * 1.05, 0.3), 0.13), 6),
                      (sd_sphere(p, (0.25, s * 1.3, 0.42), 0.06), 6)]
            if shut:                       # along the crate's long sides, the jaws hooked round its far end
                arm = [(-0.3, s * 0.55), (-0.9, s * 1.14), (-4.0, s * 1.14)]
                jaw = (sd_box(p, (-4.05, s * 0.9, 0.5), (0.12, 0.28, 0.1), 0.03), 5)
            else:                          # splayed open
                arm = [(-0.3, s * 0.55), (-0.9, s * 1.2), (-2.7, s * 1.75)]
                jaw = (sd_box(p, (-2.8, s * 1.62, 0.42), (0.14, 0.24, 0.1), 0.03), 5)
            for (x0, y0), (x1, y1) in zip(arm, arm[1:]):
                items.append((sd_capsule(p, (x0, y0, 0.5), (x1, y1, 0.5), 0.1), 3))
            items.append(jaw)
        return union(*items)
    return scene


def tug_sprite(shut):
    img = bu.render(lambda p: tug_scene(shut)(p - np.array((-0.6, 0.0, 0.0))), tug_mats(), TUG, TUG[0] / PX, crisp=80)
    a = np.array(img).astype(np.float64)                 # a darker far (bottom-right) edge, as the props have
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op
    a[br, :3] *= 0.55
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== the mockup

class Scene:
    def __init__(self):
        self.pieces, self.placed, self.data = bl.load_proposal()
        self.s = {n: bu.load(n) for n in ("hold-clamp-left", "hold-clamp-right", "hold-clamp-left-break",
                                          "hold-clamp-right-break", "helix-core", "helix-core-rise",
                                          "helix-core-drift", "helix-core-glow")}
        self.small = bu.load("explosion-small")
        self.ship = artkit.load_frames("ship")[2]
        objs = self.data["ground_objects"]
        self.clamps = [(o["kind"], o["at"][0], o["at"][1]) for o in objs if o["kind"].startswith("hold-clamp")]
        self.crate = next(o for o in objs if o["kind"] == "helix-core")["at"]
        self.tug = {False: tug_sprite(False), True: tug_sprite(True)}

    def ground_y(self, t, t_enter, h):
        return bl.screen_y(t, t_enter, h)

    def crate_at(self, u):
        """The crate's screen position and look (frame, glow frame or None) u s into the GIF, while not gripped."""
        nt, x = self.crate
        lt = T0 + u
        y = self.ground_y(lt, nt, 40)
        if u < RISE_AT:
            return x, y, self.s["helix-core"][int(u * 2) % 2], None
        r = u - RISE_AT
        if r < 1.0:
            i = min(9, int(r * 10))
            return x, y - 30 * r, self.s["helix-core-rise"][i], self.s["helix-core-glow"][i]
        y1 = self.ground_y(T0 + RISE_AT + 1.0, nt, 40) - 30
        d = r - 1.0
        return (x + DRIFT_V[0] * d, y1 + DRIFT_V[1] * d, self.s["helix-core-drift"][int(d * 8) % 8],
                self.s["helix-core-glow"][10 + int(d * 6) % 6])

    def meet(self):
        """When b's tug (flying left from the right edge, its grip point level with the crate) reaches the crate."""
        for k in range(int(DUR * 100)):
            u = TUG_IN + k / 100
            cx, _, _, _ = self.crate_at(u)
            if 480 + 90 - TUG_V * (u - TUG_IN) + GRIP <= cx:
                return u
        return DUR

    def frame(self, u, variant, t_meet):
        lt = T0 + u
        img = bl.compose(lt, self.pieces, self.placed)
        for kind, nt, x in self.clamps:
            y = self.ground_y(lt, nt, 32)
            k = SHOT[[c for c in self.clamps].index((kind, nt, x))]
            side = kind.rsplit("-", 1)[1]
            if u < k:
                img = bu.paste(img, self.s[kind][0], x, y)
            else:
                img = bu.paste(img, self.s[kind][2], x, y)
                j = int((u - k) * 10)
                if j < 8:
                    img = bu.add(img, self.s[f"hold-clamp-{side}-break"][j], x, y)
                j = int((u - k) * 20)
                if j < len(self.small):
                    img = bu.add(img, self.small[j], x, y)
        ship_x = 240 - 130 * min(1.0, max(0.0, (u - 1.3) / 0.8))     # under the clamps, then away to the left
        gripped = variant == "b" and u >= t_meet
        if not gripped:
            cx, cy, fr, glow = self.crate_at(u)
            if -60 < cy < 600:
                img = bu.paste(img, fr, cx, cy)
                if glow is not None:
                    img = bu.add(img, glow, cx, cy)
        if variant == "b" and u >= TUG_IN:
            _, cy0, _, _ = self.crate_at(min(u, t_meet))
            tx = 480 + 90 - TUG_V * (u - TUG_IN)
            ty = cy0 if u < t_meet else cy0 - 40 * (u - t_meet)
            if gripped:
                img = bu.paste(img, self.s["helix-core-drift"][int(u * 8) % 8], tx + GRIP, ty)
            img = bu.paste(img, self.tug[u >= t_meet - 0.15], tx, ty)
        img = bu.paste(img, self.ship, ship_x, 470)
        said = (variant == "b" and u >= t_meet + 0.3) or (variant == "a" and self.crate_at(u)[1] > 560)
        if said:
            raster.draw_text(img, 156, 8, LINE[:33], (150, 220, 255))
            raster.draw_text(img, 156, 20, LINE[34:], (150, 220, 255))
        raster.draw_text(img, 8, 540 - 14, f"{variant.upper()}  L34 T {lt:5.1f} S  (110 PX/S)", raster.LABEL)
        return img


def main():
    sc = Scene()
    t_meet = sc.meet()
    n = int(DUR * FPS)
    frames = []
    for i in range(n):
        u = i / FPS
        a, b = sc.frame(u, "a", t_meet), sc.frame(u, "b", t_meet)
        out = Image.new("RGBA", (976, 540), raster.SHEET_BG)
        out.alpha_composite(a, (0, 0))
        out.alpha_composite(b, (496, 0))
        frames.append(out)
    gif = CONCEPT / "salvage-tug-r67-b.gif"
    size = e60.rig.write_gif(frames, gif, fps=FPS, colors=128)
    print(f"wrote {gif.relative_to(bl.ROOT)} ({size / 1e6:.1f} MB), the tug meets the crate at {t_meet:.2f} s")

    # the tug sheet: the sprite at 1x and 2x, then b's key frames at 1x
    kw, kh = 300, 540
    sheet = raster.sheet(16 + 5 * (kw + 8) + 8, 56 + 3 * TUG[1] + 16 + 14 + kh + 20,
                         "QG-2 B: THE CDF SALVAGE TUG (CONCEPT SPRITE)", "CONCEPT, ROUND 67 G.2 - R67")
    raster.draw_text(sheet, 16, 38, "KHAKI HULL, WHITE BAND, BLUE CDF MARK, COCKPIT AT THE NOSE (LEFT), TWO THRUSTER PODS, "
                     "GRAPPLE ARMS: OPEN / SHUT ON THE CRATE (1X, 2X)", raster.LABEL)
    bg = sc.frame(0.0, "a", t_meet).crop((0, 0, 480, 200))
    x = 16
    crate = sc.s["helix-core-drift"][0]
    for shut in (False, True):
        for k in (1, 2):
            cell = bg.crop((0, 0, (TUG[0] + 70) * k, TUG[1] * k)).copy()
            spr = sc.tug[shut]
            if shut:
                cell = bu.paste(cell, crate.resize((crate.width * k, crate.height * k), Image.NEAREST),
                                (TUG[0] // 2 + 60 + GRIP) * k, TUG[1] * k // 2)
            cell = bu.paste(cell, spr.resize((spr.width * k, spr.height * k), Image.NEAREST),
                            (TUG[0] // 2 + 60) * k, TUG[1] * k // 2)
            if k == 1:
                sheet.alpha_composite(cell, (x, 56))
                x += cell.width + 8
            else:
                sheet.alpha_composite(cell, (16 if not shut else 16 + cell.width + 8, 56 + TUG[1] + 8))
    y = 56 + 3 * TUG[1] + 16
    raster.draw_text(sheet, 16, y, "B IN THE LEVEL (1X, THE MIDDLE OF THE FIELD WHERE THE TUG WORKS):", raster.LABEL)
    for i, u in enumerate(KEYS):
        im = sc.frame(u, "b", t_meet).crop((150, 0, 450, 540))
        sheet.alpha_composite(im, (16 + i * (kw + 8), y + 14))
        raster.draw_text(sheet, 16 + i * (kw + 8), y + 14 + kh + 2, f"+{u:.1f} S", raster.LABEL_DIM)
    p = CONCEPT / "salvage-tug-r67-b.png"
    sheet.convert("RGB").save(p, optimize=True)
    print(f"wrote {p.relative_to(bl.ROOT)} {sheet.size}")

    # a | b: the same moments at half size
    hw, hh = 240, 270
    ab = raster.sheet(16 + 5 * (hw + 8) + 8, 60 + 2 * (hh + 40), "QG-2: A | B, THE CORE MISSED",
                      "CONCEPT, ROUND 67 G.2 - R67")
    raster.draw_text(ab, 16, 38, "THE SAME MOMENTS AT HALF SIZE: CLAMPS SHOT, THE RISE, THE DRIFT, THE MISS", raster.LABEL)
    for j, (v, lab) in enumerate((("a", "A: A MISSED CORE IS RADIO ONLY (OKAFOR'S TUG LINE)"),
                                  ("b", "B: THE CDF SALVAGE TUG CROSSES THE FIELD AND TAKES IT"))):
        y = 56 + j * (hh + 40)
        raster.draw_text(ab, 16, y, lab, (255, 210, 120))
        for i, u in enumerate(KEYS):
            im = sc.frame(u, v, t_meet).resize((hw, hh), Image.BOX)
            ab.alpha_composite(im, (16 + i * (hw + 8), y + 14))
            raster.draw_text(ab, 16 + i * (hw + 8), y + 14 + hh + 2, f"+{u:.1f} S", raster.LABEL_DIM)
    p = CONCEPT / "salvage-objects-r67-ab.png"
    ab.convert("RGB").save(p, optimize=True)
    print(f"wrote {p.relative_to(bl.ROOT)} {ab.size}")


if __name__ == "__main__":
    main()
