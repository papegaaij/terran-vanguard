#!/usr/bin/env python3
"""Concept round 60: the Vorne audition (M9 "Act 5 - The Belt" design phase, step AUD-P; Chairman Silas Vorne, the
main villain, the speaker `vorne`, see design/audio/voice/README.md "Act 5 voices"; pending the user's review).

Outputs (design/audio/voice/concept/):
  voice-vorne-r60-<a|b|c>.ogg  Vorne (a new speaker, `uncast: true` until round 60 picks): two of his lines, the
                               broadcast's opening (Act 5 intro page 2: "Citizens of the solar system. My name is
                               Silas Vorne. From this hour the Jovian Ascendancy governs itself, and it does not
                               stand alone.") and the recruitment (Level 35: "Lancer. You are wasted on Okafor. Come
                               to Callisto. Name your price."), 1 s apart, each through the new enemy-channel filter
                               (`filter: enemy`, below) on its own
OGG Vorbis q4, 44.1 kHz mono; Chatterbox with candidate reference clip a, b or c at the README's proposed settings
for him (a theatrical base: exaggeration 0.6, cfg_weight 0.4 (slower), temperature 0.7).

The enemy filter (the voice README, *Act 5 voices*): radio filter b's band (300 Hz-3.4 kHz) and saturation as every
radio voice, its steady hiss with the slow flutter, but no crackle and no clicks; plus a steady static bed under the
whole line, band noise 400 Hz-4 kHz about 26 dB under the voice, swelling slowly (+-3 dB at 0.25 Hz); the line opens
and closes with the intercept's static sweep instead of the squelch (here round 60's proposals
design/audio/sfx/concept/intercept-open-r60-a.ogg and intercept-close-r60-a.ogg, whichever the round picks).

Casting (the voice README: an older man's polished low voice, a chairman, not a soldier: long sentences, patient,
amused, warm on the surface and cold under it; below every cast man (Ares Control 112 Hz, Thera Deep Control 119,
Nansen 124, the Kilo Lead 129: a clip under about 105 Hz), slow, with an orator's controlled range, not a monotone;
three new readers from the public-domain LibriVox recordings, never a main-cast reader nor a voice already cast or
auditioned). Clip pitches measured with librosa pyin (median, IQR) and the speech rate with Whisper's word timings,
over 30 s from the cut:
  a Jeffrey Church (The Genealogy of Morals, ch. 1, Nietzsche; cut at 90 s): 100 Hz, a wide range (IQR 54 Hz),
    2.0 words a second: a slow, deliberate American reading of an argument, the most rhetorical rise and fall
  b Clive Catterall (The Prince (version 4), ch. 1, Machiavelli; cut at 40 s): 96 Hz (IQR 44 Hz), 2.5 words a
    second: an older English voice reading the manual for running a principality, measured and dry
  c Gregg Margarite (Rastignac the Devil, ch. 2, Farmer; cut at 120 s): 84 Hz (IQR 39 Hz), 2.6 words a second: a
    deep American bass, the lowest of the three, warm and theatrical
(Also measured and passed over: Paul Adams, The Prince, 110 Hz, above the target; Peter Dann, Heart of Darkness,
99 Hz but 2.7 words a second and no licence on the archive.org item; Beyond Good and Evil's reader, 87 Hz but nearly
monotone (IQR 13 Hz); the Use and Abuse of History's, 105 Hz and monotone (IQR 9 Hz); the Philippics', 108 Hz and
2.9 words a second; David Clarke, 111 Hz, 3.1 words a second; Peter Yearsley, 120 Hz; Martin Geeson, 173 Hz.)

Reference clips: design/audio/voice/refs/ref-vorne-r60-<a|b|c>.wav (Git LFS, in CREDITS.md) with their Whisper
transcripts (.txt), 12 s cuts from LibriVox chapter recordings (public domain, sources in REFS; licences checked on
each archive.org item's `licenseurl`), made as in rounds 19-52 with
    ffmpeg -ss <start> -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
(`python3 tools/concept/audio/tts_r60.py --refs` cuts the missing ones). At the pick the chosen clip stays the
speaker's `ref`, the others move to concept/rejected/ with their auditions.

Takes: seeds SEEDS for every candidate and line; the pinned take (PINS) is the one Whisper read back right from the
raw WAV with its median pitch nearest the clip's (no octave jump) and no pause over 0.8 s.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper (faster-whisper)
and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the pitch check.
Rerun: python3 tools/concept/audio/tts_r60.py            (generate every seed, post-process the pins, read back)
       python3 tools/concept/audio/tts_r60.py --post     (post-process and read back only)
       python3 tools/concept/audio/tts_r60.py --refs     (cut the missing reference clips)
Raw WAVs go to ~/.cache/tv-tts/raw/r60/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
DEST = ROOT / "design" / "audio" / "voice" / "concept"
SFX = ROOT / "design" / "audio" / "sfx" / "concept"
BASE = Path.home() / ".cache" / "tv-tts"
RAW = BASE / "raw" / "r60"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

SPEAKER = "vorne"
SETTINGS = {"exaggeration": 0.6, "cfg_weight": 0.4, "temperature": 0.7}
# The Act 5 intro's page 2 (design/campaign/act-5-the-belt/README.md, Act intro) and Level 35's recruitment.
LINES = ["Citizens of the solar system. My name is Silas Vorne. From this hour the Jovian Ascendancy governs itself, "
         "and it does not stand alone.",
         "Lancer. You are wasted on Okafor. Come to Callisto. Name your price."]
NAMES = "Silas Vorne, Jovian Ascendancy, Lancer, Okafor, Callisto."
GAP = 1.0  # s of silence between two filtered lines
SWEEPS = ("intercept-open-r60-a.ogg", "intercept-close-r60-a.ogg")

# variant: (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    "a": ("genealogy_of_morals_1507_librivox", "genealogyofmorals_01_nietzsche_64kb.mp3", 90, "Jeffrey Church",
          "Public Domain Mark 1.0"),
    "b": ("theprincever4_1501_librivox", "prince_01_machiavelli_64kb.mp3", 40, "Clive Catterall",
          "Public Domain Mark 1.0"),
    "c": ("rastignac_devil_1003_librivox", "rastignacthedevil_2_farmer_64kb.mp3", 120, "Gregg Margarite",
          "public domain"),
}
SEEDS = (100, 101, 102)
# More seeds where the first three gave no clean take: (variant, line) -> extra seeds.
EXTRA = {("a", 0): (103, 104, 105), ("b", 0): (103, 104, 105)}


def seeds(variant, j):
    return SEEDS + EXTRA.get((variant, j), ())


# (variant, line) -> seed: the pin is the take Whisper read back right from the raw WAV with its median pitch nearest
# the clip's (a 103 Hz, b 92 Hz, c 89 Hz on the 12 s clips) without an octave jump (a p95 at the tracker's 399 Hz
# ceiling) and no pause over 0.8 s. Every reader comes out higher than his clip: a 123-138 Hz, b 107-127 Hz, c 93-106
# Hz; only c stays under the cast men. a's line 1 jumped an octave or paused over 0.8 s in all six takes (his reading
# rests about a second between sentences, as his clip does: 1.28 s); a 1 103 (131 Hz, no jump) is pinned with its
# 1.06 s rest between "Silas Vorne." and "From this hour" (judgement call: a rest between sentences, not a stumble).
# b 1 104 (115 Hz, 0.72 s) is b's one clean first line in six takes.
PINS = {("a", 0): 103, ("a", 1): 102, ("b", 0): 104, ("b", 1): 100, ("c", 0): 102, ("c", 1): 102}


def ref(variant):
    return REFDIR / f"ref-{SPEAKER}-r60-{variant}.wav"


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
                wav = model.generate(text, audio_prompt_path=str(ref(variant)), **SETTINGS)
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
        f0, voiced, _ = librosa.pyin(y, fmin=55, fmax=400, sr=sr)
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
            path = DEST / f"voice-{SPEAKER}-r60-{variant}.ogg"
            if path.exists():
                print("read back", path.name, ":", text(path, NAMES))


def run_whisper(mode):
    subprocess.run([str(WHISPER_PY), __file__, "--whisper", mode], check=True,
                   env=dict(os.environ, HF_HOME=str(BASE / "hf")))


def enemy(x, s):
    """The enemy-channel filter: radio filter b's band, hiss and saturation without its crackle and clicks, a steady
    static bed (400 Hz-4 kHz band noise 26 dB under the voice, swelling +-3 dB at 0.25 Hz), and the intercept's
    sweep in and out instead of the squelch. Mono float at s.SR in, (1, n) out at -16 LUFS."""
    import numpy as np
    sr = s.SR
    rng = np.random.default_rng(60)
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = int(0.15 * sr)
    voice = np.concatenate([np.zeros(pad), x, np.zeros(pad)])
    n = len(voice)
    t = np.arange(n) / sr
    flutter = 1 + 0.15 * s.sine(0.7, n) + 0.1 * s.sine(3.1, n)
    hiss = s.noise(n, rng) * 0.019 * flutter  # filter b's hiss, no crackle
    y = voice + hiss
    for _ in range(2):
        y = s.svf(y, 300, mode="hp")
        y = s.svf(y, 3400, mode="lp")
    y = s.saturate(y * 1.6, 2.0) * 0.8
    # the static bed: band noise 26 dB under the voice's RMS (over its voiced part), swelling slowly
    band = s.noise(n, rng)
    for _ in range(2):
        band = s.svf(band, 400, mode="hp")
        band = s.svf(band, 4000, mode="lp")
    active = np.abs(y) > 0.02
    v_rms = np.sqrt(np.mean(y[active] ** 2)) if active.any() else 0.1
    band = band / (np.sqrt(np.mean(band ** 2)) + 1e-12) * v_rms * 10 ** (-26 / 20)
    band *= 10 ** (3 * np.sin(2 * np.pi * 0.25 * t) / 20)
    y = y + band
    # the intercept's sweeps over the line's head and tail
    opening = s.decode(SFX / SWEEPS[0]).mean(axis=0)
    closing = s.decode(SFX / SWEEPS[1]).mean(axis=0)
    out = np.concatenate([opening * 0.9, y, closing * 0.9])
    return s.master(out[None, :], target_lufs=-16.0, ceiling_db=-1.5)


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
            parts += [enemy(x, s), np.zeros((1, int(GAP * s.SR)))]
        y = np.concatenate(parts[:-1], axis=1)
        tags = {"COMMENT": f"tools/concept/audio/tts_r60.py {SPEAKER} {variant}"}
        s.write_ogg(DEST / f"voice-{SPEAKER}-r60-{variant}.ogg", y, quality=4, tags=tags)
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
