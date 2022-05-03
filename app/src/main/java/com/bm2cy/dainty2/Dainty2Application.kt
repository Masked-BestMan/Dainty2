package com.bm2cy.dainty2

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bm2cy.dainty2.utils.LogUtil

class Dainty2Application: Application() {
    var isFirstLaunch = true
    lateinit var curActivity: Activity
    override fun onCreate() {
        super.onCreate()
        app = this
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {
                LogUtil.d("onActivityStarted ${activity::class.java.simpleName}")
                curActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
    }

    companion object Instance{
        lateinit var app: Dainty2Application
    }




}