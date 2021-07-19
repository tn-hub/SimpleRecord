package com.tnp.simplerecord.navigation.model

data class AlarmDTO (
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    var kind : Int? = null, // 0: 좋아요, 1: 댓글, 2: 팔로우
    var message : String? = null,
    var timestamp : Long? = null
)