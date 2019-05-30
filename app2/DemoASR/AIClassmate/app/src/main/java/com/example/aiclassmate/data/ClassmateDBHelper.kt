package com.example.aiclassmate.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.aiclassmate.*
import org.jetbrains.anko.db.*

class ClassmateDBHelper private constructor(ctx: Context) : ManagedSQLiteOpenHelper(ctx, DB_NAME,
    null, 1) {
    init {
        instance = this
    }

    companion object {
        private var instance: ClassmateDBHelper? = null

        @Synchronized
        fun getInstance(ctx: Context) = instance ?: ClassmateDBHelper(ctx.applicationContext)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Here you create tables
        db.createTable(
            LECTURE_TABLE, true,
            LECTURE_ID to INTEGER + PRIMARY_KEY + UNIQUE,
            LECTURE_NAME to TEXT,
            LECTURE_CONTENT to BLOB)

        db.createTable(
            NOTE_TABLE, true,
            NOTE_ID to INTEGER + PRIMARY_KEY + UNIQUE,
            NOTE_CONTENT to TEXT,
            NOTE_LEC_ID to INTEGER
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}

// Access property for Context
val Context.database: ClassmateDBHelper
    get() = ClassmateDBHelper.getInstance(this)