#!/usr/bin/env python3
"""Concept round 82: the databank screen (design/ui/databank), mockups in the game's glass UI style.

Drawn at the game's internal 960x540 from the real assets, then shown at 1280x720 the way the game scales a
1280x720 window (nearest x2, then a smooth step down: the sharp-bilinear look). Nothing is hand-edited:
  - the glass kit's nine-patches (assets/sprites/ui/*.9.png: glass, frame, inset, row, selection, chip, tab,
    tag, hint, rule, callout) and the cursors, with the colours of vanguard.game.ui.Glass;
  - the game's bitmap fonts (assets/fonts/label-8x12, body-10x20, heading-20x30), drawn tinted with a shadow;
  - the hangar's tactical map (assets/ui/hangar-map.png) as the backdrop, the title scene and logo for the menu;
  - the units' production frames (assets/sprites/<unit>_0.png, frame 0 faces down), the intel portraits
    (assets/sprites/intel/), the Iron Sovereign's base, rim and core frames, the gear icons
    (assets/sprites/icons/*-large.png), Dr. Varga's 72x72 radio portrait.
The campaign shown: medium, the hangar before Level 40 (Levels 01-39 flown). Numbers are the data's
(design/enemies/.../data.yaml, design/player/...): HP and bounty as the databank computes them (act HP factor,
difficulty lever, act credit factor 1.6^(act-1), the level's bounty_scale).

Outputs (design/ui/databank/concept/), each 1280x720:
  databank-r82-a.png            layout a: list left (names by act, first-met level), the Mirror Interceptor at L3
  databank-r82-b.png            layout b: portrait grid left (the intel portraits by act), the same entry
  databank-enemy-none-r82-a.png the Mirror Interceptor with no sensor suite: name, silhouette, first met, the vague
                                line, the locked fields naming the sensor level they need, the pointer to the suite
  databank-boss-r82-a.png       the Iron Sovereign at L3 (BOSSES tab): stats, phase notes, weapon advice
  databank-weapon-r82-a.png     the Tesla Coil Pod (WEAPONS tab): the level table, test fire, strong against
  databank-sensor-r82-a.png     the sensor suite's own entry (SHIP tab), owned none: what each level shows
  databank-access-r82-a.png     the ways in, 960x600 at 1:1: the hangar's top bar with the DATA chip, and the main
                                menu with DATABANK between MISSIONS and SCORES

Run: python3 tools/concept/databank_r82.py   (a few seconds)
"""
from pathlib import Path

from PIL import Image, ImageChops, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "design/ui/databank/concept"
ASSETS = ROOT / "assets"
SPR = ASSETS / "sprites"

# vanguard.game.ui.Glass
WHITE = (235, 240, 255)
BODY = (200, 212, 240)
LABEL = (150, 170, 205)
DIM = (70, 80, 110)
AMBER = (255, 255, 0)
CYAN = (0, 255, 255)
GREEN = (80, 255, 120)
ALERT = (255, 80, 60)
VALUE = (150, 170, 200)
UNLIT = (26, 30, 56)
SHADOW = (0, 0, 30)
DISABLED = (80, 88, 130)
W, H = 960, 540


class Font:
    """A BMFont text file + its page, drawn tinted (the game's fonts are white on alpha)."""

    def __init__(self, name):
        self.chars = {}
        page = None
        for line in (ASSETS / "fonts" / f"{name}.fnt").read_text().splitlines():
            parts = line.split()
            if not parts:
                continue
            kv = dict(p.split("=", 1) for p in parts[1:] if "=" in p)
            if parts[0] == "page":
                page = kv["file"].strip('"')
            elif parts[0] == "char":
                self.chars[int(kv["id"])] = {k: int(v) for k, v in kv.items() if k != "letter"}
            elif parts[0] == "common":
                self.line = int(kv["lineHeight"])
        self.page = Image.open(ASSETS / "fonts" / page).convert("RGBA")

    def width(self, text):
        return sum(self.chars.get(ord(c), self.chars[32])["xadvance"] for c in text)

    def draw(self, img, x, y, text, color):
        for ch in text:
            c = self.chars.get(ord(ch), self.chars[32])
            if c["width"] and c["height"]:
                g = self.page.crop((c["x"], c["y"], c["x"] + c["width"], c["y"] + c["height"]))
                tint = Image.new("RGBA", g.size, color + (255,))
                tint.putalpha(g.getchannel("A"))
                img.alpha_composite(tint, (x + c["xoffset"], y + c["yoffset"]))
            x += c["xadvance"]


LBL = Font("label-8x12")
BDY = Font("body-10x20")
HDG = Font("heading-20x30")


def text(img, font, x, y, s, color, shadow=True):
    if shadow:
        font.draw(img, x + 1, y + 1, s, SHADOW)
    font.draw(img, x, y, s, color)


def right(img, font, rx, y, s, color):
    text(img, font, rx - font.width(s), y, s, color)


def centred(img, font, cx, y, s, color):
    text(img, font, round(cx - font.width(s) / 2), y, s, color)


