package com.nft.quizgame.function.quiz

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.common.State
import com.nft.quizgame.common.dialog.DialogStatusObserver.DIALOG_TAG_PREFIX
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.DOUBLE_DONE
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.ENVELOPE_BONUS_CLICK
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.ENVELOPE_BONUS_OBTAIN
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.OTHER_POPUP_SHOW
import com.nft.quizgame.databinding.FragmentStageQuizBinding
import com.nft.quizgame.dialog.QuizEnvelopeDialog
import com.nft.quizgame.dialog.QuizRewardDialog
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.postDelayed
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.quiz.bean.QuizItemBean
import com.nft.quizgame.function.quiz.view.OptionState
import com.nft.quizgame.view.CoinPolymericView
import kotlinx.android.synthetic.main.fragment_stage_quiz.*

/**
 * 闯关模式Fragment
 */
class StageQuizFragment : BaseQuizFragment() {

    companion object {
        const val ENTER_FLAG_NONE = -1 //没进度记录
        const val ENTER_FLAG_NORMAL = 0 //正常点返回键退出后进入
        const val ENTER_FLAG_WRONG = 1 //打错题目后退出后进入
    }

    private var hasPostInitialized = false

    private val model: StageQuizViewModel by viewModels {
        val param = QuizViewModuleParam().apply {
            this.userData = userModel.userData
            this.globalProperty = globalPropertyViewModel.globalProperty.value!!
            this.cardProperties = quizPropertyViewModel.cardProperties!!
            this.moduleConfig = quizPropertyViewModel.getModuleConfig(moduleCode)
            this.rule = quizPropertyViewModel.getRule(moduleCode)
        }
        QuizViewModelFactory(param)
    }

