package com.example.aiclassmate.view.adapter

import android.os.Environment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aiclassmate.R
import com.example.aiclassmate.data.Lecture
import com.example.aiclassmate.view.activity.MainActivity
import com.uttampanchasara.pdfgenerator.CreatePdf
import kotlinx.android.synthetic.main.item_lecture_preview.view.*
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat

class LectureAdapter(val activity: MainActivity, val lstLecture: List<Lecture>) :
    RecyclerView.Adapter<LectureAdapter.Companion.LectureAdapterVH>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) = LectureAdapterVH(LayoutInflater.from(activity)
        .inflate(R.layout.item_lecture_preview, p0, false))

    override fun getItemCount() = lstLecture.size

    override fun onBindViewHolder(lectureVH: LectureAdapterVH, p1: Int) {
        with(lectureVH) {
            lectureName.text = lstLecture[p1].name
            lectureContent.text = lstLecture[p1].content
            lectureNots.text = "${lstLecture[p1].noteList.size} ghi chú"

            var lectureProcessing = false

            var clickListener : View.OnClickListener? = null
            clickListener = View.OnClickListener {
                if (activity.writeAllow) {
                    if (lectureProcessing) {
                        activity.toast("Đang tạo pdf")
                        return@OnClickListener
                    }
                    lectureProcessing = true
                    CreatePdf(activity)
                        .setPdfName(buildFilePDFName(lstLecture[p1].name))
                        .openPrintDialog(false)
                        .setContentBaseUrl(null)
                        .setContent(buildContent(lstLecture[p1]))
                        .setFilePath(Environment.getExternalStorageDirectory().absolutePath + "/AIClassmate")
                        .setCallbackListener(object : CreatePdf.PdfCallbackListener {
                            override fun onFailure(errorMsg: String) {
                                lectureProcessing = false
                                activity.toast(errorMsg)
                            }

                            override fun onSuccess(filePath: String) {
                                lectureProcessing = false
                                activity.toast("Pdf lưu tại: $filePath")
                                activity.sharePdf(filePath)
                            }
                        })
                        .create()
                }
                else {
                    activity.requestPermission(clickListener)
                }
            }

            lectureLL.setOnClickListener(clickListener)
            lectureTV.setOnClickListener(clickListener)
            lectureIM.setOnClickListener(clickListener)

            lectureContent.rootView.setOnClickListener {
                activity.openLecture(lstLecture[p1])
            }
        }
    }

    fun buildContent(lecture: Lecture) = buildString {
        append("<h1>").append(lecture.name.toUpperCase()).append("</h1><br/><br/>")
        append(lecture.content)
        if (lecture.noteList.isNotEmpty()) {
            append("<br/><br/><br/><h2>Ghi chú:</h2>")
            lecture.noteList.forEach {
                append("- ${it.content}<br/>")
            }
        }
    }

    fun buildFilePDFName(lectureTitle: String) = buildString {
        val lstToken = lectureTitle.split(' ')
        append("Bai_")
        for (token in lstToken) {
            append(token.get(0).toUpperCase())
        }
        append("_")
        val sdf = SimpleDateFormat("dd_MM_yyyy")
        append(sdf.format(System.currentTimeMillis()))
    }

    companion object {
        class LectureAdapterVH(itemV: View) : RecyclerView.ViewHolder(itemV) {
            val lectureName: TextView
            val lectureContent: TextView
            val lectureNots: TextView
            val lectureLL: View
            val lectureTV: View
            val lectureIM: View

            init {
                with(itemV) {
                    lectureName = lecture_title
                    lectureContent = lecture_content
                    lectureNots = lecture_note_count
                    lectureLL = ll_export_pdf
                    lectureTV = tv_export_pdf
                    lectureIM = iv_export_pdf
                }
            }
        }
    }
}