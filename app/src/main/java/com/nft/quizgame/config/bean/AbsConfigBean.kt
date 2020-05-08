package com.nft.quizgame.config.bean

import android.text.TextUtils
import com.nft.quizgame.cache.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2020/3/30
 * @tips 这个类是Object的子类
 * @fuction
 */
abstract class AbsConfigBean(cacheListener: CacheListener? = null) {
    companion object {
        const val CACHE_KEY_PREFIX = "key_ab_config_"
    }

    protected var mAbTestId = -1 //AB测试ID

    protected var mFilterId = -1

    protected var outDateTime = -1L

    protected var mCacheListener: CacheListener? = cacheListener

    var mIsInited: Boolean = false //是否已经初始化过

    protected var mCacheManager: CacheManager
    private val mutex = Mutex()

    init {
        mCacheManager = CacheManager()
    }

    fun setAbTestId(abTestId: Int) {
        mAbTestId = abTestId
    }

    fun getAbTestId(): Int {
        return mAbTestId
    }

    fun setFilterId(filterId: Int) {
        mFilterId = filterId
    }

    fun getFilterId(): Int {
        return mFilterId
    }

    suspend fun readObjectByCache(needCheckOutDate: Boolean) {
        mutex.withLock {
            if (mIsInited) {
                if (needCheckOutDate && outDateTime > 0 && outDateTime < System.currentTimeMillis()) {
                    mCacheListener?.onConfigDataOutDate()
                }
                return
            }
            val cache = withContext(Dispatchers.IO) {
                mCacheManager.loadCache(getCacheKey())
            }
            outDateTime = -1L
            if (cache != null && cache.cacheContent.isNotBlank()) {
                outDateTime = cache.cacheTime + cache.cacheLimit
                if (needCheckOutDate && outDateTime < System.currentTimeMillis()) {
                    mCacheListener?.onConfigDataOutDate()
                }

                val dataJson = cache.cacheContent
                if (!TextUtils.isEmpty(dataJson)) {
                    try {
                        extractData(JSONObject(dataJson))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            } else {
                restoreDefault()
                if (needCheckOutDate) {
                    mCacheListener?.onConfigDataOutDate()
                }
            }
            mIsInited = true
        }
    }

    suspend fun saveObjectToCache(dataJson: JSONObject) {
        withContext(Dispatchers.IO) {
            mCacheManager.saveCache(getCacheKey(), dataJson.toString())
        }
        mIsInited = false
    }

    private fun extractData(dataJson: JSONObject?): Boolean {
        if (dataJson != null) {
            val infoJson = dataJson.optJSONObject("infos")
            if (null != infoJson) {
                try {
                    val abTestId = infoJson.getInt("abtest_id")
                    if (abTestId != -1) {
                        setAbTestId(abTestId)
                    }
                    val filterId = infoJson.getInt("filter_id")
                    if (filterId != -1) {
                        setFilterId(filterId)
                    }
                } catch (e: JSONException) {
                    //do nothing
                }

                val cfgs = infoJson.optJSONArray("cfgs")
                if (cfgs != null && cfgs.length() > 0) {
                    readConfig(cfgs)
                    return true
                } else {
                    restoreDefault()
                }
            }
        }
        return false
    }

    protected abstract fun readConfig(jsonArray: JSONArray)

    abstract fun getCacheKey(): String

    protected abstract fun restoreDefault()


    interface CacheListener {
        fun onConfigDataOutDate()
    }
}