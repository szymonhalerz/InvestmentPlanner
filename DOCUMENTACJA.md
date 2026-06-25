# Dokumentacja aplikacji „InvestmentPlanner”

## 1. Opis koncepcji programu – lista wymagań

InvestmentPlanner jest lokalną aplikacją mobilną wspierającą planowanie celów finansowych. Nie wymaga logowania ani Internetu. Cele, wpłaty, ustawienia poduszki finansowej oraz plan inwestycyjny są zapisywane w bazie SQLite przez bibliotekę Room.

Zrealizowane funkcje:

1. Dodawanie celu z nazwą, kategorią, kwotą docelową i opisem.
2. Wybór kategorii z gotowych, kolorowych chipów zamiast ręcznego wpisywania.
3. Lista celów z kwotami, procentem i paskiem postępu.
4. Szczegóły celu, historia wpłat, miesięczny plan i prognoza realizacji.
5. Dodawanie oraz usuwanie wpłat.
6. Usuwanie celu razem z przypisanymi wpłatami.
7. Dashboard podsumowujący liczbę celów, zebrane środki, plan miesiąca i brakującą wpłatę.
8. Kalkulator i lokalny zapis poduszki finansowej z potwierdzeniem Snackbar.
9. Edukacyjny plan inwestycyjny zależny od profilu ryzyka.
10. Automatyczne tworzenie osobnych celów finansowych zgodnie z procentami planu.
11. Wykres donut pokazujący rzeczywisty łączny postęp wszystkich celów.
12. Miesięczny status każdego celu i suma wpłat z bieżącego miesiąca.
13. Dwa wykresy Canvas: postęp miesięczny i długoterminowy.
14. Profil finansowy z dochodem, wydatkami, możliwościami inwestowania i ryzykiem.
15. Priorytety celów: wysoki, średni i niski.
16. Ekran analizy finansowej oraz wykres podziału kategorii.
17. Kalkulator procentu składanego.
18. Dolna nawigacja: Dashboard, Cele, Analiza i Kalkulator.
19. Cele inwestycyjne, poduszka finansowa oraz cele zakupowe/dowolne.
20. Kalkulator wkładu własnego na mieszkanie.
21. Kokpit miesiąca z listą wymaganych wpłat i sugestią działania.
22. Szybka wpłata z wyborem celu, kwoty, daty i opisu.
23. Walidacja kwot, horyzontu oraz wymaganych pól formularzy.

### Dostępne kategorie inwestycji

- 📊 ETF-y
- 📈 Akcje
- 🏛️ Obligacje skarbowe
- 🪙 Metale szlachetne
- ₿ Kryptowaluty
- 💰 Gotówka / konto oszczędnościowe
- 🚗 Zakup samochodu
- 🏠 Zakup mieszkania
- 🛟 Poduszka finansowa
- ✨ Inne

## 2. Opis technologii wykonania

- **Kotlin** – język aplikacji.
- **Jetpack Compose i Material 3** – interfejs użytkownika.
- **Room Database / SQLite** – lokalne przechowywanie danych.
- **KSP** – generowanie kodu Room bez `kapt`.
- **ViewModel, Kotlin Coroutines i Flow** – stan ekranów i reaktywna aktualizacja danych.
- **Navigation Compose** – nawigacja między ekranami.
- **Gradle Kotlin DSL** – konfiguracja projektu.

Minimalna wersja systemu to Android 8.0 (API 26), a kod jest kompilowany dla Javy 17.

## 3. Opis implementacji i struktury programu

Projekt nadal zawiera jeden prosty moduł `app` i zachowuje pierwotny podział:

```text
pl.edu.investmentplanner
├── MainActivity.kt
├── InvestmentPlannerApplication.kt
├── InvestmentViewModel.kt
├── data
│   ├── InvestmentGoalEntity.kt
│   ├── PaymentEntity.kt
│   ├── FinancialCushionEntity.kt
│   ├── InvestmentPlanEntity.kt
│   ├── UserProfileEntity.kt
│   ├── GoalWithTotal.kt
│   ├── InvestmentDao.kt
│   ├── InvestmentDatabase.kt
│   └── InvestmentRepository.kt
└── ui
    ├── InvestmentPlannerApp.kt
    ├── InvestmentPlannerTheme.kt
    ├── InvestmentCategories.kt
    ├── FinancialProgressChart.kt
    ├── MonthlyPlanChart.kt
    ├── GoalForecast.kt
    ├── CategoryBreakdownChart.kt
    ├── MainBottomBar.kt
    ├── GoalTypes.kt
    ├── HomeScreen.kt
    ├── GoalsScreen.kt
    ├── AnalysisScreen.kt
    ├── CompoundInterestScreen.kt
    ├── UserProfileScreen.kt
    ├── HousingDownPaymentScreen.kt
    ├── QuickPaymentScreen.kt
    ├── AddGoalScreen.kt
    ├── GoalDetailsScreen.kt
    ├── AddPaymentScreen.kt
    ├── FinancialCushionScreen.kt
    ├── InvestmentPlanScreen.kt
    └── Formatters.kt
```