    lateinit var binding: FragmentStageQuizBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stage_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStageQuizBinding.bind(view)
        binding.delegate = actionDelegate as ActionDelegate
        binding.currentStageData = model.currentStageData
        binding.leftToTargetData = model.leftToTargetData
        binding.totalCoinLayout.coinDisplay = userModel.userData.value!!.coinDisplay
        binding.quizContentLayout.isDoubleCoin = model.isDoubleBonus
        binding.coinPerItemLayout.isDoubleCoin = model.isDoubleBonus
        binding.coinPerItemLayout.coinOfQuizItem = model.coinOfQuizItem
        binding.tipsCard.cardId = R.mipmap.card_tips
        binding.tipsCard.delegate = actionDelegate
        binding.changeCard.cardId = R.mipmap.card_change
        binding.changeCard.delegate = actionDelegate
        binding.lifecycleOwner = viewLifecycleOwner
        model.quizItemsData.observe(viewLifecycleOwner, Observer {
            if (it != null && it.isEmpty() && !model.isFetchingQuizItems) {
                //题目耗尽
                PrivatePreference.getPreference().putValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NORMAL)
                        .apply()
            }
        })
        val flag = PrivatePreference.getPreference().getValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NONE)
        if (savedInstanceState == null && (flag == ENTER_FLAG_NONE || flag == ENTER_FLAG_NORMAL)
                && globalPropertyViewModel.getCurrentStage() >= 5) {
            postInit(binding)
        }
    }

    override fun onFragmentEntered(savedInstanceState: Bundle?) {
        super.onFragmentEntered(savedInstanceState)
        PrivatePreference.getPreference()
                .getValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NONE).let { flag ->
                    when (flag) {
                        ENTER_FLAG_NONE, ENTER_FLAG_NORMAL -> {
                            if (globalPropertyViewModel.getCurrentStage() < 5) {
                                //展示闯关模式介绍
                                QuizSimpleDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                                        .setTag(getDialogTag())
                                        .logo(R.mipmap.dialog_logo_stage_mode_desc)
                                        .desc(text = requireContext().getString(R.string.stage_introduction,
                                                quizPropertyViewModel.getRule(moduleCode).stageRule?.envelopeInterval,
                                                quizPropertyViewModel.getRule(moduleCode).stageRule?.envelopeCoin))
                                        .confirmButton(R.string.know_it) {
                                            it.dismiss()
                                        }.onDismiss { _, _ -> postInit(binding) }.show()
                                BaseSeq103OperationStatistic.uploadData(optionCode = BaseSeq103OperationStatistic.MODE_GUIDE_SHOW,
                                        obj = moduleCode.toString(), entrance = getStatisticEntrance())
                            } else {
                                postInit(binding)
                            }
                        }
                        ENTER_FLAG_WRONG -> {
                            model.extractLeftToTarget(
                                    quizPropertyViewModel.getRule(moduleCode).stageRule?.envelopeInterval!!)

                            val title = requireContext().getString(R.string.stage_exit_dialog_title,
                                    globalPropertyViewModel.getCurrentStage())
                            val spanTitle = title.getStyleSpanString(title,
                                    color = Color.parseColor("#FF7F00"),
                                    style = Typeface.BOLD,
                                    flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            // 上一次挑战失败，这次进来先展示激励视频对话框
                            QuizRewardDialog(requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID,
                                    QuizAdConst.GO_ON_STAGE_REWARD_AD_MODULE_ID, getStatisticEntrance())
                                    .setTag(getDialogTag())
                                    .logo(R.mipmap.dialog_logo_stage_mode_desc)
                                    .title(text = spanTitle, textSize = 18f)
                                    .desc(text = requireContext().getString(R.string.stage_exit_dialog_desc,
                                            model.leftToTargetData.value,
                                            quizPropertyViewModel.getRule(moduleCode).stageRule?.envelopeCoin))
                                    .rewardButton(R.string.go_on, adCloseCallback = { dialog, isRewardGained ->
                                        if (isRewardGained) {
                                            dialog.dismiss()
                                            postInit(binding)
                                            PrivatePreference.getPreference()
                                                    .putValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NORMAL)
                                                    .apply()
                                        }
                                    }).cancelButton(R.string.play_next_time) {
                                        it.dismiss()
                                    }.onDismiss { _, isConfirmClicked ->
                                        if (!isConfirmClicked) {
                                            popBackStack()
                                        }
                                    }.show()
                        }
                    }
                }
    }

    private fun postInit(binding: FragmentStageQuizBinding) {
        if (hasPostInitialized) {
            return
        }
        hasPostInitialized = true

        model.currentStageData.observe(viewLifecycleOwner, Observer {
            if (it > 0) {
                globalPropertyViewModel.updateStage(it)
            }
        })
        model.currentQuizItemData.observe(viewLifecycleOwner, Observer {
            if (!ignoreCurrentQuizItem) {
                model.handleStage()
                if (it != null) {
                    model.checkDoubleBonus()
                    model.handleCoinOfQuizItem(quizPropertyViewModel.getRule(moduleCode))
                    model.handleEnvelope()
                }
            }
            ignoreCurrentQuizItem = false
        })
        quizPropertyViewModel.getCard(CardPropertyBean.TYPE_TIPS, model.getMode()).let { cardBean ->
            binding.tipsCard.cardAmount = cardBean.cardAmountDisplay
        }
        quizPropertyViewModel.getCard(CardPropertyBean.TYPE_CHANGE, model.getMode())
                .let { cardBean ->
                    binding.changeCard.cardAmount = cardBean.cardAmountDisplay
                }
        quizPropertyViewModel.getCard(CardPropertyBean.TYPE_ENVELOPE, model.getMode())
                .let { cardBean ->
                    binding.envelopeAmount = cardBean.cardAmountDisplay
                }
        initQuizItems()
    }

    override fun setupViewModel(): BaseQuizViewModel {
        return model
    }

    override fun setupActionDelegate(): BaseActionDelegate<*> {
        return ActionDelegate(this)
    }

    override fun getDialogTag(): String {
        return DIALOG_TAG_PREFIX + "stage"
    }

    override fun getStatisticEntrance(): String {
        return "2"
    }

    class ActionDelegate(fragment: StageQuizFragment) : BaseActionDelegate<StageQuizFragment>(fragment) {

        override fun back() {
            val fragment: StageQuizFragment = getFragment() ?: return
            val title = fragment.getString(R.string.stage_exit_dialog_title,
                    fragment.globalPropertyViewModel.getCurrentStage())
            val spanTitle = title.getStyleSpanString(title,
                    color = Color.parseColor("#FF7F00"),
                    style = Typeface.BOLD,
                    flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            // 展示退出确认对话框
            BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "8", entrance = fragment.getStatisticEntrance())
            QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                    .setTag(fragment.getDialogTag())
                    .logo(R.mipmap.dialog_logo_stage_mode_desc)
                    .title(text = spanTitle,textSize = 18f)
                    .desc(text = fragment.getString(R.string.stage_halfway_exit_dialog_desc,
                            fragment.model.leftToTargetData.value,
                            fragment.quizPropertyViewModel.getRule(fragment.moduleCode).stageRule?.envelopeCoin))
                    .confirmButton(R.string.go_on) {
                        it.dismiss()
                    }.cancelButton(R.string.play_next_time) {
                        it.dismiss()
                    }.onDismiss { _, isConfirmClicked ->
                        if (!isConfirmClicked) {
                            PrivatePreference.getPreference().putValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NORMAL).apply()
                            fragment.popBackStack()
                        }
                    }.show()
        }

        override fun makeChoiceAnswer(optionState: OptionState) {
            super.makeChoiceAnswer(optionState)
            val fragment: StageQuizFragment = getFragment() ?: return
            val itemBean = fragment.model.currentQuizItemData.value as QuizItemBean
            if (itemBean.answer == optionState.answer) {
                optionState.state.set(OptionState.STATE_PRESSED_CORRECT)
                var earnCoin = fragment.model.coinOfQuizItem.value ?: 10
                if (fragment.model.isDoubleBonus.value == true) {
                    fragment.model.stateData.value = Event(State.BlockTouchEvent())
                    postDelayed(500) {
                        if (fragment.activity != null && !fragment.requireActivity().isFinishing) {
                            fragment.handleDoubleBonus(itemBean, earnCoin) {
                                fragment.recorder.mainModeRecord.coinsDoubleTimes++
                                BaseSeq103OperationStatistic.uploadData(optionCode = DOUBLE_DONE,
                                        obj = fragment.moduleCode.toString(), entrance = fragment.getStatisticEntrance(),
                                        tabCategory = itemBean.ease.toString())
                            }
                            fragment.model.stateData.value = Event(State.UnblockTouchEvent())
                        }
                    }
                } else {
                    fragment.userModel.addUserCoin(earnCoin)
                    fragment.model.markQuizItemAnswered(true)
                    fragment.startCoinAnimation(earnCoin, false)
                }
            } else {
                optionState.state.set(OptionState.STATE_PRESSED_INCORRECT)
                fragment.choiceBinding?.optionGroup?.optionStateList?.let { optionList ->
                    optionList.forEachIndexed { index, optionState ->
                        if (index == itemBean.answer) {
                            optionState.state.set(OptionState.STATE_TIPS_CORRECT)
                        }
                    }
                }
                fragment.model.stateData.value = Event(State.BlockTouchEvent())
                PrivatePreference.getPreference().putValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_WRONG).apply()
                postDelayed(500) {
                    if (fragment.activity != null && !fragment.requireActivity().isFinishing) {
                        val title = fragment.getString(R.string.stage_exit_dialog_title,
                                fragment.globalPropertyViewModel.getCurrentStage())
                        val spanTitle = title.getStyleSpanString(title,
                                color = Color.parseColor("#FF7F00"),
                                style = Typeface.BOLD,
                                flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        // 闯关失败，是否继续？
                        BaseSeq103OperationStatistic.uploadData(optionCode = OTHER_POPUP_SHOW, obj = "5", entrance = fragment.getStatisticEntrance())
                        QuizRewardDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID,
                                QuizAdConst.GO_ON_STAGE_REWARD_AD_MODULE_ID, getFragment()?.getStatisticEntrance()?:"")
                                .setTag(fragment.getDialogTag())
                                .logo(R.mipmap.dialog_logo_challenge_failed)
                                .title(text = spanTitle, textSize = 18f)
                                .desc(text = fragment.getString(R.string.stage_exit_dialog_desc,
                                        fragment.model.leftToTargetData.value,
                                        fragment.quizPropertyViewModel.getRule(fragment.moduleCode).stageRule?.envelopeCoin))
                                .rewardButton(R.string.go_on, adCloseCallback = { dialog, isRewardGained ->
                                    if (isRewardGained) {
                                        dialog.dismiss()
                                        fragment.model.markQuizItemAnswered(isCorrect = false, isForceShowNextItem = true)
                                        fragment.ignoreCurrentQuizItem = true
                                        fragment.recorder.mainModeRecord.resurrectionTimes++
                                        PrivatePreference.getPreference()
                                                .putValue(PrefConst.KEY_STAGE_QUIZ_ENTER_FLAG, ENTER_FLAG_NORMAL).apply()
                                    }
                                }).cancelButton(R.string.play_next_time) {
                                    it.dismiss()
                                }.onDismiss { _, isConfirmClicked ->
                                    if (!isConfirmClicked) {
                                        fragment.model.markQuizItemAnswered(false)
                                        fragment.popBackStack()
                                    }
                                }.show()
                        fragment.model.stateData.value = Event(State.UnblockTouchEvent())
                    }
                }
            }
        }

        fun openEnvelope() {
            val fragment: StageQuizFragment = getFragment() ?: return
            fragment.quizPropertyViewModel.getCard(CardPropertyBean.TYPE_ENVELOPE, fragment.model.getMode()).let { card ->
                if (card.cardAmount > 0) {
                    val coinPolymericView = fragment.total_coin_layout as CoinPolymericView
                    coinPolymericView.user = fragment.userModel.userData.value
                    //展示红包对话框
                    QuizEnvelopeDialog(
                            fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID,
                            QuizAdConst.ENVELOPE_REWARD_AD_MODULE_ID,fragment.getStatisticEntrance(),
                            coinPolymericView.getImageCoinCoordinate(),
                            coinPolymericView.coinAnimObserver, fragment.moduleCode, card.cardAmount,
                            fragment.quizPropertyViewModel.getRule(fragment.moduleCode).stageRule?.envelopeCoin!!
                    ) { coin ->
                        fragment.userModel.addUserCoin(coin)
                        card.cardAmount--
                        fragment.quizPropertyViewModel.updateCard(card)
                        BaseSeq103OperationStatistic.uploadData(optionCode = ENVELOPE_BONUS_OBTAIN, obj = fragment.moduleCode.toString())
                    }.setTag(fragment.getDialogTag()).show()
                    BaseSeq103OperationStatistic.uploadData(optionCode = ENVELOPE_BONUS_CLICK,
                            obj = fragment.moduleCode.toString(), entrance = card.cardAmount.toString())
                }
            }
        }
    }
}