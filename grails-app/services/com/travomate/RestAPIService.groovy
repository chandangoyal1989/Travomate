package com.travomate

import com.travomate.security.UserOTP

class RestAPIService {

    def utilityService

    User getUser(Long id){
        return User.get(id)
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
        String mMessage = "Your one time password for OneSS.com is :${otp}. Thanks for signing up for oness!"
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
        sbPostData.append("&mobiles=" + mMobileNo)
        sbPostData.append("&message=" + encoded_message)
        sbPostData.append("&route=" + route)
        sbPostData.append("&sender=" + senderId)
        sbPostData.append("&sender=" + senderId)
        sbPostData.append("&country=" + countryCode)

        log.info("EmailID ######################## generateMobileAlert" + mMobileNo)
        // final string
        mainUrl = sbPostData.toString()
        try {
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
        } catch (IOException e) {
            e.printStackTrace()
        }

    }
}
