package com.nft.quizgame.function.quiz

import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import java.util.*

/**
 * 自由模式ViewModel
 */
open class FreeQuizViewModel(param: QuizViewModuleParam) :
        BaseQuizViewModel(param.userData, param.globalProperty, param.cardProperties, param.moduleConfig, param.rule) {

    protected var doubleBonusInterval = -1
    var isDoubleBonus = MutableLiveData(false)

    open fun checkDoubleBonus() {
        val min = rule.freeRule!!.doubleBonusMinQuizCount
        val max = rule.freeRule!!.doubleBonusMaxQuizCount
        isDoubleBonus.value = false
        when (doubleBonusInterval) {
            -1 -> {
                doubleBonusInterval = min + Random().nextInt(max - min + 1) - 1
                if (doubleBonusInterval < 1) {
                    doubleBonusInterval = 1
                }
            }
            0 -> {
                isDoubleBonus.value = true
                doubleBonusInterval = min + Random().nextInt(max - min + 1) - 1
                if (doubleBonusInterval < 1) {
                    doubleBonusInterval = 1
                }
            }
            else -> {
                doubleBonusInterval--
            }
        }
    }

    override fun getMode(): QuizMode {
        return QuizMode.FREE
    }

    override fun checkAndUpdateCardProperties(correctCount: Int) {
        if (correctCount % rule.freeRule!!.tipsCardInterval == 0) {
            cardProperties[getMode()]?.get(CardPropertyBean.TYPE_TIPS)?.let { property ->
                property.cardAmount++
                cardAmountChangeData.value = Event(CardPropertyBean.TYPE_TIPS)
            }
        }
    }

    override fun getCardInterval(cardType: Int): Int {
        return when (cardType) {
            CardPropertyBean.TYPE_TIPS -> rule.freeRule!!.tipsCardInterval
            else -> 0
        }
    }

    override fun onCleared() {
        cardProperties[getMode()]?.get(CardPropertyBean.TYPE_TIPS)?.let { property ->
            property.cardAmount = 0
        }
        super.onCleared()
    }
}