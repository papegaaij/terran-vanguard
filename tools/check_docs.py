#!/usr/bin/env python3
"""Validate the living design documentation in design/.

Checks (see CLAUDE.md for the rules):
  1. every directory under design/ has a README.md (concept/ directories are exempt)
  2. every README.md has valid frontmatter
  3. every subdirectory is listed in its parent's Contents table, with Design/Impl/Art
     columns matching the child's frontmatter
  4. every file in a concept/ directory is referenced by the owning README, or (rejected proposals)
     by concept/rejected/README.md, the one file allowed there, which the README must link to and
     whose rows must all be rejected
  5. relative links in markdown files, depends-on entries and review-board HTML resolve
  6. tables marked <!-- data: NAME --> match what tools/sync_tables.py renders from data.yaml
  7. a decisions-archive.md (a README's older Decisions entries) sits next to a README.md that
     links to it; its links are checked as in 5

Usage: python3 tools/check_docs.py        exit code 1 when problems are found
       python3 tools/check_docs.py --fix  first sync Contents status cells from the children and
                                          re-render the marked tables
       python3 tools/check_docs.py --self-test
                                          run the checker on small generated trees (the rejected
                                          table and decisions archive rules) and report the result
"""
import re
import sys
from datetime import date
from pathlib import Path

import sync_tables

ROOT = Path(__file__).resolve().parent.parent
DESIGN = ROOT / "design"

REQUIRED = ["title", "design", "implementation", "art", "updated"]
ALLOWED = {
    "design": {"idea", "draft", "review", "approved"},
    "implementation": {"n/a", "not-started", "in-progress", "done"},
    "art": {"n/a", "none", "proposed", "chosen", "final"},
}
MD_LINK = re.compile(r"\]\(([^)\s]+)(?:\s+\"[^\"]*\")?\)")
HTML_LINK = re.compile(r"""(?:src|href)\s*=\s*["']([^"']+)["']""")

problems = []
FIX = "--fix" in sys.argv[1:]


def rel(p):
    return p.relative_to(ROOT).as_posix()


def report(path, msg):
    problems.append(f"{rel(path)}: {msg}")


def parse_frontmatter(path):
    text = path.read_text(encoding="utf-8")
    if not text.startswith("---\n"):
        report(path, "missing frontmatter")
        return None
    end = text.find("\n---", 4)
    if end < 0:
        report(path, "unterminated frontmatter")
        return None
    meta = {}
    for line in text[4:end].splitlines():
        line = line.split(" #", 1)[0].rstrip()
        if not line.strip():
            continue
        if ":" not in line:
            report(path, f"bad frontmatter line: {line!r}")
            continue
        key, value = line.split(":", 1)
        value = value.strip()
        if value.startswith("[") and value.endswith("]"):
            value = [v.strip() for v in value[1:-1].split(",") if v.strip()]
        meta[key.strip()] = value
    return meta


def check_frontmatter(path, meta):
    for key in REQUIRED:
        if key not in meta or meta[key] in ("", []):
            report(path, f"frontmatter lacks '{key}'")
    for key, allowed in ALLOWED.items():
        if key in meta and meta[key] not in allowed:
            report(path, f"'{key}: {meta[key]}' not one of {sorted(allowed)}")
    if "updated" in meta:
        try:
            date.fromisoformat(meta["updated"])
        except (TypeError, ValueError):
            report(path, f"'updated: {meta['updated']}' is not YYYY-MM-DD")
    for dep in meta.get("depends-on", []) or []:
        if not (path.parent / dep).exists():
            report(path, f"depends-on '{dep}' does not exist")


def is_concept(path):
    return "concept" in path.relative_to(DESIGN).parts


def check_contents(readme, subdirs, metas):
    lines = readme.read_text(encoding="utf-8").splitlines()
    for sub in subdirs:
        target = f"{sub.name}/README.md"
        rows = [i for i, l in enumerate(lines) if l.lstrip().startswith("|") and f"]({target})" in l]
        if not rows:
            report(readme, f"Contents table has no row linking '{target}'")
            continue
        child = metas.get(sub / "README.md")
        if not child:
            continue
        cells = [c.strip().strip("`") for c in lines[rows[0]].strip().strip("|").split("|")]
        if len(cells) < 5:
            report(readme, f"Contents row for '{sub.name}' needs Part|Summary|Design|Impl|Art")
            continue
        wanted = [child.get(k, "") for k in ("design", "implementation", "art")]
        if FIX and cells[-3:] != wanted:
            lines[rows[0]] = "| " + " | ".join(cells[:-3] + wanted) + " |"
            readme.write_text("\n".join(lines) + "\n", encoding="utf-8")
            print(f"fixed: {rel(readme)} row '{sub.name}'")
            continue
        for key, got in zip(("design", "implementation", "art"), cells[-3:]):
            if child.get(key) != got:
                report(readme, f"Contents row '{sub.name}': {key} is '{got}', "
                               f"child says '{child.get(key)}'")


