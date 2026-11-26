package com.wenodh.zen.focus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Small local history database; no network or account is required. */
internal class FocusSessionHistoryDb(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE sessions (
                id TEXT PRIMARY KEY NOT NULL,
                started_at INTEGER NOT NULL,
                ends_at INTEGER NOT NULL,
                duration_minutes INTEGER NOT NULL,
                status TEXT NOT NULL,
                finished_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX index_sessions_started_at ON sessions(started_at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No released schema yet. Future changes must use explicit migrations.
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

    fun todayCompletedMinutes(dayStartMillis: Long): Int {
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(duration_minutes), 0) FROM sessions WHERE status = ? AND started_at >= ?",
            arrayOf(SessionTimeline.STATUS_COMPLETED, dayStartMillis.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun todayCompletedCount(dayStartMillis: Long): Int {
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM sessions WHERE status = ? AND started_at >= ?",
            arrayOf(SessionTimeline.STATUS_COMPLETED, dayStartMillis.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    companion object {
        private const val DATABASE_NAME = "wenodh_zen_history.db"
        private const val DATABASE_VERSION = 1
    }
}
