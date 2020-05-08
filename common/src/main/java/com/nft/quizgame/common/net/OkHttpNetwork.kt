package com.nft.quizgame.common.net

import com.android.volley.*
import com.android.volley.Request.Method.*
import com.nft.quizgame.common.utils.Logcat
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*

/**
 * Created by yangjiacheng on 2017/11/16.
 * ...
 * 为 Volley 创建的 OkHttp3 网络请求 Client，并规避过时API，不使用 HttpUrlConnection
 */
class OkHttpNetwork internal constructor() : Network {

    @Throws(VolleyError::class)
    override fun performRequest(request: com.android.volley.Request<*>): NetworkResponse {
        val builder = Request.Builder()
        val headers = request.headers
        for (name in headers.keys) {
            builder.addHeader(name, headers[name])
        }
        builder.url(request.url)
        setRequestMethod(builder, request)
        builder.tag(request.tag)
        val okHttpRequest = builder.build()
        var okHttpResponse: Response? = null
        try {
            okHttpResponse = VolleyManager.getInstance().defaultOkHttpClient.newCall(okHttpRequest).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var response: NetworkResponse? = null
        if (okHttpResponse != null) {
            try {
                var bytes: ByteArray? = null
                if (okHttpResponse.body() != null) {
                    bytes = okHttpResponse.body()!!.bytes()
                    debugLogResponse(bytes)
                }
                val headerArrayList = ArrayList<Header>()
                for (i in 0 until okHttpResponse.headers().toMultimap().size) {
                    val header = Header(okHttpResponse.headers().name(i),
                            okHttpResponse.headers().value(i))
                    headerArrayList.add(header)
                }
                response = NetworkResponse(okHttpResponse.code(), bytes,
                        okHttpResponse.code() == 304,
                        okHttpResponse.receivedResponseAtMillis() -
                                okHttpResponse.sentRequestAtMillis(), headerArrayList)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (response == null) {
            response = NetworkResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, null, false, 0,
                    null)
        }
        return response
    }

    private fun debugLogResponse(response: ByteArray?) {
        if (response != null) {
            Logcat.d(VolleyManager.LOG_TAG + " Response", String(response))
        }
    }

    companion object {
        @Throws(AuthFailureError::class)
        private fun setRequestMethod(builder: Request.Builder,
                                     request: com.android.volley.Request<*>) {
            when (request.method) {
                DEPRECATED_GET_OR_POST -> {
                    val postBody = request.body
                    if (postBody != null) {
                        builder.post(RequestBody
                                .create(MediaType.parse(request.bodyContentType), postBody))
                    }
                }
                GET -> builder.get()
                DELETE -> builder.delete()
                POST -> builder.post(createRequestBody(request))
                PUT -> builder.put(createRequestBody(request))
                HEAD -> builder.head()
                OPTIONS -> builder.method("OPTIONS", null)
                TRACE -> builder.method("TRACE", null)
                PATCH -> builder.patch(createRequestBody(request))
                else -> throw IllegalStateException("Unknown method type.")
            }
        }

        @Throws(AuthFailureError::class)
        private fun createRequestBody(r: com.android.volley.Request<*>): RequestBody? {
            val body = r.body ?: return null
            return RequestBody.create(MediaType.parse(r.bodyContentType), body)
        }
    }
}