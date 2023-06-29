package com.undsf.mirai.redicer

import com.undsf.mirai.redicer.services.DiceService
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info
import java.util.regex.Matcher
import java.util.regex.Pattern

object REDicer : KotlinPlugin(
    JvmPluginDescription(
        id = "com.undsf.mirai.redicer",
        name = "REDicer",
        version = "0.1.4",
    ) {
        author("Arathi of Nebnizilla")
        info("""骰了吧""")
    }
) {
    private val diceSvc = DiceService()
    private val patternRoll: Pattern = Pattern.compile("^r(\\d+)?d(\\d+)?(\\+(\\d+))?\$")
    private val patternDicePool: Pattern = Pattern.compile("^w(w)?( )?(\\d+)( )?(a(\\d+))?( )?(\\+(\\d+))?( )?(\\-(\\d+))?\$")

    override fun onEnable() {
        logger.info { "REDicer已启用" }

        globalEventChannel().subscribeMessages {
            startsWith(".") reply {
                logger.info("接收到来自${this.sender.nick}的消息${it}")
                handleCommand(this.sender, it)
            }
        }
    }

    private suspend fun handleCommand(sender: User, message: String) : String? {
        val command = message.substring(1)

        // r命令
        var matcher = patternRoll.matcher(command)
        if (matcher.find()) {
            return handleRoll(sender, matcher)
        }

        // w命令
        matcher = patternDicePool.matcher(command)
        if (matcher.find()) {
            return handleDicePool(sender, matcher)
        }

        return null
    }

    private fun handleRoll(sender: User, matcher: Matcher) : String {
        var amount = 1
        var face = 100
        var addition = 0

        val amountStr = matcher.group(1)
        val faceStr = matcher.group(2)
        val additionStr = matcher.group(4)
        if (amountStr != null && amountStr.isNotEmpty()) {
            amount = amountStr.toInt()
        }
        if (faceStr != null && faceStr.isNotEmpty()) {
            face = faceStr.toInt()
        }
        if (additionStr != null && additionStr.isNotEmpty()) {
            addition = additionStr.toInt()
        }

        if (amount <= 0 || amount > 100) {
            return "无效的骰子数量"
        }
        if (face <= 1 || face > 1048576) {
            return "无效的骰子面数"
        }
        if (addition < -1048576 || addition > 1048576) {
            return "无效的修正值"
        }

        val resp = StringBuilder()
        var sum = 0
        for (i in 1 .. amount) {
            val point = diceSvc.roll(face)
            if (i > 1) {
                resp.append(" + ")
            }
            resp.append(point)
            sum += point
        }
        if (addition > 0) {
            resp.append(" + ")
            resp.append(addition)
            sum += addition
        }

        if (amount > 1) {
            resp.append(" = $sum")
        }

        logger.info { "${matcher.group(0)} = $sum" }
        return "${matcher.group(0)} = $resp"
    }

    private fun handleDicePool(sender: User, matcher: Matcher) : String {
        var amount = 0
        var threshold = 8
        var addition = 0
        var showDetails = false

        val w = matcher.group(1)
        if (w == "w") {
            showDetails = true
        }

        val amountStr = matcher.group(3)
        if (amountStr != null && amountStr.isNotEmpty()) {
            amount = amountStr.toInt()
        }
        if (amount <= 0) {
            return "无效的骰子个数"
        }

        val thresholdStr = matcher.group(6)
        if (thresholdStr != null && thresholdStr.isNotEmpty()) {
            threshold = thresholdStr.toInt()
        }
        if (threshold < 5 || threshold > 10) {
            return "无效的加骰参数"
        }

        var plusStr = matcher.group(9)
        if (plusStr != null && plusStr.isNotEmpty()) {
            addition += plusStr.toInt()
        }

        var minusStr = matcher.group(12)
        if (minusStr != null && minusStr.isNotEmpty()) {
            addition -= minusStr.toInt()
        }

        val resp = StringBuilder()
        var counter = 0

        var diceLeft = 0

        if (showDetails) {
            resp.append("[")
        }
        for (i in 1 .. amount) {
            val point = diceSvc.roll(10)
            if (showDetails) {
                if (i > 1) {
                    resp.append(" ")
                }
                resp.append(point)
            }

            if (point >= threshold) {
                counter++
            }
            if (point == 10) {
                diceLeft++
            }
        }
        if (showDetails) {
            resp.append("] [")
        }

        var first = true
        while (diceLeft > 0) {
            val point = diceSvc.roll(10)
            if (showDetails) {
                if (!first) {
                    resp.append(" ")
                }
                else {
                    first = false
                }
                resp.append(point)
            }

            if (point >= threshold) {
                counter++
            }
            if (point != 10) {
                diceLeft--
            }
        }
        if (showDetails) {
            resp.appendLine("]")
        }

        val result = counter + addition
        logger.info("${matcher.group(0)} = $result")
        resp.appendLine("${matcher.group(0)} = $result")

        return resp.toString()
    }
}