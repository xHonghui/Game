package com.nft.quizgame.function.signin

import androidx.lifecycle.MutableLiveData
import com.android.volley.VolleyError
import com.nft.quizgame.common.dialog.BaseDialog
import com.nft.quizgame.common.dialog.IDialog
import com.nft.quizgame.common.net.RequestCallback
import com.nft.quizgame.function.signin.bean.SignInBean
import com.nft.quizgame.net.NetManager
import com.nft.quizgame.net.bean.SigninRequestBean
import com.nft.quizgame.net.bean.SignInResponseBean
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.suspendCoroutine

object SignInManager {

    private const val dialogCount: Int = 4

    val showDialogCountData = MutableLiveData(dialogCount)

    //缓存红包弹窗、签到弹窗、版本更新弹窗
    private var dialogList = TreeSet(Comparator<BaseDialog<*>> { dialog1, dialog2 ->
        dialog2.showPriority().compareTo(dialog1.showPriority())
    })

    fun addElement(dialog: BaseDialog<*>) {
        if (showDialogCountData.value!! <= 0) {
            dialog.show()
            return
        }
        dialogList.add(dialog)
    }

    fun showAllDialog() {
        for (dialog in dialogList) {
            dialog.show()
        }
        dialogList.clear()
    }

    fun setAlloc(){
        showDialogCountData.value = showDialogCountData.value!! - 1
    }

    fun checkSignIn(signInBean: SignInBean) {
        //todo 1、从外部进入到首页 2、获取签到配置成功 3、当天未签到 4、当天未自动弹出此弹框

    }


}