package com.nft.quizgame.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.nft.quizgame.R
import com.nft.quizgame.common.view.CircleProgressBar

class QuizDownloadingDialog(activity: Activity, adModuleId: Int = -1) : QuizDialog<QuizUpdateDialog>(activity, adModuleId) {

    private var progress: Int = 0
    private var closeCallback: (() -> Unit)? = null
    private lateinit var progressBar: CircleProgressBar
    private lateinit var txtHide:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val progressView = LayoutInflater.from(activity).inflate(R.layout.progressing_view, null)
        customView(view = progressView)
        progressBar = progressView.findViewById(R.id.progress_bar)
        progressBar.setProgress(progress)
        txtHide = progressView.findViewById(R.id.txt_hide)
        txtHide.setOnClickListener {
            closeCallback?.invoke()
            dismiss()
        }
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        if (::progressBar.isInitialized) {
            progressBar.setProgress(this.progress)
        }
    }

    fun downloadError(){

    }

    fun setCloseCallback(callback: () -> Unit) {
        this.closeCallback = callback
    }

}