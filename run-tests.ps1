# Uruchom wszystkie testy
# Run all tests

Write-Host "Uruchamianie testow..." -ForegroundColor Cyan
Write-Host "Running tests..." -ForegroundColor Cyan
Write-Host ""

./gradlew allTests

Write-Host ""
Write-Host "Raport testow dostepny w:" -ForegroundColor Yellow
Write-Host "Test report available at:" -ForegroundColor Yellow
Write-Host "  build\reports\tests\allTests\index.html" -ForegroundColor White
