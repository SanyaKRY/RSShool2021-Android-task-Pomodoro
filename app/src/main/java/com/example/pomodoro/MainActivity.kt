package com.example.pomodoro

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), PromodoroListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding
    private val promodoros = mutableListOf<Promodoro>()
    private val promodoroAdapter = PromodoroAdapter(this)
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = promodoroAdapter
        }

        binding.inputMinutesTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(binding.inputMinutesTextView.text.trim().isNotEmpty()){
                    binding.addTimerButton.isEnabled = true
                } else {
                    binding.addTimerButton.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.addTimerButton.setOnClickListener {
            val time = TimeUnit.MINUTES.toMillis(binding.inputMinutesTextView.text.toString().toLong())
            promodoros.add(Promodoro(nextId++, time, time, false))
            promodoroAdapter.submitList(promodoros.toList())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.i("CAT_TAG", "Lifecycle.Event.ON_STOP & onAppBackgrounded()")
        var promodoroIdStarted = promodoros.find { it.isStarted }
        if(promodoroIdStarted != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(TIMER_TIME_MS, (promodoroIdStarted?.timerMs + System.currentTimeMillis()))
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.i("CAT_TAG", "Lifecycle.Event.ON_START & onAppBackgrounded()")
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun start(id: Int) {
        val promodoroIdStarted = promodoros.find { it.isStarted }
        if (promodoroIdStarted != null && promodoroIdStarted.id != id) {
            stop(promodoroIdStarted.id, promodoroIdStarted.timerMs)
        }
        changePromodoro(id, null, true)
    }

    override fun stop(id: Int, timerMs: Long) {
        changePromodoro(id, timerMs, false)
    }

    override fun delete(id: Int) {
        promodoros.remove(promodoros.find { it.id == id })
        promodoroAdapter.submitList(promodoros.toList())
    }

    override fun finish(id: Int) {
        MediaPlayer.create(this, R.raw.cat_meow_sound).start()
        Toast.makeText(this, "Pomodoro has finished.", Toast.LENGTH_SHORT).show()
    }

    private fun changePromodoro(id: Int, timerMs: Long?, isStarted: Boolean) {
        val newPromodoros = mutableListOf<Promodoro>()
        promodoros.forEach {
            if (it.id == id) {
                newPromodoros.add(Promodoro(it.id, timerMs ?: it.timerMs, it.time, isStarted))
            } else {
                newPromodoros.add(it)
            }
        }
        promodoroAdapter.submitList(newPromodoros)
        promodoros.clear()
        promodoros.addAll(newPromodoros)
    }

    companion object {
        const val COMMAND_ID: String = "COMMAND_ID"
        const val TIMER_TIME_MS: String = "TIMER_TIME_MS"
        const val COMMAND_START: String = "COMMAND_START"
        const val COMMAND_STOP: String = "COMMAND_STOP"
        const val INVALID: String = "INVALID"
    }
}