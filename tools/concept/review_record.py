#!/usr/bin/env python3
"""Record page of a closed concept round: design/concept-rounds/round-NN/review.html.

    python3 tools/concept/review_record.py NN [--check] [--out FILE]

A static, self-contained page (inline CSS, light/dark, no script) that records what a round put in front of the user
and what was decided: the round's title and summary and every row as the review page showed it (section, part link,
the question with its variants, Claude's weak spots, the recommendation, the media) plus the user's answer and the
outcome, read-only. Run it once at the round's close (design/concept-rounds/README.md, "Record page").

Sources, in this order:
  1. the row data the review page embedded: build/roundNN-review/index.html, the <script type="application/json"
     id="review-data"> block (round 34: id="r34-data");
  2. the round README's tables: the Part cell and the Answer / Outcome cells (the record of truth; a row the README
     lacks, or an empty cell, shows "not recorded").
Media is never copied: the review page's media/...webp|mp3|mp4 names are mapped back to the originals in the design
tree (or assets/voice for the voice takes) by file stem and linked by relative path (images small and lazy, audio and
video with preload="none"). A file not found shows a plain "file not in repo" note. --check writes nothing and lists
what could not be mapped.
"""

import argparse
import collections
import html
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MEDIA_EXT = {".png", ".gif", ".jpg", ".jpeg", ".ogg", ".wav", ".mp4", ".webm", ".mp3", ".webp"}
IMG_EXT = {".png", ".gif", ".jpg", ".jpeg", ".webp"}
VID_EXT = {".mp4", ".webm"}
AUD_EXT = {".ogg", ".wav", ".mp3"}
PREFER = {
    "img": [".png", ".jpg", ".jpeg", ".webp", ".gif"],
    "vid": [".mp4", ".webm", ".gif"],
    "aud": [".ogg", ".wav", ".mp3"],
}


# ---------------------------------------------------------------------------------------------------- sources


def load_review(n):
    path = os.path.join(ROOT, "build", f"round{n}-review", "index.html")
    if not os.path.exists(path):
        return None
    text = open(path, encoding="utf-8").read()
    m = re.search(r'<script type="application/json" id="(?:review-data|r%d-data)">(.*?)</script>' % n, text, re.S)
    return json.loads(m.group(1)) if m else None


def split_row(line):
    return [c.strip() for c in re.split(r"(?<!\\)\|", line.strip()[1:-1])]


def load_readme(n):
    """id -> {header: cell} of every table row in the round README (the first row with an answer wins)."""
    path = os.path.join(ROOT, "design", "concept-rounds", f"round-{n:02d}", "README.md")
    rows = {}
    hdr = None
    for line in open(path, encoding="utf-8").read().split("\n"):
        if not line.startswith("|"):
            hdr = None
            continue
        if re.fullmatch(r"[|\-: ]+", line):
            continue
        cells = split_row(line)
        if hdr is None:
            hdr = [c.lower() for c in cells]
            continue
        rid = re.sub(r"[*`]", "", cells[0])
        row = dict(zip(hdr, cells))
        old = rows.get(rid)
        if old is None or (not (old.get("answer") or old.get("outcome")) and (row.get("answer") or row.get("outcome"))):
            rows[rid] = row
    return rows


def build_index():
    idx = collections.defaultdict(list)
    for top in ("design", os.path.join("assets", "voice")):
        for dp, _dn, fn in os.walk(os.path.join(ROOT, top)):
            for f in fn:
                s, e = os.path.splitext(f)
                if e.lower() in MEDIA_EXT and e.lower() != ".webp":
                    idx[s].append(os.path.relpath(os.path.join(dp, f), ROOT))
    return idx


# ---------------------------------------------------------------------------------------------------- mapping


def stem_variants(stem):
    """Candidate original stems of a review page's file stem, with the level hint they carry."""
    out = [(stem, None)]
    m = re.match(r"^l(\d\d)-(.+)$", stem)
    if m:
        out.append((m.group(2), m.group(1)))
    for s, h in list(out):
        s2 = re.sub(r"-(?:\d+|p\d+)$", "", s)
        if s2 != s:
            out.append((s2, h))
    return out


