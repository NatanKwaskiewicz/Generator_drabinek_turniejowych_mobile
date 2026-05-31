# Generator Drabinek Turniejowych — Aplikacja Mobilna

Aplikacja Android do przeglądania turniejów, zarządzania drużynami i śledzenia drabinek.

**Stack:** Android (Java), Retrofit 2, Material Design 3

---

## Wymagania

- Android Studio (Hedgehog lub nowszy)
- JDK 17
- Android SDK — `compileSdk 34`, `minSdk 26`
- Uruchomiony backend z aplikacji webowej (patrz niżej)

---

## Backend

> ⚠️ Aplikacja mobilna **nie posiada własnego backendu**. Aby działała, musisz uruchomić backend z poziomu [aplikacji webowej](../Generator_drabinek_turniejowych-main).

Kroki:

1. Sklonuj i skonfiguruj projekt webowy zgodnie z jego README
2. Uruchom backend:
   ```bash
   npm run dev:backend
   ```
3. Backend musi być dostępny na porcie `3000`

Aplikacja mobilna domyślnie łączy się pod adres `http://10.0.2.2:3000/`, który w emulatorze Androida odpowiada `localhost` komputera hosta. Jeśli uruchamiasz aplikację na **fizycznym urządzeniu**, zmień adres w pliku:

```
app/src/main/java/com/tourney/app/api/RetrofitClient.java
```

```java
private static final String BASE_URL = "http://<TWOJE_IP>:3000/";
```

---

## Uruchomienie

1. Otwórz projekt w Android Studio (`File → Open`)
2. Poczekaj na synchronizację Gradle
3. Podłącz emulator lub fizyczne urządzenie (Android 8.0+)
4. Kliknij **Run ▶** lub użyj skrótu `Shift+F10`
