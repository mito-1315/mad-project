"""
=============================================================
  COMPLAINT SEVERITY CLASSIFIER — STEP-BY-STEP ML GUIDE
=============================================================
Dataset: 800 university complaints
Labels : Low / Medium / High severity
Model  : TF-IDF + Logistic Regression (beginner-friendly)

Run this script once to train and save the model.
Then your Node.js backend calls it via a tiny Flask API.

Install dependencies first:
  pip install pandas scikit-learn flask joblib
=============================================================
"""

# ─────────────────────────────────────────
# STEP 1 — Import everything we need
# ─────────────────────────────────────────
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report, confusion_matrix
import joblib   # saves the trained model to disk
import re
import os

print("=" * 55)
print("  COMPLAINT SEVERITY CLASSIFIER — TRAINING PIPELINE")
print("=" * 55)

# ─────────────────────────────────────────
# STEP 2 — Load the dataset
# ─────────────────────────────────────────
# Update this path to wherever you placed the CSV
DATA_PATH = "university_complaint_triage_dataset.csv"

print(f"\n[STEP 2] Loading dataset from: {DATA_PATH}")
df = pd.read_csv(DATA_PATH)

print(f"  ✔ Loaded {len(df)} rows")
print(f"  ✔ Columns: {df.columns.tolist()}")
print(f"\n  Label distribution:")
print(df['urgency'].value_counts().to_string())

# ─────────────────────────────────────────
# STEP 3 — Preprocessing (cleaning text)
# ─────────────────────────────────────────
# WHY? Raw text has noise — punctuation, numbers, extra spaces.
# Cleaning helps the model focus on meaningful words.

print("\n[STEP 3] Preprocessing text...")

def clean_text(text: str) -> str:
    """
    Simple cleaning pipeline:
      1. Lowercase everything
      2. Remove special characters
      3. Collapse multiple spaces
    """
    text = text.lower()                        # "VPN Issue" → "vpn issue"
    text = re.sub(r'[^a-z0-9\s]', ' ', text)  # remove punctuation
    text = re.sub(r'\s+', ' ', text).strip()   # collapse spaces
    return text

df['clean_text'] = df['text'].apply(clean_text)

# We also include department as a feature — it adds context!
# e.g., "High" complaints in IT vs Hostels have different keywords
df['features'] = df['clean_text'] + " dept_" + df['department'].str.replace(' ', '_').str.lower()

print("  ✔ Text cleaned and department prefix added")
print(f"\n  Sample before: {df['text'].iloc[0][:80]}...")
print(f"  Sample after : {df['features'].iloc[0][:80]}...")

# ─────────────────────────────────────────
# STEP 4 — Encode labels
# ─────────────────────────────────────────
# Machine learning needs numbers, not strings.
# We map: Low → 0, Medium → 1, High → 2

print("\n[STEP 4] Encoding labels...")

label_map = {"Low": 0, "Medium": 1, "High": 2}
reverse_map = {0: "Low", 1: "Medium", 2: "High"}

df['label'] = df['urgency'].map(label_map)
print(f"  ✔ Label mapping: {label_map}")

# ─────────────────────────────────────────
# STEP 5 — Split into Train / Test sets
# ─────────────────────────────────────────
# WHY? We train on 80% of data and test on the remaining 20%
# to measure how well the model generalises to NEW complaints.

print("\n[STEP 5] Splitting data into train (80%) / test (20%)...")

X = df['features']
y = df['label']

X_train, X_test, y_train, y_test = train_test_split(
    X, y,
    test_size=0.20,       # 20% goes to testing
    random_state=42,      # fixed seed → reproducible split
    stratify=y            # keeps label ratios equal in both splits
)

print(f"  ✔ Training samples : {len(X_train)}")
print(f"  ✔ Test samples     : {len(X_test)}")

