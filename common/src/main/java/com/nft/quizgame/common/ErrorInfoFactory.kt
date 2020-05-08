package com.nft.quizgame.common

object ErrorInfoFactory {

    fun getErrorInfo(errorCode: Int?): ErrorInfo {
        val errorInfo = ErrorInfo()
        errorInfo.imageId = R.mipmap.dialog_logo_problem
        when (errorCode) {
            ErrorCode.NETWORK_ERROR -> {
                errorInfo.titleId = R.string.network_error_title
                errorInfo.descId = R.string.network_error_desc
            }
            ErrorCode.INIT_DATA_ERROR->{
                errorInfo.titleId = R.string.data_init_error_title
                errorInfo.descId = R.string.data_init_error_desc
            }
            ErrorCode.WITHDRAW_FREQUENCY_LIMIT->{
                errorInfo.titleId = R.string.withdraw_fail
                errorInfo.descId = R.string.withdraw_frequency_limit
            }
            ErrorCode.WITHDRAW_INVENTORY_SHORTAGE->{
                errorInfo.titleId = R.string.withdraw_fail
                errorInfo.descId = R.string.withdraw_inventory_shortage
            }
            else -> {
                errorInfo.titleId = R.string.unknown_error_title
                errorInfo.descId = R.string.unknown_error_desc
            }
        }
        return errorInfo
    }



    class ErrorInfo {
        var imageId = 0
        var titleId = 0
        var descId = 0
    }
}