class Mapper:
    def __init__(self, n, rowdir):
        self.n = n
        self.index = build_index()
        self.rowdir = rowdir  # absolute dir of review.html
        self.unmapped = []

    def find(self, src, kind, hints):
        stem = os.path.splitext(os.path.basename(src))[0]
        if stem.endswith("-poster"):
            return None
        for cand, level in stem_variants(stem):
            files = self.index.get(cand)
            if not files:
                continue
            hs = list(hints) + ([f"level-{level}"] if level else [])
            order = PREFER[kind]

            def score(p):
                ext = os.path.splitext(p)[1].lower()
                ext_rank = order.index(ext) if ext in order else 9
                aff = sum(1 for h in hs if h and h in p)
                rejected = 1 if "/rejected/" in p else 0
                return (-aff, ext_rank, rejected, p)

            best = sorted(files, key=score)[0]
            return os.path.relpath(os.path.join(ROOT, best), self.rowdir)
        return None


# ---------------------------------------------------------------------------------------------------- markdown


def inline_md(text):
    """The README's inline Markdown (code, bold, italic, links) as HTML."""
    if not text:
        return ""
    parts = re.split(r"(`[^`]*`)", text)
    out = []
    for p in parts:
        if p.startswith("`") and p.endswith("`") and len(p) > 1:
            out.append("<code>" + html.escape(p[1:-1]) + "</code>")
            continue
        p = html.escape(p, quote=False)
        p = re.sub(r"\[([^\]]+)\]\(([^)\s]+)\)", lambda m: f'<a href="{m.group(2)}">{m.group(1)}</a>', p)
        p = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", p)
        p = re.sub(r"(?<![*\w])\*([^*\s][^*]*)\*(?!\w)", r"<em>\1</em>", p)
        p = p.replace("&lt;br&gt;", "<br>")
        out.append(p)
    return "".join(out)


def plain(text):
    return re.sub(r"[*`_]", "", re.sub(r"\[([^\]]*)\]\([^)]*\)", r"\1", text or "")).strip()


def clean_html(s):
    """Fragments from the review page: drop its in-page anchors, keep the rest."""
    s = re.sub(r'<a href="#[^"]*">(.*?)</a>', r"<span>\1</span>", s or "", flags=re.S)
    return s


# ---------------------------------------------------------------------------------------------------- render


def answer_block(row, rec, options):
    ans = (row or {}).get("answer", "")
    out = (row or {}).get("outcome", "")
    if not row or not (ans or out):
        extra = "".join(
            f'<div class="ans out"><span class="tag">README: {html.escape(k)}</span> <span>{inline_md(row[k])}</span></div>'
            for k in ("built (where)", "proposal (pending)", "then")
            if row and row.get(k)
        )
        return extra + '<div class="ans none"><span class="tag">Answer</span> <span>not recorded</span></div>'
    parts = []
    if ans:
        first = plain(ans).split()[0].lower().strip(".,;:()") if plain(ans) else ""
        badge = ""
        if rec and first == str(rec).lower():
            badge = '<span class="badge ok">as recommended</span> '
        elif rec and first in {str(o).lower() for o in options or []}:
            badge = '<span class="badge pick">not the recommendation</span> '
        parts.append(f'<div class="ans"><span class="tag">Answer</span> {badge}<span>{inline_md(ans)}</span></div>')
    if out:
        parts.append(f'<div class="ans out"><span class="tag">Outcome</span> <span>{inline_md(out)}</span></div>')
    return "".join(parts)


