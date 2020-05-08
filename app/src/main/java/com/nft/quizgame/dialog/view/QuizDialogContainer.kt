package com.nft.quizgame.dialog.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import com.nft.quizgame.common.dialog.BaseDialog
import kotlinx.android.synthetic.main.quiz_dialog.view.*

class QuizDialogContainer : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    private val detector: GestureDetector
    lateinit var dialog: BaseDialog<*>
    private val rect = Rect()
    private val loc = IntArray(2)

    init {
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                val x = e.rawX.toInt()
                val y = e.rawY.toInt()
                quiz_dialog_compose.getLocationInWindow(loc)
                rect.set(loc[0], loc[1], loc[0] + quiz_dialog_compose.width, loc[1] + quiz_dialog_compose.height)
                if (rect.contains(x, y)) {
                    return false
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                dialog.dismiss()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return detector.onTouchEvent(event)
    }
}