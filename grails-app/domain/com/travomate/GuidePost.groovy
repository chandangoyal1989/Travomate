package com.travomate

import org.bson.types.ObjectId

class GuidePost {

    static mapWith = "mongo"

    ObjectId id
    Long userId
    String place
    String serviceTime
    String serviceDate
    String serviceDescription
    String postDescription
    String postTime

    static constraints = {
        postDescription(nullable: true)
        serviceDescription(nullable: true)
    }

    static mapping = {
        collection 'guide_post'
    }
}
