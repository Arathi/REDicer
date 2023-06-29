package com.undsf.mirai.redicer

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object RollCommand : SimpleCommand(
    REDicer,
    "roll",
    description = "常规骰子命令"
) {
    private val dice = DiceUtil()

    /**
     * amount 骰子数量
     * face 骰子面数
     */
    @Handler
    suspend fun CommandSender.handle(amount: Int, face: Int) {
        val text: StringBuilder = StringBuilder()
        text.appendLine("开始投掷${amount}个${face}面骰")
        var result = 0
        for (i in 1 .. amount) {
            val point = dice.roll(face)
            if (i > 1) {
                text.append("+")
            }
            text.append(point)
            result += point
        }
        text.appendLine("=$result")
        sendMessage(text.toString())
    }
}