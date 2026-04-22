"""
Exports a HuggingFace sentiment model to ONNX format for use in Java/DJL.

Usage:
    pip install transformers torch onnx onnxruntime
    python export_model.py <huggingface-model-name>

Example:
    python export_model.py Kushtrim/norbert3-large-norsk-sentiment-sst2

The model is saved to ~/.cache/news-analyzer/sentiment-model/
"""
import shutil
import sys
import torch
from pathlib import Path
from transformers import AutoModelForSequenceClassification, AutoTokenizer

OUTPUT_DIR = Path.home() / ".cache" / "news-analyzer" / "sentiment-model"

if len(sys.argv) < 2:
    print("Usage: python export_model.py <huggingface-model-name>")
    print("Example: python export_model.py Kushtrim/norbert3-large-norsk-sentiment-sst2")
    sys.exit(1)

model_name = sys.argv[1]
print(f"Model:  {model_name}")
print(f"Output: {OUTPUT_DIR}")

model = AutoModelForSequenceClassification.from_pretrained(model_name, trust_remote_code=True)

if model.config.num_labels != 2:
    print(f"\nError: Model has {model.config.num_labels} labels, expected 2 (negative/positive).")
    sys.exit(1)

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
