package com.example.aiclassmate.view.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.aiclassmate.R
import com.example.aiclassmate.asr.AsrProcess
import com.example.aiclassmate.asr.AsrUICallback
import com.example.aiclassmate.view.custom.Fancy
import kotlinx.android.synthetic.main.activity_lecture_edit.*
import android.text.InputType
import android.widget.EditText
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.example.aiclassmate.data.*
import org.jetbrains.anko.*


class AddLectureActivity : AppCompatActivity() {
    private val REQUEST_PERMISSION_CODE = 1000
    private val handler = Handler()
    private var isEditing = false
    private var micAllow = false
    private val titleAsrTimeout = 3000L
    private val contentAsrTimeout = -1L
    private val clickAsrLimit = 1000L
    private var counterEdt = 0
    private lateinit var repeatContent: AsrUICallback
    private var lastTimeAsrContent: Long = 0L
    private var inProcessDone = false
    private val noteList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecture_edit)

        intent?.let {
            val lectureExtra = it.getSerializableExtra(MainActivity.LECTURE_SEND)

            if (lectureExtra != null) {
                val lecture = lectureExtra as Lecture
                ed_lec_title.text.clear()
                ed_lec_title.text.append(lecture.name)
                tv_lec_title.text = lecture.name

                noteList.addAll(lecture.noteList)
                txt_note_count.text = "${noteList.size} Ghi chú"

                ed_lec_content.text.clear()
                ed_lec_content.text.append(lecture.content)
            }
        }

        ed_lec_title.setOnFocusChangeListener { _, hasFocus ->
            isEditing = hasFocus

            if (!isEditing) {
                tv_lec_title.text = ed_lec_title.text
                ed_lec_title.visibility = View.GONE
                tv_lec_title.visibility = View.VISIBLE

                handler.postDelayed({
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                        currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }, 10)
            }
        }

        fl_lec_title.setOnClickListener {
            if (!isEditing) {
                showTitleEdt()
            }
        }

        var alreadyCancelTitleAsr = false

        mic_title.setOnClickListener {
            if (micAllow) {
                AsrProcess.startRecording(this@AddLectureActivity, titleAsrTimeout,object : AsrUICallback {
                    override fun onStartAudio() {
                        alreadyCancelTitleAsr = false
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                            currentFocus?.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS
                        )
                        fr_asr_title.visibility = View.VISIBLE
                        tv_asr_flash.text = "Hãy đọc tiêu đề"
                        ll_done_rec.visibility = View.GONE
                        ll_note_root.visibility = View.GONE
                        txt_note_count.visibility = View.GONE
                        mic_title.visibility = View.INVISIBLE
                        mic_title_loader.visibility = View.VISIBLE
                        Fancy.flashText(tv_asr_flash)
                    }

                    override fun onAudioProcessing() {
                        handler.postDelayed({
                            tv_asr_flash.text = "Đang xử lý"
                        }, 10)
                    }

                    override fun onAudioComplete(text: String) {
                        if (alreadyCancelTitleAsr) return
                        handler.postDelayed({
                            ll_done_rec.visibility = View.VISIBLE
                            ll_note_root.visibility = View.VISIBLE
                            txt_note_count.visibility = View.VISIBLE
                            mic_title_loader.visibility = View.INVISIBLE
                            mic_title.visibility = View.VISIBLE
                            Fancy.stopFlashing()
                            hidePreviewTitle()
                            finishTitleEdt(text)
                        }, 100)
                    }

                    override fun onOtherAudioRecording() {
                        handler.postDelayed({
                            toast("Đang thu âm")
                        }, 100)
                    }

                    override fun onAsrError(err: String) {
                        if (alreadyCancelTitleAsr) return
                        handler.postDelayed({
                            ll_done_rec.visibility = View.VISIBLE
                            ll_note_root.visibility = View.VISIBLE
                            txt_note_count.visibility = View.VISIBLE
                            mic_title_loader.visibility = View.INVISIBLE
                            mic_title.visibility = View.VISIBLE
                            Fancy.stopFlashing()
                            hidePreviewTitle()
                            toast("Lỗi: $err")
                        }, 100)
                    }
                })
            }
            else {
                toast("Bạn cần cấp quyền mic để sử dụng tính năng")

                handler.postDelayed({
                    requestPermission()
                }, 500)
            }
        }

        btn_stop_asr_title.setOnClickListener {
            alreadyCancelTitleAsr = true
            AsrProcess.stopAndDoneProcess()
            ll_done_rec.visibility = View.VISIBLE
            ll_note_root.visibility = View.VISIBLE
            txt_note_count.visibility = View.VISIBLE
            mic_title_loader.visibility = View.INVISIBLE
            mic_title.visibility = View.VISIBLE
            Fancy.stopFlashing()
            hidePreviewTitle()
        }

        ed_lec_content.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (counterEdt < 1) {
                    // Close keyboard for first time
                    handler.postDelayed({
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                            currentFocus?.windowToken,
                            InputMethodManager.HIDE_NOT_ALWAYS
                        )
                    }, 10)
                    counterEdt++
                }
            }
            else {
                counterEdt = 0
            }
        }

        var asrContentRecording = false
        var saveActionPending = false

        btn_lec_record.setOnClickListener {
            if (System.currentTimeMillis() - lastTimeAsrContent < clickAsrLimit) {
                toast("Don't harass me")
                return@setOnClickListener
            }
            lastTimeAsrContent = System.currentTimeMillis()
            if (asrContentRecording) {
                // Close current asr
                asrContentRecording = false
                AsrProcess.stopRecordingManually()
            }
            else {
                if (micAllow) {
                    repeatContent = object : AsrUICallback {
                        override fun onStartAudio() {
                            asrContentRecording = true
                            btn_lec_record.imageResource = R.drawable.ic_close_mic
                            btn_lec_record_shadow.visibility = View.VISIBLE
                            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                                currentFocus?.windowToken,
                                InputMethodManager.HIDE_NOT_ALWAYS
                            )
                        }

                        override fun onAudioProcessing() {
                            handler.postDelayed({
                                toast("Đang xử lý")
                            }, 10)
                        }

                        override fun onAudioComplete(text: String) {
                            handler.postDelayed({
                                ed_lec_content.append(text)
                                handler.postDelayed({
                                    btn_lec_record.imageResource = R.drawable.ic_white_mic
                                    btn_lec_record_shadow.visibility = View.INVISIBLE
                                    if (saveActionPending == true) {
                                        saveAction()
                                    }
                                }, 500)
                            }, 30)
                        }

                        override fun onOtherAudioRecording() {
                            handler.postDelayed({
                                toast("Đang thu âm")
                            }, 100)
                        }

                        override fun onAsrError(err: String) {
                            handler.postDelayed({
                                asrContentRecording = false
                                toast("Lỗi: $err, không thể lưu đoạn nhận diện cuối cùng")
                                handler.postDelayed({
                                    btn_lec_record.imageResource = R.drawable.ic_white_mic
                                    btn_lec_record_shadow.visibility = View.INVISIBLE
                                    if (saveActionPending == true) {
                                        saveAction()
                                    }
                                }, 300)
                            }, 100)
                        }
                    }

                    AsrProcess.startRecording(this@AddLectureActivity, contentAsrTimeout, repeatContent)
                } else {
                    toast("Bạn cần cấp quyền mic để sử dụng tính năng")

                    handler.postDelayed({
                        requestPermission()
                    }, 500)
                }
            }
        }

        btn_done_lecture.setOnClickListener {
            if (inProcessDone) {
                toast("Đang xử lý lưu")
                return@setOnClickListener
            }

            inProcessDone = true

            if (asrContentRecording) {
                saveActionPending = true
                asrContentRecording = false
                AsrProcess.stopRecordingManually()
                toast("Xin hãy đợi bộ nhận diện kết thúc")
            }
            else {
                saveAction()
            }
        }

        ll_create_note.setOnClickListener {
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
                txt_note_count.text = "${noteList.size} Ghi chú}"
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

        ll_view_note.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra(NoteActivity.NOTE_VIEW, NoteWrapper(noteList))
            startActivityForResult(intent, NOTE_VIEW_REQ)
        }
    }

    val NOTE_VIEW_REQ = 1001

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NOTE_VIEW_REQ) {
            if (resultCode == Activity.RESULT_OK) {
                val noteWraper = data?.getSerializableExtra(NoteActivity.NOTE_VIEW) as NoteWrapper
                noteList.clear()
                noteList.addAll(noteWraper.note)
                txt_note_count.text = "${noteList.size} Ghi chú"
            }
        }
    }

    private fun saveAction() {
        inProcessDone = false
        if (ed_lec_title.text.isEmpty()) {
            toast("Xin hãy nhập tiêu đề")
        } else {
            doAsync {
                LectureDBLogic.addLecture(
                    database, Lecture(
                        name = ed_lec_title.text.toString(),
                        content = ed_lec_content.text.toString(),
                        noteList = noteList
                    )
                )
                uiThread {
                    toast("Save lecture done")
                    finish()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                micAllow = grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!checkPermissionOnDevice()) {
            requestPermission()
        }
        else {
            micAllow = true

            if (ed_lec_title.text.isEmpty()) {
                showTitleEdt()
            }
        }
    }

    private fun showTitleEdt() {
        tv_lec_title.visibility = View.GONE
        ed_lec_title.visibility = View.VISIBLE
        ed_lec_title.requestFocus()

        handler.postDelayed({
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                ed_lec_title,
                InputMethodManager.SHOW_IMPLICIT
            )
        }, 200)
    }

    private fun hidePreviewTitle() {
        fr_asr_title.visibility = View.GONE
    }

    private fun finishTitleEdt(text: String) {
        tv_lec_title.visibility = View.VISIBLE
        ed_lec_title.visibility = View.GONE
        tv_lec_title.text = text
        ed_lec_title.text.clear()
        ed_lec_title.append(text)
    }

    private fun checkPermissionOnDevice() = ContextCompat.checkSelfPermission(this,
        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_PERMISSION_CODE
        )
    }
}