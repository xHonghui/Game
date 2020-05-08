package com.nft.quizgame.function.quiz

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.common.State
import com.nft.quizgame.common.dialog.DialogStatusObserver.DIALOG_TAG_PREFIX
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.ANSWER_WRONG
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic.DOUBLE_DONE
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.databinding.FragmentFreeQuizBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.postDelayed
import com.nft.quizgame.function.quiz.bean.CardPropertyBean
import com.nft.quizgame.function.quiz.bean.QuizItemBean
import com.nft.quizgame.function.quiz.view.OptionState
import kotlinx.android.synthetic.main.fragment_free_quiz.*

/**
 * 自由模式Fragment
 */
class FreeQuizFragment : BaseQuizFragment() {

    private val model: FreeQuizViewModel by viewModels {
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
        return inflater.inflate(R.layout.fragment_free_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moduleName = quizPropertyViewModel.getModuleConfig(moduleCode).moduleName
        val binding = FragmentFreeQuizBinding.bind(view)
        binding.delegate = actionDelegate as ActionDelegate
        binding.moduleName = moduleName
        binding.totalCoinLayout.coinDisplay = userModel.userData.value!!.coinDisplay
        binding.quizContentLayout.isDoubleCoin = model.isDoubleBonus
        binding.coinPerItemLayout.isDoubleCoin = model.isDoubleBonus
        binding.coinPerItemLayout.coinOfQuizItem = model.coinOfQuizItem
        binding.tipsCard.cardId = R.mipmap.card_tips
        binding.tipsCard.delegate = actionDelegate
        binding.lifecycleOwner = viewLifecycleOwner
        binding.tipsCard.cardAmount = quizPropertyViewModel.getCard(CardPropertyBean.TYPE_TIPS, model.getMode())
                .cardAmountDisplay
        if (moduleName == getString(R.string.title_free) || TextUtils.isEmpty(moduleName)) {
            val lp = txt_title.layoutParams as ConstraintLayout.LayoutParams
            lp.topToTop = R.id.total_coin_layout
            lp.bottomToBottom = R.id.total_coin_layout
            txt_title.textSize = 16f
            txt_name.visibility = View.GONE
        } else {
            val lp = txt_title.layoutParams as ConstraintLayout.LayoutParams
            lp.topToTop = R.id.total_coin_layout
            txt_title.textSize = 12f
            txt_name.visibility = View.VISIBLE
        }

        initQuizItems()
    }

    override fun onFragmentEntered(savedInstanceState: Bundle?) {
        super.onFragmentEntered(savedInstanceState)
        model.currentQuizItemData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                model.checkDoubleBonus()
                model.handleCoinOfQuizItem(quizPropertyViewModel.getRule(moduleCode))
            }
        })
    }

    override fun setupViewModel(): BaseQuizViewModel {
        return model
    }

    override fun setupActionDelegate(): BaseActionDelegate<*> {
        return ActionDelegate(this)
    }

    override fun getDialogTag(): String {
        return DIALOG_TAG_PREFIX + "free"
    }

    override fun getStatisticEntrance(): String {
        return "1"
    }

    class ActionDelegate(fragment: FreeQuizFragment) : BaseActionDelegate<FreeQuizFragment>(fragment) {

        override fun makeChoiceAnswer(optionState: OptionState) {
            super.makeChoiceAnswer(optionState)
            val fragment: FreeQuizFragment = getFragment() ?: return

            val itemBean = fragment.model.currentQuizItemData.value as QuizItemBean
            if (itemBean.answer == optionState.answer) {
                optionState.state.set(OptionState.STATE_PRESSED_CORRECT)
                val earnCoin = fragment.model.coinOfQuizItem.value ?: 10
                if (fragment.model.isDoubleBonus.value == true) {
                    fragment.model.stateData.value = Event(State.BlockTouchEvent())
                    postDelayed(500){
                        if (fragment.activity != null && !fragment.requireActivity().isFinishing) {
                            fragment.handleDoubleBonus(itemBean, earnCoin) {
                                fragment.recorder.freeModeRecord.coinsDoubleTimes++
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
                    fragment.startCoinAnimation(earnCoin,false)
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
                fragment.model.markQuizItemAnswered(isCorrect = false, isForceShowNextItem = true)
                postDelayed(300) {
                    if (fragment.activity != null && !fragment.requireActivity().isFinishing) {
                        val answerContent = itemBean.optionList!![itemBean.answer!!]
                        val dialogDesc = "${itemBean.content}\n${fragment.getString(
                                R.string.correct_answer_is)}$answerContent".getStyleSpanString(answerContent,
                                color = Color.parseColor("#35A2FF"),
                                style = Typeface.BOLD,
                                flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                                indexOf = false)
                        QuizSimpleDialog(fragment.requireActivity(), QuizAdConst.DIALOG_BOTTOM_AD_MODULE_ID)
                                .logo(jsonId = R.raw.dialog_logo_answer_failed, width = DrawUtils.dip2px(106f), height = DrawUtils.dip2px(121f))
                                .desc(text = dialogDesc)
                                .confirmButton(R.string.go_on) { it.dismiss() }.show()
                        BaseSeq103OperationStatistic.uploadData(optionCode = ANSWER_WRONG, obj = fragment.moduleCode.toString())
                    }
                }
            }
        }
    }

}