def media_html(mlist, mapper, hints, sink):
    out = []
    for m in mlist or []:
        label = html.escape(m.get("label", ""))
        t = m["t"]
        if t == "img":
            srcs = [x["src"] for x in m.get("tiles", [])]
            kind = "img"
        elif t == "vid":
            srcs = [x["src"] for x in m.get("parts", [])]
            kind = "vid"
        else:
            srcs = [m["src"]]
            kind = "aud"
        seen = []
        for s in srcs:
            f = mapper.find(s, kind, hints)
            if f is None:
                if not os.path.basename(s).endswith("-poster.webp"):
                    sink.append(s)
                    seen.append(("missing", s))
            elif ("ok", f) not in seen:
                seen.append(("ok", f))
        if len(seen) > 1 and t == "img":
            # tiles of one sheet: the original is one file, listed once; keep distinct files only
            pass
        for state, f in seen:
            if state == "missing":
                out.append(
                    f'<figure class="m miss"><figcaption>{label} <em>file not in repo</em> '
                    f"<code>{html.escape(os.path.basename(f))}</code></figcaption></figure>"
                )
                continue
            ext = os.path.splitext(f)[1].lower()
            href = html.escape(f)
            if ext in AUD_EXT:
                out.append(
                    f'<figure class="m aud"><figcaption>{label}</figcaption>'
                    f'<audio controls preload="none" src="{href}"></audio></figure>'
                )
            elif ext in VID_EXT:
                out.append(
                    f'<figure class="m vid"><figcaption>{label}</figcaption>'
                    f'<video controls preload="none" playsinline src="{href}"></video></figure>'
                )
            else:
                out.append(
                    f'<figure class="m img"><a href="{href}"><img loading="lazy" decoding="async" src="{href}" '
                    f'alt="{label}"></a><figcaption>{label}</figcaption></figure>'
                )
    return '<div class="media">' + "".join(out) + "</div>" if out else ""


def rootlinks(s):
    """Links and images in a quoted draft are relative to the repository root: make them relative to the page."""

    def fix(m):
        path = m.group(2)
        if re.match(r"(?:[a-z]+:|#|\.\.|/)", path) or not os.path.exists(os.path.join(ROOT, path.split("#")[0])):
            return m.group(0)
        return f'{m.group(1)}="../../../{path}"'

    return re.sub(r'\b(href|src)="([^"]*)"', fix, s)


def row_html(r, readme, mapper, sink):
    rid = r["id"]
    row = readme.get(rid)
    rec = r.get("rec")
    part = ""
    if row and row.get("part"):
        part = f'<div class="part">{inline_md(row["part"])}</div>'
    hints = []
    if row:
        for m in re.finditer(r"\(([^)\s]+)\)", row.get("part", "")):
            d = m.group(1).split("#")[0]
            hints.append(os.path.normpath(os.path.join(os.path.dirname(mapper.readme_path), d)))
            hints.append(os.path.basename(os.path.dirname(hints[-1])))
    kind = r.get("kind", "")
    h = [f'<article class="row" id="r-{html.escape(re.sub(r"[^A-Za-z0-9._-]", "_", rid))}">']
    h.append(
        f'<header><span class="rid">{html.escape(rid)}</span><h3>{html.escape(r.get("title", ""))}</h3>'
        f'<span class="kind">{html.escape(kind)}</span></header>'
    )
    h.append(part)
    if r.get("line"):
        h.append(f'<p class="line">{html.escape(r["line"])}</p>')
    if r.get("fields"):
        h.append("<dl>" + "".join(f"<dt>{html.escape(k)}</dt><dd>{clean_html(v)}</dd>" for k, v in r["fields"]) + "</dl>")
    if r.get("decide"):
        h.append(f'<div class="q">{clean_html(r["decide"])}</div>')
    for s in r.get("sub", []) or []:
        sr = readme.get(s["id"])
        h.append(
            f'<div class="sub"><span class="rid">{html.escape(s["id"])}</span> {clean_html(s.get("html", ""))} '
            f'<span class="opts">{" / ".join(html.escape(o) for o in s.get("options", []))}</span>'
            + (answer_block(sr, None, None) if sr else "")
            + "</div>"
        )
    if r.get("listen"):
        h.append(
            '<ul class="listen">'
            + "".join(
                f'<li><strong>{html.escape(x["what"])}</strong>: {html.escape(x.get("hint", ""))}</li>' for x in r["listen"]
            )
            + "</ul>"
        )
    h.append(media_html(r.get("media"), mapper, hints, sink))
    for dt in r.get("details", []) or []:
        h.append(f'<details><summary>{html.escape(dt["title"])}</summary>{rootlinks(clean_html(dt["html"]))}</details>')
    if r.get("weak"):
        h.append(f'<p class="weak"><span class="tag">Weak spots (Claude)</span> {clean_html(r["weak"])}</p>')
    if rec:
        h.append(f'<p class="rec"><span class="tag">Recommendation</span> {html.escape(str(rec))}</p>')
    if r.get("outcome") and not (row and row.get("outcome")):
        h.append(f'<div class="ans out"><span class="tag">Outcome</span> <span>{clean_html(r["outcome"])}</span></div>')
    h.append(answer_block(row, rec, r.get("options")))
    h.append("</article>")
    return "\n".join(x for x in h if x)


