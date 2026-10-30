#!/usr/bin/env python3
"""An act's voice listen-through: every voiced line of one act in play order (round 45 for Act 3, M7 part I, I.5;
generalised for Act 5 in M9 part I, step VOX-I).

Outputs (into --out, default design/concept-rounds/round-45/):
  listen-through.html  one page, every voiced line the act plays in play order: the act intro, its levels (each
                       level's briefing pages, then its radio cues by time with the event cues where the data lists
                       them, the escort-only and `requires_not` lines, both {side} takes, every {ally}, {group} and
                       {section} name, the secondary and the level end, then its secrets), Rook's barks by trigger
                       (with the reversed sections' "up top" variants), the other calls heard in the act (the
                       specials, the low-armour line, the game over) and the act outro; per line the speaker, the
                       text as shown and as spoken (the respellings), its expression and filter when not the plain
                       ones (Vorne's SMUG/RAGE, the enemy channel), an inline player and a link to the file in
                       assets/voice/, and its marks: pinned (a seed pin in the speaker table, the pin's note as the
                       tooltip), listened (an earlier round's row with the **listen** action links the file, or a
                       one-file row flagged LISTEN or "(listen for"), auditioned (a round's casting row played the
                       line's take, AUDITIONS below) and the round whose README first linked the file. A checkbox
                       hides the pinned and listened lines. Relative links only, computed from --out.
  listen-through.md    the index: lines, pinned, listened and missing files per section; with --check also the
                       "Listen first" block and every line's row with its check (round 59's layout).

--check adds the per-line check: the file's duration, peak and the level of its last 30 ms (ffmpeg's decode), and a
faster-whisper base.en transcript (the renderer's model, in the Dia venv ~/.cache/tv-tts/venv-dia, models in
~/.cache/tv-tts/hf) compared with the text as spoken. Transcripts are cached by file in build/listen-through-whisper.json.
"Listen first" lists a line whose transcript misses or changes words the line needs (names the small model does not
know are ignored), a slow line (over 0.7 s a word), a clipping peak, a loud tail (not a cut-off line), and every line of
the speakers named with --name (the new voices). A hint, not a verdict: nobody has listened.

Sources: design/audio/voice/data.yaml (speakers, settings, respellings, pins), design/campaign/<act>/data.yaml and its
levels' data.yaml, design/player/wingmen/data.yaml (the barks), design/player/specials/data.yaml,
design/player/armor/data.yaml, design/systems/retry/data.yaml, design/allies/data.yaml (a hull escort's sections), and
the earlier rounds' README.md (the marks). A line's file is found by its key, computed as
vanguard.content.voice.VoiceLines does (SHA-256 over the voice's settings, the text as spoken, the expression, the
shout flag and the filter); a key without a file is reported (stdout and the page) as missing. --lines FILE checks the
keys against :pipeline:voiceLines' lines.json (every key of the act's sources must be on the page). A stage direction
("[the Choir sings]") plays the speaker's stage sound.

Rerun (Act 3, round 45): python3 tools/concept/listen_through.py
      (Act 5, round 69): python3 tools/concept/listen_through.py --act act-5-the-belt --round 69 \\
                             --out design/concept-rounds/round-69 --check --name Bellwether --name Harker
"""
import argparse
import difflib
import hashlib
import html
import json
import os
import re
import subprocess
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
DESIGN = ROOT / "design"
VERSION = "voice-1"   # VoiceLines.VERSION
TTS = Path.home() / ".cache" / "tv-tts"
WHISPER_PY = TTS / "venv-dia" / "bin" / "python"   # faster-whisper, as tools/art/voice.py
WHISPER_CACHE = ROOT / "build" / "listen-through-whisper.json"

sys.path.insert(0, str(Path(__file__).resolve().parent))
from board import CSS  # noqa: E402  (the review boards' look)

# set by main() from the arguments
ACT = DESIGN / "campaign" / "act-3-red-dust"
OUT = DESIGN / "concept-rounds" / "round-45"
ROUND = 45
ITEM = "I.5"


def load(path):
    return yaml.safe_load(path.read_text(encoding="utf-8"))


VOICES = load(DESIGN / "audio" / "voice" / "data.yaml")


# ---------------------------------------------------------------- VoiceLines in Python

def spoken(text):
    """VoiceLines.spoken: stage directions left out, '*' removed, a dash read as a comma."""
    s = re.sub(r"\[[^\]]*\]", " ", text).replace("*", "")
    s = re.sub(r"\s*[—–]\s*", ", ", s)
    s = re.sub(r"\s+", " ", s).strip()
    return "" if re.fullmatch(r"[\s!-/:-@\[-`{-~]*", s) else s


def respelled(text):
    """VoiceData.respelled: whole words, case-sensitive, the longest first."""
    table = VOICES.get("respell") or {}
    if not table:
        return text
    words = sorted(table, key=lambda w: (-len(w), w))
    pattern = r"(?<![\w'-])(" + "|".join(re.escape(w) for w in words) + r")(?![\w-])"
    return re.sub(pattern, lambda m: table[m.group(1)], text)


