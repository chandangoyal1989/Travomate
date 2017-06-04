
package com.travomate

import com.travomate.dto.TripReviewDTO
import com.travomate.dto.UserProfileDTO
import com.travomate.security.UserOTP
import com.travomate.tool.TripReviewDTOMapper
import com.travomate.tool.UserDTOMapper
import com.travomate.tool.UserProfileDTOMapper
import org.springframework.web.multipart.commons.CommonsMultipartFile

class RestAPIService {

    def utilityService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()
    UserDTOMapper userDTOMapper = UserDTOMapper.getInstance()
    TripReviewDTOMapper tripReviewDTOMapper = TripReviewDTOMapper.getInstance()

    User getUser(Long id) {
        return User.get(id)
    }

    List<User> getListOfUsersByListOfId(List<Long> userIdList) {
        return User.findAllByIdInList(userIdList)

    }


    List<UserProfile> getListOfUserProfilesByListOfUser(List<User> userList) {
        return UserProfile.findAllByUserInList(userList)
    }

    UserProfile getUserProfileByName(String name) {
        return UserProfile.findByName(name)
    }


    List<UserProfile> getUserProfileByNameLike(String name) {
        return UserProfile.findAllByNameIlike(name)
//        return UserProfile.findAllByNameLike(name)
    }


    public UserProfile getProfile(User user) {
        UserProfile profile = UserProfile.findByUser(user)
        return profile
    }

    public List<UserFriends> getUserFriends(UserProfile userProfile) {
        return UserFriends.findAllByProfileUser(userProfile)
    }

    public List<UserProfileImage> getListOfImagesForUser(User user, String imageType) {
        return UserProfileImage.findAllByUserAndImageType(user, imageType)
    }

    String getUserOTP(User user, String source) {
        String otp = null
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if (userOTPAuthentication == null) {
            otp = utilityService.generateOTP()
            saveUserOTP(user, otp, source)
        } else {
            if (userOTPAuthentication.otp != null) {
                otp = userOTPAuthentication.otp
            } else {
                otp = utilityService.generateOTP()
                saveUserOTP(user, otp, source)
            }
        }

        return otp
    }


    void saveUserOTP(User user, String otp, String source) {
        UserOTP userOTPAuthentication = null
        userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if (userOTPAuthentication == null) {
            userOTPAuthentication = new UserOTP()
        }

        userOTPAuthentication.user = user
        userOTPAuthentication.otp = otp
        userOTPAuthentication.source = source
        userOTPAuthentication.save(flush: true, failOnError: true)
    }


    Boolean checkIfUserOTPIsCorrect(User user, String inputOTP, String source) {
        Boolean isVerified = false
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if (userOTPAuthentication.otp != null && userOTPAuthentication.otp.equalsIgnoreCase(inputOTP)) {
            isVerified = true
            resetUserOTP(user, source)
        }

        return isVerified
    }


    void resetUserOTP(User user, String source) {
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        userOTPAuthentication.otp = null
        userOTPAuthentication.save(flush: true, failOnError: true)
    }

    Boolean updateOTPVerificationStatus(User user, String verifiedId, String source) {
        log.info("updateMailVerificationStatus for user : " + user.contact)
        Boolean isVerified = true
        if (Constants.OTP_MAIL_SOURCE_KEY.equalsIgnoreCase(source)) {
            User userTemp = User.findByEmail(verifiedId)
            if (userTemp != null && userTemp.id != user.id) {
                isVerified = false
            } else {
                user.isEmailVerified = true
                user.email = verifiedId
                user.save(flush: true, failOnError: true)
            }
        } else if (Constants.OTP_PHONE_SOURCE_KEY.equalsIgnoreCase(source)) {
            User userTemp = User.findByContact(verifiedId)
            if (userTemp != null && userTemp.id != user.id) {
                isVerified = false
            } else {
                user.isContactVerified = true
                user.contact = verifiedId
                user.save(flush: true, failOnError: true)
            }
        }
        return isVerified

    }

