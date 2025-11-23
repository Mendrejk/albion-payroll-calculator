# Albion Payroll Calculator v2.0.0 - Release Notes

## 🎉 Major Release: GUI Edition

This is a major milestone release introducing a graphical user interface alongside the existing CLI.

## What's New

### ✨ Graphical User Interface (GUI)
- **Desktop Application** built with Jetpack Compose
- Modern, user-friendly interface with tabbed navigation
- Three main sections:
  - **Input Tab** - For loading and entering payroll data
  - **Results Tab** - For viewing calculation results
  - **History Tab** - For accessing previous payrolls

### 🏗️ Architecture Improvements
- **Kotlin Multiplatform** structure
- Shared business logic between CLI and GUI
- No code duplication - single source of truth for calculations
- Both CLI and GUI maintained in parallel

### 📦 Distribution
- **Portable ZIP** (92 MB compressed, 160 MB extracted)
- No installation required - extract and run
- Self-contained with embedded JVM
- No admin rights needed
- Can run from USB drive
- Double-click `AlbionPayrollCalculator.exe` to start

## Technical Details

### Project Structure
```
src/
├── commonMain/     # Shared business logic
│   ├── PayrollModels.kt
│   ├── PayrollCalculator.kt
│   ├── PayrollFormatter.kt
│   ├── DateUtils.kt
│   └── StringSimilarity.kt
├── nativeMain/     # CLI version (unchanged)
└── desktopMain/    # New GUI version
```

### Requirements
- **For Development:** JDK 17+ (OpenJDK 24 recommended)
- **For End Users:** None - .exe is self-contained

### Building
```bash
# Run GUI in development
./gradlew desktopRun

# Build portable ZIP distribution
./gradlew packagePortableZip

# Run CLI (still works!)
./gradlew runReleaseExecutableNative
```

## Compatibility

- ✅ **CLI version still works** - No breaking changes
- ✅ **Same input format** - Uses existing `wejscie.txt` files
- ✅ **Same calculation logic** - Identical results to v1.x
- ✅ **Same output format** - CSV and Discord markdown

## What's Next (v2.1+)

The GUI is currently a basic shell. Future updates will add:
- File picker and drag-drop support
- Manual data entry forms
- Rich results display with tables
- Export functionality
- History browser
- Visual charts and summaries

## Migration Guide

No migration needed! This is a pure addition:
- Keep using CLI if you prefer
- Try the GUI for a better experience
- Both versions use the same data files

## Known Limitations

- GUI is currently basic (tabs only)
- File I/O not yet integrated
- Manual input forms not yet implemented
- Results display is placeholder

These will be addressed in upcoming v2.x releases.

---

**Version:** 2.0.0  
**Release Date:** November 23, 2025  
**Build:** Kotlin 2.1.20, Compose 1.7.1
