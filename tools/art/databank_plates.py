#!/usr/bin/env python3
"""Production art: the databank's plates (design/ui/databank, step DB-ART, round 82): one still picture of each boss,
of each unit whose sprite is over 128 px and of the Ascendancy concept page's cruiser, for the entry page's picture.

Outputs (assets/sprites/databank/, packed onto the shared sprite pages as ``databank/<slug>``), each 192x192 with a
transparent background and a ``Source`` chunk:
  <boss>.png       gorgon-frigate, brood-carrier, harbour-kraken, siege-spire, revenant-walker, dust-colossus,
                   abyssal-maw, iron-sovereign, honour-guard (design/ui/databank/data.yaml's ``bosses``)
  <unit>.png       leviathan, abyss-ray (the databank's enemies whose sprite is over 128 px)
  ascendancy-cruiser.png  the picture of the concept page "The Ascendancy" (its cruisers)
  design/ui/databank/concept/databank-plates-final-r82-a.png   review sheet: the plates at 1:1, then the sensor-none
                   silhouette a plate gives (a preview of what the game derives from the plate's alpha at load)

Nothing is redrawn: each plate is the boss or unit as the game assembles it from its production frames (the frames in
assets/sprites, placed by the production generators' own compose functions, pivots and offsets: gorgon_frigate.compose,
brood_carrier.compose, leviathan.compose, revenant_walker.walker_draw, iron_sovereign.station_layer, the intel
silhouettes' layouts of tools/art/intel.py for the Siege Spire, the Abyssal Maw and the Honour Guard), at rest, nose
down as it comes at the player, top-lit as the sprites are, in its faction's livery. Glows are added the way the game
blends them (additive) and kept only where they fall on the body, so a plate has no translucent pixel: no halo, no
dither. A plate that fits 184 px stays at 1:1; a larger one is reduced to fit 184 px as the render path reduces a
render (box filter on premultiplied colour, 1-bit alpha at 50 % coverage, a mild unsharp mask). One median-cut palette
of 48 colours per plate, no dithering.

The silhouette at sensor none has no file: the game derives it from the plate's alpha at load (the databank README).

Run: python3 tools/art/databank_plates.py [slug ...] [--review | --check]   (a few seconds)
  --review  only rebuilds the review sheet; --check re-composes every plate and compares it with the file (pixels,
            size, 1-bit alpha, colours, the Source chunk); exits 1 on a difference
"""
import sys

import numpy as np
from PIL import Image, ImageChops, ImageFilter

import artkit
from artkit import DESIGN, SPRITES, sprite

import abyss_ray  # noqa: E402,F401  (the Abyss Ray's frames: its name only)
import abyssal_maw  # noqa: E402  (the Abyssal Maw's coil layout and frames)
import brood_carrier  # noqa: E402  (the Brood Carrier's compose)
import dust_colossus  # noqa: E402  (the Dust Colossus's frames)
import gorgon_frigate  # noqa: E402  (the Gorgon Frigate's compose)
import harbour_kraken  # noqa: E402  (the Harbour Kraken's layout)
import honour_guard  # noqa: E402  (the aces' frames)
import iron_sovereign  # noqa: E402  (the station's layer)
import l23kit  # noqa: E402
import leviathan  # noqa: E402  (the Leviathan's compose)
import revenant_walker  # noqa: E402  (the walker's draw)

SCRIPT = "databank_plates.py"
BATCH = "M10 databank batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r82"
OUT = SPRITES / "databank"
CONCEPT = DESIGN / "ui" / "databank" / "concept"
SIZE = 192
INNER = 184                 # a plate's subject fits this, so a 4 px margin stays clear round it
COLOURS = 48
CLEAR = (0, 0, 0, 0)


def frames(name):
    return artkit.load_frames(name)


def clear_compose(fn, *args, **kw):
    """Call a generator's compose function, which starts from the review plate, on a clear canvas instead."""
    plate = artkit.PLATE
    artkit.PLATE = CLEAR
    try:
        return fn(*args, **kw)
    finally:
        artkit.PLATE = plate


# --------------------------------------------------------------------------- the subjects, at 1:1

