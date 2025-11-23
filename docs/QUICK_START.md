# Quick Start Guide - Albion Payroll Calculator v2.0.0

## Running the Application

### GUI (Recommended)
```bash
./gradlew run
```
or
```bash
./gradlew runGui
```

### CLI
```bash
./gradlew runCli
```

## Building for Distribution

### Portable ZIP (GUI)
```bash
./gradlew packagePortableZip
```
Output: `build/distributions/AlbionPayrollCalculator-2.1.0-portable.zip`

### CLI Executable
```bash
./gradlew buildCli
```
Output: `build/bin/native/releaseExecutable/AlbionPayrollCalculator.exe`

## Testing

### Run All Tests
```bash
./gradlew allTests
```

### Run Specific Tests
```bash
./gradlew desktopTest    # GUI/JVM tests
./gradlew nativeTest     # CLI/Native tests
```

## Common Tasks

| Task | Command | Description |
|------|---------|-------------|
| Run GUI | `./gradlew run` | Start desktop application |
| Run CLI | `./gradlew runCli` | Start command-line version |
| Build GUI | `./gradlew packagePortableZip` | Create portable ZIP |
| Build CLI | `./gradlew buildCli` | Create native executable |
| Run Tests | `./gradlew allTests` | Run all tests |
| Clean | `./gradlew clean` | Clean build artifacts |

## First Time Setup

1. **Install JDK 17+** (OpenJDK 24 recommended)
2. **Set JAVA_HOME** environment variable
3. **Clone repository**
4. **Run GUI:** `./gradlew run`

## File Locations

### Input Files
- Current: `wejscie.txt` (root directory)
- Archive: `payroll/YYYY/MM/input/`

### Output Files
- Generated in: `payroll/YYYY/MM/output/`
- Format: `wyjscie_DD_MM_YYYY.txt`, `wyjscie_discord_DD_MM_YYYY.md`

## Troubleshooting

### "No main class specified"
Use `./gradlew run` instead of `./gradlew desktopRun`

### "jlink fails"
Ensure JAVA_HOME points to a full JDK (not JRE) with jmods directory

### Tests fail
Run `./gradlew clean allTests` to rebuild and test

## Documentation

- [Full README](README.md) - Complete project documentation
- [GUI Guide](docs/GUI_README.md) - Desktop application guide
- [Distribution Guide](docs/DISTRIBUTION_GUIDE.md) - Building and packaging
- [Testing Guide](docs/TESTING.md) - Running and writing tests

## Quick Examples

### Calculate Payroll (GUI)
1. Run: `./gradlew run`
2. Click "Wczytaj wejscie.txt"
3. Click "Oblicz Rozliczenie"
4. View results in "Wyniki" tab

### Calculate Payroll (CLI)
1. Prepare `wejscie.txt` in root directory
2. Run: `./gradlew runCli`
3. Enter 'p' for payroll
4. Results saved to `wyjscie_DD_MM_YYYY.txt`

## Version Info

- **Version:** 2.0.0
- **Kotlin:** 2.1.20
- **Compose:** 1.7.1
- **Targets:** JVM (GUI), Native (CLI)
