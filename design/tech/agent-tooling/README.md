---
title: Agent tooling
design: review
implementation: done
art: n/a
depends-on: [../architecture]
updated: 2026-11-13
---

# Agent tooling

## Summary

The tools Claude Code's agents use to read this repo cheaply: `tools/doc_section.py` for reading big READMEs by
section, grep and line-range reads for code. Two code-navigation trials are over: the language servers (Pyright stays
installed with `tools/dev/setup_lsp.sh`, Eclipse JDT LS was removed on 2026-11-13) and jCodeMunch (dropped on
2026-11-13). None of it is needed to build or play the game.

## Design

### Why

A measurement of one session's 202 sub-agents (2026-10-25) found that cost is dominated by re-reading context: each API
call re-reads the agent's whole conversation, and about 80 % of that volume was file content the agents had read through
the shell: Java source about 40 %, Markdown about 21 %, Python about 10 %, YAML about 6 %. A language server lets an agent
find a class, a method's callers or a module's functions without reading files to look for them; the section tool does
the same for documents. Research on coding agents supports structure-aware navigation for finding the right code
(SWE-agent, AutoCodeRover, RepoGraph, LocAgent); embedding/vector search was rejected (studies find grep-style search as
good or better on code). The effect on this project's token use is measured with `briefs/tokmeter.py` (local) against
earlier steps.

### Setup (a new machine)

1. Prerequisites: Node.js with npm, Python 3, and `~/.local/bin` on `PATH`.
   Linux or macOS (Windows: WSL).
2. Run `tools/dev/setup_lsp.sh` (safe to rerun; `--check` only reports). It installs, at pinned versions:
   - **Pyright 1.1.414** (MIT) into `~/.local/share/pyright/1.1.414` (npm, install scripts off), linked as
     `~/.local/bin/pyright-langserver`;
   - the official Claude Code plugin `pyright-lsp` (user scope, from `claude-plugins-official`);
   - links from `build/pyright-venvs/{chatterbox,dia}` to the voice TTS venvs' `site-packages` in `~/.cache/tv-tts/`, when
     this machine has them (see [voice](../../audio/voice/README.md)), so Pyright resolves the voice tools' imports.
3. Run `/reload-plugins` in Claude Code (or restart it). Check with the LSP tool on a Python file under `tools/`.

### How it is configured

- **`pyrightconfig.json`** (tracked): only `tools/`, type checking off (the scripts are untyped numpy/PIL code: only
  syntax errors and unresolved imports are reported), the tool folders as import roots (the scripts import each other by
  folder), the voice venv links.
- **The watch proxy** (`tools/dev/lsp_watch_proxy.py`, installed to `~/.local/share/lsp-watch-proxy/`): Claude Code's LSP
  client never tells a server about files changed on disk ([claude-code#85225](https://github.com/anthropics/claude-code/issues/85225),
  open), so files created, changed or deleted by the shell, git or a generator stayed stale in the index and the servers
  pushed false errors ("cannot be resolved" for classes that compile). The launcher runs the server behind this stdio proxy:
  it passes every message through, advertises `workspace.didChangeWatchedFiles` on the client's behalf and answers the
  server's registration of it (Pyright drops file events without it), and polls the watched files once a second (about
  15 ms for the 1,100 Java files, 30 ms for the Python), sending the server what appeared, changed or disappeared. Tested
  outside Claude Code: Pyright clears and restores an import error within seconds of a shell edit.
- **One server per Claude Code session**, shared by its sub-agents: about 0.9 GB for Pyright.

### Rules for agents

