package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.regex.Pattern

/**
 * Utility for parsing, validating, and launching Google Drive links for
 * non-sensitive dive club materials (training videos, slides, PDFs, club guidelines).
 */
object GoogleDriveHelper {

    private val FILE_ID_PATTERN = Pattern.compile("/(?:file/)?d/([a-zA-Z0-9_-]+)")
    private val QUERY_ID_PATTERN = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)")
    private val FOLDER_ID_PATTERN = Pattern.compile("/folders/([a-zA-Z0-9_-]+)")

    /**
     * Checks if the given URL points to a Google Drive or Google Docs resource.
     */
    fun isGoogleDriveUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()
        return lower.contains("drive.google.com") ||
                lower.contains("docs.google.com") ||
                lower.contains("sheets.google.com") ||
                lower.contains("slides.google.com")
    }

    /**
     * Checks if the URL points specifically to a Google Drive folder.
     */
    fun isGoogleDriveFolder(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("/drive/folders/") || url.contains("drive.google.com/drive/u/")
    }

    /**
     * Checks if the URL points to Google Slides.
     */
    fun isGoogleSlides(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        return url.contains("docs.google.com/presentation") || url.contains("slides.google.com")
    }

    /**
     * Extracts the Google Drive file or folder ID from various URL formats.
     */
    fun extractFileOrFolderId(url: String?): String? {
        if (url.isNullOrBlank()) return null

        val folderMatcher = FOLDER_ID_PATTERN.matcher(url)
        if (folderMatcher.find()) {
            return folderMatcher.group(1)
        }

        val fileMatcher = FILE_ID_PATTERN.matcher(url)
        if (fileMatcher.find()) {
            return fileMatcher.group(1)
        }

        val queryMatcher = QUERY_ID_PATTERN.matcher(url)
        if (queryMatcher.find()) {
            return queryMatcher.group(1)
        }

        return null
    }

    /**
     * Returns a direct preview URL suitable for in-app web views or direct viewing.
     */
    fun getPreviewUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        val id = extractFileOrFolderId(url) ?: return url
        return if (isGoogleDriveFolder(url)) {
            "https://drive.google.com/drive/folders/$id"
        } else if (isGoogleSlides(url)) {
            "https://docs.google.com/presentation/d/$id/preview"
        } else {
            "https://drive.google.com/file/d/$id/preview"
        }
    }

    /**
     * Opens the Google Drive resource in the native Google Drive app or system browser.
     */
    fun openInGoogleDriveOrBrowser(context: Context, rawUrl: String) {
        if (rawUrl.isBlank()) return
        val cleanUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Link konnte nicht geöffnet werden: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
