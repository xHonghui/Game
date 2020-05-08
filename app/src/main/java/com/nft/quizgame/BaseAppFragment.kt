package com.nft.quizgame

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.nft.quizgame.common.BaseFragment
import com.nft.quizgame.ext.toast
import com.nft.quizgame.function.user.LoginFragment
import com.nft.quizgame.function.user.UserViewModel

abstract class BaseAppFragment :BaseFragment() {

    protected val userModel: UserViewModel by lazy {
        AppViewModelProvider.getInstance().get(UserViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userModel.refreshTokenExpiredLiveData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {boolean->
                if (boolean) {
                    val bundle = Bundle()
                    bundle.putBoolean(LoginFragment.KEY_POP_BACK_TO_MAIN,true)
                    navigate(R.id.action_to_login,bundle)
                    toast(requireContext(),R.string.refresh_token_expired)
                }
            }
        })
    }

}