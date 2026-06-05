package it.nexdam.app.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

const val SUPABASE_URL = "https://ggzuryrpwygedwpnzrjj.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdnenVyeXJwd3lnZWR3cG56cmpqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAxNDQ2NjgsImV4cCI6MjA5NTcyMDY2OH0.Y-SvKM67TX6UbyFgLOSfZBlqTdy0WzsT1xNWcCHnX28"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
    install(Realtime)
}
