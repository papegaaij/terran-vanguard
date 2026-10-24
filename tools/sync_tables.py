#!/usr/bin/env python3
"""Render the marked tables in the design READMEs from the data files next to them.

A generated table sits between two markers in a README:

    <!-- data: waves -->
    | t (s) | Section | ... |
    <!-- /data -->

The name after "data:" picks a renderer below; it reads the data.yaml in the README's own
directory (and, where it needs them, other data files: an enemy's bounty for a level's credit
budget, the difficulty levers for an enemy's HP). Everything outside the markers is hand-written.
The schemas are documented in design/tech/architecture/README.md#data-file-schemas.

Usage:
  python3 tools/sync_tables.py          rewrite every marked table from its data
  python3 tools/sync_tables.py --check  list the tables that differ from their data, exit 1 if any
"""
import os
import re
import textwrap
import sys

from design_data import (DESIGN, ROOT, base_angle, difficulty_hp, draw, enemy_dir, fill, grouped, load, num,
                         round_half_up, stats, upgrade_costs, weapon_rules)

BLOCK = re.compile(r"(<!-- data: ([a-z-]+) -->\n)(.*?)(<!-- /data -->)", re.S)


def row(cells):
    return "|" + "|".join(f" {c} " if c != "" else " " for c in cells) + "|"


def table(head, rows):
    return "\n".join([row(head), "|" + "---|" * len(head)] + [row(r) for r in rows])


def ticks(names):
    return ", ".join(f"`{n}`" for n in names)


def price(p):
    return "starter" if p == 0 else grouped(p)


def available(item):
    note = item.get("notes", {}).get("available")
    return item["available"] + (f" {note}" if note else "")


def data(d):
    return load(f"{d}/data.yaml")


def scoring():
    return load("systems/scoring/data.yaml")


def pickups():
    return load("player/data.yaml")["pickups"]


def pickup_credits(name):
    """Credits of a credit pickup named in a level ("small salvage")."""
    size, kind = name.split(" ", 1)
    return pickups()[kind]["credits"][size]


# --- player equipment -------------------------------------------------------------------------

def level_rows(w):
    """The per-level table rows of a weapon: levels 1-5 and the overdrive pattern."""
    rows = []
    for i, lv in enumerate(w["levels"] + [w["overdrive"]], start=1):
        volley, single = stats(w, lv)
        od = i == 6
        rows.append([
            "OD" if od else str(i), str(len(lv["pattern"])), pattern_text(w, lv), num(lv["rate"]),
            num(lv["damage"]), f"{volley:.1f}", f"{single:.1f}", extra_text(lv),
            "— (ignores cap)" if od else num(draw(w, i)),
            "—" if od or i == 1 else grouped(upgrade_costs(w)[i - 2]),
        ])
    return rows


def beam_rows(w):
    """M7 part A: a beam's per-level rows: width and damage per second (its volley and single-target DPS)."""
    rows = []
    for i, lv in enumerate(w["levels"] + [w["overdrive"]], start=1):
        od = i == 6
        rows.append(["OD" if od else str(i), num(lv["width"]), num(lv["damage"]), f"{lv['damage']:.1f}",
                     f"{lv['damage']:.1f}", "— (ignores cap)" if od else num(draw(w, i)),
                     "—" if od or i == 1 else grouped(upgrade_costs(w)[i - 2])])
    return rows


def deflector_rows(w):
    """M7 part A: a deflector's per-level rows: field radius, charges, recharge and the bullets it catches
    in 2 s when it is caught on every charge's return."""
    rows = []
    for i, lv in enumerate(w["levels"] + [w["overdrive"]], start=1):
        od = i == 6
        sustained = 2 / lv["recharge"]
        rows.append(["OD" if od else str(i), num(lv["radius"]), str(lv["charges"]), f"{num(lv['recharge'])} s",
                     f"{sustained:.1f}".rstrip("0").rstrip("."), "— (ignores cap)" if od else num(draw(w, i)),
                     "—" if od or i == 1 else grouped(upgrade_costs(w)[i - 2])])
    return rows


def drone_rows(w):
    """M7 part A: a drone bay's per-level rows: its shots and the drone's hits and rebuild time."""
    rows = []
    for i, lv in enumerate(w["levels"] + [w["overdrive"]], start=1):
        volley, single = stats(w, lv)
        od = i == 6
        rows.append(["OD" if od else str(i), num(lv["rate"]), num(lv["damage"]), f"{volley:.1f}", f"{single:.1f}",
                     f"{lv['drone_hits']} hits", f"{num(lv['rebuild'])} s",
                     "— (ignores cap)" if od else num(draw(w, i)),
                     "—" if od or i == 1 else grouped(upgrade_costs(w)[i - 2])])
    return rows


def depth_charge_rows(w):
    """M8 part A: the Depth Charges' per-level rows: the charges of a volley, their fuse, the volleys, the damage of
    a charge, its DPS and its burst radius."""
    rows = []
    for i, lv in enumerate(w["levels"] + [w["overdrive"]], start=1):
        volley, single = stats(w, lv)
        od = i == 6
        rows.append(["OD" if od else str(i), str(len(lv["pattern"])), f"{num(lv['fuse'])} s", num(lv["rate"]),
                     num(lv["damage"]), f"{volley:.1f}", f"{single:.1f}", f"{num(lv['blast'])} px",
                     "— (ignores cap)" if od else num(draw(w, i)),
                     "—" if od or i == 1 else grouped(upgrade_costs(w)[i - 2])])
    return rows


def per_level_rows(w):
    """A weapon's per-level rows as its table shows them (M7 part A: a beam's, a deflector's, a drone bay's; M8 part
    A: the depth charges')."""
    if w["hits"] == "beam":
        return beam_rows(w)
    if w["hits"] == "deflector":
        return deflector_rows(w)
    if w["hits"] == "depth-charge":
        return depth_charge_rows(w)
    return drone_rows(w) if "drone" in w else level_rows(w)


def weapon_levels(d):
    w = data(d)
    od_note = f"OD = the overdrive ({pickups()['overdrive']['seconds']} s pickup)."
    generated = "Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand."
    if w["hits"] == "beam":
        head = ["Level", "Width px", "Damage / s", "Volley DPS", "Single-target DPS", "Draw MW", "Upgrade cost"]
        return table(head, beam_rows(w)) + "\n\n" + (
            f"{generated} One beam: whatever it touches first takes all of its damage per second, so volley and "
            f"single-target DPS are the same. {od_note}")
    if w["hits"] == "deflector":
        head = ["Level", "Field radius px", "Charges", "One charge back every", "Bullets per 2 s (sustained)",
                "Draw MW", "Upgrade cost"]
        return table(head, deflector_rows(w)) + "\n\n" + (
            f"{generated} Values are per pod; it deals no damage. Bullets per 2 s: one caught on every charge's "
            f"return. {od_note}")
    if "drone" in w:
        head = ["Level", "Rate /s", "Damage", "Volley DPS", "Single-target DPS", "Drone breaks after", "Rebuild",
                "Draw MW", "Upgrade cost"]
        if w["drone"].get("station") == "trail":  # M7 part G: the rear-guard drone trails the ship, firing back
            return table(head, drone_rows(w)) + "\n\n" + (
                f"{generated} Values are per bay (one drone each). It flies {num(w['drone']['behind'])} px behind "
                f"the ship and fires straight back, its one shot a volley, so every shot counts as a single-target "
                f"hit. {od_note}")
        return table(head, drone_rows(w)) + "\n\n" + (
            f"{generated} Values are per bay (one drone each). Its shots aim at the convergence point "
            f"{num(w['drone']['converge'])} px ahead of the ship's centre, so every shot counts as a single-target "
            f"hit. {od_note}")
    if w["hits"] == "depth-charge":
        head = ["Level", "Charges", "Fuse", "Volleys /s", "Damage", "Volley DPS", "Single-target DPS", "Burst radius",
                "Draw MW", "Upgrade cost"]
        return table(head, depth_charge_rows(w)) + "\n\n" + (
            f"{generated} A charge's damage is dealt once to every target inside its burst radius (a boss part at "
            f"most one burst a charge), so against a group the volley counts more than the single-target DPS. "
            f"{od_note}")
    rows = level_rows(w)
    per = " Values are per pod." if w.get("pod") else (" Values are per side." if w.get("mirrored") else "")
    target = weapon_rules()["single_target"]
    head = ["Level", "Shots", "Pattern", "Rate /s", "Damage", "Volley DPS", "Single-target DPS", "Extra",
            "Draw MW", "Upgrade cost"]
    note = (f"Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand; the exact "
            f"offset and angle of every shot are in that file.{per} Single-target DPS counts the shots that hit a "
            f"{target['width']} px target {target['distance']} px from the muzzle"
            f"{' (seeking/lobbed: all shots)' if w.get('seek') else ''}. "
            f"OD = the overdrive pattern ({pickups()['overdrive']['seconds']} s pickup).")
    return table(head, rows) + "\n\n" + note


