"""Shared helpers of the M7 part A production art (the Act 3 arsenal: ion_beam.py, light_drone.py,
act3_pods.py, act3_specials.py, and the new icons in icons.py), on top of artkit.

* **Held sprites.** The atlas packer fails on a sprite no data claims (vanguard.pipeline.SpriteUse:
  a weapon's sprites are claimed by its slug once ``design/player/weapons/<slug>/data.yaml`` exists,
  a special's by its name as a slug once ``design/player/specials/data.yaml`` lists it). A frame set
  whose root is not claimed yet is written to ``build/art-held/sprites/`` instead of
  ``assets/sprites/`` (and its review is built from there); rerun the script once the data is in
  and it lands in ``assets/``. The pods (``pod-*``) and the icons are always claimed.
* **Review files** of round 37: a chosen look's production review is
  ``<subject>-final-r37-a.png/.gif``; a look round 36 did not choose gets a/b proposals,
  ``<subject>-r37-<variant>.png/.gif`` (variant a is the one written to ``assets/``).
"""
import contextlib
import re

import yaml

import artkit
from artkit import DESIGN, ROOT
from render.enemy_rigs import write_gif  # noqa: E402

BATCH = "M7 part A batch"
ROUND = "r37"
HELD = ROOT / "build" / "art-held" / "sprites"
ASSETS_SPRITES = artkit.SPRITES


def special_slugs():
    data = yaml.safe_load((DESIGN / "player" / "specials" / "data.yaml").read_text(encoding="utf-8"))
    return {re.sub(r"[^a-z0-9]+", "-", s["name"].lower()).strip("-") for s in data["specials"]}


def claimed(root):
    """Whether the game data claims the sprite root yet (a weapon slug or a special's slug)."""
    if (DESIGN / "player" / "weapons" / root / "data.yaml").exists():
        return True
    return root in special_slugs()


@contextlib.contextmanager
def sprites_of(root):
    """artkit's sprite folder for the frame sets of ``root``: assets/sprites when the data claims
    the root, build/art-held/sprites until then."""
    target = ASSETS_SPRITES if claimed(root) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def where(root):
    return "assets/sprites" if claimed(root) else "build/art-held/sprites (not claimed by the data yet)"


