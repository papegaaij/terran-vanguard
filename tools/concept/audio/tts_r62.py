#!/usr/bin/env python3
"""Concept round 62 (M9 part B, step VOX-B): the casting of Ceres Hub Control and Harker (QB-1) and Vorne's pitch
(round 61 C.9); see design/audio/voice/README.md "Act 5 voices". Pending the user's review.

Outputs:
  design/audio/voice/refs/ref-ceres-control-r62-<a|b>.wav, ref-harker-r62-<a|b>.wav (+ .txt, Whisper's transcript):
      the candidates' reference clips, 12 s cuts from LibriVox chapter recordings (public domain / CC0, REFS), mono
      24 kHz WAV, made as in rounds 19-60 with
          ffmpeg -ss <start> -t 12 -i https://archive.org/download/<item>/<file> -ac 1 -ar 24000 <clip>.wav
  design/audio/voice/refs/ref-vorne.wav (+ .txt): Vorne's production clip, round 60's ref-vorne-r60-a.wav (Jeffrey
      Church) pitched VORNE_SHIFT semitones by ffmpeg's rubberband filter with the formants preserved
      (`rubberband=pitch=2^(-2/12):formant=preserved:pitchq=quality`); Chatterbox re-voices the lowered clip, so the
      takes carry no pitch-shifter artefacts (the shift is not applied to the takes).
  design/audio/voice/concept/vorne-pitch-r62-<a|b>.ogg: one chunk of the Act 5 intro's page 2 ("From this hour the
      Jovian Ascendancy governs itself, and it does not stand alone: the Vrell are our partners.") at Vorne's settings
      (0.6 / 0.4 / 0.7) through the enemy filter (tools/art/voice.py enemy(), the game's static sweep not baked in):
      a with ref-vorne (pitched, the production render), b with ref-vorne-r60-a (unshifted, as auditioned in round 60).
  design/audio/voice/concept/voice-<ceres-control|harker>-r62-<a|b>.ogg: each candidate's two audition lines (Level
      29: Hub Control's t=1 and t=101, Harker's t=68 (70.5 since step B4b) and t=133), 1 s apart, cut from the production renders of
      tools/art/voice.py (a: assets/voice/<speaker>/<key>.ogg, b: concept/voice-<speaker>-r62-b/<key>.ogg; AUDITION's
      keys), so the audition is the game's own take.

Casting (the voice README, Act 5 voices: new readers from LibriVox's public-domain and CC0 recordings, never a
main-cast reader nor a voice already cast or auditioned). Clip pitches measured with librosa pyin (median, IQR) and the
pace with Whisper's word timings over the 12 s clip; "takes" is the median pitch of the rendered audition lines:
  Ceres Hub Control (a man, tired and steady, a mid register 130-150 Hz between the Kilo Lead and Rook):
    a Paul Adams (Sidelights on Relativity, ch. 1, Einstein; cut at 66 s): 123 Hz, IQR 24 Hz, 2.7 words/s: an older,
      low, even lecture read (takes 143 / 132 Hz)
    b Phil Chenevert (A Spaceship Named McGuire, ch. 1, Garrett; cut at 66 s): 141 Hz, IQR 38 Hz, 2.4 words/s: a mid,
      clipped American narration (takes 152 / 144 Hz)
  Harker (the mutineers' flight lead, 115-135 Hz or a clearly different accent from Rook's 146-188 Hz):
    a Ted Delorme (The Mysterious Stranger, ch. 1, Twain; cut at 66 s): 110 Hz, IQR 31 Hz, 2.7 words/s: a dry,
      rueful American voice (takes 109 / 119 Hz)
    b David Clarke (The Count of Monte Cristo, ch. 1, Dumas; cut at 72 s): 104 Hz, IQR 20 Hz, 2.5 words/s: a hard,
      level English voice (the accent apart from Rook; takes 121 / 117 Hz)
(Also measured and passed over: Bob Neufeld, 153 Hz, and Don W. Jenkins, 154 Hz, above the range once Chatterbox
lifts them; Barry Eads, 142 Hz, a take jumped to 182 Hz; Lars Rolander, 95 Hz (takes 107 Hz, a Swedish accent, under
Harker's range); Kevin Green, 98 Hz with a wide range; Simon Evers, 116 Hz, 3.0 words/s and a 1.4 s pause; Peter
Yearsley, 80 Hz; Tom Weiss, 148 Hz and a wide range; Andy Minter's Zenda chapter, 215 Hz in character voices.)

Vorne's pitch (round 61 C.9; the README asks for up to -3 semitones toward about 105 Hz, kept only if natural): tried
on this chunk with seeds 100-102: the takes' own shift (rubberband, formants preserved) at -1.5 / -2 / -3 st lowers
the median from 114-122 Hz to 104-110 / 102-107 / 98-101 Hz but moves the spectral centroid down 10 / 15 / 18 % and
halves to thirds the spectral flatness (formant smearing and a dulled, phasey timbre); the clip's shift keeps both
(centroid within 6 %, flatness as unshifted): -2 st gives 100-106 Hz, -3 st 84-96 Hz but one take in six came out
unvoiced (a whisper, 9 voiced frames) and others creaky. -2 st on the clip is the lowest natural one.

Setup: the Chatterbox venv of round 18 (~/.cache/tv-tts/venv-chatterbox, see tts_r18.py); Whisper (faster-whisper)
and librosa from the Dia venv (~/.cache/tv-tts/venv-dia), for the read-back and the pitch check.
Rerun: python3 tools/concept/audio/tts_r62.py --refs        (cut the missing clips, Vorne's pitched clip, transcripts)
       python3 tools/concept/audio/tts_r62.py --pitch       (Vorne's comparison: generate, post, read back)
       python3 tools/concept/audio/tts_r62.py --auditions   (the audition files from the rendered lines)
Raw WAVs go to ~/.cache/tv-tts/raw/r62/. Seeds are fixed, but GPU sampling is not bit-exact.
"""
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REFDIR = ROOT / "design" / "audio" / "voice" / "refs"
DEST = ROOT / "design" / "audio" / "voice" / "concept"
ASSETS = ROOT / "assets" / "voice"
BASE = Path.home() / ".cache" / "tv-tts"
RAW = BASE / "raw" / "r62"
WHISPER_PY = BASE / "venv-dia" / "bin" / "python"

