package gg.urchin.client.commands

import java.time.Duration

private const val PERMANENT = "permanent"
private const val DAYS_PER_WEEK = 7
private val DEFAULT_LENGTH: Duration = Duration.ofDays(14)
private val LENGTH = Regex("""(\d+)([hdw]?)""")

internal sealed class WatchLength(val apiValue: String) {
    abstract fun expiresAt(now: Long = System.currentTimeMillis()): Long?

    object Permanent : WatchLength(PERMANENT) {
        override fun expiresAt(now: Long): Long? = null
    }

    class For(private val length: Duration) : WatchLength("${length.toHours()}h") {
        override fun expiresAt(now: Long): Long = now + length.toMillis()
    }

    companion object {
        fun parse(input: String): WatchLength? {
            val text = input.trim().lowercase()
            if (text.isEmpty()) return For(DEFAULT_LENGTH)
            if (text == PERMANENT) return Permanent
            val (count, unit) = LENGTH.matchEntire(text)?.destructured ?: return null
            val amount = count.toLongOrNull() ?: return null
            if (amount == 0L) return Permanent
            return For(
                when (unit) {
                    "h" -> Duration.ofHours(amount)
                    "w" -> Duration.ofDays(amount * DAYS_PER_WEEK)
                    else -> Duration.ofDays(amount)
                },
            )
        }
    }
}
