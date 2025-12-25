package com.example.vkmusictv

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка WebView (DEXP / Android TV 8)
        if (!isWebViewUsable()) {
            openInExternalBrowser()
            return
        }

        setContentView(R.layout.activity_main)
        initWebView()
    }

    private fun isWebViewUsable(): Boolean {
        return try {
            val test = WebView(this)
            test.destroy()
            true
        } catch (t: Throwable) {
            false
        }
    }

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

    private fun initWebView() {
    val webView = findViewById<WebView>(R.id.webview)

    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true

        // 🔥 КРИТИЧНО для ERR_CACHE_MISS
        cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        setAppCacheEnabled(false)

        // Разрешаем мультимедиа
        mediaPlaybackRequiresUserGesture = false

        // TV User-Agent
        userAgentString = userAgentString + " AndroidTV"
    }

    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: android.webkit.WebResourceRequest?
        ): Boolean {
            return false
        }

        override fun onReceivedError(
            view: WebView?,
            request: android.webkit.WebResourceRequest?,
            error: android.webkit.WebResourceError?
        ) {
            // fallback если снова ошибка
            openInExternalBrowser()
        }
    }

    // ❗ грузим ЧИСТЫЙ URL без истории
    webView.clearCache(true)
    webView.clearHistory()

    webView.loadUrl("https://m.vk.com/audio")
}
