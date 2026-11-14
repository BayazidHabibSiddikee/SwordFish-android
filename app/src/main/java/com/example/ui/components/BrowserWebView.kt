package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.blocker.AdBlockEngine
import com.example.data.blocker.AppBlockerEngine
import com.example.data.blocker.SwordfishAdBlockBridge
import com.example.data.blocker.SwordfishAdBlockWebViewClient
import com.example.data.local.PasswordAutofillEngine
import com.example.data.model.BrowserTab
import com.example.data.reader.ReaderModeEngine
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.WebCommand
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    tab: BrowserTab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // JavaScript Bridge instance for bidirectional communication
    val adBlockBridge = remember(tab.id) {
        SwordfishAdBlockBridge(
            onAdRemoved = { selector, count ->
                viewModel.onJsAdElementBlocked(selector, count)
            },
            onVideoAdSkipped = { durationSec ->
                viewModel.onVideoAdSkipped(durationSec)
            },
            onPopupBlocked = { url ->
                viewModel.onPopupBlocked(url)
            },
            onAntiAdblockNeutralized = { details ->
                viewModel.onAntiAdblockNeutralized(details)
            },
            onLoginFormDetected = { domain, userSel, passSel ->
                viewModel.onLoginFormDetected(domain, userSel, passSel)
            },
            onPasswordFormSubmitted = { domain, user, pass ->
                viewModel.onPasswordFormSubmitted(domain, user, pass)
            },
            onReaderContentExtracted = { jsonStr ->
                viewModel.onReaderContentExtracted(jsonStr)
            },
            onReaderModeUnavailable = { reason ->
                viewModel.onReaderModeUnavailable(reason)
            },
            onReaderAvailabilityChanged = { isAvailable ->
                viewModel.onReaderAvailabilityChanged(isAvailable)
            },
            onPageHtmlExtracted = { url, html ->
                viewModel.onPageHtmlExtracted(url, html)
            }
        )
    }

    val adBlockWebViewClient = remember(tab.id) {
        SwordfishAdBlockWebViewClient(
            blocklistService = viewModel.blocklistService,
            getFilterSettings = { viewModel.filterSettings.value },
            isShieldEnabled = { viewModel.shieldStats.value.isShieldEnabled },
            onBlocked = { uri, isScript, isTracker ->
                mainHandler.post {
                    viewModel.onAdOrTrackerBlocked(isTracker)
                }
            },
            onUrlBlockedByAppBlocker = { url ->
                mainHandler.post {
                    viewModel.loadUrl(url)
                }
            },
            checkIsUrlDistractionBlocked = { url ->
                val enabledBlockedDomains = viewModel.blockedSites.value
                    .filter { it.isEnabled }
                    .map { it.domain }
                val (isBlocked, _) = AppBlockerEngine.checkBlocked(
                    url,
                    enabledBlockedDomains,
                    isFocusModeActive = viewModel.isFocusModeActive.value,
                    filterSettings = viewModel.filterSettings.value
                )
                isBlocked
            },
            onPageStartedCallback = { view, url, favicon ->
                url?.let {
                    viewModel.onPageStarted(it)

                    // Early JS Bridge script injection into DOM
                    val filters = viewModel.filterSettings.value
                    if (viewModel.shieldStats.value.isShieldEnabled) {
                        if (filters.adBlock.cosmeticDomHiding) {
                            view?.evaluateJavascript(AdBlockEngine.PROPER_JS_ADBLOCKER, null)
                        }
                        if (filters.adBlock.youtubeAdBlocking && AdBlockEngine.isYouTubeUrl(it)) {
                            view?.evaluateJavascript(AdBlockEngine.YOUTUBE_ADBLOCKER_JS, null)
                        }
                    }
                }
            },
            onReceivedErrorCallback = { view, request, error ->
                if (request?.isForMainFrame == true) {
                    val failedUrl = request.url?.toString() ?: ""
                    viewModel.handleOfflineNavigation(failedUrl)
                }
            },
            onPageFinishedCallback = { view, url ->
                val safeUrl = url ?: ""
                val filters = viewModel.filterSettings.value

                // Granular JS Bridge & YouTube AdBlocker Injection based on active filter toggles
                if (viewModel.shieldStats.value.isShieldEnabled) {
                    if (filters.adBlock.cosmeticDomHiding) {
                        view?.evaluateJavascript(AdBlockEngine.PROPER_JS_ADBLOCKER, null)
                    }

                    if (filters.adBlock.youtubeAdBlocking && AdBlockEngine.isYouTubeUrl(safeUrl)) {
                        view?.evaluateJavascript(AdBlockEngine.YOUTUBE_ADBLOCKER_JS, null)
                    }
                }

                // Inject secure login form detector and autofill bridge
                view?.evaluateJavascript(PasswordAutofillEngine.LOGIN_DETECTOR_JS, null)

                // Check Reader Mode availability if page is eligible
                if (ReaderModeEngine.isReaderEligibleUrl(safeUrl)) {
                    view?.evaluateJavascript(ReaderModeEngine.READER_AVAILABILITY_CHECK_JS, null)
                }

                // Extract rendered DOM HTML for offline Room cache if not incognito
                if (!tab.isIncognito && safeUrl.isNotBlank() && !safeUrl.startsWith("about:") && !safeUrl.startsWith("data:")) {
                    view?.evaluateJavascript(
                        "if (window.SwordfishBridge && window.SwordfishBridge.reportPageHtml) { window.SwordfishBridge.reportPageHtml(window.location.href, document.documentElement.outerHTML); }",
                        null
                    )
                }

                viewModel.onPageFinished(
                    title = view?.title ?: "",
                    url = safeUrl,
                    canGoBack = view?.canGoBack() == true,
                    canGoForward = view?.canGoForward() == true
                )
            }
        )
    }

    val adBlockChromeClient = remember(tab.id) {
        object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                viewModel.onProgressChanged(newProgress)

                // Inject during page construction so dynamic ad scripts cannot mount
                if (newProgress in 35..80 && viewModel.shieldStats.value.isShieldEnabled) {
                    val currentUrl = view?.url ?: ""
                    view?.evaluateJavascript(AdBlockEngine.PROPER_JS_ADBLOCKER, null)
                    if (AdBlockEngine.isYouTubeUrl(currentUrl)) {
                        view?.evaluateJavascript(AdBlockEngine.YOUTUBE_ADBLOCKER_JS, null)
                    }
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrBlank()) {
                    viewModel.onPageFinished(
                        title = title,
                        url = view?.url ?: tab.url,
                        canGoBack = view?.canGoBack() == true,
                        canGoForward = view?.canGoForward() == true
                    )
                }
            }
        }
    }

    val webView = remember(tab.id) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = false
                allowContentAccess = false
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                safeBrowsingEnabled = true
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
                cacheMode = if (tab.isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                userAgentString = if (tab.isDesktopMode) {
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                } else {
                    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                }
            }

            // Expose the JavaScript Bridge Layer to in-page scripts
            addJavascriptInterface(adBlockBridge, "SwordfishBridge")

            // Bind clients synchronously before loading any URL to avoid external intent redirects
            this.webViewClient = adBlockWebViewClient
            this.webChromeClient = adBlockChromeClient

            // Immediately load URL if present on initial tab creation
            if (tab.url.isNotBlank()) {
                loadUrl(tab.url)
            }
        }
    }

    // Reactive URL loader when tab target changes (e.g. from omnibox, quick links, or suggestions)
    LaunchedEffect(tab.url) {
        val targetUrl = tab.url.trim()
        val currentUrl = webView.url?.trim()
        if (targetUrl.isNotBlank() && currentUrl != targetUrl && currentUrl?.trimEnd('/') != targetUrl.trimEnd('/')) {
            webView.loadUrl(targetUrl)
        }
    }

    // Update Desktop Mode User Agent
    LaunchedEffect(tab.isDesktopMode) {
        webView.settings.userAgentString = if (tab.isDesktopMode) {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        } else {
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }
    }

    // Listen to WebCommands for this active tab
    LaunchedEffect(tab.id) {
        viewModel.webCommands.collectLatest { command ->
            when (command) {
                is WebCommand.GoBack -> {
                    if (webView.canGoBack()) webView.goBack()
                }
                is WebCommand.GoForward -> {
                    if (webView.canGoForward()) webView.goForward()
                }
                is WebCommand.Reload -> {
                    webView.reload()
                }
                is WebCommand.LoadUrl -> {
                    if (webView.url != command.url) {
                        webView.loadUrl(command.url)
                    }
                }
                is WebCommand.LoadCachedHtml -> {
                    webView.loadDataWithBaseURL(command.baseUrl, command.html, "text/html", "UTF-8", null)
                }
                is WebCommand.EvaluateJs -> {
                    webView.evaluateJavascript(command.script, null)
                }
            }
        }
    }

    DisposableEffect(tab.id) {
        onDispose {
            webView.removeJavascriptInterface("SwordfishBridge")
            webView.stopLoading()
        }
    }

    AndroidView(
        factory = { webView },
        update = { view ->
            // Bulletproof sync in case the view was attached after tab state updated
            if (tab.url.isNotBlank() && view.url == null) {
                view.loadUrl(tab.url)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
