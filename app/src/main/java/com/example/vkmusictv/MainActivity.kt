package com.example.vkmusictv

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var trackTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        trackTitle = findViewById(R.id.trackTitle)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString += " AndroidTV"
        }

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onTrackChanged(title: String) {
                runOnUiThread { trackTitle.text = title }
            }
        }, "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                injectController()
            }
        }

        webView.loadUrl("https://m.vk.com/audio")
    }

    private fun injectController() {
        webView.evaluateJavascript(JS_CONTROLLER, null)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_CENTER -> js("window.VKTV.click('play')")
                KeyEvent.KEYCODE_DPAD_LEFT -> js("window.VKTV.click('prev')")
                KeyEvent.KEYCODE_DPAD_RIGHT -> js("window.VKTV.click('next')")
                else -> return super.dispatchKeyEvent(event)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun js(code: String) {
        webView.evaluateJavascript(code, null)
    }

    companion object {
        const val JS_CONTROLLER = """
(function() {
  if (window.VKTV) return;

  function detect() {
    if (document.querySelector('.audio_page_player_play')) return 'audio_page';
    if (document.querySelector('.AudioPlayer__play')) return 'react';
    if (document.querySelector('button[aria-label="Play"]')) return 'aria';
    return 'unknown';
  }

  const MAP = {
    audio_page: {
      play:['.audio_page_player_play'],
      next:['.audio_page_player_next'],
      prev:['.audio_page_player_prev'],
      title:['.audio_page_player_title']
    },
    react: {
      play:['.AudioPlayer__play'],
      next:['.AudioPlayer__next'],
      prev:['.AudioPlayer__prev'],
      title:['.AudioPlayer__title']
    },
    aria: {
      play:['button[aria-label="Play"]','button[aria-label="Pause"]'],
      next:['button[aria-label="Next"]'],
      prev:['button[aria-label="Previous"]'],
      title:['[aria-live="polite"]']
    },
    unknown: {
      play:['.AudioPlayer__play','.audio_page_player_play'],
      next:['.AudioPlayer__next','.audio_page_player_next'],
      prev:['.AudioPlayer__prev','.audio_page_player_prev'],
      title:['.AudioPlayer__title','.audio_page_player_title']
    }
  };

  window.VKTV = {
    v: detect(),
    click(a){
      let s = MAP[this.v][a] || [];
      for (let i of s) {
        let e = document.querySelector(i);
        if (e) { e.click(); return; }
      }
    },
    title(){
      let s = MAP[this.v].title || [];
      for (let i of s) {
        let e = document.querySelector(i);
        if (e && e.innerText) {
          Android.onTrackChanged(e.innerText);
          return;
        }
      }
    }
  };

  new MutationObserver(() => {
    window.VKTV.v = detect();
    window.VKTV.title();
  }).observe(document.body, { subtree:true, childList:true });
})();
"""
    }
}
