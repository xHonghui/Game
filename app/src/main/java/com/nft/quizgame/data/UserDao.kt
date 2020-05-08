package com.nft.quizgame.data

import androidx.room.*
import com.nft.quizgame.function.user.bean.UserBean

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user: UserBean)

    @Update
    fun updateUser(user: UserBean)

    @Delete
    fun removeUser(user: UserBean)

    @Query("SELECT * FROM user WHERE _user_id = :userId")
    fun queryUser(userId: String): UserBean?


}