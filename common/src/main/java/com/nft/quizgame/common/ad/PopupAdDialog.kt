package com.nft.quizgame.common.ad

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.nft.quizgame.common.R
import com.nft.quizgame.common.utils.WindowController
import com.nft.quizgame.ext.postDelayed
import kotlinx.android.synthetic.main.dialog_ad.*

class PopupAdDialog(context: Context, adBean: AdBean?) : Dialog(context) {

    private val mAdBean: AdBean? = adBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_ad)
        val window = window ?: return
        val params = window.attributes
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        params.width = WindowController.getScreenWidth()
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        params.gravity = Gravity.CENTER
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        initView()
    }

    private fun initView() {
        iv_close.setOnClickListener { dismiss() }

        postDelayed(5000) {
            if (!isShowing) {
                return@postDelayed
            }
            iv_close.visibility = (View.VISIBLE)
        }

        showAd()
    }

    private fun showAd() {
        if (mAdBean == null) {
            dismiss()
            return
        }
        AdController.showInternalAd(ShowInternalAdParameter(null, mAdBean, fl_ad_container))
    }

    companion object {
        var sAdWidth = popupAdWidth
    }

}