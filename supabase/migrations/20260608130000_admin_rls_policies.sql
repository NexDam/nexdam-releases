-- Aggiunge le RLS policy per il ruolo admin (role = 'admin' in profiles).
-- Senza queste, l'admin non può leggere/scrivere nessuna riga delle tabelle
-- dei clienti, perché le policy esistenti filtrano solo per client_id/auth.uid().
--
-- L'admin è identificato da: EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'admin')

-- ── projects ─────────────────────────────────────────────────────────────────
create policy "Admin reads all projects"
    on public.projects for select
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin inserts projects"
    on public.projects for insert
    with check (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin updates projects"
    on public.projects for update
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin deletes projects"
    on public.projects for delete
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

-- ── project_messages ─────────────────────────────────────────────────────────
create policy "Admin reads all messages"
    on public.project_messages for select
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin inserts messages"
    on public.project_messages for insert
    with check (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

-- ── project_files ─────────────────────────────────────────────────────────────
create policy "Admin reads all files"
    on public.project_files for select
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin inserts files"
    on public.project_files for insert
    with check (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin updates files"
    on public.project_files for update
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin deletes files"
    on public.project_files for delete
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

-- ── invoices ──────────────────────────────────────────────────────────────────
create policy "Admin reads all invoices"
    on public.invoices for select
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin inserts invoices"
    on public.invoices for insert
    with check (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin updates invoices"
    on public.invoices for update
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));

create policy "Admin deletes invoices"
    on public.invoices for delete
    using (exists (select 1 from public.profiles where id = auth.uid() and role = 'admin'));
