package com.nft.quizgame.function.withdraw

import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.R
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.State
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.ext.post
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.net.bean.CashOutRuleRequestBean
import com.nft.quizgame.net.bean.CashOutRuleResponseBean
import com.nft.quizgame.net.bean.WithdrawRequestBean
import com.nft.quizgame.net.exception.NetError
import com.nft.quizgame.utils.StateErrorConvertUtil
import kotlinx.coroutines.*
import java.lang.Exception

class WithdrawViewModel : BaseViewModel() {

    private val repository = WithdrawRepository()
    val withdrawListLiveData = MutableLiveData<ArrayList<WithdrawItem>>()
    var mData: CashOutRuleResponseBean.CashOutRuleDTO? = null

    fun getWithdrawList() {

        //获取数据时，同时将金币同步
        stateData.value = Event(State.Loading())
        val handler = CoroutineExceptionHandler { _, exception ->
            Logcat.d("WithdrawViewModel", exception.message)
            post {
                Logcat.d("WithdrawViewModel", "post ${exception.message}")
                stateData.value = Event(StateErrorConvertUtil.convertError(exception as Exception))
            }
        }

        launch(handler) {
            delay(400) //这里睡300毫秒,减轻动画卡顿感
            val a = async(Dispatchers.IO) {
                val coinViewModel = AppViewModelProvider.getInstance().get(CoinOptViewModel::class.java)
                if (coinViewModel.hasPendingOrders()) {
                    Logcat.d("withdraViewModel", "hasPendingOrders")
                    coinViewModel.processAllPendingOrders()
                    Logcat.d("withdraViewModel", "processAllPendingOrders")
                }
            }

            var cashOutRuleList: ArrayList<WithdrawItem>? = null
            val b = async(Dispatchers.IO) {
                if (withdrawListLiveData.value != null) {
                    return@async
                }
                val data = repository.getCashOutRuleList(CashOutRuleRequestBean())
                mData = data

                val withdrawAmounts = data.withdrawAmounts
                if (withdrawAmounts == null || withdrawAmounts.isEmpty()) {
                    throw NetError(-1, "data is empty")
                }

                val result = ArrayList<WithdrawItem>(withdrawAmounts.size)
                for (item in withdrawAmounts) {
                    val withdrawItem = WithdrawItem()
                    val price = item.realCurrency
                    val gold = item.coinAmount

                    withdrawItem.title = QuizAppState.getContext().getString(R.string.money_symbol, price)
                    withdrawItem.desc =
                            QuizAppState.getContext().getString(R.string.consume_gold_symbol, gold.toString())
                    withdrawItem.cashOutId = item.cashOutId
                    withdrawItem.gold = gold
                    withdrawItem.money = price!!.toDouble()

                    result.add(withdrawItem)
                }
                result[0].isNewUser = true

                if (data.isFirstTimeWithdraw == 0) {
                    //不是首次提现
                    result.removeAt(0)
                }

                cashOutRuleList = result
            }


            a.await()
            b.await()

            launch(Dispatchers.Main) {
                if (cashOutRuleList != null) {
                    withdrawListLiveData.value = cashOutRuleList
                }
                stateData.value = Event(State.Success())
            }

        }
    }

    fun isTodayWithdraw(): Boolean {
        val data = mData
        return data != null && data.isTodayWithdraw == 1
//        return false
    }

    fun requestWithdraw(cashOutId: Int, cashOutGold: Int, account: String, accountName: String) {
        val bean = WithdrawRequestBean()
        bean.cashOutId = cashOutId
        bean.withdrawalAccount = account
        bean.userRealName = accountName
        stateData.value = Event(State.Loading())

        repository.requestWithdraw(bean, { errorCode, errorMsg ->
            stateData.value = Event(State.Error(errorCode ?: 0, errorMsg))
        }, {
            withdrawListLiveData.value?.let {
                if (it.isNotEmpty()) {
                    if (it[0].isNewUser) {
                        it.removeAt(0)
                        withdrawListLiveData.value = it
                    }

                }
            }

            mData?.let {
                it.isFirstTimeWithdraw = 0
                it.isTodayWithdraw = 1
            }

            AppViewModelProvider.getInstance().get(UserViewModel::class.java).addUserCoin(-cashOutGold)
            stateData.value = Event(State.Success())
        })
    }


}