def gorgon_frigate_layer():
    """The frigate at rest: the three necks hanging straight, the crown shut, the core glow at 0.6."""
    gf = gorgon_frigate
    rest = [[0.0] * 6 for _ in gf.CHAINS]
    return clear_compose(gf.compose, gf.load_art(), rest, crown=0, glow=0.6, size=(420, 460), centre=(210, 150))


def brood_carrier_layer():
    """The carrier nose down with every part on: the sacs shut, the iris shut, the core glow at 0.5, the turret
    aimed down the screen."""
    bc = brood_carrier
    return clear_compose(bc.compose, bc.load_art(), 0, sacs=None, iris=0, glow=0.5, canvas=(700, 700))


def leviathan_layer():
    """The second pass facing down: the body in sway frame 1 with every part intact and the blowhole glow at 0.8."""
    lv = leviathan
    body = frames("leviathan-down")[1]
    parts = {n: f[1] if n == "fluke" else f[0] for n, f in lv.load_parts("intact").items()}
    return lv.compose(body, parts, 1, frames("leviathan-blowhole-glow")[0], 0.8)


def harbour_kraken_layer():
    """The head surfaced with its eyes open over the arms that grip the platform from below (``grip-sub``), placed
    as the fight places them (the head and the grip relative to the platform's centre); the platform left out."""
    hk = harbour_kraken
    img = Image.new("RGBA", (520, 640), CLEAR)
    plat = np.array([260.0, 120.0])
    sprite.paste_center(img, frames("harbour-kraken-grip-sub")[0], *(plat + hk.GRIP_SUB_AT))
    sprite.paste_center(img, frames("harbour-kraken")[2], *(plat + hk.HEAD_AT - hk.PLAT))
    return img


def siege_spire_layer():
    """The spire rooted as the arena first shows it: the trunk (crown shut), its six live roots and their pods,
    placed by assets/pivots/siege-spire.json (the intel silhouette's layout)."""
    import json
    piv = json.loads((artkit.PIVOTS / "siege-spire.json").read_text(encoding="utf-8"))
    img = Image.new("RGBA", (520, 520), CLEAR)
    cx, cy = 260, 260
    roots, pods, mortars = frames("siege-spire-root"), frames("siege-spire-pod"), frames("siege-spire-mortar")
    for i, r in enumerate(piv["roots"]):
        sprite.paste_center(img, roots[2 * i], cx + r["offset"][0], cy + r["offset"][1])
    sprite.paste_center(img, frames("siege-spire")[0], cx + piv["trunk"]["offset"][0], cy + piv["trunk"]["offset"][1])
    for r in piv["roots"]:
        pod = pods[0] if r["kind"] == "turret" else mortars[0]
        sprite.paste_center(img, pod, cx + r["pod"][0], cy + r["pod"][1])
    return img


def revenant_walker_layer():
    """The walker at heading 0 as the game assembles it (legs in walk phase 0, hull, pods aimed ahead, the flamer at
    rest): revenant_walker.walker_draw."""
    rw = revenant_walker
    s = {name: rw.load(name) for name in rw.SET_NAMES}
    img, _ = rw.walker_draw(Image.new("RGBA", (320, 320), CLEAR), s, 160, 160, 0, 0)
    return img


def dust_colossus_layer():
    """The open mouth risen straight up (the UNDER pose, as phase 3 shows it): the intel silhouette's frame."""
    return dust_colossus.load("dust-colossus-mouth")[4 * 3 + 1]


MAW_NECK = 6               # the coil's segments nearest the head the Maw's plate keeps


def abyssal_maw_layer():
    """The Maw as the arena shows it after its rise (phase 1): the head with its sacs at the top and the thick end of
    the coil curling away from it round the arena's corner (its last ``MAW_NECK`` segments), the production frames on
    the concept's path (the intel silhouette's layout); the rest of the coil would shrink the head to a speck."""
    am = abyssal_maw
    segs, heads, sacs = (am.kit.load(f"{am.ROOT_NAME}-{n}") for n in ("segment", "head", "sac"))
    img = Image.new("RGBA", (640, 700), CLEAR)
    dx, dy = 80, 120            # the arena's edges cut the head and the coil: a margin round them
    hx, hy = am.b48.HEAD_XY
    for x, y, h, w, *_ in am.b48.coil_layout(am.b48.T_P1 + 1.0)[-MAW_NECK:]:
        am.kit.paste_c(img, segs[am.seg_index(h, am.TAPERS.index(w))], x + dx, y + dy)
    for ox, oy in am.b48.SAC_OFF:
        am.kit.paste_c(img, sacs[0], hx + ox + dx, hy + oy + dy)
    am.kit.paste_c(img, heads[0], hx + dx, hy + dy)
    return img


