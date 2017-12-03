
package com.travomate

import com.travomate.dto.TripReviewAlbumDTO
import com.travomate.dto.TripReviewDTO
import com.travomate.dto.UserProfileDTO
import com.travomate.security.UserOTP
import com.travomate.tool.TripReviewAlbumDTOMapper
import com.travomate.tool.TripReviewDTOMapper
import com.travomate.tool.UserDTOMapper
import com.travomate.tool.UserProfileDTOMapper
import org.apache.commons.io.FileUtils
import org.json.simple.JSONObject
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.nio.file.Files
import java.text.SimpleDateFormat

@Transactional
class RestAPIService {

    def utilityService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()
    UserDTOMapper userDTOMapper = UserDTOMapper.getInstance()
    TripReviewDTOMapper tripReviewDTOMapper = TripReviewDTOMapper.getInstance()
    TripReviewAlbumDTOMapper tripReviewAlbumDTOMapper = TripReviewAlbumDTOMapper.getInstance()

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


    List<UserProfile> getUserProfileByNameLike(String name, User user) {
        return UserProfile.findAllByNameIlikeAndUserNotEqual("%${name}%", user)
//        return UserProfile.findAllByNameLike(name)
    }


    public UserProfile getProfile(User user) {
        UserProfile profile = UserProfile.findByUser(user)
        return profile
    }

