package com.nft.quizgame.common.net

import com.nft.quizgame.common.utils.Logcat
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.*

/**
 * Created by yangjiacheng on 2017/11/20.
 * ...
 * OkHTTP 网络请求 日志格式化打印
 */
class LogInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        Logcat.d(VolleyManager.LOG_TAG + " header",
                String.format(Locale.getDefault(), "Sending request %s on %s%n%s", request.url(), chain.connection(),
                        request.headers()))
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        Logcat.d(VolleyManager.LOG_TAG + " header",
                String.format(Locale.getDefault(), "Received response for %s in %.1fms%n%s", response.request().url(),
                        (t2 - t1) / 1e6, response.headers()))
        return response
    }
}