def save_proposal(sheet, gif_frames, concept_dir, subject, variant, fps=20, colors=128, chosen=None):
    """An a/b proposal's sheet and GIF, ``<subject>-r37-<variant>``; once the user has picked
    (``chosen``), the other variant goes to ``concept/rejected/`` (round 37 closed, 2026-10-13)."""
    if chosen is not None and variant != chosen:
        concept_dir = concept_dir / "rejected"
    concept_dir.mkdir(parents=True, exist_ok=True)
    png = concept_dir / f"{subject}-{ROUND}-{variant}.png"
    gif = concept_dir / f"{subject}-{ROUND}-{variant}.gif"
    sheet.convert("RGB").save(png, optimize=True)
    size = write_gif(gif_frames, gif, fps=fps, colors=colors)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def quantize_bright(frames, colours, weight=12):
    """artkit.quantize_set with the bright pixels weighted when the palette is cut (a glow is
    mostly dim light; a plain median cut would merge its white core into the halo)."""
    import numpy as np
    from PIL import Image
    arrays = [np.array(f.convert("RGBA")) for f in frames]
    pixels = np.concatenate([a[a[..., 3] > 0][:, :3] for a in arrays])
    if len(pixels) == 0:
        return frames
    bright = pixels[pixels.max(axis=1) >= 170]
    pool = np.concatenate([pixels] + [bright] * weight)
    palette = Image.fromarray(pool.reshape(-1, 1, 3), "RGB").quantize(
        colors=colours, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    out = []
    for a in arrays:
        rgb = Image.fromarray(np.ascontiguousarray(a[..., :3]), "RGB")
        mapped = np.array(rgb.quantize(palette=palette, dither=Image.Dither.NONE).convert("RGB"))
        result = np.dstack([mapped, a[..., 3]])
        result[a[..., 3] == 0] = 0
        out.append(Image.fromarray(result.astype(np.uint8), "RGBA"))
    return out


# --------------------------------------------------------------------------- wing pods

def render_pods(module, factory, arg, scale=1.0, colours=24):
    """A new wing-pod type the way tools/art/stormhawk.py cuts the round-08 pods: the Stormhawk
    rendered at every banking frame with the pod on both wings (``module.factory(arg)`` -> (scene,
    materials) in the right wing pod's local units, origin at the mount v8.POD_AT, mirrored to the left
    wing, ``scale`` model units per local unit; by name, so the render workers can import it), minus
    the bare hull; returns ({"left": frames, "right": frames},
    {"left": offsets, "right": offsets}) with one palette for both sides."""
    from concurrent.futures import ProcessPoolExecutor
    import stormhawk
    jobs = [(b, None) for b in stormhawk.BANKS] + [(b, (module, factory, arg, scale)) for b in stormhawk.BANKS]
    with ProcessPoolExecutor() as pool:
        renders = list(pool.map(_render_pod_job, jobs))
    n = len(stormhawk.BANKS)
    factor = renders[0][1]
    pieces = {"left": [], "right": []}
    for k in range(n):
        hi_ship, hi_pod = renders[k][0], renders[n + k][0]
        cut = stormhawk.pod_overlay(hi_pod, hi_ship, factor, artkit.native(hi_pod, factor))
        for side, piece in cut.items():
            pieces[side].append(piece)
    frames = artkit.quantize_set([p[0] for side in ("left", "right") for p in pieces[side]], colours)
    return ({"left": frames[:n], "right": frames[n:]},
            {side: [list(p[1]) for p in pieces[side]] for side in ("left", "right")})


def _render_pod_job(job):
    import numpy as np
    import stormhawk
    import vfx_r08 as v8
    from render.palette import B
    from render.sdf import mirror_x, rotate_y, union
    bank, pod = job
    scene, mats = v8.ship_model(np.radians(bank), None, B.ship_colors())
    if pod is not None:
        import importlib
        module, factory, arg, scale = pod
        local_scene, local_mats = getattr(importlib.import_module(module), factory)(arg)
        base = len(mats)

        def with_pod(p, scene=scene):
            d, m = scene(p)
            q = (mirror_x(rotate_y(p, np.radians(bank))) - v8.POD_AT) / scale
            d2, m2 = local_scene(q)
            return union((d, m), (d2 * scale, m2 + base))
        scene, mats = with_pod, mats + list(local_mats)
    return artkit.render_hi(artkit.perspective(scene, stormhawk.CAMERA), mats,
                            (stormhawk.SIZE, stormhawk.SIZE), stormhawk.EXTENT)


def write_pods(pod, frames, offsets, source):
    """``pod-<pod>-<left|right>_0..4`` into assets/sprites and their offsets into
    assets/pivots/pods.json (tools/art/stormhawk.py's file: the other pods' entries are kept)."""
    import json
    path = artkit.PIVOTS / "pods.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    for side in ("left", "right"):
        name = f"pod-{pod}-{side}"
        artkit.write_frames(name, frames[side], source)
        data["offsets"][name] = offsets[side]
    path.write_text(json.dumps(data, indent=1) + "\n", encoding="utf-8")


def pod_composite(bank, pod):
    """The hull of one banking frame with a pod type's sprites at their offsets (as the game)."""
    import json
    from PIL import Image
    hull = artkit.load_frames("ship")[bank]
    pods = json.loads((artkit.PIVOTS / "pods.json").read_text(encoding="utf-8"))
    img = Image.new("RGBA", hull.size, (0, 0, 0, 0))
    img.alpha_composite(hull)
    for side in ("left", "right"):
        name = f"pod-{pod}-{side}"
        img.alpha_composite(artkit.load_frames(name)[bank], tuple(pods["offsets"][name][bank]))
    return img
