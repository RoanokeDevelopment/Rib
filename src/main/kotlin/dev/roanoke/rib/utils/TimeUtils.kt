package dev.roanoke.rib.utils

import java.time.Duration
import java.time.Instant
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TimeUtils {
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

    }
}