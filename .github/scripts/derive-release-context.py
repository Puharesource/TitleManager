#!/usr/bin/env python3
from __future__ import annotations

import os
import re
import sys

VERSION_RE = re.compile(r"^v?(?P<version>\d+\.\d+\.\d+(?:-(?P<channel>alpha|beta)\.\d+)?)$")
SHORT_SHA = os.environ.get("GITHUB_SHA", "")[:7]
REF_TYPE = os.environ.get("GITHUB_REF_TYPE", "")
REF_NAME = os.environ.get("GITHUB_REF_NAME", "")
DEFAULT_SNAPSHOT_VERSION = os.environ.get("TITLEMANAGER_SNAPSHOT_VERSION", "3.0.0-SNAPSHOT")


def output(name: str, value: str) -> None:
    print(f"{name}={value}")


if REF_TYPE == "branch" and REF_NAME == "main":
    output("version", DEFAULT_SNAPSHOT_VERSION)
    output("gradle_version", DEFAULT_SNAPSHOT_VERSION)
    output("hangar_version", f"{DEFAULT_SNAPSHOT_VERSION}+{SHORT_SHA}")
    output("channel", "Snapshot")
    output("release_tag", f"snapshot-{SHORT_SHA}")
    output("release_name", f"{DEFAULT_SNAPSHOT_VERSION} ({SHORT_SHA})")
    output("prerelease", "true")
    output("snapshot", "true")
    sys.exit(0)

if REF_TYPE == "tag":
    match = VERSION_RE.match(REF_NAME)
    if match is None:
        raise SystemExit(f"Unsupported release tag: {REF_NAME}. Expected vX.Y.Z, vX.Y.Z-alpha.N, or vX.Y.Z-beta.N")

    version = match.group("version")
    prerelease_channel = match.group("channel")
    channel = "Release" if prerelease_channel is None else prerelease_channel.capitalize()
    output("version", version)
    output("gradle_version", version)
    output("hangar_version", version)
    output("channel", channel)
    output("release_tag", REF_NAME)
    output("release_name", version)
    output("prerelease", "false" if channel == "Release" else "true")
    output("snapshot", "false")
    sys.exit(0)

raise SystemExit(f"Unsupported publishing ref {REF_TYPE}:{REF_NAME}; publishing only supports main and SemVer tags")
