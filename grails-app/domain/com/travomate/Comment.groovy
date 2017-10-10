package com.travomate

import org.bson.types.ObjectId

class Comment {

    static mapWith = "mongo"

    ObjectId id
    String commentText
    String postId
    Long postedById
    String postDate
    String parentCommentId


    static constraints = {
        parentCommentId nullable:true
    }

    static mapping = {
        commentText type: 'text'
        collection 'post_comment'
    }
}
