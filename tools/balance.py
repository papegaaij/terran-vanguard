#!/usr/bin/env python3
"""The balancing sheet for the player's equipment (Acts 1-4; Act 3 from M7 part A, Level 15's data from M7 part B;
Act 4's visits from M8 part A).

The numbers come from the parts' data files (design/player/**/data.yaml, the economy, the
difficulty levers, the enemies and the levels); the expected purchases per hangar visit are in
design/player/balance-plan.yaml. The weapon tables are rendered by tools/sync_tables.py.

The checks themselves are content tests (design/tech/architecture, balance checks as JUnit tests):
BalanceTest (this sheet's plan, DPS and enemy checks) and ActPlaythroughTest (Acts 1-3 flown with the
plan on every difficulty). This script prints the same sheet for reading:

Usage:
  python3 tools/balance.py              the sheet per visit, levels 01-28 (the plan's visits), and the enemy checks
  python3 tools/balance.py --weapons    print every weapon's per-level numbers

Per visit: the income of the level before it (the level's typical haul from its credit-budget
table, design/systems/economy, Per-level budget: every planned level has its data since M7 part H, so
the fallback to budget(n) is gone, M7 part I; M8 part A: TEMPORARILY budget(n) again for an Act 4 level that has
no data yet, until M8 part I; plus the typical player's grade bonus, a B: round 34
P3 = A), the plan's purchases (with the free charges a special gives at its unlock), the repair
estimate (the ship's, and Rook's after a level flown with him), the power load and the plan's forward
single-target DPS against the reference DPS the enemy stat blocks assume (design/enemies/data.yaml),
inside 0.75-1.33x (M8 part A: at the plan's `underwater` levels a weapon without the anti-sub trait, and Rook's
gun, counts at the water's damage factor of design/world/europa/data.yaml; the Bulwark's anti-sub cannon is the
escort's DPS while Rook is missing); Levels 01-03's gentle onboarding (up to 1.75x) is the accepted exception
(user decision 2026-10-01). Per level with data: the slowest time to kill among its enemies and its
boss's at the plan's DPS. Per enemy: the time to kill at its first level and the bounty against
the balancing basis (design/enemies/README.md); a deviation is listed, marked when it is an
accepted exception (the Coilwyrm's bounty, user decision 2026-10-05; the Ravager's and the Wraith's
times to kill, M5 parts C and D); BalanceTest holds the same ACCEPTED list and the rear checks the
user accepted (round 34 P4, REAR_PENDING). Per level with a rear ambush (M5 part D, D7 = a): each ambush unit's HP against the
plan's rear DPS and its decloak flash plus hold; BalanceTest checks HP / rear DPS <= 0.6 x that. M7 part E (step
E5): the beam era's table (beam_era_hp: the small units' HP doubled from Level 18, where the plan fits the Ion Beam;
a problem when the plan's first Ion Beam is at another level).

Exit status: 1 when the plan overspends, exceeds the power, buys an item before its unlock or
over a special's most charges, or the DPS leaves its band (other than the accepted exception).
"""
import re
import sys

from design_data import DESIGN, difficulty_hp, draw, enemy_dir, load, stats, upgrade_costs, weapons
from sync_tables import credit_budget, per_level_rows

ONBOARDING = {1, 2, 3}           # accepted: about 1.5x the reference DPS (design/enemies, Balancing basis)
DPS_BAND = (0.75, 1.33)
ONBOARDING_HIGH = 1.75
EFFECTIVE = 0.6                  # bosses and set pieces: effective DPS 0.6 x reference
WALKER_EFFICIENCY = 0.52         # M7 part F: a walking boss's share of the effective DPS that lands; step F4 measured 1.39 (52.7 s at 3,740 HP; the paper's 0.68 was on the screen 80 %, half of it facing its plate); round 43's rework (far more fire, the bigger cone) 1.08 (57.1 s at 3,170 HP); round 44 (pods armoured while shut, the rail lance, the stomp) 0.52 (50.8 s at 1,350 HP)
WALKER_INTRO = 4                 # s of a walking boss's intro pass before it can be hit (the walker's spec)
SHARE_TOLERANCE = 1 / 3          # a boss's or set piece's bounty share: "about" its target
# Deviations from the balancing basis the user accepted (BalanceTest.ACCEPTED holds the same list).
ACCEPTED = {
    ("coilwyrm", "bounty"): "a multi-part enemy, cutting it up is extra work; a head-first kill pays 40",
    ("ravager", "ttk"): "a fast, fragile pack hunter (user decision 2026-10-07, M5 part C)",
    ("wraith", "ttk"): "set for the plan's rear DPS with its rear check instead (user decision D7 = a, M5 part D)",
}
HOLD_SHARE = 0.6                 # a hold's cluster dies in at most this share of its window (BalanceTest, medium)
REAR_SHARE = 0.6                 # a rear-ambush unit dies in at most this share of its flash and hold (BalanceTest, medium)
# M5 part F: rear checks over REAR_SHARE the user accepted (round 34 P4; BalanceTest.REAR_PENDING), printed as accepted
REAR_PENDING = {(12, "wraith"): "the act HP factor on the Wraith, the rear still the Tail Gun L1 until L14; "
                                "accepted by the user (round 34 P4)"}