def pattern_text(w, lv):
    pat = lv["pattern"]
    if len(pat) == 1:
        return "single"
    xs = sorted({x for x, _ in pat})
    base = base_angle(w, lv)
    angles = sorted({round(a - base, 1) for _, a in pat})
    if len(angles) == 1:
        return "parallel " + ", ".join(f"{x:+g}" for x in xs) + " px"
    if len(xs) == 1:
        return f"fan ±{max(abs(v) for v in angles):g}°"
    return f"{len(pat)} mixed"


def extra_text(lv):
    parts = [f"{label} {lv[key]:g}{unit}" for key, label, unit in (
        ("pierce", "pierce", ""), ("blast", "blast", " px"), ("turn", "turn", "°/s"), ("max_live", "max live", ""))
        if key in lv]
    return ", ".join(parts) or "—"


def weapon_properties(d):
    w = data(d)
    slot = {"front": "Front gun", "rear": "Rear gun", "wing": "Wing mount (per pod)"}[w["slot"]]
    speed = w.get("speed")
    if speed is None:
        speed_text = "lobbed / dropped"
    elif isinstance(speed, list):
        speed_text = f"{num(speed[0])} → {num(speed[1])} px/s"
    else:
        speed_text = f"{num(speed)} px/s"
    rng = w.get("range")
    if w["hits"] == "depth-charge":  # M8 part A: tossed, a fuse per level instead of a range
        fuses = [lv["fuse"] for lv in w["levels"]]
        blasts = [lv["blast"] for lv in w["levels"]]
        # M8 AW (the user, 2026-10-23): tossed forward (angle 0) instead of rolled out backwards (180).
        way = "backwards from the stern" if abs(w["levels"][0]["pattern"][0][1]) > 90 else \
            "tossed forward from the stern rack over the hull"
        speed_text = (f"{num(speed)} px/s {way}, slowing to a hover over {num(w['drift'])} s, then "
                      "sinking with the scroll")
        range_text = (f"a burst on its fuse ({num(fuses[0])} → {num(fuses[-1])} s) of {num(blasts[0])} → "
                      f"{num(blasts[-1])} px radius")
    elif rng == "screen":
        range_text = "until off-screen"
    elif rng == "drop":
        range_text = "drops straight down"
    elif rng is None:
        range_text = f"{num(w['lifetime'])} s lifetime"
    elif "lifetime" in w:
        range_text = f"{num(w['lifetime'])} s lifetime, seeking within {num(rng)} px"
    else:
        range_text = f"{num(rng)} px"
    costs = " / ".join(grouped(c) for c in upgrade_costs(w))
    rows = [
        ["Slot", slot],
        ["Traits", ticks(w["traits"])],
        ["Layers hit", w["notes"]["layers_hit"]],
        ["Price", "starter (always owned)" if w["price"] == 0 else f"{grouped(w['price'])} cr"],
        ["Upgrade cost L2 / L3 / L4 / L5", f"{costs} cr (sell-back per [economy](../../../systems/economy/README.md))"],
        ["Unlock", "start" if w["unlock"] == 1 else f"L{w['unlock']:02d} (in the shop from the hangar visit before it)"],
        ["Projectile", f"{speed_text}, {w['size'][0]}×{w['size'][1]} px, "
                       + (range_text if w["hits"] == "depth-charge" else f"range {range_text}")],
        ["Sound family", f"`{w['sfx']}` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families))"],
        ["Projectile family (VFX)",
         f"`{w['vfx']}` ({w.get('notes', {}).get('vfx', 'projectile sheet of concept round 08')})"],
    ]
    return table(["Property", "Value"], rows)


def shields(d):
    rows = [[m["name"], num(m["capacity"]), num(m["regen"]), f"{m['delay']:.1f} s", f"{num(m['draw'])} MW",
             price(m["price"]), available(m)] for m in data(d)["models"]]
    return table(["Model", "Capacity", "Regen /s", "Regen delay", "Draw", "Price (first draft)", "Available"], rows)


def plating(d):
    rows = [[p["name"], num(p["max"]), price(p["price"]), available(p)] for p in data(d)["plating"]]
    return table(["Plating level", "Max armour", "Price (first draft)", "Available"], rows)


def generators(d):
    rows = [[m["name"], f"{num(m['output'])} MW", price(m["price"]), available(m)] for m in data(d)["models"]]
    return table(["Model", "Output", "Price (first draft)", "Available"], rows)


def engines(d):
    rows = [[m["name"], f"{num(m['speed'])} px/s", f"{num(m['draw'])} MW", price(m["price"]), available(m)]
            for m in data(d)["engines"]]
    return table(["Model", "Speed", "Draw", "Price (first draft)", "Available"], rows)


def bays(d):
    b = data(d)["bays"]
    rows = [[f"1–{b['start']}" if b["start"] > 1 else "1", "starter", "start"]]
    rows += [[str(b["start"] + i + 1), price(e["price"]), available(e)] for i, e in enumerate(b["extra"])]
    return table(["Bay", "Price", "Available"], rows)


def utility(d):
    rows = []
    for m in data(d)["utility"]:
        levels = "L1" if len(m["prices"]) == 1 else f"L1–L{len(m['prices'])}"
        note = m["notes"].get("levels")
        note = fill(note, m) if note else None
        rows.append([m["name"], fill(m["notes"]["effect"], m), levels + (f" {note}" if note else ""), num(m["draw"]),
                     " / ".join(grouped(p) for p in m["prices"]), available(m), m["design"]])
    return table(["Module", "Effect", "Levels", "Draw", "Price", "Unlock", "Design"], rows)


def player_pickups(d):
    rows = []
    for key, p in data(d)["pickups"].items():
        notes = p["notes"]
        name = notes.get("name", key.replace("_", " ").capitalize())
        rows.append([name, notes["source"], fill(notes["effect"], p)])
    return table(["Pickup", "Source", "Effect"], rows)


def ship_movement(d):
    ship = data(d)
    engine = next(e for e in load("player/systems/data.yaml")["engines"] if e["available"] == "start")
    values = ship | {"engine": engine, "centre_margin": ship["size"] / 2 + ship["edge_gap"]}
    rows = [[label, fill(text, values)] for label, text in ship["notes"]["movement"].items()]
    return table(["Property", "Value (first draft)"], rows)


# --- enemies ----------------------------------------------------------------------------------

def stat_block(d):
    e = data(d)
    notes = e["notes"]
    basis = load("enemies/data.yaml")
    levers = load("systems/difficulty/data.yaml")["enemy_hp"]
    values = e | {"contact_damage": basis["contact_damage"][e["tier"]]}
    bullet = {b["bullet"]: b["damage"] for b in basis["bullet_damage"]}

    def hp(level):
        return difficulty_hp(e["hp"], levers[level])

    def suffix(key):
        return f" {fill(notes[key], values)}" if key in notes else ""

    # A spawn or a pounce (M5 part C) has no bullet: its text names no {damage}.
    # M7 part C: an attack whose text is empty (the Tendril Anchor's hard orb, told in the link's) adds nothing.
    attacks = "; ".join(fill(a["notes"]["text"], values | a | {"damage": bullet.get(a.get("bullet"))})
                        for a in e["attacks"] if a["notes"]["text"])
    attacks = fill(notes.get("attack_prefix", ""), values) + attacks
    parts = notes["parts"] if e["parts"] == "multi" else e["parts"]
    hp_text = (fill(notes["hp"], values) + " " if "hp" in notes else "") + num(e["hp"])
    formations = ", ".join(f["name"] + (f" ({'–'.join(str(n) for n in f['size'])})" if "size" in f else "")
                           for f in e["formations"])
    weak = ", ".join(w["name"] + (f" (×{num(w['multiplier'])})" if "multiplier" in w else "")
                     for w in e["weak_points"]) or "none"
    rows = [
        ["Faction", e["faction"] + (" (machine: the EMP Burst stuns it longest)" if e.get("machine") else "")],
        ["Layer", f"`{e['layer']}`" + fill(notes.get("layer", ""), values)],
        ["Size tier", f"`{e['tier']}`"],
        ["Size", f"{e['size'][0]}×{e['size'][1]} px, hitbox {e['hitbox'][0]}×{e['hitbox'][1]}" + suffix("size")],
        ["Parts", parts],
        ["Orientation", f"`{e['orientation']}`" + suffix("orientation")],
        ["HP", f"{hp_text} (easy {hp('easy')} / hard {hp('hard')}, from the global multipliers)"],
        # M7 part E: an armour written as a key (the Dust Devil's `vanes`) is told by its notes
        ["Armour / shield", fill(notes["armour"], values) if "armour" in notes else e["armour"]],
        ["Speed", f"{num(e['speed'])} px/s" + suffix("speed")],
        ["Movement", fill(notes["movement"], values)],
        ["Attack", attacks or fill(notes["attack"], values)],
        ["Formations", formations + suffix("formations")],
        ["Weak points", weak + suffix("weak_points")],
        ["Effective traits", ticks(e["traits"])],
        ["Credits", f"{e['bounty']} (score {e['bounty'] * scoring()['kill_score']} × chain)" + fill(notes.get("bounty", ""), values)],
        ["Death", fill(notes["death"], values)],
        ["First level / used in", f"L{e['first_level']:02d}; {notes['used_in']}"],
        ["Difficulty hooks", notes["difficulty"]],
    ]
    return table(["Field", "Value"], rows)


