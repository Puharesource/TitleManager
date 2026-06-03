#!/usr/bin/env python3
from __future__ import annotations

import json
import os
import re
import subprocess
from collections import defaultdict

BUCKET = os.environ.get("R2_BUCKET", "titlemanager-maven")
ENDPOINT_URL = os.environ["R2_ENDPOINT_URL"]
KEEP = int(os.environ.get("SNAPSHOT_RETAIN_COUNT", "20"))
SNAPSHOT_DIR_RE = re.compile(r"/\d+\.\d+\.\d+-SNAPSHOT/")
TIMESTAMP_RE = re.compile(r"-(\d{8}\.\d{6}-\d+)")


def aws_json(*args: str) -> dict:
    result = subprocess.run(
        ["aws", *args, "--endpoint-url", ENDPOINT_URL],
        check=True,
        text=True,
        capture_output=True,
    )
    return json.loads(result.stdout or "{}")


def aws(*args: str) -> None:
    subprocess.run(["aws", *args, "--endpoint-url", ENDPOINT_URL], check=True)


def list_objects() -> list[str]:
    keys: list[str] = []
    token: str | None = None
    while True:
        args = ["s3api", "list-objects-v2", "--bucket", BUCKET]
        if token is not None:
            args += ["--continuation-token", token]
        data = aws_json(*args)
        keys.extend(item["Key"] for item in data.get("Contents", []))
        if not data.get("IsTruncated"):
            return keys
        token = data.get("NextContinuationToken")


def snapshot_directory(key: str) -> str | None:
    match = SNAPSHOT_DIR_RE.search(key)
    if match is None:
        return None
    return key[: match.end()]


def timestamp_group(key: str) -> str | None:
    name = key.rsplit("/", 1)[-1]
    match = TIMESTAMP_RE.search(name)
    return match.group(1) if match is not None else None


def main() -> None:
    grouped: dict[str, dict[str, list[str]]] = defaultdict(lambda: defaultdict(list))
    for key in list_objects():
        directory = snapshot_directory(key)
        group = timestamp_group(key)
        if directory is not None and group is not None:
            grouped[directory][group].append(key)

    keys_to_delete: list[str] = []
    for groups in grouped.values():
        stale_groups = sorted(groups, reverse=True)[KEEP:]
        for group in stale_groups:
            keys_to_delete.extend(groups[group])

    for key in keys_to_delete:
        aws("s3api", "delete-object", "--bucket", BUCKET, "--key", key)

    print(f"Deleted {len(keys_to_delete)} stale snapshot objects")


if __name__ == "__main__":
    main()
