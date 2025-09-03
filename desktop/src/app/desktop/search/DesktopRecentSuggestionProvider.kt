package app.desktop.search

import android.content.SearchRecentSuggestionsProvider
import com.android.launcher3.BuildConfig

class DesktopRecentSuggestionProvider : SearchRecentSuggestionsProvider() {
    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".search.DesktopRecentSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}
