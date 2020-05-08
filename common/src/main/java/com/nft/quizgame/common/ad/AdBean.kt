package com.nft.quizgame.common.ad

import android.app.AlarmManager
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.utils.Logcat

class AdBean(val moduleId: Int) {
    companion object {
        private const val TAG = "AdBean"
        private const val VALID_DATE = AlarmManager.INTERVAL_HOUR
    }

    var adData: AdData? = null
    var interactionListener: AdInteractionListener? = null
    private val createTime: Long = System.currentTimeMillis()
    /**
     * 是否展示过
     */
    var isShown: Boolean = false
    /**
     * 是否失效
     *
     * @return
     */
    val isOutDate: Boolean
        get() = System.currentTimeMillis() - createTime >= VALID_DATE

    val isLoading = MutableLiveData(false)

    interface AdInteractionListener {
        fun onAdShowed()
        fun onAdClicked()
        fun onAdClosed()
        fun onVideoPlayFinished()
    }

    open class AdInteractionListenerAdapter : AdInteractionListener {
        override fun onAdShowed() {
            Logcat.i(TAG, "onAdShowed")
        }

        override fun onAdClicked() {
            Logcat.i(TAG, "onAdClicked")
        }

        override fun onAdClosed() {
            Logcat.i(TAG, "onAdClosed")
        }

        override fun onVideoPlayFinished() {
            Logcat.i(TAG, "onVideoPlayFinished")
        }

    }
}

