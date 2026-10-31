#!/usr/bin/env python3
"""Concept round 73 (M10 "Act 6 - Jovian Storm", step m10-f72, 2026-10-31): round 72's voice follow-ups; pending the
user's review. Built on tools/concept/audio/voice_r72.py (its worker, read-back and pin machinery, imported and
pointed at this round's takes).

Outputs:
  design/audio/voice/concept/voice-aurelia-traffic-r73-b.ogg: Aurelia Traffic (reader b, Lizzie Driver, chosen in
      round 72 V.3 b) with her t=1 call (round 72's pinned take, unchanged) and the t=150 sign-off "The lift rings are
      failing. Aurelia is falling. Aurelia Traffic, signing off." re-rendered on more seeds (the grim, slower row,
      same clip) until Whisper reads back "lift", not "Liff" (round 72's pin, seed 103); radio filter b, 1 s apart.
  design/audio/voice/concept/vorne-shout-r73-<a|b>.ogg: Vorne's Level 42 line 5 "No! We had an agreement!" shouted
      in his low register (round 72 V.5: a at 265 Hz and b at 156 Hz were rejected):
      a round 72's shout-row take (seed 101, refs/ref-vorne.wav), cut at the end of its shouted "-ment!", pitched down
        to about TARGET_HZ by WORLD (pyworld: the f0 scaled, the spectral envelope - the formants - and the timing,
        so the break after "No!", untouched); ffmpeg's rubberband with formants preserved smeared the formants at
        this depth (-15 semitones: the centroid -58 %, Whisper heard no words) and is used only for the clip below;
      b new shout takes cloned from a lower cut of his clip (refs/ref-vorne.wav a further LOW_SHIFT semitones down,
        formants preserved; written to the raw cache, not to refs/) at the shout row and a stronger one, seeds
        SEEDS_VORNE; the pin is the take Whisper hears shouted (both "!") with the brightest spectrum (the shout's
        strain: the share of 1-4 kHz energy), then lowered by WORLD as a when it sits above 130 Hz.
      Both through the enemy filter (tools/art/voice.py enemy(), -16 LUFS, as his other lines).

Setup and venvs as voice_r72.py (Chatterbox on the GPU; Whisper and librosa from the Dia venv); ffmpeg with
librubberband; WORLD in its own venv (~/.cache/tv-tts/venv-world: `uv venv -p 3.11` and `uv pip install pyworld numpy
soundfile "setuptools<81"`, as tools/concept/audio's other venvs).
Rerun: python3 tools/concept/audio/voice_r73.py           (lower the clip, generate, read back, pin, post-process)
       python3 tools/concept/audio/voice_r73.py --post    (read back, pin and post-process only)
Raw WAVs and the read-back go to ~/.cache/tv-tts/raw/r73/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import json
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import voice_r72 as v72  # noqa: E402

ROOT = v72.ROOT
DEST = v72.DEST
RAW = v72.BASE / "raw" / "r73"
RAW72 = v72.BASE / "raw" / "r72"
READBACK = RAW / "readback.json"
READBACK72 = v72.READBACK
LOW_CLIP = RAW / "ref-vorne-low.wav"
LOW_SHIFT = -4.0            # semitones under refs/ref-vorne.wav (itself round 60's clip -2)
TARGET_HZ = 110.0           # the lowered shouts' median (the brief: 100-120 Hz, his register 90-106 Hz)

AURELIA_LINE = v72.LINES["aurelia-traffic"][1][0]
SHOUT_LINE = "No! We had an agreement!"
SHOUT_STRONG = {"exaggeration": 1.7, "cfg_weight": 0.15, "temperature": 0.8}
SEEDS_AURELIA = tuple(range(104, 120))
SEEDS_VORNE = tuple(range(100, 110))
# Aurelia's respelling fallback (only used when no seed of the plain text reads back "lift"; not needed)
RESPELL = None
# The sign-off's pin, by hand over pin_aurelia()'s pick (seed 110): of the eleven takes Whisper reads back as "lift",
# 117 shows the /t/ plainest in a zoom of the word (3-12 kHz in 5 ms frames): the /f/'s frication, a 35 ms closure
# 23 dB under it, then a release burst 4-6 dB above the frication before the /r/; seed 110's burst is weak (10 dB
# over its dip, under the frication) and round 72's seed 103 ("Liff") runs the frication into the /r/ with no gap.
AURELIA_PIN = 117
VORNE_B_PIN = None          # None: the brightest shouted take
WORLD_PY = v72.BASE / "venv-world" / "bin" / "python"   # pyworld 0.3.5 (+ numpy, soundfile, setuptools<81)


def jobs():
    """(job id, clip, text, settings) for every take of this round."""
    out = []
    text = RESPELL or AURELIA_LINE
    for seed in SEEDS_AURELIA:
        out.append((f"aurelia-b-2-s{seed}", v72.ref("aurelia-traffic", "b"), text, v72.GRIM_SLOW))
    for row, settings in (("shout", v72.VORNE_SHOUT), ("strong", SHOUT_STRONG)):
        for seed in SEEDS_VORNE:
            out.append((f"vorne-low-{row}-s{seed}", LOW_CLIP, SHOUT_LINE, settings))
    return out


def patch():
    """Point voice_r72's worker and read-back at this round's takes."""
    v72.RAW = RAW
    v72.READBACK = READBACK
    v72.jobs = jobs


def low_clip():
    RAW.mkdir(parents=True, exist_ok=True)
    if not LOW_CLIP.exists():
        rubberband(v72.REFDIR / "ref-vorne.wav", LOW_CLIP, 2 ** (LOW_SHIFT / 12))


def rubberband(src, dst, factor):
    subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-i", str(src), "-af",
                    f"rubberband=pitch={factor:.6f}:formant=preserved:pitchq=quality:window=long",
                    "-ac", "1", "-ar", "24000", str(dst)], check=True)


def measure(paths):
    """In the Dia venv: each WAV's Whisper read-back and pyin pitch (median, IQR), the share of its energy in
    1-4 kHz against 80 Hz-4 kHz (the shout's strain: brighter when shouted) and the spectral centroid."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")
    out = {}
    for p in paths:
        y, sr = librosa.load(p, sr=24000)
        f0, voiced, _ = librosa.pyin(y, fmin=55, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        S = np.abs(librosa.stft(y, n_fft=2048)) ** 2
        fr = librosa.fft_frequencies(sr=sr, n_fft=2048)
        loud = S.sum(0) > 0.05 * S.sum(0).max()
        band = S[:, loud].sum(1)
        bright = float(band[(fr >= 1000) & (fr < 4000)].sum() / band[(fr >= 80) & (fr < 4000)].sum())
        cent = float(np.median(librosa.feature.spectral_centroid(S=np.sqrt(S), freq=fr)[0][loud]))
        segs, _ = asr.transcribe(p, language="en", initial_prompt=v72.NAMES, word_timestamps=True)
        segs = list(segs)
        out[p] = {"pitch": [round(float(np.median(f))), round(float(np.percentile(f, 75) - np.percentile(f, 25)))]
                  if len(f) else [0, 0], "bright": round(bright, 3), "centroid": round(cent),
                  "heard": " ".join(s.text.strip() for s in segs),
                  "words": [[w.word.strip(), round(w.start, 2), round(w.end, 2)] for s in segs for w in s.words]}
        print(Path(p).name, out[p]["pitch"], out[p]["bright"], out[p]["centroid"], out[p]["heard"], flush=True)
    return out


def run_measure(paths):
    res = RAW / "measure.json"
    subprocess.run([str(v72.WHISPER_PY), __file__, "--measure", str(res)] + [str(p) for p in paths], check=True,
                   env=dict(os.environ, HF_HOME=str(v72.BASE / "hf")))
    return json.loads(res.read_text())


def has_lift(r):
    return "lift" in v72._norm(r["text"]) and "liff" not in v72._norm(r["heard"])


def pin_aurelia(rb):
    """The sign-off's pin: a take that reads back the whole line with "lift", no pause over 0.8 s, the narrowest
    pitch spread."""
    best = None
    for seed in SEEDS_AURELIA:
        r = rb.get(f"aurelia-b-2-s{seed}")
        if r and has_lift(r) and r["cover"] >= 1.0:
            key = (r["pause"] <= 0.8, -r["pitch"][1])
            if best is None or key > best[0]:
                best = (key, seed, r)
    return best


def pin_shout(rb):
    """Vorne b's pin: a take Whisper hears shouted (the line read back word for word with both "!"), the break after
    "No!" under 0.8 s, then the brightest spectrum (the shout's strain), measured on the cut take."""
    cands = []
    for row in ("shout", "strong"):
        for seed in SEEDS_VORNE:
            r = rb.get(f"vorne-low-{row}-s{seed}")
            if r and r["cover"] >= 1.0 and r["text"].count("!") >= 2 and r["pause"] <= 0.8:
                cands.append((f"vorne-low-{row}-s{seed}", r))
    return cands


def cut(s, path, end):
    """The take from its start to the end of its last word: the first point after Whisper's end of the line where the
    level stays 30 dB under the peak for 60 ms (+50 ms), else the whole (trimmed) take; a 40 ms fade. Round 72's cut
    (0.15 s after Whisper's end) clipped the shouted "-ment!", which Whisper times early and which runs 0.3-0.4 s."""
    import numpy as np
    x = s.decode(path)[0].copy()
    sr, n, hop = s.SR, int(0.02 * s.SR), int(0.01 * s.SR)
    pk = np.max(np.abs(x)) + 1e-12
    lev = np.array([20 * np.log10(np.sqrt(np.mean(x[i:i + n] ** 2)) / pk + 1e-12) for i in range(0, len(x) - n, hop)])
    k = len(x)
    for f in range(int(end * sr / hop), len(lev) - 6):
        if np.all(lev[f:f + 6] < -30):
            k = min(len(x), f * hop + int(0.05 * sr))
            break
    x = x[:k]
    x[-int(0.04 * sr):] *= np.linspace(1, 0, int(0.04 * sr))
    return x


def world(src, dst, factor):
    """In the WORLD venv: src's pitch times ``factor`` by WORLD analysis and resynthesis (harvest f0, CheapTrick
    envelope, D4C aperiodicity), the spectral envelope (the formants) and the timing untouched."""
    import numpy as np
    import pyworld as pw
    import soundfile as sf
    x, sr = sf.read(src)
    x = (x.mean(1) if x.ndim > 1 else x).astype(np.float64)
    f0, t = pw.harvest(x, sr, f0_floor=60, f0_ceil=600, frame_period=5.0)
    y = pw.synthesize(f0 * factor, pw.cheaptrick(x, f0, t, sr), pw.d4c(x, f0, t, sr), sr, frame_period=5.0)
    sf.write(dst, (y / np.max(np.abs(y)) * 0.8).astype(np.float32), sr)


def post():
    import numpy as np
    sys.path.insert(0, str(HERE))
    sys.path.insert(0, str(ROOT / "tools" / "art"))
    import tts_r18
    from voice import enemy
    s = tts_r18._synth()
    rb = json.loads(READBACK.read_text())
    rb72 = json.loads(READBACK72.read_text())
    tmp = RAW / "post"
    tmp.mkdir(parents=True, exist_ok=True)

    def wav(x, name):
        p = tmp / f"{name}.wav"
        s.write_wav(p, x / max(1e-9, np.max(np.abs(x))) * 0.8)
        return p

    def lowered(name, hz):
        """The cut take tmp/<name>.wav pitched down to TARGET_HZ by WORLD when above 130 Hz; (signal, factor)."""
        f = TARGET_HZ / hz if hz > 130 else 1.0
        if f < 1.0:
            subprocess.run([str(WORLD_PY), __file__, "--world", str(tmp / f"{name}.wav"), str(tmp / f"{name}-low.wav"),
                            f"{f:.6f}"], check=True)
            return s.decode(tmp / f"{name}-low.wav")[0], f
        return s.decode(tmp / f"{name}.wav")[0], f

    def write(name, parts, filt):
        gap = np.zeros((1, int(v72.GAP * s.SR)))
        y = np.concatenate(sum([[filt(p), gap] for p in parts], [])[:-1], axis=1)
        s.write_ogg(DEST / f"{name}.ogg", y, quality=4, tags={"COMMENT": f"tools/concept/audio/voice_r73.py {name}"})
        print(name, round(y.shape[1] / s.SR, 2), "s", round(s.lufs(y)[0], 1), "LUFS", flush=True)

    # Aurelia Traffic b: round 72's pinned call, the new sign-off
    v72.READBACK = READBACK72                           # round 72's pin of the call
    seed1, r1 = v72.pin("aurelia-traffic-b-1", v72.LINES["aurelia-traffic"][0][0])
    v72.READBACK = READBACK
    lifts = [sd for sd in SEEDS_AURELIA if rb.get(f"aurelia-b-2-s{sd}") and has_lift(rb[f"aurelia-b-2-s{sd}"])]
    print(f"aurelia b: seeds reading back 'lift': {lifts}; rule's pick {pin_aurelia(rb)[1]}; pinned {AURELIA_PIN}",
          flush=True)
    r2 = rb[f"aurelia-b-2-s{AURELIA_PIN}"]
    x1 = cut(s, RAW72 / f"aurelia-traffic-b-1-s{seed1}.wav", r1["end"])
    x2 = cut(s, RAW / f"aurelia-b-2-s{AURELIA_PIN}.wav", r2["end"])
    wav(x2, "aurelia-signoff")
    write("voice-aurelia-traffic-r73-b", [x1, x2], tts_r18.radio)

    # Vorne: a round 72's shout-row take (seed 101); b the shouted takes of the lower clip, the brightest pinned
    wav(cut(s, RAW72 / "vorne-shout-r72-a-1-s101.wav", rb72["vorne-shout-r72-a-1-s101"]["end"]), "vorne-a")
    cands = pin_shout(rb)
    for job, r in cands:
        wav(cut(s, RAW / f"{job}.wav", r["end"]), job)
    m = run_measure([tmp / "vorne-a.wav"] + [tmp / f"{job}.wav" for job, _ in cands])
    ranked = sorted(cands, key=lambda c: -m[str(tmp / f"{c[0]}.wav")]["bright"])
    for job, r in ranked:
        q = m[str(tmp / f"{job}.wav")]
        print(f"  shouted: {job} {q['pitch']} Hz, bright {q['bright']}, centroid {q['centroid']}: {q['heard']}",
              flush=True)
    jb = VORNE_B_PIN or ranked[0][0]
    ya, fa = lowered("vorne-a", m[str(tmp / "vorne-a.wav")]["pitch"][0])
    yb, fb = lowered(jb, m[str(tmp / f"{jb}.wav")]["pitch"][0])
    print(f"vorne a: r72 seed 101 x{fa:.3f} ({12 * np.log2(fa):+.1f} st); vorne b: {jb} x{fb:.3f} "
          f"({12 * np.log2(fb):+.1f} st)", flush=True)
    write("vorne-shout-r73-a", [ya], lambda x: enemy(x, s))
    write("vorne-shout-r73-b", [yb], lambda x: enemy(x, s))
    fin = run_measure([tmp / "vorne-a.wav", tmp / "vorne-a-low.wav", tmp / f"{jb}.wav", tmp / f"{jb}-low.wav",
                       tmp / "aurelia-signoff.wav"])
    (RAW / "final.json").write_text(json.dumps(fin, indent=1))


def main(args):
    patch()
    if args[:1] == ["--worker"]:
        v72.worker()
        return
    if args[:1] == ["--whisper"]:
        v72.whisper(args[1])
        return
    if args[:1] == ["--world"]:
        world(args[1], args[2], float(args[3]))
        return
    if args[:1] == ["--measure"]:
        Path(args[1]).write_text(json.dumps(measure(args[2:]), indent=1))
        return
    if args[:1] != ["--post"]:
        low_clip()
        env = dict(os.environ, HF_HOME=str(v72.BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        subprocess.run([str(v72.BASE / "venv-chatterbox" / "bin" / "python"), __file__, "--worker"], env=env,
                       check=True)
    subprocess.run([str(v72.WHISPER_PY), __file__, "--whisper", "--read-back"], check=True,
                   env=dict(os.environ, HF_HOME=str(v72.BASE / "hf")))
    post()


if __name__ == "__main__":
    main(sys.argv[1:])
