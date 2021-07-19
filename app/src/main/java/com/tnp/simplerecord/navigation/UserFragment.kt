package com.tnp.simplerecord.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tnp.simplerecord.LoginActivity
import com.tnp.simplerecord.MainActivity
import com.tnp.simplerecord.R
import com.tnp.simplerecord.navigation.model.AlarmDTO
import com.tnp.simplerecord.navigation.model.ContentDTO
import com.tnp.simplerecord.navigation.model.FollowDTO
import com.tnp.simplerecord.navigation.util.FcmPush
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*; //

class UserFragment : Fragment() {
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var currentUserUid : String? = null
    var auth : FirebaseAuth? = null
    companion object { // static 선언과 같음
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        uid = arguments?.getString("destinationUid")
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        firestore = FirebaseFirestore.getInstance()

        if(uid == currentUserUid){
            // my page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener{
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            // otheruser page
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
            // [follow] 클릭
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        // RecyclerView, 그리드레이아웃 사용
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity,3)

        // [프로필사진] 클릭 -> Main onActivityResult
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getFollowerAndFollowing()
        getProfileImages()
        return fragmentView
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()

                if(followDTO?.followers?.containsKey((currentUserUid!!))){
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!,R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                } else {
                    if(uid != currentUserUid){
                        fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                    }
                }
            }
        }
    }

    /**
     * follow 기능. transaction
     * A currentUserUid : 유저 - followingCount / followings 덕질목록
     * B uid(destinationUid) : 대상 - followerCount / followers 팬목록
     */
    fun requestFollow(){
        // [step 1] currentUserUid
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction{ transaction ->
            // 0. currentUserUid 값 받아와서 FollowDTO로 캐스팅
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            // 1. 데이터 없을 때
            if(followDTO == null){
                // data 설정
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true
                // data 전달
                transaction.set(tsDocFollowing,followDTO)
                return@runTransaction
            }
            // 2. 데이터에 그 아이디를 포함하고 있으면 취소, 없으면 추가
            if(followDTO.followings.containsKey(uid)){
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings?.remove(uid)
            } else {
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO!!.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        // [step 2] uid(destinationUid)
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            // 0. uid(destinationUid) 값 받아와서 FollowDTO로 캐스팅
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }
            // 1. follower 목록에 내아이디 갖고 있을 경우 취소, 없으면 추가
            if(followDTO!!.followers.containsKey(currentUserUid)){
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid)
            } else {
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }

    /**
     * 코멘트 알람
     */
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        FcmPush.instance.sendMessage(destinationUid, "SimpleRecord", message)
    }

    /**
     * 프로필 사진 다운로드 from firebase storage
     * > 실시간 반영 확인 Snapshot 사용
     */
    @SuppressLint("UseRequireInsteadOfGet")
    fun getProfileImages(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, error ->
                if(querySnapshot == null) return@addSnapshotListener

                // get data
                for (snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!) // !! null safety 제거
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
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
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)


        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}