package com.nft.quizgame.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.base.services.version.Version
import com.nft.quizgame.R
import kotlinx.android.synthetic.main.quiz_dialog.*

class QuizUpdateDialog(activity: Activity, val version: Version, adModuleId: Int = -1) : QuizDialog<QuizUpdateDialog>(activity, adModuleId) {

    private var confirmButtonCallback: (() -> Unit)? = null
    private var cancelButtonCallback: (() -> Unit)? = null
    private lateinit var txtTitle: TextView
    private lateinit var txtUpdate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logo(R.mipmap.dialog_logo_new_version_found)
        confirmButton(R.string.upgrade) {
            confirmButtonCallback?.invoke()
            dismiss()
        }
        cancelButton(R.string.ignore_upgrade) {
            cancelButtonCallback?.invoke()
            dismiss()
        }
        content_layout.setOnClickListener { }
        dialog_container.setOnClickListener { dismiss() }
        customView(view = LayoutInflater.from(activity).inflate(R.layout.update_desc_layout, null))
        txtTitle = findViewById(R.id.txt_title)
        txtUpdate = findViewById(R.id.txt_update_log)
        txtTitle.text = version.detail
        txtUpdate.text = version.updateLog
    }

    override fun showPriority(): Int {
        return 2
    }

    fun setConfirmButtonCallback(callback: (() -> Unit)) {
        this.confirmButtonCallback = callback
    }

    fun setCancelButtonCallback(callback: () -> Unit) {
        this.cancelButtonCallback = callback
    }

}