package com.nft.quizgame.common.net

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.NoCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by yangjiacheng on 2017/11/16.
 * ...
 */
class VolleyManager private constructor() {

    private val requestQueue: RequestQueue
    val defaultOkHttpClient: OkHttpClient

    init {
        defaultOkHttpClient = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(obtainOkHttpCache())
                .addInterceptor(LogInterceptor())
                .build()
        //使用 OkHttp 缓存，Volley 不缓存
        requestQueue = RequestQueue(NoCache(), OkHttpNetwork())
    }

    fun <T> add(request: Request<T>?): Request<T> {
        return requestQueue.add(request)
    }

    fun start() {
        requestQueue.start()
    }

    fun clearNetworkCache() {
        requestQueue.cache.clear()
    }

    fun cancelPendingRequests(tag: Any) {
        requestQueue.cancelAll(tag)
        for (call in defaultOkHttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag() == tag) call.cancel()
        }
        for (call in defaultOkHttpClient.dispatcher().runningCalls()) {
            if (call.request().tag() == tag) call.cancel()
        }
    }

    private fun obtainOkHttpCache(): Cache {
        val cacheSize = 20 * 1024 * 1024
        val cacheFile = File(sContext.externalCacheDir, "NetworkCache")
        return Cache(cacheFile, cacheSize.toLong())
    }

    companion object {
        const val LOG_TAG = "Network Log"
        const val DEFAULT_REQUEST_TAG = "VolleyManager"
        private lateinit var sContext: Context

        fun initContext(context: Context) {
            sContext = context
        }

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = VolleyManager()
    }
}