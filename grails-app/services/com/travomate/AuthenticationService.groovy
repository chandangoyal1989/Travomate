package com.travomate

import com.travomate.security.AuthenticatedSession

class AuthenticationService {

    def springSecurityService
    def passwordEncoder

    public String createSession(User user){
        String sid = null
        AuthenticatedSession asession = new AuthenticatedSession()
        asession.user = user
        asession.sid = UUID.randomUUID().toString().toUpperCase().replace("-", "")
        sid = asession.sid
        def result = asession.save()
        return sid
    }

    public Integer verifyPassword(User user, String inputPassword){
        log.info("verifyPassword : "+inputPassword)
        Integer isVerified = -1
        if(user != null) {
            log.info("encoded input password : "+springSecurityService.encodePassword(inputPassword) + " user password : "+user.password)
            if (passwordEncoder.isPasswordValid(user.password, inputPassword, null)) { //validates raw password against hashed
                isVerified = 1
            } else {
                isVerified = 0
            }
        }
        return isVerified
    }

    public Integer verifyEmailForOAuthService(String email){
        log.info("verifyEmailForOAuthService "+email)
        User user = User.findByEmail(email)
        if(user != null){
            return 1
        }
        return 0
    }


    public Integer createUser(def postParams){
        Integer userExist = 0
        User newUser = null
        if(postParams.contact != null){
            newUser = User.findByContact(postParams.contact)
        } else if(postParams.email != null){
            newUser = User.findByEmail(postParams.email)

        }

        if(newUser == null){
            newUser = new User()
            newUser.enabled = true
            newUser.accountExpired = false
            newUser.accountLocked = false
            newUser.passwordExpired = false
            newUser.email = postParams.email?:null
            newUser.contact = postParams.contact ? Long.parseLong(postParams.contact + "") : null
            newUser.isExternalLogin = postParams.isExternalLogin != null ? postParams.isExternalLogin : false
            newUser.externalAuthServiceName =  postParams.oAuthServiceName?:null
            newUser.password = postParams.password?:null
            newUser.save(flush: true, failOnError: true)
        } else {
            if(newUser.isContactVerified == false){
                userExist = 1
            } else {
                userExist = 2
            }
        }

        return userExist
    }
}
