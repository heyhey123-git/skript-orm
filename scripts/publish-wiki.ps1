#!/usr/bin/env pwsh
#
# Render bilingual GitHub wiki pages, with Chinese first.
#
# The wiki is a separate git repository. Convert relative documentation links to wiki page names,
# including language switches. Links to files not published in the wiki point to the source repository.
#
# Keep this script ASCII-only: Windows PowerShell reads BOM-less scripts using the local code page.
# Read the following data files as UTF-8 so Chinese text works consistently across platforms:
#
#   wiki-pages.tsv    source files and their wiki page names
#   wiki-sidebar.md   shared navigation, with Chinese pages listed first
#
# This script only renders files. The caller handles commits, credentials, and publication,
# so the same script can be used for local previews and CI.

[CmdletBinding()]
param(
    # A working copy of the wiki repository, such as the directory a workflow checks out.
    [Parameter(Mandatory = $true)]
    [string]$WikiDirectory,

    # Where the documentation is read from. The default is the repository this script lives in.
    [string]$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path,

    [string]$RepositoryUrl = 'https://github.com/heyhey123-git/skript-orm',

    [string]$Branch = 'master'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$pagesFile = Join-Path $PSScriptRoot 'wiki-pages.tsv'
$sidebarFile = Join-Path $PSScriptRoot 'wiki-sidebar.md'

# ReadAllText defaults to UTF-8, and strips a byte order mark if the file carries one.
function Read-Text {
    param([string]$Path)
    return [System.IO.File]::ReadAllText($Path)
}

function Write-Text {
    param([string]$Path, [string]$Text)
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Text, $utf8)
}

# Load source-to-page mappings. Use hyphens rather than spaces in page names to match wiki URLs.
$pages = [ordered]@{}
foreach ($line in ((Read-Text $pagesFile) -replace "`r`n", "`n") -split "`n") {
    $entry = $line.Trim()
    if ($entry -eq '' -or $entry.StartsWith('#')) {
        continue
    }
    $parts = $entry -split "`t"
    if ($parts.Count -ne 2) {
        throw "Every line of wiki-pages.tsv is a file name, a tab, and a page name: '$entry' is not."
    }
    $pages[$parts[0]] = $parts[1]
}
if ($pages.Count -eq 0) {
    throw "wiki-pages.tsv names no pages."
}

$sidebar = (Read-Text $sidebarFile) -replace "`r`n", "`n"

# Resolve wiki links by source file name, regardless of directory.
# Reject duplicate file names because they would make link targets ambiguous.
$pageByFileName = @{}
$knownPages = @{}
foreach ($file in $pages.Keys) {
    $fileName = [System.IO.Path]::GetFileName($file)
    if ($pageByFileName.ContainsKey($fileName)) {
        throw "Two wiki pages would answer to the name '$fileName'."
    }
    $pageByFileName[$fileName] = $pages[$file]
    $knownPages[$pages[$file]] = $true
}

# Check that every mapped page appears in the sidebar and every sidebar link has a mapping.
# Report mismatches before writing files.
$problems = @()
foreach ($page in $pages.Values) {
    if ($sidebar -notmatch "\]\($([regex]::Escape($page))\)") {
        $problems += "The sidebar does not link to the page '$page'."
    }
}
foreach ($match in [regex]::Matches($sidebar, '\]\(([^)]+)\)')) {
    $target = $match.Groups[1].Value
    if ($target -match '^[a-zA-Z][a-zA-Z0-9+.-]*:') {
        continue
    }
    if (-not $knownPages.ContainsKey($target)) {
        $problems += "The sidebar links to '$target', which is not a page in wiki-pages.tsv."
    }
}

