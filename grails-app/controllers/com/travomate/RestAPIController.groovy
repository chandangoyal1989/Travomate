package com.travomate

import grails.converters.JSON

class RestAPIController extends Rest{

    AuthenticationService authenticationService
    RestAPIService restAPIService

    def index() {}

    def login(){
        def postParams = JSON.parse(request.JSON.toString())
        String contact = postParams.contact
        String inputPassword = postParams.password
        String email = postParams.email
        String oAuthServiceName = postParams.oAuthServiceName
        log.info("login postparams : "+postParams)
        User user = null
        if(contact != null) {
            user = User.findByContact(contact)
        } else if(email != null){
            user = User.findByEmail(email)
        }
        if(user == null){
            authenticationFailure("User is not logged in")
        } else if(!user.isExternalLogin && !user.isContactVerified){
            log.info "else if"
            authenticationFailure("User is not verified")
        } else {
            log.info("in else")
            String sid = authenticationService.createSession(user)
            Integer isVerified = 0
            log.info "oAuthServiceName " + postParams.oAuthServiceName
            if (oAuthServiceName == null) {
                isVerified = authenticationService.verifyPassword(user, inputPassword)
            } else {
                isVerified = authenticationService.verifyEmailForOAuthService(email)
            }
            if (isVerified == 1) {
                Expando loginResponse = new Expando()
                loginResponse.sid = sid
                loginResponse.userid = user.id
                JSON results = loginResponse.properties as JSON
                success(results)
            } else if (isVerified == 0) {
                authenticationFailure("Input password is not correct")
            } else {
                notFound("User does not exist")
            }
        }
    }

    def signup() {
        def postParams = JSON.parse(request.JSON.toString())
        Integer userExists = authenticationService.createUser(postParams)
        if(userExists == 0){
            success("User created")
        } else if(userExists == 1){
            success("User exists but contact is not verified")
        } else if(userExists == 2){
            success("User exists and contact verified")
        }


    }

    def sendMsg() {
        def postParams = JSON.parse(request.JSON.toString())
        Long userid = postParams?.userid ? Long.parseLong(postParams.userid + "") : null
        String contact = postParams.contact
        User user = null
        if(userid != null) {
            user = restAPIService.getUser(userid)
        } else if(contact != null){
            user = User.findByContact(contact)
        }
        if(user != null) {
            String otp = restAPIService.getUserOTP(user, Constants.OTP_PHONE_SOURCE_KEY)
            restAPIService.callSMSService(postParams, otp)
            success("Message sent to user's mobile no")
        } else{
            notFound("user not found")
        }

    }

    def verifyOTP(){
        def postParams = JSON.parse(request.JSON.toString())
        User user = null
        Long userid = postParams.userid != null ? Long.parseLong(postParams.userid + "") : null
        String contact = null
        if(userid == null){
            contact = postParams.verifiedId
            user = User.findByContact(contact)
        } else {
            user = restAPIService.getUser(userid)
        }
        if(user != null) {
            String verifiedId = postParams.verifiedId
            String otp = postParams.otp
            String source = postParams.source
            Boolean isVerified = restAPIService.checkIfUserOTPIsCorrect(user, otp, source)

            if (isVerified == true) {
                Boolean isUpdated = restAPIService.updateOTPVerificationStatus(user, verifiedId, source)
                if (isUpdated == true) {
                    success("OTP entered is correct")
                } else {
                    error("This id is already registered with different account")
                }
            } else {
                error("Incorrect OTP")
            }
        } else {
            notFound("user does not exists")
        }
    }

    def logout() {
        authenticationService.deleteSession(params.sid)
        success("User session deleted")
    }
}
