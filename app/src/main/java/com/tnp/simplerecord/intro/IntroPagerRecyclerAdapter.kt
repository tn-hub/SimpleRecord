package com.tnp.simplerecord.intro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tnp.simplerecord.R
import kotlinx.android.synthetic.main.item_intro_pager.view.*

class IntroPagerRecyclerAdapter(private var pageList: ArrayList<PageItem>) : RecyclerView.Adapter<PagerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_intro_pager, parent, false ))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        // 데이터와 뷰 묶기
        holder.bindWithView(pageList[position])
    }

    override fun getItemCount(): Int {
        return pageList.size
    }


}
