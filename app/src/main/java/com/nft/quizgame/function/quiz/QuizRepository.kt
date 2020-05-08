package com.nft.quizgame.function.quiz

import android.text.TextUtils
import android.util.SparseArray
import androidx.core.util.isEmpty
import androidx.core.util.valueIterator
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.data.AppDatabase
import com.nft.quizgame.ext.toast
import com.nft.quizgame.function.quiz.bean.*
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.*
import com.nft.quizgame.net.exception.NetError
import com.tencent.bugly.crashreport.CrashReport
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class QuizRepository {

    private val dao = AppDatabase.getInstance().quizDao()

    companion object {
        const val MAX_ID_COUNT = 500
        const val MAX_LOAD_ITEM_COUNT = 50
        const val MAX_ANSWERED_ITEM_COUNT = 1000
    }

    fun updateQuizItem(itemBean: QuizItemBean) {
        dao.updateQuizItem(itemBean)
    }

    suspend fun fetchQuizItems(accessToken: String, filterIds: List<Int>? = null, ease: List<Int>? = null,
                               tags: List<Int>? = null,
                               minCountThreshold: Int): LinkedList<QuizItemBean>? {
        var results = loadQuizItemsFromDB(filterIds, ease, tags)
        if (results.size < minCountThreshold) {
            val itemList = loadQuizItemsFromNetwork(accessToken, ease, tags)
            AppDatabase.getInstance().runInTransaction {
                itemList.forEach { bean ->
                    dao.addQuizItem(bean)
                    bean.tagList?.forEach { tag ->
                        dao.addQuizTag(tag)
                    }
                }
            }
            results = loadQuizItemsFromDB(filterIds, ease, tags)
        }
        return LinkedList(results)
    }

    private fun loadQuizItemsFromDB(filterIds: List<Int>?, ease: List<Int>?, tags: List<Int>?): List<QuizItemBean> {
        var filterIdList = filterIds
        if (filterIdList == null) {
            filterIdList = arrayListOf()
        }
        return if (ease != null && tags != null) {
            dao.loadQuizItems(filterIdList, ease, tags, MAX_LOAD_ITEM_COUNT)
        } else if (ease != null) {
            dao.loadQuizItemsByEase(filterIdList, ease, MAX_LOAD_ITEM_COUNT)
        } else if (tags != null) {
            dao.loadQuizItemsByTags(filterIdList, tags, MAX_LOAD_ITEM_COUNT)
        } else {
            dao.loadQuizItems(filterIdList, MAX_LOAD_ITEM_COUNT)
        }
    }

    private suspend fun loadQuizItemsFromNetwork(accessToken: String, ease: List<Int>? = null,
                                                 tags: List<Int>? = null) = suspendCoroutine<ArrayList<QuizItemBean>> { cont ->
        val ids: List<Int>? = if (ease != null && tags != null) {
            dao.loadQuizIds(ease, tags, MAX_ID_COUNT)
        } else if (ease != null) {
            dao.loadQuizIdsByEase(ease, MAX_ID_COUNT)
        } else if (tags != null) {
            dao.loadQuizIdsByTags(tags, MAX_ID_COUNT)
        } else {
            null
        }
        val requestBean = QuestionRequestBean().apply {
            this.accessToken = accessToken
            this.size = MAX_LOAD_ITEM_COUNT
            this.difficulty = ease
            this.tags = tags
            this.questionIds = ids
        }
        val idSet = hashSetOf<Int>()
        if (ids != null) {
            idSet.addAll(ids)
        }
        NetManager.performQuestionRequest(requestBean, object : RequestCallback<QuestionResponseBean> {

            override fun onResponse(response: QuestionResponseBean) {
                if (response.errorCode != 0) {
                    cont.resumeWithException(NetError(response.errorCode, response.errorMessage))
                    return
                }
                var isDuplicate = false
                val duplicateList = arrayListOf<Int>()
                val itemList = arrayListOf<QuizItemBean>()
                response.data?.questions?.forEach { question ->
                    if (idSet.contains(question.id)) {
                        isDuplicate = true
                        duplicateList.add(question.id)
                    }
                    val quizItemBean = QuizItemBean().apply {
                        this.id = question.id
                        this.content = question.title
                        this.answer = question.answer - 1
                        this.ease = question.difficulty
                        this.options = question.options
                        question.tags?.let {
                            val tags = it.split("^")
                            val tagList = arrayListOf<QuizTag>()
                            tags.forEach continuing@{ tag ->
                                if (TextUtils.isEmpty(tag)) {
                                    CrashReport.postCatchedException(IllegalArgumentException(
                                            "Tag cannot be empty. Info -- QuestionId: ${question.id}, tags: $it"))
                                    return@continuing
                                }
                                val quizTag = QuizTag().apply {
                                    this.quizItemId = question.id
                                    this.tag = tag.toInt()
                                }
                                tagList.add(quizTag)
                            }
                            this.tagList = tagList
                        }
                    }
                    itemList.add(quizItemBean)
                }
                if (isDuplicate) {
                    if (BuildConfig.DEBUG) {
                        val sb = StringBuilder("重复问题ID：")
                        duplicateList.forEachIndexed { index, i ->
                            if (index < duplicateList.size - 1) {
                                sb.append(i).append(", ")
                            } else {
                                sb.append(i)
                            }
                        }
                        Logcat.i("Test", sb.toString())
                        toast(QuizAppState.getContext(), "题目重复了，快去看日志")
                    }
                    CrashReport.postCatchedException(RuntimeException("Questions duplicated"))
                }
                cont.resume(itemList)
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }


    fun fetchCardProperties(userId: String, mode: QuizMode): SparseArray<CardPropertyBean> {
        val map = SparseArray<CardPropertyBean>()
        dao.loadCardProperties(userId, mode.value).forEach { bean ->
            map.put(bean.cardType, bean)
        }
        if (map.isEmpty()) {
            CardPropertyBean.initCardProperties(map, userId, mode)
            map.valueIterator().forEach { bean ->
                dao.addCardProperty(bean)
            }
        }
        return map
    }

    fun updateCardProperty(property: CardPropertyBean) {
        dao.updateCardProperty(property)
    }

    suspend fun fetchModuleConfigs(accessToken: String, now: Long): SparseArray<ModuleConfig> {
        val map = SparseArray<ModuleConfig>()
        var caches = dao.loadModuleConfigs(now, ModuleConfigCache.TIME_LIMIT)
        if (caches.isEmpty()) {
            val configs = loadModuleConfigsFromNetwork(accessToken)
            dao.removeAllModuleConfigs()
            if (configs != null && configs.isNotEmpty()) {
                configs.forEach { config ->
                    val moduleConfigCache = ModuleConfigCache().apply {
                        val gson = Gson()
                        this.moduleCode = config.moduleCode
                        this.configJson = gson.toJson(config)
                        this.updateTime = now
                    }
                    dao.addModuleConfigCache(moduleConfigCache)
                }
            }
            caches = dao.loadModuleConfigs(now, ModuleConfigCache.TIME_LIMIT)
        }
        caches.forEach { cache ->
            map.put(cache.moduleCode!!, cache.moduleConfig)
        }
        return map
    }

    private suspend fun loadModuleConfigsFromNetwork(
            accessToken: String) = suspendCoroutine<List<ModuleConfig>?> { cont ->
        val requestBean = ModuleRequestBean().apply {
            this.accessToken = accessToken
        }
        NetManager.performModuleRequest(requestBean, object : RequestCallback<ModuleResponseBean> {
            override fun onResponse(response: ModuleResponseBean) {
                val configs = response.data?.moduleConfigs
                cont.resume(configs)
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }

    suspend fun fetchRules(accessToken: String, now: Long): SparseArray<Rule> {
        val map = SparseArray<Rule>()
        var caches = dao.loadRuleCache(now, RuleCache.TIME_LIMIT)
        if (caches.isEmpty()) {
            val ruleDTO = loadRuleFromNetwork(accessToken)
            dao.removeAllRules()
            if (ruleDTO != null) {
                val gson = Gson()
                ruleDTO.newUserBonusRule?.let { bonusRule ->
                    val rules = ArrayList<Rule>()
                    if (ruleDTO.rules != null) {
                        rules.addAll(ruleDTO.rules!!)
                    }
                    rules.add(Rule().apply {
                        this.moduleCode = -1
                        this.type = RuleCache.TYPE_NEW_USER_BONUS
                        this.newUserBonusRule = bonusRule
                    })
                    ruleDTO.rules = rules
                }
                ruleDTO.rules?.forEach { rule ->
                    val ruleCache = RuleCache().apply {
                        this.moduleCode = rule.moduleCode!!
                        this.type = rule.type
                        val json = when (rule.type) {
                            RuleCache.TYPE_NEW_USER_BONUS -> gson.toJson(rule.newUserBonusRule)
                            RuleCache.TYPE_RACING -> gson.toJson(rule.racingRule)
                            RuleCache.TYPE_FREE -> gson.toJson(rule.freeRule)
                            RuleCache.TYPE_STAGE -> gson.toJson(rule.stageRule)
                            else -> ""
                        }
                        this.ruleJson = json
                        this.updateTime = now
                    }
                    dao.addRuleCache(ruleCache)
                }
            }
            caches = dao.loadRuleCache(now, RuleCache.TIME_LIMIT)
        }
        caches.forEach { cache ->
            map.put(cache.moduleCode, cache.rule)
        }
        return map
    }

    private suspend fun loadRuleFromNetwork(accessToken: String) = suspendCoroutine<RuleResponseBean.RuleDTO?> { cont ->
        val requestBean = RuleRequestBean().apply {
            this.accessToken = accessToken
        }
        NetManager.performRuleRequest(requestBean, object : RequestCallback<RuleResponseBean> {
            override fun onResponse(response: RuleResponseBean) {
                cont.resume(response.data)
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }

    fun houseKeepQuizItems() {
        val count = dao.getAnsweredQuizItemCount()
        val limit = count - MAX_ANSWERED_ITEM_COUNT
        if (limit > 0) {
            AppDatabase.getInstance().runInTransaction {
                dao.removeOutDateQuizItemRelativeTag(limit)
                dao.removeOutDateQuizItem(limit)
            }
        }
    }
}