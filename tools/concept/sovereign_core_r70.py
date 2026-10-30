#!/usr/bin/env python3
"""Concept round 70, row A.2: the Iron Sovereign's core phase, "something extra, less predictable": three variants
as storyboard strips (design/enemies/bosses/iron-sovereign, *Phases*, the draft for round 70).

Input (a real game still, not hand-edited): concept/sovereign-core-capture-r70-a.png, one 960x540 frame of the open
core (phase 4: the rings dark and still, the reactor open under its hex shield, Rook and the ship below), shot with
`--level 35 --invulnerable --loadout front=scatter-vulcan:4,rear=swarm-tail:3,left=micro-missile-pod:4,
right=micro-missile-pod:4,generator=4,shield=2,armor=4 --escort rook:autocannon:1 --shots 18600` (auto-fire on,
medium) on Xvfb. The new elements are drawn over the play field at game scale (1:1) with the production sprites
(assets/sprites: dart-drone_*, minelayer-mine-lit_*, iron-sovereign-bay-glow_*, explosion-vacuum-*,
iron-sovereign-cookoff-debris_*) and, where the game draws in code, in its look (the enemy `small` bullets'
magenta dots, the Rail Bunker's red telegraph line); the text in the game's bitmap fonts (assets/fonts).

Variants (five panels each, the field 480x540 at 1:1; panel 5 a schematic of how two seeds differ):
  a  the playbook: after each attack the core picks its next from a pool of four by the sim's seeded random
     (replays hold), never the same twice running, each with its own 0.8 s tell: spiral, rail volley, Dart surge,
     mine ring.
  b  the wreck throw: every 2.5-4 s (seeded) the core's tractor tears a dead battery's wreck off its ring (gold glow
     and sparks, 0.8 s), lifts it and hurls it at the ship's spot, tumbling (200 px/s, 60 HP, `heavy` on contact);
     the wreck, and so the direction, in a seeded order (then hull plates); spiral and Darts as built.
  c  the core breaks loose: the cradle's clamps blow and the reactor flies between five stops (the centre and four
     over the dead rings), the next picked at random, never the same, marked by a gold target ring 1 s ahead; it
     travels in 1.5 s, holds 6-9 s (seeded) and fires its spiral, bays and rail from wherever it is.

Outputs (design/enemies/bosses/iron-sovereign/concept/):
  sovereign-core-r70-a.png, -b.png, -c.png   one storyboard each at 1:1: panels 1-3 over panels 4-5 (1456 px wide)
  sovereign-core-r70-abc.png                 the three as one strip each at half size, stacked (the pick row's
                                             side-by-side view)

Run: python3 tools/concept/sovereign_core_r70.py   (a few seconds; the seeds' sequences use Python's random with
     fixed seeds, illustrative only)
"""
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "design/enemies/bosses/iron-sovereign/concept"
SPR = ROOT / "assets/sprites"
FONTS = ROOT / "assets/fonts"

CORE = (240, 160)
SHIP = (240, 450)
BAYS = [(CORE[0] + 60 * math.cos(math.radians(a)), CORE[1] + 60 * math.sin(math.radians(a))) for a in (45, 135, 225, 315)]
WHITE = (224, 230, 255)
GOLD = (255, 200, 60)
RED = (255, 60, 40)
AMBER = (255, 160, 20)
BG = (14, 16, 26)


class Font:
    def __init__(self, name):
        self.chars = {}
        for line in (FONTS / f"{name}.fnt").read_text().splitlines():
            parts = line.split()
            if not parts:
                continue
            kv = dict(p.split("=", 1) for p in parts[1:] if "=" in p)
            if parts[0] == "page":
                self.page = Image.open(FONTS / kv["file"].strip('"')).convert("RGBA")
            elif parts[0] == "char":
                self.chars[int(kv["id"])] = {k: int(v) for k, v in kv.items() if k != "letter"}

    def width(self, text):
        return sum(self.chars.get(ord(c), self.chars[32])["xadvance"] for c in text)

    def draw(self, img, x, y, text, color=WHITE):
        for ch in text:
            c = self.chars.get(ord(ch), self.chars[32])
            if c["width"] and c["height"]:
                g = self.page.crop((c["x"], c["y"], c["x"] + c["width"], c["y"] + c["height"]))
                tint = Image.new("RGBA", g.size, color + (255,))
                tint.putalpha(g.getchannel("A"))
                img.paste(tint, (x + c["xoffset"], y + c["yoffset"]), tint)
            x += c["xadvance"]


