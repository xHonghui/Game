package com.nft.quizgame.common.buychannel

import android.app.Application
import android.text.TextUtils
import com.appsflyer.AppsFlyerLib
import com.cs.bd.buychannel.BuyChannelApi
import com.cs.bd.buychannel.BuySdkInitParams
import com.cs.bd.buychannel.IBuyChannelUpdateListener
import com.cs.bd.commerce.util.LogUtils
import com.nft.quizgame.common.*
import com.nft.quizgame.common.statistic.BaseSeq19OperationStatistic
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat

/**
 * Created by kingyang on 2017/2/16.
 */
object BuyChannelApiProxy {
    private const val UNKNOWN_BUY_CHANNEL = "unknown_buychannel"
    private lateinit var sContext: Application
    fun preInit(app: Application?) {
        if (BuildConfig.DEBUG) {
            BuyChannelApi.setDebugMode()
        }
        BuyChannelApi.preInit(true, app)
    }

    @JvmStatic
    fun init(application: Application) {
        sContext = application
        LogUtils.setShowLog(BuildConfig.DEBUG)
        AppsFlyerLib.getInstance().setOutOfStore(AppUtils.getStore(sContext)) //"xxx"为对应的商店名
        AppsFlyerLib.getInstance().setCollectIMEI(true) //设置收集设备IMEI用于追踪用户；
        AppsFlyerLib.getInstance().setCollectAndroidID(true) //设置收集设备AndroidId；
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(
                2) //通过该API设置两次Session上报的时间间隔。Activity onResume()会有首次Session的上报，用户授权后获取IMEI，会有第二次Session的上报，如果两次Session上报的时间间隔小于设定的值，则第二次Session会被block掉。
        val builder = BuySdkInitParams.Builder(QuizEnv.sChannelId,
                sContext.resources.getInteger(R.integer.diff_config_statistic_45_fun_id),
                sContext.getString(R.string.diff_buychannel_cid), null, false,
                sContext.getString(R.string.diff_buychannel_product_key),
                sContext.getString(R.string.diff_buychannel_access_key))
        BuyChannelApi.init(application, builder.build())
        registerBuyChannelUpdateListener(IBuyChannelUpdateListener { buyChannel ->
            Logcat.i("buychannelsdk", "BuyChannel: $buyChannel")
            val iApplication = QuizAppState.getApplication() as IApplication
            if (iApplication.isMainProcess()) {
                if (isBuyChannelFetched) {
                    BaseSeq19OperationStatistic.uploadBasicInfo()
                }
            }
        })
    }

    val buyChannel: String
        get() = BuyChannelApi.getBuyChannelBean(sContext).buyChannel

    val secondUserType: Int
        get() = BuyChannelApi.getBuyChannelBean(sContext).secondUserType

    val isBuyUser: Boolean
        get() = BuyChannelApi.getBuyChannelBean(sContext).isUserBuy

    fun registerBuyChannelUpdateListener(listener: IBuyChannelUpdateListener?) {
        BuyChannelApi.registerBuyChannelListener(sContext, listener)
    }

    fun unregisterBuyChannelUpdateListener(listener: IBuyChannelUpdateListener?) {
        BuyChannelApi.unregisterBuyChannelListener(sContext, listener)
    }

    val isBuyChannelFetched: Boolean
        get() {
            val buyChannel = buyChannel
            return if (TextUtils.isEmpty(buyChannel)) {
                false
            } else UNKNOWN_BUY_CHANNEL != buyChannel
        }

    val campaign: String
        get() {
            val bean = BuyChannelApi.getBuyChannelBean(sContext)
            val campaign = bean.campaign
            Logcat.i("campaign", "campaign: $campaign")
            Logcat.i("campaign", bean.toJsonStr())
            return campaign
        }
}