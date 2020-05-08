package com.nft.quizgame.function.sync

import android.os.SystemClock
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.QUIZ_DATA
import com.nft.quizgame.function.quiz.QuizMode
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameProgressRecorder {

    private val globalPropertyViewModel: GlobalPropertyViewModel by lazy {
        AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
    }

    var answerTotal = 0
    var answerCorrectTotal = 0
    var answerStartTime = 0L
    var optionSelectTimes = SyncDataUploadRequestBean.OptionSelectTimes()
    var coinOptDetail: SyncDataUploadRequestBean.CoinOptDetail? = null
    var mainModeRecord = SyncDataUploadRequestBean.MainModeRecord()
    var freeModeRecord = SyncDataUploadRequestBean.FreeModeRecord()
    var raceModeRecord = SyncDataUploadRequestBean.RaceModeRecord()
    var lastRecordTime = 0L
    var onlineStartTime = 0L
    var totalEarnCoin = 0

    private var lastAnswerTime = 0L
    private val questionStateList = arrayListOf<QuestionState>()

    fun onGameStarted() {
        answerStartTime = System.currentTimeMillis()
        lastAnswerTime = answerStartTime
    }

    fun onQuestionAnswer(id: Int, isCorrect: Boolean) {
        val now = System.currentTimeMillis()
        val questionState = QuestionState().apply {
            this.id = id
            this.state = if (isCorrect) 1 else 2
            this.consumingTime = now - lastAnswerTime
        }
        questionStateList.add(questionState)
        lastAnswerTime = now
    }

    fun uploadQuestionStateStatistics() {
        if (questionStateList.isNotEmpty()) {
            val stateList = arrayListOf<QuestionState>().apply { addAll(questionStateList) }
            questionStateList.clear()
            GlobalScope.launch {
                val sb = StringBuilder()
                stateList.forEach {
                    sb.append(it.id).append(";").append(it.state).append(";").append(it.consumingTime).append("#")
                }
                sb.delete(sb.lastIndexOf("#"), sb.length)
                val stateValue = sb.toString()
                BaseSeq103OperationStatistic.uploadData(optionCode = QUIZ_DATA, obj = stateValue)
            }
        }
    }

    private fun resetGameProgressRecord() {
        answerTotal = 0
        answerCorrectTotal = 0
        optionSelectTimes = SyncDataUploadRequestBean.OptionSelectTimes()
        coinOptDetail = null
        mainModeRecord = SyncDataUploadRequestBean.MainModeRecord()
        freeModeRecord = SyncDataUploadRequestBean.FreeModeRecord()
        raceModeRecord = SyncDataUploadRequestBean.RaceModeRecord()
    }

    fun uploadGameProgress(uploadType: Int, moduleCode: Int, mode: QuizMode, coin: Int) {
        GlobalScope.launch(Main) {
            val now = System.currentTimeMillis()
            val gameProgress = withContext(IO) {
                globalPropertyViewModel.getCurrentGameProgress()
            }
            gameProgress.uploadType = uploadType
            if (onlineStartTime > 0) {
                gameProgress.onlineTime = (SystemClock.elapsedRealtime() - onlineStartTime).toInt()
            }
            onlineStartTime = SystemClock.elapsedRealtime()
            gameProgress.moduleCode = moduleCode
            gameProgress.gameMode = mode.value
            gameProgress.answerTotal = answerTotal
            gameProgress.answerCorrectTotal = answerCorrectTotal
            gameProgress.answerStartTime = answerStartTime
            gameProgress.recordStartTime = if (lastRecordTime > 0) lastRecordTime else answerStartTime
            gameProgress.optionSelectTimes = optionSelectTimes
            gameProgress.recordEndTime = now
            gameProgress.coinBalance = coin
            if (coinOptDetail != null) {
                gameProgress.coinOptDetails = arrayListOf(coinOptDetail!!)
            } else {
                gameProgress.coinOptDetails = arrayListOf()
            }
            when (mode) {
                QuizMode.FREE -> gameProgress.freeModeRecord = freeModeRecord
                QuizMode.MAIN -> gameProgress.mainModeRecord = mainModeRecord
                QuizMode.RACING -> gameProgress.raceModeRecord = raceModeRecord
                else -> {
                }
            }
            resetGameProgressRecord()
            globalPropertyViewModel.commitCurrentGameProgress(gameProgress)
            globalPropertyViewModel.uploadGameProgresses()
            lastRecordTime = now
        }
    }

    class QuestionState {
        var id: Int = 0
        var state: Int = 0
        var consumingTime: Long = 0L
    }
}


