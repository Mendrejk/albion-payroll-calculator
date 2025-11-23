# Algorytm obliczania rozliczenia

## FORMAT PLIKU WEJŚCIOWEGO (nawiasy oznaczają komenatrz, nie są częścią pliku):
```
{Wszystkie kontenty z danego cyklu rozliczeniowego w ręcznie zapisanym formacie (nazwa skarbonki nie ma znaczenia)}
{Nagłówek} KONTENTY:
{opcjonalnie, nazwa skarbonki poprzedzona znakiem #. Linie rozpoczęte znakiem # są ignorowane przez algorytm (np. # HO1:)}
{Numer Kontentu} 1: {nick organizatora lub puste jeśli brak}
  {Numery Zwózki} 1: {ilość kasy w itemach}, {ilość kasy w złocie (sam przecinek jeśli brak)}, {uczestnik 1}, {uczestnik 2}, ... (nie licząc organizatora)
  2: {ilość kasy w itemach}, itd ...
2: {nick organizatora lub puste jeśli brak}
  1: itd ...
3: itd ...

REKRUTACJA:
{Nick Rekutera}: {Ilość punktów zwrotu podatku}
itd ...
```

## ALGORYTM:
1. Wczytaj plik wejściowy
2. Pobierz kontenty, pogrupuj po nich
3. Dla każdego kontentu:
   1. Odlicz od każdej kwoty w itemach i w kasie 20% - 10% na poczet gildii oraz 10% na poczet zwrotów podatku, zaokrąglając pozostałe kwoty rozliczeniowe w dół do pełnych tysięcy.
   2. JEŚLI KONTENT MA ORGANIZATORA: dolicz organizatorowi 1 punkt zwrotu podatku. Dodaj go do listy rozliczanych jeśli w niej nie jest.
   3. JEŚLI KONTENT MA ORGANIZATORA: Oblicz wartość punktu zwrotu do podziału w ramach zwózki, dzieląc 1 przez ilość zwózek w contencie (np. 4 zwózki -> 0.25 punkta zwrotu do podziału w każdej zwózce)
   3. Dla każdej zwózki:
      1. Oblicz **JEDNOSTKĘ** wypłaty osobno dla itemów i kasy wyliczoną według wzoru: łączna kwota podzielona przez sumę: (2 za każdego pełnoprawnego uczestnika, 1 za każdego uczestnika z dopiskiem (50%) po nicku), zaokrąglając w dół do pełnych tysięcy
      2. JEŚLI KONTENT MA ORGANIZATORA: Oblicz wartość punktów zwrotu dla każdego uczestnika według wzoru: (wartość punktu zwrotu dla zwózki / ilość uczestników zwózki, nie licząc organizatora), zaoakrąglając w dół do pełnych tysięcy (np. punkty zwrotu dla zwózki = 0.2, 4 uczestników bez organizatora -> wartość punktów zwrotu dla uczestnika = 0.05)
      3. Dla każdego uczestnika zwózki (licząc organizatora, jeśli jest):
         1. Dodaj go do listy rozliczanych jeśli w niej nie jest. Dolicz mu obliczone kwoty w itemach i w kasie według wzoru: **JEDNOSTKA** * (2 za pełnoprawnego uczestnika, 1 za uczestnika z dopiskiem (50%)).
         2. JEŚLI KONTENT MA ORGANIZATORA I DANY UCZESTNIK NIE JEST ORGANIZATOREM: dolicz mu obliczoną wartość punktów zwrotu dla zwózki.
4. Weź rekrutacje, pogrupuj po rekruterach
5. Dla każdego rekrutera:
   1. Dodaj go do listy rozliczanych jeśli w niej nie jest. Dopisz mu podane punkty zwrotu podatku.
6. Na podstawie puli zwrotów podatku i łącznej sumy rozdanych punktów zwrotu podatku (punkty rekruterów + 2 punkty za każdy kontent z organizatorem), oblicz wartość odpowiadającą 1 punktowi zwrotu podatku, zaokrąglając w dół do pełnych tysięcy.
7. Dla każdego rozliczanego:
   1. Oblicz i zapisz wartość zwrotu podatku dla danego rozliczanego, używając obliczonej wartości za 1 punkt zwrotu, zaokrąglając w dół do pełnych tysięcy.
8. Zapisz wynik do pliku wyjściowego w formacie csv:
   1. Nagłówek: `Nick,Itemy,Kasa,Zwrot Podatku`
   2. Dla każdego rozliczanego: `Nick,Rozliczenie w itemach,Rozliczenie w kasie,Zwrot podatku`
9. Rozdziel rozliczenie do skrzyń, napisz wiadomość z ogłoszeniem rozliczenia.
TODO: Generować wiadomość z ogłoszeniem rozliczenia (markdown)

# UWAGI:
- Wszystkie wartości są zawsze liczbami naturalnymi
- Rozmiar liter w nickach nie ma znaczenia