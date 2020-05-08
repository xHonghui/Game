package com.nft.quizgame.common.net

import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

/**
 * Created by yangjiacheng on 2017/12/27.
 *
 * 处理服务器返回为 String 的请求
 */
class StringRequest internal constructor(builder: Builder) :
        AbsRequest<String>(builder.method, builder.url, builder.headers, builder.params, builder.tag, builder.decoder,
                builder.callback) {
    override fun doParseNetworkResponse(response: NetworkResponse): Response<String> {
        var parsed: String
        try {
            parsed = String(response.data, charset(HttpHeaderParser.parseCharset(response.headers)))
            if (decoder != null) {
                parsed = decoder!!.decode(response.data)
            }
        } catch (e: Exception) {
            return Response.error(ParseError(e))
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
    }

    class Builder : SuperRequestBuilder() {
        var callback: RequestCallback<String>? = null
        fun callback(callback: RequestCallback<String>?): Builder {
            this.callback = callback
            return this
        }

        override fun build(): StringRequest {
            checkUrlNull()
            return StringRequest(this)
        }
    }
}