REJECTED = "concept/rejected/README.md"


def link_targets(path):
    """The relative link targets of a markdown file, resolved (without anchors)."""
    text = path.read_text(encoding="utf-8")
    out = set()
    for target in MD_LINK.findall(text):
        if re.match(r"^(?:[a-z]+:|#|//)", target):
            continue
        out.add((path.parent / target.split("#", 1)[0]).resolve())
    return out


def check_rejected_table(readme, rejected):
    if REJECTED not in readme.read_text(encoding="utf-8"):
        report(readme, f"does not link its {REJECTED}")
    for line in rejected.read_text(encoding="utf-8").splitlines():
        if not line.startswith("|") or re.fullmatch(r"[|\s:-]+", line):
            continue
        cells = [c.strip() for c in re.split(r"(?<!\\)\|", line.strip().strip("|"))]
        if cells[0] == "File":
            continue
        if not cells[-1].lower().startswith("rejected"):
            report(rejected, f"row with status '{cells[-1][:40]}' is not rejected: {cells[0][:80]}")


def check_concept_files(owner_dir):
    readme = owner_dir / "README.md"
    if not readme.exists():
        return
    text = readme.read_text(encoding="utf-8")
    rejected = owner_dir / REJECTED
    rejected_text, rejected_links = "", set()
    if rejected.exists():
        check_rejected_table(readme, rejected)
        rejected_text, rejected_links = rejected.read_text(encoding="utf-8"), link_targets(rejected)
    for f in sorted((owner_dir / "concept").rglob("*")):
        if f.is_file() and f.name != "prompts.md" and f != rejected:
            ref = f.relative_to(owner_dir).as_posix()
            if ref not in text and ref not in rejected_text and f.resolve() not in rejected_links:
                report(readme, f"concept file '{ref}' is not listed in the README or {REJECTED}")
    if not (owner_dir / "concept" / "prompts.md").exists():
        report(readme, "concept/ has no prompts.md")
    for md in sorted((owner_dir / "concept").rglob("*.md")):
        if md.name != "prompts.md" and md != rejected:
            report(md, "only prompts.md and rejected/README.md are allowed markdown files in concept/")


def check_archive(archive):
    readme = archive.parent / "README.md"
    if not readme.exists():
        report(archive, "has no README.md next to it")
    elif "](decisions-archive.md" not in readme.read_text(encoding="utf-8"):
        report(readme, "does not link its decisions-archive.md")
    if not archive.read_text(encoding="utf-8").startswith("# "):
        report(archive, "does not start with a '# ' title")


def check_links(path, pattern):
    text = path.read_text(encoding="utf-8")
    text = re.sub(r"```.*?```", "", text, flags=re.S)  # ignore code blocks
    text = re.sub(r"`[^`\n]*`", "", text)  # and inline code
    for target in pattern.findall(text):
        if re.match(r"^(?:[a-z]+:|#|//)", target) or "{" in target:
            continue
        target = target.split("#", 1)[0].split("?", 1)[0]
        if target and not (path.parent / target).exists():
            report(path, f"broken link '{target}'")


def check_data_tables():
    for readme in sync_tables.marked_readmes():
        try:
            new = sync_tables.synced(readme)
        except (KeyError, ValueError, OSError) as e:
            report(readme, f"cannot render its data tables: {e!r}")
            continue
        if new == readme.read_text(encoding="utf-8"):
            continue
        if FIX:
            readme.write_text(new, encoding="utf-8")
            print(f"fixed: {rel(readme)} data tables")
        else:
            report(readme, "a marked table differs from its data.yaml (run tools/sync_tables.py)")


def run(data_tables=True):
    if not DESIGN.is_dir():
        print("design/ not found")
        return None
    dirs = [DESIGN] + sorted(d for d in DESIGN.rglob("*") if d.is_dir())
    metas = {}
    for d in dirs:
        if is_concept(d):
            continue
        readme = d / "README.md"
        if not readme.exists():
            report(d, "directory has no README.md")
            continue
        meta = parse_frontmatter(readme)
        if meta is not None:
            check_frontmatter(readme, meta)
            metas[readme] = meta
    for d in dirs:
        if is_concept(d) or not (d / "README.md").exists():
            continue
        subdirs = [s for s in sorted(d.iterdir()) if s.is_dir() and s.name != "concept"]
        check_contents(d / "README.md", subdirs, metas)
        if (d / "concept").is_dir():
            check_concept_files(d)
    for archive in sorted(DESIGN.rglob("decisions-archive.md")):
        check_archive(archive)
    for md in [ROOT / "README.md", ROOT / "CLAUDE.md"] + sorted(DESIGN.rglob("*.md")):
        if md.exists():
            check_links(md, MD_LINK)
    for html in sorted(DESIGN.rglob("*.html")):
        check_links(html, HTML_LINK)
    if data_tables:
        check_data_tables()
    return len(metas)


