package com.tnp.simplerecord.intro

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_intro_pager.view.*

class PagerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    private val itemImage = itemView.pager_item_image
    private val itemContent = itemView.pager_item_text
    private val itemBg = itemView.pager_item_bg

    fun bindWithView(pageItem: PageItem){
        itemImage.setImageResource(pageItem.imageSrc)
        itemContent.text = pageItem.content
        itemBg.setBackgroundResource(pageItem.bgColor)
    }

}