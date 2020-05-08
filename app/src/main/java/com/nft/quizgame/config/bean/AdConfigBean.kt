package com.nft.quizgame.config.bean

import org.json.JSONArray

class AdConfigBean(cacheListener: CacheListener? = null) : AbsConfigBean(cacheListener) {

    companion object {
        const val SID = 906
        private const val CACHE_KEY = CACHE_KEY_PREFIX + "advert"
    }

    var isSplashAdOpened = true
    var isDialogBottomAdOpened = true
    var isExitGameAdOpened = false

    override fun readConfig(jsonArray: JSONArray) {
        val jsonObj = jsonArray.optJSONObject(0) ?: return
        isSplashAdOpened = jsonObj.optInt("launching") == 1
        isDialogBottomAdOpened = jsonObj.optInt("dialog_bottom") == 1
        isExitGameAdOpened = jsonObj.optInt("game_exit") == 1
    }

    override fun getCacheKey(): String = CACHE_KEY

    override fun restoreDefault() {
        isSplashAdOpened = true
        isDialogBottomAdOpened = true
        isExitGameAdOpened = false
    }

}