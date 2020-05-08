package com.nft.quizgame.function.user

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.LoginPhoneCaptchaRequestBean
import com.nft.quizgame.net.bean.LoginPhoneCaptchaResponseBean

class LoginPhoneRepository {

    fun sendCaptcha(requestBean: LoginPhoneCaptchaRequestBean,
        failCallback: (errorCod: Int?, errorMsg: String?) -> Unit, successCallback: (responseBean: LoginPhoneCaptchaResponseBean) -> Unit) {
        NetManager.performSendCaptcha(requestBean,object :RequestCallback<LoginPhoneCaptchaResponseBean>{
            override fun onResponse(response: LoginPhoneCaptchaResponseBean) {
                if (response.errorCode == 0) {
                    successCallback.invoke(response)
                } else {
                    //error
                    //1	3009	超过发送限制次数
                    //2	3017	发送短信失败，可酌情重试，有可能是服务器异常导致，也有可能是短信提供商限制了单个手机号单天可发送验证码的次数导致，该手机需第二天才能正常接收验证码
                    failCallback.invoke(response.errorCode, response.errorMessage)
                }
            }

            override fun onErrorResponse(error: VolleyError) {
//                failCallback.invoke(error.networkResponse?.statusCode, error.message)
                failCallback.invoke(ErrorCode.NETWORK_ERROR, error.message)
            }

            override fun onUserExpired() {

            }

        })

    }

}