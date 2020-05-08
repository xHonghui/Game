package com.nft.quizgame.common.net

import android.util.ArrayMap
import com.android.volley.Request

/**
 * Created by yangjiacheng on 2017/12/27.
 * ...
 */
abstract class SuperRequestBuilder internal constructor() {
    abstract fun build(): AbsRequest<*>
    var url: String? = null
    var method = Request.Method.GET
    var headers: MutableMap<String, String> = ArrayMap(20)
    var params: MutableMap<String, String> = ArrayMap(20)
    var tag: Any? = null
    var decoder: ResponseDecoder? = null

    fun method(method: Int): SuperRequestBuilder {
        this.method = method
        return this
    }

    fun url(url: String?): SuperRequestBuilder {
        this.url = url
        return this
    }

    fun headers(headers: Map<String, String>?): SuperRequestBuilder {
        this.headers.putAll(headers!!)
        return this
    }

    fun addHeader(headerName: String, headerValue: String): SuperRequestBuilder {
        headers[headerName] = headerValue
        return this
    }

    fun params(params: Map<String, String>?): SuperRequestBuilder {
        this.params.putAll(params!!)
        return this
    }

    fun addParam(paramName: String, paramValue: String): SuperRequestBuilder {
        params[paramName] = paramValue
        return this
    }

    fun setTag(tag: Any?): SuperRequestBuilder {
        this.tag = tag
        return this
    }

    fun decoder(decoder: ResponseDecoder?): SuperRequestBuilder {
        this.decoder = decoder
        return this
    }

    fun checkUrlNull() {
        checkNotNull(url) { "url == null" }
    }
}