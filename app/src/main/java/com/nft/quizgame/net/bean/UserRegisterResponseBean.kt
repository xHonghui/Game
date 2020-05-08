package com.nft.quizgame.net.bean

class UserRegisterResponseBean :BaseResponseBean(){

    var data: UserRegisterDTO? = null

    class UserRegisterDTO {
        var token: Token? = null
    }

}