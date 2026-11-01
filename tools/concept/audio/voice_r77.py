#!/usr/bin/env python3
"""Concept round 77 (M10 "Act 6 - Jovian Storm" part E, step m10-e5, 2026-11-01): Level 39's voices for review;
pending the user's review. Joins the production files only (no new take): built on tools/art/voice.py's line list and
tools/concept/audio/round18's decoder and writer, as voice_r76.py.

Outputs (design/campaign/act-6-jovian-storm/level-39-io-flyby/concept/):
  level-39-listen-r77-a.ogg: a listen-through of every Level 39 line step VOX-E voiced, as the game plays them (the
      production files in assets/voice/, unchanged): its two briefing pages (Okafor, Varga), then its radio lines in
      the order of their time (the event lines at their likeliest place in a run: Rook's first tower down in the Loki
      hold, the survey hut's secret line, Okafor's topple line in the Pele hold, the secondary met after P6, the level's
      end, then the six failed-primary lines, one per tower, last); 1.2 s apart (2 s after the briefing). The cue list
      with each line's start in the file goes to level-39-listen-r77-a.txt beside it (the round's row quotes it).
  vorne-t91-r77-a.ogg: Level 39's t=91 line alone, as built (the production file: no expression in the data, so the
      neutral row, the enemy filter), for round 77's pick against his grim row (V.2).

Rerun: python3 tools/concept/audio/voice_r77.py --lines <lines.json>
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

DEST = ROOT / "design" / "campaign" / "act-6-jovian-storm" / "level-39-io-flyby" / "concept"   # beside the level (the voice docs stay VOX steps')
LEVEL = "level-39-io-flyby"
GAP = 1.2
SECTION_GAP = 2.0
# the event lines' likeliest place in a run (the level's radio has them by event, not by time)
EVENT_TIME = {"FIRST_TARGET": 46.0, "secret": 97.0, "TOPPLE_TARGET": 128.0, "SECONDARY_OBJECTIVE": 158.5,
              "LEVEL_END": 197.0, "MISSION_FAILED": 998.0}


def level39(all_lines):
    """Level 39's briefing pages and its radio lines, in playing order."""
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
    brief, radio = level39(all_lines)
    parts, cues, t = [], [], 0.0
    groups = [("Level 39 briefing", brief), ("Level 39 radio", radio)]
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
    s.write_ogg(DEST / "level-39-listen-r77-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r77.py listen-through (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r77.py"})
    vorne = next(ln for ln in radio if ln["voice"] == "vorne")
    s.write_ogg(DEST / "vorne-t91-r77-a.ogg", s.decode(ROOT / "assets" / vorne["path"])[0][None, :], quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r77.py: Level 39's t=91 line as built (assets/voice file)",
                      "SOURCE": "tools/concept/audio/voice_r77.py"})
    (DEST / "level-39-listen-r77-a.txt").write_text(
        "Level 39 listen-through (round 77): each line's start in level-39-listen-r77-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"listen-through: {len(cues)} lines, {t:.1f} s")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    post(json.loads(path.read_text()))


if __name__ == "__main__":
    main(sys.argv[1:])
