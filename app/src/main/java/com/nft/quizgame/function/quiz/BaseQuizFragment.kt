package com.nft.quizgame.function.quiz

import android.graphics.Color
import android.graphics.Typeface
import android.os.*
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.MainActivity
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.ad.QuizLoadAdParameter
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.ad.AdBean
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.ad.PopupAdDialog
import com.nft.quizgame.common.ad.ShowInternalAdParameter
import com.nft.quizgame.common.buychannel.AppsFlyProxy
import com.nft.quizgame.common.dialog.DialogStatusObserver
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.DOUBLE_GUIDE_SHOW
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.EXCHANGE_CARD_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.EXCHANGE_CARD_USE
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.FREE_MODE_ENTER
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.GAME_QUIT
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.OPTION_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.OTHER_MODULE_ENTER
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.OTHER_POPUP_SHOW
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.QUIZ_RUN_OUT
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.QUIZ_SHOW
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.RACING_MODE_ENTER
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.STAGE_MODE_ENTER
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.TIPS_CARD_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.TIPS_CARD_USE
import com.nft.quizgame.config.ConfigManager
import com.nft.quizgame.config.bean.AdConfigBean
import com.nft.quizgame.databinding.QuizContentLayoutChoiceBinding
import com.nft.quizgame.dialog.QuizDialog.Companion.mNumberTextSize
import com.nft.quizgame.dialog.QuizRewardDialog
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.post
import com.nft.quizgame.ext.toast
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.quiz.bean.QuizItemBean
import com.nft.quizgame.function.quiz.view.OptionState
import com.nft.quizgame.function.sync.GameProgressRecorder
import com.nft.quizgame.function.sync.GlobalPropertyViewModel
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean
import com.nft.quizgame.view.CoinAnimationLayer
import com.nft.quizgame.view.CoinAnimationLayer.Companion.DEFAULT_COIN_ANIMATION_COUNT
import com.nft.quizgame.view.CoinPolymericView
import kotlinx.android.synthetic.main.fragment_free_quiz.*
import kotlinx.android.synthetic.main.loading_view.*
import kotlinx.android.synthetic.main.quiz_coin_per_item_layout.*
import kotlinx.android.synthetic.main.quiz_content_layout.*
import kotlinx.android.synthetic.main.touch_blocking_view.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseQuizFragment : BaseAppFragment() {

    companion object {
        const val KEY_MODULE_CODE = "module_code"
        const val MSG_UPLOAD_PROGRESS_BY_CYCLE = 0
        const val CYCLE_INTERVAL = 1 * 60 * 1000L
    }

    private var pendingExit = PendingExit(false, null, null)
    private var pendingShowAd = false

    protected var currentQuizItemType: Int = -1

    private lateinit var model: BaseQuizViewModel

    protected val recorder = GameProgressRecorder()

    protected var isUploadProgressManually = false

    protected var loadingTime = -1L

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPLOAD_PROGRESS_BY_CYCLE -> {
                    recorder.uploadGameProgress(SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_CYCLE,
                            moduleCode, model.getMode(), userModel.userData.value!!.coinInfoData.value!!.existingCoin)
                    sendEmptyMessageDelayed(MSG_UPLOAD_PROGRESS_BY_CYCLE, CYCLE_INTERVAL)
                }
            }
        }
    }

    protected val globalPropertyViewModel: GlobalPropertyViewModel by lazy {
        AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
    }

    protected val quizPropertyViewModel: QuizPropertyViewModel by lazy {
        AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java)
    }

    protected val coinOptViewModel: CoinOptViewModel by lazy {
        AppViewModelProvider.getInstance().get(CoinOptViewModel::class.java)
    }

    protected var choiceBinding: QuizContentLayoutChoiceBinding? = null

    protected lateinit var actionDelegate: BaseActionDelegate<*>

    protected var hasUsedTipCard = false
    protected var ignoreCurrentQuizItem = false
    var moduleCode = -1
    private var intervalRefreshAdLiveData: MutableLiveData<Boolean>? = null
    protected var bottomAdListener: AdBean.AdInteractionListenerAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moduleCode = arguments?.getInt(KEY_MODULE_CODE) ?: -1
        model = setupViewModel()
        actionDelegate = setupActionDelegate()
        model.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is State.Loading -> {
                        //Show progressBar
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        //Hide progressBar
                        loading_view.visibility = View.INVISIBLE
                    }
                    is State.Error -> {
                        //Hide progressBar
                        loading_view.visibility = View.INVISIBLE
                        val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                        QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                                .setTag(getDialogTag())
                                .desc(errorInfo.descId)
                                .confirmButton(R.string.retry) { dialog ->
                                    dialog.dismiss()
                                }.cancelButton(R.string.cancel) { dialog ->
                                    dialog.dismiss()
                                }.onDismiss { _, isConfirmClicked ->
                                    if (isConfirmClicked) {
                                        model.fetchQuizItems(model.quizItemsData.value?.size ?: 0 > 1)
                                    } else {
                                        if (model.quizItemsData.value == null
                                                || model.quizItemsData.value!!.isEmpty()) {
                                            popBackStack()
                                        }
                                    }
                                }.show()
                    }
                    is State.BlockTouchEvent -> {
                        touch_blocking_view.visibility = View.VISIBLE
                    }
                    is State.UnblockTouchEvent -> {
                        touch_blocking_view.visibility = View.INVISIBLE
                    }
                    else -> {

                    }
                }
            }
        })
    }

    protected fun initQuizItems() {
        val isInit = model.needInitQuizData()
        if (isInit) {
            launch {
                val config = quizPropertyViewModel.getModuleConfig(moduleCode)
                model.fetchQuizItems(isInit = true, isSilence = false, ease = config.easeList, tags = config.tagList)
            }
        }
    }

    override fun onFragmentEntered(savedInstanceState: Bundle?) {
        userModel.userData.value!!.coinAnim = userModel.userData.value!!.coinInfoData.value!!.existingCoin
        userModel.userData.value!!.coinInfoData.value!!.coinDiff = 0
        userModel.userData.value!!.coinInfoData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                recorder.totalEarnCoin += it.coinDiff
            }
        })
        model.quizItemsData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    model.showCurrentItem()
                } else {
                    if (!model.isFetchingQuizItems) {
                        //展示题目耗尽对话框
                        QuizSimpleDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, getStatisticEntrance())
                                .setTag(getDialogTag())
                                .logo(resId = R.mipmap.dialog_logo_no_question_bank)
                                .title(R.string.no_question_dialog_title)
                                .desc(R.string.no_question_dialog_desc)
                                .confirmButton(R.string.know_it) { dialog ->
                                    dialog.dismiss()
                                }.onDismiss { _, _ -> popBackStack() }.show()
                        BaseSeq103OperationStatistic.uploadData(optionCode = QUIZ_RUN_OUT, obj = moduleCode.toString(),
                                entrance = getStatisticEntrance())
                    }
                }
            }
        })

        val loadingStartTime = System.currentTimeMillis()
        loadingTime = -1
        model.currentQuizItemData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                hasUsedTipCard = false
                BaseSeq103OperationStatistic.uploadData(optionCode = QUIZ_SHOW, obj = moduleCode.toString(),
                        entrance = getStatisticEntrance(), tabCategory = it.ease.toString())
            }
            if (currentQuizItemType != it.type) {
                currentQuizItemType = it.type
                quiz_content_container.removeAllViews()
            }
            when (it.type) {
                QuizItemBean.TYPE_SINGLE_CHOICE -> {
                    setupChoiceContentLayout(quiz_content_container.childCount == 0, actionDelegate)
                }
            }
            if (loadingTime == -1L) {
                loadingTime = System.currentTimeMillis() - loadingStartTime
                recorder.onGameStarted()

                val optionCode = when (moduleCode) {
                    quizPropertyViewModel.getEntrance(quizPropertyViewModel.getFreeEntranceId())?.moduleCode
                            ?: -1 -> FREE_MODE_ENTER
                    quizPropertyViewModel.getEntrance(quizPropertyViewModel.getStageEntranceId())?.moduleCode
                            ?: -1 -> STAGE_MODE_ENTER
                    quizPropertyViewModel.getEntrance(quizPropertyViewModel.getRaceEntranceId())?.moduleCode
                            ?: -1 -> RACING_MODE_ENTER
                    else -> OTHER_MODULE_ENTER
                }
                val requireActivity = requireActivity()
                var enter = -1
                if (requireActivity is MainActivity) {
                    enter = requireActivity.mEnter
                }
                BaseSeq103OperationStatistic.uploadData(optionCode = optionCode, obj = moduleCode.toString(), entrance = enter.toString(), tabCategory = loadingTime.toString())
            }
        })

        model.cardAmountChangeData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                it.getContentIfNotHandled()?.let { cardType ->
                    when (model.getMode()) {
                        QuizMode.FREE -> when (cardType) {
                            CardPropertyBean.TYPE_TIPS -> recorder.freeModeRecord.tipsCards++
                            else -> {
                            }
                        }
                        QuizMode.MAIN -> {
                            when (cardType) {
                                CardPropertyBean.TYPE_TIPS -> recorder.mainModeRecord.tipsCards++
                                CardPropertyBean.TYPE_CHANGE -> recorder.mainModeRecord.changeCards++
                                CardPropertyBean.TYPE_ENVELOPE -> recorder.mainModeRecord.envelopeBonus++
                                else -> {
                                }
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        })
        //加载广告
        loadExitAd()
        fl_ad_container.post {
            intervalRefreshAdLiveData = AdController.intervalRefreshAd(60 * 1000L, viewLifecycleOwner)
            intervalRefreshAdLiveData?.observe(viewLifecycleOwner, Observer {
                if (it == true) {
                    loadBottomAd()
                }
            })
        }
        AdController.getAdLoadLiveData(QuizAdConst.QUIZ_BOTTOM_AD_MODULE_ID).observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { adLoadEvent ->
                if (adLoadEvent.adBeanModuleId != QuizAdConst.QUIZ_BOTTOM_AD_MODULE_ID) {
                    return@let
                }
                when (adLoadEvent) {
                    is AdLoadEvent.OnAdLoadSuccess -> {
                        val animLayer = view?.findViewById<CoinAnimationLayer>(R.id.coin_anim_layer)
                        if (animLayer != null && animLayer.isAnimating()) {
                            pendingShowAd = true
                        } else {
                            showBottomAd()
                        }
                    }
                }
            }
        })
    }

    private fun showBottomAd() {
        val pendingAdBean = AdController.getPendingAdBean(QuizAdConst.QUIZ_BOTTOM_AD_MODULE_ID, false)
                ?: return
        val activity = activity ?: return

        link.visibility = View.VISIBLE
        fl_ad_container.visibility = View.VISIBLE
        pendingAdBean.interactionListener = bottomAdListener
        AdController.showInternalAd(ShowInternalAdParameter(activity, pendingAdBean, fl_ad_container))
        intervalRefreshAdLiveData?.value = false
    }

    override fun onPause() {
        super.onPause()
        if (model.getMode() != QuizMode.RACING && !isUploadProgressManually) {
            uploadGameProgress(SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_APP_STATE)
        }
        recorder.uploadQuestionStateStatistics()
    }

    override fun onResume() {
        super.onResume()
        recorder.onlineStartTime = SystemClock.elapsedRealtime()
        if (model.getMode() != QuizMode.RACING) {
            handler.sendEmptyMessageDelayed(MSG_UPLOAD_PROGRESS_BY_CYCLE, CYCLE_INTERVAL)
        }
    }

    override fun popBackStack(destinationId: Int?, inclusive: Boolean?): Boolean {
        val animLayer = view?.findViewById<CoinAnimationLayer>(R.id.coin_anim_layer)
        return if (animLayer != null && animLayer.isAnimating()) {
            pendingExit.isExit = true
            pendingExit.destinationId = destinationId
            pendingExit.inclusive = inclusive
            true
        } else {
            doPopBackStack(destinationId, inclusive)
        }
    }

    private fun doPopBackStack(destinationId: Int?, inclusive: Boolean?): Boolean {
        if (model.getMode() != QuizMode.RACING && !isUploadProgressManually) {
            uploadGameProgress(SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_MANUAL)
        }
        return super.popBackStack(destinationId, inclusive)
    }

    override fun onDestroy() {
        DialogStatusObserver.ignoreAllDialogs(getDialogTag())
        val activity = requireActivity()
        GlobalScope.launch(Main) {
            val configBean = ConfigManager.getInstance().getConfigBean(AdConfigBean.SID) as AdConfigBean
            if (configBean.isExitGameAdOpened) {
                AdController.getPendingAdBean(QuizAdConst.EXIT_QUIZ_AD_MODULE_ID)?.let { adBean ->
                    AdController.showInterstitialAd(activity, adBean)
                }
            }
        }
        BaseSeq103OperationStatistic.uploadData(optionCode = GAME_QUIT, obj = moduleCode.toString(),
                entrance = getStatisticEntrance(), tabCategory = model.totalAnswer.toString(),
                position = model.correctData.value.toString(),
                associatedObj = if (model.getMode() == QuizMode.STAGE || model.getMode() == QuizMode.MAIN) (globalPropertyViewModel.getCurrentStage() - 1).toString() else "")
        super.onDestroy()
    }

    protected fun uploadGameProgress(uploadType: Int) {
        if (uploadType == SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_MANUAL) {
            isUploadProgressManually = true
        }
        handler.removeMessages(MSG_UPLOAD_PROGRESS_BY_CYCLE)
        userModel.userData.value?.coinInfoData?.value?.apply {
            GlobalScope.launch(Main) {
                if (recorder.totalEarnCoin > 0) {
                    val orderId = coinOptViewModel.operateCashIn(coinCode!!, recorder.totalEarnCoin,
                            getString(R.string.end_game_cash_in_desc))
                    if (!TextUtils.isEmpty(orderId)) {
                        recorder.coinOptDetail = SyncDataUploadRequestBean.CoinOptDetail().apply {
                            this.optTime = System.currentTimeMillis()
                            this.optType = SyncDataUploadRequestBean.CoinOptDetail.OPT_CASH_IN
                            this.amount = recorder.totalEarnCoin
                            this.orderId = orderId
                        }
                    }
                    recorder.totalEarnCoin = 0
                }
                recorder.uploadGameProgress(uploadType, moduleCode, model.getMode(),
                        userModel.userData.value!!.coinInfoData.value!!.existingCoin)
            }
        }
    }

    private fun setupChoiceContentLayout(addView: Boolean, delegate: BaseActionDelegate<*>) {
        var needAddView = addView
        if (choiceBinding == null) {
            choiceBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()), R.layout.quiz_content_layout_choice, quiz_content_container,
                    true)
            needAddView = false
        }
        if (needAddView) {
            quiz_content_container.addView(choiceBinding?.root)
        }
        choiceBinding?.quizItemData = model.currentQuizItemData
        choiceBinding?.lifecycleOwner = viewLifecycleOwner
        model.currentQuizItemData.value?.optionList?.let { optionList ->
            model.currentQuizItemData.value?.answer?.let { correctAnswer ->
                choiceBinding?.optionGroup?.setOptions(delegate, optionList, correctAnswer)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (!isTransiting && !touch_blocking_view.isVisible && !loading_view.isVisible) {
            actionDelegate.back()
        }
        return true
    }

    private fun loadExitAd() {
        launch {
            val configBean = ConfigManager.getInstance().getConfigBean(AdConfigBean.SID) as AdConfigBean
            if (configBean.isExitGameAdOpened) {
                AdController.loadAd(QuizLoadAdParameter(requireContext(), QuizAdConst.EXIT_QUIZ_AD_MODULE_ID).apply {
                    entrance = getStatisticEntrance()
                    this.feedViewWidth = PopupAdDialog.sAdWidth
                })
            }
        }
    }

    private fun loadBottomAd() {
        AdController.loadAd(QuizLoadAdParameter(requireContext(), QuizAdConst.QUIZ_BOTTOM_AD_MODULE_ID).apply {
            feedViewWidth = fl_ad_container.width - fl_ad_container.paddingLeft - fl_ad_container.paddingRight
            entrance = getStatisticEntrance()
        })
    }

    private val smallCoinLoc = intArrayOf(0, 0)

    private fun getCoinAnimStartCoordinate(): IntArray {
//        if (smallCoinLoc[0] > 0 && smallCoinLoc[1] > 0) {
//            return smallCoinLoc
//        }
        img_small_coin.getLocationInWindow(smallCoinLoc)
        smallCoinLoc[0] += (img_small_coin.width / 2f).toInt()
        smallCoinLoc[1] += (img_small_coin.height / 2f).toInt()
        return smallCoinLoc
    }

    protected fun startCoinAnimation(earnCoin: Int, isDouble: Boolean) {
        view?.findViewById<CoinAnimationLayer>(R.id.coin_anim_layer)?.let { layer ->
            layer.visibility = View.VISIBLE
            val startLoc = getCoinAnimStartCoordinate()
            val coinPolymericView = total_coin_layout as CoinPolymericView
            coinPolymericView.user = userModel.userData.value
            val endLoc = coinPolymericView.getImageCoinCoordinate()
            val coinCount = if (isDouble) DEFAULT_COIN_ANIMATION_COUNT * 2 else DEFAULT_COIN_ANIMATION_COUNT
            val mod = earnCoin % coinCount
            val bonusPerCoin = earnCoin / coinCount.toFloat()
            val bonusArray = FloatArray(coinCount)
            for (i in 0 until coinCount) {
                if (i == coinCount - 1) {
                    bonusArray[i] = bonusPerCoin + mod
                } else {
                    bonusArray[i] = bonusPerCoin
                }
            }
            if (!layer.animationStateData.hasActiveObservers()) {
                layer.animationStateData.observeForever(coinPolymericView.coinAnimObserver)
            }
            layer.startCoinAnimation(startLoc[0], startLoc[1], endLoc[0], endLoc[1], bonusArray) {
                if (pendingExit.isExit) {
                    pendingExit.isExit = false
                    doPopBackStack(pendingExit.destinationId, pendingExit.inclusive)
                } else if (pendingShowAd) {
                    showBottomAd()
                    pendingShowAd = false
                }
            }
        }
    }

    override fun onDestroyView() {
        view?.findViewById<CoinAnimationLayer>(R.id.coin_anim_layer)?.let { layer ->
            val coinPolymericView = total_coin_layout as CoinPolymericView
            layer.animationStateData.removeObserver(coinPolymericView.coinAnimObserver)
        }
        super.onDestroyView()
    }

    protected fun handleDoubleBonus(itemBean: QuizItemBean, earnCoin: Int, bonusGainedCallback: () -> Unit) {
        //展示激励视频获取双倍金币
        QuizRewardDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID,
                QuizAdConst.DOUBLE_BONUS_REWARD_AD_MODULE_ID, getStatisticEntrance())
                .setTag(getDialogTag())
                .logo(R.mipmap.dialog_logo_double_bonus)
                .title(R.string.double_bonus_chance).desc(R.string.double_bonus_chance_desc)
                .rewardButton(R.string.double_bonus_immediately, onClickCallback = {
                    BaseSeq103OperationStatistic.uploadData(optionCode = BaseSeq103OperationStatistic.DOUBLE_GUIDE_CLICK,
                            obj = moduleCode.toString(), entrance = getStatisticEntrance(),
                            tabCategory = itemBean.ease.toString())
                }, adCloseCallback = { dialog, isRewardGained ->
                    if (isRewardGained) {
                        dialog.dismiss()
                        activity?.let { activity ->
                            BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "10", entrance = getStatisticEntrance())
                            val doubleBonusGainDialog = QuizSimpleDialog(activity, QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, getStatisticEntrance())
                                    .logo(R.mipmap.dialog_logo_double_bonus_gained)
                                    .desc(text = getString(R.string.gain_double_bonus,
                                            "${earnCoin}×2=${earnCoin * 2}"))
                                    .confirmButton(R.string.go_on) {
                                        it.dismiss()
                                    }
                            doubleBonusGainDialog.show()
                            val coinPolymericView = total_coin_layout as CoinPolymericView
                            coinPolymericView.user = userModel.userData.value
                            post {
                                doubleBonusGainDialog.startCoinAnimation(earnCoin * 2,
                                        DEFAULT_COIN_ANIMATION_COUNT * 2,
                                        coinPolymericView.getImageCoinCoordinate(),
                                        coinPolymericView.coinAnimObserver)
                            }
                        }
                        userModel.addUserCoin(earnCoin * 2)
                        model.markQuizItemAnswered(true)
                        bonusGainedCallback()
                    }
                }).cancelButton(R.string.give_up) {
                    it.dismiss()
                }.onDismiss { _, isConfirmClicked ->
                    if (!isConfirmClicked) {
                        userModel.addUserCoin(earnCoin)
                        model.markQuizItemAnswered(true)
                        startCoinAnimation(earnCoin, false)
                    }
                }.show()
        BaseSeq103OperationStatistic.uploadData(optionCode = DOUBLE_GUIDE_SHOW, obj = moduleCode.toString(),
                entrance = getStatisticEntrance(), tabCategory = itemBean.ease.toString())
    }

    abstract fun setupViewModel(): BaseQuizViewModel
    abstract fun setupActionDelegate(): BaseActionDelegate<*>
    abstract fun getDialogTag(): String
    abstract fun getStatisticEntrance(): String

    private data class PendingExit(var isExit: Boolean, var destinationId: Int?, var inclusive: Boolean?)

    abstract class BaseActionDelegate<T : BaseQuizFragment>(fragment: T) {

        private val fragmentRef: WeakReference<T> = WeakReference(fragment)

        protected fun getFragment():T? = fragmentRef.get()

        open fun back() {
            getFragment()?.popBackStack()
        }

        open fun useCard(cardId: Int) {
            val fragment = getFragment() ?: return
            when (cardId) {
                R.mipmap.card_change -> {
                    val card = fragment.quizPropertyViewModel.getCard(CardPropertyBean.TYPE_CHANGE, fragment.model.getMode())
                    //展示使用换题卡对话框
                    if (card.cardAmount > 0) {
                        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "2", entrance = fragment.getStatisticEntrance())
                        QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, fragment.getStatisticEntrance())
                                .setTag(fragment.getDialogTag())
                                .logo(R.mipmap.dialog_logo_change_card_use)
                                .title(text = fragment.requireContext().getString(R.string.change_card_dialog_title,
                                        card.cardAmount).getStyleSpanString(card.cardAmount.toString(),
                                        color = Color.parseColor("#35A2FF"),
                                        style = Typeface.BOLD, size = mNumberTextSize,
                                        flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
                                .desc(R.string.change_card_dialog_desc)
                                .confirmButton(R.string.use) {
                                    it.dismiss()
                                    card.cardAmount--
                                    fragment.quizPropertyViewModel.updateCard(card)
                                    fragment.model.markQuizItemAnswered(isCorrect = false, isForceShowNextItem = true)
                                    fragment.ignoreCurrentQuizItem = true
                                    val itemBean = fragment.model.currentQuizItemData.value
                                    BaseSeq103OperationStatistic.uploadData(optionCode = EXCHANGE_CARD_USE,
                                            obj = fragment.moduleCode.toString(), entrance = fragment.getStatisticEntrance(),
                                            tabCategory = itemBean?.ease?.toString())
                                }.cancelButton(R.string.dont_use) {
                                    it.dismiss()
                                }.show()
                    } else {
                        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "1", entrance = fragment.getStatisticEntrance())
                        QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, fragment.getStatisticEntrance())
                                .setTag(fragment.getDialogTag())
                                .logo(R.mipmap.dialog_logo_change_card_desc)
                                .title(text = fragment.requireContext().getString(R.string.change_card_dialog_title,
                                        card.cardAmount).getStyleSpanString(card.cardAmount.toString(),
                                        color = Color.parseColor("#35A2FF"),
                                        style = Typeface.BOLD, size = mNumberTextSize,
                                        flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
                                .desc(text = fragment.getString(R.string.change_card_dialog_desc2,
                                        fragment.model.getCardInterval(CardPropertyBean.TYPE_CHANGE)))
                                .confirmButton(R.string.go_on) {
                                    it.dismiss()
                                }.show()
                    }
                    BaseSeq103OperationStatistic.uploadData(optionCode = EXCHANGE_CARD_CLICK)
                }
                R.mipmap.card_tips -> {
                    if (fragment.hasUsedTipCard) {
                        toast(fragment.requireContext(), R.string.cannot_use_tips_card)
                        return
                    }
                    val card = fragment.quizPropertyViewModel.getCard(CardPropertyBean.TYPE_TIPS, fragment.model.getMode())
                    if (card.cardAmount > 0) {
                        //展示使用提示卡对话框
                        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "4", entrance = fragment.getStatisticEntrance())
                        QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, fragment.getStatisticEntrance())
                                .setTag(fragment.getDialogTag())
                                .logo(R.mipmap.dialog_logo_tips_card_use)
                                .title(text = fragment.getString(R.string.tips_card_dialog_title,
                                        card.cardAmount).getStyleSpanString(card.cardAmount.toString(),
                                        color = Color.parseColor("#35A2FF"),
                                        style = Typeface.BOLD, size = mNumberTextSize,
                                        flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
                                .desc(R.string.tips_card_dialog_desc)
                                .confirmButton(R.string.use) {
                                    it.dismiss()
                                    card.cardAmount--
                                    fragment.quizPropertyViewModel.updateCard(card)
                                    fragment.model.currentQuizItemData.value?.answer.let { answer ->
                                        fragment.choiceBinding?.optionGroup?.optionStateList?.let { optionList ->
                                            val optionTipsList = ArrayList<OptionState>().apply {
                                                addAll(optionList)
                                            }
                                            optionList.forEachIndexed foreach@{ index, _ ->
                                                if (index == answer) {
                                                    optionTipsList.removeAt(index) //剔除正确的答案
                                                    return@foreach
                                                }
                                            }
                                            optionTipsList.removeAt(Random().nextInt(optionTipsList.size)) //随机剔除一个错误的答案
                                            optionTipsList.forEach { optionState ->
                                                optionState.state.set(OptionState.STATE_TIPS_INCORRECT)
                                            }
                                        }
                                        fragment.hasUsedTipCard = true
                                        val itemBean = fragment.model.currentQuizItemData.value
                                        BaseSeq103OperationStatistic.uploadData(optionCode = TIPS_CARD_USE,
                                                obj = fragment.moduleCode.toString(),
                                                entrance = fragment.getStatisticEntrance(),
                                                tabCategory = itemBean?.ease?.toString())
                                    }
                                }.cancelButton(R.string.dont_use) {
                                    it.dismiss()
                                }.show()
                    } else {
                        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "3", entrance = fragment.getStatisticEntrance())
                        QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, fragment.getStatisticEntrance())
                                .setTag(fragment.getDialogTag())
                                .logo(R.mipmap.dialog_logo_tips_card_desc)
                                .title(text = fragment.requireContext().getString(R.string.tips_card_dialog_title,
                                        card.cardAmount).getStyleSpanString(card.cardAmount.toString(),
                                        color = Color.parseColor("#35A2FF"),
                                        style = Typeface.BOLD, size = mNumberTextSize,
                                        flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
                                .desc(text = fragment.getString(R.string.tips_card_dialog_desc2,
                                        fragment.model.getCardInterval(CardPropertyBean.TYPE_TIPS)))
                                .confirmButton(R.string.go_on) {
                                    it.dismiss()
                                }.show()
                    }
                    BaseSeq103OperationStatistic.uploadData(optionCode = TIPS_CARD_CLICK)
                }
            }
        }

        open fun makeChoiceAnswer(optionState: OptionState) {
            val fragment = getFragment() ?: return
            var isCorrect = false
            fragment.recorder.answerTotal++
            val itemBean = fragment.model.currentQuizItemData.value as QuizItemBean
            if (itemBean.answer == optionState.answer) {
                fragment.recorder.answerCorrectTotal++
                isCorrect = true
            }
            when (optionState.answer) {
                0 -> fragment.recorder.optionSelectTimes.aSelectTimes++
                1 -> fragment.recorder.optionSelectTimes.bSelectTimes++
                2 -> fragment.recorder.optionSelectTimes.cSelectTimes++
                3 -> fragment.recorder.optionSelectTimes.dSelectTimes++
            }
            BaseSeq103OperationStatistic.uploadData(optionCode = OPTION_CLICK, obj = fragment.moduleCode.toString(),
                    entrance = fragment.getStatisticEntrance(), tabCategory = itemBean.ease.toString(),
                    position = if (isCorrect) "1" else "2")
            fragment.recorder.onQuestionAnswer(itemBean.id, isCorrect)

            val preference = PrivatePreference.getPreference()
            val answerCount = preference.getValue(PrefConst.KEY_ANSWER_COUNT, 0)
            if (answerCount > 49) {
                return
            }

            if (answerCount == 49) {
                AppsFlyProxy.uploadQuizDone50()
            }

            preference.putValue(PrefConst.KEY_ANSWER_COUNT, answerCount + 1).apply()

        }
    }

}