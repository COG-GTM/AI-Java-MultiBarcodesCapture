<#
.SYNOPSIS
  Windows smoke check that the Android app and the WMS web interface still build.

.DESCRIPTION
  1. Finds a JDK 17+ and bootstraps the Android SDK (cmdline-tools, platform 35, build-tools,
     CMake 3.22.1) and a portable PHP CLI when they are missing.
  2. Android: gradlew assembleDebug testDebugUnitTest, then checks the debug APK has a
     manifest, dex and the native libyuvprocessor.so for every ABI in abiFilters.
     lintDebug fails only on errors not already listed in ci/windows/lint-baseline.txt
     (the app has pre-existing lint errors); refresh it with -UpdateLintBaseline.
  3. WebInterface: php -l on every .php file, JSON parse of every lang/*.json file,
     node --check on every .js file (when Node is installed).
  Writes a Markdown report and exits non-zero when any check fails.

.EXAMPLE
  powershell -NoProfile -ExecutionPolicy Bypass -File ci\windows\verify-app.ps1
#>
[CmdletBinding()]
param(
    [string]$ToolsRoot = $(if ($env:BARCODE_CI_TOOLS) { $env:BARCODE_CI_TOOLS } else { 'C:\Android' }),
    [string]$ReportPath = '',
    [switch]$SkipAndroid,
    [switch]$SkipWeb,
    [switch]$SkipLint,
    [switch]$UpdateLintBaseline
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
if (-not $ReportPath) { $ReportPath = Join-Path $RepoRoot 'build\windows-check\report.md' }
$ReportPath = [IO.Path]::GetFullPath($ReportPath)
$LogDir = Split-Path $ReportPath -Parent
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null

$Results = New-Object System.Collections.Generic.List[object]

function Add-Result([string]$Check, [bool]$Passed, [string]$Detail) {
    $Results.Add([pscustomobject]@{ Check = $Check; Passed = $Passed; Detail = $Detail })
    $status = if ($Passed) { 'PASS' } else { 'FAIL' }
    Write-Host "[$status] $Check - $Detail"
}

function Get-Download([string]$Url, [string]$Destination) {
    Write-Host "Downloading $Url"
    Invoke-WebRequest -Uri $Url -OutFile $Destination -UseBasicParsing
}

function Resolve-JavaHome {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    $candidates += Get-ChildItem 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft' -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match 'jdk-?(\d+)' -and [int]($Matches[1]) -ge 17 } |
        Sort-Object Name -Descending | ForEach-Object { $_.FullName }
    foreach ($c in $candidates) {
        $java = Join-Path $c 'bin\java.exe'
        if (-not (Test-Path $java)) { continue }
        $version = (& cmd /c "`"$java`" -version 2>&1") -join ' '
        if ($version -match 'version "(\d+)' -and [int]$Matches[1] -ge 17) { return $c }
    }
    throw 'No JDK 17+ found. Install Temurin 17 or 21 or set JAVA_HOME.'
}

function Initialize-AndroidSdk([string]$SdkRoot) {
    $sdkManager = Join-Path $SdkRoot 'cmdline-tools\latest\bin\sdkmanager.bat'
    if (-not (Test-Path $sdkManager)) {
        New-Item -ItemType Directory -Force -Path $SdkRoot | Out-Null
        $zip = Join-Path $ToolsRoot 'cmdline-tools.zip'
        Get-Download 'https://dl.google.com/android/repository/commandlinetools-win-13114758_latest.zip' $zip
        $tmp = Join-Path $SdkRoot 'cmdline-tools-tmp'
        Expand-Archive -Path $zip -DestinationPath $tmp -Force
        New-Item -ItemType Directory -Force -Path (Join-Path $SdkRoot 'cmdline-tools') | Out-Null
        Move-Item (Join-Path $tmp 'cmdline-tools') (Join-Path $SdkRoot 'cmdline-tools\latest')
        Remove-Item $tmp -Recurse -Force
    }
    $packages = @('platforms;android-35', 'build-tools;35.0.0', 'platform-tools', 'cmake;3.22.1')
    $missing = @($packages | Where-Object { -not (Test-Path (Join-Path $SdkRoot ($_ -replace ';', '\\'))) })
    if (-not $missing.Count) { return }
    # Via cmd so sdkmanager's stderr warnings don't become terminating PowerShell errors.
    $licLog = Join-Path $LogDir 'sdk-licenses.log'
    $installLog = Join-Path $LogDir 'sdk-install.log'
    & cmd /c "(for /l %i in (1,1,30) do @echo y) | `"$sdkManager`" --sdk_root=`"$SdkRoot`" --licenses > `"$licLog`" 2>&1"
    $pkgArgs = ($missing | ForEach-Object { '"' + $_ + '"' }) -join ' '
    & cmd /c "`"$sdkManager`" --sdk_root=`"$SdkRoot`" $pkgArgs > `"$installLog`" 2>&1"
    if ($LASTEXITCODE -ne 0) { throw "sdkmanager failed (see $installLog)" }
}

function Get-PhpExe {
    $onPath = Get-Command php -ErrorAction SilentlyContinue
    if ($onPath) { return $onPath.Source }
    $php = Join-Path $ToolsRoot 'php\php.exe'
    if (-not (Test-Path $php)) {
        $zip = Join-Path $ToolsRoot 'php.zip'
        Get-Download 'https://windows.php.net/downloads/releases/latest/php-8.3-nts-Win32-vs16-x64-latest.zip' $zip
        Expand-Archive -Path $zip -DestinationPath (Join-Path $ToolsRoot 'php') -Force
    }
    return $php
}

function Invoke-Gradle([string[]]$Tasks, [string]$Log, [string]$InitScript) {
    Push-Location $RepoRoot
    try {
        & cmd /c "gradlew.bat --no-daemon --stacktrace --init-script `"$InitScript`" $($Tasks -join ' ') > `"$Log`" 2>&1"
        return $LASTEXITCODE
    } finally { Pop-Location }
}

# Lint key = id|repo-relative file|message (no line numbers, so unrelated edits don't shift it).
function Get-LintErrorKeys([string]$XmlPath) {
    $root = $RepoRoot.TrimEnd('\') + '\'
    $doc = New-Object Xml.XmlDocument
    $doc.Load($XmlPath)
    foreach ($i in $doc.issues.issue) {
        if ($i.severity -notin @('Error', 'Fatal')) { continue }
        $file = [string]@($i.location)[0].file
        if ($file.StartsWith($root, [StringComparison]::OrdinalIgnoreCase)) { $file = $file.Substring($root.Length) }
        "{0}|{1}|{2}" -f $i.id, ($file -replace '\\', '/'), $i.message
    }
}

# The app has pre-existing lint errors, so lint gates on errors that are not in
# ci/windows/lint-baseline.txt. Refresh it with -UpdateLintBaseline after fixing or accepting issues.
function Test-Lint([string]$InitScript) {
    $lintLog = Join-Path $LogDir 'lint.log'
    $null = Invoke-Gradle @('lintDebug') $lintLog $InitScript
    $xml = Join-Path $RepoRoot 'AI_MultiBarcodes_Capture\build\reports\lint-results-debug.xml'
    if (-not (Test-Path $xml)) { Add-Result 'Android lint (no new errors)' $false 'lint did not produce lint-results-debug.xml (see lint.log)'; return }
    $current = @(Get-LintErrorKeys $xml | Sort-Object)
    $baselineFile = Join-Path $PSScriptRoot 'lint-baseline.txt'
    if ($UpdateLintBaseline) {
        [IO.File]::WriteAllLines($baselineFile, [string[]]$current, (New-Object Text.UTF8Encoding $false))
        Add-Result 'Android lint (no new errors)' $true "baseline updated: $($current.Count) errors"
        return
    }
    $remaining = @{}
    if (Test-Path $baselineFile) { foreach ($k in [IO.File]::ReadAllLines($baselineFile)) { if ($k) { $remaining[$k] = 1 + [int]$remaining[$k] } } }
    $new = New-Object System.Collections.Generic.List[string]
    foreach ($k in $current) { if ($remaining[$k] -gt 0) { $remaining[$k]-- } else { $new.Add($k) } }
    $detail = "$($current.Count) errors, $($new.Count) new vs baseline"
    if ($new.Count) { $script:LintNew = $new; $detail += ': ' + (($new | Select-Object -First 5 | ForEach-Object { ($_ -split '\|')[0..1] -join ' in ' }) -join '; ') }
    Add-Result 'Android lint (no new errors)' ($new.Count -eq 0) $detail
}

function Test-Android {
    $env:JAVA_HOME = Resolve-JavaHome
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
    $sdkRoot = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } elseif ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { Join-Path $ToolsRoot 'sdk' }
    Initialize-AndroidSdk $sdkRoot
    $env:ANDROID_HOME = $sdkRoot
    $env:ANDROID_SDK_ROOT = $sdkRoot
    Add-Result 'Toolchain' $true "JAVA_HOME=$env:JAVA_HOME; ANDROID_HOME=$sdkRoot"

    $initScript = Join-Path $PSScriptRoot 'maven-mirror.init.gradle'
    $gradleLog = Join-Path $LogDir 'gradle.log'
    $gradleExit = Invoke-Gradle @('assembleDebug', 'testDebugUnitTest') $gradleLog $initScript
    $failedTask = (Select-String -Path $gradleLog -Pattern "Execution failed for task '([^']+)'" | Select-Object -First 1)
    $detail = if ($gradleExit -eq 0) { 'gradlew assembleDebug testDebugUnitTest succeeded' }
              elseif ($failedTask) { "gradlew exit $gradleExit; $($failedTask.Matches[0].Value)" }
              else { "gradlew exit $gradleExit (see gradle.log)" }
    Add-Result 'Gradle build + unit tests' ($gradleExit -eq 0) $detail
    if ($gradleExit -ne 0) { $script:GradleTail = (Get-Content $gradleLog -Tail 40) -join "`n"; return }

    if (-not $SkipLint) { Test-Lint $initScript }

    $moduleDir = Join-Path $RepoRoot 'AI_MultiBarcodes_Capture'
    $testXml = Get-ChildItem (Join-Path $moduleDir 'build\test-results\testDebugUnitTest') -Filter '*.xml' -ErrorAction SilentlyContinue
    $tests = 0; $failures = 0
    foreach ($x in $testXml) { $s = ([xml](Get-Content $x.FullName)).testsuite; $tests += [int]$s.tests; $failures += [int]$s.failures + [int]$s.errors }
    Add-Result 'Unit tests' ($tests -gt 0 -and $failures -eq 0) "$tests tests, $failures failures"

    $apk = Get-ChildItem (Join-Path $moduleDir 'build\outputs\apk\debug') -Filter '*.apk' -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $apk) { Add-Result 'Debug APK' $false 'no APK under build\outputs\apk\debug'; return }
    Add-Result 'Debug APK' $true ("{0} ({1:N1} MB)" -f $apk.Name, ($apk.Length / 1MB))

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($apk.FullName)
    try { $entries = $zip.Entries | ForEach-Object { $_.FullName } } finally { $zip.Dispose() }
    $gradleFile = Get-Content (Join-Path $moduleDir 'build.gradle.kts') -Raw
    $abis = [regex]::Matches($gradleFile, 'abiFilters\s*\+=\s*listOf\(([^)]*)\)') | ForEach-Object { [regex]::Matches($_.Groups[1].Value, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value } }
    $missing = @($abis | Where-Object { $entries -notcontains "lib/$_/libyuvprocessor.so" })
    Add-Result 'Native libyuvprocessor.so packaged' ($missing.Count -eq 0 -and $abis.Count -gt 0) $(if ($missing.Count) { "missing for: $($missing -join ', ')" } else { "ABIs: $($abis -join ', ')" })
    $hasManifest = $entries -contains 'AndroidManifest.xml'
    $hasDex = @($entries | Where-Object { $_ -match '^classes\d*\.dex$' }).Count -gt 0
    Add-Result 'APK contents (manifest + dex)' ($hasManifest -and $hasDex) "AndroidManifest.xml=$hasManifest, classes*.dex=$hasDex"
}

function Test-Web {
    $webSrc = Join-Path $RepoRoot 'WebInterface\src'
    $php = Get-PhpExe
    $phpFiles = Get-ChildItem $webSrc -Recurse -Filter '*.php'
    $bad = @()
    foreach ($f in $phpFiles) {
        $out = & $php -l $f.FullName 2>&1
        if ($LASTEXITCODE -ne 0) { $bad += "$($f.FullName.Substring($RepoRoot.Length + 1)): $($out -join ' ')" }
    }
    Add-Result 'PHP syntax (php -l)' ($bad.Count -eq 0 -and $phpFiles.Count -gt 0) $(if ($bad.Count) { $bad -join '; ' } else { "$($phpFiles.Count) files OK" })

    $jsonFiles = Get-ChildItem (Join-Path $webSrc 'lang') -Filter '*.json'
    $badJson = @()
    foreach ($f in $jsonFiles) {
        try { [IO.File]::ReadAllText($f.FullName, [Text.Encoding]::UTF8) | ConvertFrom-Json | Out-Null } catch { $badJson += $f.Name }
    }
    Add-Result 'Translation JSON (lang/*.json)' ($badJson.Count -eq 0 -and $jsonFiles.Count -gt 0) $(if ($badJson.Count) { "invalid: $($badJson -join ', ')" } else { "$($jsonFiles.Count) files OK" })

    $node = Get-Command node -ErrorAction SilentlyContinue
    if ($node) {
        $badJs = @()
        $jsFiles = Get-ChildItem $webSrc -Recurse -Filter '*.js'
        foreach ($f in $jsFiles) { & $node.Source --check $f.FullName 2>&1 | Out-Null; if ($LASTEXITCODE -ne 0) { $badJs += $f.Name } }
        Add-Result 'JavaScript syntax (node --check)' ($badJs.Count -eq 0) $(if ($badJs.Count) { "invalid: $($badJs -join ', ')" } else { "$($jsFiles.Count) files OK" })
    } else {
        Write-Host 'node not found; skipping JavaScript syntax check'
    }
}

$started = Get-Date
if (-not $SkipAndroid) { try { Test-Android } catch { Add-Result 'Android check' $false $_.Exception.Message } }
if (-not $SkipWeb) { try { Test-Web } catch { Add-Result 'WebInterface check' $false $_.Exception.Message } }

$commit = (& git -C $RepoRoot rev-parse --short HEAD 2>$null)
$branch = (& git -C $RepoRoot rev-parse --abbrev-ref HEAD 2>$null)
$failed = @($Results | Where-Object { -not $_.Passed })
$verdict = if ($failed.Count -eq 0 -and $Results.Count -gt 0) { 'PASS' } else { 'FAIL' }
$minutes = [int]((Get-Date) - $started).TotalMinutes

$lines = @("## Windows app check: $verdict", '', "Commit ``$commit`` on ``$branch`` - $minutes min on $([Environment]::OSVersion.VersionString)", '', '| Check | Result | Detail |', '|---|---|---|')
foreach ($r in $Results) { $lines += "| $($r.Check) | $(if ($r.Passed) { 'PASS' } else { 'FAIL' }) | $($r.Detail -replace '\|', '/') |" }
if ($script:LintNew) { $lines += @('', '<details><summary>New lint errors</summary>', '', '```') + @($script:LintNew) + @('```', '</details>') }
if ($script:GradleTail) { $lines += @('', '<details><summary>Gradle output (last 40 lines)</summary>', '', '```', $script:GradleTail, '```', '</details>') }
Set-Content -Path $ReportPath -Value ($lines -join "`n") -Encoding UTF8
Write-Host ''
Write-Host ($lines -join "`n")
Write-Host "Report: $ReportPath"
if ($verdict -ne 'PASS') { exit 1 }
