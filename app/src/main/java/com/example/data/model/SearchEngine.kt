package com.example.data.model

enum class SearchEngine(val displayName: String, val searchUrl: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    BRAVE("Brave", "https://search.brave.com/search?q=", "https://search.brave.com"),
    BING("Bing", "https://www.bing.com/search?q=", "https://www.bing.com"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org")
}
