package com.wenodh.zen.schedule

import java.util.Calendar
import java.util.TimeZone

object DailySchedule {
    fun nextTriggerMillis(
        nowMillis: Long,
        hour: Int,
        minute: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        require(hour in 0..23 && minute in 0..59)
        val now = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
        val next = now.clone() as Calendar
        next.set(Calendar.HOUR_OF_DAY, hour)
        next.set(Calendar.MINUTE, minute)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        if (next.timeInMillis <= nowMillis) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis
    }
}
