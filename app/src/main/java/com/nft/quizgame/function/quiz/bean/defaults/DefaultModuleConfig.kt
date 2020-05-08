package com.nft.quizgame.function.quiz.bean.defaults

import com.nft.quizgame.net.bean.ModuleConfig

sealed class DefaultModuleConfig : ModuleConfig() {

    /**
    1	自由模式
    2	闯关模式
    3	竞速模式
    4	新冠肺炎
    5	新手入门
    6	最强大脑
    7	体育知识
    8	娱乐达人
     */
    companion object {
        const val MODULE_CODE_1 = 1
        const val MODULE_CODE_2 = 2
        const val MODULE_CODE_3 = 3
        const val MODULE_CODE_4 = 4
        const val MODULE_CODE_5 = 5
        const val MODULE_CODE_6 = 6
        const val MODULE_CODE_7 = 7
        const val MODULE_CODE_8 = 8
    }

    class DefaultModuleConfig1 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_1
            this.difficulties = "1^2^3"
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig2 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_2
            this.difficulties = "1^2^3"
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig3 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_3
            this.difficulties = "1^2^3"
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig4 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_4
            this.difficulties = "1^2^3"
            this.tags = "2179" //肺炎抗疫
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig5 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_5
            this.difficulties = "1"
            this.tags = "2266^2267^2268^2212^2229^2240^2245"
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig6 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_6
            this.difficulties = "3"
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig7 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_7
            this.difficulties = "1^2^3"
            this.tags = "2229" //运动
            extractEaseAndTag()
        }
    }

    class DefaultModuleConfig8 : DefaultModuleConfig() {
        init {
            this.moduleCode = MODULE_CODE_8
            this.difficulties = "1^2^3"
            this.tags = "2281" //娱乐
            extractEaseAndTag()
        }
    }
}