def reference_dps(d):
    dps = data(d)["reference_dps"]
    return table(["Level"] + [f"{n:02d}" for n in dps], [["DPS"] + [num(v) for v in dps.values()]])


def player_damage(d):
    basis = data(d)
    rows = [[b["notes"]["label"], num(b["damage"]) + (f" {b['notes']['damage']}" if "damage" in b["notes"] else ""),
             b["notes"]["used_for"]] for b in basis["bullet_damage"]]
    contact = " · ".join(f"{tier} {num(v)}" for tier, v in basis["contact_damage"].items())
    rows.append(["Contact", contact, basis["notes"]["contact"]])
    return table(["Class", "Damage", "Used for"], rows)


def formations(d):
    return table(["Name", "Description"], [[f"`{n}`", text] for n, text in data(d)["formations"].items()])


# --- levels -----------------------------------------------------------------------------------

EDGES = {"left": "left", "right": "right", "alternating": "alternating edges"}
EVENTS = {"level-end": "Level end", "secondary-objective": "Secondary objective met"}
PORTRAITS = {"generic-cdf": "Generic CDF", "generic-civilian": "Generic civilian",
             "generic-colonial": "Generic colonial",
             "conamara": "Conamara Station",
             "thera-deep": "Thera Deep"}  # the generic portraits of unnamed speakers; M8 part B: Dr. Brandt's own; M8 part C: Thera Deep Control's


def timed_halt(section):
    """M5 part G: a section of speed 0 without an arena (Level 13's defend phase)."""
    return section.get("speed") == 0 and not section.get("arena")


def travelled(level, i, a, b):
    """The px section i scrolls from a to b s; around a timed halt the scroll eases (smoothstep) down to 0
    over the halt's ramp before it and back up over as long after it; M7 part B: a moving section's `ramp` eases
    from the speed before it over its first `ramp` s (LevelData.scrollAt)."""
    secs = level["sections"]
    s = secs[i]
    speed = s.get("speed", level["scroll_speed"])
    start = secs[i - 1]["end"] if i else 0
    px = (b - a) * speed

    def u(t, origin, ramp):
        return min(1.0, max(0.0, (t - origin) / ramp))

    def ease_in(x):   # ∫ S, lost against full speed while easing down
        return x ** 3 - x ** 4 / 2

    def ease_out(x):  # ∫ (1 - S), lost while easing up
        return x - x ** 3 + x ** 4 / 2

    if speed and i + 1 < len(secs) and timed_halt(secs[i + 1]):
        ramp = min(secs[i + 1].get("ramp", 2), s["end"] - start)
        px -= speed * ramp * (ease_in(u(b, s["end"] - ramp, ramp)) - ease_in(u(a, s["end"] - ramp, ramp)))
    if speed and i > 0 and timed_halt(secs[i - 1]):
        ramp = min(secs[i - 1].get("ramp", 2), s["end"] - start)
        px -= speed * ramp * (ease_out(u(b, start, ramp)) - ease_out(u(a, start, ramp)))
    elif speed and i > 0 and not timed_halt(s) and (not s.get("arena") or s.get("speed")) and s.get("ramp"):
        # M7 part B: a moving section's ramp eases from the speed before it over its first `ramp` s; M7 part H: a
        # moving (crawling) arena's too (Level 21's 140 -> 20 over 2 s)
        ramp = min(s["ramp"], s["end"] - start)
        before = secs[i - 1].get("speed", level["scroll_speed"])
        px -= (speed - before) * ramp * (ease_out(u(b, start, ramp)) - ease_out(u(a, start, ramp)))
    return px


def sections(level):
    """(start s, end s, start px, end px, speed) per section."""
    out, t, px = [], 0, 0
    for i, s in enumerate(level["sections"]):
        speed = s.get("speed", level["scroll_speed"])
        end_px = px + travelled(level, i, t, s["end"])
        out.append((t, s["end"], px, end_px, speed))
        t, px = s["end"], end_px
    return out


def atmosphere_text(section):
    """A section's atmosphere, with its heavier peak if it has one ("medium, heavy peak 140–148")."""
    peak = section.get("peak")
    return section["atmosphere"] + (f", {peak['atmosphere']} peak {num(peak['from'])}–{num(peak['to'])}"
                                    if peak else "")


def section_of(level, t):
    return next(i for i, (start, end, *_) in enumerate(sections(level), start=1) if start <= t < end)


def groups(wave):
    return wave.get("groups") or [{k: wave[k] for k in ("formation", "enemy", "count")}]


def enemy_link(d, slug):
    return f"[{enemy_name(slug)}]({os.path.relpath(DESIGN / enemy_dir(slug) / 'README.md', DESIGN / d)})"


def enemy_name(slug):
    return load(f"{enemy_dir(slug)}/data.yaml")["name"]


def medium_waves(level):
    """The waves flown at medium: a wave may `skip` difficulties (Level 06's hard-only pair)."""
    return [w for w in level["waves"] if "medium" not in w.get("skip", [])]


def refills(wave):
    """M7 part G: a pursuit's refills at medium, (how many, motes each): one every `refill_every` s after its entry
    while before its break-off, each the wave's or its unit's `refill` (Level 20's swarms; the flock's room aside)."""
    if "pursuit" not in wave:
        return 0, 0
    unit = load(f"{enemy_dir(wave['enemy'])}/data.yaml")["pursuit"]
    every = wave["pursuit"].get("refill_every", unit["refill_every"])
    each = wave["pursuit"].get("refill", unit["refill"])
    n = 0
    while wave["t"] + (n + 1) * every < wave["pursuit"]["break_at"]:
        n += 1
    return n, each


def group_count(wave, g):
    """A group's units at medium: its count, M7 part G: with a pursuit's refills."""
    n, each = refills(wave)
    return g["count"] + n * each


def enemy_totals(level):
    totals = {}
    for wave in medium_waves(level):
        for g in groups(wave):
            totals[g["enemy"]] = totals.get(g["enemy"], 0) + group_count(wave, g)
    return totals


def set_piece_totals(level):
    """The set pieces' enemies, one unit each (Level 03's Leviathan)."""
    totals = {}
    for piece in level.get("set_pieces", []):
        totals[piece["enemy"]] = totals.get(piece["enemy"], 0) + 1
    return totals


def boss_kind(slug):
    """A boss's kind as the wave table names it ("mid-boss")."""
    return load(f"{enemy_dir(slug)}/data.yaml")["boss"]["kind"]


def boss_name(level):
    """The level boss's name ("Harbour Kraken")."""
    return enemy_name(level["boss"]["enemy"])


def boss_totals(level):
    return {level["boss"]["enemy"]: 1} if "boss" in level else {}


def boss_streams(slug):
    """A boss's streams: (enemy, count) per phase that sends them."""
    return [(p["streams"]["enemy"], p["streams"]["count"])
            for p in load(f"{enemy_dir(slug)}/data.yaml")["boss"]["phases"] if "streams" in p]


def reverses(level):
    """M7 part G: whether a section scrolls backwards (a negative speed, Level 20's retreat)."""
    return any(s.get("speed", level["scroll_speed"]) < 0 for s in level["sections"])


def travel_at(level, t):
    """M7 part G: the direction of travel at t, +1 while the ground scrolls down the screen, -1 while it scrolls up."""
    return -1 if scroll_at(level, t + 0.001) < scroll_at(level, t) else 1


