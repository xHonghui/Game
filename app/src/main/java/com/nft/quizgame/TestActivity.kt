package com.nft.quizgame

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.nft.quizgame.dialog.QuizDownloadingDialog
import com.nft.quizgame.dialog.QuizUpdateDialog
import com.nft.quizgame.ext.toast
import com.nft.quizgame.function.coin.CoinOptViewModel
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.net.bean.CoinInfo
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {


    private val userModel: UserViewModel by lazy {
        AppViewModelProvider.getInstance().get(UserViewModel::class.java)
    }

    private val coinOptViewModel: CoinOptViewModel by lazy {
        AppViewModelProvider.getInstance().get(CoinOptViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        tv_add_coin.setOnClickListener {
            val toString = et_number.text.toString()
            if(toString.isEmpty()){
                return@setOnClickListener
            }
            val coins = toString.toInt()
            userModel.addUserCoin(coins)

            GlobalScope.launch {
                coinOptViewModel.operateCashIn(CoinInfo.GOLD_COIN, coins, getString(R.string.test_order_des))
            }

            toast(this,R.string.success)
        }


        account_id.text = userModel.userData.value?.userId?:"empty"

        tv_sub_coin.setOnClickListener {

            val toString = et_number.text.toString()
            if(toString.isEmpty()){
                return@setOnClickListener
            }
            userModel.addUserCoin(-toString.toInt())
            toast(this,R.string.success)
        }

        tv_update_dialog.setOnClickListener {
            //QuizUpdateDialog(this).show()
        }

        tv_update_download_dialog.setOnClickListener {
            //QuizDownloadingDialog(this).show()
        }


        tv_account.setOnClickListener {
            val toString = et_account.text.toString()
            if(TextUtils.isEmpty(toString)){
                return@setOnClickListener
            }
            AppViewModelProvider.getInstance().get(UserViewModel::class.java).visitorAccountTest = toString
            toast(this,R.string.success)
        }

    }

}
