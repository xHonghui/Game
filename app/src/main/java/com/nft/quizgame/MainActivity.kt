package com.nft.quizgame

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavArgument
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.nft.quizgame.common.BaseActivity
import com.nft.quizgame.common.BaseActivityDelegate
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.function.main.MainFragment
import com.nft.quizgame.function.quiz.FreeQuizFragment
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.quiz.RacingQuizFragment
import com.nft.quizgame.function.quiz.StageQuizFragment
import com.nft.quizgame.function.user.UserViewModel

class MainActivity : BaseActivity() {

    companion object {
        const val KEY_ENTER_FUNCTION = "key_enter_function"
        const val ENTER_FUNCTION_CHALLENGE = "enter_function_challenge"
        const val ENTER_FUNCTION_STAGE = "enter_function_stage"
        const val ENTER_FUNCTION_NEWBIE = "enter_function_newbie"
        const val ENTER_FUNCTION_STRONGEST_BRAIN = "enter_function_strongest_brain"

        const val KEY_ENTER = "key_enter"
        const val ENTER_PUSH = 3
        const val ENTER_EXTERNAL_DIALOG = 2
        const val ENTER_ICON = 1
    }

    var mEnter = ENTER_ICON

    override fun createDelegate(): BaseActivityDelegate {
        return DefaultActivityDelegate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val findNavController = Navigation.findNavController(this, R.id.test_nav_host_fragment)
        val graph = findNavController.graph
        intent.getStringExtra(KEY_ENTER_FUNCTION)?.let {
            val build = NavArgument.Builder()
                    .setDefaultValue(it)
                    .build()
            graph.addArgument(KEY_ENTER_FUNCTION, build)
        }

        mEnter = intent.getIntExtra(KEY_ENTER, ENTER_ICON)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mEnter = intent?.getIntExtra(KEY_ENTER, ENTER_ICON) ?: ENTER_ICON
        Logcat.d("MainActivity", "mEnter = $mEnter")

        val enterFun = intent?.getStringExtra(KEY_ENTER_FUNCTION) ?: ""

        if (ENTER_EXTERNAL_DIALOG == mEnter) {
            AppViewModelProvider.getInstance().get(UserViewModel::class.java).userData.value
                    ?: return

            val findNavController = Navigation.findNavController(this, R.id.test_nav_host_fragment)
            val fragment = supportFragmentManager.fragments.firstOrNull()
            //处于MainFragment的情况
            if (fragment is NavHostFragment) {
                val fragments = fragment.childFragmentManager.fragments
                if (fragments.isNotEmpty()) {
                    //展示的fragment
                    val showFragment = fragments[0]
                    if (showFragment is MainFragment) {
                        showFragment.onExternalDialogEnter(enterFun)
                        return
                    }

                    when (enterFun) {
                        ENTER_FUNCTION_CHALLENGE -> {
                            if (showFragment is RacingQuizFragment) {
                                return
                            }
                        }
                        ENTER_FUNCTION_STAGE -> {
                            if (showFragment is StageQuizFragment) {
                                return
                            }
                        }
                        ENTER_FUNCTION_NEWBIE -> {
                            if (showFragment is FreeQuizFragment) {
                                val moduleCode = AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java).getEntrance(5)?.moduleCode
                                if (showFragment.moduleCode == moduleCode) {
                                    return
                                }
                            }
                        }
                        ENTER_FUNCTION_STRONGEST_BRAIN -> {
                            if (showFragment is FreeQuizFragment) {
                                val moduleCode = AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java).getEntrance(6)?.moduleCode
                                if (showFragment.moduleCode == moduleCode) {
                                    return
                                }
                            }
                        }
                    }
                }

            }
            //处于其他Fragment的情况
            MainFragment.mKeyEnter = enterFun
            findNavController.popBackStack(R.id.main, false)
        }
    }

}
