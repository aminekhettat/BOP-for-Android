#!/usr/bin/env python3
import os
import re
import subprocess
import sys

def get_current_version():
    toml_path = "gradle/libs.versions.toml"
    if not os.path.exists(toml_path):
        return None, None
    with open(toml_path, "r", encoding="utf-8") as f:
        content = f.read()
    
    name_match = re.search(r'appVersionName = "(.*?)"', content)
    code_match = re.search(r'appVersionCode = "(.*?)"', content)
    
    if name_match and code_match:
        return name_match.group(1), code_match.group(1)
    return None, None

def update_version(new_name, new_code):
    print(f"Auto-syncing version: {new_name} (Code: {new_code})")
    
    # 1. Update libs.versions.toml
    toml_path = "gradle/libs.versions.toml"
    with open(toml_path, "r", encoding="utf-8") as f:
        content = f.read()
    content = re.sub(r'appVersionName = ".*?"', f'appVersionName = "{new_name}"', content)
    content = re.sub(r'appVersionCode = ".*?"', f'appVersionCode = "{new_code}"', content)
    with open(toml_path, "w", encoding="utf-8") as f:
        f.write(content)

    # 2. Update README.md
    readme_path = "README.md"
    if os.path.exists(readme_path):
        with open(readme_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = re.sub(r'Version \d+\.\d+\.\d+', f'Version {new_name}', content)
        with open(readme_path, "w", encoding="utf-8") as f:
            f.write(content)

def main():
    # Only run if we're not already in the middle of this script's own commit
    if os.environ.get("BOP_VERSION_SYNC_IN_PROGRESS"):
        return

    name, code = get_current_version()
    if not name or not code:
        print("Could not find version info in libs.versions.toml")
        return

    # Auto-increment version code for every commit
    new_code = str(int(code) + 1)
    
    update_version(name, new_code)
    
    # Set env var to prevent infinite loop
    os.environ["BOP_VERSION_SYNC_IN_PROGRESS"] = "1"
    
    # Stage the changed files
    subprocess.run(["git", "add", "gradle/libs.versions.toml", "README.md"])

if __name__ == "__main__":
    main()
