"""
Setup script for news-analyzer dependencies.

Usage:
    python setup.py                                        # setup everything
    python setup.py maltparser                             # only MaltParser
    python setup.py sentiment                              # export sentiment model (prompts for name)
    python setup.py sentiment <huggingface-model-name>     # export specific sentiment model
"""

import subprocess
import sys
from pathlib import Path

SETUP_DIR = Path(__file__).parent


def run_maltparser():
    print("=== MaltParser ===\n")
    subprocess.run([sys.executable, str(SETUP_DIR / "setup_maltparser.py")], check=True)


def run_sentiment(model_name: str):
    print("\n=== Sentiment model ===\n")
    subprocess.run([sys.executable, str(SETUP_DIR / "export_model.py"), model_name], check=True)


def main():
    args = sys.argv[1:]

    if not args:
        run_maltparser()
        model_name = input("\nEnter HuggingFace sentiment model name: ").strip()
        if not model_name:
            print("No model name provided, skipping sentiment setup.")
            return
        run_sentiment(model_name)

    elif args[0] == "maltparser":
        run_maltparser()

    elif args[0] == "sentiment":
        if len(args) > 1:
            model_name = args[1]
        else:
            model_name = input("Enter HuggingFace sentiment model name: ").strip()
            if not model_name:
                print("No model name provided.")
                sys.exit(1)
        run_sentiment(model_name)

    else:
        print(f"Unknown command: {args[0]}")
        print("Usage: python setup.py [maltparser | sentiment <model-name>]")
        sys.exit(1)


if __name__ == "__main__":
    main()
