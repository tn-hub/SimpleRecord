package com.tnp.simplerecord.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tnp.simplerecord.R
import com.tnp.simplerecord.navigation.model.AlarmDTO
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm,container,false)
        view.alarmfragment_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { querySnapshot, error ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // item_comment.xml 재활용
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // kind에 따라 메세지 분기
            var view = holder.itemView

            // 프로필이미지
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    var url = task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentview_imageview_profile)
                }
            }

            // 알림메세지
            when(alarmDTOList[position].kind){
                0 -> { // 0: 좋아요
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_favorite)
                    view.commentview_textview_profile.text = str_0
                }
                1 -> {// 1: 댓글
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of : " + alarmDTOList[position].message
                    view.commentview_textview_profile.text = str_0
                }
                2 -> {// 2: 팔로우
                    var str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    view.commentview_textview_profile.text = str_0
                }
            }
            view.commentview_textview_comment.visibility = View.INVISIBLE
        }
        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

    }
}