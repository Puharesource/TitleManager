#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path

root = Path("build/maven-repository")
root.mkdir(parents=True, exist_ok=True)
(root / "index.html").write_text(
    """<!doctype html>
<html lang=\"en\">
<head>
  <meta charset=\"utf-8\">
  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
  <title>TitleManager Maven Repository</title>
  <style>body{font-family:system-ui,sans-serif;max-width:860px;margin:3rem auto;padding:0 1rem;line-height:1.55}code,pre{background:#f4f4f5;border-radius:.35rem}code{padding:.1rem .25rem}pre{padding:1rem;overflow:auto}</style>
</head>
<body>
  <h1>TitleManager Maven Repository</h1>
  <p>Add this repository to consume the public TitleManager API artifacts:</p>
  <pre><code>repositories {
    maven("https://repo.tarkan.dev")
}</code></pre>
  <pre><code>dependencies {
    compileOnly("dev.tarkan.titlemanager:modules:bukkit:api:3.0.0-SNAPSHOT")
}</code></pre>
  <p>Published artifacts include <code>titlemanager-bukkit-api</code> and <code>titlemanager-core</code>.</p>
</body>
</html>
""",
    encoding="utf-8",
)
