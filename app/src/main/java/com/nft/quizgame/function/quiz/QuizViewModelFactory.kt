package com.nft.quizgame.function.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizViewModelFactory(private val param: QuizViewModuleParam) :
        ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(QuizViewModuleParam::class.java).newInstance(param)
    }
}