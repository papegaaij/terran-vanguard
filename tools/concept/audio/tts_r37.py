#!/usr/bin/env python3
"""Concept round 37: the Lance Control audition (M7 part A; the Orbital Lance's caller, see
design/audio/voice/README.md "Act 3 voices"; pending the user's review).

Outputs (design/audio/voice/concept/; the rejected a's audition in concept/rejected/, not rewritten):
  voice-lance-control-r37-<a|b>.ogg  Lance Control (the CDF orbital officer who fires the Orbital Lance,
                                     from Level 17; Hammer Lead's Airstrike call is the model): its one
                                     line, "Lance uplink locked. Firing!", through radio filter b
OGG Vorbis q4, 44.1 kHz mono; Chatterbox with candidate reference clip a or b at the neutral settings
(data.yaml's `expressions.neutral`), as the game would render the line.

Casting (the voice README: clipped and precise, a voice unlike Hammer Lead's; two new readers from the
public-domain LibriVox recordings, never a main-cast reader nor a voice already cast or auditioned):
both read military or aviation non-fiction in a brisk, even delivery; Hammer Lead's clip is at 99 Hz,
so neither candidate sits there (clip pitches measured with librosa pyin, median; the speech rate with
Whisper's word timings over the 12 s clip):
  a Ted Lienhart (Our Air Force: The Keystone of National Defense, ch. 1, Mitchell): 132 Hz, a narrow
    pitch range (IQR 16 Hz), 2.7 words a second: the clipped staff officer
  b Celine Major (Campaigns of the Civil War, ch. 1, Geer): 159 Hz, 3.0 words a second: quicker and
    lighter, the precise operator
(Also measured and passed over: Norman Elfer, 99 Hz, Hammer Lead's pitch; Clive Catterall, 89 Hz, slow;
Scott Danneker, a wide pitch range and long pauses; Susan Morin, a storybook read.)

Reference clips: design/audio/voice/refs/ref-lance-control.wav (b's) and (rejected)
design/audio/voice/concept/rejected/ref-lance-control-r37-a.wav (Git LFS, in CREDITS.md), 12 s
cuts from LibriVox chapter recordings (public domain, sources in REFS; licences checked on each
archive.org item's `licenseurl`), made as in rounds 19-36 with
    ffmpeg -ss 60 -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
(`python3 tools/concept/audio/tts_r37.py --refs` cuts the missing ones) and checked with their
Whisper (faster-whisper base.en) transcript, the .txt next to each clip.
At the user's pick (2026-10-13, b, Celine Major) b's clip was renamed refs/ref-lance-control.wav (the
cast voice's, which `ref("b")` now reads; step Q2); a's clip, transcript and audition moved to
design/audio/voice/concept/rejected/ when the round closed (step Q3). So the script now renders, posts
and reads back only the chosen b (CHOSEN; `--refs` cuts nothing while b's clip exists); a's source and
pin stay in REFS and PINS as the record.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper
(faster-whisper) and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the
pitch check.
Rerun: python3 tools/concept/audio/tts_r37.py            (generate, post-process, read back)
       python3 tools/concept/audio/tts_r37.py --post     (post-process and read back only)
       python3 tools/concept/audio/tts_r37.py --refs     (cut the missing reference clips)
Raw WAVs go to ~/.cache/tv-tts/raw/r37/. Seeds are fixed, but GPU sampling is not bit-exact.
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
RAW = BASE / "raw" / "r37"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

SPEAKER = "lance-control"
NEUTRAL = {"exaggeration": 0.5, "cfg_weight": 0.5, "temperature": 0.7}
# The one line (design/player/specials/data.yaml, orbital_lance.radio; the specials README).
LINES = ["Lance uplink locked. Firing!"]
NAMES = None  # no read-back prompt: a prompt that is the line itself makes Whisper drop words from it
GAP = 1.0  # s of silence between two filtered lines

# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("ourairforce_2502_librivox", "ourairforce_01_mitchell_64kb.mp3", 60, "Ted Lienhart",
          "Public Domain Mark 1.0"),
    "b": ("campaignsofthecivilwar_2603_librivox", "campaignscivilwar_01_geer_64kb.mp3", 60, "Celine Major",
          "Public Domain Mark 1.0"),
}
SEED = 37
# The takes are pinned (variant, line) -> seed: of seeds 100-105 per candidate, the shortest (most clipped)
# take that Whisper read back right from the raw WAV: a 104 (1.76 s), b 101 (2.32 s); two of a's six
# takes read "Landsup link", two of b's "Plants" / "Plans are blank", b 102 began with a breath ("Ooh").
# Through radio filter b Whisper base.en hears "Lands up, link" / "Plants up, link" in both picks (the
# filter's band limit; the raw takes read "Lance uplink"), so the round's listener judges "uplink".
PINS = {("a", 0): 104, ("b", 0): 101}
CHOSEN = "b"   # the user's pick (round 37, S.16, 2026-10-13): its clip is the cast voice's


def ref(variant):
    if variant == CHOSEN:
        return REFDIR / f"ref-{SPEAKER}.wav"
    return DEST / "rejected" / f"ref-{SPEAKER}-r37-{variant}.wav"   # moved when the round closed


def refs():
    """Cut the missing reference clips and write their Whisper transcripts."""
    for variant in (CHOSEN,):
        item, name, start, _, _ = REFS[variant]
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
    for i, variant in enumerate(REFS):
        if variant != CHOSEN:
            continue
        for j, text in enumerate(LINES):
            seed = PINS.get((variant, j), SEED + 10 * i + j)
            for f in (random.seed, np.random.seed, torch.manual_seed):
                f(seed)
            wav = model.generate(text, audio_prompt_path=str(ref(variant)), **NEUTRAL)
            wav = wav.cpu().numpy().reshape(-1)
            sf.write(str(RAW / f"{SPEAKER}-{variant}-{j + 1}.wav"), wav.astype("float32"), model.sr)
            print(variant, j + 1, "seed", seed, round(len(wav) / model.sr, 2), "s", flush=True)


def whisper(mode):
    """In the Dia venv: the reference transcripts (.txt) and clip pitches, or the raw takes' and the
    finished files' read-back with the takes' median pitch (librosa pyin) and their longest pause."""
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
        return round(float(np.median(f))), round(float(f.max()))

    if mode == "--transcribe-refs":
        for variant in (CHOSEN,):
            t = text(ref(variant))
            ref(variant).with_suffix(".txt").write_text(t.split("  [")[0] + "\n")
            print(ref(variant).name, pitch(ref(variant)), "Hz (median, max) :", t)
    else:
        for variant in (CHOSEN,):
            for j in range(len(LINES)):
                path = RAW / f"{SPEAKER}-{variant}-{j + 1}.wav"
                print("raw", path.stem, pitch(path), "Hz :", text(path, NAMES))
            path = DEST / f"voice-{SPEAKER}-r37-{variant}.ogg"
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
    for variant in (CHOSEN,):
        parts = []
        for j in range(len(LINES)):
            x = s.decode(RAW / f"{SPEAKER}-{variant}-{j + 1}.wav")[0]
            parts += [tts_r18.radio(x), np.zeros((1, int(GAP * s.SR)))]  # filter b
        y = np.concatenate(parts[:-1], axis=1)
        tags = {"COMMENT": f"tools/concept/audio/tts_r37.py {SPEAKER} {variant}"}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r37-{variant}.ogg", y, quality=4, tags=tags)
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
