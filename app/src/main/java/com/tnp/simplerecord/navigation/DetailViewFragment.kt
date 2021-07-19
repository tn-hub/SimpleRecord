package com.tnp.simplerecord.navigation

import android.content.Intent
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
import com.tnp.simplerecord.navigation.model.ContentDTO
import com.tnp.simplerecord.navigation.util.FcmPush
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        // recyclerview - 세로(linearlayout)
        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init { // 생성자
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // clear
                contentDTOs.clear()
                contentUidList.clear()
                // querySnapshot null체크
                if(querySnapshot == null) return@addSnapshotListener

                // 스냅샷
                for (snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                // 새로고침
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }
        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) // RecyclerView를 사용할 떄 메모리를 적게 사용하기 위함

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

//        fun getProfileImages(){
//            FirebaseFirestore.getInstance().collection("profileImages").document(cont[position].uid!!).get().addOnCompleteListener { task ->
//                if(task.isSuccessful){
//                    var url = task.result!!["image"]
//                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentview_imageview_profile)
//                }
//            }
//
//            firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
//                if(documentSnapshot == null) return@addSnapshotListener
//                if(documentSnapshot.data != null){
//                    var url = documentSnapshot?.data!!["image"]
//                    Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
//                }
//            }
//        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            // 서버에서 넘어온 데이터 맵핑
            var viewholder = (holder as CustomViewHolder).itemView
            // userid
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![position].userId
            // profileImage(glide)
            // Q) 변경
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).apply(RequestOptions().circleCrop()).into(viewholder.detailviewitem_profile_image)
            // image(glide)
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_imageview_content)
            // explain
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].explain
            // likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs!![position].favoriteCount
            // [좋아요] 클릭
            viewholder.detailviewitem_favorite_imageview.setOnClickListener{
                favoriteEvent(position)
            }
            // 페이지 로드 시 좋아요/좋아요X 설정(로그인 유저 아이디 기준)
            if(contentDTOs!![position].favorites.containsKey(uid)){
                // 채워져있는 하트
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            } else {
                // 비워져있는 하트
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            // [유저 계정] 클릭
            viewholder.detailviewitem_profile_image.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            viewholder.detailviewitem_profile_textview.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }

            // [코멘트] 클릭
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }
        }

        fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction{ transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    // 좋아요 취소
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                } else {
                    // 좋아요 추가
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        /**
         * 좋아요 알람
         */
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email + getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid, "SimpleRecord", message)
        }
    }
}