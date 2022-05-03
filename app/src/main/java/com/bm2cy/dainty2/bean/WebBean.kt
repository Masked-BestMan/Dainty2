package com.bm2cy.dainty2.bean

import android.graphics.drawable.Drawable
import com.bm2cy.dainty2.component.CommonWebView

data class WebBean(val id: Int, val webView: CommonWebView, var tabTitle: String,
                   var tabIcon: Drawable?, var picture: Drawable?)
