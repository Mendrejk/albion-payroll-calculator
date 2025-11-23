# Release Notes v2.1.0 - Interactive Input Builder

## 🎉 New Features

### Interactive Input Builder (Kreator)
The biggest addition to v2.1.0 is the **Interactive Input Builder** - a step-by-step GUI for creating payroll data without manually editing text files.

#### Key Features:
- **Three-section interface**: Kontenty, CTA, Rekrutacja
- **Visual form builder**: Add, edit, and remove entries with buttons and forms
- **Real-time validation**: Immediate feedback on input errors
- **Example data loader**: Quick start with sample data
- **Seamless integration**: Direct calculation from the builder

#### How It Works:

1. **Kontenty Section**
   - Add multiple contents with unique IDs
   - Optional organizer assignment
   - Add multiple hauls per content
   - Each haul includes:
     - Items and cash values (in thousands)
     - Location dropdown (BEACH, FORT, MISTS, ROADS)
     - Tab number
     - Organizer presence checkbox
     - Optional caller
     - Participant list (comma-separated)

2. **CTA Section**
   - Add CTA events with IDs
   - Assign caller
   - List participants

3. **Rekrutacja Section**
   - Add recruitment entries
   - Assign recruiter and points

4. **Action Buttons**
   - **Załaduj przykładowe dane**: Load example data to get started
   - **Użyj tego wejścia**: Save input for later use
   - **Oblicz**: Calculate and view results immediately

### UI Improvements
- **Three-tab layout**: Kreator (new), Plik (file input), Wyniki (results)
- **Collapsible content cards**: Expand/collapse individual contents
- **Delete buttons**: Easy removal of entries
- **Material Design 3**: Modern, clean interface

## 📖 Documentation

New documentation added:
- **[Interactive Input Guide](INTERACTIVE_INPUT_GUIDE.md)** - Complete guide in Polish and English

Updated documentation:
- **README.md** - Added interactive input feature
- **QUICK_START.md** - Mentioned PowerShell scripts

## 🔧 Technical Changes

### New Files
- `src/desktopMain/kotlin/InteractiveInputScreen.kt` - Interactive input builder implementation
- `docs/INTERACTIVE_INPUT_GUIDE.md` - User guide for the new feature

### Modified Files
- `src/desktopMain/kotlin/Main.kt` - Added third tab for interactive input
- `README.md` - Updated features and documentation links
- `docs/QUICK_START.md` - Added PowerShell script instructions

### Data Classes
New data classes for interactive input:
- `HaulData` - Represents a single haul
- `ContentData` - Represents a content with multiple hauls
- `CtaData` - Represents a CTA event
- `RecruitmentData` - Represents a recruitment entry
- `InteractiveInputState` - Manages the entire input state

## 🎯 Benefits

1. **Easier for new users**: No need to learn text file format
2. **Fewer errors**: Visual forms prevent syntax mistakes
3. **Faster input**: Dropdowns and checkboxes speed up data entry
4. **Better UX**: Immediate feedback and validation
5. **Flexible workflow**: Use interactive builder OR file input

## 🔄 Backward Compatibility

- **100% compatible** with existing text file format
- File input tab still available for advanced users
- Generated text matches the original format exactly
- All existing features work unchanged

## 🚀 Usage Example

```
1. Launch GUI (run-gui.ps1)
2. Go to "Kreator" tab
3. Click "Załaduj przykładowe dane" to see how it works
4. Modify the example or start fresh
5. Click "Oblicz" to calculate results
```

## 📝 Future Enhancements

Potential improvements for future versions:
- Import from existing text files into the builder
- Save/load builder state
- Participant autocomplete
- Validation warnings before calculation
- Bulk operations (duplicate content, etc.)
- Export to file from builder

## 🐛 Known Issues

None reported yet.

## 📦 Migration Guide

No migration needed - this is a pure addition. Existing workflows continue to work as before.

---

**Version**: 2.1.0  
**Release Date**: November 2025  
**Compatibility**: Requires JDK 17+, Gradle 8.4+
