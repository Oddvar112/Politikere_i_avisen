"""
Exports a HuggingFace sentiment model to ONNX format for use in Java/DJL.

Usage:
    pip install transformers torch onnx onnxruntime
    python export_model.py <huggingface-model-name> [output-subdir]

Examples:
    python export_model.py Kushtrim/norbert3-large-norsk-sentiment-sst2
    python export_model.py marcuskd/norbert2_sentiment_test1 sentiment-marcuskd

Default output: ~/.cache/news-analyzer/sentiment-model/
With subdir:    ~/.cache/news-analyzer/<subdir>/
"""
import shutil
import sys
import torch
from pathlib import Path
from transformers import AutoModelForSequenceClassification, AutoTokenizer

CACHE_ROOT = Path.home() / ".cache" / "news-analyzer"

if len(sys.argv) < 2:
    print("Usage: python export_model.py <huggingface-model-name> [output-subdir]")
    print("Example: python export_model.py Kushtrim/norbert3-large-norsk-sentiment-sst2")
    sys.exit(1)

model_name = sys.argv[1]
output_subdir = sys.argv[2] if len(sys.argv) > 2 else "sentiment-model"
OUTPUT_DIR = CACHE_ROOT / output_subdir
print(f"Model:  {model_name}")
print(f"Output: {OUTPUT_DIR}")

model = AutoModelForSequenceClassification.from_pretrained(model_name, trust_remote_code=True)

if model.config.num_labels < 2:
    print(f"\nError: Model has {model.config.num_labels} labels, need at least 2.")
    sys.exit(1)
if model.config.num_labels != 2:
    print(f"\nNote: Model has {model.config.num_labels} labels (id2label={model.config.id2label}). "
          f"Default Java translator handles only 2 — caller must provide a custom translator for >2 labels.")

tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
model.eval()

dummy = tokenizer("Dette er en test.", return_tensors="pt")
input_ids = dummy["input_ids"]
attention_mask = dummy["attention_mask"]

if OUTPUT_DIR.exists():
    shutil.rmtree(OUTPUT_DIR)
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

onnx_path = OUTPUT_DIR / "model.onnx"
print("Exporting to ONNX...")

with torch.no_grad():
    torch.onnx.export(
        model,
        (input_ids, attention_mask),
        str(onnx_path),
        input_names=["input_ids", "attention_mask"],
        output_names=["logits"],
        dynamic_axes={
            "input_ids": {0: "batch", 1: "sequence"},
            "attention_mask": {0: "batch", 1: "sequence"},
            "logits": {0: "batch"},
        },
        opset_version=14,
    )

tokenizer.save_pretrained(OUTPUT_DIR)

print(f"\nDone! {onnx_path} ({onnx_path.stat().st_size / 1024 / 1024:.0f} MB)")
print(f"Model is ready to use — no config changes needed.")