def voice_of(name):
    for slug in sorted(VOICES["speakers"]):
        sp = VOICES["speakers"][slug]
        if name in sp.get("names", []) and sp.get("ref"):
            return slug
    return None


def settings(voice, expression, shout):
    sp = VOICES["speakers"][voice]
    if sp.get("fixed"):
        f = sp["fixed"]
        return f["exaggeration"], f["cfg_weight"], f["temperature"]
    row = VOICES["expressions"]["shout" if shout else expression]
    shift = sp.get("shift") or {}
    return tuple(row[k] + shift.get(k, 0.0) for k in ("exaggeration", "cfg_weight", "temperature"))


def post_filter(voice, filt):
    """VoiceLines.line's filter: dry and enemy stay; otherwise the speaker table's filter (enemy or pa) wins."""
    if filt in ("dry", "enemy"):
        return filt
    table = VOICES["speakers"][voice].get("filter")
    if table:
        return "enemy" if table == "enemy" else "pa"
    return filt


def key_of(voice, text, expression, shout, filt):
    """VoiceLines.line's key and text as spoken."""
    said = respelled(spoken(text))
    sp = VOICES["speakers"][voice]
    post = post_filter(voice, filt)
    ex, cfg, temp = settings(voice, expression, shout)
    parts = [VERSION, voice, sp["ref"], f"{ex:.3f}", f"{cfg:.3f}", f"{temp:.3f}", sp.get("layering") or "-",
             said, expression, "true" if shout else "false", post]
    return hashlib.sha256("\n".join(parts).encode("utf-8")).hexdigest()[:12], said


PINS = {k: (voice, note) for voice, sp in VOICES["speakers"].items() for k, note in (sp.get("pins") or {}).items()}


def pin_notes():
    """The pins' comments in the speaker table (yaml drops them): key -> note."""
    notes = {}
    for line in (DESIGN / "audio" / "voice" / "data.yaml").read_text(encoding="utf-8").splitlines():
        m = re.match(r"\s+([0-9a-f]{12}):\s*\d+\s*#\s*(.*)", line)
        if m:
            notes[m.group(1)] = m.group(2)
    return notes


PIN_NOTES = pin_notes()


def round_marks():
    """From the earlier rounds' READMEs: key -> first round linking its file; key -> rounds with a listen row."""
    first, listened = {}, {}
    for readme in sorted(DESIGN.glob("concept-rounds/round-*/README.md")):
        n = int(readme.parent.name.split("-")[1])
        if n >= ROUND:
            continue
        for row in readme.read_text(encoding="utf-8").splitlines():
            keys = re.findall(r"assets/voice/[a-z0-9-]+/([0-9a-f]{12})\.ogg", row)
            for k in keys:
                first.setdefault(k, n)
                # a listen row: its action is **listen** (all its files), or a one-file row flagged LISTEN or
                # "(listen for …)" (a Listening table); a summary row's LISTEN names one of many files, so not it
                flagged = "**listen**" in row or (len(keys) == 1 and re.search(r"\bLISTEN\b|\(listen\b", row))
                if flagged and n not in listened.get(k, []):
                    listened.setdefault(k, []).append(n)
    return first, listened


FIRST, LISTENED = {}, {}

# Casting rows from round 60 on played the game's own takes as auditions (the rounds link the audition compilations,
# not the line files): (round, row, speaker, section id, when) -> the take heard. The text may have changed since
# (a retimed or trimmed line is a new take), so the mark is a hint.
AUDITIONS = [
    (62, "V.1", "Ceres Hub Control", "l29", "t=1"), (62, "V.1", "Ceres Hub Control", "l29", "t=101"),
    (62, "V.2", "Harker", "l29", "t=70.5"), (62, "V.2", "Harker", "l29", "t=133"),
    (62, "V.3", "Vorne", "act-intro", None),
    (64, "V.1", "Bellwether", "l31", "t=1"), (64, "V.1", "Bellwether", "l31", "t=86"),
    (64, "V.1", "Bellwether", "l31", "t=93"),
]


# ---------------------------------------------------------------- the lines in play order

class Line:
    def __init__(self, when, speaker, text, expression="neutral", shout=False, filt="radio", note=""):
        self.when, self.speaker, self.text, self.note = when, speaker, text, note
        self.expression, self.shout = expression, shout
        self.voice = voice_of(speaker)
        self.key = self.path = self.said = self.filter = None
        self.stage = False
        self.auditioned = []
        self.check = None
        if self.voice is None:
            return
        if not spoken(text):
            stage = VOICES["speakers"][self.voice].get("stage")
            if "[" in text and stage:
                self.stage, self.path = True, f"voice/{self.voice}/{stage}"
            return
        self.key, self.said = key_of(self.voice, text, expression, shout, filt)
        self.filter = post_filter(self.voice, filt)
        self.path = f"voice/{self.voice}/{self.key}.ogg"

    @property
    def exists(self):
        return self.path is not None and (ROOT / "assets" / self.path).exists()

    @property
    def pinned(self):
        return self.key in PINS

    @property
    def listened(self):
        return LISTENED.get(self.key, [])

    @property
    def delivery(self):
        """The expression and filter when not the plain ones (a shout, Vorne's SMUG/RAGE, the enemy channel)."""
        out = []
        if self.shout:
            out.append("SHOUT")
        elif self.expression != "neutral":
            out.append(self.expression.upper())
        if self.filter in ("enemy", "distorted", "pa"):
            out.append(f"{self.filter} filter")
        return ", ".join(out)


