package com.nft.quizgame.function.quiz

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.ext.notify

/**
 * 竞速模式ViewModel
 */
class RacingQuizViewModel(param: QuizViewModuleParam) :
        BaseQuizViewModel(param.userData, param.globalProperty, param.cardProperties, param.moduleConfig, param.rule) {

    var duration = 120L
    val timeProgress = MutableLiveData<TimeProgress>()
    var targetData = MutableLiveData(0)
    var challengeStateData = MutableLiveData<Boolean>() //true：成功，false：失败

    private var timer: RacingTimer? = null
    var isPaused = false

    override fun getMode(): QuizMode {
        return QuizMode.RACING
    }

    override fun checkAndUpdateCardProperties(correctCount: Int) {
    }

    override fun getCardInterval(cardType: Int): Int {
        return 0
    }

    fun startTime() {
        targetData.value = rule.racingRule?.target!!
        correctData.value = 0
        duration = rule.racingRule?.limitTime?.toLong()!!
        val value = TimeProgress().apply { time = duration }
        timeProgress.value = value
        timer?.cancel()
        timer = RacingTimer(duration * 1000L, 500).apply {
            start()
        }
    }

    fun pause() {
        isPaused = true
        cancelTime()
    }

    fun resume() {
        isPaused = false
        if (timer == null && timeProgress.value?.time ?: 0 > 0) {
            timer = RacingTimer(timeProgress.value!!.time * 1000L, 500).apply {
                start()
            }
        }
    }

    fun cancelTime() {
        timer?.cancel()
        timer = null
    }

    fun isTiming() = !isPaused && timer != null && timeProgress.value!!.time > 0

    override fun onCleared() {
        cancelTime()
        super.onCleared()
    }

    override fun showNextItem() {
        if (targetData.value!! > 0 && correctData.value!! > 0 && correctData.value!! >= targetData.value!!) {
            if (challengeStateData.value != true) { //这里做一下容错处理，避免弹出两次挑战成功对话框
                challengeStateData.value = true
            }
            return
        }
        super.showNextItem()
    }

    inner class RacingTimer(millisInFuture: Long, countDownInterval: Long) :
            CountDownTimer(millisInFuture, countDownInterval) {

        override fun onTick(millisUntilFinished: Long) {
            timeProgress.value?.apply {
                time = millisUntilFinished / 1000 + 1
                progress = (100f * time / duration).toInt()
            }
            timeProgress.notify()
        }

        override fun onFinish() {
            timeProgress.value?.apply {
                time = 0
                progress = 0
            }
            timeProgress.notify()
        }
    }

    class TimeProgress {
        var time = 0L
        var progress = 0
    }
}