# Script to organize payroll and energy files into year/month/type folders

Write-Host "Organizing payroll and energy files..." -ForegroundColor Cyan

# Function to extract date from filename and create folder structure
function Move-ToYearMonthType {
    param(
        [string]$FilePath,
        [string]$BaseFolder,
        [string]$Type  # "input" or "output"
    )
    
    $fileName = Split-Path $FilePath -Leaf
    $fileItem = Get-Item $FilePath
    $year = $null
    $month = $null
    $day = $null
    
    # Try to extract date from filename (DD_MM_YYYY pattern)
    if ($fileName -match '(\d{2})_(\d{2})_(\d{4})') {
        $day = $matches[1]
        $month = $matches[2]
        $year = $matches[3]
    }
    # Try DD_MM pattern (no year) - use file modification date
    elseif ($fileName -match '(\d{2})_(\d{2})[\._]') {
        $day = $matches[1]
        $month = $matches[2]
        # Use the year from the file's LastWriteTime
        $year = $fileItem.LastWriteTime.Year.ToString()
        Write-Host "  Note: $fileName has no year, using $year from file date" -ForegroundColor Yellow
    }
    # Special case for files like "wyjscie_baza_*.txt" - use modification date
    elseif ($fileName -match 'wyjscie_baza') {
        $year = $fileItem.LastWriteTime.Year.ToString()
        $month = $fileItem.LastWriteTime.Month.ToString("00")
        Write-Host "  Note: $fileName is special file, using $year/$month from file date" -ForegroundColor Yellow
    }
    
    if ($year -and $month) {
        # Create year/month/type folder structure
        $targetFolder = Join-Path $BaseFolder "$year\$month\$Type"
        New-Item -ItemType Directory -Force -Path $targetFolder | Out-Null
        
        # Move file
        $targetPath = Join-Path $targetFolder $fileName
        if (Test-Path $FilePath) {
            Move-Item -Path $FilePath -Destination $targetPath -Force
            Write-Host "  Moved: $fileName -> $year/$month/$Type/" -ForegroundColor Green
            return $true
        }
    }
    return $false
}

# Create base folders
New-Item -ItemType Directory -Force -Path "payroll" | Out-Null
New-Item -ItemType Directory -Force -Path "energia" | Out-Null

# Move payroll INPUT files from root
Write-Host "`nOrganizing payroll INPUT files from root..." -ForegroundColor Yellow
$payrollInputFiles = Get-ChildItem -Path "." -Filter "wejscie_*.txt" -File
foreach ($file in $payrollInputFiles) {
    Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "payroll" -Type "input"
}

# Move payroll OUTPUT files from root
Write-Host "`nOrganizing payroll OUTPUT files from root..." -ForegroundColor Yellow
$payrollOutputFiles = Get-ChildItem -Path "." -Filter "wyjscie_*.txt" -File
$payrollOutputFiles += Get-ChildItem -Path "." -Filter "wyjscie_discord_*.md" -File
foreach ($file in $payrollOutputFiles) {
    Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "payroll" -Type "output"
}

# Move payroll archive files
Write-Host "`nOrganizing payroll archive files..." -ForegroundColor Yellow
if (Test-Path "payroll_archive") {
    $archiveFiles = Get-ChildItem -Path "payroll_archive" -File
    foreach ($file in $archiveFiles) {
        # Determine if input or output
        if ($file.Name -like "wejscie_*") {
            Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "payroll" -Type "input"
        } else {
            Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "payroll" -Type "output"
        }
    }
    
    # Remove empty archive folder
    if ((Get-ChildItem "payroll_archive" -Force | Measure-Object).Count -eq 0) {
        Remove-Item "payroll_archive" -Force
        Write-Host "  Removed empty payroll_archive folder" -ForegroundColor Gray
    }
}

# Move energia INPUT files
Write-Host "`nOrganizing energia INPUT files..." -ForegroundColor Yellow
if (Test-Path "energia/in") {
    $energiaInFiles = Get-ChildItem -Path "energia/in" -Filter "*.txt" -File
    foreach ($file in $energiaInFiles) {
        Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "energia" -Type "input"
    }
    
    # Remove old in folder if empty
    if ((Get-ChildItem "energia/in" -Force | Measure-Object).Count -eq 0) {
        Remove-Item "energia/in" -Force
        Write-Host "  Removed empty energia/in folder" -ForegroundColor Gray
    }
}

# Move energia OUTPUT files
Write-Host "`nOrganizing energia OUTPUT files..." -ForegroundColor Yellow
if (Test-Path "energia/out") {
    $energiaOutFiles = Get-ChildItem -Path "energia/out" -Filter "*.txt" -File
    foreach ($file in $energiaOutFiles) {
        Move-ToYearMonthType -FilePath $file.FullName -BaseFolder "energia" -Type "output"
    }
    
    # Remove old out folder if empty
    if ((Get-ChildItem "energia/out" -Force | Measure-Object).Count -eq 0) {
        Remove-Item "energia/out" -Force
        Write-Host "  Removed empty energia/out folder" -ForegroundColor Gray
    }
}

# Keep wejscie.txt and przykladoweWejscie.txt in root
Write-Host "`nKeeping template files in root:" -ForegroundColor Yellow
Write-Host "  - wejscie.txt (current input)" -ForegroundColor Gray
Write-Host "  - przykladoweWejscie.txt (example)" -ForegroundColor Gray

Write-Host "`nDone! File structure organized." -ForegroundColor Green
Write-Host "`nNew structure:" -ForegroundColor Cyan
Write-Host "  payroll/YYYY/MM/input/   - wejscie_*.txt" -ForegroundColor White
Write-Host "  payroll/YYYY/MM/output/  - wyjscie_*.txt, wyjscie_discord_*.md" -ForegroundColor White
Write-Host "  energia/YYYY/MM/input/   - energia_*.txt" -ForegroundColor White
Write-Host "  energia/YYYY/MM/output/  - energy_balance_*.txt" -ForegroundColor White
