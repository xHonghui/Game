package com.nft.quizgame.view

import android.content.Context
import android.util.AttributeSet
import com.nft.quizgame.common.view.TouchBlockingView

class LoadingView : TouchBlockingView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

}