package me.lucwallace.yptmc

import java.util.UUID

data class User(
    val uuid: UUID,
    val yptID: Int,
    val totalMinutes: Int,
    val todayMinutes: Int,
    val serverMinutes: Int,
    val isStudying: Boolean
)
