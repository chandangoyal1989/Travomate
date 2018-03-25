package com.travomate
import org.bson.types.ObjectId

class UserExpression {
    static mapWith = "mongo"

    ObjectId id
    Long userId
    String description
    String imageLoc

    static constraints = {
    }

    static mapping = {
        collection 'user_expression'
    }
}
