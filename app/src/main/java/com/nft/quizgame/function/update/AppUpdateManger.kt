package com.nft.quizgame.function.update

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.base.microservicesbase.ServicesCallback
import com.base.services.version.Version
import com.base.services.version.VersionApi
import com.google.gson.Gson
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.ext.post
import com.nft.quizgame.version.VersionController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object AppUpdateManger {

    const val TAG = "AppUpdateManger"

    val mDownloadFinishData = MutableLiveData<UpgradeVersionHelper.VersionRecord>()
    val mVersionLiveData = MutableLiveData<Version>()

    fun init() {
        val value = PrivatePreference.getPreference().getValue(PrefConst.KEY_APP_UPDATE_INFO, "")
        if (!TextUtils.isEmpty(value)) {
            val fromJson = Gson().fromJson<Version>(value, Version::class.java)
            onNewVersion(fromJson)
        }
    }

    suspend fun checkAppUpdate() = suspendCoroutine<Version> { suspend ->
        val deviceMap = HashMap<String, Any>()
        deviceMap.put("channel", AppUtils.getChannel(QuizAppState.getContext()))
        deviceMap.put("version_number", VersionController.currentVersionCode)
        deviceMap.put("gp", false)
        deviceMap.put("source", 1)
        Logcat.d(TAG, "checkAppUpdate")
        VersionApi.getVersion(QuizAppState.getContext(), deviceMap,
                object : ServicesCallback<Version> {
                    override fun onSuccess(version: Version?) {
                        if (version == null) {
                            Logcat.d(TAG, "Version == null")
                            suspend.resumeWithException(IllegalStateException("Version == null"))
                        } else {
                            Logcat.d(TAG, version.toString())
                            onNewVersion(version)
                            suspend.resume(version)
                        }
                    }

                    override fun onError(e: Exception?) {
                        Logcat.d(TAG, "onError ${e?.toString() ?: ""}")
                        suspend.resumeWithException(e ?: IllegalStateException("Exception"))
                    }

                }
        )
    }


    private fun onNewVersion(version: Version) {

        post {
            val value = mVersionLiveData.value
            if (value != null && value.versionNumber >= version.versionNumber) {
                return@post
            }

            if (!version.isHaveNewVersion || version.versionNumber <= VersionController.currentVersionCode) {
                return@post
            }


            //保存数据
            PrivatePreference.getPreference().putValue(PrefConst.KEY_APP_UPDATE_INFO, Gson().toJson(version)).apply()
            if (version.versionNumber > VersionController.currentVersionCode) {
                mVersionLiveData.value = version
            }
        }

    }


}