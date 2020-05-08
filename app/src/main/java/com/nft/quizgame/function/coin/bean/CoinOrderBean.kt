package com.nft.quizgame.function.coin.bean

import androidx.room.*
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean.CoinOptDetail.Companion.OPT_CASH_IN

@Entity(tableName = "coin_order",
        foreignKeys = [ForeignKey(entity = UserBean::class, parentColumns = ["_user_id"], childColumns = ["_user_id"])],
        indices = [Index(value = ["_user_id"])])
class CoinOrderBean : IDataBase {

    @PrimaryKey
    @ColumnInfo(name = "_opt_time")
    var optTime: Long = 0
    @ColumnInfo(name = "_user_id")
    var userId: String? = null
    @ColumnInfo(name = "_opt_type")
    var optType: Int = OPT_CASH_IN
    @ColumnInfo(name = "_coin_code")
    var coinCode: String? = null
    @ColumnInfo(name = "_opt_coin")
    var optCoin: Int = 0
    @ColumnInfo(name = "_desc")
    var desc: String? = null
    @ColumnInfo(name = "_order_id")
    var orderId: String? = null
}