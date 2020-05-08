package com.nft.quizgame.net.bean

import com.nft.quizgame.net.CoinRequestProperty

class CoinInfoRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880201"
    }
    init {
        requestProperty = CoinRequestProperty()
    }
}