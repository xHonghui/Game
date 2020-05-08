package com.nft.quizgame.function.withdraw

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.R
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.databinding.FragmentWithdrawInfoFillBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.toast
import com.nft.quizgame.statistic.Statistic103
import kotlinx.android.synthetic.main.fragment_withdraw_info_fill.*

class WithdrawInfoFillFragment : BaseAppFragment() {

    companion object {
        const val CASH_OUT_ID = "cash_out_id"
        const val CASH_OUT_GOLD = "cash_out_gold"
    }

    var mCashOutId = 0
    var mCashOutGold = 0

    val withdrawViewModel: WithdrawViewModel by lazy {
        ViewModelProvider(activity as AppCompatActivity).get(WithdrawViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statistic103.uploadInfopageShow()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_withdraw_info_fill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        assert(arguments != null)

        val bind = FragmentWithdrawInfoFillBinding.bind(view)
        bind.delegate = ActionDelegate()
        bind.lifecycleOwner = viewLifecycleOwner

        arguments?.let {
            mCashOutId = it.getInt(CASH_OUT_ID)
            mCashOutGold = it.getInt(CASH_OUT_GOLD)
        }

        et_account.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            cl_account.isSelected = hasFocus
        }

        et_account_name.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            cl_account_name.isSelected = hasFocus
        }


        withdrawViewModel.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is State.Loading -> {
                        //Show progressBar
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        //Hide progressBar
                        loading_view.visibility = View.INVISIBLE
                        Statistic103.uploadWithdrawfeedbackShow(1)
                        //提现成功，弹窗提示
                        QuizSimpleDialog(requireActivity()).logo(0).title(R.string.withdraw_success)
                                .desc(R.string.withdraw_success_desc).confirmButton(R.string.ok) { dialog ->
                                    dialog.dismiss()
                                }.onDismiss { _, _ ->
                                        navigateUp()
                                }.show()

                    }
                    is State.Error -> {
                        //Hide progressBar
                        loading_view.visibility = View.INVISIBLE
                        when (state.errorCode) {
                            ErrorCode.WITHDRAW_FREQUENCY_LIMIT,
                            ErrorCode.WITHDRAW_INVENTORY_SHORTAGE -> {
                                if(state.errorCode == ErrorCode.WITHDRAW_FREQUENCY_LIMIT){
                                    Statistic103.uploadWithdrawfeedbackShow(5)
                                }else{
                                    Statistic103.uploadWithdrawfeedbackShow(4)
                                }

                                val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                                QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                                        .closeButton { dialog ->
                                            dialog.dismiss()
                                        }
                                        .desc(errorInfo.descId).confirmButton(R.string.go_it) { dialog ->
                                            dialog.dismiss()
                                        }
                                        .onDismiss { dialog, b ->
                                                navigateUp()
                                        }
                                        .show()
                            }

                            else -> {
                                Statistic103.uploadWithdrawfeedbackShow(3)
                                val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                                QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                                        .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                            dialog.dismiss()

                                            val account = et_account.text.toString()
                                            val accountName = et_account_name.text.toString()
                                            withdrawViewModel.requestWithdraw(mCashOutId, mCashOutGold, account, accountName)
                                        }
                                        .cancelButton(R.string.cancel) { dialog ->
                                            dialog.dismiss()
                                            navigateUp()
                                        }.show()
                            }
                        }
                    }
                    else -> {
                    }

                }
            }

        })


    }

    inner class ActionDelegate {

        fun backClick() {
            navigateUp()
        }

        fun withdrawNow() {
            //提现
            val account = et_account.text.toString()
            val accountName = et_account_name.text.toString()
            if (TextUtils.isEmpty(account) || TextUtils.isEmpty(accountName)) {
                toast(context!!, getString(R.string.account_name_cant_empty))
                return
            }

            if (account.length != 11 && !AppUtils.isEmail(account)) {
                toast(context!!, getString(R.string.please_input_correct_account))
                return
            }

            Statistic103.uploadWithdrawClick(mCashOutGold,2)
            withdrawViewModel.requestWithdraw(mCashOutId, mCashOutGold, account, accountName)

        }

    }

}