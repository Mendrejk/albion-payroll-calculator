# Interactive Input Guide / Przewodnik Interaktywnego Wprowadzania

## English

### Overview
The Interactive Input feature allows you to build payroll input step-by-step directly in the GUI, without manually editing text files.

### How to Use

1. **Launch the GUI**
   - Double-click `run-gui.ps1` or run `./gradlew run`
   - The application opens with three tabs: **Kreator**, **Plik**, **Wyniki**

2. **Kreator Tab (Interactive Builder)**
   - This is the new interactive input builder
   - Three sections: **Kontenty**, **CTA**, **Rekrutacja**

#### Adding Contents (Kontenty)

1. Click **"Dodaj Kontent"** to add a new content
2. Fill in:
   - **ID Kontentu**: Content number (e.g., "1", "2", "3")
   - **Organizator**: Optional organizer name (e.g., "smokq", "fenix")
3. Click **"Dodaj Haul"** to add hauls to this content
4. For each haul, fill in:
   - **Przedmioty (k)**: Items value in thousands (e.g., "3770" for 3.77M)
   - **Gotówka (k)**: Cash value in thousands (e.g., "0" or "3670")
   - **Lokacja**: Select location (BEACH, FORT, MISTS, ROADS)
   - **Tab**: Tab number (usually "1", "2", or "3")
   - **Organizator był obecny**: Check if organizer was present
   - **Caller**: Optional caller name (e.g., "karoll" or "karoll(il)")
   - **Uczestnicy**: Comma-separated participant names (e.g., "orzech, yuegre, arkadius")

#### Adding CTA Events

1. Switch to the **CTA** section
2. Click **"Dodaj CTA"**
3. Fill in:
   - **ID CTA**: CTA number
   - **Caller**: CTA caller name
   - **Uczestnicy**: Comma-separated participant names

#### Adding Recruitment Points

1. Switch to the **Rekrutacja** section
2. Click **"Dodaj Rekrutację"**
3. Fill in:
   - **Rekruter**: Recruiter name
   - **Punkty**: Points value (e.g., "6" or "3.5")

#### Calculate Results

1. Click **"Oblicz"** at the bottom to calculate and view results
2. Or click **"Użyj tego wejścia"** to save the input for later use

---

## Polski

### Przegląd
Funkcja Interaktywnego Wprowadzania pozwala na tworzenie danych wejściowych krok po kroku bezpośrednio w GUI, bez ręcznej edycji plików tekstowych.

### Jak Używać

1. **Uruchom GUI**
   - Kliknij dwukrotnie `run-gui.ps1` lub uruchom `./gradlew run`
   - Aplikacja otwiera się z trzema zakładkami: **Kreator**, **Plik**, **Wyniki**

2. **Zakładka Kreator (Interaktywny Kreator)**
   - To jest nowy interaktywny kreator danych wejściowych
   - Trzy sekcje: **Kontenty**, **CTA**, **Rekrutacja**

#### Dodawanie Kontentów

1. Kliknij **"Dodaj Kontent"** aby dodać nowy kontent
2. Wypełnij:
   - **ID Kontentu**: Numer kontentu (np. "1", "2", "3")
   - **Organizator**: Opcjonalna nazwa organizatora (np. "smokq", "fenix")
3. Kliknij **"Dodaj Haul"** aby dodać haule do tego kontentu
4. Dla każdego haula wypełnij:
   - **Przedmioty (k)**: Wartość przedmiotów w tysiącach (np. "3770" dla 3.77M)
   - **Gotówka (k)**: Wartość gotówki w tysiącach (np. "0" lub "3670")
   - **Lokacja**: Wybierz lokację (BEACH, FORT, MISTS, ROADS)
   - **Tab**: Numer taba (zazwyczaj "1", "2", lub "3")
   - **Organizator był obecny**: Zaznacz jeśli organizator był obecny
   - **Caller**: Opcjonalna nazwa callera (np. "karoll" lub "karoll(il)")
   - **Uczestnicy**: Nazwy uczestników oddzielone przecinkami (np. "orzech, yuegre, arkadius")

#### Dodawanie Wydarzeń CTA

1. Przejdź do sekcji **CTA**
2. Kliknij **"Dodaj CTA"**
3. Wypełnij:
   - **ID CTA**: Numer CTA
   - **Caller**: Nazwa callera CTA
   - **Uczestnicy**: Nazwy uczestników oddzielone przecinkami

#### Dodawanie Punktów Rekrutacji

1. Przejdź do sekcji **Rekrutacja**
2. Kliknij **"Dodaj Rekrutację"**
3. Wypełnij:
   - **Rekruter**: Nazwa rekrutera
   - **Punkty**: Wartość punktów (np. "6" lub "3.5")

#### Oblicz Wyniki

1. Kliknij **"Oblicz"** na dole aby obliczyć i zobaczyć wyniki
2. Lub kliknij **"Użyj tego wejścia"** aby zapisać dane wejściowe do późniejszego użycia

---

## Tips / Wskazówki

- **Participant names**: Use lowercase, consistent names (e.g., always "orzech", not "Orzech")
- **Half shares**: Add "(il)" suffix for half shares (e.g., "karoll(il)")
- **Validation**: The app will show errors if the input format is incorrect
- **Save your work**: Use "Użyj tego wejścia" to preserve your input before calculating

---

- **Nazwy uczestników**: Używaj małych liter, spójnych nazw (np. zawsze "orzech", nie "Orzech")
- **Połówki**: Dodaj sufiks "(il)" dla połówek (np. "karoll(il)")
- **Walidacja**: Aplikacja pokaże błędy jeśli format danych jest nieprawidłowy
- **Zapisz swoją pracę**: Użyj "Użyj tego wejścia" aby zachować dane przed obliczeniem
