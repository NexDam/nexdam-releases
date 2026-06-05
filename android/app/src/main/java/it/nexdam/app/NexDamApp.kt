package it.nexdam.app

import android.app.Application
import it.nexdam.app.data.supabase

class NexDamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Supabase client is initialized as a top-level singleton in SupabaseClient.kt
        // Accessing it here ensures it's created on app start
        supabase.toString()
    }
}