LABEL = Font("label-8x12")
BODY = Font("body-10x20")


def sprite(name):
    return Image.open(SPR / f"{name}.png").convert("RGBA")


def put(img, spr, x, y, scale=1.0):
    if scale != 1.0:
        spr = spr.resize((round(spr.width * scale), round(spr.height * scale)), Image.LANCZOS)
    img.paste(spr, (round(x - spr.width / 2), round(y - spr.height / 2)), spr)


def base():
    return Image.open(OUT / "sovereign-core-capture-r70-a.png").convert("RGB").crop((240, 0, 720, 540))


def glow_layer(img, draw_fn, blur=4, strength=1.0):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    draw_fn(ImageDraw.Draw(layer))
    g = layer.filter(ImageFilter.GaussianBlur(blur))
    if strength != 1.0:
        g.putalpha(g.getchannel("A").point(lambda v: min(255, int(v * strength))))
    img.paste(g, (0, 0), g)
    img.paste(layer, (0, 0), layer)


def bullet(d, x, y):
    d.ellipse([x - 3.5, y - 3.5, x + 3.5, y + 3.5], fill=(225, 40, 220, 255))
    d.ellipse([x - 1.5, y - 1.5, x + 1.5, y + 1.5], fill=(255, 230, 255, 255))


def spiral(img, centre, arms=2, t_end=2.2, phase=0.0):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    for arm in range(arms):
        for i in range(int(t_end * 5)):
            te = i / 5
            a = math.radians(phase + 72 * te + arm * 360 / arms)
            r = 40 + 130 * (t_end - te)
            x, y = centre[0] + r * math.cos(a), centre[1] + r * math.sin(a)
            if -5 < x < 485 and -5 < y < 545:
                bullet(d, x, y)
    img.paste(layer, (0, 0), layer)


def dart(img, x, y, angle_deg):
    k = round((angle_deg - 90) / 22.5) % 16
    put(img, sprite(f"dart-drone_{k}"), x, y)


def line_to_edge(p, ang):
    x, y = p
    dx, dy = math.cos(ang), math.sin(ang)
    t = 900
    return (x + dx * t, y + dy * t)


def telegraph(img, p, q, bright=False):
    def f(d):
        if bright:
            d.line([p, q], fill=(255, 220, 200, 255), width=4)
            d.line([p, q], fill=(255, 80, 50, 200), width=9)
        else:
            d.line([p, q], fill=(255, 50, 40, 210), width=3)

    glow_layer(img, f, blur=3)


def core_tell(img, color, radius=46, width=5):
    glow_layer(img, lambda d: d.ellipse([CORE[0] - radius, CORE[1] - radius, CORE[0] + radius, CORE[1] + radius],
                                        outline=color + (230,), width=width), blur=5)


def caption(img, lines, title=None):
    h = 14 * len(lines) + 10
    plate = Image.new("RGBA", (480, h), (6, 8, 20, 215))
    img.paste(plate, (0, 540 - h), plate)
    for i, line in enumerate(lines):
        LABEL.draw(img, 8, 540 - h + 6 + 14 * i, line)


def num(img, n):
    d = ImageDraw.Draw(img)
    d.rectangle([4, 28, 26, 50], fill=(6, 8, 20), outline=GOLD)
    BODY.draw(img, 10, 29, str(n), GOLD)