CSS = """
:root{--bg:#f4f5f7;--fg:#1b2230;--mut:#5a6577;--card:#fff;--line:#d3d9e2;--acc:#1f5fa8;--ok:#1d6b3a;--okbg:#e3f3e8;
--hi:#fff6dc;--hib:#d9b84a;--code:#e9edf3;--warn:#8a4b00}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--bg:#10141b;--fg:#dde3ee;--mut:#98a3b6;--card:#171d27;
--line:#2c3544;--acc:#7fb2ee;--ok:#7bd69a;--okbg:#163323;--hi:#2c2610;--hib:#8a7424;--code:#232c3a;--warn:#e3a35a;color-scheme:dark}}
:root[data-theme="dark"]{--bg:#10141b;--fg:#dde3ee;--mut:#98a3b6;--card:#171d27;--line:#2c3544;--acc:#7fb2ee;--ok:#7bd69a;
--okbg:#163323;--hi:#2c2610;--hib:#8a7424;--code:#232c3a;--warn:#e3a35a;color-scheme:dark}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--fg);font:16px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
main{max-width:60rem;margin:0 auto;padding:1rem 16px 4rem}
h1{font-size:1.6rem;margin:.5rem 0}h2{font-size:1.25rem;margin:2rem 0 .5rem;border-bottom:1px solid var(--line);padding-bottom:.25rem}
h3{font-size:1.02rem;margin:0;flex:1 1 12rem}a{color:var(--acc)}
.sum{color:var(--mut)}.note{font-size:.9rem;color:var(--mut)}
nav.sec{display:flex;flex-wrap:wrap;gap:.4rem;margin:1rem 0}nav.sec a{padding:.2rem .6rem;border:1px solid var(--line);border-radius:1rem;
text-decoration:none;font-size:.9rem;background:var(--card)}
.row{background:var(--card);border:1px solid var(--line);border-radius:.4rem;padding:.7rem .9rem;margin:.7rem 0;overflow-wrap:anywhere}
.row header{display:flex;flex-wrap:wrap;gap:.3rem .6rem;align-items:baseline}
.rid{font:600 .85rem ui-monospace,Menlo,Consolas,monospace;background:var(--code);padding:.05rem .4rem;border-radius:.25rem}
.kind{font-size:.8rem;color:var(--mut)}.part{font-size:.88rem;margin:.2rem 0}
.q,.weak,.rec,.sub,.line,dl{font-size:.92rem;margin:.45rem 0}.weak{color:var(--warn)}
code{background:var(--code);padding:0 .25rem;border-radius:.2rem;font-size:.88em}
.tag{font-size:.72rem;text-transform:uppercase;letter-spacing:.05em;color:var(--mut);font-weight:700;margin-right:.3rem}
.ans{margin:.5rem 0 0;padding:.45rem .6rem;border-left:4px solid var(--hib);background:var(--hi);border-radius:.2rem}
.ans.out{border-left-color:var(--ok)}.ans.none{opacity:.7}
.badge{font-size:.75rem;font-weight:700;padding:.05rem .4rem;border-radius:.6rem;background:var(--okbg);color:var(--ok)}
.badge.pick{background:var(--hi);color:var(--warn);border:1px solid var(--hib)}
dl{display:grid;grid-template-columns:7rem 1fr;gap:.2rem .6rem}dt{font-weight:700}dd{margin:0}
.media{display:flex;flex-wrap:wrap;gap:.5rem;margin:.5rem 0}.m{margin:0;max-width:100%}.m figcaption{font-size:.75rem;color:var(--mut)}
.m.img img{max-width:14rem;max-height:10rem;border:1px solid var(--line);border-radius:.2rem;background:#000;display:block}
.m.vid video{width:14rem;max-width:100%}.m.aud audio{width:16rem;max-width:100%}.miss{color:var(--warn)}
details{margin:.4rem 0;font-size:.9rem}summary{cursor:pointer;color:var(--acc)}
.listen{font-size:.85rem;color:var(--mut);margin:.3rem 0;padding-left:1.2rem}.opts{color:var(--mut)}
.sub{padding-left:.6rem;border-left:2px solid var(--line)}
table{border-collapse:collapse;display:block;overflow-x:auto}td,th{border:1px solid var(--line);padding:.2rem .4rem}
blockquote{margin:.4rem 0;padding-left:.8rem;border-left:3px solid var(--line)}
@media (max-width:480px){dl{grid-template-columns:1fr}.m.img img{max-width:100%}}
"""


