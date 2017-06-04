package com.travomate

import org.bson.types.ObjectId

class Notification {

    static mapWith = "mongo"

    ObjectId id
    String postId
    Long postedBy
    Long notifiedUserId
    String postStatus
    String postDate
    String notificationType
    String postType

    static mapping = {
        collection 'notification'
    }
}
