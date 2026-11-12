package edu.temple.basictimer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
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

/*

Create a branch of your previously forked Service activity (https://github.com/karlmorris/ServiceInClassActivity/). Call the branch "menuintegration"
Replicate all button functions as Menu action
Make each item an Action Button (if there is room)
to submit to Canvas:
Push your new branch (do not merge the branch with master) to GitHub
Visit the repository website
On the left of the page above the list of files, you will see the branch selector currently showing 'master'
Change the current branch to the new branch you pushed, 'menuintegrator'
Copy the website URL (not the Git url) and submit that to Canvas. It should look like: https://github.com/<your_name>/ServiceInClassActivity/tree/menuintegration
 */
class MainActivity : AppCompatActivity() {
    var timerBinder: TimerService.TimerBinder? = null
    lateinit var startButton: Button

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
    private fun startTimer() {
        timerBinder?.start(10)
        startButton.text = "Pause"
    }
    private fun pauseTimer() {
        timerBinder?.run{
            if (isRunning) {
                pause()
                startButton.text = "Resume"
            }
        }
    }
    private fun stopTimer() {
        timerBinder?.run{
            if (isPaused) {
                pause()
            }
            stop()
        }
        startButton.text = "Start"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindService(
            Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE
        )

        startButton = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            timerBinder?.run{if(isRunning) pauseTimer() else startTimer() }
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener { stopTimer() }

        /*
        easier?
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
        */

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /*when (item.itemId) {
            R.id.action_start_timer -> startTimer()
            R.id.action_stop_timer -> stopTimer()
            else -> return super.onOptionsItemSelected(item)
        }
        return true*/
        when (item.itemId) {
            R.id.action_start_timer -> {
                timerBinder?.start(100); true
            }

            R.id.action_stop_timer -> {
                timerBinder?.stop(); true
            }

            else -> false

        }
        return super.onOptionsItemSelected(item)
    }
}