LATCHED_SHARE = 0.25             # a latched Lamprey dies in at most this share of its shake-off time (BalanceTest)


# M8 part A: the first level whose missing data falls back to budget(n) (temporary: M8 part I removes it).
FALLBACK_FROM = 22


def load_all():
    """Everything the report needs, gathered from the data files."""
    plan = load("player/balance-plan.yaml")
    economy = load("systems/economy/data.yaml")
    return {
        "plan": plan,
        "economy": economy,
        "availability": load("player/data.yaml")["availability"],
        "repair_cost": load("systems/difficulty/data.yaml")["repair_cost"][plan["difficulty"]],
        "weapons": weapons(),
        "core": {
            "generator": load("player/generator/data.yaml")["models"],
            "shield": load("player/shields/data.yaml")["models"],
            "armor": load("player/armor/data.yaml")["plating"],
            "engine": load("player/systems/data.yaml")["engines"],
        },
        "utility": {u["name"]: u for u in load("player/systems/data.yaml")["utility"]},
        "specials": {s["name"]: s for s in load("player/specials/data.yaml")["specials"]},
        "wingmen": load("player/wingmen/data.yaml"),
        "water": load("world/europa/data.yaml")["underwater"],
    }


def budget(data, n):
    b = data["economy"]["budget"]
    return b["base"] * b["growth"] ** (n - 1)


def level_dir(n):
    """The level's directory relative to design/, or None while it has no data file."""
    found = list((DESIGN / "campaign").glob(f"act-*/level-{n:02d}-*/data.yaml"))
    return found[0].parent.relative_to(DESIGN).as_posix() if found else None


def typical_haul(d):
    """The typical haul of the credit-budget table rendered from the level's data."""
    total = next(line for line in credit_budget(d).splitlines() if line.startswith("| **Total**"))
    return int(re.findall(r"\*\*([\d,]+)\*\*", total)[-1].replace(",", ""))


def income(data, n):
    """The credits the level earns: its typical haul. M7 part I: every planned level has its data (Level 21
    since part H), so a level without a data file is an error, no longer budget(n). M8 part A (temporary, until M8
    part I): an Act 4 level without its data yet earns budget(n), rounded half to even."""
    d = level_dir(n)
    if not d:
        if n >= FALLBACK_FROM:
            return round(budget(data, n))
        sys.exit(f"Level {n:02d} is in the balance plan but has no data.yaml")
    return typical_haul(d)


def water(data, n):
    """M8 part A: the damage factor on a weapon without the anti-sub trait in Level n: the water's at the plan's
    underwater levels, else 1."""
    return data["water"]["damage"] if n in data["plan"].get("underwater", []) else 1


def rook_missing(data, n):
    """M8 part A: whether Rook is missing in Level n (design/player/wingmen, Rook's loss and absence)."""
    missing = data["wingmen"]["rook"]["missing"]
    return missing["after"] < n < missing["back"]


