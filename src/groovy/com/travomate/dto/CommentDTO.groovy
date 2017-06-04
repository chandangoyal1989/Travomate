package com.travomate.dto

import org.bson.types.ObjectId

/**
 * Created by mchopra on 5/13/2017.
 */
class CommentDTO {
    ObjectId id
    String commentText
    String postId
    Long postedBy
    String postDate
    String parentCommentId
}