class Nine:
    """A libGDX nine-patch (.9.png): the 1 px border marks the stretched rows and columns."""

    def __init__(self, name):
        im = Image.open(SPR / "ui" / f"{name}.9.png").convert("RGBA")
        w, h = im.size
        px = im.load()
        xs = [x for x in range(1, w - 1) if px[x, 0][3] > 0]
        ys = [y for y in range(1, h - 1) if px[0, y][3] > 0]
        self.img = im.crop((1, 1, w - 1, h - 1))
        self.l, self.r = xs[0] - 1, (w - 2) - xs[-1]
        self.t, self.b = ys[0] - 1, (h - 2) - ys[-1]

    def draw(self, img, x, y, w, h, alpha=1.0, tint=None):
        src = self.img
        sw, sh = src.size
        out = Image.new("RGBA", (w, h))
        cols = [(0, self.l, 0, self.l), (self.l, sw - self.r, self.l, w - self.r), (sw - self.r, sw, w - self.r, w)]
        rows = [(0, self.t, 0, self.t), (self.t, sh - self.b, self.t, h - self.b), (sh - self.b, sh, h - self.b, h)]
        for sx0, sx1, dx0, dx1 in cols:
            for sy0, sy1, dy0, dy1 in rows:
                if sx1 > sx0 and sy1 > sy0 and dx1 > dx0 and dy1 > dy0:
                    piece = src.crop((sx0, sy0, sx1, sy1)).resize((dx1 - dx0, dy1 - dy0), Image.NEAREST)
                    out.paste(piece, (dx0, dy0))
        if tint is not None:
            a = out.getchannel("A")
            lum = out.convert("L")
            out = Image.merge("RGBA", [lum.point(lambda v, c=c: v * c // 255) for c in tint] + [a])
        if alpha < 1:
            out.putalpha(out.getchannel("A").point(lambda v: int(v * alpha)))
        img.alpha_composite(out, (x, y))


NP = {n: Nine(n) for n in ["glass", "frame", "inset", "row", "selection", "chip", "chip-on", "chip-off", "tab",
                           "tab-on", "tag", "hint", "rule", "callout", "callout-on", "portrait-hud"]
      if (SPR / "ui" / f"{n}.9.png").exists()}


def sprite(path):
    return Image.open(path).convert("RGBA")


CURSOR = sprite(SPR / "ui/cursor.png")
CURSOR_SMALL = sprite(SPR / "ui/cursor-small.png")
CURSOR_LARGE = sprite(SPR / "ui/cursor-large.png")
SCROLL_UP = sprite(SPR / "ui/scroll-up.png")
SCROLL_DOWN = sprite(SPR / "ui/scroll-down.png")


def panel(img, x, y, w, h, alpha=0.70):
    NP["glass"].draw(img, x, y, w, h, alpha)
    NP["frame"].draw(img, x, y, w, h)


def header(img, s, x, rx, y, color=AMBER):
    text(img, LBL, x, y, s, color)
    sx = x + LBL.width(s) + 6
    NP["rule"].draw(img, sx, y + 2, rx - sx, NP["rule"].img.size[1])


def chip(img, s, x, y, w, h, on=False, enabled=True, font=LBL):
    NP["chip-on" if on else "chip" if enabled else "chip-off"].draw(img, x, y, w, h)
    col = AMBER if on else BODY if enabled else DISABLED
    cap = 7 if font is LBL else 13
    centred(img, font, x + w / 2, round(y + (h - cap) / 2) - (2 if font is LBL else 3), s, col)


def tag(img, s, color, x, y):
    w = LBL.width(s) + 6
    NP["tag"].draw(img, x, y, w, 12, tint=color)
    text(img, LBL, x + 3, y + 1, s, color)
    return w


def selection(img, x, y, w, h, font=LBL, cx=None):
    NP["selection"].draw(img, x, y, w, h)
    cur = CURSOR_SMALL if font is LBL else CURSOR
    img.alpha_composite(cur, (x + 2, y + (h - cur.size[1]) // 2))


def hints(img, s):
    w = LBL.width(s) + 20
    NP["hint"].draw(img, round((W - w) / 2), H - 25, w, 18)
    centred(img, LBL, W / 2, H - 22, s, DIM)


def backdrop():
    bg = sprite(ASSETS / "ui/hangar-map.png")
    return bg


def upscale(img):
    big = img.convert("RGB").resize((W * 2, H * 2), Image.NEAREST)
    return big.resize((1280, 720), Image.BILINEAR)


def silhouette(spr, color=(40, 48, 80), edge=(90, 104, 150)):
    a = spr.getchannel("A").point(lambda v: 255 if v > 60 else 0)
    out = Image.new("RGBA", spr.size, color + (0,))
    out.putalpha(a)
    # a lit rim: the alpha minus its one-pixel erosion
    inner = a.filter(ImageFilter.MinFilter(3))
    rim = Image.new("RGBA", spr.size, edge + (0,))
    rim.putalpha(ImageChops.subtract(a, inner))
    out.alpha_composite(rim)
    # scan lines over it (the sensors' "unknown" look)
    px = out.load()
    for yy in range(0, out.size[1], 3):
        for xx in range(out.size[0]):
            r, g, b, al = px[xx, yy]
            if al:
                px[xx, yy] = (r + 14, g + 16, b + 26, al)
    return out


def unit(slug):
    for p in (SPR / f"{slug}_0.png", SPR / f"{slug}.png"):
        if p.exists():
            return sprite(p)
    raise FileNotFoundError(slug)


def mirror_interceptor():
    s = unit("mirror-interceptor")
    for lay in ("mirror-interceptor-plate_0.png", "mirror-interceptor-pods_0.png"):
        s.alpha_composite(sprite(SPR / lay))
    return s


def iron_sovereign(size):
    base = sprite(SPR / "iron-sovereign-base.png")
    rim = sprite(SPR / "iron-sovereign-rim.png")
    base.alpha_composite(rim, ((base.size[0] - rim.size[0]) // 2, (base.size[1] - rim.size[1]) // 2))
    core = sprite(SPR / "iron-sovereign-core_0.png")
    base.alpha_composite(core, ((base.size[0] - core.size[0]) // 2, (base.size[1] - core.size[1]) // 2))
    return base.resize((size, size), Image.LANCZOS)


def portrait(slug):
    p = SPR / "intel" / f"{slug}.png"
    if p.exists():
        return sprite(p)
    s = unit(slug)
    s.thumbnail((30, 30), Image.LANCZOS)
    out = Image.new("RGBA", (30, 30))
    out.alpha_composite(s, ((30 - s.size[0]) // 2, (30 - s.size[1]) // 2))
    return out


# ---------------------------------------------------------------- the screen's frame

TABS = [("ENEMIES", 63), ("BOSSES", 9), ("WEAPONS", 24), ("SHIP", 31), ("CONCEPTS", 14)]
LIST = (16, 80, 300, 424)
ENTRY = (324, 80, 620, 424)


def frame(tab, sensor):
    img = backdrop()
    NP["glass"].draw(img, 16, 6, W - 32, 40, 0.86)
    NP["frame"].draw(img, 16, 6, W - 32, 40)
    text(img, HDG, 26, 11, "DATABANK", WHITE)
    text(img, LBL, 200, 13, "CAMPAIGN - BEFORE L40 - HONOUR GUARD", CYAN)
    text(img, LBL, 200, 27, "ACT VI - JOVIAN STORM - MEDIUM", LABEL)
    # the sensor badge: what the entries can show, and where to get more
    NP["inset"].draw(img, 600, 12, 206, 28)
    if sensor:
        text(img, LBL, 610, 15, "SENSOR SUITE", LABEL)
        right(img, BDY, 798, 14, f"L{sensor}", GREEN)
        text(img, LBL, 610, 27, "SHOWS EVERYTHING", DIM)
    else:
        text(img, LBL, 610, 15, "SENSOR SUITE", LABEL)
        right(img, BDY, 798, 14, "NONE", ALERT)
        text(img, LBL, 610, 27, "SEE SHIP TAB", AMBER)
    chip(img, "< BACK", 814, 10, 124, 32, font=BDY)
    # tabs: Q/E (the bumpers), as the scores and options tabs
    x = 16
    for name, n in TABS:
        s = f"{name} {n}"
        w = LBL.width(s) + 22
        NP["tab-on" if name == tab else "tab"].draw(img, x, 54, w, 20)
        centred(img, LBL, x + w / 2, 58, s, AMBER if name == tab else BODY)
        x += w + 4
    text(img, LBL, x + 8, 58, "Q/E", DIM)
    panel(img, *LIST)
    panel(img, *ENTRY)
    hints(img, "Q/E TAB    UP/DOWN ENTRY    LEFT/RIGHT LEVEL    ESC BACK" if tab == "WEAPONS"
          else "Q/E TAB    UP/DOWN ENTRY    ENTER SENSOR SUITE    ESC BACK" if not sensor
          else "Q/E TAB    UP/DOWN ENTRY    ESC BACK")
    return img


def list_rows(img, title, rows, selected, scroll_up=False, scroll_down=False):
    x, y, w, h = LIST
    header(img, title, x + 12, x + w - 12, y + 8)
    ry = y + 26
    for row in rows:
        kind = row[0]
        if kind == "act":
            text(img, LBL, x + 12, ry + 3, row[1], LABEL)
            NP["rule"].draw(img, x + 16 + LBL.width(row[1]), ry + 5, x + w - 28 - (x + 16 + LBL.width(row[1])), 6)
        else:
            name, level, extra = row[1], row[2], row[3] if len(row) > 3 else None
            unknown = name == "???"
            if row == selected:
                selection(img, x + 6, ry, w - 12, 16)
            else:
                NP["row"].draw(img, x + 6, ry, w - 12, 16)
            col = AMBER if row == selected else DISABLED if unknown else WHITE
            text(img, LBL, x + 18, ry + 3, name, col)
            rx = x + w - 14
            if extra:
                tw = LBL.width(extra[0]) + 6
                tag(img, extra[0], extra[1], rx - LBL.width(level) - 6 - tw, ry + 2)
            right(img, LBL, rx, ry + 3, level, DIM if unknown else LABEL)
        ry += 17
    if scroll_up:
        img.alpha_composite(SCROLL_UP, (x + w - 16, y + 22))
    if scroll_down:
        img.alpha_composite(SCROLL_DOWN, (x + w - 16, y + h - 12))


ACT_V = [("unit", "TALON", "L29"), ("unit", "WAVERING STORMHAWK", "L29", ("ALLY", GREEN)),
         ("unit", "DART DRONE", "L30"), ("unit", "GILDED GUNSHIP", "L30"), ("unit", "MINELAYER", "L30"),
         ("unit", "RAIL SERPENT", "L30"), ("unit", "BUZZSAW DRONE", "L31"), ("unit", "HORNET", "L31"),
         ("unit", "SHARD DRONE", "L31"), ("unit", "SENTINEL TOWER", "L32"), ("unit", "STRIDER", "L32"),
         ("unit", "RAIL BUNKER", "L33"), ("unit", "VOID LEECH", "L33")]
SELECTED = ("unit", "MIRROR INTERCEPTOR", "L37")
ACT_VI = [("unit", "CHIMERA", "L36"), ("unit", "FLEET STORMHAWK", "L36", ("ALLY", GREEN)),
          ("unit", "HELIUM-3 HARVESTER", "L36"), ("unit", "GRAFT TURRET", "L37"), SELECTED,
          ("unit", "HARROW", "L38"), ("unit", "CRACKING TOWER", "L39", ("NEW", AMBER)),
          ("unit", "PUMP STATION", "L39", ("NEW", AMBER)), ("unit", "???", "ACT VI"), ("unit", "???", "ACT VI")]


def enemy_list(img):
    rows = [("act", "ACT V - THE BELT")] + ACT_V[-9:] + [("act", "ACT VI - JOVIAN STORM")] + ACT_VI
    list_rows(img, "63 KNOWN - 3 UNKNOWN", rows, SELECTED, scroll_up=True)


def field(img, y, label, value, color=CYAN, x=484, vx=574, locked=None):
    text(img, LBL, x, y, label, LABEL if not locked else DIM)
    if locked:
        text(img, LBL, vx, y, "- - - -", DIM)
        tag(img, locked, AMBER, vx + 48, y - 1)
    elif isinstance(value, str):
        text(img, LBL, vx, y, value, color)
    else:
        cx = vx
        for part, col in value:
            text(img, LBL, cx, y, part, col)
            cx += LBL.width(part)


def picture_box(img, pic, sensor_note):
    x, y = ENTRY[0] + 12, ENTRY[1] + 26
    NP["inset"].draw(img, x, y, 136, 136)
    px, py = x + (136 - pic.size[0]) // 2, y + (136 - pic.size[1]) // 2
    img.alpha_composite(pic, (px, py))
    if sensor_note:
        text(img, LBL, x + 6, y + 122, sensor_note, DIM)


def advice(img, y, rows):
    x0 = ENTRY[0] + 12
    header(img, "WEAPON ADVICE", x0, ENTRY[0] + ENTRY[2] - 12, y)
    y += 18
    for tags, line, mine in rows:
        cx = x0
        for t, col in tags:
            cx += tag(img, t, col, cx, y - 1) + 4
        text(img, LBL, max(cx + 4, x0 + 150), y, line[0], line[1])
        if mine:
            right(img, LBL, ENTRY[0] + ENTRY[2] - 14, y, mine, GREEN)
        y += 15
    return y


def varga(img, line_lines, y=None):
    x, w = ENTRY[0] + 12, ENTRY[2] - 24
    y = y or ENTRY[1] + ENTRY[3] - 98
    NP["inset"].draw(img, x, y, w, 88)
    img.alpha_composite(sprite(SPR / "portraits/radio-varga-neutral.png"), (x + 8, y + 8))
    NP["frame"].draw(img, x + 6, y + 6, 76, 76)
    text(img, LBL, x + 92, y + 10, "DR. VARGA", AMBER)
    text(img, LBL, x + 172, y + 10, "SCIENCE OFFICER", DIM)
    for i, s in enumerate(line_lines):
        text(img, LBL, x + 92, y + 28 + 13 * i, s, CYAN)


# ---------------------------------------------------------------- the entries

def mirror_entry(img, sensor):
    x0 = ENTRY[0] + 12
    header(img, "ENEMY - FIRST MET L37 FALL OF AURELIA", x0, ENTRY[0] + ENTRY[2] - 12, ENTRY[1] + 8)
    spr = mirror_interceptor()
    if sensor >= 2:
        picture_box(img, spr, "ANIMATED - 1:1")
    else:
        picture_box(img, silhouette(spr), "SILHOUETTE")
    text(img, HDG, 484, 104, "MIRROR INTERCEPTOR", WHITE)
    y = 140
    if sensor >= 2:
        fields = [
            ("CLASS", "JOVIAN ASCENDANCY - MEDIUM - MACHINE"),
            ("LAYER", "AIR - EVERY WEAPON REACHES IT"),
            ("FROM", "SIDES 70 %   REAR 30 %"),
            ("KILLS", "23"),
            ("HP", [("120", WHITE), ("   AT L37, MEDIUM", DIM)]),
            ("MIRROR", [("ARC 120   HEAT 130   DOWN 3 S", WHITE)]),
            ("BOUNTY", [("101 CR", AMBER), ("   AT L37", DIM)]),
            ("ATTACKS", "BURST - 3 AIMED MEDIUM (6), EVERY 2.4 S"),
            ("", "MIRROR - TURNS YOUR SHOTS BACK AT YOU"),
        ]
        for label, value in fields:
            field(img, y, label, value)
            y += 13
        text(img, LBL, 484, y, "WEAK", LABEL)
        text(img, LBL, 574, y, "THE OPEN FLANKS AND THE ENGINES", AMBER)
        y += 22
        y = advice(img, max(y, 270), [
            ([("SHIELD-BREAKER", AMBER)], ("SHATTERS THE MIRROR: DOWN 3 S", WHITE), "TESLA COIL POD L2"),
            ([("EMP BURST", AMBER)], ("SHATTERS IT AND STUNS IT 3 S", WHITE), "2 CHARGES"),
            ([("BEAM", AMBER)], ("HEATS THE MIRROR: BREAKS IN 1.0 S", WHITE), "ION BEAM L5"),
            ([("SIDE", AMBER), ("REAR", AMBER)], ("THE FLANKS AND THE REAR ARE OPEN", WHITE),
             "MICRO-MISSILE POD L4"),
            ([("BOMBS", LABEL), ("SHELLS", LABEL)], ("FALL PAST THE MIRROR ONTO THE HULL", WHITE), None),
            ([("FORWARD", ALERT)], ("BOUNCES BACK: A SMALL BULLET (4) AT YOU", ALERT), None),
        ])
        varga(img, ["\"ITS MIRROR ONLY GUARDS THE NOSE. GET BESIDE IT, OR CRACK",
                    "THE PLATE WITH THE TESLA POD - THEN EVERYTHING HITS.",
                    "AND DON'T FEED IT YOUR FRONT GUN.\""])
    else:
        locked = [("CLASS", "SENSOR L1"), ("LAYER", "SENSOR L1"), ("FROM", "SENSOR L1"), ("KILLS", "SENSOR L1"),
                  ("HP", "SENSOR L2"), ("BOUNTY", "SENSOR L2"), ("ATTACKS", "SENSOR L2"),
                  ("WEAK", "SENSOR L3")]
        for label, need in locked:
            field(img, y, label, None, locked=need)
            y += 13
        # the pointer to the sensor suite: the discoverability the user asked for
        bx, by, bw = x0, 272, ENTRY[2] - 24
        NP["callout-on"].draw(img, bx, by, bw, 66)
        text(img, LBL, bx + 10, by + 8, "NO SENSOR SUITE - THE DATABANK SHOWS ONLY WHAT YOU SAW.", AMBER)
        text(img, LBL, bx + 10, by + 22, "A SENSOR SUITE FILLS EVERY ENEMY YOU HAVE MET, AT ONCE:", WHITE)
        text(img, LBL, bx + 10, by + 36, "L1 CLASS, LAYER, DIRECTIONS   L2 PICTURE, HP, ATTACKS", BODY)
        text(img, LBL, bx + 10, by + 50, "L3 WEAK POINTS AND WEAPON ADVICE.", BODY)
        chip(img, "SENSOR SUITE >", bx + bw - 132, by + 44, 122, 16, on=False)
        text(img, LBL, x0, 350, "SHOP: UTILITY SLOT - L1 800 CR, 1 MW.  EASY ADDS ONE LEVEL.", LABEL)
        varga(img, ["\"SOMETHING FAST WITH A MIRROR FOR A NOSE. THAT'S ALL MY",
                    "SCANS GIVE ME, LANCER. FIT ME A SENSOR SUITE AND I'LL",
                    "TELL YOU HOW TO KILL IT.\""])


def screen_a(sensor=3):
    img = frame("ENEMIES", sensor)
    enemy_list(img)
    mirror_entry(img, sensor)
    return img


def screen_b():
    img = frame("ENEMIES", 3)
    x, y, w, h = LIST
    header(img, "63 KNOWN - 3 UNKNOWN", x + 12, x + w - 12, y + 8)
    acts = [
        ("ACT IV - DEEP WATER", ["drill-rig", "glassfin", "eel-swarm", "abyss-ray", "barbfin", "siren",
                                  "spiral-nautilus", "glow-angler", "depth-hunter", "relay-beacon"]),
        ("ACT V - THE BELT", ["asteroid-mite", "clamp-gun", "sam-nest", "talon", "wavering-stormhawk", "dart-drone",
                              "gilded-gunship", "minelayer", "rail-serpent", "buzzsaw-drone", "hornet",
                              "shard-drone", "sentinel-tower", "strider", "rail-bunker", "void-leech"]),
        ("ACT VI - JOVIAN STORM", ["chimera", "fleet-stormhawk", "helium-3-harvester", "graft-turret",
                                   "mirror-interceptor", "harrow", "cracking-tower", "pump-station", None, None]),
    ]
    gy = y + 24
    for i, (name, slugs) in enumerate(acts):
        if i == 0:
            slugs = slugs[-8:]  # scrolled: the act's last row
        text(img, LBL, x + 12, gy, name, LABEL)
        NP["rule"].draw(img, x + 16 + LBL.width(name), gy + 2, x + w - 28 - (x + 16 + LBL.width(name)), 6)
        gy += 14
        for k, slug in enumerate(slugs):
            cx, cy = x + 12 + (k % 8) * 35, gy + (k // 8) * 35
            sel = slug == "mirror-interceptor"
            NP["callout-on" if sel else "callout"].draw(img, cx, cy, 34, 34)
            if slug:
                img.alpha_composite(portrait(slug), (cx + 2, cy + 2))
            else:
                centred(img, BDY, cx + 17, cy + 7, "?", DISABLED)
        gy += ((len(slugs) + 7) // 8) * 35 + 6
    img.alpha_composite(SCROLL_UP, (x + w - 16, y + 22))
    # the selected cell's name line under the grid
    NP["inset"].draw(img, x + 10, y + h - 46, w - 20, 36)
    text(img, LBL, x + 18, y + h - 40, "MIRROR INTERCEPTOR", AMBER)
    text(img, LBL, x + 18, y + h - 26, "JOVIAN ASCENDANCY - FIRST MET L37", LABEL)
    mirror_entry(img, 3)
    return img


def boss_screen():
    img = frame("BOSSES", 3)
    sel = ("unit", "IRON SOVEREIGN", "L35")
    rows = [("act", "ACT I - FIRST CONTACT"), ("unit", "LEVIATHAN", "L03", ("SET PIECE", LABEL)),
            ("unit", "GORGON FRIGATE", "L05", ("MID-BOSS", LABEL)), ("unit", "BROOD CARRIER", "L07"),
            ("act", "ACT II - HOMEFRONT"), ("unit", "HARBOUR KRAKEN", "L11", ("MID-BOSS", LABEL)),
            ("unit", "SIEGE SPIRE", "L14"),
            ("act", "ACT III - RED DUST"), ("unit", "REVENANT WALKER", "L19", ("MID-BOSS", LABEL)),
            ("unit", "DUST COLOSSUS", "L21"),
            ("act", "ACT IV - DEEP WATER"), ("unit", "ABYSSAL MAW", "L28"),
            ("act", "ACT V - THE BELT"), sel,
            ("act", "ACT VI - JOVIAN STORM"), ("unit", "???", "ACT VI"), ("unit", "???", "ACT VI")]
    list_rows(img, "9 KNOWN - 2 UNKNOWN", rows, sel)
    x0 = ENTRY[0] + 12
    header(img, "ACT BOSS - FIRST MET L35 IRON SOVEREIGN", x0, ENTRY[0] + ENTRY[2] - 12, ENTRY[1] + 8)
    picture_box(img, iron_sovereign(128), None)
    text(img, HDG, 484, 104, "IRON SOVEREIGN", WHITE)
    y = 140
    for label, value in [
        ("CLASS", "JOVIAN ASCENDANCY - HUGE - STATION"),
        ("LAYER", "GROUND - EVERY WEAPON, ANTI-GROUND X2"),
        ("BEATEN", [("L35", WHITE), ("   PAR 2:25 (THE BOSS RUSH BONUS)", DIM)]),
        ("HP", [("17 220", WHITE), ("   CORE 6 600 + 13 BATTERIES", DIM)]),
        ("SHIELD", "THE OPEN CORE: 80 (HEX)"),
        ("BOUNTY", [("1 672 CR", AMBER), ("   AT L35, MEDIUM", DIM)]),
        ("ATTACKS", "SAM SALVOS, RAIL LINES, LASER SWEEPS,"),
        ("", "CORE FANS, THEN ITS PLAYBOOK"),
    ]:
        field(img, y, label, value)
        y += 13
    y = 254
    header(img, "PHASES", x0, ENTRY[0] + ENTRY[2] - 12, y)
    y += 18
    for label, s in [
        ("RINGS", "OUTER 6 SAM (220)  MIDDLE 4 RAIL (1 200)  INNER 3 LASER (1 500)"),
        ("1 - 3", "KILL A RING'S BATTERIES TO LOCK IT; EACH LOCK SPEEDS THE CORE"),
        ("", "BATTERIES FIRE ONLY INSIDE A 120-DEGREE ARC FACING YOU"),
        ("4", "THREE LOCKS OPEN THE CORE: SPIRAL, RAIL VOLLEY, DART SURGE,"),
        ("", "MINE RING, WRECK THROW - NEVER THE SAME MOVE TWICE"),
    ]:
        text(img, LBL, x0, y, label, LABEL)
        text(img, LBL, x0 + 56, y, s, BODY)
        y += 13
    y = advice(img, y + 6, [
        ([("ANTI-GROUND", AMBER)], ("X2 ON EVERY PART: IT IS A GROUND STATION", WHITE), "BOMB RACK L3"),
        ([("HOMING", AMBER), ("PIERCING", AMBER)], ("FIND THE BATTERIES ON THE RINGS", WHITE),
         "MICRO-MISSILE POD L4"),
        ([("SHIELD-BREAKER", AMBER), ("EMP", AMBER)], ("EMPTY THE OPEN CORE'S SHIELD", WHITE), "TESLA COIL POD L2"),
    ])
    varga(img, ["\"STRIP THE RINGS FIRST - EVERY GUN ON THEM FIRES ONLY",
                "TOWARD YOU. WHEN THE CORE OPENS IT'S SHIELDED: CRACK IT",
                "WITH THE TESLA POD AND POUR IT ON.\""], y=ENTRY[1] + ENTRY[3] - 98 + 4)
    return img


def weapon_screen():
    img = frame("WEAPONS", 3)
    sel = ("unit", "TESLA COIL POD", "L2", ("FITTED", GREEN))
    rows = [("act", "FRONT"), ("unit", "PULSE CANNON", "L5"), ("unit", "SCATTER VULCAN", "L3"),
            ("unit", "LANCE LASER", "SHOP"), ("unit", "ION BEAM", "L5", ("FITTED", GREEN)),
            ("unit", "HARPOON TORPEDOES", "SHOP"), ("unit", "PLASMA ARC", "SHOP", ("NEW", AMBER)),
            ("act", "WINGS"), ("unit", "MICRO-MISSILE POD", "L4", ("FITTED", GREEN)),
            ("unit", "BOMB RACK", "L3"), ("unit", "SWIVEL GUN", "SHOP"), sel,
            ("unit", "DEFLECTOR POD", "SHOP"), ("unit", "LIGHT DRONE BAY", "SHOP"),
            ("act", "REAR"), ("unit", "TAIL GUN", "L2"), ("unit", "SWARM TAIL", "L3", ("FITTED", GREEN)),
            ("unit", "FAN BLASTER", "SHOP"), ("unit", "SIDE SPLITTER", "SHOP"), ("unit", "REAR LANCE", "SHOP"),
            ("act", "SPECIALS"), ("unit", "AIRSTRIKE", "X2")]
    list_rows(img, "24 KNOWN - ALL IN THE SHOP", rows, sel, scroll_down=True)
    x0 = ENTRY[0] + 12
    header(img, "WING WEAPON - IN THE SHOP FROM L29", x0, ENTRY[0] + ENTRY[2] - 12, ENTRY[1] + 8)
    icon = sprite(SPR / "icons/tesla-coil-pod-large.png").resize((96, 96), Image.NEAREST)
    picture_box(img, icon, None)
    text(img, HDG, 484, 104, "TESLA COIL POD", WHITE)
    y = 140
    for label, value in [
        ("TRAITS", "AREA, SHIELD-BREAKER"),
        ("YOURS", [("L2", AMBER), ("   FITTED ON THE LEFT WING", GREEN)]),
        ("HITS", "AIR, LOW-AIR, GROUND"),
        ("NOT", [("HARDENED GROUND, HIGH-AIR, SUB", ALERT)]),
        ("ARC", "THE NEAREST TARGET IN REACH, A SHIELDED ONE"),
        ("", "FIRST; SPARKS HALF ITS DAMAGE AROUND IT"),
    ]:
        field(img, y, label, value)
        y += 13
    # the level table, the player's level lit
    ty = 236
    cols = [x0 + 96 + 96 * i for i in range(5)]
    for i, cx in enumerate(cols):
        centred(img, LBL, cx, ty, f"L{i + 1}", AMBER if i == 1 else LABEL)
    NP["rule"].draw(img, x0, ty + 12, ENTRY[2] - 24, 6)
    table = [("DPS", ["12", "17", "23", "31", "40"]), ("DRAW MW", ["2", "2.5", "3", "3.5", "4"]),
             ("COST CR", ["3 000", "1 500", "3 000", "6 000", "12 000"]),
             ("REACH PX", ["140", "150", "160", "170", "180"])]
    for r, (label, vals) in enumerate(table):
        yy = ty + 20 + 13 * r
        text(img, LBL, x0, yy, label, LABEL)
        for i, (cx, v) in enumerate(zip(cols, vals)):
            centred(img, LBL, cx, yy, v, AMBER if i == 1 else WHITE if i > 1 else DIM)
    # test fire, as the hangar's
    tx, tyy = x0, 318
    NP["inset"].draw(img, tx, tyy, 220, 72)
    text(img, LBL, tx + 6, tyy + 4, "TEST FIRE L2", AMBER)
    ship = unit("stormhawk") if (SPR / "stormhawk_0.png").exists() else None
    arc = sprite(SPR / "tesla-coil-pod-arc_0.png")
    if ship is not None:
        ship.thumbnail((28, 28), Image.LANCZOS)
        img.alpha_composite(ship, (tx + 20, tyy + 34))
    a = arc.copy()
    a.thumbnail((150, 40), Image.LANCZOS)
    img.alpha_composite(a, (tx + 44, tyy + 30))
    for dx, dy in ((150, 28), (190, 40), (170, 56)):
        NP["callout-on"].draw(img, tx + dx, tyy + dy, 10, 10)
    text(img, LBL, x0 + 232, 322, "STRONG AGAINST", LABEL)
    for i, s in enumerate(["MIRROR INTERCEPTOR - SHATTERS ITS MIRROR",
                           "TALON, HORNET, GILDED GUNSHIP - EMPTIES",
                           "  THEIR HEX SHIELDS",
                           "CHIMERA, HARROW - SPARKS ON THE FLOCK"]):
        text(img, LBL, x0 + 232, 336 + 13 * i, s, CYAN)
    NP["inset"].draw(img, x0, 400, ENTRY[2] - 24, 92)
    text(img, LBL, x0 + 8, 406, "AN ARC TO THE NEAREST TARGET IN REACH: IT NEVER MISSES WHAT IT CAN", BODY)
    text(img, LBL, x0 + 8, 419, "REACH, AND A HEX SHIELD IT TOUCHES IS EMPTIED AND STAYS DOWN 3 S.", BODY)
    text(img, LBL, x0 + 8, 440, "SHORT REACH (140-180 PX): FLY CLOSE. HARDENED TARGETS SHRUG IT OFF.", LABEL)
    text(img, LBL, x0 + 8, 466, "ENTER  OPEN IN THE HANGAR SHOP", DIM)
    return img


def sensor_screen():
    img = frame("SHIP", 0)
    sel = ("unit", "SENSOR SUITE", "SHOP")
    rows = [("act", "CORE"), ("unit", "GENERATOR MK V CRUCIBLE", "SHOP", ("NEW", AMBER)),
            ("unit", "GENERATOR MK IV TOKAMAK", "FITTED"), ("unit", "SHIELD MK IV", "FITTED"),
            ("unit", "REFLEX SHIELD", "SHOP"), ("unit", "COMPOSITE V", "SHOP"), ("unit", "COMPOSITE IV", "FITTED"),
            ("unit", "ENGINE MK II", "FITTED"),
            ("act", "UTILITY"), sel, ("unit", "PICKUP MAGNET", "L2"), ("unit", "SALVAGE SCANNER", "SHOP"),
            ("unit", "TARGETING COMPUTER", "OWNED"), ("unit", "EVASIVE THRUSTERS", "SHOP"),
            ("unit", "AUTO-REPAIR NANITES", "SHOP"), ("unit", "PRESSURE HULL", "OWNED"),
            ("unit", "ASCENDANCY IFF SPOOFER", "SHOP", ("NEW", AMBER)),
            ("act", "ESCORT"), ("unit", "ROOK", "AUTOCANNON L3"), ("unit", "BULWARK HEAVY DRONE", "SHOP")]
    list_rows(img, "31 KNOWN - ALL IN THE SHOP", rows, sel, scroll_up=True, scroll_down=True)
    x0 = ENTRY[0] + 12
    header(img, "UTILITY MODULE - IN THE SHOP FROM THE START", x0, ENTRY[0] + ENTRY[2] - 12, ENTRY[1] + 8)
    icon = sprite(SPR / "icons/utility-sensor-suite-large.png").resize((96, 96), Image.NEAREST)
    picture_box(img, icon, None)
    text(img, HDG, 484, 104, "SENSOR SUITE", WHITE)
    y = 140
    for label, value in [
        ("YOURS", [("NONE", ALERT), ("   NOT OWNED", DIM)]),
        ("SLOT", "UTILITY - 1 MW AT EVERY LEVEL"),
        ("COST", "L1 800   L2 2 000   L3 4 500 CR"),
        ("FLIGHT", "L2+: ARROWS FOR THREATS OFF THE SCREEN"),
        ("EASY", "COUNTS ONE LEVEL HIGHER (AT MOST L3)"),
    ]:
        field(img, y, label, value)
        y += 13
    ty = 254
    header(img, "WHAT EACH LEVEL SHOWS", x0, ENTRY[0] + ENTRY[2] - 12, ty)
    text(img, LBL, x0 + 44, ty + 18, "HANGAR INTEL (NEXT LEVEL)", LABEL)
    text(img, LBL, x0 + 316, ty + 18, "DATABANK (EVERY ENEMY MET)", LABEL)
    ladder = [
        ("NONE", ["SETTING, LAYERS, MAIN", "DIRECTION"], ["NAME, SILHOUETTE, FIRST", "MET, A VAGUE LINE"]),
        ("L1", ["+ DIRECTIONS, DENSITY,", "HAZARDS, OBJECTIVE"], ["+ CLASS, LAYER, WHERE IT", "COMES FROM, KILLS"]),
        ("L2", ["+ ENEMY TYPES, BOSS,", "SPECIALS"], ["+ PICTURE, HP, BOUNTY,", "ATTACKS BY NAME"]),
        ("L3", ["+ RECOMMENDED TRAITS,", "WAVES, SECRETS"], ["+ WEAK POINTS, WEAPON", "ADVICE, BOSS PHASES"]),
    ]
    yy = ty + 34
    for lvl, a, b in ladder:
        text(img, LBL, x0, yy, lvl, AMBER if lvl != "NONE" else ALERT)
        for i, s in enumerate(a):
            text(img, LBL, x0 + 44, yy + 12 * i, s, BODY)
        for i, s in enumerate(b):
            text(img, LBL, x0 + 316, yy + 12 * i, s, CYAN)
        yy += 28
    varga(img, ["\"I CAN ONLY TELL YOU WHAT THE SHIP CAN SEE, LANCER.",
                "GIVE ME A SENSOR SUITE AND EVERY FILE IN HERE FILLS IN -",
                "EVERYTHING WE'VE MET, NOT JUST THE NEXT FIGHT.\""])
    return img


def access_sheet():
    sheet = Image.new("RGBA", (W, 600), (4, 6, 20, 255))
    # the hangar's top bar with the fourth command chip
    bar = backdrop().crop((0, 0, W, 52))
    NP["glass"].draw(bar, 16, 6, W - 32, 40, 0.86)
    NP["frame"].draw(bar, 16, 6, W - 32, 40)
    text(bar, HDG, 26, 11, "HANGAR", WHITE)
    text(bar, LBL, 160, 13, "BEFORE L10 - EVACUATION CORRIDOR", CYAN)
    text(bar, LBL, 160, 27, "ACT II - HOMEFRONT - MEDIUM", LABEL)
    for i, s in enumerate(["UNDO", "REPAIR", "SAVE", "DATA"]):
        chip(bar, s, 424 + i * 58, 15, 54, 22, on=(s == "DATA"))
    NP["inset"].draw(bar, 662, 12, 144, 28)
    right(bar, BDY, 798, 16, "CR 3 450", AMBER)
    chip(bar, "> LAUNCH", 814, 10, 124, 32, font=BDY)
    sheet.alpha_composite(bar, (0, 0))
    # the main menu with DATABANK
    menu = sprite(ASSETS / "ui/title-scene.png")
    logo = sprite(ASSETS / "ui/title-logo.png")
    menu.alpha_composite(logo, ((W - logo.size[0]) // 2, 20))
    items = ["CONTINUE", "NEW GAME", "LOAD GAME", "MISSIONS", "DATABANK", "SCORES", "OPTIONS", "CREDITS", "QUIT"]
    py = 172
    height = 2 * 18 + (len(items) - 1) * 33 + 21
    panel(menu, 586, py, 334, height)
    for i, s in enumerate(items):
        yy = py + 18 + i * 33
        if s == "DATABANK":
            NP["selection"].draw(menu, 588, yy - 4, 330, 30)
            menu.alpha_composite(CURSOR_LARGE, (602, yy + 4))
        text(menu, HDG, 616, yy, s, AMBER if s == "DATABANK" else WHITE)
    hints(menu, "UP/DOWN SELECT    ENTER CONFIRM    ESC BACK")
    sheet.alpha_composite(menu, (0, 60))
    text(sheet, LBL, 8, 52, "HANGAR: THE DATA CHIP (TAB / Y), OR A CLICK ON AN INTEL PORTRAIT", LABEL, shadow=False)
    return sheet


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    outputs = {
        "databank-r82-a.png": screen_a(3),
        "databank-r82-b.png": screen_b(),
        "databank-enemy-none-r82-a.png": screen_a(0),
        "databank-boss-r82-a.png": boss_screen(),
        "databank-weapon-r82-a.png": weapon_screen(),
        "databank-sensor-r82-a.png": sensor_screen(),
    }
    for name, img in outputs.items():
        upscale(img).save(OUT / name, optimize=True)
        print("wrote", OUT / name)
    access_sheet().convert("RGB").save(OUT / "databank-access-r82-a.png", optimize=True)
    print("wrote", OUT / "databank-access-r82-a.png")


if __name__ == "__main__":
    main()
