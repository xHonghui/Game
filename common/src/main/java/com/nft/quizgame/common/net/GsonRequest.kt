package com.nft.quizgame.common.net

import com.android.volley.NetworkError
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.JsonSyntaxException
import com.nft.quizgame.common.utils.Logcat
import java.io.UnsupportedEncodingException

/**
 * Created by yangjiacheng on 2017/11/17.
 *
 * 处理服务器返回为单一的Json对象的请求
 */
open class GsonRequest<T> internal constructor(method: Int, url: String?, headers: Map<String, String>,
                                               params: Map<String, String>, tag: Any?, decoder: ResponseDecoder?,
                                               callback: RequestCallback<T>?, private val clazz: Class<T>?,private val responseProcessCallback:ResponseProcessCallback<T>?) :
        AbsRequest<T>(method, url, headers, params, tag, decoder, callback) {

    private var mAlreadyProcess = false

    internal constructor(builder: Builder<T>) : this(builder.method, builder.url, builder.headers, builder.params,
            builder.tag, builder.decoder, builder.callback, builder.clazz,builder.responseProcessCallback)

    override fun doParseNetworkResponse(resp: NetworkResponse): Response<T> {
        var response = resp
        return try {
            var json = String(response.data, charset(HttpHeaderParser.parseCharset(response.headers)))
            if (json.contains("403 Forbidden")) {
                response = NetworkResponse(403, response.data, response.notModified,
                    response.networkTimeMs, response.allHeaders
                )
                return Response.error(NetworkError(response))
            }
            if (decoder != null) {
                json = try {
                    decoder!!.decode(response.data)
                } catch (e1: Exception) {
                    return Response.error(ParseError(e1))
                }
            }
            Logcat.i("GsonRequest", "json: $json")
            val fromJson = getGson().fromJson(json, clazz)
            mAlreadyProcess =  responseProcessCallback?.process(fromJson) ?:false
            Response.success(fromJson, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e2: UnsupportedEncodingException) {
            try {
                var json = String(response.data)
                if (decoder != null) {
                    json = try {
                        decoder!!.decode(response.data)
                    } catch (e3: Exception) {
                        return Response.error(ParseError(e3))
                    }
                }
                Response.success(getGson().fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response))
            } catch (e4: JsonSyntaxException) {
                Response.error(ParseError(e4))
            }
        } catch (e5: JsonSyntaxException) {
            Response.error(ParseError(e5))
        }
    }

    class Builder<T> : SuperRequestBuilder() {
        var clazz: Class<T>? = null
        var callback: RequestCallback<T>? = null
        var responseProcessCallback:ResponseProcessCallback<T>?=null

        fun targetObject(clazz: Class<T>?): Builder<T> {
            this.clazz = clazz
            return this
        }

        fun callback(callback: RequestCallback<T>?): Builder<T> {
            this.callback = callback
            return this
        }

        fun responseProcessCallback(callback: ResponseProcessCallback<T>?): Builder<T> {
            this.responseProcessCallback = callback
            return this
        }

        override fun build(): GsonRequest<T> {
            checkUrlNull()
            checkNotNull(clazz) { "targetObject == null" }
            return GsonRequest(this)
        }
    }


    override fun deliverResponse(response: T) {
        if(mAlreadyProcess){
            return
        }
        super.deliverResponse(response)
    }

}