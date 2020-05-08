package com.nft.quizgame.function.user

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.R
import com.nft.quizgame.common.ErrorCode
import com.nft.quizgame.common.ErrorInfoFactory
import com.nft.quizgame.common.State
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.databinding.FragmentLoginPhoneBinding
import com.nft.quizgame.dialog.QuizSimpleDialog
import com.nft.quizgame.ext.toast
import kotlinx.android.synthetic.main.fragment_login_phone.*
import kotlinx.android.synthetic.main.loading_view.*

class LoginPhoneFragment : BaseAppFragment() {


    val loginPhoneModel: LoginPhoneViewModel by lazy {
        ViewModelProvider(activity as AppCompatActivity).get(LoginPhoneViewModel::class.java)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bind = FragmentLoginPhoneBinding.bind(view)
        val actionDelegate = ActionDelegate()
        bind.delegate = actionDelegate

        loginPhoneModel.countDownTimerData.observe(viewLifecycleOwner, Observer {

            if (it == 0L) {
                tv_login.text = getString(R.string.login)
                configLoginBtnEnable()
            } else {
                tv_login.text = getString(R.string.next_get_verification_code_hint_symbol, it / 1000 )
                configLoginBtnEnable()
            }

        })

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

            val action = arguments?.getBoolean(LoginFragment.KEY_POP_BACK_TO_MAIN, false)
            if (action == true) {
                popBackStack(R.id.main, false)
            } else {
                navigate(R.id.action_splash_to_main)
            }
        })


        loginPhoneModel.stateData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is State.Loading -> {
                        loading_view.visibility = View.VISIBLE
                    }
                    is State.Success -> {
                        loading_view.visibility = View.INVISIBLE

                        Logcat.d("LoginPhoneFragment", "获取验证码成功")
                        val args = Bundle()
                        args.putString(LoginPhoneVerificationFragment.KEY_PHONE_NUMBER, et_phone_number.text.toString())

                        arguments?.let { bundle ->
                            val boolean = bundle.getBoolean(LoginFragment.KEY_POP_BACK_TO_MAIN, false)
                            args.putBoolean(LoginFragment.KEY_POP_BACK_TO_MAIN, boolean)
                        }

                        navigate(R.id.action_to_login_phone_verification, args)

                    }
                    is State.Error -> {
                        loading_view.visibility = View.INVISIBLE
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
                                            actionDelegate.login()
                                        }
                                        .show()
                            }
                        }
                    }
                }
            }
        })

        val localPhoneNumber =
                PrivatePreference.getPreference().getValue(PrefConst.KEY_LOGIN_PHONE_NUMBER, "")
        if (TextUtils.isEmpty(localPhoneNumber)) {
            context?.let {
                //调起键盘
                val inputMethodManager =
                        it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(et_phone_number, InputMethodManager.SHOW_FORCED)
            }
        } else {
            et_phone_number.setText(localPhoneNumber)
        }

        et_phone_number.addTextChangedListener(afterTextChanged = {
            configLoginBtnEnable()
            tv_clean.visibility = if (et_phone_number.text.isNotEmpty()) View.VISIBLE else View.GONE
        })


    }

    private fun configLoginBtnEnable() {
        val value = loginPhoneModel.countDownTimerData.value
        value ?: return
        tv_login.isEnabled = value == 0L && phoneNumberInputCorrect()

    }

    private fun phoneNumberInputCorrect(): Boolean {
        return et_phone_number.text.length == 11
    }

    inner class ActionDelegate {


        fun touristsLogin() {
            userModel.registerVisitor()
        }

        fun alipayLogin() {
            userModel.registerAliPay(requireActivity())
        }


        fun login() {
            //如果验证码时间少于60秒，优先展示 %d秒后可登录，且不可点击
            //手机号输入不完整时，不可点击
            //以上条件满足时，可点击，点击获取验证码，获取完成后跳转输入验证码界面

            //保存在本地，并且调用发送验证码接口
            val toString = et_phone_number.text.toString()
            PrivatePreference.getPreference().putValue(PrefConst.KEY_LOGIN_PHONE_NUMBER, toString).apply()
            //发送验证码，成功后跳转页面
            loginPhoneModel.sendVerificationCode(toString)

        }

        fun clearInputNumber() {
            et_phone_number.setText("")
        }

        fun back() {
            navigateUp()
        }
    }

}

