package it.nexdam.desktop.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://ggzuryrpwygedwpnzrjj.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdnenVyeXJwd3lnZWR3cG56cmpqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAxNDQ2NjgsImV4cCI6MjA5NTcyMDY2OH0.Y-SvKM67TX6UbyFgLOSfZBlqTdy0WzsT1xNWcCHnX28"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}