def cue_lines(level, cue, sections_):
    """One radio cue's lines: the line and its easy/hard variants, both {side} takes, every {ally}, {group} or
    {section}."""
    expression = cue.get("expression", "neutral")
    shout = bool(cue.get("shout", False))
    filt = "distorted" if cue.get("distorted") else "radio"
    if "t" in cue:
        when = f"t={float(cue['t']):g}"
    else:
        when = cue["event"] + (f" ({cue['phase']})" if cue.get("phase") else "")
    notes = []
    if cue.get("requires") == "escort":
        notes.append("Rook flies")
    elif cue.get("requires"):
        notes.append(f"requires {cue['requires']}")
    if cue.get("requires_not"):
        notes.append("Rook does not fly" if cue["requires_not"] == "escort" else f"requires not {cue['requires_not']}")
    trigger = (cue.get("notes") or {}).get("trigger")
    if trigger and "t" not in cue:
        notes.append(trigger.replace("`", ""))
    texts = [("", cue["line"])]
    for diff in ("easy", "hard"):
        if isinstance(cue.get(diff), dict) and cue[diff].get("line"):
            texts.append((diff, cue[diff]["line"]))
    allies = [u["name"] for u in (level.get("allies") or {}).get("units", [])]
    convoy = [u["name"] for u in (level.get("convoy") or {}).get("units", []) if u.get("lane") is not None]
    groups = groups_of(level)
    out = []
    for diff, text in texts:
        if "{side}" in text:
            sided = [(f"{s} side", text.replace("{side}", s)) for s in ("left", "right")]
        elif "{section}" in text and sections_:
            sided = [(f"the {s}", text.replace("{section}", s)) for s in sections_]
        else:
            sided = [("", text)]
        for side, t in sided:
            tag = ", ".join(x for x in (diff, side) if x)
            names = []
            if "{group}" in t:
                names = [(g, t.replace("{group}", g)) for g in groups] or [("", t)]
            elif "{ally}" in t and (convoy or allies):
                names = [(a, t.replace("{ally}", a)) for a in (convoy or allies)]
            else:
                names = [("", t)]
            for name, final in names:
                note = "; ".join(x for x in [tag, *notes] if x)
                out.append(Line(when, cue["speaker"], final, expression, shout, filt, note))
    return out


def groups_of(level):
    obj = level.get("objectives") or {}
    if obj.get("targets"):
        return list(obj["targets"])
    sec = obj.get("secondary")
    if isinstance(sec, dict) and sec.get("groups"):
        return list(sec["groups"])
    return []


def hull_sections(level):
    """VoiceLines.sections: the hull escort's section names in hull order (design/allies), empty without one."""
    escort = (level.get("objectives") or {}).get("escort")
    if not isinstance(escort, dict) or not escort.get("hull"):
        return []
    ally = (load(DESIGN / "allies" / "data.yaml") or {}).get(escort.get("ally")) or {}
    return [s["name"] for s in ally.get("sections") or []]


def pages(pages_, label):
    return [Line(f"{label} page {i}", p["speaker"], p["line"], p.get("expression", "neutral"), False,
                 "enemy" if p.get("channel") == "enemy" else "dry")
            for i, p in enumerate(pages_, 1)]


def level_lines(level):
    lines = pages((level.get("briefing") or {}).get("pages", []), "briefing")
    timed, last = [], 0.0
    for i, cue in enumerate(level.get("radio") or []):
        last = float(cue["t"]) if "t" in cue else last
        timed.append((last, i, cue))
    sections_ = hull_sections(level)
    for _, _, cue in sorted(timed, key=lambda x: (x[0], x[1])):
        lines += cue_lines(level, cue, sections_)
    for secret in level.get("secrets") or []:
        r = secret["radio"]
        note = "Rook flies" if r.get("requires") == "escort" else ""
        lines.append(Line(f"secret: {secret['name']}", r["speaker"], r["line"], r.get("expression", "neutral"),
                          False, "distorted" if r.get("distorted") else "radio", note))
    return lines


SMALL = {"of", "from", "at", "the", "to", "and", "in"}


