package com.nft.quizgame.config

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.SparseArray
import com.cpcphone.abtestcenter.AbtestCenterService
import com.cs.bd.ad.AdSdkApi
import com.cs.bd.commerce.util.io.StringUtils
import com.nft.quizgame.R
import com.nft.quizgame.common.IApplication
import com.nft.quizgame.common.ICustomAction
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.Machine
import com.nft.quizgame.config.bean.AbsConfigBean
import com.nft.quizgame.config.bean.AdConfigBean
import com.nft.quizgame.config.bean.ExternalDialogAdConfigBean
import com.nft.quizgame.version.VersionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * ┌───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┬───┐ ┌───┬───┬───┐
 * │Esc│ │ F1│ F2│ F3│ F4│ │ F5│ F6│ F7│ F8│ │ F9│F10│F11│F12│ │P/S│S L│P/B│ ┌┐    ┌┐    ┌┐
 * └───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┴───┘ └───┴───┴───┘ └┘    └┘    └┘
 * ┌──┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───────┐┌───┬───┬───┐┌───┬───┬───┬───┐
 * │~`│! 1│@ 2│# 3│$ 4│% 5│^ 6│& 7│* 8│( 9│) 0│_ -│+ =│ BacSp ││Ins│Hom│PUp││N L│ / │ * │ - │
 * ├──┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─────┤├───┼───┼───┤├───┼───┼───┼───┤
 * │Tab │ Q │ W │ E │ R │ T │ Y │ U │ I │ O │ P │{ [│} ]│ | \ ││Del│End│PDn││ 7 │ 8 │ 9 │   │
 * ├────┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴┬──┴─────┤└───┴───┴───┘├───┼───┼───┤ + │
 * │Caps │ A │ S │ D │ F │ G │ H │ J │ K │ L │: ;│" '│ Enter  │             │ 4 │ 5 │ 6 │   │
 * ├─────┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴────────┤    ┌───┐    ├───┼───┼───┼───┤
 * │Shift  │ Z │ X │ C │ V │ B │ N │ M │< ,│> .│? /│  Shift   │    │ ↑ │    │ 1 │ 2 │ 3 │   │
 * ├────┬──┴─┬─┴──┬┴───┴───┴───┴───┴───┴──┬┴───┼───┴┬────┬────┤┌───┼───┼───┐├───┴───┼───┤ E││
 * │Ctrl│Ray │Alt │         Space         │ Alt│code│fuck│Ctrl││ ← │ ↓ │ → ││   0   │ . │←─┘│
 * └────┴────┴────┴───────────────────────┴────┴────┴────┴────┘└───┴───┴───┘└───────┴───┴───┘
 *
 * @author Rayhahah
 * @blog http://rayhahah.com
 * @time 2020/3/30
 * @tips 这个类是Object的子类
 * @fuction
 */
class ConfigManager private constructor() {

    companion object {
        private const val TAG = "ConfigManager"
        fun getInstance() = Holder.instance
    }

    object Holder {
        val instance = ConfigManager()
    }

    init {
        val app = QuizAppState.getApplication() as IApplication
        if (!app.isMainProcess()) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (ICustomAction.ACTION_REQUEST_AB_CONFIG_COMPLETED == action) {
                        val sid = intent.getIntExtra("sid", -1)
                        val success = intent.getBooleanExtra("success", false)
                        if (success) {
                            GlobalScope.launch(Dispatchers.Main) {
                                val configBean = getConfigBean(sid)
                                configBean.mIsInited = false
                                configBean.readObjectByCache(false)
                                val callback = mCallbacks.get(sid)
                                if (callback != null) {
                                    callback.success(configBean)
                                    mCallbacks.remove(sid)
                                }
                            }
                        } else {
                            val callback = mCallbacks.get(sid)
                            if (callback != null) {
                                callback.error()
                                mCallbacks.remove(sid)
                            }
                        }
                    }
                }
            }
            val intentFilter = IntentFilter()
            intentFilter.addAction(ICustomAction.ACTION_REQUEST_AB_CONFIG_COMPLETED)
            QuizAppState.getApplication().registerReceiver(receiver, intentFilter)
        } else {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (ICustomAction.ACTION_REQUEST_AB_CONFIG == action) {
                        val sid = intent.getIntExtra("sid", -1)
                        doRequestConfig(context, sid, null)
                    }
                }
            }
            val intentFilter = IntentFilter()
            intentFilter.addAction(ICustomAction.ACTION_REQUEST_AB_CONFIG)
            QuizAppState.getApplication().registerReceiver(receiver, intentFilter)
        }
    }

    private val cid = Integer.parseInt(QuizAppState.getContext().resources.getString(R.string.diff_config_ab_test_cid))
    private val cid2 = QuizAppState.getContext().resources.getInteger(R.integer.diff_config_statistic_19_product_id)
    private val mConfigMap = ConcurrentHashMap<Int, AbsConfigBean>()
    private val mCallbacks = SparseArray<HttpCallback>()


    fun loadAllConfigs() {
        GlobalScope.launch {
            getConfigBean(AdConfigBean.SID)
            getConfigBean(ExternalDialogAdConfigBean.SID)
        }
    }
    /**
     * 根据不同的业务id获取配置信息
     * @param sid
     * @return
     */
    suspend fun getConfigBean(sid: Int, needCheckOutDate: Boolean = true): AbsConfigBean {
        var configBean: AbsConfigBean? = mConfigMap[sid]
        if (configBean == null) {
            configBean = ConfigBeanFactory.getConfigBean(sid)
            if (configBean != null) {
                mConfigMap[sid] = configBean
            }
        }
        configBean?.readObjectByCache(needCheckOutDate)
        return configBean!!
    }

    fun requestConfig(context: Context, sid: Int, callback: HttpCallback? = null) {
        val app = QuizAppState.getApplication() as IApplication
        if (app.isMainProcess()) {
            doRequestConfig(context, sid, callback)
        } else {
            if (callback != null) {
                mCallbacks.put(sid, callback)
            }
            val intent = Intent(ICustomAction.ACTION_REQUEST_AB_CONFIG)
            intent.putExtra("sid", sid)
            QuizAppState.getContext().sendBroadcast(intent)
        }
    }

    private fun doRequestConfig(context: Context, sid: Int, callback: HttpCallback?) {
        val service = AbtestCenterService.Builder()
            .sid(intArrayOf(sid))// 业务ID
            .cid(cid)     // 产品ID
            .cid2(cid2) //统计协议使用的产品ID, GO桌面为1
            .cversion(
                AppUtils.getVersionCodeByPkgName(context, context.packageName)
            ) // 客户端版本号，必须大于0
            .local(StringUtils.toUpperCase(Machine.getCountry(context))) //国家
            .utm_source(BuyChannelApiProxy.buyChannel) //买量渠道
            .user_from(BuyChannelApiProxy.secondUserType) // 买量SDK用户类型
            .entrance(AbtestCenterService.Builder.Entrance.MAIN_PACKAGE)    //业务请求入口
            .cdays(
                AdSdkApi.calculateCDays(context, AppUtils.getInstallTime(context))
            )       //客户端安装天数，必须大于0
            .aid(Machine.getAndroidId(context)) //客户端安卓ID
            .isSafe(true)
            .isupgrade(if (VersionController.isNewUser) 2 else 1).build(context) //是否升级用户： 1是，2否

        Logcat.i(
            TAG, "sid: " + sid + " cid: " + cid + " cid2: " + cid2 + " versionCode: " +
                    AppUtils.getVersionCodeByPkgName(context, context.packageName) +
                    " locale: " +
                    StringUtils.toUpperCase(Machine.getCountry(context)) + " buyChannel: " +
                    BuyChannelApiProxy.buyChannel + " cdays: " + AdSdkApi.calculateCDays(
                context,
                AppUtils.getInstallTime(context)
            ) +
                    " androidID: " + Machine.getAndroidId(context) + " isNewUser: " +
                    VersionController.isNewUser + " userFrom: " + BuyChannelApiProxy.secondUserType
        )
        try {
            service.send(object : AbtestCenterService.ResultCallback {

                override fun onResponse(response: String) {
                    GlobalScope.launch(Dispatchers.Main) {
                        val configBean = getConfigBean(sid, false)
                        Logcat.i(TAG, response)
                        val json = getDataJson(response)
                        val intent = Intent(ICustomAction.ACTION_REQUEST_AB_CONFIG_COMPLETED)
                        intent.putExtra("sid", sid)
                        if (json != null) {
                            configBean.saveObjectToCache(json)
                            callback?.success(configBean)
                            AbtestCenterService.retentionStatics(
                                context, cid2, sid, configBean.getAbTestId(),
                                configBean.getFilterId()
                            )
                            intent.putExtra("success", true)
                        } else {
                            callback?.error()
                            intent.putExtra("success", false)
                        }
                        QuizAppState.getContext().sendBroadcast(intent)
                    }
                }

                override fun onError(errorMsg: String?, errorCode: Int) {
                    Logcat.e(TAG, errorMsg)
                    callback?.error()
                    val intent = Intent(ICustomAction.ACTION_REQUEST_AB_CONFIG_COMPLETED)
                    intent.putExtra("sid", sid)
                    intent.putExtra("success", false)
                    QuizAppState.getContext().sendBroadcast(intent)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.error()
            val intent = Intent(ICustomAction.ACTION_REQUEST_AB_CONFIG_COMPLETED)
            intent.putExtra("sid", sid)
            intent.putExtra("success", false)
            QuizAppState.getContext().sendBroadcast(intent)
        }
    }


    /**
     * get info
     */
    private fun getDataJson(json: String): JSONObject? {
        if (!TextUtils.isEmpty(json)) {
            try {
                val jsonObject = JSONObject(json)
                val value = jsonObject.optBoolean("success")
                if (value) {
                    return jsonObject.optJSONObject("datas")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
        return null
    }
}