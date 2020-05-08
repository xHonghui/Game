package com.nft.quizgame.common.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

abstract class BaseDialog<T>(protected val activity: Activity) : Dialog(activity), IDialog<T>,
        LifecycleOwner {

    private val mLifecycleRegistry by lazy { LifecycleRegistry(this) }
    protected var isConfirmClicked = false
    private var tag: Any = ""
    private var isIgnored = false
    //显示优先级，默认为0，值越高越优先显示


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLifecycleRegistry.currentState = Lifecycle.State.CREATED
        if (isFullScreenTransparent()) {
            val params = window?.attributes
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            params?.height = WindowManager.LayoutParams.MATCH_PARENT
            params?.gravity = Gravity.CENTER
            window?.attributes = params
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    abstract fun isFullScreenTransparent(): Boolean
    abstract fun dismiss(invokeSuper: Boolean)

    override fun setTag(tag: Any): T {
        this.tag = tag
        return this as T
    }

    override fun getTag() = tag

    override fun show() {
        DialogStatusObserver.showDialog(this)
        mLifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun dismiss() {
        DialogStatusObserver.dismissDialog(this)
    }

    override fun doShow() {
        super.show()
    }

    override fun doDismiss() {
        super.dismiss()
        mLifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun isActivityFinishing(): Boolean = activity.isFinishing

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    override fun ignoreCurrentDialog() {
        isIgnored = true
    }

    //显示优先级，值越大，越优先显示
    override fun showPriority(): Int {
        return 0
    }

    fun onDismiss(callback: (Dialog, Boolean) -> Unit): BaseDialog<*> {
        setOnDismissListener {
            if (!isIgnored) {
                callback(this, isConfirmClicked)
            }
        }
        return this
    }
}