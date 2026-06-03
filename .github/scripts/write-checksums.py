#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import sys
from pathlib import Path

ALGORITHMS = ("sha256", "sha512")

for raw_path in sys.argv[1:]:
    path = Path(raw_path)
    if not path.is_file():
        raise SystemExit(f"Not a file: {path}")
    data = path.read_bytes()
    for algorithm in ALGORITHMS:
        digest = hashlib.new(algorithm, data).hexdigest()
        checksum_path = path.with_name(f"{path.name}.{algorithm}")
        checksum_path.write_text(f"{digest}  {path.name}\n", encoding="utf-8")
