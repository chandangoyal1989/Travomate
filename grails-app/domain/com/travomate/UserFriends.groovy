package com.travomate

class UserFriends {

    UserProfile profileUser
    UserProfile friend
    Date friendshipDate


    static mapping = {
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'USER_FRIENDSHIP_SEQ']
    }
}