# (speaker, variant): (archive.org item, chapter file, cut start s, reader, licence)
REFS = {
    ("ceres-control", "a"): ("sidelights_relativity_pa_librivox", "sidelightsonrelativity_1_einstein_64kb.mp3", 66,
                             "Paul Adams", "Public Domain Mark 1.0"),
    ("ceres-control", "b"): ("spaceshipnamedmcguire_pc_librivox", "spaceshipnamedmcguire_1_garrett_64kb.mp3", 66,
                             "Phil Chenevert", "CC0 1.0"),
    ("harker", "a"): ("mysterious_stranger_td_librivox", "stranger_01_twain_64kb.mp3", 66, "Ted Delorme",
                      "Public Domain Mark 1.0"),
    ("harker", "b"): ("count_montecristo_1308_librivox", "countofmontecristo_001_dumas_64kb.mp3", 72, "David Clarke",
                      "CC0 1.0"),
}
VORNE_SHIFT = -2.0  # semitones on the clip
VORNE = {"exaggeration": 0.6, "cfg_weight": 0.4, "temperature": 0.7}  # neutral + the speaker table's shift
CHUNK = ("From this hour the Jovian Ascendancy governs itself, and it does not stand alone: the Vrell are our "
         "partners.")
NAMES = "Ceres, Vesta, Callisto, Helix, Vorne, Harker, Rook, Aegis, Jovian Ascendancy, Vrell."
SEEDS = (100, 101, 102)
# variant -> seed: the take Whisper read back right with no pause over 0.8 s and no octave jump (see --pitch's log): both
# seed 100, a 90 Hz (IQR 18, p95 106; seeds 101/102: 96 / 101 Hz), b 115 Hz (IQR 25, p95 140; 114 / 122 Hz), every
# take read back word for word
PINS = {"a": 100, "b": 100}
# speaker -> variant -> the keys of its two audition lines (tools/art/voice.py's keys for each ref; a in assets/, b in
# concept/voice-<speaker>-r62-b/)
AUDITION = {
    # step B4b: the t=101, t=68 (now 70.5) and t=133 lines trimmed to one HUD page and re-rendered (new keys)
    "ceres-control": {"a": ["071a2bca99db", "2a82a54ba3f0"], "b": ["f8ba5ed91356", "ba32c66c3b1b"]},
    "harker": {"a": ["35f4cf2532d8", "d12fe51a5397"], "b": ["b21cb3f53bd3", "c2b4d483e96d"]},
}
GAP = 1.0


def ref(speaker, variant):
    return REFDIR / f"ref-{speaker}-r62-{variant}.wav"


def raw(variant, seed):
    return RAW / f"vorne-pitch-{variant}-s{seed}.wav"


def refs():
    """Cut the missing reference clips, make Vorne's pitched clip, write the transcripts."""
    for (speaker, variant), (item, name, start, _, _) in REFS.items():
        out = ref(speaker, variant)
        if not out.exists():
            subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-ss", str(start), "-t", "12",
                            "-i", f"https://archive.org/download/{item}/{name}", "-ac", "1", "-ar", "24000",
                            str(out)], check=True)
    vorne = REFDIR / "ref-vorne.wav"
    if not vorne.exists():
        factor = 2 ** (VORNE_SHIFT / 12)
        subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-i", str(REFDIR / "ref-vorne-r60-a.wav"),
                        "-af", f"rubberband=pitch={factor:.6f}:formant=preserved:pitchq=quality", "-ac", "1",
                        "-ar", "24000", str(vorne)], check=True)
    run_whisper("--transcribe-refs")


