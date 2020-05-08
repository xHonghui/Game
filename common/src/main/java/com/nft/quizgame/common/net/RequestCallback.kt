package com.nft.quizgame.common.net

import com.android.volley.Response
import com.android.volley.VolleyError

/**
 * Created by yangjiacheng on 2017/11/17.
 * ...
 */
interface RequestCallback<T> : Response.Listener<T>, Response.ErrorListener {

    /**
     * Note ：默认情况下，Volley 会在 mainThread 回调此方法
     * @param response response
     */
    override fun onResponse(response: T)

    override fun onErrorResponse(error: VolleyError)

    fun onUserExpired()
}