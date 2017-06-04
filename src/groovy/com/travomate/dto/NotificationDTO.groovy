package com.travomate.dto

import org.bson.types.ObjectId

/**
 * Created by mchopra on 3/22/2017.
 */
class NotificationDTO {
    ObjectId id
    ObjectId postId
    Long postedBy
    Long notifiedUserId
    String postStatus
    String postDate
    String notificationType

}
