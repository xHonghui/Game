package com.nft.quizgame.function.quiz.view

import androidx.databinding.ObservableInt

class OptionState(val answer:Int = 0, var content: String, state: Int = STATE_NORMAL) {
    companion object {
        const val STATE_NORMAL = 0
        const val STATE_PRESSED_CORRECT = 1
        const val STATE_PRESSED_INCORRECT = 2
        const val STATE_TIPS_CORRECT = 3
        const val STATE_TIPS_INCORRECT = 4
    }

    var state: ObservableInt = ObservableInt(STATE_NORMAL)

    init {
        this.state.set(state)
    }
}