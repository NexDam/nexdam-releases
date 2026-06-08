package it.nexdam.desktop.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.web.WebView
import netscape.javascript.JSObject

/**
 * Site key pubblica di Cloudflare Turnstile usata per la protezione CAPTCHA
 * di Supabase Auth. È un valore pubblico (non la Secret Key), sicuro da
 * includere nel client.
 */
const val TURNSTILE_SITE_KEY = "0x4AAAAAADgtYXtPGUWVr663"

private fun turnstileHtml(siteKey: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js" async defer></script>
        <style>
            html, body {
                margin: 0;
                padding: 0;
                background: transparent;
                display: flex;
                align-items: center;
                justify-content: center;
            }
        </style>
    </head>
    <body>
        <div class="cf-turnstile"
             data-sitekey="$siteKey"
             data-theme="dark"
             data-callback="onTurnstileToken"
             data-expired-callback="onTurnstileExpired"
             data-error-callback="onTurnstileExpired"></div>
        <script>
            function onTurnstileToken(token) { javaBridge.onToken(token); }
            function onTurnstileExpired() { javaBridge.onExpired(); }
        </script>
    </body>
    </html>
""".trimIndent()

/**
 * Bridge esposto a JavaScript dentro la WebView JavaFX: i suoi metodi
 * pubblici diventano chiamabili da `javaBridge.<metodo>(...)`.
 */
class TurnstileBridge(private val onToken: (String?) -> Unit) {
    @Suppress("unused")
    fun onToken(token: String) {
        Platform.runLater { onToken(token) }
    }

    @Suppress("unused")
    fun onExpired() {
        Platform.runLater { onToken(null) }
    }
}

/**
 * Mostra il widget Cloudflare Turnstile (CAPTCHA) richiesto da Supabase Auth
 * per login e registrazione, incorporato tramite JavaFX WebView (motore
 * WebKit) — Compose Desktop non ha un WebView nativo. Una volta risolto,
 * restituisce il token da passare come `captchaToken` alla chiamata di auth.
 */
@Composable
fun TurnstileWidget(
    modifier: Modifier = Modifier,
    siteKey: String = TURNSTILE_SITE_KEY,
    onToken: (String?) -> Unit
) {
    val currentOnToken by rememberUpdatedState(onToken)
    val bridge = remember { TurnstileBridge { currentOnToken(it) } }

    SwingPanel(
        modifier = modifier.height(90.dp),
        factory = {
            val jfxPanel = JFXPanel()
            Platform.runLater {
                val webView = WebView()
                val engine = webView.engine
                engine.isJavaScriptEnabled = true
                engine.loadWorker.stateProperty().addListener { _, _, newState ->
                    if (newState == Worker.State.SUCCEEDED) {
                        val window = engine.executeScript("window") as JSObject
                        window.setMember("javaBridge", bridge)
                    }
                }
                engine.loadContent(turnstileHtml(siteKey), "text/html")
                val scene = Scene(webView)
                scene.fill = Color.TRANSPARENT
                jfxPanel.scene = scene
            }
            jfxPanel
        }
    )
}
