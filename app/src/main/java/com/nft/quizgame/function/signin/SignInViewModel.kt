package com.nft.quizgame.function.signin

import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.BaseViewModel
import com.nft.quizgame.function.signin.bean.SignInBean
import com.nft.quizgame.net.bean.SigninRequestBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInViewModel : BaseViewModel() {

    var repository = SignInRepository()
    var signInData = MutableLiveData<SignInBean>()


    init {
        launch(Dispatchers.IO) {
            val signInRequestBean = SigninRequestBean()
            repository.getSignInData(signInRequestBean, { errorCod, ErrorMsg ->

            }, { it ->
                signInData.value = it
            })
        }
    }

}