function Convert-Page {
    param([string]$Text, [string]$SourcePath)

    # Resolve repository links relative to the source page's directory.
    # Split path segments explicitly for consistent behavior on Windows and Linux;
    # System.Uri handled local absolute paths differently on the CI runner.
    $directory = @()
    $sourceParts = $SourcePath -split '/'
    if ($sourceParts.Count -gt 1) {
        $directory = @($sourceParts[0..($sourceParts.Count - 2)])
    }

    # Preserve the language switch; the link conversion below points it to the translated wiki page.
    $body = $Text -replace "`r`n", "`n"

    return [regex]::Replace($body, '\]\(([^)]+)\)', {
            param($match)

            $target = $match.Groups[1].Value
            if ($target -match '^[a-zA-Z][a-zA-Z0-9+.-]*:' -or $target.StartsWith('#')) {
                return $match.Value
            }

            $anchor = ''
            $path = $target
            $hash = $path.IndexOf('#')
            if ($hash -ge 0) {
                $anchor = $path.Substring($hash)
                $path = $path.Substring(0, $hash)
            }
            if ($path -eq '') {
                return $match.Value
            }

            $fileName = [System.IO.Path]::GetFileName($path)
            if ($pageByFileName.ContainsKey($fileName)) {
                return "]($($pageByFileName[$fileName])$anchor)"
            }

            # Files not published as wiki pages are linked to the source repository.
            $segments = @($directory)
            foreach ($segment in (($path -replace '\\', '/') -split '/')) {
                if ($segment -eq '' -or $segment -eq '.') {
                    continue
                }
                if ($segment -eq '..') {
                    if ($segments.Count -eq 0) {
                        throw "$SourcePath links to '$target', which is outside the repository."
                    }
                    $segments = @($segments | Select-Object -SkipLast 1)
                    continue
                }
                $segments += $segment
            }
            if ($segments.Count -eq 0) {
                throw "$SourcePath links to '$target', which names the repository itself."
            }

            return "]($RepositoryUrl/blob/$Branch/$($segments -join '/')$anchor)"
        })
}

# Render and validate all pages in memory before writing files.
$rendered = [ordered]@{}
foreach ($file in $pages.Keys) {
    $source = Join-Path $RepositoryRoot $file
    if (-not (Test-Path -Path $source -PathType Leaf)) {
        throw "The documentation file '$file' is missing, but wiki-pages.tsv maps it to a page."
    }
    $rendered[$pages[$file]] = Convert-Page -Text (Read-Text $source) -SourcePath $file
}

foreach ($page in $rendered.Keys) {
    foreach ($match in [regex]::Matches($rendered[$page], '\]\(([^)]+)\)')) {
        $target = $match.Groups[1].Value
        if ($target -match '^[a-zA-Z][a-zA-Z0-9+.-]*:' -or $target.StartsWith('#')) {
            continue
        }
        $path = ($target -split '#')[0]
        if (-not $knownPages.ContainsKey($path)) {
            $problems += "$page links to '$target', which is not a page in this wiki."
        }
    }
}
if ($problems.Count -gt 0) {
    throw ($problems -join [System.Environment]::NewLine)
}

if (-not (Test-Path -Path $WikiDirectory -PathType Container)) {
    throw "The wiki directory '$WikiDirectory' does not exist."
}
if (-not (Test-Path -Path (Join-Path $WikiDirectory '.git'))) {
    Write-Warning "'$WikiDirectory' is not a checkout of the wiki repository; the pages are written but nothing here will commit them."
}

# Remove Markdown pages that are no longer in the mapping, including manually created pages.
# Keep the shared sidebar and footer.
$carried = @{}
foreach ($page in $rendered.Keys) {
    $carried["$page.md"] = $true
}
$carried['_Sidebar.md'] = $true
$carried['_Footer.md'] = $true

$stale = @(
    Get-ChildItem -Path $WikiDirectory -Recurse -File -Filter '*.md' |
        Where-Object {
            $_.FullName -notmatch '[\\/]\.git[\\/]' -and -not $carried.ContainsKey($_.Name)
        }
)

$updated = 0
$unchanged = 0
foreach ($page in $rendered.Keys) {
    $target = Join-Path $WikiDirectory "$page.md"
    $previous = if (Test-Path -Path $target -PathType Leaf) {
        (Read-Text $target) -replace "`r`n", "`n"
    } else {
        $null
    }

    if ($previous -eq $rendered[$page]) {
        $unchanged++
        continue
    }
    Write-Text -Path $target -Text $rendered[$page]
    Write-Host "publish-wiki: $page (updated)"
    $updated++
}

$sidebarPath = Join-Path $WikiDirectory '_Sidebar.md'
$previousSidebar = if (Test-Path -Path $sidebarPath -PathType Leaf) {
    (Read-Text $sidebarPath) -replace "`r`n", "`n"
} else {
    $null
}
if ($previousSidebar -ne $sidebar) {
    Write-Text -Path $sidebarPath -Text $sidebar
    Write-Host 'publish-wiki: _Sidebar (updated)'
    $updated++
} else {
    $unchanged++
}

$removed = 0
foreach ($file in $stale) {
    Remove-Item -Path $file.FullName -Force
    Write-Host "publish-wiki: $($file.BaseName) (removed, no longer a page)"
    $removed++
}

$total = $rendered.Count + 1
if ($updated -eq 0 -and $removed -eq 0) {
    Write-Host "publish-wiki: the wiki already matches the documentation ($total pages)."
} else {
    Write-Host "publish-wiki: $total pages, $updated changed, $removed removed."
}
