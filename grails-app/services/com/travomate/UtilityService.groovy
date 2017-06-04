package com.travomate

import java.util.regex.Matcher
import java.util.regex.Pattern

class UtilityService {

    public String generateOTP(){
        Random rnd = new Random()
        String otp = (100000 + rnd.nextInt(900000)).toString()
        return otp
    }

    public String removeWhitespace(String str){
        String parsedStr = str
        if(checkIfStringContainsWhitespace(parsedStr)){
            log.info("contains whitespace")
            parsedStr = parsedStr.replaceAll("\\s","")
        }

        return parsedStr
    }

    Boolean checkIfStringContainsWhitespace(String str){
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(str);
        boolean found = matcher.find();
        log.info " str "+str+" found "+found
        return found
    }
}
