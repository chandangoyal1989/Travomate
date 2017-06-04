package com.travomate

import org.bson.types.ObjectId

class Comment {

    static mapWith = "mongo"

    ObjectId id
    String commentText
    String postId
    Long postedBy
    String postDate
    String parentCommentId


    static constraints = {
    }

    static mapping = {
        commentText type: 'text'
        collection 'post_comment'
    }
}
