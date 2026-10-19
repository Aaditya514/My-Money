Add-Type -AssemblyName System.Drawing

function Create-PwaIcon([int]$size, [string]$outputPath) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAlias

    # Background deep teal #0f766e
    $bgBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 15, 118, 110))
    $rect = New-Object System.Drawing.Rectangle 0, 0, $size, $size
    $g.FillRectangle($bgBrush, $rect)

    # Inner soft circle
    $innerBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 19, 78, 74))
    $innerMargin = [int]($size * 0.1)
    $innerSize = $size - (2 * $innerMargin)
    $g.FillEllipse($innerBrush, $innerMargin, $innerMargin, $innerSize, $innerSize)

    # White Wallet / Finance glyph
    $strokeWidth = [float]($size * 0.04)
    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::White), $strokeWidth

    # Wallet outline
    $wX = [int]($size * 0.28)
    $wY = [int]($size * 0.33)
    $wW = [int]($size * 0.44)
    $wH = [int]($size * 0.34)
    $g.DrawRectangle($pen, $wX, $wY, $wW, $wH)

    # Flap line
    $g.DrawLine($pen, [float]($wX), [float]($wY + $wH * 0.4), [float]($wX + $wW), [float]($wY + $wH * 0.4))

    # Center coin dot
    $whiteBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $dotSize = [int]($size * 0.07)
    $dotX = [int]($wX + $wW * 0.7 - $dotSize / 2)
    $dotY = [int]($wY + $wH * 0.4 - $dotSize / 2)
    $g.FillEllipse($whiteBrush, $dotX, $dotY, $dotSize, $dotSize)

    $bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $pen.Dispose()
    $whiteBrush.Dispose()
    $innerBrush.Dispose()
    $bgBrush.Dispose()
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Successfully generated $outputPath ($size x $size)"
}

$iconDir = "C:\Users\KIIT\Desktop\Finance-Tracker\finance-tracker-main\finance-tracker-main\frontend\src\assets\icons"
if (-not (Test-Path $iconDir)) {
    New-Item -ItemType Directory -Path $iconDir -Force | Out-Null
}

Create-PwaIcon 192 "$iconDir\icon-192.png"
Create-PwaIcon 512 "$iconDir\icon-512.png"
Create-PwaIcon 512 "$iconDir\icon-maskable-512.png"

