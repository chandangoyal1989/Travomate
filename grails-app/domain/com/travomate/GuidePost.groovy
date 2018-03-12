package com.travomate

import org.bson.types.ObjectId

class GuidePost {

    static mapWith = "mongo"

    ObjectId id
    Long userId
    String place
    String serviceStartTime
    String serviceEndTime
    String serviceFromDate
    String serviceToDate
    String serviceDescription
    String postDescription
    String postTime
    Double price

    static constraints = {
        postDescription(nullable: true)
        serviceDescription(nullable: true)
    }

    static mapping = {
        collection 'guide_post'
    }
}
