[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Version](https://img.shields.io/badge/version-2.0.0-green.svg)](https://github.com)

# Albion Payroll Calculator v2.0.0

A Kotlin Multiplatform application for calculating guild payroll in Albion Online. Features both a command-line interface (CLI) and a modern graphical user interface (GUI).

## 🚀 Quick Start

### For End Users
1. Download `AlbionPayrollCalculator-2.0.0-portable.zip` from releases
2. Extract the ZIP anywhere (desktop, USB drive, etc.)
3. Run `AlbionPayrollCalculator/AlbionPayrollCalculator.exe`
4. No installation or admin rights needed!

### For Developers

**Using PowerShell Scripts (Easiest):**
```powershell
.\run-gui.ps1          # Run GUI
.\run-cli.ps1          # Run CLI
.\build-portable.ps1   # Build portable ZIP
.\run-tests.ps1        # Run tests
```

**Using Gradle Directly:**
```bash
./gradlew run          # Run GUI
./gradlew runCli       # Run CLI
```

## 📋 Features

- **Payroll Calculation** - Automated guild payroll processing
- **Tax Management** - 20% tax split (10% guild, 10% returns)
- **Content Organization** - Support for organizers and callers
- **CTA Tracking** - Call to Arms participation rewards
- **Recruitment Bonuses** - Tax return points for recruiters
- **Name Similarity Detection** - Catches potential duplicate entries
- **Multiple Output Formats** - CSV and Discord markdown

## 🏗️ Project Structure

This is a **Kotlin Multiplatform** project with two targets:

- **CLI** (`src/nativeMain`) - Native command-line version
- **GUI** (`src/desktopMain`) - Compose Desktop graphical interface
- **Shared** (`src/commonMain`) - Common business logic

## 📖 Documentation

- **[Quick Start Guide](docs/QUICK_START.md)** - Get started in 5 minutes
- [GUI Documentation](docs/GUI_README.md) - Desktop app guide
- [Distribution Guide](docs/DISTRIBUTION_GUIDE.md) - Building and packaging
- [File Organization](docs/FILE_ORGANIZATION.md) - Project structure
- [Testing Guide](docs/TESTING.md) - Running and writing tests
- [Algorithm Documentation](docs/Algorithm.md) - Calculation details
- [Release Notes](docs/RELEASE_NOTES_v2.0.0.md) - What's new in v2.0

## 🛠️ Building from Source

### Requirements
- JDK 17+ (OpenJDK 24 recommended)
- Gradle 8.4+

### Commands
```bash
# Run GUI in development
./gradlew run          # or: ./gradlew runGui

# Run CLI
./gradlew runCli       # or: ./gradlew runReleaseExecutableNative

# Build portable ZIP distribution
./gradlew packagePortableZip

# Build CLI binary
./gradlew buildCli     # or: ./gradlew linkReleaseExecutableNative

# Run tests
./gradlew allTests
```

## 📦 Output Files

The calculator generates:
- `wyjscie_DD_MM_YYYY.txt` - Formatted payroll table
- `wyjscie_discord_DD_MM_YYYY.md` - Discord-ready markdown

## 🎯 Input Format

Place your payroll data in `wejscie.txt`:

```
KONTENTY:
1: organizer_name
  1: items, cash, location, tab, has_organizer, caller, participant1, participant2, ...
  2: items, cash, location, tab, has_organizer, caller, participant1, ...

CTA:
1: caller - participant1, participant2, participant3

REKRUTACJA:
recruiter_name: points
```

See `przykladoweWejscie.txt` for a complete example.

## 🔧 Configuration

The project uses `gradle.properties` for Java configuration. Update `org.gradle.java.home` if needed.

## 📝 License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
