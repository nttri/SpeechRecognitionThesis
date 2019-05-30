package com.example.aiclassmate.data

import java.io.Serializable

data class Lecture(var id: Int = 0, val name: String, val content: String,
                   var noteList: List<Note> = arrayListOf()) : Serializable

data class Note(val id: Int = 0, val content: String) : Serializable