### Baza danych

`InvestmentGoalEntity` reprezentuje cel. `PaymentEntity` zawiera klucz obcy `goalId`; relacja ma typ jeden-do-wielu. Usunięcie celu kaskadowo usuwa wpłaty.

`FinancialCushionEntity` i `InvestmentPlanEntity` są pojedynczymi rekordami o stałym `id = 1`. Przechowują ostatnie ustawienia użytkownika. `InvestmentGoalEntity.generatedByPlan` rozróżnia cele ręczne od utworzonych przez plan. Każdy cel przechowuje również `horizonMonths` i `plannedMonthlyPayment`.

Wersja bazy wynosi 7. Migracja `MIGRATION_6_7` dodaje pole `goalType` i przypisuje istniejące cele do inwestycji, poduszki albo celów zakupowych. Wcześniejsze migracje zachowują priorytety, profil, horyzonty oraz miesięczne plany. Dla nieobsługiwanych wersji pozostawiono `fallbackToDestructiveMigration(true)`.

Room zwraca dane jako `Flow`. Po zmianie wpłaty dashboard, suma i procent celu aktualizują się automatycznie.

Zapytania celów zwracają dwie sumy: wszystkie wpłaty (`totalPaid`) oraz wpłaty od początku bieżącego miesiąca (`monthlyPaid`). Kwota początkowa podawana przy tworzeniu celu jest zapisywana jako historyczna wpłata sprzed początku miesiąca, dlatego nie zawyża wykonania bieżącego planu.

### Miesięczny plan celu

Podczas dodawania celu użytkownik podaje kwotę docelową, już zebraną kwotę oraz horyzont w miesiącach. Wymagana wpłata jest liczona wzorem:

```text
planowana wpłata miesięczna = (kwota docelowa − zebrana kwota) / horyzont w miesiącach
pozostało w miesiącu = max(planowana wpłata − wpłaty w tym miesiącu, 0)
```

Cele utworzone przez plan inwestycyjny dostają horyzont `liczba lat × 12` i miesięczną wpłatę zgodną z udziałem portfela. Przy 3000 zł profil zrównoważony tworzy miesięcznie: ETF-y 1500 zł, obligacje 900 zł, akcje 300 zł i metale 300 zł.

### Status i prognoza celu

- **Lekko opóźniony** – wykonano mniej niż 75% planu,
- **Na dobrej drodze** – wykonano od 75% do mniej niż 100%,
- **Cel wykonany** – osiągnięto miesięczny plan lub cały cel długoterminowy,
- **Cel przekroczony** – wpłacono więcej niż zakładano w miesiącu.

Prognoza dzieli sumę historii przez liczbę miesięcy kalendarzowych od pierwszej wpłaty. Pozostała kwota podzielona przez tę średnią daje szacowaną liczbę miesięcy do celu. Jeśli średnia jest niższa od planowanej wpłaty, ekran pokazuje miesięczny brak.

### Profil użytkownika i budżet miesięczny

`UserProfileEntity` przechowuje miesięczny dochód, wydatki, możliwą kwotę inwestycji, sytuację życiową i profil ryzyka. Profil jest pojedynczym rekordem Room o `id = 1`. Formularz sprawdza, czy deklarowana inwestycja nie przekracza nadwyżki `dochód − wydatki`.

Dashboard porównuje sumę planowanych wpłat wszystkich celów z kwotą możliwą do inwestowania. Pokazuje, ile środków zostanie po realizacji planu albo o ile należy zmniejszyć plany.

### Priorytety celów

Każdy cel ma priorytet wysoki, średni lub niski. Lista celów jest sortowana według priorytetu. Cele wysokiego priorytetu mają zielone obramowanie i są prezentowane w osobnej sekcji dashboardu.

### Cele dowolne i zakupowe

Każdy cel ma typ: `Inwestycyjny`, `Poduszka finansowa` albo `Zakupowy / dowolny`. Typ ogranicza listę kategorii w formularzu. Dla celów zakupowych dostępne są m.in. wkład własny, mieszkanie, samochód, remont, wakacje, sprzęt, kurs i inny cel. Wszystkie typy korzystają z tej samej logiki wpłat, miesięcznego planu, statusu oraz prognozy.

