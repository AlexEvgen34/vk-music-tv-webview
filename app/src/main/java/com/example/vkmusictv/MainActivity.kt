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
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        // Chromium error code
        private const val ERR_CACHE_MISS = -10
    }

    private var cacheMissReloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Экран выбора
        showChoiceScreen()
    }

    /**
     * Экран выбора WebView / Браузер
     */
    private fun showChoiceScreen() {
        setContentView(R.layout.activity_choice)

        val btnWebView = findViewById<Button>(R.id.btn_webview)
        val btnBrowser = findViewById<Button>(R.id.btn_browser)

        btnWebView.requestFocus()

        btnWebView.setOnClickListener {
            startWebViewMode()
        }

        btnBrowser.setOnClickListener {
            openInExternalBrowser()
        }
    }

    /**
     * Запуск WebView
     */
    private fun startWebViewMode() {
        if (isAndroidTv() && !isWebViewUsable()) {
            Toast.makeText(
                this,
                "WebView недоступен на этом TV",
                Toast.LENGTH_LONG
            ).show()
            openInExternalBrowser()
            return
        }

        setContentView(R.layout.activity_main)
        initWebView()
    }

    private fun isAndroidTv(): Boolean {
        return packageManager.hasSystemFeature("android.software.leanback")
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

    /**
     * WebView с обработкой ERR_CACHE_MISS
     */
    private fun initWebView() {
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = false
            userAgentString += " AndroidTV"
        }

        webView.clearCache(true)
        webView.clearHistory()

        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (!request.isForMainFrame) return

                // ERR_CACHE_MISS — перезагружаем один раз
                if (error.errorCode == ERR_CACHE_MISS && !cacheMissReloaded) {
                    cacheMissReloaded = true
                    view.post { view.reload() }
                    return
                }

                // fallback только на TV
                if (isAndroidTv()) {
                    openInExternalBrowser()
                }
            }
        }

        webView.loadUrl("https://m.vk.com/audio")
    }

    /**
     * Внешний браузер
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
