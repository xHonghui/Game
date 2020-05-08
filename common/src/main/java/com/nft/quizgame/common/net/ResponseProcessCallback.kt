package com.nft.quizgame.common.net

interface ResponseProcessCallback<T> {

    fun process(data: T):Boolean

}