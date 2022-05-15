package com.example.pomodoro

data class Promodoro constructor(var id: Int, var timerMs: Long, var time: Long, var isStarted: Boolean) {
}