    def callSMSService(def postParams, String otp) {

        // Prepare Url
        String authkey = Constants.SMS_AUTH_KEY
        // Multiple mobiles numbers separated by comma
        String mMobileNo = postParams.contact;
        // Sender ID,While using route4 sender id should be 6 characters long.
        String senderId = Constants.SMS_SENDER_ID
        // Your message to send, Add URL encoding here.
        String mMessage = "Your one time password for Travomate is :${otp}. Thanks for signing up for Travomate!"
        // define route
        String route = "4"

        String countryCode = Constants.SMS_COUNTRY_ID

        URLConnection myURLConnection = null
        URL myURL = null
        BufferedReader reader = null

        // encoding message
        String encoded_message = URLEncoder.encode(mMessage)

        // Send SMS API
        String mainUrl = Constants.SMS_URL

        // Prepare parameter string
        StringBuilder sbPostData = new StringBuilder(mainUrl)
        sbPostData.append("authkey=" + authkey)
        sbPostData.append("&mobiles=" + (mMobileNo))
        sbPostData.append("&message=" + encoded_message)
        sbPostData.append("&route=" + route)
        sbPostData.append("&sender=" + senderId)
        sbPostData.append("&country=" + countryCode)




        log.info("EmailID ######################## generateMobileAlert" + mMobileNo)
        // final string
        mainUrl = sbPostData.toString()

        System.out.println("sms url : " + mainUrl)

        // prepare connection
        myURL = new URL(mainUrl)
        myURLConnection = myURL.openConnection()
        myURLConnection.connect()
        reader = new BufferedReader(new InputStreamReader(
                myURLConnection.getInputStream()))
        // reading response
        String response
        while ((response = reader.readLine()) != null)
        // print response
            System.out.println(response)
        // finally close connection
        reader.close()


    }


    private void saveImageFileToSystem(String fileImgLoc, CommonsMultipartFile fileDescriptor) {
        File file = new File(fileImgLoc)
        if (!file.exists()) {
            file.mkdirs()
        }
        fileDescriptor.transferTo(new File(fileImgLoc + "${fileDescriptor.originalFilename}"))
    }


    public Boolean saveImage(User user, String picType, CommonsMultipartFile photo) {
        String picName = photo.originalFilename
        String imageBaseDir = Constants.IMAGE_BASE_DIR
        String imageLoc = null
        UserProfile userProfile = UserProfile.findByUser(user)
        if (Constants.PROFILE_TYPE_IMAGE.equalsIgnoreCase(picType)) {
            imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.PROFILE_IMAGE_DIR + Constants.FILE_PATH_DELIMITER
            userProfile.profileImageLoc = imageLoc + picName
        } else if (Constants.COVER_PIC_TYPE_IMAGE.equalsIgnoreCase(picType)) {
            imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.PROFILE_COVER_IMAGE_DIR + Constants.FILE_PATH_DELIMITER
            userProfile.coverImageLoc = imageLoc + picName
        } else if (Constants.ID_PROOF_TYPE_IMAGE.equalsIgnoreCase(picType)) {
            imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.ID_PROOF_IMAGE_DIR + Constants.FILE_PATH_DELIMITER
            userProfile.idProofLoc = imageLoc + picName
        } else {
            return false
        }
        log.info("user id : " + user.id + " pictype : " + picType + " picName : " + picName + " imageloc : " + imageLoc)
        userProfile.save(flush: true, failOnError: true)

        //save image to user_profile_image table
        UserProfileImage userProfileImage = UserProfileImage.findByUserAndImageTypeAndInUse(user, picType, true)
        if (userProfileImage != null) {
            userProfileImage.inUse = false
            userProfileImage.save(failOnError: true, flush: true)

        }
        userProfileImage = new UserProfileImage()
        userProfileImage.imageLoc = imageLoc
        userProfileImage.inUse = true
        userProfileImage.user = user
        userProfileImage.imageType = picType
        userProfileImage.save(failOnError: true, flush: true)


        saveImageFileToSystem(imageLoc, photo)

        return true
    }


