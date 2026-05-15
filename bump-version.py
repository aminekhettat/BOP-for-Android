import os
import re
import sys

def bump_version(new_name, new_code):
    print(f"Bumping version to {new_name} (Code: {new_code})...")
    
    # 1. Update libs.versions.toml
    toml_path = "gradle/libs.versions.toml"
    if os.path.exists(toml_path):
        with open(toml_path, "r", encoding="utf-8") as f:
            content = f.read()
        content = re.sub(r'appVersionName = ".*?"', f'appVersionName = "{new_name}"', content)
        content = re.sub(r'appVersionCode = ".*?"', f'appVersionCode = "{new_code}"', content)
        with open(toml_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {toml_path}")

    # 2. Update AndroidManifest.xml (if exists)
    manifest_path = "app/src/main/AndroidManifest.xml"
    if os.path.exists(manifest_path):
        with open(manifest_path, "r", encoding="utf-8") as f:
            content = f.read()
        # AndroidManifest might not have version attributes if handled by Gradle, but let's check
        content = re.sub(r'android:versionName=".*?"', f'android:versionName="{new_name}"', content)
        content = re.sub(r'android:versionCode=".*?"', f'android:versionCode="{new_code}"', content)
        with open(manifest_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {manifest_path}")

    # 3. Update README.md (Version badge or text)
    readme_path = "README.md"
    if os.path.exists(readme_path):
        with open(readme_path, "r", encoding="utf-8") as f:
            content = f.read()
        # Find something like "Version 1.0.0" or similar
        content = re.sub(r'Version \d+\.\d+\.\d+', f'Version {new_name}', content)
        with open(readme_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated {readme_path}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python bump-version.py <version_name> <version_code>")
        sys.exit(1)
    bump_version(sys.argv[1], sys.argv[2])
