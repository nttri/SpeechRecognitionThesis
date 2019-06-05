package com.example.aiclassmate.view.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.example.aiclassmate.R
import com.example.aiclassmate.data.LectureDBLogic
import com.example.aiclassmate.data.database
import com.example.aiclassmate.view.adapter.LectureAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import android.net.Uri
import android.support.v7.app.AlertDialog
import com.example.aiclassmate.data.Lecture
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object {
        val LECTURE_SEND = "lecture_send"
    }

    val REQUEST_PERMISSION_CODE = 1002
    var writeAllow = false
    var pendingListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)

        btn_talk.setOnClickListener {
            startActivity(Intent(this, AddLectureActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        writeAllow = checkPermissionOnDevice()

        doAsync {
            val allDtb = LectureDBLogic.getAllLecture(database)

            uiThread {
                if (allDtb.size > 0) {
                    lst_lecture.adapter = LectureAdapter(this@MainActivity, allDtb)
                    lst_lecture.layoutManager =
                        LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                writeAllow = grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED

                if (writeAllow) {
                    pendingListener?.onClick(null)
                }
                else {
                    toast("Xin hãy cấp quyền để lưu pdf")
                }
            }
        }
    }

    private fun checkPermissionOnDevice() = ContextCompat.checkSelfPermission(this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(pendingListener: View.OnClickListener?) {
        this.pendingListener = pendingListener
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION_CODE
        )
    }

    fun sharePdf(filePath: String) {
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val intentShareFile = Intent(Intent.ACTION_SEND)
                    val fileWithinMyDir = File(filePath)

                    if (fileWithinMyDir.exists()) {
                        intentShareFile.type = "application/pdf"
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"))

                        intentShareFile.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Sharing File..."
                        )
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")

                        startActivity(Intent.createChooser(intentShareFile, "Share File"))
                    }
                    dialog.cancel()
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.cancel()
                }
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Bạn có muốn chia sẻ tài liệu này không?").setPositiveButton("Có", dialogClickListener)
            .setNegativeButton("Không", dialogClickListener)
            .show()

    }


    fun openLecture(lecture: Lecture) {
        val intent = Intent(this, AddLectureActivity::class.java)
        intent.putExtra(LECTURE_SEND, lecture)
        startActivity(intent)
    }
}
