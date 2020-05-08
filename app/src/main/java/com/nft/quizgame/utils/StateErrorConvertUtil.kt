package com.nft.quizgame.utils

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.State
import com.nft.quizgame.net.exception.NetError
import java.lang.Exception

object StateErrorConvertUtil {

    fun convertError(e: Exception): State.Error {
        return when (e) {
            is NetError -> {
                State.Error(e.errorCode, e.errorMsg)
            }
            is VolleyError -> {
//                State.Error(e.networkResponse?.statusCode ?: -1, e.message)
                State.Error(ErrorCode.NETWORK_ERROR, e.message)
            }
            else -> {
                State.Error(ErrorCode.NETWORK_ERROR, e.message)
            }
        }

    }

}