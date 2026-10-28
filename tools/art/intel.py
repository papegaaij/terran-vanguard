#!/usr/bin/env python3
"""Production art: the hangar intel's sensor-L2 pictures (design/ui/hangar, design/player/systems
"Sensor levels and hangar intel": enemy types with portraits, boss name and silhouette).

Outputs (assets/sprites/intel/, packed onto the shared sprite pages as ``intel/<name>``):
  <enemy>.png        30x30 sensor portrait of an enemy type of a level's waves: skitter, needler,
                     stinger, spine-turret (Levels 01-02, UI batch); spore-bomber, whirl-seed
                     (Level 03, M4 part C batch); brood-pod, scuttler (Level 04, M4 part D batch); mantis,
                     coilwyrm (Level 06, M4 part F batch: the production models of tools/art/mantis.py,
                     nose down, and tools/art/coilwyrm.py, the head with a segment trailing); creeper
                     (Level 08, M5 part B batch: the production model of tools/art/creeper.py);
                     hive-node, ravager (Level 09, M5 part C batch: the production models of
                     tools/art/hive_node.py, the iris half open, and tools/art/ravager.py);
                     wraith, mote-swarm (Level 10, M5 part D batch: the production models of
                     tools/art/wraith.py, decloaked, nose down in ripple frame 1, and
                     tools/art/mote_swarm.py, a spike leading down, its slit at the middle glow);
                     driftjelly, reef-spitter (Level 11, M5 part E batch: the production models of
                     tools/art/driftjelly.py at rest, and tools/art/reef_spitter.py, the gun aiming down
                     on its kelp raft); lamprey (Level 12, M5 part F batch: the production model of
                     tools/art/lamprey.py nose down, in swim frame 1); skimmer (Level 13, M5 part G
                     batch: the production model of tools/art/skimmer.py, the whole hull at heading 0,
                     moving down the screen, its tail centred); burrower, dust-kite (Level 15, M7 part B
                     batch: the production models of tools/art/burrower.py, surfaced with the maw open,
                     and tools/art/dust_kite.py, nose down with the membrane up); tendril-anchor,
                     threadcrawler (Level 16, M7 part C batch: the production models of
                     tools/art/canyon_units.py, the anchor's cone pointing down at its full pulse, and the
                     Threadcrawler's head nose down with its first segment trailing); choir-herald, shellback
                     (Level 17, M7 part D batch: the production models of tools/art/siege_units.py, the
                     Herald facing down with its crest lit, the Shellback walking down, its vent swollen);
                     dust-devil, ghost-drone (Level 18, M7 part E batch: the production models of
                     tools/art/storm_units.py, the Dust Devil in its rise with the lime core showing, the
                     Ghost Drone nose down with its red eye); warden-tank, revenant-walker (Level 19, M7
                     part F batch: the production models of tools/art/warden_tank.py, the grey hull driving
                     down with the turret aimed along it, and tools/art/revenant_walker.py, walking down in
                     walk phase 0 with its pods aimed ahead and the flamer at rest, scaled to fit);
                     drill-rig (Level 22, M8 part B batch: the production models of tools/art/drill_rig.py,
                     the core with its augers and the four pumps on their hoses, scaled to fit); glassfin,
                     eel-swarm (Level 23, M8 part C batch: the production models of tools/art/glassfin.py,
                     three fry of a school nose down, and tools/art/eel_swarm.py, the head nose down with the
                     stripe lit and two body eels trailing); abyss-ray, barbfin (Level 24, M8 part D batch: the
                     production models of tools/art/abyss_ray.py, the bone skate nose down with its wings level,
                     and tools/art/barbfin.py, the bone lionfish nose down with its barb lit); siren,
                     spiral-nautilus (Level 25, M8 part E batch: the production models of tools/art/siren.py, the
                     frill bell facing down with its sac mid-tell, and tools/art/spiral_nautilus.py, uncoiled nose
                     down, the shell behind the open crown with its lime-white eyes, scaled to fit); glow-angler
                     (Level 26, M8 part F batch: the production model of tools/art/glow_angler.py, the rust angler
                     nose down with its gape half open and the teal lure lit on its stalk); talon,
                     sam-nest, asteroid-mite, clamp-gun, wavering-stormhawk (Level 29, M9 part B batch, step B4: the
                     production models of tools/art/belt_units.py in Q.11 b's gunmetal, nose down: the Talon, the SAM
                     Nest's rack over its base, the Mite in its crawl, the clamp gun, the Stormhawk with its IFF lit);
                     minelayer, gilded-gunship, dart-drone, rail-serpent (Level 30, M9 part C batch, step C4: the
                     production models of tools/art/belt_units_c.py, nose down: the Minelayer's barge with its drum
                     dark, the Gilded Gunship with its doors shut and lamps dark, the Dart, the Rail Serpent's engine
                     car); hornet, shard-drone, buzzsaw-drone (Level 31, M9 part D batch, step ART-D: the
                     production models of tools/art/belt_units_d.py: the Hornet facing down with its hatches dark,
                     the Shard Drone's tri-prism, the Buzzsaw's gold-edged blades)
  unclassified-<enemy>.png  30x30 the sensor-L2 picture of a unit the intel does not name yet
                     (`unclassified_until_flown`, M7 part E batch): unclassified-ghost-drone, the Ghost
                     Drone's outline only, filled flat in dark teal with the plate's grid and a cyan rim
                     like a boss silhouette, broken by sensor noise (rows torn sideways, dropped pixels):
                     "unclassified, small, air" until Level 18 is first flown
  boss-<boss>.png    40x40 sensor silhouette of a boss of Act 1: gorgon-frigate (L05 mid-boss),
                     brood-carrier (L07) (UI batch); leviathan (L03's set piece, the threat
                     profile's "unknown huge contact", M4 part C batch); harbour-kraken (L11's
                     mid-boss, M5 part E batch: the head and mantle of the `harbour-kraken-sub` sprite);
                     siege-spire (L14's act boss, M5 part H batch: the rooted body as the arena first
                     shows it, the production trunk, roots and pods of tools/art/siege_spire.py placed by
                     its pivots); revenant-walker (L19's mid-boss, M7 part F batch: the "unclassified,
                     huge, ground" contact, the production walker's sprites at heading 0 placed by
                     its pivots, broken by the unclassified pictures' sensor noise); dust-colossus (L21's act
                     boss, M7 part H batch: the open mouth of tools/art/dust_colossus.py as phase 3 shows it,
                     straight up, the outline only); abyssal-maw (L28's act boss, M8 part H batch: the coil of
                     tools/art/abyssal_maw.py round the arena's edges with the head and its sacs at the top, as
                     phase 1 shows it, the outline only)
  design/ui/hangar/concept/intel-final-r13-a.png   review sheet of the UI batch's pictures
  design/ui/hangar/concept/intel-final-r16-a.png   review sheet of the M4 part C batch's
  design/ui/hangar/concept/intel-final-r17-a.png   review sheet of the M4 part D batch's
  design/ui/hangar/concept/intel-final-r23-a.png   review sheet of the M4 part F batch's
  design/ui/hangar/concept/intel-final-r30-a.png   review sheet of the M5 part B batch's
  design/ui/hangar/concept/intel-final-r31-a.png   review sheet of the M5 part C batch's
  design/ui/hangar/concept/intel-final-r32-a.png   review sheet of the M5 part D batch's
  design/ui/hangar/concept/intel-final-r33-a.png   review sheet of the M5 part E batch's
  design/ui/hangar/concept/intel-final-r34-a.png   review sheet of the M5 part F batch's
  design/ui/hangar/concept/intel-final-r35-a.png   review sheet of the M5 part G batch's
  design/ui/hangar/concept/intel-final-r36-a.png   review sheet of the M5 part H batch's
  design/ui/hangar/concept/intel-final-r38-a.png   review sheet of the M7 part B batch's
  design/ui/hangar/concept/intel-final-r39-a.png   review sheet of the M7 part C batch's
  design/ui/hangar/concept/intel-final-r40-a.png   review sheet of the M7 part D batch's
  design/ui/hangar/concept/intel-final-r41-a.png   review sheet of the M7 part E batch's
  design/ui/hangar/concept/intel-final-r42-a.png   review sheet of the M7 part F batch's
  design/ui/hangar/concept/intel-final-r62-a.png   review sheet of the M9 part B batch's
  design/ui/hangar/concept/intel-final-r63-a.png   review sheet of the M9 part C batch's
  design/ui/hangar/concept/intel-final-r64-a.png   review sheet of the M9 part D batch's

The names are the enemy's or boss's name as a slug (vanguard.game.render.Portraits.slug). A
portrait is the unit's chosen round-04 model (tools/concept/enemies_r04.py, imported unchanged) in
its own colours, nose down as it comes at the player (the Whirl Seed is the six-blade production
seed of tools/art/vrell_l03.py, as the game draws it, at rest; the Brood Pod and the Scuttler are
the production models of tools/art/vrell_l04.py, the pod between swells, the Scuttler walking down
at heading 0 in its first walk frame; the Creeper likewise from tools/art/creeper.py; the Hive Node from tools/art/hive_node.py with its
iris half open and glowing, the Ravager from tools/art/ravager.py running down in gallop frame 2; the Wraith from tools/art/wraith.py nose down, decloaked, the Mote from tools/art/mote_swarm.py), ray-marched at 8x through the sprite path
(1-bit alpha, unsharp mask) onto the intel's sensor plate: a dark teal screen with a dot grid, a
cyan scan line and corner brackets, lightly tinted cyan as the scan sees it, 32 colours. A boss
silhouette shows what L2 knows of it: the outline only, the shape filled flat in dark teal with a
bright cyan rim and the plate's grid running through it, from the chosen models (the Brood
Carrier of round 04, the Gorgon Frigate's bell, necks and heads of round 06 at rest, the
Leviathan's production model of tools/art/leviathan.py facing down with its parts on and the tail
straight).

Set pieces (the Leviathan) and other units outside a level's waves get no portrait: the intel lists
the waves' enemy types only. The Leviathan gets a silhouette instead, as a boss does: Level 03's
threat profile promises an "unknown huge contact" silhouette at L2. Its name follows the game's
``intel/boss-<slug>`` lookup.

Run: python3 tools/art/intel.py [name ...] [--review]   (~40 s; with names, e.g. spore-bomber or
boss-brood-carrier, only those pictures and the review sheets of their batches; --review only
rebuilds the sheets)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, SPRITES, sprite

import burrower  # noqa: E402  (the Burrower's production model)
import canyon_units  # noqa: E402  (the Tendril Anchor's and the Threadcrawler's production models)
import coilwyrm  # noqa: E402  (the Coilwyrm's production models)
import dust_kite  # noqa: E402  (the Dust Kite's production model)
import drill_rig  # noqa: E402  (the Drill Rig's production models)
import abyss_ray  # noqa: E402  (the Abyss Ray's production model)
import abyssal_maw  # noqa: E402  (the Abyssal Maw's production sprites)
import belt_units  # noqa: E402  (M9 part B: Level 29's production models)
import belt_units_c  # noqa: E402  (M9 part C: Level 30's production models)
import belt_units_d  # noqa: E402  (M9 part D: Level 31's production models)
import barbfin  # noqa: E402  (the Barbfin's production model)
import siren  # noqa: E402  (the Siren's production model)
import glow_angler  # noqa: E402  (the Glow Angler's production model)
import depth_hunter  # noqa: E402  (the Depth Hunter's production model)
import relay_beacon  # noqa: E402  (the relay beacon's production model)
import spiral_nautilus  # noqa: E402  (the Spiral Nautilus's production models)
import eel_swarm  # noqa: E402  (the Eel Swarm's production models)
import glassfin  # noqa: E402  (the Glassfin's production model)
import creeper  # noqa: E402  (the Creeper's production model)
import dust_colossus  # noqa: E402  (the Dust Colossus's production sprites)
import reef_spitter  # noqa: E402  (the Reef Spitter's production gun and raft models)
import revenant_walker  # noqa: E402  (the Revenant Walker's production models and sprites)
import hive_node  # noqa: E402  (the Hive Node's production model)
import lamprey  # noqa: E402  (the Lamprey's production model)
import leviathan  # noqa: E402  (the Leviathan's production model)
import mantis  # noqa: E402  (the Mantis's production model)
import mote_swarm  # noqa: E402  (the Mote's production model)
import ravager  # noqa: E402  (the Ravager's production model)
import siege_units  # noqa: E402  (the Choir Herald's and the Shellback's production models)
import skimmer  # noqa: E402  (the Skimmer's production model and headings)
import storm_units  # noqa: E402  (the Dust Devil's and the Ghost Drone's production models)
import vrell_l03  # noqa: E402  (the Whirl Seed's production model)
import warden_tank  # noqa: E402  (the Warden Tank's production model, Act 3 grey)
import vrell_l04  # noqa: E402  (the Brood Pod's and the Scuttler's production models)
import wraith  # noqa: E402  (the Wraith's production model)

import bosses_r06  # noqa: E402  (concept scripts, imported unchanged)
import enemies_r03 as e3  # noqa: E402
import enemies_r04 as e4  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import r06_models as m6  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_rigs import model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "intel.py"
UI_BATCH = "UI batch"
# the batch each picture was made in (the Source note); the UI batch's are left out
BATCHES = {"spore-bomber": "M4 part C batch", "whirl-seed": "M4 part C batch",
           "boss-leviathan": "M4 part C batch",
           "brood-pod": "M4 part D batch", "scuttler": "M4 part D batch",
           "mantis": "M4 part F batch", "coilwyrm": "M4 part F batch",
           "creeper": "M5 part B batch",
           "hive-node": "M5 part C batch", "ravager": "M5 part C batch",
           "wraith": "M5 part D batch", "mote-swarm": "M5 part D batch",
           "driftjelly": "M5 part E batch", "reef-spitter": "M5 part E batch",
           "boss-harbour-kraken": "M5 part E batch",
           "lamprey": "M5 part F batch",
           "skimmer": "M5 part G batch", "boss-siege-spire": "M5 part H batch",
           "burrower": "M7 part B batch", "dust-kite": "M7 part B batch",
           "tendril-anchor": "M7 part C batch", "threadcrawler": "M7 part C batch",
           "choir-herald": "M7 part D batch", "shellback": "M7 part D batch",
           "dust-devil": "M7 part E batch", "ghost-drone": "M7 part E batch",
           "unclassified-ghost-drone": "M7 part E batch",
           "warden-tank": "M7 part F batch", "revenant-walker": "M7 part F batch",
           "boss-revenant-walker": "M7 part F batch", "boss-dust-colossus": "M7 part H batch",
           "drill-rig": "M8 part B batch", "glassfin": "M8 part C batch", "eel-swarm": "M8 part C batch",
           "abyss-ray": "M8 part D batch", "barbfin": "M8 part D batch",
           "siren": "M8 part E batch", "spiral-nautilus": "M8 part E batch", "glow-angler": "M8 part F batch",
           "depth-hunter": "M8 part G batch", "relay-beacon": "M8 part G batch",
           "boss-abyssal-maw": "M8 part H batch",
           "talon": "M9 part B batch", "sam-nest": "M9 part B batch", "asteroid-mite": "M9 part B batch",
           "clamp-gun": "M9 part B batch", "wavering-stormhawk": "M9 part B batch",
           "minelayer": "M9 part C batch", "gilded-gunship": "M9 part C batch", "dart-drone": "M9 part C batch",
           "rail-serpent": "M9 part C batch",
           "hornet": "M9 part D batch", "shard-drone": "M9 part D batch", "buzzsaw-drone": "M9 part D batch"}
# the concept round that reviews a batch
ROUNDS = {UI_BATCH: "r13", "M4 part C batch": "r16", "M4 part D batch": "r17", "M4 part F batch": "r23",
          "M5 part B batch": "r30", "M5 part C batch": "r31", "M5 part D batch": "r32", "M5 part E batch": "r33",
          "M5 part F batch": "r34", "M5 part G batch": "r35", "M5 part H batch": "r36",
          "M7 part B batch": "r38", "M7 part C batch": "r39", "M7 part D batch": "r40",
          "M7 part E batch": "r41", "M7 part F batch": "r42", "M7 part H batch": "r44", "M8 part B batch": "r51",
          "M8 part C batch": "r52", "M8 part D batch": "r53", "M8 part E batch": "r54",
          "M8 part F batch": "r55", "M8 part G batch": "r57",
          "M8 part H batch": "r58", "M9 part B batch": "r62", "M9 part C batch": "r63",
          "M9 part D batch": "r64"}
OUT = SPRITES / "intel"
CONCEPT = DESIGN / "ui" / "hangar" / "concept"
PORTRAIT = 30
SILHOUETTE = 40
COLOURS = 32
# enemy -> its chosen model, (scene, mats) at rest
ENEMIES = {
    "skitter": lambda: e4.R04["skitter-a"][4](0.0, 1.0),
    "needler": lambda: e4.R04["needler-a"][4](0.0, 1.0),
    "stinger": lambda: e4.R04["stinger-a"][4](0.0, 1.0),
    "spine-turret": lambda: e4.R04["spine-turret-a"][4](0.0, 1.0),
    "spore-bomber": lambda: e4.R04["spore-bomber-a"][4](0.0, 1.0),
    "whirl-seed": lambda: vrell_l03.seed_model(0.0),
    "brood-pod": lambda: vrell_l04.pod_model(0.0, 1.0),
    "scuttler": lambda: scuttler_model(),
    "mantis": lambda: mantis.turned(*mantis.model(0.3, 0.0, 1.0), 0.0),
    "coilwyrm": lambda: coilwyrm_model(),
    "creeper": lambda: creeper.model(0, 0),
    "hive-node": lambda: hive_node.model(3, 0),
    "ravager": lambda: ravager.model(0, 2),
    "wraith": lambda: wraith.model(0, 1),
    "mote-swarm": lambda: mote_swarm.model(0, 1.1),
    "driftjelly": lambda: m6.driftjelly(0.0),
    "lamprey": lambda: lamprey.model(0, phase=lamprey.TAU / lamprey.SWIM),
    "skimmer": lambda: skimmer.turned(lambda: m6.skimmer(0.0), skimmer.rotation(0)),
    "burrower": burrower.intel_model,
    "dust-kite": dust_kite.intel_model,
    "tendril-anchor": canyon_units.anchor_intel_model,
    "threadcrawler": canyon_units.threadcrawler_intel_model,
    "choir-herald": siege_units.herald_intel_model,
    "shellback": siege_units.shellback_intel_model,
    "dust-devil": storm_units.devil_intel_model,
    "ghost-drone": storm_units.ghost_intel_model,
    "warden-tank": warden_tank.intel_model,
    "revenant-walker": lambda: walker_model(),
    "drill-rig": drill_rig.intel_model,
    "glassfin": glassfin.intel_model,
    "eel-swarm": eel_swarm.intel_model,
    "abyss-ray": abyss_ray.intel_model,
    "barbfin": barbfin.intel_model,
    "siren": siren.intel_model,
    "spiral-nautilus": spiral_nautilus.intel_model,
    "glow-angler": glow_angler.intel_model,
}
# units the intel shows unnamed before their first level is flown: name -> the model whose outline it shows
UNCLASSIFIED = {"ghost-drone": storm_units.ghost_intel_model}
BOSSES = ("gorgon-frigate", "brood-carrier", "leviathan", "harbour-kraken", "siege-spire", "revenant-walker",
          "dust-colossus", "abyssal-maw")
# silhouettes of contacts the intel cannot classify: drawn with the unclassified pictures' sensor noise
NOISY = {"revenant-walker"}
# portraits that are not one model: name -> the 8x render (an RGBA array), as sdf.render gives it

PLATE = np.array([4, 16, 28], float)
GRID = np.array([16, 60, 80], float)
SCAN = np.array([60, 220, 255], float)
SHAPE = np.array([10, 52, 66], float)


def scuttler_model():
    """The Scuttler at heading 0 (walking down the screen), walk frame 0, as vrell_l04 renders it."""
    rot = np.pi
    base = vrell_l04.scuttler_scene(0.0)
    return (lambda p: base(rotate_z(p, rot))), model_space_materials(vrell_l04.scuttler_mats("unit"), rot)


def coilwyrm_model():
    """The Coilwyrm's head nose down with its first segment trailing up behind it, slightly off line,
    the production models (tools/art/coilwyrm.py) at heading 0."""
    from render import archetype_models as am
    head, mats = coilwyrm.turned(*am.coil_head(0.45), 0.0)
    seg, _ = coilwyrm.turned(*am.coil_segment(), 0.0)
    parts = [(head, (0.0, -0.28), 0.86), (seg, (0.14, 0.72), 0.62)]

    def scene(p):
        out = []
        for model, (x, y), k in parts:
            d, m = model((p - np.array([x, y, 0.0])) / k)
            out.append((d * k, m))
        return sdf.union(*out[::-1])
    return scene, mats


def walker_model(k=0.84):
    """The Revenant Walker walking down (heading 0, walk phase 0): legs, hull, the two pods aimed ahead and
    the flamer at rest against the heading, the production models of tools/art/revenant_walker.py scaled by
    ``k`` to fit the portrait."""
    rw = revenant_walker
    legs, torso, pod, flamer = rw.legs_scene(0.0), rw.torso_scene(0, True), rw.pod_scene(0), rw.flamer_scene()
    rot = rw.model_rotation(rw.heading(0))

    def model(p):
        parts = [legs(p), torso(p)]
        for side in (1, -1):
            parts.append(pod(p - sdf.vec(side * rw.PODS[0], rw.PODS[1], 0)))
        parts.append(flamer(rotate_z(p - sdf.vec(*rw.FLAMER_AT, 0), np.pi)))
        return sdf.union(*parts)

    def scene(p):
        d, m = model(rotate_z(p, rot) / k)
        return d * k, m
    return scene, model_space_materials(rw.mats(), rot)


def walker_mask():
    """The production walker at heading 0 as the game assembles it (legs, hull, pods, flamer at rest,
    tools/art/revenant_walker.walker_draw), the outline only."""
    rw = revenant_walker
    s = {name: rw.load(name) for name in rw.SET_NAMES}
    img, _ = rw.walker_draw(Image.new("RGBA", (300, 300), (0, 0, 0, 0)), s, 150, 150, 0, 0)
    return np.array(img)[..., 3] > 127


def sensor_noise(rim, n, seed):
    """The unclassified pictures' noise past the torn rows (``tear``): dropped rim pixels, and the mask of
    a speckle over the plate."""
    rng = np.random.default_rng(seed)
    rim &= rng.random(rim.shape) > 0.18
    return rim, rng.random((n, n)) > 0.94


def tear(shape, n, rows, seed):
    rng = np.random.default_rng(seed)
    for y in rng.choice(n, rows, replace=False):
        shape[y] = np.roll(shape[y], int(rng.choice((-2, -1, 1, 2))))
    return shape


def plate(n):
    """The sensor plate: dark teal with a dot grid, a scan line at two thirds and corner brackets."""
    yy, xx = np.mgrid[0:n, 0:n]
    img = np.empty((n, n, 3))
    img[...] = PLATE
    img[(xx % 5 == 2) & (yy % 5 == 2)] = GRID
    img[2 * n // 3] = PLATE * 0.5 + SCAN * 0.35
    arm = max(3, n // 8)
    for cy, cx in ((0, 0), (0, n - 1), (n - 1, 0), (n - 1, n - 1)):
        ys = slice(cy, cy + arm) if cy == 0 else slice(cy - arm + 1, cy + 1)
        xs = slice(cx, cx + arm) if cx == 0 else slice(cx - arm + 1, cx + 1)
        img[cy, xs] = SCAN
        img[ys, cx] = SCAN
    return img


def reef_spitter_hi():
    """The Reef Spitter: its gun (heading 0, aiming down the screen) on the kelp raft, the production
    models of tools/art/reef_spitter.py rendered together at the raft's scale, the gun over the raft."""
    size, extent = (PORTRAIT * 8, PORTRAIT * 8), reef_spitter.RAFT_EXTENT * 1.0
    raft_scene, raft_mats = reef_spitter.raft_model()
    raft = sdf.render(raft_scene, raft_mats, size, extent)
    gun_scene, gun_mats = m6.reef_gun(recoil=0.0, glow=1.0)
    rot = reef_spitter.gun_rotation(0)
    gun = sdf.render(lambda p: gun_scene(rotate_z(p, rot)), model_space_materials(gun_mats, rot), size, extent)
    ga, ra = gun[..., 3:4], raft[..., 3:4]
    alpha = ga + ra * (1 - ga)
    rgb = (gun[..., :3] * ga + raft[..., :3] * ra * (1 - ga)) / np.maximum(alpha, 1e-6)
    return np.concatenate([rgb, alpha], axis=-1)