def apply_plan(data):
    """Yield (level, state) for every level from 01 to the plan's last visit, following the expected-loadout plan."""
    plan, W = data["plan"], data["weapons"]
    state = {"credits": data["economy"]["starting_credits"], "spent": 0, "income": 0, "slots": {},
             "invested": {}, "core": {}, "utility": {}, "charges": {}, "log": [], "earned": None,
             "owned": {}}   # owned: slot -> {item: level} kept off the loadout (Rook's other guns)

    def pay(amount, what, slot=None):
        state["credits"] -= amount
        state["spent"] += amount
        state["log"].append(f"{what} ({amount})")
        if slot:
            state["invested"][slot] = state["invested"].get(slot, 0) + amount

    for n in range(1, max(int(key) for key in plan["plan"]) + 1):
        key = f"{n:02d}"
        state["log"] = []
        # the free charges a special gives once at its unlock, as the hangar opens
        for name, sp in data["specials"].items():
            if sp["unlock"] == n and sp.get("free_charges"):
                state["charges"][name] = state["charges"].get(name, 0) + sp["free_charges"]
                state["log"].append(f"{sp['free_charges']}× {name} charge free")
        if n >= data["wingmen"]["rook"]["joins"] and "escort" not in state["slots"]:
            # Rook joins (M5 part A): the starter gun, free, fitted at L1
            state["slots"]["escort"] = [data["wingmen"]["guns"]["list"][0]["id"], 1]
        step = plan["plan"].get(key, {})
        if "start" in step:
            s = step["start"]
            state["slots"]["front"] = list(s["front"])
            for k in ("generator", "shield", "armor", "engine"):
                state["core"][k] = s[k]
        for slot in step.get("sell", []):
            slug, _ = state["slots"].pop(slot)
            refund = round(state["invested"].pop(slot, 0) * data["economy"]["sell_back"])
            pay(-refund, f"sell {W[slug]['name']} ({slot})")
        for slot, slug in step.get("buy", []):
            if slot == "escort":  # Rook's gun (M5 part C): price factor × its base weapon's price, no power
                gun = escort_gun(data, slug)
                pay(round(data["wingmen"]["guns"]["price_factor"] * W[gun["base"]]["price"]),
                    f"buy Rook's {gun['name']}", slot)
                fitted = state["slots"].get(slot) or [data["wingmen"]["guns"]["list"][0]["id"], 1]  # his starter
                state["owned"].setdefault(slot, {})[fitted[0]] = fitted[1]
                state["slots"][slot] = [slug, 1]
                continue
            w = W[slug]
            if w["unlock"] > n:
                state["log"].append(f"!! {slug} not unlocked before L{key}")
            if slot in state["slots"]:  # the item it replaces goes to the inventory (M7 part A: the front gun)
                old_slug, old_level = state["slots"][slot]
                state["owned"].setdefault(slot, {})[old_slug] = old_level
                state["invested"].pop(slot, None)
            pay(w["price"], f"buy {w['name']} → {slot}", slot)
            state["slots"][slot] = [slug, 1]
        for slot, slug in step.get("sell_owned", []):  # M7 part A: an owned, unfitted weapon sold
            owned = state["owned"].get(slot, {})
            if slug not in owned:
                state["log"].append(f"!! sell {slug} ({slot}): not owned")
                continue
            lvl = owned.pop(slug)
            spent = W[slug]["price"] + sum(upgrade_costs(W[slug])[:lvl - 1])
            pay(-round(spent * data["economy"]["sell_back"]), f"sell {W[slug]['name']} (owned)")
        for slot, slug in step.get("fit", []):  # an owned item fitted again (M5 part D: Rook's Autocannon)
            if slot == "special":  # M7 part A: a special fitted again; its charges and the others' are kept
                state["log"].append(f"fit {slug} (special) again")
                continue
            owned = state["owned"].get(slot, {})
            if slug not in owned:
                state["log"].append(f"!! fit {slug} ({slot}): not owned")
                continue
            fitted = state["slots"].get(slot)
            if fitted:
                owned[fitted[0]] = fitted[1]
            state["slots"][slot] = [slug, owned.pop(slug)]
            if slot == "escort":
                state["log"].append(f"fit Rook's {escort_gun(data, slug)['name']} again")
            else:
                state["log"].append(f"fit {W[slug]['name']} → {slot} again")
        for slot, lvl in step.get("upgrade", []):
            slug, cur = state["slots"][slot]
            costs = upgrade_costs(W[slug])
            for l in range(cur + 1, lvl + 1):
                pay(costs[l - 2], f"{W[slug]['name']} ({slot}) → L{l}", slot)
            state["slots"][slot] = [slug, lvl]
        for kind, idx in step.get("core", []):
            item = data["core"][kind][idx]
            if data["availability"][item["available"]] > n:
                state["log"].append(f"!! {kind} {item['name']} not available before L{key}")
            pay(item["price"], f"{kind} {item['name']}")
            state["core"][kind] = idx
        for name, lvl in step.get("utility", []):
            item = data["utility"][name]
            cur = state["utility"].get(name, 0)
            for l in range(cur + 1, lvl + 1):
                pay(item["prices"][l - 1], f"{name} L{l}")
            state["utility"][name] = lvl
        for name, count in step.get("charges", []):
            sp = data["specials"][name]
            if sp["unlock"] > n:
                state["log"].append(f"!! {name} not unlocked before L{key}")
            state["charges"][name] = state["charges"].get(name, 0) + count
            if state["charges"][name] > sp["max_charges"]:
                state["log"].append(f"!! {name}: {state['charges'][name]} charges, at most {sp['max_charges']}")
            pay(sp["charge_price"] * count, f"{count}× {name} charge")
        if n > 1:
            pay(plan["repair_points_per_level"] * data["repair_cost"], "repairs (estimate)")
        if n > data["wingmen"]["rook"]["joins"] and not rook_missing(data, n):
            # round 34, P3 = A: Rook's repairs after a level flown with him, after the ship's; M8 part A: none while
            # he is missing (his card is MIA, the loaner Bulwark is repaired free)
            pay(plan["escort_repair_points_per_level"] * data["repair_cost"], "Rook's repairs (estimate)")
        yield n, state
        earned = income(data, n)
        # round 34, P3 = A: the typical player's grade bonus (a B) on top of the haul, rounded half to even
        earned += round(earned * plan["typical_grade_bonus"])
        state["earned"] = earned
        state["credits"] += earned
        state["income"] += earned


def escort_gun(data, gun_id):
    return next(g for g in data["wingmen"]["guns"]["list"] if g["id"] == gun_id)


def escort_dps(data, state, n):
    """Rook's single-target DPS (M5 part I, BalanceTest.escort): his gun's scale x its base weapon's
    single-target DPS at the gun's level (M8 part A: x the water's factor under water); 0 before he has a gun. M8
    part A: the Bulwark's cannon while Rook is missing (anti-sub, full damage). Not part of the forward DPS."""
    if "escort" not in state["slots"]:
        return 0.0
    if rook_missing(data, n):
        gun = data["wingmen"]["bulwark"]["gun"]
        return gun["rate"] * gun["damage"]
    slug, lvl = state["slots"]["escort"]
    gun = escort_gun(data, slug)
    w = data["weapons"][gun["base"]]
    return gun["scale"] * stats(w, w["levels"][lvl - 1])[1] * water(data, n)


