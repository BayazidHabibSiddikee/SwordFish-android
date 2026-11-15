package com.example.data.blocker

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.FilterSettings
import com.example.data.service.BlocklistService
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * A specialized WebViewClient implementation that intercepts and blocks known ad-related
 * JavaScript sources, ad networks, tracking beacons, and telemetry network requests.
 */
open class SwordfishAdBlockWebViewClient(
    private val blocklistService: BlocklistService? = null,
    private val getFilterSettings: () -> FilterSettings? = { null },
    private val isShieldEnabled: () -> Boolean = { true },
    private val onBlocked: (uri: Uri, isScript: Boolean, isTracker: Boolean) -> Unit = { _, _, _ -> },
    private val onUrlBlockedByAppBlocker: ((url: String) -> Unit)? = null,
    private val checkIsUrlDistractionBlocked: ((url: String) -> Boolean)? = null,
    private val onPageStartedCallback: ((view: WebView?, url: String?, favicon: Bitmap?) -> Unit)? = null,
    private val onPageFinishedCallback: ((view: WebView?, url: String?) -> Unit)? = null,
    private val onReceivedErrorCallback: ((view: WebView?, request: WebResourceRequest?, error: WebResourceError?) -> Unit)? = null
) : WebViewClient() {

    companion object {
        // Known ad-related and tracking JavaScript source names, libraries, and path signatures
        val AD_JS_SOURCE_PATTERNS = listOf(
            "adsbygoogle.js",
            "show_ads.js",
            "ads.js",
            "ads.min.js",
            "ad.js",
            "adframe.js",
            "gpt.js",
            "prebid.js",
            "prebid-ads.js",
            "adroll.js",
            "criteo.js",
            "outbrain.js",
            "taboola.js",
            "popads.js",
            "popunder.js",
            "infolinks_main.js",
            "carbon.js",
            "fbevents.js",
            "fb-pixel.js",
            "analytics.js",
            "gtag/js",
            "gtm.js",
            "hotjar.js",
            "clarity.js",
            "segment.js",
            "mixpanel.js",
            "statcounter.js",
            "quant.js",
            "scorecardresearch.js",
            "amazon-adsystem.com",
            "/api/stats/ads",
            "show_ads_impl.js",
            "advertisement.js",
            "advertising.js",
            "telemetry.js",
            "fingerprint.js",
            "fp.min.js",
            "tracking.js"
        )

        /**
         * Benign JavaScript stub returned when an ad-related script is blocked.
         * Defines empty window globals (adsbygoogle, gtag, ga, fbq) to prevent host pages
         * from crashing with Uncaught ReferenceErrors or broken execution.
         */
        const val BENIGN_AD_STUB_JS = """/* [Swordfish Shield] Ad JavaScript Blocked */
(function() {
    window.adsbygoogle = window.adsbygoogle || [];
    window.adsbygoogle.loaded = true;
    window.ga = window.ga || function() {};
    window.gtag = window.gtag || function() {};
    window.fbq = window.fbq || function() {};
    window._fbq = window._fbq || function() {};
    window.dataLayer = window.dataLayer || [];
})();"""

        /**
         * Evaluates whether a given URI is a known ad-related JavaScript source.
         */
        fun isAdRelatedJavaScriptSource(uri: Uri): Boolean {
            val urlLower = uri.toString().lowercase(Locale.ROOT)
            val pathLower = (uri.path ?: "").lowercase(Locale.ROOT)
            val hostLower = (uri.host ?: "").lowercase(Locale.ROOT)

            val isScriptExtensionOrPath = pathLower.endsWith(".js") ||
                    urlLower.contains(".js?") ||
                    urlLower.contains("/js/") ||
                    pathLower.contains("/script") ||
                    pathLower.contains("javascript")

            for (pattern in AD_JS_SOURCE_PATTERNS) {
                if (urlLower.contains(pattern) || pathLower.contains(pattern) || hostLower.contains(pattern)) {
                    return true
                }
            }

            // Path indicators for ad scripts
            if (isScriptExtensionOrPath) {
                if (pathLower.contains("/ad/") ||
                    pathLower.contains("/ads/") ||
                    pathLower.contains("/banner/") ||
                    pathLower.contains("/adserver/") ||
                    pathLower.contains("sponsor") ||
                    pathLower.contains("tracker") ||
                    pathLower.contains("telemetry") ||
                    pathLower.contains("pixel")
                ) {
                    return true
                }
            }

            return false
        }

        /**
         * Creates an empty executable JavaScript response with HTTP 200 and UTF-8 encoding.
         */
        fun createEmptyScriptResponse(): WebResourceResponse {
            val bytes = BENIGN_AD_STUB_JS.toByteArray(StandardCharsets.UTF_8)
            val response = WebResourceResponse(
                "application/javascript",
                "UTF-8",
                ByteArrayInputStream(bytes)
            )
            response.setStatusCodeAndReasonPhrase(200, "OK")
            response.responseHeaders = mapOf(
                "Cache-Control" to "no-cache, no-store, must-revalidate",
                "Content-Type" to "application/javascript; charset=UTF-8",
                "X-Shield-Action" to "Ad-Script-Blocked"
            )
            return response
        }

        /**
         * Creates an empty generic network response (e.g. for ad pixels, images, iframes, telemetry).
         */
        fun createEmptyNetworkResponse(): WebResourceResponse {
            val response = WebResourceResponse(
                "text/plain",
                "UTF-8",
                ByteArrayInputStream(ByteArray(0))
            )
            response.setStatusCodeAndReasonPhrase(200, "OK")
            response.responseHeaders = mapOf(
                "Cache-Control" to "no-cache, no-store, must-revalidate",
                "Content-Type" to "text/plain; charset=UTF-8",
                "X-Shield-Action" to "Network-Ad-Blocked"
            )
            return response
        }
    }

    /**
     * Intercepts network requests to identify and block known ad-related JavaScript sources
     * and network requests. Subresources (scripts, images, trackers) are filtered, while
     * main frame document navigation is never intercepted.
     */
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        // Main frame page navigation must NEVER be intercepted by ad/tracker subresource blockers
        if (request == null || request.isForMainFrame) {
            return super.shouldInterceptRequest(view, request)
        }
        val uri = request.url ?: return super.shouldInterceptRequest(view, request)

        // Never intercept core media chunks or streaming video sources
        val host = uri.host?.lowercase(Locale.ROOT) ?: ""
        if (host.endsWith("googlevideo.com") || host.endsWith("ytimg.com")) {
            return super.shouldInterceptRequest(view, request)
        }

        return handleRequestInterception(uri) ?: super.shouldInterceptRequest(view, request)
    }

    /**
     * Overload for legacy or direct URL interceptions.
     */
    @Deprecated("Deprecated in Java", ReplaceWith("shouldInterceptRequest(view, request)"))
    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        if (url.isNullOrBlank()) return super.shouldInterceptRequest(view, url)
        val uri = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return super.shouldInterceptRequest(view, url)
        }
        return handleRequestInterception(uri) ?: super.shouldInterceptRequest(view, url)
    }

    private fun handleRequestInterception(uri: Uri): WebResourceResponse? {
        if (!isShieldEnabled()) {
            return null
        }

        val filters = getFilterSettings()
        if (filters != null && !filters.isMasterShieldEnabled) {
            return null
        }

        // 0. Security Systems: Block malicious / phishing subresources
        if (filters?.security?.maliciousSiteShield == true) {
            val (isMalicious, _) = com.example.data.security.SecurityEngine.checkMalicious(uri.toString())
            if (isMalicious) {
                onBlocked(uri, false, true)
                return createEmptyNetworkResponse()
            }
        }

        // 1. Intercept Known Ad-Related JavaScript Sources
        if (isAdRelatedJavaScriptSource(uri)) {
            val isTracker = uri.toString().contains("analytics") ||
                    uri.toString().contains("tracker") ||
                    uri.toString().contains("telemetry") ||
                    uri.toString().contains("gtag") ||
                    uri.toString().contains("fbevents") ||
                    uri.toString().contains("pixel")

            onBlocked(uri, true, isTracker)
            return createEmptyScriptResponse()
        }

        // 2. Intercept via managed BlocklistService
        if (blocklistService != null) {
            val result = blocklistService.shouldInterceptUrl(uri, filters)
            if (result.isBlocked) {
                val isTracker = result.type == com.example.data.model.BlocklistType.TRACKING
                val isScript = (uri.path ?: "").endsWith(".js") || uri.toString().contains(".js?")
                onBlocked(uri, isScript, isTracker)

                return if (isScript) createEmptyScriptResponse() else createEmptyNetworkResponse()
            }
        }

        // 3. Fallback to AdBlockEngine domain & keyword heuristics
        if (AdBlockEngine.isAdOrTracker(uri, filters)) {
            val isTracker = uri.toString().contains("analytics") ||
                    uri.toString().contains("tracker") ||
                    uri.toString().contains("telemetry") ||
                    uri.toString().contains("gtag") ||
                    uri.toString().contains("pixel")
            val isScript = (uri.path ?: "").endsWith(".js") || uri.toString().contains(".js?")

            onBlocked(uri, isScript, isTracker)
            return if (isScript) createEmptyScriptResponse() else createEmptyNetworkResponse()
        }

        return null
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        return handleUrlOverride(view, url)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("shouldOverrideUrlLoading(view, request)"))
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return handleUrlOverride(view, url)
    }

    private fun handleUrlOverride(view: WebView?, url: String): Boolean {
        val filters = getFilterSettings()

        // ── Link-click / redirect ad blocker ──
        // When the user taps a link, intercept navigation to ad / porn / redirect-trap URLs
        // before the WebView loads them ("this link takes me to another ad page").
        val uri = try { Uri.parse(url) } catch (_: Exception) { null }
        if (uri != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            val filtersForLink = filters

            // 1. Porn / adult content (gated by adultContentBlocking)
            if (filtersForLink?.adBlock?.adultContentBlocking != false &&
                AdBlockEngine.isPornDomain(uri.host)) {
                onBlocked(uri, false, true)
                view?.post {
                    android.widget.Toast.makeText(
                        view.context,
                        "🛑 Swordfish Shield: Blocked adult content",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }

            // 2. Ad network / redirect-trap domains (pop-ads that open on link click)
            if (filtersForLink?.adBlock?.networkAdInterception != false &&
                AdBlockEngine.isAdOrTracker(uri, filtersForLink)) {
                onBlocked(uri, false, false)
                view?.post {
                    android.widget.Toast.makeText(
                        view.context,
                        "🛑 Swordfish Shield: Blocked ad redirect",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }

            // 3. Redirect-trap URL heuristics (common ad-redirect path patterns)
            val lowerUrl = url.lowercase()
            val redirectAdKeywords = listOf(
                "/click/", "/redirect/", "/goto/", "/out/", "/goto.php", "/adclick/",
                "/adredirect/", "/track/", "/tracking/", "/adserver/", "/interstitial/",
                "/popunder/", "/popad/", "adf.ly", "bit.ly", "t.co", "ouo.io", "shorte.st",
                "adfly", "adfoc.us", "linkshrink", "tinyurl", "clk.im", "adshort", "shrtco.de",
                "cutt.ly", "tiny.cc", "rebrand.ly", "goo.gl", "ow.ly", "buff.ly", "is.gd",
                "v.gd", "soo.gd", "po.st", "trib.al", "url.sh", "clickmeter.com", "go2.cloud",
                "adcash.com", "adf.ly", "adfly", "adflylink.com", "popads.net", "popcash.net",
                "popunder", "popunderjs", "popunder-popup", "interstitial-ad", "interstitial",
                "linkbucks", "linkbucks.com", "adlinkfly", "safelink", "safelinkconverter",
                "adsfly", "ouo.io", "ouo", "bolt.sh", "shrinkme.io", "ad4m.org"
            )
            if (redirectAdKeywords.any { lowerUrl.contains(it) }) {
                onBlocked(uri, false, false)
                view?.post {
                    android.widget.Toast.makeText(
                        view.context,
                        "🛑 Swordfish Shield: Blocked ad redirect trap",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        }

        // Security Systems: Malicious Site Shield
        if (filters?.security?.maliciousSiteShield == true) {
            val (isMalicious, reason) = com.example.data.security.SecurityEngine.checkMalicious(url)
            if (isMalicious) {
                val uri = try { Uri.parse(url) } catch (_: Exception) { null }
                if (uri != null) {
                    onBlocked(uri, false, true)
                }
                view?.post {
                    android.widget.Toast.makeText(
                        view.context,
                        "🛑 Swordfish Security Shield: Blocked dangerous site ($reason)",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                return true
            }
        }

        // Security Systems: HTTPS-Only Mode Upgrade
        if (filters?.security?.httpsOnlyMode == true) {
            val (upgraded, secureUrl) = com.example.data.security.SecurityEngine.upgradeToHttps(url, true)
            if (upgraded) {
                view?.post {
                    view.loadUrl(secureUrl)
                }
                return true
            }
        }

        if (checkIsUrlDistractionBlocked != null && checkIsUrlDistractionBlocked.invoke(url)) {
            onUrlBlockedByAppBlocker?.invoke(url)
            return true
        }

        // Handle external URI schemes gracefully (intent:, market:, mailto:, tel:)
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("about:") && !url.startsWith("data:")) {
            try {
                val intent = android.content.Intent.parseUri(url, android.content.Intent.URI_INTENT_SCHEME)
                if (intent != null) {
                    val context = view?.context
                    if (context != null) {
                        intent.addCategory(android.content.Intent.CATEGORY_BROWSABLE)
                        intent.component = null
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                            return true
                        }
                    }
                }
            } catch (_: Exception) {}
            return true // Prevent WebView from throwing net::ERR_UNKNOWN_URL_SCHEME
        }

        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStartedCallback?.invoke(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinishedCallback?.invoke(view, url)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        onReceivedErrorCallback?.invoke(view, request, error)
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: android.webkit.SslErrorHandler?,
        error: android.net.http.SslError?
    ) {
        // Allow navigation in cloud emulator environments without silent abort
        handler?.proceed()
    }
}
