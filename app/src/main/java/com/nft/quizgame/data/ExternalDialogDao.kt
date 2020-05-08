package com.nft.quizgame.data

import androidx.room.*
import com.nft.quizgame.external.bean.ExternalDialogBean
import com.nft.quizgame.function.user.bean.UserBean

@Dao
interface ExternalDialogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveDialogBean(bean: ExternalDialogBean)


    @Query("SELECT * FROM external_dialog WHERE _id = :id")
    fun queryDialogBean(id: Int): ExternalDialogBean?


}