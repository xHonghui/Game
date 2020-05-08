package com.nft.quizgame.function.signin

import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.function.signin.bean.SignInBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.SigninRequestBean
import com.nft.quizgame.net.bean.SignInResponseBean
import kotlin.coroutines.suspendCoroutine

class SignInRepository {

    suspend fun getSignInData(signInRequestBean: SigninRequestBean,
                              failCallback: (errorCod: Int?, ErrorMsg: String?) -> Unit,
                              successCallback: (signInBean: SignInBean) -> Unit) = suspendCoroutine<SignInBean> {

    }


}