    public void createOrModifyProfile(User user, def postParams) {
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("createOrModifyProfile post params : " + postParams)
        System.out.println("languages : " + postParams.languages?.join(","))
        if (userProfile != null) {
            userProfile.name = postParams.name ?: userProfile.name
            userProfile.nationality = postParams.nationality ?: userProfile.nationality
            userProfile.occupation = postParams.occupation ?: userProfile.occupation
            userProfile.languages = postParams.languages ? postParams.languages*.toString().join(',').toString() : userProfile.languages
            userProfile.userIntro = postParams.userIntro ?: userProfile.userIntro
            userProfile.city = postParams.city ?: userProfile.city
            userProfile.state = postParams.state ?: userProfile.state
            userProfile.country = postParams.country ?: userProfile.country
            userProfile.user.dateOfBirth = postParams.dob ?: userProfile.user.dateOfBirth
            userProfile.user.gender = postParams.gender ?: userProfile.user.gender
//            userProfile.idProof = request.getFile('idProof')? request.getFile('idProof').bytes : userProfile.idProof
//            userProfile.coverImage = request.getFile('coverImage')? request.getFile('coverImage').bytes : userProfile.coverImage
//            userProfile.profileImage = request.getFile('profileImage')? request.getFile('profileImage').bytes : userProfile.profileImage
            userProfile.save(flush: true, failOnError: true)
        } else {
            userProfile = new UserProfile()
            userProfile.user = user
            userProfile.name = postParams.name ?: null
            userProfile.nationality = postParams.nationality ?: null
            userProfile.occupation = postParams.occupation ?: null
            userProfile.languages = postParams.languages ? postParams.languages.join(',') : null
            userProfile.userIntro = postParams.userIntro ?: null
            userProfile.city = postParams.city ?: null
            userProfile.state = postParams.state ?: null
            userProfile.country = postParams.country ?: null
            userProfile.user.dateOfBirth = postParams.dob ?: null
            userProfile.user.gender = postParams.gender ?: null
//            userProfile.idProof = request.getFile('idProof')? request.getFile('idProof').bytes : null
//            userProfile.coverImage = request.getFile('coverImage') ? request.getFile('coverImage').bytes : null
//            userProfile.profileImage = request.getFile('profileImage') ? request.getFile('profileImage').bytes : null
            userProfile.save(flush: true, failOnError: true)
        }
    }


    public Boolean saveFriendRequest(Long fromUserId, Long toUserId) {
        Boolean isSaved = true
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if (recipient != null && sender != null) {
            UserFriendRequest friendRequest = new UserFriendRequest()
            friendRequest.recipient = recipient
            friendRequest.sender = sender
            friendRequest.requestSent = new Date()
            if (friendRequest.validate()) {
                friendRequest.save(flush: true, failOnError: true)
            } else {
                isSaved = false
            }
        } else {
            isSaved = false
        }

        return isSaved

    }


    public void deleteFriendRequest(Long fromUserId, Long toUserId) {
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if (recipient != null && sender != null) {
            UserFriendRequest friendRequest = UserFriendRequest.findByRecipientAndSender(recipient, sender)
            friendRequest.delete()
        }
    }


    public List<UserFriendRequest> getFriendRequests(Long profileUserId) {
        log.info("user profile : " + User.get(profileUserId))
        return UserFriendRequest.findAllByRecipient(User.get(profileUserId))

    }


    public Boolean saveFriendship(Long fromUserId, Long toUserId) {
        Boolean isSaved = true
        UserProfile recipient = UserProfile.findByUser(getUser(toUserId))
        UserProfile sender = UserProfile.findByUser(getUser(fromUserId))
        System.out.println("recipient " + recipient + " sender " + sender)
        if (recipient != null && sender != null) {
            UserFriends friendship1 = new UserFriends()
            friendship1.profileUser = recipient
            friendship1.friend = sender
            friendship1.friendshipDate = new Date()

            UserFriends friendship2 = new UserFriends()
            friendship2.profileUser = sender
            friendship2.friend = recipient
            friendship2.friendshipDate = new Date()
            boolean valid = friendship1.validate()
            System.out.println("valid1 : " + valid)
            valid = friendship2.validate()
            System.out.println("valid2 : " + valid)
            friendship1.save(flush: true, failOnError: true)
            friendship2.save(flush: true, failOnError: true)

            //delete friend request
            deleteFriendRequest(fromUserId, toUserId)

        } else {
            isSaved = false
        }

        return isSaved
    }


