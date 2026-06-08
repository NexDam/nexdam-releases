-- La migrazione che ha creato `device_push_tokens` impostava le RLS policy ma
-- dimenticava i GRANT a livello di tabella per il ruolo `authenticated`.
-- Senza questi, Postgres rifiuta l'accesso con "permission denied for table
-- device_push_tokens" ancora prima di valutare le RLS policy, impedendo al
-- client di registrare il proprio token FCM (e quindi di ricevere le push
-- quando l'app è completamente chiusa).
grant select, insert, update, delete on public.device_push_tokens to authenticated;
