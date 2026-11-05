#!/usr/bin/env python3
"""Concept round 84 (M11 "Act 7 - Beyond the Gate" design phase, step AUD-R, 2026-11-04): the two voice treatments of
the voice README's *Act 7 voices (draft)*, the Choir's Act 7 dress and Vorne's fused voice; pending the user's review.
Built on tools/art/voice.py (its worker, chunking, retries, Whisper timing, WORLD lowering and enemy filter) and
tools/concept/audio/tts_r18.py (the Choir's layering, radio filter b), as voice_r73.py and voice_r74.py.

Outputs (design/audio/voice/concept/), each a line list 1.2 s apart:
  choir-act7-r84-a.ogg: the Choir in its Act 7 dress, lines 1, 2, 13 and 16 of its Act 7 table: the round-18 layering
      (copies at -12, -5, 0 and +7 semitones, one reversed, a low drone, a 2.2 s reverb), radio filter b's band and
      saturation but NO hiss, crackle or clicks (the clean channel), except line 1 (the intro's page 3, through the
      gate) under filter b's static at noise step 1 (the hiss x1.33, the crackle's density x1.5); the Heart's lines
      (13, 16) fearful and slower: the take time-stretched to tempo 0.85 (pitch kept; 1.18x its length) and the copies'
      stagger widened from 0-45 ms to 0-80 ms; line 16's copies dropping out one by one over the line (the -12, +7, -5
      and the reversed copy fading in turn), so it ends in one voice.
  choir-act7-r84-b.ogg: the same takes in the Choir's built dress (Acts 1-6): tts_r18.choir() and the `distorted`
      filter (radio filter b with dropouts and more crackle), no stretch: the alternative of keeping the Choir as ever.
  vorne-fused-r84-a.ogg: Vorne's Level 48 lines 1, 5 and 9 in his fused voice as drafted: his take, then copies of it
      at -12 and -5 semitones (formants preserved, ffmpeg rubberband) 90 and 60 ms late through a 2.2 s reverb (35 %
      wet, the copies only) at -15 dB under him (line 1) and -10 dB (lines 5 and 9); then the enemy filter. Line 1 the
      neutral row on his theatrical base (0.6 / 0.4 / 0.7); line 5 the shout as round 73's treatment (his clip -4
      semitones, the shout row 1.3 / 0.2 / 0.8 from seed 105, cut after the last word, WORLD-lowered to about 110 Hz
      when above 130 Hz); line 9 neutral, cut by the dash rule at "to" into static and a dead channel (tts_r32's
      cut_off(): the copies are cut with him).
  vorne-fused-r84-b.ogg: a stronger fusion on the same takes: the copies 140 and 100 ms late through a 3.2 s reverb
      (50 % wet) at -8 dB (line 1) and -4 dB (lines 5 and 9), plus a third copy at -19 semitones (an octave and a
      fifth down) at -6 dB under the others.

Setup: tools/art/voice.py's (Chatterbox in ~/.cache/tv-tts/venv-chatterbox on the GPU, Whisper in the Dia venv, WORLD
in ~/.cache/tv-tts/venv-world). Raw takes go to ~/.cache/tv-tts/raw/r84/.
Rerun: python3 tools/concept/audio/voice_r84.py            (render the takes, time them, post-process)
       python3 tools/concept/audio/voice_r84.py --search   (then search the SEARCH keys' seeds, pin, post-process)
       python3 tools/concept/audio/voice_r84.py --post     (post-process only)
Seeds are fixed, but GPU sampling is not bit-exact across drivers.
"""
import json
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
sys.path.insert(0, str(HERE))
import voice as V  # noqa: E402

DEST = ROOT / "design" / "audio" / "voice" / "concept"
RAW = V.BASE / "raw" / "r84"
GAP = 1.2
CHOIR_ROW = {"exaggeration": 0.3, "cfg_weight": 0.3, "temperature": 0.7}
VORNE_NEUTRAL = {"exaggeration": 0.6, "cfg_weight": 0.4, "temperature": 0.7}   # neutral + his theatrical shift
VORNE_SHOUT = {"exaggeration": 1.3, "cfg_weight": 0.2, "temperature": 0.8}     # the shout row + his shift
LOW_SHIFT = -4.0
TARGET_HZ = 110.0

