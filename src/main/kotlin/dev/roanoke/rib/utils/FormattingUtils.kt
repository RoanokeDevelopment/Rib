package dev.roanoke.rib.utils

import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class FormattingUtils {
    companion object {
        fun formatDate(instant: Instant): String {
            val formatter = DateTimeFormatter.ofPattern("d'th' MMMM yyyy")
                .withLocale(Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())

            return formatter.format(instant)
        }

        fun formatTimeDifference(start: Instant, end: Instant): String {
            val duration = Duration.between(start, end)
            val period = Period.between(start.atZone(ZoneId.systemDefault()).toLocalDate(),
                end.atZone(ZoneId.systemDefault()).toLocalDate())

            return when {
                period.years > 0 -> "${period.years} years ago"
                period.months > 0 -> "${period.months} months ago"
                period.days > 0 -> "${period.days} days ago"
                duration.toHours() > 0 -> "${duration.toHours()} hours ago"
                duration.toMinutes() > 0 -> "${duration.toMinutes()} minutes ago"
                else -> "Just now"
            }
        }

        fun parseDuration(input: String): Duration {
            val pattern = Regex("""(\d+)([smhd])""")
            val matchResult = pattern.matchEntire(input)

            if (matchResult != null) {
                val (amount, unit) = matchResult.destructured
                val amountLong = amount.toLong()

                return when (unit) {
                    "s" -> Duration.ofSeconds(amountLong)
                    "m" -> Duration.ofMinutes(amountLong)
                    "h" -> Duration.ofHours(amountLong)
                    "d" -> Duration.ofDays(amountLong)
                    else -> throw IllegalArgumentException("Unknown time unit: $unit")
                }
            } else {
                throw IllegalArgumentException("Invalid duration format: $input")
            }
        }

        fun getOrdinal(number: Int): String {
            val suffix = when {
                number % 100 in 11..13 -> "th"
                number % 10 == 1 -> "st"
                number % 10 == 2 -> "nd"
                number % 10 == 3 -> "rd"
                else -> "th"
            }
            return "$number$suffix"
        }

    }
}