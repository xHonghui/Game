package com.nft.quizgame.net.bean

class AliPayAuthInfoResponseBean:BaseResponseBean() {

    var data: SignDTO? = null

    class SignDTO {
        var sign: String = ""
    }

}