def dives(wave):
    """Whether a wave's enemy is a diver (a `movement.dive`, the Stinger): 2026-10-21 (user), never flipped in a
    reversed scroll, it enters from the top and dives at the ship as going forward (`WaveSchedule.flips`)."""
    movement = load(f"{enemy_dir(wave['enemy'])}/data.yaml").get("movement") if wave.get("enemy") else None
    return isinstance(movement, dict) and "dive" in movement


def unflipped(wave, level):
    """M7 part G: whether a front/rear wave of a reversed level enters by the screen, not by travel: a diver
    spawned while the travel is reversed (so its `from` reads the other way by travel)."""
    return reverses(level) and travel_at(level, wave["t"]) < 0 and dives(wave)


def entry(wave, level=None):
    """Where a wave enters: `sides` alone means both side edges, one side reads "left side"; M5 part G: a
    cross's `all` reads "all edges". M7 part G: in a level that scrolls backwards, front and rear read as the screen
    edge with their meaning by travel ("bottom (ahead)", "top (behind)")."""
    edge = wave.get("edge")
    if wave["from"] == "all":
        return "all edges"
    # M8 part E: a wave out of a cave's notch, or nests on the walls' feet (Level 25)
    if "cave" in wave:
        return f"side ({edge} cave)"
    if "wall" in wave:
        if wave["wall"] == "opposite":
            return "wall (left and right, opposite)"
        return f"wall ({edge})" if edge else "wall (both, staggered)"
    if wave["from"] == "sides" and edge in ("left", "right"):
        return f"{edge} side"
    if level is not None and reverses(level) and wave["from"] in ("front", "rear"):
        side = f", {EDGES[edge]}" if edge else ""
        if unflipped(wave, level):
            return f"{'top' if wave['from'] == 'front' else 'bottom'} (a diver, not flipped{side})"
        top = (wave["from"] == "front") == (travel_at(level, wave["t"]) > 0)
        return f"{'top' if top else 'bottom'} ({'ahead' if wave['from'] == 'front' else 'behind'}{side})"
    return wave["from"] + (f" ({EDGES[edge]})" if edge else "")


def directions(level):
    """The share of the level's enemies (medium) per entry direction, front · sides · rear."""
    counts = {"front": 0, "sides": 0, "rear": 0}
    for wave in medium_waves(level):
        n = sum(group_count(wave, g) for g in groups(wave))
        if wave["from"] == "all":  # M5 part G: a cross from all four edges, split evenly (two are the sides)
            counts["front"] += n / 4
            counts["sides"] += n / 2
            counts["rear"] += n / 4
        elif wave["from"] in ("front", "rear") and unflipped(wave, level):
            # 2026-10-21: a diver of a reversed section comes in at the top: behind by travel
            counts["rear" if wave["from"] == "front" else "front"] += n
        else:
            counts[wave["from"]] += n
    total = sum(counts.values())
    return " · ".join(f"{name} {round(100 * n / total)}%" for name, n in counts.items() if n)


def threat_profile(d):
    level = data(d)
    profile = level["threat_profile"]
    values = {**profile, "directions": directions(level), "traits": ", ".join(f"`{t}`" for t in profile["traits"])}
    rows = [[field, fill(text, values)] for field, text in profile["notes"].items()]
    return table(["Field", "Value"], rows)


def level_sections(d):
    level = data(d)
    rows = []
    for i, (s, (start, end, px0, px1, speed)) in enumerate(zip(level["sections"], sections(level)), start=1):
        layers = " ".join(f"`{layer}`: {text}" for layer, text in s["notes"]["layers"].items())
        # M7 part E: a level with a dust storm's `visibility` shows each section's radius at medium (its notes)
        storm = [s["notes"]["visibility"]] if "visibility" in level else []
        if reverses(level):  # M7 part G: signed scroll and speeds (Level 20), "2,800 → −3,110", "−130"
            def signed(text):
                return text.replace("-", "−")
            scroll = f"{signed(grouped(px0, ','))} → {signed(grouped(px1, ','))}"
            speed_text = signed(num(speed)) if speed < 0 else f"+{num(speed)}"
        else:
            scroll = f"{grouped(px0, ',')}–{grouped(px1, ',')}"
            speed_text = num(speed)
        rows.append([f"{i}. {s['name']}", f"{start}–{end}", scroll, speed_text,
                     atmosphere_text(s)] + storm + [layers, s["notes"]["purpose"]])
    storm_head = ["Visibility (medium)"] if "visibility" in level else []
    return table(["Section", "t (s)", "Scroll (px)", "Speed (px/s)", "Atmosphere"] + storm_head
                 + ["Layers and content", "Purpose"], rows)


BACKDROP_LAYERS = ["deep", "far", "ground", "low-air", "high-air"]


def scroll_at(level, t):
    """The ground's scroll distance at t s (the first and last section's speed carry on outside)."""
    for i, (start, end, px0, _, speed) in enumerate(sections(level)):
        if t < end or end == level["sections"][-1]["end"]:
            return px0 + travelled(level, i, start, t)


def time_at(level, px):
    """The time the ground has scrolled px (the first moment, a timed halt's start for its scroll)."""
    for start, end, px0, px1, speed in sections(level):
        if px < px1 or end == level["sections"][-1]["end"]:
            if not speed:
                return start
            lo, hi = start - 1e4, end + 1e4
            for _ in range(80):  # the scroll grows with t: bisect (M5 part G's eases)
                mid = (lo + hi) / 2
                lo, hi = (mid, hi) if scroll_at(level, mid) < px else (lo, mid)
            return (lo + hi) / 2


def on_screen(level, backdrop, placed):
    """When a set piece is on screen, (from, to) s within the level; a flying piece's flight."""
    if "path" in placed:
        return placed["path"][0][0], placed["path"][-1][0]
    piece = backdrop["pieces"][placed["piece"]]
    reach = (270 + piece["size"][1] / 2) / backdrop["scroll_factors"][piece["layer"]]
    centre = scroll_at(level, placed["t"])
    return max(0, time_at(level, centre - reach)), min(level["sections"][-1]["end"], time_at(level, centre + reach))


def backdrop_layers(b):
    """The backdrop's layer columns: M7 part C's `plateau` (a canyon level's) after the ground, where it has one."""
    if "plateau" not in b["scroll_factors"]:
        return BACKDROP_LAYERS
    return BACKDROP_LAYERS[:3] + ["plateau"] + BACKDROP_LAYERS[3:]


def backdrop_table(d):
    level = data(d)
    b = level["backdrop"]
    layers = backdrop_layers(b)
    cells = [{layer: [] for layer in layers} for _ in level["sections"]]
    for i, s in enumerate(level["sections"]):
        for tile in s["tiles"]:
            cells[i][b["tile_sets"][tile]["layer"]].append(f"`{tile}`")
    for placed in b["placed"]:
        start, end = on_screen(level, b, placed)
        repeat = placed.get("repeat")
        if repeat:  # a stream: from the first placement's start to the last one's end
            shift = (repeat["count"] - 1) * repeat["every"]
            last = dict(placed, t=placed["t"] + shift)
            if "path" in placed:
                last["path"] = [[p[0] + shift, p[1], p[2]] for p in placed["path"]]
            end = on_screen(level, b, last)[1]
        layer = b["pieces"][placed["piece"]]["layer"]
        when = f"flies {num(start)}–{num(end)}" if "path" in placed else f"{num(round(start))}–{num(round(end))}"
        if repeat:
            when += f" s, ×{repeat['count']} every {num(repeat['every'])}"
        cells[section_of(level, start) - 1][layer].append(f"{placed['piece']} {when} s")
    rows = []
    for i, s in enumerate(level["sections"]):
        def mix(name):
            look = b["atmosphere"][name]
            parts = [look[k] for k in ("banks", "wisps") if k in look]
            return ", ".join(parts + [f"haze {num(round(look['haze'] * 100))} %"])

        atmosphere = f"{s['atmosphere']}: {mix(s['atmosphere'])}"
        if "peak" in s:
            peak = s["peak"]
            atmosphere += f"; {peak['atmosphere']} peak {num(peak['from'])}–{num(peak['to'])} s: {mix(peak['atmosphere'])}"
        rows.append([f"{i + 1}. {s['name']}", atmosphere] + ["; ".join(cells[i][layer]) for layer in layers])
    return table(["Section", "Atmosphere"] + [f"`{layer}`" for layer in layers], rows)


def wall_x(points, row):
    """A wall's collision line at a row ([scroll, x(, height)] points, linear between them); None off it."""
    for a, b in zip(points, points[1:]):
        if a[0] <= row <= b[0]:
            return a[1] + (b[1] - a[1]) * (row - a[0]) / (b[0] - a[0])
    return None