CHOIR = [
    ("choir-1", "…small ones… stay behind your door… do not follow us in…"),
    ("choir-2", "…small bright one… why do you stand in the door…"),
    ("choir-13", "…we sang so it would not hear us… you are so loud… so loud…"),
    ("choir-16", "…it is behind us… it has always been behind us…"),
]
VORNE = [
    ("vorne-1", "Lancer. You followed me all the way in. How loyal. How very Coalition.", VORNE_NEUTRAL, 101),
    ("vorne-5", "Get out of my head!", VORNE_SHOUT, 105),
    ("vorne-9", "Do you know what's coming after them, pilot? Do you have any idea what I was trying to,",
     VORNE_NEUTRAL, 101),
]
FEARFUL = {"choir-13", "choir-16"}


# Takes searched over seeds (each seed one take, no retries; the pin is the first, in seed order, that Whisper reads
# back word for word): the first renders' Level 48 shout read "JIT OUT OF MY HEY!" (seed 112, after seven takes
# rejected as short) and line 13 added a third "so loud".
SEARCH = {"vorne-5": range(120, 136), "choir-13": range(8420, 8432)}


def patch():
    V.RAW = RAW
    V.TIMINGS = RAW / "timings.json"
    V.treated_ref = lambda line: Path(line["clip"])
    for key, seeds in SEARCH.items():
        for seed in seeds:
            V.TREATMENTS[f"{key}-s{seed}"] = {"takes": 1}


def _words(text):
    import re
    return [w for w in re.sub(r"[^\w' ]", " ", text.lower()).split()]


def search():
    """Every SEARCH seed rendered once, timed by Whisper; the first word-for-word take of each key becomes its take."""
    import shutil
    base = {j["key"]: j for j in jobs()}
    todo = [dict(base[key], key=f"{key}-s{seed}", pin=seed) for key, seeds in SEARCH.items() for seed in seeds]
    jp = RAW / "search-jobs.json"
    jp.write_text(json.dumps(todo, indent=1))
    env = dict(os.environ, HF_HOME=str(V.BASE / "hf"), TOKENIZERS_PARALLELISM="false",
               TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
    subprocess.run([str(V.BASE / "venv-chatterbox" / "bin" / "python"), __file__, "--worker", str(jp)], env=env,
                   check=True)
    keys = RAW / "search-keys.json"
    keys.write_text(json.dumps([j["key"] for j in todo]))
    subprocess.run([str(V.WHISPER_PY), __file__, "--whisper", str(keys)], check=True,
                   env=dict(os.environ, HF_HOME=str(V.BASE / "hf")))
    timings = json.loads((RAW / "timings.json").read_text())
    for key, seeds in SEARCH.items():
        want = _words(base[key]["spoken"])
        pick = None
        for seed in seeds:
            heard = _words(" ".join(w for w, _, _ in timings[f"{key}-s{seed}"]))
            print(f"  {key} seed {seed}: {' '.join(heard)}", flush=True)
            if pick is None and heard == want:
                pick = seed
        print(f"  {key}: pinned seed {pick}", flush=True)
        if pick is not None:
            shutil.copyfile(RAW / f"{key}-s{pick}.wav", RAW / f"{key}.wav")
            timings[key] = timings[f"{key}-s{pick}"]
    (RAW / "timings.json").write_text(json.dumps(timings, indent=1))


def jobs():
    out = []
    for key, text in CHOIR:
        out.append(dict(key=key, voice="choir", source=f"r84 {key}", spoken=text, text=text, pin=8400 + int(key[6:]),
                        clip=str(V.REFDIR / "ref-varga.wav"), **CHOIR_ROW))
    for key, text, row, pin in VORNE:
        clip = RAW / "ref-vorne-low.wav" if key == "vorne-5" else V.REFDIR / "ref-vorne.wav"
        out.append(dict(key=key, voice="vorne", source=f"r84 {key}", spoken=text, text=text, pin=pin, clip=str(clip),
                        **row))
    return out


def rubberband(x, sr, semis=0.0, tempo=1.0, formants=True):
    """Pitch (semitones) and/or tempo through ffmpeg's rubberband, mono float in and out."""
    import numpy as np
    opts = [f"pitch={2 ** (semis / 12):.6f}", f"tempo={tempo:.6f}", "pitchq=quality"]
    if formants:
        opts.append("formant=preserved")
    r = subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-f", "f32le", "-ar", str(sr), "-ac", "1",
                        "-i", "-", "-af", "rubberband=" + ":".join(opts), "-f", "f32le", "-"],
                       input=x.astype("<f4").tobytes(), capture_output=True, check=True).stdout
    return np.frombuffer(r, dtype="<f4").astype(float)


