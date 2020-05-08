package com.nft.quizgame.function.user

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.R
import com.nft.quizgame.TestActivity
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.databinding.FragmentLoginBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.toast
import com.nft.quizgame.statistic.Statistic103
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.loading_view.*


class LoginFragment : BaseAppFragment() {

    companion object {
        const val KEY_POP_BACK_TO_MAIN = "key_pop_back_to_main"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statistic103.uploadLoginGuideShow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentLoginBinding.bind(view)
        val actionDelegate = ActionDelegate()
        bind.delegate = actionDelegate
        bind.lifecycleOwner = viewLifecycleOwner

        if (com.nft.quizgame.BuildConfig.DEBUG) {
            tv_test.visibility = View.VISIBLE
        }

        configAgreement()

        userModel.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE
                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE

                        if(state.errorCode == ErrorCode.ALI_PAY_AUTH_ERROR){
                            toast(requireContext(),state.message?:"")
                            return@Observer
                        }

                        val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                        //未知错误
                        QuizSimpleDialog(requireActivity()).title(errorInfo.titleId)
                                .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                    dialog.dismiss()
                                    val event = state.event
                                    if (event !is Int) {
                                        return@confirmButton
                                    }
                                    if(event == UserViewModel.TYPE_LOADING_REGISTER_ALIPAY){
                                        actionDelegate.alipayLogin()
                                    }else if(event == UserViewModel.TYPE_LOADING_REGISTER_VISITOR){
                                        actionDelegate.touristsLogin()
                                    }

                                }
                                .show()

                    }
                    else -> {
                    }
                }
            }
        })


        userModel.userData.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                return@Observer
            }

            val action = arguments?.getBoolean(KEY_POP_BACK_TO_MAIN, false)
            if (action == true) {
                popBackStack(R.id.main, false)
            } else {
                navigate(R.id.action_splash_to_main)
            }
        })


    }

    private fun configAgreement() {
        val string = getString(R.string.protocol_statement)
        val userAgreement = getString(R.string.user_agreement)
        val privacyAgreement = getString(R.string.privacy_agreement)
        val spannableString = SpannableString(string)
        val indexOfStartUserAgreement = string.indexOf(userAgreement)
        val indexOfEndUserAgreement = string.indexOf(userAgreement) + userAgreement.length

        val indexOfStartPrivacyAgreement = string.indexOf(privacyAgreement)
        val indexOfEndPrivacyAgreement = string.indexOf(privacyAgreement) + privacyAgreement.length
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                Statistic103.uploadAgreementClick(1)
                AppUtils.openBrowser(requireContext(), getString(R.string.diff_config_user_agreement))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#B0C8FF")
                ds.clearShadowLayer()
            }


        }, indexOfStartUserAgreement, indexOfEndUserAgreement, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(p0: View) {
                        Statistic103.uploadAgreementClick(2)
                        AppUtils.openBrowser(requireContext(), getString(R.string.diff_config_privacy_agreement))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.parseColor("#B0C8FF")
                        ds.clearShadowLayer()
                    }

                },
                indexOfStartPrivacyAgreement,
                indexOfEndPrivacyAgreement,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
                StyleSpan(Typeface.BOLD), indexOfStartUserAgreement, indexOfEndUserAgreement,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
                StyleSpan(Typeface.BOLD), indexOfStartPrivacyAgreement, indexOfEndPrivacyAgreement,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        //设置下划线
        //设置下划线
        tv_protocol.text = (spannableString)
        tv_protocol.movementMethod = (LinkMovementMethod.getInstance())
        tv_protocol.highlightColor = (resources.getColor(android.R.color.transparent))//不设置该属性，点击后会有背景色
    }

    override fun onBackPressed(): Boolean {
        activity?.finish()
        return true
    }

    inner class ActionDelegate {

        fun test() {
            startActivity(Intent(requireContext(), TestActivity::class.java))
        }

        fun skip() {
            userModel.registerVisitor(true)
        }


        fun touristsLogin() {
            userModel.registerVisitor()
        }

        fun alipayLogin() {
            userModel.registerAliPay(requireActivity())
        }

        fun phoneLogin() {
            Statistic103.uploadLoginoptionClick(2)
            navigate(R.id.action_to_login_phone, arguments)
        }
    }


}