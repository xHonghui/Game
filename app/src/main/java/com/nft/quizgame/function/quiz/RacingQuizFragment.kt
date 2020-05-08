package com.nft.quizgame.function.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.common.ad.AdBean
import com.nft.quizgame.common.dialog.DialogStatusObserver.DIALOG_TAG_PREFIX
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.CHALLENGE_FAILED
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.CHALLENGE_SUCCESS
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.OTHER_POPUP_SHOW
import com.nft.quizgame.databinding.FragmentRacingQuizBinding
import com.nft.quizgame.dialog.ChallengeSuccessDialog
import com.nft.quizgame.dialog.QuizRewardDialog
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.function.quiz.bean.QuizItemBean
import com.nft.quizgame.function.quiz.view.OptionState
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean
import com.nft.quizgame.net.bean.SyncDataUploadRequestBean
import com.nft.quizgame.view.CoinPolymericView
import kotlinx.android.synthetic.main.fragment_stage_quiz.*

/**
 * 竞速模式Fragment
 */
class RacingQuizFragment : BaseQuizFragment() {

    companion object {
        const val ENTER_FLAG_NONE = -1 //没挑战记录
        const val ENTER_FLAG_NORMAL = 0 //挑战成功后
    }

    private var needPauseTimer = false
    private var hasPostInitialized = false

    private val model: RacingQuizViewModel by viewModels {
        val param = QuizViewModuleParam().apply {
            this.userData = userModel.userData
            this.globalProperty = globalPropertyViewModel.globalProperty.value!!
            this.cardProperties = quizPropertyViewModel.cardProperties!!
            this.moduleConfig = quizPropertyViewModel.getModuleConfig(moduleCode)
            this.rule = quizPropertyViewModel.getRule(moduleCode)
        }
        QuizViewModelFactory(param)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_racing_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRacingQuizBinding.bind(view)
        binding.targetData = model.targetData
        binding.correctData = model.correctData
        binding.delegate = actionDelegate as ActionDelegate
        binding.totalCoinLayout.coinDisplay = userModel.userData.value!!.coinDisplay
        binding.timeLayout.timeData = model.timeProgress
        binding.lifecycleOwner = viewLifecycleOwner

        val flag = PrivatePreference.getPreference().getValue(PrefConst.KEY_RACING_QUIZ_ENTER_FLAG, ENTER_FLAG_NONE)
        if (savedInstanceState == null && globalPropertyViewModel.getChallengeState() == GlobalPropertyBean.CHALLENGE_STATE_NONE
                && flag == ENTER_FLAG_NORMAL) {
            postInit()
        }
    }

    override fun onFragmentEntered(savedInstanceState: Bundle?) {
        super.onFragmentEntered(savedInstanceState)
        when (globalPropertyViewModel.getChallengeState()) {
            GlobalPropertyBean.CHALLENGE_STATE_NONE -> {
                val flag = PrivatePreference.getPreference()
                        .getValue(PrefConst.KEY_RACING_QUIZ_ENTER_FLAG, ENTER_FLAG_NONE)
                if (flag == ENTER_FLAG_NONE) {
                    //展示游戏介绍
                    QuizSimpleDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                            .setTag(getDialogTag())
                            .logo(R.mipmap.dialog_logo_race_mode_desc)
                            .desc(text = requireContext().getString(R.string.racing_introduction,
                                    quizPropertyViewModel.getRule(moduleCode).racingRule?.limitTime,
                                    quizPropertyViewModel.getRule(moduleCode).racingRule?.target,
                                    quizPropertyViewModel.getRule(moduleCode).racingRule?.obtainCoinDesc()))
                            .confirmButton(R.string.know_it) {
                                it.dismiss()
                            }.onDismiss { _, _ -> postInit() }.show()
                } else {
                    postInit()
                }
                BaseSeq103OperationStatistic.uploadData(optionCode = BaseSeq103OperationStatistic.MODE_GUIDE_SHOW, obj = moduleCode.toString(),
                        entrance = getStatisticEntrance())
            }
            GlobalPropertyBean.CHALLENGE_STATE_FAIL -> {
                showChallengeFailDialog {
                    postInit()
                }
            }
        }

        bottomAdListener = object : AdBean.AdInteractionListenerAdapter() {

            override fun onAdClicked() {
                super.onAdClicked()
                needPauseTimer = true
            }
        }
    }

