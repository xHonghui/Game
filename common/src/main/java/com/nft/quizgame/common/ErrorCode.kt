package com.nft.quizgame.common

/**
 * 错误码集合
 *
 * @author yangguanxiang
 */
class ErrorCode {
    companion object {
        const val UNKNOWN_ERROR = -1
        const val NETWORK_ERROR = 0
        const val INIT_DATA_ERROR = 1
        const val ALI_PAY_AUTH_ERROR = 2
        const val WITHDRAW_INVENTORY_SHORTAGE= 4008
        const val WITHDRAW_FREQUENCY_LIMIT = 4007

        //token过期，需要用refreshToken重新刷新
        const val ACCESS_TOKEN_EXPIRED = 3007
        //需要重新登录
        const val REFRESH_TOKEN_EXPIRED = 3008
        //验证码超出次数限制
        const val VERIFICATION_CODE_LIMIT = 3009
        //发送短信失败
        const val GET_VERIFICATION_CODE_FAIL = 3017
    }
}
//const val FACE_DETECT_ERROR = 0