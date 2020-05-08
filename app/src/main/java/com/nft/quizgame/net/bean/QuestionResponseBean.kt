package com.nft.quizgame.net.bean

class QuestionResponseBean : BaseResponseBean() {

    var data: QuestionDTO? = null

    class QuestionDTO {
        var questions: List<Question>? = null
        var total: Long = 0L
    }
}