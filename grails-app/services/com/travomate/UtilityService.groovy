package com.travomate

class UtilityService {

    public String generateOTP(){
        Random rnd = new Random()
        String otp = (100000 + rnd.nextInt(900000)).toString()
        return otp
    }
}