def island_span(island, row):
    """An island's rock on a row ([x, scroll] polygon): (from, to), or None off it."""
    xs = []
    pts = island["points"]
    for a, b in zip(pts, pts[1:] + pts[:1]):
        lo, hi = sorted((a[1], b[1]))
        if lo <= row <= hi:
            xs += [a[0], b[0]] if a[1] == b[1] else [a[0] + (b[0] - a[0]) * (row - a[1]) / (b[1] - a[1])]
    return (min(xs), max(xs)) if xs else None


def terrain_table(d):
    """M7 part C: the walls' collision lines row by row (each wall point's row with the time it reaches the top
    edge), the flyable width (the channels beside an island), the notes by row; then the islands' outlines."""
    level = data(d)
    terrain = level["terrain"]
    left, right = terrain.get("left", []), terrain.get("right", [])
    islands = terrain.get("islands", [])
    notes = terrain.get("notes", {})
    rows = []
    first = min(p[0] for p in left + right)
    if first > 0 and "before" in notes:
        rows.append([f"0–{num(round(time_at(level, first), 1))} (0–{grouped(first, ',')})", "—", "—",
                     num(480), notes["before"]])
    for scroll in sorted({p[0] for p in left + right} | {p[1] for i in islands for p in i["points"]}):
        lx, rx = wall_x(left, scroll), wall_x(right, scroll)
        spans = [s for s in (island_span(i, scroll) for i in islands) if s]
        edges = [lx if lx is not None else 0] + [x for s in sorted(spans) for x in s] + [rx if rx is not None else 480]
        widths = [edges[k + 1] - edges[k] for k in range(0, len(edges), 2)]
        heights = {p[2] for p in left + right if p[0] == scroll and len(p) > 2}
        note = notes.get("rows", {}).get(scroll, "")
        if heights:
            note = "; ".join(filter(None, [note, f"wall height {num(heights.pop())}"]))
        rows.append([f"{num(round(time_at(level, scroll), 1))} ({grouped(scroll, ',')})",
                     "—" if lx is None else num(lx), "—" if rx is None else num(rx),
                     " + ".join(num(round(w)) for w in widths), note])
    out = table(["t at the top edge (scroll px)", "Left x", "Right x", "Flyable width", "Notes"], rows)
    for island in islands:
        outline = " → ".join(f"({num(p[0])}, {grouped(p[1], ',')})" for p in island["points"])
        out += f"\n\n**{island['name'][0].upper()}{island['name'][1:]}** (`{island['look']}`; x, scroll at the top edge): {outline}."
    notches = terrain.get("notches", [])
    if notches:
        # M8 part E: the side caves (Level 25), each a notch in its wall with the time it reaches the top edge
        listed = "; ".join(f"{k} {n['side']} {num(round(time_at(level, n['scroll']), 1))} ({grouped(n['scroll'], ',')})"
                           for k, n in enumerate(notches))
        sizes = {(n["width"], n["depth"]) for n in notches}
        size = " and ".join(f"{num(w)} px along the wall, {num(d)} deep" for w, d in sorted(sizes))
        out += (f"\n\n**Notches** (the side caves, {size}; index, wall, t at the top edge of the middle (scroll px)): "
                f"{listed}.")
    contact = terrain["contact"]
    damage = contact["damage"]
    out += (f"\n\n**Contact**: {num(damage['medium'])} per touch (easy {num(damage['easy'])}, hard {num(damage['hard'])}), "
            f"pushed {num(contact['push'])} px along the wall's normal over {num(contact['push_seconds'])} s, then a "
            f"{num(contact['cooldown'])} s cooldown in which a touch only clamps.")
    return out


def waves(d):
    level = data(d)
    rows = []
    for w in level["waves"]:
        gs = groups(w)
        enter = entry(w, level)
        rows.append((w["t"], [num(w["t"]), str(section_of(level, w["t"])), " + ".join(dict.fromkeys(g["formation"] for g in gs)),
                              " + ".join(enemy_link(d, g["enemy"]) for g in gs),
                              " + ".join(str(g["count"]) for g in gs)
                              + (" + {} × {}".format(*refills(w)) if refills(w)[0] else ""),
                              enter, fill(w["notes"], w) if "notes" in w else ""]))
    # a set piece's passes, each a row at its window on screen (notes.t, "62–74")
    for piece in level.get("set_pieces", []):
        for p in piece["passes"]:
            notes = p["notes"]
            start = float(re.match(r"[\d.]+", str(notes["t"])).group(0))
            rows.append((start, [str(notes["t"]), str(p["section"]), "solo set piece",
                                 f"{enemy_link(d, piece['enemy'])} ({notes['name']})", "1", notes["from"],
                                 fill(notes.get("notes", ""), p)]))
    # a boss arrives at its time (Level 05's mid-boss), its streams in the notes
    boss = level.get("boss")
    if boss:
        notes = boss.get("notes", {}).get("waves", "")
        enter = boss.get("notes", {}).get("from", "front (descends from above)")
        rows.append((boss["t"], [num(boss["t"]), str(boss["section"]), boss_kind(boss["enemy"]),
                                 enemy_link(d, boss["enemy"]), "1", enter, notes]))
    rows = [cells for _, cells in sorted(rows, key=lambda r: r[0])]
    totals = " · ".join(f"{enemy_name(slug)} {n}"
                        for slug, n in (enemy_totals(level) | set_piece_totals(level) | boss_totals(level)).items())
    head = ["t (s)", "Section", "Formation", "Enemies (link)", "Count", "Enter from", "Notes"]
    return table(head, rows) + f"\n\nTotals: {totals}."


def ground_values(target):
    """A ground target's fields for its notes: its `count` (one per placement if not given), the
    credits of its drop, and for an enemy its stat block's `name` and `bounty`."""
    values = {"count": len(target["at"])} | target
    if target.get("drop", "").endswith("salvage"):  # a credit pickup (Level 09's cocoon drops an armour patch)
        values["drop_credits"] = pickup_credits(target["drop"])
    if "enemy" in target:
        e = load(f"{enemy_dir(target['enemy'])}/data.yaml")
        values |= {"name": e["name"], "bounty": e["bounty"]}
    return values


def placements(target):
    """The easy / hard placements of a ground target, if they differ ("; easy ×2, hard ×4")."""
    if target.get("link") == "pairs":  # M7 part C: a gate's anchors, in pairs ("; hard 2 pairs")
        changes = [f"{level} {len(target[level]['at']) // 2} pair{'s' if len(target[level]['at']) > 2 else ''}"
                   for level in ("easy", "hard") if "at" in target.get(level, {})]
        return "; " + ", ".join(changes) if changes else ""
    changes = [f"{level} ×{len(target[level]['at'])}" for level in ("easy", "hard") if "at" in target.get(level, {})]
    return "; " + ", ".join(changes) if changes else ""


def ground_targets(d):
    level = data(d)
    rows = []
    for g in level["ground_targets"]:
        values = ground_values(g)
        rows.append([str(g["section"]), fill(g["notes"]["target"], values) + placements(g),
                     fill(g["notes"]["effect"], values)])
    bursts = level.get("root_bursts")
    if bursts:  # M5 part H: the root bursts' growths (Level 14), one row
        times = ", ".join(num(b["t"]) for b in bursts["bursts"])
        first = bursts["bursts"][0]
        growths = " + ".join(enemy_link(d, slug) for slug in first["growths"])
        easy = first.get("easy", {}).get("growths")
        easy_text = f"; easy {' + '.join(enemy_name(slug) for slug in easy)}" if easy else ""
        hard = bursts.get("hard", {}).get("first_shot_delay")
        hard_text = f" (hard {num(hard)} s)" if hard is not None else ""
        rows.append([", ".join(sorted({str(section_of(level, b["t"])) for b in bursts["bursts"]})),
                     f"Root bursts ×{len(bursts['bursts'])} (t≈{times}), each growing {growths} after a "
                     f"{num(bursts['telegraph'])} s dust telegraph{easy_text}",
                     f"Invulnerable and silent for {num(bursts['first_shot_delay'])} s{hard_text}; the secondary's "
                     f"group `{bursts['group']}`"])
    return table(["Section", "Target", "Effect"], rows)


def group_noun(level):
    """What the secondary objective's groups are: the first word their names share ("Dock One",
    "Dock Two": "dock"), else "group"."""
    words = {name.split()[0] for name in level["objectives"]["secondary"].get("groups", [])}
    return words.pop().lower() if len(words) == 1 else "group"


