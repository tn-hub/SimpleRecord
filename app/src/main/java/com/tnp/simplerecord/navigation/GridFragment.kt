package com.tnp.simplerecord.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.tnp.simplerecord.R
import com.tnp.simplerecord.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_grid.view.*

class GridFragment : Fragment() {
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        firestore = FirebaseFirestore.getInstance()
        fragmentView?.gridfragment_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.gridfragment_recyclerview?.layoutManager = GridLayoutManager(activity,3)
        return fragmentView
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, error ->
                if(querySnapshot == null) return@addSnapshotListener

                // get data
                for (snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!) // !! null safety 제거
                }
                notifyDataSetChanged() // RecyclerView 새로고침
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 포스팅한 데이터 목록
            var width = resources.displayMetrics.widthPixels / 3 // 화면 폭
            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width) // 정사각형
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 이미지뷰 -> glide 다운로드
            var imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}