package com.notifmanager.utils

/**
 * Central constants file
 *
 * UPDATED: Added auto-delete constants
 */
object Constants {

    // Database
    const val DATABASE_NAME = "notification_manager_db"
    const val DATABASE_VERSION = 2 // UPDATED for new tables

    // Priority Score Ranges (0-100 scale)
    const val SCORE_CRITICAL_MIN = 70
    const val SCORE_IMPORTANT_MIN = 40
    const val SCORE_NORMAL_MIN = 15
    const val SCORE_SILENT_MAX = 14

    // Base Importance Weights
    const val BASE_WEIGHT_BANKING = 80
    const val BASE_WEIGHT_MESSAGING = 60
    const val BASE_WEIGHT_EMAIL = 50
    const val BASE_WEIGHT_SOCIAL = 35
    const val BASE_WEIGHT_NEWS = 25
    const val BASE_WEIGHT_ENTERTAINMENT = 15
    const val BASE_WEIGHT_GAMES = 10

    // Frequency Penalty Thresholds
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
    const val BEHAVIOR_WEIGHT_OPENED = 5
    const val BEHAVIOR_WEIGHT_DISMISSED = -3
    const val BEHAVIOR_WEIGHT_IGNORED = -1
    const val BEHAVIOR_MAX_ADJUSTMENT = 20

    // Time Windows
    const val TIME_WINDOW_FREQUENCY_CHECK_MS = 3600000L  // 1 hour
    const val TIME_WINDOW_RECENT_BEHAVIOR_DAYS = 7
    const val TIME_QUICK_DISMISS_THRESHOLD_MS = 3000L  // 3 seconds

    // Background Cleanup (UPDATED)
    const val CLEANUP_OLD_NOTIFICATIONS_DAYS = 30  // Delete very old notifications
    const val CLEANUP_SILENT_NOTIFICATIONS_DAYS = 2  // Delete silent after 2 days (NEW)
    const val AUTO_DELETE_DEFAULT_DAYS = 2  // Auto-delete all after 2 days (NEW)

    // WorkManager Tags
    const val WORK_TAG_CLEANUP = "cleanup_worker"
    const val WORK_TAG_BEHAVIOR_UPDATE = "behavior_update_worker"

    // Notification Categories
    enum class NotificationCategory(val displayName: String, val colorHex: String) {
        CRITICAL("Critical", "#EF4444"),
        IMPORTANT("Important", "#F97316"),
        NORMAL("Normal", "#3B82F6"),
        SILENT("Silent", "#6B7280")
    }

    // Content Types (NEW - for channel/sender identification)
    enum class ContentType {
        YOUTUBE_CHANNEL,
        WHATSAPP_CONTACT,
        WHATSAPP_GROUP,
        SMS_SENDER,
        EMAIL_SENDER,
        INSTAGRAM_ACCOUNT,
        TWITTER_ACCOUNT,
        GENERIC
    }

    // Keywords for content analysis
    object Keywords {
        val CRITICAL = setOf<String>(
            "otp", "verification code", "security code", "authentication",
            "failed", "failure", "declined", "rejected", "denied",
            "urgent", "emergency", "critical", "alert", "warning",
            "suspicious activity", "unauthorized", "breach", "fraud",
            "password reset", "account locked", "verify your identity",
            "payment failed", "transaction declined", "insufficient funds",
            "missed call", "voicemail", "alarm", "reminder", "due now",
            "expires today", "last chance", "time sensitive", "immediate action"
        )

        val IMPORTANT = setOf<String>(
            "message", "replied", "mentioned you", "tagged you",
            "shared with you", "sent you", "commented", "reacted",
            "delivery", "shipped", "out for delivery", "arriving",
            "appointment", "meeting", "scheduled", "confirmed",
            "invoice", "receipt", "payment", "charged", "subscription",
            "update", "new version", "upgrade", "download",
            "from:", "to:", "re:", "fwd:",
            "booking", "reservation", "ticket", "order"
        )

        val SPAM = setOf<String>(
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

        val FINANCIAL = setOf<String>(
            "bank", "account", "balance", "transaction", "transfer",
            "credit card", "debit card", "payment", "deposit", "withdrawal",
            "statement", "bill", "due", "overdue", "pending",
            "upi", "paytm", "gpay", "phonepe", "wallet"
        )
    }

    // App Package Categories
    object AppCategories {
        val BANKING = setOf<String>(
            "com.google.android.apps.nbu.paisa.user",  // Google Pay
            "net.one97.paytm",
            "com.phonepe.app",
            "com.axis.mobile",
            "com.sbi.lotusintouch",
            "com.hdfc.mobile",
            "com.icicibank.pockets"
        )

        val MESSAGING = setOf<String>(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.discord",
            "com.snapchat.android",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging"
        )

        val EMAIL = setOf<String>(
            "com.google.android.gm",
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
            "com.amazon.avod.thirdpartyclient",
            "com.hotstar.android"
        )

        val GAMES = setOf<String>()
    }

    // Shared Preferences Keys (UPDATED)
    object PrefsKeys {
        const val DARK_MODE = "dark_mode_enabled"
        const val AUTO_DELETE_SILENT = "auto_delete_silent_notifications"
        const val AUTO_DELETE_DAYS = "auto_delete_days"  // NEW
        const val LEARNING_ENABLED = "learning_enabled"
        const val FIRST_RUN = "first_run"
    }
}