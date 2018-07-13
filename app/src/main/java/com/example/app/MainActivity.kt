package com.example.app

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

val View.contextJob: Job?
    get() = (context as? JobHolder)?.job

class MainActivity : AppCompatActivity(), JobHolder {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) return true
        return super.onOptionsItemSelected(item)
    }

    private val handler = Handler()

    private fun countDown() {
        fab.setOnClickListener {
            for (i in 10 downTo 1) { // countDown from 10 to 1
                handler.postDelayed({
                    hello.text = "Countdown $i ..." // update text
                }, 1000L * (10 - i))
            }
        }
    }

    private suspend fun countDownTask() {
        for (i in 10 downTo 1) { // countDown from 10 to 1
            hello.text = "Countdown $i ..." // update text
            delay(500)
        }
    }

    private fun countDownCoroutine() {
        fab.setOnClickListener {
            launch(UI) {
                countDownTask()
            }
        }
    }

    private fun countDownCoroutineActor() {
        // launch one actor
        val eventActor = actor<View>(UI) {
            for (event in channel) countDownTask()
        }
        // install a listener to activate this actor
        fab.setOnClickListener {
            eventActor.offer(it)
        }
    }

    private fun countDownCoroutineConflation() {
        val eventActor = actor<View>(UI, capacity = Channel.CONFLATED) {
            // <--- Changed here
            for (event in channel) countDownTask() // pass event to action
        }

        fab.setOnClickListener {
            eventActor.offer(it)
        }
    }

    private fun fib(x: Int): Int = if (x <= 1) x else fib(x - 1) + fib(x - 2)


    suspend fun fibBlocking(x: Int): Int {
        return withContext(CommonPool) {
            if (x <= 1) x else fibBlocking(x - 1) + fibBlocking(x - 2)
        }
    }

    private fun fibCoroutine() {
        var result = "none" // the last result
        launch(UI) {
            var counter = 0
            while (true) {
                hello.text = "${++counter}: $result"
                delay(100) // update the text every 100ms
            }
        }

        // compute the next fibonacci number of each click
        var x = 1
        fab.setOnClickListener {
            launch(UI) {
                result = "fib($x) = ${fibBlocking(x)}"
                x++
            }
        }
    }

    override val job: Job = Job()

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // cancel the job when activity is destroyed
    }

    private fun countDownCoroutineLifeCycle(view: View) {
        val eventActor = actor<View>(UI, parent = view.contextJob, capacity = Channel.CONFLATED) {
            for (event in channel) countDownTask() // pass event to action
        }

        fab.setOnClickListener {
            eventActor.offer(it)
        }
    }

    private fun inspectionCoroutine() {
        fab.setOnClickListener {
            hello.text = "Before launch"
            launch(UI) {
                inspectionTask()
            }
            hello.append("\nAfter launch")
        }
    }

    private suspend fun inspectionTask() {
        hello.append("\nInside coroutine")
        delay(500)
        hello.append("\nAfter delay")
    }

    private fun inspectionCoroutineUndispatched() {
        fab.setOnClickListener {
            hello.text = "Before launch"
            launch(UI, CoroutineStart.UNDISPATCHED) {
                inspectionTask()
            }
            hello.append("\nAfter launch")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        inspectionCoroutineUndispatched()
    }
}