# ---------------------------------------------------------------- variant a: the playbook
def a_panels():
    p1 = base()
    core_tell(p1, (255, 255, 240), radius=44, width=6)
    spiral(p1, CORE, t_end=2.4, phase=30)
    caption(p1, ["SPIRAL - tell: the reactor flares white, the shield", "ripples (0.8 s); 2 arms, 130 px/s, for 4 s"])

    p2 = base()
    glow_layer(p2, lambda d: [d.pieslice([CORE[0] - 44, CORE[1] - 44, CORE[0] + 44, CORE[1] + 44], a - 12, a + 12,
                                         fill=(255, 40, 30, 150)) for a in (70, 90, 110)], blur=4)
    base_ang = math.atan2(SHIP[1] - CORE[1], SHIP[0] - CORE[0])
    for off in (-22, 0, 22):
        telegraph(p2, CORE, line_to_edge(CORE, base_ang + math.radians(off)))
    caption(p2, ["RAIL VOLLEY - tell: three petals glow red; three lines", "fan 22 deg through the ship's spot, 1.0 s, `heavy`"])

    p3 = base()
    glow = sprite("iron-sovereign-bay-glow_1")
    for bx, by in BAYS:
        put(p3, glow, bx, by, 2.0)
    for bx, by in BAYS:
        out = math.atan2(by - CORE[1], bx - CORE[0])
        for i in range(3):
            r = 22 + 24 * i
            bend = math.radians(18 * i) * (1 if bx < CORE[0] else -1)
            ang = out + bend * (1 if by > CORE[1] else -1)
            x, y = bx + r * math.cos(ang), by + r * math.sin(ang)
            dart(p3, x, y, math.degrees(ang))
    caption(p3, ["DART SURGE - tell: all four bays light gold (0.8 s);", "four streams of 3 at once (12 Darts, the cap 16)"])

    p4 = base()
    core_tell(p4, AMBER, radius=50, width=4)
    rng = random.Random(4)
    mine = sprite("minelayer-mine-lit_0")
    for i in range(8):
        a = math.radians(i * 45 + 22.5)
        r = rng.uniform(120, 220)
        x, y = CORE[0] + r * math.cos(a), CORE[1] + r * math.sin(a)
        glow_layer(p4, lambda d, a=a, r=r: d.line([(CORE[0] + 50 * math.cos(a), CORE[1] + 50 * math.sin(a)),
                                                  (CORE[0] + (r - 14) * math.cos(a), CORE[1] + (r - 14) * math.sin(a))],
                                                 fill=(255, 170, 60, 70), width=3), blur=2)
        glow_layer(p4, lambda d, x=x, y=y: d.ellipse([x - 40, y - 40, x + 40, y + 40], outline=(255, 120, 40, 110), width=1), blur=1)
        glow_layer(p4, lambda d, x=x, y=y: d.ellipse([x - 9, y - 9, x + 9, y + 9], fill=(255, 150, 40, 170)), blur=4)
        put(p4, mine, x, y, 1.2)
    caption(p4, ["MINE RING - tell: amber rim lights (0.8 s); 8 mines", "thrown out, stop at 120-220 px, arm: 40 px, 8 s fuse"])

    p5 = schematic_a()
    for i, p in enumerate((p1, p2, p3, p4), 1):
        num(p, i)
    return [p1, p2, p3, p4, p5]


