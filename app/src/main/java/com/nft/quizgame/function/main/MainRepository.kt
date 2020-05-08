package com.nft.quizgame.function.main

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.CashOutInfo
import com.nft.quizgame.net.bean.CashOutInfoRequestBean
import com.nft.quizgame.net.bean.CashOutInfoResponseBean
import com.nft.quizgame.net.exception.NetError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainRepository {

    suspend fun loadCashOutInfo(accessToken: String, size: Int) =
        suspendCoroutine<List<CashOutInfo>?> { cont ->
            val requestBean = CashOutInfoRequestBean().apply {
                this.accessToken = accessToken
                this.size = size
            }
            NetManager.performCashOutInfoRequest(requestBean, object :
                RequestCallback<CashOutInfoResponseBean> {
                override fun onResponse(response: CashOutInfoResponseBean) {
                    cont.resume(response.data?.cashOutInfo)
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