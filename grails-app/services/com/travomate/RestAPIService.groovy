package com.travomate

import com.travomate.dto.UserDTO
import com.travomate.dto.UserProfileDTO
import com.travomate.security.UserOTP
import com.travomate.tool.UserDTOMapper
import com.travomate.tool.UserProfileDTOMapper
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.jws.soap.SOAPBinding

class RestAPIService {

    def utilityService

    UserProfileDTOMapper userProfileDTOMapper = UserProfileDTOMapper.getInstance()
    UserDTOMapper userDTOMapper = UserDTOMapper.getInstance()


    User getUser(Long id){
        return User.get(id)
    }

    public UserProfile getProfile(User user){
        UserProfile profile = UserProfile.findByUser(user)
        return profile
    }

    public List<UserFriends> getUserFriends(UserProfile userProfile){
        return UserFriends.findAllByProfileUser(userProfile)
    }

    String getUserOTP(User user, String source){
        String otp = null
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if(userOTPAuthentication == null){
            otp = utilityService.generateOTP()
            saveUserOTP(user, otp, source)
        } else {
            if(userOTPAuthentication.otp != null){
                otp = userOTPAuthentication.otp
            } else {
                otp = utilityService.generateOTP()
                saveUserOTP(user, otp, source)
            }
        }

        return otp
    }


    void saveUserOTP(User user, String otp, String source){
        UserOTP userOTPAuthentication = null
        userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if(userOTPAuthentication == null){
            userOTPAuthentication = new UserOTP()
        }

        userOTPAuthentication.user = user
        userOTPAuthentication.otp = otp
        userOTPAuthentication.source = source
        userOTPAuthentication.save(flush: true, failOnError: true)
    }


    Boolean checkIfUserOTPIsCorrect(User user, String inputOTP, String source){
        Boolean isVerified = false
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        if(userOTPAuthentication.otp != null && userOTPAuthentication.otp.equalsIgnoreCase(inputOTP)){
            isVerified = true
            resetUserOTP(user, source)
        }

        return isVerified
    }


    void resetUserOTP(User user, String source){
        UserOTP userOTPAuthentication = UserOTP.findByUserAndSource(user, source)
        userOTPAuthentication.otp = null
        userOTPAuthentication.save(flush:true, failOnError: true)
    }

    Boolean updateOTPVerificationStatus(User user, String verifiedId, String source){
        log.info("updateMailVerificationStatus for user : "+user.contact)
        Boolean isVerified = true
        if(Constants.OTP_MAIL_SOURCE_KEY.equalsIgnoreCase(source)) {
            User userTemp = User.findByEmail(verifiedId)
            if(userTemp != null && userTemp.id != user.id){
                isVerified = false
            } else {
                user.isEmailVerified = true
                user.email = verifiedId
                user.save(flush:true, failOnError: true)
            }
        } else if(Constants.OTP_PHONE_SOURCE_KEY.equalsIgnoreCase(source)){
            User userTemp = User.findByContact(verifiedId)
            if(userTemp != null && userTemp.id != user.id){
                isVerified = false
            } else {
                user.isContactVerified = true
                user.contact = verifiedId
                user.save(flush:true, failOnError: true)
            }
        }
        return isVerified

    }

