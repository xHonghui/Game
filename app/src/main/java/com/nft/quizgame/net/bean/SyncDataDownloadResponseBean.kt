package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean

class SyncDataDownloadResponseBean : BaseResponseBean() {

    var data: SyncDataDownloadDTO? = null

    class SyncDataDownloadDTO {
        @SerializedName("challenge_today")
        var challengeToday: Int = GlobalPropertyBean.CHALLENGE_STATE_NONE
        @SerializedName("main_mode_progress")
        var mainModeProgress: Int = 0
    }
}