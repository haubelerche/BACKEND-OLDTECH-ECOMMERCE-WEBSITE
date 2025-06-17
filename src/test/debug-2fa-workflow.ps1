# PowerShell Script for Debugging 2FA Workflow
# This script helps isolate and test each part of the 2FA workflow separately

# Configuration
$baseUrl = "http://localhost:8080/oldtech"
$email = "luonghau2909@gmail.com" # Change to your test account
$password = "123456789" # Change to your password

# Function for colored output
function Write-ColorOutput($text, $color) {
    Write-Host $text -ForegroundColor $color
}

# Function to display a menu and get user selection
function Show-Menu {
    Write-ColorOutput "=== 2FA Testing Menu ===" "Cyan"
    Write-ColorOutput "1. Login and get token" "White"
    Write-ColorOutput "2. Setup 2FA (requires token)" "White"
    Write-ColorOutput "3. Verify 2FA code" "White"
    Write-ColorOutput "4. Check 2FA status" "White"
    Write-ColorOutput "5. Disable 2FA (requires token)" "White"
    Write-ColorOutput "6. Exit" "White"

    $choice = Read-Host "Enter your choice (1-6)"
    return $choice
}

# Function to login and get token
function Get-AuthToken {
    Write-ColorOutput "Logging in as $email..." "Yellow"

    $loginBody = @{
        email = $email
        password = $password
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
        Write-ColorOutput "Login successful!" "Green"
        Write-ColorOutput "Token: $($response.accessToken)" "Cyan"
        return $response.accessToken
    }
    catch {
        Write-ColorOutput "Login failed: $_" "Red"
        if ($_.ErrorDetails.Message) {
            Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
        }
        return $null
    }
}

# Function to setup 2FA
function Setup-2FA {
    $token = Read-Host "Enter your authentication token"

    Write-ColorOutput "Setting up 2FA..." "Yellow"

    $headers = @{
        "Authorization" = "Bearer $token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/setup" -Method Post -Headers $headers
        Write-ColorOutput "2FA setup successful!" "Green"

        # Save QR code to file
        $qrCodeBase64 = $response.qrCodeDataUrl.Split(",")[1]
        [System.IO.File]::WriteAllBytes("$PSScriptRoot\qrcode.png", [Convert]::FromBase64String($qrCodeBase64))

        Write-ColorOutput "QR Code saved to $(Resolve-Path "$PSScriptRoot\qrcode.png")" "Green"
        Write-ColorOutput "Message: $($response.message)" "Cyan"
    }
    catch {
        Write-ColorOutput "2FA setup failed: $_" "Red"
        if ($_.ErrorDetails.Message) {
            Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
        }
    }
}

# Function to verify 2FA code
function Verify-2FACode {
    $verifyEmail = Read-Host "Enter email"
    $code = Read-Host "Enter 6-digit verification code from Google Authenticator"

    $verifyBody = @{
        email = $verifyEmail
        code = $code
    } | ConvertTo-Json

    Write-ColorOutput "Verifying 2FA code..." "Yellow"

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/verify" -Method Post -Body $verifyBody -ContentType "application/json"
        if ($response.valid -eq $true) {
            Write-ColorOutput "Code verified successfully!" "Green"
            Write-ColorOutput "Message: $($response.message)" "Green"
        }
        else {
            Write-ColorOutput "Code verification failed." "Red"
            Write-ColorOutput "Message: $($response.message)" "Red"
        }
    }
    catch {
        Write-ColorOutput "Verification request failed: $_" "Red"
        if ($_.ErrorDetails.Message) {
            Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
        }
    }
}

# Function to check 2FA status
function Check-2FAStatus {
    $statusEmail = Read-Host "Enter email"

    Write-ColorOutput "Checking 2FA status..." "Yellow"

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/status?email=$statusEmail" -Method Get
        Write-ColorOutput "Status check successful!" "Green"
        Write-ColorOutput "2FA Enabled: $($response.enabled)" "Cyan"
        Write-ColorOutput "Message: $($response.message)" "Cyan"
    }
    catch {
        Write-ColorOutput "Status check failed: $_" "Red"
        if ($_.ErrorDetails.Message) {
            Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
        }
    }
}

# Function to disable 2FA
function Disable-2FA {
    $token = Read-Host "Enter your authentication token"

    Write-ColorOutput "Disabling 2FA..." "Yellow"

    $headers = @{
        "Authorization" = "Bearer $token"
    }

    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/disable" -Method Post -Headers $headers
        Write-ColorOutput "2FA successfully disabled!" "Green"
        Write-ColorOutput "Message: $($response.message)" "Green"
    }
    catch {
        Write-ColorOutput "Disable request failed: $_" "Red"
        if ($_.ErrorDetails.Message) {
            Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
        }
    }
}

# Main program loop
$exit = $false
while (-not $exit) {
    $choice = Show-Menu

    switch ($choice) {
        "1" { Get-AuthToken }
        "2" { Setup-2FA }
        "3" { Verify-2FACode }
        "4" { Check-2FAStatus }
        "5" { Disable-2FA }
        "6" { $exit = $true }
        default { Write-ColorOutput "Invalid choice. Please try again." "Red" }
    }

    if (-not $exit) {
        Write-Host ""
        Write-ColorOutput "Press Enter to continue..." "White"
        Read-Host
        Clear-Host
    }
}

Write-ColorOutput "2FA testing complete." "Cyan"
