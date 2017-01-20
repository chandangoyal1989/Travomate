package com.travomate

import com.travomate.dto.UserProfileDTO
import com.travomate.tool.UserProfileDTOMapper
import grails.converters.JSON
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class RestAPIController extends Rest{

    AuthenticationService authenticationService
    RestAPIService restAPIService
    MongoService mongoService
    def mailService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()

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
            try {
                restAPIService.callSMSService(postParams, otp)
                success("Message sent to user's mobile no")
            }catch(Exception e){
                e.printStackTrace()
                error("Unable to send the message")
            }

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

    def sendMail() {
        def mailParameters = JSON.parse(request.JSON.toString())
        String mailId = mailParameters.mailId
        String contact = mailParameters.contact
        Long userid = Long.parseLong(mailParameters.userid + "")
        User user = restAPIService.getUser(userid)
        if(user != null) {
            String otp = restAPIService.getUserOTP(user, Constants.OTP_MAIL_SOURCE_KEY)

            log.info("OTP for user : " + userid + " is " + otp)

            //Mail scenario using Grails Plugin
            try {
                mailService.sendMail {
                    from "travomateotp@gmail.com"
                    to mailId
                    subject "Account Verification"
                    body(view: "/verifymail", model: [otp: otp, firstName: user.firstName, lastName: user.lastName ?: ""])
                }
                log.info("mail sent")
                success("Email sent successfully")

            }
            catch (Exception mailException) {
                mailException.printStackTrace()
                error("Due to some server issue, unable to send the mail")
            }


        } else {
            notFound("User does not exist")
        }
    }


    def uploadPic(){
//        def postParams = JSON.parse(request.JSON.toString())
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if(user != null) {
            String picType = params.picType
            System.out.println("params : "+params)
            CommonsMultipartFile imageFile = request.getFile('imageFile')
            byte[] photo = imageFile.bytes
            Boolean imageSaved = restAPIService.saveImage(user, picType, photo)
           if(imageSaved) {
               response.setHeader('Content-length', photo.length.toString())
               response.contentType="image/jpg" //or whatever the format is...
               response.outputStream << photo
           } else {
               error("Invalid pic type")
           }

        } else {
            notFound("User does not exist")
        }
    }

    def createProfile(){
        def postParams = JSON.parse(request.JSON.toString())
        System.out.println("post params : "+postParams)
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if(userId != null){

            restAPIService.createOrModifyProfile(user, postParams)
            success("Profile Changes done")
        } else {
            notFound("User does not exist")
        }
    }


    def deleteProfile(){
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if(user != null) {
            restAPIService.deleteProfile(user)
            success("User profile deleted")
        } else {
            notFound("User does not exist")
        }
    }


    def getImage(){
//        def postParams = JSON.parse(request.JSON.toString())
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if(user != null) {
            String picType = params.picType
            UserProfile userProfile = UserProfile.findByUser(user)
            byte[] photo = userProfile."${picType}"
            response.setHeader('Content-length', photo.length.toString())
            response.contentType="image/jpg" //or whatever the format is...
            response.outputStream << photo
        } else {
            notFound("User does not exist")
        }
    }


    def showProfile() {
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if(userId != null){

            UserProfile profile = restAPIService.getProfile(user)
            UserProfileDTO userProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(profile)
            Expando profileResponse = new Expando()
            profileResponse.userProfile = userProfileDTO
            JSON results = profileResponse.properties as JSON
            success(results)
        } else {
            notFound("User does not exist")
        }
    }


    def sendFriendRequest(){
        def postParams = JSON.parse(request.JSON.toString())
        Long fromUserId = postParams.fromUserId != null ? Long.parseLong(postParams.fromUserId + "") : null
        Long toUserId = postParams.toUserId != null ? Long.parseLong(postParams.toUserId + "") : null
        System.out.println("from user id : "+fromUserId+" to user id : "+toUserId)
        if(fromUserId != null && toUserId != null){
            Boolean isSaved = restAPIService.saveFriendRequest(fromUserId, toUserId)
            if(isSaved){
                success("Friend request sent")
            } else {
                error("Friend request could not be sent")
            }
        } else {
            error("Incorrect user information")
        }
    }


    def acceptFriendRequest(){
        def postParams = JSON.parse(request.JSON.toString())
        Long fromUserId = postParams.fromUserId != null ? Long.parseLong(postParams.fromUserId + "") : null
        Long toUserId = postParams.toUserId != null ? Long.parseLong(postParams.toUserId + "") : null
        System.out.println("from user id : "+fromUserId+" to user id : "+toUserId)
        if(fromUserId != null && toUserId != null){
            Boolean isSaved = restAPIService.saveFriendship(fromUserId, toUserId)
            if(isSaved){
                success("Friend request Accepted")
            } else {
                error("Incorrect information to accept the friend request")
            }
        } else {
            error("Incorrect user information")
        }
    }


    def deleteFriend(){
        Long profileUserId = params.profileUserId != null ? Long.parseLong(params.profileUserId) : null
        Long toBeDeletedUserId = params.toBeDeletedUserId != null ? Long.parseLong(params.toBeDeletedUserId) : null
        if(profileUserId != null && toBeDeletedUserId != null){
            Boolean isDeleted = restAPIService.deleteFriend(profileUserId, toBeDeletedUserId)
            if(isDeleted){
                success("Friend deleted")
            } else {
                error("Incorrect user information")
            }
        } else {
            error("Incorrect user information")
        }
    }


    def deleteFriendRequest(){
        Long senderId = params.senderId != null ? Long.parseLong(params.senderId) : null
        Long recipientId = params.recipientId != null ? Long.parseLong(params.recipientId) : null
        if(senderId != null && recipientId != null){
            restAPIService.deleteFriendRequest(senderId, recipientId)
            success("Friend request deleted")
        } else {
            error("Incorrect user information")
        }
    }

    def getFriends(){
        Long profileUserId = params.profileUserId ? Long.parseLong(params.profileUserId) : null
        if(profileUserId != null){
            User user = restAPIService.getUser(profileUserId)
            UserProfile profileUser = restAPIService.getProfile(user)
            List<UserFriends> userFriends = restAPIService.getUserFriends(profileUser)
            Expando userFriendsExpando = restAPIService.constructUserFriendList(userFriends)
            userFriendsExpando.profileUser = profileUserId
            JSON results = userFriendsExpando.properties as JSON
            success(results)
        } else {
            error("Missing profile user id")
        }
    }


    def insertMongo(){
        System.out.println("in insertMongo action")
        mongoService.saveTravellerPost()
        success("Mongo object saved")
    }

    def logout() {
        authenticationService.deleteSession(params.sid)
        success("User session deleted")
    }
}
