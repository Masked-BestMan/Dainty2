package com.bm2cy.dainty2.utils

import android.util.Log

class LogUtil {
    companion object {
        private const val TAG = "Dainty2"

        fun d(debug: String){
            Log.d(TAG, debug)
        }
    }
}