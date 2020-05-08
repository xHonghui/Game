package com.nft.quizgame.dialog

import android.app.Activity
import android.os.Bundle
import android.view.Window
import com.nft.quizgame.R
import com.nft.quizgame.common.dialog.BaseDialog
import kotlinx.android.synthetic.main.signin_dialog.*

/**
 * 签到 Activity
 * */
class SignInDialog(activity: Activity) : BaseDialog<SignInDialog>(activity) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.signin_dialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tv1.text = "SignInDialog"
    }

    override fun showPriority(): Int {
        return 3
    }

    override fun isFullScreenTransparent() = true

    override fun dismiss(invokeSuper: Boolean) {
        super.dismiss()
    }
}