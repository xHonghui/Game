package com.nft.quizgame.function.quiz.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.nft.quizgame.data.IDataBase

@Entity(tableName = "quiz_item_tag",
        primaryKeys = ["_quiz_item_id", "_tag"],
        foreignKeys = [ForeignKey(entity = QuizItemBean::class, parentColumns = ["_id"],
                childColumns = ["_quiz_item_id"])])
class QuizTag : IDataBase {

    @ColumnInfo(name = "_quiz_item_id")
    var quizItemId: Int = -1

    @ColumnInfo(name = "_tag")
    var tag: Int = -1
}