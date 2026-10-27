---
title: Agent tooling
design: review
implementation: done
art: n/a
depends-on: [../architecture]
updated: 2026-10-27
---

# Agent tooling

## Summary

The tools Claude Code's agents use to read this repo cheaply: jCodeMunch's symbol index for Java and Python (a trial,
scoped to this project; set up with `tools/dev/setup_jcodemunch.sh`), and `tools/doc_section.py` for reading big
READMEs by section. The language servers (Eclipse JDT LS, Pyright, `tools/dev/setup_lsp.sh`) were tried first and are no
longer in the agents' brief. None of it is needed to build or play the game.

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

1. Prerequisites: a JDK 21+ (the build's own is fine), Node.js with npm, Python 3, curl, and `~/.local/bin` on `PATH`.
   Linux or macOS (Windows: WSL).
2. Run `tools/dev/setup_lsp.sh` (safe to rerun; `--check` only reports). It installs, at pinned versions:
   - **Eclipse JDT LS 1.61.0** (EPL-2.0) into `~/.local/share/jdtls/1.61.0`, checksum-verified, and writes the launcher
     `~/.local/bin/jdtls`;
   - **Pyright 1.1.414** (MIT) into `~/.local/share/pyright/1.1.414` (npm, install scripts off), linked as
     `~/.local/bin/pyright-langserver`;
   - the official Claude Code plugins `jdtls-lsp` and `pyright-lsp` (user scope, from `claude-plugins-official`);
   - links from `build/pyright-venvs/{chatterbox,dia}` to the voice TTS venvs' `site-packages` in `~/.cache/tv-tts/`, when
     this machine has them (see [voice](../../audio/voice/README.md)), so Pyright resolves the voice tools' imports.
3. Run `/reload-plugins` in Claude Code (or restart it). Check with the LSP tool, e.g. `workspaceSymbol` for `HeatPlumes`.

### How it is configured

- **The jdtls launcher** keeps one index per project directory in `~/.cache/jdtls-data/` (about 64 MB here); a second
  instance on the same project (another session) gets a throw-away index instead of fighting over the workspace lock. It
  runs on Java 21 with at most 2 GB and keeps Eclipse's metadata in the index
  (`-Djava.import.generatesMetadataFilesAtProjectRoot=false`), not as `.project`/`.classpath` files in the repo. It still
  compiles into `<module>/bin/`, which `.gitignore` ignores with the Eclipse files. Its Gradle import starts a Gradle
  daemon with the agents' 10-minute idle timeout. The first start imports the build in about 30 s.
- **`pyrightconfig.json`** (tracked): only `tools/`, type checking off (the scripts are untyped numpy/PIL code: only
  syntax errors and unresolved imports are reported), the tool folders as import roots (the scripts import each other by
  folder), the voice venv links.
- **The watch proxy** (`tools/dev/lsp_watch_proxy.py`, installed to `~/.local/share/lsp-watch-proxy/`): Claude Code's LSP
  client never tells a server about files changed on disk ([claude-code#85225](https://github.com/anthropics/claude-code/issues/85225),
  open), so files created, changed or deleted by the shell, git or a generator stayed stale in the index and the servers
  pushed false errors ("cannot be resolved" for classes that compile). Both launchers run the server behind this stdio proxy:
  it passes every message through, advertises `workspace.didChangeWatchedFiles` on the client's behalf and answers the
  server's registration of it (Pyright drops file events without it), and polls the watched files once a second (about
  15 ms for the 1,100 Java files, 30 ms for the Python), sending the server what appeared, changed or disappeared. Tested
  outside Claude Code: a class created through the shell is found by jdtls within 4 s and gone after its deletion; Pyright
  clears and restores an import error the same way.
- **One server per Claude Code session**, shared by its sub-agents: about 1.7 GB for jdtls and 0.9 GB for Pyright.

### Rules for agents

In `briefs/common.md` (the steps' shared brief): find Java and Python code with the LSP tool (`workspaceSymbol`,
`documentSymbol`, `findReferences`, `incomingCalls`, `goToDefinition`, `hover`), then read the whole members or sections
they work with by line range, never a window around a hit; read big READMEs with `tools/doc_section.py`; edit Java and
Python with Claude Code's Edit/Write tools. Known limits:

- jdtls works from disk only (`--disk-only`): files edited with Claude Code's Edit/Write tools fell out of its index
  through the client's document sync ([claude-code#76870](https://github.com/anthropics/claude-code/issues/76870)), so the
  proxy drops that sync and the watcher reports every change within about 2 s, whatever made it (verified in Claude Code
  2026-10-26: a class written with the Write tool is found by `workspaceSymbol`, and gone after its deletion). Pyright keeps the
  document sync (it reports diagnostics only for open files).
- jdtls reports Eclipse-compiler warnings `./gradlew` does not (resource leak, missing switch case, unused); Pyright adds
  "is not accessed" hints. Both are ignored; the Gradle build is the judge.
- Pyright's `documentSymbol` lists local variables too; the module's API is its unindented names.

### jCodeMunch (trial since 2026-10-26)

[jCodeMunch-MCP](https://github.com/jgravelle/jcodemunch-mcp) (J. Gravelle) parses the repo with tree-sitter into a symbol
index and serves it as MCP tools: `search_symbols`, `get_file_outline`, `get_symbol_source` (one member's exact source),
`find_references`, `search_text`. It replaces the language servers in the agents' brief: the agents never took to the
LSP's two-step lookup (find a range, then read it), and fetching one member in one call competes with `sed -n`.

- **Licence:** free for non-commercial use only (its LICENSE, v1.2 of 2026-10-21; commercial use needs a paid licence);
  not open source. It is never vendored: the setup script installs it from PyPI, pinned.
- **Scoped to this project** (the machine is also used for commercial work): no `jcodemunch-mcp init`, nothing in
  `~/.claude/CLAUDE.md` or the user settings; its own venv in `~/.local/share/jcodemunch/<version>`; the index and its
  `config.jsonc` in `build/code-index/` (`CODE_INDEX_PATH`, about 55 MB, 15 s to build); the MCP server in Claude Code's
  local scope (`claude mcp add --scope local`), so sessions in other directories never see it.
- **Settings:** the anonymous savings counter off (`share_savings`, `JCODEMUNCH_SHARE_SAVINGS=0`), AI summaries off,
  direct tools (`tool_surface: full`, `tool_profile: core`, 20 tools) instead of the default three-verb front door,
  code only (`extra_ignore_patterns`: the design tree, assets, data and Markdown are read with `doc_section.py`/grep).
  The **turn budget is off** (`turn_budget_tokens: 0`): it is an advisory per-turn counter (20 000 tokens, a new
  turn after 30 s of silence) kept once per server process, so parallel sub-agents on one session's server never let it
  reset and every answer carried "Turn budget exhausted … consider stopping exploration". `stats_file_interval: 0`
  stops `session_stats.json`; jCodeMunch writes it and `_savings.json` (local token counts, never sent with
  `share_savings` off) to `~/.code-index/` whatever `CODE_INDEX_PATH` says, and `_savings.json` has no switch.
- **Freshness:** the server runs its file watcher (`serve --watcher`, the `[watch]` extra), so edits by any tool reach
  the index within seconds.
- Setup: `tools/dev/setup_jcodemunch.sh` (`--check` only reports), then `/mcp` (or restart Claude Code).

## Implementation

- [x] `tools/dev/setup_lsp.sh` installs the pinned servers, the launcher and the plugins, and links the voice venvs.
- [x] Both servers run behind `tools/dev/lsp_watch_proxy.py`, so files changed outside Claude Code's edit tools reach them.
- [x] `pyrightconfig.json` and the `.gitignore` lines for jdtls's output are tracked.
- [x] `tools/doc_section.py` reads Markdown by section (CLAUDE.md, *Tools*).
- [x] Token use of part H's steps compared with parts F and G; keep or drop the servers on the result. Measured
  2026-10-26: the servers were hardly used (16 LSP calls in part H's seven code steps, all `documentSymbol`; median
  0.08 s, none empty), while shell reads of Java stayed at about 470 calls. The code steps used 49.5M tokens each
  against 34–36M in parts E–G, but part H's work was larger, so the cost difference says nothing about the servers.
  Memory: jdtls 1.5–2.0 GB, Pyright 0.2–1.0 GB. Round 59's five steps made 4 LSP calls. Dropped from the agents'
  briefs (user, 2026-10-26: not effective; see *Decisions*).
- [x] `tools/dev/setup_jcodemunch.sh` installs jCodeMunch pinned, scoped to this project (see *jCodeMunch*).
- [ ] Token use of the next part's steps with jCodeMunch compared with parts E–I (`briefs/tokmeter.py`); keep or drop.

## Decisions

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
