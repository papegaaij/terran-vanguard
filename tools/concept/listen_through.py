#!/usr/bin/env python3
"""The Act 3 voice listen-through (round 45, M7 part I, I.5): every voiced Act 3 line in play order.

Outputs (design/concept-rounds/round-45/):
  listen-through.html  one page, every voiced line Act 3 plays in play order: the act intro, Levels 15-21 (each
                       level's briefing pages, then its radio cues by time with the event cues where the data lists
                       them, the escort-only lines, both {side} takes, every {ally} name, the secondary and the
                       level end, then its secrets), Rook's barks by trigger (with the reversed sections' "up top"
                       variants), the other calls heard in Act 3 (the specials, the low-armour line, the game over)
                       and the act outro; per line the speaker, the text as shown and as spoken (the respellings),
                       an inline player and a link to the file in assets/voice/, and its marks: pinned (a seed pin
                       in the speaker table, the pin's note as the tooltip), listened (an earlier round's row with
                       the **listen** action links the file, or a one-file row flagged LISTEN or "(listen for") and the round whose README first linked the file. A checkbox hides
                       the pinned and listened lines. Relative links only; the answers go in round 45's README.
  listen-through.md    the index: lines, pinned, listened and missing files per section.

Sources: design/audio/voice/data.yaml (speakers, settings, respellings, pins), design/campaign/act-3-red-dust/
data.yaml and its levels' data.yaml, design/player/wingmen/data.yaml (the barks), design/player/specials/data.yaml,
design/player/armor/data.yaml, design/systems/retry/data.yaml, and the earlier rounds' README.md (the marks).
A line's file is found by its key, computed as vanguard.content.voice.VoiceLines does (SHA-256 over the voice's
settings, the text as spoken, the expression, the shout flag and the filter); a key without a file is reported
(stdout and the page) as missing. A stage direction ("[the Choir sings]") plays the speaker's stage sound.

Rerun: python3 tools/concept/listen_through.py
"""
import hashlib
import html
import re
import sys
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parents[2]
DESIGN = ROOT / "design"
ACT = DESIGN / "campaign" / "act-3-red-dust"
OUT = DESIGN / "concept-rounds" / "round-45"
ROUND = 45
VERSION = "voice-1"   # VoiceLines.VERSION

sys.path.insert(0, str(Path(__file__).resolve().parent))
from board import CSS  # noqa: E402  (the review boards' look)


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


def key_of(voice, text, expression, shout, filt):
    """VoiceLines.line's key; None when the speaker has no voice or nothing is spoken."""
    said = respelled(spoken(text))
    sp = VOICES["speakers"][voice]
    post = "pa" if filt != "dry" and sp.get("filter") else filt
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


FIRST, LISTENED = round_marks()


# ---------------------------------------------------------------- the lines in play order

class Line:
    def __init__(self, when, speaker, text, expression="neutral", shout=False, filt="radio", note=""):
        self.when, self.speaker, self.text, self.note = when, speaker, text, note
        self.voice = voice_of(speaker)
        self.key = self.path = self.said = None
        self.stage = False
        if self.voice is None:
            return
        if not spoken(text):
            stage = VOICES["speakers"][self.voice].get("stage")
            if "[" in text and stage:
                self.stage, self.path = True, f"voice/{self.voice}/{stage}"
            return
        self.key, self.said = key_of(self.voice, text, expression, shout, filt)
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


def cue_lines(level, cue):
    """One radio cue's lines: the line and its easy/hard variants, both {side} takes, every {ally} or {group}."""
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
        sided = [(s, text.replace("{side}", s)) for s in ("left", "right")] if "{side}" in text else [("", text)]
        for side, t in sided:
            tag = ", ".join(x for x in (diff, f"{side} side" if side else "") if x)
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


def pages(pages, label):
    return [Line(f"{label} page {i}", p["speaker"], p["line"], p.get("expression", "neutral"), False, "dry")
            for i, p in enumerate(pages, 1)]


def level_lines(level):
    lines = pages((level.get("briefing") or {}).get("pages", []), "briefing")
    timed, last = [], 0.0
    for i, cue in enumerate(level.get("radio") or []):
        last = float(cue["t"]) if "t" in cue else last
        timed.append((last, i, cue))
    for _, _, cue in sorted(timed, key=lambda x: (x[0], x[1])):
        lines += cue_lines(level, cue)
    for secret in level.get("secrets") or []:
        r = secret["radio"]
        lines.append(Line(f"secret: {secret['name']}", r["speaker"], r["line"], r.get("expression", "neutral"),
                          False, "distorted" if r.get("distorted") else "radio"))
    return lines