def sections():
    act = load(ACT / "data.yaml")
    slug_act = ACT.name
    n_act = int(slug_act.split("-")[1])
    out = [("act-intro", f"Act {n_act} intro", slug_act, pages(act["briefing"], "briefing"))]
    for d in sorted(ACT.glob("level-*/data.yaml")):
        slug = d.parent.name
        n = int(slug.split("-")[1])
        words = slug.split("-")[2:]
        title = " ".join(w if (w in SMALL and i) else w.capitalize() for i, w in enumerate(words))
        out.append((f"l{n}", f"Level {n} {title}", f"{slug_act}/{slug}", level_lines(load(d))))
    wing = load(DESIGN / "player" / "wingmen" / "data.yaml")["barks"]
    barks = []
    for b in wing["triggers"]:
        note = re.sub(r"\{(\w+)\}", lambda m: f"{b.get(m.group(1), m.group(0)):g}"
                      if isinstance(b.get(m.group(1)), (int, float)) else m.group(0),
                      (b.get("notes") or {}).get("trigger", "").replace("`", ""))
        for text in b.get("lines", []):
            barks.append(Line(b["trigger"], wing["speaker"], text, b.get("expression", "neutral"),
                              bool(b.get("shout")), "radio", note))
        for text in b.get("top_lines", []) or []:
            barks.append(Line(b["trigger"] + " (top)", wing["speaker"], text, b.get("expression", "neutral"),
                              bool(b.get("shout")), "radio", "a reversed section: behind is the top"))
    out.append(("barks", "Rook's barks", "player/wingmen", barks))
    specials = load(DESIGN / "player" / "specials" / "data.yaml")
    other = []
    for name, entry in specials.items():
        if isinstance(entry, dict) and isinstance(entry.get("radio"), dict):
            r = entry["radio"]
            other.append(Line(f"special: {name}", r["speaker"], r["line"], r.get("expression", "neutral")))
    for when, path, field in (("low armour", DESIGN / "player" / "armor" / "data.yaml", "radio"),
                              ("game over", DESIGN / "systems" / "retry" / "data.yaml", "game_over")):
        r = load(path)[field]
        other.append(Line(when, r["speaker"], r["line"], r.get("expression", "neutral"), False,
                          "distorted" if r.get("distorted") else "radio"))
    out.append(("other", f"Other calls heard in Act {n_act}", "player/specials", other))
    out.append(("act-outro", f"Act {n_act} outro", slug_act, pages(act["outro"]["pages"], "outro")))
    for rnd, row, speaker, sid, when in AUDITIONS:
        if rnd >= ROUND:
            continue
        for s, _, _, ls in out:
            if s != sid:
                continue
            for x in ls:
                if x.speaker == speaker and (when is None or x.when == when) and x.key:
                    x.auditioned.append((rnd, row))
    return out


def check_keys(secs, lines_json):
    """The page's keys against :pipeline:voiceLines: every key it lists for the act's sources is on the page, and
    every key on the page is one it lists (a line shared with an earlier act is listed once, under its first source)."""
    entries = json.loads(Path(lines_json).read_text(encoding="utf-8"))
    every = {e["key"] for e in entries}
    want = {e["key"]: e["source"] for e in entries if e["source"].startswith(ACT.name)}
    have = {x.key for _, _, _, ls in secs for x in ls if x.key}
    missing = {k: s for k, s in want.items() if k not in have}
    unknown = sorted(have - every)
    for k, s in sorted(missing.items(), key=lambda kv: kv[1]):
        print(f"voiceLines key not on the page: {k} ({s})")
    for k in unknown:
        print(f"page key voiceLines does not list: {k}")
    print(f"voiceLines: {len(want)} keys under {ACT.name}, {len(missing)} not on the page; "
          f"{len(have)} page keys, {len(unknown)} unknown to voiceLines")
    return len(missing) + len(unknown)


# ---------------------------------------------------------------- the check (--check)

NUMBERS = ("zero one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen "
           "seventeen eighteen nineteen").split()
TENS = "_ _ twenty thirty forty fifty sixty seventy eighty ninety".split()

# names and words base.en does not know, or spells its own way: a difference on them is not a mishearing
NAMES = {"vrell", "aegis", "vorne", "ceres", "harker", "bellwether", "dacosta", "okafor", "varga", "rook", "helix",
         "callisto", "lancer", "stormhawk", "stormhawks", "ascendancy", "cdf", "europa", "conamara", "thera", "brandt",
         "sovereign", "skitter", "skitters", "talon", "talons", "hornet", "hornets", "airees", "mk", "silas",
         "jovian", "tesla", "rtb", "bulwark", "ember", "vigil", "marta", "wren", "currier", "sable", "leech",
         "leeches", "serpent", "serpents", "sam", "sams", "okay", "ok", "vesta", "kleopatra", "ha", "hah", "nav"}
# British spellings base.en writes the American way
SPELLING = {"grey": "gray", "colour": "color", "colours": "colors", "armour": "armor", "metres": "meters",
            "kilometres": "kilometers", "harbour": "harbor", "centre": "center", "defence": "defense",
            # homophones the transcript cannot tell apart
            "youre": "your", "doc": "dock"}


