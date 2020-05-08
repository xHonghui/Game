package com.nft.quizgame.config.bean

import com.nft.quizgame.common.utils.Logcat
import org.json.JSONArray

class ExternalDialogAdConfigBean(cacheListener: CacheListener? = null) : AbsConfigBean(cacheListener) {

    companion object {
        const val SID = 905
        private const val CACHE_KEY = CACHE_KEY_PREFIX +"external_dialog_advert"
    }

    val list = ArrayList<Boolean>()
    var periodAStart = "1200"
    var periodAFinish = "1400"

    var periodBStart = "2000"
    var periodBFinish = "2200"

    override fun readConfig(jsonArray: JSONArray) {
        val jsonObj = jsonArray.optJSONObject(0) ?: return
        Logcat.d("ExternalDialogAdConfigBean", "readConfig")
        list.clear()
        for (i in 1..15) {
            list.add(jsonObj.optInt("push_no$i") == 1)
        }
        periodAStart = jsonObj.optString("period_a_start")
        periodAFinish = jsonObj.optString("period_a_finish")
        periodBStart = jsonObj.optString("period_b_start")
        periodBFinish = jsonObj.optString("period_b_finish")

    }

    override fun getCacheKey(): String = CACHE_KEY

    override fun restoreDefault() {
        list.clear()
        periodAStart = "1200"
        periodAFinish = "1400"
        periodBStart = "2000"
        periodBFinish = "2200"
    }

}