# Payroll Calculator Tests

This directory contains comprehensive unit and integration tests for the Albion Payroll Calculator.

## Test Structure

### Unit Tests

#### `PayrollCalculatorTest.kt`
Tests for core calculation functions:
- **Tax Calculations** - Verifies 80/10/10 split (after-tax/returns/tax)
- **Return Multipliers** - Tests scaling based on participant count
- **Participant Parsing** - Tests full share vs 50% share parsing
- **Simple Payroll** - Basic 2-player scenario
- **Organizer Logic** - Verifies organizer gets correct shares and return points
- **Reduced Share** - Tests 50% participants get half the payout
- **CTA Calculations** - Verifies Call to Arms return point distribution
- **Recruitment Points** - Tests recruitment bonus system
- **Formatting Functions** - Number formatting with spaces and 'k' suffix

#### `StringSimilarityTest.kt`
Tests for name similarity detection:
- **Identical Strings** - Should return 1.0
- **Different Strings** - Should return low similarity
- **Empty Strings** - Edge case handling
- **Similar Names** - Real examples from payroll data
- **Typos** - Minor spelling differences
- **Prefix Matching** - Jaro-Winkler prefix bonus

### Integration Tests

#### `PayrollIntegrationTest.kt`
End-to-end tests with realistic scenarios:
- **Real World Scenario** - Multi-haul content with 6 participants
- **Complex Scenario** - Organizer + caller + participants
- **Multiple Contents and CTAs** - Full payroll cycle
- **Recruitment Integration** - Multiple recruiters with different points

## Running Tests

### Run All Tests
```bash
./gradlew allTests
```

### Run Desktop Tests Only
```bash
./gradlew desktopTest
```

### Run Native Tests Only
```bash
./gradlew nativeTest
```

### Run with Detailed Output
```bash
./gradlew allTests --info
```

### View Test Reports
After running tests, open:
```
build/reports/tests/allTests/index.html
```

## Test Coverage

The tests cover:
- ✅ Tax calculation (80/10/10 split)
- ✅ Rounding behavior (down to thousands)
- ✅ Return point multipliers (1-5 players = 1x, 6-10 = 2x, etc.)
- ✅ Organizer bonuses (2 shares + return points)
- ✅ Caller bonuses (2 shares + return points)
- ✅ Full share vs 50% share participants
- ✅ CTA return points (2x multiplier)
- ✅ Recruitment return points
- ✅ Multiple contents and hauls
- ✅ Name similarity detection
- ✅ Number formatting

## Adding New Tests

When adding features, add corresponding tests:

1. **Unit tests** for individual functions
2. **Integration tests** for complete workflows
3. **Edge cases** for boundary conditions

Example:
```kotlin
@Test
fun testNewFeature() {
    // Arrange
    val input = createTestInput()
    
    // Act
    val result = calculateSomething(input)
    
    // Assert
    assertEquals(expected, result, "Description of what should happen")
}
```

## Test Data

Tests use realistic values from actual payroll runs:
- Item amounts in thousands (e.g., 3770000 = 3770k)
- Cash amounts in thousands
- Real player names (anonymized in some cases)
- Actual content/haul structures

## Continuous Integration

These tests should be run:
- ✅ Before committing changes
- ✅ Before creating releases
- ✅ After refactoring
- ✅ When adding new features

## Known Limitations

- Tests don't cover file I/O (parsing is in native-specific code)
- Energy balance calculations not yet tested
- Discord markdown formatting not tested (output formatting)

## Future Test Additions

Consider adding:
- Performance tests for large payrolls
- Stress tests with many participants
- Property-based tests for calculation invariants
- Regression tests for specific bug fixes
