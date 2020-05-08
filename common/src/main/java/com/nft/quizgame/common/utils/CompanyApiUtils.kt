package com.nft.quizgame.common.utils

import android.text.TextUtils
import com.gomo.commons.security.Base64
import com.nft.quizgame.common.encrypt.HmacUtils

/**
 * Created by yangjiacheng on 2018/4/10.
 * ...
 */
object CompanyApiUtils {

    fun obtainRequestUrl(hostName: String, requestUri: String, queryString: String): String {
        return if (TextUtils.isEmpty(queryString)) {
            hostName + requestUri
        } else "$hostName$requestUri?$queryString"
    }


    //获取 Signature header
    fun obtainSignature(signatureKey: String, method: String, requestUrl: String,
                        queryString: String, payload: String): String {
        val valueToDigest = StringBuilder()
        valueToDigest.append(method).append("\n").append(requestUrl).append("\n")
                .append(queryString).append("\n").append(payload)
        return createSignature(signatureKey, valueToDigest.toString())
    }

    private fun createSignature(signatureKey: String, valueToDigest: String): String {
        val digest = HmacUtils.hmacSha256(signatureKey, valueToDigest)
        return Base64.encodeBase64URLSafeString(digest)
    }
}
