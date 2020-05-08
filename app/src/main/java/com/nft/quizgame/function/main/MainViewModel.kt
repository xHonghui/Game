package com.nft.quizgame.function.main

import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.R
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.exception.NetError
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val userData: MutableLiveData<UserBean>) : BaseViewModel() {

    companion object {
        const val CASH_OUT_INFO_SIZE = 20
        const val CASH_OUT_INFO_OUT_DATE_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES
    }

    var cashOutInfoData = MutableLiveData<List<String>>()

    private val repository = MainRepository()

    private var lastFetchCashOutInfoTime = 0L

    fun fetchCashOutInfo(context: Context) {
        launch(IO) {
            var list: List<String>? = null
            try {
                list = repository.loadCashOutInfo(userData.value!!.accessToken, CASH_OUT_INFO_SIZE)?.map { info ->
                    context.getString(R.string.cash_out_info, info.userName, info.amount)
                }
                withContext(Main) {
                    lastFetchCashOutInfoTime = System.currentTimeMillis()
                    cashOutInfoData.value = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is NetError) {
                    CrashReport.postCatchedException(e)
                }
            }
        }
    }

    fun isCashOutInfoOutDate(): Boolean = System.currentTimeMillis() - lastFetchCashOutInfoTime > CASH_OUT_INFO_OUT_DATE_TIME
}