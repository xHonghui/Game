package com.nft.quizgame.function.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.R
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.databinding.FragmentLoginPhoneVerificationBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.toast
import com.nft.quizgame.function.user.LoginFragment.Companion.KEY_POP_BACK_TO_MAIN
import com.wynsbin.vciv.VerificationCodeInputView.OnInputListener
import kotlinx.android.synthetic.main.fragment_login_phone_verification.*
import kotlinx.android.synthetic.main.loading_view.*


class LoginPhoneVerificationFragment : BaseAppFragment() {

    companion object {
        const val KEY_PHONE_NUMBER = "key_phone_number"
    }

    val loginPhoneModel: LoginPhoneViewModel by lazy {
        ViewModelProvider(activity as AppCompatActivity).get(LoginPhoneViewModel::class.java)
    }


    lateinit var mPhoneNumber: String
    lateinit var mCode: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_phone_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bind = FragmentLoginPhoneVerificationBinding.bind(view)
        val actionDelegate = ActionDelegate()
        bind.delegate = actionDelegate

        val string = arguments?.getString(KEY_PHONE_NUMBER)
        string?.let {
            mPhoneNumber = it
        }
        tv_des.text = getString(R.string.verification_code_already_send_symbol, mPhoneNumber)

        context?.let {
            //调起键盘
            val inputMethodManager = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(et_verification_code, InputMethodManager.SHOW_FORCED)
        }

        loginPhoneModel.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE
                        toast(requireContext(), R.string.get_verification_code_success)
                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE
                        when (state.errorCode) {
                            ErrorCode.VERIFICATION_CODE_LIMIT -> {
                                toast(requireContext(), getString(R.string.error_verification_code_limit))
                            }
                            ErrorCode.GET_VERIFICATION_CODE_FAIL -> {
                                toast(requireContext(), getString(R.string.get_verification_code_fail))
                            }
                            else -> {
                                val errorInfo = ErrorInfoFactory.getErrorInfo(state.errorCode)
                                //未知错误
                                QuizSimpleDialog(requireActivity()).title(errorInfo.titleId)
                                        .desc(errorInfo.descId).confirmButton(R.string.retry) { dialog ->
                                            dialog.dismiss()
                                            actionDelegate.getVerificationCode()
                                        }
                                        .show()
                            }
                        }

                    }
                }
            }
        })

        userModel.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                val isVerificationCode = state.event == UserViewModel.TYPE_LOADING_REGISTER_PHONE
                when (state) {
                    is State.Loading -> {
                        if (isVerificationCode) {
                            //校验中验证码不可修改
                            et_verification_code.isEnabled = false

                            tv_status.setText(R.string.checking)
                            tv_status.visibility = View.VISIBLE

                        } else {
                            loading_view.visibility = View.VISIBLE
                        }
                    }
                    is State.Success -> {

                        if (isVerificationCode) {
                            //验证成功，登录成功
                            Logcat.d("LoginPhoneVerificationFragment", "验证成功，登录成功")
                        } else {
                            loading_view.visibility = View.INVISIBLE
                        }
                    }
                    is State.Error -> {
                        if (isVerificationCode) {
                            //验证失败
                            tv_status.setText(R.string.verification_code_not_match)
                            et_verification_code.isEnabled = true
                        } else {
                            loading_view.visibility = View.INVISIBLE

                            if (state.errorCode == ErrorCode.ALI_PAY_AUTH_ERROR) {
                                toast(requireContext(), state.message ?: "")
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
                                        if (event == UserViewModel.TYPE_LOADING_REGISTER_ALIPAY) {
                                            actionDelegate.alipayLogin()
                                        } else if (event == UserViewModel.TYPE_LOADING_REGISTER_VISITOR) {
                                            actionDelegate.touristsLogin()
                                        }

                                    }
                                    .show()

                        }

                    }
                }


            }
        })


        loginPhoneModel.countDownTimerData.observe(viewLifecycleOwner, Observer {

            if (it == 0L) {
                tv_reacquire_verification_code.text = getString(R.string.reacquire_verification_code)

                tv_reacquire_verification_code.isEnabled = true
            } else {
                tv_reacquire_verification_code.text = getString(R.string.next_get_verification_code_hint_symbol, it / 1000)
                tv_reacquire_verification_code.isEnabled = false
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

        et_verification_code.setOnInputListener(object : OnInputListener {
            override fun onComplete(code: String) {
                mCode = code
                userModel.registerPhone(mPhoneNumber, mCode)
            }

            override fun onInput() {}
        })


    }


    inner class ActionDelegate {

        fun getVerificationCode() {
            loginPhoneModel.sendVerificationCode(mPhoneNumber)
        }


        fun touristsLogin() {
            userModel.registerVisitor()
        }

        fun alipayLogin() {
            userModel.registerAliPay(requireActivity())
        }


        fun back() {
            navigateUp()
        }
    }


}