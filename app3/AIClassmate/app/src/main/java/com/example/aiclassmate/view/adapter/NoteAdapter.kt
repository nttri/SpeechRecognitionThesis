package com.example.aiclassmate.view.adapter

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aiclassmate.R
import com.example.aiclassmate.data.Note
import com.example.aiclassmate.view.activity.NoteActivity
import kotlinx.android.synthetic.main.item_note_preview.view.*

class NoteAdapter(val activity: NoteActivity, val lstNote: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.Companion.NoteAdapterVH>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) = NoteAdapterVH(
        LayoutInflater.from(activity)
            .inflate(R.layout.item_note_preview, p0, false))

    override fun getItemCount() = lstNote.size

    override fun onBindViewHolder(lectureVH: NoteAdapterVH, p1: Int) {
        with(lectureVH) {
            if (p1 % 2 != 0) {
                noteItem.setCardBackgroundColor(Color.parseColor("#b3ffff"))
            }
            noteTitle.text = "Ghi chú $p1"
            noteContent.text = lstNote[p1].content

            val clickListener: View.OnClickListener?

            clickListener = View.OnClickListener {
                activity.deleteNote(p1)
            }

            noteLL.setOnClickListener(clickListener)
            noteIM.setOnClickListener(clickListener)

            noteContent.rootView.setOnClickListener {
                activity.editNote(lstNote[p1], p1)
            }
        }
    }

    companion object {
        class NoteAdapterVH(itemV: View) : RecyclerView.ViewHolder(itemV) {
            val root: View
            val noteTitle: TextView
            val noteContent: TextView
            val noteLL: View
            val noteIM: View
            val noteItem: CardView

            init {
                root = itemV
                with(itemV) {
                    noteTitle = note_title
                    noteContent = note_content
                    noteLL = ll_del_note
                    noteIM = iv_del_note
                    noteItem = cv_item_note
                }
            }
        }
    }
}