package com.travomate

class UserFriendRequest {

    User recipient
    User sender
    Date requestSent

    static mapping = {
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'USER_FRIEND_REQUEST_SEQ']
    }
}