def loadout_numbers(data, state, n):
    """The fit's load, output and single-target forward, rear and volley DPS in Level n (M8 part A: a weapon without
    the anti-sub trait at the water's factor under water)."""
    W, core = data["weapons"], data["core"]
    load_mw, fwd, rear, volley = 0.0, 0.0, 0.0, 0.0
    for slot, (slug, lvl) in state["slots"].items():
        if slot == "escort":
            continue
        w = W[slug]
        v, s = stats(w, w["levels"][lvl - 1])
        factor = 1 if "anti-sub" in w["traits"] else water(data, n)
        v, s = v * factor, s * factor
        load_mw += draw(w, lvl)
        volley += v
        if w["slot"] == "rear":
            rear += s
        else:
            fwd += s
    load_mw += core["shield"][state["core"]["shield"]]["draw"]
    load_mw += core["engine"][state["core"]["engine"]]["draw"]
    load_mw += sum(data["utility"][n]["draw"] for n in state["utility"])
    output = core["generator"][state["core"]["generator"]]["output"]
    return load_mw, output, fwd, rear, volley


# --- enemies ----------------------------------------------------------------------------------

def enemy(slug):
    return dict(load(f"{enemy_dir(slug)}/data.yaml"), slug=slug)


def enemy_slugs():
    return sorted(p.parent.name for p in (DESIGN / "enemies").glob("*/*/data.yaml"))


def level_enemies(d):
    """The enemies a level uses: waves (incl. a spawner's brood), ground units, set pieces, its boss."""
    level = load(f"{d}/data.yaml")
    slugs = set()
    for wave in level.get("waves", []):
        for g in wave.get("groups") or [wave]:
            slugs.add(g["enemy"])
    slugs.update(g["enemy"] for g in level.get("ground_targets", []) if "enemy" in g)
    slugs.update(p["enemy"] for p in level.get("set_pieces", []))
    for s in list(slugs):
        slugs.update(a["spawn"]["enemy"] for a in enemy(s).get("attacks", []) if a.get("spawn"))
    boss = level.get("boss", {}).get("enemy")
    return sorted(slugs - {boss}), boss


def boss_or_set_piece(e):
    return "boss" in e or e["tier"] == "huge"


ARENA_EASE = 1.0  # s: an arena boss's bar appears as the scroll eases into the halt (Sortie.ARENA_RAMP_SECONDS)


def arena_seconds(e, dps):
    """M5 part E (E6 = a): an arena boss's fight at the effective dps, its exposure windows counted
    (BalanceTest.arenaSeconds): the ease into the halt; a phase ending on slams lasts its delay and its
    slams (the last to its impact), at most its seconds; a phase without a surfacing (round 40, the rooted
    Kraken's Arms) cuts its parts down to its left, the cheapest first, at most its seconds; a phase ending on a
    share of its parts' HP
    takes that HP in cycles of its surfacing (rise, open, dive, a slam cycle after the dive) at the mean
    of the part's own and its spots' multiplier while open (round 40, rooted arms: its spots'), its own over half the rise and the dive; the
    last phase takes the rest with the part kept up."""
    boss = e["boss"]
    if "slam" not in boss:
        return laneless_seconds(e, dps)
    slam = boss["slam"]
    cycle = slam["telegraph"] + slam["rise"] + slam["awash"] + slam["sink"]
    parts = {p["name"]: p for p in e["part_list"]}
    left = {name: p["hp"] for name, p in parts.items()}
    seconds = ARENA_EASE
    for phase in boss["phases"]:
        until = phase["until"]
        if "slams" in until:
            slams = phase.get("delay", 0) + (until["slams"] - 1) * cycle + slam["telegraph"] + slam["rise"]
            seconds += min(slams, until.get("seconds", float("inf")))
            continue
        if "surface" not in phase:
            # Round 40 (the rooted Kraken's Arms): its always-hittable parts cut down to `left`, the cheapest
            # first at their own multiplier, its delay first, at most its seconds.
            names = sorted(until["parts"], key=lambda n: left[n] / parts[n].get("multiplier", 1))
            cut = names[:len(names) - until.get("left", 0)]
            spent = phase.get("delay", 0) + sum(left[n] / (dps * parts[n].get("multiplier", 1)) for n in cut)
            seconds += min(spent, until.get("seconds", float("inf")))
            for n in cut:
                left[n] = 0
            continue
        surface = phase["surface"]
        part = parts[surface["part"]]
        own = part.get("multiplier", 1)
        spot = max([s["multiplier"] for s in part.get("spots", [])] or [own])
        # Round 40: the rooted Kraken's head sits in open view above the lanes and its fans are evadable, so the
        # open window goes to the eyes; before, the eyes and the mantle took about equal shares.
        opened = spot if boss["lanes"].get("rooted") else (own + spot) / 2
        names = until["parts"]
        need = sum(left[n] for n in names)
        full = sum(parts[n]["hp"] for n in names)
        if "below" in until:
            need -= until["below"] * full
            after_dive = phase.get("slamming") == "after_dive"
            round_s = surface["rise"] + surface["open"] + surface["dive"] + (cycle if after_dive else 0)
            damage = dps * (surface["open"] * opened + (surface["rise"] + surface["dive"]) / 2 * own)
            seconds += need / damage * round_s
            for n in names:
                left[n] = until["below"] * parts[n]["hp"]
        else:
            seconds += need / (dps * opened)
            for n in names:
                left[n] = 0
    return seconds


