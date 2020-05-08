package com.nft.quizgame.function.withdraw

import com.android.volley.VolleyError
import com.nft.quizgame.R
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.CashOutRuleRequestBean
import com.nft.quizgame.net.bean.CashOutRuleResponseBean
import com.nft.quizgame.net.bean.RequestWithdrawResponseBean
import com.nft.quizgame.net.bean.WithdrawRequestBean
import com.nft.quizgame.net.exception.NetError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WithdrawRepository {

    suspend fun getCashOutRuleList(requestBean: CashOutRuleRequestBean) = suspendCoroutine<CashOutRuleResponseBean.CashOutRuleDTO> { cont ->

        NetManager.performGetCashOutRuleList(requestBean, object : RequestCallback<CashOutRuleResponseBean> {
            override fun onResponse(response: CashOutRuleResponseBean) {
                if (response.errorCode == 0) {
                    val data = response.data
                    if (data == null) {
                        cont.resumeWithException(NetError(-1, "data is empty"))
                        return
                    }

                    cont.resume(data)

                } else {
                    //error
                    cont.resumeWithException(NetError(response.errorCode, response.errorMessage))
                }


            }

            override fun onErrorResponse(error: VolleyError) {
                cont.resumeWithException(error)
            }

            override fun onUserExpired() {

            }
        })

    }

    fun requestWithdraw(
            requestBean: WithdrawRequestBean, failCallback: (errorCod: Int?, ErrorMsg: String?) -> Unit,
            successCallback: () -> Unit) {
        NetManager.performRequestWithdraw(requestBean, object : RequestCallback<RequestWithdrawResponseBean> {
            override fun onResponse(response: RequestWithdrawResponseBean) {
                if (response.errorCode == 0) {
                    successCallback.invoke()
                } else {
                    //error
                    failCallback.invoke(response.errorCode, response.errorMessage)
                }
            }

            override fun onErrorResponse(error: VolleyError) {
//                failCallback.invoke(error.networkResponse?.statusCode, error.message)
                failCallback.invoke(ErrorCode.NETWORK_ERROR, error.message)
            }

            override fun onUserExpired() {
            }
        })

    }

}