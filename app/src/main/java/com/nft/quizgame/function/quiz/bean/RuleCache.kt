package com.nft.quizgame.function.quiz.bean

import android.app.AlarmManager
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.net.bean.Rule

@Entity(tableName = "rule_cache")
class RuleCache : IDataBase {
    companion object {
        const val TIME_LIMIT = AlarmManager.INTERVAL_HOUR * 8

        const val TYPE_NEW_USER_BONUS = -1
        const val TYPE_FREE = 0
        const val TYPE_STAGE = 1
        const val TYPE_RACING = 2
    }

    @PrimaryKey
    @ColumnInfo(name = "_module_code")
    var moduleCode: Int = -1

    /**
     * -1 新人红包
     * 0 自由模式
     * 1 闯关模式
     * 2 竞速模式
     */
    @ColumnInfo(name = "_type")
    var type: Int = -2

    @ColumnInfo(name = "_rule")
    var ruleJson: String? = null
        set(value) {
            field = value
            val gson = Gson()
            rule = Rule().apply {
                this.moduleCode = this@RuleCache.moduleCode
                this.type = this@RuleCache.type
                when (type) {
                    TYPE_NEW_USER_BONUS -> newUserBonusRule = gson.fromJson<NewUserBonusRule>(field,
                            NewUserBonusRule::class.java)
                    TYPE_FREE -> freeRule = gson.fromJson<FreeRule>(field, FreeRule::class.java)
                    TYPE_STAGE -> stageRule = gson.fromJson<StageRule>(field, StageRule::class.java)
                    TYPE_RACING -> racingRule = gson.fromJson<RacingRule>(field, RacingRule::class.java)
                }
            }

        }

    @ColumnInfo(name = "_update_time")
    var updateTime: Long = 0L

    @Ignore
    var rule: Rule? = null
}