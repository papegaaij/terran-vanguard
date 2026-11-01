#!/usr/bin/env python3
"""Concept round 75 (M10 "Act 6 - Jovian Storm" part C, step m10-c5, 2026-11-01): Level 37's voices for review;
pending the user's review. Joins the production files only (no new take): built on tools/art/voice.py's line list and
tools/concept/audio/round18's decoder and writer, as voice_r74.py.

Outputs (design/audio/voice/concept/):
  level-37-listen-r75-a.ogg: a listen-through of every Level 37 line step VOX-C voiced, as the game plays them (the
      production files in assets/voice/, unchanged): its two briefing pages (Okafor, Varga), then its radio lines in
      the order of their time (the event lines at their likeliest place in a run: the first bounce after the t=38
      mirrors' first burst, the first shuttle lost after pair 1's station, the CDF locker at t=61, the first prey kill
      after the t=118 snake's ram, the secondary's outcome (met, then failed) as pair 3 leaves, the level's end last);
      1.2 s apart (2 s after the briefing). The cue list with each line's start in the file goes to
      level-37-listen-r75-a.txt beside it (the round's row quotes it).
  act-6-intro-page-4-r75-a.ogg: the Act 6 intro's page 4 as the game plays it since step VOX-C0 (round 74 V.2 b's grim,
      slower reading rendered by the pipeline at the act data's `grim` row: cfg_weight 0.5, seed 103): a copy of the
      production file, for comparison with round 74's take `act-6-intro-page-4-r74-b.ogg` (cfg_weight 0.4).
  level-37-vorne-r75-a.ogg: Vorne's t=110 order as the game plays it (the `rage` expression row of his voice, the
      enemy-channel filter): a copy of the production file.

Rerun: python3 tools/concept/audio/voice_r75.py --lines <lines.json>
       (the line list from `:pipeline:voiceLines`, run isolated as tools/art/voice.py runs it; without --lines the
       list voice.py last wrote, tools/art/voice.py's LINES, is read)
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
LEVEL = "level-37-fall-of-aurelia"
GAP = 1.2
SECTION_GAP = 2.0
# the event lines' likeliest place in a run (the level's radio has them by event, not by time)
EVENT_TIME = {"FIRST_BOUNCE": 42.0, "FIRST_ALLY_LOST": 56.5, "secret": 61.0, "FIRST_PREY_KILL": 119.5,
              "SECONDARY_OBJECTIVE": 143.0, "SECONDARY_FAILED": 143.5, "LEVEL_END": 197.0}


def level37(all_lines):
    """Level 37's briefing pages and its radio lines, in playing order."""
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
    page4 = next(ln for ln in all_lines
                 if ln["source"].startswith("act-6-jovian-storm briefing") and "Vorne, this is Okafor" in ln["text"])
    shutil.copyfile(ROOT / "assets" / page4["path"], DEST / "act-6-intro-page-4-r75-a.ogg")
    print("page 4:", page4["path"], page4["expression"], page4.get("cfg_weight"), "pin", page4.get("pin"))
    brief, radio = level37(all_lines)
    vorne = next(ln for ln in radio if ln["voice"] == "vorne")
    shutil.copyfile(ROOT / "assets" / vorne["path"], DEST / "level-37-vorne-r75-a.ogg")
    print("vorne:", vorne["path"], vorne["expression"], vorne["filter"])
    parts, cues, t = [], [], 0.0
    groups = [("Level 37 briefing", brief), ("Level 37 radio", radio)]
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
    s.write_ogg(DEST / "level-37-listen-r75-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r75.py listen-through (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r75.py"})
    (DEST / "level-37-listen-r75-a.txt").write_text(
        "Level 37 listen-through (round 75): each line's start in level-37-listen-r75-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"listen-through: {len(cues)} lines, {t:.1f} s")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    post(json.loads(path.read_text()))


if __name__ == "__main__":
    main(sys.argv[1:])
