# Zbuduj przenośną wersję GUI (portable ZIP)
# Build portable GUI distribution (ZIP)

Write-Host "Budowanie przenosnej wersji GUI..." -ForegroundColor Cyan
Write-Host "Building portable GUI distribution..." -ForegroundColor Cyan
Write-Host ""

./gradlew clean packagePortableZip

Write-Host ""
Write-Host "Gotowe! Portable ZIP utworzony w:" -ForegroundColor Green
Write-Host "Done! Portable ZIP created at:" -ForegroundColor Green
Write-Host "  build\distributions\AlbionPayrollCalculator-2.0.0-portable.zip" -ForegroundColor White
Write-Host ""
Write-Host "Rozpakuj i uruchom: AlbionPayrollCalculator\AlbionPayrollCalculator.exe" -ForegroundColor Yellow
Write-Host "Extract and run: AlbionPayrollCalculator\AlbionPayrollCalculator.exe" -ForegroundColor Yellow
