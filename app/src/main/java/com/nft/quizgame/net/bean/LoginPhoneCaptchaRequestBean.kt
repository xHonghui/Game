package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.UserRequestProperty

class LoginPhoneCaptchaRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_PATH = "/ISO1880105"
        const val TYPE_SIGN_IN = "sign_in"
        const val TYPE_SIGN_UP = "sign_up"
    }

    init {
        requestProperty = UserRequestProperty()
    }

    //机号码，不用加前缀+86
    //例如：15312334445
    @SerializedName("phone_number")
    var phoneNumber: String? = null

    //验证码用途，取值：
    //sign_in（登录）
    //sign_up（注册）
    var type: String? = null


    override fun needAccessToken():Boolean{
        return false
    }
}