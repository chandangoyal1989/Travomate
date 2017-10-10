package com.travomate

import org.bson.types.ObjectId


class TravellerPost {

    static mapWith = "mongo"

    ObjectId id
    Long userId
    String source
    String destination
    String startDate
    String endDate
    String startTime
    String endTime
    String postDescription
    String postTime

    static constraints = {
        postDescription(nullable: true)
    }

    static mapping = {
        collection 'traveller_post'
    }
}
