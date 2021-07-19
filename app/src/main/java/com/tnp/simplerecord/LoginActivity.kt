package com.tnp.simplerecord

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 이용가이드 SharedPreference
        if(MySharedPreferences.getFirstUse(this).isNullOrBlank()){
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

        // 인증
        auth = FirebaseAuth.getInstance()

        // [로그인]
        btn_login_email.setOnClickListener{
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

            if(et_email.text.toString().equals("") && et_email.length() == 0){
                Toast.makeText(this, "Email 입력해주세요.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!et_email.text.toString().matches(emailPattern.toRegex())) {
                Toast.makeText(this, "이메일 형식이 잘못되었습니다.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(et_password.text.toString().equals("") && et_password.length() == 0){
                Toast.makeText(this, "Password 입력해주세요.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signinAndSignup()
        }

        // [구글 로그인] step 1
        btn_login_google.setOnClickListener{
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }
    // [구글 로그인] step 2
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result!!.isSuccess){
                var account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }
    // [구글 로그인] step 3
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    moveMainPage(task.result?.user)
                } else {
                    Toast.makeText(this, task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener {
            task ->
                if(task.isSuccessful){
                    // 아이디 생성 성공
                    moveMainPage(task.result?.user)
                } else {
                    signinEmail()
                }
        }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    // 로그인 성공
                    moveMainPage(task.result?.user)
                } else {
                    // 로그인 실패
                    Toast.makeText(this, task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
}

