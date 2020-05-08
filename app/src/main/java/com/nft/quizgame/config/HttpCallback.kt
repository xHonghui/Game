package com.nft.quizgame.config

import com.nft.quizgame.config.bean.AbsConfigBean

interface HttpCallback {
    fun success(configBean: AbsConfigBean)
    fun error()
}
