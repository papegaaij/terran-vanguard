#!/usr/bin/env python3
"""A stdio proxy between Claude Code and a language server that tells the server about files changed on disk.

Claude Code's LSP client never sends `workspace/didChangeWatchedFiles` (anthropics/claude-code#85225), so a server never
learns about files created, changed or deleted by the shell, git or a generator: its index goes stale and it reports false
errors. This proxy passes every message through unchanged and, once the session is initialized, polls the files matching
the given patterns under the workspace root and sends the server a `workspace/didChangeWatchedFiles` notification for
every file that appeared, changed or disappeared (polling: no dependencies, works on Linux and macOS). It advertises
`workspace.didChangeWatchedFiles` (dynamic registration) on the client's behalf and answers the server's registration of
it itself: Pyright drops file events unless that registration succeeded. With `--disk-only` it also drops the client's
document sync, so the server works from disk alone: Claude Code's edit tools write to disk at once, and its document sync
leaves edited files out of jdtls's index (anthropics/claude-code#76870).

  lsp_watch_proxy.py --watch '*.java' --watch 'build.gradle.kts' [--root-dir src] [--interval 1.0] -- SERVER ARGS...

`--watch` takes file-name globs (several allowed); `--root-dir` limits the scan to directories with that name below the root
(plus the root's own files), default: the whole root. Directories named build, bin, .git, .gradle, node_modules and
__pycache__ are never scanned. LSP_WATCH_PROXY_LOG=<file> logs the registrations and the events it sends. Used by
tools/dev/setup_lsp.sh (design/tech/agent-tooling).
"""
import argparse
import fnmatch
import json
import os
import subprocess
import sys
import threading
import time
from urllib.parse import unquote, urlparse
from pathlib import Path

SKIP = {"build", "bin", ".git", ".gradle", "node_modules", "__pycache__", ".claude"}
CREATED, CHANGED, DELETED = 1, 2, 3


def read_message(stream):
    """One LSP message (headers + body) as raw bytes and its parsed body, or (None, None) at end of stream."""
    headers = b""
    while not headers.endswith(b"\r\n\r\n"):
        ch = stream.read(1)
        if not ch:
            return None, None
        headers += ch
    length = 0
    for line in headers.decode("ascii", "replace").split("\r\n"):
        if line.lower().startswith("content-length:"):
            length = int(line.split(":", 1)[1])
    body = stream.read(length)
    try:
        return headers + body, json.loads(body)
    except ValueError:
        return headers + body, None


LOG = os.environ.get("LSP_WATCH_PROXY_LOG")


def log(text):
    if LOG:
        with open(LOG, "a") as f:
            f.write(time.strftime("%H:%M:%S ") + text + "\n")


def frame(obj):
    body = json.dumps(obj).encode()
    return b"Content-Length: %d\r\n\r\n" % len(body) + body


