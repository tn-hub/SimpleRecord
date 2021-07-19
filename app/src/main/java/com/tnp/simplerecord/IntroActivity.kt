package com.tnp.simplerecord

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.tnp.simplerecord.intro.IntroPagerRecyclerAdapter
import com.tnp.simplerecord.intro.PageItem
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    // 데이터 배열 선언
    private var pageItemList = ArrayList<PageItem>()
    private lateinit var introPagerRecyclerAdapter : IntroPagerRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // 데이터 배열 준비
        pageItemList.add(PageItem(R.color.colorIntro_1, R.drawable.intro1, "모두와 이야기를\n나누는 공간"))
        pageItemList.add(PageItem(R.color.colorIntro_2, R.drawable.intro2, "사진과 감상을\n기록하고 공유하세요"))
        pageItemList.add(PageItem(R.color.colorIntro_3, R.drawable.intro3, "댓글을 통해 소통하세요"))
        pageItemList.add(PageItem(R.color.colorWhite, R.drawable.intro4, "SimpleRecord\n지금 바로 시작해보세요"))

        // 어뎁터 인스턴스 생성
        introPagerRecyclerAdapter = IntroPagerRecyclerAdapter(pageItemList)
        intro_viewpager.apply {
            adapter = introPagerRecyclerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        // 시작하기 -> login
        intro_btn_start.setOnClickListener{
            MySharedPreferences.setFirstUse(this, "N")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}