# PowerShell script to fix all TextAppearance.Wellness references
$files = Get-ChildItem -Path "app\src\main\res\layout" -Recurse -Filter "*.xml"

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace '@style/TextAppearance\.Wellness\.H1', '@style/TextAppearance.Material3.HeadlineLarge'
    $content = $content -replace '@style/TextAppearance\.Wellness\.H2', '@style/TextAppearance.Material3.HeadlineMedium'
    $content = $content -replace '@style/TextAppearance\.Wellness\.H3', '@style/TextAppearance.Material3.TitleLarge'
    $content = $content -replace '@style/TextAppearance\.Wellness\.H4', '@style/TextAppearance.Material3.TitleMedium'
    $content = $content -replace '@style/TextAppearance\.Wellness\.Body', '@style/TextAppearance.Material3.BodyLarge'
    $content = $content -replace '@style/TextAppearance\.Wellness\.Caption', '@style/TextAppearance.Material3.BodySmall'
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "Fixed all TextAppearance.Wellness references"
