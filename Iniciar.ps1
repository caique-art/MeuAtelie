$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Instale e abra o Docker Desktop e execute este script novamente.'
}
if (-not (Test-Path -LiteralPath '.env')) {
    $appPassword = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
    $rootPassword = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(24))
    "DB_PASSWORD=$appPassword`nDB_ROOT_PASSWORD=$rootPassword" | Set-Content -LiteralPath '.env' -Encoding utf8
    Write-Host 'Configuracao local criada com senhas aleatorias.'
}
docker compose up --build -d
if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar. Confirme que o Docker Desktop esta aberto.' }
Write-Host 'Sistema iniciado. Acesse http://localhost:8080 apos a inicializacao.'
