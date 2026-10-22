#!/usr/bin/env python3
"""Concept round 52: the Thera Deep Control audition (M8 part C, step AUD-C; Thera Deep's harbour controller, the
speaker `thera-control`, Levels 23 and 27, see design/audio/voice/README.md "Act 4 voices"; pending the user's
review).

Outputs (design/audio/voice/concept/):
  voice-thera-control-r52-<a|b>.ogg  Thera Deep Control (a new speaker, `uncast: true` until round 52 picks): his
                                     audition lines, Level 23's t=34 call ("Aegis, Thera Deep Control. We see you
                                     on the shaft. The farms are below you, and so is whatever is draining them.")
                                     and the t=70 line ("Those are our farms, Aegis. Forty thousand people eat from
                                     those racks."), 1 s apart, each through radio filter b on its own
OGG Vorbis q4, 44.1 kHz mono; Chatterbox with candidate reference clip a or b at the neutral settings
(data.yaml's `expressions.neutral`), as the game would render the lines.

Casting (the voice README: a calm man, older than Rook, the city's harbour controller; below or between the cast
men (Ares Control 118 Hz on his trimmed clip, Nansen 123 Hz, the Kilo Lead 129 Hz; Hammer Lead 99 Hz) by timbre
rather than pitch alone, warmer and slower than Ares Control's dry 3.0 words a second; two new readers from the
public-domain LibriVox recordings, never a main-cast reader nor a voice already cast or auditioned). Clip pitches
measured with librosa pyin (median, IQR), the speech rate with Whisper's word timings over the 12 s clip:
  a Bruce Pirie (The Mayor of Casterbridge, ch. 1, Hardy; cut at 120 s): 100 Hz, a narrow range (IQR 19 Hz), 2.6
    words a second: a low, even, unhurried country narration, between Hammer Lead and Ares Control
  b John Van Stan (Early Greek Philosophy & Other Essays, ch. 1, Nietzsche; cut at 60 s): 119 Hz, a narrow range
    (IQR 26 Hz), 1.7 words a second: Ares Control's pitch but much slower and softer, a measured essay read
(Also measured and passed over: John Greenman, 103 Hz but quick, 3.2 words a second; Andy Minter, 113 Hz, 2.9; Tom
Weiss, 114 Hz, 2.8 (The Harbor) with octave jumps in the clip; Bob Neufeld, 152 Hz, and Don W. Jenkins, 150 Hz, above
the cast men; Barry Eads, 142 Hz, a wide range; Phil Chenevert, 130 Hz and quick; Clive Catterall, too low and
creaky for the pitch tracker.)

Reference clips: design/audio/voice/refs/ref-thera-control-r52-<a|b>.wav (Git LFS, in CREDITS.md) with their Whisper
transcripts (.txt), 12 s cuts from LibriVox chapter recordings (public domain, sources in REFS; licences checked on
each archive.org item's `licenseurl`), made as in rounds 19-51 with
    ffmpeg -ss <start> -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
(`python3 tools/concept/audio/tts_r52c.py --refs` cuts the missing ones). At the pick the chosen clip stays the
speaker's `ref` (see the voice README on keeping the -rNN- name), the other moves to concept/rejected/ with its
audition.

Takes: seeds SEEDS for every candidate and line; the pinned take (PINS) is the one Whisper read back right from the
raw WAV with its median pitch nearest the clip's (no octave jump) and no long pause.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper (faster-whisper)
and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the pitch check.
Rerun: python3 tools/concept/audio/tts_r52c.py            (generate every seed, post-process the pins, read back)
       python3 tools/concept/audio/tts_r52c.py --post     (post-process and read back only)
       python3 tools/concept/audio/tts_r52c.py --refs     (cut the missing reference clips)
Raw WAVs go to ~/.cache/tv-tts/raw/r52c/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
DEST = ROOT / "design" / "audio" / "voice" / "concept"
BASE = Path.home() / ".cache" / "tv-tts"
RAW = BASE / "raw" / "r52c"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

SPEAKER = "thera-control"
NEUTRAL = {"exaggeration": 0.5, "cfg_weight": 0.5, "temperature": 0.7}
# Level 23's t=34 and t=70 lines (design/campaign/act-4-deep-water/level-23-through-the-ice/README.md, Radio).
LINES = ["Aegis, Thera Deep Control. We see you on the shaft. The farms are below you, and so is whatever is "
         "draining them.",
         "Those are our farms, Aegis. Forty thousand people eat from those racks."]
NAMES = "Aegis, Thera Deep Control, Thera Deep."
GAP = 1.0  # s of silence between two filtered lines

# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("mayor_casterbridge_1207_librivox", "mayorofcasterbridge_01_hardy_64kb.mp3", 120, "Bruce Pirie",
          "Public Domain Mark 1.0"),
    "b": ("earlygreekphilosophy_2103_librivox", "earlyessays_01_nietzsche_64kb.mp3", 60, "John Van Stan",
          "Public Domain Mark 1.0"),
}
SEEDS = (100, 101, 102)
# More seeds where the first three gave no clean take: (variant, line) -> extra seeds.
EXTRA = {("b", 1): (103, 104, 105)}


def seeds(variant, j):
    return SEEDS + EXTRA.get((variant, j), ())
# (variant, line) -> seed: the pin is the take Whisper read back right from the raw WAV with its median pitch nearest
# the clip's (a 100 Hz, b 119 Hz) without a jump (a 1 101 rose to 399 Hz at its p95, b 1 102 too). Both readers come
# out higher than their clips (a 116-130 Hz, b 119-144 Hz): a 1 100 (116 Hz, 7.7 s), a 2 100 (127 Hz, 5.3 s),
# b 1 101 (133 Hz, 8.0 s). b's "racks" read back as "wrecks" in five of six takes (seeds 100-104, his open vowel);
# b 2 105 (126 Hz, 4.6 s) is the one read back right ("racks"; Whisper adds a hallucinated "Exhale" on its tail).
PINS = {("a", 0): 100, ("a", 1): 100, ("b", 0): 101, ("b", 1): 105}


def ref(variant):
    return REFDIR / f"ref-{SPEAKER}-r52-{variant}.wav"


def raw(variant, j, seed):
    return RAW / f"{SPEAKER}-{variant}-{j + 1}-s{seed}.wav"


def refs():
    """Cut the missing reference clips and write their Whisper transcripts."""
    for variant, (item, name, start, _, _) in REFS.items():
        out = ref(variant)
        if not out.exists():
            subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-ss", str(start), "-t", "12",
                            "-i", f"https://archive.org/download/{item}/{name}", "-ac", "1", "-ar", "24000",
                            str(out)], check=True)
    run_whisper("--transcribe-refs")


def worker():
    import random

    import numpy as np
    import soundfile as sf
    import torch
    from chatterbox.tts import ChatterboxTTS
    model = ChatterboxTTS.from_pretrained(device="cuda")
    RAW.mkdir(parents=True, exist_ok=True)
    for variant in REFS:
        for j, text in enumerate(LINES):
            for seed in seeds(variant, j):
                if raw(variant, j, seed).exists():
                    continue
                for f in (random.seed, np.random.seed, torch.manual_seed):
                    f(seed)
                wav = model.generate(text, audio_prompt_path=str(ref(variant)), **NEUTRAL)
                wav = wav.cpu().numpy().reshape(-1)
                sf.write(str(raw(variant, j, seed)), wav.astype("float32"), model.sr)
                print(variant, j + 1, "seed", seed, round(len(wav) / model.sr, 2), "s", flush=True)


def whisper(mode):
    """In the Dia venv: the reference transcripts (.txt) and clip pitches, or the raw takes' and the finished
    files' read-back with the takes' median pitch (librosa pyin) and their longest pause."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")

    def text(path, prompt=None):
        segs, _ = asr.transcribe(str(path), language="en", initial_prompt=prompt, word_timestamps=True)
        segs = list(segs)
        words = [w for seg in segs for w in seg.words]
        gap = max((b.start - a.end for a, b in zip(words, words[1:])), default=0.0)
        return " ".join(seg.text.strip() for seg in segs) + f"  [longest pause {gap:.2f} s]"

    def pitch(path):
        y, sr = librosa.load(str(path), sr=24000)
        f0, voiced, _ = librosa.pyin(y, fmin=60, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        return round(float(np.median(f))), round(float(np.percentile(f, 95))), round(len(y) / sr, 2)

    if mode == "--transcribe-refs":
        for variant in REFS:
            t = text(ref(variant))
            ref(variant).with_suffix(".txt").write_text(t.split("  [")[0] + "\n")
            print(ref(variant).name, pitch(ref(variant)), "Hz (median, p95, s) :", t)
    else:
        for variant in REFS:
            for j in range(len(LINES)):
                for seed in seeds(variant, j):
                    path = raw(variant, j, seed)
                    if path.exists():
                        print("raw", path.stem, pitch(path), "Hz (median, p95, s) :", text(path, NAMES))
            path = DEST / f"voice-{SPEAKER}-r52-{variant}.ogg"
            if path.exists():
                print("read back", path.name, ":", text(path, NAMES))


def run_whisper(mode):
    subprocess.run([str(WHISPER_PY), __file__, "--whisper", mode], check=True,
                   env=dict(os.environ, HF_HOME=str(BASE / "hf")))


def post():
    import numpy as np
    sys.path.insert(0, str(HERE))
    import tts_r18
    s = tts_r18._synth()
    DEST.mkdir(parents=True, exist_ok=True)
    for variant in REFS:
        if not all((variant, j) in PINS for j in range(len(LINES))):
            print(variant, "not pinned yet: no audition file")
            continue
        parts = []
        for j in range(len(LINES)):
            x = s.decode(raw(variant, j, PINS[(variant, j)]))[0]
            parts += [tts_r18.radio(x), np.zeros((1, int(GAP * s.SR)))]  # filter b
        y = np.concatenate(parts[:-1], axis=1)
        tags = {"COMMENT": f"tools/concept/audio/tts_r52c.py {SPEAKER} {variant}"}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r52-{variant}.ogg", y, quality=4, tags=tags)
        print(variant, round(y.shape[1] / s.SR, 2), "s", round(s.lufs(y)[0], 1), "LUFS", flush=True)


def main(args):
    if args[:1] == ["--worker"]:
        worker()
        return
    if args[:1] == ["--whisper"]:
        whisper(args[1])
        return
    if args[:1] == ["--refs"]:
        refs()
        return
    if args[:1] != ["--post"]:
        env = dict(os.environ, HF_HOME=str(BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        py = BASE / "venv-chatterbox" / "bin" / "python"
        subprocess.run([str(py), __file__, "--worker"], env=env, check=True)
    post()
    run_whisper("--read-back")


if __name__ == "__main__":
    main(sys.argv[1:])
