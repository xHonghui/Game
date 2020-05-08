package com.nft.quizgame.external.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nft.quizgame.data.IDataBase

@Entity(tableName = "external_dialog")
class ExternalDialogBean : IDataBase {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    var id : Int = 0

    @ColumnInfo(name = "_show_count")
    var showCount : Int = 0

    @ColumnInfo(name = "_last_show_time")
    var lastShowTime : Long = 0

    @ColumnInfo(name = "_click_data")
    var clickData : Int = 0


}