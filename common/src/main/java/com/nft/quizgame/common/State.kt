package com.nft.quizgame.common

sealed class State(val errorCode: Int? = null, val message: String? = null, val event: Any? = null) {
    class Success(event: Any? = null) : State(event = event)
    class Loading(event: Any? = null) : State(event = event)
    class Error(errorCode: Int, message: String? = null, event: Any? = null) : State(errorCode, message, event)
    class BlockTouchEvent(event: Any? = null) : State(event = event)
    class UnblockTouchEvent(event: Any? = null) : State(event = event)
    //token刷新接口才需要该判断，其他不需要判断
    class Request(event: Any? = null) : State(event = event)
}