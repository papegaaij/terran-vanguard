#!/usr/bin/env python3
"""Concept round 74 (M10 "Act 6 - Jovian Storm" part B, step m10-b5, 2026-10-31): the Act 6 voices for review;
pending the user's review. Built on tools/art/voice.py (its worker, chunking, retries and post filters).

Outputs (design/audio/voice/concept/):
  act-6-listen-r74-a.ogg: a listen-through of every Act 6 line part B voiced, as the game plays them (the production
      files in assets/voice/, unchanged): the act intro's four pages (Okafor's 1, 2 and 4 dry; Vorne's page 3 through
      the enemy filter), Level 36's two briefing pages, then its radio lines in the order of their time (the event lines
      at their likeliest place: the secondary's outcome (met, then failed) after the last harvester pair, the first strike
      kill after t=138, the pod's secret after t=146, the level's end last); 1.2 s apart (2 s after the intro and after the briefing). The cue list with
      each line's start in the file goes to act-6-listen-r74-a.txt beside it (the round's row quotes it).
  act-6-intro-page-4-r74-a.ogg: the intro's page 4 as built (the act data's `fierce` row, its pinned take): a copy of
      the production file.
  act-6-intro-page-4-r74-b.ogg: the same page at the voice README's reading, the **grim row, slower** (exaggeration
      0.45, cfg_weight 0.4, temperature 0.7: round 72's GRIM_SLOW, as Aurelia Traffic's sign-off), Okafor's clip,
      chunked and retried by voice.py's rules from seed 101 (`SEED`), dry (no filter) at -16 LUFS as the other pages.

Setup: tools/art/voice.py's (Chatterbox in ~/.cache/tv-tts/venv-chatterbox on the GPU; the line list from
`:pipeline:voiceLines`, run isolated as voice.py runs it). Raw takes go to ~/.cache/tv-tts/raw/r74/.
Rerun: python3 tools/concept/audio/voice_r74.py            (the line list, the page-4 render, the listen-through)
       python3 tools/concept/audio/voice_r74.py --post     (post-process and join only)
Seeds are fixed, but GPU sampling is not bit-exact across drivers.
"""
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
import voice as V  # noqa: E402

DEST = ROOT / "design" / "audio" / "voice" / "concept"
RAW = V.BASE / "raw" / "r74"
GRIM_SLOW = {"exaggeration": 0.45, "cfg_weight": 0.4, "temperature": 0.7}
SEED = 101
PAGE4_KEY = "r74-intro-page-4-grim"
GAP = 1.2
SECTION_GAP = 2.0
# the event lines' likeliest place in a run (the level's radio has them by event, not by time)
EVENT_TIME = {"FIRST_STRIKE_KILL": 138.5, "secret": 147.0, "SECONDARY_OBJECTIVE": 112.0, "SECONDARY_FAILED": 112.5,
              "LEVEL_END": 192.0}


def lines():
    V.GRADLE.mkdir(parents=True, exist_ok=True)
    V.INIT.write_text("allprojects {\n    layout.buildDirectory = file(\"" + (V.GRADLE / "build").as_posix()
                      + "/${project.path.replace(':', '_')}\")\n}\n")
    subprocess.run([str(ROOT / "gradlew"), "-q", "-Dorg.gradle.daemon.idletimeout=600000",
                    "--project-cache-dir", "build/voice-cache", "--init-script", str(V.INIT), ":pipeline:voiceLines"],
                   cwd=ROOT, check=True)
    return json.loads(V.LINES.read_text())


def act6(all_lines):
    """The intro pages, Level 36's briefing pages and its radio lines, in playing order."""
    def src(line):
        return line["source"]
    intro = sorted((ln for ln in all_lines if src(ln).startswith("act-6-jovian-storm briefing")), key=src)
    brief = sorted((ln for ln in all_lines if "level-36" in src(ln) and "briefing" in src(ln)), key=src)
    radio = [ln for ln in all_lines if "level-36" in src(ln) and "briefing" not in src(ln)]

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
    return intro, brief, radio


def page4(intro):
    line = next(ln for ln in intro if "Vorne, this is Okafor" in ln["text"])
    return line


def render(line):
    job = dict(line, key=PAGE4_KEY, pin=SEED, **GRIM_SLOW)
    RAW.mkdir(parents=True, exist_ok=True)
    jobs = RAW / "jobs.json"
    jobs.write_text(json.dumps([job], indent=1))
    env = dict(os.environ, HF_HOME=str(V.BASE / "hf"), TOKENIZERS_PARALLELISM="false",
               TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
    py = V.BASE / "venv-chatterbox" / "bin" / "python"
    subprocess.run([str(py), __file__, "--worker", str(jobs)], env=env, check=True)


def post(intro, brief, radio):
    import numpy as np
    r18 = V._filters()
    s = r18._synth()
    # page 4: a (as built, a copy) and b (grim, slower; dry as the page's production post)
    line = page4(intro)
    shutil.copyfile(ROOT / "assets" / line["path"], DEST / "act-6-intro-page-4-r74-a.ogg")
    x = s.decode(RAW / f"{PAGE4_KEY}.wav")[0]
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = np.zeros(int(0.1 * s.SR))
    y = s.master(np.concatenate([pad, x, pad])[None, :], target_lufs=-16.0, ceiling_db=-1.5)
    log = json.loads((RAW / "worker-log.json").read_text())[PAGE4_KEY]
    seeds = ",".join(str(seed) for seed in log.get("seeds", []))
    s.write_ogg(DEST / "act-6-intro-page-4-r74-b.ogg", y, quality=4,
                tags={"COMMENT": f"tools/concept/audio/voice_r74.py okafor grim-slow seeds {seeds}",
                      "SOURCE": "tools/concept/audio/voice_r74.py"})
    print("page 4 b:", log["seconds"], "s, seeds", seeds, "chunks resolved",
          all(c["resolved"] for c in log["chunks"]))
    # the listen-through
    parts, cues, t = [], [], 0.0
    groups = [("Act 6 intro", intro), ("Level 36 briefing", brief), ("Level 36 radio", radio)]
    for g, (name, group) in enumerate(groups):
        for i, ln in enumerate(group):
            x = s.decode(ROOT / "assets" / ln["path"])[0]
            cues.append(f"{t:6.1f} s  {name}: {ln['voice']} ({ln['source']}): {ln['text']}")
            parts.append(x)
            t += len(x) / s.SR
            last = i == len(group) - 1 and g < len(groups) - 1
            gap = SECTION_GAP if last else GAP
            parts.append(np.zeros(int(gap * s.SR)))
            t += gap
    y = np.concatenate(parts[:-1])[None, :]
    s.write_ogg(DEST / "act-6-listen-r74-a.ogg", y, quality=4,
                tags={"COMMENT": "tools/concept/audio/voice_r74.py listen-through (assets/voice files joined)",
                      "SOURCE": "tools/concept/audio/voice_r74.py"})
    (DEST / "act-6-listen-r74-a.txt").write_text(
        "Act 6 listen-through (round 74): each line's start in act-6-listen-r74-a.ogg\n" + "\n".join(cues) + "\n")
    print(f"listen-through: {len(cues)} lines, {t:.1f} s")


def main(args):
    if args[:1] == ["--worker"]:
        V.RAW = RAW
        RAW.mkdir(parents=True, exist_ok=True)
        V.worker(args[1])
        return
    intro, brief, radio = act6(lines())
    if "--post" not in args:
        render(page4(intro))
    post(intro, brief, radio)


if __name__ == "__main__":
    main(sys.argv[1:])
