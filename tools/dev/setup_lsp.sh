#!/usr/bin/env bash
# Sets up the language servers Claude Code's agents use to navigate this repo (design/tech/agent-tooling):
# Eclipse JDT LS for Java and Pyright for Python, at pinned versions, plus the two official Claude Code plugins.
#
#   tools/dev/setup_lsp.sh            install what is missing (safe to rerun)
#   tools/dev/setup_lsp.sh --check    only report what is installed
#
# Installs into ~/.local/share/{jdtls,pyright}/<version> and ~/.local/bin (must be on PATH). Needs: curl, tar,
# sha256sum (or shasum), python3, a JDK 21+ (runs jdtls; the build's own JDK is fine), Node.js with npm (runs
# Pyright). Linux and macOS; on Windows use WSL. Run /reload-plugins (or restart Claude Code) afterwards.
set -euo pipefail

JDTLS_VERSION=1.61.0
JDTLS_BUILD=202609031315
PYRIGHT_VERSION=1.1.414

ROOT=$(cd "$(dirname "$0")/../.." && pwd)
SHARE="$HOME/.local/share"
BIN="$HOME/.local/bin"
CHECK=false
[[ "${1:-}" == "--check" ]] && CHECK=true

say() { printf '%s\n' "$*"; }
have() { command -v "$1" >/dev/null 2>&1; }

java_major() { "$1" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1; }

find_jdk21() {
    local c
    for c in "${JDTLS_JAVA_HOME:-}" "${JAVA_HOME:-}" /usr/lib/jvm/java-21-openjdk-* /usr/lib/jvm/java-2[2-9]-openjdk-* \
             /usr/lib/jvm/java-[3-9][0-9]-openjdk-*; do
        [[ -n "$c" && -x "$c/bin/java" ]] || continue
        local m; m=$(java_major "$c/bin/java")
        [[ -n "$m" && "$m" -ge 21 ]] && { echo "$c"; return; }
    done
    if [[ "$(uname)" == Darwin ]] && /usr/libexec/java_home -v 21+ >/dev/null 2>&1; then
        /usr/libexec/java_home -v 21+; return
    fi
    return 1
}

sha256() { if have sha256sum; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi; }

install_jdtls() {
    local dir="$SHARE/jdtls/$JDTLS_VERSION"
    if [[ -x "$dir/bin/jdtls" ]]; then say "jdtls $JDTLS_VERSION: installed ($dir)"; else
        $CHECK && { say "jdtls $JDTLS_VERSION: MISSING"; return; }
        local f="jdt-language-server-$JDTLS_VERSION-$JDTLS_BUILD.tar.gz"
        local url="https://download.eclipse.org/jdtls/milestones/$JDTLS_VERSION/$f"
        local tmp; tmp=$(mktemp -d)
        curl -fsSL -o "$tmp/$f" "$url"
        curl -fsSL -o "$tmp/$f.sha256" "$url.sha256"
        [[ "$(sha256 "$tmp/$f")" == "$(cut -d' ' -f1 < "$tmp/$f.sha256")" ]] || { say "jdtls: checksum mismatch"; exit 1; }
        mkdir -p "$dir"
        tar -xzf "$tmp/$f" -C "$dir"
        rm -rf "$tmp"
        say "jdtls $JDTLS_VERSION: installed into $dir"
    fi
    local jdk; jdk=$(find_jdk21) || { say "jdtls: no JDK 21+ found (set JDTLS_JAVA_HOME)"; exit 1; }
    $CHECK && { say "jdtls launcher: $(command -v jdtls || echo MISSING) (JDK $jdk)"; return; }
    mkdir -p "$BIN"
    # The launcher: one index per project directory in ~/.cache/jdtls-data; a second instance on the same project
    # (another Claude Code session) gets a throw-away index instead of fighting over the workspace lock. Eclipse
    # metadata stays in the index (not .project/.classpath in the repo); jdtls's Gradle import gets the same 10-minute
    # daemon idle timeout as the agents' builds.
    cat > "$BIN/jdtls" <<EOF
#!/usr/bin/env bash
# Written by tools/dev/setup_lsp.sh (terran-vanguard). Eclipse JDT LS for Claude Code's jdtls-lsp plugin.
JDTLS_HOME="$dir"
export JAVA_HOME="$jdk"
key=\$(printf '%s' "\$PWD" | (sha1sum 2>/dev/null || shasum) | cut -c1-12)
base="\$HOME/.cache/jdtls-data"
mkdir -p "\$base"
args=(--jvm-arg=-Xmx2G --jvm-arg=-Djava.import.generatesMetadataFilesAtProjectRoot=false
      --jvm-arg=-Dorg.gradle.daemon.idletimeout=600000)
exec 9>"\$base/\$key.lock"
if flock -n 9 2>/dev/null; then
    exec python3 "\$JDTLS_HOME/bin/jdtls" "\${args[@]}" -data "\$base/\$key" "\$@"
fi
exec 9>&-
tmp=\$(mktemp -d "\$base/\$key-pid\$\$.XXXX")
trap 'rm -rf "\$tmp"' EXIT
python3 "\$JDTLS_HOME/bin/jdtls" "\${args[@]}" -data "\$tmp" "\$@"
EOF
    chmod +x "$BIN/jdtls"
    have flock || say "note: no flock (macOS): every jdtls instance shares one index; avoid two sessions on the repo"
    say "jdtls launcher: $BIN/jdtls (JDK $jdk)"
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
    ln -sf "$dir/node_modules/.bin/pyright-langserver" "$BIN/pyright-langserver"
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
    for p in jdtls-lsp pyright-lsp; do
        if claude plugin list 2>/dev/null | grep -q "$p@claude-plugins-official"; then say "plugin $p: installed"
        elif $CHECK; then say "plugin $p: MISSING"
        else claude plugin install "$p@claude-plugins-official" >/dev/null && say "plugin $p: installed"; fi
    done
}

install_jdtls
install_pyright
link_voice_venvs
install_plugins
case ":$PATH:" in *":$BIN:"*) ;; *) say "note: add $BIN to PATH";; esac
$CHECK || say "done: run /reload-plugins in Claude Code (or restart it)"
