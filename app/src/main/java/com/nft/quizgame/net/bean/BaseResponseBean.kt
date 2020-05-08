package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

abstract class BaseResponseBean {
    /**
     * 1    error_code  int 错误码，error_code=0为成功，其余代码参考统一错误代码说明
    2	error_message	string	错误信息，当error_code > 0时此字段才有内容
     */
    @SerializedName("error_code")
    var errorCode: Int = 0
    @SerializedName("error_message")
    var errorMessage: String? = null
}