    def callSMSService(def postParams, String otp){

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
        sbPostData.append("&mobiles=" + (countryCode + mMobileNo))
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


    public Boolean saveImage(User user, String picType, byte[] photo){
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("pic type : "+picType)
        if(Constants.PROFILE_TYPE_IMAGE.equalsIgnoreCase(picType)){
            userProfile.profileImage = photo
        } else if(Constants.COVER_PIC_TYPE_IMAGE.equalsIgnoreCase(picType)){
            userProfile.coverImage = photo
        } else if(Constants.ID_PROOF_TYPE_IMAGE.equalsIgnoreCase(picType)){
            userProfile.idProof = photo
        } else {
            return false
        }
        userProfile.save(flush: true, failOnError: true)
        return true
    }


    public void createOrModifyProfile(User user, def postParams){
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("createOrModifyProfile post params : "+postParams)
        if(userProfile != null){
            userProfile.name = postParams.name ?: userProfile.name
            userProfile.nationality = postParams.nationality ?: userProfile.nationality
            userProfile.occupation = postParams.occupation ?: userProfile.occupation
            userProfile.languages = postParams.languages ? postParams.languages.join(",") : userProfile.languages
            userProfile.userIntro = postParams.userIntro ?: userProfile.userIntro
//            userProfile.idProof = request.getFile('idProof')? request.getFile('idProof').bytes : userProfile.idProof
//            userProfile.coverImage = request.getFile('coverImage')? request.getFile('coverImage').bytes : userProfile.coverImage
//            userProfile.profileImage = request.getFile('profileImage')? request.getFile('profileImage').bytes : userProfile.profileImage
            userProfile.save(flush: true, failOnError: true)
        } else {
            userProfile = new UserProfile()
            userProfile.user = user
            userProfile.name = postParams.name ?: null
            userProfile.nationality = postParams ?: null
            userProfile.occupation = postParams ?: null
            userProfile.languages = postParams.languages ?: null
            userProfile.userIntro = postParams.userIntro ?: null
//            userProfile.idProof = request.getFile('idProof')? request.getFile('idProof').bytes : null
//            userProfile.coverImage = request.getFile('coverImage') ? request.getFile('coverImage').bytes : null
//            userProfile.profileImage = request.getFile('profileImage') ? request.getFile('profileImage').bytes : null
            userProfile.save(flush: true, failOnError: true)
        }
    }


    public Boolean saveFriendRequest(Long fromUserId, Long toUserId){
        Boolean isSaved = true
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if(recipient != null && sender != null){
            UserFriendRequest friendRequest = new UserFriendRequest()
            friendRequest.recipient = recipient
            friendRequest.sender = sender
            friendRequest.requestSent = new Date()
            if(friendRequest.validate()) {
                friendRequest.save(flush: true, failOnError: true)
            } else {
                isSaved = false
            }
        } else {
            isSaved = false
        }

        return isSaved

    }


    public void deleteFriendRequest(Long fromUserId, Long toUserId){
        User recipient = User.get(toUserId)
        User sender = User.get(fromUserId)

        if(recipient != null && sender != null){
            UserFriendRequest friendRequest = UserFriendRequest.findByRecipientAndSender(recipient, sender)
            friendRequest.delete()
        }
    }


    public Boolean saveFriendship(Long fromUserId, Long toUserId) {
        Boolean isSaved = true
        UserProfile recipient = UserProfile.findByUser(getUser(toUserId))
        UserProfile sender = UserProfile.findByUser(getUser(fromUserId))
        System.out.println("recipient "+recipient+" sender "+sender)
        if(recipient != null && sender != null){
            UserFriends friendship1 = new UserFriends()
            friendship1.profileUser = recipient
            friendship1.friend = sender
            friendship1.friendshipDate = new Date()

            UserFriends friendship2 = new UserFriends()
            friendship2.profileUser = sender
            friendship2.friend = recipient
            friendship2.friendshipDate = new Date()
            boolean valid = friendship1.validate()
            System.out.println("valid1 : "+valid)
            valid = friendship2.validate()
            System.out.println("valid2 : "+valid)
            friendship1.save(flush: true, failOnError: true)
            friendship2.save(flush: true, failOnError: true)

            //delete friend request
            deleteFriendRequest(fromUserId, toUserId)

        } else {
            isSaved = false
        }

        return isSaved
    }


    public Boolean deleteFriend(Long profileUserId, Long toBeDeletedUserId){
        Boolean isDeleted  = true
        UserProfile profileUser = UserProfile.findByUser(User.get(profileUserId))
        UserProfile toBeDeletedUser = UserProfile.findByUser(User.get(toBeDeletedUserId))

        if(profileUser != null && toBeDeletedUser != null){
            UserFriends friendship1 = UserFriends.findByProfileUserAndFriend(profileUser, toBeDeletedUser)
            friendship1.delete()

            UserFriends friendship2 = UserFriends.findByProfileUserAndFriend(toBeDeletedUser, profileUser)
            friendship2.delete()
        } else {
            isDeleted = false
        }

        return isDeleted
    }


    public Expando constructUserFriendList(List<UserFriends> userFriends){
        Expando userFriendsExpando = new Expando()
        List<UserProfileDTO> friendList = new ArrayList<UserProfileDTO>()
        userFriends?.each{
            UserProfile friendProfile = it.friend
            UserProfileDTO friendProfileDTO = userProfileDTOMapper.mapUserProfileToUserProfileDTO(friendProfile)
            friendList.add(friendProfileDTO)
        }
        userFriendsExpando.friends = friendList

        return userFriendsExpando
    }


    public void deleteProfile(User user){
        UserProfile userProfile = UserProfile.findByUser(user)
        System.out.println("delete profile user id  : "+user.id)
        userProfile.delete()
    }


}
