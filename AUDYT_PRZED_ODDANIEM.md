# Audyt przed oddaniem projektu

Projekt: **InvestmentPlanner**  
Temat: **Mobilna aplikacja do planowania i monitorowania inwestycji**  
Data audytu: **25.06.2026**

## Podsumowanie

Projekt spełnia wymagania zaliczeniowe dla aplikacji Android tworzonej w Kotlinie z użyciem Jetpack Compose oraz lokalnej bazy danych Room/SQLite. Aplikacja umożliwia tworzenie celów finansowych, dodawanie wpłat, wyświetlanie historii wpłat, obliczanie procentu realizacji oraz prezentowanie podstawowych statystyk i wizualizacji.

Wykonano pełny build Gradle:

```powershell
.\gradlew.bat build --console=plain
```

Wynik: **BUILD SUCCESSFUL**.

## Tabela audytu

| Obszar | Status | Uwagi |
| ------ | ------ | ----- |
| Android / Kotlin | OK | Projekt zawiera moduł `app`, manifest Androida, `MainActivity` oraz kod w Kotlinie. |
| Jetpack Compose | OK | UI jest zbudowane w Jetpack Compose, z użyciem Material 3 i Navigation Compose. |
| Room Database / SQLite | OK | Projekt używa Room (`room-runtime`, `room-ktx`, `room-compiler` przez KSP). Room zapisuje dane w lokalnej bazie SQLite. |
| Konfiguracja Gradle | OK | Projekt używa Gradle Kotlin DSL, Android Gradle Plugin, Kotlin Android, Compose plugin i KSP. Nie wykryto konfliktu kapt/KSP. |
| MainActivity | OK | `MainActivity` uruchamia Compose przez `setContent` i ładuje `InvestmentPlannerApp`. |
| Encja celu | OK | Istnieje `InvestmentGoalEntity` z nazwą, kategorią, kwotą docelową, opisem i dodatkowymi polami planistycznymi. |
| Encja wpłaty | OK | Istnieje `PaymentEntity` z `goalId`, kwotą, datą i notatką. |
| Relacja cel-wpłaty | OK | `PaymentEntity` ma klucz obcy `goalId` do `InvestmentGoalEntity`; usunięcie celu usuwa powiązane wpłaty kaskadowo. |
| DAO | OK | `InvestmentDao` zawiera zapytania do odczytu celów, szczegółów celu, historii wpłat oraz operacje insert/delete. |
| Lokalny zapis danych | OK | Cele, wpłaty, poduszka finansowa, plan inwestycyjny i profil użytkownika są zapisywane lokalnie w Room. |
| Migracje bazy | OK | Baza ma wersję 7, z migracjami oraz `fallbackToDestructiveMigration(true)` dla bezpieczeństwa w projekcie studenckim. |
| Dodawanie celów | OK | `AddGoalScreen` pozwala dodać cel z nazwą, typem, kategorią, kwotą, kwotą zebraną, horyzontem, priorytetem i opisem. |
| Kategorie celu | OK | Kategorie są wybierane z listy/chipów, a nie wpisywane ręcznie. |
| Lista celów | OK | Dashboard i ekran `Cele` pokazują listy celów. Ekran `Cele` dzieli je na inwestycje, poduszkę i cele zakupowe/dowolne. |
| Szczegóły celu | OK | `GoalDetailsScreen` pokazuje kwotę docelową, sumę wpłat, procent realizacji, miesięczny plan i prognozę. |
| Dodawanie wpłat | OK | `AddPaymentScreen` oraz `QuickPaymentScreen` zapisują wpłaty do lokalnej bazy. |
| Historia wpłat | OK | Historia wpłat jest widoczna w szczegółach celu i pobierana z tabeli `payments`. |
| Usuwanie celu | OK | Szczegóły celu zawierają akcję usunięcia celu. |
| Usuwanie wpłaty | OK | Historia wpłat zawiera akcję usunięcia pojedynczej wpłaty. |
| Procent realizacji celu | OK | `GoalWithTotal` oblicza sumę wpłat, pozostałą kwotę, procent i postęp celu. |
| Wizualizacja danych | OK | Aplikacja używa pasków postępu oraz wykresów Canvas: postęp miesięczny, postęp długoterminowy i podział kategorii. |
| Dashboard | OK | Dashboard pokazuje podsumowanie, plan miesiąca, najbliższe działanie, postęp długoterminowy i narzędzia finansowe. |
| Walidacja formularzy | OK | Formularze sprawdzają m.in. puste kwoty, kwoty większe od zera i poprawny horyzont. |
| Dokumentacja | OK | `DOCUMENTACJA.md` istnieje i zawiera 6 wymaganych punktów, miejsce na link do repozytorium oraz miejsca na zrzuty ekranu. |
| Repozytorium Git | OK | Projekt zawiera folder `.git`, branch `main` i zdalny adres `origin`. Po audycie są niezacommitowane zmiany do dodania. |
| Pliki potrzebne do Android Studio | OK | Repozytorium zawiera `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `gradlew.bat` i `gradle.properties`. |
| Build projektu | OK | `.\gradlew.bat build --console=plain` zakończył się sukcesem. |
| Uruchomienie na fizycznym urządzeniu | NIE SPRAWDZONO | Build tworzy APK, ale fizyczne uruchomienie na telefonie trzeba sprawdzić ręcznie. |
| Publikacja na GitHub | NIE SPRAWDZONO | Zdalny adres jest ustawiony, ale wysłanie zależy od logowania do GitHuba na komputerze użytkownika. |

## Naprawione podczas audytu

1. Poprawiono użycie `getBackStackEntry()` w `InvestmentPlannerApp.kt`, ponieważ lint Navigation Compose blokował pełny build projektu.
2. Uzupełniono `.gitignore` o lokalne katalogi robocze: `.kotlin/`, `.codex/`, `.agents/`, `work/`, `outputs/`.
3. Uzupełniono `DOCUMENTACJA.md` o wymagany placeholder linku:

```text
Link do repozytorium: [tutaj wkleić link]
```

4. Dodano miejsca na zrzuty ekranu w opisie warstwy użytkowej:

```text
[Tutaj wstawić zrzut ekranu]
```

## Co trzeba sprawdzić ręcznie przed oddaniem

1. Otworzyć projekt w Android Studio i uruchomić aplikację na emulatorze albo telefonie.
2. Dodać przykładowy cel, np. ETF-y lub samochód.
3. Dodać wpłatę do celu i sprawdzić, czy procent realizacji się zmienia.
4. Wejść w szczegóły celu i sprawdzić historię wpłat.
5. Sprawdzić, czy po zamknięciu i ponownym uruchomieniu aplikacji dane nadal są widoczne.
6. Wstawić własne zrzuty ekranu do `DOCUMENTACJA.md`.
7. Wkleić link do repozytorium GitHub w punkcie 5 dokumentacji.
8. Zacommitować zmiany po audycie i wypchnąć je na GitHub.

## Rekomendacja końcowa

Projekt jest gotowy do oddania po wykonaniu dwóch czynności organizacyjnych: uzupełnieniu linku do repozytorium w dokumentacji oraz wysłaniu aktualnego commita na GitHub. Funkcjonalnie i technicznie aplikacja spełnia wymagania tematu.
