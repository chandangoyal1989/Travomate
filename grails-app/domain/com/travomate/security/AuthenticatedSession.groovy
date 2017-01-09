package com.travomate.security

import com.travomate.User

class AuthenticatedSession {

    User user
    String sid
    Date dateCreated
    Date lastUpdated

    static constraints = {
        user(blank:false)
        sid(blank:false)
    }

    static mapping = {
        table  'authenticated_session'
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'AUTHENTICATED_SESSION_SEQ']
    }
}