def iron_sovereign_layer():
    """The station at rest: the base disc with its lamps and bays, the three rings with their batteries, the core
    shut: iron_sovereign.station_layer."""
    sv = iron_sovereign
    lay = sv.station_layer({"outer": 0.0, "middle": 0.0, "inner": 0.0}, size=(480, 480), centre=(240, 240),
                           overlays=False)
    sprite.paste_center(lay, frames(f"{sv.ROOT_NAME}-core")[0], 240, 240)
    return lay


def honour_guard_layer():
    """The three aces at 1:1 as their entry shows them: Harker leading at the top facing down, Vesper banking in from
    the left, Kestrel rising from the rear on the right (the intel silhouette's layout)."""
    hg = honour_guard
    img = Image.new("RGBA", (190, 170), CLEAR)
    for ace, k, x, y in (("harker", 0, 95, 46), ("vesper", 52, 44, 118), ("kestrel", 28, 146, 124)):
        sprite.paste_center(img, l23kit.load(f"{hg.ROOT_NAME}-{ace}")[k], x, y)
    return img


def abyss_ray_layer():
    """The bone skate at heading 0, moving down the screen, its wings level (frame 0)."""
    return frames("abyss-ray")[0]


def ascendancy_cruiser_layer():
    """Level 28's cruiser hull nose up as the act shows it (the single frame)."""
    return frames("ascendancy-cruiser")[0]


PLATES = {
    "gorgon-frigate": gorgon_frigate_layer,
    "brood-carrier": brood_carrier_layer,
    "harbour-kraken": harbour_kraken_layer,
    "siege-spire": siege_spire_layer,
    "revenant-walker": revenant_walker_layer,
    "dust-colossus": dust_colossus_layer,
    "abyssal-maw": abyssal_maw_layer,
    "iron-sovereign": iron_sovereign_layer,
    "honour-guard": honour_guard_layer,
    "leviathan": leviathan_layer,
    "abyss-ray": abyss_ray_layer,
    "ascendancy-cruiser": ascendancy_cruiser_layer,
}


# --------------------------------------------------------------------------- the plate

def trimmed(img):
    """The layer cropped to its opaque pixels (alpha at 50 % or more)."""
    a = np.array(img)[..., 3]
    ys, xs = np.nonzero(a >= 128)
    return img.crop((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))


def reduce(img, size):
    """The render path's reduction: box filter on premultiplied colour, 1-bit alpha at 50 %, a mild unsharp mask."""
    a = np.array(img).astype(np.float64) / 255
    prem = np.dstack([a[..., :3] * a[..., 3:4], a[..., 3]])
    small = np.dstack([np.array(Image.fromarray(prem[..., c].astype(np.float32), "F").resize(size, Image.BOX))
                       .astype(np.float64) for c in range(4)])
    alpha = small[..., 3]
    rgb = small[..., :3] / np.maximum(alpha, 1e-6)[..., None]
    out = sprite.to_image(np.dstack([rgb, alpha]), 0.5)
    return sprite.sharpen(out, 90)