# ---------------------------------------------------------------- the Choir's Act 7 dress

def choir7(x, s, stagger=0.045, drop_out=False):
    """tts_r18.choir() with the stagger scaled to `stagger` s and, for the last line, the copies fading out in turn
    (-12, +7, -5, then the reversed one), the unshifted copy left alone."""
    import numpy as np
    import tts_r18
    sr = s.SR
    x = x / max(1e-9, np.max(np.abs(x)))
    n = len(x) + int(1.2 * sr)
    out = np.zeros(n)
    k = stagger / 0.045
    t = np.arange(len(x)) / len(x)

    def gate(order):  # fades out over the order-th quarter of the line (0 = first)
        if not drop_out:
            return 1.0
        a, b = 0.15 + 0.18 * order, 0.33 + 0.18 * order
        return np.clip((b - t) / (b - a), 0, 1)
    for semi, g, d, order in ((-12, 0.8, 0.0, 0), (-5, 0.55, 0.015, 2), (0, 0.45, 0.03, None), (7, 0.3, 0.045, 1)):
        y = tts_r18._ffmpeg_pitch(x, 2 ** (semi / 12), sr) * g
        if order is not None:
            y = y * gate(order)
        elif drop_out:
            y = y * 1.6  # the one voice left carries the line
        i = int(d * k * sr)
        out[i:i + len(y)] += y
    out[:len(x)] += x[::-1] * 0.22 * gate(3)
    tt = np.arange(n) / sr
    rng = np.random.default_rng(18)
    drone = (s.sine(55.0, n) + 0.6 * s.sine(82.4, n) * (0.6 + 0.4 * s.sine(0.23, n))
             + 0.5 * s.onepole_lp(s.noise(n, rng), 300)) * 0.12
    env = np.minimum(1, tt / 0.4) * np.minimum(1, (tt[-1] - tt) / 0.6)
    out = out + drone * env
    wet = s.reverb(np.vstack([out, out]), seconds=2.2, mix=0.35, seed=18)[0][:n]
    return wet / max(1e-9, np.max(np.abs(wet)))


def channel(x, s, step=None):
    """Radio filter b's band and saturation; `step` None: clean (no hiss, crackle or clicks, 5 ms fades); a step n:
    filter b's hiss x1.33^n and crackle density x(1 + n/2), its clicks in and out."""
    import numpy as np
    sr = s.SR
    rng = np.random.default_rng(84)
    x = x / max(1e-9, np.max(np.abs(x))) * 0.8
    pad = int(0.15 * sr)
    x = np.concatenate([np.zeros(pad), x, np.zeros(pad)])
    n = len(x)
    y = x
    if step is not None:
        flutter = 1 + 0.15 * s.sine(0.7, n) + 0.1 * s.sine(3.1, n)
        y = y + s.noise(n, rng) * 0.019 * 1.33 ** step * flutter
        y = y + (rng.random(n) < 0.001 * (1 + step / 2)) * rng.uniform(-1, 1, n) * 0.11
    for _ in range(2):
        y = s.svf(y, 300, mode="hp")
        y = s.svf(y, 3400, mode="lp")
    y = s.saturate(y * 1.6, 2.0) * 0.8
    if step is not None:
        for at in (int(0.03 * sr), n - int(0.05 * sr)):
            m = int(0.004 * sr)
            click = s.noise(m, rng) * np.exp(-np.arange(m) / (0.0008 * sr)) * 0.6
            y[at:at + m] += s.svf(click, 2500, q=1.2, mode="bp") * 2.5
    else:
        f = int(0.005 * sr)
        y[:f] *= np.linspace(0, 1, f)
        y[-f:] *= np.linspace(1, 0, f)
    return s.master(y[None, :], target_lufs=-16.0, ceiling_db=-1.5)


