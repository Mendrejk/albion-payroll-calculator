# File Organization Structure

## Overview

All payroll and energy balance files are organized by year, month, and type (input/output) for easy navigation and historical tracking.

## Directory Structure

```
payroll/
├── 2024/
│   ├── 02/
│   │   └── input/
│   ├── 09/
│   │   ├── input/
│   │   └── output/
│   ├── 10/
│   │   ├── input/
│   │   └── output/
│   ├── 11/
│   │   ├── input/
│   │   └── output/
│   └── 12/
│       ├── input/
│       └── output/
└── 2025/
    ├── 01/ through 11/
    │   ├── input/    - wejscie_DD_MM_YYYY.txt
    │   └── output/   - wyjscie_DD_MM_YYYY.txt
                      - wyjscie_discord_DD_MM_YYYY.md

energia/
└── 2025/
    ├── 04/ through 11/
    │   ├── input/    - energia_DD_MM_YYYY.txt
    │   └── output/   - energy_balance_DD_MM_YYYY.txt
```

## File Types

### Payroll Files

**Input Files** (`payroll/YYYY/MM/input/`):
- `wejscie_DD_MM_YYYY.txt` - Payroll input data
  - Contains: KONTENTY, CTA, REKRUTACJA sections
  - Format: Manual entry of content hauls, CTAs, and recruitment points

**Output Files** (`payroll/YYYY/MM/output/`):
- `wyjscie_DD_MM_YYYY.txt` - Formatted payroll table
  - Contains: Tax totals, participant payouts, location breakdowns
- `wyjscie_discord_DD_MM_YYYY.md` - Discord-ready markdown
  - Contains: Formatted sections for posting in Discord

### Energy Balance Files

**Input Files** (`energia/YYYY/MM/input/`):
- `energia_DD_MM_YYYY.txt` - Energy transaction export
  - Format: CSV with Date, Player, Reason, Amount

**Output Files** (`energia/YYYY/MM/output/`):
- `energy_balance_DD_MM_YYYY.txt` - Calculated energy balances
  - Contains: Per-player energy balance and territory attack counts

## Root Files

Files kept in the project root:
- `wejscie.txt` - Current/working payroll input file
- `przykladoweWejscie.txt` - Example input file for reference

## Organization Script

The `organize_files.ps1` script automatically organizes files:
- Extracts dates from filenames
- Creates year/month/type folder structure
- Moves files to appropriate locations
- Handles files without years (uses modification date)
- Removes empty archive folders

### Running the Script

```powershell
powershell -ExecutionPolicy Bypass -File ./organize_files.ps1
```

## File Naming Convention

### Standard Format
- Payroll input: `wejscie_DD_MM_YYYY.txt`
- Payroll output: `wyjscie_DD_MM_YYYY.txt`
- Discord output: `wyjscie_discord_DD_MM_YYYY.md`
- Energy input: `energia_DD_MM_YYYY.txt`
- Energy output: `energy_balance_DD_MM_YYYY.txt`

### Legacy Format (Pre-2025)
- Some 2024 files: `wejscie_DD_MM.txt` (no year)
- Organized using file modification date

## Statistics

**Current Organization:**
- Payroll: 2024 (5 months), 2025 (11 months)
- Energia: 2025 (8 months)
- Total: ~200+ files organized

## Benefits

✅ **Easy Navigation** - Find files by year and month  
✅ **Clear Separation** - Inputs and outputs in separate folders  
✅ **Historical Tracking** - See payroll history over time  
✅ **Scalability** - Structure grows naturally with new data  
✅ **Clean Root** - Only current working files in root  

## Future Considerations

- GUI file browser to navigate organized structure
- Automatic archiving after payroll completion
- Date-based file naming in the application
- Backup/export functionality for specific date ranges
