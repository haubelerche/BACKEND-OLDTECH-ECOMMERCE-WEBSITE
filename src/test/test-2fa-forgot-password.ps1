# PowerShell Script to Test 2FA Forgot Password Workflow
# This script tests the correct flow for users who have 2FA enabled and forgot their password

# Configuration
$baseUrl = "http://localhost:8080/oldtech"
$email = "luonghau2909@gmail.com" # Your registered email with 2FA enabled

function Write-ColorOutput($text, $color) {
    Write-Host $text -ForegroundColor $color
}

Write-ColorOutput "=== Testing 2FA Forgot Password Workflow ===" "Cyan"
Write-ColorOutput "This script tests the correct flow for users with 2FA who forgot their password" "White"
Write-Host ""

# Step 1: Check if 2FA is enabled for the user
Write-ColorOutput "Step 1: Checking 2FA status for $email..." "Yellow"

try {
    $statusResponse = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/status?email=$email" -Method Get
    Write-ColorOutput "2FA Status Check Result:" "Green"
    Write-ColorOutput "- 2FA Enabled: $($statusResponse.enabled)" "Cyan"
    Write-ColorOutput "- Recovery Method: $($statusResponse.recoveryMethod)" "Cyan"
    Write-ColorOutput "- Message: $($statusResponse.message)" "Cyan"
    
    if ($statusResponse.enabled -eq $true) {
        Write-Host ""
        Write-ColorOutput "✓ 2FA is enabled for this user." "Green"
        
        # Give user options for what they want to do
        Write-Host ""
        Write-ColorOutput "What would you like to do?" "Yellow"
        Write-ColorOutput "1. Reset password using 2FA (if you forgot your password)" "White"
        Write-ColorOutput "2. Recover QR code (if you need to re-scan QR code)" "White"
        $userChoice = Read-Host "Enter your choice (1 or 2)"
        
        if ($userChoice -eq "2") {
            # QR Recovery Flow
            Write-Host ""
            Write-ColorOutput "=== QR Code Recovery ===" "Cyan"
            Write-ColorOutput "This will regenerate your QR code using your existing secret." "White"
            Write-ColorOutput "You need your current 2FA code to prove you have the authenticator app." "White"
            Write-Host ""
            
            $qrRecoveryCode = Read-Host "Enter your current 6-digit 2FA code from Google Authenticator"
            
            $qrRecoveryBody = @{
                email = $email
                code = $qrRecoveryCode
            } | ConvertTo-Json
            
            try {
                $qrResponse = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/recover-qr" -Method Post -Body $qrRecoveryBody -ContentType "application/json"
                
                if ($qrResponse.success -eq $true) {
                    Write-ColorOutput "✓ QR code recovery successful!" "Green"
                    Write-ColorOutput "Message: $($qrResponse.message)" "Green"
                    Write-ColorOutput "Secret Key: $($qrResponse.secretKey)" "Cyan"
                    Write-ColorOutput "QR Code data URL generated successfully" "Cyan"
                    Write-Host ""
                    Write-ColorOutput "You can now scan the QR code again or manually enter the secret key in your authenticator app." "Green"
                } else {
                    Write-ColorOutput "✗ QR code recovery failed" "Red"
                    Write-ColorOutput "Message: $($qrResponse.message)" "Red"
                }
            }
            catch {
                Write-ColorOutput "✗ QR recovery request failed" "Red"
                Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
                if ($_.ErrorDetails.Message) {
                    Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
                }
            }
            
        } elseif ($userChoice -eq "1") {
            # Password Reset Flow (existing code)
            Write-Host ""
            Write-ColorOutput "=== Password Reset with 2FA ===" "Cyan"
            
            # Step 2: Get 2FA code from user
            Write-Host ""
            Write-ColorOutput "Step 2: Enter your 2FA code from Google Authenticator" "Yellow"
            $twoFactorCode = Read-Host "Enter your 6-digit 2FA code"
            
            # Step 3: Get new password from user
            $newPassword = Read-Host "Enter your new password" -AsSecureString
            $newPasswordText = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($newPassword))
            
            # Step 4: Reset password using 2FA
            Write-ColorOutput "Step 3: Resetting password using 2FA verification..." "Yellow"
            
            $resetBody = @{
                email = $email
                code = $twoFactorCode
                newPassword = $newPasswordText
            } | ConvertTo-Json
            
            try {
                $resetResponse = Invoke-RestMethod -Uri "$baseUrl/auth/2fa/reset-password" -Method Post -Body $resetBody -ContentType "application/json"
                
                if ($resetResponse.success -eq $true) {
                    Write-ColorOutput "✓ Password reset successful!" "Green"
                    Write-ColorOutput "Message: $($resetResponse.message)" "Green"
                    
                    # Step 5: Test login with new password
                    Write-Host ""
                    Write-ColorOutput "Step 4: Testing login with new password..." "Yellow"
                    
                    $loginBody = @{
                        email = $email
                        password = $newPasswordText
                    } | ConvertTo-Json
                    
                    try {
                        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
                        Write-ColorOutput "✓ Login successful with new password!" "Green"
                        Write-ColorOutput "Access Token received (first 50 chars): $($loginResponse.accessToken.Substring(0, [Math]::Min(50, $loginResponse.accessToken.Length)))..." "Cyan"
                    }
                    catch {
                        Write-ColorOutput "✗ Login failed with new password" "Red"
                        Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
                    }
                } else {
                    Write-ColorOutput "✗ Password reset failed" "Red"
                    Write-ColorOutput "Message: $($resetResponse.message)" "Red"
                }
            }
            catch {
                Write-ColorOutput "✗ Password reset request failed" "Red"
                Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
                if ($_.ErrorDetails.Message) {
                    Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
                }
            }
        }
    } else {
        Write-ColorOutput "! This user does not have 2FA enabled. Use regular forgot password flow instead." "Yellow"
        Write-ColorOutput "Regular forgot password endpoint: POST $baseUrl/auth/forgot-password" "Cyan"
    }
}
catch {
    Write-ColorOutput "✗ Failed to check 2FA status" "Red"
    Write-ColorOutput "Error: $($_.Exception.Message)" "Red"
    if ($_.ErrorDetails.Message) {
        Write-ColorOutput "Response: $($_.ErrorDetails.Message)" "Red"
    }
}

Write-Host ""
Write-ColorOutput "=== Workflow Summary ===" "Cyan"
Write-ColorOutput "For users with 2FA enabled who forgot their password:" "White"
Write-ColorOutput "1. Check 2FA status: GET /auth/2fa/status?email=EMAIL" "White"
Write-ColorOutput "2. If 2FA enabled, use: POST /auth/2fa/reset-password" "White"
Write-ColorOutput "   - Body: { email, code, newPassword }" "White"
Write-ColorOutput "3. If 2FA NOT enabled, use: POST /auth/forgot-password" "White"
Write-ColorOutput "   - Body: { email }" "White"
Write-Host ""
Write-ColorOutput "IMPORTANT: The /auth/2fa/setup endpoint should NEVER be called during forgot password flow!" "Red"
Write-ColorOutput "That endpoint is only for setting up 2FA when logged in." "Red"

Write-Host ""
Write-ColorOutput "Press Enter to exit..." "White"
Read-Host
