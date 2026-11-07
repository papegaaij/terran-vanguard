#!/usr/bin/env python3
"""Concept round 93 (M11 part F, step m11-ef5, 2026-11-07): Vorne's Level 48 voice for review; pending the user's
review. Joins the production files only (no new take): built on tools/art/voice.py's line list and
tools/concept/audio/round18's decoder and writer, as voice_r75.py.

Outputs (design/audio/voice/concept/):
  level-48-vorne-r93-a.ogg: the fight's nine lines (the Chimera's document's order): Vorne's eight in the fused voice (step VOX-EF: his own voice with the
      Choir's copies under it, -12/-5/-19 semitones, in a 3.2 s hall; the copies 8 dB under him for lines 1-3, then
      4 dB from his shout on) and the Choir's line 4 on its clean channel, as the game plays them on the enemy channel (the production files in assets/voice/,
      unchanged), in the fight's order, 1.5 s apart. The cue list with each line's start goes to
      level-48-vorne-r93-a.txt beside it.
  level-48-vorne-shout-r93-a.ogg: his shout "Get out of my head!" as the game plays it (round 73's auditioned
      treatment at 112-115 Hz, the fused filter's later stage): a copy of the production file.

Rerun: python3 tools/concept/audio/voice_r93.py [--lines <lines.json>]
       (without --lines the list voice.py last wrote, tools/art/voice.py's LINES, is read)
"""
import json
import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
import voice as V  # noqa: E402

DEST = ROOT / "design" / "audio" / "voice" / "concept"
LEVEL = "level-48-vornes-end"
GAP = 1.5
SHOUT = "Get out of my head!"


def post(all_lines):
    import numpy as np
    r18 = V._filters()
    s = r18._synth()
    # the level data's order is the fight's order (the arrival, then the phase lines in turn, the cut line last)
    vorne = [ln for ln in all_lines if LEVEL in ln["source"] and (
        ln["voice"] == "vorne" or (ln["voice"] == "choir" and "he is ours" in ln["text"]))]
    shout = next(ln for ln in vorne if ln["text"] == SHOUT)
    shutil.copyfile(ROOT / "assets" / shout["path"], DEST / "level-48-vorne-shout-r93-a.ogg")
    print("shout:", shout["path"], shout["expression"], shout["filter"])
    parts, cues, t = [], [], 0.0
    for i, ln in enumerate(vorne):
        x = s.decode(ROOT / "assets" / ln["path"])[0]
        m, sec = divmod(t, 60)
        cues.append(f"{int(m)}:{sec:04.1f}  line {i + 1}, {ln['voice']} ({ln['source'].split(' radio ')[-1]}, {ln['filter']}): "
                    f"{ln['text']}")
        parts.append(x)
        t += len(x) / s.SR
        parts.append(np.zeros(int(GAP * s.SR)))
        t += GAP
    y = np.concatenate(parts[:-1])[None, :]
    s.write_ogg(DEST / "level-48-vorne-r93-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r93.py Vorne's Level 48 lines (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r93.py"})
    (DEST / "level-48-vorne-r93-a.txt").write_text(
        "Vorne's Level 48 lines (round 93): each line's start in level-48-vorne-r93-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"lines: {len(cues)} lines, {t - GAP:.1f} s")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    post(json.loads(path.read_text()))


if __name__ == "__main__":
    main(sys.argv[1:])
