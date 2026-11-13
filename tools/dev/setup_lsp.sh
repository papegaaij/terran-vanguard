#!/usr/bin/env bash
# Sets up the language servers Claude Code's agents use to navigate this repo (design/tech/agent-tooling):
# Pyright for Python, at a pinned version, plus its official Claude Code plugin. (Eclipse JDT LS for Java was dropped on
# 2026-11-13: the agents hardly used it and it took 1.5-2 GB a session.)
#
#   tools/dev/setup_lsp.sh            install what is missing (safe to rerun)
#   tools/dev/setup_lsp.sh --check    only report what is installed
#
# Installs into ~/.local/share/pyright/<version> and ~/.local/bin (must be on PATH). Needs: python3, Node.js with npm
# (runs Pyright). Linux and macOS; on Windows use WSL. Run /reload-plugins (or restart Claude Code) afterwards.
set -euo pipefail

PYRIGHT_VERSION=1.1.414

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
SHARE="$HOME/.local/share"
BIN="$HOME/.local/bin"
CHECK=false
[[ "${1:-}" == "--check" ]] && CHECK=true

say() { printf '%s\n' "$*"; }
have() { command -v "$1" >/dev/null 2>&1; }

# Claude Code never tells a language server about files changed on disk (anthropics/claude-code#85225): the server runs
# behind tools/dev/lsp_watch_proxy.py, which polls the files and sends the server workspace/didChangeWatchedFiles.
PROXY="$SHARE/lsp-watch-proxy/lsp_watch_proxy.py"
install_proxy() {
    if $CHECK; then
        if cmp -s "$ROOT/tools/dev/lsp_watch_proxy.py" "$PROXY"; then say "watch proxy: installed ($PROXY)"
        else say "watch proxy: MISSING or outdated"; fi
        return
    fi
    mkdir -p "$(dirname "$PROXY")"
    cp "$ROOT/tools/dev/lsp_watch_proxy.py" "$PROXY"
    say "watch proxy: $PROXY"
}

install_pyright() {
    local dir="$SHARE/pyright/$PYRIGHT_VERSION"
    if [[ -x "$dir/node_modules/.bin/pyright-langserver" ]]; then say "pyright $PYRIGHT_VERSION: installed ($dir)"; else
        $CHECK && { say "pyright $PYRIGHT_VERSION: MISSING"; return; }
        have npm || { say "pyright: npm not found (install Node.js)"; exit 1; }
        npm install --prefix "$dir" --ignore-scripts --no-audit --no-fund "pyright@$PYRIGHT_VERSION" >/dev/null
        say "pyright $PYRIGHT_VERSION: installed into $dir"
    fi
    $CHECK && return
    mkdir -p "$BIN"
    rm -f "$BIN/pyright-langserver"
    printf '%s\n' '#!/usr/bin/env bash' \
        '# Written by tools/dev/setup_lsp.sh (terran-vanguard): Pyright for the pyright-lsp plugin, behind the watch proxy.' \
        "exec python3 \"$PROXY\" --watch '*.py' --watch 'pyrightconfig.json' -- \"$dir/node_modules/.bin/pyright-langserver\" \"\$@\"" \
        > "$BIN/pyright-langserver"
    chmod +x "$BIN/pyright-langserver"
    ln -sf "$dir/node_modules/.bin/pyright" "$BIN/pyright"
}

# pyrightconfig.json (tracked) reads the voice tools' heavy packages (torch, chatterbox, faster-whisper) from
# build/pyright-venvs/<name>: link the TTS venvs' site-packages there when this machine has them.
link_voice_venvs() {
    local v sp
    for v in chatterbox dia; do
        sp=$(ls -d "$HOME/.cache/tv-tts/venv-$v"/lib/python*/site-packages 2>/dev/null | head -1 || true)
        if [[ -n "$sp" ]]; then
            $CHECK || { mkdir -p "$ROOT/build/pyright-venvs"; ln -sfn "$sp" "$ROOT/build/pyright-venvs/$v"; }
            say "voice venv $v: linked for pyright"
        else
            say "voice venv $v: not on this machine (pyright warns on the voice tools' imports; harmless)"
        fi
    done
}

install_plugins() {
    have claude || { say "claude CLI not found: install the plugins by hand (see design/tech/agent-tooling)"; return; }
    local p
    for p in pyright-lsp; do
        if claude plugin list 2>/dev/null | grep -q "$p@claude-plugins-official"; then say "plugin $p: installed"
        elif $CHECK; then say "plugin $p: MISSING"
        else claude plugin install "$p@claude-plugins-official" >/dev/null && say "plugin $p: installed"; fi
    done
}

install_proxy
install_pyright
link_voice_venvs
install_plugins
case ":$PATH:" in *":$BIN:"*) ;; *) say "note: add $BIN to PATH";; esac
$CHECK || say "done: run /reload-plugins in Claude Code (or restart it)"
