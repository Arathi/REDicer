package com.undsf.mirai.redicer

import kotlin.random.Random

class DiceUtil {
    fun roll(face: Int) : Int {
        return Random.nextInt(1, face + 1);
    }
}