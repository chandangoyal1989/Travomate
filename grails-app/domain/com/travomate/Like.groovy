package com.travomate

import org.bson.types.ObjectId

class Like {

    static mapWith = "mongo"

    ObjectId id
    String likedObjectId
    String likedObjectType
    Long likedBy
    String likedOn

    static constraints = {
    }

    static mapping = {
        collection 'post_like'
    }
}
