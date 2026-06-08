package it.nexdam.app.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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
            function onTurnstileToken(token) { NexDamCaptcha.onToken(token); }
            function onTurnstileExpired() { NexDamCaptcha.onExpired(); }
        </script>
    </body>
    </html>
""".trimIndent()

/**
 * Mostra il widget Cloudflare Turnstile (CAPTCHA) richiesto da Supabase Auth
 * per login e registrazione. Una volta risolto, restituisce il token da
 * passare come `captchaToken` alla chiamata di autenticazione.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TurnstileWidget(
    modifier: Modifier = Modifier,
    siteKey: String = TURNSTILE_SITE_KEY,
    onToken: (String?) -> Unit
) {
    val currentOnToken by rememberUpdatedState(onToken)

    AndroidView(
        modifier = modifier.height(80.dp),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                setBackgroundColor(Color.TRANSPARENT)
                webViewClient = WebViewClient()
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onToken(token: String) {
                            post { currentOnToken(token) }
                        }

                        @JavascriptInterface
                        fun onExpired() {
                            post { currentOnToken(null) }
                        }
                    },
                    "NexDamCaptcha"
                )
                loadDataWithBaseURL(
                    "https://challenges.cloudflare.com",
                    turnstileHtml(siteKey),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    )
}
