package com.technion.fitracker

import android.content.SearchRecentSuggestionsProvider

class MySuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.technion.fitracker.MySuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}