def laneless_seconds(e, dps):
    """M5 part H (H10 = a): an arena boss without lanes (the Siege Spire) at the effective dps
    (BalanceTest.lanelessSeconds): the ease into the halt, then each phase its delay, its move (the lift)
    and its parts' HP at their own multipliers (the weak spots' x2; anti-ground on the roots not counted)."""
    parts = {p["name"]: p for p in e["part_list"]}
    seconds = ARENA_EASE
    for phase in e["boss"]["phases"]:
        seconds += phase.get("delay", 0) + phase.get("move", {}).get("descend", 0)
        for name in phase["until"].get("parts", []):
            seconds += parts[name]["hp"] / (dps * parts[name].get("multiplier", 1))
    return seconds


def walker_seconds(e, dps):
    """M7 part F: a walking boss's paper fight at dps: its intro, then its HP at the walker's efficiency."""
    return WALKER_INTRO + e["hp"] / (dps * WALKER_EFFICIENCY)


def boss_seconds(e, dps):
    """A boss's fight at dps: its HP over it, an arena boss's window-aware (arena_seconds), a walking one's
    with its passes (walker_seconds)."""
    if "stride" in e.get("movement", {}):
        return walker_seconds(e, dps)
    return arena_seconds(e, dps) if "anchored" in e.get("movement", {}) else e["hp"] / dps


def ttk_problem(e, ref):
    """The time to kill at the first level against the balancing basis, or None (see BalanceTest)."""
    r = ref.get(e["first_level"])
    if r is None:
        return None
    if e["tier"] == "tiny" and "boss" not in e:
        shot = load("player/weapons/pulse-cannon/data.yaml")["levels"][0]["damage"]
        return None if e["hp"] <= shot else f"HP {e['hp']}: not one hit of the starting gun ({shot})"
    if "boss" in e:
        lo, hi = (45, 75) if e["boss"]["kind"] == "mid-boss" else (90, 180)
    else:
        lo, hi = {"small": (0, 0.3), "medium": (0.4, 1.5), "large": (1, 3), "huge": (20, 40)}[e["tier"]]
    dps = r * (EFFECTIVE if boss_or_set_piece(e) else 1)
    if "anchored" in e.get("movement", {}) or "stride" in e.get("movement", {}):
        seconds = boss_seconds(e, dps)
        if seconds < lo or seconds > hi:
            return f"window-aware {seconds:.1f} s at {dps:.1f} DPS, target {lo:g}-{hi:g} s"
        return None
    if e["hp"] < lo * dps - 1 or e["hp"] > hi * dps + 1:  # whole HP: 1 HP of rounding
        return f"HP {e['hp']:g} / {dps:.1f} DPS = {e['hp'] / dps:.2f} s, target {lo:g}-{hi:g} s"
    return None


def bounty_problem(data, e):
    """The bounty against its class (Act 1 terms) or, for bosses and set pieces, its share of the budget."""
    if boss_or_set_piece(e):
        share = (0.15 if e["boss"]["kind"] == "mid-boss" else 0.30) if "boss" in e else 0.15
        d = level_dir(e["first_level"])
        if not d:
            return None
        # Data holds Act 1 terms; the act factor applies to every payout, a boss's included (no exemption).
        act = int(d.split("/")[1].split("-")[1])
        paid = (e["bounty"] * data["economy"]["act_factor"] ** (act - 1)
                * load(f"{d}/data.yaml").get("bounty_scale", 1))
        actual = paid / budget(data, e["first_level"])
        if abs(actual - share) > share * SHARE_TOLERANCE:
            return (f"{paid:.0f} paid = {100 * actual:.1f} % of L{e['first_level']:02d}'s budget, "
                    f"target about {100 * share:.0f} %")
        return None
    hardened = e.get("armour") == "hardened"
    lo, hi = {"tiny": (2, 5), "small": (10, 15), "medium": (40, 60) if hardened else (18, 30),
              "large": (40, 60)}[e["tier"]]
    return None if lo <= e["bounty"] <= hi else f"{e['bounty']} outside the {e['tier']} class {lo}-{hi}"


ACT_2 = range(8, 15)  # Levels 08-14
ACT_3 = range(15, 22)  # Levels 15-21 (M7 part A: the reference DPS before the levels' data)


