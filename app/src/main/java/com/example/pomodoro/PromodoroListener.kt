package com.example.pomodoro

interface PromodoroListener {

    fun start(id: Int)

    fun stop(id: Int, timerMs: Long)

    fun delete(id: Int)

    fun finish(id: Int)
}