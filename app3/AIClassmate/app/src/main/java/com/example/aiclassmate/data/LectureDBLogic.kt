package com.example.aiclassmate.data

import android.database.DatabaseUtils
import com.example.aiclassmate.*
import org.jetbrains.anko.db.*

object LectureDBLogic {
    val LECTURE_INSERT_ERR = -1L
    val NOTE_INSERT_ERR = -2L

    val lectureParser = rowParser { id: Int, name: String, content: String ->
        Lecture(id, name, content)
    }

    val noteParser = rowParser { id: Int, content: String ->
        Note(id, content)
    }

    fun countLecture(db: ClassmateDBHelper) = db.use { return@use DatabaseUtils.queryNumEntries(this, LECTURE_TABLE) }

    fun addLecture(db: ClassmateDBHelper, lecture: Lecture): Long {
        with(lecture) {
            var result = db.use { insert(LECTURE_TABLE, LECTURE_NAME to name,
                    LECTURE_CONTENT to content) }

            if (result == LECTURE_INSERT_ERR) return LECTURE_INSERT_ERR
            this.id = result.toInt()
            db.use {
                lecture.noteList.forEach {
                    result = this.insert(NOTE_TABLE, NOTE_CONTENT to it.content,
                        NOTE_LEC_ID to id)
                }
            }

            // Return other error so that we understand note insert fail and not lecture
            if (result == NOTE_INSERT_ERR) return NOTE_INSERT_ERR
            return result
        }
    }

    fun getAllLecture(db: ClassmateDBHelper): List<Lecture> {
        var result: List<Lecture> = listOf()

        db.use {
            result = select(LECTURE_TABLE, LECTURE_ID, LECTURE_NAME, LECTURE_CONTENT)
                .parseList(lectureParser)

            result.forEach {lecture ->
                val noteLst = select(NOTE_TABLE, NOTE_ID, NOTE_CONTENT).whereArgs(
                    "$NOTE_LEC_ID = {lectureId}", "lectureId" to lecture.id
                ).parseList(noteParser)
                lecture.noteList = noteLst
            }
        }

        return result
    }
}