### Wkład własny na mieszkanie

Specjalny kalkulator przyjmuje cenę mieszkania, udział wkładu 10/20/30%, koszty dodatkowe, zebraną kwotę oraz planowany miesiąc zakupu.

```text
wymagany wkład = cena mieszkania × procent wkładu
cel końcowy = wymagany wkład + koszty dodatkowe
brakująca kwota = max(cel końcowy − zebrana kwota, 0)
wpłata miesięczna = brakująca kwota / liczba miesięcy do zakupu
```

Dla ceny 500 000 zł, wkładu 20% i kosztów 30 000 zł celem jest 130 000 zł. Zapis tworzy zwykły cel zakupowy Room.

### Kokpit miesiąca i sugestia

Sekcja „Ten miesiąc” łączy sumę planowanych wpłat, wpłaty od początku miesiąca, pozostałą kwotę i procent. Niżej pokazuje maksymalnie pięć celów wymagających działania. Kolejność uwzględnia najpierw wysoki priorytet, a następnie największą brakującą miesięczną kwotę.

Karta „Najbliższe działanie” wybiera jedno zadanie z tej samej kolejki. Gdy wszystkie wpłaty wykonano, informuje o realizacji miesiąca w 100%.

### Szybka wpłata

Szybka wpłata jest dostępna bezpośrednio z kokpitu. Użytkownik wybiera cel, kwotę, datę w formacie `RRRR-MM-DD` i opis. Rekord trafia do `PaymentEntity`; Room `Flow` natychmiast aktualizuje cel, kokpit, analizę i wykresy. Po zapisie dashboard pokazuje Snackbar „Wpłata dodana”.

### Kalkulator procentu składanego

Kalkulator używa miesięcznej kapitalizacji:

```text
r = roczna stopa / 12
n = liczba lat × 12
wynik = kwota początkowa × (1+r)^n + wpłata miesięczna × ((1+r)^n − 1) / r
```

Dla stopy 0% zwracana jest po prostu suma wpłat. Wynik jest edukacyjną symulacją bez podatków, opłat i inflacji.

### Obliczanie poduszki finansowej

Sytuacji życiowej i stabilności dochodu przypisywane są proste punkty ryzyka. Suma punktów daje rekomendację:

- najniższe ryzyko – 3 miesiące,
- niskie lub standardowe ryzyko – 6 miesięcy,
- podwyższone ryzyko – 9 miesięcy,
- najwyższe ryzyko – 12 miesięcy.

```text
kwota poduszki = miesięczne wydatki × liczba miesięcy
brakująca kwota = max(kwota poduszki − zebrana kwota, 0)
procent = zebrana kwota / kwota poduszki × 100%
```

### Plan inwestycyjny

Plan korzysta z trzech stałych profili:

- **Ostrożny:** 60% obligacje/gotówka, 30% ETF-y, 10% metale szlachetne.
- **Zrównoważony:** 50% ETF-y, 30% obligacje, 10% akcje, 10% metale szlachetne.
- **Dynamiczny:** 70% ETF-y/akcje, 15% obligacje, 10% metale szlachetne, 5% kryptowaluty.

Aplikacja oblicza miesięczną kwotę dla każdej części i sumę wpłat w całym horyzoncie. Nie prognozuje stopy zwrotu.

Zapis planu jest transakcją Room. Najpierw usuwane są wyłącznie poprzednie cele oznaczone jako wygenerowane przez plan, następnie zapisywany jest plan i wstawiana nowa lista celów wraz z miesięcznymi wpłatami. Cele dodane ręcznie pozostają bez zmian. Kwota każdego celu jest liczona jako:

```text
łączna kwota = miesięczna wpłata × 12 × liczba lat
kwota celu = łączna kwota × procent składnika portfela
```

Dla przykładu 3000 zł miesięcznie przez 5 lat daje 180 000 zł. Profil zrównoważony tworzy cele: ETF-y 90 000 zł, obligacje 54 000 zł, akcje 18 000 zł i metale szlachetne 18 000 zł.

## 4. Opis warstwy użytkowej z widokami ekranu

### Dashboard

[Tutaj wstawić zrzut ekranu]

Ekran startowy odpowiada na pięć pytań: ile wpłacić, ile już wpłacono, gdzie wpłacić teraz, jak idzie długoterminowy plan i który cel jest najważniejszy. Kolejne sekcje to „Ten miesiąc”, „Najbliższe działanie”, „Postęp długoterminowy”, „Wysoki priorytet” i „Narzędzia finansowe”.

