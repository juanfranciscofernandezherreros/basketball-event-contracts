#!/usr/bin/env python3
"""Update one consumer POM and release notes for a contracts release."""

import argparse
import datetime as dt
import re
from pathlib import Path


def dependency_block(pom: str):
    for match in re.finditer(r"<dependency\b[^>]*>.*?</dependency>", pom, re.S):
        if re.search(r"<artifactId>\s*basketball-event-contracts\s*</artifactId>", match.group()):
            return match
    return None


def dependency_version(pom: str, block) -> str:
    tag = re.search(r"<version>\s*([^<]+)\s*</version>", block.group())
    if not tag:
        raise ValueError("basketball-event-contracts dependency has no version")
    value = tag.group(1).strip()
    if value.startswith("${") and value.endswith("}"):
        name = value[2:-1]
        prop = re.search(rf"(<{re.escape(name)}>\s*)([^<]+)(\s*</{re.escape(name)}>)", pom)
        if not prop:
            raise ValueError(f"Could not resolve Maven property {name}")
        return prop.group(2).strip()
    return value


def bump_patch(value: str) -> str:
    match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)(-SNAPSHOT)?", value.strip())
    if not match:
        raise ValueError(f"Service version is not patchable SemVer: {value}")
    return f"{match.group(1)}.{match.group(2)}.{int(match.group(3)) + 1}{match.group(4) or ''}"


def project_version(pom: str):
    root = re.search(r"<project\b[^>]*>(.*)</project>\s*$", pom, re.S)
    if not root:
        raise ValueError("Could not parse Maven project")
    header = root.group(1)
    parent = re.search(r"<parent\b[^>]*>.*?</parent>", header, re.S)
    parent_end = parent.end() if parent else 0
    prefix_end = parent.start() if parent else len(header)
    if parent:
        header = header[:parent.start()] + header[parent.end():]
    section = re.search(r"<(?:properties|dependencies|dependencyManagement|build|profiles)\b", header)
    if section:
        header = header[:section.start()]
    tag = re.search(r"<version>\s*([^<]+)\s*</version>", header)
    if tag:
        if parent and tag.start(1) >= prefix_end:
            start = root.start(1) + parent_end + tag.start(1) - prefix_end
            end = root.start(1) + parent_end + tag.end(1) - prefix_end
        else:
            start = root.start(1) + tag.start(1)
            end = root.start(1) + tag.end(1)
        return tag.group(1).strip(), start, end
    revision = re.search(r"(<revision>\s*)([^<]+)(\s*</revision>)", pom)
    if revision:
        return revision.group(2).strip(), revision.start(2), revision.end(2)
    raise ValueError("Could not find a project version or CI-friendly revision")


def apply_update(pom_path: Path, readme_path: Path, changelog_path: Path, target: str, issue: str):
    pom = pom_path.read_text(encoding="utf-8")
    block = dependency_block(pom)
    if not block:
        raise ValueError(f"{pom_path} does not declare basketball-event-contracts; initial migration required")
    current = dependency_version(pom, block)
    if current == target:
        return False
    if tuple(map(int, target.split("."))) <= tuple(map(int, current.split("."))):
        raise ValueError(f"Refusing contract downgrade/non-increase {current} -> {target}")

    service_version, start, end = project_version(pom)
    if service_version.startswith("${"):
        name = re.search(r"\$\{([^}]+)\}", service_version).group(1)
        prop = re.search(rf"(<{re.escape(name)}>\s*)([^<]+)(\s*</{re.escape(name)}>)", pom)
        if not prop:
            raise ValueError(f"Could not resolve project version property {name}")
        next_service_version = bump_patch(prop.group(2).strip())
        pom = pom[:prop.start(2)] + next_service_version + pom[prop.end(2):]
    else:
        next_service_version = bump_patch(service_version)
        pom = pom[:start] + next_service_version + pom[end:]

    block = dependency_block(pom)
    tag = re.search(r"<version>\s*([^<]+)\s*</version>", block.group())
    value = tag.group(1).strip()
    if value.startswith("${") and value.endswith("}"):
        name = value[2:-1]
        prop = re.search(rf"(<{re.escape(name)}>\s*)([^<]+)(\s*</{re.escape(name)}>)", pom)
        if not prop:
            raise ValueError(f"Could not resolve Maven property {name}")
        pom = pom[:prop.start(2)] + target + pom[prop.end(2):]
    else:
        start = block.start(1) + tag.start(1)
        end = block.start(1) + tag.end(1)
        pom = pom[:start] + target + pom[end:]
    pom_path.write_text(pom, encoding="utf-8", newline="")

    readme = readme_path.read_text(encoding="utf-8") if readme_path.exists() else ""
    ref = re.search(r"basketball-event-contracts.{0,120}?\d+\.\d+\.\d+", readme, re.S)
    if ref:
        readme = readme[:ref.start()] + re.sub(r"\d+\.\d+\.\d+", target, ref.group(), count=1) + readme[ref.end():]
    else:
        readme = readme.rstrip() + f"\n\n## Contrato compartido\n\nConsume `basketball-event-contracts` {target}.\n"
    readme_path.write_text(readme, encoding="utf-8", newline="")

    changelog = changelog_path.read_text(encoding="utf-8") if changelog_path.exists() else "# Changelog\n"
    newline = changelog.find("\n")
    entry = f"\n## {next_service_version} - {dt.datetime.now(dt.timezone.utc):%Y-%m-%d}\n\n- {issue}: actualiza `basketball-event-contracts` a {target}.\n"
    changelog_path.write_text(changelog[:newline + 1] + entry + changelog[newline + 1:], encoding="utf-8", newline="")
    return True


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode", choices=("inspect", "apply"), required=True)
    parser.add_argument("--pom", type=Path, required=True)
    parser.add_argument("--readme", type=Path, required=True)
    parser.add_argument("--changelog", type=Path, required=True)
    parser.add_argument("--contract-version", required=True)
    parser.add_argument("--issue", default="")
    args = parser.parse_args()
    pom = args.pom.read_text(encoding="utf-8")
    block = dependency_block(pom)
    if not block:
        result = "missing"
    else:
        current = dependency_version(pom, block)
        current_parts = tuple(map(int, current.split(".")))
        target_parts = tuple(map(int, args.contract_version.split(".")))
        result = "false" if current_parts == target_parts else "ahead" if current_parts > target_parts else "true"
    if args.mode == "inspect":
        print(f"needs_update={result}")
    elif result == "missing":
        raise SystemExit(f"{args.pom} has no contract dependency; its initial migration must be merged first")
    else:
        apply_update(args.pom, args.readme, args.changelog, args.contract_version, args.issue)


if __name__ == "__main__":
    main()
