package com.nft.quizgame.function.quiz

import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.State
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.ext.notify
import com.nft.quizgame.ext.postDelayed
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.quiz.bean.QuizItemBean
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.ModuleConfig
import com.nft.quizgame.net.bean.Rule
import com.nft.quizgame.net.exception.NetError
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

abstract class BaseQuizViewModel(protected val userData: MutableLiveData<UserBean>,
                                 protected val globalProperty: GlobalPropertyBean,
                                 protected val cardProperties: HashMap<QuizMode, SparseArray<CardPropertyBean>>,
                                 protected val moduleConfig: ModuleConfig,
                                 protected val rule: Rule) : BaseViewModel() {

    companion object {
        const val MIN_ITEMS_COUNT_THRESHOLD = 10 //当内存中剩余题目数小于或等于这个值，会向数据库加载下一批题目
    }

    val repository = QuizRepository()
    val quizItemsData = MutableLiveData<LinkedList<QuizItemBean>>()
    val currentQuizItemData = MutableLiveData<QuizItemBean>()
    var coinOfQuizItem = MutableLiveData(0)
    var correctData = MutableLiveData(0)
    var totalAnswer = 0
    var cardAmountChangeData = MutableLiveData<Event<Int>>()
    var isFetchingQuizItems = false

    abstract fun getMode(): QuizMode
    abstract fun checkAndUpdateCardProperties(correctCount: Int)
    abstract fun getCardInterval(cardType:Int):Int

    suspend fun fetchQuizItems(isInit: Boolean, isSilence: Boolean, filterIds: List<Int>? = null,
                               ease: List<Int>? = null, tags: List<Int>? = null) {
        if (isFetchingQuizItems) {
            if (!isSilence) {
                stateData.value = Event(State.Loading()) //触发展示进度对话框
            }
            return
        }
        isFetchingQuizItems = true
        if (!isSilence) {
            stateData.value = Event(State.Loading())
        }
        try {
            val items = withContext(IO) {
                if (isInit) {
                    delay(400) //这里睡300毫秒多留cpu时间给主线程做初始化，减轻动画卡顿感
                }
                repository.fetchQuizItems(userData.value!!.accessToken, filterIds, ease, tags,
                        if (isInit) MIN_ITEMS_COUNT_THRESHOLD else 1)
            }
            isFetchingQuizItems = false
            if (quizItemsData.value == null) {
                quizItemsData.value = items
            } else {
                quizItemsData.value?.addAll(items!!)
                quizItemsData.notify()
            }
            stateData.value = Event(State.Success())
        } catch (e: Exception) {
            isFetchingQuizItems = false
            when (e) {
                is NetError -> {
                    stateData.value = Event(State.Error(e.errorCode))
                }
                is VolleyError -> {
                    stateData.value = Event(State.Error(ErrorCode.NETWORK_ERROR))
                }
                else -> {
                    stateData.value = Event(State.Error(ErrorCode.UNKNOWN_ERROR))
                }
            }
        }
    }

    fun markQuizItemAnswered(isCorrect: Boolean, isForceShowNextItem: Boolean = false) {
        totalAnswer++
        launch {
            currentQuizItemData.value?.let {
                it.isCorrect = isCorrect
                it.answerTime = System.currentTimeMillis()
                withContext(IO) {
                    repository.updateQuizItem(it)
                }
                when {
                    isCorrect -> {
                        correctData.value = correctData.value!! + 1
                        checkAndUpdateCardProperties(correctData.value!!)
                        showNextItem()
                    }
                    isForceShowNextItem -> {
                        showNextItem()
                    }
                }
            }
        }
    }

    fun showCurrentItem() {
        quizItemsData.value?.let { itemList ->
            if (currentQuizItemData.value != itemList[0]) {
                currentQuizItemData.value = itemList[0]
            }
        }
    }

    open fun showNextItem() {
        val size = quizItemsData.value?.size ?: 0
        if (size <= MIN_ITEMS_COUNT_THRESHOLD) {
            if (size <= 1) {
                if (quizItemsData.value?.isNotEmpty() == true) {
                    quizItemsData.value?.removeFirst()
                }
            } else {
                doShowNextItem()
            }
            fetchQuizItems(size > 1)
        } else {
            doShowNextItem()
        }
    }

    fun fetchQuizItems(isSilence: Boolean) {
        if (!isFetchingQuizItems) {
            launch {
                val filterIds = quizItemsData.value?.map { bean -> bean.id }
                fetchQuizItems(isInit = false, isSilence = isSilence, filterIds = filterIds,
                        ease = moduleConfig.easeList,
                        tags = moduleConfig.tagList)
            }
        }
    }

    private fun doShowNextItem() {
        stateData.value = Event(State.BlockTouchEvent())
        var item: QuizItemBean? = null
        quizItemsData.value?.let { itemList ->
            itemList.removeFirst()
            item = itemList[0]
        }
        postDelayed(500) {
            currentQuizItemData.value = item
            stateData.value = Event(State.UnblockTouchEvent())
        }
    }

    fun needInitQuizData() = quizItemsData.value == null || quizItemsData.value!!.isEmpty()

    fun handleCoinOfQuizItem(rule: Rule) {
        when (rule.type) {
            QuizMode.FREE.value -> {
                rule.freeRule?.let {
                    coinOfQuizItem.value = it.obtainRandomCoin()
                }
            }
            QuizMode.MAIN.value, QuizMode.STAGE.value -> {
                rule.stageRule?.let {
                    coinOfQuizItem.value = it.obtainRandomCoin()
                }
            }
            QuizMode.RACING.value -> {
                rule.racingRule?.let {
                    coinOfQuizItem.value = it.obtainRandomCoin()
                }
            }
        }
    }
}