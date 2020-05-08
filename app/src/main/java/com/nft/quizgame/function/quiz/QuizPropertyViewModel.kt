package com.nft.quizgame.function.quiz

import android.app.AlarmManager
import android.util.SparseArray
import androidx.core.util.keyIterator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.State
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.quiz.bean.EntranceBean
import com.nft.quizgame.function.quiz.bean.defaults.*
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.ModuleConfig
import com.nft.quizgame.net.bean.Rule
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class QuizPropertyViewModel(protected val userData: LiveData<UserBean>) : BaseViewModel() {

    private val repository = QuizRepository()

    var cardProperties: HashMap<QuizMode, SparseArray<CardPropertyBean>>? = null
    var entranceMap = SparseArray<EntranceBean>()
    var moduleConfigs: SparseArray<ModuleConfig>? = null
    private var rules: SparseArray<Rule>? = null
    private var updateTime = 0L
    var isDataInitialized = false
    var isFetchingData = false
    val propertyDataState = MutableLiveData<Event<State>>()

    private fun clearData() {
        cardProperties?.clear()
        cardProperties = null
        moduleConfigs?.clear()
        moduleConfigs = null
        rules?.clear()
        rules = null
        entranceMap.clear()
    }

    suspend fun initPropertyData(forceInitialize: Boolean) {
        if (forceInitialize) {
            isDataInitialized = false
            clearData()
        }
        if (isDataInitialized) {
            return
        }
        val cardDeferred = async(IO) {
            val map = HashMap<QuizMode, SparseArray<CardPropertyBean>>()
            var cardProperties = repository.fetchCardProperties(userData.value!!.userId, QuizMode.FREE)
            map[QuizMode.FREE] = cardProperties
            cardProperties = repository.fetchCardProperties(userData.value!!.userId, QuizMode.MAIN)
            map[QuizMode.MAIN] = cardProperties
            cardProperties = repository.fetchCardProperties(userData.value!!.userId, QuizMode.RACING)
            map[QuizMode.RACING] = cardProperties
            map
        }
        cardProperties = cardDeferred.await()
        fetchPropertyData()
        houseKeepQuizItems()
        isDataInitialized = true
    }

    private fun initEntrances() {
        var entranceId = 0
        moduleConfigs?.keyIterator()?.forEach { key ->
            entranceId++
            val entranceBean = EntranceBean().apply {
                this.entranceId = entranceId
                this.moduleCode = key
            }
            entranceMap.put(entranceBean.entranceId, entranceBean)
        }
    }

    /**
     * 获取全选标签的自由模式的入口ID
     */
    fun getFreeEntranceId(): Int = 1

    /**
     * 获取全选标签的闯关模式的入口ID
     */
    fun getStageEntranceId(): Int = 2

    /**
     * 获取全选标签的竞速模式的入口ID
     */
    fun getRaceEntranceId(): Int = 3

    fun getEntrance(entranceId: Int): EntranceBean? = entranceMap.get(entranceId)

    fun getModuleConfig(moduleCode: Int): ModuleConfig {
        var config = moduleConfigs?.get(moduleCode)
        if (config == null) {
            config = getDefaultConfig(moduleCode)
        }
        return config
    }

    private fun getDefaultConfig(moduleCode: Int): ModuleConfig {
        return when (moduleCode) {
            DefaultModuleConfig.MODULE_CODE_1 -> DefaultModuleConfig.DefaultModuleConfig1()
            DefaultModuleConfig.MODULE_CODE_2 -> DefaultModuleConfig.DefaultModuleConfig2()
            DefaultModuleConfig.MODULE_CODE_3 -> DefaultModuleConfig.DefaultModuleConfig3()
            DefaultModuleConfig.MODULE_CODE_4 -> DefaultModuleConfig.DefaultModuleConfig4()
            DefaultModuleConfig.MODULE_CODE_5 -> DefaultModuleConfig.DefaultModuleConfig5()
            DefaultModuleConfig.MODULE_CODE_6 -> DefaultModuleConfig.DefaultModuleConfig6()
            DefaultModuleConfig.MODULE_CODE_7 -> DefaultModuleConfig.DefaultModuleConfig7()
            DefaultModuleConfig.MODULE_CODE_8 -> DefaultModuleConfig.DefaultModuleConfig8()
            else -> DefaultModuleConfig.DefaultModuleConfig1()
        }
    }

    fun getRule(moduleCode: Int): Rule {
        var rule = rules?.get(moduleCode)
        if (rule == null) {
            rule = getDefaultRule(moduleCode)
        }
        return rule
    }

    private fun getDefaultRule(moduleCode: Int): Rule {
        return when (moduleCode) {
            DefaultModuleConfig.MODULE_CODE_1 -> DefaultFreeRule()
            DefaultModuleConfig.MODULE_CODE_2 -> DefaultStageRule()
            DefaultModuleConfig.MODULE_CODE_3 -> DefaultRacingRule()
            DefaultModuleConfig.MODULE_CODE_4 -> DefaultFreeRule()
            DefaultModuleConfig.MODULE_CODE_5 -> DefaultFreeRule()
            DefaultModuleConfig.MODULE_CODE_6 -> DefaultFreeRule()
            DefaultModuleConfig.MODULE_CODE_7 -> DefaultFreeRule()
            DefaultModuleConfig.MODULE_CODE_8 -> DefaultFreeRule()
            -1 -> DefaultNewUserBonusRule()
            else -> DefaultFreeRule()
        }
    }

    fun getCard(cardType: Int, mode: QuizMode): CardPropertyBean {
        return cardProperties!![mode]!!.get(cardType)!!
    }

    fun updateCard(cardPropertyBean: CardPropertyBean) {
        launch(IO) {
            repository.updateCardProperty(cardPropertyBean)
        }
    }

    private fun isDataOutDate(): Boolean = updateTime > 0 && System.currentTimeMillis() - updateTime > 8 * AlarmManager.INTERVAL_HOUR

    private suspend fun fetchPropertyData() {
        if (isFetchingData) {
            return
        }
        isFetchingData = true
        val now = System.currentTimeMillis()
        val moduleConfigDeferred = async(IO) {
            repository.fetchModuleConfigs(userData.value!!.accessToken, now)
        }
        val ruleDeferred = async(IO) {
            repository.fetchRules(userData.value!!.accessToken, now)
        }
        try {
            rules = ruleDeferred.await()
            moduleConfigs = moduleConfigDeferred.await()
            initEntrances()
            updateTime = now
            propertyDataState.value = Event(State.Success())
        } finally {
            isFetchingData = false
        }
    }

    fun checkAndReloadQuizPropertyData() {
        launch {
            try {
                if (isDataOutDate() && !isFetchingData) {
                    propertyDataState.value = Event(State.Loading())
                    moduleConfigs?.clear()
                    moduleConfigs = null
                    rules?.clear()
                    rules = null
                    entranceMap.clear()
                    fetchPropertyData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CrashReport.postCatchedException(e)
                propertyDataState.value = Event(State.Error(ErrorCode.INIT_DATA_ERROR))
            }
        }
    }

    private suspend fun houseKeepQuizItems() {
        withContext(IO) {
            try {
                repository.houseKeepQuizItems()
            } catch (e: Exception) {
                e.printStackTrace()
                CrashReport.postCatchedException(e)
            }
        }
    }
}