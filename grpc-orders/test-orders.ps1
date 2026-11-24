# Sends the required test requests to order-service and prints the HTTP status and body.
# Usage (with both services running):   powershell -ExecutionPolicy Bypass -File .\test-orders.ps1
# For the "user-service stopped" case:  stop user-service first, then run with -Case 3

param([int]$Case = 0)

$url = "http://localhost:8080/api/orders"

function Send-Order($title, $body) {
    Write-Host ""
    Write-Host "### $title" -ForegroundColor Cyan
    Write-Host "POST $url  $body"
    try {
        $r = Invoke-WebRequest -Uri $url -Method Post -ContentType "application/json" -Body $body -UseBasicParsing
        Write-Host "HTTP $($r.StatusCode)" -ForegroundColor Green
        Write-Host $r.Content
    } catch {
        $resp = $_.Exception.Response
        if ($null -eq $resp) { Write-Host "No response: is order-service running on port 8080?" -ForegroundColor Red; return }
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        Write-Host "HTTP $([int]$resp.StatusCode)" -ForegroundColor Yellow
        Write-Host $reader.ReadToEnd()
    }
}

if ($Case -eq 0 -or $Case -eq 1) { Send-Order "1) User exists -> 201 Created" '{"userId":101,"productName":"Laptop","quantity":2}' }
if ($Case -eq 0 -or $Case -eq 2) { Send-Order "2) User not found -> 404" '{"userId":999,"productName":"Laptop","quantity":2}' }
if ($Case -eq 3)                 { Send-Order "3) user-service stopped -> 503" '{"userId":102,"productName":"Phone","quantity":1}' }
if ($Case -eq 0 -or $Case -eq 4) {
    Send-Order "4a) Invalid fields -> 400" '{"userId":101,"quantity":0}'
    Send-Order "4b) Malformed JSON -> 400" '{"userId":101,'
}

Write-Host ""
Write-Host "### Saved orders (GET $url)" -ForegroundColor Cyan
(Invoke-WebRequest -Uri $url -UseBasicParsing).Content
