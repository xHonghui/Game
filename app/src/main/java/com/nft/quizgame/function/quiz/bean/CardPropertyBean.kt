package com.nft.quizgame.function.quiz.bean

import android.util.SparseArray
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.nft.quizgame.data.IDataBase
import com.nft.quizgame.function.quiz.QuizMode
import com.nft.quizgame.function.user.bean.UserBean
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Entity(
        tableName = "card_property",
        primaryKeys = ["_user_id", "_card_type", "_quiz_mode"],
        foreignKeys = [ForeignKey(entity = UserBean::class, parentColumns = ["_user_id"], childColumns = ["_user_id"])],
        indices = [Index(value = ["_user_id"])]
)
class CardPropertyBean : IDataBase {

    companion object {
        const val TYPE_TIPS = 0 //提示卡
        const val TYPE_CHANGE = 1 //换题卡
        const val TYPE_ENVELOPE = 3 //红包

        fun initCardProperties(map: SparseArray<CardPropertyBean>, userId: String, mode: QuizMode) {
            CardPropertyBean(userId, TYPE_TIPS, mode, 0).apply { map.put(cardType, this) }
            CardPropertyBean(userId, TYPE_CHANGE, mode, 0).apply { map.put(cardType, this) }
            CardPropertyBean(userId, TYPE_ENVELOPE, mode, 0).apply { map.put(cardType, this) }
        }
    }

    constructor()
    constructor(userId: String, cardType: Int, quizMode: QuizMode, cardAmount: Int) {
        this.userId = userId
        this.cardType = cardType
        this.quizMode = quizMode.value
        this.cardAmount = cardAmount
    }

    @ColumnInfo(name = "_card_type")
    var cardType: Int = -1
    @ColumnInfo(name = "_quiz_mode")
    var quizMode: Int = -1
    @ColumnInfo(name = "_card_amount")
    var cardAmount: Int = 0
        set(value) {
            field = value
            GlobalScope.launch(Main) {
                cardAmountDisplay.value = when {
                    value == 0 -> {
                        ""
                    }
                    value > 99 -> {
                        "99+"
                    }
                    else -> {
                        value.toString()
                    }
                }
            }
        }
    @ColumnInfo(name = "_user_id")
    var userId: String = ""

    @Ignore
    var cardAmountDisplay = MutableLiveData("")
}