class Watcher:
    def __init__(self, root, patterns, root_dirs, interval, send):
        self.root, self.patterns, self.root_dirs, self.interval, self.send = root, patterns, root_dirs, interval, send
        self.state = {}

    def matches(self, name):
        return any(fnmatch.fnmatch(name, p) for p in self.patterns)

    def scan(self):
        found = {}

        def walk(directory, inside):
            try:
                entries = list(os.scandir(directory))
            except OSError:
                return
            for e in entries:
                if e.is_dir(follow_symlinks=False):
                    if e.name not in SKIP:
                        walk(e.path, inside or not self.root_dirs or e.name in self.root_dirs)
                elif inside and self.matches(e.name):
                    try:
                        st = e.stat()
                        found[e.path] = (st.st_mtime_ns, st.st_size)
                    except OSError:
                        pass

        for e in os.scandir(self.root):  # the root's own files always count (build files, configs)
            if e.is_file() and self.matches(e.name):
                st = e.stat()
                found[e.path] = (st.st_mtime_ns, st.st_size)
        walk(self.root, not self.root_dirs)
        return found

    def run(self):
        self.state = self.scan()
        while True:
            time.sleep(self.interval)
            now = self.scan()
            changes = [{"uri": Path(p).as_uri(), "type": CREATED} for p in now.keys() - self.state.keys()]
            changes += [{"uri": Path(p).as_uri(), "type": DELETED} for p in self.state.keys() - now.keys()]
            changes += [{"uri": Path(p).as_uri(), "type": CHANGED}
                        for p in now.keys() & self.state.keys() if now[p] != self.state[p]]
            self.state = now
            if changes:
                log(f"didChangeWatchedFiles: {changes}")
                self.send({"jsonrpc": "2.0", "method": "workspace/didChangeWatchedFiles", "params": {"changes": changes}})


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--watch", action="append", required=True)
    ap.add_argument("--root-dir", action="append", default=[])
    ap.add_argument("--interval", type=float, default=1.0)
    ap.add_argument("--disk-only", action="store_true",
                    help="drop the client's document sync (didOpen/didChange/didSave/didClose): the server reads every "
                         "file from disk and learns of changes from the watcher only")
    ap.add_argument("server", nargs=argparse.REMAINDER)
    a = ap.parse_args()
    cmd = a.server[1:] if a.server[:1] == ["--"] else a.server
    server = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    lock = threading.Lock()

    def to_server(raw):
        with lock:
            server.stdin.write(raw)
            server.stdin.flush()

    out = sys.stdout.buffer

    def from_server():
        # Pass everything through, but answer the server's own registration of watched files (we advertised the
        # capability on the client's behalf; Pyright ignores file events otherwise) instead of forwarding it.
        while True:
            raw, msg = read_message(server.stdout)
            if raw is None:
                break
            if isinstance(msg, dict) and msg.get("method") in ("client/registerCapability", "client/unregisterCapability"):
                key = "registrations" if msg["method"] == "client/registerCapability" else "unregisterations"
                regs = (msg.get("params") or {}).get(key) or []
                rest = [r for r in regs if r.get("method") != "workspace/didChangeWatchedFiles"]
                log(f"{msg['method']}: {json.dumps(regs)[:600]}")
                if len(rest) < len(regs):
                    if not rest:
                        to_server(frame({"jsonrpc": "2.0", "id": msg["id"], "result": None}))
                        continue
                    msg["params"][key] = rest
                    raw = frame(msg)
            out.write(raw)
            out.flush()
        # the server may exit on the client's `exit` while the client keeps our stdin open: leave with it
        os._exit(server.wait())

    threading.Thread(target=from_server, daemon=True).start()

    root = os.getcwd()
    started = False
    stdin = sys.stdin.buffer
    try:
        while True:
            raw, msg = read_message(stdin)
            if raw is None:
                break
            if isinstance(msg, dict) and msg.get("method") == "initialize":
                params = msg.get("params") or {}
                uri = params.get("rootUri") or next(iter(params.get("workspaceFolders") or []), {}).get("uri")
                if uri:
                    root = unquote(urlparse(uri).path)
                caps = params.setdefault("capabilities", {}).setdefault("workspace", {})
                caps["didChangeWatchedFiles"] = {"dynamicRegistration": True}
                msg["params"] = params
                raw = frame(msg)
            if a.disk_only and isinstance(msg, dict) and msg.get("method", "").startswith("textDocument/did"):
                log(f"dropped {msg['method']} {(msg.get('params') or {}).get('textDocument', {}).get('uri', '')}")
                continue
            to_server(raw)
            if isinstance(msg, dict) and msg.get("method") == "initialized" and not started:
                started = True
                w = Watcher(root, a.watch, set(a.root_dir), a.interval, lambda o: to_server(frame(o)))
                threading.Thread(target=w.run, daemon=True).start()
    except (BrokenPipeError, OSError):
        pass
    try:
        server.stdin.close()
    except OSError:
        pass
    sys.exit(server.wait())


if __name__ == "__main__":
    main()
