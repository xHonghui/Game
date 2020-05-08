package com.nft.quizgame.function.quiz.bean

import android.app.AlarmManager
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.net.bean.ModuleConfig

@Entity(tableName = "module_config_cache")
class ModuleConfigCache : IDataBase {

    companion object {
        const val TIME_LIMIT = AlarmManager.INTERVAL_HOUR * 8
    }

    @PrimaryKey
    @ColumnInfo(name = "_module_code")
    var moduleCode: Int? = null

    @ColumnInfo(name = "_config")
    var configJson: String? = null
        set(value) {
            field = value
            val gson = Gson()
            moduleConfig = gson.fromJson<ModuleConfig>(field, ModuleConfig::class.java)
            moduleConfig?.extractEaseAndTag()
        }

    @Ignore
    var moduleConfig: ModuleConfig? = null

    @ColumnInfo(name = "_update_time")
    var updateTime: Long = 0L
}