package com.nft.quizgame.common

abstract class BaseActivityDelegate(protected val activity: BaseActivity) {

    lateinit var currentFragment: BaseFragment

    abstract fun onCreate()

    abstract fun onResume()

    abstract fun onPause()

    abstract fun onStop()

    abstract fun onStart()

    abstract fun onDestroy()

    fun dispatchBackPressedEvent(fragment: BaseFragment): Boolean {
        val result: Boolean = fragment.onBackPressed()
        if (result) {
            return true
        }
        val parentFragment = fragment.parentFragment
        if (parentFragment is BaseFragment) {
            if (dispatchBackPressedEvent(parentFragment)) {
                return true
            }
        }
        return false
    }
}