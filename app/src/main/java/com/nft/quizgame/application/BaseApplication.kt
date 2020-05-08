package com.nft.quizgame.application

import android.app.Application
import android.content.Context
import com.nft.quizgame.common.IApplication

/**
 * App基类
 * @author yangguanxiang
 */
abstract class BaseApplication(private var mProcessName: String?) : Application(), IApplication {

    protected lateinit var mDelegate: BaseApplicationDelegate

    override fun onCreate() {
        super.onCreate()
        mDelegate.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mDelegate = BaseApplicationDelegate(this)
    }

    override fun getCustomProcessName(): String? {
        return mProcessName
    }

    override fun stopSdk(stop: Boolean) {}

}