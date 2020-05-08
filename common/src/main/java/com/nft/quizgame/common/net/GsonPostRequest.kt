package com.nft.quizgame.common.net

import com.android.volley.AuthFailureError
import org.json.JSONObject

/**
 * Created by yangjiacheng on 2018/4/17.
 * 需要 post 一个Json 字符串到服务器的请求
 * TODO 考虑到提交内容的ContentType有多种情况，不一定只是 “application/json”，后续需要对其他情况进行封装
 */
class GsonPostRequest<T> internal constructor(builder: Builder<T>) :
        GsonRequest<T>(builder.method, builder.url, builder.headers, builder.params, builder.tag, builder.decoder,
                builder.callback, builder.clazz,builder.responseProcessCallback) {

    private val mRequestBody: Any?
    override fun getBodyContentType(): String {
        return if (mRequestBody == null) {
            PROTOCOL_CHARSET
        } else PROTOCOL_CONTENT_TYPE
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        if (mRequestBody == null) {
            return super.getBody()
        }
        if (mRequestBody is JSONObject) {
            return mRequestBody.toString().toByteArray()
        } else if (mRequestBody is String) {
            return mRequestBody.toByteArray()
        }else if(mRequestBody is ByteArray){
            return mRequestBody
        }
        return null
    }

    class Builder<T> : SuperRequestBuilder() {
        var clazz: Class<T>? = null
        var callback: RequestCallback<T>? = null
        var requestBody: Any? = null
        var responseProcessCallback:ResponseProcessCallback<T>?=null

        fun responseProcessCallback(callback: ResponseProcessCallback<T>?): Builder<T> {
            this.responseProcessCallback = callback
            return this
        }

        fun targetObject(clazz: Class<T>?): Builder<T> {
            this.clazz = clazz
            return this
        }

        fun callback(callback: RequestCallback<T>?): Builder<T> {
            this.callback = callback
            return this
        }

        fun requestBody(requestBody: Any?): Builder<T> {
            this.requestBody = requestBody
            return this
        }

        override fun build(): GsonPostRequest<T> {
            checkUrlNull()
            checkNotNull(clazz) { "targetObject == null" }
            return GsonPostRequest(this)
        }
    }

    companion object {
        private const val PROTOCOL_CHARSET = "utf-8"
        private val PROTOCOL_CONTENT_TYPE = String.format(
            "application/json; charset=%s",
            PROTOCOL_CHARSET
        )
    }

    init {
        mRequestBody = builder.requestBody
    }
}