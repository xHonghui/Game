package com.nft.quizgame.common.dialog

/**
 *
 * @author yangguanxiang
 */
interface IDialog<T> {
    fun isActivityFinishing(): Boolean
    fun doShow()
    fun isShowing(): Boolean
    fun doDismiss()
    fun setTag(tag: Any): T
    fun getTag(): Any
    fun ignoreCurrentDialog()
    fun showPriority():Int
}
