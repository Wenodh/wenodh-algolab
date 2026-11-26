package com.wenodh.zen.focus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Small local history database; no network or account is required. */
internal class FocusSessionHistoryDb(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createSessionsTable(db)
        createBlockedAttemptsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createBlockedAttemptsTable(db)
    }

    private fun createSessionsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sessions (
                id TEXT PRIMARY KEY NOT NULL,
                started_at INTEGER NOT NULL,
                ends_at INTEGER NOT NULL,
                duration_minutes INTEGER NOT NULL,
                status TEXT NOT NULL,
                finished_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_started_at ON sessions(started_at)")
    }

    private fun createBlockedAttemptsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS blocked_attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id TEXT NOT NULL,
                package_name TEXT NOT NULL,
                attempted_at INTEGER NOT NULL,
                FOREIGN KEY(session_id) REFERENCES sessions(id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_attempts_session ON blocked_attempts(session_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_blocked_attempts_time ON blocked_attempts(attempted_at)")
    }

    fun insertSession(id: String, startedAt: Long, endsAt: Long, durationMinutes: Int) {
        writableDatabase.execSQL(
            "INSERT INTO sessions(id, started_at, ends_at, duration_minutes, status) VALUES(?, ?, ?, ?, ?)",
            arrayOf(id, startedAt, endsAt, durationMinutes, SessionTimeline.STATUS_ACTIVE)
        )
    }

    fun finishSession(id: String, status: String, finishedAt: Long) {
        writableDatabase.execSQL(
            "UPDATE sessions SET status = ?, finished_at = ? WHERE id = ? AND status = ?",
            arrayOf(status, finishedAt, id, SessionTimeline.STATUS_ACTIVE)
        )
    }

    fun recordBlockedAttempt(sessionId: String, packageName: String, attemptedAt: Long) {
        val values = ContentValues().apply {
            put("session_id", sessionId)
            put("package_name", packageName)
            put("attempted_at", attemptedAt)
        }
        writableDatabase.insertOrThrow("blocked_attempts", null, values)
    }

    fun blockedAttemptCount(sessionId: String): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM blocked_attempts WHERE session_id = ?",
            arrayOf(sessionId)
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    fun todayBlockedAttemptCount(dayStartMillis: Long): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM blocked_attempts WHERE attempted_at >= ?",
            arrayOf(dayStartMillis.toString())
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    fun todayCompletedMinutes(dayStartMillis: Long): Int =
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(duration_minutes), 0) FROM sessions WHERE status = ? AND finished_at >= ?",
            arrayOf(SessionTimeline.STATUS_COMPLETED, dayStartMillis.toString())
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

    fun todayCompletedCount(dayStartMillis: Long): Int =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM sessions WHERE status = ? AND finished_at >= ?",
            arrayOf(SessionTimeline.STATUS_COMPLETED, dayStartMillis.toString())
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    
    companion object {
        private const val DATABASE_NAME = "wenodh_zen_history.db"
        private const val DATABASE_VERSION = 2
    }
}
