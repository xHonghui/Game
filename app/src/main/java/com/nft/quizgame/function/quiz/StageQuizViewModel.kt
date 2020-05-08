package com.nft.quizgame.function.quiz

import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

/**
 * 闯关模式ViewModel
 */
class StageQuizViewModel(param: QuizViewModuleParam) : FreeQuizViewModel(param) {

    private var canAddingStage = false
    private var firstHandleEnvelope = true

    val currentStageData = MutableLiveData(0) //当前关卡数
    val leftToTargetData = MutableLiveData(0) //离红包奖励的剩余关卡数

    override fun getMode(): QuizMode {
        return QuizMode.MAIN
    }

    override fun checkDoubleBonus() {
        val min = rule.stageRule!!.doubleBonusMinQuizCount
        val max = rule.stageRule!!.doubleBonusMaxQuizCount
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

    override fun checkAndUpdateCardProperties(correctCount: Int) {
        if (correctCount % rule.stageRule!!.tipsCardInterval == 0) {
            cardProperties[getMode()]?.get(CardPropertyBean.TYPE_TIPS)?.let { property ->
                property.cardAmount++
                cardAmountChangeData.value = Event(CardPropertyBean.TYPE_TIPS)
                launch(IO) {
                    repository.updateCardProperty(property)
                }
            }
        }
        if (correctCount % rule.stageRule!!.changeCardInterval == 0) {
            cardProperties[getMode()]?.get(CardPropertyBean.TYPE_CHANGE)?.let { property ->
                property.cardAmount++
                cardAmountChangeData.value = Event(CardPropertyBean.TYPE_CHANGE)
                launch(IO) {
                    repository.updateCardProperty(property)
                }
            }
        }
    }

    override fun getCardInterval(cardType: Int): Int {
        return when (cardType) {
            CardPropertyBean.TYPE_TIPS -> rule.stageRule!!.tipsCardInterval
            CardPropertyBean.TYPE_CHANGE -> rule.stageRule!!.changeCardInterval
            else -> 0
        }
    }

    fun handleStage() {
        if (canAddingStage) {
            if (quizItemsData.value != null) {
                globalProperty.mainModeProgress++
                currentStageData.value = globalProperty.mainModeProgress
            }
        } else {
            canAddingStage = true
        }
        if (globalProperty.mainModeProgress == 0) {
            globalProperty.mainModeProgress = 1
            currentStageData.value = globalProperty.mainModeProgress
        }
    }

    /**
     * 返回是否增加一个红包
     */
    fun handleEnvelope(): Boolean {
        val target = rule.stageRule?.envelopeInterval!!
        extractLeftToTarget(target)
        if (leftToTargetData.value == target && !firstHandleEnvelope) {
            cardProperties[getMode()]?.get(CardPropertyBean.TYPE_ENVELOPE)?.let { property ->
                property.cardAmount++
                cardAmountChangeData.value = Event(CardPropertyBean.TYPE_ENVELOPE)
                launch(IO) {
                    repository.updateCardProperty(property)
                }
                return true
            }
        }
        firstHandleEnvelope = false
        return false
    }

    /**
     * 获取剩余多少关能够获得红包
     */
    fun extractLeftToTarget(target: Int) {
        val currentStage = globalProperty.mainModeProgress
        currentStageData.value = currentStage
        leftToTargetData.value = target - (currentStage - 1) % target
    }
}