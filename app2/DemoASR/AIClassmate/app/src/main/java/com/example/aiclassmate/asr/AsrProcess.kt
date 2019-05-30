package com.example.aiclassmate.asr

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

interface AsrUICallback {
    fun onStartAudio()
    fun onAudioProcessing()
    fun onAudioComplete(text: String)
    fun onOtherAudioRecording()
    fun onAsrError(err: String)
}

object AsrProcess {
    private val TIME_OUT_RECORD = 5000L
    private lateinit var recorder: AudioRecord
    private lateinit var recordingThread: Thread
    private var isRecording = false
    private var isProcessing = false
    private var forceStop = false
    private val RECORDER_SAMPLERATE = 16000
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private val BufferElements2Rec = 1024
    private val BytesPerElement = 2 // 2 bytes = 16 bits
    private lateinit var fileDir: String
    private var appendByteArray: MutableList<Byte> = mutableListOf()
    private lateinit var currentAsrCallback: AsrUICallback
    private var timeout = 0L

    public fun startRecording(c: Context, timeout: Long, asrCallback: AsrUICallback) {
        if (isProcessing) {
            asrCallback.onOtherAudioRecording()
            return
        }

        this.timeout = timeout
        currentAsrCallback = asrCallback
        currentAsrCallback.onStartAudio()
        isRecording = true
        isProcessing = true
        forceStop = false


        fileDir = c.filesDir.absolutePath

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            RECORDER_SAMPLERATE, RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement
        )

        recorder.startRecording()
        recordingThread = Thread(Runnable { sendAudioToServer() }, "AudioRecorder Thread")
        recordingThread.start()
    }

    private fun short2byte(sData: ShortArray): ByteArray {
        val shortArrsize = sData.size
        val bytes = ByteArray(shortArrsize * 2)
        for (i in 0 until shortArrsize) {
            bytes[i * 2] = (sData[i].toInt() and 0x00FF).toByte()
            bytes[i * 2 + 1] = (sData[i].toInt() shr 8).toByte()
            sData[i] = 0
        }
        return bytes

    }

    private fun sendAudioToServer() {
        val sData = ShortArray(BufferElements2Rec)
        val startTime = System.currentTimeMillis()
        val pcmFilePath = fileDir + File.separator + "tmpfile.pcm"
        val wavFilePath = fileDir + File.separator + "tmpfile.wav"

        try {
            FileOutputStream(pcmFilePath).use {
                while (isRecording) {
                    recorder.read(sData, 0, BufferElements2Rec)
                    try {
                        val bData = short2byte(sData)
                        it.write(bData, 0, BufferElements2Rec * BytesPerElement)
                        if (timeout != -1L) {
                            if (System.currentTimeMillis() - startTime > timeout) {
                                stopRecordingManually()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        currentAsrCallback.onAsrError(e.message ?: "Stupid error")
                    }
                }
            }

            if (forceStop) return

            currentAsrCallback.onAudioProcessing()
            AudioConverter.PCMToWAV(File(pcmFilePath), File(wavFilePath), 1, RECORDER_SAMPLERATE, 16)

            // Send data here
            Log.e("XX", "Send data")
            postRequest(File(wavFilePath))
        }
        catch (e : Exception) {
            e.printStackTrace()
            currentAsrCallback.onAsrError(e.message ?: "Stupid error")
        }
    }

    private fun postRequest(file: File) {
        val client = OkHttpClient()
        val url = "https://vnstt001.herokuapp.com/file"

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("voice", file.name, RequestBody.create(MediaType.parse("audio/wav"), file))
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                isProcessing = false
                currentAsrCallback.onAsrError(e.message ?: "Stupid error")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val mMessage = response.body()!!.string()
                isProcessing = false
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(mMessage)
                        val text = json.getString("text")
                        currentAsrCallback.onAudioComplete(text)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        currentAsrCallback.onAsrError(e.message ?: "Stupid error")
                    }
                }
            }
        })
    }

    fun stopRecordingManually() {
        if (isRecording) {
            isRecording = false
            recorder.stop()
            recorder.release()
        }
    }

    fun stopAndDoneProcess() {
        forceStop = true
        isProcessing = false
        stopRecordingManually()
    }
}