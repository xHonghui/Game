package com.nft.quizgame.external.bean

import android.graphics.Color
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.MainActivity
import com.nft.quizgame.R
import com.nft.quizgame.function.user.UserViewModel

class NewbieZoneBean:BaseExternalDialogBean(7,1) {
    override fun getTitle(): String {
        return getString(R.string.newbie_zone)
    }

    override fun getContent(): CharSequence {
        val string = getString(R.string.newbie_zone_content)
        val diff = getString(R.string.newbie_zone_content_diff)

        return getDiffStr(string,Color.BLUE,diff)
    }

    override fun getBtnText(): String {
        return getString(R.string.newbie_zone_content_btn)
    }

    override fun specialConditions(): Boolean {
        val value = AppViewModelProvider.getInstance().get(UserViewModel::class.java)
            .userData.value?.coinInfoData?.value
        val existingCoin =value ?.existingCoin?:0
        return existingCoin < 1000
    }

    override fun timeInterval(): Long {
        return -1
    }

    override fun clickFrequencyClaim(): Int {
        return -1
    }

    override fun frequencyLimit(): Int {
        return 1
    }

    override fun enter(): String? {
        return MainActivity.ENTER_FUNCTION_NEWBIE
    }
}