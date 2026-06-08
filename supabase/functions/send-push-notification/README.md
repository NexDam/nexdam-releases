# Setup notifiche push (Firebase Cloud Messaging)

Questi passaggi vanno fatti una volta sola, al di fuori del codice.

## 1. Crea il progetto Firebase
1. Vai su https://console.firebase.google.com → "Aggiungi progetto" → crea "NexDam".
2. Aggiungi un'app Android con applicationId `it.nexdam.app`.
3. Scarica `google-services.json` e mettilo in `android/app/google-services.json`
   (necessario per buildare in locale: senza questo file `assembleRelease`/`assembleDebug`
   falliscono con "File google-services.json is missing").
4. Per la CI ([release.yml](../../../.github/workflows/release.yml)), che builda l'APK
   con `assembleRelease`, aggiungi un secret GitHub `GOOGLE_SERVICES_JSON_BASE64` con il
   contenuto del file codificato in base64 (stesso pattern del keystore già in uso):
   ```
   # PowerShell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) | Set-Clipboard
   ```
   Incollalo come valore del secret su GitHub → Settings → Secrets and variables → Actions.
   **Fai questo passaggio prima di pushare**, altrimenti la build Android in CI fallisce.

## 2. Crea una Service Account per l'invio server-side
1. Console Firebase → ⚙️ Impostazioni progetto → "Account di servizio".
2. "Genera nuova chiave privata" → scarica il JSON.
3. Da quel JSON ti servono 3 valori: `project_id`, `client_email`, `private_key`.

## 3. Applica la migration del database
```
supabase db push
```
Crea la tabella `device_push_tokens` (con RLS) descritta in
`supabase/migrations/20260608000000_device_push_tokens.sql`.

## 4. Configura i secrets della Edge Function
```
supabase secrets set FCM_PROJECT_ID="il-project-id-firebase"
supabase secrets set FCM_CLIENT_EMAIL="xxx@yyy.iam.gserviceaccount.com"
supabase secrets set FCM_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n"
```
`SUPABASE_URL` e `SUPABASE_SERVICE_ROLE_KEY` sono già disponibili automaticamente
nelle Edge Functions.

## 5. Deploy della funzione
```
supabase functions deploy send-push-notification --no-verify-jwt
```
`--no-verify-jwt` è necessario perché il chiamante è il Database Webhook di Supabase,
non un utente autenticato.

## 6. Crea il Database Webhook
Dashboard Supabase → Database → Webhooks → "Create a new hook":
- Table: `project_messages`
- Events: `Insert`
- Type: `Supabase Edge Functions`
- Edge Function: `send-push-notification`

Da questo momento, ogni nuovo messaggio con `is_admin = true` farà partire una push
FCM verso tutti i dispositivi Android registrati del cliente del progetto, recapitata
anche ad app completamente chiusa.
