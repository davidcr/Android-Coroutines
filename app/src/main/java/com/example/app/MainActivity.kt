package com.example.app

import android.os.Bundle
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

//TODO 6-3 devine an getter extension for a view that safecast its context into a JobHolder

class MainActivity : AppCompatActivity(), JobHolder {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        countDown()
    }

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
            //TODO 1 use a handler and a for loop to modify the R.id.hello TextView to display a 10 to 1 countdown with 500ms delay
        }
    }

    private suspend fun countDownTask() {
        //TODO 2-1 use a loop and a delay to change the TextView text
    }

    private fun countDownCoroutine() {
        //TODO 2-2 set a clicklistener to R.id.fab to launch our suspending function
    }

    private fun countDownCoroutineActor() {
        //TODO 3-1 create a value containing an actor on the UI, the generic on the actor to specify our actor is in fact a View (our floating button)
        //TODO 3-2 inside the lambda loop the actor channel for each of its events and then call the countDownTask
        //TODO 3-3 set the click listener and call the offer function of the actor and give it the view reference from the onClick lambda
    }

    private fun countDownCoroutineConflation() {
        //TODO 4 Use Channel.CONFLATED option for actor function and see the difference and use countDownTask
    }

    private fun fib(x: Int): Int = if (x <= 1) x else fib(x - 1) + fib(x - 2)


    /** Missing function modifier (keyword) **/ fun fibBlocking(x: Int): Int {
        //TODO 5 return a suspending function with CommonPool context. CommonPool is a CouroutineDispatcher that dispatch task for work intensive tasks
        return 0
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
                result = "fib($x) = ${fib(x)}" //TODO replace with fibBlocking
                x++
            }
        }
    }

    //TODO 6-2 override the job field and instanciate it

    override fun onDestroy() {
        super.onDestroy()
        //TODO 6-3 cancel the job when activity goes into destroy state
    }

    private fun countDownCoroutineLifeCycle(view: View) {
        //TODO 6-4 give the actor function a parent Job so the task pool inherit it as their root
        val eventActor = actor<View>(UI, /**parent = ???, **/ capacity = Channel.CONFLATED) {
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

}
