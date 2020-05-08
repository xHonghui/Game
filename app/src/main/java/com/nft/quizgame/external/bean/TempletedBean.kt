package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R

class TempletedBean:BaseExternalDialogBean(0,0) {
    override fun getTitle(): String {
        return getString(R.string.topic_update_title)
    }

    override fun getContent(): CharSequence {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun timeInterval(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clickFrequencyClaim(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun frequencyLimit(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun jump(activity: Activity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}