def depth_hunter_hi():
    """The Depth Hunter nose down at a little more than the portraits' usual extent, so the whole glider fits."""
    scene, mats = depth_hunter.intel_model()
    return sdf.render(scene, mats, (PORTRAIT * 8, PORTRAIT * 8), 2.7)


def belt_hi(scene, mats, extent, camera=None):
    """M9 part B: a Level 29 unit's 8x render at its sprite's extent (tools/art/belt_units.py's models, Q.11 b's
    gunmetal), nose down; ``camera`` for the Stormhawk's perspective camera."""
    if camera:
        scene = artkit.perspective(scene, camera)
    return sdf.render(scene, mats, (PORTRAIT * 8, PORTRAIT * 8), extent)


def talon_hi():
    """The Talon nose down (heading 0), gold panels on the gunmetal hull, as belt_units.py's frames."""
    scene, mats = belt_units.e60.talon_model("b", False)
    return belt_hi(*belt_units.turned(scene, belt_units.gunmetal(mats), 0, 16), belt_units.TAL_EXT)


def sam_nest_hi():
    """The SAM Nest awake: the rack aiming down, hatch shut, over its round base with the red lights (reef_spitter_hi's
    composite of two models)."""
    size, extent = (PORTRAIT * 8, PORTRAIT * 8), belt_units.SAM_EXT
    base_scene, base_mats = belt_units.e60.sam_base("a", "red", 1.0)
    base = sdf.render(base_scene, belt_units.gunmetal(base_mats), size, extent)
    rack_scene, rack_mats = belt_units.e60.sam_rack("a", 0.0, 0.0, "red")
    rack = sdf.render(*belt_units.turned(rack_scene, belt_units.gunmetal(rack_mats), 0, 16), size, extent)
    ga, ba = rack[..., 3:4], base[..., 3:4]
    alpha = ga + ba * (1 - ga)
    rgb = (rack[..., :3] * ga + base[..., :3] * ba * (1 - ga)) / np.maximum(alpha, 1e-6)
    return np.concatenate([rgb, alpha], axis=-1)


