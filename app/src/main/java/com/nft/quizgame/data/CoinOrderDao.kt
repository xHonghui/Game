package com.nft.quizgame.data

import androidx.room.*
import com.nft.quizgame.function.coin.bean.CoinOrderBean

@Dao
interface CoinOrderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addCoinOrder(order: CoinOrderBean)

    @Update
    fun updateCoinOrder(order: CoinOrderBean)

    @Delete
    fun removeCoinOrder(order: CoinOrderBean)

    @Query("SELECT * FROM coin_order WHERE _user_id = :userId order by _opt_time asc")
    fun loadCoinOrders(userId: String): List<CoinOrderBean>


}