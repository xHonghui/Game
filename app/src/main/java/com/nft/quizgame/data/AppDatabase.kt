package com.nft.quizgame.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nft.quizgame.cache.CacheBean
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.external.bean.ExternalDialogBean
import com.nft.quizgame.function.coin.bean.CoinOrderBean
import com.nft.quizgame.function.quiz.bean.*
import com.nft.quizgame.function.sync.bean.GameProgressCache
import com.nft.quizgame.function.user.bean.UserBean

@Database(
    entities = [
        UserBean::class,
        QuizItemBean::class,
        QuizTag::class,
        CacheBean::class,
        CardPropertyBean::class,
        ModuleConfigCache::class,
        RuleCache::class,
        GameProgressCache::class,
        CoinOrderBean::class,
        ExternalDialogBean::class
    ],
    exportSchema = true,
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun quizDao(): QuizDao
    abstract fun cacheDao(): CacheDao
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun coinOrderDao(): CoinOrderDao
    abstract fun externalDialogDao(): ExternalDialogDao

    companion object {

        private const val DB_NAME = "quiz_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDataBase(QuizAppState.getContext()).also { instance = it }
            }
        }

        private fun buildDataBase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Logcat.i("Test", "db is created")
                    }
                }).build()
        }
    }
}