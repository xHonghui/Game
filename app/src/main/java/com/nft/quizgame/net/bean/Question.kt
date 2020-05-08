package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class Question {
    /**
    1	id	int	否	题目id
    2	title	string	否	题目标题
    3	title_img	string	是	题目标题图片
    4	options	string	否
    题目选项,用 “^” 按顺序分割，如：选项A^选项B^选项C^选项D

    5	type	int	否	题目类型：1：文字（选项为文字），2：图片（选项为图片）
    6	answer	int	否	对应options 字段中的选项，如：1代表答案为第一项; 1代表答案为第二项, 依次类推，注意：有可能为多选题，如：13：代表答案为13
    7	tags	string	是	题目标签,记录标签id,用 “^” 按顺序分割,如：宗教^文化^生活
    8	difficulty	int	否	难度：1，2，3
     */

    var id: Int = 0
    var title: String? = null
    @SerializedName("title_img")
    var titleImage: String? = null
    var options: String? = null
    var type: Int = 0
    var answer: Int = 0
    var tags: String? = null
    var difficulty: Int = 0
    @SerializedName("correct_rate")
    var correctRate: Int = 0
    @SerializedName("avg_time_consume")
    var avgTimeConsume = 0
}