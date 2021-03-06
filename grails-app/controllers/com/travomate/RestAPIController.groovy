package com.travomate

import com.travomate.dto.*
import com.travomate.tool.*
import grails.converters.JSON
import org.bson.types.ObjectId
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.jws.soap.SOAPBinding
import javax.print.DocFlavor
import java.util.zip.DeflaterOutputStream

class RestAPIController extends Rest {

    AuthenticationService authenticationService
    RestAPIService restAPIService
    MongoService mongoService
    def mailService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()
    UserFriendRequestDTOMapper userFriendRequestDTOMapper = UserFriendRequestDTOMapper.getInstance()
    NotificationDTOMapper notificationDTOMapper = NotificationDTOMapper.getInstance()
    TravellerPostDTOMapper travellerPostDTOMapper = TravellerPostDTOMapper.getInstance()
    UserExpressionDTOMapper userExpressionDTOMapper = UserExpressionDTOMapper.getInstance()
    GuidePostDTOMapper guidePostDTOMapper = GuidePostDTOMapper.getInstance()
    CommentDTOMapper commentDTOMapper = CommentDTOMapper.getInstance()
    LikeDTOMapper likeDTOMapper = LikeDTOMapper.getInstance()
    UserProfileImageDTOMapper userProfileImageDTOMapper = UserProfileImageDTOMapper.getInstance()
    TripReviewDTOMapper tripReviewDTOMapper = TripReviewDTOMapper.getInstance()

    def index() {}

