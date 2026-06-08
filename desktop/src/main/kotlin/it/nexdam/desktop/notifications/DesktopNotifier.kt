package it.nexdam.desktop.notifications

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage

/**
 * Mostra notifiche di sistema (system tray) su Windows e Linux quando
 * arriva un nuovo messaggio dal team NexDam su uno dei progetti dell'utente.
 */
object DesktopNotifier {

    private var trayIcon: TrayIcon? = null

    /** Immagine del logo usata sia per le notifiche tray sia per l'icona dell'app in tray. */
    fun appIconImage(): BufferedImage = buildIconImage()

    private fun buildIconImage(): BufferedImage {
        val size = 32
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = Color(0x4F, 0x6E, 0xF7) // colore primario del brand NexDam
        g.fillOval(0, 0, size, size)
        g.color = Color.WHITE
        g.font = g.font.deriveFont(16f)
        val text = "N"
        val fm = g.fontMetrics
        val tx = (size - fm.stringWidth(text)) / 2
        val ty = (size - fm.height) / 2 + fm.ascent
        g.drawString(text, tx, ty)
        g.dispose()
        return image
    }

    @Synchronized
    private fun ensureTrayIcon(): TrayIcon? {
        if (trayIcon != null) return trayIcon
        if (!SystemTray.isSupported()) return null
        return try {
            val image = buildIconImage()
            val icon = TrayIcon(image, "NexDam Client Portal")
            icon.isImageAutoSize = true
            SystemTray.getSystemTray().add(icon)
            trayIcon = icon
            icon
        } catch (_: Exception) {
            null
        }
    }

    fun notifyNewMessage(projectTitle: String, body: String) {
        val icon = ensureTrayIcon() ?: return
        try {
            icon.displayMessage("Nuovo messaggio · $projectTitle", body, TrayIcon.MessageType.INFO)
        } catch (_: Exception) {
            // ignora se il sistema operativo non supporta le notifiche tray in questo momento
        }
    }
}