# ─────────────────────────────────────────
# STEP 6 — Build the ML Pipeline
# ─────────────────────────────────────────
# A Pipeline chains two steps so they're treated as one unit:
#
#   TfidfVectorizer  →  converts text to numbers
#   LogisticRegression → learns the classification
#
# TF-IDF (Term Frequency–Inverse Document Frequency):
#   Gives higher weight to words that are common in a complaint
#   but rare across the whole dataset. "Urgent", "broken", 
#   "blocked" get higher scores than "the", "and", "I".
#
# Logistic Regression:
#   Simple, fast, and highly interpretable. Perfect for text
#   classification as a starting point.

print("\n[STEP 6] Building TF-IDF + Logistic Regression pipeline...")

model_pipeline = Pipeline([
    ('tfidf', TfidfVectorizer(
        ngram_range=(1, 2),  # use single words AND two-word phrases
        max_features=5000,   # keep only the top 5000 terms
        min_df=2,            # ignore terms appearing in < 2 docs
        sublinear_tf=True    # apply log normalization to term freq
    )),
    ('clf', LogisticRegression(
        C=1.0,               # regularisation strength (default is good)
        max_iter=1000,       # enough iterations to converge
        random_state=42      # multi_class is auto-detected in modern sklearn
    ))
])

print("  ✔ Pipeline created")

# ─────────────────────────────────────────
# STEP 7 — Train the model
# ─────────────────────────────────────────
print("\n[STEP 7] Training model... (this takes a few seconds)")

model_pipeline.fit(X_train, y_train)

print("  ✔ Training complete!")

# ─────────────────────────────────────────
# STEP 8 — Evaluate on test data
# ─────────────────────────────────────────
print("\n[STEP 8] Evaluating on test set...")

y_pred = model_pipeline.predict(X_test)
accuracy = (y_pred == y_test).mean()

print(f"\n  Overall Accuracy: {accuracy:.2%}")
print("\n  Detailed Report (precision, recall, F1-score):")
print("  " + "-" * 50)
print(classification_report(
    y_test, y_pred,
    target_names=["Low", "Medium", "High"]
))

print("  Confusion Matrix (rows=actual, cols=predicted):")
cm = confusion_matrix(y_test, y_pred)
print(f"               Low  Med  High")
for i, row_label in enumerate(["Low ", "Med ", "High"]):
    print(f"  Actual {row_label}: {cm[i]}")

# ─────────────────────────────────────────
# STEP 9 — Save the model
# ─────────────────────────────────────────
print("\n[STEP 9] Saving model to disk...")

os.makedirs("saved_model", exist_ok=True)
joblib.dump(model_pipeline, "saved_model/severity_model.pkl")
joblib.dump(reverse_map,    "saved_model/label_map.pkl")

print("  ✔ Model saved → saved_model/severity_model.pkl")
print("  ✔ Label map   → saved_model/label_map.pkl")

# ─────────────────────────────────────────
# STEP 10 — Quick sanity check (try a new complaint)
# ─────────────────────────────────────────
print("\n[STEP 10] Sanity check with sample complaints:")

sample_complaints = [
    ("MATLAB license request for a course next week.", "Academics"),
    ("Hostel Wi-Fi is completely down. Cannot submit exam tomorrow!", "Hostels"),
    ("Request a new parking sticker for my car.", "Transport"),
]

for text, dept in sample_complaints:
    clean = clean_text(text) + " dept_" + dept.replace(" ", "_").lower()
    pred_label_idx = model_pipeline.predict([clean])[0]
    proba = model_pipeline.predict_proba([clean])[0]
    confidence = proba.max()
    label = reverse_map[pred_label_idx]
    print(f"\n  Complaint : {text[:60]}")
    print(f"  Department: {dept}")
    print(f"  Prediction: {label}  (confidence: {confidence:.0%})")

print("\n" + "=" * 55)
print("  Training pipeline COMPLETE.")
print("  Next step: run flask_api.py to serve predictions.")
print("=" * 55)
