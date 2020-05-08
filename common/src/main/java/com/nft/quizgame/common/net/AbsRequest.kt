package com.nft.quizgame.common.net

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import java.net.HttpURLConnection

/**
 * Created by yangjiacheng on 2018/4/17.
 * ...
 */
abstract class AbsRequest<T> internal constructor(method: Int, url: String?, private val headers: Map<String, String>,
                                                  private val params: Map<String, String>, tag: Any?,
                                                  protected var decoder: ResponseDecoder?,
                                                  private val callback: RequestCallback<T>?) :
        Request<T>(method, url, callback) {

    private var gson: Gson? = null

    fun getGson(): Gson {
        if (gson == null) {
            gson = Gson()
        }
        return gson!!
    }

    override fun deliverResponse(response: T) {
        callback?.onResponse(response)
    }

    override fun getParams(): Map<String, String> {
        return params
    }

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    fun execute() {
        VolleyManager.getInstance().add(this)
    }

    private fun setRequestTag(tag: Any?) {
        var tag: Any? = tag
        if (null == tag) {
            tag = VolleyManager.DEFAULT_REQUEST_TAG
        }
        setTag(tag)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        return if (response.statusCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            var errorMsg = ""
            try {
                errorMsg = "Server error url: $url \n" + "Server error: ${response.statusCode}, data: ${if (response.data != null) String(response.data,
                        charset(HttpHeaderParser.parseCharset(response.headers))) else ""}"
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Response.error(VolleyError(errorMsg))
        } else {
            doParseNetworkResponse(response)
        }
    }

    protected abstract fun doParseNetworkResponse(response: NetworkResponse): Response<T>

    init {
        setRequestTag(tag)
    }
}