# ---------------------------------------------------------------- Vorne's fused voice

def fuse(x, s, copies, db_under, delays, rev):
    """His take with the Choir's lower copies a breath behind it: each copy (semitones, extra dB) at `db_under` dB
    under him, `delays` s late, the copies alone through a reverb (seconds, wet)."""
    import numpy as np
    sr = s.SR
    x = x / max(1e-9, np.max(np.abs(x)))
    tail = int(1.5 * sr)
    bus = np.zeros(len(x) + tail)
    for (semi, extra), d in zip(copies, delays):
        y = rubberband(x, sr, semi)
        y = y / max(1e-9, np.sqrt(np.mean(y ** 2))) * np.sqrt(np.mean(x ** 2)) * 10 ** ((-db_under + extra) / 20)
        i = int(d * sr)
        m = min(len(y), len(bus) - i)
        bus[i:i + m] += y[:m]
    wet = s.reverb(np.vstack([bus, bus]), seconds=rev[0], mix=rev[1], seed=84)[0][:len(bus)]
    out = wet.copy()
    out[:len(x)] += x
    return out


VARIANTS = {  # copies (semitones, extra dB), dB under him (line 1, lines 5 and 9), delays, reverb
    "a": dict(copies=[(-12, 0.0), (-5, 0.0)], under=(15.0, 10.0), delays=(0.09, 0.06), rev=(2.2, 0.35)),
    "b": dict(copies=[(-12, 0.0), (-5, 0.0), (-19, -6.0)], under=(8.0, 4.0), delays=(0.14, 0.10, 0.12),
              rev=(3.2, 0.5)),
}


# ---------------------------------------------------------------- post

def pitch_world(path):
    """In the WORLD venv: the median f0 (Hz) of the voiced frames of a WAV (harvest)."""
    import numpy as np
    import pyworld as pw
    import soundfile as sf
    x, sr = sf.read(path)
    x = (x.mean(1) if x.ndim > 1 else x).astype(np.float64)
    f0, _ = pw.harvest(x, sr, f0_floor=60, f0_ceil=600, frame_period=5.0)
    print(json.dumps(float(np.median(f0[f0 > 0]))))


