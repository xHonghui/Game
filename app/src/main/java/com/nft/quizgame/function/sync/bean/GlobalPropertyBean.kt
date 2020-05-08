package com.nft.quizgame.function.sync.bean

class GlobalPropertyBean {

    companion object {
        const val CHALLENGE_STATE_SUCCESS = 1
        const val CHALLENGE_STATE_FAIL = 2
        const val CHALLENGE_STATE_NONE = 3
    }

    var isNewUser = false
    var challengeToday: Int = CHALLENGE_STATE_NONE
    var mainModeProgress: Int = 0
    var currentGameProgress: GameProgressCache? = null
    var updateTime = 0L
    var userId:String? = null
}