    private fun postInit() {
        if (hasPostInitialized) {
            return
        }
        hasPostInitialized = true

        model.currentQuizItemData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (!model.isTiming()) {
                    startGame()
                }
            }
        })
        model.timeProgress.observe(viewLifecycleOwner, Observer {
            if (it.time <= 0) {
                model.challengeStateData.value = false
            }
        })

        model.challengeStateData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                model.cancelTime()
                if (it == true) {
                    earnCoins()
                    //展示挑战成功对话框
                    showChallengeSuccessDialog()
                    globalPropertyViewModel.updateChallengeState(GlobalPropertyBean.CHALLENGE_STATE_SUCCESS)
                    PrivatePreference.getPreference().putValue(PrefConst.KEY_RACING_QUIZ_ENTER_FLAG, ENTER_FLAG_NORMAL)
                            .apply()
                    BaseSeq103OperationStatistic.uploadData(optionCode = CHALLENGE_SUCCESS,
                            obj = moduleCode.toString(), entrance = model.timeProgress.value?.time.toString())
                    uploadGameProgress(SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_MANUAL)
                } else {
                    //展示挑战失败对话框
                    showChallengeFailDialog {
                        model.showNextItem()
                        startGame()
                    }
                    BaseSeq103OperationStatistic.uploadData(optionCode = CHALLENGE_FAILED, obj = moduleCode.toString(),
                            entrance = (model.targetData.value!! - model.correctData.value!!).toString())
                    uploadGameProgress(SyncDataUploadRequestBean.GameProgress.UPLOAD_TYPE_MANUAL)
                }
            }
        })
        initQuizItems()
    }

    private fun showChallengeSuccessDialog() {
        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "6", entrance = getStatisticEntrance())
        val coinPolymericView = total_coin_layout as CoinPolymericView
        coinPolymericView.user = userModel.userData.value
        ChallengeSuccessDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID,
                model.coinOfQuizItem.value!!, coinPolymericView.getImageCoinCoordinate(),
                coinPolymericView.coinAnimObserver)
                .setTag(getDialogTag())
                .confirmButton(R.string.know_it) { dialog ->
                    dialog.dismiss()
                }.onDismiss { _, _ ->
                    popBackStack()
                }.show()
    }

    private fun showChallengeFailDialog(rewardCallback: () -> Unit) {
        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "7", entrance = getStatisticEntrance())
        globalPropertyViewModel.updateChallengeState(GlobalPropertyBean.CHALLENGE_STATE_FAIL)
        QuizRewardDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID, QuizAdConst.CHALLENGE_REWARD_AD_MODULE_ID
                , getStatisticEntrance())
                .setTag(getDialogTag())
                .logo(R.mipmap.dialog_logo_failed)
                .title(R.string.challenge_fail)
                .desc(text = requireContext().getString(R.string.challenge_fail_desc,
                        quizPropertyViewModel.getRule(moduleCode).racingRule?.obtainCoinDesc()))
                .rewardButton(R.string.challenge_again, adCloseCallback = { dialog, isRewardGained ->
                    if (isRewardGained) {
                        dialog.dismiss()
                        globalPropertyViewModel.updateChallengeState(GlobalPropertyBean.CHALLENGE_STATE_NONE)
                        rewardCallback()
                    }
                }).cancelButton(R.string.give_up) { dialog ->
                    dialog.dismiss()
                }.onDismiss { _, isConfirmClicked ->
                    if (!isConfirmClicked) {
                        popBackStack()
                    }
                }.show()
    }

    override fun onPause() {
        super.onPause()
        if (needPauseTimer) {
            needPauseTimer = false
            model.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (model.isPaused) {
            model.resume()
        }
    }

    private fun startGame() {
        model.startTime()
    }

    override fun setupViewModel(): BaseQuizViewModel {
        return model
    }

    override fun setupActionDelegate(): BaseActionDelegate<*> {
        return ActionDelegate(this)
    }

    override fun getDialogTag(): String {
        return DIALOG_TAG_PREFIX + "race"
    }

    override fun getStatisticEntrance(): String {
        return "3"
    }

    class ActionDelegate(fragment: RacingQuizFragment) : BaseActionDelegate<RacingQuizFragment>(fragment) {

        override fun back() {
            val fragment: RacingQuizFragment = getFragment() ?: return
            //展示退出确认对话框
            BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "9", entrance = fragment.getStatisticEntrance())
            QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                    .setTag(fragment.getDialogTag())
                    .logo(R.mipmap.dialog_logo_racing_no_finish)
                    .title(R.string.racing_exit_dialog_title)
                    .desc(text = fragment.getString(R.string.racing_exit_dialog_desc,
                            fragment.quizPropertyViewModel.getRule(fragment.moduleCode).racingRule?.obtainCoinDesc()))
                    .confirmButton(R.string.go_on) {
                        it.dismiss()
                    }.cancelButton(R.string.play_next_time) {
                        it.dismiss()
                    }.onDismiss { _, isConfirmClicked -> if (!isConfirmClicked) fragment.popBackStack() }.show()
        }

        override fun makeChoiceAnswer(optionState: OptionState) {
            super.makeChoiceAnswer(optionState)
            val fragment: RacingQuizFragment = getFragment() ?: return
            val itemBean = fragment.model.currentQuizItemData.value as QuizItemBean
            if (itemBean.answer == optionState.answer) {
                optionState.state.set(OptionState.STATE_PRESSED_CORRECT)
                fragment.model.markQuizItemAnswered(true)
            } else {
                optionState.state.set(OptionState.STATE_PRESSED_INCORRECT)
                fragment.choiceBinding?.optionGroup?.optionStateList?.let { optionList ->
                    optionList.forEachIndexed { index, optionState ->
                        if (index == itemBean.answer) {
                            optionState.state.set(OptionState.STATE_TIPS_CORRECT)
                        }
                    }
                }
                fragment.model.markQuizItemAnswered(isCorrect = false, isForceShowNextItem = true)
            }
        }
    }

    private fun earnCoins() {
        model.handleCoinOfQuizItem(quizPropertyViewModel.getRule(moduleCode))
        val earnCoin = model.coinOfQuizItem.value!!
        userModel.addUserCoin(earnCoin)
    }

}