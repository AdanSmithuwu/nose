[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$SqlcmdArgs
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$SqlFile = Join-Path $ScriptDir 'InstallAll.sql'

$Sqlcmd = Get-Command sqlcmd -ErrorAction SilentlyContinue
if (-not $Sqlcmd) {
    Write-Error 'Error: sqlcmd command not found in PATH'
    exit 1
}

& $Sqlcmd -b -i $SqlFile @SqlcmdArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error 'Error: sqlcmd failed to execute InstallAll.sql'
    exit $LASTEXITCODE
}

Write-Output 'InstallAll.sql ejecutado correctamente'
