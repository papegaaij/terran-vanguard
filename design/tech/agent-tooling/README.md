---
title: Agent tooling
design: review
implementation: done
art: n/a
depends-on: [../architecture]
updated: 2026-10-25
---

# Agent tooling

## Summary

The tools Claude Code's agents use to read this repo cheaply: language servers for Java (Eclipse JDT LS) and Python
(Pyright) behind Claude Code's LSP tool, and `tools/doc_section.py` for reading big READMEs by section. Set up on a new
machine with `tools/dev/setup_lsp.sh`. None of it is needed to build or play the game.

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
- **One server per Claude Code session**, shared by its sub-agents: about 1.7 GB for jdtls and 0.9 GB for Pyright.

### Rules for agents

In `briefs/common.md` (the steps' shared brief): find Java and Python code with the LSP tool (`workspaceSymbol`,
`documentSymbol`, `findReferences`, `incomingCalls`, `goToDefinition`, `hover`), then read the whole members or sections
they work with by line range, never a window around a hit; read big READMEs with `tools/doc_section.py`; edit Java and
Python with Claude Code's Edit/Write tools. Known limits:

- The servers only see changes made with Edit/Write: files changed by the shell, git or a generator stay stale in the
  index (a deleted class still shows up) until the server restarts.
- jdtls reports Eclipse-compiler warnings `./gradlew` does not (resource leak, missing switch case, unused); Pyright adds
  "is not accessed" hints. Both are ignored; the Gradle build is the judge.
- Pyright's `documentSymbol` lists local variables too; the module's API is its unindented names.

## Implementation

- [x] `tools/dev/setup_lsp.sh` installs the pinned servers, the launcher and the plugins, and links the voice venvs.
- [x] `pyrightconfig.json` and the `.gitignore` lines for jdtls's output are tracked.
- [x] `tools/doc_section.py` reads Markdown by section (CLAUDE.md, *Tools*).
- [ ] Token use of part H's steps compared with parts F and G; keep or drop the servers on the result.

## Decisions

- 2026-10-25: the user asked for stable, free tooling with research behind it that fits work on one laptop with
  concurrent sub-agents. Chosen: language servers through Claude Code's LSP tool, jdtls first, then Pyright; rejected:
  vector/embedding indexes (research favours grep-style search on code), research code-graph prototypes (not stable,
  quality loss), Serena (overlaps, mostly blog evidence). The oversized READMEs were split in the same change (Decisions
  archives, rejected concept rows, the architecture schemas as child parts).
