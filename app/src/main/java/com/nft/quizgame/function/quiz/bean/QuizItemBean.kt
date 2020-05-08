package com.nft.quizgame.function.quiz.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.nft.quizgame.data.IDataBase

@Entity(tableName = "quiz_item")
class QuizItemBean : IDataBase {
    companion object {
        const val TYPE_SINGLE_CHOICE = 0
    }

    @PrimaryKey
    @ColumnInfo(name = "_id")
    var id: Int = 0

    @ColumnInfo(name = "_content")
    var content: String? = null

    @ColumnInfo(name = "_options")
    var options: String? = null
        set(value) {
            field = value
            if (optionList == null) {
                optionList = arrayListOf()
                value?.split("^")?.filter { it.isNotEmpty() }?.let { optionList!!.addAll(it) }
            }
        }

    @ColumnInfo(name = "_answer")
    var answer: Int? = null

    @ColumnInfo(name = "_ease")
    var ease: Int? = null

    @ColumnInfo(name = "_type")
    var type: Int = TYPE_SINGLE_CHOICE

    @ColumnInfo(name = "_is_correct")
    var isCorrect: Boolean? = null

    @ColumnInfo(name = "_answer_time")
    var answerTime: Long = 0

    @Ignore
    var optionList: MutableList<String>? = null
        set(value) {
            field = value
            if (options == null) {
                options = ""
                value?.forEachIndexed { index, option ->
                    options += if (index == value.size - 1) {
                        option
                    } else {
                        "$option^"
                    }
                }
            }
        }

    @Ignore
    var tagList: List<QuizTag>? = null
}