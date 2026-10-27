#!/usr/bin/env bash
# Sets up jCodeMunch, the code symbol index Claude Code's agents use to read this repo's Java and Python
# (design/tech/agent-tooling), scoped to this project only.
#
#   tools/dev/setup_jcodemunch.sh            install and register (safe to rerun)
#   tools/dev/setup_jcodemunch.sh --check    only report
#
# jCodeMunch-MCP is free for non-commercial use only (its LICENSE; commercial use needs a paid licence), so nothing
# here touches global config: no `jcodemunch-mcp init`, no policy in ~/.claude/CLAUDE.md, no user-scope hooks. The
# package goes into its own venv under ~/.local/share/jcodemunch/<version>, the index and its config into
# build/code-index/ (CODE_INDEX_PATH), and the MCP server is registered in Claude Code's *local* scope (this project
# directory only). The anonymous savings counter and AI summaries are off. Needs python3 (3.10+) and the claude CLI.
set -euo pipefail

VERSION=1.108.397

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
DIR="$HOME/.local/share/jcodemunch/$VERSION"
BIN="$DIR/bin/jcodemunch-mcp"
export CODE_INDEX_PATH="$ROOT/build/code-index"
export JCODEMUNCH_SHARE_SAVINGS=0
CHECK=false
[[ "${1:-}" == "--check" ]] && CHECK=true

say() { printf '%s\n' "$*"; }

if [[ -x "$BIN" ]]; then say "jcodemunch $VERSION: installed ($DIR)"; else
    $CHECK && { say "jcodemunch $VERSION: MISSING"; exit 0; }
    python3 -m venv "$DIR"
    "$DIR/bin/pip" install -q --disable-pip-version-check "jcodemunch-mcp[watch]==$VERSION"
    say "jcodemunch $VERSION: installed into $DIR"
fi

if ! $CHECK; then
    # Code only: the design tree, assets and data files are read with tools/doc_section.py and grep.
    "$BIN" config set share_savings false >/dev/null
    "$BIN" config set use_ai_summaries false >/dev/null
    "$BIN" config set surface_offer_seen true >/dev/null
    "$BIN" config set tool_surface full >/dev/null
    "$BIN" config set tool_profile core >/dev/null
    # The turn budget is a process-wide advisory counter; with parallel sub-agents on one server it never resets
    # and every answer says "Turn budget exhausted ... stop exploring". Off.
    "$BIN" config set turn_budget_tokens 0 >/dev/null
    # session_stats.json goes to ~/.code-index whatever CODE_INDEX_PATH says (as does _savings.json, which has no
    # switch): keep the home-directory writes to that one small file.
    "$BIN" config set stats_file_interval 0 >/dev/null
    "$BIN" config set extra_ignore_patterns \
        '["assets/","design/","briefs/","build/","**/bin/","*.md","*.yaml","*.yml","*.json","*.jsonc","*.txt"]' >/dev/null
    (cd "$ROOT" && "$BIN" index "$ROOT" --no-ai-summaries >/dev/null)
fi
(cd "$ROOT" && "$BIN" list-repos 2>/dev/null | head -1) || true

if command -v claude >/dev/null 2>&1; then
    if (cd "$ROOT" && claude mcp get jcodemunch >/dev/null 2>&1); then say "MCP server jcodemunch: registered (local scope)"
    elif $CHECK; then say "MCP server jcodemunch: MISSING"
    else
        (cd "$ROOT" && claude mcp add --scope local jcodemunch -e CODE_INDEX_PATH="$CODE_INDEX_PATH" \
            -e JCODEMUNCH_SHARE_SAVINGS=0 -- "$BIN" serve --watcher --watcher-path "$ROOT" >/dev/null)
        say "MCP server jcodemunch: registered (local scope); reconnect it with /mcp or restart Claude Code"
    fi
else
    say "claude CLI not found: register the MCP server by hand (see design/tech/agent-tooling)"
fi
