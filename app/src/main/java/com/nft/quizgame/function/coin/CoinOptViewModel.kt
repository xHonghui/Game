package com.nft.quizgame.function.coin

import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.function.coin.bean.CoinOrderBean
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.CoinInfo
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean.CoinOptDetail.Companion.OPT_CASH_IN
import com.nft.quizgame.net.exception.NetError
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class CoinOptViewModel(private val userData: MutableLiveData<UserBean>) : BaseViewModel() {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val repository = CoinOptRepository()

    private var coinInfoMap: HashMap<String, CoinInfo>? = null

    private var pendingOrders: List<CoinOrderBean>? = null

    private val orderContext = newSingleThreadContext("coin_order_thread")

    private var isDataInitialized = false

    suspend fun initData(forceInitialize: Boolean) {
        if (forceInitialize) {
            isDataInitialized = false
            coinInfoMap?.clear()
        }
        if (isDataInitialized) {
            return
        }
        loadCoinInfoList()
        isDataInitialized = true
    }

    private suspend fun loadCoinInfoList() {
        val infoDeferred = async(IO) {
            val list = repository.fetchCoinInfoList(userData.value!!.accessToken)
            val map = hashMapOf<String, CoinInfo>()
            list.forEach { info ->
                map[info.coinCode!!] = info
            }
            map
        }
        coinInfoMap = infoDeferred.await()
    }

    fun getCoinInfo(coinCode: String): CoinInfo? {
        return coinInfoMap?.get(coinCode)
    }

    /**
     * 充值
     */
    suspend fun operateCashIn(coinCode: String, coins: Int, desc: String = "充值"): String? {
        return withContext(orderContext) {
            val orderBean = CoinOrderBean().apply {
                this.optTime = System.currentTimeMillis()
                this.userId = userData.value!!.userId
                this.optType = OPT_CASH_IN
                this.coinCode = coinCode
                this.optCoin = coins
                this.desc = desc
            }
            repository.addCoinOrder(orderBean)
            var orderId: String? = null
            try {
                orderId = repository.makeOrderByNetwork(userData.value!!.accessToken, orderBean)
                orderBean.orderId = orderId
                repository.updateCoinOrder(orderBean)
                repository.operateOrderByNetwork(userData.value!!.accessToken, orderId)
                repository.removeCoinOrder(orderBean)
            } catch (e: Exception) {
                e.printStackTrace()
                CrashReport.postCatchedException(e)
                if (e is NetError) {
                    if (e.errorCode != ErrorCode.REFRESH_TOKEN_EXPIRED) {
                        repository.removeCoinOrder(orderBean) //订单异常，删除该订单
                    }
                }
            }
            orderId
        }
    }

    /**
     * 是否还有未完成订单
     */
    suspend fun hasPendingOrders(): Boolean {
        return withContext(orderContext) {
            pendingOrders = repository.fetchCoinOrders(userData.value!!.userId)
            pendingOrders != null && pendingOrders!!.isNotEmpty()
        }
    }

    /**
     * 处理所有未完成的订单
     */
    suspend fun processAllPendingOrders() {
        withContext(orderContext) {
            pendingOrders?.forEach { order ->
                if (order.orderId != null) {
                    repository.operateOrderByNetwork(userData.value!!.accessToken, order.orderId!!)
                    repository.removeCoinOrder(order)
                } else {
                    val orderId = repository.makeOrderByNetwork(userData.value!!.accessToken, order)
                    order.orderId = orderId
                    repository.updateCoinOrder(order)
                    try {
                        val success = repository.operateOrderByNetwork(userData.value!!.accessToken, orderId)
                        if (success) {
                            repository.removeCoinOrder(order)
                        }
                    } catch (e: NetError) {
                        if (e.errorCode != ErrorCode.REFRESH_TOKEN_EXPIRED) {
                            repository.removeCoinOrder(order) //订单异常，删除该订单
                        } else {
                            throw e
                        }
                    }
                }
            }
            pendingOrders = null
        }
    }
}