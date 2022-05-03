package com.bm2cy.dainty2.presenter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bm2cy.dainty2.Dainty2Application
import com.bm2cy.dainty2.R
import com.bm2cy.dainty2.bean.WebBean
import com.bm2cy.dainty2.component.overview.model.OverviewAdapter
import com.bm2cy.dainty2.component.overview.model.ViewHolder
import com.bm2cy.dainty2.component.overview.views.Overview
import com.bm2cy.dainty2.internal.RecentListInterface
import com.bm2cy.dainty2.internal.WebTabManager
import com.bm2cy.dainty2.utils.LogUtil
import com.bm2cy.dainty2.view.WebActivity

class TabReviewViewPresenter{

    companion object Adapter{
        @JvmStatic
        @BindingAdapter("setAdapter")
        fun setAdapter(rl: Overview, command: RecentListInterface){
            command.execute(rl)
        }
    }

    val recentListCommand: RecentListInterface = object : RecentListInterface{
        override fun execute(layout: Overview) {
            LogUtil.d("setTaskStack")
            setStackAdapter(layout)

            layout.setCallbacks(object : Overview.RecentsViewCallbacks {
                override fun onCardDismissed(position: Int) {
                    LogUtil.d("onCardDismissed:$position")
                }

                override fun onAllCardsDismissed() {
                    LogUtil.d("onAllCardsDismissed")
                }

            })
            onClick(layout)
        }
    }

    private fun setStackAdapter(layout: Overview){
        val models: List<WebBean> = WebTabManager.getInstance().getCacheWebTab()
        LogUtil.d("setStackAdapter:${models}")
        layout.setTaskStack(object : OverviewAdapter<ViewHolder<View, WebBean>, WebBean>(models){
            override fun onCreateViewHolder(context: Context?, parent: ViewGroup?): ViewHolder<View, WebBean> {
                val view = LayoutInflater.from(context).inflate(R.layout.recent_tab_card, parent, false)
                return ViewHolder<View, WebBean>(view)
            }

            override fun onBindViewHolder(vh: ViewHolder<View, WebBean>?, position: Int){
                vh?.let {
                    val wb = it.model
                    it.itemView.findViewById<TextView>(R.id.tab_title).text = wb.tabTitle
                    it.itemView.findViewById<ImageView>(R.id.tab_icon).setImageDrawable(wb.tabIcon)
                    val recentContent = it.itemView.findViewById<ImageView>(R.id.recent_content)
                    recentContent.setImageDrawable(wb.picture)
                    it.itemView.setOnClickListener {
                        LogUtil.d("recent content click:${position}")
                        WebTabManager.getInstance().joinWebTabToLast(wb.id)
                        val activity = Dainty2Application.app.curActivity
                        val intent = Intent(activity, WebActivity::class.java)
                        activity.startActivityForResult(intent, 100)
                    }

                }

            }

        })
    }


    fun onClick(view: View){
        WebTabManager.getInstance().divideAllWebView()
        WebTabManager.getInstance().addNewTab()
        val activity = Dainty2Application.app.curActivity
        val intent = Intent(activity, WebActivity::class.java)
        activity.startActivityForResult(intent, 100)
    }

    fun updateRecentList(layout: Overview){
        setStackAdapter(layout)
    }
}