def act_hp_report(ref, levels=ACT_2, act=2):
    """The act HP factor (design/enemies, Balancing basis; D5 = c of M5 part A): every returning
    medium-or-larger unit's HP (and the listed small ones', capped) (parts summed, medium) and time to kill at the act's levels after its
    first; BalanceTest checks the same."""
    print(f"\nReturning units in Act {act} (act HP factor = reference DPS at the level / at the first level; "
          "from the beam era on × its factor, the time to kill at the reference DPS × that factor):")
    print("Enemy            | Tier     | First | " + " | ".join(f"L{n:02d} HP / TTK" for n in levels))
    for slug in enemy_slugs():
        e = enemy(slug)
        if "boss" in e or e["tier"] == "huge" or not gets_act_hp(e) or e["first_level"] not in ref:
            continue
        cells = []
        for n in levels:
            if n <= e["first_level"] or n not in ref or not gets_act_hp(e, n):
                cells.append(f"{'-':>12}")
                continue
            hp = e["hp"] * act_hp_factor(e, ref[n] / ref[e["first_level"]]) * beam_era(e, n)
            cells.append(f"{hp:5.0f} {hp / (ref[n] * beam_era(e, n)):4.2f} s")
        print(f"{e['name']:16} | {e['tier']:8} | L{e['first_level']:02d}   | " + " | ".join(cells))


def beam_era_report(ref):
    """M7 part E, step E5: every unit of the beam era's tiers at the level before it and in the beam era's levels
    (HP at medium with every factor; BeamEraHpTest measures their time on the screen against Level 10's)."""
    era = load("enemies/data.yaml")["beam_era_hp"]
    levels = [n for n in range(era["from_level"] - 1, ACT_3.stop) if n in ref]
    print(f"\nBeam era (from L{era['from_level']:02d}, the plan's Ion Beam): × {era['factor']:g} on "
          f"{', '.join(era['tiers'])} units, HP at medium / time to kill at the reference DPS × the factor:")
    print("Enemy            | Tier     | First | " + " | ".join(f"L{n:02d} HP / TTK" for n in levels))
    for slug in enemy_slugs():
        e = enemy(slug)
        if "boss" in e or e["tier"] not in era["tiers"] or e["first_level"] > levels[-1]:
            continue
        cells = []
        for n in levels:
            if n < e["first_level"]:
                cells.append(f"{'-':>12}")
                continue
            hp = level_hp(e, n, ref)
            cells.append(f"{hp:5.0f} {hp / (ref[n] * beam_era(e, n)):4.2f} s")
        print(f"{e['name']:16} | {e['tier']:8} | L{e['first_level']:02d}   | " + " | ".join(cells))


def beam_era_problems(data):
    """The beam era starts at the first visit whose plan fits the Ion Beam in front (step E5)."""
    era = load("enemies/data.yaml")["beam_era_hp"]
    first = next((n for n, st in apply_plan(data) if st["slots"].get("front", ("",))[0] == "ion-beam"), None)
    if first != era["from_level"]:
        return [f"beam_era_hp.from_level {era['from_level']} but the plan fits the Ion Beam from L{first}"]
    return []


def enemy_report(data, ref):
    print("\nEnemies (balancing basis, at the first level):")
    print("Enemy            | Tier     | First | HP     | TTK at ref (s) | Bounty | Deviation")
    deviations = 0
    for slug in enemy_slugs():
        e = enemy(slug)
        r = ref.get(e["first_level"])
        dps = r * (EFFECTIVE if boss_or_set_piece(e) else 1) if r else None
        found = [(kind, p) for kind, p in (("ttk", ttk_problem(e, ref)), ("bounty", bounty_problem(data, e))) if p]
        problems = [p + (f" (accepted: {ACCEPTED[slug, kind]})" if (slug, kind) in ACCEPTED else "")
                    for kind, p in found]
        deviations += sum((slug, kind) not in ACCEPTED for kind, _ in found)
        kind = e["boss"]["kind"] if "boss" in e else e["tier"]
        ttk = (boss_seconds(e, dps) if "boss" in e else e["hp"] / dps) if dps else 0
        print(f"{e['name']:16} | {kind:8} | L{e['first_level']:02d}   | {e['hp']:6g} | "
              f"{ttk:14.2f} | {e['bounty']:6} | " + "; ".join(problems))
    if deviations:
        print("A deviation fails BalanceTest unless it is listed there as accepted or pending a decision.")


# --- report -----------------------------------------------------------------------------------

