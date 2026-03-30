# Run from the repo root: .\docs\generate-graphics.ps1
# Generates docs\feature-graphic.png (1024x500) and docs\social-preview.png (1280x640)

Add-Type -AssemblyName System.Drawing

function New-ClockBanner {
    param([int]$Width, [int]$Height, [string]$OutputPath)

    $bmp = New-Object System.Drawing.Bitmap($Width, $Height)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint  = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    # --- Background ---
    $g.Clear([System.Drawing.Color]::FromArgb(15, 23, 42))  # #0F172A

    # --- Clock placement (left-center) ---
    $clockD  = [int]($Height * 0.70)
    $r       = $clockD / 2.0
    $clockCX = [int]($Width * 0.22)
    $clockCY = $Height / 2.0

    # Outer blue circle
    $blueBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(37, 99, 235))
    $g.FillEllipse($blueBrush, [float]($clockCX - $r), [float]($clockCY - $r), [float]$clockD, [float]$clockD)

    # White ring
    $ringD = $clockD * 0.88
    $ringR = $ringD / 2.0
    $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $g.FillEllipse($whiteBrush, [float]($clockCX - $ringR), [float]($clockCY - $ringR), [float]$ringD, [float]$ringD)

    # Clock face
    $faceD = $clockD * 0.78
    $faceR = $faceD / 2.0
    $faceBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(238, 242, 255))
    $g.FillEllipse($faceBrush, [float]($clockCX - $faceR), [float]($clockCY - $faceR), [float]$faceD, [float]$faceD)

    # Tick marks
    $tickColor = [System.Drawing.Color]::FromArgb(51, 65, 85)
    for ($i = 0; $i -lt 12; $i++) {
        $angle    = $i * 30.0 * [Math]::PI / 180.0
        $isMajor  = ($i % 3 -eq 0)
        $innerR   = $r * $(if ($isMajor) { 0.71 } else { 0.76 })
        $outerR   = $r * 0.83
        $tickW    = [int]($clockD * $(if ($isMajor) { 0.032 } else { 0.020 }))
        $tickPen  = New-Object System.Drawing.Pen($tickColor, [float]$tickW)
        $tickPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $tickPen.EndCap   = [System.Drawing.Drawing2D.LineCap]::Round
        $sinA = [Math]::Sin($angle); $cosA = [Math]::Cos($angle)
        $g.DrawLine($tickPen,
            [float]($clockCX + $innerR * $sinA), [float]($clockCY - $innerR * $cosA),
            [float]($clockCX + $outerR * $sinA), [float]($clockCY - $outerR * $cosA))
        $tickPen.Dispose()
    }

    # Hour hand — 10 o'clock (300°)
    $handColor = [System.Drawing.Color]::FromArgb(15, 23, 42)
    $hourAngle  = 300.0 * [Math]::PI / 180.0
    $hourLen    = $r * 0.48
    $hourPen    = New-Object System.Drawing.Pen($handColor, [float][int]($clockD * 0.068))
    $hourPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $hourPen.EndCap   = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($hourPen,
        [float]$clockCX, [float]$clockCY,
        [float]($clockCX + $hourLen * [Math]::Sin($hourAngle)),
        [float]($clockCY - $hourLen * [Math]::Cos($hourAngle)))
    $hourPen.Dispose()

    # Minute hand — 2 o'clock (60°)
    $minAngle = 60.0 * [Math]::PI / 180.0
    $minLen   = $r * 0.63
    $minPen   = New-Object System.Drawing.Pen($handColor, [float][int]($clockD * 0.048))
    $minPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $minPen.EndCap   = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawLine($minPen,
        [float]$clockCX, [float]$clockCY,
        [float]($clockCX + $minLen * [Math]::Sin($minAngle)),
        [float]($clockCY - $minLen * [Math]::Cos($minAngle)))
    $minPen.Dispose()

    # Center dot — sky blue
    $dotD    = [int]($clockD * 0.09)
    $dotBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(56, 189, 248))  # #38BDF8
    $g.FillEllipse($dotBrush, [float]($clockCX - $dotD/2.0), [float]($clockCY - $dotD/2.0), [float]$dotD, [float]$dotD)

    # --- Text (right side) ---
    $titlePt    = [int]($Height * 0.092)
    $subtitlePt = [int]($Height * 0.046)
    $tagPt      = [int]($Height * 0.036)

    $fontTitle    = New-Object System.Drawing.Font("Segoe UI", $titlePt,    [System.Drawing.FontStyle]::Bold)
    $fontSubtitle = New-Object System.Drawing.Font("Segoe UI", $subtitlePt, [System.Drawing.FontStyle]::Regular)
    $fontTag      = New-Object System.Drawing.Font("Segoe UI", $tagPt,      [System.Drawing.FontStyle]::Regular)

    $brushWhite  = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $brushGrey   = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(148, 163, 184))  # #94A3B8
    $brushAccent = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(56, 189, 248))   # #38BDF8

    $textX     = [float]($Width * 0.44)
    $titleY    = [float]($Height * 0.25)
    $subtitleY = [float]($Height * 0.46)
    $tagY      = [float]($Height * 0.60)

    $g.DrawString("Advanced Clock",                    $fontTitle,    $brushWhite,  $textX, $titleY)
    $g.DrawString("Fully customizable home screen widget", $fontSubtitle, $brushGrey,   $textX, $subtitleY)
    $g.DrawString("Free  ·  Android 8.0+",              $fontTag,      $brushAccent, $textX, $tagY)

    # --- Cleanup & save ---
    foreach ($obj in @($fontTitle,$fontSubtitle,$fontTag,$blueBrush,$whiteBrush,$faceBrush,
                        $dotBrush,$brushWhite,$brushGrey,$brushAccent,$g)) { $obj.Dispose() }

    $absPath = [System.IO.Path]::GetFullPath($OutputPath)
    $bmp.Save($absPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()

    Write-Host "Saved $absPath  ($Width x $Height)"
}

New-ClockBanner -Width 1024 -Height 500 -OutputPath "docs\feature-graphic.png"
New-ClockBanner -Width 1280 -Height 640 -OutputPath "docs\social-preview.png"
