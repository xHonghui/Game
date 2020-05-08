package com.nft.quizgame.function.sync.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean

@Entity(tableName = "game_progress", primaryKeys = ["_user_id", "_key"],
        foreignKeys = [ForeignKey(entity = UserBean::class, parentColumns = ["_user_id"], childColumns = ["_user_id"])])
class GameProgressCache : IDataBase {
    @ColumnInfo(name = "_user_id")
    var userId: String = ""
    @ColumnInfo(name = "_key")
    var key: Int = -1
    @ColumnInfo(name = "_value")
    var value: String? = null
    @ColumnInfo(name = "_update_time")
    var updateTime: Long = 0

    @Ignore
    var gameProgress:SyncDataUploadRequestBean.GameProgress? = null
}