def sections_of(d):
    for s in d["sections"]:
        yield s, s.get("rows", []), [(g["title"], g["rows"]) for g in s.get("groups", [])]


def build(n, check=False):
    d = load_review(n)
    if d is None:
        return None, ["no review data (build/round%d-review/index.html with a JSON block)" % n], 0
    readme_path = os.path.join(ROOT, "design", "concept-rounds", f"round-{n:02d}", "README.md")
    outdir = os.path.dirname(readme_path)
    readme = load_readme(n)
    mapper = Mapper(n, outdir)
    mapper.readme_path = readme_path
    unmapped = []
    notrec = []
    parts = []
    nav = []
    for s, rows, groups in sections_of(d):
        sid = html.escape(s["id"])
        nav.append(f'<a href="#s-{sid}">{html.escape(s["nav"])}</a>')
        parts.append(f'<section id="s-{sid}"><h2>{html.escape(s["title"])}</h2>')
        if s.get("intro"):
            parts.append(f'<div class="sum">{clean_html(s["intro"])}</div>')
        allrows = list(rows)
        for r in rows:
            parts.append(row_html(r, readme, mapper, unmapped))
        for gt, grows in groups:
            parts.append(f"<h3>{html.escape(gt)}</h3>")
            for r in grows:
                allrows.append(r)
                parts.append(row_html(r, readme, mapper, unmapped))
        for r in allrows:
            x = readme.get(r["id"])
            if not x or not (x.get("answer") or x.get("outcome")):
                notrec.append(r["id"])
        parts.append("</section>")
    title = f"Round {n}: record of the decisions"
    head = (
        '<!doctype html><html lang="en"><head><meta charset="utf-8">'
        '<meta name="viewport" content="width=device-width,initial-scale=1">'
        f"<title>{html.escape(title)}</title><style>{CSS}</style></head><body><main>"
        f'<p class="note"><a href="README.md">Round {n} README</a> · <a href="../README.md">All rounds</a> · '
        f'built {html.escape(str(d.get("built", "")))} · recorded by <code>tools/concept/review_record.py {n}</code></p>'
        f"<h1>{html.escape(d.get('title', 'Concept round %d' % n))}: record of the decisions</h1>"
        f'<div class="sum">{clean_html(d.get("summary", ""))}</div>'
        '<p class="note">A read-only record: every row as the review page showed it, with the answer and the outcome '
        "from the round README (the record of truth). Media is linked from the design tree, not copied.</p>"
        f'<nav class="sec">{"".join(nav)}</nav>'
    )
    page = head + "\n".join(parts) + "</main></body></html>\n"
    problems = []
    if unmapped:
        problems.append(f"{len(unmapped)} media not mapped: " + ", ".join(sorted(set(os.path.basename(u) for u in unmapped))[:8]))
    if notrec:
        problems.append(f"{len(notrec)} rows without a recorded answer/outcome: " + ", ".join(notrec[:8]))
    return page, problems, len(unmapped)


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("round", type=int)
    ap.add_argument("--check", action="store_true", help="write nothing, list what could not be mapped")
    ap.add_argument("--out", help="write here instead of design/concept-rounds/round-NN/review.html")
    a = ap.parse_args()
    page, problems, _ = build(a.round)
    if page is None:
        print(f"round {a.round}: {problems[0]}")
        return 1
    for p in problems:
        print(f"round {a.round}: {p}")
    if not a.check:
        out = a.out or os.path.join(ROOT, "design", "concept-rounds", f"round-{a.round:02d}", "review.html")
        open(out, "w", encoding="utf-8").write(page)
        print(f"round {a.round}: {out} {len(page.encode()) // 1024} KB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
