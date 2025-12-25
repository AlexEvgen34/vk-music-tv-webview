package com.example.vkmusictv

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        // Chromium error code
        private const val ERR_CACHE_MISS = -10
    }

    // защита от бесконечного reload
    private var cacheMissReloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Первый экран — выбор режима
        showChoiceScreen()
    }

    /**
     * Экран выбора: WebView / Браузер
     */
    private fun showChoiceScreen() {
        setContentView(R.layout.activity_choice)

        val btnWebView = findViewById<Button>(R.id.btn_webview)
        val btnBrowser = findViewById<Button>(R.id.btn_browser)

        // фокус сразу на первой кнопке (важно для TV)
        btnWebView.requestFocus()

        btnWebView.setOnClickListener {
            startWebViewMode()
        }

        btnBrowser.setOnClickListener {
            openInExternalBrowser()
        }
    }

    /**
     * Запуск режима WebView
     */
    private fun startWebViewMode() {
        // На Android TV проверяем WebView заранее
        if (isAndroidTv() && !isWebViewUsable()) {
            Toast.makeText(
                this,
                "WebView недоступен на этом TV.\nОткрываем браузер.",
                Toast.LENGTH_LONG
            ).show()
            openInExternalBrowser()
            return
        }

        setContentView(R.layout.activity_main)
        initWebView()
    }

    /**
     * Проверка: Android TV или нет
     */
    private fun isAndroidTv(): Boolean {
        return packageManager.hasSystemFeature("android.software.leanback")
    }

    /**
     * Проверка: не падает ли WebView при создании
     * (актуально для DEXP / Android TV 8)
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
     * + корректная обработка ERR_CACHE_MISS
     */
    private fun initWebView() {
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true

            // меньше проблем с кэшем на TV и при первом запуске
            cacheMode = WebSettings.LOAD_NO_CACHE

            // воспроизведение без жестов (важно для TV)
            mediaPlaybackRequiresUserGesture = false

            // User-Agent с пометкой TV
            userAgentString = userAgentString + " AndroidTV"
        }

        // чистый старт
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
                // обрабатываем только основную страницу
                if (!request.isForMainFrame) return

                // ERR_CACHE_MISS — нормальная ситуация, перезагружаем один раз
                if (error.errorCode == ERR_CACHE_MISS && !cacheMissReloaded) {
                    cacheMissReloaded = true
                    view.post { view.reload() }
                    return
                }

                // если реально сломалось — fallback только на TV
                if (isAndroidTv()) {
                    openInExternalBrowser()
                }
            }
        }

        webView.loadUrl("https://m.vk.com/audio")
    }

    /**
     * Открытие VK Music во внешнем браузере
     */
    private fun openInExternalBrowser() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://m.vk.com/audio")
        )
        startActivity(intent)
        finish()
    }
}
