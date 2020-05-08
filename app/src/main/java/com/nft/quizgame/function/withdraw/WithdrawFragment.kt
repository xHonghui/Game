package com.nft.quizgame.function.withdraw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.R
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.databinding.FragmentWithdrawBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.function.user.bean.UserBean
import com.nft.quizgame.statistic.Statistic103
import kotlinx.android.synthetic.main.fragment_withdraw.*
import java.util.*

class WithdrawFragment : BaseAppFragment() {

    companion object {
        const val KEY_CURRENT_WITHDRAW_ITEM = "key_current_withdraw_item"
        const val KEY_IS_FIRST_TIME_WITHDRAW = "key_is_first_time_withdraw"
    }


    private val withdrawViewModel: WithdrawViewModel by lazy {
        ViewModelProvider(activity as AppCompatActivity).get(WithdrawViewModel::class.java)
    }

    private lateinit var withdrawItemAdapter: WithdrawItemAdapter
    private val mWithdrawItems = ArrayList<WithdrawItem>()
    private var mCurrentWithdrawItem: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = userModel.userData.value
        val gold: Int
        if (user == null || user.coinInfoData.value == null) {
            gold = 0
        } else {
            gold = user.coinInfoData.value!!.existingCoin
        }
        Statistic103.uploadWalletShow(gold)

        withdrawViewModel.withdrawListLiveData.value = null
        withdrawViewModel.getWithdrawList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_withdraw, container, false)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            mCurrentWithdrawItem = it.getInt(KEY_CURRENT_WITHDRAW_ITEM, 0)
            val isFirstTimeWithdraw = it.getBoolean(KEY_IS_FIRST_TIME_WITHDRAW, true)
            if (isFirstTimeWithdraw && withdrawViewModel.isTodayWithdraw()) {
                //销毁时是首次提现，回来时已经提现过，索引要减1
                mCurrentWithdrawItem = Math.max(mCurrentWithdrawItem - 1, 0)
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentWithdrawBinding.bind(view)
        bind.delegate = ActionDelegate()
        bind.lifecycleOwner = viewLifecycleOwner

        //设置金币
        tv_gold.text = "0"
        tv_gold_convert.text = getString(R.string.gold_convert_money_symbol, 0.toString())
        userModel.userData.observe(viewLifecycleOwner, Observer<UserBean> { user ->
            configUserGold(user)
        })

        withdrawItemAdapter = WithdrawItemAdapter()
        recycler_view.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        recycler_view.adapter = withdrawItemAdapter

        withdrawItemAdapter.setData(mWithdrawItems)
        withdrawItemAdapter.checkListener = object : WithdrawItemAdapter.OnCheckListener {
            override fun onCheck(data: List<WithdrawItem?>?, position: Int) {
                mCurrentWithdrawItem = position
                configUserGold(userModel.userData.value)
            }

        }


        //获取数据
        withdrawViewModel.withdrawListLiveData.observe(viewLifecycleOwner,
                Observer<List<WithdrawItem>> { t ->
                    mWithdrawItems.clear()
                    if (t != null) {
                        mWithdrawItems.addAll(t)

                        if (mCurrentWithdrawItem >= mWithdrawItems.size) {
                            mCurrentWithdrawItem = mWithdrawItems.size - 1
                        }

                        withdrawItemAdapter.checkItem(mCurrentWithdrawItem)
                    } else {
                        withdrawItemAdapter.notifyDataSetChanged()
                    }
                })

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
                    }
                    is State.Error -> {
                        //Hide progressBar
                        loading_view.visibility = View.INVISIBLE
                        when (state.errorCode) {
                            ErrorCode.NETWORK_ERROR -> {
                                val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                                QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                                        .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                            dialog.dismiss()
                                            withdrawViewModel.getWithdrawList()
                                        }
                                        .cancelButton(R.string.cancel) { dialog ->
                                            dialog.dismiss()
                                            navigateUp()
                                        }.show()
                            }
                            else -> {
                                val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                                QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                                        .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                            dialog.dismiss()
                                            withdrawViewModel.getWithdrawList()
                                        }.show()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun configUserGold(user: UserBean?) {
        if (user == null || user.coinInfoData.value == null || user.coinInfoData.value!!.existingCoin == 0) {
            tv_gold.text = "0"
            tv_gold_convert.text = getString(R.string.gold_convert_money_symbol, 0.toString())
        } else {
            tv_gold.text = user.coinInfoData.value!!.existingCoin.toString()
            var scale = 10000
            if (mWithdrawItems.size > mCurrentWithdrawItem) {
                val currentItem = mWithdrawItems[mCurrentWithdrawItem]
                scale = (currentItem.gold / currentItem.money).toInt()
            }


            val format = String.format(Locale.CHINA, "%.2f", user.coinInfoData.value!!.existingCoin * 1f / scale)
            tv_gold_convert.text = getString(R.string.gold_convert_money_symbol, format)

        }
    }

    private fun showWithdrawLimit() {
        Statistic103.uploadWithdrawfeedbackShow(5)
        val errorInfo = ErrorInfoFactory.getErrorInfo(ErrorCode.WITHDRAW_FREQUENCY_LIMIT)
        QuizSimpleDialog(requireActivity()).logo(errorInfo.imageId).title(errorInfo.titleId)
                .closeButton { dialog ->
                    dialog.dismiss()
                }
                .desc(errorInfo.descId).confirmButton(R.string.go_it) { dialog ->
                    navigateUp()
                    dialog.dismiss()
                }
                .show()
    }

    private fun showCoinInadequate() {
        Statistic103.uploadWithdrawfeedbackShow(2)
        QuizSimpleDialog(requireActivity())
                .title(R.string.withdraw_fail)
                .closeButton { dialog ->
                    dialog.dismiss()
                }
                .desc(R.string.gold_inadequate_tips).confirmButton(R.string.quiz_make_money) { dialog ->
                    Statistic103.uploadEarncoinsClick(2)
                    dialog.dismiss()
                    navigateUp()
                }.show()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mWithdrawItems.isNotEmpty()) {
            outState.putBoolean(KEY_IS_FIRST_TIME_WITHDRAW, mWithdrawItems[0].isNewUser)
        }
        outState.putInt(KEY_CURRENT_WITHDRAW_ITEM, mCurrentWithdrawItem)

    }

    inner class ActionDelegate {
        fun backClick() {
            navigateUp()
        }

        fun makeMoney() {
            Statistic103.uploadEarncoinsClick(1)
            navigateUp()
        }

        fun withdrawNow() {
            if (mWithdrawItems.isEmpty()) {
                return
            }
            val currentItem = mWithdrawItems[mCurrentWithdrawItem]

            val user = userModel.userData.value ?: return

            if (currentItem.gold > user.coinInfoData.value!!.existingCoin) {
                showCoinInadequate()
                return
            }


            if (withdrawViewModel.isTodayWithdraw()) {
                showWithdrawLimit()
                return
            }

            //立即提现10+3

            val cashOutId = currentItem.cashOutId
            val gold = currentItem.gold
            val bundle = Bundle()
            bundle.putInt(WithdrawInfoFillFragment.CASH_OUT_ID, cashOutId)
            bundle.putInt(WithdrawInfoFillFragment.CASH_OUT_GOLD, gold)
            Statistic103.uploadWithdrawClick(gold, 1)
            navigate(R.id.action_to_withdraw_info_fill, bundle)
        }
    }

}