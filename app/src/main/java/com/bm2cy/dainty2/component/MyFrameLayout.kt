package com.bm2cy.dainty2.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class MyFrameLayout(context: Context, attributes: AttributeSet?): FrameLayout(context, attributes){
    constructor(context: Context): this(context, null)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }
}