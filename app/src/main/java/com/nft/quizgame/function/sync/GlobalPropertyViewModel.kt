package com.nft.quizgame.function.sync

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.utils.TimeUtils
import com.nft.quizgame.function.sync.bean.GameProgressCache
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean
import com.nft.quizgame.net.exception.NetError
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class GlobalPropertyViewModel(private val userData: MutableLiveData<UserBean>) : BaseViewModel() {

    private val repository = GlobalPropertyRepository()

    var globalProperty = MutableLiveData<GlobalPropertyBean>()
    private var backupProperty: GlobalPropertyBean? = null

    private var progressContext = newSingleThreadContext("game_progress_thread")

    private var isDataInitialized = false
    private var isFetchingData = false

    suspend fun initPropertyData(forceInitialize: Boolean) {
        if (forceInitialize) {
            isDataInitialized = false
            globalProperty.value = null
        }
        if (isDataInitialized) {
            return
        }
        fetchPropertyData()
        isDataInitialized = true
    }

    suspend fun fetchPropertyData() {
        if (isFetchingData) {
            return
        }
        isFetchingData = true
        backupProperty = globalProperty.value
        uploadGameProgresses()
        val deferred = async(progressContext) {
            val bean = repository.downloadDataFromNetwork(userData.value!!.accessToken, userData.value!!.userId)
            val gameProgressCache = repository.fetchLastGameProgress(userData.value!!.userId)?.apply {
                val gson = Gson()
                gameProgress = gson.fromJson(value, SyncDataUploadRequestBean.GameProgress::class.java)
            }
            bean.currentGameProgress = gameProgressCache
            bean.updateTime = System.currentTimeMillis()
            bean.userId = userData.value!!.userId
            bean
        }
        try {
            globalProperty.value = deferred.await()
        } finally {
            isFetchingData = false
        }
    }

//    fun isDataOutDate(): Boolean = if (globalProperty.value == null) false else System.currentTimeMillis() - globalProperty.value!!.updateTime > 8 * AlarmManager.INTERVAL_HOUR

    fun isDataOutDate(): Boolean = if (globalProperty.value == null) false else !TimeUtils.isSameDay(System.currentTimeMillis(), globalProperty.value!!.updateTime)

    fun restoreBackupPropertyData() {
        if (backupProperty != null && backupProperty?.userId == userData.value?.userId) {
            globalProperty.value = backupProperty
        }
        backupProperty = null
    }

    fun getCurrentStage(): Int {
        return globalProperty.value?.mainModeProgress ?: 1
    }

    fun updateStage(stage: Int) {
        globalProperty.value?.let {
            it.mainModeProgress = stage
        }
    }

    fun getChallengeState(): Int {
        return globalProperty.value?.challengeToday ?: GlobalPropertyBean.CHALLENGE_STATE_NONE
    }

    fun updateChallengeState(state: Int) {
        globalProperty.value?.let {
            it.challengeToday = state
        }
    }

    fun getCurrentGameProgress(): SyncDataUploadRequestBean.GameProgress {
        var gameProgressCache = globalProperty.value?.currentGameProgress
        if (gameProgressCache == null) {
            gameProgressCache = GameProgressCache().also { cache ->
                cache.key = repository.getLastGameProgressKey(userData.value!!.userId) + 1
                cache.userId = userData.value!!.userId
                cache.gameProgress = SyncDataUploadRequestBean.GameProgress()
            }
            globalProperty.value?.currentGameProgress = gameProgressCache
        }
        return gameProgressCache.gameProgress!!
    }

    fun commitCurrentGameProgress(progress: SyncDataUploadRequestBean.GameProgress) {
        globalProperty.value?.currentGameProgress?.let { cache ->
            cache.gameProgress = progress
            launch(progressContext) {
                cache.value = Gson().toJson(cache.gameProgress)
                cache.updateTime = System.currentTimeMillis()
                repository.addGameProgress(cache)
            }
        }
    }

    suspend fun uploadGameProgresses() {
        globalProperty.value?.currentGameProgress = null
        withContext(progressContext) {
            val caches = repository.fetchGameProgresses(userData.value!!.userId).apply {
                val gson = Gson()
                this.forEach {
                    it.gameProgress = gson.fromJson(it.value, SyncDataUploadRequestBean.GameProgress::class.java)
                }
            }
            val progressList = caches.mapNotNull {
                it.gameProgress
            }
            if (progressList.isNotEmpty()) {
                try {
                    val success = repository.uploadGameProgress(userData.value!!.accessToken, progressList)
                    if (success) {
                        caches.forEach {
                            repository.removeGameProgress(it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is NetError) {
                        CrashReport.postCatchedException(e)
                    }
                }
            }
        }
    }
}