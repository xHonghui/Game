package com.nft.quizgame.net

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.android.volley.Request
import com.google.gson.GsonBuilder
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.State
import com.nft.quizgame.common.encrypt.QuizDesUtils
import com.nft.quizgame.common.net.GsonPostRequest
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.common.net.ResponseDecoder
import com.nft.quizgame.common.net.ResponseProcessCallback
import com.nft.quizgame.common.utils.CompanyApiUtils
import com.nft.quizgame.common.utils.CompanyApiUtils.obtainRequestUrl
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.net.bean.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object NetManager {

    private const val AUTHORIZATION = "Authorization"
    private const val X_CRYPTO = "X-Crypto"
    private const val X_SIGNATURE = "X-Signature"
    private const val X_AUTH_TOKEN = "X-Auth-Token"


    val tokenLiveData = MutableLiveData<State>()

    private val userModel: UserViewModel by lazy {
        AppViewModelProvider.getInstance().get(UserViewModel::class.java)
    }

    fun performModuleRequest(requestBean: ModuleRequestBean, callback: RequestCallback<ModuleResponseBean>) {
        performRequest(ModuleRequestBean.REQUEST_PATH, requestBean, ModuleResponseBean::class.java, callback)
    }

    fun performQuestionRequest(requestBean: QuestionRequestBean, callback: RequestCallback<QuestionResponseBean>) {
        performRequest(QuestionRequestBean.REQUEST_PATH, requestBean, QuestionResponseBean::class.java, callback)
    }

    fun performRuleRequest(requestBean: RuleRequestBean, callback: RequestCallback<RuleResponseBean>) {
        performRequest(RuleRequestBean.REQUEST_PATH, requestBean, RuleResponseBean::class.java, callback)
    }

    fun performUserRegisterRequest(requestBean: UserRegisterRequestBean,
                                   callback: RequestCallback<UserRegisterResponseBean>) {
        performRequest(UserRegisterRequestBean.REQUEST_PATH, requestBean, UserRegisterResponseBean::class.java,
                callback)
    }

    fun performSendCaptcha(requestBean: LoginPhoneCaptchaRequestBean, callback: RequestCallback<LoginPhoneCaptchaResponseBean>) {
        performRequest(LoginPhoneCaptchaRequestBean.REQUEST_PATH, requestBean, LoginPhoneCaptchaResponseBean::class.java, callback)

    }

    fun performGetCashOutRuleList(requestBean: CashOutRuleRequestBean,
                                  callback: RequestCallback<CashOutRuleResponseBean>) {
        performRequest(CashOutRuleRequestBean.REQUEST_PATH, requestBean, CashOutRuleResponseBean::class.java, callback)
    }


    fun performSyncDataDownloadRequest(requestBean: SyncDataDownloadRequestBean,
                                       callback: RequestCallback<SyncDataDownloadResponseBean>) {
        performRequest(SyncDataDownloadRequestBean.REQUEST_PATH, requestBean, SyncDataDownloadResponseBean::class.java,
                callback)
    }

    fun performSyncDataUploadRequest(requestBean: SyncDataUploadRequestBean,
                                     callback: RequestCallback<SyncDataUploadResponseBean>) {
        performRequest(SyncDataUploadRequestBean.REQUEST_PATH, requestBean, SyncDataUploadResponseBean::class.java,
                callback)
    }

    fun performCashOutInfoRequest(requestBean: CashOutInfoRequestBean,
                                  callback: RequestCallback<CashOutInfoResponseBean>) {
        performRequest(CashOutInfoRequestBean.REQUEST_PATH, requestBean, CashOutInfoResponseBean::class.java, callback)
    }

    fun performRequestWithdraw(requestBean: WithdrawRequestBean, callback: RequestCallback<RequestWithdrawResponseBean>) {
        performRequest(WithdrawRequestBean.REQUEST_PATH, requestBean, RequestWithdrawResponseBean::class.java, callback)
    }

    fun performRefreshToken(requestBean: RefreshTokenRequestBean, callback: RequestCallback<UserRegisterResponseBean>) {
        performRequest(RefreshTokenRequestBean.REQUEST_PATH, requestBean, UserRegisterResponseBean::class.java, callback)
    }


    fun performCoinInfoRequest(requestBean: CoinInfoRequestBean, callback: RequestCallback<CoinInfoResponseBean>) {
        performRequest(CoinInfoRequestBean.REQUEST_PATH, requestBean, CoinInfoResponseBean::class.java, callback)
    }

    fun performCoinOrderRequest(requestBean: CoinOrderRequestBean, callback: RequestCallback<CoinOrderResponseBean>) {
        performRequest(CoinOrderRequestBean.REQUEST_PATH, requestBean, CoinOrderResponseBean::class.java, callback)
    }

    fun performCoinOptRequest(
            requestBean: CoinOptRequestBean,
            callback: RequestCallback<CoinOptResponseBean>
    ) {
        performRequest(
                CoinOptRequestBean.REQUEST_PATH,
                requestBean,
                CoinOptResponseBean::class.java,
                callback
        )
    }

    fun performAliPayAuthInfo(
            requestBean: AliPayAuthInfoRequestBean,
            callback: RequestCallback<AliPayAuthInfoResponseBean>
    ) {
        performRequest(
                AliPayAuthInfoRequestBean.REQUEST_PATH,
                requestBean,
                AliPayAuthInfoResponseBean::class.java,
                callback
        )
    }

    //这里需要对accessToken做并发处理
    private fun <T> performRequest(requestUri: String, requestBean: BaseRequestBean, responseBeanClass: Class<T>,
                                   callback: RequestCallback<T>) {

        GlobalScope.launch(Dispatchers.Main) {

            if (requestBean.needAccessToken()) {
                //登录成功需要将状态改为成功
                val value = tokenLiveData.value
                if (value is State.Error) {
                    //3008	refresh_token过期	refresh_token过期，客户端应该让用户重新登录


                    //用户失效则直接不请求，直接回调
                    if (value.errorCode == ErrorCode.REFRESH_TOKEN_EXPIRED) {
                        callback.onUserExpired()
                        return@launch
                    }

                    //如果是网络请求失败，则重新请求
                    tokenLiveData.value = State.Request()

                }

                if (tokenLiveData.value is State.Request || tokenLiveData.value is State.Loading) {
                    observerTokenLiveData(requestUri, requestBean, responseBeanClass, callback)
                    return@launch
                }


            }


            requestBean.resetAccessToken()
            val version = getLiveDataVersion(tokenLiveData.value)


            launch(Dispatchers.IO) {
                realPerformRequest(requestUri, requestBean, responseBeanClass, version, callback)
            }
        }


    }


    fun getLiveDataVersion(state: State?): Int {
        var version = 0

        val event = state?.event
        if (event is Int) {
            version = event
        }
        return version
    }

    private fun <T> realPerformRequest(
            requestUri: String, requestBean: BaseRequestBean, responseBeanClass: Class<T>,
            version: Int, callback: RequestCallback<T>
    ) {

        val host = requestBean.requestProperty.host
        val apiKey = requestBean.requestProperty.apiKey
        val secretKey = requestBean.requestProperty.secretKey
        val desKey = requestBean.requestProperty.desKey
        requestBean.requestProperty.clear()

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val payload = gson.toJson(requestBean)
        Logcat.i("GsonRequest_payload", payload)

        val body = QuizDesUtils.encrypt(payload, desKey)

        val queryBuilder = StringBuilder()
        queryBuilder.append("api_key=").append(apiKey).append("&").append("timestamp=").append(System.currentTimeMillis())
        val query = queryBuilder.toString()
        val url = obtainRequestUrl(host, requestUri, query)
        val signature = CompanyApiUtils.obtainSignature(secretKey, "POST", requestUri, query, payload)
        val builder: GsonPostRequest.Builder<T> = GsonPostRequest.Builder()

        builder.requestBody(body).targetObject(responseBeanClass)
                .responseProcessCallback(object : ResponseProcessCallback<T> {
                    override fun process(data: T): Boolean {

                        if (!(data is BaseResponseBean && data.errorCode == ErrorCode.ACCESS_TOKEN_EXPIRED)) {
                            return false
                        }

                        if (!requestBean.needAccessToken() && BuildConfig.DEBUG) {
                            throw IllegalStateException("不需要AccessToken接口出现token失效异常")
                        }

                        requestBean.requestProperty.host = host
                        requestBean.requestProperty.apiKey = apiKey
                        requestBean.requestProperty.secretKey = secretKey
                        requestBean.requestProperty.desKey = desKey

                        GlobalScope.launch(Dispatchers.Main) {

                            val value = tokenLiveData.value
                            if (value == null || value is State.Error) {
                                tokenLiveData.value =
                                        State.Request(getLiveDataVersion(tokenLiveData.value))
                            } else if (value is State.Success) {
                                val event = value.event
                                if (event is Int && event > version) {
                                    //现在token的版本大于请求时的token
                                    performRequest(requestUri, requestBean, responseBeanClass, callback)
                                    return@launch
                                } else {
                                    tokenLiveData.value =
                                            State.Request(getLiveDataVersion(tokenLiveData.value))
                                }
                            }
                            observerTokenLiveData(requestUri, requestBean, responseBeanClass, callback)
                        }
                        return true
                    }
                })
                .callback(callback)
                .method(Request.Method.POST)
                .url(url)
                .addHeader(X_AUTH_TOKEN, requestBean.accessToken)
                .addHeader(X_SIGNATURE, signature)
                .addHeader(X_CRYPTO, "des")
                .addHeader(AUTHORIZATION, signature)
                .decoder(object : ResponseDecoder {
                    override fun decode(data: ByteArray): String {
                        return QuizDesUtils.decryptToString(data, desKey)
                    }
                })
                .build().execute()
    }

    private fun <T> observerTokenLiveData(
            requestUri: String, requestBean: BaseRequestBean, responseBeanClass: Class<T>,
            callback: RequestCallback<T>
    ) {

        tokenLiveData.observeForever(object : Observer<State> {
            override fun onChanged(state: State?) {

                if (state !is State.Success && state !is State.Error) {
                    return
                }

                tokenLiveData.removeObserver(this)

                if (state is State.Success) {
                    //重新请求
                    performRequest(
                            requestUri,
                            requestBean,
                            responseBeanClass,
                            callback
                    )
                } else if (state is State.Error) {
                    //3008	refresh_token过期	refresh_token过期，客户端应该让用户重新登录
                    //失败状态 这里分为重新登录或者请求失败，需要做处理
                    callback.onUserExpired()
                }


            }

        })

    }
}