def schematic_a():
    img = Image.new("RGB", (480, 540), BG)
    BODY.draw(img, 12, 12, "THE PICK, TWO SEEDS", GOLD)
    LABEL.draw(img, 12, 40, "after each attack (and a 1.5 s rest, 0.8 s below")
    LABEL.draw(img, 12, 54, "half HP) the core draws the next from the pool;")
    LABEL.draw(img, 12, 68, "never the same twice running, a surge at most every")
    LABEL.draw(img, 12, 82, "third draw. The sim's seeded random: a replay")
    LABEL.draw(img, 12, 96, "draws the same order.")
    cols = {"SPIRAL": (255, 255, 240), "RAIL": RED, "DARTS": GOLD, "MINES": AMBER}
    for row, seed in enumerate((11, 77)):
        rng = random.Random(seed)
        seq, last, since = [], None, 3
        while len(seq) < 9:
            pick = rng.choice(list(cols))
            if pick == last or (pick == "DARTS" and since < 2):
                continue
            since = 0 if pick == "DARTS" else since + 1
            seq.append(pick)
            last = pick
        y = 140 + row * 190
        BODY.draw(img, 12, y, f"seed {seed}:", WHITE)
        for i, s in enumerate(seq):
            x = 16 + (i % 3) * 152
            yy = y + 30 + (i // 3) * 44
            d = ImageDraw.Draw(img)
            d.rectangle([x, yy, x + 140, yy + 32], outline=cols[s], fill=(20, 22, 36))
            BODY.draw(img, x + 8, yy + 6, f"{i + 1} {s}", cols[s])
    LABEL.draw(img, 12, 520, "5  the order differs by seed; each tell names the attack")
    return img


# ---------------------------------------------------------------- variant b: the wreck throw
# the wrecked batteries' domes on the dead rings, as they sit in the capture (field px)
DOMES = [(125, 322), (362, 226), (40, 140), (172, 282), (440, 177), (117, 92), (326, 340), (290, 228), (156, 170),
         (305, 42), (274, 80)]


def dome_piece(src, x, y, r=21):
    m = Image.new("L", src.size, 0)
    ImageDraw.Draw(m).ellipse([x - r, y - r, x + r, y + r], fill=255)
    c = Image.new("RGBA", src.size, (0, 0, 0, 0))
    c.paste(src, (0, 0), m)
    return c.crop((x - r, y - r, x + r + 1, y + r + 1)), m


def socket(img, m):
    img.paste(Image.new("RGB", img.size, (8, 8, 13)), (0, 0), m)


def dome_tell(img, x, y):
    glow_layer(img, lambda d: d.ellipse([x - 24, y - 24, x + 24, y + 24], outline=GOLD + (255,), width=3), blur=4,
               strength=1.6)
    rng = random.Random(x * 7 + y)
    glow_layer(img, lambda d: [d.point((x + rng.uniform(-30, 30), y + rng.uniform(-30, 30)), fill=(255, 240, 160, 255))
                               for _ in range(24)], blur=1)


def flying(img, piece, x, y, turn, lift=1.2):
    shadow = Image.new("RGBA", piece.size, (0, 0, 0, 0))
    shadow.putalpha(piece.getchannel("A").point(lambda v: v * 3 // 5))
    put(img, shadow, x + 12, y + 16, lift)
    rim = Image.new("RGBA", piece.size, GOLD + (0,))
    rim.putalpha(piece.getchannel("A").filter(ImageFilter.MaxFilter(5)).point(lambda v: v // 2))
    put(img, rim.rotate(turn, resample=Image.BICUBIC), x, y, lift * 1.08)
    put(img, piece.rotate(turn, resample=Image.BICUBIC), x, y, lift)


def aim(img, x0, y0, x1, y1):
    ang = math.atan2(y1 - y0, x1 - x0)
    n = int(math.hypot(x1 - x0, y1 - y0) // 34)
    glow_layer(img, lambda d: [d.line([(x0 + 34 * i * math.cos(ang), y0 + 34 * i * math.sin(ang)),
                                       (x0 + (34 * i + 14) * math.cos(ang), y0 + (34 * i + 14) * math.sin(ang))],
                                      fill=(255, 200, 60, 170), width=2) for i in range(1, n)], blur=2)


def b_panels():
    src = base()
    (ax, ay), (bx, by) = DOMES[0], DOMES[1]
    pa, ma = dome_piece(src, ax, ay)
    pb, mb = dome_piece(src, bx, by)

    p1 = src.copy()
    dome_tell(p1, ax, ay)
    spiral(p1, CORE, t_end=1.6, phase=200)
    caption(p1, ["tell: a dead battery's wreck glows gold, sparks fly", "(the core's tractor, 0.8 s); which wreck: seeded"])

    p2 = src.copy()
    socket(p2, ma)
    lx, ly = ax + 0.15 * (SHIP[0] - ax), ay + 0.15 * (SHIP[1] - ay) - 10
    aim(p2, lx, ly, *SHIP)
    flying(p2, pa, lx, ly, 0)
    caption(p2, ["torn loose: it lifts onto the play plane (an empty", "socket left) and locks on the ship's spot"])

    p3 = src.copy()
    socket(p3, ma)
    fx, fy = ax + 0.62 * (SHIP[0] - ax), ay + 0.62 * (SHIP[1] - ay)
    glow_layer(p3, lambda d: d.line([(lx, ly), (fx, fy)], fill=(255, 190, 90, 60), width=10), blur=6)
    flying(p3, pa, fx, fy, 70)
    dome_tell(p3, bx, by)
    spiral(p3, CORE, t_end=1.8, phase=260)
    caption(p3, ["hurled at 200 px/s, tumbling: dodge it, or shoot it", "(60 HP); the next wreck already glows (middle ring)"])

    p4 = src.copy()
    socket(p4, ma)
    socket(p4, mb)
    put(p4, sprite("explosion-vacuum-small_3"), fx + 26, fy + 30, 1.3)
    put(p4, sprite("iron-sovereign-cookoff-debris_4"), fx + 26, fy + 30, 1.2)
    gx, gy = bx + 0.45 * (SHIP[0] + 50 - bx), by + 0.45 * (SHIP[1] - by)
    glow_layer(p4, lambda d: d.line([(bx, by), (gx, gy)], fill=(255, 190, 90, 60), width=10), blur=6)
    flying(p4, pb, gx, gy, -120)
    spiral(p4, CORE, t_end=2.0, phase=320)
    caption(p4, ["a hit breaks it (debris, no bullets); the next comes", "from another ring and side, between the spiral's arms"])

    p5 = schematic_b(src)
    for i, p in enumerate((p1, p2, p3, p4), 1):
        num(p, i)
    return [p1, p2, p3, p4, p5]


def schematic_b(src):
    img = src.copy().point(lambda v: v // 3)
    BODY.draw(img, 12, 34, "WHICH WRECK, TWO SEEDS", GOLD)
    d = ImageDraw.Draw(img)
    for row, (seed, col, off) in enumerate(((11, (120, 220, 255), -9), (77, GOLD, 9))):
        order = list(range(len(DOMES)))
        random.Random(seed).shuffle(order)
        for i, k in enumerate(order[:6]):
            x, y = DOMES[k][0] + off, DOMES[k][1]
            d.ellipse([x - 9, y - 9, x + 9, y + 9], outline=col, width=2, fill=(10, 12, 20))
            LABEL.draw(img, round(x - 4), round(y - 5), str(i + 1), col)
        LABEL.draw(img, 12, 400 + row * 18, f"seed {seed}: its first six throws", col)
    LABEL.draw(img, 12, 440, "one every 2.5-4 s (seeded), at most 3 in the air;")
    LABEL.draw(img, 12, 454, "the 13 wrecks in a seeded order, then hull plates;")
    LABEL.draw(img, 12, 468, "from any ring and side: above, beside, behind.")
    LABEL.draw(img, 12, 520, "5  two seeds: the throws' order and origin differ")
    return img


# ---------------------------------------------------------------- variant c: the core breaks loose
STOPS = [(240, 160), (110, 130), (370, 130), (140, 280), (340, 280)]


def core_disc(src):
    m = Image.new("L", src.size, 0)
    ImageDraw.Draw(m).ellipse([CORE[0] - 50, CORE[1] - 50, CORE[0] + 50, CORE[1] + 50], fill=255)
    c = Image.new("RGBA", src.size, (0, 0, 0, 0))
    c.paste(src, (0, 0), m)
    return c.crop((CORE[0] - 50, CORE[1] - 50, CORE[0] + 51, CORE[1] + 51))


def cradle(img):
    d = ImageDraw.Draw(img)
    d.ellipse([CORE[0] - 46, CORE[1] - 46, CORE[0] + 46, CORE[1] + 46], fill=(9, 9, 14), outline=(120, 90, 30), width=3)
    for a in range(0, 360, 60):
        r = math.radians(a + 30)
        x, y = CORE[0] + 44 * math.cos(r), CORE[1] + 44 * math.sin(r)
        d.rectangle([x - 4, y - 4, x + 4, y + 4], fill=(60, 50, 30))


def target(img, x, y, alpha=230):
    def f(d):
        d.ellipse([x - 54, y - 54, x + 54, y + 54], outline=GOLD + (alpha,), width=3)
        for a in range(0, 360, 90):
            r = math.radians(a)
            d.line([(x + 46 * math.cos(r), y + 46 * math.sin(r)), (x + 64 * math.cos(r), y + 64 * math.sin(r))],
                   fill=GOLD + (alpha,), width=3)

    glow_layer(img, f, blur=3)


def c_panels():
    src = base()
    disc = core_disc(src)

    p1 = src.copy()
    for a in (45, 135, 225, 315):
        r = math.radians(a)
        fx, fy = CORE[0] + 50 * math.cos(r), CORE[1] + 50 * math.sin(r)
        glow_layer(p1, lambda d, fx=fx, fy=fy: d.ellipse([fx - 9, fy - 9, fx + 9, fy + 9], fill=(255, 235, 190, 230)), blur=5)
    target(p1, *STOPS[1])
    caption(p1, ["tell: the cradle's clamps blow (four flashes) and a", "gold ring marks the next stop, 1 s ahead"])

    p2 = src.copy()
    cradle(p2)
    mx, my = (CORE[0] + STOPS[1][0]) / 2, (CORE[1] + STOPS[1][1]) / 2
    glow_layer(p2, lambda d: [d.ellipse([mx + 18 * i - 14 + 0, my + 4 * i - 14, mx + 18 * i + 14, my + 4 * i + 14],
                                        fill=(255, 200, 90, 70 - 12 * i)) for i in range(1, 6)], blur=6)
    target(p2, *STOPS[1], alpha=150)
    put(p2, disc, mx, my)
    caption(p2, ["it flies there in 1.5 s on its thrusters, leaving the", "empty cradle; shots still hit it on the way"])

    p3 = src.copy()
    cradle(p3)
    put(p3, disc, *STOPS[1])
    spiral(p3, STOPS[1], t_end=2.4, phase=40)
    glow = sprite("iron-sovereign-bay-glow_1")
    bx, by = STOPS[1][0] + 42, STOPS[1][1] + 42
    put(p3, glow, bx, by, 1.4)
    for i in range(3):
        dart(p3, bx + 20 + 22 * i, by + 18 + 16 * i, 40)
    caption(p3, ["at the stop it holds 6-9 s (seeded) and fires from", "there: the spiral, the nearest bay's Darts, its rail"])

    p4 = src.copy()
    cradle(p4)
    target(p4, *STOPS[4])
    put(p4, disc, STOPS[4][0] - 20, STOPS[4][1] - 40)
    telegraph(p4, (STOPS[4][0] - 20, STOPS[4][1] - 40), line_to_edge((STOPS[4][0] - 20, STOPS[4][1] - 40),
                                                                     math.atan2(SHIP[1] - STOPS[4][1] + 40, SHIP[0] - STOPS[4][0] + 20)))
    caption(p4, ["the next stop: never the same twice; a low stop brings", "the core (and its 72 px no-spawn ring) near the ship"])

    p5 = schematic_c(src)
    for i, p in enumerate((p1, p2, p3, p4), 1):
        num(p, i)
    return [p1, p2, p3, p4, p5]


def schematic_c(src):
    img = src.copy().point(lambda v: v // 3)
    BODY.draw(img, 12, 34, "THE FIVE STOPS, TWO SEEDS", GOLD)
    d = ImageDraw.Draw(img)
    for i, (x, y) in enumerate(STOPS):
        d.ellipse([x - 16, y - 16, x + 16, y + 16], outline=WHITE, width=2, fill=(12, 14, 24))
        LABEL.draw(img, x - 4, y - 5, "CABDE"[i], WHITE)
    for row, (seed, col, off) in enumerate(((11, (120, 220, 255), -4), (77, GOLD, 4))):
        rng = random.Random(seed)
        route, cur = [0], 0
        while len(route) < 7:
            nxt = rng.randrange(5)
            if nxt != cur:
                route.append(nxt)
                cur = nxt
        pts = [(STOPS[k][0] + off, STOPS[k][1] + off) for k in route]
        d.line(pts, fill=col, width=2)
        LABEL.draw(img, 12, 400 + row * 18, f"seed {seed}: " + " > ".join("CABDE"[k] for k in route), col)
    LABEL.draw(img, 12, 440, "C the cradle, A/B over the rings high, D/E low over")
    LABEL.draw(img, 12, 454, "the outer ring (y 280: 170 px above the ship band);")
    LABEL.draw(img, 12, 468, "the bays, spiral and rail move with the core.")
    LABEL.draw(img, 12, 520, "5  two seeds fly different routes")
    return img


TITLES = {
    "a": "a  THE PLAYBOOK: a random attack from a pool of four, each with its own tell",
    "b": "b  THE WRECK THROW: the dead batteries' wrecks torn loose and hurled at the ship",
    "c": "c  THE CORE BREAKS LOOSE: the reactor flies between five stops, picked at random",
}


def strip(name, panels, cols=5):
    gap = 8
    rows = (len(panels) + cols - 1) // cols
    out = Image.new("RGB", (cols * 480 + (cols - 1) * gap, 30 + rows * (540 + gap) - gap), BG)
    BODY.draw(out, 8, 4, TITLES[name], GOLD)
    for i, p in enumerate(panels):
        out.paste(p, ((i % cols) * (480 + gap), 30 + (i // cols) * (540 + gap)))
    return out


def main():
    strips = {}
    for name, fn in (("a", a_panels), ("b", b_panels), ("c", c_panels)):
        panels = fn()
        strip(name, panels, cols=3).save(OUT / f"sovereign-core-r70-{name}.png", optimize=True)
        s = strip(name, panels)
        strips[name] = s.resize((s.width // 2, s.height // 2), Image.LANCZOS)
    w = strips["a"].width
    out = Image.new("RGB", (w, sum(s.height + 6 for s in strips.values())), BG)
    y = 0
    for s in strips.values():
        out.paste(s, (0, y))
        y += s.height + 6
    out.save(OUT / "sovereign-core-r70-abc.png", optimize=True)


if __name__ == "__main__":
    main()
