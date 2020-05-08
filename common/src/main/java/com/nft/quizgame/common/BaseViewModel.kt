package com.nft.quizgame.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nft.quizgame.common.event.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    val stateData = MutableLiveData<Event<State>>()

    fun isLoading() = stateData.value?.peekContent() is State.Loading

}