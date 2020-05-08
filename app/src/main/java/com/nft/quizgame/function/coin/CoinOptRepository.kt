package com.nft.quizgame.function.coin

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.data.AppDatabase
import com.nft.quizgame.function.coin.bean.CoinOrderBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.*
import com.nft.quizgame.net.exception.NetError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CoinOptRepository {

    private val dao = AppDatabase.getInstance().coinOrderDao()

    suspend fun fetchCoinInfoList(accessToken: String) = suspendCoroutine<List<CoinInfo>> { cont ->
        val requestBean = CoinInfoRequestBean().apply {
            this.accessToken = accessToken
        }
        NetManager.performCoinInfoRequest(requestBean, object : RequestCallback<CoinInfoResponseBean> {
            override fun onResponse(response: CoinInfoResponseBean) {
                if (response.errorCode == 0) {
                    cont.resume(response.data?.coinInfoList!!)
                } else {
                    cont.resumeWithException(NetError(response.errorCode, response.errorMessage))
                }
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }

    fun addCoinOrder(order: CoinOrderBean) {
        dao.addCoinOrder(order)
    }

    fun updateCoinOrder(order: CoinOrderBean) {
        dao.updateCoinOrder(order)
    }

    fun removeCoinOrder(order: CoinOrderBean) {
        dao.removeCoinOrder(order)
    }

    fun fetchCoinOrders(userId: String): List<CoinOrderBean> {
        return dao.loadCoinOrders(userId)
    }

    suspend fun makeOrderByNetwork(accessToken: String,
                                   coinOrderBean: CoinOrderBean) = suspendCoroutine<String> { cont ->
        val requestBean = CoinOrderRequestBean().apply {
            this.accessToken = accessToken
            this.optType = coinOrderBean.optType
            this.coinCode = coinOrderBean.coinCode
            this.optCoin = coinOrderBean.optCoin
            this.desc = coinOrderBean.desc
        }
        NetManager.performCoinOrderRequest(requestBean, object : RequestCallback<CoinOrderResponseBean> {
            override fun onResponse(response: CoinOrderResponseBean) {
                if (response.errorCode == 0) {
                    cont.resume(response.data!!)
                } else {
                    cont.resumeWithException(NetError(response.errorCode, response.errorMessage))
                }
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }

    suspend fun operateOrderByNetwork(accessToken: String, orderId: String) = suspendCoroutine<Boolean> { cont ->
        val requestBean = CoinOptRequestBean().apply {
            this.accessToken = accessToken
            this.orderId = orderId
        }
        NetManager.performCoinOptRequest(requestBean, object : RequestCallback<CoinOptResponseBean> {
            override fun onResponse(response: CoinOptResponseBean) {
                if (response.errorCode == 0) {
                    cont.resume(true)
                } else {
                    cont.resumeWithException(NetError(response.errorCode, "orderId: $orderId, ${response.errorMessage}"))
                }
            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {
                cont.resumeWithException(NetError(ErrorCode.REFRESH_TOKEN_EXPIRED))
            }
        })
    }
}