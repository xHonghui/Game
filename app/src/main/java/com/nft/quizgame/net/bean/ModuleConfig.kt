package com.nft.quizgame.net.bean

import android.text.TextUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
1	moudle_code	int	否	模块编码
2	module_name	string	否	模块名称
3	tags	string	是	题目标签,记录标签id,用 “^” 按顺序分割,如：^1^2^3^
4	difficultys	string	是	难度,用 “^” 按顺序分割：1^2^3
 */
open class ModuleConfig {
    @SerializedName("module_code")
    var moduleCode: Int? = null
    @SerializedName("module_name")
    var moduleName:String? = null
    @SerializedName("tags")
    var tags: String? = null
    @SerializedName("difficultys")
    var difficulties: String? = null

    @Expose(serialize = false, deserialize = false)
    var easeList: List<Int>? = null
    @Expose(serialize = false, deserialize = false)
    var tagList: List<Int>? = null

    fun extractEaseAndTag(){
        easeList = if (difficulties != null) {
            val easeArray = difficulties!!.split("^")
            val list = arrayListOf<Int>()
            easeArray.forEach { ease ->
                list.add(ease.toInt())
            }
            list
        } else {
            null
        }

        tagList = if (!TextUtils.isEmpty(tags)) {
            val tagArray = tags!!.split("^")
            val list = arrayListOf<Int>()
            tagArray.forEach { tag ->
                list.add(tag.toInt())
            }
            list
        } else {
            null
        }
    }
}