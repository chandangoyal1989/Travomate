package com.travomate.dto

import org.bson.types.ObjectId

/**
 * Created by mchopra on 5/16/2017.
 */
class LikeDTO {

    ObjectId id
    String likedObjectId
    String likedObjectType
    Long likedBy
    String likedOn
}
