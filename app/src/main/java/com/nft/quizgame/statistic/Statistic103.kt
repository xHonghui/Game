package com.nft.quizgame.statistic

import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.uploadData

object Statistic103 {


    /**
     *启动视频展示
     * obj 上传展示时长（S）
     * enter 1图标 2应用外弹框 3推送通知栏
     * tab
     */
    fun uploadLaunchingShow(obj: Long, enter: Int, tab: String) {
        uploadData(optionCode = "launching_show", obj = obj.toString(), entrance = enter.toString(), tabCategory = tab)
    }

    /**
     * 登录引导页展示
     */
    fun uploadLoginGuideShow() {
        uploadData(optionCode = "loginguide_show")
    }

    /**
     * 登录方式点击
     * 1支付宝 2手机 3游客 4跳过
     */
    fun uploadLoginoptionClick(obj: Int) {
        uploadData(optionCode = "loginoption_click", obj = obj.toString())
    }

    /**
     * 协议入口点击
     * 1用户协议 2隐私协议
     */
    fun uploadAgreementClick(obj: Int) {
        uploadData(optionCode = "agreement_click", obj = obj.toString())
    }

    /**
     * 请求验证码
     * 1成功 2失败
     */
    fun uploadCodeRequest(obj: Int) {
        uploadData(optionCode = "code_request", obj = obj.toString())
    }

    /**
     * 登录
     * 1支付宝 2手机 3游客（含跳过）
     * 1成功 2失败
     * 上传错误码
     */
    fun uploadLogin_done(obj: Int,entrance:Int,tab:String?="") {
        uploadData(optionCode = "login_done", obj = obj.toString(),entrance = entrance.toString(),tabCategory = tab)
    }

    /**
     * 我的钱包界面展示
     * 上传现有金币数
     */
    fun uploadWalletShow(entrance: Int) {
        uploadData(optionCode = "wallet_show", entrance = entrance.toString())
    }

    /**
     * 赚更多按钮点击
     * 1我的钱包界面顶部 2积分不足弹框
     */
    fun uploadEarncoinsClick(entrance: Int) {
        uploadData(optionCode = "earncoins_click", entrance = entrance.toString())
    }

    /**
     * 账号信息填写界面展示
     */
    fun uploadInfopageShow() {
        uploadData(optionCode = "infopage_show")
    }

    /**
     * 提现按钮点击
     * obj 上传提现金额
     * entrance 1我的钱包界面 2信息填写界面
     */
    fun uploadWithdrawClick(obj: Int, entrance: Int) {
        uploadData(optionCode = "withdraw_click", obj = obj.toString(), entrance = entrance.toString())
    }

    /**
     * 提现反馈弹框展示
     * obj 1成功 2积分不足 3网络原因 4库存不足 5超过当天提现次数
     */
    fun uploadWithdrawfeedbackShow(obj: Int) {
        uploadData(optionCode = "withdrawfeedback_show", obj = obj.toString())
    }

    //应用外弹框	弹框展示
    // obj 上传文案序号		externalpopup_show
    // tabCategory 1时段一 2时段二
    fun uploadExternalpopupShow(obj: Int, tab: Int) {
        uploadData(optionCode = "externalpopup_show", obj = obj.toString(), tabCategory = tab.toString())
    }

    //	弹框点击
    //	obj 上传文案序号			externalpopup_click
    //	tabCategory 1时段一 2时段二
    fun uploadExternalpopupClick(obj: Int, tab: Int) {
        uploadData(optionCode = "externalpopup_click", obj = obj.toString(), tabCategory = tab.toString())
    }

    //	弹框设置入口点击
    //	obj 上传文案序号			externalpopup_set
    //tabCategory	1时段一 2时段二
    fun uploadExternalpopupSet(obj: Int, tab: Int) {
        uploadData(optionCode = "externalpopup_set", obj = obj.toString(), tabCategory = tab.toString())
    }

    //	弹框关闭
    //	obj 上传文案序号			externalpopup_close
    //	entrance 1当天关闭 2近期关闭
    //tabCategory	1时段一 2时段二
    fun uploadExternalpopupClose(obj: Int, entrance: Int, tab: Int) {
        uploadData(optionCode = "externalpopup_close", obj = obj.toString(), entrance = entrance.toString(), tabCategory = tab.toString())
    }

}