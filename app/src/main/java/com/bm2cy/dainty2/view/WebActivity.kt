package com.bm2cy.dainty2.view

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bm2cy.dainty2.R
import com.bm2cy.dainty2.databinding.ActivityWebBinding
import com.bm2cy.dainty2.internal.WebTabManager
import com.bm2cy.dainty2.utils.LogUtil
import com.bm2cy.dainty2.presenter.WebViewPresenter


class WebActivity: AppCompatActivity(){
    private lateinit var presenter: WebViewPresenter
    private lateinit var binding: ActivityWebBinding
    private lateinit var mWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebTabManager.getInstance().divideAllWebView()
        presenter = WebViewPresenter()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web)
        binding.presenter = presenter
        binding.lifecycleOwner = this
        val wb = WebTabManager.getInstance().getCacheWebTab().last()
        mWebView = wb.webView
        binding.webViewContainer.addView(mWebView)
    }

    override fun onResume() {
        super.onResume()
        mWebView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mWebView.onPause()
    }

    override fun finish() {
        LogUtil.d("finish WebActivity")
        presenter.catchWebView(this)
        super.finish()
    }

}