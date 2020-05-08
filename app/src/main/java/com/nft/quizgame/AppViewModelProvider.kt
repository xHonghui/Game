package com.nft.quizgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.sync.GlobalPropertyViewModel
import com.nft.quizgame.function.user.UserViewModel

class AppViewModelProvider(factory: Factory) : ViewModelProvider(ViewModelStore(), factory) {
    companion object {
        fun getInstance(): AppViewModelProvider = Holder.instance
    }

    class Holder {
        companion object {
            val instance = AppViewModelProvider(AppViewModelFactory())
        }
    }

    class AppViewModelFactory : Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when (modelClass) {
                GlobalPropertyViewModel::class.java -> {
                    val userModel = getInstance().get(UserViewModel::class.java)
                    GlobalPropertyViewModel(userModel.userData) as T
                }
                QuizPropertyViewModel::class.java -> {
                    val userModel = getInstance().get(UserViewModel::class.java)
                    QuizPropertyViewModel(userModel.userData) as T
                }
                CoinOptViewModel::class.java -> {
                    val userModel = getInstance().get(UserViewModel::class.java)
                    CoinOptViewModel(userModel.userData) as T
                }
                else ->
                    modelClass.newInstance()
            }
        }
    }
}