package com.nft.quizgame.common.pref

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.nft.quizgame.common.QuizAppState

/**
 *
 * @author yangguanxiang
 */
@SuppressLint("CommitPrefEdits")
class PrivatePreference private constructor() {
    companion object {
        private const val PREF_NAME = "quiz_game_prefs.xml"
        fun getPreference(): PrivatePreference = Holder.instance
    }

    object Holder {
        val instance = PrivatePreference()
    }

    private val pref: QuizSharedPreferences
    private val edit: SharedPreferences.Editor


    init {
        pref = QuizSharedPreferences.getSharedPreferences(
            QuizAppState.getContext(), PREF_NAME,
            Context.MODE_PRIVATE
        )
        edit = pref.edit()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(key: String, defaultValue: T): T {
        val value: Any? = when (defaultValue) {
            is Long -> pref.getLong(key, defaultValue)
            is String -> pref.getString(key, defaultValue)
            is Int -> pref.getInt(key, defaultValue)
            is Boolean -> pref.getBoolean(key, defaultValue)
            is Float -> pref.getFloat(key, defaultValue)
            else -> throw IllegalArgumentException("没有找到该对象类型的sharePref")
        }
        return value as T
    }

    fun <T> putValue(key: String, defaultValue: T): PrivatePreference {
        when (defaultValue) {
            is Long -> edit.putLong(key, defaultValue)
            is String -> edit.putString(key, defaultValue)
            is Int -> edit.putInt(key, defaultValue)
            is Boolean -> edit.putBoolean(key, defaultValue)
            is Float -> edit.putFloat(key, defaultValue)
            else -> throw IllegalArgumentException("对象${defaultValue}不能保存到sharePref")
        }
        return this
    }

    fun apply() {
        edit.apply()
    }
}