package com.travomate

import com.travomate.dto.*
import com.travomate.tool.*
import grails.converters.JSON
import org.bson.types.ObjectId
import org.springframework.web.multipart.commons.CommonsMultipartFile

class RestAPIController extends Rest{

    AuthenticationService authenticationService
    RestAPIService restAPIService
    MongoService mongoService
    def mailService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()
    UserFriendRequestDTOMapper userFriendRequestDTOMapper = UserFriendRequestDTOMapper.getInstance()
    NotificationDTOMapper notificationDTOMapper = NotificationDTOMapper.getInstance()
    TravellerPostDTOMapper travellerPostDTOMapper = TravellerPostDTOMapper.getInstance()
    GuidePostDTOMapper guidePostDTOMapper = GuidePostDTOMapper.getInstance()
    CommentDTOMapper commentDTOMapper = CommentDTOMapper.getInstance()
    LikeDTOMapper likeDTOMapper = LikeDTOMapper.getInstance()
    UserProfileImageDTOMapper userProfileImageDTOMapper = UserProfileImageDTOMapper.getInstance()

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
            Boolean imageSaved = restAPIService.saveImage(user, picType, imageFile)
           if(imageSaved) {
               success("Profile Image uploaded successfully")
           } else {
               error("Invalid pic type")
           }

        } else {
            notFound("User does not exist")
        }
    }

    def createProfile(){
        def postParams = JSON.parse(request.JSON.toString())
        log.info("post params : "+postParams)
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
            List<UserProfileImage> userProfileImageList = restAPIService.getListOfImagesForUser(user, picType)
            UserProfileImageDTO[] userProfileImageDTOs = userProfileImageDTOMapper.mapUserProfileImageListToDTOArray(userProfileImageList)
            Expando imageResponseExpando = new Expando()
            imageResponseExpando.userImages = userProfileImageDTOs
            JSON results = imageResponseExpando.properties as JSON
            success(results)
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
        log.info("from user id : "+fromUserId+" to user id : "+toUserId)
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
        log.info("from user id : "+fromUserId+" to user id : "+toUserId)
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


    def showFriendRequests(){
        Long profileUserId = params.profileUserId != null ? Long.parseLong(params.profileUserId) : null
        if(profileUserId != null){
            List<UserFriendRequest> friendRequestList = restAPIService.getFriendRequests(profileUserId)
            UserFriendRequestDTO[] userFriendRequestDTOs =  userFriendRequestDTOMapper.mapUserFriendRequestListToUserFriendRequestDTOArray(friendRequestList)
            Expando friendRequestResponse = new Expando()
            friendRequestResponse.friendRequest = userFriendRequestDTOs
            friendRequestResponse.profileUserId = profileUserId
            JSON results = friendRequestResponse.properties as JSON
            success(results)
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
        log.info("in insertMongo action")
        mongoService.insertMongo()
        success("Mongo object saved")
    }

    def postTravellerFeed() {
        log.info(" in post feed")
        def postParams = JSON.parse(request.JSON.toString())
        ObjectId postId = mongoService.createOrModifyTravellerPost(postParams, null)
        mongoService.sendNotification(postId.toString(), postParams, Constants.PostType.TRAVELLER)
        Expando resultExpando = new Expando()
        resultExpando.postId = postId.toString()
        JSON results = resultExpando.properties as JSON
        success(results, "Traveller Post saved")

    }

    def deleteTravellerFeed(){
//        Long postId = params.postId != null ? Long.parseLong(params.postId + "") : null
        String postId = params.postId
        if(postId != null) {
            mongoService.deleteNotifications(postId)
            mongoService.deleteTravellerPost(postId)
        } else {
            error("Post Id is missing")
        }
    }

    def editTravellerPost(){
        def postId = new ObjectId(params.postId)
        def postParams = JSON.parse(request.JSON.toString())
        if(postId != null) {
            mongoService.createOrModifyTravellerPost(postParams, postId)
            success("Traveller post modified")
        } else {
            error("Post Id is missing")
        }
    }

    def getTravellerFeeds(){
        log.info("getTravellerFeeds")
        def topPosts = mongoService.getLatestTravellerFeeds(Integer.parseInt(params.offset))
        TravellerPostDTO[] travellerPostDTOs = travellerPostDTOMapper.mapTravellerPostListToTravellerPostDTOArray(topPosts)
        Expando travellerFeedResponse = new Expando()
        travellerFeedResponse.travellerFeed = travellerPostDTOs
        JSON results = travellerFeedResponse.properties as JSON
        success(results)
    }


    def postGuideFeed(){
        log.info(" in postGuideFeed")
        def postParams = JSON.parse(request.JSON.toString())
        def guidePostId = mongoService.createOrModifyGuidePost(postParams, null)
        mongoService.sendNotification(guidePostId.toString(), postParams, Constants.PostType.GUIDE)
        Expando resultExpando = new Expando()
        resultExpando.postId = guidePostId.toString()
        JSON results = resultExpando.properties as JSON
        success(results, "Guide Post saved")
    }

    def deleteGuidePost(){
        log.info("In deleteGuidePost")
        String postId = params.postId
        if(postId != null){
            mongoService.deleteNotifications(postId)
            mongoService.deleteGuidePost(postId)
            success("Guide Post deleted")
        } else {
            error("Guide  Post Id is missing")
        }
    }

    def getGuideFeeds(){
        log.info("getGuideFeeds")
        def topPosts = mongoService.getTopGuideFeeds(Integer.parseInt(params.offset))
        GuidePostDTO[] guidePostDTOs = guidePostDTOMapper.mapGuidePostListToGuidePostDTOArray(topPosts)
        Expando guideFeedResponse = new Expando()
        guideFeedResponse.guideFeed = guidePostDTOs
        JSON results = guideFeedResponse.properties as JSON
        success(results)
    }


    def modifyGuidePost(){
        log.info("In modifyGuidePost")
        def postId = new ObjectId(params.postId)
        def postParams = JSON.parse(request.JSON.toString())
        if(postId != null){
            mongoService.createOrModifyGuidePost(postParams, postId)
            success("Modified Guide Post")
        } else {
            error("Guide Post Id is missing")
        }
    }

    def storeUserLocation(){
        log.info("In storeUserLocation")
        def postParams = JSON.parse(request.JSON.toString())
        mongoService.saveUserLatLong(postParams)
        success("User Location saved successfully")

    }

    def findNearUsers(){

        def postParams = JSON.parse(request.JSON.toString())
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")];
        List<Long> nearbyUserIds = mongoService.nearSphereWIthMaxDistance(location)
        log.info("nearbyUserIds : "+nearbyUserIds)
        List<User> nearByUsers = restAPIService.getListOfUsersByListOfId(nearbyUserIds)
        List<UserProfile> userProfileList = restAPIService.getListOfUserProfilesByListOfUser(nearByUsers)
        UserProfileDTO[] userProfileDTOs = userProfileDTOMapper.mapUserProfileListToUserProfileDTOArray(userProfileList)
        Expando nearbyUsersResponse = new Expando()
        nearbyUsersResponse.nearbyUsers = userProfileDTOs
        JSON results = nearbyUsersResponse.properties as JSON
        success(results)

    }


    def userNotification(){
        def postParams = JSON.parse(request.JSON.toString())
        Long userId = Long.parseLong(params.profileUserId + "")
        List<Notification> userNotifications = mongoService.getUserNotifications(userId)
        log.info("userNotifications for ID : "+userId + " is "+userNotifications)
        NotificationDTO[] notificationDTOs =  notificationDTOMapper.mapNotificationListToNotificationDTOArray(userNotifications)
        Expando userNotificationsResponse = new Expando()
        userNotificationsResponse.notifications = notificationDTOs
        userNotificationsResponse.profileUserId = userId
        JSON results = userNotificationsResponse.properties as JSON
        success(results)
    }

    def searchUser(){
        String userName = params.name
        List<UserProfile> userProfile = restAPIService.getUserProfileByNameLike(userName)
        UserProfileDTO[] userProfileDTOs = userProfileDTOMapper.mapUserProfileListToUserProfileDTOArray(userProfile)
        Expando userProfileResponse = new Expando()
        userProfileResponse.userProfile = userProfileDTOs
        userProfileResponse.name = userName
        JSON results = userProfileResponse.properties as JSON
        success(results)
    }


    def addComment(){
        def postParams = JSON.parse(request.JSON.toString())
        String postId = params.postId
        mongoService.saveComment(postId, postParams)
        success("Comments added to Post")
    }


    def getComments(){
        String postId = params.postId
        List<Comment> commentList = mongoService.getCommentListForPost(postId)
        List<Expando> commentExpandoList = new ArrayList<Expando>()
        Expando commentExpando = null
        commentList?.each{ comment ->
            commentExpando = new Expando()
            commentExpando.id = comment.id.toString()
            commentExpando.commentText = comment.commentText
            commentExpando.postDate = comment.postDate
            commentExpando.postedBy = UserProfile.findByUser(User.get(comment.postedBy))
            List<Comment> replies = Comment.findAllByPostIdAndParentCommentId(comment.id.toString())
            commentExpando.replies = commentDTOMapper.mapCommentListToCommentDTOArray(replies)

            //Add comment likes
            List<Like> commentLikes = Like.findAllByLikedObjectIdAndLikedObjectType(comment.id.toString(), Constants.LIKE_COMMENT_STRING)
            commentExpando.likes = likeDTOMapper.mapLikeListToLikeDTOArray(commentLikes)

            commentExpandoList.add(commentExpando.properties)
        }

        Expando commentResponse = new Expando()
        commentResponse.postComments = commentExpandoList
        JSON results = commentResponse.properties as JSON
        success(results)
    }


    def addPostLike(){
        def postParams = JSON.parse(request.JSON.toString())
        String likedObjectId = params.objectId
        mongoService.addUserLike(likedObjectId, postParams)
        success("Like added")

    }


    def getPostLikes(){
        String likedPostId = params.objectId
        List<Like> postLikes = mongoService.getUserLikesForPost(likedPostId)
        LikeDTO[] likeDTOs = likeDTOMapper.mapLikeListToLikeDTOArray(postLikes)
        Expando postLikeExpando = new Expando()
        postLikeExpando.postLikes = likeDTOs
        postLikeExpando.postId = likedPostId
        JSON results = postLikeExpando.properties as JSON
        success(results)
    }


    def resetPassword(){
        def postParams = JSON.parse(request.JSON.toString())
        String newPassword = postParams.password
        String contact  = postParams.contact
        User user = User.findByContact(contact)
        if(user != null) {
            restAPIService.changePassword(user, newPassword)
            success("Password reset successfully")
        } else {
            notFound("Invalid userid")
        }

    }

    def getTripReview(){
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        Expando tripReviewExpando = new Expando()
        tripReviewExpando.review = restAPIService.getTripReviews(userId)
        JSON results = tripReviewExpando.properties as JSON
        success(results)
    }
    def saveTripReview(){
        def postParams = JSON.parse(request.JSON.toString())
        Long userId = postParams.userid ? Long.parseLong(postParams.userid) : null
        User user = User.get(userId)
        if(user != null){
            restAPIService.saveTripReview(postParams)
            success("Trip Review saved successfully")
        } else {
            notFound("Invalid userid")
        }
    }


    def uploadTripReviewPic(){
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if(user != null) {
            String picType = params.picType
            log.info("uploadTripReviewPic params : "+params)
            CommonsMultipartFile imageFile = request.getFile('imageFile')
            Boolean imageSaved = restAPIService.saveTripReviewImage(user, picType, imageFile)
            if(imageSaved) {
                success("Profile Image uploaded successfully")
            } else {
                error("Invalid pic type")
            }

        } else {
            notFound("User does not exist")
        }
    }


    def logout() {
        authenticationService.deleteSession(params.sid)
        success("User session deleted")
    }
}
