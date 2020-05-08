package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.QuizRequestProperty

class QuestionRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880501"
    }
    init {
        requestProperty = QuizRequestProperty()
    }
    var size: Int = 0
    var difficulty: List<Int>? = null
    var tags: List<Int>? = null
    @SerializedName("question_ids")
    var questionIds: List<Int>? = null
}