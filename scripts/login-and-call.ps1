<#
.SYNOPSIS
Logs in to the running collab-messaging-secure app, extracts the JWT, copies it to the clipboard, and calls a protected endpoint.

.DESCRIPTION
This helper calls POST /auth/login with provided credentials, prints and copies the returned token, then issues a GET to the protected endpoint with the Authorization header.

.PARAMETER Username
The username to log in as (default: alice).

.PARAMETER Password
The password to use (default: alicepass).

.PARAMETER LoginUri
Login endpoint URI (default: http://localhost:8080/auth/login).

.PARAMETER ProtectedEndpoint
The protected endpoint to call after login (default: http://localhost:8080/some/protected/endpoint).

.EXAMPLE
  .\scripts\login-and-call.ps1

.EXAMPLE
  .\scripts\login-and-call.ps1 -Username bob -Password bobpass -ProtectedEndpoint 'http://localhost:8080/api/protected'
#>

param(
    [string]$Username = 'alice',
    [string]$Password = 'alicepass',
    [string]$LoginUri = 'http://localhost:8080/auth/login',
    [string]$ProtectedEndpoint = 'http://localhost:8080/some/protected/endpoint'
)

function Exit-WithError($msg) {
    Write-Error $msg
    exit 1
}

Write-Host "Logging in as '$Username' -> $LoginUri"

try {
    $body = @{ username = $Username; password = $Password } | ConvertTo-Json
    $response = Invoke-RestMethod -Method Post -Uri $LoginUri -ContentType 'application/json' -Body $body -ErrorAction Stop
} catch {
    Exit-WithError "Login failed: $($_.Exception.Message)"
}

if (-not $response -or -not $response.token) {
    Exit-WithError "Login succeeded but no token returned. Response:`n$($response | ConvertTo-Json -Depth 5)"
}

$token = $response.token
Write-Host "`n=== JWT TOKEN ===`n$token`n===============`n"

# try to copy to clipboard (works in Windows PowerShell)
try {
    Set-Clipboard -Value $token -ErrorAction Stop
    Write-Host "Token copied to clipboard."
} catch {
    Write-Host "Could not copy token to clipboard (Set-Clipboard may not be available)."
}

Write-Host "Calling protected endpoint: $ProtectedEndpoint"
$headers = @{ Authorization = "Bearer $token" }

try {
    $result = Invoke-RestMethod -Method Get -Uri $ProtectedEndpoint -Headers $headers -ErrorAction Stop
    Write-Host "Protected endpoint response:`n$($result | ConvertTo-Json -Depth 5)"
} catch {
    $ex = $_.Exception
    Write-Error "Protected call failed: $($ex.Message)"
    if ($ex.Response -ne $null) {
        try {
            # try to print HTTP status code
            if ($ex.Response -is [System.Net.HttpWebResponse]) {
                Write-Host "HTTP Status: $($ex.Response.StatusCode)"
            }
        } catch {}
        try {
            $stream = $ex.Response.GetResponseStream()
            if ($stream) {
                $reader = [System.IO.StreamReader]::new($stream)
                $bodyText = $reader.ReadToEnd()
                if ($bodyText) { Write-Host "Response body:`n$bodyText" }
            }
        } catch {}
    }
    exit 1
}

Write-Host "Done."
