# BOP Android - Professional Quality Verification Chain
Write-Host "--- BOP Quality Verification Started ---" -ForegroundColor Cyan

# 1. Run Unit Tests & Coverage (JUnit 5 + MockK + Kover)
Write-Host ">> Step 1: Unit Tests & Code Coverage (Kover)..." -ForegroundColor Yellow
.\gradlew.bat test koverXmlReport
if ($LASTEXITCODE -ne 0) { Write-Error "Unit Tests Failed!"; exit 1 }

# 2. Run Static Analysis (Detekt)
Write-Host ">> Step 2: Static Code Analysis (Detekt)..." -ForegroundColor Yellow
.\gradlew.bat :app:detekt
if ($LASTEXITCODE -ne 0) { Write-Warning "Detekt found issues. Please review reports." }

# 3. Run Android Lint (UI & Logic quality)
Write-Host ">> Step 3: Android Linting..." -ForegroundColor Yellow
.\gradlew.bat :app:lintDebug
if ($LASTEXITCODE -ne 0) { Write-Warning "Lint found issues." }

# 4. Run Security Scan (OWASP Dependency Check)
Write-Host ">> Step 4: Security Scan (Dependency Check)..." -ForegroundColor Yellow
.\gradlew.bat dependencyCheckAnalyze
if ($LASTEXITCODE -ne 0) { Write-Warning "Security scan found vulnerabilities." }

# 5. Run UI & Accessibility Tests (Compose Test + AccessibilityChecks)
Write-Host ">> Step 5: UI & Accessibility Tests (Requires connected device/emulator)..." -ForegroundColor Yellow
.\gradlew.bat connectedDebugAndroidTest
if ($LASTEXITCODE -ne 0) { Write-Warning "UI Tests failed or no device connected." }

Write-Host "--- Verification Complete ---" -ForegroundColor Green
Write-Host "Reports generated in:"
Write-Host " - Kover: app/build/reports/kover/"
Write-Host " - Detekt: app/build/reports/detekt/"
Write-Host " - Lint: app/build/reports/lint-results-debug.html"
Write-Host " - Security: build/reports/dependency-check-report.html"
