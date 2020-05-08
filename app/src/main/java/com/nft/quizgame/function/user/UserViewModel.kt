package com.nft.quizgame.function.user

import android.app.Activity
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.alipay.sdk.app.AuthTask
import com.android.volley.VolleyError
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.R
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.State
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.Machine
import com.nft.quizgame.ext.notify
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.sync.GlobalPropertyViewModel
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.CoinInfo
import com.nft.quizgame.net.bean.UserRegisterRequestBean
import com.nft.quizgame.net.exception.NetError
import com.nft.quizgame.sound.SoundManager
import com.nft.quizgame.statistic.Statistic103
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class UserViewModel : BaseViewModel() {


    companion object {
        const val TYPE_LOADING_REGISTER_ALIPAY = 1
        const val TYPE_LOADING_REGISTER_PHONE = 2
        const val TYPE_LOADING_REGISTER_VISITOR = 3
    }

    val userData = MutableLiveData<UserBean>()
    val refreshTokenExpiredLiveData = MutableLiveData<Event<Boolean>>()
    private val repository = UserRepository()
    val initAppDataState = MutableLiveData<Event<State>>()
    var visitorAccountTest = ""

    init {
        userData.observeForever { user ->
            if (user != null) {
                initAllData(true, user)
            }
        }

        launch {
            var userBean: UserBean? = null
            withContext(Dispatchers.IO) {
                userBean = repository.loadUser()
            }

            userBean ?: return@launch

            withContext(Dispatchers.Main) {
                userData.value = userBean
            }

        }

        NetManager.tokenLiveData.observeForever { state ->
            if (state !is State.Request) {
                return@observeForever
            }

            val liveDataVersion = NetManager.getLiveDataVersion(state)

            NetManager.tokenLiveData.value = State.Loading(liveDataVersion)
            launch(Dispatchers.IO) {

                try {
                    val refreshToken = repository.refreshToken()

                    withContext(Dispatchers.Main) {

                        val value = userData.value
                        assert(value != null)
                        value!!.accessToken = refreshToken.accessToken!!
                        value.refreshToken = refreshToken.refreshToken!!

                        withContext(Dispatchers.IO) {
                            repository.updateUser(value)
                        }
                        userData.value = value
                        val version = liveDataVersion + 1
                        NetManager.tokenLiveData.value = State.Success(version)
                    }


                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        when (e) {
                            is VolleyError -> {
                                //网络错误
                                NetManager.tokenLiveData.value =
                                        State.Error(e.networkResponse.statusCode, e.message, liveDataVersion)
                            }
                            is NetError -> {
                                if (e.errorCode != ErrorCode.REFRESH_TOKEN_EXPIRED && BuildConfig.DEBUG) {
                                    throw IllegalStateException("刷新token出现异常 errorCode = " + e.errorCode)
                                }

                                clearUser()


                                //这里是需要重新登录
                                refreshTokenExpiredLiveData.value = Event(true)


                                NetManager.tokenLiveData.value =
                                        State.Error(e.errorCode, e.message, liveDataVersion)
                            }
                            else -> {
                                throw IllegalStateException(e)
                            }
                        }

                    }
                }


            }
        }


    }

    private fun clearUser() {
        userData.value = null
        PrivatePreference.getPreference().putValue(PrefConst.KEY_CURRENT_USER_ID, "").apply()

    }

    fun initAllData(forceInitialize: Boolean, user: UserBean) {
        val startTime = System.currentTimeMillis()
        initAppDataState.value = Event(State.Loading())
        launch {
            try {
                AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
                        .initPropertyData(forceInitialize)
                AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java)
                        .initPropertyData(forceInitialize)
                val coinViewModel = AppViewModelProvider.getInstance().get(CoinOptViewModel::class.java)
                if (coinViewModel.hasPendingOrders()) {
                    coinViewModel.processAllPendingOrders()
                }
                coinViewModel.initData(forceInitialize)
                user.coinInfoData.value = coinViewModel.getCoinInfo(CoinInfo.GOLD_COIN)
                if (user.coinInfoData.value == null) {
                    initAppDataState.value = Event(State.Error(ErrorCode.INIT_DATA_ERROR, event = System.currentTimeMillis() - startTime))
                } else {
                    withContext(IO){
                        SoundManager.preloadSounds()
                    }
                    initAppDataState.value = Event(State.Success(System.currentTimeMillis() - startTime))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                CrashReport.postCatchedException(e)
                if (e is NetError && e.errorCode == ErrorCode.REFRESH_TOKEN_EXPIRED) {
                    initAppDataState.value = Event(State.Error(e.errorCode, event = System.currentTimeMillis() - startTime))
                } else {
                    initAppDataState.value = Event(State.Error(ErrorCode.INIT_DATA_ERROR, event = System.currentTimeMillis() - startTime))
                }
            }
        }
    }

    fun addUserCoin(coins: Int) {
        userData.value?.let { user ->
            user.coinInfoData.value?.apply {
                this.totalCoin += coins
                this.existingCoin += coins
                this.coinDiff = coins
            }
            user.coinInfoData.notify()
        }
    }

    //手机登录
    fun registerPhone(phoneNumber: String, verificationCode: String) {
        val userRegisterRequestBean = UserRegisterRequestBean()
        userRegisterRequestBean.accountType = UserBean.TYPE_PHONE
        userRegisterRequestBean.identityId = phoneNumber
        userRegisterRequestBean.verificationCode = verificationCode
        register(userRegisterRequestBean, TYPE_LOADING_REGISTER_PHONE)
    }

    //游客登录
    fun registerVisitor(skip: Boolean?=false) {
        if (skip == true) {
            Statistic103.uploadLoginoptionClick(4)
        } else {
            Statistic103.uploadLoginoptionClick(3)
        }

        val userRegisterRequestBean = UserRegisterRequestBean()
        userRegisterRequestBean.accountType = UserBean.TYPE_VISITOR
        userRegisterRequestBean.identityId = Machine.getAndroidId(QuizAppState.getContext())
        if (BuildConfig.DEBUG) {
            userRegisterRequestBean.identityId += visitorAccountTest
        }
        register(userRegisterRequestBean, TYPE_LOADING_REGISTER_VISITOR)
    }

    //支付宝登录
    fun registerAliPay(activity: Activity) {
        Statistic103.uploadLoginoptionClick(1)
        stateData.value = Event(State.Loading())

        val weakReference = WeakReference<Activity>(activity)

        launch(Dispatchers.IO) {
            try {
                val authInfo = repository.requestAliPayAuthInfo()
                val get = weakReference.get() ?: return@launch
                //通知外部
                // 构造AuthTask 对象
                // 构造AuthTask 对象
                val authTask = AuthTask(get)
                // 调用授权接口，获取授权结果
                // 调用授权接口，获取授权结果
                val result = authTask.authV2(authInfo.sign, true)
                Logcat.d("Login", result.toString())

                launch(Dispatchers.Main) {
                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    val authResult = AuthResult(result, true)
                    val resultStatus = authResult.resultStatus
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.resultCode, "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        val userRegisterRequestBean = UserRegisterRequestBean()
                        userRegisterRequestBean.accountType = UserBean.TYPE_ALIPAY
                        userRegisterRequestBean.identityId = authResult.alipayOpenId
                        userRegisterRequestBean.authCode = authResult.authCode
                        register(userRegisterRequestBean, TYPE_LOADING_REGISTER_ALIPAY)

                    } else {
                        // 其他状态值则为授权失败
                        Logcat.e("auth", authResult.toString())
                        stateData.value = Event(
                                State.Error(ErrorCode.ALI_PAY_AUTH_ERROR, QuizAppState.getContext().getString(R.string.auth_failed)))
                    }
                }


            } catch (e: java.lang.Exception) {
                launch(Dispatchers.Main) {
                    when (e) {
                        is VolleyError -> {
                            stateData.value =
                                    Event(State.Error(e.networkResponse?.statusCode
                                            ?: -1, e.message))
                        }
                        is NetError -> {
                            stateData.value = Event(State.Error(e.errorCode, e.message))
                        }
                        else -> {
                            throw IllegalStateException(e)
                        }
                    }

                }
            }
        }

    }

    //如果已经注册，会返回成功
    private fun register(userRegisterRequestBean: UserRegisterRequestBean, loadingType: Int) {
        stateData.value = Event(State.Loading(loadingType))

        repository.register(userRegisterRequestBean, { errorCode, errorMsg ->
            Logcat.d("UserViewModel", "onErrorResponse")
            Statistic103.uploadLogin_done(loadingType,1,(errorCode ?: -1).toString())
            stateData.value = Event(State.Error(errorCode ?: -1, errorMsg ?: "", loadingType))
        }, {
            Statistic103.uploadLogin_done(loadingType,1)
            Logcat.d("UserViewModel", "onResponse")
            userData.value = it

            val liveDataVersion = NetManager.getLiveDataVersion(NetManager.tokenLiveData.value)
            NetManager.tokenLiveData.value = State.Success(liveDataVersion + 1)

            stateData.value = Event(State.Success(loadingType))
        })
    }

    fun getUserAccessToken(): String? {
        return userData.value?.accessToken
    }

    fun getUserRefreshToken(): String? {
        return userData.value?.refreshToken
    }
}