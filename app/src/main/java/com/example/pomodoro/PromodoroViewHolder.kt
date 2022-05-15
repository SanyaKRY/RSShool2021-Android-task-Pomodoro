package com.example.pomodoro

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.PromodoroItemBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PromodoroViewHolder(private val binding: PromodoroItemBinding, private val listener: PromodoroListener): RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null
    private var startTime: Long = 0L

    fun bind(promodoro: Promodoro) {
        binding.stopwatchTimer.text = promodoro.timerMs.displayTime()
        binding.customView.setPeriod(promodoro.time)

        if (promodoro.isStarted) {
            startTimer(promodoro)
        } else {
            stopTimer(promodoro)
        }

        initButtonsListeners(promodoro)
    }

    private fun initButtonsListeners(promodoro: Promodoro) {
        binding.startStopButto.setOnClickListener {
            if (promodoro.isStarted) {
                listener.stop(promodoro.id, promodoro.timerMs)
            } else {
                listener.start(promodoro.id)
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(promodoro.id) }
    }

    private fun startTimer(promodoro: Promodoro) {
        binding.startStopButto.text = "Stop"
        timer?.cancel()
        startTime = System.currentTimeMillis()
        timer = getCountDownTimer(promodoro)
        timer?.start()
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(promodoro: Promodoro) {
        binding.startStopButto.text = "Start"
        timer?.cancel()
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(promodoro: Promodoro): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val localTimerMs = startTime + promodoro.timerMs
            override fun onTick(millisUntilFinished: Long) {
                if (promodoro.timerMs <= 0L) {
                    onFinish()
                } else {
                    promodoro.timerMs = localTimerMs - System.currentTimeMillis()
                    binding.stopwatchTimer.text = promodoro.timerMs.displayTime()
                    GlobalScope.launch {
                        binding.customView.setCurrent(promodoro.time - promodoro.timerMs)
                    }
                }
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = promodoro.time.displayTime()
                stopTimer(promodoro)
                promodoro.timerMs = promodoro.time
                promodoro.isStarted = false
                listener.finish(promodoro.id)
                binding.root.setBackgroundColor(Color.parseColor("#F44336"))
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {
        private const val START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD  = 1000L * 60L * 60L * 24L
    }
}