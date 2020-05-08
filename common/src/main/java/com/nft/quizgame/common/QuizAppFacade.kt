package com.nft.quizgame.common

interface QuizAppFacade {

    fun isFirstRun(): Boolean

    fun getCDays(): Int

    fun getABUser(): String?

    fun getUserAccessToken(): String?

    fun getUserRefreshToken(): String?
}