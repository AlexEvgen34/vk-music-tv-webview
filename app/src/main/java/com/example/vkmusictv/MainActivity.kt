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

        // ⚠️ ЖЁСТКАЯ проверка WebView ТОЛЬКО для Android TV
        if (isAndroidTv() && !isWebViewUsable()) {
            openInExternalBrowser()
            return
        }

        // На смартфоне — всегда пробуем WebView
        setContentView(R.layout.activity_main)
        initWebView()
    }

    /**
     * Определяем, Android TV это или нет
     */
    private fun isAndroidTv(): Boolean {
        return packageManager.hasSystemFeature("android.software.leanback")
    }

    /**
     * Проверка: не падает ли WebView при создании
     * (критично для DEXP / Android TV 8)
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
     * Инициализация WebView
     * + фикс ERR_CACHE_MISS
     */
    private fun initWebView() {
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            // 🔥 Фикс ERR_CACHE_MISS
            cacheMode = WebSettings.LOAD_NO_CACHE

            // Медиа без жестов (важно для TV)
            mediaPlaybackRequiresUserGesture = false

            // User-Agent для TV-версии сайтов
            userAgentString = userAgentString + " AndroidTV"
        }

        // Чистим всё перед первой загрузкой
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
                // ❗ fallback ТОЛЬКО на Android TV
                if (isAndroidTv()) {
                    openInExternalBrowser()
                }
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
            "WebView недоступен.\nОткрываем VK Music в браузере.",
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
