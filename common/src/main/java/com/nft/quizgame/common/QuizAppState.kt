package com.nft.quizgame.common

import android.app.Application
import android.content.Context

object QuizAppState {

    private lateinit var app: Application
    private lateinit var facade: QuizAppFacade

    fun init(app: Application, facade: QuizAppFacade) {
        this.app = app
        this.facade = facade
    }

    fun getApplication(): Application = app
    fun getContext(): Context = app.applicationContext
    fun getFacade(): QuizAppFacade = facade
}