def mite_hi():
    """The Asteroid Mite nose down in its crawl, the back glow teal."""
    scene, mats = belt_units.e60.mite_model("a", phase=0.0, pose="cling")
    return belt_hi(*belt_units.turned(scene, mats, 0, 16), belt_units.MITE_EXT)


def clamp_gun_hi():
    """The clamp gun aiming down: the gunmetal turret, gold collar, its barrel and red lamp."""
    return belt_hi(*belt_units.turned(*belt_units.clamp_gun_model(), 0, 16), belt_units.CLAMP_EXT)


def hawk_hi():
    """The wavering Ceres Wing Stormhawk nose down, wings level, its IFF light lit white (the holding-fire blink)."""
    scene, mats = belt_units.b60.stormhawk_model("a", np.pi / 2, 0, "white")
    return belt_hi(scene, mats, belt_units.HAWK_EXT, belt_units.HAWK_CAM)


def minelayer_hi():
    """The Minelayer nose down, its drum dark (belt_units_c.py's frames)."""
    return belt_hi(*belt_units_c.turned(*belt_units_c.layer_model(0.0), 0, 16), belt_units_c.ML_EXT)


def gunship_hi():
    """The Gilded Gunship facing down, doors shut and ring lamps dark (its idle frame)."""
    return belt_hi(*belt_units_c.turned(*belt_units_c.gunship_model(), 0, 16), belt_units_c.GS_EXT)


