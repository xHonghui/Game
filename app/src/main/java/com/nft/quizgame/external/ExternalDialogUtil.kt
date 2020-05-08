package com.nft.quizgame.external

import com.nft.quizgame.config.bean.ExternalDialogAdConfigBean
import com.nft.quizgame.external.bean.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ExternalDialogUtil {

    val mHashMap = TreeMap<Int, BaseExternalDialogBean>()
    var init = false
    val tag = "ExternalDialog"

    suspend fun init() = suspendCoroutine<TreeMap<Int, BaseExternalDialogBean>?> { continuation ->

        if (init) {
            continuation.resume(mHashMap)
        }

        GlobalScope.launch(Dispatchers.Main) {

            val async = async(Dispatchers.IO) {
                configBean()
            }

            val await = async.await()
            mHashMap.putAll(await)

            init = true
            continuation.resume(mHashMap)
        }
    }

    private fun configBean(): TreeMap<Int, BaseExternalDialogBean> {
        val map = TreeMap<Int, BaseExternalDialogBean>()
        var bean: BaseExternalDialogBean = ChallengeInviteBean()
        map.put(bean.priority, bean)
        bean = ChallengeUndoneBean()
        map.put(bean.priority, bean)
        bean = ComeAnswerQuestionsBean()
        map.put(bean.priority, bean)
        bean = ComeSoonWithdrawBean()
        map.put(bean.priority, bean)
        bean = TopicUpdateBean()
        map.put(bean.priority, bean)
        bean = MissYouBean()
        map.put(bean.priority, bean)
        bean = NewbieZoneBean()
        map.put(bean.priority, bean)
        bean = StrongestBrainBean()
        map.put(bean.priority, bean)
        bean = IqOnlineBean()
        map.put(bean.priority, bean)
        bean = RewardIncreaseBean()
        map.put(bean.priority, bean)
        bean = KnowledgeBean()
        map.put(bean.priority, bean)
        bean = DoYouKnowBean()
        map.put(bean.priority, bean)
        bean = RescueOperationsBean()
        map.put(bean.priority, bean)
        bean = TakeTheBonusBean()
        map.put(bean.priority, bean)
        bean = MillionSubsidiesBean()
        map.put(bean.priority, bean)

        return map
    }

    fun getDialogBean(id: Int): BaseExternalDialogBean? {
        for (mutableEntry in mHashMap) {
            if (mutableEntry.value.id == id) {
                return mutableEntry.value
            }
        }

        return null

    }

    fun getSatisfyDialogBean(configBean: ExternalDialogAdConfigBean): BaseExternalDialogBean? {
        for (mutableEntry in mHashMap) {
            val value = mutableEntry.value
            if (value.satisfy(configBean)) {
                return value
            }
        }

        return null

    }


}