"""
=============================================================
  FLASK ML API — Serves severity predictions to Node.js
=============================================================
This is a tiny Python web server.
Node.js backend calls POST /predict → gets Low/Medium/High back.

Run:
  python flask_api.py
  → starts on http://localhost:5001

Dependencies:
  pip install flask flask-cors scikit-learn joblib pandas
=============================================================
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import re
import os

app = Flask(__name__)
CORS(app)   # allow requests from Node.js on a different port

# ── Load the saved model once at startup ──────────────────
MODEL_PATH = os.path.join(os.path.dirname(__file__), "saved_model", "severity_model.pkl")
LABEL_PATH = os.path.join(os.path.dirname(__file__), "saved_model", "label_map.pkl")

try:
    model    = joblib.load(MODEL_PATH)
    label_map = joblib.load(LABEL_PATH)   # {0: "Low", 1: "Medium", 2: "High"}
    print("✔ Model loaded successfully")
except FileNotFoundError:
    print("✘ Model not found! Run train_model.py first.")
    model = None

def clean_text(text: str) -> str:
    """Same cleaning function used during training — must match exactly."""
    text = text.lower()
    text = re.sub(r'[^a-z0-9\s]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

# ── Routes ────────────────────────────────────────────────

@app.route('/health', methods=['GET'])
def health():
    """Node.js can ping this to check the ML service is alive."""
    return jsonify({
        "status": "ok",
        "model_loaded": model is not None
    })

@app.route('/predict', methods=['POST'])
def predict():
    """
    Expects JSON body:
      { "text": "complaint text here", "department": "IT Support" }

    Returns:
      {
        "severity": "High",
        "confidence": 0.87,
        "probabilities": { "Low": 0.05, "Medium": 0.08, "High": 0.87 }
      }
    """
    if model is None:
        return jsonify({"error": "Model not loaded. Run train_model.py first."}), 503

    data = request.get_json()

    # Validate input
    if not data or 'text' not in data:
        return jsonify({"error": "Missing 'text' field in request body"}), 400

    text       = str(data.get('text', ''))
    department = str(data.get('department', 'general'))

    if len(text.strip()) < 5:
        return jsonify({"error": "Text too short to classify"}), 400

    # ── Preprocess (must match training exactly!) ─────────
    clean = clean_text(text) + " dept_" + department.replace(' ', '_').lower()

    # ── Predict ───────────────────────────────────────────
    pred_idx    = model.predict([clean])[0]
    probabilities = model.predict_proba([clean])[0]

    severity    = label_map[pred_idx]
    confidence  = float(probabilities.max())

    # Map probabilities to human-readable labels
    prob_dict = {
        label_map[i]: round(float(p), 4)
        for i, p in enumerate(probabilities)
    }

    return jsonify({
        "severity":      severity,
        "confidence":    round(confidence, 4),
        "probabilities": prob_dict
    })

# ── Run ───────────────────────────────────────────────────
if __name__ == '__main__':
    print("\n🚀 ML API running at http://localhost:5001")
    print("   POST /predict  → classify a complaint")
    print("   GET  /health   → check model status\n")
    app.run(host='0.0.0.0', port=5001, debug=True)