def dart_hi():
    """The Dart Drone nose down, its engine lit."""
    return belt_hi(*belt_units_c.turned(*belt_units_c.dart_model(), 0, 16), belt_units_c.DART_EXT)


def rail_serpent_hi():
    """The Rail Serpent's engine car nose down, its white-gold reactor lit (the vital part)."""
    scene, mats = belt_units_c.turned(*belt_units_c.rail_model("engine"), 0, 32)
    return belt_hi(scene, mats, belt_units_c.RS_EXT * belt_units_c.RS_SIZE["engine"] / 48)


def hornet_hi():
    """The Hornet facing down, its hatches dark (belt_units_d.py's idle frame)."""
    return belt_hi(*belt_units_d.turned(*belt_units_d.hornet_model(), 0, 16), belt_units_d.HOR_EXT)


def shard_drone_hi():
    """The Shard Drone's tri-prism (its first spin frame)."""
    return belt_hi(*belt_units_d.turned(*belt_units_d.shard_model(), 0, 48), belt_units_d.SH_EXT)


def buzzsaw_drone_hi():
    """The Buzzsaw Drone's six gold-edged blades round the gunmetal hub."""
    return belt_hi(*belt_units_d.turned(*belt_units_d.saw_model(), 0, 48), belt_units_d.SAW_EXT)