def report(data):
    ref = load("enemies/data.yaml")["reference_dps"]
    print("Lvl | Budget | Before visit | Spent here | Left | Load/Out | Fwd DPS | Ref DPS | Fwd/Ref | Rear DPS "
          "| Volley DPS | Rook DPS | Purchases")
    problems, accepted, ttk_lines = [], [], []
    prev_spent = 0
    for n, st in apply_plan(data):
        spent_here = st["spent"] - prev_spent
        prev_spent = st["spent"]
        load_mw, out, fwd, rear, volley = loadout_numbers(data, st, n)
        flags = []
        if st["credits"] < 0:
            flags.append("OVERSPENT")
        if load_mw > out:
            flags.append("OVER POWER")
        r = ref.get(n)
        ratio = f"{fwd / r:7.2f}" if r else "      -"
        if r and not DPS_BAND[0] <= fwd / r <= DPS_BAND[1]:
            if n in ONBOARDING and DPS_BAND[1] < fwd / r <= ONBOARDING_HIGH:
                accepted.append(f"L{n:02d}: DPS {fwd:.0f} vs enemy reference {r:.0f} ({fwd / r:.2f}×), "
                                "gentle onboarding (user decision 2026-10-01)")
            else:
                flags.append(f"DPS {fwd:.0f} vs enemy reference {r:.0f}")
        print(f"{n:02d}  | {budget(data, n):6.0f} | {st['credits'] + spent_here:12.0f} | {spent_here:10.0f} | "
              f"{st['credits']:4.0f} | {load_mw:4.1f}/{out:<3g} | {fwd:7.1f} | {(r or 0):7.0f} | {ratio} | "
              f"{rear:8.1f} | {volley:10.1f} | {escort_dps(data, st, n):8.1f} | "
              + "; ".join(st["log"]) + (("  <-- " + ", ".join(flags)) if flags else ""))
        for f in flags:
            problems.append(f"L{n:02d}: {f}")
        problems += [f"L{n:02d}: {m[3:]}" for m in st["log"] if m.startswith("!!")]
        d = level_dir(n)
        if d:
            slugs, boss = level_enemies(d)
            slowest = max(slugs, key=lambda s: enemy(s)["hp"])
            line = (f"L{n:02d}: typical haul {income(data, n)} of budget {budget(data, n):.0f}; "
                    f"slowest TTK at the plan's DPS {enemy(slowest)['name']} {enemy(slowest)['hp'] / fwd:.1f} s")
            if boss:
                line += f"; boss {enemy(boss)['name']} {boss_seconds(enemy(boss), EFFECTIVE * fwd):.0f} s (at 0.6×)"
            ttk_lines.append(line)
            ttk_lines += hold_lines(data, d, st)
            rear_lines, slow, pending = ambush_lines(d, rear, n, ref)
            ttk_lines += rear_lines
            problems += [f"L{n:02d}: {m}" for m in slow]
            accepted += [f"L{n:02d}: {m}" for m in pending]
    print("\nBefore visit: the credits after the level before it, which earns its typical haul (its "
          f"credit-budget table; M8 part A, temporary: budget(n) for a level from L{FALLBACK_FROM} without data), "
          "plus the typical player's grade bonus "
          f"(+{data['plan']['typical_grade_bonus']:.0%}, a B). Spent here includes the repair estimate "
          "(the ship's, and Rook's from the L09 visit). Rook DPS: his gun's single-target DPS "
          "(from Level 08), not in the forward DPS.")
    print("\nLevels with data:")
    for line in ttk_lines:
        print("  " + line)
    hard = data["plan"].get("difficulties", {})
    if hard:
        print("\nPlan additions on other difficulties (ActPlaythroughTest): "
              + "; ".join(f"{diff} L{lvl}: " + ", ".join(f"{action} {items}" for action, items in step.items())
                           for diff, steps in hard.items() for lvl, step in steps.items()))
    enemy_report(data, ref)
    act_hp_report(ref)
    act_hp_report(ref, ACT_3, 3)
    beam_era_report(ref)
    problems += beam_era_problems(data)
    print("\nAccepted: " + ("none" if not accepted else "\n  " + "\n  ".join(accepted)))
    print("\nProblems: " + ("none" if not problems else "\n  " + "\n  ".join(problems)))
    return 1 if problems else 0


def anti_ground_dps(data, state):
    """The single-target DPS of the fit's anti-ground sources: its weapons and Rook's gun (his gun's
    scale × its base weapon); the only damage a hardened unit takes (the specials aside)."""
    W, dps = data["weapons"], 0.0
    for slot, (slug, lvl) in state["slots"].items():
        if slot == "escort":
            gun = escort_gun(data, slug)
            w = W[gun["base"]]
            if "anti-ground" in w.get("traits", []):
                dps += gun["scale"] * stats(w, w["levels"][lvl - 1])[1]
        elif "anti-ground" in W[slug].get("traits", []):
            dps += stats(W[slug], W[slug]["levels"][lvl - 1])[1]
    return dps


def hold_lines(data, d, state):
    """A level's hold zones (M5 part C): each cluster's HP against the fit's anti-ground DPS and the
    hold's window at medium (from the trigger until its first unit's far edge leaves the bottom
    edge: the ease, then the hold speed); BalanceTest checks HP / DPS <= 0.6 x window."""
    level = load(f"{d}/data.yaml")
    lines = []
    dps = anti_ground_dps(data, state)
    for i, hold in enumerate(level.get("holds", [])):
        units = [g for g in level["ground_targets"] if g.get("group") in hold["groups"]]
        hp = sum(enemy(g["enemy"])["hp"] * len(g["at"]) for g in units)
        half = max(enemy(g["enemy"])["hitbox"][1] / 2 for g in units)
        ease = hold["ramp"] * (level["scroll_speed"] + hold["speed"]) / 2
        window = hold["ramp"] + (540 + half - hold["y"] - ease) / hold["speed"]
        ttk = hp / dps if dps else float("inf")
        lines.append(f"  hold {i + 1} ({', '.join(hold['groups'])}): {hp:g} HP / anti-ground {dps:.1f} DPS = "
                     f"{ttk:.1f} s of a {window:.1f} s window ({ttk / window:.2f}, at most {HOLD_SHARE})")
    return lines


