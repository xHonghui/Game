package com.nft.quizgame.ext

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData


private val handle: Handler = Handler(Looper.getMainLooper())

fun post(r: () -> Unit) {
    handle.post(r)
}

fun postDelayed(delay: Long, r: () -> Unit) {
    handle.postDelayed(r, delay)
}

fun postDelayed(delay: Long, r: Runnable) {
    handle.postDelayed(r, delay)
}

fun removeCallbacks(r: Runnable) {
    handle.removeCallbacks(r)
}

fun toast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

fun toast(context: Context, resId: Int) {
    Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
}

fun <T> MutableLiveData<T>.notify() {
    this.postValue(this.value)
}

fun String.getStyleSpanString(subString: String, color: Int? = null, style: Int? = null, size: Int? = null,
                              flag: Int, indexOf: Boolean = true): SpannableString {
    val spanString = SpannableString(this)
    val startIndex = if (indexOf) this.indexOf(subString) else this.lastIndexOf(subString)
    val endIndex = startIndex + subString.length

    if (color != null) {
        val colorSpan = ForegroundColorSpan(color)
        spanString.setSpan(colorSpan, startIndex, endIndex, flag)
    }
    if (style != null) {
        val styleSpan = StyleSpan(style)
        spanString.setSpan(styleSpan, startIndex, endIndex, flag)
    }
    if (size != null) {
        val sizeSpan = AbsoluteSizeSpan(size, true)
        spanString.setSpan(sizeSpan, startIndex, endIndex, flag)
    }
    return spanString
}

fun String.getClickableSpanString(subString: String, color: Int? = null, flag: Int,
                                  clickCallback: () -> Unit): SpannableString {
    val spanString = SpannableString(this)
    val startIndex = this.indexOf(subString)
    val endIndex = startIndex + subString.length
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            clickCallback()
        }
        override fun updateDrawState(ds : TextPaint) {
            ds.color = ds.linkColor
            ds.isUnderlineText = false
        }
    }
    spanString.setSpan(clickableSpan, startIndex, endIndex, flag)
    if (color != null) {
        val colorSpan = ForegroundColorSpan(color)
        spanString.setSpan(colorSpan, startIndex, endIndex, flag)
    }
    return spanString
}
