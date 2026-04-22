"""
Sets up MaltParser with a Norwegian Bokmaal dependency model.
1. Downloads MaltParser 1.9.2
2. Downloads Norwegian UD Treebank (Bokmaal)
3. Trains a MaltParser model on the treebank

Usage:
    python setup_maltparser.py
"""

import shutil
import subprocess
import sys
import urllib.request
import zipfile
from pathlib import Path

CACHE_DIR = Path.home() / ".cache" / "news-analyzer"
MALTPARSER_DIR = CACHE_DIR / "maltparser"
MALTPARSER_JAR = MALTPARSER_DIR / "maltparser-1.9.2" / "maltparser-1.9.2.jar"
MALTPARSER_ZIP_URL = "http://maltparser.org/dist/maltparser-1.9.2.zip"

TREEBANK_DIR = CACHE_DIR / "ud-norwegian-bokmaal"
TREEBANK_ZIP_URL = "https://github.com/UniversalDependencies/UD_Norwegian-Bokmaal/archive/refs/heads/master.zip"
TRAIN_FILE = TREEBANK_DIR / "UD_Norwegian-Bokmaal-master" / "no_bokmaal-ud-train.conllu"

MODEL_NAME = "norsk-bokmaal"
MODEL_FILE = MALTPARSER_DIR / f"{MODEL_NAME}.mco"


def download(url: str, dest: Path, description: str):
    if dest.exists():
        print(f"  Already downloaded: {dest}")
        return
    print(f"  Downloading {description}...")
    dest.parent.mkdir(parents=True, exist_ok=True)
    urllib.request.urlretrieve(url, dest)
    print(f"  Done: {dest} ({dest.stat().st_size / 1024 / 1024:.1f} MB)")


def main():
    print("=== MaltParser setup for Norwegian Bokmaal ===\n")

    # 1. Download and extract MaltParser
    print("1. MaltParser 1.9.2")
    maltparser_zip = CACHE_DIR / "maltparser-1.9.2.zip"
    download(MALTPARSER_ZIP_URL, maltparser_zip, "MaltParser ZIP")

    if not MALTPARSER_JAR.exists():
        print("  Extracting MaltParser...")
        MALTPARSER_DIR.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(maltparser_zip, 'r') as z:
            z.extractall(MALTPARSER_DIR)
        print(f"  JAR: {MALTPARSER_JAR}")

    if not MALTPARSER_JAR.exists():
        print(f"  Error: Could not find {MALTPARSER_JAR}")
        for p in MALTPARSER_DIR.rglob("*.jar"):
            print(f"    {p}")
        sys.exit(1)

    # 2. Download Norwegian UD Treebank
    print("\n2. Norwegian UD Treebank (Bokmaal)")
    treebank_zip = CACHE_DIR / "ud-nb-master.zip"
    download(TREEBANK_ZIP_URL, treebank_zip, "UD Norwegian Bokmaal treebank")

    if not TRAIN_FILE.exists():
        print("  Extracting treebank...")
        TREEBANK_DIR.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(treebank_zip, 'r') as z:
            z.extractall(TREEBANK_DIR)

    if not TRAIN_FILE.exists():
        print(f"  Error: Could not find {TRAIN_FILE}")
        for p in TREEBANK_DIR.rglob("*.conllu"):
            print(f"    {p}")
        sys.exit(1)

    token_count = sum(1 for line in open(TRAIN_FILE, encoding="utf-8")
                      if line.strip() and not line.startswith("#"))
    print(f"  Training data: {token_count:,} tokens")

    # 3. Train MaltParser model
    print(f"\n3. Training MaltParser model '{MODEL_NAME}'...")
    if MODEL_FILE.exists():
        print(f"  Model already exists: {MODEL_FILE}")
    else:
        cmd = [
            "java", "-jar", str(MALTPARSER_JAR),
            "-c", MODEL_NAME,
            "-i", str(TRAIN_FILE),
            "-if", "conllu",
            "-m", "learn",
            "-w", str(MALTPARSER_DIR)
        ]
        print(f"  Command: {' '.join(cmd)}")
        print("  This may take a few minutes...")
        result = subprocess.run(cmd, cwd=str(MALTPARSER_DIR), capture_output=True, text=True)
        if result.returncode != 0:
            print(f"  STDOUT:\n{result.stdout}")
            print(f"  STDERR:\n{result.stderr}")
            sys.exit(1)
        if not MODEL_FILE.exists():
            for mco in MALTPARSER_DIR.rglob("*.mco"):
                shutil.copy2(mco, MODEL_FILE)
                break

    if not MODEL_FILE.exists():
        print("  Error: Model was not created.")
        sys.exit(1)

    print(f"\nDone!")
    print(f"MaltParser JAR: {MALTPARSER_JAR}")
    print(f"Model:          {MODEL_FILE}")


if __name__ == "__main__":
    main()
