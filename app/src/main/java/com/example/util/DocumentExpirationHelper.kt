package com.example.util

import com.example.data.model.DiverDocumentEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Expiration Alert Levels for diver credentials & medical statements.
 */
enum class ExpirationAlertLevel {
    EXPIRED,        // Past expiry date (Red alert)
    EXPIRING_SOON,  // Within 30 days (or 60 days for medical/insurance) (Amber warning)
    VALID,          // Normal valid with > 30 days remaining (Green)
    LIFETIME        // Lifetime certification / no expiration date (Cyan/Blue)
}

/**
 * Rich expiration metadata for UI badges, countdown banners, and verification dialogs.
 */
data class DocumentExpirationInfo(
    val level: ExpirationAlertLevel,
    val daysRemaining: Long?,
    val badgeLabel: String,
    val detailStatusText: String,
    val urgencyScore: Int // 0: Lifetime, 1: Valid, 2: Expiring Soon, 3: Expired (for sorting alerts to top)
)

object DocumentExpirationHelper {

    private val DATE_FORMATS = listOf(
        "yyyy-MM-dd",
        "dd.MM.yyyy",
        "MM/dd/yyyy",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy"
    )

    private val LIFETIME_KEYWORDS = listOf(
        "lifetime",
        "no expiration",
        "permanent",
        "unbefristet",
        "kein ablauf",
        "none",
        "n/a",
        "unlimited",
        "forever",
        "-"
    )

