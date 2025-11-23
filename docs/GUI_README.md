# Albion Payroll Calculator v2.0.0 - GUI Edition

## Project Structure

The project is now a **Kotlin Multiplatform** project with two targets:

### 1. Native CLI (Existing)
- **Location:** `src/nativeMain/kotlin/`
- **Purpose:** Command-line interface for payroll calculation
- **Run:** `./gradlew runReleaseExecutableNative`
- **Build:** `./gradlew linkReleaseExecutableNative`

### 2. Desktop GUI (New)
- **Location:** `src/desktopMain/kotlin/`
- **Purpose:** Compose Desktop GUI application
- **Technology:** Jetpack Compose for Desktop (JVM-based)
- **Run:** `./gradlew desktopRun`

### 3. Shared Code
- **Location:** `src/commonMain/kotlin/`
- **Purpose:** Business logic shared between CLI and GUI
- **Files:**
  - `PayrollModels.kt` - Data models
  - `PayrollCalculator.kt` - Core calculation logic
  - `PayrollFormatter.kt` - Formatting utilities
  - `DateUtils.kt` - Date utilities
  - `StringSimilarity.kt` - Name similarity detection

## Building the Desktop GUI

### Run in Development
```bash
./gradlew desktopRun
```

### Build Portable Distribution (Windows)
```bash
./gradlew packagePortableZip
```

This creates a truly portable ZIP file:
```
build/distributions/AlbionPayrollCalculator-2.0.0-portable.zip
```

**What you get:**
- 92 MB ZIP file
- Extract anywhere (USB drive, desktop, etc.)
- Run `AlbionPayrollCalculator/AlbionPayrollCalculator.exe`
- No installation, no admin rights needed
- Includes embedded Java runtime

### Alternative: Create Portable Folder Only
```bash
./gradlew createPortable
```

This creates the portable folder without zipping:
```
build/compose/portable/AlbionPayrollCalculator/
```

## Current GUI Features

The GUI currently has a basic structure with three tabs:

1. **Input Tab** - For loading and entering payroll data
2. **Results Tab** - For displaying calculation results
3. **History Tab** - For viewing previous payroll files

## Next Steps for GUI Development

1. **File I/O Integration**
   - Add file picker for input files
   - Implement file loading from `wejscie.txt`
   - Add save functionality for results

2. **Input Form**
   - Create forms for manual data entry
   - Add validation
   - Support for kontenty, zwózki, CTA, and rekrutacja

3. **Results Display**
   - Table view of payroll results
   - Export to CSV and Discord markdown
   - Visual summaries and charts

4. **History Management**
   - List previous payroll files
   - Load and view historical data
   - Compare payrolls

## Requirements

- **JDK 17 or higher** (for desktop GUI)
- **Gradle 8.4+**

## Notes

- The CLI version still works exactly as before
- Both CLI and GUI share the same calculation logic
- The desktop app requires JVM but can be bundled with it for distribution
- For truly portable .exe, the JVM will be embedded (increases size to ~50-80MB)
