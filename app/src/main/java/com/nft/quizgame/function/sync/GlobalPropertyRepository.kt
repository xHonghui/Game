package com.nft.quizgame.function.sync

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.data.AppDatabase
import com.nft.quizgame.function.sync.bean.GameProgressCache
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.SyncDataDownloadRequestBean
import com.nft.quizgame.net.bean.SyncDataDownloadResponseBean
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean
import com.nft.quizgame.net.bean.SyncDataUploadResponseBean
import com.nft.quizgame.net.exception.NetError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GlobalPropertyRepository {

    private val dao = AppDatabase.getInstance().gameProgressDao()

    fun fetchGameProgresses(userId: String): List<GameProgressCache> {
        return dao.loadGameProgresses(userId)
    }

    fun fetchLastGameProgress(userId: String): GameProgressCache? {
        return dao.loadLastGameProgress(userId)
    }

    suspend fun downloadDataFromNetwork(accessToken: String,
                                        userId: String) = suspendCoroutine<GlobalPropertyBean> { cont ->
        val requestBean = SyncDataDownloadRequestBean().apply {
            this.accessToken = accessToken
        }
        NetManager.performSyncDataDownloadRequest(requestBean, object : RequestCallback<SyncDataDownloadResponseBean> {
            override fun onResponse(response: SyncDataDownloadResponseBean) {
                val bean = GlobalPropertyBean()
                bean.isNewUser = response.errorCode == 2000
                response.data?.apply {
                    bean.challengeToday = if (challengeToday == 0) GlobalPropertyBean.CHALLENGE_STATE_NONE else challengeToday
                    bean.mainModeProgress = mainModeProgress + 1
                }
                cont.resume(bean)
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }

    fun addGameProgress(cache: GameProgressCache) {
        cache.updateTime = System.currentTimeMillis()
        dao.addGameProgress(cache)
    }

    fun updateGameProgress(cache: GameProgressCache) {
        cache.updateTime = System.currentTimeMillis()
        dao.updateGameProgress(cache)
    }

    fun getLastGameProgressKey(userId: String): Int {
        return dao.getLastGameProgressKey(userId)
    }

    suspend fun uploadGameProgress(accessToken: String,
                                   gameProgresses: List<SyncDataUploadRequestBean.GameProgress>) = suspendCoroutine<Boolean> { cont ->
        val requestBean = SyncDataUploadRequestBean().apply {
            this.accessToken = accessToken
            this.gameProgressList = gameProgresses
        }
        NetManager.performSyncDataUploadRequest(requestBean, object : RequestCallback<SyncDataUploadResponseBean> {
            override fun onResponse(response: SyncDataUploadResponseBean) {
                if (response.errorCode == 0) {
                    cont.resume(true)
                } else {
                    cont.resumeWithException(NetError(response.errorCode, response.errorMessage))
                }
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {

            }
        })
    }

    fun removeGameProgress(cache: GameProgressCache) {
        dao.removeGameProgress(cache)
    }
}