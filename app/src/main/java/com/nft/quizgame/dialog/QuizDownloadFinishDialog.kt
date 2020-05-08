package com.nft.quizgame.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.base.services.version.Version
import com.nft.quizgame.R

class QuizDownloadFinishDialog(activity: Activity,val version: Version, adModuleId: Int = -1) : QuizDialog<QuizUpdateDialog>(activity, adModuleId) {

    private var confirmCallback: (() -> Unit)? = null
    private var versionUpdateTxt:String = ""
    private lateinit var txtFinish: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        logo(R.mipmap.dialog_logo_new_version_install)
        val customView = LayoutInflater.from(activity).inflate(R.layout.download_finish_layout, null)
        customView(view = customView)
        confirmButton(R.string.install_app) {
            confirmCallback?.invoke()
            dismiss()
        }
        txtFinish = customView.findViewById(R.id.txt_download_finish)
        txtFinish.text = "V${version.versionName}${activity.getString(R.string.apk_download_finish)}"
    }

    fun setConfirmCallback(callback: (() -> Unit)) {
        confirmCallback = callback
    }

}