package com.nft.quizgame.function.quiz

//0.自由模式 1.闯关模式 2.竞速模式 3.主线模式
enum class QuizMode(val value: Int) {
    FREE(0),
    STAGE(1),
    RACING(2),
    MAIN(3)
}