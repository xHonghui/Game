package com.nft.quizgame.common.net

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

/**
 * Created by yangjiacheng on 2017/12/9.
 * 直接从请求中读出响应 bytes
 */
class BytesRequest internal constructor(builder: Builder) :
        AbsRequest<ByteArray>(builder.method, builder.url, builder.headers, builder.params, builder.tag,
                builder.decoder, builder.callback) {

    override fun doParseNetworkResponse(response: NetworkResponse): Response<ByteArray> {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))
    }

    class Builder internal constructor() : SuperRequestBuilder() {
        var callback: RequestCallback<ByteArray>? = null
        fun callback(callback: RequestCallback<ByteArray>?): Builder {
            this.callback = callback
            return this
        }

        override fun build(): BytesRequest {
            checkUrlNull()
            return BytesRequest(this)
        }
    }
}