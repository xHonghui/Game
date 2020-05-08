package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class ModuleResponseBean : BaseResponseBean() {

    var data: ModuleConfigDTO? = null

    class ModuleConfigDTO {
        @SerializedName("module_config")
        var moduleConfigs: List<ModuleConfig>? = null
    }
}