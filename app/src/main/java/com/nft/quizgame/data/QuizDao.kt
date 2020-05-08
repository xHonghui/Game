package com.nft.quizgame.data

import androidx.room.*
import com.nft.quizgame.function.quiz.bean.*

@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addQuizItem(item: QuizItemBean)

    @Update
    fun updateQuizItem(item: QuizItemBean)

    @Delete
    fun removeQuizItem(item: QuizItemBean)

    @Query("select * from quiz_item where _answer_time = 0 and _id not in (:filterIds) order by random() limit :limit")
    fun loadQuizItems(filterIds: List<Int>, limit: Int): List<QuizItemBean>

    @Query("select * from quiz_item where _answer_time = 0 and _id not in (:filterIds) and _ease in (:ease) order by random() limit :limit")
    fun loadQuizItemsByEase(filterIds: List<Int>, ease: List<Int>, limit: Int): List<QuizItemBean>

    @Query("select * from quiz_item where _answer_time = 0 and _id not in (:filterIds) and _id in (select _quiz_item_id from quiz_item_tag where _tag in (:tags)) order by random() limit :limit")
    fun loadQuizItemsByTags(filterIds: List<Int>, tags: List<Int>, limit: Int): List<QuizItemBean>

    @Query("select * from quiz_item where _answer_time = 0 and _id not in (:filterIds) and _ease in (:ease) and _id in (select _quiz_item_id from quiz_item_tag where _tag in (:tags)) order by random() limit :limit")
    fun loadQuizItems(filterIds: List<Int>, ease: List<Int>, tags: List<Int>, limit: Int): List<QuizItemBean>

    @Query("select _id from quiz_item where _ease in (:ease) and _id in (select _quiz_item_id from quiz_item_tag where _tag in (:tags)) order by _answer_time desc limit :limit")
    fun loadQuizIds(ease: List<Int>, tags: List<Int>, limit: Int): List<Int>

    @Query("select _id from quiz_item where _ease in (:ease) order by _answer_time desc limit :limit")
    fun loadQuizIdsByEase(ease: List<Int>, limit: Int): List<Int>

    @Query("select _id from quiz_item where _id in (select _quiz_item_id from quiz_item_tag where _tag in (:tags)) order by _answer_time desc limit :limit")
    fun loadQuizIdsByTags(tags: List<Int>, limit: Int): List<Int>

    @Query("select _id from quiz_item order by _answer_time desc limit :limit")
    fun loadQuizIds(limit: Int): List<Int>

    @Query("select count(*) from quiz_item where _answer_time > 0")
    fun getAnsweredQuizItemCount():Int

    @Query("delete from quiz_item_tag where _quiz_item_id in (select _id from quiz_item where _answer_time > 0 order by _answer_time asc limit :limit)")
    fun removeOutDateQuizItemRelativeTag(limit: Int)

    @Query("delete from quiz_item where _id in (select _id from quiz_item where _answer_time > 0 order by _answer_time asc limit :limit)")
    fun removeOutDateQuizItem(limit: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addCardProperty(property: CardPropertyBean)

    @Update
    fun updateCardProperty(property: CardPropertyBean)

    @Delete
    fun removeCardProperty(property: CardPropertyBean)

    @Query("select * from card_property where _user_id = :userId and _quiz_mode = :mode")
    fun loadCardProperties(userId: String, mode: Int): List<CardPropertyBean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addRuleCache(cache: RuleCache)

    @Update
    fun updateRuleCache(cache: RuleCache)

    @Delete
    fun deleteRuleCache(cache: RuleCache)

    @Query("select * from rule_cache where _update_time > :now - :limitTime")
    fun loadRuleCache(now: Long, limitTime: Long): List<RuleCache>

    @Query("delete from rule_cache")
    fun removeAllRules()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addModuleConfigCache(cache: ModuleConfigCache)

    @Update
    fun updateModuleConfigCache(cache: ModuleConfigCache)

    @Delete
    fun deleteModuleConfigCache(cache: ModuleConfigCache)

    @Query("select * from module_config_cache where _update_time > :now - :limitTime")
    fun loadModuleConfigs(now: Long, limitTime: Long): List<ModuleConfigCache>

    @Query("delete from module_config_cache")
    fun removeAllModuleConfigs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addQuizTag(tag: QuizTag)

    @Delete
    fun removeQuizTag(tag: QuizTag)
}