package com.notifmanager.utils

/**
 * Central constants file - All magic numbers and strings in one place
 * Makes the app easy to maintain and configure
 */
object Constants {

    // Database
    const val DATABASE_NAME = "notification_manager_db"
    const val DATABASE_VERSION = 1

    // Priority Score Ranges (0-100 scale)
    const val SCORE_CRITICAL_MIN = 70
    const val SCORE_IMPORTANT_MIN = 40
    const val SCORE_NORMAL_MIN = 15
    const val SCORE_SILENT_MAX = 14

    // Base Importance Weights (starting points for app categories)
    const val BASE_WEIGHT_BANKING = 80
    const val BASE_WEIGHT_MESSAGING = 60
    const val BASE_WEIGHT_EMAIL = 50
    const val BASE_WEIGHT_SOCIAL = 35
    const val BASE_WEIGHT_NEWS = 25
    const val BASE_WEIGHT_ENTERTAINMENT = 15
    const val BASE_WEIGHT_GAMES = 10

    // Frequency Penalty Thresholds (notifications per hour)
    const val FREQUENCY_LOW_THRESHOLD = 2
    const val FREQUENCY_MEDIUM_THRESHOLD = 5
    const val FREQUENCY_HIGH_THRESHOLD = 10

    // Frequency Penalty Multipliers
    const val PENALTY_NONE = 1.0f
    const val PENALTY_LOW = 0.9f
    const val PENALTY_MEDIUM = 0.7f
    const val PENALTY_HIGH = 0.5f
    const val PENALTY_SPAM = 0.3f

    // Behavior Learning Weights
    const val BEHAVIOR_WEIGHT_OPENED = 5  // Points added when user opens notification
    const val BEHAVIOR_WEIGHT_DISMISSED = -3  // Points removed when dismissed quickly
    const val BEHAVIOR_WEIGHT_IGNORED = -1  // Points removed when ignored completely
    const val BEHAVIOR_MAX_ADJUSTMENT = 20  // Max points behavior can add/remove

    // Time Windows
    const val TIME_WINDOW_FREQUENCY_CHECK_MS = 3600000L  // 1 hour in milliseconds
    const val TIME_WINDOW_RECENT_BEHAVIOR_DAYS = 7  // Look at last 7 days of behavior
    const val TIME_QUICK_DISMISS_THRESHOLD_MS = 3000L  // 3 seconds = quick dismiss

    // Background Cleanup
    const val CLEANUP_OLD_NOTIFICATIONS_DAYS = 30  // Delete notifications older than 30 days
    const val CLEANUP_SILENT_NOTIFICATIONS_DAYS = 7  // Delete silent notifications after 7 days

    // WorkManager Tags
    const val WORK_TAG_CLEANUP = "cleanup_worker"
    const val WORK_TAG_BEHAVIOR_UPDATE = "behavior_update_worker"

    // Notification Categories (for grouping)
    enum class NotificationCategory(val displayName: String, val colorHex: String) {
        CRITICAL("Critical", "#EF4444"),  // Red
        IMPORTANT("Important", "#F97316"),  // Orange
        NORMAL("Normal", "#3B82F6"),  // Blue
        SILENT("Silent", "#6B7280")  // Gray
    }

    // Keywords for content analysis (comprehensive list)
    object Keywords {
        // Critical keywords - instant high priority
        val CRITICAL = setOf(
            "otp", "verification code", "security code", "authentication",
            "failed", "failure", "declined", "rejected", "denied",
            "urgent", "emergency", "critical", "alert", "warning",
            "suspicious activity", "unauthorized", "breach", "fraud",
            "password reset", "account locked", "verify your identity",
            "payment failed", "transaction declined", "insufficient funds",
            "missed call", "voicemail", "alarm", "reminder", "due now",
            "expires today", "last chance", "time sensitive", "immediate action"
        )

        // Important keywords - medium-high priority
        val IMPORTANT = setOf(
            "message", "replied", "mentioned you", "tagged you",
            "shared with you", "sent you", "commented", "reacted",
            "delivery", "shipped", "out for delivery", "arriving",
            "appointment", "meeting", "scheduled", "confirmed",
            "invoice", "receipt", "payment", "charged", "subscription",
            "update", "new version", "upgrade", "download",
            "from:", "to:", "re:", "fwd:",  // Email indicators
            "booking", "reservation", "ticket", "order"
        )

        // Spam/Low priority keywords - reduce importance
        val SPAM = setOf(
            "new video", "uploaded", "live now", "streaming",
            "watch now", "click here", "tap to open", "check this out",
            "sale", "discount", "offer", "deal", "promo", "coupon",
            "free", "win", "prize", "reward", "claim", "gift",
            "like this", "follow", "subscribe", "share",
            "recommended for you", "you might like", "trending",
            "game", "level up", "achievement", "daily bonus",
            "energy refilled", "lives restored", "new content",
            "news:", "breaking:", "story", "article", "post",
            "add friend", "friend suggestion", "people you may know"
        )

        // Financial/Banking keywords - auto-boost
        val FINANCIAL = setOf(
            "bank", "account", "balance", "transaction", "transfer",
            "credit card", "debit card", "payment", "deposit", "withdrawal",
            "statement", "bill", "due", "overdue", "pending",
            "upi", "paytm", "gpay", "phonepe", "wallet"
        )
    }

    // App Package Categories (for base importance scoring)
    object AppCategories {
        val BANKING = setOf<String>(
            "com.google.android.apps.nbu.paisa.user",  // Google Pay
            "net.one97.paytm",  // Paytm
            "com.phonepe.app",  // PhonePe
            "com.axis.mobile",  // Axis Bank
            "com.sbi.lotusintouch",  // SBI
            "com.hdfc.mobile",  // HDFC
            "com.icicibank.pockets",  // ICICI
            // Add more banking apps
        )

        val MESSAGING = setOf<String>(
            "com.whatsapp",
            "com.whatsapp.w4b",  // WhatsApp Business
            "org.telegram.messenger",
            "com.discord",
            "com.snapchat.android",
            "com.facebook.orca",  // Messenger
            "com.google.android.apps.messaging",  // Messages
            "com.samsung.android.messaging"
        )

        val EMAIL = setOf<String>(
            "com.google.android.gm",  // Gmail
            "com.microsoft.office.outlook",
            "com.yahoo.mobile.client.android.mail",
            "com.samsung.android.email.provider"
        )

        val SOCIAL = setOf<String>(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.linkedin.android",
            "com.reddit.frontpage",
            "com.tumblr",
            "com.pinterest"
        )

        val ENTERTAINMENT = setOf<String>(
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.spotify.music",
            "com.amazon.avod.thirdpartyclient",  // Prime Video
            "com.hotstar.android"
        )

        val GAMES = setOf<String>(
            // This will match any app with game in package name
            // Individual games added as needed
        )
    }

    // Shared Preferences Keys
    object PrefsKeys {
        const val DARK_MODE = "dark_mode_enabled"
        const val AUTO_DELETE_SILENT = "auto_delete_silent_notifications"
        const val AUTO_DELETE_DAYS = "auto_delete_days"
        const val LEARNING_ENABLED = "learning_enabled"
        const val FIRST_RUN = "first_run"
    }
}