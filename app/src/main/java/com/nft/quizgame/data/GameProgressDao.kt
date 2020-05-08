package com.nft.quizgame.data

import androidx.room.*
import com.nft.quizgame.function.sync.bean.GameProgressCache

@Dao
interface GameProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addGameProgress(property: GameProgressCache)

    @Update
    fun updateGameProgress(property: GameProgressCache)

    @Delete
    fun removeGameProgress(property: GameProgressCache)

    @Query("select * from game_progress where _user_id = :userId order by _update_time asc")
    fun loadGameProgresses(userId: String): List<GameProgressCache>

    @Query("select * from game_progress where _user_id = :userId and _update_time = (select max(_update_time) from game_progress)")
    fun loadLastGameProgress(userId: String): GameProgressCache?

    @Query("select _key from game_progress where _user_id = :userId order by _update_time asc")
    fun getLastGameProgressKey(userId: String): Int
}