package com.bm2cy.dainty2.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bm2cy.dainty2.R
import com.bm2cy.dainty2.databinding.ActivityTabReviewBinding
import com.bm2cy.dainty2.internal.UpdateWebTabEvent
import com.bm2cy.dainty2.presenter.TabReviewViewPresenter
import com.bm2cy.dainty2.utils.LogUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class TabReviewActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTabReviewBinding
    private lateinit var presenter: TabReviewViewPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = TabReviewViewPresenter()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tab_review)
        binding.presenter = presenter
        binding.lifecycleOwner = this
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: UpdateWebTabEvent) {
        LogUtil.d("onMessageEvent")
        presenter.updateRecentList(binding.recentList)
    }

}