def radio(d):
    level = data(d)
    rows = []
    for cue in level["radio"]:
        if "t" in cue:
            note = cue.get("notes", {}).get("trigger")
            trigger = f"t={num(cue['t'])}" + (f" ({note})" if note else "")
        elif "trigger" in cue.get("notes", {}):
            trigger = cue["notes"]["trigger"]
        elif cue["event"] == "first-kill":
            trigger = f"First {enemy_name(cue['enemy'])} destroyed"
        elif cue["event"] == "enemy-escaped":
            trigger = f"First {enemy_name(cue['enemy'])} leaves the screen"
        elif cue["event"] == "group-cleared":
            trigger = f"{cue['group']} cleared"
        elif cue["event"] == "group-lost":
            trigger = f"{cue['group']} lost"
        elif cue["event"] == "first-group-lost":
            trigger = f"First {group_noun(level)} lost"
        else:
            trigger = EVENTS[cue["event"]]
        speaker = cue["speaker"] + (" (distorted)" if cue.get("distorted") else "")
        if "portrait" in cue:
            speaker = f"{PORTRAITS[cue['portrait']]} ({speaker})"
        line = f'"{cue["line"]}"' + "".join(
            f'; {level}: "{cue[level]["line"]}"' for level in ("easy", "hard") if level in cue
        )
        rows.append([trigger, speaker, line])
    return table(["Trigger", "Speaker", "Line"], rows)


SPEAKERS = {"Okafor": "Commander Okafor", "Varga": "Dr. Varga", "Rook": "Rook"}


def quote(pages):
    """Briefing pages as a block quote, one paragraph per page with its speaker."""
    paragraphs = []
    for page in pages:
        text = f'**{SPEAKERS.get(page["speaker"], page["speaker"])}:** "{page["line"]}"'
        paragraphs.append("\n".join("> " + line for line in textwrap.wrap(text, 94, break_on_hyphens=False, break_long_words=False)))
    return "\n>\n".join(paragraphs)


def briefing(d):
    return quote(data(d)["briefing"]["pages"])


def teaser(d):
    return quote([data(d)["briefing"]["teaser"]])


def act_title_card(d):
    card = data(d)["title_card"]
    return f"> {card['act']}\n> {card['name']}\n> {card['line']}"


def act_briefing(d):
    return quote(data(d)["briefing"])


def act_outro(d):
    return quote(data(d)["outro"]["pages"])


def act_outro_pages(d):
    """The act-end outro's pages: the speaker (with the expression) and the image with its description."""
    rows = []
    for i, page in enumerate(data(d)["outro"]["pages"], start=1):
        speaker = SPEAKERS.get(page["speaker"], page["speaker"])
        expression = page.get("expression")
        rows.append([str(i), speaker + (f" ({expression})" if expression else ""),
                     f"`{page['image']}`: {page['notes']['image']}"])
    return table(["Page", "Speaker", "Image"], rows)


# --- Rook (design/player/wingmen) -------------------------------------------------------------

def rook_guns(d):
    """Rook's guns: each a player weapon's per-level table with the damage scaled, at the price factor."""
    guns = data(d)["guns"]
    factor = guns["price_factor"]
    rows = []
    for i, gun in enumerate(guns["list"]):
        w = load(f"player/weapons/{gun['base']}/data.yaml")
        dps = [stats(w, w["levels"][lv])[0] * gun["scale"] for lv in (0, len(w["levels"]) - 1)]
        base = w.get("upgrade_base", w["price"])
        upgrades = " / ".join(grouped(base * factor * f) for f in weapon_rules()["upgrade_cost_factors"])
        note = gun.get("notes", {}).get("scale")
        rows.append([
            gun["name"], f"`{gun['id']}`", f"[{w['name']}](../weapons/{gun['base']}/README.md)",
            f"× {num(gun['scale'])}" + (f" {note}" if note else ""),
            f"{round_half_up(dps[0])} → {round_half_up(dps[1])}",
            "free (fitted when he joins)" if i == 0 else grouped(w["price"] * factor), upgrades, available(gun),
        ])
    return table(["Gun", "Id", "Based on", "Scale", "DPS L1 → L5", "Price", "Upgrades L2 / L3 / L4 / L5",
                  "Available"], rows)


def rook_barks(d):
    """Rook's radio barks in priority order: the trigger (its numbers filled in) and the variants."""
    barks = data(d)["barks"]
    rows = []
    for i, bark in enumerate(barks["triggers"], start=1):
        values = {k: (f"{round_half_up(v * 100)} %" if k == "below" else num(v)) for k, v in bark.items()
                  if k in ("ahead", "below", "kills", "seconds")}
        trigger = fill(bark["notes"]["trigger"], values) + (" (shouted)" if bark.get("shout") else "")
        variants = " · ".join(f'"{line}"' for line in bark["lines"])
        if bark.get("top_lines"):  # M7 part G: the rear wave's variants in a reversed section (Level 20)
            variants += "; in a reversed section, from the top: " + " · ".join(f'"{line}"' for line in bark["top_lines"])
        rows.append([str(i), trigger, variants])
    return table(["Priority", "Trigger", f"Variants (speaker {barks['speaker']})"], rows)


def level_number(d):
    return int(re.match(r"level-(\d+)-", os.path.basename(d)).group(1))


def stat_bounty(slug):
    return load(f"{enemy_dir(slug)}/data.yaml")["bounty"]


def spawns(slug):
    """An enemy's spawn attacks' `spawn` blocks (the Brood Pod's Skitters)."""
    return [a["spawn"] for a in load(f"{enemy_dir(slug)}/data.yaml").get("attacks", []) if a.get("spawn")]


def part_names(parts):
    """Boss parts as the credit table names them: equal names but a side ("bay 1 left", "bay 1
    right", …) and equal bounties grouped ("bays 8 × 25"), the others "core 250"."""
    out, groups = [], {}
    for p in parts:
        base = re.sub(r" \d+( left| right)?$| (left|right)$", "", p["name"])
        groups.setdefault((base, p["bounty"]), []).append(p)
    for (base, credits), members in groups.items():
        out.append(f"{base}s {len(members)} × {credits}" if len(members) > 1 else f"{members[0]['name']} {credits}")
    return out


def whole_part(e):
    """M7 part D: the vital part of a unit that is not a segment chain whose vital part pays a first bonus (the
    Shellback's body: a body-first kill pays the whole); None for another unit."""
    if e.get("segment_chain"):
        return None
    return next((p for p in e.get("part_list", []) if p.get("kind") == "vital" and "first_bonus" in p), None)


