package com.example.aiclassmate.view.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.aiclassmate.R
import com.example.aiclassmate.data.Note
import com.example.aiclassmate.data.NoteWrapper
import com.example.aiclassmate.view.adapter.NoteAdapter
import kotlinx.android.synthetic.main.activity_lecture_edit.*
import kotlinx.android.synthetic.main.activity_main.main_toolbar
import kotlinx.android.synthetic.main.activity_view_note.*

class NoteActivity : AppCompatActivity() {
    companion object {
        val NOTE_VIEW = "note_view"
    }

    var pendingListener: View.OnClickListener? = null
    val handler = Handler()
    val noteList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_view_note)

        setSupportActionBar(main_toolbar)
        this.supportActionBar?.title = "Ghi chú"


        intent?.let {
            noteList.addAll((it.getSerializableExtra(NOTE_VIEW) as NoteWrapper).note)
        }

        lst_note.adapter = NoteAdapter(this@NoteActivity, noteList)
        lst_note.layoutManager =
            LinearLayoutManager(this@NoteActivity, LinearLayoutManager.VERTICAL, false)

        btn_add_note.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Thêm ghi chú")

            // Set up the input
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("Thêm") { dialog, _ ->
                if (input.text.isEmpty()) {
                    dialog.cancel()
                    return@setPositiveButton
                }
                noteList.add(Note(content = input.text.toString()))
                lst_note.adapter?.notifyItemInserted(noteList.lastIndex)
                dialog.cancel()
            }

            builder.setNegativeButton("Bỏ qua") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()

            handler.postDelayed({
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                    input,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }, 50)
        }
    }

    fun editNote(note: Note, pos: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chỉnh sửa")

        // Set up the input
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.text.append(note.content)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Đồng ý") { dialog, _ ->
            note.content = input.text.toString()
            lst_note.adapter?.notifyItemChanged(pos)
            dialog.cancel()
        }

        builder.setNegativeButton("Bỏ qua") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()

        handler.postDelayed({
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                input,
                InputMethodManager.SHOW_IMPLICIT
            )
        }, 50)
    }

    fun deleteNote(p1: Int) {
        noteList.removeAt(p1)
        lst_note.adapter?.notifyItemRemoved(p1)
    }

    override fun onBackPressed() {
        val resultInt = Intent()
        resultInt.putExtra(NOTE_VIEW, NoteWrapper(noteList))
        setResult(Activity.RESULT_OK, resultInt)
        finish()
    }
}