def post():
    import numpy as np
    import tts_r18
    import tts_r32
    s = tts_r18._synth()
    timings = json.loads((RAW / "timings.json").read_text())
    for k, w in timings.items():
        print(f"  heard {k}: {' '.join(x[0] for x in w)}", flush=True)

    def raw(key):
        return s.decode(RAW / f"{key}.wav")[0]

    def write(name, parts):
        gap = np.zeros((1, int(GAP * s.SR)))
        y = np.concatenate(sum([[p, gap] for p in parts], [])[:-1], axis=1)
        s.write_ogg(DEST / f"{name}.ogg", y, quality=4,
                    tags={"COMMENT": f"tools/concept/audio/voice_r84.py {name}", "SOURCE": "tools/concept/audio/voice_r84.py"})
        print(name, round(y.shape[1] / s.SR, 2), "s", round(s.lufs(y)[0], 1), "LUFS", flush=True)

    # the Choir: a the Act 7 dress, b the built dress
    a, b = [], []
    for key, _ in CHOIR:
        x = raw(key)
        b.append(V.distorted(tts_r18.radio(tts_r18.choir(x)), s))
        fear = key in FEARFUL
        xa = rubberband(x, s.SR, 0.0, 0.85, formants=False) if fear else x
        layered = choir7(xa, s, 0.08 if fear else 0.045, drop_out=key == "choir-16")
        a.append(channel(layered, s, step=1 if key == "choir-1" else None))
    write("choir-act7-r84-a", a)
    write("choir-act7-r84-b", b)

    # Vorne: line 5 cut and lowered as round 73's treatment; line 9 cut at "to"
    line5 = dict(key="vorne-5", spoken=VORNE[1][1])
    V.TREATMENTS["vorne-5"] = {"cut": True}
    x5 = V.treat(line5, raw("vorne-5"), s)
    tmp = RAW / "vorne-5-cut.wav"
    s.write_wav(tmp, x5 / max(1e-9, np.max(np.abs(x5))) * 0.8)
    hz = json.loads(subprocess.run([str(V.WORLD_PY), __file__, "--pitch", str(tmp)], check=True,
                                   capture_output=True, text=True).stdout.strip().splitlines()[-1])
    if hz > 130:
        f = TARGET_HZ / hz
        dst = RAW / "vorne-5-world.wav"
        subprocess.run([str(V.WORLD_PY), str(ROOT / "tools" / "art" / "voice.py"), "--world", str(tmp), str(dst),
                        f"{f:.6f}"], check=True)
        x5 = s.decode(dst)[0]
        print(f"  vorne-5: {hz:.0f} Hz, WORLD x{f:.3f} ({12 * np.log2(f):+.1f} st)", flush=True)
    else:
        print(f"  vorne-5: {hz:.0f} Hz, not lowered", flush=True)
    words9 = timings["vorne-9"]
    ends = [e for w, _, e in words9 if w.strip(",.?!—").lower() == "to"]
    end9 = ends[-1] if ends else words9[-1][2]
    print(f"  vorne-9: cut at {end9:.2f} s ({'to' if ends else 'its last word'})", flush=True)
    takes = {"vorne-1": raw("vorne-1"), "vorne-5": x5, "vorne-9": raw("vorne-9")}
    for v, cfg in VARIANTS.items():
        parts = []
        for key, under in (("vorne-1", cfg["under"][0]), ("vorne-5", cfg["under"][1]), ("vorne-9", cfg["under"][1])):
            y = fuse(takes[key], s, cfg["copies"], under, cfg["delays"], cfg["rev"])
            if key == "vorne-9":
                y = tts_r32.cut_off(y, s.SR, end9, np.random.default_rng(32), s)
                parts.append(V.enemy(y, s)[:, :int((0.15 + end9 + tts_r32.STATIC + tts_r32.DEAD) * s.SR)])
            else:
                parts.append(V.enemy(y, s))
        write(f"vorne-fused-r84-{v}", parts)


def main(args):
    patch()
    if args[:1] == ["--worker"]:
        RAW.mkdir(parents=True, exist_ok=True)
        V.worker(args[1])
        return
    if args[:1] == ["--whisper"]:
        V.whisper(args[1])
        return
    if args[:1] == ["--pitch"]:
        pitch_world(args[1])
        return
    if args[:1] == ["--search"]:
        search()
        post()
        return
    if "--post" not in args:
        RAW.mkdir(parents=True, exist_ok=True)
        low = RAW / "ref-vorne-low.wav"
        if not low.exists():
            subprocess.run(["ffmpeg", "-nostdin", "-loglevel", "error", "-y", "-i", str(V.REFDIR / "ref-vorne.wav"),
                            "-af", f"rubberband=pitch={2 ** (LOW_SHIFT / 12):.6f}:formant=preserved:pitchq=quality:"
                            "window=long", "-ac", "1", "-ar", "24000", str(low)], check=True)
        jp = RAW / "jobs.json"
        jp.write_text(json.dumps(jobs(), indent=1))
        env = dict(os.environ, HF_HOME=str(V.BASE / "hf"), TOKENIZERS_PARALLELISM="false",
                   TORCH_FORCE_NO_WEIGHTS_ONLY_LOAD="1")
        subprocess.run([str(V.BASE / "venv-chatterbox" / "bin" / "python"), __file__, "--worker", str(jp)], env=env,
                       check=True)
        keys = RAW / "keys.json"
        keys.write_text(json.dumps([j["key"] for j in jobs()]))
        subprocess.run([str(V.WHISPER_PY), __file__, "--whisper", str(keys)], check=True,
                       env=dict(os.environ, HF_HOME=str(V.BASE / "hf")))
    post()


if __name__ == "__main__":
    main(sys.argv[1:])
