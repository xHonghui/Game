package com.nft.quizgame.external

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nft.quizgame.R
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.external.bean.BaseExternalDialogBean
import com.nft.quizgame.statistic.Statistic103
import kotlinx.android.synthetic.main.activity_external_dialog.*

class ExternalDialogActivity : AppCompatActivity() {

    companion object {
        private const val KEY_BEAN = "key_bean"
        private const val KEY_STATISTIC_PERIOD = "key_statistic_period"

        fun getIntent(context: Context, dialogBeanId: Int, statisticsPeriod: Int): Intent {
            val intent = Intent(context, ExternalDialogActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(KEY_BEAN, dialogBeanId)
            intent.putExtra(KEY_STATISTIC_PERIOD, statisticsPeriod)
            return intent
        }
    }

    lateinit var dialogBean: BaseExternalDialogBean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_external_dialog)
        content.setOnClickListener {
            if (cl_more_content.visibility == View.VISIBLE) {
                cl_more_content.visibility = View.GONE
                iv_more.isSelected = false
                return@setOnClickListener
            }
        }

        val intExtra = intent.getIntExtra(KEY_BEAN, -1)
        val bean = ExternalDialogUtil.getDialogBean(intExtra)
        if(bean == null){
            Logcat.e(ExternalDialogUtil.tag,"bean == null, id = $intExtra")
            Logcat.e(ExternalDialogUtil.tag,"data = ${ExternalDialogUtil.mHashMap}")
            finish()
            return
        }


        dialogBean = bean


        tv_title.text = dialogBean.getTitle()
        tv_des.text = dialogBean.getContent()
        tv_btn.text = dialogBean.getBtnText()

        tv_btn.setOnClickListener {
            dialogBean.jump(this)
            finish()
        }

        iv_more.setOnClickListener {
            if (cl_more_content.visibility == View.VISIBLE) {
                cl_more_content.visibility = View.GONE
            } else {
                cl_more_content.visibility = View.VISIBLE
                Statistic103.uploadExternalpopupSet(dialogBean.id, dialogBean.statisticPeriod)
            }

            iv_more.isSelected = cl_more_content.visibility == View.VISIBLE
        }

        tv_close_remind_today.setOnClickListener {
            Statistic103.uploadExternalpopupClose(dialogBean.id, 1, dialogBean.statisticPeriod)

            val preference = PrivatePreference.getPreference()
            preference.putValue(PrefConst.KEY_CLOSE_REMIND_TODAY, System.currentTimeMillis())
            preference.apply()
            finish()
        }

        tv_close_remind_recent.setOnClickListener {
            Statistic103.uploadExternalpopupClose(dialogBean.id, 2, dialogBean.statisticPeriod)
            val preference = PrivatePreference.getPreference()
            preference.putValue(PrefConst.KEY_CLOSE_REMIND_RECENT, System.currentTimeMillis())
            preference.apply()
            finish()
        }
        dialogBean.statisticPeriod = intent.getIntExtra(KEY_STATISTIC_PERIOD, -1)
        dialogBean.onShow()

//        dialogBean.onShow()
//        dialogBean.jump()


    }

    override fun onBackPressed() {
        if (cl_more_content.visibility == View.VISIBLE) {
            cl_more_content.visibility = View.GONE
            iv_more.isSelected = false
            return
        }

        super.onBackPressed()
    }
}