def credit_budget(d):
    """The level's credits at medium: a perfect run, and the typical haul (each source weighted by
    the economy's typical player) that should land on budget(n). Bounties (kills, parts, ground
    targets) are × the act factor × the level's bounty_scale, rounded half to even per payout."""
    level = data(d)
    economy = load("systems/economy/data.yaml")
    n_level = level_number(d)
    budget = economy["budget"]["base"] * economy["budget"]["growth"] ** (n_level - 1)
    rate = economy["typical_player"]
    act = int(re.search(r"act-(\d+)-", d).group(1))
    factor = economy["act_factor"] ** (act - 1)
    scale = level.get("bounty_scale", 1)

    def bounty(b):
        return round(b * factor * scale)

    def pay(c):
        return round(c * factor)

    def layer_rate(slug):
        return rate["ground_targets"] if load(f"{enemy_dir(slug)}/data.yaml")["layer"] == "ground" else rate["air_kills"]

    rows = []  # [source, perfect, typical]

    def add(source, items):
        """items: (count, credits per payout, share the typical player collects[, False: not in a perfect run])."""
        rows.append([source, sum(i[0] * i[1] for i in items if len(i) < 4 or i[3]),
                     sum(i[0] * i[1] * i[2] for i in items)])

    kills = [(enemy_name(slug), n, slug) for slug, n in enemy_totals(level).items()]
    # the units a spawner releases (Level 04's Brood Pods: their Skitters), killed too
    for slug, n in enemy_totals(level).items():
        for spawn in spawns(slug):
            kills.append((f"released {enemy_name(spawn['enemy'])}", n * spawn["count"], spawn["enemy"]))
    # a periodic spawner among the ground targets (Level 09's Hive Nodes, M5 part C): one release each
    released = {}
    for g in level["ground_targets"]:
        for spawn in spawns(g["enemy"]) if "enemy" in g else []:
            if "every" in spawn:
                released[spawn["enemy"]] = released.get(spawn["enemy"], 0) + len(g["at"]) * spawn["count"]
    kills += [(f"released {enemy_name(slug)}", n, slug) for slug, n in released.items()]

    def kill_items(n, slug):
        """A kill's payouts; a segment chain's head, segments and tail each pay (and round) their own. M7 part C: a
        chain whose head pays a first bonus (the Threadcrawler's whole 48) is taken apart in a perfect run (the tail
        first, with its bonus) and killed head first by the typical player."""
        e = load(f"{enemy_dir(slug)}/data.yaml")
        chain = e.get("segment_chain")
        vital = whole_part(e)
        if not chain and vital:  # M7 part D: the Shellback, its vent first in a perfect run, body first typically
            others = [p for p in e["part_list"] if p is not vital]
            return ([(n, bounty(vital["bounty"]), layer_rate(slug)),
                     (n, bounty(vital["first_bonus"]), layer_rate(slug), False)]
                    + [(n, bounty(p["bounty"]), 0) for p in others])
        if not chain:
            return [(n, bounty(e["bounty"]), layer_rate(slug))]
        head, tail = e["part_list"][0], e["part_list"][1]
        if "first_bonus" in head:
            return [(n, bounty(head["bounty"]), layer_rate(slug)),
                    (n, bounty(head["first_bonus"]), layer_rate(slug), False),
                    (n * chain["segments"], bounty(chain["bounty"]), 0),
                    (n, bounty(tail["bounty"]), 0),
                    (n, bounty(tail.get("first_bonus", 0)), 0)]
        return [(n, bounty(head["bounty"]), layer_rate(slug)),
                (n * chain["segments"], bounty(chain["bounty"]), layer_rate(slug)),
                (n, bounty(tail["bounty"]), layer_rate(slug))]

    def kill_name(name, n, slug):
        e = load(f"{enemy_dir(slug)}/data.yaml")
        head = e.get("part_list", [{}])[0]
        if e.get("segment_chain") and "first_bonus" in head:  # M7 part C: the Threadcrawler
            tail = e["part_list"][1]
            parts = e["bounty"] + tail.get("first_bonus", 0)
            return f"{name} {n} × {parts} by parts ({head['bounty'] + head['first_bonus']} whole)"
        vital = whole_part(e)
        if vital:  # M7 part D: a non-chain unit whose vital part pays a first bonus (the Shellback's whole 48)
            return f"{name} {n} × {e['bounty']} by parts ({vital['bounty'] + vital['first_bonus']} whole)"
        return f"{name} {n} × {stat_bounty(slug)}"

    add("Kills: " + " + ".join(kill_name(name, n, slug) for name, n, slug in kills),
        [item for _, n, slug in kills for item in kill_items(n, slug)])
    # ground enemies by their stat block's bounty, then the destructibles that pay or drop credits
    enemies = {}
    for g in level["ground_targets"]:
        if "enemy" in g:
            enemies[g["enemy"]] = enemies.get(g["enemy"], 0) + ground_values(g)["count"]
    for burst in level.get("root_bursts", {}).get("bursts", []):  # M5 part H: the growths (Level 14)
        for slug in burst["growths"]:
            enemies[slug] = enemies.get(slug, 0) + 1
    paying = [ground_values(g) for g in level["ground_targets"]
              if "enemy" not in g and ("bounty" in g or g.get("drop", "").endswith("salvage"))]
    parts = [f"{enemy_name(slug)} {n} × {stat_bounty(slug)}" for slug, n in enemies.items()]
    parts += [fill(g["notes"]["budget"], g) for g in paying]
    def ground_items(slug, n):
        """M8 part B: a ground target with unit parts (the Drill Rig) pays its body and each part, each rounded (the
        whole: the body's bounty and first bonus, as the parts' bounties); another by its stat block's bounty."""
        e = load(f"{enemy_dir(slug)}/data.yaml")
        vital = whole_part(e)
        if not vital:
            return [(n, bounty(stat_bounty(slug)), rate["ground_targets"])]
        return ([(n, bounty(vital["bounty"]), rate["ground_targets"])]
                + [(n, bounty(p["bounty"]), rate["ground_targets"]) for p in e["part_list"] if p is not vital])

    items = [item for slug, n in enemies.items() for item in ground_items(slug, n)]
    for g in paying:
        items.append((g["count"], bounty(g.get("bounty", 0)), rate["ground_targets"]))
        items.append((g["count"], pay(g.get("drop_credits", 0)), rate["ground_targets"] * rate["pickups"]))
    # M7 part C: a linked nest's tendrils burnt (the gates, in a perfect run; the typical player kills an anchor)
    burnt = {}
    for g in level["ground_targets"]:
        if g.get("link") == "pairs":
            link = next(a["link"] for a in load(f"{enemy_dir(g['enemy'])}/data.yaml")["attacks"] if a.get("link"))
            burnt[link["burnt_bounty"]] = burnt.get(link["burnt_bounty"], 0) + len(g["at"]) // 2
    for credits, n in burnt.items():
        parts.append(f"{n} tendrils burnt × {credits}")
        items.append((n, bounty(credits), 0))
    if parts:
        add("Ground targets: " + " + ".join(parts), items)
    # set pieces: their parts' bounties, and their death drop
    for piece in level.get("set_pieces", []):
        e = load(f"{enemy_dir(piece['enemy'])}/data.yaml")
        add(f"Set piece: {e['name']} parts",
            [(1, bounty(part["bounty"]), rate["ground_targets"]) for part in e["part_list"]])
        for drop in e.get("drops", []):
            if drop["pickup"].endswith("salvage"):
                add(f"Pickup: {e['name']} {drop['pickup']}",
                    [(1, pay(pickup_credits(drop["pickup"])), rate["ground_targets"] * rate["pickups"])])
    # the boss: its parts' bounties (it dies in every won run), and the streams of a fight at par
    boss = level.get("boss")
    if boss:
        e = load(f"{enemy_dir(boss['enemy'])}/data.yaml")
        paying = [p for p in e["part_list"] if p.get("bounty")]  # fire-only parts pay nothing
        names = " + ".join(part_names(paying))
        add(f"{boss_kind(boss['enemy']).capitalize()}: {e['name']} ({names})",
            [(1, bounty(p["bounty"]), 1) for p in paying])
        streams = boss.get("notes", {}).get("streams", 0)
        for enemy, count in boss_streams(boss["enemy"]):
            if streams:
                add(f"{boss_kind(boss['enemy']).capitalize()} streams: {streams} × {count} "
                    f"{enemy_name(enemy)} × {stat_bounty(enemy)} (a fight at par)",
                    [(streams * count, bounty(stat_bounty(enemy)), layer_rate(enemy))])
        # part G: the units its windows launch in a typical fight (Level 07's carrier)
        launched = boss.get("notes", {}).get("spawns", {})
        if launched:
            add(f"{boss_kind(boss['enemy']).capitalize()} spawns: "
                + " + ".join(f"{enemy_name(slug)} {n} × {stat_bounty(slug)}" for slug, n in launched.items())
                + " (a typical fight)",
                [(n, bounty(stat_bounty(slug)), layer_rate(slug)) for slug, n in launched.items()])
    escort = level["objectives"].get("escort")
    if escort:
        # a convoy on the road gives its y, an air escort its stations (M5 part D); the scripted
        # loss's unit (Level 10's Lifeline Three) is not one of the units the escort pays for
        units = len(escort["y"] if "y" in escort else escort["stations"])
        noun = escort["ally"].split("-")[-1]
        saveable = units - (1 if "scripted_loss" in escort else 0)
        unpaid = f" (of {units}: the scripted loss's {noun} is not paid)" if saveable < units else ""
        add(f"Primary objective: {saveable} {noun}s home × {escort['credits']}{unpaid}",
            [(saveable, pay(escort["credits"]), rate["primary"])])
    for s in level["secrets"]:
        if "data_core" in s:  # Level 06's settlement log: no credits, an early unlock
            add(f"Data core: {s['name']} (unlocks the {s['data_core']['unlocks']}; no credits)", [(1, 0, 0)])
            continue
        add(f"Secret: {s['name']} (hidden crate, {round(100 * s['crate'] / budget)}% of budget)",
            [(1, pay(s["crate"]), rate["secrets"])])
    secondary = level["objectives"].get("secondary")  # M5 part D: a level may have none (Level 10)
    count = 1
    if secondary is None:
        source = None
    elif secondary.get("links"):  # M7 part C (Level 16): every tendril broken
        source = f"Secondary: {secondary['name'][0].lower()}{secondary['name'][1:]} (every tendril broken)"
    elif "name" in secondary:  # the objective's own name (Level 09's "Hold the bridge")
        if "groups" in secondary:
            count = len(secondary["groups"])
        source = f"Secondary: {secondary['name'][0].lower()}{secondary['name'][1:]}"
    elif "groups" in secondary:
        count = len(secondary["groups"])
        source = f"Secondary: {count} {group_noun(level)}s × {secondary['credits']}"
    elif "escapes" in secondary and spawns(secondary["escapes"]):
        source = f"Secondary: every {enemy_name(secondary['escapes'])} killed before it bursts"
    elif "escapes" in secondary:
        source = f"Secondary: no {enemy_name(secondary['escapes'])} gets through"
    elif "parts" in secondary:
        source = (f"Secondary: all {len(secondary['parts'])} {secondary['label'].lower()} destroyed "
                  f"before the {secondary['before'].lower()} phase ends")
    elif secondary.get("afloat"):  # M5 part E (Level 11): the convoy's damageable units afloat at the boss's death
        units = [u for u in level["convoy"]["units"] if not u.get("leaves")]
        source = (f"Secondary: convoy afloat (all {len(units)} "
                  f"{units[0]['ally'].replace('-', ' ')}s afloat at the {boss_name(level)}'s death)")
    elif "kill_all" in secondary:
        names = " and ".join(enemy_name(slug) for slug in secondary["kill_all"])
        source = f"Secondary: every {names} destroyed"
    else:
        source = "Secondary objective"
    if secondary is not None:
        add(source, [(count, pay(secondary["credits"]), rate["secondary"])])
    perfect = sum(r[1] for r in rows)
    typical = sum(r[2] for r in rows)
    cells = [[source, grouped(p, ","), grouped(round(t), ",")] for source, p, t in rows]
    note = f" (bounty scale {scale})" if scale != 1 else ""
    return table(["Source", "Perfect run", "Typical haul"], cells + [
        [f"**Total**{note}", f"**{grouped(perfect, ',')}**", f"**{grouped(round(typical), ',')}**"],
        [f"Budget(n) = the typical haul's target; typical {100 * (typical - budget) / budget:+.0f} %, "
         f"perfect {perfect / budget:.2f} × budget", "", grouped(round(budget), ",")]])


