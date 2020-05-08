package com.nft.quizgame.net.exception

class NetError(val errorCode: Int = 0, val errorMsg: String? = "") :
        Exception("errorCode: $errorCode, errorMessage: $errorMsg")