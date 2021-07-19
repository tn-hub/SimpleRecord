package com.tnp.simplerecord.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.tnp.simplerecord.R
import com.tnp.simplerecord.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // initiate Firebase
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 앨범 open -> here onActivityResult
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // [업로드]
        addphoto_btn_upload.setOnClickListener{
            contentUpload()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                // 선택한 사진 path
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            } else {
                // 사진 선택 취소 -> 액티비티 닫기
                finish()
            }
        }
    }

    fun contentUpload() {
        // 파일명 중복X
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png";

        // 이미지 업로드
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 업로드 방식 1 : Promise method 구글 권장
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()
            contentDTO.imageUrl = uri.toString()
            contentDTO.uid = auth?.currentUser?.uid
            contentDTO.userId = auth?.currentUser?.email
            contentDTO.explain = addphoto_edit_explain.text.toString()
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
            return@addOnSuccessListener
        }

        /*
        // 업로드 방식 2 : callback method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()
                contentDTO.imageUrl = uri.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.userId = auth?.currentUser?.email
                contentDTO.explain = addphoto_edit_explain.text.toString()
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        */

    }
}