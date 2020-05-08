package com.nft.quizgame.function.user

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.State
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.net.bean.LoginPhoneCaptchaRequestBean
import com.nft.quizgame.statistic.Statistic103

class LoginPhoneViewModel : BaseViewModel() {

    val tag = this.javaClass.name
    //倒计时
    val countDownTimerData = MutableLiveData<Long>()
    val getVerificationCodeTimeInterval = 60 * 1000L
    var countDownTimer: CountDownTimer? = null


    val repository = LoginPhoneRepository()

    init {
        configCountDownTimer()
    }


    private fun configCountDownTimer() {
        countDownTimer?.cancel()

        //获取最后一次的时间
        val l = System.currentTimeMillis() - PrivatePreference.getPreference().getValue(
            PrefConst.KEY_LAST_GET_VERIFICATION_CODE_TIME,
            0L
        )
        if (l > getVerificationCodeTimeInterval) {
            countDownTimerData.value = 0
        } else {
            countDownTimerData.value = getVerificationCodeTimeInterval - l

            countDownTimer = object : CountDownTimer(getVerificationCodeTimeInterval - l, 1000L) {
                override fun onFinish() {
                    countDownTimerData.value = 0
                }

                override fun onTick(p0: Long) {
                    countDownTimerData.value = p0
                }

            }

            //倒计时
            countDownTimer?.start()
        }

    }

    fun sendVerificationCode(phoneNumber: String) {
        //获取验证码接口
        stateData.value = Event(State.Loading())

        val loginPhoneCaptchaRequestBean = LoginPhoneCaptchaRequestBean()
        loginPhoneCaptchaRequestBean.phoneNumber =phoneNumber
        loginPhoneCaptchaRequestBean.type = LoginPhoneCaptchaRequestBean.TYPE_SIGN_IN
        repository.sendCaptcha(loginPhoneCaptchaRequestBean,{errorCode, errorMsg ->
            Statistic103.uploadCodeRequest(2)
            Logcat.d(tag, "onErrorResponse")
            stateData.value = Event(State.Error(errorCode ?: 0, errorMsg ?: ""))
        },{
            Statistic103.uploadCodeRequest(1)
            PrivatePreference.getPreference()
                    .putValue(PrefConst.KEY_LAST_GET_VERIFICATION_CODE_TIME, System.currentTimeMillis()).apply()
            configCountDownTimer()
            stateData.value = Event(State.Success())
        })


    }


}