def number_words(n):
    if n < 20:
        return NUMBERS[n]
    if n < 100:
        return TENS[n // 10] + ("" if n % 10 == 0 else " " + NUMBERS[n % 10])
    if n < 1000:
        rest = n % 100
        return NUMBERS[n // 100] + " hundred" + ("" if rest == 0 else " " + number_words(rest))
    return str(n)


def words_of(text):
    """Lower case, no apostrophes (a possessive reads as the plural), numerals as words, American spellings."""
    t = text.lower().replace("’", "'").replace("…", " ").replace("%", " percent").replace("'", "")
    t = re.sub(r"(\d+)(st|nd|rd|th)\b", r"\1", t)
    t = re.sub(r"(\d+)(s?)\b", lambda m: " " + number_words(int(m.group(1))) + m.group(2) + " ", t)
    t = re.sub(r"[-/]", " ", t)
    t = re.sub(r"[^a-z ]", " ", t)
    return [SPELLING.get(w, w) for w in t.split()]


def is_name(word):
    """A name or numeral the small model does not know (a possessive or plural of one too)."""
    stem = word[:-1] if word.endswith("s") else word
    return word in NAMES or stem in NAMES or word in NUMBERS or word in TENS or word == "hundred"


def compare(said, heard):
    """The words of the line the transcript misses or changes (names ignored), as 'said -> heard' pairs."""
    a, b = words_of(said), words_of(heard)
    out = []
    for op, i1, i2, j1, j2 in difflib.SequenceMatcher(None, a, b, autojunk=False).get_opcodes():
        if op == "equal" or op == "insert":
            continue
        miss = [w for w in a[i1:i2] if not is_name(w)]
        if not miss:
            continue
        joined_a, joined_b = "".join(a[i1:i2]), "".join(b[j1:j2])
        if joined_a == joined_b:   # "lifeboat" vs "life boat"
            continue
        out.append((" ".join(a[i1:i2]), " ".join(b[j1:j2])))
    return out, len(a)


def decode(path):
    """The file as mono float samples (ffmpeg), and its rate."""
    import numpy as np
    rate = 48000
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-ac", "1", "-ar", str(rate), "-f", "f32le", "-"],
                         check=True, capture_output=True).stdout
    return np.frombuffer(raw, dtype=np.float32), rate


def levels(path):
    import numpy as np
    x, rate = decode(path)
    tail = x[-int(0.03 * rate):]
    rms = float(np.sqrt(np.mean(tail.astype(np.float64) ** 2))) if len(tail) else 0.0
    return {"seconds": round(len(x) / rate, 2), "peak": round(float(np.max(np.abs(x))) if len(x) else 0.0, 3),
            "tail_db": round(20 * np.log10(max(rms, 1e-6)), 1)}


WHISPER_JOB = r"""
import json, sys
from faster_whisper import WhisperModel
asr = WhisperModel("base.en", device="cpu", compute_type="int8")
out = {}
for path in json.load(open(sys.argv[1])):
    segs, _ = asr.transcribe(path, language="en", beam_size=5)
    out[path] = " ".join(s.text.strip() for s in segs).strip()
    print("heard", path.rsplit("/", 1)[-1], ":", out[path], flush=True)
json.dump(out, open(sys.argv[2], "w"), indent=1)
"""


def transcripts(paths):
    """faster-whisper base.en over the files, cached by path, size and mtime."""
    cache = json.loads(WHISPER_CACHE.read_text(encoding="utf-8")) if WHISPER_CACHE.exists() else {}

    def tag(p):
        st = p.stat()
        return f"{p.relative_to(ROOT)}:{st.st_size}:{int(st.st_mtime)}"

    todo = sorted({str(p) for p in paths if tag(p) not in cache})
    if todo:
        WHISPER_CACHE.parent.mkdir(parents=True, exist_ok=True)
        job = WHISPER_CACHE.with_suffix(".todo.json")
        res = WHISPER_CACHE.with_suffix(".out.json")
        job.write_text(json.dumps(todo), encoding="utf-8")
        env = dict(os.environ, HF_HOME=str(TTS / "hf"), HF_HUB_OFFLINE="1")
        subprocess.run([str(WHISPER_PY), "-c", WHISPER_JOB, str(job), str(res)], check=True, env=env)
        for path, text in json.loads(res.read_text(encoding="utf-8")).items():
            cache[tag(Path(path))] = text
        WHISPER_CACHE.write_text(json.dumps(cache, indent=1, sort_keys=True), encoding="utf-8")
        job.unlink()
        res.unlink()
    return {p: cache[tag(p)] for p in paths}


SLOW = 0.7          # s a word
CLIP = 0.98         # sample peak
LOUD_TAIL = -35.0   # dBFS over the last 30 ms
LOUD_TAIL_BED = -25.0   # the same for the enemy channel and a distorted line, which end in their static bed


def run_check(secs, named):
    files = {ROOT / "assets" / x.path for _, _, _, ls in secs for x in ls if x.exists and not x.stage}
    heard = transcripts(sorted(files))
    for _, _, _, ls in secs:
        for x in ls:
            if not x.exists:
                continue
            f = ROOT / "assets" / x.path
            c = levels(f)
            reasons = []
            if x.stage:
                c.update(heard="", diff=[], words=0)
            else:
                c["heard"] = heard[f]
                diff, n = compare(x.said, c["heard"])
                c["diff"], c["words"] = diff, n
                missed = sum(len(s.split()) for s, _ in diff)
                if diff and (missed >= 2 or n <= 6):
                    reasons.append("whisper heard " + "; ".join(f'"{h}" for "{s}"' if h else f'no "{s}"'
                                                                 for s, h in diff[:4]))
                if n and c["seconds"] / n > SLOW:
                    reasons.append(f"slow ({c['seconds']:.1f} s for {n} words)")
            if c["peak"] >= CLIP:
                reasons.append(f"peak {c['peak']:.2f}")
            cut = re.search(r"[—–]\s*$", x.text) and x.filter != "dry"
            bed = x.filter in ("enemy", "distorted")   # a steady static bed (or crackle) by design
            if c["tail_db"] > (LOUD_TAIL_BED if bed else LOUD_TAIL) and not cut:
                reasons.append(f"loud tail ({c['tail_db']:.0f} dBFS)")
            if x.speaker in named:
                reasons.append(f"{x.speaker}'s voice ({VOICES['speakers'][x.voice]['ref']}), new in this act")
            c["reasons"] = reasons
            x.check = c


# ---------------------------------------------------------------- output

EXTRA_CSS = """
table.lt { border-collapse:collapse; width:100%; font-size:13px; }
table.lt th, table.lt td { border-bottom:1px solid var(--edge); padding:5px 6px; text-align:left; vertical-align:top; }
table.lt th { color:var(--dim); font-weight:normal; }
table.lt td.n { color:var(--dim); width:2.5em; }
table.lt td.when { white-space:nowrap; color:var(--dim); }
table.lt td.who { white-space:nowrap; font-weight:bold; }
table.lt .said { color:var(--dim); font-size:12px; margin-top:2px; }
table.lt .note { color:var(--dim); font-size:12px; font-style:italic; }
table.lt audio { width:230px; max-width:100%; height:30px; }
table.lt td.file a { color:var(--accent); font-family:monospace; font-size:12px; }
.mark { display:inline-block; font-size:11px; font-weight:bold; text-transform:uppercase; border-radius:3px;
        padding:0 5px; margin:1px 2px 1px 0; border:1px solid currentColor; white-space:nowrap; }
.mark.pin { color:var(--warm); } .mark.lis { color:#66bb6a; } .mark.rev { color:var(--dim); font-weight:normal; }
.mark.miss { color:#ef5350; } .mark.stage { color:var(--accent); }
.wrap { overflow-x:auto; }
label.hide { font-size:13px; color:var(--dim); cursor:pointer; }
body.skip tr.skip { display:none; }
@media (max-width:700px) { table.lt td.file, table.lt th.file { display:none; } table.lt audio { width:160px; } }
"""

CHECK_CSS = """
table.lt td.chk { font-size:12px; color:var(--dim); min-width:12em; }
table.lt td.chk b { color:var(--warm); font-weight:normal; }
.mark.aud { color:#9575cd; } .mark.del { color:var(--accent); font-weight:normal; }
"""


def rel(target):
    return Path(os.path.relpath(target, OUT)).as_posix()


def marks(line):
    out = []
    if line.stage:
        out.append('<span class="mark stage">stage sound</span>')
    elif line.path is None:
        out.append('<span class="mark miss">no voice</span>')
    elif not line.exists:
        out.append('<span class="mark miss">missing file</span>')
    if line.pinned:
        note = PIN_NOTES.get(line.key, "")
        out.append(f'<span class="mark pin" title="{html.escape(note)}">pinned</span>')
    for n in line.listened:
        out.append(f'<span class="mark lis">listened r{n:02d}</span>')
    for n, row in line.auditioned:
        out.append(f'<span class="mark aud" title="round {n} {row}: the casting audition played this cue\'s take; '
                   f'the text may have changed since">auditioned r{n:02d}</span>')
    if line.key in FIRST:
        out.append(f'<span class="mark rev">first in r{FIRST[line.key]:02d}</span>')
    return " ".join(out)


def check_cell(x):
    c = x.check
    if c is None:
        return ""
    head = f"{c['seconds']:.1f} s, peak {c['peak']:.2f}, tail {c['tail_db']:.0f} dB"
    body = (f"<br>heard: {html.escape(c['heard'])}" if c["heard"] else "")
    flag = (f"<br><b>listen: {html.escape('; '.join(c['reasons']))}</b>" if c["reasons"] else "")
    return head + body + flag


def write_html(secs, checked):
    total = sum(len(ls) for _, _, _, ls in secs)
    pinned = sum(1 for _, _, _, ls in secs for x in ls if x.pinned)
    heard = sum(1 for _, _, _, ls in secs for x in ls if x.listened and not x.pinned)
    nav = " ".join(f'<a href="#{sid}">{html.escape(title)} ({len(ls)})</a>' for sid, title, _, ls in secs)
    n_act = int(ACT.name.split("-")[1])
    assets = rel(ROOT / "assets")
    body = []
    for sid, title, doc, ls in secs:
        rows = []
        for i, x in enumerate(ls, 1):
            said = ""
            if x.said and x.said != x.text:
                said = f'<div class="said">spoken: {html.escape(x.said)}</div>'
            note = ", ".join(n for n in ((x.delivery if checked else ""), x.note) if n)
            note = f'<div class="note">{html.escape(note)}</div>' if note else ""
            href = f"{assets}/{x.path}" if x.path else ""
            player = f'<audio controls preload="none" src="{href}"></audio>' if x.exists else ""
            link = f'<a href="{href}">{html.escape(Path(x.path).name)}</a>' if x.exists else ""
            skip = ' class="skip"' if (x.pinned or x.listened) else ""
            chk = f'<td class="chk">{check_cell(x)}</td>' if checked else ""
            rows.append(f"<tr{skip}><td class=\"n\">{i}</td><td class=\"when\">{html.escape(x.when)}</td>"
                        f"<td class=\"who\">{html.escape(x.speaker)}</td>"
                        f"<td>{html.escape(x.text)}{said}{note}</td><td>{player}</td>"
                        f"<td class=\"file\">{link}</td><td>{marks(x)}</td>{chk}</tr>")
        target = DESIGN / "campaign" / doc if doc.startswith(ACT.name) else DESIGN / doc
        body.append(f'<section id="{sid}"><h2><a href="{rel(target / "README.md")}">{html.escape(title)}</a></h2>')
        body.append('<div class="wrap"><table class="lt"><tr><th>#</th><th>When</th><th>Speaker</th>'
                    '<th>Line (as shown; as spoken when respelled)</th><th>Play</th><th class="file">File</th>'
                    '<th>Marks</th>' + ("<th>Check</th>" if checked else "") + '</tr>'
                    + "".join(rows) + "</table></div></section>")
    flagged = sum(1 for _, _, _, ls in secs for x in ls if x.check and x.check["reasons"])
    check_p = ""
    if checked:
        check_p = (f"\n<p>The check: duration, sample peak and the last 30 ms's level of each file, and what "
                   f"faster-whisper base.en heard against the text as spoken; <b>listen</b> on {flagged} lines "
                   f"(the <a href=\"listen-through.md\">index</a> lists them first): a hint, not a verdict.</p>")
    voice_readme = rel(DESIGN / "audio" / "voice" / "README.md")
    page = f"""<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Act {n_act} Listen-through</title>
<style>{CSS}{EXTRA_CSS}{CHECK_CSS if checked else ""}</style></head>
<body>
<header><h1>Act {n_act} listen-through</h1>
<p>Round {ROUND}, {ITEM}: every voiced Act {n_act} line in play order, {total} lines ({pinned} pinned, {heard} more already
on a round's listen row). Accept or redo each in <a href="README.md">round {ROUND}'s README</a>. Marks: <b>pinned</b>
a seed pin in the <a href="{voice_readme}">speaker table</a> (hover for why), <b>listened</b> a listen
row of that round, <b>first in</b> the round that first linked the file.</p>{check_p}
<p><label class="hide"><input type="checkbox" id="skip"> hide the pinned and listened lines</label></p></header>
<nav><div>{nav}</div></nav>
{''.join(body)}
<footer>Generated by tools/concept/listen_through.py from the voice and level data; do not edit by hand.</footer>
<script>
document.getElementById('skip').addEventListener('change', e => document.body.classList.toggle('skip', e.target.checked));
document.addEventListener('play', e => {{ for (const a of document.querySelectorAll('audio')) if (a !== e.target) a.pause(); }}, true);
</script>
</body></html>
"""
    (OUT / "listen-through.html").write_text(page, encoding="utf-8")


def md_cell(s):
    return s.replace("|", "\\|")


def md_check(x):
    c = x.check
    if c is None:
        return "missing file" if not x.exists else ""
    marks_ = []
    if x.pinned:
        marks_.append("pinned")
    marks_ += [f"listened r{n:02d}" for n in x.listened]
    marks_ += [f"auditioned r{n:02d} {row}" for n, row in x.auditioned]
    verdict = ("listen: " + "; ".join(c["reasons"])) if c["reasons"] else "ok"
    return md_cell(verdict + (f" ({', '.join(marks_)})" if marks_ else ""))


def md_row(x, i, where=None):
    cue = x.when + (f" ({x.note})" if x.note else "")
    if x.delivery:
        cue += f" [{x.delivery}]"
    text = x.text + (f" *(spoken: {x.said})*" if x.said and x.said != x.text else "")
    link = f"[{Path(x.path).parent.name}/{Path(x.path).name}]({rel(ROOT / 'assets')}/{x.path})" if x.path else "—"
    secs = f"{x.check['seconds']:.1f}" if x.check else "—"
    cells = ([where] if where else []) + [str(i), x.speaker, md_cell(cue), md_cell(text), link, secs, md_check(x)]
    return "| " + " | ".join(cells) + " |"


def write_md(secs, checked):
    rows = []
    tot = [0, 0, 0, 0]
    for sid, title, _, ls in secs:
        n = len(ls)
        p = sum(1 for x in ls if x.pinned)
        h = sum(1 for x in ls if x.listened)
        m = sum(1 for x in ls if not x.exists)
        tot = [a + b for a, b in zip(tot, (n, p, h, m))]
        rows.append(f"| [{title}](listen-through.html#{sid}) | {n} | {p} | {h} | {m} |")
    rows.append(f"| **Total** | **{tot[0]}** | **{tot[1]}** | **{tot[2]}** | **{tot[3]}** |")
    n_act = int(ACT.name.split("-")[1])
    md = f"""# Act {n_act} listen-through: index

Generated by `tools/concept/listen_through.py` (do not edit by hand) for round {ROUND} ({ITEM}): every voiced Act {n_act}
line in play order, on [listen-through.html](listen-through.html). *Pinned*: a seed pin in the
[speaker table]({rel(DESIGN / 'audio' / 'voice' / 'data.yaml')}); *listened*: a listen row of an earlier round links the file (a line can
be both); *missing*: no file for the line's key (should be 0; a stage direction plays its stage sound).

| Section | Lines | Pinned | Listened | Missing |
|---|---|---|---|---|
""" + "\n".join(rows) + "\n"
    if checked:
        seconds = sum(x.check["seconds"] for _, _, _, ls in secs for x in ls if x.check)
        flagged = [(title, i, x) for _, title, _, ls in secs for i, x in enumerate(ls, 1)
                   if x.check and x.check["reasons"]]
        peak = max((x.check["peak"] for _, _, _, ls in secs for x in ls if x.check), default=0)
        md += f"""
**The check**: every file's duration, sample peak and the level of its last 30 ms, and a faster-whisper base.en
transcript (the renderer's model) against the text as spoken: {tot[0]} lines, {seconds / 60:.1f} minutes, the loudest
peak {peak:.2f}. **listen** marks a line whose transcript misses or changes words the line needs (names the small model
does not know are ignored), a slow delivery (over {SLOW} s a word), a peak of {CLIP} or more, a tail over
{LOUD_TAIL:.0f} dBFS ({LOUD_TAIL_BED:.0f} on the enemy channel and a distorted line, which end in their static bed;
a cut-off line ends in its static by design), and the new voices named for this round: a hint,
not a verdict (nobody has listened). *auditioned*: a casting row of that round played the cue's take (the text may
have changed since).

## Listen first ({len(flagged)} lines)

| Where | # | Speaker | Cue | Text | File | s | Check |
|---|---|---|---|---|---|---|---|
""" + "\n".join(md_row(x, i, title) for title, i, x in flagged) + "\n"
        for sid, title, _, ls in secs:
            md += f"\n## {title}\n\n| # | Speaker | Cue | Text | File | s | Check |\n|---|---|---|---|---|---|---|\n"
            md += "\n".join(md_row(x, i) for i, x in enumerate(ls, 1)) + "\n"
    (OUT / "listen-through.md").write_text(md, encoding="utf-8")
    return tot


def main():
    global ACT, OUT, ROUND, ITEM, FIRST, LISTENED
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("--act", default="act-3-red-dust", help="the act folder under design/campaign/")
    ap.add_argument("--out", default="design/concept-rounds/round-45", help="output directory (links relative to it)")
    ap.add_argument("--round", type=int, default=45, help="the round the page is for (marks: earlier rounds only)")
    ap.add_argument("--item", default="I.5", help="the round's item id in the header")
    ap.add_argument("--check", action="store_true", help="the per-line check (levels and a Whisper transcript)")
    ap.add_argument("--name", action="append", default=[], help="a speaker whose every line goes on Listen first")
    ap.add_argument("--lines", help=":pipeline:voiceLines' lines.json, to check the keys")
    args = ap.parse_args()
    ACT = DESIGN / "campaign" / args.act
    OUT = (ROOT / args.out).resolve()
    ROUND, ITEM = args.round, args.item
    FIRST, LISTENED = round_marks()
    OUT.mkdir(parents=True, exist_ok=True)
    secs = sections()
    missing = 0
    for _, title, _, ls in secs:
        for x in ls:
            if not x.exists:
                missing += 1
                print(f"missing: {title} {x.when} {x.speaker}: {x.text[:60]} ({x.path})")
    if args.lines:
        check_keys(secs, args.lines)
    if args.check:
        run_check(secs, set(args.name))
    write_html(secs, args.check)
    tot = write_md(secs, args.check)
    print(f"{tot[0]} lines, {tot[1]} pinned, {tot[2]} listened, {tot[3]} missing -> "
          f"{os.path.relpath(OUT / 'listen-through.html', ROOT)}")
    if args.check:
        flagged = [(t, x) for _, t, _, ls in secs for x in ls if x.check and x.check["reasons"]]
        minutes = sum(x.check["seconds"] for _, _, _, ls in secs for x in ls if x.check) / 60
        print(f"checked: {minutes:.1f} minutes, {len(flagged)} on Listen first")


if __name__ == "__main__":
    main()
