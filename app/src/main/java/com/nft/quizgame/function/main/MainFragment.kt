package com.nft.quizgame.function.main

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nft.quizgame.*
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.dialog.DialogStatusObserver
import com.nft.quizgame.common.dialog.DialogStatusObserver.DIALOG_TAG_PREFIX
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.ENTRANCE_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.HOME_ENTER
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.LUCKY_POCKET_SHOW
import com.nft.quizgame.databinding.FragmentMainBinding
import com.nft.quizgame.dialog.NewUserEnvelopeDialog
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.getClickableSpanString
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.post
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.quiz.BaseQuizFragment
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.quiz.bean.RuleCache
import com.nft.quizgame.function.sync.GlobalPropertyViewModel
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.function.update.AppUpdateManger
import com.nft.quizgame.function.update.UpgradeVersionHelper
import com.nft.quizgame.function.update.VersionUpdateConst
import com.nft.quizgame.net.bean.CoinInfo
import com.nft.quizgame.sound.SoundManager
import com.nft.quizgame.version.VersionController
import com.nft.quizgame.view.CoinPolymericView
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.guide_dialog_layout.view.*
import kotlinx.android.synthetic.main.loading_view.loading_view
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainFragment : BaseAppFragment() {

    companion object {
        var mKeyEnter: String? = null
    }

    private val globalViewModel: GlobalPropertyViewModel by lazy {
        AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
    }
    private val quizPropertyViewModel: QuizPropertyViewModel by lazy {
        AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java)
    }

    private val coinOptViewModel: CoinOptViewModel by lazy {
        AppViewModelProvider.getInstance().get(CoinOptViewModel::class.java)
    }

    private val model: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainViewModel(userModel.userData) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mKeyEnter = arguments?.getString(MainActivity.KEY_ENTER_FUNCTION)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    private val actionDelegate by lazy { ActionDelegate() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainBinding.bind(view)


        if (BuildConfig.DEBUG) {
            tv_test.visibility = View.VISIBLE
        }

        AppUpdateManger.mVersionLiveData.observe(viewLifecycleOwner, Observer {
            val versionCode = PrivatePreference.getPreference().getValue(VersionUpdateConst.KEY_SP_APP_VERSION, -1)
            if (versionCode == -1 || (versionCode != -1 && it.versionNumber > versionCode)) {
                UpgradeVersionHelper.getInstance().startUpgradeAndDownloading(requireActivity(), it)
            }
        })

        AppUpdateManger.mDownloadFinishData.observe(viewLifecycleOwner, Observer {
            if (it.isDownloadFinish && it.versionCode > VersionController.currentVersionCode) {
                UpgradeVersionHelper.getInstance().alreadyDownload(requireActivity())
            }
        })

        userModel.userData.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                user.coinInfoData.observe(viewLifecycleOwner, Observer { coinInfo ->
                    if (coinInfo != null) {
                        user.coinAnim = coinInfo.existingCoin
                    }
                })
                binding.totalCoinCashOut.coinDisplay = user.coinDisplay
            }
        })
        globalViewModel.globalProperty.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.challengeState.isChallengeSuccess = globalViewModel.getChallengeState() == GlobalPropertyBean.CHALLENGE_STATE_SUCCESS

                val stageDetail = getString(R.string.main_stage_detail, it.mainModeProgress)
                txt_stage_detail.text = stageDetail.getStyleSpanString(it.mainModeProgress.toString(),
                        color = Color.parseColor("#FF4A4A"),
                        style = Typeface.BOLD,
                        flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        })

        quizPropertyViewModel.propertyDataState.observe(viewLifecycleOwner, Observer {
            var propertyDataState = it.getContentIfNotHandled()
            if (propertyDataState != null) {
                when (propertyDataState) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE
                        bindEntrances(binding)
                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE
                        val errorInfo = ErrorInfoFactory.getErrorInfo(propertyDataState.errorCode)
                        QuizSimpleDialog(requireActivity())
                                .setTag(getDialogTag())
                                .logo(errorInfo.imageId)
                                .title(errorInfo.titleId)
                                .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                    dialog.dismiss()
                                }
                                .onDismiss { _, _ -> quizPropertyViewModel.checkAndReloadQuizPropertyData() }
                                .show()
                    }
                }
            } else {
                propertyDataState = it.peekContent()
                if (propertyDataState is State.Success) {
                    bindEntrances(binding)
                }
            }
        })

        binding.delegate = actionDelegate
        binding.totalCoinCashOut.delegate = actionDelegate
        binding.challengeState.delegate = actionDelegate
        binding.lifecycleOwner = viewLifecycleOwner

        userModel.initAppDataState.observe(viewLifecycleOwner, Observer {
            val requireActivity = requireActivity()
            val enter = if (requireActivity is MainActivity) {
                requireActivity.mEnter
            } else {
                -1
            }
            var state = it.getContentIfNotHandled()
            if (state != null) {
                when (state) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE
                        post {
                            processEnter()
                            checkNewUserBonus()
                        }
                        SoundManager.playMusic(0)
                        BaseSeq103OperationStatistic.uploadData(optionCode = HOME_ENTER, entrance = enter.toString(), tabCategory = state.event?.toString())
                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE
                        if (state.errorCode == ErrorCode.INIT_DATA_ERROR) {
                            showInitErrorDialog()
                        }
                        BaseSeq103OperationStatistic.uploadData(optionCode = HOME_ENTER, entrance = enter.toString(), tabCategory = state.event?.toString())
                    }
                }
            } else {
                state = it.peekContent()
                when (state) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE
                        img_entrance_stage.post {
                            processEnter()
                        }
                        BaseSeq103OperationStatistic.uploadData(optionCode = HOME_ENTER, entrance = enter.toString())
                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE
                        if (state.errorCode == ErrorCode.INIT_DATA_ERROR) {
                            showInitErrorDialog()
                        }
                        BaseSeq103OperationStatistic.uploadData(optionCode = HOME_ENTER, entrance = enter.toString())
                    }
                }
            }
        })

        txt_cash_out_info.setText(11f, 0, Color.WHITE)
        txt_cash_out_info.setTextStillTime(2000)
        txt_cash_out_info.setAnimTime(500)
        model.cashOutInfoData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                txt_cash_out_info.setTextList(it)
            }
        })

        UpgradeVersionHelper.getInstance().registerNetworkStateChange(requireContext())
    }

    private fun showInitErrorDialog() {
        val errorInfo = ErrorInfoFactory.getErrorInfo(ErrorCode.INIT_DATA_ERROR)
        QuizSimpleDialog(requireActivity())
                .setTag(getDialogTag())
                .logo(errorInfo.imageId)
                .title(errorInfo.titleId)
                .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                    dialog.dismiss()
                }
                .onDismiss { _, _ -> userModel.initAllData(false, userModel.userData.value!!) }
                .show()
    }

    private fun bindEntrances(binding: FragmentMainBinding) {
        binding.freeModeEntrance = quizPropertyViewModel.getFreeEntranceId()
        binding.stageModeEntrance = quizPropertyViewModel.getStageEntranceId()
        binding.challengeState.raceModeEntrance = quizPropertyViewModel.getRaceEntranceId()
    }

    private fun processEnter() {
        val keyEnter = mKeyEnter ?: return
        if (quizPropertyViewModel.isFetchingData) {
            quizPropertyViewModel.propertyDataState.observe(viewLifecycleOwner, Observer {
                when (it.peekContent()) {
                    is State.Success -> processEnter()
                }
            })
            return
        }
        mKeyEnter = null
        DialogStatusObserver.ignoreAllDialogs(getDialogTag())
        when (keyEnter) {
            MainActivity.ENTER_FUNCTION_CHALLENGE -> {
                actionDelegate.toGame(quizPropertyViewModel.getRaceEntranceId())
            }
            MainActivity.ENTER_FUNCTION_STAGE -> {
                actionDelegate.toGame(quizPropertyViewModel.getStageEntranceId())
            }
            MainActivity.ENTER_FUNCTION_NEWBIE -> {
                actionDelegate.toGame(5)
            }
            MainActivity.ENTER_FUNCTION_STRONGEST_BRAIN -> {
                actionDelegate.toGame(6)
            }
        }
    }

    private fun checkNewUserBonus() {
        if (userModel.userData.value == null || userModel.userData.value?.coinInfoData?.value == null
                || !quizPropertyViewModel.isDataInitialized) {
            return
        }
        val currentCoin = userModel.userData.value?.coinInfoData?.value!!.existingCoin
        if (globalViewModel.globalProperty.value!!.isNewUser && currentCoin == 0) {
            val coinPolymericView = total_coin_cash_out as CoinPolymericView
            coinPolymericView.user = userModel.userData.value
            val rule = quizPropertyViewModel.getRule(-1)
            if (rule.type == RuleCache.TYPE_NEW_USER_BONUS && rule.newUserBonusRule != null && rule.newUserBonusRule!!.realBonus > 0) {
                globalViewModel.globalProperty.value!!.isNewUser = false
                NewUserEnvelopeDialog(requireActivity(), coinPolymericView.getImageCoinCoordinate(),
                        coinPolymericView.coinAnimObserver,
                        rule.newUserBonusRule!!.realBonus) { bonus ->
                    userModel.addUserCoin(bonus)
                    GlobalScope.launch {
                        coinOptViewModel.operateCashIn(CoinInfo.GOLD_COIN, bonus,
                                getString(R.string.new_user_bonus_cash_in_desc))
                    }
                }.setTag(getDialogTag()).show()
                BaseSeq103OperationStatistic.uploadData(optionCode = LUCKY_POCKET_SHOW)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        txt_cash_out_info.startAutoScroll()
        if (model.isCashOutInfoOutDate()) {
            model.fetchCashOutInfo(requireContext())
        }
        if (globalViewModel.isDataOutDate()) {
            launch {
                try {
                    globalViewModel.fetchPropertyData()
                } catch (e: Exception) {
                    globalViewModel.restoreBackupPropertyData()
                }
            }
        }
        quizPropertyViewModel.checkAndReloadQuizPropertyData()
        post {
            checkNewUserBonus()
        }
        PrivatePreference.getPreference().putValue(PrefConst.KEY_MAIN_LAST_SHOW_TIME, System.currentTimeMillis()).apply()
    }

    override fun onPause() {
        super.onPause()
        txt_cash_out_info.stopAutoScroll()
    }

    override fun onBackPressed(): Boolean {
        requireActivity().finish()
        return true
    }

    override fun onDestroy() {
        UpgradeVersionHelper.getInstance().release(requireActivity())
        super.onDestroy()
        mKeyEnter = null
    }

    inner class ActionDelegate {
        fun test() {
            startActivity(Intent(context!!, TestActivity::class.java))
        }

        fun toGame(entranceId: Int) {
            quizPropertyViewModel.getEntrance(entranceId)?.let { entranceBean ->
                val args = Bundle()
                args.putInt(BaseQuizFragment.KEY_MODULE_CODE, entranceBean.moduleCode)
                val rule = quizPropertyViewModel.getRule(entranceBean.moduleCode)
                when (rule.type) {
                    RuleCache.TYPE_FREE -> {
                        navigate(R.id.action_main_to_free_quiz, args)
                        BaseSeq103OperationStatistic.uploadData(optionCode = ENTRANCE_CLICK,
                                obj = if (entranceId == quizPropertyViewModel.getFreeEntranceId()) "d1" else entranceBean.moduleCode.toString())
                    }
                    RuleCache.TYPE_STAGE -> {
                        navigate(R.id.action_main_to_stage_quiz, args)
                        BaseSeq103OperationStatistic.uploadData(optionCode = ENTRANCE_CLICK,
                                obj = if (entranceId == quizPropertyViewModel.getStageEntranceId()) "d2" else entranceBean.moduleCode.toString())
                    }
                    RuleCache.TYPE_RACING -> {
                        if (globalViewModel.getChallengeState() != GlobalPropertyBean.CHALLENGE_STATE_SUCCESS) {
                            navigate(R.id.action_main_to_racing_quiz, args)
                        } else {
                            QuizSimpleDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                                    .setTag(getDialogTag())
                                    .logo(R.mipmap.dialog_logo_challenge_success)
                                    .logoBg(R.mipmap.dialog_logo_light_bg, true)
                                    .title(R.string.challenge_success_title1)
                                    .desc(R.string.challenge_success_desc)
                                    .confirmButton(R.string.know_it) {
                                        it.dismiss()
                                    }.show()
                        }
                        BaseSeq103OperationStatistic.uploadData(optionCode = ENTRANCE_CLICK,
                                obj = if (entranceId == quizPropertyViewModel.getRaceEntranceId()) "d3" else entranceBean.moduleCode.toString())
                    }
                }
            }
        }

        fun toCashOut() {
            navigate(R.id.action_to_withdraw)
            BaseSeq103OperationStatistic.uploadData(optionCode = ENTRANCE_CLICK, obj = "d4")
        }

        fun openGuideDialog() {
            val customView = LayoutInflater.from(activity).inflate(R.layout.guide_dialog_layout, null)
            val dialog = QuizSimpleDialog(requireActivity()).logo(R.mipmap.dialog_logo_guide)
                    .setTag(getDialogTag())
                    .customView(view = customView)
                    .closeButton {
                        it.dismiss()
                    }.confirmButton(R.string.earn_money) {
                        it.dismiss()
                    }

            val cashOut = getString(R.string.guide_cash_out)
            val guideDesc = getString(R.string.guide_content1_desc, cashOut)
            customView.txt_content1_desc.text = guideDesc.getStyleSpanString(cashOut,
                    color = Color.parseColor("#3882FF"), flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            val goToChallenge = getString(R.string.guide_go_to_challenge)
            val guideDesc2 = getString(R.string.guide_content2_desc2, goToChallenge)
            customView.txt_content2_desc2.text = guideDesc2.getClickableSpanString(goToChallenge,
                    Color.parseColor("#3882FF"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                dialog.dismiss()
                toGame(quizPropertyViewModel.getRaceEntranceId())
            }
            customView.txt_content2_desc2.movementMethod = LinkMovementMethod.getInstance()

            val goToQuiz = getString(R.string.guide_go_to_quiz)
            val guideDesc3 = getString(R.string.guide_content2_desc3, goToQuiz)
            customView.txt_content2_desc3.text = guideDesc3.getClickableSpanString(goToQuiz,
                    Color.parseColor("#3882FF"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                dialog.dismiss()
                toGame(quizPropertyViewModel.getFreeEntranceId())
            }
            customView.txt_content2_desc3.movementMethod = LinkMovementMethod.getInstance()

            dialog.show()
            BaseSeq103OperationStatistic.uploadData(optionCode = ENTRANCE_CLICK, obj = "d5")
        }
    }

    fun onExternalDialogEnter(enter: String) {
        mKeyEnter = enter
        val peekContent = userModel.initAppDataState.value?.peekContent()
        if (peekContent is State.Success) {
            processEnter()
        }
    }

    private fun getDialogTag(): String = DIALOG_TAG_PREFIX + "main"
}