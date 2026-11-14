package com.example.data.reader

import android.net.Uri
import com.example.data.model.ReaderArticle
import org.json.JSONObject

/**
 * Intelligent Reader Mode Extraction Engine.
 * Strips away advertisements, sidebars, navigation banners, cookie notices, and clutter
 * from loaded web pages and extracts pristine, distraction-free article text, headings, and images.
 */
object ReaderModeEngine {

    /**
     * Checks if a URL is eligible for reader mode extraction (e.g. web pages, news articles, blogs).
     */
    fun isReaderEligibleUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase().trim()
        if (lower.startsWith("about:") || lower.startsWith("chrome:") || lower.startsWith("file:")) return false
        if (lower.contains("youtube.com") || lower.contains("youtu.be")) return false
        if (lower.contains("instagram.com") || lower.contains("tiktok.com")) return false
        if (lower.contains("login") || lower.contains("signin") || lower.contains("signup") || lower.contains("auth")) return false
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    /**
     * JavaScript code injected into the WebView to extract clean article content
     * and report back to native code via window.SwordfishBridge.reportReaderContent.
     */
    val READER_EXTRACTOR_JS = """
        (function() {
            try {
                // 1. Check if extraction is already running
                if (window.__swordfish_reader_extracting__) return;
                window.__swordfish_reader_extracting__ = true;

                var doc = document;
                var bridge = window.SwordfishBridge;

                // 2. Extract Title
                var title = '';
                var ogTitle = doc.querySelector('meta[property="og:title"]');
                if (ogTitle && ogTitle.content) {
                    title = ogTitle.content.trim();
                } else {
                    var h1 = doc.querySelector('h1');
                    if (h1 && h1.innerText && h1.innerText.trim().length > 5) {
                        title = h1.innerText.trim();
                    } else {
                        title = doc.title || 'Untitled Article';
                    }
                }

                // 3. Extract Author / Byline
                var byline = '';
                var authorMeta = doc.querySelector('meta[name="author"]') || doc.querySelector('meta[property="article:author"]');
                if (authorMeta && authorMeta.content) {
                    byline = authorMeta.content.trim();
                } else {
                    var authorEl = doc.querySelector('[rel="author"], .byline, .author-name, .author, [itemprop="author"]');
                    if (authorEl && authorEl.innerText) {
                        byline = authorEl.innerText.trim();
                    }
                }

                // 4. Extract Publication Date
                var publishedDate = '';
                var dateMeta = doc.querySelector('meta[property="article:published_time"]') || doc.querySelector('meta[name="pubdate"]');
                if (dateMeta && dateMeta.content) {
                    publishedDate = dateMeta.content.trim();
                } else {
                    var timeEl = doc.querySelector('time, .published-date, .post-date, [itemprop="datePublished"]');
                    if (timeEl) {
                        publishedDate = (timeEl.getAttribute('datetime') || timeEl.innerText || '').trim();
                    }
                }

                // 5. Extract Site Name
                var siteName = '';
                var siteMeta = doc.querySelector('meta[property="og:site_name"]');
                if (siteMeta && siteMeta.content) {
                    siteName = siteMeta.content.trim();
                } else {
                    siteName = window.location.hostname.replace(/^www\./i, '');
                }

                // 6. Extract Top Featured Image
                var topImageUrl = '';
                var ogImage = doc.querySelector('meta[property="og:image"]');
                if (ogImage && ogImage.content) {
                    topImageUrl = ogImage.content.trim();
                }

                // 7. Clone root document body to parse safely without mutating active page
                var bodyClone = doc.body.cloneNode(true);

                // 8. Distraction & Ad Selectors to strictly purge
                var elementsToPurge = [
                    'script', 'style', 'nav', 'header', 'footer', 'aside', 'form',
                    'iframe', 'noscript', 'canvas', 'svg:not([role="img"])',
                    // Ad containers and networks
                    '.adsbygoogle', '[id*="google_ads"]', '[class*="ad-box"]', '[class*="ad-banner"]',
                    '[class*="advertisement"]', '[class*="ad-container"]', '[class*="sponsor"]',
                    '.taboola', '.outbrain', '.trc_related_container', '.mgid-wrapper',
                    'div[data-ad]', 'div[data-ad-unit]', 'div[id*="dfp-ad"]',
                    // Navigation, sidebars & recommendation widgets
                    '.navbar', '.nav', '.sidebar', '#sidebar', '.widget', '.social-share',
                    '.share-bar', '.share-buttons', '.comments', '#comments', '.comment-section',
                    '.related-posts', '.recommended', '.newsletter-signup', '.cookie-banner',
                    '.consent-modal', '.paywall-overlay', '.floating-bar', '[role="navigation"]',
                    '[role="banner"]', '[role="contentinfo"]', '[aria-hidden="true"]'
                ];

                elementsToPurge.forEach(function(sel) {
                    try {
                        var matched = bodyClone.querySelectorAll(sel);
                        for (var i = 0; i < matched.length; i++) {
                            matched[i].remove();
                        }
                    } catch (e) {}
                });

                // 9. Heuristic Article Node Candidate Selection
                var candidateSelectors = [
                    'article', '[role="article"]', '.post-content', '.entry-content',
                    '.article-body', '#article-body', '.story-body', '.story-content',
                    '.content-main', '#main-content', '.main-content', 'main'
                ];

                var bestNode = null;
                for (var s = 0; s < candidateSelectors.length; s++) {
                    var el = bodyClone.querySelector(candidateSelectors[s]);
                    if (el && el.innerText && el.innerText.trim().length > 200) {
                        bestNode = el;
                        break;
                    }
                }

                // Fallback scoring: inspect all div and section elements
                if (!bestNode) {
                    var candidates = bodyClone.querySelectorAll('div, section');
                    var maxScore = -1;
                    for (var c = 0; c < candidates.length; c++) {
                        var node = candidates[c];
                        var pElements = node.querySelectorAll('p');
                        if (pElements.length < 2) continue;

                        var textLen = node.innerText ? node.innerText.trim().length : 0;
                        if (textLen < 200) continue;

                        // Density calculation: favors nodes with rich paragraphs and low link density
                        var linkTextLen = 0;
                        var links = node.querySelectorAll('a');
                        for (var l = 0; l < links.length; l++) {
                            linkTextLen += links[l].innerText ? links[l].innerText.length : 0;
                        }

                        var linkRatio = linkTextLen / (textLen || 1);
                        if (linkRatio > 0.5) continue; // Too many links (likely navigation or directory)

                        var score = (pElements.length * 30) + textLen - (linkRatio * 400);
                        if (score > maxScore) {
                            maxScore = score;
                            bestNode = node;
                        }
                    }
                }

                // If still not found, fallback to bodyClone
                if (!bestNode) {
                    bestNode = bodyClone;
                }

                // 10. Extract Clean Structural Elements from bestNode
                // Allowed tags: p, h2, h3, h4, h5, h6, blockquote, pre, code, ul, ol, li, img, figure, figcaption
                var allowedTags = ['P', 'H2', 'H3', 'H4', 'H5', 'H6', 'BLOCKQUOTE', 'PRE', 'CODE', 'UL', 'OL', 'LI', 'IMG', 'FIGURE', 'FIGCAPTION'];
                
                var cleanHtmlBlocks = [];
                var plainTextParts = [];

                // Helper to resolve absolute URLs for images
                function resolveUrl(relativeUrl) {
                    if (!relativeUrl) return '';
                    try {
                        var a = doc.createElement('a');
                        a.href = relativeUrl;
                        return a.href;
                    } catch(e) {
                        return relativeUrl;
                    }
                }

                // Iterate through direct children or relevant paragraphs & headings
                var contentNodes = bestNode.querySelectorAll('p, h2, h3, h4, blockquote, pre, ul, ol, img');
                
                // Track duplicate text to avoid repeating quotes or teasers
                var seenTexts = {};

                for (var n = 0; n < contentNodes.length; n++) {
                    var cNode = contentNodes[n];
                    var tag = cNode.tagName.toUpperCase();

                    if (tag === 'IMG') {
                        var src = cNode.getAttribute('src') || cNode.getAttribute('data-src') || cNode.getAttribute('data-original');
                        if (src && !src.includes('data:image') && !src.includes('tracker') && !src.includes('pixel') && !src.includes('ad')) {
                            var fullImgSrc = resolveUrl(src);
                            var alt = cNode.getAttribute('alt') || '';
                            cleanHtmlBlocks.push('<div class="reader-image-wrap"><img src="' + fullImgSrc + '" alt="' + alt + '" class="reader-image"/>' + (alt ? '<p class="reader-caption">' + alt + '</p>' : '') + '</div>');
                        }
                        continue;
                    }

                    var text = cNode.innerText ? cNode.innerText.trim() : '';
                    if (!text || text.length < 3) continue;

                    // Avoid duplicate lines
                    if (seenTexts[text]) continue;
                    seenTexts[text] = true;

                    plainTextParts.push(text);

                    if (tag === 'P') {
                        cleanHtmlBlocks.push('<p>' + cNode.innerHTML + '</p>');
                    } else if (tag.startsWith('H')) {
                        cleanHtmlBlocks.push('<' + tag.toLowerCase() + '>' + text + '</' + tag.toLowerCase() + '>');
                    } else if (tag === 'BLOCKQUOTE') {
                        cleanHtmlBlocks.push('<blockquote>' + text + '</blockquote>');
                    } else if (tag === 'PRE') {
                        cleanHtmlBlocks.push('<pre><code>' + text + '</code></pre>');
                    } else if (tag === 'UL' || tag === 'OL') {
                        cleanHtmlBlocks.push('<' + tag.toLowerCase() + '>' + cNode.innerHTML + '</' + tag.toLowerCase() + '>');
                    }
                }

                var fullPlainText = plainTextParts.join('\n\n');
                var fullCleanHtml = cleanHtmlBlocks.join('\n');

                // If content is too short, page might not be an article
                if (fullPlainText.length < 100 && (!bestNode.innerText || bestNode.innerText.trim().length < 100)) {
                    if (bridge && typeof bridge.reportReaderModeUnavailable === 'function') {
                        bridge.reportReaderModeUnavailable('Insufficient readable article text on this page.');
                    }
                    window.__swordfish_reader_extracting__ = false;
                    return;
                }

                // If content blocks were sparse, use paragraphs from innerText
                if (cleanHtmlBlocks.length === 0) {
                    var paragraphs = (bestNode.innerText || '').split('\n').filter(function(line) {
                        return line.trim().length > 25;
                    });
                    fullCleanHtml = paragraphs.map(function(p) { return '<p>' + p.trim() + '</p>'; }).join('\n');
                    fullPlainText = paragraphs.join('\n\n');
                }

                var words = fullPlainText.split(/\s+/).filter(Boolean).length;
                var readTime = Math.max(1, Math.ceil(words / 200));

                var articleData = {
                    title: title,
                    byline: byline,
                    siteName: siteName,
                    publishedDate: publishedDate,
                    contentHtml: fullCleanHtml,
                    plainText: fullPlainText,
                    wordCount: words,
                    readingTimeMinutes: readTime,
                    url: window.location.href,
                    topImageUrl: topImageUrl
                };

                if (bridge && typeof bridge.reportReaderContent === 'function') {
                    bridge.reportReaderContent(JSON.stringify(articleData));
                }

                window.__swordfish_reader_extracting__ = false;
            } catch (err) {
                if (window.SwordfishBridge && typeof window.SwordfishBridge.reportReaderModeUnavailable === 'function') {
                    window.SwordfishBridge.reportReaderModeUnavailable(err.toString());
                }
                window.__swordfish_reader_extracting__ = false;
            }
        })();
    """.trimIndent()

