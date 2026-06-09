# NexDam Client Portal

> Applicazione multi-piattaforma per la gestione dei progetti clienti NexDam.  
> Sviluppata in **Kotlin + Jetpack Compose**, disponibile su **Android** e **Desktop** (Windows/Linux).

---

## Indice

- [Panoramica](#panoramica)
- [Piattaforme](#piattaforme)
- [Funzionalità](#funzionalità)
- [Architettura](#architettura)
- [Requisiti](#requisiti)
- [Setup sviluppo](#setup-sviluppo)
- [Variabili e secret](#variabili-e-secret)
- [Build e rilascio](#build-e-rilascio)
- [Struttura del progetto](#struttura-del-progetto)
- [Database & Backend](#database--backend)

---

## Panoramica

NexDam Client Portal è l'app dedicata ai clienti NexDam per:
- Seguire l'avanzamento dei propri progetti
- Scambiare messaggi in tempo reale con il team NexDam
- Scaricare file e documenti caricati dall'amministratore
- Consultare e monitorare le fatture
- Ricevere notifiche push anche ad app completamente chiusa

Il backend è interamente basato su **Supabase** (PostgreSQL + Auth + Storage + Realtime + Edge Functions) e **Firebase Cloud Messaging** per le notifiche push.

---

## Piattaforme

| Piattaforma | Tecnologia | Output |
|---|---|---|
| 🤖 Android | Kotlin + Jetpack Compose | APK firmato |
| 🪟 Windows | Kotlin + Compose Desktop + JavaFX | Installer `.msi` |
| 🐧 Linux | Kotlin + Compose Desktop + JavaFX | Pacchetto `.deb` |

---

## Funzionalità

### Clienti
- **Autenticazione** — Login/registrazione con email + password, CAPTCHA Cloudflare Turnstile, conferma email
- **Dashboard** — Lista progetti con badge messaggi non letti, file, fatture
- **Progetto** — Dettaglio progetto con chat in tempo reale, file allegati, fatture
- **Profilo** — Visualizzazione e modifica dati personali
- **Blog** — Sezione notizie/aggiornamenti NexDam
- **Notifiche push** — Ricevi notifica anche ad app chiusa quando il team NexDam scrive un messaggio (Android, via FCM)

### Admin (team NexDam)
- Accesso a **tutti i progetti** di tutti i clienti
- Invio messaggi ai clienti (i messaggi admin fanno scattare la push notification)
- Gestione file e fatture per ogni progetto

---

## Architettura

```
nexdam-releases/
├── android/          # App Android (Kotlin + Jetpack Compose)
├── desktop/          # App Desktop Windows/Linux (Kotlin + Compose Desktop)
├── ios/              # App iOS (Swift/SwiftUI)
├── supabase/
│   ├── functions/    # Edge Functions Deno (send-push-notification)
│   └── migrations/   # Migrazioni SQL del database
└── .github/
    └── workflows/
        └── release.yml   # CI/CD: build + firma + release automatica
```

### Stack tecnico

| Layer | Tecnologia |
|---|---|
| UI | Jetpack Compose / Compose Desktop |
| Navigazione | Navigation Compose (Android) |
| State management | ViewModel + StateFlow |
| Backend | Supabase (PostgreSQL + Auth + Realtime + Storage) |
| Notifiche push | Firebase Cloud Messaging (FCM HTTP v1 API) |
| Edge Functions | Deno (TypeScript) su Supabase |
| CAPTCHA | Cloudflare Turnstile (WebView nativo Android / JavaFX WebView Desktop) |
| Build & CI | GitHub Actions |
| Firma Android | Keystore JKS + `signingConfig` |

---

## Requisiti

### Android
- Android Studio Hedgehog o superiore
- JDK 17+
- Android SDK con `compileSdk = 35`
- File `google-services.json` posizionato in `android/app/` (fornito separatamente)

### Desktop
- JDK 17+
- Gradle 9.3+
- Per la build MSI (Windows): WiX Toolset installato

---

## Setup sviluppo

### 1. Clona il repository

```bash
git clone https://github.com/NexDam/nexdam-releases.git
cd nexdam-releases
```

### 2. Configura Android

Posiziona il file `google-services.json` (scaricabile dalla Firebase Console → Progetto `nexdam-afe2d` → Impostazioni → App Android `it.nexdam.app`) in:

```
android/app/google-services.json
```

Per eseguire:
```bash
cd android
./gradlew assembleDebug
# oppure installa direttamente su device connesso:
./gradlew installDebug
```

### 3. Esegui Desktop

```bash
cd desktop
./gradlew run
```

---

## Variabili e secret

### GitHub Actions Secrets (necessari per la CI)

| Secret | Descrizione |
|---|---|
| `KEYSTORE_BASE64` | Keystore Android codificato in Base64 |
| `KEY_ALIAS` | Alias della chiave nel keystore |
| `KEY_PASSWORD` | Password della chiave |
| `STORE_PASSWORD` | Password del keystore |
| `GOOGLE_SERVICES_JSON_BASE64` | Contenuto del `google-services.json` codificato in Base64 |

### Supabase Edge Function Secrets

Impostabili con `supabase secrets set <NOME>="<valore>"`:

| Secret | Descrizione |
|---|---|
| `FCM_PROJECT_ID` | `project_id` del service account Firebase |
| `FCM_CLIENT_EMAIL` | `client_email` del service account Firebase |
| `FCM_PRIVATE_KEY` | `private_key` del service account Firebase (con `\n` letterali) |

> Il service account si genera da: Firebase Console → Progetto `nexdam-afe2d` → Impostazioni → Account di servizio → Genera nuova chiave privata

---

## Build e rilascio

Il rilascio è completamente automatizzato tramite GitHub Actions (`.github/workflows/release.yml`).

Ogni push su `main` triggera:
1. **Build Android** — `assembleRelease` + firma con keystore
2. **Build Windows** — `packageMsi`
3. **Build Linux** — `packageDeb`
4. **Creazione/aggiornamento release GitHub** — carica automaticamente i 3 artefatti sulla release `latest`

I file vengono pubblicati su:
- GitHub Release → `latest`
- Sito NexDam (collegato alla release)

### Build manuale

```bash
# Android APK firmato
cd android && ./gradlew assembleRelease

# Desktop Windows
cd desktop && ./gradlew packageMsi

# Desktop Linux
cd desktop && ./gradlew packageDeb
```

---

## Struttura del progetto

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/it/nexdam/app/
│   │   │   ├── data/           # SupabaseClient, models
│   │   │   ├── notifications/  # FCM service, DeviceTokenRegistrar, MessageNotifier
│   │   │   ├── ui/
│   │   │   │   ├── components/ # TurnstileWidget, ...
│   │   │   │   ├── navigation/ # NavGraph
│   │   │   │   ├── screens/    # Login, Register, Dashboard, Project, Profile, Blog
│   │   │   │   ├── theme/      # Colori, tipografia
│   │   │   │   └── viewmodels/ # AuthViewModel, DashboardViewModel, ProjectViewModel, ...
│   │   │   └── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── gradle/libs.versions.toml

desktop/
├── src/main/kotlin/it/nexdam/desktop/
│   ├── data/           # SupabaseClient, models
│   ├── notifications/  # AutostartManager, DesktopNotifier
│   ├── ui/
│   │   ├── components/ # TurnstileWidget
│   │   ├── screens/    # Login, MainScreen, Project, Profile, Blog panels
│   │   ├── theme/
│   │   └── viewmodels/ # AuthViewModel, AppViewModel, BlogViewModel
│   └── Main.kt         # Entry point, Tray, autostart
└── build.gradle.kts

supabase/
├── functions/
│   └── send-push-notification/
│       └── index.ts    # Edge Function: genera JWT OAuth2 → FCM HTTP v1 API
└── migrations/
    ├── 20260608000000_device_push_tokens.sql
    ├── 20260608120000_device_push_tokens_grants.sql
    └── 20260608130000_admin_rls_policies.sql
```

---

## Database & Backend

### Tabelle principali

| Tabella | Descrizione |
|---|---|
| `profiles` | Dati utente (`role`: `admin` / `client`) |
| `projects` | Progetti clienti (`client_id`, `status`, ...) |
| `project_messages` | Messaggi chat per progetto (`is_admin` per distinguere admin/cliente) |
| `project_files` | File allegati ai progetti |
| `invoices` | Fatture per progetto |
| `device_push_tokens` | Token FCM per notifiche push ad app chiusa |

### Notifiche push — flusso

```
Admin scrive messaggio (is_admin=true)
        ↓
Database Webhook (INSERT su project_messages)
        ↓
Edge Function send-push-notification (Deno)
        ↓
OAuth2 access token via service account Firebase
        ↓
FCM HTTP v1 API → notifica sul dispositivo del cliente
```

### RLS (Row Level Security)

- **Clienti**: accesso solo ai propri dati (`auth.uid() = client_id` / `user_id`)
- **Admin** (`role = 'admin'`): accesso completo a tutte le righe di tutte le tabelle operative

---

*NexDam © 2026 — tutti i diritti riservati*