    /**
     * Login API
     * @return
     */
    def login() {
        def postParams = JSON.parse(request.JSON.toString())
        String contact = postParams.contact
        String inputPassword = postParams.password
        String email = postParams.email
        String oAuthServiceName = postParams.oAuthServiceName
        User user = null
        if (contact != null) {
            user = User.findByContact(contact)
        } else if (email != null) {
            user = User.findByEmail(email)
        }
        if (user == null) {
            authenticationFailure("User is not logged in")
        } else if (!user.isExternalLogin && !user.isContactVerified) {
            authenticationFailure("User is not verified")
        } else {
            String sid = authenticationService.createSession(user)
            Integer isVerified = 0
            log.info "oAuthAuthentication with service :" + postParams.oAuthServiceName
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
    /**
     * Register device API
     * @return
     */
    def registerDevice() {
        log.info("Storing device token information")
        def postParams = JSON.parse(request.JSON.toString())
        log.info("Devive token:" + postParams.deviceId)
        Long userId = postParams.userId != null ? Long.parseLong(postParams.userId) : null
        User user = User.get(userId)
        if (userId != null) {
            restAPIService.updateDeviceId(user, postParams.deviceId)
            success("Device id for user with id " + postParams.userId + " saved successfully")
        } else {
            notFound("User does not exist")
        }
    }

    /**
     * Sign up API
     * @return
     */
    def signup() {
        def postParams = JSON.parse(request.JSON.toString())
        Integer userExists = authenticationService.createUser(postParams)
        if (userExists == 0) {
            success("User created")
        } else if (userExists == 1) {
            success("User exists but contact is not verified")
        } else if (userExists == 2) {
            success("User exists and contact verified")
        }

    }

    /**
     * API to send message to user's device
     * @return
     */

    def sendMsg() {
        def postParams = JSON.parse(request.JSON.toString())
        Long userid = postParams?.userid ? Long.parseLong(postParams.userid + "") : null
        String contact = postParams.contact
        User user = null
        if (userid != null) {
            user = restAPIService.getUser(userid)
        } else if (contact != null) {
            user = User.findByContact(contact)
        }
        if (user != null) {
            String otp = restAPIService.getUserOTP(user, Constants.OTP_PHONE_SOURCE_KEY)
            try {
                restAPIService.callSMSService(postParams, otp)
                success("Message sent to user's mobile no")
            } catch (Exception e) {
                e.printStackTrace()
                error("Unable to send the message")
            }
        } else {
            notFound("user not found")
        }
    }

    /**
     * API to verify the OTP submitted by user
     * @return
     */
    def verifyOTP() {
        def postParams = JSON.parse(request.JSON.toString())
        User user = null
        Long userid = postParams.userid != null ? Long.parseLong(postParams.userid + "") : null
        String contact = null
        if (userid == null) {
            contact = postParams.verifiedId
            user = User.findByContact(contact)
        } else {
            user = restAPIService.getUser(userid)
        }
        if (user != null) {
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

    /**
     * API to send OTP mail to user
     * @return
     */
    def sendMail() {
        def mailParameters = JSON.parse(request.JSON.toString())
        String mailId = mailParameters.mailId
        String contact = mailParameters.contact
        Long userid = Long.parseLong(mailParameters.userid + "")
        User user = restAPIService.getUser(userid)
        if (user != null) {
            String otp = restAPIService.getUserOTP(user, Constants.OTP_MAIL_SOURCE_KEY)
            log.info("OTP for user : " + userid + " is " + otp)
            //Send A/c verification mail using Grails Plugin
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

    /**
     * API to upload pic
     * @return
     */
    def uploadPic() {
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if (user != null) {
            String picType = params.picType
            log.info("Uploadparams : " + params)
            CommonsMultipartFile imageFile = request.getFile('imageFile')
            byte[] photo = imageFile.bytes
            Boolean imageSaved = restAPIService.saveImage(user, picType, imageFile)
            if (imageSaved) {
                success("Profile Image uploaded successfully")
            } else {
                error("Invalid pic type")
            }
        } else {
            notFound("User does not exist")
        }
    }

    /**
     * API to create user's profile
     * @return
     */
    def createProfile() {
        def postParams = JSON.parse(request.JSON.toString())
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if (userId != null) {
            restAPIService.createOrModifyProfile(user, postParams)
            success("Profile Changes done")
        } else {
            notFound("User does not exist")
        }
    }

    /**
     * API to delete profile
     * @return
     */
    def deleteProfile() {
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if (user != null) {
            restAPIService.deleteProfile(user)
            success("User profile deleted")
        } else {
            notFound("User does not exist")
        }
    }

    /**
     * API to get the list of images (based on the picType) of a user
     * @return
     */

    def getImage() {
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if (user != null) {
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

    /**
     * API to get user's profile details
     * @return
     */
    def showProfile() {
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if (userId != null) {
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

    /**
     * API to send friend request
     * @return
     */
    def sendFriendRequest() {
        def postParams = JSON.parse(request.JSON.toString())
        Long fromUserId = postParams.fromUserId != null ? Long.parseLong(postParams.fromUserId + "") : null
        Long toUserId = postParams.toUserId != null ? Long.parseLong(postParams.toUserId + "") : null
        log.info("friend request from user id : " + fromUserId + " to user id : " + toUserId)
        if (fromUserId != null && toUserId != null) {
            Boolean isFriendRequestSent = restAPIService.checkIfFriendRequestAlreadySent(fromUserId, toUserId)
            log.info("isFriendRequestSent " + isFriendRequestSent)
            if (!isFriendRequestSent) {
                Boolean isFriend = restAPIService.checkIfSenderIsAlreadyAFriend(fromUserId, toUserId)
                log.info("checkIfSenderIsAlreadyAFriend " + isFriend)
                if (isFriend) {
                    success("User is already a friend")
                } else {
                    Boolean isSaved = restAPIService.saveFriendRequest(fromUserId, toUserId)
                    if (isSaved) {
                        User fromUser = User.get(fromUserId)
                        User toUser = User.get(toUserId)
                        success("Friend request sent")

                        UserProfile fromUserProfile = UserProfile.findByUser(fromUser)
                        UserProfile toUserProfile = UserProfile.findByUser(toUser)
                        log.info("ToUser First Name:" + toUserProfile.name + " toUser DeviceId:" + toUser.deviceId)

                        ArrayList<String> deviceTokenArray = new ArrayList<String>()
                        if (toUser.deviceId != null)
                            deviceTokenArray.add(toUser.deviceId)

                        String name = ""
                        if (fromUserProfile.name != null)
                            name = fromUserProfile.name
                        if (deviceTokenArray.size() > 0)
                            restAPIService.sendFCMNotification(deviceTokenArray, name + " has sent you a friend request.", "For friend request")

                    } else {
                        error("Friend request could not be sent")
                    }
                }
            } else {
                success("Friend request already sent")
            }
        } else {
            error("Incorrect user information")
        }
    }

    /**
     * API to accept a friend request
     * @return
     */
    def acceptFriendRequest() {
        def postParams = JSON.parse(request.JSON.toString())
        Long fromUserId = postParams.fromUserId != null ? Long.parseLong(postParams.fromUserId + "") : null
        Long toUserId = postParams.toUserId != null ? Long.parseLong(postParams.toUserId + "") : null
        log.info("acceptFriendRequest from user id : " + fromUserId + " by user id : " + toUserId)
        if (fromUserId != null && toUserId != null) {
            Boolean isSaved = restAPIService.saveFriendship(fromUserId, toUserId)
            if (isSaved) {
                User fromUser = User.get(fromUserId)
                User toUser = User.get(toUserId)
                success("Friend request Accepted")

                UserProfile fromUserProfile = UserProfile.findByUser(fromUser)
                UserProfile toUserProfile = UserProfile.findByUser(toUser)
                ArrayList<String> deviceTokenArray = new ArrayList<String>()
                if (fromUser.deviceId != null)
                    deviceTokenArray.add(fromUser.deviceId)

                log.info("toUser First Name:" + fromUserProfile.name + " toUser DeviceId:" + fromUser.deviceId)

                String name = ""
                if (toUserProfile.name != null)
                    name = toUserProfile.name
                if (deviceTokenArray.size() > 0)
                    restAPIService.sendFCMNotification(deviceTokenArray, name + " has accepted your friend request.", "Friend request accepted.")

            } else {
                error("Friend Request cannot be accepted")
            }
        } else {
            error("Incorrect user information")
        }
    }

    /**
     * API to get a list of pending friend requests
     * @return
     */
    def showFriendRequests() {
        Long profileUserId = params.profileUserId != null ? Long.parseLong(params.profileUserId) : null
        if (profileUserId != null) {
            String requestType = params.requestType ?: Constants.RECIPIENT_FRIEND_REQUEST_API_PATH_STR
            List<UserFriendRequest> friendRequestList = restAPIService.getFriendRequests(profileUserId, requestType)
            UserFriendRequestDTO[] userFriendRequestDTOs = userFriendRequestDTOMapper.mapUserFriendRequestListToUserFriendRequestDTOArray(friendRequestList)
            Expando friendRequestResponse = new Expando()
            friendRequestResponse.friendRequest = userFriendRequestDTOs
            friendRequestResponse.profileUserId = profileUserId
            JSON results = friendRequestResponse.properties as JSON
            success(results)
        } else {
            error("Incorrect user information")
        }
    }

    /**
     * API to unfriend a user
     * @return
     */
    def deleteFriend() {
        Long profileUserId = params.profileUserId != null ? Long.parseLong(params.profileUserId) : null
        Long toBeDeletedUserId = params.toBeDeletedUserId != null ? Long.parseLong(params.toBeDeletedUserId) : null
        if (profileUserId != null && toBeDeletedUserId != null) {
            Boolean isDeleted = restAPIService.deleteFriend(profileUserId, toBeDeletedUserId)
            if (isDeleted) {
                success("Friend deleted")
            } else {
                error("Incorrect user information")
            }
        } else {
            error("Incorrect user information")
        }
    }

    /**
     * API to delete friend request
     * @return
     */
    def deleteFriendRequest() {
        Long senderId = params.senderId != null ? Long.parseLong(params.senderId) : null
        Long recipientId = params.recipientId != null ? Long.parseLong(params.recipientId) : null
        if (senderId != null && recipientId != commentednull) {
            restAPIService.deleteFriendRequest(senderId, recipientId)
            success("Friend request deleted")
        } else {
            error("Incorrect user information")
        }
    }

    /**
     * API to get the list of friends
     * @return
     */
    def getFriends() {
        Long profileUserId = params.profileUserId ? Long.parseLong(params.profileUserId) : null
        if (profileUserId != null) {
            User user = restAPIService.getUser(profileUserId)
            UserProfile profileUser = restAPIService.getProfile(user)
            List<UserFriends> userFriends = restAPIService.getUserFriends(user)
            Expando userFriendsExpando = restAPIService.constructUserFriendList(userFriends)
            userFriendsExpando.profileUser = profileUserId
            JSON results = userFriendsExpando.properties as JSON
            success(results)
        } else {
            error("Missing profile user id")
        }
    }

    /**
     * Mongo test API
     * @return
     */
    def insertMongo() {
        log.info("in insertMongo action")
        mongoService.insertMongo()
        success("Mongo object saved")
    }

    /**
     * API to submit user expression post
     * @return
     */

    def postUserExpression() {
        Long userId = params.userId != null ? Long.parseLong(params.userId + "") : null
        User user = User.get(userId)
        log.info(" in postUserExpression")
        CommonsMultipartFile imageFile = request.getFile('imageFile')

        ObjectId postId = mongoService.saveUserExpression(user, params, imageFile)
        Expando resultExpando = new Expando()
        resultExpando.postId = postId.toString()
        JSON results = resultExpando.properties as JSON
        log.info("UserExpression Result:" + results)
        success(results, "UserExpression saved.")
    }

    /**
     * API to delete user expression
     * @return
     */
    def deleteUserExpression() {
        String postId = params.postId
        log.info("Deleted Post ID:"+postId);
        if (postId != null) {
            //  mongoService.deleteNotifications(postId)
            mongoService.deleteUserExpression(postId)
            success("UserExpression deleted successfully.")
        } else {
            error("UserExpression Id is missing")
        }
    }

    /**
     * API to modify a user expression
     * @return
     */
    def editUserExpression() {
        Long userId = params.userId != null ? Long.parseLong(params.userId + "") : null
        User user = User.get(userId)
        log.info(" in editUserExpression")
        CommonsMultipartFile imageFile = request.getFile('imageFile')
        def postId = new ObjectId(params.postId)

        if (postId != null) {
            ObjectId postIdResult = mongoService.createOrModifyUserExpression(user, params, imageFile, postId)
            Expando resultExpando = new Expando()
            resultExpando.postId = postIdResult.toString()
            JSON results = resultExpando.properties as JSON
            log.info("UserExpression edit postUserExpressionResult:" + results)
            success(results,"UserExpression modified.")
        } else {
            error("UserExpression id is missing.")
        }
    }

    /**
     * API to get a list of user expression
     * @return
     */
    def getUserExpressions() {
        def topPosts = mongoService.getLatestUserExpressions(Integer.parseInt(params.offset))
        UserExpressionDTO[] userExpressionDTOs = userExpressionDTOMapper.mapUserExpressionListToUserExpressionDTOArray(topPosts)
        Expando userExpressionResponse = new Expando()
        userExpressionResponse.userExpression = userExpressionDTOs
        JSON results = userExpressionResponse.properties as JSON
        success(results)
    }

    /**
     * API to get user expression by post ID
     * @return
     */
    def getUserExpression() {
        def list = mongoService.getLatestUserExpression(params.postId)
        UserExpressionDTO[] userExpressionDTOs = userExpressionDTOMapper.mapUserExpressionListToUserExpressionDTOArray(list)
        Expando userExpressionResponse = new Expando()
        userExpressionResponse.userExpression = userExpressionDTOs
        JSON results = userExpressionResponse.properties as JSON
        success(results)
    }

    /**
     * API to submit traveller post
     * @return
     */

    def postTravellerFeed() {
        ArrayList<String> deviceIdForNearByList = new ArrayList<String>();
        ArrayList<String> deviceIdForFriendsList = new ArrayList<String>();
        ArrayList<String> deviceIdForSameDestinationList = new ArrayList<String>();

        log.info(" in post feed")
        def postParams = JSON.parse(request.JSON.toString())
        log.info("Post traveller Data:" + postParams)
        ObjectId postId = mongoService.saveTravellerPost(postParams)
        Expando resultExpando = new Expando()
        resultExpando.postId = postId.toString()
        JSON results = resultExpando.properties as JSON
        log.info("Post Traveller Result:" + results)
        Double[] location = mongoService.getUserLocation(Long.parseLong(postParams.userId + ""))
        success(results, "Traveller Post saved")

        deviceIdForNearByList = getDeviceIdsForNearByTravellerOrGuide(Long.parseLong(postParams.userId + ""), location)
        deviceIdForFriendsList = getDeviceIdsForFriends(Long.parseLong(postParams.userId + ""))
        deviceIdForSameDestinationList = getDeviceIdsForSameDestinationUser(Long.parseLong(postParams.userId + ""), postParams.destination, postParams.startDate, postParams.endDate)

        removeDuplicatesFromLists(deviceIdForSameDestinationList, deviceIdForNearByList, deviceIdForFriendsList);

        User user = User.get(Long.parseLong(postParams.userId + ""))
        UserProfile userProfile = UserProfile.findByUser(user)

        String name = "";
        if (userProfile.name != null)
            name = userProfile.name;

        if (deviceIdForSameDestinationList.size() > 0) {
            restAPIService.sendFCMNotification(deviceIdForSameDestinationList, "There's a match between " + name + "'s travel feed and yours! We recommend you to check it out.", "same destination people.")
        }
        if (deviceIdForFriendsList.size() > 0) {
            restAPIService.sendFCMNotification(deviceIdForFriendsList, name + " has just now posted a new travel feed. Check out his new expeditions!", "friends")
        }
        if (deviceIdForNearByList.size() > 0) {
            restAPIService.sendFCMNotification(deviceIdForNearByList, name + " has unrevealed a new place! See what's new since you last visited.", "Near By friends")
        }
    }

    /**
     * API to delete traveller feed
     * @return
     */
    def deleteTravellerFeed() {
        String postId = params.postId
        if (postId != null) {
            mongoService.deleteNotifications(postId)
            mongoService.deleteTravellerPost(postId)
        } else {
            error("Post Id is missing")
        }
    }

    /**
     * API to modify a traveller's post
     * @return
     */
    def editTravellerPost() {
        def postId = new ObjectId(params.postId)
        def postParams = JSON.parse(request.JSON.toString())
        if (postId != null) {
            mongoService.createOrModifyTravellerPost(postParams, postId)
            success("Traveller post modified")
        } else {
            error("Post Id is missing")
        }
    }

    /**
     * API to get a list of traveller posts
     * @return
     */
    def getTravellerFeeds() {
        def topPosts = mongoService.getLatestTravellerFeeds(Integer.parseInt(params.offset))
        TravellerPostDTO[] travellerPostDTOs = travellerPostDTOMapper.mapTravellerPostListToTravellerPostDTOArray(topPosts)
        Expando travellerFeedResponse = new Expando()
        travellerFeedResponse.travellerFeed = travellerPostDTOs
        JSON results = travellerFeedResponse.properties as JSON
        success(results)
    }

    /**
     * API to submit Guide post
     * @return
     */
    def postGuideFeed() {
        def postParams = JSON.parse(request.JSON.toString())
        def guidePostId = mongoService.saveGuidePost(postParams)
        Expando resultExpando = new Expando()
        resultExpando.postId = guidePostId.toString()
        JSON results = resultExpando.properties as JSON
        success(results, "Guide Post saved")
    }

    /**API to delete guide post
     * @return
     */
    def deleteGuidePost() {
        log.info("In deleteGuidePost")
        String postId = params.postId
        if (postId != null) {
            mongoService.deleteNotifications(postId)
            mongoService.deleteGuidePost(postId)
            success("Guide Post deleted")
        } else {
            error("Guide  Post Id is missing")
        }
    }

    /**
     * API to get the list of guide posts
     * @return
     */
    def getGuideFeeds() {
        log.info("getGuideFeeds")
        def topPosts = mongoService.getTopGuideFeeds(Integer.parseInt(params.offset))
        GuidePostDTO[] guidePostDTOs = guidePostDTOMapper.mapGuidePostListToGuidePostDTOArray(topPosts)
        Expando guideFeedResponse = new Expando()
        guideFeedResponse.guideFeed = guidePostDTOs
        JSON results = guideFeedResponse.properties as JSON
        success(results)
    }

    /**
     * API to edit a guide post
     * @return
     */
    def modifyGuidePost() {
        log.info("In modifyGuidePost")
        def postId = new ObjectId(params.postId)
        def postParams = JSON.parse(request.JSON.toString())
        if (postId != null) {
            mongoService.createOrModifyGuidePost(postParams, postId)
            success("Modified Guide Post")
        } else {
            error("Guide Post Id is missing")
        }
    }

    /**
     * API to store the user's lat-long in db
     * @return
     */
    def storeUserLocation() {
        log.info("In storeUserLocation")
        def postParams = JSON.parse(request.JSON.toString())
        mongoService.saveUserLatLong(postParams)
        success("User Location saved successfully")

    }

    /**
     * API to find users within a radius of 5km of a position (lat-long)
     * @return
     */
    def findNearUsers() {
        def postParams = JSON.parse(request.JSON.toString())
        Double[] location = [Double.parseDouble(postParams.longitude + ""), Double.parseDouble(postParams.latitude + "")]
        List<Long> nearbyUserIds = mongoService.nearSphereWIthMaxDistance(location)
        log.info("nearbyUserIds : " + nearbyUserIds)
        List<User> nearByUsers = restAPIService.getListOfUsersByListOfId(nearbyUserIds)
        List<UserProfile> userProfileList = restAPIService.getListOfUserProfilesByListOfUser(nearByUsers)
        UserProfileDTO[] userProfileDTOs = userProfileDTOMapper.mapUserProfileListToUserProfileDTOArray(userProfileList)
        Expando nearbyUsersResponse = new Expando()
        nearbyUsersResponse.nearbyUsers = userProfileDTOs
        JSON results = nearbyUsersResponse.properties as JSON
        success(results)

    }

    private void removeDuplicatesFromLists(List<String> deviceIdForSameDestinationList, List<String> deviceIdForNearByList, List<String> deviceIdForFriendsList) {
        Iterator<String> sameDestinationIterator = deviceIdForSameDestinationList.iterator();
        Iterator<String> nearByIterator = deviceIdForNearByList.iterator();
        while (sameDestinationIterator.hasNext()) {
            String temp = sameDestinationIterator.next();
            while (nearByIterator.hasNext()) {
                if (temp.equals(nearByIterator.next())) {
                    nearByIterator.remove();
                }
            }
        }

        sameDestinationIterator = deviceIdForSameDestinationList.iterator();
        Iterator<String> friendsIterator = deviceIdForFriendsList.iterator();
        while (sameDestinationIterator.hasNext()) {
            String temp = sameDestinationIterator.next();
            while (friendsIterator.hasNext()) {
                if (temp.equals(friendsIterator.next())) {
                    friendsIterator.remove();
                }
            }
        }

        nearByIterator = deviceIdForNearByList.iterator();
        friendsIterator = deviceIdForFriendsList.iterator();
        while (nearByIterator.hasNext()) {
            String temp = nearByIterator.next();
            while (friendsIterator.hasNext()) {
                if (temp.equals(friendsIterator.next())) {
                    friendsIterator.remove();
                }
            }
        }
    }

    private void sendPushNotificationForPostComment(Long postedById, String postId) {
        log.info("Inside sendPushNotificationForPostComment")
        User user = mongoService.getUserIdFromPostedByIdAndPostId(postedById, postId)
        if (user != null) {
            UserProfile userProfile = UserProfile.findByUser(user)
            log.info("User Name:" + userProfile.name + " Device Id:" + user.deviceId)
            if (userProfile.userId != postedById) {
                ArrayList<String> deviceTokenArray = new ArrayList<String>()
                if (user.deviceId != null)
                    deviceTokenArray.add(user.deviceId)

                User user1 = User.get(postedById)
                UserProfile userProfile1 = UserProfile.findByUser(user1)
                String name = ""
                if (userProfile1.name != null)
                    name = userProfile1.name

                if (deviceTokenArray.size() > 0)
                    restAPIService.sendFCMNotification(deviceTokenArray, name + " has commented on your post.", "Comment on post")
            }
        }
    }

    private void sendPushNotificationForPostCommentReply(Long postedById, String parentCommentId) {
        log.info("Inside sendPushNotificationForPostCommentReply")
        User user = mongoService.getUserIdFromPostedByIdAndParentCommentId(postedById, parentCommentId)

        if (user != null) {
            UserProfile userProfile = UserProfile.findByUser(user)
            log.info("User Name:" + userProfile.name + " Device Id:" + user.deviceId)
            if (userProfile.userId != postedById) {
                ArrayList<String> deviceTokenArray = new ArrayList<String>()
                if (user.deviceId != null)
                    deviceTokenArray.add(user.deviceId)

                User user1 = User.get(postedById)
                UserProfile userProfile1 = UserProfile.findByUser(user1)
                String name = ""
                if (userProfile1.name != null)
                    name = userProfile1.name

                if (deviceTokenArray.size() > 0)
                    restAPIService.sendFCMNotification(deviceTokenArray, name + " has replied on your comment.", "Reply on post")
            }
        }
    }

    private void sendPushNotificationForPostOrCommentLike(Long likedById, String likedObjectId, String likedObjectType) {
        log.info("Inside sendPushNotificationForPostOrCommentLike")
        User user = mongoService.getUserIdFromLikedObjectId(likedById, likedObjectId, likedObjectType)
        if (user != null) {
            UserProfile userProfile = UserProfile.findByUser(user)
            log.info("User Name:" + userProfile.name + " Device Id:" + user.deviceId)
            if (userProfile.userId != likedById) {
                ArrayList<String> deviceTokenArray = new ArrayList<String>()
                if (user.deviceId != null)
                    deviceTokenArray.add(user.deviceId)

                User user1 = User.get(likedById)
                UserProfile userProfile1 = UserProfile.findByUser(user1)
                String name = ""
                if (userProfile1.name != null)
                    name = userProfile1.name

                if (deviceTokenArray.size() > 0) {
                    if (likedObjectType.equals("comment"))
                        restAPIService.sendFCMNotification(deviceTokenArray, name + " has liked your comment.", "like on comment")
                    else
                        restAPIService.sendFCMNotification(deviceTokenArray, name + " has liked your post.", "like on post")
                }
            }
        }
    }

    private List<String> getDeviceIdsForNearByTravellerOrGuide(Long userId, Double[] location) {
        ArrayList<String> deviceTokenList = new ArrayList<String>()
        List<Long> nearbyUserIds = mongoService.nearSphereWIthMaxDistance(location)
        List<User> nearByUsers = restAPIService.getListOfUsersByListOfId(nearbyUserIds)
        for (User u : nearByUsers) {
            if (u.id != userId && u.deviceId != null) {
                deviceTokenList.add(u.deviceId)
            }
        }
        return deviceTokenList
    }

    private List<String> getDeviceIdsForSameDestinationUser(Long userId, String destination, String startDate, String endDate) {
        ArrayList<String> deviceTokenList = new ArrayList<String>()
        ArrayList<Long> userIdList = new ArrayList<Long>()
        List<TravellerPost> travellerPostList = mongoService.getTravellerPostDestination(destination, startDate, endDate)

        for (TravellerPost tp : travellerPostList) {
            if (tp.userId != userId) {
                log.info("SameDestinationUser:" + tp.userId)
                userIdList.add(tp.userId)
            }
        }
        for (Long id : userIdList) {
            if (User.get(id).deviceId != null)
                deviceTokenList.add(User.get(id).deviceId)
        }

        return deviceTokenList
    }

    private List<String> getDeviceIdsForFriends(Long userId) {
        ArrayList<String> deviceTokenList = new ArrayList<String>()
        User user = User.get(userId)
        List<UserFriends> userFriends = restAPIService.getUserFriends(user)
        log.info("User friend size:" + userFriends.size());
        for (UserFriends uf : userFriends) {
            if (uf.friend.deviceId != null) {
                log.info("User friend email ID:" + uf.friend.email + "Device ID:" + uf.friend.deviceId)
                deviceTokenList.add(uf.friend.deviceId)
            }
        }
        return deviceTokenList
    }

    /**
     * API to get the list of notification for a user
     * @return
     */

    def userNotification() {
        def postParams = JSON.parse(request.JSON.toString())
        Long userId = Long.parseLong(params.profileUserId + "")
        List<Notification> userNotifications = mongoService.getUserNotifications(userId)
        log.info("userNotifications for ID : " + userId + " is " + userNotifications)
        NotificationDTO[] notificationDTOs = notificationDTOMapper.mapNotificationListToNotificationDTOArray(userNotifications)
        Expando userNotificationsResponse = new Expando()
        userNotificationsResponse.notifications = notificationDTOs
        userNotificationsResponse.profileUserId = userId
        JSON results = userNotificationsResponse.properties as JSON
        success(results)
    }

    /**
     * Search API
     * @return
     */
    def searchUser() {
        String userName = params.name
        Long userId = params.userId ? Long.parseLong(params.userId) : null
        User user = User.get(userId)
        List<UserProfile> userProfile = restAPIService.getUserProfileByNameLike(userName, user)
        UserProfileDTO[] userProfileDTOs = userProfileDTOMapper.mapUserProfileListToUserProfileDTOArray(userProfile)
        Expando userProfileResponse = new Expando()
        userProfileResponse.userProfile = userProfileDTOs
        userProfileResponse.name = userName
        JSON results = userProfileResponse.properties as JSON
        success(results)
    }

    /**
     * API to add the comment
     * @return
     */
    def addComment() {
        def postParams = JSON.parse(request.JSON.toString())
        Comment savedComment = mongoService.saveComment(null, params, postParams)
        Expando response = new Expando()
        response.id = savedComment.id.toString()
        JSON results = response.properties as JSON
        log.info("Comments result:" + results)
        success(results, "Comments added to Post")
        if (savedComment.parentCommentId == null)
            sendPushNotificationForPostComment(savedComment.postedById, savedComment.postId)
        else
            sendPushNotificationForPostCommentReply(savedComment.postedById, savedComment.parentCommentId)
    }

    /**
     * API to edit the comment
     * @return
     */
    def editComment() {
        def postParams = JSON.parse(request.JSON.toString())
        String commentId = params.commentId
        Comment savedComment = mongoService.saveComment(commentId, null, postParams)
        if (savedComment != null) {
            Expando commentResponse = new Expando()
            commentResponse.id = savedComment.id.toString()
            JSON results = commentResponse.properties as JSON
            success(results, "Comment modified successfully")
        } else {
            notFound("Invalid comment Id")
        }
    }

    /**
     * API to delete the comment
     * @return
     */
    def deleteComment() {
        String commentId = params.commentId
        if (commentId != null) {
            mongoService.deleteComment(commentId)
            success("Comment deleted successfully")
        } else {
            error("CommentId cannot be null")
        }
    }

    /**
     * API to get the list of comments for a post
     * @return
     */
    def getComments() {
        String postId = params.postId
        List<Comment> commentList = mongoService.getCommentListForPost(params)
        List<Expando> commentExpandoList = new ArrayList<Expando>()
        Expando commentExpando = null
        commentList?.each { comment ->
            commentExpando = new Expando()
            commentExpando.id = comment.id.toString()
            commentExpando.commentText = comment.commentText
            commentExpando.postDate = comment.postDate
            commentExpando.postedBy = userProfileDTOMapper.mapUserProfileToUserProfileDTO(UserProfile.findByUser(User.get(comment.postedById)))
            log.info("postID : " + postId + " comment id : " + comment.id.toString())

            /*
            List<Comment> replies = Comment.findAllByPostIdAndParentCommentId(postId, comment.id.toString())
            commentExpando.replies = commentDTOMapper.mapCommentListToCommentDTOArray(replies)
            */

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

    /**
     * API to get the replies for a given comment
     * @return
     */
    def getReplies() {
        String commentId = params.commentId
        List<Comment> replyList = mongoService.getRepliesForComment(commentId)
        Expando replyResponse = new Expando()
        replyResponse.replies = commentDTOMapper.mapCommentListToCommentDTOArray(replyList)
        replyResponse.commentId = commentId
        JSON results = replyResponse.properties as JSON
        success(results)

    }

    /**
     * API to like a post
     * @return
     */
    def addPostLike() {
        def postParams = JSON.parse(request.JSON.toString())
        String likedObjectId = params.objectId
        Like savedLike = mongoService.saveLike(likedObjectId, postParams)
        Expando likeResponse = new Expando()
        likeResponse.id = savedLike.id.toString()
        JSON results = likeResponse.properties as JSON
        success(results, "Like added to ${postParams.likedObjectType}")
        sendPushNotificationForPostOrCommentLike(savedLike.likedBy, savedLike.likedObjectId, savedLike.likedObjectType)
    }

    /**
     * API to remove the like from a post
     * @return
     */
    def deleteLike() {
        String likeId = params.likeId
        if (likeId != null) {
            mongoService.deleteLike(likeId)
            success("Like deleted successfully")
        } else {
            error("LikeId cannot be null")
        }
    }

    /**
     * API to get the no of likes for a post/comment
     * @return
     */
    def getLikesForAnObject() {
        String likedObjectId = params.objectId
        String likedObjectType = params.objectType
        List<Like> postLikes = mongoService.getUserLikes(likedObjectId, likedObjectType.toLowerCase())
        LikeDTO[] likeDTOs = likeDTOMapper.mapLikeListToLikeDTOArray(postLikes)
        Expando postLikeExpando = new Expando()
        postLikeExpando.likes = likeDTOs
        postLikeExpando.likedObjectId = likedObjectId
        JSON results = postLikeExpando.properties as JSON
        success(results)
    }

    /**
     * Reset Password API
     * @return
     */
    def resetPassword() {
        def postParams = JSON.parse(request.JSON.toString())
        String newPassword = postParams.password
        String contact = postParams.contact
        User user = User.findByContact(contact)
        if (user != null) {
            restAPIService.changePassword(user, newPassword)
            success("Password reset successfully")
        } else {
            notFound("Invalid userid")
        }
    }

    /**
     * API to get the list of trip reviews posted by a user
     * @return
     */
    def getTripReview() {
        Long userId = params.userid != null ? Long.parseLong(params.userid) : null
        Expando tripReviewExpando = new Expando()
        tripReviewExpando.userTripReviews = restAPIService.getTripReviews(userId)
        JSON results = tripReviewExpando.properties as JSON
        success(results)
    }

    /**
     * API to save trip review posted by a user
     * @return
     */
    def saveTripReview() {
        def postParams = JSON.parse(request.JSON.toString())
        Long userId = params.userid ? Long.parseLong(params.userid) : null
        User user = User.get(userId)
        if (user != null) {
            Long tripReviewId = params.tripReviewId != null ? Long.parseLong(params.tripReviewId + "") : 0
            TripReview tripReview = restAPIService.saveTripReview(postParams, userId, tripReviewId)
            if (tripReview == null) {
                error("Trip Review with name ${params.title} already exists")
            } else {
                Expando tripReviewResponse = new Expando()
                tripReviewResponse.id = tripReview.id
                JSON results = tripReviewResponse.properties as JSON
                success(results, "Trip Review saved successfully")
            }
        } else {
            notFound("Invalid userid")
        }
    }

    /**
     * API to modify a trip review
     * @return
     */
    def editTripReview() {
        Long tripReviewId = params.tripReviewId != null ? Long.parseLong(params.tripReviewId) : null
        TripReview tripReview = TripReview.get(tripReviewId)
        if (tripReview != null) {
            def postParams = JSON.parse(request.JSON.toString())
            TripReview editedTripReview = restAPIService.saveTripReview(postParams, tripReview.userId, tripReviewId)
            if (editedTripReview == null) {
                error("Trip Review with name ${postParams.title} already exists")
            } else {
                Expando tripReviewResponse = new Expando()
                tripReviewResponse.id = tripReview.id
                JSON results = tripReviewResponse.properties as JSON
                success(results, "Trip Review modified successfully")
            }
        } else {
            notFound("Invalid Trip Review Id")
        }
    }

    /**
     * API to delete a trip review
     * @return
     */
    def deleteTripReview() {
        Long tripReviewId = params.tripReviewId != null ? Long.parseLong(params.tripReviewId) : null
        TripReview tripReview = TripReview.get(tripReviewId)
        if (tripReview != null) {
            restAPIService.deleteTripReview(tripReviewId)
            success("Trip Review with Id ${tripReviewId} deleted successfully")
        } else {
            notFound("Invalid Trip Review Id")
        }
    }

    /**
     * API to upload trip review pics
     * @return
     */
    def uploadTripReviewPic() {
        Long userId = params.userid != null ? Long.parseLong(params.userid + "") : null
        User user = User.get(userId)
        if (user != null) {
            String picType = params.picType
            log.info("uploadTripReviewPic params : " + params)
            CommonsMultipartFile imageFile = request.getFile('imageFile')
            def postParams = JSON.parse(request.JSON.toString())
            Long tripReviewId = params.tripReviewId != null ? Long.parseLong(params.tripReviewId + "") : null
            log.info("tripReviewId " + tripReviewId)

            TripReviewAlbum tripReviewAlbum = restAPIService.saveTripReviewImage(user, picType, imageFile, tripReviewId, params.title)
            TripReview tripReview = tripReviewAlbum.tripReview
            TripReviewDTO tripReviewDTO = tripReviewDTOMapper.mapTripReviewToTripReviewDTO(tripReview)

            if (tripReviewAlbum) {
                Expando tripReviewResponse = new Expando()
                tripReviewResponse.imageId = tripReviewAlbum.id
                tripReviewResponse.tripReview = tripReviewDTO
                JSON results = tripReviewResponse.properties as JSON
                success(results, "Trip Review Image uploaded successfully")
            } else {
                error("Trip Review with name ${params.title} already exists")
            }

        } else {
            notFound("User does not exist")
        }
    }

    /**
     * API to delete a trip review pic
     * @return
     */
    def deleteTripReviewImage() {
        Long imageId = params.imageId != null ? Long.parseLong(params.imageId) : null
        if (imageId != null) {
            restAPIService.deleteTripReviewImage(imageId)
            success("Image with id ${imageId} deleted successfully")
        } else {
            notFound("Invalid Image Id")
        }
    }

    /**
     * API to get the list of trip reviews posted by all user
     * @return
     */
    def getAllTripReview() {
        Expando tripReviewExpando = new Expando()
        tripReviewExpando.userTripReviews = restAPIService.getAllTripReviews()
        JSON results = tripReviewExpando.properties as JSON
        success(results)
    }

    /**
     * API to a given filter feed by city name
     */
    def searchFeed() {
        String cityName = params.cityName
        String feedType = params.feedType
        Integer offset = params.offset ? Integer.parseInt(params.offset) : 0
        def filteredFeeds = mongoService.filterFeedByCity(cityName, feedType, offset)

        //Send filtered traveller feed /guide feed based on the feedType param
        if (Constants.GUIDE_FEED_TYPE.equalsIgnoreCase(feedType)) {
            GuidePostDTO[] guidePostDTOs = guidePostDTOMapper.mapGuidePostListToGuidePostDTOArray(filteredFeeds)
            Expando guideFeedResponse = new Expando()
            guideFeedResponse.filteredFeed = guidePostDTOs
            JSON results = guideFeedResponse.properties as JSON
            success(results)
        } else if (Constants.TRAVELLER_FEED_TYPE.equalsIgnoreCase(feedType)) {
            TravellerPostDTO[] travellerPostDTOs = travellerPostDTOMapper.mapTravellerPostListToTravellerPostDTOArray(filteredFeeds)
            Expando travellerFeedResponse = new Expando()
            travellerFeedResponse.filteredFeed = travellerPostDTOs
            JSON results = travellerFeedResponse.properties as JSON
            success(results)
        } else {
            error("Invalid feed type")
        }
    }

    /**
     * Logout API
     * @return
     */
    def logout() {
        authenticationService.deleteSession(params.sid)
        success("User session deleted")
    }
}
