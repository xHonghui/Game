package com.nft.quizgame.common.utils

import android.text.TextUtils
import org.json.JSONObject

object JsonParseUtils {


    fun <T> getValue(key: String, default: T, json: String): T {
        if (!TextUtils.isEmpty(json)) {
            val json = JSONObject(json)
            if (json.has(key)) {
                return json.get(key) as T
            }
        }
        return default
    }

}