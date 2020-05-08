package com.nft.quizgame.common

import com.nft.quizgame.common.PackageNames.Companion.PACKAGE_NAME


/**
 * 广播类型
 */
class ICustomAction {
    companion object {
        val ACTION_REQUEST_AB_CONFIG = "$PACKAGE_NAME.intent.ACTION_REQUEST_AB_CONFIG"

        val ACTION_REQUEST_AB_CONFIG_COMPLETED =
            "$PACKAGE_NAME.intent.ACTION_REQUEST_AB_CONFIG_COMPLETED"

        val ACTION_UPLOAD_BASIC_STATISTIC = "$PACKAGE_NAME.intent.ACTION_UPLOAD_BASIC_STATISTIC"
        val ACTION_APP_UPDATE_CONFIG = "$PACKAGE_NAME.intent.ACTION_APP_UPDATE_CONFIG"
        val ACTION_AB_CONFIG_REFRESH = "$PACKAGE_NAME.intent.ACTION_AB_CONFIG_REFRESH"

    }
}