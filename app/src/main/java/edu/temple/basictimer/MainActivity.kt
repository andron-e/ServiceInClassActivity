package edu.temple.basictimer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/*
using a binder to start and pause the service from the UI
can delay coroutine is easy, but pausing it is difficult
threads - spinlock(forever loop for thread)

goal: bind to interface and surface to UI
start and stop:
- step 1: implement start and stop and pause functionality in application (2 buttons 3 functions)
- start should change to pause, pause should change to unpause/resume
- stop stops the timer no matter what
- using isRunning and isPaused helps with pause functionality
- think through state chart
- commit

- step 2: implement or utilize the state flow inside the object TimerInfo, use this to update the textView in activity,
- first goes to log, then use stateflow for update
-


another solution would use compose
 */
class MainActivity : AppCompatActivity() {
    var timerBinder: TimerService.TimerBinder? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?, service: IBinder?
        ) {
            timerBinder = service as TimerService.TimerBinder

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerBinder = null
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE
        )

        // easier?
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            timerBinder?.run {
                if (isRunning) pause() else start(10)
                startButton.text = if (isRunning) "Pause" else "Resume"
            }
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.run {
                if (isPaused) pause()
                stop()
            }
            startButton.text = "Start"
        }

        /*
        // in-class solution
        findViewById<Button>(R.id.startButton).setOnClickListener {
                timerBinder?.run{
                    if (!isRunning) {
                        timerBinder?.start(10)
                        (it as Button).text = "Resume"
                    } else {
                        timerBinder?.pause()
                        (it as Button).text = if (isPaused) {
                            "Resume"
                        } else {
                            "Pause"
                        }
                    }
                }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            timerBinder?.stop()
            findViewById<Button>(R.id.startButton).text = "Start"
        }
        */

        // update the text view
        val timerTextView = findViewById<TextView>(R.id.textView)
        lifecycleScope.launch {
            repeatOnLifecycle((Lifecycle.State.RESUMED)) {               // will only start on started or resumed state
                TimerInfo.currentTime.collect {
                    timerTextView.text = it.toString()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}