#!/usr/bin/env python3
"""Compare deux rapports Semgrep et échoue si de nouvelles alertes apparaissent."""

import json
import sys
from collections import Counter
from pathlib import Path


def load_findings(filename):
    data = json.loads(Path(filename).read_text())
    return Counter(
        (result["check_id"], result["path"])
        for result in data.get("results", [])
    )


if len(sys.argv) != 3:
    print("Usage: python3 compare_semgrep.py AVANT.json APRES.json")
    sys.exit(2)

before = load_findings(sys.argv[1])
after = load_findings(sys.argv[2])

new_findings = after - before

print(f"Alertes de référence : {sum(before.values())}")
print(f"Alertes actuelles : {sum(after.values())}")
print(f"Nouvelles alertes : {sum(new_findings.values())}")

for (rule, path), count in sorted(new_findings.items()):
    print(f"\n[NOUVELLE VULNÉRABILITÉ] {rule}")
    print(f"Fichier : {path}")
    print(f"Nombre de nouvelles occurrences : {count}")

if new_findings:
    print("\nÉCHEC : de nouvelles vulnérabilités ont été détectées.")
    sys.exit(1)

print("\nSUCCÈS : aucune nouvelle vulnérabilité détectée.")
