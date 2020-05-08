package com.nft.quizgame.common

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.ext.post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


abstract class BaseFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    private lateinit var actDelegate: BaseActivityDelegate
    protected var isTransiting = false
    /**
     * 该方法在切换动画完成时调用，如没有切换动画在onViewCreated后调用
     */
    open fun onFragmentEntered(savedInstanceState: Bundle?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        actDelegate = (requireActivity() as BaseActivity).activityDelegate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            post { onFragmentEntered(savedInstanceState) }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim > 0) {
            val animation = AnimationUtils.loadAnimation(activity, nextAnim)
            animation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    isTransiting = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (enter) {
                        onFragmentEntered(null)
                    }
                    isTransiting = false
                }
            })
            return animation
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onResume() {
        super.onResume()
        actDelegate.currentFragment = this
    }


    /**fragment拦截返回键返回true，不拦截返回false*/
    open fun onBackPressed(): Boolean {
        return false
    }

    fun navigateUp() {
        if (!isDetached) {
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                Logcat.e("Exception", e.message)
            }

        }
    }

    fun navigate(actionId: Int, args: Bundle? = null, navOption: NavOptions? = null, navigatorExtras: Navigator.Extras? = null) {
        var finalNavOption: NavOptions? = navOption
        if (finalNavOption == null) {
            val currentDest = findNavController().currentDestination
            if (currentDest != null) {
                val action = currentDest.getAction(actionId)
                if (action != null && action.navOptions != null) {
                    if (action.navOptions!!.popUpTo != -1 || action.navOptions!!.enterAnim != -1 || action.navOptions!!.exitAnim != -1
                            || action.navOptions!!.popEnterAnim != -1 || action.navOptions!!.popExitAnim != -1) {
                        finalNavOption = action.navOptions
                    }
                }
            }
        }
        if (finalNavOption == null) {
            finalNavOption = navOptions {
                anim {
                    enter = R.anim.fragment_open_enter
                    exit = R.anim.fragment_open_exit
                    popEnter = R.anim.fragment_close_enter
                    popExit = R.anim.fragment_close_exit
                }
            }
        }
        if (!isDetached) {
            try {
                findNavController().navigate(actionId, args, finalNavOption, navigatorExtras)
            } catch (e: Exception) {
                Logcat.e("Exception", e.message)
            }
        }
    }

    open fun popBackStack(destinationId: Int? = null, inclusive: Boolean? = null): Boolean {
        try {
            return if (destinationId != null && inclusive != null) {
                findNavController().popBackStack(destinationId, inclusive)
            } else {
                findNavController().popBackStack()
            }
        } catch (e: Exception) {
            Logcat.e("Exception", e.message)
        }
        return false
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}