HI = {"reef-spitter": reef_spitter_hi, "relay-beacon": relay_beacon.intel_hi, "depth-hunter": depth_hunter_hi,
      "talon": talon_hi, "sam-nest": sam_nest_hi, "asteroid-mite": mite_hi, "clamp-gun": clamp_gun_hi,
      "wavering-stormhawk": hawk_hi, "minelayer": minelayer_hi, "gilded-gunship": gunship_hi, "dart-drone": dart_hi,
      "rail-serpent": rail_serpent_hi, "hornet": hornet_hi, "shard-drone": shard_drone_hi,
      "buzzsaw-drone": buzzsaw_drone_hi}


def portrait(slug):
    if slug in HI:
        hi = HI[slug]()
    else:
        scene, mats = ENEMIES[slug]()
        hi = sdf.render(scene, mats, (PORTRAIT * 8, PORTRAIT * 8), e3.EXTENT * 1.06)
    unit = np.array(artkit.native(hi, 8)).astype(np.float64)
    a = unit[..., 3:4] / 255
    rgb = unit[..., :3] * 0.82 + SCAN * 0.12 * (unit[..., :3].mean(-1, keepdims=True) / 255 + 0.3)
    card = plate(PORTRAIT) * (1 - a) + rgb * a
    img = Image.fromarray(np.clip(card, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    return artkit.quantize_set([img], COLOURS)[0]


def gorgon_mask():
    layer = Image.new("RGBA", (480, 480), (0, 0, 0, 0))
    bosses_r06.gorgon_compose(layer, 240, 190, np.pi / 2, 0.0, [np.pi / 2] * 3)
    return np.array(layer)[..., 3] > 127


def carrier_mask():
    scene, mats = em.brood_carrier(0.0, 1.0, 0.0, 0.0)
    hi = sdf.render(scene, mats, (e3.BW, e3.BH), e3.EXTENT)
    return hi[..., 3] > 0.5


def leviathan_mask():
    """The production Leviathan facing down (the second pass's view), every part on, the tail
    straight, at 2x its 300x480 frame: the outline only, so no shadows."""
    model, mats = leviathan.unit_model(0.0)
    rot = leviathan.ROT_DOWN
    w, h = leviathan.W * 2, leviathan.H * 2
    scene = lambda p: model(rotate_z(p, rot))  # noqa: E731
    hi = sdf.render(scene, model_space_materials(mats * (len(leviathan.PARTS) + 1), rot), (w, h),
                    leviathan.W / leviathan.S, shadows=False, steps=140)
    return hi[..., 3] > 0.5


def kraken_mask():
    """The Harbour Kraken's head and mantle facing down, the production `harbour-kraken-sub` sprite's
    outline (tools/art/harbour_kraken.py: what lies under the platform), the arms left out."""
    return np.array(Image.open(SPRITES / "harbour-kraken-sub.png").convert("RGBA"))[..., 3] > 127


def spire_mask():
    """The Siege Spire rooted, as the arena shows it at first: the production trunk (crown shut), its six
    live roots and their pods (tools/art/siege_spire.py's sprites placed by assets/pivots/siege-spire.json),
    the outline only."""
    import json
    piv = json.loads((ROOT / "assets" / "pivots" / "siege-spire.json").read_text(encoding="utf-8"))
    layer = Image.new("RGBA", (520, 520), (0, 0, 0, 0))
    cx, cy = 260, 260

    def put(img, x, y):
        layer.alpha_composite(img, (int(round(cx + x - img.width / 2)), int(round(cy + y - img.height / 2))))
    for i, r in enumerate(piv["roots"]):
        put(Image.open(SPRITES / f"siege-spire-root_{2 * i}.png").convert("RGBA"), *r["offset"])
    put(Image.open(SPRITES / "siege-spire_0.png").convert("RGBA"), *piv["trunk"]["offset"])
    for r in piv["roots"]:
        pod = "siege-spire-pod_0.png" if r["kind"] == "turret" else "siege-spire-mortar_0.png"
        put(Image.open(SPRITES / pod).convert("RGBA"), *r["pod"])
    return np.array(layer)[..., 3] > 127


def silhouette(slug):
    mask = {"gorgon-frigate": gorgon_mask, "brood-carrier": carrier_mask, "leviathan": leviathan_mask,
            "harbour-kraken": kraken_mask, "siege-spire": spire_mask, "revenant-walker": walker_mask,
            "dust-colossus": dust_colossus.intel_mask,
            "abyssal-maw": abyssal_maw.intel_mask}[slug]()
    ys, xs = np.nonzero(mask)
    mask = mask[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
    inner = SILHOUETTE - 6
    scale = inner / max(mask.shape)
    size = (max(1, round(mask.shape[1] * scale)), max(1, round(mask.shape[0] * scale)))
    small = np.array(Image.fromarray((mask * 255).astype(np.uint8)).resize(size, Image.BOX)) > 110
    shape = np.zeros((SILHOUETTE, SILHOUETTE), bool)
    y0, x0 = (SILHOUETTE - size[1]) // 2, (SILHOUETTE - size[0]) // 2
    shape[y0:y0 + size[1], x0:x0 + size[0]] = small
    if slug in NOISY:
        shape = tear(shape, SILHOUETTE, 7, 4219)
    card = plate(SILHOUETTE)
    yy, xx = np.mgrid[0:SILHOUETTE, 0:SILHOUETTE]
    card[shape] = SHAPE
    card[shape & ((xx % 5 == 2) | (yy % 5 == 2))] = SHAPE * 0.6 + SCAN * 0.25
    pad = np.pad(shape, 1)
    rim = shape & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    if slug in NOISY:
        rim, speck = sensor_noise(rim, SILHOUETTE, 4220)
        card[rim] = SCAN
        card[speck] = card[speck] * 0.5 + SCAN * 0.3
        img = Image.fromarray(np.clip(card, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        return artkit.quantize_set([img], 16)[0]
    card[rim] = SCAN
    return Image.fromarray(card.astype(np.uint8), "RGB").convert("RGBA")


def unclassified(slug):
    """The sensor's picture of a unit it cannot name: the model's outline at the portrait's scale (nose down),
    filled flat in dark teal with the grid, a cyan rim, then broken by sensor noise: a few rows torn sideways
    by a pixel or two, dropped pixels along the rim, a noise speckle over the plate."""
    scene, mats = UNCLASSIFIED[slug]()
    hi = sdf.render(scene, mats, (PORTRAIT * 8, PORTRAIT * 8), e3.EXTENT * 1.06)
    shape = np.array(artkit.native(hi, 8, crisp=0))[..., 3] > 127
    rng = np.random.default_rng(4501)
    for y in rng.choice(PORTRAIT, 6, replace=False):
        shape[y] = np.roll(shape[y], int(rng.choice((-2, -1, 1, 2))))
    card = plate(PORTRAIT)
    yy, xx = np.mgrid[0:PORTRAIT, 0:PORTRAIT]
    card[shape] = SHAPE
    card[shape & ((xx % 5 == 2) | (yy % 5 == 2))] = SHAPE * 0.6 + SCAN * 0.25
    pad = np.pad(shape, 1)
    rim = shape & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    rim &= rng.random(rim.shape) > 0.18
    card[rim] = SCAN
    speck = rng.random((PORTRAIT, PORTRAIT)) > 0.94
    card[speck] = card[speck] * 0.5 + SCAN * 0.3
    img = Image.fromarray(np.clip(card, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    return artkit.quantize_set([img], 16)[0]


def render(job):
    kind, slug = job
    if kind == "unclassified":
        return unclassified(slug)
    return portrait(slug) if kind == "enemy" else silhouette(slug)


def picture_name(job):
    kind, slug = job
    return f"boss-{slug}" if kind == "boss" else f"unclassified-{slug}" if kind == "unclassified" else slug


JOBS = ([("enemy", slug) for slug in list(ENEMIES) + list(HI)] + [("boss", slug) for slug in BOSSES]
        + [("unclassified", slug) for slug in UNCLASSIFIED])


def batch_of(name):
    return BATCHES.get(name, UI_BATCH)


def build(names):
    OUT.mkdir(parents=True, exist_ok=True)
    jobs = [job for job in JOBS if not names or picture_name(job) in names]
    for old in OUT.glob("*.png") if not names else (OUT / f"{picture_name(job)}.png" for job in jobs):
        old.unlink(missing_ok=True)
    with ProcessPoolExecutor() as pool:
        for job, img in zip(jobs, pool.map(render, jobs)):
            name = picture_name(job)
            artkit.save_png(img, OUT / f"{name}.png", artkit.source_note(SCRIPT, batch_of(name)))
    print(f"{len(jobs)} intel pictures in {OUT.relative_to(ROOT)}")


def review(names):
    """The review sheets of the named pictures' batches; without names every batch's."""
    batches = {batch_of(name) for name in names or map(picture_name, JOBS)}
    for batch in sorted(batches, key=list(ROUNDS).index):
        review_batch(batch, [picture_name(job) for job in JOBS if batch_of(picture_name(job)) == batch])


def review_batch(batch, names):
    rnd = ROUNDS[batch]
    sheet = raster.sheet(16 + 6 * 180 + 16, 60 + 150 + 20, f"HANGAR INTEL (FINAL {rnd.upper()}): SENSOR L2 PICTURES",
                         f"PRODUCTION ART, {batch.upper()} - {rnd.upper()}")
    bosses = any(name.startswith("boss-") for name in names)
    raster.draw_text(sheet, 16, 38, f"ENEMY PORTRAITS {PORTRAIT}X{PORTRAIT}"
                                    + (f" AND BOSS SILHOUETTES {SILHOUETTE}X{SILHOUETTE}" if bosses else "")
                                    + ", AT 1X AND 3X", raster.LABEL)
    for i, name in enumerate(names):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        x, y = 16 + i * 180, 56
        sheet.alpha_composite(img, (x, y))
        sheet.alpha_composite(sprite.enlarge(img, 3), (x + img.width + 6, y))
        raster.draw_text(sheet, x, y + 3 * img.height + 4,
                         f"{name.upper()} ({artkit.colour_count([img])} COL)", raster.LABEL_DIM)
    path = CONCEPT / f"intel-final-{rnd}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


if __name__ == "__main__":
    names = [a for a in sys.argv[1:] if not a.startswith("--")]
    unknown = set(names) - set(map(picture_name, JOBS))
    if unknown:
        sys.exit(f"unknown intel pictures: {', '.join(sorted(unknown))}")
    if "--review" not in sys.argv[1:]:
        build(names)
    review(names)
