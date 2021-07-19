package com.tnp.simplerecord.navigation.model

/**
 * 중복 follow 금지하기 위한 map 생성
 */
data class FollowDTO (
    var followerCount : Int = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),

    var followingCount : Int = 0,
    var followings : MutableMap<String,Boolean> = HashMap()
)