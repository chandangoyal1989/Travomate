package com.travomate.dto

import com.travomate.User

/**
 * Created by mchopra on 1/12/2017.
 */
class UserProfileDTO {

    Long id
    User user
    String name
    String nationality
    String occupation
    byte[] idProof
    String languages
    String userIntro
    byte[] profileImage
    byte[] coverImage
}
