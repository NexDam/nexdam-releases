package it.nexdam.desktop.notifications

import java.io.File
import java.util.prefs.Preferences

/**
 * Registra l'app per l'avvio automatico al login dell'utente, così che resti
 * sempre attiva in background (system tray) e possa ricevere le notifiche
 * realtime anche quando non viene aperta manualmente.
 *
 * Su Windows aggiunge una voce in HKCU\...\Run, su Linux scrive un file
 * .desktop in ~/.config/autostart. Su entrambi i sistemi l'operazione viene
 * eseguita una sola volta (tracciata in Preferences) e fallisce in silenzio
 * se il pacchetto non è stato installato tramite installer nativo (es. quando
 * si esegue da IDE/gradle, dove non esiste un eseguibile stabile da puntare).
 */
object AutostartManager {

    private const val APP_NAME = "NexDamClientPortal"
    private val prefs = Preferences.userRoot().node("it/nexdam/desktop")

    fun ensureEnabled() {
        if (prefs.getBoolean("autostart_registered", false)) return

        val launcher = nativeLauncherPath() ?: return

        val ok = when {
            isWindows() -> registerWindows(launcher)
            isLinux() -> registerLinux(launcher)
            else -> false
        }

        if (ok) prefs.putBoolean("autostart_registered", true)
    }

    private fun isWindows() = System.getProperty("os.name").contains("Windows", ignoreCase = true)
    private fun isLinux() = System.getProperty("os.name").contains("Linux", ignoreCase = true)

    /**
     * Percorso dell'eseguibile nativo generato dall'installer (MSI/DEB).
     * Quando l'app gira da IDE/gradle questo non esiste: in tal caso non
     * registriamo nulla, per evitare di puntare a un path temporaneo.
     */
    private fun nativeLauncherPath(): String? {
        val command = ProcessHandle.current().info().command().orElse(null) ?: return null
        val file = File(command)
        if (!file.exists()) return null
        val name = file.name.lowercase()
        if (name == "java" || name == "java.exe" || name.startsWith("javaw")) return null
        return file.absolutePath
    }

    private fun registerWindows(launcherPath: String): Boolean = try {
        val process = ProcessBuilder(
            "reg", "add",
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
            "/v", APP_NAME,
            "/t", "REG_SZ",
            "/d", "\"$launcherPath\" --autostart",
            "/f"
        ).redirectErrorStream(true).start()
        process.waitFor() == 0
    } catch (_: Exception) {
        false
    }

    private fun registerLinux(launcherPath: String): Boolean = try {
        val autostartDir = File(System.getProperty("user.home"), ".config/autostart")
        if (!autostartDir.exists()) autostartDir.mkdirs()
        val desktopFile = File(autostartDir, "nexdam-client-portal.desktop")
        desktopFile.writeText(
            """
            [Desktop Entry]
            Type=Application
            Name=NexDam Client Portal
            Exec=$launcherPath --autostart
            X-GNOME-Autostart-enabled=true
            Terminal=false
            """.trimIndent()
        )
        true
    } catch (_: Exception) {
        false
    }
}
