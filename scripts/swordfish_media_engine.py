#!/usr/bin/env python3
"""
Swordfish Ubuntu Edition - Media Engine & CLI Pipeline
Integrated with yt-dlp, aria2c, mediainfo, ffmpeg, and AtomicParsley.
"""

import sys
import subprocess
import json
import os
import shutil

def check_dependencies():
    tools = {
        "yt-dlp": shutil.which("yt-dlp") or "/usr/local/bin/yt-dlp",
        "ffmpeg": shutil.which("ffmpeg") or "/usr/bin/ffmpeg",
        "aria2c": shutil.which("aria2c") or "/usr/bin/aria2c",
        "mediainfo": shutil.which("mediainfo") or "/usr/bin/mediainfo",
        "atomicparsley": shutil.which("AtomicParsley") or "/usr/bin/AtomicParsley"
    }
    status = {}
    for name, path in tools.items():
        exists = os.path.exists(path) if path else False
        version = "unknown"
        if exists:
            try:
                if name == "yt-dlp":
                    res = subprocess.run([path, "--version"], capture_output=True, text=True, timeout=5)
                    version = res.stdout.strip()
                elif name == "aria2c":
                    res = subprocess.run([path, "--version"], capture_output=True, text=True, timeout=5)
                    version = res.stdout.split("\n")[0]
                elif name == "mediainfo":
                    res = subprocess.run([path, "--version"], capture_output=True, text=True, timeout=5)
                    version = res.stdout.split("\n")[0]
                elif name == "ffmpeg":
                    res = subprocess.run([path, "-version"], capture_output=True, text=True, timeout=5)
                    version = res.stdout.split("\n")[0].split("Copyright")[0].strip()
                elif name == "atomicparsley":
                    res = subprocess.run([path, "-v"], capture_output=True, text=True, timeout=5)
                    version = res.stdout.split("\n")[0] if res.stdout else "AtomicParsley 2021"
            except Exception as e:
                version = f"Error: {e}"
        status[name] = {
            "installed": exists,
            "path": path,
            "version": version
        }
    return status

def get_media_info(url_or_path):
    """Inspects media information using yt-dlp or mediainfo."""
    if url_or_path.startswith("http://") or url_or_path.startswith("https://"):
        cmd = ["yt-dlp", "--dump-single-json", "--no-warnings", url_or_path]
        try:
            res = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
            if res.returncode == 0:
                data = json.loads(res.stdout)
                return {
                    "success": True,
                    "title": data.get("title", "Unknown"),
                    "duration": data.get("duration", 0),
                    "uploader": data.get("uploader", "Unknown"),
                    "view_count": data.get("view_count", 0),
                    "formats": [
                        {
                            "format_id": f.get("format_id"),
                            "ext": f.get("ext"),
                            "resolution": f.get("resolution"),
                            "vcodec": f.get("vcodec"),
                            "acodec": f.get("acodec")
                        }
                        for f in data.get("formats", [])[-5:]
                    ]
                }
            else:
                return {"success": False, "error": res.stderr}
        except Exception as e:
            return {"success": False, "error": str(e)}
    else:
        # Local file
        if not os.path.exists(url_or_path):
            return {"success": False, "error": "File does not exist"}
        try:
            res = subprocess.run(["mediainfo", "--Output=JSON", url_or_path], capture_output=True, text=True, timeout=5)
            if res.returncode == 0:
                return {"success": True, "info": json.loads(res.stdout)}
            else:
                return {"success": False, "error": res.stderr}
        except Exception as e:
            return {"success": False, "error": str(e)}

def download_media(url, output_dir, extract_audio=False, use_aria2=True):
    """Downloads media stream with yt-dlp, optional audio extraction and aria2c acceleration."""
    os.makedirs(output_dir, exist_ok=True)
    out_template = os.path.join(output_dir, "%(title)s.%(ext)s")
    cmd = ["yt-dlp", "--no-warnings", "-o", out_template]
    
    if use_aria2 and shutil.which("aria2c"):
        cmd.extend(["--downloader", "aria2c", "--downloader-args", "aria2c:-x 4 -s 4 -k 1M"])
        
    if extract_audio:
        cmd.extend(["-x", "--audio-format", "mp3", "--audio-quality", "0"])
    else:
        cmd.extend(["-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"])
        
    cmd.append(url)
    try:
        proc = subprocess.run(cmd, capture_output=True, text=True, timeout=120)
        return {
            "success": proc.returncode == 0,
            "stdout": proc.stdout,
            "stderr": proc.stderr
        }
    except Exception as e:
        return {"success": False, "error": str(e)}

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "status":
        print(json.dumps(check_dependencies(), indent=2))
    elif len(sys.argv) > 2 and sys.argv[1] == "info":
        print(json.dumps(get_media_info(sys.argv[2]), indent=2))
    elif len(sys.argv) > 2 and sys.argv[1] == "download":
        url = sys.argv[2]
        audio_only = "--audio" in sys.argv
        out = sys.argv[3] if len(sys.argv) > 3 and not sys.argv[3].startswith("--") else "./downloads"
        print(json.dumps(download_media(url, out, extract_audio=audio_only), indent=2))
    else:
        print("Swordfish Media Engine CLI - Active tools:")
        print(json.dumps(check_dependencies(), indent=2))
