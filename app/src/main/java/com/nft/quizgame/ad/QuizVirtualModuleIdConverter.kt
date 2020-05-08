package com.nft.quizgame.ad

import android.content.Context
import com.nft.quizgame.R
import com.nft.quizgame.common.ad.VirtualModuleIdConverter

class QuizVirtualModuleIdConverter : VirtualModuleIdConverter {
    override fun convertToVirtualModuleId(context: Context, moduleId: Int): Int {
        return when (moduleId) {
            QuizAdConst.SPLASH_AD_MODULE_ID -> context.resources.getInteger(R.integer.diff_splash_ad_module_id)
            QuizAdConst.QUIZ_BOTTOM_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_quiz_bottom_ad_module_id)
            QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_dialog_bottom_ad_module_id)
            QuizAdConst.DOUBLE_BONUS_REWARD_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_double_bonus_reward_ad_module_id)
            QuizAdConst.GO_ON_STAGE_REWARD_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_go_on_stage_reward_ad_module_id)
            QuizAdConst.CHALLENGE_REWARD_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_challenge_reward_ad_module_id)
            QuizAdConst.EXIT_QUIZ_AD_MODULE_ID -> context.resources.getInteger(R.integer.diff_exit_quiz_ad_module_id)
            QuizAdConst.ENVELOPE_REWARD_AD_MODULE_ID -> context.resources.getInteger(
                    R.integer.diff_envelope_reward_ad_module_id)
            else -> -1
        }
    }
}