    public List<UserFriends> getUserFriends(User user) {
        return UserFriends.findAllByProfileUser(user)
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

    /**
     * Verifies the OTP sent by the user
     * @param user
     * @param inputOTP
     * @param source
     * @return
     */
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

    /**
     * Updates OTP verification status in db
     * @param user
     * @param verifiedId
     * @param source
     * @return
     */
    Boolean updateOTPVerificationStatus(User user, String verifiedId, String source) {
        log.info("updateMailVerificationStatus for user : " + user.contact)
        Boolean isVerified = true

        //Mail OTP
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
            //Phone OTP
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
            log.info(response)
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


    /**
     * This method saves image to filesystem and saved image;s location in db
     * @param user
     * @param picType
     * @param photo
     * @return
     */
    public Boolean saveImage(User user, String picType, CommonsMultipartFile photo) {
        String picName = photo.originalFilename
        String imageBaseDir = Constants.IMAGE_BASE_DIR
        String imageLoc = null
        try {
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
        }catch(Exception e){
            log.error("Error while saving the image", e);
            reutrn false
        }

        return true
    }

    /**
     * This method creates/edit a user's profile
     * @param user
     * @param postParams
     */
    public void createOrModifyProfile(User user, def postParams) {
        UserProfile userProfile = UserProfile.findByUser(user)
        log.info("createOrModifyProfile post params : " + postParams)
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
            userProfile.verificationStatus = Constants.VerificationStatus.Pending.toString();
            userProfile.save(flush: true, failOnError: true)
        }
    }


    /**
     * This method checks if a friend request is already sent by the user
     * @param fromUserId
     * @param toUserId
     * @return
     */
    public Boolean checkIfFriendRequestAlreadySent(Long fromUserId, Long toUserId) {
        Boolean isFriendRequestSent = false;
        User sender = User.get(fromUserId)
        User recipient = User.get(toUserId)
        log.info("sender "+sender + " recipient "+recipient)
        UserFriendRequest userFriendRequest1 = UserFriendRequest.findByRecipientAndSender(sender, recipient);
        UserFriendRequest userFriendRequest = UserFriendRequest.findByRecipientAndSender(recipient, sender);
        if(userFriendRequest != null || userFriendRequest1 != null){
            isFriendRequestSent = true
        }

        return isFriendRequestSent;
    }


    /**
     * This method checks if a user is already a friend of another user
     * @param fromUserId
     * @param toUserId
     * @return
     */
    public Boolean checkIfSenderIsAlreadyAFriend(Long fromUserId, Long toUserId){
        Boolean isFriend = false
        User sender = User.get(fromUserId)
        User recipient = User.get(toUserId)
        UserFriends friendship1 = UserFriends.findByFriendAndProfileUser(sender, recipient)
        UserFriends friendship2 = UserFriends.findByFriendAndProfileUser(recipient, sender)
        log.info("friendship1 " + friendship1 + " friendship2 " + friendship2)
        if(friendship1 != null && friendship2 != null){
            isFriend = true
        }

        return isFriend
    }

    /**
     * Saves friend request sent by a user to another user
     * @param fromUserId
     * @param toUserId
     * @return
     */
    public Boolean saveFriendRequest(Long fromUserId, Long toUserId) {
        Boolean isSaved = true
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if (recipient != null && sender != null) {
            UserFriendRequest friendRequest = new UserFriendRequest()
            friendRequest.recipient = recipient
            friendRequest.sender = sender
            friendRequest.requestSent = new Date()
            friendRequest.save(flush: true, failOnError: true)
        } else {
            isSaved = false
        }

        return isSaved

    }

    /**
     * Deletes friend request
     * @param fromUserId
     * @param toUserId
     */
    public void deleteFriendRequest(Long fromUserId, Long toUserId) {
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if (recipient != null && sender != null) {
            UserFriendRequest friendRequest = UserFriendRequest.findByRecipientAndSender(recipient, sender)
            if(friendRequest != null) {
                friendRequest.delete()
            }
        }
    }

    /**
     * Get the list of friend requests for a user
     * @param profileUserId
     * @param requestType
     * @return
     */
    public List<UserFriendRequest> getFriendRequests(Long profileUserId, String requestType) {
        //Friend requests received by the given user
        if(Constants.RECIPIENT_FRIEND_REQUEST_API_PATH_STR.equalsIgnoreCase(requestType)) {
            return UserFriendRequest.findAllByRecipient(User.get(profileUserId))
        } else {
            //Friend requests sent by the given user
            return UserFriendRequest.findAllBySender(User.get(profileUserId))
        }

    }

    /**
     * Creates friendship relationship between 2 users
     * @param fromUserId
     * @param toUserId
     * @return
     */
    public Boolean saveFriendship(Long fromUserId, Long toUserId) {
        Boolean isSaved = false
        User recipient = getUser(toUserId)
        User sender = getUser(fromUserId)
        System.out.println("recipient " + recipient + " sender " + sender)
        try {
            if (recipient != null && sender != null) {

                //Add friendship only if the 2 users are already not friends of each other
                List<UserFriends> existingFriendList = UserFriends.findAllByProfileUserInListAndFriendInList([recipient, sender], [recipient, sender])
                log.info("existingFriendList " + existingFriendList)

                if (existingFriendList == null || existingFriendList?.size() == 0) {
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
                    isSaved = true
                }

            }
        }catch(Exception e){
            log.error("Failed to save friendship between users with id " + fromUserId + " and  " + toUserId,e)
        }

        return isSaved
    }

    /**
     * This method removes a user from friend list of another user
     * @param profileUserId
     * @param toBeDeletedUserId
     * @return
     */
    public Boolean deleteFriend(Long profileUserId, Long toBeDeletedUserId) {
        Boolean isDeleted = true
        User profileUser = User.get(profileUserId)
        User toBeDeletedUser = User.get(toBeDeletedUserId)

        //Delete friendship from both ends
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

    /**
     * Returns the list of friends of a user
     * @param userFriends
     * @return
     */
    public Expando constructUserFriendList(List<UserFriends> userFriends) {
        Expando userFriendsExpando = new Expando()
        List<UserProfileDTO> friendList = new ArrayList<UserProfileDTO>()
        userFriends?.each {
            User friendProfile = it.friend
            UserProfileDTO friendProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(UserProfile.findByUser(friendProfile))
            friendList.add(friendProfileDTO)
        }
        userFriendsExpando.friends = friendList

        return userFriendsExpando
    }

    /**
     * Delete a user's profile
     * @param user
     */
    public void deleteProfile(User user) {
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("delete profile user id  : " + user.id)
        UserFriends userFriends = UserFriends.findByProfileUser(user)
        userFriends.delete()
        userProfile.delete()
    }

    /**
     * Change password
     * @param user
     * @param newPsssword
     */
    public void changePassword(User user, String newPsssword) {
        user.password = newPsssword
        user.save(flush: true, failOnError: true)
    }


    /**
     * Returns the list of trip reviews posted by a user
     * @param userId
     * @return
     */
    public List<Expando> getTripReviews(Long userId) {
        List<Expando> tripReviewExpandoList = new ArrayList<Expando>()
        Expando tripReviewExpando = null
        User user = User.get(userId)
        List<TripReview> reviewList

        if (user)
            reviewList = TripReview.findAllByUser(user)
        else
            reviewList = new ArrayList<>()

        reviewList?.each{ review ->
            tripReviewExpando = new Expando()
            TripReviewDTO tripReviewDTO = tripReviewDTOMapper.mapTripReviewToTripReviewDTO(review)
            List<TripReviewAlbum> tripReviewAlbumList = TripReviewAlbum.findAllByTripReview(review)
            TripReviewAlbumDTO[] tripReviewAlbumDTOArray = tripReviewAlbumDTOMapper.mapTripReviewAlbumListtoTripReviewAlbumDTO(tripReviewAlbumList)
            tripReviewExpando.review = tripReviewDTO
            tripReviewExpando.pics = tripReviewAlbumDTOArray
            tripReviewExpandoList.add(tripReviewExpando.properties)
         }
        return tripReviewExpandoList
    }

    /**
     * Check Duplicate trip review name
     * @param tripReviewTitle
     * @param user
     * @param tripReviewId
     * @return
     */
    public Boolean checkIfTripReviewWithSameNameExists(String tripReviewTitle, User user, Long tripReviewId){
        TripReview tripReview = user != null ? TripReview.findByTitleAndUser(tripReviewTitle, user) : TripReview.findByTitle(tripReviewTitle)
        log.info("checkIfTripReviewWithSameNameExists tripReviewID : " + tripReviewId + " db tripReview Id : " + tripReview?.id)
        if(tripReview != null ){
            if((tripReviewId != 0 && tripReviewId == tripReview.id)) {
                return false
            } else {
                return true
            }
        }
        return false
    }

    /**
     * Saves trip review in db
     * @param params
     * @param userId
     * @param tripReviewId
     * @return
     */
    public TripReview saveTripReview(def params, Long userId, Long tripReviewId) {
        TripReview tripReview = null
        User user = (userId != null) ? User.get(userId) : null
        try {
            Boolean isDuplicate = checkIfTripReviewWithSameNameExists(params.title, user, tripReviewId)
            log.info("isDuplicate trip review : " + isDuplicate)
            if (isDuplicate) {
                log.info("Duplicate trip review title")
            } else {
                //Create a new trip review if it doesn't exist, otherwise get the existing review
                if (tripReviewId != 0) {
                    tripReview = TripReview.get(tripReviewId)
                } else {
                    tripReview = new TripReview()
                }
                if (userId != null) {
                    tripReview.user = User.get(userId)
                }
                log.info("Trip review : " + tripReview)
                tripReview.routeToTake = params.routeToTake
                tripReview.timeToVisit = params.timeToVisit
                tripReview.tripDescription = params.tripDescription
                tripReview.title = params.title
                tripReview = tripReview.save(flush: true, failOnError: true)
                log.info("saved Trip review : " + tripReview.title)
            }
        }catch(Exception e){
            log.error("Failed to save trip review for userId " + userId,e)
        }
        return tripReview
    }


    /**
     * Deletes the trip review and all related information
     * @param tripReviewId
     */
    public void deleteTripReview(Long tripReviewId){
        TripReview toBeDeleted = TripReview.get(tripReviewId)

        //Delete related tripreview album and cover pic
        deleteAllTripReviewImages(toBeDeleted)

        //Delete trip review
        toBeDeleted.delete();
    }


    public TripReview saveTripReview(Long tripReviewId){
        TripReview tripReview = TripReview.get(tripReviewId);
        if(tripReview == null){
            tripReview = new TripReview()
        }
        tripReview.user = user;
        tripReview = tripReview.save(flush:true, failOnError: true)
        return tripReview
    }

    /**
     * Deletes all images of a trip review
     * @param tripReview
     */
    public void deleteAllTripReviewImages(TripReview tripReview){
        List<TripReviewAlbum> tripReviewAlbumList = TripReviewAlbum.findAllByTripReview(tripReview);
        TripReviewAlbum.deleteAll(tripReviewAlbumList)
        String toBeDeletedDir = Constants.IMAGE_BASE_DIR + tripReview.user.id + Constants.FILE_PATH_DELIMITER  + Constants.TRIP_REVIEW_IMAGE_DIR + Constants.FILE_PATH_DELIMITER + tripReview.title + "_" + tripReview.id + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_ALBUM_DIR + Constants.FILE_PATH_DELIMITER
        FileUtils.deleteDirectory(new File(toBeDeletedDir));
    }

    /**
     * Saves a trip review image to the file system and updates the location in db
     * @param user
     * @param picType
     * @param photo
     * @param tripReviewId
     * @param tripReviewTitle
     * @return
     */
    public TripReviewAlbum saveTripReviewImage(User user, String picType, CommonsMultipartFile photo, Long tripReviewId, String tripReviewTitle) {
        String picName = photo.originalFilename
        String imageBaseDir = Constants.IMAGE_BASE_DIR
        String imageLoc = null
        TripReview tripReview = null
        TripReviewAlbum tripReviewAlbum = new TripReviewAlbum();
        try {
            if (tripReviewId == 0) {

                //Duplicate trip review name
                Boolean isDuplicate = checkIfTripReviewWithSameNameExists(tripReviewTitle, user, tripReviewId)
                log.info("isDuplicate trip review : " + isDuplicate)
                if (isDuplicate) {
                    return null
                }
                tripReview = new TripReview()
                tripReview.user = user
                tripReview.title = tripReviewTitle
                tripReview = tripReview.save(failOnError: true)
            } else {
                tripReview = TripReview.get(tripReviewId)
            }


            tripReviewAlbum.tripReview = tripReview
            String todaysDate = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
            String tripReviewDir = tripReview.title + "_" + tripReview.id;

            //Saves image pic/ profile pic at respective locations
            if (Constants.TRIP_REVIEW_COVER_IMAGE.equalsIgnoreCase(picType)) {
                TripReviewAlbum currentCoverPic = TripReviewAlbum.findByTripReviewAndIsCover(tripReview, true);
                if (currentCoverPic != null) {
                    currentCoverPic.isCover = false
                    currentCoverPic.save(flush: true, failOnError: true)
                }
                imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_IMAGE_DIR + Constants.FILE_PATH_DELIMITER + tripReviewDir + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_COVER_PIC_DIR + Constants.FILE_PATH_DELIMITER
                tripReviewAlbum.isCover = true
            } else if (Constants.TRIP_REVIEW_ALBUM.equalsIgnoreCase(picType)) {
                imageLoc = imageBaseDir + user.id + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_IMAGE_DIR + Constants.FILE_PATH_DELIMITER + tripReviewDir + Constants.FILE_PATH_DELIMITER + Constants.TRIP_REVIEW_ALBUM_DIR + Constants.FILE_PATH_DELIMITER
                tripReviewAlbum.isCover = false
            }
            tripReviewAlbum.imageLoc = imageLoc + picName
            log.info("user id : " + user.id + " pictype : " + picType + " picName : " + picName + " imageloc : " + imageLoc)
            tripReviewAlbum = tripReviewAlbum.save(flush: true, failOnError: true)

            saveImageFileToSystem(imageLoc, photo)
        }catch(Exception e){
            log.error("Fail to save trip review image for user with id " + user.id, e);
        }

        return tripReviewAlbum

    }


    public void deleteTripReviewImage(Long imageId){
        TripReviewAlbum reviewImage = TripReviewAlbum.get(imageId)
        String imageLoc = reviewImage.imageLoc
        reviewImage.delete()
        File fileToBeDeleted = new File(imageLoc)
        try{
            Boolean isDeleted = Files.deleteIfExists(fileToBeDeleted.toPath())
            log.info("File with id ${imageId} got deleted : " + isDeleted)
        } catch(Exception e){
            log.info("Image with id ${imageId} could not be deleted")
            e.printStackTrace()
        }


    }

    /**
     * FCM notification test method
     * @param deviceToken
     * @param notificationType
     * @return
     * @throws Exception
     */
    public String sendFCMNotification(String deviceToken, Constants.NotificationType notificationType) throws Exception{
        String result = "";
        URL url = new URL(Constants.API_URL_FCM);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "key=${Constants.AUTH_KEY_FCM}");
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();

        json.put("to", deviceToken.trim());
        JSONObject info = new JSONObject();
        info.put("title", "Travomate"); // Notification title
        info.put("body", "message body"); // Notification
        // body
        json.put("notification", info);
        try {
            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());
            wr.write(json.toString());
            wr.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            result = "failure";
        }
        System.out.println("FCM Notification is sent successfully");

        return result;
    }
}
