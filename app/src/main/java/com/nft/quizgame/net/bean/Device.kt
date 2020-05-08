package com.nft.quizgame.net.bean

import android.os.Build
import com.google.gson.annotations.SerializedName
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Machine
import com.nft.quizgame.version.VersionController

class Device {

    /**
     *
    1	version_number int	否 设备安装应用版本号
    2	type	int	否	设备类型，1:Android,2:iOS
    3	phone_model string	否 设备机型
    4	net_type	string	否	设备网络类型：2G,3G,4G,5G,WiFi,UNKNOWN
    5	device_id	string	否	设备标识，安卓：android id / IOS： ios idfa
    6	system_version	string	否	设备系统版本号
    7	package_name	string	否	安卓传包名，IOS传appId
    8	sim_country	string	否	SIM卡归属国家地区
    9	language	string	否	语言
    10	user_type	int	否	0：自然，1：买量
    11	chanel_number	int	否	渠道号
    12	install_day	int	否	安装天数
     */
    @SerializedName("version_number")
    var versionNumber: Int = VersionController.currentVersionCode
    var type: Int = 1
    @SerializedName("phone_model")
    var phoneModel: String = Build.MODEL
    @SerializedName("net_type")
    var netType: String = Machine.buildNetworkState(QuizAppState.getContext())
    @SerializedName("device_id")
    var deviceId: String = Machine.getAndroidId(QuizAppState.getContext())
    @SerializedName("system_version")
    var systemVersion: String = Build.VERSION.RELEASE
    @SerializedName("package_name")
    var packageName: String = QuizAppState.getContext().packageName
    @SerializedName("sim_country")
    var simCountry: String = Machine.getSimCountryIso(QuizAppState.getContext())
    var language: String = Machine.getLanguage(QuizAppState.getContext())
    @SerializedName("user_type")
    var userType: Int = if (BuyChannelApiProxy.isBuyUser) 1 else 0
    @SerializedName("chanel_number")
    var channelNum: Int = AppUtils.getChannel(QuizAppState.getContext()).toInt()
    @SerializedName("install_day")
    var installDay: Int = VersionController.cdays
}