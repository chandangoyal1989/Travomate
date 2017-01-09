package com.travomate.security

import com.travomate.User

class UserOTP {

    User user
    String otp
    String source

    static mapping = {
        table  'user_otp'
        id generator:'org.hibernate.id.enhanced.SequenceStyleGenerator', params:[sequence:'USER_OTP_AUTHENTICATION_SEQ']
    }

    static constraints = {
        user(blank:false)
        otp(nullable: true)
        source(blank: false)
    }
}