def sections():
    act = load(ACT / "data.yaml")
    out = [("act-intro", "Act 3 intro", "act-3-red-dust", pages(act["briefing"], "briefing"))]
    for d in sorted(ACT.glob("level-*/data.yaml")):
        slug = d.parent.name
        n = int(slug.split("-")[1])
        title = " ".join(w if w in ("of", "from") else w.capitalize() for w in slug.split("-")[2:])
        out.append((f"l{n}", f"Level {n} {title}", f"act-3-red-dust/{slug}", level_lines(load(d))))
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
    out.append(("other", "Other calls heard in Act 3", "player/specials", other))
    out.append(("act-outro", "Act 3 outro", "act-3-red-dust", pages(act["outro"]["pages"], "outro")))
    return out


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
    if line.key in FIRST:
        out.append(f'<span class="mark rev">first in r{FIRST[line.key]:02d}</span>')
    return " ".join(out)


def write_html(secs):
    total = sum(len(ls) for _, _, _, ls in secs)
    pinned = sum(1 for _, _, _, ls in secs for x in ls if x.pinned)
    heard = sum(1 for _, _, _, ls in secs for x in ls if x.listened and not x.pinned)
    nav = " ".join(f'<a href="#{sid}">{html.escape(title)} ({len(ls)})</a>' for sid, title, _, ls in secs)
    body = []
    for sid, title, doc, ls in secs:
        rows = []
        for i, x in enumerate(ls, 1):
            said = ""
            if x.said and x.said != x.text:
                said = f'<div class="said">spoken: {html.escape(x.said)}</div>'
            note = f'<div class="note">{html.escape(x.note)}</div>' if x.note else ""
            href = f"../../../assets/{x.path}" if x.path else ""
            player = f'<audio controls preload="none" src="{href}"></audio>' if x.exists else ""
            link = f'<a href="{href}">{html.escape(Path(x.path).name)}</a>' if x.exists else ""
            skip = ' class="skip"' if (x.pinned or x.listened) else ""
            rows.append(f"<tr{skip}><td class=\"n\">{i}</td><td class=\"when\">{html.escape(x.when)}</td>"
                        f"<td class=\"who\">{html.escape(x.speaker)}</td>"
                        f"<td>{html.escape(x.text)}{said}{note}</td><td>{player}</td>"
                        f"<td class=\"file\">{link}</td><td>{marks(x)}</td></tr>")
        body.append(f'<section id="{sid}"><h2><a href="../../campaign/{doc}/README.md">{html.escape(title)}</a></h2>'
                    if doc.startswith("act-3") else
                    f'<section id="{sid}"><h2><a href="../../{doc}/README.md">{html.escape(title)}</a></h2>')
        body.append('<div class="wrap"><table class="lt"><tr><th>#</th><th>When</th><th>Speaker</th>'
                    '<th>Line (as shown; as spoken when respelled)</th><th>Play</th><th class="file">File</th>'
                    '<th>Marks</th></tr>' + "".join(rows) + "</table></div></section>")
    page = f"""<!doctype html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Act 3 Listen-through</title>
<style>{CSS}{EXTRA_CSS}</style></head>
<body>
<header><h1>Act 3 listen-through</h1>
<p>Round {ROUND}, I.5: every voiced Act 3 line in play order, {total} lines ({pinned} pinned, {heard} more already
on a round's listen row). Accept or redo each in <a href="README.md">round {ROUND}'s README</a>. Marks: <b>pinned</b>
a seed pin in the <a href="../../audio/voice/README.md">speaker table</a> (hover for why), <b>listened</b> a listen
row of that round, <b>first in</b> the round that first linked the file.</p>
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


def write_md(secs):
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
    md = f"""# Act 3 listen-through: index

Generated by `tools/concept/listen_through.py` (do not edit by hand) for round {ROUND} (I.5): every voiced Act 3
line in play order, on [listen-through.html](listen-through.html). *Pinned*: a seed pin in the
[speaker table](../../audio/voice/data.yaml); *listened*: a listen row of an earlier round links the file (a line can
be both); *missing*: no file for the line's key (should be 0; a stage direction plays its stage sound).

| Section | Lines | Pinned | Listened | Missing |
|---|---|---|---|---|
""" + "\n".join(rows) + "\n"
    (OUT / "listen-through.md").write_text(md, encoding="utf-8")
    return tot


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    secs = sections()
    for _, title, _, ls in secs:
        for x in ls:
            if not x.exists:
                print(f"missing: {title} {x.when} {x.speaker}: {x.text[:60]} ({x.path})")
    write_html(secs)
    tot = write_md(secs)
    print(f"{tot[0]} lines, {tot[1]} pinned, {tot[2]} listened, {tot[3]} missing -> "
          f"{(OUT / 'listen-through.html').relative_to(ROOT)}")


if __name__ == "__main__":
    main()
