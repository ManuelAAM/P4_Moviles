package com.escom.ipn.p4.cyberbreach.service

interface IAudioService {
    var isMuted: Boolean
    fun playClick()
    fun playHit()
    fun playMiss()
    fun playSkill()
    fun playVictory()
    fun playAlarm()
}
