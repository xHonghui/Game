package com.nft.quizgame.common.dialog

import java.util.*
import kotlin.Comparator

/**
 * Dialog状态观察者，用于Home键事件处理中
 * @author yangguanxiang
 */
object DialogStatusObserver {

    const val DIALOG_TAG_PREFIX = "dialog_tag_"

    private var mDialog: IDialog<*>? = null

    private var pendingDialogs = TreeSet(Comparator<IDialog<*>> { dialog1, dialog2 ->
        dialog2.showPriority().compareTo(dialog1.showPriority())
    })

    fun showDialog(dialog: IDialog<*>) {
        if (isDialogShowing()) {
            pendingDialogs.add(dialog)
        } else {
            if (!dialog.isActivityFinishing()) {
                dialog.doShow()
                onDialogShow(dialog)
            } else {
                showNextDialog()
            }
        }
    }

    fun dismissDialog(dialog: IDialog<*>) {
        dialog.doDismiss()
        onDialogDismiss(dialog)
        showNextDialog()
    }

    private fun showNextDialog() {
        if (pendingDialogs.isNotEmpty()) {
            val nextDialog = pendingDialogs.pollFirst()
            pendingDialogs.remove(nextDialog)
            showDialog(nextDialog)
        }
    }

    fun isDialogShowing(): Boolean {
        return mDialog != null && mDialog!!.isShowing()
    }

    private fun onDialogShow(dialog: IDialog<*>) {
        mDialog = dialog
    }

    private fun onDialogDismiss(dialog: IDialog<*>) {
        if (mDialog === dialog) {
            mDialog = null
        }
    }

    /**
     * 忽略掉当前对话框和等待队列中的对话框。
     * 被忽略掉的对话框直接关闭不回调onDismiss接口方法
     */
    fun ignoreAllDialogs(tag: Any?) {
        if (mDialog != null && (tag == null || mDialog!!.getTag() == tag) && mDialog!!.isShowing()) {
            try {
                mDialog!!.ignoreCurrentDialog()
                mDialog!!.doDismiss()
            } catch (e: Exception) {
            }
            mDialog = null
        }
        pendingDialogs.removeAll { dialog -> tag == null || dialog.getTag() == tag }
        showNextDialog()
    }
}
