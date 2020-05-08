package com.nft.quizgame.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.view.View
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizLoadAdParameter
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.post
import com.nft.quizgame.view.CoinAnimationLayer
import kotlinx.android.synthetic.main.quiz_dialog.*
import kotlinx.android.synthetic.main.quiz_dialog_default_container.*

class QuizEnvelopeDialog(activity: Activity, adModuleId: Int, rewardModuleId: Int,entrance: String, coinAnimEndLoc: IntArray,
                         coinAnimObserver: Observer<Float>, private val moduleCode:Int, private var envelopeCount: Int,
                         private val envelopeCoin: Int, openEnvelopeCallback: (Int) -> Unit) :
        QuizRewardDialog(activity, adModuleId, rewardModuleId,entrance) {

    private var canOpenEnvelope = false

    init {
        coinAnimationHelper = CoinAnimationDialogHelper(this, coinAnimEndLoc, coinAnimObserver, openEnvelopeCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logo(R.mipmap.dialog_logo_rtc_envelopes)
        title(text = activity.getString(R.string.envelope_dialog_title, envelopeCount)
                .getStyleSpanString(envelopeCount.toString(),
                color = Color.parseColor("#35A2FF"),
                style = Typeface.BOLD, size = mNumberTextSize,
                flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
        desc(R.string.envelope_dialog_desc)
        rewardButton(R.string.open_envelope, onClickCallback = {
            BaseSeq103OperationStatistic.uploadData(
                    optionCode = BaseSeq103OperationStatistic.ENVELOPE_BONUS_POPUP_CLICK,
                    obj = moduleCode.toString(), entrance = "1")
        }, rewardGainedCallback = {
//            canOpenEnvelope = true
        }, adCloseCallback = { _, _ ->
//            if (canOpenEnvelope) {
//                canOpenEnvelope = false
                openEnvelope()
//            }
        })
        cancelButton(R.string.go_on) { dismiss() }
        BaseSeq103OperationStatistic.uploadData(optionCode = BaseSeq103OperationStatistic.ENVELOPE_BONUS_POPUP_SHOW,
                obj = moduleCode.toString(), entrance = "1")
    }

    private fun openEnvelope() {
        envelopeCount--
        logo(R.mipmap.dialog_logo_congratulations)
        title(text = activity.getString(R.string.open_envelope_success, envelopeCoin)
                .getStyleSpanString(envelopeCoin.toString(),
                color = Color.parseColor("#35A2FF"),
                style = Typeface.BOLD, size = mNumberTextSize,
                flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
        txt_desc.visibility = View.GONE
        if (envelopeCount > 0) {
            rewardButton(R.string.open_envelope_again, onClickCallback = {
                BaseSeq103OperationStatistic.uploadData(
                        optionCode = BaseSeq103OperationStatistic.ENVELOPE_BONUS_POPUP_CLICK,
                        obj = moduleCode.toString(), entrance = "2")
            }, rewardGainedCallback = {
//                canOpenEnvelope = true
            }, adCloseCallback = { _, _ ->
//                if (canOpenEnvelope) {
//                    canOpenEnvelope = false
                    openEnvelope()
//                }
            })
            AdController.loadAd(QuizLoadAdParameter(activity, rewardModuleId).apply {  entrance = mAdEntrance })
            BaseSeq103OperationStatistic.uploadData(
                    optionCode = BaseSeq103OperationStatistic.ENVELOPE_BONUS_POPUP_SHOW,
                    obj = moduleCode.toString(), entrance = "2")
        } else {
            sl_cancel.visibility = View.GONE
            sl_ok.visibility = View.VISIBLE
            sl_ok.clearAnimation()
            btn_ok.setText(R.string.go_on)
            btn_ok.setOnClickListener {
                dismiss()
            }
        }
        post {
            coinAnimationHelper!!.startCoinAnimation(envelopeCoin, CoinAnimationLayer.DEFAULT_COIN_ANIMATION_COUNT)
        }
    }
}