    /**
     * Fast availability check script to see if Reader Mode is supported on the current page.
     */
    val READER_AVAILABILITY_CHECK_JS = """
        (function() {
            try {
                var paragraphs = document.querySelectorAll('article p, main p, .post-content p, .article-body p, p');
                var totalWords = 0;
                for (var i = 0; i < Math.min(paragraphs.length, 10); i++) {
                    var text = paragraphs[i].innerText || '';
                    totalWords += text.split(/\s+/).filter(Boolean).length;
                    if (totalWords >= 120) {
                        if (window.SwordfishBridge && typeof window.SwordfishBridge.reportReaderAvailable === 'function') {
                            window.SwordfishBridge.reportReaderAvailable(true);
                        }
                        return;
                    }
                }
                if (window.SwordfishBridge && typeof window.SwordfishBridge.reportReaderAvailable === 'function') {
                    window.SwordfishBridge.reportReaderAvailable(totalWords >= 80);
                }
            } catch(e) {}
        })();
    """.trimIndent()

    /**
     * Sanitizes raw HTML into readable plain-paragraphs if rendering natively.
     */
    fun extractParagraphs(article: ReaderArticle): List<String> {
        if (article.plainText.isNotBlank()) {
            return article.plainText.split("\n\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        return emptyList()
    }
}
