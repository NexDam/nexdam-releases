-- Tabella che associa ad ogni utente i token FCM dei suoi dispositivi,
-- usata dall'Edge Function "send-push-notification" per inviare le push
-- anche quando l'app è completamente chiusa.
create table if not exists public.device_push_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    token text not null,
    platform text not null default 'android',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (user_id, token)
);

create index if not exists device_push_tokens_user_id_idx on public.device_push_tokens (user_id);

alter table public.device_push_tokens enable row level security;

-- Ogni utente può gestire solo i propri token.
create policy "Users can view their own device tokens"
    on public.device_push_tokens for select
    using (auth.uid() = user_id);

create policy "Users can insert their own device tokens"
    on public.device_push_tokens for insert
    with check (auth.uid() = user_id);

create policy "Users can update their own device tokens"
    on public.device_push_tokens for update
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);

create policy "Users can delete their own device tokens"
    on public.device_push_tokens for delete
    using (auth.uid() = user_id);

-- Mantiene aggiornato updated_at ad ogni upsert del token.
create or replace function public.touch_device_push_tokens_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

drop trigger if exists trg_device_push_tokens_updated_at on public.device_push_tokens;
create trigger trg_device_push_tokens_updated_at
    before update on public.device_push_tokens
    for each row execute function public.touch_device_push_tokens_updated_at();
