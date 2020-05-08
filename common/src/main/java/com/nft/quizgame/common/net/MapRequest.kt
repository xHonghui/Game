package com.nft.quizgame.common.net

import android.util.ArrayMap
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.UnsupportedEncodingException

/**
 * Created by yangjiacheng on 2017/12/7.
 *
 *
 * 支持请求到的数据是多个并列 Json 对象并列的格式，映射为 Map 类型，以 <Map></Map><String></String>, T> 的格式回调
 * Note：并列 Json 对象需对应相同的 bean，只是key不同。
 * @see .dataKey 为并列 Json 对象的 Key
 *
 *
 *
 * {@link GsonRequest} 处理返回为单一的Json对象的请求
 */
class MapRequest<T> internal constructor(builder: Builder<T>) :
    AbsRequest<Map<String, T>>(
        builder.method, builder.url, builder.headers, builder.params, builder.tag,
        builder.decoder, builder.callback
    ) {
    private val clazz: Class<T>?
    private val dataKey: String?
    override fun doParseNetworkResponse(response: NetworkResponse): Response<Map<String, T>> {
        val typeOfHashMap = object : TypeToken<Map<String?, Any?>?>() {}.type
        return try {
            var json: String = String(response.data, charset(HttpHeaderParser.parseCharset(response.headers)))
            decoder?.apply {
                json = decode(response.data)
            }
            val jsonObject = JSONObject(json)
            val responseData = jsonObject.getString(dataKey)
            val objectMap = getGson().fromJson<Map<String, Any>>(responseData, typeOfHashMap)
            val responseMap: MutableMap<String, T> = ArrayMap()
            for (key in objectMap.keys) {
                val objectJson = Gson().toJson(objectMap[key])
                val obj = Gson().fromJson(objectJson, clazz)
                responseMap[key] = obj
            }
            Response.success(
                responseMap,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            try {
                var json = String(response.data)
                decoder?.apply {
                    json = decode(response.data)
                }
                val jsonObject = JSONObject(json)
                val responseData = jsonObject.getString(dataKey)
                val objectMapMap = getGson().fromJson<Map<String, Any>>(responseData, typeOfHashMap)
                val responseMap: MutableMap<String, T> =
                    ArrayMap()
                for (key in objectMapMap.keys) {
                    val objectJson = Gson().toJson(responseMap[key])
                    val `object` = Gson().fromJson(objectJson, clazz)
                    responseMap[key] = `object`
                }
                Response.success(
                    responseMap,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            } catch (e1: Exception) {
                Response.error(ParseError(e))
            }
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    class Builder<T> : SuperRequestBuilder() {
        var clazz: Class<T>? = null
        var dataKey: String? = null
        var callback: RequestCallback<Map<String, T>>? = null
        fun dataKey(dataKey: String): Builder<T> {
            this.dataKey = dataKey
            return this
        }

        fun targetObject(clazz: Class<T>): Builder<T> {
            this.clazz = clazz
            return this
        }

        fun callback(callback: RequestCallback<Map<String, T>>?): Builder<T> {
            this.callback = callback
            return this
        }

        override fun build(): MapRequest<T> {
            checkUrlNull()
            checkNotNull(clazz) { "targetObject == null" }
            checkNotNull(dataKey) { "dataKey == null" }
            return MapRequest(this)
        }
    }

    init {
        clazz = builder.clazz
        dataKey = builder.dataKey
    }
}