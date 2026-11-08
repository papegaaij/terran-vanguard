#!/usr/bin/env python3
"""Concept rounds 94 and 95 (M11 parts G and H, step m11-gh6, 2026-11-08): the voice of Levels 49 and 50 and the ending for
review; pending the user's review. Joins the production files only (no new take), as voice_r93.py.

Outputs (design/audio/voice/concept/), each with a cue list (.txt) beside it:
  level-49-echoes-r94-a.ogg: Level 49's briefing pages and radio lines in the level's order, 1.5 s apart (the Choir's two
      on their clean channel), as the game plays them (the production files in assets/voice/, unchanged).
  level-49-choir-r94-a.ogg: the Choir's two Level 49 lines alone.
  level-50-heart-r95-a.ogg: the Heart's five lines in the fearful, slower dress (round 84 V.1 a: tempo 0.85, the choir
      stagger, line 5 dropping into one voice), in the fight's order.
  level-50-lines-r95-a.ogg: Level 50's briefing pages and radio lines without the Heart's five.
  act-7-ending-r95-a.ogg: the four ending pages (dry), in order.

Round 96 (M11 part I, step VOX-I, 2026-11-08: the Act 7 listen-through, after the Whisper read-back of every Act 7
file; --round 96), each with its cue list (.txt; the line's start, speaker, source, filter, noise step and text):
  act-7-intro-listen-r96-a.ogg: the act intro's four pages (page 3 the Choir through the gate).
  level-NN-listen-r96-a.ogg (NN 43-50): the level's briefing pages, then its radio lines in the data's order (timed
      cues, the event cues where the data lists them, Rook's escort-only lines and the `requires_not` rows, the
      secondary's outcomes, the secrets), every file the game can play in the level.
  act-7-ending-listen-r96-a.ogg: the campaign's four ending pages (dry).

Rerun: python3 tools/concept/audio/voice_r9495.py [--lines <lines.json>] [--round 96]
       (without --lines the list voice.py last wrote, tools/art/voice.py's LINES, is read; without --round, rounds 94
       and 95's files)
"""
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
import voice as V  # noqa: E402

DEST = ROOT / "design" / "audio" / "voice" / "concept"
GAP = 1.5


def join(s, lines, name, title, noise=False):
    import numpy as np
    parts, cues, t = [], [], 0.0
    for i, ln in enumerate(lines):
        x = s.decode(ROOT / "assets" / ln["path"])[0]
        m, sec = divmod(t, 60)
        where = ln["source"].split("/")[-1]
        dress = ln["filter"] + (f", noise {ln.get('noise', 0)}" if noise and ln["filter"] == "radio" else "")
        cues.append(f"{int(m)}:{sec:04.1f}  {i + 1}, {ln['voice']} ({where}, {dress}): {ln['text']}")
        parts.append(x)
        t += len(x) / s.SR
        parts.append(np.zeros(int(GAP * s.SR)))
        t += GAP
    y = np.concatenate(parts[:-1])[None, :]
    s.write_ogg(DEST / f"{name}.ogg", y, quality=4,
                tags={"COMMENT": f"tools/concept/audio/voice_r9495.py {title} (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r9495.py"})
    (DEST / f"{name}.txt").write_text(f"{title}: each line's start in {name}.ogg\n" + "\n".join(cues) + "\n")
    print(f"{name}: {len(cues)} lines, {t - GAP:.1f} s")


ACT7 = "act-7-beyond-the-gate"


def round96(s, all_lines):
    """The Act 7 listen-through: one file per level 43-50, the intro and the ending."""
    act = [ln for ln in all_lines if ln["source"].startswith(ACT7)]
    join(s, [ln for ln in act if ln["source"].startswith(f"{ACT7} briefing page")],
         "act-7-intro-listen-r96-a", "Act 7 intro listen-through (round 96)", noise=True)
    for n in range(43, 51):
        level = [ln for ln in act if ln["source"].startswith(f"{ACT7}/level-{n}-")]
        join(s, level, f"level-{n}-listen-r96-a", f"Level {n} listen-through (round 96)", noise=True)
    join(s, [ln for ln in act if ln["source"].startswith(f"{ACT7} ending page")],
         "act-7-ending-listen-r96-a", "Act 7 ending listen-through (round 96)")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    all_lines = json.loads(path.read_text())
    s = V._filters()._synth()
    if "--round" in args and args[args.index("--round") + 1] == "96":
        round96(s, all_lines)
        return
    l49 = [ln for ln in all_lines if "level-49-echoes" in ln["source"]]
    l50 = [ln for ln in all_lines if "level-50-choir-heart" in ln["source"]]
    heart = [ln for ln in l50 if ln["voice"] == "choir"]
    join(s, l49, "level-49-echoes-r94-a", "Level 49's lines")
    join(s, [ln for ln in l49 if ln["voice"] == "choir"], "level-49-choir-r94-a", "Level 49's Choir lines")
    join(s, heart, "level-50-heart-r95-a", "the Heart's five lines")
    join(s, [ln for ln in l50 if ln["voice"] != "choir"], "level-50-lines-r95-a", "Level 50's lines")
    join(s, [ln for ln in all_lines if "act-7-beyond-the-gate ending page" in ln["source"]],
         "act-7-ending-r95-a", "Act 7's ending pages")


if __name__ == "__main__":
    main(sys.argv[1:])
