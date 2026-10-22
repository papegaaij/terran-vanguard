#!/usr/bin/env python3
"""Concept round 51: the Dr. Ilse Brandt audition (M8 part B, step AUD-B; Conamara Station's director, the
speaker `conamara`, Level 22, see design/audio/voice/README.md "Act 4 voices"; pending the user's review).

Outputs (design/audio/voice/concept/):
  voice-conamara-r51-<a|b>.ogg  Dr. Brandt (a new speaker, `uncast: true` until round 51 picks): her first call,
                                Level 22's t=1 ("Aegis flight, Conamara Station. Four rigs on the shelf, forty of
                                us in the dome, and the ice will not hold still."), and her next line after Varga's
                                "Ilse? It's Elena. Hold on, we are coming." (t=66), the t=100 call ("Something went
                                through under the north dome. There is water in the shaft at Kilo-Seven. It leads
                                down."), 1 s apart, each through radio filter b on its own
OGG Vorbis q4, 44.1 kHz mono; Chatterbox with candidate reference clip a or b at the neutral settings
(data.yaml's `expressions.neutral`), as the game would render the lines.

The voice README's audition lines are "her first call and her answer to Varga"; Level 22's radio table gives her no
line that answers Varga directly (Okafor does not reply either), so the second line is her next call after Varga's,
t=100 (the gap is in the step's report).

Casting (the voice README: a woman, dry, tired, precise, older than Varga, a lower and flatter register than Varga's
190-210 Hz and apart from Okafor's voice, 152 Hz; two new readers from the public-domain LibriVox recordings, never
a main-cast reader nor a voice already cast or auditioned). Clip pitches measured with librosa pyin (median, IQR),
the speech rate with Whisper's word timings over the 12 s clip:
  a Margaret Espaillat (Eminent Victorians, ch. 1, Strachey): 168 Hz, a very narrow range (IQR 22 Hz), 2.4 words a
    second: dry, even, precise non-fiction
  b Sue Anderson (Gallipoli Diary, ch. 1, Gillam): 172 Hz, a narrow range (IQR 29 Hz), 2.4 words a second: a
    steady, plain, older read of a war diary
(Also measured and passed over: Mil Nicholson, 147 Hz, too near Okafor's 152 Hz; Elizabeth Klett, 219 Hz and a wide
range (IQR 61 Hz), above Varga; Kristin LeMoine, a storybook read with a wide range.)

Reference clips: design/audio/voice/refs/ref-conamara-r51-<a|b>.wav (Git LFS, in CREDITS.md) with their Whisper
transcripts (.txt), 12 s cuts from LibriVox chapter recordings (public domain, sources in REFS; licences checked on
each archive.org item's `licenseurl`), made as in rounds 19-37 with
    ffmpeg -ss 60 -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
(`python3 tools/concept/audio/tts_r51b.py --refs` cuts the missing ones). At the pick the chosen clip becomes
refs/ref-conamara.wav (the speaker's `ref`), the other moves to concept/rejected/ with its audition.

Takes: seeds SEEDS for every candidate and line; the pinned take (PINS) is the one Whisper read back right from the
raw WAV with its median pitch nearest the clip's (no octave jump).

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper (faster-whisper)
and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the pitch check.
Rerun: python3 tools/concept/audio/tts_r51b.py            (generate every seed, post-process the pins, read back)
       python3 tools/concept/audio/tts_r51b.py --post     (post-process and read back only)
       python3 tools/concept/audio/tts_r51b.py --refs     (cut the missing reference clips)
Raw WAVs go to ~/.cache/tv-tts/raw/r51b/. Seeds are fixed, but GPU sampling is not bit-exact.
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
RAW = BASE / "raw" / "r51b"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

SPEAKER = "conamara"
NEUTRAL = {"exaggeration": 0.5, "cfg_weight": 0.5, "temperature": 0.7}
# Level 22's t=1 and t=100 lines (design/campaign/act-4-deep-water/level-22-ice-shelf/README.md, Radio).
LINES = ["Aegis flight, Conamara Station. Four rigs on the shelf, forty of us in the dome, and the ice will not "
         "hold still.",
         "Something went through under the north dome. There is water in the shaft at Kilo-Seven. It leads down."]
NAMES = "Aegis, Conamara Station, Kilo-Seven."
GAP = 1.0  # s of silence between two filtered lines

# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("eminent_victorians_1204_librivox", "eminentvictorians_01_strachey_64kb.mp3", 60, "Margaret Espaillat",
          "Public Domain Mark 1.0"),
    "b": ("gallipoli_diary_sa_librivox", "gallipolidiary_01_gillam_64kb.mp3", 60, "Sue Anderson",
          "public domain"),
}
SEEDS = (100, 101, 102)
# (variant, line) -> seed: every take read back right from the raw WAV (Whisper with NAMES); the pin is the take whose
# median pitch is nearest the clip's (a 168 Hz, b 172 Hz) without a jump (a 1 101 rose to 399 Hz at its p95):
# a 1 100 (165 Hz, 8.8 s), a 2 100 (162 Hz, 6.7 s), b 1 100 (173 Hz, 9.0 s), b 2 102 (173 Hz, 6.7 s).
PINS = {("a", 0): 100, ("a", 1): 100, ("b", 0): 100, ("b", 1): 102}


def ref(variant):
    return REFDIR / f"ref-{SPEAKER}-r51-{variant}.wav"


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
            for seed in SEEDS:
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
                for seed in SEEDS:
                    path = raw(variant, j, seed)
                    if path.exists():
                        print("raw", path.stem, pitch(path), "Hz (median, p95, s) :", text(path, NAMES))
            path = DEST / f"voice-{SPEAKER}-r51-{variant}.ogg"
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
        tags = {"COMMENT": f"tools/concept/audio/tts_r51b.py {SPEAKER} {variant}"}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r51-{variant}.ogg", y, quality=4, tags=tags)
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