def plate(slug):
    """The 192x192 plate of ``slug``: its subject trimmed, kept at 1:1 when it fits 184 px, else reduced to fit."""
    subject = trimmed(PLATES[slug]())
    w, h = subject.size
    if max(w, h) > INNER:
        k = INNER / max(w, h)
        subject = reduce(subject, (max(1, round(w * k)), max(1, round(h * k))))
    else:
        a = np.array(subject)
        a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
        a[a[..., 3] == 0] = 0
        subject = Image.fromarray(a, "RGBA")
    out = Image.new("RGBA", (SIZE, SIZE), CLEAR)
    out.alpha_composite(subject, ((SIZE - subject.width) // 2, (SIZE - subject.height) // 2))
    return artkit.quantize_set([out], COLOURS)[0]


def build(slugs):
    OUT.mkdir(parents=True, exist_ok=True)
    for slug in slugs:
        img = plate(slug)
        artkit.save_png(img, OUT / f"{slug}.png", SOURCE)
        a = np.array(img)[..., 3]
        ys, xs = np.nonzero(a)
        print(f"{slug}: {xs.max() - xs.min() + 1}x{ys.max() - ys.min() + 1} px on {SIZE}x{SIZE}, "
              f"{artkit.colour_count([img])} colours")


def check():
    problems = []
    for slug in PLATES:
        path = OUT / f"{slug}.png"
        if not path.exists():
            problems.append(f"{slug}: missing")
            continue
        img = Image.open(path)
        if img.info.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{slug}: Source chunk {img.info.get(artkit.SOURCE_KEY)!r}")
        img = img.convert("RGBA")
        a = np.array(img)
        if img.size != (SIZE, SIZE):
            problems.append(f"{slug}: {img.size[0]}x{img.size[1]}, not {SIZE}x{SIZE}")
        if not np.isin(a[..., 3], (0, 255)).all():
            problems.append(f"{slug}: translucent pixels")
        if artkit.colour_count([img]) > COLOURS:
            problems.append(f"{slug}: {artkit.colour_count([img])} colours")
        m = SIZE - INNER
        if a[: m // 2, :, 3].any() or a[-(m // 2):, :, 3].any() or a[:, : m // 2, 3].any() or a[:, -(m // 2):, 3].any():
            problems.append(f"{slug}: reaches the {m // 2} px margin")
        if not np.array_equal(a, np.array(plate(slug))):
            problems.append(f"{slug}: differs from a fresh compose (rerun the script)")
    extra = sorted(p.name for p in OUT.glob("*.png") if p.stem not in PLATES)
    problems += [f"{name}: not a plate of this script" for name in extra]
    for p in problems:
        print("PROBLEM", p)
    print(f"{len(PLATES)} plates checked, {len(problems)} problems")
    return not problems


# --------------------------------------------------------------------------- review

def silhouette(img):
    """A preview of the sensor-none look the game derives from a plate's alpha: dark navy, a lit rim, scan lines."""
    a = img.getchannel("A")
    out = Image.new("RGBA", img.size, (40, 48, 80, 0))
    out.putalpha(a)
    rim = Image.new("RGBA", img.size, (90, 104, 150, 0))
    rim.putalpha(ImageChops.subtract(a, a.filter(ImageFilter.MinFilter(3))))
    out.alpha_composite(rim)
    arr = np.array(out)
    lines = (np.arange(SIZE) % 3 == 0)[:, None] & (arr[..., 3] > 0)
    arr[lines, :3] = np.minimum(255, arr[lines, :3].astype(int) + (14, 16, 26))
    return Image.fromarray(arr, "RGBA")


def review():
    artkit.REVIEW_ROUND = ROUND
    names = list(PLATES)
    plates = {n: Image.open(OUT / f"{n}.png").convert("RGBA") for n in names}
    rows = []
    for i in range(0, len(names), 4):
        group = names[i:i + 4]
        rows.append((" / ".join(n.upper() for n in group), [plates[n] for n in group], 1, False))
    for i in range(0, len(names), 6):
        group = names[i:i + 6]
        rows.append((f"SENSOR NONE, DERIVED AT LOAD (PREVIEW, 1/2): {' / '.join(n.upper() for n in group)}",
                     [silhouette(plates[n]).resize((SIZE // 2, SIZE // 2), Image.NEAREST) for n in group], 1, False))
    sheet = artkit.review_sheet("DATABANK PLATES - 192X192, 1:1", rows, width=1000, batch=BATCH)
    png, _ = artkit.review_paths(CONCEPT, "databank-plates")
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(artkit.ROOT)}")


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        build([a for a in args if not a.startswith("--")] or list(PLATES))
    review()
