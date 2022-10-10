package com.undsf.mirai.redicer.services

import kotlin.random.Random

class DiceService {
    fun roll(face: Int) : Int {
        return Random.nextInt(1, face + 1);
    }
}