def self_test():
    """Run the checker on small generated trees; each case names the problems it must (not) find."""
    import tempfile
    global ROOT, DESIGN
    saved = ROOT, DESIGN
    fm = "---\ntitle: T\ndesign: draft\nimplementation: n/a\nart: proposed\nupdated: 2026-10-25\n---\n\n# T\n"
    base = {
        "design/README.md": fm + "\n| Part | Summary | Design | Impl | Art |\n|---|---|---|---|---|\n"
                             "| [part](part/README.md) | x | draft | n/a | proposed |\n",
        "design/part/concept/prompts.md": "# prompts\n",
        "design/part/concept/a-r01-a.png": "",
        "design/part/concept/rejected/a-r01-b.png": "",
        "design/part/README.md": fm + "\n## Concept art\n\nRejected: [concept/rejected/README.md]"
                                  "(concept/rejected/README.md).\n\n| File | What | Status |\n|---|---|---|\n"
                                  "| [concept/a-r01-a.png](concept/a-r01-a.png) | a | chosen |\n"
                                  "\n## Decisions\n\nOlder: [decisions-archive.md](decisions-archive.md).\n",
        "design/part/concept/rejected/README.md": "# T rejected\n\n| File | What | Status |\n|---|---|---|\n"
                                                  "| [concept/rejected/a-r01-b.png](a-r01-b.png) | b | rejected |\n",
        "design/part/decisions-archive.md": "# T decisions archive\n\n- 2026-10-01: [x](README.md#decisions)\n",
    }
    cases = [  # (name, file changes, substrings every expected problem contains)
        ("valid tree", {}, []),
        ("rejected file listed nowhere", {"design/part/concept/rejected/README.md": "# T rejected\n"},
         ["concept file 'concept/rejected/a-r01-b.png' is not listed"]),
        ("rejected file listed only by its link", {"design/part/concept/rejected/README.md":
            "# T\n\n| File | What | Status |\n|---|---|---|\n| [b](a-r01-b.png) | b | rejected |\n"}, []),
        ("chosen row in the rejected table", {"design/part/concept/rejected/README.md":
            "# T\n\n| File | What | Status |\n|---|---|---|\n| [b](a-r01-b.png) | b | chosen |\n"},
         ["is not rejected"]),
        ("rejected table not linked", {"design/part/README.md": fm + "\n[concept/a-r01-a.png](concept/a-r01-a.png)"
            " [d](decisions-archive.md)\n"}, ["does not link its concept/rejected/README.md"]),
        ("broken link in the rejected table", {"design/part/concept/rejected/README.md":
            "# T\n\n| File | What | Status |\n|---|---|---|\n| [concept/rejected/a-r01-b.png](a-r01-b.png) |"
            " [c](../gone.png) | rejected |\n"}, ["broken link '../gone.png'"]),
        ("other markdown in concept/", {"design/part/concept/notes.md": "# n\n"},
         ["only prompts.md", "concept file 'concept/notes.md' is not listed"]),
        ("archive not linked", {"design/part/README.md": base["design/part/README.md"].replace(
            "Older: [decisions-archive.md](decisions-archive.md).", "")}, ["does not link its decisions-archive.md"]),
        ("broken link in the archive", {"design/part/decisions-archive.md": "# A\n\n- 2026-10-01: [x](gone.md)\n"},
         ["broken link 'gone.md'"]),
    ]
    failed = 0
    try:
        for name, changes, expected in cases:
            with tempfile.TemporaryDirectory() as tmp:
                ROOT, DESIGN = Path(tmp), Path(tmp) / "design"
                for rel_path, content in {**base, **changes}.items():
                    f = ROOT / rel_path
                    f.parent.mkdir(parents=True, exist_ok=True)
                    f.write_text(content, encoding="utf-8")
                problems.clear()
                run(data_tables=False)
                ok = (all(any(e in p for p in problems) for e in expected)
                      and all(any(e in p for e in expected) for p in problems))
                failed += not ok
                print(f"{'ok  ' if ok else 'FAIL'} {name}" + ("" if ok else f": {problems}"))
    finally:
        ROOT, DESIGN = saved
        problems.clear()
    print(f"self-test: {len(cases) - failed} of {len(cases)} cases passed")
    return 1 if failed else 0


def main():
    if "--self-test" in sys.argv[1:]:
        return self_test()
    count = run()
    if count is None:
        return 1
    for p in problems:
        print(p)
    print(f"{count} documents checked, {len(problems)} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
