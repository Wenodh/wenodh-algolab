package com.wenodh.zen.schedule

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DailyScheduleTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test
    fun selectsLaterTimeOnSameDay() {
        val now = Calendar.getInstance(utc).apply {
            set(2026, Calendar.JANUARY, 2, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val expected = Calendar.getInstance(utc).apply {
            set(2026, Calendar.JANUARY, 2, 9, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(expected, DailySchedule.nextTriggerMillis(now, 9, 30, utc))
    }

    @Test
    fun rollsToTomorrowWhenTodaysTimeHasPassed() {
        val now = Calendar.getInstance(utc).apply {
            set(2026, Calendar.JANUARY, 2, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val expected = Calendar.getInstance(utc).apply {
            set(2026, Calendar.JANUARY, 3, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(expected, DailySchedule.nextTriggerMillis(now, 9, 0, utc))
    }
}
