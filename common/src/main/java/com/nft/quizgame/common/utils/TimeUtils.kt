package com.nft.quizgame.common.utils

import java.util.*

class TimeUtils {
    companion object {

        @JvmStatic
        fun isSameDay(time1: Long, time2: Long): Boolean {
            val date1 = Date(time1)
            val date2 = Date(time2)
            val calendar1: Calendar = Calendar.getInstance()
            calendar1.time = date1
            val calendar2: Calendar = Calendar.getInstance()
            calendar2.time = date2
            return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                    && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                    && calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE)
        }
    }
}