    /**
     * Compute expiration status and localized descriptions for a document.
     */
    fun computeExpirationInfo(
        document: DiverDocumentEntity,
        isGerman: Boolean = false
    ): DocumentExpirationInfo {
        val rawExpiry = document.expiryDate.trim()

        if (rawExpiry.isBlank() || LIFETIME_KEYWORDS.any { rawExpiry.contains(it, ignoreCase = true) }) {
            return DocumentExpirationInfo(
                level = ExpirationAlertLevel.LIFETIME,
                daysRemaining = null,
                badgeLabel = if (isGerman) "Unbefristet" else "Lifetime Cert",
                detailStatusText = if (isGerman) "Dauerhaft gültig ohne Ablaufdatum" else "Permanent validity / No expiration date required",
                urgencyScore = 0
            )
        }

        val parsedDate = parseDate(rawExpiry)
        if (parsedDate == null) {
            // Cannot parse as standard date, treat as Custom / Lifetime
            return DocumentExpirationInfo(
                level = ExpirationAlertLevel.LIFETIME,
                daysRemaining = null,
                badgeLabel = rawExpiry.take(16),
                detailStatusText = if (isGerman) "Gültigkeit: $rawExpiry" else "Validity: $rawExpiry",
                urgencyScore = 0
            )
        }

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val expiryCalendar = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = expiryCalendar.timeInMillis - todayCalendar.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 0 -> {
                val daysAgo = abs(diffDays)
                val badge = if (isGerman) {
                    if (daysAgo == 1L) "Gestern abgelaufen" else "Abgelaufen (vor $daysAgo T.)"
                } else {
                    if (daysAgo == 1L) "Expired yesterday" else "Expired ($daysAgo d ago)"
                }
                val detail = if (isGerman) {
                    "Dokument ist seit dem $rawExpiry (vor $daysAgo Tagen) abgelaufen. Tauchbasen verlangen ein aktuelles Dokument!"
                } else {
                    "Document expired on $rawExpiry ($daysAgo days ago). Dive centers may refuse boat or excursion admission!"
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.EXPIRED,
                    daysRemaining = diffDays,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 3
                )
            }
            diffDays == 0L -> {
                val badge = if (isGerman) "Läuft HEUTE ab!" else "Expires TODAY!"
                val detail = if (isGerman) {
                    "Gültigkeit endet heute ($rawExpiry). Verlängerung erforderlich!"
                } else {
                    "Validity ends today ($rawExpiry). Immediate renewal recommended!"
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.EXPIRING_SOON,
                    daysRemaining = 0L,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 2
                )
            }
            diffDays == 1L -> {
                val badge = if (isGerman) "Läuft morgen ab!" else "Expires tomorrow!"
                val detail = if (isGerman) {
                    "Gültigkeit endet morgen ($rawExpiry). Verlängerung vorbereiten."
                } else {
                    "Validity ends tomorrow ($rawExpiry). Renewal recommended."
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.EXPIRING_SOON,
                    daysRemaining = 1L,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 2
                )
            }
            diffDays <= 30 -> {
                val badge = if (isGerman) "Läuft in $diffDays T. ab" else "Expires in $diffDays d"
                val detail = if (isGerman) {
                    "Läuft in $diffDays Tagen ab ($rawExpiry). Bitte rechtzeitig erneuern."
                } else {
                    "Expires in $diffDays days ($rawExpiry). Please renew before your next dive trip."
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.EXPIRING_SOON,
                    daysRemaining = diffDays,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 2
                )
            }
            diffDays <= 60 && (document.category == "MEDICAL_FORM" || document.category == "INSURANCE") -> {
                val badge = if (isGerman) "Ablauf in $diffDays T. (Erneuern)" else "Expires in $diffDays d (Renew)"
                val detail = if (isGerman) {
                    "Ärztliches Attest / Versicherung läuft in $diffDays Tagen ab ($rawExpiry). Neuer Arzttermin empfohlen."
                } else {
                    "Medical clearance / insurance expires in $diffDays days ($rawExpiry). Schedule your checkup soon."
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.EXPIRING_SOON,
                    daysRemaining = diffDays,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 2
                )
            }
            else -> {
                val months = (diffDays / 30).coerceAtLeast(1)
                val badge = if (isGerman) {
                    if (diffDays > 365) "Gültig (> 1 Jahr)" else "Gültig ($months Mon. / $diffDays T.)"
                } else {
                    if (diffDays > 365) "Valid (> 1 year)" else "Valid ($months mo / $diffDays d)"
                }
                val detail = if (isGerman) {
                    "Gültig bis zum $rawExpiry (noch $diffDays Tage verbleibend)."
                } else {
                    "Valid through $rawExpiry ($diffDays days remaining)."
                }
                DocumentExpirationInfo(
                    level = ExpirationAlertLevel.VALID,
                    daysRemaining = diffDays,
                    badgeLabel = badge,
                    detailStatusText = detail,
                    urgencyScore = 1
                )
            }
        }
    }

    private fun parseDate(dateStr: String): Date? {
        for (pattern in DATE_FORMATS) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                }
                val date = sdf.parse(dateStr)
                if (date != null) return date
            } catch (_: Exception) {}
        }
        return null
    }

    fun getExpiredDocuments(docs: List<DiverDocumentEntity>, isGerman: Boolean = false): List<Pair<DiverDocumentEntity, DocumentExpirationInfo>> {
        return docs.map { it to computeExpirationInfo(it, isGerman) }
            .filter { it.second.level == ExpirationAlertLevel.EXPIRED }
    }

    fun getExpiringSoonDocuments(docs: List<DiverDocumentEntity>, isGerman: Boolean = false): List<Pair<DiverDocumentEntity, DocumentExpirationInfo>> {
        return docs.map { it to computeExpirationInfo(it, isGerman) }
            .filter { it.second.level == ExpirationAlertLevel.EXPIRING_SOON }
    }

    fun getAlertDocuments(docs: List<DiverDocumentEntity>, isGerman: Boolean = false): List<Pair<DiverDocumentEntity, DocumentExpirationInfo>> {
        return docs.map { it to computeExpirationInfo(it, isGerman) }
            .filter { it.second.level == ExpirationAlertLevel.EXPIRED || it.second.level == ExpirationAlertLevel.EXPIRING_SOON }
            .sortedByDescending { it.second.urgencyScore }
    }

    fun computeDateExpiration(dateStr: String?, isGerman: Boolean = false): DocumentExpirationInfo {
        if (dateStr.isNullOrBlank()) {
            return DocumentExpirationInfo(
                level = ExpirationAlertLevel.EXPIRED,
                daysRemaining = null,
                badgeLabel = if (isGerman) "Keine TTU / Fehlt" else "No Medical / Missing",
                detailStatusText = if (isGerman) "Kein ärztliches Tauchtauglichkeitsattest hinterlegt." else "No medical fitness document on file.",
                urgencyScore = 3
            )
        }
        val raw = dateStr.trim()
        val parsedDate = parseDate(raw)
        if (parsedDate == null) {
            return DocumentExpirationInfo(
                level = ExpirationAlertLevel.LIFETIME,
                daysRemaining = null,
                badgeLabel = raw.take(16),
                detailStatusText = if (isGerman) "Gültigkeit: $raw" else "Validity: $raw",
                urgencyScore = 0
            )
        }
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expiryCalendar = Calendar.getInstance().apply {
            time = parsedDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = expiryCalendar.timeInMillis - todayCalendar.timeInMillis
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        return when {
            diffDays < 0 -> {
                val daysAgo = abs(diffDays)
                val badge = if (isGerman) "Abgelaufen (vor $daysAgo T.)" else "Expired ($daysAgo d ago)"
                val detail = if (isGerman) "TTU abgelaufen am $raw. Tauchfreigabe ausgesetzt." else "Medical expired on $raw. Diving clearance suspended."
                DocumentExpirationInfo(ExpirationAlertLevel.EXPIRED, diffDays, badge, detail, 3)
            }
            diffDays <= 30 -> {
                val badge = if (isGerman) "Ablauf in $diffDays T." else "Expires in $diffDays d"
                val detail = if (isGerman) "TTU läuft in $diffDays Tagen ab ($raw). Erneuerung erforderlich." else "Medical expires in $diffDays days ($raw). Renewal needed."
                DocumentExpirationInfo(ExpirationAlertLevel.EXPIRING_SOON, diffDays, badge, detail, 2)
            }
            else -> {
                val badge = if (isGerman) "Gültig bis $raw" else "Valid until $raw"
                val detail = if (isGerman) "Gültig bis $raw ($diffDays Tage verbleibend)." else "Valid through $raw ($diffDays days remaining)."
                DocumentExpirationInfo(ExpirationAlertLevel.VALID, diffDays, badge, detail, 1)
            }
        }
    }

    fun hasUrgentExpirationAlerts(docs: List<DiverDocumentEntity>): Boolean {
        return docs.any {
            val info = computeExpirationInfo(it)
            info.level == ExpirationAlertLevel.EXPIRED || info.level == ExpirationAlertLevel.EXPIRING_SOON
        }
    }
}
