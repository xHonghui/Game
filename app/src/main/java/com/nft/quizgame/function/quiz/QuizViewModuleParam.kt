package com.nft.quizgame.function.quiz

import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.ModuleConfig
import com.nft.quizgame.net.bean.Rule
import java.util.*

class QuizViewModuleParam {
    lateinit var userData: MutableLiveData<UserBean>
    lateinit var globalProperty: GlobalPropertyBean
    lateinit var cardProperties: HashMap<QuizMode, SparseArray<CardPropertyBean>>
    lateinit var moduleConfig: ModuleConfig
    lateinit var rule: Rule
}