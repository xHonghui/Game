package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.QuizRequestProperty

class SyncDataUploadRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880512"
    }

    init {
        requestProperty = QuizRequestProperty()
    }

    @SerializedName("game_progress")
    var gameProgressList: List<GameProgress>? = null

    /**
    1	upload_type	int	否	1.前后台切换上传 2.周期性上传 3.手动保存
    2	online_time	int	否	app位于前台的时长，单位：分钟（本周期内）
    3	module_code	string	否	模块编码
    4	game_mode	int	否	0.主线模式 1.闯关模式 2.自由模式 3.竞速模式
    5	answer_total	int	否	答题总数（本周期内）
    6	answer_correct_total	int	否	答题正确总数（本周期内）
    7	answer_start_time	long	否	游戏开始时间戳（本周期内）
    8	record_start_time	long	否	记录开始时间戳（本周期内）
    9	record_end_time	long	否	记录结束时间戳（本周期内）
    10	option_select_times	json	否	选项选择情况，参考选项选择信息单元（本周期内）
    11	coin_balance	decimal	否	当前金币余额
    12	coin_opt_details	jsonarray	是	金币操作详情，参考金币操作信息单元（本次同步周期）
    13	main_mode_record	json	是	主线模式记录，参考主线模式信息单元（本次同步周期）
    14	checkpoint_mode_record	json	是	闯关模式记录，参考闯关模式信息单元（本次同步周期）
    15	free_mode_record	json	是	自由模式记录，参考自由模式信息单元（本次同步周期）
    16	race_mode_record	json	是	竞速模式记录，参考竞速模式信息单元（本次同步周期）
     */
    class GameProgress {
        companion object {
            const val UPLOAD_TYPE_APP_STATE = 1 //前后台切换上传
            const val UPLOAD_TYPE_CYCLE = 2 //周期性上传
            const val UPLOAD_TYPE_MANUAL = 3 //手动保存
        }

        @SerializedName("upload_type")
        var uploadType: Int? = null
        @SerializedName("online_time")
        var onlineTime: Int = 0
        @SerializedName("module_code")
        var moduleCode: Int? = null
        @SerializedName("game_mode")
        var gameMode: Int? = null
        @SerializedName("answer_total")
        var answerTotal: Int = 0
        @SerializedName("answer_correct_total")
        var answerCorrectTotal: Int = 0
        @SerializedName("answer_start_time")
        var answerStartTime: Long? = null
        @SerializedName("record_start_time")
        var recordStartTime: Long? = null
        @SerializedName("record_end_time")
        var recordEndTime: Long? = null
        @SerializedName("option_select_times")
        var optionSelectTimes: OptionSelectTimes? = null
        @SerializedName("coin_balance")
        var coinBalance: Int = 0
        @SerializedName("coin_opt_details")
        var coinOptDetails: List<CoinOptDetail>? = null
        @SerializedName("main_mode_record")
        var mainModeRecord: MainModeRecord? = null
        @SerializedName("checkpoint_mode_record")
        var stageModeRecord: StageModeRecord? = null
        @SerializedName("free_mode_record")
        var freeModeRecord: FreeModeRecord? = null
        @SerializedName("race_mode_record")
        var raceModeRecord: RaceModeRecord? = null
    }


    /**
    1	a_select_times	int	否	选择A选项的次数
    2	b_select_times	int	否	选择B选项的次数
    3	c_select_times	int	否	选择C选项的次数
    4	d_select_times	int	否	选择D选项的次数
     */
    class OptionSelectTimes {
        @SerializedName("a_select_times")
        var aSelectTimes = 0
        @SerializedName("b_select_times")
        var bSelectTimes = 0
        @SerializedName("c_select_times")
        var cSelectTimes = 0
        @SerializedName("d_select_times")
        var dSelectTimes = 0
    }

    /**
    1	opt_type	int	否	操作类型，0：充值，1：消费，2：提现，3：转化，4：退款
    2	order_id	string	否	操作类型对应的订单ID
    3	amount	decimal	否	操作数额
    4	opt_time	long	否	操作时间时间戳
     */
    class CoinOptDetail {
        companion object {
            const val OPT_CASH_IN = 0
            const val OPT_CASH_USE = 1
            const val OPT_CASH_OUT = 2
        }
        @SerializedName("opt_type")
        var optType: Int? = null
        @SerializedName("order_id")
        var orderId: String? = null
        @SerializedName("amount")
        var amount: Int = 0
        @SerializedName("opt_time")
        var optTime: Long? = null
    }

    /**
    1	coins_double_times	int	否	翻倍次数
    2   reminder_cards  int	否	提示卡获取次数
    3   change_cards    int	否	换题卡获取次数
    4	red_bag_bonus	int	否	红包奖励获取次数
    5	resurrection_times	int	否	复活次数
     */
    class MainModeRecord {
        @SerializedName("coins_double_times")
        var coinsDoubleTimes: Int = 0
        @SerializedName("reminder_cards")
        var tipsCards: Int = 0
        @SerializedName("change_cards")
        var changeCards: Int = 0
        @SerializedName("red_bag_bonus")
        var envelopeBonus: Int = 0
        @SerializedName("resurrection_times")
        var resurrectionTimes: Int = 0
    }

    /**
    1	coins_double_times	int	否	翻倍次数
    2   reminder_cards  int	否	提示卡获取次数
    3   change_cards    int	否	换题卡获取次数
    4	red_bag_bonus	int	否	红包奖励获取次数
    5	resurrection_times	int	否	复活次数
     */
    class StageModeRecord {
        @SerializedName("coins_double_times")
        var coinsDoubleTimes: Int = 0
        @SerializedName("reminder_cards")
        var tipsCards: Int = 0
        @SerializedName("change_cards")
        var changeCards: Int = 0
        @SerializedName("red_bag_bonus")
        var envelopeBonus: Int = 0
        @SerializedName("resurrection_times")
        var resurrectionTimes: Int = 0
    }

    /**
    1	coins_double_times	int	否	翻倍次数
    2   reminder_cards  int	否	提示卡获取次数
     */
    class FreeModeRecord {
        @SerializedName("coins_double_times")
        var coinsDoubleTimes: Int = 0
        @SerializedName("reminder_cards")
        var tipsCards: Int = 0
    }

    /**
     * resurrection_times	int	否	复活次数
     */
    class RaceModeRecord {
        @SerializedName("resurrection_times")
        var resurrectionTimes: Int = 0
    }
}