def gets_act_hp(e, n=None):
    """Whether a unit gets the act HP factor where it returns (in Level n, or in any level when n is None):
    medium and larger, and the small units the basis lists in small_act_hp in its levels (the user's Act 2
    playtest, 2026-10-18: the Needler and the Stinger in Levels 08-09)."""
    small = load("enemies/data.yaml")["small_act_hp"]
    return e["tier"] not in ("tiny", "small") or (e["slug"] in small["units"] and (n is None or n in small["levels"]))


def act_hp_factor(e, ratio):
    """The act HP factor from the reference DPS ratio: a listed small unit's capped at small_act_hp.cap."""
    return min(ratio, load("enemies/data.yaml")["small_act_hp"]["cap"]) if e["tier"] == "small" else ratio


def level_hp(e, n, ref):
    """A unit's HP at medium in Level n: with the act HP factor for a returning medium-or-larger unit (or
    a small one in the basis's small_act_hp) from Act 2 on (reference DPS at the level / at its first
    level) and the beam era's factor (beam_era), rounded as the game does."""
    factor = 1
    if n >= ACT_2.start and gets_act_hp(e, n) and n > e["first_level"] and n in ref and e["first_level"] in ref:
        factor = act_hp_factor(e, ref[n] / ref[e["first_level"]])
    return difficulty_hp(e["hp"] * factor * beam_era(e, n), 1)


def beam_era(e, n):
    """M7 part E, step E5 (the user's "tougher, not more" of 2026-10-19): the basis's beam_era_hp factor on a unit
    of its tiers (not a boss) from its from_level on, its own first level included; else 1. BalanceTest judges
    such a unit's time to kill at the reference DPS times the same factor."""
    era = load("enemies/data.yaml")["beam_era_hp"]
    return era["factor"] if n >= era["from_level"] and e["tier"] in era["tiers"] and "boss" not in e else 1


def ambush_lines(d, rear, n, ref):
    """A level's rear-ambush units (M5 part D, D7 = a): each one's HP (with the act HP factor) against the
    plan's rear DPS and its window behind the ship (its decloak flash and its hold) at medium;
    BalanceTest checks HP / rear DPS <= 0.6 x window. M5 part F: a unit that latches (the Lamprey), its HP
    against the rear DPS x its damage factor, within 0.25 of its shake-off time. Returns (lines, the too
    slow ones, the too slow ones the user accepted)."""
    level = load(f"{d}/data.yaml")
    lines, slow, pending = [], [], []
    for slug in dict.fromkeys(w["enemy"] for w in level["waves"] if w.get("formation") == "rear ambush"):
        e = enemy(slug)
        hp = level_hp(e, n, ref)
        window = e.get("cloak", {}).get("flash", 0) + e["movement"]["hover"]["seconds"]
        ttk = hp / rear if rear else float("inf")
        lines.append(f"  rear check {e['name']}: {hp:g} HP / rear {rear:.1f} DPS = {ttk:.1f} s of its "
                     f"{window:.1f} s flash and hold ({ttk / window:.2f}, at most {REAR_SHARE})")
        if not ttk <= REAR_SHARE * window:
            text = f"rear DPS {rear:.1f} too low for the {e['name']} ({ttk:.1f} s of {window:.1f} s)"
            if (n, slug) in REAR_PENDING:
                pending.append(f"{text}: {REAR_PENDING[(n, slug)]}")
            else:
                slow.append(text)
    for slug in dict.fromkeys(g["enemy"] for w in level["waves"] for g in w.get("groups", [w]) if "latch" in enemy(g["enemy"])):
        e = enemy(slug)
        latch = e["latch"]
        hp = level_hp(e, n, ref)
        ttk = hp / (rear * latch["damage_factor"]) if rear else float("inf")
        within = latch["shake"]["within"]
        lines.append(f"  latched check {e['name']}: {hp:g} HP / (rear {rear:.1f} DPS x {latch['damage_factor']:g}) "
                     f"= {ttk:.2f} s of its {within:g} s shake-off ({ttk / within:.2f}, at most {LATCHED_SHARE})")
        if not ttk <= LATCHED_SHARE * within:
            slow.append(f"rear DPS {rear:.1f} too low for a latched {e['name']} ({ttk:.2f} s)")
    return lines, slow, pending


def weapons_dump(data):
    for slug, w in data["weapons"].items():
        print(f"\n{w['name']} ({slug})")
        for r in per_level_rows(w):
            print("  " + " | ".join(r))


def main():
    data = load_all()
    if "--weapons" in sys.argv:
        weapons_dump(data)
        return 0
    return report(data)


if __name__ == "__main__":
    sys.exit(main())
