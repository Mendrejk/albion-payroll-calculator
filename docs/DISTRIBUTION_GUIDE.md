# Distribution Guide - Albion Payroll Calculator v2.0.0

## For End Users

### What You Get
- **File:** `AlbionPayrollCalculator-2.1.0-portable.zip` (92 MB)
- **Type:** Truly portable - no installation required
- **Contents:** Application + embedded Java runtime

### How to Use
1. **Download** the ZIP file
2. **Extract** to any location:
   - Desktop
   - Documents folder
   - USB drive
   - Network drive
3. **Navigate** to the extracted folder
4. **Double-click** `AlbionPayrollCalculator/AlbionPayrollCalculator.exe`
5. **Done!** The application starts immediately

### No Installation Needed
- ✅ No installer to run
- ✅ No admin rights required
- ✅ No registry modifications
- ✅ No system changes
- ✅ No Java installation needed
- ✅ Can run from USB drive
- ✅ Can copy to multiple computers

### System Requirements
- Windows 10 or later
- ~160 MB disk space (extracted)
- That's it!

## For Developers

### Building the Portable Distribution

#### Prerequisites
- JDK 17+ (OpenJDK 24 recommended)
- Set `JAVA_HOME` environment variable
- Gradle 8.4+ (included via wrapper)

#### Build Commands

**Quick Build (Recommended):**
```bash
./gradlew packagePortableZip
```

Output: `build/distributions/AlbionPayrollCalculator-2.1.0-portable.zip`

**Alternative - Folder Only:**
```bash
./gradlew createPortable
```

Output: `build/compose/portable/AlbionPayrollCalculator/`

**Clean Build:**
```bash
./gradlew clean packagePortableZip
```

### What Gets Built

```
AlbionPayrollCalculator-2.1.0-portable.zip
└── AlbionPayrollCalculator/
    ├── AlbionPayrollCalculator.exe  (586 KB launcher)
    ├── README.txt                    (usage instructions)
    ├── app/                          (application JARs)
    │   └── AlbionPayrollCalculator-2.1.0.jar
    └── runtime/                      (embedded JVM)
        ├── bin/
        ├── conf/
        ├── legal/
        └── lib/
```

### Distribution Checklist

Before releasing:
- [ ] Run `./gradlew clean packagePortableZip`
- [ ] Test the ZIP on a clean Windows machine
- [ ] Verify no Java installation is required
- [ ] Test running from different locations (desktop, USB)
- [ ] Verify the application starts and functions correctly
- [ ] Check the README.txt is included
- [ ] Verify version number in window title

### File Sizes

| Item | Size |
|------|------|
| ZIP file | ~92 MB |
| Extracted folder | ~160 MB |
| Launcher .exe | ~586 KB |
| Application JAR | ~2 MB |
| Embedded JVM | ~150 MB |

### Gradle Tasks Reference

| Task | Description | Output |
|------|-------------|--------|
| `desktopRun` | Run GUI in development | N/A |
| `createDistributable` | Create app folder | `build/compose/binaries/main/app/` |
| `createPortable` | Copy to portable folder | `build/compose/portable/` |
| `packagePortableZip` | Create portable ZIP | `build/distributions/*.zip` |

### Troubleshooting Build Issues

**Issue: jlink fails**
- **Cause:** Using JRE instead of JDK
- **Solution:** Set `JAVA_HOME` to a full JDK with jmods
- **Check:** `Test-Path "$env:JAVA_HOME\jmods"` should return `True`

**Issue: Wrong Java version**
- **Solution:** Update `gradle.properties`:
  ```properties
  org.gradle.java.home=C:\\path\\to\\your\\jdk
  ```

**Issue: Build is slow**
- **Normal:** First build downloads dependencies (~150 MB)
- **Subsequent builds:** Much faster (cached)

## Release Process

1. **Update version** in `build.gradle.kts`
2. **Update version** in window title (`src/desktopMain/kotlin/Main.kt`)
3. **Update documentation** (README.md, RELEASE_NOTES.md)
4. **Clean build:** `./gradlew clean packagePortableZip`
5. **Test** the ZIP on a clean machine
6. **Create GitHub release** with the ZIP file
7. **Update release notes** with download link

## Support

For build issues or questions:
- Check `gradle.properties` for Java configuration
- Verify JDK installation has jmods directory
- Run with `--stacktrace` for detailed errors
- Check Compose Desktop documentation