    public Boolean deleteFriend(Long profileUserId, Long toBeDeletedUserId) {
        Boolean isDeleted = true
        UserProfile profileUser = UserProfile.findByUser(User.get(profileUserId))
        UserProfile toBeDeletedUser = UserProfile.findByUser(User.get(toBeDeletedUserId))

        if (profileUser != null && toBeDeletedUser != null) {
            UserFriends friendship1 = UserFriends.findByProfileUserAndFriend(profileUser, toBeDeletedUser)
            friendship1.delete()

            UserFriends friendship2 = UserFriends.findByProfileUserAndFriend(toBeDeletedUser, profileUser)
            friendship2.delete()
        } else {
            isDeleted = false
        }

        return isDeleted
    }


    public Expando constructUserFriendList(List<UserFriends> userFriends) {
        Expando userFriendsExpando = new Expando()
        List<UserProfileDTO> friendList = new ArrayList<UserProfileDTO>()
        userFriends?.each {
            UserProfile friendProfile = it.friend
            UserProfileDTO friendProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(friendProfile)
            friendList.add(friendProfileDTO)
        }
        userFriendsExpando.friends = friendList

        return userFriendsExpando
    }


    public void deleteProfile(User user) {
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("delete profile user id  : " + user.id)
        UserFriends userFriends = UserFriends.findByProfileUser(userProfile)
        userFriends.delete()
        userProfile.delete()
    }


    public void changePassword(User user, String newPsssword) {
        user.password = newPsssword
        user.save(flush: true, failOnError: true)
    }

    public List<TripReviewDTO> getTripReviews(Long userId) {
        User user = User.get(userId)
        List<TripReview> reviewList

        if (user)
            reviewList = TripReview.findAllByUser(user)
        else
            reviewList = new ArrayList<>()

        List<TripReviewDTO> tripReviewDTOList = tripReviewDTOMapper.mapTripReviewListtoTripReviewDTO(reviewList)
        return tripReviewDTOList
    }

    public void saveTripReview(def postParams) {
        TripReview tripReview = null
        if (postParams.tripReviewId != null) {
            tripReview = TripReview.get(Long.parseLong(postParams.tripReviewId))
        } else {
            tripReview = new TripReview()
        }
        tripReview.user = User.get(postParams?.userid)
        tripReview.routeToTake = postParams.routeToTake
        tripReview.timeToVisit = postParams.timeToVisit
        tripReview.tripDescription = postParams.tripDescription
        tripReview.title = postParams.title
        tripReview.save(flush: true, failOnError: true)
    }


    public Boolean saveTripReviewImage(User user, String picType, CommonsMultipartFile photo) {
        String picName = photo.originalFilename
        String imageBaseDir = Constants.IMAGE_BASE_DIR
        String imageLoc = null
        UserProfile userProfile = UserProfile.findByUser(user)
        if (Constants.TRIP_REVIEW_COVER_IMAGE.equalsIgnoreCase(picType)) {
            imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_IMAGE_DIR + Constants.FILE_PATH_DELIMITER
            userProfile.profileImageLoc = imageLoc + picName
        } else if (Constants.TRIP_REVIEW_ALBUM.equalsIgnoreCase(picType)) {
            String albumName = "";
            imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER  + Constants.PROFILE_COVER_IMAGE_DIR + Constants.FILE_PATH_DELIMITER
            userProfile.coverImageLoc = imageLoc + picName
        } else {
            return false
        }
        log.info("user id : " + user.id + " pictype : " + picType + " picName : " + picName + " imageloc : " + imageLoc)
        userProfile.save(flush: true, failOnError: true)

        //save image to user_profile_image table
        UserProfileImage userProfileImage = UserProfileImage.findByUserAndImageTypeAndInUse(user, picType, true)
        if (userProfileImage != null) {
            userProfileImage.inUse = false
            userProfileImage.save(failOnError: true, flush: true)

        }
        userProfileImage = new UserProfileImage()
        userProfileImage.imageLoc = imageLoc
        userProfileImage.inUse = true
        userProfileImage.user = user
        userProfileImage.imageType = picType
        userProfileImage.save(failOnError: true, flush: true)


        saveImageFileToSystem(imageLoc, photo)

        return true
    }
}
