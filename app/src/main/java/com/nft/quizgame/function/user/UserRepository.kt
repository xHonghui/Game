package com.nft.quizgame.function.user

import android.text.TextUtils
import com.android.volley.VolleyError
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.data.AppDatabase
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.*
import com.nft.quizgame.net.exception.NetError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository {
    private val dao = AppDatabase.getInstance().userDao()

    fun addUser(user: UserBean) {
        dao.addUser(user)
    }

    fun updateUser(user: UserBean) {
        dao.updateUser(user)
    }

    fun loadUser(): UserBean? {
        val userId = PrivatePreference.getPreference().getValue(PrefConst.KEY_CURRENT_USER_ID, "")
        if (!TextUtils.isEmpty(userId)) {
            return dao.queryUser(userId)
        }
        return null
    }

    fun register(
        userRegisterRequestBean: UserRegisterRequestBean,
        failCallback: (errorCod: Int?, ErrorMsg: String?) -> Unit,
        successCallback: (userBean: UserBean) -> Unit
    ) {
        NetManager.performUserRegisterRequest(userRegisterRequestBean,
            object : RequestCallback<UserRegisterResponseBean> {
                override fun onErrorResponse(error: VolleyError) {
//                    failCallback.invoke(error.networkResponse?.statusCode, error.message)
                    failCallback.invoke(ErrorCode.NETWORK_ERROR, error.message)
                }

                override fun onResponse(response: UserRegisterResponseBean) {
                    if (response.errorCode == 0) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val identityId = userRegisterRequestBean.identityId!!

                            PrivatePreference.getPreference().putValue(PrefConst.KEY_CURRENT_USER_ID, identityId).apply()

                            var userBean = withContext(Dispatchers.IO) {
                                loadUser()
                            }

                            if (userBean == null) {
                                userBean = UserBean()
                            }


                            userBean.userId = identityId
                            userBean.userType = userRegisterRequestBean.accountType!!
                            userBean.accessToken = response.data!!.token!!.accessToken!!
                            userBean.refreshToken = response.data!!.token!!.refreshToken!!

                            withContext(Dispatchers.IO) {
                                addUser(userBean)
                            }


                            successCallback.invoke(userBean)

                        }


                    } else {
                        //error
                        failCallback.invoke(response.errorCode, response.errorMessage)
                    }

                }

                override fun onUserExpired() {

                }

            })
    }


    suspend fun refreshToken() = suspendCoroutine<Token> { cont ->


        NetManager.performRefreshToken(
            RefreshTokenRequestBean(),
            object : RequestCallback<UserRegisterResponseBean> {
                override fun onResponse(response: UserRegisterResponseBean) {
                    if (response.errorCode == 0) {
                        cont.resume(response.data!!.token!!)
                    } else {
                        //error
                        cont.resumeWithException(NetError(response.errorCode,response.errorMessage))
                    }
                }

                override fun onErrorResponse(error: VolleyError) {
                    cont.resumeWithException(error)
                }

                override fun onUserExpired() {

                }


            })
    }


     suspend fun requestAliPayAuthInfo() = suspendCoroutine<AliPayAuthInfoResponseBean.SignDTO> { cont ->

        NetManager.performAliPayAuthInfo(AliPayAuthInfoRequestBean(),
            object : RequestCallback<AliPayAuthInfoResponseBean> {
                override fun onResponse(response: AliPayAuthInfoResponseBean) {
                    if (response.errorCode == 0) {
                        cont.resume(response.data!!)
                    } else {
                        //error
                        cont.resumeWithException(NetError(response.errorCode))
                    }
                }

                override fun onErrorResponse(error: VolleyError) {
                    cont.resumeWithException(error)
                }

                override fun onUserExpired() {
                }

            })

    }


}