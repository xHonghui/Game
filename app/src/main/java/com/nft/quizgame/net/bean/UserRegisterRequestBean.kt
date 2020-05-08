package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.UserRequestProperty

class UserRegisterRequestBean : BaseRequestBean() {

    companion object {
        const val REQUEST_PATH = "/ISO1880101"
    }

    init {
        requestProperty = UserRequestProperty()
    }

    //账号类型：wechat alipay visitor（备注：游客） phone
    @SerializedName("account_type")
    var accountType: String? = null

    //账号标识ID，游客登录时传设备ID，第三方登录时，第三方的用户标识
    @SerializedName("identity_id")
    var identityId: String? = null

    //	用户买量类型
    @SerializedName("user_chanel")
    var userChanel: String? = null

    //	验证码，手机登录时不能为空
    @SerializedName("verification_code")
    var verificationCode: String? = null


    //第三方应用的授权码，第三方账号登录时不能为空
    @SerializedName("auth_code")
    var authCode: String? = null

    override fun needAccessToken(): Boolean {
        return false
    }
}