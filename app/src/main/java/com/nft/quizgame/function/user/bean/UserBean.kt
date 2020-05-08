package com.nft.quizgame.function.user.bean

import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.net.bean.CoinInfo

@Entity(tableName = "user")
class UserBean : IDataBase {

    companion object {
        const val PAY_TYPE_NONE = -1
        const val PAY_TYPE_ALI_PAY = 0
        const val PAY_TYPE_WE_CHART_PAY = 1

        const val TYPE_WECHAT = "wechat"
        const val TYPE_ALIPAY = "alipay"
        const val TYPE_VISITOR = "visitor"
        const val TYPE_PHONE = "phone"

    }

    @PrimaryKey
    @ColumnInfo(name = "_user_id")
    var userId: String = ""
    @ColumnInfo(name = "_phone_num")
    var phoneNum: String? = null
    @ColumnInfo(name = "_pay_id")
    var payId: String? = null
    @ColumnInfo(name = "_pay_type")
    var payType: Int = PAY_TYPE_NONE
    @ColumnInfo(name = "_user_type")
    var userType: String = ""
    @ColumnInfo(name = "_access_token")
    var accessToken: String = ""
    @ColumnInfo(name = "_refresh_token")
    var refreshToken: String = ""

    @Ignore
    var coinInfoData:MutableLiveData<CoinInfo> = MutableLiveData()
    @Ignore
    var coinAnim = 0
        set(value) {
            field = value
            coinDisplay.value = when {
                value > 9999999 -> {
                    "${value / 1000}K"
                }
                else -> {
                    value.toString()
                }
            }
        }
    @Ignore
    var coinDisplay = MutableLiveData("0")


}