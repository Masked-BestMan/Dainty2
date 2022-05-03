package com.bm2cy.dainty2.presenter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import com.bm2cy.dainty2.Dainty2Application
import com.bm2cy.dainty2.internal.UpdateWebTabEvent
import com.bm2cy.dainty2.internal.WebTabManager
import org.greenrobot.eventbus.EventBus

class WebViewPresenter {

    fun onClick(view: View){
        WebTabManager.getInstance().divideAllWebView()
        Dainty2Application.app.curActivity.finish()
        EventBus.getDefault().post(UpdateWebTabEvent())
    }

    fun catchWebView(context: Context){
        val wv_capture = WebTabManager.getInstance().getCacheWebTab().last().webView
        wv_capture.isDrawingCacheEnabled = true
        val bitmap = wv_capture.drawingCache
        WebTabManager.getInstance().getCacheWebTab().last().picture = BitmapDrawable(context.resources, bitmap).current
    }
}