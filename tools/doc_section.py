#!/usr/bin/env python3
"""Read a Markdown file by section instead of whole (saves an agent's context).

  python3 tools/doc_section.py FILE                  the outline: every heading with its line range and size
  python3 tools/doc_section.py FILE "Heading" [...]  the sections whose heading contains that text (case-insensitive),
                                                     each with its subsections; several headings print several sections
  python3 tools/doc_section.py FILE "Heading" --grep TEXT
                                                     only the lines of those sections that contain TEXT (with line numbers)

Headings inside fenced code blocks are ignored. Line numbers are 1-based, so a section can be edited after reading
it (`Read` with offset/limit).
"""
import re
import sys


def headings(lines):
    out, fence = [], False
    for i, line in enumerate(lines):
        if line.lstrip().startswith("```"):
            fence = not fence
        elif not fence:
            m = re.match(r"(#{1,6})\s+(.*)", line)
            if m:
                out.append((i, len(m.group(1)), m.group(2).strip()))
    return out


def section_end(hs, k, total):
    level = hs[k][1]
    for i, lv, _ in hs[k + 1:]:
        if lv <= level:
            return i
    return total


def main(argv):
    if len(argv) < 2:
        sys.exit(__doc__)
    path, rest = argv[1], argv[2:]
    grep = None
    if "--grep" in rest:
        j = rest.index("--grep")
        grep, rest = rest[j + 1].lower(), rest[:j] + rest[j + 2:]
    lines = open(path, encoding="utf-8").read().split("\n")
    hs = headings(lines)
    if not rest:
        for k, (i, level, title) in enumerate(hs):
            end = section_end(hs, k, len(lines))
            size = sum(len(l) + 1 for l in lines[i:end])
            print(f"{'  ' * (level - 1)}{title}  [lines {i + 1}-{end}, {size / 1024:.1f} KB]")
        return
    found = False
    for want in rest:
        for k, (i, _, title) in enumerate(hs):
            if want.lower() in title.lower():
                found = True
                end = section_end(hs, k, len(lines))
                for n in range(i, end):
                    if grep is None or n == i or grep in lines[n].lower():
                        print(f"{n + 1}: {lines[n]}" if grep else lines[n])
                break
    if not found:
        sys.exit(f"no heading containing {rest!r} in {path}")


if __name__ == "__main__":
    main(sys.argv)
