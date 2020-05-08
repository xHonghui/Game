package com.nft.quizgame.common.ad

import android.content.Context

interface VirtualModuleIdConverter {
    fun convertToVirtualModuleId(context: Context, moduleId: Int): Int
}