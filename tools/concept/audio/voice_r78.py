#!/usr/bin/env python3
"""Concept round 78 (M10 "Act 6 - Jovian Storm" part F, step m10-f5, 2026-11-01): Level 40's voices for review;
pending the user's review. Joins the production files only (no new take): built on tools/art/voice.py's line list and
tools/concept/audio/round18's decoder and writer, as voice_r77.py.

Outputs:
  design/campaign/act-6-jovian-storm/level-40-honour-guard/concept/level-40-listen-r78-a.ogg: a listen-through of every
      Level 40 line step VOX-F voiced, as the game plays them (the production files in assets/voice/, unchanged): its
      two briefing pages (Okafor, Varga), then its radio lines in the order of their time, the event lines at their
      likeliest place in a run (Sentry Two's recorder in section 2; the duel's lines after the sting in the data's order:
      Harker's taunt and Rook's answer, Vesper's and Kestrel's joins, the three first-loss lines, Rook's "for Ceres",
      the three "Just us now" lines, then Rook's and Okafor's lines at the last kill (with and without Rook)); 1.2 s
      apart (2 s after the briefing). The cue list with each line's start goes to level-40-listen-r78-a.txt beside it.
  design/campaign/act-6-jovian-storm/level-39-io-flyby/concept/vorne-t91-grim-r78-a.ogg: Level 39's t=91 line at
      Vorne's grim row (round 77 V.2 b, step VOX-F's take), for the user's ear.

Rerun: python3 tools/concept/audio/voice_r78.py --lines <lines.json>
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

DEST = ROOT / "design" / "campaign" / "act-6-jovian-storm" / "level-40-honour-guard" / "concept"   # beside the level (the voice docs stay VOX steps')
L39 = ROOT / "design" / "campaign" / "act-6-jovian-storm" / "level-39-io-flyby" / "concept"
LEVEL = "level-40-honour-guard"
GAP = 1.2
SECTION_GAP = 2.0
# the event lines' likeliest place in a run (the level's radio has them by event, not by time)
# (a stable sort keeps the data's order among the duel's phase lines)
EVENT_TIME = {"secret": 54.0, "BOSS_ARRIVAL": 103.0, "BOSS_PHASE": 103.5, "BOSS_DESTROYED": 104.0}


def level40(all_lines):
    """Level 40's briefing pages and its radio lines, in playing order."""
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
    radio.sort(key=when)
    return brief, radio


def src_of(line):
    return line["source"]


def post(all_lines):
    import numpy as np
    r18 = V._filters()
    s = r18._synth()
    brief, radio = level40(all_lines)
    parts, cues, t = [], [], 0.0
    groups = [("Level 40 briefing", brief), ("Level 40 radio", radio)]
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
    s.write_ogg(DEST / "level-40-listen-r78-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r78.py listen-through (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r78.py"})
    vorne = next(ln for ln in all_lines if ln["voice"] == "vorne" and "level-39-io-flyby radio t=91" in src_of(ln))
    s.write_ogg(L39 / "vorne-t91-grim-r78-a.ogg", s.decode(ROOT / "assets" / vorne["path"])[0][None, :], quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r78.py: Level 39's t=91 line at Vorne's grim row (assets/voice file)",
                      "SOURCE": "tools/concept/audio/voice_r78.py"})
    (DEST / "level-40-listen-r78-a.txt").write_text(
        "Level 40 listen-through (round 78): each line's start in level-40-listen-r78-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"listen-through: {len(cues)} lines, {t:.1f} s")


def main(args):
    path = Path(args[args.index("--lines") + 1]) if "--lines" in args else V.LINES
    post(json.loads(path.read_text()))


if __name__ == "__main__":
    main(sys.argv[1:])
