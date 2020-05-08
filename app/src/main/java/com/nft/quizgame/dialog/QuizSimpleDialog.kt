package com.nft.quizgame.dialog

import android.app.Activity

open class QuizSimpleDialog(activity: Activity, adModuleId: Int = -1, entrance: String = "") :
        QuizDialog<QuizSimpleDialog>(activity, adModuleId, entrance)