Sekcja miesięczna zawiera wykres słupkowy Canvas pokazujący wpłaconą i pozostałą część planu. Sekcja długoterminowa zawiera wykres donut Canvas wykorzystujący sumę wszystkich wpłat i wszystkich kwot docelowych. Dashboard pokazuje także, czy plan mieści się w budżecie profilu.

### Profil finansowy

[Tutaj wstawić zrzut ekranu]

Formularz pozwala zapisać dochód, wydatki, kwotę możliwą do inwestowania, sytuację życiową i profil ryzyka. Po zapisie aplikacja wraca na dashboard i pokazuje Snackbar.

### Cele

[Tutaj wstawić zrzut ekranu]

Osobna zakładka dolnej nawigacji dzieli cele na sekcje „Inwestycje”, „Poduszka finansowa” i „Cele zakupowe / dowolne”. W każdej sekcji cele są sortowane od wysokiego do niskiego priorytetu.

### Analiza

[Tutaj wstawić zrzut ekranu]

Ekran pokazuje łączną wartość celów, zebraną kwotę, procent całości, wykonanie miesiąca, największy cel, najlepiej realizowany cel i ocenę, czy użytkownik jest na dobrej drodze. Zawiera wykres miesięczny, długoterminowy oraz donut podziału celów według kategorii.

### Kalkulator

[Tutaj wstawić zrzut ekranu]

Użytkownik podaje kwotę początkową, miesięczną wpłatę, liczbę lat i roczną stopę zwrotu. Wynik, suma wpłat i szacowany zysk aktualizują się automatycznie. Ekran wyraźnie informuje, że jest to symulacja edukacyjna.

### Dodawanie celu

[Tutaj wstawić zrzut ekranu]

Formularz zaczyna się od wyboru typu celu, a następnie pokazuje odpowiednie kategorie. Zawiera nazwę, kategorię, priorytet, kwotę docelową, zebraną kwotę, horyzont i opis. Dla typu zakupowego udostępnia skrót do kalkulatora wkładu własnego.

### Szczegóły celu

[Tutaj wstawić zrzut ekranu]

Widok pokazuje postęp długoterminowy, horyzont, miesięczny plan, wpłaty w miesiącu, pozostałą kwotę, status oraz osobny pasek miesięczny. Karta prognozy prezentuje średnią miesięczną wpłatę, szacowany czas dojścia do celu i ewentualny miesięczny brak. Niżej pozostaje historia wpłat z możliwością dodawania i usuwania.

### Poduszka finansowa

[Tutaj wstawić zrzut ekranu]

Użytkownik wpisuje miesięczne wydatki i zebraną kwotę oraz wybiera sytuację życiową i stabilność dochodu. Ekran na bieżąco pokazuje rekomendowaną liczbę miesięcy, cel, brakującą kwotę i procent. Kwoty muszą być większe od zera.

Przycisk zapisuje rekord w Room. Dopiero po zakończeniu operacji aplikacja wraca do dashboardu, odświeża kartę poduszki i pokazuje Snackbar „Poduszka finansowa zapisana”. Karta prezentuje procent, kwotę zebraną, cel i brakującą kwotę.

### Plan inwestycyjny

[Tutaj wstawić zrzut ekranu]

Użytkownik wpisuje dodatnią miesięczną kwotę i liczbę lat od 1 do 60 oraz wybiera profil ryzyka. Ekran pokazuje procentowy podział, kwoty miesięczne i łączną sumę wpłat. Widoczna jest informacja, że wynik jest edukacyjną symulacją, a nie poradą inwestycyjną.

Po zakończeniu transakcji Room aplikacja wraca do dashboardu, aktualizuje kartę planu, tworzy cele w sekcji „Twoje cele” i pokazuje Snackbar „Plan inwestycyjny zapisany”.

## 5. Miejsce na link do repozytorium

Link do repozytorium: [tutaj wkleić link]

## 6. Materiały źródłowe

1. Android Developers – Jetpack Compose: <https://developer.android.com/compose>
2. Android Developers – Material 3: <https://developer.android.com/develop/ui/compose/designsystems/material3>
3. Android Developers – Room: <https://developer.android.com/training/data-storage/room>
4. Android Developers – ViewModel: <https://developer.android.com/topic/libraries/architecture/viewmodel>
5. Android Developers – Navigation Compose: <https://developer.android.com/develop/ui/compose/navigation>
6. Kotlin – dokumentacja języka: <https://kotlinlang.org/docs/home.html>
7. KSP – Kotlin Symbol Processing: <https://kotlinlang.org/docs/ksp-overview.html>