In `briefs/common.md` (the steps' shared brief): read big READMEs with `tools/doc_section.py`; find Java and Python code
with grep (`git grep -n`), then read the whole members or sections they work with by line range, never a window around a
hit; edit with Claude Code's Edit/Write tools. Pyright's LSP diagnostics on `tools/` still arrive after edits; its "is
not accessed" hints are ignored.

### jCodeMunch (tried 2026-10-26 to 2026-11-13, dropped)

[jCodeMunch-MCP](https://github.com/jgravelle/jcodemunch-mcp) (J. Gravelle, a tree-sitter symbol index served as MCP
tools, free for non-commercial use only) replaced the language servers in the agents' brief for a trial, scoped to this
project (a local-scope MCP server, its own venv, the index in `build/code-index/`). The agents hardly used it (about
0.4 % of code lookups, see *Decisions*) and the user dropped it on 2026-11-13: the MCP server, its venv, its indexes and
`tools/dev/setup_jcodemunch.sh` are gone. Agents find code with grep and read it by line range.

## Implementation

- [x] `tools/dev/setup_lsp.sh` installs the pinned servers, the launcher and the plugins, and links the voice venvs.
- [x] Pyright runs behind `tools/dev/lsp_watch_proxy.py`, so files changed outside Claude Code's edit tools reach them.
- [x] `pyrightconfig.json` is tracked (the `.gitignore` keeps the Eclipse output lines for IDEs).
- [x] Eclipse JDT LS removed (2026-11-13, the user): the `jdtls-lsp` plugin uninstalled (user scope), its install,
  launcher and index cache deleted, `setup_lsp.sh` no longer installs it.
- [x] `tools/doc_section.py` reads Markdown by section (CLAUDE.md, *Tools*).
- [x] Token use of part H's steps compared with parts F and G; keep or drop the servers on the result. Measured
  2026-10-26: the servers were hardly used (16 LSP calls in part H's seven code steps, all `documentSymbol`; median
  0.08 s, none empty), while shell reads of Java stayed at about 470 calls. The code steps used 49.5M tokens each
  against 34–36M in parts E–G, but part H's work was larger, so the cost difference says nothing about the servers.
  Memory: jdtls 1.5–2.0 GB, Pyright 0.2–1.0 GB. Round 59's five steps made 4 LSP calls. Dropped from the agents'
  briefs (user, 2026-10-26: not effective; see *Decisions*).
- [x] jCodeMunch tried, scoped to this project (2026-10-26), and removed (2026-11-13, the user): the local-scope MCP
  server, its venv, its indexes (`build/code-index/`, `~/.code-index/`) and `tools/dev/setup_jcodemunch.sh` deleted.
- [x] Use of jCodeMunch measured (2026-11-13, about 0.4 % of code lookups); keep or drop: dropped.

## Decisions

- 2026-11-13 (user): jCodeMunch dropped after the measurement below: the MCP server removed from both checkouts' local
  scope, `~/.local/share/jcodemunch`, `build/code-index/` and `~/.code-index/` deleted, `tools/dev/setup_jcodemunch.sh`
  removed, the agents' common brief back to grep and line-range reads.
- 2026-11-13 (user): Eclipse JDT LS removed everywhere: the `jdtls-lsp` plugin uninstalled at user scope, `~/.local/share/jdtls`, `~/.local/bin/jdtls` and `~/.cache/jdtls-data` (2.8 GB) deleted, `setup_lsp.sh` installs only Pyright. Pyright stays. jCodeMunch measured for the user the same day: of 426 sub-agents since 2026-10-26, 161 called it, but 139 of its 308 calls were `resolve_repo`; about 170 real lookups (83 outlines, 41 `search_text`, 22 `get_symbol_source`, 21 `search_symbols`, 1 `find_references`) against about 30 400 shell greps, 14 900 `sed -n` reads and 2 950 Reads (about 0.4 % of code navigation); the user dropped it (the entry above).
- 2026-10-27: the refactor steps (RF0–RF6) were the jCodeMunch trial: `get_symbol_source` and regex `search_text` (the
  enclosing method per hit) helped; `find_references` follows imports only (same-package users missed), big outlines
  overflow to a file, grep stayed cheaper for counts and sweeps. The warning on every answer was the turn budget
  (above), shared by all agents: turned off (the user asked to check it).
- 2026-10-26: the language servers were measured over parts H and I: they answered fast and correctly but the agents
  hardly called them (16 and 4 calls) and kept reading Java with `sed -n`. The user does not want agents forced onto a
  tool, so they leave the briefs; jCodeMunch is tried instead (jDocMunch rejected: the agents already read docs by
  section, and the documentation cost comes from repeated reads and long runs, not from whole-file reads).

- 2026-10-25: the user asked for stable, free tooling with research behind it that fits work on one laptop with
  concurrent sub-agents. Chosen: language servers through Claude Code's LSP tool, jdtls first, then Pyright; rejected:
  vector/embedding indexes (research favours grep-style search on code), research code-graph prototypes (not stable,
  quality loss), Serena (overlaps, mostly blog evidence). The oversized READMEs were split in the same change (Decisions
  archives, rejected concept rows, the architecture schemas as child parts).
