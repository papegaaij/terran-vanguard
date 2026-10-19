#!/usr/bin/env python3
"""Concept round 36: the Ares Control audition (Act 3 "Red Dust", M7 part B; the colony's one voice,
Levels 15-17 and 19-21, see design/audio/voice/README.md "Act 3 voices"; pending the user's review).

Outputs (design/audio/voice/concept/; the rejected a's audition in concept/rejected/, not rewritten):
  voice-ares-control-r36-<a|b>.ogg  Ares Control (Ares Landing's flight controller, a new speaker):
                              three of his lines, chosen for the arc from resentment to the thaw:
                              Level 15's t=12 jab ("Aegis flight, Ares Landing. Five weeks we held
                              alone. Nice of Earth to drop by."), Level 17's t=102 ("Inner ring:
                              Schiaparelli, Burroughs, then Eden. Eden feeds the whole colony.", the
                              dome names) and Level 21's closing line ("Clear sky over every dome,
                              Aegis. Dinner's on Ares Landing."), 1 s apart, each through radio
                              filter b on its own
OGG Vorbis q4, 44.1 kHz mono; Chatterbox with candidate reference clip a or b at the voice README's
proposed base settings for him (a little drier than neutral: exaggeration 0.4, cfg_weight 0.45,
temperature 0.7).

Pronunciation: the text as spoken applies the voice README's proposed respelling list (Ares ->
"Airees", Schiaparelli -> "Skyaparelli", Burroughs -> "Burrows"; SAY); the subtitle keeps the real
spelling. Whisper's read-back gets the real names as its prompt (NAMES).

Casting: both candidates are men (the spec: older than Rook, a clip pitch about 100-115 Hz, below
Nansen's 124 Hz and the Kilo Lead's 129 Hz), new (not in the cast, not auditioned before), readers
of colonial or Mars non-fiction and fiction, chosen for a low, measured delivery (pitches in
prompts.md):
  a David Wales (Pioneer Life Among the Loyalists in Upper Canada, ch. 1, Herrington): the colonial
    settler-militia register
  b Thomas A (The Warlord of Mars, ch. 2, Burroughs; ch. 1 at 60 s measured 116 Hz, ch. 2 112 Hz)

Reference clips: design/audio/voice/refs/ref-ares-control.wav (b, trimmed; see below) and (rejected)
design/audio/voice/concept/rejected/ref-ares-control-r36-a.wav (Git LFS, in CREDITS.md), 12 s cuts from LibriVox chapter recordings (public domain, sources in REFS; licences
checked on each archive.org item's `licenseurl`), made as in rounds 19-35 with
    ffmpeg -ss 60 -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
(`python3 tools/concept/audio/tts_r36.py --refs` cuts the missing ones) and checked with their
Whisper (faster-whisper base.en) transcript, the .txt next to each clip.

At the user's pick (2026-10-13, b = Thomas A, in round 36's review) a's clip, transcript and audition
moved to design/audio/voice/concept/rejected/ when the round closed. So the script now renders, posts
and reads back only the chosen b (CHOSEN; `--refs` cuts nothing while b's clip exists); a's source
stays in REFS as the record. b's clip became refs/ref-ares-control.wav at the cast (M7 part B, step
AUD-B, 2026-10-14), with its pauses trimmed (trim_pauses): the 12 s cut held a 0.6 s and a 1.2 s
silence inside the reading (and 0.6 s of lead-in), and Chatterbox copies a clip's prosody (the
audition's takes held 2 s pauses inside two lines), so every silence under SILENCE_DB inside the clip
is shortened to PAUSE_KEEP s and the lead-in to LEAD_KEEP s (10 ms cross-fades at the joins): 10.3 s.
`--refs` cuts the 12 s from archive.org and trims it the same way. The audition above was rendered
from the untrimmed clip (then refs/ref-ares-control-r36-b.wav), so a rerun now differs from it.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper
(faster-whisper) and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and
the pitch check.
Rerun: python3 tools/concept/audio/tts_r36.py            (generate, post-process, read back)
       python3 tools/concept/audio/tts_r36.py --post     (post-process and read back only)
       python3 tools/concept/audio/tts_r36.py --refs     (cut the missing reference clips)
Raw WAVs go to ~/.cache/tv-tts/raw/r36/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import os
import re
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
DEST = ROOT / "design" / "audio" / "voice" / "concept"
BASE = Path.home() / ".cache" / "tv-tts"
RAW = BASE / "raw" / "r36"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

SPEAKER = "ares-control"
SETTINGS = {"exaggeration": 0.4, "cfg_weight": 0.45, "temperature": 0.7}
# The lines as written (design/campaign/act-3-red-dust/level-15-olympus-descent, level-17-dome-siege,
# level-21-dust-colossus READMEs, Radio chatter).
LINES = [
    "Aegis flight, Ares Landing. Five weeks we held alone. Nice of Earth to drop by.",
    "Inner ring: Schiaparelli, Burroughs, then Eden. Eden feeds the whole colony.",
    "Clear sky over every dome, Aegis. Dinner's on Ares Landing.",
]
# The voice README's proposed respelling list (the words these lines use), applied to the text as spoken.
SAY = {"Ares": "Airees", "Schiaparelli": "Skyaparelli", "Burroughs": "Burrows"}
NAMES = "Aegis, Ares Landing, Schiaparelli, Burroughs, Eden, Earth."
GAP = 1.0  # s of silence between two filtered lines

# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("pioneerlifeuppercanada_1407_librivox", "pioneerlife_01_herrington_64kb.mp3", 60, "David Wales",
          "Public Domain Mark 1.0"),
    "b": ("warlord_mars_tac_librivox", "warlord_of_mars_02_burroughs_64kb.mp3", 60, "Thomas A",
          "Public Domain Mark 1.0"),
}
SEED = 36
CHOSEN = "b"                                    # the user's pick (round 36's review, 2026-10-13)


def spoken(text):
    for word, say in SAY.items():
        text = re.sub(rf"\b{word}\b", say, text)
    return text


SILENCE_DB = -50.0  # dBFS of a 10 ms RMS frame under which the reference clip is silent
PAUSE_KEEP = 0.3    # s: a silence inside the clip longer than this is shortened to it
LEAD_KEEP = 0.1     # s of the lead-in silence kept
JOIN = 0.01         # s cross-fade at each join


def ref(variant):
    if variant != CHOSEN:
        return DEST / "rejected" / f"ref-{SPEAKER}-r36-{variant}.wav"   # moved when the round closed
    return REFDIR / f"ref-{SPEAKER}.wav"                                # the cast clip (part B), trimmed


def read_wav(path):
    import wave

    import numpy as np
    with wave.open(str(path)) as w:
        return np.frombuffer(w.readframes(w.getnframes()), dtype="<i2") / 32768.0, w.getframerate()


def write_wav(path, x, sr):
    import wave

    import numpy as np
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes(np.clip(np.round(x * 32768), -32768, 32767).astype("<i2").tobytes())


def silences(x, sr):
    """[(start, end)] sample ranges of the runs of 10 ms frames whose RMS is under SILENCE_DB."""
    import numpy as np
    w = int(0.01 * sr)
    n = len(x) // w
    quiet = 20 * np.log10(np.sqrt(np.mean(x[:n * w].reshape(n, w) ** 2, axis=1)) + 1e-9) < SILENCE_DB
    runs, start = [], None
    for i, q in enumerate(np.append(quiet, False)):
        if q and start is None:
            start = i
        elif not q and start is not None:
            runs.append((start * w, i * w))
            start = None
    return runs


def trim_pauses(x, sr):
    """The clip with its lead-in shortened to LEAD_KEEP s and every silence inside it to PAUSE_KEEP s
    (the middle of a pause is cut out, the join cross-faded over JOIN s); the tail is left alone."""
    import numpy as np
    j, half = int(JOIN * sr), int(PAUSE_KEEP * sr) // 2
    ramp = np.linspace(0, 1, j)
    out = x
    for start, end in reversed(silences(x, sr)):
        if end >= len(x):
            continue
        if start == 0:
            out = out[max(0, end - int(LEAD_KEEP * sr)):]
            continue
        a, b = start + half, end - half
        if b - a <= 2 * j:
            continue
        join = out[a - j:a] * (1 - ramp) + out[b:b + j] * ramp
        out = np.concatenate([out[:a - j], join, out[b + j:]])
    return out


def refs():
    """Cut the missing reference clips and write their Whisper transcripts."""
    for variant in (CHOSEN,):
        item, name, start, _, _ = REFS[variant]
        out = ref(variant)
        if not out.exists():
            cut = out.with_name(out.stem + "-untrimmed.wav")
            subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-ss", str(start), "-t", "12",
                            "-i", f"https://archive.org/download/{item}/{name}", "-ac", "1", "-ar", "24000",
                            str(cut)], check=True)
            x, sr = read_wav(cut)
            write_wav(out, trim_pauses(x, sr), sr)
            cut.unlink()
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
            for f in (random.seed, np.random.seed, torch.manual_seed):
                f(SEED + 10 * i + j)
            wav = model.generate(spoken(text), audio_prompt_path=str(ref(variant)), **SETTINGS)
            wav = wav.cpu().numpy().reshape(-1)
            sf.write(str(RAW / f"{SPEAKER}-{variant}-{j + 1}.wav"), wav.astype("float32"), model.sr)
            print(variant, j + 1, round(len(wav) / model.sr, 2), "s", flush=True)


def whisper(mode):
    """In the Dia venv: the reference transcripts (.txt) and clip pitches, or the raw takes' and the
    finished files' read-back with the takes' median pitch (librosa pyin)."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")

    def text(path, prompt=None):
        segs, _ = asr.transcribe(str(path), language="en", initial_prompt=prompt)
        return " ".join(seg.text.strip() for seg in segs)

    def pitch(path):
        y, sr = librosa.load(str(path), sr=24000)
        f0, voiced, _ = librosa.pyin(y, fmin=60, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        return round(float(np.median(f))), round(float(f.max()))

    if mode == "--transcribe-refs":
        for variant in (CHOSEN,):
            t = text(ref(variant))
            ref(variant).with_suffix(".txt").write_text(t + "\n")
            print(ref(variant).name, pitch(ref(variant)), "Hz (median, max) :", t)
    else:
        for variant in (CHOSEN,):
            for j in range(len(LINES)):
                path = RAW / f"{SPEAKER}-{variant}-{j + 1}.wav"
                print("raw", path.stem, pitch(path), "Hz :", text(path, NAMES))
            path = DEST / f"voice-{SPEAKER}-r36-{variant}.ogg"
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
        tags = {"COMMENT": f"tools/concept/audio/tts_r36.py {SPEAKER} {variant}"}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r36-{variant}.ogg", y, quality=4, tags=tags)
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
