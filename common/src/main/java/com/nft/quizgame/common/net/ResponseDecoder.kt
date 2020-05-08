package com.nft.quizgame.common.net

interface ResponseDecoder {
    @Throws(Exception::class)
    fun decode(data: ByteArray): String
}