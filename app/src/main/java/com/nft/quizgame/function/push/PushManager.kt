package com.nft.quizgame.function.push

import android.content.Context
import cn.jpush.android.api.JPushInterface
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.utils.NotifyManager
import java.util.HashSet

object PushManager {

    fun init(context: Context) {
        NotifyManager.getInstance().init(context)
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(context)
    }

    fun setTag(context: Context){
        if (BuildConfig.DEBUG) {
            val hashSet = HashSet<String>()
            hashSet.add("debug")
            val filterValidTags = JPushInterface.filterValidTags(hashSet)
            if (filterValidTags.size > 0) {
                Logcat.d("PushManager","setTags")
                JPushInterface.setTags(context, 1, hashSet)
            }
        }
    }

}