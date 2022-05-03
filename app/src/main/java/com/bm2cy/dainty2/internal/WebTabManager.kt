package com.bm2cy.dainty2.internal

import android.content.MutableContextWrapper
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bm2cy.dainty2.Dainty2Application
import com.bm2cy.dainty2.R
import com.bm2cy.dainty2.bean.WebBean
import com.bm2cy.dainty2.component.CommonWebView
import com.bm2cy.dainty2.utils.LogUtil
import java.util.*

class WebTabManager private constructor(){

    private object Holder{
        val instance = WebTabManager()
    }

    companion object Instance {
        var increaseKey = 0
        @JvmStatic
        fun getInstance(): WebTabManager{
            return Holder.instance
        }
    }

    private val cacheWebTab = LinkedList<WebBean>()

    fun divideAllWebView(){
        LogUtil.d("divideAllWebView: ${cacheWebTab.size}")
        for (wb in cacheWebTab){
            wb.webView.parent?.let {
                (it as ViewGroup).removeAllViews()
            }
        }
    }

    fun getCacheWebTab(): List<WebBean>{
        return cacheWebTab
    }

    fun addNewTab(): WebBean {
        val webView = CommonWebView(MutableContextWrapper(Dainty2Application.app))
        val wb = WebBean(increaseKey++, webView, "首页", ContextCompat.getDrawable(Dainty2Application.app, R.drawable.web_tab_icon_home), ContextCompat.getDrawable(Dainty2Application.app, R.drawable.ic_launcher_foreground))
        cacheWebTab.add(wb)
        return wb
    }

    fun joinWebTabToLast(key: Int){
        for (wb in cacheWebTab){
            if (wb.id == key){
                cacheWebTab.remove(wb)
                cacheWebTab.addLast(wb)
                break
            }
        }
    }

    fun updateLastTabTitle(tabTitle: String){
        val wb = cacheWebTab.last
        wb.tabTitle = tabTitle
    }

    fun updateLastTabIcon(tabIcon: Drawable){
        val wb = cacheWebTab.last
        wb.tabIcon = tabIcon
    }
}