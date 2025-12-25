package com.example.vkmusictv

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, можно ли вообще использовать WebView
        if (!isWebViewUsable()) {
            openInExternalBrowser()
            return
        }

        setContentView(R.layout.activity_main)
        initWebView()
    }

    /**
     * Проверка: не падает ли WebView при создании (DEXP / RTM8)
     */
    private fun isWebViewUsable(): Boolean {
        return try {
            val test = WebView(this)
            test.destroy()
            true
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Инициализация WebView с фиксом ERR_CACHE_MISS
     */
    private fun initWebView() {
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            // 🔥 КРИТИЧНО: фиксим ERR_CACHE_MISS
            cacheMode = WebSettings.LOAD_NO_CACHE
            setAppCacheEnabled(false)

            // Медиа без жестов (важно для TV)
            mediaPlaybackRequiresUserGesture = false

            // User-Agent для TV
            userAgentString = userAgentString + " AndroidTV"
        }

        // Чистим всё перед загрузкой
        webView.clearCache(true)
        webView.clearHistory()

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                // Если WebView снова сломался — уходим в браузер
                openInExternalBrowser()
            }
        }

        webView.loadUrl("https://m.vk.com/audio")
    }

    /**
     * Fallback: открываем VK Music во внешнем браузере
     */
    private fun openInExternalBrowser() {
        Toast.makeText(
            this,
            "WebView недоступен. Открываем VK Music в браузере.",
            Toast.LENGTH_LONG
        ).show()

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://m.vk.com/audio")
        )
        startActivity(intent)
        finish()
    }
}