def worker():
    import random

    import numpy as np
    import soundfile as sf
    import torch
    from chatterbox.tts import ChatterboxTTS
    model = ChatterboxTTS.from_pretrained(device="cuda")
    RAW.mkdir(parents=True, exist_ok=True)
    clips = {"a": REFDIR / "ref-vorne.wav", "b": REFDIR / "ref-vorne-r60-a.wav"}
    for variant, clip in clips.items():
        for seed in SEEDS:
            if raw(variant, seed).exists():
                continue
            for f in (random.seed, np.random.seed, torch.manual_seed):
                f(seed)
            wav = model.generate(CHUNK, audio_prompt_path=str(clip), **VORNE)
            sf.write(str(raw(variant, seed)), wav.cpu().numpy().reshape(-1).astype("float32"), model.sr)
            print("vorne", variant, "seed", seed, flush=True)


def whisper(mode):
    """In the Dia venv: the clips' transcripts and pitches, or the raw takes' and finished files' read-back with
    their median pitch (librosa pyin), voiced share and longest pause."""
    import librosa
    import numpy as np
    from faster_whisper import WhisperModel
    asr = WhisperModel("base.en", device="cpu", compute_type="int8")

    def text(path, prompt=None):
        segs, _ = asr.transcribe(str(path), language="en", initial_prompt=prompt, word_timestamps=True)
        segs = list(segs)
        words = [w for seg in segs for w in seg.words]
        gap = max((b.start - a.end for a, b in zip(words, words[1:])), default=0.0)
        span = words[-1].end - words[0].start if words else 1.0
        return (" ".join(seg.text.strip() for seg in segs)
                + f"  [{len(words) / span:.1f} words/s, longest pause {gap:.2f} s]")

    def pitch(path):
        y, sr = librosa.load(str(path), sr=24000)
        f0, voiced, _ = librosa.pyin(y, fmin=55, fmax=400, sr=sr)
        f = f0[voiced & ~np.isnan(f0)]
        iqr = np.percentile(f, 75) - np.percentile(f, 25)
        return f"{np.median(f):.0f} Hz (IQR {iqr:.0f}, p95 {np.percentile(f, 95):.0f}, voiced {len(f)})"

    if mode == "--transcribe-refs":
        for clip in [ref(s, v) for s, v in REFS] + [REFDIR / "ref-vorne.wav"]:
            t = text(clip)
            if not clip.with_suffix(".txt").exists():
                clip.with_suffix(".txt").write_text(t.split("  [")[0] + "\n")
            print(clip.name, pitch(clip), ":", t)
    else:
        for variant in ("a", "b"):
            for seed in SEEDS:
                print("raw", raw(variant, seed).stem, pitch(raw(variant, seed)), ":", text(raw(variant, seed), NAMES))
        for path in sorted(DEST.glob("vorne-pitch-r62-*.ogg")) + sorted(DEST.glob("voice-*-r62-*.ogg")):
            print("read back", path.name, pitch(path), ":", text(path, NAMES))


def run_whisper(mode):
    subprocess.run([str(WHISPER_PY), __file__, "--whisper", mode], check=True,
                   env=dict(os.environ, HF_HOME=str(BASE / "hf")))


def _tools():
    sys.path.insert(0, str(HERE))
    sys.path.insert(0, str(ROOT / "tools" / "art"))
    import tts_r18
    import voice
    return tts_r18._synth(), voice


def pitch_post():
    s, voice = _tools()
    for variant, seed in PINS.items():
        x = s.decode(raw(variant, seed))[0]
        x = voice.trim(x, s.SR)
        y = voice.enemy(x, s)
        tags = {"COMMENT": f"tools/concept/audio/tts_r62.py vorne pitch {variant} seed {seed}"}
        s.write_ogg(DEST / f"vorne-pitch-r62-{variant}.ogg", y, quality=4, tags=tags)
        print("vorne-pitch", variant, round(y.shape[1] / s.SR, 2), "s", round(s.lufs(y)[0], 1), "LUFS", flush=True)


def auditions():
    import numpy as np
    s, _ = _tools()
    for speaker, variants in AUDITION.items():
        for variant, keys in variants.items():
            where = ASSETS / speaker if variant == "a" else DEST / f"voice-{speaker}-r62-{variant}"
            parts = []
            for key in keys:
                parts += [s.decode(where / f"{key}.ogg")[:1], np.zeros((1, int(GAP * s.SR)))]
            y = np.concatenate(parts[:-1], axis=1)
            tags = {"COMMENT": f"tools/concept/audio/tts_r62.py {speaker} {variant} " + " ".join(keys)}
            s.write_ogg(DEST / f"voice-{speaker}-r62-{variant}.ogg", y, quality=4, tags=tags)
            print(speaker, variant, round(y.shape[1] / s.SR, 2), "s", flush=True)


def main(args):
    if args[:1] == ["--worker"]:
        worker()
    elif args[:1] == ["--whisper"]:
        whisper(args[1])
    elif args[:1] == ["--refs"]:
        refs()
    elif args[:1] == ["--pitch"]:
        env = dict(os.environ, HF_HOME=str(BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        subprocess.run([str(BASE / "venv-chatterbox" / "bin" / "python"), __file__, "--worker"], env=env, check=True)
        pitch_post()
        run_whisper("--read-back")
    elif args[:1] == ["--auditions"]:
        auditions()
        run_whisper("--read-back")
    else:
        print(__doc__)


if __name__ == "__main__":
    main(sys.argv[1:])
