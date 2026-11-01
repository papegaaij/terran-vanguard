#!/usr/bin/env python3
"""Concept round 76 (M10 "Act 6 - Jovian Storm" part D, step m10-d5, 2026-11-01): Level 38's voices for review;
pending the user's review. Joins the production files only (no new take): built on tools/art/voice.py's line list and
tools/concept/audio/round18's decoder and writer, as voice_r75.py.

Outputs (design/audio/voice/concept/):
  level-38-listen-r76-a.ogg: a listen-through of every Level 38 line step VOX-D voiced, as the game plays them (the
      production files in assets/voice/, unchanged): its two briefing pages (Okafor, Varga), then its radio lines in
      the order of their time (the event lines at their likeliest place in a run: Rook's first strike kill after the
      t=33 column's strike, the cargo pod's secret line after the t=122.5 column's strike, the secondary met and the
      level's end last); 1.2 s apart (2 s after the briefing). The cue list with each line's start in the file goes to
      level-38-listen-r76-a.txt beside it (the round's row quotes it).

Rerun: python3 tools/concept/audio/voice_r76.py --lines <lines.json>
       (the line list from `:pipeline:voiceLines`, run isolated as tools/art/voice.py runs it; without --lines the
       list voice.py last wrote, tools/art/voice.py's LINES, is read)
"""
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
import voice as V  # noqa: E402

DEST = ROOT / "design" / "audio" / "voice" / "concept"
LEVEL = "level-38-lightning-belt"
GAP = 1.2
SECTION_GAP = 2.0
# the event lines' likeliest place in a run (the level's radio has them by event, not by time)
EVENT_TIME = {"FIRST_STRIKE_KILL": 34.5, "secret": 123.6, "SECONDARY_OBJECTIVE": 196.5, "SECONDARY_FAILED": 196.6,
              "LEVEL_END": 197.0}


def level38(all_lines):
    """Level 38's briefing pages and its radio lines, in playing order."""
    def src(line):
        return line["source"]
    brief = sorted((ln for ln in all_lines if LEVEL in src(ln) and "briefing" in src(ln)), key=src)
    radio = [ln for ln in all_lines if LEVEL in src(ln) and "briefing" not in src(ln)]

    def when(line):
        s = src(line)
        for event, t in EVENT_TIME.items():
            if event in s:
                return t
        for tok in s.replace("=", " ").split():
            try:
                return float(tok.rstrip("s:,"))
            except ValueError:
                pass
        return 999.0
    radio.sort(key=lambda ln: (when(ln), src(ln)))
    return brief, radio


def post(all_lines):
    import numpy as np
    r18 = V._filters()
    s = r18._synth()
    brief, radio = level38(all_lines)
    parts, cues, t = [], [], 0.0
    groups = [("Level 38 briefing", brief), ("Level 38 radio", radio)]
    for g, (name, group) in enumerate(groups):
        for i, ln in enumerate(group):
            x = s.decode(ROOT / "assets" / ln["path"])[0]
            m, sec = divmod(t, 60)
            cues.append(f"{int(m)}:{sec:04.1f}  {name}: {ln['voice']} ({ln['source']}, {ln['expression']}): {ln['text']}")
            parts.append(x)
            t += len(x) / s.SR
            last = i == len(group) - 1 and g < len(groups) - 1
            gap = SECTION_GAP if last else GAP
            parts.append(np.zeros(int(gap * s.SR)))
            t += gap
    y = np.concatenate(parts[:-1])[None, :]
    s.write_ogg(DEST / "level-38-listen-r76-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r76.py listen-through (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r76.py"})
    (DEST / "level-38-listen-r76-a.txt").write_text(
        "Level 38 listen-through (round 76): each line's start in level-38-listen-r76-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"listen-through: {len(cues)} lines, {t:.1f} s")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    post(json.loads(path.read_text()))


if __name__ == "__main__":
    main(sys.argv[1:])