# --- systems ----------------------------------------------------------------------------------

def score_bonuses(d):
    rows = []
    for b in data(d)["bonuses"]:
        score = ("kill % × " if b.get("per_kill_percent") else "") + grouped(b["points"])
        score += " × " + {"level": "level number", "act": "act"}[b["scale"]]
        rows.append([b["name"], b["notes"]["condition"], score])
    return table(["Bonus", "Condition", "Score"], rows)


def grades(d):
    rows, previous = [], None
    for g in data(d)["grades"]:
        rating = f"≥ {g['rating']}" if "rating" in g else f"< {previous}"
        bonus = f"+{round_half_up(100 * g['credit_bonus'])} %" if g["credit_bonus"] else "—"
        rows.append([g["grade"], rating, bonus])
        previous = g.get("rating")
    return table(["Grade", "Rating", "Credit bonus"], rows)


def difficulty(d):
    lv = data(d)
    order = ("easy", "medium", "hard")

    def factor(v):
        text = f"{v:.2f}".rstrip("0")
        return "× " + (text + "0" if text.endswith(".") else text)

    def change(v, at_least=None):
        if v == 0:
            return "base"
        sign = "+" if v > 0 else "−"
        text = f"{sign}{round_half_up(abs(v) * 100)} %"
        return text + (f" (at least {sign}{at_least})" if at_least else "")

    def per(key, fmt):
        return [fmt(lv[key][level]) for level in order]

    retries = [f"{lv['retries'][level]}, {lv['notes']['retries']}" if level in lv["retries"] else "unlimited"
               for level in order]
    rows = [
        ["Enemy HP"] + per("enemy_hp", factor),
        ["Enemy bullet speed"] + per("enemy_bullet_speed", factor),
        ["Enemy fire rate"] + per("enemy_fire_rate", factor),
        ["Bullets per `fan` / `ring` / `burst`"] + per("pattern_bullets", lambda v: change(v, 1)),
        ["Aimed shots"] + [lv["aimed_shots"][level] + (f", ±{num(lv['aimed_spread_degrees'][level])}° spread"
                                                       if lv["aimed_spread_degrees"][level] else "")
                           for level in order],
        ["Formation size"] + per("formation_size", change),
        ["Bullet budget (max enemy bullets on screen)"] + per("bullet_budget", num),
        ["Player shield regen"] + per("shield_regen", factor),
        ["Credit income"] + per("credit_income", factor),
        ["Score multiplier"] + per("score", factor),
        ["Armour repair in hangar"] + per("repair_cost", lambda v: f"{v} cr / point" if v else "free"),
        ["Retries per level"] + retries,
        ["Boss checkpoint"] + per("boss_checkpoint", lambda v: "yes" if v else "no"),
        ["Intel"] + per("sensor_bonus", lambda v: f"sensor level +{v}" if v else "normal"),
    ]
    return table(["Lever", "Easy", "Medium", "Hard"], rows)


def underwater(d):
    """M8 part A: Europa's under-water rules as the game reads them (design/world/europa/data.yaml)."""
    u = data(d)["underwater"]

    def names(slugs):
        return ", ".join(f"[{load(f'player/weapons/{slug}/data.yaml')['name']}]"
                         f"(../../player/weapons/{slug}/README.md)" for slug in slugs)

    rows = [
        ["Projectile speed", f"× {num(u['bullet_speed'])}: every bullet, the player's and the enemies', at its spawn"],
        ["Handling (drag)", f"acceleration and braking × {num(u['acceleration'])}, top speed unchanged (the pressure "
                            "hull removes it)"],
        ["Damage", f"× {num(u['damage'])} for a weapon without the `anti-sub` trait (the ship's and Rook's guns), "
                   "after `anti-ground`'s ×2; not an energy weapon"],
        ["Energy weapons", f"damage × {num(u['energy_damage'])} (in place of the guns' × {num(u['damage'])}), reach "
                           f"× {num(u['reach'])} of the play field's height: {names(u['energy_weapons'])}"],
        ["Torpedo-type range", f"× {num(u['torpedo_range'])}: {names(u['torpedo_weapons'])}"],
        ["Waterline ease", f"{num(u['ease'])} s, for new projectiles"],
        ["Hydro-kit refit", f"at the hangar visit before L{u['hydro_kit']:02d} (a one-off notice)"],
    ]
    return table(["Rule", "Number"], rows) + "\n\nGenerated from [data.yaml](data.yaml) by `tools/sync_tables.py`."


RENDERERS = {
    "underwater": underwater,
    "weapon-levels": weapon_levels, "weapon-properties": weapon_properties, "shields": shields,
    "plating": plating, "generators": generators, "engines": engines, "bays": bays, "utility": utility,
    "pickups": player_pickups, "ship-movement": ship_movement, "stat-block": stat_block,
    "reference-dps": reference_dps, "player-damage": player_damage, "formations": formations,
    "level-sections": level_sections, "backdrop": backdrop_table, "threat-profile": threat_profile, "waves": waves, "ground-targets": ground_targets, "radio": radio,
    "credit-budget": credit_budget, "briefing": briefing, "teaser": teaser, "terrain": terrain_table,
    "act-title-card": act_title_card, "act-briefing": act_briefing, "act-outro": act_outro, "act-outro-pages": act_outro_pages,
    "rook-guns": rook_guns, "rook-barks": rook_barks, "score-bonuses": score_bonuses, "grades": grades, "difficulty": difficulty,
}


def synced(readme):
    """The README's text with every marked table rendered from its data."""
    d = readme.parent.relative_to(DESIGN).as_posix()

    def render(m):
        name = m.group(2)
        if name not in RENDERERS:
            raise ValueError(f"unknown table '{name}'")
        return m.group(1) + RENDERERS[name](d) + "\n" + m.group(4)

    return BLOCK.sub(render, readme.read_text(encoding="utf-8"))


def marked_readmes():
    return [p for p in sorted(DESIGN.rglob("README.md")) if "<!-- data: " in p.read_text(encoding="utf-8")]


def main():
    check = "--check" in sys.argv[1:]
    stale = 0
    for readme in marked_readmes():
        new = synced(readme)
        if new != readme.read_text(encoding="utf-8"):
            stale += 1
            print(f"{'differs' if check else 'updated'}: {readme.relative_to(ROOT).as_posix()}")
            if not check:
                readme.write_text(new, encoding="utf-8")
    print(f"{stale} README(s) {'differ from their data' if check else 'updated'}")
    return 1 if check and stale else 0


if __name__ == "__main__":
    sys.exit(main())
