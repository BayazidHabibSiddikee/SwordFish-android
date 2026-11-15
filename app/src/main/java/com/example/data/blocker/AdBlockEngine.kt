package com.example.data.blocker

import android.net.Uri
import android.webkit.WebResourceResponse
import com.example.data.model.FilterSettings
import java.io.ByteArrayInputStream

object AdBlockEngine {

    // Known global advertising networks, analytics, trackers, and telemetry domains
    private val AD_DOMAINS = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adnxs.com",
        "criteo.com",
        "outbrain.com",
        "taboola.com",
        "popads.net",
        "popcash.net",
        "adcolony.com",
        "unityads.unity3d.com",
        "applifier.com",
        "vungle.com",
        "chartboost.com",
        "scorecardresearch.com",
        "quantserve.com",
        "moatads.com",
        "rubiconproject.com",
        "openx.net",
        "pubmatic.com",
        "casalemedia.com",
        "bidswitch.net",
        "advertising.com",
        "adroll.com",
        "smartadserver.com",
        "adtechus.com",
        "serving-sys.com",
        "zedo.com",
        "revcontent.com",
        "mgid.com",
        "exoclick.com",
        "propellerads.com",
        "trafficjunky.com",
        "amazon-adsystem.com",
        "connect.facebook.net",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "hotjar.com",
        "segment.io",
        "mixpanel.com",
        "adform.net",
        "bidvertiser.com",
        "infolinks.com",
        "buysellads.com",
        "yandexadexchange.net",
        "aniview.com",
        "applovin.com",
        "admob.com",
        "flurry.com",
        "branch.io",
        "appsflyer.com",
        "adjust.com",
        "adition.com",
        "adfox.ru",
        "adblade.com",
        "adsterra.com",
        "richaudience.com",
        "sharethrough.com",
        "yieldmo.com",
        "sovrn.com",
        "sonobi.com",
        "triplelift.com",
        "innovid.com",
        "exponential.com",
        "adtelligent.com",
        "adview.media",
        "adsrvr.org",
        "amobee.com",
        "appnexus.com",
        "audienceinsights.net",
        "behavioralengine.com",
        "bidswitch.com",
        "brightroll.com",
        "connatix.com",
        "controlletmedia.com",
        "conversantmedia.com",
        "crwdcntrl.net",
        "demandbase.com",
        "disqus.com",
        "dotomi.com",
        "doublepulse.com",
        "engagedid.com",
        "eyeota.com",
        "everesttech.net",
        "exoclick.com",
        "eyota.com",
        "forcecdn.net",
        "freewheel.net",
        "go-mpulse.net",
        "gumgum.com",
        "impactradius-event.com",
        "impactradius-go.com",
        "improvedigital.com",
        "indexww.com",
        "ipredictive.com",
        "kruxdigital.com",
        "liverail.com",
        "lotame.com",
        "media.net",
        "mediavine.com",
        "newrelic.com",
        "nrich.ai",
        "onebyaol.com",
        "openx.net",
        "optimizeconversion.com",
        "outbrain.com",
        "permutive.com",
        "pulsepoint.com",
        "quantcast.com",
        "qubit.com",
        "reinvigorate.net",
        "rubiconproject.com",
        "ru4.com",
        "semasio.net",
        "servebom.com",
        "sitebeam.net",
        "smaato.net",
        "sublimead.com",
        "taboola.com",
        "thetradeDesk.com",
        "tubemogul.com",
        "turn.com",
        "unrulymedia.com",
        "ve.interclick.com",
        "visualrevenue.com",
        "w55c.net",
        "wcommon.net",
        "xaxis.com",
        "zemanta.com",
        "zolo.com"
    )

    // Adult / pornography domains blocked at network level (gated by adultContentBlocking)
    private val PORN_DOMAINS = hashSetOf(
        "pornhub.com", "xvideos.com", "xnxx.com", "xhamster.com", "redtube.com",
        "youporn.com", "tube8.com", "spankbang.com", "chaturbate.com", "livejasmin.com",
        "brazzers.com", "realitykings.com", "naughtyamerica.com", "bangbros.com",
        "twistys.com", "digitalplayground.com", "21sextury.com", "mofos.com",
        "faketaxi.com", "girlsdoporn.com", "blacked.com", "vixen.com", "tushy.com",
        "slayed.com", "deeper.com", "girlsway.com", "onlyfans.com", "manyvids.com",
        "clips4sale.com", "streamate.com", "imlive.com", "camsoda.com", "bongacams.com",
        "stripchat.com", "xvideos2.com", "xvideos3.com", "redtube.porn", "youporn.porn",
        "thumblinker.com", "fapvid.com", "hqtube.com", "pornrox.com", "madthumbs.com",
        "tubegalore.com", "proporn.com", "eporner.com", "pornhat.com", "porntrex.com",
        "fuq.com", "ixxx.com", "hclips.com", "gotporn.com", "hellporno.com",
        "4tube.com", "spankwire.com", "keezmovies.com", "nuvid.com", "pornmaki.com",
        "zoig.com", "sunporno.com", "beeg.com", "tnaflix.com", "pornwhite.com",
        "xtube.com", "gaytube.com", "malehub.com", "boyfriendtv.com", "men.com",
        "realitydudes.com", "nextdoorstudios.com", "corbinfisher.com", "chaosmen.com"
    )

    // Adult-content URL path / keyword heuristics
    private val PORN_KEYWORDS = listOf(
        "/porn/", "/xxx/", "/sex/", "/adult/", "/nsfw/", "/erotic/",
        "tube8", "redtube", "youporn", "xhamster", "xnxx", "pornhub",
        "onlyfans", "manyvids", "chaturbate", "livejasmin"
    )

    private val AD_KEYWORDS = listOf(
        "/pagead/",
        "/ads/banner",
        "/ads/display",
        "/adserver/",
        "/adclick",
        "ads.min.js",
        "/gtag/js",
        "/fbevents.js",
        "show_ads.js",
        "/api/stats/ads",
        "/pagead/parallelverify",
        "googleads.g.doubleclick.net"
    )

    // Bridge-aware JavaScript AdBlocker communicating via window.SwordfishBridge
    const val PROPER_JS_ADBLOCKER = """
        (function() {
            if (window.__swordfish_adblocker_installed__) return;
            window.__swordfish_adblocker_installed__ = true;

            var bridge = window.SwordfishBridge;

            // 1. Popup & Popunder Trap with Bridge notification
            var originalOpen = window.open;
            window.open = function(url, target, features) {
                if (!url || url === 'about:blank' || (typeof url === 'string' && (url.includes('ad') || url.includes('pop') || url.includes('click') || url.includes('track')))) {
                    if (bridge && typeof bridge.reportPopupBlocked === 'function') {
                        bridge.reportPopupBlocked(url || 'popup');
                    }
                    return null;
                }
                return originalOpen.apply(this, arguments);
            };

            // 2. Fetch custom ad selectors from Bridge if available, otherwise use defaults
            var adSelectors = [
                '.adsbygoogle', '[id*="google_ads"]', '[class*="ad-box"]', '[class*="ad-banner"]',
                '[class*="advertisement"]', '[class*="ad-container"]', '[class*="sponsor"]',
                'iframe[src*="doubleclick"]', 'iframe[src*="adservice"]',
                'iframe[src*="googlesyndication"]', '.taboola', '.outbrain',
                '.trc_related_container', '.mgid-wrapper', 'div[data-ad]', 'div[data-ad-unit]',
                'div[id*="dfp-ad"]', 'ins.adsbygoogle', '.ad-slot', '[id*="ad-slot"]',
                '[class*="adunit"]', '[id*="adunit"]', 'div[class*="native-ad"]',
                'aside[id*="ad"]', 'aside[class*="ad"]'
            ];

            if (bridge && typeof bridge.getCustomBlockSelectors === 'function') {
                try {
                    var customSelectors = JSON.parse(bridge.getCustomBlockSelectors());
                    if (Array.isArray(customSelectors) && customSelectors.length > 0) {
                        adSelectors = customSelectors;
                    }
                } catch(e) {}
            }

            // 3. Inject CSS to hide all ad elements immediately
            var css = adSelectors.join(',\n') + ' {\n' +
                '  display: none !important;\n' +
                '  visibility: hidden !important;\n' +
                '  height: 0 !important;\n' +
                '  min-height: 0 !important;\n' +
                '  max-height: 0 !important;\n' +
                '  pointer-events: none !important;\n' +
                '  opacity: 0 !important;\n' +
                '}';
            var style = document.createElement('style');
            style.type = 'text/css';
            style.id = 'swordfish-bridge-styles';
            style.appendChild(document.createTextNode(css));
            (document.head || document.documentElement).appendChild(style);

            // 4. Dynamic Node Filtering & Bridge Reporting
            function removeAdNodes() {
                try {
                    adSelectors.forEach(function(sel) {
                        var elements = document.querySelectorAll(sel);
                        if (elements.length > 0) {
                            var removed = 0;
                            for (var i = 0; i < elements.length; i++) {
                                var el = elements[i];
                                if (el && el.parentNode) {
                                    el.parentNode.removeChild(el);
                                    removed++;
                                }
                            }
                            if (removed > 0 && bridge && typeof bridge.reportAdBlocked === 'function') {
                                bridge.reportAdBlocked(sel, removed);
                            }
                        }
                    });

                    // Anti-Adblock modal neutralizer
                    var antiAdblockModals = document.querySelectorAll(
                        '[class*="adblock-modal"], [id*="adblock-blocker"], [class*="sp_message_container"], div[class*="adblock-notice"]'
                    );
                    for (var j = 0; j < antiAdblockModals.length; j++) {
                        var modal = antiAdblockModals[j];
                        if (modal && modal.parentNode) {
                            modal.parentNode.removeChild(modal);
                            document.body.style.overflow = 'auto';
                            document.documentElement.style.overflow = 'auto';
                            if (bridge && typeof bridge.reportAntiAdblockNeutralized === 'function') {
                                bridge.reportAntiAdblockNeutralized(modal.className || 'modal');
                            }
                        }
                    }
                } catch(e) {}
            }

            // Run immediately and observe DOM mutations
            removeAdNodes();
            var observer = new MutationObserver(function() {
                removeAdNodes();
            });
            if (document.body) {
                observer.observe(document.body, { childList: true, subtree: true });
            } else {
                document.addEventListener('DOMContentLoaded', function() {
                    observer.observe(document.body, { childList: true, subtree: true });
                });
            }
        })();
    """

    // Bridge-aware YouTube AdBlocker
    const val YOUTUBE_ADBLOCKER_JS = """
        (function() {
            if (window.__swordfish_yt_blocker_active__) return;
            window.__swordfish_yt_blocker_active__ = true;

            var bridge = window.SwordfishBridge;

            // YouTube ad hiding CSS
            var ytCss = `
                ytd-promoted-video-renderer, ytd-display-ad-renderer, ytd-statement-banner-renderer,
                ytd-banner-promo-renderer, ytd-ad-slot-renderer, ytd-in-feed-ad-layout-renderer,
                ytm-promoted-sparkles-web-renderer, ytm-companion-ad-renderer, .ytp-ad-overlay-container,
                #player-ads, .ytp-ad-message-container, ytd-player-legacy-desktop-watch-ads-renderer,
                yt-upsell-dialog-renderer, .sparkles-light-cta, ytm-promoted-video-renderer,
                .ytp-ad-module, ytd-merch-shelf-renderer, #masthead-ad {
                    display: none !important;
                    visibility: hidden !important;
                    height: 0 !important;
                    pointer-events: none !important;
                }
            `;
            var style = document.createElement('style');
            style.type = 'text/css';
            style.id = 'swordfish-yt-style';
            style.appendChild(document.createTextNode(ytCss));
            (document.head || document.documentElement).appendChild(style);

            // Fast loop to auto-skip YouTube video ads and notify bridge
            function skipYouTubeVideoAds() {
                try {
                    var isAdShowing = document.querySelector('.ad-showing, .ad-interrupting, .ytp-ad-player-overlay, .video-ads');
                    var video = document.querySelector('video');

                    if (video && isAdShowing) {
                        var duration = video.duration || 0;
                        video.muted = true;
                        video.playbackRate = 16.0;
                        if (video.duration && !isNaN(video.duration)) {
                            video.currentTime = video.duration;
                        }
                        if (bridge && typeof bridge.reportVideoAdSkipped === 'function') {
                            bridge.reportVideoAdSkipped(duration);
                        }
                    }

                    // Click skip button immediately when rendered
                    var skipButtons = document.querySelectorAll(
                        '.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button, .videoAdUiSkipButton, button.ytp-ad-skip-button-text, .ytp-ad-overlay-close-button'
                    );
                    for (var i = 0; i < skipButtons.length; i++) {
                        skipButtons[i].click();
                    }

                    // Dismiss premium / upsell dialogs
                    var dismissBtns = document.querySelectorAll('ytd-popup-container yt-button-renderer[dialog-dismiss]');
                    for (var j = 0; j < dismissBtns.length; j++) {
                        dismissBtns[j].click();
                    }
                } catch (e) {}
            }

            setInterval(skipYouTubeVideoAds, 250);
        })();
    """

    fun isAdOrTracker(uri: Uri, filterSettings: FilterSettings? = null): Boolean {
        if (filterSettings != null && !filterSettings.isMasterShieldEnabled) {
            return false
        }
        val host = uri.host?.lowercase() ?: return false
        val path = uri.path?.lowercase() ?: ""
        val fullUrl = uri.toString().lowercase()

        val isYtAd = host.contains("youtube.com") && (
            path.contains("/api/stats/ads") ||
            path.contains("/pagead/") ||
            fullUrl.contains("adformat=") ||
            fullUrl.contains("ad_type=") ||
            fullUrl.contains("ptracking")
        )

        if (isYtAd) {
            val ytFilterActive = filterSettings?.adBlock?.youtubeAdBlocking ?: true
            if (ytFilterActive) return true
        }

        val isTracker = host.contains("analytics") ||
            host.contains("tracker") ||
            host.contains("telemetry") ||
            path.contains("/gtag/") ||
            path.contains("/fbevents") ||
            host.contains("hotjar") ||
            host.contains("mixpanel") ||
            host.contains("segment.io")

        if (isTracker) {
            val trackerFilterActive = filterSettings?.tracking?.analyticsTelemetryBlocking ?: true
            if (!trackerFilterActive) return false
        } else {
            val adFilterActive = filterSettings?.adBlock?.networkAdInterception ?: true
            if (!adFilterActive) return false
        }

        // Suffix domain matching
        for (adDomain in AD_DOMAINS) {
            if (host == adDomain || host.endsWith(".$adDomain")) {
                return true
            }
        }

        // Keyword matching in host or path
        for (keyword in AD_KEYWORDS) {
            if (path.contains(keyword) || host.contains(keyword) || fullUrl.contains(keyword)) {
                return true
            }
        }

        // Adult / pornography domain blocking (gated by adultContentBlocking filter)
        val adultFilterActive = filterSettings?.adBlock?.adultContentBlocking ?: true
        if (adultFilterActive) {
            // Suffix domain matching against known adult domains
            for (pornDomain in PORN_DOMAINS) {
                if (host == pornDomain || host.endsWith(".$pornDomain")) {
                    return true
                }
            }
            // Keyword / path heuristics for adult content
            for (keyword in PORN_KEYWORDS) {
                if (path.contains(keyword) || host.contains(keyword) || fullUrl.contains(keyword)) {
                    return true
                }
            }
        }

        return false
    }

    /** Returns true if the given host is a known adult / pornography domain. */
    fun isPornDomain(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val h = host.lowercase()
        for (pornDomain in PORN_DOMAINS) {
            if (h == pornDomain || h.endsWith(".$pornDomain")) return true
        }
        return false
    }

    /**
     * Strips common tracking, affiliate, and campaign parameters (utm_*, gclid, fbclid, etc.)
     * from URLs to enforce user query privacy.
     */
    fun stripTrackingParameters(url: String): String {
        return try {
            val uri = Uri.parse(url)
            if (uri.query.isNullOrBlank()) return url

            val trackingParamPrefixes = listOf("utm_", "mc_", "_hs")
            val exactTrackingParams = setOf(
                "gclid", "fbclid", "msclkid", "yclid", "dclid", "twclid", "zanpid",
                "igshid", "_ga", "_gl", "wbraid", "gbraid", "wickedid"
            )

            val queryParamNames = uri.queryParameterNames
            val cleanBuilder = uri.buildUpon().clearQuery()

            var strippedAny = false
            for (param in queryParamNames) {
                val isTrackingParam = exactTrackingParams.contains(param.lowercase()) ||
                        trackingParamPrefixes.any { param.lowercase().startsWith(it) }

                if (isTrackingParam) {
                    strippedAny = true
                } else {
                    for (value in uri.getQueryParameters(param)) {
                        cleanBuilder.appendQueryParameter(param, value)
                    }
                }
            }

            if (strippedAny) cleanBuilder.build().toString() else url
        } catch (_: Exception) {
            url
        }
    }

    fun isYouTubeUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
